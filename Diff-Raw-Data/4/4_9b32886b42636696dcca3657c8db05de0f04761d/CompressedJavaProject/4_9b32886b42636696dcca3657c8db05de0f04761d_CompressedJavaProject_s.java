 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.servlet.ui.internal.navigator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jst.servlet.ui.internal.plugin.ServletUIPlugin;
 import org.eclipse.jst.servlet.ui.internal.plugin.WEBUIMessages;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 
 public class CompressedJavaProject implements ICompressedNode {
  
 	
 	private IJavaProject project;
 	private CompressedJavaLibraries compressedLibraries;
 	private Image image; 
  
 	public CompressedJavaProject(StructuredViewer viewer, IJavaProject project) {
 		this.project = project; 
 
 	}
 
 	public Image getImage() {
 		if(image == null)
 			image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
 		return image;
 	}
 
 	public String getLabel() {
 		return determineLabel();
 
 	}
 
 	public boolean isFlatteningSourceFolder() {
 		return getNonExternalSourceFolders().size() == 1;
 	}
 
 	private String determineLabel() {
 		List nonextSourceFolders = getNonExternalSourceFolders();
 		IPackageFragmentRoot singleRoot = null;
 		if (nonextSourceFolders.size() == 1) {
 			singleRoot = (IPackageFragmentRoot) nonextSourceFolders.get(0);
 		}
 		return NLS.bind(WEBUIMessages.Compressed_JavaResources, ((singleRoot != null) ? ": " + singleRoot.getElementName() : "")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public IJavaProject getProject() {
 		return project;
 	}
 
 	public Object[] getChildren(ITreeContentProvider delegateContentProvider) {
 		
 		List nonExternalSourceFolders = getNonExternalSourceFolders();
 		if (nonExternalSourceFolders.size() == 1) {
 			Object[] sourceFolderChildren = delegateContentProvider.getChildren(nonExternalSourceFolders.get(0));
 			nonExternalSourceFolders.clear();
 			nonExternalSourceFolders.addAll(Arrays.asList(sourceFolderChildren));
 		} 
 		nonExternalSourceFolders.add(getCompressedJavaLibraries());
 		return nonExternalSourceFolders.toArray();
 	}
 
 	public List getNonExternalSourceFolders() {
 		List nonExternalSourceFolders = null;
 		IPackageFragmentRoot[] sourceFolders;
 		try {
 			sourceFolders = project.getPackageFragmentRoots();
 			nonExternalSourceFolders = new ArrayList(Arrays.asList(sourceFolders));
 			for (Iterator iter = nonExternalSourceFolders.iterator(); iter.hasNext();) {
 				IPackageFragmentRoot root = (IPackageFragmentRoot) iter.next();
 				if (root.isExternal() || root.isArchive())
 					iter.remove();
 			}
 		} catch (JavaModelException e) {
 			ServletUIPlugin.log(e);
 		}
		return nonExternalSourceFolders;
 	}
 	
 	public CompressedJavaLibraries getCompressedJavaLibraries() {		
 		if(compressedLibraries == null) 
 			compressedLibraries = new CompressedJavaLibraries(this);
 		return compressedLibraries;
 		
 	}
 
 }
