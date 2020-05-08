 package org.chai.kevin.value;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Sum;
 import org.chai.kevin.location.CalculationLocation;
 
 public class SumValue extends CalculationValue<SumPartialValue> {
 	
 	public SumValue(Set<SumPartialValue> calculationPartialValues, Sum calculation, Period period, CalculationLocation location) {
 		super(new ArrayList<SumPartialValue>(calculationPartialValues), calculation, period, location);
 	}
 	
 	public SumValue(List<SumPartialValue> calculationPartialValues, Sum calculation, Period period, CalculationLocation location) {
 		super(calculationPartialValues, calculation, period, location);
 	}
 
 	@Override
 	public Value getValue() {
 		//data Location
 		if (getLocation().collectsData()) {
 			return getDataLocationValue();
 		}
 		//location
 		Double sum = 0d;
 		for (SumPartialValue partialValue : getCalculationPartialValues()) {
 			if (!partialValue.getValue().isNull()) sum += partialValue.getValue().getNumberValue().doubleValue();
 		}
 		
 		return getData().getType().getValue(sum);
 	}
 	
 	@Override
 	public Value getPercentage(){		
 		Double percentage = 0d;
 		//data location
 		if (getLocation().collectsData()) {
 			Value dataLocationValue = getDataLocationValue();			
 			if(!dataLocationValue.isNull()){
 				percentage = dataLocationValue.getNumberValue().doubleValue() * 100;
 			}
 			
 		}
 		//location
 		else{			
 			Double sum = 0d;
 			Integer num = 0;
 			for (SumPartialValue sumPartialValue : getCalculationPartialValues()) {
 				if (!sumPartialValue.getValue().isNull()) {
 					sum += sumPartialValue.getValue().getNumberValue().doubleValue();
 					num += sumPartialValue.getNumberOfDataLocations();
 				}
 			}
 			
 			percentage = (sum / num) * 100;	
 		}
 		
 		if (percentage.isNaN() || percentage.isInfinite()) 
 			percentage = null;
 		return getData().getType().getValue(percentage);
 	}
 	
 	//@Override TODO move to CalculationValue for use with AggregationValue
 	public Integer getNumberOfDataLocations(){
		//data Location
		if (getLocation().collectsData()) {
			return 1;
		}
		//location
 		Integer numberOfDataLocations = 0;
 		for (SumPartialValue sumPartialValue : getCalculationPartialValues()) {
 			if (!sumPartialValue.getValue().isNull()) {
 				numberOfDataLocations += sumPartialValue.getNumberOfDataLocations();
 			}
 		}
 		return numberOfDataLocations;
 	}
 	
 	private Value getDataLocationValue(){
 		if (getCalculationPartialValues().size() > 1) throw new IllegalStateException("Calculation for DataLocation does not contain only 1 partial value");
 		if (getCalculationPartialValues().size() == 0) return Value.NULL_INSTANCE();
 		return getCalculationPartialValues().get(0).getValue();
 	}
 	
 	@Override
 	public String toString() {
 		return "SumValue [getValue()=" + getValue() + "]";
 	}
 
 	@Override
 	public Date getTimestamp() {
 		return null;
 	}
 	
 }
