 package org.paulg.simplegraph.interpreter;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class FunctionTest {
 
 	private static final double EPSILON = 0.000000001;
 
 	@Test
 	public void testOneOperatorExpressions() {
 		double x = 2;
 		assertEquals(2 * x, f("2 * x ", x), EPSILON);
 		assertEquals(2 + x, f("2 + x ", x), EPSILON);
 		assertEquals(2 - x, f("2 - x ", x), EPSILON);
 		assertEquals(2 / x, f("2 / x ", x), EPSILON);
 	}
 
 	@Test
 	public void testExpressionsWithBrackets() {
 		double x = 2;
 		assertEquals(x, f("(x)", x), EPSILON);
 		assertEquals(2 * x, f("(2 * x)", x), EPSILON);
 		assertEquals(3 * (2 + x), f("3 * (2 + x)", x), EPSILON);
 		assertEquals(3 + (2 * x), f("3 + (2 * x)", x), EPSILON);
 	}
	
 	@Test
 	public void testOperatorBinding() {
 		double x = 2;
 		assertEquals(2 * x + 1, f("2 * x + 1", x), EPSILON);
 	}
 
 	private double f(String formula, double x) {
 		Function f = new Function(formula);
 		double res = f.evaluate(x);
 		return res;
 	}
 
 }
