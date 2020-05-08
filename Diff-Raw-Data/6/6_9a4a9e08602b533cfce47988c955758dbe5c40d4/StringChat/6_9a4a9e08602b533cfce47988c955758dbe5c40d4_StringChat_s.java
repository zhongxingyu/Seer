 package uk.ac.cam.db538.fjava.tick1;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class StringChat {
 	public static void main(String[] args) {
 		String server = null;
 		int port = 0;
 		
 		if (args.length != 2)
 			System.err.println("This application requires two arguments: <machine> <port>");
 		
 		server = args[0];
 		port = Integer.parseInt(args[1]);
 
 		/* 
 		 *  Socket is declared final because it is referenced inside
 		 *  of the Thread class
 		 */
 		try {
 			final Socket s = new Socket(server, port);
 			Thread output = new Thread() {
 				@Override
 				public void run() {
 					try {
 						BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
 						while (true)
 							System.out.println(reader.readLine());
 					} catch (IOException e) {
 					}
 				}
 			};
 			output.setDaemon(true);
 			output.start();
			
			PrintWriter w = new PrintWriter(s.getOutputStream(), true);
 			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
 			while (true)
				w.write(r.readLine());
 		} catch (NumberFormatException e) {
 		} catch (UnknownHostException e) {
 			System.err.println("Cannot connect to " + server + " on port " + port);
 		} catch (IOException e) {
 			System.err.println("Cannot connect to " + server + " on port " + port);
 		}
 	}
 }
