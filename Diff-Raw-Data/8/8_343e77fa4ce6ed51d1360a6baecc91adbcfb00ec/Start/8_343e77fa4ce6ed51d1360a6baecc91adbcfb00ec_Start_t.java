 package org.vfny.geoserver.jetty;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 /**
  * Jetty starter, will run geoserver inside the Jetty web container.<br>
  * Useful for debugging, especially in IDE were you have direct dependencies
  * between the sources of the various modules (such as Eclipse).
  * 
  * @author wolf
  * 
  */
 public class Start {
 	private static final Logger log = Logger.getLogger(Start.class.getName());
 
 	public static void main(String[] args) {
 		Server jettyServer = null;
 		try {
 			jettyServer = new Server();
 
 			SocketConnector conn = new SocketConnector();
 			conn.setPort(8080);
 			jettyServer.setConnectors(new Connector[] { conn });
 
 			WebAppContext wah = new WebAppContext();
 			wah.setContextPath("/geoserver");
 			wah.setWar("src/main/webapp");
 			jettyServer.setHandler(wah);
                         wah.setTempDirectory(new File("target/work"));
 
 			jettyServer.start();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Could not start the Jetty server: "
 					+ e.getMessage(), e);
 			if (jettyServer != null) {
 				try {
 					jettyServer.stop();
 				} catch (Exception e1) {
 					log.log(Level.SEVERE, "Unable to stop the "
 							+ "Jetty server:" + e1.getMessage(), e1);
 				}
 			}
 		}
 	}
 }
