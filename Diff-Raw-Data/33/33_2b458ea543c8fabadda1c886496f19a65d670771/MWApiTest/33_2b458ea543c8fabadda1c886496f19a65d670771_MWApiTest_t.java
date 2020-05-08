 package org.mediawiki.api;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.AbstractMap;
 import java.util.List;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.simple.parser.ParseException;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public class MWApiTest {
 
     // Test accounts on testwiki. I know, not ideal.
     private final String USERNAME = "javaMWApiTest";
     private final String PASSWORD = "javamwapi";
    private final String APIURL = "http://test.wikipedia.org/w/api.php";
     
     private MWApi api;
     @Before
     public void setUp() {
 		api = new MWApi(APIURL, new DefaultHttpClient());
     }
     
 	@Test
 	public void testSiteMatrix() throws IOException {
 		ApiResult result =  api.action("sitematrix").param("smlimit", 10).get();
 		assertTrue(result.getNode("//sitematrix") != null);
 		assertEquals(10, result.getNodes("//sitematrix/language").size());
 	}
 	
 	@Test
 	public void testLogin() throws IOException {
 		assertEquals("Success", api.login(USERNAME, PASSWORD));
 	}
 	
 	@Test
 	public void testLogout() throws IOException {
 	    // Login, check token, logout, check token
 		assertEquals("Success", api.login(USERNAME, PASSWORD));
 		assertFalse("+\\".equals(api.getEditToken())); 
 		api.logout();
 		assertEquals("+\\", api.getEditToken()); 
 	}
 
 	@Test
	public void testLoggedInEditAttempt() throws IOException {
	   assertEquals("Success", api.login(USERNAME, PASSWORD));
 	   String token = api.getEditToken();
 	   String text = "Has anyone really been far even as decided to use even go want to do look more like?";
	   String title = "India";
 	   ApiResult editResult = api.action("edit").param("title", title).param("text", text).param("token", token).param("summary", "Sample summary").post();
	   // FIXME: Possibly horrible hack
	   if( editResult.getNode("/api/edit/captcha") != null ) {
	       // Well, we've ourselves a captcha!
	       // I don't see how this could be fixed.
	       // The actual solution is, of course, to run the tests on a local mediawiki instance
	       // Unitl then, let's just ignore if we hit a captcha, okay?
	   } else {
	       // No Captcha!
            assertEquals("Success", editResult.getString("/api/edit/@result"));
            ApiResult checkResult = api.action("parse").param("page", title).param("prop", "wikitext").get();
            assertEquals(text, checkResult.getString("/api/parse/wikitext"));
	   }
 	}
 	
 	@Test
 	public void testAnonymousEditToken() throws IOException {
 	    // +\ is anonymous edit token
 	   assertEquals("+\\", api.getEditToken()); 
 	}
 
 }
