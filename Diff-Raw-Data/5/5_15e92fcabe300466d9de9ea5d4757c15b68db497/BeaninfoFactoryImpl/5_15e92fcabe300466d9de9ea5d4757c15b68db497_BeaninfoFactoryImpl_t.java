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
  *  $RCSfile: BeaninfoFactoryImpl.java,v $
 *  $Revision: 1.10 $  $Date: 2005/10/11 21:26:28 $ 
  */
 
 import java.util.Map;
 
 import org.eclipse.jem.internal.beaninfo.*;
 import org.eclipse.jem.internal.beaninfo.common.FeatureAttributeValue;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 
 
 public class BeaninfoFactoryImpl extends EFactoryImpl implements BeaninfoFactory{
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */	
 	public BeaninfoFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case BeaninfoPackage.FEATURE_DECORATOR: return createFeatureDecorator();
 			case BeaninfoPackage.BEAN_DECORATOR: return createBeanDecorator();
 			case BeaninfoPackage.EVENT_SET_DECORATOR: return createEventSetDecorator();
 			case BeaninfoPackage.METHOD_DECORATOR: return createMethodDecorator();
 			case BeaninfoPackage.PARAMETER_DECORATOR: return createParameterDecorator();
 			case BeaninfoPackage.PROPERTY_DECORATOR: return createPropertyDecorator();
 			case BeaninfoPackage.INDEXED_PROPERTY_DECORATOR: return createIndexedPropertyDecorator();
 			case BeaninfoPackage.METHOD_PROXY: return createMethodProxy();
 			case BeaninfoPackage.BEAN_EVENT: return createBeanEvent();
 			case BeaninfoPackage.FEATURE_ATTRIBUTE_MAP_ENTRY: return (EObject)createFeatureAttributeMapEntry();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object createFromString(EDataType eDataType, String initialValue) {
 		switch (eDataType.getClassifierID()) {
 			case BeaninfoPackage.IMPLICIT_ITEM: {
 				ImplicitItem result = ImplicitItem.get(initialValue);
 				if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
 				return result;
 			}
 			case BeaninfoPackage.FEATURE_ATTRIBUTE_VALUE:
 				return createFeatureAttributeValueFromString(eDataType, initialValue);
 			default:
 				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String convertToString(EDataType eDataType, Object instanceValue) {
 		switch (eDataType.getClassifierID()) {
 			case BeaninfoPackage.IMPLICIT_ITEM:
 				return instanceValue == null ? null : instanceValue.toString();
 			case BeaninfoPackage.FEATURE_ATTRIBUTE_VALUE:
 				return convertFeatureAttributeValueToString(eDataType, instanceValue);
 			default:
 				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public FeatureDecorator createFeatureDecorator() {
 		FeatureDecoratorImpl featureDecorator = new FeatureDecoratorImpl();
 		featureDecorator.setSource(FeatureDecorator.class.getName());
 		return featureDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public EventSetDecorator createEventSetDecorator() {
 		EventSetDecoratorImpl eventSetDecorator = new EventSetDecoratorImpl();
 		eventSetDecorator.setSource(EventSetDecorator.class.getName());
 		return eventSetDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MethodProxy createMethodProxy() {
 		MethodProxyImpl methodProxy = new MethodProxyImpl();
 		return methodProxy;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public PropertyDecorator createPropertyDecorator() {
 		PropertyDecoratorImpl propertyDecorator = new PropertyDecoratorImpl();
		propertyDecorator.setSource(PropertyDecorator.class.getName());
 		return propertyDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public IndexedPropertyDecorator createIndexedPropertyDecorator() {
 		IndexedPropertyDecoratorImpl indexedPropertyDecorator = new IndexedPropertyDecoratorImpl();
 		indexedPropertyDecorator.setSource(IndexedPropertyDecorator.class.getName());
 		return indexedPropertyDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public BeanDecorator createBeanDecorator() {
 		BeanDecoratorImpl beanDecorator = new BeanDecoratorImpl();
 		beanDecorator.setSource(BeanDecorator.class.getName());
 		return beanDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public MethodDecorator createMethodDecorator() {
 		MethodDecoratorImpl methodDecorator = new MethodDecoratorImpl();
 		methodDecorator.setSource(MethodDecorator.class.getName());
 		return methodDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public ParameterDecorator createParameterDecorator() {
 		ParameterDecoratorImpl parameterDecorator = new ParameterDecoratorImpl();
 		parameterDecorator.setSource(ParameterDecorator.class.getName());
 		return parameterDecorator;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BeaninfoPackage getBeaninfoPackage() {
 		return (BeaninfoPackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	public static BeaninfoPackage getPackage() {
 		return BeaninfoPackage.eINSTANCE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BeanEvent createBeanEvent() {
 		BeanEventImpl beanEvent = new BeanEventImpl();
 		return beanEvent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Map.Entry createFeatureAttributeMapEntry() {
 		FeatureAttributeMapEntryImpl featureAttributeMapEntry = new FeatureAttributeMapEntryImpl();
 		return featureAttributeMapEntry;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public FeatureAttributeValue createFeatureAttributeValueFromString(EDataType eDataType, String initialValue) {
 		return new FeatureAttributeValue(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	public String convertFeatureAttributeValueToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue.toString();
 	}
 
 }
