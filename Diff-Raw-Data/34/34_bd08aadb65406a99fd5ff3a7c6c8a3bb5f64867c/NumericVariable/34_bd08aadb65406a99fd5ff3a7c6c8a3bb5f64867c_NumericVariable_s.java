 package startrekj.hpbasic;
 
 import static startrekj.hpbasic.Arithmetic.*;
 
 public class NumericVariable implements NumericExpression {
 
 	private String name;
 	private Number value;
 	
 	public NumericVariable(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void incrementByOne() {
 		value = add(value, 1);
 	}
 
 	public void setValue(Number value) {
 		this.value = value;
		if(name.equals("Q1")||name.equals("Q2")||name.equals("S1")||name.equals("S2"))
			System.out.println("value changed: " + name + "=" + value);
 	}
 
 	public Number getValue() {
 		return value;
 	}
 
 	public Number evaluate() {
 		return value;
 	}
 	@Override
 	public String toString() {
 		return name;
 	}
 }
