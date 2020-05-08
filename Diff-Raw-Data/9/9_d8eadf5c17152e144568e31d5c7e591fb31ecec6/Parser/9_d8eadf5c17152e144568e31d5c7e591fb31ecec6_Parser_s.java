 package crux;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Stack;
 
 import ast.Command;
 
 /**
  * Syntactic parser that reads a stream of tokens and builds a parse tree.
  */
 public class Parser {
     /* Author details. */
     public static String studentName = "Erik Westrup";
     public static String studentID = "50471668";
     public static String uciNetID = "ewestrup";
 
 	/* Current depth of the tree (where we are at). */
 	private int parseTreeRecursionDepth = 0;
 
 	/* The string representation of our parse tree. */
 	private StringBuffer parseTreeBuffer = new StringBuffer();
 
     /* The symbol table. */
     private SymbolTable symbolTable;
 
 	/* Buffer for error messages. */
 	private StringBuffer errorBuffer = new StringBuffer();
 
 
 	/* Scanner to fecth tokens from. */
 	private Scanner scanner;
 
 	/* The current token that's being processed. */
 	private Token currentToken;
 
 // Parser ==========================================
 
 	/**
 	 * Construct a new parser using a specified scanner.
 	 * @param scanner The scanner to use.
 	 */
 	public Parser(Scanner scanner) {
 		this.scanner = scanner;
 		symbolTable = new SymbolTable();
 	}
 
 	/**
 	 *	Begin the parsing.
 	 *	@return An AST command representing the parsed and abstracted program.
 	 */
 	public ast.Command parse() {
         initSymbolTable();
 		// Load first token.
 		currentToken = scanner.next();
 		try {
 			return program();
 		} catch (QuitParseException qpe) {
 			errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
 			errorBuffer.append("[Could not complete parsing.]");
 
             return new ast.Error(lineNumber(), charPosition(), "Could not complete parsing.");
 		}
 	}
 
 	/* Get current tokens line number.
 	 * @return The line number.
 	 */
 	private int lineNumber() {
 		return currentToken.lineNumber();
 	}
 
 	/* Get current tokens char position.
 	 * @return The char position.
 	 */
 	private int charPosition() {
 		return currentToken.charPosition();
 	}
 
 
 
 
 	/**
 	 * Report an error for an unexpected nonterminal.
 	 * @param nt The expected non terminal.
 	 * @return A string representing the error.
 	 * pre: The unexpected token is at currentToken.
 	 */
 	private String reportSyntaxError(NonTerminal nt) {
 		// While this original line is informative the order of the elements in the firstset is not deterministic so that makes automated testing harder.
 		//String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected one of " + nt.firstSet() + " but got " + currentToken.kind() + ".]";
 		String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";
 		errorBuffer.append(message + "\n");
 		//errorBuffer.append("lexeme = \"" + currentToken + "\"\n"); // Delete this when done. Test program probably don't expect this error report to exist.
 		//errorBuffer.append(parseTreeBuffer.toString() + '\n'); // Delete this when done. Test program probably don't expect this error report to exist.
 		return message;
 	}
 
 	/**
 	 * Report an error for an unexpected kind.
 	 * @param kind The expected kind.
 	 * @return A string representing the error.
 	 * pre: The unexpected kind is at currentToken.kind().
 	 */
 	private String reportSyntaxError(Token.Kind kind) {
 		String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
 		errorBuffer.append(message + "\n");
 		//errorBuffer.append("lexeme = \"" + currentToken + "\"\n"); // Delete this when done. Test program probably don't expect this error report to exist.
 		//errorBuffer.append(parseTreeBuffer.toString() + '\n'); // Delete this when done. Test program probably don't expect this error report to exist.
 		return message;
 	}
 
 	/**
 	 * Get the error report
 	 * @return An erro repport.
 	 */
 	public String errorReport() {
 		return errorBuffer.toString();
 	}
 
 	/**
 	 * Query for errors.
 	 * @return true if there are any error messages repported.
 	 */
 	public boolean hasError() {
 		return errorBuffer.length() > 0;
 	}
 
 	/**
 	 * Returns a string representation of the parse tree.
 	 * @return The parse tree representation.
 	 */
 	public String parseTreeReport() {
 		return parseTreeBuffer.toString();
 	}
 
 
 	/**
 	 * Build parse tree when entering a new production rule.
 	 * @param nonTerminal The non terminal just entered.
 	 */
 	public void enterRule(NonTerminal nonTerminal) {
 		String lineData = new String();
 		for (int i = 0; i < parseTreeRecursionDepth; i++) {
 			lineData += "  ";
 		}
 		lineData += nonTerminal.name();
 		//System.out.println("descending " + lineData);
 		parseTreeBuffer.append(lineData + "\n");
 		parseTreeRecursionDepth++;
 	}
 
 	/**
 	 * Build parse tree when exiting a production rule.
 	 * @param nonTerminal The non terminal just exited.
 	 */
 	private void exitRule(NonTerminal nonTerminal) {
 		parseTreeRecursionDepth--;
 	}
 
 
 	/* Examine if the current token is of a specified kind.
 	 * @param kind The kind to test for.
 	 * @return ture if the current token had the specified kind.
 	 * post: Advance stream: no.
 	 */
 	private boolean have(Token.Kind kind) {
 		return currentToken.is(kind);
 	}
 
 	/* Examine if the current token is in the first set of the given non terminal.
 	 * @param nt The non-terminal who's first set is to be checked.
 	 * @return ture if the current token is in first(nt).
 	 * post: Advance stream: no.
 	 */
 	private boolean have(NonTerminal nt)
 	{
 		return nt.firstSet().contains(currentToken.kind());
 	}
 
 	/* Examine if the current token is of a specified kind.
 	 * @param kind The kind to test for.
 	 * @return ture if the current token had the specified kind.
 	 * post: Advance stream: if token matched.
 	 */
 	private boolean accept(Token.Kind kind) {
 		if (have(kind)) {
 			currentToken = scanner.next();
 			return true;
 		}
 		return false;
 	}	 
 
 	/* Examine if the current token is in the first set of the given non terminal.
 	 * @param nt The non-terminal who's first set is to be checked.
 	 * @return ture if the current token is in first(nt).
 	 * post: Advance stream: if token matched.
 	 */
 	private boolean accept(NonTerminal nt) {
 		if (have(nt)) {
 			currentToken = scanner.next();
 			return true;
 		}
 		return false;
 	}
 
 	/* Examine if the current token is of a specified kind.
 	 * @param kind The kind to test for.
 	 * @return ture if the current token had the specified kind.
 	 * @throws QuitParseException if token did not match.
 	 * post: Advance stream: if token matched,
 	 */
 	private boolean expect(Token.Kind kind) {
 		if (accept(kind)) {
 			return true;
 		}
 		String errorMessage = reportSyntaxError(kind);
 		throw new QuitParseException(errorMessage);
 		//return false;
 	}
 
 	/* Examine if the current token is in the first set of the given non terminal.
 	 * @param nt The non-terminal who's first set is to be checked.
 	 * @return ture if the current token is in first(nt).
 	 * @throws QuitParseException if token did not match.
 	 * post: Advance stream: if token matched.
 	 */
 	private boolean expect(NonTerminal nt) {
 		if (accept(nt)) {
 			return true;
 		}
 		String errorMessage = reportSyntaxError(nt);
 		throw new QuitParseException(errorMessage);
 		//return false;
 	}
 
 
 // SymbolTable Management ==========================
 
     /**
      * Initialize the symbolTable with predefined symbols.
      */
     private void initSymbolTable() {
         for (String predefFunction : SymbolTable.PREDEF_FUNCS) {
 			symbolTable.insert(predefFunction);
         }
     }
 
     /**
      * Try to resolve a given symbol name.
      * @param ident The identifier to resolve.
      * @return A found matching symbol or ErrorSymbol.
      */
     private Symbol tryResolveSymbol(Token ident) {
         assert(ident.is(Token.Kind.IDENTIFIER));
         String name = ident.lexeme();
         try {
             return symbolTable.lookup(name);
         } catch (SymbolNotFoundError snfe) {
             String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
             return new ErrorSymbol(message);
         }
     }
 
 // Helper Methods ==========================================
     /**
      * Enters a new scobe for symbols.
      */
     private void enterScope() {
         symbolTable = symbolTable.mutate();
     }
 
     /**
      * Exit current symbol scope.
      */
     private void exitScope() {
     	symbolTable = symbolTable.getParent();
     }
 
 
     /**
      * Report a resolve error for a given symbol name.
      * @param name The errornous symbol name.
      * @param lineNum The line number where the error occured.
      * @param charPos The character position where the error occured.
      * @return An error message built from the current symbol.
      */
     private String reportResolveSymbolError(String name, int lineNum, int charPos) {
         String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
         if (Compiler.currentLab != Compiler.Lab.LAB2) {
         	errorBuffer.append(message + "\n");
         	errorBuffer.append(symbolTable.toString() + "\n");
         }
         return message;
     }
 
     /**
      * Try to declare a symbol name.
      * @param ident The identifier to declare.
      * @return The new symbol or ErrorSymbol if it already existed.
      */
     private Symbol tryDeclareSymbol(Token ident) {
         assert(ident.is(Token.Kind.IDENTIFIER));
         String name = ident.lexeme();
         try {
             return symbolTable.insert(name);
         } catch (RedeclarationError re) {
             String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
             return new ErrorSymbol(message);
         }
     }
 
     /**
      * Report a redeclaration error for a symbol name.
      * @param name The redeclared symbol name.
      * @param lineNum The line number where the error occured.
      * @param charPos The character position where the error occured.
      * @return An error message generated from the current information.
      */
     private String reportDeclareSymbolError(String name, int lineNum, int charPos) {
         String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
         if (Compiler.currentLab != Compiler.Lab.LAB2) {
         	errorBuffer.append(message + "\n");
         	errorBuffer.append(symbolTable.toString() + "\n");
         }
         return message;
     }    
 
 
     /**
      * Same as accept(Token.Kind) but the Token is returned.
      * @param kind The kind to accept.
      * @return The found token or null.
      */
     private Token acceptRetrieve(Token.Kind kind) {
         Token tok = currentToken;
         if (have(kind)) {
 			currentToken = scanner.next();
             return tok;
         } else {
         	return null;
         }
     }
 
     /**
      * Same as accept(NonTerminal) but the Token is returned.
      * @param nt The kind to accept.
      * @return The found token or null.
      */
     private Token acceptRetrieve(NonTerminal nt) {
         Token tok = currentToken;
         if (have(nt)) {
 			currentToken = scanner.next();
             return tok;
         } else {
         	return null;
         }
     }
     /**
      * Same as expect(Token.Kind) but the Token is returned.
      * @param kind The expected kind.
      * @return The found token.
      * @throws QuitParseException when the token was not found.
      */
     private Token expectRetrieve(Token.Kind kind) {
         Token tok = currentToken;
         if (accept(kind)) {
             return tok;
         }
         String errorMessage = reportSyntaxError(kind);
         throw new QuitParseException(errorMessage);
         //return ErrorToken(errorMessage);
     }
 
     /**
      * Same as expect(NonTerminal) but the Token is returned.
      * @param nt The expected kind.
      * @return The found token.
      * @throws QuitParseException when the token was not found.
      */
     private Token expectRetrieve(NonTerminal nt) {
         Token tok = currentToken;
         if (accept(nt)) {
             return tok;
         }
         String errorMessage = reportSyntaxError(nt);
         throw new QuitParseException(errorMessage);
         //return ErrorToken(errorMessage);
     }
 
     /*
      * TODO needed?
      */
     private String expectIdentifier() {
         String name = currentToken.lexeme();
         if (expect(Token.Kind.IDENTIFIER)) {
             return name;
         }
         return null;
     }
 
     /**
      * TODO might be needed in a future lab according to the TA.
      */
     private Integer expectInteger() {
         String num = currentToken.lexeme();
         if (expect(Token.Kind.INTEGER)) {
             return Integer.valueOf(num);
         } else {
         	return null;
         }
     }
 
 	/**
 	 * A unchecked exception thrown when the parser had to quit its operation.
 	 */
 	private class QuitParseException extends RuntimeException {
 		private static final long serialVersionUID = 1L;
 		public QuitParseException(String errorMessage) {
 			super(errorMessage);
 		}
 	}
 
 	//  Grammar rules ==========================
 
 	/**
 	 * Production for rule:
 	 * literal := INTEGER | FLOAT | TRUE | FALSE .
 	 */
 	public ast.Expression literal() {
 		enterRule(NonTerminal.LITERAL);

         Token tok = expectRetrieve(NonTerminal.LITERAL);
 		ast.Expression expr = Command.newLiteral(tok);

 		exitRule(NonTerminal.LITERAL);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * designator := IDENTIFIER { "[" expression0 "]" } .
 	 **/
 	public ast.Expression designator() {
 		enterRule(NonTerminal.DESIGNATOR);
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryResolveSymbol(identifier);
 		ast.Expression expr = new ast.AddressOf(identifier.lineNumber(), identifier.charPosition(), symbol);
 		while (accept(Token.Kind.OPEN_BRACKET)) {
 			ast.Command amount = (ast.Command) expression0(); //  TODO really do we needed to cast stuff? will expressions always be commands?
 			expr = new ast.Index(amount.lineNumber(), amount.charPosition(), expr, (ast.Expression) amount);
 			expect(Token.Kind.CLOSE_BRACKET);
 		}
 		exitRule(NonTerminal.DESIGNATOR);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * type := IDENTIFIER .
 	 */
 	public void type() {
 		enterRule(NonTerminal.TYPE);
 		// TODO in a future lab, check that the identifier is one of {void, bool, int, float}? should these be in the symbol table?
 		expect(Token.Kind.IDENTIFIER);
 		exitRule(NonTerminal.TYPE);
 	}
 
 	/**
 	 * Production for rule:
 	 * op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
 	 */
 	public Token op0() {
 		enterRule(NonTerminal.OP0);
 		Token token = expectRetrieve(NonTerminal.OP0);
 		exitRule(NonTerminal.OP0);
 		return token;
 	}
 
 	/**
 	 * Production for rule:
 	 * op1 := "+" | "-" | "or" .
 	 */
 	public Token op1() {
 		enterRule(NonTerminal.OP1);
 		Token token = expectRetrieve(NonTerminal.OP1);
 		exitRule(NonTerminal.OP1);
 		return token;
 	}
 
 	/**
 	 * Production for rule:
 	 * op2 := "*" | "/" | "and" .
 	 */
 	public Token op2() {
 		enterRule(NonTerminal.OP2);
 		Token token = expectRetrieve(NonTerminal.OP2);
 		exitRule(NonTerminal.OP2);
 		return token;
 	}
 
 	/**
 	 * Production for rule:
 	 * expression0 := expression1 [ op0 expression1 ] .
 	 */
 	public ast.Expression expression0() {
 		enterRule(NonTerminal.EXPRESSION0);
 		ast.Expression expr = expression1();
 		if (have(NonTerminal.OP0)) {
 			ast.Expression lhs = expr;
 			Token op = op0();
 			ast.Expression rhs = expression1();
 			expr = Command.newExpression(lhs, op, rhs);
 		}
 		exitRule(NonTerminal.EXPRESSION0);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * expression1 := expression2 { op1  expression2 } .
 	 */
 	public ast.Expression expression1() {
 		enterRule(NonTerminal.EXPRESSION1);
 		ast.Expression expr = expression2();
 		while (have(NonTerminal.OP1)) {
 			ast.Expression lhs = expr;
 			Token op = op1();
 			ast.Expression rhs = expression2();
 			expr = Command.newExpression(lhs, op, rhs);
 		}
 		exitRule(NonTerminal.EXPRESSION1);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * expression2 := expression3 { op2 expression3 } .
 	 */
 	public ast.Expression expression2() {
 		enterRule(NonTerminal.EXPRESSION2);
 		ast.Expression expr = expression3();
 		while (have(NonTerminal.OP2)) {
 			ast.Expression lhs = expr;
 			Token op = op2();
 			ast.Expression rhs = expression3();
 			expr = Command.newExpression(lhs, op, rhs);
 		}
 		exitRule(NonTerminal.EXPRESSION2);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * expression3 := "not" expression3 | "(" expression0 ")" | designator |
 	 *  call-expression | literal .
 	 */
 	public ast.Expression expression3() {
 		enterRule(NonTerminal.EXPRESSION3);
 		ast.Expression expr;
 		Token not;
 		if ((not = acceptRetrieve(Token.Kind.NOT)) != null) {
 			ast.Expression rhs = expression3();
 			expr = Command.newExpression(rhs, not, null);
 		} else if (accept(Token.Kind.OPEN_PAREN)) {
 			expr = expression0();
 			expect(Token.Kind.CLOSE_PAREN);
 		} else if (have(NonTerminal.DESIGNATOR)) {
 			int lineNumber = currentToken.lineNumber();
 			int charPos = currentToken.charPosition();
 			ast.Expression designator = designator(); // TODO do this at more places, like call-expr below (tried it but did tests failed)?
 			expr = new ast.Dereference(lineNumber, charPos, designator);
 		} else if (have(NonTerminal.CALL_EXPRESSION)) {
 			expr = call_expression();
 		} else if (have(NonTerminal.LITERAL)) {
 			expr = literal();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.EXPRESSION3));
 		}
 		exitRule(NonTerminal.EXPRESSION3);
 		return expr;
 	}
 
 	/**
 	 * Production for rule:
 	 * call-expression := "::" IDENTIFIER "(" expression-list ")" .
 	 */
 	public ast.Call call_expression() {
 		enterRule(NonTerminal.CALL_EXPRESSION);
 		Token callToken = expectRetrieve(Token.Kind.CALL);
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryResolveSymbol(identifier);
 		expect(Token.Kind.OPEN_PAREN);
 		ast.ExpressionList args = expression_list();
 		expect(Token.Kind.CLOSE_PAREN);
 		ast.Call call = new ast.Call(callToken.lineNumber(), callToken.charPosition(), symbol, args);
 		exitRule(NonTerminal.CALL_EXPRESSION);
 		return call;
 	}
 
 	/**
 	 * Production for rule:
 	 * expression-list := [ expression0 { "," expression0 } ] .
 	 */
 	public ast.ExpressionList expression_list() {
 		enterRule(NonTerminal.EXPRESSION_LIST);
 		// TODO linenum of empty list does not makes sense but the PrettyPrinter will not look at it anyways so just use the next token, TA says.
 		ast.ExpressionList exprList = new ast.ExpressionList(currentToken.lineNumber(), currentToken.charPosition());
 		if (have(NonTerminal.EXPRESSION0)) {
 			do {
 				ast.Expression expr = expression0();
 				exprList.add(expr);
 			} while (accept(Token.Kind.COMMA));
 		}
 		exitRule(NonTerminal.EXPRESSION_LIST);
 		return exprList;
 	}
 
 	/**
 	 * Production for rule:
 	 * parameter := IDENTIFIER ":" type .
 	 */
 	public Symbol paramter() {
 		enterRule(NonTerminal.PARAMETER);
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryDeclareSymbol(identifier);
 		expect(Token.Kind.COLON);
 		type();
 		exitRule(NonTerminal.PARAMETER);
 		return symbol;
 	}
 
 	/**
 	 * Production for rule:
 	 * parameter-list := [ parameter { "," parameter } ] .
 	 */
 	public List<Symbol> parameter_list() {
 		enterRule(NonTerminal.PARAMETER_LIST);
 		List<Symbol> list = new LinkedList<Symbol>();
 		if (have(NonTerminal.PARAMETER)) {
 			do {
 				Symbol symbol = paramter();
 				list.add(symbol);
 			} while (accept(Token.Kind.COMMA));
 		}
 		exitRule(NonTerminal.PARAMETER_LIST);
 		return list;
 	}
 
 	/**
 	 * Production for rule:
 	 * variable-declaration := "var" IDENTIFIER ":" type ";" .
 	 * 
 	 */
 	public ast.VariableDeclaration variable_declaration() {
 		enterRule(NonTerminal.VARIABLE_DECLARATION);
 		Token var = expectRetrieve(Token.Kind.VAR); // TODO can we avoid saving this? TA: will be changed in a code update maybe?
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryDeclareSymbol(identifier);
 		ast.VariableDeclaration varDecl = new ast.VariableDeclaration(var.lineNumber(), var.charPosition(), symbol);
 		expect(Token.Kind.COLON);
 		type();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.VARIABLE_DECLARATION);
 		return varDecl;
 	}
 
 	/**
 	 * Production for rule:
 	 * array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
 	 */
 	public ast.ArrayDeclaration array_declaration() {
 		enterRule(NonTerminal.ARRAY_DECLARATION);
 		Token array = expectRetrieve(Token.Kind.ARRAY);
		//expect(Token.Kind.IDENTIFIER);
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryDeclareSymbol(identifier);
 		ast.ArrayDeclaration arrayDecl = new ast.ArrayDeclaration(array.lineNumber(), array.charPosition(), symbol);
 		expect(Token.Kind.COLON);
 		type();
 		expect(Token.Kind.OPEN_BRACKET);
 		expect(Token.Kind.INTEGER);
 		expect(Token.Kind.CLOSE_BRACKET);
 		while (accept(Token.Kind.OPEN_BRACKET)) {
 			expect(Token.Kind.INTEGER);
 			expect(Token.Kind.CLOSE_BRACKET);
 		}
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.ARRAY_DECLARATION);
 		return arrayDecl;
 	}
 
 	/**
 	 * Production for rule:
 	 * function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
 	 */
 	public ast.FunctionDefinition function_definition() {
 		enterRule(NonTerminal.FUNCTION_DEFINITION);
 		Token func = expectRetrieve(Token.Kind.FUNC);
 		Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
 		Symbol symbol = tryDeclareSymbol(identifier);
 		expect(Token.Kind.OPEN_PAREN);
 		enterScope();
 		List<Symbol> args = parameter_list();
 		expect(Token.Kind.CLOSE_PAREN);
 		expect(Token.Kind.COLON);
 		type();
 		ast.StatementList body = statement_block();
 		ast.FunctionDefinition funcDecl = new ast.FunctionDefinition(func.lineNumber(), func.charPosition(), symbol, args, body);
 		exitScope();
 		exitRule(NonTerminal.FUNCTION_DEFINITION);
 		return funcDecl;
 	}
 
 	/**
 	 * Production for rule:
 	 * declaration := variable-declaration | array-declaration | function-definition .
 	 */
 	public ast.Declaration declaration() {
 		enterRule(NonTerminal.DECLARATION);
 		ast.Declaration declaration;
 		if (have(NonTerminal.VARIABLE_DECLARATION)) {
 			declaration = variable_declaration();
 		} else if (have(NonTerminal.ARRAY_DECLARATION)) {
 			declaration = array_declaration();
 		} else if (have(NonTerminal.FUNCTION_DEFINITION)) {
 			declaration = function_definition();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.DECLARATION));
 		}
 		exitRule(NonTerminal.DECLARATION);
 		return declaration;
 	}
 
 	/**
 	 * Production for rule:
 	 * declaration-list := { declaration } .
 	 */
 	public ast.DeclarationList declaration_list() {
 		ast.DeclarationList declarationList = new ast.DeclarationList(currentToken.lineNumber(), currentToken.charPosition());
 		enterRule(NonTerminal.DECLARATION_LIST);
 		while (have(NonTerminal.DECLARATION)) {
 			ast.Declaration declaration = declaration();
 			declarationList.add(declaration);
 		}
 		exitRule(NonTerminal.DECLARATION_LIST);
 		return declarationList;
 	}
 

 	/**
 	 * Production for rule:
 	 * assignment-statement := "let" designator "=" expression0 ";" .
 	 */
 	public ast.Assignment assignment_statement() {
 		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
 		Token let = expectRetrieve(Token.Kind.LET);
 		ast.Expression destination = designator();
 		expect(Token.Kind.ASSIGN);
 		ast.Expression source = expression0();
 		expect(Token.Kind.SEMICOLON);
 		ast.Assignment assignment = new ast.Assignment(let.lineNumber(), let.charPosition(), destination, source);
 		exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
 		return assignment;
 	}
 
 	/**
 	 * Production for rule:
 	 * call-statement := call-expression ";" .
 	 */
 	public ast.Call call_statement() {
 		enterRule(NonTerminal.CALL_STATEMENT);
 		ast.Call call = call_expression();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.CALL_STATEMENT);
 		return call;
 	}
 
 	/**
 	 * Production for rule:
 	 * if-statement := "if" expression0 statement-block [ "else" statement-block ] .
 	 */
 	public ast.IfElseBranch if_statement() {
 		enterRule(NonTerminal.IF_STATEMENT);
 		Token ifToken = expectRetrieve(Token.Kind.IF);
 		ast.Expression condition = expression0();
 		enterScope();
 		ast.StatementList thenBlock = statement_block();
 		exitScope();
 		ast.StatementList elseBlock;
 		if (accept(Token.Kind.ELSE)) {
 			enterScope();
 			elseBlock = statement_block();
 			exitScope();
 		} else { // TODO what is cleanest?: eles branch or alwya enter statement_block who retunes an empty list?
 			elseBlock = new ast.StatementList(currentToken.lineNumber(), currentToken.charPosition());
 		}

 		exitRule(NonTerminal.IF_STATEMENT);
 		ast.IfElseBranch ifElseBranch = new ast.IfElseBranch(ifToken.lineNumber(), ifToken.charPosition(), condition, thenBlock, elseBlock);
 		return ifElseBranch;
 	}
 
 	/**
 	 * Production for rule:
 	 * while-statement := "while" expression0 statement-block .
 	 */
 	public ast.WhileLoop while_statement() {
 		enterRule(NonTerminal.WHILE_STATEMENT);
 		Token whileToken = expectRetrieve(Token.Kind.WHILE);
 		ast.Expression condition = expression0();
 		enterScope();
 		ast.StatementList body = statement_block();
 		exitScope();
 		ast.WhileLoop whileLoop = new ast.WhileLoop(whileToken.lineNumber(), whileToken.charPosition(), condition, body);
 		exitRule(NonTerminal.WHILE_STATEMENT);
 		return whileLoop;
 	}
 
 	/**
 	 * Production for rule:
 	 * return-statement := "return" expression0 ";" .
 	 */
 	public ast.Return return_statement() {
 		enterRule(NonTerminal.RETURN_STATEMENT);
 		Token returnToken = expectRetrieve(Token.Kind.RETURN);
 		ast.Expression arg = expression0();
 		expect(Token.Kind.SEMICOLON);
 		ast.Return returnStmt = new ast.Return(returnToken.lineNumber(), returnToken.charPosition(), arg);
 		exitRule(NonTerminal.RETURN_STATEMENT);
 		return returnStmt;
 	}
 
 	/**
 	 * Production for rule:
 	 * statement := variable-declaration | call-statement | assignment-statement 
 	 * | if-statement | while-statement | return-statement .
 	 */
 	public ast.Statement statement() {
 		enterRule(NonTerminal.STATEMENT);
 		ast.Statement stmt;
 		if (have(NonTerminal.VARIABLE_DECLARATION)) {
 			stmt = variable_declaration();
 		} else if (have(NonTerminal.CALL_STATEMENT)) {
 			stmt = call_statement();
 		} else if (have(NonTerminal.ASSIGNMENT_STATEMENT)) {
 			stmt = assignment_statement();
 		} else if (have(NonTerminal.IF_STATEMENT)) {
 			stmt = if_statement();
 		} else if (have(NonTerminal.WHILE_STATEMENT)) {
 			stmt = while_statement();
 		} else if (have(NonTerminal.RETURN_STATEMENT)) {
 			stmt = return_statement();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.STATEMENT));
 		}
 		exitRule(NonTerminal.STATEMENT);
 		return stmt;
 	}
 
 	/**
 	 * Production for rule:
 	 * statement-list := { statement } .
 	 */
 	public ast.StatementList statement_list() {
 		enterRule(NonTerminal.STATEMENT_LIST);
 		ast.StatementList stmtList = new ast.StatementList(currentToken.lineNumber(), currentToken.charPosition());
 		while (have(NonTerminal.STATEMENT)) {
 			ast.Statement stmt = statement();
 			stmtList.add(stmt);
 		}
 		exitRule(NonTerminal.STATEMENT_LIST);
 		return stmtList;
 	}
 
 	/**
 	 * Production for rule:
 	 * statement-block := "{" statement-list "}" .
 	 */
 	public ast.StatementList statement_block() {
 		enterRule(NonTerminal.STATEMENT_BLOCK);
 		expect(Token.Kind.OPEN_BRACE);
 		ast.StatementList stmtList = statement_list();
 		expect(Token.Kind.CLOSE_BRACE);
 		exitRule(NonTerminal.STATEMENT_BLOCK);
 		return stmtList;
 	}
 
 	/**
 	 * Production for rule:
 	 * program := declaration-list EOF .
 	 */
 	public ast.DeclarationList program() {
 		enterRule(NonTerminal.PROGRAM);
 		ast.DeclarationList declarationList = declaration_list();
 		expect(Token.Kind.EOF);
 		exitRule(NonTerminal.PROGRAM);
 		return declarationList;
 	}
 }
