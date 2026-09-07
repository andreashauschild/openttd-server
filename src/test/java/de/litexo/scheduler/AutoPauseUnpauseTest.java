package de.litexo.scheduler;

import de.litexo.OpenttdProcess;
import de.litexo.commands.Command;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
class AutoPauseUnpauseTest {


    @Mock
    OpenttdService service;

    @Mock
    OpenttdProcess openttdProcess;

    @Mock
    ServerInfoCommand serverInfoCommand;

    @Mock
    DefaultRepository repository;

    @Mock
    UpdateServerInfo updateServerInfo;

    @InjectMocks
    AutoPauseUnpause subject = new AutoPauseUnpause();

    /**
     * Start of the currently running check of the calling thread, see {@link #test070_tickAndEventCannotInterleave}.
     */
    private final ThreadLocal<Long> checkStart = new ThreadLocal<>();

    @BeforeEach
    void beforeEach() {
        // A dead process is skipped without sending a command, every case here is about a running one
        lenient().when(this.openttdProcess.isAlive()).thenReturn(true);
        // The locks and the re-run flags of the subject are kept per process id
        lenient().when(this.openttdProcess.getId()).thenReturn("111");
    }

    @DisplayName("Test unpause because clients are connected, no matter how many of them are spectators")
    @Test
    void test001() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(5);
        lenient().when(serverInfoCommand.getCurrentSpectators()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(UnpauseCommand.class), anyBoolean());
        verify(this.openttdProcess, never()).executeCommand(any(PauseCommand.class), anyBoolean());

    }

    @DisplayName("Test pause because no client is connected at all")
    @Test
    void test002_noClientConnectedPauses() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(0);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(PauseCommand.class), anyBoolean());
        verify(this.openttdProcess, never()).executeCommand(any(UnpauseCommand.class), anyBoolean());
        Assertions.assertTrue(server.isPaused());
    }

    @DisplayName("Test unpause because client connected to company")
    @Test
    void test010() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(7);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(UnpauseCommand.class), anyBoolean());

    }

    @DisplayName("Test handleTerminalUpdateEvent")
    @Test
    void test020() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.findProcessByThreadUuid("111")).thenReturn(Optional.of(this.openttdProcess));

        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(7);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has started a new company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has joined company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has joined the game"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has left the game"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "closed connection"));

        verify(this.openttdProcess, times(5)).executeCommand(any(UnpauseCommand.class), anyBoolean());

    }

    @DisplayName("Test that the server info is taken from the same 'server_info' result instead of a second command")
    @Test
    void test030_serverInfoAppliedFromSameCommandResult() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(0);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess, times(1)).executeCommand(any(ServerInfoCommand.class), anyBoolean());
        verify(this.updateServerInfo, times(1)).applyServerInfo(eq(server), eq(this.serverInfoCommand));
        Assertions.assertTrue(server.isPaused());
    }

    @DisplayName("Test that a process that is not running any more is not asked for its server info")
    @Test
    void test040_deadProcessIsSkipped() {
        when(this.service.getProcesses()).thenReturn(List.of(this.openttdProcess));
        when(this.openttdProcess.isAlive()).thenReturn(false);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess, never()).executeCommand(any(), anyBoolean());
    }

    @DisplayName("Test that a connected spectator keeps the game running")
    @Test
    void test050_connectedSpectatorKeepsGameRunning() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.getProcesses()).thenReturn(List.of(this.openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(1);
        // The only connected client is a spectator, that used to be treated as an empty server
        lenient().when(serverInfoCommand.getCurrentSpectators()).thenReturn(1);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess, times(1)).executeCommand(any(UnpauseCommand.class), anyBoolean());
        verify(this.openttdProcess, never()).executeCommand(any(PauseCommand.class), anyBoolean());
        Assertions.assertFalse(server.isPaused());
    }

    @DisplayName("Test that events arriving during a running check are coalesced into exactly one re-run")
    @Test
    void test060_eventDuringRunningCheckIsCoalescedIntoOneRerun() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.findProcessByThreadUuid("111")).thenReturn(Optional.of(this.openttdProcess));

        CountDownLatch firstCheckEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstCheck = new CountDownLatch(1);
        AtomicInteger serverInfoCalls = new AtomicInteger();
        when(this.openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenAnswer(a -> {
            if (serverInfoCalls.incrementAndGet() == 1) {
                firstCheckEntered.countDown();
                Assertions.assertTrue(releaseFirstCheck.await(10, TimeUnit.SECONDS));
            }
            return this.serverInfoCommand;
        });
        when(this.openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            return argument;
        });
        when(this.serverInfoCommand.getCurrentClients()).thenReturn(0);
        when(this.serverInfoCommand.isExecuted()).thenReturn(true);

        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            pool.execute(() -> this.subject.handleTerminalUpdateEvent(
                    new OpenttdTerminalUpdateEvent(this, "111", "has joined spectators")));
            Assertions.assertTrue(firstCheckEntered.await(10, TimeUnit.SECONDS));

            // Both events arrive while the first check still holds the lock, they must end up as one single re-run
            this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has joined company"));
            this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has left the game"));

            releaseFirstCheck.countDown();
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            releaseFirstCheck.countDown();
            pool.shutdownNow();
        }

        // The initial check plus exactly one re-run for the whole burst
        verify(this.openttdProcess, times(2)).executeCommand(any(ServerInfoCommand.class), anyBoolean());
    }

    @DisplayName("Test that the scheduled tick and an event driven check never run at the same time")
    @Test
    void test070_tickAndEventCannotInterleave() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.getProcesses()).thenReturn(List.of(this.openttdProcess));
        when(this.service.findProcessByThreadUuid("111")).thenReturn(Optional.of(this.openttdProcess));

        // One entry per executed check, [start of 'server_info', end of 'pause']
        List<long[]> checkIntervals = new CopyOnWriteArrayList<>();
        CountDownLatch firstCheckEntered = new CountDownLatch(1);

        when(this.openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenAnswer(a -> {
            this.checkStart.set(System.nanoTime());
            firstCheckEntered.countDown();
            Thread.sleep(200);
            return this.serverInfoCommand;
        });
        when(this.openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            checkIntervals.add(new long[]{this.checkStart.get(), System.nanoTime()});
            return argument;
        });
        when(this.serverInfoCommand.getCurrentClients()).thenReturn(0);
        when(this.serverInfoCommand.isExecuted()).thenReturn(true);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            pool.execute(() -> this.subject.handleTerminalUpdateEvent(
                    new OpenttdTerminalUpdateEvent(this, "111", "has joined the game")));
            Assertions.assertTrue(firstCheckEntered.await(10, TimeUnit.SECONDS));
            // The tick starts while the event driven check is in the middle of its 'server_info'
            pool.execute(() -> this.subject.checkAutoPauseUnpause());
            pool.shutdown();
            Assertions.assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        Assertions.assertEquals(2, checkIntervals.size(), "Both the event and the tick must have run a check");
        List<long[]> sorted = new ArrayList<>(checkIntervals);
        sorted.sort(Comparator.comparingLong(interval -> interval[0]));
        for (int i = 1; i < sorted.size(); i++) {
            Assertions.assertTrue(sorted.get(i - 1)[1] <= sorted.get(i)[0],
                    "Two pause/unpause checks of the same server overlapped in time");
        }
    }

}
