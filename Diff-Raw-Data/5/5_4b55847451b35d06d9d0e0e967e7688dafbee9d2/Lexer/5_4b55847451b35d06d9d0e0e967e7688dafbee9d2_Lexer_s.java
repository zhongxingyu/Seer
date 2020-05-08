 /*
  *  Java HTML Tidy - JTidy
  *  HTML parser and pretty printer
  *
  *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  *  Institute of Technology, Institut National de Recherche en
  *  Informatique et en Automatique, Keio University). All Rights
  *  Reserved.
  *
  *  Contributing Author(s):
  *
  *     Dave Raggett <dsr@w3.org>
  *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
  *     Gary L Peskin <garyp@firstech.com> (Java development)
  *     Sami Lempinen <sami@lempinen.net> (release management)
  *     Fabrizio Giustina <fgiust at users.sourceforge.net>
  *
  *  The contributing author(s) would like to thank all those who
  *  helped with testing, bug fixes, and patience.  This wouldn't
  *  have been possible without all of you.
  *
  *  COPYRIGHT NOTICE:
  *
  *  This software and documentation is provided "as is," and
  *  the copyright holders and contributing author(s) make no
  *  representations or warranties, express or implied, including
  *  but not limited to, warranties of merchantability or fitness
  *  for any particular purpose or that the use of the software or
  *  documentation will not infringe any third party patents,
  *  copyrights, trademarks or other rights.
  *
  *  The copyright holders and contributing author(s) will not be
  *  liable for any direct, indirect, special or consequential damages
  *  arising out of any use of the software or documentation, even if
  *  advised of the possibility of such damage.
  *
  *  Permission is hereby granted to use, copy, modify, and distribute
  *  this source code, or portions hereof, documentation and executables,
  *  for any purpose, without fee, subject to the following restrictions:
  *
  *  1. The origin of this source code must not be misrepresented.
  *  2. Altered versions must be plainly marked as such and must
  *     not be misrepresented as being the original source.
  *  3. This Copyright notice may not be removed or altered from any
  *     source or altered source distribution.
  *
  *  The copyright holders and contributing author(s) specifically
  *  permit, without fee, and encourage the use of this source code
  *  as a component for supporting the Hypertext Markup Language in
  *  commercial products. If you use this source code in a product,
  *  acknowledgment is not required but would be appreciated.
  *
  */
 package org.w3c.tidy;
 
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.w3c.tidy.Node.NodeType;
 import org.w3c.tidy.Options.DoctypeModes;
 
 import static org.w3c.tidy.Versions.*;
 
 /**
  * Lexer for html parser.
  * <p>
  * Given a file stream fp it returns a sequence of tokens. GetToken(fp) gets the next token UngetToken(fp) provides one
  * level undo The tags include an attribute list: - linked list of attribute/value nodes - each node has 2
  * null-terminated strings. - entities are replaced in attribute values white space is compacted if not in preformatted
  * mode If not in preformatted mode then leading white space is discarded and subsequent white space sequences compacted
  * to single space chars. If XmlTags is no then Tag names are folded to upper case and attribute names to lower case.
  * Not yet done: - Doctype subset and marked sections
  * </p>
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public class Lexer
 {
 
     /**
      * state: ignore whitespace.
      */
     public static final short IGNORE_WHITESPACE = 0;
 
     /**
      * state: mixed content.
      */
     public static final short MIXED_CONTENT = 1;
 
     /**
      * state: preformatted.
      */
     public static final short PREFORMATTED = 2;
 
     /**
      * state: ignore markup.
      */
     public static final short IGNORE_MARKUP = 3;
 
     /**
      * lists all the known versions.
      */
     private static final W3CDoctype[] W3C_DOCTYPES = {
     	new W3CDoctype(  2, HT20, "HTML 2.0",               "-//IETF//DTD HTML 2.0//EN",              null                                                        ),
     	new W3CDoctype(  2, HT20, "HTML 2.0",               "-//IETF//DTD HTML//EN",                  null                                                        ),
     	new W3CDoctype(  2, HT20, "HTML 2.0",               "-//W3C//DTD HTML 2.0//EN",               null                                                        ),
     	new W3CDoctype(  1, HT32, "HTML 3.2",               "-//W3C//DTD HTML 3.2//EN",               null                                                        ),
     	new W3CDoctype(  1, HT32, "HTML 3.2",               "-//W3C//DTD HTML 3.2 Final//EN",         null                                                        ),
     	new W3CDoctype(  1, HT32, "HTML 3.2",               "-//W3C//DTD HTML 3.2 Draft//EN",         null                                                        ),
     	new W3CDoctype(  6, H40S, "HTML 4.0 Strict",        "-//W3C//DTD HTML 4.0//EN",               "http://www.w3.org/TR/REC-html40/strict.dtd"                ),
     	new W3CDoctype(  8, H40T, "HTML 4.0 Transitional",  "-//W3C//DTD HTML 4.0 Transitional//EN",  "http://www.w3.org/TR/REC-html40/loose.dtd"                 ),
     	new W3CDoctype(  7, H40F, "HTML 4.0 Frameset",      "-//W3C//DTD HTML 4.0 Frameset//EN",      "http://www.w3.org/TR/REC-html40/frameset.dtd"              ),
     	new W3CDoctype(  3, H41S, "HTML 4.01 Strict",       "-//W3C//DTD HTML 4.01//EN",              "http://www.w3.org/TR/html4/strict.dtd"                     ),
     	new W3CDoctype(  5, H41T, "HTML 4.01 Transitional", "-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/html4/loose.dtd"                      ),
     	new W3CDoctype(  4, H41F, "HTML 4.01 Frameset",     "-//W3C//DTD HTML 4.01 Frameset//EN",     "http://www.w3.org/TR/html4/frameset.dtd"                   ),
     	new W3CDoctype(  9, X10S, "XHTML 1.0 Strict",       "-//W3C//DTD XHTML 1.0 Strict//EN",       "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"         ),
     	new W3CDoctype( 11, X10T, "XHTML 1.0 Transitional", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"   ),
     	new W3CDoctype( 10, X10F, "XHTML 1.0 Frameset",     "-//W3C//DTD XHTML 1.0 Frameset//EN",     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"       ),
     	new W3CDoctype( 12, XH11, "XHTML 1.1",              "-//W3C//DTD XHTML 1.1//EN",              "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"              ),
     	new W3CDoctype( 13, XB10, "XHTML Basic 1.0",        "-//W3C//DTD XHTML Basic 1.0//EN",        "http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd"        ),
     };
 
     /**
      * getToken state: content.
      */
     private static final short LEX_CONTENT = 0;
 
     /**
      * getToken state: gt.
      */
     private static final short LEX_GT = 1;
 
     /**
      * getToken state: endtag.
      */
     private static final short LEX_ENDTAG = 2;
 
     /**
      * getToken state: start tag.
      */
     private static final short LEX_STARTTAG = 3;
 
     /**
      * getToken state: comment.
      */
     private static final short LEX_COMMENT = 4;
 
     /**
      * getToken state: doctype.
      */
     private static final short LEX_DOCTYPE = 5;
 
     /**
      * getToken state: procinstr.
      */
     private static final short LEX_PROCINSTR = 6;
 
     /**
      * getToken state: cdata.
      */
     private static final short LEX_CDATA = 8;
 
     /**
      * getToken state: section.
      */
     private static final short LEX_SECTION = 9;
 
     /**
      * getToken state: asp.
      */
     private static final short LEX_ASP = 10;
 
     /**
      * getToken state: jste.
      */
     private static final short LEX_JSTE = 11;
 
     /**
      * getToken state: php.
      */
     private static final short LEX_PHP = 12;
 
     /**
      * getToken state: xml declaration.
      */
     private static final short LEX_XMLDECL = 13;
 
     /**
      * file stream.
      */
     protected StreamIn in;
 
     /**
      * error output stream.
      */
     protected PrintWriter errout;
 
     /**
      * for accessibility errors.
      */
     protected short badAccess;
 
     /**
      * for bad style errors.
      */
     protected short badLayout;
 
     /**
      * for bad char encodings.
      */
     protected short badChars;
 
     /**
      * for mismatched/mispositioned form tags.
      */
     protected short badForm;
 
     /**
      * count of warnings in this document.
      */
     protected int warnings;
 
     /**
      * count of errors.
      */
     protected int errors;
     
     protected int optionErrors;
     protected int accessErrors;
     protected int infoMessages;
     protected int docErrors;
 
     /**
      * lines seen.
      */
     protected int lines;
 
     /**
      * at start of current token.
      */
     protected int columns;
 
     /**
      * used to collapse contiguous white space.
      */
     protected boolean waswhite;
 
     /**
      * true after token has been pushed back.
      */
     protected boolean pushed;
 
     /**
      * when space is moved after end tag.
      */
     protected boolean insertspace;
 
     /**
      * Netscape compatibility.
      */
     protected boolean excludeBlocks;
 
     /**
      * true if moved out of table.
      */
     protected boolean exiled;
 
     /**
      * true if xmlns attribute on html element.
      */
     protected boolean isvoyager;
 
     /**
      * bit vector of HTML versions.
      */
     protected int versions;
 
     /**
      * version as given by doctype (if any).
      */
     protected int doctype;
     
     protected int versionEmitted;
 
     /**
      * set if html or PUBLIC is missing.
      */
     protected boolean badDoctype;
 
     /**
      * start of current node.
      */
     protected int txtstart;
 
     /**
      * end of current node.
      */
     protected int txtend;
 
     /**
      * state of lexer's finite state machine.
      */
     protected short state;
 
     /**
      * current node.
      */
     protected Node token;
 
     /**
      * Lexer character buffer parse tree nodes span onto this buffer which contains the concatenated text contents of
      * all of the elements. Lexsize must be reset for each file. Byte buffer of UTF-8 chars.
      */
     protected byte[] lexbuf;
 
     /**
      * allocated.
      */
     protected int lexlength;
 
     /**
      * used.
      */
     protected int lexsize;
 
     /**
      * Inline stack for compatibility with Mosaic. For deferring text node.
      */
     protected Node inode;
 
     /**
      * for inferring inline tags.
      */
     protected int insert;
 
     /**
      * stack.
      */
     protected Stack<IStack> istack;
 
     /**
      * start of frame.
      */
     protected int istackbase;
 
     /**
      * used for cleaning up presentation markup.
      */
     protected Style styles;
 
     /**
      * configuration.
      */
     protected Configuration configuration;
 
     /**
      * already seen end body tag?
      */
     protected boolean seenEndBody;
 
     /**
      * already seen end html tag?
      */
     protected boolean seenEndHtml;
 
     /**
      * report.
      */
     protected Report report;
 
     /**
      * Root node is saved here.
      */
     protected Node root;
 
     /**
      * node list.
      */
     private List<Node> nodeList;
     
     protected String givenDoctype;
     
     /**
      * sequential number for generated css classes.
      */
     private int classNum;
 
     /**
      * Instantiates a new Lexer.
      * @param in StreamIn
      * @param configuration configuation instance
      * @param report report instance, for reporting errors
      */
     public Lexer(StreamIn in, Configuration configuration, Report report)
     {
         this.report = report;
         this.in = in;
         this.lines = 1;
         this.columns = 1;
         this.state = LEX_CONTENT;
         this.versions = (VERS_ALL | VERS_PROPRIETARY);
         this.doctype = VERS_UNKNOWN;
         this.insert = -1;
         this.istack = new Stack<IStack>();
         this.configuration = configuration;
         this.nodeList = new Vector<Node>();
     }
 
     /**
      * Creates a new node and add it to nodelist.
      * @return Node
      */
     public Node newNode()
     {
         Node node = new Node();
         this.nodeList.add(node);
         node.line = lines;
         node.column = columns;
         return node;
     }
 
     /**
      * Creates a new node and add it to nodelist.
      * @param type node type
      * @param textarray array of bytes contained in the Node
      * @param start start position
      * @param end end position
      * @return Node
      */
     public Node newNode(NodeType type, byte[] textarray, int start, int end)
     {
         Node node = new Node(type, textarray, start, end);
         this.nodeList.add(node);
         node.line = lines;
         node.column = columns;
         return node;
     }
     
     protected Node textToken() {
         return newNode(NodeType.TextNode, lexbuf, txtstart, txtend);
     }
     
     protected Node newLiteralTextNode(final String s) {
     	final int start = lexsize;
     	addStringToLexer(s);
         return newNode(NodeType.TextNode, lexbuf, start, lexsize);
     }
 
     /**
      * Creates a new node and add it to nodelist.
      * @param type node type
      * @param textarray array of bytes contained in the Node
      * @param start start position
      * @param end end position
      * @param element tag name
      * @return Node
      */
     public Node newNode(NodeType type, byte[] textarray, int start, int end, String element)
     {
         Node node = new Node(type, textarray, start, end, element, this.configuration.tt);
         this.nodeList.add(node);
         node.line = lines;
         node.column = columns;
         return node;
     }
 
     /**
      * Clones a node and add it to node list.
      * @param node Node
      * @return cloned Node
      */
     public Node cloneNode(Node node)
     {
         Node cnode = node.cloneNode(false);
         cnode.line = lines;
         cnode.column = columns;
         this.nodeList.add(cnode);
         for (AttVal att = cnode.attributes; att != null; att = att.next)
         {
             if (att.asp != null)
             {
                 this.nodeList.add(att.asp);
             }
             if (att.php != null)
             {
                 this.nodeList.add(att.php);
             }
         }
         return cnode;
     }
 
     /**
      * Clones an attribute value and add eventual asp or php node to node list.
      * @param attrs original AttVal
      * @return cloned AttVal
      */
     public AttVal cloneAttributes(AttVal attrs)
     {
         AttVal cattrs = (AttVal) attrs.clone();
         for (AttVal att = cattrs; att != null; att = att.next)
         {
             if (att.asp != null)
             {
                 this.nodeList.add(att.asp);
             }
             if (att.php != null)
             {
                 this.nodeList.add(att.php);
             }
         }
         return cattrs;
     }
 
     /**
      * Update <code>oldtextarray</code> in the current nodes.
      * @param oldtextarray previous text array
      * @param newtextarray new text array
      */
     protected void updateNodeTextArrays(byte[] oldtextarray, byte[] newtextarray)
     {
         Node node;
         for (int i = 0; i < this.nodeList.size(); i++)
         {
             node = this.nodeList.get(i);
             if (node.textarray == oldtextarray)
             {
                 node.textarray = newtextarray;
             }
         }
     }
 
     /**
      * Adds a new line node. Used for creating preformatted text from Word2000.
      * @return new line node
      */
     public Node newLineNode()
     {
         Node node = newNode();
 
         node.textarray = this.lexbuf;
         node.start = this.lexsize;
         addCharToLexer('\n');
         node.end = this.lexsize;
         return node;
     }
 
     /**
      * Has end of input stream been reached?
      * @return <code>true</code> if end of input stream been reached
      */
     public boolean endOfInput()
     {
         return this.in.isEndOfStream();
     }
 
     /**
      * Adds a byte to lexer buffer.
      * @param c byte to add
      */
     public void addByte(int c)
     {
         if (this.lexsize + 1 >= this.lexlength)
         {
             while (this.lexsize + 1 >= this.lexlength)
             {
                 if (this.lexlength == 0)
                 {
                     this.lexlength = 8192;
                 }
                 else
                 {
                     this.lexlength = this.lexlength * 2;
                 }
             }
 
             byte[] temp = this.lexbuf;
             this.lexbuf = new byte[this.lexlength];
             if (temp != null)
             {
                 System.arraycopy(temp, 0, this.lexbuf, 0, temp.length);
                 updateNodeTextArrays(temp, this.lexbuf);
             }
         }
 
         this.lexbuf[this.lexsize++] = (byte) c;
         this.lexbuf[this.lexsize] = (byte) '\0'; // debug
     }
 
     /**
      * Substitute the last char in buffer.
      * @param c new char
      */
     public void changeChar(byte c)
     {
         if (this.lexsize > 0)
         {
             this.lexbuf[this.lexsize - 1] = c;
         }
     }
 
     /**
      * Store char c as UTF-8 encoded byte stream.
      * @param c char to store
      */
     public void addCharToLexer(int c)
     {
         // Allow only valid XML characters. See: http://www.w3.org/TR/2004/REC-xml-20040204/#NT-Char
         // Fix by Pablo Mayrgundter 17-08-2004
 
         if ((this.configuration.isXmlOut() || this.configuration.isXHTML()) // only for xml output
             && !((c >= 0x20 && c <= 0xD7FF) // Check the common-case first.
                 || c == 0x9
                 || c == 0xA
                 || c == 0xD // Then white-space.
                 || (c >= 0xE000 && c <= 0xFFFD) // Then high-range unicode.
             || (c >= 0x10000 && c <= 0x10FFFF)))
         {
             return;
         }
 
         int i = 0;
         int[] count = new int[]{0};
         byte[] buf = new byte[10]; // unsigned char
 
         boolean err = EncodingUtils.encodeCharToUTF8Bytes(c, buf, null, count);
         if (err)
         {
             // replacement char 0xFFFD encoded as UTF-8
             buf[0] = (byte) 0xEF;
             buf[1] = (byte) 0xBF;
             buf[2] = (byte) 0xBD;
             count[0] = 3;
         }
 
         for (i = 0; i < count[0]; i++)
         {
             addByte(buf[i]); // uint
         }
 
     }
 
     /**
      * Adds a string to lexer buffer.
      * @param str String to add
      */
     public void addStringToLexer(String str)
     {
         for (int i = 0; i < str.length(); i++)
         {
             addCharToLexer(str.charAt(i));
         }
     }
     
     private void setLocus() {
         lines = in.getCurline();
         columns = in.getCurcol();
     }
     
     private enum ENTState {
         DEFAULT {
 			@Override
 			public boolean check(final int x) {
 				return TidyUtils.isNamechar((char) x);
 			}
 		},
         NUMDEC {
 			@Override
 			public boolean check(final int x) {
 				return TidyUtils.isDigit((char) x);
 			}
 		},
         NUMHEX {
 			@Override
 			public boolean check(final int x) {
 				return TidyUtils.isDigitHex((char) x);
 			}
 		};
         
         public abstract boolean check(int x);
     }
 
     /**
      * Parse an html entity.
      * @param mode mode
      */
     public void parseEntity(final short mode) {
         // No longer attempts to insert missing ';' for unknown
         // entities unless one was present already, since this
         // gives unexpected results.
         //
         // For example: <a href="something.htm?foo&bar&fred">
         // was tidied to: <a href="something.htm?foo&amp;bar;&amp;fred;">
         // rather than: <a href="something.htm?foo&amp;bar&amp;fred">
         //
         // My thanks for Maurice Buxton for spotting this.
         //
         // Also Randy Waki pointed out the following case for the
         // 04 Aug 00 version (bug #433012):
         //
         // For example: <a href="something.htm?id=1&lang=en">
         // was tidied to: <a href="something.htm?id=1&lang;=en">
         // rather than: <a href="something.htm?id=1&amp;lang=en">
         //
         // where "lang" is a known entity (#9001), but browsers would
         // misinterpret "&lang;" because it had a value > 256.
         //
         // So the case of an apparently known entity with a value > 256 and
         // missing a semicolon is handled specially.
         //
         // "ParseEntity" is also a bit of a misnomer - it handles entities and
         // numeric character references. Invalid NCR's are now reported.
 
         int start;
         ENTState entState = ENTState.DEFAULT;
         int charRead = 0;
         boolean semicolon = false;
         boolean isXml = configuration.isXmlTags();
         boolean preserveEntities = configuration.isPreserveEntities();
         int c, startcol;
 
         start = this.lexsize - 1; // to start at "&"
         startcol = this.in.getCurcol() - 1;
 
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM) {
             if (c == ';') {
                 semicolon = true;
                 break;
             }
             ++charRead;
 
             if (charRead == 1 && c == '#') {
                 // #431953 - start RJ
                 if (!this.configuration.isNcr()
                     || "BIG5".equals(this.configuration.getInCharEncodingName())
                     || "SHIFTJIS".equals(this.configuration.getInCharEncodingName())) {
                     this.in.ungetChar('#');
                     return;
                 }
                 // #431953 - end RJ
 
                 addCharToLexer(c);
                 entState = ENTState.NUMDEC;
                 continue;
             }
             else if (charRead == 2 && entState == ENTState.NUMDEC
                     && (c == 'x' || (!isXml && c == 'X'))) {
             	addCharToLexer(c);
                 entState = ENTState.NUMHEX;
                 continue;
             }
             
             if (entState.check(c)) {
                 addCharToLexer(c);
                 continue;
             }
 
             // otherwise put it back
             this.in.ungetChar(c);
             break;
         }
         
         final String str = TidyUtils.getString(this.lexbuf, start, this.lexsize - start);
 
         if ("&apos".equals(str) && !configuration.isXmlOut() && !this.isvoyager && !configuration.isXHTML()) {
             report.entityError(this, ErrorCode.APOS_UNDEFINED, str, 39);
         }
 
         final Entity ent = EntityTable.getDefaultEntityTable().entityInfo(str, isXml);
         final boolean found = ent != null;
         int ch = found ? ent.getCode() : 0;
         final int entver = found ? ent.getVersions() : isXml ? VERS_XML : VERS_PROPRIETARY;
 
         // deal with unrecognized or invalid entities
         // #433012 - fix by Randy Waki 17 Feb 01
         // report invalid NCR's - Terry Teague 01 Sep 01
         if (!found || (ch >= 128 && ch <= 159) || (ch >= 256 && c != ';')) {
             // set error position just before offending character
         	setLocus();
         	columns = startcol;
 
             if (this.lexsize > start + 1) {
                 if (ch >= 128 && ch <= 159) {
                     // invalid numeric character reference
                     int c1 = 0;
                     int replaceMode = Report.DISCARDED_CHAR;
 
                     if ("WIN1252".equals(configuration.getReplacementCharEncoding())) {
                         c1 = EncodingUtils.decodeWin1252(ch);
                     }
                     else if ("MACROMAN".equals(configuration.getReplacementCharEncoding())) {
                         c1 = EncodingUtils.decodeMacRoman(ch);
                     }
                     
                     if (c1 != 0) {
                     	replaceMode = Report.REPLACED_CHAR;
                     }
 
                     if (c != ';') {/* issue warning if not terminated by ';' */
                         report.entityError(this, ErrorCode.MISSING_SEMICOLON_NCR, str, c);
                     }
 
                     report.encodingError(this, ErrorCode.INVALID_NCR, ch, replaceMode);
 
                     if (c1 != 0) {
                         // make the replacement
                         this.lexsize = start;
                         addCharToLexer(c1);
                         semicolon = false;
                     } else {
                         /* discard */
                         this.lexsize = start;
                         semicolon = false;
                     }
                 } else {
                     report.entityError(this, ErrorCode.UNKNOWN_ENTITY, str, ch);
                 }
 
                 if (semicolon) {
                     addCharToLexer(';');
                 }
             } else {
                 // naked &
                 report.entityError(this, ErrorCode.UNESCAPED_AMPERSAND, str, ch);
             }
         } else {
             // issue warning if not terminated by ';'
             if (c != ';') {
                 // set error position just before offending character
             	setLocus();
                 this.columns = startcol;
                 report.entityError(this, ErrorCode.MISSING_SEMICOLON, str, c);
             }
             
             if (preserveEntities) {
             	addCharToLexer(';');
             } else {
 	            this.lexsize = start;
 	
 	            if (ch == 160 && mode == PREFORMATTED) {
 	                ch = ' ';
 	            }
 	
 	            addCharToLexer(ch);
 	
 	            if (ch == '&' && !this.configuration.isQuoteAmpersand()) {
 	                addStringToLexer("amp;");
 	            }
             }
             constrainVersion(entver);
         }
     }
 
     /**
      * Parses a tag name.
      * @return first char after the tag name
      */
     public char parseTagName()
     {
         int c;
 
         // fold case of first char in buffer
         c = this.lexbuf[this.txtstart];
 
         if (!this.configuration.isXmlTags() && TidyUtils.isUpper((char) c))
         {
             c = TidyUtils.toLower((char) c);
             this.lexbuf[this.txtstart] = (byte) c;
         }
 
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM)
         {
             if (!TidyUtils.isNamechar((char) c))
             {
                 break;
             }
 
             // fold case of subsequent chars
             if (!this.configuration.isXmlTags() && TidyUtils.isUpper((char) c))
             {
                 c = TidyUtils.toLower((char) c);
             }
 
             addCharToLexer(c);
         }
 
         this.txtend = this.lexsize;
         return (char) c;
     }
 
     /**
      * calls addCharToLexer for any char in the string.
      * @param str input String
      */
     public void addStringLiteral(String str)
     {
         int len = str.length();
         for (int i = 0; i < len; i++)
         {
             addCharToLexer(str.charAt(i));
         }
     }
 
     /**
      * calls addCharToLexer for any char in the string till len is reached.
      * @param str input String
      * @param len length of the substring to be added
      */
     void addStringLiteralLen(String str, int len)
     {
         int strlen = str.length();
         if (strlen < len)
         {
             len = strlen;
         }
         for (int i = 0; i < len; i++)
         {
             addCharToLexer(str.charAt(i));
         }
     }
 
     /**
      * Choose what version to use for new doctype.
      * @return html version constant
      */
     public int htmlVersion() {
     	int j = 0;
         int score = 0;
         int vers = versions;
         int dtver = doctype;
         DoctypeModes dtmode = configuration.getDocTypeMode();
         boolean xhtml = (configuration.isXmlOut() || isvoyager) &&
                      !configuration.isHtmlOut();
         boolean html4 = dtmode == DoctypeModes.Strict || dtmode == DoctypeModes.Loose
         		|| (VERS_FROM40 & dtver) != 0;
 
         for (int i = 0; i < W3C_DOCTYPES.length; ++i) {
         	if ((xhtml && (VERS_XHTML & W3C_DOCTYPES[i].vers) == 0) ||
                 (html4 && (VERS_FROM40 & W3C_DOCTYPES[i].vers) == 0)) {
                 continue;
             }
             if ((vers & W3C_DOCTYPES[i].vers) != 0 &&
             		(W3C_DOCTYPES[i].score < score || score == 0)) {
                 score = W3C_DOCTYPES[i].score;
                 j = i;
             }
         }
 
         if (score != 0) {
             return W3C_DOCTYPES[j].vers;
         }
         return VERS_UNKNOWN;
     }
     
     private static String getFPIFromVers(final int vers) {
         for (W3CDoctype d : W3C_DOCTYPES) {
             if (d.vers == vers) {
                 return d.fpi;
             }
         }
         return null;
     }
     
     private static String getSIFromVers(final int vers) {
         for (W3CDoctype d : W3C_DOCTYPES) {
             if (d.vers == vers) {
                 return d.si;
             }
         }
         return null;
     }
 
     static String getNameFromVers(final int vers) {
         for (W3CDoctype d : W3C_DOCTYPES) {
             if (d.vers == vers) {
                 return d.name;
             }
         }
         return null;
     }
     
     private static int getVersFromFPI(final String fpi) {
     	for (W3CDoctype d : W3C_DOCTYPES) {
             if (d.fpi.equalsIgnoreCase(fpi)) {
                 return d.vers;
             }
     	}
         return 0;
     }
     
     boolean warnMissingSIInEmittedDocType() {
         boolean isXhtml = isvoyager;
         Node doctype;
         /* Do not warn in XHTML mode */
         if (isXhtml) {
             return false;
         }
         /* Do not warn if emitted doctype is proprietary */
         if (getNameFromVers(versionEmitted) == null) {
             return false;
         }
         /* Do not warn if no SI is possible */
         if (getSIFromVers(versionEmitted) == null) {
             return false;
         }
         if ((doctype = root.findDocType()) != null
              && doctype.getAttrByName("SYSTEM") == null) {
             return true;
         }
         return false;
     }
 
     /**
      * Add meta element for Tidy. If the meta tag is already present, update release date.
      * @param root root node
      * @return <code>true</code> if the tag has been added
      */
     public boolean addGenerator(Node root)
     {
         AttVal attval;
         Node node;
         Node head = root.findHEAD();
 
         if (head != null)
         {
             String meta = "HTML Tidy for Java (vers. " + Report.RELEASE_DATE_STRING + "), see jtidy.sourceforge.net";
 
             for (node = head.content; node != null; node = node.next)
             {
                 if (node.is(TagId.META))
                 {
                     attval = node.getAttrByName("name");
 
                     if (attval != null && attval.value != null && "generator".equalsIgnoreCase(attval.value))
                     {
                         attval = node.getAttrByName("content");
 
                         if (attval != null
                             && attval.value != null
                             && attval.value.length() >= 9
                             && "HTML Tidy".equalsIgnoreCase(attval.value.substring(0, 9)))
                         {
                             attval.value = meta;
                             return false;
                         }
                     }
                 }
             }
 
             node = this.inferredTag(TagId.META);
             node.addAttribute("content", meta);
             node.addAttribute("name", "generator");
             head.insertNodeAtStart(node);
             return true;
         }
 
         return false;
     }
 
     /**
      * Check system keywords (keywords should be uppercase).
      * @param doctype doctype node
      * @return true if doctype keywords are all uppercase
      */
     public boolean checkDocTypeKeyWords(Node doctype)
     {
         int len = doctype.end - doctype.start;
         String s = TidyUtils.getString(this.lexbuf, doctype.start, len);
 
         return !(TidyUtils.findBadSubString("SYSTEM", s, s.length())
             || TidyUtils.findBadSubString("PUBLIC", s, s.length())
             || TidyUtils.findBadSubString("//DTD", s, s.length())
             || TidyUtils.findBadSubString("//W3C", s, s.length()) 
             || TidyUtils.findBadSubString("//EN", s, s.length()));
     }
 
     /**
      * Examine DOCTYPE to identify version.
      * @param doctype doctype node
      * @return version code
      */
     public int findGivenVersion(final Node doctype) {
     	AttVal fpi = doctype.getAttrByName("PUBLIC");
 
         if (fpi == null || fpi.value == null) {
             return VERS_UNKNOWN;
         }
         int vers = getVersFromFPI(fpi.value);
 
         if ((VERS_XHTML & vers) != 0) {
         	configuration.setXmlOut(true);
         	configuration.setXHTML(true);
             isvoyager = true;
         }
         /* todo: add a warning if case does not match? */
         fpi.value = getFPIFromVers(vers);
         return vers;
     }
 
     /**
      * Fix xhtml namespace.
      * @param root root Node
      * @param profile current profile
      */
     public void fixHTMLNameSpace(Node root, String profile)
     {
         Node node;
         AttVal attr;
 
         node = root.content;
         while (node != null && !node.is(TagId.HTML))
         {
             node = node.next;
         }
 
         if (node != null)
         {
 
             for (attr = node.attributes; attr != null; attr = attr.next)
             {
                 if (attr.attribute.equals("xmlns"))
                 {
                     break;
                 }
 
             }
 
             if (attr != null)
             {
                 if (!attr.value.equals(profile))
                 {
                     report.warning(this, node, null, ErrorCode.INCONSISTENT_NAMESPACE);
                     attr.value = profile;
                 }
             }
             else
             {
                 attr = new AttVal(node.attributes, null, '"', "xmlns", profile);
                 attr.dict = AttributeTable.getDefaultAttributeTable().findAttribute(attr);
                 node.attributes = attr;
             }
         }
     }
 
     /**
      * Put DOCTYPE declaration between the &lt:?xml version "1.0" ... ?&gt; declaration, if any, and the
      * <code>html</code> tag. Should also work for any comments, etc. that may precede the <code>html</code> tag.
      * @param root root node
      * @return new doctype node
      */
     Node newDocTypeNode(Node root)
     {
         Node html = root.findHTML();
         if (html == null)
         {
             return null;
         }
 
         Node newdoctype = newNode();
         newdoctype.setType(NodeType.DocTypeTag);
         newdoctype.next = html;
         newdoctype.parent = root;
         newdoctype.prev = null;
 
         if (html == root.content)
         {
             // No <?xml ... ?> declaration.
             root.content.prev = newdoctype;
             root.content = newdoctype;
             newdoctype.prev = null;
         }
         else
         {
             // we have an <?xml ... ?> declaration.
             newdoctype.prev = html.prev;
             newdoctype.prev.next = newdoctype;
         }
         html.prev = newdoctype;
         return newdoctype;
     }
 
     /**
      * Adds a new xhtml doctype to the document.
      * @param root root node
      * @return <code>true</code> if a doctype has been added
      */
     public boolean setXHTMLDocType(final Node root) {
     	Node doctype = root.findDocType();
     	DoctypeModes dtmode = configuration.getDocTypeMode();
     	final String pub = "PUBLIC";
     	final String sys = "SYSTEM";
     	
     	versionEmitted = apparentVersion();
     	if (dtmode == DoctypeModes.Omit) {
     		if (doctype != null) {
     			Node.discardElement(doctype);
     		}
     	}
     	if (dtmode == DoctypeModes.User && configuration.getDocTypeStr() == null) {
     		return false;
     	}
     	if (doctype == null) {
     		doctype = newDocTypeNode(root);
     		doctype.element = "html";
     	} else {
     		doctype.element = doctype.element.toLowerCase();
     	}
     	
     	switch(dtmode) {
         case Strict:
             /* XHTML 1.0 Strict */
         	doctype.repairAttrValue(pub, getFPIFromVers(X10S));
         	doctype.repairAttrValue(sys, getSIFromVers(X10S));
             versionEmitted = X10S;
             break;
         case Loose:
             /* XHTML 1.0 Transitional */
         	doctype.repairAttrValue(pub, getFPIFromVers(X10T));
         	doctype.repairAttrValue(sys, getSIFromVers(X10T));
             versionEmitted = X10T;
             break;
         case User:
             /* user defined document type declaration */
         	doctype.repairAttrValue(pub, configuration.getDocTypeStr());
         	doctype.repairAttrValue(sys, "");
             break;
         case Auto:
             if ((versions & XH11) != 0 && this.doctype == XH11) {
                 if (doctype.getAttrByName(sys) == null) {
                 	doctype.repairAttrValue(sys, getSIFromVers(XH11));
                 }
                 versionEmitted = XH11;
                 return true;
             }
             else if ((versions & XH11) != 0 && (versions & VERS_HTML40) == 0) {
             	doctype.repairAttrValue(pub, getFPIFromVers(XH11));
             	doctype.repairAttrValue(sys, getSIFromVers(XH11));
             	
                 versionEmitted = XH11;
             }
             else if ((versions & XB10) != 0 && this.doctype == XB10) {
                 if (doctype.getAttrByName(sys) == null) {
                 	doctype.repairAttrValue(sys, getSIFromVers(XB10));
                 }
                 versionEmitted = XB10;
                 return true;
             }
             else if ((versions & VERS_HTML40_STRICT) != 0) {
             	doctype.repairAttrValue(pub, getFPIFromVers(X10S));
             	doctype.repairAttrValue(sys, getSIFromVers(X10S));
                 versionEmitted = X10S;
             }
             else if ((versions & VERS_FRAMESET) != 0) {
             	doctype.repairAttrValue(pub, getFPIFromVers(X10F));
             	doctype.repairAttrValue(sys, getSIFromVers(X10F));
                 versionEmitted = X10F;
             }
             else if ((versions & VERS_LOOSE) != 0) {
             	doctype.repairAttrValue(pub, getFPIFromVers(X10T));
             	doctype.repairAttrValue(sys, getSIFromVers(X10T));
                 versionEmitted = X10T;
             }
             else {
                 if (doctype != null) {
                     Node.discardElement(doctype);
                 }
                 return false;
             }
             break;
         case Omit:
             assert(false);
             break;
         }
         return false;
     }
 
     /**
      * Return the html version used in document.
      * @return version code
      */
     public int apparentVersion() {
     	if ((doctype == XH11 || doctype == XB10) &&
     	        (versions & doctype) != 0) {
    	        return doctype;
     	} else {
     		return htmlVersion();
     	}
     }
 
     /**
      * Fixup doctype if missing.
      * @param root root node
      * @return <code>false</code> if current version has not been identified
      */
     public boolean fixDocType(Node root) {
         Node doctype = root.findDocType();
         DoctypeModes dtmode = configuration.getDocTypeMode();
         int guessed = VERS_UNKNOWN;
         boolean hadSI = false;
 
         if (dtmode == DoctypeModes.Auto &&
             (this.versions & this.doctype) != 0 &&
             !((VERS_XHTML & this.doctype) != 0 && !isvoyager)
             && doctype != null) {
             versionEmitted = this.doctype;
             return true;
         }
 
         if (dtmode == DoctypeModes.Omit) {
             if (doctype != null) {
                 Node.discardElement(doctype);
             }
             versionEmitted = apparentVersion();
             return true;
         }
 
         if (configuration.isXmlOut()) {
             return true;
         }
         if (doctype != null) {
             hadSI = doctype.getAttrByName("SYSTEM") != null;
         }
         if ((dtmode == DoctypeModes.Strict ||
              dtmode == DoctypeModes.Loose) && doctype != null) {
             Node.discardElement(doctype);
             doctype = null;
         }
 
         switch (dtmode)
         {
         case Strict:
             guessed = H41S;
             break;
         case Loose:
             guessed = H41T;
             break;
         case Auto:
             guessed = htmlVersion();
             break;
         }
 
         versionEmitted = guessed;
         if (guessed == VERS_UNKNOWN) {
             return false;
         }
         if (doctype != null) {
             doctype.element = doctype.element.toLowerCase();
         } else {
             doctype = newDocTypeNode(root);
             doctype.element = "html";
         }
 
         doctype.repairAttrValue("PUBLIC", getFPIFromVers(guessed));
 
         if (hadSI) {
             doctype.repairAttrValue("SYSTEM", getSIFromVers(guessed));
         }
         return true;
     }
 
     /**
      * Ensure XML document starts with <code>&lt;?XML version="1.0"?&gt;</code>. Add encoding attribute if not using
      * ASCII or UTF-8 output.
      * @param root root node
      * @return always true
      */
     public boolean fixXmlDecl(final Node root) {
         final Node xml;
         if (root.content != null && root.content.type == NodeType.XmlDecl) {
             xml = root.content;
         } else {
             xml = newNode(NodeType.XmlDecl, this.lexbuf, 0, 0);
             root.insertNodeAtStart(xml);
         }
         final AttVal version = xml.getAttrByName("version");
         final AttVal encoding = xml.getAttrByName("encoding");
 
         // We need to insert a check if declared encoding and output encoding mismatch
         // and fix the Xml declaration accordingly!!!
         if (encoding == null && !"UTF8".equals(this.configuration.getOutCharEncodingName())) {
         	final String enc = EncodingNameMapper.toIana(configuration.getOutCharEncodingName()).toLowerCase();
             if (enc != null) {
                 xml.addAttribute("encoding", enc);
             }
         }
         if (version == null) {
             xml.addAttribute("version", "1.0");
         }
         return true;
     }
 
     /**
      * Generates and inserts a new node.
      * @param name tag name
      * @return generated node
      */
     public Node inferredTag(final TagId id) {
     	final Dict dict = configuration.tt.lookup(id);
         if (dict == null) {
         	throw new RuntimeException();
         }
         Node node = newNode(NodeType.StartTag, this.lexbuf, this.txtstart, this.txtend, dict.name);
         node.implicit = true;
         return node;
     }
 
     private static enum CDataState {
     	INTERMEDIATE, STARTTAG, ENDTAG;
     }
 
     /**
      * Create a text node for the contents of a CDATA element like style or script which ends with &lt;/foo> for some
      * foo.
      * @param container container node
      * @return cdata node
      */
     public Node getCDATA(Node container)
     {
         int start = 0;
         int nested = 0;
         CDataState state = CDataState.INTERMEDIATE;
         int c;
         boolean isEmpty = true;
         boolean matches = false;
         boolean hasSrc = container.getAttrById(AttrId.SRC) != null;
 
         this.lines = this.in.getCurline();
         this.columns = this.in.getCurcol();
         this.waswhite = false;
         this.txtstart = this.lexsize;
         this.txtend = this.lexsize;
 
         /* seen start tag, look for matching end tag */
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM) {
         	addCharToLexer(c);
         	txtend = lexsize;
         	
             if (state == CDataState.INTERMEDIATE) {
             	if (c != '<') {
                     if (isEmpty && !TidyUtils.isWhite((char) c)) {
                         isEmpty = false;
                     }
                     continue;
                 }
             	c = in.readChar();
             	if (TidyUtils.isLetter((char) c)) {
             		/* <head><script src=foo><meta name=foo content=bar>*/
                     if (hasSrc && isEmpty && container.is(TagId.SCRIPT)) {
                         /* ReportError(doc, container, NULL, MISSING_ENDTAG_FOR); */
                         lexsize = txtstart;
                         in.ungetChar(c);
                         in.ungetChar('<');
                         return null;
                     }
                     addCharToLexer(c);
                     start = lexsize - 1;
                     state = CDataState.STARTTAG;
             	} else if (c == '/') {
                     addCharToLexer(c);
                     c = in.readChar();
                     if (!TidyUtils.isLetter((char) c)) {
                         in.ungetChar(c);
                         continue;
                     }
                     in.ungetChar(c);
                     start = lexsize;
                     state = CDataState.ENDTAG;
                 } else if (c == '\\') {
                     /* recognize document.write("<script><\/script>") */
                 	addCharToLexer(c);
                 	c = in.readChar();
                     if (c != '/') {
                     	in.ungetChar(c);
                         continue;
                     }
                     addCharToLexer(c);
                 	c = in.readChar();
                 	if (!TidyUtils.isLetter((char) c)) {
                 		in.ungetChar(c);
                         continue;
                     }
                 	in.ungetChar(c);
                     start = lexsize;
                     state = CDataState.ENDTAG;
                 } else {
                 	in.ungetChar(c);
                 }
             } else if (state == CDataState.STARTTAG) {
             	/* '<' + Letter found */
             	if (TidyUtils.isLetter((char) c)) {
                      continue;
             	}
             	matches = container.element.equalsIgnoreCase(TidyUtils.getString(lexbuf, start,
             			container.element.length()));
             	if (matches) {
             		nested++;
             	}
             	state = CDataState.INTERMEDIATE;
             } else if (state == CDataState.ENDTAG) {
             	/* '<' + '/' + Letter found */
             	if (TidyUtils.isLetter((char) c)) {
                     continue;
             	}
             	matches = container.element.equalsIgnoreCase(TidyUtils.getString(lexbuf, start,
             			container.element.length()));
                 if (isEmpty && !matches) {
                     /* ReportError(doc, container, NULL, MISSING_ENDTAG_FOR); */
 
                     for (int i = lexsize - 1; i >= start; --i) {
                         in.ungetChar(lexbuf[i]);
                     }
                     in.ungetChar('/');
                     in.ungetChar('<');
                     break;
                 }
                 if (matches && nested-- <= 0) {
                     for (int i = lexsize - 1; i >= start; --i) {
                     	in.ungetChar(lexbuf[i]);
                     }
                     in.ungetChar('/');
                     in.ungetChar('<');
                     lexsize -= (lexsize - start) + 2;
                     break;
                 } else if (lexbuf[start - 2] != '\\') {
                     /* if the end tag is not already escaped using backslash */
                 	lines = in.getCurline();
                     columns = in.getCurcol();
                     columns -= 3;
                     report.warning(this, null, null, ErrorCode.BAD_CDATA_CONTENT);
 
                     /* if javascript insert backslash before / */
                     if (container.isJavaScript()) {
                         for (int i = lexsize; i > start-1; --i) {
                             lexbuf[i] = lexbuf[i-1];
                         }
                         lexbuf[start-1] = '\\';
                         lexsize++;
                     }
                 }
                 state = CDataState.INTERMEDIATE;
             }
         }
         if (isEmpty) {
             lexsize = txtstart = txtend;
         } else {
             txtend = lexsize;
         }
         if (c == StreamIn.END_OF_STREAM) {
             report.warning(this, container, null, ErrorCode.MISSING_ENDTAG_FOR);
         }
 	    /* this was disabled for some reason... */
 //	    #if 0
 //	        if (lexer->txtend > lexer->txtstart)
 //	            return TextToken(lexer);
 //	        else
 //	            return NULL;
 //	    #else
 	        return textToken();
 //	    #endif
     }
 
     /**
      *
      *
      */
     public void ungetToken()
     {
         this.pushed = true;
     }
 
     /**
      * Gets a token.
      * @param mode one of the following:
      * <ul>
      * <li><code>MixedContent</code>-- for elements which don't accept PCDATA</li>
      * <li><code>Preformatted</code>-- white spacepreserved as is</li>
      * <li><code>IgnoreMarkup</code>-- for CDATA elements such as script, style</li>
      * </ul>
      * @return next Node
      */
     public Node getToken(short mode)
     {
         int c = 0;
         int badcomment = 0;
         // pass by reference
         boolean[] isempty = new boolean[1];
         AttVal attributes = null;
 
         if (this.pushed)
         {
             // duplicate inlines in preference to pushed text nodes when appropriate
             if (this.token.type != NodeType.TextNode || (this.insert == -1 && this.inode == null))
             {
                 this.pushed = false;
                 return this.token;
             }
         }
 
         // at start of block elements, unclosed inline
         // elements are inserted into the token stream
         if (this.insert != -1 || this.inode != null)
         {
             return insertedToken();
         }
 
         this.lines = this.in.getCurline();
         this.columns = this.in.getCurcol();
         this.waswhite = false;
 
         this.txtstart = this.lexsize;
         this.txtend = this.lexsize;
 
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM)
         {
         	if (this.insertspace) {
                 addCharToLexer(' ');
                 this.waswhite = true;
                 this.insertspace = false;
             }
 
             // treat \r\n as \n and \r as \n
             if (c == '\r')
             {
                 c = this.in.readChar();
 
                 if (c != '\n')
                 {
                     this.in.ungetChar(c);
                 }
 
                 c = '\n';
             }
 
             addCharToLexer(c);
 
             switch (this.state)
             {
                 case LEX_CONTENT :
                     // element content
 
                     // Discard white space if appropriate.
                     // Its cheaper to do this here rather than in parser methods for elements that
                     // don't have mixed content.
                     if (TidyUtils.isWhite((char) c) && (mode == IGNORE_WHITESPACE) && this.lexsize == this.txtstart + 1)
                     {
                         --this.lexsize;
                         this.waswhite = false;
                         this.lines = this.in.getCurline();
                         this.columns = this.in.getCurcol();
                         continue;
                     }
 
                     if (c == '<')
                     {
                         this.state = LEX_GT;
                         continue;
                     }
 
                     if (TidyUtils.isWhite((char) c))
                     {
                         // was previous char white?
                         if (this.waswhite)
                         {
                             if (mode != PREFORMATTED && mode != IGNORE_MARKUP)
                             {
                                 --this.lexsize;
                                 this.lines = this.in.getCurline();
                                 this.columns = this.in.getCurcol();
                             }
                         }
                         else
                         {
                             // prev char wasn't white
                             this.waswhite = true;
 
                             if (mode != PREFORMATTED && mode != IGNORE_MARKUP && c != ' ')
                             {
                                 changeChar((byte) ' ');
                             }
                         }
 
                         continue;
                     }
                     else if (c == '&' && mode != IGNORE_MARKUP)
                     {
                         parseEntity(mode);
                     }
 
                     // this is needed to avoid trimming trailing whitespace
                     if (mode == IGNORE_WHITESPACE)
                     {
                         mode = MIXED_CONTENT;
                     }
 
                     this.waswhite = false;
                     continue;
 
                 case LEX_GT :
                     // <
 
                     // check for endtag
                     if (c == '/')
                     {
                         c = this.in.readChar();
                         if (c == StreamIn.END_OF_STREAM)
                         {
                             this.in.ungetChar(c);
                             continue;
                         }
 
                         addCharToLexer(c);
 
                         if (TidyUtils.isLetter((char) c))
                         {
                             this.lexsize -= 3;
                             this.txtend = this.lexsize;
                             this.in.ungetChar(c);
                             this.state = LEX_ENDTAG;
                             this.lexbuf[this.lexsize] = (byte) '\0'; // debug
                             in.moveCurcol(-2);
 
                             // if some text before the </ return it now
                             if (this.txtend > this.txtstart)
                             {
                                 // trim space char before end tag
                                 if (mode == IGNORE_WHITESPACE && this.lexbuf[this.lexsize - 1] == (byte) ' ')
                                 {
                                     this.lexsize -= 1;
                                     this.txtend = this.lexsize;
                                 }
 
                                 this.token = textToken();
                                 return this.token;
                             }
 
                             continue; // no text so keep going
                         }
 
                         // otherwise treat as CDATA
                         this.waswhite = false;
                         this.state = LEX_CONTENT;
                         continue;
                     }
 
                     if (mode == IGNORE_MARKUP)
                     {
                         // otherwise treat as CDATA
                         this.waswhite = false;
                         this.state = LEX_CONTENT;
                         continue;
                     }
 
                     // look out for comments, doctype or marked sections this isn't quite right, but its getting there
                     if (c == '!')
                     {
                         c = this.in.readChar();
 
                         if (c == '-')
                         {
                             c = this.in.readChar();
 
                             if (c == '-')
                             {
                                 this.state = LEX_COMMENT; // comment
                                 this.lexsize -= 2;
                                 this.txtend = this.lexsize;
 
                                 // if some text before < return it now
                                 if (this.txtend > this.txtstart)
                                 {
                                     this.token = textToken();
                                     return this.token;
                                 }
 
                                 this.txtstart = this.lexsize;
                                 continue;
                             }
 
                             report.warning(this, null, null, ErrorCode.MALFORMED_COMMENT);
                         }
                         else if (c == 'd' || c == 'D')
                         {
                             this.state = LEX_DOCTYPE; // doctype
                             this.lexsize -= 2;
                             this.txtend = this.lexsize;
                             mode = IGNORE_WHITESPACE;
 
                             // skip until white space or '>'
 
                             for (;;)
                             {
                                 c = this.in.readChar();
 
                                 if (c == StreamIn.END_OF_STREAM || c == '>')
                                 {
                                     this.in.ungetChar(c);
                                     break;
                                 }
 
                                 if (!TidyUtils.isWhite((char) c))
                                 {
                                     continue;
                                 }
 
                                 // and skip to end of whitespace
 
                                 for (;;)
                                 {
                                     c = this.in.readChar();
 
                                     if (c == StreamIn.END_OF_STREAM || c == '>')
                                     {
                                         this.in.ungetChar(c);
                                         break;
                                     }
 
                                     if (TidyUtils.isWhite((char) c))
                                     {
                                         continue;
                                     }
 
                                     this.in.ungetChar(c);
                                     break;
                                 }
 
                                 break;
                             }
 
                             // if some text before < return it now
                             if (this.txtend > this.txtstart)
                             {
                                 this.token = textToken();
                                 return this.token;
                             }
 
                             this.txtstart = this.lexsize;
                             continue;
                         }
                         else if (c == '[')
                         {
                             // Word 2000 embeds <![if ...]> ... <![endif]> sequences
                             this.lexsize -= 2;
                             this.state = LEX_SECTION;
                             this.txtend = this.lexsize;
 
                             // if some text before < return it now
                             if (this.txtend > this.txtstart)
                             {
                                 this.token = textToken();
                                 return this.token;
                             }
 
                             this.txtstart = this.lexsize;
                             continue;
                         }
 
                         // otherwise swallow chars up to and including next '>'
                         while (true)
                         {
                             c = this.in.readChar();
                             if (c == '>')
                             {
                                 break;
                             }
                             if (c == -1)
                             {
                                 this.in.ungetChar(c);
                                 break;
                             }
                         }
 
                         this.lexsize -= 2;
                         this.lexbuf[this.lexsize] = (byte) '\0';
                         this.state = LEX_CONTENT;
                         continue;
                     }
 
                     // processing instructions
 
                     if (c == '?')
                     {
                         this.lexsize -= 2;
                         this.state = LEX_PROCINSTR;
                         this.txtend = this.lexsize;
 
                         // if some text before < return it now
                         if (this.txtend > this.txtstart)
                         {
                             this.token = textToken();
                             return this.token;
                         }
 
                         this.txtstart = this.lexsize;
                         continue;
                     }
 
                     // Microsoft ASP's e.g. <% ... server-code ... %>
                     if (c == '%')
                     {
                         this.lexsize -= 2;
                         this.state = LEX_ASP;
                         this.txtend = this.lexsize;
 
                         // if some text before < return it now
                         if (this.txtend > this.txtstart)
                         {
                             this.token = textToken();
                             return this.token;
                         }
 
                         this.txtstart = this.lexsize;
                         continue;
                     }
 
                     // Netscapes JSTE e.g. <# ... server-code ... #>
                     if (c == '#')
                     {
                         this.lexsize -= 2;
                         this.state = LEX_JSTE;
                         this.txtend = this.lexsize;
 
                         // if some text before < return it now
                         if (this.txtend > this.txtstart)
                         {
                             this.token = textToken();
                             return this.token;
                         }
 
                         this.txtstart = this.lexsize;
                         continue;
                     }
 
                     // check for start tag
                     if (TidyUtils.isLetter((char) c))
                     {
                         this.in.ungetChar(c); // push back letter
                         in.ungetChar('<');
                         this.lexsize -= 2; // discard " <" + letter
                         this.txtend = this.lexsize;
                         this.state = LEX_STARTTAG; // ready to read tag name
 
                         // if some text before < return it now
                         if (this.txtend > this.txtstart)
                         {
                             this.token = textToken();
                             return this.token;
                         }
 
                         continue; // no text so keep going
                     }
 
                     // otherwise treat as CDATA
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     continue;
 
                 case LEX_ENDTAG :
                     // </letter
                     this.txtstart = this.lexsize - 1;
                     in.moveCurcol(2);
                     c = parseTagName();
                     this.token = newNode(NodeType.EndTag, // create endtag token
                         this.lexbuf,
                         this.txtstart,
                         this.txtend,
                         TidyUtils.getString(this.lexbuf, this.txtstart, this.txtend - this.txtstart));
                     this.lexsize = this.txtstart;
                     this.txtend = this.txtstart;
 
                     // skip to '>'
                     while (TidyUtils.isWhite((char) c))
                     {
                         c = this.in.readChar();
                     }
 
                     if (c == StreamIn.END_OF_STREAM) {
                         if (!configuration.isTidyCompat()) {
                         	in.ungetChar(c);
                         	report.attrError(this, token, null, ErrorCode.UNEXPECTED_GT);
                         }
                         continue;
                     }
 
                     // should be at the '>' if we're not, assume one
                     if (c != '>') {
                         this.in.ungetChar(c);
                         c = '>';
                         report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_GT);
                     }
 
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     return this.token; // the endtag token
 
                 case LEX_STARTTAG :
                 	c = in.readChar();
                 	changeChar((byte)c);
                     // first letter of tagname
                     this.txtstart = this.lexsize - 1; // set txtstart to first letter
                     c = parseTagName();
                     isempty[0] = false;
                     attributes = null;
                     this.token = newNode(
                         (isempty[0] ? NodeType.StartEndTag : NodeType.StartTag),
                         this.lexbuf,
                         this.txtstart,
                         this.txtend,
                         TidyUtils.getString(this.lexbuf, this.txtstart, this.txtend - this.txtstart));
 
                     // parse attributes, consuming closing ">"
                     if (c != '>')
                     {
                         if (c == '/')
                         {
                             this.in.ungetChar(c);
                         }
 
                         attributes = parseAttrs(isempty);
                     }
 
                     if (isempty[0])
                     {
                         this.token.type = NodeType.StartEndTag;
                     }
 
                     this.token.attributes = attributes;
                     this.lexsize = this.txtstart;
                     this.txtend = this.txtstart;
 
                     // swallow newline following start tag
                     // special check needed for CRLF sequence
                     // this doesn't apply to empty elements
                     // nor to preformatted content that needs escaping
 
                     if ((mode != PREFORMATTED || preContent(this.token))
                         && (this.token.expectsContent() || this.token.is(TagId.BR)))
                     {
 
                         c = this.in.readChar();
 
                         if (c == '\r')
                         {
                             c = this.in.readChar();
 
                             if (c != '\n')
                             {
                                 this.in.ungetChar(c);
                             }
                         }
                         else if (c != '\n' && c != '\f')
                         {
                             this.in.ungetChar(c);
                         }
 
                         this.waswhite = true; // to swallow leading whitespace
                     }
                     else
                     {
                         this.waswhite = false;
                     }
 
                     this.state = LEX_CONTENT;
 
                     if (this.token.tag == null)
                     {
                         report.error(this, null, this.token, ErrorCode.UNKNOWN_ELEMENT);
                     }
                     else if (!this.configuration.isXmlTags())
                     {
                         constrainVersion(this.token.tag.versions);
 
                         if (TidyUtils.toBoolean(this.token.tag.versions & VERS_PROPRIETARY))
                         {
                             if (!this.configuration.isMakeClean() || (!this.token.is(TagId.NOBR) &&
                                 !this.token.is(TagId.WBR))) {
                                 report.warning(this, null, this.token, ErrorCode.PROPRIETARY_ELEMENT);
                                 
                                 if (token.is(TagId.LAYER)) {
                                     badLayout |= Report.USING_LAYER;
                                 }
                                 else if (token.is(TagId.SPACER)) {
                                     badLayout |= Report.USING_SPACER;
                                 }
                                 else if (token.is(TagId.NOBR)) {
                                     badLayout |= Report.USING_NOBR;
                                 }
                             }
                         }
                         token.repairDuplicateAttributes(this);
                     } else {
                     	token.repairDuplicateAttributes(this);
                     }
                     return token; // return start tag
 
                 case LEX_COMMENT :
                     // seen <!-- so look for -->
 
                     if (c != '-')
                     {
                         continue;
                     }
 
                     c = this.in.readChar();
                     addCharToLexer(c);
 
                     if (c != '-')
                     {
                         continue;
                     }
 
                     end_comment : while (true)
                     {
                         c = this.in.readChar();
 
                         if (c == '>')
                         {
                             if (badcomment != 0)
                             {
                                 report.warning(this, null, null, ErrorCode.MALFORMED_COMMENT);
                             }
 
                             this.txtend = this.lexsize - 2; // AQ 8Jul2000
                             this.lexbuf[this.lexsize] = (byte) '\0';
                             this.state = LEX_CONTENT;
                             this.waswhite = false;
                             this.token = newNode(NodeType.CommentTag, this.lexbuf, this.txtstart, this.txtend);
 
                             // now look for a line break
 
                             c = this.in.readChar();
 
                             if (c == '\r')
                             {
                                 c = this.in.readChar();
 
                                 if (c != '\n')
                                 {
                                     this.token.linebreak = true;
                                 }
                             }
 
                             if (c == '\n')
                             {
                                 this.token.linebreak = true;
                             }
                             else
                             {
                                 this.in.ungetChar(c);
                             }
 
                             return this.token;
                         }
 
                         // note position of first such error in the comment
                         if (badcomment == 0)
                         {
                             this.lines = this.in.getCurline();
                             this.columns = this.in.getCurcol() - 3;
                         }
 
                         badcomment++;
                         if (this.configuration.isFixComments())
                         {
                             this.lexbuf[this.lexsize - 2] = (byte) '=';
                         }
 
                         addCharToLexer(c);
 
                         // if '-' then look for '>' to end the comment
                         if (c != '-')
                         {
                             break end_comment;
                         }
 
                     }
                     // otherwise continue to look for -->
                     this.lexbuf[this.lexsize - 2] = (byte) '=';
                     continue;
 
                 case LEX_DOCTYPE :
                     // seen <!d so look for '> ' munging whitespace
 
                 	/* use ParseDocTypeDecl() to tokenize doctype declaration */
                     in.ungetChar(c);
                     this.lexsize -= 1;
                     token = parseDocTypeDecl();
                     
                     this.txtend = this.lexsize;
 //                    this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
 
                     // make a note of the version named by the doctype
                     if (doctype == VERS_UNKNOWN && token != null && !configuration.isXmlTags()) {
                     	this.doctype = findGivenVersion(this.token);
                     }
                     return this.token;
 
                 case LEX_PROCINSTR :
                     // seen <? so look for '> '
                     // check for PHP preprocessor instructions <?php ... ?>
 
                     if (this.lexsize - this.txtstart == 3)
                     {
                         if ((TidyUtils.getString(this.lexbuf, this.txtstart, 3)).equals("php"))
                         {
                             this.state = LEX_PHP;
                             continue;
                         }
                     }
 
                     if (this.lexsize - this.txtstart == 4)
                     {
                         if ((TidyUtils.getString(this.lexbuf, this.txtstart, 3)).equals("xml")
                             && TidyUtils.isWhite((char) this.lexbuf[this.txtstart + 3]))
                         {
                             this.state = LEX_XMLDECL;
                             attributes = null;
                             continue;
                         }
                     }
 
                     if (this.configuration.isXmlPIs()) // insist on ?> as terminator
                     {
                         if (c != '?')
                         {
                             continue;
                         }
 
                         // now look for '>'
                         c = this.in.readChar();
 
                         if (c == StreamIn.END_OF_STREAM)
                         {
                             report.warning(this, null, null, ErrorCode.UNEXPECTED_END_OF_FILE);
                             this.in.ungetChar(c);
                             continue;
                         }
 
                         addCharToLexer(c);
                     }
 
                     if (c != '>')
                     {
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.ProcInsTag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 case LEX_ASP :
                     // seen <% so look for "%> "
                     if (c != '%')
                     {
                         continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.AspTag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 case LEX_JSTE :
                     // seen <# so look for "#> "
                     if (c != '#')
                     {
                         continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.JsteTag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 case LEX_PHP :
                     // seen " <?php" so look for "?> "
                     if (c != '?')
                     {
                         continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.PhpTag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 case LEX_XMLDECL : // seen "<?xml" so look for "?>"
 
                     if (TidyUtils.isWhite((char) c) && c != '?')
                     {
                         continue;
                     }
 
                     // get pseudo-attribute
                     if (c != '?')
                     {
                         String name;
                         Node[] asp = new Node[1];
                         Node[] php = new Node[1];
                         AttVal av = new AttVal();
                         int[] pdelim = new int[1];
                         isempty[0] = false;
 
                         this.in.ungetChar(c);
 
                         name = this.parseAttribute(isempty, asp, php);
                         av.attribute = name;
 
                         av.value = this.parseValue(name, true, isempty, pdelim);
                         av.delim = pdelim[0];
                         av.next = attributes;
                         av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
 
                         attributes = av;
                         // continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
                     this.lexsize -= 1;
                     this.txtend = this.txtstart;
                     this.lexbuf[this.txtend] = '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.XmlDecl, this.lexbuf, this.txtstart, this.txtend);
                     this.token.attributes = attributes;
                     return this.token;
 
                 case LEX_SECTION :
                     // seen " <![" so look for "]> "
                     if (c == '[')
                     {
                         if (this.lexsize == (this.txtstart + 6)
                             && (TidyUtils.getString(this.lexbuf, this.txtstart, 6)).equals("CDATA["))
                         {
                             this.state = LEX_CDATA;
                             this.lexsize -= 6;
                             continue;
                         }
                     }
 
                     if (c != ']')
                     {
                         continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.SectionTag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 case LEX_CDATA :
                     // seen " <![CDATA[" so look for "]]> "
                     if (c != ']')
                     {
                         continue;
                     }
 
                     // now look for ']'
                     c = this.in.readChar();
 
                     if (c != ']')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     // now look for '>'
                     c = this.in.readChar();
 
                     if (c != '>')
                     {
                         this.in.ungetChar(c);
                         continue;
                     }
 
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                     this.lexbuf[this.lexsize] = (byte) '\0';
                     this.state = LEX_CONTENT;
                     this.waswhite = false;
                     this.token = newNode(NodeType.CDATATag, this.lexbuf, this.txtstart, this.txtend);
                     return this.token;
 
                 default :
                     // should never reach here
                     break;
             }
         }
 
         if (this.state == LEX_CONTENT) // text string
         {
             this.txtend = this.lexsize;
 
             if (this.txtend > this.txtstart)
             {
                 this.in.ungetChar(c);
 
                 if (this.lexbuf[this.lexsize - 1] == (byte) ' ')
                 {
                     this.lexsize -= 1;
                     this.txtend = this.lexsize;
                 }
 
                 this.token = textToken();
                 return this.token;
             }
         }
         else if (this.state == LEX_COMMENT) // comment
         {
             if (c == StreamIn.END_OF_STREAM)
             {
                 report.warning(this, null, null, ErrorCode.MALFORMED_COMMENT);
             }
 
             this.txtend = this.lexsize;
             this.lexbuf[this.lexsize] = (byte) '\0';
             this.state = LEX_CONTENT;
             this.waswhite = false;
             this.token = newNode(NodeType.CommentTag, this.lexbuf, this.txtstart, this.txtend);
             return this.token;
         }
 
         return null;
     }
 
     /**
      * parser for ASP within start tags Some people use ASP for to customize attributes Tidy isn't really well suited to
      * dealing with ASP This is a workaround for attributes, but won't deal with the case where the ASP is used to
      * tailor the attribute value. Here is an example of a work around for using ASP in attribute values:
      * <code>href='<%=rsSchool.Fields("ID").Value%>'</code> where the ASP that generates the attribute value is
      * masked from Tidy by the quotemarks.
      * @return parsed Node
      */
     public Node parseAsp()
     {
         int c;
         Node asp = null;
 
         this.txtstart = this.lexsize;
 
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM)
         {
 
             addCharToLexer(c);
 
             if (c != '%')
             {
                 continue;
             }
 
             if ((c = this.in.readChar()) == StreamIn.END_OF_STREAM)
             {
                 break;
             }
             addCharToLexer(c);
 
             if (c == '>')
             {
                 break;
             }
         }
 
         this.lexsize -= 2;
         this.txtend = this.lexsize;
 
         if (this.txtend > this.txtstart)
         {
             asp = newNode(NodeType.AspTag, this.lexbuf, this.txtstart, this.txtend);
         }
 
         this.txtstart = this.txtend;
         return asp;
     }
 
     /**
      * PHP is like ASP but is based upon XML processing instructions, e.g. <code>&lt;?php ... ?&gt;</code>.
      * @return parsed Node
      */
     public Node parsePhp()
     {
         int c;
         Node php = null;
 
         this.txtstart = this.lexsize;
 
         while ((c = this.in.readChar()) != StreamIn.END_OF_STREAM)
         {
             addCharToLexer(c);
 
             if (c != '?')
             {
                 continue;
             }
 
             if ((c = this.in.readChar()) == StreamIn.END_OF_STREAM)
             {
                 break;
             }
             addCharToLexer(c);
 
             if (c == '>')
             {
                 break;
             }
         }
 
         this.lexsize -= 2;
         this.txtend = this.lexsize;
 
         if (this.txtend > this.txtstart)
         {
             php = newNode(NodeType.PhpTag, this.lexbuf, this.txtstart, this.txtend);
         }
 
         this.txtstart = this.txtend;
         return php;
     }
 
     /**
      * consumes the '>' terminating start tags.
      * @param isempty flag is passed as array so it can be modified
      * @param asp asp Node, passed as array so it can be modified
      * @param php php Node, passed as array so it can be modified
      * @return parsed attribute
      */
     public String parseAttribute(boolean[] isempty, Node[] asp, Node[] php)
     {
         int start = 0;
         String attr;
         int c = 0;
         int lastc = 0;
 
         asp[0] = null; // clear asp pointer
         php[0] = null; // clear php pointer
         // skip white space before the attribute
 
         for (;;)
         {
             c = this.in.readChar();
 
             if (c == '/')
             {
                 c = this.in.readChar();
 
                 if (c == '>')
                 {
                     isempty[0] = true;
                     return null;
                 }
 
                 this.in.ungetChar(c);
                 c = '/';
                 break;
             }
 
             if (c == '>')
             {
                 return null;
             }
 
             if (c == '<')
             {
                 c = this.in.readChar();
 
                 if (c == '%')
                 {
                     asp[0] = parseAsp();
                     return null;
                 }
                 else if (c == '?')
                 {
                     php[0] = parsePhp();
                     return null;
                 }
 
                 this.in.ungetChar(c);
                 if (this.state != LEX_XMLDECL) // FG fix for 532535
                 {
                     this.in.ungetChar('<'); // fix for 433360
                 }
                 report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_GT);
                 return null;
             }
 
             if (c == '=')
             {
                 report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_EQUALSIGN);
                 continue;
             }
 
             if (c == '"' || c == '\'')
             {
                 report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_QUOTEMARK);
                 continue;
             }
 
             if (c == StreamIn.END_OF_STREAM)
             {
                 report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_END_OF_FILE);
                 this.in.ungetChar(c);
                 return null;
             }
 
             if (!TidyUtils.isWhite((char) c))
             {
                 break;
             }
         }
 
         start = this.lexsize;
         lastc = c;
 
         for (;;)
         {
             // but push back '=' for parseValue()
             if (c == '=' || c == '>')
             {
                 this.in.ungetChar(c);
                 break;
             }
 
             if (c == '<' || c == StreamIn.END_OF_STREAM)
             {
                 this.in.ungetChar(c);
                 break;
             }
             if (lastc == '-' && (c == '"' || c == '\''))
             {
                 this.lexsize--;
                 this.in.ungetChar(c);
                 break;
             }
             if (TidyUtils.isWhite((char) c))
             {
                 break;
             }
 
             // what should be done about non-namechar characters?
             // currently these are incorporated into the attr name
 
             if (!this.configuration.isXmlTags() && TidyUtils.isUpper((char) c))
             {
                 c = TidyUtils.toLower((char) c);
             }
 
             // ++len; #427672 - handle attribute names with multibyte chars - fix by Randy Waki - 10 Aug 00
             addCharToLexer(c);
 
             lastc = c;
             c = this.in.readChar();
         }
 
         // #427672 - handle attribute names with multibyte chars - fix by Randy Waki - 10 Aug 00
         int len = this.lexsize - start;
         attr = (len > 0 ? TidyUtils.getString(this.lexbuf, start, len) : null);
         this.lexsize = start;
 
         return attr;
     }
 
     /**
      * Invoked when &lt; is seen in place of attribute value but terminates on whitespace if not ASP, PHP or Tango this
      * routine recognizes ' and " quoted strings.
      * @return delimiter
      */
     public int parseServerInstruction()
     {
         int c, delim = '"';
         boolean isrule = false;
 
         c = this.in.readChar();
         addCharToLexer(c);
 
         // check for ASP, PHP or Tango
         if (c == '%' || c == '?' || c == '@')
         {
             isrule = true;
         }
 
         for (;;)
         {
             c = this.in.readChar();
 
             if (c == StreamIn.END_OF_STREAM)
             {
                 break;
             }
 
             if (c == '>')
             {
                 if (isrule)
                 {
                     addCharToLexer(c);
                 }
                 else
                 {
                     this.in.ungetChar(c);
                 }
 
                 break;
             }
 
             // if not recognized as ASP, PHP or Tango
             // then also finish value on whitespace
             if (!isrule)
             {
                 if (TidyUtils.isWhite((char) c))
                 {
                     break;
                 }
             }
 
             addCharToLexer(c);
 
             if (c == '"')
             {
                 do
                 {
                     c = this.in.readChar();
 
                     if (endOfInput()) // #427840 - fix by Terry Teague 30 Jun 01
                     {
                         report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_END_OF_FILE);
                         this.in.ungetChar(c);
                         return 0;
                     }
                     if (c == '>') // #427840 - fix by Terry Teague 30 Jun 01
                     {
                         this.in.ungetChar(c);
                         report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_GT);
                         return 0;
                     }
 
                     addCharToLexer(c);
                 }
                 while (c != '"');
                 delim = '\'';
                 continue;
             }
 
             if (c == '\'')
             {
                 do
                 {
                     c = this.in.readChar();
 
                     if (endOfInput()) // #427840 - fix by Terry Teague 30 Jun 01
                     {
                         report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_END_OF_FILE);
                         this.in.ungetChar(c);
                         return 0;
                     }
                     if (c == '>') // #427840 - fix by Terry Teague 30 Jun 01
                     {
                         this.in.ungetChar(c);
                         report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_GT);
                         return 0;
                     }
 
                     addCharToLexer(c);
                 }
                 while (c != '\'');
             }
         }
 
         return delim;
     }
 
     /**
      * Parse an attribute value.
      * @param name attribute name
      * @param foldCase fold case?
      * @param isempty is attribute empty? Passed as an array reference to allow modification
      * @param pdelim delimiter, passed as an array reference to allow modification
      * @return parsed value
      */
     public String parseValue(String name, boolean foldCase, boolean[] isempty, int[] pdelim)
     {
         // values start with "=" or " = " etc.
         // doesn't consume the ">" at end of start tag
 
         int len = 0;
         int start;
         boolean seenGt = false;
         boolean munge = true;
         int c = 0;
         int lastc, delim, quotewarning;
         String value;
 
         delim = 0;
         pdelim[0] = '"';
 
         // Henry Zrepa reports that some folk are using the embed element with script attributes where newlines are
         // significant and must be preserved
 
         if (this.configuration.isLiteralAttribs())
         {
             munge = false;
         }
 
         // skip white space before the '='
         while (true)
         {
             c = this.in.readChar();
 
             if (c == StreamIn.END_OF_STREAM)
             {
                 this.in.ungetChar(c);
                 break;
             }
 
             if (!TidyUtils.isWhite((char) c))
             {
                 break;
             }
         }
 
         // c should be '=' if there is a value other legal possibilities are white space, '/' and '>'
 
         if (c != '=' && c != '"' && c != '\'')
         {
             this.in.ungetChar(c);
             return null;
         }
 
         // skip white space after '='
 
         while (true)
         {
             c = this.in.readChar();
 
             if (c == StreamIn.END_OF_STREAM)
             {
                 this.in.ungetChar(c);
                 break;
             }
 
             if (!TidyUtils.isWhite((char) c))
             {
                 break;
             }
         }
 
         // check for quote marks
 
         if (c == '"' || c == '\'')
         {
             delim = c;
         }
         else if (c == '<')
         {
             start = this.lexsize;
             addCharToLexer(c);
             pdelim[0] = parseServerInstruction();
             len = this.lexsize - start;
             this.lexsize = start;
             return (len > 0 ? TidyUtils.getString(this.lexbuf, start, len) : null);
         }
         else
         {
             this.in.ungetChar(c);
         }
 
         // and read the value string check for quote mark if needed
 
         quotewarning = 0;
         start = this.lexsize;
         c = '\0';
 
         while (true)
         {
             lastc = c; // track last character
             c = this.in.readChar();
 
             if (c == StreamIn.END_OF_STREAM)
             {
                 report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_END_OF_FILE);
                 this.in.ungetChar(c);
                 break;
             }
 
             if (delim == (char) 0)
             {
                 if (c == '>')
                 {
                     this.in.ungetChar(c);
                     break;
                 }
 
                 if (c == '"' || c == '\'')
                 {
                     int q = c;
                     report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_QUOTEMARK);
                     
                     /* handle <input onclick=s("btn1")> and <a title=foo""">...</a> */
                     /* this doesn't handle <a title=foo"/> which browsers treat as  */
                     /* 'foo"/' nor  <a title=foo" /> which browser treat as 'foo"'  */
                     
                     c = in.readChar();
                     if (c == '>') {
                     	addCharToLexer(q);
                     	in.ungetChar(c);
                         break;
                     } else {
                     	in.ungetChar(c);
                         c = q;
                     }
                 }
 
                 if (c == '<')
                 {
                     this.in.ungetChar(c); // fix for 433360
                     c = '>';
                     this.in.ungetChar(c);
                     report.attrError(this, this.token, null, ErrorCode.UNEXPECTED_GT);
                     break;
                 }
 
                 // For cases like <br clear=all/> need to avoid treating /> as part of the attribute value, however
                 // care is needed to avoid so treating <a href=http://www.acme.com /> in this way, which would map the
                 // <a> tag to <a href="http://www.acme.com"/>
 
                 if (c == '/')
                 {
                     // peek ahead in case of />
                     c = this.in.readChar();
 
                     if (c == '>' && !AttributeTable.getDefaultAttributeTable().isUrl(name))
                     {
                         isempty[0] = true;
                         this.in.ungetChar(c);
                         break;
                     }
 
                     // unget peeked char
                     this.in.ungetChar(c);
                     c = '/';
                 }
             }
             else
             {
                 // delim is '\'' or '"'
                 if (c == delim)
                 {
                     break;
                 }
 
                 // treat CRLF, CR and LF as single line break
 
                 if (c == '\r')
                 {
                     c = this.in.readChar();
                     if (c != '\n')
                     {
                         this.in.ungetChar(c);
                     }
 
                     c = '\n';
                 }
 
                 if (c == '\n' || c == '<' || c == '>')
                 {
                     ++quotewarning;
                 }
 
                 if (c == '>')
                 {
                     seenGt = true;
                 }
             }
 
             if (c == '&')
             {
                 // no entities in ID attributes
                 if ("id".equalsIgnoreCase(name))
                 {
                     report.attrError(this, null, null, ErrorCode.ENTITY_IN_ID);
                     continue;
                 }
 
                 addCharToLexer(c);
                 parseEntity((short) 0);
                 continue;
 
             }
 
             // kludge for JavaScript attribute values with line continuations in string literals
 
             if (c == '\\')
             {
                 c = this.in.readChar();
 
                 if (c != '\n')
                 {
                     this.in.ungetChar(c);
                     c = '\\';
                 }
             }
 
             if (TidyUtils.isWhite((char) c))
             {
                 if (delim == (char) 0)
                 {
                     break;
                 }
 
                 if (munge)
                 {
                     // discard line breaks in quoted URLs
                     // #438650 - fix by Randy Waki
                     if (c == '\n' && AttributeTable.getDefaultAttributeTable().isUrl(name))
                     {
                         // warn that we discard this newline
                         report.attrError(this, this.token, null, ErrorCode.NEWLINE_IN_URI);
                         continue;
                     }
 
                     c = ' ';
 
                     if (lastc == ' ')
                     {
                     	if (AttributeTable.getDefaultAttributeTable().isUrl(name)) {
                             report.attrError(this, token, null, ErrorCode.WHITE_IN_URI);
                     	}
                         continue;
                     }
                 }
             }
             else if (foldCase && TidyUtils.isUpper((char) c))
             {
                 c = TidyUtils.toLower((char) c);
             }
 
             addCharToLexer(c);
         }
 
         if (quotewarning > 10 && seenGt && munge)
         {
             // there is almost certainly a missing trailing quote mark as we have see too many newlines, < or >
             // characters. an exception is made for Javascript attributes and the javascript URL scheme which may
             // legitimately include < and >, and for attributes starting with "<xml " as generated by Microsoft Office.
 
             if (!AttributeTable.getDefaultAttributeTable().isScript(name)
                 && !(AttributeTable.getDefaultAttributeTable().isUrl(name) && "javascript:".equals(TidyUtils.getString(
                     this.lexbuf,
                     start,
                     11)))
                 && !"<xml ".equals(TidyUtils.getString(this.lexbuf, start, 5))) // #500236 - fix by Klaus Johannes Rusch
             // 06 Jan 02
             {
                 report.error(this, null, null, ErrorCode.SUSPECTED_MISSING_QUOTE);
             }
         }
 
         len = this.lexsize - start;
         this.lexsize = start;
 
         if (len > 0 || delim != 0)
         {
             // ignore leading and trailing white space for all but title, alt, value and prompts attributes unless
             // --literal-attributes is set to yes
             // #994841 - Whitespace is removed from value attributes
 
             if (munge && !TidyUtils.isInValuesIgnoreCase(new String[]{"alt", "title", "value", "prompt"}, name))
             {
                 while (TidyUtils.isWhite((char) this.lexbuf[start + len - 1]))
                 {
                     --len;
                 }
 
                 while (TidyUtils.isWhite((char) this.lexbuf[start]) && start < len)
                 {
                     ++start;
                     --len;
                 }
             }
 
             value = TidyUtils.getString(this.lexbuf, start, len);
         }
         else
         {
             value = null;
         }
 
         // note delimiter if given
         if (delim != 0)
         {
             pdelim[0] = delim;
         }
         else
         {
             pdelim[0] = '"';
         }
 
         return value;
     }
 
     /**
      * Check if attr is a valid name.
      * @param attr String to check, must be non-null
      * @return <code>true</code> if attr is a valid name.
      */
     public static boolean isValidAttrName(String attr)
     {
         char c;
         int i;
 
         // first character should be a letter
         c = attr.charAt(0);
 
         if (!TidyUtils.isLetter(c))
         {
             return false;
         }
 
         // remaining characters should be namechars
         for (i = 1; i < attr.length(); i++)
         {
             c = attr.charAt(i);
 
             if (TidyUtils.isNamechar(c))
             {
                 continue;
             }
 
             return false;
         }
 
         return true;
     }
 
     /**
      * In CSS1, selectors can contain only the characters A-Z, 0-9, and Unicode characters 161-255, plus dash (-); they
      * cannot start with a dash or a digit; they can also contain escaped characters and any Unicode character as a
      * numeric code (see next item). The backslash followed by at most four hexadecimal digits (0..9A..F) stands for the
      * Unicode character with that number. Any character except a hexadecimal digit can be escaped to remove its special
      * meaning, by putting a backslash in front.
      * @param buf css selector name
      * @return <code>true</code> if the given string is a valid css1 selector name
      */
     public static boolean isCSS1Selector(String buf)
     {
         if (buf == null)
         {
             return false;
         }
 
         // #508936 - CSS class naming for -clean option
         boolean valid = true;
         int esclen = 0;
         char c;
         int pos;
 
         for (pos = 0; valid && pos < buf.length(); ++pos)
         {
             c = buf.charAt(pos);
             if (c == '\\')
             {
                 esclen = 1; // ab\555\444 is 4 chars {'a', 'b', \555, \444}
             }
             else if (Character.isDigit(c))
             {
                 // Digit not 1st, unless escaped (Max length "\112F")
                 if (esclen > 0)
                 {
                     valid = (++esclen < 6);
                 }
                 if (valid)
                 {
                     valid = (pos > 0 || esclen > 0);
                 }
             }
             else
             {
                 valid = (esclen > 0 // Escaped? Anything goes.
                     || (pos > 0 && c == '-') // Dash cannot be 1st char
                     || Character.isLetter(c) // a-z, A-Z anywhere
                 || (c >= 161 && c <= 255)); // Unicode 161-255 anywhere
                 esclen = 0;
             }
         }
         return valid;
     }
 
     /**
      * Parse tag attributes.
      * @param isempty is tag empty?
      * @return parsed attribute/value list
      */
     public AttVal parseAttrs(boolean[] isempty)
     {
         AttVal av, list;
         String attribute, value;
         int[] delim = new int[1];
         Node[] asp = new Node[1];
         Node[] php = new Node[1];
 
         list = null;
 
         while (!endOfInput())
         {
             attribute = parseAttribute(isempty, asp, php);
 
             if (attribute == null)
             {
                 // check if attributes are created by ASP markup
                 if (asp[0] != null)
                 {
                     av = new AttVal(list, null, asp[0], null, '\0', null, null);
                     list = av;
                     continue;
                 }
 
                 // check if attributes are created by PHP markup
                 if (php[0] != null)
                 {
                     av = new AttVal(list, null, null, php[0], '\0', null, null);
                     list = av;
                     continue;
                 }
 
                 break;
             }
 
             value = parseValue(attribute, false, isempty, delim);
 
             if (attribute != null && isValidAttrName(attribute))
             {
                 av = new AttVal(null, null, null, null, delim[0], attribute, value);
                 av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
                 list = AttVal.addAttrToList(list, av);
             }
             else
             {
                 av = new AttVal(null, null, null, null, 0, attribute, value);
 
                 if (TidyUtils.lastChar(attribute) == '"') {
                     report.attrError(this, this.token, av, ErrorCode.MISSING_QUOTEMARK);
                 } else if (value == null) {
                 	report.attrError(this, this.token, av, ErrorCode.MISSING_ATTR_VALUE);
                 } else {
                 	report.attrError(this, this.token, av, ErrorCode.INVALID_ATTRIBUTE);
                 }
             }
         }
 
         return list;
     }
 
     /**
      * Push a copy of an inline node onto stack but don't push if implicit or OBJECT or APPLET (implicit tags are ones
      * generated from the istack) One issue arises with pushing inlines when the tag is already pushed. For instance:
      * <code>&lt;p>&lt;em> text &lt;p>&lt;em> more text</code> Shouldn't be mapped to
      * <code>&lt;p>&lt;em> text &lt;/em>&lt;/p>&lt;p>&lt;em>&lt;em> more text &lt;/em>&lt;/em></code>
      * @param node Node to be pushed
      */
     public void pushInline(Node node)
     {
         IStack is;
 
         if (node.implicit)
         {
             return;
         }
 
         if (node.tag == null)
         {
             return;
         }
 
         if (!TidyUtils.toBoolean(node.tag.model & Dict.CM_INLINE))
         {
             return;
         }
 
         if (TidyUtils.toBoolean(node.tag.model & Dict.CM_OBJECT))
         {
             return;
         }
 
         if (!node.is(TagId.FONT) && isPushed(node))
         {
             return;
         }
 
         // make sure there is enough space for the stack
         is = new IStack();
         is.tag = node.tag;
         is.element = node.element;
         if (node.attributes != null)
         {
             is.attributes = cloneAttributes(node.attributes);
         }
         this.istack.push(is);
     }
 
     /**
      * Pop a copy of an inline node from the stack.
      * @param node Node to be popped
      */
     public void popInline(Node node)
     {
         IStack is;
 
         if (node != null)
         {
 
             if (node.tag == null)
             {
                 return;
             }
 
             if (!TidyUtils.toBoolean(node.tag.model & Dict.CM_INLINE))
             {
                 return;
             }
 
             if (TidyUtils.toBoolean(node.tag.model & Dict.CM_OBJECT))
             {
                 return;
             }
 
             // if node is </a> then pop until we find an <a>
             if (node.is(TagId.A))
             {
 
                 while (this.istack.size() > 0)
                 {
                     is = this.istack.pop();
                     if (is.tag.id == TagId.A)
                     {
                         break;
                     }
                 }
 
                 if (this.insert >= this.istack.size())
                 {
                     this.insert = -1;
                 }
                 return;
             }
         }
 
         if (this.istack.size() > 0)
         {
             is = this.istack.pop();
             if (this.insert >= this.istack.size())
             {
                 this.insert = -1;
             }
         }
     }
 
     /**
      * Is the node in the stack?
      * @param node Node
      * @return <code>true</code> is the node is found in the stack
      */
     public boolean isPushed(Node node)
     {
         int i;
         IStack is;
 
         for (i = this.istack.size() - 1; i >= 0; --i)
         {
             is = this.istack.elementAt(i);
             if (is.tag == node.tag)
             {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * This has the effect of inserting "missing" inline elements around the contents of blocklevel elements such as P,
      * TD, TH, DIV, PRE etc. This procedure is called at the start of ParseBlock. When the inline stack is not empty, as
      * will be the case in: <code>&lt;i>&lt;h1>italic heading&lt;/h1>&lt;/i></code> which is then treated as
      * equivalent to <code>&lt;h1>&lt;i>italic heading&lt;/i>&lt;/h1></code> This is implemented by setting the lexer
      * into a mode where it gets tokens from the inline stack rather than from the input stream.
      * @param node original node
      * @return stack size
      */
     public int inlineDup(Node node)
     {
         int n;
 
         n = this.istack.size() - this.istackbase;
         if (n > 0)
         {
             this.insert = this.istackbase;
             this.inode = node;
         }
 
         return n;
     }
 
     /**
      * @return
      */
     public Node insertedToken()
     {
         Node node;
         IStack is;
         int n;
 
         // this will only be null if inode != null
         if (this.insert == -1)
         {
             node = this.inode;
             this.inode = null;
             return node;
         }
 
         // is this is the "latest" node then update the position, otherwise use current values
         if (this.inode == null)
         {
             this.lines = this.in.getCurline();
             this.columns = this.in.getCurcol();
         }
 
         node = newNode(NodeType.StartTag, this.lexbuf, this.txtstart, this.txtend);
 
         // GLP: Bugfix 126261. Remove when this change is fixed in istack.c in the original Tidy
         node.implicit = true;
         is = this.istack.elementAt(this.insert);
         node.element = is.element;
         node.tag = is.tag;
         if (is.attributes != null)
         {
             node.attributes = cloneAttributes(is.attributes);
         }
 
         // advance lexer to next item on the stack
         n = this.insert;
 
         // and recover state if we have reached the end
         if (++n < this.istack.size())
         {
             this.insert = n;
         }
         else
         {
             this.insert = -1;
         }
 
         return node;
     }
 
     /**
      * Can the given element be removed?
      * @param element node
      * @return <code>true</code> if he element can be removed
      */
     public boolean canPrune(final Node element) {
         if (element.type == NodeType.TextNode) {
             return true;
         }
         if (element.content != null) {
             return false;
         }
         if (element.hasCM(Dict.CM_BLOCK) && element.attributes != null) {
             return false;
         }
         if (element.is(TagId.A) && element.attributes != null) {
             return false;
         }
         if (element.is(TagId.P) && !this.configuration.isDropEmptyParas()) {
             return false;
         }
         if (element.hasCM(Dict.CM_ROW)) {
             return false;
         }
         if (element.hasCM(Dict.CM_EMPTY)) {
             return false;
         }
         if (element.is(TagId.APPLET)) {
             return false;
         }
         if (element.is(TagId.OBJECT)) {
             return false;
         }
         if (element.is(TagId.SCRIPT) && element.getAttrById(AttrId.SRC) != null) {
             return false;
         }
         if (element.is(TagId.TITLE)) {
             return false;
         }
         // #433359 - fix by Randy Waki 12 Mar 01 - Empty iframe is trimmed
         if (element.is(TagId.IFRAME)) {
             return false;
         }
         /* fix for bug 770297 */
         if (element.is(TagId.TEXTAREA)) {
             return false;
         }
         if (element.getAttrById(AttrId.ID) != null || element.getAttrById(AttrId.NAME) != null) {
             return false;
         }
         /* fix for bug 695408; a better fix would look for unknown and    */
         /* known proprietary attributes that make the element significant */
         if (element.getAttrById(AttrId.DATAFLD) != null) {
             return false;
         }
         /* fix for bug 723772, don't trim new-...-tags */
         if (element.tag.id == TagId.UNKNOWN) {
             return false;
         }
         if (element.is(TagId.BODY)) {
             return false;
         }
         if (element.is(TagId.COLGROUP)) {
             return false;
         }
         return true;
     }
 
     /**
      * duplicate name attribute as an id and check if id and name match.
      * @param node Node to check for name/it attributes
      */
     public void fixId(Node node)
     {
         AttVal name = node.getAttrByName("name");
         AttVal id = node.getAttrByName("id");
 
         if (name != null)
         {
             if (id != null)
             {
                 if (id.value != null && !id.value.equals(name.value))
                 {
                     report.attrError(this, node, name, ErrorCode.ID_NAME_MISMATCH);
                 }
             }
             else if (this.configuration.isXmlOut())
             {
                 node.addAttribute("id", name.value);
             }
         }
     }
 
     /**
      * Defer duplicates when entering a table or other element where the inlines shouldn't be duplicated.
      */
     public void deferDup()
     {
         this.insert = -1;
         this.inode = null;
     }
 
     /**
      * Constraint the html version in the document to the given one. Everything is allowed in proprietary version of
      * HTML this is handled here rather than in the tag/attr dicts.
      * @param vers html version code
      */
     void constrainVersion(int vers)
     {
         this.versions &= (vers | VERS_PROPRIETARY);
     }
 
     /**
      * Is content acceptable for pre elements?
      * @param node content
      * @return <code>true</code> if node is acceptable in pre elements
      */
     protected boolean preContent(Node node)
     {
         // p is coerced to br's
         if (node.is(TagId.P))
         {
             return true;
         }
 
         if (node.tag == null
             || node.is(TagId.P)
             || !TidyUtils.toBoolean(node.tag.model & (Dict.CM_INLINE | Dict.CM_NEW)))
         {
             return false;
         }
         return true;
     }
 
     /**
      * document type.
      */
     private static class W3CDoctype {
     	int score;
     	int vers;
         String name;
         String fpi;
         String si;
         
 		public W3CDoctype(final int score, final int vers, final String name, final String fpi, final String si) {
 			this.score = score;
 			this.vers = vers;
 			this.name = name;
 			this.fpi = fpi;
 			this.si = si;
 		}
     }
     
     private enum ParseDocTypeDeclState {
       DT_INTERMEDIATE,
       DT_DOCTYPENAME,
       DT_PUBLICSYSTEM,
       DT_QUOTEDSTRING,
       DT_INTSUBSET
     };
     
     /*
     Returns document type declarations like
 
     <!DOCTYPE foo PUBLIC "fpi" "sysid">
     <!DOCTYPE bar SYSTEM "sysid">
     <!DOCTYPE baz [ <!ENTITY ouml "&#246"> ]>
 
     as
 
     <foo PUBLIC="fpi" SYSTEM="sysid" />
     <bar SYSTEM="sysid" />
     <baz> &lt;!ENTITY ouml &quot;&amp;#246&quot;&gt; </baz>
   */
     private Node parseDocTypeDecl() {
         int start = lexsize;
         ParseDocTypeDeclState state = ParseDocTypeDeclState.DT_DOCTYPENAME;
         int c;
         int delim = 0;
         boolean hasfpi = true;
 
         Node node = newNode(NodeType.DocTypeTag, lexbuf, txtstart, txtend);
 
         waswhite = false;
 
         /* todo: reset lexsize when appropriate to avoid wasting memory */
 
         while ((c = in.readChar()) != StreamIn.END_OF_STREAM) {
             /* convert newlines to spaces */
             if (state != ParseDocTypeDeclState.DT_INTSUBSET) {
                 c = c == '\n' ? ' ' : c;
             }
 
             /* convert white-space sequences to single space character */
             if (TidyUtils.isWhite((char) c) && state != ParseDocTypeDeclState.DT_INTSUBSET) {
                 if (!waswhite) {
                 	addCharToLexer(c);
                     waswhite = true;
                 } else {
                     /* discard space */
                     continue;
                 }
             } else {
                 addCharToLexer(c);
                 waswhite = false;
             }
 
             switch(state) {
             case DT_INTERMEDIATE:
                 /* determine what's next */
                 if (TidyUtils.toUpper((char) c) == 'P' || TidyUtils.toUpper((char) c) == 'S') {
                     start = lexsize - 1;
                     state = ParseDocTypeDeclState.DT_PUBLICSYSTEM;
                     continue;
                 }
                 else if (c == '[') {
                     start = lexsize;
                     state = ParseDocTypeDeclState.DT_INTSUBSET;
                     continue;
                 }
                 else if (c == '\'' || c == '"') {
                     start = lexsize;
                     delim = c;
                     state = ParseDocTypeDeclState.DT_QUOTEDSTRING;
                     continue;
                 }
                 else if (c == '>') {
                     AttVal si;
 
                     node.end = --(lexsize);
 
                     si = node.getAttrByName("SYSTEM");
                     if (si != null) {
                     	AttrCheckImpl.URL.check(this, node, si);
                     }
                     if (node.element == null || !TidyUtils.isValidXMLID(node.element)) {
                        report.error(this, null, null, ErrorCode.MALFORMED_DOCTYPE);
                         return null;
                     }
 //    #ifdef TIDY_STORE_ORIGINAL_TEXT
 //                    StoreOriginalTextInToken(doc, node, 0);
 //    #endif
                     return node;
                 }
                 else {
                     /* error */
                 }
                 break;
             case DT_DOCTYPENAME:
                 /* read document type name */
                 if (TidyUtils.isWhite((char) c) || c == '>' || c == '[') {
                     node.element = TidyUtils.getString(lexbuf, start, lexsize - start - 1);
                     if (c == '>' || c == '[') {
                         --(lexsize);
                         in.ungetChar(c);
                     }
 
                     state = ParseDocTypeDeclState.DT_INTERMEDIATE;
                     continue;
                 }
                 break;
             case DT_PUBLICSYSTEM:
                 /* read PUBLIC/SYSTEM */
                 if (TidyUtils.isWhite((char) c) || c == '>') {
                     String attname = TidyUtils.getString(lexbuf, start, lexsize - start - 1);
                     hasfpi = !attname.equalsIgnoreCase("SYSTEM");
 
                     /* todo: report an error if SYSTEM/PUBLIC not uppercase */
 
                     if (c == '>') {
                         --(lexsize);
                         in.ungetChar(c);
                     }
 
                     state = ParseDocTypeDeclState.DT_INTERMEDIATE;
                     continue;
                 }
                 break;
             case DT_QUOTEDSTRING:
                 /* read quoted string */
                 if (c == delim) {
                     String value = TidyUtils.getString(lexbuf, start, lexsize - start - 1);
                     AttVal att = node.addAttribute(hasfpi ? "PUBLIC" : "SYSTEM", value);
                     att.delim = delim;
                     hasfpi = false;
                     state = ParseDocTypeDeclState.DT_INTERMEDIATE;
                     delim = 0;
                     continue;
                 }
                 break;
             case DT_INTSUBSET:
                 /* read internal subset */
                 if (c == ']') {
                     Node subset;
                     txtstart = start;
                     txtend = lexsize - 1;
                     subset = textToken();
                     node.insertNodeAtEnd(subset);
                     state = ParseDocTypeDeclState.DT_INTERMEDIATE;
                 }
                 break;
             }
         }
 
         /* document type declaration not finished */
        report.error(this, null, null, ErrorCode.MALFORMED_DOCTYPE);
         return null;
     }
 
 	protected Node findXmlDecl() {
 		Node node = root.content;
 	    while (node != null && node.type != NodeType.XmlDecl) {
 	    	node = node.next;
 	    }
 	    return node;
 	}
 	
 	protected boolean textNodeEndWithSpace(final Node node) {
         if (node.isText() && node.end > node.start) {
             int i, c = '\0'; /* initialised to avoid warnings */
             for (i = node.start; i < node.end; ++i) {
                 c = lexbuf[i] & 0xFF; // Convert to unsigned.
             }
 
             if (c == ' ' || c == '\n') {
                 return true;
             }
         }
         return false;
     }
 	
 	/*
 	   We have two CM_INLINE elements pushed ... the first is closing,
 	   but, like the browser, the second should be retained ...
 	   Like <b>bold <i>bold and italics</b> italics only</i>
 	   This function switches the tag positions on the stack,
 	   returning true if both were found in the expected order.
 	*/
 	protected boolean switchInline(final Node element, final Node node) {
 	    if (element != null && element.tag != null
 	    		&& node != null && node.tag != null
 	    		&& isPushed(element) && isPushed(node)
 	    		&& istack.size() - istackbase >= 2) {
 	        /* we have a chance of succeeding ... */
 	        for (int i = (istack.size() - istackbase - 1); i >= 0; --i) {
 	            if (istack.get(i).tag == element.tag) {
 	                /* found the element tag - phew */
 	                int istack1 = i;
 	                int istack2 = -1;
 	                --i; /* back one more, and continue */
 	                for ( ; i >= 0; --i) {
 	                    if (istack.get(i).tag == node.tag) {
 	                        /* found the element tag - phew */
 	                        istack2 = i;
 	                        break;
 	                    }
 	                }
 	                if (istack2 >= 0) {
 	                    /* perform the swap */
 	                    IStack tmp = istack.get(istack2);
 	                    istack.set(istack2, istack.get(istack1));
 	                    istack.set(istack1, tmp);
 	                    return true;
 	                }
 	            }
 	        }
 	    }
 	    return false;
 	}
 
 	/*
 	  We want to push a specific a specific element on the stack,
 	  but it may not be the last element, which InlineDup()
 	  would handle. Return true if found and inserted.
 	*/
 	protected boolean inlineDup1(final Node node, final Node element) {
 		int n;
 		if (element != null && element.tag != null
 				&& ((n = istack.size() - istackbase) > 0)) {
 			for (int i = n - 1; i >=0; --i ) {
 				if (istack.get(i).tag == element.tag) {
 					/* found our element tag - insert it */
 					insert = i;
 					inode = node;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	protected void addClassNoIndent(final Node node) {
 		final String sprop = "padding-left: 2ex; margin-left: 0ex; margin-top: 0ex; margin-bottom: 0ex";
 		if (!configuration.isDecorateInferredUL()) {
 			return;
 		}
 		if (configuration.isMakeClean()) {
 			Clean.addStyleAsClass(this, node, sprop);
 		}
 		else {
 			Clean.addStyleProperty(node, sprop);
 		}
 	}
 	
 	/**
      * Generates a new css class name.
      * @param lexer Lexer
      * @return generated css class
      */
     protected String gensymClass() {
         String pfx = configuration.getCssPrefix();
         if (pfx == null) {
         	pfx = "c";
         }
         return pfx + ++classNum;
     }
 }
