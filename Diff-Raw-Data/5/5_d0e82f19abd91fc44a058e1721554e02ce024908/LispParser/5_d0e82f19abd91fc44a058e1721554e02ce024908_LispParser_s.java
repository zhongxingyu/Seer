 package com.cse755;
 
 import java.text.ParseException;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Stack;
 
 import com.cse755.Token.TokenType;
 
 /**
  * Parses a series of {@link Token}s and creates a binary tree representation of
  * the S-Expressions.
  * 
  * @author Dan Ziemba
  */
 public class LispParser {
 
 	public enum Symbol {
 		// Terminal symbols
 		TS_ATOM, TS_DOT, TS_C_PAREN, TS_O_PAREN, TS_INVALID, TS_EOS,
 		// Non-terminal symbols
 		NTS_E, NTS_R, NTS_S, NTS_X, NTS_Y;
 
 		public static Symbol lexer(Token t) {
 			switch (t.type) {
 			case NUMBER:
 				return TS_ATOM;
 			case WORD:
 				return TS_ATOM;
 			case DOT:
 				return TS_DOT;
 			case CLOSE_PAREN:
 				return TS_C_PAREN;
 			case OPEN_PAREN:
 				return TS_O_PAREN;
 			case EOF:
 				return TS_EOS;
 			default:
 				return TS_INVALID; // Should never reach this
 			}
 		}
 	}
 
 	private LispTokenizer tk;
 	private Stack<Symbol> ss;
 	private Map<Symbol, Map<Symbol, Integer>> table;
 	private Token currentT;
 	private Symbol lastS;
 	private SExpression seRoot;
 	private SExpression seWorking;
 
 	private void buildParseTable() {
 		table = new EnumMap<Symbol, Map<Symbol, Integer>>(Symbol.class);
 		Map<Symbol, Integer> innerMap;
 		// NTS_E
 		innerMap = new EnumMap<Symbol, Integer>(Symbol.class);
 		innerMap.put(Symbol.TS_ATOM, 2);
 		innerMap.put(Symbol.TS_O_PAREN, 3);
 		table.put(Symbol.NTS_E, innerMap);
 		// NTS_R
 		innerMap = new EnumMap<Symbol, Integer>(Symbol.class);
 		innerMap.put(Symbol.TS_ATOM, 8);
 		innerMap.put(Symbol.TS_C_PAREN, 9);
 		innerMap.put(Symbol.TS_O_PAREN, 8);
 		table.put(Symbol.NTS_R, innerMap);
 		// NTS_S
 		innerMap = new EnumMap<Symbol, Integer>(Symbol.class);
 		innerMap.put(Symbol.TS_ATOM, 1);
 		innerMap.put(Symbol.TS_O_PAREN, 1);
 		table.put(Symbol.NTS_S, innerMap);
 		// NTS_X
 		innerMap = new EnumMap<Symbol, Integer>(Symbol.class);
 		innerMap.put(Symbol.TS_ATOM, 5);
 		innerMap.put(Symbol.TS_C_PAREN, 4);
 		innerMap.put(Symbol.TS_O_PAREN, 5);
 		table.put(Symbol.NTS_X, innerMap);
 		// NTS_Y
 		innerMap = new EnumMap<Symbol, Integer>(Symbol.class);
 		innerMap.put(Symbol.TS_DOT, 6);
 		innerMap.put(Symbol.TS_ATOM, 7);
 		innerMap.put(Symbol.TS_C_PAREN, 7);
 		innerMap.put(Symbol.TS_O_PAREN, 7);
 		table.put(Symbol.NTS_Y, innerMap);
 	}
 
 	private void buildSexpression() {
 		Symbol currentS = Symbol.lexer(currentT);
 		switch (currentS) {
 		case TS_ATOM:
 			if (lastS == null) {
 				// First symbol was atom
 				seWorking.setAtom(new Atom(currentT));
 			} else if (lastS == Symbol.TS_O_PAREN) {
 				// First atom inside '(', will be left child
 				seWorking.setLeftChild(new SExpression(new Atom(currentT)));
 			} else if (lastS == Symbol.TS_ATOM || lastS == Symbol.TS_C_PAREN) {
 				// Last symbol was atom or ')', will be right child
 				seWorking.setRightChild(new SExpression());
 				seWorking = seWorking.getRightChild();
 				seWorking.setLeftChild(new SExpression(new Atom(currentT)));
 			} else if (lastS == Symbol.TS_DOT) {
 				// Last symbol was '.', put direct in right child
 				seWorking.setRightChild(new SExpression(new Atom(currentT)));
				// Climb tree to lowest parent with empty right child
				while (seWorking.getRightChild() != null
						&& seWorking.getParent() != null) {
					seWorking = seWorking.getParent();
				}
 			}
 			break;
 		case TS_DOT:
 			seWorking.setHasDot(true);
 			break;
 		case TS_C_PAREN:
 			if (lastS == Symbol.TS_O_PAREN) {
 				// Empty '()' == NIL
 				seWorking.setAtom(new Atom(currentT.lineNumber));
 			} else {
 				// Must be following an atom or ')'
 				if (seWorking.getRightChild() == null) {
 					if (!seWorking.hasDot()) {
 						// Don't re-set, could have already been set because of
 						// '.'
 						seWorking.setRightChild(new SExpression(new Atom(
 								currentT.lineNumber)));
 					}
 				}
 				// Climb tree to lowest parent with empty right child
 				while (seWorking.getRightChild() != null
 						&& seWorking.getParent() != null) {
 					seWorking = seWorking.getParent();
 				}
 			}
 			break;
 		case TS_O_PAREN:
 			if (lastS == Symbol.TS_ATOM || lastS == Symbol.TS_C_PAREN) {
 				// Should be on right side, need 2 levels
 				seWorking.setRightChild(new SExpression());
 				seWorking = seWorking.getRightChild();
 				seWorking.setLeftChild(new SExpression());
 				seWorking = seWorking.getLeftChild();
 			} else if (lastS == Symbol.TS_O_PAREN) {
 				// Should be on left side
 				seWorking.setLeftChild(new SExpression());
 				seWorking = seWorking.getLeftChild();
 			} else if (lastS == Symbol.TS_DOT) {
 				// Should be on right side
 				seWorking.setRightChild(new SExpression());
 				seWorking = seWorking.getRightChild();
 			}
 			break;
 		case TS_EOS:
 
 			break;
 
 		default:
 			// Should never reach here
 			System.out.println("WARNING: Invalid Symbol encountered!"
 					+ "This should never happen!");
 			break;
 		}
 
 		// Set last symbol
 		lastS = currentS;
 	}
 
 	private void executeRule(Integer rule) {
 		switch (rule) {
 		case 1: // <S> ::= <E>
 			ss.pop();
 			ss.push(Symbol.NTS_E);
 			break;
 		case 2: // <E> ::= atom
 			ss.pop();
 			ss.push(Symbol.TS_ATOM);
 			break;
 		case 3: // <E> ::= (<X>
 			ss.pop();
 			ss.push(Symbol.NTS_X);
 			ss.push(Symbol.TS_O_PAREN);
 			break;
 		case 4: // <X> ::= )
 			ss.pop();
 			ss.push(Symbol.TS_C_PAREN);
 			break;
 		case 5: // <X> ::= <E><Y>
 			ss.pop();
 			ss.push(Symbol.NTS_Y);
 			ss.push(Symbol.NTS_E);
 			break;
 		case 6: // <Y> ::= .<E>)
 			ss.pop();
 			ss.push(Symbol.TS_C_PAREN);
 			ss.push(Symbol.NTS_E);
 			ss.push(Symbol.TS_DOT);
 			break;
 		case 7: // <Y> ::= <R>)
 			ss.pop();
 			ss.push(Symbol.TS_C_PAREN);
 			ss.push(Symbol.NTS_R);
 			break;
 		case 8: // <R> ::= <E><R>
 			ss.pop();
 			ss.push(Symbol.NTS_R);
 			ss.push(Symbol.NTS_E);
 			break;
 		case 9: // <R> ::= empty
 			ss.pop();
 			break;
 
 		default:
 			// Should never reach here
 			System.out.println("WARNING: Invalid rule encountered!"
 					+ "This should never happen!");
 			break;
 		}
 	}
 
 	/**
 	 * Creates a new LispParser with the given LispTokenizer
 	 * 
 	 * @param tk
 	 *            LispTokenizer that should be at its starting position
 	 */
 	public LispParser(LispTokenizer tk) {
 		this.tk = tk;
 		buildParseTable();
 	}
 
 	public SExpression getNextSExpression() throws ParseException {
 		// Init stack
 		ss = new Stack<LispParser.Symbol>();
 		ss.push(Symbol.TS_EOS);
 		ss.push(Symbol.NTS_S);
 		// Prepare first token
 		if (tk.getToken() == null) {
 			tk.prepareNext();
 		}
 		// Create empty s-expression
 		seRoot = new SExpression();
 		seWorking = seRoot;
 		// Clear last symbol
 		lastS = null;
 		// Return null if starting with empty file
 		if (tk.getToken().type == TokenType.EOF) {
 			return null;
 		}
 
 		while (ss.size() > 0 && ss.peek() != Symbol.TS_EOS) {
 			currentT = tk.getToken();
 			if (Symbol.lexer(currentT).equals(ss.peek())) {
 				// Matched symbol with top of stack
 				tk.prepareNext();
 				ss.pop();
 				buildSexpression();
 			} else {
 				// Get rule for NTS on stack, or report error
 				Map<Symbol, Integer> innerMap;
 				if ((innerMap = table.get(ss.peek())) != null) {
 					Integer rule;
 					if ((rule = innerMap.get(Symbol.lexer(currentT))) != null) {
 						executeRule(rule);
 					} else {
 						// Didn't match rule
 						throw new ParseException(
 								"Input doesn't match Lisp grammar", 0);
 					}
 				} else {
 					// Top of stack was not NTS
 					throw new ParseException("Unexpected error while parsing",
 							0);
 				}
 			}
 		}
 
 		return seRoot;
 	}
 
 }
