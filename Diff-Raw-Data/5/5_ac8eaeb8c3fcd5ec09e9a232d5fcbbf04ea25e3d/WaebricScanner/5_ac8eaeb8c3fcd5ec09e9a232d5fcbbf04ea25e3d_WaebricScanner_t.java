 package org.cwi.waebric.scanner;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cwi.waebric.WaebricKeyword;
 import org.cwi.waebric.scanner.token.Position;
 import org.cwi.waebric.scanner.token.Token;
 import org.cwi.waebric.scanner.token.TokenIterator;
 
 public class WaebricScanner {
 	
 	public static final int EOF = -1;
 	public static final int TAB_LENGTH = 5;
 	
 	/**
 	 * Current character
 	 */
 	private int curr;
 	
 	/**
 	 * Value buffer
 	 */
 	private String buffer;
 	
 	/**
 	 * Current character position
 	 */
 	private Position cpos;
 	
 	/**
 	 * Current token position
 	 */
 	private Position tpos;
 	
 	/**
 	 * Collection of tokens
 	 */
 	private final List<Token> tokens;
 	
 	/**
 	 * Collection of lexical exceptions
 	 */
 	private final List<LexicalException> exceptions;
 
 	/**
 	 * Input stream
 	 */
 	private final Reader reader;
 	
 	/**
 	 * Construct tokenizer based on reader, in case an invalid reader is given,
 	 * a null pointer exception will be thrown.
 	 * 
 	 * @see Reader
 	 * 
 	 * @param reader Input character stream
 	 * @param exceptions Collection of scan exceptions
 	 * @throws IOException Thrown by Reader
 	 */
 	public WaebricScanner(Reader reader) throws IOException {
 		if(reader == null) {
 			throw new NullPointerException();
 		}
 		
 		// Initiate position structures
 		cpos = new Position();
 		tpos = new Position();
 		
 		this.exceptions = new ArrayList<LexicalException>();
 		this.tokens = new ArrayList<Token>();
 		this.reader = reader; // Store reader reference
 		
 		read(); // Buffer first character
 	}
 	
 	/**
 	 * Read next character from stream and increment character count.
 	 * @return character
 	 * @throws IOException
 	 */
 	private void read() throws IOException {
 		curr = reader.read();
 		
 		// Maintain actual line and character numbers
 		if(curr == '\n') { cpos.charno = 0; cpos.lineno++; } // New line
 		else if(curr == '\t') { cpos.charno += TAB_LENGTH; } // Tab
 		else if(curr >= 0) { cpos.charno++; } // Not end of file
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws IOException 
 	 */
 	public List<LexicalException> tokenizeStream() throws IOException {
 		if(tokens.size() > 0) { return exceptions; }
 		
 		while(curr != EOF) {
 			buffer = ""; // Clean buffer
 			
 			// Store actual token position
 			tpos.lineno = cpos.lineno;
 			tpos.charno = cpos.charno;
 			
 			// Process token, based on first character
 			if(isLayout(curr)) { processLayout(); }
 			else if(curr == '/') { processComment(); }
 			else if(curr == '"') { tokenizeText(); }
 			else if(curr == '\'') { tokenizeSymbol(); }
 			else if(isLetter(curr)) { tokenizeWord(); }
 			else if(isNumeral(curr)) { tokenizeNumber(); }
 			else { tokenizeCharacter(); }
 		}
 		
 		return exceptions;
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void processLayout() throws IOException {
 		read(); // Skip layout characters
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void processComment() throws IOException {
 		read(); // Retrieve next symbol
 		
 		if(curr == '*') { // Multiple-line comment /* */
 			read(); // Retrieve first comment character
 			
 			char previous; // Store previous
 			do {
 				previous = (char) curr;
 				read(); // Retrieve next comment character
 			} while(! (previous == '*' && curr == '/') && curr != EOF);
 			
 			read(); // Retrieve next character
 		} else if(curr == '/') { // Single-line comment //
 			read(); // Retrieve first comment character
 			
 			do {
 				read(); // Retrieve next comment character
 			} while(curr != '\n'  && curr != EOF);
 		
 			read(); // Retrieve next character
 		} else { // Symbol character /
 			Token slash = new Token.CharacterToken('/', tpos.lineno, tpos.charno);
 			tokens.add(slash);
 		}
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeWord() throws IOException {
 		do {
 			buffer += (char) curr;
 			read(); // Read next character
 		} while(isLetter(curr) || isNumeral(curr) || curr == '-');
 		
 		// Determine token sort
 		if(isKeyword(buffer)) {
 			// Retrieve keyword element from enumeration
 			WaebricKeyword element = WaebricKeyword.valueOf(buffer.toUpperCase());
 			
 			// Store keyword
 			Token keyword = new Token.KeywordToken(element, tpos.lineno, tpos.charno);
 			tokens.add(keyword);
 		} else {
 			// Store word
 			Token identifier = new Token.IdentifierToken(buffer, tpos.lineno, tpos.charno);
 			tokens.add(identifier);
 		}
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeNumber() throws IOException {
 		int number = 0; // Integer buffer
 		
 		while(isNumeral(curr)) {
 			number *= 10; // Create space for next character
 			number += curr - 48; // '0' equals decimal 48
 			read(); // Read next number
 		}
 		
 		// Store natural
 		Token natural = new Token.NaturalToken(number, tpos.lineno, tpos.charno);
 		tokens.add(natural);
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeCharacter() throws IOException {
 		// Store character
 		Token character = new Token.CharacterToken((char) curr, tpos.lineno, tpos.charno);
 		tokens.add(character);
 		
 		read(); // Retrieve next character
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeSymbol() throws IOException {
 		read(); // Retrieve first symbol character
 		
 		while(isSymbolChar(curr)) {
 			buffer += (char) curr;
 			read(); // Retrieve next symbol character
 		}
 		
 		// Store symbol
 		Token symbol = new Token.SymbolToken(buffer, tpos.lineno, tpos.charno);
 		tokens.add(symbol);
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeText() throws IOException {
 		read(); // Retrieve first character
 		
 		int previous = 0;
 		while(curr != '"' || previous == '\\') {
 			if(curr == EOF) {
 				// Unclosed text token, store exception
 				exceptions.add(new LexicalException.UnclosedText(buffer, tpos));
 				return;
 			} else if(curr == '<' && ! inStringContext()) {
 				// Embedding character detected, scan as embed and quit text
 				tokenizeEmbedding(); return;
 			}
 
 			previous = curr; // Store current as previous to look for '\'
 			buffer += (char) curr; // Acceptable character, store in buffer
 			
 			read(); // Retrieve next character
 		}
 		
 		// Create token from buffered data
 		if(inStringContext()) {
 			if(isString(buffer)) { 
 				tokens.add(new Token.StringToken(buffer, tpos.lineno, tpos.charno)); 
			} else { exceptions.add(new LexicalException.InvalidString(buffer, tpos)); }
 		} else {
 			if(isText(buffer)) {
 				tokens.add(new Token.TextToken(buffer, tpos.lineno, tpos.charno));
			} else { exceptions.add(new LexicalException.InvalidText(buffer, tpos)); }
 		}
 
 		read(); // Skip closure " symbol
 	}
 	
 	/**
 	 * Check if current token is in string context
 	 * @return
 	 */
 	private boolean inStringContext() {
 		return tokens.size() > 0 
 			&& tokens.get(tokens.size()-1).getLexeme() == WaebricKeyword.COMMENT;
 	}
 	
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private void tokenizeEmbedding() throws IOException {
 		List<Token> content = new ArrayList<Token>();
 		
 		// Attach opening quote token
 		content.add(new Token.CharacterToken('"', tpos.lineno, tpos.charno));
 
 		int previous = 0;
 		boolean quoted = false, embeded = false;
 		do {
 			if(curr == EOF) {
 				// Unclosed embedding token, store exception
 				exceptions.add(new LexicalException.UnclosedEmbedding(buffer, cpos));
 				return;
 			}
 			
 			if(curr == '"' && previous != '\\') { quoted = ! quoted; }
 			if(curr == '<' && ! quoted) { 
 				// Detected start of embed, process pre-text
 				content.add(new Token.TextToken(buffer, cpos.lineno, cpos.charno));
 				buffer = ""; // Clean buffer
 				embeded = true;
 			}
 
 			// Acceptable character, store in buffer
 			buffer += (char) curr;
 			previous = curr;
 			
 			if(curr == '>' && ! quoted) { 
 				// Detected end of embed, process content
 				flushBuffer(content);
 				embeded = false;
 			}
 
 			read(); // Retrieve next character
 		} while(((curr != '"' || previous == '\\') || embeded));
 
 		if(! buffer.equals("")) {
 			// Process post text
 			content.add(new Token.TextToken(buffer, cpos.lineno, cpos.charno));
 		}
 
 		if(curr != EOF) { 
 			// Attach closure quote
 			content.add(new Token.CharacterToken('"', cpos.lineno, cpos.charno));
 		}
 		
 		// Create token from buffered content
 		Token embedding = new Token.EmbeddingToken(content, tpos.lineno, tpos.charno);
 		tokens.add(embedding);
 		
 		read(); // Skip closure " symbol
 	}
 	
 	/**
 	 * Convert buffer data in character input stream, which is then converted by a new 
 	 * scanner instance into tokens.
 	 * @param tokens
 	 * @throws IOException 
 	 */
 	private void flushBuffer(List<Token> tokens) throws IOException {
 		// Buffer contains no contents, quit
 		if(buffer == null || buffer.equals("")) { return; } 
 
 		// Initiate scanner
 		StringReader reader = new StringReader(buffer);
 		WaebricScanner instance = new WaebricScanner(reader);
 		
 		// Set absolute start position
 		if(! tokens.isEmpty()) { 
 			Token last = tokens.get(tokens.size()-1);
 			instance.setPosition(new Position(last.getLine(), last.getCharacter()));
 		}
 		
 		// Process buffer and store tokens
 		instance.tokenizeStream();
 		tokens.addAll(instance.getTokens());
 		
 		buffer = ""; // Clean buffer
 	}
 	
 	/**
 	 * @param c
 	 * @return
 	 */
 	public static boolean isLetter(int c) {
 		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
 	}
 	
 	/**
 	 * @param c
 	 * @return
 	 */
 	public static boolean isNumeral(int c) {
 		return c >= '0' && c <= '9';
 	}
 	
 	/**
 	 * @param c
 	 * @return
 	 */
 	public static boolean isLayout(int c) {
 		return c == ' ' || c == '\t' || c =='\n' || c == '\r';
 	}
 	
 	/**
 	 * @param c
 	 * @return 
 	 */
 	private static boolean isSymbolChar(int c) {
 		return c > 31 && c < 127 && c != ' ' && c != ';' && c != ',' && c != '>' && c != '}' && c != ']' && c != ')';
 	}
 	
 	/**
 	 * 
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isText(String lexeme) {
 		if(lexeme == null) { return false; }
 		char chars[] = lexeme.toCharArray();
 		
 		for(int i = 0; i < chars.length; i++) {
 			if(! isTextChar(chars[i])) { 
 				if(chars[i] == '&' || chars[i] == '"') {
 					// Allow \& and \"
 					if(i == 0 || chars[i-1] != '\\') { 
 						// Allow XML references
 						int end = lexeme.indexOf(';', i);
 						if(end == -1) { return false; } // Invalid XML reference
 						String reference = lexeme.substring(i, end + 1);
 						if(reference.matches("&[a-zA-Z_:][a-zA-Z0-9.-_:]*;")
 								|| reference.matches("&#x[0-9a-fA-F]+;") 
 								|| reference.matches("&#[0-9]+;")) 
 						{ i += reference.length(); } // Continue check behind ;
 						else { return false;	} // Invalid XML reference
 					}
 				} else { return false; } // Unacceptable character
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static boolean isTextChar(char c) {
 		return c > 31 && c < 128 && c != '<' && c != '&' && c != '"' || c == '\n' || c == '\t' || c == '\r';
 	}
 	
 	/**
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isString(String lexeme) {
 		if(lexeme == null) { return false; }
 		char chars[] = lexeme.toCharArray();
 		
 		for(int i = 0; i < chars.length; i++) {
 			char c = chars[i]; // Retrieve current character
 			if(! isStringChar(c)) {
 				if(c == '\\') { // Allow "\\n" "\\t" "\\\"" "\\\\"
 					if(i+1 < chars.length) {
 						char peek = chars[i+1];
 						if(peek == 'n' || peek == 't' || peek == '"' || peek == '\\') {
 							i++; // Check checking \\ and accept
 						} else { return false; }
 					} else { return false; }
 				} else { return false; }
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * @param c
 	 * @return
 	 */
 	public static boolean isStringChar(char c) {
 		return c > 31 && c != '\n' && c != '\t' && c != '"' && c != '\\';
 	}
 	
 	/**
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isKeyword(String data) {
 		try {
 			// Literal should be in enumeration
 			WaebricKeyword literal = WaebricKeyword.valueOf(data.toUpperCase());
 			return literal != null;
 		} catch(IllegalArgumentException e) {
 			// Enumeration does not exists
 			return false;
 		}
 	}
 	
 	/**
 	 * Retrieve tokens
 	 * @return list
 	 */
 	public List<Token> getTokens() {
 		return tokens;
 	}
 	
 	/**
 	 * Retrieve token iterator
 	 * @return iterator
 	 */
 	public TokenIterator iterator() {
 		return new TokenIterator(tokens);
 	}
 	
 	/**
 	 * Modify current position
 	 * @param cpos
 	 */
 	public void setPosition(Position cpos) {
 		this.cpos = cpos;
 	}
 	
 }
