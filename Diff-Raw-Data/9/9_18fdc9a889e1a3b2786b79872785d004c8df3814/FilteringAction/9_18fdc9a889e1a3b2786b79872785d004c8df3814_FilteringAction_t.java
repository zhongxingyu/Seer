 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewer.filter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.viewer.AbstractOrderingAction;
 import org.eclipse.emf.compare.ui.viewer.structure.ParameterizedStructureContentProvider;
 import org.eclipse.emf.compare.ui.viewer.structure.ParameterizedStructureMergeViewer;
 import org.eclipse.jface.action.IAction;
 
 /**
  * Action to filter difference elements.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  * @since 1.3
  */
 public class FilteringAction extends AbstractOrderingAction {
 	/** Descriptor for filters. */
 	private IDifferenceFilter relatedFilter;
 
 	/**
 	 * Related menu.
 	 */
 	private FiltersMenu menu;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param desc
 	 *            The descriptor of filters.
 	 * @param viewer
 	 *            The viewer.
 	 * @param pMenu
 	 *            The menu managing this action.
 	 * @since 1.3
 	 */
 	public FilteringAction(DifferenceFilterDescriptor desc, ParameterizedStructureMergeViewer viewer,
 			FiltersMenu pMenu) {
 		super(desc.getName(), IAction.AS_CHECK_BOX, viewer);
 		relatedFilter = desc.getExtension();
 		this.menu = pMenu;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param desc
 	 *            The descriptor of filters.
 	 * @param viewer
 	 *            The viewer.
 	 * @param checked
 	 *            The flag to check or not the action.
 	 * @param pMenu
 	 *            The menu managing this action.
 	 * @since 1.3
 	 */
 	public FilteringAction(DifferenceFilterDescriptor desc, ParameterizedStructureMergeViewer viewer,
 			boolean checked, FiltersMenu pMenu) {
 		this(desc, viewer, pMenu);
 		setChecked(checked);
 		this.menu = pMenu;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ui.viewer.AbstractOrderingAction#doRun(org.eclipse.emf.compare.ui.viewer.structure.ParameterizedStructureContentProvider)
 	 */
 	@Override
 	protected void doRun(ParameterizedStructureContentProvider provider) {
 		final List<IDifferenceFilter> filters = new ArrayList<IDifferenceFilter>();
 		if (isChecked()) {
			menu.getSelectedFilters().add(relatedFilter);
 		} else {
			menu.getSelectedFilters().remove(relatedFilter);
 		}
		filters.addAll(menu.getSelectedFilters());
 		mViewer.getCompareConfiguration()
 				.setProperty(EMFCompareConstants.PROPERTY_STRUCTURE_FILTERS, filters);
 	}
 }
