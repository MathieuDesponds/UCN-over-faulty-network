package cs451.Parsing;

import cs451.Constants;
import cs451.Host;

import java.util.List;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Host ME;

    public int MY_ID;
    public int NUMBER_OF_HOSTS;

    public Parser(String[] args) {
        this.args = args;
    }

    public void parse() {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if (argsNum != Constants.ARG_LIMIT_CONFIG) {
            help();
        }

        if (!idParser.populate(args[Constants.ID_KEY], args[Constants.ID_VALUE])) {
            help();
        }

        if (!hostsParser.populate(args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE])) {
            help();
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help();
        }

        if (!outputParser.populate(args[Constants.OUTPUT_KEY], args[Constants.OUTPUT_VALUE])) {
            help();
        }

        MY_ID = myId();
        NUMBER_OF_HOSTS = hosts().size();
        ME = getHostWithId(myId());

        if (!configParser.populate(args[Constants.CONFIG_VALUE], NUMBER_OF_HOSTS)) {
            help();
        }
        MY_ID = myId();
        NUMBER_OF_HOSTS = hosts().size();
        ME = getHostWithId(myId());
    }

    private void help() {
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public List<Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public String config() {
        return configParser.getPath();
    }

    public int configNbMessage() {
        return configParser.getNbMessage();
    }

    public Host getHostWithId(int hostId) {
        for(Host h : hosts()){
            if(h.getId()==hostId)
                return h;
        }
        return null;
    }

    public int [][] getCause(){
        return configParser.getCause();
    }

    public Host getME() {
        return ME;
    }
}
