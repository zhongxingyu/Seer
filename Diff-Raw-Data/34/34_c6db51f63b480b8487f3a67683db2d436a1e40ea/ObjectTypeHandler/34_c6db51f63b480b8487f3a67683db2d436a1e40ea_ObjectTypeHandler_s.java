 /*
  * Last modification information:
 * $Revision: 1.9 $
 * $Date: 2005-01-27 16:45:29 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.concord.otrunk.OTrunkImpl;
 import org.jdom.Attribute;
 import org.jdom.Element;
 
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
 	TypeService typeService;	
 	String objectName = null;
 	String objectClassName = null;
 	String parentObjectName = null;
 	ResourceDefinition [] resources = null;
 	XMLDatabase xmlDB = null;
 	
 	public ObjectTypeHandler(TypeService typeService, XMLDatabase xmlDB)
 	{
 		super("object");
 		this.typeService = typeService;
 		this.xmlDB = xmlDB;
 	}
 	
 	public ObjectTypeHandler(
 			String objectName,
 			String objectClassName,
 			String parentObjectName,
 			ResourceDefinition [] resources,
 			TypeService typeService,
 			XMLDatabase xmlDB)
 	{
 		this(typeService, xmlDB);
 		this.objectName = objectName;
 		this.parentObjectName = parentObjectName;
 		this.resources = resources;
 		this.objectClassName = objectClassName;
 	}
 	
 	public String getObjectName()
 	{
 		return objectName;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.xml.ResourceTypeHandler#handleElement(org.w3c.dom.Element, java.util.Properties)
 	 */
 	public Object handleElement(Element element, Properties elementProps)
 	{
 		String refid = element.getAttributeValue("refid");
 		if(refid != null && refid.length() > 0){
 			return new XMLDataObjectRef(refid, element);
 		}
 				
 		String idStr = element.getAttributeValue("id");
 		if(idStr != null && idStr.length() <= 0) {
 			idStr = null;
 		}
 
 		XMLDataObject obj = null;
 		try {
 			obj = xmlDB.createDataObject(element, idStr);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		obj.setResource(OTrunkImpl.RES_CLASS_NAME, getClassName());
 		
 		List attributes = element.getAttributes();
 		for(Iterator attIter = attributes.iterator(); attIter.hasNext(); ) {
 			Attribute attrib = (Attribute)attIter.next();
 			String attribName = attrib.getName();
 			if(attribName.equals("id") ||
 					attribName.equals("refid") ||
 					attribName.equals("local_id")) {
 				continue;
 			}
 			
 			try {
 				Object resValue = handleChildResource(attribName, attrib.getValue());
 				obj.setResource(attribName, resValue);
 			} catch (HandleElementException e) {
 				System.err.println("error in attribute: " +
 						TypeService.attributePath(attrib));
 				e.printStackTrace();
 			}
 		}
 		
 		List children = element.getChildren();		
 		for(Iterator childIter = children.iterator(); childIter.hasNext(); ) {
 			Element child = (Element)childIter.next();
 
 			try {
 				Object resValue = handleChildResource(child.getName(), child);
 				obj.setResource(child.getName(),resValue);
 			} catch (HandleElementException e) {
 				System.err.println("error in element: " +
 						TypeService.elementPath(child));
 				e.printStackTrace();				
 			}
 		}
 		
 		return obj;
 	}
 
 	public String getClassName()
 	{
 		return objectClassName;
 	}
 	
 	/**
 	 * This element comes with extra information.  The name of 
 	 * the element identifies it as a resource in the parentType
 	 * The resource in the parent type has type information.  So
 	 * if the element is "&lt;myText>hi this is my text&lt;/myText>"
 	 * and myText is defined as a "string" in the parentType then
 	 * this element will be turned into a string.
 	 * 
 	 * @param parentType
 	 * @param child
 	 * @return
 	 */
 	public Object handleChildResource(String childName, Object childObj)
 		throws HandleElementException
 	{
 		Properties elementProps;
 
 		ResourceDefinition resourceDef = getResourceDefinition(childName);
 		
 		if(resourceDef == null) {
 			System.err.println("error reading childName: " + childName +
 					" in type: " + getObjectName());
 			return null;
 		}
 		elementProps = getResourceProperties(resourceDef);
 		String resPrimitiveType = resourceDef.getType();	
 
 		String resourceType = resPrimitiveType;
 		if(resPrimitiveType.equals("object")) {
 			if(!(childObj instanceof Element)) {
 				System.err.println("child of type object must be an element");
 				return null;
 			}
 			Element child = (Element)childObj;
 			
 			String childRefId = child.getAttributeValue("refid");
 			
 			// If this element doesn't have a reference id then 
 			// then the first child of this element is object 
 			if(childRefId == null) {
 				List children = child.getChildren();
 				if(children.size() != 1) {
 					// invalid object tag
 					System.err.println("empty object field: " + 
 							TypeService.elementPath(child));
 					return null;
 				}
 				childObj = children.get(0);
 				resourceType = ((Element)childObj).getName();
 			}			
 		}
 		
 		ResourceTypeHandler resHandler = typeService.getElementHandler(resourceType);
 		
 		if(resHandler == null){
 			System.err.println("Can't find type handler for: " +
 					resourceType);
 			return null;
 		}
 		
 		if(childObj instanceof String) {
 			if(resHandler instanceof PrimitiveResourceTypeHandler){
 				return ((PrimitiveResourceTypeHandler)resHandler).
				handleElement((String)childObj, elementProps);
 			} else {
 				throw new HandleElementException("Can't use an attribute for a non-primitive type");
 			}
 		} else {
 			return resHandler.handleElement((Element)childObj, elementProps);
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
 	
 	public ResourceDefinition [] getResourceDefinitions()
 	{
 		return resources;
 	}
 	
 	public ResourceDefinition getResourceDefinition(String name)
 	{	
 		if(resources == null) {
 			throw new RuntimeException("null resource defs in: " + objectName);
 		}
 		for(int i=0; i<resources.length; i++) {
 			if(resources[i].getName().equals(name)) {
 				return resources[i];
 			}
 		}
 
 		ObjectTypeHandler parentType = getParentType(); 
 		if(parentType == null) {
 			return null;
 		}
 
 		return parentType.getResourceDefinition(name);		
 	}
 	
 	public Properties getResourceProperties(ResourceDefinition resType)
 	{
 		// Go through the resource type object and make a properties
 		// object that descriptes the type both the type of the resource and
 		// any extra paramaters of the type
 		Properties props = new Properties();
 		ResourceDefinition.Parameter []  params = resType.getParameters();
 		if(params == null) {
 			return null;
 		}
 		
 		for(int i=0; i<params.length; i++){
 			props.setProperty(params[i].name, params[i].value);
 		}
 		return props;		
 	}	
 }
