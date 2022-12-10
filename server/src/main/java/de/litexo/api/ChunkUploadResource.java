package de.litexo.api;

import de.litexo.model.external.ServerFileType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.spi.HttpRequest;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@javax.ws.rs.Path("/api/chunk-upload")
@RolesAllowed("login_user")
public class ChunkUploadResource {
    @ConfigProperty(name = "openttd.save.dir")
    String openttdSaveDir;

    @ConfigProperty(name = "openttd.config.dir")
    String openttdConfigDir;


    Path configDir;

    Path saveDir;

    @PostConstruct
    void init() throws IOException {
        this.configDir = initDir(this.openttdConfigDir);
        this.saveDir = initDir(this.openttdSaveDir);
    }

    private Path initDir(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            return Files.createDirectories(Paths.get(path));
        } else {
            return Paths.get(path);
        }
    }

    /**
     * Endpoint for chunked file upload
     *
     * @param fileName name of the file
     * @param offset   offset in bytes of the current chunk
     * @param size     total size of the file that is sent
     * @param chunk    chunk of bytes of the transmitted file
     * @param request  http request object (inject by container)
     * @return response
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response add(@QueryParam("type") ServerFileType type, @QueryParam("fileName") String fileName,
                        @QueryParam("offset") int offset, @QueryParam("fileSize") int size, byte[] chunk,
                        @Context HttpRequest request) throws IOException {
        System.out.println(String.format("name: %s from:%s to:%s size:%s Auth:Header %s"
                , fileName, offset, offset + chunk.length, size, request.getHttpHeaders().getHeaderString(HttpHeaders.AUTHORIZATION)));

        if (appendWrite(type, fileName, size, offset, chunk)) {
            return Response.status(201).build();
        }
        return Response.ok().build();
    }

    private boolean appendWrite(ServerFileType type, String fileName, int size, int offset, byte[] chunk) throws IOException {
        Path upload = null;
        switch (type) {
            case CONFIG -> upload = configDir.resolve(fileName);
            case SAVE_GAME -> upload = saveDir.resolve(fileName);
            default -> throw new ServiceRuntimeException("Unknown file type for upload");

        }

        if (Files.exists(upload) && offset == 0) {
            Files.deleteIfExists(upload);
        }

        if (!Files.exists(upload) || offset == 0) {
            Files.write(upload, new byte[0]);
        }

        Files.write(upload, chunk, StandardOpenOption.APPEND);

        long uploaded = Files.size(upload);
        System.out.println("Size on server: " + uploaded + " Expected: " + size + "Appended: " + chunk.length);

        if (uploaded == size) {
            return true;
        }
        return false;
    }
}
