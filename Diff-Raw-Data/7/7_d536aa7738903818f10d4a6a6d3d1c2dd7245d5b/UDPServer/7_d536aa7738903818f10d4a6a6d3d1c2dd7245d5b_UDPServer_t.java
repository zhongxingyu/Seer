 //
 import java.net.*;
 import java.io.*;
 
 public class UDPServer {
 	
 	public static void main (String args[]) throws Exception 
 	{
 		//use DatagramSocket for UDP connection
 		DatagramSocket s = new DatagramSocket(9001);		
 		byte[] infilenameByte = new byte[1000];
 		
 		FileOutputStream myFile = null;
 		String filename;
 		
 		DatagramPacket infilename = new DatagramPacket(infilenameByte, infilenameByte.length);
 		s.receive(infilename);
 		
 		 filename = new String(infilename.getData());
 		if (!filename.equals(null)) {
 			myFile = new FileOutputStream("Data/"+filename);
 			String reply = "Filename received!";
 			
 			byte[] outfilenameByte = new byte[1000];
 			outfilenameByte = reply.getBytes();
 
 			// create reply packet using outfilename buffer.
 			// Note: dest address/port is retrieved from infilename
 			DatagramPacket outfilename = new DatagramPacket(
 			outfilenameByte, outfilenameByte.length, infilename.getAddress(),
 			infilename.getPort());
 				
 			s.send(outfilename);
 		}
 		else {
 			System.out.println("filename not received");
 			System.exit(0);
 		}
 		
 //		myFile = new FileOutputStream("Data/"+filename);
 		int packet_number = 1;
 		
 		while (true)
 		{
 			byte[] inBuf = new byte[65504];
 			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
 			s.receive(inPkt);			
 			
 			byte[] temp = new byte[65504];							
 			temp = inPkt.getData();
 				
 			// check that the packet_number is in sequence
 			if ((int)temp[0] == packet_number) {
 				
 				// check that packet is not the last packet
 				if ((int)temp[1] == 0) {
 															
 					myFile.write(temp, 4, 65500);
 				
 					// Sends ACK of the next packet number if the packet received is in correct order
 					String ACK = Integer.toString(packet_number+1); 
 					byte[] outACKbyte = new byte[1000];
 					outACKbyte = ACK.getBytes();
 					DatagramPacket outACK = new DatagramPacket(
 							outACKbyte, outACKbyte.length, inPkt.getAddress(),
 							inPkt.getPort());
 							
 					s.send(outACK);
 					packet_number++;
 				
 				}
 				// if the packet is the last packet
 				else if ( (int) temp[1] == 1) {
 									
 					String ACK = Integer.toString(packet_number+1); 
 					byte[] outACKbyte = new byte[1000];
 					outACKbyte = ACK.getBytes();
 					DatagramPacket outACK = new DatagramPacket(
 							outACKbyte, outACKbyte.length, inPkt.getAddress(),
 							inPkt.getPort());
 								
 					s.send(outACK);
 														
					int last_packet_length = (((((int)temp[2])&0xff) << 8) | (int)temp[3]&0xff);
 					
					System.out.println("temp[2]: " + ((int)temp[2]&0xff));
					System.out.println("temp[3]: " + ((int)temp[3]&0xff));
 					System.out.println("last packet len: " + last_packet_length);
 					
 					myFile.write(temp, 4, last_packet_length);
 					myFile.close();
 					
 					
 					// Create reply
 					String reply2 = "File received!";
 							
 					byte[] outBuf = new byte[1000];
 					outBuf = reply2.getBytes();
 
 					// create reply packet using output buffer.
 					// Note: dest address/port is retrieved from inPkt
 					DatagramPacket outPacket = new DatagramPacket(
 						outBuf, outBuf.length, inPkt.getAddress(),
 						inPkt.getPort());
 
 					// finally, send the packet
 					s.send(outPacket);
 					
 					
 					
 				}
 				
 			}
 			// if the packet_number is not in sequence, request for retransmission
 			else {
 				//send ack for the previous packet number
 				String ACK = Integer.toString(packet_number); 
 				byte[] outACKbyte = new byte[1000];
 				outACKbyte = ACK.getBytes();
 				DatagramPacket outACK = new DatagramPacket(
 						outACKbyte, outACKbyte.length, inPkt.getAddress(),
 						inPkt.getPort());
 						
 				s.send(outACK);					
 			
 			}								 						 		
 											
 		}
 			
 	}	
 }
