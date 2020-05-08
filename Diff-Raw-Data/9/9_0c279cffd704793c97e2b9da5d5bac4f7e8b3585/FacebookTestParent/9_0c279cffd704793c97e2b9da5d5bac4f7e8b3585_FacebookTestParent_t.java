 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.mule.module.facebook.oauth.FacebookConnectorOAuthState;
 import org.mule.tck.junit4.FunctionalTestCase;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 
 
 public class FacebookTestParent extends FunctionalTestCase {
 	
 	protected static final String[] SPRING_CONFIG_FILES = new String[] {"AutomationSpringBeans.xml"};
 	protected static ApplicationContext context;
 	protected Map<String,Object> testObjects;
 
 	
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 	
    protected MessageProcessor lookupMessageProcessor(String name) {
         return (MessageProcessor) muleContext.getRegistry().lookupFlowConstruct(name);
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
     
 }
