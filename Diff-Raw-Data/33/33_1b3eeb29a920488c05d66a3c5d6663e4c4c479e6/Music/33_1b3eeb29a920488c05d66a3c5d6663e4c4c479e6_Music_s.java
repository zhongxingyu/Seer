 package com.earlofmarch.reach.model;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * The <code>Music</code> class is an easy-to-use implementation of the Java Sound API's
  * <code>{@link Clip}</code> to play audio.
  * @author Kenneth 
  * Modified by @author Aly
  * 
  * Beep part by Aly! :D
  *
  */
 public class Music {
 	
 	private static Clip musicClip;
 	public static String theme = "";
 	
 	/**
 	 * Creates a new audio Clip with the specified audio file
 	 * @param url The name of the audio file
 	 */
 	public static void playMusic(URL url) {
 		try {
 			musicClip = AudioSystem.getClip();
 			AudioInputStream stream = AudioSystem.getAudioInputStream(url);
 			musicClip.open(stream);
 			musicClip.start();
 			
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}	
 	
 	public static void stopCurrentMusic(){
 		musicClip.stop();
 	}
 	
 	public static float RATE = 8000f;
 	
 	/**
 	 * Plays a beep sound with the given parameters.
 	 */
 	public static void playBeep(int hz, int msecs, double vol){
 	    if (vol > 1.0 || vol < 0.0)
 	        return;
 	
 	    byte[] buf = new byte[(int)RATE * msecs / 1000];
 	
 	    for (int i=0; i<buf.length; i++) {
 	        double angle = i / (RATE / hz) * 2.0 * Math.PI;
 	        buf[i] = (byte)(Math.sin(angle) * 127.0 * vol);
 	    }
 	
 	    // shape the front and back 10ms of the wave form
 	    for (int i=0; i < RATE / 100.0 && i < buf.length / 2; i++) {
 	        buf[i] = (byte)(buf[i] * i / (RATE / 100.0));
 	        buf[buf.length-1-i] = (byte)(buf[buf.length-1-i] * i / (RATE / 100.0));
 	    }
 	
 	    AudioFormat af = new AudioFormat(RATE,8,1,true,false);
 	    SourceDataLine sdl;
 		try {
 			 sdl = AudioSystem.getSourceDataLine(af);		
 		     sdl.open(af);
 		     sdl.start();
 		     sdl.write(buf,0,buf.length);
 		     sdl.drain();
 		     sdl.close();
 		} catch (LineUnavailableException e) {}
 	}
 	
 	public static class RunBeep implements Runnable{
 		int h,m;
 		double v;
 		
 		public RunBeep(int hz, int msecs, double vol){
 			this.run();
 			h=hz;
 			m=msecs;
 			v=vol;
 		}
 		@Override
 		public void run() {
 			Music.playBeep(h, m, v);
 		}		
 	}
 }
