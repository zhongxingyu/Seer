 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.adapters.SemanticAdapter;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.DomainPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.ui.widgets.eobjflatcombo.EObjectFlatComboSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 import org.osgi.framework.Bundle;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EEFUtils {
 
 	public static final String JDT_CORE_SYMBOLIC_NAME = "org.eclipse.jdt.core"; //$NON-NLS-1$
 
 	public static Object choiceOfValues(EObject eObject, EStructuralFeature feature) {
 		return choiceOfValues(EEFRuntimePlugin.getDefault().getAdapterFactory(), eObject, feature);
 	}
 
 	/**
 	 * Return the choice of value for the given feature
 	 * 
 	 * @param adapterFactory
 	 *            the adapterFactory to use
 	 * @param eObject
 	 *            the EObject to process
 	 * @param feature
 	 *            the feature to process
 	 * @return list of possible values
 	 */
 	public static Object choiceOfValues(AdapterFactory adapterFactory, EObject eObject,
 			EStructuralFeature feature) {
 		Object choiceOfValues = null;
 		IItemPropertySource ps = (IItemPropertySource)adapterFactory
 				.adapt(eObject, IItemPropertySource.class);
 		if (ps != null) {
 			IItemPropertyDescriptor propertyDescriptor = ps.getPropertyDescriptor(eObject, feature);
 			if (propertyDescriptor != null)
 				choiceOfValues = propertyDescriptor.getChoiceOfValues(eObject);
 		}
 		if (choiceOfValues == null && eObject.eResource() != null
 				&& eObject.eResource().getResourceSet() != null)
 			choiceOfValues = eObject.eResource().getResourceSet();
 		if (choiceOfValues == null)
 			choiceOfValues = "";
 		else if (choiceOfValues instanceof List) {
 			List<Object> list = (List<Object>)choiceOfValues;
 			for (int i = 0; i < list.size(); i++) {
 				Object next = list.get(i);
 				if (next == null) {
 					list.set(i, "");
 				}
 			}
 		}
 		return choiceOfValues;
 	}
 
 	/**
 	 * Return the choice of value for the given feature
 	 * 
 	 * @param adapterFactory
 	 *            the adapterFactory to use
 	 * @param eObject
 	 *            the EObject to process
 	 * @param feature
 	 *            the feature to process
 	 * @return list of possible values
 	 */
 	public static String getLabel(AdapterFactory adapterFactory, EObject eObject) {
 		IItemLabelProvider labelProvider = (IItemLabelProvider)adapterFactory.adapt(eObject,
 				IItemLabelProvider.class);
 		if (labelProvider != null) {
 			return labelProvider.getText(eObject);
 		}
 		return null;
 	}
 
 	/**
 	 * Return all the instanciable types for a given reference.
 	 * 
 	 * @param eReference
 	 *            the reference to process.
 	 * @param editingDomain
 	 *            the editing domain where we live !
 	 * @return a list of {@link EClass} containing all the instanciable type for the give reference.
 	 */
 	public static List<EClass> allTypeFor(EReference eReference, EditingDomain editingDomain) {
 		Collection<?> list = editingDomain.getNewChildDescriptors(
 				EEFUtils.searchInstanceOf(editingDomain.getResourceSet(), eReference.getEContainingClass()),
 				null);
 		ArrayList<EClass> instanciableTypesInHierarchy = new ArrayList<EClass>();
 		for (Object object : list) {
 			if (object instanceof CommandParameter) {
 				if (((CommandParameter)object).getFeature() instanceof EReference) {
 					if ((((CommandParameter)object).getFeature()).equals(eReference)) {
 						instanciableTypesInHierarchy.add(((CommandParameter)object).getEValue().eClass());
 					}
 				}
 			}
 		}
 		return instanciableTypesInHierarchy;
 	}
 
 	/**
 	 * Search an instance of a given Class.
 	 * 
 	 * @param allResources
 	 *            the ResourceSet where to search
 	 * @param eClass
 	 *            the searched type
 	 * @return the first instance of the given type.
 	 */
 	public static EObject searchInstanceOf(ResourceSet allResources, EClass eClass) {
 		for (Resource resource : allResources.getResources()) {
 			TreeIterator<EObject> iterator = resource.getAllContents();
 			while (iterator.hasNext()) {
 				Object objectTemp = iterator.next();
 				if (objectTemp instanceof EObject) {
 					if (eClass.isInstance(objectTemp))
 						return (EObject)objectTemp;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param eClassifier
 	 * @param resourceSet
 	 * @return all the concret types of a given classifier.
 	 */
 	public static List<EClass> instanciableTypesInHierarchy(EClassifier eClassifier, ResourceSet resourceSet) {
 		List<EClass> result = new ArrayList<EClass>();
 		if (eClassifier instanceof EClass) {
 			EClass eClass = (EClass)eClassifier;
 			if (!eClass.isAbstract())
 				result.add(eClass);
 			result.addAll(instanciableSubTypes(eClass, resourceSet));
 		}
 		return result;
 	}
 
 	private static List<EClass> instanciableSubTypes(EClass eClass, ResourceSet resourceSet) {
 		List<EClass> result = new ArrayList<EClass>();
 		for (EPackage ePackage : allPackages(eClass, resourceSet)) {
 			for (EClassifier eClassifier : ePackage.getEClassifiers()) {
 				if (eClassifier instanceof EClass) {
 					EClass eClass2 = (EClass)eClassifier;
 					if (!eClass2.equals(eClass) && eClass.isSuperTypeOf(eClass2) && !eClass2.isAbstract()
 							&& !eClass2.isInterface())
 						result.add(eClass2);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	private static List<EPackage> allPackages(EClass eClass, ResourceSet resourceSet) {
 		List<EPackage> result = new ArrayList<EPackage>();
 		if (eClass.eResource() != null) {
 			EcoreUtil.resolveAll(eClass);
 			List<Resource> resourcesToProcess = null;
 			if (eClass.eResource().getResourceSet() != null)
 				resourcesToProcess = eClass.eResource().getResourceSet().getResources();
 			else {
 				resourcesToProcess = new ArrayList<Resource>();
 				resourcesToProcess.add(eClass.eResource());
 			}
 			for (Resource resource : resourcesToProcess)
 				result.addAll(allPackageOfResource(resource));
 		} else {
 			EPackage rootPackage = eClass.getEPackage();
 			while (rootPackage.getESuperPackage() != null)
 				rootPackage = rootPackage.getESuperPackage();
 			result.addAll(allSubPackages(rootPackage));
 		}
 
 		if (resourceSet != null) {
 			for (EPackage ePackage : getAllEPackagesFromResourceSet(resourceSet)) {
 				if (!result.contains(ePackage))
 					result.add(ePackage);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Retrieves all the EPackages used by the ResourceSet
 	 */
 	private static List<EPackage> getAllEPackagesFromResourceSet(ResourceSet resourceSet_p) {
 		List<EPackage> result = new ArrayList<EPackage>();
 		for (Resource resource : resourceSet_p.getResources()) {
 			for (EPackage pkg : allPackageOfResource(resource)) {
				EPackage staticPackage = getStaticPackage(pkg);
				if (staticPackage != null) {
					result.add(staticPackage);
				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Get the equivalent package from the Global EPackage registry.
 	 */
 	private static EPackage getStaticPackage(EPackage ePackage_p) {
 		Object staticPackage = EPackage.Registry.INSTANCE.get(ePackage_p.getNsURI());
 		if (null != staticPackage) {
 			if (staticPackage instanceof EPackage) {
 				return (EPackage)staticPackage;
 			} else if (staticPackage instanceof EPackage.Descriptor) {
 				return ((EPackage.Descriptor)staticPackage).getEPackage();
 			}
 		}
 		return null;
 	}
 
 	private static List<EPackage> allSubPackages(EPackage ePackage) {
 		List<EPackage> result = new ArrayList<EPackage>();
 		for (EPackage subPackage : ePackage.getESubpackages())
 			result.addAll(allSubPackages(subPackage));
 		return result;
 	}
 
 	private static List<EPackage> allPackageOfResource(Resource resource) {
 		List<EPackage> result = new ArrayList<EPackage>();
 		for (Iterator<?> iterator = resource.getAllContents(); iterator.hasNext();) {
 			EObject type = (EObject)iterator.next();
 			if (type instanceof EPackage)
 				result.add((EPackage)type);
 		}
 		return result;
 	}
 
 	/**
 	 * Convert a treeIterator in Object list
 	 * 
 	 * @param iter
 	 *            the iterator
 	 * @return the result list
 	 */
 	public static List<Object> asList(TreeIterator<Object> iter) {
 		List<Object> result = new ArrayList<Object>();
 		while (iter.hasNext())
 			result.add(iter.next());
 		return result;
 	}
 
 	/**
 	 * Convert a treeIterator in EObject list
 	 * 
 	 * @param iter
 	 *            the iterator
 	 * @return the result list
 	 */
 	public static List<EObject> asEObjectList(TreeIterator<EObject> iter) {
 		List<EObject> result = new ArrayList<EObject>();
 		while (iter.hasNext())
 			result.add(iter.next());
 		return result;
 	}
 
 	/**
 	 * method defining if a bundle is loaded or not
 	 * 
 	 * @param name
 	 *            the searched bundle
 	 * @return <code>true</code> when the bundle is loaded
 	 */
 	public static boolean isBundleLoaded(String name) {
 		Bundle bundle = Platform.getBundle(name);
 		return bundle != null && bundle.getState() == Bundle.ACTIVE;
 	}
 
 	/**
 	 * @param eObject
 	 *            the element to check
 	 * @return true if the given element match or contains a matching element
 	 */
 	public static boolean containsInstanceOfEClass(EObject element, EClass eClassToCheck) {
 		// Check type and super type matching
 		if (isInstanceOfEClass(element, eClassToCheck)) {
 			return true;
 		}
 		// Check containment
 		for (EObject container : element.eContents()) {
 			if (containsInstanceOfEClass(container, eClassToCheck)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param eObject
 	 *            the element to check
 	 * @return true if the given element match or contains a matching element
 	 */
 	public static boolean isInstanceOfEClass(EObject element, EClass eClassToCheck) {
 		// Check type and super type matching
 		EClass eClass = element.eClass();
 		if (eClass.equals(eClassToCheck)) {
 			return true;
 		} else {
 			for (EClass eSuperClass : eClass.getEAllSuperTypes()) {
 				if (eSuperClass.equals(eClassToCheck)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param settings
 	 * @param eObject
 	 */
 	public static void putToReference(EEFEditorSettings settings, EObject eObject) {
 		if (settings instanceof ReferencesTableSettings) {
 			((ReferencesTableSettings)settings).addToReference(eObject);
 		} else if (settings instanceof EObjectFlatComboSettings) {
 			((EObjectFlatComboSettings)settings).setToReference(eObject);
 		}
 	}
 
 	/**
 	 * Search an {@link EditingDomain} in the given {@link PropertiesEditingContext} hierarchy.
 	 * 
 	 * @param editingContext
 	 *            to process
 	 * @return an {@link EditingDomain} if there is a {@link DomainPropertiesEditionContext} in the given
 	 *         hierarchy
 	 */
 	public static EditingDomain getEditingDomain(PropertiesEditingContext editingContext) {
 		if (editingContext instanceof DomainPropertiesEditionContext) {
 			return ((DomainPropertiesEditionContext)editingContext).getEditingDomain();
 		} else if (editingContext.getParentContext() != null) {
 			return getEditingDomain(editingContext.getParentContext());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * This method analyze an input to exact the EObject to edit. First we try to adapt this object in
 	 * {@link SemanticAdapter}. If this can't be done, we check if this object is an {@link EObject}. Finally,
 	 * if this object isn't an {@link EObject}, we try to adapt it in EObject.
 	 * 
 	 * @param object
 	 *            element to test
 	 * @return the EObject to edit with EEF.
 	 */
 	public static EObject resolveSemanticObject(Object object) {
 		IAdaptable adaptable = null;
 		if (object instanceof IAdaptable) {
 			adaptable = (IAdaptable)object;
 			Object getAdapter = adaptable.getAdapter(SemanticAdapter.class);
 			if (getAdapter != null) {
 				// 1 - Object is an IAdaptable and we can adapt this object in a SemanticAdapter
 				return ((SemanticAdapter) getAdapter).getEObject();
 			} 
 		}
 		Object loadAdapter = Platform.getAdapterManager().loadAdapter(object, SemanticAdapter.class.getName());
 		if (loadAdapter != null) {
 			// 2 - Platform can adapt Object in a SemanticAdapter
 			return ((SemanticAdapter) loadAdapter).getEObject();
 		}
 		if (object instanceof EObject) {
 			return (EObject)object;
 		}
 		if (adaptable != null) {
 			if (adaptable.getAdapter(EObject.class) != null) {
 				// 3 - we can adapt Object in EObject
 				return (EObject)adaptable.getAdapter(EObject.class);
 			}
 		}
 		return null;
 	}
 
 }
