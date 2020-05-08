 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.araqne.logdb.LogQueryParseException;
 import org.araqne.logdb.query.expr.Expression;
 
 public class ExpressionParser {
 
 	public static Expression parse(String s, ParsingRule r) {
 		if (s == null)
 			throw new IllegalArgumentException("expression string should not be null");
 
 		s = s.replaceAll("\t", "    ");
 		List<Term> terms = tokenize(s, r);
 		List<Term> output = convertToPostfix(terms, r);
 		Stack<Expression> exprStack = new Stack<Expression>();
 		OpEmitterFactory of = r.getOpEmmiterFactory();
 		TermEmitterFactory tf = r.getTermEmitterFactory();
 		FuncEmitterFactory ff = r.getFuncEmitterFactory();
 		
 		for (Term term : output) {
 			if (r.getOpTerm().isInstance(term)) {
 				of.emit(exprStack, term);
 			} else if (term instanceof TokenTerm) {
 				// parse token expression (variable or numeric constant)
 				TokenTerm t = (TokenTerm) term;
 				tf.emit(exprStack, t);
 			} else if (term instanceof FuncTerm) {
 				// parse function expression
 				FuncTerm f = (FuncTerm) term;
 				ff.emit(exprStack, f);
 			} else {
				throw new IllegalStateException("Unexpected term : " + term.toString());
 			}
 		}
 
 		return exprStack.pop();
 	}
 	
 	private static ParsingRule evalRule = new ParsingRule(EvalOpTerm.NOP, new EvalOpEmitterFactory(), new EvalFuncEmitterFactory(), new EvalTermEmitterFactory());
 
 	public static Expression parse(String s) {
 		return parse(s, evalRule);
 	}
 
 	private static List<Term> convertToPostfix(List<Term> tokens, ParsingRule rule) {
 		Stack<Term> opStack = new Stack<Term>();
 		List<Term> output = new ArrayList<Term>();
 
 		int i = 0;
 		int len = tokens.size();
 		
 		OpTerm opTerm = rule.getOpTerm();
 		while (i < len) {
 			Term token = tokens.get(i);
 
 			if (isDelimiter(token, rule)) {
 				// need to pop operator and write to output?
 				while (needPop(token, opStack, output, rule)) {
 					Term last = opStack.pop();
 					output.add(last);
 				}
 
 				if (opTerm.isInstance(token) || token instanceof FuncTerm) {
 					opStack.add(token);
 				} else if (((TokenTerm) token).getText().equals("(")) {
 					opStack.add(token);
 				} else if (((TokenTerm) token).getText().equals(")")) {
 					boolean foundMatchParens = false;
 					while (!opStack.isEmpty()) {
 						Term last = opStack.pop();
 						if (last instanceof TokenTerm && ((TokenTerm) last).getText().equals("(")) {
 							foundMatchParens = true;
 							break;
 						} else {
 							output.add(last);
 						}
 					}
 
 					if (!foundMatchParens)
 						throw new LogQueryParseException("parens-mismatch", -1);
 					
 					// postprocess for closed parenthesis
 					
 					// postprocess function term
 					if (!opStack.empty()) {
 						Term last = opStack.pop();
 						if (last instanceof FuncTerm) {
 							output.add(last);
 						} else {
 							opStack.push(last);
 						}
 					}
 
 					// postprocess comma term
 					// Being closed by parenthesis means the comma list is ended.
 					if (!output.isEmpty()) {
 						Term recent = output.get(output.size() - 1);
 						if (recent instanceof OpTerm) {
 							OpTerm recentOp = (OpTerm) recent;
 							output.set(output.size() - 1, recentOp.postProcessCloseParen());
 						}
 					}
 				}
 			} else {
 				output.add(token);
 			}
 
 			i++;
 		}
 
 		// last operator flush
 		while (!opStack.isEmpty()) {
 			Term op = opStack.pop();
 			output.add(op);
 		}
 
 		return output;
 	}
 
 	private static boolean needPop(Term token, Stack<Term> opStack, List<Term> output, ParsingRule rule) {
 		if (!(rule.getOpTerm().isInstance(token)))
 			return false;
 
 		OpTerm currentOp = (OpTerm) token;
 
 		int precedence = currentOp.getPrecedence();
 		boolean leftAssoc = currentOp.isLeftAssoc();
 
 		OpTerm lastOp = null;
 		if (!opStack.isEmpty()) {
 			Term t = opStack.peek();
 			if (!(t instanceof OpTerm)) {
 				return false;
 			}
 			lastOp = (OpTerm) t;
 		} else {
 			return false;
 		}
 
 		if (leftAssoc && precedence <= lastOp.getPrecedence())
 			return true;
 
 		if (precedence < lastOp.getPrecedence())
 			return true;
 
 		return false;
 	}
 
 	private static boolean isOperator(String token, ParsingRule rule) {
 		if (token == null)
 			return false;
 
 		String o = token.trim();
 
 		if (o.equals("(") || o.equals(")"))
 			return true;
 
 		if (rule.getOpTerm().parse(o) != null)
 			return true;
 		
 		return false;
 	}
 
 	public static List<Term> tokenize(String s, ParsingRule rule) {
 		return tokenize(s, 0, s.length() - 1, rule);
 	}
 
 	private static List<Term> tokenize(String s, int begin, int end, ParsingRule rule) {
 		List<Term> tokens = new ArrayList<Term>();
 
 		String lastToken = null;
 		int next = begin;
 		while (true) {
 			ParseResult r = nextToken(s, next, end, rule);
 			if (r == null)
 				break;
 
 			String token = (String) r.value;
 			if (token.isEmpty())
 				continue;
 
 			// read function call (including nested one)
 			if (token.equals("(") && lastToken != null && !isOperator(lastToken, rule)) {
 				// remove last term and add function term instead
 				tokens.remove(tokens.size() - 1);
 				tokens.add(new FuncTerm(lastToken.trim()));			
 			}
 
 			OpTerm op = rule.getOpTerm().parse(token);
 
 			// check if unary operator
 			// TODO: move deciding unary code into OpTerm
 			if (op != null && op.getSymbol().equals("-")) {
 				Term lastTerm = null;
 				if (!tokens.isEmpty()) {
 					lastTerm = tokens.get(tokens.size() - 1);
 				}
 				
 				if (lastToken == null || lastToken.equals("(") || rule.getOpTerm().isInstance(lastTerm)) {
 					op = EvalOpTerm.Neg;
 				}
 			}
 
 			if (op != null)
 				tokens.add(op);
 			else
 				tokens.add(new TokenTerm(token));
 
 			next = r.next;
 			lastToken = token;
 		}
 
 		return tokens;
 	}
 
 	private static ParseResult nextToken(String s, int begin, int end, ParsingRule rule) {
 		if (begin > end)
 			return null;
 
 		// use r.next as a position here (need +1 for actual next)
 		ParseResult r = findNextDelimiter(s, begin, end, rule);
 		if (r.next < begin) {
 			// no symbol operator and white space, return whole string
 			String token = s.substring(begin, end + 1).trim();
 			return new ParseResult(token, end + 1);
 		}
 		
 		if (isAllWhitespaces(s, begin, r.next - 1)) {
 			// check if next token is quoted string
 			if (r.value.equals("\"")) {
 				int p = findClosingQuote(s, r.next + 1);
 				// int p = s.indexOf('"', r.next + 1);
 				if (p < 0) {
 					throw new LogQueryParseException("quote-mismatch", r.next + 1);
 //					String quoted = unveilEscape(s.substring(r.next));
 //					return new ParseResult(quoted, s.length());
 				} else {
 					String quoted = unveilEscape(s.substring(r.next, p + 1));
 					return new ParseResult(quoted, p + 1);
 				}
 			}
 			
 			// check whitespace
 			String token = (String) r.value;
 			if (token.trim().isEmpty())
 				return nextToken(s, skipSpaces(s, begin), end, rule);
 
 			// return operator
 			int len = token.length();
 			return new ParseResult(token, r.next + len);
 		} else {
 			// return term
 			String token = s.substring(begin, r.next).trim();
 			return new ParseResult(token, r.next);
 		}
 	}
 
 	private static String unveilEscape(String s) {
 		StringBuilder sb = new StringBuilder();
 		boolean escape = false;
 
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 
 			if (escape) {
 				if (c == '\\')
 					sb.append('\\');
 				else if (c == '"')
 					sb.append('"');
 				else
 					throw new LogQueryParseException("invalid-escape-sequence", -1, "char=" + c);
 				escape = false;
 			} else {
 				if (c == '\\')
 					escape = true;
 				else
 					sb.append(c);
 			}
 		}
 
 		return sb.toString();
 	}
 
 	private static int findClosingQuote(String s, int offset) {
 		boolean escape = false;
 		for (int i = offset; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (escape) {
 				if (c == '\\' || c == '"')
 					escape = false;
 				else
 					throw new LogQueryParseException("invalid-escape-sequence", offset);
 			} else {
 				if (c == '\\')
 					escape = true;
 				else if (c == '"')
 					return i;
 			}
 		}
 
 		return -1;
 	}
 
 	private static boolean isAllWhitespaces(String s, int begin, int end) {
 		if (end < begin)
 			return true;
 
 		for (int i = begin; i <= end; i++)
 			if (s.charAt(i) != ' ' && s.charAt(i) != '\t')
 				return false;
 
 		return true;
 	}
 
 	private static ParseResult findNextDelimiter(String s, int begin, int end, ParsingRule rule) {
 		// check parens, comma and operators
 		ParseResult r = new ParseResult(null, -1);
 		min(r, "\"", s.indexOf('"', begin), end);
 		min(r, "(", s.indexOf('(', begin), end);
 		min(r, ")", s.indexOf(')', begin), end);
 		for (OpTerm op : rule.getOpTerm().delimiters()) {
 			min(r, op.getSymbol(), s.indexOf(op.getSymbol(), begin), end);
 		}
 		
 		// check white spaces
 		// tabs are removed by ExpressionParser.parse, so it processes space only.
 		min(r, " ", s.indexOf(' ', begin), end);
 		return r;
 	}
 
 	private static void min(ParseResult r, String symbol, int p, int end) {
 		if (p < 0)
 			return;
 
 		boolean change = p >= 0 && p <= end && (r.next == -1 || p < r.next);
 		if (change) {
 			r.value = symbol;
 			r.next = p;
 		}
 	}
 
 	private static boolean isDelimiter(Term t, ParsingRule rule) {
 		if ( rule.getOpTerm().isInstance(t) || t instanceof FuncTerm)
 			return true;
 
 		if (t instanceof TokenTerm) {
 			String text = ((TokenTerm) t).getText();
 			return text.equals("(") || text.equals(")");
 		}
 
 		return false;
 	}
 
 	public static class TokenTerm implements Term {
 		private String text;
 
 		public TokenTerm(String text) {
 			this.text = text;
 		}
 
 		@Override
 		public String toString() {
 			return getText();
 		}
 
 		public String getText() {
 			return text;
 		}
 
 	}
 
 	public static class FuncTerm implements Term {
 		private String name;
 
 		public FuncTerm(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public String toString() {
 			return "func "+name+"()";
 		}
 
 		public String getName() {
 			return name;
 		}
 	}
 
 	public static int skipSpaces(String text, int position) {
 		int i = position;
 
 		while (i < text.length() && text.charAt(i) == ' ')
 			i++;
 
 		return i;
 	}
 }
