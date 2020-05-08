 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 
 public class WebPropertiesUtil {
 	// private static final char[] BAD_CHARS = {'/', '\\', ':'};
 	private static final char[] BAD_CHARS = {':'};
 	public static final String DEFAULT_JAVA_SOURCE_NAME = "Java Source"; //$NON-NLS-1$
 
 	/**
 	 * Update the Web Content folder to a new value if it is different. This applies to both Static
 	 * and J2EE Web Projects. In the case of a J2EE Project, the library classpath entries will be
 	 * modifies to reflect the new location.
 	 * 
 	 * @param project
 	 *            The Web Project to update
 	 * @param webContentName
 	 *            The new name given to the Web Project's Web Content folder
 	 * @param progressMonitor
 	 *            Indicates progress of the update operation
 	 * @return True if the web content rename was actually renamed, false if unneeded.
 	 * @throws CoreException
 	 *             The exception that occured during renaming of the the project's web content
 	 *             folder
 	 */
 	public static boolean updateWebContentNameAndProperties(IProject project, String webContentName, IProgressMonitor progressMonitor) throws CoreException {
 		boolean success = false;
 		if (project.exists() && project.isOpen()) {
 
 			/*
 			 * IBaseWebNature webNature = J2EEWebNatureRuntimeUtilities.getRuntime(project); if
 			 * (webContentName == null) { if (webNature.isStatic()) { webContentName =
 			 * J2EEWebNatureRuntimeUtilities.getDefaultStaticWebContentName(); } else {
 			 * webContentName = J2EEWebNatureRuntimeUtilities.getDefaultJ2EEWebContentName(); } }
 			 */
 
 			IPath newPath = new Path(webContentName);
 			if (getModuleServerRoot(project).getProjectRelativePath().equals(newPath))
 				return false;
 			if (project.exists(newPath)) {
 				IStatus status = new Status(IStatus.ERROR, "org.eclipse.jst.j2ee", IStatus.OK, ProjectSupportResourceHandler.getString("Could_not_rename_____2", new Object[]{webContentName}), null); //$NON-NLS-1$ //$NON-NLS-2$	
 				throw new CoreException(status);
 			}
 
 			moveWebContentFolder(project, webContentName, progressMonitor);
 			updateWebContentNamePropertiesOnly(project, webContentName, progressMonitor);
 			success = true;
 		}
 		return success;
 	}
 
 	/**
 	 * Update the classpath entries and Server Root Name for this web project only.
 	 * 
 	 * @param project
 	 * @param webContentName
 	 * @return
 	 */
 	public static void updateWebContentNamePropertiesOnly(IProject project, String webContentName, IProgressMonitor progressMonitor) throws CoreException {
 		IPath newPath = new Path(webContentName);
 		if (getModuleServerRoot(project).equals(newPath))
 			return;
 
 		if (!getModuleServerRoot(project).equals(webContentName)) {
 
 			// if (webModuleArtifact.isJ2EE) {
 			// Update the library references
 			IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 
 			IClasspathEntry[] classpath = javaProject.getRawClasspath();
 			IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length];
 
 			for (int i = 0; i < classpath.length; i++) {
 				if (classpath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
 					IClasspathEntry library = classpath[i];
 					IPath libpath = library.getPath();
 					IPath modServerRootPath = getModuleServerRoot(project).getFullPath();
 					if (modServerRootPath.isPrefixOf(libpath)) {
 						IPath prunedPath = libpath.removeFirstSegments(modServerRootPath.segmentCount());
 						IPath relWebContentPath = new Path(webContentName + "/" + prunedPath.toString()); //$NON-NLS-1$
 						IResource absWebContentPath = project.getFile(relWebContentPath);
 
 						IPath srcAttachmentPath = library.getSourceAttachmentPath();
 						if (null != srcAttachmentPath) {
 							prunedPath = srcAttachmentPath.removeFirstSegments(modServerRootPath.segmentCount());
 						}
 						IResource absWebContentSrcAttachmentPath = project.getFile(relWebContentPath);
 
 						newClasspath[i] = JavaCore.newLibraryEntry(absWebContentPath.getFullPath(), absWebContentSrcAttachmentPath.getFullPath(), library.getSourceAttachmentRootPath(), library.isExported());
 
 					} else {
 						newClasspath[i] = classpath[i];
 					}
 
 				} else {
 					newClasspath[i] = classpath[i];
 				}
 				// }
 
 				// Set the java output folder
 				IFolder outputFolder = project.getFolder(getModuleServerRoot(project).getFullPath());
 				javaProject.setRawClasspath(newClasspath, outputFolder.getFullPath(), new SubProgressMonitor(progressMonitor, 1));
 			}
 			// update websettings
 			// TODO add to WebArtifactEdit
 			// webNature.setModuleServerRootName(webContentName);
 		}
 	}
 
 	/**
 	 * Moves the web content folder to the name indicated only if that path doesn't already exist in
 	 * the project.
 	 * 
 	 * @param project
 	 *            The web project to be updated.
 	 * @param webContentName
 	 *            The new web content name
 	 * @param progressMonitor
 	 *            Indicates progress
 	 * @throws CoreException
 	 *             The exception that occured during move operation
 	 */
 	public static void moveWebContentFolder(IProject project, String webContentName, IProgressMonitor progressMonitor) throws CoreException {
 		IPath newPath = new Path(webContentName);
 		if (!project.exists(newPath)) {
 			if (newPath.segmentCount() > 1) {
 				for (int i = newPath.segmentCount() - 1; i > 0; i--) {
 					IPath tempPath = newPath.removeLastSegments(i);
 					IFolder tempFolder = project.getFolder(tempPath);
 					if (!tempFolder.exists()) {
 						tempFolder.create(true, true, null);
 					}
 				}
 			}
 			newPath = project.getFullPath().append(newPath);
 			IContainer webContentRoot = getModuleServerRoot(project);
 			IPath oldPath = webContentRoot.getProjectRelativePath();
 			webContentRoot.move(newPath, IResource.FORCE | IResource.KEEP_HISTORY, new SubProgressMonitor(progressMonitor, 1));
 			for (int i = 0; i < oldPath.segmentCount(); i++) {
 				IPath tempPath = oldPath.removeLastSegments(i);
 				IFolder tempFolder = project.getFolder(tempPath);
 				if (tempFolder.exists() && tempFolder.members().length == 0) {
 					tempFolder.delete(true, true, null);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Synchonizies the class path and the lib directories to catch any changes from the last use
 	 * Creation date: (4/17/01 11:48:12 AM)
 	 */
 	protected static void synch(IProject project, IProgressMonitor monitor) {
 
 		try {
 			if (monitor == null) {
 				monitor = new NullProgressMonitor();
 			}
 			monitor.beginTask(ProjectSupportResourceHandler.getString("Sychronize_Class_Path_UI_"), 4); //$NON-NLS-1$
 			//$NON-NLS-1$ = "Sychronize Class Path"
 
 			IFolder lib_folder = getWebLibFolder(project);
 			// Nothing to do if the lib folder does not exist.
 			if (lib_folder == null || !lib_folder.isAccessible())
 				return;
 			IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 			IPath lib_path = lib_folder.getProjectRelativePath();
 			IPath lib_full_path = lib_folder.getFullPath();
 
 			IClasspathEntry[] cp = javaProject.getRawClasspath();
 
 			boolean needsToBeModified = false;
 			// Create a map of the lib projects in the current project
 			Hashtable lib_jars = new Hashtable();
 			IResource[] children = lib_folder.members();
 			monitor.subTask(ProjectSupportResourceHandler.getString("Catalog_Lib_Directory__UI_")); //$NON-NLS-1$
 			//$NON-NLS-1$ = "Catalog Lib Directory:"
 			for (int j = 0; j < children.length; j++) {
 				IResource child = children[j];
 				// monitor.setTaskName(ResourceHandler.getString("Catalog_Lib_Directory__UI_") +
 				// child); //$NON-NLS-1$ = "Catalog Lib Directory:"
 				// Make sure it is a zip or a jar file
 				if (child.getType() == IResource.FILE && (child.getFullPath().toString().toLowerCase().endsWith(".jar") //$NON-NLS-1$
 							|| child.getFullPath().toString().toLowerCase().endsWith(".zip"))) { //$NON-NLS-1$
 					lib_jars.put(child.getFullPath(), child);
 				}
 
 			}
 
 			monitor.worked(1);
 			monitor.subTask(ProjectSupportResourceHandler.getString("Update_ClassPath__UI_")); //$NON-NLS-1$
 			//$NON-NLS-1$ = "Update ClassPath:"
 			// Loop through all the classpath dirs looking for ones that may have
 			// been deleted
 			Vector newClassPathVector = new Vector();
 			for (int j = 0; j < cp.length; j++) {
 
 				// If it is a lib_path
 				if (cp[j].getPath().toString().startsWith(lib_path.toString()) || cp[j].getPath().toString().startsWith(lib_full_path.toString())) {
 					// It was already in the class path
 					if (lib_jars.get(cp[j].getPath()) != null) {
 						newClassPathVector.add(cp[j]);
 						// Remove it from the hash table of paths to add back
 						// monitor.setTaskName(ResourceHandler.getString("Catalog_Lib_Directory__UI_")
 						// + cp[j].getPath()); //$NON-NLS-1$ = "Catalog Lib Directory:"
 						lib_jars.remove(cp[j].getPath());
 
 					} else {
 						// You have removed something form the class path you
 						// will need to re-build
 						// monitor.setTaskName(ResourceHandler.getString("Catalog_Lib_Directory_Remo_UI_")
 						// + cp[j].getPath()); //$NON-NLS-1$ = "Catalog Lib Directory:Remove "
 						needsToBeModified = true;
 					}
 				} else {
 					monitor.subTask(ProjectSupportResourceHandler.getString("Catalog_Lib_Directory__UI_") + cp[j].getPath()); //$NON-NLS-1$
 					//$NON-NLS-1$ = "Catalog Lib Directory:"
 					newClassPathVector.add(cp[j]);
 				}
 			}
 			monitor.worked(1);
 			monitor.subTask(ProjectSupportResourceHandler.getString("Update_ClassPath__UI_")); //$NON-NLS-1$
 			//$NON-NLS-1$ = "Update ClassPath:"
 
 			// Add any entries not already found
 			Enumeration aenum = lib_jars.keys();
 			while (aenum.hasMoreElements()) {
 				IPath path = (IPath) aenum.nextElement();
 				newClassPathVector.add(JavaCore.newLibraryEntry(path, null, null));
 				// You have added something form the class path you
 				// will need to re-build
 				// monitor.setTaskName(ResourceHandler.getString("23concat_UI_", (new Object[] {
 				// path }))); //$NON-NLS-1$ = "Catalog Lib Directory:Add {0}"
 				needsToBeModified = true;
 			}
 
 			monitor.worked(1);
 			monitor.subTask(ProjectSupportResourceHandler.getString("Set_ClassPath__UI_")); //$NON-NLS-1$
 			//$NON-NLS-1$ = "Set ClassPath:"
 
 			// Tansfer the vector to an array
 			IClasspathEntry[] newClassPathArray = new IClasspathEntry[newClassPathVector.size()];
 
 			for (int j = 0; j < newClassPathArray.length; j++) {
 				newClassPathArray[j] = (IClasspathEntry) newClassPathVector.get(j);
 			}
 
 			// Only change the class path if there has been a modification
 			if (needsToBeModified) {
 
 				try {
 					javaProject.setRawClasspath(newClassPathArray, monitor);
 				} catch (Exception e) {
 					Logger.getLogger().log(e);
 				}
 			}
 
 		} catch (ClassCastException ex) {
 			Logger.getLogger().log(ex);
 		} catch (CoreException ex) {
 			Logger.getLogger().log(ex);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	public static void updateContextRoot(IProject project, String contextRoot) {
 		if (project.exists() && project.isOpen()) {
 			WebArtifactEdit webEdit = null;
 			try {
 				// TODO migrate to flex projects
 				// webEdit = (WebArtifactEdit) StructureEdit.getFirstArtifactEditForRead(project);
 				if (webEdit != null)
 					webEdit.setServerContextRoot(contextRoot);
 			} finally {
 				if (webEdit != null)
 					webEdit.dispose();
 			}
 
 		}
 	}
 
 
 	/**
 	 * @param project
 	 *            org.eclipse.core.resources.IProject
 	 */
 	/**
 	 * Returns a error message that states whether a context root is valid or not returns null if
 	 * context root is fine
 	 * 
 	 * @return java.lang.String
 	 * @param contextRoot
 	 *            java.lang.String
 	 */
 	public static String validateContextRoot(String contextRoot) {
 
 		if (contextRoot == null)
 			return null;
 
 		String errorMessage = null;
 
 		String name = contextRoot;
 		if (name.equals("") || name == null) { //$NON-NLS-1$
 			// this was added because the error message shouldnt be shown initially. It should be
 			// shown only if context root field is edited to
 			errorMessage = ProjectSupportResourceHandler.getString("Context_Root_cannot_be_empty_2"); //$NON-NLS-1$
 			return errorMessage;
 		}
 
 		/*******************************************************************************************
 		 * // JZ - fix to defect 204264, "/" is valid in context root if (name.indexOf("//") != -1) {
 		 * //$NON-NLS-1$ errorMessage = "// are invalid characters in a resource name"; return
 		 * errorMessage; }
 		 ******************************************************************************************/
 
 		if (name.trim().equals(name)) {
 			StringTokenizer stok = new StringTokenizer(name, "."); //$NON-NLS-1$
 			outer : while (stok.hasMoreTokens()) {
 				String token = stok.nextToken();
 				for (int i = 0; i < token.length(); i++) {
 					if (!(token.charAt(i) == '_') && !(token.charAt(i) == '-') && !(token.charAt(i) == '/') && Character.isLetterOrDigit(token.charAt(i)) == false) {
 						if (Character.isWhitespace(token.charAt(i))) {
 							// Removed because context roots can contain white space
 							// errorMessage =
 							// ResourceHandler.getString("_Context_root_cannot_conta_UI_");//$NON-NLS-1$
 							// = " Context root cannot contain whitespaces."
 						} else {
 							errorMessage = ProjectSupportResourceHandler.getString("The_character_is_invalid_in_a_context_root", new Object[]{(new Character(token.charAt(i))).toString()}); //$NON-NLS-1$
 							break outer;
 						}
 					}
 				}
 			}
 		} // en/ end of if(name.trim
 		else
 			errorMessage = ProjectSupportResourceHandler.getString("Names_cannot_begin_or_end_with_whitespace_5"); //$NON-NLS-1$
 
 		return errorMessage;
 	}
 
 
 	/**
 	 * Return true if the string contains any of the characters in the array.
 	 */
 	private static boolean contains(String str, char[] chars) {
 		for (int i = 0; i < chars.length; i++) {
 			if (str.indexOf(chars[i]) != -1)
 				return true;
 		}
 		return false;
 	}
 
 
 	public static String validateFolderName(String folderName) {
 		if (folderName.length() == 0)
 			return ProjectSupportResourceHandler.getString("Folder_name_cannot_be_empty_2"); //$NON-NLS-1$
 
 		if (contains(folderName, BAD_CHARS))
 			return ProjectSupportResourceHandler.getString("Folder_name_is_not_valid", new Object[]{folderName}); //$NON-NLS-1$
 
 		return null;
 	}
 
 
 	public static String validateWebContentName(String webContentName, IProject project, String javaSourceName) {
 
 		String msg = validateFolderName(webContentName);
 		if (msg != null)
 			return msg;
 
 		if (javaSourceName != null && webContentName.equals(javaSourceName))
 			return ProjectSupportResourceHandler.getString("Folder_names_cannot_be_equal_4"); //$NON-NLS-1$
 
 		// If given a java project, check to make sure current package fragment
 		// root folders do not overlap with new web content name
 		if (project != null) {
 			IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 			if (javaProject != null) {
 				try {
 					IPackageFragmentRoot roots[] = javaProject.getPackageFragmentRoots();
 					for (int i = 0; i < roots.length; i++) {
 						IPackageFragmentRoot root = roots[i];
 						if (!root.isArchive()) {
 							IResource resource = root.getCorrespondingResource();
 							if (resource.getType() == IResource.FOLDER) {
 								IPath path = resource.getFullPath();
 								String rootFolder = path.segment(1);
 								if (webContentName.equals(rootFolder)) {
 									if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
 										return ProjectSupportResourceHandler.getString("Folder_name_cannot_be_the_same_as_Java_source_folder_5"); //$NON-NLS-1$
 
 									return ProjectSupportResourceHandler.getString("Folder_name_cannot_be_the_same_as_Java_class_folder_6"); //$NON-NLS-1$
 								}
 							}
 						}
 					}
 				} catch (JavaModelException e) {
 					return null;
 				}
 			}
 		}
 
 		return null;
 	}
 
 
 	/**
 	 * Update given web nature to the current version if necessary.
 	 * 
 	 * @param webNature
 	 *            The web Nature that should be examined.
 	 * @return True if successful, false if unnecessary.
 	 * @throws CoreException
 	 *             The exception that occured during the version change operation.
 	 */
 	/*
 	 * static public boolean updateNatureToCurrentVersion(J2EEWebNatureRuntime webNature) throws
 	 * CoreException {
 	 * 
 	 * boolean success = false;
 	 * 
 	 * if (webNature.getVersion() != WEB.CURRENT_VERSION) {
 	 * webNature.setVersion(J2EESettings.CURRENT_VERSION); success = true; }
 	 * ((J2EEModuleWorkbenchURIConverterImpl)
 	 * webNature.getResourceSet().getURIConverter()).recomputeContainersIfNecessary();
 	 * 
 	 * return success; }
 	 */
 
 	/**
 	 * Move the old source folder to the new default folder.
 	 * 
 	 * @param project
 	 *            The Web Project we are working with.
 	 * @param oldSourceFolder
 	 *            The old "Java Source" folder that will be moved.
 	 * @param javaSourceName
 	 *            The new name of the "Java Source" folder, or null for default.
 	 * @return The location of the new folder, or null if no move was necessary.
 	 * @throws CoreException
 	 *             The exception that occured during the move operation.
 	 */
 	static public IContainer updateJavaSourceName(IProject project, IContainer oldSourceFolder, String javaSourceName, IProgressMonitor progressMonitor) throws CoreException {
 		IContainer newSourceFolder = null;
 		if (oldSourceFolder != null) {
 			IPath newPath;
 			if (javaSourceName == null)
 				newPath = new Path(DEFAULT_JAVA_SOURCE_NAME);
 			else
 				newPath = new Path(javaSourceName);
 
 			// Make sure new path is different form old path
 			if (!project.getFolder(newPath).getFullPath().equals(oldSourceFolder.getFullPath())) {
 				oldSourceFolder.move(newPath, IResource.FORCE | IResource.KEEP_HISTORY, new SubProgressMonitor(progressMonitor, 1));
 				JemProjectUtilities.removeFromJavaClassPath(project, oldSourceFolder);
 				newSourceFolder = project.getFolder(newPath);
 				JemProjectUtilities.appendJavaClassPath(project, JavaCore.newSourceEntry(project.getFolder(newPath).getFullPath()));
 			}
 		}
 		return newSourceFolder;
 	}
 
 
 	/**
 	 * Get the source folder that should be used for migration.
 	 * 
 	 * @param project
 	 *            The Web Project to examine.
 	 * @return The source folder to use in migration, or null if it should be skipped.
 	 */
 	static public IContainer getJavaSourceFolder(IProject project) {
 		List sourceRoots = JemProjectUtilities.getSourceContainers(project);
 		IContainer oldSourceFolder = null;
 
 		if (sourceRoots != null) {
 			if (sourceRoots.size() == 1) {
 				IContainer sourceFolder = (IContainer) sourceRoots.get(0);
 				if (sourceFolder instanceof IFolder) {
 					oldSourceFolder = sourceFolder;
 				}
 			}
 		}
 		return oldSourceFolder;
 	}
 
 	public static IFolder getModuleServerRoot(IProject project) {
 		// TODO need to implement module server root properly
 		return project.getFolder("WebContent");
 	}
 
 	public static IVirtualFolder getWebLibFolder(IVirtualComponent webComponent) {
		IPath path = new Path(J2EEConstants.WEB_INF + "/" + "lib");
 		IVirtualFolder libFolder = webComponent.getRootFolder().getFolder(path);
 		return libFolder;
 	}
 
 	//TODO delete jsholl
 	/**
 	 * @deprecated use getWebLibFolder(IVirtualComponent webComponent)
 	 * @param project
 	 * @return
 	 */
 	public static IFolder getWebLibFolder(IProject project) {
 		IFlexibleProject flex = ComponentCore.createFlexibleProject(project);
 		return getWebLibFolder(flex.getComponents()[0]).getUnderlyingFolder();
 	}
 
 	//	
 	// static public boolean isImportedClassesJARFileInLibDir(IResource resource) {
 	// if (resource == null || !resource.exists())
 	// return false;
 	// return resource.getType() == resource.FILE &&
 	// resource.getName().endsWith(IWebNatureConstants.IMPORTED_CLASSES_SUFFIX) && isZip(resource);
 	// }
 	//	
 	// static public boolean isLibDirJARFile(IResource resource) {
 	// if (resource == null || !resource.exists())
 	// return false;
 	// return resource.getType() == resource.FILE && isZip(resource);
 	// }
 	//	
 	// static public boolean isZip(IResource resource) {
 	// String path = resource.getLocation().toOSString();
 	// ZipFile zip = null;
 	//
 	// try {
 	// zip = new ZipFile(path);
 	// } catch (IOException notAZip) {
 	// return false;
 	// } finally {
 	// if (zip != null) {
 	// try {
 	// zip.close();
 	// } catch (IOException ex) {}
 	// }
 	// }
 	// return zip != null;
 	// }
 
 
 }
