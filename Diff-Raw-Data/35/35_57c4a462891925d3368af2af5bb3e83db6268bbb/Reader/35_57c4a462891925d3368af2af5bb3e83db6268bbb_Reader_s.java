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
		// System.out.println("Server start read data:" +
		// System.currentTimeMillis());
		// channel.read(buffer);
		// System.out.println("Server read data end:" +
		// System.currentTimeMillis());
		// buffer.position(0);
 		int length = message.getInt();
 		byte[] data = new byte[length];
 		message.get(data, 0, length);
		// if(length == 0 || data == null || data.length <= 0){
		// System.out.println("Reader : request data length:" + length);
		// System.out.println("Reader : request data is empty, return.");
		// return;
		// }
 		Call newCall = new Call();
 		newCall.setClientChannel(channel);
 		newCall.setRequestBuffer(data);
 		server.getRequestQueue().add(newCall);
 		System.out.println("Reader put the data to server's buffer:"
 				+ newCall.toString());
 		System.out.println("Reader Done!");
 	}
 
 }
