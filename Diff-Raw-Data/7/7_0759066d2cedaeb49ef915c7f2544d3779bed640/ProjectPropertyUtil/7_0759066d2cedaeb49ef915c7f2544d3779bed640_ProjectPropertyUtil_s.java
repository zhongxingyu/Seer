 /**
  * <copyright>
  *
  * Copyright (c) 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: ProjectPropertyUtil.java,v 1.5 2011/09/24 06:48:01 mtaal Exp $
  */
 package org.eclipse.emf.texo.eclipse;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.texo.generator.TexoResourceManager;
 import org.eclipse.jdt.core.IJavaProject;
 
 /**
  * Convenience method to get/set persistent properties on a project.
  * 
  * @author mtaal
  */
 public class ProjectPropertyUtil {
 
   public static final String TEMPLATES_LOCATION_PROPERTY = "TEMPLATE_FOLDER"; //$NON-NLS-1$
   public static final String OUTPUT_LOCATION_PROPERTY = "OUTPUT_FOLDER"; //$NON-NLS-1$
 
   public static final QualifiedName GEN_OUTPUT_FOLDER_PROPERTY = new QualifiedName(TexoEclipsePlugin.PLUGIN_ID,
       "GEN_OUTPUT_FOLDER"); //$NON-NLS-1$
 
   public static final String GEN_OUTPUT_FOLDER_PROPERTY_DEFAULT = "src-gen"; //$NON-NLS-1$
 
   /**
    * @param project
    *          the project to get the output folder for.
    * @return the output folder for the project used for generated code artifacts. Is determined on the basis of a
    *         property set ({@link #GEN_OUTPUT_FOLDER_PROPERTY}) on the project or on properties present in the .settings
    *         folder. If not set then the default {@link #GEN_OUTPUT_FOLDER_PROPERTY_DEFAULT} is used.
    */
   public static String getGenOutputFolder(IProject project) {
     final String value = getProjectProperties(project).getProperty(OUTPUT_LOCATION_PROPERTY);
     if (value == null) {
       return GEN_OUTPUT_FOLDER_PROPERTY_DEFAULT;
     }
     return value;
   }
 
   /**
    * Get the project properties from the project's .setting folder.
    */
   public static Properties getProjectProperties(IProject project) {
     final File file = getPropertiesFile(project);
     try {
       final Properties props = new Properties();
 
       // the default
       props.setProperty(OUTPUT_LOCATION_PROPERTY, GEN_OUTPUT_FOLDER_PROPERTY_DEFAULT);
 
       // set the properties from the old location
       String value = project.getPersistentProperty(TexoResourceManager.TEMPLATE_FOLDER_PROPERTY);
       if (value != null) {
         props.setProperty(TEMPLATES_LOCATION_PROPERTY, value);
       }
       value = project.getPersistentProperty(GEN_OUTPUT_FOLDER_PROPERTY);
       if (value != null) {
         props.setProperty(OUTPUT_LOCATION_PROPERTY, value);
       }
 
       if (!file.exists()) {
         return props;
       }
 
       // load from the new location
       final InputStream is = new FileInputStream(file);
       props.load(is);
       is.close();
       return props;
     } catch (Exception e) {
       throw new IllegalStateException("Error writing to file " + file.toString()); //$NON-NLS-1$
     }
   }
 
   /**
    * Set the project properties in the .settings folder.
    */
   public static void setProjectProperties(IProject project, String outputLocation, String templateLocation)
       throws CoreException {
     final Properties props = new Properties();
     if (templateLocation != null) {
       props.setProperty(TEMPLATES_LOCATION_PROPERTY, templateLocation);
     }
     props.setProperty(OUTPUT_LOCATION_PROPERTY, outputLocation);
     final File file = getPropertiesFile(project);
     try {
       final OutputStream os = new FileOutputStream(file, false);
       props.store(os, null);
       os.close();
     } catch (IOException e) {
       throw new IllegalStateException("Error writing to file " + file.toString()); //$NON-NLS-1$
     }
 
     project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
   }
 
   private static File getPropertiesFile(IProject project) {
    final IPath iPath = project.getLocation().append(".settings").append("org.eclipse.emf.texo.prefs"); //$NON-NLS-1$ //$NON-NLS-2$
     return iPath.toFile();
   }
 
   /**
    * @param object
    *          an instance of IProject of IJavaProject
    * @return the IProject
    */
   public static IProject getProject(Object object) {
     if (object instanceof IProject) {
       return (IProject) object;
     } else if (object instanceof IJavaProject) {
       return ((IJavaProject) object).getProject();
     } else {
       throw new IllegalArgumentException("The argument " + object + " is not a Project"); //$NON-NLS-1$ //$NON-NLS-2$
     }
   }
 
 }
