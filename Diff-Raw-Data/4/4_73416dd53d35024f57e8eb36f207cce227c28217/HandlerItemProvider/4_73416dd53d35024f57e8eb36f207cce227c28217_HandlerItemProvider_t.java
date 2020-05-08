 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.webservice.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.internal.provider.CompatibilityDescriptionGroupItemProvider;
 import org.eclipse.jst.j2ee.internal.webservice.plugin.WebServicePlugin;
 import org.eclipse.jst.j2ee.webservice.internal.wsclient.Webservice_clientPackage;
 import org.eclipse.jst.j2ee.webservice.wsclient.Handler;
 import org.eclipse.jst.j2ee.webservice.wscommon.WscommonFactory;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddFactory;
 
 /**
  * This is the item provider adpater for a {@link com.ibm.etools.wsclient.Handler}object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class HandlerItemProvider extends CompatibilityDescriptionGroupItemProvider implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public HandlerItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public List getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addHandlerNamePropertyDescriptor(object);
 			addSoapRolesPropertyDescriptor(object);
 			addPortNamesPropertyDescriptor(object);
 			addHandlerClassPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Handler Name feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 *  
 	 */
 	protected void addHandlerNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_Handler_handlerName_feature"), //$NON-NLS-1$
 					getString("%_UI_Handler_handlerName_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getHandler_HandlerName(), true, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Soap Roles feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 *  
 	 */
 	protected void addSoapRolesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_Handler_soapRoles_feature"), //$NON-NLS-1$
 					getString("%_UI_Handler_soapRoles_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getHandler_SoapRoles(), false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Port Names feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 *  
 	 */
 	protected void addPortNamesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_Handler_portNames_feature"), //$NON-NLS-1$
 					getString("%_UI_Handler_portNames_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getHandler_PortNames(), false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Handler Class feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 *  
 	 */
 	protected void addHandlerClassPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_Handler_handlerClass_feature"), //$NON-NLS-1$
 					getString("%_UI_Handler_handlerClass_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getHandler_HandlerClass(), false));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren}and
 	 * {@link org.eclipse.emf.edit.command.AddCommand}and
 	 * {@link org.eclipse.emf.edit.command.RemoveCommand}support in {@link #createCommand}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 *  
 	 */
 	public Collection getChildrenReferences(Object object) {
 		if (childrenReferences == null) {
 			super.getChildrenReferences(object);
			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getHandler_InitParams());
			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getHandler_SoapHeaders());
 			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getHandler_HandlerName());
 		}
 		return childrenReferences;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected EReference getChildReference(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildReference(object, child);
 	}
 
 
 	/**
 	 * This returns Handler.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public Object getImage(Object object) {
 		return getResourceLocator().getImage("icons/obj16/handler.gif"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	/*
 	 * public String getText(Object object) { String label = ((Handler)object).getHandlerName();
 	 * return label == null || label.length() == 0 ? getString("_UI_Handler_type") :
 	 * getString("_UI_Handler_type") + " " + label; }
 	 */
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @non-generated
 	 */
 	public String getText(Object object) {
 		String label = ((Handler) object).getHandlerName();
 		return label == null || label.length() == 0 ? getString("%_UI_Handler_type") : getString("%_UI_Handler_type") + ": " + label; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 
 	/**
 	 * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(Handler.class)) {
 			case Webservice_clientPackage.HANDLER__HANDLER_NAME :
 			case Webservice_clientPackage.HANDLER__SOAP_ROLES :
 			case Webservice_clientPackage.HANDLER__PORT_NAMES :
 			case Webservice_clientPackage.HANDLER__INIT_PARAMS :
 			case Webservice_clientPackage.HANDLER__SOAP_HEADERS : {
 				fireNotifyChanged(notification);
 				return;
 			}
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing all of the children that can be created under this object. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getHandler_InitParams(), CommonFactory.eINSTANCE.createParamValue()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getHandler_SoapHeaders(), CommonFactory.eINSTANCE.createQName()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getHandler_SoapHeaders(), WscommonFactory.eINSTANCE.createSOAPHeader()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getHandler_SoapHeaders(), WsddFactory.eINSTANCE.createWSDLPort()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 */
 	public ResourceLocator getResourceLocator() {
 		return WebServicePlugin.getInstance();
 	}
 }
