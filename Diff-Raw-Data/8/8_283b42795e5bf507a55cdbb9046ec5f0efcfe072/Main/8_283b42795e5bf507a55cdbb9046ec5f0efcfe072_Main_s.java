 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Main.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Main
  */
 package edu.columbia.mipl;
 
 import java.io.*;
 import java.util.*;
 
 import edu.columbia.mipl.syntax.*;
 import edu.columbia.mipl.codegen.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.execute.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 public class Main {
 	public static void main(String[] args) {
 		//Parser parser = new Parser(new Program(new SemanticChecker(), new ProgramExecutor())); // Interactive Mode
 		//Parser parser = new Parser("test/input/multireturn.mipl", new Program(new SemanticChecker(), new ProgramExecutor())); // Interpreter Mode
 		//Parser parser = new Parser("test/input/multireturn.mipl", new SemanticChecker()); // CheckingOnly mode
 
 		// Compiling Mode
 		Parser parser = new Parser(args[0]);
 		if (parser.getNumError() != 0) {
 			System.out.println("Error on parsing input!");
 			return;
 		}
		if (!parser.getProgram().traverse(new SemanticChecker())) {
			System.out.println("There are semantic errors!");
			return;
		}
 		parser.getProgram().traverse(new CodeGenerator("build", "MiplProgram"));
 	}
 }
