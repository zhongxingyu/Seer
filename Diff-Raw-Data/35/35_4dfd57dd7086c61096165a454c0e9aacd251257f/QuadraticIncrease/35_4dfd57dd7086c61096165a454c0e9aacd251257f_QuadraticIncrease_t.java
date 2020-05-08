 package eu.choreos.vv.increasefunctions;
 
 public class QuadraticIncrease implements ScalabilityFunction {
 	
 
	private double actualIncreasingTime;
 
 	public QuadraticIncrease() {
 		actualIncreasingTime=2;
 	}
 
 	@Override
 	public Number increaseParams(Number actualParam, Number fixedParam) {
		Number value = fixedParam.doubleValue()*(actualIncreasingTime*actualIncreasingTime);
 		actualIncreasingTime++;
 		return value;
 	}
 }
