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
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		AudioPlayerSettings.stop();
 		System.exit(0);
 	}
 
 	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

 	}
 
 	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

 	}
 
 	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

 	}
 
 }
