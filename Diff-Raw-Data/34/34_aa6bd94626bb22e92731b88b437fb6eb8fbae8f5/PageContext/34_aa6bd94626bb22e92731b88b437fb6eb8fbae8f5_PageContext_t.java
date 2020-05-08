 package uk.ac.warwick.dcs.boss.frontend;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.java.dev.weblets.WebletUtils;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 
 import uk.ac.warwick.dcs.boss.frontend.sites.SystemPageFactory;
 import uk.ac.warwick.dcs.boss.model.FactoryException;
 import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
 import uk.ac.warwick.dcs.boss.model.session.ISessionAuthenticator;
 import uk.ac.warwick.dcs.boss.model.session.UserSession;
 import uk.ac.warwick.dcs.boss.model.session.SessionException;
 import uk.ac.warwick.dcs.boss.model.session.SessionAutenticatorFactory;
 import uk.ac.warwick.dcs.boss.model.session.UserNotFoundException;
 
 /**
  * PageContext contains absolutely everything needed to be known about a particularly page request.
  * It is provided with all page accesses (and stored in the page's related Velocity context as well).
  * @author davidbyard
  */
 public class PageContext {
 
 	private Logger logger;
 	private String siteName;
 	private String pageName;
 	private Date startingTime;
 	private UserSession session;
 	
 	private ServletContext context;
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 
 	/**
 	 * Generate a PageContext given a request and a response.
 	 * @param context is the context.
 	 * @param request is the request.
 	 * @param response is the response.
 	 */
 	public PageContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
 		this.context = context;
 		this.request = request;
 		this.response = response;
 		this.startingTime = new Date();
 		this.session = null;
 		
 		PageSession sessionAttribute = (PageSession)request.getSession().getAttribute("session");
 		if (sessionAttribute != null) {
 			this.session = sessionAttribute.getUserSession();
 		}
 		
 		String pathInfo = request.getPathInfo();
 		
 		if (pathInfo == null) {
 			this.siteName = "system";
 			this.pageName = "home";
 			return;
 		}
 		
 		if (pathInfo.startsWith("/")) {
 			pathInfo = pathInfo.substring(1); 
 		}
 		
 		if (!pathInfo.contains("/")) {
 			this.siteName = "system";
 			this.pageName = "home";
 			return;
 		}
 		
 		int slashPosition = pathInfo.indexOf('/');
 		this.siteName = pathInfo.substring(0, slashPosition);
 		this.pageName = pathInfo.substring(slashPosition + 1);
 		
 		// Generate the logger in the context of this page.
 		this.logger = Logger.getLogger("sites." + this.siteName + "." + this.pageName);
 	}
 
 	/**
 	 * Log something.
 	 * @param level is the log level.
 	 * @param message is the message.
 	 */
 	public void log(Level level, String message) {
 		if (this.session == null) {
 			logger.log(level, "anon@" + request.getRemoteAddr() + ": " + message);
 		} else if (this.session.getPersonBinding() == null) {
 			logger.log(level, "anon@" + request.getRemoteAddr() + ": " + message);
 		} else {
 			logger.log(level, this.session.getPersonBinding().getUniqueIdentifier() + "@" + request.getRemoteAddr() + ": " + message);
 		}
 	}
 
 	/**
 	 * Log something.
 	 * @param level is the log level.
 	 * @param throwable is the throwable.
 	 */
 	public void log(Level level, Throwable throwable) {
 		if (this.session == null) {
 			logger.log(level, "anon@" + request.getRemoteAddr() + ": Unhandled exception");
 			logger.trace("Tracing...", throwable);
 		} else if (this.session.getPersonBinding() == null) {
 			logger.log(level, "anon@" + request.getRemoteAddr() + ": Unhandled exception");
 			logger.trace("Tracing...", throwable);
 		} else {
 			logger.log(level, this.session.getPersonBinding().getUniqueIdentifier() + "@" + request.getRemoteAddr() + ": Unhandled exception");
 			logger.trace("Tracing...", throwable);
 		}
 	}
 	
 	/**
 	 * Turn the response into a file download.
 	 * @param mimeType is the mime type of the download.
 	 * @param filename is the filename of the download.
 	 * @param is is the stream containing the data to send to the the user.
 	 * @throws ServletException
 	 */
 	public void performSendFile(String mimeType, String filename, InputStream is) throws ServletException {
 		response.setContentType("application/x-download");
 		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
 		
 		try {
 			byte buffer[] = new byte[1024];
 			int nread = -1;
 			while ((nread = is.read(buffer)) != -1) {
 				response.getOutputStream().write(buffer, 0, nread);
 			}					
 		} catch (IOException e) {
 			throw new ServletException("IO error returning file stream", e);
 		}
 	}
 	
 	/**
 	 * Turn the response into a file download and return the output stream to write to.
 	 * @param mimeType is the mime type of the download.
 	 * @param filename is the filename of the download.
 	 * @return an output stream that you should write the download to.
 	 * @throws ServletException
 	 */
 	public OutputStream performManualSendFile(String mimeType, String filename) throws ServletException {
 		response.setContentType("application/x-download");
 		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
 		
 		try {
 			return response.getOutputStream();
 		} catch (IOException e) {
 			throw new ServletException("IO error returning file stream", e);
 		}
 	}
 	
 	/**
 	 * Log in a user.
 	 * @param locale is the locale (ignored currently)
 	 * @param username is the username
 	 * @param password is the password
 	 * @throws UserNotFoundException is thrown if the username or password is incorrect.
 	 * @throws SessionException
 	 */
 	public void performLogin(String locale, String username, String password) throws UserNotFoundException, SessionException {
 		ISessionAuthenticator authenticator;
 		try {
 			SessionAutenticatorFactory saf = (SessionAutenticatorFactory)FactoryRegistrar.getFactory(SessionAutenticatorFactory.class);
 			authenticator = saf.getInstance();
 		} catch (FactoryException e) {
 			throw new SessionException("factory error", e);
 		}
 		
 		UserSession newSession = authenticator.performLogin(locale, username, password);
 		if (this.session != null) {
 			this.session.logout();
 		}
 		this.session = newSession;
 		request.getSession().setAttribute("session", new PageSession(newSession));
 	}
 	
 	/**
 	 * Log out the current user.
 	 */
 	public void performLogout() {
 		if (session != null) {
 			session = null;
 			request.getSession().invalidate();
 		}
 	}
 	
 	/**
 	 * Redirect to another URL.
 	 * @param url is the URL to redirect to.
 	 * @throws IOException
 	 */
 	public void performRedirect(String url) throws IOException {
 		response.sendRedirect(url);
 	}
 	
 	/**
 	 * Render page template as the response.
 	 * @param template is the template to render.
 	 * @param context is the template context.
 	 * @throws IOException
 	 */
 	public void renderTemplate(Template template, VelocityContext context) throws IOException {
 		response.setContentType("text/html");
 		template.merge(context, response.getWriter());
 	}
 	
 	/**
 	 * Get the site this request was for.
 	 * @return the site name.
 	 */
 	public String getSiteName() {
 		return siteName;
 	}
 
 	/**
 	 * Get the page name this request was for..
 	 * @return 
 	 */
 	public String getPageName() {
 		return pageName;
 	}
 
 	/**
 	 * Get the time the request was made.
 	 * @return the time the request was made.
 	 */
 	public Date getStartingTime() {
 		return startingTime;
 	}
 	
 	/**
 	 * Get the user session bound to this request.
 	 * @return a user session, or null if the user is anonymous
 	 */
 	public UserSession getSession() {
 		return session;
 	}
 	
 	/**
 	 * Get a parameter passed in the request by either GET or POST.
 	 * @param parameter is the parameter name
 	 * @return the value, or null if the parameter was not provided or was empty
 	 */
 	public String getParameter(String parameter) {
 		String result = request.getParameter(parameter);
 		if (result == null) {
 			return null;
 		} else {
 			if (result.trim().equals("")) {
 				return null;
 			}
 			return result.trim();
 		}
 	}
	
	/**
	 * Get a group of values of a parameter passed in the request
	 * @param parameter is the parameter name
	 * @return the value, or null if the parameter was not provided
	 */
	public String[] getParameterValues(String parameter) {
		String[] result = request.getParameterValues(parameter); 
		if (result == null) {
			return null;
		}
		else
			return result;
	}
 
 	/**
 	 * Set the content type of the response.
 	 * @param contentType
 	 */
 	public void setContentType(String contentType) {
 		response.setContentType(contentType);
 	}
 	
 	/**
 	 * Set the status code of the response.
 	 * @param responseCode
 	 */
 	public void setHttpStatus(int responseCode) {
 		response.setStatus(responseCode);
 	}
 	
 	/**
 	 * Get the relative path to static files.
 	 * @return
 	 */
 	public String getStaticPath() {
 		try {
 			URL url = new URL(request.getRequestURL().toString());
 			return new URL(url, request.getContextPath()).toString();
 		} catch (MalformedURLException e) {
 			return request.getContextPath();
 		}
 	}
 	
 	/**
 	 * Get the full URL required to re-access this page, including GET parameters.
 	 * @return a valid URL
 	 */
 	public String getFullCurrentUrl() {
 		return request.getRequestURL() + "?" + request.getQueryString();
 	}
 	
 	/**
 	 * Get the full URL required to re-access this page, minus GET parameters.
 	 * @return a valid URL
 	 */
 	public String getCurrentPageUrl() {
 		return getPageUrl(siteName, pageName);
 	}
 	
 	/**
 	 * Get the URL of the captcha image.
 	 * @return a valid URL
 	 */
 	public String getCaptchaUrl() {
 		return getStaticPath() + "/captcha.jpg";
 	}
 	
 	/**
 	 * Get the string expected from a captcha.
 	 * @return the expected captcha string
 	 */
 	public String getCaptchaString() {
 		return (String)request.getSession().getAttribute(nl.captcha.servlet.Constants.SIMPLE_CAPCHA_SESSION_KEY);
 	}
 	
 	/**
 	 * Get the URL of a given site and page.
 	 * @param site is the site name
 	 * @param page is the page name
 	 * @return a valid URL
 	 */
 	public String getPageUrl(String site, String page) {
 		return getStaticPath() + "/" + request.getServletPath() + "/" + site + "/" + page; 
 	}
 	
 	/**
 	 * Get the URL of the home page.
 	 * @return a valid URL
 	 */
 	public String getHomeUrl() {
 		return getPageUrl("system", "home");
 	}
 	
 	/**
 	 * Get the URL of the login page, with redirect afterwards
 	 * @param redirectSite is the site to redirect to after login
 	 * @param redirectPage is the page to redirect to after login
 	 * @return a valid URL
 	 */
 	public String getLoginUrl(String redirectUrl) {
 		return getStaticPath() + "/" + request.getServletPath() + "/" + SystemPageFactory.SITE_NAME
 		    + "/" + SystemPageFactory.LOGIN_PAGE
 			+ "?redirectUrl=" + redirectUrl;
 	}
 	
 	/**
 	 * Get the URL of the logout page
 	 * @param redirectSite is the site to redirect to after logout
 	 * @param redirectPage is the page to redirect to after logout
 	 * @return
 	 */
 	public String getLogoutUrl(String redirectUrl) {
 		return getStaticPath() + "/" + request.getServletPath() + "/" + SystemPageFactory.SITE_NAME
 	    + "/" + SystemPageFactory.LOGOUT_PAGE
 		+ "?redirectUrl=" +  redirectUrl;
 	}
 	
 	/**
 	 * Check whether this request included file uploads.
 	 * @return true if the request included file uploads, false if not.
 	 */
 	public boolean hasUploadedFiles() {
 		return ServletFileUpload.isMultipartContent(request);
 	}
 	
 	/**
 	 * Access uploaded files provided with this request.
 	 * @return an iterator for the uploaded files.
 	 * @throws IOException
 	 * @throws FileUploadException
 	 */
 	public FileItemIterator getUploadedFiles() throws IOException, FileUploadException {
 		ServletFileUpload upload = new ServletFileUpload();
 		return upload.getItemIterator(request);
 	}
 
 	/**
 	 * Obtain the configuration file path.
 	 * @return the path to the configuration file.
 	 */
 	public String getConfigurationFilePath() {
 		return this.context.getRealPath("WEB-INF/config.properties");
 	}
 	
 	public String getContextPath() {
 		return this.context.getContextPath();
 	}
 	
 	public String getWebletResourceURL(String weblet, String pathInfo) {
 		return WebletUtils.getURL(weblet, pathInfo);
 	}
 	
 }
