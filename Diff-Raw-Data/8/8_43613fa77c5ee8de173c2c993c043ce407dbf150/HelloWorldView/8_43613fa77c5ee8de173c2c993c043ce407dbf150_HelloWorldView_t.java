 /**
  * Copyright (c) 2011 Jan Ehrhardt.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Jan Ehrhardt
  */
 package org.ducktools.eclipse.tycho.samples.ui.views;
 
 import static org.eclipse.swt.SWT.H_SCROLL;
 import static org.eclipse.swt.SWT.MULTI;
 import static org.eclipse.swt.SWT.V_SCROLL;
 import static org.eclipse.ui.ISharedImages.IMG_OBJS_INFO_TSK;
 import static org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT;
 import static org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER;
 import static org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS;
 import static org.eclipse.ui.PlatformUI.getWorkbench;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.DrillDownAdapter;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 
 /**
  * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained from the model. The
  * sample creates a dummy model on the fly, but a real implementation would connect to the model available either in
  * this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
  * <p>
  * The view uses a label provider to define how model objects should be presented in the view. Each view can present the
  * same model objects using different labels and icons, if needed. Alternatively, a single label provider can be shared
  * between views in order to ensure that objects of the same type are presented in the same way everywhere.
  * <p>
  * 
  * @author <a href="https://github.com/derjan1982">Jan Ehrhardt</a>
  */
 public class HelloWorldView extends ViewPart {
 
   private static final Logger logger = getLogger(HelloWorldView.class);
 
   private TreeViewer viewer;
   private DrillDownAdapter drillDownAdapter;
   private Action action1;
   private Action action2;
   private Action doubleClickAction;
 
   /*
    * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
    * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore
    * it and always show the same content (like Task List, for example).
    */
 
   class TreeObject implements IAdaptable {
     private String name;
     private TreeParent parent;
 
     public TreeObject(String name) {
       this.name = name;
     }
 
     public String getName() {
       return name;
     }
 
     public void setParent(TreeParent parent) {
       this.parent = parent;
     }
 
     public TreeParent getParent() {
       return parent;
     }
 
     public String toString() {
       return getName();
     }
 
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
       return null;
     }
   }
 
   class TreeParent extends TreeObject {
    private ArrayList<TreeObject> children;
 
     public TreeParent(String name) {
       super(name);
      children = new ArrayList<TreeObject>();
     }
 
     public void addChild(TreeObject child) {
       children.add(child);
       child.setParent(this);
     }
 
     public void removeChild(TreeObject child) {
       children.remove(child);
       child.setParent(null);
     }
 
     public TreeObject[] getChildren() {
       return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
     }
 
     public boolean hasChildren() {
       return children.size() > 0;
     }
   }
 
   class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
     private TreeParent invisibleRoot;
 
     public void inputChanged(Viewer v, Object oldInput, Object newInput) {
     }
 
     public void dispose() {
     }
 
     public Object[] getElements(Object parent) {
       if (parent.equals(getViewSite())) {
         if (invisibleRoot == null)
           initialize();
         return getChildren(invisibleRoot);
       }
       return getChildren(parent);
     }
 
     public Object getParent(Object child) {
       if (child instanceof TreeObject) {
         return ((TreeObject) child).getParent();
       }
       return null;
     }
 
     public Object[] getChildren(Object parent) {
       if (parent instanceof TreeParent) {
         return ((TreeParent) parent).getChildren();
       }
       return new Object[0];
     }
 
     public boolean hasChildren(Object parent) {
       if (parent instanceof TreeParent)
         return ((TreeParent) parent).hasChildren();
       return false;
     }
 
     /*
      * We will set up a dummy model to initialize tree heararchy. In a real code, you will connect to a real model and
      * expose its hierarchy.
      */
     private void initialize() {
       TreeObject to1 = new TreeObject("Leaf 1");
       TreeObject to2 = new TreeObject("Leaf 2");
       TreeObject to3 = new TreeObject("Leaf 3");
       TreeParent p1 = new TreeParent("Parent 1");
       p1.addChild(to1);
       p1.addChild(to2);
       p1.addChild(to3);
 
       TreeObject to4 = new TreeObject("Leaf 4");
       TreeParent p2 = new TreeParent("Parent 2");
       p2.addChild(to4);
 
       TreeParent root = new TreeParent("Root");
       root.addChild(p1);
       root.addChild(p2);
 
       invisibleRoot = new TreeParent("");
       invisibleRoot.addChild(root);
     }
   }
 
   class ViewLabelProvider extends LabelProvider {
 
     public String getText(Object obj) {
       return obj.toString();
     }
 
     public Image getImage(Object obj) {
       String imageKey = IMG_OBJ_ELEMENT;
       if (obj instanceof TreeParent)
         imageKey = IMG_OBJ_FOLDER;
       return getWorkbench().getSharedImages().getImage(imageKey);
     }
   }
 
   class NameSorter extends ViewerSorter {
   }
 
   /**
    * The constructor.
    */
   public HelloWorldView() {
   }
 
   /**
    * This is a callback that will allow us to create the viewer and initialize it.
    */
   public void createPartControl(Composite parent) {
     viewer = new TreeViewer(parent, MULTI | H_SCROLL | V_SCROLL);
     drillDownAdapter = new DrillDownAdapter(viewer);
     viewer.setContentProvider(new ViewContentProvider());
     viewer.setLabelProvider(new ViewLabelProvider());
     viewer.setSorter(new NameSorter());
     viewer.setInput(getViewSite());
 
     makeActions();
     hookContextMenu();
     hookDoubleClickAction();
     contributeToActionBars();
   }
 
   private void hookContextMenu() {
     MenuManager menuMgr = new MenuManager("#PopupMenu");
     menuMgr.setRemoveAllWhenShown(true);
     menuMgr.addMenuListener(new IMenuListener() {
       public void menuAboutToShow(IMenuManager manager) {
         HelloWorldView.this.fillContextMenu(manager);
       }
     });
     Menu menu = menuMgr.createContextMenu(viewer.getControl());
     viewer.getControl().setMenu(menu);
     getSite().registerContextMenu(menuMgr, viewer);
   }
 
   private void contributeToActionBars() {
     IActionBars bars = getViewSite().getActionBars();
     fillLocalPullDown(bars.getMenuManager());
     fillLocalToolBar(bars.getToolBarManager());
   }
 
   private void fillLocalPullDown(IMenuManager manager) {
     manager.add(action1);
     manager.add(new Separator());
     manager.add(action2);
   }
 
   private void fillContextMenu(IMenuManager manager) {
     manager.add(action1);
     manager.add(action2);
     manager.add(new Separator());
     drillDownAdapter.addNavigationActions(manager);
     // Other plug-ins can contribute there actions here
     manager.add(new Separator(MB_ADDITIONS));
   }
 
   private void fillLocalToolBar(IToolBarManager manager) {
     manager.add(action1);
     manager.add(action2);
     manager.add(new Separator());
     drillDownAdapter.addNavigationActions(manager);
   }
 
   private void makeActions() {
     action1 = new Action() {
       public void run() {
         String message = "Action 1 executed";
         logger.info(message);
         showMessage(message);
       }
     };
     action1.setText("Action 1");
     action1.setToolTipText("Action 1 tooltip");
     action1.setImageDescriptor(getWorkbench().getSharedImages().getImageDescriptor(IMG_OBJS_INFO_TSK));
 
     action2 = new Action() {
       public void run() {
         String message = "Action 2 executed";
         logger.info(message);
         showMessage(message);
       }
     };
     action2.setText("Action 2");
     action2.setToolTipText("Action 2 tooltip");
     action2.setImageDescriptor(getWorkbench().getSharedImages().getImageDescriptor(IMG_OBJS_INFO_TSK));
     doubleClickAction = new Action() {
       public void run() {
         ISelection selection = viewer.getSelection();
         Object obj = ((IStructuredSelection) selection).getFirstElement();
         showMessage("Double-click detected on " + obj.toString());
       }
     };
   }
 
   private void hookDoubleClickAction() {
     viewer.addDoubleClickListener(new IDoubleClickListener() {
       public void doubleClick(DoubleClickEvent event) {
         doubleClickAction.run();
       }
     });
   }
 
   private void showMessage(String message) {
     MessageDialog.openInformation(viewer.getControl().getShell(), "Hello World", message);
   }
 
   /**
    * Passing the focus request to the viewer's control.
    */
   public void setFocus() {
     viewer.getControl().setFocus();
   }
 }
