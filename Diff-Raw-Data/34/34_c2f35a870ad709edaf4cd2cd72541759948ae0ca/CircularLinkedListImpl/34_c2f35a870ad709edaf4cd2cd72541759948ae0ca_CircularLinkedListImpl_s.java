 /**
  * 
  */
 package poker.unused;
 
 import java.util.Iterator;
 
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

 public class CircularLinkedListImpl<T> implements Iterable<T>, CircularLinkedList<T>{
 	
 	private Node<T> head = null;
 	private int size = 0;
 	
 	@Override
 	public boolean isEmpty() {
 		return (size == 0);
 	}
 	
 	@Override
 	public void add(T t) {
 		Node<T> newNode = new Node<T>(t);
 		if (size == 0) {
 			head = newNode;
 		} else {
 			Node<T> end = head.previous;
 			end.next = newNode;
 			newNode.next = head;
 			head.previous = newNode;
 			newNode.previous = end;
 		}
 		size++;
 	}
 
 	@Override
 	public boolean remove(T t) {
 		if ((size==1) && head.contents.equals(t)) {
 			head = null;
 			size--;
 			return true;
 		} else if (size>1) {
 			Node<T> curr = head;
 			do {
 				if (curr.contents.equals(t)) {
 					curr.previous.next = curr.next;
 					curr.next.previous = curr.previous;
 					size--;
 					return true;
 				}
 				curr = curr.next;	
 			} while (curr != head);
 		}
 		return false;
 	}
 	
 	@Override
 	public void moveHead(int n) {
 		if (size!=0) {
 			for (int i=0; i<n; i++) {
 				head = head.next;
 			}
 		}
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return new Iterator<T>() {
 			Node<T> end = (size==0)? null: head.previous;
 			Node<T> curr = end;
 			int at = 0;
 
 			@Override
 			public boolean hasNext() {
 				return (at!=size);
 			}
 
 			@Override
 			public T next() {
 				curr = curr.next;
 				at++;
 				return curr.contents;
 			}
 
 			@Override
 			public void remove() {
				throw new NotImplementedException();
 			}
 			
 		};
 	}
 	
 	@Override
 	public String toString() {
 		String result = "";
 		if (head==null) {
 			return result;
 		}
 		Node<T> curr = head;
 		StringBuffer buf = new StringBuffer();
 		do {
 			buf.append(curr.equals(head)?"":",");
 			buf.append(curr.toString());
 			curr = curr.next;
 		} while (!curr.equals(head));
 		result = buf.toString();
 		return result;
 	}
 	
 	public class Node<V> {
 		private V contents;
 		private Node<V> next;
 		private Node<V> previous;
 		
 		public Node(V v) {
 			this.contents = v;
 			this.next = this;
 			this.previous = this;
 		}
 		
 		@Override
 		public String toString() {
 			return contents.toString();
 		}
 	}
 }
