 package neuronalnetwork;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import neuronalnetwork.function.TransferFunction;
 import util.MoreMath;
 
 public class BackPropagation {
 
 	private NeuralNetwork net;
 	private TransferFunction f;
 	private float eta;
 	
 	public BackPropagation(TransferFunction f, float eta) {
 		this.f = f;
 		this.eta = eta;
 	}
 	
 	public void train(NeuralNetwork net, Map<float[], float[]> train, int epochs) {
 		this.net = net;
 		while (epochs-- > 0) { 
 			for (Entry<float[], float[]> entryTrain: train.entrySet()) {
 				float[] input = entryTrain.getKey();
 				float[] expectedOutput = entryTrain.getValue();
 				train(input, expectedOutput);
 			}
 		}
 	}
 	
 	private void train(float[] input, float[] expectedOutput) {
 		float[] output = net.evaluate(input, f);
 		int k = net.getTotalLayers();
 		float[][] deltas = new float[k][];
 		// Eval delta for last layer (k)
 		k--;								// -1 to make k an index
 		Layer l = net.getLayer(k);
 		deltas[k] = getLastLayerDelta(l, output, expectedOutput);
 		// Eval all deltas for all layers from 0 to k - 1;
		for (int m = k - 1; m > 0; m--) {
 			deltas[m] = getLayerDelta(m, deltas[m + 1]);
 		}
 		updateUnits(deltas, input);
 	}
 	
 	private float[] getLastLayerDelta(Layer last, float[] output, float[] expectedOutput) {
 		float[] delta = new float[last.getNeuronsDim()];
 		float[] h = last.getH();
 		for (int i = 0; i < delta.length; i++) {
 			delta[i] = (expectedOutput[i] - output[i]) * f.valueAtDerivated(h[i]);
 		}
 		return delta;
 	}
 	
 	private float[] getLayerDelta(int m, float[] nextLayerDelta) {
 		Layer curr = net.getLayer(m);
 		float[] h = curr.getH();
 		float[] delta = new float[curr.getNeuronsDim()];
		float[][] nextLayerWeights = curr.getWeights();
 
 		for (int i = 0; i < delta.length; i++) {
			System.out.println("");
 			float sum = MoreMath.dotProduct(nextLayerWeights[i], nextLayerDelta);
 			delta[i] = f.valueAtDerivated(h[i]) * sum;
 		}
 		return delta;
 	}
 
 	private void updateUnits(float[][] deltas, float[] input) {
 		for (int m = 0; m < deltas.length; m++) {
 			Layer curr = net.getLayer(m);
 			float[][] weights = curr.getWeights();
 			float[] bias = curr.getBias();
 			// update connections
 			for (int i = 0; i < curr.getNeuronsDim(); i++) {
 				for (int j = 0; j < curr.getInputLen(); j++) {
 					float delta = getDelta(m, deltas, i, input);
 					weights[j][i] += delta;
 				}
 				// update bias
 				float biasDelta = eta * deltas[m][i] * (-1);
 				bias[i] += biasDelta;
 			}
 		}
 	}
 	
 	private float getDelta(int m, float[][] deltas, int i, float[] input) {
 		float[] h = net.getLayer(m).getH();
 		float v;
 		if (m == 0) {
 			v = input[i];
 		} else {
 			v = f.valueAt(h[i]);
 		}
 		return eta * deltas[m][i] * v;
 	}
 }
