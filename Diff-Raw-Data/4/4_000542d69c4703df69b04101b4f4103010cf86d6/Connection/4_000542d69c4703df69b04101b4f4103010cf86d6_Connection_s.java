 package AP2DX;
 
 import java.net.*;
 import java.io.*;
 
 /*
 * @author Maarten Inja
  * A class that should handle all a socket connection to both send and receive messages. The receiving part might be difficult
  * since this could happen all the time, or not at all.
  */
 public class Connection {
 
 	private InetAddress address;
 	private Socket socket;
 
 	private PrintWriter out;
 	private BufferedReader in;
 	private Module module;
 
 	public Connection(String addressString, int port, Module module)
 			throws Exception {
 		this.module = module;
 		address = InetAddress.getByName(addressString);
 		socket = new Socket(address, port);
 		out = new PrintWriter(socket.getOutputStream(), true);
 		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 	}
 
 	/** Sends a message to whatever this thing is connected to. */
 	public boolean sendMessage(String message) {
 		try {
 			out.print(message);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public String readMessage() {
 		try {
 			return in.readLine();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/** Closes all connections. */
 	public boolean close() {
 		try {
 			out.close();
 			in.close();
 			socket.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public Module GetModule() {
 		return module;
 	}
 }
