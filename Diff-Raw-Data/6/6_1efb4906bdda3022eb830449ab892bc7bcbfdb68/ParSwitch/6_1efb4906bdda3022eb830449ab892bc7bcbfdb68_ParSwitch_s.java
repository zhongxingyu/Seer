 /*******************************************************************************
  * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.par.util;
 
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;
import org.eclipse.virgo.ide.par.*;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Switch</b> for the model's inheritance hierarchy.
  * It supports the call {@link #doSwitch(EObject) doSwitch(object)}to invoke the <code>caseXXX</code> method for each class of the model,
  * starting with the actual class of the object
  * and proceeding up the inheritance hierarchy
  * until a non-null result is returned,
  * which is the result of the switch.
  * <!-- end-user-doc -->
  * @see org.eclipse.virgo.ide.par.ParPackage
  * @generated
  */
 public class ParSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static ParPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ParSwitch() {
 		if (modelPackage == null) {
 			modelPackage = ParPackage.eINSTANCE;
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
 		}
 		else {
 			List<EClass> eSuperTypes = theEClass.getESuperTypes();
 			return
 				eSuperTypes.isEmpty() ?
 					defaultCase(theEObject) :
 					doSwitch(eSuperTypes.get(0), theEObject);
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
 			case ParPackage.PAR: {
 				Par par = (Par)theEObject;
 				T result = casePar(par);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case ParPackage.BUNDLE: {
 				Bundle bundle = (Bundle)theEObject;
 				T result = caseBundle(bundle);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			default: return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Par</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Par</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T casePar(Par object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Bundle</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Bundle</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseBundle(Bundle object) {
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
 
 } //ParSwitch
