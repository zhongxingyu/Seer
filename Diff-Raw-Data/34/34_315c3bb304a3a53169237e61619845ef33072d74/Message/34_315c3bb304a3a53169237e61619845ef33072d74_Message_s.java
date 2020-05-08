 package org.kryomq;
 
import java.io.ByteArrayOutputStream;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

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
