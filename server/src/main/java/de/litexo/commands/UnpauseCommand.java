package de.litexo.commands;

import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;

import javax.enterprise.inject.spi.CDI;
import java.util.Optional;

public class UnpauseCommand extends Command {

    public UnpauseCommand() {
        super("unpause");
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
        DefaultRepository defaultRepository = CDI.current().select(DefaultRepository.class).get();
        Optional<OpenttdServer> openttdServer = defaultRepository.getOpenttdServer(openttdServeId);
        if (openttdServer.isPresent()) {
            openttdServer.get().setPaused(false);
            defaultRepository.updateServer(openttdServeId, openttdServer.get());
        }
        super.onSuccess(openttdServeId);
    }
}
