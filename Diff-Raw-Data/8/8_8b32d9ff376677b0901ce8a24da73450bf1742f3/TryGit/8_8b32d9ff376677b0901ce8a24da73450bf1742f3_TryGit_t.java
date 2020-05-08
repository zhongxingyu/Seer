 /**
  * (c) me & GPL3
  */
 package com.puzzleduck.trygit;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 /**
  * @author puzzleduck
  *
  */
 public class TryGit {
 
 	protected static JFrame mainWindow;
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		initUI();
 		
 		System.out.println("OK");
 
 	}
 
 	
 	
 	
 	
 	private static void initUI()
 	{
 
 		//init l&f
 		try {
 			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");// or "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" or "com.sun.java.swing.plaf.motif.MotifLookAndFeel" or "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
 		} catch (Exception e) {
 			e.printStackTrace();// doing nothing... auto fallback to system default
 		}
 		
 		//main frame
 		mainWindow = new JFrame();
 		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		int windowHeight = 800;
 		int windowWidth = 800;
 		mainWindow.setBounds(0, 0, windowWidth, windowHeight);
 		mainWindow.setTitle("Try Git?");
 		
 		//main panel
 		JPanel mainPanel = new JPanel();
		mainWindow.getContentPane().add(mainPanel);

		JLabel versionLabel = new JLabel("v0.1.0");
 		mainPanel.add(versionLabel);
		JLabel versionTagline = new JLabel("Try Git has evolved into a monster");
		mainPanel.add(versionTagline);
 		
 		//...
 		
 		mainWindow.setVisible(true);
 		
 	}//init UI
 	
 	
 	
 	
 }
