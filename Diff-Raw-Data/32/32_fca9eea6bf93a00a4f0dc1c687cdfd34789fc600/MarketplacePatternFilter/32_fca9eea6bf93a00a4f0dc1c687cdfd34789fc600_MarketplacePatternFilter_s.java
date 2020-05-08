 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
 import org.eclipse.jface.viewers.Viewer;
 
 class MarketplacePatternFilter extends PatternFilter {
 
 	public MarketplacePatternFilter() {
 		setIncludeLeadingWildcard(true);
 	}
 
 	private boolean filterMatches(String text) {
 		return text != null && wordMatches(text);
 	}
 
 	@Override
 	protected Object[] getChildren(Object element) {
 		if (element instanceof CatalogCategory) {
 			return ((CatalogCategory) element).getItems().toArray();
 		}
 		return super.getChildren(element);
 	}
 
 	@Override
 	protected boolean isLeafMatch(Viewer filteredViewer, Object element) {
		if (element instanceof CatalogItem) {
			CatalogItem item = (CatalogItem) element;
			Object data = item.getData();
			if (data instanceof CatalogDescriptor) {
				// always allow these to pass through
				return true;
			}
			if (!(filterMatches(item.getName()) || filterMatches(item.getDescription())
					|| filterMatches(item.getProvider()) || filterMatches(item.getLicense()))) {
				return false;
			}
			return true;
		}
		return false;
 	}
 
 }
