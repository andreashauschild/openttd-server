package de.litexo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProccesInputThreadTest {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @AfterEach
    void afterEach() {
        this.executorService.shutdownNow();
    }

    @DisplayName("Test that the password argument of a password command is redacted in the console echo")
    @Test
    void test_passwordCommandsAreRedacted() {
        assertEquals("server_pw ********", ProccesInputThread.redact("server_pw hunter2"));
        assertEquals("rcon_pw ********", ProccesInputThread.redact("rcon_pw hunter2"));
        assertEquals("company_pw ********", ProccesInputThread.redact("company_pw hunter2"));
        // Everything behind the command is the password, even if it contains blanks
        assertEquals("server_pw ********", ProccesInputThread.redact("server_pw two words"));
        // 'rcon' keeps its command, only the password is a secret
        assertEquals("rcon ******** pause", ProccesInputThread.redact("rcon hunter2 pause"));
        // 'setting' and 'setting_newgame' carry the password as their second argument
        assertEquals("setting server_password ********", ProccesInputThread.redact("setting server_password hunter2"));
        assertEquals("setting network.rcon_password ********", ProccesInputThread.redact("setting network.rcon_password hunter2"));
        assertEquals("setting_newgame server_password ********", ProccesInputThread.redact("setting_newgame server_password hunter2"));
        assertEquals("SETTING Server_Password ********", ProccesInputThread.redact("SETTING Server_Password hunter2"));
    }

    @DisplayName("Test that commands without a password argument are echoed unchanged")
    @Test
    void test_otherCommandsAreNotRedacted() {
        assertEquals("server_info", ProccesInputThread.redact("server_info"));
        assertEquals("echo @@@@_abc_@@@@", ProccesInputThread.redact("echo @@@@_abc_@@@@"));
        assertEquals("setting max_clients 25", ProccesInputThread.redact("setting max_clients 25"));
        // Without a value there is no password to hide, the console answers with the current value
        assertEquals("setting server_password", ProccesInputThread.redact("setting server_password"));
        assertEquals("server_pw", ProccesInputThread.redact("server_pw"));
        assertEquals("", ProccesInputThread.redact(""));
    }

    @DisplayName("Test that the process receives the real command while the console history gets the redacted one")
    @EnabledOnOs(OS.LINUX)
    @Test
    void test_processReceivesTheRealCommand() throws Exception {
        Process process = new ProcessBuilder(List.of("sh", "-c", "cat")).start();
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        ProccesInputThread subject = new ProccesInputThread(process, lines);
        this.executorService.execute(subject);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            subject.write("server_pw hunter2");

            assertEquals("server_pw ********", lines.poll(5, TimeUnit.SECONDS));
            assertEquals("server_pw hunter2", reader.readLine());
        } finally {
            subject.stop();
            process.destroyForcibly();
        }
    }
}
