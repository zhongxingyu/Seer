 package com.angelini.fly;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AuthServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private static final String LOGIN_PAGE = "/layout/login.html";
	private static final String LOGOUT_PAGE = "/layout/logout.html";
 	
 	public static final String LOGIN_URL = "/auth/login";
 	
 	private String loginPage;
	private String logoutPage;
 	private Authentication authentication;
 	private AuthenticationCheck authCheck;
 	
 	private static Logger log = LoggerFactory.getLogger(AuthServlet.class);
 	
 	public AuthServlet(FlyDB db, Authentication authentication, Class<?> authCheckClass) throws IOException {
 		try {
 			authCheck = (AuthenticationCheck) authCheckClass.newInstance();
 			authCheck.init(db);
 		} catch (Exception e) {
 			throw new RuntimeException("Cannot instantiate AuthenticationCheck class", e);
 		}
 		
 		this.authentication = authentication;
 		loginPage = Utils.readFile(LOGIN_PAGE);
		logoutPage = Utils.readFile(LOGOUT_PAGE);
 	}
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		if (req.getPathInfo().equals("/login")) {
 			resp.setStatus(200);
 			resp.setContentType("text/html");
 			resp.getWriter().print(loginPage);
 			return;
 		}
 		
 		if (req.getPathInfo().equals("/logout")) {
 			Cookie auth = new Cookie(Constants.AUTH_COOKIE, "");
 			Cookie authSigned = new Cookie(Constants.AUTH_SIG_COOKIE, "");
 			
 			auth.setMaxAge(0);
 			auth.setPath("/");
 			auth.setHttpOnly(true);
 			
 			authSigned.setMaxAge(0);
 			authSigned.setPath("/");
 			authSigned.setHttpOnly(true);
 			
 			resp.addCookie(auth);
 			resp.addCookie(authSigned);
 			
 			resp.setStatus(200);
 			resp.setContentType("text/html");
			resp.getWriter().print(logoutPage);
 			return;
 		}
 		
 		resp.setStatus(404);
 		return;
 	}
 	
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		if (!req.getPathInfo().equals("/login")) {
 			resp.setStatus(404);
 			return;
 		}
 		
 		try {
 			Map<String, String> params = Utils.readBody(req.getReader());
 			String identifier = authCheck.check(params.get("username"), params.get("password")); 
 			if (identifier != null) {
 				String signed = authentication.signValue(identifier);
 				
 				Cookie auth = new Cookie(Constants.AUTH_COOKIE, identifier);
 				Cookie authSigned = new Cookie(Constants.AUTH_SIG_COOKIE, signed);
 				
 				auth.setPath("/");
 				auth.setHttpOnly(true);
 				authSigned.setPath("/");
 				authSigned.setHttpOnly(true);
 				
 				resp.addCookie(auth);
 				resp.addCookie(authSigned);
 				
 				resp.sendRedirect("/");
 				
 			} else {
 				resp.sendRedirect(LOGIN_URL + "?error=" + URLEncoder.encode("Username & Password combination invalid", "UTF8"));
 			}
 			
 		} catch (Exception e) {
 			log.error("Error reading post body", e);
 			resp.setStatus(500);
 		}
 	}
 
 }
