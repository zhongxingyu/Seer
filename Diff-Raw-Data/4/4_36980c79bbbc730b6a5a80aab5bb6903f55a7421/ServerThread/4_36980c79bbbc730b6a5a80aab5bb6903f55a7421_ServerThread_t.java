 import java.net.*;
 import java.io.*;
 
 public class ServerThread extends Thread {
 	Socket s = null;
 	public ServerThread(Socket socket) {
 		super("ServerThread");
 		s = socket;
 	}
 	public void run() {
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			while (s.isConnected() && !s.isClosed()) {
				String line = in.readLine();
				if (line.equals("exit")) break;
				System.out.println(line);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Done!");
 	}
 }
