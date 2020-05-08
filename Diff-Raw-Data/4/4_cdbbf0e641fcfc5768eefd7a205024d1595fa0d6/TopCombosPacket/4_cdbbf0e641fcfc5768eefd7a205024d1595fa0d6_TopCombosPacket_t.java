 package com.wayne.cookoutapp.server.net.packet.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import com.wayne.cookoutapp.server.ComboRating;
 
 public class TopCombosPacket extends ServerPacket {
 	
 	public TopCombosPacket(List<ComboRating> topList) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		DataOutputStream resp = new DataOutputStream(bos);
 		
 		try {
			resp.writeByte(SERVER_PACKET_HEADER_TOP_COMBOS);
 			for(ComboRating cr : topList) {
				
 				resp.writeByte(cr.getFlavor1());
 				resp.writeByte(cr.getFlavor2());
 				resp.writeInt(cr.getTotalRating());
 				resp.writeInt(cr.getTimesRated());
 			}
 
 			resp.writeByte(0x00);
 			
 		} catch (IOException e) {
 			
 		}
 		
 		data = bos.toByteArray();
 		
 	}
 }
