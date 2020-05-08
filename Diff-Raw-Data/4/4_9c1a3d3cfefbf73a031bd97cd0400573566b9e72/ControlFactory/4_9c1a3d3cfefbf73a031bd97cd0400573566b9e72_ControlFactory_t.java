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
 package org.eclipse.emf.ecp.editor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecp.common.model.ECPModelelementContext;
 import org.eclipse.emf.ecp.editor.mecontrols.AbstractMEControl;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 
 /**
  * Factory for generating {@link AbstractMEControl}'s according to a {@link IItemPropertyDescriptor}.
  * 
  * @author shterev
  */
 public class ControlFactory {
 	private HashMap<Class<?>, ArrayList<AbstractMEControl>> controlRegistry;
 
 	/**
 	 * Default constructor.
 	 */
 	public ControlFactory() {
 		controlRegistry = new HashMap<Class<?>, ArrayList<AbstractMEControl>>();
 		initializeMEControls();
 	}
 
 	private void initializeMEControls() {
 		IConfigurationElement[] attributecontrols = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			"org.eclipse.emf.ecp.editor.attributecontrols");
 		IConfigurationElement[] referencecontrols = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			"org.eclipse.emf.ecp.editor.referencecontrols");
 		ArrayList<IConfigurationElement> allControls = new ArrayList<IConfigurationElement>();
 		allControls.addAll(Arrays.asList(attributecontrols));
 		allControls.addAll(Arrays.asList(referencecontrols));
 		for (IConfigurationElement e : allControls) {
 			String type = e.getAttribute("type");
 			try {
				AbstractMEControl control = (AbstractMEControl) e.createExecutableExtension("class");			
				Class<?> resolvedType = control.getClass().getClassLoader().loadClass(type);
 				boolean showLabel = Boolean.parseBoolean(e.getAttribute("showLabel"));
 				control.setShowLabel(showLabel);
 				ArrayList<AbstractMEControl> list = controlRegistry.get(resolvedType);
 				if (list == null) {
 					list = new ArrayList<AbstractMEControl>();
 				}
 				list.add(control);
 				controlRegistry.put(resolvedType, list);
 
 			} catch (ClassNotFoundException e1) {
 				Activator.logException(e1);
 			} catch (CoreException e2) {
 				Activator.logException(e2);
 			}
 		}
 
 	}
 
 	/**
 	 * Creates a {@link AbstractMEControl} according to the {@link IItemPropertyDescriptor}.
 	 * 
 	 * @param itemPropertyDescriptor the descriptor
 	 * @param modelElement model element
 	 * @return the {@link AbstractMEControl}
 	 */
 	public AbstractMEControl createControl(IItemPropertyDescriptor itemPropertyDescriptor, EObject modelElement) {
 		return createControl(itemPropertyDescriptor, modelElement, null);
 	}
 
 	/**
 	 * Creates a {@link AbstractMEControl} according to the {@link IItemPropertyDescriptor}.
 	 * 
 	 * @param itemPropertyDescriptor the descriptor
 	 * @param modelElement model element
 	 * @param context model element context
 	 * @return the {@link AbstractMEControl}
 	 */
 	public AbstractMEControl createControl(IItemPropertyDescriptor itemPropertyDescriptor, EObject modelElement,
 		ECPModelelementContext context) {
 
 		EStructuralFeature feature = (EStructuralFeature) itemPropertyDescriptor.getFeature(modelElement);
 		if (feature instanceof EAttribute) {
 			return createAttribute(itemPropertyDescriptor, feature, modelElement, context);
 		} else if (feature instanceof EReference) {
 			return createReferenceControl(itemPropertyDescriptor, (EReference) feature, modelElement, context);
 		}
 
 		return null;
 	}
 
 	private AbstractMEControl createReferenceControl(IItemPropertyDescriptor itemPropertyDescriptor,
 		EReference feature, EObject modelElement, ECPModelelementContext context) {
 		Class<?> instanceClass = feature.getEType().getInstanceClass();
 		Set<Class<?>> keySet = controlRegistry.keySet();
 		ArrayList<AbstractMEControl> candidates = new ArrayList<AbstractMEControl>();
 		for (Class<?> clazz : keySet) {
 			if (clazz.isAssignableFrom(instanceClass)) {
 				candidates.addAll(controlRegistry.get(clazz));
 			}
 		}
 		AbstractMEControl control = getBestCandidate(candidates, itemPropertyDescriptor, feature, modelElement, context);
 		AbstractMEControl ret = null;
 		if (control == null) {
 			return null;
 		}
 		try {
 			ret = control.getClass().newInstance();
 			ret.setShowLabel(control.getShowLabel());
 		} catch (InstantiationException e) {
 			// Do nothing
 		} catch (IllegalAccessException e) {
 			// Do nothing
 		}
 		return ret;
 	}
 
 	private AbstractMEControl createAttribute(IItemPropertyDescriptor itemPropertyDescriptor,
 		EStructuralFeature feature, EObject modelElement, ECPModelelementContext context) {
 		Class<?> instanceClass = ((EAttribute) feature).getEAttributeType().getInstanceClass();
 		// Test which controls have a fitting type
 		// TODO: could be chached?
 		Set<Class<?>> keySet = controlRegistry.keySet();
 		ArrayList<AbstractMEControl> candidates = new ArrayList<AbstractMEControl>();
 		for (Class<?> clazz : keySet) {
 			if (instanceClass.isPrimitive()) {
 				try {
 					Class<?> primitive = (Class<?>) clazz.getField("TYPE").get(null);
 					if (primitive.equals(instanceClass)) {
 						candidates.addAll(controlRegistry.get(clazz));
 					}
 
 				} catch (IllegalArgumentException e) {
 					// Do nothing
 				} catch (SecurityException e) {
 					// Do nothing
 				} catch (IllegalAccessException e) {
 					// Do nothing
 				} catch (NoSuchFieldException e) {
 					// Do nothing
 				}
 			}
 			if (clazz.isAssignableFrom(instanceClass)) {
 				candidates.addAll(controlRegistry.get(clazz));
 			}
 		}
 		AbstractMEControl control = getBestCandidate(candidates, itemPropertyDescriptor, feature, modelElement, context);
 		AbstractMEControl ret = null;
 		if (control == null) {
 			return null;
 		}
 		try {
 			ret = control.getClass().newInstance();
 			ret.setShowLabel(control.getShowLabel());
 		} catch (InstantiationException e) {
 			// Do nothing
 		} catch (IllegalAccessException e) {
 			// Do nothing
 		}
 		return ret;
 
 	}
 
 	private AbstractMEControl getBestCandidate(ArrayList<AbstractMEControl> candidates,
 		IItemPropertyDescriptor itemPropertyDescriptor, EStructuralFeature feature, EObject modelElement,
 		ECPModelelementContext context) {
 		int bestValue = 0;
 		AbstractMEControl bestCandidate = null;
 		for (AbstractMEControl abstractMEControl : candidates) {
 			abstractMEControl.setContext(context);
 			int newValue = abstractMEControl.canRender(itemPropertyDescriptor, modelElement);
 			if (newValue > bestValue) {
 				bestCandidate = abstractMEControl;
 				bestValue = newValue;
 			}
 		}
 		return bestCandidate;
 	}
 
 }
