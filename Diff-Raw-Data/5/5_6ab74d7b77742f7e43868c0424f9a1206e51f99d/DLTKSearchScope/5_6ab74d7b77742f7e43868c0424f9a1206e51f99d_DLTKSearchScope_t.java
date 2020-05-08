 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.search;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.compiler.env.AccessRuleSet;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.internal.core.Model;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ScriptFolder;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  * A Java-specific scope for searching relative to one or more script elements.
  */
 public class DLTKSearchScope extends AbstractSearchScope {
 	private ArrayList elements;
 
 	/*
 	 * The paths of the resources in this search scope (or the classpath
 	 * entries' paths if the resources are projects)
 	 */
 	private ArrayList projectPaths = new ArrayList(); // container paths
 	// projects
 	private int[] projectIndexes; // Indexes of projects in list
 	private String[] containerPaths; // path to the container (e.g. /P/src,
 	// /P/lib.jar, c:\temp\mylib.jar)
 	private String[] relativePaths; // path relative to the container (e.g.
 	// x/y/Z.class, x/y, (empty))
 	private boolean[] isPkgPath; // in the case of packages, matches must be
 	// direct children of the folder
 	protected AccessRuleSet[] pathRestrictions;
 	private int pathsCount;
 	private int threshold;
 
 	private IPath[] enclosingProjectsAndArchives;
 	protected final IDLTKLanguageToolkit toolkit;
 
 	public final static AccessRuleSet NOT_ENCLOSED = new AccessRuleSet(null,
 			null);
 
 	public DLTKSearchScope(IDLTKLanguageToolkit toolkit) {
 		this(toolkit, 5);
 	}
 
 	private DLTKSearchScope(IDLTKLanguageToolkit toolkit, int size) {
 		this.toolkit = toolkit;
 		initialize(size);
 
 		// disabled for now as this could be expensive
 		// ModelManager.getModelManager().rememberScope(this);
 	}
 
 	public IDLTKLanguageToolkit getLanguageToolkit() {
 		return toolkit;
 	}
 
 	private void addEnclosingProjectOrArchive(IPath path) {
 		int length = this.enclosingProjectsAndArchives.length;
 		for (int i = 0; i < length; i++) {
 			if (this.enclosingProjectsAndArchives[i].equals(path))
 				return;
 		}
 		System.arraycopy(this.enclosingProjectsAndArchives, 0,
 				this.enclosingProjectsAndArchives = new IPath[length + 1], 0,
 				length);
 		this.enclosingProjectsAndArchives[length] = path;
 	}
 
 	/**
 	 * Add script project all fragment roots to current script search scope.
 	 * 
 	 * @see #add(ScriptProject, IPath, int, HashSet, IClasspathEntry)
 	 */
 	public void add(ScriptProject project, int includeMask,
 			HashSet visitedProject) throws ModelException {
 		add(project, null, includeMask, visitedProject, null);
 	}
 
 	/**
 	 * Add a path to current script search scope or all project fragment roots
 	 * if null. Use project resolved classpath to retrieve and store access
 	 * restriction on each classpath entry. Recurse if dependent projects are
 	 * found.
 	 * 
 	 * @param scriptProject
 	 *            Project used to get resolved classpath entries
 	 * @param pathToAdd
 	 *            Path to add in case of single element or null if user want to
 	 *            add all project package fragment roots
 	 * @param includeMask
 	 *            Mask to apply on buildpath entries
 	 * @param visitedProjects
 	 *            Set to avoid infinite recursion
 	 * @param referringEntry
 	 *            Project raw entry in referring project buildpath
 	 * @throws ModelException
 	 *             May happen while getting script model info
 	 */
 	void add(ScriptProject scriptProject, IPath pathToAdd, int includeMask,
 			HashSet visitedProjects, IBuildpathEntry referringEntry)
 			throws ModelException {
 		if (!natureFilter(scriptProject)) {
 			return;
 		}
 		IProject project = scriptProject.getProject();
 		if (!project.isAccessible() || !visitedProjects.add(project))
 			return;
 
 		IPath projectPath = project.getFullPath();
 		String projectPathString = projectPath.toString();
 		this.addEnclosingProjectOrArchive(projectPath);
 
 		// Iterate via project fragments without buildpath entries
 		IProjectFragment[] fragments = scriptProject.getProjectFragments();
 		for (int i = 0; i < fragments.length; i++) {
 			if (fragments[i].getRawBuildpathEntry() == null) {
 				add(fragments[i]);
 			}
 		}
 
 		IBuildpathEntry[] entries = scriptProject.getResolvedBuildpath();
 		IScriptModel model = scriptProject.getModel();
 		ModelManager.PerProjectInfo perProjectInfo = scriptProject
 				.getPerProjectInfo();
 		for (int i = 0, length = entries.length; i < length; i++) {
 			IBuildpathEntry entry = entries[i];
 			AccessRuleSet access = null;
 			BuildpathEntry cpEntry = (BuildpathEntry) entry;
 			if (referringEntry != null) {
 				// Add only exported entries.
 				// Source folder are implicitly exported.
 				if (!entry.isExported()
 						&& entry.getEntryKind() != IBuildpathEntry.BPE_SOURCE)
 					continue;
 				cpEntry = cpEntry.combineWith((BuildpathEntry) referringEntry);
 				// cpEntry =
 				// ((BuildpathEntry)referringEntry).combineWith(cpEntry);
 			}
 			access = cpEntry.getAccessRuleSet();
 			switch (entry.getEntryKind()) {
 			case IBuildpathEntry.BPE_LIBRARY:
 				IBuildpathEntry rawEntry = null;
 				Map resolvedPathToRawEntries = perProjectInfo.resolvedPathToRawEntries;
 				if (resolvedPathToRawEntries != null) {
 					rawEntry = (IBuildpathEntry) resolvedPathToRawEntries
 							.get(entry.getPath());
 				}
 				if (rawEntry == null) {
 					break;
 				}
 				switch (rawEntry.getEntryKind()) {
 				case IBuildpathEntry.BPE_LIBRARY:
 					if ((includeMask & APPLICATION_LIBRARIES) != 0) {
 						IPath path = entry.getPath();
 						if (pathToAdd == null || pathToAdd.equals(path)) {
 							String pathToString = path.toString();
 							add(
 									projectPath.toString(),
 									"", pathToString, false/* not a package */, access); //$NON-NLS-1$
 							addEnclosingProjectOrArchive(path);
 						}
 					}
 					break;
 				case IBuildpathEntry.BPE_CONTAINER:
 					IBuildpathContainer container = DLTKCore
 							.getBuildpathContainer(rawEntry.getPath(),
 									scriptProject);
 					if (container == null)
 						break;
 					if ((container.getKind() == IBuildpathContainer.K_APPLICATION && (includeMask & APPLICATION_LIBRARIES) != 0)
 							|| (includeMask & SYSTEM_LIBRARIES) != 0) {
 						IPath path = entry.getPath();
 						if (pathToAdd == null || pathToAdd.equals(path)) {
 							String pathToString = path.toString();
 							add(
 									projectPath.toString(),
 									"", pathToString, false/* not a package */, access); //$NON-NLS-1$
 							addEnclosingProjectOrArchive(path);
 						}
 					}
 					break;
 				}
 				break;
 			case IBuildpathEntry.BPE_PROJECT:
 				if ((includeMask & REFERENCED_PROJECTS) != 0) {
 					IPath path = entry.getPath();
 					if (pathToAdd == null || pathToAdd.equals(path)) {
 						add((ScriptProject) model.getScriptProject(entry
 								.getPath().lastSegment()), null, includeMask,
 								visitedProjects, cpEntry);
 					}
 				}
 				break;
 			case IBuildpathEntry.BPE_SOURCE:
 				if ((includeMask & SOURCES) != 0) {
 					IPath path = entry.getPath();
 					if (pathToAdd == null || pathToAdd.equals(path)) {
 						add(projectPath.toString(), Util
 								.relativePath(path, 1/*
 													 * remove project segment
 													 */), projectPathString,
 								false/*
 									 * not a package
 									 */, access);
 					}
 				}
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Add an element to the script search scope.
 	 * 
 	 * @param element
 	 *            The element we want to add to current script search scope
 	 * @throws ModelException
 	 *             May happen if some Script Model info are not available
 	 */
 	public void add(IModelElement element) throws ModelException {
 		if (!natureFilter(element)) {
 			return;
 		}
 		IPath containerPath = null;
 		String containerPathToString = null;
 		int includeMask = SOURCES | APPLICATION_LIBRARIES | SYSTEM_LIBRARIES;
 		switch (element.getElementType()) {
 		case IModelElement.SCRIPT_MODEL:
 			// a workspace scope should be used
 			break;
 		case IModelElement.SCRIPT_PROJECT:
 			add((ScriptProject) element, null, includeMask, new HashSet(2),
 					null);
 			break;
 		case IModelElement.PROJECT_FRAGMENT:
 			IProjectFragment root = (IProjectFragment) element;
 			String projectPath = null;
 			if (!root.isExternal()) {
 				IPath rootPath = root.getPath();
 				containerPath = root.getKind() == IProjectFragment.K_SOURCE ? root
 						.getParent().getPath()
 						: rootPath;
 				containerPathToString = containerPath.toString();
 				IResource rootResource = root.getResource();
 				projectPath = root.getScriptProject().getPath().toString();
 				if (rootResource != null && rootResource.isAccessible()) {
 					String relativePath = Util.relativePath(rootResource
 							.getFullPath(), containerPath.segmentCount());
 					add(projectPath, relativePath, containerPathToString,
 							false/* not a package */, null);
 				} else {
 					add(projectPath,
 							org.eclipse.dltk.compiler.util.Util.EMPTY_STRING,
 							containerPathToString, false/* not a package */,
 							null);
 				}
 			} else {
 				projectPath = root.getScriptProject().getPath().toString();
 				containerPath = root.getPath();
 				containerPathToString = containerPath.toString();
 				add(projectPath,
 						org.eclipse.dltk.compiler.util.Util.EMPTY_STRING,
 						containerPathToString, false/* not a package */, null);
 			}
 			break;
 		case IModelElement.SCRIPT_FOLDER:
 			root = (IProjectFragment) element.getParent();
 			projectPath = root.getScriptProject().getPath().toString();
 			if (root.isArchive()) {
 				if (DLTKCore.DEBUG) {
 					System.err.println("TODO: Check. Bug possible..."); //$NON-NLS-1$
 				}
 				String relativePath = ((ScriptFolder) element).getPath()
 						.toString() + '/';
 				containerPath = root.getPath();
 				containerPathToString = containerPath.toString();
 				add(projectPath, relativePath, containerPathToString,
 						true/* package */, null);
 			} else {
 				IResource resource = element.getResource();
 				if (resource != null) {
 					if (resource.isAccessible()) {
 						containerPath = root.getKind() == IProjectFragment.K_SOURCE ? root
 								.getParent().getPath()
 								: root.getPath();
 					} else {
 						// for working copies, get resource container full path
 						containerPath = resource.getParent().getFullPath();
 					}
 					containerPathToString = containerPath.toString();
 					String relativePath = Util.relativePath(resource
 							.getFullPath(), containerPath.segmentCount());
 					add(projectPath, relativePath, containerPathToString,
 							true/* package */, null);
 				}
 			}
 			break;
 		default:
 			// remember sub-cu (or sub-class file) script elements
 			if (element instanceof IMember) {
 				if (this.elements == null) {
 					this.elements = new ArrayList();
 				}
 				this.elements.add(element);
 			}
 			root = (IProjectFragment) element
 					.getAncestor(IModelElement.PROJECT_FRAGMENT);
 			projectPath = root.getScriptProject().getPath().toString();
 			String relativePath;
 			if (root.getKind() == IProjectFragment.K_SOURCE) {
 				containerPath = root.getParent().getPath();
 				relativePath = Util
 						.relativePath(getPath(element, false/* full path */), 1/*
 																				 * remove
 																				 * project
 																				 * segmet
 																				 */);
 			} else {
 				containerPath = root.getPath();
 				relativePath = getPath(element, true/* relative path */)
 						.toString();
 			}
 			containerPathToString = containerPath.toString();
 			add(projectPath, relativePath, containerPathToString, false/*
 																		 * not a
 																		 * package
 																		 */,
 					null);
 		}
 
 		if (containerPath != null)
 			addEnclosingProjectOrArchive(containerPath);
 	}
 
 	private boolean natureFilter(IModelElement element) {
 		// For all projects scope
 		if (toolkit == null) {
 			return true;
 		}
 		IDLTKLanguageToolkit elementToolkit = DLTKLanguageManager
 				.getLanguageToolkit(element);
 		if (elementToolkit != null
 				&& elementToolkit.getNatureId().equals(toolkit.getNatureId())) {
 			// Filter by nature.
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the given path to this search scope. Remember if subfolders need to
 	 * be included and associated access restriction as well.
 	 */
 	private void add(String projectPath, String relativePath,
 			String containerPath, boolean isPackage, AccessRuleSet access) {
 		// normalize containerPath and relativePath
 		containerPath = normalize(containerPath);
 		relativePath = normalize(relativePath);
 		int length = this.containerPaths.length, index = (containerPath
 				.hashCode() & 0x7FFFFFFF)
 				% length;
 		String currentRelativePath, currentContainerPath;
 		while ((currentRelativePath = this.relativePaths[index]) != null
 				&& (currentContainerPath = this.containerPaths[index]) != null) {
 			if (currentRelativePath.equals(relativePath)
 					&& currentContainerPath.equals(containerPath))
 				return;
 			if (++index == length) {
 				index = 0;
 			}
 		}
 		int idx = this.projectPaths.indexOf(projectPath);
 		if (idx == -1) {
 			// store project in separated list to minimize memory footprint
 			this.projectPaths.add(projectPath);
 			idx = this.projectPaths.indexOf(projectPath);
 		}
 		this.projectIndexes[index] = idx;
 		this.relativePaths[index] = relativePath;
 		this.containerPaths[index] = containerPath;
 		this.isPkgPath[index] = isPackage;
 		if (this.pathRestrictions != null)
 			this.pathRestrictions[index] = access;
 		else if (access != null) {
 			this.pathRestrictions = new AccessRuleSet[this.relativePaths.length];
 			this.pathRestrictions[index] = access;
 		}
 
 		// assumes the threshold is never equal to the size of the table
 		if (++this.pathsCount > this.threshold)
 			rehash();
 	}
 
 	public boolean encloses(String resourcePathString) {
 		int separatorIndex = resourcePathString.indexOf(FILE_ENTRY_SEPARATOR);
 		if (separatorIndex != -1) {
 			// internal or external zip (case 3, 4, or 5)
 			String zipPath = resourcePathString.substring(0, separatorIndex);
 			String relativePath = resourcePathString
 					.substring(separatorIndex + 1);
 			return indexOf(zipPath, relativePath) >= 0;
 		}
 		// resource in workspace (case 1 or 2)
 		return indexOf(resourcePathString) >= 0;
 	}
 
 	/**
 	 * Returns paths list index of given path or -1 if not found. NOTE: Use
 	 * indexOf(String, String) for path inside archives
 	 * 
 	 * @param fullPath
 	 *            the full path of the resource, e.g. 1. /P/src/pkg/X.java 2.
 	 *            /P/src/pkg
 	 */
 	private int indexOf(String fullPath) {
 		// cannot guess the index of the container path
 		// fallback to sequentially looking at all known paths
 		for (int i = 0, length = this.relativePaths.length; i < length; i++) {
 			String currentRelativePath = this.relativePaths[i];
 			if (currentRelativePath == null)
 				continue;
 
 			String currentContainerPath = containerPaths[i];
 			String currentFullPath = currentRelativePath.length() == 0 ? currentContainerPath
 					: (currentContainerPath + '/' + currentRelativePath);
 			if (encloses(currentFullPath, fullPath, i))
 				return i;
 		}
 		return -1;
 	}
 
 	/**
 	 * Returns paths list index of given path or -1 if not found.
 	 * 
 	 * @param containerPath
 	 * @param relativePath
 	 *            the forward slash path relatively to the container, e.g. 1.
 	 *            x/y/Z.class 2. x/y 3. X.java 4. (empty)
 	 */
 	private int indexOf(String containerPath, String relativePath) {
 		// normalize containerPath and relativePath
 		containerPath = normalize(containerPath);
 		relativePath = normalize(relativePath);
 
 		// use the hash to get faster comparison
 		int length = this.containerPaths.length, index = (containerPath
 				.hashCode() & 0x7FFFFFFF)
 				% length;
 		String currentContainerPath;
 		while ((currentContainerPath = this.containerPaths[index]) != null) {
 			if (currentContainerPath.equals(containerPath)) {
 				String currentRelativePath = this.relativePaths[index];
 				if (encloses(currentRelativePath, relativePath, index))
 					return index;
 			}
 			if (++index == length) {
 				index = 0;
 			}
 		}
 		return -1;
 	}
 
 	/*
 	 * Returns whether the enclosing path encloses the given path (or is equal
 	 * to it)
 	 */
 	private boolean encloses(String enclosingPath, String path, int index) {
 		// normalize given path as it can come from outside
 		path = normalize(path); // new Path(normalize(path)).toString();
 		int pathLength = path.length();
 		int enclosingLength = enclosingPath.length();
 		if (pathLength < enclosingLength) {
 			return false;
 		}
 		if (enclosingLength == 0) {
 			return true;
 		}
 		if (pathLength == enclosingLength) {
 			return path.equals(enclosingPath);
 		}
 		if (!this.isPkgPath[index]) {
 			return path.startsWith(enclosingPath)
 					&& path.charAt(enclosingLength) == '/';
 		} else {
 			// if looking at a package, this scope encloses the given path
 			// if the given path is a direct child of the folder
 			// or if the given path path is the folder path (see bug 13919
 			// Declaration for package not found if scope is not project)
 			if (path.startsWith(enclosingPath)
 					&& ((enclosingPath.length() == path.lastIndexOf('/')) || (enclosingPath
 							.length() == path.length()))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IJavaSearchScope#encloses(IModelElement)
 	 */
 	public boolean encloses(IModelElement element) {
 		IDLTKLanguageToolkit elementToolkit = DLTKLanguageManager
 				.getLanguageToolkit(element);
 		if (!toolkit.getNatureId().equals(elementToolkit.getNatureId())) {
 			return false;
 		}
 
 		if (this.elements != null) {
 			for (int i = 0, length = this.elements.size(); i < length; i++) {
 				IModelElement scopeElement = (IModelElement) this.elements
 						.get(i);
 				IModelElement searchedElement = element;
 				while (searchedElement != null) {
 					if (searchedElement.equals(scopeElement))
 						return true;
 					searchedElement = searchedElement.getParent();
 				}
 			}
 			return false;
 		}
 		IProjectFragment root = (IProjectFragment) element
 				.getAncestor(IModelElement.PROJECT_FRAGMENT);
 		if (root != null && root.isArchive()) {
 			// external or internal archive
 			IPath rootPath = root.getPath();
 			String rootPathToString = rootPath.toString();
 			IPath relativePath = getPath(element, true/* relative path */);
 			return indexOf(rootPathToString, relativePath.toString()) >= 0;
 		}
 		// resource in workspace
 		String fullResourcePathString = getPath(element, false/* full path */)
 				.toString();
 		return indexOf(fullResourcePathString) >= 0;
 	}
 
 	public IPath[] enclosingProjectsAndZips() {
 		return this.enclosingProjectsAndArchives;
 	}
 
 	private IPath getPath(IModelElement element, boolean relativeToRoot) {
 		switch (element.getElementType()) {
 		case IModelElement.SCRIPT_MODEL:
 			return Path.EMPTY;
 		case IModelElement.SCRIPT_PROJECT:
 			return element.getPath();
 		case IModelElement.PROJECT_FRAGMENT:
 			if (relativeToRoot)
 				return Path.EMPTY;
 			return element.getPath();
 		case IModelElement.SCRIPT_FOLDER:
 			String relativePath = ((ScriptFolder) element).getRelativePath()
 					.toString() + '/';
 			return getPath(element.getParent(), relativeToRoot).append(
 					new Path(relativePath));
 		case IModelElement.SOURCE_MODULE:
 			return getPath(element.getParent(), relativeToRoot).append(
 					new Path(element.getElementName()));
 		default:
 			return getPath(element.getParent(), relativeToRoot);
 		}
 	}
 
 	/**
 	 * Get access rule set corresponding to a given path.
 	 * 
 	 * @param relativePath
 	 *            The path user want to have restriction access
 	 * @return The access rule set for given path or null if none is set for it.
 	 *         Returns specific uninit access rule set when scope does not
 	 *         enclose the given path.
 	 */
 	public AccessRuleSet getAccessRuleSet(String relativePath,
 			String containerPath) {
 		// if(
 		// containerPath.startsWith(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR))
 		// {
 		// containerPath = IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR +
 		// relativePath;
 		// }
 		int index = indexOf(containerPath, relativePath);
 		if (index == -1) {
 			// this search scope does not enclose given path
 			return NOT_ENCLOSED;
 		}
 		if (this.pathRestrictions == null)
 			return null;
 		return this.pathRestrictions[index];
 	}
 
 	protected void initialize(int size) {
 		this.pathsCount = 0;
 		this.threshold = size; // size represents the expected number of
 		// elements
 		int extraRoom = (int) (size * 1.75f);
 		if (this.threshold == extraRoom)
 			extraRoom++;
 		this.relativePaths = new String[extraRoom];
 		this.containerPaths = new String[extraRoom];
 		this.projectIndexes = new int[extraRoom];
 		this.isPkgPath = new boolean[extraRoom];
 		this.pathRestrictions = null; // null to optimize case where no access
 		// rules are used
 
 		this.enclosingProjectsAndArchives = new IPath[0];
 	}
 
 	/*
 	 * Removes trailing slashes from the given path
 	 */
 	private String normalize(String path) {
 		int pathLength = path.length();
 		int index = pathLength - 1;
 		while (index >= 0 && path.charAt(index) == '/')
 			index--;
 		if (index != pathLength - 1)
 			return path.substring(0, index + 1);
 		return path;
 	}
 
 	public void processDelta(IModelElementDelta delta) {
 		switch (delta.getKind()) {
 		case IModelElementDelta.CHANGED:
 			IModelElementDelta[] children = delta.getAffectedChildren();
 			for (int i = 0, length = children.length; i < length; i++) {
 				IModelElementDelta child = children[i];
 				this.processDelta(child);
 			}
 			break;
 		case IModelElementDelta.REMOVED:
 			IModelElement element = delta.getElement();
 			if (this.encloses(element)) {
 				if (this.elements != null) {
 					this.elements.remove(element);
 				}
 				IPath path = null;
 				switch (element.getElementType()) {
 				case IModelElement.SCRIPT_PROJECT:
 					path = ((IScriptProject) element).getProject()
 							.getFullPath();
 				case IModelElement.PROJECT_FRAGMENT:
 					if (path == null) {
 						path = ((IProjectFragment) element).getPath();
 					}
 					int toRemove = -1;
 					for (int i = 0; i < this.pathsCount; i++) {
 						if (this.relativePaths[i].equals(path.toString())) {
 							toRemove = i;
 							break;
 						}
 					}
 					if (toRemove != -1) {
 						this.relativePaths[toRemove] = null;
 						rehash();
 					}
 				}
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Returns the package fragment root corresponding to a given resource path.
 	 * 
 	 * @param resourcePathString
 	 *            path of expected package fragment root.
 	 * @return the {@link IProjectFragment package fragment root} which path
 	 *         match the given one or <code>null</code> if none was found.
 	 */
 	public IProjectFragment projectFragment(String resourcePathString) {
 		int index = -1;
 		int separatorIndex = resourcePathString.indexOf(FILE_ENTRY_SEPARATOR);
 		boolean isZIPFile = separatorIndex != -1;
 		boolean isBuiltin = resourcePathString
 				.startsWith(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR);
 		if (isZIPFile) {
 			// internal or external jar (case 3, 4, or 5)
 			String zipPath = resourcePathString.substring(0, separatorIndex);
 			String relativePath = resourcePathString
 					.substring(separatorIndex + 1);
 			index = indexOf(zipPath, relativePath);
 		} else {
 			// resource in workspace (case 1 or 2)
 			index = indexOf(resourcePathString);
 		}
 		if (index >= 0) {
 			int idx = projectIndexes[index];
 			String projectPath = idx == -1 ? null : (String) this.projectPaths
 					.get(idx);
 			if (projectPath != null) {
 				IScriptProject project = DLTKCore.create(ResourcesPlugin
 						.getWorkspace().getRoot().getProject(projectPath));
 				if (isZIPFile) {
 					return project
 							.getProjectFragment(this.containerPaths[index]);
 				}
 				if (isBuiltin) {
 					return project
 							.getProjectFragment(this.containerPaths[index]);
 				}
 				Object target = Model.getTarget(ResourcesPlugin.getWorkspace()
 						.getRoot(), Path
 						.fromPortableString(this.containerPaths[index] + '/'
 								+ this.relativePaths[index]), false);
 				if (target instanceof IProject) {
 					return project.getProjectFragment((IProject) target);
 				}
 				if (target instanceof IResource) {
 					IModelElement element = DLTKCore.create((IResource) target);
 					return (IProjectFragment) element
 							.getAncestor(IModelElement.PROJECT_FRAGMENT);
 				}
 				if (target instanceof IFileHandle) {
 					try {
 						IProjectFragment[] fragments = project
 								.getProjectFragments();
 						IFileHandle t = (IFileHandle) target;
						IPath absPath = t.getFullPath();
 						for (int i = 0; i < fragments.length; ++i) {
 							IProjectFragment f = fragments[i];
 							if (f instanceof ExternalProjectFragment) {
 								ExternalProjectFragment ep = (ExternalProjectFragment) f;
								IPath pPath = ep.getPath();
 								if (absPath.equals(pPath)) {
 									return f;
 								}
 							}
 						}
 					} catch (ModelException e) {
 						e.printStackTrace();
 						return null;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private void rehash() {
 		DLTKSearchScope newScope = new DLTKSearchScope(toolkit,
 				this.pathsCount * 2); // double the number of expected
 		// elements
 		newScope.projectPaths.ensureCapacity(this.projectPaths.size());
 		String currentPath;
 		for (int i = this.relativePaths.length; --i >= 0;)
 			if ((currentPath = this.relativePaths[i]) != null) {
 				int idx = this.projectIndexes[i];
 				String projectPath = idx == -1 ? null
 						: (String) this.projectPaths.get(idx);
 				newScope.add(projectPath, currentPath, this.containerPaths[i],
 						this.isPkgPath[i], this.pathRestrictions == null ? null
 								: this.pathRestrictions[i]);
 			}
 
 		this.relativePaths = newScope.relativePaths;
 		this.containerPaths = newScope.containerPaths;
 		this.projectPaths = newScope.projectPaths;
 		this.projectIndexes = newScope.projectIndexes;
 		this.isPkgPath = newScope.isPkgPath;
 		this.pathRestrictions = newScope.pathRestrictions;
 		this.threshold = newScope.threshold;
 	}
 
 	public String toString() {
 		StringBuffer result = new StringBuffer("JavaSearchScope on "); //$NON-NLS-1$
 		if (this.elements != null) {
 			result.append("["); //$NON-NLS-1$
 			for (int i = 0, length = this.elements.size(); i < length; i++) {
 				ModelElement element = (ModelElement) this.elements.get(i);
 				result.append("\n\t"); //$NON-NLS-1$
 				result.append(element.toStringWithAncestors());
 			}
 			result.append("\n]"); //$NON-NLS-1$
 		} else {
 			if (this.pathsCount == 0) {
 				result.append("[empty scope]"); //$NON-NLS-1$
 			} else {
 				result.append("["); //$NON-NLS-1$
 				for (int i = 0; i < this.relativePaths.length; i++) {
 					String path = this.relativePaths[i];
 					if (path == null)
 						continue;
 					result.append("\n\t"); //$NON-NLS-1$
 					result.append(this.containerPaths[i]);
 					if (path.length() > 0) {
 						result.append('/');
 						result.append(path);
 					}
 				}
 				result.append("\n]"); //$NON-NLS-1$
 			}
 		}
 		return result.toString();
 	}
 }
