 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.search;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.BasicSearchEngine;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.MethodNameMatchRequestor;
 import org.eclipse.dltk.core.search.TypeNameRequestor;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.ProjectFragment;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 import org.eclipse.dltk.internal.core.util.HashtableOfArrayToObject;
 
 /**
  * Wrapper used to link {@link IRestrictedAccessTypeRequestor} with
  * {@link TypeNameRequestor}. This wrapper specifically allows usage of internal
  * method
  * {@link BasicSearchEngine#searchAllTypeNames(char[] packageName, int packageMatchRule, char[] typeName, int typeMatchRule, int searchFor, org.eclipse.jdt.core.search.IJavaSearchScope scope, IRestrictedAccessTypeRequestor nameRequestor, int waitingPolicy, org.eclipse.core.runtime.IProgressMonitor monitor) }
  * . from API method
  * {@link org.eclipse.jdt.core.search.SearchEngine#searchAllTypeNames(char[] packageName, int packageMatchRule, char[] typeName, int matchRule, int searchFor, org.eclipse.jdt.core.search.IJavaSearchScope scope, TypeNameRequestor nameRequestor, int waitingPolicy, org.eclipse.core.runtime.IProgressMonitor monitor) }
  * .
  */
 public class MethodNameMatchRequestorWrapper implements
 		IRestrictedAccessMethodRequestor {
 
 	MethodNameMatchRequestor requestor;
 	private IDLTKSearchScope scope; // scope is needed to retrieve project path
 	// for external resource
 	private HandleFactory handleFactory; // in case of IJavaSearchScope
 	// defined by clients, use an
 	// HandleFactory instead
 
 	/**
 	 * Cache package fragment root information to optimize speed performance.
 	 */
 	private String lastPkgFragmentRootPath;
 	private IProjectFragment lastProjectFragment;
 
 	/**
 	 * Cache package handles to optimize memory.
 	 */
 	private HashtableOfArrayToObject packageHandles;
 
 	public MethodNameMatchRequestorWrapper(MethodNameMatchRequestor requestor,
 			IDLTKSearchScope scope) {
 		this.requestor = requestor;
 		this.scope = scope;
 		if (!(scope instanceof DLTKSearchScope)) {
 			this.handleFactory = new HandleFactory();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor#
 	 * acceptType(int, char[], char[], char[][], java.lang.String,
 	 * org.eclipse.jdt.internal.compiler.env.AccessRestriction)
 	 */
 	public void acceptMethod(char[] simpleMethodName, String path) {
 		try {
 			IMethod method = null;
 			if (this.handleFactory != null) {
 				Openable openable = this.handleFactory.createOpenable(path,
 						this.scope);
 				if (openable == null)
 					return;
 				switch (openable.getElementType()) {
 				case IModelElement.SOURCE_MODULE:
 					ISourceModule cu = (ISourceModule) openable;
 					method = cu.getMethod(new String(simpleMethodName));
 					break;
 				}
 			} else {
 				int separatorIndex = path
 						.indexOf(IDLTKSearchScope.FILE_ENTRY_SEPARATOR);
 				method = separatorIndex == -1 ? createMethodFromPath(path,
 						new String(simpleMethodName)) : null/*
														 * createTypeFrom( path
														 * , separatorIndex )
														 */;
 				if (DLTKCore.DEBUG) {
 					System.err.println("TODO: Add types from zips..."); //$NON-NLS-1$
 				}
 			}
 			if (method != null) {
 				this.requestor
 						.acceptMethodNameMatch(new DLTKSearchMethodNameMatch(
 								method, method.getFlags()));
 			}
 		} catch (ModelException e) {
 			// skip
 		}
 	}
 
 	private IMethod createMethodFromPath(String resourcePath,
 			String simpleMethodName) throws ModelException {
 		// path to a file in a directory
 		// Optimization: cache package fragment root handle and package handles
 		int rootPathLength = -1;
 		if (this.lastPkgFragmentRootPath == null
 				|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath)
 						&& (rootPathLength = this.lastPkgFragmentRootPath
 								.length()) > 0 && resourcePath
 						.charAt(rootPathLength) == '/')) {
 			IProjectFragment root = ((DLTKSearchScope) this.scope)
 					.projectFragment(resourcePath);
 			if (root == null)
 				return null;
 			this.lastProjectFragment = root;
 			this.lastPkgFragmentRootPath = this.lastProjectFragment.getPath()
 					.toString();
 			this.packageHandles = new HashtableOfArrayToObject(5);
 		}
 		IPath resourcePath2 = new Path(resourcePath);
 		if (!resourcePath2.toString().startsWith(this.lastPkgFragmentRootPath)) {
 			return null;
 		}
 		// create handle
 		resourcePath = resourcePath.substring(this.lastPkgFragmentRootPath
 				.length() + 1);
 		String[] simpleNames = new Path(resourcePath).segments();
 		String[] pkgName;
 		int length = simpleNames.length - 1;
 		if (length > 0) {
 			pkgName = new String[length];
 			System.arraycopy(simpleNames, 0, pkgName, 0, length);
 		} else {
 			pkgName = CharOperation.NO_STRINGS;
 		}
 		IScriptFolder pkgFragment = (IScriptFolder) this.packageHandles
 				.get(pkgName);
 		if (pkgFragment == null) {
 			pkgFragment = ((ProjectFragment) this.lastProjectFragment)
 					.getScriptFolder(pkgName);
 			this.packageHandles.put(pkgName, pkgFragment);
 		}
 		String simpleName = simpleNames[length];
 		ISourceModule unit = pkgFragment.getSourceModule(simpleName);
 		if (unit == null) {
 			return null;
 		}
 		return findMethod(unit, simpleMethodName);
 	}
 
 	private IMethod findMethod(ISourceModule unit, String simpleMethodName)
 			throws ModelException {
 		final IModelElement[] elements = unit.getChildren();
 		for (int i = 0; i < elements.length; i++) {
 			if (elements[i].getElementType() == ISourceModule.METHOD) {
				IMethod method = (IMethod) elements[i];
				if (method.getElementName().equals(simpleMethodName)) {
					return method;
 				}
 			}
 		}
 		return null;
 	}
 }
