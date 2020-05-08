 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.parser.impl;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationTag;
 import org.ebayopensource.turmeric.tools.annoparser.parser.AnnotationParser;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * This Class is the Default Annotation Parser for all the Annotation tags.
  * This Class an also be used to any annotation tag, with any level of children.
  *
  * @author sdaripelli
  */
 public class DefaultAnnotationParser implements AnnotationParser {
 
 	
 	private final static String CLASS_NAME=DefaultAnnotationParser.class.getClass().getName();  
 	Logger logger  = Logger.getLogger(CLASS_NAME);
 	/**
 	 * Parses the annotation.
 	 * This is the main implementation method of the Custom Annotation Parser,
 	 * 
 	 * @param elem
 	 *            the element in the DOM tree corresponding to a Custom Annotation tag. 
 	 * @return the parsed eBay custom annotation data
 	 */
 	public ParsedAnnotationTag parseAnnotation(final Element elem) {
 		
 		logger.log(Level.FINER, "Entering parseAnnotation in EbayAnnotationParser",elem);
 		final ParsedAnnotationTag retData = createAnnotationData(elem);
 		Node firstChild=getFirstChild(elem);
 		if (firstChild!=null &&getFirstChild(elem).getNodeType() == Node.ELEMENT_NODE) {
 			this.visit(elem, 0, retData);
 		} 
 		logger.log(Level.FINER, "Exiting parseAnnotation in EbayAnnotationParser",retData);
 		return retData;
 	}
 
 	/**
 	 * This method is a Recursive method which recursively, visits every child and sibling on the given Node from the specified 
 	 * level. and this created ParsedAnnotationTag for each tag, and associates it with the Parent ParsedAnnotationTag Object.
 	 * 
 	 * @param node
 	 *            the node
 	 * @param level
 	 *            the level
 	 * @param parent
 	 *            the parent
 	 */
 	private void visit(final Node node, final int level,
 			final ParsedAnnotationTag parent) {
 		for (Node childNode = getFirstChild(node); childNode != null;) {
 			Node nextChild = getNextSibling(childNode);
 			if (getFirstChild(childNode)!=null && getFirstChild(childNode).getNodeType() == Node.ELEMENT_NODE) {
 				this.visit(childNode, level + 1, addChild(parent, childNode));
 			} else {
 				addChild(parent, childNode);
 			}
 			childNode = nextChild;
 		}
 
 	}
 
 	/**
 	 * Adds a child node to Parent.
 	 *
 	 * @param parent the parent
 	 * @param childNode the child node
 	 * @return new child data.
 	 */
 	private ParsedAnnotationTag addChild(final ParsedAnnotationTag parent,
 			Node childNode) {
 		logger.log(Level.FINER, "Entering addChild in EbayAnnotationParser",new Object[]{parent,childNode});
 		ParsedAnnotationTag retData = createAnnotationData(childNode);
 		parent.addChild(retData);
 		logger.log(Level.FINER, "Exiting addChild in EbayAnnotationParser",retData);
 		return retData;
 	}
 
 	/**
 	 * Creates the annotation data.
 	 *
 	 * @param childNode the child node
 	 * @return the ebay annotation data
 	 */
 	private ParsedAnnotationTag createAnnotationData(Node childNode) {
 		logger.log(Level.FINER, "Entering createAnnotationData in EbayAnnotationParser",childNode);
 		ParsedAnnotationTag retData = new ParsedAnnotationTag();
 		retData.setTagName(childNode.getNodeName());
 		final NamedNodeMap attr = childNode.getAttributes();
 		if (attr != null) {
 			for (int temp = 0; temp < attr.getLength(); temp++) {
 				final Node attrNode = attr.item(temp);
 				retData.addAttribute(attrNode.getNodeName(), attrNode
 						.getNodeValue());
 			}
 		}
 		if (getFirstChild(childNode) == null) {
 			retData.setTagValue(null);
 		} else {
 			if (getFirstChild(childNode).getNodeType() == Node.TEXT_NODE) {
 				retData.setTagValue(getFirstChild(childNode).getNodeValue());
 			}
 		}
 		logger.log(Level.FINER, "Exiting createAnnotationData in EbayAnnotationParser",retData);
 		return retData;
 	}
 	
 	/**
 	 * Gets the first child ignoring empty white-space nodes.
 	 * 
 	 * @param node
 	 *            the node
 	 * @return the first child
 	 */
 	private Node getFirstChild(Node node){
 		Node firstChild=node.getFirstChild();
 		if(firstChild!=null &&firstChild.getNodeType()==Node.TEXT_NODE){
 			String value=firstChild.getTextContent();
 			if(value!=null){
 				value=value.replaceAll("\t", "");
 				value=value.replaceAll("\n", "");
 				value=value.replaceAll(" ", "");
 				if(Utils.isEmpty(value)){
 					firstChild=getNextSibling(firstChild);
 				}
 			}
 		}
 		logger.log(Level.INFO, "First Child of the Node",firstChild);
 		return firstChild;
 	}
 	
 	/**
 	 * Gets the next sibling ignoring empty white-space nodes.
 	 * 
 	 * @param node
 	 *            the node
 	 * @return the next sibling
 	 */
 	private Node getNextSibling(Node node){
 		Node sib=node.getNextSibling();
		if(sib !=null && Node.COMMENT_NODE==sib.getNodeType()){
			sib=getNextSibling(sib);
		}
		if(sib!=null && sib.getNodeType()==Node.TEXT_NODE ){
 			String value=sib.getTextContent();
 			if(value!=null){
 				value=value.replaceAll("\t", "");
 				value=value.replaceAll("\n", "");
 				value=value.replaceAll(" ", "");
 				if(Utils.isEmpty(value)){
 					sib=getNextSibling(sib);
 				}
 			}
 		}
 		logger.log(Level.INFO, "Next Sibling",sib);
 		return sib;
 	}
 }
