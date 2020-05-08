 package suite.node.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import suite.node.Atom;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Str;
 import suite.node.Tree;
 import suite.node.io.Operator.Assoc;
 import suite.node.io.TermParser.TermOp;
 import suite.node.util.Context;
 import suite.node.util.Singleton;
 import suite.util.LogUtil;
 import suite.util.ParserUtil;
 import suite.util.To;
 import suite.util.Util;
 
 public class Parser {
 
 	private Context localContext;
 
 	private Operator operators[];
 
 	public static final String openGroupComment = "-=";
 	public static final String closeGroupComment = "=-";
 	public static final String openLineComment = "--";
 	public static final String closeLineComment = "\n";
 
 	private static final List<Character> whitespaces = Arrays.asList('\t', '\r', '\n');
 
 	public Parser(Operator operators[]) {
 		this(Singleton.get().getGrandContext(), operators);
 	}
 
 	public Parser(Context context, Operator operators[]) {
 		localContext = context;
 		this.operators = operators;
 	}
 
 	public Node parse(InputStream is) throws IOException {
 		return parse(To.string(is));
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
 		return parseWithoutComments(s, 0);
 	}
 
 	private Node parseWithoutComments(String s, int fromOp) {
 		return parseRawString(s.trim(), fromOp);
 	}
 
 	private Node parseRawString(String s, int fromOp) {
 		return !s.isEmpty() ? parseRawString0(s, fromOp) : Atom.NIL;
 	}
 
 	private Node parseRawString0(String s, int fromOp) {
 		char first = Util.charAt(s, 0), last = Util.charAt(s, -1);
 
 		for (int i = fromOp; i < operators.length; i++) {
 			Operator operator = operators[i];
 			String lr[] = ParserUtil.search(s, operator);
 			int li, ri;
 
 			if (lr == null)
 				continue;
 
 			if (operator == TermOp.BRACES) {
 				String right = lr[1].trim();
 
 				if (Util.charAt(right, -1) != '}')
 					continue;
 
 				lr[1] = Util.substr(right, 0, -1);
 				li = 0;
 				ri = 0;
 			} else {
 				if (operator == TermOp.TUPLE_) {
 					if (Util.isBlank(lr[0]))
 						return parseRawString(lr[1], fromOp);
 					if (Util.isBlank(lr[1]))
 						return parseRawString(lr[0], fromOp);
 				}
 
 				boolean isLeftAssoc = operator.getAssoc() == Assoc.LEFT;
 				li = fromOp + (isLeftAssoc ? 0 : 1);
 				ri = fromOp + (isLeftAssoc ? 1 : 0);
 			}
 
 			return Tree.create(operator, parseWithoutComments(lr[0], li), parseWithoutComments(lr[1], ri));
 		}
 
 		if (Arrays.asList("[]").contains(s))
 			return Atom.create(s);
 		if (first == '(' && last == ')' || first == '[' && last == ']')
 			return parseWithoutComments(Util.substr(s, 1, -1));
 		if (first == '`' && last == '`')
 			return Tree.create(TermOp.TUPLE_, Atom.create("`"), parseRawString(" " + Util.substr(s, 1, -1) + " ", 0));
 
 		if (ParserUtil.isInteger(s))
 			return Int.create(Integer.parseInt(s));
 		if (s.startsWith("+x"))
 			return Int.create(Integer.parseInt(s.substring(2), 16));
 		if (s.startsWith("+'") && s.endsWith("'") && s.length() == 4)
 			return Int.create(s.charAt(2));
 
 		if (first == '"' && last == '"')
 			return new Str(Escaper.unescape(Util.substr(s, 1, -1), "\""));
 
 		if (first == '\'' && last == '\'')
 			s = Escaper.unescape(Util.substr(s, 1, -1), "'");
 		else {
 			s = s.trim(); // Trim unquoted atoms
 			if (!ParserUtil.isParseable(s))
 				LogUtil.info("Suspicious input when parsing " + s);
 		}
 
 		return Atom.create(localContext, s);
 	}
 
 	/**
 	 * Turns indent patterns into parentheses, to provide Python-like parsing.
 	 */
 	private String convertIndents(String s) {
 		StringBuilder sb = new StringBuilder();
 		int nLastIndents = 0;
 		String lastIndent = "";
 
		s = "\n" + s + "\n";

 		while (!s.isEmpty()) {
 			String line;
 			String lr[];
 
 			lr = ParserUtil.search(s, "\n", Assoc.RIGHT, false);
 			if (lr != null) {
 				line = lr[0];
 				s = lr[1];
 			} else {
 				line = s;
 				s = "";
 			}
 
 			int length = line.length(), nIndents = 0;
 			while (nIndents < length && Character.isWhitespace(line.charAt(nIndents)))
 				nIndents++;
 
 			String indent = line.substring(0, nIndents);
 			line = line.substring(nIndents).trim();
 
 			if (!lastIndent.startsWith(indent) && !lastIndent.startsWith(lastIndent))
 				throw new RuntimeException("Indent mismatch");
 
 			lastIndent = indent;
 
 			// Converts :: notation, "if:: a" becomes "if (a)"
 			lr = ParserUtil.search(line, "::", Assoc.RIGHT);
 			line = lr != null ? lr[0] + " (" + lr[1] + ")" : line;
 			length = line.length();
 
 			if (length != 0) { // Ignore empty lines
 				int startPos = 0, endPos = length;
 
 				// Find operators at beginning and end of line
 				for (Operator operator : operators) {
 					String name = operator.getName().trim();
 
 					if (!name.isEmpty()) {
 						if (line.startsWith(name + " "))
 							startPos = Math.max(startPos, 1 + name.length());
 						if (line.equals(name))
 							startPos = Math.max(startPos, name.length());
 						if (line.endsWith(name))
 							endPos = Math.min(endPos, length - name.length());
 					}
 				}
 
 				if (startPos > endPos) // When a line has only one operator
 					startPos = 0;
 
 				// Insert parentheses by line indentation
 				String decoratedLine = "";
 				while (nLastIndents > nIndents) {
 					decoratedLine += ") ";
 					nLastIndents--;
 				}
 				decoratedLine += line.substring(0, startPos);
 				while (nLastIndents < nIndents) {
 					decoratedLine += " (";
 					nLastIndents++;
 				}
 				decoratedLine += line.substring(startPos, endPos);
 				decoratedLine += line.substring(endPos);
 
 				sb.append(decoratedLine + "\n");
 			}
 
 			nLastIndents = nIndents;
 		}
 
 		return sb.toString();
 	}
 
 	private boolean isWhitespaces(String s) {
 		boolean result = true;
 		for (char c : s.toCharArray())
 			result &= whitespaces.contains(c);
 		return result;
 	}
 
 	private String convertWhitespaces(String s) {
 		for (char whitespace : whitespaces)
 			s = replace(s, "" + whitespace, " ");
 		return s;
 	}
 
 	private String replace(String s, String from, String to) {
 		while (true) {
 			int pos = ParserUtil.search(s, 0, from);
 
 			if (pos != -1)
 				s = s.substring(0, pos) + to + s.substring(pos + from.length());
 			else
 				return s;
 		}
 	}
 
 	private String removeComments(String s) {
 		s = removeComments(s, openGroupComment, closeGroupComment);
 		s = removeComments(s, openLineComment, closeLineComment);
 		return s;
 	}
 
 	private String removeComments(String s, String open, String close) {
 		int closeLength = !isWhitespaces(close) ? close.length() : 0;
 
 		while (true) {
 			int pos1 = ParserUtil.search(s, 0, open);
 			if (pos1 == -1)
 				return s;
 			int pos2 = ParserUtil.search(s, pos1 + open.length(), close);
 			if (pos2 == -1)
 				return s;
 			s = s.substring(0, pos1) + s.substring(pos2 + closeLength);
 		}
 	}
 
 }
