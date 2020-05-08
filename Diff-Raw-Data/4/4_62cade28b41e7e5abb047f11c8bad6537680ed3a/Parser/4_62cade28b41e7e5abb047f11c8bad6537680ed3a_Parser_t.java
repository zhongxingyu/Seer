 package crux;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
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
 
 	/**
 	 * Returns a string representation of the parse tree.
 	 * @return The parse tree representation.
 	 */
 	public String parseTreeReport() {
 		return parseTreeBuffer.toString();
 	}
 
 	/* Buffer for error messages. */
 	private StringBuffer errorBuffer = new StringBuffer();
 
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
 	 * A unchecked exception thrown when the parser had to quit its operation.
 	 */
 	private class QuitParseException extends RuntimeException {
 		private static final long serialVersionUID = 1L;
 		public QuitParseException(String errorMessage) {
 			super(errorMessage);
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
 
 
 
 	/* Scanner to fecth tokens from. */
 	private Scanner scanner;
 
 	/* The current token that's being processed. */
 	private Token currentToken;
 
 	/**
 	 * Construct a new parser using a specified scanner.
 	 * @param scanner The scanner to use.
 	 */
 	public Parser(Scanner scanner) {
 		this.scanner = scanner;
 	}
 
 	/**
 	 *	Begin the parsing.
 	 */
 	public void parse() {
 		// Load first token.
 		currentToken = scanner.next();
 		try {
 			program();
 		} catch (QuitParseException qpe) {
 			errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
 			errorBuffer.append("[Could not complete parsing.]");
 		}
 	}
 
 
 	// =Helper functions=
 
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
 
 
 	// Grammar Rules
 
 	/**
 	 * Production for rule:
 	 * literal := INTEGER | FLOAT | TRUE | FALSE .
 	 */
 	public void literal() {
 		enterRule(NonTerminal.LITERAL);
 		if (have(Token.Kind.INTEGER)) {
 			expect(Token.Kind.INTEGER);
 		} else if (have(Token.Kind.FLOAT)) {
 			expect(Token.Kind.FLOAT);
 		} else if (have(Token.Kind.TRUE)) {
 			expect(Token.Kind.TRUE);
 		} else if (have(Token.Kind.FALSE)) {
 			expect(Token.Kind.FALSE);
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.LITERAL));
 		}
 		exitRule(NonTerminal.LITERAL);
 	}
 
 	/**
 	 * Production for rule:
 	 * designator := IDENTIFIER { "[" expression0 "]" } .
 	 **/
 	public void designator() {
 		enterRule(NonTerminal.DESIGNATOR);
 		expect(Token.Kind.IDENTIFIER);
 		while (accept(Token.Kind.OPEN_BRACKET)) {
 			expression0();
 			expect(Token.Kind.CLOSE_BRACKET);
 		}
 		exitRule(NonTerminal.DESIGNATOR);
 	}
 
 	/**
 	 * Production for rule:
 	 * type := IDENTIFIER .
 	 */
 	public void type() {
 		enterRule(NonTerminal.TYPE);
 		expect(Token.Kind.IDENTIFIER);
 		exitRule(NonTerminal.TYPE);
 	}
 
 	/**
 	 * Production for rule:
 	 * op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
 	 */
 	public void op0() {
 		enterRule(NonTerminal.OP0);
 		if (have(Token.Kind.GREATER_EQUAL)) {
 			expect(Token.Kind.GREATER_EQUAL);
 		} else if (have(Token.Kind.LESSER_EQUAL)) {
 			expect(Token.Kind.LESSER_EQUAL);
 		} else if (have(Token.Kind.NOT_EQUAL)) {
 			expect(Token.Kind.NOT_EQUAL);
 		} else if (have(Token.Kind.EQUAL)) {
 			expect(Token.Kind.EQUAL);
 		} else if (have(Token.Kind.GREATER_THAN)) {
 			expect(Token.Kind.GREATER_THAN);
 		} else if (have(Token.Kind.LESS_THAN)) {
 			expect(Token.Kind.LESS_THAN);
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.OP0));
 		}
 		exitRule(NonTerminal.OP0);
 	}
 
 	/**
 	 * Production for rule:
 	 * op1 := "+" | "-" | "or" .
 	 */
 	public void op1() {
 		enterRule(NonTerminal.OP1);
 		if (have(Token.Kind.ADD)) {
 			expect(Token.Kind.ADD);
 		} else if (have(Token.Kind.SUB)) {
 			expect(Token.Kind.SUB);
 		} else if (have(Token.Kind.OR)) {
 			expect(Token.Kind.OR);
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.OP1));
 		}
 		exitRule(NonTerminal.OP1);
 	}
 
 	/**
 	 * Production for rule:
 	 * op2 := "*" | "/" | "and" .
 	 */
 	public void op2() {
 		enterRule(NonTerminal.OP2);
 		if (have(Token.Kind.MUL)) {
 			expect(Token.Kind.MUL);
 		} else if (have(Token.Kind.DIV)) {
 			expect(Token.Kind.DIV);
 		} else if (have(Token.Kind.AND)) {
 			expect(Token.Kind.AND);
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.OP2));
 		}
 		exitRule(NonTerminal.OP2);
 	}
 
 	/**
 	 * Production for rule:
 	 * expression0 := expression1 [ op0 expression1 ] .
 	 */
 	public void expression0() {
 		enterRule(NonTerminal.EXPRESSION0);
 		expression1();
 		while (have(NonTerminal.OP0)) {
 			op0();
 			expression1();
 		}
 		exitRule(NonTerminal.EXPRESSION0);
 	}
 
 	/**
 	 * Production for rule:
 	 * expression1 := expression2 { op1  expression2 } .
 	 */
 	public void expression1() {
 		enterRule(NonTerminal.EXPRESSION1);
 		expression2();
 		while (have(NonTerminal.OP1)) {
 			op1();
 			expression2();
 		}
 		exitRule(NonTerminal.EXPRESSION1);
 	}
 
 	/**
 	 * Production for rule:
 	 * expression2 := expression3 { op2 expression3 } .
 	 */
 	public void expression2() {
 		enterRule(NonTerminal.EXPRESSION2);
 		expression3();
 		while (have(NonTerminal.OP2)) {
 			op2();
 			expression3();
 		}
 		exitRule(NonTerminal.EXPRESSION2);
 	}
 
 	/**
 	 * Production for rule:
 	 * expression3 := "not" expression3 | "(" expression0 ")" | designator |
 	 *  call-expression | literal .
 	 */
 	public void expression3() {
 		enterRule(NonTerminal.EXPRESSION3);
 		if (accept(Token.Kind.NOT)) {
 			expression3();
 		} else if (accept(Token.Kind.OPEN_PAREN)) {
 			expression0();
 			expect(Token.Kind.CLOSE_PAREN);
 		} else if (have(NonTerminal.DESIGNATOR)) {
 			designator();
 		} else if (have(NonTerminal.CALL_EXPRESSION)) {
 			call_expression();
 		} else if (have(NonTerminal.LITERAL)) {
 			literal();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.EXPRESSION3));
 		}
 		exitRule(NonTerminal.EXPRESSION3);
 	}
 
 	/**
 	 * Production for rule:
 	 * call-expression := "::" IDENTIFIER "(" expression-list ")" .
 	 */
 	public void call_expression() {
 		enterRule(NonTerminal.CALL_EXPRESSION);
 		expect(Token.Kind.CALL);
 		expect(Token.Kind.IDENTIFIER);
 		expect(Token.Kind.OPEN_PAREN);
 		expression_list();
 		expect(Token.Kind.CLOSE_PAREN);
 		exitRule(NonTerminal.CALL_EXPRESSION);
 	}
 
 	/**
 	 * Production for rule:
 	 * expression-list := [ expression0 { "," expression0 } ] .
 	 */
 	public void expression_list() {
 		enterRule(NonTerminal.EXPRESSION_LIST);
 		if (have(NonTerminal.EXPRESSION0)) {
 			 do {
 				expression0();
 			} while (accept(Token.Kind.COMMA));
 		}
 		exitRule(NonTerminal.EXPRESSION_LIST);
 	}
 
 	/**
 	 * Production for rule:
 	 * parameter := IDENTIFIER ":" type .
 	 */
 	public void paramter() {
 		enterRule(NonTerminal.PARAMETER);
 		expect(Token.Kind.IDENTIFIER);
 		expect(Token.Kind.COLON);
 		type();
 		exitRule(NonTerminal.PARAMETER);
 	}
 
 	/**
 	 * Production for rule:
 	 * parameter-list := [ parameter { "," parameter } ] .
 	 */
 	public void parameter_list() {
 		enterRule(NonTerminal.PARAMETER_LIST);
 		if (have(NonTerminal.PARAMETER)) {
 			do {
 				paramter();
 			} while (accept(Token.Kind.COMMA));
 		}
 		exitRule(NonTerminal.PARAMETER_LIST);
 	}
 
 	/**
 	 * Production for rule:
 	 * variable-declaration := "var" IDENTIFIER ":" type ";" .
 	 * 
 	 */
 	public void variable_declaration() {
 		enterRule(NonTerminal.VARIABLE_DECLARATION);
 		expect(Token.Kind.VAR);
 		expect(Token.Kind.IDENTIFIER);
 		expect(Token.Kind.COLON);
 		type();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.VARIABLE_DECLARATION);
 	}
 
 	/**
 	 * Production for rule:
 	 * array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
 	 */
 	public void array_declaration() {
 		enterRule(NonTerminal.ARRAY_DECLARATION);
 		expect(Token.Kind.ARRAY);
 		expect(Token.Kind.IDENTIFIER);
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
 	}
 
 	/**
 	 * Production for rule:
 	 * function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
 	 */
 	public void function_definition() {
 		enterRule(NonTerminal.FUNCTION_DEFINITION);
 		expect(Token.Kind.FUNC);
 		expect(Token.Kind.IDENTIFIER);
 		expect(Token.Kind.OPEN_PAREN);
 		parameter_list();
 		expect(Token.Kind.CLOSE_PAREN);
 		expect(Token.Kind.COLON);
 		type();
 		statement_block();
 		exitRule(NonTerminal.FUNCTION_DEFINITION);
 	}
 
 	/**
 	 * Production for rule:
 	 * declaration := variable-declaration | array-declaration | function-definition .
 	 */
 	public void declaration() {
 		enterRule(NonTerminal.DECLARATION);
 		if (have(NonTerminal.VARIABLE_DECLARATION)) {
 			variable_declaration();
 		} else if (have(NonTerminal.ARRAY_DECLARATION)) {
 			array_declaration();
 		} else if (have(NonTerminal.FUNCTION_DEFINITION)) {
 			function_definition();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.DECLARATION));
 		}
 		exitRule(NonTerminal.DECLARATION);
 	}
 
 	/**
 	 * Production for rule:
 	 * declaration-list := { declaration } .
 	 */
 	public void declaration_list() {
 		enterRule(NonTerminal.DECLARATION_LIST);
 		while (have(NonTerminal.DECLARATION)) {
 			declaration();
 		}
 		exitRule(NonTerminal.DECLARATION_LIST);
 	}
 
 
 	/**
 	 * Production for rule:
 	 * assignment-statement := "let" designator "=" expression0 ";" .
 	 */
 	public void assignment_statement() {
 		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
 		expect(Token.Kind.LET);
 		designator();
 		expect(Token.Kind.ASSIGN);
 		expression0();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
 	}
 	/**
 	 * Production for rule:
 	 * call-statement := call-expression ";" .
 	 */
 	public void call_statement() {
 		enterRule(NonTerminal.CALL_STATEMENT);
 		call_expression();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.CALL_STATEMENT);
 	}
 
 	/**
 	 * Production for rule:
 	 * if-statement := "if" expression0 statement-block [ "else" statement-block ] .
 	 */
 	public void if_statement() {
 		enterRule(NonTerminal.IF_STATEMENT);
 		expect(Token.Kind.IF);
 		expression0();
 		statement_block();
 		if (accept(Token.Kind.ELSE)) {
 			statement_block();
 		}
 		exitRule(NonTerminal.IF_STATEMENT);
 	}
 
 	/**
 	 * Production for rule:
 	 * while-statement := "while" expression0 statement-block .
 	 */
 	public void while_statement() {
 		enterRule(NonTerminal.WHILE_STATEMENT);
 		expect(Token.Kind.WHILE);
 		expression0();
 		statement_block();
 		exitRule(NonTerminal.WHILE_STATEMENT);
 	}
 
 	/**
 	 * Production for rule:
 	 * return-statement := "return" expression0 ";" .
 	 */
 	public void return_statement() {
 		enterRule(NonTerminal.RETURN_STATEMENT);
 		expect(Token.Kind.RETURN);
 		expression0();
 		expect(Token.Kind.SEMICOLON);
 		exitRule(NonTerminal.RETURN_STATEMENT);
 	}
 
 	/**
 	 * Production for rule:
 	 * statement := variable-declaration | call-statement | assignment-statement 
 	 * | if-statement | while-statement | return-statement .
 	 */
 	public void statement() {
 		enterRule(NonTerminal.STATEMENT);
 		if (have(NonTerminal.VARIABLE_DECLARATION)) {
 			variable_declaration();
 		} else if (have(NonTerminal.CALL_STATEMENT)) {
 			call_statement();
 		} else if (have(NonTerminal.ASSIGNMENT_STATEMENT)) {
 			assignment_statement();
 		} else if (have(NonTerminal.IF_STATEMENT)) {
 			if_statement();
 		} else if (have(NonTerminal.WHILE_STATEMENT)) {
 			while_statement();
 		} else if (have(NonTerminal.RETURN_STATEMENT)) {
 			return_statement();
 		} else {
 			throw new QuitParseException(reportSyntaxError(NonTerminal.STATEMENT));
 		}
 		exitRule(NonTerminal.STATEMENT);
 	}
 
 	/**
 	 * Production for rule:
 	 * statement-list := { statement } .
 	 */
 	public void statement_list() {
 		enterRule(NonTerminal.STATEMENT_LIST);
 		while (have(NonTerminal.STATEMENT)) {
 			statement();
 		}
 		exitRule(NonTerminal.STATEMENT_LIST);
 	}
 
 	/**
 	 * Production for rule:
 	 * statement-block := "{" statement-list "}" .
 	 */
 	public void statement_block() {
 		enterRule(NonTerminal.STATEMENT_BLOCK);
 		expect(Token.Kind.OPEN_BRACE);
 		statement_list();
 		expect(Token.Kind.CLOSE_BRACE);
 		exitRule(NonTerminal.STATEMENT_BLOCK);
 	}
 
 	/**
 	 * Production for rule:
 	 * program := declaration-list EOF .
 	 */
 	public void program() {
 		enterRule(NonTerminal.PROGRAM);
 		declaration_list();
 		expect(Token.Kind.EOF);
 		exitRule(NonTerminal.PROGRAM);
 	}
 }
