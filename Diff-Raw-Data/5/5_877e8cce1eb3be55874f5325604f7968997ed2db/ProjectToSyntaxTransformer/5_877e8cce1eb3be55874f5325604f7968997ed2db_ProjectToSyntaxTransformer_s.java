 package com.technophobia.substeps.document.content.assist.feature;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 
 import com.technophobia.eclipse.transformer.ProjectToJavaProjectTransformer;
 import com.technophobia.substeps.FeatureEditorPlugin;
 import com.technophobia.substeps.classloader.ClassLoadedClassAnalyser;
 import com.technophobia.substeps.classloader.JavaProjectClassLoader;
 import com.technophobia.substeps.model.Syntax;
 import com.technophobia.substeps.runner.runtime.ClassLocator;
 import com.technophobia.substeps.runner.runtime.StepClassLocator;
 import com.technophobia.substeps.runner.syntax.SyntaxBuilder;
 import com.technophobia.substeps.supplier.Transformer;
 
 public class ProjectToSyntaxTransformer implements Transformer<IProject, Syntax> {
 
     @Override
     public Syntax from(final IProject project) {
 
         final IJavaProject javaProject = new ProjectToJavaProjectTransformer().from(project);
         final ClassLoader classLoader = new JavaProjectClassLoader(javaProject);
         final String[] outputFolders = outputFoldersForProject(javaProject);
 
         final List<Class<?>> stepClasses = new ArrayList<Class<?>>();
         for (final String outputFolder : outputFolders) {
             final ClassLocator classLocator = new StepClassLocator(outputFolder, classLoader);
             stepClasses.addAll(stepClasses(outputFolder, classLocator));
         }
         return SyntaxBuilder.buildSyntax(stepClasses, new File(projectLocationPath(javaProject).toOSString()), true,
                 null, new ClassLoadedClassAnalyser(classLoader), false);
     }
 
 
     private String[] outputFoldersForProject(final IJavaProject project) {
         final Collection<String> outputFolders = new ArrayList<String>();
         final IPath projectLocation = projectLocationPath(project);
 
         try {
             final IPath defaultOutputLocation = project.getOutputLocation();
             if (defaultOutputLocation != null) {
                 final IPath fullPath = appendPathTo(projectLocation, defaultOutputLocation);
                 if (fullPath.toFile().exists()) {
                     outputFolders.add(fullPath.toOSString());
                 }
             }
             for (final IClasspathEntry entry : project.getRawClasspath()) {
                 if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                     final IPath outputLocation = entry.getOutputLocation();
                     if (outputLocation != null) {
                         final IPath fullPath = appendPathTo(projectLocation, outputLocation);
                         if (fullPath.toFile().exists()) {
                             outputFolders.add(fullPath.toOSString());
                         }
                     }
                 }
             }
         } catch (final JavaModelException ex) {
             FeatureEditorPlugin.instance().log(IStatus.WARNING,
                     "Could not get output folder location for project " + project.getElementName());
         }
 
         return outputFolders.toArray(new String[outputFolders.size()]);
     }
 
 
     private IPath appendPathTo(final IPath projectLocation, final IPath outputLocation) {
         return projectLocation.append(outputLocation.removeFirstSegments(1));
     }
 
 
     private IPath projectLocationPath(final IJavaProject project) {
         return project.getResource().getLocation().makeAbsolute();
     }
 
 
     private List<Class<?>> stepClasses(final String outputFolder, final ClassLocator classLocator) {
         FeatureEditorPlugin.instance().log(IStatus.INFO, "output folder " + outputFolder);
         return toList(classLocator.fromPath(outputFolder));
     }
 
 
     private List<Class<?>> toList(final Iterator<Class<?>> it) {
         final List<Class<?>> list = new ArrayList<Class<?>>();
         while (it.hasNext()) {
            list.add(it.next());
         }
         return list;
     }
 
 }
