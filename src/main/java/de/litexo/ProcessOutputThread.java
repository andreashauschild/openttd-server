package de.litexo;

import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * Reads one stream of the process line by line and hands every line over to the shared queue of the {@link ProcessThread}.
 * There is no stop flag: the reader ends on EOF, which happens as soon as the process is gone.
 */
public class ProcessOutputThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(ProcessOutputThread.class);

    private final InputStream processInputStream;

    private final BlockingQueue<String> lines;

    public ProcessOutputThread(InputStream processInputStream, BlockingQueue<String> lines) {
        this.processInputStream = processInputStream;
        this.lines = lines;
    }

    @Override
    public void run() {
        // UTF-8 is mandatory: OpenTTD prefixes its console messages with a left-to-right mark (U+200E)
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(this.processInputStream, StandardCharsets.UTF_8))) {
            for (String line; (line = inputReader.readLine()) != null; ) {
                this.lines.put(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.warn("Failed to read a process stream", e);
        }
    }
}
