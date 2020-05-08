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
 
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Label provider for TreeViewers that provides different images based on the
  * expansion state of a tree element:
  * <ul>
  * <li>expandable node - collapsed</li>
  * <li>expandable node - expaned</li>
  * <li>leaf (i.e. node with no children)</li>
  *</ul>
  */
 // TODO [ev] unit tests
 final class TreeRidgetLabelProvider extends ObservableMapLabelProvider {
 
 	private static final UpdateIconsTreeListener LISTENER = new UpdateIconsTreeListener();
 
 	private final TreeViewer viewer;
 
 	TreeRidgetLabelProvider(final TreeViewer viewer, IObservableMap[] attributeMap) {
 		super(attributeMap);
 		viewer.getTree().removeTreeListener(LISTENER);
 		viewer.getTree().addTreeListener(LISTENER);
 		this.viewer = viewer;
 	}
 
 	@Override
 	public Image getImage(Object element) {
 		String key = getImageKey(element);
 		return Activator.getSharedImage(key);
 	}
 
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return getImage(element);
		}
		return super.getColumnImage(element, columnIndex);
	}

 	// helping methods
 	// ////////////////
 
 	private String getImageKey(Object element) {
 		String key = SharedImages.IMG_LEAF;
 		if (viewer.isExpandable(element)) {
 			boolean isExpanded = viewer.getExpandedState(element);
 			key = isExpanded ? SharedImages.IMG_NODE_EXPANDED : SharedImages.IMG_NODE_COLLAPSED;
 		}
 		return key;
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * This listener is in charge of updating a tree item's icon whenever the
 	 * item is collapsed or expanded.
 	 */
 	private static final class UpdateIconsTreeListener implements TreeListener {
 
 		public void treeCollapsed(TreeEvent e) {
 			// cannot use treeItem.getExpanded() because it has the old value
 			updateIcon((TreeItem) e.item, false);
 		}
 
 		public void treeExpanded(TreeEvent e) {
 			// cannot use treeItem.getExpanded() because it has the old value
 			updateIcon((TreeItem) e.item, true);
 		}
 
 		private void updateIcon(TreeItem item, boolean isExpanded) {
 			String key = isExpanded ? SharedImages.IMG_NODE_EXPANDED : SharedImages.IMG_NODE_COLLAPSED;
 			item.setImage(Activator.getSharedImage(key));
 		}
 	}
 
 }
