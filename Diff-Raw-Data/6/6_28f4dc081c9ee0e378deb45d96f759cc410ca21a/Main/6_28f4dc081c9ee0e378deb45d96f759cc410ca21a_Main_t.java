 /**
  *
  */
 package ca.uwaterloo.joos;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
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
 import ca.uwaterloo.joos.symboltable.SymbolTable;
 import ca.uwaterloo.joos.typelinker.TypeLinker;
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
 
 	public AST constructAst(File source) throws Exception {
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
 		
 //		System.out.println(parseTree);
 		
 		/* AST Constructing */
 		AST ast = new AST(parseTree, source.getName());
 		
 //		ToStringVisitor visitor = new ToStringVisitor();
 //		ast.getRoot().accept(visitor);
 //		System.out.println(visitor.getString());
 		
 		/* AST Weeding */
 		this.weeder.weedAst(ast);
 
 		return ast;
 	}
 	
 	public void typeChecking(List<AST> asts) throws Exception {
 		SymbolTable table = new SymbolTable();
 		
 		table.build(asts);
 //		table.listScopes();
 		
 		for(AST ast: asts) {
 //			System.out.println("Checking " + ast.getRoot().getIdentifier() );
 			TypeLinker linker = new TypeLinker(table);
 			ast.getRoot().accept(linker);
 		}
 	}
 	
 	public void execute(String[] args) throws Exception {
//		for(String arg: args) {
//			System.out.println("Source: " + arg);
//		}
 		
 		List<AST> asts = new ArrayList<AST>();
 		for(String arg: args) {
 			asts.add(this.constructAst(new File(arg)));
 		}
 		
 		typeChecking(asts);
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		if(args.length < 1) {
 			System.exit(-1);
 		}
 		
 		Main instance = new Main();
 
 		try {
 			instance.execute(args);
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(42);
 		}
 	}
 }
