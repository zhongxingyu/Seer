 package cs437.som.distancemetrics;
 
 import cs437.som.DistanceMetric;
 import cs437.som.SOMError;
 
 /**
  * Chebyshev distance strategy.
  *
  * The Chebyshev distance of 2 vectors is the maximum of the absolute value of
  * the differences of the individual components of the 2 vectors.
  *
  * The exact behavior follows the formula:
 * <pre>
  *      for v_1, v_2 \in \mathbb{R}^n
  *      \max|v_{1_i} - v_{2_i}|, i=1..n
 * </pre>
  */
 public class ChebyshevDistanceMetric implements DistanceMetric {
 
     /**
      * Calculate the Chebyshev distance between 2 vectors.
      *
      * @param v0 The first vector.
      * @param v1 The second vector.
      * @return The distance between v0 and v1.
      * @throws cs437.som.SOMError If the vector sizes do not match.
      */
     public double distance(double[] v0, double[] v1) throws SOMError {
         if (v0.length != v1.length) {
             throw new SOMError("ChebyshevDistanceMetric: input vector lengths do not match.");
         }
 
         double max = 0.0;
         for (int i = 0; i < v0.length; i++) {
             double difference = Math.abs(v0[i] - v1[i]);
             if (difference > max) {
                 max = difference;
             }
         }
 
         return max;
     }
 
     @Override
     public String toString() {
         return "ChebyshevDistanceMetric";
     }
 }
