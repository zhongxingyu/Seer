 package org.eclipse.jem.internal.beaninfo.impl;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: PropertyDecoratorImpl.java,v $
 *  $Revision: 1.4 $  $Date: 2004/03/30 15:56:06 $ 
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
 import org.eclipse.jem.internal.beaninfo.PropertyDecorator;
 import org.eclipse.jem.internal.beaninfo.adapters.BeaninfoProxyConstants;
 import org.eclipse.jem.internal.beaninfo.core.Utilities;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.Method;
 import org.eclipse.jem.internal.proxy.core.IBeanProxy;
 import org.eclipse.jem.internal.proxy.core.IBeanTypeProxy;
 import org.eclipse.jem.internal.proxy.core.IBooleanBeanProxy;
 import org.eclipse.jem.internal.proxy.core.IMethodProxy;
 import org.eclipse.jem.internal.proxy.core.ThrowableProxy;
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
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getPropertyEditorClass <em>Property Editor Class</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getReadMethod <em>Read Method</em>}</li>
  *   <li>{@link org.eclipse.jem.internal.beaninfo.impl.PropertyDecoratorImpl#getWriteMethod <em>Write Method</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 
 
 public class PropertyDecoratorImpl extends FeatureDecoratorImpl implements PropertyDecorator{
 
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
 	 * The cached value of the '{@link #isBound() <em>Bound</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isBound()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean bound = BOUND_EDEFAULT;
 
 	/**
 	 * This is true if the Bound attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean boundESet = false;
 
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
 	 * The cached value of the '{@link #isConstrained() <em>Constrained</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isConstrained()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean constrained = CONSTRAINED_EDEFAULT;
 
 	/**
 	 * This is true if the Constrained attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean constrainedESet = false;
 
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
 	 * The cached value of the '{@link #isDesignTime() <em>Design Time</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isDesignTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean designTime = DESIGN_TIME_EDEFAULT;
 
 	/**
 	 * This is true if the Design Time attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean designTimeESet = false;
 
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
 	 * The cached value of the '{@link #isAlwaysIncompatible() <em>Always Incompatible</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isAlwaysIncompatible()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean alwaysIncompatible = ALWAYS_INCOMPATIBLE_EDEFAULT;
 
 	// getting read/write methods are expensive, and they are called very often in parsing, so we will cache them
 	private Method cachedReadMethod, cachedWriteMethod;
 	boolean retrievedReadMethod, retrievedWriteMethod;
 		
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
 	 * The cached value of the '{@link #getWriteMethod() <em>Write Method</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getWriteMethod()
 	 * @generated
 	 * @ordered
 	 */
 	protected Method writeMethod = null;
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */	
 	protected PropertyDecoratorImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return BeaninfoPackage.eINSTANCE.getPropertyDecorator();
 	}
 
 	public boolean isBound() {
 		if (!isSetBound()) {
 			if (validProxy(fFeatureProxy)) {
 				try {
 					return ((IBooleanBeanProxy) BeaninfoProxyConstants.getConstants(fFeatureProxy.getProxyFactoryRegistry()).getIsBoundProxy().invoke(fFeatureProxy)).booleanValue();
 				} catch (ThrowableProxy e) {
 				}
 			} else
 				if (fFeatureDecoratorProxy != null)
 					return ((PropertyDecorator) fFeatureDecoratorProxy).isBound();
 		}
 					
 		return this.isBoundGen();
 	}
 	public boolean isConstrained() {
 		if (!isSetConstrained()) {
 			if (validProxy(fFeatureProxy)) {
 				try {
 					return ((IBooleanBeanProxy) BeaninfoProxyConstants.getConstants(fFeatureProxy.getProxyFactoryRegistry()).getIsConstrainedProxy().invoke(fFeatureProxy)).booleanValue();
 				} catch (ThrowableProxy e) {
 				}
 			} else
 				if (fFeatureDecoratorProxy != null)
 					return ((PropertyDecorator) fFeatureDecoratorProxy).isConstrained();
 		}
 									
 		return this.isConstrainedGen();
 	}
 	public boolean isDesignTime() {
 		if (validProxy(fFeatureProxy) && !this.isSetDesignTimeGen()) {
 			getAttributes();	// This will cause the isDesignTime flag to be set if found.
 			return isDesignTimeProxy;	// Return the value
 		}
 		
 		return this.isDesignTimeGen();
 	}
 	public boolean isSetDesignTime() {
 		if (validProxy(fFeatureProxy) && !this.isSetDesignTimeGen()) {
 			getAttributes();	// This will cause the isDesignTime flag to be set if found.
 			return setIsDesignTimeProxy;	// Return whether set or not.
 		}
 		
 		return this.isSetDesignTimeGen();
 	}
 	public JavaClass getPropertyEditorClass() {
 		if (validProxy(fFeatureProxy) && !this.eIsSet(BeaninfoPackage.eINSTANCE.getPropertyDecorator_PropertyEditorClass()))
 			try {
 				return (JavaClass) Utilities.getJavaClass((IBeanTypeProxy) BeaninfoProxyConstants.getConstants(fFeatureProxy.getProxyFactoryRegistry()).getPropertyEditorClassProxy().invoke(fFeatureProxy),  getEModelElement().eResource().getResourceSet());
 			} catch (ThrowableProxy e) {
 			};
 					
 		return this.getPropertyEditorClassGen();
 	}
 	
 	public Method getReadMethod() {
 		if (validProxy(fFeatureProxy) && !this.eIsSet(BeaninfoPackage.eINSTANCE.getPropertyDecorator_ReadMethod())) {
 			if (validProxy(fFeatureProxy)) {
				if (retrievedReadMethod && cachedReadMethod.eContainer() == null) {
 					clearCache();	// Method has been removed or refreshed, so we have a new one to get instead.
 				}
 				if (!retrievedReadMethod) {
 					try {
 						cachedReadMethod = Utilities.getMethod((IMethodProxy) BeaninfoProxyConstants.getConstants(fFeatureProxy.getProxyFactoryRegistry()).getReadMethodProxy().invoke(fFeatureProxy),  getEModelElement().eResource().getResourceSet());
 						retrievedReadMethod = true;
 					} catch (ThrowableProxy e) {
 					}
 				}
 				return cachedReadMethod;
 			} else
 				if (fFeatureDecoratorProxy != null)
 					return ((PropertyDecorator) fFeatureDecoratorProxy).getReadMethod();
 		}
 									
 		return this.getReadMethodGen();
 	}
 	public Method getWriteMethod() {
 		if (validProxy(fFeatureProxy) && !this.eIsSet(BeaninfoPackage.eINSTANCE.getPropertyDecorator_WriteMethod())) {
 			if (validProxy(fFeatureProxy)) {
				if (retrievedReadMethod && cachedReadMethod.eContainer() == null) {
 					clearCache();	// Method has been removed or refreshed, so we have a new one to get instead.
 				}				
 				if (!retrievedWriteMethod) {
 					try {
 						cachedWriteMethod = Utilities.getMethod((IMethodProxy) BeaninfoProxyConstants.getConstants(fFeatureProxy.getProxyFactoryRegistry()).getWriteMethodProxy().invoke(fFeatureProxy),  getEModelElement().eResource().getResourceSet());
 						retrievedWriteMethod = true;
 					} catch (ThrowableProxy e) {
 					}
 				}
 				return cachedWriteMethod;
 			} else
 				if (fFeatureDecoratorProxy != null)
 					return	((PropertyDecorator) fFeatureDecoratorProxy).getWriteMethod();
 		}
 									
 		return this.getWriteMethodGen();
 	}
 	
 	public EClassifier getPropertyType() {
 		EStructuralFeature feature = (EStructuralFeature) getEModelElement();
 		return (feature != null) ? feature.getEType() : null;
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isBoundGen() {
 		return bound;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBound(boolean newBound) {
 		boolean oldBound = bound;
 		bound = newBound;
 		boolean oldBoundESet = boundESet;
 		boundESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__BOUND, oldBound, bound, !oldBoundESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetBound() {
 		boolean oldBound = bound;
 		boolean oldBoundESet = boundESet;
 		bound = BOUND_EDEFAULT;
 		boundESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__BOUND, oldBound, BOUND_EDEFAULT, oldBoundESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetBound() {
 		return boundESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isConstrainedGen() {
 		return constrained;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConstrained(boolean newConstrained) {
 		boolean oldConstrained = constrained;
 		constrained = newConstrained;
 		boolean oldConstrainedESet = constrainedESet;
 		constrainedESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED, oldConstrained, constrained, !oldConstrainedESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetConstrained() {
 		boolean oldConstrained = constrained;
 		boolean oldConstrainedESet = constrainedESet;
 		constrained = CONSTRAINED_EDEFAULT;
 		constrainedESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED, oldConstrained, CONSTRAINED_EDEFAULT, oldConstrainedESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetConstrained() {
 		return constrainedESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isDesignTimeGen() {
 		return designTime;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDesignTime(boolean newDesignTime) {
 		boolean oldDesignTime = designTime;
 		designTime = newDesignTime;
 		boolean oldDesignTimeESet = designTimeESet;
 		designTimeESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME, oldDesignTime, designTime, !oldDesignTimeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetDesignTime() {
 		boolean oldDesignTime = designTime;
 		boolean oldDesignTimeESet = designTimeESet;
 		designTime = DESIGN_TIME_EDEFAULT;
 		designTimeESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME, oldDesignTime, DESIGN_TIME_EDEFAULT, oldDesignTimeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetDesignTimeGen() {
 		return designTimeESet;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isAlwaysIncompatible() {
 		return alwaysIncompatible;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAlwaysIncompatible(boolean newAlwaysIncompatible) {
 		boolean oldAlwaysIncompatible = alwaysIncompatible;
 		alwaysIncompatible = newAlwaysIncompatible;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE, oldAlwaysIncompatible, alwaysIncompatible));
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
 	public void setReadMethod(Method newReadMethod) {
 		Method oldReadMethod = readMethod;
 		readMethod = newReadMethod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD, oldReadMethod, readMethod));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setWriteMethod(Method newWriteMethod) {
 		Method oldWriteMethod = writeMethod;
 		writeMethod = newWriteMethod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD, oldWriteMethod, writeMethod));
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
 		if (boundESet) result.append(bound); else result.append("<unset>");
 		result.append(", constrained: ");
 		if (constrainedESet) result.append(constrained); else result.append("<unset>");
 		result.append(", designTime: ");
 		if (designTimeESet) result.append(designTime); else result.append("<unset>");
 		result.append(", alwaysIncompatible: ");
 		result.append(alwaysIncompatible);
 		result.append(", filterFlags: ");
 		result.append(filterFlags);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JavaClass getPropertyEditorClassGen() {
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
 	public JavaClass basicGetPropertyEditorClass() {
 		return propertyEditorClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method getReadMethodGen() {
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
 	public Method basicGetReadMethod() {
 		return readMethod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Method getWriteMethodGen() {
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
 					return ((InternalEObject)eContainer).eInverseRemove(this, EcorePackage.EMODEL_ELEMENT__EANNOTATIONS, EModelElement.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return ((InternalEObject)eContainer).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT:
 				return isAttributesExplicit() ? Boolean.TRUE : Boolean.FALSE;
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				if (resolve) return getPropertyEditorClass();
 				return basicGetPropertyEditorClass();
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				if (resolve) return getReadMethod();
 				return basicGetReadMethod();
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				if (resolve) return getWriteMethod();
 				return basicGetWriteMethod();
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT:
 				setAttributesExplicit(((Boolean)newValue).booleanValue());
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				setPropertyEditorClass((JavaClass)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				setReadMethod((Method)newValue);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				setWriteMethod((Method)newValue);
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT:
 				setAttributesExplicit(ATTRIBUTES_EXPLICIT_EDEFAULT);
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
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				setPropertyEditorClass((JavaClass)null);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				setReadMethod((Method)null);
 				return;
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				setWriteMethod((Method)null);
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
 				return mergeIntrospection != MERGE_INTROSPECTION_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES_EXPLICIT:
 				return attributesExplicit != ATTRIBUTES_EXPLICIT_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__ATTRIBUTES:
 				return attributes != null && !attributes.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__BOUND:
 				return isSetBound();
 			case BeaninfoPackage.PROPERTY_DECORATOR__CONSTRAINED:
 				return isSetConstrained();
 			case BeaninfoPackage.PROPERTY_DECORATOR__DESIGN_TIME:
 				return isSetDesignTime();
 			case BeaninfoPackage.PROPERTY_DECORATOR__ALWAYS_INCOMPATIBLE:
 				return alwaysIncompatible != ALWAYS_INCOMPATIBLE_EDEFAULT;
 			case BeaninfoPackage.PROPERTY_DECORATOR__FILTER_FLAGS:
 				return filterFlags != null && !filterFlags.isEmpty();
 			case BeaninfoPackage.PROPERTY_DECORATOR__PROPERTY_EDITOR_CLASS:
 				return propertyEditorClass != null;
 			case BeaninfoPackage.PROPERTY_DECORATOR__READ_METHOD:
 				return readMethod != null;
 			case BeaninfoPackage.PROPERTY_DECORATOR__WRITE_METHOD:
 				return writeMethod != null;
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @see org.eclipse.jem.internal.beaninfo.FeatureDecorator#setDescriptorProxy(IBeanProxy)
 	 */
 	public void setDescriptorProxy(IBeanProxy descriptor) {
 		super.setDescriptorProxy(descriptor);
 		clearCache();
 		
 	}
 	
 	private void clearCache() {
 		if (retrievedReadMethod) {
 			retrievedReadMethod = false;
 			cachedReadMethod = null;
 		}
 		if (retrievedWriteMethod) {
 			retrievedWriteMethod = false;
 			cachedWriteMethod = null;
 		}
 	}
 
 }
