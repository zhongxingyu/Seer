 package com.github.mikiwiik;
 
 import com.google.common.collect.ImmutableList;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 /**
  * Miscellaneous bit-operation related util methods
  */
 public class BitUtils {
 
     /**
      * Convert the given String into Lists of boolean values representing the String as Ascii bits
      * Inspired by
      *      <a href="http://stackoverflow.com/questions/917163/convert-a-string-like-testing123-to-binary-in-java">
      *      http://stackoverflow.com/questions/917163/convert-a-string-like-testing123-to-binary-in-java</a>
      * @param string
      * @return
      * @throws UnsupportedEncodingException
      */
     public static List<Boolean> asciiAsBinary(String string) throws UnsupportedEncodingException {
 
         final ImmutableList.Builder<Boolean> bits = new ImmutableList.Builder<Boolean>();
 
         // Could do byte[] bytes = string.getBytes("US-ASCII");
         byte[] bytes = string.getBytes();
         for (byte b : bytes) {
             int val = b;
             for (int i = 0; i < 8; i++) {
                 bits.add((val & 128) != 0);
                 val <<= 1;
             }
         }
         return bits.build();
     }
 
     /**
     * Convert the given char into a String containing whitespace representation of a character number.<br/>
     * From:
     * <a href="http://compsoc.dur.ac.uk/whitespace/tutorial.php">http://compsoc.dur.ac.uk/whitespace/tutorial.php</a>:
     * <blockquote>Numbers can be any number of bits wide, and are simply represented as a series of [Space] and [Tab],
     * terminated by a [LF]. [Space] represents the binary digit 0, [Tab] represents 1. The sign of a number is given
     * by its first character, [Space] for positive and [Tab] for negative. Note that this is not twos complement, it
     * just indicates a sign.</blockquote>
     *
      * <br/>
      * NOTE: Behaviour is very much undefined for non-ascii chars.
      *
      * @param character
      * @return 
      */
     public static String charAsWspaceNumber(char character) {
         // NOTE: all characters are positive wspace numbers.
         final StringBuilder stringBuilder = new StringBuilder(" ");
 
         int asciiValue = (int)character;
         for (int i = 0; i < 8; i++) {
             final boolean binaryValue = (asciiValue & 128) != 0;
             stringBuilder.append(binaryValue ? '\t' : ' ');
             asciiValue <<= 1;
         }
        // Numbers are terminated by a [LF].
        stringBuilder.append('\n');
         return stringBuilder.toString();
     }
 }
