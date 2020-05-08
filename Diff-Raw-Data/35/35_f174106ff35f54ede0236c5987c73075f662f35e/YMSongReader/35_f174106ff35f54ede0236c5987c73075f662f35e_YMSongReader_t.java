 /*
  * Copyright (c) 2012 Julien Névo. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  * 
  *  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above
  * copyright notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *  * The names of the authors or their institutions shall not
  * be used to endorse or promote products derived from this software
  * without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package aks.jnv.reader;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import aks.jnv.R;
 import aks.jnv.audio.ISeekPositionObserver;
 import aks.jnv.song.Song;
 import aks.jnv.song.SongUtil;
 import aks.jnv.util.BinaryConstants;
 import aks.jnv.util.Util;
 import aks.jnv.view.PlayMusicActivity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.util.Log;
 
 /**
  * Reader of a YM song.
  * 
  * @author Julien Névo
  *
  */
 public class YMSongReader implements ISongReader {
 
 	private static final int ATARI_ST_PSG_FREQUENCY = 2000000;
 	private static final int DEFAULT_PSG_FREQUENCY = ATARI_ST_PSG_FREQUENCY;		// Default value in case the PSG frequency isn't given.
 	private static final int DEFAULT_REPLAY_FREQUENCY = 50;		// Default value in case the replay frequency isn't given.
 	
 	private static final int LEONARD_STRING_OFFSET = 4;			// Offset of the "LeOnArD!" string.
 	private static final int NB_VALID_VBL_OFFSET = 12;
 	private static final int SONG_ATTRIBUTES_OFFSET = 16;
 	private static final int NB_DIGIDRUMS_OFFSET = 20;
 	private static final int PSG_FREQUENCY_OFFSET = 22;			// Only from YM5 and up !
 	private static final int REPLAY_FREQUENCY_OFFSET = 26;		// Only from YM5 and up !
 	
 	private static final int FRAME_LOOP_START_OFFSET = 24;
 	private static final int FRAME_LOOP_START_OFFSET_YM5 = 28;	// Only from YM5 and up !
 	
 	private static final String LEONARD_TAG = "LeOnArD!";
 	
 	private static final int DWORD_SIZE = 4;
 	private static final int WORD_SIZE = 2;
 	
 	private static final int NB_REGISTERS_TILL_YM3 = 14;
 	private static final int NB_REGISTERS_FROM_YM4 = 16;
 	
 	private static final int HEADER_TAG_SIZE = 4;				// Size in bytes of the Header Tag (YMxx).
 	private static final int YM3B_LOOP_INFORMATION_SIZE = 4;	// YM3B have a four bytes loop start DWord at the end of the file.
 
 	/** The song being read.*/
 	private Song song;
 	
 	private short[] data;			// The data of the song.
 	
 	private short[][] samples;		// Samples. May be Null.
 
 	private int replayFrequency;	// The replay frequency of the song, in Hz (50hz, 25hz etc.).
 	private int PSGFrequency;		// The frequency of the sound processor for which the song has been made, in Hz.
 	private String name;			// The name of the song.
 	private String author;			// The author of the song.
 	private String comments;		// The comments of the song.
 	
 	/** Indicates if the YM registers are encoded all one after one in each frame (false) or interlaced (true).*/
 	private boolean isInterleaved;
 	
 	/** Indicates the version of the YM (1, 2, 6...). */
 	private int YMVersion;
 	
 	/** Nb of valid VBL in the song.*/
 	private int nbFrames;
 	
 	/** Indicates if the samples are signed. */
 	private boolean areSamplesSigned;
 	
 	/** Indicates if the samples are already in ST 4 bits format. */
 	private boolean areSamplesInST4BitsFormat;
 	
 	/** The sample count. */
 	private int nbSamples;
 	
 	/** The frame loop start. */
 	private int frameLoopStart;
 	
 	/** Offset of the first register of the first frame of the song. */
 	private int registersBaseOffset;
 	
 	/** Current Frame/VBL. */
 	private int currentFrame;
 	
 	/** Duration in seconds. */
 	private int durationInSeconds;
 	
 	/** Observers of the seek position. They will be notified whenever it changes. */
 	private List<ISeekPositionObserver> seekPositionObservers = new ArrayList<ISeekPositionObserver>();
 	
 	/** The current seek position in seconds. */
 	private int currentSeekPosition;
 	
 	/** The volume of the channel A. */
 	private int volumeA;
 	
 	/** The volume of the channel B. */
 	private int volumeB;
 	
 	/** The volume of the channel C. */
 	private int volumeC;
 	
 	/** The noise value. */
 	private int noiseValue;
 	
 	/** The bits of the channels using the noise (bit 0 to 1 = noise on channel A etc.). */
 	private int channelsUsingNoise;
 	
	/** The registers to be played. They evolve according to the music replayFrequency, not at each frame. */
	private short[] regs = new short[NB_REGISTERS_FROM_YM4];	// Uses the biggest number of registers.
	
 	/**
 	 * Constructor.
 	 * @param song The YMSong this reader must read.
 	 */
 //	public YMSongReader(YMSong song) {
 //		this.song = song;
 //	}
 
 	/**
 	 * Constructor of the SongReader. The Song MUST be in the correct format.
 	 * The doesRawDataFit method must have been called before to ensure that.
 	 * @param data the raw binary data of the song.
 	 */
 	public YMSongReader(short[] data) {
 		this.data = data;
 		readSongInformation();
 		song = new Song(data, replayFrequency, PSGFrequency, name, author, comments);
 	}
 
 	
 	// ***************************************
 	// Getters and setters
 	// ***************************************
 
 	/**
 	 * Returns the Song read by this Reader.
 	 * @return the Song read by this Reader.
 	 */
 	public Song getSong() {
 		return song;
 	}
 
 	@Override
 	public int getReplayFrequency() {
 		return replayFrequency;
 	}
 
 	@Override
 	public int getPSGFrequency() {
 		return PSGFrequency;
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getAuthor() {
 		return author;
 	}
 
 
 	@Override
 	public String getComments() {
 		return comments;
 	}
 
 	@Override
 	public int getDuration() {
 		return durationInSeconds;
 	}
 
 	@Override
 	public String getFormat() {
 		return "YM " + YMVersion;
 	}
 
 
 	// ***************************************
 	// Private methods
 	// ***************************************
 
 	/**
 	 * Parses the given raw binary data and fills the field of the song.
 	 * The Song MUST be in the correct format. The doesRawDataFit method must have been called before to ensure that.
 	 */
 	private void readSongInformation() {
 		YMVersion = data[2] - '0';
 		
 		// These are default values. They are given only from YM5 and above.
 		PSGFrequency = DEFAULT_PSG_FREQUENCY;
 		replayFrequency = DEFAULT_REPLAY_FREQUENCY;
 		int index = FRAME_LOOP_START_OFFSET;
 		
 		if (YMVersion <= 3) {
 			int endTagSize;
 			// Is it a version 3B ?
 			if (data[3] - '0' == 'b') {
 				// If yes, the loop information is encoded at the end.
 				endTagSize = YM3B_LOOP_INFORMATION_SIZE;
 				frameLoopStart = SongUtil.readDWord(data, data.length - endTagSize);
 			} else {
 				endTagSize = 0;
 				frameLoopStart = 0;
 			}
 			
 			// These versions don't hold any information.
 			nbFrames = (data.length - HEADER_TAG_SIZE - endTagSize) / NB_REGISTERS_TILL_YM3;		// Removes the header.
 			registersBaseOffset = HEADER_TAG_SIZE;
 			isInterleaved = true;
 			author = "";
 			name = "";
 			comments = "";
 		} else {
 			// YM Version 4 and more.
 			
 			nbFrames = SongUtil.readDWord(data, NB_VALID_VBL_OFFSET);
 			int songAttributes = SongUtil.readDWord(data, SONG_ATTRIBUTES_OFFSET);
 			
 			isInterleaved = ((songAttributes & 1) != 0);
 			areSamplesSigned = ((songAttributes & 2) != 0);
 			areSamplesInST4BitsFormat = ((songAttributes & 4) != 0);
 			
 			nbSamples = SongUtil.readWord(data, NB_DIGIDRUMS_OFFSET);
 			//Log.e("XXX", "NB DIGIDRUMS= " + nbSamples);
 			
 			// Information about the frequency of the PSG and the song are only available from YM5.
 			if (YMVersion >=5) {
 				PSGFrequency = SongUtil.readDWord(data, PSG_FREQUENCY_OFFSET);
 				replayFrequency =  SongUtil.readWord(data, REPLAY_FREQUENCY_OFFSET);
 				index = FRAME_LOOP_START_OFFSET_YM5;
 			}
 			
 			frameLoopStart = SongUtil.readDWord(data, index);
 			index += DWORD_SIZE;
 			
 			// Skips possible additional data.
 			if (YMVersion >= 5) {
 				int additionalDataSize = SongUtil.readWord(data, index);
 				index += WORD_SIZE + additionalDataSize;
 			}
 	
 			// Reads the samples, if any.
 			if (nbSamples > 0) {
 				samples = new short[nbSamples][];
 				
 				for (int i = 0 ; i < nbSamples; i++) {
 					int sampleSize = SongUtil.readDWord(data, index);
 					index += DWORD_SIZE;
 					samples[i] = new short[sampleSize];
 					
 					// Reads the sample data itself.
 					for (int j = 0 ; j < sampleSize; j++) {
 						// Make them use only 4 bits if they don't already, and unsigned.
 						int value = data[index++];
 						if (!areSamplesInST4BitsFormat) {
 							value >>>= 4;
 						}
 						if (areSamplesSigned) {
 							value += 8;
 						}
 						samples[i][j] = (short)value;
 					}
 					
 				}
 			}
 			
 			name = SongUtil.readNTString(data, index);
 			index += name.length() + 1;		// Go next to the field. +1 because there's a 0 ending the string.
 			author = SongUtil.readNTString(data, index);
 			index += author.length() + 1;
 			comments = SongUtil.readNTString(data, index);
 			registersBaseOffset = index + comments.length() + 1;
 		}
 		currentFrame = 0;
 		durationInSeconds = (nbFrames / replayFrequency);
 		currentSeekPosition = calculateSeekPosition();
 	}
 
 	/**
 	 * Calculates the seek position in seconds according to the current frame and the replay frequency.
 	 * @return the seek position in seconds.
 	 */
 	private int calculateSeekPosition() {
 		return currentFrame / replayFrequency;
 	}
 
 
 	// ***************************************
 	// Public methods
 	// ***************************************
 	
 	@Override
 	public short[] getNextRegisters() {
 		int nbRegisters = (YMVersion > 3) ? NB_REGISTERS_FROM_YM4 : NB_REGISTERS_TILL_YM3;
 		
 		if (isInterleaved) {
 			// V0R0,V1R0,V2R0,....,VnR0
 			// V0R1,V1R1,V2R1,....,VnR1
 			int index = registersBaseOffset + currentFrame;
 			for (int i = 0; i < nbRegisters; i++) {
 				int temp = index + i * nbFrames;
 				regs[i] = data[temp];
 			}
 		} else {
 			// V0R0,V0R1,V0R2,....,V0R14,V0R15
 			// V1R0,V1R1,V1R2,....,V1R14,V1R15
 			int index = registersBaseOffset + currentFrame * nbRegisters;
 			for (int i = 0; i < nbRegisters; i++) {
 				regs[i] = data[index + i];
 			}
 		}
 
 		// Next frame.
 		if (++currentFrame > nbFrames) {
 			currentFrame = frameLoopStart;
 		}
 		
 		int newSeekPosition = calculateSeekPosition();
 		setNewSeekPositionAndNotifyIfNeeded(newSeekPosition);
 		fillVolumeAndNoiseValues(regs);
 		
 		return regs;
 	}
 
 	@Override
 	public short[] getSample(int sampleNumber) {
 		short[] sample = null;
 		
 		if ((samples != null) && (sampleNumber < samples.length)) {
 			sample = samples[sampleNumber];
 		}
 		
 		return sample;
 	}
 
 //	@Override
 //	public int seekInSeconds(int secondToReach) {
 //		int result = 0;
 //		if (secondToReach < durationInSeconds) {
 //			currentFrame = secondToReach * replayFrequency;
 //			result = secondToReach;
 //		}
 //		return result;
 //	}
 	
 
 	@Override
 	public void addSeekObserver(ISeekPositionObserver observer) {
 		seekPositionObservers.add(observer);
 	}
 
 	// FIXME ******* Seek here and seekInSeconds just above !!!!!!!!!!!! ************
 	@Override
 	public void seek(int seconds) {
 		currentFrame = calculateFrameNumberFromSeconds(seconds);
 		setNewSeekPositionAndNotifyIfNeeded(seconds);
 		//currentSeekPosition = seconds;
 	}
 
 	@Override
 	public int getVolumeChannel(int channel) {
 		int value = 0;
 		switch (channel) {
 		case 1:
 			value = volumeA;
 			break;
 		case 2:
 			value = volumeB;
 			break;
 		case 3:
 			value = volumeC;
 			break;
 		}
 		return value;
 	}
 
 
 	@Override
 	public int getNoiseValue() {
 		return noiseValue;
 	}
 
 
 	@Override
 	public int getNoiseChannels() {
 		return channelsUsingNoise;
 	}
 
 	
 	
 	// ***************************************
 	// Private methods
 	// ***************************************
 
 	/**
 	 * Fills the volume and noise values so that the equalizer can read them when it wants.
 	 * @param regs the PSG registers.
 	 */
 	private void fillVolumeAndNoiseValues(short[] regs) {
 		int value = regs[8];
 		if (value > 16) { value = 16; }
 		volumeA = value;
 
 		value = regs[9];
 		if (value > 16) { value = 16; }
 		volumeB = value;
 		
 		value = regs[10];
 		if (value > 16) { value = 16; }
 		volumeC = value;
 		
 		noiseValue = regs[6] & BinaryConstants.B_00011111;
 		
 		// Takes the bits 3,4,5 and inverts them because for the PSG, 0 is "open".
 		channelsUsingNoise = ((regs[7] >> 3) ^ 111) & BinaryConstants.B_00000111;
 	}
 
 	/**
 	 * Sets the given seek position and notifies the change, but only if the seek position is different from
 	 * the current one.
 	 * @param newSeekPosition the new seek position to set.
 	 */
 	private void setNewSeekPositionAndNotifyIfNeeded(int newSeekPosition) {
 		if (currentSeekPosition != newSeekPosition) {
 			currentSeekPosition = newSeekPosition;
 			notifyNewSeekPosition();
 		}
 	}
 	
 	/**
 	 * Notifies the seek position observers about a change.
 	 */
 	private void notifyNewSeekPosition() {
 		for (ISeekPositionObserver observer : seekPositionObservers) {
 			observer.notifyNewSeekPositionFromSubject(currentSeekPosition);
 		}
 	}
 
 	/**
 	 * Calculates the frame number according to the given second, with a maximum of the duration of song tested. 
 	 * @param second the second of the song to reach.
 	 * @return the frame number.
 	 */
 	private int calculateFrameNumberFromSeconds(int second) {
 		int frameNumber = second * replayFrequency;
 		return frameNumber > nbFrames ? nbFrames : frameNumber;
 	}
 	
 	
 	
 	// ***************************************
 	// Static public methods
 	// ***************************************
 	
 	/**
 	 * Indicates if the given raw binary data fits the format of the song.
 	 * @param data the raw binary data of the song.
 	 * @return true if the given raw binary data fits the format of the song.
 	 */
 //	public static boolean doesRawDataFit(short[] data) {
 //		
 //		// Checks for the YMx! tag.
 //		if ((data[0] != 'Y') || (data[1] != 'M') || (data[3] != '!')) {
 //			return false;
 //		}
 //		
 //		// Checks for the Leonard tag.
 //		return SongUtil.readNTString(data, LEONARD_STRING_OFFSET).equals(LEONARD_TAG);
 //	}
 
 
 	/**
 	 * Indicates if the given raw binary data fits the format of the song. It may be packed in LHA or not.
 	 * @param is the input stream containing the song.
 	 * @return true if the given raw binary data fits the format of the song.
 	 */
 //	public static short[] doesRawDataFit(InputStream is) {
 //		
 //		// First checks if the input stream is a LHA file.
 //		byte[] dataByte = null;
 //		
 //			try {
 //				dataByte = Util.unpackLHAFile(is);
 //			} catch (IOException e) {
 //				e.printStackTrace();
 //			}
 //		
 //		short[] data = Util.byteArrayToShortArray(dataByte);
 //		// Checks for the YMx! tag and the Leonard tag.
 //		boolean result = (data[0] == 'Y') && (data[1] == 'M') && (data[3] == '!')
 //				&& (SongUtil.readNTString(data, LEONARD_STRING_OFFSET).equals(LEONARD_TAG)); 
 //		
 //		return result ? data : null;
 //	}
 	
 	
 	/**
 	 * Indicates if the given raw binary data fits the format of the song. It may be packed in LHA or not.
 	 * @param musicFile the file containing the song.
 	 * @return the given raw binary data fits the format of the song, or null if something went wrong.
 	 */
 	public static short[] doesRawDataFit(File musicFile) {
 		
 		// First checks if the input stream is a LHA file.
 		byte[] dataByte = null;
 						// FIXME HACK for debug (no file !)
 						if (musicFile == null) {
 							Log.e("XXX", "YMSongReader::doesRawDataFit : null");
 							//Context ctx = FirstActivity.getContext();
 							Context ctx = PlayMusicActivity.getContext();
 							Resources r = ctx.getResources();
 							//InputStream is = r.openRawResource(R.raw.syntaxdecomp);
 							//InputStream is = r.openRawResource(R.raw.molusk);
 							//InputStream is = r.openRawResource(R.raw.megapockolipse);
 							//InputStream is = r.openRawResource(R.raw.w3_3);
 							//InputStream is = r.openRawResource(R.raw.akscreen);
 							//InputStream is = r.openRawResource(R.raw.lethal1);
 							//InputStream is = r.openRawResource(R.raw.bioniccommando1);
 							//InputStream is = r.openRawResource(R.raw.arpy1);
 							InputStream is = r.openRawResource(R.raw.cybern3);
 							try {
 								dataByte = Util.readInputStream(is);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 							Log.e("XXX", "YMSongReader::doesRawDataFit : size = " + dataByte.length);
 							return Util.byteArrayToShortArray(dataByte);
 						}
 		
 		
 		try {
 			dataByte = Util.unpackLHAFile(musicFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		short[] data = Util.byteArrayToShortArray(dataByte);
 		// Checks for the YMx! tag and the Leonard tag.
 		boolean result = (data[0] == 'Y') && (data[1] == 'M') && (data[3] == '!')
 				&& (SongUtil.readNTString(data, LEONARD_STRING_OFFSET).equals(LEONARD_TAG)); 
 		
 		return result ? data : null;
 	}
 
 
 
 
 	
 
 }
