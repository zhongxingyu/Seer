 package hunternif.nn;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /** This class is not thread-safe! */
 public class Neuron {
 	private IActivationFunction function;
 	
 	private double accumulatedInput = 0;
 	private int inputsProcessed = 0;
 	
 	private Map<Neuron, Double> inputWeights = new HashMap<>();
 	protected Set<Neuron> outputs = new HashSet<>();
 	
 	public Neuron(IActivationFunction function) {
 		this.function = function;
 	}
 	
 	public void connectInput(Neuron input) {
 		setInputWeight(input, 1);
 	}
 	public void setInputWeight(Neuron input, double weight) {
		weight = Math.max(0, Math.min(weight, 1));
 		inputWeights.put(input, Double.valueOf(weight));
 		input.connectOutput(this);
 	}
 	public double getInputWeight(Neuron input) {
 		Double weight = inputWeights.get(input);
 		if (weight == null) weight = Double.valueOf(0);
 		return weight;
 	}
 	private void connectOutput(Neuron output) {
 		outputs.add(output);
 	}
 	
 	public void input(Neuron source, double signal) {
 		double weight = getInputWeight(source);
 		accumulatedInput += signal * weight;
 		inputsProcessed++;
 		if (inputsProcessed >= inputWeights.size()) {
 			double result = function.produce(accumulatedInput);
 			propagate(result);
 		}
 	}
 	
 	protected void reset() {
 		accumulatedInput = 0;
 		inputsProcessed = 0;
 	}
 	
 	protected void propagate(double signal) {
 		for (Neuron output : outputs) {
 			output.input(this, signal);
 		}
 		reset();
 	}
 }
