 package com.jpii.navalbattle.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 
 import com.jpii.navalbattle.data.Constants;
 
 @SuppressWarnings("serial")
 public class LoggingInWindow extends Window {
 	
 	private Timer timer;
 	private int currentImage = 0;
 	private int length = 0;
 	
 	public LoggingInWindow() {
 		setUndecorated(true);
 		getContentPane().setLayout(null);
 		
 		JProgressBar progressBar = new JProgressBar();
 		final JLabel label = new JLabel("");
 		
 		progressBar.setBounds(0, 326, 492, 14);
 		label.setBounds(0, 0, 492, 340);
 		
 		progressBar.setIndeterminate(true);
 		
 		getContentPane().add(progressBar);
 		getContentPane().add(label);
 		
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				length += Constants.SPLASH_DURATION;
 				if (currentImage == 0) {
					label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/roketgamer_title.png")));
 				}
 				else if (currentImage == 1) {
					label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/navalbattle_title.png")));
 				}
 				else {
					label.setIcon(new ImageIcon(LoggingInWindow.class.getResource("/com/jpii/navalbattle/res/jpii_title.png")));
 				}
 				if (currentImage == 2)
 					currentImage = 0;
 				else
 					currentImage++;
 				if (length > Constants.SPLASH_SCREEN_TIMEOUT)
 					openMenu();
 			}
 		};
 		timer = new Timer(Constants.SPLASH_DURATION,al);
 		timer.setInitialDelay(0);
 		timer.start();
 		
 		label.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent me) {
 				openMenu();
 			}
 		});
 		if(!isVisible())
 			timer.stop();
 	}
 	
 	boolean alreadyOpened = false;
 	
 	public void openMenu() {
 		if (!alreadyOpened) {
 			alreadyOpened = true;
 			nextWindow("MainMenuWindow");
 		}
 	}
 
 public void setVisible(boolean visible){
 	super.setVisible(visible);
 	if(isVisible()){
 		alreadyOpened = false;
 		timer.start();
 	}
 	else{
 		if(timer!=null)
 			timer.stop();
 		length = 0;
 	}
 }
 	
 	
 	
 }
