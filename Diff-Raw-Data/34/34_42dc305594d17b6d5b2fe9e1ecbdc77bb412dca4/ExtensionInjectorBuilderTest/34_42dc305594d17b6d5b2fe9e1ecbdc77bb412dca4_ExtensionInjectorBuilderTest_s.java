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
 package org.eclipse.riena.core.wire;
 
 import java.lang.reflect.Method;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInjector;
 import org.eclipse.riena.core.injector.extension.IData;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.wire.ExtensionInjectorBuilder;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 
 /**
  * Test the {@code ExtensionInjectorBuilder}.
  */
 @NonUITestCase
 public class ExtensionInjectorBuilderTest extends RienaTestCase {
 
 	public void testBuildForUpdate1() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class
 				.getDeclaredMethod("update1", new Class[] { IData.class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		assertEquals("testA", id(injector));
 		assertEquals(IData.class, useType(injector));
		assertEquals(1, getMin(injector));
 		assertEquals(1, getMax(injector));
 		assertTrue(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("update1", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
 		assertFalse(getSpecific(injector));
 	}
 
 	@InjectExtension(id = "testA")
 	public void update1(IData data) {
 
 	}
 
 	public void testBuildForUpdate2() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class.getDeclaredMethod("update2",
 				new Class[] { IData[].class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		assertEquals("testB", id(injector));
 		assertEquals(IData.class, useType(injector));
 		assertEquals(2, getMin(injector));
 		assertEquals(5, getMax(injector));
 		assertFalse(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("update2", getUpdate(injector));
 		assertTrue(getDoNotReplaceSymbols(injector));
 		assertTrue(getSpecific(injector));
 	}
 
 	@InjectExtension(id = "testB", doNotReplaceSymbols = true, heterogeneous = true, specific = true, min = 2, max = 5)
 	public void update2(IData[] data) {
 
 	}
 
 	public void testBuildForUpdate3() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class
 				.getDeclaredMethod("update3", new Class[] { IData.class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		assertEquals("testC", id(injector));
 		assertEquals(IData.class, useType(injector));
 		assertEquals(0, getMin(injector));
 		assertEquals(1, getMax(injector));
		assertTrue(getHomogenious(injector));
 		assertSame(this, getBean(injector));
		assertEquals("update1", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
		assertFalse(getSpecific(injector));
 	}
 
 	@InjectExtension(id = "testC", heterogeneous = true, specific = true, min = 0, max = 1)
 	public void update3(IData data) {
 
 	}
 
 	private String id(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		return ReflectionUtils.getHidden(extensionDescriptor, "extensionPointId");
 	}
 
 	private Object useType(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		return ReflectionUtils.getHidden(extensionDescriptor, "interfaceType");
 	}
 
 	private int getMin(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		return ReflectionUtils.getHidden(extensionDescriptor, "minOccurences");
 	}
 
 	private int getMax(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		return ReflectionUtils.getHidden(extensionDescriptor, "maxOccurences");
 	}
 
 	private boolean getHomogenious(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		return ReflectionUtils.getHidden(extensionDescriptor, "homogeneous");
 	}
 
 	private Object getBean(ExtensionInjector injector) {
 		return ReflectionUtils.getHidden(injector, "target");
 	}
 
 	private String getUpdate(ExtensionInjector injector) {
 		return ReflectionUtils.getHidden(injector, "updateMethodName");
 	}
 
 	private boolean getDoNotReplaceSymbols(ExtensionInjector injector) {
 		return !(Boolean) ReflectionUtils.getHidden(injector, "symbolReplace");
 	}
 
 	private boolean getSpecific(ExtensionInjector injector) {
 		return !(Boolean) ReflectionUtils.getHidden(injector, "nonSpecific");
 	}
 
 }
