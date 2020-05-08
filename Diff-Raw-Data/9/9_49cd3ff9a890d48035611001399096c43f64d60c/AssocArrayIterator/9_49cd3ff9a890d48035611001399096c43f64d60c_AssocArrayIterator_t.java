 package AssocArray;
 
 import java.util.Iterator;
 
 
 public class AssocArrayIterator<E> implements Iterator<E> {
 
 	private AssocArray<E> tempList;
 	private int position;
 	
 	public AssocArrayIterator(AssocArray<E> list) {
 		tempList = list;
 		position = 0;
 	}
 	
 	/**
 	*	Looks ahead to see if there is another element in the Array
 	*	
 	*	Returns True if there is another element; False otherwise
 	**/
 	public boolean hasNext() {
		return position < tempList.length();
 	}
 
 	/**
 	*	Returns the current Element and then moves the iterator ahead to the next Element
 	**/
 	public E next() {
 		E temp = tempList.get(position);
 		position++;
 		return temp;
 	}
 
 	/**
 	 * Not Supported with this implementation
 	 */
 	public void remove() {
 		throw new UnsupportedOperationException();
 		
 	}
 
 
 }
