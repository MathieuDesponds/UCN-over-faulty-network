package cs451.links;

import cs451.Message;

import java.io.IOException;
import java.net.*;

public class FairLossLink extends Link {
    private DatagramSocket socket;
    private String ip;
    private int port;

    public FairLossLink(String ip, int port){
        this.ip = ip;
        this.port = port;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            //InetAddress inetAddress = InetAddress.getLocalHost();
            socket = new DatagramSocket(port, inetAddress);
        }
        catch (UnknownHostException e){}
        catch (SocketException e) {}

    }

    @Override
    public Message deliver() {
        byte [] buf = new byte [32];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String payload = new String(packet.getData(), 0, packet.getLength());
        Message m = new Message(packet.getAddress().getHostName(), packet.getPort(), ip, port, 0, payload);
        return m;
    }

    @Override
    public void send(Message m) {
        byte [] buf = m.getPayload().getBytes();
        try{
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, InetAddress.getByName(m.getDstIP()), m.getDstPort());
            socket.send(packet);
        }
        catch(UnknownHostException e){}
        catch(IOException e){}
    }
}
