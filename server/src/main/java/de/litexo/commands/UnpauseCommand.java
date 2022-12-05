package de.litexo.commands;

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
}
