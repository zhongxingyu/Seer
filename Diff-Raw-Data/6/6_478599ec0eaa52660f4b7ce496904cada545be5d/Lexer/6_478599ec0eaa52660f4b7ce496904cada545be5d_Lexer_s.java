 package edu.kit.pp.minijava;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 
 public class Lexer {
 	private PushbackReader _reader;
 
 	public Lexer(Reader reader) throws IOException {
 		_reader = new PushbackReader(new BufferedReader(reader));
 	}
 
 	public Token next() throws IOException {
 		while (true) {
 			int c = read();
 			switch (c) {
 			case ' ':
 			case '\n':
 			case '\r':
 			case '\t':
 				break;
 			case '/':
 				c = read();
 				switch (c) {
 				case '*':
 					skipComment();
 					break;
 				case '=':
 					return new Token("/=");
 				default:
 					unread(c);
 					return new Token("/");
 				}
 			case '!':
 				return ifThenElse('=', "!=", "!");
 			case '(':
 				return new Token("(");
 			case ')':
 				return new Token(")");
 			case '*':
 				return ifThenElse('=', "*=", "*");
 			case '+':
 				c = read();
 				switch (c) {
 				case '+': return new Token("++");
 				case '=': return new Token("+=");
 				default:
 					unread(c);
 					return new Token("+");
 				}
 			case ',':
 				return new Token(",");
 			case '-':
 				c = read();
 				switch (c) {
 				case '-': return new Token("--");
 				case '=': return new Token("-=");
 				default:
 					unread(c);
 					return new Token("-");
 				}
 			case '.':
 				return new Token(".");
 			case ':':
 				return new Token(":");
 			case ';':
 				return new Token(";");
 			case '<':
 				c = read();
 				switch (c) {
 				case '<':
 					return ifThenElse('=', "<<=", "<<");
 				case '=':
 					return new Token("<=");
 				default:
 					unread(c);
 					return new Token("<");
 				}
 			case '=':
 				return ifThenElse('=', "==", "=");
 			case '>':
 				c = read();
 				switch (c) {
 				case '>':
 					c = read();
 					switch (c) {
 					case '>':
 						return ifThenElse('=', ">>>=", ">>>");
 					case '=':
 						return new Token(">>=");
 					default:
 						unread(c);
 						return new Token(">>");
 					}
 				case '=':
 					return new Token(">=");
 				default:
 					unread(c);
 					return new Token(">");
 				}
 			case '?':
 				return new Token("?");
 			case '%':
 				return ifThenElse('=', "%=", "%");
 			case '&':
 				c = read();
 				switch (c) {
 				case '&':
 					return new Token("&&");
 				case '=':
 					return new Token("&=");
 				default:
 					unread(c);
 					return new Token("&");
 				}
 			case '[':
 				return new Token("[");
 			case ']':
 				return new Token("]");
 			case '^':
 				return ifThenElse('=', "^=", "^");
 			case '{':
 				return new Token("{");
 			case '}':
 				return new Token("}");
 			case '~':
 				return new Token("~");
 			case '|':
 				c = read();
 				switch (c) {
 				case '|':
 					return new Token("||");
 				case '=':
 					return new Token("|=");
 				default:
 					unread(c);
 					return new Token("|");
 				}
 			case -1: return null; /*return new Token("EOF");*/
 			default:
 				if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
 					return lexIdentifier(c);
 				}
 				else if (c >= '0' && c <= '9') {
 					return lexInteger(c);
 				}
 				else {
 					return new Token("MIST");
 				}
 			}
 		}
 	}
 
 	private void skipComment() throws IOException {
 		while (true) {
 			int c = read();
 			switch (c) {
 			case '*':
 				c = read();
 				if (c == '/') return;
 			case -1: return;
 			default: ;
 			}
 		}
 	}
 
 	private Token lexIdentifier(int c) throws IOException {
 		StringBuffer name = new StringBuffer();
 		name.append((char) c);
 		while (true) {
 			c = read();
 			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || (c >= '0' && c <= '9')) {
 				name.append((char) c);
 			}
 			else {
 				unread(c);
 				return new Token(name.toString());
 			}
 		}
 	}
 
 	private Token lexInteger(int c) throws IOException {
 		StringBuffer name = new StringBuffer();
 		name.append((char) c);
 		if (c == '0') {
 			return new Token(name.toString());
 		}
 		else {
 			while (true) {
 				c = read();
 				if (c >= '0' && c <= '9') {
 					name.append((char) c);
 				}
 				else {
 					unread(c);
 					return new Token(name.toString());
 				}
 			}
 		}
 	}
 
 	private int read() throws IOException {
 		return _reader.read();
 	}
 
 	private void unread(int c) throws IOException {
 		_reader.unread(c);
 	}
 
 	private Token ifThenElse(int c, String t1, String t2) throws IOException {
		c = read();
		if (c == '=') return new Token(t1);
		else unread(c);
 		return new Token(t2);
 	}
 }
