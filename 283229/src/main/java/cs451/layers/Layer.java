package cs451.layers;

import cs451.Message;

public abstract class Layer {
    protected Layer topLayer;
    protected Layer downLayer;

    protected void setDownLayer(Layer downLayer) {
        this.downLayer = downLayer;
    }

    protected void setTopLayer(Layer topLayer) {
        this.topLayer = topLayer;
    }

    /**
     * The layer that is bottom me can call this function to give me a message it delivered
     */
    public abstract void deliveredFromBottom(Message m);

    /**
     * The layer that is upon me can call this function to give me a message it wants to send
     * @param m
     */
    public abstract void sendFromTop(Message m);
    public abstract void close();
}
