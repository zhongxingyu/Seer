 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.BitSet;
 
 
 public class ActualMessage implements Serializable {
 
 
 	private static final long serialVersionUID = 3545119718998974040L;
 	int length;
 	int messageType;
 	byte[] messagePayload;
 
 	//Constructor for choke,unchoke, interested, not interested
 		ActualMessage(String mType) {
 			if(mType=="choke") {
 				messageType = 0;
 			}
 			else if(mType=="unchoke") {
 				messageType = 1;
 			}
 			else if(mType=="interested") {
 				messageType = 2;
 			}
 			else { //not interested
 				messageType = 3;
 			}
 			
 			length = 0;
 		}
 		
 		//Constructor for have, request
 		ActualMessage(String mType,int index) {
 			if(mType=="have") {
 				messageType = 4;
 			}
 			else { //request
 				messageType = 6;
 			}
 			byte[] chunkIndex = ByteBuffer.allocate(4).putInt(index).array();
 			messagePayload = new byte[chunkIndex.length];
 			System.arraycopy (chunkIndex,0,messagePayload,0, chunkIndex.length);
 			
 			
 			length = 0;
 		}
 	
 	//Constructor for bitfield message
 	ActualMessage(int pieces) {
 		messageType = 5;
 		BitSet bitfield = new BitSet(pieces);
 		for(int i=0;i<pieces;i++) {
 		bitfield.set(i);
 		}
 		 byte[] abcd= toByteArray(bitfield).clone();
 		 messagePayload = abcd.clone();
 		length = messagePayload.length;
 		
 	}
 
 	//Constructor for piece messages
 	ActualMessage(byte[] myChunk, int chunkid) {
 		byte[] chunkid_b = ByteBuffer.allocate(4).putInt(chunkid).array();
 		
 		assert (myChunk.length > 0);
 		assert (chunkid_b.length == 4);
 		messageType = 7;
 		messagePayload = new byte[myChunk.length+chunkid_b.length];
		peerProcess.logger.print("\nparameter length "+myChunk.length+" chunkid_b" +chunkid_b.length+" messagepayload length "+messagePayload.length);
 		System.arraycopy (chunkid_b,0,messagePayload,0, chunkid_b.length);
		System.arraycopy (myChunk,0,messagePayload,chunkid_b.length, myChunk.length);
 		length = messagePayload.length;
 		//		peerProcess.logger.println("Actual message payload content is \n\n\n"+new String(messagePayload)+"\n\n");
 	}
 
 
 	public int getChunkid(){
 	  ByteBuffer chunkid_b = ByteBuffer.wrap(messagePayload);
 	  chunkid_b.position(0);
 	  return chunkid_b.getInt();
 	}
 	
 	public byte[] getPayload(){
 	  byte[] payload = new byte[messagePayload.length-4];
 	  System.arraycopy(messagePayload, 4, payload, 0, messagePayload.length-4);
 	  return payload;
 	}
 
 	/*	ActualMessage(int l,Byte mT, Byte mP) {
 		length = l;
 		messageType = new Byte(mT); 
 		messagePayload = new Byte(mP); // Single chunk in byte
 		bitfield = new BitSet(l);
 		bitfield.set(1,true);
 	}*/
 
 	public BitSet toBitSet(byte[] bytes) {
 		BitSet bits = new BitSet();
 		for(int i=0;i<bytes.length*8;i++) {
 			if((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0){
 				bits.set(i);
 			}
 		}
 		return bits;
 	}
 
 	public byte[] toByteArray (BitSet bits) {
 		byte[] bytes = new byte[bits.length()/8+1];
 		for(int i=0;i<bits.length();i++) {
 			if(bits.get(i)) {
 				bytes[bytes.length-i/8-1] |= 1<<(i%8);
 			}
 		}
 		return bytes;
 	}
 
 	/*public static void main(String arg[])
 	{
 		ActualMessage a = new ActualMessage(306);
 		System.out.println(a.messagePayload.length);
 		//System.out.println(a.messagePayload);
 		
 		BitSet mb = new BitSet(10);
 		mb.set(2);
 		System.out.println(mb.get(2));
 		System.out.println(mb.get(9));
 		byte[] mby = new byte[mb.length()/8+1];
 		mby = toByteArray(mb);* 
 		for (int i =0;i<mby.length;i++){
 		System.out.println(mby[i]);}
 		
 		BitSet mb2 = new BitSet();
 		mb2 = toBitSet(mby);
 		System.out.println(mb2.get(2));
 		System.out.println(mb2.get(9));
 		//System.out.println(a.messageType);
 		//System.out.println(a.bitfield.toString());
 		//System.out.println(a.bitfield.get(0));
 		BitSet myRecvBits = new BitSet(18);
 		myRecvBits = a.toBitSet(a.messagePayload); 					
 		int maxIndex = 306;
 		
 		int index=0, firstBit=-1;
 		while(index<maxIndex && index!=firstBit) {
 			int x = myRecvBits.nextSetBit(index);
 			index=x+1;
 			firstBit=x;
 			System.out.println(x);
 		}
 		
 		System.out.println("Size of bitfield "+myRecvBits.size()+"\n"+" content "+myRecvBits.toString());
 		
 
 
 	}*/
 }
