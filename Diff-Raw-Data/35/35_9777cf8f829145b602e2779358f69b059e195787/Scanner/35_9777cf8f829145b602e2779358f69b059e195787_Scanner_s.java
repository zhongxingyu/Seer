 package scanner;
 
 import java.util.ArrayList;
 import main.SyntaxException;
 
 public class Scanner {
 	// Public for testing
 	public String code;
 	private int pointer;
 	public ArrayList<Token> tokens;
 	
 	public Scanner(String code) {
 		this.code = code;
 		this.tokens = new ArrayList<Token>();
 	}
 	
 	/**
 	 * Scans string for tokens
 	 * 
 	 * Scans for numbers (1.5, 1, 0, 0.5, .5, etc.), strings ("Hello!"), 
 	 * code literals ({ somecode() }), array/list start and end ([ and ])
 	 * removes comments
 	 * 
 	 * @return ArrayList List of tokens
	 * @throws Exception
 	 */
	public void scan() throws Exception {
 		// Loop over all code and extract tokens
 		char c;
 		for(this.pointer = 0; this.pointer < this.code.length()-1; this.pointer++) {
 			this.skipSpaces();
 			
 			c = this.code.charAt(this.pointer);
 			if((c >= '0') && (c <= '9')) {
 				this.parseNumber();
 			} else
 			if(c == '"') {
 				this.parseString();
 			} else
 			if(c == '{') {
 				this.parseCode();
 			} else
 			if(c == '[') {
 				this.tokens.add(new Token(TokenType.OpenSquareBracket));
 			} else
 			if(c == ']') {
 				this.tokens.add(new Token(TokenType.CloseSquareBracket));
 			}
 			if((c == '/') && this.code.charAt(this.pointer+1) == '*') {
 				this.skipComment();
 			}
 			else {
 				this.parseLiteral();
 			}
 		}
 	}
 	
 	private void skipComment() {
 		
 	}
 
 	/**
 	 * Parse functions and variables
 	 */
 	private void parseLiteral() {
 		
 	}
 	
 	private void parseCode() {
 		
 	}
 
 	private void parseString() throws SyntaxException {
 		if(this.code.charAt(this.pointer) != '"') {
 			return;
 		}
 		this.pointer++;
 		
 		String str = "";
 		/*
 		 * While '"' not found keep adding. Stop if reached
 		 * end of string.
 		 */
 		int startPos = this.pointer;
		while(((this.code.charAt(this.pointer) != '"'))
		   && (this.pointer < this.code.length())) {
 			str += this.code.charAt(this.pointer);
 			
 			this.pointer++;
 		}
 		
 		// If no matching '"' found.
 		if(this.pointer == this.code.length()) {
 			throw new SyntaxException(startPos, "String not closed.");
 		}
 		
 		this.tokens.add(new Token(TokenType.String, str));
 	}
 
 	private void parseNumber() {
 		
 	}
 
 	/**
 	 * Moves pointer to skip spaces
 	 */
 	private void skipSpaces() {
 		while(this.code.charAt(this.pointer) == ' ') {
 			this.pointer++;
 		}
 	}
 }
