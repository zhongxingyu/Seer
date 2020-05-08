 package org.cwi.waebric.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cwi.waebric.parser.ast.AbstractSyntaxTree;
 import org.cwi.waebric.parser.ast.module.Modules;
 import org.cwi.waebric.scanner.token.TokenIterator;
 
 /**
  * Parser attempts to reconstruct the derivation of a Waebric program,
  * this returns a collection of error messages. The parsing process was 
  * successful when zero errors are returned. After a successful parsing 
  * attempt the Abstract Syntax Tree (AST) can be retrieved.
  * 
  * @author Jeroen van Schagen
  * @date 18-05-2009
  */
 public class WaebricParser extends AbstractParser {
 
 	private AbstractSyntaxTree tree;
 
 	/**
 	 * Construct parser based on iterator.
 	 * @param iterator
 	 */
 	public WaebricParser(TokenIterator iterator) {
 		super(iterator, new ArrayList<SyntaxException>());
 	}
 	
 	/**
 	 * Parse token stream in abstract syntax tree.
 	 * 
 	 * @return Exceptions
 	 */
 	public List<SyntaxException> parseTokens() {
 		exceptions.clear(); // Clear exceptions
 		ModuleParser parser = new ModuleParser(tokens, exceptions);
 		
 		try {
 			Modules modules = parser.parseModules(); // Parse root node
 			tree = new AbstractSyntaxTree(modules); // Construct AST
		} catch (SyntaxException e) {
			throw new InternalError(); // Exceptions are logged
		}
 		
 		return exceptions; // Publish exceptions
 	}
 	
 	/**
 	 * Retrieve abstract syntax tree (AST).
 	 * 
 	 * @return
 	 */
 	public AbstractSyntaxTree getAbstractSyntaxTree() {
 		return tree;
 	}
 	
 }
