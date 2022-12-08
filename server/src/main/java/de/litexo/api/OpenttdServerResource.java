package de.litexo.api;

import de.litexo.OpenttdProcess;
import de.litexo.commands.Command;
import de.litexo.model.OpenttdServer;
import de.litexo.model.OpenttdServerConfig;
import de.litexo.model.ServerFile;
import de.litexo.services.OpenttdService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Path("/api/openttd-server")
@RolesAllowed("login_user")
public class OpenttdServerResource {

    @Inject
    OpenttdService openttdService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/processes")
    @Operation(operationId = "processes")
    public List<OpenttdProcess> getProcesses() {
        return this.openttdService.getProcesses();
    }

    @POST
    @Path("/start-server")
    @Operation(operationId = "start")
    public OpenttdProcess startServer(
            @QueryParam("name") String name,
            @QueryParam("port") Integer port,
            @QueryParam("savegame") String savegame,
            @QueryParam("config") String config
    ) {
        return this.openttdService.start(name, port, savegame, config);
    }

    @POST
    @Path("/start-server2")
    @Operation(operationId = "startServer")
    public OpenttdServer startServer(
            @QueryParam("name") String name,
            @QueryParam("savegame") String savegame
    ) {
        return this.openttdService.startServer(name, savegame);
    }

    @POST
    @Path("/stop-server")
    @Operation(operationId = "stop")
    public void stopServer(@QueryParam("name") String name) {
        this.openttdService.stop(name);
    }

    @POST
    @Path("/save")
    @Operation(operationId = "save")
    public void saveServer(@QueryParam("name") String name) {
        this.openttdService.saveGame(name);
    }

    @POST
    @Path("/dump")
    @Operation(operationId = "dump")
    public void dumpProcessData(@QueryParam("name") String name, @QueryParam("dir") String dir) {
        this.openttdService.dumpProcessData(name, dir);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/send-terminal-command")
    @Operation(operationId = "send-terminal-command")
    public void sendTerminalCommand(@QueryParam("name") String name, String cmd) {
        this.openttdService.sendTerminalCommand(name, cmd);
    }

    @POST
    @Path("/server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "addServer")
    public OpenttdServer addServer(OpenttdServer server) {
        return this.openttdService.addServer(server);
    }

    @PUT
    @Path("/server")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateServer")
    public OpenttdServer updateServer(OpenttdServer server) {
        return this.openttdService.updateServer(server);
    }

    @DELETE
    @Path("/server")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteServer")
    public void deleteServer(@QueryParam("name") String name) {
        this.openttdService.deleteServer(name);
    }

    @GET
    @Path("/server")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getServer")
    public OpenttdServer getServer(@QueryParam("name") String name) {

        Optional<OpenttdServer> openttdServer = this.openttdService.getOpenttdServer(name);
        if (openttdServer.isPresent()) {
            return openttdServer.get();
        }
        return null;
    }

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getOpenttdServerConfig")
    public OpenttdServerConfig getOpenttdServerConfig() {

        return this.openttdService.getOpenttdServerConfig();

    }

    @GET
    @Path("/files/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getServerFiles")
    public List<ServerFile> getServerFiles() {
        return this.openttdService.getServerFiles();

    }

    @POST
    @Path("/command")
    @Operation(operationId = "executeCommand")
    public Command executeCommand(@QueryParam("name") String name, Command command
    ) {
        return this.openttdService.execCommand(name, command);
    }

}
