 package no.f12.jzx.weboo.server;
 
 import java.io.File;
 
 
 public class Server {
 
 	public static void main(String[] args) throws Exception {
 		int port = determineServerPort();
 		File contextPath = determineContextPath();
 
 		WebServer setupServer = new WebServer(port);
 		setupServer.start(contextPath, "");
 	}
 
 	private static File determineContextPath() {
 		File contextPath = new File("./src/main/webapp");
		File herokuPath = new File("./weboo-webapp/src/main/webapp");
		if (herokuPath.exists()) {
			return herokuPath;
 		}
 		return contextPath;
 	}
 
 	private static int determineServerPort() {
 		int port = 8080;
 		String systemPort = System.getenv("PORT");
 		if (systemPort != null) {
 			port = Integer.valueOf(systemPort);
 		}
 		return port;
 	}
 }
