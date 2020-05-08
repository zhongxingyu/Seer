 package name.kazennikov.tokens;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 
 public class SimpleTokenizer {
 
 	/**
 	 * Return true if char is a separator
 	 * @param ch
 	 * @return
 	 */
 	public static boolean isSeparator(char ch) {
		if(Character.isSpaceChar(ch))
 			return true;
 
 		int type = Character.getType(ch);
 
 		if(type == Character.CONNECTOR_PUNCTUATION || 
 				type == Character.DASH_PUNCTUATION ||
 				type == Character.START_PUNCTUATION ||
 				type == Character.END_PUNCTUATION ||
 				type == Character.INITIAL_QUOTE_PUNCTUATION ||
 				type == Character.FINAL_QUOTE_PUNCTUATION || 
 				type == Character.OTHER_PUNCTUATION)
 			return true;
 
 
 		return false;
 	}
 
 	/**
 	 * Infer type for non-punctuation token
 	 * @param text text that contains token
 	 * @param start token start
 	 * @param end token end
 	 * @return inferred type
 	 */
 	public static TokenType infer(String text, int start, int end) {
 		boolean hasDigits = false;
 		boolean hasLetters = false;
 
 		while(start < end) {
 			char ch = text.charAt(start);
 			if(Character.isLetter(ch))
 				hasLetters = true;
 			else if(Character.isDigit(ch))
 				hasDigits = true;
 			else
 				return BaseTokenType.MISC;
 			start++;
 		}
 
 		if(hasDigits && hasLetters)
 			return BaseTokenType.ALPHANUM;
 
 		if(hasDigits)
 			return BaseTokenType.DIGITS;
 
 		return BaseTokenType.LETTERS; // last possible choice
 	}
 
 
 
 
 
 	/**
 	 * Splits given text into a list of tokens
 	 * @param text text to split
 	 * @return list of tokens
 	 */
 	public static List<AbstractToken> tokenize(String text) {
 		List<AbstractToken> tokens = new ArrayList<AbstractToken>();
 
 		int pos = 0;
 		int start = 0;
 
 		while(pos < text.length()) {
 			char ch = text.charAt(pos);
 
 			if(isSeparator(ch)) {
 				if(start != pos) {
 					tokens.add(TextToken.valueOf(text, start, pos, infer(text, start, pos)));
 				}
 
 				int tStart = pos;
				if(Character.isSpaceChar(ch)) {
 					boolean newLine = (ch == '\n' || ch == '\r');
 
 					while(pos < text.length()) {
 						ch = text.charAt(pos);
 						newLine = newLine || ch == '\n' || ch == '\r';
						if(!Character.isSpaceChar(ch))
 							break;
 						pos++;
 					}
 					tokens.add(TextToken.valueOf(text, tStart, pos, newLine? BaseTokenType.NEWLINE : BaseTokenType.SPACE));
 				} else {
 					int puncStart = pos;
 					while (pos < text.length() && text.charAt(pos) == ch)
 						pos++;
 					tokens.add(TextToken.valueOf(text, puncStart, pos, BaseTokenType.PUNC));
 				}
 				start = pos;
 				continue;
 			}
 
 			pos++;
 		}
 		if(start != pos) {
 			tokens.add(TextToken.valueOf(text,start, pos, infer(text, start, pos)));
 		}
 
 		return tokens;
 	}
 	
 	public static BaseToken tokenize(String text, TokenType type) {
 		return BaseToken.valueOf(type, tokenize(text));
 	}
 
 	public static void main(String[] args) {
 		BaseToken t = tokenize("Это... стол.", NLPTokenType.SENTENCE);
 		System.out.println(t.size());
 	}
 }
 
 
