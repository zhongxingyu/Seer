 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * Implementation of the Scanner for JAY
  * 
  * @author Allon Hadaya, Keith McPherson
  */
 public class TokenStream {
 
 	private BufferedReader input;
 	private boolean isEof = false;
 	private char nextChar = ' ';
 
 	/**
 	 * Construct a new TokenStream that reads from fileName
 	 * 
 	 * @param fileName The file to be scanned
 	 */
 	public TokenStream(String fileName) {
 		try {
 			input = new BufferedReader(new FileReader(fileName));
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found: " + fileName);
 			isEof = true;
 		}
 	}
 
 	/**
 	 * @return Returns the next token type and value
 	 */
 	public Token nextToken() {
 
 		skipWhiteSpace();
 
 		Token t = new Token();
 
 		// comment or /
 		if (nextChar == '/') {
 			t.setValue(t.getValue() + nextChar);
 			readChar();
 			if (nextChar == '/') {
 				while (!isEndOfLine(nextChar)) {
 					readChar();
 				}
 				readChar();
 				t = nextToken();
 			} else {
 				t.setType("Operator");
 			}
 			return t;
 		}
 
 		// &&
 		if (nextChar == '&') {
 			t.setValue(t.getValue() + nextChar);
 			readChar();
 			if (nextChar == '&') {
 				t.setType("Operator");
 				t.setValue(t.getValue() + nextChar);
 				readChar();
 			} else {
 				collectOtherToken(t);
 			}
 			return t;
 		}
 
 		// ||
 		if (nextChar == '|') {
 			t.setValue(t.getValue() + nextChar);
 			readChar();
 			if (nextChar == '|') {
 				t.setType("Operator");
 				t.setValue(t.getValue() + nextChar);
 				readChar();
 			} else {
 				collectOtherToken(t);
 			}
 			return t;
 		}
 
 		// <, >, !, =, <=, >=, !=, ==, +, -, *, or !
 		if (isOperator(nextChar)) {
 			t.setType("Operator");
 			t.setValue(t.getValue() + nextChar);
 			switch (nextChar) {
 			case '<':
 			case '>':
 			case '!':
 			case '=':
 				readChar();
 				if (nextChar == '=') { // matches <=, >=, !=, ==
 					t.setValue(t.getValue() + nextChar);
 					readChar();
 				}
 				break;
 			default: // other 1 character operators
 				readChar();
 				break;
 			}
 			return t;
 		}
 
 		// separator
 		if (isSeparator(nextChar)) {
 			t.setType("Separator");
 			t.setValue(t.getValue() + nextChar);
 			readChar();
 			return t;
 		}
 
 		// identifier, keyword, or literal.
 		if (isLetter(nextChar)) {
 			t.setType("Identifier");
 
 			while ((isLetter(nextChar) || isDigit(nextChar))) {
 				t.setValue(t.getValue() + nextChar);
 				readChar();
 			}
 
 			if (isKeyword(t.getValue())) {
 				t.setType("Keyword");
 			}
 			if (isBooleanLiteral(t.getValue())) {
				t.setType("Literal");
 			}
 			if (!isEndOfToken()) {
 				collectOtherToken(t);
 			}
 			return t;
 		}
 
 		// IntegerLiteral
 		if (isDigit(nextChar)) {
			t.setType("Literal");
 
 			while (isDigit(nextChar)) {
 				t.setValue(t.getValue() + nextChar);
 				readChar();
 			}
 
 			if (!isEndOfToken()) {
 				collectOtherToken(t);
 			}
 
 			return t;
 		}
 
 		// Other
 		collectOtherToken(t);
 
 		return t;
 	}
 
 	/**
 	 * Sets nextChar to the next character in input.
 	 */
 	private void readChar() {
 		int next = 0;
 		if (!isEof) {
 			try {
 				next = input.read();
 			} catch (IOException e) {
 				System.exit(-1);
 			}
 			if (next == -1) {
 				isEof = true;
 				next = 0;
 			}
 		}
 		nextChar = (char) next;
 	}
 
 	private boolean isKeyword(String s) {
 		return s.matches("boolean|else|if|int|main|void|while");
 	}
 
 	private boolean isBooleanLiteral(String s) {
 		return s.matches("true|false");
 	}
 
 	private boolean isSeparator(char c) {
 		return String.valueOf(c).matches("[(){};,]");
 	}
 
 	private boolean isOperator(char c) {
 		return String.valueOf(c).matches("(=|\\+|-|\\*|/|<|>|!)");
 	}
 
 	private boolean isLetter(char c) {
 		return String.valueOf(c).matches("[a-zA-Z]");
 	}
 
 	private boolean isDigit(char c) {
 		return String.valueOf(c).matches("[0-9]");
 	}
 
 	private boolean isEndOfToken() {
 		return (isWhiteSpace(nextChar) || isOperator(nextChar) || isSeparator(nextChar) || isEof);
 	}
 
 	private boolean isWhiteSpace(char c) {
 		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
 	}
 
 	private boolean isEndOfLine(char c) {
 		return (c == '\r' || c == '\n' || c == '\f');
 	}
 
 	public boolean isEoF() {
 		return isEof;
 	}
 
 	public boolean isEndofFile() {
 		return isEof;
 	}
 
 	public boolean isEoFile() {
 		return isEof;
 	}
 
 	private void skipWhiteSpace() {
 		while (!isEof && isWhiteSpace(nextChar)) {
 			readChar();
 		}
 	}
 
 	private void collectOtherToken(Token t) {
 		while (!isEndOfToken()) {
 			t.setValue(t.getValue() + nextChar);
 			readChar();
 		}
 	}
 }
