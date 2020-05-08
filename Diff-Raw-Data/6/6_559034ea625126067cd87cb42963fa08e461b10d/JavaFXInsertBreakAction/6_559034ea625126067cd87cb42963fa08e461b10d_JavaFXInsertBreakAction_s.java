 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * Portions Copyrighted 1997-2008 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.editor;
 
 import org.netbeans.api.editor.mimelookup.MimeLookup;
 import org.netbeans.api.editor.mimelookup.MimePath;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.BaseKit;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Caret;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.TextAction;
 import java.awt.event.ActionEvent;
 import java.util.logging.Logger;
 
 /**
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
  * @todo documentation
  */
 public class JavaFXInsertBreakAction extends BaseKit.InsertBreakAction {
 
     static final long serialVersionUID = -1506173310438326380L;
 
     private boolean isJavadocTouched = false;
     private static Logger log = Logger.getLogger(JavaFXInsertBreakAction.class.getName());
 
     @Override
     public void actionPerformed(ActionEvent evt, JTextComponent target) {
         try {
             super.actionPerformed(evt, target);
 
             // XXX temporary solution until the editor will provide a SPI to plug. See issue #115739
             // This must run outside the document lock
             if (isJavadocTouched) {
                 Lookup.Result<TextAction> res = MimeLookup.getLookup(MimePath.parse("text/x-javadoc")).lookupResult(TextAction.class); // NOI18N
                 ActionEvent newevt = new ActionEvent(target, ActionEvent.ACTION_PERFORMED, "fix-javadoc"); // NOI18N
                 for (TextAction action : res.allInstances()) {
                     action.actionPerformed(newevt);
                 }
             }
         } finally {
             isJavadocTouched = false;
         }
     }
 
     protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
         int dotPos = caret.getDot();
         try {
             if (posWithinString(doc, dotPos)) {
                 try {
                     doc.insertString(dotPos, "\"  \"", null);                       // NOI18N
                     dotPos += 3;
                     caret.setDot(dotPos);
                     return dotPos;
                 } catch (BadLocationException ex) {
                     log.severe("Exception thrown during InsertBreakAction. " + ex);  // NOI18N
                 }
             } else {
                 int start = IndentUtils.lineStartOffset(doc, dotPos);
                 int lineIndent = IndentUtils.lineIndent(doc, start);
                 if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
                     int innerLineIndent = lineIndent + IndentUtils.indentLevelSize(doc);
                     StringBuilder sb = createAdditiveString(doc, lineIndent, "}"); // NOI18N
                     doc.insertString(dotPos, sb.toString(), null);
                     return dotPos + 1 + innerLineIndent;
                 } 
             }
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         return this.javadocBlockCompletion(target, doc, dotPos);
     }
 
     private static boolean posWithinString(BaseDocument doc, int dotOffset) {
         TokenSequence<JFXTokenId> ts = BracketCompletion.getTokenSequence(doc, dotOffset);
         // Check that the ENTER right at begining of string-literal is not treated like "within" the string
         return (ts.moveNext() && ts.token().id() == JFXTokenId.STRING_LITERAL && ts.offset() != dotOffset);
     }
 
     private StringBuilder createAdditiveString(BaseDocument doc, int baseIndent, String closingString) {
         StringBuilder sb = new StringBuilder("\n"); // NOI18N
         sb.append(IndentUtils.createIndentString(doc, baseIndent + IndentUtils.indentLevelSize(doc)));
         sb.append("\n"); // NOI18N
         sb.append(IndentUtils.createIndentString(doc, baseIndent)).append(closingString);
         return sb;
     }
 
     protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret, Object cookie) {
         if (cookie != null) {
             if (cookie instanceof Integer) {
                 // integer
                 int nowDotPos = (Integer) cookie;
                 caret.setDot(nowDotPos);
             }
         }
     }
 
     private Object javadocBlockCompletion(JTextComponent target, BaseDocument doc, final int dotPosition) {
         try {
             TokenHierarchy<BaseDocument> tokens = TokenHierarchy.get(doc);
             TokenSequence<?> ts = tokens.tokenSequence();
             ts.move(dotPosition);
             if (!((ts.moveNext() || ts.movePrevious()) && ts.token().id() == JFXTokenId.DOC_COMMENT)) {
                 return null;
             }
 
             int jdoffset = dotPosition - 3;
             if (jdoffset >= 0) {
                 CharSequence content = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(doc);
                 if (isOpenJavadoc(content, dotPosition - 1) && !isClosedJavadoc(content, dotPosition)) {
                     // complete open javadoc
                     // note that the formater will add one line of javadoc
                     doc.insertString(dotPosition, "*/", null); // NOI18N
                    doc.getFormatter().indentNewLine(doc, dotPosition);
                     target.setCaretPosition(dotPosition);
 
                     isJavadocTouched = true;
                     return Boolean.TRUE;
                 }
             }
         } catch (BadLocationException ex) {
             // ignore
             Exceptions.printStackTrace(ex);
         }
         return null;
     }
 
     private static boolean isOpenJavadoc(CharSequence content, int pos) {
         for (int i = pos; i >= 0; i--) {
             char c = content.charAt(i);
             if (c == '*' && i - 2 >= 0 && content.charAt(i - 1) == '*' && content.charAt(i - 2) == '/') { // NOI18N
                 // matched /**
                 return true;
             } else if (c == '\n') { // NOI18N
                 // no javadoc, matched start of line
                 return false;
             } else if (c == '/' && i - 1 >= 0 && content.charAt(i - 1) == '*') { // NOI18N
                 // matched javadoc enclosing tag
                 return false;
             }
         }
 
         return false;
     }
 
     private static boolean isClosedJavadoc(CharSequence txt, int pos) {
         int length = txt.length();
         int quotation = 0;
         for (int i = pos; i < length; i++) {
             char c = txt.charAt(i);
             if (c == '*' && i < length - 1 && txt.charAt(i + 1) == '/') { // NOI18N
                 if (quotation == 0 || i < length - 2) {
                     return true;
                 }
                 // guess it is not just part of some text constant
                 boolean isClosed = true;
                 for (int j = i + 2; j < length; j++) {
                     char cc = txt.charAt(j);
                     if (cc == '\n') { // NOI18N
                         break;
                     } else if (cc == '"' && j < length - 1 && txt.charAt(j + 1) != '\'') { // NOI18N
                         isClosed = false;
                         break;
                     }
                 }
 
                 if (isClosed) {
                     return true;
                 }
             } else if (c == '/' && i < length - 1 && txt.charAt(i + 1) == '*') { // NOI18N
                 // start of another comment block
                 return false;
             } else if (c == '\n') { // NOI18N
                 quotation = 0;
             } else if (c == '"' && i < length - 1 && txt.charAt(i + 1) != '\'') { // NOI18N
                 quotation = ++quotation % 2;
             }
         }
 
         return false;
     }
 
 }
