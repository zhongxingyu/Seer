 package bht.tools.util;
 
import com.sun.istack.internal.logging.Logger;
 import java.awt.Window;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import javax.swing.AbstractListModel;
 import javax.swing.ListModel;
 
 /**
  * <strong>Copyright Blue Husky Programming, © 2011</strong> GPLv3<br/>
  * Would have been called "Array++" if "+" were an acceptable character. This class is made to give enhanced features to arrays,
  * such as basic search functions and comparability, along with the standard accessing methods, such as {@link get(int)} and
  * {@link set(int, Object)}. The internal structure is an array, so as to make the interactions as <b>memory-efficient</b>
  * as possible.<br/>
  * <br/>Many array-management methods return {@code this}, so as to allow daisy-chaining of commands, like you can with
  * {@code String}s.<br/>
  * <h3>Examples:</h3> {@code String} operation: {@code String s = oldString.replace(" i "," I ").trim();}<br/> {@code Array++}
  * operation: {@code ArrayPP<Character> a = charArrayPP.replace(" i ", " I ").trim();}
  *
  * @param <T> The type of array with which this object will be dealing.
  * <h3>Examples:</h3>
  * <ul>
  *	<li>{@code ArrayPP<String> strings = new ArrayPP();}
  *		<ul><li>A new array of {@code String}s. Analogous to {@code String[] strings;}</li></ul>
  *	</li>
  *	<li>{@code ArrayPP strings = new ArrayPP<String>(args);}
  *		<ul><li>A clone of an array of {@code String}s. Analogous to {@code String[] strings = args;}</li></ul>
  *	</li>
  *	<li>{@code String[] newArgs = new ArrayPP<String>(args).trim().add("Java").toArray();}
  *		<ul>
  *			<li>A clone of an array of {@code String}s. Analogous to {@code String[] strings = args;}, where all null values in{@code args} have been removed and {@code "Java"} has been placed on the end.</li>
  *		</ul>
  *	</li>
  * </ul>
  * @author Supuhstar of Blue Husky Programming
  * @version 1.7.0
  * @since 1.6_24
  * @see <a href="http://download.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html">arrays</a>
  */
 public class ArrayPP<T> implements Comparable<ArrayPP<?>>, Iterable<T>, Cloneable
 {
 	/**
 	 * The contents of the array. Feel free to directly interact; the methods of this class are designed not to care!
 	 */
 	public T[] t;
 
 //  /**
 //   * Creates a new Array++, without anything in it.
 //   * <h5>Analogy:</h5>
 //   * {@code ArrayPP&lt;String&gt; s = new ArrayPP();}<br/>
 //   * is analogous to<br/>
 //   * {@code String s[] = new String[]{};}
 //   */
 //   * @deprecated This may lead to {@code ClassCastException}s. Please use {@code ArrayPP(T... array)}
 //  public ArrayPP()//Commented out because ArrayPP(T... array) does the same thing on March 5, 2012 (1.4.14) for Marian
 //  {
 //    this(0);
 //  }
 
 	/**
 	 * Creates a new Array++ of the given size, without anything in it.
 	 * <h5>Analogy:</h5> {@code ArrayPP&lt;String&gt; s = new ArrayPP(5);}<br/>
 	 * is analogous to<br/> {@code String s[] = new String[5];}
 	 *
 	 * @param init the initial size of the array.
 	 * @deprecated The buffer of empty spaces is never used and simply stays at the front of the array unless a method like
 	 * {@link #trim()} is called. Only useful in conjunction with {@link #set(int, Object)} methods
 	 * @throws NegativeArraySizeException if the size provided is less than 0
 	 */
 //   * @deprecated This might lead to {@code ClassCastException}s. Please use {@code ArrayPP(T... array)}
 	@SuppressWarnings("OverridableMethodCallInConstructor")
 	public ArrayPP(int init) throws NegativeArraySizeException
 	{
 		if (init < 0)
 			throw new NegativeArraySizeException(init + " is less than 0 (the minimum array size)");
 
 		t = (T[]) new Object[init];/*ArrayPP<T>(null, null).clear().toArray();
     
 		 for (int i=0; i < init; i++)
 		 add(null);*/
 	}
 
 	/**
 	 * Creates a new Array++ to manage the given array.
 	 * <h5>Analogy:</h5> {@code ArrayPP&lt;String&gt; s = new ArrayPP(args);}<br/>
 	 * is analogous to<br/> {@code String s[] = System.arraycopy(args);}
 	 *
 	 * @param array The array to be managed
 	 */
 	@SuppressWarnings(
 			{
 		"null", "ConstantConditions"
 	})
 	public ArrayPP(T... array)
 	{
 		boolean n = array == null;
 		t = (T[]) java.util.Arrays.copyOf(n ? new Object[]
 		{
 		} : array, n ? 0 : array.length);
 	}
 
 	/**
 	 * Creates a new array which contains all the elements which are contained within the given {@link Enumeration}
 	 *
 	 * @param e the {@link Enumeration} whose contents will be placed in this array.
 	 * @since June 21, 2012 (1.5) for Minecraft Texture Masher
 	 */
 	@SuppressWarnings("OverridableMethodCallInConstructor")
 	public ArrayPP(Enumeration<T> e)
 	{
 		this();
 		while (e.hasMoreElements())
 			add(e.nextElement());
 	}
 
 	/**
 	 * A convenience constructor that creates an {@code ArrayPP} out of the values in the provided {@code Iterable}
 	 *
 	 * @param array the {@code Iterable} of values that will be added to this array
 	 */
 	@SuppressWarnings(
 	{
 		"empty-statement", "OverridableMethodCallInConstructor"
 	})
 	public ArrayPP(Iterable<T> array)
 	{
 		this();
 //    t = new ArrayPP<T>().toArray();
 		for (T t1 : array)
 			add(t1);
 	}
 
 	/**
 	 * A convenience constructor that creates an {@code ArrayPP} out of the values in the provided {@code javax.swing.JList}
 	 *
 	 * @param jList the {@code javax.swing.JList} of values that will be added to this array
 	 */
 //   * @throws ClassCastException If {@code jList} contains an item that does not extend {@code T} 
 	@SuppressWarnings("OverridableMethodCallInConstructor")
 	public ArrayPP(javax.swing.JList<T> jList)
 	{
 		this(jList.getModel().getSize());
 		for (int i = 0; i < length(); i++)
 			t[i] = /*(T)*/ jList.getModel().getElementAt(i);
 	}
 
 	/**
 	 * A convenience constructor that creates an {@code ArrayPP} out of the values in the provided {@code java.util.List}
 	 *
 	 * @param list the {@code java.util.List} of values that will be added to this array
 	 */
 	@SuppressWarnings("OverridableMethodCallInConstructor")
 	public ArrayPP(java.util.List<T> list)
 	{
 		this(list.size());
 		for (int i = 0; i < length(); i++)
 			t[i] = list.get(i);
 	}
 
 	/**
 	 * Returns the number of items in the array
 	 *
 	 * @return an {@code int} representing the number of items in the array
 	 */
 	public int length()
 	{
 		return t.length;
 	}
 
 	/**
 	 * Returns a {@code String} representing the data in the array.<br/>
 	 * <i>Equivalent to {@code java.util.Arrays.toString(toArray())}</i>
 	 *
 	 * @return a {@code String} representing the data in the array
 	 */
 	@Override
 	public String toString()
 	{
 		return java.util.Arrays.toString(t);
 	}
 
 	/**
 	 * Returns a <b>copy</b> of the array at the core of this class
 	 *
 	 * @return a <b>copy</b> of the array at the core of this class
 	 */
 	public T[] toArray()
 	{
 		T[] ret = (T[]) new Object[length()];
 		for (int i = 0, l = ret.length; i < l; i++)
 			ret[i] = get(i);
 		return ret;
 	}
 
 	/**
 	 * Returns whether this array does not contain anything but the specified items
 	 *
 	 * @param items the items of which the array should consist
 	 * @return whether the array has any other item within it
 	 */
 	public boolean hasOnly(T... items)
 	{
 		ArrayPP<T> temp = clone();
 		temp.remove(items, true);
 		return temp.isEmpty();
 	}
 
 	/**
 	 * Returns true if and only if there is an object at an index {@code i} in this array such that <b>ANY</b> of the following
 	 * return true:
 	 * <ul>
 	 * <li>{@code this.get(i).equals(item)} returns true</li>
 	 * <li>{@code this.get(i).toString().equalsIgnoreCase(item.toString())}</li>
 	 * <li>{@code new StringPP(this.get(i).toString()).containsIgnoreCase(item.toString()))}</li>
 	 * <li>this is an {@code ArrayPP} of {@code Searchable}s AND the {@code getMatchStrength} method returns 1 or greater
 	 * </ul>
 	 * <B>tl;dr</B>: looks for this item in the array and returns true if it even finds part of it.
 	 *
 	 * @param item the item to be searched for
 	 * @return {@code true} if and only if this array contains the specified {@code item}
 	 */
 	public boolean contains(T item)
 	{
 		if (t.length == 0)
 			return false;
 
 		for (Object x : t)
 			if (x.equals(item)
 				|| x.toString().equalsIgnoreCase(item.toString()))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Returns true if and only if the array contains <b>ALL</b> of the given items
 	 *
 	 * @param items the items to be searched for
 	 * @return true if and only if the array contains <b>ALL</b> of the given items
 	 */
 	public boolean containsAll(T... items)
 	{
 		for (T item : items)
 			if (!contains(item))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the array contains <b>NONE</b> of the given items
 	 *
 	 * @param items the items to be searched for
 	 * @return true if and only if the array contains <b>NONE</b> of the given items
 	 */
 	public boolean containsNone(T... items)
 	{
 		return !containsAny(items);
 	}
 
 	/**
 	 * Returns true if and only if the array contains <b>ANY</b> of the given items
 	 *
 	 * @param items the items to be searched for
 	 * @return true if and only if the array contains <b>ANY</b> of the given items
 	 */
 	public boolean containsAny(T... items)
 	{
 		for (T item : items)
 			if (contains(item))
 				return true;
 		return false;
 	}
 
 	/**
 	 * if this {@code ArrayPP} AND {@code app} are arrays of {@code Comparable}s, then this method returns the average of the
 	 * return values of all the objects' {@code .compareTo} methods, else returns the difference in length between this array's
 	 * length and the provided one's.
 	 *
 	 * @param app the array to compare
 	 * @return an {@code int} representation of the comparison of the two arrays
 	 */
 	@Override
 	public int compareTo(ArrayPP<?> app)
 	{
 		if (t instanceof Comparable[] && app.toArray() instanceof Comparable[])
 		{
 			double avg = 0;
 			if (app.length() > t.length)
 				for (int i = 0; i < Math.min(t.length, app.length()); i++)
 					avg += ((Comparable) app.get(i)).compareTo(t[i]);
 			return (int) (avg / Math.min(t.length, app.length()));
 		}
 		return t.length - app.length();
 	}
 
 	/**
 	 * Returns the object at index {@code index}
 	 *
 	 * @param index the index of the object to be gotten
 	 * @return the object at index {@code index}
 	 * @throws ArrayIndexOutOfBoundsException if {@code index} is less than 0 or greater than the length of the array
 	 * @version 1.1.0
 	 */
 	public T get(int index) throws ArrayIndexOutOfBoundsException
 	{
 		if (index >= 0 && index < t.length)//This and all other logic added 2013-05-18 (1.5.2) for BHIM Try 2
 			return (T) t[index];
 		if (index < 0 && index > -t.length)
 			return (T) t[t.length + index];
 		throw new ArrayIndexOutOfBoundsException("Could not get object at index " + index);
 	}
 
 	/**
 	 * Returns the objects at indices {@code indices}
 	 *
 	 * @param indices the indices of the objects to be gotten
 	 * @return the objects at indices {@code indices}
 	 * @throws ArrayIndexOutOfBoundsException if {@code index} is less than 0 or greater than the length of the array
 	 */
 	public T[] getAll(int... indices) throws ArrayIndexOutOfBoundsException
 	{
 		Object[] t1 = new Object[indices.length];
 		for (int i = 0; i < indices.length; i++)
 			t1[i] = get(indices[i]);
 		return (T[]) t1;
 	}
 
 	/**
 	 * Returns the index of the first instance of {@code val}, or -1 if it isn't found.
 	 *
 	 * @param val the object to be indexed
 	 * @return the index of the first instance of {@code val}, or -1 if it isn't found.
 	 */
 	public int getIndexOf(T val)
 	{
 		for (int i = 0; i < t.length; i++)
 			if (val.equals(t[i]))
 				return i;
 		return -1;
 	}
 
 	/**
 	 * Finds and returns the index of the last index of {@code val} in the array, or {@code -1} if no such value is found.
 	 *
 	 * @param val the value for which the last index will be found.
 	 * @return the index of the last index of {@code val} in the array, or {@code -1} if no such value is found.
 	 */
 	public int getLastIndexOf(T val)
 	{
 		int[] i = getIndicesOf(val);
 		return i.length > 0 ? i[i.length - 1] : -1;
 	}
 
 	/**
 	 * Returns the index of each and every instance of the value {@code val} in this array. If no such item is found, the
 	 * returned array is empty
 	 *
 	 * @param val the value for which this method will search and tally
 	 * @return an array of the indices of this value, as an {@code int[]}
 	 */
 	public int[] getIndicesOf(T val)
 	{
 //    System.out.println("getIndicesOf(" + val + "); in " + this);
 		ArrayPP<Integer> appI = new ArrayPP<>(new Integer[]
 		{
 		});
 		int[] ret;
 		for (int i = 0; i < t.length; i++)
 			if (val.equals(t[i]))
 				appI.add(i);
 
 		ret = new int[appI.length()];
 		for (int i = 0; i < ret.length; i++)
 			ret[i] = appI.get(i);
 //    System.out.println("After getIndicesOf: " + appI);
 		return ret;
 	}
 
 	/**
 	 * Returns the number of times this value occurs in the array
 	 *
 	 * @param val the value whose instances are to be counted
 	 * @return the number of times this value occurs in the array
 	 */
 	public int getNumInstancesOf(T val)
 	{
 		return getIndicesOf(val).length;
 	}
 
 	/**
 	 * Sets the value in the array at index {@code index} to be {@code val}
 	 *
 	 * @param index the index of the value to be changed
 	 * @param val the new value of the slot in the array at index {@code index}
 	 * @return the resulting array.
 	 * @throws ArrayIndexOutOfBoundsException if the index is out of the boundaries of the array (if {@code index} &lt; 0 or
 	 * {@code index} &gt; {@code length()})
 	 */
 	public ArrayPP<T> set(int index, T val) throws ArrayIndexOutOfBoundsException
 	{
 		t[index] = val;
 		return this;
 	}
 
 	/**
 	 * Appends a set of values to the end of the array
 	 *
 	 * @param vals the values to be appended
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> add(T... vals)
 	{
 		int i1 = length(), i2, l;
 		t = java.util.Arrays.copyOf(t, i1 + vals.length);
 		for (l = length(), i2 = 0; i1 < l; i1++, i2++)
 			t[i1] = vals[i2];
 		/*for (int i=0; i < vals.length; i++)
 		 addAll(vals[i]);*/
 		return this;
 	}
 
 	/**
 	 * Appends a set of values to the end of the array
 	 *
 	 * @param vals the values to be appended, as represented by an {@code Iterable}
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> addAll(Iterable<T> vals)
 	{
 		ArrayPP<T> app = new ArrayPP<>(vals);
 		int l = 0, i1 = length();
 		for (T t1 : vals) l++;
 
 		t = java.util.Arrays.copyOf(t, i1 + l);
 		for (T t1 : vals)
 			t[i1++] = t1;
 		return this;
 	}
 
 	/**
 	 * Inserts a value into the array after the specified index
 	 *
 	 * @param val the value to be inserted
 	 * @param index The index BEFORE which the object will be inserted. For instance, if you have an array {@code {A, B, C}} and
 	 * you want to insert {@code D} at index {@code 1}, the resulting array would be {@code {A, D, B, C}}.
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> insert(T val, int index)
 	{
 		return insert((T[]) new Object[]
 		{
 			val
 		}, index);
 	}
 
 	/**
 	 * Insets a set of values into the array at specified points.<br/>
 	 * <b>ARRAYS MUST BE THE SAME SIZE.</b>
 	 *
 	 * @param vals the values to be inserted
 	 * @param indices the indeces after which the new values will be inserted
 	 * @return the resulting array.
 	 * @throws ArrayIndexOutOfBoundsException if any one of the given indices is higher than the current length of the array
 	 * plus the number of given values
 	 */
 	public ArrayPP<T> insert(T[] vals, int... indices)
 	{
 		if (vals.length != indices.length)
 			throw new IllegalArgumentException("Array lengths must be equal. (" + vals.length + " != " + indices.length + ")");
 
 		t = java.util.Arrays.copyOf(t, length() + vals.length);//Create a copy of this array with space at the end for all the new values
 
 		for (int i = length() - 1; i >= 0; i--)//Prepare the array for insertions
 		{
 			for (int i1 = 0, l1 = indices.length; i1 < l1; i1++)
 				if (i == indices[i1])//If the currently observed index in the array is the same as one of the given indices for insertion
 					i--;//Increment the counter so that the following code is guaranteed to fill all spaces in the array with preexisting values
 			if (i < 0)
 				i = 0;
 			swap(i, i > 0 ? i - 1 : 0);
 		}
 
 		for (int i = 0, l = indices.length; i < l; i++)//Fill the prepared spaces with the given values
 			set(indices[i], vals[i]);
 		return this;
 	}
 
 	/**
 	 * Removes the specified values from the array
 	 *
 	 * @param vals the values to be removed
 	 * @param removeAll {@code true} if all instances of each value should be removed, false if just the first of each.
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> remove(T[] vals, boolean removeAll)
 	{
 		for (T x : vals)
 			remove(x, removeAll);
 		return this;
 	}
 
 	/**
 	 * Removes a value from the array. If the given value is not found, then nothing changes and this method returns normally
 	 *
 	 * @param val the value to be removed
 	 * @param removeAll {@code true} if all instances should be removed, false if just the first.
 	 * @return the resulting array.
 	 * @see #remove(int)
 	 * @see #remove(int[])
 	 * @see #getIndexOf(java.lang.Object)
 	 * @see #getIndicesOf(java.lang.Object)
 	 */
 	public ArrayPP<T> remove(T val, boolean removeAll)
 	{
 		return removeAll ? remove(getIndicesOf(val)) : remove(getIndexOf(val));
 	}
 
 	/**
 	 * Removes the item at the specified index from the list. If the index is out of the boundaries of the array, then nothing
 	 * is changed
 	 *
 	 * @param index the index of the item to be removed
 	 * @return {@code this}
 	 */
 	public ArrayPP<T> remove(int index)
 	{
 		ArrayPP<T> a = new ArrayPP<>();
 		for (int i = 0; i < t.length; i++)
 			if (i != index)
 				a.add((T) t[i]);
 		t = a.toArray();
 		return this;
 	}
 
 	/**
 	 * Removes the items at the specified indeces from the list.
 	 *
 	 * @param indices the indices of the items to be removed
 	 * @return the resulting array.
 	 * @see #remove(int)
 	 */
 	public ArrayPP<T> remove(int... indices)
 	{
 		for (int i = 0; i < indices.length; i++)
 		{
 			remove(indices[i]);
 			for (int j = i; j < indices.length; j++)
 				if (indices[j] > indices[i])
 					indices[j]--;//shift next values to fit the changing array
 		}
 		//Possible other solution:
     /*
 		 for(int i=indices.length; i >= 0; i--)
 		 {
 		 remove(indices[i]);
 		 }
 		 */
 		return this;
 	}
 
 	/**
 	 * Searches for and replaces the first or all instances of {@code a} with {@code b}. If there is no instance of {@code a},
 	 * the array will not be changed.
 	 *
 	 * @param a the value that will go away
 	 * @param b the value that will reign anew
 	 * @param replaceAll if {@code false}, will stop after the first instance of a match has been found.
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> replace(T a, T b, boolean replaceAll)
 	{
 		for (int i = 0; i < t.length; i++)
 			if (t[i].equals(a))
 			{
 				t[i] = b;
 				if (!replaceAll)
 					break;
 			}
 		return this;
 	}
 
 	/**
 	 * Searches for and replaces the first or all instances of any and all values in the array {@code a} with the value in
 	 * {@code b}. If there is no instance of any value in {@code a}, the array will not be changed.
 	 *
 	 * @param a the values that will go away
 	 * @param b the value that will reign anew
 	 * @param replaceAll if {@code false}, will stop after the first instance of a match has been found and continue to the next
 	 * value in the array {@code a}.
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> replace(T[] a, T b, boolean replaceAll)
 	{
 		for (T x : a)
 			replace(x, b, replaceAll);
 		return this;
 	}
 
 	/**
 	 * Sequentially searches for and replaces the first or all instances of the values in {@code a} with the corresponding
 	 * values in {@code b}. If there is no instance of any value in {@code a}, the array will not be changed.
 	 *
 	 * @param a the values that will go away
 	 * @param b the corresponding values that will reign anew
 	 * @param replaceAll if {@code false}, will stop after the first instance of a match has been found and continue to the next
 	 * value in the array {@code a}.
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> replace(T[] a, T[] b, boolean replaceAll)
 	{
 		for (int i = 0; i < a.length; i++)
 			replace(a[i], b[i], replaceAll);
 		return this;
 	}
 
 	/**
 	 * Removes null values from the array
 	 *
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> trimInside()
 	{
 		for (int i = 0; i < length(); i++)
 			while (i < length() && get(i) == null)
 				remove(i);
 		return this;
 	}
 
 	/**
 	 * Removes null values from the ends of the array
 	 *
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> trim()
 	{
 		while (get(0) == null)
 			remove(0);
 		while (get(length() - 1) == null)
 			remove(length() - 1);
 		return this;
 	}
 
 	/**
 	 * Fills the array with the value specified
 	 *
 	 * @param val the value with which the array will be filled
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> fillWith(T val)
 	{
 		for (int i = 0; i < t.length; i++)
 			t[i] = val;
 		return this;
 	}
 
 	/**
 	 * Fills the {@code null} values in the array with the value specified
 	 *
 	 * @param val the value with which the empty spaces in the array will be filled
 	 * @return the resulting array.
 	 */
 	public ArrayPP<T> fillEmptyWith(T val)
 	{
 		for (int i = 0; i < t.length; i++)
 			if (t[i] == null)
 				t[i] = val;
 		return this;
 	}
 
 	/**
 	 * Returns true if the array is full of only {@code null} values or has a size of {@code 0}
 	 *
 	 * @return whether the array is empty
 	 */
 	public boolean isEmpty()
 	{
 		for (int i = 0; i < t.length; i++)
 			if (t[i] != null)
 				return false;
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the array has a size of {@code 0}
 	 *
 	 * @return whether the array is flat
 	 */
 	public boolean isFlat()
 	{
 		return t.length == 0;
 	}
 
 	/**
 	 * Returns a deep clone of a portion of this array.<BR/>
 	 * Cases:
 	 * <UL>
 	 *	<LI>{@code 0 < beginIndex < length()}<WBO/> &ndash; Start from the {@code beginIndex}<SUP>th</SUP> index from the beginning of the array</LI>
 	 *	<LI>{@code 0 > beginIndex > -length()}<WBO/> &ndash; Start from the {@code beginIndex}<SUP>th</SUP> index from the end of the array</LI>
 	 *	<LI>{@code 0 < endIndex < length()}<WBO/> &ndash; End on the {@code endIndex}<SUP>th</SUP> index from the beginning of the array</LI>
 	 *	<LI>{@code 0 > endIndex > length()}<WBO/> &ndash; End on the {@code endIndex}<SUP>th</SUP> index from the end of the array</LI>
 	 *	<LI>{@code endIndex == 0}<WBO/> &ndash; {@code endIndex} is set to {@code length() - 1}</LI>
 	 *	<LI>{@code endIndex < beginIndex}<WBO/> &ndash; The array returned is the reverse of {@code getAll(beginIndex, endIndex)}</LI>
 	 *	<LI>{@code beginIndex > lenth() || endIndex > length()}<WBO/> &ndash; An {@link ArrayIndexOutOfBoundsException} is thrown</LI>
 	 * </UL>
 	 *
 	 * @param beginIndex the index of the first item in the clone
 	 * @param endIndex the index of the last item in the clone
 	 * @return a deep clone of a portion of this array
 	 * @throws ArrayIndexOutOfBoundsException if the absolute value of either parameter is larger than the length of this array
 	 * @version 1.1.0
 	 */
 	public ArrayPP<T> subSet(int beginIndex, int endIndex)
 	{
 		if (Math.abs(beginIndex) > length() || Math.abs(endIndex) > length())
 		if (beginIndex < 0)
 			beginIndex = length() + beginIndex;
 		if (endIndex < 0)
 			endIndex = length() + endIndex;
 		else if (endIndex == 0)
 			endIndex = length();
 		if (beginIndex > endIndex)
 			return subSet(endIndex, beginIndex).reverse();
 		
 		ArrayPP<T> ret = new ArrayPP<>();
 		for (int i = beginIndex, l = length(); i <= endIndex && i < l; i++)
 			ret.add(get(i));
 		for (int i = beginIndex, l = length(); i <= endIndex && i < l; i++)
 			ret.add(get(i));
 		return ret;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		int hash = 7;
 		hash = 29 * hash + Arrays.deepHashCode(this.t);
 		return hash;
 	}
 
 	@Override
 	public boolean equals(Object o)
 	{
 		return o instanceof ArrayPP && equals((ArrayPP<T>) o);
 	}
 
 	/**
 	 * Returns {@code true} if, and only if, the {@code ArrayPP}s are exactly the same length and the {@code .equals} methods of
 	 * each and every corresponding object in each array return {@code true}
 	 *
 	 * @param a the {@code ArrayPP} which will be subject to comparison to this one
 	 * @return {@code true} if and only if both arrays contain the same objects
 	 */
 	public boolean equals(ArrayPP<T> a)
 	{
 		int l = length();
 		if (l != a.length())
 			return false;
 		for (int i = 0; i < l; i++)
 			if (!get(i).equals(a.get(i)))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Swaps the item at the first given index with the item at the second
 	 *
 	 * @param item1 the index of the first item to be swapped
 	 * @param item2 the index of the second item to be swapped
 	 * @return the resulting {@code ArrayPP}
 	 */
 	public ArrayPP<T> swap(int item1, int item2)
 	{
 		T tmp = get(item1);
 		t[item1] = t[item2];
 		t[item2] = tmp;
 		return this;
 	}
 
 	/**
 	 * Removes ALL items from the array. Analogous to {@code t = new T[0];}.
 	 *
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> clear()
 	{
 		if (t == null)
 			t = (T[]) new Object[0];
 		else if (length() > 0)
 			t = Arrays.copyOf(t, 0);
 		return this;
 	}
 
 	/**
 	 * Returns a clone of this array, but with all non-matching objects removed. If any one object's {@code equals} method
 	 * returns {@code false}, this method will check its {@code toString().containsAll} method, and then try {@code StringPP}'s
 	 * {@code containsIgnoreCase} if that is unsuccessful. If the new array of objects already containsAll an observed object,
 	 * it will <b>NOT</b> be added (this leads to a possible use for cleaning out duplicates by using an empty array as the
 	 * parameter {@code vals})
 	 *
 	 * @param vals the values to be isolated.
 	 * @return the resulting ArrayPP
 	 */
 	public ArrayPP<T> isolate(T... vals)
 	{
 		ArrayPP<T> temp = new ArrayPP<>();
 		for (int i = 0; i < vals.length; i++)
 			for (int j = 0; j < length(); j++)
 				if (t[j].equals(vals[i])
 					|| t[j].toString().contains(vals[i].toString()))
 					if (!temp.contains(get(j)))
 						temp.add(get(j));
 		return temp;
 	}
 
 	/**
 	 * Returns a copy of this array, such that<br/>
 	 * <pre>
 	 * ArrayPP&lt;T&gt; one = new ArrayPP(new T[]{itemOfTypeT}), two = one.clone();
 	 * System.out.println(one.equals(two));
 	 * System.out.println(one.getClass() == two.getClass());
 	 * System.out.println(one.clone().getClass() == two.getClass());
 	 * System.out.println(one == two);
 	 * </pre> prints
 	 * <pre>
 	 * true
 	 * true
 	 * true
 	 * false
 	 * </pre>
 	 * <br/>
 	 * <b>This method works best when this array is made up of objects which implement {@link CompleteObject}</b>
 	 *
 	 * @return a new {@code ArrayPP} that contains all the objects within this one
 	 * @see CompleteObject
 	 */
 	@Override
 	public ArrayPP<T> clone()
 	{
 		return new ArrayPP<>((T[]) t);
 	}
 
 	/**
 	 * Returns the last item from this array
 	 *
 	 * @return the resulting {@code ArrayPP}
 	 */
 	public T getLastItem()
 	{
 		if (length() <= 0)
 			throw new IllegalStateException("There are no items in the array, last item not gotten.");
 		return get(length() - 1);
 	}
 
 	/**
 	 * Returns the first item from this array
 	 *
 	 * @return the resulting {@code ArrayPP}
 	 */
 	public T getFirstItem()
 	{
 		return get(0);
 	}
 
 	/**
 	 * Removes the last item from this array
 	 *
 	 * @return the resulting {@code ArrayPP}
 	 */
 	public ArrayPP<T> removeLastItem()
 	{
 		remove(length() - 1);
 		return this;
 	}
 
 	/**
 	 * Removes the first item from this array
 	 *
 	 * @return the resulting {@code ArrayPP}
 	 */
 	public ArrayPP<T> removeFirstItem()
 	{
 		remove(0);
 		return this;
 	}
 
 	@Override
 	public Iterator<T> iterator()
 	{
 		return new java.util.Iterator<T>()
 		{
 			boolean hasRem = false;
 			int pos = 0, rem = 0;
 
 			@Override
 			public boolean hasNext()
 			{
 				return pos < length();
 			}
 
 			@Override
 			public T next() throws ArrayIndexOutOfBoundsException
 			{
 				hasRem = false;
 				pos++;
 				return get(pos - 1);
 			}
 
 			@Override
 			public void remove()
 			{
 				if (!hasRem)
 					getThis().remove(pos);
 				hasRem = true;
 			}
 		};
 	}
 
 	/**
 	 * Returns {@code this}
 	 *
 	 * @return {@code this}
 	 */
 	private ArrayPP<T> getThis()
 	{
 		return this;
 	}
 
 	/**
 	 * Creates and returns a {@code javax.swing.JList} version of this array
 	 *
 	 * @return a {@code javax.swing.JList} version of this array
 	 */
 	public javax.swing.JList/*<T>*/ toJList()
 	{
 		return new javax.swing.JList/*<>*/(t);
 	}
 
 	/**
 	 * Sorts the contents of this array based on the "bubble sort" technique, where each item has meta information about its
 	 * weight
 	 *
 	 * @param strengthMarkers higher strength means the corresponding item becomes higher in the final array
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> bubbleSort(ArrayPP<Double> strengthMarkers)
 	{
 		boolean changed;
 		ArrayPP<Double> mk = strengthMarkers.clone();
 		out:
 		for (int i = 0, j, l = length() - 1; i < l; i++)//Efficiency added Mar 11, 2012 (1.4.14) for Marian
 		{
 			changed = false;
 			for (j = 0; j < l; j++)
 			{
 				if (mk.get(j) < mk.get(j + 1))//Dunno how I got this wrong... Mar 11, 2012 (1.4.14) for Marian
 				{
 					changed = true;
 					mk.swap(j, j + 1);
 					swap(j, j + 1);
 				}
 				if (!changed)
 					break out;// Better apply cream
 			}
 		}
 
 		return this;
 	}
 
 	/**
 	 * Sorts the array using the "bubble sort" algorithm.
 	 *
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> bubbleSort()
 	{
 		boolean changed;
 		T get, t1 = null;
 		out:
 		for (int i = 0, l = length() - 1; i < l; i++)//Changed to "l = length() - 1" Mar 11, 2012 (1.4.14) for Marian
 		{
 			changed = false;
 			in:
 			for (int j = 0; j < l; j++)
 				if (((get = get(j)) instanceof Comparable && ((Comparable) get).compareTo(t1 = get(j + 1)) < 0))
 				{
 					changed = true;
 					swap(j, j + 1);//Changed to "j + 1" Mar 11, 2012 (1.4.14) for Marian
 				}
 			if (!changed)
 				break out;// Sound the alarm! D:
 		}
 
 		return this;
 	}
 
 	/**
 	 * Performs a quicksort on the given array.
 	 *
 	 * @param array the array to sort
 	 * @return the sorted array
 	 */
 	public static ArrayPP<Comparable> quicksort(ArrayPP<Comparable> array)
 	{
 		if (array.length() <= 1)
 			return array;  // an array of zero or one elements is already sorted
 		Comparable pivot = 0; //select and remove a pivot value 'pivot' from 'array'
 		ArrayPP<Comparable> less = new ArrayPP<>(), greater = new ArrayPP<>();// create empty lists 'less' and 'greater'
 		for (Comparable x : array)
 			if (x.compareTo(pivot) <= 0)
 				less.add(x); // append 'x' to 'less'
 			else
 				greater.add(x); // append 'x' to 'greater'
 		return quicksort(less).add(pivot).add(quicksort(greater)); // two recursive calls
 	}
 
 	/**
 	 * Performs a quicksort on this array <STRONG>if and only if this array is made up of {@link Comparable}s</STRONG>. If this
 	 * is not an array of {@link Comparable}s, then a {@link IllegalStateException} is returned.<BR/>
 	 * In order to guarantee that this array is full of {@link Comparable} elements, then initialize it as
 	 * {@code ArrayPP<Comparable>}. <STRONG>Using a {@link Comparable64} will also work.</STRONG>
 	 *
 	 * @return {@code this}
 	 * @throws IllegalStateException if {@code this} is not an array of {@link Comparable} elements.
 	 */
 	public ArrayPP<? extends Comparable> quicksort()
 	{
 		try
 		{
 			if (length() <= 1)
 				return (ArrayPP<? extends Comparable>) this;  // an array of zero or one elements is already sorted
 			Comparable pivot = 0; //select and remove a pivot value 'pivot' from 'array'
 			ArrayPP<Comparable> less = new ArrayPP<>(), greater = new ArrayPP<>(); // create empty lists 'less' and 'greater'
 			for (Comparable x : (ArrayPP<? extends Comparable>) this)
 				if (x.compareTo(pivot) <= 0)
 					less.add(x); // append 'x' to 'less'
 				else
 					greater.add(x); // append 'x' to 'greater'
 			return quicksort(less).add(pivot).add(quicksort(greater)); // two recursive calls
 		}
 		catch (ClassCastException e) // If this is not an instance of ArrayPP<Comparable>
 		{
 			throw new IllegalStateException("Tried to sort an array does not contain Comparable elements", e);
 		}
 	}
 
 	/**
 	 * Creates and returns a {@code ListModel} version of this array
 	 *
 	 * @return a {@code ListModel} version of this array
 	 */
 	public ListModel toListModel()
 	{
 		return new AbstractListModel()
 		{
 			@Override
 			public int getSize()
 			{
 				return t.length;
 			}
 
 			@Override
 			public Object getElementAt(int index)
 			{
 				return t[index];
 			}
 		};
 	}
 
 	/**
 	 * Removes every item in the array except the given ones
 	 *
 	 * @param keepObjects the items to keep
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> removeAllBut(Object... keepObjects)
 	{
 		for (int i = 0; i < length(); i++)
 			for (Object objToKeep : keepObjects)
 				if (!get(i).equals(objToKeep))
 				{
 					remove(i--);
 					break;
 				}
 		return this;
 	}
 
 	/**
 	 * Removes every item in the array except the ones at the given indices
 	 *
 	 * @param keepIndices the indices of the items to keep
 	 * @return the resulting array
 	 */
 	public ArrayPP<T> removeAllBut(int... keepIndices)
 	{
 		ArrayPP<T> temp = new ArrayPP<>();
 		for (int i : keepIndices)
 			temp.add(get(i));
 		clear().addAll(temp);
 		return this;
 	}
 
 	/**
 	 * Searches the array to see if this exact item is in it. If there is no item in this array for which
 	 * {@code arrayItem == item} returns true, this method returns false
 	 *
 	 * @param item the item to be searched for
 	 * @return {@code true} if and only if there is an item in this array such that {@code arrayItem == item} returns true
 	 */
 	public boolean containsExactly(T item)
 	{
 		if (t.length == 0)//a shortcut, to cut down on read time of an empty array
 			return false;
 
 		for (int i = 0; i < length(); i++)
 			if (t[i].equals(item))
 				return true;
 
 		return false;
 	}
 
 	/**
 	 * Searches the array to see if this exact sequence of items is in it. If there is no sequence items in this array that
 	 * exactly matches the given sequence, this method returns {@code false}
 	 *
 	 * @param items the items to be searched for
 	 * @return {@code true} if and only if all these items appear in this array in the same sequence
 	 */
 	public boolean containsSequence(T... items)
 	{
 		if (items.length == 0 || items.length > length())
 			return false;
 		s:
 		for (int i = 0, j = 0, l = length() - items.length; i < l; i++, j = i)
 			if (get(i).equals(items[0]))
 			{
 				for (T item : items)
 				{
 					if (!get(j).equals(item))
 						continue s;
 					j++;
 				}
 				return true;
 			}
 		return false;
 	}
 
 	/**
 	 * Destroys this array. All items are observed. If an individual item is an instance of {@link CompleteObject}, then its
 	 * {@link CompleteObject#finalize()} method is called. Whether or not it implements {@link CompleteObject}, it is then set
 	 * to {@code null}. After all items in this array have been set to null, the {@link #clear()} method is called
 	 */
 	@SuppressWarnings("UseOfSystemOutOrSystemErr")
 	public void destroy()
 	{
 		for (int i = 0, l = length(); i < l; i++)
 			t[i] = null;
 		clear();
 	}
 
 	/**
 	 * Will return {@code true} if and only if <b>ALL</b> the given conditions are satisfied:
 	 * <ol>
 	 * <li>The given index is non-negative</li>
 	 * <li>The given index is less than the number of objects in this array</li>
 	 * <li>The item at the given index (now knowing that the array contains the given index) is non-null</li>
 	 * </ol>
 	 *
 	 * @param index the index to test
 	 * @return {@code true} if there is an item in this array at the index {@code index}
 	 */
 	public boolean has(int index)
 	{
 		return index >= 0 && index < length() && get(index) != null;
 	}
 
 	/**
 	 * Ensures that the array is at least large enough to contain the given index
 	 *
 	 * @param rowIndex
 	 */
 	public void reserve(int rowIndex)
 	{
 		while (length() < rowIndex)
 			add((T) null);
 	}
 
 	/**
 	 * Adds the given item to the array if and only if it is not already in the array
 	 *
 	 * @return {@code this}
 	 * @param item the item to be added
 	 * @see #containsExactly(java.lang.Object)
 	 * @since Feb 26, 2012 (1.4.14) for Marian
 	 */
 	public ArrayPP addWithoutDuplicates(T item)
 	{
 		if (!containsExactly(item))
 			add(item);
 		return this;
 	}
 
 	/**
 	 * Iterates through all the objects in this array and performs one of the following:
 	 * <ol>
 	 * <li>If the object being observed implements {@link CompleteObject}, then its {@link Object#finalize()} method is
 	 * called</li>
 	 * <li>Else, if the object being observed is an instance of {@link Window}, then its {@link Window#dispose()} method is
 	 * called</li>
 	 * <li>Else, it is set to {@code null}</li>
 	 * </ol>
 	 *
 	 * @throws Throwable
 	 */
 	@Override
 	@SuppressWarnings("FinalizeNotProtected")
 	public void finalize() throws Throwable
 	{
 		for (int i = 0, l = length(); i < l; i++)
 			if (get(i) instanceof Window)
 				((Window) get(i)).dispose();
 			else
 				set(i, null);
 		super.finalize();
 	}
 
 	public ArrayPP<T> setOrCreate(T value, int index)
 	{
 		while (length() < index)
 			add((T) null);
 		return set(index, value);
 	}
 
 	/**
 	 * For use as a stack, semantically equivalent to {@link #insert(val, 0)}
 	 *
 	 * @param val The value to push
 	 * @return {@code this}
 	 * @since 2012/10/01 (1.5.1)
 	 * @see #insert(java.lang.Object, int)
 	 */
 	public ArrayPP<T> push(T val)
 	{
 		return insert(val, 0);
 	}
 
 	/**
 	 * For use as a stack, semantically equivalent to {@link #getFirstItem()}
 	 *
 	 * @return the top value
 	 * @since 2012/10/01 (1.5.1)
 	 * @see #getFirstItem()
 	 */
 	public T peek()
 	{
 		return getFirstItem();
 	}
 
 	/**
 	 * For use as a stack, semantically equivalent to {@link #getFirstItem()} and then {@link #removeFirstItem()}
 	 *
 	 * @return the top value
 	 * @since 2012/10/01 (1.5.1)
 	 * @see #getFirstItem()
 	 * @see #removeFirstItem()
 	 */
 	public T pop()
 	{
 		T ret = getFirstItem();
 		removeFirstItem();
 		return ret;
 	}
 
 	/**
 	 * Reverses this array and returns it
 	 * @return {@code this}
 	 */
 	public ArrayPP<T> reverse()
 	{
 		for(int i=0, l = length(), hl=l / 2; i < hl; i++)
 			swap(i, -i);
 		return this;
 	}
 	
 	public Collection<T> makeCollection()
 	{
 		return new Collection<T>()
 		{
 			@Override
 			public int size()
 			{
 				return length();
 			}
 
 			@Override
 			public <T2> T2[] toArray(T2[] a)
 			{
 				if (a == null || size() > a.length)
 					a = (T2[]) new Object[size()];
 				for (int i = 0; i < a.length; i++)
 					try
 					{
 						a[i] = (T2) get(i);
 					}
 					catch (ClassCastException ex)
 					{
						Logger.getLogger(getClass()).warning("Could not add item " + i + " to array; types don't match.", ex);
 						continue;
 					}
 				return a;
 			}
 
 			@Override
 			public boolean add(T e)
 			{
 				System.out.println("derp");
 				int before = length();
 				getThis().add(e);
 				return length() == before;
 			}
 
 			@Override
 			public boolean remove(Object o)
 			{
 				int before = length();
 				try
 				{
 					getThis().remove((T) o, false);
 				}
 				catch (ClassCastException ex)
 				{
 					return false;
 				}
 				return length() == before;
 			}
 
 			@Override
 			@SuppressWarnings("element-type-mismatch")
 			public boolean containsAll(Collection<?> c)
 			{
 				for (Object object : c)
 					if (!contains(object))
 						return false;
 				return true;
 			}
 
 			@Override
 			public boolean addAll(Collection<? extends T> c)
 			{
 				if (c == null)
 					return false;
 				int before = length();
 				for (T x : c)
 					add(x);
 				return length() == before;
 			}
 
 			@Override
 			public boolean removeAll(Collection<?> c)
 			{
 				int before = length();
 				for (Object x : c)
 					try
 					{
 						getThis().remove((T) x, true);
 					}
 					catch (ClassCastException ex)
 					{
 						continue;
 					}
 				return length() == before;
 			}
 
 			@Override
 			public boolean retainAll(Collection<?> c)
 			{
 				int before = length();
 				removeAllBut(c.toArray());
 				return length() == before;
 			}
 
 			@Override
 			public boolean isEmpty()
 			{
 				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public boolean contains(Object o)
 			{
 				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public Iterator<T> iterator()
 			{
 				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public Object[] toArray()
 			{
 				return getThis().toArray();
 			}
 
 			@Override
 			public void clear()
 			{
 				getThis().clear();
 			}
 		};
 	}
 }
