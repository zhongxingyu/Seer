 
 package com.zanoccio.packetkit.headers;
 
 import com.zanoccio.packetkit.ByteTrie;
 import com.zanoccio.packetkit.PacketFragment;
 import com.zanoccio.packetkit.PacketUtilities;
 
 public enum ICMPType implements PacketFragment {
 	ECHO_REQUEST(0x8);
 
 	private static ByteTrie<ICMPType> TRIE = PacketUtilities.trieFromEnum(ICMPType.class);
 
 
	public ICMPType fromByte(byte[] bytes) {
		return TRIE.get(bytes);
 	}
 
 
 	private byte[] bytes;
 
 
 	ICMPType(int code) {
 		bytes = new byte[] { (byte) code };
 	}
 
 
 	@Override
 	public byte[] getBytes() {
 		return bytes;
 	}
 
 
 	@Override
 	public int size() {
 		return 1;
 	}
 }
