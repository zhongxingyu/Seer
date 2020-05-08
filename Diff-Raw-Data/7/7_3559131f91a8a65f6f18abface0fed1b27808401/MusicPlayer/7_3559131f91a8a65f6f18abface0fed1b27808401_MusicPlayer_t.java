 package com.calumgilchrist.ld23.tinyworld.core;
 
 import java.util.Random;
 import java.util.ArrayList;
 
 import static playn.core.PlayN.*;
 import playn.core.Sound;
 
 public class MusicPlayer {
 	private ArrayList<Sound> tracks;
 	private int currentTrack;
 	Random r;
 	
 	public MusicPlayer(){
 		tracks = new ArrayList<Sound>();
 		r = new Random(tracks.size());
 		currentTrack = -1;
 	}
 	
 	public void add(String path){
 		Sound newTrack = assets().getSound(path);
 		tracks.add(newTrack);
 		r = new Random(tracks.size());
 	}
 	
 	public void start(){
		currentTrack = r.nextInt(tracks.size());
 	}
 	
 	public void update(){		
 		if(currentTrack != -1){
 			if(!tracks.get(currentTrack).isPlaying()){
				int i = r.nextInt(tracks.size());
 				currentTrack = i;
				tracks.get(currentTrack).play();
 			}	
 		}
 	}
 }
