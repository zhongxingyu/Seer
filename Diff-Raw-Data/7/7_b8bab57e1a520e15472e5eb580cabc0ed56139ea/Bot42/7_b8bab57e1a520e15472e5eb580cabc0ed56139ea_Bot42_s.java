 package com.bot42;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class Bot42 {
 	public static String nick = "Bot42";
 	public static String host = "irc.kottnet.net";
 	public static int port = 6667;
 	public static Socket ircSocket;
 	
 	public static PrintWriter ircWriter = null;
 	public static BufferedReader ircReader = null;
 	
 	public static void main(String[] args) {
 		try {
 			ircSocket = new Socket(host, port);
 			ircWriter = new PrintWriter(ircSocket.getOutputStream());
 			ircReader = new BufferedReader(new InputStreamReader(ircSocket.getInputStream()));
 			
 			while (true) {
 				String message = read();
 				String[] splitMessage = message.split(" ");
 				
 				if (splitMessage[0] == "PING") {
 					write("PONG " + splitMessage[1]);
 				}
 			}
 		} catch (UnknownHostException e) {
 			System.out.println("[ERR] Can't connect to host " + host + ":" + port);
 			return;
 		} catch (IOException e) {
 			System.out.println("[ERR] IO Exception " + e + " occured");
 			return;
 		}
 	}
 	
 	public static void write(String line) {
 		ircWriter.println(line);
 		System.out.println("[OUT] " + line);
 	}
 	
 	public static String read() throws IOException {
 		String line = "";
<<<<<<< HEAD
 		line = ircReader.readLine();
		System.out.println("[IN]  " + line);
=======
		// space before [IN] to make it line up a bit nicer?
 		System.out.println(" [IN] " + line);
>>>>>>> c952b849d3539dfa30239600479949396daea2cc
 		return line;
 	}
 }
