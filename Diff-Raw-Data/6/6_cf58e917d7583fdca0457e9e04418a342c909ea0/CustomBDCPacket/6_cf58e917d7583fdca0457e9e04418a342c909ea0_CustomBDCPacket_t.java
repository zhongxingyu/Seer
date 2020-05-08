 package gieraffe.bdc.network;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteStreams;
 
 import net.minecraft.network.packet.Packet250CustomPayload;
 
 /**
  * Standard package for client-server communication
  * @author daniel
  *
  */
 public class CustomBDCPacket {
 	
 	public String channel;
 	/* ID of the block correspending to gui/container/tile */
 	public int blockID;
 	public int x, y, z;
 	public int[] message;
 	
 	private Packet250CustomPayload packet = null;
 	
 	/**
 	 * Constructor for new Packets, used by the packet sender
 	 * @param channel
 	 * @param blockID
 	 * @param x, y, z world
 	 * @param data
 	 */
 	public CustomBDCPacket(String parChannel, int parBlockID, int parX, int parY, int parZ, int[] parMessage) {
 		
 		this.channel = parChannel;
 		this.blockID = parBlockID;
 		this.x = parX;
 		this.y = parY;
 		this.z = parZ;
 		this.message = parMessage;
 		
 		
 		// allocate new package
 		packet = new Packet250CustomPayload();
 		
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 				
 		// fill in package data
 		try {			
 			// set meta
 			outputStream.writeInt(this.blockID);
 			outputStream.writeInt(this.x);
 			outputStream.writeInt(this.y);
 			outputStream.writeInt(this.z);
 			outputStream.writeInt(this.message.length);
 			
 			for (int i = 0; i < this.message.length; i++) { 				//set message
 				outputStream.writeInt(this.message[i]);
 			}
 			
 		} catch (Exception ex) {
 		        ex.printStackTrace();
 		}
 
 		packet.channel = channel;
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 	}
 	
 	/**
 	 * Constructor used by the Packet Handler
 	 * @param packet
 	 */
 	public CustomBDCPacket(Packet250CustomPayload parPacket) {
 		this.packet = parPacket;
 		
		// read out channel (for cosmetics)..
		this.channel = parPacket.channel;
		
 		// read out packet data
 		ByteArrayDataInput data = ByteStreams.newDataInput(this.packet.data);
 		
 		this.blockID = data.readInt();
 		this.x = data.readInt();
 		this.y = data.readInt();
 		this.z = data.readInt();
 		int messagelength = data.readInt();
 		
 		// read out the message in the data
 		this.message = new int[messagelength];
 		for (int i = 0; i < messagelength; i++) {
			message[i] = data.readInt();
 		}
 	}
 	
 	public Packet250CustomPayload getPacket() {
 		return (this.packet != null) ? this.packet : null;
 	}
 	
 }
