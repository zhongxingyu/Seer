 package edu.pdx.cawhite.math;
 	
 /**
  * @author Cameron Brandon White
  */
 public class ColumnMatrix extends Matrix {
 	
 
 	/**
 	 * Create the matrix with the given items
 	 * 
 	 * @param items items to add to the matrix
 	 */
 	public ColumnMatrix(double[][] components) {
 		super();
 		this.components = components;
 	}
 
 	/**
 	 * Create a matrix with the given number of rows and columns
 	 * 
 	 * @param numberOfRows The number of rows the new matrix should have.
 	 * @param numberOfCols The number of columns the new matrix should have.
 	 */
 	public ColumnMatrix(int numberOfRows, int numberOfColumns) {
 		super();
		this.components = new double[numberOfRows][numberOfColumns];
 	}
 
 	protected ColumnMatrix() {
 		super();
 	}
	
 	/**
 	 * Get the item at the given position.
 	 * 
 	 * @param rowIndex 		The row index.
 	 * @param columnIndex 	The column index.
 	 * @return The item at the given position.
 	 */
 	public Double get(int rowIndex, int columnIndex) {
 		try {
 			return this.components[columnIndex][rowIndex];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 
 	/**
 	 * Get the number of rows the matrix has.
 	 * 
 	 * @return the number of rows in the matrix
 	 */
 	public int getNumberOfRows() {
 		return this.components[0].length;
 	}
 
 	/**
 	 * Get the number of columns the matrix has.
 	 * 
 	 * @return The number of columns in the matrix.
 	 */
 	public int getNumberOfColumns() {
 		return this.components.length;
 	}
 
 	/**
 	 * Set the item at the given position.
 	 * 
 	 * @param rowIndex 		The row index.
 	 * @param columnIndex 	The column index.
 	 * @param item 			The new item.
 	 */
 	public void set(int rowIndex, int columnIndex, Double item) {
 		try {
 			this.components[columnIndex][rowIndex] = item;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException();
 		}	
 	}
 
 	/**
 	 * Add this matrix with the other in a new matrix
 	 * 
 	 * @param other 	The other matrix
 	 * @return 			The new matrix.
 	 */
 	public ColumnMatrix add(ColumnMatrix other) 
 			throws MatrixSizeException {
 		ColumnMatrix newMatrix = new ColumnMatrix(getNumberOfRows(), 
 												  getNumberOfColumns());
 		return (ColumnMatrix) super.add(other, newMatrix);
 	}
 
 	/**
 	 * Add this matrix with the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	public ColumnMatrix iadd(ColumnMatrix other) 
 			throws MatrixSizeException {
 		return (ColumnMatrix) super.iadd(other);
 	}
 
 	/**
 	 * Subtract this matrix from the other in a new matrix.
 	 * 
 	 * @param other 	The other matrix
 	 * @return 			The new matrix.
 	 */
 	public ColumnMatrix sub(ColumnMatrix other) 
 			throws MatrixSizeException {
 		ColumnMatrix newMatrix = new ColumnMatrix(getNumberOfRows(), 
 				                                  getNumberOfColumns());
 		return (ColumnMatrix) super.sub(other, newMatrix);
 	}
 
 	/**
 	 * Subtract this matrix from the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	public ColumnMatrix isub(ColumnMatrix other) 
 			throws MatrixSizeException {
 		return (ColumnMatrix) super.isub(other);
 	}
 
 	/**
 	 * Multiply this matrix by the scalar in a new matrix.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			The new matrix.
 	 */
 	public ColumnMatrix mul(Double scalar) 
 			throws MatrixSizeException {
 		ColumnMatrix newMatrix = new ColumnMatrix(getNumberOfRows(),
 												  getNumberOfColumns());
 		return (ColumnMatrix) mul(scalar, newMatrix);
 	}
 
 	/**
 	 * Multiply this matrix by the scalar in place.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			This vector modified.
 	 */
 	public ColumnMatrix imul(Double scalar) {
 		return (ColumnMatrix) super.imul(scalar);
 	}
 
 	/**
 	 * Multiply this matrix by the other Matrix in a new matrix
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			The new matrix.
 	 */
 	public RowMatrix mul(RowMatrix other) 
 			throws MatrixSizeException {
 		assert this.getNumberOfColumns() == other.getNumberOfRows();
 
 		RowMatrix newMatrix = new RowMatrix(getNumberOfRows(),
 											getNumberOfColumns());
 
 		return (RowMatrix) super.mul(other, newMatrix);
 	}
 
 	protected ColumnMatrix matrixOperation(
 		ColumnMatrix other, VectorOperation vectorOperation) 
 			throws MatrixSizeException {
 
 		ColumnMatrix newMatrix = new ColumnMatrix(getNumberOfRows(),
 												  getNumberOfColumns());
 
 		return (ColumnMatrix) super.matrixOperation(other, newMatrix, vectorOperation);
 	}
 
 	protected ColumnMatrix iMatrixOperation(
 			ColumnMatrix other, IVectorOperation vectorOperation) 
 				throws MatrixSizeException {
 
 		return (ColumnMatrix) super.iMatrixOperation(other, vectorOperation);
 	}
 }
