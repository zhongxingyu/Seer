 package sounds;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.util.*;
 import javazoom.jl.player.advanced.AdvancedPlayer;
 import javazoom.jl.decoder.JavaLayerException;
 
 
 public class SoundLib {
   private Hashtable<String, String> sounds;
   private String nextSound;
   private AdvancedPlayer audio;
   private boolean isLoop;
   
   public SoundLib(){
     sounds = new Hashtable<String, String>();
   }
 
 //_________________________________________________________________________________________________________________________  
   public void setNextSound(String name){
 	  nextSound=name;  
   }
   
 //_________________________________________________________________________________________________________________________
   public void loadSound(String name, String path){
 
   if(sounds.containsKey(name)){
     return;
   }
     sounds.put(name,path);
   }
   
 //_________________________________________________________________________________________________________________________  
   public void playSingleSound(String name){
 	  if(audio!=null)
 		  stopLoopingSound();
 	  playSound(name);
   }
   
 //_________________________________________________________________________________________________________________________  
   private void playSound(String name){
   	try {
   		nextSound=name;
		audio = new AdvancedPlayer(new FileInputStream(getClass().getClassLoader().getResource(sounds.get(nextSound)).getFile()));
	} catch (FileNotFoundException | JavaLayerException e) {
 		e.printStackTrace();
 	}
 	  
 	  Thread th = new PlayerThread();
 	  th.start();
 
   }
   
 //__________________________________________________________________________________________________________________________
   
   public void stop(){
 		  audio.close();
 		  audio=null;
 
 	  
   }
     
 //_________________________________________________________________________________________________________________________  
   public void loopSound(String name){
 	  playSound(name);
 	  isLoop=true;
   }
 
 //_________________________________________________________________________________________________________________________  
   public void stopLoopingSound(){
 	  isLoop=false;
 	  stop();
   }
   
   public class PlayerThread extends Thread {
 	  
 	  public PlayerThread(){
 		  this.setDaemon(true);
 	  }
 		
 		@Override public void run(){
 	    	try {
 	    		audio.play();
 	    		if(isLoop){	    				
 	    				playSound(nextSound);
 	    		}
 	    	}catch (JavaLayerException e) {
 	    		e.printStackTrace();
 				
 	    	}
 			
 		}
 				
 	};
   
 }
