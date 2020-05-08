 package com.barantschik.trinkets.raytracer;
 
 import java.util.Arrays;
 
 public abstract class GMath
 {
 	public static double[] normalize(double[] vec)
 	{
 		vec = Arrays.copyOf(vec, vec.length);
 		double magnitude = getMag(vec);
 		if(magnitude == 0)
 		{
 			return null;
 		}
 		else
 		{			
 			for(int i = 0; i < vec.length; i++) vec[i] /= magnitude;
 			return vec;
 		}
 	}
 
 	public static double getMag(double[] vec)
 	{
 		double magnitude = 0;
 		for(double d : vec) magnitude += d * d;
 		magnitude = Math.sqrt(magnitude);
 		return magnitude;
 	}
 
 	public static double[] subtract(double[] a, double[] b)
 	{
 		b = Arrays.copyOf(b,  b.length);
 		for(int i = 0; i < b.length; i++)
 		{
 			b[i] -= a[i];
 		}
 		return b;
 	}
 
 	public static double det(M3x3 matrix)
 	{
 		double a = matrix.values[0][0] * det(matrix.getMinor(0, 0));
 		double b = -matrix.values[1][0] * det(matrix.getMinor(1, 0));
 		double c = matrix.values[2][0] * det(matrix.getMinor(2, 0));
 		return a + b + c;
 	}
 	
 	public static double det(double a, double b, double c, double d)
 	{
 		return a * d - b * c;
 	}
 	
 	public static double det(double[] matrix)
 	{
 		return matrix[0] * matrix[3] - matrix[1] * matrix[2];
 	}
 
 	public static double[] cross(double[] a, double[] b)
 	{
 		double x = det(a[1], a[2], b[1], b[2]);
 		double y = -det(a[0], a[2], b[0], b[2]);
 		double z = det(a[0], a[1], b[0], b[1]);
 
 		return new double[]{x, y, z};
 	}
 
 	public static double dot(double[] a, double[] b)
 	{
 		double total = 0;
 		for(int i = 0; i < a.length; i++)
 		{
 			total += a[i] * b[i];
 		}
 		return total;
 	}
 
 	public static double[] mult(double[] vals, double scal)
 	{
 		vals = Arrays.copyOf(vals,  vals.length);
 		for(int i = 0; i < vals.length; i++) vals[i] *= scal;
 		return vals;
 	}
 
 	public static M3x3 mult(M3x3 matrix, double scal)
 	{
 		matrix = new M3x3(matrix.values);
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 3; j++)
 			{
				matrix.values[i][j] = matrix.values[i][j] * scal;
 			}
 		}
 		return matrix;
 	}
 	
 	public static double[] mult(M3x3 matrix, double[] vector)
 	{
 		double[] values = new double[3];
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 3; j++)
 			{
 				values[i] += vector[j] * matrix.values[i][j];
 			}
 		}
 		return values;
 	}
 	
 	public static double[] negative(double[] a)
 	{
 		a = Arrays.copyOf(a, a.length);
 		for(int i = 0; i < a.length; i++) a[i] = -a[i];
 		return a;
 	}
 
 	public static double[] add(double[]... vals)
 	{
 		vals = Arrays.copyOf(vals, vals.length);
 		double[] total = new double[vals[0].length];
 		for(double[] dA : vals)
 		{
 			for(int i = 0; i < dA.length; i++)
 			{
 				total[i] += dA[i];
 			}
 		}
 		return total;
 	}
 
 	public static double[] solveQuad(double a, double b, double c)
 	{
 		double discriminant = b * b - 4 * a * c;
 		if(discriminant < 0)
 		{
 			return new double[]{Double.NaN};
 		}
 		else
 		{
 			double sqrtDiscriminant = Math.sqrt(discriminant);
 			double firstAnswer = (-b + sqrtDiscriminant) / (2 * a);
 			double secondAnswer = (-b - sqrtDiscriminant) / (2 * a);
 			return new double[]{firstAnswer, secondAnswer};
 		}
 	}
 
 	public static double[] findAverage(double[] a, double[] b)
 	{
 		return mult(add(a, b), 0.5);
 	}
 
 	public static double[] getHalfAngle(double[] view, double[] light, double[] normal)
 	{
 		view = normalize(view);
 		light = normalize(light);
 		double[] average = findAverage(view, light);
 		if(average[0] == 0 && average[1] == 0 && average[2] == 0) return normalize(normal);
 		else return normalize(average);
 	}
 
 	public static M3x3 constructMatrix(double[] a, double[] b, double[] c)
 	{
 		double[][] matrix = new double[3][3];
 		for(int i = 0; i < 3; i++)
 		{
 			matrix[i][0] = a[i];
 			matrix[i][1] = b[i];
 			matrix[i][2] = c[i];
 		}
 		return new M3x3(matrix);
 	}
 
 	public static M3x3 findInverseMatrix(M3x3 original)
 	{
 		double determinant = det(original);
 		if(determinant != 0)
 		{
 			return mult(getCofactors(findTranspose(original)), (1 / determinant));
 		}
 		return null;
 	}
 	
 	public static M3x3 getCofactors(M3x3 original)
 	{
 		double[][] values = new double[3][3];
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 3; j++)
 			{
 				values[i][j] = det(original.getMinor(i, j));
 				if((i + j) % 2 == 1) values[i][j] = -values[i][j];
 			}
 		}
 		return new M3x3(values);
 	}
 	
 	public static M3x3 findTranspose(M3x3 original)
 	{
 		double[][] values = new double[3][3];
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 3; j++)
 			{
 				values[i][j] = original.values[j][i];
 			}
 		}
 		return new M3x3(values);
 	}
 }   
