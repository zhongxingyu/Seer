 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *     Gunnar Wagenknecht - new features, enhancements and bug fixes
  *******************************************************************************/
 package net.sourceforge.eclipseccase.views;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashSet;
 import java.util.Set;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.ClearCaseUI;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.ui.*;
 import org.eclipse.ui.progress.IElementCollector;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 
 /**
  * Base class for views showing ClearCase elements.
  * 
  * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
  */
 public abstract class ClearCaseViewPart extends ResourceNavigator implements IResourceStateListener, IResourceChangeListener {
 
 	private ClearCaseContentProvider contentProvider;
 
 	protected boolean initialized = false;
 
 	/**
 	 * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getContentProvider()
 	 */
 	protected ClearCaseContentProvider getContentProvider() {
 		if (contentProvider == null) {
 			contentProvider = new ClearCaseContentProvider();
 		}
 		return contentProvider;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.views.navigator.ResourceNavigator#initContentProvider(
 	 * org.eclipse.jface.viewers.TreeViewer)
 	 */
 	@Override
 	protected void initContentProvider(TreeViewer viewer) {
 		viewer.setContentProvider(getContentProvider());
 		StateCacheFactory.getInstance().addStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.views.navigator.ResourceNavigator#initLabelProvider(org
 	 * .eclipse.jface.viewers.TreeViewer)
 	 */
 	@Override
 	protected void initLabelProvider(TreeViewer viewer) {
 		viewer.setLabelProvider(new DecoratingLabelProvider(new ClearCaseViewLabelProvider(), getPlugin().getWorkbench().getDecoratorManager().getLabelDecorator()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.views.navigator.ResourceNavigator#setSorter(org.eclipse
 	 * .ui.views.navigator.ResourceSorter)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void setComparator(ResourceComparator comparator) {
 		super.setComparator(new ResourceComparator(comparator.getCriteria()) {
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.ui.views.navigator.ResourceSorter#compare(org.eclipse
 			 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 			 */
 			@Override
 			public int compare(Viewer viewer, Object o1, Object o2) {
 				// have to deal with non-resources in navigator
 				// if one or both objects are not resources, returned a
 				// comparison
 				// based on class.
 				if (!(o1 instanceof IResource && o2 instanceof IResource))
 					return compareClass(o1, o2);
 				IResource r1 = (IResource) o1;
 				IResource r2 = (IResource) o2;
 
 				int typeres = compareClearCaseTypes(r1, r2);
 				if (typeres != 0) {
 					return typeres;
 				}
 				if (getCriteria() == NAME)
 					return compareNames(r1, r2);
 				else if (getCriteria() == TYPE)
 					return compareTypes(r1, r2);
 				else
 					return 0;
 			}
 
 			@Override
 			protected int compareNames(IResource resource1, IResource resource2) {
 				return getComparator().compare(resource1.getFullPath().toString(), resource2.getFullPath().toString());
 			}
 
 			protected int compareClearCaseTypes(IResource resource1, IResource resource2) {
 				// TODO: optimize this, how can the repeated lookups be
 				// eliminated?
 				StateCache c1 = StateCacheFactory.getInstance().get(resource1);
 				StateCache c2 = StateCacheFactory.getInstance().get(resource2);
 				// sort checkedout files first
 				if (c1.isCheckedOut()) {
 					if (c2.isCheckedOut()) {
 						return 0;
 					} else {
 						return -1;
 					}
 				} else if (c2.isCheckedOut()) {
 					return 1;
 				} else {
 					// no CO
 					// sort hijacked files second
 					if (c1.isHijacked()) {
 						if (c2.isHijacked()) {
 							return 0;
 						} else {
 							return -1;
 						}
 					} else if (c2.isHijacked()) {
 						return 1;
 					} else {
 						// both files are neither CO nor hijacked
 						return 0;
 					}
 				}
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.navigator.ResourceNavigator#getInitialInput()
 	 */
 	@Override
 	protected IAdaptable getInitialInput() {
 		return getRoot();
 	}
 
 	private ClearCaseViewRoot myRoot;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sourceforge.eclipseccase.views.ClearCaseViewPart#getRoot()
 	 */
 	protected ClearCaseViewRoot getRoot() {
 		if (null == myRoot) {
 			myRoot = new ClearCaseViewRoot() {
 
 				@Override
 				protected void collectElements(IProject[] clearcaseProjects, IElementCollector collector, IProgressMonitor monitor) {
 					findResources(clearcaseProjects, collector, monitor);
 				}
 			};
 		}
 
 		return myRoot;
 	}
 
 	/**
 	 * Finds all checkouts
 	 * 
 	 * @param collector
 	 * @param monitor
 	 */
 	protected void findResources(IProject[] clearcaseProjects, IElementCollector collector, IProgressMonitor monitor) {
 		try {
 			monitor.beginTask("Searching for resource", clearcaseProjects.length * 100000);
 			for (int i = 0; i < clearcaseProjects.length; i++) {
 				IProject project = clearcaseProjects[i];
 				monitor.subTask("Searching in project " + project.getName());
 				try {
 					findResources(project, collector, new SubProgressMonitor(monitor, 100000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 				} catch (CoreException ex) {
 					handleError(ex, "Error", "An error occured while searching for resources in project " + project.getName() + ".");
 				}
 
 				if (monitor.isCanceled())
 					throw new OperationCanceledException();
 			}
 		} finally {
 			monitor.done();
 		}
 
 	}
 
 	protected void findResources(IResource resource, final IElementCollector collector, final IProgressMonitor monitor) throws CoreException {
 		try {
 			if (monitor.isCanceled())
 				throw new OperationCanceledException();
 
 			// filter out ignored resources
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (null == provider)
 				return;
 
 			// determine children
 			IResource[] children = (resource instanceof IContainer) ? ((IContainer) resource).members() : new IResource[0];
 
 			monitor.beginTask("processing", (children.length + 1) * 1000);
 
 			// determine state
			if (!provider.isUnknownState(resource) && shouldAdd(resource)) {
 				collector.add(resource, new SubProgressMonitor(monitor, 1000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 			}
 
 			// process state
 			if (children.length > 0) {
 				for (int i = 0; i < children.length; i++) {
 					findResources(children[i], collector, new SubProgressMonitor(monitor, 1000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 				}
 			}
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.views.navigator.ResourceNavigator#setWorkingSet(org.eclipse
 	 * .ui.IWorkingSet)
 	 */
 	@Override
 	public void setWorkingSet(IWorkingSet workingSet) {
 		getContentProvider().setWorkingSet(workingSet);
 		super.setWorkingSet(workingSet);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.navigator.ResourceNavigator#updateTitle()
 	 */
 	@Override
 	public void updateTitle() {
 		// do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.navigator.ResourceNavigator#makeActions()
 	 */
 	@Override
 	protected void makeActions() {
 		setActionGroup(new ClearCaseViewActionGroup(this));
 	}
 
 	/**
 	 * Refreshes the viewer.
 	 */
 	public void refresh() {
 		if (getViewer() == null)
 			return;
 		getContentProvider().cancelJobs(getRoot());
 		getViewer().refresh();
 		initialized = true;
 	}
 
 	public void refreshInGuiThread() {
 		getViewer().getControl().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				refresh();
 			}
 		});
 	}
 
 	/**
 	 * Shows the given errors to the user.
 	 * 
 	 * @param Exception
 	 *            the exception containing the error
 	 * @param title
 	 *            the title of the error dialog
 	 * @param message
 	 *            the message for the error dialog
 	 * @param shell
 	 *            the shell to open the error dialog in
 	 */
 	public void handleError(Exception exception, String title, String message) {
 		IStatus status = null;
 		boolean log = false;
 		boolean dialog = false;
 		Throwable t = exception;
 		if (exception instanceof TeamException) {
 			status = ((TeamException) exception).getStatus();
 			log = false;
 			dialog = true;
 		} else if (exception instanceof InvocationTargetException) {
 			t = ((InvocationTargetException) exception).getTargetException();
 			if (t instanceof TeamException) {
 				status = ((TeamException) t).getStatus();
 				log = false;
 				dialog = true;
 			} else if (t instanceof CoreException) {
 				status = ((CoreException) t).getStatus();
 				log = true;
 				dialog = true;
 			} else if (t instanceof InterruptedException)
 				return;
 			else {
 				status = new Status(IStatus.ERROR, ClearCaseUI.PLUGIN_ID, 1, "An unknown exception occured: " + t.getLocalizedMessage(), t);
 				log = true;
 				dialog = true;
 			}
 		}
 		if (status == null)
 			return;
 		if (!status.isOK()) {
 			IStatus toShow = status;
 			if (status.isMultiStatus()) {
 				IStatus[] children = status.getChildren();
 				if (children.length == 1) {
 					toShow = children[0];
 				}
 			}
 			if (title == null) {
 				title = status.getMessage();
 			}
 			if (message == null) {
 				message = status.getMessage();
 			}
 			if (dialog && getViewSite() != null && getViewSite().getShell() != null) {
 				ErrorDialog.openError(getViewSite().getShell(), title, message, toShow);
 			}
 			if (log || getViewSite() == null || getViewSite().getShell() == null) {
 				ClearCasePlugin.log(toShow.getSeverity(), message, t);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.views.navigator.ResourceNavigator#dispose()
 	 */
 	@Override
 	public void dispose() {
 		StateCacheFactory.getInstance().removeStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.IResourceStateListener#stateChanged(net.
 	 * sourceforge.eclipseccase.StateCache)
 	 */
 	public void resourceStateChanged(final IResource[] resources) {
 		if (!initialized) {
 			refreshInGuiThread();
 			return;
 		}
 
 		for (int i = 0; i < resources.length; i++) {
 			final IResource resource = resources[i];
 
 			// filter out ignored resources
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 			if (null == provider || provider.isIgnored(resource))
 				return;
 
 			// do not add non existent resources
 			final boolean shouldAdd = resource.exists() && shouldAdd(resource);
 
 			if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 				getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
 					public void run() {
 						if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 							// we remove in every case
 							getViewer().remove(resource);
 
 							// only add if desired
 							if (shouldAdd) {
 								getViewer().add(getRoot(), resource);
 							}
 						}
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Indicates if the given resource should be shown in the viewer.
 	 * <p>
 	 * Ignored resources are already filtered out.
 	 * </p>
 	 * 
 	 * @param resource
 	 * @return <code>true</code> if the given resource should be shown in the
 	 *         viewer
 	 */
 	protected abstract boolean shouldAdd(IResource resource);
 
 	protected static final CoreException IS_AFFECTED_EX = new CoreException(Status.CANCEL_STATUS);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
 	 * .eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		if (!initialized) {
 			refreshInGuiThread();
 			return;
 		}
 		IResourceDelta rootDelta = event.getDelta();
 		if (null != rootDelta) {
 
 			final Set toRemove = new HashSet();
 
 			try {
 				rootDelta.accept(new IResourceDeltaVisitor() {
 
 					public boolean visit(IResourceDelta delta) throws CoreException {
 
 						switch (delta.getKind()) {
 
 						case IResourceDelta.ADDED:
 							// do nothing
 							return false;
 
 						case IResourceDelta.REMOVED:
 							toRemove.add(delta.getResource());
 							return true;
 
 						default:
 							IResource resource = delta.getResource();
 							if (null != resource) {
 								// filter out non clear case projects
 								if (resource.getType() == IResource.PROJECT)
 									return null != ClearCaseProvider.getClearCaseProvider(delta.getResource());
 								return true;
 							}
 							return false;
 						}
 					}
 				});
 			} catch (CoreException ex) {
 				// refresh on exception
 				if (IS_AFFECTED_EX == ex) {
 					refresh();
 				}
 			}
 
 			if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 				getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
 					public void run() {
 						if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 							// remove resources
 							getViewer().remove(toRemove.toArray());
 						}
 					}
 				});
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.views.navigator.ResourceNavigator#init(org.eclipse.ui.
 	 * IViewSite, org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		IWorkbenchSiteProgressService progressService = getProgressService();
 		if (null != progressService) {
 			progressService.showBusyForFamily(ClearCasePlugin.FAMILY_CLEARCASE_OPERATION);
 		}
 	}
 
 	/**
 	 * Returns the IWorkbenchSiteProgressService for the receiver.
 	 * 
 	 * @return IWorkbenchSiteProgressService (maybe <code>null</code>)
 	 */
 	protected IWorkbenchSiteProgressService getProgressService() {
 		return (IWorkbenchSiteProgressService) getSite().getAdapter(IWorkbenchSiteProgressService.class);
 	}
 
 	protected void handleDoubleClick(DoubleClickEvent event) {
 		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 		Object element = selection.getFirstElement();
 
 		TreeViewer viewer = getTreeViewer();
 		if (viewer.isExpandable(element)) {
 			viewer.setExpandedState(element, !viewer.getExpandedState(element));
 		} else if (selection.size() == 1 && (element instanceof IResource)) {
 			// OpenFileAction ofa = new OpenFileAction(getSite().getPage());
 			// ofa.selectionChanged((IStructuredSelection)
 			// viewer.getSelection());
 			// if (ofa.isEnabled()) {
 			// ofa.run();
 			// }
 		}
 
 	}
 }
