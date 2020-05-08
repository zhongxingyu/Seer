 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.util;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.felix.ipojo.metadata.Attribute;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.FieldMetadata;
 import org.apache.felix.ipojo.parser.MethodMetadata;
 import org.apache.felix.ipojo.parser.PojoMetadata;
 
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration.CodeReflection;
 import fr.imag.adele.apam.declarations.CallbackDeclaration;
 import fr.imag.adele.apam.declarations.ComponentDeclaration;
 import fr.imag.adele.apam.declarations.ComponentKind;
 import fr.imag.adele.apam.declarations.ComponentReference;
 import fr.imag.adele.apam.declarations.CompositeDeclaration;
 import fr.imag.adele.apam.declarations.ConstrainedReference;
 import fr.imag.adele.apam.declarations.InjectedPropertyPolicy;
 import fr.imag.adele.apam.declarations.ResolvePolicy;
 import fr.imag.adele.apam.declarations.GrantDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationReference;
 import fr.imag.adele.apam.declarations.InstanceDeclaration;
 import fr.imag.adele.apam.declarations.InstanceReference;
 import fr.imag.adele.apam.declarations.InterfaceReference;
 import fr.imag.adele.apam.declarations.LinkDeclaration;
 import fr.imag.adele.apam.declarations.MessageReference;
 import fr.imag.adele.apam.declarations.MissingPolicy;
 import fr.imag.adele.apam.declarations.OwnedComponentDeclaration;
 import fr.imag.adele.apam.declarations.PropertyDefinition;
 import fr.imag.adele.apam.declarations.ProviderInstrumentation;
 import fr.imag.adele.apam.declarations.RelationDeclaration;
 import fr.imag.adele.apam.declarations.RelationPromotion;
 import fr.imag.adele.apam.declarations.RequirerInstrumentation;
 import fr.imag.adele.apam.declarations.ResolvableReference;
 import fr.imag.adele.apam.declarations.CreationPolicy;
 import fr.imag.adele.apam.declarations.ResourceReference;
 import fr.imag.adele.apam.declarations.SpecificationDeclaration;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 import fr.imag.adele.apam.declarations.UndefinedReference;
 import fr.imag.adele.apam.util.CoreParser.ErrorHandler.Severity;
 
 /**
  * Parse an APAM declaration from its iPojo metadata representation.
  * 
  * Notice that this parser tries to build a representation of the metadata declarations even in the presence of
  * errors. It is up to the error handler to abort parsing if necessary by throwing unrecoverable parsing exceptions.
  * It will add place holders for missing information that can be verified after parsing by another tool.
  * 
  * @author vega
  * 
  */
 public class CoreMetadataParser implements CoreParser {
 
 	/**
 	 * Constants defining the different element and attributes
 	 */
 	private static final String  APAM                    = "fr.imag.adele.apam";
 	private static final String  COMPONENT               = "component";
 	private static final String  SPECIFICATION           = "specification";
 	private static final String  IMPLEMENTATION          = "implementation";
 	private static final String  COMPOSITE               = "composite";
 	private static final String  INSTANCE                = "instance";
 	private static final String  INSTANCE_ALT            = "apam-instance";
 	private static final String  DEFINITIONS             = "definitions";
 	private static final String  DEFINITION              = "definition";
 	private static final String  PROPERTIES              = "properties";
 	private static final String  PROPERTY                = "property";
 	private static final String  DEPENDENCIES            = "dependencies";
 	private static final String  DEPENDENCY              = "dependency";
 	private static final String  RELATIONS               = "relations";
 	private static final String  RELATION                = "relation";
 	private static final String  OVERRIDE                = "override";
 	private static final String  LINK                    = "link";
 	private static final String  INTERFACE               = "interface";
 	private static final String  MESSAGE                 = "message";
 	private static final String  CONSTRAINTS             = "constraints";
 	private static final String  CONSTRAINT              = "constraint";
 	private static final String  PREFERENCES             = "preferences";
 	private static final String  CONTENT                 = "contentMngt";
 	private static final String  START                   = "start";
 	private static final String  TRIGGER                 = "trigger";
 	private static final String  OWN                     = "own";
 	private static final String  PROMOTE                 = "promote";
 	private static final String  GRANT                   = "grant";
 	private static final String  DENY          	     = "deny";
 	private static final String  STATE                   = "state";
 	private static final String  IMPORTS                 = "import";
 	private static final String  EXPORT                  = "export";
 	private static final String  EXPORTAPP               = "exportapp";
 	private static final String  CALLBACK                = "callback";
 
 	private static final String  ATT_NAME                = "name";
 	private static final String  ATT_CLASSNAME           = "classname";
 	private static final String  ATT_EXCLUSIVE           = "exclusive";
 	private static final String  ATT_SINGLETON           = "singleton";
 	private static final String  ATT_SHARED              = "shared";
 	private static final String  ATT_INSTANTIABLE        = "instantiable";
 	private static final String  ATT_SPECIFICATION       = "specification";
 	private static final String  ATT_IMPLEMENTATION      = "implementation";
 	private static final String  ATT_MAIN_IMPLEMENTATION = "main";              // "mainImplem"
 	private static final String  ATT_INSTANCE            = "instance";
 	private static final String  ATT_INTERFACES          = "interfaces";
 	private static final String  ATT_MESSAGES            = "messages";
 	private static final String  ATT_TYPE                = "type";
 	private static final String  ATT_DEFAULT             = "default";
 	private static final String  ATT_VALUE               = "value";
 	private static final String  ATT_FIELD               = "field";
 	private static final String  ATT_INJECTED            = "injected";
 	private static final String  ATT_MULTIPLE            = "multiple";
 	private static final String  ATT_SOURCE              = "source";
 	private static final String  ATT_TARGET              = "target";
 	private static final String  ATT_SOURCE_KIND         = "sourceKind";
 	private static final String  ATT_TARGET_KIND         = "targetKind";
 	private static final String  ATT_FAIL                = "fail";
 	private static final String  ATT_EXCEPTION           = "exception";
 	private static final String  ATT_HIDE                = "hide";
 	private static final String  ATT_FILTER              = "filter";
 	private static final String  ATT_PROPERTY            = "property";
 	private static final String  ATT_DEPENDENCY          = "dependency";
 	private static final String  ATT_TO                  = "to";
 	private static final String  ATT_WHEN                = "when";
 	private static final String  ATT_ON_REMOVE           = "onRemove";
 	private static final String  ATT_ON_INIT             = "onInit";
 	private static final String  ATT_METHOD              = "method";
 	private static final String  ATT_PUSH                = "push";
 	private static final String  ATT_PULL                = "pull";
 	private static final String  ATT_BIND                = "added";
 	private static final String  ATT_UNBIND              = "removed";
 	private static final String  ATT_CREATION_POLICY     = "creation";
 	private static final String  ATT_RESOLVE_POLICY      = "resolve";
 
 	private static final String  VALUE_OPTIONAL          = "optional";
 	private static final String  VALUE_WAIT              = "wait";
 	private static final String  VALUE_EXCEPTION         = "exception";
 
 	private static final String  VALUE_KIND_INSTANCE        = "instance";
 	private static final String  VALUE_KIND_IMPLEMENTATION  = "implementation";
 	private static final String  VALUE_KIND_SPECIFICATION   = "specification";
 
 	/**
 	 * The parsed metatadata
 	 */
 	private Element              metadata;
 
 	/**
 	 * The optional service that give access to introspection information
 	 */
 	private IntrospectionService introspector;
 
 	/**
 	 * A service to access introspection information for primitive components
 	 */
 	public interface IntrospectionService {
 
 		/**
 		 * Get reflection information for the implementation class
 		 */
 		public Class<?> getInstrumentedClass(String classname) throws ClassNotFoundException;
 	}
 
 	/**
 	 * The last parsed declarations
 	 */
 	private List<ComponentDeclaration> declaredElements;
 
 	/**
 	 * The currently used error handler
 	 */
 	private ErrorHandler               errorHandler;
 
 	public CoreMetadataParser(Element metadata) {
 		this(metadata, null);
 	}
 
 	public CoreMetadataParser(Element metadata, IntrospectionService introspector) {
 		setMetadata(metadata, introspector);
 	}
 
 	/**
 	 * Initialize parser with the given metadata and instrumentation code
 	 */
 	public synchronized void setMetadata(Element metadata, IntrospectionService introspector) {
 		this.metadata = metadata;
 		this.introspector = introspector;
 		declaredElements = null;
 	}
 
 	/**
 	 * Parse the ipojo metadata to get the component declarations
 	 */
 	@Override
 	public synchronized List<ComponentDeclaration> getDeclarations(ErrorHandler errorHandler) {
 		if (declaredElements != null)
 			return declaredElements;
 
 		declaredElements = new ArrayList<ComponentDeclaration>();
 		this.errorHandler = errorHandler;
 
 		List<SpecificationDeclaration> specifications = new ArrayList<SpecificationDeclaration>();
 		List<AtomicImplementationDeclaration> primitives = new ArrayList<AtomicImplementationDeclaration>();
 		List<CompositeDeclaration> composites = new ArrayList<CompositeDeclaration>();
 		List<InstanceDeclaration> instances = new ArrayList<InstanceDeclaration>();
 
 		for (Element element : metadata.getElements()) {
 
 			/*
 			 * Ignore not APAM elements 
 			 */
 			if (!CoreMetadataParser.isApamDefinition(element))
 				continue;
 
 			/*
 			 * switch depending on component type
 			 */
 			if (CoreMetadataParser.isSpecification(element))
 				specifications.add(parseSpecification(element));
 
 			if (CoreMetadataParser.isPrimitiveImplementation(element))
 				primitives.add(parsePrimitive(element));
 
 			if (CoreMetadataParser.isCompositeImplementation(element))
 				composites.add(parseComposite(element));
 
 			if (CoreMetadataParser.isInstance(element))
 				instances.add(parseInstance(element));
 
 		}
 
 		/*
 		 * Add declarations in order of relation to ease cross-reference
 		 * validation irrespective of the declaration order
 		 */
 
 		declaredElements.addAll(specifications);
 		declaredElements.addAll(primitives);
 		declaredElements.addAll(composites);
 		declaredElements.addAll(instances);
 
 		// Release references once the parsed data is cached
 		metadata = null;
 		introspector = null;
 		this.errorHandler = null;
 
 		return declaredElements;
 	}
 
 	/**
 	 * parse the common attributes shared by all declarations
 	 */
 	private void parseComponent(Element element, ComponentDeclaration component) {
 
 		parseProvidedResources(element, component);
 		parsePropertyDefinitions(element, component);
 		parseProperties(element, component);
 		parseRelations(element, component);
 		parseLinks(element,component);
 
 		boolean isInstantiable = parseBoolean(component.getName(),element, CoreMetadataParser.ATT_INSTANTIABLE, false, true);
 		boolean isExclusive = parseBoolean(component.getName(),element, CoreMetadataParser.ATT_EXCLUSIVE, false, false);
 		boolean isSingleton = parseBoolean(component.getName(),element, CoreMetadataParser.ATT_SINGLETON, false, false);
 		boolean isShared = parseBoolean(component.getName(),element, CoreMetadataParser.ATT_SHARED, false, true);
 
 		component.setExclusive(isExclusive);
 		component.setInstantiable(isInstantiable);
 		component.setSingleton(isSingleton);
 		component.setShared(isShared);
 
 		boolean isDefinedInstantiable = parseisDefinedBoolean(component.getName(),element, CoreMetadataParser.ATT_INSTANTIABLE);
 		boolean isDefinedExclusive = parseisDefinedBoolean(component.getName(),element, CoreMetadataParser.ATT_EXCLUSIVE);
 		boolean isDefinedSingleton = parseisDefinedBoolean(component.getName(),element, CoreMetadataParser.ATT_SINGLETON);
 		boolean isDefinedShared = parseisDefinedBoolean(component.getName(),element, CoreMetadataParser.ATT_SHARED);
 
 		component.setDefinedExclusive(isDefinedExclusive);
 		component.setDefinedInstantiable(isDefinedInstantiable);
 		component.setDefinedSingleton(isDefinedSingleton);
 		component.setDefinedShared(isDefinedShared);
 
 		// Exclusive means: shared=false and singleton=true.
 		if (isDefinedExclusive && isExclusive) {
 			if (isDefinedShared && isShared) {
 				errorHandler.error(Severity.ERROR, "A component cannot be both exclusive and shared or not singleton");
 			}
 			if (isDefinedSingleton && isSingleton) {
 				errorHandler.error(Severity.ERROR, "A component cannot be both exclusive and not singleton");
 			}
 			component.setSingleton(true);
 			component.setShared(false);
 			component.setDefinedSingleton(true);
 			component.setDefinedShared(true);
 		}
 	}
 
 	/**
 	 * Parse an specification declaration
 	 */
 	private SpecificationDeclaration parseSpecification(Element element) {
 
 		SpecificationDeclaration declaration = new SpecificationDeclaration(parseName(element));
 		parseComponent(element, declaration);
 
 		return declaration;
 	}
 
 	/**
 	 * Parse an atomic implementation declaration
 	 */
 	private AtomicImplementationDeclaration parsePrimitive(Element element) {
 
 		String name = parseName(element);
 		SpecificationReference specification = parseSpecificationReference(name,element,CoreMetadataParser.ATT_SPECIFICATION, false);
 
 		String className = parseString(name,element, CoreMetadataParser.ATT_CLASSNAME, true);
 
 		/*
 		 * load Pojo instrumentation metadata
 		 */
 		PojoMetadata pojoMetadata = null;
 		Class<?> instrumentedCode = null;
 		try {
 			pojoMetadata = new PojoMetadata(element);
 			instrumentedCode = ((className != CoreParser.UNDEFINED) && (introspector != null)) ? introspector
 					.getInstrumentedClass(className) : null;
 		} catch (ClassNotFoundException e) {
 			errorHandler.error(Severity.ERROR, "Apam component " + name + ": " + "the component class " + className
 					+ " can not be loaded");
 		} catch (Exception ignoredException) {
 		}
 
 		CodeReflection reflection = new ImplementationReflection(className, pojoMetadata, instrumentedCode);
 
 		AtomicImplementationDeclaration declaration = new AtomicImplementationDeclaration(name,specification,reflection);
 		parseComponent(element, declaration);
 
 
 		/*
 		 *  Parse message producer method interception
 		 */
 		String messageMethods = parseString(name,element, CoreMetadataParser.ATT_PUSH, false);
 		for (String messageMethod : Util.split(messageMethods)) {
 
 			/*
 			 * Parse optionally specified method signature
 			 */
 			String methodName 		= messageMethod.trim();
 			String methodSignature	= null;
 			
 			if (methodName.indexOf("(") != -1 && methodName.endsWith(")")) {
 				methodSignature = methodName.substring(methodName.indexOf("(")+1, methodName.length()-1);
 				methodName		= methodName.substring(0,methodName.indexOf("(")-1);
 			}
 			
 			declaration.getProviderInstrumentation().add(new ProviderInstrumentation.MessageProviderMethodInterception(declaration, methodName, methodSignature));
 		}
 
 		/*
 		 *  Verify that at least one method is intercepted for each declared produced message.
 		 */
 		for (MessageReference message : declaration.getProvidedResources(MessageReference.class)) {
 
 			boolean declared = declaration.getProviderInstrumentation().size() > 0;
 			boolean injected = false;
 			boolean defined = false;
 
 			for (ProviderInstrumentation providerInstrumentation : declaration.getProviderInstrumentation()) {
 				
 				ResourceReference instrumentedResource = providerInstrumentation.getProvidedResource();
 				
 				if (instrumentedResource instanceof UndefinedReference)
 					continue;
 
 				defined = true;
 
 				if (! instrumentedResource.equals(message))
 					continue;
 
 				injected = true;
 				break;
 			}
 
 			/*
 			 * If we could determine the method types and there was no injection then signal error
 			 * 
 			 * NOTE Notice that some errors will not be detected at build time since all the reflection
 			 * information is not available, and validation must be delayed until run time
 			 */
 			if (!declared || (defined && !injected))
 				errorHandler.error(Severity.ERROR, "Apam component " + name + ": " + " message of type "
 						+ message.getJavaType() + " is not produced by any push method");
 
 		}
 
 		/*
 		 *  if not explicitly provided, get all the implemented interfaces.
 		 */
 		if (declaration.getProvidedResources().isEmpty() && (pojoMetadata != null)) {
 			for (String implementedInterface : pojoMetadata.getInterfaces()) {
 				if (!implementedInterface.startsWith("java.lang"))
 					declaration.getProvidedResources().add(new InterfaceReference(implementedInterface));
 			}
 		}
 
 		/*
 		 * If not explicitly provided, get all produced messages from the declared intercepted methods
 		 */
 		Set<MessageReference> declaredMessages = declaration.getProvidedResources(MessageReference.class);
 		for (ProviderInstrumentation providerInstrumentation : declaration.getProviderInstrumentation()) {
 
 			MessageReference instrumentedMessage = providerInstrumentation.getProvidedResource().as(MessageReference.class);
 
 			if (instrumentedMessage == null)
 				continue;
 			
 			if (declaredMessages.contains(instrumentedMessage))
 				continue;
 
 			declaration.getProvidedResources().add(instrumentedMessage);
 		}
 
 		/*
 		 *  If instrumented code is provided verify that all provided resources reference accessible classes
 		 */
 		if (introspector != null) {
 			for (ResourceReference providedResource : declaration.getProvidedResources()) {
 				
 				if (providedResource instanceof UndefinedReference)
 					continue;
 				
 				try {
 					introspector.getInstrumentedClass(providedResource.getJavaType());
 				} catch (ClassNotFoundException e) {
 					errorHandler.error(Severity.ERROR, "Apam component " + name + ": " + "the provided resource "
 							+ providedResource.getJavaType() + " can not be loaded");
 				}
 			}
 		}
 
 		/*
 		 * Iterate over all sub elements looking for callback declarations
 		 */
 		for (Element callback : optional(element.getElements(CoreMetadataParser.CALLBACK, CoreMetadataParser.APAM))) {
 			String onInit = parseString(name,callback, CoreMetadataParser.ATT_ON_INIT, false);
 			String onRemove = parseString(name,callback, CoreMetadataParser.ATT_ON_REMOVE, false);
 
 			if (onInit != null) {
 				declaration.addCallback(AtomicImplementationDeclaration.Event.INIT,parseCallback(declaration,onInit));
 			}
 
 			if (onRemove != null) {
 				declaration.addCallback(AtomicImplementationDeclaration.Event.REMOVE,parseCallback(declaration,onRemove));
 			}
 
 		}
 
 		return declaration;
 
 	}
 
 	/**
 	 * Parse a composite declaration
 	 */
 	private CompositeDeclaration parseComposite(Element element) {
 
 		String name = parseName(element);
 		SpecificationReference specification = parseSpecificationReference(name,element,CoreMetadataParser.ATT_SPECIFICATION, false);
 		ComponentReference<?> implementation = parseAnyComponentReference(name,element,CoreMetadataParser.ATT_MAIN_IMPLEMENTATION, false);
 
 		CompositeDeclaration declaration = new CompositeDeclaration(name, specification, implementation);
 		parseComponent(element, declaration);
 		parseCompositeContent(element, declaration);
 
 		return declaration;
 	}
 
 	/**
 	 * parse the content management policies of a composite
 	 */
 	private void parseCompositeContent(Element element, CompositeDeclaration declaration) {
 
 		/*
 		 * Look for content management specification
 		 */
 		Element contents[] = optional(element.getElements(CoreMetadataParser.CONTENT, CoreMetadataParser.APAM));
 
 		if (contents.length > 1)
 			errorHandler.error(Severity.ERROR, "A single content management is allowed in a composite declaration" + element);
 
 		if (contents.length == 0)
 			return;
 
 		Element content = contents[0];
 
 		parseState(content, declaration);
 		parseVisibility(content, declaration);
 		parsePromotions(content, declaration);
 		parseOwns(content, declaration);
 
 		parseContextualRelations(content, declaration);
 
 		parseOwnedInstances(content, declaration);
 		parseContextualLinks(content, declaration);
 	}
 
 	/**
 	 * Parse an instance declaration
 	 */
 	private InstanceDeclaration parseInstance(Element element) {
 
 		String name = parseName(element);
 		ImplementationReference<?> implementation = parseImplementationReference(name,element,
 				CoreMetadataParser.ATT_IMPLEMENTATION, true);
 
 		/*
 		 * look for optional trigger declarations
 		 */
 
 		Element triggers[] = optional(element.getElements(CoreMetadataParser.TRIGGER, CoreMetadataParser.APAM));
 		Element trigger = triggers.length > 0 ? triggers[0] : null;
 
 		if (triggers.length > 1)
 			errorHandler.error(Severity.ERROR, "A single trigger declaration is allowed in an instance declaration"
 					+ element);
 
 		/*
 		 * Parse triggering conditions
 		 */
 		Set<ConstrainedReference> triggerDeclarations = new HashSet<ConstrainedReference>();
 		for (Element triggerCondition : optional(trigger != null ? trigger.getElements() : null)) {
 
 			/*
 			 * ignore elements that are not from APAM
 			 */
 			if (!CoreMetadataParser.isApamDefinition(triggerCondition))
 				continue;
 
 			ConstrainedReference triggerDeclaration = new ConstrainedReference(parseResolvableReference(name,
 					triggerCondition, CoreMetadataParser.ATT_NAME, true));
 
 			/*
 			 * parse optional constraints
 			 */
 			for (Element constraints : optional(triggerCondition.getElements(CoreMetadataParser.CONSTRAINTS,
 					CoreMetadataParser.APAM))) {
 				parseConstraints(name,constraints, triggerDeclaration);
 			}
 
 			triggerDeclarations.add(triggerDeclaration);
 
 		}
 
 		InstanceDeclaration declaration = new InstanceDeclaration(implementation, name, triggerDeclarations);
 		parseComponent(element, declaration);
 		return declaration;
 	}
 
 	/**
 	 * parse the provided resources of a component
 	 */
 	private void parseProvidedResources(Element element, ComponentDeclaration component) {
 
 		String interfaces = parseString(component.getName(),element, CoreMetadataParser.ATT_INTERFACES, false);
 		String messages = parseString(component.getName(),element, CoreMetadataParser.ATT_MESSAGES, false);
 
 		for (String interfaceName : Util.split(interfaces)) {
 			component.getProvidedResources().add(new InterfaceReference(interfaceName));
 		}
 
 		for (String message : Util.split(messages)) {
 			component.getProvidedResources().add(new MessageReference(message));
 		}
 
 	}
 
 	/**
 	 * parse callback declaration
 	 */
 
 	private CallbackDeclaration parseCallback(AtomicImplementationDeclaration implementation, String methodName) {
 		CallbackDeclaration callback = new CallbackDeclaration(implementation, methodName);
 		if (!callback.isValidInstrumentation())
 			errorHandler.error(Severity.ERROR, implementation.getName() + " : the specified method \"" + methodName + "\" is invalid or not found");
 		return callback;
 
 	}
 
 	/**
 	 * parse the declared links of a component
 	 */
 	private void parseLinks(Element element, ComponentDeclaration component) {
 
 		/*
 		 * Iterate over all sub elements looking for link declarations
 		 */
 		for (Element link : optional(element.getElements(CoreMetadataParser.LINK, CoreMetadataParser.APAM))) {
 			/*
 			 * Add to component declaration
 			 */
 			component.getPredefinedLinks().add(parseLink(link,component));
 
 		}
 
 	}
 
 	/**
 	 * parse the contextual links defined in a composite
 	 */
 	private void parseContextualLinks(Element element, CompositeDeclaration composite) {
 
 		/*
 		 * Iterate over all sub elements looking for link declarations
 		 */
 		for (Element link : optional(element.getElements(CoreMetadataParser.LINK, CoreMetadataParser.APAM))) {
 			/*
 			 * Add to component declaration
 			 */
 			composite.getContextualLinks().add(parseLink(link,composite,true));
 
 		}
 
 	}
 
 	/**
 	 * parse a link declaration
 	 */
 	private LinkDeclaration parseLink(Element element, ComponentDeclaration component) {
 		return parseLink(element, component, false);
 	}
 
 	/**
 	 * parse a link declaration
 	 */
 	private LinkDeclaration parseLink(Element element, ComponentDeclaration component, boolean isContextual) {
 
 		String id 		= parseString(component.getName(), element, CoreMetadataParser.ATT_NAME, true);
 		String source 	= parseString(component.getName(), element, CoreMetadataParser.ATT_SOURCE, isContextual);
 		String target 	= parseString(component.getName(), element, CoreMetadataParser.ATT_TARGET, true);
 
 		ComponentReference<?> sourceReference = source != null ? new ComponentReference<ComponentDeclaration>(source) : component.getReference();
 		ComponentReference<?> targetReference = new ComponentReference<ComponentDeclaration>(target);
 
 		return new LinkDeclaration(id, sourceReference, targetReference);
 	}
 
 	/**
 	 * parse the declared relations of a component
 	 */
 	private void parseRelations(Element element, ComponentDeclaration component) {
 
 		/*
 		 *	Skip the optional enclosing list 
 		 */
 		for (Element dependencies : every(element.getElements(CoreMetadataParser.DEPENDENCIES,CoreMetadataParser.APAM),
 				element.getElements(CoreMetadataParser.RELATIONS,CoreMetadataParser.APAM)) ) {
 			parseRelations(dependencies, component);
 		}
 
 		/*
 		 * Iterate over all sub elements looking for relation declarations
 		 */
 		for (Element relation : every(element.getElements(CoreMetadataParser.DEPENDENCY, CoreMetadataParser.APAM),
 				element.getElements(CoreMetadataParser.RELATION, CoreMetadataParser.APAM))) {
 			/*
 			 * Add to component declaration
 			 */
 			RelationDeclaration relationDeclaration = parseRelation(relation,component);
 			if (! component.getDependencies().add(relationDeclaration)) {
 				errorHandler.error(Severity.ERROR, "Duplicate relation identifier " + relationDeclaration);
 			}
 
 
 			/*
 			 * If the relation explicitly defines a target, an implicit link is being defined
 			 */
 			String target = parseString(component.getName(), element, CoreMetadataParser.ATT_TARGET, false);
 			if (target != null)
 				component.getPredefinedLinks().add(parseLink(relation,component));
 		}
 
 	}
 
 	/**
 	 * parse the contextual relations defined in a composite
 	 */
 	private void parseContextualRelations(Element element, CompositeDeclaration composite) {
 
 		/*
 		 *	Skip the optional enclosing list 
 		 */
 		for (Element dependencies : every(element.getElements(CoreMetadataParser.DEPENDENCIES,CoreMetadataParser.APAM),
 				element.getElements(CoreMetadataParser.RELATIONS,CoreMetadataParser.APAM)) ) {
 			parseContextualRelations(dependencies, composite);
 		}
 
 		/*
 		 * Iterate over all sub elements looking for relation declarations
 		 */
 		for (Element relation : every(element.getElements(CoreMetadataParser.DEPENDENCY, CoreMetadataParser.APAM),
 				element.getElements(CoreMetadataParser.RELATION, CoreMetadataParser.APAM),
 				element.getElements(CoreMetadataParser.OVERRIDE, CoreMetadataParser.APAM))) {
 
 			/*
 			 * Add to content contextual declaration
 			 */
 			RelationDeclaration relationDeclaration 		= parseRelation(relation,composite,true);
 			Collection<RelationDeclaration> declarations 	= relationDeclaration.isOverride() ? composite.getOverridenDependencies() : composite.getContextualDependencies();
 
 			if ( ! declarations.add(relationDeclaration)) {
 				errorHandler.error(Severity.ERROR,"Duplicate relation identifier " + relationDeclaration);
 			}
 
 
 			/*
 			 * If the relation explicitly defines a target, an implicit link is being defined
 			 */
 			String target = parseString(composite.getName(), element, CoreMetadataParser.ATT_TARGET, false);
 			if (target != null)
 				composite.getContextualLinks().add(parseLink(relation,composite,true));
 
 		}
 
 	}
 
 	/**
 	 * parse a relation declaration
 	 */
 	private RelationDeclaration parseRelation(Element element, ComponentDeclaration component) {
 		return parseRelation(element, component, false);
 	}
 
 	/**
 	 * parse a relation declaration
 	 */
 	private RelationDeclaration parseRelation(Element element, ComponentDeclaration component, boolean isContextual) {
 		/*
 		 * Get the reference to the target of the relation if specified
 		 */
 		ResolvableReference targetDef = parseResolvableReference(component.getName(),element, false);
 
 		/*
 		 * All dependencies have an optional identifier and multiplicity specification, as well as an optional source kind
 		 * and target kind
 		 */
 		String id 					= parseString(component.getName(), element, CoreMetadataParser.ATT_NAME, false);
 		boolean isOverride			= isContextual && element.getName().equals(OVERRIDE);
 
 		boolean isMultiple			= parseBoolean(component.getName(),element, CoreMetadataParser.ATT_MULTIPLE, false, false);
 
 		String sourceName 			= parseString(component.getName(), element, CoreMetadataParser.ATT_SOURCE, isContextual && ! isOverride);
 
 		ComponentKind sourceKind	= parseKind(component.getName(), element,CoreMetadataParser.ATT_SOURCE_KIND, isContextual && ! isOverride, null);
 		ComponentKind targetKind 	= parseKind(component.getName(),element,CoreMetadataParser.ATT_TARGET_KIND,false,null);
 
 		/*
 		 * For atomic components, dependency declarations may optionally have a number of nested instrumentation declarations.
 		 * 
 		 * These are parsed first, as a number of attributes of the relation that are not explicitly declared can be inferred
 		 * from the instrumentation metadata.
 		 */
 
 		List<RequirerInstrumentation> instrumentations = new ArrayList<RequirerInstrumentation>();
 		if (component instanceof AtomicImplementationDeclaration) {
 
 			AtomicImplementationDeclaration atomic = (AtomicImplementationDeclaration) component;
 
 			/*
 			 * Optionally, as a shortcut, a single injection may be specified directly as an attribute
 			 * of the relation
 			 */
 			RequirerInstrumentation directInstrumentation = parseRelationInstrumentation(element, atomic, false);
 			if (directInstrumentation != null) {
 				instrumentations.add(directInstrumentation);
 			}
 
 			for (Element instrumentation : optional(element.getElements())) {
 
 				/*
 				 * ignore elements that are not from APAM
 				 */
 				if (!CoreMetadataParser.isApamDefinition(instrumentation))
 					continue;
 
 				/*
 				 * Accept only resource references
 				 */
 				String resourceKind = instrumentation.getName();
 				if (!(CoreMetadataParser.INTERFACE.equals(resourceKind) || CoreMetadataParser.MESSAGE.equals(resourceKind)))
 					continue;
 
 				instrumentations.add(parseRelationInstrumentation(instrumentation, atomic, true));
 
 			}
 
 
 		}
 
 		/*
 		 * If a target kind is explicitly declared, the specified instrumentation fields or method types must match.
 		 */
 		if (targetKind != null) {
 			for (RequirerInstrumentation instrumentation : instrumentations) {
 				
 				String javaType 			= instrumentation.getRequiredResource().getJavaType();
 				ResourceReference resource	= targetDef != null ? targetDef.as(ResourceReference.class) : null;
 				
 				/*
 				 * NOTE For target kind INSTANCE we can only verify if an explicit target resource is specified,
 				 * otherwise we accept the definition and it must be checked at the semantic level.
 				 */
 				boolean match    			= targetKind.isAssignableTo(javaType) || 
 											  (targetKind.equals(INSTANCE) && resource != null ? javaType.equals(resource) : true);
 				if (!match) {
 					errorHandler.error(Severity.ERROR,
 							"relation target doesn't match the type of the field or method " + instrumentation.getName() +
 							" in " + element);
 				}
 				
 			}
 		}
 
 		/*
 		 * If no ID was explicitly specified, but a single instrumentation was declared the the name of the field or method
 		 * becomes the ID of the relation.
 		 */
 		if ( id == null && instrumentations.size() == 1) {
 			id = instrumentations.get(0).getName();
 		}
 
 		/*
 		 * If no target was explicitly specified, sometimes we can infer it from the instrumentation metadata.
 		 * 
 		 */
 		if ( !instrumentations.isEmpty() && (targetDef == null || targetKind == null)) {
 			
 			ComponentKind 		inferredKind = null;
 			ResolvableReference inferredTarget = null;
 			
 			for (RequirerInstrumentation instrumentation : instrumentations) {
 				
 				String javaType 					= instrumentation.getRequiredResource().getJavaType();
 				ComponentKind 		candidateKind 	= null;
 				ResolvableReference candidateTarget = null;
 				
 				if (ComponentKind.COMPONENT.isAssignableTo(javaType)) {
 					candidateKind 	= null;
 					candidateTarget	= null;
 				}
 				else if (ComponentKind.SPECIFICATION.isAssignableTo(javaType)) {
 					candidateKind 	= ComponentKind.SPECIFICATION;
 					candidateTarget	= null;
 				}
 				else if (ComponentKind.IMPLEMENTATION.isAssignableTo(javaType)) {
 					candidateKind 	= ComponentKind.IMPLEMENTATION;
 					candidateTarget	= null;
 				}
 				else if (ComponentKind.INSTANCE.isAssignableTo(javaType)) {
 					candidateKind 	= ComponentKind.INSTANCE;
 					candidateTarget	= null;
 				}
 				else {
 					candidateKind 	= ComponentKind.INSTANCE;
 					candidateTarget	= instrumentation.getRequiredResource();
 				}
 				
 				/*
 				 * If there are conflicting declarations we gave up inferring target
 				 */
 				if (inferredKind != null && candidateKind != null && !inferredKind.equals(candidateKind)) {
 					inferredKind 	= null;
 					inferredTarget 	= null;
 					break;
 				}
 
 				if (inferredTarget != null && candidateTarget != null && !inferredTarget.equals(candidateTarget)) {
 					inferredKind 	= null;
 					inferredTarget 	= null;
 					break;
 				}
 
 				inferredKind 	= candidateKind != null ? candidateKind : inferredKind;
 				inferredTarget	= candidateTarget != null ? candidateTarget : inferredTarget;
 			}
 			
 			if (targetDef == null && inferredTarget != null)
 				targetDef = inferredTarget;
 			
 			if (targetKind == null && inferredKind != null)
 				targetKind = inferredKind;
 		}
 
 		if (id == null && targetDef == null) {
 			errorHandler.error(Severity.ERROR, "relation name or target must be specified " + element);
 		}
 
 		/*
 		 * No target was explicitly specified, record this fact
 		 */
 		if (targetDef == null)
 			targetDef = new ComponentReference<ComponentDeclaration>(CoreMetadataParser.UNDEFINED);
 
 		/*
 		 * Get the resolution policies
 		 */
 		String creationPolicyString		= parseString(component.getName(), element, CoreMetadataParser.ATT_CREATION_POLICY, false);
 		CreationPolicy creationPolicy   = CreationPolicy.getPolicy(creationPolicyString);
 		
 		String resolvePolicyString		= parseString(component.getName(), element, CoreMetadataParser.ATT_RESOLVE_POLICY, false);
 		ResolvePolicy resolvePolicy 	= ResolvePolicy.getPolicy(resolvePolicyString);
 
 		/*
 		 * Get the optional missing policy
 		 */
 		MissingPolicy missingPolicy 	= parsePolicy(component.getName(),element, CoreMetadataParser.ATT_FAIL, false, null);
 		String missingException 		= parseString(component.getName(),element, CoreMetadataParser.ATT_EXCEPTION, false);
 
 		/*
 		 * Get the optional contextual properties
 		 */
 		String mustHide = parseString(component.getName(),element, CoreMetadataParser.ATT_HIDE, false);
 
 		/*
 		 * Create the relation and add the declared instrumentation
 		 */
 		RelationDeclaration relation = new RelationDeclaration(component.getReference(),id,
 												sourceName,sourceKind,
 												targetDef,targetKind,
 												creationPolicy, resolvePolicy,isMultiple,
 												missingPolicy,missingException,
 												isOverride,mustHide != null ? Boolean.valueOf(mustHide): null);
 		
 		for (RequirerInstrumentation instrumentation : instrumentations) {
 			instrumentation.setRelation(relation);
 		}
 
 
 		/*
 		 * look for bind and unbind callbacks 
 		 */
 		String bindCallback = parseString(component.getName(),element, CoreMetadataParser.ATT_BIND, false);
 		String unbindCallback = parseString(component.getName(),element, CoreMetadataParser.ATT_UNBIND, false);
 		
 		if (component instanceof AtomicImplementationDeclaration) {
 			if (bindCallback != null) {
 				CallbackDeclaration callback = new CallbackDeclaration((AtomicImplementationDeclaration) component, bindCallback);
 				if (!callback.isValidInstrumentation())
 					errorHandler.error(Severity.ERROR, component.getName() + " : the specified method \"" + bindCallback + "\" in \""
 							+ CoreMetadataParser.ATT_BIND
 							+ "\" is invalid or not found");
 				relation.addCallback(RelationDeclaration.Event.BIND,callback);
 			}
 			if (unbindCallback != null) {
 				CallbackDeclaration callback = new CallbackDeclaration((AtomicImplementationDeclaration) component,unbindCallback);
 				if (!callback.isValidInstrumentation())
 					errorHandler.error(Severity.ERROR,  component.getName() + " : the specified method \"" + unbindCallback + "\" in \""
 							+ CoreMetadataParser.ATT_UNBIND
 							+ "\" is invalid or not found");
 				relation.addCallback(RelationDeclaration.Event.UNBIND,callback);
 			}
 		}
 
 		/*
 		 * Get the optional constraints and preferences
 		 */
 		for (Element constraints : optional(element
 				.getElements(CoreMetadataParser.CONSTRAINTS, CoreMetadataParser.APAM))) {
 			parseConstraints(component.getName(), constraints, relation);
 		}
 
 		for (Element preferences : optional(element
 				.getElements(CoreMetadataParser.PREFERENCES, CoreMetadataParser.APAM))) {
 			parsePreferences(component.getName(), preferences, relation);
 		}
 
 		return relation;
 	}
 
 	/**
 	 * parse the injected dependencies of a primitive
 	 */
 	private RequirerInstrumentation parseRelationInstrumentation(Element element, AtomicImplementationDeclaration atomic, boolean mandatory) {
 
 		String field = parseString(atomic.getName(),element, CoreMetadataParser.ATT_FIELD, false);
 		//        String method = parseString(element, CoreMetadataParser.ATT_METHOD, false);
 
 		String push = parseString(atomic.getName(),element, CoreMetadataParser.ATT_PUSH, false);
 		String pull = parseString(atomic.getName(),element, CoreMetadataParser.ATT_PULL, false);
 
 		if ((field == null) && (push == null) && (pull == null) && mandatory)
 			errorHandler.error(Severity.ERROR,
 					"in the component \"" + atomic.getName()
 					+ "\" relation attribute \"" + CoreMetadataParser.ATT_FIELD + "\" or \""
 					+ CoreMetadataParser.ATT_PUSH + "\" or \"" + CoreMetadataParser.ATT_PULL
 					+ "\" must be specified in " + element.getName());
 
 		if ((field == null) && CoreMetadataParser.INTERFACE.equals(element.getName()) && mandatory)
 			errorHandler.error(Severity.ERROR,
 					"in the component \"" + atomic.getName()
 					+ "\" relation attribute \""
 					+ CoreMetadataParser.ATT_FIELD + "\" must be specified in " + element.getName());
 
 		if ((push == null) && (pull==null) && CoreMetadataParser.MESSAGE.equals(element.getName()) && mandatory)
 			errorHandler.error(Severity.ERROR,
 					"in the component \"" + atomic.getName()
 					+ "\" relation attribute \""
 					+ CoreMetadataParser.ATT_PUSH + " or " + CoreMetadataParser.ATT_PULL +
 					"\" must be specified in " + element.getName());
 
 		if ((field == null) && (push == null) && (pull == null)) {
 			return mandatory ? new RequirerInstrumentation.RequiredServiceField(atomic, CoreParser.UNDEFINED) : null;
 		}
 		
 		RequirerInstrumentation instrumentation = null;
 
 		if (field != null) { 
 			instrumentation = new RequirerInstrumentation.RequiredServiceField(atomic, field);
 		} else if (push != null) { 
 			instrumentation = new RequirerInstrumentation.MessageConsumerCallback(atomic, push);
 		} else if (pull != null) {
 			instrumentation = new RequirerInstrumentation.MessageQueueField(atomic, pull);
 		}
 
 		if (!instrumentation.isValidInstrumentation())
 			errorHandler.error(Severity.ERROR, atomic.getName() + " : invalid class type for field or method " + instrumentation.getName());
 
 		return instrumentation;
 	}
 
 	/**
 	 * parse a constraints declaration
 	 */
 	private void parseConstraints(String componentName,Element element, ConstrainedReference reference) {
 
 		for (Element constraint : optional(element.getElements())) {
 
 			String filter = parseString(componentName,constraint, CoreMetadataParser.ATT_FILTER);
 
 			if (constraint.getName().equals(CoreMetadataParser.IMPLEMENTATION) ||
 					constraint.getName().equals(CoreMetadataParser.CONSTRAINT))
 				reference.getImplementationConstraints().add(filter);
 
 			if (constraint.getName().equals(CoreMetadataParser.INSTANCE))
 				reference.getInstanceConstraints().add(filter);
 
 		}
 
 	}
 
 	/**
 	 * parse a preferences declaration
 	 */
 	private void parsePreferences(String componentName,Element element, ConstrainedReference reference) {
 
 		for (Element preference : optional(element.getElements())) {
 
 			String filter = parseString(componentName,preference, CoreMetadataParser.ATT_FILTER);
 
 			if (preference.getName().equals(CoreMetadataParser.IMPLEMENTATION) ||
 					preference.getName().equals(CoreMetadataParser.CONSTRAINT))
 				reference.getImplementationPreferences().add(filter);
 
 			if (preference.getName().equals(CoreMetadataParser.INSTANCE))
 				reference.getInstancePreferences().add(filter);
 
 		}
 
 	}
 
 	/**
 	 * parse the property definitions of the component
 	 */
 	private void parsePropertyDefinitions(Element element, ComponentDeclaration component) {
 
 		/*
 		 *	Skip the optional enclosing list 
 		 */
 		for (Element definitions : optional(element.getElements(CoreMetadataParser.DEFINITIONS, CoreMetadataParser.APAM))) {
 			parsePropertyDefinitions(definitions, component);
 		}
 
 		for (Element definition : optional(element.getElements(CoreMetadataParser.DEFINITION, CoreMetadataParser.APAM))) {
 
 			String name = parseString(component.getName(),definition, CoreMetadataParser.ATT_NAME);
 			String type = parseString(component.getName(),definition, CoreMetadataParser.ATT_TYPE);
 			String defaultValue = parseString(component.getName(),definition, CoreMetadataParser.ATT_DEFAULT, false);
 			
 			String field 					= null;
 			String callback 				= null;
 			InjectedPropertyPolicy injected	= null;
 			
 			if ( component instanceof AtomicImplementationDeclaration) {
 				field 		= parseString(component.getName(),definition, CoreMetadataParser.ATT_FIELD, false);
 				callback 	= parseString(component.getName(),definition, CoreMetadataParser.ATT_METHOD, false);
 				injected	= parseInjectedPropertyPolicy(component.getName(),definition);
 			
 			} 
 			
 			component.getPropertyDefinitions().add(	new PropertyDefinition(component, name, type, defaultValue, field, callback, injected));
 
 		}
 	}
 	
 	/**
 	 * Parse the strategy for field synchronization (NOT compatible with deprecated internal=true attribute) 
 	 */
 	private InjectedPropertyPolicy parseInjectedPropertyPolicy(String componentName,Element element) {
 	    String value=parseString(componentName,element, CoreMetadataParser.ATT_INJECTED, false);
 	    InjectedPropertyPolicy injected;
 		injected=InjectedPropertyPolicy.getPolicy(value);
		return injected;
 	}
 
 	/**
 	 * parse the properties of the component
 	 */
 	private void parseProperties(Element element, ComponentDeclaration component) {
 
 		/*
 		 *	Skip the optional enclosing list 
 		 */
 		for (Element properties : optional(element.getElements(CoreMetadataParser.PROPERTIES, CoreMetadataParser.APAM))) {
 			parseProperties(properties, component);
 		}
 
 		for (Element property : optional(element.getElements(CoreMetadataParser.PROPERTY, CoreMetadataParser.APAM))) {
 
 			/*
 			 * If a name is specified, get the associated value
 			 */
 			String name = parseString(component.getName(),property, ATT_NAME);
 			String value = parseString(component.getName(),property, ATT_VALUE);
 			component.getProperties().put(name, value);
 
 		}
 	}
 
 	/**
 	 * Parse the definition of the state of a composite
 	 */
 	private void parseState(Element element, CompositeDeclaration composite) {
 		/*
 		 * Look for content management specification
 		 */
 		Element states[] = optional(element.getElements(CoreMetadataParser.STATE, CoreMetadataParser.APAM));
 
 		if (states.length > 1)
 			errorHandler.error(Severity.ERROR, "A single state declaration is allowed in a composite declaration"
 					+ element);
 
 		if (states.length == 0)
 			return;
 
 		Element state = states[0];
 
 		composite.setStateProperty(parsePropertyReference(composite.getName(),state, true));
 	}
 
 	/**
 	 * Parse the visibility rules for the content of a composite
 	 */
 	private void parseVisibility(Element element, CompositeDeclaration composite) {
 
 		for (Element rule : optional(element.getElements())) {
 
 			String implementationsRule = parseString(composite.getName(),rule, CoreMetadataParser.ATT_IMPLEMENTATION, false);
 			String instancesRule = parseString(composite.getName(),rule, CoreMetadataParser.ATT_INSTANCE, false);
 
 			if (rule.getName().equals(CoreMetadataParser.IMPORTS)) {
 				if (implementationsRule != null) {
 					composite.getVisibility().setBorrowImplementations(implementationsRule);
 				}
 				if (instancesRule != null) {
 					composite.getVisibility().setImportInstances(instancesRule);
 				}
 			}
 
 			//            if (rule.getName().equals(CoreMetadataParser.FRIEND)) {
 			//                if (implementationsRule != null) {
 			//                    composite.getVisibility().setFriendImplementations(implementationsRule);
 			//                }
 			//                if (instancesRule != null) {
 			//                    composite.getVisibility().setFriendInstances(instancesRule);
 			//                }
 			//            }
 
 			if (rule.getName().equals(CoreMetadataParser.EXPORT)) {
 				if (implementationsRule != null) {
 					composite.getVisibility().setExportImplementations(implementationsRule);
 				}
 
 				if (instancesRule != null) {
 					composite.getVisibility().setExportInstances(instancesRule);
 				}
 			}
 
 			if (rule.getName().equals(CoreMetadataParser.EXPORTAPP)) {
 
 				if (instancesRule != null) {
 					composite.getVisibility().setApplicationInstances(instancesRule);
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * Parse the list of promoted dependencies of a composite
 	 */
 	private void parsePromotions(Element element, CompositeDeclaration composite) {
 		for (Element promotion : optional(element.getElements(CoreMetadataParser.PROMOTE, CoreMetadataParser.APAM))) {
 
 			RelationDeclaration.Reference source = parseRelationReference(composite.getName(),promotion, true);
 			String target = parseString(composite.getName(),promotion, CoreMetadataParser.ATT_TO);
 
 			composite.getPromotions().add(
 					new RelationPromotion(source, new RelationDeclaration.Reference(composite.getReference(),
 							target)));
 		}
 	}
 
 	/**
 	 * Parse the list of owned components of a composite
 	 */
 	private void parseOwns(Element element, CompositeDeclaration composite) {
 		for (Element owned : optional(element.getElements(CoreMetadataParser.OWN, CoreMetadataParser.APAM))) {
 
 			ComponentReference<?> ownedComponentTarget = parseComponentReference(composite.getName(),owned, true);
 			String property = parseString(composite.getName(),owned, CoreMetadataParser.ATT_PROPERTY, false);
 			String values = parseString(composite.getName(),owned, CoreMetadataParser.ATT_VALUE, property != null);
 
 			OwnedComponentDeclaration ownedComponent = new OwnedComponentDeclaration(ownedComponentTarget,property,
 																new HashSet<String>(Arrays.asList(Util.split(values))));
 
 			/*
 			 * parse optional grants
 			 */
 			for (Element grant : optional(owned.getElements(CoreMetadataParser.GRANT, CoreMetadataParser.APAM))) {
 
 				ComponentReference<?> definingComponent = parseComponentReference(composite.getName(),grant, true);
 				String identifier = parseString(composite.getName(),grant, CoreMetadataParser.ATT_DEPENDENCY, false);
 				identifier = identifier != null ? identifier : ownedComponent.getComponent().getName();
 				RelationDeclaration.Reference relation = new RelationDeclaration.Reference(
 						definingComponent,
 						identifier);
 
 				String states = parseString(composite.getName(),grant, CoreMetadataParser.ATT_WHEN, true);
 
 				GrantDeclaration grantDeclaration = new GrantDeclaration(
 						relation, new HashSet<String>(Arrays
 								.asList(Util.split(states))));
 				ownedComponent.getGrants().add(grantDeclaration);
 			}
 			
 			/*
 			 * parse explicit denies
 			 */
 			for (Element deny : optional(owned.getElements(CoreMetadataParser.DENY, CoreMetadataParser.APAM))) {
 
 				ComponentReference<?> definingComponent = parseComponentReference(composite.getName(),deny, true);
 				String identifier = parseString(composite.getName(),deny, CoreMetadataParser.ATT_DEPENDENCY, false);
 				identifier = identifier != null ? identifier : ownedComponent.getComponent().getName();
 				RelationDeclaration.Reference relation = new RelationDeclaration.Reference(
 						definingComponent,
 						identifier);
 
 
 				String states = parseString(composite.getName(),deny, CoreMetadataParser.ATT_WHEN, true);
 
 				GrantDeclaration denyDeclaration = new GrantDeclaration(
 						relation, new HashSet<String>(Arrays
 								.asList(Util.split(states))));
 				ownedComponent.getDenies().add(denyDeclaration);
 			}
 			
 			
 
 			composite.getOwnedComponents().add(ownedComponent);
 		}
 	}
 
 	/**
 	 * Parse the list of owned instances of a composite
 	 */
 	private void parseOwnedInstances(Element element, CompositeDeclaration composite) {
 
 		for (Element start : optional(element.getElements(CoreMetadataParser.START, CoreMetadataParser.APAM))) {
 			composite.getInstanceDeclarations().add(parseInstance(start));
 		}
 
 	}
 
 	/**
 	 * Tests whether the specified element is an Apam declaration
 	 */
 	private static final boolean isApamDefinition(Element element) {
 		return (element.getNameSpace() != null) && CoreMetadataParser.APAM.equals(element.getNameSpace());
 
 	}
 
 	/**
 	 * Determines if this element represents an specification declaration
 	 */
 	private static final boolean isSpecification(Element element) {
 		return CoreMetadataParser.SPECIFICATION.equals(element.getName());
 	}
 
 	/**
 	 * Determines if this element represents a primitive declaration
 	 */
 	private static final boolean isPrimitiveImplementation(Element element) {
 		return CoreMetadataParser.IMPLEMENTATION.equals(element.getName());
 	}
 
 	/**
 	 * Determines if this element represents a composite declaration
 	 */
 	private static final boolean isCompositeImplementation(Element element) {
 		return CoreMetadataParser.COMPOSITE.equals(element.getName());
 	}
 
 	/**
 	 * Determines if this element represents an instance declaration
 	 */
 	private static final boolean isInstance(Element element) {
 		return CoreMetadataParser.INSTANCE.equals(element.getName())
 				|| CoreMetadataParser.INSTANCE_ALT.equals(element.getName());
 	}
 
 	/**
 	 * Get a string attribute value
 	 */
 	private final String parseString(String componentName, Element element, String attribute, boolean mandatory) {
 		String value = element.getAttribute(attribute);
 
 		if (mandatory && (value == null)) {
 			errorHandler.error(Severity.ERROR, "in component \"" + componentName + "\" attribute \"" + attribute + "\" must be specified in "
 					+ element.getName());
 			value = CoreParser.UNDEFINED;
 		}
 
 		if (mandatory && (value != null) && value.trim().isEmpty()) {
 			errorHandler.error(Severity.ERROR, "in component \"" + componentName + "\" attribute \"" + attribute + "\" cannot be empty in "
 					+ element.getName());
 			value = CoreParser.UNDEFINED;
 		}
 
 		return value;
 	}
 
 	/**
 	 * Get a mandatory string attribute value
 	 */
 	private final String parseString(String componentName,Element element, String attribute) {
 		return parseString(componentName,element, attribute, true);
 	}
 
 	/**
 	 * Get a mandatory element name
 	 */
 	private final String parseName(Element element) {
 		return parseString( element.getAttribute(CoreMetadataParser.ATT_NAME),element, CoreMetadataParser.ATT_NAME);
 	}
 
 	/**
 	 * Get a boolean attribute value
 	 */
 	private boolean parseBoolean(String componentName,Element element, String attribute, boolean mandatory, boolean defaultValue) {
 		String valueString = parseString(componentName,element, attribute, mandatory);
 		return ((valueString == null) && !mandatory) ? defaultValue : Boolean.parseBoolean(valueString);
 	}
 
 	/**
 	 * Get a boolean attribute value
 	 */
 	private boolean parseisDefinedBoolean(String componentName,Element element, String attribute) {
 		return (parseString(componentName,element, attribute, false) != null);
 	}
 
 	/**
 	 * The list of allowed values for specifying the missing policy
 	 */
 	private static final List<String> MISSING_VALUES = Arrays.asList(CoreMetadataParser.VALUE_WAIT,
 			CoreMetadataParser.VALUE_EXCEPTION,
 			CoreMetadataParser.VALUE_OPTIONAL);
 
 	/**
 	 * Get a missing policy attribute value
 	 */
 	private MissingPolicy parsePolicy(String componentName,Element element, String attribute, boolean mandatory, MissingPolicy defaultValue) {
 
 		String encodedPolicy = parseString(componentName,element, attribute, mandatory);
 
 		if ((encodedPolicy == null) && !mandatory)
 			return defaultValue;
 
 		if ((encodedPolicy == null) && mandatory)
 			return null;
 
 		if (CoreMetadataParser.VALUE_WAIT.equalsIgnoreCase(encodedPolicy))
 			return MissingPolicy.WAIT;
 
 		if (CoreMetadataParser.VALUE_OPTIONAL.equalsIgnoreCase(encodedPolicy))
 			return MissingPolicy.OPTIONAL;
 
 		if (CoreMetadataParser.VALUE_EXCEPTION.equalsIgnoreCase(encodedPolicy))
 			return MissingPolicy.EXCEPTION;
 
 		errorHandler.error(Severity.ERROR, "in component "+componentName+" invalid value for missing policy : \"" + encodedPolicy
 				+ "\",  accepted values are " + CoreMetadataParser.MISSING_VALUES.toString());
 		return null;
 	}
 
 	/**
 	 * The list of allowed values for specifying the missing policy
 	 */
 	private static final List<String> KIND_VALUES = Arrays.asList(CoreMetadataParser.VALUE_KIND_INSTANCE,
 			CoreMetadataParser.VALUE_KIND_IMPLEMENTATION,
 			CoreMetadataParser.VALUE_KIND_SPECIFICATION);
 
 	/**
 	 * Get a missing policy attribute value
 	 */
 	private ComponentKind parseKind(String componentName,Element element, String attribute, boolean mandatory, ComponentKind defaultValue) {
 
 		String encodedKind = parseString(componentName,element, attribute, mandatory);
 
 		if ((encodedKind == null) && !mandatory)
 			return defaultValue;
 
 		if ((encodedKind == null) && mandatory)
 			return null;
 
 		if (CoreMetadataParser.VALUE_KIND_INSTANCE.equalsIgnoreCase(encodedKind))
 			return ComponentKind.INSTANCE;
 
 		if (CoreMetadataParser.VALUE_KIND_IMPLEMENTATION.equalsIgnoreCase(encodedKind))
 			return ComponentKind.IMPLEMENTATION;
 
 		if (CoreMetadataParser.VALUE_KIND_SPECIFICATION.equalsIgnoreCase(encodedKind))
 			return ComponentKind.SPECIFICATION;
 
 		errorHandler.error(Severity.ERROR, "in component "+componentName+ " invalid value for component kind : \"" + encodedKind
 				+ "\",  accepted values are " + CoreMetadataParser.KIND_VALUES.toString());
 		return null;
 	}
 
 	/**
 	 * Get an specification reference coded in an attribute
 	 */
 	private SpecificationReference parseSpecificationReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String specification = parseString(inComponent,element, attribute, mandatory);
 		return ((specification == null) && !mandatory) ? null : new SpecificationReference(specification);
 	}
 
 	/**
 	 * Get an implementation reference coded in an attribute
 	 */
 	private ImplementationReference<?>
 	parseImplementationReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String implementation = parseString(inComponent,element, attribute, mandatory);
 		return ((implementation == null) && !mandatory) ? null
 				: new ImplementationReference<ImplementationDeclaration>(implementation);
 	}
 
 	/**
 	 * Get an instance reference coded in an attribute
 	 */
 	private InstanceReference parseInstanceReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String instance = parseString(inComponent,element, attribute, mandatory);
 		return ((instance == null) && !mandatory) ? null : new InstanceReference(instance);
 	}
 
 	/**
 	 * Get a generic component reference coded in an attribute
 	 */
 	private ComponentReference<?> parseAnyComponentReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String component = parseString(inComponent,element, attribute, mandatory);
 		return ((component == null) && !mandatory) ? null : new ComponentReference<ComponentDeclaration>(component);
 	}
 
 	/**
 	 * Get an interface reference coded in an attribute
 	 */
 	private InterfaceReference parseInterfaceReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String interfaceName = parseString(inComponent,element, attribute, mandatory);
 		return ((interfaceName == null) && !mandatory) ? null : new InterfaceReference(interfaceName);
 	}
 
 	/**
 	 * Get a message reference coded in an attribute
 	 */
 	private MessageReference parseMessageReference(String inComponent,Element element, String attribute, boolean mandatory) {
 		String messageName = parseString(inComponent,element, attribute, mandatory);
 		return ((messageName == null) && !mandatory) ? null : new MessageReference(messageName);
 	}
 
 	/**
 	 * Get a component reference coded in an attribute
 	 */
 	private ComponentReference<?> parseComponentReference(String inComponent,Element element, String attribute, boolean mandatory) {
 
 		String referenceKind = getReferenceKind(element, attribute);
 
 		if (CoreMetadataParser.SPECIFICATION.equals(referenceKind))
 			return parseSpecificationReference(inComponent,element, attribute, mandatory);
 
 		if (CoreMetadataParser.IMPLEMENTATION.equals(referenceKind))
 			return parseImplementationReference(inComponent,element, attribute, mandatory);
 
 		if (CoreMetadataParser.INSTANCE.equals(referenceKind))
 			return parseInstanceReference(inComponent,element, attribute, mandatory);
 
 		if (CoreMetadataParser.COMPONENT.equals(referenceKind))
 			return parseAnyComponentReference(inComponent,element, attribute, mandatory);
 
 		if (mandatory) {
 			errorHandler.error(Severity.ERROR, "component name must be specified in " + element);
 			return new ComponentReference<ComponentDeclaration>(CoreParser.UNDEFINED);
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * The list of possible kinds of component references
 	 */
 	private final static List<String> COMPONENT_REFERENCES = Arrays.asList(CoreMetadataParser.SPECIFICATION,
 			CoreMetadataParser.IMPLEMENTATION,
 			CoreMetadataParser.INSTANCE,
 			CoreMetadataParser.COMPONENT);
 
 	/**
 	 * Get a component reference implicitly coded in the element (either in a name attribute or an attribute named
 	 * after the kind of reference)
 	 */
 	private ComponentReference<?> parseComponentReference(String inComponent,Element element, boolean mandatory) {
 
 		String attribute = CoreMetadataParser.UNDEFINED;
 
 		/*
 		 * If the kind of reference is coded in the element name, the actual value must be coded in
 		 * the attribute NAME
 		 */
 		if (CoreMetadataParser.COMPONENT_REFERENCES.contains(element.getName()))
 			attribute = CoreMetadataParser.ATT_NAME;
 
 		/*
 		 * Otherwise try to find a defined attribute matching the kind of reference
 		 */
 		for (Attribute definedAttribute : element.getAttributes()) {
 
 			if (!CoreMetadataParser.COMPONENT_REFERENCES.contains(definedAttribute.getName()))
 				continue;
 
 			attribute = definedAttribute.getName();
 			break;
 		}
 
 		if (attribute.equals(CoreMetadataParser.UNDEFINED) && mandatory) {
 			errorHandler.error(Severity.ERROR, "component name must be specified in " + element.getName());
 			return new ComponentReference<ComponentDeclaration>(CoreParser.UNDEFINED);
 		}
 
 		if (attribute.equals(CoreMetadataParser.UNDEFINED) && !mandatory)
 			return null;
 
 		return parseComponentReference(inComponent,element, attribute, mandatory);
 	}
 
 	/**
 	 * Get a relation declaration reference coded in the element
 	 */
 	private RelationDeclaration.Reference parseRelationReference(String inComponent,Element element, boolean mandatory) {
 
 		ComponentReference<?> definingComponent = parseComponentReference(inComponent,element, mandatory);
 		String identifier = parseString(definingComponent.getName(),element, CoreMetadataParser.ATT_DEPENDENCY, mandatory);
 
 		if (!mandatory && (definingComponent == null || identifier == null)) {
 			return null;
 		}
 
 		return new RelationDeclaration.Reference(definingComponent, identifier);
 	}
 
 	/**
 	 * Get a property declaration reference coded in the element
 	 */
 	private PropertyDefinition.Reference parsePropertyReference(String inComponent,Element element, boolean mandatory) {
 
 		ComponentReference<?> definingComponent = parseComponentReference(inComponent,element, mandatory);
 		String identifier = parseString(definingComponent.getName(),element, CoreMetadataParser.ATT_PROPERTY, mandatory);
 
 		if (!mandatory && (definingComponent == null || identifier == null)) {
 			return null;
 		}
 
 		return new PropertyDefinition.Reference(definingComponent, identifier);
 	}
 
 	/**
 	 * Get a resource reference coded in an attribute
 	 */
 	private ResourceReference parseResourceReference(String inComponent, Element element, String attribute, boolean mandatory) {
 
 		String referenceKind = getReferenceKind(element, attribute);
 
 		if (CoreMetadataParser.INTERFACE.equals(referenceKind))
 			return parseInterfaceReference(inComponent,element, attribute, mandatory);
 
 		if (CoreMetadataParser.MESSAGE.equals(referenceKind))
 			return parseMessageReference(inComponent,element, attribute, mandatory);
 
 		if (mandatory) {
 			errorHandler.error(Severity.ERROR, "resource name must be specified in " + element.getName());
 			return new UndefinedReference( new ResourceReference(element.getName()));
 		}
 
 		return null;
 	}
 
 	/**
 	 * The list of possible kinds of component references
 	 */
 	private final static List<String> RESOURCE_REFERENCES = Arrays.asList(CoreMetadataParser.INTERFACE,
 			CoreMetadataParser.MESSAGE);
 
 	/**
 	 * Get a resolvable reference coded in an attribute
 	 */
 	private ResolvableReference parseResolvableReference(String inComponent,Element element, String attribute, boolean mandatory) {
 
 		String referenceKind = getReferenceKind(element, attribute);
 
 		if (CoreMetadataParser.COMPONENT_REFERENCES.contains(referenceKind))
 			return parseComponentReference(inComponent,element, attribute, mandatory);
 
 		if (CoreMetadataParser.RESOURCE_REFERENCES.contains(referenceKind))
 			return parseResourceReference(inComponent,element, attribute, mandatory);
 
 		if (mandatory) {
 			errorHandler.error(Severity.ERROR, "component name or resource must be specified in " + element.getName());
 			return new ComponentReference<ComponentDeclaration>(CoreParser.UNDEFINED);
 		}
 
 		return null;
 	}
 
 	/**
 	 * The list of possible kinds of references
 	 */
 	private final static List<String> ALL_REFERENCES = Arrays.asList(CoreMetadataParser.SPECIFICATION,
 			CoreMetadataParser.IMPLEMENTATION,
 			CoreMetadataParser.INSTANCE,
 			CoreMetadataParser.COMPONENT,
 			CoreMetadataParser.INTERFACE,
 			CoreMetadataParser.MESSAGE);
 
 	/**
 	 * Get a resolvable reference implicitly coded in the element (either in a name attribute or an attribute named
 	 * after the kind of reference)
 	 */
 	private ResolvableReference parseResolvableReference(String inComponent,Element element, boolean mandatory) {
 
 		String attribute = CoreMetadataParser.UNDEFINED;
 
 		/*
 		 * If the kind of reference is coded in the element name, the actual value must be coded in
 		 * the attribute NAME
 		 */
 		if (CoreMetadataParser.ALL_REFERENCES.contains(element.getName()))
 			attribute = CoreMetadataParser.ATT_NAME;
 
 		/*
 		 * Otherwise try to find a defined attribute matching the kind of reference
 		 */
 		for (Attribute definedAttribute : element.getAttributes()) {
 
 			if (!CoreMetadataParser.ALL_REFERENCES.contains(definedAttribute.getName()))
 				continue;
 
 			attribute = definedAttribute.getName();
 			break;
 		}
 
 		if (attribute.equals(CoreMetadataParser.UNDEFINED) && mandatory) {
 			errorHandler.error(Severity.ERROR, "component name or resource must be specified in " + element.getName());
 			return new ComponentReference<ComponentDeclaration>(CoreParser.UNDEFINED);
 		}
 
 		if (attribute.equals(CoreMetadataParser.UNDEFINED) && !mandatory)
 			return null;
 
 		return parseResolvableReference(inComponent,element, attribute, mandatory);
 	}
 
 	/**
 	 * Infer the kind of a reference from the specified element tag and attribute name
 	 */
 	private final String getReferenceKind(Element element, String attribute) {
 
 		if (CoreMetadataParser.ALL_REFERENCES.contains(attribute))
 			return attribute;
 
 		if (CoreMetadataParser.ALL_REFERENCES.contains(element.getName()))
 			return element.getName();
 
 		return CoreParser.UNDEFINED;
 	}
 
 	/**
 	 * Handle transparently optional elements in the metadata
 	 */
 	private final static Element[] EMPTY_ELEMENTS = new Element[0];
 
 	private Element[] optional(Element[] elements) {
 		if (elements == null)
 			return CoreMetadataParser.EMPTY_ELEMENTS;
 
 		return elements;
 
 	}
 
 	private Element[] every(Element[] ...alternatives) {
 		if (alternatives == null)
 			return CoreMetadataParser.EMPTY_ELEMENTS;
 
 		List<Element> all = new ArrayList<Element>();
 		for (Element[] elements : alternatives) {
 			if (elements != null)
 				all.addAll(Arrays.asList(elements));
 		}
 		return all.toArray(new Element[all.size()]);
 
 	}
 
 
 	/**
 	 * A utility class to obtain information about declared fields and methods.
 	 * 
 	 * It tries to use java reflection metadata if available, otherwise it fall backs
 	 * to use the iPojo metadata
 	 * 
 	 * @author vega
 	 * 
 	 */
 	private static class ImplementationReflection implements CodeReflection {
 
 		/**
 		 * The iPojo generated metadata
 		 */
 		private final PojoMetadata pojoMetadata;
 
 		/**
 		 * The name of the instrumented class
 		 */
 		private final String       className;
 
 		/**
 		 * The optional reflection information
 		 */
 		private final Class<?>     instrumentedCode;
 
 		public ImplementationReflection(String className, PojoMetadata pojoMetadata, Class<?> instrumentedCode) {
 			this.className = className;
 			this.pojoMetadata = pojoMetadata;
 			this.instrumentedCode = instrumentedCode;
 		}
 
 		@Override
 		public String getClassName() {
 			return className;
 		}
 
 		/**
 		 * Get the type of reference from the instrumented metadata of the field
 		 */
 		@Override
 		public ResourceReference getFieldType(String fieldName) throws NoSuchFieldException {
 
 			/*
 			 * Get iPojo metadata
 			 */
 			FieldMetadata fieldIPojoMetadata = null;
 			if ((pojoMetadata != null) && (pojoMetadata.getField(fieldName) != null))
 				fieldIPojoMetadata = pojoMetadata.getField(fieldName);
 
 			/*
 			 * Try to get reflection information if available,.
 			 */
 			Field fieldReflectionMetadata = null;
 			if (instrumentedCode != null) {
 				try {
 					fieldReflectionMetadata = instrumentedCode.getDeclaredField(fieldName);
 				} catch (Exception e) {
 				}
 			}
 
 
 			/*
 			 * Try to use reflection information
 			 */
 			if (fieldReflectionMetadata != null) {
 
 				/*
 				 * Verify if it is a collection
 				 */
 				String collectionType = getCollectionType(fieldReflectionMetadata);
 				if (collectionType != null)
 					return collectionType != CoreParser.UNDEFINED ? new InterfaceReference(collectionType) : new UndefinedReference(new InterfaceReference(fieldName));
 
 				/*
 				 * Verify if it is a message
 				 */
 				String messageType = getMessageType(fieldReflectionMetadata);
 				if (messageType != null)
 					return messageType != CoreParser.UNDEFINED ? new MessageReference(messageType) : new UndefinedReference(new MessageReference(fieldName));
 
 				/*
 				 * Otherwise we consider it as an interface
 				 */
 				return new InterfaceReference(fieldReflectionMetadata.getType().getCanonicalName());
 
 			}
 
 			/*
 			 *  Try to use iPojo metadata, less precise specially for generics
 			 */
 			if (fieldIPojoMetadata != null) {
 				
 				/*
 				 * Verify if it is a collection
 				 */
 				String collectionType = getCollectionType(fieldIPojoMetadata);
 				if (collectionType != null)
 					return collectionType != CoreParser.UNDEFINED ? new InterfaceReference(collectionType) : new UndefinedReference(new InterfaceReference(fieldName));
 							
 				/*
 				 * Verify if it is a message
 				 */
 				String messageType = getMessageType(fieldIPojoMetadata);
 				if (messageType != null)
 					return messageType != CoreParser.UNDEFINED ? new MessageReference(messageType) : new UndefinedReference(new MessageReference(fieldName));
 
 				/*
 				 * Otherwise we consider it as an interface
 				 */
 				return new InterfaceReference(fieldIPojoMetadata.getFieldType());
 			}
 
 			throw new NoSuchFieldException("unavailable field " + fieldName);
 
 		}
 
         /**
          * Get the cardinality of the field from the instrumented metadata
          */
 		@Override
 		public boolean isCollectionField(String fieldName) throws NoSuchFieldException {
 
 			/*
 			 * Try to get reflection information if available,.
 			 */
 			Field fieldReflectionMetadata = null;
 			if (instrumentedCode != null) {
 				try {
 					fieldReflectionMetadata = instrumentedCode.getDeclaredField(fieldName);
 				} catch (Exception ignored) {
 				}
 			}
 
 			/*
 			 * Get iPojo metadata
 			 */
 			FieldMetadata fieldIPojoMetadata = null;
 			if ((pojoMetadata != null) && (pojoMetadata.getField(fieldName) != null))
 				fieldIPojoMetadata = pojoMetadata.getField(fieldName);
 
 			if (fieldReflectionMetadata != null)
 				return getCollectionType(fieldReflectionMetadata) != null;
 
 			if (fieldIPojoMetadata != null)
 				return getCollectionType(fieldIPojoMetadata) != null;
 
 			throw new NoSuchFieldException("unavailable metadata for field " + fieldName);
 
 		}
 		
 		@Override
 		public String getMethodReturnType(String methodName, String methodSignature, boolean includeInherited) throws NoSuchMethodException {
 				
 			MethodMetadata methodIPojoMetadata = null;
 			if (pojoMetadata != null) {
 				for (MethodMetadata method :  pojoMetadata.getMethods(methodName)) {
 					
 					if (methodSignature == null) {
 						methodIPojoMetadata = method;
 						break;
 					}
 					
 					String signature[]	= Util.split(methodSignature);
 					String arguments[]	= method.getMethodArguments();
 					boolean match 		= (signature.length == arguments.length);
 
 					for (int i = 0; match && i < signature.length; i++) {
 						if (!signature[i].equals(arguments[i]))
 							match = false;
 					}
 					
 					if (match) {
 						methodIPojoMetadata = method;
 						break;
 					}
 				}
 			}
 			
 			Method methodReflectionMetadata = null;
 			if (instrumentedCode != null) {
 				for (Method method :  includeInherited ? instrumentedCode.getMethods() : instrumentedCode.getDeclaredMethods()) {
 					
 					if (!method.getName().equals(methodName))
 						continue;
 					
 					if (methodSignature == null) {
 						methodReflectionMetadata = method;
 						break;
 					}
 					
 					String signature[]		= Util.split(methodSignature);
 					Class<?> parameters[]	= method.getParameterTypes();
 					boolean match 			= (signature.length == parameters.length);
 
 					for (int i = 0; match && i < signature.length; i++) {
 						if (! FieldMetadata.getReflectionType(signature[i]).equals(parameters[i].getName()))
 							match = false;
 					}
 					
 					if (match) {
 						methodReflectionMetadata = method;
 						break;
 					}
 				}
 			}
 			
 			if (methodReflectionMetadata != null)
 				return wrap(methodReflectionMetadata.getReturnType().getCanonicalName());
 
 			if (methodIPojoMetadata != null)
 				return wrap(methodIPojoMetadata.getMethodReturn());
 
 			throw new NoSuchMethodException("unavailable metadata for method " + methodName+"("+methodSignature != null ? methodSignature : ""+")");
 			
 		}
 
 		@Override
 		public int getMethodParameterNumber(String methodName, boolean includeInherited) throws NoSuchMethodException {
 			
 			if (pojoMetadata != null) {
 				for (MethodMetadata method :  pojoMetadata.getMethods(methodName)) {
 					return method.getMethodArguments().length;
 				}
 			}
 			
 			if (instrumentedCode != null) {
 				for (Method method :  includeInherited ? instrumentedCode.getMethods() : instrumentedCode.getDeclaredMethods()) {
 					
 					if (!method.getName().equals(methodName))
 						continue;
 					
 					return method.getParameterTypes().length;
 				}
 			}
 			
 			throw new NoSuchMethodException("unavailable metadata for method " + methodName);
 		}
 		
 		@Override
 		public String[] getMethodParameterTypes(String methodName, boolean includeInherited) throws NoSuchMethodException {
 
 			List<String> signature = new ArrayList<String>();
 
 			if (pojoMetadata != null) {
 				for (MethodMetadata method : pojoMetadata.getMethods(methodName)) {
 					
 					for(String argument : method.getMethodArguments()){
 						signature.add(wrap(argument));
 					}
 					
 					return signature.toArray(new String[0]);
 				}
 			}
 
 			if (instrumentedCode != null) {
 				for (Method method : includeInherited ? instrumentedCode.getMethods() : instrumentedCode.getDeclaredMethods()) {
 
 					if (!method.getName().equals(methodName))
 						continue;
 
 					
 					for(Class<?> parameterType : method.getParameterTypes()){
 						signature.add(wrap(parameterType.getCanonicalName()));
 					}
 					
 					return signature.toArray(new String[0]);
 				}
 			}
 
 			throw new NoSuchMethodException("unavailable metadata for method "
 					+ methodName);
 		}
 			
 		@Override
 		public String getMethodParameterType(String methodName, boolean includeInherited) throws NoSuchMethodException {
 			
 			MethodMetadata methodIPojoMetadata = null;
 			if (pojoMetadata != null) {
 				for (MethodMetadata method :  pojoMetadata.getMethods(methodName)) {
 					
 					String arguments[]	= method.getMethodArguments();
 					boolean match 		= (1 == arguments.length);
 					if (match)
 						methodIPojoMetadata = method;
 				}
 			}
 			
 			Method methodReflectionMetadata = null;
 			if (instrumentedCode != null) {
 				for (Method method :  includeInherited ? instrumentedCode.getMethods() : instrumentedCode.getDeclaredMethods()) {
 					
 					if (!method.getName().equals(methodName))
 						continue;
 					
 					Class<?> parameters[]	= method.getParameterTypes();
 					boolean match 			= (1 == parameters.length);
 
 					if (match)
 						methodReflectionMetadata = method;
 				}
 			}
 			
 			if (methodReflectionMetadata != null)
 				return wrap(methodReflectionMetadata.getParameterTypes()[0].getCanonicalName());
 
 			if (methodIPojoMetadata != null)
 				return wrap(methodIPojoMetadata.getMethodArguments()[0]);
 
 			throw new NoSuchMethodException("unavailable metadata for method " + methodName);
 			
 		}
 
 		
 		/**
 		 * The list of supported collections for aggregate dependencies
 		 */
 		private final static Class<?>[] supportedCollections = new Class<?>[] {
 											Collection.class,
 											List.class,
 											Vector.class,
 											Set.class
 										};
 
 		/**
 		 * The list of supported types for push message queues
 		 */
 		private final static Class<?>[] supportedMessageQueues = new Class<?>[] {
 											Queue.class,
 										};
 
 		/**
 		 * Utility method to get the raw class of a possibly parameterized type
 		 */
 		private static final Class<?> getRawClass(Type type) {
 			
 			if (type instanceof Class)
 				return (Class<?>) type;
 			
 			if (type instanceof ParameterizedType)
 				return (Class<?>) ((ParameterizedType)type).getRawType();
 			
 			return null;
 		}
 		
 		/**
 		 * Utility method to get the single type argument of a parameterized type
 		 */
 		private static final Class<?> getSingleTypeArgument(Type type) {
 
 			if (! (type instanceof ParameterizedType))
 				return null;
 			
 			ParameterizedType parameterizedType = (ParameterizedType) type;
 			Type[] arguments = parameterizedType.getActualTypeArguments();
 			
 			if ((arguments.length == 1) && (arguments[0] instanceof Class))
 				return (Class<?>) arguments[0];
 			else
 				return null;
 		}
 
 		/**
 		 * Utility method to get the associated wrapper class name for a primitive type
 		 */
 
 		private final static Map<String,Class<?>> wrappers = new HashMap<String, Class<?>>();
 		static {
 			wrappers.put(Boolean.TYPE.getName(), Boolean.class);
 			wrappers.put(Character.TYPE.getName(), Character.class);
 			wrappers.put(Byte.TYPE.getName(), Byte.class);
 			wrappers.put(Short.TYPE.getName(), Short.class);
 			wrappers.put(Integer.TYPE.getName(), Integer.class);
 			wrappers.put(Float.TYPE.getName(), Float.class);
 			wrappers.put(Long.TYPE.getName(), Long.class);
 			wrappers.put(Double.TYPE.getName(), Double.class);
 		}
 		
 		private static String wrap(String type) {
 			Class<?> wrapper = wrappers.get(type);
 			return wrapper != null ? wrapper.getCanonicalName() : type;
 		}
 		
 		/**
 		 * If the type of the specified field is one of the supported collections returns the type of the
 		 * elements in the collection, otherwise return null.
 		 * 
 		 * May return {@link CoreParser#UNDEFINED} if field is defined as a collection but the type of the
 		 * elements in the collection cannot be determined.
 		 */
 		private static String getCollectionType(Field field) {
 
 			Type fieldType 		= field.getGenericType();
 			Class<?> fieldClass = getRawClass(fieldType);
 			
 			if (fieldClass == null)
 				return null;
 
 			/*
 			 * First try to see if the field is an array declaration
 			 */
 			if (fieldType instanceof Class && fieldClass.isArray()) {
 				return wrap(fieldClass.getComponentType().getCanonicalName());
 			}
 
 			if (fieldType instanceof GenericArrayType) {
 				Type elementType = ((GenericArrayType)fieldType).getGenericComponentType();
 				if (elementType instanceof Class)
 					return ((Class<?>) elementType).getCanonicalName();
 				else
 					return CoreParser.UNDEFINED;
 			}
 
 			/*
 			 * Verify if the class of the field is one of the supported collections and get
 			 * the element type
 			 */
 
 			for (Class<?> supportedCollection : supportedCollections) {
 				if (supportedCollection.equals(fieldClass)) {
 					Class<?> element = getSingleTypeArgument(fieldType);
 					return element != null ? wrap(element.getCanonicalName()) : CoreParser.UNDEFINED;
 				}
 			}
 
 			/*
 			 * If it is not an array or one of the supported collections just return null
 			 */
 			return null;
 
 		}
 
 		/**
 		 * If the type of the specified field is one of the supported collections returns the type of the
 		 * elements in the collection, otherwise return null.
 		 * 
 		 * May return {@link CoreParser#UNDEFINED} if field is defined as a collection but the type of the
 		 * elements in the collection cannot be determined.
 		 */
 		private static String getCollectionType(FieldMetadata field) {
 			String fieldType = field.getFieldType();
 
 			if (fieldType.endsWith("[]")) {
 				int index = fieldType.indexOf('[');
 				return wrap(fieldType.substring(0, index));
 			}
 
 			for (Class<?> supportedCollection : supportedCollections) {
 				if (supportedCollection.getCanonicalName().equals(fieldType)) {
 					return CoreParser.UNDEFINED;
 				}
 			}
 
 			return null;
 		}
 
 		/**
 		 * If the type of the specified field is one of the supported message queues returns
 		 * the type of the message data, otherwise return null.
 		 * 
 		 * May return {@link CoreParser#UNDEFINED} if the type of the data in the queue cannot
 		 * be determined.
 		 */
 		private static String getMessageType(Field field) {
 			Type fieldType 		= field.getGenericType();
 			Class<?> fieldClass = getRawClass(fieldType);
 			
 			if (fieldClass == null)
 				return null;
 
 			/*
 			 * Verify if the class of the field is one of the supported message queues and get
 			 * the element type
 			 */
 			for (Class<?> supportedMessageQueue : supportedMessageQueues) {
 				if (supportedMessageQueue.equals(fieldClass)) {
 					Class<?> element = getSingleTypeArgument(fieldType);
 					return element != null ? wrap(element.getCanonicalName()) : CoreParser.UNDEFINED;
 				}
 			}
 
 			/*
 			 * If it is not one of the supported message queues just return null
 			 */
 			return null;
 
 		}
 
 		private static String getMessageType(FieldMetadata field) {
 			String fieldType = field.getFieldType();
 
 			for (Class<?> supportedMessage : supportedMessageQueues) {
 				if (supportedMessage.getCanonicalName().equals(fieldType)) {
 					return CoreParser.UNDEFINED;
 				}
 			}
 
 			return null;
 		}
 
 
 	}
 
 }
