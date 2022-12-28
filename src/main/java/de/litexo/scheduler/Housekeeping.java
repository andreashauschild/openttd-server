package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;
import org.apache.commons.io.FileUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.litexo.services.OpenttdService.AUTO_SAVE_INFIX;
import static de.litexo.services.OpenttdService.MANUALLY_SAVE_INFIX;

@ApplicationScoped
public class Housekeeping {

    @Inject
    OpenttdService service;

    @Inject
    DefaultRepository repository;

    @Scheduled(every = "600s")
    void deleteOldAutosaves() {
        InternalOpenttdServerConfig serverConfig = this.service.getOpenttdServerConfig();
        List<ServerFile> openttdSaveGames = this.repository.getOpenttdSaveGames();

        for (OpenttdProcess process : service.getProcesses()) {
            Optional<OpenttdServer> serverOpt = service.getOpenttdServer(process.getId());
            if (serverOpt.isPresent()) {
                OpenttdServer server = serverOpt.get();

                // oldest begins with index 0
                List<ServerFile> autoSaveGames = openttdSaveGames.stream()
                        .filter(f -> f.getName().contains(server.getId()) && f.getName().contains(AUTO_SAVE_INFIX))
                        .sorted(Comparator.comparingLong(ServerFile::getLastModified)).toList();

                // oldest begins with index 0
                List<ServerFile> manSaveGames = openttdSaveGames.stream()
                        .filter(f -> f.getName().contains(server.getId()) && f.getName().contains(MANUALLY_SAVE_INFIX))
                        .sorted(Comparator.comparingLong(ServerFile::getLastModified)).toList();

                if (autoSaveGames.size() > serverConfig.getNumberOfAutoSaveFilesToKeep()) {
                    List<ServerFile> toDelete = autoSaveGames.subList(0,autoSaveGames.size()- serverConfig.getNumberOfAutoSaveFilesToKeep());
                    for (ServerFile f : toDelete) {
                        FileUtils.deleteQuietly(new File(f.getPath()));
                    }
                }

                if (manSaveGames.size() > serverConfig.getNumberOfManuallySaveFilesToKeep()) {
                    List<ServerFile> toDelete = manSaveGames.subList(0,manSaveGames.size()-serverConfig.getNumberOfManuallySaveFilesToKeep());
                    for (ServerFile f : toDelete) {
                        FileUtils.deleteQuietly(new File(f.getPath()));
                    }
                }

            }
        }
    }
}
