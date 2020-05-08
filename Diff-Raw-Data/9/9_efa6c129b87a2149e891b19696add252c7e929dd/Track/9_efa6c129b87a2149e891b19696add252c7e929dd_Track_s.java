package edu.mines.csci598.recycler.bettyCrocker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.BooleanControl;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * A Track is responsible for playing the audio from a single
  * mp3 file. It is created by using the constructor which accepts
  * the name of the mp3 file.
  * 
  * Typically, this class is not referenced directly other than
  * creating the track, and setting the controls for the Track which
  * include gain, muting, and a 32 channel equalizer.
  * 
  * Normally a Song is created and is responsible for playing any Tracks
  * that might have been created.
  * 
  * @author John
  *
  */
 public class Track {
 	
 	private File soundFile;
 	private AudioInputStream audioInputStream;
 	private AudioInputStream decodedAudioInputStream;
 	private AudioFormat audioFormat;
 	private AudioFormat decodedAudioFormat;
 	private SourceDataLine sourceDataLine;
 	
 	private CountDownLatch startLatch; //Used by the Song class to be started with the other Tracks
 	
 	private FloatControl gainControl;
 	private BooleanControl muteControl;
 	
 	private float gain = 0.0f;
 	private boolean mute = false;
 	private float[] equalizer = new float[32];
 	
 	private boolean isPlaying = false;
 	
 	/**
 	 * Creates a Track from the supplied file path.
 	 * @param fileName
 	 */
 	public Track(String fileName) {
 		soundFile = new File(fileName);
 		System.out.println("absolute path: " + soundFile.getAbsolutePath());
 	}
 	
 	/**
 	 * Starts playing the Track.
 	 */
 	public void startPlaying() {
 		if (!isPlaying) {
 			isPlaying = true;
 			new PlayThread().start();
 		}
 	}
 	
 	/**
 	 * Stops playing the Track.
 	 */
 	public void stopPlaying() {
 		isPlaying = false;
 	}
 
 	/**
 	 * Returns whether the Track is currently playing.
 	 * @return
 	 */
 	public boolean isPlaying() {
 		return isPlaying;
 	}
 	
 	/**
 	 * Returns the sound file associated with this Track.
 	 * @return
 	 */
 	public File getSoundFile() {
 		return soundFile;
 	}
 	
 	/**
 	 * Sets the start latch for synchronizing the Tracks with each
 	 * other. This does not typically get called, except by the Song
 	 * class when starting to play the Tracks.
 	 * @param startLatch
 	 */
 	public void setStartLatch(CountDownLatch startLatch) {
 		this.startLatch = startLatch;
 	}
 	
 	/**
 	 * Sets the gain control value for the Track.
 	 * @param gain
 	 */
 	public void setGain(float gain) {
 		this.gain = gain;
 	}
 	
 	/**
 	 * Sets the mute control value for the track.
 	 * @param mute
 	 */
 	public void setMute(boolean mute) {
 		this.mute = mute;
 	}
 	
 	/**
 	 * Applies an EQ filter based on a 32 channel EQ that
 	 * is set by the float array. The indices of the array correspond
 	 * to each of the frequency levels that can be adjusted, with the
 	 * lower indices corresponding to the lower frequencies (bass), and the
 	 * higher indices corresponding to the higher frequencies (treble).
 	 * 
 	 * This method makes no assumption on the size of the array passed to it
 	 * and will only set the first 32 indices in the array.
 	 * 
 	 * @param equalizer
 	 */
 	public void setEqualizer(float[] equalizer) {
 		System.arraycopy(equalizer, 0, this.equalizer , 0, 32);
 	}
 	
 	/*
 	 * An internal method that sets the values for a BooleanControl.
 	 */
 	private void adjustBooleanControl(BooleanControl control, boolean value) {
 		control.setValue(value);
 	}
 	
 	/*
 	 * An internal method that ensures the values used to set a FloatControl
 	 * are within the bounds of the control.
 	 */
 	private void adjustFloatControl(FloatControl control, float value) {
 		if (value < control.getMaximum() && value > control.getMinimum())
 			control.setValue(value);
 		else if (value > control.getMaximum()) {
 			control.setValue(control.getMaximum());
 		} else if (value < control.getMinimum()) {
 			control.setValue(control.getMinimum());
 		}
 	}
 	
 	/*
 	 * An internal method that returns the correct decoded audio format for the
 	 * types of files being used.
 	 */
 	private AudioFormat getDecodedAudioFormat(AudioFormat baseFormat) {
 		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
 		float sampleRate = baseFormat.getSampleRate();
 		int sampleSizeInBits = 16;
 		int channels = baseFormat.getChannels();
 		int frameSize = baseFormat.getChannels() * 2;
 		float frameRate = baseFormat.getSampleRate();
 		boolean bigEndian = false;
 		
 		return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
 	}
 	
 	/*
 	 * An internal class responsible for playing the Track without blocking the 
 	 * main execution.
 	 */
 	private class PlayThread extends Thread {
 		private final int BUFFER_SIZE = 2000; //Decent buffer size found with experimentation
 		private byte tempBuffer[] = new byte[BUFFER_SIZE]; //Used to transfer data from the input and output streams
 		
 		public void run() {
 			try {
 				//Wait until the CountDownLatch has been 'lifted'
 				startLatch.await();
 				
 				try {
 					//Read the sound file
 					audioInputStream = AudioSystem.getAudioInputStream(soundFile);
 					audioFormat = audioInputStream.getFormat();
 					
 					//Decode the sound file
 					decodedAudioFormat = getDecodedAudioFormat(audioFormat);
 					decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedAudioFormat, audioInputStream);
 					
 					//Get a source data line to put the sound file on
 					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, decodedAudioFormat);
 					sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
 					sourceDataLine.open(decodedAudioFormat);
 					
 					//Get the controls for the stream if they are supported
 					if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
 						gainControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
 					}
 					if (sourceDataLine.isControlSupported(BooleanControl.Type.MUTE)) {
 						muteControl = (BooleanControl) sourceDataLine.getControl(BooleanControl.Type.MUTE);
 					}
 					float[] equalizerControl = new float[32];
 					if (decodedAudioInputStream instanceof javazoom.spi.PropertiesContainer) {
 						@SuppressWarnings("rawtypes")
 						Map properties = ((javazoom.spi.PropertiesContainer) decodedAudioInputStream).properties();
 						equalizerControl = (float[]) properties.get("mp3.equalizer");
 					}
 					
 					//Start streaming the sound file
 					sourceDataLine.start();
 	
 					int count;
 					while ((count = decodedAudioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && isPlaying) {
 						if (count > 0) {
 							//Set the controls for the stream if they are supported
 							if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
 								adjustFloatControl(gainControl, gain);
 							}
 							if (sourceDataLine.isControlSupported(BooleanControl.Type.MUTE)) {
 								adjustBooleanControl(muteControl, mute);
 							}
 							System.arraycopy(equalizer, 0, equalizerControl, 0, 32);
 							
 							//Write the data line to the buffer
 							sourceDataLine.write(tempBuffer, 0, count);
 						}
 					}
 					//The sound file is finished at this point
 					
 					//Clean up the data line
 					sourceDataLine.drain();
 					sourceDataLine.close();
 					
 					//Close the stream
 					audioInputStream.close();
 					decodedAudioInputStream.close();
 					
 					isPlaying = false;
 					
 				} catch (IOException e) {
 					System.out.println("IO Error opening " + soundFile.getAbsolutePath());
 					e.printStackTrace();
 					System.exit(0);
 				} catch (UnsupportedAudioFileException e) {
 					System.out.println("Error opening " + soundFile.getAbsolutePath());
 					e.printStackTrace();
 					System.exit(0);
 				} catch (LineUnavailableException e) {
 					e.printStackTrace();
 					System.exit(0);
 				}
 				
 			} catch (InterruptedException e) {
 				System.out.println("Track play was interrupted.");
 				e.printStackTrace();
 			}
 		}
 
 	}
 	
 }
