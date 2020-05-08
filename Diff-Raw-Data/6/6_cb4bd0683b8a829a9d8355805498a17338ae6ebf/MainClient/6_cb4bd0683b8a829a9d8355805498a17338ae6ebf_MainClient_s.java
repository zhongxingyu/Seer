 package jmbs.client;
 
 import java.awt.EventQueue;
 
 import jmbs.client.Graphics.ConnectionFrame;
 import jmbs.client.Graphics.MainWindow;
import jmbs.common.User;
 
 public class MainClient {
 
 	/**
 	 * Tha main window
 	 */
 	private static MainWindow window;
 	/**
 	 * The Connection Window
 	 */
 	private static ConnectionFrame cf;
	private static User currentUser;
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
					// In the next time we'ill create this user after connection
					currentUser = new User("Younes", "CHEIKH", "younes.cheikh@gmail.com", 3);
 					window = new MainWindow();
 					/*
 					 * We start by displaying the connection Frame
 					 * when user enter a right identity, the main window will be shown automaticly
 					 */
 					cf = new ConnectionFrame(window);
 					cf.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 
 }
