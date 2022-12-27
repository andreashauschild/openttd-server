package de.litexo;


import de.litexo.api.ServiceRuntimeException;
import de.litexo.events.EventBus;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProcessExecutorService {
    @Inject
    ManagedExecutor executor;

    @Inject
    EventBus eventBus;

    Map<String, ProcessThread> processes = new HashMap<>();

    public ProcessThread start(List<String> command) {
        ProcessThread processThread = new ProcessThread(this.executor, command, eventBus);
        this.executor.execute(processThread);
        return processThread;
    }

    public void write(String processId, String data) {
        if (this.processes.containsKey(processId)) {
            this.processes.get(processId).write(data);
        } else {
            throw new ServiceRuntimeException("Failed to write to process with id: " + processId);
        }
    }
}
