 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 import org.apache.commons.cli.*;
 
 import java.io.*;
 import java.util.Vector;
 import java.util.HashMap;
 
 /**
  * This is the base of our compiler.
  *
  * It deals with command line arguments, and sets the flow of things.
  *
  * @author Nat Welch
  * @author Ben Sweedler
  */
 public class Evil {
    public static CommandLine cmd = null;
    private static String inputFile = null;
 
    public static void main(String[] args) {
       // Store the options
       parseParameters(args);
 
       CommonTokenStream tokens = new CommonTokenStream(createLexer());
       EvilParser parser = new EvilParser(tokens);
       EvilParser.program_return ret = null;
 
       try {
          ret = parser.program();
       } catch (org.antlr.runtime.RecognitionException e) {
          error(e.toString());
       }
 
       CommonTree t = (CommonTree)ret.getTree();
       if (cmd.hasOption("displayAST") && t != null) {
          DOTTreeGenerator gen = new DOTTreeGenerator();
          StringTemplate st = gen.toDOT(t);
          System.out.println(st);
       }
 
       // To create and invoke a tree parser.  Modify with the appropriate
       // name of the tree parser and the appropriate start rule.
       try {
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
          TypeCheck tparser = new TypeCheck(nodes);
          CFG cfg = new CFG(nodes);
 
          if (typeFlag)
             tparser.verify();
 
          cfg.build();
          if (dumpFlag)
             cfg.dump();

       } catch (org.antlr.runtime.RecognitionException e) {
          error(e.toString());
       }
    }
 
    // Input Flags
    private static boolean debugFlag = false;
    private static boolean dumpFlag = false;
    private static boolean typeFlag = true;
 
    /**
     * Defines possible options and sets them up.
     *
     * see http://commons.apache.org/cli/usage.html for more details.
     */
    private static void parseParameters(String [] args) {
       // create the command line parser
       CommandLineParser parser = new PosixParser();
 
       // create the options
       Options options = new Options();
       options.addOption("a", "displayAST", false, "Print out a dotty graph of the AST." );
       options.addOption("d", "debug", false, "Print debug messages while running." );
       options.addOption("h", "help", false, "Print this help message." );
       options.addOption("i", "dumpIL", false, "Dump ILOC to STDOUT." );
       options.addOption("t", "notype", false, "Don't Typecheck." );
 
       try {
          // parse the command line arguments
          cmd = parser.parse( options, args );
 
          if (cmd.hasOption("help")) {
             // automatically generate the help statement
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("java Evil [options] filename.ev", options);
             System.exit(0);
          }
 
          if (cmd.hasOption("debug")) {
             debugFlag = true;
          }
 
          if (cmd.hasOption("dumpIL")) {
             dumpFlag = true;
          }
 
          if (cmd.hasOption("notype")) {
             typeFlag = false;
          }
 
          String[] fileArgs = cmd.getArgs();
 
          // Set input file.
          if (fileArgs.length > 1) {
             error("Only one file can be compiled at a time.");
          } else if (fileArgs.length == 0) {
             error("No files specified.");
          } else {
             inputFile = fileArgs[0];
          }
       } catch (ParseException exp) {
          error("Unexpected exception:" + exp.getMessage());
       }
    }
 
    public static void error(String msg) {
       error(msg, 0);
    }
 
    public static void error(String msg, int lineno) {
       if (inputFile == null)
          System.err.println(lineno + " : " + msg);
       else
          System.err.println(inputFile + ":" + lineno + " : " + msg);
 
       System.exit(1);
    }
 
    public static void debug(String msg) {
       if (debugFlag) {
          System.err.println(msg);
       }
    }
 
    private static EvilLexer createLexer() {
       try {
          ANTLRInputStream input;
 
          input = new ANTLRInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
 
          return new EvilLexer(input);
       } catch (java.io.IOException e) {
          System.err.println("file not found: " + inputFile);
          System.exit(1);
          return null;
       }
    }
 }
