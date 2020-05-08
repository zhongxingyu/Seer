 package eu.choreos.vv.increasefunctions;
 
 
 public class LinearIncrease implements ScalabilityFunction {
 
 	@Override
 	public Number increaseParams(Number actualParam, Number fixedParam) {
		return actualParam.intValue() + fixedParam.intValue();
 	}
 
 }
