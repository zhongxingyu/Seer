 package q.util;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 字符串处理
  * 
  * @author seanlinwang
  * @since 1.0, 2010-4-1 下午07:02:52
  */
 
 public class StringKit {
 
 	public static final String BACK_SLASH = "\\\"";
 	private static final WordTokenizer LOWER_CASE_WITH_UNDERSCORES_TOKENIZER = new WordTokenizer() {
 		protected void startSentence(StringBuffer buffer, char ch) {
 			buffer.append(Character.toLowerCase(ch));
 		}
 
 		protected void startWord(StringBuffer buffer, char ch) {
 			if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
 				buffer.append(UNDERSCORE);
 			}
 
 			buffer.append(Character.toLowerCase(ch));
 		}
 
 		protected void inWord(StringBuffer buffer, char ch) {
 			buffer.append(Character.toLowerCase(ch));
 		}
 
 		protected void startDigitSentence(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 
 		protected void startDigitWord(StringBuffer buffer, char ch) {
 			if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
 				buffer.append(UNDERSCORE);
 			}
 
 			buffer.append(ch);
 		}
 
 		protected void inDigitWord(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 
 		protected void inDelimiter(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 	};
 
 	private abstract static class WordTokenizer {
 		protected static final char UNDERSCORE = '_';
 
 		/**
 		 * Parse sentence°£
 		 */
 		public String parse(String str) {
 			if (isEmpty(str)) {
 				return str;
 			}
 
 			int length = str.length();
 			StringBuffer buffer = new StringBuffer(length);
 
 			for (int index = 0; index < length; index++) {
 				char ch = str.charAt(index);
 
 				// ∫ˆ¬‘ø’∞◊°£
 				if (Character.isWhitespace(ch)) {
 					continue;
 				}
 
 				// ¥Û–¥◊÷ƒ∏ø™ º£∫UpperCaseWordªÚ «TitleCaseWord°£
 				if (Character.isUpperCase(ch)) {
 					int wordIndex = index + 1;
 
 					while (wordIndex < length) {
 						char wordChar = str.charAt(wordIndex);
 
 						if (Character.isUpperCase(wordChar)) {
 							wordIndex++;
 						} else if (Character.isLowerCase(wordChar)) {
 							wordIndex--;
 							break;
 						} else {
 							break;
 						}
 					}
 
 					// 1. wordIndex == length£¨Àµ√˜◊Ó∫Û“ª∏ˆ◊÷ƒ∏Œ™¥Û–¥£¨“‘upperCaseWord¥¶¿Ì÷Æ°£
 					// 2. wordIndex == index£¨Àµ√˜index¥¶Œ™“ª∏ˆtitleCaseWord°£
 					// 3. wordIndex > index£¨Àµ√˜indexµΩwordIndex - 1¥¶»´≤ø «¥Û–¥£¨“‘upperCaseWord¥¶¿Ì°£
 					if ((wordIndex == length) || (wordIndex > index)) {
 						index = parseUpperCaseWord(buffer, str, index, wordIndex);
 					} else {
 						index = parseTitleCaseWord(buffer, str, index);
 					}
 
 					continue;
 				}
 
 				// –°–¥◊÷ƒ∏ø™ º£∫LowerCaseWord°£
 				if (Character.isLowerCase(ch)) {
 					index = parseLowerCaseWord(buffer, str, index);
 					continue;
 				}
 
 				//  ˝◊÷ø™ º£∫DigitWord°£
 				if (Character.isDigit(ch)) {
 					index = parseDigitWord(buffer, str, index);
 					continue;
 				}
 
 				// ∑«◊÷ƒ∏ ˝◊÷ø™ º£∫Delimiter°£
 				inDelimiter(buffer, ch);
 			}
 
 			return buffer.toString();
 		}
 
 		private int parseUpperCaseWord(StringBuffer buffer, String str, int index, int length) {
 			char ch = str.charAt(index++);
 
 			//  ◊◊÷ƒ∏£¨±ÿ»ª¥Ê‘⁄«“Œ™¥Û–¥°£
 			if (buffer.length() == 0) {
 				startSentence(buffer, ch);
 			} else {
 				startWord(buffer, ch);
 			}
 
 			// ∫Û–¯◊÷ƒ∏£¨±ÿŒ™–°–¥°£
 			for (; index < length; index++) {
 				ch = str.charAt(index);
 				inWord(buffer, ch);
 			}
 
 			return index - 1;
 		}
 
 		private int parseLowerCaseWord(StringBuffer buffer, String str, int index) {
 			char ch = str.charAt(index++);
 
 			//  ◊◊÷ƒ∏£¨±ÿ»ª¥Ê‘⁄«“Œ™–°–¥°£
 			if (buffer.length() == 0) {
 				startSentence(buffer, ch);
 			} else {
 				startWord(buffer, ch);
 			}
 
 			// ∫Û–¯◊÷ƒ∏£¨±ÿŒ™–°–¥°£
 			int length = str.length();
 
 			for (; index < length; index++) {
 				ch = str.charAt(index);
 
 				if (Character.isLowerCase(ch)) {
 					inWord(buffer, ch);
 				} else {
 					break;
 				}
 			}
 
 			return index - 1;
 		}
 
 		private int parseTitleCaseWord(StringBuffer buffer, String str, int index) {
 			char ch = str.charAt(index++);
 
 			//  ◊◊÷ƒ∏£¨±ÿ»ª¥Ê‘⁄«“Œ™¥Û–¥°£
 			if (buffer.length() == 0) {
 				startSentence(buffer, ch);
 			} else {
 				startWord(buffer, ch);
 			}
 
 			// ∫Û–¯◊÷ƒ∏£¨±ÿŒ™–°–¥°£
 			int length = str.length();
 
 			for (; index < length; index++) {
 				ch = str.charAt(index);
 
 				if (Character.isLowerCase(ch)) {
 					inWord(buffer, ch);
 				} else {
 					break;
 				}
 			}
 
 			return index - 1;
 		}
 
 		private int parseDigitWord(StringBuffer buffer, String str, int index) {
 			char ch = str.charAt(index++);
 
 			//  ◊◊÷∑˚£¨±ÿ»ª¥Ê‘⁄«“Œ™ ˝◊÷°£
 			if (buffer.length() == 0) {
 				startDigitSentence(buffer, ch);
 			} else {
 				startDigitWord(buffer, ch);
 			}
 
 			// ∫Û–¯◊÷∑˚£¨±ÿŒ™ ˝◊÷°£
 			int length = str.length();
 
 			for (; index < length; index++) {
 				ch = str.charAt(index);
 
 				if (Character.isDigit(ch)) {
 					inDigitWord(buffer, ch);
 				} else {
 					break;
 				}
 			}
 
 			return index - 1;
 		}
 
 		protected boolean isDelimiter(char ch) {
 			return !Character.isUpperCase(ch) && !Character.isLowerCase(ch) && !Character.isDigit(ch);
 		}
 
 		protected abstract void startSentence(StringBuffer buffer, char ch);
 
 		protected abstract void startWord(StringBuffer buffer, char ch);
 
 		protected abstract void inWord(StringBuffer buffer, char ch);
 
 		protected abstract void startDigitSentence(StringBuffer buffer, char ch);
 
 		protected abstract void startDigitWord(StringBuffer buffer, char ch);
 
 		protected abstract void inDigitWord(StringBuffer buffer, char ch);
 
 		protected abstract void inDelimiter(StringBuffer buffer, char ch);
 	}
 
 	private static final WordTokenizer CAMEL_CASE_TOKENIZER = new WordTokenizer() {
 		protected void startSentence(StringBuffer buffer, char ch) {
 			buffer.append(Character.toLowerCase(ch));
 		}
 
 		protected void startWord(StringBuffer buffer, char ch) {
 			if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
 				buffer.append(Character.toUpperCase(ch));
 			} else {
 				buffer.append(Character.toLowerCase(ch));
 			}
 		}
 
 		protected void inWord(StringBuffer buffer, char ch) {
 			buffer.append(Character.toLowerCase(ch));
 		}
 
 		protected void startDigitSentence(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 
 		protected void startDigitWord(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 
 		protected void inDigitWord(StringBuffer buffer, char ch) {
 			buffer.append(ch);
 		}
 
 		protected void inDelimiter(StringBuffer buffer, char ch) {
 			if (ch != UNDERSCORE) {
 				buffer.append(ch);
 			}
 		}
 	};
 
 	/**
 	 * <p>
 	 * Removes control characters (char &lt;= 32) from both ends of this String, handling <code>null</code> by returning <code>null</code>.
 	 * </p>
 	 * 
 	 * <p>
 	 * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32. To strip whitespace use {@link #strip(String)}.
 	 * </p>
 	 * 
 	 * <p>
 	 * To trim your choice of characters, use the {@link #strip(String, String)} methods.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.trim(null)          = null
 	 * StringUtils.trim("")            = ""
 	 * StringUtils.trim("     ")       = ""
 	 * StringUtils.trim("abc")         = "abc"
 	 * StringUtils.trim("    abc    ") = "abc"
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to be trimmed, may be null
 	 * @return the trimmed string, <code>null</code> if null String input
 	 */
 	public static String trim(String str) {
 		return str == null ? null : str.trim();
 	}
 
 	/**
 	 * 将字符串的首字符传唤为大写
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static String capitalize(String str) {
 		if (StringKit.isEmpty(str))
 			return str;
 		char[] chars = str.toCharArray();
 		char c = chars[0];
 		if (c >= 97 && c <= 122)
 			chars[0] = (char) (c - 32);
 		return new String(chars);
 	}
 
 	public static void escapeJson(Writer sb, String source) throws IOException {
 		char[] chars = source.toCharArray();
 		for (int i = 0; i < chars.length; i++) {
 			char c = chars[i];
 			if (0 <= c && c <= 31) {
 				switch (c) {
 				case '\r':
 					sb.append("\\r");
 					break;
 				case '\n':
 					sb.append("\\n");
 					break;
 				case '\t':
 					sb.append("\\t");
 					break;
 				case '\f':
 					sb.append("\\f");
 					break;
 				case '\b':
 					sb.append("\\b");
 					break;
 				default:
 					;
 				}
 			} else {
 				switch (c) {
 				case '"':
 					sb.append("\\\"");
 					break;
 				case '\\':
 					sb.append("\\\\");
 					break;
				case '/':
					sb.append("\\/");
 				default:
 					sb.append(c);
 				}
 			}
 		}
 	}
 
 	public static String escapeJson(String source) {
 		char[] chars = source.toCharArray();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < chars.length; i++) {
 			char c = chars[i];
 			if (0 <= c && c <= 31) {
 				switch (c) {
 				case '\r':
 					sb.append("\\r");
 					break;
 				case '\n':
 					sb.append("\\n");
 					break;
 				case '\t':
 					sb.append("\\t");
 					break;
 				case '\f':
 					sb.append("\\f");
 					break;
 				case '\b':
 					sb.append("\\b");
 					break;
 				default:
 					;
 				}
 			} else {
 				switch (c) {
 				case '"':
 					sb.append("\\\"");
 					break;
 				case '\\':
 					sb.append("\\\\");
 					break;
				case '/':
 					//sb.append("\\/");
 				default:
 					sb.append(c);
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	/** The Constant QUOT. */
 	public static final String QUOT = "&quot;";
 
 	/** The Constant AMP. */
 	public static final String AMP = "&amp;";
 
 	/** The Constant APOS. */
 	public static final String APOS = "&apos;";
 
 	/** The Constant GT. */
 	public static final String GT = "&gt;";
 
 	/** The Constant LT. */
 	public static final String LT = "&lt;";
 
 	/**
 	 * xml字符转义包括(<,>,',&,")五个字符.
 	 * 
 	 * @param source
 	 *            所需转义的字符串
 	 * 
 	 * @return 转义后的字符串
 	 * @throws IOException
 	 */
 	public static void escapeXml(Writer writer, String source) throws IOException {
 		char[] chars = source.toCharArray();
 		for (int i = 0; i < chars.length; i++) {
 			char c = chars[i];
 			switch (c) {
 			case '<':
 				writer.append(LT);
 				break;
 			case '>':
 				writer.append(GT);
 				break;
 			case '\'':
 				writer.append(APOS);
 				break;
 			case '&':
 				writer.append(AMP);
 				break;
 			case '\"':
 				writer.append(QUOT);
 				break;
 			default:
 				if ((c == 0x9) || (c == 0xA) || (c == 0xD) || ((c >= 0x20) && (c <= 0xD7FF)) || ((c >= 0xE000) && (c <= 0xFFFD)) || ((c >= 0x10000) && (c <= 0x10FFFF)))
 					writer.append(c);
 			}
 		}
 	}
 
 	/**
 	 * 过滤不可见字符
 	 * 
 	 * @author liupo
 	 * @param input
 	 * @return
 	 */
 	public static String stripNonValidXMLCharacters(String input) {
 		if (input == null || ("".equals(input)))
 			return "";
 		StringBuilder out = new StringBuilder();
 		char current;
 		for (int i = 0; i < input.length(); i++) {
 			current = input.charAt(i);
 			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))
 				out.append(current);
 		}
 		return out.toString();
 	}
 
 	/**
 	 * <p>
 	 * Splits the provided text into an array, separator specified. This is an alternative to using StringTokenizer.
 	 * </p>
 	 * 
 	 * <p>
 	 * The separator is not included in the returned String array. Adjacent separators are treated as one separator. For more control over the split use the StrTokenizer class.
 	 * </p>
 	 * 
 	 * <p>
 	 * A <code>null</code> input String returns <code>null</code>.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.split(null, *)         = null
 	 * StringUtils.split("", *)           = []
 	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
 	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
 	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
 	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to parse, may be null
 	 * @param separatorChar
 	 *            the character used as the delimiter
 	 * @return an array of parsed Strings, <code>null</code> if null String input
 	 * @since 2.0
 	 */
 	public static String[] split(String str, char separatorChar) {
 		return splitWorker(str, separatorChar, -1, false);
 	}
 
 	public static String[] split(String str, char separatorChar, int max) {
 		return splitWorker(str, separatorChar, max, false);
 	}
 
 	/**
 	 * Performs the logic for the <code>split</code> and <code>splitPreserveAllTokens</code> methods that do not return a maximum array length.
 	 * 
 	 * @param str
 	 *            the String to parse, may be <code>null</code>
 	 * @param separatorChar
 	 *            the separate character
 	 * @param preserveAllTokens
 	 *            if <code>true</code>, adjacent separators are treated as empty token separators; if <code>false</code>, adjacent separators are treated as one separator.
 	 * @return an array of parsed Strings, <code>null</code> if null String input
 	 */
 	private static String[] splitWorker(String str, char separatorChar, int max, boolean preserveAllTokens) {
 		// Performance tuned for 2.0 (JDK1.4)
 
 		if (str == null) {
 			return null;
 		}
 		int len = str.length();
 		if (len == 0) {
 			return ArrayKit.EMPTY_STRING_ARRAY;
 		}
 		List<String> list = new ArrayList<String>();
 		int i = 0, start = 0, tokenTimes = 0;
 		boolean match = false;
 		boolean lastMatch = false;
 		while (i < len) {
 			if (str.charAt(i) == separatorChar) {
 				if (match || preserveAllTokens) {
 					tokenTimes++;
 					if (tokenTimes == max) {
 						i = len;
 						break;
 					}
 					list.add(str.substring(start, i));
 					match = false;
 					lastMatch = true;
 				}
 				start = ++i;
 				continue;
 			}
 			lastMatch = false;
 			match = true;
 			i++;
 		}
 		if (match || (preserveAllTokens && lastMatch)) {
 			list.add(str.substring(start, i));
 		}
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * @param value
 	 * @return
 	 */
 	public static boolean isEmpty(String value) {
 		return value == null || value.length() == 0;
 	}
 
 	/**
 	 * @param key
 	 * @return
 	 */
 	public static boolean isNotEmpty(String value) {
 		return !isEmpty(value);
 	}
 
 	/**
 	 * <p>
 	 * Checks if a String is whitespace, empty ("") or null.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.isBlank(null)      = true
 	 * StringUtils.isBlank("")        = true
 	 * StringUtils.isBlank(" ")       = true
 	 * StringUtils.isBlank("bob")     = false
 	 * StringUtils.isBlank("  bob  ") = false
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to check, may be null
 	 * @return <code>true</code> if the String is null, empty or whitespace
 	 * @since 2.0
 	 */
 	public static boolean isBlank(String str) {
 		int strLen;
 		if (str == null || (strLen = str.length()) == 0) {
 			return true;
 		}
 		for (int i = 0; i < strLen; i++) {
 			if ((Character.isWhitespace(str.charAt(i)) == false)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Ω´◊÷∑˚¥Æ◊™ªª≥…camel case°£
 	 * 
 	 * <p>
 	 * »Áπ˚◊÷∑˚¥Æ «<code>null</code>‘Ú∑µªÿ<code>null</code>°£
 	 * 
 	 * <pre>
 	 * StringUtil.toCamelCase(null)  = null
 	 * StringUtil.toCamelCase("")    = ""
 	 * StringUtil.toCamelCase("aBc") = "aBc"
 	 * StringUtil.toCamelCase("aBc def") = "aBcDef"
 	 * StringUtil.toCamelCase("aBc def_ghi") = "aBcDefGhi"
 	 * StringUtil.toCamelCase("aBc def_ghi 123") = "aBcDefGhi123"
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * <p>
 	 * ¥À∑Ω∑®ª·±£¡Ù≥˝¡Àœ¬ªÆœﬂ∫Õø’∞◊“‘Õ‚µƒÀ˘”–∑÷∏Ù∑˚°£
 	 * </p>
 	 * 
 	 * @param str
 	 *            “™◊™ªªµƒ◊÷∑˚¥Æ
 	 * 
 	 * @return camel case◊÷∑˚¥Æ£¨»Áπ˚‘≠◊÷∑˚¥ÆŒ™<code>null</code>£¨‘Ú∑µªÿ<code>null</code>
 	 */
 	public static String toCamelCase(String str) {
 		return CAMEL_CASE_TOKENIZER.parse(str);
 	}
 
 	/**
 	 * Ω´◊÷∑˚¥Æ◊™ªª≥…œ¬ªÆœﬂ∑÷∏Ùµƒ–°–¥◊÷∑˚¥Æ°£
 	 * 
 	 * <p>
 	 * »Áπ˚◊÷∑˚¥Æ «<code>null</code>‘Ú∑µªÿ<code>null</code>°£
 	 * 
 	 * <pre>
 	 * StringUtil.toLowerCaseWithUnderscores(null)  = null
 	 * StringUtil.toLowerCaseWithUnderscores("")    = ""
 	 * StringUtil.toLowerCaseWithUnderscores("aBc") = "a_bc"
 	 * StringUtil.toLowerCaseWithUnderscores("aBc def") = "a_bc_def"
 	 * StringUtil.toLowerCaseWithUnderscores("aBc def_ghi") = "a_bc_def_ghi"
 	 * StringUtil.toLowerCaseWithUnderscores("aBc def_ghi 123") = "a_bc_def_ghi_123"
 	 * StringUtil.toLowerCaseWithUnderscores("__a__Bc__") = "__a__bc__"
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * <p>
 	 * ¥À∑Ω∑®ª·±£¡Ù≥˝¡Àø’∞◊“‘Õ‚µƒÀ˘”–∑÷∏Ù∑˚°£
 	 * </p>
 	 * 
 	 * @param str
 	 *            “™◊™ªªµƒ◊÷∑˚¥Æ
 	 * 
 	 * @return œ¬ªÆœﬂ∑÷∏Ùµƒ–°–¥◊÷∑˚¥Æ£¨»Áπ˚‘≠◊÷∑˚¥ÆŒ™<code>null</code>£¨‘Ú∑µªÿ<code>null</code>
 	 */
 	public static String toLowerCaseWithUnderscores(String str) {
 		return LOWER_CASE_WITH_UNDERSCORES_TOKENIZER.parse(str);
 	}
 
 	// Stripping
 	// -----------------------------------------------------------------------
 	/**
 	 * <p>
 	 * Strips whitespace from the start and end of a String.
 	 * </p>
 	 * 
 	 * <p>
 	 * This is similar to {@link #trim(String)} but removes whitespace. Whitespace is defined by {@link Character#isWhitespace(char)}.
 	 * </p>
 	 * 
 	 * <p>
 	 * A <code>null</code> input String returns <code>null</code>.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.strip(null)     = null
 	 * StringUtils.strip("")       = ""
 	 * StringUtils.strip("   ")    = ""
 	 * StringUtils.strip("abc")    = "abc"
 	 * StringUtils.strip("  abc")  = "abc"
 	 * StringUtils.strip("abc  ")  = "abc"
 	 * StringUtils.strip(" abc ")  = "abc"
 	 * StringUtils.strip(" ab c ") = "ab c"
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to remove whitespace from, may be null
 	 * @return the stripped String, <code>null</code> if null String input
 	 */
 	public static String strip(String str) {
 		return strip(str, null);
 	}
 
 	/**
 	 * <p>
 	 * Strips whitespace from the start and end of a String returning <code>null</code> if the String is empty ("") after the strip.
 	 * </p>
 	 * 
 	 * <p>
 	 * This is similar to {@link #trimToNull(String)} but removes whitespace. Whitespace is defined by {@link Character#isWhitespace(char)}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.stripToNull(null)     = null
 	 * StringUtils.stripToNull("")       = null
 	 * StringUtils.stripToNull("   ")    = null
 	 * StringUtils.stripToNull("abc")    = "abc"
 	 * StringUtils.stripToNull("  abc")  = "abc"
 	 * StringUtils.stripToNull("abc  ")  = "abc"
 	 * StringUtils.stripToNull(" abc ")  = "abc"
 	 * StringUtils.stripToNull(" ab c ") = "ab c"
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to be stripped, may be null
 	 * @return the stripped String, <code>null</code> if whitespace, empty or null String input
 	 * @since 2.0
 	 */
 	public static String stripToNull(String str) {
 		if (str == null) {
 			return null;
 		}
 		str = strip(str, null);
 		return str.length() == 0 ? null : str;
 	}
 
 	/**
 	 * <p>
 	 * Strips any of a set of characters from the start and end of a String. This is similar to {@link String#trim()} but allows the characters to be stripped to be controlled.
 	 * </p>
 	 * 
 	 * <p>
 	 * A <code>null</code> input String returns <code>null</code>. An empty string ("") input returns the empty string.
 	 * </p>
 	 * 
 	 * <p>
 	 * If the stripChars String is <code>null</code>, whitespace is stripped as defined by {@link Character#isWhitespace(char)}. Alternatively use {@link #strip(String)}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.strip(null, *)          = null
 	 * StringUtils.strip("", *)            = ""
 	 * StringUtils.strip("abc", null)      = "abc"
 	 * StringUtils.strip("  abc", null)    = "abc"
 	 * StringUtils.strip("abc  ", null)    = "abc"
 	 * StringUtils.strip(" abc ", null)    = "abc"
 	 * StringUtils.strip("  abcyx", "xyz") = "  abc"
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to remove characters from, may be null
 	 * @param stripChars
 	 *            the characters to remove, null treated as whitespace
 	 * @return the stripped String, <code>null</code> if null String input
 	 */
 	public static String strip(String str, String stripChars) {
 		if (isEmpty(str)) {
 			return str;
 		}
 		str = stripStart(str, stripChars);
 		return stripEnd(str, stripChars);
 	}
 
 	/**
 	 * <p>
 	 * Strips any of a set of characters from the start of a String.
 	 * </p>
 	 * 
 	 * <p>
 	 * A <code>null</code> input String returns <code>null</code>. An empty string ("") input returns the empty string.
 	 * </p>
 	 * 
 	 * <p>
 	 * If the stripChars String is <code>null</code>, whitespace is stripped as defined by {@link Character#isWhitespace(char)}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.stripStart(null, *)          = null
 	 * StringUtils.stripStart("", *)            = ""
 	 * StringUtils.stripStart("abc", "")        = "abc"
 	 * StringUtils.stripStart("abc", null)      = "abc"
 	 * StringUtils.stripStart("  abc", null)    = "abc"
 	 * StringUtils.stripStart("abc  ", null)    = "abc  "
 	 * StringUtils.stripStart(" abc ", null)    = "abc "
 	 * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to remove characters from, may be null
 	 * @param stripChars
 	 *            the characters to remove, null treated as whitespace
 	 * @return the stripped String, <code>null</code> if null String input
 	 */
 	public static String stripStart(String str, String stripChars) {
 		int strLen;
 		if (str == null || (strLen = str.length()) == 0) {
 			return str;
 		}
 		int start = 0;
 		if (stripChars == null) {
 			while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
 				start++;
 			}
 		} else if (stripChars.length() == 0) {
 			return str;
 		} else {
 			while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
 				start++;
 			}
 		}
 		return str.substring(start);
 	}
 
 	/**
 	 * <p>
 	 * Strips any of a set of characters from the end of a String.
 	 * </p>
 	 * 
 	 * <p>
 	 * A <code>null</code> input String returns <code>null</code>. An empty string ("") input returns the empty string.
 	 * </p>
 	 * 
 	 * <p>
 	 * If the stripChars String is <code>null</code>, whitespace is stripped as defined by {@link Character#isWhitespace(char)}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * StringUtils.stripEnd(null, *)          = null
 	 * StringUtils.stripEnd("", *)            = ""
 	 * StringUtils.stripEnd("abc", "")        = "abc"
 	 * StringUtils.stripEnd("abc", null)      = "abc"
 	 * StringUtils.stripEnd("  abc", null)    = "  abc"
 	 * StringUtils.stripEnd("abc  ", null)    = "abc"
 	 * StringUtils.stripEnd(" abc ", null)    = " abc"
 	 * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
 	 * </pre>
 	 * 
 	 * @param str
 	 *            the String to remove characters from, may be null
 	 * @param stripChars
 	 *            the characters to remove, null treated as whitespace
 	 * @return the stripped String, <code>null</code> if null String input
 	 */
 	public static String stripEnd(String str, String stripChars) {
 		int end;
 		if (str == null || (end = str.length()) == 0) {
 			return str;
 		}
 
 		if (stripChars == null) {
 			while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
 				end--;
 			}
 		} else if (stripChars.length() == 0) {
 			return str;
 		} else {
 			while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
 				end--;
 			}
 		}
 		return str.substring(0, end);
 	}
 
 }
