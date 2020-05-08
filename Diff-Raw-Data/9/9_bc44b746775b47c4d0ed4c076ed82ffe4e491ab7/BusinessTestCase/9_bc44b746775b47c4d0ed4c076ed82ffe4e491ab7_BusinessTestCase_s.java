 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.authorizationservice;
 
 import java.security.AccessControlException;
 
 /**
  * 
  */
 public class BusinessTestCase {
 
 	boolean hasPermission() {
 		try {
 			SecurityManager sm = System.getSecurityManager();
 			if (sm != null) {
 				sm.checkPermission(new TestcasePermission("testPerm"));
 				return true;
 			}
			return true;
 		} catch (AccessControlException ex) {
 			return false;
 		}
 	}
 }
