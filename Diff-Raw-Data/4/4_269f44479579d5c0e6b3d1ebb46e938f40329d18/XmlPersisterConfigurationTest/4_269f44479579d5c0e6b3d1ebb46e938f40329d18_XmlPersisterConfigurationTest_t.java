 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics.aop.config.v11;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerFactory;
 import org.quartz.impl.StdSchedulerFactory;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.jmx.support.JmxUtils;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.unitils.UnitilsJUnit4TestClassRunner;
 import org.unitils.spring.annotation.SpringApplicationContext;
 import org.unitils.spring.annotation.SpringBeanByName;
 
 import com.mtgi.analytics.BehaviorTrackingManagerImpl;
 import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
 
 @SpringApplicationContext("com/mtgi/analytics/aop/config/v11/XmlPersisterConfigurationTest-applicationContext.xml")
 @RunWith(UnitilsJUnit4TestClassRunner.class)
 public class XmlPersisterConfigurationTest {
 
 	@SpringBeanByName
 	private ThreadPoolTaskExecutor testExecutor;
 
 	@SpringBeanByName
 	private BehaviorTrackingManagerImpl xmlTracking;
 
 	@SpringBeanByName
 	private Scheduler testScheduler;
 
 	@SpringApplicationContext
 	private ConfigurableApplicationContext spring;
 	
 	@After
 	public void cleanup() {
 		spring.close();
 	}
 	
 	@Test
 	public void testXmlPersisterConfiguration() throws Exception {
 		assertNotNull("custom tracking manager configured", xmlTracking);
 		assertEquals("application name set", "testApp", xmlTracking.getApplication());
 		assertEquals("correct persister type provided", XmlBehaviorEventPersisterImpl.class, xmlTracking.getPersister().getClass());
 
 		XmlBehaviorEventPersisterImpl persister = (XmlBehaviorEventPersisterImpl)xmlTracking.getPersister();
 		assertFalse("default setting overridden", persister.isBinary());
 		assertFalse("default setting overridden", persister.isCompress());
 		
 		File location = new File(persister.getFile());
 		assertTrue("custom file name [" + persister.getFile() + "]", location.getName().startsWith("xml-tracking"));
 		assertEquals("custom XML attributes can be post-processed by property resolver", 
 					 new File(System.getProperty("java.io.tmpdir")).getCanonicalPath(), 
 					 location.getParentFile().getCanonicalPath());
 		
 		TaskExecutor executor = xmlTracking.getExecutor();
 		assertSame("application executor is used", testExecutor, executor);
 
 		List<String> triggers = Arrays.asList(testScheduler.getTriggerNames("BehaviorTracking"));
 		assertEquals("flush and rotate jobs scheduled in application scheduler", 2, triggers.size());
 		assertTrue("flush job scheduled", triggers.contains("xmlTracking_flush_trigger"));
 		assertTrue("rotate job scheduled", triggers.contains("org.springframework.scheduling.quartz.CronTriggerBean_rotate_trigger"));
 
 		SchedulerFactory factory = new StdSchedulerFactory();
 		assertEquals("private scheduler was not created", 1, factory.getAllSchedulers().size());
 		assertSame(testScheduler, factory.getAllSchedulers().iterator().next());
 		
 		//verify that MBeans have been registered
         MBeanServer server = JmxUtils.locateMBeanServer();
        assertNotNull("manager mbean found", server.getMBeanInfo(new ObjectName("testApp:package=com.mtgi.analytics,group=xmlTracking,name=BeetManager")));
        ObjectName logName = new ObjectName("testApp:package=com.mtgi.analytics,group=xmlTracking,name=BeetLog");
         assertNotNull("log mbean found", server.getMBeanInfo(logName));
         
         assertEquals(persister.getFileSize(), server.getAttribute(logName, "FileSize"));
 	}
 	
 }
