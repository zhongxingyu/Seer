 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.outputgenerator.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface;
 import org.ebayopensource.turmeric.tools.annoparser.WSDLDocument;
 import org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface;
 import org.ebayopensource.turmeric.tools.annoparser.commons.AnnotationsHelper;
 import org.ebayopensource.turmeric.tools.annoparser.commons.Constants;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.context.OutputGenaratorParam;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.AbstractType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Attribute;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ComplexType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Element;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.EnumElement;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.OperationHolder;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationInfo;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationTag;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.PortType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.SimpleType;
 import org.ebayopensource.turmeric.tools.annoparser.exception.OutputFormatterException;
 import org.ebayopensource.turmeric.tools.annoparser.outputgenerator.OutputGenerator;
 import org.ebayopensource.turmeric.tools.annoparser.utils.HtmlUtils;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Node;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 
 /**
  * The Class JavaDocOutputGenerator generated javadoc Output for the WSDL
  * Document Supplied.
  * 
  * @author sdaripelli
  */
 public class JavaDocOutputGenerator implements OutputGenerator {
 
 	private static final String TYPES = "/types/";
 	/** The current types folder path. */
 	private String currentTypesFolderPath = null;
 	
 	private String classUseFolderPath = null;
 	
 	private final static String CLASS_NAME = JavaDocOutputGenerator.class
 			.getClass().getName();
 	Logger logger = Logger.getLogger(CLASS_NAME);
 
 	private OutputGenaratorParam outputGenaratorParam;
 
 	private Map<String, List<String>> packageServicesMap = new HashMap<String, List<String>>();
 	private Map<String, List<XSDDocInterface>> packageDocMap = new HashMap<String, List<XSDDocInterface>>();
 	private List<AbstractType> processedTypes = new ArrayList<AbstractType>();
 
 	private static final String SEPARATOR = "/";
 
 	private String currentPackageName;
 	
 	private Map<String, TreeSet<String>> returnTypeToMethodMap = new HashMap<String, TreeSet<String>>();
 	
 	private Map<String, TreeSet<String>> paramsToMethodMap = new HashMap<String, TreeSet<String>>();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ebayopensource.turmeric.tools.annoparser.outputformatter.DocHandler
 	 * #handleWsdlDoc
 	 * (org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface,
 	 * java.lang.String)
 	 */
 
 	public void generateWsdlOutput(WSDLDocInterface wsdlDoc,
 			OutputGenaratorParam outputGenaratorParam)
 			throws OutputFormatterException {
 		logger.entering("JavaDocOutputGenerator", "handleWsdlDoc",
 				new Object[] { wsdlDoc, outputGenaratorParam });
 		String packageName = wsdlDoc.getPackageName();
 		this.outputGenaratorParam = outputGenaratorParam;
 		if (packageName == null) {
 			packageName = "DefaultDomain";
 		}
 		if (packageName.startsWith("/")) {
 			packageName = packageName.substring(1);
 		}
 		currentPackageName = packageName;
 		currentTypesFolderPath = getCurrentOutputDir() + File.separator
 				+ packageName + File.separator + "types" + File.separator;
 
 		classUseFolderPath = getCurrentOutputDir() + File.separator
 				+ packageName + File.separator + "types-use" + File.separator;
 		
 		writeCssFiles();
 		logger.logp(Level.INFO, "JavaDocOutputGenerator", "handleWsdlDoc",
 				"Types Folder Path");
 		List<PortType> portTypes = wsdlDoc.getPortTypes();
 		for (PortType portType : portTypes) {
 			StringBuffer html = new StringBuffer();
 			html.append(HtmlUtils.getStartTags(wsdlDoc.getServiceName(),
 					packageName));
 			Map<String, String> replacementMap=new HashMap<String, String>();
 			replacementMap.put("REL_PATH", getRelativePath(currentPackageName));
 			buildHeader(html,replacementMap,"operationHeader");
 
 			html.append(Constants.HTML_BR);
 			buildPortType(html, portType, wsdlDoc);
 			html.append(Constants.HTML_BR);
 			buildOperationTable(html, portType, wsdlDoc);
 			html.append(Constants.HTML_BR);
 			buildOperationDetails(html, portType, wsdlDoc);
 			
 			addFooter(html,replacementMap,"operationHeader");
 
 			html.append(HtmlUtils.getEndTags());
 			String outputDir = getCurrentOutputDir() + File.separator;
 			writeFile(html, outputDir + File.separator + packageName, wsdlDoc
 					.getServiceName()
 					+ Constants.DOT_HTML);
 		}
 		writeOrphanTypes(wsdlDoc);
 		createTypeIndex(wsdlDoc);
 		addPackageToDocMap(packageName, wsdlDoc);
 		addPackageToServiceMap(packageName, wsdlDoc.getServiceName());
 		processedTypes = new ArrayList<AbstractType>();
 		logger.exiting("JavaDocOutputGenerator", "handleWsdlDoc", new Object[] {
 				wsdlDoc, outputGenaratorParam });
 	}
 
 	private void writeOrphanTypes(WSDLDocInterface wsdlDoc)
 			throws OutputFormatterException {
 		for (ComplexType cType : wsdlDoc.getAllComplexTypes()) {
 			if (!processedTypes.contains(cType)) {
 				writeComplexTypeFile(wsdlDoc, cType, cType.getName());
 			}
 		}
 		for (SimpleType cType : wsdlDoc.getAllSimpleTypes()) {
 			if (!processedTypes.contains(cType)) {
 				writeSimpleTypeFile(wsdlDoc, cType);
 			}
 		}
 
 	}
 
 	private void writePackageTree(List<XSDDocInterface> wsdlDoc, String outputdir, boolean isAllPackages)
 			throws OutputFormatterException {
 		Node root = getTypesInTree(wsdlDoc);
 		
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags("Hierarchy For Service domain "
 				+ currentPackageName, outputdir));
 		Map<String, String> replacementMap=new HashMap<String, String>();
 		replacementMap.put("REL_PATH", (outputdir==null?".":getRelativePath(outputdir)));
 		replacementMap.put("TREE_REL_PATH", ".");
 		
 		buildHeader(html, replacementMap,"typesUseHeader");
 		html.append(Constants.HTML_H3_START + "Hierarchy For Service domain "
 				+ currentPackageName + Constants.HTML_H3_END);
 		if (!isAllPackages) {
 			html.append("<b>Package Hierarchies:</b>" + Constants.HTML_BR);
 			String str = getRelativePath(outputdir);
 			html.append(Constants.NBSP_THRICE + Constants.NBSP_THRICE + HtmlUtils.getAnchorTag(null, str + "Tree" + Constants.DOT_HTML, null, "All Packages"));
 		}
 		
 		html.append(Constants.HTML_HR);
 		html.append(Constants.HTML_H3_START + "Complex Type Hierarchy"
 				+ Constants.HTML_H3_END);
 		html.append("<ul>");
 		writeTree(root, html, isAllPackages);
 		html.append("</ul>");
 		addFooter(html, replacementMap,"typesUseHeader");
 		html.append(HtmlUtils.getEndTags());
 		if(outputdir==null){
 			outputdir="";
 		}
 		writeFile(html, getCurrentOutputDir()+outputdir, "Tree" + Constants.DOT_HTML);
 	}
 
 	private void writeTree(Node root, StringBuffer html, boolean isAllPackages) {
 		Set<Node> children = root.getChildren();
 		if (children != null) {
 			html.append("<ul>");
 			for (Node node : children) {
 				if (node.isFlag()) {
 					html.append("<li type='circle'>\n");
 					String nodeName = node.getName().substring(node.getName().lastIndexOf(".") + 1);
 					String tempDir = "";
 					if (isAllPackages) {
 						 tempDir = "." + node.getName().substring(0, node.getName().lastIndexOf(".")) + "/";
 					}
 					html.append(HtmlUtils.getAnchorTag("", tempDir + "types/"
 							+ nodeName + Constants.DOT_HTML, "", nodeName)
 							+ Constants.HTML_BR);
 					writeTree(node, html, isAllPackages);
 				}
 			}
 			if (children.size() != 0) {
 				html.append("</ul>");
 			}
 		}
 	}
 
 	private Node getTypesInTree(List<XSDDocInterface> wsdlDoc) {
 		List<ComplexType> complexTypes = new ArrayList<ComplexType>();
 		for (XSDDocInterface xsdDocument : wsdlDoc) {
 			if (xsdDocument.getAllComplexTypes() != null) {
 				complexTypes.addAll(xsdDocument.getAllComplexTypes());
 			}
 		}
 
 		// complexTypes = getInput();
 
 		Node root = new Node();
 		root.setName("Root");
 		root.setLevel(0);
 		for (ComplexType type : complexTypes) {
 			Node node = new Node();
 			String packageName=type.getPackageName();
 			if (packageName == null) {
 				packageName = "/DefaultDomain";
 			}
 			node.setName(packageName + "." + type.getName());
 			node.setOriginalParent(packageName + "." + type.getParentType());
 
 			/*
 			 * if
 			 * (type.getName().equals("GetSearchKeywordsRecommendationResponse"
 			 * )) { System.out.println(); }
 			 */
 
 			getParent(root, node, packageName + "." + type.getParentType());
 			if (!node.isNodeAdded()) {
 				if (root.getChildren() == null) {
 					root.setChildren(new TreeSet<Node>());
 				}
 				node.setParent(root);
 				node.setLevel(root.getLevel() + 1);
 				root.getChildren().add(node);
 			} /*
 			 * else { if (tNode.getChildren() == null) { tNode.setChildren(new
 			 * TreeSet<Node>()); } node.setParent(tNode);
 			 * tNode.getChildren().add(node); }
 			 */
 			normalizeTree(type, root, node);
 		}
 		return root;
 	}
 
 	private void normalizeTree(ComplexType type, Node root, Node node) {
 		Set<Node> set = root.getChildren();
 		if (node.getName().equals(root.getName())) {
 			return;
 		}
 		if (set != null) {
 			for (Node n : set) {
 				if (n.getOriginalParent() != null
 						&& n.getOriginalParent().equals(node.getName())
 						&& n.isFlag()) {
 					if (node.getChildren() == null) {
 						node.setChildren(new TreeSet<Node>());
 					}
 					n.setFlag(false);
 					Node newNode = new Node();
 					newNode.setName(n.getName());
 					newNode.setOriginalParent(n.getOriginalParent());
 					newNode.setParent(node);
 					newNode.setLevel(node.getLevel() + 1);
 					newNode.setChildren(n.getChildren());
 					newNode.setFlag(true);
 					node.getChildren().add(newNode);
 				}
 				normalizeTree(type, n, node);
 			}
 		}
 	}
 
 	private Node getParent(Node root, Node node, String parent) {
 		if (root.getName().equals(parent)) {
 			if (root.getChildren() == null) {
 				root.setChildren(new TreeSet<Node>());
 			}
 			root.getChildren().add(node);
 			node.setParent(root);
 			node.setLevel(root.getLevel() + 1);
 			node.setNodeAdded(true);
 			return root;
 		} else {
 			Node retValue = null;
 			if (root.getChildren() != null) {
 				Set<Node> set = root.getChildren();
 				for (Node tRoot : set) {
 					retValue = getParent(tRoot, node, parent);
 				}
 			}
 			return retValue;
 		}
 	}
 
 	/**
 	 * Adds the package to service map.
 	 * 
 	 * @param packageName
 	 *            the package name
 	 * @param serviceName
 	 *            the service name
 	 */
 	private void addPackageToServiceMap(String packageName, String serviceName) {
 		List<String> list = packageServicesMap.get(packageName);
 		if (list == null) {
 			list = new ArrayList<String>();
 		}
 		list.add(serviceName);
 		packageServicesMap.put(packageName, list);
 	}
 
 	/**
 	 * Adds the package to Doc map.
 	 * 
 	 * @param packageName
 	 *            the package name
 	 * @param serviceName
 	 *            the service name
 	 */
 	private void addPackageToDocMap(String packageName, XSDDocInterface doc) {
 		List<XSDDocInterface> list = packageDocMap.get(packageName);
 		if (list == null) {
 			list = new ArrayList<XSDDocInterface>();
 		}
 		list.add(doc);
 		packageDocMap.put(packageName, list);
 	}
 
 	/**
 	 * Creates the all packages description file.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void createAllPackagesDescriptionFile()
 			throws OutputFormatterException {
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags(Constants.ALL_CLASSES, null));
 		Map<String, String> replacementMap=new HashMap<String, String>();
 		replacementMap.put("REL_PATH", ".");
 		replacementMap.put("TREE_REL_PATH", ".");
 		replacementMap.put("KEY_LINKS", "");
 		buildHeader(html, replacementMap,"indexHeader");
 		html.append(getTextInDiv("WSDL API specification", "pageHeading"));
 		html.append(Constants.HTML_BR);
 		addFooter(html, replacementMap,"indexHeader");
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, getCurrentOutputDir(), Constants.PACKAGES.toLowerCase()
 				+ Constants.INDEX + Constants.DOT_HTML);
 	}
 
 	/**
 	 * Creates the index file.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void createIndexFile() throws OutputFormatterException {
 		StringBuffer html = new StringBuffer(200);
 		html.append("<html><frameset cols=\"20%,80%\">");
 		html.append("<frameset rows=\"30%,70%\">");
 		html.append("<frame src='" + Constants.PACKAGES.toLowerCase()
 				+ Constants.DOT_HTML + "' title=''/>");
 		html.append("<frame src='" + Constants.ALLCLASSES.toLowerCase()
 				+ Constants.DOT_HTML + "' name='" + Constants.CLASSESFRAME
 				+ "' title=''/>");
 		html.append("</frameset>");
 		html.append("<frame src='" + Constants.PACKAGES.toLowerCase()
 				+ Constants.INDEX + Constants.DOT_HTML + "' name='"
 				+ Constants.CLASSFRAME + "' title=''/>");
 		html.append("</frameset></html>");
 		writeFile(html, getCurrentOutputDir(), Constants.INDEX
 				+ Constants.DOT_HTML);
 	}
 
 	/**
 	 * Creates the individual package files.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void createIndividualPackageFiles() throws OutputFormatterException {
 		Set<String> set = packageServicesMap.keySet();
 		for (String packageName : set) {
 			List<String> list = packageServicesMap.get(packageName);
 			StringBuffer html = new StringBuffer();
 			html.append(HtmlUtils.getStartTags(Constants.CLASSES, packageName));
 			html
 					.append("<script language='JavaScript'>function go(targetUrl1){parent.classframe.location=targetUrl1;}</script>");
 			html.append(Constants.HTML_BOLD_START + Constants.CLASSES
 					+ Constants.HTML_BOLD_END + Constants.HTML_BR);
 			for (String className : list) {
 				html
 						.append("<a href='" + className
 								+ "TypeIndex.html' onclick='go(\"" + className
 								+ ".html\")'>" + className + "</a>"
 								+ Constants.HTML_BR);
 			}
 			html.append(HtmlUtils.getEndTags());
 			writeFile(html, getCurrentOutputDir() + packageName,
 					Constants.ALLCLASSES + Constants.DOT_HTML);
 		}
 	}
 
 	/**
 	 * Creates the all classes file.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void createAllClassesFile() throws OutputFormatterException {
 		Set<String> set = packageServicesMap.keySet();
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags(Constants.ALL_CLASSES, null));
 		html
 				.append("<script language='JavaScript'>function go(targetUrl){parent."
 						+ Constants.CLASSFRAME
 						+ ".location=targetUrl;}</script>");
 		html.append(Constants.HTML_BOLD_START + Constants.ALL_CLASSES
 				+ Constants.HTML_BOLD_END + Constants.HTML_BR);
 		for (String packageName : set) {
 
 			List<String> list = packageServicesMap.get(packageName);
 			for (String className : list) {
 				html.append("<a href='" + packageName + SEPARATOR + className
 						+ "TypeIndex.html' onclick='go(\"" + packageName
 						+ SEPARATOR + className + ".html\")'>" + className
 						+ "</a>" + Constants.HTML_BR);
 			}
 		}
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, getCurrentOutputDir(), Constants.ALLCLASSES
 				+ Constants.DOT_HTML);
 	}
 
 	private void createTypeIndex(WSDLDocInterface doc)
 			throws OutputFormatterException {
 
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags("All Types", null));
 		html
 				.append("<script language='JavaScript'>function go(targetUrl){parent."
 						+ Constants.CLASSFRAME
 						+ ".location=targetUrl;}</script>");
 		html.append(Constants.HTML_BOLD_START + "Complex Types"
 				+ Constants.HTML_BOLD_END + Constants.HTML_BR);
 		for (ComplexType node : doc.getAllComplexTypes()) {
 			html.append(HtmlUtils.getAnchorTag("", "types/" + node.getName()
 					+ Constants.DOT_HTML, "", node.getName(),
 					Constants.CLASSFRAME, null)
 					+ Constants.HTML_BR);
 		}
 		html.append(Constants.HTML_BOLD_START + "Simple Types"
 				+ Constants.HTML_BOLD_END + Constants.HTML_BR);
 		for (SimpleType node : doc.getAllSimpleTypes()) {
 			html.append(HtmlUtils.getAnchorTag("", "types/" + node.getName()
 					+ Constants.DOT_HTML, "", node.getName(),
 					Constants.CLASSFRAME, null)
 					+ Constants.HTML_BR);
 		}
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, getCurrentOutputDir() + currentPackageName, doc
 				.getServiceName()
 				+ "TypeIndex" + Constants.DOT_HTML);
 	}
 
 	/**
 	 * Creates the all packages file.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void createAllPackagesFile() throws OutputFormatterException {
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags(Constants.PACKAGES, null));
 		Set<String> set = packageServicesMap.keySet();
 		html.append(Constants.HTML_BOLD_START + Constants.PACKAGES
 				+ Constants.HTML_BOLD_END + Constants.HTML_BR);
 		for (String packageName : set) {
 			html.append(HtmlUtils.getAnchorTag(packageName, "." + SEPARATOR
 					+ packageName + SEPARATOR + Constants.ALLCLASSES
 					+ Constants.DOT_HTML, null, packageName,
 					Constants.CLASSESFRAME, null)
 					+ Constants.HTML_BR);
 		}
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, getCurrentOutputDir(), Constants.PACKAGES.toLowerCase()
 				+ Constants.DOT_HTML);
 	}
 
 	private String getRelativePath(String currLocFromBase) {
 		String relPath = "";
 
 		if (currLocFromBase != null) {
 			if (currLocFromBase.startsWith("/")) {
 				currLocFromBase = currLocFromBase.substring(1);
 			}
 			String[] folders = currLocFromBase.split("/");
 			for (int i=0;i<folders.length;i++) {
 				relPath = relPath + "../";
 			}
 		}
 		return relPath;
 	}
 
 	/**
 	 * Write complex type file.
 	 * 
 	 * @param doc
 	 *            the doc
 	 * @param type
 	 *            the type
 	 * @param parentPath
 	 *            the parent path
 	 * @throws OutputFormatterException
 	 */
 	private void writeComplexTypeFile(WSDLDocInterface doc, ComplexType type,
 			String parentPath) throws OutputFormatterException {
 		logger.entering("JavaDocOuputGenerator", "writeComplexTypeFile",
 				new Object[] { doc, type });
 		processedTypes.add(type);
 		boolean isRecursive = false;
 		StringBuffer html = new StringBuffer();
 		String typeName = type.getName();
 		if (typeName != null && !Utils.isEmpty(typeName)) {
 			String parentType = type.getParentType();
 			if (!Utils.isEmpty(parentType)) {
 				parentType = Utils.removeNameSpace(parentType);
 				ComplexType parent = doc.searchCType(parentType);
 				if (parent != null && !processedTypes.contains(parent)) {
 					writeComplexTypeFile(doc, parent, parentPath);
 				}
 			}
 			html = new StringBuffer();
 			html.append(HtmlUtils.getStartTags(typeName, currentPackageName
 
 					+ TYPES));
 			Map<String, String> replacementMap=new HashMap<String, String>();
 			replacementMap.put("REL_PATH", getRelativePath(currentPackageName + TYPES));
 			replacementMap.put("TYPE_NAME", type.getName());
 			buildHeader(html, replacementMap, "ComplexTypeHeader");
 
 			html.append(getTextInDiv("Type : " + typeName, "JavadocHeading"));
 
 			html.append(Constants.HTML_HR);
 			html.append(Constants.HTML_DL_START + Constants.HTML_DD_START);
 			if (!Utils.isEmpty(parentType)) {
 				html.append(getTextInSpan("Extends: ", "javaDocInlineHeading"));
 				html.append(HtmlUtils.getAnchorTag(null, parentType
 						+ Constants.DOT_HTML, parentType, parentType)
 						+ Constants.HTML_BR);
 			}
 			if (type.getAnnotations().getDocumentation() != null) {
 				html.append(getTextInSpan(type.getAnnotations()
 						.getDocumentation(), "javaDocOpDetail"));
 			}
 			html.append(Constants.HTML_DD_END + Constants.HTML_DL_END);
 			StringBuffer deprDet = AnnotationsHelper.processDeprication(type
 					.getAnnotations());
 			if (deprDet != null) {
 				html
 						.append(getTextInDiv("Depricated: ",
 								"javaDocInlineHeading"));
 				html.append(deprDet);
 			}
 			processRelatedInfo(html, doc, type.getAnnotations());
 			html.append(Constants.HTML_HR);
 			buildFieldSummary(html, doc, type);
 
 			html.append(Constants.HTML_HR);
 			html
 					.append(HtmlUtils.getAnchorTag("FieldDetail", null, null,
 							null));
 			html.append(getTableTagWithStyle("JavadocTable"));
 			html.append(getTableRowTagWithStyle("JavadocTableTr"));
 			html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 			html.append("Field Detail");
 			html.append(Constants.HTML_TABLE_TH_END);
 			html.append(Constants.HTML_TABLE_TR_END);
 			html.append(Constants.HTML_TABLE_END);
 
 			Set<Element> elements = type.getChildElements();
 			if (type.getSimpleAttributeContent() != null) {
 				elements.addAll(type.getSimpleAttributeContent());
 			}
 			if (elements.isEmpty()) {
 				html.append(type.getName() + " has no fields");
 				html.append(Constants.HTML_TABLE_END);
 			} else {
 				html.append(Constants.HTML_BR + Constants.HTML_BR);
 				for (Element element : elements) {
 					if (parentPath.contains("." + element.getName() + ".")) {
 						isRecursive = true;
 					}
 					String nodePath = parentPath + "." + element.getName();
 					String typeCName = getCTypeTypeName(element.getType());
 					if (type.getName().equalsIgnoreCase(typeCName)) {
 						isRecursive = true;
 					}
 					html.append(HtmlUtils.getAnchorTag(element.getName(), null,
 							null, null));
 					ComplexType cType=doc.searchCType(typeCName);
 					if ( cType!= null && !isRecursive) {
 						writeComplexTypeFile(doc, doc.searchCType(typeCName),
 								nodePath);
 						String typeVisName=
 							HtmlUtils.getAnchorTag(null, typeCName
 									+ Constants.DOT_HTML, typeCName,
 									typeCName);
 						StringBuffer attrParams = new StringBuffer();
 						if (!Utils.isEmpty(type.getParentType())) {
 							String baseType = Utils.removeNameSpace(type.getParentType());
 							if(doc.searchCType(baseType)==null && doc.searchSimpleType(baseType)==null ){
 								attrParams.append(baseType);
 							}else{
 								attrParams.append(HtmlUtils.getAnchorTag(null, baseType
 										+ Constants.DOT_HTML, baseType, baseType));
 							}
 							
 						}
 						if (attrParams.length() > 0) {
 							typeVisName += " ( " +attrParams+ " )";
 						}
 						html.append(getTextInSpan(element.getName()
 								+ "  : <b>"
 								+ typeVisName + "</b>",
 								"javaDocMethodTypes"));
 					} else if (doc.searchSimpleType(typeCName) != null) {
 						SimpleType Stype=doc.searchSimpleType(typeCName);
 						writeSimpleTypeFile(doc, doc
 								.searchSimpleType(typeCName));
 						String typeVisName=
 							HtmlUtils.getAnchorTag(null, typeCName
 									+ Constants.DOT_HTML, typeCName,
 									typeCName);
 						String base = Utils.removeNameSpace(Stype.getBase());
 						if(!Utils.isEmpty(base)){
 							if(doc.searchCType(base)==null && doc.searchSimpleType(base)==null ){
 								typeVisName+=" ( "+base+" )";
 							}else{
 								typeVisName+=" ( "+ HtmlUtils.getAnchorTag(null,  base
 										+ Constants.DOT_HTML, base,
 										base) +" )";
 							}
 						}
 						html.append(getTextInSpan(element.getName()
 								+ "  : <b>"
 								+ HtmlUtils.getAnchorTag(null, typeCName
 										+ Constants.DOT_HTML, typeCName,
 										typeVisName) + "</b>",
 								"javaDocMethodTypes"));
 					} else {
 						html.append(getTextInSpan(element.getName() + "  : <b>"
 								+ typeCName + "</b>", "javaDocMethodTypes"));
 					}
 					if (element.getAnnotationInfo().getDocumentation() != null) {
 						html
 								.append(Constants.HTML_BR
 										+ getTextInSpan(element
 												.getAnnotationInfo()
 												.getDocumentation(),
 												"javaDocOpDetail"));
 					}
 					processDefaultsAndBoundries(html, doc, element);
 					deprDet = AnnotationsHelper.processDeprication(element
 							.getAnnotationInfo());
 					if (deprDet != null) {
 						html.append(getTextInDiv("Depricated: ",
 								"javaDocInlineHeading"));
 						html.append(deprDet);
 					}
 					processRelatedInfo(html, doc, element.getAnnotationInfo());
 					html.append(Constants.HTML_HR + Constants.HTML_BR);
 				}
 			}
 			writeUseFile(doc, type);			
 			addFooter(html, replacementMap, "ComplexTypeHeader");
 
 			writeFile(html, currentTypesFolderPath, typeName
 					+ Constants.DOT_HTML);
 		}
 		logger.exiting("JavaDocOuputGenerator", "writeComplexTypeFile", html);
 	}
 	
 	private void writeUseFile(WSDLDocInterface doc, AbstractType type) throws OutputFormatterException {
 		logger.entering("JavaDocOuputGenerator", "writeUseFile");
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags(type.getName(), currentPackageName + "/types-use"));
 		Map<String, String> replacementMap=new HashMap<String, String>();
 		replacementMap.put("REL_PATH", getRelativePath(currentPackageName + "/types-use"));
 		replacementMap.put("TREE_REL_PATH", "..");
 		
 		buildHeader(html, replacementMap,"typesUseHeader");
 		
 		addHeadingTable(html, "Uses of " + type.getName());
 		boolean tableAdded = false;
 		
 		Set<String> methods = returnTypeToMethodMap.get(type.getName());
 		if (methods != null) {
 			html.append(Constants.HTML_BR_TWICE);
 			html.append(getTableTagWithStyle("JavadocTable"));
 			html.append(getTableRowTagWithStyle("JavadocTableTr"));
 			html.append("<th colspan=\"2\" class=\"TableSubHeadingColor\">");
 			html.append("Methods that return " + type.getName());
 			html.append(Constants.HTML_TABLE_TH_END);
 			html.append(Constants.HTML_TABLE_TR_END);
 		
 			for (String str : methods) {
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				
 				html.append(getTableDataTagWithStyle("JavadocTableTd"));
 				html.append(HtmlUtils.getAnchorTag(null, "../types/" + type.getName() + Constants.DOT_HTML, null, type.getName()));
 				html.append(Constants.HTML_TABLE_TD_END);
 				
 				html.append(getTableDataTagWithStyle("JavadocTableTd"));
 				html.append(str);				
 				html.append(Constants.HTML_TABLE_TD_END);
 				
 				html.append(Constants.HTML_TABLE_TR_END);
 			}
 			html.append(Constants.HTML_TABLE_END);			
 			tableAdded = true;
 		}
 		
 		methods = paramsToMethodMap.get(type.getName());
 		if (methods != null) {
 			html.append(Constants.HTML_BR_TWICE);
 			html.append(getTableTagWithStyle("JavadocTable"));
 			html.append(getTableRowTagWithStyle("JavadocTableTr"));
 			html.append("<th colspan=\"2\" class=\"TableSubHeadingColor\">");
 			html.append("Methods with parameters of type " + type.getName());
 			html.append(Constants.HTML_TABLE_TH_END);
 			html.append(Constants.HTML_TABLE_TR_END);
  		
  			Iterator<String> itMethods = methods.iterator();
 			while(itMethods.hasNext()) {
 				String method = itMethods.next();
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append(getTableDataTagWithStyle("JavadocTableTd"));
 				String retTypeName = method.substring(0, method.indexOf('-'));
 				html.append(HtmlUtils.getAnchorTag(null, "../types/" + retTypeName + Constants.DOT_HTML, null, retTypeName));
 				html.append(Constants.HTML_TABLE_TD_END);
 				
 				html.append(getTableDataTagWithStyle("JavadocTableTd"));
 				html.append(method.substring(method.indexOf('-') + 1));
 				html.append(Constants.HTML_TABLE_TD_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 			}
 			html.append(Constants.HTML_TABLE_END);
 			tableAdded = true;
 		}
 		
 		if (doc instanceof WSDLDocument) {
 			// write calls that use this type
 			Map<String, List<ComplexType>> map = doc.getElementComplexTypeMap();
 			List<ComplexType> tempTypes = map.get(type.getName());
 			if (tempTypes != null) {
 				html.append(Constants.HTML_BR_TWICE);
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th colspan=\"2\" class=\"TableSubHeadingColor\">");
 				html.append("Types that use " + type.getName());
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				
 				Set<String> set = new TreeSet<String>();
 				if (tempTypes != null) {
 					for (ComplexType tempType : tempTypes) {
 						set.addAll(getExtensionTypes(tempType.getName(), doc));
 					}
 				}
 				Iterator<String> setIterator = set.iterator();
 				while (setIterator.hasNext()) {
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 					String name = setIterator.next();
 					html.append(HtmlUtils.getAnchorTag(null, "../types/" + name
 							+ Constants.DOT_HTML, null, name));					
 					html.append(Constants.HTML_TABLE_TD_END);
 					
 					ComplexType cType = doc.searchCType(Utils.removeNameSpace(name));
 					String desc = "";
 					if (cType != null) {
 						desc = cType.getAnnotations().getDocumentation();
 					}
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 					html.append(desc);
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 				html.append(Constants.HTML_TABLE_END);
 				tableAdded = true;
 			}
 		}		
 		
 		if (!tableAdded) {
 			html.append(type.getName() + " is not used anywhere directly.");
 		}
 		
 		html.append(Constants.HTML_BR);
 		addFooter(html, replacementMap,"typesUseHeader");
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, classUseFolderPath, type.getName() + Constants.DOT_HTML);
 		logger.exiting("JavaDocOuputGenerator", "writeUseFile", html);
 	}
 	
 	private Set<String> getExtensionTypes(String cTypeName, XSDDocInterface doc) {
 		Set<String> extnTypes = new HashSet<String>();
 		Set<String> parentTypes = doc.getParentToComplexTypeMap()
 				.get(cTypeName);
 		if (parentTypes != null) {
 			for (String parent : parentTypes) {
 				if (!parent.equals(cTypeName)) {
 					ComplexType parentType = doc.searchCType(Utils
 							.removeNameSpace(parent));
 					if (parentType != null) {
 						Set<String> innerExtnTypes = getExtensionTypes(
 								parentType.getName(), doc);
 						extnTypes.addAll(innerExtnTypes);
 					}
 				}
 			}
 
 		}
 		if (extnTypes.isEmpty()) {
 			extnTypes.add(cTypeName);
 		}
 		return extnTypes;
 	}
 	
 	
 
 	private void addHeadingTable(StringBuffer html, String heading) {
 		html.append(getTableTagWithStyle("JavadocTable"));
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append(heading);
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		html.append(Constants.HTML_TABLE_END);
 	}
 	
 	private void buildFieldSummary(StringBuffer html, WSDLDocInterface doc,
 			ComplexType type) {
 		Set<Element> elements = type.getChildElements();
 		if (type.getSimpleAttributeContent() != null) {
 			elements.addAll(type.getSimpleAttributeContent());
 		}
 
 		html.append(HtmlUtils.getAnchorTag("FieldSummary", null, null, null));
 
 		html.append(getTableTagWithStyle("JavadocTable"));
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append("<th colspan=\"2\" class=\"JavadocTableHeaders\">");
 		html.append("Field Summary");
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		if (elements == null || elements.isEmpty()) {
 			html.append(Constants.HTML_TABLE_END);
 			html.append(type.getName() + " has no fields");
 
 			return;
 		}
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append("Type");
 		html.append(Constants.HTML_TABLE_TH_END);
 
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append("Name");
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		
 		for (Element element : elements) {
 			html.append(getTableRowTagWithStyle("JavadocTableTr"));
 			html.append(getTableDataTagWithStyle("JavadocTableTd"));
 			String typeCName = getCTypeTypeName(element.getType());
 			if (doc.searchCType(typeCName) != null
 					|| doc.searchSimpleType(typeCName) != null) {
 
 				html.append(HtmlUtils.getAnchorTag(null, element.getType()
 						+ Constants.DOT_HTML, element.getType(), element
 						.getType()));
 			} else {
 				html.append(element.getType());
 			}
 
 			html.append(Constants.HTML_TABLE_TD_END);
 
 			html.append(getTableDataTagWithStyle("JavadocTableTd"));
 			html.append(HtmlUtils.getAnchorTag(null, "#" + element.getName(),
 					element.getName(), element.getName()));
 			html.append(Constants.HTML_BR);
 			if (element.getAnnotationInfo() != null
 					&& element.getAnnotationInfo().getDocumentation() != null) {
 				html.append(element.getAnnotationInfo().getDocumentation());
 			}
 			html.append(Constants.HTML_TABLE_TD_END);
 			html.append(Constants.HTML_TABLE_TR_END);
 		}
 		html.append(Constants.HTML_TABLE_END);
 	}
 
 	private void processSeeLinks(StringBuffer html, ParsedAnnotationInfo annInfo) {
 
 		if (annInfo != null) {
 			List<ParsedAnnotationTag> seeLinks = AnnotationsHelper
 					.getAnnotationTag(annInfo, "SeeLink");
 			boolean headingAdded = false;
 			if (seeLinks != null) {
 				headingAdded = true;
 				html.append(getTextInDiv("<br>See: <br>",
 						"javaDocRelatedHeading"));
 				for (ParsedAnnotationTag seeLink : seeLinks) {
 					html.append(AnnotationsHelper.processSeelink(seeLink)
 							+ "<br>");
 				}
 			}
 			List<ParsedAnnotationTag> callInofs = AnnotationsHelper
 					.getCallInfo(annInfo);
 			if (callInofs != null) {
 				for (ParsedAnnotationTag callInfo : callInofs) {
 					seeLinks = AnnotationsHelper.getCallInfoChildren(callInfo,
 							"SeeLink");
 					if (seeLinks != null) {
 						if (!headingAdded) {
 							html.append(getTextInDiv("<br>See: <br>",
 									"javaDocRelatedHeading"));
 						}
 						for (ParsedAnnotationTag seeLink : seeLinks) {
 							html.append(AnnotationsHelper
 									.processSeelink(seeLink)
 									+ "<br>");
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Write simple type file.
 	 * 
 	 * @param doc
 	 *            the doc
 	 * @param type
 	 *            the type
 	 * @throws OutputFormatterException
 	 */
 	private void writeSimpleTypeFile(WSDLDocInterface doc, SimpleType type)
 			throws OutputFormatterException {
 		logger.entering("JavaDocOuputGenerator", "writeSimpleTypeFile",
 				new Object[] { doc, type });
 		String typeName = type.getName();
 		StringBuffer html = new StringBuffer();
 		if (typeName != null && !Utils.isEmpty(typeName)) {
 
 			html = new StringBuffer();
 			html.append(HtmlUtils.getStartTags(typeName, currentPackageName
 
 					+ TYPES));
 			Map<String, String> replacementMap=new HashMap<String, String>();
 			replacementMap.put("REL_PATH", getRelativePath(currentPackageName + TYPES));
 			replacementMap.put("TYPE_NAME", type.getName());
 			buildHeader(html, replacementMap,"SimpleTypeHeader");
 
 			String parentType = type.getBase();
 			if (!Utils.isEmpty(parentType)) {
 				parentType = Utils.removeNameSpace(parentType);
 				typeName = typeName + "( " + parentType + ")";
 			}
 			html.append(getTextInDiv("Type : " + typeName, "JavadocHeading"));
 			html.append(Constants.HTML_HR);
 			html.append(Constants.HTML_DL_START + Constants.HTML_DD_START);
 
 			if (type.getAnnotations().getDocumentation() != null) {
 				html.append(getTextInSpan(type.getAnnotations()
 						.getDocumentation(), "javaDocOpDetail"));
 			}
 			html.append(Constants.HTML_DD_END + Constants.HTML_DL_END);
 			StringBuffer deprDet = AnnotationsHelper.processDeprication(type
 					.getAnnotations());
 			if (deprDet != null) {
 				html
 						.append(getTextInDiv("Depricated: ",
 								"javaDocInlineHeading"));
 				html.append(deprDet);
 			}
 			processRelatedInfo(html, doc, type.getAnnotations());
 
 			html.append(Constants.HTML_HR);
 
 			html.append(Constants.HTML_BR + Constants.HTML_BR);
 			List<EnumElement> enumElements = type.getEnums();
 			if (enumElements != null && !enumElements.isEmpty()) {
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th colspan=\"2\" class=\"JavadocTableHeaders\">");
 				html.append("Enumeration Values");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 				html.append("Value");
 				html.append(Constants.HTML_TABLE_TH_END);
 
 				html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 				html.append("Description");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 
 				for (EnumElement enumElement : enumElements) {
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 					html.append(HtmlUtils.getAnchorTag(enumElement.getValue(),
 							null, null, null));
 					html.append(enumElement.getValue());
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 					if (enumElement.getAnnotations().getDocumentation() != null) {
 						html.append(enumElement.getAnnotations()
 								.getDocumentation());
 					}
 					deprDet = AnnotationsHelper.processDeprication(enumElement
 							.getAnnotations());
 					if (deprDet != null) {
 						html.append(getTextInDiv("Depricated: ",
 								"javaDocInlineHeading"));
 						html.append(deprDet);
 					}
 					processRelatedInfo(html, doc, enumElement.getAnnotations());
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 
 				html.append(Constants.HTML_TABLE_END);
 			}
 			writeUseFile(doc, type);
 			addFooter(html, replacementMap,"SimpleTypeHeader");
 
 			writeFile(html, currentTypesFolderPath, type.getName()
 					+ Constants.DOT_HTML);
 
 		}
 
 		logger.exiting("JavaDocOuputGenerator", "writeSimpleTypeFile", html);
 	}
 
 	/**
 	 * Gets the c type type name.
 	 * 
 	 * @param type
 	 *            the type
 	 * @return the c type type name
 	 */
 	private String getCTypeTypeName(String type) {
 		String retType = "";
 		if (type != null) {
 			String[] types = type.split(":");
 			if (types != null) {
 				switch (types.length) {
 				case 1:
 					retType = types[0];
 					break;
 				case 2:
 					retType = types[1];
 					break;
 				default:
 					retType = "";
 				}
 
 			}
 		}
 		return retType;
 	}
 
 	/**
 	 * Write css files.
 	 * 
 	 * @throws OutputFormatterException
 	 */
 	private void writeCssFiles() throws OutputFormatterException {
 		InputStream is = this.getClass().getClassLoader().getResourceAsStream(
 				"style.css");
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
 			String outputDir = getCurrentOutputDir();
 			writeFile(sb, outputDir + File.separator + "css",
 					"JavaDocDefaultStyle.css");
 			if (Context.getContext().getCssFilePath() != null) {
 				sb = new StringBuffer();
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 				writeFile(sb, outputDir + File.separator + "css",
 						"CustomStyle.css");
 
 			}
 
 		} catch (IOException e) {
 			logger.severe(e.getMessage());
 		}
 	}
 
 	/**
 	 * Write file.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param dir
 	 *            the dir
 	 * @param fileName
 	 *            the file name
 	 * @throws OutputFormatterException
 	 */
 	private void writeFile(StringBuffer html, String dir, String fileName)
 			throws OutputFormatterException {
 		try {
 			File file = new File(dir);
 			file.mkdirs();
 			FileWriter fw = new FileWriter(dir + File.separator + fileName);
 			fw.write(html.toString());
 			fw.flush();
 			fw.close();
 		} catch (IOException e) {
 			throw new OutputFormatterException(e);
 		}
 	}
 
 	/**
 	 * Gets the table tag with style.
 	 * 
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the table tag with style
 	 */
 	private String getTableTagWithStyle(String styleClassName) {
 		String tableWithAttr = Constants.HTML_TABLE_START_WITH_ATTRS;
 		return tableWithAttr.replaceAll("\\{#\\}", "class=\"" + styleClassName
 				+ "\"");
 	}
 
 	/**
 	 * Gets the table row tag with style.
 	 * 
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the table row tag with style
 	 */
 	private String getTableRowTagWithStyle(String styleClassName) {
 		String tableWithAttr = Constants.HTML_TABLE_TR_START_WITH_ATTRS;
 		return tableWithAttr.replaceAll("\\{#\\}", "class=\"" + styleClassName
 				+ "\"");
 	}
 
 	/**
 	 * Gets the table data tag with style.
 	 * 
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the table data tag with style
 	 */
 	private String getTableDataTagWithStyle(String styleClassName) {
 		String tableWithAttr = Constants.HTML_TABLE_TD_START_WITH_ATTRS;
 		return tableWithAttr.replaceAll("\\{#\\}", "class=\"" + styleClassName
 				+ "\"");
 	}
 
 	/**
 	 * Gets the table head tag with style.
 	 * 
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the table head tag with style
 	 */
 	private String getTableHeadTagWithStyle(String styleClassName) {
 		String tableWithAttr = Constants.HTML_TABLE_TH_START_WITH_ATTRS;
 		return tableWithAttr.replaceAll("\\{#\\}", "class=\"" + styleClassName
 				+ "\"");
 	}
 
 	/**
 	 * Builds the header.
 	 * 
 	 * @param html
 	 *            the html
 	 * 
 	 * @throws OutputFormatterException
 	 */
 
 	private void buildHeader(StringBuffer html,Map<String,String> replacementMap,String hdrFileParameterName) throws OutputFormatterException {
 
 		logger.entering("JavaDocOutputGenerator", "buildHeader", html);
 		String header = getFooterInformation(replacementMap,hdrFileParameterName);
 		html.append(header);
 
 		logger.exiting("JavaDocOutputGenerator", "buildHeader", html);
 	}
 
 	
 
 	/**
 	 * Adds the footer.
 	 * 
 	 * @param html
 	 *            the html
 	 * @throws OutputFormatterException
 	 */
 
 	private void addFooter(StringBuffer html,Map<String,String> replacementMap,String hdrFileParameterName) throws OutputFormatterException {
 
 		logger.entering("JavaDocOutputGenerator", "addFooter", html);
 		String content = getFooterInformation(replacementMap,hdrFileParameterName);
 		html.append(getTextInDiv(content, "footer"));
 		html.append(HtmlUtils.getEndTags());
 		logger.exiting("JavaDocOutputGenerator", "addFooter", html);
 	}
 
 	/**
 	 * Gets the footer information. This method can be over ridden to supply
 	 * Footer.
 	 * 
 	 * @return the footer information
 	 * @throws OutputFormatterException
 	 */
 
 	protected String getFooterInformation(Map<String,String> replacementMap,String hdrFileParameterName)
 			throws OutputFormatterException {
 		String headerFile = outputGenaratorParam.getParameters().get(
 				hdrFileParameterName);
 		if (headerFile != null) {
 			try {
 				String header = Utils.getFileAsString(
 						Utils.convertToURL(headerFile).openStream()).toString();
 				for(Map.Entry<String, String> entry:replacementMap.entrySet()){
 					header = header.replaceAll("\\$\\{"+entry.getKey()+"\\}", entry.getValue());
 				}
 				String version = outputGenaratorParam.getParameters().get(
 						"version");
 				header = header.replaceAll("\\$\\{VERSION\\}", version);
 				header = "<hr/>" + header;
 				return header;
 			} catch (IOException e) {
 				throw new OutputFormatterException(e);
 			}
 
 		}
 		return "";
 	}
 
 	/**
 	 * Gets the text in div.
 	 * 
 	 * @param text
 	 *            the text
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the text in div
 	 */
 	private String getTextInDiv(String text, String styleClassName) {
 		String temp = "";
 		if (styleClassName != null) {
 			temp += "class='" + styleClassName + "'";
 		}
 		String tag = Constants.HTML_DIV_START.replace("{#}", temp);
 		tag += text;
 		tag += Constants.HTML_DIV_END;
 		logger.info("Div Tag:" + tag);
 		return tag;
 	}
 
 	/**
 	 * Gets the text in span.
 	 * 
 	 * @param text
 	 *            the text
 	 * @param styleClassName
 	 *            the style class name
 	 * @return the text in span
 	 */
 	private String getTextInSpan(String text, String styleClassName) {
 		String temp = "";
 		if (styleClassName != null) {
 			temp += "class='" + styleClassName + "'";
 		}
 		String tag = Constants.HTML_SPAN_START.replace("{#}", temp);
 		tag += text;
 		tag += Constants.HTML_SPAN_END;
 		return tag;
 	}
 
 	/**
 	 * Builds the port type.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param portType
 	 *            the port type
 	 * @param serviceName
 	 *            the service name
 	 */
 	private void buildPortType(StringBuffer html, PortType portType,
 			WSDLDocInterface doc) {
 		String serviceName = doc.getServiceName();
 		logger.entering("JavaDocOutputGenerator", "buildPortType",
 				new Object[] { html, portType, serviceName });
 		String heading = Constants.SERVICE_LABEL + serviceName;
 
 		html.append(getTextInDiv(heading, "JavadocHeading"));
 		ParsedAnnotationInfo anno = doc.getAnnotations();
 		String version = AnnotationsHelper.getFirstAnnotationValue(anno,
 				"Version");
 		if (!Utils.isEmpty(version)) {
 			heading = "<br>Version:  " + version;
 		}
 		html.append(getTextInDiv(heading, "javaDocInlineHeading"));
 		html.append(Constants.HTML_HR);
 		html.append(Constants.HTML_DL_START + Constants.HTML_DD_START);
 		if (anno != null && !Utils.isEmpty(anno.getDocumentation())) {
 			html.append(anno.getDocumentation());
 		}
 		if (portType.getAnnotations() != null
 				&& portType.getAnnotations().getDocumentation() != null) {
 			html.append("<br>" + portType.getAnnotations().getDocumentation());
 		}
 		html.append(Constants.HTML_DD_END + Constants.HTML_DL_END);
 		html.append(Constants.HTML_HR);
 		logger.exiting("JavaDocOutputGenerator", "buildPortType", html);
 	}
 
 	/**
 	 * Builds the operation table.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param portType
 	 *            the port type
 	 * @param wsdlDoc
 	 *            the wsdl doc
 	 */
 	private void buildOperationTable(StringBuffer html, PortType portType,
 			WSDLDocInterface wsdlDoc) {
 		logger.entering("JavaDocOutputGenerator", "buildOperationTable",
 				new Object[] { html, portType });
 		html.append(HtmlUtils.getAnchorTag(Constants.OPERATION_SUMMARY_HREF,
 				null, null, null));
 		html.append(getTableTagWithStyle("JavadocTable"));
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append("<th colspan=\"2\" class=\"JavadocTableHeaders\">");
 		html.append(Constants.OPERATION_SUMMARY_LABEL);
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append(Constants.RETURN_TYPE_LABEL);
 		html.append(Constants.HTML_TABLE_TH_END);
 
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append(Constants.METHOD_NAME_LABEL);
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		for (OperationHolder opH : portType.getOperations()) {
 			html.append(getTableRowTagWithStyle("JavadocTableTr"));
 			html.append(getTableDataTagWithStyle("JavadocTableTd"));
 			setOutputTypes(html, opH, wsdlDoc.getServiceName());
 			html.append(Constants.HTML_TABLE_TD_END);
 			html.append(getTableDataTagWithStyle("JavadocTableTd"));
 			html.append(getTextInSpan(HtmlUtils.getAnchorTag(null, "#"
 					+ opH.getName(), opH.getName(), opH.getName()),
 					"javaDocMethodName")
 					+ " (");
 			html.append(getInputTypes(opH, wsdlDoc.getServiceName(), ""));
 
 			html.append(")");
 			html.append(Constants.HTML_BR);
 			html.append(getSummary(wsdlDoc, opH));
 			html.append(Constants.HTML_TABLE_TD_END);
 			html.append(Constants.HTML_TABLE_TR_END);
			if (returnTypeToMethodMap.get(opH.getOutputTypes().get(0).getType()) == null) {
 				returnTypeToMethodMap.put(opH.getOutputTypes().get(0).getType(), new TreeSet<String>());
 			} 
 			String str = wsdlDoc.getServiceName() + "." + opH.getName() + "(";
 			str += getInputTypes(opH, wsdlDoc.getServiceName(), "../") + ")";			
 			str += Constants.HTML_BR + Constants.NBSP_THRICE + getSummary(wsdlDoc, opH);
			returnTypeToMethodMap.get(opH.getOutputTypes().get(0).getType()).add(str);
 			
 			if (opH.getInputTypes() != null) {
 				Iterator<Element> iter = opH.getInputTypes().iterator();
 				while (iter.hasNext()) {
 					Element elem = iter.next();
 					if (paramsToMethodMap.get(elem.getType()) == null) {
 						paramsToMethodMap.put(elem.getType(), new TreeSet<String>());						
 					}
 					String retType = "";
 					if (opH.getOutputTypes() != null && opH.getOutputTypes().size() > 0) {
 						retType = opH.getOutputTypes().get(0).getType();
 					}					
 					str = retType + "-" + wsdlDoc.getServiceName() + "." + opH.getName() + "(" + getParams(opH.getInputTypes(), "../") + ")";
 					str += Constants.HTML_BR + Constants.NBSP_THRICE + getSummary(wsdlDoc, opH);
 					paramsToMethodMap.get(elem.getType()).add(str);
 				}
 			}
 		}
 		html.append(Constants.HTML_TABLE_END);
 		logger.exiting("JavaDocOutputGenerator", "buildOperationTable", html);
 	}
 
 	private String getParams(List<Element> inputs, String locBase) {
 		String params = "";
 		if (inputs != null) {
 			Iterator<Element> iter = inputs.iterator();
 			while (iter.hasNext()) {
 				Element elem = iter.next();
 
 				params = HtmlUtils.getAnchorTag(null, locBase + "types" + SEPARATOR
 						+ elem.getType() + Constants.DOT_HTML, elem.getType(),
 						elem.getType());
 				params += " " + elem.getName();
 				if (iter.hasNext()) {
 					params += ",";
 				}
 				
 			}
 		}
 		return params;
 	}
 	
 	/**
 	 * Sets the input types.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param opH
 	 *            the op h
 	 * @param wsdlDoc
 	 *            the wsdl doc
 	 * @throws OutputFormatterException
 	 */
 	private void setInputTypes(StringBuffer html, OperationHolder opH,
 			WSDLDocInterface wsdlDoc) throws OutputFormatterException {
 		logger.entering("JavaDocOutputGenerator", "setInputTypes",
 				new Object[] { html, opH });
 		List<Element> inputs = opH.getInputTypes();
 		if (inputs != null) {
 			Iterator<Element> iter = inputs.iterator();
 			while (iter.hasNext()) {
 				Element elem = iter.next();
 				html.append(getTextInSpan(HtmlUtils.getAnchorTag(null, "types"
 						+ SEPARATOR + elem.getType() + Constants.DOT_HTML, elem
 						.getType(), elem.getType()), "javaDocMethodTypes"));
 				html.append(" ");
 				html
 						.append(getTextInSpan(elem.getName(),
 								"javaDocMethodTypes"));
 				if (iter.hasNext()) {
 					html.append(",");
 				}
 				ComplexType cType = wsdlDoc.searchCType(elem.getType());
 				if (cType != null) {
 					writeComplexTypeFile(wsdlDoc, cType, cType.getName());
 				}
 			}
 		}
 		logger.exiting("JavaDocOutputGenerator", "setInputTypes", html);
 	}
 
 	/**
 	 * Sets the output types.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param opH
 	 *            the op h
 	 * @param wsdlDoc
 	 *            the wsdl doc
 	 * @throws OutputFormatterException
 	 */
 	private void setOutputTypes(StringBuffer html, OperationHolder opH,
 			WSDLDocInterface wsdlDoc) throws OutputFormatterException {
 		logger.entering("JavaDocOutputGenerator", "setOutputTypes",
 				new Object[] { html, opH });
 		StringBuffer rtBuf = new StringBuffer("");
 		List<Element> inputs = opH.getOutputTypes();
 		if (inputs != null) {
 			Iterator<Element> iter = inputs.iterator();
 			while (iter.hasNext()) {
 				Element elem = iter.next();
 
 				html.append(getTextInSpan(HtmlUtils.getAnchorTag(null, "types"
 						+ SEPARATOR + elem.getType() + Constants.DOT_HTML, elem
 						.getType(), elem.getType()), "javaDocMethodTypes"));
 				if (iter.hasNext() && rtBuf.length() > 0) {
 					html.append(",");
 				}
 				ComplexType cType = wsdlDoc.searchCType(elem.getType());
 				if (cType != null) {
 					writeComplexTypeFile(wsdlDoc, cType, cType.getName());
 				}
 			}
 		}
 		html.append(rtBuf);
 		logger.exiting("JavaDocOutputGenerator", "setOutputTypes", html);
 	}
 
 	/**
 	 * Sets the input types.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param opH
 	 *            the op h
 	 * @param serviceName
 	 *            the service name
 	 */
 	private String getInputTypes(OperationHolder opH,
 			String serviceName, String locBase) {
 		logger.entering("JavaDocOutputGenerator", "setInputTypes",
 				new Object[] {opH });
 		String params = "";
 		List<Element> inputs = opH.getInputTypes();
 		if (inputs != null) {
 			Iterator<Element> iter = inputs.iterator();
 			while (iter.hasNext()) {
 				Element elem = iter.next();
 
 				params = HtmlUtils.getAnchorTag(null, locBase + "types" + SEPARATOR
 						+ elem.getType() + Constants.DOT_HTML, elem.getType(),
 						elem.getType());
 				params += " " + elem.getName();
 				if (iter.hasNext()) {
 					params += ",";
 				}
 				
 			}
 		}
 		
 		logger.exiting("JavaDocOutputGenerator", "setInputTypes", params);
 		return params;
 	}
 
 	/**
 	 * Sets the output types.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param opH
 	 *            the op h
 	 * @param serviceName
 	 *            the service name
 	 */
 	private void setOutputTypes(StringBuffer html, OperationHolder opH,
 			String serviceName) {
 		logger.entering("JavaDocOutputGenerator", "setOutputTypes",
 				new Object[] { html, opH });
 		StringBuffer rtBuf = new StringBuffer("");
 		List<Element> inputs = opH.getOutputTypes();
 		if (inputs != null) {
 			Iterator<Element> iter = inputs.iterator();
 			while (iter.hasNext()) {
 				Element elem = iter.next();
 
 				html.append(HtmlUtils.getAnchorTag(null, "types" + SEPARATOR
 						+ elem.getType() + Constants.DOT_HTML, elem.getType(),
 						elem.getType()));
 				if (iter.hasNext() && rtBuf.length() > 0) {
 					html.append(",");
 				}
 			}
 		}
 		html.append(rtBuf);
 		logger.exiting("JavaDocOutputGenerator", "setOutputTypes", html);
 	}
 
 	/**
 	 * Builds the operation details.
 	 * 
 	 * @param html
 	 *            the html
 	 * @param portType
 	 *            the port type
 	 * @param serviceName
 	 *            the service name
 	 * @throws OutputFormatterException
 	 */
 	private void buildOperationDetails(StringBuffer html, PortType portType,
 			WSDLDocInterface doc) throws OutputFormatterException {
 		logger.entering("JavaDocOutputGenerator", "buildOperationDetails",
 				new Object[] { html, portType });
 		html.append(HtmlUtils.getAnchorTag(Constants.OPERATIONS_DETAIL_HREF,
 				null, null, null));
 
 		html.append(getTableTagWithStyle("JavadocTable"));
 		html.append(getTableRowTagWithStyle("JavadocTableTr"));
 		html.append(getTableHeadTagWithStyle("JavadocTableHeaders"));
 		html.append(Constants.OPERATION_DETAIL_LABEL);
 		html.append(Constants.HTML_TABLE_TH_END);
 		html.append(Constants.HTML_TABLE_TR_END);
 		html.append(Constants.HTML_TABLE_END);
 		for (OperationHolder opH : portType.getOperations()) {
 			html
 					.append(HtmlUtils.getAnchorTag(opH.getName(), null, null,
 							null));
 			html.append(getTextInDiv(opH.getName(), "javaDocInlineHeading"));
 
 			html.append("<pre wrap='true'>");
 			setOutputTypes(html, opH, doc);
 			html.append(" " + getTextInSpan(opH.getName(), "javaDocOpName")
 					+ "(");
 			setInputTypes(html, opH, doc);
 
 			html.append(")");
 			html.append(Constants.HTML_PRE_END);
 
 			html.append(Constants.HTML_DL_START + Constants.HTML_DD_START);
 			appendOperationDocumentation(html, doc, opH);
 			html.append(Constants.HTML_DD_END + Constants.HTML_DL_END);
 		}
 		logger.exiting("JavaDocOutputGenerator", "buildOperationDetails",
 				new Object[] { html, portType });
 	}
 
 	/**
 	 * Gets the summary.
 	 * 
 	 * @param wsdlDoc
 	 *            the wsdl doc
 	 * @param holder
 	 *            the holder
 	 * @return the summary
 	 */
 	private String getSummary(WSDLDocInterface wsdlDoc, OperationHolder holder) {
 		String summary = null;
 		if (holder != null) {
 			List<Element> inputs = holder.getInputTypes();
 			for (Element elem : inputs) {
 				String type = elem.getType();
 				ComplexType ctype = wsdlDoc.searchCType(type);
 				ParsedAnnotationInfo annotationInfo = ctype.getAnnotations();
 				Map<String, List<ParsedAnnotationTag>> tagList = annotationInfo
 						.getValue();
 				if (tagList != null) {
 					List<ParsedAnnotationTag> summaryTag = (List<ParsedAnnotationTag>) tagList
 							.get("Summary");
 
 					if (summaryTag != null) {
 						summary = summaryTag.get(0).getTagValue();
 						break;
 					} else {
 						summary = annotationInfo.getDocumentation();
 					}
 				}
 
 			}
 		}
 		if (summary == null) {
 			summary = "";
 		}
 		return summary;
 	}
 
 	/**
 	 * Gets the operation documentation.
 	 * 
 	 * @param wsdlDoc
 	 *            the wsdl doc
 	 * @param holder
 	 *            the holder
 	 * @return the operation documentation
 	 */
 	private void appendOperationDocumentation(StringBuffer html,
 			WSDLDocInterface wsdlDoc, OperationHolder holder) {
 		if (holder != null) {
 			String documentation = null;
 			if (holder.getAnnotations() != null) {
 				documentation = holder.getAnnotations().getDocumentation();
 				if (holder.getAnnotations().getDocumentation() != null) {
 					html.append(getTextInSpan(documentation, "javaDocOpDetail")
 							+ Constants.HTML_BR);
 				}
 			}
 			List<Element> inputs = holder.getInputTypes();
 			for (Element elem : inputs) {
 				String type = elem.getType();
 				ComplexType ctype = wsdlDoc.searchCType(type);
 				ParsedAnnotationInfo annotationInfo = ctype.getAnnotations();
 				if (annotationInfo != null) {
 					Map<String, List<ParsedAnnotationTag>> tagList = annotationInfo
 							.getValue();
 					if (tagList != null) {
 						List<ParsedAnnotationTag> summaryTag = (List<ParsedAnnotationTag>) tagList
 								.get("Summary");
 
 						if (summaryTag != null) {
 							String summary = summaryTag.get(0).getTagValue();
 							html.append(getTextInDiv("Operation Summary: ",
 									"javaDocRelatedHeading"));
 							html.append(getTextInSpan(summary,
 									"javaDocOpDetail")
 									+ Constants.HTML_BR + Constants.HTML_BR);
 						}
 					}
 					documentation = annotationInfo.getDocumentation();
 					if (documentation != null) {
 						html.append(getTextInDiv("Request Description: ",
 								"javaDocRelatedHeading"));
 						html.append(getTextInSpan(documentation,
 								"javaDocOpDetail")
 								+ Constants.HTML_BR);
 					}
 				}
 
 			}
 			List<Element> outputs = holder.getOutputTypes();
 			for (Element opElem : outputs) {
 				String opType = opElem.getType();
 				ComplexType opCtype = wsdlDoc.searchCType(opType);
 				ParsedAnnotationInfo opannotationInfo = opCtype
 						.getAnnotations();
 				if (opannotationInfo != null) {
 					String opDocumentation = opannotationInfo
 							.getDocumentation();
 					if (opDocumentation != null) {
 						html.append(Constants.HTML_BR
 								+ getTextInDiv("Response Description: ",
 										"javaDocRelatedHeading"));
 						html.append(getTextInSpan(opDocumentation,
 								"javaDocOpDetail")
 								+ Constants.HTML_BR);
 					}
 				}
 			}
 			for (Element elem : inputs) {
 				String type = elem.getType();
 				ComplexType ctype = wsdlDoc.searchCType(type);
 				ParsedAnnotationInfo annotationInfo = ctype.getAnnotations();
 				if (annotationInfo != null) {
 					StringBuffer deprDet = AnnotationsHelper
 							.processDeprication(annotationInfo);
 					if (deprDet != null) {
 						html.append(getTextInDiv("Depricated: ",
 								"javaDocInlineHeading"));
 						html.append(deprDet);
 					}
 					processRelatedInfo(html, wsdlDoc, annotationInfo);
 
 				}
 
 			}
 
 		}
 
 	}
 
 	private void processOccurances(StringBuffer html, ParsedAnnotationInfo anno) {
 		if (anno != null) {
 			List<ParsedAnnotationTag> callInfos = AnnotationsHelper
 					.getCallInfo(anno);
 			Map<String, String[]> callReqRet = new HashMap<String, String[]>();
 			if (callInfos != null) {
 				for (ParsedAnnotationTag callInfo : callInfos) {
 					String req = AnnotationsHelper.getFirstCallInfoTagValue(
 							callInfo, "RequiredInput");
 					String ret = AnnotationsHelper.getFirstCallInfoTagValue(
 							callInfo, "Returned");
 					String[] reqret = new String[2];
 					reqret[0] = req == null ? "---" : req;
 					reqret[1] = ret == null ? "---" : ret;
 					if (callInfo != null && callInfo.getChildren() != null
 							&& callInfo.getChildren().get("AllCalls") != null) {
 
 						callReqRet.put("For all calls", reqret);
 					} else {
 						String value = AnnotationsHelper
 								.getFirstCallInfoTagValue(callInfo,
 										"AllCallsExcept");
 						if (!Utils.isEmpty(value)) {
 							callReqRet.put("For all calls except" + value,
 									reqret);
 						}
 					}
 					List<ParsedAnnotationTag> calls = AnnotationsHelper
 							.getCallInfoChildren(callInfo, "CallName");
 					if (calls != null) {
 						Iterator<ParsedAnnotationTag> callIter = calls
 								.iterator();
 						while (callIter.hasNext()) {
 							ParsedAnnotationTag call = callIter.next();
 							String callString = call.getTagValue();
 							callReqRet.put(callString, reqret);
 						}
 
 					}
 				}
 			}
 			if (!callReqRet.isEmpty()) {
 				html.append(Constants.HTML_BR
 						+ getTextInDiv("Used By: ", "javaDocRelatedHeading"));
 
 				html.append(Constants.HTML_TABLE_START_WITH_ATTRS
 						.replaceAll("\\{#\\}", "class=\""
 								+ "JavadocOccurancesTable" + "\""));
 				html.append(getTableRowTagWithStyle("JavadocOccurancTableTr"));
 				html
 						.append(getTableHeadTagWithStyle("JavadocOccurancTableHeaders"));
 				html.append("Call Name");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html
 						.append(getTableHeadTagWithStyle("JavadocOccurancTableHeaders"));
 				html.append("Required");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html
 						.append(getTableHeadTagWithStyle("JavadocOccurancTableHeaders"));
 				html.append("Returned");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				for (Map.Entry<String, String[]> callsRet : callReqRet
 						.entrySet()) {
 					html
 							.append(getTableRowTagWithStyle("JavadocOccurancTableTr"));
 					html
 							.append(getTableDataTagWithStyle("JavadocOccurancTableTd"));
 					html.append(callsRet.getKey());
 					html.append(Constants.HTML_TABLE_TD_END);
 					html
 							.append(getTableDataTagWithStyle("JavadocOccurancTableTd"));
 					html.append(callsRet.getValue()[0]);
 					html.append(Constants.HTML_TABLE_TD_END);
 					html
 							.append(getTableDataTagWithStyle("JavadocOccurancTableTd"));
 					html.append(callsRet.getValue()[1]);
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 				html.append(Constants.HTML_TABLE_END);
 				html.append(Constants.HTML_BR);
 			}
 		}
 	}
 
 	private void processDefaultsAndBoundries(StringBuffer html,
 			WSDLDocInterface doc, Element element) {
 		if (element != null) {
 			ParsedAnnotationInfo anno = element.getAnnotationInfo();
 			String maxOccursApp = null;
 			String minOccursApp = null;
 			if (anno != null) {
 
 				String defaultVal = AnnotationsHelper.getFirstAnnotationValue(
 						anno, "Default");
 				if (!Utils.isEmpty(defaultVal)) {
 					html.append(Constants.HTML_BR
 							+ getTextInDiv("Default Value: " + defaultVal,
 									"javaDocRelatedHeading"));
 				}
 				String maxLength = AnnotationsHelper.getFirstAnnotationValue(
 						anno, "MaxLength");
 				if (!Utils.isEmpty(maxLength)) {
 					html.append(Constants.HTML_BR
 							+ getTextInDiv("Max Length: " + maxLength,
 									"javaDocRelatedHeading"));
 				}
 				String max = AnnotationsHelper.getFirstAnnotationValue(anno,
 						"Max");
 				String min = AnnotationsHelper.getFirstAnnotationValue(anno,
 						"Min");
 				if (!Utils.isEmpty(max)) {
 					html.append(Constants.HTML_BR
 							+ getTextInDiv("Max Value: " + max,
 									"javaDocRelatedHeading"));
 				}
 				if (!Utils.isEmpty(min)) {
 					html.append(Constants.HTML_BR
 							+ getTextInDiv("Min Value: " + max,
 									"javaDocRelatedHeading"));
 				}
 				maxOccursApp = AnnotationsHelper.getFirstAnnotationValue(anno,
 						"MaxOccurs");
 				minOccursApp = AnnotationsHelper.getFirstAnnotationValue(anno,
 						"MinOccurs");
 
 			}
 			String minOccurs = null;
 			String maxOccurs = null;
 			if (element.getAttributes() != null) {
 				for (Attribute attr : element.getAttributes()) {
 					if ("maxOccurs".equals(attr.getName())) {
 						maxOccurs = attr.getValue();
 					}
 					if ("minOccurs".equals(attr.getName())) {
 						minOccurs = attr.getValue();
 					}
 				}
 			}
 			if (maxOccurs == null) {
 				maxOccurs = maxOccursApp;
 			}
 			if (minOccurs == null) {
 				minOccurs = minOccursApp;
 			}
 			if (maxOccurs == null) {
 				maxOccurs = "?";
 			}
 			if (minOccurs == null) {
 				minOccurs = "?";
 			}
 			if ("unbounded".equals(maxOccurs)) {
 				maxOccurs = "*";
 			}
 			String repeatableString = "";
 			if (maxOccurs == minOccurs) {
 				repeatableString = Constants.HTML_BR + " Occurrence : ["
 						+ maxOccurs + "]";
 			} else {
 				repeatableString = Constants.HTML_BR + " Occurrence : ["
 						+ minOccurs + "..." + maxOccurs + "]";
 			}
 			html
 					.append(getTextInDiv(repeatableString,
 							"javaDocRelatedHeading"));
 			if (anno != null) {
 				List<ParsedAnnotationTag> callInfos = AnnotationsHelper
 						.getCallInfo(anno);
 				if (callInfos != null) {
 					for (ParsedAnnotationTag callInfo : callInfos) {
 						String maxOccursCall = AnnotationsHelper
 								.getFirstCallInfoTagValue(callInfo, "MaxOccurs");
 						String minOccursCall = AnnotationsHelper
 								.getFirstCallInfoTagValue(callInfo, "MinOccurs");
 
 						if (!Utils.isEmpty(minOccursCall)
 								&& !Utils.isEmpty(maxOccursCall)) {
 							if (minOccursCall == null) {
 								minOccurs = "?";
 							}
 							if ("unbounded".equals(maxOccursCall)) {
 								maxOccurs = "*";
 							}
 							repeatableString = "";
 							if (maxOccurs == minOccurs) {
 								repeatableString = " Occurrence : ["
 										+ maxOccurs + "]";
 							} else {
 								repeatableString = " Occurrence : ["
 										+ minOccurs + "..." + maxOccurs + "]";
 							}
 							if (callInfo.getChildren().get("AllCalls") != null) {
 
 								html.append(getTextInDiv("For All Calls"
 										+ repeatableString,
 										"javaDocRelatedHeading"));
 							} else {
 								String value = AnnotationsHelper
 										.getFirstCallInfoTagValue(callInfo,
 												"AllCallsExcept");
 								if (!Utils.isEmpty(value)) {
 									html.append(getTextInDiv(
 											"For All Calls Except " + value
 													+ repeatableString,
 											"javaDocRelatedHeading"));
 								}
 							}
 							List<ParsedAnnotationTag> calls = AnnotationsHelper
 									.getCallInfoChildren(callInfo, "CallName");
 							if (calls != null) {
 								String callString = "For Calls ";
 								Iterator<ParsedAnnotationTag> callIter = calls
 										.iterator();
 								while (callIter.hasNext()) {
 									ParsedAnnotationTag call = callIter.next();
 									callString = callString
 											+ call.getTagValue();
 									if (callIter.hasNext()) {
 										callString = callString + ",";
 									}
 								}
 								html.append(getTextInDiv(callString
 										+ repeatableString,
 										"javaDocRelatedHeading"));
 							}
 						}
 					}
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * Process related info.
 	 */
 	private void processRelatedInfo(StringBuffer html, WSDLDocInterface doc,
 			ParsedAnnotationInfo appInfoElem) {
 		processOccurances(html, appInfoElem);
 		processSeeLinks(html, appInfoElem);
 		// process any RelatedCalls block
 		String relCallsElem = AnnotationsHelper.getFirstAnnotationValue(
 				appInfoElem, "RelatedCalls");
 		List<String> relatedCalls = null;
 		if (relCallsElem != null) {
 			relatedCalls = Utils.commaSepLineToList(relCallsElem);
 		}
 		// form the Related Calls material, but do not yet add it
 		StringBuffer relCallsSB = new StringBuffer();
 		if (relatedCalls != null && relatedCalls.size() > 0) {
 			relCallsSB
 					.append("See also the documentation for these calls:<ul>");
 			for (int i = 0; i < relatedCalls.size(); i++) {
 				String otherCall = relatedCalls.get(i);
 				relCallsSB.append("<li>" + otherCall + "</li>");
 			}
 			relCallsSB.append("</ul>");
 		}
 		List<ParsedAnnotationTag> relCallsOtherElems = AnnotationsHelper
 				.getAnnotationTag(appInfoElem, "RelatedCallsOther");
 		if (relCallsOtherElems != null) {
 			// for each RelatedCallsOther block that was found...
 			for (int j = 0; j < relCallsOtherElems.size(); j++) {
 				ParsedAnnotationTag relCallsOtherElem = relCallsOtherElems
 						.get(j);
 
 				// it should identify the apiNickname of the other service that
 				// has
 				// calls
 				// related to this call
 
 				String otherApi = AnnotationsHelper.getSingleAnnoChildVal(
 						relCallsOtherElem, "ApiNickname");
 
 				String otherApiURL = AnnotationsHelper.getSingleAnnoChildVal(
 						relCallsOtherElem, "ApiCallRefBaseURL");
 
 				List<String[]> relCalls = new ArrayList<String[]>();
 
 				// it should have a list of names of related calls
 				List<ParsedAnnotationTag> relCallBlocks = relCallsOtherElem
 						.getChildren().get("RelatedCall");
 
 				for (int k = 0; k < relCallBlocks.size(); k++) {
 					ParsedAnnotationTag relCallElem = relCallBlocks.get(k);
 
 					String callNameOther = AnnotationsHelper
 							.getSingleAnnoChildVal(relCallElem, "Name");
 
 					String summOther = AnnotationsHelper.getSingleAnnoChildVal(
 							relCallElem, "Summary");
 
 					// form an array of these two
 					relCalls.add(new String[] { callNameOther, summOther });
 					relCallsSB
 							.append("See also the documentation for these calls (in the "
 									+ "<a href=\""
 									+ otherApiURL
 									+ "/index.html\">"
 									+ otherApi
 									+ "</a> API ):" + "<ul>");
 
 					relCallsSB.append("<li>" + "<a href=\"" + otherApiURL + "/"
 							+ callNameOther + ".html\">" + callNameOther
 							+ "</a> - " + summOther + "</li>");
 
 					relCallsSB.append("</ul>");
 				}
 
 			}
 		}
 
 		// if there is either See Also material OR Related Calls info,
 		// inject a heading, then add the material
 		if (!relCallsSB.toString().equals("")) {
 			html.append(Constants.HTML_BR
 					+ Constants.HTML_BR
 					+ getTextInDiv("Related Information: ",
 							"javaDocRelatedHeading"));
 			if (!relCallsSB.toString().equals("")) {
 				html.append(relCallsSB.toString());
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ebayopensource.turmeric.tools.annoparser.outputformatter.DocHandler
 	 * #handleXsdDoc
 	 * (org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface,
 	 * java.lang.String)
 	 */
 
 	public void generateXsdOutput(XSDDocInterface xsdDoc,
 			OutputGenaratorParam outputGenaratorParam) {
 		// Java Doc is not available for XSD
 		return;
 
 	}
 
 	public void createTreeFiles() throws OutputFormatterException {
 		List<XSDDocInterface> allPackages = new ArrayList<XSDDocInterface>();
 		for (Map.Entry<String, List<XSDDocInterface>> entry : packageDocMap
 				.entrySet()) {
 			currentPackageName = entry.getKey();
 			List<XSDDocInterface> list = entry.getValue();
 			for (XSDDocInterface xsd : list) {
 				List<ComplexType> cTypes = xsd.getAllComplexTypes();
 				for (ComplexType cType : cTypes) {
 					cType.setPackageName(((WSDLDocInterface)xsd).getPackageName());
 				}
 			}
 
 			writePackageTree(list, currentPackageName, false);
 
 			allPackages.addAll(entry.getValue());
 		}
 	
 		writePackageTree(allPackages, null, true);
 
 	}
 
 	private void createIndexFiles() throws OutputFormatterException {
 		Map<String, IndexerDataObject> completeMap = getCompleteIndexerMap();
 		StringBuffer keyLinks = getKeyLinks(completeMap);
 		int i = 1;
 		for (Map.Entry<String, IndexerDataObject> entry : completeMap
 				.entrySet()) {
 			StringBuffer html = new StringBuffer();
 			html.append(HtmlUtils.getStartTags("index-" + i, "../"));
 			Map<String, String> replacementMap=new HashMap<String, String>();
 			replacementMap.put("REL_PATH", "..");
 			replacementMap.put("TREE_REL_PATH", "..");
 			replacementMap.put("KEY_LINKS", keyLinks.toString());
 			buildHeader(html,replacementMap,"indexHeader");
 			html.append("<h2><b>" + entry.getKey() + "</b></h2><dl>");
 			List<IndexerBaseDataObject> data = entry.getValue()
 					.getDataObjects();
 			Collections.sort(data);
 			for (IndexerBaseDataObject dataObj : data) {
 				if (!(dataObj instanceof IndexerType)) {
 					String baseHref = "../" + dataObj.getPackageName();
 					String typeDesc = "";
 					String baseTitleDesc = "";
 					String baseName = "";
 					String baseDoc = "";
 					String inPageHref = dataObj.getBaseName();
 					if (dataObj instanceof IndexerOperationHolder) {
 						baseHref = baseHref + SEPARATOR
 								+ dataObj.getServiceName() + Constants.DOT_HTML;
 						typeDesc = "Operation in ";
 						baseName = dataObj.getServiceName();
 						baseTitleDesc = typeDesc + baseName;
 						IndexerOperationHolder opHolder = (IndexerOperationHolder) dataObj;
 						if (opHolder.getOperation().getAnnotations() != null
 								&& opHolder.getOperation().getAnnotations()
 										.getDocumentation() != null) {
 							baseDoc = opHolder.getOperation().getAnnotations()
 									.getDocumentation();
 						}
 					}
 					if (dataObj instanceof IndexerElementHolder) {
 						IndexerElementHolder elemHolder = (IndexerElementHolder) dataObj;
 						if (elemHolder.isReqResp()) {
 							baseHref = baseHref + SEPARATOR
 									+ dataObj.getServiceName()
 									+ Constants.DOT_HTML;
 							if (elemHolder.isInput()) {
 								typeDesc = "Input for Operation in ";
 							} else {
 								typeDesc = "Output for Operation in ";
 							}
 							baseName = dataObj.getServiceName();
 							baseTitleDesc = typeDesc + baseName;
 							inPageHref = elemHolder.getOperationHolder()
 									.getBaseName();
 						} else {
 							baseHref = baseHref
 									+ TYPES
 									+ elemHolder.getElement()
 											.getContainerComplexType()
 											.getName() + Constants.DOT_HTML;
 							if (elemHolder.getElement() instanceof Element) {
 								typeDesc = "Field in Complex Type ";
 							} else {
 								typeDesc = "Attribute in Complex Type ";
 							}
 							baseName = elemHolder.getElement()
 									.getContainerComplexType().getName();
 							baseTitleDesc = typeDesc + baseName;
 						}
 						if (elemHolder.getElement().getAnnotationInfo() != null
 								&& elemHolder.getElement().getAnnotationInfo()
 										.getDocumentation() != null) {
 							baseDoc = elemHolder.getElement()
 									.getAnnotationInfo().getDocumentation();
 						}
 					}
 					if (dataObj instanceof IndexerEnumValueElements) {
 						IndexerEnumValueElements elemHolder = (IndexerEnumValueElements) dataObj;
 						baseHref = baseHref + TYPES
 								+ elemHolder.getEnumElem().getType()
 								+ Constants.DOT_HTML;
 
 						typeDesc = "Enumeration in Simple Type ";
 						baseName = elemHolder.getEnumElem().getType();
 						baseTitleDesc = typeDesc + baseName;
 
 						if (elemHolder.getEnumElem().getAnnotations() != null
 								&& elemHolder.getEnumElem().getAnnotations()
 										.getDocumentation() != null) {
 							baseDoc = elemHolder.getEnumElem().getAnnotations()
 									.getDocumentation();
 						}
 					}
 					html.append("<dt><a href='" + baseHref + "#" + inPageHref
 							+ "'><b>" + dataObj.getBaseName() + "</b></a> -"
 							+ typeDesc + "<a href='" + baseHref + "' title='"
 							+ baseTitleDesc + "'>" + baseName + "</a></dt><dd>"
 							+ baseDoc + "</dd>");
 				}
 			}
 			html.append("</dl>");
 			addFooter(html,replacementMap,"indexHeader");
 			html.append(HtmlUtils.getEndTags());
 			writeFile(html, getCurrentOutputDir() + "index-files", "index-" + i
 					+ ".html");
 			i = i + 1;
 		}
 		createDeprecationFile(completeMap);
 	}
 
 	private StringBuffer getKeyLinks(Map<String, IndexerDataObject> completeMap) {
 		StringBuffer keyLinks = new StringBuffer();
 		int i = 1;
 		for (String str : completeMap.keySet()) {
 			keyLinks.append("<a href='index-" + i + ".html'>" + str
 					+ "</a>&nbsp;");
 			i = i + 1;
 		}
 		return keyLinks;
 	}
 
 	private Map<String, IndexerDataObject> getCompleteIndexerMap()
 			throws OutputFormatterException {
 		Map<String, IndexerDataObject> completeMap = new TreeMap<String, IndexerDataObject>();
 		for (Map.Entry<String, List<XSDDocInterface>> entry : packageDocMap
 				.entrySet()) {
 			List<XSDDocInterface> wsdlDoc = (List<XSDDocInterface>) entry
 					.getValue();
 			for (XSDDocInterface doc : wsdlDoc) {
 				WSDLDocInterface wsdl = (WSDLDocInterface) doc;
 				for (ComplexType complexType : doc.getAllComplexTypes()) {
 					for (Element element : complexType.getChildElements()) {
 						if (element.getContainerComplexType() != null
 								&& element.getContainerComplexType().getName()
 										.equals(complexType.getName())) {
 							addElementToIndexMap(completeMap, element, wsdl
 									.getServiceName(), entry.getKey());
 						}
 					}
 					if (complexType.getSimpleAttributeContent() != null) {
 						for (Element element : complexType
 								.getSimpleAttributeContent()) {
 							addElementToIndexMap(completeMap, element, wsdl
 									.getServiceName(), entry.getKey());
 						}
 					}
 					IndexerType type = new IndexerType();
 					type.setServiceName(wsdl.getServiceName());
 					type.setPackageName(entry.getKey());
 					type.setType(complexType);
 				}
 				for (SimpleType simpleType : doc.getAllSimpleTypes()) {
 					IndexerType type = new IndexerType();
 					type.setServiceName(wsdl.getServiceName());
 					type.setPackageName(entry.getKey());
 					type.setType(simpleType);
 				}
 
 				for (OperationHolder oph : wsdl.getAllOperations()) {
 					IndexerOperationHolder inOph = new IndexerOperationHolder();
 					inOph.setOperation(oph);
 					inOph.setServiceName(wsdl.getServiceName());
 					inOph.setPackageName(entry.getKey());
 					String firstChar = getFirstChar(oph.getName());
 					IndexerDataObject index = completeMap.get(firstChar);
 					if (index == null) {
 						index = new IndexerDataObject();
 						completeMap.put(firstChar, index);
 					}
 					index.addDataObjects(inOph);
 					setReqRespElems(completeMap, oph, inOph);
 				}
 				for (EnumElement enumElem : doc.getAllEnums()) {
 					IndexerEnumValueElements indexEnumElem = new IndexerEnumValueElements();
 					indexEnumElem.setValue(enumElem.getValue());
 					indexEnumElem.setEnumElem(enumElem);
 					indexEnumElem.setServiceName(wsdl.getServiceName());
 					indexEnumElem.setPackageName(entry.getKey());
 					String firstChar = getFirstChar(enumElem.getValue());
 					IndexerDataObject index = completeMap.get(firstChar);
 					if (index == null) {
 						index = new IndexerDataObject();
 						completeMap.put(firstChar, index);
 					}
 					index.addDataObjects(indexEnumElem);
 				}
 			}
 		}
 		return completeMap;
 	}
 
 	private void setReqRespElems(Map<String, IndexerDataObject> completeMap,
 			OperationHolder oph, IndexerOperationHolder inOph) {
 		for (Element element : oph.getInputTypes()) {
 			IndexerElementHolder elementHolder = new IndexerElementHolder();
 			elementHolder.setReqResp(true);
 			elementHolder.setInput(true);
 			elementHolder.setServiceName(inOph.getServiceName());
 			elementHolder.setPackageName(inOph.getPackageName());
 			elementHolder.setOperationHolder(inOph);
 			elementHolder.setElement(element);
 			String elemName = element.getName();
 			String firstChar = getFirstChar(elemName);
 			IndexerDataObject index = completeMap.get(firstChar);
 			if (index == null) {
 				index = new IndexerDataObject();
 				completeMap.put(firstChar, index);
 			}
 			index.addDataObjects(elementHolder);
 		}
 		for (Element element : oph.getOutputTypes()) {
 			IndexerElementHolder elementHolder = new IndexerElementHolder();
 			elementHolder.setReqResp(true);
 			elementHolder.setInput(false);
 			elementHolder.setServiceName(inOph.getServiceName());
 			elementHolder.setPackageName(inOph.getPackageName());
 			elementHolder.setOperationHolder(inOph);
 			elementHolder.setElement(element);
 			String elemName = element.getName();
 			String firstChar = getFirstChar(elemName);
 			IndexerDataObject index = completeMap.get(firstChar);
 			if (index == null) {
 				index = new IndexerDataObject();
 				completeMap.put(firstChar, index);
 			}
 			index.addDataObjects(elementHolder);
 		}
 	}
 
 	private void addElementToIndexMap(
 			Map<String, IndexerDataObject> completeMap, Element element,
 			String serviceName, String packageName) {
 		IndexerElementHolder elementHolder = new IndexerElementHolder();
 		elementHolder.setReqResp(false);
 		elementHolder.setElement(element);
 		elementHolder.setServiceName(serviceName);
 		elementHolder.setPackageName(packageName);
 		String elemName = element.getName();
 
 		String firstChar = getFirstChar(elemName);
 		IndexerDataObject index = completeMap.get(firstChar);
 		if (index == null) {
 			index = new IndexerDataObject();
 			completeMap.put(firstChar, index);
 		}
 		index.addDataObjects(elementHolder);
 	}
 
 	private String getFirstChar(String elemName) {
 		if (elemName == null) {
 			return "_";
 		}
 		char ch = elemName.charAt(0);
 		String firstChar = null;
 		if (Character.isDigit(ch)) {
 			firstChar = "#";
 		} else {
 			firstChar = String.valueOf(ch).toUpperCase();
 		}
 		return firstChar;
 	}
 
 	private void createDeprecationFile(
 			Map<String, IndexerDataObject> completeMap)
 			throws OutputFormatterException {
 		Map<IndexerType, String> deprecatedTypes = new TreeMap<IndexerType, String>();
 		Map<IndexerElementHolder, String> deprecatedElements = new TreeMap<IndexerElementHolder, String>();
 		Map<IndexerOperationHolder, String> deprecatedOperations = new TreeMap<IndexerOperationHolder, String>();
 		Map<IndexerEnumValueElements, String> deprecatedEnums = new TreeMap<IndexerEnumValueElements, String>();
 		for (Map.Entry<String, IndexerDataObject> entry : completeMap
 				.entrySet()) {
 			List<IndexerBaseDataObject> data = entry.getValue()
 					.getDataObjects();
 			Collections.sort(data);
 			for (IndexerBaseDataObject dataObj : data) {
 				if (dataObj instanceof IndexerType) {
 					StringBuffer depreDetails = AnnotationsHelper
 							.processDeprication(((IndexerType) dataObj)
 									.getType().getAnnotations());
 					if (depreDetails != null) {
 						deprecatedTypes.put(((IndexerType) dataObj),
 								depreDetails.toString());
 					}
 				}
 				if (dataObj instanceof IndexerElementHolder) {
 					StringBuffer depreDetails = AnnotationsHelper
 							.processDeprication(((IndexerElementHolder) dataObj)
 									.getElement().getAnnotationInfo());
 					if (depreDetails != null) {
 						deprecatedElements.put(
 								((IndexerElementHolder) dataObj), depreDetails
 										.toString());
 					}
 				}
 				if (dataObj instanceof IndexerOperationHolder) {
 					StringBuffer depreDetails = AnnotationsHelper
 							.processDeprication(((IndexerOperationHolder) dataObj)
 									.getOperation().getAnnotations());
 					if (depreDetails == null) {
 						OperationHolder oper = ((IndexerOperationHolder) dataObj)
 								.getOperation();
 						for (Element input : oper.getInputTypes()) {
 							depreDetails = AnnotationsHelper
 									.processDeprication(input
 											.getAnnotationInfo());
 						}
 						if (depreDetails == null) {
 							for (Element input : oper.getOutputTypes()) {
 								depreDetails = AnnotationsHelper
 										.processDeprication(input
 												.getAnnotationInfo());
 							}
 						}
 					}
 					if (depreDetails != null) {
 						deprecatedOperations.put(
 								((IndexerOperationHolder) dataObj),
 								depreDetails.toString());
 					}
 				}
 				if (dataObj instanceof IndexerEnumValueElements) {
 					StringBuffer depreDetails = AnnotationsHelper
 							.processDeprication(((IndexerEnumValueElements) dataObj)
 									.getEnumElem().getAnnotations());
 					if (depreDetails != null) {
 						deprecatedEnums.put(
 								((IndexerEnumValueElements) dataObj),
 								depreDetails.toString());
 					}
 				}
 			}
 		}
 		StringBuffer html = new StringBuffer();
 		html.append(HtmlUtils.getStartTags("Deprecation Details", null));
 		Map<String, String> replacementMap=new HashMap<String, String>();
 		replacementMap.put("REL_PATH", ".");
 		replacementMap.put("TREE_REL_PATH", ".");
 		replacementMap.put("KEY_LINKS", "");
 		buildHeader(html,replacementMap,"indexHeader");
 		html.append(getTextInDiv("Deprecation Details", "pageHeading"));
 		html.append(Constants.HTML_BR);
 		if (!(deprecatedTypes.isEmpty() && deprecatedElements.isEmpty()
 				&& deprecatedEnums.isEmpty() && deprecatedOperations.isEmpty())) {
 			if (!deprecatedOperations.isEmpty()) {
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th class=\"JavadocTableHeaders\">");
 				html.append("Deprecated Operations");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				for (Map.Entry<IndexerOperationHolder, String> entry : deprecatedOperations
 						.entrySet()) {
 					IndexerOperationHolder op = entry.getKey();
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 
 					html
 							.append("<a href='"
 									+ op.getPackageName()
 									+ op.getBaseName()
 									+ Constants.DOT_HTML
 									+ "' title='Operation in "
 									+ op.getPackageName()
 									+ "'>"
 									+ op.getBaseName()
 									+ "</a>"
 									+ "<br>"
 									+ "<i>"
 									+ entry.getValue() + "</i>&nbsp;");
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 
 			}
 			if (!deprecatedTypes.isEmpty()) {
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th class=\"JavadocTableHeaders\">");
 				html.append("Deprecated Types");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				for (Map.Entry<IndexerType, String> entry : deprecatedTypes
 						.entrySet()) {
 					IndexerType op = entry.getKey();
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 
 					html
 							.append("<a href='"
 									+ op.getPackageName()
 									+ "/types/"
 									+ op.getBaseName()
 									+ Constants.DOT_HTML
 									+ "' title='Type in "
 									+ op.getPackageName()
 									+ "'>"
 									+ op.getBaseName()
 									+ "</a>"
 									+ "<br>"
 									+ "<i>"
 									+ entry.getValue() + "</i>&nbsp;");
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 			}
 
 			if (!deprecatedElements.isEmpty()) {
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th class=\"JavadocTableHeaders\">");
 				html.append("Deprecated Fields");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 				for (Map.Entry<IndexerElementHolder, String> entry : deprecatedElements
 						.entrySet()) {
 
 					IndexerElementHolder elemHolder = entry.getKey();
 					String baseHref = elemHolder.getPackageName();
 					String typeDesc = "";
 					String baseTitleDesc = "";
 					String baseName = "";
 					String inPageHref = elemHolder.getBaseName();
 					if (elemHolder.isReqResp()) {
 						baseHref = baseHref + SEPARATOR
 								+ elemHolder.getServiceName()
 								+ Constants.DOT_HTML;
 						if (elemHolder.isInput()) {
 							typeDesc = "Input for Operation in ";
 						} else {
 							typeDesc = "Output for Operation in ";
 						}
 						baseName = elemHolder.getServiceName();
 						baseTitleDesc = typeDesc + baseName;
 						inPageHref = elemHolder.getOperationHolder()
 								.getBaseName();
 					} else {
 						baseHref = baseHref
 								+ TYPES
 								+ elemHolder.getElement()
 										.getContainerComplexType().getName()
 								+ Constants.DOT_HTML;
 						if (elemHolder.getElement() instanceof Element) {
 							typeDesc = "Field in Complex Type ";
 						} else {
 							typeDesc = "Attribute in Complex Type ";
 						}
 						baseName = elemHolder.getElement()
 								.getContainerComplexType().getName();
 						baseTitleDesc = typeDesc + baseName;
 					}
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 
 					html.append("<a href='" + baseHref + "#" + inPageHref
 							+ "'><b>" + elemHolder.getBaseName() + "</b></a> -"
 							+ typeDesc + "<a href='" + baseHref + "' title='"
 							+ baseTitleDesc + "'>" + baseName + "</a>");
 					html
 							.append("<br>"
 									+ "<i>"
 									+ entry.getValue() + "</i>&nbsp;");
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 			}
 			if (!deprecatedEnums.isEmpty()) {
 				html.append(getTableTagWithStyle("JavadocTable"));
 				html.append(getTableRowTagWithStyle("JavadocTableTr"));
 				html.append("<th class=\"JavadocTableHeaders\">");
 				html.append("Deprecated Enumerations");
 				html.append(Constants.HTML_TABLE_TH_END);
 				html.append(Constants.HTML_TABLE_TR_END);
 
 				
 				for (Map.Entry<IndexerEnumValueElements, String> entry : deprecatedEnums
 						.entrySet()) {
 					html.append(getTableRowTagWithStyle("JavadocTableTr"));
 					html.append(getTableDataTagWithStyle("JavadocTableTd"));
 					IndexerEnumValueElements elemHolder = entry.getKey();
 					String baseHref = elemHolder.getPackageName();
 					String typeDesc = "";
 					String baseTitleDesc = "";
 					String baseName = "";
 					String inPageHref = elemHolder.getBaseName();
 					baseHref = baseHref + TYPES
 							+ elemHolder.getEnumElem().getType()
 							+ Constants.DOT_HTML;
 
 					typeDesc = "Enumeration in Simple Type ";
 					baseName = elemHolder.getEnumElem().getType();
 					baseTitleDesc = typeDesc + baseName;
 					html.append("<a href='" + baseHref + "#" + inPageHref
 							+ "'><b>" + elemHolder.getBaseName() + "</b></a> -"
 							+ typeDesc + "<a href='" + baseHref + "' title='"
 							+ baseTitleDesc + "'>" + baseName + "</a>");
 					html
 							.append("<br>"
 									+ "<i>"
 									+ entry.getValue() + "</i>");
 					html.append(Constants.HTML_TABLE_TD_END);
 					html.append(Constants.HTML_TABLE_TR_END);
 				}
 			}
 
 		} else {
 			html.append("<h2>None of the enitites are Deprecated</h2>");
 		}
 
 		addFooter(html,replacementMap,"indexHeader");
 		html.append(HtmlUtils.getEndTags());
 		writeFile(html, getCurrentOutputDir(), "DeprecatedObjects.html");
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ebayopensource.turmeric.tools.annoparser.outputformatter.OutputGenerator
 	 * #completeProcessing()
 	 */
 	public void completeProcessing() throws OutputFormatterException {
 		if (packageServicesMap != null && !packageServicesMap.isEmpty()) {
 
 			createAllPackagesFile();
 			createAllPackagesDescriptionFile();
 			createIndividualPackageFiles();
 			createAllClassesFile();
 			createIndexFile();
 			createTreeFiles();
 			createIndexFiles();
 		}
 	}
 
 	/**
 	 * Gets the current output dir.
 	 * 
 	 * @return the current output dir
 	 */
 	private String getCurrentOutputDir() {
 		String outputDir = Context.getContext().getOutputDir();
 		if (outputGenaratorParam != null
 				&& outputGenaratorParam.getOutputDir() != null) {
 			outputDir = outputGenaratorParam.getOutputDir();
 		}
 		return outputDir + File.separator + "wsdlApiSpecDocs" + File.separator;
 	}
 }
