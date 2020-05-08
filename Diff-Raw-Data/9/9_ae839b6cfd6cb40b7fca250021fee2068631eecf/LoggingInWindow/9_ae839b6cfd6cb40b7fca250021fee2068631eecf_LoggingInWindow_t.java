 package com.jpii.navalbattle.gui;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.*;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.Helper;
 
 @SuppressWarnings("serial")
 public class LoggingInWindow extends JFrame {
 	
 	private ImageChanger imageChanger;
 	
 	public LoggingInWindow() {
 		
 		NavalBattle.getDebugWindow().printInfo("LoginWindow opened");
 		
 		this.setIconImage(Helper.GUI_WINDOW_ICON);
 		
 		setTitle("NavalBattle - Logging In");
 		getContentPane().setLayout(null);
 		
 		JProgressBar progressBar = new JProgressBar();
 		progressBar.setIndeterminate(true);
 		progressBar.setBounds(0, 286, 475, 14);
 		getContentPane().add(progressBar);
 		
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/jpii_title.png")));
 		label.setBounds(0, 0, 475, 300);
 		getContentPane().add(label);
 		
 		this.setSize(491, 339);		
 		
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				NavalBattle.close();
 			}
 		});
 		
 		addMouseListener(new MouseAdapter() {
 			@SuppressWarnings("deprecation")
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Skipping splash screens");
 				NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 				dispose();
 				NavalBattle.getDebugWindow().printInfo("Opening MainMenuWindow");
 				new MainMenuWindow();
 				
 				imageChanger.stop();
 			}
 		});
 		
 		imageChanger = new ImageChanger(label);
 		imageChanger.start();
 		this.setVisible(true);
		this.setLocation(1280/2-getWidth()/2,800/2-getHeight()/2);
 	}
 	
 	class ImageChanger extends Thread {
 		
 		private JLabel label;
 		
 	    public ImageChanger(JLabel label) {
 	        super("ImageChanger");
 	        this.label = label;
 	    }
 	    
 	    public void run() {
 	        try {
 				Thread.sleep(Constants.SPLASH_DURATION);
 			} catch (InterruptedException e) { }
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/roketgamer_title.png")));
 	        
 	        try {
 				Thread.sleep(Constants.SPLASH_DURATION);
 			} catch (InterruptedException e) { }
 	        
 	        label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/navalbattle_title.png")));
 	        
 	        try {
 				Thread.sleep(Constants.SPLASH_DURATION);
 			} catch (InterruptedException e) { }
 	        
 	        NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 			dispose();
 			NavalBattle.getDebugWindow().printInfo("Opening MainMenuWindow");
 			new MainMenuWindow();
 	    }
 	}
 }
