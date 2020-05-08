 package Communications;
 import java.io.*;
 import java.net.*;
 //import java.util.Enumeration;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 
 public class UDPReceiverThread implements Runnable {
 		protected DatagramSocket socket=null;
 		protected ConcurrentLinkedQueue<Byte> out =null;
 		protected volatile boolean running;
 		
 		public UDPReceiverThread(ConcurrentLinkedQueue<Byte> queue) throws SocketException{
 			/*InetAddress broadcast = null;
 			Enumeration<NetworkInterface> interfaces =NetworkInterface.getNetworkInterfaces();
 			while (interfaces.hasMoreElements()) {
 				NetworkInterface networkInterface = interfaces.nextElement();
 				if (networkInterface.isLoopback())
 					continue;    // Don't want to broadcast to the loopback interface
 				for (InterfaceAddress interfaceAddress :
 					networkInterface.getInterfaceAddresses()) {
 					broadcast = interfaceAddress.getAddress();
 					if (!broadcast.toString().contains("192.168"))
 						continue;
 					break;
 				}
 				if(broadcast!=null || broadcast.toString().contains("192.168")){
 					break;
 				}
 			}*/
 			try {
 				socket = new DatagramSocket(12346,InetAddress.getByName("0.0.0.0"));
 			} catch (UnknownHostException e) {
 				System.out.println("\rFailed to create UDP receiver");
 			}
 			socket.setSoTimeout(500);
 			out=queue;
 		}
 		
 		
 		public void setRunning(boolean isRunning){
 			running=isRunning;
 		}
 		
 		public void run(){
 			running=true;
 			byte[] buffer=new byte[1024];
 			DatagramPacket packet= new DatagramPacket(buffer,buffer.length);
 			while(running){
 				try{
 					socket.receive(packet);
 					byte[] data=packet.getData();
 					for(int i=0;i<data.length;i++){
 						out.add(data[i]);
						System.out.println(out.size());
						System.out.println("i="+Integer.toString(i));
 					}
 					System.out.println("Got it");
 				}
 				catch(SocketTimeoutException e){
 				} catch (IOException e) {
 					System.out.println("socket receive error");
 					running=false;
 				}
 			}
 			socket.close();
 		}
 		
 
 }
