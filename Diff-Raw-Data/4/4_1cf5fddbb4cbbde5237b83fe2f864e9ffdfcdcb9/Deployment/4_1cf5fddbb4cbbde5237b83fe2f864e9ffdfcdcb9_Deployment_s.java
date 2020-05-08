 /*
  * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  *     Vladimir Pasquier <vpasquier@nuxeo.com>
  */
 package org.nuxeo.ide.sdk.deploy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.nuxeo.ide.sdk.IConnectProvider;
 import org.nuxeo.ide.sdk.SDKPlugin;
 import org.nuxeo.ide.sdk.index.UnitProvider;
 import org.nuxeo.ide.sdk.userlibs.UserLib;
 
 /**
  * Deployment provides all projects outputs (sources, resources) giving to
  * server the dev.bundle file with project outputs locations
  */
 public class Deployment {
 
     protected String name;
 
     protected Set<IProject> projects;
 
     protected Set<String> libs;
 
     protected UnitProvider unitProvider;
 
     public static final String DEPENDENCY_NAME = "org.jboss.seam.annotations.Name";
 
     public static final String SOURCE_ELEMENT = "@Name";
 
     public Deployment(String name) {
         this.name = name;
         projects = new HashSet<IProject>();
         libs = new HashSet<String>();
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void addProject(IProject project) {
         projects.add(project);
     }
 
     public void addProjects(IProject[] projects) {
         this.projects.addAll(Arrays.asList(projects));
     }
 
     public void removeProject(IProject project) {
         projects.remove(project);
     }
 
     public void clearProjects() {
         projects.clear();
     }
 
     public IProject[] getProjects() {
         return projects.toArray(new IProject[projects.size()]);
     }
 
     public void addLibrary(UserLib lib) {
         libs.add(lib.getPath());
     }
 
     public void addLibraries(UserLib[] libs) {
         for (UserLib lib : libs) {
             this.libs.add(lib.getPath());
         }
     }
 
     public void removeLibrary(UserLib lib) {
         libs.remove(lib.getPath());
     }
 
     public void clearLibraries() {
         libs.clear();
     }
 
     public List<String> getProjectNames() {
         ArrayList<String> result = new ArrayList<String>(projects.size());
         for (IProject project : projects) {
             result.add(project.getName());
         }
         return result;
     }
 
     public Set<String> getLibraryPaths() {
         return libs;
     }
 
     public UserLib[] getLibraries() {
         UserLib[] result = new UserLib[libs.size()];
         int i = 0;
         for (String path : libs) {
             result[i++] = new UserLib(path);
         }
         return result;
     }
 
     public String getContentAsString() throws Exception {
         String crlf = "\n";
         StringBuilder builder = new StringBuilder();
         builder.append("# Projects").append(crlf);
         for (IProject project : projects) {
             unitProvider = new UnitProvider();
             // Sort units per type
             unitProvider.getUnitsForDep(project, DEPENDENCY_NAME,
                     SOURCE_ELEMENT);
             // Workspace - Project Path
             String workspacePath = project.getWorkspace().getRoot().getLocation().toOSString();
            String projectPath = workspacePath + File.separator
                    + project.getName() + File.separator;
             // Resources copy
             resourcesCopy(project, projectPath + "pojo-bin");
             // default classes -> copy all pojo classes into pojo-bin output
             // folder
             for (ICompilationUnit unit : unitProvider.getPojoUnits()) {
                 unitOutputCopy(projectPath, unit, project, "pojo-bin");
             }
             // Write into dev.bundles the path to pojo-bin
             builder.append("bundle:").append(projectPath + "pojo-bin").append(
                     File.separator).append("main").append(crlf);
             // Seam bin cleanup
             File seamBin = new File(projectPath + "seam-bin");
             if (seamBin.exists()) {
                 deleteTree(seamBin);
             }
             // Seam classes -> copy all seam classes into seam-bin output
             // folder
             for (ICompilationUnit unit : unitProvider.getDepUnits()) {
                 unitOutputCopy(projectPath, unit, project, "seam-bin");
             }
             // Write into dev.bundles the path to seam-bin
             if (!unitProvider.getDepUnits().isEmpty()) {
                 builder.append("seam:").append(projectPath + "seam-bin").append(
                         File.separator).append("main").append(crlf);
             }
             // l10n resource bundle fragments
             IFolder l10n = project.getFolder("src/main/resources/OSGI-INF/l10n");
             if (l10n.exists()) {
                 for (IResource m : l10n.members()) {
                     if (IResource.FILE == m.getType()) {
                         builder.append("resourceBundleFragment:").append(
                                 m.getLocation().toOSString()).append(crlf);
                     }
                 }
             }
             // studio project dependencies
             IConnectProvider connectProvider = SDKPlugin.getDefault().getConnectProvider();
             if (connectProvider != null) {
                 for (IConnectProvider.Infos infos : SDKPlugin.getDefault().getConnectProvider().getLibrariesInfos(
                         project, null)) {
                     builder.append("bundle:").append(infos.file.getPath()).append(
                             crlf);
                 }
             }
         }
         builder.append(crlf);
         builder.append("# User Libraries").append(crlf);
         for (String lib : libs) {
             File file = new File(lib);
             if (file.exists()) {
                 builder.append("library:").append(lib).append(crlf);
             }
         }
         return builder.toString();
     }
 
     protected void unitOutputCopy(String projectPath, ICompilationUnit unit,
             IProject project, String outputPath) throws IOException,
             JavaModelException {
         // Retrieve the output class of java unit
         String clazzOutputPath = outputPath(project,
                 unit.getParent().getResource().getProjectRelativePath());
         File clazzOutputFile = new File(clazzOutputPath + File.separator
                 + relativeUnitPath(unit));
         // New class output target
         File newClazzOutputFile = new File(projectPath + outputPath
                 + File.separator + "main" + File.separator
                 + relativeUnitPath(unit));
         if (newClazzOutputFile.exists()) {
             // clean up
             newClazzOutputFile.delete();
         }
         // Copy each class in the structure created into
         // output folders
         newClazzOutputFile.getParentFile().mkdirs();
         FileUtils.copyFile(clazzOutputFile, newClazzOutputFile);
     }
 
     protected String relativeUnitPath(ICompilationUnit unit) {
         String unitPath = unit.getPath().toOSString();
         int index = unitPath.indexOf(unitProvider.getParentNameSpace());
         String relativePath = unitPath.substring(index);
         // Add .class
         int mid = relativePath.lastIndexOf(".");
         relativePath = relativePath.substring(0, mid).concat(".class");
         return relativePath;
     }
 
     protected void resourcesCopy(IProject project, String pojoBin)
             throws JavaModelException, IOException {
         String resourcesOutputPath = outputPath(project, new Path(
                 "src/main/resources"));
         File resourcesOutputFolder = new File(resourcesOutputPath);
         File newResourcesOutputFolder = new File(pojoBin + File.separator
                 + "main");
         copyFolder(resourcesOutputFolder, newResourcesOutputFolder);
     }
 
     /**
      * Retrieve from source path output path in output/bin folder
      */
     protected String outputPath(IProject project, IPath sourcePath)
             throws JavaModelException {
         IJavaProject java = JavaCore.create(project);
         IFolder folder = project.getFolder(sourcePath);
         if (!folder.exists()) {
             return null;
         }
         IPackageFragmentRoot root = java.getPackageFragmentRoot(folder);
         // We introspect the source tree in order to find the classpath src
         // entry (in case of src/main/seam)
         while (!root.isOpen()) {
             folder = project.getFolder(folder.getParent().getProjectRelativePath());
             root = java.getPackageFragmentRoot(folder);
         }
         IClasspathEntry entry = root.getRawClasspathEntry();
         IPath outputLocation = entry.getOutputLocation();
         if (outputLocation == null) {
             outputLocation = java.getOutputLocation();
         }
         IFolder output = project.getWorkspace().getRoot().getFolder(
                 outputLocation);
 
         String path = output.getRawLocation().toOSString();
         return path;
     }
 
     public void copyFolder(File src, File dest) throws IOException {
         if (src.isDirectory()) {
             // Check if its sources directory (do not copy before introspection)
             if (!src.getName().equals(unitProvider.getParentNameSpace())) {
                 if (dest.exists()) {
                     deleteTree(dest);
                 }
                 dest.mkdirs();
                 String files[] = src.list();
                 for (String file : files) {
                     File srcFile = new File(src, file);
                     File destFile = new File(dest, file);
                     copyFolder(srcFile, destFile);
                 }
             }
         } else {
             InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest);
             byte[] buffer = new byte[1024];
             int length;
             while ((length = in.read(buffer)) > 0) {
                 out.write(buffer, 0, length);
             }
             in.close();
             out.close();
         }
     }
 
     public static void deleteTree(File dir) {
         for (File file : dir.listFiles()) {
             if (file.isDirectory()) {
                 deleteTree(file);
             } else {
                 file.delete();
             }
         }
         dir.delete();
     }
 }
