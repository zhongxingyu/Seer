 package edu.berkeley.cs.cs162;
 
 public class MessageJob {
 	String dest,msg;
	int sqn;
	public MessageJob(String dest, String msg, int sqn){
 		this.dest = dest;
 		this.msg = msg;
		this.sqn = sqn;
 	}
 }
