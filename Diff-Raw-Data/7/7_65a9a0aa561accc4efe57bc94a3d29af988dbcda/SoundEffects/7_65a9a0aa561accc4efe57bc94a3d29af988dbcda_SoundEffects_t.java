 package ch.bfh.bti7301.w2013.battleship.sounds;
 
import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 public class SoundEffects {
 	private static Effect sos = getClip("sounds/MorseSOS.wav");
 	private static Effect hit = getClip("sounds/MissileHitShort.wav");
 	private static Effect sunk = getClip("sounds/MissileHit.wav");
 	private static Effect scream = getClip("sounds/WilhelmScream.wav");
 
 	private SoundEffects() {
 		//
 	}
 
 	public static void playSOS() {
 		play(sos);
 	}
 
 	public static void playHit() {
 		play(hit);
 	}
 
 	public static void playSunk() {
 		play(sunk);
 	}
 
 	public static void playWilhelmScream() {
 		play(scream);
 	}
 
 	private static void play(Effect clip) {
 		if (clip != null) {
 			clip.play();
 		}
 	}
 
 	private static Effect getClip(String resource) {
 		try {
 			AudioInputStream ais = createReusableAudioInputStream(resource);
 			if (ais != null)
 				return new Effect(ais);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static AudioInputStream createReusableAudioInputStream(String res) {
 		AudioInputStream ais = null;
 		try {
			ais = AudioSystem.getAudioInputStream(new BufferedInputStream(
					SoundEffects.class.getClassLoader()
							.getResourceAsStream(res)));
 			byte[] buffer = new byte[1024 * 32];
 			int read = 0;
 			ByteArrayOutputStream baos = new ByteArrayOutputStream(
 					buffer.length);
 			while ((read = ais.read(buffer, 0, buffer.length)) != -1) {
 				baos.write(buffer, 0, read);
 			}
 			AudioInputStream reusableAis = new AudioInputStream(
 					new ByteArrayInputStream(baos.toByteArray()),
 					ais.getFormat(), AudioSystem.NOT_SPECIFIED);
 			return reusableAis;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			if (ais != null) {
 				try {
 					ais.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private static class Effect {
 		AudioInputStream ais;
 
 		private Effect(AudioInputStream ais) {
 			this.ais = ais;
 		}
 
 		private void play() {
 			try {
 				ais.reset();
 				Clip clip = AudioSystem.getClip();
 				clip.open(ais);
 				clip.start();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
