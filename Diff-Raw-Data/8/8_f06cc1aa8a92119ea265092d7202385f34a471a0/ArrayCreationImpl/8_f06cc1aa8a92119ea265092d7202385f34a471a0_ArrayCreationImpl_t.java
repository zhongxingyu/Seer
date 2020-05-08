 package org.eclipse.jem.internal.instantiation.impl;
 /*******************************************************************************
  * Copyright (c)  2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ArrayCreationImpl.java,v $
 *  $Revision: 1.2 $  $Date: 2004/01/21 16:41:18 $ 
  */
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import org.eclipse.jem.internal.instantiation.*;
 import org.eclipse.jem.internal.instantiation.ArrayCreation;
 import org.eclipse.jem.internal.instantiation.ArrayInitializer;
 import org.eclipse.jem.internal.instantiation.Expression;
 import org.eclipse.jem.internal.instantiation.InstantiationPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Array Creation</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jem.internal.instantiation.impl.ArrayCreationImpl#getType <em>Type</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.instantiation.impl.ArrayCreationImpl#getDimensions <em>Dimensions</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.instantiation.impl.ArrayCreationImpl#getInitializer <em>Initializer</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ArrayCreationImpl extends ExpressionImpl implements ArrayCreation {
 	/**
 	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TYPE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected String type = TYPE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getDimensions() <em>Dimensions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDimensions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList dimensions = null;
 
 	/**
 	 * The cached value of the '{@link #getInitializer() <em>Initializer</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInitializer()
 	 * @generated
 	 * @ordered
 	 */
 	protected ArrayInitializer initializer = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ArrayCreationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return InstantiationPackage.eINSTANCE.getArrayCreation();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(String newType) {
 		String oldType = type;
 		type = newType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, InstantiationPackage.ARRAY_CREATION__TYPE, oldType, type));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getDimensions() {
 		if (dimensions == null) {
 			dimensions = new EObjectContainmentEList(Expression.class, this, InstantiationPackage.ARRAY_CREATION__DIMENSIONS);
 		}
 		return dimensions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ArrayInitializer getInitializer() {
 		return initializer;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetInitializer(ArrayInitializer newInitializer, NotificationChain msgs) {
 		ArrayInitializer oldInitializer = initializer;
 		initializer = newInitializer;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, InstantiationPackage.ARRAY_CREATION__INITIALIZER, oldInitializer, newInitializer);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setInitializer(ArrayInitializer newInitializer) {
 		if (newInitializer != initializer) {
 			NotificationChain msgs = null;
 			if (initializer != null)
 				msgs = ((InternalEObject)initializer).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - InstantiationPackage.ARRAY_CREATION__INITIALIZER, null, msgs);
 			if (newInitializer != null)
 				msgs = ((InternalEObject)newInitializer).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - InstantiationPackage.ARRAY_CREATION__INITIALIZER, null, msgs);
 			msgs = basicSetInitializer(newInitializer, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, InstantiationPackage.ARRAY_CREATION__INITIALIZER, newInitializer, newInitializer));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case InstantiationPackage.ARRAY_CREATION__DIMENSIONS:
 					return ((InternalEList)getDimensions()).basicRemove(otherEnd, msgs);
 				case InstantiationPackage.ARRAY_CREATION__INITIALIZER:
 					return basicSetInitializer(null, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case InstantiationPackage.ARRAY_CREATION__TYPE:
 				return getType();
 			case InstantiationPackage.ARRAY_CREATION__DIMENSIONS:
 				return getDimensions();
 			case InstantiationPackage.ARRAY_CREATION__INITIALIZER:
 				return getInitializer();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case InstantiationPackage.ARRAY_CREATION__TYPE:
 				setType((String)newValue);
 				return;
 			case InstantiationPackage.ARRAY_CREATION__DIMENSIONS:
 				getDimensions().clear();
 				getDimensions().addAll((Collection)newValue);
 				return;
 			case InstantiationPackage.ARRAY_CREATION__INITIALIZER:
 				setInitializer((ArrayInitializer)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case InstantiationPackage.ARRAY_CREATION__TYPE:
 				setType(TYPE_EDEFAULT);
 				return;
 			case InstantiationPackage.ARRAY_CREATION__DIMENSIONS:
 				getDimensions().clear();
 				return;
 			case InstantiationPackage.ARRAY_CREATION__INITIALIZER:
 				setInitializer((ArrayInitializer)null);
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case InstantiationPackage.ARRAY_CREATION__TYPE:
 				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
 			case InstantiationPackage.ARRAY_CREATION__DIMENSIONS:
 				return dimensions != null && !dimensions.isEmpty();
 			case InstantiationPackage.ARRAY_CREATION__INITIALIZER:
 				return initializer != null;
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		if (getType() == null || (getDimensions().isEmpty() && getInitializer() == null))
 			return super.toString();
 		String type = getType();
 		StringBuffer buf = new StringBuffer(100);
 		buf.append("new ");
 		int ob = type.indexOf('[');
 		buf.append(type.substring(0, ob));
 		int realdims = 0;
 		while (ob != -1) {
 			realdims++;
			ob = type.indexOf('[',ob+1);
 		}
 		List dims = getDimensions();
 		for (int i = 0; i < dims.size(); i++) {
 			buf.append('[');
 			buf.append(dims.get(i));
 			buf.append(']');
 		}
		for (int i=dims.size(); i < realdims; i++) {
 			buf.append("[]");
 		}
 		
 		if (getInitializer() != null) {
 			buf.append(' ');
 			buf.append(getInitializer().toString());
 		}
 		
 		return buf.toString();
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.instantiation.impl.ExpressionImpl#accept0(org.eclipse.jem.internal.instantiation.ParseVisitor)
 	 */
 	protected void accept0(ParseVisitor visitor) {
 		boolean visitChildren = visitor.visit(this);
 		if (visitChildren) {
 			// visit children in normal left to right reading order
 			acceptChildren(visitor, dimensions);
 			acceptChild(visitor, getInitializer());
 		}
 		visitor.endVisit(this);
 	}	
 } //ArrayCreationImpl
