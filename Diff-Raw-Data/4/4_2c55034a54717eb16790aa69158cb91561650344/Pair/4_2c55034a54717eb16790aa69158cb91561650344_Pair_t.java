 package be.kuleuven.cs.distrinet.chameleon.util;
 
 import be.kuleuven.cs.distrinet.rejuse.contract.Contracts;
 
 /**
  * A class of pairs. A pair does not allow null values.
  * 
  * @author Marko van Dooren
  *
  * @param <T1> The type of the first element of the pair.
  * @param <T2> The type of the second element of the pair.
  */
 public class Pair<T1,T2> {
 	
 	/**
 	 * Create a new pair with the two given values.
 	 * 
 	 * @param first
 	 * @param second
 	 */
  /*@
    @ public behavior
    @
    @ pre first != null;
    @ pre second != null;
    @
   @ post first() == first;
   @ post second() == second;
    @*/
 	public Pair(T1 first, T2 second) {
 		setFirst(first);
 		setSecond(second);
 	}
 	
 	/**
 	 * Return the first object of the pair.
 	 */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @*/
 	public T1 first() {
 		return _first;
 	}
 	
 	private void setFirst(T1 first) {
 		Contracts.notNull(first, "The first element of a pair should not be null.");
 		_first = first;
 	}
 
 	private T1 _first;
 	
 	/**
 	 * Return the second object of the pair.
 	 */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @*/
 	public T2 second() {
 		return _second;
 	}
 	
 	private void setSecond(T2 second) {
 		Contracts.notNull(second, "The second element of a pair should not be null.");
 		_second = second;
 	}
 	
 	private T2 _second;
 	
 	/**
 	 * The string representation is: (first,second).
 	 */
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("(")
 		       .append(first().toString())
 		       .append(",")
 		       .append(second().toString())
 		       .append(")");
 		return builder.toString();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = prime + _first.hashCode();
 		result = prime * result + _second.hashCode();
 		return result;
 	}
 
 	/**
 	 * A pair is equal to another pair that has the same
 	 * first and second values.
 	 */
  /*@
    @ public behavior
    @
    @ post result == other instanceof Pair &&
    @                first().equals(((Pair)other).first()) &&
    @                second().equals(((Pair)other).second());
    @*/
 	@Override
 	public boolean equals(Object other) {
 		return (other instanceof Pair) && 
 				   first().equals(((Pair)other).first()) &&
 				   second().equals(((Pair)other).second());
 	}
 	
 }
