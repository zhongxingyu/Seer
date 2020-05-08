 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.engine;
 
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 /**
   * Tokenizes WebMacro template files. It is an wrapper around a 
   * StreamTokenizer that's configured to tokenize the kinds of symbols 
   * that appear in WebMacro template files.
   * <p>
   * It also provides several utility functions that make it
   * convenient to parse stuch files.
   * <p>
   * All characters are word characters except: {}();.=#&@$ space and newline
   * which are "ordinary"--meaning they can be returned as token types.
   * <p>
   * The parseTool also has the property that it returns unique strings 
   * only: If two strings returned by the parseTool are .equals equivalent,
   * then they are also == equivalent.
   * <p>
   * Furthermore, unlike StreamTokenizer, the pushback method for this 
   * ParseTool works for strings as well as regular tokens.
   */
 final class ParseTool
 {
 
    // INSTANCE DATA
 
    Object[] asArg  = { this };
 
    /**
      * This is the underlying stream parseTool we use
      */
    private LineNumberReader _in;
 
    /**
      * Our name, representing the source of the stream
      */
    private String _name;
 
    private int _cur;                  // current character
    private int _last;                 // last returned character
    private int _len;                  // length of our input buffer
    private int _pos;                  // position of next char in input buffer
    private String _buf;               // our buffer of upcoming chars
    private boolean _escaped = false;  // last char was escape char
 
    /**
      * Maximum number of positions in the stream that can be marked (4).
      */
    final public int MAX_MARKS = 4;
 
    /**
      * A mark number that is invalid. If you pass this value to 
      * clearMark() or rewind() they do nothing and return. If you
      * pass it to moveMark you will get a new, valid mark back.
      */
    final public int DUMMY_MARK = -1;
 
    private Mark[] _marks = new Mark[ MAX_MARKS ];
    private int _marked = 0;
 
    // CLASS DATA
 
    /**
      * We need to know this to help us out with stupid platforms
      * that decided to use non-standard line separators. I'm not in
      * general hostile towards mac/nt, except in cases like this 
      * where they cause me to do unnecessary work.
      */
    static String lineSeparator;
    static {
       String tmpLineSep;
       try {
          tmpLineSep = System.getProperty("line.separator").intern();
       } catch (Exception e) {
          tmpLineSep = "\n".intern(); // we make this portable
       }
       lineSeparator = tmpLineSep;
    }
 
    /**
      * Constant used to indicate end of file
      */
    static final int EOF = -1;
 
 
    // CONSTRUCTOR, INIT & META METHODS
 
    /**
      * Create a parseTool that reads from the supplied input stream
      */
    ParseTool(String name, InputStream in) {
       this(name, new InputStreamReader(in));
    }
 
    /**
      * Create a parseTool that reads from the supplied stream.
      * The supplied name is used in error messages, to identify
      * the source of the input.
      */
    ParseTool(String name, Reader inputStream) 
    {
       _escaped = false;
       _name = name;
       LineNumberReader in = new LineNumberReader(inputStream);
       _cur = _last = _len = _pos = EOF;
       _buf = ""; // must be non-null or we will abort before we start
       _in = in;
       for (int i = 0; i < MAX_MARKS; i++) {
          _marks[i] = new Mark();
       }
       read(); // get it started (read the extra newline)
    }
 
 
    /**
      * Return the name of the stream we read from
      */
    final String getName() {
       return _name;
    }
 
    /**
      * A string suitable for use in error messages consisting of the
      * name of the stream (supplied in the ctor) plus the current
      * line number.
      */
    final public String toString()
    {
       return _name + ":" + getLineNumber();
    }
 
 
    /**
      * Return what line number of our stream we have read up to so far.
      */
    final public int getLineNumber()
    {
       return _in.getLineNumber();
    }
 
 
    /**
      * Is the supplied character a valid namestart char? Meaning the 
      * same as a Java identifier start, excluding '$'
      */
    static public final boolean isNameStartChar(int c) {
       return ((c != '$') && (Character.isJavaIdentifierStart((char) c)));
    }
 
    /**
      * Is the current character a valid namestart char? Meaning the 
      * same as a Java identifier start, excluding '$'
      */
    public final boolean isNameStartChar() {
       return isNameStartChar(_cur);
    }
 
    /**
      * Is the supplied character a valid name char? Meaning the 
      * same as a Java identifier name char, excluding '$', and 
      * including the characters '-', '*', and '+' as well.
      */
    static public final boolean isNameChar(int c) {
       return ((c != '$') && 
             ((c == '-') || (c == '*') || (c == '+')
              || Character.isJavaIdentifierPart((char) c))
          );
    }
 
    /**
      * Is the current character a valid name char? Meaning the 
      * same as a Java identifier name char, excluding '$'
      */
    public final boolean isNameChar() {
       return isNameChar(_cur);
    }
 
    /**
      * Is the specified character a valid number start character?
      * Meaning 0-9 or + or -.
      */
    static public final boolean isNumberStartChar(int c) {
       switch (c) {
          case '+': case '-':
          case '0': case '1': case '2': case '3': case '4': 
          case '5': case '6': case '7': case '8': case '9':
             return true;
       }
       return false;
    }
 
    /**
      * Is the current character a valid number start character?
      * Meaning 0-9 or + or -.
      */
    public final boolean isNumberStartChar() {
       return isNumberStartChar(_cur);
    }
  
    /**
      * Is the specified character a valid number start character?
      * Meaning 0-9 or + or -.
      */
    public static final boolean isNumberChar(int c) {
       switch (c) {
          case '0': case '1': case '2': case '3': case '4': 
          case '5': case '6': case '7': case '8': case '9':
             return true;
       }
       return false;
    }
 
    /**
      * Is the specified character a valid number start character?
      * Meaning 0-9 or + or -.
      */
    public final boolean isNumberChar() {
       return isNumberChar(_cur);
    }
  
    /**
      * Is the supplied character whitespace? (tab, space, newline)
      */
    static public final boolean isWhitespace(int c) {
       return ((c == ' ') || (c == '\t') || (c == '\n'));
    }
 
    /**
      * Is the current character whitespace? (tab, space, newline)
      */
    public final boolean isWhitespace() {
       return isWhitespace(_cur);
    }
 
    /**
      * Is the supplied character space? (tab, space, but not newline)
      */
    static public final boolean isSpace(int c) {
       return ((c == ' ') || (c == '\t'));
    }
 
    /**
      * Is the current character space? (tab, space, but not newline)
      */
    public final boolean isSpace() {
       return isSpace(_cur);
    }
 
 
    // INTERNAL: STREAM READING
 
 
    private void read() {
       read(false);
    }
 
    /**
      * Advance to the next character on the stream. The escaped boolean
      * is used to recurse exactly once to read an escaped character.
      */
    private void read(boolean escaped) {
       _last = _cur;
       _escaped = false;
       if (_pos < _len) {
          _cur = _buf.charAt(_pos++); 
       } else {
          _cur = (_buf != null) ? '\n' : EOF;
          try {
             _buf = _in.readLine();
             _buf.length(); // XXX: sometimes see "null" (literal) without this?
             if (_marked != 0) {
                // marks need the new data too
                for (int i = 0; i < MAX_MARKS; i++) {
                   if (_marks[i] != null) {
                      if (_cur != EOF) {
                         _marks[i].buf.append((char) _cur);
                      }
                      _marks[i].buf.append(_buf);
                   }
                }
             }
          } catch (Exception e) {
             _buf = null;
          }
          _pos = 0;
          _len = (_buf != null) ? _buf.length() : EOF;
       }
 
       if (!escaped && (_cur == '\\')) {
          read(true);
          _escaped = true; 
       }
 
    }
 
 
    /**
      * Return true if the current character on the stream is 
      * the EOF character, meaning we have reached end of file. 
      */
    boolean isAtEOF() {
       return (_buf == null);
    }
 
    /**
      * Return true if the current character is escaped
      */
    boolean isEscaped() {
       return _escaped;
    }
 
 
    // PARSING ROUTINES
   
    /**
      * Advance to the next character in the stream. Return the new 
      * current character.
      */
    public final int nextChar() {
       read();
       return _cur;
    }
 
    /**
      * Return the current character, or EOF if the end of stream 
      * has been reached. Does not advance the stream.
      */
    public final int getChar() {
       return _cur;
    }
 
    /**
      * What was the last character read, before this one? This lookBack
      * survives all of the other commands. If the previous character 
      * is unknown then -1 will be returned (eg: if no previous char).
      */
    public final int lookBack() {
       return _last;
    }
 
 
    /**
      * Mark the current position for a subsequent rewind. Subsequent
      * data read will be built up in a buffer associated with the mark,
      * adding a slight cost to reads. You can only mark up to MAX_MARKS 
      * positions in the stream.
      * @excpetion ParseException tried to mark more than MAX_MARKS positions
      */
    final public int mark() 
       throws ParseException
    {
       for (int i = 0; i < MAX_MARKS; i++) {
          if (_marks[i].isSet == false) {
             _marks[i].set(_last, _cur,_escaped, _pos, _buf);
             _marked++;
             return i;
          }
       }
       throw new ParseException(this,"Tried to mark more than MAX_MARKS positions");
    }
 
    /**
      * Rewind to the marked position, and free the extra memory used
      * by the mark. Returns the DUMMY_MARK value which can safely 
      * be used to rewind() and clear() with no effect.
      */
    final public int rewind(int i) {
 
       if ((i < 0) || (i >= MAX_MARKS)) {
          return DUMMY_MARK; 
       }
       Mark m = _marks[i];
       if (m.isSet == false) {
          return DUMMY_MARK;
       }
 
       _buf = m.buf.toString();
       _pos = 0;
       _len = _buf.length();
       _last = -1;
       _cur = m.last;
       _escaped = false;
       _marks[i].isSet = false;
       _marked--;
       read(); // recompute escaped, _pos, _cur, _last, etc.
 
       return DUMMY_MARK;
    }
 
 
 
    /**
      * Is there a mark on the stream?
      */
    public final boolean isMarked() {
       return (_marked != 0);
    }
 
    /**
      * Clear a mark, freeing memory associated with it. Returns the
      * DUMMY_MARK value, which can safely be used to rewind() and
      * clear() with no effect.
      */
    final public int clearMark(int i) {
       if ((i < 0) || (i >= MAX_MARKS)) {
          return DUMMY_MARK; 
       }
       if (_marks[i].isSet == false) {
          return DUMMY_MARK;
       }
       _marks[i].isSet = false;
       _marked--;
       return DUMMY_MARK;
    }
 
    /**
      * Clear all marks, freeing the memory associated with them
      */
    final public void clearMarks() {
       for (int i = 0; i < MAX_MARKS; i++) {
          _marks[i].isSet = false;
       }
       _marked = 0;
    }
 
    /**
      * Skip all characters up to but not including the next 
      * unescaped newline
      * @return whether anything was skipped or not
      */
    public final boolean skipToEOL() throws IOException {
       boolean readSome = false;
       while (((_cur != '\n') || _escaped) && (_cur != EOF)) {
          read();
          readSome = true;
       }
       return readSome;
    }
 
    /**
      * skip to the first non-space character (skips tabs and spaces)
      * @return whether anything was skipped
      */
    public final boolean skipSpaces() throws IOException {
       boolean readSome = false;
       while (isSpace(_cur) && !_escaped) 
       {
          read();
          readSome = true;
       }
       return readSome;
    }
 
    /**
      * Skip to the next non-whitespace character (skips tabs, spaces,
      * and newlines).
      * @return whether anything was skipped
      */
    public final boolean skipWhitespace() throws IOException {
       boolean readSome = false;
       while (isWhitespace(_cur) && !_escaped) {
          read();
          readSome = true;
       }
       return readSome;
    }
 
 
    /**
      * If the current token is the specified character, and not escaped, 
      * advance to the next token and return true, else do not
      * advance and return false.
      */
    public final boolean parseChar(char c) throws IOException
    {
       if ((_cur == c) && (_cur != EOF) && !_escaped) {
          read();
          return true;
       }
       return false;
    }
 
    /**
      * Parse a string containing legal name characters only, meaning
      * that it begins with a valid Java identifier start character, 
      * and contains valid Java identifier part characters; except 
      * that '$' is not allowed to be part of a name. Any character
      * can be included in a name by escaping it, otherwise this 
      * means letters, digits, and underscores.
      */
    public final String parseName() throws IOException
    {
       StringBuffer name;
       if ( isNameStartChar(_cur) || _escaped ) {
          name = new StringBuffer();
          name.append((char) _cur); 
          read();
       } else {
          return null;
       }
 
       while ( isNameChar((char) _cur) || _escaped ) { 
          name.append((char) _cur);
          read();
       }
       return name.toString();
    }
 
    /**
      * Parse a string containing a number. Return it as either an
      * Integer or Long object. Longs are suffixed with an 'l'. If
      * the object on the stream is not a number, this method
      * will return a null.
      */
    public final Object parseNumber() throws IOException, ParseException
    {
 
       Object num = null;
       StringBuffer buf;
       if ( ! isNumberStartChar() ) {
          return null;
       }
       parseChar('+'); // skip it if it's a plus
 
       int mark = mark(); // prepare to rewind, it might not be a number
       try {
          buf = new StringBuffer();
          buf.append((char) _cur);
          read();
          while (isNumberChar()) {
             buf.append((char) _cur);
             read();
          }
          String snum = buf.toString();
          try {
             if ((_cur == 'l') || (_cur == 'L')) {
                num = new Long(snum);
                read();
             } else {
                num = new Integer(snum);
             }
          } catch (NumberFormatException e) {
             rewind(mark);
          }
          return num; 
       } finally {
          clearMark(mark);
       }
    }
 
    /**
      * If the supplied word is the next thing on the stream, advance
      * past it and return true; else do not advance and return false;
      * @param word the word we are looking for
      * @returns whether or not we found it
      */
    public final boolean parseString(String str) 
       throws IOException, ParseException
    {
       int spos = 0;
       int slen = str.length();
       int mark = mark(); // prepare to rewind, in case we don't get it
       try {
          while ((spos < slen) && (_cur == str.charAt(spos)) && (!_escaped)) {
             read();
             spos++;
          }
          if (slen == spos) {
             return true;
          } else if (spos > 0) {
             rewind(mark);
          }
          return false;  
       } finally {
          clearMark(mark);
       }
    }
 
 
    /**
      * Parse everything on the stream up until the supplied character is
      * found (unescaped), or until EOF. Everything read is appended
      * to the supplied string buffer, and the named character will
      * be the current char on the stream.  
      */
    public final boolean parseUntil(StringBuffer buf, char c) throws IOException 
    {
       boolean readSome = false;
       while (((_cur != c) || _escaped) && (_cur != EOF)) {
          buf.append((char) _cur);
          read();
          readSome = true;
       }
       return readSome;
    }
 
    /**
      * Parse everything on the stream up to, and including the supplied 
      * string.
      */
    public final void parseUntil(StringBuffer buf, String marker) 
       throws ParseException
    {
       if (marker.length() < 1) {
          return;
       } 
       char start = marker.charAt(0);
       int mark = DUMMY_MARK;
 
       buf.setLength(0);
       LOOKING: while(_cur != EOF) {
          if (_cur == start) {
             mark = mark();
             try {
                for (int i = 0; i < marker.length(); i++) {
                   if (_cur != marker.charAt(i)) {
                      rewind(mark);
                      buf.append((char) _cur);
                      read();
                      continue LOOKING;
                   }
                   read();
                }
                buf.append(marker);
                return;
             } finally {
                clearMark(mark);
             }
          }  
          buf.append((char) _cur);
          read();
       }
       clearMark(mark);
       throw new ParseException(this,"Expected " + marker 
             + " but reached end of file");
    }
 
 }
 
 
 /**
   * Utility class representing a mark
   */
 class Mark
 {
    final StringBuffer buf = new StringBuffer();
    int last;
    boolean isSet;
 
    public Mark()
    {
       isSet = false;   
    }
    
    public void set(int last, int cur, boolean escaped, int pos, String buffer)
    {
       this.last = last;
       if (escaped) {
          buf.append('\\');
       }
       buf.setLength(0);
       buf.append((char) cur);
       if (buffer != null) {
          buf.append(buffer.substring(pos));
       }
       isSet = true;
    }
 }
 
 
