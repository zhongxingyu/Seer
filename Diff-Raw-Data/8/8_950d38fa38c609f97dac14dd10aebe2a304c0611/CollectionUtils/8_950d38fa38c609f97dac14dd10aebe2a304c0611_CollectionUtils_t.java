 package com.alexrnl.commons.utils;
 
 import java.util.Iterator;
 import java.util.Objects;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utilities on collections.<br />
  * @author Alex
  */
 public final class CollectionUtils {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(CollectionUtils.class.getName());
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default private constructor.
 	 */
 	private CollectionUtils () {
 		super();
 	}
 	
 	/**
 	 * Check if a collection is sorted.<br />
 	 * @param collection
 	 *        the collection to check.
 	 * @return <code>true</code> if the collection is sorted.
 	 */
 	public static <T extends Comparable<? super T>> boolean isSorted (final Iterable<T> collection) {
 		Objects.requireNonNull(collection);
 		final Iterator<T> it = collection.iterator();
 		
 		// Empty collection case
 		if (!it.hasNext()) {
 			return true;
 		}
 
 		T current = it.next();
 		while (it.hasNext()) {
 			final T next = it.next();
			if (current.compareTo(next) > 0) {
 				if (lg.isLoggable(Level.INFO)) {
 					lg.info("Collection is unordered at element " + current);
 				}
 				return false;
 			}
 			current = next;
 		}
 		
 		return true;
 	}
 }
