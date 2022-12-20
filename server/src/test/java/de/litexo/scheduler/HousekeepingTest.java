package de.litexo.scheduler;

import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

@ExtendWith(MockitoExtension.class)
class HousekeepingTest {

    @Mock
    OpenttdService service;

    @Mock
    DefaultRepository repository;

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File saveGameDir;

}
