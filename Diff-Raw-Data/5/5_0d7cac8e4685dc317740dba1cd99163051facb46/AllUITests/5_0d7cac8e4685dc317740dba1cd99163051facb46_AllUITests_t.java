 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * emueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.ui;
 
 import org.eclipse.emf.emfstore.client.test.common.cases.ESTestWithLoggedInUser;
import org.eclipse.emf.emfstore.client.test.ui.conflictdetection.BidirectionalConflictMergeTest;
 import org.eclipse.emf.emfstore.client.test.ui.controllers.AllUIControllerTests;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 /**
  * Runs all UI tests.
  * 
  * @author emueller
  */
 @RunWith(Suite.class)
 @Suite.SuiteClasses({
	AllUIControllerTests.class,
	BidirectionalConflictMergeTest.class })
 public class AllUITests extends ESTestWithLoggedInUser {
 
 	public static final int TIMEOUT = 20000;
 
 	@BeforeClass
 	public static void beforeClass() {
 		startEMFStore();
 	}
 
 	@AfterClass
 	public static void afterClass() {
 		stopEMFStore();
 	}
 	// private static void startEMFStore() {
 	// ServerConfiguration.setTesting(true);
 	// ServerConfiguration.getProperties().setProperty(ServerConfiguration.XML_RPC_PORT, String.valueOf(8080));
 	// try {
 	// ESEMFStoreController.startEMFStore();
 	// } catch (final FatalESException e) {
 	// fail(e.getMessage());
 	// }
 	// SWTBotPreferences.TIMEOUT = TIMEOUT;
 	// }
 	//
 	// @BeforeClass
 	// public static void beforeClass() {
 	// ESWorkspaceProvider.INSTANCE.setSessionProvider(new TestSessionProvider2());
 	// startEMFStore();
 	// }
 	//
 	// @AfterClass
 	// public static void tearDownAfterClass() throws Exception {
 	// stopEMFStore();
 	// }
 	//
 	// private static void stopEMFStore() {
 	// ESEMFStoreController.stopEMFStore();
 	// try {
 	// // give the server some time to unbind from it's ips. Not the nicest solution ...
 	// Thread.sleep(10000);
 	// } catch (final InterruptedException e) {
 	// fail();
 	// }
 	// }
 }
