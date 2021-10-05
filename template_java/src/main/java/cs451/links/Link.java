package cs451.links;

import cs451.Message;

public abstract class Link {
    public abstract Message deliver();
    public abstract void send(Message m);
}
