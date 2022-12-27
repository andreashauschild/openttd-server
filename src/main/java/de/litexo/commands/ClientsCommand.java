package de.litexo.commands;

import de.litexo.commands.model.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientsCommand extends Command {

    @Getter
    private List<Client> clients = new ArrayList<>();

    public ClientsCommand() {
        super("clients");
    }

    @Override
    public boolean check(String logs) {
        List<String> lines = new ArrayList<>(Arrays.asList(logs.split("\\R")));
        List<Client> found = new ArrayList<>();
        for(String line : lines){
            if(line.contains("Client #")){
                found.add(parseClient(line));
            }
        }

        // Read until no changes
        if(found.size()==0 || found.size()>this.clients.size()){
            this.clients = found;
            return false;
        }else{
            this.clients = found;
            return true;
        }
    }

    private Client parseClient(String line){
        Client client = new Client();
        String name = line.substring(line.indexOf("'")+1,line.lastIndexOf("'"));
        String withoutName = line.substring(0,line.indexOf("name")-1)+line.substring(line.lastIndexOf("'")+1);
        String regex = "(?ims)^Client\s+#([0-9]+)\s+company:\s+([0-9]+)\s+IP:\s+([a-zA-z0-9.:]+)[^\s]*$";
                Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(withoutName);

        if (m.find()){
            client.setName(name);
            client.setIndex(m.group(1));
            client.setCompany(m.group(2));
            client.setIp(m.group(3));
       }
        System.out.println(client);
        return client;
    }
}
