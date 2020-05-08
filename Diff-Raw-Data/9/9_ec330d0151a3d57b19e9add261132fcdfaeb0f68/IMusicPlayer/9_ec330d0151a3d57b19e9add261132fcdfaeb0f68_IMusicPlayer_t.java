 /**
  * File IMusicPlayer.java
  * ---------------------------------------------------------
  *
  * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * - The origin of the software must not be misrepresented.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
  *   if it breaks something.
  */
 package de.hotware.hotsound.audio.player;
 
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.Mixer;
 
 /**
  * Music Player interface. provides basic player methods and getters for a
  * player.
  * 
  * @author Martin Braun
  */
 public interface IMusicPlayer {
 
 	/**
 	 * inserts a Song to the the Player possible difference between
 	 * implementations: some could allow multiple songs to be added, but some
 	 * could only hold one song at a time
 	 */
 	public void insert(ISong pSong) throws SongInsertionException;
 
 	/**
 	 * inserts a Song to the the Player possible difference between
 	 * implementations: some could allow multiple songs to be added, but some
 	 * could only hold one song at a time
 	 */
 	public void insert(ISong pSong, Mixer pMixer) throws SongInsertionException;
 
 	/**
 	 * starts the playback
 	 * 
 	 * @throws IllegalStateException
 	 *             if the player hasn't been initialized yet (insert not called)
 	 * @throws IOException
 	 *             if there was an IO error during the start of the playback
 	 */
 	public void startPlayback() throws IOException;
 
 	/**
 	 * pauses the playback
 	 * 
 	 * @throws IllegalStateException
 	 *             if the player hasn't been initialized yet (insert not called)
 	 */
 	public void pausePlayback();
 
 	/**
 	 * unpauses the playback
 	 * 
 	 * @throws IllegalStateException
 	 *             if the player hasn't been initialized yet (insert not called)
 	 */
 	public void unpausePlayback();
 
 	/**
	 * stops the playback but doesn't reset the Player. you can restart it via
	 * restartPlayback afterwards
 	 * 
 	 * @throws IllegalStateException
 	 *             if the player hasn't been initialized yet (insert not called)
 	 */
 	public void stopPlayback();
 
 	/**
 	 * @return true if stopped (not started or ended), false otherwise
 	 */
 	public boolean isStopped();
 
 	/**
 	 * @return true if started and paused (not ended), false otherwise
 	 */
 	public boolean isPaused();
 
 	/**
 	 * @return the AudioFormat of the current song
 	 */
 	public AudioFormat getAudioFormat();
 
 	/**
 	 * @return the DataLine of the current song
 	 */
 	public DataLine getDataLine();
 
 	/**
 	 * seeks to the given position
 	 * 
 	 * @param pPosition
 	 *            position in microseconds
 	 */
 	public void seek(int pPosition);
 
 	public void restartPlayback();
 
 	public void setPlaybackListener(IPlaybackListener pPlaybackListener);
 
 	/**
 	 * Exception that occurs if an error during song insertion occurs
 	 * 
 	 * @author Martin Braun
 	 */
 	public static class SongInsertionException extends Exception {
 
 		private static final long serialVersionUID = 8381906976505908003L;
 
 		public SongInsertionException() {
 			super();
 		}
 
 		public SongInsertionException(String pMessage) {
 			super(pMessage);
 		}
 
 		public SongInsertionException(String pMessage, Throwable pCause) {
 			super(pMessage, pCause);
 		}
 
 		public SongInsertionException(Throwable pCause) {
 			super(pCause);
 		}
 
 	}
 
 }
