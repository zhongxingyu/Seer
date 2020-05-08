 package com.incandescent.closure;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.google.javascript.jscomp.*;
 import com.google.javascript.jscomp.Compiler;
 
 public class CheckTypes extends CommandLineRunner {
     protected static String[] includes;
     protected static String[] excludes;
 
     // treat non-flag options as js source files
     static String[] parseCommandLine(String[] args) {
         List<String> newArgs = new ArrayList<String>(args.length);
         List<String> inc = new ArrayList<String>();
         List<String> exc = new ArrayList<String>();
         int i = 0;
         while (i < args.length) {
             String arg = args[i];
             if ("--report-include".equals(arg)) {
                 i++;
                 System.err.println("Including in report: "+ args[i]);
                 inc.add(args[i]);
             } else if ("--report-exclude".equals(arg)) {
                 i++;
                 System.err.println("Excluding from report: "+ args[i]);
                 exc.add(args[i]);
             } else if (arg.startsWith("-")) {
                 newArgs.add(arg);
             } else {
                 newArgs.add("--js");
                 newArgs.add(arg);
             }
             i++;
         }
         includes = inc.toArray(new String[inc.size()]);
         excludes = exc.toArray(new String[exc.size()]);
         return newArgs.toArray(new String[newArgs.size()]);
     }
 
     CheckTypes(String[] args) {
         super(parseCommandLine(args));
     }
 
 
     @Override
     protected Compiler createCompiler() {
         Compiler compiler = super.createCompiler();
         compiler.setErrorManager(new FilteredPrintStreamErrorManager(getErrorPrintStream(), includes, excludes));
         return compiler;
     }
 
     @Override
     protected CompilerOptions createOptions() {
         CompilerOptions options = super.createOptions();
         options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
         return options;
     }
 
     public static void main(String[] args) {
         CheckTypes runner = new CheckTypes(args);
         if (runner.shouldRunCompiler()) {
             runner.run();
         } else {
             System.exit(-1);
         }
     }
 }
