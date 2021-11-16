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

    //For performance
    private int numberOfTotalMessage;
    private long startTime = 0;
    private long finishedTime = 0;

    public OutputLayer(Layer topLayer, Parser parser){
        this.path = parser.output();
        output = new ArrayList<>();
        Layer downLayer = new FIFOUniformBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        int nbM = parser.configNbMessage();
        numberOfTotalMessage = nbM * (parser.NUMBER_OF_HOSTS+1);
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        if(!closed) {
            output.add("d " + ((BroadcastMessage)m).getBroadcasterID() + " " + m.getSeqNumber());
        }
        if(output.size() == numberOfTotalMessage){
            finishedTime = System.currentTimeMillis();
        }
    }

    @Override
    public <BroadcastMessage extends Message> void  sendFromTop(BroadcastMessage m) {
        if(!closed) {
            downLayer.sendFromTop(m);
            output.add("b " + m.getSeqNumber());
        }
        if(startTime==0)
            startTime = System.currentTimeMillis();
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
            writer.write("Time is "+(finishedTime-startTime)+" ms");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
