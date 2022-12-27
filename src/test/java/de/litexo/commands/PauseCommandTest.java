package de.litexo.commands;

import de.litexo.ProcessThread;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PauseCommandTest {

    @Mock
    ProcessThread process;

    @Mock
    DefaultRepository repository;


    PauseCommand subject;

    @BeforeEach
    void beforeEach() {
        subject = new PauseCommand(repository);
        this.subject.marker = "@@@-xxx-asdasd";
    }

    @Test
    void test001() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/pause.txt", StandardCharsets.UTF_8);
        when(this.process.getLogs()).thenReturn(logs);

        this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(this.subject.isExecuted());
    }
}