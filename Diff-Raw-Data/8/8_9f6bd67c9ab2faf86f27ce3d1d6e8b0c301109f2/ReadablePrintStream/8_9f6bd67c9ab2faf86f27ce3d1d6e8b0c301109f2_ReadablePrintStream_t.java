 package tests.helperclasses;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Stack;
 
 public class ReadablePrintStream extends PrintStream {
 
 	public Stack<String> output;
 	
 	public ReadablePrintStream(OutputStream out) {
 		super(out);
		output = new Stack<>();
 	}
 
 	public void println(String s) {
//		if(output == null) {
//			output = new Stack<>();
//		}
 		output.push(s);	
 		super.println(s);
 	}
 }
