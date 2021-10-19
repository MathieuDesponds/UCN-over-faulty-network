package cs451.links;

import cs451.Message;

import java.net.SocketTimeoutException;

public abstract class Link {
    public abstract Message deliver() throws SocketTimeoutException;
    //public abstract void send(Message m);
    public abstract void close();
}
