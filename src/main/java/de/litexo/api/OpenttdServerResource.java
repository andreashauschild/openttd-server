package de.litexo.api;

import de.litexo.OpenttdProcess;
import de.litexo.commands.Command;
import de.litexo.model.external.ExportModel;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.OpenttdServerConfigGet;
import de.litexo.model.external.OpenttdServerConfigUpdate;
import de.litexo.model.external.ServerFile;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.model.mapper.OpenttdServerConfigMapper;
import de.litexo.repository.DefaultRepository;
import de.litexo.security.SecurityUtils;
import de.litexo.services.OpenttdService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@Path("/api/openttd-server")
@RolesAllowed("login_user")
public class OpenttdServerResource {

    @Inject
    OpenttdService openttdService;

    @Inject
    DefaultRepository repository;

    @Inject
    OpenttdServerConfigMapper openttdServerConfigMapper;


    @GET
    @Path("/download/save-game")
    @Operation(operationId = "downloadSaveGame")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Object downloadSaveGame(@QueryParam("fileName") String fileName, @QueryParam("downloadName") String downloadName) {
        Optional<ServerFile> saveGame = repository.getSaveGame(fileName);
        if (saveGame.isEmpty()) {
            throw new ServiceRuntimeException("File '" + fileName + "' does not exists");
        }
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get(saveGame.get().getPath());
                byte[] data = Files.readAllBytes(path);
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found !!");
            }
        };
        if (downloadName == null) {
            downloadName = saveGame.get().getName();
        }
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename=\"" + SecurityUtils.sanitizeFilename(downloadName) + "\"")
                .build();
    }

    @GET
    @Path("/download/openttd-config")
    @Operation(operationId = "downloadOpenttdConfig")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Object downloadOpenttdConfig(@QueryParam("fileName") String fileName) {
        Optional<ServerFile> openttdConfig = repository.getConfig(fileName);
        if (openttdConfig.isEmpty()) {
            throw new ServiceRuntimeException("File '" + fileName + "' does not exists");
        }
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get(openttdConfig.get().getPath());
                byte[] data = Files.readAllBytes(path);
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found !!");
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename=\"" + SecurityUtils.sanitizeFilename(openttdConfig.get().getName()) + "\"")
                .build();
    }


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
    @Produces(MediaType.APPLICATION_JSON)
    public OpenttdServer stopServer(@PathParam("id") String id) {
        Optional<OpenttdServer> server = this.openttdService.stop(id);
        if (server.isPresent()) {
            return server.get();
        } else {
            throw new ServiceRuntimeException("Can't stop server with id '" + id + "'. Server does not exists");
        }
    }

    @POST
    @Path("/server/{id}/pause-unpause")
    @Operation(operationId = "pauseUnpause")
    @Produces(MediaType.APPLICATION_JSON)
    public OpenttdServer pauseUnpauseServer(@PathParam("id") String id) {
        Optional<OpenttdServer> server = this.openttdService.pauseUnpauseServer(id);
        if (server.isPresent()) {
            return server.get();
        } else {
            throw new ServiceRuntimeException("Can't pause server with id '" + id + "'. Server does not exists");
        }
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


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/exportModel")
    public ExportModel exportModel() {
        return new ExportModel();
    }
}
