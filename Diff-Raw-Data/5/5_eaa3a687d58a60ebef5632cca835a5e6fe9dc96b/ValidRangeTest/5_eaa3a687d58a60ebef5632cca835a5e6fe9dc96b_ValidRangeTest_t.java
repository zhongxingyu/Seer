 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.validation;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Locale;
 
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.ui.swt.test.TestUtils;
 
 /**
  * Tests for the {@link ValidRange} rule.
  */
 @NonUITestCase
 public class ValidRangeTest extends RienaTestCase {
 
 	/**
 	 * Create a range using the 0-arg constructor.
 	 */
 	protected ValidRange createRange() {
 		return new ValidRange();
 	}
 
 	/**
 	 * Create a range using the 2-arg constructor.
 	 */
 	protected ValidRange createRange(final Number min, final Number max) {
 		return new ValidRange(min, max);
 	}
 
 	/**
 	 * Create a range using the 3-arg constructor.
 	 */
 	protected ValidRange createRange(final Number min, final Number max, final Locale locale) {
 		return new ValidRange(min, max, locale);
 	}
 
 	public void testRangeUSlocale() {
 		ValidRange rule = createRange(0, 10, Locale.US);
 		assertTrue(rule.validate(null).isOK());
 		assertTrue(rule.validate("").isOK());
 		assertTrue(rule.validate("0").isOK());
 		assertTrue(rule.validate("2.5").isOK());
 		assertTrue(rule.validate("5").isOK());
 		assertTrue(rule.validate("10").isOK());
 
 		assertFalse(rule.validate("-0.0000001").isOK());
 		assertFalse(rule.validate("10.0000001").isOK());
 
 		rule = createRange(-5000, 5000, Locale.US);
 		assertTrue(rule.validate("-5000").isOK());
 		assertTrue(rule.validate("-5,000").isOK());
 		assertTrue(rule.validate("- 5,000").isOK());
 		assertFalse(rule.validate("-5000.0001").isOK());
 		assertFalse(rule.validate("-5,000.0001").isOK());
 	}
 
 	public void testEmptyValuesUSLocale() {
 		final ValidRange rule = createRange(10, 20, Locale.US);
 		assertFalse(rule.validate(null).isOK());
 		assertFalse(rule.validate("").isOK());
 	}
 
 	public void testRangeGermanLocale() {
 		ValidRange rule = createRange(0, 10, Locale.GERMANY);
 		assertTrue(rule.validate(null).isOK());
 		assertTrue(rule.validate("").isOK());
 		assertTrue(rule.validate("0").isOK());
 		assertTrue(rule.validate("2,5").isOK());
 		assertTrue(rule.validate("5").isOK());
 		assertTrue(rule.validate("10").isOK());
 		assertFalse(rule.validate("-0,0000001").isOK());
 		assertFalse(rule.validate("10,0000001").isOK());
 
 		rule = createRange(-5000, 5000, Locale.GERMANY);
 		assertTrue(rule.validate("-5000").isOK());
 		assertTrue(rule.validate("-5.000").isOK());
 		assertTrue(rule.validate("- 5.000").isOK());
 		assertFalse(rule.validate("-5000,0001").isOK());
 		assertFalse(rule.validate("-5.000,0001").isOK());
 	}
 
 	public void testEmptyValuesGermanLocale() {
 		final ValidRange rule = createRange(10, 20, Locale.GERMANY);
 		assertFalse(rule.validate(null).isOK());
 		assertFalse(rule.validate("").isOK());
 	}
 
 	public void testRangeArabLocale() {
 		if (!TestUtils.isArabLocaleAvailable()) {
 			System.err
 					.println(getClass().getName()
 							+ ".testRangeArabLocale(): Skipping test because no Arab locale is available. Use international JRE to run all tests.");
 			return;
 		}
 
 		// Arab locales have a trailing minus
 		ValidRange rule = createRange(0, 10, new Locale("ar", "AE"));
 		assertTrue(rule.validate(null).isOK());
 		assertTrue(rule.validate("").isOK());
 		assertTrue(rule.validate("0").isOK());
 		assertTrue(rule.validate("2.5").isOK());
 		assertTrue(rule.validate("5").isOK());
 		assertTrue(rule.validate("10").isOK());
 		assertFalse(rule.validate("0.0000001-").isOK());
 		assertFalse(rule.validate("10.0000001").isOK());
 
 		rule = createRange(-5000, 5000, new Locale("ar", "AE"));
 		assertTrue(rule.validate("5000-").isOK());
 		assertTrue(rule.validate("5,000-").isOK());
 		assertTrue(rule.validate("5,000 -").isOK());
 		assertFalse(rule.validate("5000.0001-").isOK());
 		assertFalse(rule.validate("5,000.0001 -").isOK());
 	}
 
 	public void testEmptyValuesArabLocale() {
 		final ValidRange rule = createRange(10, 20, new Locale("ar", "AE"));
 		assertFalse(rule.validate(null).isOK());
 		assertFalse(rule.validate("").isOK());
 	}
 
 	public void testConstructorInitTypes() {
 		ValidRange rule = createRange((byte) -10, (byte) 10);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange((short) -10, (short) 10);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange((long) -10, (long) 10);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange((float) -10, (float) 10);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange((double) -10, (double) 10);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange(BigInteger.ZERO, BigInteger.TEN);
 		assertTrue(rule.validate("10").isOK());
 		rule = createRange(BigDecimal.ZERO, BigDecimal.TEN);
 		assertTrue(rule.validate("10").isOK());
 	}
 
 	public void testUnparseableNumbers() {
 		final ValidRange rule = createRange(0, 10, Locale.US);
 		assertFalse(rule.validate("A10").isOK());
 		assertFalse(rule.validate("1A0").isOK());
 		assertFalse(rule.validate("10A").isOK());
 	}
 
 	public void testConstructorSanity() throws Exception {
 		// different types:
 		try {
 			createRange((byte) 10, (short) 10);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		try {
 			createRange(10, 1000d);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		// min greater max:
 		try {
 			createRange(100, 10);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		// null parameter
 		try {
 			createRange(null, 10);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		try {
 			createRange(100, null);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		try {
 			createRange(null, null);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 		try {
 			createRange(10, 100, null);
 			fail("expected some RuntimeException");
 		} catch (final RuntimeException e) {
 			ok("passed test");
 		}
 	}
 
 	public void testSetInitializationData() throws Exception {
 		ValidRange rule = createRange();
 		assertTrue(rule.validate("0").isOK());
 		assertFalse(rule.validate("10").isOK());
 
 		rule = createRange();
 		rule.setInitializationData(null, null, "1");
 		assertFalse(rule.validate("1").isOK());
 		assertFalse(rule.validate("10").isOK());
 
 		rule = createRange();
 		rule.setInitializationData(null, null, "1,10");
 		assertTrue(rule.validate("1").isOK());
 		assertTrue(rule.validate("10").isOK());
 		assertFalse(rule.validate("0").isOK());
 		assertFalse(rule.validate("11").isOK());
 
 		rule = createRange();
 		rule.setInitializationData(null, null, "1.1,10.1");
 		assertFalse(rule.validate("1").isOK());
 		assertTrue(rule.validate("2").isOK());
 		assertTrue(rule.validate("10").isOK());
 		assertFalse(rule.validate("0").isOK());
 		assertFalse(rule.validate("11").isOK());
 
 		rule = createRange();
 		String localString = Locale.US.getLanguage() + "," + Locale.US.getCountry();
 		rule.setInitializationData(null, null, "1.1,10.1," + localString);
 		assertFalse(rule.validate("1").isOK());
 		assertTrue(rule.validate("1.1").isOK());
 
 		rule = createRange();
 		localString = Locale.GERMANY.getLanguage() + "," + Locale.GERMANY.getCountry();
 		rule.setInitializationData(null, null, "1.1,10.1," + localString);
 		assertFalse(rule.validate("1").isOK());
 		assertTrue(rule.validate("1,1").isOK());
 	}
 
 	public void testDoubleValuesWithMinMaxEqual() {
 		Double min = 5000.0;
 		Double max = 5000.0;
 		ValidRange rule = createRange(min, max);
 		String value = "5000";
 
 		assertTrue(rule.validate(value).isOK());
 
 		min = 5000.5;
 		max = 5000.5;
 		rule = createRange(min, max);
		value = TestUtils.getLocalizedNumber("5000,5");
 
 		assertTrue(rule.validate(value).isOK());
 
 		min = 5000.55;
 		max = 5000.55;
 		rule = createRange(min, max);
		value = TestUtils.getLocalizedNumber("5000,55");
 
 		assertTrue(rule.validate(value).isOK());
 	}
 
 }
