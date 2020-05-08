 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.internal.view.model.jsp.analyzer;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.internal.proxy.core.IBeanProxy;
 import org.eclipse.jem.internal.proxy.core.IBeanTypeProxy;
 import org.eclipse.jem.internal.proxy.core.IConfigurationContributor;
 import org.eclipse.jem.internal.proxy.core.IStandardBeanTypeProxyFactory;
 import org.eclipse.jem.internal.proxy.core.ProxyFactoryRegistry;
 import org.eclipse.jem.internal.proxy.ide.IDERegistration;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValidatorTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IJSFTagElement;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IHandlerTagElement.TagHandlerType;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IJSFTagElement.TagType;
 import org.eclipse.jst.jsf.core.JSFVersion;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.JSFCoreTraceOptions;
 import org.eclipse.jst.jsf.core.internal.jem.BeanProxyUtil.BeanProxyWrapper;
 import org.eclipse.jst.jsf.core.internal.jem.BeanProxyUtil.ProxyException;
 import org.eclipse.jst.jsf.designtime.internal.view.DTComponentIntrospector;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDComponentTagElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDConverterTagElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDTagElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDTagHandlerElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDValidatorTagElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.IAttributeAdvisor.NullAttributeAdvisor;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDElementDeclaration;
 
 /**
  * Utility class supporting methods to derive information from JSP tag
  * definitions (TLD)
  * 
  * @author cbateman
  * 
  */
 public final class TagAnalyzer
 {
     private static final String JAVAX_FACES_WEBAPP_CONVERTER_TAG = "javax.faces.webapp.ConverterTag"; //$NON-NLS-1$
     private static final String JAVAX_FACES_WEBAPP_CONVERTER_ELTAG = "javax.faces.webapp.ConverterELTag"; //$NON-NLS-1$
     private static final String JAVAX_FACES_WEBAPP_VALIDATOR_TAG = "javax.faces.webapp.ValidatorTag"; //$NON-NLS-1$
     private static final String JAVAX_FACES_WEBAPP_VALIDATOR_ELTAG = "javax.faces.webapp.ValidatorELTag"; //$NON-NLS-1$
     private static final String JAVAX_FACES_WEBAPP_FACET_TAG = "javax.faces.webapp.FacetTag"; //$NON-NLS-1$
 
     // private static final String JAVAX_FACES_WEBAPP_ACTIONLISTENER_TAG =
     // "javax.faces.webapp.ActionListenerTag";
     // private static final String JAVAX_FACES_WEBAPP_VALUECHANGELISTENER_TAG =
     // "javax.faces.webapp.ValueChangeListenerTag";
     private static final String JAVAX_FACES_WEBAPP_ATTRIBUTE_TAG = "javax.faces.webapp.AttributeTag"; //$NON-NLS-1$
 
     private final static Set<String> COMPONENT_TAG_HANDLER_TYPES_JSF11;
     private final static Set<String> COMPONENT_TAG_HANDLER_TYPES_JSF12;
 
     private final static Set<String> CONVERTER_TAG_HANDLER_TYPES_JSF11 = Collections
             .singleton(JAVAX_FACES_WEBAPP_CONVERTER_TAG);
     private final static Set<String> CONVERTER_TAG_HANDLER_TYPES_JSF12;
 
     private final static Set<String> VALIDATOR_TAG_HANDLER_TYPES_JSF11 = Collections
             .singleton(JAVAX_FACES_WEBAPP_VALIDATOR_TAG);
     private final static Set<String> VALIDATOR_TAG_HANDLER_TYPES_JSF12;
 
     private final static Set<String> FACET_TAG_HANDLER = Collections
             .singleton(JAVAX_FACES_WEBAPP_FACET_TAG);
     private final static Set<String> ATTRIBUTE_TAG_HANDLER = Collections
             .singleton(JAVAX_FACES_WEBAPP_ATTRIBUTE_TAG);
     // private final static Set<String> ACTIONLISTENER_TAG_HANDLER = Collections
     // .singleton(JAVAX_FACES_WEBAPP_ACTIONLISTENER_TAG);
     // private final static Set<String> VALUECHANGELISTENER_TAG_HANDLER =
     // Collections
     // .singleton(JAVAX_FACES_WEBAPP_VALUECHANGELISTENER_TAG);
 
     private final static Set<String> ALL_HANDLER_TAGS;
 
     static
     {
         // components
         // JSF 1.1
         Set<String> set = new HashSet<String>(8);
         set.add("javax.faces.webapp.UIComponentTag"); //$NON-NLS-1$
         set.add("javax.faces.webapp.UIComponentBodyTag"); //$NON-NLS-1$
         COMPONENT_TAG_HANDLER_TYPES_JSF11 = Collections.unmodifiableSet(set);
 
         // JSF 1.2+
         set = new HashSet<String>(8);
         set.addAll(COMPONENT_TAG_HANDLER_TYPES_JSF11);
         set.add("javax.faces.webapp.UIComponentELTag"); //$NON-NLS-1$
         COMPONENT_TAG_HANDLER_TYPES_JSF12 = Collections.unmodifiableSet(set);
 
         // converters
         set = new HashSet<String>(8);
         set.add(JAVAX_FACES_WEBAPP_CONVERTER_TAG);
         set.add(JAVAX_FACES_WEBAPP_CONVERTER_ELTAG);
         CONVERTER_TAG_HANDLER_TYPES_JSF12 = Collections.unmodifiableSet(set);
 
         // validators
         set = new HashSet<String>(8);
         set.add(JAVAX_FACES_WEBAPP_VALIDATOR_TAG);
         set.add(JAVAX_FACES_WEBAPP_VALIDATOR_ELTAG);
         VALIDATOR_TAG_HANDLER_TYPES_JSF12 = Collections.unmodifiableSet(set);
 
         // tag handlers
         ALL_HANDLER_TAGS = new HashSet<String>();
         ALL_HANDLER_TAGS.addAll(FACET_TAG_HANDLER);
         ALL_HANDLER_TAGS.addAll(ATTRIBUTE_TAG_HANDLER);
         // ALL_HANDLER_TAGS.addAll(VALUECHANGELISTENER_TAG_HANDLER);
         // ALL_HANDLER_TAGS.addAll(ACTIONLISTENER_TAG_HANDLER);
     }
 
     /**
      * Tries to determine the component type of the component that corresponds
      * to the JSP tag defined by tldDecl.
      * 
      * @param tldDecl
      * @param project
      * @return get the component type from the tag definition or null if none.
      */
     public static String findComponentType(final TLDElementDeclaration tldDecl,
             final IProject project)
     {
         final String className = tldDecl.getTagclass();
 
         final IConfigurationContributor[] contributor = new IConfigurationContributor[]
         { new ServletBeanProxyContributor(project) };
 
         ProxyFactoryRegistry registry = null;
         try
         {
             registry = getProxyFactoryRegistry(contributor, project);
     
             if (registry != null)
             {
 
                 final IStandardBeanTypeProxyFactory factory = registry
                         .getBeanTypeProxyFactory();
                 final IBeanTypeProxy classTypeProxy = factory
                         .getBeanTypeProxy(className);
                 final BeanProxyWrapper classTypeWrapper = new BeanProxyWrapper(project,
                         classTypeProxy);
     
                 try
                 {
                     classTypeWrapper.init();
                     return classTypeWrapper.callStringMethod("getComponentType"); //$NON-NLS-1$
                 }
                 catch (final ProxyException tp)
                 {
                     if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
                     {
                         JSFCoreTraceOptions.log("TagAnalyzer.findComponentType", tp); //$NON-NLS-1$
                     }
                 }
             }
         }
         finally
         {
             if (registry != null)
             {
                 registry.terminateRegistry(true);
             }
         }
         return null;
     }
 
     /**
      * Tries to introspect tldDecl's tag class to determine what the identifier
      * of the underlying Converter that is used to register it in the app config
      * 
      * @param tldDecl
      * @param project
      * @return the converter type identifier for the tldDecl or null if not
      *         found
      */
     public static String findConverterType(final TLDElementDeclaration tldDecl,
             final IProject project)
     {
         // TODO: allow for pluggable resolver?
 
         // This one is more hacky and less prone to success than the component
         // type finder because the ConverterTag doesn't initialize its type
         // field on construction. Instead, both MyFaces and RI seem to do it
         // based on the doStartTag method. They also don't provide a standard
         // interface for acquiring the id so instead we make some guess on
         // the internal field name.
         ProxyFactoryRegistry registry = null;
         try
         {
             final String className = tldDecl.getTagclass();
     
             final IConfigurationContributor[] contributor = new IConfigurationContributor[]
             { new ServletBeanProxyContributor(project) };
             registry = getProxyFactoryRegistry(
                     contributor, project);
     
             if (registry != null)
             {
                 final IStandardBeanTypeProxyFactory factory = registry
                         .getBeanTypeProxyFactory();
                 final IBeanTypeProxy classTypeProxy = factory
                         .getBeanTypeProxy(className);
     
                 try
                 {
                     final IType type = JavaCore.create(project).findType(className);
     
                     if (type != null
                             && DTComponentIntrospector
                                     .isTypeNameInstanceOfClass(
                                             type,
                                             Collections
                                                     .singleton(JAVAX_FACES_WEBAPP_CONVERTER_ELTAG)))
                     {
                         return findConverterType_InELTag(factory, classTypeProxy,project);
                     }
                     // the assumption is that this is a component tag, so we assume
                     // it is one.
                     else if (type != null
                             && DTComponentIntrospector
                                     .isTypeNameInstanceOfClass(
                                             type,
                                             Collections
                                                     .singleton(JAVAX_FACES_WEBAPP_CONVERTER_TAG)))
                     {
                         return findConverterType_InTag(factory, classTypeProxy, project);
                     }
                 }
                 catch (final JavaModelException jme)
                 {
                     // suppress for now
                 }
             }
         }
         finally
         {
             if (registry != null)
             {
                 registry.terminateRegistry(true);
             }
         }
         return null;
     }
 
     private static String findConverterType_InTag(
             final IStandardBeanTypeProxyFactory factory,
             final IBeanTypeProxy classTypeProxy, final IProject project)
     {
         final IBeanTypeProxy nullPageContextType = factory
                 .getBeanTypeProxy("javax.servlet.jsp.PageContext"); //$NON-NLS-1$
         final BeanProxyWrapper classTypeWrapper = new BeanProxyWrapper(project,
                 classTypeProxy);
 
         try
         {
             classTypeWrapper.init();
 
             callSuppressExceptions(classTypeWrapper, "setPageContext", //$NON-NLS-1$
                     new IBeanProxy[]
                     { null }, new IBeanTypeProxy[]
                     { nullPageContextType });
             callSuppressExceptions(classTypeWrapper, "doStartTag"); //$NON-NLS-1$
 
             final IBeanTypeProxy converterProxy = factory
                     .getBeanTypeProxy(JAVAX_FACES_WEBAPP_CONVERTER_TAG);
 
             // hopefully doStartTag set the converter field before it
             // failed.
             // now try to guess what it's called
             String converterId = getStringField(classTypeWrapper,
                     converterProxy, "converterId"); //$NON-NLS-1$
 
             if (converterId != null)
             {
                 return converterId;
             }
 
             converterId = getStringField(classTypeWrapper, converterProxy,
                     "_converterId"); //$NON-NLS-1$
 
             if (converterId != null)
             {
                 return converterId;
             }
 
             // no? look for a CONVERTER_ID
             converterId = getStringField(classTypeWrapper, classTypeProxy,
                     "CONVERTER_ID"); //$NON-NLS-1$
         }
         catch (final ProxyException tp)
         {
             // fall through
         }
         return null;
     }
 
     private static String findConverterType_InELTag(
             final IStandardBeanTypeProxyFactory factory,
             final IBeanTypeProxy classTypeProxy, final IProject project)
     {
         final BeanProxyWrapper classTypeWrapper = new BeanProxyWrapper(project,
                 classTypeProxy);
         final IBeanTypeProxy elExpressionType = factory
                 .getBeanTypeProxy("javax.el.ValueExpression"); //$NON-NLS-1$
 
         if (elExpressionType == null)
         {
             return null;
         }
 
         try
         {
             classTypeWrapper.init();
 
             callSuppressExceptions(classTypeWrapper, "doStartTag"); //$NON-NLS-1$
 
             // no? look for a CONVERTER_ID
             final IBeanProxy converterId = getFieldInParents(classTypeWrapper,
                     classTypeProxy, "CONVERTER_ID_EXPR"); //$NON-NLS-1$
 
             if (converterId != null)
             {
                 converterId.getTypeProxy().isKindOf(elExpressionType);
                 final BeanProxyWrapper elExprValue = new BeanProxyWrapper(project,
                         converterId.getTypeProxy());
                 final String value = elExprValue
                         .callStringMethod("getExpressionString"); //$NON-NLS-1$
                 System.out.println("Expression string:" + value); //$NON-NLS-1$
             }
 
             //            
             //
             // final IBeanTypeProxy converterProxy = factory
             // .getBeanTypeProxy(JAVAX_FACES_WEBAPP_CONVERTER_TAG);
 
         }
         catch (final ProxyException tp)
         {
             // fall through
         }
         return null;
     }
 
     /**
      * @param tldDecl
      * @param project
      * @return the validator type identifier for the tldDecl or null if not
      *         found
      */
     public static String findValidatorType(final TLDElementDeclaration tldDecl,
             final IProject project)
     {
         // TODO: allow for pluggable resolver?
 
         // This one is more hacky and less prone to success than the component
         // type finder because the Validator doesn't initialize its type
         // field on construction. Instead, both MyFaces and RI seem to do it
         // based on the doStartTag method. They also don't provide a standard
         // interface for acquiring the id so instead we make some guess on
         // the internal field name.
         final String className = tldDecl.getTagclass();
 
         final IConfigurationContributor[] contributor = new IConfigurationContributor[]
         { new ServletBeanProxyContributor(project) };
 
         ProxyFactoryRegistry registry = null;
 
         try
         {
             registry = getProxyFactoryRegistry(contributor, project);
     
             if (registry != null)
             {
                 final IStandardBeanTypeProxyFactory factory = registry
                         .getBeanTypeProxyFactory();
                 final IBeanTypeProxy classTypeProxy = factory
                         .getBeanTypeProxy(className);
                 final BeanProxyWrapper classTypeWrapper = new BeanProxyWrapper(project,
                         classTypeProxy);
                 final IBeanTypeProxy converterProxy = factory
                         .getBeanTypeProxy(JAVAX_FACES_WEBAPP_VALIDATOR_TAG);
                 try
                 {
                     classTypeWrapper.init();
     
                     callSuppressExceptions(classTypeWrapper, "doStartTag"); //$NON-NLS-1$
                     callSuppressExceptions(classTypeWrapper, "createValidator"); //$NON-NLS-1$
     
                     // hopefully doStartTag set the converter field before it
                     // failed.
                     // now try to guess what it's called
                     String validatorId = getStringField(classTypeWrapper,
                             converterProxy, "validatorId"); //$NON-NLS-1$
     
                     if (validatorId != null)
                     {
                         return validatorId;
                     }
     
                     validatorId = getStringField(classTypeWrapper, converterProxy,
                             "_validatorId"); //$NON-NLS-1$
     
                     if (validatorId != null)
                     {
                         return validatorId;
                     }
     
                     // no? then see if there's a VALIDATOR_ID field *on the tag*
                     validatorId = getStringField(classTypeWrapper, classTypeProxy,
                             "VALIDATOR_ID"); //$NON-NLS-1$
     
                     if (validatorId != null)
                     {
                         return validatorId;
                     }
                 }
                 catch (final ProxyException tp)
                 {
                     // fall through
                 }
             }
         }
         finally
         {
             if (registry != null)
             {
                 registry.terminateRegistry(true);
             }
         }
         return null;
 
     }
 
     private static IBeanProxy getFieldInParents(
             final BeanProxyWrapper classTypeWrapper,
             final IBeanTypeProxy typeProxy, final String fieldName)
     {
         try
         {
             return classTypeWrapper.getFieldValueIncludeParents(fieldName,
                     typeProxy);
         }
         catch (final ProxyException e)
         {
             // suppress
         }
 
         return null;
     }
 
     private static String getStringField(
             final BeanProxyWrapper classTypeWrapper,
             final IBeanTypeProxy typeProxy, final String fieldName)
     {
         try
         {
             return classTypeWrapper.getStringFieldValue(fieldName, typeProxy);
         }
         catch (final ProxyException e)
         {
             // suppress
         }
 
         return null;
     }
 
     private static void callSuppressExceptions(
             final BeanProxyWrapper classTypeWrapper, final String methodName)
     {
         try
         {
             classTypeWrapper.call(methodName);
         }
         catch (final ProxyException tp)
         {
             // suppress this since it may well throw an exception
             // depending on what assumptions the tag class is making
             // that won't be true because we're not in a servlet
         }
     }
 
     private static void callSuppressExceptions(
             final BeanProxyWrapper classTypeWrapper, final String methodName,
             final IBeanProxy[] args, final IBeanTypeProxy[] argTypes)
     {
         try
         {
             classTypeWrapper.call(methodName, args, argTypes);
         }
         catch (final ProxyException tp)
         {
             // suppress this since it may well throw an exception
             // depending on what assumptions the tag class is making
             // that won't be true because we're not in a servlet
         }
 
     }
 
     /**
      * @param project
      * @return a new proxy factory registry or null;
      * 
      * TODO: caching?
      */
     private static ProxyFactoryRegistry getProxyFactoryRegistry(
             final IConfigurationContributor[] configuration,
             final IProject project)
     {
         try
         {
             return IDERegistration.startAnImplementation(configuration, false,
                     project, project.getName(), JSFCorePlugin.PLUGIN_ID,
                     new NullProgressMonitor());
         }
         catch (final CoreException e)
         {
             JSFCorePlugin.log("Error starting vm for project: " //$NON-NLS-1$
                     + project.getName(), e);
         }
 
         return null;
     }
 
     /**
      * @param tldDecl
      * @param project
      * @return the tag element for the tldDecl
      */
     public static TLDTagElement createTLDTagElement(
             final TLDElementDeclaration tldDecl, final IProject project)
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
         {
             JSFCoreTraceOptions
                     .log(String
                             .format(
                                     "TagAnalyzer.createTLDTagElement: Start tld=%s, project=%s", //$NON-NLS-1$
                                     tldDecl.getNodeName(), project.getName()));
         }
         long startTime = 0;
         if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR_PERF)
         {
             startTime = System.nanoTime();
         }
 
         try
         {
             final IJavaProject javaProject = JavaCore.create(project);
             final String typeName = tldDecl.getTagclass();
 
             if (typeName == null
                     || JavaConventions.validateJavaTypeName(typeName,
                             JavaCore.VERSION_1_3,
                             JavaCore.VERSION_1_3).getSeverity() == IStatus.ERROR)
             {
            	if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
                {
                    JSFCoreTraceOptions.log(
                        "Bad tag class name in " + tldDecl.toString()); //$NON-NLS-1$
                }
                 return null;
             }
 
             final IType type = javaProject.findType(typeName);
             
             if (type == null)
             {
                 if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
                 {
                     JSFCoreTraceOptions.log("Type not found for: "+typeName); //$NON-NLS-1$
                 }
                 return null;
             }
             final TagType tagType = getJSFComponentTagType(type, project);
 
             if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
             {
                 JSFCoreTraceOptions.log(String.format(
                         "Tag class type=%s\nTag type=%s", type, tagType)); //$NON-NLS-1$
             }
 
             if (tagType == TagType.COMPONENT)
             {
                 final TLDTagElement element = createComponentTagElement(tldDecl, project);
                 
                 if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR_PERF)
                 {
                     String name = element != null ? element.toString()
                             : "<none>"; //$NON-NLS-1$
                     System.out.printf(
                             "Time to create component tag element %s was %d\n", //$NON-NLS-1$
                             name, Long.valueOf(System.nanoTime() - startTime));
                 }
                 return element;
             }
             else if (tagType == TagType.CONVERTER)
             {
                 final TLDTagElement element =  createConverterTagElement(tldDecl, project);
                 if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR_PERF)
                 {
                     String name = element != null ? element.toString()
                             : "<none>"; //$NON-NLS-1$
                     System.out.printf(
                             "Time to create converter tag element %s was %d\n", //$NON-NLS-1$
                             name, Long.valueOf(System.nanoTime() - startTime));
                 }
                 return element;
             }
             else if (tagType == TagType.VALIDATOR)
             {
                 final TLDTagElement element =  createValidatorTagElement(tldDecl, project);
                 if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR_PERF)
                 {
                     String name = element != null ? element.toString()
                             : "<none>"; //$NON-NLS-1$
                     System.out.printf(
                             "Time to create validator tag element %s was %d\n", //$NON-NLS-1$
                             name, Long.valueOf(System.nanoTime() - startTime));
                 }
                 return element;
             }
             else if (tagType == TagType.HANDLER)
             {
                 final TLDTagElement element =  createHandlerTagElement(tldDecl, type);
                 if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR_PERF)
                 {
                     String name = element != null ? element.toString()
                             : "<none>"; //$NON-NLS-1$
                     System.out.printf(
                             "Time to create handler tag element %s was %d\n", //$NON-NLS-1$
                             name, Long.valueOf(System.nanoTime() - startTime));
                 }
                 return element;
             }
         }
         catch (final JavaModelException jme)
         {
             JSFCorePlugin.log(jme, "Trying to get type for TLD"); //$NON-NLS-1$
         }
 
         return null;
     }
 
     /**
      * @param tldDecl
      * @param project
      * @return a component tag element for tldDecl
      */
     public static TLDTagElement createComponentTagElement(
             final TLDElementDeclaration tldDecl, final IProject project)
     {
         final String componentType = findComponentType(tldDecl, project);
 
         if (componentType != null)
         {
             final String componentClass = DTComponentIntrospector
                     .findComponentClass(componentType, project);
 
             if (componentClass != null && !"".equals(componentClass.trim())) //$NON-NLS-1$
             {
                 final ComponentTypeInfo typeInfo = DTComponentIntrospector
                         .getComponent(componentType, componentClass, project,
                                 new IConfigurationContributor[]
                                 {new ServletBeanProxyContributor(project)});
 
                 if (typeInfo != null)
                 {
                     final TLDComponentTagElement tagElement = new TLDComponentTagElement(
                             tldDecl, typeInfo, new NullAttributeAdvisor());
                     return tagElement;
                 }
             }
         }
 
         // TODO: really return null?
         return null;
     }
 
     /**
      * @param tldDecl
      * @param project
      * @return a converter tag element for tldDecl
      */
     public static TLDTagElement createConverterTagElement(
             final TLDElementDeclaration tldDecl, final IProject project)
     {
         final String converterId = findConverterType(tldDecl, project);
 
         if (converterId != null)
         {
             final String converterClass = DTComponentIntrospector
                     .findConverterClass(converterId, project);
 
             if (converterClass != null && !"".equals(converterClass.trim())) //$NON-NLS-1$
             {
                 final ConverterTypeInfo typeInfo = DTComponentIntrospector
                         .getConverter(converterId, converterClass);
                 final TLDConverterTagElement tagElement = new TLDConverterTagElement(
                         tldDecl, typeInfo, new NullAttributeAdvisor());
                 return tagElement;
             }
         }
 
         // we know (actually we assume by contract) that this a converter
         // so create an unknown converter tag for it
         return new TLDConverterTagElement(tldDecl, ConverterTypeInfo.UNKNOWN, new NullAttributeAdvisor());
     }
 
     /**
      * @param tldDecl
      * @param project
      * @return a validator tag element for the tldDec
      */
     public static TLDTagElement createValidatorTagElement(
             final TLDElementDeclaration tldDecl, final IProject project)
     {
         final String validatorId = findValidatorType(tldDecl, project);
 
         if (validatorId != null)
         {
             final String validatorClass = DTComponentIntrospector
                     .findValidatorClass(validatorId, project);
 
             if (validatorClass != null && !"".equals(validatorClass.trim())) //$NON-NLS-1$
             {
                 final ValidatorTypeInfo typeInfo = DTComponentIntrospector
                         .getValidator(validatorId, validatorClass);
                 final TLDValidatorTagElement tagElement = new TLDValidatorTagElement(
                         tldDecl, typeInfo, new NullAttributeAdvisor());
                 return tagElement;
             }
         }
         // we know (actually we assume by contract) that this a validator
         // so create an unknown converter tag for it
         return new TLDValidatorTagElement(tldDecl, ValidatorTypeInfo.UNKNOWN, new NullAttributeAdvisor());
     }
 
     /**
      * @param tldDecl
      * @param type
      * @return the tag element for a JSF handler
      */
     public static TLDTagElement createHandlerTagElement(
             final TLDElementDeclaration tldDecl, final IType type)
     {
         if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 FACET_TAG_HANDLER))
         {
             return new TLDTagHandlerElement(tldDecl, TagHandlerType.FACET, new NullAttributeAdvisor());
         }
         else if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 ATTRIBUTE_TAG_HANDLER))
         {
             return new TLDTagHandlerElement(tldDecl, TagHandlerType.ATTRIBUTE, new NullAttributeAdvisor());
         }
         // else if (isTypeNameInstanceOfClass(type, ACTIONLISTENER_TAG_HANDLER))
         // {
         // return new TLDTagHandlerElement(tldDecl,
         // TagHandlerType.ACTIONLISTENER);
         // }
         // else if (isTypeNameInstanceOfClass(type,
         // VALUECHANGELISTENER_TAG_HANDLER))
         // {
         // return new TLDTagHandlerElement(tldDecl,
         // TagHandlerType.VALUECHANGELISTENER);
         // }
         return null;
     }
 
     /**
      * @param type
      * @param project
      * @return true if type represents a JSF component action class (spec 9.2.2)
      */
     public static IJSFTagElement.TagType getJSFComponentTagType(
             final IType type, final IProject project)
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
         {
             JSFCoreTraceOptions
                     .log(String
                             .format(
                                     "TagAnalyzer.getJSFComponentTagType: Determining Tag Type for type %s on project %s", //$NON-NLS-1$
                                     type.getFullyQualifiedName(), project
                                             .toString()));
         }
 
         Set<String> componentTags = null;
         Set<String> converterTags = null;
         Set<String> validatorTags = null;
 
         final JSFVersion jsfVersion = ServletBeanProxyContributor.getProjectVersion(project);
 
         if (jsfVersion == null){
         	return null;
         }
         // v1.1(9.2.2): JSF component tags must sub-class one of these
         else if (jsfVersion == JSFVersion.V1_0 || jsfVersion == JSFVersion.V1_1)
         {
             componentTags = COMPONENT_TAG_HANDLER_TYPES_JSF11;
             converterTags = CONVERTER_TAG_HANDLER_TYPES_JSF11;
             validatorTags = VALIDATOR_TAG_HANDLER_TYPES_JSF11;
         }
         // v1.2(9.2.2): JSF component tags must sub-class UIComponentELTag
         // the original two are included because we must be backward
         // compatible
         else if (jsfVersion == JSFVersion.V1_2 || jsfVersion == JSFVersion.V2_0)
         {
             componentTags = COMPONENT_TAG_HANDLER_TYPES_JSF12;
             converterTags = CONVERTER_TAG_HANDLER_TYPES_JSF12;
             validatorTags = VALIDATOR_TAG_HANDLER_TYPES_JSF12;
         }
         else 
         {
             throw new IllegalArgumentException();
         }
 
         TagType tagType = null;
 
         if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 componentTags))
         {
             tagType = TagType.COMPONENT;
         }
         else if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 converterTags))
         {
             tagType = TagType.CONVERTER;
         }
         else if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 validatorTags))
         {
             tagType = TagType.VALIDATOR;
         }
         else if (DTComponentIntrospector.isTypeNameInstanceOfClass(type,
                 ALL_HANDLER_TAGS))
         {
             tagType = TagType.HANDLER;
         }
 
         if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
         {
             JSFCoreTraceOptions.log(String.format(
                     "TagAnalyzer.getJSFComponentTagType: tag type is %s", //$NON-NLS-1$
                     tagType != null ? tagType.toString() : "null")); //$NON-NLS-1$
         }
         return tagType;
     }
 
 
 }
