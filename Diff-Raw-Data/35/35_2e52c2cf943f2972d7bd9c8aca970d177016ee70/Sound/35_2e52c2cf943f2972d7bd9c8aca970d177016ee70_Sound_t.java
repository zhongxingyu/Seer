 package com.github.dnsev.videncode;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.ByteArrayOutputStream;
 
 import java.util.ArrayList;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.sound.sampled.Clip;
 
 
 
 class Sound {
 
 	private boolean valid = false;
 	private Clip clip = null;
 	private AudioFormat audioFormat = null;
 	private ByteArrayInputStream input = null;
 	private AudioInputStream audioStream = null;
 	public Sound(final File source) {
 		try {
 			this.input = new ByteArrayInputStream(this.toBytes(source));
 			this.audioStream = AudioSystem.getAudioInputStream(this.input);
 			this.clip = AudioSystem.getClip();
 			this.clip.open(this.audioStream);
 		}
 		catch (UnsupportedAudioFileException e) {
			this.input = null;
			this.audioStream = null;
			this.clip = null;
 			return;
 		}
 		catch (IOException e) {
			this.input = null;
			this.audioStream = null;
			this.clip = null;
 			return;
 		}
 		catch (LineUnavailableException e) {
			this.input = null;
			this.audioStream = null;
			this.clip = null;
 			return;
 		}
 
 		this.valid = true;
 	}
 
 	public final void play() {
 		if (!this.valid) return;
 
 		this.stop();
 		this.clip.start();
 	}
 	public final void stop() {
		if (!this.valid) return;

 		this.clip.stop();
 		this.clip.setFramePosition(0);
 	}
 
 	private final byte[] toBytes(File f) {
 		// Open file
 		FileInputStream fileIn;
 		try {
 			fileIn = new FileInputStream(f);
 		}
 		catch (Exception e) {
 			return new byte[0];
 		}
 
 		// Read to byte array
 		byte[] ret = null;
 		try {
 			int count;
 			byte[] buffer = new byte[512 * 1024];
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 			while ((count = fileIn.read(buffer)) >= 0) {
 				out.write(buffer, 0, count);
 			}
 
 			ret = out.toByteArray();
 		}
 		catch (Exception e) {
 			ret = null;
 		}
 
 		// Return
 		try {
 			fileIn.close();
 		}
 		catch (Exception e) {
 		}
 		return (ret == null ? new byte[0] : ret);
 	}
 
 }
 
 
