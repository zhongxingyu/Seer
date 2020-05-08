 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import java.util.Arrays;
 
 import org.eclipse.compare.INavigatable;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Item;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class Navigatable implements INavigatable {
 
 	private final AdapterFactory adapterFactory;
 
 	private final WrappableTreeViewer viewer;
 
 	/**
 	 * @param adapterFactory
 	 */
 	public Navigatable(AdapterFactory adapterFactory, WrappableTreeViewer viewer) {
 		this.adapterFactory = adapterFactory;
 		this.viewer = viewer;
 	}
 
 	public boolean selectChange(int flag) {
 		Object nextOrPrev = null;
 		Item[] selection = viewer.getSelection(viewer.getTree());
 		Item firstSelectedItem = selection.length > 0 ? selection[0] : null;
 		switch (flag) {
 			case NEXT_CHANGE:
 				nextOrPrev = getNextDiff(firstSelectedItem);
 				break;
 			case PREVIOUS_CHANGE:
 				nextOrPrev = getPreviousDiff(firstSelectedItem);
 				break;
 			case FIRST_CHANGE:
 				nextOrPrev = getNextDiff(null);
 				break;
 			case LAST_CHANGE:
 				nextOrPrev = getPreviousDiff(null);
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 
 		if (nextOrPrev != null) {
 			StructuredSelection newSelection = new StructuredSelection(nextOrPrev);
 			viewer.setSelection(newSelection);
 			viewer.fireOpen(new OpenEvent(viewer, newSelection));
 		}
 
 		return nextOrPrev == null;
 
 	}
 
 	public Object getInput() {
 		return viewer.getInput();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.INavigatable#openSelectedChange()
 	 */
 	public boolean openSelectedChange() {
 		ISelection selection = viewer.getSelection();
 		if (selection.isEmpty()) {
 			return false;
 		} else {
 			viewer.fireOpen(new OpenEvent(viewer, selection));
 			return true;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.INavigatable#hasChange(int)
 	 */
 	public boolean hasChange(int changeFlag) {
 		Item[] selection = viewer.getSelection(viewer.getTree());
 		Item firstSelectedItem = selection.length > 0 ? selection[0] : null;
 		switch (changeFlag) {
 			case NEXT_CHANGE:
 				return getNextDiff(firstSelectedItem) != null;
 			case PREVIOUS_CHANGE:
 				return getPreviousDiff(firstSelectedItem) != null;
 			default:
 				throw new IllegalStateException();
 		}
 	}
 
 	/**
 	 * Returns, from the given TreeNode, the next TreeNode that contains a diff.
 	 * 
 	 * @param treeNode
 	 *            the given TreeNode for which we want to find the next.
 	 * @return the next TreeNode that contains a diff.
 	 */
 	private Object getNextDiff(Item item) {
 		Object ret = null;
 
 		Item[] children = getChildren(item);
 		ret = getFirstDiffChild(children);
 
 		if (ret == null) {
 			ret = getNextSiblingDiff(item);
 		}
 
 		return ret;
 	}
 
 	private Object getPreviousDiff(Item item) {
 		Object ret = null;
 
 		if (item == null) {
 			ret = getDeepestDiffChild(null);
 		} else {
 			ret = getPreviousSiblingDeepestDiff(item);
 		}
 
 		return ret;
 	}
 
 	private Object getNextSiblingDiff(Item item) {
		if (item == null) {
			return null;
		}
 		Object ret = null;
 		Item parentItem = viewer.getParentItem(item);
 		final Item[] siblings = getChildren(parentItem);
 		int indexOfItem = Arrays.asList(siblings).indexOf(item);
 		if (indexOfItem + 1 < siblings.length) {
 			for (int i = indexOfItem + 1; i < siblings.length && ret == null; i++) {
 				Item followingSibling = siblings[i];
 				ret = getDataOrNextDiff(followingSibling);
 			}
 		} else if (parentItem != null) {
 			ret = getNextSiblingDiff(parentItem);
 		}
 		return ret;
 	}
 
 	private Object getPreviousSiblingDeepestDiff(Item item) {
		if (item == null) {
			return null;
		}
 		Object ret = null;
 		Item parentItem = viewer.getParentItem(item);
 		final Item[] siblings = getChildren(parentItem);
 		int indexOfItem = Arrays.asList(siblings).indexOf(item);
 		if (indexOfItem - 1 >= 0) {
 			for (int i = indexOfItem - 1; i >= 0 && ret == null; i--) {
 				Item previousSibling = siblings[i];
 				ret = getDeepestDiffChild(previousSibling);
 			}
 		} else if (parentItem != null) {
 			EObject eObject = EMFCompareStructureMergeViewer.getDataOfTreeNodeOfAdapter(parentItem.getData());
 			if (eObject instanceof Diff) {
 				ret = parentItem.getData();
 			} else {
 				ret = getPreviousDiff(parentItem);
 			}
 		}
 		return ret;
 	}
 
 	private Object getDataOrNextDiff(Item item) {
 		Object ret;
 		EObject eObject = EMFCompareStructureMergeViewer.getDataOfTreeNodeOfAdapter(item.getData());
 		if (eObject instanceof Diff) {
 			ret = item.getData();
 		} else {
 			ret = getNextDiff(item);
 		}
 		return ret;
 	}
 
 	private Object getFirstDiffChild(Item[] children) {
 		Object ret = null;
 		for (int i = 0; i < children.length && ret == null; i++) {
 			Item child = children[i];
 			ret = getDataOrNextDiff(child);
 		}
 		return ret;
 	}
 
 	private Item[] getChildren(Item item) {
 		final Item[] children;
 		if (item != null) {
 			children = viewer.getChildren(item);
 		} else {
 			children = viewer.getChildren(viewer.getTree());
 		}
 		return children;
 	}
 
 	private Object getDeepestDiffChild(Item item) {
 		Object ret = null;
 		Item[] children = getChildren(item);
 		for (int i = children.length - 1; i >= 0 && ret == null; i--) {
 			Item child = children[i];
 			ret = getDeepestDiffChild(child);
 			if (ret == null) {
 				EObject eObject = EMFCompareStructureMergeViewer.getDataOfTreeNodeOfAdapter(child.getData());
 				if (eObject instanceof Diff) {
 					ret = child.getData();
 				}
 			}
 		}
 		if (ret == null) {
 			EObject eObject = EMFCompareStructureMergeViewer.getDataOfTreeNodeOfAdapter(item.getData());
 			if (eObject instanceof Diff) {
 				ret = item.getData();
 			}
 		}
 
 		return ret;
 	}
 }
