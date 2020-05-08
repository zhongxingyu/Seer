 
 package com.zanoccio.packetkit.headers;
 
 import com.zanoccio.packetkit.PacketFragment;
 import com.zanoccio.packetkit.PacketUtilities;
 
 public enum ARPOpcode implements PacketFragment {
	REPLY(0x0001),
	REQUEST(0x0002);
 
 	// there are many more:
 	// http://www.iana.org/assignments/arp-parameters/arp-parameters.xml
 
 	private byte[] bytes;
 
 
 	ARPOpcode(int code) {
 		bytes = PacketUtilities.toByteArray((short) code);
 	}
 
 
 	@Override
 	public byte[] getBytes() {
 		return bytes;
 	}
 
 
 	@Override
 	public int size() {
 		return 2;
 	}

 }
