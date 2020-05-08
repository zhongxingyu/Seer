 /**
  * Copyright (c) 2009-2010, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 import java.util.Collection;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3FunctionType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BTypeCalculator;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>B3 Function Type</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3FunctionTypeImpl#getFunctionType <em>Function Type</em>}</li>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3FunctionTypeImpl#getReturnType <em>Return Type</em>}</li>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3FunctionTypeImpl#getParameterTypes <em>Parameter Types</em>}</li>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3FunctionTypeImpl#isVarArgs <em>Var Args</em>}</li>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3FunctionTypeImpl#getTypeCalculator <em>Type Calculator</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class B3FunctionTypeImpl extends EObjectImpl implements B3FunctionType {
 	/**
 	 * The cached value of the '{@link #getFunctionType() <em>Function Type</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getFunctionType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type functionType;
 
 	/**
 	 * The cached value of the '{@link #getReturnType() <em>Return Type</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getReturnType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type returnType;
 
 	/**
 	 * The cached value of the '{@link #getParameterTypes() <em>Parameter Types</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getParameterTypes()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Type> parameterTypes;
 
 	/**
 	 * The default value of the '{@link #isVarArgs() <em>Var Args</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #isVarArgs()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean VAR_ARGS_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isVarArgs() <em>Var Args</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #isVarArgs()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean varArgs = VAR_ARGS_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getTypeCalculator() <em>Type Calculator</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getTypeCalculator()
 	 * @generated
 	 * @ordered
 	 */
 	protected BTypeCalculator typeCalculator;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected B3FunctionTypeImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Type basicGetFunctionType() {
 		return functionType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Type basicGetReturnType() {
 		return returnType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public BTypeCalculator basicGetTypeCalculator() {
 		return typeCalculator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch(featureID) {
 			case B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE:
 				if(resolve)
 					return getFunctionType();
 				return basicGetFunctionType();
 			case B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE:
 				if(resolve)
 					return getReturnType();
 				return basicGetReturnType();
 			case B3backendPackage.B3_FUNCTION_TYPE__PARAMETER_TYPES:
 				return getParameterTypes();
 			case B3backendPackage.B3_FUNCTION_TYPE__VAR_ARGS:
 				return isVarArgs();
 			case B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR:
 				if(resolve)
 					return getTypeCalculator();
 				return basicGetTypeCalculator();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch(featureID) {
 			case B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE:
 				return functionType != null;
 			case B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE:
 				return returnType != null;
 			case B3backendPackage.B3_FUNCTION_TYPE__PARAMETER_TYPES:
 				return parameterTypes != null && !parameterTypes.isEmpty();
 			case B3backendPackage.B3_FUNCTION_TYPE__VAR_ARGS:
 				return varArgs != VAR_ARGS_EDEFAULT;
 			case B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR:
 				return typeCalculator != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch(featureID) {
 			case B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE:
 				setFunctionType((Type) newValue);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE:
 				setReturnType((Type) newValue);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__PARAMETER_TYPES:
 				getParameterTypes().clear();
 				getParameterTypes().addAll((Collection<? extends Type>) newValue);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__VAR_ARGS:
 				setVarArgs((Boolean) newValue);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR:
 				setTypeCalculator((BTypeCalculator) newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch(featureID) {
 			case B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE:
 				setFunctionType((Type) null);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE:
 				setReturnType((Type) null);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__PARAMETER_TYPES:
 				getParameterTypes().clear();
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__VAR_ARGS:
 				setVarArgs(VAR_ARGS_EDEFAULT);
 				return;
 			case B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR:
 				setTypeCalculator((BTypeCalculator) null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Type getFunctionType() {
 		// Must set a default of BFunction as this may not always be known.
 		// if(functionType == null)
 		// setFunctionType(BFunction.class);
 		if(functionType != null && functionType instanceof EObject && ((EObject) functionType).eIsProxy()) {
 			InternalEObject oldFunctionType = (InternalEObject) functionType;
 			functionType = (Type) eResolveProxy(oldFunctionType);
 			if(functionType != oldFunctionType) {
 				if(eNotificationRequired())
 					eNotify(new ENotificationImpl(
 						this, Notification.RESOLVE, B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE, oldFunctionType,
 						functionType));
 			}
 		}
 		return functionType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<Type> getParameterTypes() {
 		if(parameterTypes == null) {
 			parameterTypes = new EObjectResolvingEList<Type>(
 				Type.class, this, B3backendPackage.B3_FUNCTION_TYPE__PARAMETER_TYPES);
 		}
 		return parameterTypes;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Type[] getParameterTypesArray() {
 		EList<Type> list = getParameterTypes();
 		Type[] ts = new Type[list.size()];
 		int i = 0;
 		for(Type t : list)
 			ts[i++] = t;
 		return ts;
 	}
 
 	// /**
 	// * <!-- begin-user-doc -->
 	// * <!-- end-user-doc -->
 	// *
 	// * @generated NOT
 	// */
 	// public String[] getParameterNamesArray() {
 	// EList<String> list = getParameterNames();
 	// String[] ts = new String[list.size()];
 	// int i = 0;
 	// for(String t : list)
 	// ts[i++] = t;
 	// return ts;
 	// }
 	/**
 	 * <!-- begin-user-doc -->
 	 * The generated version casts the return type to EObject. It may be just a java.lang.reflect imlementation.
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Type getReturnType() {
 		if(returnType != null && ((EObject) returnType).eIsProxy()) {
 			InternalEObject oldReturnType = (InternalEObject) returnType;
 			returnType = (Type) eResolveProxy(oldReturnType);
 			if(returnType != oldReturnType) {
 				if(eNotificationRequired())
 					eNotify(new ENotificationImpl(
 						this, Notification.RESOLVE, B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE, oldReturnType,
 						returnType));
 			}
 		}
 		return returnType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Type getReturnTypeForParameterTypes(Type[] types) {
 		BTypeCalculator tc = getTypeCalculator();
 		if(tc == null)
 			return getReturnType();
 		return tc.getSignature(types).getReturnType();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public BTypeCalculator getTypeCalculator() {
 		if(typeCalculator != null && typeCalculator.eIsProxy()) {
 			InternalEObject oldTypeCalculator = (InternalEObject) typeCalculator;
 			typeCalculator = (BTypeCalculator) eResolveProxy(oldTypeCalculator);
 			if(typeCalculator != oldTypeCalculator) {
 				if(eNotificationRequired())
 					eNotify(new ENotificationImpl(
 						this, Notification.RESOLVE, B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR,
 						oldTypeCalculator, typeCalculator));
 			}
 		}
 		return typeCalculator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns true if the function has same or more generic signature - i.e. where
 	 * returnType, and each parameterType is assignable from the corresponding value from type, they
 	 * have the same number of parameters and varargs flag.
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean isAssignableFrom(Type type) {
 		if(!(type instanceof B3FunctionType))
 			return false;
 		B3FunctionType fromType = (B3FunctionType) type;
 		if(!TypeUtils.isAssignableFrom(getReturnType(), fromType.getReturnType()))
 			return false;
 		if(isVarArgs() != fromType.isVarArgs())
 			return false;
 		Type[] p = getParameterTypesArray();
 		Type[] pt = fromType.getParameterTypesArray();
 		if(p.length != pt.length)
 			return false;
 		for(int i = 0; i < p.length; i++)
 			if(!TypeUtils.isAssignableFrom(p[i], pt[i]))
 				return false;
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isVarArgs() {
 		return varArgs;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void setFunctionType(Type newFunctionType) {
		setFunctionTypeGen(TypeUtils.coerceToEObjectType(newFunctionType));
 
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * functionType may not be an EObject, if not, no notification is generated.
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setFunctionTypeGen(Type newFunctionType) {
 		Type oldFunctionType = functionType;
 		functionType = newFunctionType;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, B3backendPackage.B3_FUNCTION_TYPE__FUNCTION_TYPE, oldFunctionType, functionType));
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void setReturnType(Type newReturnType) {
 		setReturnTypeGen(TypeUtils.coerceToEObjectType(newReturnType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * returnType may not be an EObject (in which case no eInverseRemove or eInverseAdd is performed).
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setReturnTypeGen(Type newReturnType) {
 		Type oldReturnType = returnType;
 		returnType = newReturnType;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, B3backendPackage.B3_FUNCTION_TYPE__RETURN_TYPE, oldReturnType, returnType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setTypeCalculator(BTypeCalculator newTypeCalculator) {
 		BTypeCalculator oldTypeCalculator = typeCalculator;
 		typeCalculator = newTypeCalculator;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, B3backendPackage.B3_FUNCTION_TYPE__TYPE_CALCULATOR, oldTypeCalculator,
 				typeCalculator));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setVarArgs(boolean newVarArgs) {
 		boolean oldVarArgs = varArgs;
 		varArgs = newVarArgs;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, B3backendPackage.B3_FUNCTION_TYPE__VAR_ARGS, oldVarArgs, varArgs));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if(eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (varArgs: ");
 		result.append(varArgs);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.B3_FUNCTION_TYPE;
 	}
 
 } // B3FunctionTypeImpl
