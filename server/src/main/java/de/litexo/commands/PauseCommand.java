package de.litexo.commands;

import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;

import javax.enterprise.inject.spi.CDI;
import java.util.Optional;

public class PauseCommand extends Command {

    public PauseCommand() {
        super("pause");
    }

    @Override
    public boolean check(String logs) {
        if (logs.contains("*** Game paused (manual)") || logs.contains("Game is already paused")) {
            return true;
        }
        return false;
    }

    @Override
    public void onSuccess(String openttdServeId) {
        DefaultRepository defaultRepository = CDI.current().select(DefaultRepository.class).get();
        Optional<OpenttdServer> openttdServer = defaultRepository.getOpenttdServer(openttdServeId);
        if (openttdServer.isPresent()) {
            openttdServer.get().setPaused(true);
            defaultRepository.updateServer(openttdServeId, openttdServer.get());
        }
        super.onSuccess(openttdServeId);
    }
}
