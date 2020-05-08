 /**
  * Copyright (c) 2009, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  * 
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 
 import org.eclipse.b3.backend.core.B3BackendActivator;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>B3 Java Import</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3JavaImportImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3JavaImportImpl#getQualifiedName <em>Qualified Name</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3JavaImportImpl#getType <em>Type</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3JavaImportImpl#isReexport <em>Reexport</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class B3JavaImportImpl extends EObjectImpl implements B3JavaImport {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getQualifiedName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String QUALIFIED_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getQualifiedName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String qualifiedName = QUALIFIED_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Type TYPE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type type = TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isReexport() <em>Reexport</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isReexport()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean REEXPORT_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isReexport() <em>Reexport</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isReexport()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean reexport = REEXPORT_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected B3JavaImportImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.B3_JAVA_IMPORT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_JAVA_IMPORT__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getQualifiedName() {
 		return qualifiedName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Also sets name to last part of qualified name if name is null.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setQualifiedName(String newQualifiedName) {
 		String oldQualifiedName = qualifiedName;
 		qualifiedName = newQualifiedName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME, oldQualifiedName, qualifiedName));
 		// set the name if not set (unless the qualified name is also null)
 		if(name == null && qualifiedName != null) {
 			int lastDot = qualifiedName.lastIndexOf('.');
 			if(lastDot < 0)
 				setName(qualifiedName);
 			else
 				setName(qualifiedName.substring(lastDot+1));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Resolves the type (by asking the bundle to load the class having qualifiedName) if not already done.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Type getType() {
 		if(type == null) {
 			try {
 				setType(B3BackendActivator.instance.getBundle().loadClass(getQualifiedName()));
 			} catch (ClassNotFoundException e) {
				// TODO: ?? Need to handle this, so the "not found" surfaces to the user...
				// Handled via model validation - type can not be null
 			}
 		}
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(Type newType) {
 		Type oldType = type;
 		type = newType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_JAVA_IMPORT__TYPE, oldType, type));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isReexport() {
 		return reexport;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReexport(boolean newReexport) {
 		boolean oldReexport = reexport;
 		reexport = newReexport;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_JAVA_IMPORT__REEXPORT, oldReexport, reexport));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case B3backendPackage.B3_JAVA_IMPORT__NAME:
 				return getName();
 			case B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME:
 				return getQualifiedName();
 			case B3backendPackage.B3_JAVA_IMPORT__TYPE:
 				return getType();
 			case B3backendPackage.B3_JAVA_IMPORT__REEXPORT:
 				return isReexport();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case B3backendPackage.B3_JAVA_IMPORT__NAME:
 				setName((String)newValue);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME:
 				setQualifiedName((String)newValue);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__TYPE:
 				setType((Type)newValue);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__REEXPORT:
 				setReexport((Boolean)newValue);
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
 			case B3backendPackage.B3_JAVA_IMPORT__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME:
 				setQualifiedName(QUALIFIED_NAME_EDEFAULT);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__TYPE:
 				setType(TYPE_EDEFAULT);
 				return;
 			case B3backendPackage.B3_JAVA_IMPORT__REEXPORT:
 				setReexport(REEXPORT_EDEFAULT);
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
 			case B3backendPackage.B3_JAVA_IMPORT__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME:
 				return QUALIFIED_NAME_EDEFAULT == null ? qualifiedName != null : !QUALIFIED_NAME_EDEFAULT.equals(qualifiedName);
 			case B3backendPackage.B3_JAVA_IMPORT__TYPE:
 				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
 			case B3backendPackage.B3_JAVA_IMPORT__REEXPORT:
 				return reexport != REEXPORT_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer("java import: ");
 		result.append(qualifiedName);
 		if(!qualifiedName.endsWith(name)) {
 			result.append(" as: ");
 			result.append(name);
 		}
 		if(reexport)
 			result.append(" rexported");
 		return result.toString();
 	}
 
 } //B3JavaImportImpl
