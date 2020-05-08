 // DOUBLE ended, SINGLY linked list.
 import java.util.Iterator;
 public class DSALinkedList<E> implements Iterable<E> {
 	private class DSAListNode<E> {
 		private E thing;
 		private DSAListNode<E> next;
 		public DSAListNode(E thing) {
 			this.thing = thing;
 			this.next = null;
 		}
 		public E getValue() {
 			return this.thing;
 		}
 		public DSAListNode<E> getNext() {
 			return this.next;
 		}
 	}
 	private class DSALinkedListIterator<E> implements Iterator<E> {
 		private DSALinkedList<E>.DSAListNode<E> iterNext;
 		public DSALinkedListIterator(DSALinkedList<E> list) {
 			iterNext = list.head;
 		}
 		public boolean hasNext() {
 			return iterNext != null;
 		}
 		public E next() {
 			E value;
 			if (iterNext == null) {
 				value = null;
 			} else {
 				value = iterNext.getValue();
 				iterNext = iterNext.getNext();
 			}
 			return value;
 		}
 		public void remove() {
 			throw new UnsupportedOperationException(
 				"not supported"
 			);
 		}
 	}
 	private DSALinkedList<E>.DSAListNode<E> head;
 	private DSALinkedList<E>.DSAListNode<E> tail;
 	public DSALinkedList() {
 		this.head = null;
 		this.tail = null;
 	}
 	public boolean isEmpty() {
 		return (head == null) && (tail == null);
 	}
 	public E peekFirst() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't peek the first element of empty list"
 			);
 		return this.head.getValue();
 	}
 	public E peekLast() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't peek the last element of empty list"
 			);
 		return this.tail.getValue();
 	}
 	public void insertFirst(E thing) {
		DSALinkedList<E>.DSAListNode<E> node =
			new DSALinkedList<E>.DSAListNode<E>(thing);
 		if (this.isEmpty())
 			this.tail = node;
 		else
 			node.next = this.head;
 		this.head = node;
 	}
 	public void insertLast(E thing) {
		DSALinkedList<E>.DSAListNode<E> node =
			new DSALinkedList<E>.DSAListNode<E>(thing);
 		if (this.isEmpty())
 			this.head = node;
 		else
 			this.tail.next = node;
 		this.tail = node;
 	}
 	public E removeFirst() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't remove the first element of empty list"
 			);
 		if (this.head == this.tail)
 			this.tail = null;
 		E thing = this.head.getValue();
 		this.head = this.head.getNext();
 		return thing;
 	}
 	public E removeLast() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't remove the last element of empty list"
 			);
 		E thing = this.tail.getValue();
 		if (this.head == this.tail)
 			this.head = this.tail = null;
 		else for (
 			DSALinkedList<E>.DSAListNode<E> cur = this.head;
 			cur != null;
 			cur = cur.getNext()
 		)
 			if (cur.getNext() == this.tail)
 				this.tail = cur;
 		return thing;
 	}
 	public Iterator<E> iterator() {
 		return new DSALinkedListIterator<E>(this);
 	}
 }
