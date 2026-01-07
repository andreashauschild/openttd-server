package de.litexo.api;


import de.litexo.model.external.ExplorerData;
import de.litexo.model.external.ExplorerDirectory;
import de.litexo.model.external.ExplorerFile;
import de.litexo.model.external.FileOperationRequest;
import de.litexo.model.external.MultiFileDownloadRequest;
import de.litexo.model.external.ServerFile;
import de.litexo.model.external.ServerFileType;
import de.litexo.repository.DefaultRepository;
import de.litexo.security.SecurityUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.valueOf;

@Path("/api/openttd-server")
@RolesAllowed("login_user")
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
                                    .setBaseName(FilenameUtils.getBaseName(f.getName()))
                                    .setExtension(FilenameUtils.getExtension(f.getName()))
                                    .setSize(f.length())
                                    .setLastModified(f.lastModified())
                                    .setIsDirectory(false);
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
                .header("content-disposition", "attachment; filename=\"" + SecurityUtils.sanitizeFilename(downloadName) + "\"")
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

    @POST
    @Path("/explorer/move")
    @Operation(operationId = "moveFile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData moveFile(FileOperationRequest request) {
        String sourcePath = request.getSourcePath();
        String destinationPath = request.getDestinationPath();

        if (sourcePath.startsWith("/") || sourcePath.startsWith("\\")) {
            sourcePath = sourcePath.substring(1);
        }
        if (destinationPath.startsWith("/") || destinationPath.startsWith("\\")) {
            destinationPath = destinationPath.substring(1);
        }

        java.nio.file.Path source = check(Paths.get(openttdRootDir).resolve(sourcePath).normalize());
        java.nio.file.Path destination = check(Paths.get(openttdRootDir).resolve(destinationPath).normalize());

        if (!Files.exists(source)) {
            throw new ServiceRuntimeException("Source does not exist: " + sourcePath);
        }

        try {
            if (Files.isDirectory(source)) {
                FileUtils.moveDirectory(source.toFile(), destination.toFile());
            } else {
                if (Boolean.TRUE.equals(request.getOverwrite())) {
                    Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(source, destination);
                }
            }
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to move " + sourcePath + " to " + destinationPath, e);
        }

        return this.getExplorerData();
    }

    @POST
    @Path("/explorer/copy")
    @Operation(operationId = "copyFile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData copyFile(FileOperationRequest request) {
        String sourcePath = request.getSourcePath();
        String destinationPath = request.getDestinationPath();

        if (sourcePath.startsWith("/") || sourcePath.startsWith("\\")) {
            sourcePath = sourcePath.substring(1);
        }
        if (destinationPath.startsWith("/") || destinationPath.startsWith("\\")) {
            destinationPath = destinationPath.substring(1);
        }

        java.nio.file.Path source = check(Paths.get(openttdRootDir).resolve(sourcePath).normalize());
        java.nio.file.Path destination = check(Paths.get(openttdRootDir).resolve(destinationPath).normalize());

        if (!Files.exists(source)) {
            throw new ServiceRuntimeException("Source does not exist: " + sourcePath);
        }

        try {
            if (Files.isDirectory(source)) {
                FileUtils.copyDirectory(source.toFile(), destination.toFile());
            } else {
                FileUtils.copyFile(source.toFile(), destination.toFile());
            }
        } catch (IOException e) {
            throw new ServiceRuntimeException("Failed to copy " + sourcePath + " to " + destinationPath, e);
        }

        return this.getExplorerData();
    }

    @GET
    @Path("/explorer/download-zip")
    @Operation(operationId = "downloadDirectoryZip")
    @Produces("application/zip")
    public Response downloadDirectoryZip(@QueryParam("dir") String dir) {
        if (dir == null || dir.isEmpty()) {
            dir = "";
        }
        if (dir.startsWith("/") || dir.startsWith("\\")) {
            dir = dir.substring(1);
        }

        java.nio.file.Path dirPath = check(Paths.get(openttdRootDir).resolve(dir).normalize());

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new ServiceRuntimeException("Directory does not exist: " + dir);
        }

        String zipName = dirPath.getFileName().toString();
        if (zipName.isEmpty()) {
            zipName = "download";
        }

        StreamingOutput stream = output -> {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                addToZip(zos, dirPath, dirPath);
            }
        };

        return Response
                .ok(stream, "application/zip")
                .header("content-disposition", "attachment; filename=\"" + SecurityUtils.sanitizeFilename(zipName) + ".zip\"")
                .build();
    }

    @POST
    @Path("/explorer/download-zip")
    @Operation(operationId = "downloadSelectedZip")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/zip")
    public Response downloadSelectedZip(MultiFileDownloadRequest request) {
        String dirPath = request.getDirectoryPath();
        if (dirPath == null) {
            dirPath = "";
        }
        if (dirPath.startsWith("/") || dirPath.startsWith("\\")) {
            dirPath = dirPath.substring(1);
        }

        java.nio.file.Path basePath = check(Paths.get(openttdRootDir).resolve(dirPath).normalize());

        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            throw new ServiceRuntimeException("Directory does not exist: " + dirPath);
        }

        StreamingOutput stream = output -> {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                for (String fileName : request.getFileNames()) {
                    java.nio.file.Path filePath = check(basePath.resolve(fileName).normalize());
                    if (Files.exists(filePath)) {
                        addToZip(zos, basePath, filePath);
                    }
                }
            }
        };

        return Response
                .ok(stream, "application/zip")
                .header("content-disposition", "attachment; filename=\"download.zip\"")
                .build();
    }

    private void addToZip(ZipOutputStream zos, java.nio.file.Path basePath, java.nio.file.Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            String entryName = basePath.relativize(filePath).toString();
            if (!entryName.isEmpty()) {
                zos.putNextEntry(new ZipEntry(entryName + "/"));
                zos.closeEntry();
            }
            try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(filePath)) {
                for (java.nio.file.Path child : stream) {
                    addToZip(zos, basePath, child);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(basePath.relativize(filePath).toString()));
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry();
        }
    }
}
