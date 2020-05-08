 package com.jpii.navalbattle.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JProgressBar;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 
 public class LoggingInWindow extends JFrame {
 	public LoggingInWindow() {
 		setTitle("NavalBattle - Logging In");
 		getContentPane().setLayout(null);
 		
 		JProgressBar progressBar = new JProgressBar();
 		progressBar.setIndeterminate(true);
 		progressBar.setBounds(0, 177, 350, 14);
 		getContentPane().add(progressBar);
 		
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/splash_jpii_sm.png")));
 		label.setBounds(0, 0, 350, 191);
 		getContentPane().add(label);
 		
 		this.setSize(366,229);
 		this.setVisible(true);
 		
 		new ImageChanger(label).start();
 	}
 	
 	class ImageChanger extends Thread {
 		
 		private JLabel label;
 		
 	    public ImageChanger(JLabel label) {
 	        super("ImageChanger");
 	        this.label = label;
 	    }
 	    
 	    public void run() {
 	        try {
				this.sleep(2000);
 			} catch (InterruptedException e) { }
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/splash_roketgamer_sm.png")));
 	        
 	        try {
				this.sleep(2000);
 			} catch (InterruptedException e) { }
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/splash_navalbattle_sm.png")));
 	    }
 	}
 }
