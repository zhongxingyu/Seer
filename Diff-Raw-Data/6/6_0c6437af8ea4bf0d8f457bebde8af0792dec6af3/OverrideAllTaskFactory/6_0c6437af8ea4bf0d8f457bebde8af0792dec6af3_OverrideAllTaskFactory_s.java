 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.javafx.api.tree.ImportTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.util.JCDiagnostic;
 import com.sun.tools.javafx.code.JavafxClassSymbol;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.javafx.tree.JFXImport;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.lang.model.element.*;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class OverrideAllTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String EXCEPTION = "java.lang.UnsupportedOperationException"; //NOI18N
     private static final String TAB = "    "; //NOI18N
     private static final String ERROR_CODE1 = "compiler.err.does.not.override.abstract"; //NOI18N
     private static final String ERROR_CODE2 = "compiler.err.abstract.cant.be.instantiated"; //NOI18N
     private static final String HINT_IDENT = "overridejavafx"; //NOI18N
     private final AtomicBoolean cancel = new AtomicBoolean();
 
     public OverrideAllTaskFactory() {
         super(JavaFXSource.Phase.ANALYZED, JavaFXSource.Priority.NORMAL);
     }
 
     @Override
     public CancellableTask<CompilationInfo> createTask(final FileObject file) {
 
         return new CancellableTask<CompilationInfo>() {
 
             public void cancel() {
                 cancel.set(true);
             }
 
             public void run(final CompilationInfo compilationInfo) throws Exception {
                 cancel.set(false);
                 final Collection<ErrorDescription> errorDescriptions = new HashSet<ErrorDescription>();
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                     if (!isValidError(diagnostic, compilationInfo) || cancel.get()) {
                         continue;
                     }
                     int position = findPosition(compilationInfo, (int) diagnostic.getStartPosition(), (int) (diagnostic.getEndPosition() - diagnostic.getStartPosition()));
                     if (!(diagnostic instanceof JCDiagnostic) || position < 0) {
                         continue;
                     }
                     ((JCDiagnostic) diagnostic).getArgs();
                     errorDescriptions.add(getErrorDescription(compilationInfo.getDocument(), file, compilationInfo, diagnostic));
                 }
                 HintsController.setErrors(compilationInfo.getDocument(), HINT_IDENT, errorDescriptions);
             }
         };
     }
 
     private boolean isValidError(Diagnostic diagnostic, CompilationInfo compilationInfo) {
         if (diagnostic.getCode().equals(ERROR_CODE1) || diagnostic.getCode().equals(ERROR_CODE2)) {
             for (Diagnostic d : compilationInfo.getDiagnostics()) {
                 if (d != diagnostic && d.getLineNumber() == diagnostic.getLineNumber()) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     private int findPosition(CompilationInfo compilationInfo, int start, int lenght) {
         Document document = compilationInfo.getDocument();
         try {
             String text = document.getText(start, lenght);
             StringTokenizer st = new StringTokenizer(text);
             boolean isExtends = false;
             boolean isClass = false;
 
             while (st.hasMoreTokens()) {
                 String token = st.nextToken();
                 if (token.equals("extends")) { //NOI18N
                     isExtends = true;
                 }
                 if (token.equals("class")) { //NOI18N
                     isClass = true;
                 }
             }
             if (isClass && !isExtends) {
                 return -1;
             }
             int index = text.indexOf("{"); //NOI18N
             if (index > 0) {
                 return start + index + 1;
             } else if (text.contains("(") || text.contains(")")) { //NOI18N
                 return -1;
             } else {
                 text = document.getText(start, document.getLength() - start);
                 index = text.indexOf("{"); //NOI18N
                 if (index < 0) {
                     return -1;
                 }
                 return start + index + 1;
             }
         } catch (BadLocationException ex) {
             ex.printStackTrace();
         }
 
         return -1;
     }
 
     private ErrorDescription getErrorDescription(final Document document,
             FileObject file,
             final CompilationInfo compilationInfo,
             final Diagnostic diagnostic) {
 
         Fix fix = new Fix() {
 
             public String getText() {
                 return NbBundle.getMessage(OverrideAllTaskFactory.class, "TITLE_IMPLEMENT_ABSTRACT"); //NOI18N
             }
 
             public ChangeInfo implement() throws Exception {
                 JCDiagnostic jcDiagnostic = (JCDiagnostic) diagnostic;
                 ClassSymbol classSymbol = null;
                 for (Object arg : jcDiagnostic.getArgs()) {
                     if (arg instanceof JavafxClassSymbol) {
                         classSymbol = (JavafxClassSymbol) arg;
                         break;
                     }
                 }
                 final Collection<MethodSymbol> abstractMethods = new HashSet<MethodSymbol>();
                 if (classSymbol != null) {
                     final Collection<JFXImport> imports = new HashSet<JFXImport>();
                     JavaFXTreePathScanner<Void, Void> visitor = new JavaFXTreePathScanner<Void, Void>() {
 
                         @Override
                         public Void visitImport(ImportTree node, Void p) {
                             if (node instanceof JFXImport) {
                                 imports.add((JFXImport) node);
                             }
 
                             return super.visitImport(node, p);
                         }
                     };
                     visitor.scan(compilationInfo.getCompilationUnit(), null);
                     Collection<? extends Element> elements = getAllMembers(classSymbol, compilationInfo);
                     for (Element e : elements) {
                         if (e instanceof MethodSymbol) {
                             MethodSymbol method = (MethodSymbol) e;
                             for (Modifier modifier : method.getModifiers()) {
                                 if (modifier == Modifier.ABSTRACT) {
                                     abstractMethods.add(method);
                                     break;
                                 }
                             }
                         }
                     }
                     if (abstractMethods.isEmpty()) {
                         return null;
                     }
                 }
                 final StringBuilder methods = new StringBuilder();
                 final String space = HintsUtils.calculateSpace((int) diagnostic.getStartPosition(), document);
                 for (MethodSymbol methodSymbol : abstractMethods) {
                     methods.append(createMethod(methodSymbol, space));
                 }
                 if (methods.toString().length() > 0) {
                     methods.append(space);
                 }
                 int length = (int) (diagnostic.getEndPosition() - diagnostic.getStartPosition());
                 final int positon = findPosition(compilationInfo, (int) diagnostic.getStartPosition(), length);
                 if (positon < 0) {
                     return null;
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         try {
                             document.insertString(positon, methods.toString(), null);
                             JTextComponent target = HintsUtils.getEditorComponent(document);
                             if (target == null) {
                                 return;
                             }
                             Imports.addImport(target, EXCEPTION);
                             for (MethodSymbol method : abstractMethods) {
                                 addImport(target, method.asType());
                                 for (VarSymbol var : method.getParameters()) {
                                     scanImport(target, var.asType());
                                 }
                             }
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 });
 
                 return null;
             }
 
             private void scanImport(JTextComponent target, Type type) {
                 if (type.getParameterTypes() != null && !type.getParameterTypes().isEmpty()) {
                     for (Type t : type.getParameterTypes()) {
                         scanImport(target, t);
                     }
                 }
                 addImport(target, type);
             }
 
             private void addImport(JTextComponent target, Type type) {
                 if (type == null) {
                     return;
                 }
                 String importName = type.toString();
                 if (!type.isPrimitive() && !importName.equalsIgnoreCase("void") && importName.contains(".")) { //NOI18N
                     importName = removeBetween("()", importName); //NOI18N
                     importName = removeBetween("<>", importName); //NOI18N
                     Matcher symbolMatcher = Pattern.compile("[\\[\\]!@#$%^&*(){}|:'/<>~`,]").matcher(importName); //NOI18N
                     if (symbolMatcher.find()) {
                         importName = symbolMatcher.replaceAll("").trim(); //NOI18N
                     }
                     if (importName.contains(".") && !importName.equals("java.lang.Object")) { //NOI18N
                         Imports.addImport(target, importName);
                     }
                 }
             }
 
             private String createMethod(MethodSymbol methodSymbol, String space) {
                 StringBuilder method = new StringBuilder();
                 method.append("\n").append(space).append(TAB).append("override "); //NOI18N
                 for (Modifier modifier : methodSymbol.getModifiers()) {
                     switch (modifier) {
                         case PUBLIC:
                             method.append("public "); //NOI18N
                             break;
                         case PROTECTED:
                             method.append("protected "); //NOI18N
                             break;
                     }
                 }
                 method.append("function ").append(methodSymbol.getQualifiedName() + " ("); //NOI18N
                 //TODO Work around for NPE which is thrown by methodSymbol.getParameters();
                 try {
                     if (methodSymbol.getParameters() != null) {
                         Iterator<VarSymbol> iterator = methodSymbol.getParameters().iterator();
                         while (iterator.hasNext()) {
                             VarSymbol var = iterator.next();
                             String varType = getTypeString(var.asType(), compilationInfo.getJavafxTypes());
                             method.append(var.getSimpleName()).append(" : ").append(HintsUtils.getClassSimpleName(varType)); //NOI18N
                             if (iterator.hasNext()) {
                                 method.append(", "); //NOI18N
                             }
                         }
                     }
                 } catch (NullPointerException npe) {
                     npe.printStackTrace();
                 }
                 //TODO Work around for methodSymbol.getReturnType() which throws NPE!
                 String returnType = null;
                 try {
                     returnType = getTypeString(methodSymbol.getReturnType(), compilationInfo.getJavafxTypes());
                 } catch (NullPointerException npe) {
                     npe.printStackTrace();
                 }
                 if (returnType == null) {
                     returnType = "Void"; //NOI18N
                 } else {
                     returnType = HintsUtils.getClassSimpleName(returnType);
                 }
                 method.append(")").append(" : ").append(returnType).append(" { \n"); //NOI18N
                 method.append(space).append(TAB).append(TAB).append("throw new UnsupportedOperationException('Not implemented yet');\n"); //NOI18N
                 method.append(space).append(TAB).append("}\n"); //NOI18N
 
                 return method.toString();
             }
         };
         if (diagnostic.getStartPosition() < 0) {
             return null;
         }
         ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, NbBundle.getMessage(OverrideAllTaskFactory.class, "TITLE_IMPLEMENT_ABSTRACT"), Collections.singletonList(fix), file, (int) diagnostic.getStartPosition(), (int) diagnostic.getStartPosition());
 
         return ed;
     }
 
     private String removeBetween(String symbols, String name) {
         int firstIndex = name.indexOf(symbols.substring(0, 1));
         int lastIndex = name.indexOf(symbols.substring(1, 2));
         if (firstIndex < 0 || lastIndex < 0) {
             return name;
         }
         name = name.replace(name.substring(firstIndex, lastIndex + 1), "");
 
         return name;
     }
 
     private String getTypeString(Type type, JavafxTypes types) {
        String typeString =  types.toJavaFXString(type);
         if (typeString == null) {
             typeString = ""; //NOI18N
        } else if (typeString.contains("<")) { //NOI18N
            typeString = typeString.substring(0, typeString.indexOf("<")); //NOI18N //Removing generics
         } else if (typeString != null && typeString.equals("void")) { //NOI18N
             typeString = "Void"; //NOI18N
         }
         
         return typeString;
     }
 
 
     //TODO Temporary log for issue 148890
     Collection<? extends Element> getAllMembers(TypeElement typeElement, CompilationInfo compilationInfo) {
         Collection<? extends Element> elements = null;
         try {
             elements = FXSourceUtils.getAllMembers(compilationInfo.getElements(), typeElement);
         } catch (NullPointerException npe) {
             npe.printStackTrace();
         }
 
         return elements;
     }
 }
 
 
 
 
