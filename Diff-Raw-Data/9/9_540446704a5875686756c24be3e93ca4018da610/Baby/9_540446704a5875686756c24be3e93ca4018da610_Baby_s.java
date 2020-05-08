 package com.aquasheep.GTFTSTG;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 
 public class Baby {
 	
 	private boolean sleeping = false;
 	private int sleepThreshold = 1000;
 	public Sound cry,laugh,sleep;
 	
 	public Baby() {
		cry = Gdx.audio.newSound(Gdx.files.internal("sounds/cry.ogg"));
 		laugh = Gdx.audio.newSound(Gdx.files.internal("sounds/laugh.ogg"));
 		sleep = Gdx.audio.newSound(Gdx.files.internal("sounds/sleep.ogg"));
 		cry.loop();
 	}
 	
 	public boolean attemptSleep(int amount) {
 		if (Math.random()*sleepThreshold+amount > sleepThreshold) {
 			sleeping = true;
 			cry.stop();
 			sleep.stop();
 			sleep.loop();
 		}
 		else {
 			sleeping = false;
 			sleep.stop();
 			cry.stop();
 			cry.loop();
 		}
 		return sleeping;
 	}
 	
 	public void update(float delta) {
 		if (sleeping) {
 			if (delta!=0)
 				if ((Math.random()*5000)>(4999)) {
 					sleeping = false;
 					sleep.stop();
 					//Just in case cry is playing
 					cry.stop();
 					cry.loop();
 				}
 		} else {
 			if (delta!=0)
 				//Very small chance to fall asleep naturally
 				if ((Math.random()*25000)>(24999)) {
 					sleeping = true;
 					cry.stop();
 					sleep.stop();
 					sleep.loop();
 				}
 		}
 	}
 	
 	public boolean isSleeping() {
 		return sleeping;
 	}
 	
 }
