 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class ArchiveFolder extends ScriptFolder {
 	public ArchiveFolder(ProjectFragment parent, IPath path) {
 		super(parent, path);
 	}
 
 	void computeForeignResources(String[] resNames, ArchiveFolderInfo info,
 			String zipName) {
 		if (resNames == null) {
 			info.setForeignResources(null);
 			return;
 		}
 		int max = resNames.length;
 		if (max == 0) {
 			info.setForeignResources(ModelElementInfo.NO_NON_SCRIPT_RESOURCES);
 		} else {
 			Object[] res = new Object[max];
 			int index = 0;
 			for (int i = 0; i < max; i++) {
 				String resName = resNames[i];
 				if (!Util.isValidSourceModuleName(getScriptProject(), resName)) {
 					Path resPath = new Path(resName);
 					res[index++] = new ArchiveEntryFile(resPath.lastSegment(),
 							zipName, path, this.getProjectFragment()
 									.getResource(),
 							(ArchiveProjectFragment) parent);
 				}
 			}
 			if (index != max) {
 				System.arraycopy(res, 0, res = new Object[index], 0, index);
 			}
 			info.setForeignResources(res);
 		}
 	}
 
 	public ISourceModule getSourceModule(String name) {
 		ArchiveProjectFragment fragment = (ArchiveProjectFragment) getProjectFragment();
 		// Path zipPath = new Path(fragment.getZipName());
 		// IPath parentRelativePath = new Path(resName)
 		// .removeFirstSegments(this.path.segmentCount());
 		return new ExternalSourceModule(
 				this,
 				name,
 				DefaultWorkingCopyOwner.PRIMARY,
 				new ArchiveEntryFile(name, fragment.getZipName(), this.path,
 						fragment.getResource(), (ArchiveProjectFragment) parent));
 	}
 
 	protected boolean computeChildren(OpenableElementInfo info,
 			ArrayList entryNames) {
 		if (entryNames != null && entryNames.size() > 0) {
 			ArrayList vChildren = new ArrayList();
 			for (Iterator iter = entryNames.iterator(); iter.hasNext();) {
 				String child = (String) iter.next();
 				ISourceModule classFile = getSourceModule(child);
 				vChildren.add(classFile);
 			}
 			IModelElement[] children = new IModelElement[vChildren.size()];
 			vChildren.toArray(children);
 			info.setChildren(children);
 		} else {
 			info.setChildren(NO_ELEMENTS);
 		}
 		return true;
 	}
 
 	public ISourceModule[] getSourceModules() throws ModelException {
 		List<IModelElement> list = getChildrenOfType(SOURCE_MODULE);
 		ISourceModule[] array = new ISourceModule[list.size()];
 		list.toArray(array);
 		return array;
 	}
 
 	public boolean isReadOnly() {
 		return true;
 	}
 
 	// Open my archive: this creates all the pkg infos
 	protected void generateInfos(Object info, HashMap newElements,
 			IProgressMonitor pm) throws ModelException {
 		// Open my archive: this creates all the pkg infos
 		Openable openableParent = (Openable) this.parent;
 		if (!openableParent.isOpen()) {
 			openableParent.generateInfos(openableParent.createElementInfo(),
 					newElements, pm);
 		}
 	}
 
 	protected Object createElementInfo() {
 		return null; // not used for ArchiveFolders: info is created when
 		// archive is opened
 	}
 
 	public Object[] getForeignResources() throws ModelException {
 		return ((ArchiveFolderInfo) getElementInfo()).getForeignResources();
 	}
 
 }
