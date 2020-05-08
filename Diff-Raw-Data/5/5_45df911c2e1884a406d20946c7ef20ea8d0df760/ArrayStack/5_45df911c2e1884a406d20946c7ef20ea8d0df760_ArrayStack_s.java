 /**
  * 
  */
 package com.sm.dstructs.deque;
 
 import java.lang.reflect.Array;
 import java.util.AbstractList;
 import java.util.Collection;
 
 /**
  * An ArrayStack implements the List interface using an Array also know as the
  * backing array. Resizing is done by doubling the array.
  * 
  * @author smtechnocrat
  * 
  */
 public class ArrayStack<T> extends AbstractList<T> {
 
 	// the array used to store all the elements
 	private T[] backingArray;
 
 	// the number of elements in the stack
 	private int n;
 
 	// Storing the class of the element in the Stack for creating new arrays
 	private Class clazz;
 
 	/**
 	 * Constructors
 	 */
 	public ArrayStack(T t, int initSize) {
 		backingArray = (T[]) Array.newInstance(t.getClass(), initSize);
 		clazz = t.getClass();
 		n = 0;
 	}
 
 	/**
 	 * @see AbstractList
 	 */
 	public T get(int i) {
 
 		if (i < 0 || i > n - 1)
 			throw new IndexOutOfBoundsException();
 
 		return backingArray[i];
 
 	}
 
 	/**
 	 * @see AbstractListls
 	 * 
 	 * 
 	 */
 	public int size() {
 		return n;
 	}
 
 	/**
 	 * Appends the specified element to the end of this list (optional
 	 * operation).
 	 * 
 	 * @see AbstractList
 	 */
 	public boolean add(T x) {
		if (n++ > backingArray.length) {
 			resize(); // we have reached the max length, need to grow the
 						// backingArray
 		}
 		backingArray[n++] = x;
 
 		return true;
 	}
 
 	/**
 	 * Inserts the specified element at the specified position in this list
 	 * (optional operation). Shifts the element currently at that position (if
 	 * any) and any subsequent elements to the right (adds one to their
 	 * indices).
 	 * 
 	 * @see AbstractList.add
 	 * 
 	 */
 	public void add(int index, T x) {
 		if (index < 0 || index > (n - 1))
 			throw new IndexOutOfBoundsException();
		if (n++ > backingArray.length)
 			resize();
 		for (int j = n; j > index; j--) {
 			backingArray[j] = backingArray[j - 1];
 		}
 		backingArray[index] = x;
 		n++;
 
 	}
 
 	public boolean addAll(int index, Collection<? extends T> c) {
 
 		if (index < 0 || index > (n - 1))
 			throw new IndexOutOfBoundsException();
 
 		int csize = c.size();
 
 		if (csize == 0)
 			return true; // Nothing to insert.
 
 		if ((csize + n) > backingArray.length)
 			resize(2 * (csize + n));
 
 		for (int j = n + csize; j > index + csize; j--)
 			backingArray[j] = backingArray[j - csize];
 
 		for (T x : c)
 			backingArray[index++] = x;
 
 		n += csize;
 
 		return true;
 
 	}
 
 	/**
 	 * Removes the element at the specified position in this list (optional
 	 * operation). Shifts any subsequent elements to the left (subtracts one
 	 * from their indices). Returns the element that was removed from the list.
 	 * 
 	 * @see AbstractList
 	 */
 	public T remove(int i) {
 
 		T x = backingArray[i]; // element to be removed.
 
 		for (int j = i; j < n - 1; j++) {
 			backingArray[j] = backingArray[j + 1];
 		}
 		n -= 1; // decrease the current size by 1 when removed
 		return x;
 	}
 
 	/**
 	 * resize the backing array
 	 */
 	protected void resize() {
 		T[] tmp = (T[]) Array.newInstance(clazz, Math.max(n * 2, 1));
 		for (int i = 0; i < n; i++) {
 			tmp[i] = backingArray[i];
 		}
 		backingArray = tmp;
 	}
 
 	/**
 	 * Grow the array by nn
 	 * 
 	 * @param nn
 	 */
 	protected void resize(int nn) {
 		T[] tmp = (T[]) Array.newInstance(clazz, nn);
 		for (int i = 0; i < n; i++) {
 			tmp[i] = backingArray[i];
 		}
 		backingArray = tmp;
 
 	}
 }
