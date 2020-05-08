 package org.emmef.cheapsets;
 
 public interface IndexFunction {
 	/**
	 * Returns the index of the element, which lies between {@code 0} and {@code size() - 1}.
 	 * @param element element to search index for
	 * @return the index of the element, which lies between {@code 0} and {@code size() - 1}. 
 	 */
 	int indexOf(Object element);
 	
 	/**
	 * returns the size of the index.
 	 * @return a positive integer
 	 */
 	int indexSize();
 }
