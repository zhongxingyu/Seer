 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
 import org.eclipse.core.databinding.observable.set.ISetChangeListener;
 import org.eclipse.core.databinding.observable.set.SetChangeEvent;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionObservableList;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree.IObservableTreeModel;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Ridget for SWT {@link Tree} widgets.
  */
 public class TreeRidget extends AbstractSelectableRidget implements ITreeRidget {
 
 	private final SelectionListener selectionTypeEnforcer;
 	private final DoubleClickForwarder doubleClickForwarder;
 
 	private Collection<IActionListener> doubleClickListeners;
 	private DataBindingContext dbc;
 	private TreeViewer viewer;
 
 	private Object treeRoot;
 	private Class<? extends Object> treeElementClass;
 	private String childrenAccessor;
 	private String valueAccessor;
 
 	public TreeRidget() {
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		doubleClickForwarder = new DoubleClickForwarder();
 		// observableValues = new WritableList();
 	}
 
 	@Override
 	protected void bindUIControl() {
 		Tree control = getUIControl();
 		if (control != null && treeRoot != null) {
 			bindToViewer(control);
 			bindToSelection();
 			control.addSelectionListener(selectionTypeEnforcer);
 			control.addMouseListener(doubleClickForwarder);
 			expand(treeRoot); // TODO [ev] restoreExpansionState
 		}
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Tree.class);
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		if (dbc != null) {
 			dbc.dispose();
 			dbc = null;
 		}
 		Tree control = getUIControl();
 		if (control != null) {
 			control.removeSelectionListener(selectionTypeEnforcer);
 			control.removeMouseListener(doubleClickForwarder);
 		}
 		viewer = null;
 	}
 
 	@Override
 	protected List<?> getRowObservables() {
 		List<?> result = null;
 		if (viewer != null) {
 			ObservableListTreeContentProvider cp = (ObservableListTreeContentProvider) viewer.getContentProvider();
 			result = new ArrayList<Object>(cp.getKnownElements());
 		}
 		return result;
 	}
 
 	@Override
 	public Tree getUIControl() {
 		return (Tree) super.getUIControl();
 	}
 
 	public void addDoubleClickListener(IActionListener listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (doubleClickListeners == null) {
 			doubleClickListeners = new ArrayList<IActionListener>();
 		}
 		doubleClickListeners.add(listener);
 	}
 
 	/**
 	 * @deprecated see {@link #bindToModel(Object, Class, String, String)}
 	 */
 	public void bindToModel(IObservableTreeModel observableTreeModel) {
 		throw new UnsupportedOperationException("deprecated");
 	}
 
 	public void bindToModel(Object treeRoot, Class<? extends Object> treeElementClass, String childrenAccessor,
 			String valueAccessor) {
 		Assert.isNotNull(treeRoot);
 		Assert.isNotNull(treeElementClass);
 		Assert.isNotNull(childrenAccessor);
 		Assert.isNotNull(valueAccessor);
 		unbindUIControl();
 		this.treeRoot = treeRoot;
 		this.treeElementClass = treeElementClass;
 		this.childrenAccessor = childrenAccessor;
 		this.valueAccessor = valueAccessor;
 		bindUIControl();
 	}
 
 	/** @deprecated */
 	public void collapse(org.eclipse.riena.ui.ridgets.tree.ITreeNode node) {
 		collapse((Object) node);
 	}
 
 	public void collapse(Object element) {
 		if (viewer != null) {
 			Tree control = getUIControl();
 			control.setRedraw(false);
 			viewer.collapseToLevel(element, 1);
 			viewer.update(element, null); // update icon
 			control.setRedraw(true);
 		}
 	}
 
 	public void collapseTree() {
 		if (viewer != null) {
 			Tree control = getUIControl();
 			control.setRedraw(false);
 			viewer.collapseAll();
 			viewer.refresh(); // update all icons
 			control.setRedraw(true);
 		}
 	}
 
 	/** @deprecated */
 	public void expand(org.eclipse.riena.ui.ridgets.tree.ITreeNode node) {
 		expand((Object) node);
 	}
 
 	public void expand(Object element) {
 		if (viewer != null) {
 			Tree control = getUIControl();
 			control.setRedraw(false);
 			viewer.expandToLevel(element, 1);
 			viewer.update(element, null); // update icon
 			control.setRedraw(true);
 		}
 	}
 
 	public void expandTree() {
 		if (viewer != null) {
 			Tree control = getUIControl();
 			control.setRedraw(false);
 			viewer.expandAll();
 			viewer.refresh(); // update all icons
 			control.setRedraw(true);
 		}
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IObservableTreeModel getRidgetObservable() {
 		throw new UnsupportedOperationException("deprecated");
 	}
 
 	public void removeDoubleClickListener(IActionListener listener) {
 		if (doubleClickListeners != null) {
 			doubleClickListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * This implementation will try to expand the path to the give option, to
 	 * ensure that the corresponding tree element exists.
 	 */
 	public boolean containsOption(Object option) {
 		reveal(new Object[] { option });
 		return super.containsOption(option);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * For each selection candidate in the List <tt>newSelection</tt>, this
 	 * implementation will try to expand the path to the corresponding tree
 	 * node, to ensure that the corresponding tree element is selectable.
 	 */
 	public final void setSelection(List<?> newSelection) {
 		reveal(newSelection.toArray());
 		super.setSelection(newSelection);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * For each selection candidate in the List <tt>newSelection</tt>, this
 	 * implementation will try to expand the path to the corresponding tree
 	 * node, to ensure that the corresponding tree element is selectable.
 	 */
 	public final void setSelection(Object candidate) {
 		if (candidate != null) {
 			reveal(new Object[] { candidate });
 		}
 		super.setSelection(candidate);
 	}
 
 	// helping methods
 	// ////////////////
 
 	/**
 	 * Initialize databining for tree viewer.
 	 */
 	private void bindToViewer(final Tree control) {
 		viewer = new TreeViewer(control);
 		// content
 		Realm realm = Realm.getDefault();
 		IObservableFactory listFactory = BeansObservables.listFactory(realm, childrenAccessor, treeElementClass);
 		// needed for updating the icons on additon / removal (who's the
 		// parent?)
 		final TreeStructureAdvisor structureAdvisor = new TreeStructureAdvisor() {
 			@Override
 			public Object getParent(Object element) {
 				if (element instanceof ITreeNode) {
 					return ((ITreeNode) element).getParent();
 				}
 				return super.getParent(element);
 			}
 		};
 		final TreeContentProvider viewerCP = new TreeContentProvider(listFactory, structureAdvisor);
 
 		viewerCP.getKnownElements().addSetChangeListener(new ISetChangeListener() {
 			// updates the icons on addition / removal
 			public void handleSetChange(SetChangeEvent event) {
 				if (viewerCP.hasInput()) {
 					Set<Object> parents = new HashSet<Object>();
 					for (Object element : event.diff.getAdditions()) {
 						Object parent = structureAdvisor.getParent(element);
 						if (parent != null) {
 							parents.add(parent);
 						}
 					}
 					for (Object element : event.diff.getRemovals()) {
 						Object parent = structureAdvisor.getParent(element);
 						if (parent != null) {
 							parents.add(parent);
 						}
 					}
 					for (Object parent : parents) {
 						if (!viewer.isBusy()) {
 							viewer.update(parent, null);
 						}
 					}
 				}
 			}
 		});
 		// TODO [ev] think about setInput(treeRoot), setInput(new Object[](tR1,
 		// trR2)), setInput(newObject[](tr1)
 		viewer.setContentProvider(viewerCP);
 		// labels
 		IObservableMap attributeMap = BeansObservables.observeMap(viewerCP.getKnownElements(), treeElementClass,
 				valueAccessor);
 		ILabelProvider viewerLP = new TreeRidgetLabelProvider(viewer, attributeMap);
 		viewer.setLabelProvider(viewerLP);
 		// input
 		viewer.setInput(new Object[] { treeRoot });
 	}
 
 	/**
 	 * Initialize databinding related to selection handling (single/multi).
 	 */
 	private void bindToSelection() {
 		StructuredSelection currentSelection = new StructuredSelection(getSelection());
 
 		dbc = new DataBindingContext();
 		IObservableValue viewerSelection = ViewersObservables.observeSingleSelection(viewer);
 		dbc.bindValue(viewerSelection, getSingleSelectionObservable(), new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		IObservableList viewerSelections = new SelectionProviderMultipleSelectionObservableList(dbc
 				.getValidationRealm(), viewer, Object.class);
 		dbc.bindList(viewerSelections, getMultiSelectionObservable(), new UpdateListStrategy(
 				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE));
 
 		viewer.setSelection(currentSelection);
 	}
 
 	/**
 	 * Expand tree paths to candidates before selecting them. This ensures the
 	 * tree items to the candidates are created and the candidates become
 	 * "known elements" (if they exist).
 	 */
 	private void reveal(Object[] candidates) {
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			control.setRedraw(false);
 			try {
 				for (Object candidate : candidates) {
 					viewer.expandToLevel(candidate, 0);
 				}
 			} finally {
 				control.setRedraw(true);
 			}
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Disallows multiple selection is the selection type of the ridget is
 	 * {@link ISelectableRidget.SelectionType#SINGLE}.
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (SelectionType.SINGLE.equals(getSelectionType())) {
 				Tree control = (Tree) e.widget;
 				if (control.getSelectionCount() > 1) {
 					// ignore this event
 					e.doit = false;
 					// set selection one item
 					TreeItem firstItem = control.getSelection()[0];
 					// (
 					// control);
 					control.setSelection(firstItem);
 					// fire event
 					Event event = new Event();
 					event.type = SWT.Selection;
 					event.doit = true;
 					control.notifyListeners(SWT.Selection, event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Notifies doubleClickListeners when the bound widget is double clicked.
 	 */
 	private final class DoubleClickForwarder extends MouseAdapter {
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {
 			if (doubleClickListeners != null) {
 				for (IActionListener listener : doubleClickListeners) {
 					listener.callback();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Extends a standard observable tree content provider with support for:
 	 * <ul>
 	 * <li>handling Object[] <b>and</b> Object input</li> <li>knowing when we
 	 * have a valid input</li>
 	 * </ul>
 	 */
 	private static class TreeContentProvider extends ObservableListTreeContentProvider {
 
 		private boolean hasInput = false;
 
 		TreeContentProvider(IObservableFactory listFactory, TreeStructureAdvisor structureAdvisor) {
 			super(listFactory, structureAdvisor);
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof Object[]) {
 				return (Object[]) inputElement;
 			}
 			return super.getElements(inputElement);
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// this is a workaround to allow our set change listener, which
 			// is in charge triggering an update of the tree icons, to skip the
 			// update when the viewer is in the process of disposing itself
 			// (newInput == null)
			hasInput = (newInput == null);
 			super.inputChanged(viewer, oldInput, newInput);
 		}
 
 		/** Returns true if we have a valid (i.e. non-null) input. */
 		boolean hasInput() {
 			return hasInput;
 		}
 	}
 
 }
