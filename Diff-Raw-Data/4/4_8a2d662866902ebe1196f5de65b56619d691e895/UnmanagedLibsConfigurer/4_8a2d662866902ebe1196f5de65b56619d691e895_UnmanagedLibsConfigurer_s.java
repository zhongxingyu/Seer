 package sbt.eclipse.logic;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 
 import sbt.eclipse.Constants;
 
 /**
  * Adds unmanaged libs to project classpath.
  * 
  * @author Joonas Javanainen
  * 
  */
 public class UnmanagedLibsConfigurer extends AbstractConfigurer {
 
     /**
      * @param project
      * @throws CoreException
      */
     public UnmanagedLibsConfigurer(IProject project) throws CoreException {
         super(project);
     }
 
     @Override
     public void run(IProgressMonitor monitor) throws CoreException {
         List<IClasspathEntry> classpaths = new ArrayList<IClasspathEntry>();
 
        File libFolder = project.getFolder(Constants.DEFAULT_LIB_FOLDER)
                .getFullPath().toFile();
         if (!libFolder.exists())
             return;
 
         List<IPath> wantedLibs = new ArrayList<IPath>();
         for (File foundLibFile : libFolder.listFiles()) {
             wantedLibs.add(new Path(foundLibFile.getAbsolutePath()));
         }
 
         for (IClasspathEntry entry : javaProject.getRawClasspath()) {
             if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                 wantedLibs.remove(entry.getPath());
             }
             classpaths.add(entry);
         }
 
         for (IPath libPath : wantedLibs) {
             classpaths.add(JavaCore.newLibraryEntry(libPath, null, null));
         }
 
         javaProject.setRawClasspath(classpaths
                 .toArray(Constants.EMPTY_CLASSPATHENTRY_ARRAY), monitor);
     }
 
 }
