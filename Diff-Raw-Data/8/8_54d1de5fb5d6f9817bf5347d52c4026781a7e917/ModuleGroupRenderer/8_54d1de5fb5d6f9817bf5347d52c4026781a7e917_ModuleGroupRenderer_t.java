 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.lnf.renderer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.ui.swt.views.ModuleGroupView;
 import org.eclipse.riena.navigation.ui.swt.views.ModuleView;
 import org.eclipse.riena.ui.swt.lnf.AbstractLnfRenderer;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Renderer of the module group inside the navigation.
  */
 public class ModuleGroupRenderer extends AbstractLnfRenderer {
 
 	private static final int MODULE_MODULE_GAP = 2;
 	private static final int MODULE_GROUP_PADDING = 1;
 
 	private boolean active;
 	private List<ModuleView> items;
 	private ModuleGroupNode navigationNode;
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Use a renderer to draw the border of the module group.
 	 */
 	@Override
 	public void paint(final GC gc, final Object value) {
 
 		super.paint(gc, value);
 
 		// border of module group
 		final EmbeddedBorderRenderer borderRenderer = getLnfBorderRenderer();
 		borderRenderer.setMarkers(getMarkers());
 		borderRenderer.setBounds(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
 		borderRenderer.setActive(isActive());
 		borderRenderer.paint(gc, null);
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.lnf.ILnfRenderer#dispose()
 	 */
 	public void dispose() {
 		for (final ModuleView item : getItems()) {
 			item.dispose();
 		}
 	}
 
 	/**
 	 * Computes the size of the module group.
 	 * 
 	 * @param gc
 	 *            <code>GC</code> of the component <code>Control</code>
 	 * @param wHint
 	 *            the width hint
 	 * @param hHint
 	 *            the height hint
 	 * @return a Point representing the size of the module group
 	 */
 	public Point computeSize(final GC gc, final int wHint, final int hHint) {
 
 		final EmbeddedBorderRenderer borderRenderer = getLnfBorderRenderer();
 
 		int w = wHint;
 		if (w == SWT.DEFAULT) {
 			w = borderRenderer.computeOuterWidth(getItemWidth());
 		}
 
 		final List<ModuleView> modules = getItems();
 		int h = 0;
 		if (modules.size() > 0) {
 
 			final Composite parent = modules.get(0).getParent();
 			if (SwtUtilities.isDisposed(parent)) {
 				return new Point(w, h);
 			}
 
 			h = getModuleGroupPadding();
			Collections.sort(modules, new ModuleComparator());
 			for (final ModuleView moduleView : modules) {
 				if (moduleView.getNavigationNode() == null || moduleView.getNavigationNode().isDisposed()) {
 					break;
 				}
 				moduleView.prepareUpdate();
 			}
 
 			parent.layout();
 
			Collections.sort(modules, new ModuleComparator());
 			for (final Iterator<ModuleView> iterator = modules.iterator(); iterator.hasNext();) {
 				final ModuleView moduleView = iterator.next();
 				if (moduleView.getNavigationNode() == null || moduleView.getNavigationNode().isDisposed()) {
 					break;
 				}
 				h += moduleView.getBounds().height;
 				if (iterator.hasNext()) {
 					h += getModuleModuleGap();
 				} else {
 					h += getModuleGroupPadding();
 				}
 			}
 			h = borderRenderer.computeOuterHeight(h);
 		}
 		return new Point(w, h);
 
 	}
 
	private class ModuleComparator implements Comparator<ModuleView> {
 
 		public int compare(final ModuleView o1, final ModuleView o2) {
 			return navigationNode.getIndexOfChild(o1.getNavigationNode()) < navigationNode.getIndexOfChild(o2.getNavigationNode()) ? -1 : 1;
 		}
 
 	}
 
 	private EmbeddedBorderRenderer getLnfBorderRenderer() {
 
 		EmbeddedBorderRenderer renderer = (EmbeddedBorderRenderer) LnfManager.getLnf().getRenderer(LnfKeyConstants.MODULE_GROUP_BORDER_RENDERER);
 		if (renderer == null) {
 			renderer = new ModuleGroupBorderRenderer();
 		}
 		return renderer;
 
 	}
 
 	/**
 	 * @return the items
 	 */
 	public List<ModuleView> getItems() {
 		if (items == null) {
 			items = new ArrayList<ModuleView>();
 		}
 		return items;
 	}
 
 	/**
 	 * @param items
 	 *            the items to set
 	 */
 	public void setItems(final List<ModuleView> items) {
 		this.items = items;
 	}
 
 	/**
 	 * Returns the gap between to modules.
 	 * 
 	 * @return gap
 	 */
 	public int getModuleModuleGap() {
 		return MODULE_MODULE_GAP;
 	}
 
 	/**
 	 * Returns the padding (top,left,bottom,right) of a module group.
 	 * 
 	 * @return padding
 	 */
 	public int getModuleGroupPadding() {
 		return MODULE_GROUP_PADDING;
 	}
 
 	/**
 	 * Returns the width of a module (module item).
 	 * 
 	 * @return width
 	 */
 	public int getItemWidth() {
 		int width = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.NAVIGATION_WIDTH);
 		width = SwtUtilities.convertXToDpi(width);
 		width -= 2 * getLnfBorderRenderer().getBorderWidth();
 		return width;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(final boolean active) {
 		this.active = active;
 	}
 
 	/**
 	 * @param navigationNode
 	 *            is the {@link ModuleGroupNode} of the related {@link ModuleGroupView}
 	 */
 	public void setNavigationNode(final ModuleGroupNode navigationNode) {
 		this.navigationNode = navigationNode;
 	}
 
 }
