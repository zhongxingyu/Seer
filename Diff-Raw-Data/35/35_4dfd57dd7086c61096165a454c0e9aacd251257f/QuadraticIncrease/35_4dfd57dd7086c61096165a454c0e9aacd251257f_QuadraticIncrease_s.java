 package eu.choreos.vv.increasefunctions;
 
 public class QuadraticIncrease implements ScalabilityFunction {
 	
 
	private int actualIncreasingTime;
 
 	public QuadraticIncrease() {
 		actualIncreasingTime=2;
 	}
 
 	@Override
 	public Number increaseParams(Number actualParam, Number fixedParam) {
		Number value = fixedParam.intValue()*(actualIncreasingTime*actualIncreasingTime);
 		actualIncreasingTime++;
 		return value;
 	}
 }
