 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.debug.ui.console;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.core.ProjectFragment;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class RubyConsoleSourceModuleLookup {
 
 	private final IDLTKSearchScope scope;
 
 	public RubyConsoleSourceModuleLookup(IDLTKLanguageToolkit toolkit) {
 		scope = SearchEngine.createWorkspaceScope(toolkit);
 	}
 
 	private IProject[] getAllProjects() {
 		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
 	}
 
 	private boolean checkScope(IProject project, IPath[] scopeProjectsAndZips) {
 		final IPath location = project.getFullPath();
 		for (int j = 0; j < scopeProjectsAndZips.length; j++) {
 			if (scopeProjectsAndZips[j].equals(location)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public ISourceModule findSourceModuleByLocalPath(final IPath path) {
 		final boolean isFullPath = EnvironmentPathUtils.isFull(path);
 		final IProject[] projects = getAllProjects();
 		final IPath[] enclosingProjectsAndZips = scope
 				.enclosingProjectsAndZips();
 		for (int i = 0, max = projects.length; i < max; i++) {
 			try {
 				final IProject project = projects[i];
 				if (!checkScope(project, enclosingProjectsAndZips)) {
 					continue;
 				}
 				if (!project.isAccessible()
 						|| !DLTKLanguageManager.hasScriptNature(project))
 					continue;
 
 				IScriptProject scriptProject = DLTKCore.create(project);
 				final ISourceModule module = findInProject(scriptProject, path,
 						isFullPath);
 				if (module != null) {
 					return module.exists() ? module : null;
 				}
 
 			} catch (CoreException e) {
 				// CoreException from hasNature - should not happen since we
 				// check that the project is accessible
 				// ModelException from getProjectFragments - a problem occurred
 				// while accessing project: nothing we can do, ignore
 			}
 		}
 		return null;
 	}
 
 	public static boolean isIncluded(IProjectFragment fragment, IPath path) {
		final ProjectFragment root = (ProjectFragment) fragment;
		return !Util.isExcluded(path, root.fullInclusionPatternChars(), root
				.fullExclusionPatternChars(), false);
 	}
 
 	private ISourceModule findInProject(IScriptProject scriptProject,
 			IPath path, boolean isFullPath) throws ModelException {
 		IProjectFragment[] roots = scriptProject.getProjectFragments();
 		for (int j = 0, rootCount = roots.length; j < rootCount; j++) {
 			final IProjectFragment root = roots[j];
 			IPath rootPath = root.getPath();
 			if (!isFullPath) {
 				rootPath = EnvironmentPathUtils.getLocalPath(rootPath);
 			}
 			if (rootPath.isPrefixOf(path) && isIncluded(root, path)) {
 				IPath localPath = path.setDevice(null).removeFirstSegments(
 						rootPath.segmentCount());
 				if (localPath.segmentCount() >= 1) {
 					final IScriptFolder folder;
 					if (localPath.segmentCount() > 1) {
 						folder = root.getScriptFolder(localPath
 								.removeLastSegments(1));
 					} else {
 						folder = root.getScriptFolder(Path.EMPTY);
 					}
 					return folder.getSourceModule(localPath.lastSegment());
 				}
 			}
 		}
 		return null;
 	}
 
 }
