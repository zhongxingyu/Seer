 package edu.mines.csci598.recycler.bettyCrocker;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * A SoundEffect is used to play a short sound clip typically short enough
  * to store completely in memory. A SoundEffect can be played at any time
  * by using the 'play' method. 
  * 
  * To create a SoundEffect, simply use the constructor that takes the
  * name of the sound file (using a relative path).
  * 
  * In addition, the volume and looping attributes
  * can be set by the user.
  * 
  * @author John
  *
  */
 public class SoundEffect {
 	
 	private File soundFile;
 	private Clip clip;
 	private AudioInputStream audioStream;
 	
 	/**
 	 * Creates a SoundEffect from the supplied file path.
 	 * @param fileName
 	 */
 	public SoundEffect(String fileName) {
 		soundFile = new File(fileName);
 	}
 	
 	/**
 	 * Plays the SoundEffect once.
 	 */
 	public void play() {
 		play(1);
 	}
 	
 	/**
 	 * Plays the sound effect a user defined
 	 * amount of times.
 	 * @param times
 	 */
 	public void play(int times) {
 		try {
 			audioStream = AudioSystem.getAudioInputStream(soundFile);
 			
 			clip = AudioSystem.getClip();
 			clip.open(audioStream);
 		
 			clip.addLineListener( new LineListener() {
 				@Override
 				public void update(LineEvent evt) {
 					if (evt.getType() == LineEvent.Type.STOP) {
 				    evt.getLine().close();
 				  }
 				}
 			});
 			
 			if (times != Clip.LOOP_CONTINUOUSLY) {
 				clip.loop(times - 1); //The Clip method for looping states 0 will play it once)
 			}	else {
 				clip.loop(Clip.LOOP_CONTINUOUSLY);
 			}
 		} catch (UnsupportedAudioFileException e) {
 			System.out.println("Unsupported audio file.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Problem reading the audio file.");
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			System.out.println("Dataline is not available.");
 			e.printStackTrace();
		} catch ( IllegalArgumentException e ){
            System.out.println();
            e.printStackTrace();
        }

 	}
 	
 	/**
 	 * Plays the SoundEffect indefinitely.
 	 */
 	public void loop() {
 		play(Clip.LOOP_CONTINUOUSLY);
 	}
 	
 	/**
 	 * Stops playing the SoundEffect
 	 */
 	public void stop() {
 		if (clip.isRunning()) {
 			clip.stop();
 		}
 	}
 	
 	/**
 	 * Returns the sound file associated with this
 	 * SoundEffect.
 	 * @return
 	 */
 	public File getSoundFile() {
 		return soundFile;
 	}
 	
 	/**
 	 * Sets the volume of the SoundEffect.
 	 * @param newVolume
 	 */
 	public void setVolume(float newVolume) {
 		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
 		volume.setValue(newVolume);
 	}
 	
 }
 
