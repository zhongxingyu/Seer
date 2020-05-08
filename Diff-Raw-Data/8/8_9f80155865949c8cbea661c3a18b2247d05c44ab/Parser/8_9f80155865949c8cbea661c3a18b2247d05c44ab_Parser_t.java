 //
//	parser skeleton, CS 480, Winter 2011
 //	written by Tim Budd
//		modified by:	Ellen Porter
//						Kiel Friedt
//						Erik Hortsch
//						Russell Haering
 //
 
 public class Parser {
 	private final Lexer lex;
 	private final boolean debug;
 
 	public Parser(Lexer l, boolean d) {
 		lex = l;
 		debug = d;
 	}
 
 	public void parse() throws ParseException {
 		lex.nextLex();
 		program();
 		if (lex.tokenCategory() != Lexer.endOfInput)
 			parseError(3); // expecting end of file
 	}
 
 	private final void start(String n) {
 		if (debug)
 			System.out.println("start " + n + " token: " + lex.tokenText());
 	}
 
 	private final void stop(String n) {
 		if (debug)
 			System.out
 			.println("recognized " + n + " token: " + lex.tokenText());
 	}
 
 	private void parseError(int number) throws ParseException {
 		throw new ParseException(number);
 	}
 
 	private void program() throws ParseException {
 		start("program");
 		while (lex.tokenCategory() != Lexer.endOfInput) {
 			declaration();
 			eatOrErr(";", 18);
 		}
 		stop("program");
 	}
 
 	private void declaration() throws ParseException {
 		start("declaration");
 		if (isClassDeclaration())
 			classDeclaration();
 		else if (isNonClassDeclaration())
 			nonClassDeclaration();
 		else
 			throw new ParseException(26);
 		stop("declaration");
 	}
 
 	private void nonClassDeclaration() throws ParseException {
 		start("nonClassDeclaration");
 		if (isFunctionDeclaration())
 			functionDeclaration();
 		else if (isNonFunctionDeclaration())
 			nonFunctionDeclaration();
 		else
 			throw new ParseException(26);
 		stop("nonClassDeclaration");
 	}
 
 	private void nonFunctionDeclaration() throws ParseException {
 		start("nonFunctionDeclaration");
 		if (isVariableDeclaration())
 			variableDeclaration();
 		else if (isConstantDeclaration())
 			constantDeclaration();
 		else if (isTypeDeclaration())
 			typeDeclaration();
 		else
 			throw new ParseException(26);
 		stop("nonFunctionDeclaration");
 	}
 
 	private void constantDeclaration() throws ParseException {
 		start("constant");
 		eatOrErr("const", 32);
 		eatOrErr(lex.isIdentifier(), 27);
 		eatOrErr("=", 20);
 		eatOrErr(isConstant(), 20);
 		stop("constant");
 	}
 
 	private void typeDeclaration() throws ParseException {
 		start("typeDeclaration");
 		eatOrErr("type", 14);
 		nameDeclaration();
 		stop("typeDeclaration");
 	}
 
 	private void variableDeclaration() throws ParseException {
 		start("variableDeclaration");
 		eatOrErr("var", 15);
 		nameDeclaration();
 		stop("variableDeclaration");
 	}
 
 	private void nameDeclaration() throws ParseException {
 		start("nameDeclaration");
 		eatOrErr(lex.isIdentifier(), 32);
 		eatOrErr(":", 19);
 		type();
 		stop("nameDeclaration");
 	}
 
 	private void classDeclaration() throws ParseException {
 		start("classDeclaration");
 		eatOrErr("class", 5);
 		eatOrErr(lex.isIdentifier(), 27);
 		classBody();
 		stop("classDeclaration");
 	}
 
 	private void classBody() throws ParseException {
 		start("classBody");
 		eatOrErr("begin", 4);
 		while (!lex.match("end")) {
 			nonClassDeclaration();
 			eatOrErr(";", 18);
 		}
 		eatOrErr("end", 8);
 		stop("classBody");
 	}
 
 	private void functionDeclaration() throws ParseException {
 		start("functionDeclaration");
 		eatOrErr("function", 10);
 		eatOrErr(lex.isIdentifier(), 27);
 		arguments();
 		returnType();
 		functionBody();
 		stop("functionDeclaration");
 	}
 
 	private void arguments() throws ParseException {
 		start("arguments");
 		eatOrErr("(", 21);
 		argumentList();
 		eatOrErr(")", 22);
 		stop("arguments");
 	}
 
 	private void argumentList() throws ParseException {
 		start("argumentList");
 		if (!lex.match(")")) {
 			nameDeclaration();
 			while (lex.match(",")) {
 				lex.nextLex();
 				nameDeclaration();
 			}
 		}
 		stop("argumentList");
 	}
 
 	private void returnType() throws ParseException {
 		start("returnType");
 		if (lex.match(":")) {
 			lex.nextLex();
 			type();
 		}
 		stop("returnType");
 	}
 
 	private void type() throws ParseException {
 		start("type");
 
 		if (hungryMatch("[")) {
 			eatOrErr((lex.tokenCategory() == 3), 32);
 			eatOrErr(":", 19);
 			eatOrErr((lex.tokenCategory() == 3), 32);
 			eatOrErr("]", 24);
 			type();
 		} else if (hungryMatch("^"))
 			type();
 		else if (!hungryMatch(lex.isIdentifier()))
 			throw new ParseException(30);
 
 		stop("type");
 	}
 
 	private void functionBody() throws ParseException {
 		start("functionBody");
 		while (!lex.match("begin")) {
 			nonClassDeclaration();
 			eatOrErr(";", 18);
 		}
 		compoundStatement();
 		stop("functionBody");
 	}
 
 	private void compoundStatement() throws ParseException {
 		start("compoundStatement");
 		eatOrErr("begin", 4);
 		while (!lex.match("end")) {
 			statement();
 			eatOrErr(";", 18);
 		}
 		eatOrErr("end", 8);
 		stop("compoundStatement");
 	}
 
 	private void statement() throws ParseException {
 		start("statement");
 		if (isReturnStatement())
 			returnStatement();
 		else if (isIfStatement())
 			ifStatement();
 		else if (isWhileStatement())
 			whileStatement();
 		else if (isCompoundStatement())
 			compoundStatement();
 		else if (isAssignOrFunction())
 			assignOrFunction();
 		else
 			throw new ParseException(34);
 		stop("statement");
 	}
 
 	private void returnStatement() throws ParseException {
 		start("returnStatement");
 		eatOrErr("return", 12);
 		if (hungryMatch("(")) {
 			expression();
 			eatOrErr(")", 22);
 		}
 		stop("returnStatement");
 	}
 
 	private void ifStatement() throws ParseException {
 		start("ifStatement");
 		eatOrErr("if", 11);
 		expression();
 		eatOrErr("then", 13);
 		statement();
 		if (hungryMatch("else"))
 			statement();
 		stop("ifStatement");
 	}
 
 	private void whileStatement() throws ParseException {
 		start("whileStatement");
 		eatOrErr("while", 16);
 		expression();
 		eatOrErr("do", 7);
 		statement();
 		stop("whileStatement");
 	}
 
 	private boolean assignOrFunction() throws ParseException {
 		start("assignOrFunction");
 		reference();
 		if (hungryMatch("="))
 			expression();
 		else if (hungryMatch("(")) {
 			parameterList();
 			eatOrErr(")", 22);
 		} else
 			throw new ParseException(37);
 		stop("assignOrFunction");
 		return true;
 	}
 
 	private boolean parameterList() throws ParseException {
 		start("parameterList");
 		if (isExpression())
 			expression();
 		while (hungryMatch(",")) {
 			if (!isExpression())
 				throw new ParseException(22);
 			expression();
 		}
 		stop("parameterList");
 		return true;
 	}
 
 	private void expression() throws ParseException {
 		start("expression");
 		do
 			relExpression();
 		while (hungryMatch(isLogicalOperator()));
 		stop("expression");
 	}
 
 	private void relExpression() throws ParseException {
 		start("relExpression");
 		do
 			plusExpression();
 		while (hungryMatch(isRelationalOperator()));
 		stop("relExpression");
 	}
 
 	private void plusExpression() throws ParseException {
 		start("plusExpression");
 		do
 			timesExpression();
 		while (hungryMatch(isAdditionOperator()));
 		stop("plusExpression");
 	}
 
 	private void timesExpression() throws ParseException {
 		start("timesExpression");
 		term();
 		while (hungryMatch(isMultiplicationOperator()))
 			term();
 		stop("timesExpression");
 	}
 
 	private void term() throws ParseException {
 		start("term");
 		if (hungryMatch("(")) {
 			expression();
 			eatOrErr(")", 22);
 		} else if (hungryMatch("not") || hungryMatch("-"))
 			lex.nextLex();
 		else if (hungryMatch("new"))
 			lex.nextLex();
 		else if (isReference()) {
 			reference();
 			if (hungryMatch("(")) {
 				parameterList();
 				eatOrErr(")", 22);
 			}
 		} else if (hungryMatch("&"))
 			reference();
 		else if (!hungryMatch(isConstant()))
 			throw new ParseException(33);
 		stop("term");
 	}
 
 	private boolean reference() throws ParseException {
 		start("reference");
 		eatOrErr(lex.isIdentifier(), 27);
 		while (lex.match("^") || lex.match(".") || lex.match("[")) {
 			if (hungryMatch("."))
 				eatOrErr(lex.isIdentifier(), 27);
 			else if (hungryMatch("[")) {
 				expression();
 				eatOrErr("]", 24);
 			} else
 				hungryMatch("^");
 		}
 		stop("reference");
 		return true;
 	}
 
 	private boolean isClassDeclaration() {
 		return (lex.match("class"));
 	}
 
 	private boolean isNonClassDeclaration() {
 		return (isFunctionDeclaration() || isNonFunctionDeclaration());
 	}
 
 	private boolean isFunctionDeclaration() {
 		return (lex.match("function"));
 	}
 
 	private boolean isNonFunctionDeclaration() {
 		return (lex.match("var") || lex.match("const") || lex.match("type"));
 	}
 
 	private boolean isConstantDeclaration() {
 		return (lex.match("const"));
 	}
 
 	private boolean isTypeDeclaration() {
 		return (lex.match("type"));
 	}
 
 	private boolean isVariableDeclaration() {
 		return (lex.match("var"));
 	}
 
 	private boolean isReturnStatement() {
 		return (lex.match("return"));
 	}
 
 	private boolean isIfStatement() {
 		return (lex.match("if"));
 	}
 
 	private boolean isWhileStatement() {
 		return (lex.match("while"));
 	}
 
 	private boolean isCompoundStatement() {
 		return (lex.match("begin"));
 	}
 
 	private boolean isAssignOrFunction() {
 		return (lex.isIdentifier());
 	}
 
 	private boolean isExpression() {
 		return (lex.match("(") || lex.match("not") || lex.match("new")
 				|| lex.match("-") || lex.match("&") || isReference() || isConstant());
 	}
 
 	private boolean isReference() {
 		return (lex.isIdentifier());
 	}
 
 	private boolean isConstant() {
 		return (lex.tokenCategory() == 3 || lex.tokenCategory() == 4 || lex
 				.tokenCategory() == 5);
 	}
 
 	private boolean isLogicalOperator() {
 		return (lex.match("and") || lex.match("or"));
 	}
 
 	private boolean isRelationalOperator() {
 		return (lex.match("<") || lex.match("<=") || lex.match("!=")
 				|| lex.match("==") || lex.match(">=") || lex.match(">"));
 	}
 
 	private boolean isAdditionOperator() {
 		return (lex.match("+") || lex.match("-") || lex.match("<<"));
 	}
 
 	private boolean isMultiplicationOperator() {
 		return (lex.match("*") || lex.match("/") || lex.match("%"));
 	}
 
 	private void eatOrErr(String match, int errno) throws ParseException {
 		if (!lex.match(match))
 			throw new ParseException(errno);
 		lex.nextLex();
 	}
 
 	private void eatOrErr(boolean cond, int errno) throws ParseException {
 		if (!cond)
 			throw new ParseException(errno);
 		lex.nextLex();
 	}
 
 	private boolean hungryMatch(String match) throws ParseException {
 		if (lex.match(match)) {
 			lex.nextLex();
 			return true;
 		} else
 			return false;
 	}
 
 	private boolean hungryMatch(boolean match) throws ParseException {
 		if (match)
 			lex.nextLex();
 		return match;
 	}
 }
