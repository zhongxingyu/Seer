 package pl.edu.agh.two.mud.server;
 
 import java.io.*;
 import java.net.*;
 
 import org.apache.log4j.*;
 
 import pl.edu.agh.two.mud.server.configuration.*;
 
 public class Server {
 	private static Logger logger = Logger.getLogger(Server.class);
 
 	public static void main(String[] args) throws IOException {
 		ApplicationContext.initialize();
 
 		ServerSocket serverSocket = null;
		int port = 6666;
 
 		try {
 			serverSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			logger.error("Cannot listen on port " + port);
 			System.exit(1);
 		}
 		logger.info("Server started, listening for clients...");
 		try {
 
 			while (true) {
 				try {
 					Service service = (Service) ApplicationContext.getBean("service");
 					service.bindSocket(serverSocket.accept());
 					service.start();
 				} catch (IOException e) {
 					logger.error("Socket error:" + e.getMessage());
 				}
 			}
 		} finally {
 			logger.info("Shutting down the server");
 			serverSocket.close();
 		}
 
 	}
 }
