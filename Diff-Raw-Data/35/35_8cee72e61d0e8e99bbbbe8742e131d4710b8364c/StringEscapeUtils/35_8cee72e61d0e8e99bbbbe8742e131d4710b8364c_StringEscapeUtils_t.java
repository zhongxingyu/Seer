 package cs444.parser.symbols.ast;
 
 public class StringEscapeUtils {
 
	public static boolean isOctalDigit(char ch) {
		return '0' <= ch && ch <= '7'; 
	}
	
 	public static char simpleEscape(char ch) {
 		
 		char value = 0;
 		
 		if (ch == 'b') {
 			value = '\b';
 		} else if (ch == 't') {
 			value = '\t';
 		} else if (ch == 'n') {
 			value = '\n';
 		} else if (ch == 'f') {
 			value = '\f';
 		} else if (ch == 'r') {
 			value = '\r';
 		} else if (ch == '\"') {
 			value = '\"';
 		} else if (ch == '\'') {
 			value = '\'';
 		} else if (ch == '\\') {
 			value = '\\';
 		}
 		
 		return value;
 	}
 	
 	public static char octalEscape(String str) {
 		return (char)Integer.parseInt(str, 8);
 	}
 }
