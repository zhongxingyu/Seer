 package edu.ncsu.csc216.solitaire.model;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * The LinkedList for the Deck of cards
  * @author William Blazer, Andrew Kofink
  */
 public class DeckLinkedList implements Iterable<Integer> {
 	
 	/**
 	 * The front node of the list
 	 */
 	private ListNode front;
 	
 	/**
 	 * The currens size of the list
 	 */
 	private int size;
 	
 	/**
 	 * The LinkedList for the Deck of cards
 	 */
 	public DeckLinkedList() {
 		size = 0;
 		front = null;
 	}
 	
 	/**
 	 * Adds a value to the end of the LinkedList
 	 * @param value The value to add
 	 */
 	public void add(int value) {
 		add(value, front);
 	}
 	
 	/**
 	 * Adds an array of values to the end of the LinkedList
 	 * @param values Array of integers to add
 	 */
 	public void add(int[] values) {
 		if (front == null) {
 			for (int i = values.length - 1; i >= 0; i--) {
 				addFirst (values[i]);
 			}
 		} else {
 			for (int value: values) {
 				add(value);
 			}
 		}
 	}
 	
 	/**
 	 * A helper method for add(int value)
 	 * @param value The value to be added
 	 * @param index The current parsed index
 	 */
 	private void add(int value, ListNode current) {
 		if (front == null) {
 			front = new ListNode(value);
 			front.next = front;
 			front.previous = front;
 			size++;
 		} else if (current.next == front) {
 			current.next = new ListNode(value);
 			current.next.next = front;
 			current.next.previous = current;
 			front.previous = current.next;
 			size++;
 		} else {
 			add(value, current.next);
 		}
 	}
 	
 	/**
 	 * Adds a value to the front of the linked list
 	 * @param value The value to be added
 	 */
 	public void addFirst(int value) {
 		if (front == null) {
 			add(value, front);
 		} else {
 			ListNode newNode = new ListNode(value, front, front.previous);
 			front.previous = newNode;
 			newNode.previous.next = newNode;
 			front = newNode;
 			size++;
 		}
 	}
 	
 	/**
 	 * Clears the linked list
 	 */
 	public void clear() {
 		front = null;
 		size = 0;
 	}
 	
 	/**
 	 * Concatenates another list onto the end of the receiver list
 	 * @param list The list to concatenate
 	 */
 	public void concat(DeckLinkedList list) {
 		if (front == null) {
 			front = list.front;
 			size = list.size;
 		} else if (list.front == null) {
 			return;
 		} else {
 			front.previous.next = list.front;
 			list.front.previous.next = front;
 			
 			ListNode middleLeft = front.previous;
 			front.previous = list.front.previous;
 			list.front.previous = middleLeft;
 			
 			size += list.size;
 		}
 	}
 	
 	/**
 	 * Detaches a deck linked list at a certain index of the current
 	 * deck linked list.
 	 * @param index - 1 The index where the list will be detached
 	 * @return The detached deck linked list
 	 */
 	public DeckLinkedList detachAt(int index) {
 		if (index >= size || index < 0) {
 			throw new IndexOutOfBoundsException();
 		}
 		
 		DeckLinkedList detatchedList = new DeckLinkedList();
 		if (index < 1) {
 			detatchedList.front = front;
 			front = null;
 			detatchedList.size = size;
 			size = 0;
 		} else {
 			ListNode nodeBeforeSplit = findNode(index - 1, front);
 			detatchedList.front = nodeBeforeSplit.next;
 			nodeBeforeSplit.next = front;
 			detatchedList.front.previous = front.previous;
 			front.previous = nodeBeforeSplit;
 			detatchedList.front.previous.next = detatchedList.front;
 			detatchedList.size = size - index;
 			size = index;
 		}
 		return detatchedList;
 	}
 	
 	/**
 	 * Method to return the node at an index
 	 * @param index The index to return the node at
 	 * @return The list node in question
 	 */
 	public ListNode findNode(int index) {
 		return findNode(index, front);
 	}
 	
 	/**
 	 * Helper method to return the node at an index
 	 * @param index The index to return the node at
 	 * @param current The current node in the recursion
 	 * @return The list node in question
 	 */
 	private ListNode findNode(int index, ListNode current) {
 		if (index < 0) {
 			return findNode(index + 1, current.previous);
 		} else if (index > 0) {
 			return findNode(index - 1, current.next);
 		} else {
 			return current;
 		}
 	}
 	
 	/**
 	 * Exchanges two values in the list
 	 * @param index1 The index of value 1
 	 * @param index2 The index of value 2
 	 */
 	public void exchange(int index1, int index2) {
 		if (index1 >= size || index1 < 0 || index2 >= size || index2 < 0) {
 			throw new IndexOutOfBoundsException();
 		}
 		
 		ListNode node1 = findNode(index1, front);
 		ListNode node2 = findNode(index2, front);
 		
 		int temp = node1.data;
 		node1.data = node2.data;
 		node2.data = temp;
 	}
 	
 	/**
 	 * Gets a value from the list at an index
 	 * @param index The index of the value
 	 * @return The value to be returned
 	 */
 	public int get(int index) {
 		if (index >= size || index < 0) {
 			throw new IndexOutOfBoundsException();
 		}
 		return findNode(index, front).data;
 	}
 	
 	/**
 	 * Gets the index of the value in question
 	 * @param value The value to search for
 	 * @return The index of the value or -1 if not found
 	 */
 	public int indexOf(int value) {
 		return findValue(value, front, 0);
 	}
 	
 	/**
 	 * Finds the node at the value
 	 * @param value The value to search for
 	 * @param current The current node in the recursion
 	 * @return The node at which the value is found or null if it is not found
 	 */
 	private int findValue(int value, ListNode current, int count) {
 		if (front == null || (current == front.previous && front.previous.data != value)) {
 			throw new NoSuchElementException();
 		} else if (current.data != value) {
 			return findValue(value, current.next, count + 1);
 		} else {
 			return count;
 		}
 	}
 	
 	/**
 	 * Removes the value at an index
 	 * @param index The index of the value to remove
 	 * @return The value that was removed
 	 */
 	public int remove(int index) {
 		if (index >= size || index < 0) {
 			throw new IndexOutOfBoundsException();
 		}
 		
 		ListNode node = findNode(index, front);
 		
 		if (node == null) {
 			return -1;
 		} else if (size == 1) {
 			front = null;
 		} else {
 			if (index == 0) {
 				front = node.next;
 			}
 			node.next.previous = node.previous;
 			node.previous.next = node.next;
 		}
 		
 		size--;
 		
 		return node.data;
 	}
 	
 	/**
 	 * Changes the data value of a node at an index
 	 * @param index The index of the value to change
 	 * @param value The value to change the node to
 	 */
 	public void set(int index, int value) {
 		if (index >= size || index < 0) {
 			throw new IndexOutOfBoundsException();
 		}
 		findNode(index, front).data = value;
 	}
 	
 	/**
 	 * Removes the last node in the list
 	 */
 	public int removeLast() {
 		return remove(size - 1);
 	}
 	
 	/**
 	 * Gets the size of the DeckLinkedList
 	 * @return The size of the DeckLinkedList
 	 */
 	public int size() {
 		return size;
 	}
 
 	public DeckLinkedListIterator iterator() {
 		return new DeckLinkedListIterator(front);
 	}
 }
 
 class DeckLinkedListIterator implements Iterator<Integer> {
 	
 	private ListNode current;
 
 	public DeckLinkedListIterator(ListNode front) {
 		current = front;
 	}
 	
	public DeckLinkedListIterator() {
		this(null);
	}
	
 	public boolean hasNext() {
 		return current.next != null;
 	}
 	
 	public Integer next() {
 		if (hasNext()) {
 			current = current.next;
 		} else {
 			throw new NoSuchElementException();
 		}
 		return current.data;
 	}
 	
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
	
 }
