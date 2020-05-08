 package com.comphenix.xp.lookup;
 
 public interface Multipliable<T> {
 	/**
 	 * Returns a shallow copy of this object with a different experience multiplier.
	 * @param newMultiplier - New multiplier value.
 	 * @return Shallow copy of this object.
 	 */
 	public T withMultiplier(double newMultiplier);
 	
 	// Retrieve the current multiplier
 	public double getMultiplier();
 }
