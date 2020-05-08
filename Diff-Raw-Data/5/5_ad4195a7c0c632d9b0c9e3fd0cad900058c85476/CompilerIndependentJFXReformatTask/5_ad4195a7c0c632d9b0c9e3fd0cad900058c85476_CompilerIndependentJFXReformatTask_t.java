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
 package org.netbeans.modules.javafx.editor.format;
 
 import java.util.List;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 import org.netbeans.modules.editor.indent.spi.Context;
 import org.netbeans.modules.editor.indent.spi.ExtraLock;
 import org.netbeans.modules.editor.indent.spi.ReformatTask;
 
 /**
  *
  * @author Anton Chechel
  */
 public class CompilerIndependentJFXReformatTask implements ReformatTask {
     private static final String TEMPLATE_START = "&template_start&"; // NOI18N
    private static final String TEMPLATE_END = "&template_end&"; // NOI18N
 
     private final Context context;
 
     public CompilerIndependentJFXReformatTask(Context context) {
         this.context = context;
     }
 
     public void reformat() throws BadLocationException {
         Document document = context.document();
         Element rootElement = document.getDefaultRootElement();
         List<Context.Region> regions = context.indentRegions();
 
         for (Context.Region region : regions) {
             int startOffset = region.getStartOffset();
             int endOffset = region.getEndOffset();
             int length = region.getEndOffset() - region.getStartOffset();
             int elementIndex = rootElement.getElementIndex(startOffset);
             final int prevIndex = elementIndex > 0 ? elementIndex - 1 : elementIndex;
             int indent = getIndent(document, prevIndex);
 
             String text = document.getText(startOffset, length);
             if (text.startsWith(TEMPLATE_START)) {
                 // remove template tags
                 int st = startOffset + text.indexOf(TEMPLATE_START);
                 document.remove(st, TEMPLATE_START.length());
                int en = endOffset - TEMPLATE_END.length() - TEMPLATE_START.length() - 1;
                 document.remove(en, TEMPLATE_END.length());
 
                 // modify indent
                 int lastElement = rootElement.getElementIndex(en);
                 for (int i = elementIndex; i <= lastElement; i++) {
                     Element element = rootElement.getElement(i);
                     int so = element.getStartOffset();
                     int currentIndent = context.lineIndent(so);
 
                     // first line should not be indented if there is something already except whitespaces
                     boolean doIndent = true;
                     if (i == elementIndex) {
                         String textBefore = document.getText(so, startOffset - so);
                         boolean isTextWSOnly = textBefore.matches("\\s+"); // NOI18N
                         doIndent = textBefore.length() == 0 || isTextWSOnly;
                         if (!doIndent) {
                             indent = getIndent(document, i);
                         }
                     } else {
                         doIndent = true;
                     }
 
                     if (doIndent) {
                         context.modifyIndent(so, currentIndent + indent);
                     }
                 }
 
             }
         }
     }
 
     public ExtraLock reformatLock() {
         return null;
     }
 
     /**
      * return indent for nearest non-ws line
      */
     private static int getIndent(final Document document, int elementIndex) throws BadLocationException {
         Element rootElement = document.getDefaultRootElement();
         boolean isTextWSOnly;
         int eso = rootElement.getStartOffset();
         boolean extendIndent = false;
         do {
             if (elementIndex < 1) {
                 break;
             }
             Element element = rootElement.getElement(elementIndex--);
             eso = element.getStartOffset();
             String elementText = document.getText(eso, element.getEndOffset() - eso);
             isTextWSOnly = elementText.matches("\\s+"); // NOI18N
             if (!isTextWSOnly) {
                 final String ett = elementText.trim();
                 extendIndent = ett.endsWith("{") || ett.endsWith("["); // NOI18N
             }
         } while (isTextWSOnly);
 
         int indent = IndentUtils.lineIndent(document, eso);
         if (extendIndent) {
             indent += IndentUtils.tabSize(document);
         }
         return indent;
     }
 
 //    private static int[] getLinesLength(String text) {
 //        final String[] lines = text.split("\n", -1); // NOI18N
 //        int[] length = new int[lines.length];
 //        for (int i = 0; i < lines.length; i++) {
 //            length[i] = lines[i].length() + 1;
 //        }
 //        return length;
 //    }
 }
