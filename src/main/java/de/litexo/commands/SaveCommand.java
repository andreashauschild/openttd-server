package de.litexo.commands;

import lombok.Getter;

/**
 * Saves the map of a running server. OpenTTD appends '.sav' to the given path and reports the result on the console,
 * so this command is the only reliable way to find out whether a save game was really written.
 * <p>
 * It is intentionally not part of the {@link Command} {@code @JsonSubTypes} list, because it is not exposed over REST.
 */
public class SaveCommand extends Command {

    private static final String SAVE_FAILED_LINE = "Saving map failed.";
    private static final long DEFAULT_SAVE_TIMEOUT_MS = 60_000;
    private static final long SAVE_POLL_INTERVAL_MS = 250;

    private final String expectedPath;

    @Getter
    private String savedPath;

    public SaveCommand(String absolutePathWithoutExtension) {
        this(absolutePathWithoutExtension, DEFAULT_SAVE_TIMEOUT_MS);
    }

    public SaveCommand(String absolutePathWithoutExtension, long timeoutMs) {
        super(String.format("save \"%s\"", absolutePathWithoutExtension));
        this.expectedPath = absolutePathWithoutExtension + ".sav";
        this.timeoutMs = timeoutMs;
        this.pollIntervalMs = SAVE_POLL_INTERVAL_MS;
    }

    @Override
    public boolean check(String logs) {
        String successLine = "Map successfully saved to '" + this.expectedPath + "'.";
        for (String line : logs.split("\\R")) {
            // A success line of another save game (for example a parallel autosave) must not be accepted
            if (line.contains(successLine)) {
                this.savedPath = this.expectedPath;
                return true;
            }
            if (line.trim().equals(SAVE_FAILED_LINE)) {
                fail("Saving map failed");
                return false;
            }
        }
        return false;
    }
}
