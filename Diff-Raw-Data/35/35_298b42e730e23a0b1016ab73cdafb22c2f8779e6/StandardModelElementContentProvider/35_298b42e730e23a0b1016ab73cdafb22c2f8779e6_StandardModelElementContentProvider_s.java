 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 
 /**
  * A base content provider for model elements. It provides access to the model
  * element hierarchy without listening to changes in the script model. If
  * updating the presentation on script model change is required than clients
  * have to subclass, listen to script model changes and have to update the UI
  * using corresponding methods provided by the JFace viewers or their own UI
  * presentation.
  * <p>
  * The following model element hierarchy is surfaced by this content provider:
  * <p>
  * 
  * <pre>
 Script model (<code>IScriptModel</code>)
    Script project (<code>IScriptProject</code>)
       package fragment root (<code>IProjectFragment</code>)
          package fragment (<code>IScriptFolder</code>)
             compilation unit (<code>ISourceModule</code>)
             binary class file (<code>IClassFile</code>)
  * </pre>
  * 
  * </p>
  * <p>
  * Note that when the entire script project is declared to be package fragment
  * root, the corresponding package fragment root element that normally appears
  * between the script project and the package fragments is automatically
  * filtered out.
  * </p>
  * 
 	 *
  */
 public class StandardModelElementContentProvider implements ITreeContentProvider, IWorkingCopyProvider {
 	protected static final Object[] NO_CHILDREN = new Object[0];
 	protected boolean fProvideMembers;
 	protected boolean fProvideWorkingCopy;
 
 	/**
 	 * Creates a new content provider. The content provider does not provide
 	 * members of compilation units or class files.
 	 */
 	public StandardModelElementContentProvider() {
 		this(false);
 	}
 
 	/**
 	 * Creates a new <code>StandardModelElementContentProvider</code>.
 	 * 
 	 * @param provideMembers
 	 *            if <code>true</code> members below compilation units and
 	 *            class files are provided.
 	 */
 	public StandardModelElementContentProvider(boolean provideMembers) {
 		fProvideMembers = provideMembers;
 		fProvideWorkingCopy = provideMembers;
 	}
 
 	/**
 	 * Returns whether members are provided when asking for a compilation units
 	 * or class file for its children.
 	 * 
 	 * @return <code>true</code> if the content provider provides members;
 	 *         otherwise <code>false</code> is returned
 	 */
 	public boolean getProvideMembers() {
 		return fProvideMembers;
 	}
 
 	/**
 	 * Sets whether the content provider is supposed to return members when
 	 * asking a compilation unit or class file for its children.
 	 * 
 	 * @param b
 	 *            if <code>true</code> then members are provided. If
 	 *            <code>false</code> compilation units and class files are the
 	 *            leaves provided by this content provider.
 	 */
 	public void setProvideMembers(boolean b) {
 		// hello
 		fProvideMembers = b;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IWorkingCopyProvider#providesWorkingCopies()
 	 */
 	public boolean providesWorkingCopies() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IStructuredContentProvider.
 	 */
 	public Object[] getElements(Object parent) {
 		return getChildren(parent);
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IContentProvider.
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 
 	/*
 	 * (non-Javadoc) Method declared on IContentProvider.
 	 */
 	public void dispose() {}
 
 	/*
 	 * (non-Javadoc) Method declared on ITreeContentProvider.
 	 */
 	public Object[] getChildren(Object element) {
 		if (!exists(element))
 			return NO_CHILDREN;
 		try {
 			if (element instanceof IScriptModel)
 				return getDLTKProjects((IScriptModel) element);
 			if (element instanceof IDLTKProject)
 				return getProjectFragments((IDLTKProject) element);
 			if (element instanceof IProjectFragment)
 				return getScriptFolders((IProjectFragment) element);
 			if (element instanceof IScriptFolder)
 				return getFolderContents((IScriptFolder) element);
 			if (element instanceof IFolder)
 				return getResources((IFolder) element);
 			if (getProvideMembers() && element instanceof ISourceReference && element instanceof IParent) {
 				return ((IParent) element).getChildren();
 			}
 		} catch (ModelException e) {
 			return NO_CHILDREN;
 		}
 		return NO_CHILDREN;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ITreeContentProvider
 	 */
 	public boolean hasChildren(Object element) {
 		if (getProvideMembers()) {
 			// assume CUs and class files are never empty
 			if (element instanceof ISourceModule) {
 				return true;
 			}
 		} else {
 			// don't allow to drill down into a compilation unit or class file
 			if (element instanceof ISourceModule || element instanceof IFile)
 				return false;
 		}
 		if (element instanceof IDLTKProject) {
 			IDLTKProject jp = (IDLTKProject) element;
 			if (!jp.getProject().isOpen()) {
 				return false;
 			}
 		}
 		if (element instanceof IParent) {
 			try {
 				// when we have children return true, else we fetch all the
 				// children
 				if (((IParent) element).hasChildren())
 					return true;
 			} catch (ModelException e) {
 				return true;
 			}
 		}
 		Object[] children = getChildren(element);
 		return (children != null) && children.length > 0;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on ITreeContentProvider.
 	 */
 	public Object getParent(Object element) {
 		if (!exists(element))
 			return null;
 		return internalGetParent(element);
 	}
 
 	private Object[] getScriptFolders(IProjectFragment root) throws ModelException {
 		IModelElement[] fragments = root.getChildren();
 		Object[] nonScriptResources = root.getForeignResources();
 		if (nonScriptResources == null)
 			return fragments;
 		return concatenate(fragments, nonScriptResources);
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected Object[] getProjectFragments(IDLTKProject project) throws ModelException {
 		if (!project.getProject().isOpen())
 			return NO_CHILDREN;
 		IProjectFragment[] roots = project.getProjectFragments();
 		List list = new ArrayList(roots.length);
 		// filter out package fragments that correspond to projects and
 		// replace them with the package fragments directly
 		for (int i = 0; i < roots.length; i++) {
 			IProjectFragment root = roots[i];
 			if (isProjectProjectFragment(root)) {
 				Object[] children = root.getChildren();
 				for (int k = 0; k < children.length; k++)
 					list.add(children[k]);
 			} else if (hasChildren(root)) {
 				list.add(root);
 			}
 		}
 		return concatenate(list.toArray(), project.getForeignResources());
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected Object[] getDLTKProjects(IScriptModel jm) throws ModelException {
 		return jm.getScriptProjects();
 	}
 
 	private Object[] getFolderContents(IScriptFolder fragment) throws ModelException {
 		// if (fragment.getKind() == IProjectFragment.K_SOURCE) {
 		return concatenate(fragment.getSourceModules(), fragment.getForeignResources());
 		// }
 		// return concatenate(fragment.getClassFiles(),
 		// fragment.getNonScriptResources());
 	}
 
 	private Object[] getResources(IFolder folder) {
 		try {
 			IResource[] members = folder.members();
 			IDLTKProject dltkProject = DLTKCore.create(folder.getProject());
 			if (dltkProject == null || !dltkProject.exists())
 				return members;
 			boolean isFolderOnBuildpath = dltkProject.isOnBuildpath(folder);
 			List nonScriptResources = new ArrayList();
 			// Can be on buildpath but as a member of non-java resource folder
 			for (int i = 0; i < members.length; i++) {
 				IResource member = members[i];
 				// A resource can also be a script element
 				// in the case of exclusion and inclusion filters.
 				// We therefore exclude model elements from the list
 				// of non-script resources.
 				if (isFolderOnBuildpath) {
 					if (dltkProject.findProjectFragment(member.getFullPath()) == null) {
 						nonScriptResources.add(member);
 					}
 				} else if (!dltkProject.isOnBuildpath(member)) {
 					nonScriptResources.add(member);
 				}
 			}
 			return nonScriptResources.toArray();
 		} catch (CoreException e) {
 			return NO_CHILDREN;
 		}
 	}	
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected boolean isBuildPathChange(IModelElementDelta delta) {
 		// need to test the flags only for package fragment roots
 		if (delta.getElement().getElementType() != IModelElement.PROJECT_FRAGMENT)
 			return false;
 		int flags = delta.getFlags();
 		return (delta.getKind() == IModelElementDelta.CHANGED && 
 			((flags & IModelElementDelta.F_ADDED_TO_BUILDPATH) != 0) || 
 			((flags & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) != 0) || 
 			((flags & IModelElementDelta.F_REORDER) != 0));
 	}	 
 	protected Object skipProjectProjectFragmment(IProjectFragment root) {
 		if (isProjectProjectFragment(root))
 			return root.getParent();
 		return root;
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected boolean isScriptFolderEmpty(IModelElement element) throws ModelException {
 		if (element instanceof IScriptFolder) {
 			IScriptFolder fragment = (IScriptFolder) element;
 			if (fragment.exists() && !(fragment.hasChildren() || fragment.getForeignResources().length > 0) && fragment.hasSubfolders())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected boolean isProjectProjectFragment(IProjectFragment root) {
		IResource resource = root.getResource();
		return (resource instanceof IProject);
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected boolean exists(Object element) {
 		if (element == null) {
 			return false;
 		}
 		if (element instanceof IResource) {
 			return ((IResource) element).exists();
 		}
 		if (element instanceof IModelElement) {
 			return ((IModelElement) element).exists();
 		}
 		return true;
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected Object internalGetParent(Object element) {
 		// try to map resources to the containing package fragment
 		if (element instanceof IResource) {
 			IResource parent = ((IResource) element).getParent();
 			IModelElement jParent = DLTKCore.create(parent);
 			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=31374
 			if (jParent != null && jParent.exists())
 				return jParent;
 			return parent;
 		} else if (element instanceof IModelElement) {
 			IModelElement parent = ((IModelElement) element).getParent();
 			// for package fragments that are contained in a project package
 			// fragment
 			// we have to skip the package fragment root as the parent.
 			if (element instanceof IScriptFolder) {
 				return skipProjectProjectFragmment((IProjectFragment) parent);
 			}
 			return parent;
 		}
 		return null;
 	}
 
 	/**
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 */
 	protected static Object[] concatenate(Object[] a1, Object[] a2) {
 		int a1Len = a1.length;
 		int a2Len = a2.length;
 		Object[] res = new Object[a1Len + a2Len];
 		System.arraycopy(a1, 0, res, 0, a1Len);
 		System.arraycopy(a2, 0, res, a1Len, a2Len);
 		return res;
 	}
 
 	protected Object[] getScriptProjects(IScriptModel jm) throws ModelException {
 		return jm.getScriptProjects();
 	}
 	protected Object skipProjectProjectFragment(IProjectFragment root) {
 		if (isProjectScriptFolder(root))
 			return root.getParent(); 
 		return root;
 	}
 	protected boolean isProjectScriptFolder(IProjectFragment root) {
 		IResource resource= root.getResource();
 		return (resource instanceof IProject);
 	}
 }
