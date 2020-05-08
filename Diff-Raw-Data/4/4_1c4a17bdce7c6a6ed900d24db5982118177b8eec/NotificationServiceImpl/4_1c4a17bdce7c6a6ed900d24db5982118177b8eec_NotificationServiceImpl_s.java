 package com.robonobo.midas;
 
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.robonobo.core.api.model.Playlist;
 import com.robonobo.midas.dao.*;
 import com.robonobo.midas.model.*;
 
 @Service("notification")
 public class NotificationServiceImpl implements NotificationService {
 	private static final int ONE_HOUR_IN_MS = 60 * 60 * 1000;
 	static final String WEEKLY = "weekly";
 	static final String DAILY = "daily";
 	static final String IMMEDIATE = "immediate";
 	static final String NONE = "none";
 	static final Pattern LIBRARY_ITEM_PAT = Pattern.compile("^library:(\\d+)$");
 	static final Pattern PLAYLIST_ITEM_PAT = Pattern.compile("^playlist:(\\d+)$");
 	@Autowired
 	MessageService message;
 	@Autowired
 	NotificationDao notifDao;
 	@Autowired
 	UserDao userDao;
 	@Autowired
 	PlaylistDao playlistDao;
 	@Autowired
 	UserConfigDao userCfgDao;
 	@Autowired
 	CommentDao commentDao;
 	Log log = LogFactory.getLog(getClass());
 
 	@Override
 	public void newComment(MidasComment c) throws IOException {
 		Set<Long> sentUids = new HashSet<Long>();
 		MidasUser commentUser = userDao.getById(c.getUserId());
 		// Send notification to owner of parent comment
 		if (c.getParentId() > 0) {
 			MidasComment par = commentDao.getComment(c.getParentId());
 			if (par.getUserId() != c.getUserId()) {
 				MidasUserConfig muc = userCfgDao.getUserConfig(par.getUserId());
 				String creStr = muc.getItem("commentReplyEmails");
 				boolean cre = (creStr == null) ? true : Boolean.valueOf(creStr);
 				if (cre) {
 					MidasUser origUser = userDao.getById(par.getUserId());
 					Matcher pm = PLAYLIST_ITEM_PAT.matcher(c.getResourceId());
 					if (pm.matches()) {
 						MidasPlaylist p = playlistDao.getPlaylistById(Long.parseLong(pm.group(1)));
 						message.sendReplyNotificationForPlaylist(origUser, commentUser, p);
 						sentUids.add(origUser.getUserId());
 					} else {
 						Matcher lm = LIBRARY_ITEM_PAT.matcher(c.getResourceId());
 						if (lm.matches()) {
 							message.sendReplyNotificationForLibrary(origUser, commentUser, Long.parseLong(lm.group(1)));
 							sentUids.add(origUser.getUserId());
 						}
 					}
 				}
 			}
 		}
 		// Send notification to owners of resource
 		Matcher pm = PLAYLIST_ITEM_PAT.matcher(c.getResourceId());
 		if (pm.matches()) {
 			MidasPlaylist p = playlistDao.getPlaylistById(Long.parseLong(pm.group(1)));
 			for (Long ownerId : p.getOwnerIds()) {
 				if (sentUids.contains(ownerId))
 					continue;
 				MidasUserConfig muc = userCfgDao.getUserConfig(ownerId);
 				String pceStr = muc.getItem("playlistCommentEmails");
 				boolean pce = (pceStr == null) ? true : Boolean.valueOf(pceStr);
 				if (pce) {
 					MidasUser owner = userDao.getById(ownerId);
 					message.sendCommentNotificationForPlaylist(owner, commentUser, p);
 					sentUids.add(ownerId);
 				}
 			}
 		} else {
 			Matcher lm = LIBRARY_ITEM_PAT.matcher(c.getResourceId());
 			long ownerId = Long.parseLong(lm.group(1));
 			if (!sentUids.contains(ownerId)) {
 				MidasUserConfig muc = userCfgDao.getUserConfig(ownerId);
 				String pceStr = muc.getItem("playlistCommentEmails");
 				boolean pce = (pceStr == null) ? true : Boolean.valueOf(pceStr);
 				if (pce) {
 					MidasUser owner = userDao.getById(ownerId);
 					message.sendCommentNotificationForLibrary(owner, commentUser);
 					sentUids.add(ownerId);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void playlistUpdated(MidasUser owner, MidasPlaylist p) {
 		if (p.getVisibility().equals(Playlist.VIS_ME))
 			return;
 		for (long friendId : owner.getFriendIds()) {
 			String pref = getUpdateFreq(friendId);
 			if (pref.equals(IMMEDIATE)) {
 				MidasUser friend = userDao.getById(friendId);
 				try {
 					message.sendPlaylistNotification(owner, friend, p);
 				} catch (IOException e) {
 					log.error("Caught exception sending playlist notification", e);
 				}
 			} else if (!pref.equals(NONE))
 				notifDao.saveNotification(new MidasNotification(owner.getUserId(), friendId, "playlist:" + p.getPlaylistId()));
 		}
 	}
 
 	@Override
 	public void addedToLibrary(MidasUser user, int numTrax) {
 		// We don't actually send library notifications immediately as there might well be more coming in down the pipe
 		// - instead we send them every hour
 		for (long friendId : user.getFriendIds()) {
 			if (!getUpdateFreq(friendId).equals(NONE))
 				notifDao.saveNotification(new MidasNotification(user.getUserId(), friendId, "library:" + numTrax));
 		}
 	}
 
 	/** Send our collected library updates every hour */
 	@Scheduled(fixedRate = ONE_HOUR_IN_MS)
 	@Transactional
 	public void sendPseudoImmediateNotifications() {
 		Map<Long, List<MidasNotification>> nMap = getAllNotificationsByNotifUser();
 		int numSent = 0;
 		for (Long notifUid : nMap.keySet()) {
 			if (!getUpdateFreq(notifUid).equals(IMMEDIATE))
 				continue;
 			Map<Long, List<MidasNotification>> notsBySource = mapNotsByUpdateUser(nMap.get(notifUid));
 			updateUser: for (Long updateUid : notsBySource.keySet()) {
 				// Only send if they're all over an hour old, otherwise they're still adding to their library
 				int numTrax = 0;
 				for (MidasNotification n : notsBySource.get(updateUid)) {
 					if (msElapsedSince(n.getDate()) < ONE_HOUR_IN_MS)
 						continue updateUser;
 					Matcher m = LIBRARY_ITEM_PAT.matcher(n.getItem());
 					if (!m.matches())
 						continue;
 					numTrax += Integer.parseInt(m.group(1));
 				}
 				MidasUser updateUser = userDao.getById(updateUid);
 				MidasUser notifyUser = userDao.getById(notifUid);
 				try {
 					message.sendLibraryNotification(updateUser, notifyUser, numTrax);
 				} catch (IOException e) {
 					log.error("Caught exception when sending library notification", e);
 					continue;
 				}
 				numSent++;
 				notifDao.deleteNotifications(notsBySource.get(updateUid));
 			}
 		}
 		if (numSent > 0)
 			log.debug("Sending " + numSent + " 'immediate' notifications");
 	}
 
 	/** Send daily notifications at 0900 GMT */
 	@Scheduled(cron = "0 0 9 * * *")
 	@Transactional
 	public void sendDailyNotifications() {
 		Map<Long, List<MidasNotification>> nMap = getAllNotificationsByNotifUser();
 		Iterator<Entry<Long, List<MidasNotification>>> it = nMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry<Long, List<MidasNotification>> en = it.next();
 			if (!getUpdateFreq(en.getKey()).equals(DAILY))
 				it.remove();
 		}
 		log.info("Sending daily notifications to " + nMap.size() + " users");
 		sendCombinedNotifications(nMap);
 	}
 
 	/** Send weekly notifications at 0915 GMT on Friday */
 	@Scheduled(cron = "0 15 9 * * 5")
 	@Transactional
 	public void sendWeeklyNotifications() {
 		Map<Long, List<MidasNotification>> nMap = getAllNotificationsByNotifUser();
 		Iterator<Entry<Long, List<MidasNotification>>> it = nMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry<Long, List<MidasNotification>> en = it.next();
 			if (!getUpdateFreq(en.getKey()).equals(WEEKLY))
 				it.remove();
 		}
 		log.info("Sending weekly notifications to " + nMap.size() + " users");
 		sendCombinedNotifications(nMap);
 	}
 
 	private void sendCombinedNotifications(Map<Long, List<MidasNotification>> nots) {
 		for (Long notUid : nots.keySet()) {
 			MidasUser notUser = userDao.getById(notUid);
 			// For each user we're notifying, group the nots together by the updating user
 			Map<Long, List<MidasNotification>> notsByUpdateUid = mapNotsByUpdateUser(nots.get(notUid));
 			Map<MidasUser, Integer> libTraxAdded = new HashMap<MidasUser, Integer>();
 			Map<Long, List<Playlist>> playlists = new HashMap<Long, List<Playlist>>();
 			for (Long updateUid : notsByUpdateUid.keySet()) {
 				MidasUser updateUser = userDao.getById(updateUid);
 				int added = 0;
 				Map<Long, Playlist> pm = new HashMap<Long, Playlist>();
 				for (MidasNotification n : notsByUpdateUid.get(updateUid)) {
 					Matcher m = LIBRARY_ITEM_PAT.matcher(n.getItem());
 					if (m.matches())
 						added += Integer.parseInt(m.group(1));
 					else {
 						m = PLAYLIST_ITEM_PAT.matcher(n.getItem());
 						if (m.matches()) {
 							Long plId = Long.parseLong(m.group(1));
 							if(pm.containsKey(plId))
 								continue;
 							Playlist p = playlistDao.getPlaylistById(plId);
 							if (p != null) {
 								// Don't send notifications for radio playlists
 								if(p.getTitle().equalsIgnoreCase("radio"))
 									continue;
 								pm.put(plId, p);
 							}
 						}
 					}
 				}
 				if (added > 0 || pm.size() > 0) {
 					libTraxAdded.put(updateUser, added);
 					playlists.put(updateUid, new ArrayList<Playlist>(pm.values()));
 				}
 			}
 			if (libTraxAdded.size() > 0 || playlists.size() > 0) {
 				try {
 					message.sendCombinedNotification(notUser, libTraxAdded, playlists);
 				} catch (IOException e) {
 					log.error("Caught exception sending combined notification to " + notUser.getEmail());
 					continue;
 				}
 			}
 			notifDao.deleteAllNotificationsTo(notUid);
 		}
 	}
 
 	private Map<Long, List<MidasNotification>> mapNotsByUpdateUser(List<MidasNotification> nList) {
 		Map<Long, List<MidasNotification>> result = new HashMap<Long, List<MidasNotification>>();
 		for (MidasNotification n : nList) {
 			long updateUid = n.getUpdateUserId();
 			if (!result.containsKey(updateUid))
 				result.put(updateUid, new ArrayList<MidasNotification>());
 			result.get(updateUid).add(n);
 		}
 		return result;
 	}
 
 	private Map<Long, List<MidasNotification>> getAllNotificationsByNotifUser() {
 		List<MidasNotification> nList = notifDao.getAllNotifications();
 		Map<Long, List<MidasNotification>> result = new HashMap<Long, List<MidasNotification>>();
 		for (MidasNotification n : nList) {
 			if (!result.containsKey(n.getNotifUserId()))
 				result.put(n.getNotifUserId(), new ArrayList<MidasNotification>());
 			result.get(n.getNotifUserId()).add(n);
 		}
 		return result;
 	}
 
 	private String getUpdateFreq(long userId) {
 		// By default, we update weekly - if they haven't set their preference yet, use that
 		MidasUserConfig muc = userCfgDao.getUserConfig(userId);
 		if (muc == null)
 			return WEEKLY;
 		String result = muc.getItem("playlistUpdateEmails");
 		if (result == null)
 			return WEEKLY;
 		return result;
 	}
 }
