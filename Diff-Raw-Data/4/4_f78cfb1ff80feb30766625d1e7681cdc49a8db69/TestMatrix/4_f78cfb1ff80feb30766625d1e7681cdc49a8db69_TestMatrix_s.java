 package org.wterliko.javanum;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.wterliko.javanum.test.NumericTestUtils.assertMartixEquals;
 import static org.wterliko.javanum.test.NumericTestUtils.assertValueClose;
 import static org.wterliko.javanum.test.NumericTestUtils.assertVectorEquals;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.wterliko.javanum.Matrix.LUDecomposition;
 
 public class TestMatrix {
 
 	private Matrix subject;
 
 	@Before
 	public void before() {
 		subject = new Matrix();
 	}
 
 	@Test
 	public void testMultiplicaiton1x1() throws Exception {
 		double[][] left = new double[][] { { 7.0 } };
 		double[][] right = new double[][] { { 8.0 } };
 		double[][] result = subject.multiply(left, right);
 		assertMartixEquals(new double[][] { { 56 } }, result);
 	}
 
 	@Test
 	public void testMultiplicaiton2x3() throws Exception {
 		double[][] left = new double[][] { { 1, 0, 2 }, { -1, 3, 1 } };
 		double[][] right = new double[][] { { 3, 1 }, { 2, 1 }, { 1, 0 } };
 		double[][] result = subject.multiply(left, right);
 		assertMartixEquals(new double[][] { { 5, 1 }, { 4, 2 } }, result);
 	}
 
 	@Test(expected = AssertionError.class)
 	public void testMultiplicationInvalidDimensions() throws Exception {
 		double[][] left = new double[][] { { 1, 0, 2 }, { -1, 3, 1 },
 				{ 2, 5, 5 } };
 		double[][] right = new double[][] { { 3, 1 }, { 2, 1 }, { 1, 0 } };
 		subject.multiply(left, right);
 		fail();
 	}
 
 	@Test
 	public void testLUDecomposition() throws Exception {
 		double[][] source = new double[][] { { 1, 2, 2 }, { 2, 1, 2 },
 				{ 2, 2, 1 } };
 		LUDecomposition decompisition = subject.lu(source);
 		System.out.println(subject.toString(decompisition.getMatrixL()));
 		System.out.println(subject.toString(decompisition.getMatrixU()));
 
 		double[][] expectedL = new double[][] { { 1, 0, 0 }, { 2, 1, 0 },
 				{ 2, 2.0 / 3.0, 1 } };
 		double[][] expectedU = new double[][] { { 1, 2, 2 }, { 0, -3, -2 },
 				{ 0, 0, -5.0 / 3.0 } };
 		System.out.println(subject.toString(expectedL));
 		System.out.println(subject.toString(expectedU));
 
 		assertMartixEquals(expectedL, decompisition.getMatrixL());
 		assertMartixEquals(expectedU, decompisition.getMatrixU());
 	}
 
 	@Test
 	public void testLUDecompositionInvert() throws Exception {
 		double[][] source = new double[][] { { 5, 3, 2 }, { 1, 2, 0 },
 				{ 3, 0, 4 } };
 		LUDecomposition result = subject.lu(source);
 		double[][] actual = subject.multiply(result.getMatrixL(),
 				result.getMatrixU());
 
 		System.out.println(subject.toString(source));
 		System.out.println(subject.toString(result.getMatrixL()));
 		System.out.println(subject.toString(result.getMatrixU()));
 
 		System.out.println(subject.toString(actual));
 		assertMartixEquals(source, actual);
 	}
 
 	@Test
 	public void testDetTriangle() throws Exception {
 		double[][] matrix = { { 1, 0, 0 }, { 5, 7, 0 }, { 2, 5, 7 } };
 		System.out.println(subject.toString(matrix));
 		assertValueClose(49.0, subject.detTriangle(matrix));
 	}
 
 	@Test
 	public void testDet() throws Exception {
 		double[][] matrix = { { 5, 3, 2 }, { 1, 2, 0 }, { 3, 0, 4 } };
 		System.out.println(subject.toString(matrix));
 		assertValueClose(16, subject.det(matrix));
 	}
 
 	@Test
 	public void testSolve() throws Exception {
 		double[][] matrix = { { 5, 3, 2 }, { 1, 2, 0 }, { 3, 0, 4 } };
 		double[] b = { 10, 5, -2 };
 		double[] result = subject.solve(matrix, b);
 		System.out.println(subject.toString(matrix));
 		System.out.println(subject.toString(b));
 		assertVectorEquals(
 				new double[] { 7.0 / 4.0, 13.0 / 8.0, -29.0 / 16.0 }, result);
 	}
 
 	@Test
 	public void testTransposeSimple() throws Exception {
 		double[][] a = new double[][] { { 2, 3, 1, 4 }, { -1, 2, 0, 1 },
 				{ 2, 2, 0, 1 } };
 		double[][] transposed = subject.transpose(a);
 		double[][] expetced = new double[][] { { 2, -1, 2 }, { 3, 2, 2 },
 				{ 1, 0, 0 }, { 4, 1, 1 } };
 		assertMartixEquals(expetced, transposed);
 	}
 
 	@Test
 	public void testTranspose2x() throws Exception {
 		double[][] a = new double[][] { { 2, 3, 1, 4 }, { -1, 2, 0, 1 },
 				{ 2, 2, 0, 1 } };
 		double[][] transposed1 = subject.transpose(a);
 		double[][] transposed2 = subject.transpose(transposed1);
 		assertMartixEquals(a, transposed2);
 	}
 
 	@Test
 	public void testcheckSymetry() throws Exception {
 		double[][] symetric = new double[][] { { 1, 2, 3 }, { 2, 5, 4 },
 				{ 3, 4, 7 } };
 		double[][] asymetric = new double[][] { { 1, 2, 3 }, { 2, 5, 4 },
 				{ 6, 4, 7 } };
 		double[][] notSquare = new double[][] { { 2, 3, 1, 4 },
 				{ -1, 2, 0, 1 }, { 2, 2, 0, 1 } };
 		assertTrue(subject.checkSymetry(symetric));
 		assertFalse(subject.checkSymetry(asymetric));
 		assertFalse(subject.checkSymetry(notSquare));
 	}
 
 	@Test
 	public void testCholeski() throws Exception {
 		double[][] matrix = new double[][] { { 1, 1, 1 }, { 1, 5, 5 },
 				{ 1, 5, 14 } };
 		System.out.println(subject.toString(matrix));
 		LUDecomposition choleski = subject.choleski(matrix);
 		double[][] expectedL = new double[][] { { 1, 0, 0 }, { 1, 2, 0 },
 				{ 1, 2, 3 } };
 		double[][] expectedU = new double[][] { { 1, 1, 1 }, { 0, 2, 2 },
 				{ 0, 0, 3 } };
 		assertMartixEquals(expectedL, choleski.getMatrixL());
 		assertMartixEquals(expectedU, choleski.getMatrixU());
 	}
 
 	@Test
 	public void testCholeskiTransposition() throws Exception {
		double[][] matrix = new double[][] { { 5, 3, 1 }, { 3, 2, 7 },
				{ 1, 7, 8 } };
 		System.out.println(subject.toString(matrix));
 		LUDecomposition choleski = subject.choleski(matrix);
 		double[][] transposedL = subject.transpose(choleski.getMatrixL());
 		assertMartixEquals(choleski.getMatrixU(), transposedL);
 	}
 
 	@Test
 	public void testCholeskiMultiplication() throws Exception {
 		double[][] matrix = new double[][] { { 1, 5, 9 }, { 5, 34, 54 },
 				{ 9, 54, 91 } };
 		System.out.println(subject.toString(matrix));
 		LUDecomposition choleski = subject.choleski(matrix);
 		double[][] actual = subject.multiply(choleski.getMatrixL(),
 				choleski.getMatrixU());
 		System.out.println(subject.toString(matrix));
 		System.out.println(subject.toString(choleski.getMatrixL()));
 		System.out.println(subject.toString(choleski.getMatrixU()));
 		System.out.println(subject.toString(actual));
 		assertMartixEquals(matrix, actual);
 	}
 
 	@Test
 	public void testPerformance() throws Exception {
 		List<double[][]> matrices = new ArrayList<double[][]>();
 		for (int i = 0; i < 100; i++) {
 			matrices.add(subject.randomMatrix(2, 2));
 		}
 		List<double[]> vectors = new ArrayList<double[]>();
 		for (int i = 0; i < 100; i++) {
 			vectors.add(subject.randomVector(2));
 		}
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < 100; i++) {
 			subject.lu(matrices.get(i));
 		}
 		long end = System.currentTimeMillis();
 		System.out.println(end - start);
 	}
 
 	//
 	// @Test
 	// public void testJama() throws Exception {
 	// List<Jama.Matrix> matrices = new ArrayList<Jama.Matrix>();
 	// for (int i = 0; i < 100; i++) {
 	// Jama.Matrix m = new Jama.Matrix(subject.randomMatrix(2, 2));
 	// matrices.add(m);
 	// }
 	// List<Jama.Matrix> vectors = new ArrayList<Jama.Matrix>();
 	// for (int i = 0; i < 100; i++) {
 	// vectors.add(new Jama.Matrix(subject.randomVector(2), 2));
 	// }
 	// long start = System.currentTimeMillis();
 	// for (int i = 0; i < 100; i++) {
 	// matrices.get(i).lu();
 	// }
 	// long end = System.currentTimeMillis();
 	// System.out.println(end - start);
 	// }
 
 	@Test
 	public void testMinor() throws Exception {
 		double[][] a = new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
 		Set<Integer> columnsToEliminate = new HashSet<Integer>();
 		columnsToEliminate.add(2);
 		Set<Integer> rowsToEliminate = new HashSet<Integer>();
 		rowsToEliminate.add(2);
 		System.out.println(subject.toString(a));
 		assertValueClose(-3,
 				subject.minor(a, columnsToEliminate, rowsToEliminate));
 	}
 
 	@Test
 	public void testMinor2Rows() throws Exception {
 		double[][] a = new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
 		Set<Integer> columnsToEliminate = new HashSet<Integer>();
 		columnsToEliminate.add(2);
 		columnsToEliminate.add(0);
 		Set<Integer> rowsToEliminate = new HashSet<Integer>();
 		rowsToEliminate.add(2);
 		rowsToEliminate.add(1);
 		System.out.println(subject.toString(a));
 		assertValueClose(2,
 				subject.minor(a, columnsToEliminate, rowsToEliminate));
 	}
 }
