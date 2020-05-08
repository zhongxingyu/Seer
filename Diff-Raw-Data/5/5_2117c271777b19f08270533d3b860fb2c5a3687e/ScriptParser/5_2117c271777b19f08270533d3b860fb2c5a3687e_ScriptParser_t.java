 package cs3240.sp09.MicroAWKIntepreter;
 import cs3240.sp09.AbstractSyntaxTree.*;
 
 public class ScriptParser {
 	
 	private ScriptReader reader;
 	
 	public ScriptParser(String script){
 		reader = new ScriptReader(script);
 	}
 	
 	public ASTNode parse() {
 		return program();
 	}
 	
 	public ASTNode program(){
 		ASTNode programNode = new ASTNode(ASTNode.NodeType.Program);
 		try{
 			programNode.setLeftChild(statement());
 		} catch(Exception e){
 			System.err.println("Parse error: " + e.getMessage());
 			e.printStackTrace();
 			programNode.setLeftChild(null);
 		}
 		if(reader.token != (char)-1){
 			programNode.setRightChild(program());
 		}
 		return programNode;
 	}
 
 	public ASTNode statement() throws MawkParserException {
 		ASTNode statementNode = new ASTNode(ASTNode.NodeType.Statement);
 		switch(reader.token){
 			case 'B':
 				statementNode.setLeftChild(begin());
 				break;
 			case 'E':
 				statementNode.setLeftChild(end());
 				break;
 			case 'w':
 				statementNode.setLeftChild(whileLoop());
 				break;
 			case 'f':
 				statementNode.setLeftChild(forLoop());
 				break;
 			case '{':
 				reader.match('{');
 				statementNode.setLeftChild(funcBlock());
 				reader.match('}');
 				break;
 			default:
 				statementNode.setLeftChild(regex());
 				reader.match('{');
 				statementNode.setRightChild(funcBlock());
 				reader.match('}');
 				break;
 		}
 		return statementNode;
 	}
 	
 	public ASTNode regex() throws MawkParserException{
 		ASTNode regexNode = new RegexNode(ASTNode.NodeType.Regex);
 		regexNode.setLeftChild(term());
 		if(reader.token == '|'){
 			reader.match('|');
 			regexNode.setRightChild(regex());
 		}
 		return regexNode;
 	}
 	
 	public ASTNode term() throws MawkParserException{
 		ASTNode termNode = new ASTNode(ASTNode.NodeType.Term);
 		termNode.setLeftChild(factor());
 		if(reader.token == 'a' || reader.token == 'b' || reader.token == 'c' || reader.token == '('){
 			termNode.setRightChild(term());
 		}
 		return termNode;
 	}
 	
 	public ASTNode factor() throws MawkParserException{
 		ASTNode factorNode = new ASTNode(ASTNode.NodeType.Factor);
 		factorNode.setLeftChild(atom());
 		if(reader.token == '*' || reader.token == '+' || reader.token == '?'){
 			factorNode.setRightChild(metacharacter());
 		}
 		return factorNode;
 	}
 
 	private ASTNode atom() throws MawkParserException {
 		ASTNode atomNode = new ASTNode(ASTNode.NodeType.Atom);
 		if(reader.token == '('){
 			reader.match('(');
 			atomNode.setLeftChild(regex());
 			reader.match(')');
 		} else {
 			atomNode.setLeftChild(character());
 		}
 		return atomNode;
 	}
 
 	public ASTNode metacharacter() throws MawkParserException{
 		ASTNode metacharacterNode = new ASTNode(ASTNode.NodeType.Metacharacter);
 		switch(reader.token){
 		case '*':
 			metacharacterNode.setLeftChild(star());
 			break;
 		case '+':
 			metacharacterNode.setLeftChild(oneOrMore());
 			break;
 		case '?':
 			metacharacterNode.setLeftChild(optional());
 			break;
 		default:
 			throw new MawkParserException("Invalid metacharacter token: " + reader.token);
 		}
 		return metacharacterNode;
 	}
 	public ASTNode star() throws MawkParserException{
 		ASTNode starNode = new ASTNode(ASTNode.NodeType.Star);
 		reader.match('*');
 		return starNode;
 	}
 	public ASTNode oneOrMore() throws MawkParserException{
 		ASTNode oneOrMoreNode = new ASTNode(ASTNode.NodeType.OneOrMore);
 		reader.match('+');
 		return oneOrMoreNode;
 	}
 	public ASTNode optional() throws MawkParserException{
 		ASTNode optionalNode = new ASTNode(ASTNode.NodeType.Optional);
 		reader.match('?');
 		return optionalNode;
 	}
 	
 	public ASTNode begin() throws MawkParserException {
 		ASTNode beginNode = new ASTNode(ASTNode.NodeType.Begin);
 		reader.matchString("BEGIN{");
 		beginNode.setLeftChild(funcBlock());
 		reader.match('}');
 		return beginNode;
 	}
 	
 	public ASTNode end() throws MawkParserException {
 		ASTNode endNode = new ASTNode(ASTNode.NodeType.End);
 		reader.matchString("END{");
 		endNode.setLeftChild(funcBlock());
 		reader.match('}');
 		return endNode;
 	}
 	
 	public ASTNode funcBlock() throws MawkParserException{
 		ASTNode funcBlockNode = new ASTNode(ASTNode.NodeType.FunctionBlock);
 		funcBlockNode.setLeftChild(function());
 		if(reader.token == ';'){
 			reader.match(';');
 			funcBlockNode.setRightChild(funcBlock());
 		}
 		return funcBlockNode;
 	}
 	
 	public ASTNode function() throws MawkParserException{
 		ASTNode functionNode = new ASTNode(ASTNode.NodeType.Function);
 		switch(reader.token){
 		case 's':
 			functionNode.setLeftChild(substringFunction());
 			break;
 		case 'i':
 			functionNode.setLeftChild(insertFunction());
 			break;
 		case 'p':
 			functionNode.setLeftChild(printFunction());
 			break;
 		case 'r':
 			functionNode.setLeftChild(reFunction());
 			break;
 		default:
 			throw new MawkParserException("Invalid function name on token: " + reader.token);
 		}
 		return functionNode;
 	}
 	
 	public ASTNode loopBlock() throws MawkParserException {
 		ASTNode loopBlockNode = new ASTNode(ASTNode.NodeType.LoopBlock);
 		loopBlockNode.setLeftChild(regex());
 		reader.match('{');
 		loopBlockNode.setRightChild(funcBlock());
 		reader.match('}');
 		return loopBlockNode;
 	}
 	
 	public ASTNode whileLoop() throws MawkParserException {
 		ASTNode whileNode = new ASTNode(ASTNode.NodeType.WhileLoop);
 		reader.matchString("while");
 		reader.match('(');
 		whileNode.setLeftChild(regex());
 		reader.match(')');
 		reader.match('{');
 		while(reader.token != '}'){
 			whileNode.setRightChild(loopBlock());
 		}
 		reader.match('}');
 		return whileNode;
 	}
 	
 	public ASTNode forLoop() throws MawkParserException {
 		ASTNode forNode = new ASTNode(ASTNode.NodeType.ForLoop);
 		reader.matchString("for");
 		reader.match('(');
 		forNode.setLeftChild(integer());
 		reader.match(')');
 		reader.match('{');
 		while(reader.token != '}') {
 			forNode.setRightChild(loopBlock());
 		}
 		reader.match('}');
 		return forNode;
 	}
 
 	public ASTNode substringFunction() throws MawkParserException {
 		ASTNode substringFunctionNode = new ASTNode(ASTNode.NodeType.SubstringFunction);
 		reader.matchString("substring(");
 		substringFunctionNode.setLeftChild(integer());
 		reader.match(',');
 		if(reader.token == 'E'){
			reader.matchString("EOL");
 			substringFunctionNode.setRightChild(new ASTNode(ASTNode.NodeType.EndIndex));
 		} else {
 			substringFunctionNode.setRightChild(integer());
 		}
 		reader.match(')');
 		return substringFunctionNode;
 	}
 	
 	public ASTNode integer() throws MawkParserException {
 		ASTNode integerNode = new ASTNode(ASTNode.NodeType.Integer);
 		integerNode.setLeftChild(number());
 		if(reader.token >= '0' && reader.token <= '9')
 			integerNode.setRightChild(integer());
 		return integerNode;
 	}
 	private ASTNode number() throws MawkParserException {
 		ASTNode numberNode = new ASTNode(ASTNode.NodeType.Number);
 		switch(reader.token){
 		case '0':
 			numberNode.setLeftChild(zero());
 			break;
 		case '1':
 			numberNode.setLeftChild(one());
 			break;
 		case '2':
 			numberNode.setLeftChild(two());
 			break;
 		case '3':
 			numberNode.setLeftChild(three());
 			break;
 		case '4':
 			numberNode.setLeftChild(four());
 			break;
 		case '5':
 			numberNode.setLeftChild(five());
 			break;
 		case '6':
 			numberNode.setLeftChild(six());
 			break;
 		case '7':
 			numberNode.setLeftChild(seven());
 			break;
 		case '8':
 			numberNode.setLeftChild(eight());
 			break;
 		case '9':
 			numberNode.setLeftChild(nine());
 			break;
 		default:
 			throw new MawkParserException(String.format("Invalid number token: %c", reader.token));
 		}
 		return numberNode;
 	}
 	private ASTNode zero() throws MawkParserException {
 		reader.match('0');
 		return new ASTNode(ASTNode.NodeType.Zero);
 	}
 	private ASTNode one() throws MawkParserException {
 		reader.match('1');
 		return new ASTNode(ASTNode.NodeType.One);
 	}
 	private ASTNode two() throws MawkParserException {
 		reader.match('2');
 		return new ASTNode(ASTNode.NodeType.Two);
 	}
 	private ASTNode three() throws MawkParserException {
 		reader.match('3');
 		return new ASTNode(ASTNode.NodeType.Three);
 	}
 	private ASTNode four() throws MawkParserException {
 		reader.match('4');
 		return new ASTNode(ASTNode.NodeType.Four);
 	}
 	private ASTNode five() throws MawkParserException {
 		reader.match('5');
 		return new ASTNode(ASTNode.NodeType.Five);
 	}
 	private ASTNode six() throws MawkParserException {
 		reader.match('6');
 		return new ASTNode(ASTNode.NodeType.Six);
 	}
 	private ASTNode seven() throws MawkParserException {
 		reader.match('7');
 		return new ASTNode(ASTNode.NodeType.Seven);
 	}
 	private ASTNode eight() throws MawkParserException {
 		reader.match('8');
 		return new ASTNode(ASTNode.NodeType.Eight);
 	}
 	private ASTNode nine() throws MawkParserException {
 		reader.match('9');
 		return new ASTNode(ASTNode.NodeType.Nine);
 	}
 
 	public ASTNode insertFunction() throws MawkParserException {
 		ASTNode insertFunctionNode = new ASTNode(ASTNode.NodeType.InsertFunction);
 		reader.matchString("insert(");
 		if(reader.token == 'E'){
			reader.matchString("EOL");
 			insertFunctionNode.setLeftChild(new ASTNode(ASTNode.NodeType.EndIndex));
 		} else {
 			insertFunctionNode.setLeftChild(integer());
 		}
 		reader.match(',');
 		insertFunctionNode.setRightChild(character());
 		reader.match(')');
 		return insertFunctionNode;
 	}
 	
 	private ASTNode character() throws MawkParserException {
 		ASTNode character = new ASTNode(ASTNode.NodeType.Character);
 		switch(reader.token){
 		case 'a':
 			character.setLeftChild(a());
 			break;
 		case 'b':
 			character.setLeftChild(b());
 			break;
 		case 'c':
 			character.setLeftChild(c());
 			break;
 		default:
 			throw new MawkParserException("Invalid character token: " + reader.token);
 		}
 		return character;
 	}
 	private ASTNode a() throws MawkParserException {
 		reader.match('a');
 		return new ASTNode(ASTNode.NodeType.A);
 	}
 	private ASTNode b() throws MawkParserException {
 		reader.match('b');
 		return new ASTNode(ASTNode.NodeType.B);
 	}
 	private ASTNode c() throws MawkParserException {
 		reader.match('c');
 		return new ASTNode(ASTNode.NodeType.C);
 	}
 
 	public ASTNode printFunction() throws MawkParserException{
 		ASTNode printFunctionNode = new ASTNode(ASTNode.NodeType.PrintFunction);
 		reader.matchString("print(");
 		if(reader.token == '"'){
 			printFunctionNode.setLeftChild(string());
 		} else {
 			printFunctionNode.setLeftChild(line());
 		}
 		reader.match(')');
 		return printFunctionNode;
 	}
 	
 	private ASTNode line() throws MawkParserException {
 		reader.matchString("LINE");
 		return new ASTNode(ASTNode.NodeType.Line);
 	}
 
 	private ASTNode string() throws MawkParserException {
 		ASTNode stringNode = new ASTNode(ASTNode.NodeType.String);
 		reader.match('"');
 		stringNode.setLeftChild(stringInner());
 		reader.match('"');
 		return stringNode;
 	}
 
 	private ASTNode stringInner() {
 		StringInnerNode stringInnerNode = new StringInnerNode(ASTNode.NodeType.StringInner);
 		String str = "";
 		while(reader.token != '"'){
 			str += reader.token;
 			reader.getcharWithWhitespace();
 		}
 		stringInnerNode.setValue(str);
 		return stringInnerNode;
 	}
 
 	public ASTNode reFunction() throws MawkParserException{
 		reader.matchString("re");
 		switch(reader.token){
 		case 'p':
 			return replaceFunction();
 		case 'm':
 			return removeFunction();
 		default:
 			throw new MawkParserException("Invalid function name that starts with re.");
 		}
 	}
 
 	private ASTNode removeFunction() throws MawkParserException {
 		ASTNode removeFunctionNode = new ASTNode(ASTNode.NodeType.RemoveFunction);
 		reader.matchString("move(");
 		removeFunctionNode.setLeftChild(character());
 		reader.match(')');
 		return removeFunctionNode;
 	}
 
 	private ASTNode replaceFunction() throws MawkParserException {
 		ASTNode replaceFunctionNode = new ASTNode(ASTNode.NodeType.ReplaceFunction);
 		reader.matchString("place(");
 		replaceFunctionNode.setLeftChild(character());
 		reader.match(',');
 		replaceFunctionNode.setRightChild(character());
 		reader.match(')');
 		return replaceFunctionNode;
 	}
 }
