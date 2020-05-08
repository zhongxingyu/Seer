 /**
  * Copyright (c) 2005, 2010, Werner Keil, JScience and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Martin Desruisseaux, Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import static org.eclipse.uomo.units.SI.METRE;
 import static org.eclipse.uomo.units.SI.Prefix.CENTI;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigInteger;
 
 import org.eclipse.uomo.units.impl.quantity.LengthAmount;
 import org.junit.Test;
 
 /**
  * Tests {@link QuantityAmount} implementations and subclasses.
  * 
  * @author Martin Desruisseaux
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version $Revision$, $Date$
  */
 public class QuantityAmountTest {
 
     /**
      * Tests the creation quantities backed by the {@code double} primitive
      * type.
      */
     @Test
     public void testDouble() {
 	// final QuantityFactory<Length> factory =
 	// QuantityFactory.getInstance(Length.class);
 	final LengthAmount length = new LengthAmount(Double.valueOf(4.0), METRE);
 
 	assertSame("Wrong tuple element.", METRE, length.getUnit());
 	assertEquals("Wrong tuple element.", Double.valueOf(4.0), length
 		.getNumber());
 	assertEquals("Wrong conversion.", 4.0, length.doubleValue(METRE), 0.0);
	assertEquals("Wrong conversion.", 400.0, length
 		.doubleValue(CENTI(METRE)), 0.0);
 
 	final LengthAmount other = new LengthAmount(8.0, METRE);
 	assertSame("Expected same implementation class.", length.getClass(),
 		other.getClass());
 	assertFalse("Quantities shall not be equal.", length.equals(other));
 	assertFalse("Quantities shall not be equal.",
 		length.hashCode() == other.hashCode());
 
 	final LengthAmount equivalent = new LengthAmount(Double.valueOf(4.0),
 		METRE);
 	assertSame("Expected same implementation class.", length.getClass(),
 		equivalent.getClass());
 	assertFalse("Quantities shall not be equal.", equivalent.equals(other));
 	assertEquals("Quantities shall be equal.", equivalent, length); // .equals(length));
 	assertTrue("'equals' shall be symmetric.", length.equals(equivalent));
 	assertEquals("Expected same hash code.", length.hashCode(), equivalent
 		.hashCode());
     }
 
     /**
      * Tests the creation quantities backed by the {@link Number} class.
      */
     @Test
     public void testNumber() {
 	// final QuantityFactory<Length> factory =
 	// QuantityFactory.getInstance(Length.class);
 	final BigInteger value = BigInteger.valueOf(4);
 	final LengthAmount length = new LengthAmount(value, METRE);
 
 	assertSame("Wrong tuple element.", METRE, length.getUnit());
 	assertEquals("Wrong tuple element.", BigInteger.valueOf(4), length
 		.getNumber());
 	assertEquals("Wrong conversion.", 4.0, length.doubleValue(METRE), 0.0);
	assertEquals("Wrong conversion.", 400.0, length
 		.doubleValue(CENTI(METRE)), 0.0);
 
 	// Quantity equivalent to 'length', but backed by a double.
 	// This is not the same class, but should nevertheless by considered
 	// equivalent.
 	final LengthAmount equivalent = new LengthAmount(4, METRE);
 	assertTrue("Quantities shall be equal.", equivalent.equals(length));
 	assertTrue("'equals' shall be symmetric.", length.equals(equivalent));
 	assertEquals("Expected same hash code.", length.hashCode(), equivalent
 		.hashCode());
     }
 }
