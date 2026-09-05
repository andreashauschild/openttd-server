package de.litexo;

import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.BaseProcess;
import lombok.Getter;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(ProcessThread.class);

    private static final int DEFAULT_MAX_LOG_CHARS = 10_000_000;
    private static final int DEFAULT_KEEP_LOG_CHARS = 3_000_000;
    private static final int PROCESS_DATA_CHARS = 20_000;
    private static final long PUMP_POLL_INTERVAL_MS = 100;
    private static final long FORCIBLE_STOP_TIMEOUT_MS = 5_000;
    private static final long READER_DRAIN_TIMEOUT_MS = 5_000;

    /**
     * Prefix of the synthetic line that is appended when the process is gone. The batch that contains it is the last
     * batch of the process and is the one that carries the exit code.
     */
    static final String EXIT_LINE_PREFIX = "[openttd-server] process exited with code ";

    private final EventBus eventBus;

    @Getter
    private final String uuid = UUID.randomUUID().toString();

    ExecutorService executorService;

    @Getter
    List<String> command;

    private final int maxLogChars;

    private final int keepLogChars;

    /**
     * Every line of stdout, stderr and of the console input in the order it was produced. Only the pump task reads it,
     * so no line can get lost or garbled.
     */
    private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

    /**
     * The whole console history. Guarded by its own monitor and never replaced, because the pump appends while the
     * commands and the REST layer read.
     */
    private final StringBuilder logs = new StringBuilder();

    /**
     * Number of characters that were removed from the beginning of {@link #logs} by the trim. Offsets handed out to
     * clients are 'discardedChars + index in logs', so they stay valid over a trim. Guarded by the monitor of
     * {@link #logs}, like the history itself.
     */
    private long discardedChars = 0;

    private volatile boolean stopped = false;

    @Getter
    private Integer exitCode;

    private ProcessOutputThread processOutput;

    private ProcessOutputThread processErrorOutput;

    private ProccesInputThread processInput;

    @Getter
    private Process process;

    public ProcessThread(ExecutorService executorService, List<String> command, EventBus eventBus) {
        this(executorService, command, eventBus, DEFAULT_MAX_LOG_CHARS, DEFAULT_KEEP_LOG_CHARS);
    }

    ProcessThread(ExecutorService executorService, List<String> command, EventBus eventBus, int maxLogChars, int keepLogChars) {
        this.executorService = executorService;
        this.command = command;
        this.eventBus = eventBus;
        this.maxLogChars = maxLogChars;
        this.keepLogChars = keepLogChars;
    }

    @Override
    public void run() {
        try {
            start(this.command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.error("Process '" + this.command + "' terminated with an error", e);
        }
    }

    private void start(List<String> cmd) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmd);
        this.process = builder.start();

        this.processOutput = new ProcessOutputThread(this.process.getInputStream(), this.lines);
        this.processErrorOutput = new ProcessOutputThread(this.process.getErrorStream(), this.lines);
        this.processInput = new ProccesInputThread(this.process, this.lines);
        Future<?> outputTask = this.executorService.submit(this.processOutput);
        Future<?> errorOutputTask = this.executorService.submit(this.processErrorOutput);
        this.executorService.execute(this.processInput);
        this.executorService.execute(this::pumpLines);

        this.process.waitFor();
        this.exitCode = this.process.exitValue();
        this.processInput.stop();
        // 'waitFor' returns while the readers may still be draining the pipes. Waiting for EOF on both of them keeps
        // the synthetic exit line the last line of the console history.
        awaitReader(outputTask);
        awaitReader(errorOutputTask);
        // Make the exit visible in the ui terminal, then let the pump drain the rest and terminate.
        this.lines.put(EXIT_LINE_PREFIX + this.exitCode);
        this.stopped = true;
        LOG.infof("Process '%s' exited with code %d", this.uuid, this.exitCode);
    }

    private void awaitReader(Future<?> readerTask) {
        try {
            readerTask.get(READER_DRAIN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            LOG.debugf(e, "Draining the output of process '%s' did not finish cleanly", this.uuid);
        }
    }

    private void pumpLines() {
        try {
            while (!this.stopped || !this.lines.isEmpty()) {
                String first = this.lines.poll(PUMP_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                List<String> batch = new ArrayList<>();
                batch.add(first);
                this.lines.drainTo(batch);
                String text = String.join("\n", batch) + "\n";
                boolean lastBatch = batch.stream().anyMatch(line -> line.startsWith(EXIT_LINE_PREFIX));
                long offset;
                synchronized (this.logs) {
                    offset = this.discardedChars + this.logs.length();
                    this.logs.append(text);
                    // Quick solution to prevent out of memory if logs get to big
                    if (this.logs.length() > this.maxLogChars) {
                        int discarded = this.logs.length() - this.keepLogChars;
                        this.logs.delete(0, discarded);
                        this.discardedChars += discarded;
                    }
                }
                this.eventBus.publish(new OpenttdTerminalUpdateEvent(this, this.uuid, text, offset, lastBatch ? this.exitCode : null));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.error("Terminal pump of process '" + this.uuid + "' failed", e);
        }
    }

    public void write(String data) {
        if (data != null && this.processInput != null) {
            this.processInput.write(data);
        }
    }

    public String getLogs() {
        synchronized (this.logs) {
            return this.logs.toString();
        }
    }

    public int getLogsLength() {
        synchronized (this.logs) {
            return this.logs.length();
        }
    }

    /**
     * @param index index in the console history the window starts at
     * @return the console history beginning at the given index, empty if nothing was written since then
     */
    public String getLogsFrom(int index) {
        synchronized (this.logs) {
            if (index < 0 || index >= this.logs.length()) {
                return "";
            }
            return this.logs.substring(index);
        }
    }

    /**
     * @return the absolute offset right after the last character the process produced, i.e. the number of characters
     *         it produced since it was started
     */
    public long getAbsoluteEnd() {
        synchronized (this.logs) {
            return this.discardedChars + this.logs.length();
        }
    }

    /**
     * @param absoluteOffset absolute offset, as published with {@link OpenttdTerminalUpdateEvent#getOffset()}
     * @return the console history from that offset on, empty if the offset was already trimmed away or lies behind
     *         the end of the history
     */
    public Optional<String> getLogsFromAbsolute(long absoluteOffset) {
        synchronized (this.logs) {
            long index = absoluteOffset - this.discardedChars;
            if (index < 0 || index > this.logs.length()) {
                return Optional.empty();
            }
            return Optional.of(this.logs.substring((int) index));
        }
    }

    /**
     * Reads the console history and its end offset in one step. Both are taken under the monitor the pump appends
     * under, so the end offset always falls on a batch boundary and a client can drop every update batch that starts
     * before it.
     *
     * @param fromOffset absolute offset the client already has, {@code null} for "everything you would show anyway"
     * @return the history from that offset on, or the last {@value #PROCESS_DATA_CHARS} characters if the offset was
     *         trimmed away or is unknown
     */
    public Snapshot snapshot(Long fromOffset) {
        synchronized (this.logs) {
            long end = this.discardedChars + this.logs.length();
            Optional<String> missing = fromOffset != null ? getLogsFromAbsolute(fromOffset) : Optional.empty();
            if (missing.isPresent()) {
                return new Snapshot(missing.get(), end);
            }
            return new Snapshot(this.logs.substring(Math.max(0, this.logs.length() - PROCESS_DATA_CHARS)), end);
        }
    }

    /**
     * @param text      console history the client is missing
     * @param endOffset absolute offset right after {@code text}
     */
    public record Snapshot(String text, long endOffset) {
    }

    public boolean isAlive() {
        return this.process != null && this.process.isAlive();
    }

    public boolean waitFor(long timeoutMs) {
        if (this.process == null) {
            return true;
        }
        try {
            return this.process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Sends 'quit' to the console and gives the process time to write its save games and to shut down its network.
     * Killing OpenTTD while it is writing a save game leaves a truncated, unreadable .sav behind.
     *
     * @param quitTimeoutMs time the process gets to exit on its own before it is killed
     * @return true if the process exited on its own
     */
    public boolean stopGracefully(long quitTimeoutMs) {
        if (this.process == null) {
            return true;
        }
        write("quit");
        boolean exited = waitFor(quitTimeoutMs);
        if (!exited) {
            LOG.warnf("Process '%s' did not exit within %d ms after 'quit'. It will be killed now.", this.uuid, quitTimeoutMs);
            this.process.destroyForcibly();
            waitFor(FORCIBLE_STOP_TIMEOUT_MS);
        }
        if (this.processInput != null) {
            this.processInput.stop();
        }
        return exited;
    }

    public void stop() {
        try {
            if (this.process != null) {
                this.process.destroyForcibly();
                waitFor(FORCIBLE_STOP_TIMEOUT_MS);
            }
        } catch (Exception e) {
            LOG.error("Failed to stop process '" + this.uuid + "'", e);
        } finally {
            if (this.processInput != null) {
                this.processInput.stop();
            }
        }
    }

    public BaseProcess toModel() {
        // Only send last 20000 chars to processdata
        Snapshot snapshot = this.snapshot(null);
        return new BaseProcess()
                .setProcessData(snapshot.text())
                .setProcessId(this.getUuid())
                .setExitCode(this.exitCode);
    }
}
