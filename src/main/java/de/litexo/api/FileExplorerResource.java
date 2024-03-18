package de.litexo.api;


import de.litexo.model.external.ExplorerData;
import de.litexo.model.external.ExplorerDirectory;
import de.litexo.model.external.ExplorerFile;
import de.litexo.model.external.ServerFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static java.lang.String.valueOf;

@Path("/api/openttd-server")
public class FileExplorerResource {
    @GET
    @Path("/explorer/data")
    @Operation(operationId = "explorerList")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplorerData getExplorerData() {
        Collection<File> filesAndDirs = FileUtils.listFilesAndDirs(new File("C:\\App\\openttd-13.4-windows-win64"), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        ExplorerData explorerData = new ExplorerData().setFileSeperator(File.separator);
        int id = 0;
        for (File file : filesAndDirs) {
            if (file.isDirectory()) {
                ExplorerDirectory directory = new ExplorerDirectory()
                        .setId(valueOf(++id))
                        .setName(file.getName())
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

}
