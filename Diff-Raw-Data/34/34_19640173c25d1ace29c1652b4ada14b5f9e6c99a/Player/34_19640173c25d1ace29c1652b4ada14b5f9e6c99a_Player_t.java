 package de.zigapeda.flowspring.player;
 
 import java.io.File;
 
import de.zigapeda.flowspring.Main;

 import javazoom.jlgui.basicplayer.BasicController;
 import javazoom.jlgui.basicplayer.BasicPlayer;
 import javazoom.jlgui.basicplayer.BasicPlayerException;
 import javazoom.jlgui.basicplayer.BasicPlayerListener;
 
 public class Player {
 	
 	private BasicPlayer player;
 	private BasicController control;
 
 	public Player() {
 		this.player = new BasicPlayer();
 		this.control = (BasicController) this.player;
 	}
 	
 	public void open(String filepath) {
 		try {
 			this.control.open(new File(filepath));
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void play(String filepath) {
 		try {
 			this.control.open(new File(filepath));
 			this.play();
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void play() {
 		try {
 			if(this.player.getStatus() == 1) {
 				this.control.resume();
 			} else {
 				this.control.play();
				Main.getWindow().setBalanceVolume();
 			}
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void pause() {
 		try {
 			this.control.pause();
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void stop() {
 		try {
 			this.control.stop();
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void seek(long bytes) {
         try {
             control.seek(bytes);
			Main.getWindow().setBalanceVolume();
         } catch (BasicPlayerException ioe) {}
 	}
 	
 	public void setGain(double gain) {
 		try {
 			this.player.setGain(gain);
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setPan(double pan) {
 		try {
 			this.player.setPan(pan);
 		} catch (BasicPlayerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getStatus() {
 		return this.player.getStatus();
 	}
 	
 	public void addListener(BasicPlayerListener listener) {
 		this.player.addBasicPlayerListener(listener);
 	}
 }
