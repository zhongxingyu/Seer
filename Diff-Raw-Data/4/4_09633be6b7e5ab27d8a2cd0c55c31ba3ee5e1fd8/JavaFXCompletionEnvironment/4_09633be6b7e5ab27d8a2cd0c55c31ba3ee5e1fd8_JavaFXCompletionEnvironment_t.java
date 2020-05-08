 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 package org.netbeans.modules.javafx.editor.completion;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javafx.api.JavafxcScope;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.ClassIndex.NameKind;
 import org.netbeans.api.javafx.source.ClassIndex.SearchScope;
 import org.netbeans.api.javafx.source.*;
 import org.netbeans.api.javafx.source.ClasspathInfo.PathKind;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXParserResult;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.*;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileSystem;
 import org.openide.filesystems.FileUtil;
 
 import javax.lang.model.element.*;
 import static javax.lang.model.element.Modifier.*;
 import javax.lang.model.type.*;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.Diagnostic;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.netbeans.api.javafx.editor.Cancellable;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 import org.netbeans.modules.parsing.api.Source;
 import org.netbeans.modules.parsing.spi.ParseException;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author David Strupl, Anton Chechel
  */
 public class JavaFXCompletionEnvironment<T extends Tree> {
 
     private static final Logger logger = Logger.getLogger(JavaFXCompletionEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
     private static int usingFakeSource = 0;
     protected int offset;
     protected String prefix;
     protected boolean isCamelCasePrefix;
     protected CompilationController controller;
     protected JavaFXTreePath path;
     protected SourcePositions sourcePositions;
     protected boolean insideForEachExpressiion = false;
     protected UnitTree root;
     protected JavaFXCompletionQuery query;
     protected Cancellable cancellable;
 
     protected JavaFXCompletionEnvironment() {
     }
 
     /*
      * Thies method must be called after constructor before a call to resolveCompletion
      */
     void init(int offset, String prefix, CompilationController controller, JavaFXTreePath path, SourcePositions sourcePositions, final JavaFXCompletionQuery query) {
         this.offset = offset;
         this.prefix = prefix;
         this.isCamelCasePrefix = prefix != null && prefix.length() > 1 && JavaFXCompletionQuery.camelCasePattern.matcher(prefix).matches();
         this.controller = controller;
         this.path = path;
         this.sourcePositions = sourcePositions;
         this.query = query;
         this.root = path.getCompilationUnit();
 
         this.cancellable = new Cancellable() {
             public boolean isCancelled() {
                 return query.isTaskCancelled0();
             }
 
             public void cancell() {
                 // do nothing for now
             }
         };
     }
 
     /**
      * This method should be overriden in subclasses
      */
     protected void inside(T t) throws IOException {
         if (LOGGABLE) log(NbBundle.getBundle("org/netbeans/modules/javafx/editor/completion/Bundle").getString("NOT_IMPLEMENTED_") + t.getJavaFXKind() + " inside " + t); // NOI18N
     }
 
     protected void insideFunctionBlock(List<ExpressionTree> statements) throws IOException {
         ExpressionTree last = null;
         for (ExpressionTree stat : statements) {
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
         localResult(null);
         addKeywordsForStatement();
     }
 
     public int getOffset() {
         return offset;
     }
 
     public String getPrefix() {
         return prefix;
     }
 
     public boolean isCamelCasePrefix() {
         return isCamelCasePrefix;
     }
 
     public CompilationController getController() {
         return controller;
     }
 
     public UnitTree getRoot() {
         return root;
     }
 
     public JavaFXTreePath getPath() {
         return path;
     }
 
     public SourcePositions getSourcePositions() {
         return sourcePositions;
     }
 
     public void insideForEachExpressiion() {
         this.insideForEachExpressiion = true;
     }
 
     public boolean isInsideForEachExpressiion() {
         return insideForEachExpressiion;
     }
 
     /**
      * If the tree is broken we are in fact not in the compilation unit.
      * @param env
      * @return
      */
     protected boolean isTreeBroken() {
         int start = (int) sourcePositions.getStartPosition(root, root);
         int end = (int) sourcePositions.getEndPosition(root, root);
         if (LOGGABLE) log("isTreeBroken start: " + start + " end: " + end); // NOI18N
         return start == -1 || end == -1;
     }
 
     protected String fullName(Tree tree) {
         switch (tree.getJavaFXKind()) {
             case IDENTIFIER:
                 return ((IdentifierTree) tree).getName().toString();
             case MEMBER_SELECT:
                 String sname = fullName(((MemberSelectTree) tree).getExpression());
                 return sname == null ? null : sname + '.' + ((MemberSelectTree) tree).getIdentifier(); // NOI18N
             default:
                 return null;
         }
     }
 
     void insideTypeCheck() throws IOException {
         InstanceOfTree iot = (InstanceOfTree) getPath().getLeaf();
         SafeTokenSequence<JFXTokenId> ts = findLastNonWhitespaceToken(iot, getOffset());
     }
 
     protected void insideExpression(JavaFXTreePath exPath) throws IOException {
         if (LOGGABLE) log("insideExpression " + exPath.getLeaf()); // NOI18N
         Tree et = exPath.getLeaf();
         Tree parent = exPath.getParentPath().getLeaf();
         int endPos = (int) getSourcePositions().getEndPosition(root, et);
         if (endPos != Diagnostic.NOPOS && endPos < offset) {
             SafeTokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(endPos, offset);
             if (LOGGABLE) log("  last: " + last); // NOI18N
             if (last != null) {
                 return;
             }
         }
         if (LOGGABLE) log(NbBundle.getBundle("org/netbeans/modules/javafx/editor/completion/Bundle").getString("NOT_IMPLEMENTED:_insideExpression_") + exPath.getLeaf()); // NOI18N
 
     }
 
     protected void addResult(JavaFXCompletionItem i) {
         query.results.add(i);
     }
 
     protected void addMembers(final TypeMirror type, final boolean methods, final boolean fields) {
         JavafxcScope sc = controller.getTreeUtilities().getScope(path);
         if (LOGGABLE) log("     addMembers scope was computed from path == " + path.getLeaf()); // NOI18N
         boolean isStatic = controller.getTreeUtilities().isStaticContext(sc);
         if (path.getLeaf() != null && path.getLeaf().toString().startsWith("variable initialization for static script only (default) var")) { // NOI18N
             isStatic = true;
         }
         if (LOGGABLE) log("         isStatic == " + isStatic); // NOI18N
         addMembers(type, methods, fields, null,sc, true, !isStatic);
     }
 
     protected void addMembers(final TypeMirror type,
             final boolean methods, final boolean fields,
             final String textToAdd, JavafxcScope scope,boolean statics, boolean instance) {
         if (LOGGABLE) log("addMembers: " + type); // NOI18N
         if (type == null || type.getKind() != TypeKind.DECLARED) {
             if (LOGGABLE) log("RETURNING: type.getKind() == " + (type != null ? type.getKind() : " type is null")); // NOI18N
             return;
         }
 
         DeclaredType dt = (DeclaredType) type;
         if (LOGGABLE) log("  elementKind == " + dt.asElement().getKind()); // NOI18N
         final ElementKind kind = dt.asElement().getKind();
         if (kind != ElementKind.CLASS && kind != ElementKind.ENUM) {
             return;
         }
 
         Elements elements = controller.getElements();
         final TypeElement te = (TypeElement) dt.asElement();
         for (Element member : te.getEnclosedElements()) {
             if (LOGGABLE) log("    member1 = " + member + " member1.getKind() " + member.getKind()); // NOI18N
             String s = member.getSimpleName().toString();
             if ("<error>".equals(s)) { // NOI18N
                 continue;
             }
             // #164909 - prevent internal SDK vars "VCNT$" and "VOFF$"
             if (s != null && s.indexOf('$') != -1) {
                 continue;
             }
             boolean isStatic = member.getModifiers().contains(STATIC);
               if (!controller.getTreeUtilities().isAccessible(scope, member, dt)) {
                 if (LOGGABLE) log("    not accessible " + s); // NOI18N
                 continue;
             }
             if (isStatic && !statics) {
                 if (LOGGABLE) log("    is static and we don't want them " + s); // NOI18N
                 continue;
             }
             if (!isStatic && !instance) {
                 if (LOGGABLE) log("     is instance and we don't want them " + s); // NOI18N
                 continue;
             }
             String tta = textToAdd;
             if (fields && (member.getKind() == ElementKind.FIELD || member.getKind() == ElementKind.ENUM_CONSTANT)) {
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     if (":".equals(textToAdd)) { // NOI18N
                         JavafxTypes types = controller.getJavafxTypes();
                         TypeMirror tm = member.asType();
                         if (types.isSequence((Type) tm)) {
                             tta += " []"; // NOI18N
                         }
                     }
                     ElementHandle eh = null;
                     try {
                         eh = ElementHandle.create(member);
                     } catch (Exception ex) {
                         // cannot convert --> ignore
                     }
                     if (eh != null) {
                         addResult(JavaFXCompletionItem.createVariableItem(eh, member.asType(), s, query.anchorOffset, tta, true));
                     }
                 }
             }
         }
 
         for (Element member : elements.getAllMembers(te)) {
             if (LOGGABLE) log("    member2 == " + member + " member2.getKind() " + member.getKind()); // NOI18N
             String s = member.getSimpleName().toString();
             if ("<error>".equals(s)) { // NOI18N
                 continue;
             }
             // #164909 - prevent internal SDK vars "VCNT$" and "VOFF$"
             if (s != null && s.indexOf('$') != -1) {
                 continue;
             }
             if (!controller.getTreeUtilities().isAccessible(scope, member, dt)) {
                 if (LOGGABLE) log("    not accessible " + s); // NOI18N
                 continue;
             }
             boolean isStatic = member.getModifiers().contains(STATIC);
             if (isStatic && !statics) {
                 if (LOGGABLE) log("    is static and we don't want them " + s); // NOI18N
                 continue;
             }
             if (!isStatic && !instance) {
                 if (LOGGABLE) log("     is instance and we don't want them " + s); // NOI18N
                 continue;
             }
             if (methods && member.getKind() == ElementKind.METHOD) {
                 if (s.contains("$")) { // NOI18N
                     continue;
                 }
 
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     boolean isInherited = !te.equals(((Symbol) member.getEnclosingElement()).enclClass());
                     boolean isDeprecated = elements.isDeprecated(member);
                     addResult(
                             JavaFXCompletionItem.createExecutableItem(
                             (ExecutableElement) member,
                             (ExecutableType) member.asType(),
                             query.anchorOffset, isInherited, isDeprecated, false, false));
                 }
             } else if (fields && member.getKind() == ElementKind.FIELD) {
                 String tta = textToAdd;
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     if (":".equals(textToAdd)) { // NOI18N
                         JavafxTypes types = controller.getJavafxTypes();
                         TypeMirror tm = member.asType();
                         if (types.isSequence((Type) tm)) {
                             tta += " []"; // NOI18N
                         }
                     }
                     ElementHandle eh = null;
                     try {
                         eh = ElementHandle.create(member);
                     } catch (Exception ex) {
                         // cannot convert --> ignore
                     }
                     if (eh != null) {
                         addResult(JavaFXCompletionItem.createVariableItem(eh, member.asType(), s, query.anchorOffset, tta, false));
                     }
                 }
             }
         }
 
         TypeMirror parent = te.getSuperclass();
         if (parent != null) {
             addMembers(parent, false, true, textToAdd, scope, statics, instance);
         }
         for (TypeMirror intf : te.getInterfaces()) {
             addMembers(intf, false, true, textToAdd, scope, statics, instance);
         }
     }
 
     protected void localResult(TypeMirror smart) {
         addLocalMembersAndVars(smart);
         addLocalAndImportedTypes(null, null, null, false, smart);
         addLocalAndImportedFunctions();
     }
 
     protected void addMemberConstantsAndTypes(final TypeMirror type, final Element elem) throws IOException {
         if (LOGGABLE) log("addMemberConstantsAndTypes: " + type + " elem: " + elem); // NOI18N
     }
 
     protected void addLocalMembersAndVars(TypeMirror smart) {
         if (LOGGABLE) log("addLocalMembersAndVars: " + prefix); // NOI18N
 
         final JavafxcTrees trees = controller.getTrees();
         if (smart != null && smart.getKind() == TypeKind.DECLARED) {
             if (LOGGABLE) log("adding declared type + subtypes: " + smart); // NOI18N
             DeclaredType dt = (DeclaredType) smart;
             TypeElement elem = (TypeElement) dt.asElement();
             addResult(JavaFXCompletionItem.createTypeItem(elem, dt, query.anchorOffset, false, false, true, false));
 
             for (DeclaredType subtype : getSubtypesOf((DeclaredType) smart)) {
                 TypeElement subElem = (TypeElement) subtype.asElement();
                 addResult(JavaFXCompletionItem.createTypeItem(subElem, subtype, query.anchorOffset, false, false, true, false));
             }
         }
 
         for (JavaFXTreePath tp = getPath(); tp != null; tp = tp.getParentPath()) {
             Tree t = tp.getLeaf();
             if (LOGGABLE) log("  tree kind: " + t.getJavaFXKind()); // NOI18N
             if (t instanceof UnitTree) {
                 UnitTree cut = (UnitTree) t;
                 for (Tree tt : cut.getTypeDecls()) {
                     if (LOGGABLE) log("      tt: " + tt); // NOI18N
                     JavaFXKind kk = tt.getJavaFXKind();
                     if (kk == JavaFXKind.CLASS_DECLARATION) {
                         JFXClassDeclaration cd = (JFXClassDeclaration) tt;
                         for (Tree jct : cd.getClassMembers()) {
                             if (LOGGABLE) log("            jct == " + jct); // NOI18N
                             JavaFXKind k = jct.getJavaFXKind();
                             if (LOGGABLE) log("       kind of jct = " + k); // NOI18N
                             if (k == JavaFXKind.FUNCTION_DEFINITION) {
                                 JFXFunctionDefinition fdt = (JFXFunctionDefinition) jct;
                                 if (LOGGABLE) log("      fdt == " + fdt.name.toString()); // NOI18N
                                 if ("javafx$run$".equals(fdt.name.toString())) { // NOI18N
                                     addBlockExpressionLocals(fdt.getBodyExpression(), tp, smart);
                                     JavaFXTreePath mp = JavaFXTreePath.getPath(cut, tt);
                                     TypeMirror tm = trees.getTypeMirror(mp);
                                     if (LOGGABLE) log("  javafx$run$ tm == " + tm + " ---- tm.getKind() == " + (tm == null ? "null" : tm.getKind())); // NOI18N
                                     JavaFXTreePath mp2 = JavaFXTreePath.getPath(cut, fdt);
                                     addMembers(tm, true, true,
                                             null, controller.getTreeUtilities().getScope(mp2),
                                             true, false);
                                 }
                             }
                         }
                     }
                 }
             }
             JavaFXKind k = t.getJavaFXKind();
             if (LOGGABLE) log("  fx kind: " + k); // NOI18N
             if (k == JavaFXKind.CLASS_DECLARATION) {
                 TypeMirror tm = trees.getTypeMirror(tp);
                 if (LOGGABLE) log("  tm == " + tm + " ---- tm.getKind() == " + (tm == null ? "null" : tm.getKind())); // NOI18N
                 addMembers(tm, true, true);
             }
             if (k == JavaFXKind.BLOCK_EXPRESSION) {
                 addBlockExpressionLocals((BlockExpressionTree) t, tp, smart);
             }
             if (k == JavaFXKind.FOR_EXPRESSION_FOR) {
                 ForExpressionTree fet = (ForExpressionTree) t;
                 if (LOGGABLE) log("  for expression: " + fet + "\n"); // NOI18N
                 for (ForExpressionInClauseTree fetic : fet.getInClauses()) {
                     if (LOGGABLE) log("  fetic: " + fetic + "\n"); // NOI18N
                     String s = fetic.getVariable().getName().toString();
                     if (LOGGABLE) log("    adding(2) " + s + " with prefix " + prefix); // NOI18N
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, fetic));
                     if (smart != null && tm != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, false));
                     }
                 }
             }
             if (k == JavaFXKind.FUNCTION_VALUE) {
                 FunctionValueTree fvt = (FunctionValueTree) t;
                 for (VariableTree var : fvt.getParameters()) {
                     if (LOGGABLE) log("  var: " + var + "\n"); // NOI18N
                     String s = var.getName().toString();
                     if (s.contains("$")) { // NOI18N
                         continue;
                     }
                     if (LOGGABLE) log("    adding(3) " + s + " with prefix " + prefix); // NOI18N
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, var));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, false));
                     }
                 }
             }
             if (k == JavaFXKind.ON_REPLACE) {
                 OnReplaceTree ort = (OnReplaceTree) t;
                 // commented out log because of JFXC-1205
                 // if (LOGGABLE) log("  OnReplaceTree: " + ort + "\n");
                 VariableTree varTree = ort.getNewElements();
                 if (varTree != null) {
                     String s1 = varTree.getName().toString();
                     if (LOGGABLE) log("    adding(4) " + s1 + " with prefix " + prefix); // NOI18N
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, varTree));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s1, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s1, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s1, query.anchorOffset, false));
                     }
                 }
                 VariableTree varTree2 = ort.getOldValue();
                 if (varTree2 != null) {
                     String s2 = varTree2.getName().toString();
                     if (LOGGABLE) log("    adding(5) " + s2 + " with prefix " + prefix); // NOI18N
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, varTree2));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s2, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s2, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s2, query.anchorOffset, false));
                     }
                 }
             }
         }
     }
 
     private void addBlockExpressionLocals(BlockExpressionTree bet, JavaFXTreePath tp, TypeMirror smart) {
         if (LOGGABLE) log("  block expression: " + bet + "\n"); // NOI18N
         for (ExpressionTree st : bet.getStatements()) {
             addLocal(st, tp, smart);
         }
         addLocal(bet.getValue(), tp, smart);
     }
 
     private void addLocal(ExpressionTree st, JavaFXTreePath tp, TypeMirror smart) {
         if (st == null) {
             return;
         }
         JavaFXTreePath expPath = new JavaFXTreePath(tp, st);
         if (LOGGABLE) log("    expPath == " + expPath.getLeaf()); // NOI18N
         JavafxcTrees trees = controller.getTrees();
         Element type = trees.getElement(expPath);
         if (type == null) {
             return;
         }
         if (LOGGABLE) log("    type.getKind() == " + type.getKind()); // NOI18N
         if (type.getKind() == ElementKind.LOCAL_VARIABLE || type.getKind() == ElementKind.FIELD) {
             final Name simpleName = type.getSimpleName();
             if (simpleName == null) {
                 return;
             }
             String s = simpleName.toString();
             if (LOGGABLE) log("    adding(1) " + s + " with prefix " + prefix); // NOI18N
             TypeMirror tm = trees.getTypeMirror(expPath);
             if (smart != null && tm != null && tm.getKind() == smart.getKind()) {
                 addResult(JavaFXCompletionItem.createVariableItem(tm,
                         s, query.anchorOffset, true));
             }
             if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                 addResult(JavaFXCompletionItem.createVariableItem(tm,
                         s, query.anchorOffset, false));
             }
         }
     }
 
     protected void addPackages(String fqnPrefix) {
         if (LOGGABLE) log("addPackages " + fqnPrefix); // NOI18N
         if (fqnPrefix == null) {
             fqnPrefix = ""; // NOI18N
         }
 
         ClasspathInfo info = controller.getClasspathInfo();
         ArrayList<FileObject> fos = new ArrayList<FileObject>();
         ClassPath cp = info.getClassPath(PathKind.SOURCE);
         fos.addAll(Arrays.asList(cp.getRoots()));
         cp = info.getClassPath(PathKind.COMPILE);
         fos.addAll(Arrays.asList(cp.getRoots()));
         cp = info.getClassPath(PathKind.BOOT);
         fos.addAll(Arrays.asList(cp.getRoots()));
         String pr = ""; // NOI18N
         if (fqnPrefix.lastIndexOf('.') >= 0) { // NOI18N
             pr = fqnPrefix.substring(0, fqnPrefix.lastIndexOf('.')); // NOI18N
         }
         if (LOGGABLE) log("  pr == " + pr); // NOI18N
         for (String name : pr.split("\\.")) { // NOI18N
             ArrayList<FileObject> newFos = new ArrayList<FileObject>();
             if (LOGGABLE) log("  traversing to " + name); // NOI18N
             for (FileObject f : fos) {
                 if (f.isFolder()) {
                     FileObject child = f.getFileObject(name);
                     if (child != null) {
                         newFos.add(child);
                     }
                 }
             }
             if (LOGGABLE) log("  replacing " + fos + "\n   with " + newFos); // NOI18N
             fos = newFos;
         }
         for (FileObject fo : fos) {
             if (fo.isFolder()) {
                 for (FileObject child : fo.getChildren()) {
                     if (child.isFolder()) {
                         if (LOGGABLE) log(" found : " + child); // NOI18N
                         if (("META-INF".equals(child.getName())) ||  // NOI18N
                             ("doc-files".equals(child.getName()))) { // NOI18N
                             continue;
                         }
                         String s = child.getPath().replace('/', '.'); // NOI18N
                         addResult(JavaFXCompletionItem.createPackageItem(s, query.anchorOffset, false));
                     }
                 }
             }
         }
     }
 
     protected List<DeclaredType> getSubtypesOf(DeclaredType baseType) {
         if (LOGGABLE) log(NbBundle.getBundle("org/netbeans/modules/javafx/editor/completion/Bundle").getString("NOT_IMPLEMENTED:_getSubtypesOf_") + baseType); // NOI18N
         return Collections.emptyList();
     }
 
     void resolveToolTip(final CompilationController controller) throws IOException {
         Phase resPhase = controller.toPhase(Phase.ANALYZED);
 
         if  ((resPhase.lessThan(Phase.ANALYZED)) || (isTreeBroken())) {
             if (LOGGABLE) log("resolveToolTip: phase: " + resPhase); // NOI18N
             return;
         }
         if (LOGGABLE) {
             log("  resolveToolTip start"); // NOI18N
         }
         Tree lastTree = null;
         while (path != null) {
             Tree tree = path.getLeaf();
             if (LOGGABLE) log("  resolveToolTip on " + tree.getJavaFXKind()); // NOI18N
             if (tree.getJavaFXKind() == Tree.JavaFXKind.METHOD_INVOCATION) {
                 FunctionInvocationTree mi = (FunctionInvocationTree) tree;
                 int startPos = lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset;
                 if (LOGGABLE) log("  startPos == " + startPos); // NOI18N
                 List<Tree> argTypes = getArgumentsUpToPos(mi.getArguments(), (int) sourcePositions.getEndPosition(root, mi.getMethodSelect()), startPos);
                 if (LOGGABLE) log("  argTypes = " + argTypes); // NOI18N
                 if (argTypes != null) {
                     TypeMirror[] types = new TypeMirror[argTypes.size()];
                     int j = 0;
                     for (Tree t : argTypes) {
                         types[j++] = controller.getTrees().getTypeMirror(JavaFXTreePath.getPath(root, t));
                         if (LOGGABLE) {
                             log("  types[j-1] == " + types[j-1]); // NOI18N
                         }
                     }
                     List<List<String>> params = null;
                     Tree mid = mi.getMethodSelect();
                     if (LOGGABLE) log("   mid == " + mid.getJavaFXKind() + mid); // NOI18N
                     if (LOGGABLE) {
                         log("    path " + path); // NOI18N
                         if (path != null) {
                             log("    path.getLeaf() == " + path.getLeaf()); // NOI18N
                         }
                     }
                     path = new JavaFXTreePath(path, mid);
                     switch (mid.getJavaFXKind()) {
                         case MEMBER_SELECT: {
                             ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                             path = new JavaFXTreePath(path, exp);
                             if (LOGGABLE) log("   path == " + path.getLeaf()); // NOI18N
                             JavafxcTrees trees = controller.getTrees();
                             final TypeMirror type = trees.getTypeMirror(path);
                             if (LOGGABLE) log("    type == " + type); // NOI18N
                             final Element element = trees.getElement(path);
                             if (LOGGABLE) log("    element == " + element); // NOI18N
                             final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface());
                             if (LOGGABLE) log("     isStatic == " + isStatic); // NOI18N
                             final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                             final JavafxcScope scope = controller.getTreeUtilities().getScope(path);
                             if (LOGGABLE) log("   scope == " + scope); // NOI18N
                             final TreeUtilities tu = controller.getTreeUtilities();
                             TypeElement enclClass = scope.getEnclosingClass();
                             final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                             ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
 
                                 public boolean accept(Element e, TypeMirror t) {
                                     boolean res = (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t);
                                     if (LOGGABLE) {
                                         log("   accept for " + e + " on " + t); // NOI18N
                                         log("   returning " + res); // NOI18N
                                     }
                                     return res;
                                 }
                             };
                             params = getMatchingParams(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree) mid).getIdentifier().toString(), types, controller.getTypes());
                             if (LOGGABLE) log("  params == " + params); // NOI18N
                             break;
                         }
                         case IDENTIFIER: {
                             final JavafxcScope scope = controller.getTreeUtilities().getScope(path);
                             if (LOGGABLE) log("   scope (2) == " + scope); // NOI18N
                             final TreeUtilities tu = controller.getTreeUtilities();
                             final TypeElement enclClass = scope.getEnclosingClass();
                             final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (path.getLeaf().getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION && ((BlockExpressionTree) path.getLeaf()).isStatic())) : false;
                             final ExecutableElement method = scope.getEnclosingMethod();
                             ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
 
                                 public boolean accept(Element e, TypeMirror t) {
                                     switch (e.getKind()) {
                                         case CONSTRUCTOR:
                                             return !e.getModifiers().contains(PRIVATE);
                                         case METHOD:
                                             return (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                                         default:
                                             return false;
                                     }
                                 }
                             };
                             String name = ((IdentifierTree) mid).getName().toString();
                             params = getMatchingParams(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), name, types, controller.getTypes());
                             if (LOGGABLE) log("  params (2) == " + params); // NOI18N
                             break;
                         }
                     }
                     if (LOGGABLE) log("  params (3) == " + params); // NOI18N
                     if (params != null) {
                         query.toolTip = new MethodParamsTipPaintComponent(params, types.length, query.component);
                     }
                     startPos = (int) sourcePositions.getEndPosition(root, mi.getMethodSelect());
                     String text = controller.getText().subSequence(startPos, offset).toString();
                     query.anchorOffset = startPos + text.indexOf('('); //NOI18N
                     query.toolTipOffset = startPos + text.lastIndexOf(','); //NOI18N
                     if (query.toolTipOffset < query.anchorOffset) {
                         query.toolTipOffset = query.anchorOffset;
                     }
                     return;
                 }
             }
             lastTree = tree;
             path = path.getParentPath();
         }
     }
 
     protected void addMethodArguments(FunctionInvocationTree mit) throws IOException {
         if (LOGGABLE) log("addMethodArguments " + mit); // NOI18N
         List<Tree> argTypes = getArgumentsUpToPos(mit.getArguments(), (int)sourcePositions.getEndPosition(root, mit.getMethodSelect()), offset);
         JavafxcTrees trees = controller.getTrees();
         if (argTypes != null) {
             TypeMirror[] types = new TypeMirror[argTypes.size()];
             int j = 0;
             for (Tree t : argTypes) {
                 JavaFXTreePath jfxtp = new JavaFXTreePath(path, t);
                 if (LOGGABLE) log("    jfxtp == " + jfxtp.getLeaf()); // NOI18N
                 types[j++] = controller.getTrees().getTypeMirror(jfxtp);
                 if (LOGGABLE) log("      types[j-1] == " + types[j-1]); // NOI18N
             }
             List<Pair<ExecutableElement, ExecutableType>> methods = null;
             String name = null;
             Tree mid = mit.getMethodSelect();
             if (LOGGABLE) {
                 log("    path " + path); // NOI18N
                 if (path != null) {
                     log("    path.getLeaf() == " + path.getLeaf()); // NOI18N
                 }
             }
             path = new JavaFXTreePath(path, mid);
             switch (mid.getJavaFXKind()) {
                 case MEMBER_SELECT: {
                     ExpressionTree exp = ((MemberSelectTree)mid).getExpression();
                     path = new JavaFXTreePath(path, exp);
                     final TypeMirror type = trees.getTypeMirror(path);
                     final Element element = trees.getElement(path);
                     final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface());
                     final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                     final TreeUtilities tu = controller.getTreeUtilities();
                     final JavafxcScope scope = tu.getScope(path);
                     TypeElement enclClass = scope.getEnclosingClass();
                     final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                     ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                         public boolean accept(Element e, TypeMirror t) {
                             return (!isStatic || e.getModifiers().contains(STATIC) ) && tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t);
                         }
                     };
                     methods = getMatchingExecutables(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree)mid).getIdentifier().toString(), types, controller.getTypes());
                     break;
                 }
                 case IDENTIFIER: {
                     final TreeUtilities tu = controller.getTreeUtilities();
                     final JavafxcScope scope = tu.getScope(path);
                     final TypeElement enclClass = scope.getEnclosingClass();
                     final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (path.getLeaf().getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION && ((BlockExpressionTree)path.getLeaf()).isStatic())) : false;
                     final ExecutableElement method = scope.getEnclosingMethod();
                     ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                         public boolean accept(Element e, TypeMirror t) {
                             switch (e.getKind()) {
                                 case LOCAL_VARIABLE:
                                 case EXCEPTION_PARAMETER:
                                 case PARAMETER:
                                     return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL));
                                 case FIELD:
                                     if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                                         return !isStatic;
                             }
                             return (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                         }
                     };
                     name = ((IdentifierTree)mid).getName().toString();
                     if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                         TypeMirror superclass = enclClass.getSuperclass();
                         methods = getMatchingExecutables(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types, controller.getTypes());
                     } else if (THIS_KEYWORD.equals(name) && enclClass != null) {
                         TypeMirror thisclass = enclClass.asType();
                         methods = getMatchingExecutables(thisclass, controller.getElementUtilities().getMembers(thisclass, acceptor), INIT, types, controller.getTypes());
                     } else {
                         Iterable<? extends Element> locals = controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor);
                         methods = getMatchingExecutables(enclClass != null ? enclClass.asType() : null, locals, name, types, controller.getTypes());
                         name = null;
                     }
                     break;
                 }
             }
             if (methods != null) {
                 Elements elements = controller.getElements();
                 for (Pair<ExecutableElement, ExecutableType> method : methods)
                    addResult(JavaFXCompletionItem.createParametersItem(method.a, method.b, query.anchorOffset, elements.isDeprecated(method.a), types.length, name));
             }
         }
     }
         private List<Tree> getArgumentsUpToPos(Iterable<? extends ExpressionTree> args, int startPos, int position) {
             List<Tree> ret = new ArrayList<Tree>();
             for (ExpressionTree e : args) {
                 int pos = (int)sourcePositions.getEndPosition(root, e);
                 if (pos != Diagnostic.NOPOS && position > pos) {
                     startPos = pos;
                     ret.add(e);
                 }
             }
             if (startPos < 0)
                 return ret;
             if (position > startPos) {
                 SafeTokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(startPos, position);
                 if (last != null && (last.token().id() == JFXTokenId.LPAREN || last.token().id() == JFXTokenId.COMMA))
                     return ret;
             }
             return null;
         }
 
         private List<Pair<ExecutableElement, ExecutableType>> getMatchingExecutables(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
             List<Pair<ExecutableElement, ExecutableType>> ret = new ArrayList<Pair<ExecutableElement, ExecutableType>>();
             for (Element e : elements) {
                 if ((e.getKind() == ElementKind.CONSTRUCTOR || e.getKind() == ElementKind.METHOD) && name.contentEquals(e.getSimpleName())) {
                     List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                     int parSize = params.size();
                     boolean varArgs = ((ExecutableElement)e).isVarArgs();
                     if (!varArgs && (parSize < argTypes.length)) {
                         continue;
                     }
                     ExecutableType eType = (ExecutableType)asMemberOf(e, type, types);
                     if (parSize == 0) {
                         ret.add(new Pair<ExecutableElement,ExecutableType>((ExecutableElement)e, eType));
                     } else {
                         Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                         TypeMirror param = null;
                         for (int i = 0; i <= argTypes.length; i++) {
                             if (parIt.hasNext()) {
                                 param = parIt.next();
 //                                if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
 //                                    param = ((ArrayType)param).getComponentType();
                             } else if (!varArgs) {
                                 break;
                             }
                             if (i == argTypes.length) {
                                 ret.add(new Pair<ExecutableElement, ExecutableType>((ExecutableElement)e, eType));
                                 break;
                             }
                             if (argTypes[i] == null || !types.isAssignable(argTypes[i], param))
                                 break;
                         }
                     }
                 }
             }
             return ret;
         }
 
         private List<List<String>> getMatchingParams(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
             if (LOGGABLE) log("getMatchingParams type == " + type + " name == " + name); // NOI18N
             List<List<String>> ret = new ArrayList<List<String>>();
             for (Element e : elements) {
                 if (LOGGABLE) log("   e == " + e); // NOI18N
                 if ((e.getKind() == ElementKind.CONSTRUCTOR || e.getKind() == ElementKind.METHOD) && name.contentEquals(e.getSimpleName())) {
                     List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                     int parSize = params.size();
                     if (LOGGABLE) log("   parSize == " + parSize); // NOI18N
                     boolean varArgs = ((ExecutableElement)e).isVarArgs();
                     if (!varArgs && (parSize < argTypes.length)) {
                         continue;
                     }
                     if (parSize == 0) {
                         ret.add(Collections.<String>singletonList(NbBundle.getMessage(JavaFXCompletionProvider.class, "JCP-no-parameters"))); // NOI18N
                     } else {
                        TypeMirror tm = asMemberOf(e, type, types);
                        if (!(tm instanceof ExecutableType)) continue; // error type, #173250
                        ExecutableType eType = (ExecutableType)tm;
                         if (LOGGABLE) log("  eType == " + eType); // NOI18N
                         Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                         TypeMirror param = null;
                         for (int i = 0; i <= argTypes.length; i++) {
                             log("    i == " + i); // NOI18N
                             if (parIt.hasNext()) {
                                 param = parIt.next();
                                 if (LOGGABLE) log("      param == " + param); // NOI18N
                                 if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
                                     param = ((ArrayType)param).getComponentType();
                             } else if (!varArgs) {
                                 break;
                             }
                             if (i == argTypes.length) {
                                 if (LOGGABLE) log("   i == argTypes.length"); // NOI18N
                                 List<String> paramStrings = new ArrayList<String>(parSize);
                                 Iterator<? extends TypeMirror> tIt = eType.getParameterTypes().iterator();
                                 for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                                     VariableElement ve = it.next();
                                     StringBuffer sb = new StringBuffer();
                                     sb.append(tIt.next());
                                     if (varArgs && !tIt.hasNext())
                                         sb.delete(sb.length() - 2, sb.length()).append("..."); //NOI18N
                                     CharSequence veName = ve.getSimpleName();
                                     if (veName != null && veName.length() > 0) {
                                         sb.append(" "); // NOI18N
                                         sb.append(veName);
                                     }
                                     if (it.hasNext()) {
                                         sb.append(", "); // NOI18N
                                     }
                                     paramStrings.add(sb.toString());
                                 }
                                 ret.add(paramStrings);
                                 break;
                             }
                             if (LOGGABLE) log("    will check " + argTypes[i] + " " + param); // NOI18N
                             if (argTypes[i] != null && !types.isAssignable(argTypes[i], param)) {
                                 break;
                             }
                         }
                     }
                 } else {
                     if (LOGGABLE) log("   e.getKind() == " + e.getKind()); // NOI18N
                 }
             }
             return ret.isEmpty() ? null : ret;
         }
 
 //        private Set<TypeMirror> getMatchingArgumentTypes(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, TypeMirror[] typeArgTypes, Types types, TypeUtilities tu) {
 //            Set<TypeMirror> ret = new HashSet<TypeMirror>();
 //            for (Element e : elements) {
 //                if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
 //                    List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
 //                    int parSize = params.size();
 //                    boolean varArgs = ((ExecutableElement)e).isVarArgs();
 //                    if (!varArgs && (parSize <= argTypes.length))
 //                        continue;
 //                    ExecutableType meth = (ExecutableType)asMemberOf(e, type, types);
 //                    Iterator<? extends TypeMirror> parIt = meth.getParameterTypes().iterator();
 //                    TypeMirror param = null;
 //                    for (int i = 0; i <= argTypes.length; i++) {
 //                        if (parIt.hasNext())
 //                            param = parIt.next();
 //                        else if (!varArgs)
 //                            break;
 //                        if (i == argTypes.length) {
 //                            if (typeArgTypes != null && param.getKind() == TypeKind.DECLARED && typeArgTypes.length == meth.getTypeVariables().size())
 //                                param = tu.substitute(param, meth.getTypeVariables(), Arrays.asList(typeArgTypes));
 //                            TypeMirror toAdd = null;
 //                            if (i < parSize)
 //                                toAdd = param;
 //                            if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
 //                                toAdd = ((ArrayType)param).getComponentType();
 //                            if (toAdd != null && ret.add(toAdd)) {
 //                                TypeMirror toRemove = null;
 //                                for (TypeMirror tm : ret) {
 //                                    if (tm != toAdd) {
 //                                        TypeMirror tmErasure = types.erasure(tm);
 //                                        TypeMirror toAddErasure = types.erasure(toAdd);
 //                                        if (types.isSubtype(toAddErasure, tmErasure)) {
 //                                            toRemove = toAdd;
 //                                            break;
 //                                        } else if (types.isSubtype(tmErasure, toAddErasure)) {
 //                                            toRemove = tm;
 //                                            break;
 //                                        }
 //                                    }
 //                                }
 //                                if (toRemove != null)
 //                                    ret.remove(toRemove);
 //                            }
 //                            break;
 //                        }
 //                        if (argTypes[i] == null)
 //                            break;
 //                        if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
 //                            if (types.isAssignable(argTypes[i], param))
 //                                varArgs = false;
 //                            else if (!types.isAssignable(argTypes[i], ((ArrayType)param).getComponentType()))
 //                                break;
 //                        } else if (!types.isAssignable(argTypes[i], param))
 //                            break;
 //                    }
 //                }
 //            }
 //            return ret.isEmpty() ? null : ret;
 //        }
 
     protected void addKeyword(String kw, String postfix, boolean smartType) {
         if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(kw, postfix, query.anchorOffset, smartType));
         }
     }
 
     protected void addKeywordsForClassBody() {
         for (String kw : CLASS_BODY_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     @SuppressWarnings("fallthrough")
     protected void addKeywordsForStatement() {
         if (LOGGABLE) log("addKeywordsForStatement"); // NOI18N
         for (String kw : STATEMENT_KEYWORDS) {
             String postfix = " ("; // NOI18N
             if (TRY_KEYWORD.equals(kw)) {
                 postfix = " {"; // NOI18N
             }
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, postfix, query.anchorOffset, false));
             }
         }
         for (String kw : STATEMENT_SPACE_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
         if (JavaFXCompletionProvider.startsWith(RETURN_KEYWORD, prefix)) {
             JavaFXTreePath mth = JavaFXCompletionProvider.getPathElementOfKind(Tree.JavaFXKind.FUNCTION_DEFINITION, path);
             if (LOGGABLE) log("   mth == " + mth); // NOI18N
             String postfix = SPACE;
             if (mth != null) {
                 Tree rt = ((FunctionDefinitionTree) mth.getLeaf()).getFunctionValue().getType();
                 if (LOGGABLE) log("    rt == " + rt + "   kind == " + (rt == null?"":rt.getJavaFXKind())); // NOI18N
                 if ((rt == null) || (rt.getJavaFXKind() == JavaFXKind.TYPE_UNKNOWN)) {
                     postfix = SEMI;
                 }
                 // TODO: handle Void return type ...
             }
             addResult(JavaFXCompletionItem.createKeywordItem(RETURN_KEYWORD, postfix, query.anchorOffset, false));
         }
         JavaFXTreePath tp = getPath();
         while (tp != null) {
             switch (tp.getLeaf().getJavaFXKind()) {
                 case FOR_EXPRESSION_IN_CLAUSE:
                 case FOR_EXPRESSION_FOR:
                 case WHILE_LOOP:
                     if (JavaFXCompletionProvider.startsWith(CONTINUE_KEYWORD, prefix)) {
                         addResult(JavaFXCompletionItem.createKeywordItem(CONTINUE_KEYWORD, SEMI, query.anchorOffset, false));
                     }
 
 /*
                 case SWITCH:
                     if (JavaFXCompletionProvider.startsWith(BREAK_KEYWORD, prefix)) {
                         addResult(JavaFXCompletionItem.createKeywordItem(BREAK_KEYWORD, SEMI, query.anchorOffset, false));
                     }
                     break;*/
             }
             tp = tp.getParentPath();
         }
     }
 
     protected void addValueKeywords() {
         if (JavaFXCompletionProvider.startsWith(FALSE_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(FALSE_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(TRUE_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(TRUE_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(NULL_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(NULL_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(NEW_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(NEW_KEYWORD, SPACE, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(BIND_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(BIND_KEYWORD, SPACE, query.anchorOffset, false));
         }
     }
 
     protected void addClassModifiers(Set<Modifier> modifiers) {
         List<String> kws = new ArrayList<String>();
         if (!modifiers.contains(PUBLIC) && !modifiers.contains(PRIVATE)) {
             kws.add(PUBLIC_KEYWORD);
         }
         if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
             kws.add(ABSTRACT_KEYWORD);
         }
         kws.add(CLASS_KEYWORD);
         for (String kw : kws) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     protected void addMemberModifiers(Set<Modifier> modifiers, boolean isLocal) {
         if (LOGGABLE) log("addMemberModifiers"); // NOI18N
         List<String> kws = new ArrayList<String>();
         if (isLocal) {
             // TODO:
         } else {
             kws.add(PUBLIC_KEYWORD);
             kws.add(PROTECTED_KEYWORD);
             kws.add(PACKAGE_KEYWORD);
             kws.add(PUBLIC_INIT_KEYWORD);
             kws.add(PUBLIC_READ_KEYWORD);
             // TODO: check this:
             if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
                 kws.add(ABSTRACT_KEYWORD);
             }
         }
         for (String kw : kws) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     /**
      * This methods hacks over issue #135926. To prevent NPE we first complete
      * all symbols that are classes and not inner classes in a package.
      * @param pe
      * @return pe.getEnclosedElements() but without the NPE
      */
     private List<? extends Element> getEnclosedElements(PackageElement pe) {
         Symbol s = (Symbol)pe;
         for (Scope.Entry e = s.members().elems; e != null; e = e.sibling) {
             if (e.sym != null) {
                 try {
                     e.sym.complete();
                 } catch (RuntimeException x) {
                     if (logger.isLoggable(Level.FINEST)) {
                         logger.log(Level.FINEST,"Let's see whether we survive this: ",x); // NOI18N
                     }
                 }
             }
         }
         return pe.getEnclosedElements();
     }
 
     protected void addPackageContent(PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType, boolean insideNew) {
         if (LOGGABLE) log("addPackageContent " + pe); // NOI18N
         Elements elements = controller.getElements();
         JavafxTypes types = controller.getJavafxTypes();
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         for (Element e : getEnclosedElements(pe)) {
             if (e.getKind().isClass() || e.getKind() == ElementKind.INTERFACE) {
                 String name = e.getSimpleName().toString();
                 if (! controller.getTreeUtilities().isAccessible(scope, e)) {
                     if (LOGGABLE) log("    not accessible " + name); // NOI18N
                     continue;
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) &&
                         !name.contains("$")) { // NOI18N
                     addResult(JavaFXCompletionItem.createTypeItem((TypeElement) e, (DeclaredType) e.asType(), query.anchorOffset, elements.isDeprecated(e), insideNew, false, false));
                 }
                 for (Element ee : e.getEnclosedElements()) {
                     if (ee.getKind().isClass() || ee.getKind() == ElementKind.INTERFACE) {
                         String ename = ee.getSimpleName().toString();
                         if (!controller.getTreeUtilities().isAccessible(scope, ee)) {
                             if (LOGGABLE) log("    not accessible " + ename); // NOI18N
                             continue;
                         }
                         log(ename + " isJFXClass " + types.isJFXClass((Symbol) ee)); // NOI18N
                         if (JavaFXCompletionProvider.startsWith(ename, prefix) &&
                                 types.isJFXClass((Symbol) ee)) {
                             addResult(JavaFXCompletionItem.createTypeItem((TypeElement) ee, (DeclaredType) ee.asType(), query.anchorOffset, elements.isDeprecated(ee), insideNew, false, false));
                         }
                     }
                 }
             }
         }
         String pkgName = pe.getQualifiedName() + "."; //NOI18N
         if (prefix != null && prefix.length() > 0) {
             pkgName += prefix;
         }
         addPackages(pkgName);
     }
 
     protected void addBasicTypes() {
         if (LOGGABLE) log("addBasicTypes "); // NOI18N
         addBasicType("Boolean", "boolean"); // NOI18N
         addBasicType("Integer", "int"); // NOI18N
         addBasicType("Number", "float"); // NOI18N
         addBasicType("String", "String"); // NOI18N
         // new ones in 1.1:
         addBasicType("Long", "long"); // NOI18N
         addBasicType("Short", "short"); // NOI18N
         addBasicType("Byte", "byte"); // NOI18N
         addBasicType("Float", "float"); // NOI18N
         addBasicType("Double", "double"); // NOI18N
         addBasicType("Character", "char"); // NOI18N
     }
 
     private void addBasicType(String name1, String name2) {
         if (LOGGABLE) log("  addBasicType " + name1 + " : " + name2); // NOI18N
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope); // NOI18N
             for (Element local : scope.getLocalElements()) {
                 if (LOGGABLE) log("    local == " + local.getSimpleName() + "  kind: " + local.getKind() + "  class: " + local.getClass().getName() + "  asType: " + local.asType()); // NOI18N
                 if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                     if (! (local instanceof TypeElement)) {
                         if (LOGGABLE) log("    " + local.getSimpleName() + " not TypeElement"); // NOI18N
                         continue;
                     }
                     TypeElement te = (TypeElement) local;
                     String name = local.getSimpleName().toString();
                     if (name.equals(name2)) {
                         if (JavaFXCompletionProvider.startsWith(name1, prefix)) {
                             if (LOGGABLE) log("    found " + name1); // NOI18N
                             if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                                 addResult(JavaFXCompletionItem.createTypeItem(name1, query.anchorOffset, false, false, true));
                             } else {
                                 DeclaredType dt = (DeclaredType) local.asType();
                                 addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, false, false, true, false));
                             }
                             return;
                         }
                     }
                 }
             }
             scope = scope.getEnclosingScope();
         }
     }
 
     protected void addLocalAndImportedTypes(final EnumSet<ElementKind> kinds, final DeclaredType baseType, final Set<? extends Element> toExclude, boolean insideNew, TypeMirror smart) {
         if (LOGGABLE) log("addLocalAndImportedTypes"); // NOI18N
         JavafxcTrees trees = controller.getTrees();
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         JavafxcScope originalScope = scope;
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope); // NOI18N
             addLocalAndImportedTypes(scope.getLocalElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,false);
             scope = scope.getEnclosingScope();
         }
         Element e = trees.getElement(path);
         while (e != null && e.getKind() != ElementKind.PACKAGE) {
             e = e.getEnclosingElement();
         }
         if (e != null) {
             if (LOGGABLE) log("will scan package " + e.getSimpleName()); // NOI18N
             PackageElement pkge = (PackageElement)e;
             addLocalAndImportedTypes(getEnclosedElements(pkge), kinds, baseType, toExclude, insideNew, smart, originalScope, pkge,false);
         }
         addPackages(""); // NOI18N
         if (query.queryType == JavaFXCompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
             addAllTypes(kinds, insideNew, prefix);
         } else {
             query.hasAdditionalItems = true;
         }
     }
 
     private void addLocalAndImportedTypes(Iterable<? extends Element> from,
             final EnumSet<ElementKind> kinds, final DeclaredType baseType,
             final Set<? extends Element> toExclude,
             boolean insideNew, TypeMirror smart, JavafxcScope originalScope,
             PackageElement myPackage,boolean simpleNameOnly) {
         final Elements elements = controller.getElements();
         for (Element local : from) {
             if (LOGGABLE) log("    local == " + local); // NOI18N
             String name = local.getSimpleName().toString();
             if (name.contains("$")) { // NOI18N
                 continue;
             }
             if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                 if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                     continue;
                 }
                 DeclaredType dt = (DeclaredType) local.asType();
                 TypeElement te = (TypeElement) local;
                 if (!controller.getTreeUtilities().isAccessible(originalScope, te)) {
                     if (LOGGABLE) log("    not accessible " + name); // NOI18N
                     continue;
                 }
                 Element parent = te.getEnclosingElement();
                 if (parent.getKind() == ElementKind.CLASS) {
                     if (!controller.getTreeUtilities().isAccessible(originalScope, parent)) {
                         if (LOGGABLE) log("    parent not accessible " + name); // NOI18N
                         continue;
                     }
                 }
                 if (smart != null && local.asType() == smart) {
                     addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, elements.isDeprecated(local), insideNew, true, false));
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) && !name.contains("$")) { // NOI18N
                     if (simpleNameOnly) {
                         addResult(JavaFXCompletionItem.createTypeItem(local.getSimpleName().toString(), query.anchorOffset, elements.isDeprecated(local), insideNew, false));
                     } else {
                         addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, elements.isDeprecated(local), insideNew, false, false));
                     }
                 }
                 if (parent == myPackage) {
                    if (LOGGABLE) log("   will check inner classes of: " + local); // NOI18N
                    if (local.getEnclosedElements() != null) {
                         addLocalAndImportedTypes(local.getEnclosedElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,true);
                    }
                 }
             }
         }
     }
 
     protected void addLocalAndImportedFunctions() {
         if (LOGGABLE) log("addLocalAndImportedFunctions"); // NOI18N
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope); // NOI18N
             for (Element local : scope.getLocalElements()) {
                 if (LOGGABLE) log("    local == " + local); // NOI18N
                 String name = local.getSimpleName().toString();
                 if (name.contains("$")) { // NOI18N
                     continue;
                 }
                 if (local.getKind() == ElementKind.METHOD) {
                     if (JavaFXCompletionProvider.startsWith(name, prefix) && !name.contains("$")) { // NOI18N
                         addResult(JavaFXCompletionItem.createExecutableItem(
                                 (ExecutableElement) local,
                                 (ExecutableType) local.asType(),
                                 query.anchorOffset, false, false, false, false));
                     }
                 }
             }
             scope = scope.getEnclosingScope();
         }
     }
 
     /**
      * @param simpleName name of a class or fully qualified name of a class
      * @return TypeElement or null if the passed in String does not denote a class
      */
     protected TypeElement findTypeElement(String simpleName) {
         if (LOGGABLE) log("findTypeElement: " + simpleName); // NOI18N
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope); // NOI18N
             TypeElement res = findTypeElement(scope.getLocalElements(), simpleName,null);
             if (res != null) {
                 return res;
             }
             scope = scope.getEnclosingScope();
         }
         Element e = trees.getElement(p);
         while (e != null && e.getKind() != ElementKind.PACKAGE) {
             e = e.getEnclosingElement();
         }
         if (e != null) {
             PackageElement pkge = (PackageElement)e;
             return findTypeElement(getEnclosedElements(pkge), simpleName,pkge);
         }
         return null;
     }
 
     /**
      * @param simpleName name of a class or fully qualified name of a class
      * @param myPackage can be null - if not null the inner classes of classes from this package will be checked
      * @return TypeElement or null if the passed in String does not denote a class
      */
     private TypeElement findTypeElement(Iterable<? extends Element> from, String simpleName,PackageElement myPackage) {
         if (LOGGABLE) log("  private findTypeElement " + simpleName + " in package " + myPackage); // NOI18N
         Elements elements = controller.getElements();
         for (Element local : from) {
             if (LOGGABLE) log("    local == " + local.getSimpleName() + "  kind: " + local.getKind() + "  class: " + local.getClass().getName() + "  asType: " + local.asType()); // NOI18N
             if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                 if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                     if (LOGGABLE) log("        is not TypeKind.DECLARED -- ignoring"); // NOI18N
                     continue;
                 }
                 if (local instanceof TypeElement) {
                     String name = local.getSimpleName().toString();
                     if (name.equals(simpleName)) {
                         return (TypeElement) local;
                     }
 
                     PackageElement pe = elements.getPackageOf(local);
                     String fullName = pe.getQualifiedName().toString() + '.' + name; // NOI18N
                     if (fullName.equals(simpleName)) {
                         return (TypeElement) local;
                     }
                     if (pe == myPackage) {
                         if (LOGGABLE) log("   will check inner classes of: " + local); // NOI18N
                         TypeElement res = findTypeElement(local.getEnclosedElements(), simpleName,null);
                         if (res != null) {
                             return res;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     protected void addAllTypes(EnumSet<ElementKind> kinds, boolean insideNew, String myPrefix) {
         if (LOGGABLE) log(" addAllTypes "); // NOI18N
         for (ElementHandle<TypeElement> name :
             controller.getClasspathInfo().getClassIndex().getDeclaredTypes(
                 myPrefix != null ? myPrefix : EMPTY,
                 NameKind.PREFIX,
                 EnumSet.allOf(SearchScope.class))) {
             String[] sigs = name.getSignatures();
             if ((sigs == null) || (sigs.length == 0)) {
                 continue;
             }
             String sig = sigs[0];
             int firstDollar = sig.indexOf('$'); // NOI18N
             if (firstDollar >= 0) {
                 int secondDollar = sig.indexOf('$', firstDollar); // NOI18N
                 if (secondDollar >= 0) {
                     // we don't want to show second level inner classes
                     continue;
                 }
             }
             if (!name.getQualifiedName().startsWith("com.sun.") && // NOI18N
                (!name.getQualifiedName().startsWith("sun.")) // NOI18N
                 ) {
                 LazyTypeCompletionItem item = LazyTypeCompletionItem.create(
                         name, kinds,
                         query.anchorOffset,
                         controller, insideNew);
                 addResult(item);
             }
         }
     }
 
     protected SafeTokenSequence<JFXTokenId> findLastNonWhitespaceToken(Tree tree, int position) {
         int startPos = (int) getSourcePositions().getStartPosition(root, tree);
         return findLastNonWhitespaceToken(startPos, position);
     }
 
     protected SafeTokenSequence<JFXTokenId> findLastNonWhitespaceToken(int startPos, int endPos) {
         TokenSequence<JFXTokenId> ts_ = ((TokenHierarchy<?>) controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, controller.getDocument(), cancellable);
         ts.move(endPos);
         ts = previousNonWhitespaceToken(ts);
         if (ts == null || ts.offset() < startPos) {
             return null;
         }
         return ts;
     }
 
     private SafeTokenSequence<JFXTokenId> findFirstNonWhitespaceToken(Tree tree, int position) {
         int startPos = (int) getSourcePositions().getStartPosition(root, tree);
         return findFirstNonWhitespaceToken(startPos, position);
     }
 
     protected SafeTokenSequence<JFXTokenId> findFirstNonWhitespaceToken(int startPos, int endPos) {
         TokenSequence<JFXTokenId> ts_ = ((TokenHierarchy<?>) controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, controller.getDocument(), cancellable);
         ts.move(startPos);
         ts = nextNonWhitespaceToken(ts);
         if (ts == null || ts.offset() >= endPos) {
             return null;
         }
         return ts;
     }
 
     protected Tree getArgumentUpToPos(Iterable<? extends ExpressionTree> args, int startPos, int cursorPos) {
         for (ExpressionTree e : args) {
             int argStart = (int) sourcePositions.getStartPosition(root, e);
             int argEnd = (int) sourcePositions.getEndPosition(root, e);
             if (argStart == Diagnostic.NOPOS || argEnd == Diagnostic.NOPOS) {
                 continue;
             }
             if (cursorPos >= argStart && cursorPos < argEnd) {
                 return e;
             } else {
                 SafeTokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(startPos, cursorPos);
                 if (last == null) {
                     continue;
                 }
                 if (last.token().id() == JFXTokenId.LPAREN) {
                     return e;
                 }
                 if (last.token().id() == JFXTokenId.COMMA && cursorPos - 1 == argEnd) {
                     return e;
                 }
             }
         }
         return null;
     }
         /**
      *
      * @param source
      */
     protected void useFakeSource(String source, final int pos) {
         if (LOGGABLE) log("useFakeSource " + source + " pos == " + pos); // NOI18N
         if (usingFakeSource > 1) {
             // allow to recurse only twice ;-)
             return;
         }
         try {
             usingFakeSource++;
             FileSystem fs = FileUtil.createMemoryFileSystem();
             final FileObject fo = fs.getRoot().createData("tmp" + (new Random().nextLong()) + ".fx"); // NOI18N
             Writer w = new OutputStreamWriter(fo.getOutputStream());
             w.write(source);
             w.close();
             if (LOGGABLE) log("  source written to " + fo); // NOI18N
             ClasspathInfo info = ClasspathInfo.create(controller.getFileObject());
             JavaFXParserResult parserResult = JavaFXParserResult.create(Source.create(fo), info);
             if (LOGGABLE) log("  JavaFXParserResult obtained " + parserResult); // NOI18N
             CompilationController.create(parserResult).runWhenScanFinished(new Task<CompilationController>() {
                 public void run(CompilationController fakeController) throws Exception {
                     if (LOGGABLE) log("    scan finished"); // NOI18N
                     JavaFXCompletionEnvironment env = query.getCompletionEnvironment(fakeController, pos,true);
                     if (LOGGABLE) log("    env == " + env); // NOI18N
                     if (fakeController.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                         if (LOGGABLE) log("    fake failed to analyze -- returning"); // NOI18N
                         return;
                     }
                     if (LOGGABLE) log("    fake analyzed"); // NOI18N
                     if (! env.isTreeBroken()) {
                         if (LOGGABLE) log("    fake non-broken tree"); // NOI18N
                         final Tree leaf = env.getPath().getLeaf();
                         env.inside(leaf);
                         // try to remove faked entries:
                         String fakeName = fo.getName();
                         Set<JavaFXCompletionItem> toRemove = new TreeSet<JavaFXCompletionItem>();
                         for (JavaFXCompletionItem r : query.results) {
                             if (LOGGABLE) log("    checking " + r.getLeftHtmlText()); // NOI18N
                             if (r.getLeftHtmlText().contains(fakeName)) {
                                 if (LOGGABLE) log("    will remove " + r); // NOI18N
                                 toRemove.add(r);
                             }
                         }
                         query.results.removeAll(toRemove);
                     }
                 }
             });
         } catch (IOException ex) {
             if (LOGGABLE) {
                 logger.log(Level.FINE,"useFakeSource failed: ",ex); // NOI18N
             }
         } catch (ParseException e) {
             Exceptions.printStackTrace(e);
         } finally {
             usingFakeSource--;
         }
     }
 
     protected static SafeTokenSequence<JFXTokenId> nextNonWhitespaceToken(SafeTokenSequence<JFXTokenId> ts) {
         while (ts.moveNext()) {
             switch (ts.token().id()) {
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     break;
                 default:
                     return ts;
             }
         }
         return null;
     }
 
     static SafeTokenSequence<JFXTokenId> previousNonWhitespaceToken(SafeTokenSequence<JFXTokenId> ts) {
         while (ts.movePrevious()) {
             switch (ts.token().id()) {
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     break;
                 default:
                     return ts;
             }
         }
         return null;
     }
 
     protected static TypeMirror asMemberOf(Element element, TypeMirror type, Types types) {
         TypeMirror ret = element.asType();
         TypeMirror enclType = element.getEnclosingElement().asType();
         if (enclType.getKind() == TypeKind.DECLARED) {
             enclType = types.erasure(enclType);
         }
         while (type != null && type.getKind() == TypeKind.DECLARED) {
             if (types.isSubtype(type, enclType)) {
                 ret = types.asMemberOf((DeclaredType) type, element);
                 break;
             }
             type = ((DeclaredType) type).getEnclosingType();
         }
         return ret;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 
     private static class Pair<A, B> {
 
         private A a;
         private B b;
 
         private Pair(A a, B b) {
             this.a = a;
             this.b = b;
         }
     }
 
 }
