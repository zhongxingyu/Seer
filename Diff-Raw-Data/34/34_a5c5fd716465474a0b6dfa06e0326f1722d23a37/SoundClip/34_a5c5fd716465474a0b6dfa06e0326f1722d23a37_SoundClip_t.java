 /**
  * Wrapper package for the Java sound library
  */
 package kaninator.sound;
 
 import javax.sound.sampled.*;
 
 import java.io.*;
 import java.net.URL;
 
 
 /**
  * Primitive sound clip class for sound effects.
  * Should only be constructed from the SoundFactory class.
  * @author phedman
  * @see kaninator.sound.SoundFactory
  */
 public class SoundClip
 {
 	private Clip clip;
 	/**
 	 * Attempts to load a sound clip from a file.
 	 * For internal use only.
 	 * Throws IOException if unsuccessful.
 	 * @param filepath Path to the sound file
 	 * @throws IOException Thrown if the file isn't found, if it is invalid or can't be loaded. 
 	 */
 	protected SoundClip(String filepath) throws IOException
 	{
 		try
 		{
 			URL url = this.getClass().getResource(filepath);
 			if(url == null)
 				throw new IOException("ERR: File not found: " + filepath);
 			
 		    AudioInputStream sound = AudioSystem.getAudioInputStream(url.openStream());
 		    DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
 		    clip = (Clip)AudioSystem.getLine(info);
 		    clip.open(sound);
 		}
 		catch(UnsupportedAudioFileException e)
 		{
 			throw new IOException("ERR: Invalid format: " + e);
 		}
 		catch(LineUnavailableException e)
 		{
 			throw new IOException("ERR: Couldn't reserve sound line:" + e);
 		}
 	}
 	
 	/**
 	 * Creates an empty sound clip, used to represent faulty sounds.
 	 * For internal use only.
 	 */
 	protected SoundClip()
 	{
 		clip = null;
 	}
 	
 	/**
 	 * If the sound clip is valid, then rewind it and play it.
 	 */
 	public void playClip()
 	{
 		if(clip != null)
 		{
			if(clip.isRunning())
			{
				clip.stop();
				clip.flush();
			}
			
 			clip.setFramePosition(0);
			if(!clip.isRunning())
				clip.start();
 		}
 	}
 	
 	/**
 	 * Needed to release the system resources when the sound clips are freed by the garbage collector.
 	 */
 	protected void finalize()
 	{
 		if(clip != null)
 		{
 			clip.close();
 		}
 	}
 }
