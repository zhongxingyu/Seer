 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.fxd.composer.editor.format;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Position;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 import org.netbeans.modules.editor.indent.spi.Context;
 import org.netbeans.modules.editor.indent.spi.ExtraLock;
 import org.netbeans.modules.editor.indent.spi.ReformatTask;
 import org.netbeans.modules.javafx.fxd.composer.editor.BracketCompletion;
 import org.netbeans.modules.javafx.fxd.composer.lexer.FXDTokenId;
 import org.netbeans.modules.javafx.fxd.composer.lexer.TokenUtils;
 
 /**
  *
  * @author Andrey Korostelev
  */
 public class FXDReformatTask implements ReformatTask {
     private final Context context;
 
     private static Logger logger = Logger.getLogger( "org.netbeans.modules.javafx.fxd.composer.editor.format" ); // NOI18N
 
     public FXDReformatTask(Context context) {
         this.context = context;
     }
 
     public void reformat() throws BadLocationException {
         final Document document = context.document();
         final BaseDocument bd = (BaseDocument)document;
         bd.runAtomic( new Runnable() {
 
             public void run() {
                 try {
                     final Queue<Adjustment> adjustments = new LinkedList<Adjustment>();
                     List<Context.Region> regions = context.indentRegions();
                     TokenSequence<FXDTokenId> ts = BracketCompletion.getTokenSequence(bd, 0);
 
                     for (Context.Region region : regions) {
                         int startOffset = region.getStartOffset();
                         int endOffset = region.getEndOffset();
                         Formatter formatter = new Formatter(context);
                         formatter.scan(adjustments, startOffset, endOffset, ts);
                     }
                     applyAdjustments(adjustments);
                 } catch( BadLocationException e ) {
                     logger.log( Level.FINE, e.getLocalizedMessage());
                 }
             }
         });
     }
 
     private void applyAdjustments(Queue<Adjustment> adjustments) throws BadLocationException {
         if (adjustments == null || adjustments.isEmpty()) return;
         logger.info("Applying " + adjustments.size() + " adjustments into source code."); // NOI18N
         while (!adjustments.isEmpty()) {
             final Adjustment adjustment = adjustments.poll();
             adjustment.apply(context);
         }
     }
 
     private static class Formatter{
 
         private static final String NEW_LINE_STRING = "\n"; // NOI18N
 
         private final Context m_context;
         private final BaseDocument m_baseDoc;
         private final int m_docIndent;
         private TokenSequence<FXDTokenId> m_ts;
         Queue<Adjustment> m_adjustments;
         private int m_currIndent;
         private int m_lastRowStart;
         private int m_startOffset;
         private int m_endOffset;
         private int m_lastIndentedRow;
 
         public Formatter(Context context){
             m_context = context;
             m_baseDoc = (BaseDocument)context.document();
             m_docIndent = IndentUtils.indentLevelSize(m_baseDoc);
         }
 
         protected void scan(Queue<Adjustment> adjustments, int startOffset, 
                 int endOffset, TokenSequence<FXDTokenId> ts) throws BadLocationException {
             m_adjustments = adjustments;
             m_startOffset = startOffset;
             m_endOffset = endOffset;
             m_ts = ts;
             m_currIndent = initialIndent();
             process();
         }
 
         private void process() throws BadLocationException{
             m_ts.move(m_startOffset);
             Token<FXDTokenId> token = TokenUtils.getNextNonWhiteFwd(m_ts);
             while (token != null){
                 // current token row start
                 int currRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_ts.offset());
                 // indent all white rows before token, skipped by getNextNonWhiteFwd
                 if (m_lastRowStart < currRowStart ){
                     indentLines(m_lastRowStart, currRowStart);
                 }
 
                 // format before endOffset only
                 if (m_lastRowStart > m_endOffset){
                     return;
                 }
 
                 // process left brackets
                 if (TokenUtils.isLBracketToken(token)) {
                     indentLine(m_lastRowStart);
                     token = processLBracket(token);
                     continue;
                 }
                 
                 // process right brackets
                 if (TokenUtils.isRBracketToken(token)){
                     m_currIndent = decIndent(m_currIndent);
                     indentLine(m_lastRowStart);
                     token = processRBracket(token);
                     continue;
                 }
                 
                 // indent current line
                 indentLine(m_lastRowStart);
                 // :
                 if (token.id() == FXDTokenId.COLON){
                     processColon();
                 }
                 // process multiline string token
                 if (token.id() == FXDTokenId.STRING_LITERAL) {
                     formatMultilineString(token);
                 }
                 // get next token to process
                 token = TokenUtils.getNextNonWhiteFwd(m_ts);
             }
         }
 
         private Token<FXDTokenId> processLBracket(Token<FXDTokenId> token) throws BadLocationException {
             m_currIndent = incIndent(m_currIndent);
             //fix spaces before
             int prevTokenEnd = getPrevNotWhiteTokenEndOffset();
             int lBracketStart = m_ts.offset();
             fixSpaces(prevTokenEnd, lBracketStart, 1);
             // line break
             token = TokenUtils.getNextNonWhiteFwd(m_ts);
             if (token != null) {
                 int nextTokenRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_ts.offset());
                 if (nextTokenRowStart == m_lastRowStart && !TokenUtils.isRBracketToken(token)) {
                     m_adjustments.offer(Adjustment.add(position(m_ts.offset()), NEW_LINE_STRING));
                     m_adjustments.offer(Adjustment.indent(position(m_ts.offset() + 1), m_currIndent));
                 } else {
                     // fix spaces after
                     fixSpaces(lBracketStart + 1, m_ts.offset(), 1);
                 }
             }
             return token;
         }
 
         private Token<FXDTokenId> processRBracket(Token<FXDTokenId> token) throws BadLocationException {
             if (!addBreakBeforeTokenIfNeed()){
                 fixSpaces(getPrevNotWhiteTokenEndOffset(), m_ts.offset(), 1);
             }
             token = TokenUtils.getNextNonWhiteFwd(m_ts);
             if (token != null) {
                 if (!TokenUtils.isRBracketToken(token) && !TokenUtils.isAttribsSeparatorToken(token)) {
                     if (!addBreakBeforeTokenIfNeed()){
                         fixSpaces(getPrevNotWhiteTokenEndOffset(), m_ts.offset(), 1);
                     }
                 }
             }
             return token;
         }
 
         private void processColon() throws BadLocationException {
             // check ws before
             int prevTokenEnd = getPrevNotWhiteTokenEndOffset();
             int nextTokenStart = m_ts.offset();
             fixSpaces(prevTokenEnd, nextTokenStart, 0);
             // check ws after
             prevTokenEnd = m_ts.offset() + m_ts.token().length();
             nextTokenStart = getNextNotWhiteTokenOffset();
             fixSpaces(prevTokenEnd, nextTokenStart, 1);
         }
 
         /**
          *
          * @param start
          * @param end
          * @param allowed number of spaces to be between start and end positions
          * @throws BadLocationException
          */
         private void fixSpaces(int start, int end, int allowed) throws BadLocationException {
             if (start + allowed != end) {
                 int startRowOffset = IndentUtils.lineStartOffset(m_baseDoc, start);
                 int endRowOffset = IndentUtils.lineStartOffset(m_baseDoc, end);
                 if (startRowOffset == endRowOffset) {
                     if (start + allowed < end) {
                         m_adjustments.offer(
                                 Adjustment.delete(position(start + allowed), position(end)));
                     } else {
                         String ws = emptyString(allowed);
                         m_adjustments.offer(
                                 Adjustment.add(position(start), ws));
                     }
                 }
             }
         }
 
         private String emptyString(int length){
             char[] arr = new char[length];
             Arrays.fill(arr, ' ');
             return String.valueOf(arr);
         }
 
         private void formatMultilineString(Token<FXDTokenId> token) throws BadLocationException {
             assert token.id() == FXDTokenId.STRING_LITERAL;
             int tokenEndRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_ts.offset() + token.length() - 1);
             int indentSteps = FormatterUtilities.MULTILINE_STRING_INDENT_STEPS;
             // if string is multiline
             if (tokenEndRowStart != m_lastRowStart) {
                 m_currIndent = incIndent(m_currIndent, indentSteps);
                 indentLines(m_lastRowStart, tokenEndRowStart);
                 indentLine(tokenEndRowStart);
                 m_currIndent = decIndent(m_currIndent, indentSteps);
 
             }
         }
 
         /**
          * @return true if break was added. false otherwise.
          * @throws BadLocationException
          */
         private boolean addBreakBeforeTokenIfNeed() throws BadLocationException{
             int tokenRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_ts.offset());
             if (tokenRowStart == m_lastRowStart) {
                 int prevTokenRowStart = getPrevNotWhiteTokenRowStart();
                 if(prevTokenRowStart == tokenRowStart){
                     m_adjustments.offer(Adjustment.add(position(m_ts.offset()), NEW_LINE_STRING));
                     m_adjustments.offer(Adjustment.indent(position(m_ts.offset() + 1), m_currIndent));
                     return true;
                 }
             }
             return false;
         }
 
         private int getPrevNotWhiteTokenRowStart() throws BadLocationException{
             TokenUtils.getNextNonWhiteBwd(m_ts);
             int prevTokenRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_ts.offset());
             TokenUtils.getNextNonWhiteFwd(m_ts);
             return prevTokenRowStart;
         }
 
         private int getPrevNotWhiteTokenEndOffset() throws BadLocationException {
             Token<FXDTokenId> t = TokenUtils.getNextNonWhiteBwd(m_ts);
             int prevTokenEnd = m_ts.offset() + t.length();
             TokenUtils.getNextNonWhiteFwd(m_ts);
             return prevTokenEnd;
         }
 
         private int getNextNotWhiteTokenOffset() throws BadLocationException {
             Token<FXDTokenId> t = TokenUtils.getNextNonWhiteFwd(m_ts);
             int nextTokenstart = m_ts.offset();
             TokenUtils.getNextNonWhiteBwd(m_ts);
             return nextTokenstart;
         }
 
         private int initialIndent() throws BadLocationException{
             int rowFirstNonWhite = Utilities.getRowFirstNonWhite(m_baseDoc, m_startOffset);
             m_lastRowStart = IndentUtils.lineStartOffset(m_baseDoc, m_startOffset);
             if ( rowFirstNonWhite >= m_startOffset){
                 return FormatterUtilities.calculateLineIndent(m_baseDoc, m_startOffset);
             } else {
                 // update m_startOffset if prev char is lbracket
                 int rowStart = Utilities.getRowStart(m_baseDoc, m_startOffset);
                 int prevCharOffset = Utilities.getFirstNonWhiteBwd(m_baseDoc, m_startOffset);
                 if (rowStart < prevCharOffset){
                     char prevChar = m_baseDoc.getChars(prevCharOffset, 1)[0];
                     if (FormatterUtilities.isLBracket(prevChar)){
                         m_startOffset = prevCharOffset;
                     }
                 }
                 return FormatterUtilities.getCurrentLineIndent(m_baseDoc, m_startOffset);
             }
         }
 
         private int incIndent(int old){
             return incIndent(old, 1);
         }
 
         /**
          * @param old old indentation value
          * @param incStep how much dedfault indentation steps should be added
          * @return
          */
         private int incIndent(int old, int incSteps){
             return old + m_docIndent * incSteps;
         }
 
         private int decIndent(int old){
             return decIndent(old, 1);
         }
 
         /**
          * @param old old indentation value
          * @param incStep how much default indentation steps should be removed
          * @return
          */
         private int decIndent(int old, int decSteps){
             int newIndent = old - m_docIndent * decSteps;
             return newIndent >= 0 ? newIndent : 0;
         }
 
         private void indentLines(int firstRowStart, int lastRowStart) throws BadLocationException {
             while (firstRowStart < lastRowStart && firstRowStart <= m_endOffset) {
                 indentLine(firstRowStart);
                 firstRowStart = Utilities.getRowStart(m_baseDoc, firstRowStart, +1);
             }
             m_lastRowStart = lastRowStart;
         }
 
         private void indentLine(int lineStart) throws BadLocationException {
             indentLine(lineStart, m_currIndent);
         }
 
         private void indentLine(int lineStart, int indent) throws BadLocationException {
             if (lineStart >= m_startOffset && lineStart != m_lastIndentedRow) {
 
                 if (isInsideMlComment(lineStart)){
                     indent += FormatterUtilities.MULTILINE_COMMENT_INDENT_CHARS;
                 }
                 if (m_context.lineIndent(m_context.lineStartOffset(lineStart)) != indent) {
                     m_adjustments.offer(Adjustment.indent(position(lineStart), indent));
                 }
                 m_lastIndentedRow = lineStart;
             }
         }
 
         private boolean isInsideMlComment(int offset) {
             boolean result = false;
             int tsOffset = m_ts.offset();
             if (TokenUtils.isInsideMlComment(m_ts, offset)) {
                 result = true;
             }
             m_ts.move(tsOffset);
             m_ts.moveNext();
             return result;
         }
 
         private Position position(int offset) throws BadLocationException {
             return m_context.document().createPosition(offset);
         }
     }
 
 
     public ExtraLock reformatLock() {
         return null;
     }
 
 }
