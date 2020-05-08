 package captcha;
 
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import password.Password;
 
 public class LoginValidator {
 	
 	private static HashMap<String, Integer> LoginTries = new HashMap<String, Integer>();
 	private static HashMap<String, Integer> AdminLoginTries = new HashMap<String, Integer>();
 
 	public static boolean isValidLogin(String password, String hash, String salt, String captcha, HttpServletRequest request, HttpSession session, boolean asAdmin) {
 		String ipAddress = request.getRemoteAddr();
 		String pwhash = Password.hashWithSalt(password, salt);				
 
 		boolean isPasswordValid = pwhash.equals(hash);
 		
 		boolean isCaptchaValid = false;
 		
 		HashMap<String, Integer> logins = asAdmin ? AdminLoginTries : LoginTries;
 
 		
 		triedToLogin(request, asAdmin);
 		
 		Integer loginTries = logins.get(ipAddress);
 		
		if (loginTries <= 3) {
 			isCaptchaValid = true;
			
			if (isPasswordValid) {
				logins.remove(ipAddress);
			}
 		} else {
 			String MD5_captcha = (String) session.getAttribute("captcha");
 			String code = (String) request.getParameter("code");
 			String MD5_code = CaptchaServlet.getMD5Hash(code);
 			
 			if (MD5_captcha != null && code != null
 					&& MD5_captcha.equals(MD5_code)) {
 				logins.remove(ipAddress);
 				isCaptchaValid = true;
 			}
 		}
 		
 		return isPasswordValid && isCaptchaValid;
 	}
 	
 	public static boolean isValidLogin(String password, String hash, String salt, String captcha, HttpServletRequest request, HttpSession session) {
 		return isValidLogin(password, hash, salt, captcha, request, session, false);
 	}
 	
 	public static void triedToLogin(HttpServletRequest request) {
 		triedToLogin(request, false);
 	}
 	
 	public static void triedToLogin(HttpServletRequest request, boolean asAdmin) {
 		String ipAddress = request.getRemoteAddr();
 		
 		HashMap<String, Integer> logins = asAdmin ? AdminLoginTries : LoginTries;
 		
 		Integer loginTries = logins.get(ipAddress);
 		if (loginTries == null) {
 			logins.put(ipAddress, new Integer(1));
 		} else {
 			logins.put(ipAddress, loginTries+1);
 		}
 	}
 	
 	public static boolean shouldShowCaptcha(HttpServletRequest request, boolean asAdmin) {
 		boolean shouldShowCaptcha = false;
 		String ipAddress = request.getRemoteAddr();
 		
 		HashMap<String, Integer> logins = asAdmin ? AdminLoginTries : LoginTries;
 		
 		Integer loginTries = logins.get(ipAddress);
 		if (loginTries != null && loginTries > 2) {
 			shouldShowCaptcha = true;
 		}
 		
 		
 		return shouldShowCaptcha;
 	}
 	
 	public static boolean shouldShowCaptcha(HttpServletRequest request) {
 		return shouldShowCaptcha(request, false);
 	}
 }
