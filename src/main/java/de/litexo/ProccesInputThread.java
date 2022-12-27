package de.litexo;

import java.util.LinkedList;
import java.util.Queue;

public class ProccesInputThread implements Runnable {

    private final StringBuilder console;
    Process process;


    private Queue<String> data = new LinkedList<>();

    private boolean stopped = false;

    public ProccesInputThread(Process process, StringBuilder console) {
        this.process = process;
        this.console = console;
    }

    @Override
    public void run() {
        try {
            while (!stopped) {
                if (!data.isEmpty()) {
                    String dataValue = data.poll()+"\n";
                    process.getOutputStream().write(dataValue.getBytes());
                    process.getOutputStream().flush();
                    console.append(dataValue+"\r\n");
                }
                Thread.sleep(100);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished: ProccesInputThread");
    }

    public void write(String data) {
        if (data != null) {
            this.data.add(data);
        }
    }

    public void stop(){
        this.stopped=true;
    }
}
