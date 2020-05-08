 /*
 
 Copyright: 2010 Purdue University
 
 This file is distributed under the following terms (MIT/X11 License):
 
 	Permission is hereby granted, free of charge, to any person
 	obtaining a copy of this file and associated documentation
 	files (the "Software"), to deal in the Software without
 	restriction, including without limitation the rights to use,
 	copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the
 	Software is furnished to do so, subject to the following
 	conditions:
 
 	The above copyright notice and this permission notice shall be
 	included in all copies or substantial portions of the Software.
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 	OTHER DEALINGS IN THE SOFTWARE.
 
 */
 package edu.purdue.bbc.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * A class for storing primitive wrappers in a List that can easily be
  * converted to an array of the primitive counterparts
  */
 public class NumberList extends ArrayList<Number> {
 	private byte[] byteArray;
 	private double[] doubleArray;
 	private float[] floatArray;
 	private int[] intArray;
 	private long[] longArray;
 	private short[] shortArray;
 
 	/**
 	 * Creates a new empty NumberList
 	 */
 	public NumberList ( ) {
 		super( );
 	}
 
 	/**
 	 * Creates a new NumberList which contains the Numbers from the passed
 	 * in collection.
 	 * 
 	 * @param c The collection to obtain new objects from.
 	 */
 	public NumberList( Collection<Number> c ) {
 		super( c );
 	}
 	
 	/**
 	 * Creates a new NumberList which contains the values from the passed
 	 * in array.
 	 * 
 	 * @param values The values to initialize the NumberList with.
 	 */
 	public NumberList( byte [] values ) {
 		super( );
 		this.addAll( values );
 	}
 
 	/**
 	 * Creates a new NumberList which contains the values from the passed
 	 * in array.
 	 * 
 	 * @param values The values to initialize the NumberList with.
 	 */
 	public NumberList( double [] values ) {
 		super( );
 		this.addAll( values );
 	}
 	/**
 	 * Creates a new NumberList which contains the values from the passed
 	 * in array.
 	 * 
 	 * @param values The values to initialize the NumberList with.
 	 */
 	public NumberList( float [] values ) {
 		super( );
 		this.addAll( values );
 	}
 	/**
 	 * Creates a new NumberList which contains the values from the passed
 	 * in array.
 	 * 
 	 * @param values The values to initialize the NumberList with.
 	 */
 	public NumberList( int [] values ) {
 		super( );
 		this.addAll( values );
 	}
 	/**
 	 * Creates a new NumberList which contains the values from the passed
 	 * in array.
 	 * 
 	 * @param values The values to initialize the NumberList with.
 	 */
 	public NumberList( long [] values ) {
 		super( );
 		this.addAll( values );
 	}
 
 	/**
 	 * Creates a new numberList with a given initial capacity. It will be
 	 * expanded as needed.
 	 * 
 	 * @param initialCapacity The initial capacity of the List.
 	 */
 	public NumberList( int initialCapacity ) {
 		super( initialCapacity );
 	}
 
 	/**
 	 * Adds a new Number to the List
 	 * 
 	 * @param e The new Number to be added.
 	 * @return true if the append was successful.
 	 */
 	public boolean add( Number e ) {
 		this.clearCache( );
 		return super.add( e );
 	}
 
 	/**
 	 * Adds a new Byte object to the List.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( byte value ) {
 		return this.add( new Byte( value ));
 	}
 
 	/**
 	 * Adds a new Double object to the List.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( double value ) {
 		return this.add( new Double( value ));
 	}
 
 	/**
 	 * Adds a new Float object to the List.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( float value ) {
 		return this.add( new Float( value ));
 	}
 
 	/**
 	 * Adds a new Integer object to the List.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( int value ) {
 		return this.add( new Integer( value ));
 	}
 
 	/**
 	 * Adds a new Long object to the List.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( long value ) {
 		return this.add( new Long( value ));
 	}
 
 	/**
 	 * Adds a new Short object to the list.
 	 * 
 	 * @param value The new value to be added.
 	 * @return true if adding the value was accepted.
 	 */
 	public boolean add( short value ) {
 		return this.add( new Short( value ));
 	}
 
 	/**
 	 * Adds a new Number object at the specified location in the List.
 	 * 
 	 * @param element The new value to be added.
 	 */
 	public void add( int index, Number element ) {
 		super.add( index, element );
 		this.clearCache( );
 	}
 
 	/**
 	 * Adds a new Byte object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, byte value ) {
 		this.add( index, new Byte( value ));
 	}
 
 	/**
 	 * Adds a new Double object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, double value ) {
 		this.add( index, new Double( value ));
 	}
 
 	/**
 	 * Adds a new Double object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, float value ) {
 		this.add( index, new Float( value ));
 	}
 
 	/**
 	 * Adds a new Integer object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, int value ) {
 		this.add( index, new Integer( value ));
 	}
 
 	/**
 	 * Adds a new Long object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, long value ) {
 		this.add( index, new Long( value ));
 	}
 
 	/**
 	 * Adds a new Short object at the specified position in the List.
 	 * 
 	 * @param value The new value to be added.
 	 */
 	public void add( int index, short value ) {
 		this.add( index, new Short( value ));
 	}
 
 	/**
 	 * Adds all values in the array to this list
 	 * 
 	 * @param values An array containing the values to be added.
 	 */
 	public void addAll( byte[] values ) {
 		for ( byte value : values ) {
 			this.add( value );
 		}
 	}
 
 	/**
 	 * Adds all values in the array to this list
 	 * 
 	 * @param values An array containing the values to be added.
 	 */
 	public void addAll( double[] values ) {
 		for ( double value : values ) {
 			this.add( value );
 		}
 	}
 
 	/**
 	 * Adds all values in the array to this list
 	 * 
 	 * @param values An array containing the values to be added.
 	 */
 	public void addAll( float[] values ) {
 		for ( float value : values ) {
 			this.add( value );
 		}
 	}
 
 	/**
 	 * Adds all values in the array to this list
 	 * 
 	 * @param values An array containing the values to be added.
 	 */
 	public void addAll( int[] values ) {
 		for ( int value : values ) {
 			this.add( value );
 		}
 	}
 
 	/**
 	 * Adds all values in the array to this list
 	 * 
 	 * @param values An array containing the values to be added.
 	 */
 	public void addAll( long[] values ) {
 		for ( long value : values ) {
 			this.add( value );
 		}
 	}
 
 	public boolean addAll( Collection<? extends Number> c ) {
 		this.clearCache( );
 		return super.addAll( c );
 	}
 
 	/**
 	 * Clears all of the cached values in the private arrays. This should be
 	 * called any time the list is modified. The cache will be rebuilt as needed.
 	 */
 	private void clearCache( ) {
 		this.byteArray = null;
 		this.doubleArray = null;
 		this.floatArray = null;
 		this.intArray = null;
 		this.longArray = null;
 		this.shortArray = null;
 	}
 
 	/**
 	 * Converts this List to an array of bytes.
 	 * 
 	 * @return A byte array containing all values of this List.
 	 */
 	public byte[] toByteArray( ) {
 		if ( byteArray == null || this.byteArray.length != this.size( )) {
 			byteArray = new byte[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			byteArray[ i++ ]  = t.byteValue( );
 		}
 		return this.byteArray;
 	}
 
 	/**
 	 * Converts this List to an array of doubles.
 	 * 
 	 * @return A double array containing all values of this List. 
 	 */
 	public double[] toDoubleArray( ) {
 		if ( this.doubleArray == null || this.doubleArray.length != this.size( )) {
 			this.doubleArray = new double[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			if ( t != null ) {
 				this.doubleArray[ i++ ]  = t.doubleValue( );
 			} else {
 				this.doubleArray[ i++ ] = Double.NaN;
 			}
 		}
 		return this.doubleArray;
 	}
 
 	/**
 	 * Converts this List to an array of floats.
 	 * 
 	 * @return A float array containing all values of this List.
 	 */
 	public float[] toFloatArray( ) {
 		if ( this.floatArray == null || this.floatArray.length != this.size( )) {
 			this.floatArray = new float[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			if ( t != null ) {
 				this.floatArray[ i++ ]  = t.floatValue( );
 			} else {
 				this.floatArray[ i++ ] = Float.NaN;
 			}
 		}
 		return this.floatArray;
 	}
 
 	/**
 	 * Converts this List an array of ints.
 	 * 
 	 * @return An int array containing all values of this List.
 	 */
 	public int[] toIntArray( ) {
 		if ( this.intArray == null || this.intArray.length != this.size( )) {
 			this.intArray = new int[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			if ( t != null ) {
 				this.intArray[ i++ ]  = t.intValue( );
 			} else {
 				this.intArray[ i++ ] = 0;
 			}
 		}
 		return this.intArray;
 	}
 
 	/**
 	 * Converts this List to an array of longs.
 	 * 
 	 * @return A long array containing all values of this List.
 	 */
 	public long[] toLongArray( ) {
 		if ( this.longArray == null || this.longArray.length != this.size( )) {
 			this.longArray = new long[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			if ( t != null ) {
 				this.longArray[ i++ ]  = t.longValue( );
 			} else {
 				this.longArray[ i++ ] = 0L;
 			}
 		}
 		return this.longArray;
 	}
 
 	/**
 	 * Converts this List to an array of shorts.
 	 * 
 	 * @return A short array containing all values of this List.
 	 */
 	public short[] toShortArray( ) {
 		if ( this.shortArray == null || this.shortArray.length != this.size( )) {
 			this.shortArray = new short[ this.size( )];
 		}
 		int i=0;
 		for( Number t : this ) {
 			if ( t != null ) {
 				this.shortArray[ i++ ]  = t.shortValue( );
 			} else {
 				this.shortArray[ i++ ] = (short)0; 
 			}
 		}
 		return this.shortArray;
 	}
 
 	/**
 	 * Removes the object at the specified position in the List.
 	 * @see java.util.ArrayList#remove(int)
 	 * 
 	 * @param index The index of the object to be removed.
 	 * @return The Number which was removed from the List.
 	 */
 	public Number remove( int index ) {
 		this.clearCache( );
 		return super.remove( index );
 	}
 
 	/**
 	 * Removes the first occurrence of the specified element from this List, if 
 	 * it is present.
 	 * @see java.util.ArrayList#remove(java.lang.Object)
 	 * 
 	 * @param o The object to be removed.
 	 * @return true if the object was found and removed.
 	 */
 	public boolean remove( Object o ) {
 		this.clearCache( );
 		return super.remove( o );
 	}
 
 	/**
 	 * Removes all elements in the specified range.
 	 * @see java.util.ArrayList#removeRange(int, int)
 	 * 
 	 * @param fromIndex The index of the first element to remove (inclusive)
 	 * @param toIndex The index after the last element to remove (non-inclusive)
 	 */
 	protected void removeRange( int fromIndex, int toIndex ) {
 		super.removeRange( fromIndex, toIndex );
 		this.clearCache( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * @see java.util.ArrayList#set(int,E)
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param element The new value for the index.
 	 * @return The old value of that index.
 	 */
 	public Number set( int index, Number element ) {
 		Number returnValue = super.set( index, element );
 		this.clearCache( );
 		return returnValue;
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as a byte.
 	 */
 	public byte set( int index, byte value ) {
 		return this.set( index, new Byte( value )).byteValue( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as a double.
 	 */
 	public double set( int index, double value ) {
 		return this.set( index, new Double( value )).doubleValue( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as a float.
 	 */
 	public float set( int index, float value ) {
 		return this.set( index, new Float( value )).floatValue( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as an int.
 	 */
 	public int set( int index, int value ) {
 		return this.set( index, new Double( value )).intValue( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as a long
 	 */
 	public long set( int index, long value ) {
 		return this.set( index, new Long( value )).longValue( );
 	}
 
 	/**
 	 * Replaces the element at the specified position in this List with the
 	 * specified value.
 	 * 
 	 * @param index The index of the value to be replaced.
 	 * @param value The new value for the specified index.
 	 * @return The old value of that index as a short.
 	 */
 	public short set( int index, short value ) {
 		return this.set( index, new Short( value )).shortValue( );
 	}
 
 	/**
 	 * Returns the mean value for this list as a Double.
 	 * 
 	 * @return The mean value.
 	 */
 	public Double getMean( ) {
 		double total = 0;
 		int items = 0;
 		for ( Number value : this ) {
 			if ( value != null && !Double.isNaN( value.doubleValue( ))) {
 				total += value.doubleValue( );
 				items++;
 			}
 		}
 		return new Double( total / items );
 	}
 
 	/**
 	 * Returns the sum of all values in this list as a Double.
 	 * 
 	 * @return The sum of the list values.
 	 */
 	public Double getSum( ) {
 		double total = 0;
 		for ( Number value : this ) {
 			if ( value != null && !Double.isNaN( value.doubleValue( ))) {
 				total += value.doubleValue( );
 			}
 		}
 		return new Double( total );
 	}
 
 	/**
 	 * Returns the median of the values in this list as a Double.
 	 * 
 	 * @return The median of the values in this list.
 	 */
 	public Double getMedian( ) {
 		int items = 0;
 		double[] values = new double[ this.size( )];
 		for ( Number value : this ) {
 			if ( value != null && !Double.isNaN( value.doubleValue( ))) {
 				values[ items++ ] = value.doubleValue( );
 			}
 		}
 		Arrays.sort( values, 0, items );
 		if ( items % 2 == 0 )
 			return new Double( ( values[ items / 2 ] + values[ items / 2 - 1 ]) / 2 );
 		else
 			return new Double( values[ items / 2 ] );
 	}
 
 	/**
 	 * Gets the minimum value from this NumberList.
 	 * 
 	 * @return The minimum value.
 	 */
 	public Number getMin( ) {
 		Number returnValue = new Double( Double.POSITIVE_INFINITY );
 		for ( Number n : this ) {
 			if ( Double.compare( n.doubleValue( ), returnValue.doubleValue( )) < 0 )
 				returnValue = n;
 		}
 		return returnValue;
 	}
 
 	/**
 	 * Gets the maximum value from this NumberList.
 	 * 
 	 * @return The maximum value.
 	 */
 	public Number getMax( ) {
 		Number returnValue = new Double( Double.NEGATIVE_INFINITY );
 		for ( Number n : this ) {
			if ( Double.compare( n.doubleValue( ), returnValue.doubleValue( )) > 0 )
 				returnValue = n;
 		}
 		return returnValue;
 	}
 
 	/**
 	 * Gets the range spanned by this NumberList.
 	 * 
 	 * @return A range which includes every value in this NumberList.
 	 */
 	public Range getRange( ) {
 		Number min = new Double( Double.POSITIVE_INFINITY );
 		Number max = new Double( Double.NEGATIVE_INFINITY );
 		for ( Number n : this ) {
			if ( Double.compare( n.doubleValue( ), max.doubleValue( )) > 0 )
 				max = n;
 			if ( Double.compare( n.doubleValue( ), min.doubleValue( )) < 0 )
 				min = n;
 		}
 		return new Range( min.doubleValue( ), max.doubleValue( ));
 	}
 
 }
 
