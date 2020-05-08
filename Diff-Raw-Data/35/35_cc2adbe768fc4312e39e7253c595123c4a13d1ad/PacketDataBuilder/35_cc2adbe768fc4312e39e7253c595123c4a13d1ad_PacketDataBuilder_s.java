 package org.sdu.server;
 
 import java.nio.ByteBuffer;
 
 import org.sdu.net.Packet;
 
 
 /**
  * 
  * */
 public class PacketDataBuilder {
 	ByteBuffer DataArr;
 	public PacketDataBuilder(){
 		DataArr = ByteBuffer.allocate(0xffff);
 		DataArr.limit(0);
 	}
 	public void SetData(byte...list){
 		DataArr.limit(DataArr.limit()+list.length);
 		DataArr.put(list,0,list.length);
 	}
 	public void SetParamB(byte...list){
		DataArr.limit(DataArr.limit()+list.length);
 		DataArr.put((byte) 0x05);
		DataArr.put((byte) list.length);
 		DataArr.put(list);
 	}
 	public void SetParamS(String tmp){
		DataArr.limit(DataArr.limit()+tmp.getBytes().length);
 		DataArr.put((byte) 0x05);
		DataArr.put((byte) tmp.getBytes().length);
 		DataArr.put(tmp.getBytes());
 	}
 	public Packet GetData(){
 		return new Packet(DataArr);
 	}
 }
