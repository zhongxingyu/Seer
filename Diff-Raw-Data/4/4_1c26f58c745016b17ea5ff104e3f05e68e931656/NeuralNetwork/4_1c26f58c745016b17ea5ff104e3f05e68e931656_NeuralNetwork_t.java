 package edu.berkeley.nlp.autoencoder;
 
 import java.util.Arrays;
 import java.util.Random;
 
 import com.google.common.base.Preconditions;
 
 import edu.berkeley.nlp.math.DifferentiableFunction;
 import edu.berkeley.nlp.math.LBFGSMinimizer;
 import edu.berkeley.nlp.util.Pair;
 
 public class NeuralNetwork {
 	private static final Random random = new Random();
 	private static final double EPSILON = 1e-1;
 	
 	private int[] layerSizes;
 	public double[] parameters;
 	
 	static public NeuralNetwork train(int[] layerSizes, 
 			double weightDecay, 
 			double sparsity, 
 			double sparsityPenaltyWeight, 
 			Iterable<Pair<double[], double[]>> examples) {
 		Preconditions.checkArgument(layerSizes.length >= 2);
 		for (int layerSize : layerSizes)
 			Preconditions.checkArgument(layerSize > 0);
 		
 		LossFunction toBeMinimized = new LossFunction(layerSizes, weightDecay, sparsity, sparsityPenaltyWeight, examples);
 		double[] parameters = new LBFGSMinimizer(100).minimize(
 				toBeMinimized,
 				toBeMinimized.initial(), 
 				1e-4); // TODO make tunable 
 		
 		return new NeuralNetwork(layerSizes, parameters);
 	}
 	
 	public NeuralNetwork(int[] layerSizes, double[] parameters) {
 		Preconditions.checkArgument(layerSizes.length >= 2);
 		for (int layerSize : layerSizes)
 			Preconditions.checkArgument(layerSize > 0);
 		
 		this.layerSizes = Arrays.copyOf(layerSizes, layerSizes.length);
 		this.parameters = parameters;
 	}
 	
 	public double[] getOutput(double[] input) {
 		double[][] activations = getActivations(input, layerSizes, parameters);
 		return activations[activations.length - 1];
 	}
 	
 	public double[] getHiddenOutput(double[] input) {
 		double[][] activations = getActivations(input, layerSizes, parameters);
 		return activations[1];
 	}
 	
 	static private double sigmoid(double z) {
 		return 1.0 / (1 + Math.exp(-z));
 	}
 	
 	static public double[][] getActivations(double[] input, int[] layerSizes, double[] parameters) {
 		Preconditions.checkArgument(input.length == layerSizes[0]);
 		double[][] result = new double[layerSizes.length][];
 		
 		double[] activations = input;
 		result[0] = activations;
 		
 		int weightsBegin = 0;
 		for (int l = 1; l < layerSizes.length; ++l) {
 			// If sizes are [A, B, C], need BxA and CxB matrices, and Bx1 and Cx1 vectors
 			int biasBegin = weightsBegin + layerSizes[l - 1] * layerSizes[l];
 			
 			double[] newActivations = new double[layerSizes[l]];
 			// z(l+1) = W(l)a(l) + b(l); a(l+1) = f(z(l+1))
 			for (int row = 0; row < layerSizes[l]; ++row) {
 				double accum = parameters[biasBegin + row];
 				for (int col = 0; col < layerSizes[l-1]; ++col)
 					accum += parameters[weightsBegin + row * layerSizes[l-1] + col] * activations[col];
 				newActivations[row] = sigmoid(accum);
 			}
 			
 			activations = newActivations;
 			weightsBegin = biasBegin + layerSizes[l];
 			result[l] = activations;
 		}
 		
 		return result;
 	}
 	
 	static public class LossFunction implements DifferentiableFunction {
 		private int dimension;
 		private int[] layerSizes;
 		private double weightDecay;
 		private double sparsity;
 		private double sparsityPenalty;
 		private Iterable<Pair<double[], double[]>> examples;
 		
 		public LossFunction(int[] layerSizes, double weightDecay, double sparsity, double sparsityPenalty, Iterable<Pair<double[], double[]>> examples) {
 			this.layerSizes = layerSizes;
 			dimension = 0;
 			for (int l = 1; l < layerSizes.length; ++l)
 				// If sizes are [A, B, C], need BxA and CxB matrices, and Bx1 and Cx1 vectors
 				dimension += (layerSizes[l - 1] + 1) * layerSizes[l];
 			
 			this.weightDecay = weightDecay;
 			this.sparsity = sparsity;
 			this.sparsityPenalty = sparsityPenalty;
 			
 			this.examples = examples;
 		}
 		
 		public double[] initial() {
 			double[] result = new double[dimension];
 			for (int i = 0; i < dimension; ++i)
 				result[i] = random.nextGaussian() * EPSILON;
 			return result;
 		}
 		
 		@Override
 		public int dimension() {
 			return dimension;
 		}
 
 		@Override
 		public double valueAt(double[] x) {
 			Preconditions.checkArgument(x.length == dimension);
 			double loss = 0;
 			int count = 0;
 			
 			// Compute average activation over all input/output pairs
 			// by each hidden layer, over all nodes in it
 			double[][] averageActivations = new double[layerSizes.length][];
 			for (int i = 1; i < layerSizes.length; ++i)
 				averageActivations[i] = new double[layerSizes[i]];
 			for (Pair<double[], double[]> pair : examples) {
 				double[] input = pair.getFirst();
 				double[][] activations = getActivations(input, layerSizes, x);
 				for (int i = 1; i < activations.length; ++i)
 					for (int j = 0; j < activations[i].length; ++j)
 						averageActivations[i][j] += activations[i][j];
 				++count;
 			}
 			for (int i = 1; i < averageActivations.length; ++i)
 				for (int j = 0; j < averageActivations[i].length; ++j)
 					averageActivations[i][j] /= count;
 			
 			// Compute over all input/output pairs
 			for (Pair<double[], double[]> pair : examples) {
 				double[] input = pair.getFirst();
 				double[] output = pair.getSecond();
 				
 				double[][] activations = getActivations(input, layerSizes, x);
 				double[] predictedOutput = activations[activations.length - 1];
 				
 				// Sum of squared errors, divided by half
 				for (int i = 0; i < output.length; ++i) {
 					double error = output[i] - predictedOutput[i];
 					loss += 0.5 * error * error;  
 				}
 			}
 			loss /= count;
 			
 			// Compute regularization penalty
 			// (proportional to the squares of all the weights)
 			int weightsBegin = 0;
 			for (int l = 1; l < layerSizes.length; ++l) {
 				int weightsEnd = weightsBegin + layerSizes[l - 1] * layerSizes[l];
 				for (int i = weightsBegin; i < weightsEnd; ++i)
 					loss += (weightDecay / 2) * x[i] * x[i];
 				weightsBegin = weightsEnd + layerSizes[l];
 			}
 			
 			// Compute sparsity penalty
 			for (int l = 1; l < layerSizes.length - 1; ++l)
 				for (int i = 0; i < layerSizes[l]; ++i)
 					loss += sparsityPenalty * 
 					 	(sparsity * Math.log(sparsity / averageActivations[l][i])
 					 	 + (1 - sparsity) * Math.log((1 - sparsity) / (1 - averageActivations[l][i])));			
 			return loss;
 		}
 
 		@Override
 		public double[] derivativeAt(double[] x) {
 			double[] gradient = new double[x.length];
 			int weightsBegin;
 			
 			// Compute average activation over all input/output pairs
 			// by each hidden layer, over all nodes in it
 			double[][] averageActivations = new double[layerSizes.length][];
 			for (int i = 1; i < layerSizes.length; ++i)
 				averageActivations[i] = new double[layerSizes[i]];
 			
 			int count = 0;
 			for (Pair<double[], double[]> pair : examples) {
 				double[] input = pair.getFirst();
 				double[][] activations = getActivations(input, layerSizes, x);
 				for (int i = 1; i < activations.length; ++i)
 					for (int j = 0; j < activations[i].length; ++j)
 						averageActivations[i][j] += activations[i][j];
 				++count;
 			}
 			
 			for (int i = 1; i < averageActivations.length; ++i)
 				for (int j = 0; j < averageActivations[i].length; ++j)
 					averageActivations[i][j] /= count;
 			
 			
 			// Compute over all input/output pairs
 			for (Pair<double[], double[]> pair : examples) {
 				double[] input = pair.getFirst();
 				double[] output = pair.getSecond();
 				double[][] activations = getActivations(input, layerSizes, x);
 				double[][] deltas = new double[layerSizes.length][];
 				
 				// Compute delta for final activations
 				double[] predictedOutput = activations[activations.length - 1];
 				double[] outputDelta = new double[layerSizes[layerSizes.length - 1]];
 				for (int i = 0; i < outputDelta.length; ++i) {
 					outputDelta[i] = -(output[i] - predictedOutput[i])
 						* (predictedOutput[i] * (1 - predictedOutput[i])); // derivative of sigmoid
 				}
 				deltas[deltas.length - 1] = outputDelta;
 				
 				if (deltas.length > 2) {
 					weightsBegin = dimension 
 						- (layerSizes[layerSizes.length - 2] + 1) * layerSizes[layerSizes.length - 1];
 					for (int l = deltas.length - 2; l >= 1; --l) {
 						double[] delta = new double[layerSizes[l]];
 						for (int i = 0; i < delta.length; ++i) {
 							for (int j = 0; j < deltas[l + 1].length; ++j)
 								delta[i] += x[weightsBegin + j * delta.length + i] * deltas[l+1][j];
 							// Add sparsity penalty (KL divergence)
 							delta[i] += sparsityPenalty * 
 								(-sparsity/averageActivations[l][i] + (1-sparsity)/(1-averageActivations[l][i]));
							// Multiply by derivative
							delta[i] *= activations[l][i] * (1 - activations[l][i]); // derivative of sigmoid
 						}
 						deltas[l] = delta;
 						weightsBegin -= (layerSizes[l - 1] + 1) * layerSizes[l];
 					}
 					Preconditions.checkState(weightsBegin == 0);
 				}
 				
 				// Finally, compute the gradients
 				weightsBegin = 0;
 				for (int l = 1; l < layerSizes.length; ++l) {
 					// If sizes are [A, B, C], need BxA and CxB matrices, and Bx1 and Cx1 vectors
 					int biasBegin = weightsBegin + layerSizes[l - 1] * layerSizes[l];
 					
 					for (int row = 0; row < layerSizes[l]; ++row) {
 						for (int col = 0; col < layerSizes[l-1]; ++col)
 							gradient[weightsBegin + row * layerSizes[l-1] + col] += 
 								activations[l-1][col] * deltas[l][row];
 						gradient[biasBegin + row] += deltas[l][row];
 					}
 					weightsBegin = biasBegin + layerSizes[l];
 				}
 			}
 			
 			// Scale gradient
 			for (int i = 0; i < gradient.length; ++i)
 				gradient[i] /= count;
 			
 			// Add regularization term
 			weightsBegin = 0;
 			for (int l = 1; l < layerSizes.length; ++l) {
 				// If sizes are [A, B, C], need BxA and CxB matrices, and Bx1 and Cx1 vectors
 				int biasBegin = weightsBegin + layerSizes[l - 1] * layerSizes[l];
 				
 				for (int row = 0; row < layerSizes[l]; ++row)
 					for (int col = 0; col < layerSizes[l-1]; ++col)
 						gradient[weightsBegin + row * layerSizes[l-1] + col] += 
 							weightDecay * x[weightsBegin + row * layerSizes[l-1] + col];
 				
 				weightsBegin = biasBegin + layerSizes[l];
 			}
 			
 			return gradient;
 		}
 	}
 }
