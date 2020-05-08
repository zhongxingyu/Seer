 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Tobias Sodergren - initial API and implementation
  *******************************************************************************/
 package net.sourceforge.eclipseccase;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sourceforge.clearcase.ClearCase;
 import net.sourceforge.clearcase.ClearCaseElementState;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 
 /**
  * This class collects information about a collection of ClearCase elements.
  * <p>
  * The constructor takes an array of {@link IResource}s which, together with
  * their children, should have their clearcase status collected. The strategy is
  * to iterate through all provided resources and:
  * <ol>
  * <li>Ignore each resource that exists in a project that is not associated by
  * ClearCase</li>
  * <li>Get the status for the rest of the resources, iterate and:</li>
  * <ul>
  * <li>If the resource exists in ClearCase, add it as to the list of elements to
  * refresh.</li>
  * <li>If the status of a resource is "outside VOB", recurse through its
  * children to find linked resources that exists in ClearCase and stop the
  * recursion for that element.</li>
  * </ul>
  * <li>For each resource, find out if it exists in a snapshot or dynamic view.</li>
  * <li>For all dynamic views, perform the "cleartool lsprivate" command</li>
  * <li>For all resources in a snapshot view, perform "cleartools ls -view_only"
  * <li>For all views, perform "cleartool lscheckout" The result is a cache with
  * all view-private and checked out elements. The rest of the elements are
  * assumed to be checked in.
  * </ol>
  * 
  * <p>
  * Assumptions made by this class:
  * <ul>
  * <li>A project is associated with one ClearCase view.
  * <li>A project can contain linked resources that points to elements in one
  * view.
  * <li>A project can be associated with a dynamic or a snapshot view.
  * </ul>
  * 
  * @author Tobias Sodergren
  * 
  */
 public class ClearcaseElementStatusCollector {
 
 	private static final String TRACE_ID = ClearcaseElementStatusCollector.class
 			.getSimpleName();
 
 	private final Set<IResource> resourcesInClearcase = new HashSet<IResource>();
 
 	private Map<String, ClearCaseElementState> elementStates = new HashMap<String, ClearCaseElementState>();
 
 	public ClearcaseElementStatusCollector(IResource[] resources) {
 		populateClearcaseResources(resources);
 	}
 
 	private class RefreshSourceData {
 		private final IProject project;
 		private final String viewName;
 		private final boolean isSnapshot;
 		private final List<IResource> resources = new ArrayList<IResource>();
 
 		public RefreshSourceData(IProject project, IResource resource,
 				String viewName, boolean isSnapshot) {
 			this.project = project;
 			this.resources.add(resource);
 			this.viewName = viewName;
 			this.isSnapshot = isSnapshot;
 		}
 
 		public IProject getProject() {
 			return project;
 		}
 
 		public String getViewName() {
 			return viewName;
 		}
 
 		public boolean isSnapshot() {
 			return isSnapshot;
 		}
 
 		public IResource[] getResources() {
 			return resources.toArray(new IResource[resources.size()]);
 		}
 
 		public void addResource(IResource resource) {
 			resources.add(resource);
 		}
 	}
 
 	public void collectRefreshStatus(final IProgressMonitor progressMonitor) {
 		IProgressMonitor monitor = progressMonitor == null ? new NullProgressMonitor()
 				: progressMonitor;
 		monitor.beginTask("Collecting refresh status", resourcesInClearcase
 				.size() * 2);
 		// Find all involved projects and whether they contain dynamic or
 		// snapshot views
 		Map<IProject, RefreshSourceData> projects = new HashMap<IProject, RefreshSourceData>();
 		Iterator<IResource> resourceIterator = resourcesInClearcase.iterator();
 		while (resourceIterator.hasNext()) {
 			IResource resource = resourceIterator.next();
 			monitor.subTask(resource.getName());
 			IProject project = resource.getProject();
 			if (!projects.containsKey(project)) {
 				// A new project is detected, add project status
 				ClearcaseProvider clearcaseProvider = ClearcaseProvider
 						.getClearcaseProvider(resource);
 				boolean isSnapshotView;
 				isSnapshotView = clearcaseProvider.getViewType(resource)
 						.equals("snapshot");
 				projects.put(project,
 						new RefreshSourceData(project, resource,
 								clearcaseProvider.getViewName(resource),
 								isSnapshotView));
 			} else {
 				RefreshSourceData data = projects.get(project);
 				data.addResource(resource);
 			}
 			monitor.worked(1);
 
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 
 		}
 
 		// For each project, perform the dynamic or snapshot listing strategy
 		Iterator<IProject> projectIterator = projects.keySet().iterator();
 		Set<String> queriedViews = new HashSet<String>();
 		while (projectIterator.hasNext()) {
 			IProject project = projectIterator.next();
 			RefreshSourceData data = (RefreshSourceData) projects.get(project);
 			if (data.isSnapshot() == true) {
 				trace("Refreshing " + data.getViewName()
 						+ " using snapshot view functionality");
 				getSnapshotViewRefreshStatusForProject(data.getResources(),
 						data.getViewName(), queriedViews, monitor);
 			} else {
 				trace("Refreshing " + data.getViewName()
 						+ " using dynamic view functionality");
 				getDynamicViewRefreshStatusForProject(data.getResources(), data
 						.getViewName(), queriedViews, monitor);
 			}
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 
 		}
 
 		monitor.done();
 	}
 
 	private void getDynamicViewRefreshStatusForProject(IResource[] resources,
 			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {
 
 		if (!queriedViews.contains(viewName)) {
 			monitor.subTask("View private: " + viewName);
 
 			ClearCaseElementState[] viewLSPrivateList = ClearcasePlugin
 					.getEngine().getViewLSPrivateList(viewName, null);
 			if (null == viewLSPrivateList) {
 				throw new RuntimeException(
 						"Could not get view private file information from view: "
 								+ viewName);
 			}
 			for (int i = 0; i < viewLSPrivateList.length; i++) {
 				ClearCaseElementState element = viewLSPrivateList[i];
 				elementStates.put(element.element, element);
 			}
 			queriedViews.add(viewName);
 
 			addCheckedOutFiles(viewName, monitor, resources[0].getLocation());
 
 		}
 
 		monitor.worked(1);
 
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 	}
 
 	private void getSnapshotViewRefreshStatusForProject(IResource[] resources,
 			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {
 
 		List<IPath> paths = optimizeClearcaseOperationsForLinkedResources(resources);
 
 		for (IPath path : paths) {
 			String resourcePath = path.toOSString();
 			monitor.subTask("View private: " + resourcePath);
 			ClearCaseElementState[] viewLSViewOnlyList = ClearcasePlugin
 					.getEngine().getViewLSViewOnlyList(resourcePath, null);
 			for (int i = 0; i < viewLSViewOnlyList.length; i++) {
 				ClearCaseElementState element = viewLSViewOnlyList[i];
 				elementStates.put(element.element, element);
 			}
 
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 		}
 
 		if (!queriedViews.contains(viewName)) {
 			addCheckedOutFiles(viewName, monitor, paths.get(0));
 			queriedViews.add(viewName);
 		}
 
 		monitor.worked(1);
 
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 	}
 
 	private void addCheckedOutFiles(String viewName, IProgressMonitor monitor,
 			IPath path) {
 		String resourcePath = path.toOSString();
 		monitor.subTask("Checked out: " + viewName);
 		ClearCaseElementState[] checkedOut = ClearcasePlugin.getEngine()
 				.getCheckedOutElements(resourcePath, null);
 		for (int i = 0; i < checkedOut.length; i++) {
 			ClearCaseElementState element = checkedOut[i];
 			elementStates.put(element.element, element);
 		}
 	}
 
 	private List<IPath> optimizeClearcaseOperationsForLinkedResources(
 			IResource[] resources) {
 		Set<IPath> paths = new HashSet<IPath>();
 
 		boolean checkParent = ClearcasePlugin.isTestLinkedParentInClearCase();
 
 		// Use parent optimization when checking status for resources.
 		for (IResource resource : resources) {
 			if (checkParent) {
 				IPath parent = resource.getLocation().removeLastSegments(1);
 				ClearCaseElementState elementState = elementStates.get(parent
 						.toOSString());
 				if (elementState == null) {
 					elementState = ClearcasePlugin.getEngine().getElementState(
 							parent.toOSString());
 					elementStates.put(parent.toOSString(), elementState);
 				}
 				if (elementState.isElement()) {
 					paths.add(parent);
 				} else {
 					paths.add(resource.getLocation());
 				}
 			} else {
 				paths.add(resource.getLocation());
 
 			}
 		}
 
 		// Sort list to get a nicer progress output
 		List<IPath> sortedPaths = new ArrayList<IPath>(paths);
 		Collections.sort(sortedPaths, new Comparator<IPath>() {
 			public int compare(IPath left, IPath right) {
 				return left.toOSString().compareTo(right.toOSString());
 			}
 		});
 
 		return sortedPaths;
 	}
 
 	private void populateClearcaseResources(IResource[] resources) {
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			try {
 				resourcesInClearcase.addAll(findClearcaseResources(resource));
 			} catch (CoreException e) {
 				ClearcasePlugin.log(
 						"Could not get ClearCase information from: "
 								+ resource.getName(), e);
 			}
 		}
 
 	}
 
 	/**
 	 * Finds resources that are placed in a ClearCase VOB. If the provided
 	 * IResource is not part of a VOB, all linked resources are evaluated since
 	 * they can be linked to a ClearCase view. When such a linked resource is
 	 * found, its children will not be visited and the resource is added to the
 	 * resources to get status information from.
 	 * 
 	 * @param resource
 	 *            The resource to check.
 	 * @return A set of resources that should get their status updated.
 	 * @throws CoreException
 	 */
 	private Set<IResource> findClearcaseResources(IResource resource)
 			throws CoreException {
 		final Set<IResource> result = new HashSet<IResource>();
 
 		// Check the resource itself if it is in ClearCase.
 		if (isResourceInClearcase(resource)) {
 			result.add(resource);
 			// No point finding the children
 			return result;
 		}
 
 		// If not, find linked resources that links to elements in ClearCase
 		resource.accept(new IResourceVisitor() {
 			public boolean visit(IResource resource) throws CoreException {
 				String element = resource.getLocation().toOSString();
 				if (resource.getType() == IResource.FILE) {
 					return false;
 				}
 				// Only linked resources can be part of ClearCase
 				if (resource.isLinked()) {
 					if (isResourceInClearcase(resource)) {
 						// If the linked resource is part of ClearCase, add it.
 						result.add(resource);
 					}
 					// Stop looking any further
 					return false;
 				}
 				elementStates.put(element, new ClearCaseElementState(element,
 						ClearCase.OUTSIDE_VOB));
 
 				// Continue iterating the children to find linked resources.
 				return true;
 			}
 		});
 		return result;
 	}
 
 	/**
 	 * Checks whether the resource is placed in a VOB or not.
 	 * 
 	 * @param resource
 	 *            The resource to check.
 	 * @return True if the resource is placed in a ClearCase VOB, false
 	 *         otherwise.
 	 */
 	private boolean isResourceInClearcase(IResource resource) {
 		ClearCaseElementState state = ClearcasePlugin.getEngine()
 				.getElementState(resource.getLocation().toOSString());
 		if (state == null) {
 			return false;
 		}
 		return !state.isOutsideVob();
 	}
 
 	public ClearCaseElementState getElementState(StateCache stateCache) {
 		ClearCaseElementState state = elementStates.get(stateCache.getPath());
 		if (state != null) {
 			return state;
 		}
 
 		// Try the parent and see if it is marked as outside VOB
 		String path = stateCache.getPath();
 		String pathSeparator = "\\";
 		if (!path.contains(pathSeparator)) {
 			pathSeparator = "/";
 		}
 		String parentPath = path.substring(0, path.lastIndexOf(pathSeparator));
 		ClearCaseElementState parentState = elementStates.get(parentPath);
 		if (parentState != null && parentState.isOutsideVob()) {
 			return new ClearCaseElementState(stateCache.getPath(),
 					ClearCase.OUTSIDE_VOB);
 		}
 
 		return new ClearCaseElementState(stateCache.getPath(),
 				ClearCase.CHECKED_IN | ClearCase.IS_ELEMENT);
 	}
 
 	private void trace(String message) {
 		if (ClearcasePlugin.DEBUG_STATE_CACHE) {
 			ClearcasePlugin.trace(TRACE_ID, message);
 		}
 
 	}
 
 }
