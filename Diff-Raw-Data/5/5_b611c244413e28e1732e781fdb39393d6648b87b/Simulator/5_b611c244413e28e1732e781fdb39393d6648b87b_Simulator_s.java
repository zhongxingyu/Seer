 package driver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import neural_net.NeuralNetworkModel;
 import neural_net.NonRecurrentNeuralNetwork;
 import neural_net.NonRecurrentRBFNeuralNetwork;
 import solver.ActivationFunction;
 import solver.ConvergenceStoppingCondition;
 import solver.GaussianBasis;
 import solver.LinearActivationFunction;
 import solver.SigmoidActivationFunction;
 import solver.Solver;
 import solver.StoppingCondition;
 import validation.DataPointGenerator;
 import validation.KFoldCrossValidation;
 import validation.RosenbrockDataPointGenerator;
 import validation.Validation;
 
 public class Simulator {
 
 	private ActivationFunction activationFunction;
 	private ActivationFunction rbfHiddenActivationFunction;
 	private NeuralNetworkModel neuralNet;
 	private Validation validation;
 	private Solver solver;
 	private StoppingCondition stoppingCondition;
 	private List<Double> inputVector;
 	private int numInputNeurons = 2;
 	private int numOutputNeurons = 1;
 	private int numHiddenLayers = 2;
 	private int numNeuronsPerHiddenLayer = 100;
	private double eta = .2;
 	private double alpha = 0;
 
 	private int rosenbrockVectorSize = numInputNeurons;
 	private int randDataPointRange = 1;
	private int numDataPoints = 100;
 	private int k = 10;
 	private double stoppingEpsilon = 0.0001;
 
 	private double gaussianInitialPeak = 1.0;
 	private double gaussianInitialCenter = 1.0;
 	private double gaussianInitialWidth = 1.0;
 
 	public Simulator() {
 		getInitialInputVector();
 		buildKFoldCrossValidation();
 		buildBackPropModel();
 
 		// buildRBFModel();
 
 	}
 
 	public void buildKFoldCrossValidation() {
 		DataPointGenerator dataPointGenerator = getRosenbrockDataPointGenerator();
 		validation = new KFoldCrossValidation(numDataPoints, dataPointGenerator, k);
 		stoppingCondition = new ConvergenceStoppingCondition(stoppingEpsilon);
 	}
 
 	public DataPointGenerator getRosenbrockDataPointGenerator() {
 		DataPointGenerator dpg = new RosenbrockDataPointGenerator(rosenbrockVectorSize, randDataPointRange);
 		return dpg;
 	}
 
 	public void buildRBFModel() {
 		neuralNet = new NonRecurrentRBFNeuralNetwork(activationFunction, numInputNeurons, inputVector, numOutputNeurons, numNeuronsPerHiddenLayer, getNewGaussianFunction());
 		neuralNet.buildModelStructure();
 
 		solver = new Solver(neuralNet, validation, alpha, eta, stoppingCondition);
 		solver.useRadialBaseStrategy();
 	}
 
 	public GaussianBasis getNewGaussianFunction() {
 		return new GaussianBasis(gaussianInitialPeak, gaussianInitialCenter, gaussianInitialWidth);
 	}
 
 	public void buildBackPropModel() {
 		sigmoidalActivation();
 		neuralNet = new NonRecurrentNeuralNetwork(activationFunction, numInputNeurons, inputVector, numOutputNeurons, numHiddenLayers, numNeuronsPerHiddenLayer);
 		neuralNet.buildModelStructure();
 		solver = new Solver(neuralNet, validation, alpha, eta, stoppingCondition);
 		solver.useBackPropStrategy();
 	}
 
 	public void linearActivation() {
 		activationFunction = new LinearActivationFunction();
 	}
 
 	public void sigmoidalActivation() {
 		activationFunction = new SigmoidActivationFunction();
 	}
 
 	public void getInitialInputVector() {
 		inputVector = new ArrayList<>();
 		for (int i = 1; i <= numInputNeurons; i++) {
 			inputVector.add(new Double(0));
 		}
 	}
 }
