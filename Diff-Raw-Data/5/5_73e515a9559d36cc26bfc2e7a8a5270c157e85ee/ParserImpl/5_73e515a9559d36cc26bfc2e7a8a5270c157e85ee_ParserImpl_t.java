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
 
 import static org.w3c.tidy.Versions.*;
 
 import org.w3c.tidy.Node.NodeType;
 
 /**
  * HTML Parser implementation.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public final class ParserImpl
 {
 
     /**
      * parser for html.
      */
     public static final Parser HTML = new ParseHTML();
 
     /**
      * parser for head.
      */
     public static final Parser HEAD = new ParseHead();
 
     /**
      * parser for title.
      */
     public static final Parser TITLE = new ParseTitle();
 
     /**
      * parser for script.
      */
     public static final Parser SCRIPT = new ParseScript();
 
     /**
      * parser for body.
      */
     public static final Parser BODY = new ParseBody();
 
     /**
      * parser for frameset.
      */
     public static final Parser FRAMESET = new ParseFrameSet();
 
     /**
      * parser for inline.
      */
     public static final Parser INLINE = new ParseInline();
 
     /**
      * parser for list.
      */
     public static final Parser LIST = new ParseList();
 
     /**
      * parser for definition lists.
      */
     public static final Parser DEFLIST = new ParseDefList();
 
     /**
      * parser for pre.
      */
     public static final Parser PRE = new ParsePre();
 
     /**
      * parser for block elements.
      */
     public static final Parser BLOCK = new ParseBlock();
 
     /**
      * parser for table.
      */
     public static final Parser TABLETAG = new ParseTableTag();
 
     /**
      * parser for colgroup.
      */
     public static final Parser COLGROUP = new ParseColGroup();
 
     /**
      * parser for rowgroup.
      */
     public static final Parser ROWGROUP = new ParseRowGroup();
 
     /**
      * parser for row.
      */
     public static final Parser ROW = new ParseRow();
 
     /**
      * parser for noframes.
      */
     public static final Parser NOFRAMES = new ParseNoFrames();
 
     /**
      * parser for select.
      */
     public static final Parser SELECT = new ParseSelect();
 
     /**
      * parser for text.
      */
     public static final Parser TEXT = new ParseText();
 
     /**
      * parser for empty elements.
      */
     public static final Parser EMPTY = new ParseEmpty();
 
     /**
      * parser for optgroup.
      */
     public static final Parser OPTGROUP = new ParseOptGroup();
 
     /**
      * ParserImpl should not be instantiated.
      */
     private ParserImpl()
     {
         // unused
     }
 
     /**
      * @param lexer
      * @param node
      * @param mode
      */
     protected static void parseTag(Lexer lexer, Node node, short mode)
     {
         // Fix by GLP 2000-12-21. Need to reset insertspace if this
         // is both a non-inline and empty tag (base, link, meta, isindex, hr, area).
         if ((node.tag.model & Dict.CM_EMPTY) != 0)
         {
             lexer.waswhite = false;
         }
         else if (!((node.tag.model & Dict.CM_INLINE) != 0))
         {
             lexer.insertspace = false;
         }
 
         if (node.tag.getParser() == null)
         {
             return;
         }
 
         if (node.type == NodeType.StartEndTag)
         {
             return;
         }
 
         node.tag.getParser().parse(lexer, node, mode);
     }
 
     /**
      * Move node to the head, where element is used as starting point in hunt for head. Normally called during parsing.
      * @param lexer
      * @param element
      * @param node
      */
     protected static void moveToHead(Lexer lexer, Node element, Node node)
     {
         Node head;
         node.removeNode(); // make sure that node is isolated
 
         if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
         {
             lexer.report.warning(lexer, element, node, ErrorCode.TAG_NOT_ALLOWED_IN);
 
             while (!element.is(TagId.HTML))
             {
                 element = element.parent;
             }
 
             for (head = element.content; head != null; head = head.next)
             {
                 if (head.is(TagId.HEAD))
                 {
                     head.insertNodeAtEnd(node);
                     break;
                 }
             }
 
             if (node.tag.getParser() != null)
             {
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
         }
         else
         {
             lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
         }
     }
 
     /**
      * moves given node to end of body element.
      * @param lexer Lexer
      * @param node Node to insert
      */
     private static void moveNodeToBody(final Lexer lexer, final Node node) {
     	final Node body = lexer.root.findBody();
         if (body != null) {
 	        node.removeNode();
 	        body.insertNodeAtEnd(node);
         }
     }
 
     /**
      * Parser for HTML.
      */
     public static class ParseHTML implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node html, short mode)
         {
             Node node, head;
             Node frameset = null;
             Node noframes = null;
 
             lexer.configuration.setXmlTags(false);
             lexer.seenEndBody = false;
 
             while (true)
             {
                 node = lexer.getToken(Lexer.IGNORE_WHITESPACE);
 
                 if (node == null)
                 {
                     node = lexer.inferredTag(TagId.HEAD);
                     break;
                 }
 
                 if (node.is(TagId.HEAD))
                 {
                     break;
                 }
 
                 if (node.tag == html.tag && node.type == NodeType.EndTag)
                 {
                     lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(html, node))
                 {
                     continue;
                 }
 
                 lexer.ungetToken();
                 node = lexer.inferredTag(TagId.HEAD);
                 break;
             }
 
             head = node;
             html.insertNodeAtEnd(head);
             HEAD.parse(lexer, head, mode);
 
             while (true)
             {
                 node = lexer.getToken(Lexer.IGNORE_WHITESPACE);
 
                 if (node == null)
                 {
                     if (frameset == null)
                     {
                         // implied body
                         node = lexer.inferredTag(TagId.BODY);
                         html.insertNodeAtEnd(node);
                         BODY.parse(lexer, node, mode);
                     }
 
                     return;
                 }
 
                 // robustly handle html tags
                 if (node.tag == html.tag)
                 {
                     if (node.type != NodeType.StartTag && frameset == null)
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
                     else if (node.type == NodeType.EndTag)
                     {
                         lexer.seenEndHtml = true;
                     }
 
                     continue;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(html, node))
                 {
                     continue;
                 }
 
                 // if frameset document coerce <body> to <noframes>
                 if (node.is(TagId.BODY))
                 {
                     if (node.type != NodeType.StartTag)
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (frameset != null)
                     {
                         lexer.ungetToken();
 
                         if (noframes == null)
                         {
                             noframes = lexer.inferredTag(TagId.NOFRAMES);
                             frameset.insertNodeAtEnd(noframes);
                             lexer.report.warning(lexer, html, noframes, ErrorCode.INSERTING_TAG);
                         }
 
                         parseTag(lexer, noframes, mode);
                         continue;
                     }
 
                     lexer.constrainVersion(~VERS_FRAMESET);
                     break; // to parse body
                 }
 
                 // flag an error if we see more than one frameset
                 if (node.is(TagId.FRAMESET))
                 {
                     if (node.type != NodeType.StartTag)
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (frameset != null)
                     {
                         lexer.report.error(lexer, html, node, ErrorCode.DUPLICATE_FRAMESET);
                     }
                     else
                     {
                         frameset = node;
                     }
 
                     html.insertNodeAtEnd(node);
                     parseTag(lexer, node, mode);
 
                     // see if it includes a noframes element so that we can merge subsequent noframes elements
 
                     for (node = frameset.content; node != null; node = node.next)
                     {
                         if (node.is(TagId.NOFRAMES))
                         {
                             noframes = node;
                         }
                     }
                     continue;
                 }
 
                 // if not a frameset document coerce <noframes> to <body>
                 if (node.is(TagId.NOFRAMES))
                 {
                     if (node.type != NodeType.StartTag)
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (frameset == null)
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                         node = lexer.inferredTag(TagId.BODY);
                         break;
                     }
 
                     if (noframes == null)
                     {
                         noframes = node;
                         frameset.insertNodeAtEnd(noframes);
                     }
 
                     parseTag(lexer, noframes, mode);
                     continue;
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (node.tag != null && (node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         moveToHead(lexer, html, node);
                         continue;
                     }
 
                     // #427675 - discard illegal frame element following a frameset - fix by Randy Waki 11 Oct 00
                     if (frameset != null && node.is(TagId.FRAME))
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
                 }
 
                 lexer.ungetToken();
 
                 // insert other content into noframes element
                 if (frameset != null)
                 {
                     if (noframes == null)
                     {
                         noframes = lexer.inferredTag(TagId.NOFRAMES);
                         frameset.insertNodeAtEnd(noframes);
                     }
                     else
                     {
                         lexer.report.warning(lexer, html, node, ErrorCode.NOFRAMES_CONTENT);
                         if (noframes.type == NodeType.StartEndTag) {
                         	noframes.type = NodeType.StartTag;
                         }
                     }
 
                     lexer.constrainVersion(VERS_FRAMESET);
                     parseTag(lexer, noframes, mode);
                     continue;
                 }
 
                 node = lexer.inferredTag(TagId.BODY);
                 lexer.report.warning(lexer, html, node, ErrorCode.INSERTING_TAG);
                 lexer.constrainVersion(~VERS_FRAMESET);
                 break;
             }
 
             // node must be body
             html.insertNodeAtEnd(node);
             parseTag(lexer, node, mode);
             lexer.seenEndHtml = true;
         }
 
     }
 
     /**
      * Parser for HEAD.
      */
     public static class ParseHead implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node head, short mode)
         {
             Node node;
             int hasTitle = 0;
             int hasBase = 0;
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == head.tag && node.type == NodeType.EndTag)
                 {
                     head.closed = true;
                     break;
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     lexer.report.warning(lexer, head, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     lexer.ungetToken();
                     break;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(head, node))
                 {
                     continue;
                 }
 
                 if (node.type == NodeType.DocTypeTag)
                 {
                     Node.insertDocType(lexer, head, node);
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, head, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (!TidyUtils.toBoolean(node.tag.model & Dict.CM_HEAD))
                 {
                     // #545067 Implicit closing of head broken - warn only for XHTML input
                     if (lexer.isvoyager)
                     {
                         lexer.report.warning(lexer, head, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     }
                     lexer.ungetToken();
                     break;
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (node.is(TagId.TITLE))
                     {
                         ++hasTitle;
 
                         if (hasTitle > 1)
                         {
                             lexer.report.warning(lexer, head, node, ErrorCode.TOO_MANY_ELEMENTS);
                         }
                     }
                     else if (node.is(TagId.BASE))
                     {
                         ++hasBase;
 
                         if (hasBase > 1)
                         {
                             lexer.report.warning(lexer, head, node, ErrorCode.TOO_MANY_ELEMENTS);
                         }
                     }
                     else if (node.is(TagId.NOSCRIPT))
                     {
                         lexer.report.warning(lexer, head, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     }
 
                     head.insertNodeAtEnd(node);
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                     continue;
                 }
 
                 // discard unexpected text nodes and end tags
                 lexer.report.warning(lexer, head, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
         }
     }
 
     /**
      * Parser for TITLE.
      */
     public static class ParseTitle implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node title, short mode)
         {
             Node node;
 
             while ((node = lexer.getToken(Lexer.MIXED_CONTENT)) != null)
             {
                 if (node.tag == title.tag && node.type == NodeType.StartTag) {
                     lexer.report.warning(lexer, title, node, ErrorCode.COERCE_TO_ENDTAG);
                     node.type = NodeType.EndTag;
                     lexer.ungetToken();
                     continue;
                 }
                 else if (node.tag == title.tag && node.type == NodeType.EndTag)
                 {
                     title.closed = true;
                     Node.trimSpaces(lexer, title);
                     return;
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     // only called for 1st child
                     if (title.content == null)
                     {
                         Node.trimInitialSpace(lexer, title, node);
                     }
 
                     if (node.start >= node.end)
                     {
                         continue;
                     }
 
                     title.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(title, node))
                 {
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, title, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // pushback unexpected tokens
                 lexer.report.warning(lexer, title, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                 lexer.ungetToken();
                 Node.trimSpaces(lexer, title);
                 return;
             }
 
             lexer.report.warning(lexer, title, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
 
     }
 
     /**
      * Parser for SCRIPT.
      */
     public static class ParseScript implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
     	public void parse(Lexer lexer, Node script, short mode) {
             Node node = lexer.getCDATA(script);
             if (node != null) {
                 script.insertNodeAtEnd(node);
             } else {
                 /* handle e.g. a document like "<script>" */
                 lexer.report.warning(lexer, script, null, ErrorCode.MISSING_ENDTAG_FOR);
                 return;
             }
             node = lexer.getToken(Lexer.IGNORE_WHITESPACE);
             if (!(node != null && node.type == NodeType.EndTag && node.tag != null &&
             		node.tag.id == script.tag.id)) {
                 lexer.report.warning(lexer, script, node, ErrorCode.MISSING_ENDTAG_FOR);
                 if (node != null) {
                 	lexer.ungetToken();
                 }
             }
         }
     }
 
     /**
      * Parser for BODY.
      */
     public static class ParseBody implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node body, short mode)
         {
             Node node;
             boolean checkstack, iswhitenode;
 
             mode = Lexer.IGNORE_WHITESPACE;
             checkstack = true;
 
             Clean.bumpObject(lexer, body.parent);
 
             while ((node = lexer.getToken(mode)) != null)
             {
             	/* find and discard multiple <body> elements */
                 if (node.tag == body.tag && node.type == NodeType.StartTag) {
                     lexer.report.warning(lexer, body, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
                 
                 // #538536 Extra endtags not detected
                 if (node.is(TagId.HTML))
                 {
                     if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag || lexer.seenEndHtml)
                     {
                         lexer.report.warning(lexer, body, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
                     else
                     {
                         lexer.seenEndHtml = true;
                     }
 
                     continue;
                 }
 
                 if (lexer.seenEndBody
                     && (node.type == NodeType.StartTag || node.type == NodeType.EndTag || node.type == NodeType.StartEndTag))
                 {
                     lexer.report.warning(lexer, body, node, ErrorCode.CONTENT_AFTER_BODY);
                 }
 
                 if (node.tag == body.tag && node.type == NodeType.EndTag)
                 {
                     body.closed = true;
                     Node.trimSpaces(lexer, body);
                     lexer.seenEndBody = true;
                     mode = Lexer.IGNORE_WHITESPACE;
 
                     if (body.parent.is(TagId.NOFRAMES))
                     {
                         break;
                     }
 
                     continue;
                 }
 
                 if (node.is(TagId.NOFRAMES))
                 {
                     if (node.type == NodeType.StartTag)
                     {
                         body.insertNodeAtEnd(node);
                         BLOCK.parse(lexer, node, mode);
                         continue;
                     }
 
                     if (node.type == NodeType.EndTag && body.parent.is(TagId.NOFRAMES))
                     {
                         Node.trimSpaces(lexer, body);
                         lexer.ungetToken();
                         break;
                     }
                 }
 
                 if ((node.is(TagId.FRAME) || node.is(TagId.FRAMESET)) && body.parent.is(TagId.NOFRAMES))
                 {
                     Node.trimSpaces(lexer, body);
                     lexer.ungetToken();
                     break;
                 }
 
                 iswhitenode = false;
 
                 if (node.type == NodeType.TextNode
                     && node.end <= node.start + 1
                     && node.textarray[node.start] == (byte) ' ')
                 {
                     iswhitenode = true;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(body, node))
                 {
                     continue;
                 }
 
                 // #538536 Extra endtags not detected
                 // if (lexer.seenEndBody && !iswhitenode)
                 // {
                 // lexer.seenEndBody = true;
                 // lexer.report.warning(lexer, body, node, Report.CONTENT_AFTER_BODY);
                 // }
 
                 // mixed content model permits text
                 if (node.type == NodeType.TextNode)
                 {
                     if (iswhitenode && mode == Lexer.IGNORE_WHITESPACE)
                     {
                         continue;
                     }
 
                     // HTML2 and HTML4 strict doesn't allow text here
                     lexer.constrainVersion(~(VERS_HTML40_STRICT | VERS_HTML20));
 
                     if (checkstack)
                     {
                         checkstack = false;
 
                         if (lexer.inlineDup(node) > 0)
                         {
                             continue;
                         }
                     }
 
                     body.insertNodeAtEnd(node);
                     mode = Lexer.MIXED_CONTENT;
                     continue;
                 }
 
                 if (node.type == NodeType.DocTypeTag)
                 {
                     Node.insertDocType(lexer, body, node);
                     continue;
                 }
                 // discard unknown and PARAM tags
                 if (node.tag == null || node.is(TagId.PARAM))
                 {
                     lexer.report.warning(lexer, body, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // Netscape allows LI and DD directly in BODY We infer UL or DL respectively and use this boolean to
                 // exclude block-level elements so as to match Netscape's observed behaviour.
 
                 lexer.excludeBlocks = false;
 
                 if ((!((node.tag.model & Dict.CM_BLOCK) != 0) && !((node.tag.model & Dict.CM_INLINE) != 0))
                     || node.is(TagId.INPUT))
                 {
                     // avoid this error message being issued twice
                     if (!((node.tag.model & Dict.CM_HEAD) != 0))
                     {
                         lexer.report.warning(lexer, body, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     }
 
                     if ((node.tag.model & Dict.CM_HTML) != 0)
                     {
                         // copy body attributes if current body was inferred
                         if (node.is(TagId.BODY) && body.implicit && body.attributes == null)
                         {
                             body.attributes = node.attributes;
                             node.attributes = null;
                         }
 
                         continue;
                     }
 
                     if ((node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         moveToHead(lexer, body, node);
                         continue;
                     }
 
                     if ((node.tag.model & Dict.CM_LIST) != 0)
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.UL);
                         lexer.addClassNoIndent(node);
                         lexer.excludeBlocks = true;
                     }
                     else if ((node.tag.model & Dict.CM_DEFLIST) != 0)
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.DL);
                         lexer.excludeBlocks = true;
                     }
                     else if ((node.tag.model & (Dict.CM_TABLE | Dict.CM_ROWGRP | Dict.CM_ROW)) != 0)
                     {
                     	// Issue 2855511
                         if (node.type != NodeType.EndTag) {
 	                        lexer.ungetToken();
 	                        node = lexer.inferredTag(TagId.TABLE);
                         }
                         lexer.excludeBlocks = true;
                     }
                     else if (node.is(TagId.INPUT))
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.FORM);
                         lexer.excludeBlocks = true;
                     }
                     else
                     {
                         if (!((node.tag.model & (Dict.CM_ROW | Dict.CM_FIELD)) != 0))
                         {
                             lexer.ungetToken();
                             return;
                         }
 
                         // ignore </td></th> <option> etc.
                         continue;
                     }
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     if (node.is(TagId.BR))
                     {
                         node.type = NodeType.StartTag;
                     }
                     else if (node.is(TagId.P))
                     {
                     	node.type = NodeType.StartEndTag;
                     	node.implicit = true;
                     }
                     else if ((node.tag.model & Dict.CM_INLINE) != 0)
                     {
                         lexer.popInline(node);
                     }
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (((node.tag.model & Dict.CM_INLINE) != 0) && !((node.tag.model & Dict.CM_MIXED) != 0))
                     {
                         // HTML4 strict doesn't allow inline content here
                         // but HTML2 does allow img elements as children of body
                         if (node.is(TagId.IMG))
                         {
                             lexer.constrainVersion(~VERS_HTML40_STRICT);
                         }
                         else
                         {
                             lexer.constrainVersion(~(VERS_HTML40_STRICT | VERS_HTML20));
                         }
 
                         if (checkstack && !node.implicit)
                         {
                             checkstack = false;
 
                             if (lexer.inlineDup(node) > 0)
                             {
                                 continue;
                             }
                         }
 
                         mode = Lexer.MIXED_CONTENT;
                     }
                     else
                     {
                         checkstack = true;
                         mode = Lexer.IGNORE_WHITESPACE;
                     }
 
                     if (node.implicit)
                     {
                         lexer.report.warning(lexer, body, node, ErrorCode.INSERTING_TAG);
                     }
 
                     body.insertNodeAtEnd(node);
                     parseTag(lexer, node, mode);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, body, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
         }
 
     }
 
     /**
      * Parser for FRAMESET.
      */
     public static class ParseFrameSet implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node frameset, short mode)
         {
             Node node;
 
             lexer.badAccess |= Report.USING_FRAMES;
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == frameset.tag && node.type == NodeType.EndTag)
                 {
                     frameset.closed = true;
                     Node.trimSpaces(lexer, frameset);
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(frameset, node))
                 {
                     continue;
                 }
 
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, frameset, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (node.tag != null && (node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         moveToHead(lexer, frameset, node);
                         continue;
                     }
                 }
 
                 if (node.is(TagId.BODY))
                 {
                     lexer.ungetToken();
                     node = lexer.inferredTag(TagId.NOFRAMES);
                     lexer.report.warning(lexer, frameset, node, ErrorCode.INSERTING_TAG);
                 }
 
                 if (node.type == NodeType.StartTag && (node.tag.model & Dict.CM_FRAMES) != 0)
                 {
                     frameset.insertNodeAtEnd(node);
                     lexer.excludeBlocks = false;
                     parseTag(lexer, node, Lexer.MIXED_CONTENT);
                     continue;
                 }
                 else if (node.type == NodeType.StartEndTag && (node.tag.model & Dict.CM_FRAMES) != 0)
                 {
                     frameset.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, frameset, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
 
             lexer.report.warning(lexer, frameset, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
 
     }
 
     /**
      * Parser for INLINE.
      */
     public static class ParseInline implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node element, short mode)
         {
             Node node, parent;
             TagTable tt = lexer.configuration.tt;
 
             if (TidyUtils.toBoolean(element.tag.model & Dict.CM_EMPTY))
             {
                 return;
             }
 
             // ParseInline is used for some block level elements like H1 to H6
             // For such elements we need to insert inline emphasis tags currently on the inline stack.
             // For Inline elements, we normally push them onto the inline stack
             // provided they aren't implicit or OBJECT/APPLET.
             // This test is carried out in PushInline and PopInline, see istack.c
             if ((element.hasCM(Dict.CM_BLOCK) || element.is(TagId.DT)) && !element.hasCM(Dict.CM_MIXED)) {
                 lexer.inlineDup(null);
             }
             else if (element.hasCM(Dict.CM_INLINE)) {
                 lexer.pushInline(element);
             }
 
             if (element.is(TagId.NOBR))
             {
                 lexer.badLayout |= Report.USING_NOBR;
             }
             else if (element.is(TagId.FONT))
             {
                 lexer.badLayout |= Report.USING_FONT;
             }
 
             // Inline elements may or may not be within a preformatted element
             if (mode != Lexer.PREFORMATTED)
             {
                 mode = Lexer.MIXED_CONTENT;
             }
 
             while ((node = lexer.getToken(mode)) != null)
             {
                 // end tag for current element
                 if (node.tag == element.tag && node.type == NodeType.EndTag)
                 {
                     if (TidyUtils.toBoolean(element.tag.model & Dict.CM_INLINE))
                     {
                         lexer.popInline(node);
                     }
 
                     if (!TidyUtils.toBoolean(mode & Lexer.PREFORMATTED))
                     {
                         Node.trimSpaces(lexer, element);
                     }
 
                     // if a font element wraps an anchor and nothing else then move the font element inside the anchor
                     // since otherwise it won't alter the anchor text color
 
                     if (element.is(TagId.FONT) && element.content != null && element.content == element.last)
                     {
                         Node child = element.content;
 
                         if (child.is(TagId.A))
                         {
                             child.parent = element.parent;
                             child.next = element.next;
                             child.prev = element.prev;
 
                             if (child.prev != null)
                             {
                                 child.prev.next = child;
                             }
                             else
                             {
                                 child.parent.content = child;
                             }
 
                             if (child.next != null)
                             {
                                 child.next.prev = child;
                             }
                             else
                             {
                                 child.parent.last = child;
                             }
 
                             element.next = null;
                             element.prev = null;
                             element.parent = child;
                             element.content = child.content;
                             element.last = child.last;
                             child.content = element;
                             child.last = element;
                             for (child = element.content; child != null; child = child.next)
                             {
                                 child.parent = element;
                             }
                         }
                     }
                     element.closed = true;
                     Node.trimSpaces(lexer, element);
                     return;
                 }
 
                 // <u> ... <u> map 2nd <u> to </u> if 1st is explicit
                 // otherwise emphasis nesting is probably unintentional
                 // big and small have cumulative effect to leave them alone
                 if (node.type == NodeType.StartTag
                     && node.tag == element.tag
                     && lexer.isPushed(node)
                     && !node.implicit
                     && !element.implicit
                     && node.tag != null
                     && ((node.tag.model & Dict.CM_INLINE) != 0)
                     && !node.is(TagId.A)
                     && !node.is(TagId.FONT)
                     && !node.is(TagId.BIG)
                     && !node.is(TagId.SMALL)
                	&& !node.is(TagId.SUB)
                    && !node.is(TagId.SUP)
                    && !node.is(TagId.Q)
                    && !node.is(TagId.SPAN))
                 {
                     if (element.content != null && node.attributes == null
                     		&& element.last.isText() && !lexer.textNodeEndWithSpace(element.last)) {
                         lexer.report.warning(lexer, element, node, ErrorCode.COERCE_TO_ENDTAG);
                         node.type = NodeType.EndTag;
                         lexer.ungetToken();
                         continue;
                     }
 
                     if (node.attributes == null || element.attributes == null) {
                     	lexer.report.warning(lexer, element, node, ErrorCode.NESTED_EMPHASIS);
                     }
                 }
                 else if (lexer.isPushed(node) && node.type == NodeType.StartTag && node.is(TagId.Q))
                 {
                     lexer.report.warning(lexer, element, node, ErrorCode.NESTED_QUOTATION);
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     // only called for 1st child
                     if (element.content == null && !TidyUtils.toBoolean(mode & Lexer.PREFORMATTED))
                     {
                         Node.trimSpaces(lexer, element);
                     }
 
                     if (node.start >= node.end)
                     {
                         continue;
                     }
 
                     element.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // mixed content model so allow text
                 if (Node.insertMisc(element, node))
                 {
                     continue;
                 }
 
                 // deal with HTML tags
                 if (node.is(TagId.HTML))
                 {
                     if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     // otherwise infer end of inline element
                     lexer.ungetToken();
                     if (!((mode & Lexer.PREFORMATTED) != 0))
                     {
                         Node.trimSpaces(lexer, element);
                     }
                     return;
                 }
 
                 // within <dt> or <pre> map <p> to <br>
                 if (node.is(TagId.P)
                     && node.type == NodeType.StartTag
                     && ((mode & Lexer.PREFORMATTED) != 0 || element.is(TagId.DT) || element.isDescendantOf(TagId.DT)))
                 {
                     node.tag = tt.lookup(TagId.BR);
                     node.element = "br";
                     Node.trimSpaces(lexer, element);
                     element.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // ignore unknown and PARAM tags
                 if (node.tag == null || node.is(TagId.PARAM))
                 {
                     lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (node.is(TagId.BR) && node.type == NodeType.EndTag)
                 {
                     node.type = NodeType.StartTag;
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     // coerce </br> to <br>
                     if (node.is(TagId.BR))
                     {
                         node.type = NodeType.StartTag;
                     }
                     else if (node.is(TagId.P))
                     {
                         // coerce unmatched </p> to <br><br>
                         if (!element.isDescendantOf(TagId.P))
                         {
                             Node.coerceNode(lexer, node, TagId.BR, false, false);
                             Node.trimSpaces(lexer, element);
                             element.insertNodeAtEnd(node);
                             node = lexer.inferredTag(TagId.BR);
                             continue;
                         }
                     }
                     else if (node.hasCM(Dict.CM_INLINE) && !node.is(TagId.A)
                     		&& !node.hasCM(Dict.CM_OBJECT) && element.hasCM(Dict.CM_INLINE)) {
                         /* retain an earlier inline element.
                            This is implemented by setting the lexer into a mode
                            where it gets tokens from the inline stack rather than
                            from the input stream. Check if the scenerio fits. */
                     	if (!element.is(TagId.A) && node.tag != element.tag
                     			&& lexer.isPushed(node) && lexer.isPushed(element)) {
                             /* we have something like
                                <b>bold <i>bold and italic</b> italics</i> */
                     		if (lexer.switchInline(element, node)) {
                     			lexer.report.warning(lexer, element, node, ErrorCode.NON_MATCHING_ENDTAG);
                     			lexer.ungetToken(); /* put this back */
                     			lexer.inlineDup1(null, element); /* dupe the <i>, after </b> */
                     			if ((mode & Lexer.PREFORMATTED) == 0) {
                     				Node.trimSpaces(lexer, element);
                     			}
                     			return; /* close <i>, but will re-open it, after </b> */
                     		}
                     	}
                     	
                         // allow any inline end tag to end current element
                         lexer.popInline(element);
 
                         if (!element.is(TagId.A))
                         {
                             if (node.is(TagId.A) && node.tag != element.tag)
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                                 lexer.ungetToken();
                             }
                             else
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.NON_MATCHING_ENDTAG);
                             }
 
                             if (!((mode & Lexer.PREFORMATTED) != 0))
                             {
                                 Node.trimSpaces(lexer, element);
                             }
                             return;
                         }
 
                         // if parent is <a> then discard unexpected inline end tag
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     } // special case </tr> etc. for stuff moved in front of table
                     else if (lexer.exiled && node.tag.model != 0 && (node.tag.model & Dict.CM_TABLE) != 0)
                     {
                         lexer.ungetToken();
                         Node.trimSpaces(lexer, element);
                         return;
                     }
                 }
 
                 // allow any header tag to end current header
                 if ((node.tag.model & Dict.CM_HEADING) != 0 && (element.tag.model & Dict.CM_HEADING) != 0)
                 {
                     if (node.tag == element.tag)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.NON_MATCHING_ENDTAG);
                     }
                     else
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                         lexer.ungetToken();
                     }
                     if (!((mode & Lexer.PREFORMATTED) != 0))
                     {
                         Node.trimSpaces(lexer, element);
                     }
                     return;
                 }
 
                 // an <A> tag to ends any open <A> element but <A href=...> is mapped to </A><A href=...>
 
                 // #427827 - fix by Randy Waki and Bjoern Hoehrmann 23 Aug 00
                 // if (node.tag == tt.tagA && !node.implicit && lexer.isPushed(node))
                 if (node.is(TagId.A)
                     && !node.implicit
                     && (element.is(TagId.A) || element.isDescendantOf(TagId.A)))
                 {
                     // coerce <a> to </a> unless it has some attributes
                     // #427827 - fix by Randy Waki and Bjoern Hoehrmann 23 Aug 00
                     // other fixes by Dave Raggett
                     // if (node.attributes == null)
                     if (node.type != NodeType.EndTag && node.attributes == null)
                     {
                         node.type = NodeType.EndTag;
                         lexer.report.warning(lexer, element, node, ErrorCode.COERCE_TO_ENDTAG);
                         // lexer.popInline(node);
                         lexer.ungetToken();
                         continue;
                     }
 
                     lexer.ungetToken();
                     lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                     // lexer.popInline(element);
                     if (!((mode & Lexer.PREFORMATTED) != 0))
                     {
                         Node.trimSpaces(lexer, element);
                     }
                     return;
                 }
 
                 if ((element.tag.model & Dict.CM_HEADING) != 0)
                 {
                     if (node.is(TagId.CENTER) || node.is(TagId.DIV))
                     {
                         if (node.type != NodeType.StartTag && node.type != NodeType.StartEndTag)
                         {
                             lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                             continue;
                         }
 
                         lexer.report.warning(lexer, element, node, ErrorCode.TAG_NOT_ALLOWED_IN);
 
                         // insert center as parent if heading is empty
                         if (element.content == null)
                         {
                             Node.insertNodeAsParent(element, node);
                             continue;
                         }
 
                         // split heading and make center parent of 2nd part
                         element.insertNodeAfterElement(node);
 
                         if (!((mode & Lexer.PREFORMATTED) != 0))
                         {
                             Node.trimSpaces(lexer, element);
                         }
 
                         element = lexer.cloneNode(element);
                         element.start = lexer.lexsize;
                         element.end = lexer.lexsize;
                         node.insertNodeAtEnd(element);
                         continue;
                     }
 
                     if (node.is(TagId.HR))
                     {
                         if (node.type != NodeType.StartTag && node.type != NodeType.StartEndTag)
                         {
                             lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                             continue;
                         }
 
                         lexer.report.warning(lexer, element, node, ErrorCode.TAG_NOT_ALLOWED_IN);
 
                         // insert hr before heading if heading is empty
                         if (element.content == null)
                         {
                             Node.insertNodeBeforeElement(element, node);
                             continue;
                         }
 
                         // split heading and insert hr before 2nd part
                         element.insertNodeAfterElement(node);
 
                         if (!((mode & Lexer.PREFORMATTED) != 0))
                         {
                             Node.trimSpaces(lexer, element);
                         }
 
                         element = lexer.cloneNode(element);
                         element.start = lexer.lexsize;
                         element.end = lexer.lexsize;
                         node.insertNodeAfterElement(element);
                         continue;
                     }
                 }
 
                 if (element.is(TagId.DT))
                 {
                     if (node.is(TagId.HR))
                     {
                         Node dd;
 
                         if (node.type != NodeType.StartTag && node.type != NodeType.StartEndTag)
                         {
                             lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                             continue;
                         }
 
                         lexer.report.warning(lexer, element, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         dd = lexer.inferredTag(TagId.DD);
 
                         // insert hr within dd before dt if dt is empty
                         if (element.content == null)
                         {
                             Node.insertNodeBeforeElement(element, dd);
                             dd.insertNodeAtEnd(node);
                             continue;
                         }
 
                         // split dt and insert hr within dd before 2nd part
                         element.insertNodeAfterElement(dd);
                         dd.insertNodeAtEnd(node);
 
                         if (!((mode & Lexer.PREFORMATTED) != 0))
                         {
                             Node.trimSpaces(lexer, element);
                         }
 
                         element = lexer.cloneNode(element);
                         element.start = lexer.lexsize;
                         element.end = lexer.lexsize;
                         dd.insertNodeAfterElement(element);
                         continue;
                     }
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
                     for (parent = element.parent; parent != null; parent = parent.parent)
                     {
                         if (node.tag == parent.tag)
                         {
                             if (!((element.tag.model & Dict.CM_OPT) != 0) && !element.implicit)
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                             }
 
                             if (element.is(TagId.A))
                             {
                                 lexer.popInline(element);
                             }
 
                             lexer.ungetToken();
 
                             if (!((mode & Lexer.PREFORMATTED) != 0))
                             {
                                 Node.trimSpaces(lexer, element);
                             }
 
                             return;
                         }
                     }
                 }
 
                 // block level tags end this element
                 if (!((node.tag.model & Dict.CM_INLINE) != 0))
                 {
                     if (node.type != NodeType.StartTag)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (!((element.tag.model & Dict.CM_OPT) != 0))
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                     }
 
                     if ((node.tag.model & Dict.CM_HEAD) != 0 && !((node.tag.model & Dict.CM_BLOCK) != 0))
                     {
                         moveToHead(lexer, element, node);
                         continue;
                     }
 
                     // prevent anchors from propagating into block tags except for headings h1 to h6
 
                     if (element.is(TagId.A))
                     {
                         if (node.tag != null && !((node.tag.model & Dict.CM_HEADING) != 0))
                         {
                             lexer.popInline(element);
                         }
                         else if (!(element.content != null))
                         {
                             Node.discardElement(element);
                             lexer.ungetToken();
                             return;
                         }
                     }
 
                     lexer.ungetToken();
 
                     if (!((mode & Lexer.PREFORMATTED) != 0))
                     {
                         Node.trimSpaces(lexer, element);
                     }
 
                     return;
                 }
 
                 // parse inline element
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (node.implicit)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.INSERTING_TAG);
                     }
 
                     // trim white space before <br>
                     if (node.is(TagId.BR))
                     {
                         Node.trimSpaces(lexer, element);
                     }
 
                     element.insertNodeAtEnd(node);
                     parseTag(lexer, node, mode);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                 continue;
             }
 
             if (!((element.tag.model & Dict.CM_OPT) != 0))
             {
                 lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_FOR);
             }
         }
     }
     
     private static Node findLastLI(final Node list) {
     	Node lastli = null;
         for (Node node = list.content; node != null; node = node.next) {
             if (node.is(TagId.LI) && node.type == NodeType.StartTag) {
                 lastli = node;
             }
         }
         return lastli;
     }
 
     /**
      * Parser for LIST.
      */
     public static class ParseList implements Parser
     {
 
         public void parse(Lexer lexer, Node list, short mode)
         {
             Node node;
             Node parent;
 
             if ((list.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             lexer.insert = -1; // defer implicit inline start tags
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == list.tag && node.type == NodeType.EndTag)
                 {
                     list.closed = true;
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(list, node))
                 {
                     continue;
                 }
 
                 if (node.type != NodeType.TextNode && node.tag == null)
                 {
                     lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
                     if (node.is(TagId.FORM))
                     {
                         badForm(lexer);
                         lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (node.tag != null && (node.tag.model & Dict.CM_INLINE) != 0)
                     {
                         lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                         lexer.popInline(node);
                         continue;
                     }
 
                     for (parent = list.parent; parent != null; parent = parent.parent)
                     {
                     	if (parent.is(TagId.BODY)) {
                     		break;
                     	}
                         if (node.tag == parent.tag)
                         {
                             lexer.report.warning(lexer, list, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                             lexer.ungetToken();
                             return;
                         }
                     }
 
                     lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (!node.is(TagId.LI))
                 {
                     lexer.ungetToken();
 
                     if (node.tag != null && (node.tag.model & Dict.CM_BLOCK) != 0 && lexer.excludeBlocks)
                     {
                         lexer.report.warning(lexer, list, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                         return;
                     }
 
                     /* http://tidy.sf.net/issue/836462
                     If "list" is an unordered list, insert the next tag within 
                     the last <li> to preserve the numbering to match the visual 
                     rendering of most browsers. */
                     final Node lastli = findLastLI(list);
                     if (list.is(TagId.OL) && lastli != null) {
                         /* Create a node for error reporting */
                 		node = lexer.inferredTag(TagId.LI);
                 		lexer.report.warning(lexer, list, node, ErrorCode.MISSING_STARTTAG);
                         node = lastli;
                     } else {
                         /* Add an inferred <li> */
                         final boolean wasblock = node.hasCM(Dict.CM_BLOCK);
                         node = lexer.inferredTag(TagId.LI);
                         /* Add "display: inline" to avoid a blank line after <li> with 
                            Internet Explorer. See http://tidy.sf.net/issue/836462 */
                         Clean.addStyleProperty(node, wasblock ? "list-style: none; display: inline"
                         		: "list-style: none");
                         lexer.report.warning(lexer, list, node, ErrorCode.MISSING_STARTTAG);
                         list.insertNodeAtEnd(node);
                     }
                 } else {
                 	// node should be <LI>
                 	list.insertNodeAtEnd(node);
                 }
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
 
             lexer.report.warning(lexer, list, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
     }
 
     /**
      * Parser for empty elements.
      */
     public static class ParseEmpty implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node element, short mode)
         {
             if (lexer.isvoyager)
             {
                 Node node = lexer.getToken(mode);
                 if (node != null && !(node.type == NodeType.EndTag && node.tag == element.tag))
                 {
                     lexer.report.warning(lexer, element, node, ErrorCode.ELEMENT_NOT_EMPTY);
                     lexer.ungetToken();
                 }
             }
         }
     }
 
     /**
      * Parser for DEFLIST.
      */
     public static class ParseDefList implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node list, short mode)
         {
             Node node, parent;
 
             if ((list.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             lexer.insert = -1; // defer implicit inline start tags
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == list.tag && node.type == NodeType.EndTag)
                 {
                     list.closed = true;
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(list, node))
                 {
                     continue;
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     lexer.ungetToken();
                     node = lexer.inferredTag(TagId.DT);
                     lexer.report.warning(lexer, list, node, ErrorCode.MISSING_STARTTAG);
                 }
 
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
                 	boolean discardIt = false;
                     if (node.is(TagId.FORM))
                     {
                         badForm(lexer);
                         lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     for (parent = list.parent; parent != null; parent = parent.parent)
                     {
                     	if (parent.is(TagId.BODY)) {
                     		discardIt = true;
                     		break;
                     	}
                         if (node.tag == parent.tag)
                         {
                             lexer.report.warning(lexer, list, node, ErrorCode.MISSING_ENDTAG_BEFORE);
 
                             lexer.ungetToken();
                             return;
                         }
                     }
                     if (discardIt) {
                         lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
                 }
 
                 // center in a dt or a dl breaks the dl list in two
                 if (node.is(TagId.CENTER))
                 {
                     if (list.content != null)
                     {
                         list.insertNodeAfterElement(node);
                     }
                     else
                     {
                         // trim empty dl list
                         Node.insertNodeBeforeElement(list, node);
 
                         // #540296 tidy dumps with empty definition list
                         Node.discardElement(list);
                     }
 
                     // and parse contents of center
                     lexer.excludeBlocks = false;
                     parseTag(lexer, node, mode);
                     lexer.excludeBlocks = true;
 
                     // now create a new dl element
                     list = lexer.inferredTag(TagId.DL);
                     node.insertNodeAfterElement(list);
                     continue;
                 }
 
                 if (!(node.is(TagId.DT) || node.is(TagId.DD)))
                 {
                     lexer.ungetToken();
 
                     if (!((node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0))
                     {
                         lexer.report.warning(lexer, list, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         return;
                     }
 
                     // if DD appeared directly in BODY then exclude blocks
                     if (!((node.tag.model & Dict.CM_INLINE) != 0) && lexer.excludeBlocks)
                     {
                         return;
                     }
 
                     node = lexer.inferredTag(TagId.DD);
                     lexer.report.warning(lexer, list, node, ErrorCode.MISSING_STARTTAG);
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     lexer.report.warning(lexer, list, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // node should be <DT> or <DD>
                 list.insertNodeAtEnd(node);
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
 
             lexer.report.warning(lexer, list, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
     }
 
     /**
      * Parser for PRE.
      */
     public static class ParsePre implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node pre, short mode)
         {
             Node node;
 
             if ((pre.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             lexer.inlineDup(null); // tell lexer to insert inlines if needed
 
             while ((node = lexer.getToken(Lexer.PREFORMATTED)) != null) {
             	if (node.type == NodeType.EndTag && (node.tag == pre.tag || pre.isDescendantOf(node.getId()))) {
             		if (node.tag != pre.tag) {
             			lexer.report.warning(lexer, pre, node, ErrorCode.MISSING_ENDTAG_BEFORE);
             			lexer.ungetToken();
             		}
                     Node.trimSpaces(lexer, pre);
                     pre.closed = true;
                     return;
                 }
 
                 if (node.is(TagId.HTML))
                 {
                     if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                     {
                         lexer.report.warning(lexer, pre, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
 
                     continue;
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     // if first check for inital newline
                     if (pre.content == null)
                     {
                         if (node.textarray[node.start] == (byte) '\n')
                         {
                             ++node.start;
                         }
 
                         if (node.start >= node.end)
                         {
                             continue;
                         }
                     }
 
                     pre.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(pre, node))
                 {
                     continue;
                 }
                 
                 if (node.tag == null) {
                     lexer.report.warning(lexer, pre, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // strip unexpected tags
                 if (!lexer.preContent(node))
                 {
                     Node newnode;
 
                     lexer.report.warning(lexer, pre, node, ErrorCode.UNESCAPED_ELEMENT);
                     newnode = Node.escapeTag(lexer, node);
                     pre.insertNodeAtEnd(newnode);
                     continue;
                 }
 
                 if (node.is(TagId.P))
                 {
                     if (node.type == NodeType.StartTag)
                     {
                         lexer.report.warning(lexer, pre, node, ErrorCode.USING_BR_INPLACE_OF);
 
                         // trim white space before <p> in <pre>
                         Node.trimSpaces(lexer, pre);
 
                         // coerce both <p> and </p> to <br>
                         Node.coerceNode(lexer, node, TagId.BR, false, false);
                         pre.insertNodeAtEnd(node);
                     }
                     else
                     {
                         lexer.report.warning(lexer, pre, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
                     continue;
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     // trim white space before <br>
                     if (node.is(TagId.BR))
                     {
                         Node.trimSpaces(lexer, pre);
                     }
 
                     pre.insertNodeAtEnd(node);
                     parseTag(lexer, node, Lexer.PREFORMATTED);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, pre, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
 
             lexer.report.warning(lexer, pre, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
     }
 
     /**
      * Parser for block elements.
      */
     public static class ParseBlock implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node element, short mode)
         {
             // element is node created by the lexer upon seeing the start tag, or by the parser when the start tag is
             // inferred.
             Node node, parent;
             boolean checkstack;
             int istackbase = 0;
             TagTable tt = lexer.configuration.tt;
 
             checkstack = true;
 
             if ((element.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             if (element.is(TagId.FORM) && element.isDescendantOf(TagId.FORM))
             {
                 lexer.report.warning(lexer, element, null, ErrorCode.ILLEGAL_NESTING);
             }
 
             // InlineDup() asks the lexer to insert inline emphasis tags currently pushed on the istack, but take care
             // to avoid propagating inline emphasis inside OBJECT or APPLET. For these elements a fresh inline stack
             // context is created and disposed of upon reaching the end of the element. They thus behave like table
             // cells in this respect.
 
             if ((element.tag.model & Dict.CM_OBJECT) != 0)
             {
                 istackbase = lexer.istackbase;
                 lexer.istackbase = lexer.istack.size();
             }
 
             if (!((element.tag.model & Dict.CM_MIXED) != 0))
             {
                 lexer.inlineDup(null);
             }
 
             mode = Lexer.IGNORE_WHITESPACE;
 
             while ((node = lexer.getToken(mode)) != null)
             {
                 // end tag for this element
                 if (node.type == NodeType.EndTag
                     && node.tag != null
                     && (node.tag == element.tag || element.was == node.tag))
                 {
 
                     if ((element.tag.model & Dict.CM_OBJECT) != 0)
                     {
                         // pop inline stack
                         while (lexer.istack.size() > lexer.istackbase)
                         {
                             lexer.popInline(null);
                         }
                         lexer.istackbase = istackbase;
                     }
 
                     element.closed = true;
                     Node.trimSpaces(lexer, element);
                     return;
                 }
 
                 if (node.is(TagId.HTML) || node.is(TagId.HEAD) || node.is(TagId.BODY))
                 {
                     if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
 
                     continue;
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     if (node.tag == null)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
 
                         continue;
                     }
                     else if (node.is(TagId.BR))
                     {
                         node.type = NodeType.StartTag;
                     }
                     else if (node.is(TagId.P))
                     {
                     	node.type = NodeType.StartEndTag;
                     	node.implicit = true;
                     }
                     else
                     {
                         // if this is the end tag for an ancestor element then infer end tag for this element
 
                         for (parent = element.parent; parent != null; parent = parent.parent)
                         {
                             if (node.tag == parent.tag)
                             {
                                 if (!((element.tag.model & Dict.CM_OPT) != 0))
                                 {
                                     lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                                 }
 
                                 lexer.ungetToken();
 
                                 if ((element.tag.model & Dict.CM_OBJECT) != 0)
                                 {
                                     // pop inline stack
                                     while (lexer.istack.size() > lexer.istackbase)
                                     {
                                         lexer.popInline(null);
                                     }
                                     lexer.istackbase = istackbase;
                                 }
 
                                 Node.trimSpaces(lexer, element);
                                 return;
                             }
                         }
                         // special case </tr> etc. for stuff moved in front of table
                         if (lexer.exiled && node.tag.model != 0 && (node.tag.model & Dict.CM_TABLE) != 0)
                         {
                             lexer.ungetToken();
                             Node.trimSpaces(lexer, element);
                             return;
                         }
                     }
                 }
 
                 // mixed content model permits text
                 if (node.type == NodeType.TextNode)
                 {
                     if (checkstack)
                     {
                         checkstack = false;
 
                         if (!((element.tag.model & Dict.CM_MIXED) != 0))
                         {
                             if (lexer.inlineDup(node) > 0)
                             {
                                 continue;
                             }
                         }
                     }
 
                     element.insertNodeAtEnd(node);
                     mode = Lexer.MIXED_CONTENT;
 
                     // HTML4 strict doesn't allow mixed content for elements with %block; as their content model
                     // But only body, map, blockquote, form and noscript have content model %block;
                     if (element.is(TagId.BODY)
                         || element.is(TagId.MAP)
                         || element.is(TagId.BLOCKQUOTE)
                         || element.is(TagId.FORM)
                         || element.is(TagId.NOSCRIPT))
                     {
                         lexer.constrainVersion(~VERS_HTML40_STRICT);
                     }
                     continue;
                 }
 
                 if (Node.insertMisc(element, node))
                 {
                     continue;
                 }
 
                 // allow PARAM elements?
                 if (node.is(TagId.PARAM))
                 {
                     if (((element.tag.model & Dict.CM_PARAM) != 0)
                         && (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag))
                     {
                         element.insertNodeAtEnd(node);
                         continue;
                     }
 
                     // otherwise discard it
                     lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // allow AREA elements?
                 if (node.is(TagId.AREA))
                 {
                     if ((element.is(TagId.MAP)) && (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag))
                     {
                         element.insertNodeAtEnd(node);
                         continue;
                     }
 
                     // otherwise discard it
                     lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // ignore unknown start/end tags
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // Allow Dict.CM_INLINE elements here. Allow Dict.CM_BLOCK elements here unless lexer.excludeBlocks is
                 // yes. LI and DD are special cased. Otherwise infer end tag for this element.
 
                 if (!((node.tag.model & Dict.CM_INLINE) != 0))
                 {
                     if (node.type != NodeType.StartTag && node.type != NodeType.StartEndTag)
                     {
                         if (node.is(TagId.FORM))
                         {
                             badForm(lexer);
                         }
                         lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     // #427671 - Fix by Randy Waki - 10 Aug 00
                     // If an LI contains an illegal FRAME, FRAMESET, OPTGROUP, or OPTION start tag, discard the start
                     // tag and let the subsequent content get parsed as content of the enclosing LI. This seems to
                     // mimic IE and Netscape, and avoids an infinite loop: without this check, ParseBlock (which is
                     // parsing the LI's content) and ParseList (which is parsing the LI's parent's content) repeatedly
                     // defer to each other to parse the illegal start tag, each time inferring a missing </li> or <li>
                     // respectively. NOTE: This check is a bit fragile. It specifically checks for the four tags that
                     // happen to weave their way through the current series of tests performed by ParseBlock and
                     // ParseList to trigger the infinite loop.
 
                     if (element.is(TagId.LI))
                     {
                         if (node.is(TagId.FRAME)
                             || node.is(TagId.FRAMESET)
                             || node.is(TagId.OPTGROUP)
                             || node.is(TagId.OPTION))
                         {
                             lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                             continue;
                         }
                     }
 
                     if (element.is(TagId.TD) || element.is(TagId.TH))
                     {
                         // if parent is a table cell, avoid inferring the end of the cell
 
                         if ((node.tag.model & Dict.CM_HEAD) != 0)
                         {
                             moveToHead(lexer, element, node);
                             continue;
                         }
 
                         if ((node.tag.model & Dict.CM_LIST) != 0)
                         {
                             lexer.ungetToken();
                             node = lexer.inferredTag(TagId.UL);
                             lexer.addClassNoIndent(node);
                             lexer.excludeBlocks = true;
                         }
                         else if ((node.tag.model & Dict.CM_DEFLIST) != 0)
                         {
                             lexer.ungetToken();
                             node = lexer.inferredTag(TagId.DL);
                             lexer.excludeBlocks = true;
                         }
 
                         // infer end of current table cell
                         if (!((node.tag.model & Dict.CM_BLOCK) != 0))
                         {
                             lexer.ungetToken();
                             Node.trimSpaces(lexer, element);
                             return;
                         }
                     }
                     else if ((node.tag.model & Dict.CM_BLOCK) != 0)
                     {
                         if (lexer.excludeBlocks)
                         {
                             if (!((element.tag.model & Dict.CM_OPT) != 0))
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                             }
 
                             lexer.ungetToken();
 
                             if ((element.tag.model & Dict.CM_OBJECT) != 0)
                             {
                                 lexer.istackbase = istackbase;
                             }
 
                             Node.trimSpaces(lexer, element);
                             return;
                         }
                     }
                     else
                     {
                         // things like list items
 
                         if ((node.tag.model & Dict.CM_HEAD) != 0)
                         {
                             moveToHead(lexer, element, node);
                             continue;
                         }
 
                         // special case where a form start tag occurs in a tr and is followed by td or th
                         if (element.is(TagId.FORM) && element.parent.is(TagId.TD) && element.parent.implicit)
                         {
                             if (node.is(TagId.TD))
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                                 continue;
                             }
 
                             if (node.is(TagId.TH))
                             {
                                 lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                                 node = element.parent;
                                 node.element = "th";
                                 node.tag = tt.lookup(TagId.TH);
                                 continue;
                             }
                         }
 
                         if (!((element.tag.model & Dict.CM_OPT) != 0) && !element.implicit)
                         {
                             lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                         }
 
                         lexer.ungetToken();
 
                         if ((node.tag.model & Dict.CM_LIST) != 0)
                         {
                             if (element.parent != null
                                 && element.parent.tag != null
                                 && element.parent.tag.getParser() == LIST)
                             {
                                 Node.trimSpaces(lexer, element);
                                 return;
                             }
 
                             node = lexer.inferredTag(TagId.UL);
                             lexer.addClassNoIndent(node);
                         }
                         else if ((node.tag.model & Dict.CM_DEFLIST) != 0)
                         {
                             if (element.parent.is(TagId.DL))
                             {
                                 Node.trimSpaces(lexer, element);
                                 return;
                             }
 
                             node = lexer.inferredTag(TagId.DL);
                         }
                         else if ((node.tag.model & Dict.CM_TABLE) != 0 || (node.tag.model & Dict.CM_ROW) != 0)
                         {
                             node = lexer.inferredTag(TagId.TABLE);
                         }
                         else if ((element.tag.model & Dict.CM_OBJECT) != 0)
                         {
                             // pop inline stack
                             while (lexer.istack.size() > lexer.istackbase)
                             {
                                 lexer.popInline(null);
                             }
                             lexer.istackbase = istackbase;
                             Node.trimSpaces(lexer, element);
                             return;
 
                         }
                         else
                         {
                             Node.trimSpaces(lexer, element);
                             return;
                         }
                     }
                 }
 
                 // parse known element
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     if (TidyUtils.toBoolean(node.tag.model & Dict.CM_INLINE))
                     {
                         if (checkstack && !node.implicit)
                         {
                             checkstack = false;
 
                             // #431731 - fix by Randy Waki 25 Dec 00
                             if (!TidyUtils.toBoolean(element.tag.model & Dict.CM_MIXED))
                             {
                                 if (lexer.inlineDup(node) > 0)
                                 {
                                     continue;
                                 }
                             }
                         }
 
                         mode = Lexer.MIXED_CONTENT;
                     }
                     else
                     {
                         checkstack = true;
                         mode = Lexer.IGNORE_WHITESPACE;
                     }
 
                     // trim white space before <br>
                     if (node.is(TagId.BR))
                     {
                         Node.trimSpaces(lexer, element);
                     }
 
                     element.insertNodeAtEnd(node);
 
                     if (node.implicit)
                     {
                         lexer.report.warning(lexer, element, node, ErrorCode.INSERTING_TAG);
                     }
 
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE // Lexer.MixedContent
                     );
                     continue;
                 }
 
                 // discard unexpected tags
                 if (node.type == NodeType.EndTag)
                 {
                     lexer.popInline(node); // if inline end tag
                 }
 
                 lexer.report.warning(lexer, element, node, ErrorCode.DISCARDING_UNEXPECTED);
                 continue;
             }
 
             if (!((element.tag.model & Dict.CM_OPT) != 0))
             {
                 lexer.report.warning(lexer, element, node, ErrorCode.MISSING_ENDTAG_FOR);
             }
 
             if ((element.tag.model & Dict.CM_OBJECT) != 0)
             {
                 // pop inline stack
                 while (lexer.istack.size() > lexer.istackbase)
                 {
                     lexer.popInline(null);
                 }
                 lexer.istackbase = istackbase;
             }
 
             Node.trimSpaces(lexer, element);
         }
 
     }
 
     /**
      * Parser for TABLE.
      */
     public static class ParseTableTag implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node table, short mode)
         {
             Node node, parent;
             int istackbase;
 
             lexer.deferDup();
             istackbase = lexer.istackbase;
             lexer.istackbase = lexer.istack.size();
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == table.tag && node.type == NodeType.EndTag)
                 {
                     lexer.istackbase = istackbase;
                     table.closed = true;
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(table, node))
                 {
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null && node.type != NodeType.TextNode)
                 {
                     lexer.report.warning(lexer, table, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // if TD or TH or text or inline or block then infer <TR>
 
                 if (node.type != NodeType.EndTag)
                 {
                     if (node.is(TagId.TD) || node.is(TagId.TH) || node.is(TagId.TABLE))
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.TR);
                         lexer.report.warning(lexer, table, node, ErrorCode.MISSING_STARTTAG);
                     }
                     else if (node.type == NodeType.TextNode || (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0)
                     {
                         Node.insertNodeBeforeElement(table, node);
                         lexer.report.warning(lexer, table, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         lexer.exiled = true;
 
                         if (!(node.type == NodeType.TextNode)) // #427662 - was (!node.type == TextNode) - fix by Young
                         {
                             parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                         }
 
                         lexer.exiled = false;
                         continue;
                     }
                     else if ((node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         moveToHead(lexer, table, node);
                         continue;
                     }
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
                     if (node.is(TagId.FORM)) {
                         badForm(lexer);
                         lexer.report.warning(lexer, table, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if ((node.tag != null && (node.tag.model & (Dict.CM_TABLE | Dict.CM_ROW)) != 0)
                         || (node.tag != null && (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0))
                     {
                         lexer.report.warning(lexer, table, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     for (parent = table.parent; parent != null; parent = parent.parent)
                     {
                         if (node.tag == parent.tag)
                         {
                             lexer.report.warning(lexer, table, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                             lexer.ungetToken();
                             lexer.istackbase = istackbase;
                             return;
                         }
                     }
                 }
 
                 if (!((node.tag.model & Dict.CM_TABLE) != 0))
                 {
                     lexer.ungetToken();
                     lexer.report.warning(lexer, table, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     lexer.istackbase = istackbase;
                     return;
                 }
 
                 if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                 {
                     table.insertNodeAtEnd(node);
 
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                     continue;
                 }
 
                 // discard unexpected text nodes and end tags
                 lexer.report.warning(lexer, table, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
 
             lexer.report.warning(lexer, table, node, ErrorCode.MISSING_ENDTAG_FOR);
             lexer.istackbase = istackbase;
         }
 
     }
 
     /**
      * Parser for COLGROUP.
      */
     public static class ParseColGroup implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node colgroup, short mode)
         {
             Node node, parent;
 
             if ((colgroup.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == colgroup.tag && node.type == NodeType.EndTag)
                 {
                     colgroup.closed = true;
                     return;
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
                     if (node.is(TagId.FORM))
                     {
                         badForm(lexer);
                         lexer.report.warning(lexer, colgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     for (parent = colgroup.parent; parent != null; parent = parent.parent)
                     {
 
                         if (node.tag == parent.tag)
                         {
                             lexer.ungetToken();
                             return;
                         }
                     }
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     lexer.ungetToken();
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(colgroup, node))
                 {
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null)
                 {
                     lexer.report.warning(lexer, colgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (!node.is(TagId.COL))
                 {
                     lexer.ungetToken();
                     return;
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     lexer.report.warning(lexer, colgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // node should be <COL>
                 colgroup.insertNodeAtEnd(node);
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
         }
 
     }
 
     /**
      * Parser for ROWGROUP.
      */
     public static class ParseRowGroup implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node rowgroup, short mode)
         {
             Node node, parent;
 
             if ((rowgroup.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == rowgroup.tag)
                 {
                     if (node.type == NodeType.EndTag)
                     {
                         rowgroup.closed = true;
                         return;
                     }
 
                     lexer.ungetToken();
                     return;
                 }
 
                 // if </table> infer end tag
                 if (node.is(TagId.TABLE) && node.type == NodeType.EndTag)
                 {
                     lexer.ungetToken();
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(rowgroup, node))
                 {
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null && node.type != NodeType.TextNode)
                 {
                     lexer.report.warning(lexer, rowgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // if TD or TH then infer <TR> if text or inline or block move before table if head content move to
                 // head
 
                 if (node.type != NodeType.EndTag)
                 {
                     if (node.is(TagId.TD) || node.is(TagId.TH))
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.TR);
                         lexer.report.warning(lexer, rowgroup, node, ErrorCode.MISSING_STARTTAG);
                     }
                     else if (node.type == NodeType.TextNode || (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0)
                     {
                         Node.moveBeforeTable(rowgroup, node);
                         lexer.report.warning(lexer, rowgroup, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         lexer.exiled = true;
 
                         // #427662 was (!node.type == TextNode) fix by Young 04 Aug 00
                         if (node.type != NodeType.TextNode)
                         {
                             parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                         }
 
                         lexer.exiled = false;
                         continue;
                     }
                     else if ((node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         lexer.report.warning(lexer, rowgroup, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         moveToHead(lexer, rowgroup, node);
                         continue;
                     }
                 }
 
                 // if this is the end tag for ancestor element then infer end tag for this element
 
                 if (node.type == NodeType.EndTag)
                 {
 
                     if (node.is(TagId.FORM)
                         || (node.tag != null && (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0))
                     {
                         if (node.is(TagId.FORM))
                         {
                             badForm(lexer);
                         }
                         lexer.report.warning(lexer, rowgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (node.is(TagId.TR) || node.is(TagId.TD) || node.is(TagId.TH))
                     {
                         lexer.report.warning(lexer, rowgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     for (parent = rowgroup.parent; parent != null; parent = parent.parent)
                     {
                         if (node.tag == parent.tag)
                         {
                             lexer.ungetToken();
                             return;
                         }
                     }
 
                 }
 
                 // if THEAD, TFOOT or TBODY then implied end tag
 
                 if ((node.tag.model & Dict.CM_ROWGRP) != 0)
                 {
                     if (node.type != NodeType.EndTag)
                     {
                         lexer.ungetToken();
                         return;
                     }
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     lexer.report.warning(lexer, rowgroup, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 if (!(node.is(TagId.TR)))
                 {
                     node = lexer.inferredTag(TagId.TR);
                     lexer.report.warning(lexer, rowgroup, node, ErrorCode.MISSING_STARTTAG);
                     lexer.ungetToken();
                 }
 
                 // node should be <TR>
                 rowgroup.insertNodeAtEnd(node);
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
         }
     }
 
     /**
      * Parser for ROW.
      */
     public static class ParseRow implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node row, short mode)
         {
             Node node, parent;
             boolean excludeState;
 
             if ((row.tag.model & Dict.CM_EMPTY) != 0)
             {
                 return;
             }
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == row.tag)
                 {
                     if (node.type == NodeType.EndTag)
                     {
                         row.closed = true;
                         Node.fixEmptyRow(lexer, row);
                         return;
                     }
 
                     lexer.ungetToken();
                     Node.fixEmptyRow(lexer, row);
                     return;
                 }
 
                 // if this is the end tag for an ancestor element then infer end tag for this element
                 if (node.type == NodeType.EndTag)
                 {
                 	if ((node.hasCM(Dict.CM_HTML | Dict.CM_TABLE) || node.is(TagId.TABLE)
                             && row.isDescendantOf(node.getId()))) {
                 		lexer.ungetToken();
                 		return;
                 	}
                 	
                     if (node.is(TagId.FORM)
                         || (node.tag != null && (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0))
                     {
                         if (node.is(TagId.FORM))
                         {
                             badForm(lexer);
                         }
                         lexer.report.warning(lexer, row, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     if (node.is(TagId.TD) || node.is(TagId.TH))
                     {
                         lexer.report.warning(lexer, row, node, ErrorCode.DISCARDING_UNEXPECTED);
                         continue;
                     }
 
                     for (parent = row.parent; parent != null; parent = parent.parent)
                     {
                         if (node.tag == parent.tag)
                         {
                             lexer.ungetToken();
                             return;
                         }
                     }
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(row, node))
                 {
                     continue;
                 }
 
                 // discard unknown tags
                 if (node.tag == null && node.type != NodeType.TextNode)
                 {
                     lexer.report.warning(lexer, row, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // discard unexpected <table> element
                 if (node.is(TagId.TABLE))
                 {
                     lexer.report.warning(lexer, row, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // THEAD, TFOOT or TBODY
                 if (node.tag != null && (node.tag.model & Dict.CM_ROWGRP) != 0)
                 {
                     lexer.ungetToken();
                     return;
                 }
 
                 if (node.type == NodeType.EndTag)
                 {
                     lexer.report.warning(lexer, row, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // if text or inline or block move before table if head content move to head
 
                 if (node.type != NodeType.EndTag)
                 {
                     if (node.is(TagId.FORM))
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.TD);
                         lexer.report.warning(lexer, row, node, ErrorCode.MISSING_STARTTAG);
                     }
                     else if (node.type == NodeType.TextNode || (node.tag.model & (Dict.CM_BLOCK | Dict.CM_INLINE)) != 0)
                     {
                         Node.moveBeforeTable(row, node);
                         lexer.report.warning(lexer, row, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         lexer.exiled = true;
                         excludeState = lexer.excludeBlocks;
                         lexer.excludeBlocks = false;
 
                         if (node.type != NodeType.TextNode)
                         {
                             parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                         }
 
                         lexer.exiled = false;
                         lexer.excludeBlocks = excludeState;
                         continue;
                     }
                     else if ((node.tag.model & Dict.CM_HEAD) != 0)
                     {
                         lexer.report.warning(lexer, row, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                         moveToHead(lexer, row, node);
                         continue;
                     }
                 }
 
                 if (!(node.is(TagId.TD) || node.is(TagId.TH)))
                 {
                     lexer.report.warning(lexer, row, node, ErrorCode.TAG_NOT_ALLOWED_IN);
                     continue;
                 }
 
                 // node should be <TD> or <TH>
                 row.insertNodeAtEnd(node);
                 excludeState = lexer.excludeBlocks;
                 lexer.excludeBlocks = false;
                 parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                 lexer.excludeBlocks = excludeState;
 
                 // pop inline stack
 
                 while (lexer.istack.size() > lexer.istackbase)
                 {
                     lexer.popInline(null);
                 }
             }
         }
     }
 
     /**
      * Parser for NOFRAMES.
      */
     public static class ParseNoFrames implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node noframes, short mode)
         {
             Node node;
 
             lexer.badAccess |= Report.USING_NOFRAMES;
             mode = Lexer.IGNORE_WHITESPACE;
 
             while ((node = lexer.getToken(mode)) != null)
             {
                 if (node.tag == noframes.tag && node.type == NodeType.EndTag)
                 {
                     noframes.closed = true;
                     Node.trimSpaces(lexer, noframes);
                     return;
                 }
 
                 if ((node.is(TagId.FRAME) || node.is(TagId.FRAMESET)))
                 {
 
                     Node.trimSpaces(lexer, noframes);
 
                     // fix for [539369]
                     if (node.type == NodeType.EndTag)
                     {
                         lexer.report.warning(lexer, noframes, node, ErrorCode.DISCARDING_UNEXPECTED);
                         // Throw it away
                     }
                     else
                     {
                         lexer.report.warning(lexer, noframes, node, ErrorCode.MISSING_ENDTAG_BEFORE);
 
                         lexer.ungetToken();
                     }
                     return;
                 }
 
                 if (node.is(TagId.HTML))
                 {
                     if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
                     {
                         lexer.report.warning(lexer, noframes, node, ErrorCode.DISCARDING_UNEXPECTED);
                     }
 
                     continue;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(noframes, node))
                 {
                     continue;
                 }
 
                 if (node.is(TagId.BODY) && node.type == NodeType.StartTag)
                 {
                     boolean seenbody = lexer.seenEndBody;
                     noframes.insertNodeAtEnd(node);
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE); // MixedContent
 
                     if (seenbody)
                     {
                         Node.coerceNode(lexer, node, TagId.DIV, false, false);
                         moveNodeToBody(lexer, node);
                     }
                     continue;
                 }
 
                 // implicit body element inferred
                 if (node.type == NodeType.TextNode || (node.tag != null && node.type != NodeType.EndTag))
                 {
                 	Node body = lexer.root.findBody();
                     if (body != null || lexer.seenEndBody) {
                         if (body == null) {
                         	lexer.report.warning(lexer, noframes, node, ErrorCode.DISCARDING_UNEXPECTED);
                             continue;
                         }
                         if (node.type == NodeType.TextNode)
                         {
                             lexer.ungetToken();
                             node = lexer.inferredTag(TagId.P);
                             lexer.report.warning(lexer, noframes, node, ErrorCode.CONTENT_AFTER_BODY);
                         }
 
                         body.insertNodeAtEnd(node);
                     }
                     else
                     {
                         lexer.ungetToken();
                         node = lexer.inferredTag(TagId.BODY);
                         if (lexer.configuration.isXmlOut())
                         {
                             lexer.report.warning(lexer, noframes, node, ErrorCode.INSERTING_TAG);
                         }
                         noframes.insertNodeAtEnd(node);
                     }
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                     // MixedContent
                     continue;
                 }
                 // discard unexpected end tags
                 lexer.report.warning(lexer, noframes, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
 
             lexer.report.warning(lexer, noframes, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
 
     }
 
     /**
      * Parser for SELECT.
      */
     public static class ParseSelect implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node field, short mode)
         {
             Node node;
 
             lexer.insert = -1; // defer implicit inline start tags
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == field.tag && node.type == NodeType.EndTag)
                 {
                     field.closed = true;
                     Node.trimSpaces(lexer, field);
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(field, node))
                 {
                     continue;
                 }
 
                 if (node.type == NodeType.StartTag
                     && (node.is(TagId.OPTION) || node.is(TagId.OPTGROUP) || node.is(TagId.SCRIPT)))
                 {
                     field.insertNodeAtEnd(node);
                     parseTag(lexer, node, Lexer.IGNORE_WHITESPACE);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, field, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
 
             lexer.report.warning(lexer, field, node, ErrorCode.MISSING_ENDTAG_FOR);
         }
 
     }
 
     /**
      * Parser for text nodes.
      */
     public static class ParseText implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node field, short mode)
         {
             Node node;
 
             lexer.insert = -1; // defer implicit inline start tags
 
             if (field.is(TagId.TEXTAREA))
             {
                 mode = Lexer.PREFORMATTED;
             }
             else
             {
                 mode = Lexer.MIXED_CONTENT; // kludge for font tags
             }
 
             while ((node = lexer.getToken(mode)) != null)
             {
                 if (node.tag == field.tag && node.type == NodeType.EndTag)
                 {
                     field.closed = true;
                     Node.trimSpaces(lexer, field);
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(field, node))
                 {
                     continue;
                 }
 
                 if (node.type == NodeType.TextNode)
                 {
                     // only called for 1st child
                     if (field.content == null && !((mode & Lexer.PREFORMATTED) != 0))
                     {
                         Node.trimSpaces(lexer, field);
                     }
 
                     if (node.start >= node.end)
                     {
                         continue;
                     }
 
                     field.insertNodeAtEnd(node);
                     continue;
                 }
 
                 // for textarea should all cases of < and & be escaped?
                 // discard inline tags e.g. font
                 if (node.tag != null
                     && ((node.tag.model & Dict.CM_INLINE) != 0)
                     && (node.tag.model & Dict.CM_FIELD) == 0) // #487283 - fix by Lee Passey 25 Jan 02
                 {
                     lexer.report.warning(lexer, field, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
 
                 // terminate element on other tags
                 if (!((field.tag.model & Dict.CM_OPT) != 0))
                 {
                     lexer.report.warning(lexer, field, node, ErrorCode.MISSING_ENDTAG_BEFORE);
                 }
 
                 lexer.ungetToken();
                 Node.trimSpaces(lexer, field);
                 return;
             }
 
             if (!((field.tag.model & Dict.CM_OPT) != 0))
             {
                 lexer.report.warning(lexer, field, node, ErrorCode.MISSING_ENDTAG_FOR);
             }
         }
 
     }
 
     /**
      * Parser for OPTGROUP.
      */
     public static class ParseOptGroup implements Parser
     {
 
         /**
          * @see org.w3c.tidy.Parser#parse(org.w3c.tidy.Lexer, org.w3c.tidy.Node, short)
          */
         public void parse(Lexer lexer, Node field, short mode)
         {
             Node node;
 
             lexer.insert = -1; // defer implicit inline start tags
 
             while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
             {
                 if (node.tag == field.tag && node.type == NodeType.EndTag)
                 {
                     field.closed = true;
                     Node.trimSpaces(lexer, field);
                     return;
                 }
 
                 // deal with comments etc.
                 if (Node.insertMisc(field, node))
                 {
                     continue;
                 }
 
                 if (node.type == NodeType.StartTag && (node.is(TagId.OPTION) || node.is(TagId.OPTGROUP)))
                 {
                     if (node.is(TagId.OPTGROUP))
                     {
                         lexer.report.warning(lexer, field, node, ErrorCode.CANT_BE_NESTED);
                     }
 
                     field.insertNodeAtEnd(node);
                     parseTag(lexer, node, Lexer.MIXED_CONTENT);
                     continue;
                 }
 
                 // discard unexpected tags
                 lexer.report.warning(lexer, field, node, ErrorCode.DISCARDING_UNEXPECTED);
             }
         }
     }
     
     private static void replaceObsoleteElements(final Lexer lexer, Node node) {
         Node next;
 
         while (node != null) {
             next = node.next;
             if (node.is(TagId.DIR) || node.is(TagId.MENU)) {
                 Node.coerceNode(lexer, node, TagId.UL, true, true);
             }
             if (node.is(TagId.XMP) || node.is(TagId.LISTING) || node.is(TagId.PLAINTEXT)) {
             	Node.coerceNode(lexer, node, TagId.PRE, true, true);
             }
             if (node.content != null) {
                 replaceObsoleteElements(lexer, node.content);
             }
             node = next;
         }
     }
     
     private static void attributeChecks(final Lexer lexer, Node node) {
         Node next;
 
         while (node != null) {
             next = node.next;
 
             if (node.isElement()) {
                 if (node.tag.getChkattrs() != null) {
                     node.tag.getChkattrs().check(lexer, node);
                 } else {
                     node.checkAttributes(lexer);
                 }
             }
             if (node.content != null) {
                 attributeChecks(lexer, node.content);
             }
 
             assert(next != node); /* http://tidy.sf.net/issue/1603538 */
             node = next;
         }
     }
     
     private static Node dropEmptyElements(final Lexer lexer, Node node) {
         Node next;
 
         while (node != null) {
             next = node.next;
 
             if (node.content != null) {
                 dropEmptyElements(lexer, node.content);
             }
 
             if (!node.isElement() && !(node.isText() && !(node.start < node.end))) {
                 node = next;
                 continue;
             }
 
             next = Node.trimEmptyElement(lexer, node);
             node = next;
         }
         return node;
     }
     
     private static boolean isPreDescendant(final Node node) {
         Node parent = node.parent;
 
         while (parent != null) {
             if (parent.tag != null && parent.tag.getParser() == ParserImpl.PRE) {
                 return true;
             }
             parent = parent.parent;
         }
         return false;
     }
     
     private static boolean cleanTrailingWhitespace(final Lexer lexer, final Node node) {
         Node next;
 
         if (!node.isText()) {
             return false;
         }
         if (node.parent.type == NodeType.DocTypeTag) {
             return false;
         }
         if (isPreDescendant(node)) {
             return false;
         }
         if (node.parent.tag != null && node.parent.tag.getParser() == ParserImpl.SCRIPT) {
             return false;
         }
         next = node.next;
 
         /* <p>... </p> */
         if (next == null && !node.parent.hasCM(Dict.CM_INLINE)) {
             return true;
         }
         /* <div><small>... </small><h3>...</h3></div> */
         if (next == null && node.parent.next != null && !node.parent.next.hasCM(Dict.CM_INLINE)) {
             return true;
         }
         if (next == null) {
             return false;
         }
         if (next.is(TagId.BR)) {
             return true;
         }
         if (next.hasCM(Dict.CM_INLINE)) {
             return false;
         }
         /* <a href='/'>...</a> <p>...</p> */
         if (next.type == NodeType.StartTag) {
             return true;
         }
         /* <strong>...</strong> <hr /> */
         if (next.type == NodeType.StartEndTag) {
             return true;
         }
         /* evil adjacent text nodes, Tidy should not generate these :-( */
         if (next.isText() && next.start < next.end
             && TidyUtils.isWhite((char) lexer.lexbuf[next.start])) {
             return true;
         }
         return false;
     }
 
     private static boolean cleanLeadingWhitespace(final Node node) {
         if (!node.isText()) {
             return false;
         }
         if (node.parent.type == NodeType.DocTypeTag) {
             return false;
         }
         if (isPreDescendant(node)) {
             return false;
         }
         if (node.parent.tag != null && node.parent.tag.getParser() == ParserImpl.SCRIPT) {
             return false;
         }
         /* <p>...<br> <em>...</em>...</p> */
         if (node.prev != null && node.prev.is(TagId.BR)) {
             return true;
         }
         /* <p> ...</p> */
         if (node.prev == null && !node.parent.hasCM(Dict.CM_INLINE)) {
             return true;
         }
         /* <h4>...</h4> <em>...</em> */
         if (node.prev != null && !node.prev.hasCM(Dict.CM_INLINE) && node.prev.isElement()) {
             return true;
         }
         /* <p><span> ...</span></p> */
         if (node.prev == null && node.parent.prev == null && !node.parent.parent.hasCM(Dict.CM_INLINE)) {
             return true;
         }
         return false;
     }
     
     private static void cleanSpaces(final Lexer lexer, Node node) {
         Node next;
 
         while (node != null) {
             next = node.next;
 
             if (node.isText() && cleanLeadingWhitespace(node)) {
                 while (node.start < node.end && TidyUtils.isWhite((char) lexer.lexbuf[node.start])) {
                     ++(node.start);
                 }
             }
             if (node.isText() && cleanTrailingWhitespace(lexer, node)) {
                 while (node.end > node.start && TidyUtils.isWhite((char) lexer.lexbuf[node.end - 1])) {
                     --(node.end);
                 }
             }
             if (node.isText() && !(node.start < node.end)) {
             	node.removeNode();
                 node = next;
                 continue;
             }
             if (node.content != null) {
                 cleanSpaces(lexer, node.content);
             }
             node = next;
         }
     }
 
     private static boolean nodeCMIsOnlyInline(final Node node) {
         return node.hasCM(Dict.CM_INLINE) && !node.hasCM(Dict.CM_BLOCK);
     }
     
     private static void encloseBodyText(final Lexer lexer) {
         Node body = lexer.root.findBody();
         if (body == null) { 
             return;
         }
         Node node = body.content;
 
         while (node != null) {
             if ((node.isText() && !node.isBlank(lexer)) ||
             		(node.isElement() && nodeCMIsOnlyInline(node))) {
                 Node p = lexer.inferredTag(TagId.P);
                 Node.insertNodeBeforeElement(node, p);
                 while (node != null && (!node.isElement() || nodeCMIsOnlyInline(node))) {
                     Node next = node.next;
                     node.removeNode();
                     p.insertNodeAtEnd(node);
                     node = next;
                 }
                 Node.trimSpaces(lexer, p);
                 continue;
             }
             node = node.next;
         }
     }
 
     /* <form>, <blockquote> and <noscript> do not allow #PCDATA in
        HTML 4.01 Strict (%block; model instead of %flow;).
       When requested, text nodes in these elements are wrapped in <p>. */
     private static void encloseBlockText(final Lexer lexer, Node node) {
         while (node != null) {
             Node next = node.next;
 
             if (node.content != null) {
                 encloseBlockText(lexer, node.content);
             }
 
             if (!(node.is(TagId.FORM) || node.is(TagId.NOSCRIPT) ||
                   node.is(TagId.BLOCKQUOTE)) || node.content == null) {
                 node = next;
                 continue;
             }
             Node block = node.content;
 
             if ((block.isText() && !block.isBlank(lexer)) ||
             		(block.isElement() && nodeCMIsOnlyInline(block))) {
             	Node p = lexer.inferredTag(TagId.P);
                 Node.insertNodeBeforeElement(block, p);
                 while (block != null && (!block.isElement() || nodeCMIsOnlyInline(block))) {
                     Node tempNext = block.next;
                     block.removeNode();
                     p.insertNodeAtEnd(block);
                     block = tempNext;
                 }
                 Node.trimSpaces(lexer, p);
                 continue;
             }
             node = next;
         }
     }
 
     /**
      * HTML is the top level element.
      */
     public static Node parseDocument(Lexer lexer)
     {
         Node node, document, html;
         Node doctype = null;
 
         document = lexer.newNode();
         document.type = NodeType.RootNode;
 
         lexer.root = document;
 
         while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
         {
         	if (node.type == NodeType.XmlDecl) {
                 if (lexer.findXmlDecl() != null && lexer.root.content != null) {
                     lexer.report.warning(lexer, lexer.root, node, ErrorCode.DISCARDING_UNEXPECTED);
                     continue;
                 }
                 if (node.line != 1 || node.column != 1) {
                 	lexer.report.warning(lexer, lexer.root, node, ErrorCode.SPACE_PRECEDING_XMLDECL);
                 }
             }
         	
             // deal with comments etc.
             if (Node.insertMisc(document, node))
             {
                 continue;
             }
 
             if (node.type == NodeType.DocTypeTag)
             {
                 if (doctype == null)
                 {
                     document.insertNodeAtEnd(node);
                     doctype = node;
                 }
                 else
                 {
                     lexer.report.warning(lexer, document, node, ErrorCode.DISCARDING_UNEXPECTED);
                 }
                 continue;
             }
 
             if (node.type == NodeType.EndTag)
             {
                 lexer.report.warning(lexer, document, node, ErrorCode.DISCARDING_UNEXPECTED); // TODO?
                 continue;
             }
 
             if (node.type != NodeType.StartTag || !node.is(TagId.HTML))
             {
                 lexer.ungetToken();
                 html = lexer.inferredTag(TagId.HTML);
             }
             else
             {
                 html = node;
             }
 
             if (document.findDocType() == null) {
                 lexer.report.warning(lexer, null, null, ErrorCode.MISSING_DOCTYPE);
             }
 
             document.insertNodeAtEnd(html);
             HTML.parse(lexer, html, (short) 0); // TODO?
             break;
         }
         
         if (lexer.root.findHTML() == null) {
             /* a later check should complain if <body> is empty */
             html = lexer.inferredTag(TagId.HTML);
             lexer.root.insertNodeAtEnd(html);
             HTML.parse(lexer, html, Lexer.IGNORE_WHITESPACE);
         }
         
         if (lexer.root.findTITLE() == null) {
             Node head = lexer.root.findHEAD();
             lexer.report.warning(lexer, head, null, ErrorCode.MISSING_TITLE_ELEMENT);
             head.insertNodeAtEnd(lexer.inferredTag(TagId.TITLE));
         }
         
         replaceObsoleteElements(lexer, lexer.root);
         attributeChecks(lexer, lexer.root);
         dropEmptyElements(lexer, lexer.root);
         cleanSpaces(lexer, lexer.root);
         
         if (lexer.configuration.isEncloseBodyText()) {
             encloseBodyText(lexer);
         }
         if (lexer.configuration.isEncloseBlockText()) {
             encloseBlockText(lexer, lexer.root);
         }
         return document;
     }
 
     /**
      * Indicates whether or not whitespace should be preserved for this element. If an <code>xml:space</code>
      * attribute is found, then if the attribute value is <code>preserve</code>, returns <code>true</code>. For
      * any other value, returns <code>false</code>. If an <code>xml:space</code> attribute was <em>not</em>
      * found, then the following element names result in a return value of <code>true:
      *  pre, script, style,</code> and
      * <code>xsl:text</code>. Finally, if a <code>TagTable</code> was passed in and the element appears as the
      * "pre" element in the <code>TagTable</code>, then <code>true</code> will be returned. Otherwise,
      * <code>false</code> is returned.
      * @param element The <code>Node</code> to test to see if whitespace should be preserved.
      * @param tt The <code>TagTable</code> to test for the <code>getNodePre()</code> function. This may be
      * <code>null</code>, in which case this test is bypassed.
      * @return <code>true</code> or <code>false</code>, as explained above.
      */
     public static boolean XMLPreserveWhiteSpace(Node element, TagTable tt)
     {
         AttVal attribute;
 
         // search attributes for xml:space
         for (attribute = element.attributes; attribute != null; attribute = attribute.next)
         {
             if (attribute.attribute.equals("xml:space"))
             {
                 if (attribute.value.equals("preserve"))
                 {
                     return true;
                 }
 
                 return false;
             }
         }
 
         if (element.element == null) // Debian Bug #137124. Fix based on suggestion by Cesar Eduardo Barros 06 Mar 02
         {
             return false;
         }
 
         // kludge for html docs without explicit xml:space attribute
         if ("pre".equalsIgnoreCase(element.element)
             || "script".equalsIgnoreCase(element.element)
             || "style".equalsIgnoreCase(element.element))
         {
             return true;
         }
 
         if ((tt != null) && (tt.findParser(element) == PRE))
         {
             return true;
         }
 
         // kludge for XSL docs
         if ("xsl:text".equalsIgnoreCase(element.element))
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * XML documents.
      */
     public static void parseXMLElement(Lexer lexer, Node element, short mode)
     {
         Node node;
 
         // if node is pre or has xml:space="preserve" then do so
 
         if (XMLPreserveWhiteSpace(element, lexer.configuration.tt))
         {
             mode = Lexer.PREFORMATTED;
         }
 
         while ((node = lexer.getToken(mode)) != null)
         {
             if (node.type == NodeType.EndTag && node.element.equals(element.element))
             {
                 element.closed = true;
                 break;
             }
 
             // discard unexpected end tags
             if (node.type == NodeType.EndTag) {
             	if (element != null) {
             		lexer.report.error(lexer, element, node, ErrorCode.UNEXPECTED_ENDTAG_IN);
             	} else {
             		lexer.report.error(lexer, element, node, ErrorCode.UNEXPECTED_ENDTAG);
             	}
                 continue;
             }
 
             // parse content on seeing start tag
             if (node.type == NodeType.StartTag)
             {
                 parseXMLElement(lexer, node, mode);
             }
 
             element.insertNodeAtEnd(node);
         }
 
         // if first child is text then trim initial space and delete text node if it is empty.
 
         node = element.content;
 
         if (node != null && node.type == NodeType.TextNode && mode != Lexer.PREFORMATTED)
         {
             if (node.textarray[node.start] == (byte) ' ')
             {
                 node.start++;
 
                 if (node.start >= node.end)
                 {
                     Node.discardElement(node);
                 }
             }
         }
 
         // if last child is text then trim final space and delete the text node if it is empty
 
         node = element.last;
 
         if (node != null && node.type == NodeType.TextNode && mode != Lexer.PREFORMATTED)
         {
             if (node.textarray[node.end - 1] == (byte) ' ')
             {
                 node.end--;
 
                 if (node.start >= node.end)
                 {
                     Node.discardElement(node);
                 }
             }
         }
     }
 
     public static Node parseXMLDocument(Lexer lexer)
     {
         Node node, document, doctype;
 
         document = lexer.newNode();
         document.type = NodeType.RootNode;
         doctype = null;
         lexer.configuration.setXmlTags(true);
 
         while ((node = lexer.getToken(Lexer.IGNORE_WHITESPACE)) != null)
         {
             // discard unexpected end tags
             if (node.type == NodeType.EndTag)
             {
                 lexer.report.warning(lexer, null, node, ErrorCode.UNEXPECTED_ENDTAG);
                 continue;
             }
 
             // deal with comments etc.
             if (Node.insertMisc(document, node))
             {
                 continue;
             }
 
             if (node.type == NodeType.DocTypeTag)
             {
                 if (doctype == null)
                 {
                     document.insertNodeAtEnd(node);
                     doctype = node;
                 }
                 else
                 {
                     lexer.report.warning(lexer, document, node, ErrorCode.DISCARDING_UNEXPECTED); // TODO
                 }
                 continue;
             }
 
             if (node.type == NodeType.StartEndTag)
             {
                 document.insertNodeAtEnd(node);
                 continue;
             }
 
             // if start tag then parse element's content
             if (node.type == NodeType.StartTag)
             {
                 document.insertNodeAtEnd(node);
                 parseXMLElement(lexer, node, Lexer.IGNORE_WHITESPACE);
             }
 
         }
 
         if (doctype != null && !lexer.checkDocTypeKeyWords(doctype))
         {
             lexer.report.warning(lexer, doctype, null, ErrorCode.DTYPE_NOT_UPPER_CASE);
         }
 
         // ensure presence of initial <?XML version="1.0"?>
         if (lexer.configuration.isXmlDecl())
         {
             lexer.fixXmlDecl(document);
         }
 
         return document;
     }
 
     /**
      * errors in positioning of form start or end tags generally require human intervention to fix.
      */
     private static void badForm(final Lexer lexer) {
         lexer.badForm = 1;
     }
 }
