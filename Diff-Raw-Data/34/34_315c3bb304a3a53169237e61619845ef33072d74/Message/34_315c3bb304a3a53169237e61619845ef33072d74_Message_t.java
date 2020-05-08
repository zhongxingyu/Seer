 package org.kryomq;
 
 public class Message {
 	public boolean reliable;
 	public String topic;
 	public String origin;
 	public byte[] buf;
 
 	public Message() {}
 	
 	public Message(byte[] buf) {
 		this(buf, true);
 	}
 
 	public Message(byte[] buf, boolean reliable) {
 		this(null, buf, reliable);
 	}
 	
 	public Message(String topic, boolean reliable) {
 		this(topic, null, reliable);
 	}
 	
 	public Message(String topic, byte[] buf, boolean reliable) {
 		this.topic = topic;
 		this.buf = buf;
 		this.reliable = reliable;
 	}
 }
