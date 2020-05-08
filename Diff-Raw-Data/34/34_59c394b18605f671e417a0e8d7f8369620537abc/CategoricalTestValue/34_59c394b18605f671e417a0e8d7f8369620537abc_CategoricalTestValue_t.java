 public class CategoricalTestValue implements TestValue {
 
	protected String value;
 	
	public CategoricalTestValue(String value){
 		this.value = value;
 	}
 	
	public String getValue() {
 		return value;
 	}
 
	public void setValue(String value) {
 		this.value = value;
 	}
 
 	@Override
 	public String printTestValue() {
 		if(value==null)
 			return "";
 		return value.toString();
 	}
 }
