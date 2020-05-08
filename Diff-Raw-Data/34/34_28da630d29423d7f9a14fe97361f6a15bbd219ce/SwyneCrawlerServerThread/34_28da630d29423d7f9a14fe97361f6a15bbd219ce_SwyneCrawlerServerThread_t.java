 package edu.iit.swyne.crawler.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
import edu.iit.swyne.crawler.server.SwyneCrawlerServerProtocol.ClientExitException;

 public class SwyneCrawlerServerThread implements RequestHandler {
 
 	private SwyneCrawlerServer server;
 
 	public SwyneCrawlerServerThread(SwyneCrawlerServer server) {
 		this.server = server;
 	}
 
 	public void handleRequest(Socket socket) {
 		BufferedReader in;
 		PrintWriter out;
 		String line, header = SwyneCrawlerServer.NAME+" "+SwyneCrawlerServer.VERSION;
 		
 		try {
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			out = new PrintWriter(socket.getOutputStream(), true);
 			
 			out.println(header);
 			
			
			boolean exited = false;
			while ((line = in.readLine()) != null && !exited)
				try {
					out.println(SwyneCrawlerServerProtocol.run(line, this.server));
				} catch (ClientExitException e) {
					out.println("Goodbye");
					exited = true;
				}
				
 			socket.shutdownInput();
 			socket.shutdownOutput();
 			
 			out.close();
 			in.close();
 			socket.close();
 			
 		} catch (IOException e) {
 			System.err.println("ERROR: Problem connecting with client");
 			e.printStackTrace();
 		}
 	}
 }
