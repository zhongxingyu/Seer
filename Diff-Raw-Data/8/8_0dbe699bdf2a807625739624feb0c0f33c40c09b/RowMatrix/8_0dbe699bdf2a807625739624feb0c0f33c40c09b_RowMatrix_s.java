 /* Copyright (C) 2013, Cameron White
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package edu.pdx.cawhite.math;
 
 import edu.pdx.cawhite.iterators.IteratorException;
import edu.pdx.cawhite.math.ColumnMatrix.Iterator;
 	
 /**
  * Implements a mathematical row matrix
  *
  * A row matrix is a matrix that contains a list of 
  * row Vectors. The importance in difference between
  * RowMatrix and ColumnMatrix is that certain matrix
  * operations are optimized by the use of one of the
  * other.
  * 
  * @author Cameron Brandon White
  */
 public class RowMatrix extends Matrix {
 
 	/**
 	 * Construct a RowMatrix with the given components
 	 */
 	public RowMatrix(double[][] components) {
 		super();
 		this.components = components;
 	}
 
 	/**
 	 * Create a matrix with the given number of rows and columns
 	 * 
 	 * @param numberOfRows The number of rows the new matrix should have.
 	 * @param numberOfCols The number of columns the new matrix should have.
 	 */
 	public RowMatrix(int numberOfRows, int numberOfColumns) {
 		super();
 		this.components = new double[numberOfRows][numberOfColumns];
 	}
 
 	/**
 	 * Get the item at the given position.
 	 * 
 	 * @param rowIndex 		The row index.
 	 * @param columnIndex 	The column index.
 	 * @return 				The item at the given position.
 	 */
 	public Double get(int rowIndex, int columnIndex) {
 		return this.components[rowIndex][columnIndex];
 	}
 	
 	/**
 	 * Get the iterator for the matrix.
 	 */	
 	public Iterator getIterator() {
 		return this.new Iterator();
 	}
 	
 	/**
 	 * Get the number of rows the matrix has.
 	 * 
 	 * @return the number of rows in the matrix
 	 */
 	public int getNumberOfRows() {
 		return this.components.length;
 	}
 
 	/**
 	 * Get the number of columns the matrix has.
 	 * 
 	 * @return The number of columns in the matrix.
 	 */
 	public int getNumberOfColumns() {
 		return this.components[0].length;
 	}
 
 	/**
 	 * Set the item at the given position.
 	 * 
 	 * @param rowIndex 		The row index.
 	 * @param columnIndex 	The column index.
 	 * @param item 			The new item.
 	 */
 	public void set(int rowIndex, int columnIndex, Double item) {
 		this.components[rowIndex][columnIndex] = item;
 	}
 
 	/**
 	 * Add this matrix with the other in a new matrix
 	 * 
 	 * @param other 	The other matrix
 	 * @return 			The new matrix.
 	 */
 	public RowMatrix add(RowMatrix other) 
 			throws MatrixSizeException {
 		RowMatrix newMatrix = new RowMatrix(getNumberOfRows(), getNumberOfColumns());
 		return (RowMatrix) super.add(other, newMatrix);
 	}
 
 	/**
 	 * Add this matrix with the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	public RowMatrix iadd(RowMatrix other) 
 			throws MatrixSizeException {
 		return (RowMatrix) super.iadd(other);
 	}
 
 	/**
 	 * Subtract this matrix from the other in a new matrix.
 	 * 
 	 * @param other The other matrix
 	 * @return 		The new matrix.
 	 */
 	public RowMatrix sub(RowMatrix other) 
 			throws MatrixSizeException {
 		RowMatrix newMatrix = new RowMatrix(getNumberOfRows(), getNumberOfColumns());
 		return (RowMatrix) super.sub(other, newMatrix);
 	}
 
 	/**
 	 * Subtract this matrix from the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	public RowMatrix isub(RowMatrix other) 
 			throws MatrixSizeException {
 		return (RowMatrix) super.isub(other);
 	}
 
 	/**
 	 * Multiply this matrix by the scalar in a new matrix.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			The new matrix.
 	 */
 	public RowMatrix mul(Double scalar) {
 		RowMatrix newMatrix = new RowMatrix(getNumberOfRows(), 
 											getNumberOfColumns());
 		return (RowMatrix) mul(scalar, newMatrix);
 	}
 
 	/**
 	 * Multiply this matrix by the scalar in place.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			This vector modified.
 	 */
 	public RowMatrix imul(Double scalar) {
 		return (RowMatrix) super.imul(scalar);
 	}
 
 	/**
 	 * Multiply this matrix by the other Matrix in a new matrix
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			The new matrix.
 	 */
 	public ColumnMatrix mul(ColumnMatrix other) 
 			throws MatrixSizeException {
 		assert this.getNumberOfColumns() == other.getNumberOfRows();
 
 		ColumnMatrix newMatrix = new ColumnMatrix(getNumberOfRows(),
 											      other.getNumberOfColumns());
 
 		return (ColumnMatrix) super.mul(other, newMatrix);
 	}
 
 	/**
 	 * Operation preformed on this matrix and another.
 	 */
 	public RowMatrix matrixOperation(
 			RowMatrix other, VectorOperation vectorOperation) 
 			throws MatrixSizeException {
 
 		RowMatrix newMatrix = new RowMatrix(getNumberOfRows(),
 											getNumberOfColumns());
 
 		return (RowMatrix) super.matrixOperation(other, newMatrix, vectorOperation);
 	}
 
 	/**
 	 * Operation preformed on this matrix and another in-place.
 	 */
 	public RowMatrix iMatrixOperation(
 			RowMatrix other, IVectorOperation vectorOperation) 
 			throws MatrixSizeException {
 
 		return (RowMatrix) super.iMatrixOperation(other, vectorOperation);
 	}
 
 	public void print() {
 		for(int i = 0; i < getNumberOfRows(); i++) {
 			for(int j = 0; j < getNumberOfColumns(); j++) {
 				System.out.print(this.get(i,j));
 				System.out.print(" ");
 			}
 			System.out.println();
 		}
 	}
 }
