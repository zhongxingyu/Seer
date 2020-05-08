 package com.zacharyliu.carsounddetectionlibrary.analyzer.classifiers;
 
 import java.util.Arrays;
 
 import com.zacharyliu.carsounddetectionlibrary.Constants;
 import com.zacharyliu.carsounddetectionlibrary.analyzer.FeatureVector;
 
 public class NeuralNetworkClassifier implements Classifier {
 	
 //	private NeuralNetwork network;
 //	private BasicNetwork network;
 	private NeuralNetwork network;
 	
 	class NeuralNetwork {
 		private final double[][] ALL_BIAS = Constants.ALL_BIAS;
 		private final double[][] ALL_WEIGHTS = Constants.ALL_WEIGHTS;
 		private final int[] NEURON_COUNTS = Constants.NEURON_COUNTS;
 		Layer[] layers;
 		
 		public NeuralNetwork() {
 			layers = new Layer[NEURON_COUNTS.length - 1];
 			boolean linear = false;
 			for (int i=0; i<layers.length; i++) {
 				if (i == layers.length - 1) {
 					linear = true;
 				}
 				layers[i] = new Layer(NEURON_COUNTS[i+1], ALL_BIAS[i], ALL_WEIGHTS[i], linear);
 			}
 		}
 		
 		public double[] run(double[] inputs) {
 			double[] output = Arrays.copyOf(inputs, inputs.length);
 			for (int i=0; i<layers.length; i++) {
 				output = layers[i].run(output);
 			}
 			return output;
 		}
 		
 		
 		class Layer {
 			private int mNum;
 			private Neuron[] mNeurons;
 			private double[] mWeights;
 
 			public Layer(int num, double[] bias, double[] weights, boolean linear) {
 				mNum = num;
 				
 				mNeurons = new Neuron[num];
 				for (int i=0; i<num; i++) {
 					mNeurons[i] = new Neuron(bias[i], linear);
 				}
 				
 				mWeights = weights;
 			}
 			
 			public double[] run(double[] lastLayerValues) {
 				assert(mNum * lastLayerValues.length == mWeights.length);
 				
 				double[] outValues = new double[mNeurons.length];
 				for (int neuronNum=0; neuronNum<mNum; neuronNum++) {
 					double total = 0;
 					for (int prevNum=0; prevNum<lastLayerValues.length; prevNum++) {
 						total += mWeights[lastLayerValues.length*neuronNum + prevNum] * lastLayerValues[prevNum];
 					}
 					outValues[neuronNum] = mNeurons[neuronNum].run(total);
 				}
 				return outValues;
 			}
 			
 			class Neuron {
 				private double mBias;
 				private boolean mLinear;
 
 				public Neuron(double bias, boolean linear) {
 					mBias = bias;
 					mLinear = linear;
 				}
 				
 				public double run(double value) {
 					if (!mLinear) {
 						value = 1.0 / (1.0 + Math.pow(Math.E, -value));
 					}
					return value + mBias;
 				}
 			}
 		}
 	}
 	
 	public NeuralNetworkClassifier(int n_inputs, int n_outputs) {
 		// Neuroph
 //		network = new MultiLayerPerceptron(n_inputs, NUMBER_HIDDEN_NEURONS, NUMBER_HIDDEN_NEURONS, NUMBER_HIDDEN_NEURONS, n_outputs);
 		
 		// Encog
 //		network = new BasicNetwork();
 //		network.addLayer(new BasicLayer(null, false, n_inputs));
 //		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, NUMBER_HIDDEN_NEURONS));
 //		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, NUMBER_HIDDEN_NEURONS));
 //		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, NUMBER_HIDDEN_NEURONS));
 //		network.addLayer(new BasicLayer(null, true, n_outputs));
 //		network.getStructure().finalizeStructure();
 //		network.reset();
 //		Log.d("Network", network.dumpWeights());
 		
 		// custom implementation
 		network = new NeuralNetwork();
 	}
 
 	@Override
 	public double[] run(FeatureVector feature_vector) {
 		// TODO Run neural network classification
 //		Result result = new Result(new double[] {(int) (Math.random() * 10)});
 		
 		// Neuroph
 //		network.setInput(feature_vector.toRawArray());
 //		network.calculate();
 //		double[] output = network.getOutput();
 //		double[] result = output;
 //		return result;
 		
 		// Encog
 //		MLData output = network.compute(new BasicMLData(feature_vector.toRawArray()));
 //		double[] result = output.getData();
 //		return result;
 		
 		// custom implementation
 		return network.run(feature_vector.toRawArray());
 	}
 
 }
