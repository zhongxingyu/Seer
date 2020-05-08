 package com.wickedspiral.jacss.parser;
 
 import com.wickedspiral.jacss.lexer.Token;
 import com.wickedspiral.jacss.lexer.TokenListener;
 
 import java.io.BufferedOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import static com.wickedspiral.jacss.lexer.Token.*;
 
 /**
  * @author wasche
  * @since 2011.08.04
  */
 public class Parser implements TokenListener
 {
     private static final String MS_ALPHA = "progid:dximagetransform.microsoft.alpha(opacity=";
     private static final Collection<String> UNITS = new HashSet<>(
             Arrays.asList("px", "em", "pt", "in", "cm", "mm", "pc", "ex", "%"));
     private static final Collection<String> KEYWORDS = new HashSet<>(
             Arrays.asList("normal", "bold", "italic", "serif", "sans-serif", "fixed"));
     private static final Collection<String> BOUNDARY_OPS = new HashSet<>(
             Arrays.asList("{", "}", ">", ";", ":", ",")); // or comment
     private static final Collection<String> DUAL_ZERO_PROPERTIES = new HashSet<>(
             Arrays.asList("background-position", "-webkit-transform-origin", "-moz-transform-origin"));
     private static final Collection<String> NONE_PROPERTIES = new HashSet<>();
 
     static
     {
         NONE_PROPERTIES.add("outline");
         for (String property : new String[] {"border", "margin", "padding"})
         {
             NONE_PROPERTIES.add(property);
             for (String edge : new String[]{"top", "left", "bottom", "right"})
             {
                 NONE_PROPERTIES.add(property + "-" + edge);
             }
         }
     }
 
     // buffers
     private LinkedList<String> ruleBuffer;
     private LinkedList<String> valueBuffer;
     private LinkedList<String> rgbBuffer;
     private String pending;
 
     // flags
     private boolean inRule;
     private boolean space;
     private boolean charset;
     private boolean at;
     private boolean ie5mac;
     private boolean rgb;
     private int checkSpace;
 
     // other state
     private String property;
     private Token lastToken;
     private String lastValue;
 
     private PrintStream out;
 
     private boolean debug;
     private boolean keepTailingSemicolons;
     private boolean noCollapseZeroes;
     private boolean noCollapseNone;
 
     public Parser(OutputStream outputStream, boolean debug, boolean keepTailingSemicolons, boolean noCollapseZeroes,
                   boolean noCollapseNone)
     {
         out = new PrintStream(new BufferedOutputStream(outputStream));
 
         ruleBuffer = new LinkedList<>();
         valueBuffer = new LinkedList<>();
         rgbBuffer = new LinkedList<>();
 
         inRule = false;
         space = false;
         charset = false;
         at = false;
         ie5mac = false;
         rgb = false;
         checkSpace = -1;
 
         this.debug = debug;
         this.keepTailingSemicolons = keepTailingSemicolons;
         this.noCollapseZeroes = noCollapseZeroes;
         this.noCollapseNone = noCollapseNone;
     }
 
     // ++ Output functions
 
     private void output(Collection<String> strings)
     {
         for (String s : strings)
         {
             output(s);
         }
     }
 
     private void output(String str)
     {
         out.print(str);
         out.flush();
     }
 
     private void dump(String str)
     {
         ruleBuffer.add(pending);
         ruleBuffer.add(str);
         output(ruleBuffer);
         ruleBuffer.clear();
     }
 
     private void write(String str)
     {
         if (str == null || str.length() == 0) return;
 
         if (str.startsWith("/*!") && ruleBuffer.isEmpty())
         {
             output(str);
         }
         if ("}".equals(str))
         {
             // check for empty rule
             if (!ruleBuffer.isEmpty() && !"{".equals(ruleBuffer.getLast()))
             {
                 output(ruleBuffer);
                 output(str);
             }
             ruleBuffer.clear();
         }
         else
         {
             ruleBuffer.add(str);
         }
     }
 
     private void buffer(String str)
     {
         if (str == null || str.length() == 0) return;
 
         if (pending == null)
         {
             pending = str;
         }
         else
         {
             write(pending);
             pending = str;
         }
     }
 
     private void queue(String str)
     {
         if (str == null || str.length() == 0) return;
 
         if (property != null)
         {
             valueBuffer.add(str);
         }
         else
         {
             buffer(str);
         }
     }
 
     private void collapseValue()
     {
         StringBuilder sb = new StringBuilder();
         for (String s : valueBuffer)
         {
             sb.append(s);
         }
         String value = sb.toString();
 
         if ("0 0".equals(value) || "0 0 0 0".equals(value) || "0 0 0".equals(value))
         {
             if (DUAL_ZERO_PROPERTIES.contains(value))
             {
                 buffer("0 0");
             }
             else
             {
                 buffer("0");
             }
         }
         else if ("none".equals(value) && (NONE_PROPERTIES.contains(property) || "background".equals(property)) && !noCollapseNone)
         {
             buffer("0");
         }
         else
         {
             buffer(value);
         }
     }
 
     // ++ TokenListener
 
     public void token(Token token, String value)
     {
         if (debug) System.err.printf("Token: %s, value: %s\n", token, value);
 
         if (rgb)
         {
             if (NUMBER == token)
             {
                 String h = Integer.toHexString(Integer.parseInt(value)).toLowerCase();
                 if (h.length() < 2)
                 {
                     rgbBuffer.add("0");
                 }
                 rgbBuffer.add(h);
             }
             else if (LPAREN == token)
             {
                 if (NUMBER == lastToken)
                 {
                     queue(" ");
                 }
                 queue("#");
                 rgbBuffer.clear();
             }
             else if (RPAREN == token)
             {
                 if (rgbBuffer.size() == 3)
                 {
                     String a = rgbBuffer.get(0);
                     String b = rgbBuffer.get(1);
                     String c = rgbBuffer.get(2);
                     if (a.charAt(0) == a.charAt(1) &&
                         b.charAt(0) == b.charAt(1) &&
                         c.charAt(0) == c.charAt(1))
                     {
                         queue(a.substring(0, 1));
                         queue(b.substring(0, 1));
                         queue(c.substring(0, 1));
                         rgb = false;
                         return;
                     }
                 }
                 for (String s : rgbBuffer)
                 {
                     queue(s);
                 }
                 rgb = false;
             }
             return;
         }
 
         if (token == WHITESPACE)
         {
             space = true;
             return; // most of these are unneeded
         }
 
         if (token == COMMENT)
         {
             // comments are only needed in a few places:
             // 1) special comments /*! ... */
             if ('!' == value.charAt(2))
             {
                 queue(value);
                 lastToken = token;
                 lastValue = value;
             }
             // 2) IE5/Mac hack
             else if ('\\' == value.charAt(value.length()-3))
             {
                 queue("/*\\*/");
                 lastToken = token;
                 lastValue = value;
                 ie5mac = true;
             }
             else if (ie5mac)
             {
                 queue("/**/");
                 lastToken = token;
                 lastValue = value;
                 ie5mac = false;
             }
             // 3) After a child selector
             else if (GT == lastToken)
             {
                 queue("/**/");
                 lastToken = token;
                 lastValue = value;
             }
             return;
         }
 
         // make sure we have space between values for multi-value properties
         // margin: 5px 5px
         if (
                 (
                         NUMBER == lastToken &&
                         (HASH == token || NUMBER == token)
                 ) ||
                 (
                         (IDENTIFIER == lastToken || PERCENT == lastToken || RPAREN == lastToken) &&
                         (NUMBER == token || IDENTIFIER == token || HASH == token)
                 )
         )
         {
             queue(" ");
             space = false;
         }
 
         // rgb()
         if (IDENTIFIER == token && "rgb".equals(value))
         {
             rgb = true;
             space = false;
             return;
         }
 
         if (AT == token)
         {
             queue(value);
             at = true;
         }
         else if (inRule && COLON == token && property == null)
         {
             queue(value);
             property = lastValue.toLowerCase();
             valueBuffer.clear();
         }
         // first-letter and first-line must be followed by a space
         else if (!inRule && COLON == lastToken && ("first-letter".equals(value) || "first-line".equals(value)))
         {
             queue(value);
             queue(" ");
         }
         else if (SEMICOLON == token)
         {
             if (at)
             {
                 at = false;
                 if ("charset".equals(ruleBuffer.get(1)))
                 {
                     if (charset)
                     {
                         ruleBuffer.clear();
                         pending = null;
                     }
                     else
                     {
                         charset = true;
                         dump(value);
                     }
                 }
                 else
                 {
                     dump(value);
                 }
             }
             else if (SEMICOLON == lastToken)
             {
                 return; // skip duplicate semicolons
             }
             else
             {
                 collapseValue();
                 valueBuffer.clear();
                 property = null;
                 queue(value);
             }
         }
         else if (LBRACE == token)
         {
             if (checkSpace != -1)
             {
                 // start of a rule, the space was correct
                 checkSpace = -1;
             }
             if (at)
             {
                 at = false;
                 dump(value);
                 pending = null;
             }
             else
             {
                 inRule = true;
                 queue(value);
             }
         }
         else if (RBRACE == token)
         {
             if (checkSpace != -1)
             {
                 // didn't start a rule, space was wrong
                 ruleBuffer.remove(checkSpace);
                 checkSpace = -1;
             }
             if (!valueBuffer.isEmpty())
             {
                 collapseValue();
             }
             if (";".equals(pending))
             {
                 if (keepTailingSemicolons)
                 {
                     buffer(";");
                 }
                 pending = value;
             }
             else
             {
                 buffer(value);
             }
             property = null;
             inRule = false;
         }
         else if (!inRule)
         {
             if (!space || GT == token || lastToken == null || BOUNDARY_OPS.contains(value))
             {
                 queue(value);
             }
             else
             {
                 if (COLON == token)
                 {
                     checkSpace = ruleBuffer.size() + 1; // include pending value
                 }
                 if ( RBRACE != lastToken )
                 {
                     queue(" ");
                 }
                 queue(value);
                 space = false;
             }
         }
         else if (NUMBER == token && value.startsWith("0."))
         {
             if (noCollapseZeroes)
             {
                 queue( value );
             }
             else
             {
                 queue(value.substring(1));
             }
         }
         else if (STRING == token && "-ms-filter".equals(property))
         {
             if (MS_ALPHA.equals(value.substring(1, MS_ALPHA.length() + 1).toLowerCase()))
             {
                 String c = value.substring(0, 1);
                 String o = value.substring(MS_ALPHA.length()+1, value.length()-2);
                 queue(c);
                 queue("alpha(opacity=");
                 queue(o);
                 queue(")");
                 queue(c);
             }
             else
             {
                 queue(value);
             }
         }
         else if (EQUALS == token)
         {
             queue(value);
             StringBuilder sb = new StringBuilder();
             for (String s : valueBuffer)
             {
                 sb.append(s);
             }
             if (MS_ALPHA.equals(sb.toString().toLowerCase()))
             {
                 buffer("alpha(opacity=");
                 valueBuffer.clear();
             }
         }
         else
         {
             String v = value.toLowerCase();
             // values of 0 don't need a unit
             if (NUMBER == lastToken && "0".equals(lastValue) && (PERCENT == token || IDENTIFIER == token))
             {
                 if (!UNITS.contains(value))
                 {
                     queue(" ");
                     queue(value);
                 }
             }
             // use 0 instead of none
             else if (COLON == lastToken && "none".equals(value) && NONE_PROPERTIES.contains(property) && !noCollapseNone)
             {
                 queue("0");
             }
             // force properties to lower case for better gzip compression
             else if (COLON != lastToken && IDENTIFIER == token)
             {
                 // #aabbcc
                 if (HASH == lastToken)
                 {
                     if (value.length() == 6 &&
                             v.charAt(0) == v.charAt(1) &&
                             v.charAt(2) == v.charAt(3) &&
                             v.charAt(4) == v.charAt(5))
                     {
                         queue(v.substring(0, 1));
                        queue(v.substring(2, 3));
                        queue(v.substring(4, 5));
                     }
                     else
                     {
                         queue(v);
                     }
                 }
                 else
                 {
                     if (property == null || KEYWORDS.contains(v))
                     {
                         queue(v);
                     }
                     else
                     {
                         queue(value);
                     }
                 }
             }
             // nothing special, just send it along
             else
             {
                 if (KEYWORDS.contains(v))
                 {
                     queue(v);
                 }
                 else
                 {
                     queue(value);
                 }
             }
         }
 
         lastToken = token;
         lastValue = value;
         space = false;
     }
 
     public void end()
     {
         write(pending);
         if (!ruleBuffer.isEmpty())
         {
             output(ruleBuffer);
         }
     }
 }
