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
 package org.eclipse.jst.jsf.facesconfig.emf.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.jst.jsf.facesconfig.emf.*;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class FacesConfigFactoryImpl extends EFactoryImpl implements FacesConfigFactory {
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2005, 2006 IBM Corporation and others"; //$NON-NLS-1$
 
     /**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
      * @return the factory instance 
      * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
     public static FacesConfigFactory init() {
 		try {
 			FacesConfigFactory theFacesConfigFactory = (FacesConfigFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/webtools/jsf/schema/facesconfig.xsd"); //$NON-NLS-1$ 
 			if (theFacesConfigFactory != null) {
 				return theFacesConfigFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new FacesConfigFactoryImpl();
 	}
 
     /**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesConfigFactoryImpl() {
 		super();
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case FacesConfigPackage.ABSOLUTE_ORDERING_TYPE: return createAbsoluteOrderingType();
 			case FacesConfigPackage.ACTION_LISTENER_TYPE: return createActionListenerType();
 			case FacesConfigPackage.APPLICATION_FACTORY_TYPE: return createApplicationFactoryType();
 			case FacesConfigPackage.APPLICATION_TYPE: return createApplicationType();
 			case FacesConfigPackage.APPLICATION_EXTENSION_TYPE: return createApplicationExtensionType();
 			case FacesConfigPackage.ATTRIBUTE_CLASS_TYPE: return createAttributeClassType();
 			case FacesConfigPackage.ATTRIBUTE_EXTENSION_TYPE: return createAttributeExtensionType();
 			case FacesConfigPackage.ATTRIBUTE_NAME_TYPE: return createAttributeNameType();
 			case FacesConfigPackage.ATTRIBUTE_TYPE: return createAttributeType();
 			case FacesConfigPackage.BEHAVIOR_CLASS_TYPE: return createBehaviorClassType();
 			case FacesConfigPackage.BEHAVIOR_ID_TYPE: return createBehaviorIdType();
 			case FacesConfigPackage.BEHAVIOR_TYPE: return createBehaviorType();
 			case FacesConfigPackage.BEHAVIOR_EXTENSION_TYPE: return createBehaviorExtensionType();
 			case FacesConfigPackage.CLIENT_BEHAVIOR_RENDERER_CLASS_TYPE: return createClientBehaviorRendererClassType();
 			case FacesConfigPackage.CLIENT_BEHAVIOR_RENDERER_TYPE: return createClientBehaviorRendererType();
 			case FacesConfigPackage.CLIENT_BEHAVIOR_RENDERER_TYPE_TYPE: return createClientBehaviorRendererTypeType();
 			case FacesConfigPackage.COMPONENT_CLASS_TYPE: return createComponentClassType();
 			case FacesConfigPackage.COMPONENT_EXTENSION_TYPE: return createComponentExtensionType();
 			case FacesConfigPackage.COMPONENT_FAMILY_TYPE: return createComponentFamilyType();
 			case FacesConfigPackage.COMPONENT_TYPE: return createComponentType();
 			case FacesConfigPackage.COMPONENT_TYPE_TYPE: return createComponentTypeType();
 			case FacesConfigPackage.CONVERTER_CLASS_TYPE: return createConverterClassType();
 			case FacesConfigPackage.CONVERTER_FOR_CLASS_TYPE: return createConverterForClassType();
 			case FacesConfigPackage.CONVERTER_ID_TYPE: return createConverterIdType();
 			case FacesConfigPackage.CONVERTER_TYPE: return createConverterType();
 			case FacesConfigPackage.CONVERTER_EXTENSION_TYPE: return createConverterExtensionType();
 			case FacesConfigPackage.DEFAULT_LOCALE_TYPE: return createDefaultLocaleType();
 			case FacesConfigPackage.DEFAULT_RENDER_KIT_ID_TYPE: return createDefaultRenderKitIdType();
 			case FacesConfigPackage.DEFAULT_VALIDATORS_TYPE: return createDefaultValidatorsType();
 			case FacesConfigPackage.DEFAULT_VALUE_TYPE: return createDefaultValueType();
 			case FacesConfigPackage.DESCRIPTION_TYPE: return createDescriptionType();
 			case FacesConfigPackage.DISPLAY_NAME_TYPE: return createDisplayNameType();
 			case FacesConfigPackage.DOCUMENT_ROOT: return createDocumentRoot();
 			case FacesConfigPackage.DYNAMIC_ATTRIBUTE: return createDynamicAttribute();
 			case FacesConfigPackage.DYNAMIC_ELEMENT: return createDynamicElement();
 			case FacesConfigPackage.EL_RESOLVER_TYPE: return createELResolverType();
 			case FacesConfigPackage.EXCEPTION_HANDLER_FACTORY_TYPE: return createExceptionHandlerFactoryType();
 			case FacesConfigPackage.EXTERNAL_CONTEXT_FACTORY_TYPE: return createExternalContextFactoryType();
 			case FacesConfigPackage.FACES_CONFIG_TYPE: return createFacesConfigType();
 			case FacesConfigPackage.FACES_CONFIG_EXTENSION_TYPE: return createFacesConfigExtensionType();
 			case FacesConfigPackage.FACES_CONTEXT_FACTORY_TYPE: return createFacesContextFactoryType();
 			case FacesConfigPackage.FACET_EXTENSION_TYPE: return createFacetExtensionType();
 			case FacesConfigPackage.FACET_NAME_TYPE: return createFacetNameType();
 			case FacesConfigPackage.FACET_TYPE: return createFacetType();
 			case FacesConfigPackage.FACTORY_TYPE: return createFactoryType();
 			case FacesConfigPackage.FACTORY_EXTENSION_TYPE: return createFactoryExtensionType();
 			case FacesConfigPackage.FROM_ACTION_TYPE: return createFromActionType();
 			case FacesConfigPackage.FROM_OUTCOME_TYPE: return createFromOutcomeType();
 			case FacesConfigPackage.FROM_VIEW_ID_TYPE: return createFromViewIdType();
 			case FacesConfigPackage.ICON_TYPE: return createIconType();
 			case FacesConfigPackage.IF_TYPE: return createIfType();
 			case FacesConfigPackage.KEY_CLASS_TYPE: return createKeyClassType();
 			case FacesConfigPackage.KEY_TYPE: return createKeyType();
 			case FacesConfigPackage.LARGE_ICON_TYPE: return createLargeIconType();
 			case FacesConfigPackage.LIFECYCLE_FACTORY_TYPE: return createLifecycleFactoryType();
 			case FacesConfigPackage.LIFECYCLE_TYPE: return createLifecycleType();
 			case FacesConfigPackage.LIFECYCLE_EXTENSION_TYPE: return createLifecycleExtensionType();
 			case FacesConfigPackage.LIST_ENTRIES_TYPE: return createListEntriesType();
 			case FacesConfigPackage.LOCALE_CONFIG_TYPE: return createLocaleConfigType();
 			case FacesConfigPackage.MANAGED_BEAN_CLASS_TYPE: return createManagedBeanClassType();
 			case FacesConfigPackage.MANAGED_BEAN_NAME_TYPE: return createManagedBeanNameType();
 			case FacesConfigPackage.MANAGED_BEAN_SCOPE_TYPE: return createManagedBeanScopeType();
 			case FacesConfigPackage.MANAGED_BEAN_TYPE: return createManagedBeanType();
 			case FacesConfigPackage.MANAGED_BEAN_EXTENSION_TYPE: return createManagedBeanExtensionType();
 			case FacesConfigPackage.MANAGED_PROPERTY_TYPE: return createManagedPropertyType();
 			case FacesConfigPackage.MAP_ENTRIES_TYPE: return createMapEntriesType();
 			case FacesConfigPackage.MAP_ENTRY_TYPE: return createMapEntryType();
 			case FacesConfigPackage.MESSAGE_BUNDLE_TYPE: return createMessageBundleType();
 			case FacesConfigPackage.NAME_TYPE: return createNameType();
 			case FacesConfigPackage.NAVIGATION_CASE_TYPE: return createNavigationCaseType();
 			case FacesConfigPackage.NAVIGATION_HANDLER_TYPE: return createNavigationHandlerType();
 			case FacesConfigPackage.NAVIGATION_RULE_TYPE: return createNavigationRuleType();
 			case FacesConfigPackage.NAVIGATION_RULE_EXTENSION_TYPE: return createNavigationRuleExtensionType();
 			case FacesConfigPackage.NULL_VALUE_TYPE: return createNullValueType();
 			case FacesConfigPackage.ORDERING_TYPE: return createOrderingType();
 			case FacesConfigPackage.ORDERING_ORDERING_TYPE: return createOrderingOrderingType();
 			case FacesConfigPackage.ORDERING_OTHERS_TYPE: return createOrderingOthersType();
 			case FacesConfigPackage.PARTIAL_VIEW_CONTEXT_FACTORY_TYPE: return createPartialViewContextFactoryType();
 			case FacesConfigPackage.PHASE_LISTENER_TYPE: return createPhaseListenerType();
 			case FacesConfigPackage.PROPERTY_CLASS_TYPE: return createPropertyClassType();
 			case FacesConfigPackage.PROPERTY_EXTENSION_TYPE: return createPropertyExtensionType();
 			case FacesConfigPackage.PROPERTY_NAME_TYPE: return createPropertyNameType();
 			case FacesConfigPackage.PROPERTY_RESOLVER_TYPE: return createPropertyResolverType();
 			case FacesConfigPackage.PROPERTY_TYPE: return createPropertyType();
 			case FacesConfigPackage.REDIRECT_TYPE: return createRedirectType();
 			case FacesConfigPackage.REDIRECT_VIEW_PARAM_TYPE: return createRedirectViewParamType();
 			case FacesConfigPackage.REFERENCED_BEAN_CLASS_TYPE: return createReferencedBeanClassType();
 			case FacesConfigPackage.REFERENCED_BEAN_NAME_TYPE: return createReferencedBeanNameType();
 			case FacesConfigPackage.REFERENCED_BEAN_TYPE: return createReferencedBeanType();
 			case FacesConfigPackage.RENDERER_CLASS_TYPE: return createRendererClassType();
 			case FacesConfigPackage.RENDERER_EXTENSION_TYPE: return createRendererExtensionType();
 			case FacesConfigPackage.RENDERER_TYPE: return createRendererType();
 			case FacesConfigPackage.RENDERER_TYPE_TYPE: return createRendererTypeType();
 			case FacesConfigPackage.RENDER_KIT_CLASS_TYPE: return createRenderKitClassType();
 			case FacesConfigPackage.RENDER_KIT_FACTORY_TYPE: return createRenderKitFactoryType();
 			case FacesConfigPackage.RENDER_KIT_ID_TYPE: return createRenderKitIdType();
 			case FacesConfigPackage.RENDER_KIT_TYPE: return createRenderKitType();
 			case FacesConfigPackage.RENDER_KIT_EXTENSION_TYPE: return createRenderKitExtensionType();
 			case FacesConfigPackage.RESOURCE_HANDLER_TYPE: return createResourceHandlerType();
 			case FacesConfigPackage.SMALL_ICON_TYPE: return createSmallIconType();
 			case FacesConfigPackage.SOURCE_CLASS_TYPE: return createSourceClassType();
 			case FacesConfigPackage.STATE_MANAGER_TYPE: return createStateManagerType();
 			case FacesConfigPackage.SUGGESTED_VALUE_TYPE: return createSuggestedValueType();
 			case FacesConfigPackage.SUPPORTED_LOCALE_TYPE: return createSupportedLocaleType();
 			case FacesConfigPackage.SYSTEM_EVENT_CLASS_TYPE: return createSystemEventClassType();
 			case FacesConfigPackage.SYSTEM_EVENT_LISTENER_CLASS_TYPE: return createSystemEventListenerClassType();
 			case FacesConfigPackage.SYSTEM_EVENT_LISTENER_TYPE: return createSystemEventListenerType();
 			case FacesConfigPackage.TAG_HANDLER_DELEGATE_FACTORY_TYPE: return createTagHandlerDelegateFactoryType();
 			case FacesConfigPackage.TO_VIEW_ID_TYPE: return createToViewIdType();
 			case FacesConfigPackage.VALIDATOR_CLASS_TYPE: return createValidatorClassType();
 			case FacesConfigPackage.VALIDATOR_ID_TYPE: return createValidatorIdType();
 			case FacesConfigPackage.VALIDATOR_TYPE: return createValidatorType();
 			case FacesConfigPackage.VALIDATOR_EXTENSION_TYPE: return createValidatorExtensionType();
 			case FacesConfigPackage.VALUE_CLASS_TYPE: return createValueClassType();
 			case FacesConfigPackage.VALUE_TYPE: return createValueType();
 			case FacesConfigPackage.VARIABLE_RESOLVER_TYPE: return createVariableResolverType();
 			case FacesConfigPackage.VIEW_HANDLER_TYPE: return createViewHandlerType();
 			case FacesConfigPackage.RESOURCE_BUNDLE_TYPE: return createResourceBundleType();
 			case FacesConfigPackage.BASE_NAME_TYPE: return createBaseNameType();
 			case FacesConfigPackage.VAR_TYPE: return createVarType();
 			case FacesConfigPackage.VIEW_DECLARATION_LANGUAGE_FACTORY_TYPE: return createViewDeclarationLanguageFactoryType();
 			case FacesConfigPackage.VISIT_CONTEXT_FACTORY_TYPE: return createVisitContextFactoryType();
 			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AbsoluteOrderingType createAbsoluteOrderingType() {
 		AbsoluteOrderingTypeImpl absoluteOrderingType = new AbsoluteOrderingTypeImpl();
 		return absoluteOrderingType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActionListenerType createActionListenerType() {
 		ActionListenerTypeImpl actionListenerType = new ActionListenerTypeImpl();
 		return actionListenerType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ApplicationFactoryType createApplicationFactoryType() {
 		ApplicationFactoryTypeImpl applicationFactoryType = new ApplicationFactoryTypeImpl();
 		return applicationFactoryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ApplicationType createApplicationType() {
 		ApplicationTypeImpl applicationType = new ApplicationTypeImpl();
 		return applicationType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeClassType createAttributeClassType() {
 		AttributeClassTypeImpl attributeClassType = new AttributeClassTypeImpl();
 		return attributeClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeExtensionType createAttributeExtensionType() {
 		AttributeExtensionTypeImpl attributeExtensionType = new AttributeExtensionTypeImpl();
 		return attributeExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeNameType createAttributeNameType() {
 		AttributeNameTypeImpl attributeNameType = new AttributeNameTypeImpl();
 		return attributeNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeType createAttributeType() {
 		AttributeTypeImpl attributeType = new AttributeTypeImpl();
 		return attributeType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorClassType createBehaviorClassType() {
 		BehaviorClassTypeImpl behaviorClassType = new BehaviorClassTypeImpl();
 		return behaviorClassType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorIdType createBehaviorIdType() {
 		BehaviorIdTypeImpl behaviorIdType = new BehaviorIdTypeImpl();
 		return behaviorIdType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorType createBehaviorType() {
 		BehaviorTypeImpl behaviorType = new BehaviorTypeImpl();
 		return behaviorType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorExtensionType createBehaviorExtensionType() {
 		BehaviorExtensionTypeImpl behaviorExtensionType = new BehaviorExtensionTypeImpl();
 		return behaviorExtensionType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClientBehaviorRendererClassType createClientBehaviorRendererClassType() {
 		ClientBehaviorRendererClassTypeImpl clientBehaviorRendererClassType = new ClientBehaviorRendererClassTypeImpl();
 		return clientBehaviorRendererClassType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClientBehaviorRendererType createClientBehaviorRendererType() {
 		ClientBehaviorRendererTypeImpl clientBehaviorRendererType = new ClientBehaviorRendererTypeImpl();
 		return clientBehaviorRendererType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClientBehaviorRendererTypeType createClientBehaviorRendererTypeType() {
 		ClientBehaviorRendererTypeTypeImpl clientBehaviorRendererTypeType = new ClientBehaviorRendererTypeTypeImpl();
 		return clientBehaviorRendererTypeType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentClassType createComponentClassType() {
 		ComponentClassTypeImpl componentClassType = new ComponentClassTypeImpl();
 		return componentClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentExtensionType createComponentExtensionType() {
 		ComponentExtensionTypeImpl componentExtensionType = new ComponentExtensionTypeImpl();
 		return componentExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentFamilyType createComponentFamilyType() {
 		ComponentFamilyTypeImpl componentFamilyType = new ComponentFamilyTypeImpl();
 		return componentFamilyType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentType createComponentType() {
 		ComponentTypeImpl componentType = new ComponentTypeImpl();
 		return componentType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentTypeType createComponentTypeType() {
 		ComponentTypeTypeImpl componentTypeType = new ComponentTypeTypeImpl();
 		return componentTypeType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterClassType createConverterClassType() {
 		ConverterClassTypeImpl converterClassType = new ConverterClassTypeImpl();
 		return converterClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterForClassType createConverterForClassType() {
 		ConverterForClassTypeImpl converterForClassType = new ConverterForClassTypeImpl();
 		return converterForClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterIdType createConverterIdType() {
 		ConverterIdTypeImpl converterIdType = new ConverterIdTypeImpl();
 		return converterIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterType createConverterType() {
 		ConverterTypeImpl converterType = new ConverterTypeImpl();
 		return converterType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultLocaleType createDefaultLocaleType() {
 		DefaultLocaleTypeImpl defaultLocaleType = new DefaultLocaleTypeImpl();
 		return defaultLocaleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultRenderKitIdType createDefaultRenderKitIdType() {
 		DefaultRenderKitIdTypeImpl defaultRenderKitIdType = new DefaultRenderKitIdTypeImpl();
 		return defaultRenderKitIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultValidatorsType createDefaultValidatorsType() {
 		DefaultValidatorsTypeImpl defaultValidatorsType = new DefaultValidatorsTypeImpl();
 		return defaultValidatorsType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultValueType createDefaultValueType() {
 		DefaultValueTypeImpl defaultValueType = new DefaultValueTypeImpl();
 		return defaultValueType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DescriptionType createDescriptionType() {
 		DescriptionTypeImpl descriptionType = new DescriptionTypeImpl();
 		return descriptionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DisplayNameType createDisplayNameType() {
 		DisplayNameTypeImpl displayNameType = new DisplayNameTypeImpl();
 		return displayNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DocumentRoot createDocumentRoot() {
 		DocumentRootImpl documentRoot = new DocumentRootImpl();
 		return documentRoot;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public DynamicAttribute createDynamicAttribute() {
 		DynamicAttributeImpl dynamicAttribute = new DynamicAttributeImpl();
 		return dynamicAttribute;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public DynamicElement createDynamicElement() {
 		DynamicElementImpl dynamicElement = new DynamicElementImpl();
 		return dynamicElement;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ELResolverType createELResolverType() {
 		ELResolverTypeImpl elResolverType = new ELResolverTypeImpl();
 		return elResolverType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExceptionHandlerFactoryType createExceptionHandlerFactoryType() {
 		ExceptionHandlerFactoryTypeImpl exceptionHandlerFactoryType = new ExceptionHandlerFactoryTypeImpl();
 		return exceptionHandlerFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExternalContextFactoryType createExternalContextFactoryType() {
 		ExternalContextFactoryTypeImpl externalContextFactoryType = new ExternalContextFactoryTypeImpl();
 		return externalContextFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesConfigType createFacesConfigType() {
 		FacesConfigTypeImpl facesConfigType = new FacesConfigTypeImpl();
 		return facesConfigType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesContextFactoryType createFacesContextFactoryType() {
 		FacesContextFactoryTypeImpl facesContextFactoryType = new FacesContextFactoryTypeImpl();
 		return facesContextFactoryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetExtensionType createFacetExtensionType() {
 		FacetExtensionTypeImpl facetExtensionType = new FacetExtensionTypeImpl();
 		return facetExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetNameType createFacetNameType() {
 		FacetNameTypeImpl facetNameType = new FacetNameTypeImpl();
 		return facetNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetType createFacetType() {
 		FacetTypeImpl facetType = new FacetTypeImpl();
 		return facetType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FactoryType createFactoryType() {
 		FactoryTypeImpl factoryType = new FactoryTypeImpl();
 		return factoryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromActionType createFromActionType() {
 		FromActionTypeImpl fromActionType = new FromActionTypeImpl();
 		return fromActionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromOutcomeType createFromOutcomeType() {
 		FromOutcomeTypeImpl fromOutcomeType = new FromOutcomeTypeImpl();
 		return fromOutcomeType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromViewIdType createFromViewIdType() {
 		FromViewIdTypeImpl fromViewIdType = new FromViewIdTypeImpl();
 		return fromViewIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IconType createIconType() {
 		IconTypeImpl iconType = new IconTypeImpl();
 		return iconType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IfType createIfType() {
 		IfTypeImpl ifType = new IfTypeImpl();
 		return ifType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public KeyClassType createKeyClassType() {
 		KeyClassTypeImpl keyClassType = new KeyClassTypeImpl();
 		return keyClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public KeyType createKeyType() {
 		KeyTypeImpl keyType = new KeyTypeImpl();
 		return keyType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LargeIconType createLargeIconType() {
 		LargeIconTypeImpl largeIconType = new LargeIconTypeImpl();
 		return largeIconType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LifecycleFactoryType createLifecycleFactoryType() {
 		LifecycleFactoryTypeImpl lifecycleFactoryType = new LifecycleFactoryTypeImpl();
 		return lifecycleFactoryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LifecycleType createLifecycleType() {
 		LifecycleTypeImpl lifecycleType = new LifecycleTypeImpl();
 		return lifecycleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ListEntriesType createListEntriesType() {
 		ListEntriesTypeImpl listEntriesType = new ListEntriesTypeImpl();
 		return listEntriesType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LocaleConfigType createLocaleConfigType() {
 		LocaleConfigTypeImpl localeConfigType = new LocaleConfigTypeImpl();
 		return localeConfigType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanClassType createManagedBeanClassType() {
 		ManagedBeanClassTypeImpl managedBeanClassType = new ManagedBeanClassTypeImpl();
 		return managedBeanClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanNameType createManagedBeanNameType() {
 		ManagedBeanNameTypeImpl managedBeanNameType = new ManagedBeanNameTypeImpl();
 		return managedBeanNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanScopeType createManagedBeanScopeType() {
 		ManagedBeanScopeTypeImpl managedBeanScopeType = new ManagedBeanScopeTypeImpl();
 		return managedBeanScopeType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanType createManagedBeanType() {
 		ManagedBeanTypeImpl managedBeanType = new ManagedBeanTypeImpl();
 		return managedBeanType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedPropertyType createManagedPropertyType() {
 		ManagedPropertyTypeImpl managedPropertyType = new ManagedPropertyTypeImpl();
 		return managedPropertyType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MapEntriesType createMapEntriesType() {
 		MapEntriesTypeImpl mapEntriesType = new MapEntriesTypeImpl();
 		return mapEntriesType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MapEntryType createMapEntryType() {
 		MapEntryTypeImpl mapEntryType = new MapEntryTypeImpl();
 		return mapEntryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MessageBundleType createMessageBundleType() {
 		MessageBundleTypeImpl messageBundleType = new MessageBundleTypeImpl();
 		return messageBundleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NameType createNameType() {
 		NameTypeImpl nameType = new NameTypeImpl();
 		return nameType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationCaseType createNavigationCaseType() {
 		NavigationCaseTypeImpl navigationCaseType = new NavigationCaseTypeImpl();
 		return navigationCaseType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationHandlerType createNavigationHandlerType() {
 		NavigationHandlerTypeImpl navigationHandlerType = new NavigationHandlerTypeImpl();
 		return navigationHandlerType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationRuleType createNavigationRuleType() {
 		NavigationRuleTypeImpl navigationRuleType = new NavigationRuleTypeImpl();
 		return navigationRuleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NullValueType createNullValueType() {
 		NullValueTypeImpl nullValueType = new NullValueTypeImpl();
 		return nullValueType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingType createOrderingType() {
 		OrderingTypeImpl orderingType = new OrderingTypeImpl();
 		return orderingType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingOrderingType createOrderingOrderingType() {
 		OrderingOrderingTypeImpl orderingOrderingType = new OrderingOrderingTypeImpl();
 		return orderingOrderingType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingOthersType createOrderingOthersType() {
 		OrderingOthersTypeImpl orderingOthersType = new OrderingOthersTypeImpl();
 		return orderingOthersType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PartialViewContextFactoryType createPartialViewContextFactoryType() {
 		PartialViewContextFactoryTypeImpl partialViewContextFactoryType = new PartialViewContextFactoryTypeImpl();
 		return partialViewContextFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PhaseListenerType createPhaseListenerType() {
 		PhaseListenerTypeImpl phaseListenerType = new PhaseListenerTypeImpl();
 		return phaseListenerType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyClassType createPropertyClassType() {
 		PropertyClassTypeImpl propertyClassType = new PropertyClassTypeImpl();
 		return propertyClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyExtensionType createPropertyExtensionType() {
 		PropertyExtensionTypeImpl propertyExtensionType = new PropertyExtensionTypeImpl();
 		return propertyExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyNameType createPropertyNameType() {
 		PropertyNameTypeImpl propertyNameType = new PropertyNameTypeImpl();
 		return propertyNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyResolverType createPropertyResolverType() {
 		PropertyResolverTypeImpl propertyResolverType = new PropertyResolverTypeImpl();
 		return propertyResolverType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyType createPropertyType() {
 		PropertyTypeImpl propertyType = new PropertyTypeImpl();
 		return propertyType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RedirectType createRedirectType() {
 		RedirectTypeImpl redirectType = new RedirectTypeImpl();
 		return redirectType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RedirectViewParamType createRedirectViewParamType() {
 		RedirectViewParamTypeImpl redirectViewParamType = new RedirectViewParamTypeImpl();
 		return redirectViewParamType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanClassType createReferencedBeanClassType() {
 		ReferencedBeanClassTypeImpl referencedBeanClassType = new ReferencedBeanClassTypeImpl();
 		return referencedBeanClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanNameType createReferencedBeanNameType() {
 		ReferencedBeanNameTypeImpl referencedBeanNameType = new ReferencedBeanNameTypeImpl();
 		return referencedBeanNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanType createReferencedBeanType() {
 		ReferencedBeanTypeImpl referencedBeanType = new ReferencedBeanTypeImpl();
 		return referencedBeanType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererClassType createRendererClassType() {
 		RendererClassTypeImpl rendererClassType = new RendererClassTypeImpl();
 		return rendererClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererExtensionType createRendererExtensionType() {
 		RendererExtensionTypeImpl rendererExtensionType = new RendererExtensionTypeImpl();
 		return rendererExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererType createRendererType() {
 		RendererTypeImpl rendererType = new RendererTypeImpl();
 		return rendererType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererTypeType createRendererTypeType() {
 		RendererTypeTypeImpl rendererTypeType = new RendererTypeTypeImpl();
 		return rendererTypeType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitClassType createRenderKitClassType() {
 		RenderKitClassTypeImpl renderKitClassType = new RenderKitClassTypeImpl();
 		return renderKitClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitFactoryType createRenderKitFactoryType() {
 		RenderKitFactoryTypeImpl renderKitFactoryType = new RenderKitFactoryTypeImpl();
 		return renderKitFactoryType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitIdType createRenderKitIdType() {
 		RenderKitIdTypeImpl renderKitIdType = new RenderKitIdTypeImpl();
 		return renderKitIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitType createRenderKitType() {
 		RenderKitTypeImpl renderKitType = new RenderKitTypeImpl();
 		return renderKitType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SmallIconType createSmallIconType() {
 		SmallIconTypeImpl smallIconType = new SmallIconTypeImpl();
 		return smallIconType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SourceClassType createSourceClassType() {
 		SourceClassTypeImpl sourceClassType = new SourceClassTypeImpl();
 		return sourceClassType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StateManagerType createStateManagerType() {
 		StateManagerTypeImpl stateManagerType = new StateManagerTypeImpl();
 		return stateManagerType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SuggestedValueType createSuggestedValueType() {
 		SuggestedValueTypeImpl suggestedValueType = new SuggestedValueTypeImpl();
 		return suggestedValueType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SupportedLocaleType createSupportedLocaleType() {
 		SupportedLocaleTypeImpl supportedLocaleType = new SupportedLocaleTypeImpl();
 		return supportedLocaleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventClassType createSystemEventClassType() {
 		SystemEventClassTypeImpl systemEventClassType = new SystemEventClassTypeImpl();
 		return systemEventClassType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventListenerClassType createSystemEventListenerClassType() {
 		SystemEventListenerClassTypeImpl systemEventListenerClassType = new SystemEventListenerClassTypeImpl();
 		return systemEventListenerClassType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventListenerType createSystemEventListenerType() {
 		SystemEventListenerTypeImpl systemEventListenerType = new SystemEventListenerTypeImpl();
 		return systemEventListenerType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TagHandlerDelegateFactoryType createTagHandlerDelegateFactoryType() {
 		TagHandlerDelegateFactoryTypeImpl tagHandlerDelegateFactoryType = new TagHandlerDelegateFactoryTypeImpl();
 		return tagHandlerDelegateFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ToViewIdType createToViewIdType() {
 		ToViewIdTypeImpl toViewIdType = new ToViewIdTypeImpl();
 		return toViewIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorClassType createValidatorClassType() {
 		ValidatorClassTypeImpl validatorClassType = new ValidatorClassTypeImpl();
 		return validatorClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorIdType createValidatorIdType() {
 		ValidatorIdTypeImpl validatorIdType = new ValidatorIdTypeImpl();
 		return validatorIdType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorType createValidatorType() {
 		ValidatorTypeImpl validatorType = new ValidatorTypeImpl();
 		return validatorType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValueClassType createValueClassType() {
 		ValueClassTypeImpl valueClassType = new ValueClassTypeImpl();
 		return valueClassType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValueType createValueType() {
 		ValueTypeImpl valueType = new ValueTypeImpl();
 		return valueType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public VariableResolverType createVariableResolverType() {
 		VariableResolverTypeImpl variableResolverType = new VariableResolverTypeImpl();
 		return variableResolverType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewHandlerType createViewHandlerType() {
 		ViewHandlerTypeImpl viewHandlerType = new ViewHandlerTypeImpl();
 		return viewHandlerType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ApplicationExtensionType createApplicationExtensionType() {
 		ApplicationExtensionTypeImpl applicationExtensionType = new ApplicationExtensionTypeImpl();
 		return applicationExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ResourceBundleType createResourceBundleType() {
 		ResourceBundleTypeImpl resourceBundleType = new ResourceBundleTypeImpl();
 		return resourceBundleType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public BaseNameType createBaseNameType() {
 		BaseNameTypeImpl baseNameType = new BaseNameTypeImpl();
 		return baseNameType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public VarType createVarType() {
 		VarTypeImpl varType = new VarTypeImpl();
 		return varType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewDeclarationLanguageFactoryType createViewDeclarationLanguageFactoryType() {
 		ViewDeclarationLanguageFactoryTypeImpl viewDeclarationLanguageFactoryType = new ViewDeclarationLanguageFactoryTypeImpl();
 		return viewDeclarationLanguageFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public VisitContextFactoryType createVisitContextFactoryType() {
 		VisitContextFactoryTypeImpl visitContextFactoryType = new VisitContextFactoryTypeImpl();
 		return visitContextFactoryType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public RenderKitExtensionType createRenderKitExtensionType() {
 		RenderKitExtensionTypeImpl renderKitExtensionType = new RenderKitExtensionTypeImpl();
 		return renderKitExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ResourceHandlerType createResourceHandlerType() {
 		ResourceHandlerTypeImpl resourceHandlerType = new ResourceHandlerTypeImpl();
 		return resourceHandlerType;
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public NavigationRuleExtensionType createNavigationRuleExtensionType() {
 		NavigationRuleExtensionTypeImpl navigationRuleExtensionType = new NavigationRuleExtensionTypeImpl();
 		return navigationRuleExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ValidatorExtensionType createValidatorExtensionType() {
 		ValidatorExtensionTypeImpl validatorExtensionType = new ValidatorExtensionTypeImpl();
 		return validatorExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public FacesConfigExtensionType createFacesConfigExtensionType() {
 		FacesConfigExtensionTypeImpl facesConfigExtensionType = new FacesConfigExtensionTypeImpl();
 		return facesConfigExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public FactoryExtensionType createFactoryExtensionType() {
 		FactoryExtensionTypeImpl factoryExtensionType = new FactoryExtensionTypeImpl();
 		return factoryExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public LifecycleExtensionType createLifecycleExtensionType() {
 		LifecycleExtensionTypeImpl lifecycleExtensionType = new LifecycleExtensionTypeImpl();
 		return lifecycleExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ManagedBeanExtensionType createManagedBeanExtensionType() {
 		ManagedBeanExtensionTypeImpl managedBeanExtensionType = new ManagedBeanExtensionTypeImpl();
 		return managedBeanExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public ConverterExtensionType createConverterExtensionType() {
 		ConverterExtensionTypeImpl converterExtensionType = new ConverterExtensionTypeImpl();
 		return converterExtensionType;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesConfigPackage getFacesConfigPackage() {
 		return (FacesConfigPackage)getEPackage();
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @return the package 
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	public static FacesConfigPackage getPackage() {
 		return FacesConfigPackage.eINSTANCE;
 	}
 
 } //FacesConfigFactoryImpl
