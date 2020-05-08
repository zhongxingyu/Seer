 package veeju.parser;
 
 import java.util.regex.*;
 import java.util.LinkedList;
 import java.io.*;
 import veeju.forms.*;
 
 /**
  * This class, not able to be instantiated, provides a handwritten parser for
  * Veeyu programming language. Its all methods are static methods.
  *
  * Its methods are all public, but it's not all of them are <em>published</em>.
  * The easiest way to parse a code string and get its result AST is
  * {@link #parse(CharSequence)} method.
  *
  * <pre>{@code
  * try {
  *     final Program program = Parser.parse("a.simple(:Veeyu, program)");
  * } catch (InvalidSyntaxException e) {
  *     System.err.println(e.getLineString());
  *     for (int i = 0; i < e.getColumn(); ++i) {
  *         System.err.print(' ');
  *     }
  *     System.err.println('^');
  * }
  * }</pre>
  */
 public final class Parser {
     /**
      * The list of whitespace characters.
      */
     public static final String WHITESPACES = " \t\r\n";
 
     /**
      * The regular expression pattern that matches to shebang line.
      */
     public static final Pattern SHEBANG_LINE_PATTERN = Pattern.compile(
         "^#![^\\n]*(?:\\n|$)"
     );
 
     /**
      * The regular expression pattern that matches to encoding hint/option
      * in the shebang line. This pattern is used for encoding detection
      * heuristic of {@link #parse(byte[])}.
      */
     public static final Pattern ENCODING_HINT_PATTERN = Pattern.compile(
         "(?:-e\\s*|--encoding(?:=|\\s*))(\\S+)"
     );
 
     protected Parser() {
         // cannot be instantiated.
     }
 
     /**
      * Parses an encoded Veeyu program from {@code stream} with the tirival
      * heuristic that detects an encoding from the shebang line, then returns
      * the parsed {@link Program} instance.
      *
      *
      * If the program doesn't have its shebang line, or the shebang line
      * doesn't have any encoding hint, it treats the program as UTF-8 encoded
      * by default.
      *
      * @param stream a stream that contains an encoded Veeyu program.
      * @return the parsed program.
      * @throws IOException when the {@code stream} causes an error.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static Program parse(final InputStream stream) throws IOException {
         final byte[] buffer = new byte[1024];
         final ByteArrayOutputStream bout = new ByteArrayOutputStream();
         while (true) {
             final int len = stream.read(buffer);
             if (len < 0) break;
             bout.write(buffer, 0, len);
         }
         return parse(bout.toByteArray());
     }
 
     /**
      * Parses {@code bytes}, encoded Veeyu program, with the trivial heuristic
      * that detects an encoding from the shebang line, then returns the parsed
      * {@link Program} instance.
      *
      * If the program doesn't have its shebang line, or the shebang line
      * doesn't have any encoding hint, it treats the program as UTF-8 encoded
      * by default.
      *
      * @param bytes encoded Veeyu program.
      * @return the parsed program.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static Program parse(final byte[] bytes) {
         String encoding = "utf-8";
         int i;
         for (i = 0; i < bytes.length && bytes[i] != (byte) 0xa; ++i);
         String firstLine;
         try {
             firstLine = new String(bytes, 0, i, "utf-8");
         } catch (UnsupportedEncodingException e) {
             throw new Error("this Java platform doesn't support " +
                             "UTF-8 encoding");
         }
         Matcher matcher = SHEBANG_LINE_PATTERN.matcher(firstLine);
         if (matcher.find(0) && matcher.start() == 0) {
             matcher = ENCODING_HINT_PATTERN.matcher(firstLine);
             matcher.find();
             try {
                 encoding = matcher.group(1);
             } catch (IllegalStateException e) {
                 // does nothing; follows the default.
             }
         }
         try {
             return parse(bytes, encoding);
         } catch (UnsupportedEncodingException e) {
             try {
                 return parse(bytes, "utf-8");
             } catch (UnsupportedEncodingException e2) {
                 throw new Error("this Java platform doesn't support " +
                                 "UTF-8 encoding");
             }
         }
     }
 
     /**
      * Parses {@code bytes}, encoded Veeyu program, with the specific encoding
      * option ({@code charsetName}) and returns the parsed {@link Program}
      * instance.
      *
      * @param bytes encoded Veeyu program.
      * @param charsetName a name of a supported encoding e.g. {@code "utf-8"}.
      * @return the parsed program.
      * @throws UnsupportedEncodingException if the named encoding is not
      *                                      supported.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static Program parse(final byte[] bytes,
                                 final String charsetName)
            throws UnsupportedEncodingException{
         return parse(new String(bytes, charsetName));
     }
 
     /**
      * Parses a Veeyu program and returns the parsed {@link Program} instance.
      *
      * @param text a program text to parse.
      * @return the parsed program.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static Program parse(CharSequence text) {
         final Matcher matcher = SHEBANG_LINE_PATTERN.matcher(text);
         if (matcher.find(0) && matcher.start() == 0) {
             final int offset = matcher.end();
             text = text.subSequence(offset, text.length());
         }
         final ParsingResult result = parseProgram(text);
         final int offset = result.getOffset();
         if (offset == text.length()) return (Program) result.getNode();
         throw new InvalidSyntaxException(text, offset, "unexpected character");
     }
 
     /**
      * Parses a program text.
      *
      * @param text a program text to parse.
      * @param offset an offset of the program text to start parsing.
      * @param terminates an exit predicate.
      * @return the parsed {@link Program} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseProgram(CharSequence text, int offset,
                                              TerminatingPredicate terminates) {
         final TerminatingPredicate formSepPrep = new TerminatingPredicate() {
             public boolean terminates(CharSequence text, int offset) {
                 if (offset < text.length()) {
                     final char c = text.charAt(offset);
                     return c == ';' || c == '\n';
                 }
                 throw new InvalidSyntaxException(text, offset,
                                                  "expected a semicolon ';' " +
                                                  "or new line");
             }
         };
         final TerminatingPredicate formEnds = new ChainPredicate(formSepPrep,
                                                                  terminates);
         final int len = text.length();
         char c;
         while (!terminates.terminates(text, offset)) {
             if (offset >= len) break;
             c = text.charAt(offset);
             if (WHITESPACES.indexOf(c) < 0 && c != ';' && c != '\n') break;
             ++offset;
         }
         final LinkedList<Form> forms = new LinkedList<Form>();
         while (!terminates.terminates(text, offset)) {
             c = text.charAt(offset);
             if (WHITESPACES.indexOf(c) >= 0) {
                 ++offset;
                 continue;
             }
             final ParsingResult result = parseForm(text, offset, formEnds);
             offset = result.getOffset();
             while (!terminates.terminates(text, offset)) {
                 boolean sep;
                 try {
                     sep = formSepPrep.terminates(text, offset);
                 } catch (InvalidSyntaxException e) {
                     break;
                 }
                 if (!sep) break;
                 ++offset;
             }
             forms.add((Form) result.getNode());
         }
         return new ParsingResult(new Program(forms), offset);
     }
 
     /**
      * Parses a program text.
      *
      * @param text a program text to parse.
      * @return the parsed {@link Program} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseProgram(final CharSequence text) {
         return parseProgram(text, 0, new EosPredicate());
     }
 
     /**
      * Parses a form text.
      *
      * @param text a form text to parse.
      * @param offset an offset of the form text to start parsing.
      * @param terminates an exit predicate.
      * @return the parsed {@link Form} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseForm(CharSequence text, int offset,
                                           TerminatingPredicate terminates) {
         ParsingResult result;
         Form form;
         final int len = text.length();
         char c;
         boolean space;
         Matcher matcher;
         result = parseAtomicForm(text, offset, terminates);
         form = (Form) result.getNode();
         offset = result.getOffset();
         space = WHITESPACES.indexOf(text.charAt(offset - 1)) >= 0;
        while (!terminates.terminates(text, offset)) {
             c = text.charAt(offset);
             if (WHITESPACES.indexOf(c) >= 0) {
                 ++offset;
                 space = true;
                 continue;
             } else if (c == '.') {
                 while (true) {
                     ++offset;
                     if (offset >= len) {
                         throw new InvalidSyntaxException(
                             text, offset,
                             "expected attribute name"
                         );
                     }
                     c = text.charAt(offset);
                     if (WHITESPACES.indexOf(c) < 0) break;
                 }
                 matcher = Symbol.PATTERN.matcher(text);
                 if (matcher.find(offset) && matcher.start() == offset) {
                     form = new Attribute(form, Symbol.create(matcher.group()));
                     offset = matcher.end();
                 } else if (c == '{') {
                     result = parseProgram(
                         text, offset + 1,
                         CharacterPredicate.curlyBracketsPredicate
                     );
                     final Program prog = (Program) result.getNode();
                     offset = result.getOffset();
                     if (text.charAt(offset) != '}') {
                         throw new InvalidSyntaxException(
                             text, offset,
                             "unexpected character '" +
                             String.valueOf(text.charAt(offset)) + "'"
                         );
                     }
                     form = new Attribute(form, new Block(prog));
                     ++offset;
                 } else if (c == '[') {
                     result = parseArguments(
                         text, offset + 1, true,
                         CharacterPredicate.squareBracketsPredicate
                     );
                     final ArgumentList args = (ArgumentList) result.getNode();
                     offset = result.getOffset();
                     if (text.charAt(offset) != ']') {
                         throw new InvalidSyntaxException(
                             text, offset,
                             "unexpected character '" +
                             String.valueOf(text.charAt(offset)) + "'"
                         );
                     }
                     form = new Attribute(form, new Index(args));
                     ++offset;
                 } else {
                     throw new InvalidSyntaxException(text, offset,
                                                      "invalid attribute");
                 }
             } else if (c == '(' || c == '[' || c == '{') {
                 // 1. '(' / 2. '[' / 3. '{'
                 final int state = c == '(' ? 1 : (c == '[' ? 2 : 3);
                 while (true) {
                     ++offset;
                     if (offset >= len) {
                         throw new InvalidSyntaxException(
                             text, offset,
                             state == 3 ? "expected program"
                                        : "expected argument list"
                         );
                     }
                     c = text.charAt(offset);
                     if (WHITESPACES.indexOf(c) < 0) break;
                 }
                 if (state == 3) {
                     result = parseProgram(
                         text, offset,
                         CharacterPredicate.curlyBracketsPredicate
                     );
                 } else {
                     result = parseArguments(
                         text, offset, true,
                         state == 1 ? CharacterPredicate.parenthesesPredicate
                                    : CharacterPredicate.squareBracketsPredicate
                     );
                 }
                 offset = result.getOffset();
                 c = text.charAt(offset);
                 if (c != (state == 1 ? ')' : (state == 2 ? ']' : '}'))) {
                     throw new InvalidSyntaxException(
                         text, offset,
                         "unexpected character '" + String.valueOf(c) + "'"
                     );
                 }
                 if (state == 1) {
                     form = new Call(form, (ArgumentList) result.getNode());
                 } else {
                     AttributeName a = state == 2
                                     ? new Index((ArgumentList) result.getNode())
                                     : new Block((Program) result.getNode());
                     form = new Attribute(form, a);
                 }
                 ++offset;
             } else {
                 boolean def = c == '=';
                 def = def && (
                     offset + 1 == len ||
                     WHITESPACES.indexOf(text.charAt(offset + 1)) >= 0
                 );
                 boolean def2 = c == ':' && text.charAt(offset + 1) == '=';
                 if (def || def2) {
                     result = parseForm(text, offset + (def ? 1 : 2),
                                        terminates);
                     offset = result.getOffset();
                     form = new Definition(form, (Form) result.getNode(),
                                           def2);
                 } else if (c == ':') {
                     result = parseArguments(text, offset + 1, false,
                                             terminates);
                     offset = result.getOffset();
                     form = new Call(form, (ArgumentList) result.getNode());
                 } else if (space) {
                     matcher = Symbol.PATTERN.matcher(text);
                     if (!matcher.find(offset) || matcher.start() != offset) {
                         throw new InvalidSyntaxException(
                             text, offset,
                             "unexpected character '" + String.valueOf(c) + "'"
                         );
                     }
                     offset = matcher.end();
                     if (len <= offset ||
                         WHITESPACES.indexOf(text.charAt(offset)) < 0) {
                         throw new InvalidSyntaxException(text, offset,
                                                          "expected whitespace");
                     }
                     while (len > offset &&
                            WHITESPACES.indexOf(text.charAt(offset)) >= 0) {
                         ++offset;
                     }
                     result = parseForm(text, offset, terminates);
                     offset = result.getOffset();
                     form = new Attribute(form, Symbol.create(matcher.group()));
                     final ArgumentList args = new ArgumentList(
                         (Form) result.getNode()
                     );
                     return new ParsingResult(new Call(form, args),
                                              offset);
                 } else {
                     throw new InvalidSyntaxException(
                         text, offset,
                         "unexpected character '" + String.valueOf(c) + "'" 
                     );
                 }
             }
         }
         return new ParsingResult(form, offset);
     }
 
     /**
      * Parses a form text.
      *
      * @param text a form text to parse.
      * @return the parsed {@link Form} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseForm(CharSequence text) {
         return parseForm(text, 0, new EosPredicate());
     }
 
     /**
      * Parses an atomic form text.
      *
      * @param text a form text to parse.
      * @param offset an offset of the form text to start parsing.
      * @param ends an exit predicate.
      * @return the parsed {@link Form} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseAtomicForm(CharSequence text, int offset,
                                                 TerminatingPredicate ends) {
         if (offset < 0) throw new IllegalArgumentException("negative offset");
         if (text.length() < 1) {
             throw new InvalidSyntaxException(text, 0, "nothing to parse");
         }
         final int length = text.length();
         char c = text.charAt(0);
         ParsingResult result;
         Form form;
         Matcher matcher;
         while (true) {
             if (offset >= length) break;
             c = text.charAt(offset);
             if (WHITESPACES.indexOf(c) < 0) break;
             ++offset;
         }
         if (offset >= length) {
             throw new InvalidSyntaxException(text, offset,
                                              "required any character");
         }
         if (c == '(') {
             result = parseForm(text, offset + 1,
                                CharacterPredicate.parenthesesPredicate);
             form = (Form) result.getNode();
            offset = result.getOffset() + 1;
         } else if (c == ':' &&
                    offset + 1 < length && text.charAt(offset + 1) == '(') {
             result = parseForm(text, offset + 2,
                                CharacterPredicate.parenthesesPredicate);
             form = new Quote((Form) result.getNode());
             offset = result.getOffset() + 1;
         } else if (c == ':') {
             matcher = Symbol.PATTERN.matcher(text);
             if (matcher.find(offset + 1) && matcher.start() == offset + 1) {
                 form = new Quote(Symbol.create(matcher.group()));
                 offset = matcher.end();
             } else {
                 throw new InvalidSyntaxException(text, offset + 1,
                                                  "invalid symbol quote");
             }
         } else if (c == '{') {
             result = parseProgram(text, offset + 1,
                                   CharacterPredicate.curlyBracketsPredicate);
             offset = result.getOffset();
             if (text.charAt(offset) != '}') {
                 throw new InvalidSyntaxException(
                     text, offset,
                     "unexpected character '" +
                     String.valueOf(text.charAt(offset)) + '"'
                 );
             }
             form = new Block((Program) result.getNode());
             ++offset;
         } else if (c == '[') {
             result = parseArguments(text, offset + 1, true,
                                     CharacterPredicate.squareBracketsPredicate);
             offset = result.getOffset();
             if (text.charAt(offset) != ']') {
                 throw new InvalidSyntaxException(
                     text, offset,
                     "unexpected character '" +
                     String.valueOf(text.charAt(offset)) + '"'
                 );
             }
             form = new Index((ArgumentList) result.getNode());
             ++offset;
         } else {
             matcher = NumberLiteral.PATTERN.matcher(text);
             if (matcher.find(offset) && matcher.start() == offset) {
                 form = new NumberLiteral(matcher.group());
                 offset = matcher.end();
             } else {
                 matcher = StringLiteral.PATTERN.matcher(text);
                 if (matcher.find(offset) && matcher.start() == offset) {
                     final String sPrefix = matcher.group(1);
                     String sBody = null;
                     for (int g = 2; g <= 5; ++g) {
                         sBody = matcher.group(g);
                         if (sBody == null) continue;
                         if (g % 2 == 0) { // when it is double quote
                             sBody = unescapeStringLiteral(sBody);
                         }
                         if (sBody.length() > 0) break;
                     }
                     if (sPrefix != null && sPrefix.length() > 0) {
                         form = new StringLiteral(sBody, sPrefix.charAt(0));
                     } else {
                         form = new StringLiteral(sBody);
                     }
                     offset = matcher.end();
                 } else {
                     matcher = Symbol.PATTERN.matcher(text);
                     if (matcher.find(offset) && matcher.start() == offset) {
                         form = Symbol.create(matcher.group());
                         offset = matcher.end();
                     } else {
                         final String msg = "unexpected character '"
                                          + String.valueOf(text.charAt(offset))
                                          + "'";
                         throw new InvalidSyntaxException(text, offset, msg);
                     }
                 }
             }
         }
        while (!ends.terminates(text, offset) &&
               WHITESPACES.indexOf(text.charAt(offset)) >= 0) {
            ++offset;
        }
         return new ParsingResult(form, offset);
     }
 
     /**
      * Parses an atomic form text.
      *
      * @param text a form text to parse.
      * @return the parsed {@link Form} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseAtomicForm(final CharSequence text) {
         return parseAtomicForm(text, 0, new EosPredicate());
     }
 
     /**
      * Parses an argument list text.
      *
      * @param text an argument list text to parse.
      * @param offset an offset of the text to start parsing.
      * @param ends an exit predicate.
      * @return the parsed {@link ArgumentList} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseArguments(CharSequence text, int offset,
                                                final boolean named,
                                                TerminatingPredicate ends) {
         final LinkedList<ArgumentList.Entry> args =
             new LinkedList<ArgumentList.Entry>();
         final TerminatingPredicate formEnds =
             new ChainPredicate(ends, CharacterPredicate.commaPredicate);
         final int len = text.length();
         while (!ends.terminates(text, offset)) {
             if (len > offset && WHITESPACES.indexOf(text.charAt(offset)) >= 0) {
                 ++offset;
                 continue;
             }
             final Matcher matcher = Symbol.PATTERN.matcher(text);
             Symbol name = null;
             if (matcher.find(offset) && matcher.start() == offset) {
                 int colonOffset = matcher.end();
                 while (colonOffset < len) {
                     final char c = text.charAt(colonOffset);
                     if (WHITESPACES.indexOf(c) >= 0) {
                         ++colonOffset;
                         continue;
                     } else if (named && c == ':') {
                         name = Symbol.create(matcher.group());
                         offset = colonOffset + 1;
                     }
                     break;
                 }
             }
             final ParsingResult result = parseForm(text, offset, formEnds);
             offset = result.getOffset();
             args.add(new ArgumentList.Entry(name, (Form) result.getNode()));
             while (!ends.terminates(text, offset) &&
                    WHITESPACES.indexOf(text.charAt(offset)) >= 0) {
                 ++offset;
             }
             if (len > offset && text.charAt(offset) == ',') {
                 ++offset;
             }
         }
         return new ParsingResult(new ArgumentList(args), offset);
     }
 
     /**
      * Parses an argument list text.
      *
      * @param text an argument list text to parse.
      * @return the parsed {@link ArgumentList} and the offset the parser stops.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static ParsingResult parseArguments(final CharSequence text,
                                                final boolean named) {
         return parseArguments(text, 0, named, new EosPredicate());
     }
 
     /**
      * Unescapes metacharacters for the string literal and returns its result.
      *
      * @param stringBody an escaped string body.
      * @return the unescaped string body.
      * @throws InvalidSyntaxException when it has an invalid syntax.
      */
     public static String unescapeStringLiteral(final String stringBody) {
         final StringBuffer result = new StringBuffer();
         final Matcher match = StringLiteral.ESCAPE_PATTERN.matcher(stringBody);
         String group, rep = null;
         while (match.find()) {
             for (int g = 1; g <= 3; ++g) {
                 group = match.group(g);
                 if (group == null || group.length() < 1) continue;
                 rep = String.valueOf((char) Integer.parseInt(group.substring(1),
                                                              16));
                 break;
             }
             if (rep == null) {
                 group = match.group(4); // rest
                 if (group != null && group.length() > 0) {
                     final char g = group.charAt(0);
                     final char[] metachars = StringLiteral.ESCAPE_METACHARS;
                     rep = group;
                     for (int i = 0; i < metachars.length; ++i) {
                         if (metachars[i] != g) continue;
                         rep = String.valueOf(StringLiteral.ESCAPE_MEANINGS[i]);
                         break;
                     }
                 }
             }
             if (rep != null) {
                 match.appendReplacement(result, rep);
                 rep = null;
             }
         }
         match.appendTail(result);
         return result.toString();
     }
 }
 
