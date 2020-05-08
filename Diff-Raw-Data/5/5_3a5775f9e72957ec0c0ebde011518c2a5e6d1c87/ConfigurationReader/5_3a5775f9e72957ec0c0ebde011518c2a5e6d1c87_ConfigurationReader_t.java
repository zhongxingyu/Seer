 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.ebayopensource.turmeric.tools.annoparser.commons.FactoryTypes;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.context.OutputGenaratorParam;
 import org.ebayopensource.turmeric.tools.annoparser.exception.AnnotationParserException;
 import org.ebayopensource.turmeric.tools.annoparser.exception.ConfigurationException;
 import org.ebayopensource.turmeric.tools.annoparser.outputgenerator.OutputGenerator;
 import org.ebayopensource.turmeric.tools.annoparser.parser.AnnotationParser;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This Class reads the Configurations XML's and creates the context.
  *
  * @author sdaripelli
  */
 public class ConfigurationReader {
 	
 	static {
 		Logger.getLogger(ConfigurationReader.class.getName()).setLevel(Level.SEVERE);
 	}
 	
 	/**
 	 * Load configurations.
 	 *
 	 * @param configFile the config file
 	 */
 	public static void loadConfigurations(String configFile){
 			try {
 				loadConfigurations(convertToURL(configFile).openStream());
 			} catch (IOException e) {
 				Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE, "Configuration File does not exist", e);
 				throw new ConfigurationException("Failed to load the configuration file. Configuration File does not exist. Cause for the exception: " + e.getMessage(), e);
 			}
 	}
 	
 	
 	
 	/**
 	 * Convert to url.
 	 * The path supplied is first tried as a URL external form string, if it fails it is tried as a Class path resource,
 	 * Then it is tried as a local file path.
 	 * 
 	 * @param path
 	 *            the path 
 	 * @return the uRL
 	 */
 	public static URL convertToURL(String path) {
 		URL url = null;
 		try {
 			url = new URL(path);
 		} catch (MalformedURLException e) {
 			url = Thread.currentThread().getContextClassLoader().getResource(
 					path);
 			if (url == null) {
 				File file = new File(path);
 				if (file.exists()) {
 					try {
 						url = file.toURI().toURL();
 					} catch (MalformedURLException e1) {
 						Logger.getLogger(ConfigurationReader.class.getName())
 								.log(Level.SEVERE, path + " is Not valid", e);
 						throw new ConfigurationException(
 								"Failed to convert the " + path + "to URL/URI."
 										+ path + " is Not valid", e);
 					}
 				}
 			}
 		}
		if(url == null){
			throw new ConfigurationException(
					"Failed to convert the " + path + "to URL/URI."
							+ path + " is Not valid");
		}
 		return url;
 	}
 	
 	/**
 	 * Load configurations from the file path specified.
 	 *
 	 * @param config the config
 	 */
 	public static void loadConfigurations(InputStream config){
 
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering loadConfigurations method in ConfigurationReader", config);
 		try {
 			//System.out.println("2" + config);
 			Document document=getDocument(config);
 			if(document==null){
 				Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Configuration File is not valid");
 				throw new ConfigurationException("Failed to load the Configuration. Configuration File is not valid");
 			}
 			Context context=Context.getContext();
 			NodeList nodeList=document.getElementsByTagName("outputcss");
 			Node node=nodeList.item(0);
 			if(node!=null){
 				String css=node.getTextContent();
 				context.setCssFilePath(css);
 			}
 			
 			populateAnnotationParsers(document, context);
 			populateOutputGenerators(document, context);
 			nodeList=document.getElementsByTagName("outputdir");
 			node=nodeList.item(0);
 			if(node!=null){
 				String outputdir=node.getTextContent();
 				context.setOutputDir(outputdir);
 			}
 			populateDocuments(document, context);
 			populateFactoryClasses(document, context);
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting loadConfigurations method in ConfigurationReader", new Object[]{document,context});
 		} catch (SAXException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Configuration File not valid",e);
 			StringBuffer sbf = new StringBuffer("Failed to load Configuration file. Configuration File not valid. Cause for the failure: " + e.getMessage());
 			throw new ConfigurationException(sbf.toString(),e);
 		} catch (IOException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Configuration File does not exist",e);
 			StringBuffer sbf = new StringBuffer("Failed to load Configuration file. Configuration File does not exist. Cause for the failure: " + e.getMessage());
 			throw new ConfigurationException(sbf.toString(),e);
 		}
 	}
 	
 	
 	
 	/**
 	 * Load default configuration. 
 	 */
 	public static void loadDefaultConfiguration(){
 		InputStream is =ConfigurationReader.class.getClassLoader().getResourceAsStream("Configuration.xml");
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering loadConfigurations method in ConfigurationReader",  is);
 		loadConfigurations(is);
 	}
 
 	/**
 	 * Populate annotation parsers.
 	 * 
 	 * @param document
 	 *            the document
 	 * @param context
 	 *            the context
 	 */
 	private static void populateAnnotationParsers(Document document, Context context) {
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering populateAnnotationParsers method in ConfigurationReader", new Object[]{document,context});
 		try {
 		NodeList nodeList=document.getElementsByTagName("defaultparser");
 		Node node=nodeList.item(0);
 		if(node!=null){
 			String defaultParser=node.getTextContent();
 			
 			if(defaultParser!=null){
 				defaultParser=defaultParser.trim();
 				if(!Utils.isEmpty(defaultParser)){
 					Class<? extends AnnotationParser> clazz = Thread.currentThread().getContextClassLoader().loadClass(defaultParser.trim()).asSubclass(AnnotationParser.class);
 					context.setDefaultAnnotationParser(clazz.newInstance());
 				}
 			}
 		}
 		
 		nodeList=document.getElementsByTagName("annoparsers");
 		node = (org.w3c.dom.Element) nodeList.item(0);
 		if(node!=null){
 			for (Node childNode = node.getFirstChild(); childNode != null;) {
 				
 				Node nextChild = childNode.getNextSibling();
 				if(childNode.getNodeType()==Node.ELEMENT_NODE){
 					String tagName=getTagValue((org.w3c.dom.Element)childNode, "tagname");
 					String parserClass=getTagValue((org.w3c.dom.Element)childNode, "parserclass");
 					if(tagName!=null && parserClass!=null){
 							Class<? extends AnnotationParser> clazz = Thread.currentThread().getContextClassLoader().loadClass(parserClass.trim()).asSubclass(AnnotationParser.class);
 							context.addParser(tagName, clazz.newInstance());
 					}
 				}				
 				childNode = nextChild;
 			}
 		}
 		} catch (ClassNotFoundException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE, "Custom Annotation Parser Class  Not Found.", e);
 			throw new AnnotationParserException("Failed to populate the Annotation Parser Class. Custom Annotation Parser Class  Not Found.",e);
 		} catch (IllegalAccessException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Could not access Custom Annotation Parser Class",e);
 			throw new AnnotationParserException("Failed to populate the Annotation Parser Class. Could not access Custom Annotation Parser Class ",e);
 		} catch (InstantiationException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Could not instantiate Custom Annotation Parser Class ",e);
 		}
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting populateAnnotationParsers method in ConfigurationReader", new Object[]{document,context});
 	}
 	
 	
 	
 	private static void populateFactoryClasses(Document document, Context context) {
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering populateFactoryClasses method in ConfigurationReader", new Object[]{document,context});
 		try {
 		NodeList nodeList=document.getElementsByTagName("factoryclasses");
 		Node node = (org.w3c.dom.Element) nodeList.item(0);
 		if(node!=null){
 			for (Node childNode = node.getFirstChild(); childNode != null;) {
 				
 				Node nextChild = childNode.getNextSibling();
 				if(childNode.getNodeType()==Node.ELEMENT_NODE){
 					String tagName=getTagValue((org.w3c.dom.Element)childNode, "type");
 					String parserClass=getTagValue((org.w3c.dom.Element)childNode, "class");
 					if(tagName!=null && parserClass!=null){
 							Class clazz = Thread.currentThread().getContextClassLoader().loadClass(parserClass.trim());
 							context.addFactoryClass(FactoryTypes.valueOf(tagName), clazz) ;
 					}
 				}				
 				childNode = nextChild;
 			}
 		}
 		} catch (ClassNotFoundException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE, "Custom Annotation Parser Class  Not Found.", e);
 			throw new AnnotationParserException("Failed to populate the Factory Class. Class  Not Found.",e);
 		} 
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting populateFactoryClasses method in ConfigurationReader", new Object[]{document,context});
 	}
 	/**
 	 * Populate documents.
 	 * 
 	 * @param document
 	 *            the document
 	 * @param context
 	 *            the context
 	 */
 	private static void populateDocuments(Document document, Context context) {
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering populateDocuments in ConfigurationReader", new Object[]{document,context});
 		NodeList nodeList=document.getElementsByTagName("documents");
 		Node node = (org.w3c.dom.Element) nodeList.item(0);
 		if(node!=null){
 			for (Node childNode = node.getFirstChild(); childNode != null;) {
 				
 				Node nextChild = childNode.getNextSibling();
 				if(childNode.getNodeType()==Node.ELEMENT_NODE){
 					String documentPath=childNode.getTextContent();
 					context.addDocument(documentPath);
 					
 				}				
 				childNode = nextChild;
 			}
 		}
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting populateDocuments in ConfigurationReader", new Object[]{document,context});
 	}
 	
 	/**
 	 * Populate output generators.
 	 * 
 	 * @param document
 	 *            the document
 	 * @param context
 	 *            the context
 	 */
 	private static void populateOutputGenerators(Document document, Context context) {
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering populateOutputGenerators in ConfigurationReader", new Object[]{document,context});
 		NodeList nodeList=document.getElementsByTagName("outputgenerators");
 		Node node = (org.w3c.dom.Element) nodeList.item(0);
 		if(node!=null){
 			for (Node childNode = node.getFirstChild(); childNode != null;) {
 				Node nextChild = childNode.getNextSibling();
 				if(childNode.getNodeType()==Node.ELEMENT_NODE){
 				String opgenName=getTagValue((org.w3c.dom.Element)childNode, "class");
 				String disabled="false";
 				if(childNode.getAttributes()!=null && childNode.getAttributes().getNamedItem("disabled")!=null){
 					disabled=childNode.getAttributes().getNamedItem("disabled").getNodeValue();
 				}
 				String name=null;
 				if(childNode.getAttributes()!=null && childNode.getAttributes().getNamedItem("name")!=null){
 					name=childNode.getAttributes().getNamedItem("name").getNodeValue();
 				}
 				if("true".equals(disabled)){
 					if(context.getOutputGenerators()!=null){
 						context.getOutputGenerators().remove(name);
 					}
 				}else if(opgenName!=null){
 					try {
 						OutputGenaratorParam outputGenaratorParam=new OutputGenaratorParam();
 						Class<? extends OutputGenerator> clazz = Thread.currentThread().getContextClassLoader().loadClass(opgenName.trim()).asSubclass(OutputGenerator.class);
 						OutputGenerator generator= clazz.newInstance();
 						outputGenaratorParam.setOutputGenerator(generator);
 						outputGenaratorParam.setName(name);
 						outputGenaratorParam.setOutputDir(getTagValue((org.w3c.dom.Element)childNode, "outputdir"));
 					    outputGenaratorParam.setParameters(getOutputParameters(childNode));
 						context.addOutputGenerator(outputGenaratorParam);
 					} catch (ClassNotFoundException e) {
 						Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Custom Output generator Class "+opgenName+" Not Found.",e);
 						throw new AnnotationParserException("Failed to populate the Output Generator Class. Custom Output generator Class "+opgenName+" Not Found.",e);
 					} catch (IllegalAccessException e) {
 						Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Could not access Output generator Class "+opgenName,e);
 						throw new AnnotationParserException("Failed to populate the Output Generator Class. Could not access Output generator Class "+opgenName,e);
 					} catch (InstantiationException e) {
 						Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Could not instantiate Output generator Class "+opgenName,e);
 						throw new AnnotationParserException("Failed to populate the Output Generator Class. Could not instantiate Output generator Class "+opgenName,e);
 					}
 				}
 				}
 				childNode = nextChild;
 			}
 		}
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting populateOutputGenerators in ConfigurationReader", new Object[]{document,context});
 	}
 
 	/**
 	 * Gets the output parameters.
 	 *
 	 * @param node the node
 	 * @return the output parameters
 	 */
 	private static Map<String, String> getOutputParameters(Node node) {
 		Map<String, String> retMap=null;
 		org.w3c.dom.Element element=(org.w3c.dom.Element)node;
 		NodeList nodeList=element.getElementsByTagName("parameters");
 		if(nodeList!=null){
 			retMap=new HashMap<String,String>();
 			org.w3c.dom.Element parametersElem = (org.w3c.dom.Element) nodeList.item(0);
 			if(parametersElem!=null){
 			NodeList parameters= parametersElem.getElementsByTagName("parameter");
 			if(parameters!=null){
 				for(int i=0;i<parameters.getLength();i++){
 					org.w3c.dom.Element parameter = (org.w3c.dom.Element) parameters.item(i);
 					retMap.put(getTagValue(parameter, "name"), getTagValue(parameter, "value"));
 				}
 			}
 			}
 		}
 		return retMap;
 	}
 
 
 
 	/**
 	 * Gets the tag value.
 	 * 
 	 * @param childNode
 	 *            the child node
 	 * @param tag
 	 *            the tag
 	 * @return the tag value
 	 */
 	private static String getTagValue(org.w3c.dom.Element childNode, String tag) {
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering getTagValue in ConfigurationReader", new Object[]{childNode,tag});
 		String tagName=null;
 		NodeList nodeList=childNode.getElementsByTagName(tag);
 		Node node=(org.w3c.dom.Element) nodeList.item(0);
 		if(node!=null){
 			tagName=node.getTextContent();
 		}
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting getTagValue in ConfigurationReader", tag);
 		return tagName;
 		
 	}
 	
 	/**
 	 * Gets the document.
 	 *
 	 * @param config the config
 	 * @return the document
 	 * @throws SAXException the sAX exception
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	private static Document getDocument(InputStream config) throws SAXException, IOException {		
 		Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Entering getDocument in ConfigurationReader", new Object[]{config});
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder builder = dbf.newDocumentBuilder();
 			Document doc = builder.parse(config);
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.FINER, "Exiting getDocument in ConfigurationReader", new Object[]{doc});
 			return doc;
 		} catch (ParserConfigurationException e) {
 			Logger.getLogger(ConfigurationReader.class.getName()).log(Level.SEVERE,"Configuration File not correct", e);
 			throw new ConfigurationException("Failed to load the Configuration File. Configuration File is not valid. Cause for the Exception is : " + e.getMessage(),e);
 		}
 	}
  }
