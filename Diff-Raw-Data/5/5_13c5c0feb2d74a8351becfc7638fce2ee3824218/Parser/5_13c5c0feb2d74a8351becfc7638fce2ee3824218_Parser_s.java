 package edu.kit.pp.minijava;
 
 import edu.kit.pp.minijava.ast.*;
 import edu.kit.pp.minijava.tokens.*;
 import java.io.IOException;
 import java.util.HashMap;
 // TODO error() method for throwing Exception
 // TODO alle expressions durchgehen und tokens so speziell wie möglich wählen
 
 public class Parser {
 
 	public static class UnexpectedTokenException extends RuntimeException {
 		private Token _token;
 
 		public UnexpectedTokenException(Token token) {
 			_token = token;
 		}
 
 		public Token getToken() {
 			return _token;
 		}
 	}
 
 	private static class ParserFunction {
 		public static interface ParseExpressionPrefixFunction {
 			Expression parse();
 		}
 		public static interface ParseExpressionInfixFunction {
 			Expression parse(Expression left);
 		}
 		public static class ParseBinaryExpressionInfixFunction implements ParseExpressionInfixFunction {
 			private Parser _parser;
 			private String[] _operators;
 			private int _precedence;
 
 			public ParseBinaryExpressionInfixFunction(Parser parser, String[] operators, int precedence) {
 				_parser = parser;
 				_operators = operators;
 				_precedence = precedence;
 			}
 
 			@Override
 			public Expression parse(Expression left) {
 				String o = null;
 				for (String operator : _operators) {
 					if (_parser.acceptToken(operator)) {
 						o = operator;
 						break;
 					}
 				}
 				if (o == null)
 					throw new UnexpectedTokenException(_parser.getCurrentToken());
 				Token t = _parser.expectToken(o);
 				Expression right = _parser.parseExpression(_precedence + 1);
 				return new BinaryExpression(t, left, right);
 			}
 		}
 
 		public ParseExpressionPrefixFunction _parseExpressionPrefixFunction;
 		public int _precedence;
 		public ParseExpressionInfixFunction _parseExpressionInfixFunction;
 
 		ParserFunction(int p, ParseExpressionPrefixFunction f, ParseExpressionInfixFunction inf) {
 			_precedence=p;
 			_parseExpressionPrefixFunction=f;
 			_parseExpressionInfixFunction=inf;
 		}
 	}
 
 	private PeekingLexer _lexer;
 	private static final int LOOK_AHEAD_SIZE=4;
 	private HashMap<String, ParserFunction> _expressionParsers;
 
 	private void initalizeExpressionParserMap(){
 		_expressionParsers=new HashMap<String, ParserFunction>();
 
 		_expressionParsers.put("=", new ParserFunction(1, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"="}, 1)));
 		_expressionParsers.put("||", new ParserFunction(2, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"||"}, 2)));
 		_expressionParsers.put("&&", new ParserFunction(3, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"&&"}, 3)));
 		ParserFunction ef = new ParserFunction(4, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"==", "!="}, 4));
 		_expressionParsers.put("==", ef);
 		_expressionParsers.put("!=", ef);
 		ParserFunction cf = new ParserFunction(5, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"<", "<=", ">", ">="}, 5));
 		_expressionParsers.put("<", cf);
 		_expressionParsers.put("<=", cf);
 		_expressionParsers.put(">", cf);
 		_expressionParsers.put(">=", cf);
 		ParserFunction af = new ParserFunction(6, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"+", "-"}, 6));
 		_expressionParsers.put("+", af);
 		_expressionParsers.put("-", af);
 		ParserFunction mf = new ParserFunction(7, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"*", "/", "%"}, 7));
 		_expressionParsers.put("*", mf);
 		_expressionParsers.put("/", mf);
 		_expressionParsers.put("%", mf);
 	}
 
 	public Parser(Lexer lexer) throws IOException {
 		_lexer = new PeekingLexer(lexer, LOOK_AHEAD_SIZE);
 
 		initalizeExpressionParserMap();
 	}
 
 	private Token consumeToken() {
 		try {
 			return _lexer.next();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	private Token getCurrentToken() {
 		return _lexer.peek(0);
 	}
 
 	private boolean acceptToken(String s, int pos){
 		return _lexer.peek(pos).getValue().equals(s);
 	}
 
 	private boolean acceptToken(String s){
 		return acceptToken(s, 0);
 	}
 
 	private boolean acceptIntegerLiteral() {
 		return getCurrentToken() instanceof IntegerLiteral;
 	}
 
 	private boolean acceptIdentifier() {
 		return getCurrentToken() instanceof Identifier;
 	}
 
 	private boolean acceptPrimaryExpression() {
 		return acceptToken("null") || acceptToken("false") || acceptToken("true") ||
 				acceptIdentifier() || acceptIntegerLiteral() || acceptToken("this") ||
 				acceptToken("(") || acceptToken("new");
 	}
 
 	private Token expectToken(String s) throws UnexpectedTokenException {
 		if (!acceptToken(s))
 			throw new UnexpectedTokenException(getCurrentToken());
 		return consumeToken();
 	}
 
 	private IntegerLiteral expectIntegerLiteral() throws UnexpectedTokenException {
 		if (!(getCurrentToken() instanceof IntegerLiteral))
 			throw new UnexpectedTokenException(getCurrentToken());
 		return (IntegerLiteral)consumeToken();
 	}
 
 	private Identifier expectIdentifier() throws UnexpectedTokenException {
 		if (!(getCurrentToken() instanceof Identifier))
 			throw new UnexpectedTokenException(getCurrentToken());
 		return (Identifier)consumeToken();
 	}
 
 	private Token expectEOF() {
 		if (!getCurrentToken().isEof())
 			throw new UnexpectedTokenException(getCurrentToken());
 		return consumeToken();
 	}
 
 	public Node parseProgram() throws UnexpectedTokenException {
 		Program p = new Program();
 
 		while (acceptToken("class")) {
 			p.add(parseClass());
 		}
 
 		expectEOF();
 
 		return p;
 	}
 
 	private ClassDeclaration parseClass() throws UnexpectedTokenException {
 		expectToken("class");
 		Identifier name = expectIdentifier();
 		expectToken("{");
 		ClassDeclaration cd = new ClassDeclaration(name);
 		while (acceptToken("public")) {
 			cd.add(parseClassMember());
 		}
 		expectToken("}");
 		return cd;
 	}
 
 	private ClassMember parseClassMember() {
 		if (acceptToken("public")) {
 			if (acceptToken("static", 1)) {
 				return parseMainMethod();
 			}
 			else if (acceptToken(";", 3)) { // TODO how to differ between field and method?
 				return parseField();
 			}
 			else {
 				return parseMethod();
 			}
 		}
 		else {
 			throw new UnexpectedTokenException(getCurrentToken());
 		}
 	}
 
 	private Field parseField() throws UnexpectedTokenException {
 		expectToken("public");
 		Type t = parseType();
 		Identifier id = expectIdentifier();
 		expectToken(";");
 		return new Field(t, id);
 	}
 
 	private MainMethod parseMainMethod() {
 		expectToken("public");
 		expectToken("static");
 		expectToken("void");
 		Identifier name = expectIdentifier();
 		expectToken("(");
 		expectToken("String");
 		expectToken("[");
 		expectToken("]");
 		Identifier variableName = expectIdentifier();
 		expectToken(")");
 		Block b = parseBlock();
 		return new MainMethod(name, variableName, b);
 	}
 
 	private Method parseMethod() {
 		expectToken("public");
 		Type type = parseType();
 		Identifier name = expectIdentifier();
 		expectToken("(");
 		Parameters p = null;
 		if (!acceptToken(")"))
 			p = parseParameters();
 		expectToken(")");
 		Block b = parseBlock();
 		return new Method(type, name, b, p);
 	}
 
 	// TODO So richtig?
 	private Parameters parseParameters() {
 		Parameters p = new Parameters();
 		p.add(parseParameter());
 		while (acceptToken(",")) {
 			expectToken(",");
 			p.add(parseParameter());
 		}
 		return p;
 	}
 
 	private Parameter parseParameter() {
 		Type t = parseType();
 		Identifier name = expectIdentifier();
 		return new Parameter(t, name);
 	}
 
 	private Type parseType() {
 		BasicType t = parseBasicType();
 		int dimension = 0;
 		while (acceptToken("[")) {
 			expectToken("[");
 			expectToken("]");
 			dimension += 1;
 		}
 		return new Type(t, dimension);
 	}
 
 	private BasicType parseBasicType() {
 		if (acceptToken("int"))
 			return new BasicType(expectToken("int"));
 		else if (acceptToken("boolean"))
 			return new BasicType(expectToken("boolean"));
 		else if (acceptToken("void"))
 			return new BasicType(expectToken("void"));
 		else if (acceptIdentifier())
 			return new BasicType(expectIdentifier());
 		throw new UnexpectedTokenException(getCurrentToken());
 	}
 
 	private Statement parseStatement() {
 		if (acceptToken("{"))
 			return parseBlock();
 		else if (acceptToken(";"))
 			return parseEmptyStatement();
 		else if (acceptToken("if"))
 			return parseIfStatement();
 		else if (acceptToken("while"))
 			return parseWhileStatement();
 		else if (acceptToken("return"))
 			return parseReturnStatement();
 		else if (acceptPrimaryExpression()) // we could also just ignore the check
 			return parseExpressionStatement();
 
 		throw new UnexpectedTokenException(getCurrentToken());
 	}
 
 	// TODO so korrekt?
 	private Block parseBlock() {
 		expectToken("{");
 		while(!acceptToken("}")) {
 			parseBlockStatement();
 		}
 		expectToken("}");
 		return new Block();
 	}
 
 	private BlockStatement parseBlockStatement() {
 		if (acceptToken("{") || acceptToken(";") || acceptPrimaryExpression() ||
 				acceptToken("if") || acceptToken("while") || acceptToken("return")) {
 			return parseStatement();
 		} else { // TODO meeeh
 			return parseLocalVariableDeclarationStatement();
 		}
 	}
 
 	private LocalVariableDeclarationStatement parseLocalVariableDeclarationStatement() {
 		Type t = parseType();
 		Identifier name = expectIdentifier();
 		Expression e = null;
		if (acceptToken("="))
 			e = parseExpression();
 		expectToken(";");
 		return new LocalVariableDeclarationStatement(t, name, e);
 	}
 
 	private EmptyStatement parseEmptyStatement() {
 		expectToken(";");
 		return new EmptyStatement();
 	}
 
 	private WhileStatement parseWhileStatement() {
 		expectToken("while");
 		expectToken("(");
 		Expression e = parseExpression();
 		expectToken(")");
 		Statement s = parseStatement();
 		return new WhileStatement(e, s);
 	}
 
 	private IfStatement parseIfStatement() {
 		expectToken("if");
 		expectToken("(");
 		Expression e = parseExpression();
 		Statement s1 = parseStatement();
 		Statement s2 = null;
 		if (acceptToken("else")) {
 			expectToken("else");
 			s2 = parseStatement();
 		}
 		return new IfStatement(e, s1, s2);
 	}
 
 	private ExpressionStatement parseExpressionStatement() {
 		Expression e = parseExpression();
 		expectToken(";");
 		return new ExpressionStatement(e);
 	}
 
 	private ReturnStatement parseReturnStatement() {
 		expectToken("return");
 		if (acceptToken(";")) {
 			expectToken(";");
 			return new ReturnStatement(null);
 		}
 		else { // TODO check for primary expression. "{" should already throw an error
 			return new ReturnStatement(parseExpression());
 		}
 	}
 
 	public Expression parseExpression() {
 		return parseExpression(0);
 	}
 
 	public Expression parseExpression(int precedence) {
 		ParserFunction pf = _expressionParsers.get(getCurrentToken().toString());
 		Expression left;
 
 		if (pf != null && pf._parseExpressionPrefixFunction != null)
 			left = pf._parseExpressionPrefixFunction.parse();
 		else
 			left = parsePrimaryExpression();
 
 		while (true) {
 			pf = _expressionParsers.get(getCurrentToken().toString());
 
 			if (pf == null)
 				break;// throw new UnexpectedTokenException(getCurrentToken());
 
 			if (pf._parseExpressionInfixFunction == null || pf._precedence < precedence)
 				break;
 
 			left = pf._parseExpressionInfixFunction.parse(left);
 		}
 
 		return left;
 	}
 
 	private Expression parsePrimaryExpression() {
 		if (acceptToken("null"))
 			return new PrimaryExpression(expectToken("null"));
 		else if (acceptToken("false"))
 			return new PrimaryExpression(expectToken("false"));
 		else if (acceptToken("true"))
 			return new PrimaryExpression(expectToken("true"));
 		else if (acceptIntegerLiteral())
 			return new PrimaryExpression(expectIntegerLiteral());
 		else if (acceptIdentifier()) {
 			if (acceptToken("(", 1)) {
 				return parseLocalMethodInvocation();
 			}
 			return new PrimaryExpression(expectIdentifier());
 		}
 		else if (acceptToken("this"))
 			return new PrimaryExpression(expectToken("this"));
 		else if (acceptToken("(")) {
 			expectToken("(");
 			Expression e = parseExpression();
 			expectToken(")");
 			return e;
 		}
 		else if (acceptToken("new")) {
 			if (acceptToken("(", 2)) {
 				expectToken("new");
 				Identifier t = expectIdentifier();
 				expectToken("(");
 				expectToken(")");
 				return new NewObjectExpression(t);
 			}
 			else if (acceptToken("[", 2)) {
 				return parseNewArrayExpression();
 			}
 		}
 
 		throw new UnexpectedTokenException(getCurrentToken());
 	}
 
 	private NewArrayExpression parseNewArrayExpression() {
 		expectToken("new");
 		BasicType bt = parseBasicType();
 		expectToken("[");
 		Expression e = parseExpression();
 		expectToken("]");
 
 		int fieldCount = 1;
 		while (acceptToken("[")) {
 			expectToken("[");
 			expectToken("]");
 			fieldCount += 1;
 		}
 
 		return new NewArrayExpression(bt, e, fieldCount);
 	}
 
 	private LocalMethodInvocationExpression parseLocalMethodInvocation() {
 		Token t = expectIdentifier();
 		expectToken("(");
 		Arguments a = parseArguments();
 		expectToken(")");
 		return new LocalMethodInvocationExpression(t, a);
 	}
 
 	private Arguments parseArguments() {
 		Arguments a = new Arguments();
 		while (!acceptToken(")")) {
 			a.add(parseExpression());
 			if (acceptToken(")"))
 				break;
 			else
 				expectToken(",");
 		}
 		return a;
 	}
 
 
 
 
 }
