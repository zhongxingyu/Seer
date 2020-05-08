 package com.spykertech.BUnit;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 import java.util.Stack;
 
 public class RpnEngine {
 	private Stack<String> stack = new Stack<String>();
 	private static final Set<String> operatorTokens = new HashSet<String>(Arrays.asList(new String[]{"+", "-", "*", "/"}));
 	private Integer decimals = null;
 	
 	public RpnEngine(int decimals) {
 		this.decimals = decimals;
 	}
 	
 	public RpnEngine() {
 		
 	}
 	
 	private Double popAsDouble() {
 		Double returnValue = Double.NaN;
 		try {
 			returnValue = Double.parseDouble(stack.pop());
 		} catch (NumberFormatException e) {
 			
 		}
 		return returnValue;
 	}
 	
 	private void add() {
 		Double operand2 = popAsDouble();
 		Double operand1 = popAsDouble();
 		stack.push(Double.valueOf(operand1 + operand2).toString());		
 	}
 	
 	private void subtract() {
 		Double operand2 = popAsDouble();
 		Double operand1 = popAsDouble();
 		stack.push(Double.valueOf(operand1 - operand2).toString());		
 	}
 	
 	private void multiply() {
 		Double operand2 = popAsDouble();
 		Double operand1 = popAsDouble();
 		stack.push(Double.valueOf(operand1 * operand2).toString());
 	}
 	
 	private void divide() {
 		Double operand2 = popAsDouble();
 		Double operand1 = popAsDouble();
 		Double result = operand1 / operand2;
 		stack.push(result.toString());
 	}
 	
 	private void performOperation(String operatorToken) {
 		if(stack.size() >= 2) {
 			if (operatorToken.equals("+")) {
 				add();
 			} else if (operatorToken.equals("-")) {
 				subtract();
 			} else if (operatorToken.equals("*")) {
 				multiply();
 			} else if (operatorToken.equals("/")) {
 				divide();
 			}
  		} else {
 			stack.setSize(0);
 			stack.push(String.format("Not enough operands to perform operation [%s]", operatorToken));
 		}
 	}
 	
 	private void pushToken(String token) {
 		if(operatorTokens.contains(token)) {
 			performOperation(token);
 		} else {
 			stack.push(token);
 		}		
 	}
 	
 	private boolean rpnStringContainsValidElements(String testString) {
 		boolean returnValue = true;
 		
 		if(!testString.equals("")) {
 			for(String testValue : testString.split(",")) {
 				if(!operatorTokens.contains(testValue)) {
 					try {
 						Double.parseDouble(testValue);
 					} catch (NumberFormatException e) {
 						returnValue = false;
 					}
 				}
 			}
 		}
 		
 		return returnValue;
 	}
 
 	public String pushPop(String startValue, String rpnString) {
 		String returnValue = startValue.toString();
 		stack.setSize(0);
 		rpnString = String.format("%s,%s", startValue, rpnString);
 		if(rpnStringContainsValidElements(rpnString)) {					
 			for(String token : rpnString.split(",")) {
 				if(token != "") {
 					pushToken(token);
 				}
 			}
 			
 			if(stack.size() == 1) {
 				returnValue = stack.pop();
 				try {
 					Double resultValue = Double.parseDouble(returnValue);
 					String format = "%f";
 					if(decimals != null) {
 						format = String.format(Locale.ENGLISH, "%s.%dg", "%", decimals);
 					}
 					returnValue = String.format(format, resultValue);
					returnValue = returnValue.replaceFirst("\\.0+$", "");
 					returnValue = returnValue.replaceFirst("\\.$", "");
 				} catch (NumberFormatException e) {
 				}
 			} else {
 				returnValue = "Error, stack left with multiple values.";
 			}
 		} else {
 			returnValue = String.format("Invalid rpn expression [%s]", rpnString);
 		}
 		
 		return returnValue;
 	}
 
 }
