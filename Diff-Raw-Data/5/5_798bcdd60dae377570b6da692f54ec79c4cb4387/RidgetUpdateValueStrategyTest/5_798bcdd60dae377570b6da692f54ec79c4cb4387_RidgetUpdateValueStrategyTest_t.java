 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.databinding;
 
 import java.util.GregorianCalendar;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.conversion.NumberToStringConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.IStatus;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.ui.ridgets.ValueBindingSupport;
 
 /**
  * Tests of the class <code>RidgetUpdateValueStrategy</code>.
  */
 @NonUITestCase
 public class RidgetUpdateValueStrategyTest extends TestCase {
 
 	public void testCreateConverter() throws Exception {
 
 		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(EasyMock.createNiceMock(IObservableValue.class)));
 
 		IConverter converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Double.TYPE);
 		assertTrue(converter instanceof StringToNumberAllowingNullConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Float.TYPE);
 		assertTrue(converter instanceof StringToNumberAllowingNullConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Long.TYPE);
 		assertTrue(converter instanceof StringToNumberAllowingNullConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Integer.TYPE);
 		assertTrue(converter instanceof StringToNumberAllowingNullConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, GregorianCalendar.class);
 		assertTrue(converter instanceof StringToGregorianCalendarConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", GregorianCalendar.class, String.class);
 		assertTrue(converter instanceof GregorianCalendarToStringConverter);
 
 		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", Integer.class, String.class);
 		assertTrue(converter instanceof NumberToStringConverter);
 
 	}
 
 	public void testConstructors() throws Exception {
 		try {
 			new RidgetUpdateValueStrategy(null);
 			fail("expected RuntimeException");
 		} catch (final RuntimeException e) {
 			// everything is fine
 		}
 
 		try {
 			new RidgetUpdateValueStrategy(null, UpdateValueStrategy.POLICY_UPDATE);
 			fail("expected RuntimeException");
 		} catch (final RuntimeException e) {
 			// everything is fine
 		}
 
 		try {
 			new RidgetUpdateValueStrategy(null, true, UpdateValueStrategy.POLICY_UPDATE);
 			fail("expected RuntimeException");
 		} catch (final RuntimeException e) {
 			// everything is fine
 		}
 
 	}
 
 	public void testValidateAfterSetWithSetError() throws Exception {
 		final IObservableValue mock = EasyMock.createNiceMock(IObservableValue.class);
 		mock.setValue(EasyMock.anyObject());
 		EasyMock.expectLastCall().andThrow(new RuntimeException("Something went wrong"));
 		EasyMock.replay(mock);
 		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(mock)) {
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see org.eclipse.riena.ui.ridgets.databinding.RidgetUpdateValueStrategy#validateAfterSet()
 			 */
 			@Override
 			protected IStatus validateAfterSet() {
 				fail("This method should not be called when doSet() returns an error status.");
 				return null;
 			}
 		};
 
		ReflectionUtils.invokeHidden(strategy, "doSet", mock, "one two");
 	}
 
 	public void testValidateAfterSetWithSetOk() throws Exception {
 		final boolean[] validateAfterSetCalled = new boolean[1]; // must be final and modifiable
 		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(EasyMock.createNiceMock(IObservableValue.class))) {
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see org.eclipse.riena.ui.ridgets.databinding.RidgetUpdateValueStrategy#validateAfterSet()
 			 */
 			@Override
 			protected IStatus validateAfterSet() {
 				validateAfterSetCalled[0] = true;
 				return super.validateAfterSet();
 			}
 		};
 
		ReflectionUtils.invokeHidden(strategy, "doSet", EasyMock.createNiceMock(IObservableValue.class), "one two");
 		assertTrue("validateAfterSet() must be called if doSet() returned an OK status.", validateAfterSetCalled[0]);
 	}
 }
