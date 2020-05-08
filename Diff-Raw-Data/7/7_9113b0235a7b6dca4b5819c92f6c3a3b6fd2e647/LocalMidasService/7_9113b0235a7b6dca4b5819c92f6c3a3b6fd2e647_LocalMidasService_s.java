 package com.robonobo.midas;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.apache.commons.collections.MapIterator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.model.Library;
 import com.robonobo.core.api.model.Playlist;
 import com.robonobo.midas.dao.*;
 import com.robonobo.midas.model.*;
 import com.robonobo.remote.service.MidasService;
 import com.twmacinta.util.MD5;
 
 import static com.robonobo.common.util.TimeUtil.*;
 
 @Service("midas")
 public class LocalMidasService implements MidasService {
 	@Autowired
 	private AppConfig appConfig;
 	@Autowired
 	private FacebookService facebook;
 	@Autowired
 	private TwitterService twitter;
 	@Autowired
 	private FriendRequestDao friendRequestDao;
 	@Autowired
 	private InviteDao inviteDao;
 	@Autowired
 	private LibraryDao libraryDao;
 	@Autowired
 	private PlaylistDao playlistDao;
 	@Autowired
 	private StreamDao streamDao;
 	@Autowired
 	private UserConfigDao userConfigDao;
 	@Autowired
 	private UserDao userDao;
 	Log log = LogFactory.getLog(getClass());
 
 	private long lastPlaylistId = -1;
 
 	@Transactional(readOnly = true)
 	public List<MidasUser> getAllUsers() {
 		return userDao.getAll();
 	}
 
 	public MidasUser getUserByEmail(String email) {
 		return userDao.getByEmail(email);
 	}
 
 	public MidasUser getUserById(long userId) {
 		return userDao.getById(userId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasUser createUser(MidasUser user) {
 		user.setVerified(true);
 		user.setUpdated(now());
 		return userDao.create(user);
 	}
 
 	public MidasUser getUserAsVisibleBy(MidasUser targetU, MidasUser requestor) {
 		// If this the user asking for themselves, give them everything. If
 		// they're a friend, they get public playlists, but no friends.
 		// Otherwise, they
 		// get a null object
 		MidasUser result;
 		if (targetU.equals(requestor)) {
 			result = new MidasUser(targetU);
 		} else if (targetU.getFriendIds().contains(requestor.getUserId())) {
 			result = new MidasUser(targetU);
 			result.setPassword(null);
 			result.getFriendIds().clear();
 			result.setInvitesLeft(0);
 			Iterator<Long> iter = result.getPlaylistIds().iterator();
 			while (iter.hasNext()) {
 				Playlist p = playlistDao.loadPlaylist(iter.next());
 				if (p.getVisibility().equals(Playlist.VIS_ME))
 					iter.remove();
 			}
 		} else
 			result = null;
 
 		return result;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void saveUser(MidasUser user) {
 		userDao.save(user);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteUser(long userId) {
 		MidasUser u = userDao.getById(userId);
 		// Go through all their playlists - if they are the only owner, delete it, otherwise remove them from the owners
 		// list
 		for (Long plId : u.getPlaylistIds()) {
 			MidasPlaylist p = playlistDao.loadPlaylist(plId);
 			p.getOwnerIds().remove(userId);
 			if (p.getOwnerIds().size() == 0)
 				playlistDao.deletePlaylist(p);
 			else
 				playlistDao.savePlaylist(p);
 		}
 		// Go through all their friends, delete this user from their friendids
 		for (long friendId : u.getFriendIds()) {
 			MidasUser friend = userDao.getById(friendId);
 			friend.getFriendIds().remove(userId);
 			userDao.save(friend);
 		}
 		// Delete any pending friend requests to this user
 		List<MidasFriendRequest> frs = friendRequestDao.retrieveByRequestee(userId);
 		for (MidasFriendRequest fr : frs) {
 			friendRequestDao.delete(fr);
 		}
 		// Finally, delete the user itself
 		userDao.delete(u);
 	}
 
 	public MidasPlaylist getPlaylistById(long playlistId) {
 		return playlistDao.loadPlaylist(playlistId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	@Override
 	public MidasPlaylist newPlaylist(MidasPlaylist playlist) {
 		if (playlist.getPlaylistId() > 0)
 			throw new SeekInnerCalmException("newPlaylist called with non-new playlist!");
 		long newPlaylistId;
 		synchronized (this) {
 			if (lastPlaylistId <= 0)
 				lastPlaylistId = playlistDao.getHighestPlaylistId();
 			if (lastPlaylistId == Long.MAX_VALUE)
 				throw new SeekInnerCalmException("playlist ids wrapped!"); // Unlikely
 			else
 				lastPlaylistId++;
 			newPlaylistId = lastPlaylistId;
 		}
 		playlist.setPlaylistId(newPlaylistId);
 		savePlaylist(playlist);
 		// Announce the playlist to facebook unless it's private
 		if (!playlist.getVisibility().equals(Playlist.VIS_ME)) {
 			long userId = (Long) playlist.getOwnerIds().toArray()[0];
 			MidasUserConfig muc = userConfigDao.getUserConfig(userId);
 			try {
 				facebook.postPlaylistCreateToFacebook(muc, playlist);
 			} catch (IOException e) {
 				log.error("Error posting playlist create to facebook", e);
 			}
 		}
 		// Only announce public playlists to twitter
 		if(playlist.getVisibility().equals(Playlist.VIS_ALL)) {
 			long userId = (Long) playlist.getOwnerIds().toArray()[0];
 			MidasUserConfig muc = userConfigDao.getUserConfig(userId);
 			twitter.postPlaylistCreateToTwitter(muc, playlist);
 		}
 		return playlist;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void savePlaylist(MidasPlaylist playlist) {
 		if (playlist.getPlaylistId() <= 0)
 			throw new SeekInnerCalmException("playlist id is not set");
 		playlistDao.savePlaylist(playlist);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deletePlaylist(MidasPlaylist playlist) {
 		playlistDao.deletePlaylist(playlist);
 	}
 
 	public MidasStream getStreamById(String streamId) {
 		return streamDao.loadStream(streamId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void saveStream(MidasStream stream) {
 		streamDao.saveStream(stream);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteStream(MidasStream stream) {
 		streamDao.deleteStream(stream);
 	}
 
 	public Long countUsers() {
 		return userDao.getUserCount();
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasInvite createOrUpdateInvite(String email, MidasUser friend, MidasPlaylist pl) {
 		MidasInvite result = inviteDao.retrieveByEmail(email);
 		if (result == null) {
 			// New invite
 			result = new MidasInvite();
 			result.setEmail(email);
 			result.setInviteCode(generateEmailCode(email));
 		}
 		result.getFriendIds().add(friend.getUserId());
 		result.getPlaylistIds().add(pl.getPlaylistId());
 		result.setUpdated(now());
 		inviteDao.save(result);
 		return result;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasFriendRequest createOrUpdateFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist pl) {
 		MidasFriendRequest result = friendRequestDao.retrieveByUsers(requestor.getUserId(), requestee.getUserId());
 		if (result == null) {
 			// New friend request
 			result = new MidasFriendRequest();
 			result.setRequestorId(requestor.getUserId());
 			result.setRequesteeId(requestee.getUserId());
 			result.setRequestCode(generateEmailCode(requestee.getEmail()));
 		}
 		result.getPlaylistIds().add(pl.getPlaylistId());
 		result.setUpdated(now());
 		friendRequestDao.save(result);
 		return result;
 	}
 
 	public MidasFriendRequest getFriendRequest(String requestCode) {
 		return friendRequestDao.retrieveByRequestCode(requestCode);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public String acceptFriendRequest(MidasFriendRequest req) {
 		MidasUser requestor = userDao.getById(req.getRequestorId());
 		if (requestor == null)
 			return "Requesting user " + req.getRequestorId() + " not found";
 		MidasUser requestee = userDao.getById(req.getRequesteeId());
 		if (requestee == null)
 			return "Requested user " + req.getRequesteeId() + " not found";
 		for (Long plId : req.getPlaylistIds()) {
 			MidasPlaylist p = playlistDao.loadPlaylist(plId);
 			p.getOwnerIds().add(requestee.getUserId());
 			playlistDao.savePlaylist(p);
 			requestee.getPlaylistIds().add(plId);
 		}
 		requestor.getFriendIds().add(requestee.getUserId());
 		userDao.save(requestor);
 		requestee.getFriendIds().add(requestor.getUserId());
 		userDao.save(requestee);
 		friendRequestDao.delete(req);
 		return null;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void ignoreFriendRequest(MidasFriendRequest request) {
 		friendRequestDao.delete(request);
 	}
 
 	public List<MidasFriendRequest> getPendingFriendRequests(long userId) {
 		return friendRequestDao.retrieveByRequestee(userId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteInvite(String inviteCode) {
 		MidasInvite invite = inviteDao.retrieveByInviteCode(inviteCode);
 		if (invite != null)
 			inviteDao.delete(invite);
 	}
 
 	public MidasInvite getInvite(String inviteCode) {
 		return inviteDao.retrieveByInviteCode(inviteCode);
 	}
 
 	@Override
 	public Library getLibrary(MidasUser u, Date since) {
 		Library lib = libraryDao.getLibrary(u.getUserId());
 		if (lib == null)
 			return null;
 		lib.setUserId(u.getUserId());
 		if (since != null) {
 			Iterator<Entry<String, Date>> it = lib.getTracks().entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Date> e = it.next();
 				if (since.after(e.getValue()))
 					it.remove();
 			}
 		}
 		return lib;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	@Override
 	public void putLibrary(Library lib) {
 		libraryDao.saveLibrary(lib);
 	}
 
 	@Override
 	public MidasUserConfig getUserConfig(MidasUser u) {
 		MidasUserConfig result = userConfigDao.getUserConfig(u.getUserId());
 		if (result == null) {
 			result = new MidasUserConfig();
 			result.setUserId(u.getUserId());
 		}
 		return result;
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public void putUserConfig(MidasUserConfig newCfg) {
 		// Update friends based on facebook & twitter deets
 		MidasUser mu = userDao.getById(newCfg.getUserId());
 		MidasUserConfig oldCfg = userConfigDao.getUserConfig(newCfg.getUserId());
 		facebook.updateFriends(mu, oldCfg, newCfg);
 		// TODO twitter
 		userConfigDao.saveUserConfig(newCfg);
 	}
 
 	private String generateEmailCode(String email) {
 		MD5 hash = new MD5();
 		hash.Update(email);
 		hash.Update(getDateFormat().format(now()));
 		return hash.asHex();
 	}
 }
