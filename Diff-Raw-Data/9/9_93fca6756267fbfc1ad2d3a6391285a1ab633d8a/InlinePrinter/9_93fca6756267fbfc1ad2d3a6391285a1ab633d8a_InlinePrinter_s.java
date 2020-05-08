 package org.spoofax.interpreter.terms;
 
 public class InlinePrinter implements ITermPrinter {
 	private final StringBuilder out;
 	
 	public InlinePrinter() {
 		out = new StringBuilder();
 	}
 	
 	public String getString() {
 		return out.toString();
 	}
 
 	public void print(String string) {
 		if (string.matches("^ *$")) {
 			return ;
 		}
 		out.append(string);
 	}
 
 	public void indent(int i) {
 	}
 
 	public void nextIndentOff() {
 	}
 
 	public void outdent(int i) {
 	}
 
 	public void println(String string, boolean b) {
 		if (string.matches("^ *$")) {
 			return ;
 		}
 		out.append(string);
 	}
 
 	public void println(String string) {
 		out.append(string);
 	}
 }
