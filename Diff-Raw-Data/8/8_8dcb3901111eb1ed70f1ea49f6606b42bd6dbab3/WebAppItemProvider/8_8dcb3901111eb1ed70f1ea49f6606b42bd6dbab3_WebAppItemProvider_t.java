 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.providers;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.CommonPackage;
 import org.eclipse.jst.j2ee.common.internal.provider.JNDIEnvRefsGroupItemProvider;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.web.plugin.WebPlugin;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.jsp.JspFactory;
 import org.eclipse.jst.j2ee.webapplication.ContextParam;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationPackage;
 
 
 /**
  * This is the item provider adpater for a {@link org.eclipse.jst.j2ee.internal.internal.webapplication.WebApp}object.
  */
 public class WebAppItemProvider extends JNDIEnvRefsGroupItemProvider implements IEditingDomainItemProvider, IItemLabelProvider, IItemPropertySource, IStructuredItemContentProvider, ITreeItemContentProvider {
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 */
 	public WebAppItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This creates a new child for {@link org.eclipse.jst.j2ee.internal.internal.webapplication.commandCreateChildCommand}.
 	 */
 	public Object createChild(Object object) {
 		// TODO: check that this is what you want.
 		ContextParam child = WebapplicationFactory.eINSTANCE.createContextParam();
 
 		// TODO: initialize child here...
 
 		return child;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getChildren(java.lang.Object)
 	 */
 	public Collection getChildren(Object object) {
 		WebApp webApp = (WebApp) object;
 		Collection myChildren = super.getChildren(object);
 		if (webApp.getVersionID() <= J2EEVersionConstants.WEB_2_3_ID) {
 			WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 			myChildren.addAll(serviceHelper.get13ServiceRefs(webApp));
 		}
 		return myChildren;
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren}and {@link AddCommand}and
 	 * {@link RemoveCommand}support in {@link #createCommand}.
 	 */
 
 
 	public Collection getChildrenReferences(Object object) {
 		if (childrenReferences == null) {
 			super.getChildrenReferences(object);
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_Contexts());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_ErrorPages());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_FileList());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_TagLibs());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_Constraints());
 			//childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_LoginConfig());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_MimeMappings());
 			//childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_SessionConfig());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_ServletMappings());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_Servlets());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_SecurityRoles());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_Filters());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_FilterMappings());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_Listeners());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_ContextParams());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_JspConfig());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_MessageDestinations());
 			childrenReferences.add(WebapplicationPackage.eINSTANCE.getWebApp_LocalEncodingMappingList());
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
 	 * This returns the image for {@link org.eclipse.jst.j2ee.internal.internal.webapplication.commandCreateChildCommand}.
 	 */
 	public Object getCreateChildImage(Object object) {
 		EObject refObject = (EObject) object;
 		return WebPlugin.getDefault().getImage(refObject.eClass().getName() + "CreateContextParam"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label for {@link org.eclipse.jst.j2ee.internal.internal.webapplication.commandCreateChildCommand}.
 	 */
 	public String getCreateChildText(Object object) {
 		return WebAppEditResourceHandler.getString("Create_ContextParam_UI_"); //$NON-NLS-1$ = "Create ContextParam"
 	}
 
 	/**
 	 * This returns the help text for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.webapplication.commandCreateChildCommand}.
 	 */
 	public String getCreateChildToolTipText(Object object) {
 		EObject refObject = (EObject) object;
 		return WebAppEditResourceHandler.getString("35concat_UI_", (new Object[]{refObject.eClass().getName()})); //$NON-NLS-1$ = "Create a child of type ContextParam for the selected {0}."
 	}
 
 	public Object getImage(Object object) {
 		String key = null;
		int versionID = 22;
		try {
			versionID = ((WebApp) object).getVersionID();
		} catch (IllegalStateException e) {
			//just bail and return 22 as default
		}
		switch (versionID) {
 			case J2EEVersionConstants.WEB_2_2_ID :
 				key = "webapp_22"; //$NON-NLS-1$ 
 				break;
 
 			case J2EEVersionConstants.WEB_2_3_ID :
 				key = "webapp_23"; //$NON-NLS-1$ 
 				break;
 
 			case J2EEVersionConstants.WEB_2_4_ID :
 			default :
 				key = "webapp_24"; //$NON-NLS-1$
 				break;
 		}
 		return WebPlugin.getDefault().getImage(key); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 */
 	public List getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			WebapplicationPackage pkg = WebapplicationPackage.eINSTANCE;
 
 			// This is for the distributable feature.
 			//
 			itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), WebAppEditResourceHandler.getString("Distributable_UI_"), //$NON-NLS-1$
 						//$NON-NLS-1$ = "Distributable"
 						WebAppEditResourceHandler.getString("The_distributable_property_UI_"), //$NON-NLS-1$ = "The distributable property"
 						pkg.getWebApp_Distributable()));
 
 
 			// This is for the fileList feature.
 			//
 			itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), WebAppEditResourceHandler.getString("FileList_UI_"), //$NON-NLS-1$ = "FileList"
 						WebAppEditResourceHandler.getString("The_fileList_property_UI_"), //$NON-NLS-1$ = "The fileList property"
 						pkg.getWebApp_FileList()));
 
 			/*
 			 * // ccc - for usability reasons, these descriptors are removed from the property
 			 * sheet. // This is for the loginConfig feature. // itemPropertyDescriptors.add (new
 			 * ItemPropertyDescriptor
 			 * (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(), "LoginConfig",
 			 * "The loginConfig property", pkg.getWebApp_LoginConfig()));
 			 *  // This is for the sessionConfig feature. // itemPropertyDescriptors.add (new
 			 * ItemPropertyDescriptor
 			 * (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 			 * "SessionConfig", "The sessionConfig property", pkg.getWebApp_SessionConfig()));
 			 */
 
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Distributable feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addDistributablePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("_UI_WebApp_distributable_feature"), //$NON-NLS-1$
 					getString("_UI_PropertyDescriptor_description", "_UI_WebApp_distributable_feature", "_UI_WebApp_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					WebapplicationPackage.eINSTANCE.getWebApp_Distributable(), true, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Version feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addVersionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("_UI_WebApp_version_feature"), //$NON-NLS-1$
 					getString("_UI_PropertyDescriptor_description", "_UI_WebApp_version_feature", "_UI_WebApp_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					WebapplicationPackage.eINSTANCE.getWebApp_Version(), true, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	public String getText(Object object) {
 		WebApp webApp = (WebApp) object;
 		String name = webApp.getDisplayName();
 		if (name == null)
 			name = WebAppEditResourceHandler.getString("<web_app>_UI_"); //$NON-NLS-1$ = "<web app>"
 		return name;
 	}
 
 	/**
 	 * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(WebApp.class)) {
 			case WebapplicationPackage.WEB_APP__DISTRIBUTABLE :
 			case WebapplicationPackage.WEB_APP__VERSION :
 			case WebapplicationPackage.WEB_APP__CONTEXTS :
 			case WebapplicationPackage.WEB_APP__ERROR_PAGES :
 			case WebapplicationPackage.WEB_APP__FILE_LIST :
 			case WebapplicationPackage.WEB_APP__TAG_LIBS :
 			case WebapplicationPackage.WEB_APP__CONSTRAINTS :
 			case WebapplicationPackage.WEB_APP__LOGIN_CONFIG :
 			case WebapplicationPackage.WEB_APP__MIME_MAPPINGS :
 			case WebapplicationPackage.WEB_APP__SESSION_CONFIG :
 			case WebapplicationPackage.WEB_APP__SERVLET_MAPPINGS :
 			case WebapplicationPackage.WEB_APP__SERVLETS :
 			case WebapplicationPackage.WEB_APP__SECURITY_ROLES :
 			case WebapplicationPackage.WEB_APP__FILTERS :
 			case WebapplicationPackage.WEB_APP__FILTER_MAPPINGS :
 			case WebapplicationPackage.WEB_APP__LISTENERS :
 			case WebapplicationPackage.WEB_APP__CONTEXT_PARAMS :
 			case WebapplicationPackage.WEB_APP__JSP_CONFIG :
 			case WebapplicationPackage.WEB_APP__MESSAGE_DESTINATIONS :
 			case WebapplicationPackage.WEB_APP__LOCAL_ENCODING_MAPPING_LIST : {
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
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_Contexts(), WebapplicationFactory.eINSTANCE.createContextParam()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_ErrorPages(), WebapplicationFactory.eINSTANCE.createErrorPage()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_ErrorPages(), WebapplicationFactory.eINSTANCE.createExceptionTypeErrorPage()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_ErrorPages(), WebapplicationFactory.eINSTANCE.createErrorCodeErrorPage()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_FileList(), WebapplicationFactory.eINSTANCE.createWelcomeFileList()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_TagLibs(), WebapplicationFactory.eINSTANCE.createTagLibRef()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_Constraints(), WebapplicationFactory.eINSTANCE.createSecurityConstraint()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_LoginConfig(), WebapplicationFactory.eINSTANCE.createLoginConfig()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_MimeMappings(), WebapplicationFactory.eINSTANCE.createMimeMapping()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_SessionConfig(), WebapplicationFactory.eINSTANCE.createSessionConfig()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_ServletMappings(), WebapplicationFactory.eINSTANCE.createServletMapping()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_Servlets(), WebapplicationFactory.eINSTANCE.createServlet()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_SecurityRoles(), CommonFactory.eINSTANCE.createSecurityRole()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_Filters(), WebapplicationFactory.eINSTANCE.createFilter()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_FilterMappings(), WebapplicationFactory.eINSTANCE.createFilterMapping()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_Listeners(), CommonFactory.eINSTANCE.createListener()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_ContextParams(), CommonFactory.eINSTANCE.createParamValue()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_JspConfig(), JspFactory.eINSTANCE.createJSPConfig()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_MessageDestinations(), CommonFactory.eINSTANCE.createMessageDestination()));
 
 		newChildDescriptors.add(createChildParameter(WebapplicationPackage.eINSTANCE.getWebApp_LocalEncodingMappingList(), WebapplicationFactory.eINSTANCE.createLocalEncodingMappingList()));
 	}
 
 	/**
 	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getCreateChildText(Object owner, Object feature, Object child, Collection selection) {
 		boolean qualify = feature == CommonPackage.eINSTANCE.getJNDIEnvRefsGroup_EjbRefs() || feature == CommonPackage.eINSTANCE.getJNDIEnvRefsGroup_EjbLocalRefs();
 		return getString(qualify ? "_UI_CreateChild_text2" : "_UI_CreateChild_text", //$NON-NLS-1$ //$NON-NLS-2$
 					new Object[]{getTypeText(child), getFeatureText(feature), getTypeText(owner)});
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ResourceLocator getResourceLocator() {
 		return J2EEPlugin.getDefault();
 	}
 }
