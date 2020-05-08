 package org.eclipse.jst.jsf.common.internal.finder.acceptor;
 
import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.jar.JarFile;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.internal.finder.VisitorMatcher.MatchingAcceptor;
 
 /**
  * A matching acceptor that provides the jars referenced by a project.
  * 
  * @author cbateman
  *
  */
 public class JarMatchingAcceptor extends MatchingAcceptor<IProject, JarFile>
 {
     @Override
     protected Collection<JarFile> getInputChildren(final IProject project)
     {
         final IJavaProject javaProject = JavaCore.create(project);
 
         IClasspathEntry[] entries = null;
         try
         {
             entries = javaProject.getResolvedClasspath(true);
         } catch (final JavaModelException e1)
         {
             JSFCommonPlugin.log(e1);
         }
 
         if (entries == null || entries.length == 0)
         {
             return Collections.EMPTY_LIST;
         }
         final List<JarFile> jars = new ArrayList<JarFile>();
         for (final IClasspathEntry entry : entries)
         {
 
             switch (entry.getEntryKind())
             {
             // this entry describes a source root in its project
             case IClasspathEntry.CPE_SOURCE:
 
                 break;
             // - this entry describes a folder or JAR containing
             // binaries
             case IClasspathEntry.CPE_LIBRARY:
             {
                 JarFile jarFileFromCPE;
                 try
                 {
                     jarFileFromCPE = getJarFileFromCPE(entry);
                     if (jarFileFromCPE != null)
                     {
                         jars.add(jarFileFromCPE);
                     }
                 } catch (final IOException e)
                 {
                     JSFCommonPlugin.log(e);
                 }
             }
                 break;
             // - this entry describes another project
             case IClasspathEntry.CPE_PROJECT:
                 // {
                 // final IPath pathToProject = entry.getPath();
                 // IWorkspace wkspace = ResourcesPlugin.getWorkspace();
                 // IResource res =
                 // wkspace.getRoot().findMember(pathToProject);
                 // if (res instanceof IProject)
                 // {
                 // tagLibsFound.addAll();
                 // }
                 // }
                 break;
             // - this entry describes a project or library indirectly
             // via a
             // classpath variable in the first segment of the path *
             case IClasspathEntry.CPE_VARIABLE:
                 break;
             // - this entry describes set of entries referenced
             // indirectly
             // via a classpath container
             case IClasspathEntry.CPE_CONTAINER:
                 break;
             }
         }
         return jars;
     }
 
     @Override
     protected Collection<JarFile> getVisitableChildren(final JarFile visitType)
     {
         return Collections.EMPTY_LIST;
     }
     
     /**
      * TODO: Merge into JSFAppConfigUtils.
      * 
      * @param entry
      * @return
      */
     private JarFile getJarFileFromCPE(final IClasspathEntry entry)
             throws IOException
     {
         if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
         {
             IPath libraryPath = entry.getPath();
             if (libraryPath.getFileExtension() != null
                     && libraryPath.getFileExtension().length() > 0)
             {
                 final IWorkspaceRoot workspaceRoot = ResourcesPlugin
                         .getWorkspace().getRoot();
                 if (libraryPath.getDevice() == null
                         && workspaceRoot.getProject(libraryPath.segment(0))
                                 .exists())
                 {
                     libraryPath = workspaceRoot.getFile(libraryPath)
                             .getLocation();
                 }
                 final String libraryPathString = libraryPath.toString();
                final File file = new File(libraryPathString);
                if (file.exists())
                {
                    return new JarFile(file);
                }
             }
         }
         return null;
     }
 
 
 }
