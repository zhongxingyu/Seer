 package cs437.som.network;
 
 import cs437.som.Dimension;
 import cs437.som.SOMError;
 import cs437.som.TrainableSelfOrganizingMap;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.security.SecureRandom;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Common functionality for basic self-organizing maps.
  */
 public abstract class NetworkBase implements TrainableSelfOrganizingMap {
     /**
      * The initial, default neighborhood width.
      */
     public static final double INITIAL_NEIGHBORHOOD_WIDTH = 5.0;
 
     /**
      * The default learning rate.
      */
     public static final double alpha = 0.08;
 
     /**
      * The current training iteration.
      */
     protected int time = 0;
 
     /**
      * The count of neurons.
      */
     protected final int neuronCount;
 
     /**
      * The length of the input vector.
      */
     protected final int inputVectorSize;
 
     /**
      * The expected number of training iterations.
      */
     protected final int expectedIterations;
 
     /**
      * A matrix of the neurons input weights.  The "left" index is the neuron
      * and the "right" index is the input weight.
      */
     protected double[][] weightMatrix;
 
     /**
      * The dimensions of the map's neuron grid.
      */
     protected final Dimension gridSize;
 
     /**
      * Constructs the common functionality for SOMs.
      *
      * @param gridSize The neuron grid dimensions.
      * @param inputVectorSize The length of expected input vectors
      * @param expectedIterations The expected count of iterations for training.
      */
     protected NetworkBase(Dimension gridSize, int inputVectorSize,
                           int expectedIterations) {
         this.inputVectorSize = inputVectorSize;
         this.expectedIterations = expectedIterations;
         this.gridSize = gridSize;
         this.neuronCount = gridSize.area;
 
         weightMatrix = new double[neuronCount][inputVectorSize];
         initialize();
     }
 
     public int getBestMatchingNeuron(double[] input) {
         checkInput(input);
 
         int bestMatch = 0;
         double lowestDistance2 = distanceToInput(0, input);
         for (int i = 1; i < neuronCount; i++) {
             double distance2temp = distanceToInput(i, input);
             if (distance2temp < lowestDistance2) {
                 lowestDistance2 = distance2temp;
                 bestMatch = i;
             }
         }
         return bestMatch;
     }
 
     public int getBestMatchingNeuron(int[] input) {
         double[] dbls = new double[input.length];
         for (int i = 0; i < input.length; i++) {
             dbls[i] = input[i];
         }
         return getBestMatchingNeuron(dbls);
     }
 
     public int getExpectedIterations() {
         return expectedIterations;
     }
 
     public Dimension getGridSize() {
         return gridSize;
     }
 
     public int getNeuronCount() {
         return gridSize.area;
     }
 
     public int getInputLength() {
         return inputVectorSize;
     }
 
     public double getWeight(int neuron, int weightIndex) {
         return weightMatrix[neuron][weightIndex];
     }
 
     public void trainWith(double[] data) {
         checkInput(data);
 
         int best = getBestMatchingNeuron(data);
         adjustNeuronWeights(best, data);
         adjustNeighborsOf(best, data);
         time++;
     }
 
     public void trainWith(int[] data) {
         double[] dbls = new double[data.length];
         for (int i = 0; i < data.length; i++) {
             dbls[i] = data[i];
         }
         trainWith(dbls);
     }
 
     /**
      * Create a string to display the neurons' weights.
      *
      * @return A string grid of the neurons weights (neurons left to right,
      * weights top to bottom).
      */
     public String weightString() {
         StringBuilder sb = new StringBuilder(weightMatrix.length * 3);
 
         for (double[] aWeightMatrix : weightMatrix) {
             sb.append(Arrays.toString(aWeightMatrix));
             sb.append(System.lineSeparator());
         }
         sb.append(System.lineSeparator());
         return sb.toString();
     }
 
     /**
      * Calculate the learning rate (alpha in most equations) for the current
      * iteration. The current iteration will be taken from the object's current
      * count
      *
      * @return The learning rate for the current iteration.
      */
     protected double learningRate() {
         return alpha;
     }
 
     /**
      * Verify that a given input vector's length matches the length expected by
      * the map.
      *
      * @param input The input vector to inspect.
      * @throws cs437.som.SOMError when the length of input does not match the
      * expected length.
      */
     protected void checkInput(double[] input) throws SOMError {
         if (input.length != inputVectorSize) {
             throw new SOMError(
                     "Input vector length does not match network input size.");
         }
     }
 
     /**
      * Initializes the neuron's weight matrix to all random doubles.
      */
     private void initialize() {
         Random r = new SecureRandom();
         for (int i = 0; i < neuronCount; i++) {
             for (int j = 0; j < inputVectorSize; j++) {
                 weightMatrix[i][j] = r.nextDouble();
             }
         }
     }
 
     /**
      * Adjust the weights of a neuron to more closely match a given input vector.
      *
      * @param neuron The index of the neuron to adjust.
      * @param input The input vector to adjust towards.
      */
     protected void adjustNeuronWeights(int neuron, double[] input) {
         for (int i = 0; i < weightMatrix[neuron].length; i++) {
             double delta = input[i] - weightMatrix[neuron][i];
             weightMatrix[neuron][i] += learningRate() * delta;
         }
     }
 
     /**
      * Adjust the weights of all neurons in the neighborhood if a given neuron to
      * more closely match a given input vector.
      *
      * @param neuron The index of the neuron who's neighborhood will be examined.
      * @param input The input vector to adjust towards.
      */
     protected void adjustNeighborsOf(int neuron, double[] input) {
         for (int i = 0; i < neuronCount; i++) {
             if (i != neuron && inNeighborhoodOf(neuron, i)) {
                 adjustNeuronWeights(i, input);
             }
         }
     }
 
     /**
      * Decide whether a neuron is in another neuron's neighborhood.  The
      * parameters imply a specific ordering, but in most cases this is an
      * unnecessary constraint.  This would only be the case if d(i, j) != d(j, i).
      * Asymmetric metrics are likely very uncommon, and it should be safe to
      * ignore this constraint.
      *
      * @param bestMatchingNeuron The winning neuron who's neighborhood we are
      * testing.
      * @param testNeuron The neuron we're testing for neighborhood inclusion.
      * @return true if the testNeuron is in the neighborhood of
      * bestMatchingNeuron, false otherwise
      */
     protected boolean inNeighborhoodOf(int bestMatchingNeuron, int testNeuron) {
         return neuronDistance(bestMatchingNeuron, testNeuron) < neighborhoodWidth();
     }
 
     /**
      * Get the width of the neighborhood of adjustment at a given iteration.
      * The iteration is taken from the object's current count.
      *
      * @return The width of the neighborhood of adjustment for the current
      * iteration.
      */
     protected double neighborhoodWidth() {
         return INITIAL_NEIGHBORHOOD_WIDTH *
                 (1.0 - (time / (double) expectedIterations));
     }
 
     /**
      * Calculate the distance between 2 neurons, given the map's layout.
      *
      * @param neuron0 The index of the first neuron.
      * @param neuron1 The index of the second neuron.
      * @return The distance between the two neurons.
      */
     protected abstract double neuronDistance(int neuron0, int neuron1);
 
     /**
      * Measure the distance from a neuron (specifically, its weight vector) to
      * an input vector.
      *
      * @param neuron The index of the neuron in question.
      * @param input The input vector.
      * @return The distance from the neuron to the vector.
      */
     protected double distanceToInput(int neuron, double[] input) {
         double sum = 0.0;
         for (int i = 0; i < inputVectorSize; i++) {
             double difference = input[i] - weightMatrix[neuron][i];
             sum += difference * difference;
         }
         return sum;
     }
 
     @Override
     public String toString() {
         return "NetworkBase{" +
                 "time=" + time +
                 ", dimensions=" + gridSize +
                 ", inputVectorSize=" + inputVectorSize +
                 ", expectedIterations=" + expectedIterations +
                 ", weightMatrix=" + weightString() +
                 '}';
     }
 
     public void write(OutputStreamWriter destination) throws IOException {
         destination.write(String.format("Grid dimensions: %d, %d%n",
                 gridSize.x, gridSize.y));
         destination.write(String.format("Input length: %d%n", inputVectorSize));
         destination.write(String.format("Weights:%n"));
         for (double[] doubles : weightMatrix) {
             destination.write(String.format("\t%s%n", Arrays.toString(doubles)));
         }
         destination.write(String.format("end weights%n"));
     }
 
     /**
      * Read a weight matrix from a stored map.
      *
      * @param input The input to read from
      * @param entryCount The number of neurons to read for.
      * @param length The input vector length.
      * @return A weight matrix that can be dropped into a SOM.
      * @throws java.io.IOException if something fails while reading the stream.
      */
     protected static double[][] readWeightMatrix(BufferedReader input,
                                                  int entryCount, int length) throws IOException {
         Pattern endTagRegEx = Pattern.compile("end\\s*(?:weights)",
                 Pattern.CASE_INSENSITIVE);
         Pattern weightVectorRegEx = Pattern.compile(
                 "([+-]?[0-9]*\\.?[0-9]+(?:[Ee][+-]?[0-9]+)?)(?:,?\\s*)?");
 
         int readLines = 0;
         double[][] weights = new double[entryCount][length];
 
         String line = input.readLine();
         while (readLines < entryCount && input.ready() &&
                 !endTagRegEx.matcher(line).matches()) {
             Matcher weightMatch = weightVectorRegEx.matcher(line);
             for (int i = 0; i < length; i++) {
                 weightMatch.find();
                 weights[readLines][i] =
                         Double.parseDouble(weightMatch.group(1));
             }
             line = input.readLine();
             readLines++;
         }
 
         return weights;
     }
 
     protected static class SOMFileReader {
         private static final Pattern dimensionRegEx = Pattern.compile(
                 "(?:grid)?\\s*dimensions\\s*:\\s*(\\d+)\\s*,\\s*(\\d+)",
                 Pattern.CASE_INSENSITIVE);
         private static final Pattern inputVectorSizeRegEx = Pattern.compile(
                 "(?:input)?\\s*length\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
         private static final Pattern iterationsRegEx = Pattern.compile(
                 "iterations\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
         private static final Pattern weightRegEx = Pattern.compile(
                 "weights\\s*(?::)", Pattern.CASE_INSENSITIVE);
 
         public Dimension dimension = null;
         public int inputVectorSize = 0;
         public int iterations = 0;
 
         public void parse(BufferedReader input) throws IOException {
             String line = input.readLine();
             Matcher match = weightRegEx.matcher(line);
             while (! match.matches() && input.ready()) {
                 if (!matchDimension(line)
                         && !matchInputVectorSize(line)
                         && !matchIterations(line)) {
                     unmatchedLine(line);
                 }
 
                 line = input.readLine();
                 match = weightRegEx.matcher(line);
             }
 
             if (dimension == null || inputVectorSize < 1) {
                throw new SOMError("A valid dimension and input vector size must" +
                        " appear in a map's configuration%nand they must appear " +
                        "before the weight matrix.");
             }
         }
 
         protected void unmatchedLine(String line) {
         }
 
         private boolean matchIterations(String line) {
             Matcher iterationsMatch = iterationsRegEx.matcher(line);
             if (iterationsMatch.matches()) {
                 iterations = Integer.parseInt(iterationsMatch.group(1));
                 return true;
             }
             return false;
         }
 
         private boolean matchInputVectorSize(String line) {
             Matcher inputMatch = inputVectorSizeRegEx.matcher(line);
             if (inputMatch.matches()) {
                 inputVectorSize = Integer.parseInt(inputMatch.group(1));
                 return true;
             }
             return false;
         }
 
         private boolean matchDimension(String line) {
             Matcher dimMatch = dimensionRegEx.matcher(line);
             if (dimMatch.matches()) {
                 dimension = new Dimension(Integer.parseInt(dimMatch.group(1)),
                         Integer.parseInt(dimMatch.group(2)));
                 return true;
             }
             return false;
         }
 
     }
 }
