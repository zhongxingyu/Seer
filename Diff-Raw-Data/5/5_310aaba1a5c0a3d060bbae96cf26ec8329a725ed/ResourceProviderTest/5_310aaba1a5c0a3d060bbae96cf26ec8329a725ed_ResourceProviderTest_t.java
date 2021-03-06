 /*******************************************************************************
  * Copyright (c) 2012 GoPivotal, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     GoPivotal, Inc. - initial API and implementation
  *******************************************************************************/
 package org.springsource.ide.eclipse.commons.core;
 
 import junit.framework.TestCase;
 
 /**
  * @author Steffen Pingel
  */
 public class ResourceProviderTest extends TestCase {
 
 	public void testGetCustomValue() {
 		assertEquals("http://my.bug.tracker", ResourceProvider.getUrl("dashboard.bug.tracker"));
 	}
 
 	public void testGetUrlOverrideValue() {
 		assertEquals("my.value", ResourceProvider.getUrl("my.key"));
 	}
 
 	public void testGetUrls() {
 		assertEquals(1, ResourceProvider.getUrls("dashboard.feeds.update").length);
		assertEquals(1, ResourceProvider.getUrls("dashboard.feeds.blogs").length);
 	}
 
 }
