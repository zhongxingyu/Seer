 package gui;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 
 import javazoom.jl.player.Player;
 
 public class MP3 {
	private int type;
     private String filename;
     private Player player; 
     private boolean isPlaying;
     private final  String audioPath [] = {"/sounds/Buzzer.mp3", "/sounds/Ding.mp3", "/sounds/lobbyMusic.mp3"};
     private BufferedInputStream bis= null;
     private InputStream fis= null;
     
     // constructor that takes the name of an MP3 file
     public MP3(int type){
     	try{
    		this.type = type;
    		loadSong();
    		isPlaying= false;
          }catch (Exception e) {
              System.out.println("Problem playing file " + filename);
          }
    }
    private void loadSong() {
    	 try {
			fis = getClass().getResource(audioPath[type]).openStream();
			bis = new BufferedInputStream(fis);
	        player = new Player(bis);
		} catch (Exception e) {
			System.out.println("Problem playing file " + filename);
		}
         
    }
 
     public void close() { if (player != null) player.close(); }
 
     // play the MP3 file to the sound card
     public void play() {
         // run in new thread to play in background
     	isPlaying = true;
         new Thread() {
             public void run() {
                 try { player.play(); player.close(); isPlaying = false;}
                 catch (Exception e) {}
             }
         }.start();
     }
     
     public boolean isPlaying(){ return isPlaying;}
     
     
     public void loopPlay() {
         // run in new thread to play in background
     	isPlaying = true;
         new Thread() {
             public void run() {
                 try {
                 	player.play();
                 	while(isPlaying){
                 		if(player.isComplete()){
                			loadSong();
                 			player.play();
                 		}
                		Thread.sleep(500);
                 	}
                } catch (Exception e) {System.err.println("Music loop exception thrown!"); }
             }
         }.start();
     }
 }
