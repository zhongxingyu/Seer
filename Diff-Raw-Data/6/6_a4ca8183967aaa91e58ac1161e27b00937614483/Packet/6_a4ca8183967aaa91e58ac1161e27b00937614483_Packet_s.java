 package wifi;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 /**
  * @author Alexander  King & Michael Lim
  * 
  * The Packet class wraps all neccesary information that is required for 
  * a transmission for the 802.11~ spec.
  * 
  */
 
 public class Packet {
 
 	//byte[] frame;
 
 	int frameType;
 	int retry;
 	short seqNum;
 	short destAddr;
 	short srcAddr;
 	byte[] data;
 	byte[] crc;
 	ByteBuffer buf;
 
 	public Packet(byte[] frame){
 		if(frame == null){
 			throw new IllegalArgumentException("Invalid packet. Packet is null.");
 		}else if(frame.length > 2038){
 			throw new IllegalArgumentException("Invalid packet size. Packet too big!");
 		} else if( frame.length < 1 ){
 			throw new IllegalArgumentException("Invalid packet size. No packet data.");
 		}
 
 		buf = ByteBuffer.allocate(frame.length);
 		buf = ByteBuffer.wrap(frame);
 
 
 
 		byte[] tempData = new byte[frame.length-10];
 		for(int i=6;i<frame.length-4;i++){ //make sub data[]
 			tempData[i-6] = frame[i];
 		}
 		//Fill packet data
 		setData(tempData);
 		//setDestAddr((short)((frame[3] | frame[2] << 8) & 0xFFF));
 		setSrcAddr(buf.getShort(4));
 		setDestAddr(buf.getShort(2));
 
 		byte[] tempCrc = new byte[4];
 		tempCrc[0] = frame[frame.length-4];
 		tempCrc[1] = frame[frame.length-3];
 		tempCrc[2] = frame[frame.length-2];
 		tempCrc[3] = frame[frame.length-1];
 
 		setCrc(tempCrc);
 		setFrameType((frame[0] >> 5) & 0x7);
 		retry = ((frame[0] >> 4) & 0x1);
 		setSeqNum((short)((frame[1])| ((frame[0]) << 8) & 0xFFF));
 	}
 
 	public Packet(int frameType, short seqNum, short destAddr, short srcAddr, byte[] data, byte[] crc){

		buf = ByteBuffer.allocate(10 + data.length);
 
 		setData(data); //set data first to short circuit
 		setFrameType(frameType);
 		setSeqNum(seqNum);		
 		setDestAddr(destAddr);
 		setSrcAddr(srcAddr);
 		setRetry(false);
 		setCrc(crc);
 	}
 
 	private short makeControl(int frameType, int retry, short seqNum){
 		int temp;
 		temp = (frameType << 13) | (retry << 12) | seqNum;
 		//Test Shifting
 		//		System.out.println("frameType: " + (frameType << 5));
 		//		System.out.println("retry: " + retry);
 		//		System.out.println("seqNum: " + seqNum);
 		//		System.out.println("byte: " + temp);
 		//		System.out.println("makeControl: " + temp);
 		//		System.out.println("Making control: " + temp);
 		return (short)temp;
 	}
 
 	public void setFrameType(int type){
 		//Check frame type
 		if(type < 0 || type > 7){
 			throw new IllegalArgumentException("Invalid frameType.");
 		}else{
 			frameType = type;
 		}
 
 		//put in ByteBuffer
 		short control = makeControl(frameType,this.retry, seqNum);
 		buf.putShort(0, control); //put control bytes
 	}
 
 	public int getFrameType(){
 		return frameType;
 	}
 
 	public void setRetry(boolean input){
 		if (input){
 			retry = 1;
 		}
 		else{
 			retry = 0;
 		}
 
 		//put in ByteBuffer
 		short control = makeControl(frameType,this.retry, seqNum);
 		buf.putShort(0, control); //put control bytes
 	}
 
 	public boolean isRetry(){
 		if(retry == 1){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	public void setSeqNum(short seq){
 		//Check seqNum
 		if(seq < 0 || seq > 4095){
 			throw new IllegalArgumentException("Invalid seqNum.");
 		}else{
 			seqNum = seq;
 		}
 
 		//put in ByteBuffer
 		short control = makeControl(frameType,this.retry, seqNum);
 		buf.putShort(0, control); //put control bytes
 	}
 
 	public short getSeqNum(){
 		return seqNum;
 	}
 
 	public void setDestAddr(short addr){
 		//Check destAddr
 		if((addr&0xff) < 0 || (addr&0xff) > 65535){
 			throw new IllegalArgumentException("Invalid destAddr.");
 		}else{
 			destAddr = addr;
 		}
 
 		//put in ByteBuffer
 		buf.putShort(2, destAddr); // put destAddr bytes
 	}
 
 	public short getDestAddr(){
 		return destAddr;
 	}
 
 	public void setSrcAddr(short addr){
 		//Check srcAddr
 		if((addr&0xff) < 0 || (addr&0xff) > 65535){
 			throw new IllegalArgumentException("Invalid srcAddr.");
 		}else{
 			this.srcAddr = addr;
 		}
 
 		//put in ByteBuffer
 		buf.putShort(4, srcAddr); // put srcAddr bytes
 	}
 
 	public short getSrcAddr(){
 		return srcAddr;
 	}
 
 	public void setData(byte[] inData){
 		//Check data
 		if(inData.length > 2038){
 			throw new IllegalArgumentException("Invalid data.");
 		}else{
 			data = inData;
 		}
 
 		if(inData != null){
 			//put in ByteBuffer
 			for(int i=0;i<data.length;i++){ //put data bytes
 				buf.put(i+6,data[i]);
 			}
 		}
 
 
 	}
 
 	public byte[] getData(){
 		return data;
 	}
 
 
 	public void setCrc(byte[] input){
 		if(input == null || input.length > 4){
 			throw new IllegalArgumentException("Invalid data.");
 		}else{
 			crc = input;
 		}
 		for(int i=0;i<4;i++){ //put crc bytes
 			buf.put(buf.limit()-(4-i),crc[i]);
 		}
 	}
 
 	public byte[] getCrc(){
 		return crc;
 	}
 
 	public byte[] getFrame(){
 		return buf.array();
 	}
 
 	public String toString(){
 		String toString = "";
 		for(int i = 0;i <getFrame().length; i++){
 			if(i<getFrame().length){
 				toString = toString + (getFrame()[i] + " ");
 			}else{
 				toString = toString + (getFrame()[i]);
 			}
 		}
 		return toString;
 	}
 }
