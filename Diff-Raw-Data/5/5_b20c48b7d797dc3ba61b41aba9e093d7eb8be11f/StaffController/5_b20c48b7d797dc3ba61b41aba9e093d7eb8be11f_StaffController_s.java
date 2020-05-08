 package org.alt60m.staffSite.servlet;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionBindingListener;
 
 import org.alt60m.cas.CASAuthenticator;
 import org.alt60m.cas.CASHelper;
 import org.alt60m.cas.CASUser;
 import org.alt60m.cas.NotAuthenticatedException;
 import org.alt60m.gcx.ConnexionBar;
 import org.alt60m.ministry.model.dbio.Staff;
 import org.alt60m.ministry.model.dbio.StaffSnapshot;
 import org.alt60m.ministry.servlet.StaffInfo;
 import org.alt60m.security.dbio.manager.UserNotFoundException;
 import org.alt60m.security.dbio.manager.UserNotVerifiedException;
 import org.alt60m.security.dbio.manager.SsmUserAlreadyExistsException;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller;
 import org.alt60m.servlet.MissingRequestParameterException;
 import org.alt60m.servlet.ServletLogging;
 import org.alt60m.servlet.UsersProcessor;
 import org.alt60m.staffSite.bean.dbio.EncryptedPreferences;
 import org.alt60m.staffSite.bean.dbio.UserPreferences;
 import org.alt60m.staffSite.model.dbio.StaffSitePref;
 import org.alt60m.staffSite.model.dbio.StaffSiteProfile;
 import org.alt60m.staffSite.profiles.dbio.InvalidAccountNumberException;
 import org.alt60m.staffSite.profiles.dbio.MultipleProfilesFoundException;
 import org.alt60m.staffSite.profiles.dbio.NotAuthorizedException;
 import org.alt60m.staffSite.profiles.dbio.ProfileAlreadyExistsException;
 import org.alt60m.staffSite.profiles.dbio.ProfileManagementException;
 import org.alt60m.staffSite.profiles.dbio.ProfileManager;
 import org.alt60m.staffSite.profiles.dbio.ProfileNotFoundException;
 import org.alt60m.util.DBConnectionFactory;
 import org.alt60m.util.EncryptorException;
 import org.alt60m.util.ObjectHashUtil;
 
 /**
  * Staff Controller 5/14/01 - Refactored to Controller2 by MDP
  *
  * @author Mat Weiss
  *
  */
 public class StaffController extends Controller {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	final String MAPPING_FOLDER = "/WEB-INF/mapping";
 
 	final String VIEWS_FILE = "/WEB-INF/staffDBIOviews.xml";
 
 	final String USERS_FILE = "/WEB-INF/staffusers.xml";
 
 	final String HRQUERYUSERS_FILE = "/WEB-INF/HRQueryUsers.xml";
 
 	final String Log4JConfig_FILE = "/WEB-INF/Log4JConfig.xml";
 
 	final String MAIL_HOST = "HART-E009V.campus.net.ccci.org";
 
 	final String MAIL_DOMAIN = "uscm.org";
 
 	final String MAIL_SUFFIX = "@uscm.org";
 
 	final String PREF_NAME_CACHED_EMAIL_PASSWORD = "cachedEmailPassword";
 
 	final String PREF_NAME_CACHED_PS_PASSWORD = "cachedPSPassword";
 
 	final String PREF_NAME_CACHED_PS_USERNAME = "cachedPSUsername";
 
 	final String PREF_NAME_ENABLE_EMAIL_SSO = "enableEmailSSO";
 
 	final String PREF_NAME_ENABLE_PS_SSO = "enablePSSSO";
 
 	final String PREF_ENABLE_SSO_YES = "Yes";
 
 	final String PREF_ENABLE_SSO_NO = "No";
 
 	final String PREF_ENABLE_SSO_KEEP_ASKING = "Ask";
 
 	String proxyUrlSuffix;
 
 	public String logoutCallbackSuffix;
 
 	/*
 	 * CODE TO CAPTURE HR INFO UPON LOGIN Added by S. Paulis 7/2005
 	 */
 	// turn this on (true) to force every staff member to enter a snapshot of
 	// their job information before they can get in to the staff site
 	final boolean ENABLE_CAPTURE_HR_INFO = false;
 
 	// turn this on (true) to have force it to do a lookup from the
 	// StaffSnapshot table to see if a staff member has entered their HR info
 	// yet (assuming ENABLE_CAPTURE_HR_INFO is true also)
 	final boolean ENABLE_FORCE_CAPTURE_BY_LOOKUP = true;
 
 	// turn this on to be able to force a staff member to enter an HR snapshot
 	// upon login by setting the flag in their staff site profile
 	final boolean ENABLE_FORCE_CAPTURE_BY_PROFILE_FLAG = false;
 
 	/*
 	 * END CODE TO CAPTURE HR INFO UPON LOGIN
 	 */
 
 	final String DEFAULT_ACTION = "logIn";
 
 	final String EMAIL_ATTEMPTS_SESSION_CNT = "email_attempts_session_ctr";
 
 	final int MAX_EMAIL_ATTEMPTS = 3;
 
 	ProfileManager _profileManager;
 
 	UserPreferences _preferences;
 
 	EncryptedPreferences _encryptedPreferences;
 
 	private Hashtable usersRoles = new Hashtable();
 
 	private Hashtable HRQueryUsersRoles = new Hashtable();
 
 	private CASHelper helper = new CASHelper();
 	;
 
 	/**
 	 * used for CAS logouts; maps tickets to sessions
 	 */
 	private Hashtable<String, HttpSession> authenticatedSessions = new Hashtable<String, HttpSession>();
 
 	/**
 	 * @author matthew.drees
 	 *
 	 * Intended to be bound to a session, and hold the CAS ticket(s) that have
 	 * authenticated the session. On the session's invalidation, it will remove
 	 * all of its associated tickets from the authenticatedSessions container.
 	 */
 	protected class TicketKeeper implements HttpSessionBindingListener {
 		private Set<String> ticketSet;
 
 		public TicketKeeper() {
 			ticketSet = new HashSet<String>();
 		}
 
 		public TicketKeeper(String firstTicket) {
 			ticketSet = new HashSet<String>();
 			ticketSet.add(firstTicket);
 		}
 
 		public void valueBound(HttpSessionBindingEvent event) {
 		}
 
 		public void valueUnbound(HttpSessionBindingEvent event) {
 			Iterator ticketIt = ticketSet.iterator();
 			while (ticketIt.hasNext()) {
 				authenticatedSessions.remove(ticketIt.next());
 			}
 		}
 
 		public void addTicket(String newTicket) {
 			ticketSet.add(newTicket);
 		}
 	}
 
 	public void init(ServletConfig config) throws ServletException {
 
 		super.init(config);
 		try {
 			setLog4JConfigFile(getServletContext()
 					.getRealPath(Log4JConfig_FILE));
 			log.info("StaffController: Starting Init");
 
 			// for hr updater/notifier email addresses
 			String servicesConfigPath = getServletContext().getRealPath(
 					MAPPING_FOLDER);
 			if (servicesConfigPath != null && servicesConfigPath.length() > 0) {
 				log.debug("The ServicesConfigPath used is: "
 						+ servicesConfigPath);
 				org.alt60m.servlet.ObjectMapping
 						.setConfigPath(servicesConfigPath);
 			} else {
 				log.info("No ServicesConfigPath was specified.");
 			}
 
 			DBConnectionFactory.setupPool();
 
 			_profileManager = new ProfileManager();
 			_preferences = new UserPreferences();
 			_encryptedPreferences = new EncryptedPreferences();
 
 			setViewsFile(getServletContext().getRealPath(VIEWS_FILE));
 			setDefaultAction(DEFAULT_ACTION);
 
 			initUsers(false);
 			initHRQueryUsers(true);
 			CASAuthenticator.init(config.getServletContext());
 
 			helper.init(config, "/servlet/StaffController");
 
 			ConnexionBar.setCasHelper(helper);
 
 			log.info("init() completed.  Ready for action.");
 		} catch (Exception e) {
 			log.fatal("init() failed", e);
 			throw new ServletException("Initialization failure", e);
 		}
 	}
 
 	private void initUsers(boolean verbose) {
 		usersRoles = UsersProcessor.parse(getServletContext().getRealPath(
 				USERS_FILE));
 
 		if (verbose) {
 			for (Enumeration e = usersRoles.keys(); e.hasMoreElements();) {
 				String k = (String) e.nextElement();
 				log.debug(k + " " + usersRoles.get(k));
 			}
 			log.debug("finished loading users.");
 		}
 	}
 
 	private void initHRQueryUsers(boolean verbose) {
 		HRQueryUsersRoles = UsersProcessor.parse(getServletContext()
 				.getRealPath(HRQUERYUSERS_FILE));
 
 		if (verbose) {
 			for (Enumeration e = HRQueryUsersRoles.keys(); e.hasMoreElements();) {
 				String k = (String) e.nextElement();
 				log.debug(k + " " + HRQueryUsersRoles.get(k));
 			}
 			log.debug("finished loading HR Query users.");
 		}
 	}
 
 	/**
 	 * Action: Reload
 	 *
 	 */
 	public void reload(ActionContext ctx) {
 		HttpServletRequest req = ctx.getRequest();
 		Enumeration headerNames = req.getHeaderNames();
 
 		while (headerNames.hasMoreElements()) {
 			String key = (String) headerNames.nextElement();
 			log.debug(key + "=" + req.getHeader(key));
 		}
 
 		try {
 			// initViews(true);
 			initUsers(true);
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Action: enterSite
 	 *
 	 */
 	public void enterSite(ActionContext ctx) {
 		// ??
 	}
 
 	protected void actionInvoked(String action, ActionContext ctx) {
 		ServletLogging sl = (ServletLogging) getServletContext().getAttribute(
 				"StaffSiteServletLogging");
 		if (sl == null) {
 			sl = new ServletLogging();
 			getServletContext().setAttribute("StaffSiteServletLogging", sl);
 		}
 
 		try {
 			Hashtable profile = ctx.getProfile();
 			if (profile != null) {
 				sl.addEvent("StaffController",
 						(String) profile.get("UserName"), action);
 			}
 		} catch (IllegalStateException e) {
 			// happens when logOut action invoked; usually, logouts occur because of
 			// cas logoutCallback call. See logIn().
 			// Admittedly, this is not a good design, but there's no way
 			// to tell whether a session has been invalidated.
 			sl.addEvent("StaffController", "unknown user", action);
 		}
 	}
 
 	public void showUsage(ActionContext ctx) {
 		try {
 			ServletLogging sl = (ServletLogging) getServletContext()
 					.getAttribute("StaffSiteServletLogging");
 			ActionResults results = new ActionResults();
 
 			if (sl != null) {
 
 				if (ctx.getInputString("includeseconds", false) != null) {
 					results.addCollection("usage", sl.getSummary(Long
 							.parseLong(ctx.getInputString("includeseconds"))));
 				} else {
 					results.addCollection("usage", sl.getSummary());
 				}
 				ctx.setReturnValue(results);
 			}
 
 			ctx.goToView("usageReport");
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 	}
 
 	// ***** NEW SSO CODE *****
 
 	public void verifyGCX(ActionContext ctx) {
 
 		try {
 
 			String gcxVerifyURL = CASAuthenticator.CAS_VERIFICATION_URL + "?"
 					+ "service=" + URLEncoder.encode(helper.getService(ctx.getRequest()), "UTF-8");
 
 			String gcxSigninURL = CASAuthenticator.CAS_LOGIN_URL + "?"
 					+ "service=" + URLEncoder.encode(gcxVerifyURL, "UTF-8");
 
 			ctx.getResponse().sendRedirect(gcxSigninURL);
 		} catch (Exception e) {
 			log.info("Unable to Redirect");
 			ctx.setSessionValue("ErrorCode", "server error");
 			ctx.goToView("loginError");
 		}
 
 	} /*
 		 * Login using GCX Single Sign On
 		 */
 
 	public void logInGCX(ActionContext ctx) {
 		try {
 			String gcxLoginURL = CASAuthenticator.CAS_LOGIN_URL + "?"
 			+ "service=" + URLEncoder.encode(helper.getService(ctx.getRequest()), "UTF-8")
 			+ "&" + "logoutCallback=" + URLEncoder.encode(helper.getLogoutCallbackService(ctx.getRequest()), "UTF-8");
 
 			log.debug("redirecting to: " + gcxLoginURL);
 			ctx.getResponse().sendRedirect(gcxLoginURL);
 		}
 		catch (Exception e) {
 			log.info("Unable to Redirect");
 			ctx.setSessionValue("ErrorCode", "server error");
 			ctx.goToView("loginError");
 		}
 	}
 
 	public void logOut(ActionContext ctx) {
 		try {
 			String gcxLogoutURL = helper.getLogoutUrl(ctx.getRequest());
 
 			ctx.getResponse().sendRedirect(gcxLogoutURL);
 			ctx.getSession().invalidate();
 
 		} catch (Exception e) {
 			log.info("Unable to Redirect");
 			ctx.setSessionValue("ErrorCode", "server error");
 			ctx.goToView("loginError");
 		}
 	}
 
 
 
 	/**
 	 * Action: logIn
 	 *
 	 */
 
 	public void logIn(ActionContext ctx) throws IOException {
 		if (ctx.getInputString(CASAuthenticator.CAS_TICKET_TOKEN) == null) {
 			ctx.goToView("login");
 		} else {
 			String ticket = ctx
 					.getInputString(CASAuthenticator.CAS_TICKET_TOKEN);
 
 			// check for logout request from CAS server;
 			// if so, invalidate appropriate session
 			if (ticket.charAt(0) == '-') {
 				HttpSession session = ((HttpSession) authenticatedSessions
 						.get(ticket.substring(1)));
 				if (session != null) {
 					session.invalidate();
 					ctx.getResponse().getWriter().print("Logout Successful");
 				} else {
 					log.warn("Logout request received, but ticket not mapped to session: "
 							+ ticket);
 					ctx.getResponse().getWriter().print("Logout Unsuccessful");
 				};
 			}
 
 			else // otherwise, it's a gcx login
 			{
 				CASUser newUser = null;
 				try {
 					String service = helper.getService(ctx.getRequest());
 					String proxyCallback = helper.getProxyCallbackService(ctx.getRequest());
 					newUser = CASAuthenticator.authenticate(service, proxyCallback, ticket);
 					HttpSession session = ctx.getSession();
 					if (ctx.getSessionValue("ticketKeeper") == null) {
 						session.setAttribute("ticketKeeper", new TicketKeeper(
 								ticket));
 					} else {
 						((TicketKeeper) session.getAttribute("ticketKeeper"))
 								.addTicket(ticket);
 					}
 					authenticatedSessions.put(ticket, session);
 
 				} catch (NotAuthenticatedException e) {
 					log.info("Not authenticated from CAS.");
 					ctx.setSessionValue("ErrorCode", "notauthorized");
 					ctx.goToView("loginError");
 				}
 				if (newUser != null) {
 					authorize(ctx, newUser);
 				}
 			}
 		}
 	}
 
 	private void authorize(ActionContext ctx, CASUser newUser) {
 		String profileId = null;
 		try {
 			profileId = _profileManager.authorize(newUser);
 		} catch (UserNotFoundException e) {
 			log.warn(e.toString());
 			log.info("User not found: " + newUser.getUsername());
 			ctx.setSessionValue("ErrorCode", "noprofile");
 			ctx.goToView("loginError");
 		} catch (ProfileNotFoundException e) {
 			log.warn(e.toString());
 			// set a message needed for more help
 			log.info("Profile not found: " + newUser.getUsername());
 			ctx.setSessionValue("ErrorCode", "noprofile");
 			ctx.goToView("loginError");
 		} catch (ProfileManagementException e) {
 			log.error(e, e);
 			log.error("Couldn't authenticate: "
 					+ newUser.getUsername() + " due to service problems.", e);
 			ctx.setSessionValue("ErrorCode", "unknown");
 			ctx.goToView("loginError");
 		} catch (MultipleProfilesFoundException e) {
 			log.error(e.getMessage(), e);
 			log.error("Multiple accounts found: "
 					+ newUser.getUsername());
 			ctx.setSessionValue("ErrorCode", "multipleprofiles");
 			ctx.goToView("loginError");
 		} catch (UserNotVerifiedException e) {
 			log.info("User GCX Account Not Verified for user: "
 					+ newUser.getUsername());
 			ctx.setSessionValue("ErrorCode", "gcxnotverified");
 			ctx.goToView("loginError");
 		} catch (SsmUserAlreadyExistsException e){
 			log.info("GCX username is in conflict with existing SSM username: "+ newUser.getUsername()+": "+e.getMessage());
 					
 			ctx.setSessionValue("ErrorCode", "ssmUserAlreadyExists");
 			ctx.setSessionValue("ErrorMessage", e.getMessage());
 			ctx.goToView("loginError");
 		}
 		if (profileId != null) {
 			authorize(ctx, newUser, profileId);
 		}
 	}
 
 	//note:
 	// Only used for people who for some reason or other can't login via SSO.
 	// They need to use their full login name, unless they have a uscm login.
 	public void authenticate(ActionContext ctx) {
 
 		String userName = new String();
 		String password = new String();
 		String profileId = null;
 
 		try {
 			userName = ctx.getInputString("UserName", true).toLowerCase();
 			if (userName.indexOf("@") == -1)
 				userName += MAIL_SUFFIX;
 			password = ctx.getInputString("Password", true);
 
 
 			// Authenticate based on credentials
 			log.debug("username: " + userName);
 			log.debug("password: " + password);
 			try {
 				profileId = _profileManager.authenticate(userName, password);
 				authorize(ctx, new CASUser(userName, null, new HashMap<String, String>()), profileId);
 			} catch (ProfileNotFoundException e1) {
 				// set a message needed for more help
 				log.info("Profile not found: " + userName);
 				ctx.setSessionValue("ErrorCode", "noprofile");
 				ctx.goToView("loginError");
 
 			} catch (MultipleProfilesFoundException e1) {
 				log.error("Multiple accounts found: " + userName);
 				ctx.setSessionValue("ErrorCode", "multipleprofiles");
 				ctx.goToView("loginError");
 			} catch (NotAuthorizedException e1) {
 				log.info("Not authorized: " + userName);
 				ctx.setSessionValue("ErrorCode", "notauthorized");
 				ctx.goToView("loginError");
 			} catch (ProfileManagementException pme) {
 				log.error("Couldn't authenticate: " + userName
 						+ " do to service problems.", pme);
 				ctx.setSessionValue("ErrorCode", "unknown");
 				ctx.goToView("loginError");
 			} catch (Exception e) {
 				log.error("Unknown login error", e);
 				ctx.setSessionValue("ErrorCode", "unknown");
 				ctx.goToView("loginError");
 			}
 		} catch (MissingRequestParameterException e) {
 			ctx.goToView("login");
 		}
 
 	}
 
 	public void authorize(ActionContext ctx, CASUser user, String profileId) {
 		// Load profile
 		String userName = user.getUsername();
 		Hashtable<String, Object> profileHash = ObjectHashUtil.obj2hash(new StaffSiteProfile(
 				profileId));
 		Staff staff = new Staff();
 
 		String accountNo = (String) profileHash.get("AccountNo");
 		staff.setAccountNo(accountNo);
 		if ((accountNo != null) && (!staff.isPKEmpty()) && (staff.select())) {
 			String region = staff.getRegion();
 			if (region != null) {
 				profileHash.put("region", region);
 			} else {
 				profileHash.put("region", "");
 			}
 		} else {
 			profileHash.put("region", "");
 		}
 
 		log.debug("Profile: " + profileHash);
 
 		ctx.setSessionValue("loggedIn", profileId);
 		ctx.setSessionValue("profile", profileHash);
 		ctx.setSessionValue("userName", userName);
 		ctx.setSessionValue("CASUser", user);
 
 		// profile information in here.........................
 		ctx.setSessionValue("zipCode",
 				getPreference(profileId, "ZIPCODE", "NO"));
 		ctx.setSessionValue("boxStyle", getPreference(profileId, "BOXSTYLE",
 				"rounded"));
 		ctx.setSessionValue("homePageArticlesToDisplay", getPreference(
 				profileId, "HOMEPAGEARTICLESTODISPLAY", "3"));
 		ctx.setSessionValue("campusOnly", getPreference(profileId,
 				"OCCASIONSCAMPUSONLY", "true"));
 		ctx.setSessionValue("weatherType", getPreference(profileId,
 				"WEATHERTYPE", "today"));
 		// ctx.setSessionValue("regionalNews", getPreference(profileId,
 		// "REGIONALNEWS", "Yes"));
 
 		ctx.setSessionValue("accountNo", profileHash.get("AccountNo"));
 
 		// -----------------------------------
 		boolean isHR = false;
 		boolean hasHRQueryAccess = false;
 
 		try {
 			boolean isStaff = ((Boolean) profileHash.get("IsStaff"))
 					.booleanValue();
 
 			if (isStaff) {
 				StaffInfo si = new StaffInfo();
 				isHR = si.isHumanResources(accountNo);
 			}
 			if (isHR || HRQueryUsersRoles.containsKey(userName)) {
 				hasHRQueryAccess = true;
 			}
 		} catch (Exception e) {
 			isHR = false;
 			log.error("Couldn't determine if HR.  Setting to false.",
 					e);
 		}
 
 		ctx.setSessionValue("isHR", String.valueOf(isHR));
 		ctx.setSessionValue("hasHRQueryAccess", String
 				.valueOf(hasHRQueryAccess));
 		// -----------------------------------
 
 		// set cookie stuff in here.........................
 		Cookie mainCookie = new Cookie("UserName", userName);
 
 		int maxage = 60 * 60 * 24 * 30; // one month
 		mainCookie.setMaxAge(maxage);
 
 		String requestedURL = (String) ctx.getSessionValue("onLogInGoto");
 		ctx.getSession().removeAttribute("onLogInGoto");
 		log.debug("requestedURL: " + requestedURL);
 
 		ctx.getResponse().addCookie(mainCookie);
 		if (requestedURL != null) {
 			log.debug("Going to : " + requestedURL);
 			try {
 				ctx.getResponse().sendRedirect(requestedURL);
 			} catch (IOException e) {
 				log.info("Unable to Redirect");
 				ctx.setSessionValue("ErrorCode", "server error");
 				ctx.goToView("loginError");
 			}
 		} else {
 			String home = getHomeUrl(ctx.getRequest());
 			try {
 				// clear ticket; need to send redirect
 				ctx.getResponse().sendRedirect(home);
 			} catch (IOException e) {
 				log.info("Unable to Redirect");
 				ctx.setSessionValue("ErrorCode", "server error");
 				ctx.goToView("loginError");
 			}
 		}
 	}
 
 	/**
 	 * Action: showOccasions
 	 *
 	 */
 	public void showOccasions(ActionContext ctx) {
 		ctx.goToView("occasions");
 	}
 
 	/**
 	 * Action: showCustomize
 	 *
 	 */
 	public void showCustomize(ActionContext ctx) {
 		ctx.goToView("customize");
 	}
 
 	/**
 	 * Action: Customize
 	 *
 	 */
 	public void Customize(ActionContext ctx) {
 		// String accountNo = (String) ctx.getProfile().get("AccountNo");
 		// String zipCode = ctx.getInputString("zipCode");
 		// String weatherType = ctx.getInputString("weatherType");
 		String homePageArticlesToDisplay = ctx
 				.getInputString("homePageArticlesToDisplay");
 		String campusOnly = ctx.getInputString("campusOnly");
 		String boxStyle = ctx.getInputString("boxStyle");
 		String regionalNews = ctx.getInputString("regionalNews");
 		log.debug("The value for regional news is: "
 				+ regionalNews);
 
 		try {
 			// setPreference(ctx.getProfileID(), "ZIPCODE", "Zip Code",
 			// zipCode);
 			// ctx.setSessionValue("zipCode", zipCode);
 			// setPreference(ctx.getProfileID(), "WEATHERTYPE", "Weather Type to
 			// Display for Home Page", weatherType);
 			// ctx.setSessionValue("weatherType", weatherType);
 			setPreference(ctx.getProfileID(), "HOMEPAGEARTICLESTODISPLAY",
 					"Number of LX Articles to Display on Home Page",
 					homePageArticlesToDisplay);
 			ctx.setSessionValue("homePageArticlesToDisplay",
 					homePageArticlesToDisplay);
 			setPreference(ctx.getProfileID(), "OCCASIONSCAMPUSONLY",
 					"Display Campus People Only for Birthdays, Anniv., ...",
 					campusOnly);
 			ctx.setSessionValue("campusOnly", campusOnly);
 			setPreference(ctx.getProfileID(), "BOXSTYLE",
 					"How themed box beans should be displayed", boxStyle);
 			ctx.setSessionValue("boxStyle", boxStyle);
 			// setPreference(ctx.getProfileID(), "REGIONALNEWS", "Whether or not
 			// to display regional articles", regionalNews);
 			// ctx.setSessionValue("regionalNews", regionalNews);
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
		ctx.goToView("home");
 	}
 
 	/**
 	 * Action: AddUser
 	 *
 	 */
 	public void AddUser(ActionContext ctx) {
 		// administration of users accounts in system
 		Hashtable<String, String> tub = new Hashtable<String, String>();
 		String Option = ctx.getInputString("Option");
 		String ErrorMsg = "";
 		String ResultMsg = "";
 
 		if (Option == null) {
 			Option = "None";
 			ErrorMsg = ErrorMsg + "Nothing Happened.<br>";
 			// Add, View, Create, Update, Delete
 		}
 
 		// //// CREATE NEW USER ACCOUNT //////////////
 		else if (Option.equals("Create")) {
 			// ERROR CHECKING
 			// ------------------------------------------------------------
 			if (ctx.getInputString("FirstName") == null) {
 				ErrorMsg = ErrorMsg + "You must supply a FirstName name.<br>";
 			} else if (ctx.getInputString("FirstName").trim().equals("")) {
 				ErrorMsg = ErrorMsg + "You must supply a FirstName name.<br>";
 			} else if ((ctx.getInputString("FirstName").trim()).length() < 2) {
 				ErrorMsg = ErrorMsg
 						+ "FirstName name must be at least 2 characters long.<br>";
 			}
 			if (ctx.getInputString("LastName") == null) {
 				ErrorMsg = ErrorMsg + "You must supply a LastName name.<br>";
 			} else if (ctx.getInputString("LastName").trim().equals("")) {
 				ErrorMsg = ErrorMsg + "You must supply a LastName name.<br>";
 			} else if ((ctx.getInputString("LastName").trim()).length() < 2) {
 				ErrorMsg = ErrorMsg
 						+ "LastName name must be at least 2 characters long.<br>";
 			}
 
 			if (ctx.getInputString("UserName") == null) {
 				ErrorMsg = ErrorMsg + "You must supply a Username.<br>";
 			} else if (ctx.getInputString("UserName").trim().equals("")) {
 				ErrorMsg = ErrorMsg + "You must supply a Username.<br>";
 			} else if ((ctx.getInputString("UserName").trim()).length() < 5) {
 				ErrorMsg = ErrorMsg
 						+ "Username must be at least 5 characters long.<br>";
 			}
 			// We don't require firstname.lastname usernames anymore
 			// if ((ctx.getInputString("UserName").indexOf(".")) == -1) {
 			// ErrorMsg = ErrorMsg + "Username must be of format:
 			// firstname.lastname.<br>";
 			// }
 			String Passwd = ctx.getInputString("Passwd");
 			String Vpasswd = ctx.getInputString("Vpasswd");
 			if (Passwd == null) {
 				ErrorMsg = ErrorMsg + "You must supply a Password.<br>";
 			} else if (Passwd.equals("")) {
 				ErrorMsg = ErrorMsg + "You must supply a Password.<br>";
 			}
 			if (Passwd.length() < 4) {
 				ErrorMsg = ErrorMsg
 						+ "Password must be at least 4 characters long.<br>";
 			}
 			if (Passwd.length() > 14) {
 				ErrorMsg = ErrorMsg
 						+ "Password must be at most 14 characters long.<br>";
 			}
 			if (!Vpasswd.equals(Passwd)) {
 				ErrorMsg = ErrorMsg + "Passwords do not match.<br>";
 			}
 
 			// PASSED ALL THE INPUT ERROR CHECKING
 			if (ErrorMsg.equals("")) {
 				try {
 
 					StringBuffer id = new StringBuffer();
 					StringBuffer errs = new StringBuffer();
 
 					StaffSiteProfile ssp = new StaffSiteProfile();
 
 					ssp.setFirstName(ctx.getInputString("FirstName", true));
 					ssp.setLastName(ctx.getInputString("LastName", true));
 					ssp.setUserName(ctx.getInputString("UserName", true));
 					String pw = ctx.getInputString("Passwd", true);
 					String pwVerify = ctx.getInputString("Vpasswd", true);
 					ssp.setAccountNo(ctx.getInputString("AccountNo", true));
 					ssp
 							.setIsStaff((ctx.getInputString("IsStaff") != null && (ctx
 									.getInputString("IsStaff"))
 									.equalsIgnoreCase("true")));
 					// ssp.setIsStudent((ctx.getInputString("IsStudent") != null
 					// &&
 					// (ctx.getInputString("IsStudent")).equalsIgnoreCase("true")));
 					ssp
 							.setChangePassword((ctx
 									.getInputString("ChangePassword") != null && (ctx
 									.getInputString("ChangePassword"))
 									.equalsIgnoreCase("true")));
 
 					_profileManager.createProfile(ssp, pw, pwVerify, id, errs);
 					tub.put("ID", id.toString());
 					ResultMsg = ResultMsg + "User Account Created.<br>";
 				} catch (ProfileAlreadyExistsException e) {
 					ErrorMsg += e.getMessage();
 				} catch (InvalidAccountNumberException e) {
 					ErrorMsg += "Account number doesn't exist.  User account created anyway.  Please change account number if user is a staff person.";
 				} catch (Exception e) {
 					ErrorMsg += "There was an error creating the user account!<br>";
 					log.error("Error connecting to secant!:" + e);
 				}
 
 			}
 
 			tub.put("FirstName", ctx.getInputString("FirstName"));
 			tub.put("LastName", ctx.getInputString("LastName"));
 			tub.put("AccountNo", ctx.getInputString("AccountNo"));
 			tub.put("UserName", ctx.getInputString("UserName"));
 			if (ctx.getInputString("ChangePassword") != null) {
 				tub.put("ChangePassword",
 						(ctx.getInputString("ChangePassword") != null && (ctx
 								.getInputString("ChangePassword"))
 								.equalsIgnoreCase("true")) ? "Y" : "");
 			}
 			if (ctx.getInputString("IsStaff") != null) {
 				tub.put("IsStaff",
 						(ctx.getInputString("IsStaff") != null && (ctx
 								.getInputString("IsStaff"))
 								.equalsIgnoreCase("true")) ? "Y" : "");
 			}
 			/*
 			 * if (ctx.getInputString("IsStudent")!=null) { tub.put("IsStudent",
 			 * (ctx.getInputString("IsStudent") != null &&
 			 * (ctx.getInputString("IsStudent")).equalsIgnoreCase("true")) ? "Y" :
 			 * ""); }
 			 */
 			tub.put("Option", "Update");
 
 		}
 
 		// ////////////////////////////////////////////////////
 		else if (Option.equals("Add")) {
 			tub.put("Option", "");
 			tub.put("UserName", "firstname.lastname@uscm.org");
 		}
 
 		// ////////////////////////////////////////////////////
 		else if (Option.equals("View")) {
 			try {
 				String userName = ctx.getInputString("UserName").toLowerCase();
 
 				StaffSiteProfile profile = _profileManager.getProfile(userName);
 				tub.put("ID", profile.getStaffSiteProfileID());
 				tub.put("UserName", profile.getUserName());
 				tub.put("FirstName", profile.getFirstName());
 				tub.put("LastName", profile.getLastName());
 				tub.put("AccountNo", profile.getAccountNo());
 				tub.put("ChangePassword", profile.getChangePassword() ? "Y"
 						: "");
 				tub.put("IsStaff", profile.getIsStaff() ? "Y" : "");
 				// tub.put("IsStudent", profile.getIsStudent() ? "Y" : "");
 
 				tub.put("Option", "Update");
 
 			} catch (Exception e) {
 				ErrorMsg = "Failed to list...";
 			}
 		}
 
 		// ////////////////////////////////////////////////////
 		else if (Option.equals("Update")) {
 
 			try {
 				String userName = ctx.getInputString("UserName").toLowerCase();
 
 				// ProfileValues input = new ProfileValues();
 				StaffSiteProfile input = new StaffSiteProfile();
 				input.setStaffSiteProfileID(ctx.getInputString("ID"));
 				input.setFirstName(ctx.getInputString("FirstName"));
 				input.setLastName(ctx.getInputString("LastName"));
 				input.setUserName(ctx.getInputString("UserName"));
 				input.setAccountNo(ctx.getInputString("AccountNo"));
 				input.setIsStaff((ctx.getInputString("IsStaff") != null && (ctx
 						.getInputString("IsStaff")).equalsIgnoreCase("true")));
 				// input.setIsStudent((ctx.getInputString("IsStudent") != null
 				// &&
 				// (ctx.getInputString("IsStudent")).equalsIgnoreCase("true")));
 				input
 						.setChangePassword((ctx
 								.getInputString("ChangePassword") != null && (ctx
 								.getInputString("ChangePassword"))
 								.equalsIgnoreCase("true")));
 
 				// String pw = ctx.getInputString("Password");
 				// String pwVerify = ctx.getInputString("VPassword");
 
 				// Perform update
 				_profileManager.updateProfile(input);
 
 				// Get updated values
 				StaffSiteProfile profile = _profileManager.getProfile(userName);
 
 				tub.put("ID", profile.getStaffSiteProfileID());
 				tub.put("UserName", profile.getUserName());
 				tub.put("FirstName", profile.getFirstName());
 				tub.put("LastName", profile.getLastName());
 				tub.put("AccountNo", profile.getAccountNo());
 				if (profile.getChangePassword())
 					tub.put("ChangePassword", "Y");
 				if (profile.getIsStaff())
 					tub.put("IsStaff", "Y");
 				// if(profile.getIsStudent())
 				// tub.put("IsStudent", "Y");
 
 				tub.put("Option", ctx.getInputString("Option"));
 			} catch (Exception e) {
 				log.error(e.getMessage(), e);
 			}
 
 		}
 
 		// ////////////////////////////////////////////////////
 		else if (Option.equals("Delete")) {
 			String userName = ctx.getInputString("UserName").toLowerCase();
 
 			try {
 				_profileManager.deleteProfile(userName);
 			} catch (Exception e) {
 				log.error("Could not create user" + e);
 			}
 			ResultMsg = ResultMsg + "User Account Deleted.<br>";
 			tub.put("UserName", ctx.getInputString("UserName"));
 		}
 
 		tub.put("ErrorMsg", ErrorMsg);
 		tub.put("ResultMsg", ResultMsg);
 
 		ctx.setSessionValue("tub", tub);
 		ctx.goToView("addUser");
 	}
 
 	/**
 	 * Action: changePassword
 	 *
 	 */
 	public void changePassword(ActionContext ctx) {
 		Hashtable<String, String> tub = new Hashtable<String, String>();
 		String ErrorMsg = "";
 		String ResultMsg = "";
 
 		if (ctx.getInputString("UserName") != null) {
 
 			String userName = ctx.getInputString("UserName").toLowerCase();
 			if (userName.indexOf("@") == -1) {
 				userName += MAIL_SUFFIX;
 			}
 			// Client client = new Client();
 
 			if (ctx.getInputString("view").equals("userChangePassword")) { // user
 				// wants
 				// to
 				// change
 				// their
 				// password
 
 				String oPasswd = ctx.getInputString("OPassword");
 				String passwd = ctx.getInputString("Password");
 				String nPasswd = ctx.getInputString("VPassword");
 				boolean changeFlag = (ctx.getInputString("ChangePassword") != null)
 						&& (ctx.getInputString("ChangePassword")
 								.equalsIgnoreCase("true"));
 
 				// error check the passwords
 				if (passwd.length() < 5)
 					ErrorMsg = ErrorMsg
 							+ "Your new password must be a minimum of 5 characters.<br>";
 				else if (!passwd.equals(nPasswd))
 					ErrorMsg = ErrorMsg + "New passwords do not match.<br>";
 				else if (oPasswd.equals(passwd))
 					ErrorMsg = ErrorMsg
 							+ "Your temporary password and desired password must be different.<br>";
 				else {
 					try {
 						_profileManager.changePassword(userName, oPasswd,
 								nPasswd, changeFlag);
 						ResultMsg += "The password for " + userName
 								+ " has been changed<br>";
 
 					} catch (org.alt60m.staffSite.profiles.dbio.NotAuthorizedException e) {
 						ErrorMsg += "You entered your old password incorrectly.<br>";
 					} catch (Exception e) {
 						log.error("Unable to change pasword", e);
 						ErrorMsg += "The password change was not successful.  This may not be the last time you will have to change your password.<br>";
 					}
 				}
 			}
 
 		}
 
 		// If their profile was marked with a change password flag, set the
 		// first login flag ??
 		boolean mustChange = ((Boolean) (ctx.getProfile().get("ChangePassword")))
 				.booleanValue();
 		tub.put("firstLogin", String.valueOf(mustChange));
 
 		tub.put("userName", (String) ctx.getProfile().get("UserName"));
 		tub.put("ErrorMsg", ErrorMsg);
 		tub.put("ResultMsg", ResultMsg);
 
 		ctx.setSessionValue("passwordChange", tub);
 		ctx.goToView("userChangePassword");
 
 	}
 
 	/**
 	 * Reset the password for the given username.
 	 */
 	public void resetPassword(ActionContext ctx){
 		Hashtable<String, String> tub = new Hashtable<String, String>();
 		String ErrorMsg = "";
 		String ResultMsg = "";
 
 		if (ctx.getInputString("UserName") != null) {
 			String userName = ctx.getInputString("UserName").toLowerCase();
 			// We are going to requre the full username, to allow for usernames
 			// that are not email addresses.
 
 			String password = ctx.getInputString("Password");
 			boolean flag = (ctx.getInputString("Flag") != null && ctx
 					.getInputString("Flag").equalsIgnoreCase("true"));
 
 			try {
 				_profileManager.resetPassword(userName, password, flag);
 				ResultMsg = "Password changed successfully!";
 
 				if (flag == true) {
 					ResultMsg += "Your password must be changed upon next login.<br>";
 				}
 
 			} catch (ProfileNotFoundException e) {
 				log.warn("username not found: " + userName);
 				ErrorMsg += "Could not reset password; cannot find user with username " + userName;
 			}
 			catch (Exception e) {
 				log.error("Could not reset password: " + e);
 				ErrorMsg += "Could not reset password: " + e.getMessage();
 				ErrorMsg += "<P> Please notify Alt60M about this error and the UserName that gave the error.";
 			}
 
 		} else {
 			ErrorMsg = ErrorMsg + " No user name given!<br>";
 		}
 
 		tub.put("userName", (String) ctx.getProfile().get("UserName"));
 		tub.put("ErrorMsg", ErrorMsg);
 		tub.put("ResultMsg", ResultMsg);
 		ctx.setSessionValue("passwordChange", tub);
 
 		if (ctx.getInputString("view").equals("sec")) {
 			ctx.goToView("adminChangePassword");
 		} else {
 			ctx.goToView("userChangePassword");
 		}
 	}
 
 	/**
 	 * Action: listUsers
 	 *
 	 */
 	public void listUsers(ActionContext ctx) {
 		Hashtable<String, String> tub = new Hashtable<String, String>();
 		String[] users = null;
 		String ErrorMsg = "";
 		String username;
 		try {
 
 			users = _profileManager.listStaffSiteUsers(); // .listUsers("@uscm.org");
 			int len = users.length;
 			tub.put("numOfUsers", String.valueOf(len));
 			for (int i = 0; i < len; i++) {
 				username = users[i];
 				tub.put(String.valueOf(i), username);
 				// key = "name" + String.valueOf(i);
 				// tub.put(key, username.substring(0,
 				// username.indexOf("@uscm.org")));
 			}
 
 		} catch (Exception e) {
 			ErrorMsg = "Error retreiving user list!";
 		}
 
 		tub.put("ErrorMsg", ErrorMsg);
 
 		ctx.setSessionValue("tub", tub);
 		ctx.goToView("listUsers");
 	}
 
 	/**
 	 * Action: captureHRinfo - CODE TO CAPTURE HR INFO UPON LOGIN
 	 *
 	 */
 	public void captureHRinfo(ActionContext ctx) {
 		try {
 
 			// get info from jsp response
 			// save it to object and then have object persist it in the table
 			String accountNo = ctx.getInputString("AccountNo");
 			StaffSnapshot S = new StaffSnapshot(accountNo);
 			S.select();
 
 			// if mult. matches found, empty record will be returned
 			// only persist if one correct record returned with matching account
 			// number or none found at all
 			if (accountNo.equals(S.getAccountNo())) {
 				S.setFirstName(ctx.getInputString("FirstName"));
 				S.setMiddleName(ctx.getInputString("MiddleName"));
 				S.setLastName(ctx.getInputString("LastName"));
 				S.setAddressType(ctx.getInputString("AddressType"));
 				S.setAddress1(ctx.getInputString("Address1"));
 				S.setAddress2(ctx.getInputString("Address2"));
 				S.setCity(ctx.getInputString("City"));
 				S.setState(ctx.getInputString("State"));
 				S.setZip(ctx.getInputString("Zip"));
 				S.setCountry(ctx.getInputString("Country"));
 				S.setIntAddress(ctx.getInputString("IntAddress"));
 				S.setMinistry(ctx.getInputString("Ministry"));
 				S.setDepartment(ctx.getInputString("Department"));
 				S.setSubMinistry(ctx.getInputString("SubMinistry"));
 				S.setPosition(ctx.getInputString("Position"));
 				S.setPositionDescr(ctx.getInputString("OtherPosition"));
 				S.setStrategy(ctx.getInputString("Strategy"));
 				S.setIntStatus(ctx.getInputString("International"));
 				S.setIntRole(ctx.getInputString("IntRole"));
 				S.setRole(ctx.getInputString("Role"));
 				S.setMaritalStatus(ctx.getInputString("Married"));
 				S.setSpouseFirstName(ctx.getInputString("SpouseFirstName"));
 				S.setNumChildren(Integer.valueOf(
 						ctx.getInputString("NumChildren")).intValue());
 
 				S.persist();
 				// reset profile flag
 				_profileManager.setCaptureHRinfoFlag((String) ctx
 						.getSessionValue("userName"), false);
 			}
 			// then redirect to home page
 			// ctx.setReturnValue(results);
			ctx.goToView("home");
 
 		} catch (Exception e) {
 			log.error("Failed to perform captureHRinfo", e);
 		}
 
 	}
 
 	/**
 	 * Action: showTools
 	 *
 	 */
 	public void showTools(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults();
 			if (ctx.getSessionValue("isHR") != null)
 				results.putValue("isHR", (String) ctx.getSessionValue("isHR"));
 			if (ctx.getSessionValue("hasHRQueryAccess") != null)
 			{
 				results.putValue("hasHRQueryAccess", (String) ctx
 					.getSessionValue("hasHRQueryAccess"));
 			}
 			if (ctx.getSessionValue("userName") != null)
 			{
 			results.putValue("FSKadmin", ((String)ctx.getSessionValue("userName")).equalsIgnoreCase("jane.stump@uscm.org")?"true":"false");
 			}
 			else
 			{
 				results.putValue("FSKadmin", "false");	
 			}
 			ctx.setReturnValue(results);
 			ctx.goToView("tools");
 
 		} catch (Exception e) {
 			if (ctx.getSessionValue("isHR") == null) {
 				ctx.goToURL("/Error.jsp?Reason=Timeout");
 			}
 			log.error("Failed to perform showTools", e);
 		}
 
 	}
 
 	/**
 	 * Action: showUserAdmin
 	 *
 	 */
 	public void showUserAdmin(ActionContext ctx) {
 		Hashtable tub = new Hashtable();
 		ctx.setSessionValue("passwordChange", tub);
 		ctx.goToView("userAdmin");
 	}
 
 	/**
 	 * Action: showHome
 	 *
 	 */
 	public void showHome(ActionContext ctx) {
 		ActionResults result=new ActionResults("staffhome");
 		org.alt60m.security.dbio.model.User user=new org.alt60m.security.dbio.model.User();
 		user.setUsername((String)ctx.getSessionValue("userName"));
 		user.select();
 		org.alt60m.ministry.model.dbio.Person person=new org.alt60m.ministry.model.dbio.Person();
 		person.setFk_ssmUserID(user.getUserID());
 		person.select();
 		result.putValue("personID",person.getPersonID()+"");
 		ctx.setReturnValue(result);
 		ctx.goToView("home");
 	}
 
 	/**
 	 * Action: showMPD
 	 *
 	 */
 	public void showMPD(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults();
 
 			results.putValue("balance", getPreference(ctx.getProfileID(),
 					"CURRENT_BALANCE", "N/A"));
 
 			ctx.setReturnValue(results);
 			ctx.goToView("mpd");
 
 		} catch (Exception e) {
 			if (ctx.getProfileID() == null) {
 				ctx.goToURL("/Error.jsp?Reason=Timeout");
 			}
 			log.error("Failed to perform loginStaffWeb", e);
 		}
 
 	}
 
 	/**
 	 * Action: loginStaffWeb
 	 *
 	 */
 	public void loginStaffWeb(ActionContext ctx) {
 
 		try {
 			ActionResults results = new ActionResults();
 
 			// String view = "";
 			boolean isStaff = ((Boolean) (ctx.getProfile().get("IsStaff")))
 					.booleanValue();
 			log.debug("Staff: " + isStaff);
 
 			if (isStaff) {
 				results.putValue("mode", "staff");
 				results.putValue("username", "cccstaff");
 				results.putValue("password", "vonette");
 				String staffwebpath = ctx.getInputString("staffwebpath");
 				if (staffwebpath != null) {
 					if (staffwebpath.equals("Reimbursements")) {
 						results
 								.putValue("staffwebpath",
 										"ss/pages/EFormFrame.html?MainFrame=../servlet/ReimbServlet");
 					} else if (staffwebpath.equals("ReimbursementAdvances")) {
 						results
 								.putValue("staffwebpath",
 										"ss/pages/EFormFrame.html?MainFrame=../servlet/ReimbAdvServlet");
 					} else if (staffwebpath.equals("AdditionalSalaryRequest")) {
 						results
 								.putValue("staffwebpath",
 										"ss/pages/EFormFrame.html?MainFrame=../servlet/AsrServlet");
 					} else if (staffwebpath.equals("SalaryCalc2002Servlet")) {
 						results
 								.putValue("staffwebpath",
 										"ss/pages/EFormFrame.html?MainFrame=../servlet/SalaryCalc2002Servlet");
 					} else {
 						results.putValue("staffwebpath", staffwebpath);
 					}
 				} else {
 					results.putValue("staffwebpath", "");
 				}
 			} else {
 
 				log.debug("notStaff...");
 				// Not staff
 				results.putValue("mode", "nonstaff");
 				ctx.setReturnValue(results);
 				ctx.goToView("errorStaffWeb");
 
 			}
 
 			ctx.setReturnValue(results);
 			ctx.goToView("loginStaffWeb");
 		} catch (Exception e) {
 			log.error("Failed to perform loginStaffWeb", e);
 		}
 
 	}
 
 	/**
 	 * Action: loginStaffResources
 	 *
 	 */
 	public void loginStaffResources(ActionContext ctx) {
 
 		try {
 			ActionResults results = new ActionResults();
 
 			// String view = "";
 			boolean isStaff = ((Boolean) (ctx.getProfile().get("IsStaff")))
 					.booleanValue();
 			log.debug("Staff: " + isStaff);
 
 			if (isStaff) {
 				results.putValue("mode", "staff");
 				results.putValue("username", "cccstaff");
 				results.putValue("password", "vonette");
 				if (ctx.getInputString("staffwebpath") != null) {
 					results.putValue("staffwebpath", ctx
 							.getInputString("staffwebpath"));
 				} else {
 					results.putValue("staffwebpath", "");
 				}
 			} else {
 
 				log.debug("notStaff...");
 				// Not staff
 				results.putValue("mode", "nonstaff");
 				ctx.setReturnValue(results);
 				ctx.goToView("errorStaffResources");
 
 			}
 
 			ctx.setReturnValue(results);
 			ctx.goToView("loginStaffResources");
 		} catch (Exception e) {
 			log.error("Failed to perform loginStaffResources", e);
 		}
 
 	}
 
 	/**
 	 * Action: loginPS
 	 *
 	 */
 	public void loginPS(ActionContext ctx) {
 
 		final String[] sections = new String[] { "PFormFrame",
 				"servlet/SaiServlet", "servlet/ReimbServlet",
 				"servlet/ReimbAdvServlet", "servlet/AsrServlet",
 				"servlet/SalaryCalcServlet" };
 
 		try {
 			ActionResults results = new ActionResults();
 			String view = "";
 			if ((ctx.getProfile() == null)
 					&& ((ctx.getSession() == null) || ctx.getSession().isNew() || (ctx
 							.getSessionValue("loggedIn") == null))) {
 				view = "login";
 			} else {
 				boolean isStaff = ((Boolean) (ctx.getProfile().get("IsStaff")))
 						.booleanValue();
 				String section = ctx.getInputString("section", sections);
 
 				String password = getEncryptedPreference(ctx.getProfileID(),
 						PREF_NAME_CACHED_PS_PASSWORD);
 				String enableSSO = getPreference(ctx.getProfileID(),
 						PREF_NAME_ENABLE_PS_SSO, PREF_ENABLE_SSO_YES);
 				String accountNo = getPreference(ctx.getProfileID(),
 						PREF_NAME_CACHED_PS_USERNAME);
 
 				if (!isStaff) {
 
 					log.debug("notStaff...");
 					// Not staff
 					results.putValue("mode", "nonstaff");
 					view = "errorPS";
 
 				} else if (!enableSSO.equals(PREF_ENABLE_SSO_NO)
 						&& !isNullOrEmpty(password)
 						&& !isNullOrEmpty(accountNo)) {
 					// Haven't selected to ignore SSO and password is non-null
 					// They are staff. Go for it
 					log.debug("Haven't selected to ignore SSO and password is non-null");
 					results.putValue("section", section);
 					results.putValue("username", "cccstaff");
 					results.putValue("password", "vonette");
 
 					results.putValue("psaccountno", accountNo);
 					results.putValue("pspassword", password);
 					view = "loginPS";
 
 				} else if (!enableSSO.equals(PREF_ENABLE_SSO_NO)
 						&& isNullOrEmpty(password)) {
 					// If haven't selected to ignore SSO, and password is
 					// null...
 					log.debug("If haven't selected to ignore SSO, and password is null...");
 					view = "setupPS";
 				} else if (enableSSO.equals(PREF_ENABLE_SSO_NO)) {
 					view = " ";
 
 					results.putValue("section", section);
 					results.putValue("username", "cccstaff");
 					results.putValue("password", "vonette");
 
 				} else {
 					// Who knows?
 					results.putValue("mode", "unknown");
 					view = "errorPS";
 				}
 			}
 			ctx.setReturnValue(results);
 			ctx.goToView(view);
 
 		} catch (Exception e) {
 			log.error("Failed to perform loginPS", e);
 		}
 
 	}
 
 	public void showSetupPS(ActionContext ctx) {
 		try {
 
 			ActionResults results = new ActionResults("showSetupPS");
 
 			boolean isStaff = ((Boolean) (ctx.getProfile().get("IsStaff")))
 					.booleanValue();
 			String defaultUsername = getPreference(ctx.getProfileID(),
 					PREF_NAME_CACHED_PS_USERNAME, "");
 			String view;
 
 			if (!isStaff) {
 				// Not staff
 
 				results.putValue("mode", "nonstaff");
 				view = "errorPS";
 
 			} else {
 
 				results.putValue("mode", "initialsetup");
 				results.putValue("accountno", defaultUsername);
 				view = "setupPS";
 			}
 
 			ctx.setReturnValue(results);
 			ctx.goToView(view);
 
 		} catch (Exception e) {
 		}
 
 	}
 
 	public void managePS(ActionContext ctx) {
 		try {
 
 			ActionResults results = new ActionResults("managePS");
 
 			boolean isStaff = ((Boolean) (ctx.getProfile().get("IsStaff")))
 					.booleanValue();
 			String defaultUsername = getPreference(ctx.getProfileID(),
 					PREF_NAME_CACHED_PS_USERNAME, "");
 			String view;
 
 			if (!isStaff) {
 				// Not staff
 
 				results.putValue("mode", "nonstaff");
 				view = "errorPS";
 
 			} else {
 
 				results.putValue("mode", "initialsetup");
 				results.putValue("accountno", defaultUsername);
 				view = "managePS";
 			}
 
 			ctx.setReturnValue(results);
 			ctx.goToView(view);
 
 		} catch (Exception e) {
 		}
 
 	}
 
 	public void showSetupEmail(ActionContext ctx) {
 		try {
 
 			ActionResults results = new ActionResults("showSetupEmail");
 
 			if (getIntSessionValue(ctx, EMAIL_ATTEMPTS_SESSION_CNT) >= MAX_EMAIL_ATTEMPTS) {
 				results.putValue("mode", "toomanyattempts");
 				ctx.setReturnValue(results);
 				ctx.goToView("errorEmail");
 			} else {
 				results.putValue("mode", "initialsetup");
 				ctx.setReturnValue(results);
 				ctx.goToView("setupEmail");
 			}
 
 		} catch (Exception e) {
 		}
 
 	}
 
 	/**
 	 * Action: setupPS
 	 *
 	 */
 	public void setupPS(ActionContext ctx) {
 
 		try {
 
 			// String accountNo = (String) ctx.getProfile().get("AccountNo");
 			String enableSSO = ctx.getInputString("SSOAction", new String[] {
 					"Yes", "No", "Ignore" });
 
 			ActionResults results = new ActionResults();
 
 			if (enableSSO.equals("Yes")) {
 
 				String psAccountNo = ctx.getInputString("accountno", true);
 				String psPassword = ctx.getInputString("password", true);
 
 				setPreference(ctx.getProfileID(), PREF_NAME_ENABLE_PS_SSO, "",
 						PREF_ENABLE_SSO_YES);
 				setPreference(ctx.getProfileID(), PREF_NAME_CACHED_PS_USERNAME,
 						"", psAccountNo);
 
 				setEncryptedPreference(ctx.getProfileID(),
 						PREF_NAME_CACHED_PS_PASSWORD, psPassword);
 
 				results.putValue("section", "SaiServlet");
 				results.putValue("username", "cccstaff");
 				results.putValue("password", "vonette");
 				results.putValue("psaccountno", psAccountNo);
 				results.putValue("pspassword", psPassword);
 
 				ctx.setReturnValue(results);
 				ctx.goToView("loginPS");
 			} else if (enableSSO.equals("No")) {
 				setPreference(ctx.getProfileID(), PREF_NAME_ENABLE_PS_SSO, "",
 						PREF_ENABLE_SSO_KEEP_ASKING);
 				results.putValue("username", "cccstaff");
 				results.putValue("password", "vonette");
 
 				ctx.setReturnValue(results);
 				ctx.goToView("loginPS");
 
 			} else { // Ignore
 
 				setPreference(ctx.getProfileID(), PREF_NAME_ENABLE_PS_SSO, "",
 						PREF_ENABLE_SSO_NO);
 
 				results.putValue("username", "cccstaff");
 				results.putValue("password", "vonette");
 
 				ctx.setReturnValue(results);
 				ctx.goToView("loginPS");
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 * Action: setupEmail
 	 *
 	 */
 	public void setupEmail(ActionContext ctx) {
 		try {
 
 			String email = (String) ((Hashtable) ctx.getSessionValue("profile"))
 					.get("Email");
 			String enableSSO = ctx.getInputString("SSOAction", new String[] {
 					"Yes", "No", "Ignore" });
 
 			ActionResults results = new ActionResults();
 			String username = "";
 
 			// If this email string contains *@uscm.org
 			if (email.indexOf("@" + MAIL_DOMAIN) > 0) {
 				username = email.substring(0, email.indexOf("@" + MAIL_DOMAIN));
 			} else {
 				username = email;
 			}
 
 			if (enableSSO.equals("Yes")) {
 				String password = ctx.getInputString("password", true);
 
 				// int attempts = getIntSessionValue(ctx.getSession(),
 				// EMAIL_ATTEMPTS_SESSION_CNT);
 				// log.debug("attempt counter: " + attempts);
 
 				// Does this password work?
 				if (verifyEmailPassword(username, password)) {
 
 					setEncryptedPreference(ctx.getProfileID(),
 							PREF_NAME_CACHED_EMAIL_PASSWORD, password);
 					setPreference(ctx.getProfileID(),
 							PREF_NAME_ENABLE_EMAIL_SSO, "", PREF_ENABLE_SSO_YES);
 
 					results.putValue("username", username);
 					results.putValue("password", password);
 
 					ctx.setReturnValue(results);
 					ctx.goToView("loginEmail");
 					//
 				} else {
 					if (incrementIntSessionValue(ctx,
 							EMAIL_ATTEMPTS_SESSION_CNT) >= MAX_EMAIL_ATTEMPTS) {
 						results.putValue("mode", "toomanyattempts");
 						ctx.setReturnValue(results);
 						ctx.goToView("errorEmail");
 					} else {
 						results.putValue("mode", "badpassword");
 						ctx.setReturnValue(results);
 						ctx.goToView("setupEmail");
 					}
 				}
 
 			} else if (enableSSO.equals("No")) {
 
 				setPreference(ctx.getProfileID(), PREF_NAME_ENABLE_EMAIL_SSO,
 						"", PREF_ENABLE_SSO_KEEP_ASKING);
 				// Just go to exchange anyway (without logging in automatically)
 				results.putValue("username", username);
 
 				ctx.setReturnValue(results);
 				ctx.goToView("loginEmail");
 
 			} else { // Ignore
 
 				setPreference(ctx.getProfileID(), PREF_NAME_ENABLE_EMAIL_SSO,
 						"", PREF_ENABLE_SSO_NO);
 
 				results.putValue("username", username);
 				ctx.setReturnValue(results);
 				ctx.goToView("loginEmail");
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 		// _prefAdaptor.list
 	}
 
 	/**
 	 * Action: loginEmail
 	 *
 	 */
 	public void loginEmail(ActionContext ctx) {
 		String goToView = "";
 
 		ActionResults results = new ActionResults();
 
 		try {
 			Hashtable profileHash = (Hashtable) ctx.getSessionValue("profile");
 			if ((profileHash == null)
 					&& ((ctx.getSession() == null) || ctx.getSession().isNew() || (ctx
 							.getSessionValue("loggedIn") == null))) {
 				goToView = "login";
 			} else {
 				// First, do they have a valid 'uscm.org' email address
 				String email = (String) profileHash.get("Email");
 
 				log.debug("email: " + email);
 
 				// If this email string contains *@uscm.org
 				if (email.indexOf("@" + MAIL_DOMAIN) > 0) {
 
 					// Does a cached password exist?
 					String password = getEncryptedPreference(
 							ctx.getProfileID(), PREF_NAME_CACHED_EMAIL_PASSWORD);
 					String username = email.substring(0, email.indexOf("@"
 							+ MAIL_DOMAIN));
 
 					boolean enableSSO = !(getPreference(ctx.getProfileID(),
 							PREF_NAME_ENABLE_EMAIL_SSO,
 							PREF_ENABLE_SSO_KEEP_ASKING)
 							.equalsIgnoreCase(PREF_ENABLE_SSO_NO));
 
 					log.debug("enableSSO: " + enableSSO);
 					log.debug(getPreference(ctx.getProfileID(),
 							PREF_NAME_ENABLE_EMAIL_SSO)); // != "False");
 					// Yes, has cached password and sso enabled
 					if ((password != null) && (enableSSO != false)) {
 
 						log.debug("username: " + username);
 
 						// is password still valid?
 						if (verifyEmailPassword(username, password)) {
 							log.debug("verified password");
 
 							results.putValue("mode", "login");
 							results.putValue("username", username);
 							results.putValue("password", password);
 
 							goToView = "loginEmail";
 
 							// password not valid
 						} else {
 							results.putValue("mode", "updatepassword");
 							goToView = "setupEmail";
 						}
 
 						// No, no cached password
 					} else {
 						log.debug("no cached password or sso disabled");
 						if (enableSSO) {
 							results.putValue("mode", "initialsetup");
 							goToView = "setupEmail";
 						} else {
 							results.putValue("mode", "login");
 							results.putValue("username", username);
 							goToView = "loginEmail";
 
 						}
 					}
 					// No "uscm.org" type email address
 				} else {
 					results.putValue("mode", "update");
 					results.putValue("problem", "email");
 					goToView = "setupEmail";
 				}
 			}
 
 			ctx.setReturnValue(results);
 			ctx.goToView(goToView);
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			log.debug("Profile:"
 					+ (Hashtable) ctx.getSessionValue("profile"));
 			log.debug("Session:"
 					+ ObjectHashUtil.obj2hash(ctx.getSession().toString()));
 			log.debug("Request:"
 					+ ObjectHashUtil.obj2hash(ctx.getRequest().toString()));
 		}
 
 	}
 
 	private String getEncryptedPreference(String profileID, String name)
 			throws EncryptorException {
 
 		StaffSitePref pref = _encryptedPreferences.getPreference(profileID,
 				name);
 		return (pref != null) ? pref.getValue() : null;
 
 		// return _prefEncryptor.decrypt(getPreference(profileID, name));
 	}
 
 	private void setEncryptedPreference(String profileID, String name,
 			String cleartext) throws EncryptorException {
 
 		_encryptedPreferences.savePreference(profileID, name, "", cleartext);
 
 		// setPreference(profileID, name,"", _prefEncryptor.encrypt(cleartext));
 		// _encryptedPreferences.savePreference(profileID, name, "", cleartext);
 	}
 
 	private boolean verifyEmailPassword(String username, String password)
 			throws Exception {
 
 		try {
 			javax.mail.Session session = javax.mail.Session.getDefaultInstance(
 					System.getProperties(), null);
 
 			// Connect to host
 			javax.mail.Store store = session.getStore("imap");
 			store.connect(MAIL_HOST, -1, username, password);
 			store.close();
 			log.debug("SUCCESS Authentication: user:" + username);
 			return true;
 		} catch (javax.mail.AuthenticationFailedException authfailed) {
 			// Catch wrong username/password type errors
 			log.info(authfailed.toString());
 			log.info("FAILED Authentication: user:" + username);
 			return false;
 		}
 		// Propogate all other errors
 
 	}
 
 	private String getPreference(String profileID, String name) {
 		// StaffSitePref pref = _preferences.getPreference(profileID, name);
 		return _preferences.getPreferenceValue(profileID, name);
 	}
 
 	private String getPreference(String profileID, String name,
 			String defaultValue) {
 		String prefValue = getPreference(profileID, name);
 		return (prefValue != null ? prefValue : defaultValue);
 	}
 
 	private void setPreference(String profileID, String name,
 			String displayName, String value) {
 		_preferences.savePreference(profileID, name, displayName, value);
 	}
 
 	private int getIntSessionValue(ActionContext ctx, String name) {
 
 		// int value;
 		String valueString = (String) ctx.getSessionValue(name);
 
 		if (valueString == null)
 			return 0;
 		else
 			return Integer.parseInt(valueString);
 	}
 
 	private int incrementIntSessionValue(ActionContext ctx, String name) {
 		// Bump the failed attempt counter for this session
 		int newValue = getIntSessionValue(ctx, name) + 1;
 		ctx.setSessionValue(name, Integer.toString(newValue));
 
 		return newValue;
 	}
 
 	boolean isNullOrEmpty(String string) {
 		return !(string != null && string.length() > 0);
 	}
 
 
 
 	private String getHomeUrl(HttpServletRequest request) {
 		return helper.getService(request) + "?action=showHome";
 	}
 
 	public void clearConnexionBarCache(ActionContext ctx) {
 		ConnexionBar.clearCache();
 	}
 
 
 	public static void recordLocation(HttpServletRequest request) {
 		//The following is done to make this work on both Tomcat 4 and Tomcat 5.5
 		StringBuffer location;
 		//in Tomcat 5.5, the following gets what we want (but is only part of servlet spec 2.4):
 		String locationStr = (String) request.getAttribute("javax.servlet.forward.request_uri");
 		if (locationStr == null)
 			// in Tomcat 4 this works (in Tomcat 5, this will return the path to this jsp; not what we want):
 			location = request.getRequestURL();
 		else
 			location = new StringBuffer(locationStr);
 
 		//same reasoning as above
 		String queryString = (String) request.getAttribute("javax.servlet.forward.query_string");
 		if (queryString == null)
 			queryString = request.getQueryString();
 		if (queryString != null)
 			location.append('?').append(queryString);
 
 		HttpSession session = request.getSession();
 		session.setAttribute("onLogInGoto", location.toString());
 	}
 }
 
