 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.text;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Whisl encoding utility functions. This class supports both encoding and
  * decoding.
  *
  * <p>The Whisl encoding is similar to URL encoding, but specifically meant to
  * support the complete Unicode character set.
  *
  * <p>The following transformations should be applied when decoding:
  *
  * <ul>
  *    <li>a plus sign (<code>'+'</code>) converts to a space character;
  *    <li>a percent sign (<code>'%'</code>) must be followed by a 2-digit hex
  *        number that indicate the Unicode value of a single character;
  *    <li>a dollar sign (<code>'$'</code>) must be followed by a 4-digit hex
  *        number that indicate the Unicode value of a single character;
  * </ul>
  *
  * <p>TODO: Add decode method.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class WhislEncoding extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Mappings from unencoded (array index) to encoded values (array
     * elements) for characters that can be encoded using the percent sign. The
     * size of this array is 128.
     */
    private static final String[] UNENCODED_TO_ENCODED;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    static {
       UNENCODED_TO_ENCODED = new String[128];
       for (int i = 0; i < 128; i++) {
          char c = (char) i;
          if ((c >= 'a' && c <= 'z')   || (c >= 'A' && c <= 'Z')   || (c >= '0' && c <= '9')
           || (c == '-') || (c == '_') || (c == '.') || (c == '*')) {
             UNENCODED_TO_ENCODED[i] = String.valueOf(c);
          } else if (c == ' ') {
             UNENCODED_TO_ENCODED[i] = "+";
          } else {
             char[] data = new char[3];
             data[0] = '%';
             data[1] = Character.toUpperCase(Character.forDigit((i >> 4) & 0xF, 16));
             data[2] = Character.toUpperCase(Character.forDigit( i       & 0xF, 16));
             UNENCODED_TO_ENCODED[i] = new String(data);
          }
       }
    }
 
    /**
     * Whisl encodes the specified character string.
     *
     * @param s
     *    the string to Whisl encode, not <code>null</code>.
     *
     * @return
     *    Whisl encoded version of the specified character string, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>
     */
    public static String encode(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Short-circuit if the string is empty
       int length = s.length();
       if (length < 1) {
         return;
       }
 
       // Construct a buffer
       FastStringBuffer buffer = new FastStringBuffer(length * 2);
 
       // Loop through the string. If the character is less than 128 then get
       // from the cache array, otherwise convert escape with a dollar sign
       for (int i = 0; i < length; i++) {
          int c = (int) s.charAt(i);
          if (c < 128) {
             buffer.append(UNENCODED_TO_ENCODED[c]);
          } else {
             buffer.append('$');
             HexConverter.toHexString(buffer, c);
          }
       }
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>WhislEncoding</code> object.
     */
    private WhislEncoding() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
