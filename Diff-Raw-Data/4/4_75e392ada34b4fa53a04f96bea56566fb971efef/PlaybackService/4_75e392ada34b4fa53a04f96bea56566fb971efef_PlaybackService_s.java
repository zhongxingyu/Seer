 package com.robonobo.core.service;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.common.util.TimeUtil;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.AudioPlayer.Status;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
 import com.robonobo.mina.external.MinaControl;
 import com.robonobo.mina.external.buffer.*;
 
 public class PlaybackService extends AbstractService implements AudioPlayerListener, PageBufferListener {
 	private AudioPlayer.Status status = Status.Stopped;
 
 	/**
 	 * If we're within this time (secs) after the start of a track, calling prev() goes to the previous track
 	 * (otherwise, returns to the start of the current one)
 	 */
 	public static final int PREV_TRACK_GRACE_PERIOD = 5;
 	/**
 	 * How much data do we need before we start playing?
 	 */
 	public static final int BYTES_BUFFERED_DATA = 256000;
 	private AudioPlayer player;
 	private Date playStartTime;
 	private final Log log = LogFactory.getLog(getClass());
 	private EventService event;
 	private TrackService tracks;
 	private StreamService streams;
 	private DownloadService download;
 	private MinaControl mina;
 	String currentStreamId;
 
 	public PlaybackService() {
 		addHardDependency("core.tracks");
 		addHardDependency("core.storage");
 		addHardDependency("core.streams");
 	}
 
 	@Override
 	public void startup() throws Exception {
 		event = rbnb.getEventService();
 		tracks = rbnb.getTrackService();
 		download = rbnb.getDownloadService();
 		streams = rbnb.getStreamService();
 		mina = rbnb.getMina();
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		stop();
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	/**
 	 * Passing streamId = null is the same as passing the currently playing stream
 	 */
 	public synchronized void play(String streamId) {
 		if (streamId == null)
 			streamId = currentStreamId;
 		// If we have an existent audio player (and we're being told to play the same stream), just set it playing
 		if (streamId.equals(currentStreamId) && player != null) {
 			try {
 				player.play();
 				status = Status.Starting;
 			} catch (IOException e) {
 				log.error("Caught exception restarting playback", e);
 			}
 			tracks.notifyPlayingTrackChange(currentStreamId);
 			event.firePlaybackStarted();
 			return;
 		}
 		// If we're currently playing something else, stop it
 		if (player != null) {
 			String stoppedStreamId = currentStreamId;
 			player.stop();
 			currentStreamId = null;
 			player = null;
 			event.fireTrackUpdated(stoppedStreamId);
 		}
 		currentStreamId = streamId;
 		Track t = tracks.getTrack(currentStreamId);
 		if (t instanceof CloudTrack) {
 			// Whoops, we're not sharing/downloading this stream...
 			// download it!
 			try {
 				download.addDownload(currentStreamId);
 			} catch (RobonoboException e) {
 				log.error("Error adding download", e);
 				return;
 			}
 			t = download.getDownload(currentStreamId);
 		}
 		// If this is a download, make sure it's running and is highest
 		// priority/velocity
 		if (t instanceof DownloadingTrack) {
 			DownloadingTrack d = (DownloadingTrack) t;
 			if (d.getDownloadStatus() == DownloadStatus.Paused) {
 				try {
 					download.startDownload(currentStreamId);
 				} catch (RobonoboException e) {
 					log.error("Error adding download", e);
 					return;
 				}
 			}
 			// Make sure the playing stream is the highest priority
 			download.updatePriorities();
 			mina.setStreamVelocity(currentStreamId, StreamVelocity.MaxRate);
 			mina.setAllStreamVelocitiesExcept(currentStreamId, StreamVelocity.LowestCost);
 		}
 		PageBuffer pb = rbnb.getStorageService().getPageBuf(currentStreamId);
 		if (pb == null)
 			throw new Errot();
 		// If we already have some of this stream, start playing it straight
 		// away, otherwise ask it to notify us when it gets data, and start
 		// playing
 		status = Status.Buffering;
 		event.firePlaybackStarting();
 		Stream s = streams.getKnownStream(currentStreamId);
 		if (bufferedEnough(s, pb))
 			startPlaying(s, pb);
 		else {
 			tracks.notifyPlayingTrackChange(currentStreamId);
 			pb.addListener(this);
 		}
 	}
 
 	public void advisedOfTotalPages(PageBuffer pb) {
 		// Do nothing
 	}
 
 	/**
 	 * Called by the pagebuffer when it receives a page - check to see if we have enough data, and start playing if so
 	 */
 	public void gotPage(final PageBuffer pb, long pageNum) {
 		if (currentStreamId.equals(pb.getStreamId())) {
 			Stream s = streams.getKnownStream(currentStreamId);
 			if (status == Status.Buffering) {
 				if (bufferedEnough(s, pb)) {
 					pb.removeListener(this);
 					startPlaying(s, pb);
 				}
 			}
 		} else {
 			// We're playing another stream now, i don't want to hear from this
 			// guy any more
 			pb.removeListener(this);
 		}
 	}
 
 	private void startPlaying(Stream s, PageBuffer pb) {
 		synchronized (this) {
 			player = getAudioPlayer(s, pb);
 			player.addListener(this);
 			try {
 				player.play();
 			} catch (IOException e) {
 				log.error("Caught exception starting playback for " + s, e);
 				player = null;
 				status = Status.Stopped;
 				return;
 			}
 		}
 		status = Status.Starting;
 		playStartTime = TimeUtil.now();
 		log.debug("Told audioplayer to start playback for " + s);
 		tracks.notifyPlayingTrackChange(currentStreamId);
 	}
 
 	/**
 	 * This is called by the audioplayer when it starts playback
 	 */
 	@Override
 	public void playbackStarted() {
 		if (status == Status.Buffering || status == Status.Starting) {
 			log.info("Audio player started playback");
 			status = Status.Playing;
 			event.firePlaybackStarted();
 			event.fireTrackUpdated(currentStreamId);
 		}
 	}
 
 	private AudioPlayer getAudioPlayer(Stream s, PageBuffer pb) {
 		return getRobonobo().getFormatService().getFormatSupportProvider(s.getMimeType())
 				.getAudioPlayer(s, pb, getRobonobo().getExecutor());
 	}
 
 	// Do we have enough buffered data to start playing?
 	private boolean bufferedEnough(Stream s, PageBuffer pb) {
 		if (pb.isComplete())
 			return true;
 		int bytesData = 0;
 		for (long pn = 0; pn < Integer.MAX_VALUE; pn++) {
 			PageInfo pi = getRobonobo().getStorageService().getPageInfo(s.getStreamId(), pn);
 			if (pi == null)
 				return false;
 			bytesData += pi.getLength();
 			if (bytesData >= BYTES_BUFFERED_DATA)
 				return true;
 		}
 		throw new Errot();
 	}
 
 	/**
 	 * Returns the current stream that is playing/paused, or null if none
 	 */
 	public String getCurrentStreamId() {
 		return currentStreamId;
 	}
 
 	public synchronized void pause() {
 		synchronized (this) {
 			if (status == Status.Buffering) {
 				// We don't have a player yet, we're waiting for feedback to be
 				// buffered - remove ourselves as a listener so we don't start
 				// playing when the buffer is full
 				rbnb.getStorageService().getPageBuf(currentStreamId).removeListener(this);
 			}
 			if (player != null) {
 				try {
 					player.pause();
 				} catch (IOException e) {
 					log.error("Error pausing", e);
 					stop();
 					return;
 				}
 			}
 			status = Status.Paused;
 		}
 		tracks.notifyPlayingTrackChange(currentStreamId);
 		event.firePlaybackPaused();
 	}
 
 	/**
 	 * @param ms
 	 *            Position to seek to, measured from the start of the stream
 	 */
 	public synchronized void seek(long ms) {
 		if (player != null) {
 			event.fireSeekStarted();
 			try {
 				player.seek(ms);
 			} catch (IOException e) {
 				log.error("Error seeking", e);
 				stop();
 			} finally {
 				event.fireSeekFinished();
 			}
 		}
 	}
 
 	/** If we are playing, pause. If we are paused, play. Otherwise, do nothing */
 	public synchronized void togglePlayPause() {
 		if (player == null)
 			return;
 		switch (status) {
 		case Paused:
 			play(currentStreamId);
 			break;
 		case Playing:
 			pause();
 			break;
 		}
 	}
 
 	public void stop() {
 		String stoppedStreamId;
 		synchronized (this) {
 			if (player != null)
 				player.stop();
 			player = null;
 			status = Status.Stopped;
 			stoppedStreamId = currentStreamId;
 			currentStreamId = null;
 		}
 		tracks.notifyPlayingTrackChange(stoppedStreamId);
 		event.firePlaybackStopped();
 	}
 
 	public void onCompletion() {
 		synchronized (this) {
 			player = null;
 			status = Status.Stopped;
 		}
 		log.debug("Finished playback");
 		String justFinStreamId = currentStreamId;
 		tracks.notifyPlayingTrackChange(justFinStreamId);
 		event.firePlaybackCompleted();
 	}
 
 	public void onError(String error) {
 		String errStreamId = currentStreamId;
 		String myErr = "Got playback error while playing " + errStreamId + ": " + error;
 		log.debug(myErr);
 		// If we are downloading this, stop at once, it's fux0red
 		DownloadingTrack d = rbnb.getDbService().getDownload(errStreamId);
 		if (d != null) {
 			try {
 				download.deleteDownload(errStreamId);
 			} catch (RobonoboException e) {
 				log.error("Caught exception while stopping download", e);
 			}
 		}
 		synchronized (this) {
 			currentStreamId = null;
 			player = null;
 		}
 		event.fireTrackUpdated(errStreamId);
 		event.firePlaybackError(myErr);
 	}
 
 	public synchronized void stopIfCurrentlyPlaying(String streamId) {
 		if (player != null && currentStreamId.equals(streamId))
 			stop();
 	}
 
 	public String getName() {
 		return "Playback service";
 	}
 
 	public String getProvides() {
 		return "core.playback";
 	}
 
 	public void onProgress(long microsecs) {
 		// The player calls this with microsecs=0 when we pause, just ignore it
 		if (microsecs == 0)
 			return;
 		// This might be null if we are a left-over thread, just exit
 		if (currentStreamId == null)
 			return;
 		event.firePlaybackProgress(microsecs);
 	}
 }
