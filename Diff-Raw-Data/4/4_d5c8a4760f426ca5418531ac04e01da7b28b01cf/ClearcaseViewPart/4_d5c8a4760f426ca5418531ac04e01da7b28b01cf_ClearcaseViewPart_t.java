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
 
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import net.sourceforge.eclipseccase.IResourceStateListener;
 import net.sourceforge.eclipseccase.StateCacheFactory;
 import net.sourceforge.eclipseccase.ui.ClearcaseUI;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.progress.IElementCollector;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 import org.eclipse.ui.views.navigator.ResourceSorter;
 
 /**
  * Base class for views showing ClearCase elements.
  * 
  * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
  */
 public abstract class ClearcaseViewPart extends ResourceNavigator implements
         IResourceStateListener, IResourceChangeListener {
 
     private ClearcaseContentProvider contentProvider;
 
     /**
      * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getContentProvider()
      */
     protected ClearcaseContentProvider getContentProvider() {
         if (contentProvider == null) {
             contentProvider = new ClearcaseContentProvider();
         }
         return contentProvider;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#initContentProvider(org.eclipse.jface.viewers.TreeViewer)
      */
     protected void initContentProvider(TreeViewer viewer) {
         viewer.setContentProvider(getContentProvider());
         StateCacheFactory.getInstance().addStateChangeListerer(this);
         ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#initLabelProvider(org.eclipse.jface.viewers.TreeViewer)
      */
     protected void initLabelProvider(TreeViewer viewer) {
         viewer.setLabelProvider(new DecoratingLabelProvider(
                 new ClearcaseViewLabelProvider(), getPlugin().getWorkbench()
                         .getDecoratorManager().getLabelDecorator()));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#setSorter(org.eclipse.ui.views.navigator.ResourceSorter)
      */
     public void setSorter(ResourceSorter sorter) {
         super.setSorter(new ResourceSorter(sorter.getCriteria()) {
 
             /*
              * (non-Javadoc)
              * 
              * @see org.eclipse.ui.views.navigator.ResourceSorter#compare(org.eclipse.jface.viewers.Viewer,
              *      java.lang.Object, java.lang.Object)
              */
             public int compare(Viewer viewer, Object o1, Object o2) {
                 //have to deal with non-resources in navigator
                 //if one or both objects are not resources, returned a
                 // comparison
                 //based on class.
                 if (!(o1 instanceof IResource && o2 instanceof IResource)) { return compareClass(
                         o1, o2); }
                 IResource r1 = (IResource) o1;
                 IResource r2 = (IResource) o2;
 
                 if (getCriteria() == NAME)
                     return compareNames(r1, r2);
                 else if (getCriteria() == TYPE)
                     return compareTypes(r1, r2);
                 else
                     return 0;
             }
 
             protected int compareNames(IResource resource1, IResource resource2) {
                 return collator.compare(resource1.getFullPath().toString(),
                         resource2.getFullPath().toString());
             }
         });
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#getInitialInput()
      */
     protected IAdaptable getInitialInput() {
         return getRoot();
     }
 
     private ClearcaseViewRoot myRoot;
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#getRoot()
      */
     protected ClearcaseViewRoot getRoot() {
         if (null == myRoot) {
             myRoot = new ClearcaseViewRoot() {
 
                 protected void collectElements(IProject[] clearcaseProjects,
                         IElementCollector collector, IProgressMonitor monitor) {
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
     protected void findResources(IProject[] clearcaseProjects,
             IElementCollector collector, IProgressMonitor monitor) {
         try {
             monitor.beginTask("Searching for resource",
                     clearcaseProjects.length * 100000);
             for (int i = 0; i < clearcaseProjects.length; i++) {
                 IProject project = clearcaseProjects[i];
                 monitor.subTask("Searching in project " + project.getName());
                 try {
                     findResources(project, collector, new SubProgressMonitor(
                             monitor, 100000,
                             SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                 } catch (CoreException ex) {
                     handleError(ex, "Error",
                             "An error occured while searching for resources in project "
                                     + project.getName() + ".");
                 }
 
                 if (monitor.isCanceled())
                         throw new OperationCanceledException();
             }
         } finally {
             monitor.done();
         }
 
     }
 
     protected void findResources(IResource resource,
             final IElementCollector collector, final IProgressMonitor monitor)
             throws CoreException {
         try {
             if (monitor.isCanceled()) throw new OperationCanceledException();
 
             // filter out ignored resources
             ClearcaseProvider provider = ClearcaseProvider
                     .getClearcaseProvider(resource);
             if (null == provider || provider.isIgnored(resource)) return;
 
             // determine children
             IResource[] children = (resource instanceof IContainer) ? ((IContainer) resource)
                     .members()
                     : new IResource[0];
 
             monitor.beginTask("processing", (children.length + 1) * 1000);
 
             // determine state
             if (shouldAdd(resource))
                     collector.add(resource, new SubProgressMonitor(monitor,
                             1000, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 
             // process state
             if (children.length > 0) {
                 for (int i = 0; i < children.length; i++) {
                     findResources(children[i], collector,
                             new SubProgressMonitor(monitor, 1000,
                                     SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                 }
             }
         } finally {
             monitor.done();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#setWorkingSet(org.eclipse.ui.IWorkingSet)
      */
     public void setWorkingSet(IWorkingSet workingSet) {
         getContentProvider().setWorkingSet(workingSet);
         super.setWorkingSet(workingSet);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#updateTitle()
      */
     public void updateTitle() {
         // do nothing
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#makeActions()
      */
     protected void makeActions() {
         setActionGroup(new ClearcaseViewActionGroup(this));
     }
 
     /**
      * Refreshes the viewer.
      */
     public void refresh() {
         if (getViewer() == null) return;
         getContentProvider().cancelJobs(getRoot());
         getViewer().refresh();
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
             } else if (t instanceof InterruptedException) {
                 return;
             } else {
                 status = new Status(IStatus.ERROR, ClearcaseUI.PLUGIN_ID, 1,
                         "An unknown exception occured: "
                                 + t.getLocalizedMessage(), t);
                 log = true;
                 dialog = true;
             }
         }
         if (status == null) return;
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
             if (dialog && getViewSite() != null
                     && getViewSite().getShell() != null) {
                 ErrorDialog.openError(getViewSite().getShell(), title, message,
                         toShow);
             }
             if (log || getViewSite() == null
                     || getViewSite().getShell() == null) {
                 ClearcasePlugin.log(toShow.getSeverity(), message, t);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#dispose()
      */
     public void dispose() {
         StateCacheFactory.getInstance().removeStateChangeListerer(this);
         ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
         super.dispose();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sourceforge.eclipseccase.IResourceStateListener#stateChanged(net.sourceforge.eclipseccase.StateCache)
      */
     public void resourceStateChanged(final IResource resource) {
         // filter out ignored resources
         ClearcaseProvider provider = ClearcaseProvider
                 .getClearcaseProvider(resource);
         if (null == provider || provider.isIgnored(resource)) return;
 
        // do not add non existent resources
        final boolean shouldAdd = resource.exists() && shouldAdd(resource);
        
         if (null != getViewer() && null != getViewer().getControl()
                 && !getViewer().getControl().isDisposed()) {
             getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
                 public void run() {
                     if (null != getViewer() && null != getViewer().getControl()
                             && !getViewer().getControl().isDisposed()) {
                         // we remove in every case
                         getViewer().remove(resource);
 
                         // only add if desired
                         if (shouldAdd) getViewer().add(getRoot(), resource);
                     }
                 }
             });
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
 
     protected static final CoreException IS_AFFECTED_EX = new CoreException(
             Status.CANCEL_STATUS);
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
      */
     public void resourceChanged(IResourceChangeEvent event) {
         IResourceDelta rootDelta = event.getDelta();
         if (null != rootDelta) {
             
             final Set toRemove = new HashSet();
             
             try {
                 rootDelta.accept(new IResourceDeltaVisitor() {
 
                     public boolean visit(IResourceDelta delta)
                             throws CoreException {
                         
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
                                         return null != ClearcaseProvider
                                                 .getClearcaseProvider(delta
                                                         .getResource());
                                 return true;
                             }
                             return false;
                         }
                     }
                 });
             } catch (CoreException ex) {
                 // refresh on exception
                 if (IS_AFFECTED_EX == ex) refresh();
             }
 
             if (null != getViewer() && null != getViewer().getControl()
                     && !getViewer().getControl().isDisposed()) {
                 getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
                     public void run() {
                         if (null != getViewer() && null != getViewer().getControl()
                                 && !getViewer().getControl().isDisposed()) {
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
      * @see org.eclipse.ui.views.navigator.ResourceNavigator#init(org.eclipse.ui.IViewSite,
      *      org.eclipse.ui.IMemento)
      */
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         super.init(site, memento);
         IWorkbenchSiteProgressService progressService = getProgressService();
         if(null != progressService) {
             progressService.showBusyForFamily(ClearcasePlugin.FAMILY_CLEARCASE_OPERATION);
         }
     }
 
     /**
      * Returns the IWorkbenchSiteProgressService for the receiver.
      * 
      * @return IWorkbenchSiteProgressService (maybe <code>null</code>)
      */
     protected IWorkbenchSiteProgressService getProgressService() {
         return (IWorkbenchSiteProgressService) getSite().getAdapter(
                 IWorkbenchSiteProgressService.class);
     }
 }
