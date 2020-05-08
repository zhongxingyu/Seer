 package org.smartsnip.client;
 
 import org.smartsnip.core.ISessionObserver;
 import org.smartsnip.core.Notification;
 import org.smartsnip.core.Session;
 
 import com.google.gwt.user.client.Cookies;
 
 /**
  * The controller according to a MVC pattern.
  * 
  * The controller is implemented as Singleton. Each call must be in a static way
  * 
  */
 public class Controller {
 	/** ID of the cookie that stores the session ID */
 	private final static String COOKIE_SESSION = "smartsnip_SID";
 
 	/**
 	 * Observer of the session
 	 * 
 	 */
 	private static final ISessionObserver sessionObserver = new ISessionObserver() {
 
 		@Override
 		public void notification(Session source, Notification notification) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void logout(Session source) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void login(Session source, String username) {
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	/** Static constructor initialises the singleton instance */
 	static {
 		// Currently empty
 	}
 
 	/** Private constructor for Singleton pattern */
 	private Controller() {
 	}
 
 	// zuständig für kommunikation mit dem server
 	// verwaltet die GUI
 
 	/**
 	 * Gets the ID of the current session
 	 * 
 	 * @return the session ID of the current session
 	 */
 	static String getSessionID() {
 		String sid = Cookies.getCookie(COOKIE_SESSION);
 		if (sid == null) sid = createNewSession();
 
 		return sid;
 	}
 
 	/**
 	 * Creates a new session
 	 * 
 	 * @return the session id of the newly generated session
 	 */
 	private static String createNewSession() {
		return Session.createNewSession().getCookie();
 	}
 
 	/**
 	 * Checks if the current session is logged in
 	 * 
 	 * @return true if the current session is logged in, otherwise false
 	 */
 	static boolean isLoggedIn() {
 		if (!hasSession()) return false;
 		return getSession().isLoggedIn();
 	}
 
 	/**
 	 * Checks if the active client has an attached session
 	 * 
 	 * @return true if a session is available, otherwise false
 	 */
 	static boolean hasSession() {
 		String sid = Cookies.getCookie(COOKIE_SESSION);
 		if (sid == null) return false;
 
 		// TODO Check if the session is alive ...
 
 		return true;
 	}
 
 	/**
 	 * Gets the session of the current client or creates one, if currently no
 	 * avaliable. If created the newly created session will be returned.
 	 * 
 	 * @return the attached session of the current client
 	 */
 	private static Session getSession() {
 		return Session.getSession(getSessionID());
 	}
 
 	/**
 	 * Gets the username of the currently logged in user. If the current session
 	 * does not exists, or no user is logged in, "guest" is returned.
 	 * 
 	 * @return the username of the currently logged in user
 	 */
 	public static String getUsername() {
 		if (!isLoggedIn()) return "guest";
 		return getSession().getUsername();
 	}
 
 	/**
 	 * Logs the current session out
 	 */
 	public static void logout() {
 		if (!isLoggedIn()) return;
 		getSession().logout();
 	}
 }
