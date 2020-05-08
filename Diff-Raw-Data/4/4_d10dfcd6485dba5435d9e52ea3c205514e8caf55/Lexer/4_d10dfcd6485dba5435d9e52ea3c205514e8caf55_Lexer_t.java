 package lexer;
 
 import gtools.GTools;
 
 import java.util.LinkedList;
 
 public class Lexer implements ILexer {
 
 	private LinkedList<IToken> tokens;
 	private int index;
 	private IToken last;
 	private GTools gt;
 
 	public Lexer(String s, GTools gt) throws Exception {
 		index = 0;
 		tokens = new LinkedList<IToken>();
 		last = new Token(gt.numberOfTerminals());
 		this.gt = gt;
 		tokenize(clean(s));
 	}
 
 	// returns token of first symbol not yet sent (if any; else, returns token with total number of terminals
 	// throws exception if unmappable character
 	public IToken getNextSymbol() throws Exception {
 		if (index == tokens.size())
 			return last;
 		return tokens.get(index++);
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	// returns the source clean code, that is, after removal of [\n \t]
 	private String clean(String s) {
 		//TODO: check unallowed chars
 		return s.replaceAll("[\n \t]+", " ");
 	}
 
 	private void tokenize(String s) throws Exception {
 		String[] reserved_words = { "lambda", "map", "true", "false", "if", "else", "function", "return", "while", "or", "and", "say", "listen", "size", "array", "read", "write", "<=", ">=", "==", "!=" };
 		char[] singletons = { '(', ')', '{', '}', ';', '=', '+', '-', '*', '/', '%', '!', '[', ']', ',', '<', '>' };
 		int j;
 		String ss;
 		int i = 0;
 		while (i < s.length()) {
 			// reserved words
 			for (j = 0 ; j < reserved_words.length ; j++)
 				if (s.startsWith(reserved_words[j], i)) {
 					tokens.add(new Token(gt.terminal(s.substring(i, i + reserved_words[j].length())), s.substring(i, i + reserved_words[j].length())));
 					i += reserved_words[j].length();
 					break;
 				}
 			if (j < reserved_words.length)
 				continue;
 			// strings
 			if (s.startsWith("\"", i)) {
 				tokens.add(new Token(gt.terminal("\""), "\""));
 				++i;
 				int end_quote = s.indexOf("\"", i);
 				if (end_quote == -1)
 					throw new Exception("Lexical exception: unterminated string literal");
				tokens.add(new Token(gt.terminal("LEX_STR"), s.substring(i, end_quote)));
				i = end_quote;
 				tokens.add(new Token(gt.terminal("\""), "\""));
 				++i;
 				continue;
 			}
 			// identifiers
 			if (s.charAt(i) == '$') {
 				tokens.add(new Token(gt.terminal("$"), "$"));
 				++i;
 				j = i;
 				while (j < s.length()) {
 					ss = s.substring(j, j + 1);
 					if (!ss.matches("[A-Za-z0-9]"))
 						break;
 					++j;
 				}
 				if (j == i || j == s.length())
 					throw new Exception("Lexical exception: unterminated identifier");
 				tokens.add(new Token(gt.terminal("LEX_ID"), s.substring(i, j)));
 				i = j;
 				continue;
 			}
 			ss = s.substring(i, i + 1);
 			// fnames
 			if (ss.matches("[a-z]")) {
 				j = i + 1;
 				while (j < s.length()) {
 					ss = s.substring(j, j + 1);
 					if (!ss.matches("[A-Za-z0-9]"))
 						break;
 					++j;
 				}
 				if (j == s.length())
 					throw new Exception("Lexical exception: unterminated fname");
 				if (!ss.matches("\\("))
 					throw new Exception("Lexical exception: fname must be followed by '('");
 				tokens.add(new Token(gt.terminal("LEX_FNAME"), s.substring(i, j)));
 				i = j;
 				continue;
 			}
 			// constants
 			if (ss.matches("[-0-9]")) {
 				j = i + 1;
 				while (j < s.length()) {
 					ss = s.substring(j, j + 1);
 					if (!ss.matches("[0-9]"))
 						break;
 					++j;
 				}
 				if (j == s.length())
 					throw new Exception("Lexical exception: unterminated constant");
 				tokens.add(new Token(gt.terminal("LEX_INT"), s.substring(i, j)));
 				i = j;
 				continue;
 			}
 			// singletons
 			for (j = 0 ; j < singletons.length ; j++)
 				if (s.charAt(i) == singletons[j]) {
 					tokens.add(new Token(gt.terminal(""+singletons[j]), ""+singletons[j]));
 					break;
 				}
 			++i;
 		}
 	}
 
 }
