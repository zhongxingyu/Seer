 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 
 public class GameSound {
 	private static ClassLoader cl = GameSound.class.getClassLoader();
 	private String soundfilepath;
 	private final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);
 	
 	public GameSound(String soundfilepath) {
 		// TODO: Error handling. File not being present.
 		this.soundfilepath = "edu/bu/cs673/AwesomeAlphabet/resources/" + soundfilepath;
 	}
 	
 	public void PlaySound() {
 		try {
 			InputStream is = cl.getResourceAsStream(soundfilepath);
 			AudioInputStream audioIn = AudioSystem.getAudioInputStream(is);
 			Clip clip = AudioSystem.getClip();
 			clip.open(audioIn);
 			clip.start();
 			LineListener listener = new LineListener() {
 				public void update(LineEvent event) {
 					if (event.getType() != LineEvent.Type.STOP) {
 						return;
 					}
 
 					try {
 						queue.put("dummy");
 						return;
 					}
 					catch (InterruptedException e) {
 						//ignore this
 					}
 				}
 			};
 
 			clip.addLineListener(listener);
 			try {
 				queue.take();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
