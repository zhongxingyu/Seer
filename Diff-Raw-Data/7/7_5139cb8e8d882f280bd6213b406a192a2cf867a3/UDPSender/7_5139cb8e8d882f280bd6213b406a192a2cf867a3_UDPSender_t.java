 package Communications;
 import java.io.*;
 import java.net.*;
 import java.util.Enumeration;
 
 import Messages.Message;
 public class UDPSender {
 	protected int port;
 	protected DatagramSocket socket;
 	InetAddress ip;
 	
 	public UDPSender() throws SocketException, UnknownHostException{
 		port =12345;
 		socket=new DatagramSocket(port);
 		socket.setBroadcast(true);
 		InetAddress broadcast = null;
 		Enumeration<NetworkInterface> interfaces =NetworkInterface.getNetworkInterfaces();
 		while (interfaces.hasMoreElements()) {
 			NetworkInterface networkInterface = interfaces.nextElement();
 			
 			if (networkInterface.isLoopback())
 				continue;    // Don't want to broadcast to the loopback interface
 			for (InterfaceAddress interfaceAddress :
 				networkInterface.getInterfaceAddresses()) {
 				broadcast = interfaceAddress.getBroadcast();
 				if (broadcast == null)
 					continue;
 				break;
 			}
 			if(broadcast!=null && broadcast.toString().contains("192.168.224")){
 				break;
 			}
 		}
 		//broadcast=InetAddress.getByName("192.168.224.142");
 		if(broadcast==null){
 			throw new UnknownHostException("\rNo broadcast address");
 		}
 		ip = broadcast;
 		System.out.println(broadcast.toString());
 	}
 	
 	public void sendMessage(Message message) throws IOException{
 		byte[] buffer=message.convert();
 		DatagramPacket packet = new DatagramPacket(buffer,buffer.length,ip,12346);
 		socket.send(packet);
 		System.out.println(new String(packet.getData(),0,packet.getLength()));
 	}
 	
 	public void close(){
 		socket.close();
 	}
 }
