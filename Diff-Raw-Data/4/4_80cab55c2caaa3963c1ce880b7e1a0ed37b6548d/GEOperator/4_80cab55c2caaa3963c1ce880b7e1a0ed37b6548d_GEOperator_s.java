 package edu.tum.lua.operator.relational;
 
 import edu.tum.lua.operator.Operator;
 
 public class GEOperator extends Operator {
 
 	private LEOperator leoperator;
 	
 	public boolean apply(Object o1, Object o2) throws NoSuchMethodException {
 		
		return !leoperator.apply(o2, o1);
 		
 	}
 	
 }
