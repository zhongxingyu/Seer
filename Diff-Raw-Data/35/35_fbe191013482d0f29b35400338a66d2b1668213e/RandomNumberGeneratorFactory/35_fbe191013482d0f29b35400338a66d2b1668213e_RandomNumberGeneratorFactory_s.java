 package com.swoop.util.random;
 
 /**
 * Factory interface for {@link com.swoop.util.RandomNumberGenerator}.
  */
 public interface RandomNumberGeneratorFactory
 {
 	/**
 	 * Create a new random number generator, randomly seeded.
 	 */
 	public RandomNumberGenerator createRandomNumberGenerator();
 }
