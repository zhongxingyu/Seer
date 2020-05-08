 package Lists;
 
 /**
  * Implementation of the SinglyLinkedList data structure.
  * @author RDrapeau
  *
  * @param <E>
  */
 public class SingleLinkedList<E extends Comparable<E>> {
 	/**
 	 * The front of the list.
 	 */
 	private Node head;
 	
 	/**
 	 * The end of the list.
 	 */
 	private Node tail;
 	
 	/**
 	 * The size of the list.
 	 */
 	private int size;
 	
 	/**
 	 * Returns the number of elements in the list.
 	 * 
 	 * @return The size of the list
 	 */
 	public int size() {
 		return size;
 	}
 	
 	/**
 	 * Adds the data as a new Node at the end of the list.
 	 * 
 	 * @param data - The data for the new Node to contain
 	 */
 	public void add(E data) {
 		add(data, size);
 	}
 	
 	/**
 	 * Adds the data as a new Node at the input index (0 based).
 	 * 
 	 * @param data - The data for the new Node to contain
 	 * @param index - The index to add the new Node at (0 based)
 	 * @throws IllegalArgumentException if the index is not valid
 	 */
 	public void add(E data, int index) {
 		if (index == 0) { // Add to the front
 			head = new Node(data, head);
 			if (tail == null) {
 				tail = head;
 			}
 		} else {
 			Node current = getNode(index - 1);
 			current.next = new Node(data, current.next);
 			if (tail.next != null) {
 				tail = tail.next;
 			}
 		}
 		size++;
 	}
 	
 	/**
 	 * Removes and returns the Node at the specified index.
 	 * 
 	 * @param index - The index of the Node to remove
 	 * @throws IllegalArgumentException if the index is not valid
 	 * @return The data of the removed Node
 	 */
 	public E remove(int index) {
 		valid(index);
 		E removed = null;
 		if (index == 0) {
 			removed = head.data;
 			head = head.next;
 			if (head == null) { // Reset the tail since the list is empty
 				tail = null;
 			}
 		} else {
 			removed = removeNext(getNode(index - 1));
 		}
 		return removed;
 	}
 	
 	/**
 	 * Removes and returns the Node that contains the data.
 	 * 
 	 * @param data - The data of the Node to remove
 	 * @return True if the Node was removed and False otherwise
 	 */
 	public boolean remove(E data) {
 		if (head != null && head.data == data) {
 			remove(0);
 			return true;
 		} 
 		Node current = head;
 		while (current != null && current.next != null) {
 			if (current.next.data == data) {
 				removeNext(current);
 				return true;
 			}
 			current = current.next;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sorts the list into ascending order.
 	 */
 	public void sort() {
 		head = sort(head, size);
 	}
 	
 	/**
 	 * Sorts the sublist that starts with the Node head.
 	 * 
 	 * @param head - Start of the list to sort
 	 * @param size - Size of the list (starting with head)
 	 * @return The head of the new sorted list
 	 */
 	private Node sort(Node head, int size) {
 		if (size > 1) {
 			int leftSize = size / 2;
 			int rightSize = size % 2 == 1 ? leftSize + 1 : leftSize;
 			
 			Node current = head;
 			for (int i = 0; i < leftSize - 1; i++) {
 				current = current.next;
 			}
 			Node right = current.next;
 			current.next = null;
 			
 			head = sort(head, leftSize);
 			right = sort(right, rightSize);
 			head = merge(head, right);
 		}
 		return head;
 	}
 	
 	/**
 	 * Merges two sorted lists together.
 	 * 
 	 * @param left - The left sublist
 	 * @param right - The right sublist
 	 * @return The head of the new sorted list
 	 */
 	private Node merge(Node left, Node right) {
 		Node current = null;
 		if (right == null || (left != null && left.data.compareTo(right.data) <= 0)) {
 			current = left;
 			left = left.next;
 		} else if (right != null) {
 			current = right;
 			right = right.next;
 		}
 		Node head = current;
 		while (left != null || right != null) {
 			if (right == null || (left != null && left.data.compareTo(right.data) <= 0)) {
 				current.next = left;
 				left = left.next;
 			} else {
 				current.next = right;
 				right = right.next;
 			}
 			current = current.next;
 		}
 		return head;
 	}
 	
 	/**
 	 * Returns whether or not the list contains the data.
 	 * 
 	 * @param data - The data to check
 	 * @return True if the data was found and false otherwise
 	 */
 	public boolean contains(E data) {
 		Node current = head;
 		while (current != null) {
 			if (current.data == data) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes the next node in the link after current and updates the tail if the end moved back.
 	 * 
 	 * @param current - The node before the one to be removed
 	 * @return The data that the removed Node contained
 	 */
 	private E removeNext(Node current) {
 		if (current != null && current.next != null) {
 			Node removed = current.next;
 			current.next = current.next.next;
 			if (current.next == null) {
 				tail = current;
 			}
 			return removed.data;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the Node at the index.
 	 * 
 	 * @param index - The index of the Node
 	 * @throws IllegalArgumentException if the index is not valid
 	 * @return The Node at the specified index
 	 */
 	private Node getNode(int index) {
 		valid(index);
 		if (index == size - 1) {
 			return tail;
 		}
 		Node current = head;
 		for (int i = 0; i < index; i++) {
 			current = current.next;
 		}
 		return current;
 	}
 	
 	/**
 	 * @throws IllegalArgumentException if passed an invalid index
 	 * @param index - The index to check
 	 */
 	private void valid(int index) {
 		if (index < 0 || index >= size) {
 			throw new IllegalArgumentException("Index is not valid. Size: " + size + ", Index: " + index);
 		}
 	}
 	
 	public String toString() {
 		String result = "[";
 		if (head != null) {
 			result += head.data;
 			Node current = head.next;
 			while (current != null) {
 				result += ", " + current.data;
 				current = current.next;
 			}
 		}
 		return result + "]";
 	}
 	
 //////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Removes duplicates in the list.
 	 */
 	public void removeDuplicates() {
 		Node current = head;
 		Node sec = head;
 		while (current != null) {
 			while (sec.next != null) {
 				if (sec.next.data.equals(current.data)) {
 					sec.next = sec.next.next;
 					size--;
 				} else {
 					sec = sec.next;
 				}
 			}
 			current = current.next;
 			sec = current;
 		}
 	}
 	
 	/**
 	 * Removes duplicates in the list and also sorts it.
 	 */
 	public void removeDuplicatesSort() {
 		sort();
 		Node current = head;
 		while (current != null && current.next != null) {
 			if (current.data.equals(current.next.data)) {
 				current.next = current.next.next;
 				size--;
 			} else {
 				current = current.next;
 			}
 		}
 	}
 	
 	/**
 	 * Returns the nth to last element in the list.
 	 * 
 	 * @param n - The value for the nth to last element with 1 being the end and size being the front
 	 * @throws IllegalArgumentException if n is <= 0 or bigger than the size of the list
 	 * @return The value of the element in the nth to last position
 	 */
 	public E nthLast(int n) {
 		if (n <= 0 || n > size) {
 			throw new IllegalArgumentException();
 		}
 		Node current = head;
 		Node forward = head;
 		for (int i = 0; i < n; i++) {
 			forward = forward.next;
 		}
 		while (forward != null) {
 			forward = forward.next;
 			current = current.next;
 		}
 		return current.data;
 	}
 	
 	/**
 	 * Removes the Node from the list by manipulating the data and nodes around it.
 	 * 
 	 * @param n - The node to remove from the list
 	 * @throws IllegalArgumentException if the node is null or is the last item in the list
 	 */
 	private void removeNode(Node n) {
 		if (n == null || n.next == null) {
 			throw new IllegalArgumentException();
 		}
 		Node next = n.next;
 		n.data = next.data;
 		n.next = next.next;
 	}
 	
 	/**
 	 * Returns a new list that is the summation of the current list and the other list.
 	 * 
 	 * @param other - The other list to add to this one
 	 * @return A new list that is the summation of the two lists
 	 */
 	public SingleLinkedList<Integer> addList(SingleLinkedList<Integer> other) {
 		SingleLinkedList<Integer> result = new SingleLinkedList<Integer>();
 		reverse();
 		other.reverse();
 		Node first = this.head;
 		Node second = (Node) other.head;
 		int carry = 0;
 		while (first != null || second != null || carry == 1) {
 			int num = 0;
 			if (first != null) {
 				num += (Integer) first.data;
 				first = first.next;
 			}
 			if (second != null) {
 				num += (Integer) second.data;
 				second = second.next;
 			}
 			if (carry == 1) {
 				num++;
 				carry = 0;
 			}
 			if (num > 9) {
 				carry = 1;
 				num -= 10;
 			}
 			result.add(num, 0);
 		}
 		return result;
 	}
 	
 	/**
 	 * Reverses the List.
 	 */
 	public void reverse() {
 		Node current = head;
 		Node prev = null;
 		Node forward;
 		while (current != null) {
 			forward = current.next;
 			current.next = prev;
 			prev = current;
 			current = forward;
 		}
 		head = prev;
 	}
 	
 	/**
 	 * Reverses the List.
 	 */
 	public void reverseR() {
 		head = reverseR(head);
 	}
 	
 	/**
 	 * Reverses the linked list starting at head.
 	 * 
 	 * @param head - The head of the LinkedList
 	 * @return The head of the reversed LinkedList
 	 */
 	private Node reverseR(Node head) {
 		if (head != null && head.next != null) {
 			Node end = head.next;
 			head.next = null;
 			Node front = reverseR(end);
 			end.next = head;
 			head = front;
 		}
 		return head;
 	}
 	
 	/**
 	 * Interweave the other linked list into this one by alternating elements.
 	 * 
 	 * @param other - The other linked list
 	 */
	public void interweave(SingleLinkedList<E> other) {
		interweave(this.head, other.head);
 	}
 	
 	/**
 	 * Interweaves the two heads of the linked list into one.
 	 * 
 	 * @param head - The first list
 	 * @param other - The second list
 	 */
 	private void interweave(Node head, Node other) {
 		Node current = head;
 		while (current != null && other != null) {
 			Node temp = current.next;
 			current.next = other;
 			other = other.next;
 			current.next.next = temp;
 			current = temp;
 		}
 	}
 	
 	/**
 	 * Represents a single Node in the SinglyLinkedList.
 	 * @author RDrapeau
 	 */
 	public class Node {
 		/**
 		 * Data of the Node.
 		 */
 		public E data;
 		
 		/**
 		 * The next Node in the list.
 		 */
 		public Node next;
 		
 		public Node(E data) {
 			this(data, null);
 		}
 		
 		/**
 		 * Constructs a new Node that contains the input data and points to the next Node.
 		 * 
 		 * @param data - The data for the Node to contain
 		 * @param next - The Node to point to
 		 */
 		public Node(E data, Node next) {
 			this.data = data;
 			this.next = next;
 		}
 	}
 }
