 package edu.utwente.vb;
 
 import java.io.File;
 
 import static junit.framework.Assert.*;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.debug.BlankDebugEventListener;
 import org.antlr.runtime.tree.BufferedTreeNodeStream;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 import edu.utwente.vb.example.CodeGenerator;
 import edu.utwente.vb.example.CodeGenerator.OutputMode;
 import edu.utwente.vb.example.Checker;
 import edu.utwente.vb.example.CodegenPreparation;
 import edu.utwente.vb.example.Lexer;
 import edu.utwente.vb.example.NonBlockingBuiltins;
 import edu.utwente.vb.example.Parser;
 import edu.utwente.vb.example.util.CheckerHelper;
 import edu.utwente.vb.symbols.Prelude;
 import edu.utwente.vb.symbols.SymbolTable;
 import edu.utwente.vb.tree.TypedNode;
 import edu.utwente.vb.tree.TypedNodeAdaptor;
 import junit.framework.TestCase;
 
 public abstract class AbstractGrammarTest{
 	protected Logger log = LoggerFactory.getLogger(AbstractGrammarTest.class);
 	/**
 	 * Maak een Parser instantie met de gegeven string als input.
 	 * 
 	 * @param testString
 	 * @return
 	 * @throws IOException
 	 */
 	protected Parser createParser(String testString) throws IOException {
 		CharStream stream = new ANTLRStringStream(testString);
 		return createParser(stream);
 	}
 	
 	protected Parser createParser(CharStream stream) throws IOException{
 		Lexer lexer = new Lexer(stream);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		Parser parser = new Parser(tokens,
 				new BlankDebugEventListener());
 		//*needed* :)
 		parser.setDebug();
 		parser.setTreeAdaptor(new TypedNodeAdaptor());
 		return parser;
 	}
 	
 	protected Checker createChecker(CharStream stream, Parser parser) throws IOException, RecognitionException{
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(parser.program().getTree());
 		
 		Checker	checker = new Checker(nodes, new BlankDebugEventListener());
 		checker.setDebug();
 		/* Patch de symbol table met default functies */
 		SymbolTable<TypedNode> symtab = new SymbolTable<TypedNode>();
 		Prelude pre = new Prelude();
 		pre.inject(symtab);
 		symtab.openScope();
 		CheckerHelper ch = new CheckerHelper(symtab);
 		checker.setCheckerHelper(ch);
 		
 		checker.setTreeAdaptor(new TypedNodeAdaptor());
 
 		return checker;
 	}
 	
 	protected CodegenPreparation createCodegenPreparation(CharStream stream, Parser parser) throws IOException, RecognitionException{
 		Checker checker = createChecker(stream, parser);
 		Checker.program_return checker_result = checker.program();
 		BufferedTreeNodeStream checker_nodes = new BufferedTreeNodeStream((TypedNode)checker_result.getTree());
 		CodegenPreparation prepare = new CodegenPreparation(checker_nodes, new BlankDebugEventListener());
 		prepare.setTreeAdaptor(new TypedNodeAdaptor());
 		
 		return prepare;
 	}
 	
 	protected CodeGenerator createCodegenerator(CharStream stream, Parser parser) throws IOException, RecognitionException{
 		CodegenPreparation prep = createCodegenPreparation(stream, parser);
 		CodegenPreparation.program_return prep_result = prep.program();
 		
 		BufferedTreeNodeStream cg_nodes = new BufferedTreeNodeStream((TypedNode)prep_result.getTree());
 		CodeGenerator gen = new CodeGenerator(cg_nodes, new BlankDebugEventListener());
 		gen.setTreeAdaptor(new TypedNodeAdaptor());
 		gen.setOutputMode(OutputMode.FILE);
 		
 		return gen;
 	}
 
 	
 	protected CharStream asCharStream(File f) throws IOException{
 		return new ANTLRFileStream(f.toString());
 	}
 }
