package cs451.layers;

import cs451.Message;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FairLossLink extends Layer {
    private final int MAX_SIZE_PACKET = 300; // it is between 217 and and 1 more for the sequence number with 1 more digit
    private DatagramSocket socket;
    private String ip;
    private int port;

    //Threads
    private ConcurrentLinkedDeque<Message> mToSend;
    Thread flST;
    Thread flRT;

    public FairLossLink(Layer topLayer, String ip, int port){
        //Layers
        super.setTopLayer(topLayer);
        super.setDownLayer(null);

        //Socket stuff
        this.ip = ip;
        this.port = port;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            socket = new DatagramSocket(port, inetAddress);
        }
        catch (UnknownHostException | SocketException e){
            System.err.println(e.getStackTrace());
        }

        //Threads
        mToSend = new ConcurrentLinkedDeque<>();

        flST = new Thread(new FLSendingThread());
        flRT = new Thread(new FLReceivingThread());
        flRT.setDaemon(true); flST.setDaemon(true);
        flST.start();
        flRT.start();
    }


    public void setTimeOut(int timeoutInterval) {
        try {
            socket.setSoTimeout(timeoutInterval);
        } catch (SocketException e) {

        }
    }

    @Override
    public void deliveredFromBottom(Message m) {
        System.err.print("Should not be called because it it the layer furthest down");
    }

    @Override
    public void sendFromTop(Message m) {
        mToSend.addLast(m);
    }

    @Override
    public void close() {
        flRT.interrupt();
        flST.interrupt();
        socket.close();
    }

    /**
     * Thread that
     *      See if it can send a message
     *      If it can, serialize the message
     *      and send it
     */
    private class FLSendingThread implements Runnable {
        @Override
        public void run() {
            while(true){
                if(!mToSend.isEmpty()) {
                    Message m = mToSend.pollFirst();
                    byte[] result = m.serializeToBytes();
                    try {
                        DatagramPacket packet = new DatagramPacket(result, result.length,
                                InetAddress.getByName(m.getDstIP()), m.getDstPort());
                        socket.send(packet);
                        System.out.println("send pkt "+m.getSeqNumber()+" "+m.getSrcID());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Thread that
     *      Listen for a packet
     *      Deserialize the packet
     *      Send it to its upper layer
     */
    private class FLReceivingThread implements Runnable {
        byte [] buf;
        DatagramPacket packet;

        FLReceivingThread(){
            buf = new byte [MAX_SIZE_PACKET];
            packet = new DatagramPacket(buf, buf.length);
        }
        @Override
        public void run() {
            while(true){
                try {
                    socket.receive(packet);
                    Message m = Message.deserializeFromBytes(packet.getData());
                    m.setAddress(packet.getAddress(), packet.getPort());
                    System.out.println("receive pkt "+m.getSeqNumber()+" "+m.getSrcID());
                    topLayer.deliveredFromBottom(m);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}