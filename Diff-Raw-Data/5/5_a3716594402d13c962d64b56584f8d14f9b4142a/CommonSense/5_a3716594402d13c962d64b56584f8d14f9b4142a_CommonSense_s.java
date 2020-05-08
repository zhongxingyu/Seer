 package nl.sense_os.commonsense.common.client.communication;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.util.Md5Hasher;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.user.client.Window.Location;
 
 public class CommonSense {
 
 	private static class Api {
 		private static final boolean IS_LIVE = GWT.getModuleBaseURL()
 				.contains("common.sense-os.nl");
 		private static final boolean IS_RC = GWT.getModuleBaseURL().contains("rc.sense-os.nl");
 		private static final boolean IS_DEV = GWT.getModuleBaseURL().contains("dev.sense-os.nl");
 		private static final String PATH_PREFIX = IS_LIVE || IS_RC || IS_DEV ? "api/" : "";
 		public static final String HOST = IS_LIVE ? "common.sense-os.nl" : IS_RC ? "rc.sense-os.nl"
 				: IS_DEV ? "dev.sense-os.nl" : "api.sense-os.nl";
 		public static final String PROTOCOL = "http";
 		public static final String PATH_SENSORS = PATH_PREFIX + "sensors";
 		public static final String PATH_DATA = PATH_PREFIX + "sensors/%1/data";
 		@SuppressWarnings("unused")
 		public static final String PATH_DATA_POINT = PATH_PREFIX + "sensors/%1/data/%2";
 		public static final String PATH_CURRENT_USER = PATH_PREFIX + "users/current";
 		public static final String PATH_FORGOT_PASSWORD = PATH_PREFIX + "requestPasswordReset";
 		public static final String PATH_LOGIN = PATH_PREFIX + "login";
		public static final String PATH_LOGIN_GOOGLE = PATH_PREFIX + "login/openID/google";
 		public static final String PATH_LOGOUT = PATH_PREFIX + "logout";
 		public static final String PATH_PW_RESET = PATH_PREFIX + "resetPassword";
 		public static final String PATH_GROUPS = PATH_PREFIX + "groups";
 		public static final String PATH_GROUP_USERS = PATH_PREFIX + "groups/%1/users";
 
 		private Api() {
 			// do not instantiate
 		}
 	}
 
 	private static final Logger LOG = Logger.getLogger(CommonSense.class.getName());
 	private static final String JSON_TYPE = "application/json";
 	private static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
 
 	/**
 	 * @param callback
 	 *            RequestCallback to handle HTTP response
 	 * @param username
 	 * @param email
 	 */
 	public static void forgotPassword(RequestCallback callback, String username, String email) {
 
 		// prepare request details
 		Method method = RequestBuilder.POST;
 		String url = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_FORGOT_PASSWORD).buildString();
 
 		// prepare request data
 		String requestData = null != username ? "username=" + URL.encode(username) : "email="
 				+ URL.encode(email);
 
 		// send request
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
 			builder.setHeader("Accept", JSON_TYPE);
 			builder.sendRequest(requestData, callback);
 		} catch (Exception e) {
 			LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
 			callback.onError(null, e);
 		}
 	}
 
 	/**
 	 * @param callback
 	 *            RequestCallback to handle HTTP response
 	 */
 	public static void getCurrentUser(RequestCallback callback) {
 
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			callback.onError(null, new Exception("Not logged in"));
 		}
 
 		Method method = RequestBuilder.GET;
 		String url = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_CURRENT_USER).buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 
 	/**
 	 * @param callback
 	 *            RequestCallback to handle HTTP response
 	 * @param sensorId
 	 *            ID of the sensor to request data for
 	 * @param startDate
 	 * @param endDate
 	 * @param date
 	 * @param perPage
 	 * @param page
 	 * @param interval
 	 * @param next
 	 * @param last
 	 */
 	public static void getSensorData(RequestCallback callback, int sensorId, String startDate,
 			String endDate, String date, String perPage, String page, String interval, String next,
 			String last, String sort) {
 
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			callback.onError(null, new Exception("Not logged in"));
 		}
 
 		// prepare request properties
 		Method method = RequestBuilder.GET;
 		UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_DATA.replace("%1", Integer.toString(sensorId)));
 		if (null != startDate) {
 			urlBuilder.setParameter("start_date", startDate);
 		}
 		if (null != endDate) {
 			urlBuilder.setParameter("end_date", endDate);
 		}
 		if (null != date) {
 			urlBuilder.setParameter("date", date);
 		}
 		if (null != perPage) {
 			urlBuilder.setParameter("per_page", perPage);
 		}
 		if (null != page) {
 			urlBuilder.setParameter("page", page);
 		}
 		if (null != interval) {
 			urlBuilder.setParameter("interval", interval);
 		}
 		if (null != last) {
 			urlBuilder.setParameter("last", last);
 		}
 		if (null != next) {
 			urlBuilder.setParameter("next", next);
 		}
 		if (null != sort) {
 			urlBuilder.setParameter("sort", sort);
 		}
 		String url = urlBuilder.buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 
 	/**
 	 * @param callback
 	 *            RequestCallback to handle HTTP response
 	 * @param perPage
 	 * @param page
 	 * @param shared
 	 * @param owned
 	 * @param physical
 	 * @param details
 	 */
 	public static void getSensors(RequestCallback callback, String perPage, String page,
 			String shared, String owned, String physical, String details) {
 
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			callback.onError(null, new Exception("Not logged in"));
 		}
 
 		// prepare request properties
 		Method method = RequestBuilder.GET;
 		UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_SENSORS);
 		if (null != page) {
 			urlBuilder.setParameter("page", page);
 		}
 		if (null != perPage) {
 			urlBuilder.setParameter("per_page", perPage);
 		}
 		if (null != shared) {
 			urlBuilder.setParameter("shared", shared);
 		}
 		if (null != owned) {
 			urlBuilder.setParameter("owned", owned);
 		}
 		if (null != physical) {
 			urlBuilder.setParameter("physical", physical);
 		}
 		if (null != details) {
 			urlBuilder.setParameter("details", details);
 		}
 		String url = urlBuilder.buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 
 	/**
 	 * Tries to connect the current user account with his or her Google account
 	 */
 	public static void googleConnect() {
 
 		// get the session ID
 		String sessionId = SessionManager.getSessionId();
 
 		// set the current location as the callback
 		UrlBuilder callbackBuilder = new UrlBuilder();
 		callbackBuilder.setProtocol(Location.getProtocol());
 		callbackBuilder.setHost(Location.getHost());
 		callbackBuilder.setPath(Location.getPath());
 		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
 			if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
 				// do not include these parameters
 			} else {
 				callbackBuilder.setParameter(entry.getKey(), entry.getValue()
 						.toArray(new String[0]));
 			}
 		}
 		String callbackUrl = callbackBuilder.buildString();
 
 		// relocate to OpenID handler
 		Location.replace(new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
 				.setParameter("session_id", sessionId).buildString());
 	}
 
 	/**
 	 * Relocates the browser to the Google Open ID login page.
 	 */
 	public static void googleLogin() {
 
 		// set the current location as the callback
 		UrlBuilder callbackBuilder = new UrlBuilder();
 		callbackBuilder.setProtocol(Location.getProtocol());
 		callbackBuilder.setHost(Location.getHost());
 		callbackBuilder.setPath(Location.getPath());
 		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
 			if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
 				// do not include these parameters
 			} else {
 				callbackBuilder.setParameter(entry.getKey(), entry.getValue()
 						.toArray(new String[0]));
 			}
 		}
 		String callbackUrl = callbackBuilder.buildString();
 
 		// relocate to OpenID handler
		Location.replace(new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
 				.buildString());
 	}
 
 	/**
 	 * @param callback
 	 *            RequestCallback to handle HTTP response
 	 * @param username
 	 *            Username
 	 * @param password
 	 *            Password (unhashed)
 	 */
 	public static void login(RequestCallback callback, String username, String password) {
 		// prepare request properties
 		Method method = RequestBuilder.POST;
 		UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_LOGIN);
 		String url = urlBuilder.buildString();
 
 		// prepare request data
 		String hashedPass = Md5Hasher.hash(password);
 		String requestData = "username=" + username + "&password=" + hashedPass;
 
 		// send request
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
 			builder.setHeader("Accept", JSON_TYPE);
 			builder.sendRequest(requestData, callback);
 		} catch (Exception e) {
 			LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
 			callback.onError(null, e);
 		}
 	}
 
 	/**
 	 * @param callback
 	 */
 	public static void logout(RequestCallback callback) {
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 
 		Method method = RequestBuilder.GET;
 		String url = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_LOGOUT).buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 
 	public static void resetPassword(RequestCallback callback, String password, String token) {
 
 		// prepare request details
 		Method method = RequestBuilder.POST;
 		String url = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_PW_RESET).buildString();
 
 		// prepare request data
 		String hashedPass = Md5Hasher.hash(password);
 		String data = "password=" + hashedPass + "&token=" + token;
 
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
 			builder.setHeader("Accept", JSON_TYPE);
 			builder.sendRequest(data, callback);
 		} catch (Exception e) {
 			LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
 			callback.onError(null, e);
 		}
 	}
 
 	private static void sendRequest(Method method, String url, String sessionId, String data,
 			RequestCallback callback) {
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			if (null != sessionId) {
 				builder.setHeader("X-SESSION_ID", sessionId);
 			}
 			builder.setHeader("Accept", JSON_TYPE);
 			builder.sendRequest(data, callback);
 		} catch (Exception e) {
 			LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
 			callback.onError(null, e);
 		}
 	}
 
 	public static void getGroups(RequestCallback callback, String perPage, String page) {
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			callback.onError(null, new Exception("Not logged in"));
 		}
 
 		// prepare request properties
 		Method method = RequestBuilder.GET;
 		UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_GROUPS);
 		if (null != page) {
 			urlBuilder.setParameter("page", page);
 		}
 		if (null != perPage) {
 			urlBuilder.setParameter("per_page", perPage);
 		}
 		String url = urlBuilder.buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 
 	public static void getGroupUsers(RequestCallback callback, int groupId, String perPage,
 			String page) {
 		// check if there is a session ID
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			callback.onError(null, new Exception("Not logged in"));
 		}
 
 		// prepare request properties
 		Method method = RequestBuilder.GET;
 		UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Api.PROTOCOL).setHost(Api.HOST)
 				.setPath(Api.PATH_GROUP_USERS.replace("%1", Integer.toString(groupId)));
 		if (null != page) {
 			urlBuilder.setParameter("page", page);
 		}
 		if (null != perPage) {
 			urlBuilder.setParameter("per_page", perPage);
 		}
 		String url = urlBuilder.buildString();
 
 		// send request
 		sendRequest(method, url, sessionId, null, callback);
 	}
 }
