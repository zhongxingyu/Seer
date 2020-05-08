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
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 package org.netbeans.modules.javafx.editor.semantic;
 
 import com.sun.javafx.api.tree.*;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.support.CancellableTreePathScanner;
 
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.Types;
 import javax.swing.text.Document;
 import java.util.*;
 
 /**
  *
  * @author Jan Lahoda
  */
 public class MethodExitDetector extends CancellableTreePathScanner<Boolean, Stack<Tree>> {
     
     public MethodExitDetector() {}
     
     private CompilationInfo info;
     private Document doc;
     private List<int[]> highlights;
     private boolean doExitPoints;
     private Collection<TypeMirror> exceptions;
     private Stack<Map<TypeMirror, List<Tree>>> exceptions2HighlightsStack;
     
     public List<int[]> process(CompilationInfo info, Document document, FunctionDefinitionTree methoddecl, Collection<Tree> excs) {
         this.info = info;
         this.doc  = document;
         this.highlights = new ArrayList<int[]>();
         this.exceptions2HighlightsStack = new Stack<Map<TypeMirror, List<Tree>>>();
         this.exceptions2HighlightsStack.push(null);
         
         try {
             UnitTree cu = info.getCompilationUnit();
             
             //"return" exit point only if not searching for exceptions:
             doExitPoints = excs == null;
             
             Boolean wasReturn = scan(JavaFXTreePath.getPath(cu, methoddecl), null);
             
             if (isCanceled())
                 return null;
             
             if (doExitPoints && wasReturn != Boolean.TRUE) {
                 int lastBracket = Utilities.findLastBracket(methoddecl, cu, info.getTrees().getSourcePositions(), document);
                 
                 if (lastBracket != (-1)) {
                     //highlight the "fall over" exitpoint:
                     highlights.add(new int[] {lastBracket, lastBracket + 1});
                 }
             }
             
             List<TypeMirror> exceptions = null;
             
             if (excs != null) {
                 exceptions = new ArrayList<TypeMirror>();
                 
                 for (Tree t : excs) {
                     if (isCanceled())
                         return null;
                     
                     TypeMirror m = info.getTrees().getTypeMirror(JavaFXTreePath.getPath(cu, t));
                     
                     if (m != null) {
                         exceptions.add(m);
                     }
                 }
             }
             
             Types t = info.getTypes();
             
             assert exceptions2HighlightsStack.size() == 1 : exceptions2HighlightsStack.size();
             
             Map<TypeMirror, List<Tree>> exceptions2Highlights = exceptions2HighlightsStack.peek();
             
             //exceptions2Highlights may be null if the method is empty (or not finished, like "public void")
             //see ExitPointsEmptyMethod and ExitPointsStartedMethod tests:
             if (exceptions2Highlights != null) {
                 for (TypeMirror type1 : exceptions2Highlights.keySet()) {
                     if (isCanceled())
                         return null;
                     
                     boolean add = true;
                     
                     if (exceptions != null) {
                         add = false;
                         
                         for (TypeMirror type2 : exceptions) {
                             add |= t.isAssignable(type1, type2);
                         }
                     }
                     
                     if (add) {
                         for (Tree tree : exceptions2Highlights.get(type1)) {
                             addHighlightFor(tree);
                         }
                     }
                 }
             }
             
             return highlights;
         } finally {
             //clean-up:
             this.info = null;
             this.doc  = null;
             this.highlights = null;
             this.exceptions2HighlightsStack = null;
         }
     }
     
     private void addHighlightFor(Tree t) {
         int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
         int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
         
         highlights.add(new int[] {start, end});
     }
     
     private void addToExceptionsMap(TypeMirror key, Tree value) {
         if (value == null)
             return ;
         
         Map<TypeMirror, List<Tree>> map = exceptions2HighlightsStack.peek();
         
         if (map == null) {
             map = new HashMap<TypeMirror, List<Tree>>();
             exceptions2HighlightsStack.pop();
             exceptions2HighlightsStack.push(map);
         }
         
         List<Tree> l = map.get(key);
         
         if (l == null) {
             map.put(key, l = new ArrayList<Tree>());
         }
         
         l.add(value);
     }
     
     private void doPopup() {
         Map<TypeMirror, List<Tree>> top = exceptions2HighlightsStack.pop();
         
         if (top == null)
             return ;
         
         Map<TypeMirror, List<Tree>> result = exceptions2HighlightsStack.pop();
         
         if (result == null) {
             exceptions2HighlightsStack.push(top);
             return ;
         }
         
         for (TypeMirror key : top.keySet()) {
             List<Tree> topKey    = top.get(key);
             List<Tree> resultKey = result.get(key);
             
             if (topKey == null)
                 continue;
             
             if (resultKey == null) {
                 result.put(key, topKey);
                 continue;
             }
             
             resultKey.addAll(topKey);
         }
         
         exceptions2HighlightsStack.push(result);
     }
     
     @Override
     public Boolean visitTry(TryTree tree, Stack<Tree> d) {
         exceptions2HighlightsStack.push(null);
         
         Boolean returnInTryBlock = scan(tree.getBlock(), d);
         
         boolean returnInCatchBlock = true;
         
         for (Tree t : tree.getCatches()) {
             Boolean b = scan(t, d);
             
             returnInCatchBlock &= b == Boolean.TRUE;
         }
         
         Boolean returnInFinallyBlock = scan(tree.getFinallyBlock(), d);
         
         doPopup();
         
         if (returnInTryBlock == Boolean.TRUE && returnInCatchBlock)
             return Boolean.TRUE;
         
         return returnInFinallyBlock;
     }
     
     @Override
     public Boolean visitReturn(ReturnTree tree, Stack<Tree> d) {
         if (exceptions == null && doExitPoints) {
             addHighlightFor(tree);
         }
         
         super.visitReturn(tree, d);
         return Boolean.TRUE;
     }
 
     @Override
     public Boolean visitBlockExpression(BlockExpressionTree tree, Stack<Tree> p) {
         if (exceptions == null && doExitPoints) {
             ExpressionTree value = tree.getValue();
             if (value == null) {
                 List<? extends ExpressionTree> statements = tree.getStatements();
                if (statements.size() > 0) {
                    value = statements.get(statements.size() - 1);
                }
             }
             if (value != null) {
                 addHighlightFor(value);
             }
         }
         return Boolean.TRUE;
     }
     
     @Override
     public Boolean visitCatch(CatchTree tree, Stack<Tree> d) {
         TypeMirror type1 = info.getTrees().getTypeMirror(new JavaFXTreePath(new JavaFXTreePath(getCurrentPath(), tree.getParameter()), tree.getParameter().getType()));
         Types t = info.getTypes();
         
         if (type1 != null) {
             Set<TypeMirror> toRemove = new HashSet<TypeMirror>();
             Map<TypeMirror, List<Tree>> exceptions2Highlights = exceptions2HighlightsStack.peek();
             
             if (exceptions2Highlights != null) {
                 for (TypeMirror type2 : exceptions2Highlights.keySet()) {
                     if (t.isAssignable(type2, type1)) {
                         toRemove.add(type2);
                     }
                 }
                 
                 for (TypeMirror type : toRemove) {
                     exceptions2Highlights.remove(type);
                 }
             }
             
         }
         
         scan(tree.getParameter(), d);
         return scan(tree.getBlock(), d);
     }
     
     @Override
     public Boolean visitMethodInvocation(FunctionInvocationTree tree, Stack<Tree> d) {
         Element el = info.getTrees().getElement(new JavaFXTreePath(getCurrentPath(), tree.getMethodSelect()));
         
         if (el == null) {
             System.err.println("Warning: decl == null"); // NOI18N
             System.err.println("tree=" + tree); // NOI18N
         }
         
         if (el != null && el.getKind() == ElementKind.METHOD) {
             for (TypeMirror m : ((ExecutableElement) el).getThrownTypes()) {
                 addToExceptionsMap(m, tree);
             }
         }
         
         super.visitMethodInvocation(tree, d);
         return null;
     }
     
     @Override
     public Boolean visitThrow(ThrowTree tree, Stack<Tree> d) {
         addToExceptionsMap(info.getTrees().getTypeMirror(new JavaFXTreePath(getCurrentPath(), tree.getExpression())), tree);
         
         super.visitThrow(tree, d);
         
         return Boolean.TRUE;
     }
             
     @Override
     public Boolean visitConditionalExpression(ConditionalExpressionTree node, Stack<Tree> p) {
         scan(node.getCondition(), p);
         Boolean thenResult = scan(node.getTrueExpression(), p);
         Boolean elseResult = scan(node.getFalseExpression(), p);
         
         if (thenResult == Boolean.TRUE && elseResult == Boolean.TRUE)
             return Boolean.TRUE;
         
         return null;
     }
 
     @Override
     public Boolean visitFunctionDefinition(FunctionDefinitionTree node, Stack<Tree> p) {
         scan(node.getModifiers(), p);
         scan(node.getFunctionValue(), p);
         return super.visitFunctionDefinition(node, p);
     }
     
 }
