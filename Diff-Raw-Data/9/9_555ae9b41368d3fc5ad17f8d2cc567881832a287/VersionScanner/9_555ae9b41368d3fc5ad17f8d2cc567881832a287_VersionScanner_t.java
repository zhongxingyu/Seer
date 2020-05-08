 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.releng.actions;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.releng.ReleaseEngineeringPlugin;
 import org.eclipse.imp.releng.metadata.FileVersionMap;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.team.core.history.IFileHistoryProvider;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.internal.ccvs.core.CVSException;
 import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
 import org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber;
 import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileHistoryProvider;
 
 public class VersionScanner {
     private final IWorkspaceRoot fWSRoot= ResourcesPlugin.getWorkspace().getRoot();
 
     private Comparator<IFile> fFileComparator= new Comparator<IFile>() {
         public int compare(IFile o1, IFile o2) {
             return o1.getLocation().toPortableString().compareTo(o2.getLocation().toPortableString());
         }
     };
 
     /**
      * Updates the given FileVersionMap with the workspace copy's version # for the
      * file at the given path. If the given file doesn't exist yet, assigns version
      * "1.1".
     * @return a set containing the given file, if dirty, or empty, if not
      */
     public Set<IFile> checkFile(String projRelFilePath, IProject pluginProject, final IFileHistoryProvider histProvider, final FileVersionMap fileVersionMap) {
         IFile file= pluginProject.getFile(projRelFilePath);
 
         if (file.exists()) {
             return checkFile(file, histProvider, fileVersionMap);
         } else {
             // TODO Should this really set the file version at 1.1?
             fileVersionMap.setVersion(file, "1.1");
             return Collections.EMPTY_SET;
         }
     }
 
     /**
      * Updates the FileVersionMap to indicate the source repository file version
      * for the workspace copy of the given IFile.<br>
      * Issues a diagnostic message if the workspace copy is dirty. <b>NOTE: This
      * functionality is currently disabled due to a bug in Team/CVS.</b>
     * @return a set containing the given file, if dirty, or empty, if not
      */
     private Set<IFile> checkFile(IFile file, final IFileHistoryProvider histProvider, final FileVersionMap fileVersionMap) {
         IFileRevision fileRev= histProvider.getWorkspaceFileRevision(file);
 
 //      VersionIncrementerPlugin.getMsgStream().println("Source file " + resource.getName() + " => " + fileRev.getContentIdentifier());
         if (fileRev == null) {
             // Apparently this file is not under version control
             return Collections.EMPTY_SET;
         }
         fileVersionMap.setVersion(file, fileRev.getContentIdentifier());
 
         // Try to determine whether the workspace file is "dirty". Do this by comparing
         // the timestamp of the workspace file with that of the associated repository
         // revision.
         if (isDirty(file, histProvider)) {
             ReleaseEngineeringPlugin.getMsgStream().println("File " + file.getFullPath() + " is out of sync w/ CVS HEAD!");
             return Collections.singleton(file);
         }
         return Collections.EMPTY_SET;
         // Unfortunately, fileRev.getTimestamp() throws an NPE from inside the Team/CVS
         // code, so the following is commented out.
 //      if (file.getLocalTimeStamp() != fileRev.getTimestamp()) {
 //          VersionIncrementerPlugin.getMsgStream().println("File " + file.getFullPath() + " is out of sync w/ CVS HEAD!");
 //          fCanProceed= false;
 //      }
 
         // TODO Attempt to determine whether the workspace file revision is the same as HEAD, and bark if not.
     }
 
     protected boolean isDirty(IFile file, IFileHistoryProvider histProvider) {
         if (histProvider instanceof CVSFileHistoryProvider) {
             // Following is adapted from CVSLightweightDecorator
             try {
                 return getCVSSubscriber().isDirty(file, null);
             } catch (CVSException e) {
                 return false;
             }
         } else {
 //          ISVNLocalResource res= null; // How to get one of these for the given IFile?
 //          SVNLightweightDecorator dec= new SVNLightweightDecorator(); // How to get at one of these?
 //          boolean dirty= dec.isDirty(res);
             return false; // don't know
         }
     }
 
     private static CVSWorkspaceSubscriber getCVSSubscriber() {
         return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
     }
 
    /**
     * @return the set of dirty files in the given project
     */
     public Set<IFile> scanSrcFolders(IProject pluginProject, final IFileHistoryProvider histProvider, final FileVersionMap fileVersionMap) {
         IJavaProject pluginJavaProject= JavaCore.create(pluginProject);
         List<IPath> srcFolderPaths= collectSrcFolders(pluginJavaProject);
         Set<IFile> result= new TreeSet<IFile>(fFileComparator);
 
         for(IPath srcFolderPath: srcFolderPaths) {
             IFolder srcFolder= fWSRoot.getFolder(srcFolderPath);
 
             result.addAll(scanFolder(srcFolder, histProvider, fileVersionMap));
         }
         return result;
     }
 
     /**
      * @return the list of source folder IPaths for the given java project
      */
     private List<IPath> collectSrcFolders(IJavaProject javaProject) {
         List<IPath> srcFolders= new ArrayList<IPath>();
         try {
             IClasspathEntry[] cpEntries= javaProject.getResolvedClasspath(true);
 
             for(IClasspathEntry entry: cpEntries) {
                 switch(entry.getEntryKind()) {
                     case IClasspathEntry.CPE_SOURCE: {
                         if (!entry.getPath().equals(javaProject.getProject().getFullPath())) {
                             srcFolders.add(entry.getPath());
                         }
                     }
                 }
             }
         } catch (CoreException e) {
             ReleaseEngineeringPlugin.logError(e);
         }
         return srcFolders;
     }
 
     public Set<IFile> scanFolder(String projRelPath, IProject project, IFileHistoryProvider histProvider, final FileVersionMap fileVersionMap) {
         IFolder folder= project.getFolder(projRelPath);
 
         if (folder.exists()) {
             return scanFolder(folder, histProvider, fileVersionMap);
         }
         return Collections.EMPTY_SET;
     }
 
     private Set<IFile> scanFolder(IFolder folder, final IFileHistoryProvider histProvider, final FileVersionMap fileVersionMap) {
         final Set<IFile> result= new TreeSet<IFile>(fFileComparator);
 
         try {
             folder.accept(new IResourceVisitor() {
                 public boolean visit(IResource resource) throws CoreException {
                     if (resource instanceof IFile) {
                         IFile file= (IFile) resource;
 
                         if (!file.isDerived()) {
                             result.addAll(checkFile(file, histProvider, fileVersionMap));
                         }
                         return false;
                     }
                     return true;
                 }
             });
         } catch(CoreException e) {
             ReleaseEngineeringPlugin.logError(e);
         }
         return result;
     }
 }
