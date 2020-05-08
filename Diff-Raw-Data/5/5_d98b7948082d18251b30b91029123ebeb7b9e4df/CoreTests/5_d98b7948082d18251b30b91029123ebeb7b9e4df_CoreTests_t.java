 package com.burritopos.server.rest.test.webresource;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 import com.burritopos.server.rest.test.webresource.activiti.ProcessDefinitionTest;
 import com.burritopos.server.rest.test.webresource.activiti.ProcessInstanceTest;
import com.burritopos.server.rest.test.webresource.activiti.ProcessTaskTest;
 
 
 @RunWith(Suite.class)
 @SuiteClasses({ 
 	ServerServiceTest.class,
 	ProcessDefinitionTest.class,
	ProcessInstanceTest.class,
	ProcessTaskTest.class
 	})
     public class CoreTests {
 
     @BeforeClass 
     public static void setUpClass() {      
 
     }
 
     @AfterClass 
     public static void tearDownClass() { 
 
     }
 
 }
