package de.litexo.api;


import de.litexo.model.external.ExplorerData;
import de.litexo.model.external.ExplorerDirectory;
import de.litexo.model.external.ExplorerFile;
import de.litexo.model.external.ServerFile;
import de.litexo.model.external.ServerFileType;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import static java.lang.String.valueOf;

@Path("/api/openttd-server")
public class FileExplorerResource {

    @ConfigProperty(name = "openttd.root.dir")
    String openttdRootDir;


    @Inject
    DefaultRepository repository;

    @GET
    @Path("/explorer/data")
    @Operation(operationId = "explorerList")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData getExplorerData() {

        Collection<File> filesAndDirs = FileUtils.listFilesAndDirs(new File(openttdRootDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        ExplorerData explorerData = new ExplorerData().setFileSeperator(File.separator).setRoot(new File(openttdRootDir).getAbsolutePath());
        int id = 0;
        for (File file : filesAndDirs) {
            if (file.isDirectory()) {
                ExplorerDirectory directory = new ExplorerDirectory()
                        .setId(valueOf(++id))
                        .setName(file.getName())
                        .setRelativePath(file.getAbsolutePath().substring(explorerData.getRoot().toString().length()))
                        .setAbsolutePath(file.getAbsolutePath());
                // Fügen Sie Dateien hinzu, die zu diesem Verzeichnis gehören
                File[] filesInDirectory = file.listFiles();
                if (filesInDirectory != null) {
                    for (File f : filesInDirectory) {
                        if (f.isFile()) {
                            ExplorerFile explorerFile = new ExplorerFile()
                                    .setId(valueOf(++id))
                                    .setName(f.getName())
                                    .setAbsolutePath(f.getAbsolutePath())
                                    .setRelativePath(f.getAbsolutePath().substring(explorerData.getRoot().toString().length()))
                                    // Hier könnten Sie baseName und extension setzen
                                    .setBaseName(FilenameUtils.getBaseName(f.getName()))
                                    .setExtension(FilenameUtils.getExtension(f.getName()));
                            directory.getFiles().add(explorerFile);
                        }
                    }
                }
                explorerData.getDirectories().add(directory);
            }
        }
        return explorerData;

    }

    @POST
    @Path("/explorer/directory")
    @Operation(operationId = "createDirectory")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData createDirectory(@QueryParam("dir") String dir) {
        if (dir.startsWith("/") || dir.startsWith("\\")) {
            dir = dir.substring(1);
        }
        java.nio.file.Path dirToCreate = Paths.get(openttdRootDir).resolve(dir).normalize();
        try {
            FileUtils.forceMkdir(check(dirToCreate).toFile());
            return this.getExplorerData();
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to create directory", e);
        }
    }

    @POST
    @Path("/explorer/rename")
    @Operation(operationId = "renameFile")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData renameFile(@QueryParam("file") String file, @QueryParam("newName") String newName) {
        if (file.startsWith("/") || file.startsWith("\\")) {
            file = file.substring(1);
        }
        java.nio.file.Path source = Paths.get(openttdRootDir).resolve(file).normalize();
        ;
        if (!Files.exists(source)) {
            throw new ServiceRuntimeException("Source does not exist: " + file);
        }
        java.nio.file.Path target = check(source.resolveSibling(newName));

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Could not rename file" + file + " to " + newName, e);
        }

        return this.getExplorerData();
    }


    @DELETE
    @Path("/explorer/file")
    @Operation(operationId = "deleteFile")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData deleteFile(@QueryParam("file") String dir) {
        if (dir.startsWith("/") || dir.startsWith("\\")) {
            dir = dir.substring(1);
        }
        java.nio.file.Path file = Paths.get(openttdRootDir).resolve(dir).normalize();
        FileUtils.deleteQuietly(check(file).toFile());
        return this.getExplorerData();
    }

    @GET
    @Path("/explorer/download")
    @Operation(operationId = "downloadFile")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Object downloadFile(@QueryParam("fileName") String fileName, @QueryParam("downloadName") String downloadName) {
        if (fileName.startsWith("/") || fileName.startsWith("\\")) {
            fileName = fileName.substring(1);
        }
        java.nio.file.Path filePath = Paths.get(openttdRootDir).resolve(fileName).normalize();
        ServerFile file = this.repository.serverFile(filePath.toFile().getAbsolutePath(), ServerFileType.OPENTTD_ROOT);
        if (!Files.exists(filePath)) {
            throw new ServiceRuntimeException("File '" + fileName + "' does not exists");
        }
        StreamingOutput fileStream = output -> {
            try {

                byte[] data = Files.readAllBytes(check(filePath));
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found !!");
            }
        };
        if (downloadName == null) {
            downloadName = file.getName();
        }
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename=" + downloadName)
                .build();
    }

    /**
     * Support method that check a path is always in openttd root directory, otherwise exception is thrown     *
     */
    private java.nio.file.Path check(java.nio.file.Path p) {
        if (!p.normalize().toAbsolutePath().toString().startsWith(Paths.get(this.openttdRootDir).toAbsolutePath().toString())) {
            throw new ServiceRuntimeException("Path check failed on path" + p.toAbsolutePath());
        }
        return p;
    }


}
