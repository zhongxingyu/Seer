 /**
  * WhileInterpreter.java
  */
 
 package wam.interpreter;
 
 import java.io.*;
 import java.util.*;
 
 import wam.log.*;
 import wam.ast.*;
 import wam.code.*;
 import wam.exception.*;
 import wam.visitor.*;
 import wam.vm.*;
import wam.parser.*;
 
 public class WhileInterpreter {
     /**
      * Main method for an interpreter for the While language.
      */
     public static void main(String[] args) {
         Log.setLogger(new StreamLogger(System.err));
 
         if (args.length > 0) {
             try {
                 WhileParser parser = new WhileParser(new FileReader(args[0]));
                 Program program = parser.program("");
 
                 PrettyWhilePrinter visitor = new PrettyWhilePrinter();
                 System.out.println(program.accept(visitor));
 
                 WhileTranslator translator = new WhileTranslator();
                 Stack<Instruction> code = program.accept(translator);
                 System.out.println(Instruction.codeToString(code));
 
                 VirtualMachine machine = new VirtualMachine();
                 Configuration configuration = new Configuration(code);
 
                 while (!configuration.isFinal()) {
                     configuration = machine.step(configuration);
                 }
                 System.out.println(configuration.getState());
            } catch (TokenMgrError e) {
                Log.logFatal("Parsing failed.");
                Log.logFatal(e.getMessage());
                System.exit(1);
             } catch (ParseException e) {
                 Log.logFatal("Parsing failed.");
                 Log.logFatal(e.getMessage());
                 System.exit(1);
             } catch (IOException e) {
                 Log.logFatal(e.getMessage());
                 System.exit(1);
             } catch (WamException e) {
                 Log.logFatal(e.getMessage());
                 System.exit(1);
             }
         } else {
             if (interactivePrompt() != 0)
                 System.exit(1);
         }
 
         System.exit(0);
     }
 
     /**
      * Run an interactive prompt, evaluating each line of code in input.
      */
     private static int interactivePrompt() {
         String line = null;
         WhileParser parser = null;
         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("> ");
 
         try {
             line = reader.readLine();
         } catch (IOException e) {
             Log.logFatal(String.format("IOError: %s", e.getMessage()));
             return 1;
         }
 
         while (line != null) {
             if (parser == null)
                 parser = new WhileParser(new StringReader(line));
             else
                 parser.ReInit(new StringReader(line));
 
             try {
                 Program program = parser.program(line);
             } catch (ParseException e) {
                 Log.logError(String.format("Failed: %s", e.getMessage()));
             }
 
             System.out.print("> ");
 
             try {
                 line = reader.readLine();
             } catch (IOException e) {
                 Log.logFatal(String.format("IOError: %s", e.getMessage()));
                 return 1;
             }
         }
 
         System.out.println();
         return 0;
     }
 }
