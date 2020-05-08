 /** Variable is a class to represent a variable
 	@author Michelle Len
 	@version 02/14/2012 for CS 48 Project, W12
 */
 
 public class Variable {
 
 	private char var; // variable represented as a char
 
 	/** Constructor
 		@param var What the variable is
 	*/
 
 	public Variable (char var) {
 		// assign attributes from parameters
 		this.var = var;
 	}
 
 	/**
 		Get the variable character
  	*/
 	
 	public char getVar () {
 		return this.var;
 	}
 
 	/**
 		Returns object as a string
 	*/
 	
 	public String toString() {
		return Character.toString(var);
 	} // do we need this method?
 
 } // class Variable
