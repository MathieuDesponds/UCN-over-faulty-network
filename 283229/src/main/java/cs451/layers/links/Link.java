package cs451.layers.links;

import cs451.Message;
import cs451.layers.Layer;

import java.net.SocketTimeoutException;

public abstract class Link extends Layer {
    public abstract Message deliver() throws SocketTimeoutException;
    public abstract void send(Message m);
    public abstract void close();
}
