 package org.libtoil.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;

import antlr.Parser;

 public class TemplateTokenizer {
 	char[] src;
 	public enum TokenType {
 		EOL,
 		LITERALAT,
 		WHITESPACE,
 		TEXTLITERAL, 
 		LINECONTINUATION, 
 		COMMENT, 
 		COMMAND,
 		TEMPLATE,
 		LINE
 	}
 
 	TemplateTokenizer(char[] src) {
 		this.src = src;
 	}
 	
 	TemplateTokenizer(String srcString) {
 		this.src = srcString.toCharArray();
 	}
 	
 	public SyntaxTreeNode parseTree() {
 		List<Token> l = parseTokens();
 		l = removeLineContinuations(l);
 		l = insertStartTokens(l);
 		SyntaxTreeNode root = null;
 		SyntaxTreeNode line = null;
 		for (Token t: l) {
 			if (t.type == TokenType.TEMPLATE) {
 				root = new SyntaxTreeNode(t);
 			} else if (t.type == TokenType.LINE) {
 				line = new SyntaxTreeNode(t);
 				root.addChild(line);
 			} else {
 				line.addChild(new SyntaxTreeNode(t));
 			}
 		}
 		return root;
 	}
 	
 	public class Token {
 		public TokenType type;
 		public int start, end;
 		
 		public Token(TokenType type, int start, int end) {
 			this.type = type;
 			this.start = start;
 			this.end = end;
 		}
 		
 		public char[] getSource() {
 			return src;
 		}
 		
 		public String toString() {
 			return new String(src, start, end-start);
 		}
 	}
 	
 	List<Token> parseTokens() {
 		List<Token> result = new ArrayList<Token>();
 		int start = 0;
 		do {
 			Token t = new Token(null, start, start);
 			char c;
 			do {
 				c = (t.end < src.length) ? src[t.end] : 0;
 				t.end++;
 			} while (!classify(t, c));
 			result.add(t);
 			start = t.end;
 		} while (start < src.length);
 		return result;
 	}
 		
 	/**
 	 * Called for each character to be 
 	 * @param t the Token currently being processed.  
 	 * @param c the current character being read from the template source
 	 * @return  true if the Token is complete
 	 */
 	private static boolean classify(Token t, char c) {
 		if (t.type == TokenType.LITERALAT) {
 			return classifyLiteralAt(t, c);
 		} else if (t.type == TokenType.COMMENT) {
 			return classifyComment(t, c);
 		} else if (t.type == TokenType.WHITESPACE) {
 			return classifyWhitespace(t, c);
 		} else if (t.type == TokenType.TEXTLITERAL) {
 			return classifyTextLiteral(t, c);
 		} else if (t.type == TokenType.COMMAND) {
 			return classifyCommand(t, c);
 		} else if (t.type == null) { 
 			return classifyNull(t, c);
 		} else {
 			throw new RuntimeException("unreachable code");
 		}
 	}
 		
 	private static boolean classifyLiteralAt(Token t, char c) {
 		if (c == '#') {
 			t.type = TokenType.COMMENT;
 		} else if (c == '@') {
 			return true;
 		} else if (Character.isWhitespace(c) || c == '\n' || c == 0) {
 			t.end--;
 			return true;
 		} else if (c == '\\') {
 			t.type = TokenType.LINECONTINUATION;
 			return true;
 		} else {
 			t.type = TokenType.COMMAND;
 		}
 		return false;
 	}
 
 	private static boolean classifyComment(Token t, char c) {
 		if (c == '\n' || c == 0) {
 			t.end--;
 			return true;
 		} 
 		return false;
 	}
 	
 	private static boolean classifyWhitespace(Token t, char c) {
 		if (c == '\n' || !Character.isWhitespace(c) || c == 0) {
 			t.end--;
 			return true;
 		} 
 		return false;
 	}
 	
 	private static boolean classifyTextLiteral(Token t, char c) {
 		if (Character.isWhitespace(c) || c == '@' || c == 0) {
 			t.end--;
 			return true;
 		} 
 		return false;
 	}
 	
 	private static boolean classifyCommand(Token t, char c) {
 		if (Character.isWhitespace(c) || c == 0) {
 			t.end = t.start + 1;
 			t.type = TokenType.LITERALAT;
 			return true;
 		} else if (c == '@') {
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean classifyNull(Token t, char c) {
 		if (c == '\n') {
 			t.type = TokenType.EOL;
 			t.end = t.start + 1;
 			return true;
 		} else if (c == '@') {
 			t.type = TokenType.LITERALAT;
 		} else if (Character.isWhitespace(c)) {
 			t.type = TokenType.WHITESPACE;
 		} else {
 			t.type = TokenType.TEXTLITERAL;
 		}
 		return false;
 	}
 	
 	
 	List<Token> removeLineContinuations(List<Token> l) {
 		List<Token> result = new ArrayList<Token>();
 		Iterator<Token> it = l.iterator();
 		while (it.hasNext()) {
 			Token t = it.next();
 			if (t.type == TokenType.LINECONTINUATION) {
 				Token continuationToken = t;
 				do {
 					if (!it.hasNext()) {
 						throw new TemplateParseError(
 				"Invalid line continuation", src, continuationToken.start);
 					}
 					t = it.next();
 				} while (t.type == TokenType.WHITESPACE);
 				if (t.type != TokenType.EOL) {
 					throw new TemplateParseError(
 							"Invalid line continuation", src, continuationToken.start);
 				}
 				if (!it.hasNext()) {
 					throw new TemplateParseError(
 							"Invalid line continuation", src, continuationToken.start);
 				}
 			} else {
 				result.add(t);
 			}
 		}
 		return result;
 	}
 
 		public List<Token> insertStartTokens(List<Token> l) {
 			List<Token> result = new ArrayList<Token>();
 			result.add(new Token(TokenType.TEMPLATE, 0, 0));
 			result.add(new Token(TokenType.LINE, 0, 0));
 			for(Token t: l) {
 				result.add(t);
 				if (t.type == TokenType.EOL) {
 					result.add(new Token(TokenType.LINE, t.end, t.end));
 				}
 			}
 			return result;
 		}
 
 }
