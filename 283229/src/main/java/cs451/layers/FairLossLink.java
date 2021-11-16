package cs451.layers;

import cs451.Host;
import cs451.Messages.Message;
import cs451.Messages.Packet;
import cs451.Parsing.Parser;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FairLossLink extends Layer {
    private final int MAX_SIZE_PACKET = 400; // it is between 217 and and 1 more for the sequence number with 1 more digit
    private DatagramSocket socket;
    private String ip;
    private int port;
    private Parser parser;

    //Threads
    private ConcurrentLinkedDeque<Packet> mToSend;
    Thread flST;
    Thread flRT;

    public FairLossLink(Layer topLayer,  Parser parser){
        //Layers
        super.setTopLayer(topLayer);
        super.setDownLayer(null);

        //Socket stuff
        Host me = parser.getME();
        this.ip = me.getIp();
        this.port = me.getPort();
        this.parser = parser;
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

    public <PKT extends Message> void sentFromTop(PKT m) {
        mToSend.addLast((Packet) m);
    }

    @Override
    public void close() {
        closed = true;
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
            while(!closed){
                if(!mToSend.isEmpty()) {
                    Packet m = mToSend.pollFirst();
                    try {
                        DatagramPacket packet = getDatagramPacketFromPacket(m);
                        socket.send(packet);
                        //System.out.println("send "+m);
                    } catch (UnknownHostException | SocketException e) {
                        //e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        private DatagramPacket getDatagramPacketFromPacket(Packet m) throws UnknownHostException {
            byte[] result = m.serializeToBytes();
            Host dst = parser.getHostWithId(m.getDstID());
            DatagramPacket pkt = new DatagramPacket(result, result.length,
                    InetAddress.getByName(dst.getIp()), dst.getPort());
            return pkt;
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
            while(!closed){
                try {
                    socket.receive(packet);
                    Packet m = (Packet)(Message.deserializeFromBytes(packet.getData()));
                    //System.out.println("receive "+m);
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