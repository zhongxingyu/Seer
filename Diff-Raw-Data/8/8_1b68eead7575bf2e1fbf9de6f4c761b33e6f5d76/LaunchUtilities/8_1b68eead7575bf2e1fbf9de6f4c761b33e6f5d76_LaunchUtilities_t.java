 /**********************************************************************
 Copyright (c) 2005 Michael Grundmann and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Contributors:
 2006 Andy Jefferson - allow for using class files as input
     ...
 **********************************************************************/
 package org.datanucleus.ide.eclipse.jobs;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.datanucleus.ide.eclipse.Plugin;
 import org.datanucleus.ide.eclipse.preferences.GeneralPreferencePage;
 import org.datanucleus.ide.eclipse.preferences.PreferenceConstants;
 import org.datanucleus.ide.eclipse.project.ProjectHelper;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.JavaRuntime;
 
 /**
  * Series of utilities used for launching processes.
  */
 public class LaunchUtilities implements IJavaLaunchConfigurationConstants
 {
     /**
      * Run a class
      * @param javaProject
      * @param name
      * @param classpath
      * @param mainClass
      * @param vmArgs
      * @param workingDir
      * @param programArgs
      * @param processId unique id for the process
      * @return the result of the launch session
      * @throws CoreException
      */
     public static ILaunch launch(IJavaProject javaProject, String name, String mainClass, List classpath, String vmArgs, String workingDir, String programArgs, String processId)
         throws CoreException
     {
         ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
         ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
         ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
         ILaunchConfiguration config = null;
         for (int i = 0; i < configurations.length; i++)
         {
             ILaunchConfiguration configuration = configurations[i];
             if (configuration.getName().equals(name))
             {
                 configuration.delete();
             }
         }
         ILaunchConfigurationWorkingCopy wc = type.newInstance(null, name);
         wc.setAttribute(ATTR_DEFAULT_CLASSPATH, false);
         wc.setAttribute(ATTR_CLASSPATH, classpath);
         wc.setAttribute(ATTR_PROJECT_NAME, javaProject.getProject().getName());
         wc.setAttribute(ATTR_MAIN_TYPE_NAME, mainClass);
         wc.setAttribute(ATTR_SOURCE_PATH, getProjectSourcePath(javaProject));
       
         wc.setAttribute(ATTR_VM_ARGUMENTS, vmArgs);
         if( workingDir!= null )
         {
             wc.setAttribute(ATTR_WORKING_DIRECTORY, workingDir);
         }
         wc.setAttribute(ATTR_PROGRAM_ARGUMENTS, programArgs);
         config = wc.doSave();
         return config.launch(ILaunchManager.RUN_MODE, null);
     }
 
     private static List getProjectSourcePath(IJavaProject javaProject) throws CoreException
     {
         List list = new ArrayList();
         IRuntimeClasspathEntry entry = JavaRuntime.newProjectRuntimeClasspathEntry(javaProject);
         entry.setClasspathProperty(IRuntimeClasspathEntry.PROJECT);
         entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
         list.add(entry.getMemento());
 
         return list;
     }    
     
     /**
      * Utility method to obtain the classpath entries for the given <code>IJavaProject</code>
      * @param javaProject The <code>IJavaProject</code> the classpath should be obtained for
      * @return A <code>List</code> of valid classpath entries as <code>String</code> values
      * @throws CoreException
      */
     public static List getDefaultClasspath(IJavaProject javaProject) throws CoreException
     {
         List classpath = new ArrayList();
 
         IRuntimeClasspathEntry outputEntry = JavaRuntime.newDefaultProjectClasspathEntry(javaProject);
         classpath.add(outputEntry.getMemento());
 
         IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
         IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath,
             IRuntimeClasspathEntry.STANDARD_CLASSES);
         classpath.add(systemLibsEntry.getMemento());
 
         String useProjectSettingsStr = javaProject.getResource().getPersistentProperty(
             new QualifiedName("org.datanucleus.ide.eclipse.preferences.general", "useProjectSettings"));
        boolean useProjectSettings = useProjectSettingsStr != null ? Boolean.parseBoolean(useProjectSettingsStr) : false;
 
         boolean useProjectClasspath = Plugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_PROJECT_CLASSPATH);
         if (useProjectSettings)
         {
            String useProjCPStr = javaProject.getResource().getPersistentProperty(
                 new QualifiedName("org.datanucleus.ide.eclipse.preferences.general", PreferenceConstants.USE_PROJECT_CLASSPATH));
            useProjectClasspath = (useProjCPStr != null ? Boolean.parseBoolean(useProjCPStr) : false);
         }
 
         if (!useProjectClasspath)
         {
             // Add on user-defined classpath jars if required
             String classpathEntries = useProjectSettings ?
                     javaProject.getResource().getPersistentProperty(
                         new QualifiedName("org.datanucleus.ide.eclipse.preferences.general", PreferenceConstants.CLASSPATH_ENTRIES)) :
                     Plugin.getDefault().getPreferenceStore().getString(PreferenceConstants.CLASSPATH_ENTRIES);
             IRuntimeClasspathEntry preferenceEntry = JavaRuntime.newStringVariableClasspathEntry(classpathEntries);
             classpath.add(preferenceEntry.getMemento());
         }
 
         return classpath;
     }
 
     /**
      * Utility method to return the default VM arguments for the current launch configuration.
      * @param resource The underlying resource
      * @return The default VM arguments for the current launch configuration
      */
     public static String getDefaultVMArguments(IResource resource)
     {
         StringBuffer args = new StringBuffer();
 
         // Log4J configuration file (if specified)
         String logFileURL = ProjectHelper.getStringPreferenceValue(resource, GeneralPreferencePage.PAGE_ID,
             PreferenceConstants.LOGGING_CONFIGURATION_FILE);
         if (logFileURL != null && logFileURL.trim().length() > 0)
         {
             // Specify the log4j config if present
             args.append(" -Dlog4j.configuration=file:\"").append(logFileURL).append("\" ");
         }
 
         return args.toString();
     }
 
     /**
      * Utility method to obtain the default output dir for the given. Does not support linked resources
      * <code>IJavaProject</code>.
      * @param javaProject The current <code>IJavaProject</code>
      * @return The default output directory for the given
      * <code>IJavaProject</code>
      */
     public static String getWorkingDir(IJavaProject javaProject)
     {
         String outputDir = "";
 
         try
         {
             IPath relativeOutLocation = javaProject.getOutputLocation();
             IWorkspaceRoot root = javaProject.getProject().getWorkspace().getRoot();
             if( javaProject.getPath().equals(relativeOutLocation))
             {
                 outputDir = javaProject.getProject().getLocation().toOSString();                
             }
             else
             {
                 IFolder outputFolder1 = root.getFolder(relativeOutLocation);
                 outputDir = outputFolder1.getRawLocation().toOSString();
             }
         }
         catch (JavaModelException e)
         {
             e.printStackTrace();// TODO
         }
         return outputDir;
     }
 
     /**
      * Utility method to compute the default input files (JDO) for the current launch configuration.
      * @param resource The <code>IResource</code> for this launch configuration
      * @param javaProject The parent <code>IJavaProject</code>
      * @return a <code>String</code> containing the program arguments
      */
     public static String getDefaultInputFiles(IResource resource, IJavaProject javaProject)
     {
         StringBuffer args = new StringBuffer();
         List argsList = new ArrayList();
         LaunchUtilities.getInputFiles(argsList, resource, javaProject, new String[] {"jdo"}, true);
         for (int i = 0; i < argsList.size(); i++)
         {
             args.append(argsList.get(i));
         }
 
         return args.toString();
     }
 
     /**
      * This method returns a <code>List</code> of input file locations for the given <code>IResource</code>
      * @param args
      * @param resource The parent <code>IResource</code>
      * @param javaProject The parent <code>IJavaProject</code>
      * @param preferenceFileSuffix An array of valid input file extensions, or
      * <code>null</code> if defaults should be used
      * @param quoteArgs Whether to quote any args
      * @return A <code>List</code> of metadata file locations
      */
     public static List getInputFiles(List args, IResource resource, IJavaProject javaProject, String[] preferenceFileSuffix, boolean quoteArgs)
     {
         // Create a set of valid input file suffixes
         Set fileSuffixes = new HashSet();
         // If file suffixes had been passed to this method...
         if (preferenceFileSuffix != null)
         {
             // ...add them to the set
             for (int i = 0; i < preferenceFileSuffix.length; i++)
             {
                 fileSuffixes.add(preferenceFileSuffix[i]);
             }
         }
         else
         {
             // If nothing has been passed add default "jdo" value
             String defaultFileSuffix = "jdo";
             fileSuffixes.add(defaultFileSuffix);
         }
 
         // Given resource is a folder...
         if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT || resource.getType() == IResource.ROOT)
         {
             try
             {
                 IResource[] members = ((IContainer) resource).members();
                 for (int i = 0; i < members.length; i++)
                 {
                     // ... so run the method with the folder as input
                     args = getInputFiles(args, members[i], javaProject, preferenceFileSuffix, quoteArgs);
                 }
             }
             catch (CoreException e)
             {
                 e.printStackTrace(); // TODO Error handling
             }
             return args;
         }
         // Given resource is a file...
         else if (resource.getType() == IResource.FILE)
         {
             IFile file = (IFile) resource;
             if (file.isAccessible())
             {
                 // ... check if file is valid and has the right extension
                 if (file.getLocation() != null && file.getLocation().getFileExtension() != null &&
                     fileSuffixes.contains(file.getLocation().getFileExtension().toLowerCase()))
                 {
                     if (quoteArgs)
                     {
                         args.add(" \"" + file.getLocation().toOSString() + "\" ");
                     }
                     else
                     {
                         args.add(file.getLocation().toOSString());
                     }
                     /*ListIterator paths = getSourcePaths(javaProject).listIterator();
                     while (paths.hasNext())
                     {
                         if (file.getProjectRelativePath().toString().startsWith(((IPath) paths.next()).makeRelative().removeFirstSegments(1).toString()))
                         {
                             // check if file allready exists in the list...
                             if (!args.contains(" \"" + file.getLocation().toOSString() + "\" "))
                             {
                                 // ...if not add it
                                 args.add(" \"" + file.getLocation().toOSString() + "\" ");
                             }
                             break;
                         }
                     }*/
                 }
             }
         }
 
         return args;
     }
 
     /**
      * Convenience method to get all source parts of the CLASSPATH.
      * @param javaProject The java project
      * @return The source parts of the classpath
      */
     protected static List getSourcePaths(IJavaProject javaProject)
     {
         List paths = new ArrayList();
         IClasspathEntry entries[];
         try
         {
             entries = javaProject.getRawClasspath();
             for (int i = 0; i < entries.length; i++)
             {
                 if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
                 {
                     paths.add(entries[i].getPath());
                 }
             }
         }
         catch (JavaModelException e)
         {
             e.printStackTrace();
         }
         return paths;
     }
 }
