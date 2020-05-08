 /**
  * 
  */
 package com.aqua.music.view.main;
 
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import open.music.api.AudioPlayerSettings;
 
 /**
  * @author "Shruti Tiwari"
 * 
  */
 public class AppCloseWindowsListern implements WindowListener {
 
 	@Override
	public void windowActivated(WindowEvent e) {
	}
 
	@Override
	public void windowClosed(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		AudioPlayerSettings.stop();
 		System.exit(0);
 	}
 
 	@Override
	public void windowDeactivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
	public void windowIconified(WindowEvent e) {
 	}
 
 	@Override
	public void windowOpened(WindowEvent e) {
 	}
 
 }
