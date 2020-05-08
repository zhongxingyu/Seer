 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.server;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 /**
  * .
  * 
  * @author wesendon
  */
 @RunWith(Suite.class)
 @Suite.SuiteClasses({ ServerInterfaceTest.class, InvalidArgumentsTest.class, InvalidAuthenticationTest.class,
	PropertiesTest.class, FileManagerTest.class })
 public class AllServerTests {
 
 }
