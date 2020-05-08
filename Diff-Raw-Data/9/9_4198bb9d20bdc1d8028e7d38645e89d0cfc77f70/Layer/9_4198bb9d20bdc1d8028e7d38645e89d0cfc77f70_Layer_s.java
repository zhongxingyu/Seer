 package neuronalnetwork;
 
 import java.util.Arrays;
 
 import neuronalnetwork.function.TransferFunction;
 import util.MoreMath;
 
 public class Layer {
 	
 	private int inputLen;
 	private int neurons;
 
 	private float[][] weights;
 	
 	// output of the layer before applying f() - Used for backpropagation
 	private float[] h;
 	// usado para copiar el array de entrada y agregar un -1 al final
 	private float[] biasedInputCache;
 	private float[] outputCache;
 	
 	public Layer(int inputLen, int neurons) {
 		this.neurons = neurons;
 		this.inputLen = inputLen;
 		h = new float[neurons];
 		weights = new float[neurons][inputLen + 1];
 		biasedInputCache = new float[inputLen + 1];
 		outputCache = new float[neurons];
 		initWeights();
 	}
 
 	private void initWeights() {
 		for (int i = 0; i < weights.length; i++) {
 			for (int j = 0; j < weights[0].length; j++) {
 				weights[i][j] = MoreMath.random(-0.5f, 0.5f);
 			}
 		}
 	}
 
 	public float[] evaluate(float[] in, TransferFunction f) {
 		validateInputDimention(in.length);
 		// copia a biasedInputCache la entrada y agrega un -1 al final
 		System.arraycopy(in, 0, biasedInputCache, 0, in.length);
 		biasedInputCache[in.length] = -1;
 		
 		for (int n = 0; n < neurons; n++) {
 			h[n] = MoreMath.dotProduct(weights[n], biasedInputCache);
 			outputCache[n] = f.valueAt(h[n]);
 		}
 		return outputCache;
 	}
 
 	private void validateInputDimention(int in) {
 		if (in != inputLen) {
 			throw new IllegalArgumentException(
 				"Invalid input dimention given: " + in + " but should be " + inputLen);
 		}
 	}
 
 	public float[] getH() {
 		return h;
 	}
 
 	public float[][] getWeights() {
 		return weights;
 	}
 
 	public int getNeuronsDim() {
 		return neurons;
 	}
 
 	public int getInputLen() {
 		return inputLen;
 	}
 
 	public boolean equals(Layer layer) {
 		if (inputLen != layer.inputLen) {
 			return false;
 		}
 		if (neurons != layer.neurons) {
 			return false;
 		}
 		float diff;
 		for (int i = 0; i < weights.length; i++) {
 			for (int j = 0; j < weights[0].length; j++) {
 				diff = weights[i][j] - layer.weights[i][j];
 				if (Math.abs(diff) > MoreMath.EPS) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		int hashCode = 0;
 		for (int i = 0; i < weights.length; i++) {
 			for (int j = 0; j < weights[0].length; j++) {				
				hashCode += weights[i][j];
 			}
 		}
 		return hashCode;
 	}
 }
