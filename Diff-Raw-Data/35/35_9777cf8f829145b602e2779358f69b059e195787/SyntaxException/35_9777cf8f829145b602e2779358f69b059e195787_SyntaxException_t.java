 package main;
 
 public class SyntaxException extends Exception {
 	protected int position;
 	protected String error;
 	
 	public SyntaxException(int position, String error) {
 		this.position = position;
 		this.error = error;
 	}
 
 	public int getPosition() {
 		return this.position;
 	}
 
 	public String getError() {
 		return this.error;
 	}
 	
 	public String toString() {
 		return "Syntax error at position " + this.position 
			 + ": \"" + this.error + "\"";
 	}
 }
