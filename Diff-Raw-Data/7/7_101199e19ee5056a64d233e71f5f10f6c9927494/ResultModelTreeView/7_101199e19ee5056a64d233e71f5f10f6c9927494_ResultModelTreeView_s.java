 package org.eclipse.emf.refactor.smells.runtime.ui;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.refactor.smells.interfaces.IHighlighting;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.emf.refactor.smells.runtime.core.ResultModel;
 import org.eclipse.emf.refactor.smells.runtime.managers.RuntimeManager;
 import org.eclipse.emf.refactor.smells.runtime.ui.actions.ClearAction;
 import org.eclipse.emf.refactor.smells.runtime.ui.actions.SaveAction;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * View for displaying the <i>ResultModel</i> of a smell identification process.
  * 
  * @author Matthias Burhenne
  *
  */
 
 public class ResultModelTreeView extends ViewPart {
 //	public static final String MENU_ID = "de.unimarburg.swt.emf.modelsmell.resultview.contextmenu";
 	public static final String MENU_ID = "org.eclipse.emf.refactor.smells.view";
 	private ResultModelTreeViewer viewer;
 	private Composite parent;
 	
 	private SaveAction saveAction;
 	private ClearAction clearAction;
 	private List<Action> additionalActions = new ArrayList<Action>();
 	private List<IHighlighting> additionalHighlightings = new ArrayList<IHighlighting>();
 	
 	private static IMenuManager barMM;
 	private static IToolBarManager toolbarMM;
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
 		this.viewer = new ResultModelTreeViewer(parent); //TODO: add link between viewer and EMFModelSmells
 		viewer.setContentProvider(new ResultModelTreeViewerContentProvider());
 		viewer.setLabelProvider(new ResultModelTreeViewerLabelProvider());
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				 if(event.getSelection().isEmpty()) {
 			           return;
 			     }
 //				 if (event.getSelection() instanceof StructuredSelection) {
 //					 StructuredSelection ss = (StructuredSelection) event.getSelection();
 //					 System.out.println("StructuredSelection: " + ss);	
 //					 Object selection = ss.getFirstElement();
 //					 System.out.println("Selection: " + selection);	
 //					 ArrayList<EObject> eObjectList = new ArrayList<EObject>();
 //					 System.out.println("eObjectList: " + eObjectList);	
 //					 if (selection instanceof EObject) {
 //						 String path = ((EObject) selection).eResource().getURI().toPlatformString(false);
 //						 System.out.println("Path: " + path);
 //						 eObjectList.add((EObject) selection);
 //					 }
 //					 if (selection instanceof EObjectGroup) {
 //						 eObjectList.addAll(((EObjectGroup) selection).getEObjects());
 //					 }
 //					 System.out.println("eObjectList: " + eObjectList);
 //					 Object[] objects = eObjectList.toArray();
 //					 RuntimeManager.selectModelObjects(objects);
 //				 }
 				 IEditorPart editorPart = null;
 				 if(event.getSelection() instanceof IStructuredSelection) {
 					 editorPart = openEditor((IStructuredSelection)event.getSelection());
 				 }
 				 if(editorPart != null && editorPart instanceof IViewerProvider){
 					 setSelectionInModel((IViewerProvider)editorPart, (IStructuredSelection)event.getSelection());
 //						 ((IViewerProvider)editorPart).getViewer().setSelection(event.getSelection(), true);
 				 }
				 doAdditionalHighlightings();
 			}
 			
 			private void setSelectionInModel(IViewerProvider provider, IStructuredSelection selection){
 				ArrayList<EObject> eObjectList = new ArrayList<EObject>();
 				for(Object selectedObject : selection.toList()){
 					if(selectedObject instanceof EObjectGroup){
 						for(EObject currentEObject : ((EObjectGroup)selectedObject).getEObjects()){
 							EObject modelInstanceObject = getModelObjectInstance(provider, currentEObject);
 							eObjectList.add(modelInstanceObject);
 						}
 						((EObjectGroup)selectedObject).setEObjects(eObjectList);
 					}else if(selectedObject instanceof EObject){
 						eObjectList.add(getModelObjectInstance(provider, (EObject)selectedObject));
 					}
 				}
 				provider.getViewer().setSelection(new StructuredSelection(eObjectList));
 //				Object o = selection.getFirstElement();
 //				if(o instanceof EObject){
 //					Object input = provider.getViewer().getInput();
 //					provider.getViewer().setSelection(selection);
 //				}else if(o instanceof EObjectGroup){
 //					StructuredSelection s = new StructuredSelection(((EObjectGroup)o).getEObjects());
 //					provider.getViewer().setSelection(s);
 //				}
 				
 			}
 			
 			private EObject getModelObjectInstance(IViewerProvider provider, EObject eObject) {
 				Viewer viewer = provider.getViewer();
 				TreeViewer treeViewer = (TreeViewer)viewer;
 				Tree tree = treeViewer.getTree();
 				TreeItem treeItem = tree.getItem(0);
 				Resource o = (Resource)treeItem.getData();
 				ResourceSet set = o.getResourceSet();
 				EObject ob = set.getEObject(EcoreUtil.getURI(eObject), true);
 //				String uri = o.getURIFragment(eObject);
 				return ob;
 //				return o.getEObject(uri);
 //				EObject equalObject = o.getEObject(eObject.eResource().getURIFragment(eObject));
 //				return equalObject;
 				
 
 				
 //				if(provider instanceof IEditingDomainProvider){
 //					IEditingDomainProvider prov = (IEditingDomainProvider)provider;
 //					TreeIterator iterator = prov.getEditingDomain().treeIterator(viewer.getCurrentRoot());
 //					while(iterator.hasNext()){
 //						Object itObj = iterator.next();
 //						if(itObj.equals(eObject)){
 //							return (EObject) itObj;
 //						}
 //					}
 //				}
 //				return eObject;
 			}
 
 			@SuppressWarnings("unchecked")
 			private IEditorPart openEditor(IStructuredSelection selection){
 				Object o = selection.getFirstElement();
 				EObject obj = null;
 				if(o instanceof EObject){
 					obj = (EObject)o;
 				}else if(o instanceof EObjectGroup){
 					EObjectGroup group = (EObjectGroup)o;
 					if(!group.getEObjects().isEmpty()){
 						obj = group.getEObjects().getFirst();
 					}
 				}
 				if(obj != null){
 //					URI uri = obj.eResource().getURI();
 //					String fileString = URI.decode(uri.path());
 //					fileString = fileString.replaceFirst("/resource", "");
 //					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileString));
 //					IWorkbench workbench = PlatformUI.getWorkbench();
 					IFile file = ((LinkedList<ResultModel>)viewer.getInput()).get(0).getIFile();
 					IWorkbenchWindow workbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 					IWorkbenchPage page = workbenchWindow.getActivePage();
 					try {
 						if(file.exists()){
 							return IDE.openEditor(page, file);
 						}
 					}catch (PartInitException exception) {
 						return null;
 					}
 				}
 		        return null;
 		     }
 				
 			
 		});
 		RuntimeManager.setResultModelTreeViewer(viewer);
 		LinkedList<ResultModel> result = RuntimeManager.getResultModels();
 		if(result != null){
 			viewer.setInput(result);
 		}
 		makeActions();
 		hookContextMenu();
 		contributeToActionBars();
 	}
 	
 	protected void doAdditionalHighlightings() {
 		for (IHighlighting highlighting : additionalHighlightings) {
 			highlighting.highlight();
 		}
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				ResultModelTreeView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 	
 
 //	private void createContextMenu() {
 //		MenuManager menuMgr = new MenuManager();
 //        menuMgr.setRemoveAllWhenShown(true);
 //        menuMgr.addMenuListener(new IMenuListener() {
 //                     
 //
 //			@Override
 //			public void menuAboutToShow(IMenuManager manager) {
 //				fillContextMenu(manager);			
 //			}
 //			
 //			private void fillContextMenu(IMenuManager manager){
 //				 manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
 //			}
 //        });
 //             
 //             // Create menu.
 //          Menu menu = menuMgr.createContextMenu(viewer.getControl());
 //             viewer.getControl().setMenu(menu);
 //             
 //             // Register menu for extension.
 //          getSite().registerContextMenu(MENU_ID, menuMgr, viewer);
 //    }
 	
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		barMM = manager;
 		manager.add(saveAction);
 		manager.add(clearAction);
 		manager.add(new Separator());
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(saveAction);
 		manager.add(clearAction);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		toolbarMM = manager;
 		manager.add(saveAction);
 		manager.add(clearAction);
 	}
 	
 	private void makeActions() {
 		saveAction = new SaveAction(parent.getShell(), viewer);
 		clearAction = new ClearAction();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public IEditorPart openEditor(IStructuredSelection selection){
 		Object o = selection.getFirstElement();
 		EObject obj = null;
 		if(o instanceof EObject){
 			obj = (EObject)o;
 		}
 		if(o instanceof EObjectGroup){
 			EObjectGroup group = (EObjectGroup)o;
 			if(!group.getEObjects().isEmpty()){
 				obj = group.getEObjects().getFirst();
 			}
 		}
 		if(obj != null){
 //			URI uri = obj.eResource().getURI();
 //			String fileString = URI.decode(uri.path());
 //			fileString = fileString.replaceFirst("/resource", "");
 //			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileString));
 //			IWorkbench workbench = PlatformUI.getWorkbench();
 			IFile file = ((LinkedList<ResultModel>)viewer.getInput()).get(0).getIFile();
 			IWorkbenchWindow workbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			IWorkbenchPage page = workbenchWindow.getActivePage();
 			try {
 				if(file.exists()){
 					return IDE.openEditor(page, file);
 				}
 			}catch (PartInitException exception) {
 				return null;
 			}
 		}
         return null;
      }
 
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	public void addAction(Action action) {
 		additionalActions.add(action);
 	}
 	
 	public void addActionsToMenu() {
 		for (Action action : additionalActions) {	
 			if (toolbarMM.getItems().length != additionalActions.size()+2) {
 				barMM.add(action);
 				barMM.update(true);
 				toolbarMM.add(action);
 				toolbarMM.update(true);
 			}
 		}
 		additionalActions.clear();
 	}
 	
 	public void addHighlighting(IHighlighting highlighting) {
 		additionalHighlightings.add(highlighting);
 	}
 
 }
