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
 
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
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
		// always match, since filtering is performed server-side
		return true;
 	}
 
 }
