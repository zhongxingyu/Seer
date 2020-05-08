 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.mule.module.facebook.oauth.FacebookConnectorOAuthState;
 import org.mule.modules.tests.TestParent;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.restfb.types.Album;
 import com.restfb.types.User;
 
 public class FacebookTestParent extends TestParent {
 
 	protected static final String[] SPRING_CONFIG_FILES = new String[] { "AutomationSpringBeans.xml" };
 	protected static ApplicationContext context;
 	protected Map<String, Object> testObjects;
 
 	// Set global timeout of tests to 10minutes
 	@Rule
 	public Timeout globalTimeout = new Timeout(600000);
 
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 	
     @BeforeClass
     public static void beforeClass() {
     	context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
     }
     
    @Before
     public void init() throws ObjectStoreException {
     	ObjectStore objectStore = muleContext.getRegistry().lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
     	objectStore.store("accessTokenId", (FacebookConnectorOAuthState) context.getBean("connectorOAuthState"));
     }
     
 	protected Album requestAlbum(String albumId) throws Exception {
 		testObjects.put("album", albumId);
 
 		MessageProcessor flow = lookupFlowConstruct("get-album");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Album) response.getMessage().getPayload();
     }
     
     protected String publishAlbum(String albumName, String msg, String profileId) throws Exception {
     	testObjects.put("albumName", albumName);
     	testObjects.put("msg", msg);
     	testObjects.put("profileId", profileId);
     	
   		MessageProcessor flow = lookupFlowConstruct("publish-album");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (String) response.getMessage().getPayload();
     }
     
	@SuppressWarnings("unchecked")
	public String publishMessage(String ProfileID, String Msg) throws Exception {
 		testObjects.put("profileId", ProfileID);
 		testObjects.put("msg", Msg);
 
 		MessageProcessor flow = lookupFlowConstruct("publish-message");
 
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		String objectID = (String) response.getMessage().getPayload();
 
 		return objectID;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<Album> requestUserAlbums(String user, String since, String until, String limit, String offset) throws Exception {
 		testObjects.put("user", user);
 		testObjects.put("since", since);
 		testObjects.put("until", until);
 		testObjects.put("limit", limit);
 		testObjects.put("offset", offset);
 
 		MessageProcessor flow = lookupFlowConstruct("get-user-albums");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Collection<Album>) response.getMessage().getPayload();
     }
 	
	public Album getAlbum(String albumId) throws Exception {
 		testObjects.put("album", albumId);
 		
 		MessageProcessor flow = lookupFlowConstruct("get-album");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Album) response.getMessage().getPayload();
 	}
     
    public User getLoggedUserDetails() throws Exception {
     	MessageProcessor flow = lookupFlowConstruct("logged-user-details");
     	MuleEvent response = flow.process(getTestEvent(testObjects));
     	return (User) response.getMessage().getPayload();
     }
     
    public String getProfileId() throws Exception {
     	return getLoggedUserDetails().getId();
     }
     
    public String publishPhoto(String albumId, String caption, File photo) throws Exception {
     	testObjects.put("albumId", albumId);
     	testObjects.put("caption", caption);
     	testObjects.put("photoRef", photo);
     	
     	MessageProcessor flow = lookupFlowConstruct("publish-photo");
     	MuleEvent response = flow.process(getTestEvent(testObjects));
     	return (String) response.getMessage().getPayload();
     }
     
    public void deleteObject(String objectId) throws Exception {
     	testObjects.put("objectId", objectId);
     	
     	MessageProcessor flow = lookupFlowConstruct("delete-object");
     	flow.process(getTestEvent(testObjects));
     }
 
 }
