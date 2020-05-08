 /**
  * Copyright (c) 2011 NumberFour AG
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  */
 package org.eclipse.dltk.javascript.typeinfo.model.impl;
 
 import java.util.Collection;
 
 import org.eclipse.dltk.javascript.typeinfo.model.FunctionType;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelPackage;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Function Type</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.dltk.javascript.typeinfo.model.impl.FunctionTypeImpl#getReturnType <em>Return Type</em>}</li>
  *   <li>{@link org.eclipse.dltk.javascript.typeinfo.model.impl.FunctionTypeImpl#getParameters <em>Parameters</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class FunctionTypeImpl extends EObjectImpl implements FunctionType {
     /**
      * The cached value of the '{@link #getReturnType() <em>Return Type</em>}' containment reference.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see #getReturnType()
      * @generated
      * @ordered
      */
     protected JSType returnType;
 
     /**
      * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see #getParameters()
      * @generated
      * @ordered
      */
     protected EList<Parameter> parameters;
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     protected FunctionTypeImpl() {
         super();
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     protected EClass eStaticClass() {
         return TypeInfoModelPackage.Literals.FUNCTION_TYPE;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public JSType getReturnType() {
         return returnType;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public NotificationChain basicSetReturnType(JSType newReturnType, NotificationChain msgs) {
         JSType oldReturnType = returnType;
         returnType = newReturnType;
         if (eNotificationRequired()) {
             ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE, oldReturnType, newReturnType);
             if (msgs == null) msgs = notification; else msgs.add(notification);
         }
         return msgs;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public void setReturnType(JSType newReturnType) {
         if (newReturnType != returnType) {
             NotificationChain msgs = null;
             if (returnType != null)
                 msgs = ((InternalEObject)returnType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE, null, msgs);
             if (newReturnType != null)
                 msgs = ((InternalEObject)newReturnType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE, null, msgs);
             msgs = basicSetReturnType(newReturnType, msgs);
             if (msgs != null) msgs.dispatch();
         }
         else if (eNotificationRequired())
             eNotify(new ENotificationImpl(this, Notification.SET, TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE, newReturnType, newReturnType));
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public EList<Parameter> getParameters() {
         if (parameters == null) {
             parameters = new EObjectContainmentEList<Parameter>(Parameter.class, this, TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS);
         }
         return parameters;
     }
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
     public TypeKind getKind() {
 		return TypeKind.FUNCTION;
     }
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
     public String getName() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("function");
 		printParameters(sb);
 		if (returnType != null) {
 			sb.append(":");
			sb.append(returnType.getName());
 		}
 		return sb.toString();
     }
 
 	protected void printParameters(StringBuilder sb) {
 		sb.append("(");
 		if (parameters != null) {
			boolean first = true;
 			for (Parameter parameter : parameters) {
 				if (!first) {
 					sb.append(",");
 				}
 				first = false;
 				if (parameter.getName() != null) {
 					sb.append(parameter.getName());
 					sb.append(":");
 				}
 				sb.append(parameter.getType() != null ? parameter.getType()
 						.getName() : "*");
 			}
 		}
 		sb.append(")");
 	}
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
         switch (featureID) {
             case TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE:
                 return basicSetReturnType(null, msgs);
             case TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS:
                 return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
         }
         return super.eInverseRemove(otherEnd, featureID, msgs);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public Object eGet(int featureID, boolean resolve, boolean coreType) {
         switch (featureID) {
             case TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE:
                 return getReturnType();
             case TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS:
                 return getParameters();
         }
         return super.eGet(featureID, resolve, coreType);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @SuppressWarnings("unchecked")
     @Override
     public void eSet(int featureID, Object newValue) {
         switch (featureID) {
             case TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE:
                 setReturnType((JSType)newValue);
                 return;
             case TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS:
                 getParameters().clear();
                 getParameters().addAll((Collection<? extends Parameter>)newValue);
                 return;
         }
         super.eSet(featureID, newValue);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public void eUnset(int featureID) {
         switch (featureID) {
             case TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE:
                 setReturnType((JSType)null);
                 return;
             case TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS:
                 getParameters().clear();
                 return;
         }
         super.eUnset(featureID);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public boolean eIsSet(int featureID) {
         switch (featureID) {
             case TypeInfoModelPackage.FUNCTION_TYPE__RETURN_TYPE:
                 return returnType != null;
             case TypeInfoModelPackage.FUNCTION_TYPE__PARAMETERS:
                 return parameters != null && !parameters.isEmpty();
         }
         return super.eIsSet(featureID);
     }
 
 } //FunctionTypeImpl
