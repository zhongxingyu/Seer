 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.editor.completion.environment;
 
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TryTree;
 import com.sun.tools.javafx.tree.JFXBlock;
 import com.sun.tools.javafx.tree.JFXFunctionValue;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.tools.Diagnostic;
 import org.netbeans.modules.javafx.editor.completion.JavaFXCompletionEnvironment;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.*;
 
 /**
  *
  * @author David Strupl
  */
 public class FunctionValueEnvironment extends JavaFXCompletionEnvironment<JFXFunctionValue> {
 
     private static final Logger logger = Logger.getLogger(FunctionValueEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     @Override
     protected void inside(JFXFunctionValue val) throws IOException {
         if (LOGGABLE) log("inside JFXFunctionValue " + val);
         JFXBlock bl = val.getBodyExpression();
         if (bl != null) {
             ExpressionTree last = null;
             for (ExpressionTree stat : bl.getStatements()) {
                 int pos = (int) sourcePositions.getStartPosition(root, stat);
                 if (pos == Diagnostic.NOPOS || offset <= pos) {
                     break;
                 }
                 last = stat;
             }
             if (last != null && last.getJavaFXKind() == Tree.JavaFXKind.TRY) {
                 if (((TryTree) last).getFinallyBlock() == null) {
                     addKeyword(CATCH_KEYWORD, null, false);
                     addKeyword(FINALLY_KEYWORD, null, false);
                     if (((TryTree) last).getCatches().size() == 0) {
                         return;
                     }
                 }
             }
            path = JavaFXTreePath.getPath(root, bl);
            localResult(null);
            addKeywordsForStatement();
         }
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
