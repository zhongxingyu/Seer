 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.archive.operations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.WARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.SaveFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveConstants;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.FileIterator;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.J2EEComponentSaveStrategyImpl;
 import org.eclipse.jst.j2ee.internal.plugin.LibCopyBuilder;
 import org.eclipse.jst.j2ee.web.datamodel.properties.IWebComponentImportDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 
 public class WebComponentSaveStrategyImpl extends J2EEComponentSaveStrategyImpl {
 
 	private HashMap filesToSave; // container a mapping from the File.getURI() to the target path
 	private HashSet filesNotToSave;
 
 	public WebComponentSaveStrategyImpl(IVirtualComponent vComponent) {
 		super(vComponent);
 	}
 
 	public void save(File aFile, FileIterator iterator) throws SaveFailureException {
 		if (aFile.isArchive() && operationHandlesNested((Archive) aFile)) {
 			return;
 		}
 
 		if (aFile.isArchive() && shouldIterateOver((Archive) aFile)) {
 			save((Archive) aFile);
 		} else {
 			InputStream in = null;
 			if (!aFile.isDirectoryEntry()) {
 				try {
 					in = iterator.getInputStream(aFile);
 				} catch (IOException ex) {
 					throw new SaveFailureException(aFile.getURI(), ex);
 				}
 			}
 			IPath path = (IPath) filesToSave.get(aFile.getURI());
 			if (path != null) {
 				try {
 					saveToWorkbenchPath(path, in);
 				} catch (Exception e) {
 					throw new SaveFailureException(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	protected boolean operationHandlesNested(Archive archive) {
 		if (null != dataModel) {
 			List list = (List) dataModel.getProperty(IWebComponentImportDataModelProperties.WEB_LIB_ARCHIVES_SELECTED);
 			return list.contains(archive);
 		}
 		return false;
 	}
 
 	protected void saveFiles() throws SaveFailureException {
 		WARFile warFile = (WARFile) getArchive();
 		// First go through the classes in the WEB-INF/classes directory and try
 		// to find a source file for each one file there. If a source file is
 		// found, copy
 		// it to the source directory, if not, copy the .class file to the
 		// imported_classes directory.
 		List classesFiles = getClassesFiles();
 		Iterator classesIterator = classesFiles.iterator();
 		filesToSave = new HashMap();
 		HashMap libModuleFiles = new HashMap();
 		boolean hasSource = false;
 		boolean hasClasses = false;
 		while (classesIterator.hasNext()) {
 			File classFile = (File) classesIterator.next();
 			if (WTProjectStrategyUtils.isClassFile(classFile.getURI())) {
 				hasClasses = true;
 				File sourceFile = warFile.getSourceFile(classFile);
 				IPath path = null;
 				if (sourceFile != null) {
 					hasSource = true;
 					// TODO can this be optimized?
 					IPath sourcePath = new Path(sourceFile.getURI());
 					IPath relClassPath = new Path(WTProjectStrategyUtils.makeRelative(classFile.getURI(), ArchiveConstants.WEBAPP_CLASSES_URI));
 					String relSourceURI = sourcePath.removeFirstSegments(sourcePath.segmentCount() - relClassPath.segmentCount()).toString();
 					path = convertToSourcePath(relSourceURI);
 					filesToSave.put(classFile.getURI(), null); // don't save the .class file
 					filesToSave.put(sourceFile.getURI(), path);
 				} else {
 					path = convertToImportedClassesPath(classFile.getURI());
 					filesToSave.put(classFile.getURI(), path);
 				}
 			}
 		}
 		// If there were no class files, then put any other resource in the
 		// classes
 		// directory to the source folder.
 		if (!hasClasses)
 			hasSource = true;
 		// Next go through the classes directory again saving all of the files
 		// that were
 		// not previously saved. This handles the resource files.
 		classesIterator = classesFiles.iterator();
 		while (classesIterator.hasNext()) {
 			File classFile = (File) classesIterator.next();
 			if (!filesToSave.containsKey(classFile.getURI())) {
 				File copyFile = warFile.getSourceFile(classFile);
 				// If its a java file, put it into the source directory
 				if (!hasSource && WTProjectStrategyUtils.isSourceFile(classFile.getURI()))
 					hasSource = true;
 				IPath path = null;
 				if (!hasSource) {
 					path = convertToImportedClassesPath(classFile.getURI());
 				} else {
 					IPath sourcePath = new Path(classFile.getURI());
 					IPath relClassPath = new Path(WTProjectStrategyUtils.makeRelative(classFile.getURI(), ArchiveConstants.WEBAPP_CLASSES_URI));
 					String relSourceURI = sourcePath.removeFirstSegments(sourcePath.segmentCount() - relClassPath.segmentCount()).toString();
 					path = convertToSourcePath(relSourceURI);
 				}
 				filesToSave.put(classFile.getURI(), path);
 				if (copyFile != null)
 					filesToSave.put(copyFile.getURI(), null);
 			}
 		}
 		// Finally, make a pass through all of the files now, saving them to the
 		// appropriate place
 		// if they have not yet been saved.
 		List allFiles = getArchive().getFiles();
 		for (Iterator iter = allFiles.iterator(); iter.hasNext();) {
 			File file = (File) iter.next();
 			if (!filesToSave.containsKey(file.getURI())) {
 				if (!libModuleFiles.containsKey(file.getURI())) {
 					IPath path = convertToContentPath(file.getURI());
 					filesToSave.put(file.getURI(), path);
 				}
 			}
 		}
 		super.saveFiles();
 	}
 
 	protected List getClassesFiles() {
 		return ((WARFile) getArchive()).getClasses();
 	}
 
 	private IPath importedClassesPath;
 
 	protected IPath convertToImportedClassesPath(String uri) {
 		if (importedClassesPath == null) {
 			IPath javaPath = getJavaSourcePath();
 			importedClassesPath = javaPath.removeLastSegments(1).append(LibCopyBuilder.IMPORTED_CLASSES_PATH);
 			try {
 				IPath workspacePath = vComponent.getProject().getFullPath().append(importedClassesPath);
 				mkdirs(workspacePath, ResourcesPlugin.getWorkspace().getRoot());
 				IVirtualFolder javaSourceFolder = vComponent.getRootFolder().getFolder(new Path("/" + J2EEConstants.WEB_INF + "/classes"));
 				javaSourceFolder.createLink(workspacePath.removeFirstSegments(1), 0, null);
 			} catch (CoreException e) {
 				// TODO
 			}
 
 		}
 		return importedClassesPath.append(WTProjectStrategyUtils.makeRelative(uri, ArchiveConstants.WEBAPP_CLASSES_URI));
 	}
 
 	private IPath javaSourcePath;
 
 	private void loadJavaSource() {
 		 IPackageFragmentRoot [] containers = ComponentUtilities.getSourceContainers(vComponent);
 		 javaSourcePath = containers[0].getPath();
 	}
 
 	protected IPath getJavaSourcePath() {
 		if (javaSourcePath == null) {
 			loadJavaSource();
 		}
 		return javaSourcePath;
 	}
 
 	protected IPath convertToSourcePath(String uri) {
 		return getJavaSourcePath().append(uri);
 	}
 
 	private IPath webContentPath;
 
 	protected IPath convertToContentPath(String uri) {
 		if (webContentPath == null) {
			webContentPath = vComponent.getRootFolder().getFolder("/").getUnderlyingResource().getFullPath();
 		}
 		return webContentPath.append(uri);
 	}
 }
