 package com.codemelon.polynomial;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.codemelon.math.Complex;
 
 /**
  * @author Marshall Farrier
  * @my.created Sep 17, 2013
  * @my.edited Sep 17, 2013
  */
 public class ComplexPolynomialTest {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#ComplexPolynomial(java.util.List)}.
 	 */
 	@Test
 	public void testComplexPolynomial() {
 		LinkedList<Complex> input = new LinkedList<Complex>();
 		input.add(Complex.ZERO);
 		input.add(Complex.ONE);
 		input.add(Complex.ZERO);
 		ComplexPolynomial p = new ComplexPolynomial(input);
 		assertEquals("Polynomial has degree 1", p.degree(), 1);
 		assertTrue("coefficient 0 is 0.0", p.coefficient(0).equalWithinDelta(Complex.ZERO));
 		assertTrue("coefficient 1 is 1.0", p.coefficient(1).equalWithinDelta(Complex.ONE));
 		assertTrue("coefficient 20 is 0.0", p.coefficient(20).equalWithinDelta(Complex.ZERO));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#fromDegree(int)}.
 	 */
 	@Test
 	public void testFromDegree() {
 		ComplexPolynomial p1 = ComplexPolynomial.fromDegree(-1);
 		ComplexPolynomial p2 = ComplexPolynomial.fromDegree(0);
 		ComplexPolynomial p3 = ComplexPolynomial.fromDegree(8);
 		assertEquals("Zero polynomial has correct degree", p1.degree(), -1);
 		assertEquals("Constant polynomial has correct degree", p2.degree(), 0);
 		assertEquals("Larger polynomial has correct degree", p3.degree(), 8);
 		assertTrue("Zero polynomial has constant coefficient 0.0", 
 				p1.coefficient(0).equalWithinDelta(Complex.ZERO));
 		assertTrue("Constant polynomial has constant coefficient 1.0", 
 				p2.coefficient(0).equalWithinDelta(Complex.ONE));
 		assertTrue("Larger polynomial has constant coefficient 1.0", 
 				p3.coefficient(0).equalWithinDelta(Complex.ONE));
 		assertTrue("Zero polynomial has 0.0 as coefficient 8", 
 				p1.coefficient(8).equalWithinDelta(Complex.ZERO));
 		assertTrue("Constant polynomial has 0.0 as coefficient 8", 
 				p2.coefficient(8).equalWithinDelta(Complex.ZERO));
 		assertTrue("Larger polynomial has 1.0 as coefficient 8", 
 				p3.coefficient(8).equalWithinDelta(Complex.ONE));
 		assertTrue("Larger polynomial has 0.0 as coefficient 8", 
 				p3.coefficient(9).equalWithinDelta(Complex.ZERO));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#plus(com.codemelon.polynomial.ComplexPolynomial)}.
 	 */
 	@Test
 	public void testPlus() {
 		Complex[] coefficients = { new Complex(2.0, 2.0), new Complex(1.0, 1.0) };
 		ComplexPolynomial p1 = new ComplexPolynomial(Arrays.asList(coefficients));
 		ComplexPolynomial p2 = ComplexPolynomial.fromDegree(2);
 		ComplexPolynomial result = p1.plus(p2);
 		Complex[] resultCoefficients = { new Complex(3.0, 2.0), new Complex(2.0, 1.0), 
 				Complex.ONE };
 		assertEquals("p1 plus p2 correct degree", result.degree(), 2);
 		assertTrue("p1 plus p2 correct constant coefficient", 
 				result.coefficient(0).equalWithinDelta(resultCoefficients[0]));
 		assertTrue("p1 plus p2 correct coefficient 1", 
 				result.coefficient(1).equalWithinDelta(resultCoefficients[1]));
 		assertTrue("p1 plus p2 correct coefficient 2", 
 				result.coefficient(2).equalWithinDelta(resultCoefficients[2]));
 		assertTrue("p1 plus p2 correct coefficient 3", 
 				result.coefficient(3).equalWithinDelta(Complex.ZERO));
 		result = p2.plus(p1);
 		assertTrue("p2 plus p1 correct constant coefficient", 
 				result.coefficient(0).equalWithinDelta(resultCoefficients[0]));
 		assertTrue("p2 plus p1 correct coefficient 1", 
 				result.coefficient(1).equalWithinDelta(resultCoefficients[1]));
 		assertTrue("p2 plus p1 correct coefficient 2", 
 				result.coefficient(2).equalWithinDelta(resultCoefficients[2]));
 		assertTrue("p2 plus p1 correct coefficient 3",
 				result.coefficient(3).equalWithinDelta(Complex.ZERO));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#minus(com.codemelon.polynomial.ComplexPolynomial)}.
 	 */
 	@Test
 	public void testMinus() {
 		Complex[] coefficients = { new Complex(2.0, 2.0), new Complex(1.0, 1.0) };
 		ComplexPolynomial p1 = new ComplexPolynomial(Arrays.asList(coefficients));
 		ComplexPolynomial p2 = ComplexPolynomial.fromDegree(2);
 		ComplexPolynomial result = p1.minus(p2);
 		Complex[] resultCoefficients = { new Complex(1.0, 2.0), new Complex(0.0, 1.0), 
 				new Complex(-1.0, 0.0), new Complex(-1.0, -2.0), new Complex(0.0, -1.0),
 				Complex.ONE};
 		assertEquals("p1 plus p2 correct degree", result.degree(), 2);
 		assertTrue("p1 plus p2 correct constant coefficient", 
 				result.coefficient(0).equalWithinDelta(resultCoefficients[0]));
 		assertTrue("p1 plus p2 correct coefficient 1", 
 				result.coefficient(1).equalWithinDelta(resultCoefficients[1]));
 		assertTrue("p1 plus p2 correct coefficient 2", 
 				result.coefficient(2).equalWithinDelta(resultCoefficients[2]));
 		assertTrue("p1 plus p2 correct coefficient 3", 
 				result.coefficient(3).equalWithinDelta(Complex.ZERO));
 		result = p2.minus(p1);
 		assertTrue("p2 plus p1 correct constant coefficient", 
 				result.coefficient(0).equalWithinDelta(resultCoefficients[3]));
 		assertTrue("p2 plus p1 correct coefficient 1", 
 				result.coefficient(1).equalWithinDelta(resultCoefficients[4]));
 		assertTrue("p2 plus p1 correct coefficient 2", 
 				result.coefficient(2).equalWithinDelta(resultCoefficients[5]));
 		assertTrue("p2 plus p1 correct coefficient 3",
 				result.coefficient(3).equalWithinDelta(Complex.ZERO));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#times(com.codemelon.polynomial.ComplexPolynomial)}.
 	 */
 	@Test
 	public void testTimesSchool() {
 		// real coefficients
 		Complex[] p1Coefficients = { new Complex(3.0, 0.0), new Complex(2.0, 0.0), 
 				Complex.ONE };
 		Complex[] p2Coefficients = { new Complex(2.0, 0.0), Complex.ONE };
 		ComplexPolynomial p1 = new ComplexPolynomial(Arrays.asList(p1Coefficients));
 		ComplexPolynomial p2 = new ComplexPolynomial(Arrays.asList(p2Coefficients));
 		ComplexPolynomial result = p1.times(p2);
 		assertTrue("p1 times p2 correct coefficient 0", result.coefficient(0).equalWithinDelta(new Complex(6.0, 0.0)));
 		assertTrue("p1 times p2 correct coefficient 1", result.coefficient(1).equalWithinDelta(new Complex(7.0, 0.0)));
 		assertTrue("p1 times p2 correct coefficient 2", result.coefficient(2).equalWithinDelta(new Complex(4.0, 0.0)));
 		assertTrue("p1 times p2 correct coefficient 3", result.coefficient(3).equalWithinDelta(new Complex(1.0, 0.0)));
 		assertTrue("p1 times p2 correct coefficient 4", result.coefficient(4).equalWithinDelta(new Complex(0.0, 0.0)));
 		result = p2.times(p1);
 		assertTrue("p2 times p1 correct coefficient 0", result.coefficient(0).equalWithinDelta(new Complex(6.0, 0.0)));
 		assertTrue("p2 times p1 correct coefficient 1", result.coefficient(1).equalWithinDelta(new Complex(7.0, 0.0)));
 		assertTrue("p2 times p1 correct coefficient 2", result.coefficient(2).equalWithinDelta(new Complex(4.0, 0.0)));
 		assertTrue("p2 times p1 correct coefficient 3", result.coefficient(3).equalWithinDelta(new Complex(1.0, 0.0)));
 		assertTrue("p2 times p1 correct coefficient 4", result.coefficient(4).equalWithinDelta(new Complex(0.0, 0.0)));
 		// complex coefficients
 		Complex[] p3Coefficients = { Complex.fromPolar(2.0, Math.PI / 8), 
 				Complex.fromPolar(1.0, 3.0 * Math.PI / 8) };
 		Complex[] p4Coefficients = { Complex.fromPolar(2.0, Math.PI / 8), 
 				Complex.fromPolar(1.0, 3.0 * Math.PI / 8).negative() };
 		ComplexPolynomial p3 = new ComplexPolynomial(Arrays.asList(p3Coefficients));
 		ComplexPolynomial p4 = new ComplexPolynomial(Arrays.asList(p4Coefficients));
 		result = p3.times(p4);
 		assertTrue("p3 times p4 correct coefficient 0", result.coefficient(0)
 				.equalWithinDelta(Complex.fromPolar(4.0, Math.PI / 4)));
 		assertTrue("p3 times p4 correct coefficient 1", result.coefficient(1).equalWithinDelta(Complex.ZERO));
 		assertTrue("p3 times p4 correct coefficient 2", result.coefficient(2)
 				.equalWithinDelta(Complex.fromPolar(1.0, 3.0 * Math.PI / 4).negative()));
 		assertTrue("p3 times p4 correct coefficient 3", result.coefficient(3).equalWithinDelta(Complex.ZERO));
 	}
 	
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#times(com.codemelon.polynomial.ComplexPolynomial)}.
 	 * This fails when the result degree is > 8. Some coefficients differ significantly
 	 * from the correct value, so it doesn't seem to be just rounding problems.
 	 */
 	@Test
 	public void testTimesFourier() {
 		Complex[] p1Coefficients = { Complex.ONE, Complex.ZERO, Complex.ZERO,
 				Complex.ZERO, Complex.ZERO, Complex.ONE };
 		Complex[] p2Coefficients = { Complex.ONE, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ZERO, Complex.ONE.negative() };
 		int resultDegree = 10;
 		ComplexPolynomial p1 = new ComplexPolynomial(p1Coefficients);
 		ComplexPolynomial p2 = new ComplexPolynomial(p2Coefficients);
 		ComplexPolynomial product = p1.times(p2);
		
 		assertEquals("correct degree", resultDegree, product.degree());
 		assertTrue("constant coefficient is 1", product.coefficient(0).equalWithinDelta(Complex.ONE));
 		for (int i = 1; i < resultDegree; i++) {
 			assertTrue("coefficient " + i + " is 0", product.coefficient(i).equalWithinDelta(Complex.ZERO));
 		}
 		assertTrue("coefficient " + resultDegree + " is -1", product.coefficient(resultDegree)
 				.equalWithinDelta(Complex.ONE.negative()));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.polynomial.ComplexPolynomial#evaluate(com.codemelon.math.Complex)}.
 	 */
 	@Test
 	public void testEvaluate() {
 		// Test real coefficients
 		Complex[] realCoefficients = { new Complex(3.0, 0.0), new Complex(2.0, 0.0), Complex.ONE};
 		ComplexPolynomial p = new ComplexPolynomial(Arrays.asList(realCoefficients));
 		Complex c = Complex.fromPolar(1.0, Math.PI / 4.0);
 		Complex answer = p.evaluate(c);
 		assertTrue("Correct answer using real coefficient polynomial", 
 				answer.equalWithinDelta(new Complex(3.0 + Math.sqrt(2.0), 1.0 + Math.sqrt(2.0))));
 		// Test complex coefficients
 		Complex[] complexCoefficients = { new Complex(1.0, 1.0), Complex.fromPolar(1.0, 3 * Math.PI / 4.0),
 				Complex.ZERO, Complex.fromPolar(1.0, Math.PI / 4.0) };
 		p = new ComplexPolynomial(Arrays.asList(complexCoefficients));
 		answer = p.evaluate(c);
 		assertTrue("Correct answer using complex coefficient polynomial", 
 				answer.equalWithinDelta(new Complex(-1.0, 1.0)));
 	}
 
 	@Test
 	public void testRecursiveFFT() {
 		// Test case from CLRS, p. 914
 		Complex[] coefficients = { Complex.ZERO, Complex.ONE, new Complex(2.0, 0.0), 
 				new Complex(3.0, 0.0) };
 		Complex[] values = ComplexPolynomial.recursiveFFT(coefficients);
 		assertTrue("Correct value for angle 0", values[0].equalWithinDelta(new Complex(6.0, 0.0)));
 		assertTrue("Correct value for angle PI / 2", values[1].equalWithinDelta(new Complex(-2.0, -2.0)));
 		assertTrue("Correct value for angle PI", values[2].equalWithinDelta(new Complex(-2.0, 0.0)));
 		assertTrue("Correct value for angle 3 * PI / 2", values[3].equalWithinDelta(new Complex(-2.0, 2.0)));
 		// Test case for 1 + x^5
 		Complex[] c2 = { Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO };
 		values = ComplexPolynomial.recursiveFFT(c2);
 		double theta = Math.PI / 8.0;
 		double sinTheta = Math.sin(theta);
 		double cosTheta = Math.cos(theta);
 		double oneOverSqrtTwo = 1.0 / Math.sqrt(2.0);
 		Complex[] expectedValues2 = { 
 				new Complex(2.0, 0.0),	// 0
 				new Complex(1.0 - sinTheta, cosTheta), // 1
 				new Complex(1.0 - oneOverSqrtTwo, -oneOverSqrtTwo), // 2
 				new Complex(1.0 + cosTheta, -sinTheta), // 3
 				new Complex(1.0, 1.0), // 4
 				new Complex(1.0 - cosTheta, -sinTheta), // 5
 				new Complex(1.0 + oneOverSqrtTwo, -oneOverSqrtTwo),	//6
 				new Complex(1.0 + sinTheta, cosTheta),	//7
 				Complex.ZERO,	//8
 				new Complex(1.0 + sinTheta, -cosTheta),	//9
 				new Complex(1.0 + oneOverSqrtTwo, oneOverSqrtTwo),	//10
 				new Complex(1.0 - cosTheta, sinTheta),	//11
 				new Complex(1.0, -1.0),	//12
 				new Complex(1.0 + cosTheta, sinTheta),	//13
 				new Complex(1.0 - oneOverSqrtTwo, oneOverSqrtTwo),	//14
 				new Complex(1.0 - sinTheta, -cosTheta)	// 15
 		};
 		for (int i = 0; i < expectedValues2.length; i++) {
 			assertTrue("Case 2: Correct value for angle " + i + "*PI/8", 
 					values[i].equalWithinDelta(expectedValues2[i]));
 		}
 	}
 
 	@Test
 	public void testRecursiveFFTInverse() {
 		// reverse of test cases for recursiveFFT
 		Complex[] values = { new Complex(6.0, 0.0), new Complex(-2.0, -2.0), new Complex(-2.0, 0.0), 
 				new Complex(-2.0, 2.0) };
 		Complex[] coefficients = ComplexPolynomial.recursiveFFTInverse(values);
 		assertTrue("Correct coefficient 0", coefficients[0].equalWithinDelta(Complex.ZERO));
 		assertTrue("Correct coefficient 1", coefficients[1].equalWithinDelta(Complex.ONE));
 		assertTrue("Correct coefficient 2", coefficients[2].equalWithinDelta(new Complex(2.0, 0.0)));
 		assertTrue("Correct coefficient 3", coefficients[3].equalWithinDelta(new Complex(3.0, 0.0)));
 		// Test case for 1 + x^5
 		Complex[] expected2 = { Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, 
 				Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO };
 		double theta = Math.PI / 8.0;
 		double sinTheta = Math.sin(theta);
 		double cosTheta = Math.cos(theta);
 		double oneOverSqrtTwo = 1.0 / Math.sqrt(2.0);
 		Complex[] values2 = { 
 				new Complex(2.0, 0.0),	// 0
 				new Complex(1.0 - sinTheta, cosTheta), // 1
 				new Complex(1.0 - oneOverSqrtTwo, -oneOverSqrtTwo), // 2
 				new Complex(1.0 + cosTheta, -sinTheta), // 3
 				new Complex(1.0, 1.0), // 4
 				new Complex(1.0 - cosTheta, -sinTheta), // 5
 				new Complex(1.0 + oneOverSqrtTwo, -oneOverSqrtTwo),	//6
 				new Complex(1.0 + sinTheta, cosTheta),	//7
 				Complex.ZERO,	//8
 				new Complex(1.0 + sinTheta, -cosTheta),	//9
 				new Complex(1.0 + oneOverSqrtTwo, oneOverSqrtTwo),	//10
 				new Complex(1.0 - cosTheta, sinTheta),	//11
 				new Complex(1.0, -1.0),	//12
 				new Complex(1.0 + cosTheta, sinTheta),	//13
 				new Complex(1.0 - oneOverSqrtTwo, oneOverSqrtTwo),	//14
 				new Complex(1.0 - sinTheta, -cosTheta)	// 15
 		};
 		coefficients = ComplexPolynomial.recursiveFFTInverse(values2);
 		for (int i = 0; i < expected2.length; i++) {
 			assertTrue("Case 2: Correct coefficient " + i, 
 					coefficients[i].equalWithinDelta(expected2[i]));
 		}
 	}
 }
