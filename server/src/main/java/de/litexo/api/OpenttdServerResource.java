package de.litexo.api;

import de.litexo.OpenttdProcess;
import de.litexo.commands.Command;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.OpenttdServerConfigGet;
import de.litexo.model.external.OpenttdServerConfigUpdate;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.model.external.ServerFile;
import de.litexo.model.mapper.OpenttdServerConfigMapper;
import de.litexo.model.mapper.OpenttdServerMapper;
import de.litexo.services.OpenttdService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;


@Path("/api/openttd-server")
@RolesAllowed("login_user")
public class OpenttdServerResource {

    @Inject
    OpenttdService openttdService;

    @Inject
    OpenttdServerConfigMapper openttdServerConfigMapper;




    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/processes")
    @Operation(operationId = "processes")
    public List<OpenttdProcess> getProcesses() {
        return this.openttdService.getProcesses();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/server/{id}/terminal/ui-open")
    @Operation(operationId = "terminalOpenInUi")
    public void setTerminalOpenInUi(@PathParam("id") String id) {
        this.openttdService.setTerminalOpenInUi(id);
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
    @Path("/server/{id}/start")
    @Operation(operationId = "startServer")
    public OpenttdServer startServer(
            @PathParam("id") String id
    ) {
        return this.openttdService.startServer(id);
    }

    @POST
    @Path("/server/{id}/stop")
    @Operation(operationId = "stop")
    public void stopServer(@PathParam("id") String id) {
        this.openttdService.stop(id);
    }

    @POST
    @Path("/server/{id}/save")
    @Operation(operationId = "save")
    public void saveServer(@PathParam("id") String id) {
        this.openttdService.saveGame(id);
    }

    @POST
    @Path("/server/{id}/dump-process-data")
    @Operation(operationId = "dumpProcessData")
    public void dumpProcessData(@PathParam("id") String id, @QueryParam("dir") String dir) {
        this.openttdService.dumpProcessData(id, dir);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/server/{id}/send-terminal-command")
    @Operation(operationId = "send-terminal-command")
    public void sendTerminalCommand(@PathParam("id") String id, String cmd) {
        this.openttdService.sendTerminalCommand(id, cmd);
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
    @Path("/server/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateServer")
    public OpenttdServer updateServer(@PathParam("id") String id, OpenttdServer server) {
        return this.openttdService.updateServer(id, server);
    }

    @DELETE
    @Path("/server/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteServer")
    public void deleteServer(@PathParam("id") String id) {
        this.openttdService.deleteServer(id);
    }

    @GET
    @Path("/server/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getServer")
    public OpenttdServer getServer(@PathParam("id") String id) {
        Optional<OpenttdServer> openttdServer = this.openttdService.getOpenttdServer(id);
        if (openttdServer.isPresent()) {
            return openttdServer.get();
        }
        return null;
    }

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getOpenttdServerConfig")
    public OpenttdServerConfigGet getOpenttdServerConfig() {
        return this.openttdServerConfigMapper.toExternal(this.openttdService.getOpenttdServerConfig());
    }

    @PATCH
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateOpenttdServerConfig")
    public OpenttdServerConfigGet updateOpenttdServerConfig(OpenttdServerConfigUpdate update) {
        InternalOpenttdServerConfig openttdServerConfig = this.openttdService.getOpenttdServerConfig();
        this.openttdServerConfigMapper.patch(update, openttdServerConfig);
        return this.openttdServerConfigMapper.toExternal(this.openttdService.save(openttdServerConfig));

    }

    @GET
    @Path("/files/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getServerFiles")
    public List<ServerFile> getServerFiles() {
        return this.openttdService.getServerFiles();

    }

    @POST
    @Path("/server/{id}/command")
    @Operation(operationId = "executeCommand")
    public Command executeCommand(@PathParam("id") String id, Command command
    ) {
        return this.openttdService.execCommand(id, command);
    }

}
