 package de.jail.geometry.distancefunctions.impl;
 
 import de.jail.geometry.distancefunctions.PointBasedDistanceFunction;
 import de.jail.geometry.schemas.Point;
 
 /**
  * 
  * This class calculates the distance between two point through the euclidean distance function. 
  * The distance function is given by the Pythagorean formula. The given formula for a n-dimensional space
  * and between two points p and q is: 
  * <p>
  * 	{@code d(p,q)=sqrt(pow(p_1 - q_1,2) + pow(p_2 - q_2,2) + ... + pow(p_n - q_n,2))}
  * </p>
  * 
  * @author Christian Vogel
  */
 public class EuclideanDistance implements PointBasedDistanceFunction {
 	
 	/**
 	 * Default constructor doing nothing special.
 	 */
 	public EuclideanDistance() {}
 
 	/**
	 * Calculates the distance between two points.
	 */
	/* (non-Javadoc)
	 * @see de.mathlib.geometry.distancefunctions.IDistanceFunction#calculate(de.mathlib.geometry.schemas.Point, de.mathlib.geometry.schemas.Point)
 	 */
 	@Override
 	public double calculate(Point arg1, Point arg2) {
 		if(arg1 == null || arg2 == null) {
 			throw new IllegalArgumentException("arguments should not be null");
 		}
 		
 		final double[] vector1 = arg1.getVector();
 		final double[] vector2 = arg2.getVector();
 		
 		if(vector1.length != vector2.length) {
 			throw new IllegalArgumentException("Both points should be in the same dimensional space.");
 		}
 		
 		double sum = 0;
 		
 		for(int i = 0; i < vector1.length; i++) {
			sum = Math.pow((vector1[i] - vector2[i]), 2);
 		}
 		
 		return Math.sqrt(sum);
 	}
 
 }
