 package NeuralNetwork;
 
 
 import Data.ImageData;
 
 public class Network {
 	//test tes
 	private Layer[] layers = new Layer[3];
 	private float learningRate;
 	private int numberOutputNodes = 10;
 	private int numberInputNodes = 784;
	private int numberHiddenNodes = numberOutputNodes*4;
 	private float[] desiredOutput= new float[numberOutputNodes];
 	
 	// getter and setter section
 	
 	public Layer getHiddenLayer() 
 	{
 		return this.layers[1];
 	}
 	
 	public void setHiddenLayer(Layer[] hiddenLayer) 
 	{
 		this.layers = hiddenLayer;
 	}
 	
 	public Layer getOutputLayer()
 	{
 		return this.layers[layers.length -1];
 		
 	}
 	
 	public Layer getInputLayer() 
 	{
 		return this.layers[0];
 	}
 	
 	public void setInputLayer(Layer inputLayer)
 	{
 		this.layers[0] = inputLayer;
 	}
 	
 	public int getOutput()
 	{
 		int max = 0;
 		Node[] outputNodes = getOutputLayer().getNodes();
 		for(int i = 1; i < outputNodes.length;i++ )
 		{
 			if (outputNodes[i].getValue() > outputNodes[max].getValue())
 				max = i;
 		}
 		return max;
 	}
 	
 
 	public Network(float learningRate)
 	{
 		this.learningRate = learningRate;
 		layers[2] = new Layer(numberOutputNodes, numberHiddenNodes, true);
		layers[1] = new Layer(numberHiddenNodes, numberInputNodes, true);
 		layers[0] = new Layer(numberInputNodes, 1, false);
 	}
 	
 	public void learn(float[] input, int expectedValue){
 		desiredOutput[expectedValue]=1.0f;
 		for (int i=0; i< numberInputNodes; i++)
 			getInputLayer().getNodes()[i].setValue(input[i]); 
 		passforward();
 		
 		//Compute Deltas (OutputLayer)
 		float temp = 0.0f;
 		float[] deltaK = new float[numberOutputNodes];
		float[] deltaJ = new float[numberHiddenNodes];
 		for (int k=0; k< numberOutputNodes; k++){
 			temp = getOutputLayer().getNodes()[k].getValue();
 			deltaK[k] = (temp - desiredOutput[k]) * temp * (1- temp);
 		}
 
 		Node tempNode = null;
 		//Update Weights connected to OutputLayer
 		for (int k=0; k< numberOutputNodes; k++){
 			tempNode = getOutputLayer().getNodes()[k];
 				for (int j=0; j< numberHiddenNodes; j++){
 				tempNode.setWeight(k, tempNode.getWeight(k) - learningRate * deltaK[k] * getHiddenLayer().getNodes()[j].getValue());
 				deltaJ[k] += tempNode.getWeight(j)* deltaK[k]; //Compute DeltaJ (HiddenLayer) Part1 (Efficiency) 
 			}
 		}
 		
 		//Compute DeltaJ (HiddenLayer) Part 2
 		for (int j=0; j< numberHiddenNodes; j++){
 			deltaJ[j] *= getHiddenLayer().getNodes()[j].getValue() * (1 - getHiddenLayer().getNodes()[j].getValue());
 		}
 		//Update Weights connected to HiddenLayer
 		for (int j=0; j< numberHiddenNodes; j++){
 				for (int i=0; i< numberInputNodes; i++){
 				tempNode = getHiddenLayer().getNodes()[j];
 				tempNode.setWeight(i, tempNode.getWeight(i) - learningRate * deltaJ[j] * getInputLayer().getNodes()[i].getValue());
 				}
 			}
 		desiredOutput[expectedValue]=0.0f;
 	}
 	
 	public void setInput(ImageData inputData)
 	{
 		float[] greyValues = inputData.getGrayValues();
 		Node[] inputNodes = this.layers[0].getNodes();
 		for(int i = 0; i < greyValues.length; i++ )
 			inputNodes[i].setValue(greyValues[i]);
 	}
 	
 	public void passforward()
 	{
 		for(int i = 1; i < layers.length; i++)
 			layers[i].calcNodeValues(layers[i-1]);
 		
 	}	
 	
 	
 	
 }
