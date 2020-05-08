 package net.codjo.test.release.util.ssh;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import org.apache.log4j.Logger;
 
 import com.googlecode.junit.ext.checkers.Checker;
 
 public class HostChecker implements Checker {
 	private static final Logger LOG = Logger.getLogger(HostChecker.class);
 	private final String host;
 	private final int port;
 
 	public HostChecker(String... arguments) {
 		host = arguments[0];
 		port = Integer.parseInt(arguments[1]);
 	}
 
 	public boolean satisfy() {
 		boolean result = false;
 		java.net.Socket socket = null;
 
 		try {
 			socket = new java.net.Socket(host, port);
 		} catch (UnknownHostException e) {
 			result = false;
 		} catch (IOException e) {
 			result = false;
 		} finally {
 			if ((socket != null) && !socket.isClosed()) {
 				try {
 					socket.close();
 				} catch (IOException ioe) {
 					LOG.error(ioe);
 				}
 			}
 		}
 
 		return result;
 	}
 }
