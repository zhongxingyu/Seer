 package edu.american.weiss.lafayette.actions;
 
 import edu.american.weiss.lafayette.chamber.Audio;
 import edu.american.weiss.lafayette.composite.BaseCompositeAction;
 
 public class AudioAction extends BaseCompositeAction {
 	
	private String audioPath;
 	
 	public AudioAction(String audioPath) {
		this.audioPath = audioPath;
 	}
 
 	public boolean runAsThread() {
 		return false;
 	}
 
 	public void run() {
		new Audio(audioPath).start();
 	}
 
 }
