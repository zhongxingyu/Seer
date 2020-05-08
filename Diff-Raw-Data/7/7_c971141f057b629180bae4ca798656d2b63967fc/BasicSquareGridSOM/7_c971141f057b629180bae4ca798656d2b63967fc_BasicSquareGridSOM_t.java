 package cs437.som.network;
 
 import cs437.som.Dimension;
 
 import java.util.Arrays;
 
 /**
  * A simple self-organizing map, using a square grid for the neurons.
  *
  * An simple example usage:
  *
  * <code>
  * <pre>
  * private static final int iterations = 500;
  *
  * public static void main(String[] args) {
  *     SelfOrganizingMap som = new BasicSquareGridSOM(7, 2, iterations);
  *     Random r = new SecureRandom();
  *
  *     for (int i = 0; i < iterations; i++) {
  *         double[] in = {r.nextDouble() * 10, r.nextDouble() * 10};
  *         som.trainWith(in);
  *     }
  *
  *     // At this point, the SOM would be queried for it's BMU.
  * }
  * </pre>
  * </code>
  */
 public class BasicSquareGridSOM extends NetworkBase {
 
     public BasicSquareGridSOM(Dimension gridSize, int inputVectorSize, int expectedIterations) {
         super(gridSize, inputVectorSize, expectedIterations);
     }
 
     @Override
     protected double neuronDistance(int neuron0, int neuron1) {
         int row0 = neuron0 / gridSize.x;
         int col0 = neuron0 % gridSize.x;
         int row1 = neuron1 / gridSize.x;
         int col1 = neuron1 % gridSize.x;
 
         int dr = row1 - row0;
         int dc = col1 - col0;
 
         return Math.sqrt((dr * dr) + (dc * dc));
     }
 
     @Override
     public String toString() {
         return "BasicSquareGridSOM{time=" + time +
                 ", weightMatrix=" + (weightMatrix == null ? null : Arrays.asList(weightMatrix)) +
                 ", neuronCount=" + neuronCount + ", inputSize=" + inputVectorSize + '}';
     }
 }
