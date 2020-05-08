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
 package org.netbeans.modules.javafx.navigation;
 
 import com.sun.javafx.api.tree.JavaFXTree;
 import com.sun.javafx.api.tree.JavaFXTree.JavaFXKind;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.StatementTree;
 import com.sun.source.tree.Tree;
 import com.sun.source.util.TreePath;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.lang.model.element.Element;
 import org.netbeans.api.javafx.source.CompilationInfo;
 
 /**
  * Only quintessentially pure space magic is here.
  * 
  * @author Anton Chechel The Wizard
  */
public class SpaceMagicUtils {
 
     public static final String MAGIC_FUNCTION = "javafx$run$";
 
     /**
      * Determines whether a given element has spiritual invocation of magic function.
      * In other words whether element belongs to javafx$run$
      * 
      * @param element to check
      * @return whether a given element belongs to javafx$run$
      */
     public static boolean hasSpiritualInvocation(Element element) {
         if (element instanceof Symbol.VarSymbol) {
             Symbol.VarSymbol var = (Symbol.VarSymbol) element;
             String ownerName = var.owner.name.toString();
             return MAGIC_FUNCTION.equals(ownerName);
         }
         return false;
     }
 
     /**
      * Determines whether a given element is a magic function.
      * In other words whether element belongs to javafx$run$
      * 
      * @param element to check
      * @return whether a given element is a magic function javafx$run$
      */
     public static boolean isSpiritualMethod(Element element) {
         if (element instanceof Symbol.MethodSymbol) {
             Symbol.MethodSymbol method = (Symbol.MethodSymbol) element;
             String ownerName = method.name.toString();
             return MAGIC_FUNCTION.equals(ownerName);
         }
         return false;
     }
 
     /**
      * Gets all members from magic function
      * In other words whether element belongs to javafx$run$
      * 
      * @param info CompilationInfo
      * @return whether a given element is a magic function javafx$run$
      */
     public static List<Element> getSpiritualMembers(final CompilationInfo info) {
         final List<Element> elements = new ArrayList<Element>();
         final JavafxcTrees trees = info.getTrees();
         final CompilationUnitTree cut = info.getCompilationUnit();
         
         for (Tree tt : cut.getTypeDecls()) {
             if (tt.getKind() == Tree.Kind.OTHER && tt instanceof JavaFXTree) {
                 JavaFXTree jfxtt = (JavaFXTree) tt;
                 JavaFXKind kk = jfxtt.getJavaFXKind();
                 if (kk == JavaFXKind.CLASS_DECLARATION) {
                     JFXClassDeclaration cd = (JFXClassDeclaration) jfxtt;
                     
                     for (Tree jct : cd.getClassMembers()) {
                         if (jct.getKind() == Tree.Kind.OTHER && jct instanceof JavaFXTree) {
                             JavaFXTree jfxjct = (JavaFXTree) jct;
                             JavaFXKind k = jfxjct.getJavaFXKind();
                             if (k == JavaFXKind.FUNCTION_DEFINITION) {
                                 JFXFunctionDefinition fdt = (JFXFunctionDefinition) jfxjct;
                                 if (MAGIC_FUNCTION.equals(fdt.name.toString())) {
                                     
                                     for (StatementTree st : fdt.getBodyExpression().getStatements()) {
                                         TreePath path = trees.getPath(cut, fdt);
                                         TreePath expPath = new TreePath(path, st);
                                         Element element = trees.getElement(expPath);
                                         if (element != null) {
                                             elements.add(element);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         return Collections.unmodifiableList(elements);
         
         // This is too advanced magic at the moment, maybe will work later on
         
 //        JavafxcTrees trees = info.getTrees();
 //        TreePath path = trees.getPath(e);
 //        JFXFunctionDefinition tree = (JFXFunctionDefinition) trees.getTree(e);
 //        JFXBlockExpression bodyExpression = tree.getBodyExpression();
 //        for (StatementTree st : bodyExpression.getStatements()) {
 //            if (canceled.get()) {
 //                return;
 //            }
 //            TreePath expPath = new TreePath(path, st);
 //            Element el = trees.getElement(expPath);
 //            if (el == null) {
 //                continue;
 //            }
 //            Description d = element2description(el, e, parentDescription.isInherited, info, pos);
 //            if (null != d) {
 //                parentDescription.subs.add(d);
 //                if (el instanceof TypeElement && !d.isInherited) {
 //                    addMembers((TypeElement) el, d, info, pos);
 //                }
 //            }
 //        }
 
 //        Symbol.MethodSymbol sym = (Symbol.MethodSymbol) e;
 //        Iterable<Symbol> elements = sym.getEnclosedElements();
 //        for (Symbol m : elements) {
 //            if (canceled.get()) {
 //                return;
 //            }
 //            Description d = element2description(m, e, parentDescription.isInherited, info, pos);
 //            if (null != d) {
 //                parentDescription.subs.add(d);
 //                if (m instanceof TypeElement && !d.isInherited) {
 //                    addMembers((TypeElement) m, d, info, pos);
 //                }
 //            }
 //        }
     }
     
 }
