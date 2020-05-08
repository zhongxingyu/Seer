 package org.pixielib.xml;
 
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 
 /**
  * Simple abstract "Quick & Dirty" SAX-like XML parser
  * <p/>
  * This class is useful for keeping track
  * of the current position in the input stream when
  * processing a node in the xml tree.
  */
 public abstract class QParser {
 
     private static final int BUFFER_SIZE = 8192;    // pushback buffer size
 
     private static final int UNDEFTAG = 0x0000;     // undefined tag
     private static final int BEGINTAG = 0x0001;     // begin tag
     private static final int ENDTAG = 0x0002;       // end tag
     private static final int EMPTYTAG = 0x0004;     // empty tag
     private static final int PROCTAG = 0x0008;      // processing instruction tag
     private static final int DECLTAG = 0x0010;      // declaration tag
 
     private static final int EMPTYTAGS = (EMPTYTAG | PROCTAG | DECLTAG);
 
     private PushbackReader reader;  // pushback reader for parsing
     private long position;          // position in input stream
     private int type;               // tag type
 
     public QParser() {
     }
 
     public long getPosition() {
         return position;
     }
 
     protected void setPosition(long position) {
         this.position = position;
     }
 
     /**
      * Parse an input stream
      *
      * @param reader, the reader to parse
      * @throws IOException, if an i/o exception occurs
      */
     public void parse(Reader reader) throws IOException {
         this.reader = new PushbackReader(reader, BUFFER_SIZE);
         parse();
     }
 
     /**
      * Unget a character from the reader
      *
      * @param c the character to unget
      * @throws IOException, if an i/o exception occurs
      */
     private void unget(int c) throws IOException {
         reader.unread(c);
         position--;
     }
 
     protected void parse() throws IOException {
 
        int c, save;
         char[] buffer;
         String tag, name;
 
         while ((c = read()) != -1) {
             buffer = lookahead(3);
             unget(c);
 
             if (c != '<') { // not a tag
                 value(getValue());
                 continue;
             }
 
             switch (buffer[0]) {
                 case '/':   // end tag
                    save = type;
                     endTag();
                    if (save != EMPTYTAG)
                        endElement();
                     break;
                 case '!':   // xml comment
                     if (buffer[1] == '-' && buffer[2] == '-') {
                         getComment();
                         type = UNDEFTAG;
                     }
                     // fallthrough
                 default:
                     tag = startTag();
                     type = tagType(tag);
                     name = tagName(tag);
                     startElement(name, tag);
                     if (type == EMPTYTAG)
                         endElement();
                     break;
             }
         }
     }
 
     private int read() throws IOException {
         int c = reader.read();
         position++;
         return c;
     }
 
     /**
      * Look ahead n characters
      *
      * @param n, the number of characters to lookahead
      * @return the char array containing up to n characters
      * @throws IOException, if an i/o error occurs
      */
     public char[] lookahead(int n) throws IOException {
         char[] buffer = new char[n];
 
         // doesn't alter position
 
         int i, c;
         for (i = 0; i < n && (c = reader.read()) != -1; i++) {
             buffer[i] = (char) c;
         }
 
         // unread the characters read
         reader.unread(buffer, 0, i);
 
         return buffer;
     }
 
     public abstract void value(String value);
 
     public abstract void startElement(String name, String tag);
 
     public abstract void endElement();
 
     public String startTag() throws IOException {
         StringBuilder tag = new StringBuilder();
 
         int c;
         while ((c = read()) != -1) {
             if (c == '>') {
                 tag.append((char) c);
                 break;
             }
             tag.append((char) c);
         }
 
         return tag.toString();
     }
 
     String endTag() throws IOException {
         if ((type & EMPTYTAGS) != 0) {
             type = UNDEFTAG;
             return "";
         }
 
         StringBuilder tag = new StringBuilder();
 
         int c;
         while ((c = read()) != -1) {
             if (c == '>') {
                 tag.append((char) c);
                 break;
             }
             tag.append((char) c);
         }
 
         type = UNDEFTAG;
 
         return tag.toString();
     }
 
     /**
      * Determine the type of a tag
      *
      * @param tag the tag to determine
      * @return the tag type
      */
     private int tagType(String tag) {
         char[] atag = tag.toCharArray();
 
         for (int i = 0; i < atag.length; i++) {
             switch (atag[i]) {
                 case '<':
                     if (i < atag.length - 1 && atag[i + 1] == '!')
                         return DECLTAG;
                     if (i < atag.length - 1 && atag[i + 1] == '?')
                         return PROCTAG;
                     if (i < atag.length - 1 && atag[i + 1] == '/')
                         return ENDTAG;
                     break;
                 case '>':
                     if (i > 0 && atag[i - 1] == '/')
                         return EMPTYTAG;
                     return BEGINTAG;
                 default:
                     break;
             }
         }
 
         return UNDEFTAG;
     }
 
     private String tagName(String tag) {
         StringBuilder name = new StringBuilder();
 
         char[] atag = tag.toCharArray();
 
         for (char c : atag) {
             switch (c) {
                 case '\t':
                 case '\n':
                 case '\r':
                 case ' ':
                 case '>':
                 case '/':
                     return name.toString();
                 case '<':
                 case '!':
                 case '?':
                     continue;
                 default:
                     name.append(c);
             }
         }
 
         return name.toString();
     }
 
     private String getComment() throws IOException {
 
         StringBuilder comment = new StringBuilder();
 
         int c;
         char[] buffer;
 
         while ((c = read()) != -1) {
             buffer = lookahead(2);
             if (c == '-' && buffer[0] == '-' && buffer[1] == '>') {
                 comment.append((char) c);    // -->
                 comment.append((char) read());
                 comment.append((char) read());
                 break;
             }
             comment.append((char) c);
         }
 
         return comment.toString();
     }
 
     private String getValue() throws IOException {
         StringBuilder value = new StringBuilder();
 
         int c;
         while ((c = read()) != -1) {
             switch (c) {
                 case '<':   // tag
                     unget(c);
                     return value.toString();
                 default:
                     value.append((char) c);
             }
         }
 
         return value.toString();
     }
 
 }
