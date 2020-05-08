 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.beaninfo.impl;
 /*
  *  $RCSfile: PropertyDecoratorImpl.java,v $
 *  $Revision: 1.17 $  $Date: 2005/09/16 20:48:46 $ 
  */
 
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import org.eclipse.jem.internal.beaninfo.BeaninfoPackage;
 import org.eclipse.jem.internal.beaninfo.ImplicitItem;
 import org.eclipse.jem.internal.beaninfo.PropertyDecorator;
 import org.eclipse.jem.java.Field;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.Method;
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Property Decorator</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#isBound <em>Bound</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#isConstrained <em>Constrained</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#isDesignTime <em>Design Time</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#isAlwaysIncompatible <em>Always Incompatible</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getFilterFlags <em>Filter Flags</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#isFieldReadOnly <em>Field Read Only</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getPropertyEditorClass <em>Property Editor Class</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getReadMethod <em>Read Method</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getWriteMethod <em>Write Method</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getField <em>Field</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 
 
 public class PropertyDecoratorImpl extends FeatureDecoratorImpl implements PropertyDecorator{
 
 	/**
 	 * Bits for implicitly set features. This is internal, not meant for clients.
 	 */
 	public static final long PROPERTY_EDITOR_CLASS_IMPLICIT = 0x1L;
 	public static final long PROPERTY_TYPE_IMPLICIT = 0x2L;
 	public static final long PROPERTY_READMETHOD_IMPLICIT = 0x4L;
 	public static final long PROPERTY_WRITEMETHOD_IMPLICIT = 0x8L;
 	public static final long PROPERTY_BOUND_IMPLICIT = 0x10L;
 	public static final long PROPERTY_CONSTRAINED_IMPLICIT = 0x20L;
 	public static final long PROPERTY_DESIGNTIME_IMPLICIT = 0x40L;
 	public static final long PROPERTY_FIELD_IMPLICIT = 0x80L;
 	
 	
 	/**
 	 * The default value of the '{@link #isBound() <em>Bound</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isBound()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean BOUND_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isBound() <em>Bound</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isBound()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int BOUND_EFLAG = 1 << 18;
 
 	/**
 	 * The flag representing whether the Bound attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int BOUND_ESETFLAG = 1 << 19;
 
 	/**
 	 * The default value of the '{@link #isConstrained() <em>Constrained</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isConstrained()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean CONSTRAINED_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isConstrained() <em>Constrained</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isConstrained()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int CONSTRAINED_EFLAG = 1 << 20;
 
 	/**
 	 * The flag representing whether the Constrained attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int CONSTRAINED_ESETFLAG = 1 << 21;
 
 	/**
 	 * The default value of the '{@link #isDesignTime() <em>Design Time</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isDesignTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean DESIGN_TIME_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isDesignTime() <em>Design Time</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isDesignTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int DESIGN_TIME_EFLAG = 1 << 22;
 
 	/**
 	 * The flag representing whether the Design Time attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int DESIGN_TIME_ESETFLAG = 1 << 23;
 
 	/**
 	 * The default value of the '{@link #isAlwaysIncompatible() <em>Always Incompatible</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isAlwaysIncompatible()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean ALWAYS_INCOMPATIBLE_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isAlwaysIncompatible() <em>Always Incompatible</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isAlwaysIncompatible()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int ALWAYS_INCOMPATIBLE_EFLAG = 1 << 24;
 
 	/**
 	 * The cached value of the '{@link #getFilterFlags() <em>Filter Flags</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFilterFlags()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList filterFlags = null;
 	/**
 	 * The default value of the '{@link #isFieldReadOnly() <em>Field Read Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isFieldReadOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean FIELD_READ_ONLY_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isFieldReadOnly() <em>Field Read Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isFieldReadOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int FIELD_READ_ONLY_EFLAG = 1 << 25;
 
 	/**
 	 * The cached value of the '{@link #getPropertyEditorClass() <em>Property Editor Class</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPropertyEditorClass()
 	 * @generated
 	 * @ordered
 	 */
 	protected JavaClass propertyEditorClass = null;
 	/**
 	 * The cached value of the '{@link #getReadMethod() <em>Read Method</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReadMethod()
 	 * @generated
 	 * @ordered
 	 */
 	protected Method readMethod = null;
 	/**
 	 * The flag representing whether the Read Method reference has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int READ_METHOD_ESETFLAG = 1 << 26;
 
 	/**
 	 * The cached value of the '{@link #getWriteMethod() <em>Write Method</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getWriteMethod()
 	 * @generated
 	 * @ordered
 	 */
 	protected Method writeMethod = null;
 	
 	/**
 	 * The flag representing whether the Write Method reference has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int WRITE_METHOD_ESETFLAG = 1 << 27;
 
 	/**
 	 * The cached value of the '{@link #getField() <em>Field</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getField()
 	 * @generated
 	 * @ordered
 	 */
 	protected Field field = null;
 
 	/**
 	 * The flag representing whether the Field reference has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int FIELD_ESETFLAG = 1 << 28;
 
 	/**
 	 * This it the property type. If null, then it will
 	 * query against model element owner.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected EClassifier propertyType;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */	
 	protected PropertyDecoratorImpl() {
 		super();
 	}
 
 	protected String getSourceDefault() {
 		return PropertyDecorator.class.getName();
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return BeaninfoPackage.eINSTANCE.getPropertyDecorator();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isBound() {
 		return (eFlags & BOUND_EFLAG) != 0;
 	}
 
 	public EClassifier getPropertyType() {
		if (propertyType == null) {
 			EStructuralFeature feature = (EStructuralFeature) getEModelElement();
 			return (feature != null) ? feature.getEType() : null;
 		} else
 			return propertyType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyType(EClassifier propertyType) {
 		// TODO: implement this method
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBound(boolean newBound) {
 		boolean oldBound = (eFlags & BOUND_EFLAG) != 0;
 		if (newBound) eFlags |= BOUND_EFLAG; else eFlags &= ~BOUND_EFLAG;
 		boolean oldBoundESet = (eFlags & BOUND_ESETFLAG) != 0;
 		eFlags |= BOUND_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__BOUND, oldBound, newBound, !oldBoundESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetBound() {
 		boolean oldBound = (eFlags & BOUND_EFLAG) != 0;
 		boolean oldBoundESet = (eFlags & BOUND_ESETFLAG) != 0;
 		if (BOUND_EDEFAULT) eFlags |= BOUND_EFLAG; else eFlags &= ~BOUND_EFLAG;
 		eFlags &= ~BOUND_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__BOUND, oldBound, BOUND_EDEFAULT, oldBoundESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetBound() {
 		return (eFlags & BOUND_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isConstrained() {
 		return (eFlags & CONSTRAINED_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConstrained(boolean newConstrained) {
 		boolean oldConstrained = (eFlags & CONSTRAINED_EFLAG) != 0;
 		if (newConstrained) eFlags |= CONSTRAINED_EFLAG; else eFlags &= ~CONSTRAINED_EFLAG;
 		boolean oldConstrainedESet = (eFlags & CONSTRAINED_ESETFLAG) != 0;
 		eFlags |= CONSTRAINED_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED, oldConstrained, newConstrained, !oldConstrainedESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetConstrained() {
 		boolean oldConstrained = (eFlags & CONSTRAINED_EFLAG) != 0;
 		boolean oldConstrainedESet = (eFlags & CONSTRAINED_ESETFLAG) != 0;
 		if (CONSTRAINED_EDEFAULT) eFlags |= CONSTRAINED_EFLAG; else eFlags &= ~CONSTRAINED_EFLAG;
 		eFlags &= ~CONSTRAINED_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED, oldConstrained, CONSTRAINED_EDEFAULT, oldConstrainedESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetConstrained() {
 		return (eFlags & CONSTRAINED_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isDesignTime() {
 		return (eFlags & DESIGN_TIME_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDesignTime(boolean newDesignTime) {
 		boolean oldDesignTime = (eFlags & DESIGN_TIME_EFLAG) != 0;
 		if (newDesignTime) eFlags |= DESIGN_TIME_EFLAG; else eFlags &= ~DESIGN_TIME_EFLAG;
 		boolean oldDesignTimeESet = (eFlags & DESIGN_TIME_ESETFLAG) != 0;
 		eFlags |= DESIGN_TIME_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME, oldDesignTime, newDesignTime, !oldDesignTimeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetDesignTime() {
 		boolean oldDesignTime = (eFlags & DESIGN_TIME_EFLAG) != 0;
 		boolean oldDesignTimeESet = (eFlags & DESIGN_TIME_ESETFLAG) != 0;
 		if (DESIGN_TIME_EDEFAULT) eFlags |= DESIGN_TIME_EFLAG; else eFlags &= ~DESIGN_TIME_EFLAG;
 		eFlags &= ~DESIGN_TIME_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME, oldDesignTime, DESIGN_TIME_EDEFAULT, oldDesignTimeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetDesignTime() {
 		return (eFlags & DESIGN_TIME_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isAlwaysIncompatible() {
 		return (eFlags & ALWAYS_INCOMPATIBLE_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAlwaysIncompatible(boolean newAlwaysIncompatible) {
 		boolean oldAlwaysIncompatible = (eFlags & ALWAYS_INCOMPATIBLE_EFLAG) != 0;
 		if (newAlwaysIncompatible) eFlags |= ALWAYS_INCOMPATIBLE_EFLAG; else eFlags &= ~ALWAYS_INCOMPATIBLE_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE, oldAlwaysIncompatible, newAlwaysIncompatible));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getFilterFlags() {
 		if (filterFlags == null) {
 			filterFlags = new EDataTypeUniqueEList(String.class, this, BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS);
 		}
 		return filterFlags;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isFieldReadOnly() {
 		return (eFlags & FIELD_READ_ONLY_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFieldReadOnly(boolean newFieldReadOnly) {
 		boolean oldFieldReadOnly = (eFlags & FIELD_READ_ONLY_EFLAG) != 0;
 		if (newFieldReadOnly) eFlags |= FIELD_READ_ONLY_EFLAG; else eFlags &= ~FIELD_READ_ONLY_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__FIELD_READ_ONLY, oldFieldReadOnly, newFieldReadOnly));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JavaClass getPropertyEditorClass() {
 		if (propertyEditorClass != null && propertyEditorClass.eIsProxy()) {
 			JavaClass oldPropertyEditorClass = propertyEditorClass;
 			propertyEditorClass = (JavaClass)eResolveProxy((InternalEObject)propertyEditorClass);
 			if (propertyEditorClass != oldPropertyEditorClass) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS, oldPropertyEditorClass, propertyEditorClass));
 			}
 		}
 		return propertyEditorClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyEditorClass(JavaClass newPropertyEditorClass) {
 		JavaClass oldPropertyEditorClass = propertyEditorClass;
 		propertyEditorClass = newPropertyEditorClass;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS, oldPropertyEditorClass, propertyEditorClass));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method getReadMethod() {
 		if (readMethod != null && readMethod.eIsProxy()) {
 			Method oldReadMethod = readMethod;
 			readMethod = (Method)eResolveProxy((InternalEObject)readMethod);
 			if (readMethod != oldReadMethod) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD, oldReadMethod, readMethod));
 			}
 		}
 		return readMethod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReadMethod(Method newReadMethod) {
 		Method oldReadMethod = readMethod;
 		readMethod = newReadMethod;
 		boolean oldReadMethodESet = (eFlags & READ_METHOD_ESETFLAG) != 0;
 		eFlags |= READ_METHOD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD, oldReadMethod, readMethod, !oldReadMethodESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetReadMethod() {
 		Method oldReadMethod = readMethod;
 		boolean oldReadMethodESet = (eFlags & READ_METHOD_ESETFLAG) != 0;
 		readMethod = null;
 		eFlags &= ~READ_METHOD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD, oldReadMethod, null, oldReadMethodESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetReadMethod() {
 		return (eFlags & READ_METHOD_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method getWriteMethod() {
 		if (writeMethod != null && writeMethod.eIsProxy()) {
 			Method oldWriteMethod = writeMethod;
 			writeMethod = (Method)eResolveProxy((InternalEObject)writeMethod);
 			if (writeMethod != oldWriteMethod) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD, oldWriteMethod, writeMethod));
 			}
 		}
 		return writeMethod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setWriteMethod(Method newWriteMethod) {
 		Method oldWriteMethod = writeMethod;
 		writeMethod = newWriteMethod;
 		boolean oldWriteMethodESet = (eFlags & WRITE_METHOD_ESETFLAG) != 0;
 		eFlags |= WRITE_METHOD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD, oldWriteMethod, writeMethod, !oldWriteMethodESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetWriteMethod() {
 		Method oldWriteMethod = writeMethod;
 		boolean oldWriteMethodESet = (eFlags & WRITE_METHOD_ESETFLAG) != 0;
 		writeMethod = null;
 		eFlags &= ~WRITE_METHOD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD, oldWriteMethod, null, oldWriteMethodESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetWriteMethod() {
 		return (eFlags & WRITE_METHOD_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Field getField() {
 		if (field != null && field.eIsProxy()) {
 			Field oldField = field;
 			field = (Field)eResolveProxy((InternalEObject)field);
 			if (field != oldField) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BeaninfoPackage.PROPERTY_DECORATOR__FIELD, oldField, field));
 			}
 		}
 		return field;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Field basicGetField() {
 		return field;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setField(Field newField) {
 		Field oldField = field;
 		field = newField;
 		boolean oldFieldESet = (eFlags & FIELD_ESETFLAG) != 0;
 		eFlags |= FIELD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__FIELD, oldField, field, !oldFieldESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetField() {
 		Field oldField = field;
 		boolean oldFieldESet = (eFlags & FIELD_ESETFLAG) != 0;
 		field = null;
 		eFlags &= ~FIELD_ESETFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__FIELD, oldField, null, oldFieldESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetField() {
 		return (eFlags & FIELD_ESETFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (bound: ");
 		if ((eFlags & BOUND_ESETFLAG) != 0) result.append((eFlags & BOUND_EFLAG) != 0); else result.append("<unset>");
 		result.append(", constrained: ");
 		if ((eFlags & CONSTRAINED_ESETFLAG) != 0) result.append((eFlags & CONSTRAINED_EFLAG) != 0); else result.append("<unset>");
 		result.append(", designTime: ");
 		if ((eFlags & DESIGN_TIME_ESETFLAG) != 0) result.append((eFlags & DESIGN_TIME_EFLAG) != 0); else result.append("<unset>");
 		result.append(", alwaysIncompatible: ");
 		result.append((eFlags & ALWAYS_INCOMPATIBLE_EFLAG) != 0);
 		result.append(", filterFlags: ");
 		result.append(filterFlags);
 		result.append(", fieldReadOnly: ");
 		result.append((eFlags & FIELD_READ_ONLY_EFLAG) != 0);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JavaClass basicGetPropertyEditorClass() {
 		return propertyEditorClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method basicGetReadMethod() {
 		return readMethod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method basicGetWriteMethod() {
 		return writeMethod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 					return ((InternalEList)getEAnnotations()).basicAdd(otherEnd, msgs);
 				case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT, msgs);
 				default:
 					return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		if (eContainer != null)
 			msgs = eBasicRemoveFromContainer(msgs);
 		return eBasicSetContainer(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 					return ((InternalEList)getEAnnotations()).basicRemove(otherEnd, msgs);
 				case BeaninfoPackage.PROPERTY_DECORATOR__DETAILS:
 					return ((InternalEList)getDetails()).basicRemove(otherEnd, msgs);
 				case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 					return eBasicSetContainer(null, BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT, msgs);
 				case BeaninfoPackage.PROPERTY_DECORATOR__CONTENTS:
 					return ((InternalEList)getContents()).basicRemove(otherEnd, msgs);
 				case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 					return ((InternalEList)getAttributes()).basicRemove(otherEnd, msgs);
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
 	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
 		if (eContainerFeatureID >= 0) {
 			switch (eContainerFeatureID) {
 				case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 					return eContainer.eInverseRemove(this, EcorePackage.EMODEL_ELEMENT__EANNOTATIONS, EModelElement.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return eContainer.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 				return getEAnnotations();
 			case BeaninfoPackage.PROPERTY_DECORATOR__SOURCE:
 				return getSource();
 			case BeaninfoPackage.PROPERTY_DECORATOR__DETAILS:
 				return getDetails();
 			case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 				return getEModelElement();
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONTENTS:
 				return getContents();
 			case BeaninfoPackage.PROPERTY_DECORATOR__REFERENCES:
 				return getReferences();
 			case BeaninfoPackage.PROPERTY_DECORATOR__DISPLAY_NAME:
 				return getDisplayName();
 			case BeaninfoPackage.PROPERTY_DECORATOR__SHORT_DESCRIPTION:
 				return getShortDescription();
 			case BeaninfoPackage.PROPERTY_DECORATOR__CATEGORY:
 				return getCategory();
 			case BeaninfoPackage.PROPERTY_DECORATOR__EXPERT:
 				return isExpert() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__HIDDEN:
 				return isHidden() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PREFERRED:
 				return isPreferred() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__MERGE_INTROSPECTION:
 				return isMergeIntrospection() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT_EMPTY:
 				return isAttributesExplicitEmpty() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICITLY_SET_BITS:
 				return new Long(getImplicitlySetBits());
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICIT_DECORATOR_FLAG:
 				return getImplicitDecoratorFlag();
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 				return getAttributes();
 			case BeaninfoPackage.PROPERTY_DECORATOR__BOUND:
 				return isBound() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED:
 				return isConstrained() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME:
 				return isDesignTime() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE:
 				return isAlwaysIncompatible() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS:
 				return getFilterFlags();
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD_READ_ONLY:
 				return isFieldReadOnly() ? Boolean.TRUE : Boolean.FALSE;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				if (resolve) return getPropertyEditorClass();
 				return basicGetPropertyEditorClass();
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				if (resolve) return getReadMethod();
 				return basicGetReadMethod();
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				if (resolve) return getWriteMethod();
 				return basicGetWriteMethod();
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD:
 				if (resolve) return getField();
 				return basicGetField();
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 				getEAnnotations().clear();
 				getEAnnotations().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__SOURCE:
 				setSource((String)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DETAILS:
 				getDetails().clear();
 				getDetails().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 				setEModelElement((EModelElement)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONTENTS:
 				getContents().clear();
 				getContents().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__REFERENCES:
 				getReferences().clear();
 				getReferences().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DISPLAY_NAME:
 				setDisplayName((String)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__SHORT_DESCRIPTION:
 				setShortDescription((String)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CATEGORY:
 				setCategory((String)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__EXPERT:
 				setExpert(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__HIDDEN:
 				setHidden(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PREFERRED:
 				setPreferred(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__MERGE_INTROSPECTION:
 				setMergeIntrospection(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT_EMPTY:
 				setAttributesExplicitEmpty(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICITLY_SET_BITS:
 				setImplicitlySetBits(((Long)newValue).longValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICIT_DECORATOR_FLAG:
 				setImplicitDecoratorFlag((ImplicitItem)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 				getAttributes().clear();
 				getAttributes().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__BOUND:
 				setBound(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED:
 				setConstrained(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME:
 				setDesignTime(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE:
 				setAlwaysIncompatible(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS:
 				getFilterFlags().clear();
 				getFilterFlags().addAll((Collection)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD_READ_ONLY:
 				setFieldReadOnly(((Boolean)newValue).booleanValue());
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				setPropertyEditorClass((JavaClass)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				setReadMethod((Method)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				setWriteMethod((Method)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD:
 				setField((Field)newValue);
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 				getEAnnotations().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__SOURCE:
 				setSource(SOURCE_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DETAILS:
 				getDetails().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 				setEModelElement((EModelElement)null);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONTENTS:
 				getContents().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__REFERENCES:
 				getReferences().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DISPLAY_NAME:
 				unsetDisplayName();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__SHORT_DESCRIPTION:
 				unsetShortDescription();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CATEGORY:
 				setCategory(CATEGORY_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__EXPERT:
 				unsetExpert();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__HIDDEN:
 				unsetHidden();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PREFERRED:
 				unsetPreferred();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__MERGE_INTROSPECTION:
 				setMergeIntrospection(MERGE_INTROSPECTION_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT_EMPTY:
 				setAttributesExplicitEmpty(ATTRIBUTES_EXPLICIT_EMPTY_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICITLY_SET_BITS:
 				setImplicitlySetBits(IMPLICITLY_SET_BITS_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICIT_DECORATOR_FLAG:
 				setImplicitDecoratorFlag(IMPLICIT_DECORATOR_FLAG_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 				getAttributes().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__BOUND:
 				unsetBound();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED:
 				unsetConstrained();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME:
 				unsetDesignTime();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE:
 				setAlwaysIncompatible(ALWAYS_INCOMPATIBLE_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS:
 				getFilterFlags().clear();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD_READ_ONLY:
 				setFieldReadOnly(FIELD_READ_ONLY_EDEFAULT);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				setPropertyEditorClass((JavaClass)null);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				unsetReadMethod();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				unsetWriteMethod();
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD:
 				unsetField();
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.ecore.EObject#eIsSet(org.eclipse.emf.ecore.EStructuralFeature)
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case BeaninfoPackage.PROPERTY_DECORATOR__SOURCE:
 				return isSourceSet();	// Override so that if set to the same as classname, then it is considered not set.
 			default:
 				return eIsSetGen(eFeature);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSetGen(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case BeaninfoPackage.PROPERTY_DECORATOR__EANNOTATIONS:
 				return eAnnotations != null && !eAnnotations.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__SOURCE:
 				return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
 			case BeaninfoPackage.PROPERTY_DECORATOR__DETAILS:
 				return details != null && !details.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__EMODEL_ELEMENT:
 				return getEModelElement() != null;
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONTENTS:
 				return contents != null && !contents.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__REFERENCES:
 				return references != null && !references.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__DISPLAY_NAME:
 				return isSetDisplayName();
 			case BeaninfoPackage.PROPERTY_DECORATOR__SHORT_DESCRIPTION:
 				return isSetShortDescription();
 			case BeaninfoPackage.PROPERTY_DECORATOR__CATEGORY:
 				return CATEGORY_EDEFAULT == null ? category != null : !CATEGORY_EDEFAULT.equals(category);
 			case BeaninfoPackage.PROPERTY_DECORATOR__EXPERT:
 				return isSetExpert();
 			case BeaninfoPackage.PROPERTY_DECORATOR__HIDDEN:
 				return isSetHidden();
 			case BeaninfoPackage.PROPERTY_DECORATOR__PREFERRED:
 				return isSetPreferred();
 			case BeaninfoPackage.PROPERTY_DECORATOR__MERGE_INTROSPECTION:
 				return ((eFlags & MERGE_INTROSPECTION_EFLAG) != 0) != MERGE_INTROSPECTION_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT_EMPTY:
 				return ((eFlags & ATTRIBUTES_EXPLICIT_EMPTY_EFLAG) != 0) != ATTRIBUTES_EXPLICIT_EMPTY_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICITLY_SET_BITS:
 				return implicitlySetBits != IMPLICITLY_SET_BITS_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__IMPLICIT_DECORATOR_FLAG:
 				return implicitDecoratorFlag != IMPLICIT_DECORATOR_FLAG_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 				return attributes != null && !attributes.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__BOUND:
 				return isSetBound();
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED:
 				return isSetConstrained();
 			case BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME:
 				return isSetDesignTime();
 			case BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE:
 				return ((eFlags & ALWAYS_INCOMPATIBLE_EFLAG) != 0) != ALWAYS_INCOMPATIBLE_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS:
 				return filterFlags != null && !filterFlags.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD_READ_ONLY:
 				return ((eFlags & FIELD_READ_ONLY_EFLAG) != 0) != FIELD_READ_ONLY_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				return propertyEditorClass != null;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				return isSetReadMethod();
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				return isSetWriteMethod();
 			case BeaninfoPackage.PROPERTY_DECORATOR__FIELD:
 				return isSetField();
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.beaninfo.PropertyDecorator#isWriteable()
 	 */
 	public boolean isWriteable() {
 		return getWriteMethod() != null || (getField() != null && !isFieldReadOnly()); 
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.beaninfo.PropertyDecorator#isReadable()
 	 */
 	public boolean isReadable() {
 		return getReadMethod() != null || getField() != null;
 	}
 	
 
 }
