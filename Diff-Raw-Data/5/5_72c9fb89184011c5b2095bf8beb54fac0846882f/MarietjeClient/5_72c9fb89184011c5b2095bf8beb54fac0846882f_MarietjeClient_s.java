 /**
  * @licence GNU General Public licence http://www.gnu.org/copyleft/gpl.html
  * @Copyright (C) 2012 Thom Wiggers
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.marietjedroid.connect;
 
 import java.io.FileInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.Semaphore;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MarietjeClient extends Observable implements Observer {
 
 	private static final String DEFAULT_REQUESTER = "marietje";
 	/**
 	 * Locks until we have recieved tracks
 	 */
 	private final Semaphore tracksRetrieved = new Semaphore(0);
 	/**
 	 * Locks until we have retrieved a now playing track
 	 */
 	private final Semaphore playingRetrieved = new Semaphore(0);
 	/**
 	 * Blocks until we have retrieved requests
 	 */
 	private final Semaphore requestsRetrieved = new Semaphore(0);
 
 	/**
 	 * Blocks until we get answer from a login attempt
 	 */
 	private final Semaphore loginAttempt = new Semaphore(0);
 
 	private final Semaphore queryResults = new Semaphore(0);
 
 	private MarietjeClientChannel channel = null;
 
	private String accessKey = null;
 
 	Integer queryToken = 0;
 
 	private boolean followingPlaying = false;
 
 	private boolean followingQueue = false;
 
 	/**
 	 * Creates a new instance of a connection, immediately starts polling
 	 * Marietje
 	 * 
 	 * @param host
 	 * @param port
 	 * @param path
 	 * @throws MarietjeException
 	 */
 	public MarietjeClient(String host, int port, String path)
 			throws MarietjeException {
 		this.channel = new MarietjeClientChannel(this, host, port, path);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	Semaphore getTracksRetrievedSemaphore() {
 		return tracksRetrieved;
 	}
 
 	/**
 	 * @return the playingRetrieved
 	 */
 	Semaphore getPlayingRetrievedSemaphore() {
 		return playingRetrieved;
 	}
 
 	Semaphore getRequestsRetrievedSemaphore() {
 		return requestsRetrieved;
 	}
 
 	Semaphore getLoginAttemptSemaphore() {
 		return loginAttempt;
 	}
 
 	/**
 	 * Gets the queue Blocks until we have it
 	 * 
 	 * @return
 	 * @throws MarietjeException
 	 */
 	public MarietjeTrack[] getQueue() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 		ArrayList<MarietjeRequest> queue = new ArrayList<MarietjeRequest>();
 
 		try {
 			if (!this.followingQueue || this.channel.getRequests() == null) {
 				this.followQueue();
 				this.requestsRetrieved.acquire();
 
 			}
 
 			JSONArray requests = this.channel.getRequests();
 
 			for (int i = 0; requests.optJSONObject(i) != null; i++) {
 				JSONObject req = requests.getJSONObject(i);
 				JSONObject media = req.getJSONObject("media");
 				String requester = req.optString("byKey", DEFAULT_REQUESTER);
 				queue.add(new MarietjeRequest(requester, req.getInt("key"),
 						media));
 			}
 
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON Error");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return queue.toArray(new MarietjeTrack[0]);
 	}
 
 	/**
 	 * Start following queue updates
 	 * 
 	 * @throws MarietjeException
 	 */
 	public void followQueue() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 		try {
 			this.channel.sendMessage("{'type':'follow','which':['requests']}");
 			this.followingQueue = true;
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON Error");
 		}
 	}
 
 	/**
 	 * Stop following queue updates
 	 * 
 	 * @throws MarietjeException
 	 */
 	public void unfollowQueue() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 		this.followingQueue = false;
 		try {
 			this.channel.sendMessage(new JSONObject(
 					"{'type':'unfollow','which':['requests']}"));
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON error");
 		}
 	}
 
 	/**
 	 * locks when waiting for a track
 	 * 
 	 * @return the now playing track
 	 * @throws MarietjeException
 	 */
 	public MarietjePlaying getPlaying() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 		MarietjePlaying nowPlaying = null;
 		try {
 			if (!followingPlaying || this.channel.getNowPlaying() == null) {
 				this.followPlaying();
 				this.playingRetrieved.acquire();
 			}
 			JSONObject np = this.channel.getNowPlaying();
 			JSONObject media = np.getJSONObject("media");
 			double servertime = np.getDouble("serverTime");
 			double endtime = np.getDouble("endTime");
 			String byKey = np.getString("byKey");
 			nowPlaying = new MarietjePlaying(byKey, servertime, endtime, media);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON error");
 		}
 
 		return nowPlaying;
 	}
 
 	/**
 	 * Start following np updates
 	 * 
 	 * @throws MarietjeException
 	 */
 	public void followPlaying() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 		try {
 			this.channel.sendMessage("{'type':'follow','which':['playing']}");
 			this.followingPlaying = true;
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON error");
 		}
 
 	}
 
 	/**
 	 * Stop following the now playing updates
 	 * 
 	 * @throws MarietjeException
 	 */
 	public void unfollowNowPlaying() throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 		try {
 			this.channel
 					.sendMessage("{'type':'unfollow','which':'['playing']}");
 			this.followingPlaying = false;
 		} catch (JSONException e) {
 			throw new MarietjeException("JSON error");
 		}
 	}
 
 	/**
 	 * Logs in, or throws an exception.
 	 * 
 	 * @param username
 	 * @param password
 	 * @throws MarietjeException
 	 */
 	public void login(String username, String password)
 			throws MarietjeException {
 		synchronized (accessKey) {
 			if (channel.getException() != null)
 				throw channel.getException();
 
 			try {
 				this.channel.sendPriorityMessage("{'type':'request_login_token'}");
 				this.loginAttempt.acquire();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String token = this.channel.getLoginToken();
 			String hash = token.concat(password);
 			MessageDigest md5 = null;
 			try {
 				md5 = MessageDigest.getInstance("MD5");
 			} catch (NoSuchAlgorithmException e) {
 				throw new MarietjeException("Problem with MD5 Message digest");
 			}
 			hash = md5.digest(hash.getBytes()).toString();
 			try {
 				this.channel.sendPriorityMessage("{'type':'login', 'username':'"
 						+ username + "'," + "'hash':'" + hash + "'}");
 			} catch (JSONException e) {
 				throw new MarietjeException("Problem with the JSON feed");
 			}
 			try {
 				this.loginAttempt.acquire();
 				if (this.channel.getLoginError() != null) {
 					throw this.channel.getLoginError();
 				}
 				this.accessKey = this.channel.getAccessKey();
 
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Requests a track
 	 * 
 	 * @param trackid
 	 * @throws MarietjeException
 	 */
 	public void requestTrack(String trackid) throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
		if (this.accessKey == null)
 			throw new MarietjeException("You must log in");
 		try {
 			this.channel.sendMessage("{'type':'request','mediaKey':'" + trackid
 					+ "'}");
 			this.requestsRetrieved.acquire();
 			if (this.channel.getRequestError() != null) {
 				throw new MarietjeException(this.channel.getRequestError());
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Upload a track
 	 * 
 	 * 
 	 * locks
 	 * 
 	 * TODO
 	 * 
 	 * @param artist
 	 * @param title
 	 * @param f
 	 * @throws MarietjeException
 	 */
 	public void uploadTrack(String artist, String title, FileInputStream f)
 			throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 
 	}
 
 	/**
 	 * Zoeken
 	 * 
 	 * @param query
 	 * @return
 	 * @throws MarietjeException
 	 */
 	public MarietjeTrack[] search(String query) throws MarietjeException {
 		return this.search(query, 0, 10);
 	}
 
 	/**
 	 * Zoeken
 	 * 
 	 * @param query
 	 * @param skip
 	 * @param count
 	 * @return the list of tracks found, or null.
 	 * @throws MarietjeException
 	 */
 	public MarietjeTrack[] search(String query, int skip, int count)
 			throws MarietjeException {
 		if (channel.getException() != null)
 			throw channel.getException();
 		synchronized (queryToken) {
 			this.queryToken++;
 		}
 
 		try {
 			this.channel.sendPriorityMessage("{'type':'query_media', 'token':"
 					+ queryToken + ",'skip':" + skip + ",'count':" + count
 					+ ",'query':'" + query + "'}");
 			this.queryResults.acquire();
 			return this.channel.getQueryResults();
 		} catch (JSONException e) {
 			throw new MarietjeException("Problem with the JSON feed");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	Semaphore getQueryResultsRetrievedSemaphore() {
 		return this.queryResults;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	public void update(Observable o, Object arg) {
 		this.setChanged();
 		this.notifyObservers(arg);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Observable#addObserver(java.util.Observer)
 	 */
 	public void addObserver(Observer o) {
 		super.addObserver(o);
 	}
 
 }
