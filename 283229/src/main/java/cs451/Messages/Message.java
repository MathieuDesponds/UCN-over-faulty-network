package cs451.Messages;

import java.io.*;

public abstract class Message implements Serializable {
    protected int seqNumber;

    protected Message(int seqNumber){
        this.seqNumber = seqNumber;
    }

//    public Message(int srcID, int dstID, int broadcasterID, int seqNumber, MessageType mt, String payload){
//        this(srcID,dstID,broadcasterID,seqNumber,mt,payload, -1);
//    }
//    public Message(int srcID, int dstID, int seqNumber, MessageType mt, String payload){
//        this(srcID,dstID,-1, seqNumber,mt,payload, -1);
//    }
//
//
//
//    public Message(int seqNumber, MessageType mt, String payload) {
//        this(-1,-1,- 1, seqNumber,mt,payload, -1);
//    }
//    public Message(Message m) {
//        this(m.srcID,m.dstID,m.broadcasterID,m.seqNumber,m.mt,m.payload, m.timeSent);
//    }

    //*************************************************************//







    //*************************************************************//

    //*************************************************************//

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public byte[] serializeToBytes(){
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)){
            oos.writeObject(this);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Message deserializeFromBytes(byte[] data){
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream oi = new ObjectInputStream(bais);) {
            Message message = (Message) oi.readObject();
            return message;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}