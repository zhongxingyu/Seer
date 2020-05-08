 
 package com.zanoccio.packetkit.headers;
 
 import com.zanoccio.packetkit.ByteTrie;
 import com.zanoccio.packetkit.PacketFragment;
 import com.zanoccio.packetkit.PacketUtilities;
import com.zanoccio.packetkit.headers.annotations.FixedSize;
 
@FixedSize(size = 1)
 public enum ICMPType implements PacketFragment {
 	ECHO_REQUEST(0x8);
 
 	private static ByteTrie<ICMPType> TRIE = PacketUtilities.trieFromEnum(ICMPType.class);
 
 
	public static ICMPType fromBytes(byte[] bytes, int offset, int length) {
		return TRIE.get(bytes, offset, length);
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
