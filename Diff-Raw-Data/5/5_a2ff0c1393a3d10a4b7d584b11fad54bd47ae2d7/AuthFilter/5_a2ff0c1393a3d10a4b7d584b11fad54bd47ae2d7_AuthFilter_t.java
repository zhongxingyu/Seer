 package com.marakana.filez.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.marakana.filez.domain.Realm;
 import com.marakana.filez.domain.UsernameAndPassword;
 import com.marakana.filez.service.AuthService;
 import com.marakana.filez.service.AuthService.AuthResult;
 import com.marakana.filez.service.AuthServiceException;
 import com.marakana.filez.service.CachingAuthService;
 import com.marakana.filez.service.Params;
 
 public class AuthFilter implements Filter {
 	private static final String AUTH_SERVICE_FACTORY_TYPE_PARAM = AuthService.Factory.class
 			.getName() + ".type";
 	private static final String AUTH_SERVICE_FACTORY_CACHE_MAX_SIZE_PARAM = AuthService.Factory.class
 			.getName() + ".maxCacheSize";
 	private static final String AUTH_SERVICE_FACTORY_CACHE_TTL_IN_SECONDS_PARAM = AuthService.Factory.class
 			.getName() + ".cacheTtlInSeconds";
 	private static final String AUTH_SERVICE_FACTORY_CACHE_FORBIDDEN_PARAM = AuthService.Factory.class
 			.getName() + ".cacheForbidden";
 	private static final String USERNAME_AND_PASSWORD_PARSER_TYPE_PARAM = UsernameAndPasswordParser.Factory.class
 			.getName() + ".type";
 
 	private static final String REALM_PARSER_TYPE_PARAM = RealmParser.Factory.class
 			.getName() + ".type";
 
 	private static Logger logger = LoggerFactory.getLogger(AuthFilter.class);
 	private AuthService authService;
 	private UsernameAndPasswordParser usernameAndPasswordParser;
 	private RealmParser realmParser;
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 		try {
 			Context ctx = new InitialContext();
 			try {
 				Params params = WebUtil.asParams((Context) ctx
 						.lookup("java:comp/env/"));
 				this.authService = this.buildAuthService(params);
 				this.realmParser = this.buildRealmParser(params);
 				this.usernameAndPasswordParser = this
 						.buildUsernameAndPasswordParser(params);
 			} finally {
 				ctx.close();
 			}
 			logger.debug("Init'd");
 		} catch (Exception e) {
 			throw new ServletException("Failed to init", e);
 		}
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest httpReq = (HttpServletRequest) req;
 		HttpServletResponse httpResp = (HttpServletResponse) resp;
 
 		Realm realm = this.realmParser.getRealm(httpReq);
 		UsernameAndPassword usernameAndPassword = this.usernameAndPasswordParser
 				.getUserAndPassword(httpReq);
 		if (usernameAndPassword == null) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Requiring Authentication for realm: " + realm
 						+ " for request " + WebUtil.dump(httpReq, null)
 						+ ". Sending back "
 						+ HttpServletResponse.SC_UNAUTHORIZED);
 			}
 			httpResp.setHeader("WWW-Authenticate",
 					String.format("Basic realm=\"%s\"", realm));
 			String httpMethod = httpReq.getMethod();
 			if ("GET".equals(httpMethod) || "POST".equals(httpMethod)) {
 				httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
 						"Please authenticate");
 			} else {
 				httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
 				httpResp.setContentType("text/plain");
 				PrintWriter out = httpResp.getWriter();
 				out.println("You must authenticate in order to access this page.");
 				out.printf("See %s://%s:%d/static/faq.html for more info\n",
 						req.getScheme(), req.getServerName(),
 						req.getServerPort());
 				out.close();
 			}
 			return;
 		} else {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Authorizing " + usernameAndPassword + " for "
 						+ realm + " using " + authService.getClass().getName());
 			}
 			try {
 				long t = System.nanoTime();
 				AuthResult authResult = this.authService.auth(
 						usernameAndPassword, realm);
 				double durationInMs = (System.nanoTime() - t) / 1000000;
 				switch (authResult) {
 				case OK:
 					if (logger.isTraceEnabled()) {
 						logger.trace(String.format(
 								"%s is authorized to access %s (%.3f ms)",
 								usernameAndPassword, realm, durationInMs));
 					}
 					chain.doFilter(req, resp);
 					break;
 				case UNAUTHORIZED:
 					if (logger.isDebugEnabled()) {
						logger.debug(String
 								.format("%s from %s is not authorized to access %s (%.3f ms)",
 										usernameAndPassword,
 										httpReq.getRemoteAddr(), realm,
 										durationInMs));
 					}
 					httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 					break;
 				case FORBIDDEN:
 					if (logger.isDebugEnabled()) {
						logger.debug(String
 								.format("%s from %s is forbidden from accessing %s (%.3f ms)",
 										usernameAndPassword,
 										httpReq.getRemoteAddr(), realm,
 										durationInMs));
 					}
 					httpResp.sendError(HttpServletResponse.SC_FORBIDDEN);
 					break;
 				}
 			} catch (AuthServiceException e) {
 				if (logger.isErrorEnabled()) {
 					logger.error(
 							String.format(
 									"%s from %s is prevented from accessing %s due to an internal server error",
 									usernameAndPassword,
 									httpReq.getRemoteAddr(), realm), e);
 				}
 				httpResp.sendError(
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"The server encountered an error while trying to auth ["
 								+ usernameAndPassword.getUsername()
 								+ "]. Please try again later.");
 			}
 		}
 	}
 
 	@Override
 	public void destroy() {
 		logger.debug("Destroy'd");
 	}
 
 	private UsernameAndPasswordParser buildUsernameAndPasswordParser(
 			Params params) throws InstantiationException,
 			IllegalAccessException, ClassNotFoundException {
 		String type = params.getString(USERNAME_AND_PASSWORD_PARSER_TYPE_PARAM);
 		if (type == null) {
 			throw new AuthServiceException("Missing "
 					+ USERNAME_AND_PASSWORD_PARSER_TYPE_PARAM + " parameter");
 		}
 		return ((UsernameAndPasswordParser.Factory) Class.forName(type)
 				.newInstance()).build(params);
 	}
 
 	private RealmParser buildRealmParser(Params params)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		String type = params.getString(REALM_PARSER_TYPE_PARAM);
 		if (type == null) {
 			throw new AuthServiceException("Missing " + REALM_PARSER_TYPE_PARAM
 					+ " parameter");
 		}
 		return ((RealmParser.Factory) Class.forName(type).newInstance())
 				.build(params);
 	}
 
 	private AuthService buildAuthService(Params params)
 			throws AuthServiceException, InstantiationException,
 			IllegalAccessException, ClassNotFoundException {
 		String type = params.getString(AUTH_SERVICE_FACTORY_TYPE_PARAM);
 		if (type == null) {
 			throw new AuthServiceException("Missing "
 					+ AUTH_SERVICE_FACTORY_TYPE_PARAM + " parameter");
 		} else {
 			if (logger.isDebugEnabled()) {
 				logger.debug("Building AuthService using " + type);
 			}
 			AuthService authService = ((AuthService.Factory) Class
 					.forName(type).newInstance()).build(params);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Built AuthService of type "
 						+ authService.getClass().getName());
 			}
 			int maxCacheSize = params.getInteger(
 					AUTH_SERVICE_FACTORY_CACHE_MAX_SIZE_PARAM, 0);
 			int cacheTtlInSeconds = params.getInteger(
 					AUTH_SERVICE_FACTORY_CACHE_TTL_IN_SECONDS_PARAM, 0);
 			boolean cacheUnauths = params.getBoolean(
 					AUTH_SERVICE_FACTORY_CACHE_FORBIDDEN_PARAM, false);
 			if (maxCacheSize > 0 && cacheTtlInSeconds > 0) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Enabling caching AuthService with max-size="
 							+ maxCacheSize + " and TTL=" + cacheTtlInSeconds
 							+ " seconds");
 				}
 				authService = new CachingAuthService(authService, maxCacheSize,
 						cacheTtlInSeconds, cacheUnauths);
 			}
 			return authService;
 		}
 	}
 }
