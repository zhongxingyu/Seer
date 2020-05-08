 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.springframework.beans.BeansException;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import org.mule.module.facebook.oauth.FacebookConnectorOAuthState;
 
 import com.restfb.types.User;
 
 public class GetUserTestCases extends FacebookTestParent {
 	
     @Before
     public  void setUp() throws ObjectStoreException {
 
 //    	ObjectStore objectStore = muleContext.getRegistry().lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
 //    	objectStore.store("accessTokenId", (FacebookConnectorOAuthState) context.getBean("connectorOAuthState"));
 //
 //    	System.out.println(objectStore.contains("accessTokenId"));
 // 	
     }
 	
     @Category({RegressionTests.class})
 	@Test
 	public void testGetUser() {
     	
     	testObjects = (HashMap<String,Object>) context.getBean("getUserTestData");
     	
		MessageProcessor flow = lookupMessageProcessor("get-user");
     	
 		try {
 
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			User user = (User) response.getMessage().getPayload();
 
 			assertTrue(user.getUsername().equals(testObjects.get("username").toString()));
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			fail();
 		}
      
 	}
     
 }
