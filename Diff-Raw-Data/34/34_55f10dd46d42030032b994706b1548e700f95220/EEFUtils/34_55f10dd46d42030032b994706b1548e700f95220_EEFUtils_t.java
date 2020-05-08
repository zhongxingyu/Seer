 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.utils;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.eef.runtime.EMFPropertiesRuntime;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EEFUtils {
 
 	/**
 	 * @deprecated
 	 */
 	public static Object choiceOfValues(AdapterFactory adapterFactory, EObject eObject,
 			EStructuralFeature feature, ResourceSet allResources) {
		return choiceOfValues(eObject, feature, allResources);
 	}
 
	/**
	 * @deprecated
	 */
 	public static Object choiceOfValues(EObject eObject, EStructuralFeature feature, ResourceSet allResources) {
		return choiceOfValues(eObject, feature);
	}
	
	public static Object choiceOfValues(EObject eObject, EStructuralFeature feature){
 		Object choiceOfValues = null;
		IItemPropertySource ps = (IItemPropertySource)EMFPropertiesRuntime.getDefault().getAdapterFactory()
				.adapt(eObject, IItemPropertySource.class);
 		if (ps != null) {
 			IItemPropertyDescriptor propertyDescriptor = ps.getPropertyDescriptor(eObject, feature);
 			if (propertyDescriptor != null)
 				choiceOfValues = propertyDescriptor.getChoiceOfValues(eObject);
 		}
		if (choiceOfValues == null && eObject.eResource() != null && eObject.eResource().getResourceSet() != null)
			choiceOfValues = eObject.eResource().getResourceSet();
 		return choiceOfValues;
 	}
 
 }
