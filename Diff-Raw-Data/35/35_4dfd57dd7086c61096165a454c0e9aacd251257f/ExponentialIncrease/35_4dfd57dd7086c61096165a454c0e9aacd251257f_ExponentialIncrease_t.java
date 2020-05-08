 package eu.choreos.vv.increasefunctions;
 
 public class ExponentialIncrease implements ScalabilityFunction {
 
 	@Override
 	public Number increaseParams(Number actualParam, Number fixedParam) {
		return actualParam.doubleValue()*fixedParam.doubleValue();
 	}
 
 }
