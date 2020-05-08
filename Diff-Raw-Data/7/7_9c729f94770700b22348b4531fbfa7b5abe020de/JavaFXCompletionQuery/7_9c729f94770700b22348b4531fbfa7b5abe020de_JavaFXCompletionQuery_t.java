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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
 import com.sun.tools.javafx.tree.*;
 
 import org.netbeans.api.editor.completion.Completion;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.*;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.javafx.editor.JavaCompletionDoc;
 import org.netbeans.modules.javafx.editor.completion.environment.*;
 import org.netbeans.spi.editor.completion.CompletionDocumentation;
 import org.netbeans.spi.editor.completion.CompletionResultSet;
 import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
 import org.openide.filesystems.FileObject;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import javax.lang.model.element.Element;
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.*;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 public final class JavaFXCompletionQuery extends AsyncCompletionQuery implements Task<CompilationController> {
 
     // -J-Dorg.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.level=FINE
     private static final Logger logger = Logger.getLogger(JavaFXCompletionQuery.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     public static final String ERROR = "<error>"; // NOI18N
     public static final String INIT = "<init>"; // NOI18N
     public static final String SPACE = " "; // NOI18N
     public static final String COLON = ":"; // NOI18N
     public static final String SEMI = ";"; // NOI18N
     public static final String EMPTY = ""; // NOI18N
     public static final String ABSTRACT_KEYWORD = "abstract"; // NOI18N
     public static final String AFTER_KEYWORD = "after"; // NOI18N
     public static final String AND_KEYWORD = "and"; // NOI18N
     public static final String AS_KEYWORD = "as"; // NOI18N
     public static final String ASSERT_KEYWORD = "assert"; // NOI18N
     public static final String AT_KEYWORD = "at"; // NOI18N
     public static final String ATTRIBUTE_KEYWORD = "attribute"; // NOI18N
     public static final String BEFORE_KEYWORD = "before"; // NOI18N
     public static final String BIND_KEYWORD = "bind"; // NOI18N
     public static final String BOUND_KEYWORD = "bound"; // NOI18N
     public static final String BREAK_KEYWORD = "break"; // NOI18N
     public static final String CATCH_KEYWORD = "catch"; // NOI18N
     public static final String CLASS_KEYWORD = "class"; // NOI18N
     public static final String CONTINUE_KEYWORD = "continue"; // NOI18N
     public static final String DEF_KEYWORD = "def"; // NOI18N
     public static final String DELETE_KEYWORD = "delete"; // NOI18N
     public static final String ELSE_KEYWORD = "else"; // NOI18N
     public static final String EXCLUSIVE_KEYWORD = "exclusive"; // NOI18N
     public static final String EXTENDS_KEYWORD = "extends"; // NOI18N
     public static final String FALSE_KEYWORD = "false"; // NOI18N
     public static final String FINALLY_KEYWORD = "finally"; // NOI18N
     public static final String FIRST_KEYWORD = "first"; // NOI18N
     public static final String FOR_KEYWORD = "for"; // NOI18N
     public static final String FROM_KEYWORD = "from"; // NOI18N
     public static final String FUNCTION_KEYWORD = "function"; // NOI18N
     public static final String IF_KEYWORD = "if"; // NOI18N
     public static final String IMPORT_KEYWORD = "import"; // NOI18N
     public static final String INDEXOF_KEYWORD = "indexof"; // NOI18N
     public static final String IN_KEYWORD = "in"; // NOI18N
     public static final String INIT_KEYWORD = "init"; // NOI18N
     public static final String INSERT_KEYWORD = "insert"; // NOI18N
     public static final String INSTANCEOF_KEYWORD = "instanceof"; // NOI18N
     public static final String INTO_KEYWORD = "into"; // NOI18N
     public static final String INVERSE_KEYWORD = "inverse"; // NOI18N
     public static final String LAST_KEYWORD = "last"; // NOI18N
     public static final String LAZY_KEYWORD = "lazy"; // NOI18N
     public static final String MIXIN_KEYWORD = "mixin"; // NOI18N
     public static final String MOD_KEYWORD = "mod"; // NOI18N
     public static final String NEW_KEYWORD = "new"; // NOI18N
     public static final String NOT_KEYWORD = "not"; // NOI18N
     public static final String NULL_KEYWORD = "null"; // NOI18N
     public static final String ON_KEYWORD = "on"; // NOI18N
     public static final String OR_KEYWORD = "or"; // NOI18N
     public static final String OVERRIDE_KEYWORD = "override"; // NOI18N
     public static final String PACKAGE_KEYWORD = "package"; // NOI18N
     public static final String POSTINIT_KEYWORD = "postinit"; // NOI18N
     public static final String PRIVATE_KEYWORD = "private"; // NOI18N
     public static final String PROTECTED_KEYWORD = "protected"; // NOI18N
     public static final String PUBLIC_KEYWORD = "public"; // NOI18N
     public static final String PUBLIC_INIT_KEYWORD = "public-init"; // NOI18N
     public static final String PUBLIC_READ_KEYWORD = "public-read"; // NOI18N
     public static final String REPLACE_KEYWORD = "replace"; // NOI18N
     public static final String RETURN_KEYWORD = "return"; // NOI18N
     public static final String REVERSE_KEYWORD = "reverse"; // NOI18N
     public static final String SIZEOF_KEYWORD = "sizeof"; // NOI18N
     public static final String STATIC_KEYWORD = "static"; // NOI18N
     public static final String STEP_KEYWORD = "step"; // NOI18N
     public static final String SUPER_KEYWORD = "super"; // NOI18N
     public static final String THEN_KEYWORD = "then"; // NOI18N
     public static final String THIS_KEYWORD = "this"; // NOI18N
     public static final String THROW_KEYWORD = "throw"; // NOI18N
     public static final String TRIGGER_KEYWORD = "trigger"; // NOI18N
     public static final String TRUE_KEYWORD = "true"; // NOI18N
     public static final String TRY_KEYWORD = "try"; // NOI18N
     public static final String TWEEN_KEYWORD = "tween"; // NOI18N
     public static final String TYPEOF_KEYWORD = "typeof"; // NOI18N
     public static final String VAR_KEYWORD = "var"; // NOI18N
     public static final String WHERE_KEYWORD = "where"; // NOI18N
     public static final String WHILE_KEYWORD = "while"; // NOI18N
     public static final String WITH_KEYWORD = "with"; // NOI18N
 
     static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
 
     public Set<JavaFXCompletionItem> results;
     boolean hasAdditionalItems;
     JToolTip toolTip;
     private CompletionDocumentation documentation;
     public int anchorOffset;
     int toolTipOffset;
     JTextComponent component;
     public int queryType;
     private int caretOffset;
     private String filterPrefix;
     private ElementHandle element;
     private boolean hasTask;
 
     public JavaFXCompletionQuery(int queryType, int caretOffset, boolean hasTask) {
         super();
         this.queryType = queryType;
         this.caretOffset = caretOffset;
         this.hasTask = hasTask;
     }
 
     boolean isTaskCancelled0() {
         return hasTask && isTaskCancelled();
     }
 
     void setElement(ElementHandle element) {
         this.element = element;
     }
 
     public JTextComponent getComponent() {
         return component;
     }
 
     @Override
     protected void preQueryUpdate(JTextComponent component) {
         int newCaretOffset = component.getSelectionStart();
         if (newCaretOffset >= caretOffset) {
             try {
                 if (isJavaIdentifierPart(component.getDocument().getText(caretOffset, newCaretOffset - caretOffset))) {
                     return;
                 }
             } catch (BadLocationException e) {
             }
         }
         Completion.get().hideCompletion();
     }
 
     @Override
     protected void prepareQuery(JTextComponent component) {
         this.component = component;
     }
 
     @Override
     protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
         try {
             this.caretOffset = caretOffset;
             if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE || JavaFXCompletionProvider.isJavaFXContext(component, caretOffset)) {
                 results = null;
                 documentation = null;
                 toolTip = null;
                 anchorOffset = -1;
                 JavaFXSource js = JavaFXSource.forDocument(doc);
                 if (js != null) {
                     if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE && element != null) {
                         FileObject fo = JavaFXSourceUtils.getFile(element, js.getClasspathInfo());
                         if (fo != null) {
                             js = JavaFXSource.forFileObject(fo);
                         }
                     }
                     Future<Void> f = js.runWhenScanFinished(this, true);
                     if (!f.isDone()) {
                         component.putClientProperty("completion-active", Boolean.FALSE); // NOI18N
                         resultSet.setWaitText(NbBundle.getMessage(JavaFXCompletionProvider.class, "scanning-in-progress")); // NOI18N
                         f.get();
                     }
                     if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                         if (results != null) {
                             resultSet.addAllItems(results);
                         }
                         resultSet.setHasAdditionalItems(hasAdditionalItems);
                         if (hasAdditionalItems) {
                             resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JavaFXCompletionProvider.class, "JCP-imported-items")); // NOI18N
                         }
                     } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                         if (toolTip != null) {
                             resultSet.setToolTip(toolTip);
                         }
                     } else if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE) {
                         if (documentation != null) {
                             resultSet.setDocumentation(documentation);
                         }
                     }
                     if (anchorOffset > -1) {
                         resultSet.setAnchorOffset(anchorOffset);
                     }
                 }
             }
         } catch (Exception e) {
             Exceptions.printStackTrace(e);
         } finally {
             resultSet.finish();
         }
     }
 
     @Override
     protected boolean canFilter(JTextComponent component) {
         filterPrefix = null;
         int newOffset = component.getSelectionStart();
         if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
             int offset = Math.min(anchorOffset, caretOffset);
             if (offset > -1) {
                 if (newOffset < offset) {
                     return true;
                 }
                 if (newOffset >= caretOffset) {
                     try {
                         String prefix = component.getDocument().getText(offset, newOffset - offset);
                         filterPrefix = isJavaIdentifierPart(prefix) ? prefix : null;
                         if (filterPrefix != null && filterPrefix.length() == 0) {
                             anchorOffset = newOffset;
                         }
                     } catch (BadLocationException e) {
                     }
                     return true;
                 }
             }
             return false;
         } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
             try {
                 if (newOffset == caretOffset) {
                     filterPrefix = EMPTY;
                 } else if (newOffset - caretOffset > 0) {
                     filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
                 } else if (newOffset - caretOffset < 0) {
                     filterPrefix = newOffset > toolTipOffset ? component.getDocument().getText(newOffset, caretOffset - newOffset) : null;
                 }
             } catch (BadLocationException ex) {
             }
             return filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1; // NOI18N
         }
         return false;
     }
 
     @Override
     protected void filter(CompletionResultSet resultSet) {
         try {
             if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                 if (results != null) {
                     if (filterPrefix != null) {
                         resultSet.addAllItems(getFilteredData(results, filterPrefix));
                         resultSet.setHasAdditionalItems(hasAdditionalItems);
                     } else {
                         Completion.get().hideDocumentation();
                         Completion.get().hideCompletion();
                     }
                 }
             } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                 resultSet.setToolTip(toolTip);
             }
             resultSet.setAnchorOffset(anchorOffset);
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
         resultSet.finish();
     }
 
     public void run(CompilationController controller) throws Exception {
         if (!hasTask || !isTaskCancelled()) {
             if ((queryType & JavaFXCompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                 if (component != null) {
                     component.putClientProperty("completion-active", Boolean.TRUE); // NOI18N
                 }
                 resolveCompletion(controller);
                 if (component != null && isTaskCancelled()) {
                     component.putClientProperty("completion-active", Boolean.FALSE); // NOI18N
                 }
             } else if (queryType == JavaFXCompletionProvider.TOOLTIP_QUERY_TYPE) {
                 JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset,true);
                 env.resolveToolTip(controller);
             } else if (queryType == JavaFXCompletionProvider.DOCUMENTATION_QUERY_TYPE) {
                 resolveDocumentation(controller);
             }
         }
     }
 
     private void resolveDocumentation(CompilationController controller) throws IOException {
         Phase toPhase = null;
         try {
             toPhase  = controller.toPhase(Phase.ANALYZED);
         } catch (Exception ex) {
         }
         if (toPhase != null && toPhase.lessThan(Phase.ANALYZED)) {
             return;
         }
         Element el = null;
         if (element != null) {
             el = element.resolve(controller);
         } else {
             JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset, true);
             if (env != null) {
                 el = controller.getTrees().getElement(env.getPath());
             }
         }
         if (el != null) {
             documentation = JavaCompletionDoc.create(controller, el);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void resolveCompletion(CompilationController controller) throws IOException {
         JavaFXCompletionEnvironment env = getCompletionEnvironment(controller, caretOffset,true);
         results = new HashSet<JavaFXCompletionItem>();
         if (anchorOffset == -1) {
             anchorOffset = env.getOffset();
         }
 
         // make sure the init method was called
         if (env.query != this) {
             throw new IllegalStateException("init method not called before resolveCompletion"); // NOI18N
         }
 
         Phase resPhase = controller.toPhase(Phase.ANALYZED);
 
         if  ((!resPhase.lessThan(Phase.ANALYZED)) && (! env.isTreeBroken())) {
             Tree leaf = env.getPath().getLeaf();
             env.inside(leaf);
             if (results.isEmpty()) {
                 if (anchorOffset != env.getOffset()) {
                     if (LOGGABLE) log("  let's try without moving back"); // NOI18N
                     env = getCompletionEnvironment(controller, caretOffset,false);
                     if (anchorOffset == -1) {
                         anchorOffset = env.getOffset();
                     }
                     leaf = env.getPath().getLeaf();
                     env.inside(leaf);
                 }
             }
         } else {
             if (LOGGABLE) log("Completion not resolved: phase: " + resPhase); // NOI18N
         }
 
         if (LOGGABLE) log("Results: " + results); // NOI18N
     }
 
     static boolean isJavaIdentifierPart(String text) {
         for (int i = 0; i < text.length(); i++) {
             if (!(Character.isJavaIdentifierPart(text.charAt(i)))) {
                 return false;
             }
         }
         return true;
     }
 
     private Collection<JavaFXCompletionItem> getFilteredData(Collection<JavaFXCompletionItem> data, String prefix) {
         if (prefix.length() == 0) {
             return data;
         }
         List<JavaFXCompletionItem> ret = new ArrayList<JavaFXCompletionItem>();
         boolean camelCase = prefix.length() > 1 && camelCasePattern.matcher(prefix).matches();
         for (Iterator<JavaFXCompletionItem> it = data.iterator(); it.hasNext();) {
             JavaFXCompletionItem itm = it.next();
             if (JavaFXCompletionProvider.startsWith(itm.getInsertPrefix().toString(), prefix)) {
                 ret.add(itm);
             }
         }
         return ret;
     }
 
     private String fullName(Tree tree) {
         switch (tree.getJavaFXKind()) {
             case IDENTIFIER:
                 return ((IdentifierTree) tree).getName().toString();
             case MEMBER_SELECT:
                 String sname = fullName(((MemberSelectTree) tree).getExpression());
                 return sname == null ? null : sname + '.' + ((MemberSelectTree) tree).getIdentifier();
             default:
                 return null;
         }
     }
 
     JavaFXCompletionEnvironment getCompletionEnvironment(CompilationController controller, int offset,boolean allowMovingBack) throws IOException {
         controller.toPhase(Phase.ANALYZED);
         String prefix = null;
         if (offset > 0) {
             TokenSequence<JFXTokenId> ts = controller.getTokenHierarchy().tokenSequence(JFXTokenId.language());
             // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
             if (ts.move(offset) == 0 || !ts.moveNext()) {
                 ts.movePrevious();
             }
             int len = offset - ts.offset();
             if (LOGGABLE) log("getCompletionEnvironment len = " + len); // NOI18N
             if (len > 0 &&
                         (ts.token().id() == JFXTokenId.IDENTIFIER) &&
                     ts.token().length() >= len) {
                 //TODO: Use isKeyword(...) when available
                 prefix = ts.token().toString().substring(0, len);
                 offset = ts.offset();
             } else if (allowMovingBack) {
                 boolean moved = false;
                 while (ts.token().id() == JFXTokenId.WS) {
                     if (LOGGABLE) log("     moving back " + ts.token().id()); // NOI18N
                     if (ts.movePrevious()) {
                         moved = true;
                     } else {
                         break;
                     }
                 }
                 if (moved) {
                     // the last one was not WS, lets move past it.
                     ts.moveNext();
                     // leave the anchor at the caret position
                     anchorOffset = offset;
                     // but search at the end of possibly broken tree
                     offset = ts.offset();
                 }
             }
         }
         if (LOGGABLE) log("getCompletionEnvironment caretOffset: " + caretOffset +  // NOI18N
                 " offset: " + offset + " prefix " + prefix); // NOI18N
         JavaFXTreePath path = pathFor(controller, offset);
         Tree t = path.getLeaf();
         if (LOGGABLE) log("   pathFor returned " + t + // NOI18N
                 "\n   class=" + ((t != null) ? t.getClass() : "<NULL>") + '\n'); // NOI18N
         JavaFXTreePath pathOfBrother = pathFor(controller, offset > 0 ? offset - 1 : offset);
         Tree brother = pathOfBrother.getLeaf();
         if (LOGGABLE) log("   brother == " + brother); // NOI18N
         if (!t.equals(brother) && isBrokenAtTheEnd(controller, brother, offset)) {
             if ((brother.getJavaFXKind() == JavaFXKind.OBJECT_LITERAL_PART) ||
                 (brother.getJavaFXKind() == JavaFXKind.MEMBER_SELECT) ||
                 (brother instanceof JFXErroneous) || (brother instanceof JFXTypeUnknown) ||
                 (brother.getJavaFXKind() == JavaFXKind.VARIABLE))
             {
                 path = pathOfBrother;
             } else {
                 path = pathOfBrother.getParentPath();
             }
             t = path.getLeaf();
             if (LOGGABLE) log("   brother gave us " + t); // NOI18N
         } else {
             SourcePositions pos = controller.getTrees().getSourcePositions();
             UnitTree unit = controller.getCompilationUnit();
             long s = pos.getStartPosition(unit, t);
             long e = pos.getEndPosition(unit, t);
             while (t != null &&
                     ( (offset <= s) || (offset >= e) ||
                       (t.getJavaFXKind() == JavaFXKind.ERRONEOUS)
                     )
                   ) {
                 path = path.getParentPath();
                 if (path != null) {
                     t = path.getLeaf();
                     if (LOGGABLE) log("    updating t == " + t); // NOI18N
                 } else {
                     t = null;
                 }
                 long s1 = pos.getStartPosition(unit, t);
                 s = s1 < s ? s1 : s;
                 long e1 = pos.getEndPosition(unit, t);
                 e = e1 > e ? e1 : e;
                 if (LOGGABLE) log("   s ==" + s + " s1 ==" + s1 + "   e ==" + e + "   e1 == " + e1); // NOI18N
                 if ((t != null) &&
                         (t.getJavaFXKind() == JavaFXKind.CLASS_DECLARATION) &&
                         s1 == e1) {
                     // strange class declaration --> continue to the parent
                     s = s1;
                     e = e1;
                 }
             }
             if ((t == null) || (controller.getTreeUtilities().isSynthetic(path))){
                 t = unit;
                 path = new JavaFXTreePath(unit);
             }
         }
         JavaFXCompletionEnvironment result = null;
         JavaFXKind k = t.getJavaFXKind();
         result = createEnvironment(k);
         result.init(offset, prefix, controller, path, controller.getTrees().getSourcePositions(), this);
         return result;
     }
 
     /**
      *
      * @param t
      * @return
      */
     private boolean isBrokenAtTheEnd(CompilationController controller, Tree t, int offset) {
         if (LOGGABLE) log("isBrokenAtTheEnd " + t + " class == " + t.getClass()); // NOI18N
         if (t instanceof JFXErroneousType || t instanceof JFXErroneous) {
             SourcePositions pos = controller.getTrees().getSourcePositions();
             UnitTree unit = controller.getCompilationUnit();
             long s = pos.getStartPosition(unit, t);
             long e = pos.getEndPosition(unit, t);
             if (LOGGABLE) log("   (1) s == " + s + "  e == " + e + "  offset == " + offset); // NOI18N
             return e == offset;
         }
         if ((t instanceof JFXObjectLiteralPart) ||
             (t instanceof JFXVar) ||
             (t instanceof JFXFunctionValue) ) {
             SourcePositions pos = controller.getTrees().getSourcePositions();
             UnitTree unit = controller.getCompilationUnit();
             long s = pos.getStartPosition(unit, t);
             long e = pos.getEndPosition(unit, t);
             if (t.toString().contains("/*missing*/")) { // ho ho hoooo // NOI18N
                 if (LOGGABLE) log("   (2) s == " + s + "  e == " + e + "  offset == " + offset); // NOI18N
                 return e == offset;
             }
             TokenSequence<JFXTokenId> ts = controller.getTokenHierarchy().tokenSequence(JFXTokenId.language());
             ts.move((int)e);
             ts = JavaFXCompletionEnvironment.previousNonWhitespaceToken(ts);
             if (ts == null || ts.offset() < s) {
                 throw new IllegalStateException("??? No non-whitespace tokens inside " + t);
             }
             if (ts.token().id() == JFXTokenId.COLON) {
                 return true;
             }
         }
         if (t instanceof JFXSelect) {
             if (t.toString().contains("<missing>")) { // ho ho hoooo // NOI18N
                 SourcePositions pos = controller.getTrees().getSourcePositions();
                 UnitTree unit = controller.getCompilationUnit();
                 long s = pos.getStartPosition(unit, t);
                 long e = pos.getEndPosition(unit, t);
                 if (LOGGABLE) log("   (3) s == " + s + "  e == " + e + "  offset == " + offset); // NOI18N
                 return e == offset;
             }
         }
         return false;
     }
 
     static JavaFXCompletionEnvironment createEnvironment(JavaFXKind k) {
         JavaFXCompletionEnvironment result = null;
         if (LOGGABLE) log("JavaFXKind: " + k); // NOI18N
         switch (k) {
             case COMPILATION_UNIT:
                 result = new CompilationUnitEnvironment();
                 break;
             case IMPORT:
                 result = new ImportTreeEnvironment();
                 break;
             case VARIABLE:
                 result = new VariableTreeEnvironment();
                 break;
             case MODIFIERS:
                 result = new ModifiersTreeEnvironment();
                 break;
             case MEMBER_SELECT:
                 result = new MemberSelectTreeEnvironment();
                 break;
             case METHOD_INVOCATION:
                 result = new MethodInvocationTreeEnvironment();
                 break;
             case INSTANTIATE_NEW:
                 result = new InstantiateNewEnvironment();
                 break;
             case RETURN:
                 result = new ReturnEnvironment();
                 break;
             case THROW:
                 result = new ThrowEnvironment();
                 break;
             case CATCH:
                 result = new CatchEnvironment();
                 break;
             case WHILE_LOOP:
                 result = new WhileLoopTreeEnvironment();
                 break;
             case PARENTHESIZED:
                 result = new ParenthesizedTreeEnvironment();
                 break;
             case TYPE_CAST:
                 result = new TypeCastEnvironment();
                 break;
             case INSTANCE_OF:
                 result = new InstanceOfTreeEnvironment();
                 break;
             case ASSIGNMENT:
                 result = new AssignmentTreeEnvironment();
                 break;
             case MULTIPLY_ASSIGNMENT:
             case DIVIDE_ASSIGNMENT:
             case PLUS_ASSIGNMENT:
             case MINUS_ASSIGNMENT:
                 result = new CompoundAssignmentTreeEnvironment();
                 break;
             case PREFIX_INCREMENT:
             case POSTFIX_INCREMENT:
             case PREFIX_DECREMENT:
             case POSTFIX_DECREMENT:
             case UNARY_MINUS:
             case LOGICAL_COMPLEMENT:
            case SIZEOF:
            case REVERSE:
                 result = new UnaryTreeEnvironment();
                 break;
             case CONDITIONAL_AND:
             case CONDITIONAL_OR:
             case DIVIDE:
             case EQUAL_TO:
             case GREATER_THAN:
             case GREATER_THAN_EQUAL:
             case LESS_THAN:
             case LESS_THAN_EQUAL:
             case MINUS:
             case MULTIPLY:
             case NOT_EQUAL_TO:
             case PLUS:
             case REMAINDER:
                 result = new BinaryTreeEnvironment();
                 break;
             case CONDITIONAL_EXPRESSION:
                 result = new ConditionalExpressionEnvironment();
                 break;
             case BLOCK_EXPRESSION:
                 result = new BlockExpressionEnvironment();
                 break;
             case CLASS_DECLARATION:
                 result = new ClassDeclarationEnvironment();
                 break;
             case FOR_EXPRESSION_FOR:
                 result = new ForExpressionEnvironment();
                 break;
             case FOR_EXPRESSION_PREDICATE:
                 result = new ForExpressionPredicateEnvironment();
                 break;
             case FOR_EXPRESSION_IN_CLAUSE:
                 result = new ForExpressionInClauseEnvironment();
                 break;
             case FUNCTION_DEFINITION:
                 result = new FunctionDefinitionEnvironment();
                 break;
             case FUNCTION_VALUE:
                 result = new FunctionValueEnvironment();
                 break;
             case INIT_DEFINITION:
                 break;
             case INSTANTIATE_OBJECT_LITERAL:
                 result = new InstantiateEnvironment();
                 break;
             case INTERPOLATE_VALUE:
                 break;
             case KEYFRAME_LITERAL:
                 break;
             case OBJECT_LITERAL_PART:
                 result = new ObjectLiteralPartEnvironment();
                 break;
             case ON_REPLACE:
                 break;
             case POSTINIT_DEFINITION:
                 break;
             case SEQUENCE_DELETE:
                 break;
             case SEQUENCE_EMPTY:
                 result = new SequenceEmptyEnvironment();
                 break;
             case SEQUENCE_EXPLICIT:
                 result = new SequenceExplicitEnvironment();
                 break;
             case SEQUENCE_INDEXED:
                 result = new SequenceIndexedEnvironment();
                 break;
             case SEQUENCE_INSERT:
                 break;
             case SEQUENCE_RANGE:
                 result = new SequenceRangeEnvironment();
                 break;
             case SEQUENCE_SLICE:
                 break;
             case STRING_EXPRESSION:
                 result = new StringExpressionEnvironment();
                 break;
             case TIME_LITERAL:
                 break;
             case TRIGGER_WRAPPER:
                 break;
             case TYPE_ANY:
                 break;
             case TYPE_CLASS:
                 break;
             case TYPE_FUNCTIONAL:
                 break;
             case TYPE_UNKNOWN:
                 break;
             case BREAK:
                 break;
             case CONTINUE:
                 break;
             case IDENTIFIER:
                 break;
             case EMPTY_STATEMENT:
                 break;
             case TRY:
                 break;
             case INT_LITERAL:
             case FLOAT_LITERAL:
             case DOUBLE_LITERAL:
             case BOOLEAN_LITERAL:
             case STRING_LITERAL:
             case NULL_LITERAL:
                 break;
             case ERRONEOUS:
                 result = new ErroneousEnvironment();
                 break;
             case INDEXOF:
                 break;
             case OTHER:
                 break;
         }
 
         if (result == null) {
             result = new JavaFXCompletionEnvironment();
         }
 
         return result;
     }
     public static JavaFXTreePath pathFor(CompilationController info, int pos) {
         return pathFor(info, new JavaFXTreePath(info.getCompilationUnit()), pos);
     }
 
     /*XXX: dbalek
      */
     public static JavaFXTreePath pathFor(CompilationController info, JavaFXTreePath path, int pos) {
         return pathFor(info, path, pos, info.getTrees().getSourcePositions());
     }
 
     /*XXX: dbalek
      */
     public static JavaFXTreePath pathFor(final CompilationController info, JavaFXTreePath path, int pos, SourcePositions sourcePositions) {
         if (info == null || path == null || sourcePositions == null)
             throw new IllegalArgumentException();
 
         class Result extends Error {
             JavaFXTreePath path;
             Result(JavaFXTreePath path) {
                 this.path = path;
             }
         }
 
         class PathFinder extends JavaFXTreePathScanner<Void,Void> {
             private int pos;
             private SourcePositions sourcePositions;
 
             private PathFinder(int pos, SourcePositions sourcePositions) {
                 this.pos = pos;
                 this.sourcePositions = sourcePositions;
             }
 
             @Override
             public Void scan(Tree tree, Void p) {
                 if (tree != null) {
                     super.scan(tree, p);
                     long start = sourcePositions.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                     long end = sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                     if (start != -1 && start <= pos && end > pos) {
                         JavaFXTreePath tp = new JavaFXTreePath(getCurrentPath(), tree);
                         boolean isSynteticMainBlock = info.getTreeUtilities().isSynthetic(tp);
                         // we don't want to return the syntetic main block as the result
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION) {
                             JavaFXTreePath parentPath = tp.getParentPath();
                             if (parentPath != null) {
                                 JavaFXTreePath grandParentPath = parentPath.getParentPath();
                                 if (grandParentPath != null) {
                                     Tree grandParent = grandParentPath.getLeaf();
                                     if (grandParent.getJavaFXKind() ==
                                             Tree.JavaFXKind.FUNCTION_DEFINITION &&
                                             info.getTreeUtilities().isSynthetic(grandParentPath)) {
                                         isSynteticMainBlock = true;
                                     }
                                 }
                             }
                         }
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.FUNCTION_VALUE) {
                             JavaFXTreePath parentPath = tp.getParentPath();
                             if (parentPath != null) {
                                 Tree parent = parentPath.getLeaf();
                                 if (parent.getJavaFXKind() ==
                                         Tree.JavaFXKind.FUNCTION_DEFINITION &&
                                         info.getTreeUtilities().isSynthetic(parentPath)) {
                                     isSynteticMainBlock = true;
                                 }
                             }
                         }
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.ERRONEOUS) {
                             JavaFXTreePath parentPath = tp.getParentPath();
                             if (parentPath != null &&
                                 parentPath.getLeaf().getJavaFXKind() == Tree.JavaFXKind.CLASS_DECLARATION
                                 || parentPath.getLeaf().getJavaFXKind() == Tree.JavaFXKind.METHOD_INVOCATION)
                             {
                                 // happens for unclosed class decl: "class A { |"
                             } else {
                                 return null;
                             }
                         }
                         if (tree.getJavaFXKind() == Tree.JavaFXKind.TYPE_UNKNOWN) {
                             return null;
                         }
                         if (!isSynteticMainBlock) {
                             throw new Result(new JavaFXTreePath(getCurrentPath(), tree));
                         }
                     } else {
                         if ((start == -1) || (end == -1)) {
                             if (!info.getTreeUtilities().isSynthetic(getCurrentPath())) {
                                 // here we might have a problem
                                 if (LOGGABLE) {
                                     logger.finest("SCAN: Cannot determine start and end for: " + treeToString(info, tree)); // NOI18N
                                 }
                             }
                         }
                     }
                 }
                 return null;
             }
         }
 
         try {
             new PathFinder(pos, sourcePositions).scan(path, null);
         } catch (Result result) {
             path = result.path;
         }
 
         if (path.getLeaf() == path.getCompilationUnit()) {
             log("pathFor returning compilation unit for position: " + pos); // NOI18N
             return path;
         }
         int start = (int)sourcePositions.getStartPosition(info.getCompilationUnit(), path.getLeaf());
         int end   = (int)sourcePositions.getEndPosition(info.getCompilationUnit(), path.getLeaf());
         while (start == -1 || pos < start || pos > end) {
             if (LOGGABLE) {
                 logger.finer("pathFor moving to parent: " + treeToString(info, path.getLeaf())); // NOI18N
             }
             path = path.getParentPath();
             if (LOGGABLE) {
                 logger.finer("pathFor moved to parent: " + treeToString(info, path.getLeaf())); // NOI18N
             }
             if (path.getLeaf() == path.getCompilationUnit()) {
                 break;
             }
             start = (int)sourcePositions.getStartPosition(info.getCompilationUnit(), path.getLeaf());
             end   = (int)sourcePositions.getEndPosition(info.getCompilationUnit(), path.getLeaf());
         }
         if (LOGGABLE) {
             log("pathFor(pos: " + pos + ") returning: " + treeToString(info, path.getLeaf())); // NOI18N
         }
         return path;
     }
     private static String treeToString(CompilationInfo info, Tree t) {
         Tree.JavaFXKind k = null;
         StringWriter s = new StringWriter();
         try {
             new JavafxPretty(s, false).printExpr((JFXTree)t);
         } catch (Exception e) {
             if (LOGGABLE) logger.log(Level.FINE, "Unable to pretty print " + t.getJavaFXKind(), e); // NOI18N
         }
         k = t.getJavaFXKind();
         String res = k.toString();
         SourcePositions pos = info.getTrees().getSourcePositions();
         res = res + '[' + pos.getStartPosition(info.getCompilationUnit(), t) + ',' + // NOI18N
                 pos.getEndPosition(info.getCompilationUnit(), t) + "]:" + s.toString(); // NOI18N
         return res;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
