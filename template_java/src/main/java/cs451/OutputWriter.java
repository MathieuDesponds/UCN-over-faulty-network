package cs451;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutputWriter {
    private List<String> output;
    private String path;
    //public enum Action {BROADCAST, DELIVER}

    public OutputWriter(String path){
        this.path = path;
        output = new ArrayList<>();
    }

    public void addBroadcast(Message m){
        output.add("b "+m.getSeqNumber());
    }
    public void addReceive(Message m){
        output.add("d "+m.getSrcPort()+" "+m.getSeqNumber());
    }

    public void write(){
        try {
            FileWriter writer = new FileWriter(path);
            for(String s : output){
                writer.write(s+"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
