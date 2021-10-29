package cs451.layers;

import cs451.Message;

public abstract class Layer {
    public abstract void deliveredFromBottom(Message m);
}
