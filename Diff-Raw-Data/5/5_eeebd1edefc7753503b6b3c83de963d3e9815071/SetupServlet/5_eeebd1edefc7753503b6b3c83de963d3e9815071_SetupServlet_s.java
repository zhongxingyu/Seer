 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.util.Collection;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiConfiguration;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.db.DatabaseConnection;
 import org.jamwiki.db.WikiDatabase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class SetupServlet extends JAMWikiServlet {
 
 	private static WikiLogger logger = WikiLogger.getLogger(SetupServlet.class.getName());
 	private static final int MINIMUM_JDK_MAJOR_VERSION = 1;
 	private static final int MINIMUM_JDK_MINOR_VERSION = 4;
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (!Utilities.isFirstUse()) {
 			throw new WikiException(new WikiMessage("setup.error.notrequired"));
 		}
 		String function = (request.getParameter("function") == null) ? request.getParameter("override") : request.getParameter("function");
 		if (function == null) function = "";
 		try {
 			if (!this.verifyJDK()) {
 				String minimumVersion = MINIMUM_JDK_MAJOR_VERSION + "." + MINIMUM_JDK_MINOR_VERSION;
 				throw new WikiException(new WikiMessage("setup.error.jdk", minimumVersion, System.getProperty("java.version")));
 			}
			if (StringUtils.hasText(function)) {
 				ServletUtil.redirect(next, WikiBase.DEFAULT_VWIKI, Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
			} else if (initialize(request, next, pageInfo)) {
 				view(request, next, pageInfo);
 			}
 		} catch (Exception e) {
 			handleSetupError(request, next, pageInfo, e);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void handleSetupError(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Exception e) {
 		// reset properties
 		Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, false);
 		if (!(e instanceof WikiException)) {
 			logger.severe("Setup error", e);
 		}
 		try {
 			this.view(request, next, pageInfo);
 		} catch (Exception ex) {
 			logger.severe("Unable to set up page view object for setup.jsp", ex);
 		}
 		if (e instanceof WikiException) {
 			WikiException we = (WikiException)e;
 			next.addObject("messageObject", we.getWikiMessage());
 		} else {
 			next.addObject("messageObject", new WikiMessage("error.unknown", e.getMessage()));
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void initParams() {
 		this.layout = false;
 		this.displayJSP = "setup";
 	}
 
 	/**
 	 *
 	 */
 	private boolean initialize(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		setProperties(request, next);
 		WikiUser user = new WikiUser();
 		setAdminUser(request, user);
 		Vector errors = validate(request, user);
 		if (errors.size() > 0) {
 			this.view(request, next, pageInfo);
 			next.addObject("errors", errors);
 			next.addObject("username", user.getUsername());
 			next.addObject("newPassword", request.getParameter("newPassword"));
 			next.addObject("confirmPassword", request.getParameter("confirmPassword"));
 			return false;
 		} else if (previousInstall() && request.getParameter("override") == null) {
 			// user is trying to do a new install when a previous installation exists
 			next.addObject("upgrade", "true");
 			next.addObject("username", user.getUsername());
 			next.addObject("newPassword", request.getParameter("newPassword"));
 			next.addObject("confirmPassword", request.getParameter("confirmPassword"));
 			return false;
 		} else {
 			Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, true);
 			Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
 			WikiBase.reset(request.getLocale(), user);
 			// FIXME - disabled automatic login because it's not possible(?)
 			// with Acegi Security
 			// Utilities.login(request, null, user, false);
 			Environment.saveProperties();
 			return true;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private boolean previousInstall() {
 		String driver = Environment.getValue(Environment.PROP_DB_DRIVER);
 		String url = Environment.getValue(Environment.PROP_DB_URL);
 		String userName = Environment.getValue(Environment.PROP_DB_USERNAME);
 		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, null);
 		try {
 			DatabaseConnection.testDatabase(driver, url, userName, password, true);
 		} catch (Exception e) {
 			// no previous database, all good
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	private void setAdminUser(HttpServletRequest request, WikiUser user) throws Exception {
 		user.setUsername(request.getParameter("username"));
 		user.setPassword(Encryption.encrypt(request.getParameter("newPassword")));
 		user.setCreateIpAddress(request.getRemoteAddr());
 		user.setLastLoginIpAddress(request.getRemoteAddr());
 		user.setAdmin(true);
 	}
 
 	/**
 	 *
 	 */
 	private void setProperties(HttpServletRequest request, ModelAndView next) throws Exception {
 		Environment.setValue(Environment.PROP_BASE_FILE_DIR, request.getParameter(Environment.PROP_BASE_FILE_DIR));
 		Environment.setValue(Environment.PROP_FILE_DIR_FULL_PATH, request.getParameter(Environment.PROP_FILE_DIR_FULL_PATH));
 		Environment.setValue(Environment.PROP_FILE_DIR_RELATIVE_PATH, request.getParameter(Environment.PROP_FILE_DIR_RELATIVE_PATH));
 		int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
 		if (persistenceType == WikiBase.PERSISTENCE_INTERNAL_DB) {
 			WikiDatabase.setupDefaultDatabase(Environment.getInstance());
 		} else if (persistenceType == WikiBase.PERSISTENCE_EXTERNAL_DB) {
 			Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
 			Environment.setValue(Environment.PROP_DB_DRIVER, request.getParameter(Environment.PROP_DB_DRIVER));
 			Environment.setValue(Environment.PROP_DB_TYPE, request.getParameter(Environment.PROP_DB_TYPE));
 			Environment.setValue(Environment.PROP_DB_URL, request.getParameter(Environment.PROP_DB_URL));
 			Environment.setValue(Environment.PROP_DB_USERNAME, request.getParameter(Environment.PROP_DB_USERNAME));
 			Encryption.setEncryptedProperty(Environment.PROP_DB_PASSWORD, request.getParameter(Environment.PROP_DB_PASSWORD), null);
 			next.addObject("dbPassword", request.getParameter(Environment.PROP_DB_PASSWORD));
 		}
 	}
 
 	/**
 	 *
 	 */
 	private Vector validate(HttpServletRequest request, WikiUser user) throws Exception {
 		Vector errors = Utilities.validateSystemSettings(Environment.getInstance());
 		if (!StringUtils.hasText(user.getUsername())) {
 			user.setUsername("");
 			errors.add(new WikiMessage("error.loginempty"));
 		}
 		String newPassword = request.getParameter("newPassword");
 		String confirmPassword = request.getParameter("confirmPassword");
 		if (newPassword != null || confirmPassword != null) {
 			if (newPassword == null) {
 				errors.add(new WikiMessage("error.newpasswordempty"));
 			} else if (confirmPassword == null) {
 				errors.add(new WikiMessage("error.passwordconfirm"));
 			} else if (!newPassword.equals(confirmPassword)) {
 				errors.add(new WikiMessage("admin.message.passwordsnomatch"));
 			}
 		}
 		return errors;
 	}
 
 	/**
 	 *
 	 */
 	private boolean verifyJDK() {
 		try {
 			String jdk = System.getProperty("java.version");
 			StringTokenizer tokens = new StringTokenizer(jdk, ".");
 			int major = new Integer(tokens.nextToken()).intValue();
 			int minor = new Integer(tokens.nextToken()).intValue();
 			if (major < MINIMUM_JDK_MAJOR_VERSION) return false;
 			if (major == MINIMUM_JDK_MAJOR_VERSION && minor < MINIMUM_JDK_MINOR_VERSION) return false;
 			return true;
 		} catch (Exception e) {
 			logger.warning("Failure determining JDK version", e);
 			// allow setup to continue if JDK version not found
 			return true;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		pageInfo.setAction(WikiPageInfo.ACTION_SETUP);
 		pageInfo.setSpecial(true);
 		pageInfo.setPageTitle(new WikiMessage("setup.title"));
 		Collection dataHandlers = WikiConfiguration.getInstance().getDataHandlers();
 		next.addObject("dataHandlers", dataHandlers);
 	}
 }
