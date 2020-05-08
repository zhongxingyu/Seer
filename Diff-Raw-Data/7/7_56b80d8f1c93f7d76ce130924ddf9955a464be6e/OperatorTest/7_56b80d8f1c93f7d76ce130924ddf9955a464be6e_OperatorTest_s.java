 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.fitting.functions;
 
 import static org.junit.Assert.*;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Random;
 
 public class OperatorTest {
 
 	private static final double ABS_TOL = 1e-7;
 
 	@Test
 	public void testAdd() {
 		Add op = new Add();
 
 		IFunction fa = new Cubic();
 		fa.setParameterValues(23., -10., 1.2, -5.2);
 
 		IFunction fb = new StraightLine();
 		fb.setParameterValues(4.2, -7.5);
 
 		op.addFunction(fa);
 		op.addFunction(fb);
 
 		Assert.assertEquals(6, op.getNoOfParameters());
 
 		Assert.assertArrayEquals(new double[] {23., -10., 1.2, -5.2, 4.2, -7.5}, op.getParameterValues(), ABS_TOL);
 		Assert.assertEquals(-23. - 10. - 1.2 - 5.2 - 4.2 - 7.5, op.val(-1), ABS_TOL);
 
 		DoubleDataset xd = new DoubleDataset(new double[] {-1, 0, 2});
 		DoubleDataset dx;
 
 		dx = op.calculateValues(xd);
 		Assert.assertArrayEquals(new double[] {-23. - 10. - 1.2 - 5.2 - 4.2 - 7.5, -5.2 - 7.5,
 				23.*8 - 10.*4 + 1.2*2 - 5.2 + 4.2*2 - 7.5}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(0), xd);
 		Assert.assertArrayEquals(new double[] {-1, 0, 8}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(1), xd);
 		Assert.assertArrayEquals(new double[] {1, 0, 4}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(2), xd);
 		Assert.assertArrayEquals(new double[] {-1, 0, 2}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(3), xd);
 		Assert.assertArrayEquals(new double[] {1, 1, 1}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(4), xd);
 		Assert.assertArrayEquals(new double[] {-1, 0, 2}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(5), xd);
 		Assert.assertArrayEquals(new double[] {1, 1, 1}, dx.getData(), ABS_TOL);
 
 		DoubleDataset[] coords = new DoubleDataset[] {DoubleDataset.arange(15, 30, 0.25)};
 		DoubleDataset weight = null;
 		CoordinatesIterator it = op.getIterator(coords);
 		DoubleDataset current = new DoubleDataset(it.getShape());
 		DoubleDataset data = Random.randn(it.getShape());
 		op.fillWithValues(current, it);
 		double rd = data.residual(current, weight, false);
 		double rf = op.residual(true, data, weight, coords);
 		Assert.assertEquals(rd, rf, 1e-9);
 	}
 
 	@Test
 	public void testMultiply() {
 		Multiply op = new Multiply();
 
 		IFunction fa = new Cubic();
 		fa.setParameterValues(23., -10., 1.2, -5.2);
 
 		IFunction fb = new StraightLine();
 		fb.setParameterValues(4.2, -7.5);
 
 		op.addFunction(fa);
 		op.addFunction(fb);
 
 		Assert.assertEquals(6, op.getNoOfParameters());
 
 		Assert.assertArrayEquals(new double[] {23., -10., 1.2, -5.2, 4.2, -7.5}, op.getParameterValues(), ABS_TOL);
 		Assert.assertEquals((-23. - 10. - 1.2 - 5.2) * (- 4.2 - 7.5), op.val(-1), ABS_TOL);
 
 		DoubleDataset xd = new DoubleDataset(new double[] {-1, 0, 2});
 		DoubleDataset dx;
 
 		dx = op.calculateValues(xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2) * (- 4.2 - 7.5), -5.2 * - 7.5,
 				(23.*8 - 10.*4 + 1.2*2 - 5.2) * (4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(0), xd);
 		Assert.assertArrayEquals(new double[] {-(-4.2 - 7.5), 0, 8*(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(1), xd);
 		Assert.assertArrayEquals(new double[] {(-4.2 - 7.5), 0, 4*(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(2), xd);
 		Assert.assertArrayEquals(new double[] {-(-4.2 - 7.5), 0, 2*(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(3), xd);
 		Assert.assertArrayEquals(new double[] {-4.2 - 7.5, -7.5, 4.2*2 - 7.5}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(4), xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2) * -1, 0,
 				(23.*8 - 10.*4 + 1.2*2 - 5.2) * 2}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(5), xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2), -5.2,
 				(23.*8 - 10.*4 + 1.2*2 - 5.2)}, dx.getData(), ABS_TOL);
 	}
 
 	@Test
 	public void testSubtract() {
 		Subtract op = new Subtract();
 
 		IFunction fa = new Cubic();
 		fa.setParameterValues(23., -10., 1.2, -5.2);
 
 		IFunction fb = new StraightLine();
 		fb.setParameterValues(-4.2, 7.5);
 
 		op.addFunction(fa);
 		op.addFunction(fb);
 
 		Assert.assertEquals(6, op.getNoOfParameters());
 
 		Assert.assertArrayEquals(new double[] {23., -10., 1.2, -5.2, -4.2, 7.5}, op.getParameterValues(), ABS_TOL);
 		Assert.assertEquals(-23. - 10. - 1.2 - 5.2 - 4.2 - 7.5, op.val(-1), ABS_TOL);
 
 		DoubleDataset xd = new DoubleDataset(new double[] {-1, 0, 2});
 		DoubleDataset dx;
 
 		dx = op.calculateValues(xd);
 		Assert.assertArrayEquals(new double[] {-23. - 10. - 1.2 - 5.2 - 4.2 - 7.5, -5.2 - 7.5,
 				23.*8 - 10.*4 + 1.2*2 - 5.2 + 4.2*2 - 7.5}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(0), xd);
 		Assert.assertArrayEquals(new double[] {-1, 0, 8}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(1), xd);
 		Assert.assertArrayEquals(new double[] {1, 0, 4}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(2), xd);
 		Assert.assertArrayEquals(new double[] {-1, 0, 2}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(3), xd);
 		Assert.assertArrayEquals(new double[] {1, 1, 1}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(4), xd);
 		Assert.assertArrayEquals(new double[] {1, 0, -2}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(5), xd);
 		Assert.assertArrayEquals(new double[] {-1, -1, -1}, dx.getData(), ABS_TOL);
 	}
 
 	@Test
 	public void testDivide() {
 		Divide op = new Divide();
 
 		IFunction fa = new Cubic();
 		fa.setParameterValues(23., -10., 1.2, -5.2);
 
 		IFunction fb = new StraightLine();
 		fb.setParameterValues(4.2, -7.5);
 
 		op.addFunction(fa);
 		op.addFunction(fb);
 
 		Assert.assertEquals(6, op.getNoOfParameters());
 
 		Assert.assertArrayEquals(new double[] {23., -10., 1.2, -5.2, 4.2, -7.5}, op.getParameterValues(), ABS_TOL);
 		Assert.assertEquals((-23. - 10. - 1.2 - 5.2) / (- 4.2 - 7.5), op.val(-1), ABS_TOL);
 
 		DoubleDataset xd = new DoubleDataset(new double[] {-1, 0, 2});
 		DoubleDataset dx;
 
 		dx = op.calculateValues(xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2) / (-4.2 - 7.5), -5.2 / - 7.5,
 				(23.*8 - 10.*4 + 1.2*2 - 5.2) / (4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(0), xd);
 		Assert.assertArrayEquals(new double[] {-1/(-4.2 - 7.5), 0, 8/(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(1), xd);
 		Assert.assertArrayEquals(new double[] {1/(-4.2 - 7.5), 0, 4/(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(2), xd);
 		Assert.assertArrayEquals(new double[] {-1/(-4.2 - 7.5), 0, 2/(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(3), xd);
 		Assert.assertArrayEquals(new double[] {1/(-4.2 - 7.5), 1/-7.5, 1/(4.2*2 - 7.5)}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(4), xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2) / ((-4.2 - 7.5)*(-4.2 - 7.5)), 0,
 				(23.*8 - 10.*4 + 1.2*2 - 5.2) * -2 / ((4.2*2 - 7.5)*(4.2*2 - 7.5))}, dx.getData(), ABS_TOL);
 
 		dx = op.calculatePartialDerivativeValues(op.getParameter(5), xd);
 		Assert.assertArrayEquals(new double[] {(-23. - 10. - 1.2 - 5.2) * -1 / ((-4.2 - 7.5)*(-4.2 - 7.5)),
 				-5.2 * -1 / (7.5 * 7.5), (23.*8 - 10.*4 + 1.2*2 - 5.2) * -1 / ((4.2*2 - 7.5)*(4.2*2 - 7.5)) }, dx.getData(), ABS_TOL);
 	}
 
 	@Test
 	public void testBinaryOperators() {
 		IFunction fa = new Cubic();
 		fa.setParameterValues(23., -10., 1.2, -5.2);
 
 		IFunction fb = new StraightLine();
 		fb.setParameterValues(-4.2, 7.5);
 
 		IFunction fc = new Offset();
 		fc.setParameterValues(42.);
 
 		IOperator op = new Subtract();
 		op.addFunction(fa);
 		op.addFunction(fb);
 		try {
 			op.addFunction(fc);
 			Assert.fail("Should have thrown exception");
 		} catch (Exception e) {
 			// do nothing
 		}
 
 		op = new Divide();
 		op.addFunction(fa);
 		op.addFunction(fb);
 		try {
 			op.addFunction(fc);
 			Assert.fail("Should have thrown exception");
 		} catch (Exception e) {
 			// do nothing
 		}
 	}
 
 	@Test
 	public void testConvolve() {
 		double w = 110 * Math.log(2)*FermiGauss.K2EV_CONVERSION_FACTOR;
 
 		DoubleDataset xd = new DoubleDataset(new double[] {23. - w, 23, 23. + 2 * w});
 
 		AFunction f = new Fermi();
 		f.setParameterValues(23., 110*FermiGauss.K2EV_CONVERSION_FACTOR, 1, 0);
 		AFunction g = new Gaussian();
 		g.setParameterValues((double) xd.mean(), 1., 1.);
 
 		Convolve cfg = new Convolve();
 		cfg.addFunction(f);
 		cfg.addFunction(g);
 
 		AFunction fg = new FermiGauss();
 		fg.setParameterValues(23., 110., 0, 1, 0, 1);
 
		Assert.assertEquals(6, cfg.getNoOfParameters());
		double[] cps = cfg.getParameterValues();
 		cps[1] /= FermiGauss.K2EV_CONVERSION_FACTOR;
 		cps[3] = cps[2];
 		cps[2] = 0;
 		cps[4] = 0;
 		double[] ps = fg.getParameterValues();
 		Assert.assertArrayEquals(cps, ps, ABS_TOL);
 
 		DoubleDataset fgx = fg.calculateValues(xd);
 		DoubleDataset cfgx = cfg.calculateValues(xd);
 		Assert.assertArrayEquals(cfgx.getData(), fgx.getData(), 200*ABS_TOL);
 	}
 
 	@Test
 	public void testToString() {
 		// make sure empty CompositeFunction does not throw exception
 		// this test is not concerned with the contents of toString, just
 		// that there is no exception.
 		final String operatorText = "Operator has no functions";
 		CompositeFunction compositeFunction = new CompositeFunction();
 		Assert.assertTrue(compositeFunction.toString().startsWith(operatorText));
 
 		Subtract subFunction = new Subtract();
 		Assert.assertTrue(compositeFunction.toString().startsWith(operatorText));
 
 		subFunction.setFunction(0, new Gaussian());
 		Assert.assertFalse(subFunction.toString().startsWith(operatorText));
 
 		subFunction.removeFunction(0);
 		Assert.assertTrue(subFunction.toString().startsWith(operatorText));
 
 		subFunction.setFunction(1, new Gaussian());
 		Assert.assertFalse(subFunction.toString().startsWith(operatorText));
 
 		subFunction.removeFunction(1);
 		Assert.assertTrue(subFunction.toString().startsWith(operatorText));
 	}
 
 	@Test
 	public void testCopy() throws Exception {
 		CompositeFunction compositeFunction = new CompositeFunction();
 		compositeFunction.addFunction(new Gaussian());
 		CompositeFunction copy = compositeFunction.copy();
 		assertEquals(1, copy.getFunctions().length);
 		assertTrue(copy.getFunction(0) instanceof Gaussian);
 
 		Add addOperator = new Add();
 		addOperator.addFunction(new Gaussian());
 		Add addCopy = (Add)addOperator.copy();
 		assertEquals(1, addCopy.getFunctions().length);
 		assertTrue(addCopy.getFunction(0) instanceof Gaussian);
 	}
 
 	public static class MockGaussianLikeThing extends Gaussian {
 		public MockGaussianLikeThing() {
 			name = "MockGaussianLikeThing";
 		}
 	}
 
 	@Test
 	public void testEquals() {
 		// Test that two function trees with different child functions
 		// but the same parameters do not compare to equals
 		CompositeFunction expected = new CompositeFunction();
 		expected.addFunction(new Gaussian());
 		CompositeFunction actual = new CompositeFunction();
 		actual.addFunction(new MockGaussianLikeThing());
 		assertTrue(!expected.equals(actual));
 	}
 
 	@Test
 	public void testRemoveFunction() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		assertEquals(0, add.getParameters().length);
 		assertEquals(0, composite.getParameters().length);
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 		add.addFunction(gaussian);
 		composite.addFunction(add);
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 		add.removeFunction(0);
 		assertEquals(0, add.getParameters().length);
 		assertEquals(0, composite.getParameters().length);
 	}
 
 	@Test
 	public void testAddFunctionOrder_BottomUp() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 
 		add.addFunction(gaussian);
 		composite.addFunction(add);
 
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 	}
 
 	@Test
 	public void testAddFunctionOrder_TopDown() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 
 		composite.addFunction(add);
 		add.addFunction(gaussian);
 
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 	}
 
 	@Test
 	public void testRemoveFunction_WithWorkaround() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 		add.addFunction(gaussian);
 		composite.addFunction(add);
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 		add.removeFunction(0);
 		assertEquals(0, add.getParameters().length);
 		assertEquals(0, composite.getParameters().length);
 	}
 
 	@Test
 	public void testAddFunctionOrder_BottomUp_WithWorkaround() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 
 		add.addFunction(gaussian);
 		composite.addFunction(add);
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 	}
 
 	@Test
 	public void testAddFunctionOrder_TopDown_WithWorkaround() {
 		CompositeFunction composite = new CompositeFunction();
 		Add add = new Add();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 
 		composite.addFunction(add);
 		add.addFunction(gaussian);
 		assertEquals(3, add.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 	}
 
 	@Test
 	public void testUpdateParametersWorkaround() {
 		CompositeFunction composite = new CompositeFunction();
 		Subtract subtract = new Subtract();
 		Gaussian gaussian = new Gaussian(1, 2, 3);
 
 		composite.addFunction(subtract);
 		subtract.setFunction(1, gaussian);
 		assertEquals(3, gaussian.getParameters().length);
 		assertEquals(3, subtract.getParameters().length);
 		assertEquals(3, composite.getParameters().length);
 	}
 
 	private static final class TestGenericBinaryOperator extends ABinaryOperator {
 		@Override
 		public double val(double... values) {
 			return 0;
 		}
 
 		@Override
 		public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
 		}
 	}
 
 	@Test
 	public void testSetFunction() {
 		IOperator myOp = new TestGenericBinaryOperator();
 
 		myOp.setFunction(1, new Gaussian());
 		assertEquals(1, myOp.getNoOfFunctions());
 		assertEquals(2, myOp.getFunctions().length);
 		assertEquals(new Gaussian(), myOp.getFunction(1));
 		assertEquals(3, myOp.getNoOfParameters());
 	}
 
 	@Test
 	public void testNoOfFunctions() {
 		IOperator myOp = new TestGenericBinaryOperator();
 
 		myOp.addFunction(new Gaussian());
 		myOp.addFunction(new Gaussian());
 		assertEquals(2, myOp.getNoOfFunctions());
 		myOp.setFunction(0, null); // functions can be set to null(!)
 		assertEquals(1, myOp.getNoOfFunctions());
 		myOp.addFunction(new Gaussian());
 		assertEquals(2, myOp.getNoOfFunctions());
 		assertNotNull(myOp.getFunction(0));
 		assertNotNull(myOp.getFunction(1));
 	}
 
 	@Test
 	public void testUpdateParametersWithEqualParameters() {
 		Add add = new Add();
 		assertEquals(0, add.getNoOfParameters());
 		add.addFunction(new Gaussian());
 		assertEquals(3, add.getNoOfParameters());
 		add.addFunction(new Gaussian());
 		assertEquals(6, add.getNoOfParameters());
 	}
 
 	@Test
 	public void testIsValid() {
 		// invalid because binary operator requires 2 functions
 		assertFalse(new TestGenericBinaryOperator().isValid());
 
 		CompositeFunction compositeFunction = new CompositeFunction();
 		TestGenericBinaryOperator binaryOperator = new TestGenericBinaryOperator();
 		compositeFunction.addFunction(binaryOperator);
 		// make sure that the invalid is propagated up
 		assertFalse(compositeFunction.isValid());
 
 		// correct the invalidity and make sure it is valid and propagated
 		binaryOperator.addFunction(new Gaussian());
 		binaryOperator.addFunction(new Gaussian());
 		assertTrue(binaryOperator.isValid());
 		assertTrue(compositeFunction.isValid());
 	}
 }
