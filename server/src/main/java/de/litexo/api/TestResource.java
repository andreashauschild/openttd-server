package de.litexo.api;

import de.litexo.OpenttdProcess;
import de.litexo.ProcessExecutorService;
import de.litexo.model.ExportModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Path("/api/test")
@RolesAllowed("login_user")
public class TestResource {

    @ConfigProperty(name = "start-server.command")
    String startServerCommand;

    List<OpenttdProcess> server = new ArrayList<>();

    @Inject
    ProcessExecutorService processExecutorService;

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "Hello RESTEasy";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello2")
    public String hello2() {

        this.processExecutorService.start(List.of("cmd.exe", "/C", "C:\\Development\\Git\\openttd-server\\loop.bat"));
        return "mUUUK";
    }


    private void run() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
//        builder.command("sh", "/home/andreas/read.sh");
        builder.command("/usr/bin/bash", "-c", "/openttd.sh");

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
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/exportModel")
    public ExportModel exportModel() {

        return new ExportModel();
    }

}
