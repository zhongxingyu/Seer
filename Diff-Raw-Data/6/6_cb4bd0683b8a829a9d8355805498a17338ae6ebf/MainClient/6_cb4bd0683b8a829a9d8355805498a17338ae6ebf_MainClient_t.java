 package jmbs.client;
 
 import java.awt.EventQueue;
 
 import jmbs.client.Graphics.ConnectionFrame;
 import jmbs.client.Graphics.MainWindow;
 
 public class MainClient {
 
 	/**
 	 * Tha main window
 	 */
 	private static MainWindow window;
 	/**
 	 * The Connection Window
 	 */
 	private static ConnectionFrame cf;
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
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
