 /**
  * BoolExpression Class
  * 
  * Building block for the BoolExpression tree in the Trigger class. This class will be extended into
  * BoolExpressionOperator and BoolExpressionComparison. This version simply contains an internal boolean
  * value, which is returned when evaluate() is called.  	 
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gridentities;
 
 
 public class BoolExpression {
 
 	/**
 	 * Value contained in this boolean expression
 	 */
 	private boolean value; 
 	
 	/**
 	 * Constructor
 	 */
 	public BoolExpression() {
 		// TODO maybe take this out in lieu of an interface; it's needed for subclasses.
 	}
 	
 	/**
 	 * Constructor
 	 * @param value The value this boolean expression will represent
 	 */
 	public BoolExpression(boolean value){
 		this.value = value;
 	}
 	
 	/**
 	 * Evaluate this boolean expression
 	 * @return the evaluation
 	 * @throws Exception if the expression is incorrectly constructed
 	 */
 	public boolean evaluate(Agent me, Agent other) throws Exception {
<<<<<<< HEAD
=======
 		//TODO Method stub?
>>>>>>> b5e6785859b9bd57f79b622af74e897197c5850a
 		return value;
 	}
 
 	/**
 	 * Parses string input and turns it into the tree representation of the boolean expression.
 	 * Every operation should be of the form (x op y) and parenthesized.
 	 * All variables must be "Primitive" (no booleans).
 	 * Examples: ((x op y) op (z op k)), (((x op y) op z) op k).
 	 * @param s The string to parse.
 	 * @return The boolean expression tree.
 	 */
 	public static BoolExpression parseExpression(String s) {
 		//Parse the string and create a BoolExpression
 		// TODO Method stub
 		return null;
 	}
 }
