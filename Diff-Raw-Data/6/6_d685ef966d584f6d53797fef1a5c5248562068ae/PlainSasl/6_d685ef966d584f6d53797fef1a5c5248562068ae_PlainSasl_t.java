 package com.zhuri.talk.protocol;
 
 import java.nio.ByteBuffer;
 import com.zhuri.util.Base64Codec;
 
 public class PlainSasl extends Packet {
 	final static String sasl = "<auth mechanism='PLAIN' xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>";
 
 	private String mUser;
 	private String mSecrect;
 	public PlainSasl(String user, String secrect) {
 		mSecrect = secrect;
 		mUser = user;
 	}
 
 	@Override
 	public String toString() {
 		byte[] buf;
 		String data;
 		ByteBuffer buffer = ByteBuffer.allocate(1000);
 
 		buffer.put((byte)0x0);
		try {
			buffer.put(mUser.getBytes("UTF-8"));
		} catch (Exception e) {
			buffer.put(mUser.getBytes());
		}
 		buffer.put((byte)0x0);
 		buffer.put(mSecrect.getBytes());
 		buffer.flip();
 
 		buf = new byte[buffer.limit()];
 		buffer.get(buf);
 
 		data = Base64Codec.encode(buf);
 		return sasl + data + "</auth>";
 	}
 }
