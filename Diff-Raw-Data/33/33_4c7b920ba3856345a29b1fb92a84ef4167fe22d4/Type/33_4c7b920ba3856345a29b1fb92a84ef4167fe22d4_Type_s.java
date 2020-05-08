 package edu.utwente.vb.symbols;
 
 /**
 * Typen
  * @author Ties
  *
  */
 public enum Type{
 	VOID, INT, CHAR, STRING;
 	
 	public static boolean isNumeric(Type t){
 		return Type.INT.equals(t);
 	}
 }
