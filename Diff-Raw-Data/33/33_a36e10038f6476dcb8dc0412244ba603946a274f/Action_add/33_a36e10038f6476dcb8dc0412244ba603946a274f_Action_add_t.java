 package de.thm.mni.nn.ui.actions;
 
 import de.thm.mni.nn.model.DataStore;
 import de.thm.mni.nn.perceptron.impl.EActivationFunction;
 import de.thm.mni.nn.perceptron.impl.ENeuronType;
 import de.thm.mni.nn.perceptron.impl.Pattern;
 import de.thm.mni.nn.perceptron.impl.Perceptron;
 import de.thm.mni.nn.ui.Action;
 import de.thm.mni.nn.ui.UserInterface;
 
 /**
  * The Add Action is used to add Perceptrons, Neurons and Axons
  * 
  */
 public class Action_add extends Action {
 
 	
 
 	public Action_add(DataStore ds, UserInterface ui) {
 		super(ds, ui);
 	}
 
 	// TODO: LearningRate
 	
 	/**
 	 * This function chooses between adding and perceptron,
 	 * neuron, pattern or axon
 	 * @param args The given string from the command window
 	 */
 	public void callAction(String args) {
 		if (args.equalsIgnoreCase("perceptron")) {
 			String perceptronName;
 			int perceptronLayer;
 
 			System.out.print("Name of perceptron: ");
 			perceptronName = ui.inputToString();
 
 			System.out.print("Number of layers: ");
 			perceptronLayer = ui.inputToInt();
 			
 			if (perceptronLayer < 2) {
 				System.out.println("Aborting...\nThe Perceptron need at least 2 layers");
 				return;
 			}
 			System.out.println("Choose an initial axon weight: [1/2]");
 			System.out.println("\t 1. Random axon weights between 0 and 1");
 			System.out.println("\t 2. Customized upper and lower boundary");
 			//Choose between initial random weights and custom random weights
 			switch (ui.inputToInt()) {
 			case 1:
 				//Proof if the given perceptron name is the same as in the datastore
 				if (ds.addPerceptron(perceptronName, new Perceptron(perceptronLayer))) {
 					System.out.println("Added the given perceptron");
 				} else {
 					System.out.println("Aborting... There is already an perceptron with this name");
 				}
 				break;
 			case 2:
 				double minSeed, maxSeed;
 
 				System.out.print("The lower boundary of the axon weight: ");
 				minSeed = ui.inputToDouble();
 
 				System.out.print("The upper boundary of the axon weight: ");
 				maxSeed = ui.inputToDouble();
 				
 				//Proof if the given perceptron name is the same as in the datastore
 				if (ds.addPerceptron(perceptronName, new Perceptron(perceptronLayer, minSeed, maxSeed))) {
 					System.out.println("Added the given perceptron");
 				} else {
 					System.out.println("Aborting... There is already an perceptron with the given name");
 				}
 				break;
 			default:
 				System.out.println("Aborting... You havn't choosed 1 or 2");
 			}
 		} else if (args.equalsIgnoreCase("neuron")) {
 			String perceptronName;
 			int layerRow, numberOfNeurons, activationInput;
 			ENeuronType neuronType = null;
 			EActivationFunction actFunc = null;
 
 			System.out.print("Adding the neurons to this perceptron: ");
 			perceptronName = ui.inputToString();
 			
 			Perceptron perceptron = ds.getPerceptron(perceptronName);
 			if(perceptron==null)
 			{
 				System.out.println("Aborting... There isn't any perceptron with the given name");
 				return;
 			}
 			
 			System.out.print("Adding neuron to the following layer: ");
 			layerRow = ui.inputToInt();
 
 			//Proof if the user adds an neuron to an existing layer
 			int nrOfLayer = perceptron.getLayerCount();
 			if(layerRow >= nrOfLayer)
 			{
 				System.out.println("Aborting...\nYou can't add neurons to a non-existent layer");
 				return;
 			}
 
 			System.out.print("Number of neurons to add: ");
 			numberOfNeurons = ui.inputToInt();
 
 			//Assign the type of the neuron (input, hidden, output) to the given neurons 
 			
 			if (layerRow == 0)
 			{
 				neuronType = ENeuronType.values()[0];
 			}
 			else if(layerRow == nrOfLayer-1)
 			{
 				neuronType = ENeuronType.values()[2];
 			}
 			else
 			{
 				neuronType = ENeuronType.values()[1];
 			}
 			
 			//Iterate the activation function and print them to the screen
 			System.out.println("Choose an activation function: ");
 			for(int i=0; i < EActivationFunction.values().length; i++) {
 				System.out.println("\t " + (i+1) + ". " + EActivationFunction.values()[i].toString());
 			}
 			
 			activationInput = ui.inputToInt();
 			//Proofs if the given activation function is a correct one between 1 and 
 			//the number of values in EActivationFunction
 			if (activationInput <= EActivationFunction.values().length	&& activationInput > 0) {
 				actFunc = EActivationFunction.values()[activationInput - 1];
 			} else {
 				System.out.println("No possible value! Aborting...");
 				return;
 			}
 			//Proof if the given perceptron name is the same as in the datastore
 			perceptron.addNeuron(layerRow, numberOfNeurons, neuronType, actFunc);
 			System.out.println("Adding neurons to Perceptron");
 		} else if (args.equalsIgnoreCase("axon")) {
 			String perceptronName;
 			int inputLayer, inputNeuron, outputLayer, outputNeuron;
 
 			System.out.print("Adding the axon to this perceptron: ");
 			perceptronName = ui.inputToString();
 			
 			Perceptron perceptron = ds.getPerceptron(perceptronName);
 			if(perceptron==null)
 			{
 				System.out.println("Aborting... There isn't any perceptron with the given name");
 				return;
 			}
 			System.out.print("Source row of the perceptron layer: ");
 			inputLayer = ui.inputToInt();
 			
 			int nrOfLayer = perceptron.getLayerCount();
 			if(inputLayer >= nrOfLayer)
 			{
 				System.out.println("Aborting...\nYou can't add neurons to a non-existent layer");
 				return;
 			}
 
 			System.out.print("Source neuron of the axon: ");
 			inputNeuron = ui.inputToInt();
 			int nrOfInputNeurons = perceptron.getNeuronsPerLayerCount(inputLayer);
 			if(inputNeuron >= nrOfInputNeurons)
 			{
 				System.out.println("Aborting...\nYou can't set an axon source to a non-existent layer");
 				return;
 			}	
 				
 			System.out.print("Target row of the perceptron layer: ");
 			outputLayer = ui.inputToInt();
 			if(outputLayer >= nrOfLayer)
 			{
 				System.out.println("Aborting...\nYou can't add neurons to a non-existent layer");
 				return;
 			}
 			if(inputLayer > outputLayer)
 			{
 				System.out.println("Aborting...\nYou can't add axons from target to source");
 				return;
 			}
 			
 			System.out.print("Target neuron of the axon: ");
 			outputNeuron = ui.inputToInt();
 			if(outputNeuron > nrOfInputNeurons)
 			{
 				System.out.println("Aborting...\nYou can't set an axon target to a non-existent layer");
 				return;
 			}
 			//Proof if the given perceptron name is the same as in the datastore
 			perceptron.addAxon(inputLayer, inputNeuron, outputLayer, outputNeuron);
 		} else if (args.equalsIgnoreCase("pattern")) {
 			String patternName;
 			int numberOfInputNeurons;
 			
 			System.out.print("Choose a pattern name: ");
 			patternName = ui.inputToString();
 			
 			System.out.print("No. of inputNeurons for the pattern: ");
 			numberOfInputNeurons = ui.inputToInt();
 			Double inputpattern[] = new Double[numberOfInputNeurons];
 			
 			for (int i=0; i<numberOfInputNeurons; i++)
 			{
 				System.out.print("pattern for " + (i+1) + ". Input-Neuron: ");
 				inputpattern[i] = ui.inputToDouble();
 			}
 			//Proof if the given pattern name is the same as in the datastore
 			if (ds.addPattern(patternName, new Pattern(inputpattern))) {
 				System.out.print("No. of output neurons for the pattern: ");
 				int numberOfOutputNeurons = ui.inputToInt();
 				Double outputpattern[] = new Double[numberOfOutputNeurons];
 				
 				for (int i=0; i<numberOfOutputNeurons; i++)
 				{
 					System.out.print("pattern for " + (i+1) + ". Output-Neuron: ");
 					outputpattern[i] = ui.inputToDouble();
 				}
 				ds.getPattern(patternName).addOutputPattern(outputpattern);
 				
 				System.out.println("Added the given pattern");
 			} else {
 				System.out.println("Aborting... There is already an pattern with this name");
 				return;
 			}
 						
 		}
 	}
 	
 	@Override
 	public String getDescription() {
 		return "Adds an 'perceptron', 'neuron' or 'axon' 'pattern' to the Neuronal Net";
 	}
 
 }
