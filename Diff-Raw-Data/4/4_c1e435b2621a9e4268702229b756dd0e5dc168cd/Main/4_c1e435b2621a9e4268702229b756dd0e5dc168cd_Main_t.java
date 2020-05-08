 package com.angelini.flyTest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.angelini.fly.Fly;
 import com.angelini.fly.FlyDB;
 
 public class Main {
 	
 	public static final int PORT = 8090;
 	
 	private static String CONNECTION = "jdbc:mysql://localhost/Fly";
 	private static final String USER = "alex";
 	private static final String PASS = "alex";
 	
 	private static Logger log = LoggerFactory.getLogger(Main.class);
 	
 	public static void main(String[] args) {
 		try {
 			FlyDB db = new FlyDB(CONNECTION, USER, PASS);
			Fly server = new Fly(PORT, db, "/htdocs");
 			
			server.addComponent("search", "/components/search.html");			
 			server.addServlet(ProductRoutes.class, "/products/*");
 			
 			server.requireAuth(Auth.class);
 			
 			server.start();
 			
 		} catch (Exception e) {
 			log.error("Server startup error", e);
 		}
 	}
 
 }
