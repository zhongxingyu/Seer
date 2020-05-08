 /*
  * $Id: TextUtils.java,v 1.29 2007/05/22 09:36:09 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.text;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Properties;
 import java.security.MessageDigest;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.ProgrammingException;
 import org.xins.common.Utils;
 
 /**
  * Text-related utility functions.
  *
  * @version $Revision: 1.29 $ $Date: 2007/05/22 09:36:09 $
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.0.0
  */
 public final class TextUtils {
 
    /**
     * Constructs a new <code>TextUtils</code> object.
     */
    private TextUtils() {
       // empty
    }
 
    /**
     * Quotes the specified string, or returns <code>"(null)"</code> if it is
     * <code>null</code>.
     *
     * @param s
     *    the input string, or <code>null</code>.
     *
     * @return
     *    if <code>s != null</code> the quoted string, otherwise the string
     *    <code>"(null)"</code>.
     */
    public static String quote(String s) {
       return s == null ? "(null)" : "\"" + s + '"';
    }
 
    /**
     * Quotes the textual presentation (returned by <code>toString()</code>) of
     * the specified object, or returns <code>"(null)"</code> if the object is
     * <code>null</code>.
     *
     * @param o
     *    the object, or <code>null</code>.
     *
     * @return
     *    if <code>o != null</code> then <code>o.toString()</code> quoted,
     *    otherwise the string <code>"(null)"</code>.
     *
     * @since XINS 1.0.1
     */
    public static String quote(Object o) {
       String s = (o == null) ? null : o.toString();
       return quote(s);
    }
 
    /**
     * Determines if the specified string is <code>null</code> or an empty
     * string.
     *
     * @param s
     *    the string, or <code>null</code>.
     *
     * @return
     *    <code>true</code> if <code>s == null || s.length() &lt; 1</code>.
     *
     * @since XINS 1.0.1
     */
    public static boolean isEmpty(String s) {
       return (s == null) || (s.length() == 0);
    }
 
    /**
     * Determines if the specified string is <code>null</code> or an empty
     * string, optionally considering whitespace as empty.
     *
     * @param s
     *    the string, or <code>null</code>.
     *
     * @param trim
     *    flag that indicates whether the string should be trimmed before
     *    checking if the length is 0.
     *
     * @return
     *    <code>true</code> if the specified string is <code>null</code> or
     *    empty.
     *
     * @since XINS 3.0
     */
    public static boolean isEmpty(String s, boolean trim) {
       if (s == null) {
          return true;
       } else if (s.length() == 0) {
          return true;
       } else if (trim && s.trim().length() == 0) {
          return true;
       } else {
          return false;
       }
    }
 
    /**
     * Trims the specified string, or returns the indicated string if the
     * argument is <code>null</code>.
     *
     * @param s
     *    the string, or <code>null</code>.
     *
     * @param ifEmpty
     *    the string to return if
     *    <code>s == null || s.trim().length &lt; 1</code>;
     *    can itself be <code>null</code>.
     *
     * @return
     *    the trimmed version of the string (see {@link String#trim()}) or
     *    (in case <code>s == null</code> or <code>s.trim().length &lt; 1</code>):
     *    <code>ifEmpty</code>.
     *
     * @since XINS 1.3.0
     */
    public static String trim(String s, String ifEmpty) {
 
       if (s != null) {
          s = s.trim();
          if (s.length() >= 1) {
             return s;
          }
       }
 
       return ifEmpty;
    }
 
    /**
     * Removes all leading and trailing whitespace from a string, and replaces
     * all internal whitespace with a single space character.
     * If <code>null</code> is passed, then <code>""</code> is returned.
     *
     * @param s
     *    the string, or <code>null</code>.
     *
     * @return
     *    the string with all leading and trailing whitespace removed and all
     *    internal whitespace normalized, never <code>null</code>.
     *
     * @since XINS 3.0
     */
    public static String normalizeWhitespace(String s) {
       String normalized = "";
       if (s != null) {
          s = s.trim();
          boolean prevIsWhitespace = false;
          for (int i = 0; i < s.length(); i++) {
             char                   c = s.charAt(i);
             boolean thisIsWhitespace = (c <= 0x20);
 
             if (thisIsWhitespace && prevIsWhitespace) {
                // skip this one
             } else if (thisIsWhitespace) {
                normalized      += ' ';
                prevIsWhitespace = true;
             } else {
                normalized      += c;
                prevIsWhitespace = false;
             }
          }
       }
       return normalized;
    }
 
    /**
     * Removes all whitespace from a string.
     * If <code>null</code> is passed, then <code>""</code> is returned.
     *
     * @param s
     *    the string, or <code>null</code>.
     *
     * @return
     *    a string without any whitespace characters,
     *    never <code>null</code>.
     *
     * @since XINS 3.0
     */
    public static String removeWhitespace(String s) {
       String normalized = "";
       if (s != null) {
          for (int i = 0; i < s.length(); i++) {
             char                   c = s.charAt(i);
             boolean thisIsWhitespace = (c <= 0x20);
 
             if (thisIsWhitespace) {
                // skip this one
             } else {
                normalized += c;
             }
          }
       }
       return normalized;
    }
 
    /**
     * Replaces substrings in a string. The substrings to be replaced are
     * passed in a {@link Properties} object. A prefix and a suffix can be
     * specified. These are prepended/appended to each of the search keys.
     *
     * <p />Example: If you have a string <code>"Hello ${name}"</code> and you
     * would like to replace <code>"${name}"</code> with <code>"John"</code>
     * and you would like to replace <code>${surname}</code> with
     * <code>"Doe"</code>, use the following code:
     *
     * <p /><blockquote><code>String s = "Hello ${name}";
     * <br />Properties replacements = new Properties();
     * <br />replacements.put("name", "John");
     * <br />replacements.put("surname", "Doe");
     * <br />
     * <br />StringUtils.replace(s, replacements, "${", "}");</code></blockquote>
     *
     * <p />The result string will be <code>"Hello John"</code>.
     *
     * @param s
     *    the text string to which replacements should be applied, not <code>null</code>.
     *
     * @param replacements
     *    the replacements to apply, not <code>null</code>.
     *
     * @param prefix
     *    the optional prefix for the search keys, or <code>null</code>.
     *
     * @param suffix
     *    the optional prefix for the search keys, or <code>null</code>.
     *
     * @return the String with the replacements.
     *
     * @throws IllegalArgumentException
     *    if one of the mandatory arguments is missing.
     *
     * @since XINS 1.4.0.
     */
    public static String replace(String s, Properties replacements, String prefix, String suffix)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s, "replacements", replacements);
 
       // Make sure prefix and suffix are not null
       prefix = (prefix == null) ? "" : prefix;
       suffix = (suffix == null) ? "" : suffix;
 
       Enumeration keys = replacements.propertyNames();
       while (keys.hasMoreElements()) {
          String key    = (String) keys.nextElement();
          String search = prefix + key + suffix;
          int index = s.indexOf(search);
          while (index >= 0) {
             String replacement = replacements.getProperty(key);
             s = s.substring(0, index) + replacement + s.substring(index + search.length());
             index = s.indexOf(search);
          }
       }
 
       return s;
    }
 
    /**
     * Tranforms the given <code>String</code> to the similar <code>String</code>,
     * but starting with an uppercase.
     *
     * @param text
     *    the text to transform, can be <code>null</code>
     *
     * @return
     *    the transformed text, the return value will start with an uppercase.
     *    <code>null</code> is returned if the text was <code>null</code>.
     *
     * @since XINS 2.0.
     */
    public static String firstCharUpper(String text) {
        if (text == null) {
            return null;
        } else if (text.length() == 0) {
            return text;
        } else if (!Character.isLowerCase(text.charAt(0))) {
            return text;
        } else if (text.length() == 1) {
            return text.toUpperCase();
        } else {
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
 
    }
 
    /**
     * Tranforms the given <code>String</code> to the similar <code>String</code>,
     * but starting with a lowercase.
     *
     * @param text
     *    the text to transform, can be <code>null</code>
     *
     * @return
     *    the transformed text, the return value will start with a lowercase.
     *    <code>null</code> is returned if the text was <code>null</code>.
     *
     * @since XINS 2.0.
     */
    public static String firstCharLower(String text) {
        if (text == null) {
            return null;
        } else if (text.length() == 0) {
            return text;
        } else if (!Character.isUpperCase(text.charAt(0))) {
            return text;
        } else if (text.length() == 1) {
            return text.toLowerCase();
        } else {
            return text.substring(0, 1).toLowerCase() + text.substring(1);
        }
    }
 
    /**
     * Removes the given character from the given <code>String</code>.
     *
     * @param charToRemove
     *     the character that should be removed from the String.
     *
     * @param text
     *     the text from which the charecter should be removed, can be <code>null</code>.
     *
     * @return
     *    the text without the character or <code>null</code> if the input text is <code>null</code>.
     *
     * @since XINS 2.0.
     */
    public static String removeCharacter(char charToRemove, String text) {
       if (text == null) {
          return null;
       }
       if (text.indexOf(charToRemove) == -1) {
          return text;
       }
       char[] inputText = text.toCharArray();
       StringBuffer result = new StringBuffer(inputText.length);
       for (int i = 0; i < inputText.length; i++) {
          if (inputText[i] != charToRemove) {
             result.append(inputText[i]);
          }
       }
       if (result.length() == inputText.length) {
          return text;
       } else {
          return result.toString();
       }
    }
 
    /**
     * Computes the message digest of the specified string. The text string is
     * first converted to bytes using the UTF-8 encoding.
     *
     * <p>If an unsupported algorithm is passed, then a
     * {@link ProgrammingException} will be thrown.
     *
     * <p>Hint: to convert the returned <code>byte</code> array to a string,
     * you may want to use {@link #hashToString(String,String)}.
     *
     * @param algorithm
     *    the algorithm to use, such as <code>"SHA-1"</code>,
     *    <code>"SHA-256"</code> or <code>"MD5"</code>;
     *    cannot be <code>null</code>.
     *
     * @param s
     *    the text {@link String} to hash, cannot be <code>null</code>.
     *
     * @return
     *    the hash as a <code>byte</code> array, as a <code>long</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>algorithm == null || s == null</code>
     *
     * @since XINS 3.0
     */
    public static final byte[] hash(String algorithm, String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("algorithm", algorithm, "s", s);
    
       // Compute the message digest
       MessageDigest  md = null;
       String  className = "java.lang.String";
       String methodName = "getBytes(java.lang.String)";
       try {
 
          // Convert the string to bytes
          byte[] bytes = s.getBytes("UTF-8");
 
          // Get a MessageDigest instance for the SHA-256 algorithm
          className  = "MessageDigest";
          methodName = "getInstance(java.lang.String)";
          md = MessageDigest.getInstance(algorithm);
 
          // Add the bytes to the digest computer
          methodName = "update(byte[])";
          md.update(bytes);
 
       // Something went wrong
       } catch (Throwable cause) {
          throw Utils.logProgrammingError(TextUtils.class.getName(), "hash(java.lang.String)", className, methodName, cause);
       }
 
       return md.digest();
    }
 
    /**
     * Computes the message digest of the specified string and converts it to a
     * hex string.
     *
     * <p>If an unsupported algorithm is passed, then a
     * {@link ProgrammingException} will be thrown.
     *
     * @param algorithm
     *    the algorithm to use, such as <code>"SHA-1"</code>,
     *    <code>"SHA-256"</code> or <code>"MD5"</code>;
     *    cannot be <code>null</code>.
     *
     * @param s
     *    the text {@link String} to hash, cannot be <code>null</code>.
     *
     * @return
     *    the hash, converted to a hex string, e.g.
     *    <code>"7b0b662c93ccc19e"</code>, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>algorithm == null || s == null</code>
     *
     * @since XINS 3.0
     */
    public static final String hashToString(String algorithm, String s)
    throws IllegalArgumentException {
       return HexConverter.toHexString(hash(algorithm, s));
    }
 
    /**
     * Returns the specified character string or <code>null</code> if the
     * string is empty. The function {@link #isEmpty(String)} is used to
     * determine if the string is considered empty.
     *
     * @param s
     *    the character {@link String}, can be <code>null</code>.
     *
     * @return
     *    the input string (<code>s</code>) if it is not empty,
     *    otherwise <code>null</code>.
     *
     * @since XINS 3.0
     */
    public static final String nullIfEmpty(String s) {
       return TextUtils.isEmpty(s) ? null : s;
    }
 
    /**
     * Converts the specified string to bytes, using the UTF-8 encoding. This
     * method is equivalent to calling:
     *
     * <blockquote><pre>string.getBytes("UTF-8")</pre></blockquote>
     *
     * expect that this method does not throw a checked exception.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws ProgrammingException
     *    if the UTF-8 encoding is not supported.
     *
     * @since XINS 3.0
     */
    public static byte[] toUTF8(String string)
    throws IllegalArgumentException, ProgrammingException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("string", string);
 
       String encoding = "UTF-8";
       try {
          return string.getBytes(encoding);
       } catch (UnsupportedEncodingException cause) {
          throw Utils.logProgrammingError(Utils.class.getName(),
                                          "toUTF8(String)",
                                          "java.lang.String",
                                          "getBytes(String)",
                                          "Encoding \"" + encoding + "\" is not supported.",
                                          cause);
       }
    }
 
    /**
     * Converts the specified collection of strings into a textual list. For
     * example, consider a list with the following items:
     *
     * <ol>
     * <li><code>"bla"</code>
     * <li><code>"foo"</code>
     * <li><code>"bar"</code>
     * </li>
     *
     * <p>When this method is called with <code>between</code> set to
     * <code>", "</code> and <code>beforeLast</code> set to
     * <code>" and "</code>, the result is:
     *
     * <blockquote><pre>"bla, foo and bar"</pre></blockquote>
     *
     * @param input
     *    the elements, cannot be <code>null</code>.
     *
     * @param between
     *    the text in between items, except before the last one,
     *    cannot be <code>null</code>.
     *
     * @param beforeLast
     *    the text before the last item,
     *    cannot be <code>null</code>.
     *
     * @param quote
     *    flag that indicates if each item should be quoted
     *    (see {@link #quote(String)}).
     *
     * @return
     *    the result string, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>input      == null
     *          || between    == null
     *          || beforeLast == null</code>.
     *
     * @since XINS 3.0
     */
    public static String list(Collection input, String between, String beforeLast, boolean quote)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("input", input, "between", between, "beforeLast", beforeLast);
 
       // No items, empty result
       int itemCount = input.size();
       if (itemCount < 1) {
          return "";
       }
 
       // First one
      Iterator iterator = input.iterator();
      Object       item = iterator.next();
      String     result = quote ? quote(item) : String.valueOf(item);
 
       // All in between
       for (int i = 1; i < (itemCount - 1); i++) {
          result += between;
          item    = iterator.next();
          result += quote ? quote(item) : String.valueOf(item);
       }
 
       // Last one
       if (itemCount > 1) {
          result += beforeLast;
          item    = iterator.next();
          result += quote ? quote(item) : String.valueOf(item);
       }
 
       return result;
    }
 
    /**
     * Retrieves an enumeration item by name. A flag indicates if the character
     * string can be normalized before finding an enum item.
     *
     * @param enumType
     *    the {@link Enum} class, cannot be <code>null</code>.
     *
     * @param name
     *    the name of the item to find, cannot be <code>null</code>.
     *
     * @param fuzzy
     *    <code>true</code> if a fuzzy match is allowed,
     *    <code>false</code> if an exact match is required.
     *
     * @return
     *    the matching {@link Enum} item,
     *    or <code>null</code> if none could be found.
     *
     * @throws IllegalArgumentException
     *    if <code>enumType == null || name == null</code>.
     *
     * @since XINS 3.0
     */
    public static final <T extends Enum<T>> T getEnumItem(Class<T> enumType, String name, boolean fuzzy)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("enumType", enumType, "name", name);
 
       if (fuzzy) {
          name = name.trim().replaceAll("\\s+", "_").toUpperCase();
       }
 
       // Find an enum item by that name
       try {
          return Enum.valueOf(enumType, name);
 
       // No such Enum item
       } catch (IllegalArgumentException cause) {
          return null;
       }
    }
 
    /**
     * Retrieves an enumeration item by name, normalizing the name.
     *
     * @param enumType
     *    the {@link Enum} class, cannot be <code>null</code>.
     *
     * @param name
     *    the name of the item to find, cannot be <code>null</code>.
     *
     * @return
     *    the matching {@link Enum} item,
     *    or <code>null</code> if none could be found.
     *
     * @throws IllegalArgumentException
     *    if <code>enumType == null || name == null</code>.
     *
     * @since XINS 3.0
     */
    public static final <T extends Enum<T>> T getEnumItem(Class<T> enumType, String name)
    throws IllegalArgumentException {
       return getEnumItem(enumType, name, true);
    }
 }
