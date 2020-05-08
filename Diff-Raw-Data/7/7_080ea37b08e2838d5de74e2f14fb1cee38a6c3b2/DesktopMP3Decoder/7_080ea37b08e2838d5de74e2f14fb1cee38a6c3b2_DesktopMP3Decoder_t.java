 package com.musicgame.PumpAndJump.music;
 
 import java.io.IOException;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.musicgame.PumpAndJump.game.sound.MP3Decoder;
 
 public class DesktopMP3Decoder extends MP3Decoder {
 
 	public DesktopMP3Decoder(FileHandle file) {
 		super(file);
 	}
 
 	@Override
 	public MP3Decoder getInstance(FileHandle file) {
 		return null;
 	}
 
 	@Override
 	public int skipSamples(int paramInt) {
 		return 0;
 	}
 
 	@Override
 	public int getChannels() {
 		return 0;
 	}
 
 	@Override
 	public int getRate() {
 		return 0;
 	}
 
 	@Override
 	public float getLength() {
 		return 0;
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public int readSamples(short[] samples, int offset, int numSamples) {
 		return 0;
 	}
 
 
 }
