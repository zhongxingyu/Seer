 package net.sourceforge.eclipseccase.views;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import net.sourceforge.clearcase.simple.ClearcaseUtil;
 import net.sourceforge.clearcase.simple.IClearcase;
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import net.sourceforge.eclipseccase.StateCache;
 import net.sourceforge.eclipseccase.StateCacheFactory;
 import net.sourceforge.eclipseccase.StateChangeListener;
 import net.sourceforge.eclipseccase.ui.ClearcaseImages;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.operation.ModalContext;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * The Checkouts view
  */
 public class CheckoutsView extends ViewPart implements StateChangeListener
 {
     TableViewer viewer;
 
     Action refreshAction;
 
     Collection checkouts = Collections.synchronizedSortedSet(new TreeSet(new Comparator()
     {
         public int compare(Object o1, Object o2)
         {
             IResource r1 = (IResource) o1;
             IResource r2 = (IResource) o2;
             /*
              * // Sorts by folder first - I don't like it - leave
              * commented till I figure out how to sort by column
              * boolean isFolder1 = r1.getType() != IResource.FILE;
              * boolean isFolder2 = r2.getType() != IResource.FILE; if
              * (isFolder1 && ! isFolder2) return -1; else if (!
              * isFolder1 && isFolder2) return 1; else
              */
             return r1.getFullPath().toString().compareTo(r2.getFullPath().toString());
         }
     }));
 
     static final CoreException FIND_NEEDED_EXCEPTION =
         new CoreException(new Status(IStatus.OK, "findNeeded", 1, "", null));
 
     private static class ProjectChangedDeltaVisitor implements IResourceDeltaVisitor
     {
         public boolean visit(IResourceDelta delta) throws CoreException
         {
             IResource resource = delta.getResource();
             switch (resource.getType())
             {
                 case IResource.ROOT :
                     return true;
                 case IResource.PROJECT :
                     {
                         switch (delta.getKind())
                         {
                             // Can't get os string for resource after deleted,
                             // so do a refresh for all project deletions
                             case IResourceDelta.REMOVED :
                                 throw FIND_NEEDED_EXCEPTION;
                                 // Don't refresh on add, only when description
                                 // changes (i.e. associated with ccase) and when
                                 // openeing/closing
                             case IResourceDelta.CHANGED :
                                 if ((delta.getFlags()
                                     & (IResourceDelta.OPEN | IResourceDelta.DESCRIPTION))
                                     != 0
                                     && ClearcasePlugin.getEngine().isElement(
                                         resource.getLocation().toOSString()))
                                 {
                                     throw FIND_NEEDED_EXCEPTION;
                                 }
                                 break;
                         }
                     }
                     return false;
             }
             return false;
         }
     }
 
     // Listener to find checkouts if project is added/removed/opened/losed
     private final IResourceChangeListener updateListener = new IResourceChangeListener()
     {
         public void resourceChanged(IResourceChangeEvent event)
         {
             if (event.getType() != IResourceChangeEvent.POST_CHANGE)
                 return;
 
             ProjectChangedDeltaVisitor visitor = new ProjectChangedDeltaVisitor();
             try
             {
                 event.getDelta().accept(visitor);
             }
             catch (CoreException e)
             {
                 if (e == FIND_NEEDED_EXCEPTION)
                 {
                     Display.getDefault().asyncExec(new Runnable()
                     {
                         public void run()
                         {
                             refreshAction.run();
                         }
                     });
                 }
                 else
                 {
                     ClearcasePlugin.log(
                         IStatus.ERROR,
                         "Unable to do a quick update of resource",
                         null);
                 }
             }
         }
     };
 
     private final class DoubleClickListener implements IDoubleClickListener
     {
         public void doubleClick(DoubleClickEvent event)
         {
             if (event.getSelection() instanceof IStructuredSelection)
             {
                 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                 for (Iterator iter = selection.iterator(); iter.hasNext();)
                 {
                     IResource element = (IResource) iter.next();
                     if (element.getType() == IResource.FILE)
                     {
                         try
                         {
                             // limit to 2.1 AND 3.0 compatible implementation
                             IFile file = (IFile) element;
                             IWorkbench workbench = PlatformUI.getWorkbench();
                             IEditorDescriptor desc =
                                 workbench.getEditorRegistry().getDefaultEditor(file.getName());
                             if (desc != null)
                             {
                                 PlatformUI
                                     .getWorkbench()
                                     .getActiveWorkbenchWindow()
                                     .getActivePage()
                                     .openEditor(
                                     new FileEditorInput(file),
                                     desc.getId(),
                                     true);
                             }
                         }
                         catch (PartInitException e)
                         {
                             ClearcasePlugin.log(
                                 IStatus.ERROR,
                                 "Could not create editor for " + element,
                                 e);
                         }
                     }
 
                 }
             }
         }
     }
 
     private final class ViewContentProvider implements IStructuredContentProvider
     {
         public void inputChanged(Viewer v, Object oldInput, Object newInput)
         {
             // ignore
         }
 
         public void dispose()
         {
             // ignore
         }
 
         public Object[] getElements(Object parent)
         {
             return (IResource[]) checkouts.toArray(new IResource[checkouts.size()]);
         }
     }
 
     private final class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
     {
         public String getColumnText(Object obj, int index)
         {
             return ((IResource) obj).getFullPath().toString();
         }
 
         public Image getColumnImage(Object obj, int index)
         {
             return getImage(obj);
         }
 
         public Image getImage(Object obj)
         {
             Image image = null;
             IResource resource = (IResource) obj;
             switch (resource.getType())
             {
                 case IResource.FILE :
                     image =
                         PlatformUI.getWorkbench().getSharedImages().getImage(
                             ISharedImages.IMG_OBJ_FILE);
                     break;
                 case IResource.FOLDER :
                     image =
                         PlatformUI.getWorkbench().getSharedImages().getImage(
                             ISharedImages.IMG_OBJ_FOLDER);
                     break;
                 case IResource.PROJECT :
                     image =
                         PlatformUI.getWorkbench().getSharedImages().getImage(
                            IDE.SharedImages.IMG_OBJ_PROJECT);
                     break;
                 default :
                     image =
                         PlatformUI.getWorkbench().getSharedImages().getImage(
                             ISharedImages.IMG_OBJ_ELEMENT);
                     break;
             }
             return image;
         }
     }
 
     /**
      * The constructor.
      */
     public CheckoutsView()
     {
         StateCacheFactory.getInstance().addStateChangeListerer(this);
     }
 
     /**
      * @see org.eclipse.ui.IWorkbenchPart#dispose()
      */
     public void dispose()
     {
         StateCacheFactory.getInstance().removeStateChangeListerer(this);
         ClearcasePlugin.getWorkspace().removeResourceChangeListener(updateListener);
         super.dispose();
     }
 
     /**
      * This is a callback that will allow us to create the viewer and
      * initialize it.
      */
     public void createPartControl(Composite parent)
     {
         viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
         viewer.setContentProvider(new ViewContentProvider());
         viewer.setLabelProvider(new ViewLabelProvider());
         viewer.setInput(ResourcesPlugin.getWorkspace());
         viewer.addDoubleClickListener(new DoubleClickListener());
         makeActions();
         hookContextMenu();
         contributeToActionBars();
 
         refreshAction.run();
         ClearcasePlugin.getWorkspace().addResourceChangeListener(
             updateListener,
             IResourceChangeEvent.POST_CHANGE);
 
     }
 
     private void hookContextMenu()
     {
         MenuManager menuMgr = new MenuManager("#PopupMenu");
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener()
         {
             public void menuAboutToShow(IMenuManager manager)
             {
                 // Other plug-ins can contribute there actions here
                 manager.add(new Separator("Additions"));
             }
         });
         Menu menu = menuMgr.createContextMenu(viewer.getControl());
         viewer.getControl().setMenu(menu);
         getSite().registerContextMenu(menuMgr, viewer);
     }
 
     private void contributeToActionBars()
     {
         IActionBars bars = getViewSite().getActionBars();
         fillLocalToolBar(bars.getToolBarManager());
     }
 
     private void fillLocalToolBar(IToolBarManager manager)
     {
         manager.add(refreshAction);
     }
 
     private void makeActions()
     {
         refreshAction = new Action()
         {
             public void run()
             {
                 IStatusLineManager statusLineManager =
                     getViewSite().getActionBars().getStatusLineManager();
                 statusLineManager.setCancelEnabled(true);
                 final IProgressMonitor progressMonitor = statusLineManager.getProgressMonitor();
 
                 final IRunnableWithProgress op = new IRunnableWithProgress()
                 {
                     public void run(IProgressMonitor monitor)
                         throws InvocationTargetException, InterruptedException
                     {
                         findCheckouts(monitor);
                     }
                 };
 
                 try
                 {
                     ModalContext.run(op, true, progressMonitor, Display.getCurrent());
                 }
                 catch (InvocationTargetException e)
                 {
                     showError(e.getTargetException().toString());
                 }
                 catch (InterruptedException e)
                 {
                     // ignore
                 }
             }
         };
         refreshAction.setText("Refresh");
         refreshAction.setToolTipText("Refreshes the list of checked out files");
         refreshAction.setImageDescriptor(
             ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_REFRESH));
     }
 
     void showMessage(String message)
     {
         MessageDialog.openInformation(viewer.getControl().getShell(), "Checkouts View", message);
     }
 
     void showError(String message)
     {
         MessageDialog.openError(viewer.getControl().getShell(), "Checkouts View", message);
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     public void setFocus()
     {
         viewer.getControl().setFocus();
     }
 
     private void asyncRefresh()
     {
         Display.getDefault().asyncExec(new Runnable()
         {
             public void run()
             {
                 viewer.refresh();
             }
         });
     }
 
     /**
      * @see net.sourceforge.eclipseccase.StateChangeListener#stateChanged(net.sourceforge.eclipseccase.StateCache)
      */
     public void stateChanged(StateCache stateCache)
     {
         if (updateCheckout(stateCache))
         {
             asyncRefresh();
         }
     }
 
     private boolean updateCheckout(StateCache stateCache)
     {
         boolean actionPerformed = false;
         IResource resource = stateCache.getResource();
         boolean contains = checkouts.contains(resource);
         if (stateCache.isCheckedOut())
         {
             if (!contains)
             {
                 checkouts.add(resource);
                 actionPerformed = true;
             }
         }
         else
         {
             if (contains)
             {
                 checkouts.remove(resource);
                 actionPerformed = true;
             }
         }
         return actionPerformed;
     }
 
     void findCheckouts(IProgressMonitor monitor)
     {
         checkouts.clear();
         IProject[] projects = ClearcasePlugin.getWorkspace().getRoot().getProjects();
         monitor.beginTask("Finding checkouts", projects.length);
 
         for (int i = 0; i < projects.length; i++)
         {
             if (monitor.isCanceled())
             {
                 checkouts.clear();
                 CheckoutsView.this.asyncRefresh();
                 monitor.done();
                 throw new OperationCanceledException();
             }
             IProject project = projects[i];
             // Only find checkouts for each project if the project is open and
             // associated with ccase
             if (project.isOpen() && ClearcaseProvider.getProvider((IResource) project) != null)
             {
                 List foundCheckouts = findCheckouts(project);
 
                 // Iterate over all checkouts and add them to the checkouts
                 // view
                 for (Iterator iter = foundCheckouts.iterator(); iter.hasNext();)
                 {
                     IResource resource = null;
                     String checkout = (String) iter.next();
                     File cofile = new File(checkout);
                     if (cofile.isDirectory())
                     {
                         resource =
                             ClearcasePlugin.getWorkspace().getRoot().getContainerForLocation(
                                 new Path(checkout));
                     }
                     else
                     {
                         resource =
                             ClearcasePlugin.getWorkspace().getRoot().getFileForLocation(
                                 new Path(checkout));
                     }
                     if (resource != null)
                     {
                         StateCache cache = StateCacheFactory.getInstance().get(resource);
                         cache.update(true);
                         updateCheckout(cache);
                     }
                 }
             }
             monitor.worked(1);
         }
 
         CheckoutsView.this.asyncRefresh();
         monitor.done();
     }
 
     private static List findCheckouts(IProject project)
     {
         List checkouts = new LinkedList();
 
         // The collection of resources each of which we find checkouts for the
         // subtree
         Collection findResources = new HashSet();
 
         //RDM: For snapshot views, the basedir of the snapshot view itself is
         // not in CC, but it's children are.
         //		
         //		// Want to find checkouts for project if it is an element.
         //		if (StateCacheFactory.getInstance().get(project).hasRemote())
         //		{
         //			findResources.add(project);
         //		}
 
         // Even if project is/isn't an element, we still need to scan the links
         // and find checkouts for any links which are elements
         try
         {
             IResource[] members = project.members();
             for (int j = 0; j < members.length; j++)
             {
                 IResource child = members[j];
                 //RDM: No matter what, we scan all the children of the project
                 // to find CC-children
                 if (null != child.getLocation() && /* child.isLinked()&& */
                     StateCacheFactory.getInstance().get(child).hasRemote())
                 {
                     findResources.add(child.getLocation().toOSString());
                 }
             }
         }
         catch (CoreException e)
         {
             ClearcasePlugin.log(
                 IStatus.ERROR,
                 "Could not determine children of project for finding checkouts: " + project,
                 e);
         }
 
         // Find checkouts for all the important resources
         for (Iterator iter = findResources.iterator(); iter.hasNext();)
         {
             String path = (String) iter.next();
             checkouts.addAll(findCheckouts(path));
         }
         return checkouts;
     }
 
     private static List findCheckouts(String path)
     {
         // Faster to find all checkouts, and filter on path of interest, than
         // it is to find checkouts for subtree.
         List resultList = new ArrayList();
 
         try
         {
             File prefixFile = new File(path);
             String prefix = prefixFile.getCanonicalPath();
             int slashIdx = prefix.indexOf(File.separator);
             String prefixNoDrive = prefix.substring(slashIdx);
             String drive = prefix.substring(0, slashIdx);
 
             IClearcase.Status viewNameStatus = ClearcasePlugin.getEngine().getViewName(prefix);
             if (!viewNameStatus.status)
                 throw new Exception(viewNameStatus.message);
             String viewName = viewNameStatus.message.trim();
 
             boolean isSnapShot = ClearcasePlugin.getEngine().isSnapShot(prefix);
             boolean projectHasViewPath = prefix.indexOf(viewName) != -1;
 
             IClearcase.Status result =
                 ClearcasePlugin.getEngine().cleartool(
                     "lsco -me -cview -short -all " + ClearcaseUtil.quote(prefix));
             if (!result.status)
                 throw new Exception(result.message);
 
             StringTokenizer st = new StringTokenizer(result.message, "\r\n");
             while (st.hasMoreTokens())
             {
                 String entry = st.nextToken();
                 // If snapshot, or dynamic but project in eclipse is in
                 // clearcase "views" directory,
                 // then just add the filename verbatim, otherwise we need to
                 // clean it up by remapping
                 // to the same drive/etc as path passed in.
                 if (isSnapShot || projectHasViewPath)
                 {
                     resultList.add(entry);
                 }
                 else
                 {
                     int idx = entry.indexOf(viewName);
                     String cleanEntry;
                     if (idx == -1)
                     {
                         cleanEntry = entry;
                     }
                     else
                     {
                         idx += viewName.length();
                         cleanEntry = entry.substring(idx);
                     }
                     if (cleanEntry.startsWith(prefixNoDrive))
                         resultList.add(drive + cleanEntry);
                 }
 
             }
 
             Collections.sort(resultList);
         }
         catch (Exception e)
         {
             ClearcasePlugin.log(IStatus.ERROR, "Could not find checkouts for path: " + path, e);
         }
 
         return resultList;
     }
 
 }
