 package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.AND;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ASM;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ASSIGN;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.AUTO;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.BREAK;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.CASE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.CHAR;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.CHARLITERAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.COLON;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.COMMA;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.CONST;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.CONTINUE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DECREMENT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DEFAULT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DIVIDE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DIVIDEEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DO;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DOT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.DOUBLE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ELSE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ENDASM;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ENTRY;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.ENUM;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.EQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.EXTERN;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.FLOAT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.FOR;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.GOTO;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.GREAT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.GREATEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.IDENTIFIER;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.IF;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.INCREMENT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.INT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LEFTBRACKET;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LEFTPAREN;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LEFTSQUAREBRACKET;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LESS;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LESSEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.LONG;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.MINUS;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.MINUSEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.MOD;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.MODEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.NOT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.NULL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.NUMBERLITERAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.OR;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.PLUS;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.PLUSEQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.QUESTION;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.REGISTER;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.RETURN;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.RIGHTBRACKET;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.RIGHTPAREN;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.RIGHTSQUAREBRACKET;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SEMICOLON;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SHARP;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SHORT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SIGNED;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SIZEOF;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STAR;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STAREQUAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STATIC;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STRINGLITERAL;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.STRUCT;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.SWITCH;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.TYPEDEF;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.UNION;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.UNSIGNED;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.VOID;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.VOLATILE;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.WHILE;
 
 public class CLineLexer implements LineLexer {
 
 	@Override
 	public List<Token> lexFile(final String text) {
 		try {
 			final BufferedReader reader = new BufferedReader(new StringReader(
 					text));
 			final CLineLexer lexer = new CLineLexer();
 			final List<Token> tokens = new ArrayList<Token>();
 			String line;
 			int lineNumber = 1;
 			while (null != (line = reader.readLine())) {
 				for (final Token t : lexer.lexLine(line)) {
 					t.line = lineNumber;
 					tokens.add(t);
 				}
 				lineNumber++;
 			}
 
 			return tokens;
 
 		} catch (final Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 			return null;
 		}
 	}
 
 	@Override
 	public List<Token> lexLine(final String line) {
 		final List<Token> tokens = new ArrayList<Token>();
 		this.lex(new StringBuilder(line), tokens);
 		return tokens;
 	}
 
 	private void lex(final StringBuilder text, final List<Token> tokenList) {
 
 		if (0 == text.length()) {
 			return;
 		}
 
 		final String string = text.toString();
 		if (string.startsWith("-=")) {
 			text.delete(0, 2);
 			tokenList.add(new MINUSEQUAL());
 		} else if (string.startsWith("+=")) {
 			text.delete(0, 2);
 			tokenList.add(new PLUSEQUAL());
 		} else if (string.startsWith("/=")) {
 			text.delete(0, 2);
 			tokenList.add(new DIVIDEEQUAL());
 		} else if (string.startsWith("*=")) {
 			text.delete(0, 2);
 			tokenList.add(new STAREQUAL());
 		} else if (string.startsWith("%=")) {
 			text.delete(0, 2);
 			tokenList.add(new MODEQUAL());
 		} else if (string.startsWith("++")) {
 			text.delete(0, 2);
 			tokenList.add(new INCREMENT());
 		} else if (string.startsWith("--")) {
 			text.delete(0, 2);
 			tokenList.add(new DECREMENT());
 		} else if (string.startsWith("<=")) {
 			text.delete(0, 2);
 			tokenList.add(new LESSEQUAL());
 		} else if (string.startsWith(">=")) {
 			text.delete(0, 2);
 			tokenList.add(new GREATEQUAL());
 		} else if (string.startsWith("==")) {
 			text.delete(0, 2);
 			tokenList.add(new EQUAL());
//		} else if (string.startsWith("!")) {
//			text.delete(0, 1);
//			tokenList.add(new NOT());
 		}
 
 		else if (string.startsWith(":")) {
 			text.delete(0, 1);
 			tokenList.add(new COLON());
 		} else if (string.startsWith(";")) {
 			text.delete(0, 1);
 			tokenList.add(new SEMICOLON());
 		} else if (string.startsWith("=")) {
 			text.delete(0, 1);
 			tokenList.add(new ASSIGN());
 		} else if (string.startsWith("-")) {
 			text.delete(0, 1);
 			tokenList.add(new MINUS());
 		} else if (string.startsWith("+")) {
 			text.delete(0, 1);
 			tokenList.add(new PLUS());
 		} else if (string.startsWith("/")) {
 			text.delete(0, 1);
 			tokenList.add(new DIVIDE());
 		} else if (string.startsWith("*")) {
 			text.delete(0, 1);
 			tokenList.add(new STAR());
 		} else if (string.startsWith("%")) {
 			text.delete(0, 1);
 			tokenList.add(new MOD());
 		} else if (string.startsWith("?")) {
 			text.delete(0, 1);
 			tokenList.add(new QUESTION());
 		} else if (string.startsWith("<")) {
 			text.delete(0, 1);
 			tokenList.add(new LESS());
 		} else if (string.startsWith(">")) {
 			text.delete(0, 1);
 			tokenList.add(new GREAT());
 		} else if (string.startsWith("&")) {
 			text.delete(0, 1);
 			tokenList.add(new AND());
 		} else if (string.startsWith("|")) {
 			text.delete(0, 1);
 			tokenList.add(new OR());
 		} else if (string.startsWith("(")) {
 			text.delete(0, 1);
 			tokenList.add(new LEFTPAREN());
 		} else if (string.startsWith(")")) {
 			text.delete(0, 1);
 			tokenList.add(new RIGHTPAREN());
 		} else if (string.startsWith("{")) {
 			text.delete(0, 1);
 			tokenList.add(new LEFTBRACKET());
 		} else if (string.startsWith("}")) {
 			text.delete(0, 1);
 			tokenList.add(new RIGHTBRACKET());
 		} else if (string.startsWith("[")) {
 			text.delete(0, 1);
 			tokenList.add(new LEFTSQUAREBRACKET());
 		} else if (string.startsWith("]")) {
 			text.delete(0, 1);
 			tokenList.add(new RIGHTSQUAREBRACKET());
 		} else if (string.startsWith(",")) {
 			text.delete(0, 1);
 			tokenList.add(new COMMA());
 		} else if (string.startsWith(".")) {
 			text.delete(0, 1);
 			tokenList.add(new DOT());
 		} else if (string.startsWith("#")) {
 			text.delete(0, 1);
 			tokenList.add(new SHARP());
 		}
 
 		else if ('\"' == string.charAt(0)) {
 			int index = 1;
 			while (index < string.length()) {
 				if ('\"' == string.charAt(index)) {
 					break;
 				}
 				index++;
 			}
 			final String value = text.substring(1, index);
 			text.delete(0, index + 1);
 			tokenList.add(new STRINGLITERAL(value));
 		}
 
 		else if ('\'' == string.charAt(0)) {
 			int index = 1;
 			while (index < string.length()) {
 				if ('\'' == string.charAt(index)) {
 					break;
 				}
 				index++;
 			}
 			final String value = text.substring(1, index);
 			text.delete(0, index + 1);
 			tokenList.add(new CHARLITERAL(value));
 		}
 
 		else if ('/' == string.charAt(0)) {
 
 			if ((2 <= string.length()) && ('/' == string.charAt(1))) {
 				return;
 			}
 		}
 
 		else if (isDigit(string.charAt(0))) {
 			int index = 1;
 			while (index < string.length()) {
 				if (!isDigit(string.charAt(index))) {
 					break;
 				}
 				index++;
 			}
 			text.delete(0, index);
 			final String sconstant = string.substring(0, index);
 			tokenList.add(new NUMBERLITERAL(sconstant));
 		}
 
 		else if (isAlphabet(string.charAt(0))) {
 			int index = 1;
 			while (index < string.length()) {
 				if (!isAlphabet(string.charAt(index))
 						&& !isDigit(string.charAt(index))
 						&& '_' != string.charAt(index)
 						&& '$' != string.charAt(index)) {
 					break;
 				}
 				index++;
 			}
 			text.delete(0, index);
 			final String identifier = string.substring(0, index);
 
 			if (identifier.equals("asm")) {
 				tokenList.add(new ASM());
 			} else if (identifier.equals("auto")) {
 				tokenList.add(new AUTO());
 			} else if (identifier.equals("break")) {
 				tokenList.add(new BREAK());
 			} else if (identifier.equals("case")) {
 				tokenList.add(new CASE());
 			} else if (identifier.equals("char")) {
 				tokenList.add(new CHAR());
 			} else if (identifier.equals("const")) {
 				tokenList.add(new CONST());
 			} else if (identifier.equals("continue")) {
 				tokenList.add(new CONTINUE());
 			} else if (identifier.equals("default")) {
 				tokenList.add(new DEFAULT());
 			} else if (identifier.equals("do")) {
 				tokenList.add(new DO());
 			} else if (identifier.equals("double")) {
 				tokenList.add(new DOUBLE());
 			} else if (identifier.equals("else")) {
 				tokenList.add(new ELSE());
 			} else if (identifier.equals("endasm")) {
 				tokenList.add(new ENDASM());
 			} else if (identifier.equals("entry")) {
 				tokenList.add(new ENTRY());
 			} else if (identifier.equals("enum")) {
 				tokenList.add(new ENUM());
 			} else if (identifier.equals("extern")) {
 				tokenList.add(new EXTERN());
 			} else if (identifier.equals("float")) {
 				tokenList.add(new FLOAT());
 			} else if (identifier.equals("for")) {
 				tokenList.add(new FOR());
 			} else if (identifier.equals("goto")) {
 				tokenList.add(new GOTO());
 			} else if (identifier.equals("if")) {
 				tokenList.add(new IF());
 			} else if (identifier.equals("int")) {
 				tokenList.add(new INT());
 			} else if (identifier.equals("long")) {
 				tokenList.add(new LONG());
 			} else if (identifier.equals("NULL")) {
 				tokenList.add(new NULL());
 			} else if (identifier.equals("register")) {
 				tokenList.add(new REGISTER());
 			} else if (identifier.equals("return")) {
 				tokenList.add(new RETURN());
 			} else if (identifier.equals("short")) {
 				tokenList.add(new SHORT());
 			} else if (identifier.equals("signed")) {
 				tokenList.add(new SIGNED());
 			} else if (identifier.equals("sizeof")) {
 				tokenList.add(new SIZEOF());
 			} else if (identifier.equals("static")) {
 				tokenList.add(new STATIC());
 			} else if (identifier.equals("struct")) {
 				tokenList.add(new STRUCT());
 			} else if (identifier.equals("switch")) {
 				tokenList.add(new SWITCH());
 			} else if (identifier.equals("typedef")) {
 				tokenList.add(new TYPEDEF());
 			} else if (identifier.equals("union")) {
 				tokenList.add(new UNION());
 			} else if (identifier.equals("unsigned")) {
 				tokenList.add(new UNSIGNED());
 			} else if (identifier.equals("void")) {
 				tokenList.add(new VOID());
 			} else if (identifier.equals("volatile")) {
 				tokenList.add(new VOLATILE());
 			} else if (identifier.equals("while")) {
 				tokenList.add(new WHILE());
 			} else {
 				tokenList.add(new IDENTIFIER(identifier));
 			}
 		}
 
 		else if (' ' == string.charAt(0) || '\t' == string.charAt(0)) {
 			text.deleteCharAt(0);
 		}
 
 		else {
 			// assert false : "unexpected situation: " + string;
 			text.delete(0, 1);
 		}
 
 		lex(text, tokenList);
 	}
 
 	private static boolean isAlphabet(final char c) {
 		return Character.isLowerCase(c) || Character.isUpperCase(c);
 	}
 
 	private static boolean isDigit(final char c) {
 		return '0' == c || '1' == c || '2' == c || '3' == c || '4' == c
 				|| '5' == c || '6' == c || '7' == c || '8' == c || '9' == c;
 	}
 }
