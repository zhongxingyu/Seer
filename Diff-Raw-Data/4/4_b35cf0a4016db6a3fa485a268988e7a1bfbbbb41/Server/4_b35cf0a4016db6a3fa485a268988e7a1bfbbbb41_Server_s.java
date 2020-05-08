 package com.socgen.maven.proxy;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
import com.socgen.maven.proxy.utils.CookieReader;
 import com.socgen.maven.proxy.utils.FirefoxCookieWatcher;
 import com.socgen.maven.proxy.utils.Watcher;
 
 public class Server {
 	public static final int SERVER_PORT = 2511;
 	private static final Log LOGGER = LogFactory.getLog(Server.class);
 	private transient ServerSocket socketServer;
 	private transient Watcher watcher;
 	
 //	public static final String EQUAL = "=";
 //	public static final String HTTP_PROXY_HOST = "http.proxyHost";
 //	public static final String HTTP_PROXY_PORT = "http.proxyPort";
 //	public static final String HTTP_PROXY_USER = "http.proxyUser";
 //	public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
 	public Server() {
		CookieReader.readCookies();
 //		watcher = new Watcher(new CookieTimeTask());
 		watcher = new Watcher(new FirefoxCookieWatcher());
 		watcher.start();
 		
 		try {
 			socketServer = new ServerSocket(SERVER_PORT);
 			LOGGER.info("Maven Server Proxy Started !");
 			startListen();
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage());
 		}
 	}
 	
 	private void startListen() throws IOException {
 		while(true){
 //			new Thread(new ServerSocketRequest(socketServer.accept())).start();
 			new Thread(new DownloaderProcessBar(socketServer.accept(), false)).start();
 		}
 	}
 
 	public void stop(){
 		try {
 			socketServer.close();
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage());
 		}
 	}
 	public static void main(final String[] args) {
 //		if (args.length > 0){
 //			for (String arg : args){
 //				final StringTokenizer stringTokenizer = new StringTokenizer(arg, EQUAL);
 //				stringTokenizer.nextToken();
 //				
 //				if (arg.startsWith(HTTP_PROXY_HOST)){
 //					System.setProperty(HTTP_PROXY_HOST, stringTokenizer.nextToken());
 //				}
 //				if (arg.startsWith(HTTP_PROXY_PORT)){
 //					System.setProperty(HTTP_PROXY_PORT, stringTokenizer.nextToken());
 //				}
 //				if (arg.startsWith(HTTP_PROXY_USER)){
 //					System.setProperty(HTTP_PROXY_PASSWORD, stringTokenizer.nextToken());
 //				}
 //				if (arg.startsWith(HTTP_PROXY_PASSWORD)){
 //					System.setProperty(HTTP_PROXY_PASSWORD, stringTokenizer.nextToken());
 //				}
 //			}
 //		}
 		new Server();
 	}
 }
