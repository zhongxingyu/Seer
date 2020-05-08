 package codeGeneration;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import malice_grammar.malice_grammarLexer;
 import malice_grammar.malice_grammarParser;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.Tree;
 import org.junit.Test;
 
 import semantics_checks.ExpressionChecker;
 import symbol_table.DATA_TYPES;
 import symbol_table.FunctionSTValue;
 import symbol_table.SymbolTable;
 import symbol_table.VariableSTValue;
 
 public class StatementsCodeGeneratorMagdaTest {
 
 //	@Test
 //	public void testAteAndDrank() throws RecognitionException {
 //		System.out.println("\nSTART TESTING ATE AND DRANK...\n");
 //		String st = "x ate";
 //		String st1 = "y ate";
 //		String st2 = "z ate";
 //		String st3 = "x drank";
 //		String st4 = "y drank";
 //		String st5 = "fnX drank";
 //		
 //		assertTrue(generateCodeForStatement(st, STATS.ATE));
 //		assertTrue(generateCodeForStatement(st1, STATS.ATE));
 //		assertTrue(generateCodeForStatement(st2, STATS.ATE));
 //		assertTrue(generateCodeForStatement(st3, STATS.DRANK));
 //		assertTrue(generateCodeForStatement(st4, STATS.DRANK));
 //		assertTrue(generateCodeForStatement(st5, STATS.DRANK));
 //		
 //		//prints out stuff for letters and sentences but thats fine as 
 //		//the statement checker will take care of it
 //	
 //	}
 	
 //	@Test
 //	public void testBecame() throws RecognitionException {
 //		
 //		System.out.println("\n\nSTART TESTING BECAME...\n");
 //		String st = "x became 2";
 //		String st1 = "y became 'a'";
 //		String st2 = "z became \"\"";
 //		String st3 = "x became 2 + 3";
 //		String st4 = "y became '0'";//should give error
 //		String st5 = "fnX became 3";
 //		String st6 = "z2 became \"rawwr\"";
 //		
 //		assertTrue(generateCodeForStatement(st, STATS.BECAME));
 //		assertTrue(generateCodeForStatement(st1, STATS.BECAME));		
 //		assertTrue(generateCodeForStatement(st2, STATS.BECAME));		
 //		assertTrue(generateCodeForStatement(st3, STATS.BECAME));
 //		assertTrue(generateCodeForStatement(st4, STATS.BECAME));
 //		assertTrue(generateCodeForStatement(st5, STATS.BECAME));
 //		assertTrue(generateCodeForStatement(st6, STATS.BECAME));
 //		
 //		
 //	}
 	
 	@Test
 	public void testFound() throws RecognitionException {
 		System.out.println("\n\nSTART TESTING FOUND...\n");
 
 		String st = "Alice found x";
 		String st1 = "Alice found y";
 		String st2 = "Alice found z";
 		String st3 = "Alice found fnX";
 		String st4 = "Alice found z2";
 		
 		assertTrue(generateCodeForStatement(st, STATS.FOUND));
 		assertTrue(generateCodeForStatement(st1, STATS.FOUND));
 		assertTrue(generateCodeForStatement(st2, STATS.FOUND));
 		assertTrue(generateCodeForStatement(st3, STATS.FOUND));
 		assertTrue(generateCodeForStatement(st4, STATS.FOUND));
 
 	}
 	
 	@Test
 	public void testSaidAndSpoke() throws RecognitionException {
 		System.out.println("\n\nSTART TESTING SPOKE...\n");
 
 		String st = "x said Alice";
 		String st1 = "y said Alice";
 		String st2 = "z said Alice";
 		String st3 = "fnX said Alice";
 		String st4 = "z2 said Alice";
 		String st5 = "(2+2) said Alice";
 		String st6 = "\'a\' said Alice";
 		
 		assertTrue(generateCodeForStatement(st, STATS.SAID));
 		assertTrue(generateCodeForStatement(st1, STATS.SAID));		
 		assertTrue(generateCodeForStatement(st2, STATS.SAID));
 		assertTrue(generateCodeForStatement(st3, STATS.SAID));
 		assertTrue(generateCodeForStatement(st4, STATS.SAID));
 		assertTrue(generateCodeForStatement(st5, STATS.SAID));
 		assertTrue(generateCodeForStatement(st6, STATS.SAID));
 	}
 	
 	
 	
 	private SymbolTable initTable(SymbolTable table) {
 		SymbolTable current = table;
 		ArrayList<DATA_TYPES> args = new ArrayList<DATA_TYPES>();
 		VariableSTValue x = new VariableSTValue(DATA_TYPES.NUMBER, true);
 		VariableSTValue y = new VariableSTValue(DATA_TYPES.LETTER, true);
 		VariableSTValue z = new VariableSTValue(DATA_TYPES.SENTENCE, true);
 
 		
 		args.add(DATA_TYPES.NUMBER);
 		
 		FunctionSTValue fn = new FunctionSTValue(current,args);
 		VariableSTValue fnX = new VariableSTValue(DATA_TYPES.NUMBER, true);
 		VariableSTValue z2 = new VariableSTValue(DATA_TYPES.SENTENCE, true);
 
 		
 		x.setLocationReg("@x");
 		y.setLocationReg("@y");
 		z.setLocationReg("@z");
 		fn.setLocationReg("@fn");
 		fnX.setLocationReg("%fnX");
 		z2.setLocationReg("%z2");
 		
 		current.insert("x", x);
 		current.insert("y", y);
 		current.insert("z", z);
 		
 		current.insert("fn", fn);
 		current = fn.getTable();
 		current.insert("fnX",fnX);
 		current.insert("z2", z2);
 		//current = current.finalizeCurrentScopeLevelTable();
 		return current;
 	}
 
 	
 
 	private boolean generateCodeForStatement(String expr, STATS statement)
 			throws RecognitionException {
 		new ExpressionChecker();
 		SymbolTable table = new SymbolTable();
 		table = initTable(table);
 		CharStream input = new ANTLRStringStream(expr); 
 		malice_grammarLexer lexer = new malice_grammarLexer(input );
 		TokenStream tokens = new CommonTokenStream(lexer);
 		malice_grammarParser parser = new malice_grammarParser(tokens ) ;
 		if (!parser.failed()) {
 			Tree tree = (Tree) parser.statement().getTree();
 			System.out.println(tree.toStringTree());
 			
 			switch(statement) {
 				case ATE:StatementsCodeGeneratorMagda.writeAteCode(tree, table, new LabelGenerator());break;
 				case DRANK:StatementsCodeGeneratorMagda.writeDrankCode(tree, table, new LabelGenerator());break;
 				case BECAME:StatementsCodeGeneratorMagda.writeBecameCode(tree, table, new LabelGenerator());break;
 				case FOUND:StatementsCodeGeneratorMagda.writeFoundCode(tree, table, new LabelGenerator());break;
 				case SAID:StatementsCodeGeneratorMagda.writePrintStatementCode(tree, table, new LabelGenerator());break;
 			}
 
 			CodeGenerator.printInstructions();
 			CodeGenerator.emptyInstructions();
 			return true;
 		}
 		return false;
 	}
 	
 	private enum STATS {
 		ATE, DRANK, BECAME, FOUND, SAID;
 	}
 	
 }
