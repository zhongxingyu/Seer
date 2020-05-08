 package org.eclipse.dltk.ui.browsing.ext;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
import java.util.Map;
import java.util.Queue;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.ui.actions.BuildActionGroup;
 import org.eclipse.dltk.internal.ui.actions.CCPActionGroup;
 import org.eclipse.dltk.internal.ui.actions.CompositeActionGroup;
 import org.eclipse.dltk.internal.ui.actions.ImportActionGroup;
 import org.eclipse.dltk.internal.ui.actions.NewWizardsActionGroup;
 import org.eclipse.dltk.internal.ui.actions.refactoring.RefactorActionGroup;
 import org.eclipse.dltk.internal.ui.search.SearchUtil;
 import org.eclipse.dltk.ui.DLTKExecuteExtensionHelper;
 import org.eclipse.dltk.ui.DLTKUILanguageManager;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.ModelElementSorter;
 import org.eclipse.dltk.ui.actions.OpenEditorActionGroup;
 import org.eclipse.dltk.ui.actions.OpenViewActionGroup;
 import org.eclipse.dltk.ui.actions.SearchActionGroup;
 import org.eclipse.dltk.ui.browsing.ScriptElementTypeComparator;
 import org.eclipse.dltk.ui.infoviews.AbstractInfoView;
 import org.eclipse.dltk.ui.viewsupport.IViewPartInputProvider;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.search.ui.ISearchResultViewPart;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.actions.ActionGroup;
 import org.eclipse.ui.part.ViewPart;
 
 public class ExtendedClassesView extends ViewPart implements
 		IViewPartInputProvider, ISelectionListener, ISelectionProvider,
 		IExecutableExtension, IMenuListener {
 
 	private final class IElementChangedListenerImplementation implements
 			IElementChangedListener {
 		public void elementChanged(ElementChangedEvent event) {
 			// We need to update
 //			if (event.getType() == ElementChangedEvent.POST_CHANGE) {
 				IModelElementDelta delta = event.getDelta();
 				if (browsingPane != null && !browsingPane.isDisposed()
 						&& typesChanged(delta)) {
 					browsingPane.refresh();
 				}
 //			}
 		}
 
 		private boolean typesChanged(IModelElementDelta delta) {
 			IModelElementDelta[] affectedChildren = delta.getAffectedChildren();
 			for (int i = 0; i < affectedChildren.length; i++) {
 				if (affectedChildren[i].getElement().getElementType() == IModelElement.TYPE) {
 					return true;
 				}
 				else {
 					if( typesChanged(affectedChildren[i])) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 	private OpenEditorActionGroup fOpenEditorGroup;
 	private CCPActionGroup fCCPActionGroup;
 	private BuildActionGroup fBuildActionGroup;
 	// private ToggleLinkingAction fToggleLinkingAction;
 	protected CompositeActionGroup fActionGroups;
 
 	// private boolean fHasCustomFilter = true;
 
 	// Filters
 	// private CustomFiltersActionGroup fCustomFiltersActionGroup;
 
 	private MultiSelectionListViewer browsingPane;
 
 	public ExtendedClassesView() {
 		elementChangedListenerImplementation = new IElementChangedListenerImplementation();
 		DLTKCore
 				.addElementChangedListener(elementChangedListenerImplementation);
 	}
 
 	public void dispose() {
 		super.dispose();
 		DLTKCore
 				.removeElementChangedListener(elementChangedListenerImplementation);
 	}
 
 	//
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout());
 		browsingPane = new MultiSelectionListViewer(parent, SWT.NONE) {
 			public void elementSelectionChanged(ISelection selection) {
 				Object[] listeners = listenerList.getListeners();
 				SelectionChangedEvent event = new SelectionChangedEvent(
 						ExtendedClassesView.this, convertSelection(selection));
 				for (int i = 0; i < listeners.length; i++) {
 					((ISelectionChangedListener) (listeners[i]))
 							.selectionChanged(event);
 				}
 			}
 
 			protected void configureViewer(TreeViewer viewer) {
 				// viewer.setCom
 				viewer.setUseHashlookup(true);
 				viewer.setSorter(new ModelElementSorter() {
 					public int compare(Viewer viewer, Object e1, Object e2) {
 						e1 = unWrap(e1);
 						e2 = unWrap(e2);
 						return super.compare(viewer, e1, e2);
 					}
 
 					private Object unWrap(Object e1) {
 						if (e1 instanceof MixedClass) {
 							MixedClass cl = ((MixedClass) e1);
 							if (cl.getElements().size() > 0) {
 								e1 = cl.getElements().get(0);
 							}
 						}
 						return e1;
 					}
 
 					protected String getElementName(Object element) {
 						element = unWrap(element);
 						return super.getElementName(element);
 					}
 
 					public int category(Object element) {
 						return super.category(unWrap(element));
 					}
 
 				});
 
 				// Initialize menu
 				createContextMenu(viewer.getControl());
 			}
 
 		};
 		browsingPane.setContentProvider(new ExtendedClasesContentProvider(this,
 				SearchEngine.createWorkspaceScope(this.fToolkit), parent));
 		IDLTKUILanguageToolkit languageToolkit = DLTKUILanguageManager
 				.getLanguageToolkit(this.fToolkit.getNatureId());
 
 		browsingPane.setLabelProvider(new ExtendedClasesLabelProvider(
 				languageToolkit.createScripUILabelProvider()));
 
 		getSite().setSelectionProvider(this);
 		getViewSite().getPage().addPostSelectionListener(this);
 		getViewSite().getPage().addPartListener(fPartListener);
 
 		createActions();
 	}
 
 	protected void createActions() {
 		fActionGroups = new CompositeActionGroup(new ActionGroup[] {
 				new NewWizardsActionGroup(this.getSite()),
 				fOpenEditorGroup = new OpenEditorActionGroup(this),
 				new OpenViewActionGroup(this),
 				fCCPActionGroup = new CCPActionGroup(this),
 				// new GenerateActionGroup(this),
 				new RefactorActionGroup(this), new ImportActionGroup(this),
 				fBuildActionGroup = new BuildActionGroup(this),
 				new SearchActionGroup(this, this.fToolkit) });
 
 		// fToggleLinkingAction = new ToggleLinkingAction(this);
 	}
 
 	protected void createContextMenu(Control parent) {
 		MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
 		menuManager.setRemoveAllWhenShown(true);
 		menuManager.addMenuListener(this);
 		Menu contextMenu = menuManager.createContextMenu(this.browsingPane);
 		// this.browsingPane.setMenu(contextMenu);
 		parent.setMenu(contextMenu);
 		getSite().registerContextMenu(menuManager, this);
 	}
 
 	/**
 	 * We need to prefer local elements with same name
 	 * 
 	 * @param selection
 	 * @return
 	 */
 	protected ISelection convertSelection(ISelection selection) {
 		List result = new ArrayList();
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection sel = (StructuredSelection) selection;
 			List list = sel.toList();
 			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
 				Object o = (Object) iterator.next();
 				if (o instanceof MixedClass) {
 					List mixedElements = ((MixedClass) o).getElements();
 					result.addAll(mixedElements);
 				} else {
 					result.add(o);
 				}
 			}
 		}
 		if (result.size() > 0) {
 			return new StructuredSelection(result);
 		}
 		return new StructuredSelection();
 	}
 
 	public void setFocus() {
 	}
 
 	public Object getViewPartInput() {
 		return browsingPane.getSelection();
 	}
 
 	private boolean isSearchResultView(IWorkbenchPart part) {
 		return SearchUtil.isSearchPlugInActivated()
 				&& part instanceof ISearchResultViewPart;
 	}
 
 	protected IWorkbenchPart fPreviousSelectionProvider;
 	protected Object fPreviousSelectedElement;
 
 	protected boolean needsToProcessSelectionChanged(IWorkbenchPart part,
 			ISelection selection) {
 		if (!fProcessSelectionEvents || part == this
 				|| isSearchResultView(part) || part instanceof AbstractInfoView) {
 			if (part == this)
 				fPreviousSelectionProvider = part;
 			return false;
 		}
 		return true;
 	}
 
 	protected final Object getSingleElementFromSelection(ISelection selection) {
 		if (!(selection instanceof IStructuredSelection) || selection.isEmpty())
 			return null;
 
 		Iterator iter = ((IStructuredSelection) selection).iterator();
 		Object firstElement = iter.next();
 		if (!(firstElement instanceof IModelElement)) {
 			if (firstElement instanceof IMarker)
 				firstElement = ((IMarker) firstElement).getResource();
 			if (firstElement instanceof IAdaptable) {
 				IModelElement je = (IModelElement) ((IAdaptable) firstElement)
 						.getAdapter(IModelElement.class);
 				if (je == null && firstElement instanceof IFile) {
 					IContainer parent = ((IFile) firstElement).getParent();
 					if (parent != null)
 						return parent.getAdapter(IModelElement.class);
 					else
 						return null;
 				} else
 					return je;
 
 			} else
 				return firstElement;
 		}
 		Object currentInput = browsingPane.getInput();
 		List elements = new ArrayList();
 		if (currentInput == null
 				|| !currentInput.equals((IModelElement) firstElement))
 			if (iter.hasNext() && selection instanceof StructuredSelection) {
 				// multi-selection and view is empty
 				return ((StructuredSelection) selection).toList();
 			} else
 				// OK: single selection and view is empty
 				return firstElement;
 
 		// be nice to multi-selection
 		while (iter.hasNext()) {
 			Object element = iter.next();
 			if (!(element instanceof IModelElement))
 				return null;
 			if (!currentInput.equals((IModelElement) element))
 				return null;
 		}
 		return firstElement;
 	}
 
 	private ScriptElementTypeComparator fTypeComparator;
 
 	private Comparator getTypeComparator() {
 		if (fTypeComparator == null) {
 			fTypeComparator = new ScriptElementTypeComparator();
 		}
 		return fTypeComparator;
 	}
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		if (!needsToProcessSelectionChanged(part, selection))
 			return;
 		if (!(selection instanceof IStructuredSelection))
 			return;
 		// Set selection
 		Object selectedElement = getSingleElementFromSelection(selection);
 		if (!checkElementNature(selectedElement)) {
 			return;
 		}
 		if (selectedElement instanceof List) {
 			List newList = new ArrayList();
 			List list = (List) selectedElement;
 			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
 				Object obj = iterator.next();
 				if (checkElementNature(obj)) {
 					newList.add(obj);
 				}
 			}
 			if (newList.size() > 0) {
 				selectedElement = newList;
 			} else {
 				return;
 			}
 		}
 
 		if (selectedElement != null
 				&& (part == null || part.equals(fPreviousSelectionProvider))
 				&& selectedElement.equals(fPreviousSelectedElement))
 			return;
 		fPreviousSelectedElement = selectedElement;
 
 		if (selectedElement != null
 				&& (selectedElement instanceof IScriptProject || selectedElement instanceof IProjectFragment)) {
 			browsingPane.setInput(selectedElement);
 		}
 	}
 
 	private boolean checkElementNature(Object selectedElement) {
 		if (selectedElement instanceof IModelElement) {
 			String natureId = this.fToolkit.getNatureId();
 			try {
 				IDLTKLanguageToolkit languageToolkit = DLTKLanguageManager
 						.getLanguageToolkit((IModelElement) selectedElement);
 				if (languageToolkit != null
 						&& natureId.equals(languageToolkit.getNatureId())) {
 					return true;
 				}
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean fProcessSelectionEvents = true;
 	private IPartListener2 fPartListener = new IPartListener2() {
 		public void partActivated(IWorkbenchPartReference ref) {
 		}
 
 		public void partBroughtToTop(IWorkbenchPartReference ref) {
 		}
 
 		public void partInputChanged(IWorkbenchPartReference ref) {
 		}
 
 		public void partClosed(IWorkbenchPartReference ref) {
 		}
 
 		public void partDeactivated(IWorkbenchPartReference ref) {
 		}
 
 		public void partOpened(IWorkbenchPartReference ref) {
 		}
 
 		public void partVisible(IWorkbenchPartReference ref) {
 			if (ref != null && ref.getId() == getSite().getId()) {
 				fProcessSelectionEvents = true;
 				IWorkbenchPage page = getSite().getWorkbenchWindow()
 						.getActivePage();
 				if (page != null)
 					selectionChanged(page.getActivePart(), page.getSelection());
 			}
 		}
 
 		public void partHidden(IWorkbenchPartReference ref) {
 			if (ref != null && ref.getId() == getSite().getId())
 				fProcessSelectionEvents = false;
 		}
 	};
 
 	ListenerList listenerList = new ListenerList();
 	private IDLTKLanguageToolkit fToolkit;
 	private IElementChangedListenerImplementation elementChangedListenerImplementation;
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listenerList.add(listener);
 	}
 
 	public ISelection getSelection() {
 		return convertSelection(browsingPane.getSelection());
 	}
 
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		listenerList.remove(listener);
 	}
 
 	public void setSelection(ISelection selection) {
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) {
 		super.setInitializationData(config, propertyName, data);
 		this.fToolkit = DLTKExecuteExtensionHelper.getLanguageToolkit(config,
 				propertyName, data);
 	}
 
 	public void menuAboutToShow(IMenuManager menu) {
 		DLTKUIPlugin.createStandardGroups(menu);
 
 		IStructuredSelection selection = (IStructuredSelection) getSelection();
 		// int size = selection.size();
 		// Object element = selection.getFirstElement();
 
 		fActionGroups.setContext(new ActionContext(selection));
 		fActionGroups.fillContextMenu(menu);
 		fActionGroups.setContext(null);
 	}
 }
