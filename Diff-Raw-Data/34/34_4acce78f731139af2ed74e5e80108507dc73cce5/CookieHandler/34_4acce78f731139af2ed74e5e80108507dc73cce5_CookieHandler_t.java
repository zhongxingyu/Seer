 package server.operations;
 
 import java.util.Date;
 
 import javax.ws.rs.core.NewCookie;
 
 import server.queries.CookieQuery;
 
 /**
  * Used to creates and validated session cookies for the client.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class CookieHandler {
 
 	public final NewCookie createCookie() {
 
		final String cookieID = new PasswordEncryptor().generateEncryptedPassword();
 
		final NewCookie cookie = new NewCookie("NSA-Cookie", cookieID);
 		final DateHelper dateHelper = new DateHelper();
 		dateHelper.addTime(0, 1, 0, 0, 0, 0);
		new CookieQuery().createCookie(cookieID, dateHelper.parseStringToDate(dateHelper.getFullDate()));
 
 		return cookie;
 	}
 
	final boolean validateCookie(final NewCookie cookie) {
 
 		String cookieValue = "";
 
 		if ("NSA-Cookie".equals(cookie.getName())) {
 			cookieValue = cookie.getValue();
 		}
 
 		if (new CookieQuery().getCookie(cookieValue) != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public final void deleteInvalidCookies() {
 		new CookieQuery().removeInvalidCookies(new Date());
 	}
 
	final boolean deleteCookie(final NewCookie cookie) {
 		return new CookieQuery().removeCookie(cookie.getValue());
 	}
 }
