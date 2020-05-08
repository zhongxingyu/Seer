 // DOUBLE ended, SINGLY linked list.
 import java.util.Iterator;
public class DSALinkedList {
 	private class DSAListNode {
 		private Object thing;
 		private DSAListNode next;
 		public DSAListNode(Object thing) {
 			this.thing = thing;
 			this.next = null;
 		}
 		public Object getValue() {
 			return this.thing;
 		}
 		public DSAListNode getNext() {
 			return this.next;
 		}
 	}
 	private class DSALinkedListIterator implements Iterator {
 		private DSAListNode iterNext;
 		public DSALinkedListIterator(DSALinkedList list) {
 			iterNext = list.head;
 		}
 		public boolean hasNext() {
 			return iterNext != null;
 		}
 		public Object next() {
 			Object value;
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
 	private DSAListNode head;
 	private DSAListNode tail;
 	public DSALinkedList() {
 		this.head = null;
 		this.tail = null;
 	}
 	public boolean isEmpty() {
 		return (head == null) && (tail == null);
 	}
 	public Object peekFirst() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't peek the first element of empty list"
 			);
 		return this.head.getValue();
 	}
 	public Object peekLast() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't peek the last element of empty list"
 			);
 		return this.tail.getValue();
 	}
 	public void insertFirst(Object thing) {
 		DSAListNode node = new DSAListNode(thing);
 		if (this.isEmpty())
 			this.tail = node;
 		else
 			node.next = this.head;
 		this.head = node;
 	}
 	public void insertLast(Object thing) {
 		DSAListNode node = new DSAListNode(thing);
 		if (this.isEmpty())
 			this.head = node;
 		else
 			this.tail.next = node;
 		this.tail = node;
 	}
 	public Object removeFirst() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't remove the first element of empty list"
 			);
 		if (this.head == this.tail)
 			this.tail = null;
 		Object thing = this.head.getValue();
 		this.head = this.head.getNext();
 		return thing;
 	}
 	public Object removeLast() {
 		if (this.isEmpty())
 			throw new IllegalStateException(
 				"can't remove the last element of empty list"
 			);
 		Object thing = this.tail.getValue();
 		if (this.head == this.tail)
 			this.head = this.tail = null;
 		else for (
 			DSAListNode cur = this.head;
 			cur != null;
 			cur = cur.getNext()
 		)
 			if (cur.getNext() == this.tail)
 				this.tail = cur;
 		return thing;
 	}
 }
