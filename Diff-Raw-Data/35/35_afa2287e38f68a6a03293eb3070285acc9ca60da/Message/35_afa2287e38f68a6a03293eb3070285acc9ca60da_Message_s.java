 package asl;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 public final class Message {
 	public String content;
 	public long recieverId;
 	public long senderId;
 	public String context;
 	public long timestamp;
 	public long queueId;
 	
 	public Message(long recieverId, long senderId, long timestamp, long queueId, String content, String context){
 		this.content = content;
 		this.recieverId = recieverId;
 		this.senderId = senderId;
 		this.context = context;
 		this.timestamp = timestamp;
 		this.queueId = queueId;
 	}
 	
 	public Message(String str) {
 		// Interpret string
 	}
 	
 	public ByteBuffer toByteBuffer() {
 		// use data from object to generate a bytebuffer
		return ASLServerSettings.charset.encode("dummy string");
 	}
 	
 }
