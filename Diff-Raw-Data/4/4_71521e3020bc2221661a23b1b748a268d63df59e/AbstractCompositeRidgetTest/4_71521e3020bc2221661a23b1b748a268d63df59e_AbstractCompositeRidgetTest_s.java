 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.ui.ridgets.swt.LabelRidget;
 import org.eclipse.riena.ui.ridgets.swt.DefaultRealm;
 
 /**
  * Tests for the class {@link AbstractCompositeRidget}.
  */
 @NonUITestCase
 public class AbstractCompositeRidgetTest extends RienaTestCase {
 
 	private DefaultRealm defaultRealm;
 	private MyCompositeRidget ridget;
 
 	@Override
 	protected void setUp() throws Exception {
 		defaultRealm = new DefaultRealm();
 		ridget = new MyCompositeRidget();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		defaultRealm.dispose();
 	}
 
 	public void testRemoveRidgets() {
 		ridget.addRidget("foo", new LabelRidget());
 		ridget.addRidget("bar", new LabelRidget());
 
 		assertEquals(2, ridget.getRidgets().size());
 
		ridget.removeRidgets();
 
 		assertEquals(0, ridget.getRidgets().size());
 	}
 
 	// helping classes 
 	//////////////////
 
 	private static final class MyCompositeRidget extends AbstractCompositeRidget {
 	}
 
 }
