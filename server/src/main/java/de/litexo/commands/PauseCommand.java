package de.litexo.commands;

public class PauseCommand extends Command {

    public PauseCommand() {
        super("pause");
    }

    @Override
    public boolean check(String logs) {
        if(logs.contains("*** Game paused (manual)") || logs.contains("Game is already paused")){
            return true;
        }
        return false;
    }
}
