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
 
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.support.EditorAwareJavaFXSourceTaskFactory;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import com.sun.tools.javafx.code.JavafxClassSymbol;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.mjavac.code.Symbol.ClassSymbol;
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import com.sun.tools.mjavac.code.Symbol.VarSymbol;
 import com.sun.tools.mjavac.code.Type;
 import com.sun.tools.mjavac.util.JCDiagnostic;
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
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.Imports;
 import org.netbeans.modules.javafx.editor.JavaFXDocument;
 import org.netbeans.spi.editor.hints.*;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author karol harezlak
  */
 public final class OverrideAllTaskFactory extends EditorAwareJavaFXSourceTaskFactory {
 
     private static final String ERROR_CODE1 = "compiler.err.does.not.override.abstract"; //NOI18N
     private static final String ERROR_CODE2 = "compiler.err.abstract.cant.be.instantiated"; //NOI18N
     private static final String HINT_IDENT = "overridejavafx"; //NOI18N
     private final AtomicBoolean cancel = new AtomicBoolean();
     private static final EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
 
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
                 if (!(compilationInfo.getDocument() instanceof JavaFXDocument)) {
                     return;
                 }
                 JavaFXDocument document = (JavaFXDocument) compilationInfo.getDocument();
                 final Collection<ErrorDescription> errorDescriptions = new HashSet<ErrorDescription>();
                 for (final Diagnostic diagnostic : compilationInfo.getDiagnostics()) {
                     if (!isValidError(diagnostic, compilationInfo) || cancel.get() || document.isPosGuarded((int) diagnostic.getPosition())) {
                         continue;
                     }
                     int position = findPosition(compilationInfo, (int) diagnostic.getStartPosition(), (int) (diagnostic.getEndPosition() - diagnostic.getStartPosition()));
                     if (!(diagnostic instanceof JCDiagnostic) || position < 0) {
                         continue;
                     }
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
             final FileObject file,
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
                     Collection<? extends Element> elements = getAllMembers(compilationInfo, classSymbol);
                     for (Element e : elements) {
                         if (e instanceof MethodSymbol) {
                             MethodSymbol method = (MethodSymbol) e;
                             for (Modifier modifier : method.getModifiers()) {
                                 if (modifier == Modifier.ABSTRACT) {
                                     boolean methodExists = false;
                                     for (Element exisitngMethod : classSymbol.getEnclosedElements()) {
                                         if (!(exisitngMethod instanceof ExecutableElement) && !method.getSimpleName().toString().equals(exisitngMethod.getSimpleName().toString())) {
                                             continue;
                                         }
                                         if (compilationInfo.getElements().overrides((ExecutableElement) exisitngMethod, (ExecutableElement) method, classSymbol)) {
                                             methodExists = true;
                                         }
                                     }
                                     if (!methodExists) {
                                         abstractMethods.add(method);
                                     }
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
                             Imports.addImport(target, HintsUtils.EXCEPTION_UOE);
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
                 method.append("\n").append(space).append(HintsUtils.TAB).append("override "); //NOI18N
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
                 method.append(space).append(HintsUtils.TAB).append(HintsUtils.TAB).append("throw new UnsupportedOperationException('Not implemented yet');\n"); //NOI18N
                 method.append(space).append(HintsUtils.TAB).append("}\n"); //NOI18N
 
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
        Type sureType;
        if (types.isSequence(type)) {
            sureType = type;
        } else {
            sureType = types.erasure(type);
        }
         String typeString = types.toJavaFXString(sureType);
         if (typeString == null) {
             typeString = ""; //NOI18N
         } else if (typeString != null && typeString.equals("void")) { //NOI18N
             typeString = "Void"; //NOI18N
         }
 
         return typeString;
     }
 
     private Collection<? extends Element> getAllMembers(CompilationInfo compilationInfo, Element element) {
 //        JavaFXTreePath path = compilationInfo.getTreeUtilities().pathFor(position);
 //        Element element = compilationInfo.getTrees().getElement(path);
 //        ClassSymbol classSymbol = null;
 //        if (element instanceof ClassSymbol) {
 //            classSymbol = (ClassSymbol) element;
 //        }
 //        if (classSymbol == null) {
 //            return Collections.EMPTY_SET;
 //        }
 //        ClassIndex classIndex = compilationInfo.getClasspathInfo().getClassIndex();
 //        Collection<ElementHandle<TypeElement>> options = classIndex.getDeclaredTypes(classSymbol.getSimpleName().toString(), ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
 //        Collection<? extends Element> elements = Collections.EMPTY_SET;
 //        for (ElementHandle<TypeElement> eh : options) {
 //            if (!eh.getQualifiedName().toString().equals(classSymbol.getQualifiedName().toString())) {
 //                continue;
 //            }
 //            TypeElement el = eh.resolve(compilationInfo);
 //            elements = compilationInfo.getElements().getAllMembers(el);
 //        }
 
         
 
         return FXSourceUtils.getAllMembers(compilationInfo.getElements(), (TypeElement) element);
     }
 }
 
 
 
 
