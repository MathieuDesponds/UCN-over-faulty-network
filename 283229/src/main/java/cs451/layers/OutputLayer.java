package cs451.layers;

import cs451.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutputLayer extends Layer{
    private List<String> output;
    private String path;

    public OutputLayer(Layer topLayer, String ip, int port, String path){
        this.path = path;
        output = new ArrayList<>();
        Layer downLayer = new FairLossLink(this, ip, port);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);
    }

    @Override
    public void deliveredFromBottom(Message m) {
        output.add("d "+m.getSndID()+" "+m.getSeqNumber());
    }

    @Override
    public void sendFromTop(Message m) {
        downLayer.sendFromTop(m);
        output.add("b "+m.getSeqNumber());
    }

    @Override
    public void close() {
        write();
        downLayer.close();

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
