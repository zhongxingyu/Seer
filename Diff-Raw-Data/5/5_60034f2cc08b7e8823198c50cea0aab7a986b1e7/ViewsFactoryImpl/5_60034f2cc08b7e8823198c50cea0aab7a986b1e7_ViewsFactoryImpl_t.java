 /**
  * ******************************************************************************
  *  Copyright (c) 2008 Obeo.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *  
  *  Contributors:
  *      Obeo - initial API and implementation
  *  ******************************************************************************
  *
 * $Id: ViewsFactoryImpl.java,v 1.4 2009/06/23 13:58:10 sbouchet Exp $
  */
 package org.eclipse.emf.eef.views.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.eef.views.Category;
 import org.eclipse.emf.eef.views.Container;
 import org.eclipse.emf.eef.views.CustomElementEditor;
 import org.eclipse.emf.eef.views.CustomView;
 import org.eclipse.emf.eef.views.ElementEditor;
 import org.eclipse.emf.eef.views.View;
 import org.eclipse.emf.eef.views.ViewReference;
 import org.eclipse.emf.eef.views.ViewsFactory;
 import org.eclipse.emf.eef.views.ViewsPackage;
 import org.eclipse.emf.eef.views.ViewsRepository;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class ViewsFactoryImpl extends EFactoryImpl implements ViewsFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static ViewsFactory init() {
 		try {
 			ViewsFactory theViewsFactory = (ViewsFactory) EPackage.Registry.INSTANCE
 					.getEFactory("http://www.eclipse.org/emf/eef/views/1.0.0"); //$NON-NLS-1$ 
 			if (theViewsFactory != null) {
 				return theViewsFactory;
 			}
 		} catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new ViewsFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewsFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 		case ViewsPackage.VIEWS_REPOSITORY:
 			return createViewsRepository();
 		case ViewsPackage.VIEW:
 			return createView();
 		case ViewsPackage.ELEMENT_EDITOR:
 			return createElementEditor();
 		case ViewsPackage.CATEGORY:
 			return createCategory();
 		case ViewsPackage.CONTAINER:
 			return createContainer();
 		case ViewsPackage.CUSTOM_ELEMENT_EDITOR:
 			return createCustomElementEditor();
 		case ViewsPackage.CUSTOM_VIEW:
 			return createCustomView();
 		case ViewsPackage.VIEW_REFERENCE:
 			return createViewReference();
 		default:
 			throw new IllegalArgumentException(
 					"The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewsRepository createViewsRepository() {
 		ViewsRepositoryImpl viewsRepository = new ViewsRepositoryImpl();
 		return viewsRepository;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public View createView() {
 		ViewImpl view = new ViewImpl();
 		return view;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ElementEditor createElementEditor() {
 		ElementEditorImpl elementEditor = new ElementEditorImpl();
 		return elementEditor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Category createCategory() {
 		CategoryImpl category = new CategoryImpl();
 		return category;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
	public org.eclipse.emf.eef.views.Container createContainer() {
 		ContainerImpl container = new ContainerImpl();
 		return container;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CustomElementEditor createCustomElementEditor() {
 		CustomElementEditorImpl customElementEditor = new CustomElementEditorImpl();
 		return customElementEditor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CustomView createCustomView() {
 		CustomViewImpl customView = new CustomViewImpl();
 		return customView;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewReference createViewReference() {
 		ViewReferenceImpl viewReference = new ViewReferenceImpl();
 		return viewReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewsPackage getViewsPackage() {
 		return (ViewsPackage) getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static ViewsPackage getPackage() {
 		return ViewsPackage.eINSTANCE;
 	}
 
 } //ViewsFactoryImpl
