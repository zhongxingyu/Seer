 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics.aop.config;
 
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.RuntimeBeanReference;
 import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.beans.factory.xml.ResourceEntityResolver;
 import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
 import org.springframework.core.Conventions;
 import org.springframework.core.io.DefaultResourceLoader;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * Base class to assist in building Spring XML extensions.  Out of the box, Spring's Extensible XML Authoring
  * support is powerful, but requires a lot of parser coding, and a lot of exposure to the sometimes
  * arcane BeanDefinition API.  <code>TemplateBeanDefinitionParser</code> allows subclasses to read
  * complex BeanDefinitions from an embedded Spring XML configuration file and then modify them according
  * to runtime configuration values.  This is often much more concise than manually constructing BeanDefinitions 
  * from scratch.
  * 
  * <p>Subclasses specify a classpath resource containing the template XML bean definitions in the constructor.
  * This is just a standard Spring XML application context configuration file.
  * Subclasses should then override {@link #transform(ConfigurableBeanFactory, BeanDefinition, Element, ParserContext)}
  * to transform the template bean definition according to runtime values.</p>
  * 
  * <p>We also make use of {@link ChainingBeanFactoryPostProcessor} so that factory post-processing
  * operations carry over into the bean factory containing template definitions.  This allows
  * our custom tags' attributes to be subject to property replacement using PropertyPlaceholderConfigurer,
  * for example.</p>
  */
 public class TemplateBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
 
 	private String templateResource;
 	private String templateId;
 
 	/**
 	 * @param templateResource qualified classpath resource containing the template XML configuration
 	 * @param templateId bean name to fetch out of the template XML configuration
 	 */
 	public TemplateBeanDefinitionParser(String templateResource, String templateId) {
 		this.templateResource = templateResource;
 		this.templateId = templateId;
 	}
 
 	@Override
 	protected Class<?> getBeanClass(Element element) {
 		return TemplateBeanDefinitionFactory.class;
 	}
 
 	/**
 	 * <p>Load the template BeanDefinition and call {@link #transform(ConfigurableListableBeanFactory, BeanDefinition, Element, ParserContext)}
 	 * to apply runtime configuration value to it.  <code>builder</code> will be configured to instantiate the bean
 	 * in the Spring context that we are parsing.</p>
 	 * 
 	 * <p>During parsing, an instance of {@link TemplateComponentDefinition} is pushed onto <code>ParserContext</code> so
 	 * that nested tags can access the enclosing template configuration with a call to {@link #findEnclosingTemplateFactory(ParserContext)}.
 	 * Subclasses can override {@link #newComponentDefinition(String, Object, DefaultListableBeanFactory)} to provide a 
 	 * subclass of {@link TemplateComponentDefinition} to the parser context if necessary.</p>
 	 */
 	@Override
 	protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
 
 		//if we have multiple nested bean definitions, we only parse the template factory
 		//once.  this allows configuration changes made by enclosing bean parsers to be inherited
 		//by contained beans, which is quite useful.
 		DefaultListableBeanFactory templateFactory = findEnclosingTemplateFactory(parserContext);
 		TemplateComponentDefinition tcd = null;
 		if (templateFactory == null) {
 			
 			//no nesting -- load the template XML configuration from the classpath.
			final BeanFactory parentFactory = (BeanFactory)parserContext.getRegistry();
 			templateFactory = new DefaultListableBeanFactory(parentFactory);
 			
 			//load template bean definitions
 			DefaultResourceLoader loader = new DefaultResourceLoader();
 			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(templateFactory);
 			reader.setResourceLoader(loader);
 			reader.setEntityResolver(new ResourceEntityResolver(loader));
 			reader.loadBeanDefinitions(templateResource);
 
 			//propagate factory post-processors from the source factory into the template
 			//factory.
 			BeanDefinition ppChain = new RootBeanDefinition(ChainingBeanFactoryPostProcessor.class);
 			ppChain.getPropertyValues().addPropertyValue("targetFactory", templateFactory);
 			parserContext.getReaderContext().registerWithGeneratedName(ppChain);
 			
 			//push component definition onto the parser stack for the benefit of
 			//nested bean definitions.
 			tcd = newComponentDefinition(element.getNodeName(), parserContext.extractSource(element), templateFactory);
 			parserContext.pushContainingComponent(tcd);
 		}
 
 		try {
 			//allow subclasses to apply overrides to the template bean definition.
 			BeanDefinition def = templateFactory.getBeanDefinition(templateId);
 			transform(templateFactory, def, element, parserContext);
 
 			//setup our factory bean to instantiate the modified bean definition upon request.
 			builder.addPropertyValue("beanFactory", templateFactory);
 			builder.addPropertyValue("beanName", templateId);
 			builder.getRawBeanDefinition().setAttribute("id", def.getAttribute("id"));
 
 		} finally {
 			if (tcd != null)
 				parserContext.popContainingComponent();
 		}
 	}
 
 	/**
 	 * Hook by which subclasses can modify template configuration values.  Default behavior does nothing to the template.
 	 * 
 	 * @param template the template bean definition
 	 * @param factory the bean factory from which <code>template</code> was loaded
 	 * @param element XML configuration fragment containing overrides that should be applied to the template
 	 * @param parserContext XML parse context supplying the configuration values
 	 */
 	protected void transform(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
 	}
 
 	/** create the component definition that will be pushed onto the parser context in {@link #doParse(Element, ParserContext, BeanDefinitionBuilder)}. */
 	protected TemplateComponentDefinition newComponentDefinition(String name, Object source, DefaultListableBeanFactory factory) {
 		return new TemplateComponentDefinition(name, source, factory);
 	}
 	
 	/** 
 	 * Overridden to prefer the <code>id</code> attribute of <code>definition</code> if it is defined, over whatever
 	 * is in <code>element</code> (which would be the superclass behavior).  This allows subclasses to specify an ID
 	 * value in {@link #transform(ConfigurableListableBeanFactory, BeanDefinition, Element, ParserContext)} if required. 
 	 */
 	@Override
 	protected String resolveId(Element element,
 			AbstractBeanDefinition definition, ParserContext parserContext)
 			throws BeanDefinitionStoreException {
 		String id = (String)definition.getAttribute("id");
 		return id == null ? super.resolveId(element, definition, parserContext) : id;
 	}
 
 	/** 
 	 * returns true to prevent parse errors if an ID is not specified via the usual means, since we allow subclasses
 	 * to generate bean IDs.  See {@link #resolveId(Element, AbstractBeanDefinition, ParserContext)}. 
 	 */
 	@Override
 	protected boolean shouldGenerateIdAsFallback() {
 		return true;
 	}
 	
 	/**
 	 * Convenience method to update a template bean definition from overriding XML data.  
 	 * If <code>overrides</code> contains attribute <code>attribute</code>, transfer that
 	 * attribute onto <code>template</code>, overwriting the default value.
 	 */
 	public static String overrideAttribute(String attribute, BeanDefinition template, Element overrides) {
 		String value = (String)template.getAttribute(attribute);
 		if (overrides.hasAttribute(attribute)) {
 			value = overrides.getAttribute(attribute);
 			template.setAttribute(attribute, value);
 		}
 		return value;
 	}
 	
 	/**
 	 * Convenience method to update a template bean definition from overriding XML data.  
 	 * If <code>overrides</code> contains attribute <code>attribute</code> or a child element
 	 * with name <code>attribute</code>, transfer that
 	 * attribute as a bean property onto <code>template</code>, overwriting the default value.
 	 * @param reference if true, the value of the attribute is to be interpreted as a runtime bean name reference; otherwise it is interpreted as a literal value
 	 */
 	public static boolean overrideProperty(String attribute, BeanDefinition template, Element overrides, boolean reference) {
 		Object value = null;
 		if (overrides.hasAttribute(attribute)) {
 			value = overrides.getAttribute(attribute);
 		} else {
 			NodeList children = overrides.getElementsByTagNameNS("*", attribute);
 			if (children.getLength() == 1) {
 				Element child = (Element)children.item(0);
 				value = child.getTextContent();
 			}
 		}
 		
 		if (value != null) {
 			if (reference)
 				value = new RuntimeBeanReference(value.toString());
 			
 			String propName = Conventions.attributeNameToPropertyName(attribute);
 			MutablePropertyValues props = template.getPropertyValues();
 			props.removePropertyValue(propName);
 			props.addPropertyValue(propName, value);
 			return true;
 		}
 		
 		return false;
 	}
 
 	/** 
 	 * If the given parse operation is nested inside an instance of {@link TemplateComponentDefinition}, return
 	 * the template bean configuration associated with that component.  Otherwise return null.
 	 */
 	public static DefaultListableBeanFactory findEnclosingTemplateFactory(ParserContext context) {
 		if (context.isNested()) {
 			//TODO: support deeper nesting.  this logic breaks completely with bt:persister-chain.
 			CompositeComponentDefinition parent = context.getContainingComponent();
 			if (parent instanceof TemplateComponentDefinition)
 				return ((TemplateComponentDefinition)parent).getTemplateFactory();
 		}
 		return null;
 	}
 	
 	/** 
 	 * A component definition providing access to the template bean configuration, for the benefit
 	 * of nested configuration tags.
 	 */
 	public static class TemplateComponentDefinition extends CompositeComponentDefinition {
 
 		private DefaultListableBeanFactory templateFactory;
 		
 		public TemplateComponentDefinition(String name, Object source, DefaultListableBeanFactory factory) {
 			super(name, source);
 			this.templateFactory = factory;
 		}
 
 		public DefaultListableBeanFactory getTemplateFactory() {
 			return templateFactory;
 		}
 	}
 }
