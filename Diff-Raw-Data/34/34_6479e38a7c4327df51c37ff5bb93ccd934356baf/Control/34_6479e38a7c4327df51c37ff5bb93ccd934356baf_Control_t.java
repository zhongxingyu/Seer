 package org.smartsnip.client;
 
 import org.smartsnip.client.GUI.Page;
 import org.smartsnip.shared.ISession;
 import org.smartsnip.shared.ISessionAsync;
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.ISnippetAsync;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XSnippet;
 import org.smartsnip.shared.XUser;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * The controller according to a MVC pattern.
  * 
  * The controller is implemented as Singleton. Each call must be in a static way
  * 
  * The controller is the main class in the client package. As the name says it
  * controls the GUI and on the other side it is responsible for the
  * communication with the server
  * 
  * @author Paul
  * @author Felix Niederwanger
  * 
  */
 public class Control implements EntryPoint {
 
	/**
	 * Path where the application is deployed, used only if not in development
	 * mode
	 */
	private static final String DEPLOY_PATH = "/Org_smartsnip";
	// private static final String DEPLOY_PATH = "/";

 	/** Base URL that should be before every resource */
 	public static final String baseURL = getBaseURL();
 
 	/** Stored session cookie */
 	private static String sessionCookie = "";
 
 	/** User currently logged in */
 	private final XUser user = new XUser("Guest", "noreal", "nomail");
 	/** Indicating if logged in */
 	private boolean loggedIn = false;
 
 	/** Singleton instance */
 	private static Control instance = null;
 	/** Session cookie */
 	private final static String COOKIE_SESSION = ISession.cookie_Session_ID;
 
 	/** Search entity */
 	public static final Search search = new Search();
 
 	/** Session proxy object, used for RPC in GWT */
 	public final static ISessionAsync proxySession = ISession.Util.getInstance();
 	/** Snippet proxy object, used for RPC in GWT */
 	public final static ISnippetAsync proxySnippet = ISnippet.Util.getInstance();
 	/** Main GUI distributor */
 	public final static GUI myGUI = GUI.getInstance();
 
 	/** Timer delay for refresh timer */
 	private final int TIMER_DELAY = 2000;
 
 	/** A scheduled timer call for some refreshings */
 	private final Timer scheduledTimer = new Timer() {
 
 		@Override
 		public void run() {
 			timerCall();
 		}
 	};
 
 	private Control() {
 	}
 
 	public static Control getInstance() {
 		if (instance == null) {
 			instance = new Control();
 		}
 		return instance;
 	}
 
 	/**
 	 * This <em>JSNI</em> method includes the {@code prettyPrint()} function of
 	 * the <em>google-code-prettify</em> package. It is made up of a bundle of
 	 * <em>JavaScript</em> files in the folder <em>googleCodePrettify</em> and
 	 * the {@code prettify.css} file.
 	 */
 	public static native void prettyPrint() /*-{
 		$wnd.prettyPrint();
 	}-*/;
 
 	/**
 	 * The main method, which is loaded on start or refresh
 	 * 
 	 * 
 	 */
 	@Override
 	public void onModuleLoad() {
 		// Actaivate timer
 		scheduledTimer.schedule(TIMER_DELAY);
 
 		proxySession.getSessionCookie(new AsyncCallback<String>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				myGUI.showMessagePopup("Error getting session cookie: " + caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(String cookie) {
 				if (cookie == null) {
 					// Static Guest session if browser does not
 					// supports cookies
 					myGUI.showMessagePopup("Null-Cookie returned");
 					return;
 				}
 				Cookies.setCookie(COOKIE_SESSION, cookie);
 				sessionCookie = cookie;
 
 			}
 		});
 	}
 
 	/**
 	 * Gets the ID of the current session
 	 * 
 	 * @return the session ID of the current session
 	 */
 	static String getSessionID() {
 		String sid = Cookies.getCookie(COOKIE_SESSION);
 		if (sid == null) {
 			sid = createNewSession();
 		}
 		return sid;
 	}
 
 	/**
 	 * Creates a new session
 	 * 
 	 * @return the session id of the newly generated session
 	 */
 	private static String createNewSession() {
 		// XXX: Paul - hab' das ich gemacht?!! Ergibt keinen Sinn auser ich
 		// dachte
 		// ich will das noch machen ...
 		return "session.getCookie()";
 	}
 
 	/**
 	 * Changes the current active page
 	 * 
 	 * @param newPage
 	 *            new Page to be set
 	 */
 	public void changeSite(Page newPage) {
 		switch (newPage) {
 		case PAGE_Impressum:
 			myGUI.showImpressum();
 			break;
 		case PAGE_User:
 			myGUI.showPersonalPage();
 			break;
 		case PAGE_SnippetOfDay:
 			showSnippetOfDay();
 			break;
 		case PAGE_Search:
 			myGUI.showSearchPage();
 			break;
 		case PAGE_Blank:
 			myGUI.showBlankPage();
 			break;
 		case PAGE_Snippet:
 			// Ignore - we don't know witch snippet
 			break;
 		case PAGE_Notifications:
 			myGUI.showNotificationPage();
 			break;
 		case PAGE_Moderator:
 			myGUI.showModeratorPage();
 			break;
 		}
 	}
 
 	/**
 	 * Changes the Smartsnip-Site.
 	 * 
 	 * 
 	 * @deprecated Because of the char argument. Use {@link #changeSite(Page)}
 	 *             instant
 	 * @param char A char which indicates which change should be done
 	 * 
 	 */
 	@Deprecated
 	public void changeSite(char c) {
 		switch (c) {
 		case 'i':
 			myGUI.showImpressum();
 			break;
 		case 'l':
 			myGUI.showLoginPopup();
 			break;
 		case 'r':
 			myGUI.showRegisterPopup();
 			break;
 		case 'p':
 			myGUI.showPersonalPage();
 			break;
 		case 'c':
 			myGUI.showContactForm();
 			break;
 		case 'n':
 			myGUI.showCreateSnippetForm();
 			break;
 		case 'm':
 			myGUI.showModeratorPage();
 			break;
 		default:
 		}
 	}
 
 	/**
 	 * Change to show one snippet
 	 * 
 	 * @param XSnippet
 	 *            The Snippet which should be shown
 	 * 
 	 */
 	public void changeToSnipPage(XSnippet snip) {
 		myGUI.showSnipPage(snip);
 
 		// Increase view counter
 		ISnippet.Util.getInstance().increaseViewCounter(snip.hash, new AsyncCallback<Void>() {
 			// Ignore callback here
 
 			@Override
 			public void onSuccess(Void result) {
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 		});
 	}
 
 	/**
 	 * Change the internal state, if the session is logged in
 	 * 
 	 * @param isLoggedin
 	 *            if logged in true
 	 */
 	void changeLoginState(boolean isLoggedin) {
 		this.loggedIn = isLoggedin;
 	}
 
 	/**
 	 * Calls a registration on the server
 	 * 
 	 * @param String
 	 *            the username
 	 * @param String
 	 *            the mailadress
 	 * @param String
 	 *            the password
 	 * 
 	 */
 	public void register(final String user, final String mail, final String pw, final Register register) {
 		if (user.isEmpty() || mail.isEmpty() || pw.isEmpty())
 			return;
 
 		proxySession.registerNewUser(user, pw, mail, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught instanceof NoAccessException) {
 					register.registerFailure("Access denied");
 				} else
 					register.registerFailure("Unknown error: " + caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				register.registerSuccess();
 			}
 		});
 	}
 
 	/**
 	 * Gets the local username
 	 * 
 	 * @return String the username
 	 */
 	public String getUsername() {
 		return user.username;
 	}
 
 	/**
 	 * Gets the local usermail
 	 * 
 	 * @return String email adress
 	 */
 	public String getUserMail() {
 		return user.email;
 	}
 
 	/**
 	 * Gets the login status
 	 * 
 	 * @return boolean login status
 	 */
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	/**
 	 * Updates the local variables with the information on the server
 	 * 
 	 */
 	public void refresh() {
 
 		proxySession.isLoggedIn(new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				handleException("Error getting session login status", caught);
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result == null)
 					return;
 
 				loggedIn = result;
 				myGUI.refresh();
 			}
 		});
 
 		proxySession.getUser(user.username, new AsyncCallback<XUser>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				handleException("Error while refreshing the user", caught);
 
 			}
 
 			@Override
 			public void onSuccess(XUser result) {
 				if (result == null || result.username.isEmpty())
 					user.username = "Guest";
 				else {
 					user.username = result.username;
 					user.email = result.email;
 					user.realname = result.realname;
 				}
 				myGUI.myMeta.update();
 				myGUI.mySearchArea.update();
 
 			}
 		});
 
 	}
 
 	private void handleException(String message, Throwable cause) {
 		// TODO Write me!
 	}
 
 	/**
 	 * Send a logout request to the server
 	 * 
 	 */
 	public void logout() {
 		proxySession.logout(new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				handleException("Logout went wrong", caught);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				changeLoginState(false);
 				refresh();
 				myGUI.myMeta.update();
 				myGUI.showSearchPage();
 			}
 		});
 
 	}
 
 	/**
 	 * writes a comment to the server
 	 * 
 	 * @param String
 	 *            the comment
 	 * @param long the hash of the current snippet
 	 */
 	public void writeComment(String comment, long hash) {
 		if (comment == null || comment.isEmpty())
 			return;
 
 		ISnippetAsync snippetProxy = ISnippet.Util.getInstance();
 
 		snippetProxy.addComment(hash, comment, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught instanceof NoAccessException) {
 					if (!isLoggedIn())
 						myGUI.showErrorPopup("You must login first");
 					else
 						myGUI.showErrorPopup("Access denied.");
 
 				} else
 					myGUI.showErrorPopup("Creation of new comment failed", caught);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				myGUI.myCommentArea.update();
 			}
 		});
 	}
 
 	/**
 	 * adds a snippet to the favorite list of a user
 	 * 
 	 * @param long the hash of the current snippet
 	 */
 	public void toFav(long hash) {
 		ISnippetAsync snippetProxy = ISnippet.Util.getInstance();
 
 		snippetProxy.addToFavorites(hash, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught instanceof NoAccessException) {
 					myGUI.showErrorPopup("Access denial", caught);
 				} else if (caught instanceof NotFoundException) {
 					myGUI.showErrorPopup("Snippet cannot be found by server", caught);
 				} else {
 					myGUI.showErrorPopup("Adding snippet to favorites failed", caught);
 				}
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 
 			}
 		});
 	}
 
 	/**
 	 * changes a snippet (with server call)
 	 * 
 	 * @param XSnippet
 	 *            a snippet
 	 */
 	public void changeSnippet(XSnippet snip) {
 
 		ISnippet.Util.getInstance().edit(snip, new AsyncCallback<Void>() {
 
 			@Override
 			public void onSuccess(Void result) {
 				Control.myGUI.mySnipArea.update();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	/**
 	 * shows the snippet of the day
 	 * 
 	 */
 	public void showSnippetOfDay() {
 		ISnippet.Util.getInstance().getSnippetOfDay(new AsyncCallback<XSnippet>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				myGUI.showErrorPage("Login failed", caught);
 			}
 
 			@Override
 			public void onSuccess(XSnippet result) {
 				changeToSnipPage(result);
 			}
 
 		});
 
 	}
 
 	/**
 	 * This method extracts the relative path out of the absolute URL, so that
 	 * only the structure on the server remains.
 	 * 
 	 * @return Gets the base URL path.
 	 */
 	private static String getBaseURL() {
 		if (isDevelopmentMode())
 			return "";
		else
			return DEPLOY_PATH;
		// String full = GWT.getModuleBaseURL();
		// String host = GWT.getHostPageBaseURL();
		// if (host.equals(full))
		// return host;
		//
		// String url = full.substring(host.length());
		// if (url == "/" || url.isEmpty())
		// return "";
		// if (url.startsWith("/"))
		// url = url.substring(1);
		//
		// return url;
 	}
 
 	/**
 	 * @return checks if currently running in development mode
 	 */
 	private static boolean isDevelopmentMode() {
 		return !GWT.isScript() && GWT.isClient();
 	}
 
 	/**
 	 * <b>CAUTION</b> This call goes well after the callback of
 	 * {@link ISession#getSessionCookie()}, so you must be sure, that this
 	 * happens before your request the session cookie
 	 * 
 	 * @return the session cookie, that is initialised after the first startup
 	 */
 	public static String getSessionCookie() {
 		return sessionCookie;
 	}
 
 	/**
 	 * Is called after the login succeeds
 	 */
 	public void onLogin(final String username) {
 		loggedIn = true;
 		myGUI.refreshMeta();
 		myGUI.refresh();
 	}
 
 	/** Scheduled timer call */
 	private void timerCall() {
 		ISession.Util.getInstance().isLoggedIn(new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 
 				Control.this.loggedIn = result;
 				Control.myGUI.myMeta.update(loggedIn);
 				reschedule();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// Ignore
 
 				// But reschedule
 				reschedule();
 			}
 
 			private void reschedule() {
 				scheduledTimer.schedule(TIMER_DELAY);
 			}
 		});
 	}
 }
