 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.navigator;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 
 /**
  * Provides the labels for the Package Explorer.
  * <p>
  * It provides labels for the packages in hierarchical layout and in all
  * other cases delegates it to its super class.
  * </p>
 	 *
  */
 public class ScriptExplorerLabelProvider extends AppearanceAwareLabelProvider {
 	
 	private ScriptExplorerContentProvider fContentProvider;
 
 	private boolean fIsFlatLayout;
 	//private PackageExplorerProblemsDecorator fProblemDecorator;
 
 	public ScriptExplorerLabelProvider(long textFlags, int imageFlags, ScriptExplorerContentProvider cp, IPreferenceStore store) {
 		super(textFlags, imageFlags, store);
 		//fProblemDecorator= new PackageExplorerProblemsDecorator();
 		//addLabelDecorator(fProblemDecorator);
 		Assert.isNotNull(cp);
 		fContentProvider= cp;
 	}		
 
 
 	public String getText(Object element) {
 		
 		if (fIsFlatLayout || !(element instanceof IScriptFolder))
 			return super.getText(element);			
 
 		IScriptFolder fragment = (IScriptFolder) element;
 		
 		if (fragment.isRootFolder()) {
 			return super.getText(fragment);
 		} else{
 			Object parent= fContentProvider.getScriptFolderProvider().getParent(fragment);
 			if (parent instanceof IScriptFolder) {
 				return getNameDelta((IScriptFolder) parent, fragment);
 			} else if (parent instanceof IFolder) {
 				int prefixLength= getPrefixLength((IFolder) parent);
 				return fragment.getElementName().substring(prefixLength);
 			}
 			else return super.getText(fragment);
 		}
 	}
 	
 	private int getPrefixLength(IFolder folder) {
 		Object parent= fContentProvider.getParent(folder);
 		int folderNameLenght= folder.getName().length() + 1;
 		if(parent instanceof IScriptFolder) {
 			String fragmentName= ((IScriptFolder)parent).getElementName();
 			return fragmentName.length() + 1 + folderNameLenght;
 		} else if (parent instanceof IFolder) {
 			return getPrefixLength((IFolder)parent) + folderNameLenght;
 		} else {
 			return folderNameLenght;
 		}
 	}
 	
 	private String getNameDelta(IScriptFolder topFragment, IScriptFolder bottomFragment) {
 		
 		String topName= topFragment.getElementName();
 		String bottomName= bottomFragment.getElementName();
 		
 		if(topName.equals(bottomName))
 			return topName;
 		
		String deltaname= bottomName;
		if (deltaname.startsWith(topName) && bottomName.length() >= topName.length()+1) {
			deltaname= bottomName.substring(topName.length()+1);
		}
 		return deltaname;
 	}
 	
 	public void setIsFlatLayout(boolean state) {
 		fIsFlatLayout= state;
 		//fProblemDecorator.setIsFlatLayout(state);
 	}
 }
