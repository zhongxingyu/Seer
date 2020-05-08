 /**
  *
  */
 package ca.uwaterloo.joos;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.logging.Logger;
 
 import ca.uwaterloo.joos.ast.AST;
 import ca.uwaterloo.joos.ast.visitor.ToStringVisitor;
 import ca.uwaterloo.joos.parser.LR1;
 import ca.uwaterloo.joos.parser.LR1Parser;
 import ca.uwaterloo.joos.parser.ParseTree;
 import ca.uwaterloo.joos.scanner.DFA;
 import ca.uwaterloo.joos.scanner.Scanner;
 import ca.uwaterloo.joos.scanner.Token;
 import ca.uwaterloo.joos.weeder.Weeder;
 
 /**
  * @author Greg Wang
  *
  */
 public class Main {
 	private static final Logger logger = Main.getLogger(Main.class);
 
 	public static Logger getLogger(Class<?> cls) {
 		return Logger.getLogger(cls.getSimpleName());
 	}
 
 	private final Scanner scanner;
 	private final LR1Parser parser;
 	private final Preprocessor preprocessor;
 	private final Weeder weeder;
 
 	public Main() {
 		// Construct Preprocessor
 		this.preprocessor = new Preprocessor();
 
 		// Read a DFA from file
 		DFA dfa = null;
 		try {
 			dfa = new DFA(new File("resources/joos.dfa"));
 		} catch (Exception e) {
 			System.err.println("ERROR: Invalid DFA File format: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			System.exit(-11);
 		}
 		// Construct Scanner
 		this.scanner = new Scanner(dfa);
 
 		// Read a LR1 from file
 		LR1 lr1 = null;
 		try {
 			lr1 = new LR1(new File("resources/joos.lr1"));
 		} catch (Exception e) {
 			System.err.println("ERROR: Invalid LR1 File format: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(-12);
 		}
 		// Construct Parser
 		this.parser = new LR1Parser(lr1);
 		
 		this.weeder = new Weeder();
 	}
 
 	public Object execute(File source) throws Exception {
 		logger.info("Processing: " + source.getName());
 
 		/* Scanning */
 		// Construct a Scanner which use the DFA
 		List<Token> tokens = null;
 
 		// Scan the source codes into tokens
 		tokens = this.scanner.fileToTokens(source);
 
 		// Preprocess the tokens
 		tokens = preprocessor.processTokens(tokens);
 
 		/* Parsing */
 		ParseTree parseTree = null;
 		parseTree = this.parser.parseTokens(tokens);
 		
 		/* AST Constructing */
 		AST ast = new AST(parseTree, source.getName());
 		
 		ToStringVisitor visitor = new ToStringVisitor();
		visitor.visit(ast.getRoot());
 		System.out.println(visitor.getString());
 		
 		/* AST Weeding */
//		this.weeder.weedAst(ast);
 
 		return ast;
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		if(args.length < 1) {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 			String line = reader.readLine();
 			if(line == null) System.exit(-1);
 			args = line.split("\\s");
 		}
 
 		if(args.length < 1) {
 			System.exit(-1);
 		}
 
 		Main instance = new Main();
 
 		try {
			Object result = instance.execute(new File(args[0]));
			System.out.println(result.toString());
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(42);
 		}
 	}
 }
