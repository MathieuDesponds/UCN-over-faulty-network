package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OutputLayer extends Layer{
    private ConcurrentLinkedDeque<String> sToWrite;
    private StringBuilder sb;
    private String path;

    //For performance
    private int numberOfTotalMessage;
    private int counterM = 0;
    private long startTime = 0;
    private long finishedTime = 0;

    public OutputLayer(Layer topLayer, Parser parser){
        this.path = parser.output();
        sToWrite = new ConcurrentLinkedDeque<>();
        Layer downLayer = new LCausalBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);
        addThread(new Thread(new OLStringBuilderThread()));

        int nbM = parser.configNbMessage();
        numberOfTotalMessage = nbM * (parser.NUMBER_OF_HOSTS+1);
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        if(!closed) {
            counterM ++;
            sToWrite.addLast("d " + ((BroadcastMessage)m).getBroadcasterID() + " " + m.getSeqNumber());
        }
        if(counterM == numberOfTotalMessage){
            finishedTime = System.currentTimeMillis();
            System.out.println("Time is "+(finishedTime- startTime));
        }
    }

    @Override
    public <BM extends Message> void  sentFromTop(BM m) {
        if(!closed) {
            counterM ++;
            downLayer.sentFromTop(m);
            sToWrite.addLast("b " + m.getSeqNumber());
        }
        if(startTime==0)
            startTime = System.currentTimeMillis();
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

    @Override
    public void close(){
        super.close();
        write();
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
