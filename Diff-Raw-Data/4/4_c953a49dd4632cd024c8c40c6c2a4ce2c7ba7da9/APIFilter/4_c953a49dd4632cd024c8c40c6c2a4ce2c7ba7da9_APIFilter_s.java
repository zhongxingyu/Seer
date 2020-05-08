 package uk.ac.cam.cl.dtg.teaching;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.UriBuilder;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi;
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.ApiPermissions;
 
 public class APIFilter implements Filter {
 	private static Logger log = LoggerFactory.getLogger(APIFilter.class);
 	private Session sess;
 	
 	/**
 	 * Name of the request attribute populated with the current user.
 	 */
 	public static final String USER_ATTR = "userId";
 	
 	/**
 	 * Base URL used to access dashboard API.
 	 */
 	private String dashboardUrl;
 
 	/**
 	 * A list of comma separated URLs to allow through
 	 */
 	private String[] excludePrefixes;
 
 	/**
 	 * API key with global permissions for checking other API keys.
 	 */
 	private String apiKey;
 	
 	/**
 	 * Whether the service supports global API keys (defaults to true).
 	 */
 	private boolean allowGlobal = true;
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 		// Load dashboard API URL from servlet context.
 		dashboardUrl = config.getServletContext().getInitParameter("dashboardUrl");
 		
 		if(dashboardUrl == null) {
 			log.error("Missing dashboard URL from context parameters.");
 		}
 		
 		// Load dashboard API URLs to exclude from filter from servlet context.
 		// Trims blank space
 		String prefixes = config.getServletContext().getInitParameter("excludePrefixes");
 		if(prefixes != null) {
 			excludePrefixes = prefixes.split(",");
 			
 			for (int i = 0; i < excludePrefixes.length; i++) {
 				excludePrefixes[i] = excludePrefixes[i].trim();
 			}
 		}
 		
 		// Load global API key from servlet context for accessing dashboard.
 		apiKey = config.getServletContext().getInitParameter("apiKey");
 		
 		if(apiKey == null) {
 			log.error("Missing API key from context parameters.");
 		}
 		
 		// Determine whether service supports global API key.
 		String sAllowGlobal = config.getInitParameter("allowGlobal");
 		
 		if(sAllowGlobal != null) {
 			if(sAllowGlobal.toLowerCase().equals("false")) {
 				allowGlobal = false;
 			} else if(sAllowGlobal.toLowerCase().equals("true")) {
 				allowGlobal = true;
 			} else {
 				log.warn("allowGlobal init-param should either be 'true' or 'false'.");
 			}
 		}
 		
 		// Initialise the session
 		Configuration configuration = new Configuration();
 		configuration.configure("/hibernateRequestLog.cfg.xml");
 		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
         								  .applySettings(configuration.getProperties())
         								  .buildServiceRegistry();
 		SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
 		sess = sf.openSession();
 		
 		log.info("API filter initialised.");
 	}
 
 	@Override
 	public void doFilter(ServletRequest servletReq, ServletResponse servletResp,
 			FilterChain chain) throws IOException, ServletException {
 		
 		// Convert arguments to HTTP ones to allow grabbing session etc.
 		HttpServletRequest request = (HttpServletRequest) servletReq;
 		HttpServletResponse response = (HttpServletResponse) servletResp;
 		HttpSession session = request.getSession();
 		
 		// Check whether the URL should be excluded from the filter
 		// If so, chain through
 		if(excludePrefixes != null) {
 			for (String p:excludePrefixes) {
 				if(request.getRequestURI().startsWith(request.getContextPath() + p)) {
 					log.info("Chaining request through API filter with prefix: " + request.getRequestURI());
 					chain.doFilter(request, response);
 					return;
 				}
 			}
 		}
 			
 		// API key provided.
 		if(request.getParameter("key") != null) {
 			String key = (String) request.getParameter("key");
 			
 			ClientRequestFactory crf = new ClientRequestFactory(UriBuilder.fromUri(dashboardUrl).build());
 			DashboardApi dApi = crf.createProxy(DashboardApi.class);
 			ApiPermissions permissions;
 			
 			try {
 				permissions = dApi.getApiPermissions(key);
 			} catch(Exception e) {
 				log.error("Error checking key: " + e.getMessage());
 				response.sendError(500, "Error checking key");
 				return;
 			}
 			
 			// Empty response
 			if(permissions == null) {
 				log.error("Error checking key: empty response");
 				response.sendError(500, "Error checking key");
 			// Key was invalid
 		    } else if(permissions.getError() != null) {
 				log.error("Request with invalid API key = " + key);
 				response.sendError(401, "Invalid API key.");
 			// Global key
 			} else if(permissions.getType().equals("global")) {
 				// Global supported, allow request with null user.
 				if(allowGlobal) {
 					log.debug("API request permitted for global key.");
 					logRequest("GLOBAL", request.getRequestURL().toString());
 					request.setAttribute(USER_ATTR, null);
 					chain.doFilter(request, response);
 				// Global unsupported, return 405 (unsupported method).
 				} else {
 					log.error("Request with global API key when allowGlobal=false.");
 					response.sendError(405, "Global API keys unsupported.");
 				}
 			// User-specific key.
 			} else if(permissions.getType().equals("user")) {
 				String userId = permissions.getUserId();
 				
 				log.debug("API request permitted with key for " + userId);
 				logRequest(userId, request.getRequestURL().toString());
 				request.setAttribute(USER_ATTR, userId);
 				chain.doFilter(request, response);
 			}
 		// Check whether we're logged in with Raven.
 		} else if(session.getAttribute("RavenRemoteUser") != null) {
 			String crsid = (String) session.getAttribute("RavenRemoteUser");
 			
 			log.debug("API request permitted for user " + crsid);
 			logRequest(crsid, request.getRequestURL().toString());
 			request.setAttribute(USER_ATTR, crsid);
 			chain.doFilter(request, response);
 		// No other authorisation options.
 		} else {
 			response.sendError(401, "Unauthorised API request.");
 		}
 	}
 
 	@Override
 	public void destroy() {
		// Nothing to do.
 	}
 	
 	private void logRequest(String crsid, String url) {
 		sess.beginTransaction();
 		
 		RequestLog rl = new RequestLog(crsid, url);
 		
 		sess.save(rl);
 		sess.getTransaction().commit();
		sess.close();
 	}
 }
