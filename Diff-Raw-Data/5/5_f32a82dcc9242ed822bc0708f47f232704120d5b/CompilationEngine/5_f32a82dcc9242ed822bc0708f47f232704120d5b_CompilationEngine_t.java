 // CompilationEngine.java
 
 package JackCompiler;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import JackCompiler.JackTokenizer.Token;
 
 public class CompilationEngine {
   public OutputStreamWriter outStream;
   public JackTokenizer tokenizer;
   public JackTokenizer.Token token_type;
   public char[] op = {'+','-','*','/','&','|','<','>','='};
 
   public enum Cat {
 	  VAR, ARG, STATIC, FIELD, CLASS, SUB;
   }
 
   // Creates a new compilation engine
   public CompilationEngine(JackTokenizer token, OutputStreamWriter stream) throws IOException {
     outStream = stream;
 	tokenizer = token;
   }
 
   // Compiles a complete class
   public void CompileClass() throws IOException {
 	
 	// Starting to compile a class
 	outStream.write("<class>\n");
 
 	if(tokenizer.keyWord().equals("class")) {
 		outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 		tokenizer.advance();
 		outStream.write("<identifier> " + tokenizer.identifier() + " CAT = class USED = false" + " </identifier>\n");
 		tokenizer.advance();
 		outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 		tokenizer.advance();
 		while(tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field")) {
 			CompileClassVarDec();
 		}
 		while(tokenizer.hasMoreTokens() && (tokenizer.keyWord().equals("constructor") || tokenizer.keyWord().equals("function") || tokenizer.keyWord().equals("method"))) {
 			CompileSubroutine();
 		}
 		
 	}
 	
 	// Finished with class, print closing bracket and tag
 	OutputXML(tokenizer.tokenType());
 	outStream.write("</class>\n");
   }
   
   // Compiles a static declaration or a field declaration
   public void CompileClassVarDec() throws IOException {
 	Boolean cont = true;
 	
 	outStream.write("<classVarDec>\n");
 	// Print out first keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out first variable declaration
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	// Check if there are more
 	if (tokenizer.symbol() == ';'){
 		cont = false;
 	}
 	
 	// If there are more variable declarations continue looping
 	while (cont){
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 
 		if (tokenizer.tokenType() == Token.SYMBOL){
 			if (tokenizer.symbol() == ';'){
 				cont = false;
 			}
 		}
 	}
 	
 	// Handle semi-colon at end
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	outStream.write("</classVarDec>\n");
   }
   
   // Compiles a complete method, function, or constructor
   public void CompileSubroutine() throws IOException {
 	outStream.write("<subroutineDec>\n");
 	outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 	tokenizer.advance();
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 	tokenizer.advance();
   	outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 	tokenizer.advance();
 	//Parameter list
 	compileParameterList();
 	//Subroutine body:
 	outStream.write("<subroutineBody>\n");
 	outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 	tokenizer.advance();
 	while (tokenizer.keyWord().equals("var")){
 		compileVarDec();
 	}
 	compileStatements();
 	
 	// Print out closing bracket for subroutine body
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	outStream.write("</subroutineBody>\n");
 	outStream.write("</subroutineDec>\n");
   }
   
   // Compiles a parameter list
   public void compileParameterList() throws IOException {
 	outStream.write("<parameterList>\n");
 	
 	Boolean cont = true;
 	token_type = tokenizer.tokenType();
 	
 	// If parameter list is empty, handle and return
 	if (token_type == Token.SYMBOL){
 		if (tokenizer.symbol() == ')'){
 			outStream.write("</parameterList>\n");
 			OutputXML(token_type);
 			tokenizer.advance();
 			return;
 		}
 	}
 	
 	// Otherwise loop through and print out the parameter list
 	while (cont){
     	//returns token's corresponding XML line
 		OutputXML(token_type);
 		
 		tokenizer.advance();
 		
 		token_type = tokenizer.tokenType();
 		if (token_type == Token.SYMBOL){
 			if (tokenizer.symbol() == ')'){
 				cont = false;
 			}
 		}
 	}
 		
 	outStream.write("</parameterList>\n");
 	
 	// Handle closing parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
   }
   
   // Compiles a var declaration
   public void compileVarDec() throws IOException {
   	Boolean cont = true;
 	
   	outStream.write("<varDec>\n");
 	
 	// Print out first variable declaration
 	outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 	tokenizer.advance();
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 	tokenizer.advance();
 	
 	// Check if there are more
 	if (tokenizer.symbol() == ';'){
 		cont = false;
 	}
 	
 	// If there are more variable declarations continue looping
 	while (cont){
 		outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 		tokenizer.advance();
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		if (tokenizer.symbol() == ';'){
 			cont = false;
 		}
 	}
 	
 	// Handle semi-colon at end
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	outStream.write("</varDec>\n");
   }
   
   // Compiles a sentence of statements
   public void compileStatements() throws IOException {
 	outStream.write("<statements>\n");
 	
 	String kwd = tokenizer.keyWord();
 	
 	Boolean cont = true;
 	while (cont){
 		if (kwd.equals("let")){
 					outStream.write("<letStatement>\n");
 					compileLet();
 					outStream.write("</letStatement>\n");
 				} else if (kwd.equals("if")){
 					outStream.write("<ifStatement>\n");
 					compileIf();
 					outStream.write("</ifStatement>\n");
 				} else if (kwd.equals("while")){
 					outStream.write("<whileStatement>\n");
 					compileWhile();
 					outStream.write("</whileStatement>\n");
 				} else if (kwd.equals("do")){
 					outStream.write("<doStatement>\n");
 					compileDo();
 					outStream.write("</doStatement>\n");
 				} else if (kwd.equals("return")){
 					outStream.write("<returnStatement>\n");
 					compileReturn();
 					outStream.write("</returnStatement>\n");
 				} else {
 					System.err.println("Error parsing statements.");
 				}
 				
 				// Stopping condition if have reached a symbol, the closing }
 				if (tokenizer.tokenType() == Token.SYMBOL) {
 					cont = false;
 				} else {
 					kwd = tokenizer.keyWord();
 				}
 			}
   
 	outStream.write("</statements>\n");
   }
   
   // Compiles a do statement
   public void compileDo() throws IOException {
 	// Print out the do keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	compileSubroutine();
 	
 	// Get the ending semi-colon
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
   }
   
   public void compileSubroutine() throws IOException {
 	// Print subroutine name or class name
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// If we have an expression list
 	if (tokenizer.symbol() == '('){
 		// Print opening parenthesis
		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 
 		// Compile expression list
 		CompileExpressionList();
 		
 	} else if (tokenizer.symbol() == '.'){
 		// Print the . symbol
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		// Print the subroutine name
		OutputXML(tokenizer.tokenType()), Cat.SUB, false);
 		tokenizer.advance();
 		
 		// Print the opening parenthesis
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		// Compile the expression list
 		CompileExpressionList();
 	}
   }
   
   // Compiles a let statement
   public void compileLet() throws IOException {
 	// Print out the let keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out the variable name
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	if (tokenizer.symbol() == '['){
 		// Print out opening bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		// Compile expression
 		CompileExpression();
 		// Print out closing bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 	}
 	
 	// Print out equals sign
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Compile expression
 	CompileExpression();
 	
 	// Print out semi-colon
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
   }
   
   // Compiles a while statement
   public void compileWhile() throws IOException {
 	// Print out the first keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out opening parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Compile the expression
 	CompileExpression();
 	
 	// Print out the closing parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out the opening bracket
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Compile statements
 	compileStatements();
 	
 	// Print out closing bracket
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
   }
   
   // Compiles a return statement
   public void compileReturn() throws IOException {
 	// Print out the first keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	if (tokenizer.tokenType() == Token.SYMBOL){
 		if (tokenizer.symbol() == ';'){
 			// Print out the semi-colon
 			OutputXML(tokenizer.tokenType());
 			tokenizer.advance();
 		}
 	} else {
 		CompileExpression();
 		
 		// Print out the semi-colon
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 	}
   }
   
   // Compiles an if statement
   public void compileIf() throws IOException {
 	// Print out the first keyword
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out opening parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Compile the expression
 	CompileExpression();
 	
 	// Print out the closing parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Print out the opening bracket
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// Compile statements
 	compileStatements();
 	
 	// Print out closing bracket
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
 	
 	// If else statement advance until next keyword for now
 	if (tokenizer.tokenType() == Token.KEYWORD && tokenizer.keyWord().equals("else")){
 		// Print out the else
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		// Print out the opening bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		// Compile statements
 		compileStatements();
 		
 		// Print out closing bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 	}
 	
   }
   
   // Compiles an expression
   public void CompileExpression() throws IOException {
 	outStream.write("<expression>\n");
 	
 	// Print out the first term
 	CompileTerm();
 	
 	// Check type of next symbol
 	char sym = tokenizer.symbol();
 	
 	while (sym == '+' || sym == '-' || sym == '*' || sym == '/' || sym == '&' || sym == '|' || sym == '<' || sym == '>' || sym == '='){
 		// Print out operation
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		
 		// Print out term
 		CompileTerm();
 		
 		// Check next symbol
 		sym = tokenizer.symbol();
 	}
 
 	outStream.write("</expression>\n");
   }
   
   // Compiles a term
   public void CompileTerm() throws IOException {
 	outStream.write("<term>\n");
 	
 	if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '(') {
 		outStream.write("<symbol> " + "(" + " </symbol>\n");
 		tokenizer.advance();
 		CompileExpression();
 		outStream.write("<symbol> " + ")" + " </symbol>\n");
 		tokenizer.advance();		
 	} else if(tokenizer.tokenType() == Token.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~'	)) {
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		CompileTerm();
 	}else {
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '[') {
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			CompileExpression();
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 		}else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '('){
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			CompileExpressionList();
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 		}else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '.') {
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			OutputXML(tokenizer.tokenType());
 			tokenizer.advance();
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			CompileExpressionList();
 			
 		}
 	}
 	outStream.write("</term>\n");
   }
   
   // Compiles a comma-separated list of expressions
   public void CompileExpressionList() throws IOException {
 	outStream.write("<expressionList>\n");
 	
 	Boolean cont = true;
 	token_type = tokenizer.tokenType();
 	
 	// If parameter list is empty, handle and return
 	if (token_type == Token.SYMBOL){
 		if (tokenizer.symbol() == ')'){
 			outStream.write("</expressionList>\n");
 			OutputXML(token_type);
 			tokenizer.advance();
 			return;
 		}
 	}
 	
 	// Otherwise loop through and print out the parameter list
 	while (cont){
     	//returns token's corresponding XML line
 		CompileExpression();
 		
 		token_type = tokenizer.tokenType();
 		if (token_type == Token.SYMBOL){
 			if (tokenizer.symbol() == ')'){
 				cont = false;
 			} else if (tokenizer.symbol() == ','){
 				OutputXML(tokenizer.tokenType());
 				tokenizer.advance();
 			}
 		}
 	}
 		
 	outStream.write("</expressionList>\n");
 	
 	// Handle closing parenthesis
 	OutputXML(tokenizer.tokenType());
 	tokenizer.advance();
   }
   public void OutputXML(Token token_type) throws IOException {
 		if(token_type == Token.KEYWORD) {
 			try{
 				outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 			}catch(IOException x){
 				//TODO: print error?
 			}
 		}
 		if(token_type == Token.SYMBOL) {
 			String x = Character.toString(tokenizer.symbol());
 			if(x.equals("<")) {
 				x = "&lt;";
 			}
 			if(x.equals(">")) {
 				x = "&gt;";
 			}
 			if(x.equals("&")) {
 				x = "&amp;";
 			}
 			try{
 				outStream.write("<symbol> " + x + " </symbol>\n");
 			}catch(IOException z){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.IDENTIFIER) {
 			try{
 				outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 			}catch(IOException h){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.INT_CONST) {	
 			try{
 				outStream.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
 			}catch(IOException k) {
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.STRING_CONST) {
 			try{
 				outStream.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
 			}catch(IOException m){
 				System.err.println("error outputing XML");
 			}
 		}
   }
   
   public void OutputXML(Token token_type, Cat c, Boolean used) throws IOException {
 		if(token_type == Token.KEYWORD) {
 			try{
 				outStream.write("<keyword> " + tokenizer.keyWord() + " CAT=" + c + " USED=" + used + " </keyword>\n");
 			}catch(IOException x){
 				//TODO: print error?
 			}
 		}
 		if(token_type == Token.SYMBOL) {
 			String x = Character.toString(tokenizer.symbol());
 			if(x.equals("<")) {
 				x = "&lt;";
 			}
 			if(x.equals(">")) {
 				x = "&gt;";
 			}
 			if(x.equals("&")) {
 				x = "&amp;";
 			}
 			try{
 				outStream.write("<symbol> " + x + " </symbol>\n");
 			}catch(IOException z){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.IDENTIFIER) {
 			try{
 				outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 			}catch(IOException h){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.INT_CONST) {	
 			try{
 				outStream.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
 			}catch(IOException k) {
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.STRING_CONST) {
 			try{
 				outStream.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
 			}catch(IOException m){
 				System.err.println("error outputing XML");
 			}
 		}
   }
 }
