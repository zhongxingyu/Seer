 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Neighborhood width strategy for self-organizing maps that decays the width
  * by the Gaussian function, centered at 0 and parametrized by the standard
  * deviation.
  *
  * <pre>
  * The exact behavior follows the formula:
 *      \frac{1}{\sigma \sqrt{2 \pi}} e^{-\frac{t^2}{2\sigma^2}}
  *  where
  *      \sigma is the standard deviation of the Gaussian
  *      e      is the base of the natural logarithm
  *      t      is the current iteration
  * </pre>
  */
 public class GaussianNeighborhoodWidthFunction
         implements NeighborhoodWidthFunction, ContinuousUnitNormal {
     private final double stdDeviation;
     private final double coefficient;
 
     /**
      * Create a Gaussian neighborhood width function.
      *
      * @param standardDeviation The standard deviation of the Gaussian curve.
      */
     public GaussianNeighborhoodWidthFunction(double standardDeviation) {
         stdDeviation = standardDeviation;
         coefficient = 1 / (stdDeviation * Math.sqrt(2 * Math.PI));
     }
 
     public void setExpectedIterations(int expectedIterations) {
     }
 
     public double neighborhoodWidth(int iteration) {
         return coefficient *
                Math.exp(-(iteration * iteration) / (2 * stdDeviation * stdDeviation));
     }
 
     @Override
     public String toString() {
         return "GaussianNeighborhoodWidthFunction " + stdDeviation;
     }
 }
