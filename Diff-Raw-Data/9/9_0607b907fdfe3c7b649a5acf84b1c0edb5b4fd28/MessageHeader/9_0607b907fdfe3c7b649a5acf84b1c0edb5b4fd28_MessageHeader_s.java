 package me.kevinwells.darxen.data;
 
 import java.io.IOException;
 
 public class MessageHeader {
 
 	public static MessageHeader parse(DataFileStream stream) throws ParseException, IOException {
 		MessageHeader res = new MessageHeader();
		//FIXME wtf?
 		stream.skip(3);
 		
 		short messageCode = stream.readShort();
 		short date = stream.readShort();
 		int time = stream.readInt();
 		int length = stream.readInt();
 		short source = stream.readShort();
 		short dest = stream.readShort();
 		short numBlocks = stream.readShort();
 		
 		return res;
 	}
 
 }
