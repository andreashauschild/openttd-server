package de.litexo.commands;

import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;

import javax.enterprise.inject.spi.CDI;
import java.util.Optional;

public class UnpauseCommand extends Command {

    private final DefaultRepository repository;

    public UnpauseCommand(DefaultRepository repository) {
        super("unpause");
        this.repository = repository;
    }

    @Override
    public boolean check(String logs) {
        if(logs.contains("*** Game unpaused (manual)") || logs.contains("Game is already unpaused")){
            return true;
        }
        return false;
    }

    @Override
    public void onSuccess(String openttdServeId) {
        Optional<OpenttdServer> openttdServer = this.repository.getOpenttdServer(openttdServeId);
        if (openttdServer.isPresent()) {
            openttdServer.get().setPaused(false);
            repository.updateServer(openttdServeId, openttdServer.get());
        }
        super.onSuccess(openttdServeId);
    }
}
