 package com.jacobmorris.rccontroller;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class RCSocket {
 	String ip;
 	int port;
 	
 	Socket socket;
 	
 	public RCSocket(String ip, int port) throws UnknownHostException, IOException {
 		this.ip = ip;
 		this.port = port;
 		socket = new Socket(ip, port);
 		socket.setSendBufferSize(1024);
 		
 	}
 	public void sendValues(int leftSpeed, int rightSpeed, int leftDirection, int rightDirection) throws UnknownHostException, IOException {	
 		PrintStream output;
 		String message = Integer.toString(leftSpeed) + "," + Integer.toString(rightSpeed) + "," +
 				Integer.toString(leftDirection) + "," + Integer.toString(rightDirection);
 
 		try {
 			output = new PrintStream(socket.getOutputStream());
 			output.println(message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		   
 	}
 }
