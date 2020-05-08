 public class CategoricalTestValue implements TestValue {
 
	protected Number value;
 	
	public CategoricalTestValue(Number value){
 		this.value = value;
 	}
 	
	public Number getValue() {
 		return value;
 	}
 
	public void setValue(Number value) {
 		this.value = value;
 	}
 
 	@Override
 	public String printTestValue() {
 		if(value==null)
 			return "";
 		return value.toString();
 	}
 }
