 /*
  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  *  Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
  * 
  *  The contents of this file are subject to the terms of either the GNU
  *  General Public License Version 2 only ("GPL") or the Common
  *  Development and Distribution License("CDDL") (collectively, the
  *  "License"). You may not use this file except in compliance with the
  *  License. You can obtain a copy of the License at
  *  http://www.netbeans.org/cddl-gplv2.html
  *  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  *  specific language governing permissions and limitations under the
  *  License.  When distributing the software, include this License Header
  *  Notice in each file and include the License file at
  *  nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  *  particular file as subject to the "Classpath" exception as provided
  *  by Sun in the GPL Version 2 section of the License file that
  *  accompanied this code. If applicable, add the following below the
  *  License Header, with the fields enclosed by brackets [] replaced by
  *  your own identifying information:
  *  "Portions Copyrighted [year] [name of copyright owner]"
  * 
  *  Contributor(s):
  * 
  *  Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl.plugins;
 
 import org.netbeans.modules.javafx.refactoring.impl.scanners.BaseRefactoringScanner;
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.Scope;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.UnitTree;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.NestingKind;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.ElementFilter;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.ClassIndex.SearchKind;
 import org.netbeans.api.javafx.source.ClassIndex.SearchScope;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.ElementUtilities;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.modules.javafx.refactoring.impl.ElementLocation;
 import org.netbeans.modules.javafx.refactoring.impl.RenameRefactoringElement;
 import org.netbeans.modules.javafx.refactoring.impl.TransformationContext;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.scanners.LocalVarScanner;
 import org.netbeans.modules.refactoring.api.Problem;
 import org.netbeans.modules.refactoring.api.RenameRefactoring;
 import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
 import org.openide.filesystems.FileObject;
 import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.Utilities;
 import org.openide.util.lookup.Lookups;
 import org.openide.util.lookup.ProxyLookup;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 public class RenameRefactoringPlugin extends JavaFXRefactoringPlugin {
     private final AtomicBoolean requestCancelled = new AtomicBoolean(false);
 
     private String fileName;
     private RenameRefactoring refactoring;
 
     private boolean doCheckName = true;
 
     private Collection<ExecutableElement> overriddenByMethods = null; // methods that override the method to be renamed
     private Collection<ExecutableElement> overridesMethods = null; // methods that are overridden by the method to be renamed
 
     private ElementLocation location = null;
 
     public RenameRefactoringPlugin(RenameRefactoring rename) {
         this.refactoring = rename;
         ElementLocation loc = rename.getRefactoringSource().lookup(ElementLocation.class);
         if (loc!=null) {
             this.location = loc;
         } else {
             try {
                 getSource().runUserActionTask(new Task<CompilationController>() {
                     public void cancel() {
                     }
 
                     public void run(CompilationController co) throws Exception {
                         UnitTree cut = co.getCompilationUnit();
                         for (Tree t: cut.getTypeDecls()) {
                             JavaFXTreePath path = JavaFXTreePath.getPath(cut, t);
                             Element e = co.getTrees().getElement(path);
                             if (e!=null && e.getSimpleName().toString().equals(co.getFileObject().getName())) {
                                 location = ElementLocation.forPath(path, co);
                                 refactoring.getContext().add(co);
                                 break;
                             }
                         }
                     }
                 }, false);
             } catch (IllegalArgumentException ex) {
                 ex.printStackTrace();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
         if (location != null) {
             fileName = location.getSourceFile().getName();
         }
     }
 
     @Override
     protected JavaFXSource prepareSource() {
         return JavaFXSource.forFileObject(location.getSourceFile());
     }
 
     public void cancelRequest() {
         requestCancelled.set(true);
     }
 
     public Problem checkParameters(final CompilationInfo info) {
         final String newElementName = refactoring.getNewName();
 
         final AtomicReference<Problem> problem = new AtomicReference<Problem>();
 
         switch(location.getElement(info).getKind()) {
             case CLASS: {
                 TypeElement te = (TypeElement) info.getElementUtilities().elementFor(location.getStartPosition());
                 switch(te.getNestingKind()) {
                     case MEMBER: {
                         final TypeElement parent = (TypeElement)te.getEnclosingElement();
                         JavaFXTreePathScanner<Void, Void> scanner = new JavaFXTreePathScanner<Void, Void>() {
                             @Override
                             public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                                 TypeElement current = (TypeElement)info.getTrees().getElement(getCurrentPath());
                                 if (current.getEnclosingElement().equals(parent)) {
                                     if (current.getSimpleName().toString().equals(newElementName)) {
                                         problem.set(new Problem(true, "Can not rename to " + current.getQualifiedName() + " - it already exists."));
                                     }
                                 }
                                 return super.visitClassDeclaration(node, p);
                             }
                         };
                         scanner.scan(info.getCompilationUnit(), null);
                     }
                 }
                 break;
             }
         }
 
         return problem.get();
     }
 
     public Problem fastCheckParameters(CompilationInfo info) {
         Problem fastCheckProblem = null;
         Element element = info.getElementUtilities().elementFor(location.getStartPosition());
         JavaFXTreePath treePath = info.getTrees().getPath(element);
 
         ElementKind kind = element.getKind();
 
         String newName = refactoring.getNewName();
         String oldName = element.getSimpleName().toString();
 
         if (oldName.equals(newName)) {
             boolean nameNotChanged = true;
             if (kind.isClass()) {
                 if (!((TypeElement) element).getNestingKind().isNested()) {
                     nameNotChanged = info.getFileObject().getName().contentEquals(((TypeElement) element).getSimpleName());
                 }
             }
             if (nameNotChanged) {
                 fastCheckProblem = createProblem(fastCheckProblem, true, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_NameNotChanged"));
                 return fastCheckProblem;
             }
 
         }
 
         if (!Utilities.isJavaIdentifier(newName)) {
             String msg = kind == ElementKind.PACKAGE?
                 NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage", new Object[]{newName}) :
                 NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidIdentifier", new Object[]{newName}); //NOI18N
 
             fastCheckProblem = createProblem(fastCheckProblem, true, msg);
             return fastCheckProblem;
         }
 
         if (kind.isClass() && !((TypeElement) element).getNestingKind().isNested()) {
             if (doCheckName) {
                 Set<FileObject> typeDefFOs = getClassIndex().getResources(ElementHandle.create(element), EnumSet.of(SearchKind.TYPE_DEFS), EnumSet.allOf(SearchScope.class));
                 FileObject typeDefFO = typeDefFOs.iterator().next();
 
                 String oldFqn = ElementHandle.create(element).getQualifiedName();
                 int pkgDelimitIndex = oldFqn.lastIndexOf(oldName);
                 String pkgname = oldFqn.substring(0, pkgDelimitIndex > 0 ? pkgDelimitIndex - 1 : 0);
                 String newFqn = pkgname + "." + newName;
 
                 if (!getClassIndex().getDeclaredTypes(newFqn, ClassIndex.NameKind.EXACT, EnumSet.allOf(SearchScope.class)).isEmpty()) {
                     String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_ClassClash", new Object[] {newName, pkgname});
                     fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                     return fastCheckProblem;
                 }
                 FileObject parentFolder = typeDefFO.getParent();
                 Enumeration enumeration = parentFolder.getFolders(false);
                 while (enumeration.hasMoreElements()) {
                     FileObject subfolder = (FileObject) enumeration.nextElement();
                     if (subfolder.getName().equals(newName)) {
                         String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_ClassPackageClash", new Object[] {newName, pkgname});
                         fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                         return fastCheckProblem;
                     }
                 }
             }
             FileObject primFile = location.getSourceFile();
             FileObject folder = primFile.getParent();
             FileObject existing = folder.getFileObject(newName, primFile.getExt());
             if (existing != null && primFile != existing) {
                 // primFile != existing is check for case insensitive filesystems; #136434
                 String msg = NbBundle.getMessage(RenameRefactoringPlugin.class,
                         "ERR_ClassClash", newName, folder.getPath());
                 fastCheckProblem = createProblem(fastCheckProblem, true, msg);
             }
         } else if (kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER) {
             String msg = variableClashes(newName,treePath, info);
             if (msg != null) {
                 fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                 return fastCheckProblem;
             }
         } else {
             String msg = clashes(element, newName, info);
             if (msg != null) {
                 fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                 return fastCheckProblem;
             }
         }
         return fastCheckProblem;
     }
 
     public Problem preCheck(CompilationInfo info) {
         Problem preCheckProblem = null;
         fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
         Element el = info.getElementUtilities().elementFor(location.getStartPosition());
         preCheckProblem = isSourceElement(el, info);
         if (preCheckProblem != null) return preCheckProblem;
         if (el != null) {
             switch (el.getKind()) {
                 case METHOD: {
                     fireProgressListenerStep();
                     fireProgressListenerStep();
                     overriddenByMethods = SourceUtils.getOverridingMethods((ExecutableElement)el, info);
     //                            fireProgressListenerStep();
                     if (el.getModifiers().contains(Modifier.NATIVE)) {
                         preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", el));
                     }
                     if (!overriddenByMethods.isEmpty()) {
                         String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_IsOverridden",
                                 new Object[] {ElementUtilities.enclosingTypeElement(el).getSimpleName().toString()});
                         preCheckProblem = createProblem(preCheckProblem, false, msg);
                     }
                     for (ExecutableElement e : overriddenByMethods) {
                         if (e.getModifiers().contains(Modifier.NATIVE)) {
                             preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", e));
                         }
                     }
                     overridesMethods = SourceUtils.getOverridenMethods((ExecutableElement)el, info);
                     fireProgressListenerStep();
                     if (!overridesMethods.isEmpty()) {
                         boolean fatal = false;
                         for (ExecutableElement method : overridesMethods) {
                             if (method.getModifiers().contains(Modifier.NATIVE)) {
                                 preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", method));
                             }
                             if (SourceUtils.isFromLibrary(method, info.getClasspathInfo())) {
                                 fatal = true;
                                 break;
                             }
                         }
                         String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, fatal?"ERR_Overrides_Fatal":"ERR_Overrides");
                         preCheckProblem = createProblem(preCheckProblem, fatal, msg);
                     }
                     break;
                 }
                 // ===========================================================================================================
                 // The following check is, probably, not necessary as it seems impossible to hide a field member in a subclass
                 // ===========================================================================================================
     //                        case FIELD:
     //                        case ENUM_CONSTANT: {
     //                            fireProgressListenerStep();
     //                            fireProgressListenerStep();
     //                            Element hiddenField = hides(el, el.getSimpleName().toString(), info);
     //                            fireProgressListenerStep();
     //                            fireProgressListenerStep();
     //                            if (hiddenField != null) {
     //                                String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_Hides", new Object[] {ElementUtilities.enclosingTypeElement(hiddenField)});
     //                                problem[0] = createProblem(problem[0], false, msg);
     //                            }
     //                            break;
     //                        }
             }
         } else {
             preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_ErroneousSource", info.getFileObject().getPath()));
         }
         fireProgressListenerStop();
         return preCheckProblem;
     }
 
     public Problem prepare(final RefactoringElementsBag bag) {
         Lookup l = refactoring.getRefactoringSource();
         final Set<ElementLocation> references = new HashSet<ElementLocation>();
         final Map<FileObject, TransformationContext> contextMap = new HashMap<FileObject, TransformationContext>();
 
         JavaFXSource jfxs = JavaFXSource.forFileObject(location.getSourceFile());
         try {
             final Set<FileObject> refFos = new HashSet<FileObject>();
             refFos.add(location.getSourceFile());
 
             final ElementHandle[] handle = new ElementHandle[1];
             jfxs.runUserActionTask(new Task<CompilationController>() {
 
                 public void run(final CompilationController cc) throws Exception {
                     final ClassIndex ci = cc.getClasspathInfo().getClassIndex();
                     Element el = cc.getElementUtilities().elementFor(location.getStartPosition());
                     handle[0] = ElementHandle.create(el);
                     switch(el.getKind()) {
                         case CLASS:
                         case INTERFACE: {
                             refFos.addAll(ci.getResources(handle[0], EnumSet.of(SearchKind.TYPE_REFERENCES, SearchKind.TYPE_DEFS), EnumSet.allOf(SearchScope.class)));
                             if (((TypeElement)el).getNestingKind() == NestingKind.TOP_LEVEL) {
                                 new JavaFXTreePathScanner<Void, Void>() {
 
                                     @Override
                                     public Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                                         TypeElement te = (TypeElement)cc.getTrees().getElement(getCurrentPath());
                                         if (te.getNestingKind() == NestingKind.MEMBER) {
                                             refFos.addAll(ci.getResources(ElementHandle.create(te), EnumSet.of(SearchKind.TYPE_REFERENCES, SearchKind.TYPE_DEFS), EnumSet.allOf(SearchScope.class)));
                                         }
                                         return super.visitClassDeclaration(node, p);
                                     }
 
                                 }.scan(cc.getCompilationUnit(), null);
                             }
                             break;
                         }
                         case FIELD: {
                            ElementHandle typeHandle = ElementHandle.create(el.getEnclosingElement());
                            if (typeHandle != null) {
                                refFos.addAll(ci.getResources(typeHandle, EnumSet.of(SearchKind.TYPE_REFERENCES), EnumSet.allOf(SearchScope.class)));
                            }
                             refFos.addAll(ci.getResources(handle[0], EnumSet.of(SearchKind.FIELD_REFERENCES), EnumSet.allOf(SearchScope.class)));
                             break;
                         }
                         case METHOD: {
                            ElementHandle typeHandle = ElementHandle.create(el.getEnclosingElement());
                            if (typeHandle != null) {
                                refFos.addAll(ci.getResources(typeHandle, EnumSet.of(SearchKind.TYPE_REFERENCES), EnumSet.allOf(SearchScope.class)));
                            }
                             refFos.addAll(ci.getResources(handle[0], EnumSet.of(SearchKind.METHOD_REFERENCES), EnumSet.allOf(SearchScope.class)));
                             break;
                         }
                         case PACKAGE: {
                             refFos.addAll(ci.getResources(handle[0], EnumSet.of(SearchKind.PACKAGES), EnumSet.allOf(SearchScope.class)));
                             break;
                         }
                     }
                 }
             }, true);
 
             for(FileObject fo : refFos) {
                 contextMap.put(fo, new TransformationContext());
                 jfxs = JavaFXSource.forFileObject(fo);
                 jfxs.runUserActionTask(new Task<CompilationController>() {
 
                     public void run(final CompilationController cc) throws Exception {
                         JavaFXTreePathScanner<Void, Set<ElementLocation>> scanner = new BaseRefactoringScanner(location, cc);
                         scanner.scan(cc.getCompilationUnit(), references);
                     }
                 }, true);
             }
 
             for(ElementLocation loc : references) {
                 RefactoringElementImplementation refImpl = RenameRefactoringElement.create(loc, refactoring.getNewName(), loc.getElement().getSimpleName().toString(), new ProxyLookup(l, Lookups.singleton(contextMap.get(loc.getSourceFile()))));
                 if (refImpl != null) {
                     bag.add(refactoring, refImpl);
                 } else {
                     // ignore
                 }
             }
         } catch (IOException e) {
             return new Problem(true, e.getLocalizedMessage());
         }
         return null;
     }
 
     private Problem checkFileNameClash(String newName, FileObject target) {
         for(FileObject fo : target.getParent().getChildren()) {
             if (!fo.equals(target) && newName.equals(fo.getName())) {
                 String msg = NbBundle.getMessage(RenameRefactoringPlugin.class, fo.isFolder() ? "MSG_PackageExists" : "MSG_FileExists", fo.getName()); // NOI18N
                 return new Problem(true, msg);
             }
         }
         return null;
     }
 
     private static final Problem isSourceElement(TypeElement el, CompilationInfo info) {
         Problem preCheckProblem = null;
         if (SourceUtils.isFromLibrary(el, info.getClasspathInfo())) { //NOI18N
             preCheckProblem = new Problem(true, NbBundle.getMessage(
                     RenameRefactoringPlugin.class, "ERR_CannotRefactorLibraryClass",
                     el
                     ));
             return preCheckProblem;
         }
         FileObject file = SourceUtils.getFile(el,info.getClasspathInfo());
         // RetoucheUtils.isFromLibrary already checked file for null
         if (!SourceUtils.isFileInOpenProject(file)) {
             preCheckProblem =new Problem(true, NbBundle.getMessage(
                     RenameRefactoringPlugin.class,
                     "ERR_ProjectNotOpened",
                     FileUtil.getFileDisplayName(file)));
             return preCheckProblem;
         }
         return null;
     }
 
     private static final Problem isSourceElement(Element el, CompilationInfo info) {
         Element e = el;
         while(e != null && (e.getKind() != ElementKind.CLASS && e.getKind() != ElementKind.INTERFACE)) {
             e = e.getEnclosingElement();
         }
         return e != null ? isSourceElement((TypeElement)e, info) : null;
     }
 
     private String clashes(Element feature, String newName, CompilationInfo info) {
         ElementUtilities utils = info.getElementUtilities();
         Element dc = feature.getEnclosingElement();
         ElementKind kind = feature.getKind();
         if (kind.isClass() || kind.isInterface()) {
             for (Element current:ElementFilter.typesIn(dc.getEnclosedElements())) {
                 if (current.getSimpleName().toString().equals(newName)) {
                     return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InnerClassClash",new Object[] {newName, dc.getSimpleName()});
                 }
             }
         } else if (kind==ElementKind.METHOD) {
             if (utils.alreadyDefinedIn((CharSequence) newName, (ExecutableElement) feature, (TypeElement) dc)) {
                 return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_MethodClash", new Object[] {newName, dc.getSimpleName()});
             }
         } else if (kind.isField()) {
             for (Element current:ElementFilter.fieldsIn(dc.getEnclosedElements())) {
                 if (current.getSimpleName().toString().equals(newName)) {
                     return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_FieldClash", new Object[] {newName, dc.getSimpleName()});
                 }
             }
         }
         return null;
     }
 
     private String variableClashes(String newName, JavaFXTreePath tp, CompilationInfo info) {
         LocalVarScanner lookup = new LocalVarScanner(info, newName);
         JavaFXTreePath scopeBlok = tp;
         EnumSet set = EnumSet.of(Tree.JavaFXKind.BLOCK_EXPRESSION, Tree.JavaFXKind.FOR_EXPRESSION_FOR, Tree.JavaFXKind.FUNCTION_DEFINITION);
         while (scopeBlok != null && scopeBlok.getLeaf() != null && !set.contains(scopeBlok.getLeaf().getJavaFXKind())) {
             scopeBlok = scopeBlok.getParentPath();
         }
         if (scopeBlok != null) {
             Element var = info.getTrees().getElement(tp);
             lookup.scan(scopeBlok, var);
 
             if (lookup.hasRefernces())
                 return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_LocVariableClash", new Object[] {newName});
 
             Scope scope = null;
             if (tp != null) {
                 scope = info.getTrees().getScope(tp);
                 if (scope != null) {
                     for (Element el:scope.getLocalElements()) {
                         if (el.getSimpleName().toString().equals(newName)) {
                             return NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_LocVariableClash", new Object[] {newName});
                         }
                     }
                 }
             }
         }
         return null;
     }
 
 //    private static Element hides(Element field, String name, CompilationInfo info) {
 //        Elements elements = info.getElements();
 //        TypeElement jc = ElementUtilities.enclosingTypeElement(field);
 //        for (Element el:elements.getAllMembers(jc)) {
 ////TODO:
 ////            if (utils.willHide(el, field, name)) {
 ////                return el;
 ////            }
 //            if (el.getKind().isField()) {
 //                if (el.getSimpleName().toString().equals(name)) {
 //                    if (!el.getEnclosingElement().equals(field.getEnclosingElement())) {
 //                        return el;
 //                    }
 //                }
 //            }
 //        }
 //        return null;
 //    }
 }
