 package org.jackie.jclassfile.model;
 
 /**
  * @author Patrik Beno
  */
 public enum Type {
 
 	BYTE('B', byte.class),
 	CHAR('C', char.class),
 	DOUBLE('D', double.class),
 	FLOAT('F', float.class),
 	INT('I', int.class),
 	LONG('J', long.class),
 	SHORT('S', short.class),
 	BOOLEAN('Z', boolean.class),
	CLASS('L', Object.class),
 
 	;
 
 	private char code;
 	private Class type;
 
 	Type(char code, Class type) {
 		this.code = code;
 		this.type = type;
 	}
 
 	public char code() {
 		return code;
 	}
 
 	public Class type() {
 		return type;
 	}
 
 	public boolean isPrimitive() {
 		return type != null && type.isPrimitive();
 	}
 
 	public static Type forCode(char c) {
 		for (Type t : Type.values()) {
 			if (t.code() == c) {
 				return t;
 			}
 		}
 		throw new IllegalArgumentException(Character.toString(c));
 	}
 }
