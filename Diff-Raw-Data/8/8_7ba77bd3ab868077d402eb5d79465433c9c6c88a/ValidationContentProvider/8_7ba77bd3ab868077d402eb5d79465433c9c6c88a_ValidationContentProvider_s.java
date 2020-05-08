 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.validation.providers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 
 /**
  * Content Provider for validation view.
  * 
  * @author Carmen Carlan
  * 
  */
 public class ValidationContentProvider extends AdapterFactoryContentProvider {
 
 	/**
 	 * Default constructor.
 	 */
 	public ValidationContentProvider() {
 		super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#getElements
 	 * (java.lang.Object)
 	 */
 	@Override
 	public Object[] getElements(Object inputElement) {
 		IStatus status = BasicDiagnostic.toIStatus((BasicDiagnostic) inputElement);
 
 		List<IStatus> constraints = new ArrayList<IStatus>();
 		if (status.isMultiStatus()) {
 			IStatus[] statuses = status.getChildren();
 			for (int i = 0; i < statuses.length; i++) {
 				constraints.add(statuses[i]);
 
 			}
 		} else if(!status.isOK()) {
 			
 			constraints.add(status);
 		}
 
 		return constraints.toArray(new Object[constraints.size()]);
 	}
 
 }
