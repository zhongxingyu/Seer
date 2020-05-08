 /*******************************************************************************
  * Copyright (c) 2008, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.filters;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * Provide a generic filter, it returns the exact elements and the containers of elements of the given
  * <Eclass> type.
  * 
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EObjectStrictFilter extends ViewerFilter {
 
 	protected EClass eClassToFilter;
 
 	/**
 	 * @param eClassToFilter
 	 *            the type use to filter elements
 	 */
 	public EObjectStrictFilter(EClass eClassToFilter) {
 		this.eClassToFilter = eClassToFilter;
 	}
 
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return eClassToFilter.isInstance(element);
 	}
 
 }
