package de.litexo.commands;

public class QuitCommand extends Command {

    public QuitCommand() {
        super("quit");
    }

    @Override
    public boolean check(String logs) {
        if (logs.contains("quit")) {
            return true;
        }
        return false;
    }
}
