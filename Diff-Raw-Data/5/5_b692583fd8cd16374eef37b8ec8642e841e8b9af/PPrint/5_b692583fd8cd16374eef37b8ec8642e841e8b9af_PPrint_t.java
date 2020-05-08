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
 
 import org.w3c.tidy.Node.NodeType;
 import org.w3c.tidy.Options.TriState;
 
 /**
  * Pretty print parse tree. Block-level and unknown elements are printed on new lines and their contents indented 2
  * spaces Inline elements are printed inline. Inline content is wrapped on spaces (except in attribute values or
  * preformatted text, after start tags and before end tags.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public class PPrint
 {
 
     /**
      * position: normal.
      */
     private static final short NORMAL = 0;
 
     /**
      * position: preformatted text.
      */
     private static final short PREFORMATTED = 1;
 
     /**
      * position: comment.
      */
     private static final short COMMENT = 2;
 
     /**
      * position: attribute value.
      */
     private static final short ATTRIBVALUE = 4;
 
     /**
      * position: nowrap.
      */
     private static final short NOWRAP = 8;
 
     /**
      * position: cdata.
      */
     private static final short CDATA = 16;
 
     /**
      * Start cdata token.
      */
     private static final String CDATA_START = "<![CDATA[";
 
     /**
      * End cdata token.
      */
     private static final String CDATA_END = "]]>";
 
     /**
      * Javascript comment start.
      */
     private static final String JS_COMMENT_START = "//";
 
     /**
      * Javascript comment end.
      */
     private static final String JS_COMMENT_END = "";
 
     /**
      * VB comment start.
      */
     private static final String VB_COMMENT_START = "\'";
 
     /**
      * VB comment end.
      */
     private static final String VB_COMMENT_END = "";
 
     /**
      * CSS comment start.
      */
     private static final String CSS_COMMENT_START = "/*";
 
     /**
      * CSS comment end.
      */
     private static final String CSS_COMMENT_END = "*/";
 
     /**
      * Default comment start.
      */
     private static final String DEFAULT_COMMENT_START = "";
 
     /**
      * Default comment end.
      */
     private static final String DEFAULT_COMMENT_END = "";
 
     private int[] linebuf;
 
     private int lbufsize;
 
     private int linelen;
 
     private int wraphere;
     
     private int ixInd;
 
     static class TidyIndent {
     	int spaces;
         int attrValStart;
         int attrStringStart;
         
         public TidyIndent() {
         	spaces = -1;
         	attrValStart = -1;
         	attrStringStart = -1;
         }
 
 		@Override
 		public String toString() {
 			return "(" + spaces + ", " + attrValStart + ", " + attrStringStart + ")";
 		}
     }
 
     private TidyIndent indent[] = new TidyIndent[2];
 
     /**
      * current configuration.
      */
     private Configuration configuration;
 
     /**
      * Instantiates a new PPrint.
      * @param configuration configuration
      */
     public PPrint(Configuration configuration)
     {
         this.configuration = configuration;
         indent[0] = new TidyIndent();
         indent[1] = new TidyIndent();
     }
 
     /**
      * @param ind
      * @return
      */
     int cWrapLen(int ind)
     {
         /* #431953 - start RJ Wraplen adjusted for smooth international ride */
         if ("zh".equals(this.configuration.getLanguage()))
         {
             // Chinese characters take two positions on a fixed-width screen
             // It would be more accurate to keep a parallel linelen and wraphere incremented by 2 for Chinese characters
             // and 1 otherwise, but this is way simpler.
             return (ind + ((this.configuration.getWraplen() - ind) / 2));
         }
         if ("ja".equals(this.configuration.getLanguage()))
         {
             /* average Japanese text is 30% kanji */
             return (ind + (((this.configuration.getWraplen() - ind) * 7) / 10));
         }
         return (this.configuration.getWraplen());
         /* #431953 - end RJ */
     }
 
     /**
      * return one less than the number of bytes used by the UTF-8 byte sequence. The Unicode char is returned in ch.
      * @param str points to the UTF-8 byte sequence
      * @param start starting offset in str
      * @param ch initialized to 1st byte, passed as an array to allow modification
      * @return one less that the number of bytes used by UTF-8 char
      */
     public static int getUTF8(byte[] str, int start, int[] ch)
     {
 
         int[] n = new int[1];
 
         int[] bytes = new int[]{0};
 
         // first byte "str[0]" is passed in separately from the
         // rest of the UTF-8 byte sequence starting at "str[1]"
         byte[] successorBytes = str;
 
         boolean err = EncodingUtils.decodeUTF8BytesToChar(
             n,
             TidyUtils.toUnsigned(str[start]),
             successorBytes,
             null,
             bytes,
             start + 1);
 
         if (err)
         {
             n[0] = 0xFFFD; // replacement char
         }
         ch[0] = n[0];
         return bytes[0] - 1;
 
     }
 
     /**
      * store char c as UTF-8 encoded byte stream.
      * @param buf
      * @param start
      * @param c
      * @return
      */
     public static int putUTF8(byte[] buf, int start, int c)
     {
         int[] count = new int[]{0};
 
         boolean err = EncodingUtils.encodeCharToUTF8Bytes(c, buf, null, count);
         if (err)
         {
             // replacement char 0xFFFD encoded as UTF-8
             buf[0] = (byte) 0xEF;
             buf[1] = (byte) 0xBF;
             buf[2] = (byte) 0xBD;
             count[0] = 3;
         }
 
         start += count[0];
 
         return start;
     }
     
     private void expand(final int len) {
     	int buflen = lbufsize;
     	if (buflen == 0) {
     		buflen = 256;
     	}
     	while (len >= buflen) {
             buflen *= 2;
     	}
 
         int[] temp = new int[buflen];
         if (temp != null) {
         	if (linebuf != null) {
         		System.arraycopy(linebuf, 0, temp, 0, lbufsize);
         	}
             lbufsize = buflen;
             linebuf = temp;
         }
     }
 
     private void addC(int c, int index) {
         if (index + 1 >= lbufsize) {
         	expand(index + 1);
         }
         linebuf[index] = c;
     }
     
     private int addChar(final int c) {
         addC(c, linelen);
         return ++linelen;
     }
     
     /**
      * Adds an ascii String.
      * @param str String to be added
      * @param index actual line lenght
      * @return final line length
      */
     private int addAsciiString(String str, int index)
     {
         int len = str.length();
         if (index + len >= lbufsize) {
         	expand(index + len);
         }
         for (int ix = 0; ix < len; ++ix) {
             linebuf[index + ix] = str.charAt(ix);
         }
         return index + len;
     }
     
     private int addString(final String str) {
        return linelen = addAsciiString(str, linelen);
     }
     
     /* Saves current output point as the wrap point,
     ** but only if indentation would NOT overflow 
     ** the current line.  Otherwise keep previous wrap point.
     */
     private boolean setWrap(final int indent) {
         boolean wrap = indent + linelen < configuration.getWraplen();
         if (wrap) {
             if (this.indent[0].spaces < 0) {
             	this.indent[0].spaces = indent;
             }
             wraphere = linelen;
         } else if (ixInd == 0) {
             /* Save indent 1st time we pass the the wrap line */
         	this.indent[1].spaces = indent;
             ixInd = 1;
         }
         return wrap;
     }
     
     private boolean isWrapInString() {
         TidyIndent ind = indent[0]; /* Always 1st */
         int wrap = wraphere;
         return ind.attrStringStart == 0 ||
                  (ind.attrStringStart > 0 && ind.attrStringStart < wrap);
     }
     
     private void clearInAttrVal() {
         TidyIndent ind = indent[ixInd];
         ind.attrValStart = -1;
     }
     
     private int setInAttrVal() {
         TidyIndent ind = indent[ixInd];
         return ind.attrValStart = linelen;
     }
     
     private boolean isWrapInAttrVal() {
         TidyIndent ind = indent[0]; /* Always 1st */
         int wrap = wraphere;
         return ind.attrValStart == 0 ||
                  (ind.attrValStart > 0 && ind.attrValStart < wrap);
     }
     
     private boolean wantIndent() {
         boolean wantIt = getSpaces() > 0;
         if (wantIt) {
             boolean indentAttrs = configuration.isIndentAttributes();
             wantIt = (!isWrapInAttrVal() || indentAttrs ) && !isWrapInString();
         }
         return wantIt;
     }
     
     private int wrapOff() {
         final int saveWrap = configuration.getWraplen();
         configuration.setWraplen(0xFFFFFFF); /* very large number */
         return saveWrap;
     }
 
     private void wrapOn(final int saveWrap) {
     	configuration.setWraplen(saveWrap);
     }
     
     private boolean setWrapAttr(final int indent, final int attrStart, final int strStart) {
 		TidyIndent ind = this.indent[0];
 		
 		boolean wrap = indent + linelen < configuration.getWraplen();
 		if (wrap) {
 			if (ind.spaces < 0 ) {
 				ind.spaces = indent;
 			}
 			wraphere = linelen;
 		} else if (ixInd == 0) {
 			/* Save indent 1st time we pass the the wrap line */
 			this.indent[1].spaces = indent;
 			ixInd = 1;
 			
 			/* Carry over string state */
 			if (this.indent[0].attrStringStart > wraphere) {
 				this.indent[1].attrStringStart = this.indent[0].attrStringStart - wraphere;
 				this.indent[0].attrStringStart = -1;
 			}
 			if (this.indent[0].attrValStart > wraphere) {
 				this.indent[1].attrValStart = this.indent[0].attrValStart - wraphere;
 				this.indent[0].attrValStart = -1;
 			}
 		}
 		this.indent[ixInd].attrValStart = attrStart;
 		this.indent[ixInd].attrStringStart = strStart;
 		return wrap;
 	}
     
     /* Reset indent state after flushing a new line
     */
     private void resetLine() {
         if (ixInd > 0 ) {
         	indent[0] = indent[1];
         	indent[1] = new TidyIndent();
         }
 
         if (wraphere > 0) {
             int wrap = wraphere;
             if (indent[0].attrStringStart > wrap) {
             	indent[0].attrStringStart -= wrap;
             }
             if (indent[0].attrValStart > wrap) {
             	indent[0].attrValStart -= wrap;
             }
         } else {
             if (indent[0].attrStringStart > 0) {
             	indent[0].attrStringStart = 0;
             }
             if (indent[0].attrValStart > 0) {
             	indent[0].attrValStart = 0;
             }
         }
         wraphere = ixInd = 0;
     }
     
     /* Shift text after wrap point to
     ** beginning of next line.
     */
     private void resetLineAfterWrap() {
     	if (linelen > wraphere) {
     		int p = 0;
     		int q = p + wraphere;
     		int end = p + linelen;
     		
     		if (!isWrapInAttrVal()) {
                 while (q < end && linebuf[q] == ' ') {
                     ++q;
                     ++wraphere;
                 }
             }
     		while (q < end) {
     			linebuf[p++] = linebuf[q++];
     		}
     		linelen -= wraphere;
         } else {
             linelen = 0;
         }
         resetLine();
     }
 
     /**
      * @param fout
      * @param indent
      */
     private void wrapLine(final Out fout)
     {
         if (wraphere == 0) {
             return;
         }
         if (wantIndent()) {
         	int spaces = getSpaces();
         	for (int i = 0; i < spaces; ++i) {
         		fout.outc(' ');
 			}
         }
         for (int i = 0; i < wraphere; ++i) {
             fout.outc(linebuf[i]);
         }
 
         if (isWrapInString()) {
             fout.outc('\\');
         }
 
         fout.newline();
         
         resetLineAfterWrap();
     }
 
     /**
      * @param fout
      */
     private void wrapAttrVal(final Out fout) {
         int i;
         
         if (wantIndent()) {
         	int spaces = getSpaces();
         	for (i = 0; i < spaces; ++i) {
                 fout.outc(' ');
             }
         }
 
         for (i = 0; i < wraphere; ++i) {
             fout.outc(linebuf[i]);
         }
 
         if (isWrapInString()) {
             fout.outc('\\');
         } else {
         	fout.outc(' ');
         }
 
         fout.newline();
         resetLineAfterWrap();
     }
     
     private int getSpaces() {
         int spaces = indent[0].spaces;
         return spaces < 0 ? 0 : spaces;
     }
     
     /* Checks current output line length along with current indent.
     ** If combined they overflow output line length, go ahead
     ** and flush output up to the current wrap point.
     */
     private boolean checkWrapLine(final Out fout) {
         if (getSpaces() + linelen >= configuration.getWraplen()) {
             wrapLine(fout);
             return true;
         }
         return false;
     }
     
     private boolean checkWrapIndent(final Out fout, final int indent) {
         if (getSpaces() + linelen >= configuration.getWraplen()) {
             wrapLine(fout);
             if (this.indent[0].spaces < 0) {
             	this.indent[0].spaces = indent;
             }
             return true;
         }
         return false;
     }
     
     private int clearInString() {
         TidyIndent ind = indent[ixInd];
         return ind.attrStringStart = -1;
     }
     
     private int toggleInString() {
         TidyIndent ind = indent[ixInd];
         boolean inString = ind.attrStringStart >= 0;
         return ind.attrStringStart = inString ? -1 : linelen;
     }
     
     private boolean isInString() {
         TidyIndent ind = indent[0]; /* Always 1st */
         return ind.attrStringStart >= 0 && 
                  ind.attrStringStart < linelen;
     }
     
     private void flushLineImpl(final Out fout) {
         checkWrapLine(fout);
 
         if (wantIndent()) {
             int spaces = getSpaces();
             for (int i = 0; i < spaces; ++i) {
             	fout.outc(' ');
             }
         }
 
         for (int i = 0; i < linelen; ++i) {
         	fout.outc(linebuf[i]);
         }
         
         if (isInString()) {
         	fout.outc('\\');
         }
         resetLine();
         linelen = 0;
     }
 
     /**
      * @param fout
      * @param indent
      */
     public void flushLine(final Out fout, final int indent) {
         if (linelen > 0) {
         	flushLineImpl(fout);
         }
         fout.newline();
         this.indent[0].spaces = indent;
     }
 
     /**
      * @param fout
      * @param indent
      */
     public void condFlushLine(final Out fout, final int indent) {
         if (linelen > 0) {
         	flushLineImpl(fout);
         	fout.newline();
             this.indent[0].spaces = indent;
         }
     }
 
     /**
      * @param c
      * @param mode
      */
     private void printChar(int c, short mode)
     {
         String entity;
         boolean breakable = false; // #431953 - RJ
 
         if (c == ' ' && !TidyUtils.toBoolean(mode & (PREFORMATTED | COMMENT | ATTRIBVALUE | CDATA)))
         {
             // coerce a space character to a non-breaking space
             if (TidyUtils.toBoolean(mode & NOWRAP))
             {
                 // by default XML doesn't define &nbsp;
                 if (this.configuration.isNumEntities() || this.configuration.isXmlTags())
                 {
                     addC('&', linelen++);
                     addC('#', linelen++);
                     addC('1', linelen++);
                     addC('6', linelen++);
                     addC('0', linelen++);
                     addC(';', linelen++);
                 }
                 else
                 {
                     // otherwise use named entity
                     addC('&', linelen++);
                     addC('n', linelen++);
                     addC('b', linelen++);
                     addC('s', linelen++);
                     addC('p', linelen++);
                     addC(';', linelen++);
                 }
                 return;
             }
             wraphere = linelen;
         }
 
         // comment characters are passed raw
         if (TidyUtils.toBoolean(mode & (COMMENT | CDATA)))
         {
             addC(c, linelen++);
             return;
         }
 
         // except in CDATA map < to &lt; etc.
         if (!TidyUtils.toBoolean(mode & CDATA))
         {
             if (c == '<')
             {
                 addC('&', linelen++);
                 addC('l', linelen++);
                 addC('t', linelen++);
                 addC(';', linelen++);
                 return;
             }
 
             if (c == '>')
             {
                 addC('&', linelen++);
                 addC('g', linelen++);
                 addC('t', linelen++);
                 addC(';', linelen++);
                 return;
             }
 
             // naked '&' chars can be left alone or quoted as &amp;
             // The latter is required for XML where naked '&' are illegal.
             if (c == '&' && this.configuration.isQuoteAmpersand())
             {
                 addC('&', linelen++);
                 addC('a', linelen++);
                 addC('m', linelen++);
                 addC('p', linelen++);
                 addC(';', linelen++);
                 return;
             }
 
             if (c == '"' && this.configuration.isQuoteMarks())
             {
                 addC('&', linelen++);
                 addC('q', linelen++);
                 addC('u', linelen++);
                 addC('o', linelen++);
                 addC('t', linelen++);
                 addC(';', linelen++);
                 return;
             }
 
             if (c == '\'' && this.configuration.isQuoteMarks())
             {
                 addC('&', linelen++);
                 addC('#', linelen++);
                 addC('3', linelen++);
                 addC('9', linelen++);
                 addC(';', linelen++);
                 return;
             }
 
             if (c == 160 && !this.configuration.isRawOut())
             {
                 if (this.configuration.isMakeBare())
                 {
                     addC(' ', linelen++);
                 }
                 else if (this.configuration.isQuoteNbsp())
                 {
                     addC('&', linelen++);
 
                     if (this.configuration.isNumEntities() || this.configuration.isXmlTags())
                     {
                         addC('#', linelen++);
                         addC('1', linelen++);
                         addC('6', linelen++);
                         addC('0', linelen++);
                     }
                     else
                     {
                         addC('n', linelen++);
                         addC('b', linelen++);
                         addC('s', linelen++);
                         addC('p', linelen++);
                     }
 
                     addC(';', linelen++);
                 }
                 else
                 {
                     addC(c, linelen++);
                 }
 
                 return;
             }
         }
 
         // #431953 - start RJ
         // Handle encoding-specific issues
 
         if ("UTF8".equals(this.configuration.getOutCharEncodingName()))
         {
             // Chinese doesn't have spaces, so it needs other kinds of breaks
             // This will also help documents using nice Unicode punctuation
             // But we leave the ASCII range punctuation untouched
 
             // Break after any punctuation or spaces characters
             if ((c >= 0x2000) && !TidyUtils.toBoolean(mode & PREFORMATTED))
             {
                 if (((c >= 0x2000) && (c <= 0x2006))
                     || ((c >= 0x2008) && (c <= 0x2010))
                     || ((c >= 0x2011) && (c <= 0x2046))
                     || ((c >= 0x207D) && (c <= 0x207E))
                     || ((c >= 0x208D) && (c <= 0x208E))
                     || ((c >= 0x2329) && (c <= 0x232A))
                     || ((c >= 0x3001) && (c <= 0x3003))
                     || ((c >= 0x3008) && (c <= 0x3011))
                     || ((c >= 0x3014) && (c <= 0x301F))
                     || ((c >= 0xFD3E) && (c <= 0xFD3F))
                     || ((c >= 0xFE30) && (c <= 0xFE44))
                     || ((c >= 0xFE49) && (c <= 0xFE52))
                     || ((c >= 0xFE54) && (c <= 0xFE61))
                     || ((c >= 0xFE6A) && (c <= 0xFE6B))
                     || ((c >= 0xFF01) && (c <= 0xFF03))
                     || ((c >= 0xFF05) && (c <= 0xFF0A))
                     || ((c >= 0xFF0C) && (c <= 0xFF0F))
                     || ((c >= 0xFF1A) && (c <= 0xFF1B))
                     || ((c >= 0xFF1F) && (c <= 0xFF20))
                     || ((c >= 0xFF3B) && (c <= 0xFF3D))
                     || ((c >= 0xFF61) && (c <= 0xFF65)))
                 {
                    wraphere = linelen + 1;
                     breakable = true;
                 }
                 else
                 {
                     switch (c)
                     {
                         case 0xFE63 :
                         case 0xFE68 :
                         case 0x3030 :
                         case 0x30FB :
                         case 0xFF3F :
                         case 0xFF5B :
                         case 0xFF5D :
                            wraphere = linelen + 1;
                             breakable = true;
                     }
                 }
                 // but break before a left punctuation
                 if (breakable)
                 {
                     if (((c >= 0x201A) && (c <= 0x201C)) || ((c >= 0x201E) && (c <= 0x201F)))
                     {
                         wraphere--;
                     }
                     else
                     {
                         switch (c)
                         {
                             case 0x2018 :
                             case 0x2039 :
                             case 0x2045 :
                             case 0x207D :
                             case 0x208D :
                             case 0x2329 :
                             case 0x3008 :
                             case 0x300A :
                             case 0x300C :
                             case 0x300E :
                             case 0x3010 :
                             case 0x3014 :
                             case 0x3016 :
                             case 0x3018 :
                             case 0x301A :
                             case 0x301D :
                             case 0xFD3E :
                             case 0xFE35 :
                             case 0xFE37 :
                             case 0xFE39 :
                             case 0xFE3B :
                             case 0xFE3D :
                             case 0xFE3F :
                             case 0xFE41 :
                             case 0xFE43 :
                             case 0xFE59 :
                             case 0xFE5B :
                             case 0xFE5D :
                             case 0xFF08 :
                             case 0xFF3B :
                             case 0xFF5B :
                             case 0xFF62 :
                                 wraphere--;
                         }
                     }
                 }
             }
             else if ("BIG5".equals(this.configuration.getOutCharEncodingName()))
             {
                 // Allow linebreak at Chinese punctuation characters
                 // There are not many spaces in Chinese
                 addC(c, linelen++);
                 if (((c & 0xFF00) == 0xA100) && !TidyUtils.toBoolean(mode & PREFORMATTED))
                 {
                     wraphere = linelen;
                     // opening brackets have odd codes: break before them
                     if ((c > 0x5C) && (c < 0xAD) && ((c & 1) == 1))
                     {
                         wraphere--;
                     }
                 }
                 return;
             }
             else if ("SHIFTJIS".equals(this.configuration.getOutCharEncodingName())
                 || "ISO2022".equals(this.configuration.getOutCharEncodingName()))
             {
                 // ISO 2022 characters are passed raw
                 addC(c, linelen++);
                 return;
             }
             else
             {
                 if (this.configuration.isRawOut())
                 {
                     addC(c, linelen++);
                     return;
                 }
             }
             // #431953 - end RJ
         }
 
         // if preformatted text, map &nbsp; to space
         if (c == 160 && TidyUtils.toBoolean(mode & PREFORMATTED))
         {
             addC(' ', linelen++);
             return;
         }
 
         // Filters from Word and PowerPoint often use smart quotes resulting in character codes between 128 and 159.
         // Unfortunately, the corresponding HTML 4.0 entities for these are not widely supported.
         // The following converts dashes and quotation marks to the nearest ASCII equivalent.
         // My thanks to Andrzej Novosiolov for his help with this code.
 
         if (this.configuration.isMakeClean() && this.configuration.isAsciiChars() || this.configuration.isMakeBare())
         {
             if (c >= 0x2013 && c <= 0x201E)
             {
                 switch (c)
                 {
                     case 0x2013 : // en dash
                     case 0x2014 : // em dash
                         c = '-';
                         break;
                     case 0x2018 : // left single quotation mark
                     case 0x2019 : // right single quotation mark
                     case 0x201A : // single low-9 quotation mark
                         c = '\'';
                         break;
                     case 0x201C : // left double quotation mark
                     case 0x201D : // right double quotation mark
                     case 0x201E : // double low-9 quotation mark
                         c = '"';
                         break;
                 }
             }
         }
 
         // don't map latin-1 chars to entities
         if ("ISO8859_1".equals(this.configuration.getOutCharEncodingName()))
         {
             if (c > 255) /* multi byte chars */
             {
                 if (!this.configuration.isNumEntities())
                 {
                     entity = EntityTable.getDefaultEntityTable().entityName((short) c);
                     if (entity != null)
                     {
                         entity = "&" + entity + ";";
                     }
                     else
                     {
                         entity = "&#" + c + ";";
                     }
                 }
                 else
                 {
                     entity = "&#" + c + ";";
                 }
 
                 for (int i = 0; i < entity.length(); i++)
                 {
                     addC(entity.charAt(i), linelen++);
                 }
 
                 return;
             }
 
             if (c > 126 && c < 160)
             {
                 entity = "&#" + c + ";";
 
                 for (int i = 0; i < entity.length(); i++)
                 {
                     addC(entity.charAt(i), linelen++);
                 }
 
                 return;
             }
 
             addC(c, linelen++);
             return;
         }
 
         // don't map utf8 or utf16 chars to entities
         if (this.configuration.getOutCharEncodingName().startsWith("UTF"))
         {
             addC(c, linelen++);
             return;
         }
 
         // use numeric entities only for XML
         if (this.configuration.isXmlTags())
         {
             // if ASCII use numeric entities for chars > 127
             if (c > 127 && "ASCII".equals(this.configuration.getOutCharEncodingName()))
             {
                 entity = "&#" + c + ";";
 
                 for (int i = 0; i < entity.length(); i++)
                 {
                     addC(entity.charAt(i), linelen++);
                 }
 
                 return;
             }
 
             // otherwise output char raw
             addC(c, linelen++);
             return;
         }
 
         // default treatment for ASCII
         if ("ASCII".equals(this.configuration.getOutCharEncodingName()) && (c > 126 || (c < ' ' && c != '\t')))
         {
             if (!this.configuration.isNumEntities())
             {
                 entity = EntityTable.getDefaultEntityTable().entityName((short) c);
                 if (entity != null)
                 {
                     entity = "&" + entity + ";";
                 }
                 else
                 {
                     entity = "&#" + c + ";";
                 }
             }
             else
             {
                 entity = "&#" + c + ";";
             }
 
             for (int i = 0; i < entity.length(); i++)
             {
                 addC(entity.charAt(i), linelen++);
             }
 
             return;
         }
 
         addC(c, linelen++);
     }
     
 	private int incrWS(int start, final int end, final int indent, final int ixWS) {
 		if (ixWS > 0) {
 			int st = start + Math.min(ixWS, indent);
 			start = Math.min(st, end);
 		}
 		return start;
 	}
 
     /**
      * The line buffer is uint not char so we can hold Unicode values unencoded. The translation to UTF-8 is deferred to
      * the outc routine called to flush the line buffer.
      * @param fout
      * @param mode
      * @param indent
      * @param textarray
      * @param start
      * @param end
      */
     private void printText(Lexer lexer, Out fout, short mode, int indent, Node node) {
     	int start = node.start;
     	int end = node.end;
         int ix, c = 0;
         int ixNL = textEndsWithNewline(lexer, node, mode);
         int ixWS = textStartsWithWhitespace(lexer, node, start, mode);
         if (ixNL > 0) {
         	end -= ixNL;
         }
         start = incrWS( start, end, indent, ixWS );
         int[] ci = new int[1];
 
         for (ix = start; ix < end; ++ix) {
         	checkWrapIndent(fout, indent);
         	c = lexer.lexbuf[ix] & 0xFF;
             // look for UTF-8 multibyte character
             if (c > 0x7F) {
                 ix += getUTF8(lexer.lexbuf, ix, ci);
                 c = ci[0];
             }
             if (c == '\n') {
                 flushLine(fout, indent);
                 ixWS = textStartsWithWhitespace(lexer, node, ix + 1, mode );
                 ix = incrWS(ix, end, indent, ixWS);
             } else {
             	printChar(c, mode);
             }
         }
     }
 
     /**
      * @param fout
      * @param indent
      * @param value
      * @param delim
      * @param wrappable
      */
     private void printAttrValue(Out fout, int indent, String value, int delim, boolean wrappable, boolean scriptAttr)
     {
     	boolean scriptlets = configuration.isWrapScriptlets();
         int[] ci = new int[1];
         byte[] valueChars = null;
         int i;
         short mode = (wrappable ? (short) (NORMAL | ATTRIBVALUE) : (short) (PREFORMATTED | ATTRIBVALUE));
 
         if (value != null)
         {
             valueChars = TidyUtils.getBytes(value);
         }
 
         // look for ASP, Tango or PHP instructions for computed attribute value
         if (valueChars != null && valueChars.length >= 5 && valueChars[0] == '<')
         {
             if (valueChars[1] == '%' || valueChars[1] == '@' || (new String(valueChars, 0, 5)).equals("<?php"))
             {
                 mode |= CDATA;
             }
         }
 
         if (delim == 0)
         {
             delim = '"';
         }
 
         addC('=', linelen++);
 
         // don't wrap after "=" for xml documents
         if (!this.configuration.isXmlOut() || configuration.isXHTML()) {
         	setWrap(indent);
             checkWrapIndent(fout, indent);
         }
 
         addC(delim, linelen++);
 
         if (value != null) {
         	int wraplen = configuration.getWraplen();
         	int attrStart = setInAttrVal();
         	int strStart = clearInString();
 
             i = 0;
             while (i < valueChars.length) {
                 int c = (valueChars[i]) & 0xFF; // Convert to unsigned.
 
                 if (wrappable && c == ' ') {
                 	setWrapAttr(indent, attrStart, strStart);
                 }
 
                 if (wrappable && wraphere > 0 && getSpaces() + linelen >= wraplen) {
                     wrapAttrVal(fout);
                 }
 
                 if (c == delim) {
                     String entity = (c == '"' ? "&quot;" : "&#39;");
                     addString(entity);
                     ++i;
                     continue;
                 }
                 else if (c == '"') {
                     if (this.configuration.isQuoteMarks()) {
                     	addString("&quot;");
                     } else {
                         addChar(c);
                     }
 
                     if (delim == '\'' && scriptAttr && scriptlets) {
                         strStart = toggleInString();
                     }
 
                     ++i;
                     continue;
                 }
                 else if (c == '\'') {
                     if (this.configuration.isQuoteMarks()) {
                     	addString("&#39;");
                     } else {
                         addChar(c);
                     }
 
                     if (delim == '"' && scriptAttr && scriptlets) {
                         strStart = toggleInString();
                     }
 
                     ++i;
                     continue;
                 }
 
                 // look for UTF-8 multibyte character
                 if (c > 0x7F) {
                     i += getUTF8(valueChars, i, ci);
                     c = ci[0];
                 }
 
                 ++i;
 
                 if (c == '\n') {
                 	/* No indent inside Javascript literals */
                     flushLine(fout, (strStart < 0 && !configuration.isLiteralAttribs() ?
                                     indent : 0));
                     continue;
                 }
 
                 printChar(c, mode);
             }
             clearInAttrVal();
             clearInString();
         }
         addChar(delim);
     }
     
 	private int attrIndent(Node node) {
 		int spaces = configuration.getSpaces();
 		int xtra = 2; /* 1 for the '<', another for the ' ' */
 		if (node.element == null) {
 			return spaces;
 		}
 		if (!node.hasCM(Dict.CM_INLINE) || !shouldIndent(node.parent != null ? node.parent : node)) {
 			return xtra + node.element.length();
 		}
 		if (null != (node = node.findContainer())) {
 			return xtra + (node.element == null ? 0 : node.element.length());
 		}
 		return spaces;
 	}
 
 	private boolean attrNoIndentFirst(final Node node, final AttVal attr) {
 		return attr == node.attributes;
 	}
 
     /**
      * @param fout
      * @param indent
      * @param node
      * @param attr
      */
     private void printAttribute(Out fout, int indent, Node node, AttVal attr) {
     	boolean xmlOut    = configuration.isXmlOut();
     	boolean xhtmlOut  = configuration.isXHTML();
     	boolean wrapAttrs = configuration.isWrapAttVals();
     	boolean ucAttrs   = configuration.isUpperCaseAttrs();
 	    boolean indAttrs  = configuration.isIndentAttributes();
 	    int xtra      = attrIndent(node);
 	    boolean first     = attrNoIndentFirst(node, attr);
 	    String name    = attr.attribute;
 	    boolean wrappable = false;
 	    
 	    /* fix for odd attribute indentation bug triggered by long values */
 	    if (!indAttrs) {
 	    	xtra = 0;
 	    }
     	
         if (indAttrs) {
         	if (node.isElement() && !first) {
         		indent += xtra;
         		condFlushLine(fout, indent);
         	} else {
         		indAttrs = false;
         	}
         }
         
         checkWrapIndent(fout, indent);
 
         if (!xmlOut && !xhtmlOut && attr.dict != null) {
             if (AttributeTable.getDefaultAttributeTable().isScript(name)) {
                 wrappable = this.configuration.isWrapScriptlets();
             }
             else if (!(attr.is(AttrId.CONTENT) || attr.is(AttrId.VALUE) || attr.is(AttrId.ALT)) && wrapAttrs) {
                 wrappable = true;
             }
         }
 
         if (!first && !setWrap(indent)) {
             flushLine(fout, indent + xtra);  /* Put it on next line */
         }
         else if (linelen > 0) {
             addChar(' ');
         }
 
         /* Attribute name */
         for (int i = 0; i < name.length(); i++) {
             addChar(TidyUtils.foldCase(name.charAt(i), ucAttrs,
             		this.configuration.isXmlTags()));
         }
         
         checkWrapIndent(fout, indent);
 
         if (attr.value == null) {
         	boolean isB = attr.isBoolAttribute();
         	boolean scriptAttr = attr.isEvent();
         	
             if (xmlOut) {
                 printAttrValue(fout, indent, isB ? attr.attribute : "", attr.delim, false, scriptAttr);
             }
             else if (!isB && node != null && !node.isNewNode()) {
                 printAttrValue(fout, indent, "", attr.delim, true, scriptAttr);
             }
             else {
                 setWrap(indent);
             }
         }
         else {
             printAttrValue(fout, indent, attr.value, attr.delim, wrappable, false);
         }
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printAttrs(final Lexer lexer, final Out fout, final int indent, final Node node) {
         // add xml:space attribute to pre and other elements
         if (configuration.isXmlOut() && configuration.isXmlSpace()
         		&& node.getAttrByName("xml:space") == null
         		&& ParserImpl.XMLPreserveWhiteSpace(node, configuration.tt)) {
             node.addAttribute("xml:space", "preserve");
         }
 
         for (AttVal av = node.attributes; av != null; av = av.next) {
             if (av.attribute != null) {
                 printAttribute(fout, indent, node, av);
             }
             else if (av.asp != null) {
                 addChar(' ');
                 printAsp(lexer, fout, indent, av.asp);
             }
             else if (av.php != null) {
                 addChar(' ');
                 printPhp(lexer, fout, indent, av.php);
             }
         }
     }
     
     private static boolean textNodeEndWithSpace(final Lexer lexer, final Node node) {
         if (node.isText() && node.end > node.start) {
             int i, c = '\0'; /* initialised to avoid warnings */
             for (i = node.start; i < node.end; ++i) {
                 c = lexer.lexbuf[i] & 0xFF; // Convert to unsigned.
             }
 
             if (c == ' ' || c == '\n') {
                 return true;
             }
         }
         return false;
     }
 
     /*
     Line can be wrapped immediately after inline start tag provided
     if follows a text node ending in a space, or it follows a <br>,
     or its parent is an inline element that that rule applies to.
     This behaviour was reverse engineered from Netscape 3.0.
 
     Line wrapping can occur if an element is not empty and before a block
     level. For instance:
     <p><span>
     x</span>y</p>
     will display properly. Whereas
     <p><img />
     x</p> won't.
    */
     private static boolean afterSpaceImp(final Lexer lexer, final Node node, final boolean isEmpty) {
         if (!node.hasCM(Dict.CM_INLINE)) {
             return true;
         }
         Node prev = node.prev;
         if (prev != null) {
             if (prev.isText()) {
             	return textNodeEndWithSpace(lexer, prev);
             } else if (prev.is(TagId.BR)) {
             	return true;
             }
             return false;
         }
         if (isEmpty && !node.parent.hasCM(Dict.CM_INLINE)) {
         	return false;
         }
         return afterSpaceImp(lexer, node.parent, isEmpty);
     }
     
     private static boolean afterSpace(final Lexer lexer, final Node node) {
     	return afterSpaceImp(lexer, node, node.hasCM(Dict.CM_EMPTY));
     }
 
     /**
      * @param fout
      * @param mode
      * @param indent
      * @param node
      */
     private void printTag(final Lexer lexer, final Out fout, final short mode, final int indent, final Node node) {
         boolean uc = configuration.isUpperCaseTags();
         boolean xhtmlOut = configuration.isXHTML();
         boolean xmlOut = configuration.isXmlOut();
         String s = node.element;
         
         addChar('<');
 
         if (node.type == NodeType.EndTag) {
             addChar('/');
         }
         
         if (s != null) {
 	        for (int i = 0; i < s.length(); i++) {
 	            addChar(TidyUtils.foldCase(s.charAt(i), uc, this.configuration.isXmlTags()));
 	        }
         }
 
         printAttrs(lexer, fout, indent, node);
 
         if ((xmlOut || xhtmlOut) && (node.type == NodeType.StartEndTag || node.hasCM(Dict.CM_EMPTY))) {
             addChar(' '); // Space is NS compatibility hack <br />
             addChar('/'); // Required end tag marker
         }
 
         addChar('>');
 
         if ((node.type != NodeType.StartEndTag || xhtmlOut) && (mode & PREFORMATTED) == 0) {
         	int wraplen = configuration.getWraplen();
         	checkWrapIndent(fout, indent);
             if (indent + linelen < wraplen) {
             	/* wrap after start tag if is <br/> or if it's not inline.
                 Technically, it would be safe to call only AfterSpace.
                 However, it would disrupt the existing algorithm. So let's
                 leave as is. Note that AfterSpace returns true for non inline
                 elements but can still be false for some <br>. So it has to
                 stay as well. */
             	if ((mode & NOWRAP) == 0 && (!node.hasCM(Dict.CM_INLINE) || node.is(TagId.BR))
             			&& afterSpace(lexer, node)) {
             		wraphere = linelen;
             	}
             }
 	        /* flush the current buffer only if it is known to be safe,
 	        i.e. it will not introduce some spurious white spaces.
 	        See bug #996484 */
 		    else if ((mode & NOWRAP) != 0 || node.is(TagId.BR) || afterSpace(lexer, node)) {
 		         condFlushLine(fout, indent);
 		    }
         }
     }
 
     /**
      * @param mode
      * @param indent
      * @param node
      */
     private void printEndTag(short mode, int indent, Node node)
     {
         String p;
 
         // Netscape ignores SGML standard by not ignoring a line break before </A> or </U> etc.
         // To avoid rendering this as an underlined space, I disable line wrapping before inline end tags
 
         // if (indent + linelen < this.configuration.wraplen && !TidyUtils.toBoolean(mode & NOWRAP))
         // {
         // wraphere = linelen;
         // }
 
         addC('<', linelen++);
         addC('/', linelen++);
 
         p = node.element;
         for (int i = 0; i < p.length(); i++)
         {
             addC(
                 TidyUtils.foldCase(p.charAt(i), this.configuration.isUpperCaseTags(), this.configuration.isXmlTags()),
                 linelen++);
         }
 
         addC('>', linelen++);
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printComment(final Lexer lexer, final Out fout, final int indent, final Node node) {
     	setWrap(indent);
     	addString("<!--");
     	printText(lexer, fout, COMMENT, 0, node);
     	addString("--");
     	addChar('>');
         if (node.linebreak && node.next != null) {
             flushLine(fout, indent);
         }
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printDocType(final Lexer lexer, final Out fout, final int indent, final Node node) {
     	int wraplen = configuration.getWraplen();
         int spaces = configuration.getSpaces();
         AttVal fpi = node.getAttrByName("PUBLIC");
         AttVal sys = node.getAttrByName("SYSTEM");
         
         /* todo: handle non-ASCII characters in FPI / SI / node->element */
 
         setWrap(indent);
         condFlushLine(fout, indent);
         
         addString("<!DOCTYPE ");
         setWrap(indent);
         if (node.element != null) {
         	addString(node.element);
         }
         
         if (fpi != null && fpi.value != null) {
             addString(" PUBLIC ");
             addChar(fpi.delim);
             addString(fpi.value);
             addChar(fpi.delim);
         }
 
         if (fpi != null && fpi.value != null && sys != null && sys.value != null) {
             int i = linelen - (sys.value.length() + 2) - 1;
             if (!(i > 0 && sys.value.length() + 2 + i < wraplen
             		&& i <= (spaces != 0 ? spaces : 2) * 2)) {
                 i = 0;
             }
 
             condFlushLine(fout, i);
             if (linelen != 0) {
                 addChar(' ');
             }
         }
         else if (sys != null && sys.value != null) {
             addString(" SYSTEM ");
         }
 
         if (sys != null && sys.value != null) {
             addChar(sys.delim);
             addString(sys.value);
             addChar(sys.delim);
         }
 
         if (node.content != null) {
             condFlushLine(fout, indent);
             addChar('[');
             printText(lexer, fout, CDATA, 0, node.content);
             addChar(']');
         }
 
         setWrap(0);
         addChar('>');
         condFlushLine(fout, indent);
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printPI(final Lexer lexer, final Out fout, final int indent, final Node node)
     {
         if (indent + linelen < this.configuration.getWraplen())
         {
             wraphere = linelen;
         }
 
         addC('<', linelen++);
         addC('?', linelen++);
 
         // set CDATA to pass < and > unescaped
         printText(lexer, fout, CDATA, indent, node);
 
         if (node.end <= 0 || node.textarray[node.end - 1] != '?') // #542029 - fix by Terry Teague 10 Apr 02
         {
             addC('?', linelen++);
         }
 
         addC('>', linelen++);
         condFlushLine(fout, indent);
     }
 
     /**
      * Pretty print the xml declaration.
      * @param fout
      * @param indent
      * @param node
      */
     private void printXmlDecl(final Lexer lexer, final Out fout, final int indent, final Node node) {
     	AttVal att;
     	setWrap(indent);
     	final int saveWrap = wrapOff();
     	
     	/* no case translation for XML declaration pseudo attributes */
         final boolean ucAttrs = configuration.isUpperCaseAttrs();
         configuration.setUpperCaseAttrs(false);
         
         addString("<?xml");
         
         /* Force order of XML declaration attributes */
         if (null != (att = node.getAttrById(AttrId.VERSION))) {
             printAttribute(fout, indent, node, att);
         }
         if (null != (att = node.getAttrById(AttrId.ENCODING))) {
             printAttribute(fout, indent, node, att);
         }
         if (null != (att = node.getAttrByName("standalone"))) {
             printAttribute(fout, indent, node, att);
         }
         
         /* restore old config value */
         configuration.setUpperCaseAttrs(ucAttrs);
 
         if (node.end <= 0 || lexer.lexbuf[node.end - 1] != '?') {
             addChar('?');
         }
         addChar('>');
         wrapOn(saveWrap);
         flushLine(fout, indent);
     }
 
     /**
      * note ASP and JSTE share <% ... %> syntax.
      * @param fout
      * @param indent
      * @param node
      */
     private void printAsp(final Lexer lexer, final Out fout, final int indent, final Node node)
     {
         int savewraplen = this.configuration.getWraplen();
 
         // disable wrapping if so requested
 
         if (!this.configuration.isWrapAsp() || !this.configuration.isWrapJste())
         {
             this.configuration.setWraplen(0xFFFFFF); // a very large number
         }
 
         addC('<', linelen++);
         addC('%', linelen++);
 
         printText(lexer, fout, (this.configuration.isWrapAsp() ? CDATA : COMMENT), indent, node);
 
         addC('%', linelen++);
         addC('>', linelen++);
         /* condFlushLine(fout, indent); */
         this.configuration.setWraplen(savewraplen);
     }
 
     /**
      * JSTE also supports <# ... #> syntax
      * @param fout
      * @param indent
      * @param node
      */
     private void printJste(final Lexer lexer, final Out fout, final int indent, final Node node)
     {
         int savewraplen = this.configuration.getWraplen();
 
         // disable wrapping if so requested
 
         if (!this.configuration.isWrapJste())
         {
             this.configuration.setWraplen(0xFFFFFF); // a very large number
         }
 
         addC('<', linelen++);
         addC('#', linelen++);
 
         printText(lexer, fout, (this.configuration.isWrapJste() ? CDATA : COMMENT), indent, node);
 
         addC('#', linelen++);
         addC('>', linelen++);
         // condFlushLine(fout, indent);
         this.configuration.setWraplen(savewraplen);
     }
 
     /**
      * PHP is based on XML processing instructions.
      * @param fout
      * @param indent
      * @param node
      */
     private void printPhp(final Lexer lexer, final Out fout, final int indent, final Node node)
     {
         int savewraplen = this.configuration.getWraplen();
 
         // disable wrapping if so requested
 
         if (!this.configuration.isWrapPhp())
         {
             this.configuration.setWraplen(0xFFFFFF); // a very large number
         }
 
         addC('<', linelen++);
         addC('?', linelen++);
 
         printText(lexer, fout, (this.configuration.isWrapPhp() ? CDATA : COMMENT), indent, node);
 
         addC('?', linelen++);
         addC('>', linelen++);
         // PCondFlushLine(fout, indent);
         this.configuration.setWraplen(savewraplen);
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printCDATA(final Lexer lexer, final Out fout, int indent, final Node node)
     {
         int savewraplen = this.configuration.getWraplen();
 
         if (!this.configuration.isIndentCdata())
         {
             indent = 0;
         }
 
         condFlushLine(fout, indent);
 
         // disable wrapping
         this.configuration.setWraplen(0xFFFFFF); // a very large number
 
         addC('<', linelen++);
         addC('!', linelen++);
         addC('[', linelen++);
         addC('C', linelen++);
         addC('D', linelen++);
         addC('A', linelen++);
         addC('T', linelen++);
         addC('A', linelen++);
         addC('[', linelen++);
 
         printText(lexer, fout, COMMENT, indent, node);
 
         addC(']', linelen++);
         addC(']', linelen++);
         addC('>', linelen++);
         condFlushLine(fout, indent);
         this.configuration.setWraplen(savewraplen);
     }
 
     /**
      * @param fout
      * @param indent
      * @param node
      */
     private void printSection(final Lexer lexer, final Out fout, final int indent, final Node node)
     {
         int savewraplen = this.configuration.getWraplen();
 
         // disable wrapping if so requested
 
         if (!this.configuration.isWrapSection())
         {
             this.configuration.setWraplen(0xFFFFFF); // a very large number
         }
 
         addC('<', linelen++);
         addC('!', linelen++);
         addC('[', linelen++);
 
         printText(lexer, fout, (this.configuration.isWrapSection() ? CDATA : COMMENT),
             indent, node);
 
         addC(']', linelen++);
         addC('>', linelen++);
         // PCondFlushLine(fout, indent);
         this.configuration.setWraplen(savewraplen);
     }
 
     /**
      * Is the current node inside HEAD?
      * @param node Node
      * @return <code>true</code> if node is inside an HEAD tag
      */
     private boolean insideHead(Node node)
     {
         if (node.is(TagId.HEAD))
         {
             return true;
         }
 
         if (node.parent != null)
         {
             return insideHead(node.parent);
         }
         return false;
     }
 
     /**
      * Is text node and already ends w/ a newline? Used to pretty print CDATA/PRE text content. If it already ends on a
      * newline, it is not necessary to print another before printing end tag.
      * @param node text node
      * @return text indent
      */
     private int textEndsWithNewline(final Lexer lexer, final Node node, final int mode) {
     	if ((mode & (CDATA|COMMENT)) != 0 && node.isText() && node.end > node.start) {
     		int ch, ix = node.end - 1;
     		// Skip non-newline whitespace
             while (ix >= node.start && (ch = (lexer.lexbuf[ix] & 0xff)) != 0
                     && (ch == ' ' || ch == '\t' || ch == '\r')) {
                 --ix;
             }
             if (lexer.lexbuf[ix] == '\n') {
             	return node.end - ix - 1; // #543262 tidy eats all memory
             }
         }
         return -1;
     }
     
     private int textStartsWithWhitespace(final Lexer lexer, final Node node, final int start, final int mode) {
         assert(node != null);
         if ((mode & (CDATA|COMMENT)) != 0 && node.isText() && node.end > node.start && start >= node.start) {
             int ch, ix = start;
             /* Skip whitespace. */
             while (ix < node.end && (ch = (lexer.lexbuf[ix] & 0xff)) != 0
                     && (ch==' ' || ch=='\t' || ch=='\r')) {
                 ++ix;
             }
             if (ix > start) {
             	return ix - start;
             }
         }
         return -1;
     }
 
     /**
      * Does the current node contain a CDATA section?
      * @param node Node
      * @return <code>true</code> if node contains a CDATA section
      */
     static boolean hasCDATA(Node node)
     {
         // Scan forward through the textarray. Since the characters we're
         // looking for are < 0x7f, we don't have to do any UTF-8 decoding.
 
         if (node.type != NodeType.TextNode)
         {
             return false;
         }
 
         int len = node.end - node.start + 1;
         String start = TidyUtils.getString(node.textarray, node.start, len);
 
         int indexOfCData = start.indexOf(CDATA_START);
         return indexOfCData > -1 && indexOfCData <= len;
     }
 
     /**
      * Print script and style elements. For XHTML, wrap the content as follows:
      * 
      * <pre>
      *     JavaScript:
      *         //&lt;![CDATA[
      *             content
      *         //]]>
      *     VBScript:
      *         '&lt;![CDATA[
      *             content
      *         ']]>
      *     CSS:
      *         /*&lt;![CDATA[* /
      *             content
      *         /*]]>* /
      *     other:
      *        &lt;![CDATA[
      *             content
      *         ]]>
      * </pre>
      * 
      * @param fout
      * @param mode
      * @param indent
      * @param lexer
      * @param node
      */
     private void printScriptStyle(Out fout, short mode, int indent, Lexer lexer, Node node)
     {
         Node content;
         String commentStart = DEFAULT_COMMENT_START;
         String commentEnd = DEFAULT_COMMENT_END;
         boolean hasCData = false;
         int contentIndent = -1;
         final boolean xhtmlOut = lexer.configuration.isXHTML();
 
         if (insideHead(node)) {
             flushLine(fout, indent);
         }
 
         // start script
         printTag(lexer, fout, mode, indent, node);
         flushLine(fout, 0);
 
 		if (xhtmlOut && node.content != null)
         {
             AttVal type = node.getAttrById(AttrId.TYPE);
             if (type != null)
             {
                 if ("text/javascript".equalsIgnoreCase(type.value))
                 {
                     commentStart = JS_COMMENT_START;
                     commentEnd = JS_COMMENT_END;
                 }
                 else if ("text/css".equalsIgnoreCase(type.value))
                 {
                     commentStart = CSS_COMMENT_START;
                     commentEnd = CSS_COMMENT_END;
                 }
                 else if ("text/vbscript".equalsIgnoreCase(type.value))
                 {
                     commentStart = VB_COMMENT_START;
                     commentEnd = VB_COMMENT_END;
                 }
             }
 
             hasCData = hasCDATA(node.content);
             if (!hasCData)
             {
                 // disable wrapping
                 int savewrap = lexer.configuration.getWraplen();
                 lexer.configuration.setWraplen(0xFFFFFF); // a very large number
 
                 linelen = addAsciiString(commentStart, linelen);
                 linelen = addAsciiString(CDATA_START, linelen);
                 linelen = addAsciiString(commentEnd, linelen);
                 condFlushLine(fout, indent);
 
                 // restore wrapping
                 lexer.configuration.setWraplen(savewrap);
             }
         }
 
         for (content = node.content; content != null; content = content.next)
         {
             printTree(fout, (short) (mode | PREFORMATTED | NOWRAP | CDATA), indent, lexer, content);
 
             if (content == node.last)
             {
                 contentIndent = textEndsWithNewline(lexer, content, CDATA);
             }
 
         }
 
         if (contentIndent < 0)
         {
             condFlushLine(fout, indent);
             contentIndent = 0;
         }
 
         if (xhtmlOut && node.content != null)
         {
             if (!hasCData)
             {
                 // disable wrapping
                 int savewrap = lexer.configuration.getWraplen();
                 lexer.configuration.setWraplen(0xFFFFFF); // a very large number
 
                 linelen = addAsciiString(commentStart, linelen);
                 linelen = addAsciiString(CDATA_END, linelen);
                 linelen = addAsciiString(commentEnd, linelen);
 
                 // restore wrapping
                 lexer.configuration.setWraplen(savewrap);
                 condFlushLine(fout, indent);
             }
         }
 
         printEndTag(mode, indent, node);
 
         if (lexer.configuration.getIndentContent() == TriState.No
         		&& node.next != null
         		&& !(node.hasCM(Dict.CM_INLINE) || node.isText())) {
             flushLine(fout, indent);
         }
     }
 
     /**
      * Should tidy indent the give tag?
      * @param node actual node
      * @return <code>true</code> if line should be indented
      */
     private boolean shouldIndent(Node node) {
     	final TriState indentContent = configuration.getIndentContent();
         if (indentContent == TriState.No) {
             return false;
         }
         if (node.is(TagId.TEXTAREA)) {
         	return false;
         }
         if (indentContent == TriState.Auto) {
             if (node.content != null && node.hasCM(Dict.CM_NO_INDENT)) {
                 for (node = node.content; node != null; node = node.next) {
                     if (node.hasCM(Dict.CM_BLOCK)) {
                         return true;
                     }
                 }
                 return false;
             }
 
             if (node.hasCM(Dict.CM_HEADING)) {
                 return false;
             }
             
             if (node.is(TagId.HTML)) {
                 return false;
             }
 
             if (node.is(TagId.P)) {
                 return false;
             }
 
             if (node.is(TagId.TITLE)) {
                 return false;
             }
             
             /* http://tidy.sf.net/issue/1610888
             Indenting <div><img /></div> produces spurious lines with IE 6.x */
             if (node.is(TagId.DIV) && node.last != null && node.last.is(TagId.IMG)) {
             	return false;
 	        }
         }
 
         if (node.hasCM(Dict.CM_FIELD | Dict.CM_OBJECT)) {
             return true;
         }
 
         if (node.is(TagId.MAP)) {
             return true;
         }
 
         return !node.hasCM(Dict.CM_INLINE) && node.content != null;
     }
 
     /**
      * Print just the content of the body element. Useful when you want to reuse material from other documents.
      * @param fout
      * @param lexer
      * @param root
      * @param xml
      */
     void printBody(Out fout, Lexer lexer, Node root, boolean xml)
     {
         if (root == null)
         {
             return;
         }
 
         // Feature request #434940 - fix by Dave Raggett/Ignacio Vazquez-Abrams 21 Jun 01
         // Sebastiano Vigna <vigna@dsi.unimi.it>
         Node body = root.findBody();
 
         if (body != null)
         {
             Node content;
             for (content = body.content; content != null; content = content.next)
             {
                 printTree(fout, (short) 0, 0, lexer, content);
             }
         }
     }
 
     /**
      * @param fout
      * @param mode
      * @param indent
      * @param lexer
      * @param node
      */
     public void printTree(Out fout, short mode, int indent, Lexer lexer, Node node)
     {
         Node content, last;
         int spaces = configuration.getSpaces();
         boolean xhtml = configuration.isXHTML();
 
         if (node == null) {
             return;
         }
 
         if (node.type == NodeType.TextNode) {
             printText(lexer, fout, mode, indent, node);
         }
         else if (node.type == NodeType.CommentTag) {
             printComment(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.RootNode) {
             for (content = node.content; content != null; content = content.next) {
                 printTree(fout, mode, indent, lexer, content);
             }
         }
         else if (node.type == NodeType.DocTypeTag) {
             printDocType(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.ProcInsTag) {
             printPI(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.XmlDecl) {
             printXmlDecl(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.CDATATag) {
             printCDATA(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.SectionTag) {
             printSection(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.AspTag) {
             printAsp(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.JsteTag) {
             printJste(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.PhpTag) {
             printPhp(lexer, fout, indent, node);
         }
         else if (node.hasCM(Dict.CM_EMPTY)
         		|| (node.type == NodeType.StartEndTag && !xhtml)) {
             if (!node.hasCM(Dict.CM_INLINE)) {
                 condFlushLine(fout, indent);
             }
 
             if (node.is(TagId.BR) && node.prev != null
             		&& !(node.prev.is(TagId.BR) || (mode & PREFORMATTED) != 0)
             		&& this.configuration.isBreakBeforeBR()) {
                 flushLine(fout, indent);
             }
             
             if (node.is(TagId.HR)) {
             	// insert extra newline for classic formatting
                 final boolean classic = configuration.isVertSpace();
                 if (classic && node.parent != null && node.parent.content != node) {
                 	flushLine(fout, indent);
                 }
             }
 
             printTag(lexer, fout, mode, indent, node);
             
             if (node.next != null) {
 	            if (node.is(TagId.PARAM) || node.is(TagId.AREA)) {
 	                condFlushLine(fout, indent);
 	            }
 	            else if ((node.is(TagId.BR) && (mode & PREFORMATTED) == 0) || node.is(TagId.HR)) {
 	                flushLine(fout, indent);
 	            }
             }
         }
         else // some kind of container element
         {
             if (node.type == NodeType.StartEndTag) {
                 node.type = NodeType.StartTag;
             }
 
             if (node.tag != null
             		&& (node.tag.getParser() == ParserImpl.PRE || node.is(TagId.TEXTAREA))) {
             	final boolean classic = configuration.isVertSpace();
             	final int indprev = indent;
                 condFlushLine(fout, indent);
                 condFlushLine(fout, indent);
                 
                 // insert extra newline for classic formatting
                 if (classic && node.parent != null && node.parent.content != node) {
                     flushLine(fout, indent);
                 }
                 printTag(lexer, fout, mode, indent, node);
 
                 indent = 0;
                 flushLine(fout, indent);
 
                 for (content = node.content; content != null; content = content.next) {
                     printTree(fout, (short) (mode | PREFORMATTED | NOWRAP), indent, lexer, content);
                 }
 
                 condFlushLine(fout, indent);
                 indent = indprev;
                 printEndTag(mode, indent, node);
                 
                 if (configuration.getIndentContent() == TriState.No && node.next != null) {
                     flushLine(fout, indent);
                 }
             }
             else if (node.is(TagId.STYLE) || node.is(TagId.SCRIPT)) {
                 printScriptStyle(fout, (short) (mode | PREFORMATTED | NOWRAP | CDATA), indent, lexer, node);
             }
             else if (node.hasCM(Dict.CM_INLINE)) {
                 if (configuration.isMakeClean()) {
                     // replace <nobr> ... </nobr> by &nbsp; or &#160; etc.
                     if (node.is(TagId.NOBR)) {
                         for (content = node.content; content != null; content = content.next) {
                             printTree(fout, (short) (mode | NOWRAP), indent, lexer, content);
                         }
                         return;
                     }
                 }
 
                 // otherwise a normal inline element
                 printTag(lexer, fout, mode, indent, node);
 
                 // indent content for SELECT, TEXTAREA, MAP, OBJECT and APPLET
                 if (shouldIndent(node)) {
                 	indent += spaces;
                     condFlushLine(fout, indent);
 
                     for (content = node.content; content != null; content = content.next) {
                         printTree(fout, mode, indent, lexer, content);
                     }
 
                     indent -= spaces;
                     condFlushLine(fout, indent);
                 }
                 else {
                     for (content = node.content; content != null; content = content.next) {
                         printTree(fout, mode, indent, lexer, content);
                     }
                 }
                 printEndTag(mode, indent, node);
             }
             else { // other tags
             	final boolean indcont = configuration.getIndentContent() != TriState.No;
             	final boolean indsmart = configuration.getIndentContent() == TriState.Auto;
             	final boolean hideend = configuration.isHideEndTags();
             	final boolean classic = configuration.isVertSpace();
             	int contentIndent = indent;
             	
             	// insert extra newline for classic formatting
                 if (classic && node.parent != null && node.parent.content != node && !node.is(TagId.HTML)) {
                 	flushLine(fout, indent);
                 }
                 
                 if (shouldIndent(node)) {
                 	contentIndent += spaces;
                 }
 
                 condFlushLine(fout, indent);
                 if (indsmart && node.prev != null) {
                     flushLine(fout, indent);
                 }
 
                 // do not omit elements with attributes
                 if (!hideend || !node.hasCM(Dict.CM_OMITST)
                     	|| node.attributes != null) {
                     printTag(lexer, fout, mode, indent, node);
 
                     if (shouldIndent(node)) {
                     	if (!(node.is(TagId.LI) && node.content.isText())) {
                     		condFlushLine(fout, contentIndent);
                     	}
                     }
                     else if (node.hasCM(Dict.CM_HTML) || node.is(TagId.NOFRAMES)
                     		|| (node.hasCM(Dict.CM_HEAD) && !node.is(TagId.TITLE))) {
                         flushLine(fout, contentIndent);
                     }
                 }
                 
                 last = null;
                 for (content = node.content; content != null; content = content.next) {
                     // kludge for naked text before block level tag
                     if (last != null && !indcont && last.isText()
                     		&& content.tag != null && !content.hasCM(Dict.CM_INLINE)) {
                         flushLine(fout, contentIndent);
                     }
 
                     printTree(fout, mode, contentIndent, lexer, content);
                     last = content;
                 }
 
                 // don't flush line for td and th
                 if (shouldIndent(node) || (!hideend && (node.hasCM(Dict.CM_HTML) || node.is(TagId.NOFRAMES)
                 		|| (node.hasCM(Dict.CM_HEAD) && !node.is(TagId.TITLE))))) {
                     condFlushLine(fout, indent);
                     if (!hideend || !node.hasCM(Dict.CM_OPT)) {
                         printEndTag(mode, indent, node);
                     }
                 }
                 else {
                     if (!hideend || !node.hasCM(Dict.CM_OPT)) {
                     	// newline before endtag for classic formatting
                         if (classic && !node.hasMixedContent()) {
                             flushLine(fout, indent);
                         }
                         printEndTag(mode, indent, node);
                     }
                 }
                 
                 if (!indcont && !hideend && !node.is(TagId.HTML) && !classic) {
                 	flushLine(fout, indent);
                 }
                 else if (classic && node.next != null
                 		&& node.hasCM(Dict.CM_LIST | Dict.CM_DEFLIST | Dict.CM_TABLE | Dict.CM_BLOCK)) {
                 	flushLine(fout, indent);
                 }
             }
         }
     }
 
     /**
      * @param fout
      * @param mode
      * @param indent
      * @param lexer
      * @param node
      */
     public void printXMLTree(Out fout, short mode, int indent, Lexer lexer, Node node)
     {
         TagTable tt = this.configuration.tt;
 
         if (node == null)
         {
             return;
         }
 
         if (node.type == NodeType.TextNode || (node.type == NodeType.CDATATag && lexer.configuration.isEscapeCdata()))
         {
             printText(lexer, fout, mode, indent, node);
         }
         else if (node.type == NodeType.CommentTag)
         {
             condFlushLine(fout, indent);
             printComment(lexer, fout, 0, node);
             condFlushLine(fout, 0);
         }
         else if (node.type == NodeType.RootNode)
         {
             Node content;
 
             for (content = node.content; content != null; content = content.next)
             {
                 printXMLTree(fout, mode, indent, lexer, content);
             }
         }
         else if (node.type == NodeType.DocTypeTag)
         {
             printDocType(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.ProcInsTag)
         {
             printPI(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.XmlDecl)
         {
             printXmlDecl(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.CDATATag)
         {
             printCDATA(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.SectionTag)
         {
             printSection(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.AspTag)
         {
             printAsp(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.JsteTag)
         {
             printJste(lexer, fout, indent, node);
         }
         else if (node.type == NodeType.PhpTag)
         {
             printPhp(lexer, fout, indent, node);
         }
         else if (TidyUtils.toBoolean(node.tag.model & Dict.CM_EMPTY)
             || node.type == NodeType.StartEndTag
             && !configuration.isXHTML())
         {
             condFlushLine(fout, indent);
             printTag(lexer, fout, mode, indent, node);
             // fgiust: Remove empty lines between tags in XML.
             // flushLine(fout, indent);
 
             // CPR: folks don't want so much vertical spacing in XML
             // if (node.next != null) { flushLine(fout, indent); }
 
         }
         else
         {
             // some kind of container element
             Node content;
             boolean mixed = false;
             int cindent;
 
             for (content = node.content; content != null; content = content.next)
             {
                 if (content.type == NodeType.TextNode)
                 {
                     mixed = true;
                     break;
                 }
             }
 
             condFlushLine(fout, indent);
 
             if (ParserImpl.XMLPreserveWhiteSpace(node, tt))
             {
                 indent = 0;
                 cindent = 0;
                 mixed = false;
             }
             else if (mixed)
             {
                 cindent = indent;
             }
             else
             {
                 cindent = indent + this.configuration.getSpaces();
             }
 
             printTag(lexer, fout, mode, indent, node);
 
             if (!mixed && node.content != null)
             {
                 flushLine(fout, indent);
             }
 
             for (content = node.content; content != null; content = content.next)
             {
                 printXMLTree(fout, mode, cindent, lexer, content);
             }
 
             if (!mixed && node.content != null)
             {
                 condFlushLine(fout, cindent);
             }
             printEndTag(mode, indent, node);
             // condFlushLine(fout, indent);
 
             // CPR: folks don't want so much vertical spacing in XML
             // if (node.next != null) { flushLine(fout, indent); }
 
         }
     }
 
     /**
      * Split parse tree by h2 elements and output to separate files. Counts number of h2 children (if any) belonging to
      * node.
      * @param node root node
      * @return number of slides (number of h2 elements)
      */
     public int countSlides(Node node)
     {
         // assume minimum of 1 slide
         int n = 1;
 
         // fix for [431716] avoid empty slides
         if (node != null && node.content != null && node.content.is(TagId.H2))
         {
             // "first" slide is empty, so ignore it
             n--;
         }
 
         if (node != null)
         {
             for (node = node.content; node != null; node = node.next)
             {
                 if (node.is(TagId.H2))
                 {
                     ++n;
                 }
             }
         }
 
         return n;
     }
 
     /**
      * Add meta element for page transition effect, this works on IE but not NS.
      * @param lexer
      * @param root
      * @param duration
      */
     public void addTransitionEffect(Lexer lexer, Node root, double duration)
     {
         Node head = root.findHEAD();
         String transition;
 
         transition = "blendTrans(Duration=" + (new Double(duration)).toString() + ")";
 
         if (head != null)
         {
             Node meta = lexer.inferredTag(TagId.META);
             meta.addAttribute("http-equiv", "Page-Enter");
             meta.addAttribute("content", transition);
             head.insertNodeAtStart(meta);
         }
     }
 }
