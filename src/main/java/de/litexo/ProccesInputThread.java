package de.litexo;

import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Writes commands to the console of the process. Every written command is put on the same queue as the process output,
 * so that the console echo keeps its position relative to the answer of the process.
 */
public class ProccesInputThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(ProccesInputThread.class);

    private static final long POLL_INTERVAL_MS = 100;

    /**
     * Minimum gap between two console writes. The dedicated console of OpenTTD processes only the first line of a
     * single read and keeps the rest of the buffer until new input arrives, so two commands written back to back leave
     * the second one unanswered until something else is typed (verified on OpenTTD 15.3: 'echo x' followed
     * immediately by 'server_info' printed the marker but nothing else for 25 seconds). 50 ms was already enough in
     * that test, 100 ms leaves some margin.
     */
    static final long MIN_WRITE_GAP_MS = 100;

    static final String REDACTED = "********";

    /**
     * Commands whose first argument is a password.
     */
    private static final List<String> PASSWORD_COMMANDS = List.of("server_pw", "rcon_pw", "company_pw");

    /**
     * Commands that take a setting name and its value, so the password is the second argument.
     */
    private static final List<String> SETTING_COMMANDS = List.of("setting", "setting_newgame");

    private final Process process;

    private final BlockingQueue<String> lines;

    private final BlockingQueue<String> data = new LinkedBlockingQueue<>();

    private volatile boolean stopped = false;

    public ProccesInputThread(Process process, BlockingQueue<String> lines) {
        this.process = process;
        this.lines = lines;
    }

    @Override
    public void run() {
        OutputStream processOutputStream = this.process.getOutputStream();
        try {
            while (!this.stopped) {
                String command = this.data.poll(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                if (command == null) {
                    continue;
                }
                processOutputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
                processOutputStream.flush();
                this.lines.put(redact(command));
                Thread.sleep(MIN_WRITE_GAP_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOG.debugf(e, "Failed to write to the console of the process, it is probably already gone");
        }
    }

    /**
     * Replaces password arguments with {@value #REDACTED}, so that a password typed into the terminal does not end up
     * in the console history, in 'GET /processes', in the websocket broadcast and in a process dump. The command that
     * is written to the process itself is never touched.
     * <p>
     * OpenTTD confirms most setting changes on its console ("'server_password' changed to: ..."). That line is output
     * of the process, not an echo of the input, so it cannot be redacted here.
     *
     * @param command the command as it was typed
     * @return the command as it should appear in the console history
     */
    static String redact(String command) {
        String[] token = command.trim().split("\\s+");
        if (token.length < 2) {
            return command;
        }
        String name = token[0].toLowerCase();
        if (PASSWORD_COMMANDS.contains(name)) {
            // 'server_pw <password>', everything behind the command is the password
            return token[0] + " " + REDACTED;
        }
        if ("rcon".equals(name)) {
            // 'rcon <password> <command>', only the password is secret, the command is worth keeping
            return join(token, 1);
        }
        if (SETTING_COMMANDS.contains(name) && token.length > 2 && token[1].toLowerCase().contains("password")) {
            // 'setting server_password <password>', also 'setting network.rcon_password <password>'
            return join(token, 2);
        }
        return command;
    }

    /**
     * @param token          the command split into its tokens
     * @param redactedIndex  index of the token that holds the password
     * @return the command with that one token replaced
     */
    private static String join(String[] token, int redactedIndex) {
        StringBuilder redacted = new StringBuilder();
        for (int i = 0; i < token.length; i++) {
            if (i > 0) {
                redacted.append(" ");
            }
            redacted.append(i == redactedIndex ? REDACTED : token[i]);
        }
        return redacted.toString();
    }

    public void write(String data) {
        if (data != null) {
            this.data.add(data);
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
