 /**
  * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
  */
 package us.stangl.crostex.util;
 
 /**
  * Simple, efficient, non-synchronized LIFO stack.
  * Originally threw EmptyStackException, but really what's the point? Pay extra
  * cost for every pop/peek, just to have a "more descriptive" exception,
  * EmptyStackException instead of ArrayIndexOutOfBoundsException? No thanks. 
  * @author Alex Stangl
  */
 public class Stack<E> {
 	// slots for holding elements
 	private E[] slots;
 	
 	// fill pointer, index of next member of slots to write into. If equal 0, stack is empty
 	private int fillPointer;
 	
 	/**
 	 * Constructor specifying initial capacity
 	 * @param initialCapacity
 	 */
 	@SuppressWarnings("unchecked")
 	public Stack(int initialCapacity) {
 		slots = (E[])new Object[initialCapacity];
 	}
 	
 	/**
 	 * No-arg constructor, for default initial capacity.
 	 */
 	public Stack() {
 		this(200);
 	}
 
 	public void push(E entry) {
 		if (fillPointer >= slots.length)
 			growSlots();
 		slots[fillPointer++] = entry;
 	}
 	
 	public boolean empty() {
 		return fillPointer == 0;
 	}
 
 	/**
 	 * return last pushed element without removing it from the stack
 	 * @throws ArrayIndexOutOfBoundsException if stack empty
 	 */
 	public E peek() {
 		return slots[fillPointer - 1];
 	}
 	
 	/**
 	 * return last pushed element, popping it off the stack
 	 * @throws ArrayIndexOutOfBoundsException if stack empty
 	 */
 	public E pop() {
		return slots[--fillPointer];
 	}
 	
 	public boolean contains(E obj) {
 		for (int i = 0; i < fillPointer; ++i)
 			if (slots[i] == obj)
 				return true;
 		return false;
 	}
 	
 	private void growSlots() {
 		@SuppressWarnings("unchecked")
 		E[] newSlots = (E[])new Object[(slots.length * 3) / 2 + 1];
 		for (int i = 0; i < slots.length; ++i)
 			newSlots[i] = slots[i];
 		slots = newSlots;
 	}
 }
