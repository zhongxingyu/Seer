 package com.dropoutdesign.ddf;
 
 import com.dropoutdesign.ddf.client.*;
 import com.dropoutdesign.ddf.config.*;
 import com.dropoutdesign.ddf.test.*;
 import com.dropoutdesign.ddf.render.*;
 
 import java.io.IOException;
 import javax.imageio.*;
 
 /**
  * Main class for interactive use.
  */
 public class Main {
 	
 	/**
 	 * Process command line arguments, starting a server, client, or renderer.
 	 */
 	public static void main(String args[]) {
 		boolean serve = false;
 		boolean render = false;
 		String floorAddr = null;
 		String renderer = null;
 		
 		int i = 0;
 		while (i < args.length) {
 			String s = args[i++];
 		
 			if (s.equals("-server")) {
 				serve = true;
 		
 			} else if (s.equals("-render")) {
 				render = true;
 				if (i < args.length) {
 					renderer = args[i++];
 				} else {
 					System.err.println("-render requires a renderer to be specified.");
 					System.err.println("Built-in renderers are:");
 					System.err.println("\tGameOfLife");
 					System.exit(1);
 				}
 			
 			} else if (s.equals("-f")) {
 				if (i < args.length) {
 					floorAddr = args[i++];
 				} else {
					System.err.println("-f requires a floor address to be specified.");
					System.err.println("Valid floor types are:");
					System.err.println("\tlocal:<configFile>");
					System.err.println("\tremote:<remoteAddr>");
					System.err.println("\tvirtual:");
 				}
 			
 			} else {
 				String e = "Invalid option: " + s + "\n";
 				e += "Usage: DDFv2.0 [-server | -render <renderer>] -f <floor>";
 				System.err.println(e);
 				System.exit(1);
 			}
 		}
 		
 		DanceFloor floor = null;
 		if (serve || render) {
 			try {
 				floor = DanceFloor.connectFloor(floorAddr);
 			} catch (Exception e) {
 				System.err.println("Unable to initialize floor: " + e);
 				System.err.println("Valid floor types are:");
 				System.err.println("\tlocal:<configFile>");
 				System.err.println("\tremote:<remoteAddr>");
 				System.err.println("\tvirtual:");
 				System.exit(1);
 			}
 		}
 		
 		
 		if (serve) {
 			try {
 				DDFServer server = new DDFServer(floor);
 				server.start();
 			} catch (Exception e) {
 				System.out.println("Unable to start server: " + e);
 				e.printStackTrace();
 			}
 		
 		} else if (render) {
 			Renderer r = null;
 			if (renderer.equalsIgnoreCase("GameOfLife")) {
 				r = new GameOfLife();
 			}
 			
 			if (r == null) {
 				System.out.println("Unrecognized renderer: " + renderer);
 				System.exit(1);
 			}
 			
 			try {
 				long frametime = 1000 / floor.getFramerate();
 				while (true) {
 					long t1 = System.currentTimeMillis();
 					floor.drawFrame(r.drawFrame());
 					long t2 = System.currentTimeMillis();
 					long d = frametime - (t2 - t1);
 					if (d > 0)
 						Thread.sleep(d);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		
 		} else {
 			ClientGUI gui = new ClientGUI("dancefloor.mit.edu");
 		}
 	}
 }
