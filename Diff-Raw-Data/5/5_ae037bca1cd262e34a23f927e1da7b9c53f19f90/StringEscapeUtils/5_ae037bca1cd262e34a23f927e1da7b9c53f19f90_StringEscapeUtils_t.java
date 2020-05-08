 package net.cscott.sdr.util;
 
 /** A fixed cut-down version of
  * {@link org.apache.commons.lang.StringEscapeUtils}.
  * @author C. Scott Ananian
  */
 public abstract class StringEscapeUtils {
     /** Return the parameter as a properly-escaped Java string literal. */
     public static String escapeJava(String s) {
         StringBuilder sb = new StringBuilder();
             //sb.append('"');
             for (int i=0; i<s.length(); i++) {
                 char c = s.charAt(i);
                 if (c < 128 && Character.isJavaIdentifierPart(c))
                     sb.append(c); // ASCII and alphanumeric-ish
                 else if (c==' ' || c=='/' || c=='-' || c=='+')
                     sb.append(c); // some specific safe characters
                 else if (c<256) // this handles quotes, slashes, and other nasties
                    sb.append(String.format("\\%03o", (int) c));
                 else // make the world safe for unicode
                    sb.append(String.format("\\"+"u%04x", (int) c));
             }
             //sb.append('"');
             return sb.toString();
     }
 }
