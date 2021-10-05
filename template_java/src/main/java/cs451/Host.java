package cs451;

import cs451.links.FairLossLink;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Host {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;
    private FairLossLink fll;
    private List<String> output;

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        fll = new FairLossLink(ip, port);
        output = new ArrayList<>();
        return true;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void send(Message m){
        System.out.println("Host "+this.id +" sending a message to "+ m.getDstPort() );
        fll.send(m);
        output.add("b "+m.getSeqNumber());
    }

    public void receive(){
        Message m = fll.deliver();
        System.out.println("Host "+this.id +" received message from "+m.getDstPort()+" "+m.getPayload());
        output.add("d "+m.getSrcPort()+" "+m.getSeqNumber());
    }

    public void close(){
        writeOutput();
        fll.close();
    }
    private void writeOutput(){
        try {
            FileWriter writer = new FileWriter("C:/Users/mathi/Documents/EPFL Master/DA/CS451-2021-project/example/output/"+this.id+".output");
            for(String s : output){
                writer.write(s+"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
