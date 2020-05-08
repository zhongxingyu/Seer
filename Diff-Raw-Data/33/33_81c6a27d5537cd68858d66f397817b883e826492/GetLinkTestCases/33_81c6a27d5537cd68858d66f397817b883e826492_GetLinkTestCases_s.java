 package org.mule.module.facebook.automation.testcases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 
 import com.restfb.types.Link;
 
 public class GetLinkTestCases extends FacebookTestParent {
 
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp(){
 		try {
 			testObjects = (HashMap<String, Object>) context.getBean("getLinkTestData");
 			String profileId = getProfileId();
 			testObjects.put("profileId", profileId);
 
 			String msg = testObjects.get("msg").toString();
 			String link = testObjects.get("link").toString();
 			
 			String messageId = publishLink(profileId, msg, link);
 			testObjects.put("messageId", messageId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testGetLink(){
 		try {
			String messageId = (String) testObjects.get("messageId");
 			
 			MessageProcessor flow = lookupFlowConstruct("get-link");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
			Link link = (Link) response.getMessage().getPayload();
     
			assertEquals(messageId, link.getId());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			String profileId = (String) testObjects.get("profileId");
 			String messageId = (String) testObjects.get("messageId");
 			deleteObject(profileId + "_" + messageId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 }
