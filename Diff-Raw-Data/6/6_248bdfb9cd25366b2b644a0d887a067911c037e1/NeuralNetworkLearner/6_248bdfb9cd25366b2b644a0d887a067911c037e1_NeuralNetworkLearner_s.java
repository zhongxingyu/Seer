 package br.eb.ime.comp.nnbot.learning;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.lang.Math;
 
 public class NeuralNetworkLearner<InputClass extends Arrayable, OutputClass extends Arrayable>
 		implements ILearner<InputClass, OutputClass> {
 	protected double learningRate;
 	protected double momentum;
 
 	protected int sizeInput;
 	protected int sizeHidden;
 	protected int sizeOutput;
 
 	protected double[] inputNeurons;
 	protected double[] hiddenNeurons;
 	protected double[] outputNeurons;
 	protected double[] expectedOutput;
 
 	protected double[][] weightsHiddenFromInputLayer;
 	protected double[][] weightsOutputFromHiddenLayer;
 
 	protected double[][] deltaHiddenFromInput;
 	protected double[][] deltaOutputFromHidden;
 
 	protected double[] hiddenErrorGradients;
 	protected double[] outputErrorGradients;
 	
 	public boolean verbose;
 
 	public NeuralNetworkLearner(double learningRate, double momentum,
 			int _sizeInput, int _sizeHidden, int _sizeOutput) {
 		this.sizeHidden = _sizeHidden;
 		this.learningRate = learningRate;
 		this.momentum = momentum;
 		
 		this.sizeInput = _sizeInput;
 		this.sizeOutput = _sizeOutput;
 		
 		hiddenNeurons = new double[sizeHidden + 1];
 		inputNeurons = new double[sizeInput + 1];
 		outputNeurons = new double[sizeOutput];
 		expectedOutput = new double[sizeOutput];
 		// Bias neuron
 		inputNeurons[sizeInput] = -1;
 		hiddenNeurons[sizeHidden] = -1;
 
 		weightsHiddenFromInputLayer = new double[sizeHidden + 1][sizeInput + 1];
 		weightsOutputFromHiddenLayer = new double[sizeOutput][sizeHidden + 1];
 
 		hiddenErrorGradients = new double[sizeHidden + 1];
 		outputErrorGradients = new double[sizeOutput];
 
 		deltaHiddenFromInput = new double[sizeHidden + 1][sizeInput + 1];
 		deltaOutputFromHidden = new double[sizeOutput][sizeHidden + 1];
 		
 		verbose = true;
 		
 		resetWeights();
 	}
 
 	/*
 	 * Initialize weight matrices with random values.
 	 * */
 	public void resetWeights() {
 		//Random r = new Random();
 		for (int i = 0; i < weightsHiddenFromInputLayer.length; i++) {
 			for (int j = 0; j < weightsHiddenFromInputLayer[i].length; j++) {
 				weightsHiddenFromInputLayer[i][j] = (((i + j) % 2 )* 2 - 1);//r.nextFloat() * 2 - 1;
 				deltaHiddenFromInput[i][j] = 0;
 			}
 		}
 		for (int i = 0; i < weightsOutputFromHiddenLayer.length; i++) {
 			for (int j = 0; j < weightsOutputFromHiddenLayer[i].length; j++) {
 				weightsOutputFromHiddenLayer[i][j] = (((i + j) % 2 )* 2 - 1);//r.nextFloat() * 2 - 1;
 				deltaOutputFromHidden[i][j] = 0;
 			}
 		}
 	}
 
 	/*
 	 * Sigmoid activating function.
 	 * */
 	public double activation(double x) {
 		return 1 / (1 + Math.exp(-x));
 	}
 
 	/*
 	 * Applies feed-forward and back-propagation to each trainingset in the list, updating weights in each one.
 	 */
 	public void train(ArrayList<TrainingSet<InputClass, OutputClass>> inputs) {
 		for (TrainingSet<InputClass, OutputClass> ioSet : inputs) {
 			InputClass input = ioSet.getInput();
 			double[] inputArray = input.toArray();
 			for (int i = 0; i < inputArray.length; i++)
 			{
 				inputNeurons[i] = inputArray[i];
 			};
 
 			OutputClass output = ioSet.getOutput();
 			expectedOutput = output.toArray();
 			
 			if (verbose) {
 				System.out.println("Input: ");
 				for (double d : inputNeurons)
 				{
 					System.out.printf("%f ", d);
 				}
 				System.out.println("");
 				System.out.println("Expected: ");
 				for (double d : expectedOutput)
 				{
 					System.out.printf("%f ", d);
 				}
 				System.out.println("");
 			}
 
 			feedForward();
 			if (verbose) {
 				System.out.println("Got: ");
 				for (double d : outputNeurons)
 				{
 					System.out.printf("%f ", d);
 				}
 				System.out.println("");
 			}
 			backPropagate();
 		}
 	}
 
 	/* 
 	 * Sets outputNeurons according to the current weights.
 	 */
 	public double[] feedForward() {
 		// h = activation(W{h from i} * i)
 		for (int i = 0; i < hiddenNeurons.length; i++) {
 			hiddenNeurons[i] = 0;
 			for (int j = 0; j < inputNeurons.length; j++) {
 				hiddenNeurons[i] += weightsHiddenFromInputLayer[i][j]
 						* inputNeurons[j];
 			}
 			hiddenNeurons[i] = activation(hiddenNeurons[i]);
 		}
 		// o = activation(W{o from h} * h)
 		for (int i = 0; i < outputNeurons.length; i++) {
 			outputNeurons[i] = 0;
 			for (int j = 0; j < hiddenNeurons.length; j++) {
 				outputNeurons[i] += weightsOutputFromHiddenLayer[i][j]
 						* hiddenNeurons[j];
 			}
 			outputNeurons[i] = activation(outputNeurons[i]);
 		}
 		return outputNeurons;
 	}
 
 	public void backPropagate() {
 		for (int i = 0; i < outputNeurons.length; i++) {
 			// Calculate error
 			outputErrorGradients[i] = outputErrorGradient(expectedOutput[i],
 					outputNeurons[i]);
 			// Propagate error to delta matrix
 		}
 		for (int i = 0; i < outputNeurons.length; i++) {
 			for (int j = 0; j < hiddenNeurons.length; j++) {
 				// The higher the momentum, the bigger the permanence of the latest measure.
 				deltaOutputFromHidden[i][j] = learningRate * hiddenNeurons[j]
 						* outputErrorGradients[i] + momentum
 						* deltaOutputFromHidden[i][j];
 			}
 		}
 
 		for (int i = 0; i < hiddenNeurons.length - 1; i++) {
 			hiddenErrorGradients[i] = hiddenErrorGradient(i);
 			for (int j = 0; j < inputNeurons.length; j++) {
 				deltaHiddenFromInput[i][j] = learningRate * inputNeurons[j]
 						* hiddenErrorGradients[i];
 			}
 		}
 		updateWeightsFromDeltas();
 	}
 
 	public void updateWeightsFromDeltas() {
 		for (int i = 0; i < hiddenNeurons.length; i++) {
 			for (int j = 0; j < inputNeurons.length; j++) {
 				weightsHiddenFromInputLayer[i][j] += deltaHiddenFromInput[i][j];
 				deltaHiddenFromInput[i][j] = 0;
 			}
 		}
 	}
 
 	public double hiddenErrorGradient(int j) {
 		double sum = 0;
 		for (int i = 0; i < outputNeurons.length; i++) {
 			sum += weightsOutputFromHiddenLayer[i][j] * outputErrorGradients[i];
 		}
 		return hiddenNeurons[j] * (1 - hiddenNeurons[j]) * sum;
 	}
 
 	public double outputErrorGradient(double expected, double actual) {
 		return actual * (1 - actual) * (expected - actual);
 	}
 
 	public ArrayList<OutputClass> validate(ArrayList<InputClass> inputs) {
 		return new ArrayList<OutputClass>();
 	}
 
 }
