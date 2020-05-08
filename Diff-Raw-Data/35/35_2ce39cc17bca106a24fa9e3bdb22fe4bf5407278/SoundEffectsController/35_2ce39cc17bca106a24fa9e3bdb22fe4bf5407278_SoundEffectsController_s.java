 package se.chalmers.tda367.group15.game.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class SoundEffectsController {
 
 	private static SoundEffectsController instance;
 
 	private List<Sound> sounds = new ArrayList<Sound>();
 
 	private Music music;
 	private Music queuedMusic;
 
 	private boolean queued = false;
 
 	public enum SoundEffect {
 		ENEMY_DEATH(new String[] { "res/sound/enemy/ondamage/die.aif" }), ENEMY_HURT(
 				new String[] { "res/sound/enemy/ondamage/damage1.aif",
 						"res/sound/enemy/ondamage/damage2.aif",
 						"res/sound/enemy/ondamage/damage3.aif" }), PISTOL_FIRED(
 				new String[] { "res/sound/pistol/shoot.aif" }), ENEMY_COLLISION(
 				new String[] { "res/sound/enemy/collision.aif" });
 
 		private String[] pathsToFiles;
 
 		private SoundEffect(String[] pathsToFiles) {
 			this.pathsToFiles = pathsToFiles;
 		}
 	}
 
 	public enum GameMusic {
 		NORMAL_MUSIC(new String[] { "res/music/normal.aif" }), BOSS_MUSIC(
 				new String[] { "res/music/boss.aif" });
 
 		private String[] pathsToFiles;
 
 		private GameMusic(String[] pathsToFiles) {
 			this.pathsToFiles = pathsToFiles;
 		}
 	}
 
 	private SoundEffectsController() {
 	}
 
 	public static SoundEffectsController instance() {
 		if (instance == null) {
 			instance = new SoundEffectsController();
 		}
 		return instance;
 	}
 
 	public void playSound(SoundEffect soundEffect) {
 		Sound sound;
 		int maxRandomNbr = soundEffect.pathsToFiles.length;
 		Random generator = new Random();
 
 		try {
 			sound = new Sound(
 					soundEffect.pathsToFiles[generator.nextInt(maxRandomNbr)]);
 			sound.play();
 			sounds.add(sound);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void playGameMusic(GameMusic gameMusic) {
 
 		try {
 			if (music != null) {
 				if (music.playing()) {
 					queuedMusic = new Music(gameMusic.pathsToFiles[0]);
 					queued = true;
 				}
 			} else {
 				music = new Music(gameMusic.pathsToFiles[0]);
 				music.loop();
 			}
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
	public void fadeGameMusic() {
 		if (music != null) {
 			music.fade(1500, 0.0f, true);
 		}
 	}
 
 	public void stopAll() {
 		if (music != null) {
 			music.release();
 		}
 		if (queuedMusic != null) {
 			queuedMusic.release();
 		}
 		for (Sound s : sounds) {
 			s.release();
 		}
 	}
 
 	public void update() {
 		// If a piece of music has been queued
		if (queued && music.getVolume() < 0.2f) {
 			music.setVolume(0.0f);
 			queuedMusic.setVolume(0.0f);
 			queuedMusic.fade(3000, 0.7f, false);
			queuedMusic.loop();
 			queued = false;
 
 		}
 	}
 	
 	public void resetAll() {
		music = null;
		queuedMusic = null;
 	}
 }
