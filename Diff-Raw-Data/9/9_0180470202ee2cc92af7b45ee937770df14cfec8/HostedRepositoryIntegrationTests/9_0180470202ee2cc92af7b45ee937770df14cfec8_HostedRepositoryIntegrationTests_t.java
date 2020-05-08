 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.apps.repository.test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
 import java.util.Properties;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 
 import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.test.framework.OsgiTestRunner;
 import org.eclipse.virgo.util.io.PathReference;
 import org.eclipse.virgo.util.io.ZipUtils;
 
 @RunWith(OsgiTestRunner.class)
 @Ignore("[DMS-2878]")
 public class HostedRepositoryIntegrationTests {
 
     private final static File INDEX_LOCATION = new File("target/work/org.eclipse.virgo.apps.repository.core_2.0.0/integration-test.index");
 
     private final static File W_INDEX_LOCATION = new File("target/work/org.eclipse.virgo.apps.repository.core_2.0.0/watched-integration-test.index");
 
     private static boolean appDeployed = false;
     
     private ApplicationDeployer deployer;
     
     @BeforeClass
     public static void awaitDeploymentOfInitialArtifacts() throws InterruptedException {
     	final CountDownLatch latch = new CountDownLatch(1);
     	
     	EventHandler eventHandler = new EventHandler() {
 			public void handleEvent(Event event) {
 				if ("org/eclipse/virgo/kernel/deployer/systemartifacts/DEPLOYED".equals(event.getTopic())) {
 					latch.countDown();
 				}				
 			}    		
     	};
     	
    	Dictionary<String, String> properties = new Hashtable<String, String>();
    	properties.put(EventConstants.EVENT_TOPIC, "org/eclipse/virgo/kernel/deployer/*");
    	FrameworkUtil.getBundle(HostedRepositoryIntegrationTests.class).getBundleContext().registerService(EventHandler.class, eventHandler, properties);
     	
     	if (!latch.await(30, TimeUnit.SECONDS)) {
     		fail("Deployment of system artifacts did not complete within 30 seconds");
     	}
     }
 
     @Before
     public void setup() throws Exception {
     	
     	BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
     	
     	ServiceReference serviceReference = bundleContext.getServiceReference(ApplicationDeployer.class.getName());
         this.deployer = (ApplicationDeployer) bundleContext.getService(serviceReference);
     	
         if (!appDeployed) {
             deployHostedRepositoryApp();
         }
     }
 
 	private void deployHostedRepositoryApp() throws DeploymentException,
 			IOException {
 		if (INDEX_LOCATION.exists()) {
 		    assertTrue(INDEX_LOCATION.delete());
 		}
 		if (W_INDEX_LOCATION.exists()) {
 		    assertTrue(W_INDEX_LOCATION.delete());
 		}		
 		
 		ApplicationDeployer.DeploymentOptions deploymentOptions = new ApplicationDeployer.DeploymentOptions(false, false, true);
 		deployer.deploy(new File("../org.eclipse.virgo.apps.repository.core/target/classes").toURI(), deploymentOptions);
 		deployer.deploy(packageWebModule().toURI(), deploymentOptions);                        
 
 		appDeployed = true;
 	}
 
     private static PathReference packageWebModule() throws IOException {
         PathReference packagedModule = new PathReference("target/org.eclipse.virgo.apps.repository.web.war");
         if (packagedModule.exists()) {
             assertTrue(packagedModule.delete());
         }
         ZipUtils.zipTo(new PathReference("../org.eclipse.virgo.apps.repository.web/target/war-expanded"), packagedModule);
         return packagedModule;
     }
 
     @Test(timeout=60000)
     public void deploymentOfBundleWithConstraintsSatisfiedFromHostedRepository() throws Exception {
         this.deployer.deploy(new File("src/test/resources/b.jar").toURI());
     }
 }
