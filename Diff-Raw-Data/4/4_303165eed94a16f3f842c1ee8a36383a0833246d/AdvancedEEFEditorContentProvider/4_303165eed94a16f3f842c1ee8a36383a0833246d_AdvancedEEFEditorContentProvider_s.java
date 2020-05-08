 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets.settings;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class AdvancedEEFEditorContentProvider extends AdapterFactoryContentProvider {
 
 	private List<Object> choiceOfValues;
 
 	private boolean eefInput = false;
 
 	/**
 	 * @param adapterFactory
 	 */
 	public AdvancedEEFEditorContentProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 		choiceOfValues = new ArrayList<Object>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		super.inputChanged(viewer, oldInput, newInput);
 		if (newInput instanceof EEFEditorSettings) {
 			choiceOfValues.clear();
 			computeChoiceOfValues(newInput);
 			eefInput = true;
 		} else {
 			eefInput = false;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object object) {
 		if (object instanceof EEFEditorSettings) {
			EList<Resource> resources = ((EEFEditorSettings)object).getSource().eResource().getResourceSet().getResources();
             resources.retainAll(this.choiceOfValues);
 			return resources.toArray();
 		}
 		return super.getElements(object);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#getChildren(java.lang.Object)
 	 */
 	public Object[] getChildren(Object object) {
 		if (eefInput) {
 			Object[] children = super.getChildren(object);
 			List<Object> result = new ArrayList<Object>();
 			for (Object child : children) {
 				if (choiceOfValues.contains(child))
 					result.add(child);
 			}
 			return result.toArray();
 		} else {
 			return super.getChildren(object);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object object) {
 		if (eefInput) {
 			return getChildren(object).length > 0;
 		} else {
 			return super.hasChildren(object);
 		}
 	}
 
 	/**
 	 * Compute the choice of values for the given input.
 	 * 
 	 * @param input
 	 *            the input to process
 	 */
 	private void computeChoiceOfValues(Object input) {
 		Object choiceOfValues2 = ((EEFEditorSettings)input).choiceOfValues(adapterFactory);
 		if (choiceOfValues2 instanceof Collection<?>) {
 			for (Object next : ((Collection<?>)choiceOfValues2)) {
 				while (next != null) {
 					if (next instanceof EObject) {
 						choiceOfValues.add(next);
 						EObject eContainer = ((EObject)next).eContainer();
 						if (eContainer == null) {
 							next = ((EObject)next).eResource();
 						} else {
 							next = eContainer;
 						}
 					} else if (next instanceof Resource) {
 						choiceOfValues.add(next);
 						next = null;
 					} else if ("".equals(next)) {
 						choiceOfValues.add(next);
 						next = null;
 					}
 				}
 			}
 		}
 	}
 
 }
