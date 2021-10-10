package cs451.links;

import cs451.Message;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class FairLossLink extends Link {
    private final int HEADER_SIZE = 1 * 4; //int a 4 times bigger than bytes
    private final int MAX_SIZE_PACKET = 32;
    private DatagramSocket socket;
    private String ip;
    private int port;

    public FairLossLink(String ip, int port,int timeout){
        this.ip = ip;
        this.port = port;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            //InetAddress inetAddress = InetAddress.getLocalHost();
            socket = new DatagramSocket(port, inetAddress);
            socket.setSoTimeout(timeout);
        }
        catch (UnknownHostException | SocketException e){}

    }
    public FairLossLink(String ip, int port){
        this(ip,port,0);
    }

    @Override
    public Message deliver() throws SocketTimeoutException {
        byte [] buf = new byte [MAX_SIZE_PACKET];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String payload = new String(packet.getData(), 1, packet.getLength());
        Message m = new Message(packet.getAddress().getHostName(), packet.getPort(), ip, port,
                ByteBuffer.wrap(Arrays.copyOfRange(packet.getData(),0,HEADER_SIZE)).getInt(), payload);
        return m;
    }

    @Override
    public void send(Message m) {
        byte [] head = ByteBuffer.allocate(HEADER_SIZE).putInt(m.getSeqNumber()).array();
        byte [] buf = m.getPayload().getBytes();
        byte [] result = concat(head, buf);

        try{
            DatagramPacket packet
                    = new DatagramPacket(result, result.length, InetAddress.getByName(m.getDstIP()), m.getDstPort());
            socket.send(packet);
        }
        catch(UnknownHostException e){}
        catch(IOException e){}
    }

    @Override
    public void close() {
        socket.close();
    }

    static byte[] concat(byte[] a1, byte[] a2) {
        byte[] result = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }
}