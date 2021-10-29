package cs451.layers.links;

import cs451.Message;
import cs451.layers.Layer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FairLossLink extends Link {
    private final int INT_SIZE = 4;
    private final int INT_IN_HEADER = 2;
    private final int HEADER_SIZE = INT_IN_HEADER * INT_SIZE; //int a 4 times bigger than bytes
    private final int MAX_SIZE_PACKET = 32;
    private DatagramSocket socket;
    private Layer topLayer;
    private String ip;
    private int port;

    //Threads
    private ConcurrentLinkedDeque<Message> mToSend;

    public FairLossLink(Layer topLayer, String ip, int port, int timeout){
        this.topLayer = topLayer;
        this.ip = ip;
        this.port = port;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            socket = new DatagramSocket(port, inetAddress);
            socket.setSoTimeout(timeout);
        }
        catch (UnknownHostException | SocketException e){
            System.err.println(e.getStackTrace());
        }
        mToSend = new ConcurrentLinkedDeque<>();
    }
    public FairLossLink(Link topLink,String ip, int port){
        this(topLink, ip, port,0);
    }

    @Override
    public Message deliver() throws SocketTimeoutException {
        byte [] buf = new byte [MAX_SIZE_PACKET];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            String payload = new String(packet.getData(), INT_IN_HEADER, packet.getLength());
            ByteBuffer b = ByteBuffer.wrap(Arrays.copyOfRange(packet.getData(),0,HEADER_SIZE));
            Message m = Message.deserializeFromBytes(packet.getData());
            //ToDo add the addresses
            return m;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (SocketException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void send(Message m) {
        byte[] result = m.serializeToBytes();

        try {
            DatagramPacket packet
                    = new DatagramPacket(result, result.length, InetAddress.getByName(m.getDstIP()), m.getDstPort());
            socket.send(packet);
            //System.out.println("send pkt "+m.getSeqNumber()+" "+m.getSndID());
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
    }

    @Override
    public void close() {
        socket.close();
    }

    public void setTimeOut(int timeoutInterval) {
        try {
            socket.setSoTimeout(timeoutInterval);
        } catch (SocketException e) {

        }
    }

    @Override
    public void deliveredFromBottom(Message m) {

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
                        //System.out.println("send pkt "+m.getSeqNumber()+" "+m.getSndID());
                    } catch (UnknownHostException e) {
                    } catch (IOException e) {
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
                    //ToDo add the addresses
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