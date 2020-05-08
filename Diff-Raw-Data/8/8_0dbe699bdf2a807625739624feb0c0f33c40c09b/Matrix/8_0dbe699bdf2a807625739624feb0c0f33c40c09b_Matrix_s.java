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
 	
 import edu.pdx.cawhite.math.Vector;
 import edu.pdx.cawhite.iterators.BidirectionalIterator;
 import edu.pdx.cawhite.iterators.IteratorException;
 
 /**
  * Abstract mathematical matrix.
  *
  * A matrix has a fixed number of rows and columns. Once created
  * the number of rows and columns can not change.
  * 
  * @author Cameron Brandon White
  */
 public abstract class Matrix {
 
 	protected double[][] components;
 
 	public abstract Matrix.Iterator getIterator();
 	public abstract int getNumberOfRows();
 	public abstract int getNumberOfColumns();
 
 	/**
 	 * Get the number of Vectors in the Matrix.
 	 * 
 	 * @return The number of vectors in the Matrix.
 	 */
 	public int getNumberOfVectors() {
 		return this.components.length;
 	}
 	
 	/**
 	 * Get the Vector at the given index.
 	 *
 	 * @return The specified Vector
 	 */	
 	public Vector getVector(int index) {
 		Vector vector = null;
 		try {
 			vector = new Vector(components[index]);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException();
 		} catch (LengthException e) {
 			assert false : "Fatal Programming Error";
 		}
 		return vector;
 	}
 
 	/**
 	 * Get the array of components in the matrix.
 	 *
 	 * @return The matrix's components
 	 */
 	public double[][] getComponents() {
 		return this.components;
 	}
 
 	/**
 	 * Set the components in the matrix
 	 *
 	 * @param components The new components
 	 */
 	public void setComponents(double[][] components) {
 		this.components = components;
 	}
 
 	/**
 	 * Set the vector at the given index.
 	 *
 	 * The contents of the vector are copied into the matrix.
 	 *
 	 * @param index The index to put the new vector
 	 * @param newVector The new vector
 	 */
 	public void setVector(int index, Vector newVector) 
 			throws IndexOutOfBoundsException, LengthException {
 		Vector currentVector = this.getVector(index);
 		currentVector.copy(newVector);
 	}
 
 	/**
 	 * Add this matrix with the other in a new matrix
 	 * 
 	 * @param other 	The other matrix
 	 * @return 			The new matrix.
 	 */
 	protected Matrix add(Matrix other, Matrix newMatrix) 
 			throws MatrixSizeException {
 		return matrixOperation(other, newMatrix, new VectorOperation() {
 			public Vector operation(Vector a, Vector b, Vector c) 
 					throws MatrixSizeException {
 				try {
 					return a.add(b, c);
 				} catch (LengthException e) {
 					throw new MatrixSizeException();
 				}
 			}
 		});
 	}
 
 
 	/**
 	 * Add this matrix with the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	protected Matrix iadd(Matrix other) throws MatrixSizeException {
 		return iMatrixOperation(other, new IVectorOperation() {
 			public Vector operation(Vector a, Vector b)
 					throws MatrixSizeException {
 				try {
 					return a.iadd(b);
 				} catch (LengthException e) {
 					throw new MatrixSizeException();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Subtract this matrix from the other in place.
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			This matrix modified.
 	 */
 	protected Matrix isub(Matrix other) throws MatrixSizeException {
 		return iMatrixOperation(other, new IVectorOperation() {
 			public Vector operation(Vector a, Vector b)
 					throws MatrixSizeException {
 				try {
 					return a.isub(b);
 				} catch (LengthException e) {
 					throw new MatrixSizeException();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Multiply this matrix by the scalar in place.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			This vector modified.
 	 */
 	protected Matrix imul(Double scalar) {
 
 		Iterator iterator = this.getIterator();
 
 		while (true) {
 			try {
 
 				Vector vector = iterator.get();
 				vector.imul(scalar);
 				iterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			}
 		}
 
 		return this;
 	}
 
 	/**
 	 * Operation preformed this matrix and another in place.
 	 */
 	protected Matrix iMatrixOperation(
 			Matrix other, IVectorOperation vectorOperation)
 				throws MatrixSizeException {
 
 		Iterator thisIterator = this.getIterator();
 		Iterator otherIterator = other.getIterator();
 
 		while (true) {	
 			try {
 
 				Vector thisVector = thisIterator.get();
 				Vector otherVector = otherIterator.get();
 
 				vectorOperation.operation(thisVector, otherVector);
 
 				thisIterator.next();
 				otherIterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			}
 		}
 
 		return this;
 	}
 
 	/**
 	 * Multiply this matrix by the other Matrix in a new matrix
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			The new matrix.
 	 */
 	public Vector mul(Vector vector, Vector newVector) 
 			throws LengthException {
 
 		Matrix.Iterator matrixIterator = this.getIterator();
 		Vector.Iterator vectorIterator = newVector.getIterator();
 
 		while (true) {
 			try {
 
 				Vector matrixVector = matrixIterator.get();
 				
 				vectorIterator.set(matrixVector.dot(vector));
 
 				matrixIterator.next();
 				vectorIterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			}
 			
 		}
 
 		return newVector;
 	}
 	
 	/**
 	 * Multiply this matrix by the other Matrix in a new matrix
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			The new matrix.
 	 */
 	public Vector mul(Vector vector) throws LengthException {
 		Vector newVector = new Vector(vector.getLength());
 		return mul(vector, newVector);
 	}
 	
 	/**
 	 * Multiply this matrix by the scalar in a new matrix.
 	 * 
 	 * @param scalar 	The scalar to multiply by.
 	 * @return 			The new matrix.
 	 */
 	protected Matrix mul(Double scalar, Matrix newMatrix) {
 
 		Matrix.Iterator thisIterator = this.getIterator();
 		Matrix.Iterator newIterator  = newMatrix.getIterator();
 
 		while (true) {
 			try {
 
 				Vector thisVector = thisIterator.get();
 				Vector newVector = newIterator.get();
 
 				thisVector.mul(scalar, newVector);
 
 				thisIterator.next();
 				newIterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			}
 
 		}
 
 		return newMatrix;
 	}
 
 	/**
 	 * Multiply this matrix by the other Matrix in a new matrix
 	 * 
 	 * @param other 	The other matrix.
 	 * @return 			The new matrix.
 	 */
 	protected Matrix mul(Matrix other, Matrix newMatrix) 
 			throws MatrixSizeException {
 
 		Matrix.Iterator otherIterator = other.getIterator();
 		Matrix.Iterator newIterator   = newMatrix.getIterator();
 
 		while (true) {
 			try {
 
 				Vector newVector = newIterator.get();
 				Vector otherVector = otherIterator.get();	
 
 				mul(otherVector, newVector);
 
 				otherIterator.next();
 				newIterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			} catch (LengthException e) {
 				throw new MatrixSizeException();
 			}
 
 		}
 
 		return newMatrix;
 	}
 
 	/**
 	 * Operation proformed on this matrix and another.
 	 */
 	protected Matrix matrixOperation(
 			Matrix other, Matrix newMatrix,
 			VectorOperation vectorOperation) 
 				throws MatrixSizeException {
 
 		Matrix.Iterator thisIterator  = this.getIterator();
 		Matrix.Iterator otherIterator = other.getIterator();
 		Matrix.Iterator newIterator   = newMatrix.getIterator();
 
 		while (true) {
 			try {
 
 				Vector thisVector = thisIterator.get();
 				Vector otherVector = otherIterator.get();
 				Vector newVector = newIterator.get();
 
 				vectorOperation.operation(thisVector, otherVector, newVector);
 
 				thisIterator.next();
 				otherIterator.next();
 				newIterator.next();
 
 			} catch (IteratorException e) {
 				break;
 			}
 		}
 
 		return newMatrix;
 	}
 
 	/**
 	 * Subtract this matrix from the other in a new matrix.
 	 * 
 	 * @param other 	The other matrix
 	 * @return 			The new matrix.
 	 */
 	protected Matrix sub(Matrix other, Matrix newMatrix) 
 			throws MatrixSizeException {
 		return matrixOperation(other, newMatrix, new VectorOperation() {
 			public Vector operation(Vector a, Vector b, Vector c) 
 					throws MatrixSizeException {
 				try {
 					return a.sub(b, c);
 				} catch (LengthException e) {
 					throw new MatrixSizeException();
 				}
 			}
 		});
 	}
 
     public interface IVectorOperation {
         Vector operation(Vector a, Vector b) throws MatrixSizeException;
     }
     
     public interface VectorOperation {
         Vector operation(Vector a, Vector b, Vector c) throws MatrixSizeException;
     }
    	
    	/**
    	 * Iterator for the Matrix class
    	 */ 
	public abstract class Iterator extends BidirectionalIterator {
 
 		protected int index;
 
 		/**
 		 * Construct the iteractor to reference the first row in the
 		 * matrix.
 		 */
 		public Iterator() {
 			this.index = 0;
 		}
 
 		/**
 		 * Get the Vector referenced by the iterator
 		 */
 		public Vector get() throws IteratorException {
 			try {
 				return Matrix.this.getVector(index);
 			} catch (IndexOutOfBoundsException e) {
 				throw new IteratorException();
 			}
 		}
 
 		/**
 		 * Set the Vector referenced by the iterator to the
 		 * Vector specified by value.
 		 *
 		 * The vector specified by value is copied to the
 		 * vector referenced by the iterator.
 		 *
 		 * @param value The new Vector
 		 */
 		public void set(Vector value) 
 				throws IteratorException, LengthException {
 			try {
 				Matrix.this.setVector(index, value);
 			} catch (IndexOutOfBoundsException e) {
 				throw new IteratorException();
 			} 
 		}
 
 		/**
 		 * Reference the next vector in the matrix
 		 *
 		 * @throws IteratorException Thrown when the vector
 		 * 		   has no next element.
 		 */
 		public void next() throws IteratorException {
 			if (index >= getNumberOfRows() - 1)
 				throw new IteratorException();
 			index++;
 		}
 
 		/**
 		 * Reference the previous vector in the matrix
 		 *
 		 * @throws IteratorException Thrown when the vector
 		 *         has no previous element.
 		 */
 		public void previous() throws IteratorException {
 			if (index <= 0)
 				throw new IteratorException();
 			index--;
 		}
 
 		/**
 		 * Restart the iterator to reference the first vector
 		 */
 		public void restart() {
 			index = 0;
 		}
 	}
 }
