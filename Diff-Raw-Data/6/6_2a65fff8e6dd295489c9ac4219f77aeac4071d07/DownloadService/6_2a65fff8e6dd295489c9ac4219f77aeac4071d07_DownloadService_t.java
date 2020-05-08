 package com.robonobo.core.service;
 
 import static com.robonobo.common.util.FileUtil.*;
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.io.File;
 import java.util.*;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.StreamVelocity;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.api.model.DownloadingTrack.DownloadStatus;
 import com.robonobo.core.metadata.StreamCallback;
 import com.robonobo.core.storage.StorageService;
 import com.robonobo.mina.external.*;
 import com.robonobo.mina.external.buffer.PageBuffer;
 import com.robonobo.mina.external.buffer.PageBufferListener;
 
 /** Responsible for translating mina Receptions to robonobo Downloads, and keeping track of not-yet-started Downloads
  * (which have no associated Reception)
  * 
  * @author macavity */
 public class DownloadService extends AbstractService implements MinaListener, PageBufferListener {
 	static final int PRIORITY_CURRENT = Integer.MAX_VALUE;
 	static final int PRIORITY_NEXT = Integer.MAX_VALUE - 1;
 	Log log = LogFactory.getLog(getClass());
 	private DbService db;
 	private MinaControl mina;
 	private StreamService streams;
 	private StorageService storage;
 	private ShareService share;
 	private EventService event;
 	private PlaybackService playback;
 	private Set<String> downloadSids;
 	private Set<String> preFetchStreamIds;
 	/** Saves downloads in the order they are added, to make sure they're downloaded in order */
 	private List<String> dPriority = new ArrayList<String>();
 	/** Don't fire the track updated event after receiving a page more than once per sec, to avoid spamming the gui with
 	 * local downloads */
 	private Map<String, Date> lastFiredPageEvent = new HashMap<String, Date>();
 	/** Make sure that we don't add two downloads with the same start time, to ensure they're restored from the db in the
 	 * right order. So, keep track of the last download time, and if it hasn't increased, add 1ms to ensure uniqueness */
 	long lastDlStartTime = 0;
 	private File downloadsDir;
 	ScheduledFuture<?> checkDownloadsTask;
 
 	public DownloadService() {
 		addHardDependency("core.mina");
 		addHardDependency("core.streams");
 		addHardDependency("core.storage");
 		addHardDependency("core.shares");
 	}
 
 	@Override
 	public void startup() throws Exception {
 		db = rbnb.getDbService();
 		mina = rbnb.getMina();
 		mina.addMinaListener(this);
 		streams = rbnb.getStreamService();
 		storage = rbnb.getStorageService();
 		share = rbnb.getShareService();
 		event = rbnb.getEventService();
 		playback = rbnb.getPlaybackService();
 		downloadSids = new HashSet<String>();
 		preFetchStreamIds = new HashSet<String>();
 		downloadsDir = new File(rbnb.getHomeDir(), "in-progress");
 		downloadsDir.mkdir();
 		// Downloads get started once trackservice is up and running
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		if (checkDownloadsTask != null)
 			checkDownloadsTask.cancel(true);
 		for (String streamId : db.getDownloadSids()) {
 			DownloadingTrack dl = getDownload(streamId);
 			stopDownload(dl);
 		}
 	}
 
 	public void startAllDownloads() throws RobonoboException {
 		// We could taskify this but we're unlikely to have enough downloads to make it worth it
 		rbnb.getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				int numStarted = 0;
 				// This list is in order of date started
 				List<String> dlSidList = db.getDownloadSids();
 				for (String sid : dlSidList){ 
 					DownloadingTrack d = db.getDownload(sid);
 					if (d.getDownloadStatus() == DownloadStatus.Finished) {
 						db.deleteDownload(sid);
 						continue;
 					}
 					PageBuffer pb = storage.getPageBuf(sid);
 					if(pb == null) {
 						// Should never happen, but perhaps we got kill-9d at the wrong point
 						log.error("No pagebuffer for download "+sid+" - deleting");
 						db.deleteDownload(sid);
 						continue;
 					}
 					d.setPageBuf(pb);
 					// If we died or got kill-9d at the wrong point, we might be 100%
 					// finished downloading - turn it into a share here, or it'll never
 					// get added
 					if (pb.isComplete()) {
 						d.setDownloadStatus(DownloadStatus.Finished);
 						share.addShareFromCompletedDownload(d);
 						db.deleteDownload(sid);
 						continue;
 					}
 					synchronized (dPriority) {
 						dPriority.add(d.getStream().getStreamId());
 					}
 					synchronized(this) {
 						downloadSids.add(sid);
 					}
 					if (numStarted < rbnb.getConfig().getMaxRunningDownloads()) {
 						startDownload(d, pb);
 						numStarted++;
 					} else {
 						d.setDownloadStatus(DownloadStatus.Paused);
 						db.putDownload(d);
 						log.debug("Queuing download for " + sid);
 					}
 					rbnb.getEventService().fireTrackUpdated(sid);
 				}
 				updatePriorities();
 				if (rbnb.getConfig().getDownloadCheckFreq() > 0) {
 					int freqMs = rbnb.getConfig().getDownloadCheckFreq();
 					checkDownloadsTask = rbnb.getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							startMoreDownloads();
 						}
 					}, freqMs, freqMs, TimeUnit.SECONDS);
 				}
 			}
 		});
 	}
 
 	public void addDownload(String sid) throws RobonoboException {
 		Stream s = streams.getKnownStream(sid);
 		if (s != null)
 			addDownload(s);
 		else {
 			streams.fetchStreams(Arrays.asList(sid), new StreamCallback() {
 				public void success(Stream s) {
 					try {
 						addDownload(s);
 					} catch (RobonoboException e) {
 						log.error("Error adding download for stream " + s.getStreamId(), e);
 					}
 				}
 
 				public void error(String sid, Exception e) {
 					log.error("Error fetching stream for downloading " + sid, e);
 				}
 			});
 		}
 	}
 
 	private void addDownload(Stream s) throws RobonoboException {
 		String sid = s.getStreamId();
 		// If we have a defunct share, replace it with this download
 		rbnb.getShareService().nukeDefunctShare(sid);
 		File dataFile = new File(downloadsDir, makeFileNameSafe(sid));
 		log.info("Adding download for " + sid);
 		DownloadingTrack d = new DownloadingTrack(s, dataFile, DownloadStatus.Paused);
 		long startTime = System.currentTimeMillis();
 		synchronized (this) {
 			if (startTime == lastDlStartTime)
 				startTime++;
 			lastDlStartTime = startTime;
 		}
 		d.setDateAdded(new Date(startTime));
 		try {
 			PageBuffer pb = storage.createPageBufForDownload(s, dataFile);
 			if (numRunningDownloads() < rbnb.getConfig().getMaxRunningDownloads())
 				startDownload(d, pb);
 		} catch (Exception e) {
 			log.error("Caught exception when starting download for " + sid, e);
 			storage.nukePageBuf(sid);
 			throw new RobonoboException(e);
 		}
 		db.putDownload(d);
 		synchronized (dPriority) {
 			dPriority.add(sid);
 		}
 		updatePriorities();
 		synchronized (this) {
 			downloadSids.add(sid);
 		}
 		event.fireTrackUpdated(sid);
 		event.fireMyLibraryUpdated();
 	}
 
 	public void deleteDownload(String streamId) throws RobonoboException {
 		log.info("Deleting download for stream " + streamId);
 		playback.stopForDeletedStream(streamId);
 		mina.stopReception(streamId);
 		db.deleteDownload(streamId);
 		// If we have started sharing this stream, don't nuke the pagebuf
 		if (db.getShare(streamId) == null)
 			storage.nukePageBuf(streamId);
 		synchronized (dPriority) {
 			dPriority.remove(streamId);
 		}
 		updatePriorities();
 		synchronized (this) {
 			downloadSids.remove(streamId);
 		}
 		event.fireTrackUpdated(streamId);
 		event.fireMyLibraryUpdated();
 		startMoreDownloads();
 	}
 
 	/** Delete in a batch to avoid starting downloads you're about to delete */
 	public void deleteDownloads(Collection<String> sids) throws RobonoboException {
 		for (String sid : sids) {
 			log.info("Deleting download for stream " + sid);
 			playback.stopForDeletedStream(sid);
 			mina.stopReception(sid);
 			db.deleteDownload(sid);
 			// If we have started sharing this stream, don't nuke the pagebuf
 			if (db.getShare(sid) == null)
 				storage.nukePageBuf(sid);
 			// TODO This two-way sync is nasty, refactor to use locks or something clearer
 			synchronized (dPriority) {
 				dPriority.remove(sid);
 			}
 			synchronized (this) {
 				downloadSids.remove(sid);
 			}
 			event.fireTrackUpdated(sid);
 		}
 		updatePriorities();
 		event.fireMyLibraryUpdated();
 		startMoreDownloads();
 	}
 
 	public void pauseDownload(String streamId) {
 		log.debug("Pausing download for " + streamId);
		DownloadingTrack d = getDownload(streamId);
		if(d == null) {
			log.error("Asked to pause download for "+streamId+", but there is no such download");
			return;
		}
 		try {
 			stopDownload(d);
 		} catch (Exception e) {
 			log.error("Caught exception when pausing download for " + streamId, e);
 		}
 		d.setDownloadStatus(DownloadStatus.Paused);
 		db.putDownload(d);
 		event.fireTrackUpdated(streamId);
 	}
 
 	public void startDownload(String streamId) throws RobonoboException {
 		DownloadingTrack d = db.getDownload(streamId);
 		if (d == null)
 			throw new SeekInnerCalmException();
 		if (d.getDownloadStatus() == DownloadStatus.Finished) {
 			log.debug("Not starting finished download " + streamId);
 			return;
 		}
 		if (d.getDownloadStatus() == DownloadStatus.Downloading) {
 			log.debug("Not starting already-running download " + streamId);
 			return;
 		}
 		PageBuffer pb = storage.getPageBuf(streamId);
 		d.setPageBuf(pb);
 		startDownload(d, pb);
 		d.setDownloadStatus(DownloadStatus.Downloading);
 		db.putDownload(d);
 		event.fireTrackUpdated(streamId);
 	}
 
 	private void startDownload(DownloadingTrack d, PageBuffer pb) throws RobonoboException {
 		log.debug("Starting download for " + d.getStream().getStreamId());
 		d.setPageBuf(pb);
 		pb.addListener(this);
 		mina.startReception(d.getStream().getStreamId(), StreamVelocity.LowestCost);
 		if (d.getDownloadStatus() != DownloadStatus.Downloading) {
 			d.setDownloadStatus(DownloadStatus.Downloading);
 			db.putDownload(d);
 		}
 	}
 
 	private void stopDownload(DownloadingTrack d) throws Exception {
 		mina.stopReception(d.getStream().getStreamId());
 		d.getPageBuf().close();
 		event.fireTrackUpdated(d.getStream().getStreamId());
 		synchronized (this) {
 			lastFiredPageEvent.remove(d.getStream().getStreamId());
 		}
 	}
 
 	private int numRunningDownloads() {
 		return db.numRunningDownloads();
 	}
 
 	public DownloadingTrack getDownload(String streamId) {
 		synchronized (this) {
 			if (!downloadSids.contains(streamId))
 				return null;
 		}
 		DownloadingTrack d = rbnb.getDbService().getDownload(streamId);
 		if (d != null) {
 			d.setNumSources(mina.numSources(streamId));
 			d.setPageBuf(storage.getPageBuf(streamId));
 		}
 		return d;
 	}
 
 	public void receptionCompleted(String streamId) {
 		Stream s = streams.getKnownStream(streamId);
 		DownloadingTrack d = db.getDownload(streamId);
 		if (d == null) {
 			log.error("ERROR: no download for completed stream " + s.getStreamId());
 			return;
 		}
 		try {
 			d.setPageBuf(storage.getPageBuf(streamId));
 		} catch (Exception e) {
 			log.error("Error stopping completed download", e);
 		}
 		d.setDownloadStatus(DownloadStatus.Finished);
 		synchronized (dPriority) {
 			dPriority.remove(streamId);
 			preFetchStreamIds.remove(streamId);
 		}
 		updatePriorities();
 		synchronized (this) {
 			downloadSids.remove(streamId);
 			lastFiredPageEvent.remove(streamId);
 		}
 		try {
 			// Start sharing this one
 			share.addShareFromCompletedDownload(d);
 		} catch (Exception e) {
 			log.error("Error pausing download or starting share", e);
 		}
 		db.deleteDownload(streamId);
 		startMoreDownloads();
 	}
 
 	private void startMoreDownloads() {
 		int activeDls = 0;
 		List<DownloadingTrack> dls = new ArrayList<DownloadingTrack>();
 		synchronized (dPriority) {
 			for (String streamId : dPriority) {
 				DownloadingTrack d = getDownload(streamId);
 				dls.add(d);
 				if (d.getDownloadStatus() == DownloadStatus.Downloading && d.getDownloadRate() > 0)
 					activeDls++;
 			}
 		}
 		int numToStart = rbnb.getConfig().getMaxRunningDownloads() - activeDls;
 		if (numToStart > 0) {
 			for (DownloadingTrack d : dls) {
 				if (d.getDownloadStatus() == DownloadStatus.Paused) {
 					try {
 						startDownload(d.getStream().getStreamId());
 						numToStart--;
 					} catch (RobonoboException e) {
 						log.error("Error starting download", e);
 					}
 				}
 				if (numToStart == 0)
 					break;
 			}
 		}
 	}
 
 	public void receptionConnsChanged(String streamId) {
 		// Do nothing
 	}
 
 	public void preFetch(String streamId) throws RobonoboException {
 		DownloadingTrack d = getDownload(streamId);
 		if (d == null) {
 			addDownload(streamId);
 			d = getDownload(streamId);
 		}
 		try {
 			if (d.getDownloadStatus() == DownloadStatus.Paused)
 				startDownload(streamId);
 		} catch (RobonoboException e) {
 			log.error("Caught exception starting pre-fetch download for stream " + streamId, e);
 			return;
 		}
 		synchronized (dPriority) {
 			preFetchStreamIds.add(streamId);
 		}
 		updatePriorities();
 	}
 
 	public void updatePriorities() {
 		mina.clearStreamPriorities();
 		// Currently playing/paused stream has priority
 		String curStreamId = playback.getCurrentStreamId();
 		synchronized (dPriority) {
 			for (int i = 0; i < dPriority.size(); i++) {
 				String streamId = dPriority.get(i);
 				int newPri = dPriority.size() - i;
 				if (preFetchStreamIds.contains(streamId))
 					newPri = PRIORITY_NEXT;
 				if (curStreamId != null && curStreamId.equals(streamId))
 					newPri = PRIORITY_CURRENT;
 				mina.setStreamPriority(streamId, newPri);
 			}
 		}
 	}
 
 	public String getName() {
 		return "Download Service";
 	}
 
 	public String getProvides() {
 		return "core.downloads";
 	}
 
 	public void nodeConnected(ConnectedNode node) {
 	}
 
 	public void nodeDisconnected(ConnectedNode node) {
 	}
 
 	@Override
 	public void advisedOfTotalPages(PageBuffer pb) {
 	}
 
 	@Override
 	public void gotPage(PageBuffer pb, long pageNum) {
 		synchronized (this) {
 			Date lastEvt = lastFiredPageEvent.get(pb.getStreamId());
 			if (lastEvt != null && msElapsedSince(lastEvt) < 1000)
 				return;
 			lastFiredPageEvent.put(pb.getStreamId(), now());
 		}
 		event.fireTrackUpdated(pb.getStreamId());
 	}
 }
