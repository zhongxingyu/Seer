 /**
  * Copyright (c) 2010, 2011 Darmstadt University of Technology.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Marcel Bruch - initial API and implementation.
  */
 package org.eclipse.recommenders.codesearch.rcp.index.ui;
 
 import static com.google.common.collect.Ordering.usingToString;
 import static java.util.Arrays.asList;
 import static org.eclipse.recommenders.codesearch.rcp.index.indexer.CodeIndexer.addFieldToDocument;
 import static org.eclipse.recommenders.codesearch.rcp.index.indexer.ResourcePathIndexer.getFile;
 import static org.eclipse.recommenders.utils.Checks.cast;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.lucene.document.Document;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IClassFile;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
 import org.eclipse.jdt.internal.core.SourceMapper;
 import org.eclipse.jdt.ui.SharedASTProvider;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.CodeIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.ResourcePathIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.TimestampIndexer;
 import org.eclipse.recommenders.rcp.RecommendersPlugin;
 import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
 
 public class ProjectIndexerRunnable implements IRunnableWithProgress {
 
     private final IJavaProject project;
     private final CodeIndexer indexer;
     private IPackageFragmentRoot[] roots;
     private IProgressMonitor monitor;
 
     //
     IPackageFragmentRoot root;
     private File rootLocation;
     private SourceMapper sourceMapper;
 
     public ProjectIndexerRunnable(final IJavaProject project, final CodeIndexer indexer) {
         this.project = project;
         this.indexer = indexer;
     }
 
     @Override
     public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 
         try {
             this.roots = project.getPackageFragmentRoots();
             this.monitor = monitor;
             openMonitor();
 
             for (final IPackageFragmentRoot root : roots) {
                 analyzeRoot(root);
             }
         } catch (final Exception e) {
             RecommendersPlugin.logError(e, "Error during code search indexing");
         } finally {
             closeMonitor();
         }
     }
 
     private void openMonitor() {
         monitor.beginTask("", roots.length + 1);
         monitor.subTask("About to begin...");
         monitor.worked(1);
     }
 
     private void analyzeRoot(final IPackageFragmentRoot root) throws Exception {
         this.root = root;
         monitor.subTask("Analyzing " + root.getElementName());
 
         rootLocation = getFile(root);
         if (monitor.isCanceled()) {
             return;
         }
         if (rootLocation == null) {
             return;
         }
        if (hasNotChanged(rootLocation)) {
             return;
         }
         if (root.isArchive()) {
             indexer.delete(rootLocation);
             if (!findSourceMapper()) {
                 return;
             }
         }
 
         analyzePackageFragments();
 
         monitor.subTask("Checkpointing...");
         indexer.commit();
         monitor.worked(1);
         root.close();
     }
 
     private void analyzePackageFragments() throws Exception {
         for (final IJavaElement child : usingToString().sortedCopy(asList(root.getChildren()))) {
             final IPackageFragment fragment = cast(child);
             monitor.subTask("Analyzing " + fragment.getElementName());
 
             if (fragment.getElementName().startsWith("sun.")) {
                 continue;
             }
             if (fragment.getElementName().startsWith("com.sun.")) {
                 continue;
             }
             if (fragment.getElementName().startsWith("com.oracle.")) {
                 continue;
             }
             if (fragment.getElementName().startsWith("sunw.")) {
                 continue;
             }
 
             for (final IClassFile clazz : usingToString().sortedCopy(asList(fragment.getClassFiles()))) {
                 analyzeClassFile(clazz);
             }
             for (final ICompilationUnit cu : fragment.getCompilationUnits()) {
                 analyzeCompilationUnit(cu);
             }
             fragment.close();
         }
         addArchiveVisitedMarker();
     }
 
     private void analyzeCompilationUnit(final ICompilationUnit cu) {
         try {
             final File cuLocation = getFile(cu);
            if (hasNotChanged(cuLocation)) {
                 return;
             }
             final CompilationUnit ast = SharedASTProvider.getAST(cu, SharedASTProvider.WAIT_YES, monitor);
             if (ast == null) {
                 return;
             }
             indexer.delete(cuLocation);
             indexer.index(ast);
         } catch (final Exception e) {
             RecommendersUtilsPlugin.logError(e, "Failed to index '%s'", cu.getResource().getFullPath());
         }
     }
 
    private boolean hasNotChanged(final File file) {
         final long lastModified = file.lastModified();
         final long lastIndexed = indexer.lastIndexed(file);
         return lastModified < lastIndexed;
     }
 
     @SuppressWarnings("restriction")
     private boolean findSourceMapper() {
         final JarPackageFragmentRoot jarRoot = (JarPackageFragmentRoot) root;
         sourceMapper = jarRoot.getSourceMapper();
         return sourceMapper != null;
     }
 
     private void analyzeClassFile(final IClassFile clazz) {
         if (monitor.isCanceled()) {
             return;
         }
         if (clazz.getElementName().contains("$")) {
             return;
         }
 
         final IType type = clazz.getType();
         if (type == null) {
             return;
         }
         final CompilationUnit ast = SharedASTProvider.getAST(clazz, SharedASTProvider.WAIT_YES, monitor);
         if (ast == null) {
             return;
         }
 
         // final String sourceFileName = type.getElementName() + ".java";
         // final char[] source = sourceMapper.findSource(type, sourceFileName);
         // if (source == null) {
         // return;
         // }
         // final IJavaProject javaProject = root.getJavaProject();
         final String unitName = root.getPath() + "!" + type.getFullyQualifiedName();
         // final CompilationUnit ast = CompilationUnitHelper.parse(source,
         // unitName, javaProject);
         // ast.setProperty("location", rootLocation);
         // ast.setProperty("project", javaProject);
         monitor.subTask(unitName);
         indexer.add(ast);
     }
 
     private void addArchiveVisitedMarker() throws IOException {
         final Document visited = new Document();
         addFieldToDocument(visited, Fields.RESOURCE_PATH, ResourcePathIndexer.getPath(rootLocation));
         addFieldToDocument(visited, Fields.TIMESTAMP, TimestampIndexer.getTimeString());
         indexer.addDocument(visited);
     }
 
     private void closeMonitor() {
         monitor.done();
     }
 }
