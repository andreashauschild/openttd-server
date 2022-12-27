package de.litexo.scheduler;

import de.litexo.OpenttdProcess;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.litexo.services.OpenttdService.AUTO_SAVE_INFIX;
import static de.litexo.services.OpenttdService.MANUALLY_SAVE_INFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HousekeepingTest {

    @Mock
    OpenttdService service;

    @Mock
    DefaultRepository repository;

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File saveGameDir;

    @InjectMocks
    Housekeeping subject;

    @DisplayName("Test delete save game Happy Flow")
    @Test
    void test_0001() throws Exception {
        String id = "id1234567";
        OpenttdServer server = new OpenttdServer().setId(id);
        OpenttdProcess process = new OpenttdProcess().setId(id);

        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setNumberOfAutoSaveFilesToKeep(3);
        config.setNumberOfManuallySaveFilesToKeep(5);
        when(this.service.getOpenttdServerConfig()).thenReturn(config);
        when(this.service.getProcesses()).thenReturn(List.of(process));
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));

        // Create auto savegames
        List<ServerFile> autSaves = createSaveGamesDummy(10, AUTO_SAVE_INFIX, server.getId());
        List<ServerFile> manSaves = createSaveGamesDummy(10, MANUALLY_SAVE_INFIX, server.getId());

        when(this.repository.getOpenttdSaveGames()).thenReturn(ListUtils.union(autSaves,manSaves));

        // run - expect 3 auto save files and 5 manuel save file are left
        this.subject.deleteOldAutosaves();

        List<File> autoSavesCleanup = getFiles(AUTO_SAVE_INFIX);
        List<File> manSavesCleanup = getFiles(MANUALLY_SAVE_INFIX);

        assertEquals(3,autoSavesCleanup.size());
        assertTrue(autoSavesCleanup.get(0).getName().startsWith("7_"));
        assertTrue(autoSavesCleanup.get(1).getName().startsWith("8_"));
        assertTrue(autoSavesCleanup.get(2).getName().startsWith("9_"));

        assertEquals(5,manSavesCleanup.size());
        assertTrue(manSavesCleanup.get(0).getName().startsWith("5_"));
        assertTrue(manSavesCleanup.get(1).getName().startsWith("6_"));
        assertTrue(manSavesCleanup.get(2).getName().startsWith("7_"));
        assertTrue(manSavesCleanup.get(3).getName().startsWith("8_"));
        assertTrue(manSavesCleanup.get(4).getName().startsWith("9_"));


    }

    @DisplayName("Test delete save game. Nothing is delete because thresholds are not exceeded")
    @Test
    void test_0010() throws Exception {
        String id = "id1234567";
        OpenttdServer server = new OpenttdServer().setId(id);
        OpenttdProcess process = new OpenttdProcess().setId(id);

        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setNumberOfAutoSaveFilesToKeep(20);
        config.setNumberOfManuallySaveFilesToKeep(20);
        when(this.service.getOpenttdServerConfig()).thenReturn(config);
        when(this.service.getProcesses()).thenReturn(List.of(process));
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));

        // Create auto savegames
        List<ServerFile> autSaves = createSaveGamesDummy(5, AUTO_SAVE_INFIX, server.getId());
        List<ServerFile> manSaves = createSaveGamesDummy(5, MANUALLY_SAVE_INFIX, server.getId());

        when(this.repository.getOpenttdSaveGames()).thenReturn(ListUtils.union(autSaves,manSaves));

        // run - expect 3 auto save files and 5 manuel save file are left
        this.subject.deleteOldAutosaves();

        List<File> autoSavesCleanup = getFiles(AUTO_SAVE_INFIX);
        List<File> manSavesCleanup = getFiles(MANUALLY_SAVE_INFIX);

        assertEquals(5,autoSavesCleanup.size());
        assertTrue(autoSavesCleanup.get(0).getName().startsWith("0_"));
        assertTrue(autoSavesCleanup.get(1).getName().startsWith("1_"));
        assertTrue(autoSavesCleanup.get(2).getName().startsWith("2_"));
        assertTrue(autoSavesCleanup.get(3).getName().startsWith("3_"));
        assertTrue(autoSavesCleanup.get(4).getName().startsWith("4_"));

        assertEquals(5,manSavesCleanup.size());
        assertTrue(manSavesCleanup.get(0).getName().startsWith("0_"));
        assertTrue(manSavesCleanup.get(1).getName().startsWith("1_"));
        assertTrue(manSavesCleanup.get(2).getName().startsWith("2_"));
        assertTrue(manSavesCleanup.get(3).getName().startsWith("3_"));
        assertTrue(manSavesCleanup.get(4).getName().startsWith("4_"));


    }

    private List<ServerFile> createSaveGamesDummy(int cnt, String infix, String id) throws Exception {
        List<ServerFile> files = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            Path file = saveGameDir.toPath().resolve(i + "_" + id + infix + System.currentTimeMillis() + ".sav");
            Thread.sleep(5);
            Files.writeString(file, "");
            files.add(new ServerFile().setName(file.toFile().getName()).setPath(file.toFile().getAbsolutePath()));
        }
        return files;
    }

    private List<File> getFiles(String infix) {
        List<File> files = new ArrayList<>(FileUtils.listFiles(saveGameDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
        return files.stream().filter(f -> f.getName().contains(infix))
                .sorted(Comparator.comparingLong(File::lastModified)).collect(Collectors.toList());
    }


}
