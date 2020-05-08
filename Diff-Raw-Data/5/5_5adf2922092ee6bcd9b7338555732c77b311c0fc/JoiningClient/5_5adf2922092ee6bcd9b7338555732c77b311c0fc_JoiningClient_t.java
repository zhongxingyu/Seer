 package client;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 import common.MessageType;
 import common.PackageType;
 
 public class JoiningClient {
 	
 	public static int bytesToInt(byte[] b, int off, int len){
             int val = 0;
             int mult = 1;
             for (int i=off+len-1; i>=off; i--)
             {
                     int toadd = b[i];
                     if ( toadd < 0)
                     {
                             toadd = 256 + toadd;
                     }
                     val += toadd * mult;
 
                     mult *= 256;
             }
 
             return val;
     }
 	
 	public static long bytesToLong(byte[] b, int off, int len){
 		long val = 0;
         int mult = 1;
         for (int i=off+len-1; i>=off; i--)
         {
                 int toadd = b[i];
                 if ( toadd < 0)
                 {
                         toadd = 256 + toadd;
                 }
                 val += toadd * mult;
 
                 mult *= 256;
         }
 
         return val;
     }
 	
 	
 	
 	public static void main(String args[]){
 		if(args.length != 3){
 			System.out.println("Proper use: port adress name");
 			System.exit(1);
 		}
 		
 		int port = Integer.parseInt(args[0]);
 		InetAddress iaddr = null;
 		
 		try {
 			iaddr = InetAddress.getByName(args[1]);
 		} catch (UnknownHostException e) {
 			System.out.println("unknown host.");
 			System.exit(1);
 		}
 		
 		String name = args[2];
 		
 		byte[] buf = name.getBytes();
 		
 		DatagramPacket send = new DatagramPacket(buf, buf.length, iaddr, port);
 		
 		int size = 65000;
 		byte[] buf2 = new byte[size];
 		DatagramPacket rec = new DatagramPacket(buf2, size);
 		
 		DatagramSocket sock = null;
 		try {
 			sock = new DatagramSocket();
 		} catch (SocketException e) {
 			System.out.println("unable to create socket.");
 			System.exit(1);
 		}
 		
 		try {
 			sock.send(send);
 		} catch (IOException e) {
 			System.out.println("IOException: Unable to send packet.");
 			System.exit(1);
 		}
 		
 		int id = -1;
 		long randSeed = -1l;
 		while(true){
 			try {
 				sock.setSoTimeout(5000);
 				sock.receive(rec);
 				if( rec.getData()[0] == PackageType.INITIALIZER ){
 					id = bytesToInt(rec.getData(), 1, 4);
 					randSeed = bytesToLong(rec.getData(),5,8);
					sock.setSoTimeout(0);
 					System.out.println("Established contact with server. Got id: "+ id +". Got seed: "+ randSeed);
 				}
 				if(rec.getData()[0] == PackageType.PLAYER_JOINED ){ 
 					int nid = bytesToInt(rec.getData(), 1, 4);
 					byte[] bt = new byte[rec.getLength() - 5];
					System.arraycopy(rec.getData(), 5, bt, 0, bt.length);
 
 					System.out.println("PLayer Joined. ID = " + nid + " Name = " + new String(bt));
 				}
 			} catch (SocketTimeoutException e ) {
 				try {
 					sock.send(send);
 				} catch (IOException e1) {
 					System.out.println("IOException: Unable to send packet.");
 					System.exit(1);
 				}
 			} catch (IOException e) {
 				System.out.println("IOException: Unable to send packet.");
 				System.exit(1);
 			}
 		}
 		
 		
 		
 		//Generate world with the seed. etc..
 		
 		
 		
 		
 		
 	}
 }
