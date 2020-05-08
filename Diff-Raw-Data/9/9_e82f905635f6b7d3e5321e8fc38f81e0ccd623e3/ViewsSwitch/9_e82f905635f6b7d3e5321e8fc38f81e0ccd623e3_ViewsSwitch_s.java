 /*******************************************************************************
 * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.views.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.views.Category;
 import org.eclipse.emf.eef.views.Container;
 import org.eclipse.emf.eef.views.CustomElementEditor;
 import org.eclipse.emf.eef.views.CustomView;
 import org.eclipse.emf.eef.views.DocumentedElement;
 import org.eclipse.emf.eef.views.ElementEditor;
 import org.eclipse.emf.eef.views.IdentifiedElement;
 import org.eclipse.emf.eef.views.View;
 import org.eclipse.emf.eef.views.ViewElement;
 import org.eclipse.emf.eef.views.ViewReference;
 import org.eclipse.emf.eef.views.ViewsPackage;
 import org.eclipse.emf.eef.views.ViewsRepository;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Switch</b> for the model's inheritance hierarchy.
  * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
  * to invoke the <code>caseXXX</code> method for each class of the model,
  * starting with the actual class of the object
  * and proceeding up the inheritance hierarchy
  * until a non-null result is returned,
  * which is the result of the switch.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.views.ViewsPackage
  * @generated
  */
 public class ViewsSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static ViewsPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewsSwitch() {
 		if (modelPackage == null) {
 			modelPackage = ViewsPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	public T doSwitch(EObject theEObject) {
 		return doSwitch(theEObject.eClass(), theEObject);
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(EClass theEClass, EObject theEObject) {
 		if (theEClass.eContainer() == modelPackage) {
 			return doSwitch(theEClass.getClassifierID(), theEObject);
 		} else {
 			List<EClass> eSuperTypes = theEClass.getESuperTypes();
 			return eSuperTypes.isEmpty() ? defaultCase(theEObject) : doSwitch(eSuperTypes.get(0), theEObject);
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(int classifierID, EObject theEObject) {
 		switch (classifierID) {
 			case ViewsPackage.VIEWS_REPOSITORY: {
 				ViewsRepository viewsRepository = (ViewsRepository)theEObject;
 				T result = caseViewsRepository(viewsRepository);
 				if (result == null)
 					result = caseDocumentedElement(viewsRepository);
 				if (result == null)
 					result = caseIdentifiedElement(viewsRepository);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.VIEW: {
 				View view = (View)theEObject;
 				T result = caseView(view);
 				if (result == null)
 					result = caseContainer(view);
 				if (result == null)
 					result = caseViewElement(view);
 				if (result == null)
 					result = caseIdentifiedElement(view);
 				if (result == null)
 					result = caseDocumentedElement(view);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.ELEMENT_EDITOR: {
 				ElementEditor elementEditor = (ElementEditor)theEObject;
 				T result = caseElementEditor(elementEditor);
 				if (result == null)
 					result = caseViewElement(elementEditor);
 				if (result == null)
 					result = caseIdentifiedElement(elementEditor);
 				if (result == null)
 					result = caseDocumentedElement(elementEditor);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.CATEGORY: {
 				Category category = (Category)theEObject;
 				T result = caseCategory(category);
 				if (result == null)
 					result = caseDocumentedElement(category);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.CONTAINER: {
 				Container container = (Container)theEObject;
 				T result = caseContainer(container);
 				if (result == null)
 					result = caseViewElement(container);
 				if (result == null)
 					result = caseIdentifiedElement(container);
 				if (result == null)
 					result = caseDocumentedElement(container);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.VIEW_ELEMENT: {
 				ViewElement viewElement = (ViewElement)theEObject;
 				T result = caseViewElement(viewElement);
 				if (result == null)
 					result = caseDocumentedElement(viewElement);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.CUSTOM_ELEMENT_EDITOR: {
 				CustomElementEditor customElementEditor = (CustomElementEditor)theEObject;
 				T result = caseCustomElementEditor(customElementEditor);
 				if (result == null)
 					result = caseElementEditor(customElementEditor);
 				if (result == null)
 					result = caseViewElement(customElementEditor);
 				if (result == null)
 					result = caseIdentifiedElement(customElementEditor);
 				if (result == null)
 					result = caseDocumentedElement(customElementEditor);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.CUSTOM_VIEW: {
 				CustomView customView = (CustomView)theEObject;
 				T result = caseCustomView(customView);
 				if (result == null)
 					result = caseView(customView);
 				if (result == null)
 					result = caseContainer(customView);
 				if (result == null)
 					result = caseViewElement(customView);
 				if (result == null)
 					result = caseIdentifiedElement(customView);
 				if (result == null)
 					result = caseDocumentedElement(customView);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.DOCUMENTED_ELEMENT: {
 				DocumentedElement documentedElement = (DocumentedElement)theEObject;
 				T result = caseDocumentedElement(documentedElement);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.VIEW_REFERENCE: {
 				ViewReference viewReference = (ViewReference)theEObject;
 				T result = caseViewReference(viewReference);
 				if (result == null)
 					result = caseViewElement(viewReference);
 				if (result == null)
 					result = caseIdentifiedElement(viewReference);
 				if (result == null)
 					result = caseDocumentedElement(viewReference);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case ViewsPackage.IDENTIFIED_ELEMENT: {
 				IdentifiedElement identifiedElement = (IdentifiedElement)theEObject;
 				T result = caseIdentifiedElement(identifiedElement);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			default:
 				return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Repository</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Repository</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseViewsRepository(ViewsRepository object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>View</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>View</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseView(View object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Element Editor</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Element Editor</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseElementEditor(ElementEditor object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCategory(Category object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Container</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Container</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseContainer(Container object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>View Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>View Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseViewElement(ViewElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Custom Element Editor</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Custom Element Editor</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCustomElementEditor(CustomElementEditor object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Custom View</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Custom View</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCustomView(CustomView object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDocumentedElement(DocumentedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>View Reference</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>View Reference</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseViewReference(ViewReference object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Identified Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Identified Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseIdentifiedElement(IdentifiedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch, but this is the last case anyway.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
 	 * @generated
 	 */
 	public T defaultCase(EObject object) {
 		return null;
 	}
 
 } //ViewsSwitch
