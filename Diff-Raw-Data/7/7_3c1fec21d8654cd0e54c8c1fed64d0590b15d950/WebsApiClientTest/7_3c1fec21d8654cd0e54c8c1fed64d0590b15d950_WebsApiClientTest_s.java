 package com.webs.api;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static junit.framework.Assert.assertTrue;
 
 import com.webs.api.model.Site;
 
 
 /**
  * @author Patrick Carroll
  */
 public class WebsApiClientTest {
 	private WebsApiClient client;
 
 	@Before
 	public void setUp() {
 		client = new WebsApiClient();
 		client.setApiPath("http://127.0.0.1:8080/com-webs-api/");
 	}
 
 
 	@Test
 	public void getSite() {
		Site site = client.getSite(31515461L);
 		System.out.println("site=" + site);
 	}
 
 	@Test
 	public void getSite_username() {
		Site site = client.getSite("podcast55");
 		System.out.println("site=" + site);
 	}
 }
