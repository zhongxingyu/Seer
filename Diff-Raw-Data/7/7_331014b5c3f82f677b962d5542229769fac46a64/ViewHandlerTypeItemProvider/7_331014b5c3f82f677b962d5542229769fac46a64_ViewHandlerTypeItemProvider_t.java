 /***************************************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *   IBM Corporation - initial API and implementation
  *   Oracle Corporation - revision
  **************************************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.edit.provider;
 
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
 import org.eclipse.emf.edit.provider.ITableItemLabelProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.jst.jsf.facesconfig.FacesConfigPlugin;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.ViewHandlerType;
 
 /**
  * This is the item provider adapter for a
  * {@link org.eclipse.jst.jsf.facesconfig.emf.ViewHandlerType} object. 
  * 
  * <!-- begin-user-doc --> 
  * @extends ITableItemLabelProvider
  * <!-- end-user-doc -->
  * 
  * @generated
  */
 public class ViewHandlerTypeItemProvider extends ItemProviderAdapter implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
 	public static final String copyright = "Copyright (c) 2005, 2006 IBM Corporation and others";
 
 	/**
 	 * This constructs an instance from a factory and a notifier. 
 	 * 
 	 * <!-- begin-user-doc --> 
 	 * @param adapterFactory
 	 * <!-- end-user-doc -->
 	 *  
 	 * 
 	 * @generated
 	 */
 	public ViewHandlerTypeItemProvider(AdapterFactory adapterFactory) {
         super(adapterFactory);
     }
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public List getPropertyDescriptors(Object object) {
         if (itemPropertyDescriptors == null) {
             super.getPropertyDescriptors(object);
 
             addTextContentPropertyDescriptor(object);
             addIdPropertyDescriptor(object);
         }
         return itemPropertyDescriptors;
     }
 
 	/**
 	 * This adds a property descriptor for the Text Content feature. 
 	 * 
 	 * <!-- begin-user-doc --> 
 	 * @param object 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addTextContentPropertyDescriptor(Object object) {
         itemPropertyDescriptors.add
             (createItemPropertyDescriptor
                 (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                  getResourceLocator(),
                  getString("_UI_ViewHandlerType_textContent_feature"),
                  getString("_UI_PropertyDescriptor_description", "_UI_ViewHandlerType_textContent_feature", "_UI_ViewHandlerType_type"),
                  FacesConfigPackage.Literals.VIEW_HANDLER_TYPE__TEXT_CONTENT,
                  true,
                  false,
                  false,
                  ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                  null,
                  null));
     }
 
     /**
      * This adds a property descriptor for the Id feature.
      * <!-- begin-user-doc --> 
      * @param object 
      * <!-- end-user-doc -->
      * @generated
      */
 	protected void addIdPropertyDescriptor(Object object) {
         itemPropertyDescriptors.add
             (createItemPropertyDescriptor
                 (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                  getResourceLocator(),
                  getString("_UI_ViewHandlerType_id_feature"),
                  getString("_UI_PropertyDescriptor_description", "_UI_ViewHandlerType_id_feature", "_UI_ViewHandlerType_type"),
                  FacesConfigPackage.Literals.VIEW_HANDLER_TYPE__ID,
                  true,
                  false,
                  false,
                  ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                  null,
                  null));
     }
 
     /**
      * This returns ViewHandlerType.gif.
      * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
      * @generated
      */
 	public Object getImage(Object object) {
         return overlayImage(object, getResourceLocator().getImage("full/obj16/ViewHandlerType"));
     }
 
     /**
      * This returns the label text for the adapted class.
      * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
      * @generated
      */
 	public String getText(Object object) {
        String label = ((ViewHandlerType)object).getTextContent();
        return (label == null || label.trim().length() == 0) ?
             getString("_UI_ViewHandlerType_type") :
            label;
     }
 
     /**
      * This handles model notifications by calling {@link #updateChildren(Notification)} to update any cached
      * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged(Notification)}.
      * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
      * @generated
      */
 	public void notifyChanged(Notification notification) {
         updateChildren(notification);
 
         switch (notification.getFeatureID(ViewHandlerType.class)) {
             case FacesConfigPackage.VIEW_HANDLER_TYPE__TEXT_CONTENT:
             case FacesConfigPackage.VIEW_HANDLER_TYPE__ID:
                 fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                 return;
         }
         super.notifyChanged(notification);
     }
 
     /**
      * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
      * describing all of the children that can be created under this object.
      * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
      * @generated
      */
 	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
         super.collectNewChildDescriptors(newChildDescriptors, object);
     }
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ResourceLocator getResourceLocator() {
         return FacesConfigPlugin.INSTANCE;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.edit.provider.ITableItemLabelProvider#getColumnImage(java.lang.Object, int)
 	 */
 	public Object getColumnImage(Object object, int columnIndex) {
 		if(columnIndex ==0)
 			return getImage(object);
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.edit.provider.ITableItemLabelProvider#getColumnText(java.lang.Object, int)
 	 */
 	public String getColumnText(Object object, int columnIndex) {
 		switch (columnIndex) {
 
 		case 0:
 			return getText(object);
 		case 1:
 			return getString("_UI_ViewHandlerType_type");
 		}
 
 		return null;
 	}
 
 }
