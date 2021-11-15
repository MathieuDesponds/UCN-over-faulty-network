package cs451.layers;

import cs451.Messages.Message;

public class MessageGrouper extends Layer {
    @Override
    public <T extends Message> void deliveredFromBottom(T m) {

    }

    @Override
    public <T extends Message> void sentFromTop(T m) {

    }
}
