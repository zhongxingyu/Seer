 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.dataobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Representation of schema type element.
  * 
  * @author srengarajan
  */
 
public class Element implements Comparable<Element> {
 
 	/**
 	 * captures the type of the element
 	 */
 	private String type;
 
 	/**
 	 * captures the element name
 	 */
 	private String name;
 
 	private Comment comment;
 
 	public Comment getComment() {
 		return comment;
 	}
 
 	public void setComment(Comment comment) {
 		this.comment = comment;
 	}
 
 	/**
 	 * captures the attributes of this element
 	 */
 	private List<Attribute> attributes = new ArrayList<Attribute>();
 
 	/**
 	 * Gets the attributes.
 	 * 
 	 * @return collection of this elements attributes
 	 */
 	public List<Attribute> getAttributes() {
 		return attributes;
 	}
 
 	/**
 	 * setter for collection of attributes.
 	 * 
 	 * @param attributes
 	 *            the new attributes
 	 */
 	public void setAttributes(List<Attribute> attributes) {
 		this.attributes = attributes;
 	}
 
 	/**
 	 * captures the annotation associated with the element
 	 */
 	private ParsedAnnotationInfo annotation;
 
 	/**
 	 * captures the container complex type if any. Useful relationship to
 	 * navigate the graph Note that this is different from the type of the
 	 * element
 	 */
 	private ComplexType containerComplexType;
 
 	/**
 	 * Gets the container complex type.
 	 * 
 	 * @return the container complex type
 	 */
 	public ComplexType getContainerComplexType() {
 		return containerComplexType;
 	}
 
 	/**
 	 * Sets the container complex type.
 	 * 
 	 * @param myComplexType
 	 *            the new container complex type
 	 */
 	public void setContainerComplexType(ComplexType myComplexType) {
 		this.containerComplexType = myComplexType;
 	}
 
 	/**
 	 * Gets the type.
 	 * 
 	 * @return type of the element
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the type.
 	 * 
 	 * @param type
 	 *            setter for type
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * Gets the name.
 	 * 
 	 * @return getter for element name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name.
 	 * 
 	 * @param name
 	 *            the new name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Gets the annotation info.
 	 * 
 	 * @return handle to ParsedAnnotationInfo object which is the parsed
 	 *         annotation data including documentation and appInfo
 	 */
 	public ParsedAnnotationInfo getAnnotationInfo() {
 		return annotation;
 	}
 
 	/**
 	 * Sets the annotation info.
 	 * 
 	 * @param Object
 	 *            the new annotation info
 	 */
 	public void setAnnotationInfo(ParsedAnnotationInfo Object) {
 		this.annotation = Object;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer retVal = new StringBuffer();
 		retVal.append("Element Name: " + name + "\n");
 		retVal.append("Element Type: " + type + "\n");
 		if (comment != null) {
 			retVal.append("Comments : " + "\n");
 			retVal.append("Before Element : " + comment.getPreviousComment()
 					+ "\n");
 			retVal.append("After Element : " + comment.getNextComment() + "\n");
 		}
 		if (annotation != null) {
 			retVal.append("Annotations: \n");
 			retVal.append(annotation.toString());
 		}
 		retVal.append("Attributes: " + "\n");
 		if (attributes != null) {
 			for (Attribute attr : this.attributes) {
 				if (attr != null) {
 					retVal.append(attr.toString() + "\n");
 				}
 			}
 		}
 		return retVal.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	public int compareTo(Element object) {
 		int retVal = this.getName().toUpperCase().compareTo(
 				object.getName().toUpperCase());
 		if (retVal == 0
 				&& (this.getType().equals(object.getType())
 						&& ((this.getContainerComplexType() != null && this
 								.getContainerComplexType().equals(
 										object.getContainerComplexType())) || (object
 						.getContainerComplexType() == null && this
 						.getContainerComplexType() == null)))) {
 			return 0;
 		} else {
 			if (retVal == 0) {
 				retVal = 1;
 			}
 			return retVal;
 		}
 	}
 }
