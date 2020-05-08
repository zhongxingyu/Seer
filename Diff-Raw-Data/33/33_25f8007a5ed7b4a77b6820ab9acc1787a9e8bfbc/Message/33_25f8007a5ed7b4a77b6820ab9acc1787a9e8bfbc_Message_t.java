 package edu.berkeley.cs.cs162;
 
 public class Message {
 	private String timestamp;
 	private String source;
 	private String dest;
 	private String content;
 	
 	public Message(String timestamp, String source, String dest, String content) {
 		this.timestamp = timestamp;
 		this.source = source;
 		this.dest = dest;
 		this.content = content;
 	}
	
 	public String getTimestamp() {
 		return timestamp;
 	}
	
 	public String getSource() {
 		return source;
 	}
	
 	public String getDest() {
 		return dest;
 	}
	
 	public String getContent() {
 		return content;
 	}
 	
 }
