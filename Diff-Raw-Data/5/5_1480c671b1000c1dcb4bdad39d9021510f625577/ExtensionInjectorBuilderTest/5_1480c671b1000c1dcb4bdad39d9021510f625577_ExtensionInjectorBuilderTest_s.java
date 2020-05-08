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
 import java.util.List;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInjector;
 import org.eclipse.riena.core.injector.extension.IData;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.core.wire.ExtensionInjectorBuilder;
 
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
 		assertEquals("testA", rawId(injector));
 		assertEquals(IData.class, useType(injector));
 		assertEquals(0, getMin(injector));
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
 
 	public void testBuildForUpdate1Array() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class.getDeclaredMethod("update1Array",
 				new Class[] { IData[].class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		assertEquals("testA[]", rawId(injector));
 		assertEquals(IData.class, useType(injector));
 		assertEquals(0, getMin(injector));
 		assertEquals(Integer.MAX_VALUE, getMax(injector));
 		assertTrue(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("update1Array", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
 		assertFalse(getSpecific(injector));
 	}
 
 	@InjectExtension(id = "testA[]")
 	public void update1Array(IData[] data) {
 	}
 
 	public void testBuildForUpdate2() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class.getDeclaredMethod("update2",
 				new Class[] { IData[].class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		assertEquals("testB", rawId(injector));
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
 		assertEquals("testC", rawId(injector));
 		assertEquals(IData.class, useType(injector));
 		assertEquals(0, getMin(injector));
 		assertEquals(1, getMax(injector));
 		assertFalse(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("update3", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
 		assertTrue(getSpecific(injector));
 	}
 
 	@InjectExtension(id = "testC", heterogeneous = true, specific = true, min = 0, max = 1)
 	public void update3(IData data) {
 	}
 
 	public void testBuildForUpdateWithAnExtensionInterfaceWithID() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class.getDeclaredMethod("updateWithID",
 				new Class[] { IDataWithID.class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		String expectedId = getContext().getBundle().getSymbolicName() + ".testWithID";
 		try {
 			injector.andStart(getContext());
 		} catch (IllegalArgumentException e) {
 			assertTrue(e.getMessage().contains(expectedId));
 		}
 		assertEquals(expectedId, firstNormalizedId(injector));
 		assertEquals(IDataWithID.class, useType(injector));
 		assertEquals(0, getMin(injector));
 		assertEquals(1, getMax(injector));
 		assertFalse(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("updateWithID", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
 		assertTrue(getSpecific(injector));
 	}
 
 	@InjectExtension(heterogeneous = true, specific = true, min = 0, max = 1)
 	public void updateWithID(IDataWithID data) {
 	}
 
 	public void testBuildForUpdateWithAnExtensionInterfaceWithIDinAnnotation() throws NoSuchMethodException {
 		Method bindMethod = ExtensionInjectorBuilderTest.class.getDeclaredMethod("updateWithIDinAnnotation",
 				new Class[] { IDataWithIDinAnnotation.class });
 		ExtensionInjectorBuilder builder = new ExtensionInjectorBuilder(this, bindMethod);
 		ExtensionInjector injector = builder.build();
 		assertNotNull(injector);
 		String expectedId = getContext().getBundle().getSymbolicName() + ".testWithIDinAnnotation";
 		try {
 			injector.andStart(getContext());
 		} catch (IllegalArgumentException e) {
 			assertTrue(e.getMessage().contains(expectedId));
 		}
 		assertEquals(expectedId, firstNormalizedId(injector));
 		assertEquals(IDataWithIDinAnnotation.class, useType(injector));
 		assertEquals(0, getMin(injector));
 		assertEquals(1, getMax(injector));
 		assertFalse(getHomogenious(injector));
 		assertSame(this, getBean(injector));
 		assertEquals("updateWithIDinAnnotation", getUpdate(injector));
 		assertFalse(getDoNotReplaceSymbols(injector));
 		assertTrue(getSpecific(injector));
 	}
 
 	@InjectExtension(heterogeneous = true, specific = true, min = 0, max = 1)
 	public void updateWithIDinAnnotation(IDataWithIDinAnnotation data) {
 	}
 
 	private String rawId(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		Object extensionPointId = ReflectionUtils.getHidden(extensionDescriptor, "extensionPointId");
 		return ReflectionUtils.getHidden(extensionPointId, "rawId");
 	}
 
 	private String firstNormalizedId(ExtensionInjector injector) {
 		Object extensionDescriptor = ReflectionUtils.getHidden(injector, "extensionDesc");
 		Object extensionPointId = ReflectionUtils.getHidden(extensionDescriptor, "extensionPointId");
 		return ((List<String>) ReflectionUtils.getHidden(extensionPointId, "normalizedIds")).get(0);
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
