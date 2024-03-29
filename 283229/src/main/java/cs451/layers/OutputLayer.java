package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

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
    public <BM extends Message> void deliveredFromBottom(BM m) {
        if(!closed) {
            output.add("d " + ((BroadcastMessage)m).getBroadcasterID() + " " + m.getSeqNumber());
        }
    }

    @Override
    public <BM extends Message> void  sentFromTop(BM m) {
        if(!closed) {
            downLayer.sentFromTop(m);
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
