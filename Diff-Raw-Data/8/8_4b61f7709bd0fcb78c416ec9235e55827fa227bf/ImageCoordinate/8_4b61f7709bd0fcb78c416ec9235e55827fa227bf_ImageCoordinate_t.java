 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.image;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Represents an integer-valued coordinate in an n-dimensional image.
  *
  * <p>
  * To avoid unnecessary object allocation when iterating over large images, this class provides basic object pooling capabilities.
  * ImageCoordinates can only be created using this class's static factory methods ({@link #createCoord()}, {@link #createCoordXYZCT(int, int, int, int, int)}, {@link #cloneCoord}), which will reuse ImageCoordinates if there are any
  * available or allocate more as necessary.
  *
  * <p> Users of this class are responsible for calling {@link #recycle} on any ImageCoordinate they explicitly create using one of the static factory methods
  * but not any coordinates that they obtain by any other means.  Calling recycle on an ImageCoordinate will return it to the pool of available coordinates.
  * ImageCoordinates are not guaranteed to retain their value after being recycled, so they should not be used after being recycled.
  *
  * <p> This class is partly thread-safe in the sense that multiple threads can use the same pool of ImageCoordinates safely.  However, ImageCoordinate objects themselves
  * are not thread-safe and should not be shared between threads.
  *
  * <p> Arbitrary dimensions are defined by an int identifier, which can be any valid int.  This class defines constants for X,Y,Z,C,T as 0 to 4.
  * When defining additional dimensions, using lower numbers and not leaving gaps will improve performance.
  *
  * @author Colin J. Fuller
  *
  */
 
 public class ImageCoordinate implements java.io.Serializable, Collection<Integer> {
 
 	//TODO: consider whether coordinates should continue to be discrete as they are in the current implementation.
 	
 	public static final int X = 0;
 	public static final int Y = 1;
 	public static final int Z = 2;
 	public static final int C = 3;
 	public static final int T = 4;
 	
 	static final long serialVersionUID = 1L;
 	
 	static final int initialStaticCoordCount = 8;
 	
 	static final int initialDimensionCapacity = 10;
 		
 	private static java.util.Deque<ImageCoordinate> availableStaticCoords;
 	
 	private int[] dimensionCoordinates;
 	private byte[] undefinedCoordinates;
 	private int coordinateCount;
 	
 	private static final byte one = 1;
 	private static final byte zero = 0;
 			
 	static {
 
         synchronized(ImageCoordinate.class) {
 
             availableStaticCoords = new java.util.LinkedList<ImageCoordinate>();
 
             for (int i = 0; i < initialStaticCoordCount; i++) {
                 ImageCoordinate c = new ImageCoordinate();
                 availableStaticCoords.add(c);
 
             }
 
         }
 	}
 	
 	private static ImageCoordinate getNextAvailableCoordinate() {
 		ImageCoordinate coord = null;
 		synchronized(ImageCoordinate.class) {
 			if (! availableStaticCoords.isEmpty()) {
 				coord = availableStaticCoords.removeFirst();
 			}
 		}
 		if (coord == null) {
 			coord = new ImageCoordinate();
 		} else {
 			coord.clear();
 		}
 		return coord;
 	}
 	
 	private ImageCoordinate(){
 		this.dimensionCoordinates = new int[initialDimensionCapacity];
 		this.undefinedCoordinates = new byte[this.dimensionCoordinates.length];
 		java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one);
 		this.coordinateCount = 0;
 		
 	}
 
     /**
      * Gets the x-component of this ImageCoordinate.
      * @return  The x-component as an integer.
      * @deprecated 	Use {@link #get(int)} instead.
      */
 	public int getX(){return this.get(ImageCoordinate.X);}
 
     /**
      * Gets the y-component of this ImageCoordinate.
      * @return  The y-component as an integer.
      * @deprecated 	Use {@link #get(int)} instead.
      */
 	public int getY(){return this.get(ImageCoordinate.Y);}
 
     /**
      * Gets the z-component of this ImageCoordinate.
      * @return  The z-component as an integer.
      * @deprecated 	Use {@link #get(int)} instead.
      */
 	public int getZ(){return this.get(ImageCoordinate.Z);}
 
     /**
      * Gets the c-component of this ImageCoordinate.
      * @return  The c-component as an integer.
      * @deprecated 	Use {@link #get(int)} instead.
      */
 	public int getC(){return this.get(ImageCoordinate.C);}
 
     /**
      * Gets the t-component of this ImageCoordinate.
      * @return  The t-component as an integer.
      * @deprecated 	Use {@link #get(int)} instead.
      */
 	public int getT(){return this.get(ImageCoordinate.T);}
 	
 	
 	/**
 	 * Gets the specified coordinate by index.
 	 * <p>
 	 * The order of coordinates will be the order in which they were set if done manually,
 	 * or the order from the coordinate this ImageCoordinate was cloned from, 
 	 * or otherwise ordered unpredictably (though consistent between calls), probably
 	 * according to hash key.
 	 * <p>
 	 * If the coordinate index is out of bounds, returns 0 instead of throwing an exception.
 	 * 
 	 * @param dimensionConstant	the constant corresponding to the dimension whose value will be retrieved.
 	 * @return					the value of the specified dimension component, or 0 if the index was out of bounds.
 	 * 
 	 */
 	public int get(int dimensionConstant) {
 		if (dimensionConstant < this.dimensionCoordinates.length){
 			return this.dimensionCoordinates[dimensionConstant];
 		}
 		return 0;
 	}
 
 	
 	/**
 	 * Sets the specified coordinate component of the ImageCoordinate by its index.
 	 * 
 	 * @param dimensionConstant		the constant corresponding to the dimension to set.
 	 * @param value					the value to which to set the coordinate.
 	 */
 	public void set(int dimensionConstant, int value) {
 		
 	
 		if (dimensionConstant >= this.dimensionCoordinates.length) {
 			int[] oldCoords = this.dimensionCoordinates;
 			byte[] oldUndefined = this.undefinedCoordinates;
 			int newSize = (dimensionConstant+1 > 2*this.dimensionCoordinates.length) ? dimensionConstant + 1 : 2*this.dimensionCoordinates.length;
 			this.dimensionCoordinates = new int[newSize];
 			this.undefinedCoordinates = new byte[newSize];
 						
 			java.util.Arrays.fill(this.dimensionCoordinates, 0);
 			java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one);
 			
 			for (int i = 0; i < oldCoords.length; i++) {
 				this.dimensionCoordinates[i]= oldCoords[i];
 				this.undefinedCoordinates[i]= oldUndefined[i];
 			}
 		
 		}
 		
 		this.coordinateCount += this.undefinedCoordinates[dimensionConstant];
 		this.undefinedCoordinates[dimensionConstant] = ImageCoordinate.zero;
 		this.dimensionCoordinates[dimensionConstant] = value;
 
 	}
 	
 
     /**
      * Factory method that creates a 5D ImageCoordinate with the specified coordinates.
      *
      * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
      *
      * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
      * make the coordinate available for reuse on other calls of this method.
      *
      * @param x     The x-component of the ImageCoordinate to be created.
      * @param y     The y-component of the ImageCoordinate to be created.
      * @param z     The z-component of the ImageCoordinate to be created.
      * @param c     The c-component of the ImageCoordinate to be created.
      * @param t     The t-component of the ImageCoordinate to be created.
      * @return      An ImageCoordinate whose components have been set to the specified values.
      */
 	public static ImageCoordinate createCoordXYZCT(int x, int y, int z, int c, int t) {
 		
 		ImageCoordinate staticCoord = ImageCoordinate.getNextAvailableCoordinate();
 		
 		//setting T first avoids multiple allocations of internal storage for new coordinates 
 		staticCoord.set(ImageCoordinate.T, t);
 		staticCoord.set(ImageCoordinate.X, x);
 		staticCoord.set(ImageCoordinate.Y, y);
 		staticCoord.set(ImageCoordinate.Z, z);
 		staticCoord.set(ImageCoordinate.C, c);
 
 		return staticCoord;
 		
 	}
 	
 	/**
      * Factory method that creates a 5D ImageCoordinate with the specified coordinates.
      *
      * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
      *
      * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
      * make the coordinate available for reuse on other calls of this method.
      *
      * @param x     The x-component of the ImageCoordinate to be created.
      * @param y     The y-component of the ImageCoordinate to be created.
      * @param z     The z-component of the ImageCoordinate to be created.
      * @param c     The c-component of the ImageCoordinate to be created.
      * @param t     The t-component of the ImageCoordinate to be created.
      * @return      An ImageCoordinate whose components have been set to the specified values.
      * @deprecated	use {@link #createCoordXYZCT(int, int, int, int, int)} instead.
      */
 	public static ImageCoordinate createCoord(int x, int y, int z, int c, int t) {
 		
 		return ImageCoordinate.createCoordXYZCT(x, y, z, c, t);
 		
 	}
 	
 	/**
      * Factory method that creates a zero-dimensional ImageCoordinate that can be manually extended.
      *
      * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
      *
      * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
      * make the coordinate available for reuse on other calls of this method.
      *
      * @return      An ImageCoordinate with no specified dimensions.
      */
 	public static ImageCoordinate createCoord() {
 		return ImageCoordinate.getNextAvailableCoordinate();
 	}
 
 
     /**
      * Factory method that creates and ImageCoordinate that represents the same position in coordinate space as the supplied coordinate.
      *
      * Users of this method should call {@link #recycle} on the ImageCoordinate returned by this method when finished with it in order to
      * make the coordinate available for reuse on other calls of this method.
      *
      * @param toClone   The ImageCoordinate to clone.
      * @return          An ImageCoordinate whose components are set to the same values as the components of the supplied ImageCoordinate.
      */
     public static ImageCoordinate cloneCoord(ImageCoordinate toClone) {
     	
     	ImageCoordinate newCoord = ImageCoordinate.getNextAvailableCoordinate();
     	
     	newCoord.setCoord(toClone);
     	
     	return newCoord;
     	
     }
 
     /**
      * Returns an ImageCoordinate to the pool of ImageCoordinates available for reuse.
      *<p>
      * This method should be called on an ImageCoordinate if and only if that coordinate was obtained using one of the static factory methods
      * ({@link #createCoord} or {@link #cloneCoord}).
      *<p>
      * Each ImageCoordinate must only be recycled once, or else future calls to the factory methods may return the same coordinate multiple times with
      * different values, which will lead to unpredictable behavior.  No checking is done to ensure that each coordinate was only recycled once (this was found
      * to cause enough overhead that the benefits of reusing objects were cancelled out).
      *
      *
      */
 	public void recycle() {
 
         synchronized(ImageCoordinate.class) {
 
             availableStaticCoords.add(this);
 
         }
 	}
 	
 	/**
 	 * Gets the dimension of the ImageCoordinate: that is, the number of defined named dimensions
 	 * it contains.
 	 * 
 	 * @return		the dimension of the ImageCoordinate.
 	 */
 	public int getDimension() {
 		return this.coordinateCount;
 	}
 	
 	/**
 	 * Gets the defined index of the specified coordinate.
 	 * @param i		The coordinate to retrieve (i.e. the value i such that the ith index that has been assigned a value will be returned.)
 	 * @return		The index of the ith component.
 	 */
 	public Integer getDefinedIndex(int i) {
 		int c = -1;
 		for (int j = 0; j < this.undefinedCoordinates.length; j++) {
 			c+= 1-this.undefinedCoordinates[j];
 			if (c == i) return j;
 		}
 		throw new NoSuchElementException("The specified coordinate is not defined: " + i + ".  Number of defined coordinates: " + this.coordinateCount);
 	}
 
     /**
      * Converts the ImageCoordinate to a string representation.
      * <p>
      * The returned string will be "(a,b,c,d,e)", where a through e 
      * are placeholders for the actual coordinates of the ImageCoordinate.
      * @return  The string representation of the ImageCoordinate.
      */
 	public String toString() {
 		if (this.getDimension() == 0) {
 			return "()";
 		}
		String valueString = "(";
		for (Integer i : this) {
			valueString+= this.get(i) + ", ";
 		}
		valueString = valueString.substring(0, valueString.length() - 2); // strip off the last ", "
 		valueString += ")";
 		return valueString;
 	}
 
     /**
      * Sets all the components of the ImageCoordinate to the specified values.
      * 
      * @param x     The new x-component of the ImageCoordinate.
      * @param y     The new y-component of the ImageCoordinate.
      * @param z     The new z-component of the ImageCoordinate.
      * @param c     The new c-component of the ImageCoordinate.
      * @param t     The new t-component of the ImageCoordinate.
      */
 	public void setCoordXYZCT(int x, int y, int z, int c, int t) {
 		this.set(ImageCoordinate.X, x);
 		this.set(ImageCoordinate.Y, x);
 		this.set(ImageCoordinate.Z, x);
 		this.set(ImageCoordinate.C, x);
 		this.set(ImageCoordinate.T, x);
 	}
 	
     /**
      * Sets all the components of the ImageCoordinate to the specified values.
      * 
      * @param x     The new x-component of the ImageCoordinate.
      * @param y     The new y-component of the ImageCoordinate.
      * @param z     The new z-component of the ImageCoordinate.
      * @param c     The new c-component of the ImageCoordinate.
      * @param t     The new t-component of the ImageCoordinate.
      * @deprecated	use {@link #setCoordXYZCT(int, int, int, int, int)} instead.
      */
 	public void setCoord(int x, int y, int z, int c, int t) {
 		this.set(ImageCoordinate.X, x);
 		this.set(ImageCoordinate.Y, x);
 		this.set(ImageCoordinate.Z, x);
 		this.set(ImageCoordinate.C, x);
 		this.set(ImageCoordinate.T, x);
 	}
 
     /**
      * Sets the components of the ImageCoordinate to the values of the components of another ImageCoordinate.
      * 
      * @param other     The ImageCoordinate whose component values will be copied.
      */
     public void setCoord(ImageCoordinate other) {
     	this.clear();
     	for (Integer i : other) {
     		this.set(i, other.get(i));
     	}
     }
     
     /**
      * Clears all stored coordinate dimensions and values.
      * <p>
      * After calling this method, the ImageCoordinate will be zero-dimensional.
      * 
      */
     public void clear() {
     	java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one);
     	this.coordinateCount = 0;
     }
 
     /**
      * Sets the x-component of the ImageCoordinate to the specified value.
      * @param x The new x-component of the ImageCoordinate.
      * @deprecated use {@link #set(int, int)} instead.
      */
 	public void setX(int x) {
 		this.set(ImageCoordinate.X, x);
 	}
 
     /**
      * Sets the y-component of the ImageCoordinate to the specified value.
      * @param y The new y-component of the ImageCoordinate.
      * @deprecated use {@link #set(int, int)} instead.
      */
 	public void setY(int y) {
 		this.set(ImageCoordinate.Y, y);
 	}
 
     /**
      * Sets the z-component of the ImageCoordinate to the specified value.
      * @param z The new z-component of the ImageCoordinate.
      * @deprecated use {@link #set(int, int)} instead.
      */
 	public void setZ(int z) {
 		this.set(ImageCoordinate.Z, z);
 	}
 
     /**
      * Sets the c-component of the ImageCoordinate to the specified value.
      * @param c The new c-component of the ImageCoordinate.
      * @deprecated use {@link #set(int, int)} instead.
      */
 	public void setC(int c) {
 		this.set(ImageCoordinate.C, c);
 	}
 
     /**
      * Sets the t-component of the ImageCoordinate to the specified value.
      * @param t The new t-component of the ImageCoordinate.
      * @deprecated use {@link #set(int, int)} instead.
      */
 	public void setT(int t) {
 		this.set(ImageCoordinate.T, t);
 	}
 	
 	/**
 	 * Collection interface methods.
 	 */
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#add(java.lang.Object)
 	 */
 	@Override
 	public boolean add(Integer arg0) {
 		throw new UnsupportedOperationException("Add not supported for ImageCoordinates.");
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#addAll(java.util.Collection)
 	 */
 	@Override
 	public boolean addAll(Collection<? extends Integer> arg0) {
 		throw new UnsupportedOperationException("Add not supported for ImageCoordinates.");
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#contains(java.lang.Object)
 	 */
 	@Override
 	public boolean contains(Object arg0) {
 		Integer iArg = (Integer) arg0;
 		for (Integer i : this) {
 			if (i == iArg) return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#containsAll(java.util.Collection)
 	 */
 	@Override
 	public boolean containsAll(Collection<?> arg0) {
 		for (Object o : arg0) {
 			if (!this.contains(o)) return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#isEmpty()
 	 */
 	@Override
 	public boolean isEmpty() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#iterator()
 	 */
 	@Override
 	public Iterator<Integer> iterator() {
 		return new ImageCoordinateIterator(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#remove(java.lang.Object)
 	 */
 	@Override
 	public boolean remove(Object arg0) {
 		throw new UnsupportedOperationException("Remove not supported for ImageCoordinates.");
 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#removeAll(java.util.Collection)
 	 */
 	@Override
 	public boolean removeAll(Collection<?> arg0) {
 		throw new UnsupportedOperationException("Remove not supported for ImageCoordinates.");
 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#retainAll(java.util.Collection)
 	 */
 	@Override
 	public boolean retainAll(Collection<?> arg0) {
 		throw new UnsupportedOperationException("Retain not supported for ImageCoordinates.");
 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#size()
 	 */
 	@Override
 	public int size() {
 		return this.getDimension();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#toArray()
 	 */
 	@Override
 	public Object[] toArray() {
 		return toArray(new Object[0]);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Collection#toArray(T[])
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> T[] toArray(T[] arg0) {
 		
 		T[] toFill = null;
 		
 		if (arg0.length >= this.coordinateCount) {
 			toFill = arg0;
 		} else {
 			toFill = (T[]) new Integer[this.coordinateCount];
 		}
 		int c = 0;
 		for (Integer i : this) {
 			arg0[c++] = (T) (Integer) this.get(i);
 		}
 		return toFill;
 		
 	}
 	
 	private static class ImageCoordinateIterator implements Iterator<Integer> {
 		
 		int currentIndex;
 		int currentDefinedCount;
 		ImageCoordinate ic;
 				
 		public ImageCoordinateIterator(ImageCoordinate ic) {
 			this.currentIndex = 0;
 			this.currentDefinedCount = 0;
 			this.ic = ic;
 		}
 		
 		public boolean hasNext() {
 			return (this.currentDefinedCount < this.ic.getDimension());
 		}
 		
 		public Integer next() {	
 			
 					
 			while(this.hasNext()) {
 				
 				if (ic.undefinedCoordinates[this.currentIndex++] == 0) {
 					this.currentDefinedCount+=1;
 					return this.currentIndex-1;
 				}
 				
 			}
 			
 			throw new NoSuchElementException("No more elements in ImageCoordinate.");
 			
 		}
 		
 		public void remove() {
 			throw new UnsupportedOperationException("Remove not supported for ImageCoordinate.");
 		}
 		
 	}
 	
 	
 }
