 package com.lucasazzola.game.graphics;
 
 import java.util.Arrays;
 
 import com.lucasazzola.game.exception.MatrixIndexOutOfBoundsException;
 
 /**
  * Three dimensional Matrix class
  * 
  * @author Lucas Azzola
  * @since 16/8/2012
  */
 public class Matrix {
 
 	/**
 	 * Public Matrix
 	 */
 	// R C
 	public float[][] m;
 
 	/**
 	 * Constructs a 3x3 matrix of values 0
 	 */
 	public Matrix() {
 		this.m = new float[3][3];
 		setElementsToValue(m, 0);
 	}
 
 	/**
 	 * Constructs a 3x3 matrix of values value
 	 * 
 	 * @param value
 	 *            the value to assign to all cells
 	 */
 	public Matrix(float value) {
 		this.m = new float[3][3];
 		setElementsToValue(m, value);
 	}
 
 	/**
 	 * Constructs a matrix with specified values
 	 * 
 	 * @param m11
 	 *            Matrix position m11
 	 * @param m12
 	 *            Matrix position m12
 	 * @param m13
 	 *            Matrix position m13
 	 * @param m21
 	 *            Matrix position m21
 	 * @param m22
 	 *            Matrix position m22
 	 * @param m23
 	 *            Matrix position m23
 	 * @param m31
 	 *            Matrix position m31
 	 * @param m32
 	 *            Matrix position m32
 	 * @param m33
 	 *            Matrix position m33
 	 */
 	public Matrix(float m11, float m12, float m13, float m21, float m22,
 			float m23, float m31, float m32, float m33) {
 		this.m = new float[3][3];
 
 		m[0][0] = m11;
 		m[0][1] = m12;
 		m[0][2] = m13;
 
 		m[1][0] = m21;
 		m[1][1] = m22;
 		m[1][2] = m23;
 
 		m[2][0] = m31;
 		m[2][1] = m32;
 		m[2][2] = m33;
 	}
 
 	/**
 	 * Constructs a matrix given a matrix array
 	 * 
 	 * @param m
 	 *            the matrix array
 	 * @throws MatrixIndexOutOfBoundsException
 	 *             when the array is not 3x3
 	 */
 	public Matrix(float[][] m) throws MatrixIndexOutOfBoundsException {
 		if (m.length != 3 || m[0].length != 3)
 			throw new MatrixIndexOutOfBoundsException();
 		this.m = m.clone();
 	}
 
 	/**
 	 * Constructs a scaling matrix from a Vector3
 	 * 
 	 * @param vector
 	 *            the vector3
 	 */
 	public Matrix(Vector3 vector) {
 		this.m = fromVector(vector);
 	}
 
 	/**
 	 * Constructs a matrix from 3 (column) Vector3s
 	 * 
 	 * @param v1
 	 *            the first column vector
 	 * @param v2
 	 *            the second column vector
 	 * @param v3
 	 *            the third column vector
 	 */
 	public Matrix(Vector3 v1, Vector3 v2, Vector3 v3) {
 		this.m = fromVectorsCols(v1, v2, v3);
 	}
 
 	/**
 	 * Constructs a Matrix given another Matrix
 	 * 
 	 * @param matrix
 	 */
 	public Matrix(Matrix matrix) {
 		this.m = matrix.m.clone();
 	}
 
 	/**
 	 * Sets all elements in m to a single value
 	 * 
 	 * @param m
 	 *            the matrix to set elements in
 	 * @param value
 	 *            the value to set
 	 */
 	private void setElementsToValue(float[][] m, float value) {
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[0].length; j++) {
 				m[i][j] = value;
 			}
 		}
 	}
 
 	/**
 	 * Add two matrices
 	 * 
 	 * @param matrix
 	 *            the first Matrix to add
 	 * @param matrix
 	 *            the second Matrix to add
 	 * @return A new matrix which is the sum of the other two
 	 */
 	public static Matrix add(Matrix m1, Matrix m2) {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
 				out.m[i][j] = m1.m[i][j] + m2.m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Subtract one matrix from another
 	 * 
 	 * @param m1
 	 *            the first Matrix
 	 * @param m2
 	 *            the matrix to subtract
 	 * @return A new matrix which is m1 - m2
 	 */
 	public static Matrix subtract(Matrix m1, Matrix m2) {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
<<<<<<< HEAD
 				out.m[i][j] = m1.m[i][j] - m2.m[i][j];
=======
				out.m[i][j] = m1.m[i][j] + m2.m[i][j];
>>>>>>> 98177f6cf6277a3c30aa0cd4d04953a48d4920ce
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Gets the negated matrix
 	 * 
 	 * @return the negated matrix
 	 */
 	public Matrix negate() {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[0].length; j++) {
 				out.m[i][j] = -m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Get a (scaling) matrix array from a Vector
 	 * 
 	 * @param vector
 	 *            the vector
 	 * @return the matrix {{x 0 0} {0 y 0} {0 0 z}}
 	 */
 	public static float[][] fromVector(Vector3 vector) {
 		float[][] m = { { vector.x, 0, 0 }, { 0, vector.y, 0 },
 				{ 0, 0, vector.z }, };
 		return m;
 	}
 
 	/**
 	 * Get a matrix array from three vectors, where a vector represents a single
 	 * column
 	 * 
 	 * @param v1
 	 *            the first column
 	 * @param v2
 	 *            the second column
 	 * @param v3
 	 *            the third column
 	 * @return a matrix array
 	 */
 	public static float[][] fromVectorsCols(Vector3 v1, Vector3 v2, Vector3 v3) {
 		float[][] m = { { v1.x, v2.x, v3.x }, { v1.y, v2.y, v3.y },
 				{ v1.z, v2.z, v3.z }, };
 		return m;
 	}
 
 	/**
 	 * Get a matrix array from three vectors, where a vector represents a single
 	 * row
 	 * 
 	 * @param v1
 	 *            the first row
 	 * @param v2
 	 *            the second row
 	 * @param v3
 	 *            the third row
 	 * @return a matrix array
 	 */
 	public static float[][] fromVectorsRows(Vector3 v1, Vector3 v2, Vector3 v3) {
 		float[][] m = { { v1.x, v1.y, v1.z }, { v2.x, v2.y, v2.z },
 				{ v3.x, v3.y, v3.z }, };
 		return m;
 	}
 
 	@Override
 	public int hashCode() {
 		return 31 + Arrays.hashCode(m);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null || getClass() != obj.getClass())
 			return false;
 		Matrix other = (Matrix) obj;
 		if (!Arrays.deepEquals(m, other.m))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Matrix [" + Arrays.toString(m) + "]";
 	}
 }
