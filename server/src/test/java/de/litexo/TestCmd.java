package de.litexo;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Disabled
public class TestCmd {


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    void test() throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine commandline = CommandLine.parse("openttd");
        commandline.addArgument("-D");
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(System.out, System.err, System.in);


        streamHandler.setProcessInputStream(outputStream);


        exec.setStreamHandler(streamHandler);

        this.executorService.execute(() -> {
            try {
                Thread.sleep(3000);
                outputStream.write("clients\n".getBytes());
                outputStream.flush();
                System.out.println("SEND");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        exec.execute(commandline);


    }

    @Test
    void test2() throws Exception {

        ProcessBuilder builder = new ProcessBuilder();
//        builder.command("sh", "/home/andreas/read.sh");
        builder.command("./openttd", "-D");

        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();


        this.executorService.execute(() -> {
            try {

                BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                while (true) {
                    if (inputReader.ready()) {
                        for (String line; (line = inputReader.readLine()) != null; ) {
                            System.out.println(line);
                        }

                    }
                    Thread.sleep(50);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        this.executorService.execute(() -> {
            try {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while (true) {
                    if (errorReader.ready()) {
                        for (String line; (line = errorReader.readLine()) != null; ) {
                            System.out.println(line);
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        this.executorService.execute(() -> {
            try {
                Thread.sleep(5000);
                process.getOutputStream().write("list_cmds\n".getBytes());
                process.getOutputStream().flush();
                System.out.println("SEND");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        int exitCode = process.waitFor();
        assert exitCode == 0;


    }
}
