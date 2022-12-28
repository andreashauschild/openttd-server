package de.litexo.commands;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerInfoCommand extends Command {

    @Getter
    private String inviteCode;
    @Getter
    private int currentClients;
    @Getter
    private int maxClients;
    @Getter
    private int currentCompanies;
    @Getter
    private int maxCompanies;
    @Getter
    private int currentSpectators;

    public ServerInfoCommand() {
        super("server_info");
    }

    @Override
    public boolean check(String logs) {
        List<String> lines = new ArrayList<>(Arrays.asList(logs.split("\\R")));
        int matched = 0;
        for (String line : lines) {
            if (line.contains("Invite code:")) {
                String[] split = line.split("Invite code:");
                if (split.length > 1) {
                    this.inviteCode = split[1].trim();
                }
                matched++;
            }
            if (line.contains("Current/maximum clients:")) {
                String[] split = line.split("Current/maximum clients:");
                if (split.length > 1) {
                    this.currentClients = Integer.parseInt(split[1].split("/")[0].trim());
                    this.maxClients = Integer.parseInt(split[1].split("/")[1].trim());
                }
                matched++;
            }
            if (line.contains("Current/maximum companies:")) {
                String[] split = line.split("Current/maximum companies:");
                if (split.length > 1) {
                    this.currentCompanies = Integer.parseInt(split[1].split("/")[0].trim());
                    this.maxCompanies = Integer.parseInt(split[1].split("/")[1].trim());
                }
                matched++;
            }
            if (line.contains("Current spectators:")) {
                String[] split = line.split("Current spectators:");
                if (split.length > 1) {
                    this.currentSpectators = Integer.parseInt(split[1].trim());
                }
                matched++;
            }

        }
        return matched == 4;
    }


}
