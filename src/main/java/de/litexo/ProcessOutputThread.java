package de.litexo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessOutputThread implements Runnable {
    InputStream processInputStream;

    private StringBuilder data;

    private boolean stopped = false;

    public ProcessOutputThread(InputStream processInputStream, StringBuilder data) {
        this.processInputStream = processInputStream;
        this.data = data;
    }

    @Override
    public void run() {
        try {

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(processInputStream));

            while (!this.stopped) {
                for (int ch; (ch = inputReader.read()) != -1; ) {
                    data.append((char) ch);
                }
                Thread.sleep(50);
            }
            System.out.println("Finished: ProcessOutputThread");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
