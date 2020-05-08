 package hunternif.nn;
 
 public class HyperbolicTangent implements IActivationFunction {
 
 	@Override
 	public double produce(double input) {
		return Math.tanh(input);
 	}
 
 }
