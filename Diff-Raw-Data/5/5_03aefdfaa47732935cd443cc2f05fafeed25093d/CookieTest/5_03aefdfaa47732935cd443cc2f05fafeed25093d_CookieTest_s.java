 package http;
 
 import http.date.DateParseException;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.Test;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Karl Bennett
  */
 public class CookieTest {
 
     public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("E',' dd MMM yyyy HH':'mm':'ss z");
 
     public static final String NAME_ONE = "cookie_name_one";
     public static final String NAME_TWO = "cookie_name_two";
     public static final String VALUE = "cookie_value";
     public static final Pattern NAME_REGEX = Pattern.compile("(" + NAME_ONE + ")=" + VALUE);
     public static final Pattern VALUE_REGEX = Pattern.compile(NAME_ONE + "=(" + VALUE + ")");
     public static final Pattern COMMENT_REGEX = Pattern.compile(";\\s+Comment=(\\w+)");
     public static final String COMMENT = "cookie_comment";
     public static final Pattern DOMAIN_REGEX = Pattern.compile(";\\s+Domain=(\\w+)");
     public static final String DOMAIN = "www.cookie.com";
     public static final Pattern EXPIRES_REGEX = Pattern.compile(";\\s+Expires=([\\w\\s,:]+)");
     public static final DateTime EXPIRES_DATE = new DateTime(DateTime.now().getMillis() / 1000 * 1000);
     public static final String EXPIRES = DATE_FORMATTER.print(EXPIRES_DATE);
     public static final Pattern MAX_AGE_REGEX = Pattern.compile(";\\s+Max-Age=(\\d+)");
     public static final int MAX_AGE = (int) ((DateTime.now().plusDays(1).getMillis() - DateTime.now().getMillis()) / 1000);
     public static final Pattern PATH_REGEX = Pattern.compile(";\\s+Path=((?:https?://)?[\\w/\\._-]+)");
     public static final URI PATH;
     static {
 
         URI path = null;
 
         try {
 
             path = new URI("/cookie");
 
         } catch (URISyntaxException e) {
 
             throw new RuntimeException(e);
         }
 
         PATH = path;
     }
     public static final Pattern SECURE_REGEX = Pattern.compile("Secure=(\\w+);");
     public static final boolean SECURE = true;
     public static final Pattern VERSION_REGEX = Pattern.compile("Version=(\\d+);");
     public static final int VERSION = 1;
 
     public static final String COOKIE_STRING_ONE = NAME_ONE + "=" + VALUE + "; Domain=" + DOMAIN + "; Comment=" + COMMENT
             + "; Max-Age=" + MAX_AGE + "; Path=" + PATH + "; Expires=" + EXPIRES + "; Secure" + "; Version=" + VERSION;
 
     public static final String COOKIE_STRING_TWO = NAME_TWO + "=" + VALUE + "; Domain=" + DOMAIN + "; Max-Age=" + MAX_AGE
             + "; Comment=" + COMMENT + "; Version=" + VERSION + "; Path=" + PATH + "; Secure" + "; Expires=" + EXPIRES;
 
     @Test
     public void testCreateCookie() throws Exception {
 
         String cookieString = new Cookie(NAME_ONE, VALUE).toString();
 
         Matcher matcher = NAME_REGEX.matcher(cookieString);
         assertTrue("the name of the cookie should be in the cookie string.", matcher.find());
         matcher = VALUE_REGEX.matcher(cookieString);
         assertTrue("the value of the cookie should be in the cookie string.", matcher.find());
         matcher = COMMENT_REGEX.matcher(cookieString);
         assertFalse("the comment for the cookie should not be in the cookie string.", matcher.find());
         matcher = DOMAIN_REGEX.matcher(cookieString);
         assertFalse("the domain for the cookie should not be in the cookie string.", matcher.find());
         matcher = EXPIRES_REGEX.matcher(cookieString);
         assertFalse("the expires date for the cookie should not be in the cookie string.", matcher.find());
         matcher = MAX_AGE_REGEX.matcher(cookieString);
         assertFalse("the max-age value for the cookie should not be in the cookie string.", matcher.find());
         matcher = PATH_REGEX.matcher(cookieString);
         assertFalse("the path for the cookie should not be in the cookie string.", matcher.find());
         matcher = SECURE_REGEX.matcher(cookieString);
         assertFalse("the secure state for the cookie should not be in the cookie string.", matcher.find());
         matcher = VERSION_REGEX.matcher(cookieString);
         assertFalse("the verion of the cookie should not be in the cookie string.", matcher.find());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreateCookieWithNullName() throws Exception {
 
         new Cookie(null, VALUE);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreateCookieWithEmptyName() throws Exception {
 
         new Cookie("", VALUE);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreateCookieWithNullValue() throws Exception {
 
         new Cookie(NAME_ONE, null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreateCookieWithNullNameAndValue() throws Exception {
 
         new Cookie(null, null);
     }
 
     @Test
     public void testSetExpiresWithString() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setExpires(EXPIRES);
 
         assertEquals("the expries date should be set correctly.", EXPIRES_DATE.toDate(), cookie.getExpires());
 
         String cookieString = cookie.toString();
 
         Matcher matcher = NAME_REGEX.matcher(cookieString);
         assertTrue("the name of the cookie should be in the cookie string.", matcher.find());
         matcher = VALUE_REGEX.matcher(cookieString);
         assertTrue("the value of the cookie should be in the cookie string.", matcher.find());
         matcher = COMMENT_REGEX.matcher(cookieString);
         assertFalse("the comment for the cookie should not be in the cookie string.", matcher.find());
         matcher = DOMAIN_REGEX.matcher(cookieString);
         assertFalse("the domain for the cookie should not be in the cookie string.", matcher.find());
         matcher = EXPIRES_REGEX.matcher(cookieString);
         assertTrue("the expires date for the cookie should be in the cookie string.", matcher.find());
         assertEquals("the expires date for the cookie should be correct.", EXPIRES, matcher.group(1));
         matcher = MAX_AGE_REGEX.matcher(cookieString);
         assertFalse("the max-age value for the cookie should not be in the cookie string.", matcher.find());
         matcher = PATH_REGEX.matcher(cookieString);
         assertFalse("the path for the cookie should not be in the cookie string.", matcher.find());
         matcher = SECURE_REGEX.matcher(cookieString);
         assertFalse("the secure state for the cookie should not be in the cookie string.", matcher.find());
         matcher = VERSION_REGEX.matcher(cookieString);
         assertFalse("the verion of the cookie should not be in the cookie string.", matcher.find());
     }
 
     @Test(expected = DateParseException.class)
     public void testSetExpiresWithMalformedString() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setExpires("malformed date");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testSetExpiresWithNullString() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setExpires((String) null);
     }
 
     @Test
     public void testHasExpired() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setExpires(DateTime.now().minusMinutes(1).toDate());
 
         assertTrue("the cookie should have expired.", cookie.hasExpired());
     }
 
     @Test
     public void testHasExpiredWithMaxAge() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setMaxAge(0);
 
         assertTrue("the cookie should have expired.", cookie.hasExpired());
     }
 
     @Test
     public void testHasExpiredWithFutureDate() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setExpires(DateTime.now().plusDays(1).toDate());
 
         assertFalse("the cookie should not have expired.", cookie.hasExpired());
     }
 
     @Test
     public void testHasExpiredWithFutureMaxAge() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setMaxAge((int) (DateTime.now().plusDays(1).getMillis() / 1000));
 
         assertFalse("the cookie should not have expired.", cookie.hasExpired());
     }
 
     @Test
     public void testSetPath() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setPath("");
 
         assertEquals("the path URI should be set correctly.", "", cookie.getPath().toString());
 
         cookie.setPath(PATH.toString());
 
         assertEquals("the path URI should be set correctly.", PATH, cookie.getPath());
 
         String cookieString = cookie.toString();
 
         Matcher matcher = NAME_REGEX.matcher(cookieString);
         assertTrue("the name of the cookie should be in the cookie string.", matcher.find());
         matcher = VALUE_REGEX.matcher(cookieString);
         assertTrue("the value of the cookie should be in the cookie string.", matcher.find());
         matcher = COMMENT_REGEX.matcher(cookieString);
         assertFalse("the comment for the cookie should not be in the cookie string.", matcher.find());
         matcher = DOMAIN_REGEX.matcher(cookieString);
         assertFalse("the domain for the cookie should not be in the cookie string.", matcher.find());
         matcher = EXPIRES_REGEX.matcher(cookieString);
         assertFalse("the expires date for the cookie should not be in the cookie string.", matcher.find());
         matcher = MAX_AGE_REGEX.matcher(cookieString);
         assertFalse("the max-age value for the cookie should not be in the cookie string.", matcher.find());
         matcher = PATH_REGEX.matcher(cookieString);
         assertTrue("the path for the cookie should be in the cookie string.", matcher.find());
         assertEquals("the path for the cookie should be correct.", PATH.toString(), matcher.group(1));
         matcher = SECURE_REGEX.matcher(cookieString);
         assertFalse("the secure state for the cookie should not be in the cookie string.", matcher.find());
         matcher = VERSION_REGEX.matcher(cookieString);
         assertFalse("the verion of the cookie should not be in the cookie string.", matcher.find());
     }
 
     @Test
     public void testSetNullPath() throws Exception {
 
         Cookie cookie = new Cookie(NAME_ONE, VALUE);
         cookie.setPath((String) null);
 
         assertNull("the path URI should be set to null correctly.", cookie.getPath());
     }
 
     @Test
     public void testGetName() throws Exception {
 
         assertEquals("the name should be set correctly.", NAME_ONE, new Cookie(NAME_ONE, VALUE).getName());
     }
 
     @Test
     public void testGetValue() throws Exception {
 
         assertEquals("the name should be set correctly.", VALUE, new Cookie(NAME_ONE, VALUE).getValue());
     }
 
     @Test
     public void testParse() throws Exception {
 
         Cookie cookieOne = new Cookie(NAME_ONE, VALUE);
         cookieOne.setComment(COMMENT);
         cookieOne.setDomain(DOMAIN);
         cookieOne.setExpires(EXPIRES);
         cookieOne.setMaxAge(MAX_AGE);
         cookieOne.setPath(PATH);
         cookieOne.setSecure(SECURE);
         cookieOne.setVersion(VERSION);
 
         Cookie cookieTwo = new Cookie(NAME_TWO, VALUE);
         cookieTwo.setComment(COMMENT);
         cookieTwo.setDomain(DOMAIN);
         cookieTwo.setExpires(EXPIRES);
         cookieTwo.setMaxAge(MAX_AGE);
         cookieTwo.setPath(PATH);
         cookieTwo.setSecure(SECURE);
         cookieTwo.setVersion(VERSION);
 
         Collection<Cookie> parsedCookies = Cookie.parse(COOKIE_STRING_ONE + "; " + COOKIE_STRING_TWO);
 
         assertNotNull("a collection of cookies should be produced.", parsedCookies);
         assertEquals("two cookies should be parsed.", 2, parsedCookies.size());
 
        Iterator<Cookie> iterator = parsedCookies.iterator();
        assertEquals("the parsed cookieOne should contain the correct values.", cookieOne, iterator.next());
        assertEquals("the parsed cookieTwo should contain the correct values.", cookieTwo, iterator.next());
     }
 }
