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
 package org.netbeans.modules.javafx.refactoring.impl.plugins;
 
 import java.io.IOException;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSourceUtils;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.modules.javafx.refactoring.impl.ElementLocation;
 import org.netbeans.modules.javafx.refactoring.impl.WhereUsedElement;
 import org.netbeans.modules.javafx.refactoring.impl.WhereUsedQueryConstants;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.scanners.FindUsagesScanner;
 import org.netbeans.modules.refactoring.api.Problem;
 import org.netbeans.modules.refactoring.api.WhereUsedQuery;
 import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
 import org.openide.filesystems.FileObject;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.Lookups;
 
 /**
  * Actual implementation of Find Usages query search for Ruby
  * 
  * @todo Perform index lookups to determine the set of files to be checked!
  * @todo Scan comments!
  * @todo Do more prechecks of the elements we're trying to find usages for
  * 
  * @author  Jaroslav Bachorik
  */
 public class WhereUsedQueryPlugin extends JavaFXRefactoringPlugin {
     private final WhereUsedQuery refactoring;
     private final ElementLocation location;
 //    private Set<IndexedClass> subclasses;
     private final String targetName;
 
     volatile private boolean cancelled = false;
 
     /** Creates a new instance of WhereUsedQuery */
     public WhereUsedQueryPlugin(WhereUsedQuery refactoring) {
         this.refactoring = refactoring;
         this.location = refactoring.getRefactoringSource().lookup(ElementLocation.class);
         targetName = location.getElement().getSimpleName().toString();
     }
 
     public void cancelRequest() {
         cancelled = true;
     }
     
     @Override
     public Problem preCheck(CompilationInfo cc) {
         if (!location.getSourceFile().isValid()) {
             return new Problem(true, NbBundle.getMessage(WhereUsedQueryPlugin.class, "DSC_ElNotAvail")); // NOI18N
         }
         
         return null;
     }
     
     //@Override
     public Problem prepare(final RefactoringElementsBag elements) {
         try {
             final Set<ElementLocation> usageLocations = new HashSet<ElementLocation>();
 
             getSource().runUserActionTask(new Task<CompilationController>() {
 
                 public void run(CompilationController cc) throws Exception {
                     Element e = location.getElement(cc);
                     collectUsages(e, cc, usageLocations);
                     collectOverridingMethods(e, cc, usageLocations);
                     collectSubclasses(e, cc, usageLocations);
                 }
             }, true);
             Lookup lkp = Lookups.singleton(location);
             for(ElementLocation location : usageLocations) {
                 elements.add(refactoring, WhereUsedElement.create(location, lkp));
             }
         } catch (IOException ex) {
             return new Problem(true, ex.getLocalizedMessage());
         }
         
         return null;
     }
 
     private void collectUsages(Element e, CompilationController cc, final Set<ElementLocation> locations) {
         if (!isFindUsages()) return;
 
         Set<ElementHandle> processingHandles = new HashSet<ElementHandle>();
         Set<FileObject> processingFiles = new HashSet<FileObject>();
 
         processingFiles.add(cc.getFileObject());
 
         ElementHandle handle = ElementHandle.create(e);
         processingHandles.add(handle);
         collectReferences(handle, processingFiles);
 
         if (isSearchFromBaseClass()) {
             for(ExecutableElement overriden : SourceUtils.getOverridenMethods((ExecutableElement)e, cc)) {
                 handle = ElementHandle.create(overriden);
                 if (!processingHandles.contains(handle)) {
                     processingHandles.add(handle);
                     collectReferences(handle, processingFiles);
                 }
             }
         }
         
         for(FileObject file : processingFiles) {
             JavaFXSource jfxs = JavaFXSource.forFileObject(file);
             if (jfxs != null) {
                 for(final ElementHandle eh : processingHandles) {
                     try {
                         jfxs.runUserActionTask(new Task<CompilationController>() {
 
                                 public void run(CompilationController cc) throws Exception {
                          new FindUsagesScanner(location, cc).scan(cc.getCompilationUnit(), locations);
                                 }
                             }, true);
                     } catch (IOException ex) {
 
                     }
                 }
             }
         }
     }
 
     private void collectOverridingMethods(Element e, CompilationController cc, final Set<ElementLocation> locations) {
         if (!isFindOverridingMethods()) return;
         if (e.getKind() != ElementKind.METHOD) return;
 
         Set<ElementHandle> processingHandles = new HashSet<ElementHandle>();
         Set<FileObject> processingFiles = new HashSet<FileObject>();
 
         processingFiles.add(cc.getFileObject());
 
         if (isSearchFromBaseClass()) {
             e = SourceUtils.getOverridenMethods((ExecutableElement)e, cc).iterator().next();
         }
 
         Stack<ExecutableElement> stack = new Stack<ExecutableElement>();
         stack.addAll(SourceUtils.getOverridingMethods(((ExecutableElement)e), cc));
         while (!stack.isEmpty()) {
             ExecutableElement method = stack.pop();
             ElementHandle handle = ElementHandle.create(method);
             if (!processingHandles.contains(handle)) {
                 processingHandles.add(handle);
                 processingFiles.add(JavaFXSourceUtils.getFile(handle, cc.getClasspathInfo()));
                 stack.addAll(SourceUtils.getOverridingMethods(method, cc));
             }
         }
 
         for(FileObject file : processingFiles) {
             JavaFXSource jfxs = JavaFXSource.forFileObject(file);
             if (jfxs != null) {
                 for(final ElementHandle eh : processingHandles) {
                     try {
                         jfxs.runUserActionTask(new Task<CompilationController>() {
                             public void run(CompilationController cc) throws Exception {
                                 new FindUsagesScanner(location, cc).scan(cc.getCompilationUnit(), locations);
                             }
                         }, true);
                     } catch (IOException ex) {
 
                     }
                 }
             }
         }
     }
 
     private void collectSubclasses(Element e, CompilationController cc, final Set<ElementLocation> handles) {
         if (!isFindDirectSubclassesOnly() && !isFindSubclasses()) return;
 
        if (e.getKind() != ElementKind.CLASS && e.getKind() != ElementKind.INTERFACE) return;

         Set<ElementHandle> processingHandles = new HashSet<ElementHandle>();
         Set<FileObject> processingFiles = new HashSet<FileObject>();
 
         processingFiles.add(cc.getFileObject());
 
         Stack<ElementHandle> stack = new Stack();
         stack.push(ElementHandle.create(e));
         while(!stack.empty()) {
             ElementHandle currentHandle = stack.pop();
             for(ElementHandle eh : getClassIndex().getElements(currentHandle, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class))) {
                 if (!processingHandles.contains(eh)) {
                     if (!isFindDirectSubclassesOnly()) {
                         stack.push(eh);
                     }
                     processingHandles.add(eh);
                     processingFiles.add(JavaFXSourceUtils.getFile(eh, cc.getClasspathInfo()));
                 }
             }
         }
 
         for(FileObject file : processingFiles) {
             JavaFXSource jfxs = JavaFXSource.forFileObject(file);
             if (jfxs != null) {
                 for(final ElementHandle eh : processingHandles) {
                     try {
                         jfxs.runUserActionTask(new Task<CompilationController>() {
 
                                 public void run(CompilationController cc) throws Exception {
                          new FindUsagesScanner(location, cc).scan(cc.getCompilationUnit(), handles);
                                 }
                             }, true);
                     } catch (IOException ex) {
 
                     }
                 }
             }
         }
     }
     
     public Problem fastCheckParameters(CompilationInfo cc) {
         if (targetName == null) {
             return new Problem(true, "Cannot determine target name. Please file a bug with detailed information on how to reproduce (preferably including the current source file and the cursor position)");
         }
         if (location.getElement(cc).getKind() == ElementKind.METHOD) {
             return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
         } 
         return null;
     }
     
     public Problem checkParameters(CompilationInfo cc) {
         return null;
     }
 
     @Override
     protected JavaFXSource prepareSource() {
         return JavaFXSource.forFileObject(location.getSourceFile());
     }
 
     private void collectReferences(ElementHandle handle, Set<FileObject> references) {
        if (handle == null) return;
         switch (handle.getKind()) {
             case CLASS: {
                 references.addAll(getClassIndex().getResources(handle, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES), EnumSet.allOf(ClassIndex.SearchScope.class)));
                 break;
             }
             case METHOD: {
                 references.addAll(getClassIndex().getResources(handle, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.allOf(ClassIndex.SearchScope.class)));
                 break;
             }
             case FIELD: {
                 references.addAll(getClassIndex().getResources(handle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.allOf(ClassIndex.SearchScope.class)));
                 break;
             }
         }
     }
 
     private void collectImplementors(ElementHandle handle, Set<FileObject> implementors) {
        if (handle == null) return;
         switch (handle.getKind()) {
             case CLASS: {
                 implementors.addAll(getClassIndex().getResources(handle, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class)));
                 break;
             }
         }
     }
 
     private Problem checkParametersForMethod(boolean overriders, boolean usages) {
         if (!(usages || overriders)) {
             return new Problem(true, NbBundle.getMessage(WhereUsedQueryPlugin.class, "MSG_NothingToFind"));
         } else
             return null;
     }
         
     private boolean isFindSubclasses() {
         return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
     }
     private boolean isFindUsages() {
         return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
     }
     private boolean isFindDirectSubclassesOnly() {
         return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
     }
     
     private boolean isFindOverridingMethods() {
         return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
     }
 
     private boolean isSearchFromBaseClass() {
         return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
     }
 
     private boolean isSearchInComments() {
         return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
     }
 }
