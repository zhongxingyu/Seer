import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
 
 
 public class ArrayLinearList<T> implements LinearList<T>, Iterable<T> 
 {
 	protected T[] element;
 	protected int size;
 	
 	@SuppressWarnings("unchecked")
 	public ArrayLinearList( int initialCapacity )
 	{
 		if(initialCapacity < 1)
 			throw new IllegalArgumentException("initialCpacity must be >= 1");
 		element = ( T[] ) new Object[initialCapacity];
 		size = 0;
 	}
 	public ArrayLinearList( )
 	{
 		this(10);
 	}
 	
 	/**
 	 * @ return true if list is empty
 	 **/
 	public boolean isEmpty()
 	{
 		return size == 0;
 	}
 	
 	/**
 	 * @return current number of elements in list
 	 **/
 	public int size()
 	{
 		return size;
 	}
 	
 	/**
 	 * @throws IndexOutOfBoundsException if index is not between 0 and size - 1 
 	 * @param index
 	 **/
 	void checkIndex(int index)
 	{
 		if(index < 0 || index >= size)
 			throw new IndexOutOfBoundsException("index = " + index + "  size = " + size);
 	}
 	
 	/**
 	 * @return element with specified index
 	 * @throws INdexOutOfBoundsException if index is not between 0 and size - 1
 	 * @param index
 	 **/
 	public T get(int index)
 	{
 		checkIndex(index);
 		return element[index];
 	}
 	
 	/**
 	 * @return index of first occurrence of theElement, return -1 if the Element is not in list
 	 * @param theElement
 	 **/
 	public int indexOf(T theElement)
 	{
 		for(int i = 0; i < size; i++)
 			if(element[i].equals(theElement))
 				return i;
 			return -1;
 	}
 	
 	/** 
 	 * Remove the element with specified index.
 	 * All elements with higher index have their index reduced by 1.
 	 * @throws IndexOutOfBoundsException when index is not between 0 and size - 1
 	 * @return removed element 
 	 **/
 	public T remove(int index)
 	{
 		checkIndex(index);
 		
 		T removedElement = element[index];
 		for(int i = index + 1; i < size; i++)
 		{
 			element[i + 1] = element[i];
 		}
 		element[--size] = null;
 		return removedElement;
 	}
 	
 	/** 
 	 * Insert an element with specified index.
 	 * All elements with equal or higher index
 	 * have their index increased by 1.
 	 * @throws IndexOutOfBoundsException when
 	 * index is not between 0 and size 
 	 **/
 	@SuppressWarnings("unchecked")
 	public void add(int index, T theElement)
 	{
 		if(index < 0 || index > size)
 			throw new IndexOutOfBoundsException("index = " + index + "  size = " + size);
 		
 		if(size == element.length)
 		{
 			T[] old = element;
 			element = ( T[] ) new Object[2 * size];
 			System.arraycopy(old, 0, element, 0, size);
 		}
 		
 		for(int i = size - 1; i >= index; i--)
 			element[i + 1] = element[i];
 		
 		element[index] = theElement;
 		
 		size++;
 	}
 	
 	/**
 	 * Convert to a string
 	 **/
 	public String toString()
 	{
 		StringBuilder s = new StringBuilder("[");
 		
 		for(T x : this)
 			s.append(Objects.toString(x) + ", ");
 		
 		if(size > 0)
 			s.setLength(s.length() - 2);
 		
 		s.append("]");
 		
 		return new String(s);
 	}
 	
 
 	public Iterator<T> iterator()
 	{
 		return new ArrayLinearListIterator<T>(this);
 	}
 	
 	@SuppressWarnings("hiding")
 	private class ArrayLinearListIterator<T> implements Iterator<T>
 	{
 		private ArrayLinearList<T> list;
 		private int nextIndex;
 		
 		public ArrayLinearListIterator(ArrayLinearList<T> theList)
 		{
 			list = theList;
 			nextIndex = 0;
 		}
 		
 		/** 
 		 * @return true iff the list has a next element 
 		 **/
 		public boolean hasNext()
 		{
 			return nextIndex < list.size;
 		}
 		
 		/** @return next element in list
 		 * @throws NoSuchElementException
 		 * when there is no next element 
 		 **/
 		public T next()
 		{
 			if(nextIndex < list.size)
 				return list.element[nextIndex++];
 			else
 				throw new NoSuchElementException("No next element");
 		}
 		
 		/**
 		 * Unsupported method
 		 */
 		public void remove()
 		{
 			throw new UnsupportedOperationException("remove not supported");
 		}
 	}
 	
 	public static void main ( String[] args )
 	{
 	    // test default constructor
 	    ArrayLinearList<Integer> x = new ArrayLinearList<>( );
 	   // test size
 	   System.out.println( "Initial size is " + x.size( ) );
 	   // test isEmpty
 	   if( x.isEmpty( ) )
 	   System.out.println( "The list is empty" );
 	   else System.out.println( "The list is not empty" );
 	   // test put
 	   x.add( 0, new Integer( 2 ) );
 	   x.add( 1, new Integer( 6 ) );
 	   x.add( 0, new Integer( 1 ) );
 	   x.add( 2, new Integer( 4 ) );
 	   // test toString
 	   System.out.println( "The list is " + x );
 		// output using an iterator
 		Iterator y = x.iterator( );
 		while( y.hasNext( ) )
 		System.out.print( y.next( ) + " " );
 		System.out.println( );
 		// test indexOf
 		int index = x.indexOf( new Integer( 4 ) );
 		if( index < 0 )
 			System.out.println( "4 not found" );
 		else 
 			System.out.println( "The index of 4 is " + index );
 		index = x.indexOf( new Integer(3) );
 		if( index < 0 )
 			System.out.println( "3 not found" );
 		else 
 			System.out.println( "The index of 3 is " + index );
 		// test get
 		System.out.println( "Element at 0 is " + x.get( 0 ) );
 		System.out.println( "Element at 3 is " + x.get( 3 ) );
 		// test remove
 		System.out.println( x.remove( 1 ) + " removed" );
 		System.out.println( "The list is " + x );
 		System.out.println( x.remove( 2 ) + " removed" );
 		System.out.println( "The list is " + x );
 		if( x.isEmpty( ) )
 			System.out.println( "The list is empty" );
 		else System.out.println( "The list is not empty" );
 			System.out.println( "List size is " + x.size( ) );
 	}
 }
