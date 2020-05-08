 package com.barantschik.trinkets.tests.raytracer;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.barantschik.trinkets.raytracer.GMath;
 import com.barantschik.trinkets.raytracer.M3x3;
 
 public class GMathTest
 {
 	private static final double EPSILON = 0.0001;
 	
 	public static boolean acceptable(double a, double b)
 	{
 		if(Math.abs(a - b) > EPSILON) return false;
 		return true;
 	}
 	
 	public static boolean acceptable(double[] a, double[] b)
 	{
 		if(a.length != b.length)
 		{
 			return false;
 		}
 		for(int i = 0; i < a.length; i++)
 		{
 			if(Math.abs(a[i] - b[i]) > EPSILON) return false;
 		}
 		return true;
 	}
 	
 	public static boolean acceptable(double[][] a, double[][] b)
 	{
 		if(a.length != b.length || a[0].length != b[0].length)
 		{
 			return false;
 		}
 		for(int i = 0; i < a.length; i++)
 		{
 			for(int j = 0; j < a[0].length; j++)
 			{
 				if(Math.abs(a[i][j] - b[i][j]) > EPSILON) return false;
 			}
 		}
 		return true;
 	}
 	
 	@Test
 	public void testNormalize()
 	{
 		double[] test1 = {0, 0, 0};
 		assertNull("Normalization should not return anything with a vector of length 0", GMath.normalize(test1));
 		double[] test2 = {1, 0, 0};
 		assertTrue("Normalization shouldn't change a vector of length 1", acceptable(test2, GMath.normalize(test2)));
 		double[] test3 = {1.3, 0, 0};
 		assertTrue("Normalization should make the length of this vector 1", acceptable(test2, GMath.normalize(test3)));
 		double[] test4 = {1.3, 1.3, 1.3};
 		double[] test4Solution = {0.57735026919, 0.57735026919, 0.57735026919};
 		assertTrue("Normalization should make the length of this vector 1", acceptable(test4Solution, GMath.normalize(test4)));
 		double[] test5 = {1.3, -1.3, -1.3};
 		double[] test5Solution = {0.57735026919, -0.57735026919, -0.57735026919};
 		assertTrue("Normalization should make the length of this vector 1", acceptable(test5Solution, GMath.normalize(test5)));
 	}
 	
 	@Test
 	public void testTransposeMatrix3x3()
 	{
 		double[][] test1 = {{1, 2, 3}, 
 							{4, 5, 6}, 
 							{7, 8, 9}};
 		double[][] test1Solution = {{1, 4, 7}, 
 									{2, 5, 8}, 
 									{3, 6, 9}};
 		assertTrue("Transposing the matrix should flip it across the diagonal", acceptable(test1Solution, GMath.findTranspose(new M3x3(test1)).values));
 	}
 
 	@Test
 	public void testConstructMatrix()
 	{
 		double[] test1A = {1, 2, 3};
 		double[] test1B = {4, 5, 6};
 		double[] test1C = {7, 8, 9};
 		double[][] test1Solution = {{1, 4, 7},
 									{2, 5, 8},
 									{3, 6, 9}};
 		assertTrue("These three arrays should become the solution array", acceptable(test1Solution, GMath.constructMatrix(test1A, test1B, test1C).values));
 	}
 	
 	@Test
 	public void testDeterminant3x3()
 	{
 		double[][] test1Values = {{1, 2, 3},
 								  {0, 1, 4},
 								  {5, 6, 0}};
 		M3x3 test1 = new M3x3(test1Values);
 		double test1Solution = 1;
 		assertTrue("The determinant of this matrix should be 1", acceptable(test1Solution, GMath.det(test1)));
 		
 		double[][] test2Values = {{2, -7.1, 3.18},
 				  				  {-1.22, 2.38, 9.1},
 				  				  {-8.1, 0, 7.65}};
 		M3x3 test2 = new M3x3(test2Values);
 		double test2Solution = 554.79474;
 		assertTrue("The determinant of this matrix should be 554.8", acceptable(test2Solution, GMath.det(test2)));
 	}

	@Test
	public void testFindCofactors()
	{
		double[][] test1Values = {{3, 0, 2},
								  {2, 0, -2},
								  {0, 1, 1}};
		M3x3 test1 = new M3x3(test1Values);
		double[][] test1Solution = {{2, -2, 2},
									{2, 3, -3},
									{0, 10, 0}};
		assertTrue("This should output the correct cofactor matrix", acceptable(test1Solution, GMath.getCofactors(test1).values));
	}

	@Test
	public void testFindInverse()
	{
		double[][] test1Values = {{3, 0, 2},
				  				  {2, 0, -2},
				  				  {0, 1, 1}};
		M3x3 test1 = new M3x3(test1Values);
		double[][] test1Solution = {{0.2, 0.2, 0},
									{-0.2, 0.3, 1},
									{0.2, -0.3, 0}};
		assertTrue("This should output the correct inverse matrix", acceptable(test1Solution, GMath.findInverseMatrix(test1).values));
	}
 }
