 package com.musicgame.PumpAndJump.music;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import android.annotation.TargetApi;
 import android.media.MediaExtractor;
 import android.os.Build;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.musicgame.PumpAndJump.game.sound.MP3Decoder;
 
 @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 public class AndroidMP3Decoder16 extends MP3Decoder
 {
 
 	MediaExtractor extractor = new MediaExtractor();
 	public AndroidMP3Decoder16(FileHandle file)
 	{
 		super(file);
 		extractor.setDataSource(absolutePath);
 	}
 
 	/*
 
 	@Override
 	protected void createAudioStream(String file)
 	{
 		extractor.setDataSource(file);
 		buf = ByteBuffer.allocate(frameSize);
 	}
 
 	*/
 	@Override
 	public MP3Decoder getInstance(FileHandle file)
 	{
 		return new AndroidMP3Decoder(file);
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
 
 	/**
 	 * Assuming that samples greater than or equal to the size of array
 	 */
 	@Override
 	public int readSamples(short[] samples, int offset, int numSamples)
 	{
 		ByteBuffer buf = ByteBuffer.allocate(numSamples);
 		int i = extractor.readSampleData(buf, offset);
 		if(i==-1)
 			return i;
 		byte[] array = buf.array();
 		for(int k = 0; k<array.length;k++)
 		{
 			samples[k] = array[k];
 		}
 		return i;
 	}
 
 }
