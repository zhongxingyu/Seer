 package com.robonobo.core.service;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.locks.*;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.concurrent.Timeout;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.util.TextUtil;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.Task;
 import com.robonobo.core.api.model.*;
 import com.robonobo.core.metadata.*;
 import com.robonobo.core.metadata.AbstractMetadataService.RequestFetchOrder;
 
 public class PlaylistService extends AbstractService {
 	static final String[] SPECIAL_PLAYLIST_NAMES = { "Loves", "Radio" };
 	Map<Long, Playlist> playlists = new HashMap<Long, Playlist>();
 	Map<String, Long> myPlaylistIdsByTitle = new HashMap<String, Long>();
 	Set<Long> forceUpdatePlaylists = new HashSet<Long>();
 	TaskService tasks;
 	DbService db;
 	AbstractMetadataService metadata;
 	EventService events;
 	StreamService streams;
 	TrackService tracks;
 	CommentService comments;
 	Timeout postLovesTimeout;
 	boolean lovesToPost = false;
 	Lock postLovesLock;
 	Condition finalLovesPosted;
 
 	public PlaylistService() {
 		addHardDependency("core.db");
 		addHardDependency("core.metadata");
 		addHardDependency("core.events");
 		addHardDependency("core.tasks");
 		addHardDependency("core.streams");
 		addHardDependency("core.tracks");
 		addHardDependency("core.comments");
 	}
 
 	@Override
 	public String getName() {
 		return "Playlist Service";
 	}
 
 	@Override
 	public String getProvides() {
 		return "core.playlists";
 	}
 
 	@Override
 	public void startup() throws Exception {
 		tasks = rbnb.getTaskService();
 		db = rbnb.getDbService();
 		metadata = rbnb.getMetadataService();
 		events = rbnb.getEventService();
 		streams = rbnb.getStreamService();
 		tracks = rbnb.getTrackService();
 		comments = rbnb.getCommentService();
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		if(lovesToPost) {
 			// Set up our monitor so we can wait for the loves to be posted (otherwise the metadata service gets shut down before they can be)
 			postLovesLock = new ReentrantLock();
 			finalLovesPosted = postLovesLock.newCondition();
 			postLovesNow();
 			postLovesLock.lock();
 			try {
 				log.info("Waiting for loves to be posted before shutting down");
 				finalLovesPosted.await();
 				log.info("Loves posted, continuing shutdown");
 			} finally {
 				postLovesLock.unlock();
 			}
 		}
 	}
 
 	public void clearPlaylists() {
 		synchronized (this) {
 			playlists.clear();
 			myPlaylistIdsByTitle.clear();
 		}
 	}
 
 	public void refreshMyPlaylists(User me) {
 		tasks.runTask(new RefreshMyPlaylistsTask(me.getPlaylistIds()));
 	}
 
 	public void refreshFriendPlaylists(Set<Long> plIds) {
 		tasks.runTask(new RefreshFriendPlaylistsTask(plIds));
 	}
 
 	public boolean isSpecialPlaylist(String title) {
 		for (String specName : SPECIAL_PLAYLIST_NAMES) {
 			if (specName.equalsIgnoreCase(title))
 				return true;
 		}
 		return false;
 	}
 
 	abstract class PlaylistFetcher implements PlaylistCallback {
 		Set<Long> waitingForPlaylists = new HashSet<Long>();
 		Set<String> waitingForStreams = new HashSet<String>();
 		Set<Long> plIds;
 		int streamsToFetch;
 
 		public PlaylistFetcher(Set<Long> plIds) {
 			this.plIds = plIds;
 			waitingForPlaylists.addAll(plIds);
 		}
 
 		@Override
 		public void success(Playlist p) {
 			long plId = p.getPlaylistId();
 			boolean mine = rbnb.getUserService().getMyUser().getPlaylistIds().contains(p.getPlaylistId());
 			synchronized (PlaylistService.this) {
 				Playlist oldP = playlists.get(plId);
 				if (oldP == null || p.getUpdated().after(oldP.getUpdated())) {
 					playlists.put(plId, p);
 					if (mine) {
 						if (oldP != null && !oldP.getTitle().equals(p.getTitle()))
 							myPlaylistIdsByTitle.remove(oldP.getTitle());
 						myPlaylistIdsByTitle.put(p.getTitle(), plId);
 					}
 					waitingForStreams.addAll(p.getStreamIds());
 				}
 			}
 			playlistUpdated(plId);
 		}
 
 		@Override
 		public void error(long plId, Exception ex) {
 			log.error("Got error fetching playlist id " + plId, ex);
 			playlistUpdated(plId);
 		}
 
 		private void playlistUpdated(long upPlId) {
 			waitingForPlaylists.remove(upPlId);
 			if (waitingForPlaylists.size() == 0) {
 				// We've got all our playlists - now get the streams (if any)
 				streamsToFetch = waitingForStreams.size();
 				onStreamUpdate(0, streamsToFetch);
 				if (streamsToFetch > 0) {
 					for (Long plId : plIds) {
 						Playlist p;
 						synchronized (PlaylistService.this) {
 							p = playlists.get(plId);
 						}
 						if (p != null) {
 							if (p.getStreamIds().size() == 0)
 								finishedFetchingPlaylist(p);
 							else
 								streams.fetchStreams(p.getStreamIds(), new StreamFetcher(p, this));
 						}
 					}
 				} else {
					// No streams to fetch - check for comments
 					for (long plId : plIds) {
						comments.fetchCommentsForPlaylist(plId);
 					}
 				}
 			}
 		}
 
 		void gotStream(String sid) {
 			if (!waitingForStreams.remove(sid))
 				return;
 			int streamsLeft = waitingForStreams.size();
 			int streamsDone = streamsToFetch - streamsLeft;
 			onStreamUpdate(streamsDone, streamsToFetch);
 		}
 
 		void onStreamUpdate(int done, int total) {
 			// Default does nothing
 		}
 	}
 
 	// We make sure we get streamhandler callbacks so that we can know when we're done
 	abstract class PlaylistsRefreshTask extends Task {
 		PlaylistFetcher fetcher;
 		private Set<Long> plIds;
 
 		public PlaylistsRefreshTask(Set<Long> plIds) {
 			this.plIds = plIds;
 			title = titleName();
 			fetcher = new PlaylistFetcher(plIds) {
 				void onStreamUpdate(int done, int total) {
 					if (done == total) {
 						completion = 1f;
 						statusText = "Done.";
 					} else {
 						completion = ((float) done) / total;
 						statusText = "Fetching details of playlist track " + (done + 1) + " of " + total;
 					}
 					fireUpdated();
 					if (completion == 1)
 						onCompletion();
 				}
 			};
 		}
 
 		abstract String titleName();
 
 		abstract void onCompletion();
 
 		@Override
 		public void runTask() throws Exception {
 			log.debug("Running playlists refresh task with plids " + plIds);
 			if(plIds.size() == 0) {
 				statusText = "Done";
 				completion = 1f;
 				fireUpdated();
 				onCompletion();
 				return;
 			}
 			statusText = "Fetching playlist details";
 			fireUpdated();
 			metadata.fetchPlaylists(plIds, fetcher);
 		}
 	}
 
 	class RefreshMyPlaylistsTask extends PlaylistsRefreshTask {
 		public RefreshMyPlaylistsTask(Set<Long> plIds) {
 			super(plIds);
 		}
 
 		String titleName() {
 			return "Refreshing my playlists";
 		}
 
 		void onCompletion() {
 			// If we don't have our special playlists, create them
 			List<String> createPls = new ArrayList<String>();
 			synchronized (this) {
 				for (String specName : SPECIAL_PLAYLIST_NAMES) {
 					if (!myPlaylistIdsByTitle.containsKey(specName))
 						createPls.add(specName);
 				}
 			}
 			for (String plName : createPls) {
 				Playlist p = new Playlist();
 				p.setTitle(plName);
 				createPlaylist(p, null, true);
 			}
 			// Now we're done with my playlists, we can load our friends
 			rbnb.getUserService().fetchFriends();
 		}
 	}
 
 	class RefreshFriendPlaylistsTask extends PlaylistsRefreshTask {
 		public RefreshFriendPlaylistsTask(Set<Long> plIds) {
 			super(plIds);
 		}
 
 		String titleName() {
 			return "Refreshing friends' playlists";
 		}
 
 		void onCompletion() {
 			// Now we've done all users and playlists - tell our metadata service to load stuff in parallel now to avoid
 			// requests getting stuck behind all our friends' libraries loading
 			metadata.setFetchOrder(RequestFetchOrder.Parallel);
 			rbnb.getLibraryService().updateFriendLibraries();
 			rbnb.getUserService().usersAndPlaylistsLoaded = true;
 			rbnb.getEventService().fireAllUsersAndPlaylistsLoaded();
 		}
 	}
 
 	class StreamFetcher implements StreamCallback {
 		Playlist p;
 		Set<String> waitingForSids = new HashSet<String>();
 		PlaylistFetcher pFetcher;
 
 		public StreamFetcher(Playlist p, PlaylistFetcher task) {
 			this.p = p;
 			waitingForSids.addAll(p.getStreamIds());
 			this.pFetcher = task;
 		}
 
 		@Override
 		public void success(Stream s) {
 			update(s.getStreamId());
 		}
 
 		@Override
 		public void error(String streamId, Exception ex) {
 			log.error("Exception fetching stream " + streamId, ex);
 			update(streamId);
 		}
 
 		private void update(String sid) {
 			waitingForSids.remove(sid);
 			if (waitingForSids.size() == 0)
 				finishedFetchingPlaylist(p);
 			if (pFetcher != null)
 				pFetcher.gotStream(sid);
 		}
 	}
 
 	private void finishedFetchingPlaylist(Playlist p) {
 		// We've loaded all our streams
 		long plId = p.getPlaylistId();
 		log.warn("Finished fetching playlist " + plId);
 		events.firePlaylistChanged(p);
 		downloadTracksIfNecessary(p);
 		comments.fetchCommentsForPlaylist(plId);
 	}
 
 	public void checkPlaylistUpdate(long plId) {
 		Set<Long> plIds = new HashSet<Long>();
 		plIds.add(plId);
 		PlaylistFetcher fetcher = new PlaylistFetcher(plIds) {
 		};
 		metadata.fetchPlaylist(plId, fetcher);
 	}
 
 	public void updatePlaylist(Playlist p) {
 		log.debug("Updating playlist " + p.getPlaylistId());
 		synchronized (this) {
 			Playlist oldP = playlists.get(p.getPlaylistId());
 			if (oldP != null && myPlaylistIdsByTitle.containsKey(oldP.getTitle()) && !oldP.getTitle().equals(p.getTitle())) {
 				myPlaylistIdsByTitle.remove(oldP.getTitle());
 				myPlaylistIdsByTitle.put(p.getTitle(), p.getPlaylistId());
 			}
 			playlists.put(p.getPlaylistId(), p);
 		}
 		db.markAllAsSeen(p);
 		events.firePlaylistChanged(p);
 		metadata.updatePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.info("Updated playlist id " + newP.getPlaylistId() + " successfully");
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error updating playlist id " + playlistId, ex);
 			}
 		});
 	}
 
 	public void createPlaylist(Playlist p, final PlaylistCallback handler) {
 		createPlaylist(p, handler, false);
 	}
 
 	private void createPlaylist(Playlist p, final PlaylistCallback handler, boolean allowSpecialName) {
 		// If necessary, change the playlist title so it doesn't conflict with our special playlists
 		if (!allowSpecialName) {
 			synchronized (this) {
 				for (String specName : SPECIAL_PLAYLIST_NAMES) {
 					if (specName.equalsIgnoreCase(p.getTitle())) {
 						int i = 1;
 						do {
 							p.setTitle(specName + "-" + i++);
 						} while (myPlaylistIdsByTitle.containsKey(p.getTitle()));
 					}
 				}
 			}
 		}
 		log.debug("Creating new playlist with title " + p.getTitle());
 		metadata.updatePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.debug("Successfully created new playlist id " + newP.getPlaylistId());
 				synchronized (PlaylistService.this) {
 					playlists.put(newP.getPlaylistId(), newP);
 					myPlaylistIdsByTitle.put(newP.getTitle(), newP.getPlaylistId());
 				}
 				db.markAllAsSeen(newP);
 				// Fire our user as updated first, then the playlist
 				rbnb.getUserService().playlistCreated(newP);
 				events.firePlaylistChanged(newP);
 				if (handler != null)
 					handler.success(newP);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error creating playlist", ex);
 				if (handler != null)
 					handler.error(playlistId, ex);
 			}
 		});
 	}
 
 	public void love(Collection<String> sids) {
 		if (log.isDebugEnabled()) {
 			StringBuffer sb = new StringBuffer("Loving ");
 			sb.append(TextUtil.numItems(sids, "track"));
 			sb.append(": ");
 			for (String sid : sids) {
 				sb.append(sid).append(" ");
 			}
 			log.debug(sb);
 		}
 		Playlist loves;
 		synchronized (this) {
 			Long lovePlid = myPlaylistIdsByTitle.get("Loves");
 			loves = playlists.get(lovePlid);
 		}
 		if (loves == null) {
 			log.error("No loves playlist!");
 			return;
 		}
 		// Can only love something once
 		boolean haveNew = false;
 		for (String sid : sids) {
 			if (!loves.getStreamIds().contains(sid)) {
 				loves.getStreamIds().add(sid);
 				haveNew = true;
 			}
 		}
 		if (!haveNew) {
 			log.debug("Not adding duplicate tracks to loves");
 			return;
 		}
 		lovesToPost = true;
 		UserConfig uc = rbnb.getUserService().getMyUserConfig();
 		// Are we posting now or later?
 		String cfg = uc.getItem("postLoves");
 		if (cfg == null || cfg.equalsIgnoreCase("together")) {
 			// Post our loves together - set our timeout
 			if (postLovesTimeout == null) {
 				postLovesTimeout = new Timeout(rbnb.getExecutor(), new CatchingRunnable() {
 					public void doRun() throws Exception {
 						postLovesNow();
 					}
 				});
 			}
 			postLovesTimeout.set(rbnb.getConfig().getPostLovesDelayMins() * 60 * 1000);
 			// Fire playlist as updated so that UI shows new loves
 			events.firePlaylistChanged(loves);
 		} else
 			postLovesNow();
 	}
 
 	private void postLovesNow() {
 		if (postLovesTimeout != null)
 			postLovesTimeout.cancel();
 		if(!lovesToPost)
 			return;
 		lovesToPost = false;
 		Playlist lovesPl;
 		synchronized (this) {
 			Long lovePlid = myPlaylistIdsByTitle.get("Loves");
 			lovesPl = playlists.get(lovePlid);
 		}
 		
 		log.debug("Posting loves now (playlist id "+lovesPl.getPlaylistId()+")");
 		db.markAllAsSeen(lovesPl);
 		events.firePlaylistChanged(lovesPl);
 		metadata.updatePlaylist(lovesPl, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.info("Updated loves (playlist id " + newP.getPlaylistId() + ") successfully");
 				signalLovesPosted();
 			}
 			
 			public void error(long playlistId, Exception ex) {
 				log.error("Error updating loves (playlist id " + playlistId+")", ex);
 				signalLovesPosted();
 			}
 
 			private void signalLovesPosted() {
 				if(postLovesLock != null) {
 					postLovesLock.lock();
 					try {
 						finalLovesPosted.signal();
 					} finally {
 						postLovesLock.unlock();
 					}
 				}
 			}
 		});
 	}
 
 	public boolean lovingAll(Collection<String> sids) {
 		Playlist loves;
 		synchronized (this) {
 			Long lovePlid = myPlaylistIdsByTitle.get("Loves");
 			loves = playlists.get(lovePlid);
 		}
 		if (loves == null) {
 			log.error("No loves playlist!");
 			return false;
 		}
 		return loves.getStreamIds().containsAll(sids);
 	}
 
 	public void addToRadio(String streamId) {
 		addToRadio(Arrays.asList(streamId));
 	}
 
 	public void addToRadio(final Collection<String> sids) {
 		String radioCfg = rbnb.getUserService().getMyUserConfig().getItem("radioPlaylist");
 		if (radioCfg == null || radioCfg.equalsIgnoreCase("auto")) {
 			rbnb.getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					if (log.isDebugEnabled()) {
 						StringBuffer sb = new StringBuffer("Adding ");
 						sb.append(TextUtil.numItems(sids, "track"));
 						sb.append(" to radio: ");
 						for (String sid : sids) {
 							sb.append(sid).append(" ");
 						}
 						log.debug(sb);
 					}
 					Playlist radioP = null;
 					synchronized (PlaylistService.this) {
 						Long plId = myPlaylistIdsByTitle.get("Radio");
 						if (plId != null)
 							radioP = playlists.get(plId);
 					}
 					if (radioP == null) {
 						log.error("No radio playlist for added track");
 						return;
 					}
 					List<String> playlistSids = radioP.getStreamIds();
 					for (String sid : sids) {
 						// If we already have this track in our radio list, remove it, and re-add it again at the end
 						int idx = playlistSids.indexOf(sid);
 						if (idx >= 0)
 							playlistSids.remove(idx);
 						playlistSids.add(sid);
 					}
 					while (playlistSids.size() > rbnb.getConfig().getRadioMaxTracksAuto())
 						playlistSids.remove(0);
 					updatePlaylist(radioP);
 				}
 			});
 		}
 	}
 
 	/** Update things that need to be updated on playlists containing this track we're now sharing */
 	public void checkPlaylistsForNewShare(SharedTrack sh) {
 		// Currently, just sync to itunes
 		final List<Playlist> affectedPs = new ArrayList<Playlist>();
 		synchronized (this) {
 			for (Playlist p : playlists.values()) {
 				if (p.getStreamIds().contains(sh.getStream().getStreamId()))
 					affectedPs.add(p);
 			}
 		}
 		if (affectedPs.size() > 0 && Platform.getPlatform().iTunesAvailable()) {
 			// Update itunes in another thread
 			rbnb.getExecutor().execute(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					for (Playlist p : affectedPs) {
 						PlaylistConfig pc = rbnb.getDbService().getPlaylistConfig(p.getPlaylistId());
 						if (shouldITunesSync(pc))
 							syncITunesIfNecessary(p);
 					}
 				}
 			});
 		}
 	}
 
 	public void playlistConfigUpdated(PlaylistConfig oldPc, PlaylistConfig newPc) {
 		long plId = newPc.getPlaylistId();
 		Playlist p;
 		synchronized (this) {
 			p = playlists.get(plId);
 		}
 		if (p == null) {
 			log.error("Playlist config updated for plid " + plId + " but there is no such playlist");
 			return;
 		}
 		downloadTracksIfNecessary(p);
 		// Only call to iTunes if necessary as this causes the iTunes program to pop open
 		if (shouldITunesSync(newPc) && !shouldITunesSync(oldPc))
 			syncITunesIfNecessary(p);
 	}
 
 	private void syncITunesIfNecessary(final Playlist p) {
 		try {
 			// If this is one of my playlists, put it under my details
 			User me = rbnb.getUserService().getMyUser();
 			if (p.getOwnerIds().contains(me.getUserId())) {
 				rbnb.getITunesService().syncPlaylist(me, p);
 				return;
 			} 
 			// Otherwise, use the first owner who's a friend of mine
 			for (Long ownerId : p.getOwnerIds()) {
 				if (me.getFriendIds().contains(ownerId)) {
 					User syncUser = rbnb.getUserService().getKnownUser(ownerId);
 					rbnb.getITunesService().syncPlaylist(syncUser, p);
 					return;
 				}
 			}
 			// Otherwise, use the first user we have details for
 			for (Long ownerId : p.getOwnerIds()) {
 				User syncUser = rbnb.getUserService().getKnownUser(ownerId);
 				if (syncUser != null) {
 					rbnb.getITunesService().syncPlaylist(syncUser, p);
 				}
 			}
 			// Look up the first owner and use them
 			long ownerId = p.getOwnerIds().iterator().next();
 			rbnb.getMetadataService().fetchUser(ownerId, new UserCallback() {
 				public void success(User u) {
 					try {
 						rbnb.getITunesService().syncPlaylist(u, p);
 					} catch (IOException e) {
 						log.error("Error syncing playlist id " + p.getPlaylistId() + " to itunes", e);
 					}
 				}
 
 				public void error(long userId, Exception e) {
 					log.error("Error syncing playlist id " + p.getPlaylistId() + " to itunes", e);
 				}
 			});
 		} catch (IOException e) {
 			log.error("Error syncing playlist id " + p.getPlaylistId() + " to itunes", e);
 		}
 	}
 
 	private boolean shouldITunesSync(PlaylistConfig pc) {
 		return "true".equalsIgnoreCase(pc.getItem("iTunesExport"));
 	}
 
 	private void downloadTracksIfNecessary(Playlist p) {
 		PlaylistConfig pc = db.getPlaylistConfig(p.getPlaylistId());
 		if (((pc != null) && "true".equalsIgnoreCase(pc.getItem("autoDownload")))) {
 			for (String sid : p.getStreamIds()) {
 				Track t = tracks.getTrack(sid);
 				try {
 					if (t instanceof CloudTrack)
 						rbnb.getDownloadService().addDownload(sid);
 				} catch (RobonoboException e) {
 					log.error("Error auto-downloading stream " + sid, e);
 				}
 			}
 		}
 	}
 
 	public synchronized Playlist getExistingPlaylist(long playlistId) {
 		return playlists.get(playlistId);
 	}
 
 	public void getOrFetchPlaylist(long playlistId, final PlaylistCallback handler) {
 		Playlist p = getExistingPlaylist(playlistId);
 		if (p != null) {
 			handler.success(p);
 			return;
 		}
 		metadata.fetchPlaylist(playlistId, new PlaylistCallback() {
 			public void success(Playlist p) {
 				synchronized (PlaylistService.this) {
 					playlists.put(p.getPlaylistId(), p);
 				}
 				handler.success(p);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				handler.error(playlistId, ex);
 			}
 		});
 	}
 
 	public synchronized Playlist getMyPlaylistByTitle(String title) {
 		Long plId = myPlaylistIdsByTitle.get(title);
 		if (plId == null)
 			return null;
 		return getExistingPlaylist(plId);
 	}
 
 	public void deletePlaylist(Playlist p) {
 		final long plId = p.getPlaylistId();
 		log.debug("Deleting playlist " + plId);
 		User me = rbnb.getUserService().getMyUser();
 		boolean firePlaylistUpdate = false;
 		synchronized (this) {
 			p = playlists.get(plId);
 			if (p == null)
 				throw new SeekInnerCalmException();
 			playlists.get(plId).getOwnerIds().remove(me.getUserId());
 			if (p.getOwnerIds().size() > 0)
 				firePlaylistUpdate = true;
 			else
 				playlists.remove(plId);
 		}
 		rbnb.getUserService().playlistDeleted(p);
 		if (firePlaylistUpdate)
 			events.firePlaylistChanged(p);
 		metadata.deletePlaylist(p, new PlaylistCallback() {
 			public void success(Playlist p) {
 				log.debug("Successfully deleted playlist " + plId);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error deleting playlist " + playlistId, ex);
 			}
 		});
 	}
 
 	public void sharePlaylist(final Playlist p, final Set<Long> friendIds, Set<String> emails) throws IOException, RobonoboException {
 		metadata.sharePlaylist(p, friendIds, emails, new PlaylistCallback() {
 			public void success(Playlist newP) {
 				log.debug("Successfully shared playlist " + p.getPlaylistId());
 				synchronized (PlaylistService.this) {
 					playlists.put(newP.getPlaylistId(), newP);
 				}
 				rbnb.getUserService().playlistShared(p, friendIds);
 				events.firePlaylistChanged(newP);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error sharing playlist " + playlistId, ex);
 			}
 		});
 	}
 
 	public void postPlaylistUpdateToService(final String service, final long playlistId, String msg) {
 		log.debug("Posting playlist update for playlist " + playlistId + " to service " + service);
 		metadata.postPlaylistUpdateToService(service, playlistId, msg, new PlaylistCallback() {
 			public void success(Playlist isnull) {
 				log.debug("Successfully posted playlist update for playlist " + playlistId + " to service " + service);
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error posting playlist update for playlist " + playlistId + " to service " + service, ex);
 			}
 		});
 	}
 }
