 package edu.american.weiss.lafayette.actions;
 
 import edu.american.weiss.lafayette.chamber.Audio;
 import edu.american.weiss.lafayette.composite.BaseCompositeAction;
 
 public class AudioAction extends BaseCompositeAction {
 	
	private Audio aud;
 	
 	public AudioAction(String audioPath) {
		aud = new Audio(audioPath);
 	}
 
 	public boolean runAsThread() {
 		return false;
 	}
 
 	public void run() {
		aud.start();
 	}
 
 }
