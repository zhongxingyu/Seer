 package tr.gov.ulakbim.jDenetX.streams.net;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: caglar
  * Date: Sep 20, 2010
  * Time: 1:55:55 PM
  * To change this template use File | Settings | File Templates.
  */
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.*;
 
 /**
  * @author caglar
  */
 public class UDPStreamReceiver {
 
     private int PortNo = 4000;
     private int PacketSize = 1024;
     private boolean HostConstraint = false;
     private InetAddress RemoteAddr = null;
     private DatagramSocket Sock = null;
     private final String host = "127.0.0.1";
 
     public UDPStreamReceiver() { }
 
     public UDPStreamReceiver(int portNo, int packetSize) {
         PortNo = portNo;
         PacketSize = packetSize;
     }
 
     public void putHostConstraint(InetAddress remoteAddr) {
         HostConstraint = true;
         RemoteAddr = remoteAddr;
     }
 
     protected String convertBinToString(byte[] data) {
         String sData = "";
         ByteArrayInputStream bin = new ByteArrayInputStream(data);
         for (int i = 0; i < data.length; i++) {
             int iData = bin.read();
             if (iData == -1)
                 break;
             else
                 sData += (char) iData;
         }
         return sData;
     }
     /*
      * Tries to bind to a specific host
      */
     public void openHostSocket(int sockTimeout) throws SocketException {
         SocketAddress sAddress = new InetSocketAddress(host, PortNo);
         Sock = new DatagramSocket(sAddress);
         Sock.setSoTimeout(sockTimeout);
 
     }
 
     public void openSocket(int portNo, int sockTimeout) throws SocketException {
         PortNo = portNo;
         Sock = new DatagramSocket(PortNo);
         Sock.setSoTimeout(sockTimeout);
         Sock.setReuseAddress(true);
     }
 
     public void openSocket(int sockTimeout) throws SocketException {
         Sock = new DatagramSocket(PortNo);
         Sock.setSoTimeout(sockTimeout);
         Sock.setReuseAddress(true);
     }
 
     public void openSocket() throws SocketException {
         Sock = new DatagramSocket(PortNo);
         Sock.setReuseAddress(true);
     }
 
     public void closeSocket() {
         Sock.close();
     }
 
     public boolean isSockBound() {
         return Sock.isBound();
     }
 
     public String getPacketData(int portNo, int packetSize) throws IOException {
         String packData = "";
         DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
         if (Sock == null) {
             throw new NullPointerException("getPacketData: Socket can not be null!");
         }
         Sock.receive(packet);
         InetAddress remote_addr = packet.getAddress();
         if (HostConstraint) {
             if (remote_addr == RemoteAddr) {
                 packData = convertBinToString(packet.getData());
             }
         } else {
             packData = convertBinToString(packet.getData());
         }
         return packData;
     }
 
    public String getPacketData() throws SocketException, IOException {
         String packData = "";
         if (Sock == null) {
             throw new NullPointerException("getPacketData: Socket can not be null!");
         }
         DatagramPacket packet = new DatagramPacket(new byte[PacketSize], PacketSize);
         if (Sock.isBound()) {
             Sock.receive(packet);
         }
         InetAddress remote_addr = packet.getAddress();
         if (HostConstraint) {
             if (remote_addr == RemoteAddr) {
                 packData = convertBinToString(packet.getData());
             }
         } else {
             packData = convertBinToString(packet.getData());
         }
         return packData;
     }
 }
