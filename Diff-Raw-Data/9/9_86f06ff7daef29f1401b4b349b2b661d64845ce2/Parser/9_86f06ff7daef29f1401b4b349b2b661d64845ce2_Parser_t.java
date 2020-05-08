 package org.parser;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.parser.Operator.Assoc;
 import org.suite.Context;
 import org.suite.Singleton;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Str;
 import org.suite.node.Tree;
 import org.util.Util;
 
 public class Parser {
 
 	private Context localContext;
 
 	private Operator operators[];
 
 	private static final String CLOSEGROUPCOMMENT = "=-";
 	private static final String OPENGROUPCOMMENT = "-=";
 	private static final String CLOSELINECOMMENT = "\n";
 	private static final String OPENLINECOMMENT = "--";
 
 	public Parser(Operator operators[]) {
 		this(Singleton.get().getGrandContext(), operators);
 	}
 
 	public Parser(Context context, Operator operators[]) {
 		this.localContext = context;
 		this.operators = operators;
 	}
 
 	public Node parseClassPathFile(String fn) throws IOException {
 		return parse(getClass().getClassLoader().getResourceAsStream(fn));
 	}
 
 	public Node parse(InputStream is) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		while (br.ready())
 			sb.append(br.readLine() + "\n");
 		br.close();
 
 		return parse(sb.toString());
 	}
 
 	public Node parse(String s) {
 		s = removeComments(s);
 		s = convertIndents(s);
 		s = convertWhitespaces(s);
 		return parseWithoutComments(s);
 	}
 
 	/**
 	 * Parse without comments.
 	 */
 	private Node parseWithoutComments(String s) {
 		s = s.trim();
 		if (s.isEmpty())
 			return Atom.nil;
 
 		char first = s.charAt(0), last = s.charAt(s.length() - 1);
 
 		for (Operator operator : operators) {
 			if (operator == TermOp.BRACES && last != '}')
 				continue;
 
 			int pos = search(s, operator);
 
 			if (operator == TermOp.BRACES)
 				s = Util.substr(s, 0, -1);
 
 			if (pos != -1) {
 				String l = s.substring(0, pos);
 				String r = s.substring(pos + operator.getName().length());
 
 				return new Tree(operator, parseWithoutComments(l),
 						parseWithoutComments(r));
 			}
 		}
 
 		if (first == '(' && last == ')' //
 				|| first == '[' && last == ']')
 			return parseWithoutComments(Util.substr(s, 1, -1));
 
 		try {
 			return Int.create(Integer.parseInt(s));
 		} catch (Exception ex) {
 		}
 
 		if (first == '"' && last == '"')
 			return new Str(unescape(Util.substr(s, 1, -1), "\""));
 
 		if (first == '\'' && last == '\'')
 			s = unescape(Util.substr(s, 1, -1), "'");
 
 		return Atom.create(localContext, s);
 	}
 
 	/**
 	 * Turns indent patterns into parentheses, to provide Python-like parsing.
 	 */
 	private String convertIndents(String s) {
 		StringBuilder sb = new StringBuilder();
 		int lastIndent = 0;
 
 		s = "\n" + s + "\n";
 
 		while (!s.isEmpty()) {
 			String line;
 			int pos = search(s, "\n", Assoc.RIGHT, false);
 			pos = pos >= 0 ? pos : s.length();
 			line = s.substring(0, pos);
 			s = pos < s.length() ? s.substring(pos + 1) : "";
 
 			int length = line.length();
 			int indent = 0;
 			while (indent < length && line.charAt(indent) == '\t')
 				indent++;
 
 			line = line.substring(indent).trim();
 			length = line.length();
 			int startPos = 0, endPos = length;
 
 			for (Operator operator : operators) {
 				String name = operator.getName().trim();
 
 				if (!name.isEmpty()) {
 					if (line.startsWith(name))
 						startPos = Math.max(startPos, name.length());
 					if (line.endsWith(name))
 						endPos = Math.min(endPos, length - name.length());
 				}
 			}
 
 			if (startPos > endPos) // When a line has only one operator
 				startPos = 0;
 
 			boolean isSeparateLine = startPos == 0 //
 					&& endPos == length //
 					&& startPos != endPos //
 					&& checkDepth(line) == 0;
 
 			String decoratedLine = "";
 			while (lastIndent > indent) {
 				decoratedLine += ") ";
 				lastIndent--;
 			}
 			decoratedLine += line.substring(0, startPos);
 			decoratedLine += isSeparateLine ? " (" : "";
 			while (lastIndent < indent) {
 				decoratedLine += " (";
 				lastIndent++;
 			}
 			decoratedLine += line.substring(startPos, endPos);
 			decoratedLine += isSeparateLine ? ") " : "";
 			decoratedLine += line.substring(endPos);
 
 			sb.append(decoratedLine + "\n");
 		}
 
 		return sb.toString();
 	}
 
 	private static final String whitespaces[] = { "\t", "\r", "\n" };
 
 	private String convertWhitespaces(String s) {
 		for (String whitespace : whitespaces)
 			s = replace(s, whitespace, " ");
 		return s;
 	}
 
 	private String replace(String s, String from, String to) {
 		while (true) {
 			int pos = search(s, 0, from);
 
 			if (pos != -1)
 				s = s.substring(0, pos) + to + s.substring(pos + from.length());
 			else
 				return s;
 		}
 	}
 
 	private String removeComments(String s) {
 		s = removeComments(s, OPENGROUPCOMMENT, CLOSEGROUPCOMMENT);
 		s = removeComments(s, OPENLINECOMMENT, CLOSELINECOMMENT);
 		return s;
 	}
 
 	private String removeComments(String s, String open, String close) {
 		while (true) {
 			int pos1 = search(s, 0, open);
 			if (pos1 == -1)
 				return s;
 			int pos2 = search(s, pos1 + open.length(), close);
 			if (pos2 == -1)
 				return s;
 			s = s.substring(0, pos1) + s.substring(pos2 + close.length());
 		}
 	}
 
 	private static String unescape(String s, String quote) {
 		s = s.replace(quote + quote, quote);
 
 		try {
 			int pos = 0;
 			while ((pos = s.indexOf('%', pos)) != -1) {
				int pos1 = pos + 1;

				if (pos1 < s.length() && s.charAt(pos1) != '%') {
					String hex = s.substring(pos1, pos + 3);
 					char c = (char) Integer.parseInt(hex, 16);
 					s = s.substring(0, pos) + c + s.substring(pos + 3);
 				} else
					s = s.substring(0, pos) + s.substring(pos1);
 
 				pos++;
 			}
 		} catch (Exception ex) {
 			// StringIndexOutOfBoundsException, NumberFormatException
 			log.error(Util.currentClass(), ex);
 		}
 
 		return s;
 	}
 
 	private static int search(String s, int start, String toMatch) {
 		int nameLength = toMatch.length();
 		int end = s.length() - nameLength;
 		int quote = 0;
 
 		for (int pos = start; pos <= end; pos++) {
 			char c = s.charAt(pos);
 			quote = checkQuote(quote, c);
 
 			if (quote == 0 && s.startsWith(toMatch, pos))
 				return pos;
 		}
 
 		return -1;
 	}
 
 	private static int search(String s, Operator operator) {
 		return search(s, operator.getName(), operator.getAssoc());
 	}
 
 	private static int search(String s, String name, Assoc assoc) {
 		return search(s, name, assoc, true);
 	}
 
 	private static int search(String s, String name, Assoc assoc,
 			boolean checkDepth) {
 		boolean isLeftAssoc = assoc == Assoc.LEFT;
 		int nameLength = name.length();
 		int end = s.length() - nameLength;
 		int quote = 0, depth = 0;
 
 		for (int i = 0; i <= end; i++) {
 			int pos = isLeftAssoc ? end - i : i;
 			char c = s.charAt(pos + (isLeftAssoc ? nameLength - 1 : 0));
 			quote = checkQuote(quote, c);
 
 			if (quote == 0) {
 				if (checkDepth)
 					depth = checkDepth(depth, c);
 
 				if (depth == 0 && s.startsWith(name, pos))
 					return pos;
 			}
 		}
 
 		return -1;
 	}
 
 	private static int checkQuote(int quote, char c) {
 		if (c == quote)
 			quote = 0;
 		else if (c == '\'' || c == '"')
 			quote = c;
 		return quote;
 	}
 
 	private static int checkDepth(String s) {
 		int depth = 0;
 		for (char c : s.toCharArray())
 			depth = checkDepth(depth, c);
 		return depth;
 	}
 
 	private static int checkDepth(int depth, char c) {
 		if (c == '(' || c == '[' || c == '{')
 			depth++;
 		if (c == ')' || c == ']' || c == '}')
 			depth--;
 		return depth;
 	}
 
 	private static Log log = LogFactory.getLog(Util.currentClass());
 
 }
