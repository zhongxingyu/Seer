 /*
  *  Abstract implementation of Matrix interface.
  *  Copyright (C) 2013 Michael Thorsley
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see [http://www.gnu.org/licenses/].
  */
 
 package com.eigenvektor.matrix;
 
 /**
  * An abstract implementation of the Matrix interface.  Provides
  * <code>equals()</code>, <code>hashCode()</code>, 
  * <code>toString()</code>, and basic implementations
  * of <code>multiply(Matrix)</code>, <code>add(Matrix)</code>,
  * and <code>subtract(Matrix)</code> that use a Full representations
  * for their return values.
  */
 public abstract class AbstractMatrix implements Matrix
 {
 	@Override
 	public Matrix multiply(Matrix m)
 	{
 		if (m == null) { throw new NullPointerException("m may not be null."); }
 		
 		// Check that the matrix is compatible for multiply.
 		if (m.getNRows() != this.getNCols())
 		{
 			throw new IllegalArgumentException("Argument not compatable for matrix multiply.");
 		}
 		
 		final int commonIndex = m.getNRows();
 		
 		// Create an output matrix.
 		FullMatrix ret = new FullMatrix(this.getNRows(), m.getNCols());
 		for (int j = 0 ; j < ret.getNRows() ; ++j)
 		{
 			for (int k = 0 ; k < ret.getNCols() ; ++k)
 			{
 				double val = 0;
 				for (int i = 0 ; i < commonIndex ; ++i)
 				{
 					val += this.get(j, i) * m.get(i, k);
 				}
 				ret.set(j, k, val);
 			}
 		}
 		
 		return ret;
 	}
 	
 
 	@Override
 	public Matrix add(Matrix m)
 	{
 		if (m == null) { throw new NullPointerException("m may not be null."); }
 		
 		if (m.getNRows() != this.getNRows() || m.getNCols() != this.getNCols())
 		{
 			throw new IllegalArgumentException("m not compatable for addition.");
 		}
 		
 		FullMatrix ret = new FullMatrix(this.getNRows(), this.getNCols());
 		for (int j = 0 ; j < ret.getNRows() ; ++j)
 		{
			for (int k = 0 ; k < ret.getNCols() ; ++k)
 			{
 				ret.set(j, k, this.get(j,k) + m.get(j, k));
 			}
 		}
 		return ret;
 	}
 	
 
 	@Override
 	public Matrix subtract(Matrix m){
 		if (m == null) { throw new NullPointerException("m may not be null."); }
 		
 		if (m.getNRows() != this.getNRows() || m.getNCols() != this.getNCols())
 		{
 			throw new IllegalArgumentException("m not compatable for addition.");
 		}
 		
 		FullMatrix ret = new FullMatrix(this.getNRows(), this.getNCols());
 		for (int j = 0 ; j < ret.getNRows() ; ++j)
 		{
			for (int k = 0 ; k < ret.getNCols() ; ++k)
 			{
 				ret.set(j, k, this.get(j,k) - m.get(j, k));
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * Gets a string representation of this.
 	 */
 	@Override
 	public String toString()
 	{
 		StringBuffer ret = new StringBuffer();
 		
 		int nRows = getNRows();
 		int nCols = getNCols();
 		
 		if (nRows > 10 || nCols > 10)
 		{
 			// If the matrix is really big, just say the size.
 			ret.append("[");
 			ret.append(getNRows());
 			ret.append("x");
 			ret.append(getNCols());
 			ret.append(" matrix]");
 		}
 		else
 		{
 			// Otherwise print out the matrix.
 			ret.append("[");
 			for (int j = 0 ; j < nRows ; ++j)
 			{
 				for (int k = 0 ; k < nCols ; ++k)
 				{
 					ret.append(get(j,k));
 					if (k < nCols - 1)
 					{
 						ret.append(", ");
 					}
 					else if (j < nRows - 1)
 					{
 						ret.append("; ");
 					}
 				}
 			}
 			ret.append("]");
 		}
 		
 		return ret.toString();
 	}
 	
 	/**
 	 * Tells if another object is equal to this.
 	 * 
 	 * @return <code>true</code> iff <code>o</code> is a matrix that is
 	 * semantically equal to <code>this</code>
 	 */
 	@Override
 	public boolean equals(final Object o)
 	{
 		// If it's not even a matrix, we're done.
 		if (!(o instanceof Matrix))
 		{
 			return false;
 		}
 		
 		Matrix m = (Matrix) o;
 		
 		// If it isn't the same size as this, we're done.
 		final int nRows = this.getNRows();
 		final int nCols = this.getNCols();
 		if (m.getNRows() != nRows || m.getNCols() != nCols)
 		{
 			return false;
 		}
 		
 		// Otherwise, do it element by element.
 		for (int j = 0 ; j < nRows ; ++j)
 		{
 			for (int k = 0 ; k < nCols ; ++k)
 			{
 				if (get(j, k) != m.get(j, k)) { return false; }
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Gets a hash code for this object.
 	 */
 	@Override
 	public int hashCode()
 	{
 		int hash = 0;
 		
 		final int nRows = this.getNRows();
 		final int nCols = this.getNCols();
 		for (int j = 0 ; j < nRows ; ++j)
 		{
 			for (int k = 0 ; k < nCols ; ++k)
 			{
 				hash ^= Double.valueOf(get(j, k)).hashCode();
 			}
 		}
 		
 		return hash;
 	}
 }
