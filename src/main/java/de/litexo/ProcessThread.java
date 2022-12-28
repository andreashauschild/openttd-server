package de.litexo;

import de.litexo.api.ServiceRuntimeException;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.BaseProcess;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class ProcessThread implements Runnable {

    private final EventBus eventBus;

    @Getter
    private String uuid = UUID.randomUUID().toString();

    ExecutorService executorService;

    @Getter
    List<String> command;

    StringBuilder logs = new StringBuilder();

    private StringBuilder console = new StringBuilder();

    private ProcessOutputThread processOutput;
    private ProcessOutputThread processErrorOutput;
    private ProccesInputThread processInput;
    @Getter
    private Process process;

    public ProcessThread(ExecutorService executorService, List<String> command, EventBus eventBus) {
        this.executorService = executorService;
        this.command = command;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        try {
            start(this.command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUuid() {
        return uuid;
    }

    private void start(List<String> cmd) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmd);
        process = builder.start();

        this.processOutput = new ProcessOutputThread(process.getInputStream(), this.console);
        this.processErrorOutput = new ProcessOutputThread(process.getErrorStream(), this.console);
        this.processInput = new ProccesInputThread(process, this.logs);
        this.executorService.execute(this.processOutput);
        this.executorService.execute(processErrorOutput);
        this.executorService.execute(processInput);
        this.executorService.execute(() -> {
            try {
                while (true) {
                    if (this.console.length() > 0) {
                        String data = this.console.toString();
                        logs.append(data);

                        // Set length to 0 to clear string builder - don't create new one because threads holding a reference
                        this.console.setLength(0);
                        this.eventBus.publish(new OpenttdTerminalUpdateEvent(ProcessThread.class, this.uuid, data));
                    }

                    // Quick solution to prevent out of memory if logs get to big
                    if (this.logs.length() > 9999999) {
                        this.logs = new StringBuilder(this.logs.substring(2999999));
                    }

                    Thread.sleep(100);
                }
            } catch (Exception e) {
                throw new ServiceRuntimeException(e);
            }

        });
        process.waitFor();
        System.out.println("FINSHED Proccess");
    }

    public void write(String data) {
        if (data != null && this.processInput != null) {
            this.processInput.write(data);
        }
    }

    public String getLogs() {
        return this.logs.toString();
    }

    public void stop() {
        try {
            if (this.process != null) {
                this.process.destroyForcibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.processErrorOutput.stop();
            this.processInput.stop();
            this.processOutput.stop();
        }
    }

    public BaseProcess toModel() {
        // Only send last 20000 chars to processdata
        String data = this.getLogs();
        if (data.length() > 20000) {
            data = logs.substring(data.length() - 20000);
        }
        return new BaseProcess().setProcessData(data).setProcessId(this.getUuid());
    }
}
