 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.internal.utils;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.tcf.te.core.interfaces.IPropertyChangeProvider;
 /**
  * A quick filter is a viewer filter that selects elements, 
  * which has the specified name pattern, under a certain tree path.
  * Other elements outside of this tree path is ignored.
  */
 public class QuickFilter extends TablePatternFilter implements PropertyChangeListener {
 	// The tree viewer to filter.
 	private TreeViewer viewer;
 	// The root path to select from.
 	private TreePath root;
 
 	/**
 	 * Create a quick filter for the specified viewer.
 	 */
 	public QuickFilter(TreeViewer viewer) {
 		super((ILabelProvider) viewer.getLabelProvider());
 		this.viewer = viewer;
 		this.addPropertyChangeListener(this);
 	}
 	
 	/**
 	 * Show the pop up dialog for the specified root path.
 	 *  
 	 * @param root The root path to filter from.
 	 */
 	public void showFilterPopup(TreePath root) {
 		this.root = root;
 		if (!isFiltering()) {
 			viewer.addFilter(this);
 		}
 		QuickFilterPopup popup = new QuickFilterPopup(viewer, this);
 		Point location = computePopupLocation();
 		popup.open();
 		popup.getShell().setLocation(location);
 	}
 
 	/**
 	 * Compute the best location of the pop up dialog.
 	 * 
 	 * @return The best location of the pop up dialog.
 	 */
 	private Point computePopupLocation() {
 	    Point location = null;
 		if (root != null) {
 		    TreeItem[] items = viewer.getTree().getSelection();
 		    if (items != null && items.length > 0) {
 		    	for(TreeItem item : items) {
 		    		viewer.getTree().showItem(item);
 		    	}
 		    	TreeItem item = items[0];
 		    	Rectangle bounds = item.getBounds();
 		    	location = new Point(bounds.x, bounds.y-bounds.height);
 		    }
 		    else {
		    	location = new Point(0, 0);
 		    }
 		}
 		else {
			location = new Point(0, 0);
 		}
 		location = viewer.getTree().toDisplay(location);
 	    return location;
     }
 	
 	/**
 	 * Adjust the position of the pop up when the tree viewer has changed.
 	 * 
 	 * @param popshell The shell of the pop up dialog.
 	 */
 	void adjustPopup(Shell popshell) {
 		Point location = computePopupLocation();
 		Point shellLocation = popshell.getLocation();
 		if(shellLocation != null && !shellLocation.equals(location)) {
 			popshell.setLocation(location);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
 	 */
 	@Override
     public void propertyChange(PropertyChangeEvent evt) {
 		Object element = getFilteringElement();
 		if(element != null) {
 			IPropertyChangeProvider provider = getPropertyChangeProvider(element);
 			if(provider!=null) {
 				provider.firePropertyChange(new PropertyChangeEvent(element, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
 			}
 		}
     }
 
 	/**
 	 * Get an adapter of IPropertyChangeProvider from the specified element.
 	 * 
 	 * @param element The element to get the adapter from.
 	 * @return The element's adapter or null if does not adapt to IPropertyChangeProvider.
 	 */
 	private IPropertyChangeProvider getPropertyChangeProvider(Object element) {
 		IPropertyChangeProvider provider = null;
 		if(element instanceof IPropertyChangeProvider) {
 			provider = (IPropertyChangeProvider) element;
 		}
 		if(provider == null && element instanceof IAdaptable) {
 			provider = (IPropertyChangeProvider) ((IAdaptable)element).getAdapter(IPropertyChangeProvider.class);
 		}
 		if(provider == null && element != null) {
 			provider = (IPropertyChangeProvider) Platform.getAdapterManager().getAdapter(element, IPropertyChangeProvider.class);
 		}
 	    return provider;
     }
 
 	/**
 	 * Get the element which is currently being filtered.
 	 * 
 	 * @return The current filtered element.
 	 */
 	private Object getFilteringElement() {
 		Object element = root.getLastSegment();
 	    return element == null ? viewer.getInput() : element;
     }
 	
 	/**
 	 * Reset the tree viewer to the original view by removing this filter.
 	 */
 	public void resetViewer() {
 		viewer.removeFilter(this);
 		setPattern(null);
 		root = null;
 		viewer.refresh();
 	}
 
 
 	/**
 	 * If the current viewer is being filtered.
 	 * 
 	 * @return true if it has this filter.
 	 */
 	public boolean isFiltering() {
 		ViewerFilter[] filters = viewer.getFilters();
 		if (filters != null) {
 			for (ViewerFilter filter : filters) {
 				if (filter == this) {
 					return matcher != null;
 				}
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.dialogs.TablePatternFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 		return skipMatching(parentElement) || super.select(viewer, parentElement, element);
 	}
 
 	/**
 	 * If the specified parent element should be skipped when matching elements.
 	 * 
 	 * @param parentElement The parent element.
 	 * @return true if it should be skipped.
 	 */
 	private boolean skipMatching(Object parentElement) {
 		if (root == null || parentElement == null) return true;
		if (parentElement instanceof TreePath) {
			return !root.equals(parentElement);
 		}
		Object rootElement = root.getLastSegment();
		return !parentElement.equals(rootElement);
 	}
 
 	/**
 	 * If the element is being filtered.
 	 * 
 	 * @param element The element to be checked.
 	 * @return true if it is filtering.
 	 */
 	public boolean isFiltering(Object element) {
		if(root != null) {
			Object rootElement = root;
			rootElement = root.getLastSegment();
			return rootElement == element && matcher != null;
 		}
	    return false;
    }
 }
