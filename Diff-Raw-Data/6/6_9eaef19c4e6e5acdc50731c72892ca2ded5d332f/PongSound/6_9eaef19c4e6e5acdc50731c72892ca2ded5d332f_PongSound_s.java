 package com.github.janpath.pongSE;
 
 import java.io.*;
 
 import java.net.URL;
 import javax.sound.sampled.*;
 
 public class PongSound {
 
 	private Clip clip;
	public static final PongSound PONG_WALL = new PongSound(PongSound.class.getClass().getResource("/janpath/pong/sound/pongWall.wav"));
	public static final PongSound PONG_PADDLE = new PongSound(PongSound.class.getClass().getResource("/janpath/pong/sound/pongPaddle.wav"));
	public static final PongSound PONG_POINT = new PongSound(PongSound.class.getClass().getResource("/janpath/pong/sound/pongPoint.wav"));
 	private AudioInputStream ais;
 
 	private PongSound(URL file) {
 
 		try {
 			ais = AudioSystem.getAudioInputStream(file);
 			AudioFormat format = ais.getFormat();
 			DataLine.Info info = new DataLine.Info(Clip.class, format,
 					((int) ais.getFrameLength() * format.getFrameSize()));
 			clip = (Clip) AudioSystem.getLine(info);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void playSound() {
 		try {
 			if (!clip.isOpen()) {
 				clip.open(ais);
 				clip.start();
 			} else {
 				clip.loop(1);
 			}
 
 		} catch (LineUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 	}
 }
