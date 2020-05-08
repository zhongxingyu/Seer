 package core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import javax.swing.JFileChooser;
 
 import nodes.ProgramNode;
 
 /**
  * The parser and interpreter. The top level parse function, a main method for
  * testing, and several utility methods are provided. You need to implement
  * parseProgram and all the rest of the parser.
  */
 
 public class Parser {
 
 	/**
 	 * Top level parse method, called by the World
 	 */
 	static RobotProgramNode parseFile(File code) {
 		Scanner scan = null;
 		try {
 			scan = new Scanner(code);
 
 			// the only time tokens can be next to each other is
 			// when one of them is one of (){},;
 			scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
 
 			RobotProgramNode n = parseProgram(scan); // You need to implement
 														// this!!!
 
 			scan.close();
 			return n;
 		} catch (FileNotFoundException e) {
 			System.out.println("Robot program source file not found");
 		} catch (ParserFailureException e) {
 			System.out.println("Parser error:");
 			System.out.println(e.getMessage());
 			scan.close();
 		}
 		return null;
 	}
 
 	/** For testing the parser without requiring the world */
 
 	public static void main(String[] args) {
 		if (args.length > 0) {
 			for (String arg : args) {
 				File f = new File(arg);
 				if (f.exists()) {
 					System.out.println("Parsing '" + f + "'");
 					RobotProgramNode prog = parseFile(f);
 					System.out.println("Parsing completed ");
 					if (prog != null) {
 						System.out.println("================\nProgram:");
 						System.out.println(prog);
 					}
 					System.out.println("=================");
 				} else {
 					System.out.println("Can't find file '" + f + "'");
 				}
 			}
 		} else {
 			while (true) {
 				JFileChooser chooser = new JFileChooser(".");// System.getProperty("user.dir"));
 				int res = chooser.showOpenDialog(null);
 				if (res != JFileChooser.APPROVE_OPTION) {
 					break;
 				}
 				RobotProgramNode prog = parseFile(chooser.getSelectedFile());
 				System.out.println("Parsing completed");
 				if (prog != null) {
 					System.out.println("Program: \n" + prog);
 				}
 				System.out.println("=================");
 			}
 		}
 		System.out.println("Done");
 	}
 
 	// Useful Patterns
 
 	//Structure
 	public static Pattern NUMPAT = Pattern.compile("-?\\d+"); // ("-?(0|[1-9][0-9]*)");
 	public static Pattern OPENPAREN = Pattern.compile("\\(");
 	public static Pattern CLOSEPAREN = Pattern.compile("\\)");
 	public static Pattern OPENBRACE = Pattern.compile("\\{");
 	public static Pattern CLOSEBRACE = Pattern.compile("\\}");
 	public static Pattern SEMICOLONPAT  = Pattern.compile("\\;");
 	public static Pattern COMMAPAT  = Pattern.compile("\\,");
 	public static Pattern EQUIVPAT  = Pattern.compile("\\=");
	public static Pattern VARPAT  = Pattern.compile("test");//("$[A-Za-z][A-Za-z0-9]*");TODO
 
 	//Terminals
 	public static Pattern MOVEPAT = Pattern.compile("move");
 	public static Pattern TAKEFUELPAT = Pattern.compile("takeFuel");
 	public static Pattern TURNLPAT = Pattern.compile("turnL");
 	public static Pattern TURNRPAT = Pattern.compile("turnR");
 	public static Pattern WAITPAT = Pattern.compile("wait");
 	public static Pattern LOOPPAT = Pattern.compile("loop");
 	public static Pattern WHILEPAT = Pattern.compile("while");
 	public static Pattern IFPAT = Pattern.compile("if");
 	public static Pattern ELIFPAT = Pattern.compile("elif");
 	public static Pattern ELSEPAT = Pattern.compile("else");
 	public static Pattern TURNAROUNDPAT = Pattern.compile("turnAround");
 	public static Pattern SHIELDONPAT = Pattern.compile("shieldOn");
 	public static Pattern SHIELDOFFPAT = Pattern.compile("shieldOff");
 	public static Pattern LESSPAT = Pattern.compile("lt");
 	public static Pattern GREATERPAT = Pattern.compile("gt");
 	public static Pattern EQUALPAT = Pattern.compile("eq");
 	public static Pattern ANDPAT = Pattern.compile("and");
 	public static Pattern ORPAT = Pattern.compile("or");
 	public static Pattern NOTPAT = Pattern.compile("not");
 	public static Pattern FUELLEFTPAT = Pattern.compile("fuelLeft");
 	public static Pattern OPPLRPAT = Pattern.compile("oppLR");
 	public static Pattern OPPFBPAT = Pattern.compile("oppFB");
 	public static Pattern NUMBARRELPAT = Pattern.compile("numBarrels");
 	public static Pattern BARRELLRPAT = Pattern.compile("barrelLR");
 	public static Pattern BARRELFBPAT = Pattern.compile("barrelFB");
 	public static Pattern WALLDISTPAT = Pattern.compile("wallDist");
 	public static Pattern ADDPAT = Pattern.compile("add");
 	public static Pattern SUBPAT = Pattern.compile("sub");
 	public static Pattern MULPAT = Pattern.compile("mul");
 	public static Pattern DIVPAT = Pattern.compile("div");
 
 	//Non-Terminals
 	public static Pattern ACTIONPAT = Pattern.compile("move|takeFuel|turnL|turnR|wait|turnAround|shieldOn|shieldOff");
 	public static Pattern SENSORPAT = Pattern.compile("fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist");
 	public static Pattern CONDITIONPAT = Pattern.compile("and|or|not|lt|gt|eq");
 	public static Pattern OPERATIONPAT = Pattern.compile("add|sub|mul|div");
 
 
 	/**
 	 * PROG ::= STMT+
 	 */
 	public static RobotProgramNode parseProgram(Scanner s) {
 		RobotProgramNode program = new ProgramNode();
 		if(program.parse(s,0)){
 			return program;
 		}else{
 			return null;
 		}
 	}
 
 	// utility methods for the parser
 	/**
 	 * Report a failure in the parser.
 	 */
 	public static void fail(String message, Scanner s) {
 		String msg = message + "\n   @ ...";
 		for (int i = 0; i < 5 && s.hasNext(); i++) {
 			msg += " " + s.next();
 		}
 		throw new ParserFailureException(msg + "...");
 	}
 
 	/**
 	 * If the next token in the scanner matches the specified pattern, consume
 	 * the token and return true. Otherwise return false without consuming
 	 * anything. Useful for dealing with the syntactic elements of the language
 	 * which do not have semantic content, and are there only to make the
 	 * language parsable.
 	 */
 	public static boolean gobble(String p, Scanner s) {
 		if (s.hasNext(p)) {
 			s.next();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean gobble(Pattern p, Scanner s) {
 		if (s.hasNext(p)) {
 			s.next();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
 
 // You could add the node classes here, as long as they are not declared public
 // (or private)
