 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.model.translator.webservices;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jst.j2ee.common.CommonPackage;
 import org.eclipse.jst.j2ee.internal.model.translator.common.CommonTranslators;
 import org.eclipse.jst.j2ee.internal.model.translator.common.JavaClassTranslator;
 import org.eclipse.jst.j2ee.internal.xml.WebServicesDeploymentDescriptorXmlMapperI;
 import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientPackage;
 import org.eclipse.wst.common.internal.emf.resource.GenericTranslator;
 import org.eclipse.wst.common.internal.emf.resource.IDTranslator;
 import org.eclipse.wst.common.internal.emf.resource.RootTranslator;
 import org.eclipse.wst.common.internal.emf.resource.Translator;
 
 public class WebServicesTranslator extends RootTranslator implements WebServicesDeploymentDescriptorXmlMapperI{
 	public static WebServicesTranslator INSTANCE = new WebServicesTranslator();
 	public static final String WEBSERVICESCLIENT = WEB_SERVICES_CLIENT; 
 	private static CommonPackage COMMON_PKG = CommonPackage.eINSTANCE;
 
 	private static  Webservice_clientPackage WEBSERVICECLIENT_PKG = Webservice_clientPackage.eINSTANCE;
 	
 	private static Translator[] children13;
     
 	public WebServicesTranslator() {
 		super(WEBSERVICESCLIENT, Webservice_clientPackage.eINSTANCE.getWebServicesClient());
 	}
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.Translator#getChildren(java.lang.Object, int)
 	 */
 	public Translator[] getChildren(Object o, int versionID) {
 		if (children13 == null)
 			children13 = create13Children();
 		return children13;
 	}
 	
 	protected Translator[] create13Children() {
 		return new Translator[] {
 			IDTranslator.INSTANCE,
			createServiceRefGroupTranslator13(),
 			createComponentScopedRefs13()
 		};
 	}
 	/**
 	 * This model was model from Websphere Webservices
 	 * @param reference
 	 * @return
 	 */
	public static Translator createServiceRefGroupTranslator13() {
		GenericTranslator result = new GenericTranslator(SERVICE_REF, WEBSERVICECLIENT_PKG.getWebServicesClient_ServiceRefs());
 		result.setChildren( new Translator[] {
 			IDTranslator.INSTANCE,
 			new Translator(DESCRIPTION, COMMON_PKG.getCompatibilityDescriptionGroup_Description()),
 			new Translator(DISPLAY_NAME, COMMON_PKG.getCompatibilityDescriptionGroup_DisplayName()),
 			new Translator(SMALL_ICON, COMMON_PKG.getCompatibilityDescriptionGroup_SmallIcon()),
 			new Translator(LARGE_ICON, COMMON_PKG.getCompatibilityDescriptionGroup_LargeIcon()),
 			new Translator(SERVICE_REF_NAME, WEBSERVICECLIENT_PKG.getServiceRef_ServiceRefName()),
 			new JavaClassTranslator(SERVICE_INTERFACE, WEBSERVICECLIENT_PKG.getServiceRef_ServiceInterface()),
 			new Translator(WSDL_FILE, WEBSERVICECLIENT_PKG.getServiceRef_WsdlFile()),
 			new Translator(JAXRPC_MAPPING_FILE, WEBSERVICECLIENT_PKG.getServiceRef_JaxrpcMappingFile()),
 			createQNameTranslator10(SERVICE_QNAME, WEBSERVICECLIENT_PKG.getServiceRef_ServiceQname()),
 			CommonTranslators.createPortComponentRefTranslator(),
 			createServiceRefHandlerTranslator13(),
 		});
 		
 		
 		return result;
 	}
 	/**
 	 * This model was model from Websphere Webservices
 	 * @return
 	 */
 	public static Translator createServiceRefHandlerTranslator13() {
 		GenericTranslator result = new GenericTranslator(HANDLER, WEBSERVICECLIENT_PKG.getServiceRef_Handlers());
 		result.setChildren(new Translator[]{
 			IDTranslator.INSTANCE,
 			new Translator(DESCRIPTION, COMMON_PKG.getCompatibilityDescriptionGroup_Description()),
 			new Translator(DISPLAY_NAME, COMMON_PKG.getCompatibilityDescriptionGroup_DisplayName()),
 			new Translator(SMALL_ICON, COMMON_PKG.getCompatibilityDescriptionGroup_SmallIcon()),
 			new Translator(LARGE_ICON, COMMON_PKG.getCompatibilityDescriptionGroup_LargeIcon()),
 			new Translator(HANDLER_NAME, WEBSERVICECLIENT_PKG.getHandler_HandlerName()),
 			new JavaClassTranslator(HANDLER_CLASS, WEBSERVICECLIENT_PKG.getHandler_HandlerClass()),
 			createParamValueType13(HANDLER_INIT_PARAM, WEBSERVICECLIENT_PKG.getHandler_InitParams()),
 			createQNameTranslator10(SOAP_HEADER, WEBSERVICECLIENT_PKG.getHandler_SoapHeaders()),
 			new Translator(SOAP_ROLE, WEBSERVICECLIENT_PKG.getHandler_SoapRoles()),
 			new Translator(PORT_NAME, WEBSERVICECLIENT_PKG.getHandler_PortNames())
 		});
 		
 		return result;
 	}
     /**
      * @return
      */
     private Translator createComponentScopedRefs13() {
 		GenericTranslator result = new GenericTranslator(COMP_SCOPED_REFS, WEBSERVICECLIENT_PKG.getWebServicesClient_ComponentScopedRefs());
 		result.setChildren(new Translator[] {
 			IDTranslator.INSTANCE,
 			new Translator(COMP_COMPONENT_NAME, WEBSERVICECLIENT_PKG.getComponentScopedRefs_ComponentName()), 
 			createServiceRefGroupTranslator13(WEBSERVICECLIENT_PKG.getComponentScopedRefs_ServiceRefs())
 		});
 		return result;
     }
     
	/**
	 * This model was model from Websphere Webservices
	 * @param reference
	 * @return
	 */
	public static Translator createServiceRefGroupTranslator13(EStructuralFeature feature) {
		GenericTranslator result = new GenericTranslator(SERVICE_REF, feature);
		result.setChildren( new Translator[] {
			IDTranslator.INSTANCE,
			CommonTranslators.DESCRIPTIONS_TRANSLATOR,
			CommonTranslators.DISPLAYNAMES_TRANSLATOR,
			CommonTranslators.ICONS_TRANSLATOR,
			new Translator(SERVICE_REF_NAME, WEBSERVICECLIENT_PKG.getServiceRef_ServiceRefName()),
			new JavaClassTranslator(SERVICE_INTERFACE, WEBSERVICECLIENT_PKG.getServiceRef_ServiceInterface()),
			new Translator(WSDL_FILE, WEBSERVICECLIENT_PKG.getServiceRef_WsdlFile()),
			new Translator(JAXRPC_MAPPING_FILE, WEBSERVICECLIENT_PKG.getServiceRef_JaxrpcMappingFile()),
			createQNameTranslator10(SERVICE_QNAME, WEBSERVICECLIENT_PKG.getServiceRef_ServiceQname()),
			CommonTranslators.createPortComponentRefTranslator(),
			CommonTranslators.createServiceRefHandlerTranslator(),
		});
		
		
		return result;
	}    
    
 	public static Translator createQNameTranslator10(String domName, EStructuralFeature feature) {		
 		GenericTranslator result = new GenericTranslator(domName, feature);
 		result.setChildren(new Translator[] {
 			IDTranslator.INSTANCE,
 			new Translator(NAMESPACEURI, COMMON_PKG.getQName_NamespaceURI()),
 			new Translator(LOCALPART, COMMON_PKG.getQName_LocalPart())
 		});
 		return result;	
 	}
 	
 	public static Translator createParamValueType13(String domName, EStructuralFeature feature){
 		GenericTranslator result = new GenericTranslator(domName, feature);
 		result.setChildren(new Translator[] {
 			IDTranslator.INSTANCE,
 			new Translator(PARAM_NAME, COMMON_PKG.getParamValue_Name()),
 			new Translator(PARAM_VALUE, COMMON_PKG.getParamValue_Value()),
 			new Translator(DESCRIPTION, COMMON_PKG.getParamValue_Description())
 		});
 		return result;
 	}
 	
 }
