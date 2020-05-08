 package eu.choreos.vv;
 
 import java.util.Arrays;
 
 import eu.choreos.vv.increasefunctions.LinearIncrease;
 import eu.choreos.vv.increasefunctions.ScalabilityFunction;
 
 public class ScalabilityTest {
 	
 	ScalabilityTestItem item;
 	String name;
 	Integer timesToExecute;
 	Double measurementLimit;
 	ScalabilityFunction function;
 	Number[] initialParameterValues;
 	Number[] currentParameterValues;
 	
 	public ScalabilityTest(ScalabilityTestItem item, String name) {
 		this(item, name, Integer.MAX_VALUE, Double.MAX_VALUE, new LinearIncrease());
 	}
 
 	public ScalabilityTest(ScalabilityTestItem item, String name,
 			Integer timesToExecute, Double measurementLimit,
 			ScalabilityFunction function) {
 		super();
 		this.item = item;
 		this.name = name;
 		this.timesToExecute = timesToExecute;
 		this.measurementLimit = measurementLimit;
 		this.function = function;
 	}
 
 	public ScalabilityTestItem getItem() {
 		return item;
 	}
 
 	public void setItem(ScalabilityTestItem item) {
 		this.item = item;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Integer getTimesToExecute() {
 		return timesToExecute;
 	}
 
 	public void setTimesToExecute(Integer timesToExecute) {
 		this.timesToExecute = timesToExecute;
 	}
 
 	public Double getMeasurementLimit() {
 		return measurementLimit;
 	}
 
 	public void setMeasurementLimit(double measurementLimit) {
 		this.measurementLimit = measurementLimit;
 	}
 
 	public ScalabilityFunction getFunction() {
 		return function;
 	}
 
 	public void setFunction(ScalabilityFunction function) {
 		this.function = function;
 	}
 	
 	public void setInitialParametersValues(Number... values) {
 		initialParameterValues = values;
 		currentParameterValues = Arrays.copyOf(values, values.length);
 	}
 	
 	public Number[] getInitialParametersValues() {
 		return initialParameterValues;
 	}
 	
 	public Number[] getCurrentParametersValues() {
 		return currentParameterValues;
 	}
 	
 	public void increaseParamentersValues() {
 		for (int i = 0; i < initialParameterValues.length; i++)
			currentParameterValues[i] = function.increaseParams((Integer)currentParameterValues[i], (Integer)initialParameterValues[i]);
 	}
 	
 	public Double execute() throws Exception {
 		return item.test(currentParameterValues);
 	}
 	
 	public ScalabilityReport executeIncreasingParams()
 			throws Exception {
 		double value = 0.0;
 		ScalabilityReport report = new ScalabilityReport(this.getName());
 		for (int i = 0; i < this.getTimesToExecute() && value <= this.getMeasurementLimit(); i++) {
 			value = this.execute();
 			report.put((double)i, value);
 			this.increaseParamentersValues();
 		}
 		return report;
 	}
 	
 }
