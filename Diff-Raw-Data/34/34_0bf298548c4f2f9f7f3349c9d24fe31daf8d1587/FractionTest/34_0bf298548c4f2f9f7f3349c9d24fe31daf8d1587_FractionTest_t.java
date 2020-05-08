 package edu.grinnell.csc207.nguyenti.utils;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.junit.Test;
 
 public class FractionTest {
 
 	@Test
 	public void testHashCode() throws Exception {
 		assertEquals("Two BigInts as input", new Fraction(
 				BigInteger.valueOf(5), BigInteger.valueOf(3)).hashCode(),
 				new Fraction(BigInteger.valueOf(5), BigInteger.valueOf(3))
 						.hashCode());
 		assertEquals("Two ints as input", new Fraction(12, 5).hashCode(),
 				new Fraction(12, 5).hashCode());
 		assertEquals("Two strings as input", new Fraction("-72/16").hashCode(),
 				new Fraction("-72/16").hashCode());
 	}
 
 	@Test
 	public void testFractionIntInt() throws Exception {
 		assertEquals("Numerator and denominator are small positive numbers", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction(1, 6)));
 		assertEquals("Numerator and denominator are negative", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction(-1, -6)));
 		assertEquals("Numerator is negative, denominator is positive", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction(-1, 6)));
 		assertEquals("Numerator is positive, denominator is negative", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction(1, -6)));
 		assertEquals(
 				"Numerator and denominator are large positive numbers",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MAX_VALUE), BigInteger
 						.valueOf(Integer.MAX_VALUE - 10))
 						.compareTo(new Fraction(Integer.MAX_VALUE,
 								Integer.MAX_VALUE - 10)));
 		assertEquals(
 				"Numerator and denominator are large negative numbers",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger
 						.valueOf(Integer.MIN_VALUE + 10))
 						.compareTo(new Fraction(Integer.MIN_VALUE,
 								Integer.MIN_VALUE + 10)));
 		assertEquals(
 				"Numerator is a large positive number, denominator is a large negative number",
 				0, new Fraction(BigInteger.valueOf(Integer.MAX_VALUE),
 						BigInteger.valueOf(Integer.MIN_VALUE))
 						.compareTo(new Fraction(Integer.MAX_VALUE,
 								Integer.MIN_VALUE)));
 		assertEquals("Numerator is a large positive number, denominator is 1",
 				0, new Fraction(BigInteger.valueOf(Integer.MAX_VALUE),
 						BigInteger.valueOf(1)).compareTo(new Fraction(
 						Integer.MAX_VALUE, 1)));
 		assertEquals("Numerator is a large negative number, denominator is 1",
 				0, new Fraction(BigInteger.valueOf(Integer.MIN_VALUE),
 						BigInteger.valueOf(1)).compareTo(new Fraction(
 						Integer.MIN_VALUE, 1)));
 
 	}
 
 	@Test
 	public void testFractionBigInteger() throws Exception {
 		assertEquals("Numerator is a small positive", 0,
 				new Fraction(BigInteger.valueOf(6), BigInteger.valueOf(1))
 						.compareTo(new Fraction(BigInteger.valueOf(6))));
 		assertEquals(
 				"Numerator is a large positive",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MAX_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(BigInteger
 						.valueOf(Integer.MAX_VALUE))));
 		assertEquals("Numerator is a small negative", 0,
 				new Fraction(BigInteger.valueOf(-6), BigInteger.valueOf(1))
 						.compareTo(new Fraction(BigInteger.valueOf(-6))));
 		assertEquals(
 				"Numerator is a large negative",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(BigInteger
 						.valueOf(Integer.MIN_VALUE))));
 	}
 
 	@Test
 	public void testFractionInt() throws Exception {
 		assertEquals("Numerator is a small positive", 0,
 				new Fraction(BigInteger.valueOf(6), BigInteger.valueOf(1))
 						.compareTo(new Fraction(6)));
 		assertEquals(
 				"Numerator is a large positive",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MAX_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(Integer.MAX_VALUE)));
 		assertEquals("Numerator is a small negative", 0,
 				new Fraction(BigInteger.valueOf(-6), BigInteger.valueOf(1))
 						.compareTo(new Fraction(-6)));
 		assertEquals(
 				"Numerator is a large negative",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(Integer.MIN_VALUE)));
 	}
 
 	@Test
 	public void testFractionString() throws Exception {
 		assertEquals("Numerator and denominator are small positive numbers", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction("1/6")));
 		assertEquals("Numerator and denominator are negative", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction("-1/-6")));
 		assertEquals("Numerator is negative, denominator is positive", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction("-1/6")));
 		assertEquals("Numerator is positive, denominator is negative", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction("1/-6")));
		assertEquals("Numerator is positive, no denominator", 0,
				new Fraction(BigInteger.valueOf(8), BigInteger.valueOf(1))
						.compareTo(new Fraction("8")));
		assertEquals("Numerator is negative, no denominator", 0,
				new Fraction(BigInteger.valueOf(-8), BigInteger.valueOf(1))
						.compareTo(new Fraction("-8")));
 	}
 
 	@Test
 	public void testFractionLongLong() throws Exception {
 		assertEquals("Numerator and denominator are small positive numbers", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction((long) 1, (long) 6)));
 		assertEquals("Numerator and denominator are negative", 0,
 				new Fraction(BigInteger.valueOf(1), BigInteger.valueOf(6))
 						.compareTo(new Fraction((long) -1, (long) -6)));
 		assertEquals("Numerator is negative, denominator is positive", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction((long) -1, (long) 6)));
 		assertEquals("Numerator is positive, denominator is negative", 0,
 				new Fraction(BigInteger.valueOf(-1), BigInteger.valueOf(6))
 						.compareTo(new Fraction((long) 1, (long) -6)));
 		assertEquals(
 				"Numerator and denominator are large positive numbers",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MAX_VALUE), BigInteger
 						.valueOf(Integer.MAX_VALUE - 10))
 						.compareTo(new Fraction((long) Integer.MAX_VALUE,
 								(long) Integer.MAX_VALUE - 10)));
 		assertEquals(
 				"Numerator and denominator are large negative numbers",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger
 						.valueOf(Integer.MIN_VALUE + 10))
 						.compareTo(new Fraction((long) Integer.MIN_VALUE,
 								(long) Integer.MIN_VALUE + 10)));
 		assertEquals(
 				"Numerator is a large positive number, denominator is a large negative number",
 				0, new Fraction(BigInteger.valueOf(Integer.MAX_VALUE),
 						BigInteger.valueOf(Integer.MIN_VALUE))
 						.compareTo(new Fraction((long) Integer.MAX_VALUE,
 								(long) Integer.MIN_VALUE)));
 		assertEquals("Numerator is a large positive number, denominator is 1",
 				0, new Fraction(BigInteger.valueOf(Integer.MAX_VALUE),
 						BigInteger.valueOf(1)).compareTo(new Fraction(
 						(long) Integer.MAX_VALUE, (long) 1)));
 		assertEquals("Numerator is a large negative number, denominator is 1",
 				0, new Fraction(BigInteger.valueOf(Integer.MIN_VALUE),
 						BigInteger.valueOf(1)).compareTo(new Fraction(
 						(long) Integer.MIN_VALUE, (long) 1)));
 	}
 
 	@Test
 	public void testFractionDouble() throws Exception {
 		assertEquals("Numerator is a small positive", 0,
 				new Fraction(BigInteger.valueOf(6), BigInteger.valueOf(1))
 						.compareTo(new Fraction(6.0)));
 		assertEquals(
 				"Numerator is a large positive",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MAX_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(
 						(double) Integer.MAX_VALUE)));
 		assertEquals("Numerator is a small negative", 0,
 				new Fraction(BigInteger.valueOf(-6), BigInteger.valueOf(1))
 						.compareTo(new Fraction((double) -6)));
 		assertEquals(
 				"Numerator is a large negative",
 				0,
 				new Fraction(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger
 						.valueOf(1)).compareTo(new Fraction(
 						(double) Integer.MIN_VALUE)));
 		assertEquals("Small non-integer double", 0,
 				new Fraction(BigInteger.valueOf(5), BigInteger.valueOf(2))
 						.compareTo(new Fraction(2.5)));
 		assertEquals("Negative non-integer double", 0,
 				new Fraction(BigInteger.valueOf(-5), BigInteger.valueOf(2))
 						.compareTo(new Fraction(-2.5)));
 	}
 
 	@Test
 	public void testToString() throws Exception {
 		assertEquals("basic fraction", "1/6", new Fraction(1, 6).toString());
 	}
 
 	@Test
 	public void testClone() throws Exception {
 		assertEquals("basic fraction", 0, new Fraction(-5, -2).clone()
 				.compareTo(new Fraction(-5, -2)));
 	}
 
 	@Test
 	public void testEqualsFraction() {
 		assertEquals("basic fraction", true,
 				new Fraction(-4).equals(new Fraction(-4)));
 	}
 
 	@Test
 	public void testAdd() throws Exception {
 		assertEquals("basic fractions", 0, new Fraction(6)
 				.add(new Fraction(-2)).compareTo(new Fraction(4)));
 	}
 
 	@Test
 	public void testSubtract() throws Exception {
 		assertEquals(
 				"basic fractions",
 				0,
 				new Fraction(6).subtract(new Fraction(-2)).compareTo(
 						new Fraction(8)));
 	}
 
 	@Test
 	public void testToDouble() throws Exception {
 		assertEquals("basic fractions", 2.5, new Fraction(5, 2).toDouble(),
 				.001);
 	}
 
 	@Test
 	public void testToBigDecimal() throws Exception {
 		assertEquals("basic fraction", 0, new Fraction(13, 4).toBigDecimal()
 				.compareTo(new BigDecimal(3.25)));
 	}
 
 	@Test
 	public void testMultiplyBy() {
 		assertEquals(
 				"basic fraction",
 				0,
 				new Fraction(2).multiplyBy(new Fraction(3)).compareTo(
 						new Fraction(6)));
 	}
 
 	@Test
 	public void testDivideBy() throws Exception {
 		assertEquals(
 				"basic fraction",
 				0,
 				new Fraction(6).divideBy(new Fraction(3)).compareTo(
 						new Fraction(2)));
 	}
 
 	@Test
 	public void testPow() throws Exception {
 		assertEquals("basic fraction", 0,
 				new Fraction(4).pow(2).compareTo(new Fraction(16)));
 	}
 
 	@Test
 	public void testReciprocal() throws Exception {
 		assertEquals("basic fraction", 0, new Fraction(5, 4).reciprocal()
 				.compareTo(new Fraction(4, 5)));
 	}
 
 	@Test
 	public void testNegate() throws Exception {
 		assertEquals("basic fraction", 0,
 				new Fraction(5).negate().compareTo(new Fraction(-5)));
 	}
 
 	@Test
 	public void testNumerator() throws Exception {
 		assertEquals("basic fraction", BigInteger.valueOf(2), new Fraction(2, 15).numerator());
 	}
 
 	@Test
 	public void testDenominator() throws Exception {
 		assertEquals("basic fraction", BigInteger.valueOf(45), new Fraction(2, 45).denominator());
 	}
 
 	@Test
 	public void testFractionalPart() throws Exception {
 		assertEquals("basic fraction", 0, new Fraction(7, 2).fractionalPart()
 				.compareTo(new Fraction(1, 2)));
 	}
 
 	@Test
 	public void testWholePart() throws Exception {
 		assertEquals("basic fraction", BigInteger.valueOf(3),
 				new Fraction(7, 2).wholePart());
 	}
 
 	//
 	// @Test
 	// public void testConstruction() throws Exception {
 	// // TEST BASIC EZ STUFF
 	// Fraction newFracZ = new Fraction(BigInteger.valueOf(5),
 	// BigInteger.valueOf(2));
 	// // assertEquals("from string", new Fraction(BigInteger.valueOf(1),
 	// // BigInteger.valueOf(6)), new Fraction("1/6"));
 	// // assertSame("from double", newFracZ, new Fraction(2.5));
 	// }
 	//
 	// @Test
 	// public void testToString() throws Exception {
 	// Fraction newFrac = new Fraction(BigInteger.valueOf(1),
 	// BigInteger.valueOf(6));
 	// Fraction newFrac1 = new Fraction(BigInteger.valueOf(-1),
 	// BigInteger.valueOf(6));
 	// Fraction newFrac2 = new Fraction(BigInteger.valueOf(1),
 	// BigInteger.valueOf(-6));
 	// Fraction newFrac3 = new Fraction(BigInteger.valueOf(-1),
 	// BigInteger.valueOf(-6));
 	// Fraction newFrac4 = new Fraction(1, 6);
 	// Fraction newFrac5 = new Fraction(2.5);
 	//
 	// assertEquals("natural numerator + denom", "1/6", newFrac.toString());
 	// assertEquals("negative numerator", "-1/6", newFrac1.toString());
 	// assertEquals("negative denom", "-1/6", newFrac2.toString());
 	// assertEquals("negative numerator and denom", "1/6", newFrac3.toString());
 	// assertEquals("negative denom", "1/6", newFrac4.toString());
 	// assertEquals("from double test", "5/2", newFrac5.toString());
 	// }
 	//
 	// @Test
 	// public void testAdd() throws Exception {
 	// System.out.println(new Fraction(2, 3));
 	// // fail("Not yet implemented");
 	// }
 	//
 	// @Test
 	// public void testDoubleValue() {
 	// // fail("Not yet implemented");
 	// }
 	//
 	// @Test
 	// public void testZeroDenominator() {
 	// try {
 	// new Fraction(1, 0);
 	// fail("Zero denominator did not throw an exception");
 	// } catch (Exception e) {
 	//
 	// } // try/catch
 
 }
