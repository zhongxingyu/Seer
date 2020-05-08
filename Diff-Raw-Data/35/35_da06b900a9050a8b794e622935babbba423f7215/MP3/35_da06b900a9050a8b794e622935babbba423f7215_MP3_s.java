 package gui;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 
 import javazoom.jl.player.Player;
 
 public class MP3 {
     private String filename;
     private Player player; 
     private boolean isPlaying;
     private final  String audioPath [] = {"/sounds/Buzzer.mp3", "/sounds/Ding.mp3", "/sounds/lobbyMusic.mp3"};
     private BufferedInputStream bis= null;
     private InputStream fis= null;
     
     // constructor that takes the name of an MP3 file
     public MP3(int type){
     	try{
         fis = getClass().getResource(audioPath[type]).openStream();
         bis = new BufferedInputStream(fis);
         player = new Player(bis);
         isPlaying= false;
          }catch (Exception e) {
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
                			player.close();
                 			player.play();
                 		}
                 	}
                } catch (Exception e) {}
             }
         }.start();
     }
 }
