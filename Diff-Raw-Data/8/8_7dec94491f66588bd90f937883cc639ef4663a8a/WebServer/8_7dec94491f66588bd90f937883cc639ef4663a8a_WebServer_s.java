 package com.exilesoft.exercise;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.eclipse.jetty.plus.jndi.EnvEntry;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ShutdownHandler;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.h2.jdbcx.JdbcDataSource;
 import org.hibernate.cfg.Environment;
 
 
 public class WebServer {
 
 	public static void main(String[] args) throws Exception {
 		String shutdownToken = "sdgsdlgnslndglnsgdsgsdg";
 		int port = 10080;
 
 		shutdown(port, shutdownToken);
 
         System.setProperty(Environment.HBM2DDL_AUTO, "update");
 
         JdbcDataSource dataSource = new JdbcDataSource();
         dataSource.setURL("jdbc:h2:file:target/db/addressbook");
         dataSource.setUser("sa");
         new EnvEntry("java:/DefaultDS", dataSource);
 
         Server server = new Server(port);
 		HandlerList handlers = new HandlerList();
 		handlers.addHandler(new ShutdownHandler(server, shutdownToken));
 		handlers.addHandler(new WebAppContext("src/main/webapp", "/"));
 		server.setHandler(handlers);
 
 		server.start();
 	}
 
 	private static void shutdown(int port, String shutdownToken) throws IOException {
		URL url = new URL("http", "localhost", port, "/shutdown?shutdownToken=" + shutdownToken);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setRequestMethod("POST");
 		try {
 			connection.getResponseCode();
 		} catch (ConnectException e) {
 			// OK: Not running
 		}
 	}
 
 }
