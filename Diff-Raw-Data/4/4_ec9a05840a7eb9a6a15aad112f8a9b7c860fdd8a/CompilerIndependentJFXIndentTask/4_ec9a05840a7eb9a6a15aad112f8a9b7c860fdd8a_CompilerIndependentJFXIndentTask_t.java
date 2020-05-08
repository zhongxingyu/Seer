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
 import org.netbeans.modules.editor.indent.spi.IndentTask;
 
 /**
  *
  * @author Anton Chechel
  */
 public class CompilerIndependentJFXIndentTask implements IndentTask {
 
     private final Context context;
 
     public CompilerIndependentJFXIndentTask(Context context) {
         this.context = context;
     }
 
     public void reindent() throws BadLocationException {
         Document document = context.document();
         Element rootElement = document.getDefaultRootElement();
         List<Context.Region> regions = context.indentRegions();
         if (regions.size() == 0) {
             indentLine(document, rootElement, context.startOffset());
         } else {
             for (Context.Region region : regions) {
                 indentLine(document, rootElement, region.getStartOffset());
             }
         }
     }
 
     private void indentLine(Document document, Element rootElement, int startOffset) throws BadLocationException {
         int elementIndex = rootElement.getElementIndex(startOffset);
         Element prevElement = rootElement.getElement(elementIndex > 0 ? elementIndex - 1 : elementIndex);
         int indent = IndentUtils.lineIndent(document, prevElement.getStartOffset());
         if (startOffset > 1) {
             String prevChar = document.getText(startOffset - 2, 1);
             int indentLevelSize = IndentUtils.indentLevelSize(document);
 //            int tabSize = IndentUtils.tabSize(document);
             if (prevChar.equals("{") || prevChar.equals("[")) { // NOI18N
 //                indent += tabSize;
                 indent += indentLevelSize;
 //            } else if (prevChar.equals("}") && indent >= tabSize) { // NOI18N
 //                indent -= tabSize;
             }
         }
         context.modifyIndent(context.lineStartOffset(startOffset), indent);
     }
 
     public ExtraLock indentLock() {
        // #183791
//        return JavaFXReformatExtraLock.getInstance();
        return null;
     }
 
 }
