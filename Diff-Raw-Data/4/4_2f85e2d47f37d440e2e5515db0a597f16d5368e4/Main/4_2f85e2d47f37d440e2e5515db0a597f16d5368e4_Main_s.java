 package org.spoofax.interpreter.cli;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.spoofax.interpreter.ConcreteInterpreter;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.jsglr.InvalidParseTableException;
 
 public class Main {
 
 	public static void main(String[] args) throws IOException, InterpreterException, InvalidParseTableException {
 	    
 		ConcreteInterpreter intp = new ConcreteInterpreter();
 		
         List<String> includes = new LinkedList<String>();
         String toCompile = null;
         String outFile = null;
         int skip = 0;
         
         for(int i = 0; i < args.length; i++) {
             if(skip > 0) {
                 skip--;
                 continue;
             }
             final String arg = args[i];
             if(arg.equals("-I")) {
                 skip++;
                 includes.add(args[i+1]);
             } else if(arg.equals("-i")) {
                 toCompile = args[i+1];
                 skip++;
             } else if(args.equals("-o")) {
                 outFile = args[i+1];
                 skip++;
             }
             else if(arg.startsWith("-")) {
                 System.err.println("Unknown option " + arg);
                 System.exit(-12);
             } else {
                 
             }
             
         }
         
         if(toCompile == null) {
         	System.err.println("No files to compile");
         	return;
         }
         includes.add(System.getProperty("user.home") + ".nix-profile/share/");
         
         intp.load(System.getProperty("share.dir") + "/libstratego-lib.ctree");
 		intp.loadConcrete(toCompile, includes.toArray(new String[0]), false);
 		intp.setCurrent(intp.getFactory().makeList());
 		intp.invoke("main_0_0");
 	}
 }
