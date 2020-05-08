 /**
  * 
  */
 package gulyan.ds;
 
 /**
  * @author gulyan
  *
  */
 public interface OList<T> {
 
 	/**
 	 * Add element to list
 	 * @param e element
 	 */
 	void push(T e);
 	
 	/**
 	 * Get next element from list and remove it from list
	 * @see top
 	 * @return element
 	 */
 	T pop();
 	
 	/**
 	 * Get next element from list
	 * @see pop
 	 * @return element
 	 */
 	T top();
 	
 	/**
 	 * Test if list is empty
 	 * @return true if list is empty, false otherwise
 	 */
 	boolean isEmpty();
 	
 	/**
 	 * Remove all elements from list
 	 */
 	void clear();
 	
 }
