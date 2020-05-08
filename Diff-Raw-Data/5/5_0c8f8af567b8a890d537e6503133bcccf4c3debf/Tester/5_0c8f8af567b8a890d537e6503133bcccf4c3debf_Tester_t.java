 package com.github.kanafghan.welipse.webdsl.parsers.tests;
 
 import com.github.kanafghan.welipse.webdsl.Expression;
 import com.github.kanafghan.welipse.webdsl.Parameter;
 import com.github.kanafghan.welipse.webdsl.VariableInitialization;
 import com.github.kanafghan.welipse.webdsl.parsers.ExpressionsLanguage;
 
 public class Tester {
 	
 	private static final int EXPRESSION = 0;
 	private static final int VARIABLES = 1;
 	private static final int PARAMETERS = 2;
 
 	public static void main(String[] args) {
 		// Test cases for testing expressions
 		String[] expressions = {
 			"1+2*2-5",
 			"2*3/4+5-(3+(-2))",
 			"player.name",
 			"c.c1",
 			"(1+(2+(3+(4+5))))+6",
			"1+5*3",
			"1>2 || 3<5",
			"3 != 5 && 4 <= x || false",
			"2 > 3 == 5",
			"2 == 3 > 5",
 			"8/2",
 			"1*(2*3)*(4*5)",
 			"x >= player.scores(true)",
 			"\"hello\".length",
 			"\"hello\".concat(\" world\")",
 			"[]",
 			"[\"Man\", \"Woman\"]",
 			"[1 => \"Man\", 0 => \"Woman\"]",
 			"WebUtils.getAllPlayer()"
 		};
 		// Test cases for creating Variables
 		String[] vars = {
 			"name:EString=player.name",
 			"name : EString = player.name",
 			"x:EInt = 2*(player.height-30)"
 		};
 		// Test cases for creating Parameters
 		String[] params = {
 			"player:Player",
 			"x:EInt",
 			"role : Role"
 		};
 
 		runTests(expressions, EXPRESSION);
 		runTests(vars, VARIABLES);
 		runTests(params, PARAMETERS);
 	}
 
 	private static void runTests(String[] expressions, int kind) {
 		for (String tc: expressions) {
 			ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 			parser.setExpression(tc);
 			try {
 				switch (kind) {
 				case EXPRESSION:
 					Expression e = parser.getExpression();
 					if (e != null) {						
 						System.out.println("Test Case: "+ tc +" PASSED ("+ e.toString() +")");
 					} else {
 						throw new Exception("Could not parse the expression!");
 					}
 					break;
 				case VARIABLES:
 					VariableInitialization v = parser.getVariable();
 					if (v != null) {						
 						System.out.println("Test Case: "+ tc +" PASSED ("+ v.toString() +")");
 					} else {
 						throw new Exception("Could not parse the variable initialization!");
 					}
 					break;
 				case PARAMETERS:
 					Parameter p = parser.getParameter();
 					if (p != null) {						
 						System.out.println("Test Case: "+ tc +" PASSED ("+ p.toString() +")");
 					} else {
 						throw new Exception("Could not parse the parameter!");
 					}
 					break;
 				default:
 					break;
 				}
 			} catch (Exception e) {
 				System.err.println("Test Case: "+ tc +" FAILED ("+ e.getMessage() +")");
 				e.printStackTrace();
 			}
 		}
 	}
 }
