 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.context.symbol.internal.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.context.symbol.IJavaTypeDescriptor2;
 import org.eclipse.jst.jsf.context.symbol.SymbolPackage;
 
 
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.jst.jsf.context.symbol.IJavaTypeDescriptor2} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class IJavaTypeDescriptor2ItemProvider
 	extends ITypeDescriptorItemProvider
 	implements	
 		IEditingDomainItemProvider,	
 		IStructuredItemContentProvider,	
 		ITreeItemContentProvider,	
 		IItemLabelProvider,	
 		IItemPropertySource {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
     public static final String copyright = "Copyright 2006 Oracle"; //$NON-NLS-1$
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
      * @param adapterFactory 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IJavaTypeDescriptor2ItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
      * @param object 
      * @return the emf property descriptors 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addTypePropertyDescriptor(object);
 			addBeanPropertiesPropertyDescriptor(object);
 			addBeanMethodsPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Type feature.
 	 * <!-- begin-user-doc -->
      * @param object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addTypePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IJavaTypeDescriptor2_type_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_IJavaTypeDescriptor2_type_feature", "_UI_IJavaTypeDescriptor2_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 SymbolPackage.Literals.IJAVA_TYPE_DESCRIPTOR2__TYPE,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Bean Properties feature.
 	 * <!-- begin-user-doc -->
      * @param object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addBeanPropertiesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IJavaTypeDescriptor2_beanProperties_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_IJavaTypeDescriptor2_beanProperties_feature", "_UI_IJavaTypeDescriptor2_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 SymbolPackage.Literals.IJAVA_TYPE_DESCRIPTOR2__BEAN_PROPERTIES,
 				 true,
 				 false,
 				 false,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Bean Methods feature.
 	 * <!-- begin-user-doc -->
      * @param object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addBeanMethodsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IJavaTypeDescriptor2_beanMethods_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_IJavaTypeDescriptor2_beanMethods_feature", "_UI_IJavaTypeDescriptor2_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 SymbolPackage.Literals.IJAVA_TYPE_DESCRIPTOR2__BEAN_METHODS,
 				 true,
 				 false,
 				 false,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
      * This adds a property descriptor for the Array Count feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
	 * @param object 
      * @generated
      */
     protected void addArrayCountPropertyDescriptor(Object object) {
         itemPropertyDescriptors.add
             (createItemPropertyDescriptor
                 (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                  getResourceLocator(),
                  getString("_UI_IJavaTypeDescriptor2_arrayCount_feature"), //$NON-NLS-1$
                  getString("_UI_PropertyDescriptor_description", "_UI_IJavaTypeDescriptor2_arrayCount_feature", "_UI_IJavaTypeDescriptor2_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                  SymbolPackage.Literals.IJAVA_TYPE_DESCRIPTOR2__ARRAY_COUNT,
                  true,
                  false,
                  false,
                  ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
                  null,
                  null));
     }
 
 
     /**
 	 * This returns IJavaTypeDescriptor2.gif.
 	 * <!-- begin-user-doc -->
      * @param object 
      * @return the image adaption for object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/IJavaTypeDescriptor2")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
      * @param object 
      * @return the text representation of object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getText(Object object) {
 		String label = ((IJavaTypeDescriptor2)object).getTypeSignature();
 		return label == null || label.length() == 0 ?
 			getString("_UI_IJavaTypeDescriptor2_type") : //$NON-NLS-1$
 			getString("_UI_IJavaTypeDescriptor2_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren(Notification)} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged(Notification)}.
 	 * <!-- begin-user-doc -->
      * @param notification 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(IJavaTypeDescriptor2.class)) {
 			case SymbolPackage.IJAVA_TYPE_DESCRIPTOR2__TYPE:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing all of the children that can be created under this object.
 	 * <!-- begin-user-doc -->
      * @param newChildDescriptors 
      * @param object 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
      * @return the resource locator 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ResourceLocator getResourceLocator() {
 		return JSFCommonPlugin.INSTANCE;
 	}
 
 }
