 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.tests.misc;
 
 import junit.framework.TestCase;
 
import org.eclipse.mylar.internal.hypertext.WebStructureBridge;
 
 /**
  * @author Mik Kersten
  */
 public class HypertextStructureBridgeTest extends TestCase {
 
	private WebStructureBridge bridge = new WebStructureBridge();
 
 	public void testParentHandle() {
 		String site = "http://www.foo.bar";
 		String page = "http://www.foo.bar/part/index.html";
 		assertEquals(site, bridge.getParentHandle(page));
 	}
 
 }
