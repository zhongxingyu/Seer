 package server.operations;
 
 import java.util.Date;
 
import javax.ws.rs.core.NewCookie;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.queries.CookieQuery;
 
 /**
  * Test for cookie creation and validation.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class CookieHandlerTest extends TestCase {
 
 	private CookieHandler handler;
 
 	@Override
 	@Before
 	public void setUp() {
 		this.handler = new CookieHandler();
 	}
 
 	@Test
 	public void testCookieCreation() {
		final NewCookie cookie = this.handler.createCookie();
 		CookieHandlerTest.assertTrue(this.handler.validateCookie(cookie));
 		CookieHandlerTest.assertTrue(this.handler.deleteCookie(cookie));
 	}
 
 	@Test
 	public void testInvalidCookie() {
 
 		final CookieQuery query = new CookieQuery();
 
 		final String cookieID = new PasswordEncryptor().generateEncryptedPassword();
 		final DateHelper dateHelper = new DateHelper();
 		dateHelper.addTime(0, 0, 0, 0, -10, 0);
 		query.createCookie(cookieID, dateHelper.parseStringToDate(dateHelper.getFullDate()));
 		CookieHandlerTest.assertNotNull(query.getCookie(cookieID));
 
 		query.removeInvalidCookies(new Date());
 		CookieHandlerTest.assertNull(query.getCookie(cookieID));
 
 	}
 }
