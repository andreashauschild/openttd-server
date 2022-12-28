package de.litexo.commands;

import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;

import java.util.Optional;

public class UnpauseCommand extends Command {

    private final DefaultRepository repository;

    public UnpauseCommand(DefaultRepository repository) {
        super("unpause");
        this.repository = repository;
    }

    @Override
    public boolean check(String logs) {
        return logs.contains("*** Game unpaused (manual)") || logs.contains("Game is already unpaused");
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
