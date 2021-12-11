package cs451.layers;

import cs451.Messages.Message;

import java.util.ArrayList;
import java.util.List;

public abstract class Layer {
    protected Layer topLayer;
    protected Layer downLayer;
    protected boolean closed = false;
    protected List<Thread> myThreads = new ArrayList<>();

    protected void setDownLayer(Layer downLayer) {
        this.downLayer = downLayer;
    }

    protected void setTopLayer(Layer topLayer) {
        this.topLayer = topLayer;
    }

    /**
     * The layer that is bottom me can call this function to give me a message it delivered
     */
    public abstract  <T extends Message> void deliveredFromBottom(T m);

    /**
     * The layer that is upon me can call this function to give me a message it wants to send
     * @param m
     */
    public abstract  <T extends Message> void sentFromTop(T m);

    public void addThread(Thread t){
        t.setDaemon(true); t.start();
        myThreads.add(t);
    }
    public final void close(){
        closed = true;
        for(Thread t : myThreads)
            t.interrupt();
        if(downLayer != null)
            downLayer.close();
    }
}
