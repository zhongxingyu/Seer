 /*
  * Copyright (c) jmelzer 2011.
  * All rights reserved.
  */
 
 package com.jmelzer.webapp.page;
 
 import com.jmelzer.webapp.WicketApplication;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 /** Simple test using the WicketTester */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring-web.xml", "classpath:spring.xml", "classpath:security.xml"})
 @TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
 @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
 @ActiveProfiles(profiles = "test")
 @Transactional
 public abstract class AbstractPageIntegrationTest {
 
     public WicketTester tester;
     @Autowired
     private ApplicationContext ctx;
 
     @Autowired
     private WicketApplication myWebApplication;
 
     @Before
     public void onSetUp() throws Exception {
         if (tester == null) {
             tester = new WicketTester(myWebApplication);
             myWebApplication.setApplicationContext(ctx);
         }
     }
 
 
 }
