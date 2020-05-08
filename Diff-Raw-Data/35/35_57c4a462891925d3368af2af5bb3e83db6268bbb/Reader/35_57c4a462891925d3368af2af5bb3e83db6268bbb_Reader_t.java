 package com.github.dtf.rpc.server;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 import com.github.dtf.io.nio.MessageHandler;
 import com.github.dtf.rpc.Call;
 
 public class Reader implements MessageHandler {
 	Server server;
 
 	public Reader(Server svc) {
 		server = svc;
 	}
 
 	public void processMessage(SocketChannel channel, ByteBuffer message) {
 		System.out.println("Server get request:" + System.currentTimeMillis());
 		System.out.println("Reader!");
 		int length = message.getInt();
 		byte[] data = new byte[length];
 		message.get(data, 0, length);
 		Call newCall = new Call();
 		newCall.setClientChannel(channel);
 		newCall.setRequestBuffer(data);
 		server.getRequestQueue().add(newCall);
 		System.out.println("Reader put the data to server's buffer:"
 				+ newCall.toString());
 		System.out.println("Reader Done!");
 	}
 
 }
