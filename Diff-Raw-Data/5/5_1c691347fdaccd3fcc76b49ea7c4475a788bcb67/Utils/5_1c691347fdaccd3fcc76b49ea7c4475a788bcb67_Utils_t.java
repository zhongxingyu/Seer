 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.plaf.compiler;
 
 import org.wings.Renderable;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.io.Device;
 import org.wings.style.Style;
 
 import java.awt.*;
 import java.io.IOException;
 
 /**
  * Utility functions to be used in generated plaf's.
  *
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  * @version $Revision$
  */
 public final class Utils implements SConstants {
     // fast conversion: translates directly into bytes (good for OutputStreams)
     private final static byte[] digits = "0123456789ABCDEF".getBytes();
 
     // byte representation of special characters
     private final static byte HASH_CHAR = (byte) '#';
     private final static byte MINUS_CHAR = (byte) '-';
     private final static byte SPACE = (byte) ' ';
     private final static byte[] EQUALS_QUOT = "=\"".getBytes();
     private final static byte QUOT = (byte) '"';
 
     /**
      * This is just a collection of static functions, thus not instanciable
      */
     private Utils() {
     }
 
     private static void quote(Device d, String s) throws IOException {
         quote(d, s, true);
     }
 
     /**
      * writes an {X|HT}ML quoted string according to RFC 1866.
      * '"', '<', '>', '&'  become '&quot;', '&lt;', '&gt;', '&amp;'
      */
     // not optimized yet
     private static void quote(Device d, String s, boolean quoteNewline) throws IOException {
         if (s == null) return;
         char[] chars = s.toCharArray();
         char c;
         int last = 0;
         for (int pos = 0; pos < chars.length; ++pos) {
             c = chars[pos];
             // write special characters as code ..
             if (c < 32 || c > 127) {
                 d.print(chars, last, (pos - last));
                 if (quoteNewline && (c == '\n' || (c == '\r' && (pos < chars.length && chars[pos+1] == '\n')))) {
                     d.print("<br>");
                     if ( c == '\r' ) pos++;
                 } else {
                     d.print("&#");
                     d.print((int) c);
                     d.print(';');
                 } // end of if ()
                 last = pos + 1;
             } else
                 switch (c) {
                     case '&':
                         d.print(chars, last, (pos - last));
                         d.print("&amp;");
                         last = pos + 1;
                         break;
                     case '"':
                         d.print(chars, last, (pos - last));
                         d.print("&quot;");
                         last = pos + 1;
                         break;
                     case '<':
                         d.print(chars, last, (pos - last));
                         d.print("&lt;");
                         last = pos + 1;
                         break;
                     case '>':
                         d.print(chars, last, (pos - last));
                         d.print("&gt;");
                         last = pos + 1;
                         break;
                         /*
                          * watchout: we cannot replace _space_ by &nbsp;
                          * since non-breakable-space is a different
                          * character: isolatin-char 160, not 32.
                          * This will result in a confusion in forms:
                          *   - the user enters space, presses submit
                          *   - the form content is written to the Device by wingS,
                          *     space is replaced by &nbsp;
                          *   - the next time the form is submitted, we get
                          *     isolatin-char 160, _not_ space.
                          * (at least Konqueror behaves this correct; mozilla does not)
                          *                                                       Henner
                          */
                 }
         }
         d.print(chars, last, chars.length - last);
     }
 
     public static void writeRaw(Device d, String s) throws IOException {
         if (s == null) return;
         d.print(s);
     }
 
    public static void writeQuoted(Device d, String s) throws IOException {
        if (s == null) return;
        quote(d, s, false);
    }

     /**
      * writes the given String to the device. The string is quoted, i.e.
      * for all special characters in *ML, their appropriate entity is
      * returned.
      * If the String starts with '<html>', the content is regarded being
      * HTML-code and is written as is (without the <html> tag).
      */
     public static void write(Device d, String s) throws IOException {
         if (s == null) return;
         if ((s.length() > 5) && (s.startsWith("<html>"))) {
             writeRaw(d, s.substring(6));
         } else {
             quote(d, s, false);
         }
     }
     
     /**
      * writes the given String to the device. The string is quoted, i.e.
      * for all special characters in *ML, their appropriate entity is
      * returned.
      * If the String starts with '<html>', the content is regarded being
      * HTML-code and is written as is (without the <html> tag).
      */
     public static void writeToolTip(Device d, String s) throws IOException {
       if (s == null) return;
       int pos = 0;
       StringBuffer sb = new StringBuffer(s);
       while ( (pos = sb.indexOf("'", pos)) != -1) {    /* Preprocess any of the ' chars */
         sb.insert(pos,'\\');
         pos+=2;
       }
       if ((s.length() > 5) && (s.startsWith("<html>"))) {
         writeRaw(d, sb.substring(6));
       } else {
         quote(d, sb.toString(), true);
       }
     }
     
     /**
      * Prints an optional attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(Device d, String attr, Style value)
             throws IOException {
         if (value != null) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             d.print(value.getName());
             d.write(QUOT);
         }
     }
 
     /**
      * Prints an optional attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(Device d, String attr, String value)
             throws IOException {
         if (value != null && value.length() > 0) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             quote(d, value);
             d.write(QUOT);
         }
     }
 
     /**
      * Prints an optional attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(Device d, String attr, Color value)
             throws IOException {
         if (value != null) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             write(d, value);
             d.write(QUOT);
         }
     }
 
     /**
      * Prints an optional, renderable attribute.
      */
     public static void optAttribute(Device d, String attr, Renderable r)
             throws IOException {
         if (r != null) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             r.write(d);
             d.write(QUOT);
         }
     }
 
     /**
      * Prints an optional attribute. If the integer value is greater than 0,
      * the attrib is added otherwise it is left out
      */
     public static void optAttribute(Device d, String attr, int value)
             throws IOException {
         if (value > 0) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             write(d, value);
             d.write(QUOT);
         }
     }
 
     /**
      * Prints an optional attribute. If the dimension value not equals <i>null</i>
      * the attrib is added otherwise it is left out
      */
     public static void optAttribute(Device d, String attr, SDimension value)
             throws IOException {
         if (value != null) {
             d.write(SPACE);
             d.print(attr);
             d.write(EQUALS_QUOT);
             write(d, value.toString());
             d.write(QUOT);
         }
     }
 
     /**
      * writes the given integer to the device. Speed optimized; character
      * conversion avoided.
      */
     public static void write(Device d, int num) throws IOException {
         int i = 10;
         byte[] out = new byte[10];
 
         if (num < 0) {
             d.write(MINUS_CHAR);
             num = -(num);
             if (num < 0) {
                 /*
                  * still negative ? Then we had Integer.MIN_VALUE
                  */
                 out[--i] = digits[-(Integer.MIN_VALUE % 10)];
                 num = -(Integer.MIN_VALUE / 10);
             }
         }
         do {
             out[--i] = digits[num % 10];
             num /= 10;
         } while (num > 0);
         d.write(out, i, 10 - i);
     }
 
     /**
      * writes the given long integer to the device. Speed optimized; character
      * conversion avoided.
      */
     public static void write(Device d, long num) throws IOException {
         int i = 20;
         byte[] out = new byte[20];
 
         if (num < 0) {
             d.write(MINUS_CHAR);
             num = -(num);
             if (num < 0) {
                 /*
                  * still negative ? Then we had Long.MIN_VALUE
                  */
                 out[--i] = digits[-(int) (Long.MIN_VALUE % 10)];
                 num = -(Long.MIN_VALUE / 10);
             }
         }
         do {
             out[--i] = digits[(int) (num % 10)];
             num /= 10;
         } while (num > 0);
         d.write(out, i, 20 - i);
     }
 
     /**
      * writes the given java.awt.Color to the device. Speed optimized;
      * character conversion avoided.
      */
     public static void write(Device d, Color c) throws IOException {
         d.write(HASH_CHAR);
         int rgb = (c == null) ? 0 : c.getRGB();
         int mask = 0xf00000;
         for (int bitPos = 20; bitPos >= 0; bitPos -= 4) {
             d.write(digits[(rgb & mask) >>> bitPos]);
             mask >>>= 4;
         }
     }
 
     /**
      * writes anything Renderable
      */
     public static void write(Device d, Renderable r) throws IOException {
         if (r == null) return;
         r.write(d);
     }
 
     /*
      * testing purposes.
      */
     public static void main(String argv[]) throws Exception {
         Color c = new Color(255, 254, 7);
         Device d = new org.wings.io.StringBufferDevice();
         write(d, c);
         quote(d, "\nThis is a <abc> string \"; foo & sons\nmoin");
         write(d, -42);
         write(d, Integer.MIN_VALUE);
 
         write(d, "hello test&nbsp;\n");
         write(d, "<html>hallo test&nbsp;\n");
         System.out.println(d.toString());
 
         d = new org.wings.io.NullDevice();
         long start = System.currentTimeMillis();
         for (int i = 0; i < 1000000; ++i) {
             quote(d, "this is a little & foo");
         }
         System.err.println("took: " + (System.currentTimeMillis() - start)
                            + "ms");
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
