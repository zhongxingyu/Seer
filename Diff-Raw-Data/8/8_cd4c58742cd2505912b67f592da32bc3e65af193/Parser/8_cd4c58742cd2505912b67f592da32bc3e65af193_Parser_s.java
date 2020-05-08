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
 
 		for (Operator operator : operators) {
 			int pos = search(s, operator);
 
 			if (pos != -1) {
 				String l = s.substring(0, pos);
 				String r = s.substring(pos + operator.getName().length());
 				return new Tree(operator, parseWithoutComments(l),
 						parseWithoutComments(r));
 			}
 		}
 
 		char first = s.charAt(0), last = s.charAt(s.length() - 1);
 
 		if (first == '(' && last == ')' //
 				|| first == '[' && last == ']' //
 				|| first == '{' && last == '}')
 			return parseWithoutComments(s.substring(1, s.length() - 1));
 
 		try {
 			return Int.create(Integer.parseInt(s));
 		} catch (Exception ex) {
 		}
 
 		if (first == '"' && last == '"')
 			return new Str(unescape(s.substring(1, s.length() - 1), "\""));
 
 		if (first == '\'' && last == '\'')
 			s = unescape(s.substring(1, s.length() - 1), "'");
 
 		return Atom.create(localContext, s);
 	}
 
 	private String convertWhitespaces(String s) {
 		return s.replace("\r", " ").replace(CLOSELINECOMMENT, " ");
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
 				if (s.charAt(pos + 1) != '%') {
 					String hex = s.substring(pos + 1, pos + 3);
 					char c = (char) Integer.parseInt(hex, 16);
 					s = s.substring(0, pos) + c + s.substring(pos + 3);
 				} else
 					s = s.substring(0, pos) + s.substring(pos + 1);
 				pos++;
 			}
		} catch (NumberFormatException ex) {
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
 		String name = operator.getName();
 		boolean isRightAssoc = operator.getAssoc() == Assoc.RIGHT;
 		int nameLength = name.length();
 		int end = s.length() - nameLength;
 		int quote = 0, depth = 0;
 
 		for (int i = 0; i <= end; i++) {
 			int pos = isRightAssoc ? end - i : i;
 			char c = s.charAt(pos + (isRightAssoc ? nameLength - 1 : 0));
 			quote = checkQuote(quote, c);
 
 			if (quote == 0) {
 				if (c == '(' || c == '[' || c == '{')
 					depth++;
 				if (c == ')' || c == ']' || c == '}')
 					depth--;
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
 
 	private static Log log = LogFactory.getLog(Util.currentClass());
 
 }
