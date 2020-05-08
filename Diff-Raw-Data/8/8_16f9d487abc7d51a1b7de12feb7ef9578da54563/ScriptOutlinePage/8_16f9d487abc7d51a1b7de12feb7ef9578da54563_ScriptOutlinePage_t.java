 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.editor;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.actions.AbstractToggleLinkingAction;
 import org.eclipse.dltk.internal.ui.actions.CCPActionGroup;
 import org.eclipse.dltk.internal.ui.actions.CompositeActionGroup;
 import org.eclipse.dltk.internal.ui.actions.refactoring.RefactorActionGroup;
 import org.eclipse.dltk.internal.ui.dnd.DLTKViewerDragAdapter;
 import org.eclipse.dltk.internal.ui.dnd.DelegatingDropAdapter;
 import org.eclipse.dltk.internal.ui.scriptview.SelectionTransferDragAdapter;
 import org.eclipse.dltk.internal.ui.scriptview.SelectionTransferDropAdapter;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IContextMenuConstants;
 import org.eclipse.dltk.ui.MembersOrderPreferenceCache;
 import org.eclipse.dltk.ui.ModelElementSorter;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.dltk.ui.ProblemsLabelDecorator.ProblemsLabelChangedEvent;
 import org.eclipse.dltk.ui.actions.CustomFiltersActionGroup;
 import org.eclipse.dltk.ui.actions.DLTKSearchActionGroup;
 import org.eclipse.dltk.ui.actions.MemberFilterActionGroup;
 import org.eclipse.dltk.ui.actions.OpenViewActionGroup;
 import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
 import org.eclipse.dltk.ui.viewsupport.DecoratingModelLabelProvider;
 import org.eclipse.dltk.ui.viewsupport.SourcePositionSorter;
 import org.eclipse.dltk.ui.viewsupport.StatusBarUpdater;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.util.TransferDragSourceListener;
 import org.eclipse.jface.util.TransferDropTargetListener;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.actions.ActionGroup;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 import org.eclipse.ui.model.WorkbenchAdapter;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.part.IShowInSource;
 import org.eclipse.ui.part.IShowInTarget;
 import org.eclipse.ui.part.IShowInTargetList;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.part.ShowInContext;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.texteditor.IUpdate;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
 
 /**
  * The content outline page of the Java editor. The viewer implements a
  * proprietary update mechanism based on Java model deltas. It does not react on
  * domain changes. It is specified to show the content of ICompilationUnits and
  * IClassFiles. Publishes its context menu under
  * <code>JavaPlugin.getDefault().getPluginId() + ".outline"</code>.
  */
 public class ScriptOutlinePage extends Page implements IContentOutlinePage,
 		IAdaptable, IPostSelectionProvider {
 
 	/**
 	 * Content provider for the children of an ICompilationUnit or an IClassFile
 	 * 
 	 * @see ITreeContentProvider
 	 */
 	protected class ChildrenProvider implements ITreeContentProvider {
 
 		private Object[] NO_CLASS = new Object[] { new NoClassElement() };
 		private ElementChangedListener fListener;
 
 		public void dispose() {
 			if (fListener != null) {
 				DLTKCore.removeElementChangedListener(fListener);
 				fListener = null;
 			}
 		}
 
 		protected IModelElement[] filter(IModelElement[] children) {
 			boolean initializers = false;
 			for (int i = 0; i < children.length; i++) {
 				if (matches(children[i])) {
 					initializers = true;
 					break;
 				}
 			}
 
 			if (!initializers)
 				return children;
 
 			Vector v = new Vector();
 			for (int i = 0; i < children.length; i++) {
 				if (matches(children[i]))
 					continue;
 				v.addElement(children[i]);
 			}
 
 			IModelElement[] result = new IModelElement[v.size()];
 			v.copyInto(result);
 			return result;
 		}
 
 		public Object[] getChildren(Object parent) {
 			if (parent instanceof IParent) {
 				IParent c = (IParent) parent;
 				try {
 					return filter(c.getChildren());
 				} catch (ModelException x) {
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=38341
 					// don't log NotExist exceptions as this is a valid case
 					// since we might have been posted and the element
 					// removed in the meantime.
 					if (DLTKCore.DEBUG || !x.isDoesNotExist())
 						DLTKUIPlugin.log(x);
 				}
 			}
 			return NO_CHILDREN;
 		}
 
 		public Object[] getElements(Object parent) {
 			return getChildren(parent);
 		}
 
 		public Object getParent(Object child) {
 			if (child instanceof IModelElement) {
 				IModelElement e = (IModelElement) child;
 				return e.getParent();
 			}
 			return null;
 		}
 
 		public boolean hasChildren(Object parent) {
 			if (parent instanceof IParent) {
 				IParent c = (IParent) parent;
 				try {
 					IModelElement[] children = filter(c.getChildren());
 					return (children != null && children.length > 0);
 				} catch (ModelException x) {
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=38341
 					// don't log NotExist exceptions as this is a valid case
 					// since we might have been posted and the element
 					// removed in the meantime.
 					if (DLTKUIPlugin.isDebug() || !x.isDoesNotExist())
 						DLTKUIPlugin.log(x);
 				}
 			}
 			return false;
 		}
 
 		/*
 		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
 		 */
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			boolean isCU = (newInput instanceof ISourceModule);
 
 			if (isCU && fListener == null) {
 				fListener = new ElementChangedListener();
 				DLTKCore.addElementChangedListener(fListener);
 			} else if (!isCU && fListener != null) {
 				DLTKCore.removeElementChangedListener(fListener);
 				fListener = null;
 			}
 		}
 
 		public boolean isDeleted(Object o) {
 			return false;
 		}
 
 		protected boolean matches(IModelElement element) {
 			if (element.getElementType() == IModelElement.METHOD) {
 				String name = element.getElementName();
 				return (name != null && name.indexOf('<') >= 0);
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * The element change listener of the java outline viewer.
 	 * 
 	 * @see IElementChangedListener
 	 */
 	protected class ElementChangedListener implements IElementChangedListener {
 
 		public void elementChanged(final ElementChangedEvent e) {
 
 			if (getControl() == null)
 				return;
 
 			Display d = getControl().getDisplay();
 			if (d != null) {
 				d.asyncExec(new Runnable() {
 					public void run() {
 						ISourceModule cu = (ISourceModule) fInput;
 						IModelElement base = cu;
 
 						IModelElementDelta delta = findElement(base, e
 								.getDelta());
 						if (delta != null && fOutlineViewer != null) {
 							fOutlineViewer.reconcile(delta);
 						}
 					}
 				});
 			}
 		}
 
 		protected IModelElementDelta findElement(IModelElement unit,
 				IModelElementDelta delta) {
 
 			if (delta == null || unit == null)
 				return null;
 
 			IModelElement element = delta.getElement();
 
 			if (unit.equals(element)) {
 				if (isPossibleStructuralChange(delta)) {
 					return delta;
 				}
 				return null;
 			}
 
 			if (element.getElementType() > IModelElement.SOURCE_MODULE)
 				return null;
 
 			IModelElementDelta[] children = delta.getAffectedChildren();
 			if (children == null || children.length == 0)
 				return null;
 
 			for (int i = 0; i < children.length; i++) {
 				IModelElementDelta d = findElement(unit, children[i]);
 				if (d != null)
 					return d;
 			}
 
 			return null;
 		}
 
 		private boolean isPossibleStructuralChange(IModelElementDelta cuDelta) {
 			if (cuDelta.getKind() != IModelElementDelta.CHANGED) {
 				return true; // add or remove
 			}
 			int flags = cuDelta.getFlags();
 			if ((flags & IModelElementDelta.F_CHILDREN) != 0) {
 				return true;
 			}
 			return (flags & (IModelElementDelta.F_CONTENT | IModelElementDelta.F_FINE_GRAINED)) == IModelElementDelta.F_CONTENT;
 		}
 	}
 
 	/**
 	 * Empty selection provider.
 	 * 
 	 * @since 3.2
 	 */
 	private static final class EmptySelectionProvider implements
 			ISelectionProvider {
 		public void addSelectionChangedListener(
 				ISelectionChangedListener listener) {
 		}
 
 		public ISelection getSelection() {
 			return StructuredSelection.EMPTY;
 		}
 
 		public void removeSelectionChangedListener(
 				ISelectionChangedListener listener) {
 		}
 
 		public void setSelection(ISelection selection) {
 		}
 	}
 
 	/**
 	 * The tree viewer used for displaying the outline.
 	 * 
 	 * @see TreeViewer
 	 */
 	protected class ScriptOutlineViewer extends TreeViewer {
 
 		/**
 		 * Indicates an item which has been reused. At the point of its reuse it
 		 * has been expanded. This field is used to communicate between
 		 * <code>internalExpandToLevel</code> and <code>reuseTreeItem</code>.
 		 */
 		private Item fReusedExpandedItem;
 		private boolean fReorderedMembers;
 		private boolean fForceFireSelectionChanged;
 
 		public ScriptOutlineViewer(Tree tree) {
 			super(tree);
 			setAutoExpandLevel(ALL_LEVELS);
 			setUseHashlookup(true);
 		}
 
 		protected boolean filtered(IModelElement parent, IModelElement child) {
 
 			Object[] result = new Object[] { child };
 			ViewerFilter[] filters = getFilters();
 			for (int i = 0; i < filters.length; i++) {
 				result = filters[i].filter(this, parent, result);
 				if (result.length == 0)
 					return true;
 			}
 
 			return false;
 		}
 
 		protected ISourceRange getSourceRange(IModelElement element)
 				throws ModelException {
 			if (element instanceof ISourceReference)
 				return ((ISourceReference) element).getSourceRange();
 			if (element instanceof IMember
 			/* && !(element instanceof IInitializer) */)
 				return ((IMember) element).getNameRange();
 			return null;
 		}
 
 		private IResource getUnderlyingResource() {
 			Object input = getInput();
 			if (input instanceof ISourceModule) {
 				ISourceModule cu = (ISourceModule) input;
 				cu = cu.getPrimary();
 				return cu.getResource();
 			} /*
 				 * else if (input instanceof IClassFile) { return ((IClassFile)
 				 * input).getResource(); }
 				 */
 			return null;
 		}
 
 		/*
 		 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
 		 */
 		protected void handleLabelProviderChanged(
 				LabelProviderChangedEvent event) {
 			Object input = getInput();
 			if (event instanceof ProblemsLabelChangedEvent) {
 				ProblemsLabelChangedEvent e = (ProblemsLabelChangedEvent) event;
 				if (e.isMarkerChange() && input instanceof ISourceModule) {
 					return; // marker changes can be ignored
 				}
 			}
 			// look if the underlying resource changed
 			Object[] changed = event.getElements();
 			if (changed != null) {
 				IResource resource = getUnderlyingResource();
 				if (resource != null) {
 					for (int i = 0; i < changed.length; i++) {
 						if (changed[i] != null && changed[i].equals(resource)) {
 							// change event to a full refresh
 							event = new LabelProviderChangedEvent(
 									(IBaseLabelProvider) event.getSource());
 							break;
 						}
 					}
 				}
 			}
 			super.handleLabelProviderChanged(event);
 		}
 
 		/*
 		 * @see TreeViewer#internalExpandToLevel
 		 */
 		protected void internalExpandToLevel(Widget node, int level) {
 			if (node instanceof Item) {
 				Item i = (Item) node;
 				if (i.getData() instanceof IModelElement) {
 					IModelElement je = (IModelElement) i.getData();
 					if (/*
 						 * je.getElementType() == IModelElement.IMPORT_CONTAINER ||
 						 */isInnerType(je)) {
 						if (i != fReusedExpandedItem) {
 							setExpanded(i, false);
 							return;
 						}
 					}
 				}
 			}
 			super.internalExpandToLevel(node, level);
 		}
 
 		/*
 		 * @see org.eclipse.jface.viewers.AbstractTreeViewer#isExpandable(java.lang.Object)
 		 */
 		public boolean isExpandable(Object element) {
 			if (hasFilters()) {
 				return getFilteredChildren(element).length > 0;
 			}
 			return super.isExpandable(element);
 		}
 
 		protected boolean mustUpdateParent(IModelElementDelta delta,
 				IModelElement element) {
 			return false;
 		}
 
 		protected boolean overlaps(ISourceRange range, int start, int end) {
 			return start <= (range.getOffset() + range.getLength() - 1)
 					&& range.getOffset() <= end;
 		}
 
 		/**
 		 * Investigates the given element change event and if affected
 		 * incrementally updates the Java outline.
 		 * 
 		 * @param delta
 		 *            the Java element delta used to reconcile the Java outline
 		 */
 		public void reconcile(IModelElementDelta delta) {
 			fReorderedMembers = false;
 			fForceFireSelectionChanged = false;
 			if (getComparator() == null) {
 
 				Widget w = findItem(fInput);
 				if (w != null && !w.isDisposed())
 					update(w, delta);
 				if (fForceFireSelectionChanged)
 					fireSelectionChanged(new SelectionChangedEvent(getSite()
 							.getSelectionProvider(), this.getSelection()));
 				if (fReorderedMembers) {
 					refresh(false);
 					fReorderedMembers = false;
 				}
 
 			} else {
 				// just for now
 				refresh(true);
 			}
 		}
 
 		protected void reuseTreeItem(Item item, Object element) {
 
 			// remove children
 			Item[] c = getChildren(item);
 			if (c != null && c.length > 0) {
 
 				if (getExpanded(item))
 					fReusedExpandedItem = item;
 
 				for (int k = 0; k < c.length; k++) {
 					if (c[k].getData() != null)
 						disassociate(c[k]);
 					c[k].dispose();
 				}
 			}
 
 			updateItem(item, element);
 			updatePlus(item, element);
 			internalExpandToLevel(item, ALL_LEVELS);
 
 			fReusedExpandedItem = null;
 			fForceFireSelectionChanged = true;
 		}
 
 		protected void update(Widget w, IModelElementDelta delta) {
 
 			Item item;
 
 			IModelElement parent = delta.getElement();
 			IModelElementDelta[] affected = delta.getAffectedChildren();
 			Item[] children = getChildren(w);
 
 			boolean doUpdateParent = false;
 			boolean doUpdateParentsPlus = false;
 
 			Vector deletions = new Vector();
 			Vector additions = new Vector();
 
 			for (int i = 0; i < affected.length; i++) {
 				IModelElementDelta affectedDelta = affected[i];
 				IModelElement affectedElement = affectedDelta.getElement();
 				int status = affected[i].getKind();
 
 				// find tree item with affected element
 				int j;
 				for (j = 0; j < children.length; j++)
 					if (affectedElement.equals(children[j].getData()))
 						break;
 
 				if (j == children.length) {
 					// remove from collapsed parent
 					if ((status & IModelElementDelta.REMOVED) != 0) {
 						doUpdateParentsPlus = true;
 						continue;
 					}
 					// addition
 					if ((status & IModelElementDelta.CHANGED) != 0
 							&& (affectedDelta.getFlags() & IModelElementDelta.F_MODIFIERS) != 0
 							&& !filtered(parent, affectedElement)) {
 						additions.addElement(affectedDelta);
 					}
 					continue;
 				}
 
 				item = children[j];
 
 				// removed
 				if ((status & IModelElementDelta.REMOVED) != 0) {
 					deletions.addElement(item);
 					doUpdateParent = doUpdateParent
 							|| mustUpdateParent(affectedDelta, affectedElement);
 
 					// changed
 				} else if ((status & IModelElementDelta.CHANGED) != 0) {
 					int change = affectedDelta.getFlags();
 					doUpdateParent = doUpdateParent
 							|| mustUpdateParent(affectedDelta, affectedElement);
 
 					if ((change & IModelElementDelta.F_MODIFIERS) != 0) {
 						if (filtered(parent, affectedElement))
 							deletions.addElement(item);
 						else
 							updateItem(item, affectedElement);
 					}
 
 					if ((change & IModelElementDelta.F_CONTENT) != 0)
 						updateItem(item, affectedElement);
 
 					// if ((change & IModelElementDelta.F_CATEGORIES) != 0)
 					// updateItem(item, affectedElement);
 
 					if ((change & IModelElementDelta.F_CHILDREN) != 0)
 						update(item, affectedDelta);
 
 					if ((change & IModelElementDelta.F_REORDER) != 0)
 						fReorderedMembers = true;
 				}
 			}
 
 			// find all elements to add
 			IModelElementDelta[] add = delta.getAddedChildren();
 			if (additions.size() > 0) {
 				IModelElementDelta[] tmp = new IModelElementDelta[add.length
 						+ additions.size()];
 				System.arraycopy(add, 0, tmp, 0, add.length);
 				for (int i = 0; i < additions.size(); i++)
 					tmp[i + add.length] = (IModelElementDelta) additions
 							.elementAt(i);
 				add = tmp;
 			}
 
 			// add at the right position
 			go2: for (int i = 0; i < add.length; i++) {
 
 				try {
 
 					IModelElement e = add[i].getElement();
 					if (filtered(parent, e))
 						continue go2;
 
 					doUpdateParent = doUpdateParent
 							|| mustUpdateParent(add[i], e);
 					ISourceRange rng = getSourceRange(e);
 					int start = rng.getOffset();
 					int end = start + rng.getLength() - 1;
 					int nameOffset = Integer.MAX_VALUE;
 					if (e instanceof IField) {
 						ISourceRange nameRange = ((IField) e).getNameRange();
 						if (nameRange != null)
 							nameOffset = nameRange.getOffset();
 					}
 
 					Item last = null;
 					item = null;
 					children = getChildren(w);
 
 					for (int j = 0; j < children.length; j++) {
 						item = children[j];
 						IModelElement r = (IModelElement) item.getData();
 
 						if (r == null) {
 							// parent node collapsed and not be opened before ->
 							// do nothing
 							continue go2;
 						}
 
 						try {
 							rng = getSourceRange(r);
 
 							// multi-field declarations always start at
 							// the same offset. They also have the same
 							// end offset if the field sequence is terminated
 							// with a semicolon. If not, the source range
 							// ends behind the identifier / initializer
 							// see
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=51851
 							boolean multiFieldDeclaration = r.getElementType() == IModelElement.FIELD
 									&& e.getElementType() == IModelElement.FIELD
 									&& rng.getOffset() == start;
 
 							// elements are inserted by occurrence
 							// however, multi-field declarations have
 							// equal source ranges offsets, therefore we
 							// compare name-range offsets.
 							boolean multiFieldOrderBefore = false;
 							if (multiFieldDeclaration) {
 								if (r instanceof IField) {
 									ISourceRange nameRange = ((IField) r)
 											.getNameRange();
 									if (nameRange != null) {
 										if (nameRange.getOffset() > nameOffset)
 											multiFieldOrderBefore = true;
 									}
 								}
 							}
 
 							if (!multiFieldDeclaration
 									&& overlaps(rng, start, end)) {
 
 								// be tolerant if the delta is not correct, or
 								// if
 								// the tree has been updated other than by a
 								// delta
 								reuseTreeItem(item, e);
 								continue go2;
 
 							} else if (multiFieldOrderBefore
 									|| rng.getOffset() > start) {
 
 								if (last != null && deletions.contains(last)) {
 									// reuse item
 									deletions.removeElement(last);
 									reuseTreeItem(last, e);
 								} else {
 									// nothing to reuse
 									createTreeItem(w, e, j);
 								}
 								continue go2;
 							}
 
 						} catch (ModelException x) {
 							// stumbled over deleted element
 						}
 
 						last = item;
 					}
 
 					// add at the end of the list
 					if (last != null && deletions.contains(last)) {
 						// reuse item
 						deletions.removeElement(last);
 						reuseTreeItem(last, e);
 					} else {
 						// nothing to reuse
 						createTreeItem(w, e, -1);
 					}
 
 				} catch (ModelException x) {
 					// the element to be added is not present -> don't add it
 				}
 			}
 
 			// remove items which haven't been reused
 			Enumeration e = deletions.elements();
 			while (e.hasMoreElements()) {
 				item = (Item) e.nextElement();
 				disassociate(item);
 				item.dispose();
 			}
 
 			if (doUpdateParent)
 				updateItem(w, delta.getElement());
 			if (!doUpdateParent && doUpdateParentsPlus && w instanceof Item)
 				updatePlus((Item) w, delta.getElement());
 		}
 
 	}
 
 	class LexicalSortingAction extends Action {
 
 		private ModelElementSorter fComparator = new ModelElementSorter();
 		private SourcePositionSorter fSourcePositonComparator = new SourcePositionSorter();
 
 		public LexicalSortingAction() {
 			super();
 			// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
 			// IJavaHelpContextIds.LEXICAL_SORTING_OUTLINE_ACTION);
 			setText(DLTKEditorMessages.ScriptOutlinePage_Sort_label);
 			DLTKPluginImages.setLocalImageDescriptors(this,
 					"alphab_sort_co.gif"); //$NON-NLS-1$
 			setToolTipText(DLTKEditorMessages.ScriptOutlinePage_Sort_tooltip);
 			setDescription(DLTKEditorMessages.ScriptOutlinePage_Sort_description);
 
 			boolean checked = fStore
 					.getBoolean("LexicalSortingAction.isChecked"); //$NON-NLS-1$
 			valueChanged(checked, false);
 		}
 
 		public void run() {
 			valueChanged(isChecked(), true);
 		}
 
 		private void valueChanged(final boolean on, boolean store) {
 			setChecked(on);
 			BusyIndicator.showWhile(fOutlineViewer.getControl().getDisplay(),
 					new Runnable() {
 						public void run() {
 							if (on)
 								fOutlineViewer.setComparator(fComparator);
 							else
 								fOutlineViewer
 										.setComparator(fSourcePositonComparator);
 						}
 					});
 
 			if (store)
 				DLTKUIPlugin.getDefault().getPreferenceStore().setValue(
 						"LexicalSortingAction.isChecked", on); //$NON-NLS-1$
 		}
 	}
 
 	static class NoClassElement extends WorkbenchAdapter implements IAdaptable {
 		/*
 		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
 		 */
 		public Object getAdapter(Class clas) {
 			if (clas == IWorkbenchAdapter.class)
 				return this;
 			return null;
 		}
 
 		/*
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString() {
 			return DLTKEditorMessages.ScriptOutlinePage_error_NoTopLevelType;
 		}
 	}
 
 	/**
 	 * This action toggles whether this Java Outline page links its selection to
 	 * the active editor.
 	 * 
 	 * @since 3.0
 	 */
 	public class ToggleLinkingAction extends AbstractToggleLinkingAction {
 
 		ScriptOutlinePage fJavaOutlinePage;
 
 		/**
 		 * Constructs a new action.
 		 * 
 		 * @param outlinePage
 		 *            the Java outline page
 		 */
 		public ToggleLinkingAction(ScriptOutlinePage outlinePage) {
 			boolean isLinkingEnabled = DLTKUIPlugin
 					.getDefault()
 					.getPreferenceStore()
 					.getBoolean(
 							PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE);
 			setChecked(isLinkingEnabled);
 			fJavaOutlinePage = outlinePage;
 		}
 
 		/**
 		 * Runs the action.
 		 */
 		public void run() {
 			DLTKUIPlugin.getDefault().getPreferenceStore().setValue(
 					PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE,
 					isChecked());
 			if (isChecked() && fEditor != null)
 				fEditor.synchronizeOutlinePage(fEditor
 						.computeHighlightRangeSourceReference(), false);
 		}
 
 	}
 
 	static Object[] NO_CHILDREN = new Object[0];
 
 	/** A flag to show contents of top level type only */
 	// private boolean fTopLevelTypeOnly;
 	private IModelElement fInput;
 	private String fContextMenuID;
 	private Menu fMenu;
 	protected ScriptOutlineViewer fOutlineViewer;
 	private ScriptEditor fEditor;
 	protected IPreferenceStore fStore;
 	private MemberFilterActionGroup fMemberFilterActionGroup;
 
 	private ListenerList fSelectionChangedListeners = new ListenerList(
 			ListenerList.IDENTITY);
 	private ListenerList fPostSelectionChangedListeners = new ListenerList(
 			ListenerList.IDENTITY);
 	private Hashtable fActions = new Hashtable();
 
 	private TogglePresentationAction fTogglePresentation;
 
 	private ToggleLinkingAction fToggleLinkingAction;
 
 	private CompositeActionGroup fActionGroups;
 
 	private IPropertyChangeListener fPropertyChangeListener;
 	/**
 	 * Custom filter action group.
 	 * 
 	 * @since 3.0
 	 */
 	private CustomFiltersActionGroup fCustomFiltersActionGroup;
 
 	// /**
 	// * Category filter action group.
 	// *
 	// * @since 3.2
 	// */
 	// private CategoryFilterActionGroup fCategoryFilterActionGroup;
 
 	public ScriptOutlinePage(ScriptEditor editor, IPreferenceStore store) {
 		super();
 
 		Assert.isNotNull(editor);
 
 		fContextMenuID = "#CompilationUnitOutlinerContext";// contextMenuID;
 		fEditor = editor;
 		fStore = store;
 
 		fTogglePresentation = new TogglePresentationAction();
 		fTogglePresentation.setEditor(editor);
 
 		fPropertyChangeListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				doPropertyChange(event);
 			}
 		};
 		fStore.addPropertyChangeListener(fPropertyChangeListener);
 	}
 
 	/**
 	 * Convenience method to add the action installed under the given actionID
 	 * to the specified group of the menu.
 	 * 
 	 * @param menu
 	 *            the menu manager
 	 * @param group
 	 *            the group to which to add the action
 	 * @param actionID
 	 *            the ID of the new action
 	 */
 	protected void addAction(IMenuManager menu, String group, String actionID) {
 		IAction action = getAction(actionID);
 		if (action != null) {
 			if (action instanceof IUpdate)
 				((IUpdate) action).update();
 
 			if (action.isEnabled()) {
 				IMenuManager subMenu = menu.findMenuUsingPath(group);
 				if (subMenu != null)
 					subMenu.add(action);
 				else
 					menu.appendToGroup(group, action);
 			}
 		}
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.IPostSelectionProvider#addPostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addPostSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		if (fOutlineViewer != null)
 			fOutlineViewer.addPostSelectionChangedListener(listener);
 		else
 			fPostSelectionChangedListeners.add(listener);
 	}
 
 	/*
 	 * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		if (fOutlineViewer != null)
 			fOutlineViewer.addSelectionChangedListener(listener);
 		else
 			fSelectionChangedListeners.add(listener);
 	}
 
 	protected void contextMenuAboutToShow(IMenuManager menu) {
 
 		// DLTKUIPlugin.createStandardGroups(menu);
 		if (menu.isEmpty()) {
 			// menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
 			menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
 			menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
 			menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
 			menu.add(new Separator(ICommonMenuConstants.GROUP_EDIT));
 			// menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
 			// menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
 			menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
 			// menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
 			// menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
 			// menu.add(new
 			// Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
 			menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
 		}
 
 		IStructuredSelection selection = (IStructuredSelection) getSelection();
 		fActionGroups.setContext(new ActionContext(selection));
 		fActionGroups.fillContextMenu(menu);
 	}
 	
 	protected ILabelDecorator getLabelDecorator() {
 		return null;
 	}
 
 	/*
 	 * @see IPage#createControl
 	 */
 	public void createControl(Composite parent) {
 
 		Tree tree = new Tree(parent, SWT.MULTI);
 
 		AppearanceAwareLabelProvider lprovider = new AppearanceAwareLabelProvider(
 				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS
 						| ScriptElementLabels.F_APP_TYPE_SIGNATURE
 						| ScriptElementLabels.ALL_CATEGORY,
 				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS, fStore);
 		
 		ILabelDecorator ldecorator = getLabelDecorator();
 		if (ldecorator != null)
 			lprovider.addLabelDecorator(ldecorator);
 
 		fOutlineViewer = new ScriptOutlineViewer(tree);
 		initDragAndDrop();
 		fOutlineViewer.setContentProvider(new ChildrenProvider());
 		fOutlineViewer.setLabelProvider(new DecoratingModelLabelProvider(
				lprovider));		
 
 		Object[] listeners = fSelectionChangedListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			fSelectionChangedListeners.remove(listeners[i]);
 			fOutlineViewer
 					.addSelectionChangedListener((ISelectionChangedListener) listeners[i]);
 		}
 
 		listeners = fPostSelectionChangedListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			fPostSelectionChangedListeners.remove(listeners[i]);
 			fOutlineViewer
 					.addPostSelectionChangedListener((ISelectionChangedListener) listeners[i]);
 		}
 
 		// MenuManager manager = new MenuManager(fContextMenuID,
 		// fContextMenuID);
 		// manager.setRemoveAllWhenShown(true);
 		// manager.addMenuListener(new IMenuListener() {
 		// public void menuAboutToShow(IMenuManager m) {
 		// contextMenuAboutToShow(m);
 		// }
 		// });
 		// fMenu = manager.createContextMenu(tree);
 		// tree.setMenu(fMenu);
 
 		IPageSite site = getSite();
 		// site
 		// .registerContextMenu(
 		// DLTKUIPlugin.getPluginId() + ".outline", manager, fOutlineViewer);
 		// //$NON-NLS-1$
 
 		updateSelectionProvider(site);
 
 		IDLTKLanguageToolkit toolkit = fEditor.getLanguageToolkit();
 		// we must create the groups after we have set the selection provider to
 		// the site
 		fActionGroups = new CompositeActionGroup(new ActionGroup[] {
 				new OpenViewActionGroup(this),
 				// new CCPActionGroup(this),
 				/* new GenerateActionGroup(this), */
 				/*
 				 * new RefactorActionGroup( this),
 				 */
 				new DLTKSearchActionGroup(this, toolkit) });
 
 		// register global actions
 		IActionBars actionBars = site.getActionBars();
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO,
 				fEditor.getAction(ITextEditorActionConstants.UNDO));
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.REDO,
 				fEditor.getAction(ITextEditorActionConstants.REDO));
 
 		IAction action = fEditor.getAction(ITextEditorActionConstants.NEXT);
 		actionBars.setGlobalActionHandler(
 				ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT,
 				action);
 		action = fEditor.getAction(ITextEditorActionConstants.PREVIOUS);
 		actionBars
 				.setGlobalActionHandler(
 						ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION,
 						action);
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS,
 				action);
 
 		actionBars
 				.setGlobalActionHandler(
 						ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY,
 						fTogglePresentation);
 
 		fActionGroups.fillActionBars(actionBars);
 
 		IStatusLineManager statusLineManager = actionBars
 				.getStatusLineManager();
 		if (statusLineManager != null) {
 			StatusBarUpdater updater = new StatusBarUpdater(statusLineManager);
 			fOutlineViewer.addPostSelectionChangedListener(updater);
 		}
 		// Custom filter group
 		fCustomFiltersActionGroup = new CustomFiltersActionGroup(
 				"org.eclipse.dltk.ui.ScriptOutlinePage", fOutlineViewer); //$NON-NLS-1$
 
 		registerToolbarActions(actionBars);
 
 		fOutlineViewer.setInput(fInput);
 	}
 
 	public void dispose() {
 
 		if (fEditor == null)
 			return;
 
 		if (fMemberFilterActionGroup != null) {
 			fMemberFilterActionGroup.dispose();
 			fMemberFilterActionGroup = null;
 		}
 
 		// if (fCategoryFilterActionGroup != null) {
 		// fCategoryFilterActionGroup.dispose();
 		// fCategoryFilterActionGroup = null;
 		// }
 
 		if (fCustomFiltersActionGroup != null) {
 			fCustomFiltersActionGroup.dispose();
 			fCustomFiltersActionGroup = null;
 		}
 
 		fEditor.outlinePageClosed();
 		fEditor = null;
 
 		fSelectionChangedListeners.clear();
 		fSelectionChangedListeners = null;
 
 		fPostSelectionChangedListeners.clear();
 		fPostSelectionChangedListeners = null;
 
 		if (fPropertyChangeListener != null) {
 			fStore.removePropertyChangeListener(fPropertyChangeListener);
 			fPropertyChangeListener = null;
 		}
 
 		if (fMenu != null && !fMenu.isDisposed()) {
 			fMenu.dispose();
 			fMenu = null;
 		}
 
 		if (fActionGroups != null)
 			fActionGroups.dispose();
 
 		fTogglePresentation.setEditor(null);
 
 		fOutlineViewer = null;
 
 		super.dispose();
 	}
 
 	private void doPropertyChange(PropertyChangeEvent event) {
 		if (fOutlineViewer != null) {
 			if (MembersOrderPreferenceCache.isMemberOrderProperty(event
 					.getProperty())) {
 				fOutlineViewer.refresh(false);
 			}
 		}
 	}
 
 	public IAction getAction(String actionID) {
 		Assert.isNotNull(actionID);
 		return (IAction) fActions.get(actionID);
 	}
 
 	/*
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class key) {
 		if (key == IShowInSource.class) {
 			return getShowInSource();
 		}
 		if (key == IShowInTargetList.class) {
 			return new IShowInTargetList() {
 				public String[] getShowInTargetIds() {
 					return new String[] { DLTKUIPlugin.ID_SCRIPTEXPLORER };
 				}
 
 			};
 		}
 		if (key == IShowInTarget.class) {
 			return getShowInTarget();
 		}
 
 		return null;
 	}
 
 	public Control getControl() {
 		if (fOutlineViewer != null)
 			return fOutlineViewer.getControl();
 		return null;
 	}
 
 	/**
 	 * Returns the <code>JavaOutlineViewer</code> of this view.
 	 * 
 	 * @return the {@link ScriptOutlineViewer}
 	 * @since 3.3
 	 */
 	protected final ScriptOutlineViewer getOutlineViewer() {
 		return fOutlineViewer;
 	}
 
 	/*
 	 * @see ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		if (fOutlineViewer == null)
 			return StructuredSelection.EMPTY;
 		return fOutlineViewer.getSelection();
 	}
 
 	/**
 	 * Returns the <code>IShowInSource</code> for this view.
 	 * 
 	 * @return the {@link IShowInSource}
 	 */
 	protected IShowInSource getShowInSource() {
 		return new IShowInSource() {
 			public ShowInContext getShowInContext() {
 				return new ShowInContext(null, getSite().getSelectionProvider()
 						.getSelection());
 			}
 		};
 	}
 
 	/**
 	 * Returns the <code>IShowInTarget</code> for this view.
 	 * 
 	 * @return the {@link IShowInTarget}
 	 */
 	protected IShowInTarget getShowInTarget() {
 		return new IShowInTarget() {
 			public boolean show(ShowInContext context) {
 				ISelection sel = context.getSelection();
 				if (sel instanceof ITextSelection) {
 					ITextSelection tsel = (ITextSelection) sel;
 					int offset = tsel.getOffset();
 					IModelElement element = fEditor.getElementAt(offset);
 					if (element != null) {
 						setSelection(new StructuredSelection(element));
 						return true;
 					}
 				} else if (sel instanceof IStructuredSelection) {
 					setSelection(sel);
 					return true;
 				}
 				return false;
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on Page
 	 */
 	public void init(IPageSite pageSite) {
 		super.init(pageSite);
 	}
 
 	private void initDragAndDrop() {
 		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer
 				.getInstance() };
 
 		// Drop Adapter
 		TransferDropTargetListener[] dropListeners = new TransferDropTargetListener[] { new SelectionTransferDropAdapter(
 				fOutlineViewer) };
 		fOutlineViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers,
 				new DelegatingDropAdapter(dropListeners));
 
 		// Drag Adapter
 		TransferDragSourceListener[] dragListeners = new TransferDragSourceListener[] { new SelectionTransferDragAdapter(
 				fOutlineViewer) };
 		fOutlineViewer.addDragSupport(ops, transfers,
 				new DLTKViewerDragAdapter(fOutlineViewer, dragListeners));
 	}
 
 	/**
 	 * Checks whether a given Java element is an inner type.
 	 * 
 	 * @param element
 	 *            the java element
 	 * @return <code>true</code> iff the given element is an inner type
 	 */
 	private boolean isInnerType(IModelElement element) {
 
 		if (element != null && element.getElementType() == IModelElement.TYPE) {
 
 			IModelElement parent = element.getParent();
 			if (parent != null) {
 				int parentElementType = parent.getElementType();
 				return (parentElementType != IModelElement.SOURCE_MODULE);
 			}
 		}
 
 		return false;
 	}
 
 	protected void registerSpecialToolbarActions(IActionBars actionBars) {
 		// derived classes could implement it
 	}
 	
 	private void registerToolbarActions(IActionBars actionBars) {
 		IToolBarManager toolBarManager = actionBars.getToolBarManager();
 		toolBarManager.add(new LexicalSortingAction());
 
 		fMemberFilterActionGroup = new MemberFilterActionGroup(fOutlineViewer,
 				fStore); //$NON-NLS-1$
 		fMemberFilterActionGroup.contributeToToolBar(toolBarManager);
 
 		fCustomFiltersActionGroup.fillActionBars(actionBars);
 		
 		registerSpecialToolbarActions(actionBars);
 
 		IMenuManager viewMenuManager = actionBars.getMenuManager();
 		viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
 
 		fToggleLinkingAction = new ToggleLinkingAction(this);
 		// viewMenuManager.add(new ClassOnlyAction());
 		viewMenuManager.add(fToggleLinkingAction);
 
 		// fCategoryFilterActionGroup = new CategoryFilterActionGroup(
 		// fOutlineViewer,
 		// "org.eclipse.jdt.ui.JavaOutlinePage", new IModelElement[] { fInput
 		// }); //$NON-NLS-1$
 		// fCategoryFilterActionGroup.contributeToViewMenu(viewMenuManager);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.IPostSelectionProvider#removePostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removePostSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		if (fOutlineViewer != null)
 			fOutlineViewer.removePostSelectionChangedListener(listener);
 		else
 			fPostSelectionChangedListeners.remove(listener);
 	}
 
 	/*
 	 * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		if (fOutlineViewer != null)
 			fOutlineViewer.removeSelectionChangedListener(listener);
 		else
 			fSelectionChangedListeners.remove(listener);
 	}
 
 	public void select(ISourceReference reference) {
 		if (fOutlineViewer != null) {
 
 			ISelection s = fOutlineViewer.getSelection();
 			if (s instanceof IStructuredSelection) {
 				IStructuredSelection ss = (IStructuredSelection) s;
 				List elements = ss.toList();
 				if (!elements.contains(reference)) {
 					s = (reference == null ? StructuredSelection.EMPTY
 							: new StructuredSelection(reference));
 					fOutlineViewer.setSelection(s, true);
 				}
 			}
 		}
 	}
 
 	public void setAction(String actionID, IAction action) {
 		Assert.isNotNull(actionID);
 		if (action == null)
 			fActions.remove(actionID);
 		else
 			fActions.put(actionID, action);
 	}
 
 	/*
 	 * @see Page#setFocus()
 	 */
 	public void setFocus() {
 		if (fOutlineViewer != null)
 			fOutlineViewer.getControl().setFocus();
 	}
 
 	public void setInput(IModelElement inputElement) {
 		fInput = inputElement;
 		if (fOutlineViewer != null) {
 			fOutlineViewer.setInput(fInput);
 			updateSelectionProvider(getSite());
 		}
 		// if (fCategoryFilterActionGroup != null)
 		// fCategoryFilterActionGroup.setInput(new IModelElement[] { fInput });
 	}
 
 	/*
 	 * @see ISelectionProvider#setSelection(ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		if (fOutlineViewer != null)
 			fOutlineViewer.setSelection(selection);
 	}
 
 	/*
 	 * @since 3.2
 	 */
 	private void updateSelectionProvider(IPageSite site) {
 		ISelectionProvider provider = fOutlineViewer;
 		if (fInput != null) {
 			ISourceModule cu = (ISourceModule) fInput
 					.getAncestor(IModelElement.SOURCE_MODULE);
 			if (cu != null && !DLTKModelUtil.isPrimary(cu))
 				provider = new EmptySelectionProvider();
 		}
 		site.setSelectionProvider(provider);
 	}
 }
