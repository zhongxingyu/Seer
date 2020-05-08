 /*
  * Sep 13, 2003
  */
 package com.thinkparity.codebase;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 import com.thinkparity.codebase.SystemUtil.SystemProperty;
 
 /**
  * String utility functions.
  * 
  * @author raykroeker@gmail.com
  * @version 1.2
  */
 public abstract class StringUtil {
 
 	/**
 	 * Build a name StringBuffer checking for null.
 	 * @param first <code>java.lang.String</code>
 	 * @param second <code>java.lang.String</code>
 	 * @param separator <code>StringUtil$Separator</code>
 	 * @return <code>java.lang.StringBuffer</code>
 	 */	
 	public static synchronized StringBuffer buildName(
 		String first,
 		String second,
 		Separator separator) {
 		if(null == separator)
 			throw new NullPointerException();
 		StringBuffer name = new StringBuffer();
 		if(null != first && 0 < first.length()) {
 			if(null != second && 0 < second.length()) {
 				name.append(second)
 					.append(separator)
 					.append(first);
 			}
 			else {
 				name.append(first);
 			}
 		}
 		else {
 			if(null != second  && 0 < second.length()) {
 				name.append(second);
 			}
 		}
 		return name;
 	}
 	
 	/**
 	 * Build a name StringBuffer checking for null.
 	 * @param first <code>java.lang.StringBuffer</code>
 	 * @param second <code>java.lang.StringBuffer</code>
 	 * @param separator <code>StringUtil$Separator</code>
 	 * @return <code>java.lang.StringBuffer</code>
 	 */
 	public static synchronized StringBuffer buildName(
 		StringBuffer first,
 		StringBuffer second,
 		Separator separator) {
 		return buildName(
 			null == first ? null : first.toString(),
 			null == second ? null : second.toString(),
 			separator);
 	}
 
 	/**
      * Print the stack trace of the throwable to a string and return it.
      *
      * @param t
      *      A throwable.
      * @return A printed stack trace.
      */
     public static String printStackTrace(final Throwable t) {
         final StringWriter sw = new StringWriter();
 		t.printStackTrace(new PrintWriter(sw));
 		return sw.toString();
     }
 	
     /**
      * Remove any characters before the find string within the search string.
      * @param search String
      * @param find String
      * @return String
      */
     public static synchronized String removeBefore(final String search,
             final String find) {
         if(null == search)
             return null;
         if(null == find)
             return search;
         final Integer findIndex = search.indexOf(find);
         if(findIndex > 0)
             return search.substring(findIndex+find.length());
         return search;
     }
     
 	/**
 	 * Remove any characters after the find string within the search string.
 	 * @param search String
 	 * @param find String
 	 * @return String
 	 */
 	public static synchronized String removeAfter(final String search,
 			final String find) {
 		if(null == search)
 			return null;
 		if(null == find)
 			return search;
 		final Integer findIndex = search.lastIndexOf(find);
 		if(findIndex > 0)
 			return search.substring(0, findIndex);
 		return search;
 	}
 
 	/**
 	 * Remove the whitespace from target, replacing it with replacement or the 
 	 * empty string if replacement is <code>null</code>.
 	 * @param target <code>java.lang.String</code>
 	 * @param replacement <code>StringUtil$Separator</code>
 	 * @return <code>java.lang.StringBuffer</code>
 	 */	
 	public static synchronized StringBuffer removeWhitespace(
 		String target,
 		Separator replacement) {
 		return StringUtil.removeWhitespace(
 			target,
 			null == replacement ? null : replacement.toString());
 	}
 
 	/**
 	 * Remove the whitespace from target, replacing it with replacement or the
 	 * empty string if replacement is <code>null</code>.
 	 * @param target <code>java.lang.String</code>
 	 * @param replacement <code>java.lang.String</code>
 	 * @return <code>java.lang.StringBuffer</code>
 	 */
 	public static final StringBuffer removeWhitespace(
 		String target,
 		String replacement) {
 		if(null == target)
 			return null;
 		if(null == replacement)
 			replacement = "";
 		StringBuffer buffer = new StringBuffer();
 		char[] targetChars = target.toCharArray();
 		for(int i = 0; i < targetChars.length; i++) {
 			if( Character.isWhitespace(targetChars[i]))
 				buffer.append(replacement);
 			else
 				buffer.append(targetChars[i]);
 		}
 		return buffer;
 	}
 
     /**
 	 * Remove the whitespace from target, replacing it with replacement or the
 	 * empty string if replacement is <code>null</code>.
 	 * @param target <code>java.lang.StringBuffer</code>
 	 * @param replacement <code>java.lang.StringBuffer</code>
 	 * @return <code>java.lang.StringBuffer</code>
 	 */
 	public static synchronized StringBuffer removeWhitespace(
 		StringBuffer target,
 		StringBuffer replacement) {
 		return StringUtil.removeWhitespace(
 			null == target ? null : target.toString(),
 			null == replacement ? null : replacement.toString());
 	}
 
 	/**
      * Reverse the order of the characters in a string.
      * 
      * @param string
      *            A <code>String</code>.
      * @return A reversed <code>String</code>
      */
     public static String reverse(final String string) {
         if (null == string)
             return null;
         final char[] chars = string.toCharArray();
         final StringBuffer buffer = new StringBuffer(chars.length);
         for (int i = chars.length - 1; i > -1; i--) {
             buffer.append(chars[i]);
         }
         return buffer.toString();
     }
 
     /**
      * Does a search and replace on a string based on the find and replace
      * criteria. This method will replace all occurances of find in search with
      * replace.
      * 
      * @param search
      *            The search input <code>String</code>.
      * @param find
      *            The find <code>Separator</code>.
      * @param replace
      *            The replacement <code>Separator</code>.
      * @return The result <code>String</code>.
      */
     public static String searchAndReplace(final String search,
             final Separator find, final Separator replace) {
         return searchAndReplace(search, find.toString(), replace.toString())
                 .toString();
     }
 
     /**
      * Does a search and replace on a string based on the find and replace
      * criteria.  This method will replace all occurances of find in search 
      * with replace.
      * @param search <code>java.lang.StringUtil</code> the string to 
      * search through
      * @param find <code>java.lang.StringUtil</code> the text to look for
      * @param replace <code>java.lang.StringUtil</code> the text to replace
      * @return <code>java.lang.StringBuffer</code> the new text with 
      * the text replaced
      */
 	public static StringBuffer searchAndReplace(
 		String search,
 		String find,
 		String replace) {
     	if(null == search)
     		return null;
     	if(null == find)
     		return new StringBuffer(search);
     	if(null == replace)
     		throw new IllegalArgumentException("replace must be provided.");
         StringBuffer processedString = new StringBuffer();
         int findStart = search.indexOf(find);
         int currentPos = 0;
         while(-1 != findStart) {
             processedString.append(search.substring(currentPos, findStart));
             processedString.append(replace);
             currentPos = findStart + find.length();
             findStart = search.indexOf(find, currentPos);
         }
         processedString.append(search.substring(currentPos));
         return processedString;
     }
 
 	/**
      * Does a search and replace on a string based on the find and replace
      * criteria.  This method will replace all occurances of find in search 
      * with replace.
      * @param search <code>java.lang.StringUtil</code> the string to 
      * search through
      * @param find <code>java.lang.StringUtil</code> the text to look for
      * @param replace <code>java.lang.StringUtil</code> the text to replace
      * @return <code>java.lang.StringBuffer</code> the new text with 
      * the text replaced
      */
 	public static StringBuffer searchAndReplace(
 		StringBuffer search,
 		String find,
 		String replace) {
 		return StringUtil.searchAndReplace(
 			null == search ? null : search.toString(),
 			find,
 			replace);
 	}
 
     /**
 	 * Does a search and replace on a string based on the find and replace
 	 * criteria.  This method will replace all occurances of find in search 
 	 * with replace.
 	 * @param search <code>java.lang.StringBuffer</code> the string to 
 	 * search through
 	 * @param find <code>java.lang.StringBuffer</code> the text to look for
 	 * @param replace <code>java.lang.StringBuffer</code> the text to replace
 	 * @return <code>java.lang.StringBufferBuffer</code> the new text with 
 	 * the text replaced
 	 */
 	public static StringBuffer searchAndReplace(
 		StringBuffer search,
 		StringBuffer find,
 		StringBuffer replace) {
 		return StringUtil.searchAndReplace(
 			null == search ? null : search.toString(),
 			null == find ? null : find.toString(),
 			null == replace ? null : replace.toString());
     }
 
     /**
      * Generated the toString for an object.
      * 
      * @param type
      *            The object type <code>Class</code>.
      * @param memberData
      *            An array of member names and values.
      * @return A <code>String</code> representation of the type.
      */
     public static String toString(final Class type, final Object... memberData) {
         final StringBuffer toString = new StringBuffer(type.getName())
             .append("$");
         if (1 == memberData.length)
             if (null == memberData[0])
                 return toString.append(Separator.Null).toString();
         for (int i = 0; i < memberData.length; i++) {
             toString.append(null == memberData[i] ? Separator.Null : memberData[i]);
             if (0 == i % 2) {
                 // a "label"
                 toString.append(":");
                 if (i + 1 >= memberData.length) {
                     toString.append(Separator.Null);
                 }
             }
             else {
                 // a "value"
                 if (i + 1 < memberData.length) {
                     toString.append(",");
                 }
             }
         }
         return toString.toString();
     }
 
     public static List<String> tokenize(final String string,
             final Separator delimiter, final List<String> list) {
         return tokenize(string,
                 null == delimiter ? null : delimiter.toString(), list);
     }
 
 	public static List<String> tokenize(final String string,
             final String delimiter, final List<String> list) {
 	    if (null == string)
 	        throw new NullPointerException();
         if (null == delimiter)
             throw new NullPointerException();
         int currentIndex = 0;
         int nextIndex = string.indexOf(delimiter);
         if (-1 == nextIndex) { // the delimiter doesn't exist
             list.add(string);
         }
         else {
             while (-1 != nextIndex) {
                 list.add(string.substring(currentIndex, nextIndex));
                currentIndex = nextIndex + 1;
                 nextIndex = string.indexOf(delimiter, currentIndex);
             }
             if (currentIndex < string.length()) {
                 list.add(string.substring(currentIndex));
             }
         }
         return list;
     }
 
 	/**
 	 * Create a StringUtil [Singleton]
 	 */
 	private StringUtil() { super(); }
 
 	/**
 	 * Provides an enumeration of some common character sets.  The enumerated
 	 * types also contain references to the actual java charsets.
 	 * @author raykroeker@gmail.com
 	 * @version 1.0.0
 	 * @see java.nio.charset.Charset
 	 */
 	public enum Charset {
 
 		ISO_8859_1("ISO-8859-1"), US_ASCII("US-ASCII"), UTF_16("UTF-16"),
 		UTF_16_BE("UTF-16BE"), UTF_16_LE("UTF-16LE"), UTF_8("UTF-8");
 
 		/**
 		 * Reference to the java charset for the enumerated type.
 		 */
 		private final java.nio.charset.Charset charset;
 
 		/**
 		 * The charset name.
 		 */
 		private final String charsetName;
 		
 		/**
 		 * Create a Charset. This will create a java charset reference upon
 		 * creation.
 		 * 
 		 * @param charsetName
 		 *            The name of the charset.
 		 * @see java.nio.charset.Charset#forName(java.lang.String)
 		 */
 		private Charset(final String charsetName) {
 			this.charsetName = charsetName;
 			this.charset = java.nio.charset.Charset.forName(charsetName);
 		}
 
 		/**
 		 * Obtain the java charset for the enumerated charset.
 		 * 
 		 * @return The java charset.
 		 */
 		public java.nio.charset.Charset getCharset() { return charset; }
 
 		/**
 		 * Obtain the charset name for the enumerated charset.
 		 * 
 		 * @return The charset name.
 		 */
 		public String getCharsetName() { return charsetName; }
 	}
 
 	/**
 	 * <b>Title:</b>  Separator
 	 * <br><b>Description:</b>  Separator is used as a place holder for various 
 	 * string constants commonly used in String manipulation.  
 	 * @author raykroeker@gmail.com
 	 * @version 1.0.1
 	 */
 	public static final class Separator extends Enum {
 
         /** The at character. */
         public static final Separator At = new Separator("@");
 
 		/**
 		 * Close parenthesis
 		 */
 		public static final Separator CloseParenthesis = new Separator(")");
 
 		/**
 		 * Close square bracket
 		 */
 		public static final Separator CloseSquareBracket =
 			new Separator("]");
 
 		/**
 		 * Comma
 		 */
 		public static final Separator Comma = new Separator(",");
 		
 		/**
 		 * Dash
 		 */
 		public static final Separator Dash = new Separator("-");
 
 		/**
 		 * Double quote
 		 */
 		public static final Separator DoubleQuote = new Separator("\"");
 
 		/**
 		 * Empty String
 		 */
 		public static final Separator EmptyString = new Separator("");
 
 		/**
 		 * Equals
 		 */
 		public static final Separator Equals = new Separator("=");
 
         /** The forward slash character. */
         public static final Separator ForwardSlash = new Separator("/");
 
 		/**
 		 * Full colon
 		 */
 		public static final Separator FullColon = new Separator(":");
 
 		/**
 		 * Greater Than
 		 */
 		public static final Separator GreaterThan = new Separator(">");
 
 		/**
 		 * Less Than
 		 */
 		public static final Separator LessThan = new Separator("<");
 
 		/**
 		 * Nix new line
 		 */
 		public static final Separator NixNewLine = new Separator("\n");
 
         /**
          * Double nix new line
          */
         public static final Separator DoubleNixNewLine =
             new Separator(NixNewLine.append(NixNewLine));
 
 		/**
 		 * Open parenthesis
 		 */
 		public static final Separator OpenParenthesis = new Separator("(");
 
 		/**
 		 * Open square bracket
 		 */
 		public static final Separator OpenSquareBracket =
 			new Separator("[");
 
 		/**
 		 * Question mark
 		 */
 		public static final Separator QuestionMark = new Separator("?");
 
 		/**
 		 * Semi colon
 		 */
 		public static final Separator SemiColon = new Separator(";");
 
 		/**
 		 * Single quote
 		 */
 		public static final Separator SingleQuote = new Separator("\'");
 
 		/**
 		 * Space
 		 */
 		public static final Separator Space = new Separator(" ");
 
         /**
          * Double space
          */
         public static final Separator DoubleSpace =
             new Separator(Space.append(Space));
 
         /**
          * Quadruple space
          */
         public static final Separator QuadrupleSpace =
             new Separator(DoubleSpace.append(DoubleSpace));
 
         /**
          * Period
          */
         public static final Separator Period = new Separator(".");
 
         /**
          * Period double space
          */
         public static final Separator PeriodDblSpace =
             new Separator(Period.append(DoubleSpace));
 
         /**
          * Comma space
          */
         public static final Separator CommaSpace =
             new Separator(Comma.append(Space));
 
 		/**
 		 * Space dash space
 		 */
 		public static final Separator SpaceDashSpace = 
 			new Separator(Space.append(Dash).append(Space));
 
 		/**
 		 * System's new line
 		 */
 		public static final Separator SystemNewLine =
 			new Separator(SystemUtil.getSystemProperty(SystemProperty.LineSeparator));
 
         /**
          * Double system new line
          */
         public static final Separator DoubleSystemNewLine =
             new Separator(SystemNewLine.append(SystemNewLine));
 
 		/**
 		 * TabId
 		 */
 		public static final Separator Tab = new Separator("\t");
 
         /**
          * Double tab
          */
         public static final Separator DoubleTab = new Separator(Tab.append(Tab));
 
 		/**
 		 * Underscore
 		 */
 		public static final Separator Underscore = new Separator("_");
 
 		/**
 		 * Windows new line
 		 */
 		public static final Separator WindowsNewLine = new Separator("\r\n");
 
         /**
          * Double windows new line
          */
         public static final Separator DoubleWindowsNewLine =
             new Separator(WindowsNewLine.append(WindowsNewLine));
 
 		/**
 		 * Windows new line tab
 		 */
 		public static final Separator WindowsNewLineTab =
 			new Separator(WindowsNewLine.append(Tab));
 
         /** Null. */
         public static final Separator Null = new Separator("null");
 
 		private static final long serialVersionUID = 1;
 
 		/**
 		 * Create a new Separator
 		 * @param separator <code>java.lang.String</code>
 		 */
 		private Separator(String separator) {
 			this(new StringBuffer().append(separator));
 		}
 
 		/**
 		 * Create a new Separator
 		 * @param separator <code>java.lang.StringBuffer</code>
 		 */
 		private Separator(StringBuffer separator) {super(separator);}
 		
 		/**
 		 * Append to the end of this Separator, the value of separator
 		 * @param separator <code>StringUtil$Separator</code>
 		 * @return <code>java.lang.StringBuffer</code>
 		 */
 		private StringBuffer append(Separator separator) {return toBuffer().append(separator);}
 		
 		/**
 		 * Obtain this Separator's value as a StringBuffer
 		 * @return <code>java.lang.StringBuffer</code>
 		 */
 		private StringBuffer toBuffer() {return new StringBuffer().append(toString());}
 	}
 }
