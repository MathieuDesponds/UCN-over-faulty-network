package cs451.Messages;

import java.io.*;

public abstract class Message implements Serializable {
    protected int seqNumber;

    protected Message(int seqNumber){
        this.seqNumber = seqNumber;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

//    {
//        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(baos)){
//            oos.writeObject(this);
//            return baos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


//    {
//        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
//            ObjectInputStream oi = new ObjectInputStream(bais);) {
//            Message message = (Message) oi.readObject();
//            return message;
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    protected static int fromByteArray(byte[] bytes, int  startPoint) {
        return bytes[startPoint] << 24 | (bytes[startPoint+1] & 0xFF) << 16 | (bytes[startPoint+2] & 0xFF) << 8 | (bytes[startPoint+3] & 0xFF);
    }
}