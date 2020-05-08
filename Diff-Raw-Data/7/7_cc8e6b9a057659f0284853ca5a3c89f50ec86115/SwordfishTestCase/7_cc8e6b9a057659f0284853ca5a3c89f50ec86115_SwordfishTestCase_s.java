 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.test.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swordfish.internal.core.test.util.Activator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import junit.framework.TestCase;
 
 /**
  * A test case class for integration testing that will start up required Swordfish bundles
  * upon creation and clean up service registrations after each test.
  */
 public class SwordfishTestCase extends TestCase {
     protected static final Logger LOG = LoggerFactory.getLogger(SwordfishTestCase.class);
     protected List<ServiceRegistration> registrationsToCancel = new ArrayList<ServiceRegistration>();
     
 	protected BundleContext bundleContext = null;
 
 	/**
 	 * Create a test case instance
 	 */
 	public SwordfishTestCase() {
		long waitForStartup = 0;
 		bundleContext = Activator.getDefault().getContext();
 
 		// Give the framework time to start if one or more bundles had to be started
 		if (startRequiredBundles(bundleContext)) {
 			try {
				LOG.info("Giving framework " + waitForStartup + "ms to start up.");
				Thread.sleep(waitForStartup);
				waitForStartup = 0;
 				
 			} catch (InterruptedException e) {
 				LOG.error("Startup interrupted", e);
 			}
 		}
 	}
 
 	
 	/**
 	 * Start not active bundles needed for testing and print state of bundles.
 	 * @param context - a bundle context
 	 * @return true if at least one bundle had to be started, otherwise false
 	 */
 	private static boolean startRequiredBundles(BundleContext context) {
 		boolean oneOrMoreBundlesWereStarted = false;
 		
 		for (Bundle b : context.getBundles()) {
 			printBundleState(b);
 
 			if (bundleNeedsToBeStarted(b)) {
 				try {
 					b.start();
 					oneOrMoreBundlesWereStarted = true;
 
 				} catch(Exception ex) {
 					LOG.error("Bundle "+ b.getSymbolicName() + " - not started", ex);
 				}
 			}
    		}
 		
 		return oneOrMoreBundlesWereStarted;
 	}
 	
 	
 	/**
 	 * Check if a bundle has to be started
 	 * @param b - a bundle
 	 * @return true - if startup is necessary
 	 */
 	private static boolean bundleNeedsToBeStarted(Bundle b) {
 		return !TestCollector.isFragment(b) 
 				&& (b.getState() != Bundle.ACTIVE)
 				&& !b.getSymbolicName().contains("swordfish.samples")
 				&& !b.getLocation().contains("/eclipse/plugins"); 
 	}
 	
 	
 	/**
 	 * Prints the state of a bundle human readable to the log
 	 * @param b - a bundle instance
 	 */
 	private static void printBundleState(Bundle b) {
 		int state = b.getState();
 		String stateStr;
 			
 		switch(state) {
 		case Bundle.ACTIVE: 
 			stateStr = "Active";
 			break;
 		case Bundle.INSTALLED:
 			stateStr = "Installed";
 			break;
 		case Bundle.UNINSTALLED:
 			stateStr = "Uninstalled";
 			break;
 		case Bundle.STARTING:
 			stateStr = "Starting";
 			break;
 		case Bundle.STOPPING:
 			stateStr = "Stopping";
 			break;
 		case Bundle.RESOLVED:
 			stateStr = "Resolved";
 			break;
 		default:
 			stateStr = "???";
 		}
    			
    		LOG.info("Bundle " + b.getSymbolicName() + " - " + stateStr);
 	}
 	
     /**
      * Add a registration to the internal list to be released during teardown
      * @param serviceRegistration - the registration to be released
      */
     protected void addRegistrationToCancel(ServiceRegistration serviceRegistration) {
     	registrationsToCancel.add(serviceRegistration);
     }
 
     
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() {
 		for (ServiceRegistration sr : registrationsToCancel) {
 			try {
 				sr.unregister();
 			} catch (Exception ex) {
 				LOG.error("Unable to unregister ", ex);
 			} 
 		}
 		
 		registrationsToCancel.clear();
 	}
 }
