 package com.willhains.equality;
 
 import java.util.*;
 
 /**
  * A utility for implementing near-ideal {@link Object#equals(Object)} methods, using a clean, easy-to-read API. Sample:
  * 
  * <pre>
  * public boolean equals(Object obj)
  * {
  *     Equals eq = Equals.compare(this, obj);
  *     return eq
  *         .and(this.name,        eq.that.name)
  *         .and(this.productCode, eq.that.productCode)
  *         .and(this.colour,      eq.that.colour)
  *         .equals();
  * }
  * </pre>
  * 
  * Note: If your superclass contains state that is checked in its {@link Object#equals(Object)} implementation, you
  * should call it as follows:
  * 
  * <pre>
  * public boolean equals(Object obj)
  * {
  *     Equals eq = Equals.compare(this, obj);
  *     return super.equals(obj) && eq                // <-- note the call to super.equals(obj)
  *         .and(this.name,        eq.that.name)
  *         .and(this.productCode, eq.that.productCode)
  *         .and(this.colour,      eq.that.colour)
  *         .equals();
  * }
  * </pre>
  * 
  * @param <V> the class whose {@link Object#equals(Object)} method is being implemented.
  * @author willhains
  */
 public final class Equals<V>
 {
 	// The equality comparison result: innocent until proven guilty
 	private boolean _eq = true;
 	
 	// Short circuit for when it is pointless to continue comparison (false when conclusion is foregone)
 	private boolean _ok = true;
 	
 	/**
 	 * Object to be compared against, guaranteed to never be null. In reality, this might not be the object passed into
 	 * the {@link Object#equals(Object)} method, but you can code as though it is. There is a magic trick here, so don't
 	 * use it for anything except the call-chaining pattern documented above.
 	 */
 	public final V that;
 	
 	/**
 	 * Initialises the state for the most efficient comparison. There are two possible scenarios:
 	 * (a) we are comparing two different, but potentially equal objects, or
 	 * (b) we can determine equality/inequality immediately.
 	 */
 	@SuppressWarnings("unchecked")
 	private Equals(final V thiss, final Object obj)
 	{
 		// Scenario A: Comparing two different, but potentially equal objects
		if(thiss != obj && obj != null && getClass().isAssignableFrom(obj.getClass())) that = (V)obj;
 		
 		// Scenario B: We can determine equality/inequality immediately and skip comparison of object state
 		else
 		{
 			// Use 'thiss' as a decoy to guarantee 'that' will never be null
 			that = thiss;
 			
 			// Determine equality/inequality now
 			_eq = obj == thiss;
 			
 			// Skip all comparisons
 			_ok = false;
 		}
 	}
 	
 	/**
 	 * Initiates a comparison between two objects.
 	 * 
 	 * @param thiss the object where {@link Object#equals(Object)} is being implemented (ALWAYS pass 'this').
 	 * @param obj the object passed into {@link Object#equals(Object)}.
 	 */
	public <V> Equals<V> compare(final V thiss, final Object obj)
 	{
 		assert thiss != null : "You must pass 'this' into the 'thiss' argument (so it should never be null).";
 		return new Equals<V>(thiss, obj);
 	}
 	
 	// Updates the state to skip subsequent comparisons once inequality is determined
 	private void _and(boolean equals)
 	{
 		assert _ok : "Should not have reached this method if _ok was already false.";
 		if(!equals) _eq = _ok = false;
 	}
 	
 	// @formatter:off
 	public Equals<V> and(boolean n, boolean nn) { if(_ok) _and(n == nn); return this; } 
 	public Equals<V> and(   byte b,    byte bb) { if(_ok) _and(b == bb); return this; }
 	public Equals<V> and(   char c,    char cc) { if(_ok) _and(c == cc); return this; }
 	public Equals<V> and(  short s,   short ss) { if(_ok) _and(s == ss); return this; }
 	public Equals<V> and(    int i,     int ii) { if(_ok) _and(i == ii); return this; }
 	public Equals<V> and(   long l,    long ll) { if(_ok) _and(l == ll); return this; }
 	public Equals<V> and(  float f,   float ff) { if(_ok) _and(f == ff); return this; }
 	public Equals<V> and( double d,  double dd) { if(_ok) _and(d == dd); return this; }
 	public Equals<V> and( Object o,  Object oo) { if(_ok) _and(o == null ? oo == null : o.equals(oo)); return this; }
 	
 	public Equals<V> and(boolean[] n, boolean[] nn) { if(_ok) _and(Arrays.equals(n, nn)); return this; }
 	public Equals<V> and(   byte[] b,    byte[] bb) { if(_ok) _and(Arrays.equals(b, bb)); return this; }
 	public Equals<V> and(   char[] c,    char[] cc) { if(_ok) _and(Arrays.equals(c, cc)); return this; }
 	public Equals<V> and(  short[] s,   short[] ss) { if(_ok) _and(Arrays.equals(s, ss)); return this; }
 	public Equals<V> and(    int[] i,     int[] ii) { if(_ok) _and(Arrays.equals(i, ii)); return this; }
 	public Equals<V> and(   long[] l,    long[] ll) { if(_ok) _and(Arrays.equals(l, ll)); return this; }
 	public Equals<V> and(  float[] f,   float[] ff) { if(_ok) _and(Arrays.equals(f, ff)); return this; }
 	public Equals<V> and( double[] d,  double[] dd) { if(_ok) _and(Arrays.equals(d, dd)); return this; }
 	public Equals<V> and( Object[] o,  Object[] oo) { if(_ok) _and(Arrays.deepEquals(o, oo)); return this; }
 	// @formatter:on
 	
 	/**
 	 * @return the result of the comparison.
 	 */
 	public boolean equals()
 	{
 		return _eq;
 	}
 }
