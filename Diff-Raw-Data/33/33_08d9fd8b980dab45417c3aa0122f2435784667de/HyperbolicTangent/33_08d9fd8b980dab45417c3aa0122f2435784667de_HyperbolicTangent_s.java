 package hunternif.nn;
 
 public class HyperbolicTangent implements IActivationFunction {
 
 	@Override
 	public double produce(double input) {
		return 1 / (1 + Math.exp(-input));
 	}
 
 }
