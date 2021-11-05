package cs451.layers;

import cs451.Message;
import cs451.Parser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutputLayer extends Layer{
    private List<String> output;
    private String path;
    private boolean closed = false;

    public OutputLayer(Layer topLayer, Parser parser){
        this.path = parser.output();
        output = new ArrayList<>();
        Layer downLayer = new FIFOUniformBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);
    }

    @Override
    public <BroadcastMessage extends Message> void deliveredFromBottom(BroadcastMessage m) {
        if(!closed) {
            output.add("d " + m.getBroadcasterID() + " " + m.getSeqNumber());
        }
    }

    @Override
    public <BroadcastMessage extends Message> void  sendFromTop(BroadcastMessage m) {
        if(!closed) {
            downLayer.sendFromTop(m);
            output.add("b " + m.getSeqNumber());
        }
    }

    @Override
    public void close() {
        super.close();
        write();
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
