 package ru.allgage.geofriend;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class TaskSocket {
 	public Socket socket;
 	public DataInputStream in;
 	public DataOutputStream out;
 	public static String log;
 	public static String pass;
 	
 	public TaskSocket() {
 		try {
			socket = new Socket("192.168.1.104", 7777);
 			socket.setKeepAlive(true);
 			in = new DataInputStream(socket.getInputStream());
 			out = new DataOutputStream(socket.getOutputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void writeAuth() {
 		try {
 			writeMessages(log, pass);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes one or many strings to the stream, flushing it afterwards.
 	 *
 	 * @param messages messages to write.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	public void writeMessages(String... messages) throws IOException {
 		for (String message : messages) {
 			out.writeUTF(message);
 		}
 
 		out.flush();
 	}
 
 
 	/**
 	 * Writes the status message to the data stream.
 	 *
 	 * @param userName  user name of the status owner.
 	 * @param latitude  latitude of the message.
 	 * @param longitude longtitude of the message.
 	 * @param message   the message text.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	public void writeStatus(
 			String userName,
 			double latitude,
 			double longitude,
 			String message) throws IOException {
 		// TODO: Proper format double.
 		String datagram = String.format("%s:%d:%d:%s", userName, latitude, longitude, message);
 		writeMessages(datagram);
 	}
 	
 	public void close() {
 		try {
 			in.close();
 			out.close();
 			socket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
