 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.28 $
 * $Date: 2007-09-10 17:08:35 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.framework.otrunk.otcore.OTClassProperty;
 import org.concord.framework.otrunk.otcore.OTType;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTIDFactory;
 
 /**
  * ObjectTypeHandler
  * Class name and description
  *
  * Date created: Oct 4, 2004
  *
  * @author scott<p>
  *
  */
 public class ObjectTypeHandler extends ResourceTypeHandler
 {
 	OTClass otClass;
 	TypeService typeService;	
 	String objectName = null;
 	String objectClassName = null;
 	String parentObjectName = null;
 	XMLDatabase xmlDB = null;
 	
 	public ObjectTypeHandler(TypeService typeService, XMLDatabase xmlDB)
 	{
 		super("object");
 		this.typeService = typeService;
 		this.xmlDB = xmlDB;
 	}
 	
 	public ObjectTypeHandler(
 		    OTClass otClass,
 			String objectName,
 			String objectClassName,
 			String parentObjectName,
 			TypeService typeService,
 			XMLDatabase xmlDB)
 	{
 		this(typeService, xmlDB);
 		this.otClass = otClass;
 		this.objectName = objectName;
 		this.parentObjectName = parentObjectName;
 		this.objectClassName = objectClassName;
 		
 		if(objectName == null){
 			throw new RuntimeException("Cannot have a null objectName");
 		}
 	}
 	
 	public String getObjectName()
 	{
 		return objectName;
 	}
 	
 	public boolean isObjectReferenceHandler()
 	{
 		// objectName should only be null if this is the objectReferenceHandler 
 		// this handler was created with the 2 arg constructor 
 		return objectName == null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.xml.ResourceTypeHandler#handleElement(org.w3c.dom.Element, java.util.Properties)
 	 */
 	public Object handleElement(OTXMLElement element, String relativePath,
 	        XMLDataObject parent)
 	{
 		if(isObjectReferenceHandler()){
 			String refid = element.getAttributeValue("refid");
 			if(refid != null && refid.length() > 0){
 				return new XMLDataObjectRef(refid, element);
 			}
 		}
 		
 		String idStr = element.getAttributeValue("id");
 		if(idStr != null && idStr.length() <= 0) {
 			idStr = null;
 		}
 
 		String localIdStr = element.getAttributeValue("local_id");
 		if(localIdStr != null && localIdStr.length() <= 0) {
 			localIdStr = null;
 		}
 		
 		XMLDataObject obj = null;
 		try {
 		    if(idStr == null && localIdStr == null && relativePath != null) {
 		        OTID pathId = OTIDFactory.createOTID(relativePath);
 		        // System.err.println(relativePath);
 		        obj = xmlDB.createDataObject(element, pathId);
 		    } else if(idStr == null && localIdStr != null) {
 		        OTID id = xmlDB.getOTIDFromLocalID(localIdStr);  
 		        obj = xmlDB.createDataObject(element, id);
 		    } else {
 		        obj = xmlDB.createDataObject(element, idStr);
 		    }
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		String objRelativePath = relativePath;
 		OTID objId = obj.getGlobalId();
 		if(idStr != null || localIdStr != null) {
 		    objRelativePath = objId.toString(); 
 		}
 
 		OTDataObjectType type = new OTDataObjectType(getClassName());
 		obj.setType(type);
 		
 		List attributes = element.getAttributes();
 		for(Iterator attIter = attributes.iterator(); attIter.hasNext(); ) {
 			OTXMLAttribute attrib = (OTXMLAttribute)attIter.next();
 			String attribName = attrib.getName();
 			if(attribName.equals("id") ||
 					attribName.equals("local_id")) {
 				continue;
 			}
 			
 			if(isObjectReferenceHandler()){
 				System.err.println("Invalid <object> attribute: " +						
 						TypeService.attributePath(attrib));
 				System.err.println("  Only viable <object> attribute is \"refid\"");
 				continue;
 			}
 			
 			try {
 			    
 				Object resValue = handleChildResource(element, attribName, 
 				        attrib.getValue(), objRelativePath, obj, XMLReferenceInfo.ATTRIBUTE,
 				        null);
 				obj.setResource(attribName, resValue);
 				
 				if(xmlDB.isTrackResourceInfo()){
 					XMLReferenceInfo info = obj.getReferenceInfo(attribName);
 					if(info == null){
 						info = new XMLReferenceInfo();
 						obj.setResourceInfo(attribName, info);
 					}
 					info.type = XMLReferenceInfo.ATTRIBUTE;				
 				}
 			} catch (HandleElementException e) {
 				System.err.println(e.getMessage() + " in attribute: " +
 						TypeService.attributePath(attrib));
 			}
 		}
 
 		List content = element.getContent();
 		String previousComment = null;
 		
 		for(Iterator childIter = content.iterator(); childIter.hasNext(); ) {
 		    OTXMLContent childContent = (OTXMLContent)childIter.next();
 		    if(childContent instanceof OTXMLComment){
 		    	previousComment = ((OTXMLComment) childContent).getText();
 		    }
 		    
 		    if(!(childContent instanceof OTXMLElement)){
 		    	continue;
 		    }
 		    
 		    OTXMLElement child = (OTXMLElement) childContent;
 			try {
 				Object resValue = handleChildResource(element, child.getName(), 
 				        child, objRelativePath, obj, XMLReferenceInfo.ELEMENT, previousComment);
 				if(resValue == null) {
                     // this should be an option debug or log message
 					// System.out.println("null resource: " + TypeService.elementPath(child));
 				}
 				String childName = child.getName();
 				obj.setResource(child.getName(),resValue);
 				if(xmlDB.isTrackResourceInfo()){
 					XMLReferenceInfo info = obj.getReferenceInfo(childName);
 					if(info == null){
 						info = new XMLReferenceInfo();
 						obj.setResourceInfo(childName, info);
 					}
 					info.type = XMLReferenceInfo.ELEMENT;									
 				}
 			} catch (HandleElementException e) {
 				System.err.println("error in element: " +
 						TypeService.elementPath(child));
 				e.printStackTrace();				
 			}
 			
 			// Clear the previous comment so it isn't picked up by the next element
 			previousComment = null;
 		}
 		
 		return obj;
 	}
 
 	public String getClassName()
 	{
 		return otClass.getInstanceClass().getName();
 	}
 	
 	/**
 	 * This element comes with extra information.  The name of 
 	 * the element identifies it as a resource in the parentType
 	 * The resource in the parent type has type information.  So
 	 * if the element is "&lt;myText>hi this is my text&lt;/myText>"
 	 * and myText is defined as a "string" in the parentType then
 	 * this element will be turned into a string.
 	 * @param parentElement TODO
 	 * @param parentType
 	 * @param child
 	 * 
 	 * @return
 	 */
 	public Object handleChildResource(OTXMLElement parentElement, String childName, 
 	        Object childObj, String relativeParentPath, XMLDataObject parent, 
 	        int xmlType, String comment)
 		throws HandleElementException
 	{
 		OTClassProperty otProperty = otClass.getProperty(childName);
 		if(otProperty == null) {
			System.err.println("error reading childName: " + childName +
					" in type: " + getObjectName());
 			return null;
 		}
 		OTType otType = otProperty.getType();
 		
 		XMLReferenceInfo resInfo = null;
 		if(xmlDB.isTrackResourceInfo()){
 			resInfo = parent.getReferenceInfo(childName);
 			if(resInfo == null){
 				resInfo = new XMLReferenceInfo();
 				parent.setResourceInfo(childName, resInfo);
 			}
 			resInfo.type = xmlType;
 			
 			resInfo.comment = comment;
 		}
 
 		if(otType instanceof OTClass &&
 				childObj instanceof String){
 		    
 		    // this is an object reference
 			String refid = (String)childObj;
 			if(refid != null && refid.length() > 0){
 				return new XMLDataObjectRef(refid, parentElement);
 			}		    
 		}
 		
 		if(otType instanceof OTClass) {
 			if(!(childObj instanceof OTXMLElement)) {
 				System.err.println("child of type object must be an element or string");
 				return null;
 			}
 			OTXMLElement child = (OTXMLElement)childObj;
 
 			// This allows users to put refid on the attribute element:
 			// like: <OTGraph><xAxis refid="id_of_some_other_object"/></OTGraph>
 			String childRefId = child.getAttributeValue("refid");
 			
 			// If this element doesn't have a reference id then 
 			// then the first child of this element is object 
 			if(childRefId == null) {
 				List children = child.getChildren();
 				if(children.size() < 1) {
 					// empty object tag
 					// this happens a lot in the current xml so
 					// I'm taking this out for now
 					/*
 					System.err.println("empty object field: " + 
 							TypeService.elementPath(child));
 							*/
 					return null;
 				}
 				childObj = children.get(0);
                 if(children.size() > 1) {
                     System.err.println("Warning: Only the first element is returned from " +
                             TypeService.elementPath(child));
                 }
 				String childElementName = ((OTXMLElement)childObj).getName();
 				
 				// Should figure out how to get the OTType for this class name it could be
 				// a shortcut name.  First check if it is a Fully Qualified name.
 				if(childElementName.equals("object")){
 					otType = OTrunkImpl.getOTClass("org.concord.framework.otrunk.OTObject");
 				} else {
 					otType = OTrunkImpl.getOTClass(childElementName);
 
 					if(otType == null){
 						otType = typeService.getClassByShortcut(childElementName);
 					}
 					
 					if(otType == null){
 						throw new IllegalStateException("Can't find OTClass for <" + childElementName + "> check the imports"); 						
 					}
 				}				
 			}			
 		}
 		
 		if(otType == null){
 			throw new IllegalStateException("Can't find otType for property: " + otProperty.getName() + 
 					" on class: " + otClass.getInstanceClass().getName()); 
 		}
 		
 		ResourceTypeHandler resHandler = typeService.getElementHandler(otType);
 		
 		if(resHandler == null){
 			System.err.println("Can't find type handler for: " +
 					otType.getInstanceClass());
 			return null;
 		}
 		
 		if(childObj instanceof String) {
 			if(resHandler instanceof PrimitiveResourceTypeHandler){
 				return ((PrimitiveResourceTypeHandler)resHandler).
 					handleElement((String)childObj);
 			} else {
 				throw new HandleElementException("Can't use an attribute for a non-primitive type");
 			}
 		} else {
 		    String childRelativePath = relativeParentPath + "/" + childName;
 			return resHandler.handleElement((OTXMLElement)childObj, childRelativePath,
 			        parent);
 		}		
 	}
 	
 	public ObjectTypeHandler getParentType()
 	{
 		ObjectTypeHandler parentType = null;
 		if(parentObjectName != null && parentObjectName.length() > 0) {
 
 			parentType = (ObjectTypeHandler)typeService.getElementHandler(parentObjectName);
 			if(parentType == null) {
 				System.err.println("can't find parent: " + parentObjectName +
 						" of: " + getObjectName());
 			}
 			return parentType;
 		}
 		return null;		
 	}
 	
 }
