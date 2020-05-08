 /**
  * File StreamMusicPlayer.java
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
 
 /**
  *  @author Martin Braun
  *  Player inspired by Matthias Pfisterer's examples on JavaSound
  *  (jsresources.org). Because of the fact, that this Software is meant 
  *  to be Open-Source and I don't want to get anybody angry about me 
  *  using parts of his intelligence without mentioning it, I hereby 
  *  mention him as inspiration, because his code helped me to write this class.
  */
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class StreamMusicPlayer implements IMusicPlayer {
 
 	protected ExecutorService mExecutorService;
 	protected StreamPlayerCallable mPlayerRunnable;
 	protected IPlaybackListener mPlaybackListener;
 	/**
 	 * the current song after insertion
 	 */
 	protected ISong mCurrentSong;
 	/**
 	 * the current mixer after insertion
 	 */
 	protected Mixer mCurrentMixer;
 	private Lock mLock;
 
 	/**
 	 * Default Constructor. initializes without a {@link #PlayerThreadListener}
 	 */
 	public StreamMusicPlayer() {
 		this(null);
 	}
 
 	/**
 	 * Default Constructor. initializes with the given
 	 * {@link #PlayerThreadListener}
 	 */
 	public StreamMusicPlayer(IPlaybackListener pPlaybackListener) {
 		this(pPlaybackListener, null);
 	}
 
 	/**
 	 * uses the given ExecutorService to run its tasks.
 	 * if null is specified it uses the current thread
 	 * @param pPlaybackListener
 	 * @param pExecutorService
 	 */
 	public StreamMusicPlayer(IPlaybackListener pPlaybackListener,
 			ExecutorService pExecutorService) {
 		this.mLock = new ReentrantLock();
 		this.mPlaybackListener = pPlaybackListener;
 		this.mExecutorService = pExecutorService;
 		this.mCurrentSong = null;
 		this.mCurrentMixer = null;
 	}
 
 	@Override
 	public void setPlaybackListener(IPlaybackListener pPlaybackListener) {
 		this.mPlaybackListener = pPlaybackListener;
 	}
 
 	/**
 	 * @inheritDoc
 	 * @throws SongInsertionException
 	 *             if audio file is either not supported, its line is not
 	 *             available or an IOException has been thrown in the underlying
 	 *             methods
 	 */
 	@Override
 	public void insert(ISong pSong) throws SongInsertionException {
 		this.insert(pSong, null);
 	}
 
 	/**
 	 * @param pMixer
 	 *            if null is passed the AudioSystem uses the default Mixer
 	 * @inheritDoc
 	 * @throws SongInsertionException
 	 *             if audio file is either not supported, its line is not
 	 *             available or an IOException has been thrown in the underlying
 	 *             methods
 	 */
 	@Override
 	public void insert(ISong pSong, Mixer pMixer) throws SongInsertionException {
 		this.mLock.lock();
 		try {
 			this.mCurrentSong = pSong;
 			this.mCurrentMixer = pMixer;
 			this.insertInternal(pSong, pMixer);
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void startPlayback() throws IOException {
 		this.mLock.lock();
 		try {
 			if(this.mPlayerRunnable == null) {
 				throw new IllegalStateException(this +
 						" has not been initialized yet!");
 			}
 			if(!this.mPlayerRunnable.isStopped()) {
 				throw new IllegalStateException("Player is already playing");
 			}
 			if(this.mExecutorService != null) {
 				//run on the thread specified
 				this.mExecutorService.submit(this.mPlayerRunnable);
 			} else {
 				//run on our own thread
 				this.mPlayerRunnable.call();
 			}
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 	
 	@Override
 	public void restartPlayback() {
 		throw new UnsupportedOperationException("not implemented yet");
 	}
 
 	@Override
 	public void pausePlayback() {
 		this.mLock.lock();
 		try {
 			if(this.mPlayerRunnable == null) {
 				throw new IllegalStateException(this +
 						" has not been initialized yet!");
 			}
 			this.mPlayerRunnable.pausePlayback();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void stopPlayback() {
 		this.mLock.lock();
 		try {
 			if(this.mPlayerRunnable == null) {
 				throw new IllegalStateException(this +
 						" has not been initialized yet!");
 			}
 			this.mPlayerRunnable.stopPlayback();
			this.mPlayerRunnable = null;
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public boolean isStopped() {
 		this.mLock.lock();
 		try {
 			if(this.mPlayerRunnable == null) {
 				throw new IllegalStateException(this +
 						" has not been initialized yet!");
 			}
 			return this.mPlayerRunnable.isStopped();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void unpausePlayback() {
 		this.mLock.lock();
 		try {
 			if(this.mPlayerRunnable == null) {
 				throw new IllegalStateException(this +
 						" has not been initialized yet!");
 			}
 			this.mPlayerRunnable.unpausePlayback();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public boolean isPaused() {
 		this.mLock.lock();
 		try {
 			return this.mPlayerRunnable.isPaused();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public AudioFormat getAudioFormat() {
 		this.mLock.lock();
 		try {
 			return this.mPlayerRunnable.getAudioFormat();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public DataLine getDataLine() {
 		this.mLock.lock();
 		try {
 			return this.mPlayerRunnable.getDataLine();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	@Override
 	public void seek(int pPosition) {
 		this.mLock.lock();
 		try {
 			throw new UnsupportedOperationException("not implemented yet");
 //			// just pause the playback
 //			this.stopPlayback();
 //			try {
 //				this.insertInternal(this.mCurrentSong, this.mCurrentMixer);
 //				if(file != null && this.mPlayerRunnable.mAudioFormat.getFrameLength() != -1)
 //				{	
 //				double skippedPercentage = (double)percentage/100;
 //				System.out.println("Percentage to skip " + skippedPercentage);
 //				long framesToSkip = (long) (getFrameLength() * skippedPercentage);
 //				System.out.println("Skipping " + framesToSkip + " frames with " + getFrameLength() + " available");
 //				long bytesSkipped = 0;
 //				byte[] garbage = new byte[4096];
 //				long bytesDropped = 0;
 //				while(bytesSkipped <= framesToSkip*audioFormat.getFrameSize() && bytesDropped != -1)
 //				{
 //				// System.out.println(bytesSkipped/audioFormat.getFrameSize());
 //				bytesDropped = audioInputStream.read(garbage, 0, garbage.length);
 //				// bytesDropped = audioInputStream.skip(4096);
 //				// System.out.println("Dropped " + bytesDropped + " bytes");
 //				bytesSkipped += bytesDropped;
 //				}
 //				skippedFrames += bytesSkipped/audioFormat.getFrameSize();
 //				System.out.println("Skipped Frames: " + bytesSkipped/audioFormat.getFrameSize());
 //				}
 //				}
 //				catch (IOException e)
 //				{
 //				e.printStackTrace();
 //				}	
 //				catch(Exception e)
 //				{
 //				e.printStackTrace();
 //			}
 //			// start playing again
 //			this.startPlayback();
 		} finally {
 			this.mLock.unlock();
 		}
 	}
 
 	private void insertInternal(ISong pSong, Mixer pMixer) throws SongInsertionException {
 		if(this.mPlayerRunnable != null &&
 				!this.mPlayerRunnable.isStopped()) {
 			throw new IllegalStateException("You can only insert Songs while the Player is stopped!");
 		}
 		try {
 			this.mPlayerRunnable = new StreamPlayerCallable(pSong,
 					this.mPlaybackListener,
 					pMixer);
 		} catch(UnsupportedAudioFileException
 				| IOException
 				| LineUnavailableException e) {
 			throw new SongInsertionException("Couldn't insert " + pSong, e);
 		}
 	}
 	
 }
