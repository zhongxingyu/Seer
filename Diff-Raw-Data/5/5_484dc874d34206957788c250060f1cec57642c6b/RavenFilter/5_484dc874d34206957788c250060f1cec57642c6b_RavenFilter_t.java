 /*
  * This class has been written using code from cam.cl.raven.RavenValve, a Tomcat Valve, 
  * written by William Billingsley (whb21 at cam.ac.uk).
  * 
  * The main body of the code remains unchanged. All that has been added in version 1 is 
  * additional error handling and a simplified configuration.
  * 
  * RavenValve and RavenFilter achieve the same end result. 
  * 
  * The advantages of RavenFilter are that it can be used on any servlet container without
  * code modification but the Principal object containing the id of the authenticated user 
  * has to be obtained via an HttpSession attribute. 
  * 
  * RavenValve, is only usable within Tomcat.  However, it does allow the Raven user id
  * to be fetched from the standard Principal object available through HttpServletRequest.getRemoteUser(). 
  * 
  * 
  * 
  * This program is free software; you can redistribute it and/or modify it under the terms
  * of the GNU Lesser General Public License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package uk.ac.cam.ucs.webauth;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.Principal;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A Servlet Filter which ensures a user is Raven authenticated.
  * 
  * <h1>Quick Configuration</h2>
  * 
  * <h3>Install Webauth package</h3>
  * 
  * Ensure you have the Raven Java Toolkit classes installed.
  * 
  * <h3>Install the Raven public key certificate</h3>
  * Download the Raven public key certificate from the <a
  * href="https://raven.cam.ac.uk/project/">Raven Project page</a>. Install into
  * your web application at <code>/WEB-INF/raven/pubkey2.crt</code>.
  * 
  * <h3>Configure web.xml</h3>
  * Add a filter definition:
  * 
  * <pre>
  *  &lt;filter&gt;
  *      &lt;filter-name&gt;ravenFilter&lt;/filter-name&gt;
  *      &lt;filter-class&gt;uk.ac.cam.ucs.webauth.RavenFilter&lt;/filter-class&gt;
  *  &lt;/filter&gt;
  * </pre>
  * 
  * Add one or more filter-mapping's for your application. Eg:
  * 
  * <pre>
  *  &lt;filter-mapping&gt;
  *      &lt;filter-name&gt;ravenFilter&lt;/filter-name&gt;
  *      &lt;url-pattern&gt;/private&lt;/url-pattern&gt;
  *  &lt;/filter-mapping&gt;
  * </pre>
  * 
  * <h3>Retrieve authenticated user name</h3>
  * 
 * Get the value of session attribute "RavenRemoteUser".
  * 
  * <p>
 * <code>String userId = request.getSession().getAttribute("RavenRemoteUser");</code>
  * </p>
  * 
  * 
  * <h1>Further Configuration</h2>
  * 
  * <h3>Filter init params</h3>
  * 
  * <table border="1">
  * <tr>
  * <th>Name</th>
  * <th>Default Value</th>
  * <th>Notes</th>
  * <tr>
  * <tr>
  * <td>authenticateUrl</td>
  * <td>https://raven.cam.ac.uk/auth/authenticate.html</td>
  * <td>Optional</td>
  * </tr>
  * <tr>
  * <td>certificatePath</td>
  * <td>/WEB-INF/raven/pubkey2.crt</td>
  * <td>Optional</td>
  * </tr>
  * </table>
  * <br/>
  * 
  * <h3>Error Codes</h3>
  * 
  * Use the following example entries for your web.xml if you wish to provide
  * your own error pages. The codes below are those given by WebauthResponse and
  * passed on by RavenFilter to the servlet container.
  * 
  * <pre>
  *  &lt;!-- 
  *  Raven related Error pages 
  *  --&gt;
  * 
  *  &lt;!-- Authentication cancelled at user's request --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;410&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt; 
  * 
  *  &lt;!-- No mutually acceptable types of authentication available --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;510&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt; 
  * 
  *  &lt;!-- Unsupported authentication protocol version --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;520&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt; 
  * 
  *  &lt;!-- Parameter error in authentication request --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;530&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt;
  * 
  *  &lt;!-- Interaction with the user would be required --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;540&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt;
  * 
  *  &lt;!--  Web server not authorised to use the authentication service --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;560&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt; 
  * 
  *  &lt;!-- Operation declined by the authentication service --&gt;
  *  &lt;error-page&gt;&lt;error-code&gt;570&lt;/error-code&gt;&lt;location&gt;/ravenError.jsp&lt;/location&gt;&lt;/error-page&gt;
  * </pre>
  * 
  * @author whb21 William Billingsley (whb21 at cam.ac.uk)
  * @author pms52 Philip Shore
  * 
  * @version 1
  * @see <a href="https://raven.cam.ac.uk/project/waa2wls-protocol.txt">The
  *      Cambridge Web Authentication System: WAA->WLS communication protocol</a>
  * 
  */
 public class RavenFilter implements Filter {
 	static Log log = LogFactory.getLog(RavenFilter.class);
 
 	/**
 	 * The request parameter name, if present, indicates a WLS Reponse that
 	 * should be validated.
 	 */
 	public static final String WLS_RESPONSE_PARAM = "WLS-Response";
 
 	/** The session attribute name of the Raven WebauthRequest object */
 	static final String SESS_RAVEN_REQ_KEY = "RavenReq";
 
 	/**
 	 * The session attribute name of the RavenState object. This object
 	 * additionally contains the Principal used to identify the authenticated
 	 * user.
 	 */
 	static final String SESS_STORED_STATE_KEY = "RavenState";
 
 	/**
 	 * The name of the request and session attribute containing the
 	 * authenticated user.
 	 */
 	public static String ATTR_REMOTE_USER = "RavenRemoteUser";
 
 	/**
 	 * The default location of the raven public key certificate, relative to the
 	 * web application
 	 */
 	static final String DEFAULT_CERTIFICATE_PATH = "/WEB-INF/raven/pubkey2.crt";
 
 	/** This is the default name for the raven public key */
 	public static final String DEFAULT_KEYNAME = "webauth-pubkey2";
 
 	/** The real path of the public key calculated from the cert init param */
 	private String sCertRealPath = null;
 
 	/**
 	 * The filter init-param param-name of the url to authenticate against.
 	 * Optional. Defaults to https://raven.cam.ac.uk/auth/authenticate.html
 	 */
 	public static String INIT_PARAM_AUTHENTICATE_URL = "authenticateUrl";
 
 	/**
 	 * The filter init-param param-name path to the certificate. Optional.
 	 * Defaults to /WEB-INF/raven/pubkey2.crt
 	 */
 	public static String INIT_PARAM_CERTIFICATE_PATH = "certificatePath";
 
 	/**
 	 * Set to a comma separated list of principals which should be allowed
 	 * access. If blank then any authenticated principal is granted access.
 	 */
 	public static String INIT_PARAM_ALLOWED_PRINCIPALS = "allowedPrincipals";
 
 	/**
 	 * The context parameter to indicate if the filter should be run in testing
 	 * mode. In this mode all requests are automatically authenticated as the
 	 * test user. Defaults to false
 	 */
 	public static String CONTEXT_PARAM_TESTING_MODE = "ravenFilterNoChecking";
 
 	public static String CONTEXT_PARAM_URL_PREFIX = "serverURLPrefix";
 
 	/**
 	 * The url of the raven authenticate page. Optional.
 	 * 
 	 * Defaults to https://raven.cam.ac.uk/auth/authenticate.html
 	 * 
 	 * Use https://raven.cam.ac.uk/auth/authenticate.html or
 	 * https://demo.raven.cam.ac.uk/auth/authenticate.html
 	 */
 	private String sRavenAuthenticatePage = "https://raven.cam.ac.uk/auth/authenticate.html";
 
 	/** KeyStore used by WebauthValidator class */
 	protected KeyStore keyStore = null;
 
 	protected WebauthValidator webauthValidator = null;
 
 	protected boolean testingMode = false;
 
 	protected String serverURLPrefix = null;
 
 	protected Set<String> allowedPrincipals = null;
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 		// check if a different authenticate page is configured.
 		// eg https://demo.raven.cam.ac.uk/auth/authenticate.html
 		String authenticatePage = config
 				.getInitParameter(INIT_PARAM_AUTHENTICATE_URL);
 		if (authenticatePage != null)
 			sRavenAuthenticatePage = authenticatePage;
 
 		// get the path to the raven certificate or use a default
 		String sCertContextPath = config
 				.getInitParameter(INIT_PARAM_CERTIFICATE_PATH);
 		if (sCertContextPath == null)
 			sCertContextPath = DEFAULT_CERTIFICATE_PATH;
 		// calculate real path from web app relative version
 		sCertRealPath = config.getServletContext()
 				.getRealPath(sCertContextPath);
 		log.debug("Certificate will be loaded from: " + sCertRealPath);
 
 		// ensure KeyStore is initialised.
 		keyStore = getKeyStore();
 
 		// ensure WebauthValidator is initialised.
 		webauthValidator = getWebauthValidator();
 
 		String sTestingMode = config.getServletContext().getInitParameter(
 				CONTEXT_PARAM_TESTING_MODE);
 		log.debug("Testing mode: " + sTestingMode);
 		testingMode = "true".equals(sTestingMode);
 
 		serverURLPrefix = config.getServletContext().getInitParameter(
 				CONTEXT_PARAM_URL_PREFIX);
 		log.debug("Server url prefix: " + serverURLPrefix);
 
 		String sAllowedPrincipals = config
 				.getInitParameter(INIT_PARAM_ALLOWED_PRINCIPALS);
 		if (sAllowedPrincipals != null) {
 			allowedPrincipals = new HashSet<String>(
 					Arrays.asList(sAllowedPrincipals.split(",")));
 			log.debug("Restricting access to " + sAllowedPrincipals);
 		} else {
 			log.debug("Granting access to all principals");
 		}
 
 	}
 
 	/**
 	 * Gets a KeyStore and initialises if necessary.
 	 * 
 	 * The caller should ensure the KeyStore is persisted to a safe place.
 	 * 
 	 * @return An initialised KeyStore
 	 */
 	protected KeyStore getKeyStore() {
 		// init a new keystore with the Raven certificate,
 		KeyStore keyStore;
 		try {
 			keyStore = KeyStore.getInstance("JKS");
 			keyStore.load(null, new char[] {}); // Null InputStream, no password
 			CertificateFactory factory = CertificateFactory
 					.getInstance("X.509");
 			Certificate cert = factory.generateCertificate(new FileInputStream(
 					sCertRealPath));
 			keyStore.setCertificateEntry(DEFAULT_KEYNAME, cert);
 		} catch (KeyStoreException e) {
 			log.error("Unable to setup KeyStore", e);
 			throw new RuntimeException(e);
 		} catch (NoSuchAlgorithmException e) {
 			log.error("Unable to find crypto algorithm.", e);
 			throw new RuntimeException(e);
 		} catch (CertificateException e) {
 			log.error("Unable to load certificate.", e);
 			throw new RuntimeException(e);
 		} catch (FileNotFoundException e) {
 			log.error("Unable to load certificate file: " + sCertRealPath, e);
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			log.error("General IO problem.  Unable to initialised filter.", e);
 			throw new RuntimeException(e);
 		}
 
 		return keyStore;
 
 	}
 
 	/**
 	 * Gets a WebauthValidator and initialises if necessary.
 	 * 
 	 * @return
 	 */
 	protected WebauthValidator getWebauthValidator() {
 		if (webauthValidator == null) {
 			webauthValidator = new WebauthValidator(getKeyStore());
 		}
 		return webauthValidator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.Filter#destroy()
 	 */
 	@Override
 	public void destroy() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
 	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
 	 */
 	@Override
 	public void doFilter(ServletRequest servletReq,
 			ServletResponse servletResp, FilterChain chain) throws IOException,
 			ServletException {
 
 		// Only process http requests.
 		if ((servletReq instanceof HttpServletRequest) == false) {
 			String msg = "Configuration Error.  RavenFilter can only handle Http requests. The rest of the filter chain will NOT be processed.";
 			log.error(msg);
 			return;
 		}
 
 		if (testingMode) {
 			servletReq.setAttribute(ATTR_REMOTE_USER, "test");
 			((HttpServletRequest) servletReq).getSession().setAttribute(
 					ATTR_REMOTE_USER, "test");
 			chain.doFilter(servletReq, servletResp);
 			return;
 		}
 
 		HttpServletRequest request = (HttpServletRequest) servletReq;
 		HttpServletResponse response = (HttpServletResponse) servletResp;
 		HttpSession session = request.getSession();
 
 		log.debug("RavenFilter running for: " + request.getServletPath());
 
 		// Check for an authentication reply in the request
 		// If its a POST request then we cannot read parameters because this
 		// trashes the inputstream which we want to pass to the servlet. So, if
 		// its a post request then assume that there is no WLS-RESPONSE in the
 		// request. This is reasonable because WLS-Response is sent from the
 		// raven server and it won't do that with a POST request.
 		String wlsResponse = null;
 		if (!"POST".equals(request.getMethod())) {
 			wlsResponse = request.getParameter(WLS_RESPONSE_PARAM);
 			log.debug("WLS-Response is " + wlsResponse);
 		} else {
 			log.debug("Not checking WLS-Response because we have a POST request");
 		}
 
 		// WebauthResponse storedResponse = (WebauthResponse)
 		// session.getAttribute(WLS_RESPONSE_PARAM);
 		WebauthRequest storedRavenReq = (WebauthRequest) session
 				.getAttribute(SESS_RAVEN_REQ_KEY);
 		log.debug("Stored raven request is " + storedRavenReq);
 		RavenState storedState = (RavenState) session
 				.getAttribute(SESS_STORED_STATE_KEY);
 		log.debug("Stored state is " + storedState);
 		/*
 		 * Check the stored state if we have it
 		 */
 		if (storedState != null) {
 			if (storedState.status != 200) {
 				session.setAttribute(SESS_STORED_STATE_KEY, null);
 				response.sendError(storedState.status);
 				return;
 			}
 
 			/*
 			 * We do not check for expiry of the state because in this
 			 * implementation we simply use the session expiry the web admin has
 			 * configured in Tomcat (since the Raven authentication is only used
 			 * to set up the session, it makes sense to use the session's expiry
 			 * rather than Raven's).
 			 */
 
 			/*
 			 * We do not check for state.last or state.issue being in the
 			 * future. State.issue is already checked in the WebauthValidator
 			 * when the state is initially created. State.last is set by
 			 * System.currentTimeMillis at state creation time and therefore
 			 * cannot be in the future.
 			 */
 
 			if (wlsResponse == null || wlsResponse.length() == 0) {
 				log.debug("Accepting stored session");
 				if (allowedPrincipals == null
 						|| allowedPrincipals.contains(storedState.principal
 								.getName())) {
 					chain.doFilter(request, response);
 					return;
 				} else {
 					response.sendError(403,"You are not authorized to view this page.");
 					return;
 				}
 			}
 		}// end if (storedState != null)
 
 		/*
 		 * Check the received response if we have it.
 		 * 
 		 * Note - if we have both a stored state and a WLS-Response, we let the
 		 * WLS-Response override the stored state (this is no worse than if the
 		 * same request arrived a few minutes later when the first session would
 		 * have expired, thus removing the stored state)
 		 */
 		if (wlsResponse != null && wlsResponse.length() > 0) {
 			WebauthResponse webauthResponse = new WebauthResponse(wlsResponse);
 			session.setAttribute(WLS_RESPONSE_PARAM, webauthResponse);
 			try {
 				log.debug("Validating received response with stored request");
 				if (storedRavenReq == null) {
 					response.sendError(500,
 							"Failed to find a stored Raven request in the user's session.");
 					return;
 				}
 				this.getWebauthValidator().validate(storedRavenReq,
 						webauthResponse);
 
 				RavenPrincipal principal = new RavenPrincipal(
 						webauthResponse.get("principal"));
 				RavenState state = new RavenState(200,
 						webauthResponse.get("issue"),
 						webauthResponse.get("life"), webauthResponse.get("id"),
 						principal, webauthResponse.get("auth"),
 						webauthResponse.get("sso"),
 						webauthResponse.get("params"));
 
 				log.debug("Storing new state " + state.toString());
 				session.setAttribute(SESS_STORED_STATE_KEY, state);
 				session.setAttribute(ATTR_REMOTE_USER,
 						state.principal.getName());
 				request.setAttribute(ATTR_REMOTE_USER,
 						state.principal.getName());
 
 				/*
 				 * We do a redirect here so the user doesn't see the
 				 * WLS-Response in his browser location
 				 */
 				response.sendRedirect(webauthResponse.get("url"));
 				return;
 			} catch (WebauthException e) {
 				log.debug("Response validation failed - " + e.getMessage());
 				try {
 					int status = webauthResponse.getInt("status");
 					if (status > 0)
 						response.sendError(status, e.getMessage());
 					else
 						response.sendError(500, "Response validation failed - "
 								+ e.getMessage());
 				} catch (Exception e2) {
 					response.sendError(500,
 							"Response validation failed - " + e.getMessage());
 				}
 				return;
 			}
 		} else {
 			/*
 			 * No WLS-Response, no stored state. Redirect the user to Raven to
 			 * log in
 			 */
 			WebauthRequest webauthReq = new WebauthRequest();
 
 			StringBuffer url = request.getRequestURL();
 			if (serverURLPrefix != null) {
 				// strip off everything up to and including the servlet path and
 				// replace with the prefix
 				String contextPath = request.getContextPath();
 				log.debug("Context path is: " + contextPath);
 				log.debug("Request url is: " + request.getRequestURL());
 				int index = url.indexOf(contextPath);
 				if (index == -1) {
 					log.error("Failed to find context path (" + contextPath
 							+ ") in request url " + url);
 				} else {
 					url = new StringBuffer(serverURLPrefix
 							+ url.substring(index + contextPath.length()));
 				}
 			}
 
 			if (request.getQueryString() != null
 					&& request.getQueryString().length() > 0) {
 				url.append('?');
 				url.append(request.getQueryString());
 			}
 			log.debug("Redirecting with url " + url.toString());
 			webauthReq.set("url", url.toString());
 			session.setAttribute(SESS_RAVEN_REQ_KEY, webauthReq);
 			response.sendRedirect(sRavenAuthenticatePage + "?"
 					+ webauthReq.toQString());
 			return;
 		}
 	}
 
 	class RavenPrincipal implements Principal {
 		protected String name;
 
 		public RavenPrincipal(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public String toString() {
 			return "RavenPrincipal--" + name;
 		}
 
 	}// end inner class RavenPrincipal
 
 	class RavenState {
 
 		int status;
 
 		String issue;
 
 		long last;
 
 		String life;
 
 		String id;
 
 		Principal principal;
 
 		String aauth;
 
 		String sso;
 
 		String params;
 
 		public RavenState(int status, String issue, String life, String id,
 				Principal principal, String aauth, String sso, String params) {
 			this.status = status;
 			this.issue = issue;
 			this.last = System.currentTimeMillis();
 			this.life = life;
 			this.id = id;
 			this.principal = principal;
 			this.aauth = aauth;
 			this.sso = sso;
 			this.params = params;
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			sb.append(" Status: ");
 			sb.append(status);
 			sb.append(" Issue: ");
 			sb.append(issue);
 			sb.append(" Last: ");
 			sb.append(last);
 			sb.append(" Life: ");
 			sb.append(life);
 			sb.append(" ID: ");
 			sb.append(id);
 			sb.append(" Principal: ");
 			sb.append(principal);
 			sb.append(" AAuth: ");
 			sb.append(aauth);
 			sb.append(" SSO: ");
 			sb.append(sso);
 			sb.append(" Params: ");
 			sb.append(params);
 			return sb.toString();
 		}
 	}// end inner class RavenState
 
 }// end RavenFilter class
