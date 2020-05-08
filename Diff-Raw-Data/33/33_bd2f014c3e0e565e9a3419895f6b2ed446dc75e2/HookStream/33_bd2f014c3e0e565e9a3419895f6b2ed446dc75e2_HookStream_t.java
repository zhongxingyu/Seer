 package com.jpii.gamekit.debug;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 
 import com.jpii.gamekit.GameKit;
 
 
 public class HookStream extends PrintStream {
 
 	public HookStream(OutputStream out) {
 		super(out);
 	}
 
 	public HookStream(String fileName) throws FileNotFoundException {
 		super(fileName);
 	}
 
 	public HookStream(File file) throws FileNotFoundException {
 		super(file);
 	}
 
 	public HookStream(OutputStream out, boolean autoFlush) {
 		super(out, autoFlush);
 	}
 
 	public HookStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
 		super(fileName, csn);
 	}
 
 	public HookStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
 		super(file, csn);
 	}
 
 	public HookStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
 		super(out, autoFlush, encoding);
 	}
 	
     public void print(boolean b) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Boolean.toString(b));
 		}
     }
 
     public void print(char c) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Character.toString(c));
 		}
     }
 
     public void print(int i) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Integer.toString(i));
 		}
     }
 
     public void print(long l) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Long.toString(l));
 		}
     }
 
     public void print(float f) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Float.toString(f));
 		}
     }
 
     public void print(double d) {
 		if (GameKit.debug != null) {
 			GameKit.debug.printInfo(Double.toString(d));
 		}
     }
 
     public void print(String s) {
     	if (GameKit.debug != null) {
 			GameKit.debug.printInfo(s);
 		}
     }
 
     public void print(Object obj) {
     	if (GameKit.debug != null) {
 			GameKit.debug.printInfo(obj.toString());
 		}
     }
 
     public void println() {
     	if (GameKit.debug != null) {
     		GameKit.debug.println("");
     	}
     }
 
     public void println(boolean x) {
 		print(x);
     }
 
     public void println(char x) {
 		print(x);
     }
 
     public void println(int x) {
 		print(x);
     }
 
     public void println(long x) {
 		print(x);
     }
 
     public void println(float x) {
 		print(x);
     }
 
     public void println(double x) {
 		print(x);
     }
     
 	public void println(String x) {
 		print(x);
 	}
 
     public void println(Object x) {
 		print(x);
     }
 }
