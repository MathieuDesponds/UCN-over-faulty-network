package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OutputLayer extends Layer{
    private List<String> output;
    private ConcurrentLinkedDeque<String> sToWrite;
    private StringBuilder sb;
    private String path;
    private boolean closed = false;


    Thread OLT;

    public OutputLayer(Layer topLayer, Parser parser){
        this.path = parser.output();
        output = new LinkedList<>();
        sToWrite = new ConcurrentLinkedDeque<>();
        Layer downLayer = new FIFOUniformBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        OLT = new Thread(new OLStringBuilderThread());
        OLT.setDaemon(true);
        OLT.start();
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        if(!closed) {
            sToWrite.addLast("d " + ((BroadcastMessage)m).getBroadcasterID() + " " + m.getSeqNumber());
        }
    }

    @Override
    public <BM extends Message> void  sentFromTop(BM m) {
        if(!closed) {
            downLayer.sentFromTop(m);
            sToWrite.addLast("b " + m.getSeqNumber());
        }
    }

    @Override
    public void close() {
        super.close();
        write();
    }

    public void write(){
        String s = sb.toString();
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private class OLStringBuilderThread implements Runnable{
        @Override
        public void run() {
            sb = new StringBuilder();
            while(!closed){
                while(!sToWrite.isEmpty()){
                    sb.append(sToWrite.pollFirst()+"\n");
                }
            }
        }
    }

}
