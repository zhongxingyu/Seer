 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.util;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author jeremiah
  */
 public class XMLSerializer
 {
 	public static final String DEFAULT_STRING = "##default##";
 	public static int MAX_DEPTH = 0;
 	private static final String CLASS_ATTRIBUTE = "class";
 	private String namespaceURI = null;
 	private String prefix = null;
 
 	
 	
         
 	
 	
 	public XMLSerializer()
 	{
 		
 	}
 	
 	/**
 	 * this will export the class as an XML element owned by the owner document.
 	 * It will NOT append it to that document. This is DUE to the way the W3C DOM standard is written.
 	 * @param ownerDocument
 	 * @param object
 	 * @return
 	 * @throws Exception 
 	 */
 	public static Element export(Document ownerDocument, Object object) throws Exception	
 	{
 		XMLSerializer xmlSerializer = new XMLSerializer();
 		Element returnElement = ownerDocument.createElement(object.getClass().getSimpleName());
 		xmlSerializer.export(object, returnElement,0);
 		return returnElement;
 	}
 	
 	/**
 	 * Append the serialization of an object to an element
 	 * @param parentElement
 	 * @param object
 	 * @return
 	 * @throws Exception
 	 */
 	public static Element export(Element parentElement, Object object) throws Exception   
     {
         XMLSerializer xmlSerializer = new XMLSerializer();
         Element returnElement = parentElement.getOwnerDocument().createElement(object.getClass().getSimpleName());
         xmlSerializer.export(object, returnElement,0);
         parentElement.appendChild(returnElement);
         return returnElement;
     }
 	
 
 	
 	
 	public void setNamespace(String prefix, String namespaceURI)
 	{
 		this.prefix = prefix;
 		this.namespaceURI = namespaceURI;
 		
 	}
 	
 	/**
 	 * 
 	 * @param rootObject
 	 * @param rootElement
 	 * @param currentDepth
 	 * @throws Exception
 	 */
 	public void export(Object rootObject, Element rootElement, int currentDepth) throws Exception
 	{
 		currentDepth++;
 		
 		Vector<Field> fieldVector = ReflectionUtility.getFieldVector(rootObject);
 		
 		for (Field field : fieldVector)
 		{
 			
 			if (Modifier.isTransient(field.getModifiers()) == false && Modifier.isStatic(field.getModifiers()) == false)
 			{
 			
 				field.setAccessible(true);
 				Object fieldValue = field.get(rootObject);
 				String fieldName = field.getName();
 				
 				if (fieldValue == null)
 				{
 					continue;
 				}
 				//can't deal with crazy $ char in class names, shouldn't mater any way,
 				if (fieldName.contains("$") && Modifier.isFinal(field.getModifiers()))
 				{
 					continue;
 				}
 				
 				//skip self referencing items
 				if (fieldValue.equals(rootObject))
 				{
 					continue;
 				}
 				
 				
 				if (field.isAnnotationPresent(XMLElement.class) == true)
 				{
 					XMLElement xmlElementAnnotation = field.getAnnotation(XMLElement.class);
 					if (xmlElementAnnotation.name().equals(DEFAULT_STRING) == false)
 					{
 						fieldName = xmlElementAnnotation.name();
 					}
 					Element newElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? fieldName : prefix+":"+fieldName);				
 					// store the content of a primitive as the text data in an
 					// element
 					if (ReflectionUtility.isPrimitive(fieldValue.getClass()) == true)
 					{
 						rootElement.appendChild(newElement);
 						newElement.appendChild(newElement.getOwnerDocument().createTextNode(ReflectionUtility.getSerializedString(fieldValue)));
 					}
 					else
 					{
 						exportComplexType(fieldValue, rootElement, newElement, fieldName, currentDepth);
 					}
 				}
 				else if (ReflectionUtility.isPrimitive(field.getType()) == true)
 				{
 
 					if (field.isAnnotationPresent(XMLAttribute.class) == true)
 					{
 						XMLAttribute xmlAttributeAnnotation = field.getAnnotation(XMLAttribute.class);
 
 						if (xmlAttributeAnnotation.name().equals(DEFAULT_STRING) == false)
 						{
 							fieldName = xmlAttributeAnnotation.name();
 						}
 					}
 
 					
 					if (fieldValue != null)
 					{
 						rootElement.setAttributeNS(namespaceURI,prefix == null ? fieldName : prefix+":"+fieldName, ReflectionUtility.getSerializedString(fieldValue));
 					}
 
 				}
 				else
 				{
 					Element newElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? fieldName : prefix+":"+fieldName);
 					exportComplexType(fieldValue, rootElement, newElement, fieldName, currentDepth);
 				}
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * @param fieldValue
 	 *            the complex type to be exported
 	 * @param rootElement
 	 *            the element to append it to once we figure out what to do with
 	 *            it.
 	 * @param newElement
 	 *            the element that will be appended
 	 * @param newElementName
 	 *            the new element name
 	 * @param currentDepth
 	 *            how big of a tree we've already built.
 	 * @throws Exception
 	 */
 
 	@SuppressWarnings("unchecked")
 	private void exportComplexType(Object fieldValue, Element rootElement, Element newElement, String newElementName, int currentDepth) throws Exception
 	{
 		currentDepth++;
 		if (currentDepth >= MAX_DEPTH && MAX_DEPTH > 0)
 		{
 			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Reached Max Depth for XML serialization: "+MAX_DEPTH);
 			return;
 		}
 		if (Element.class.isAssignableFrom(fieldValue.getClass()))
 		{
 			Element fieldValueElement = (Element) fieldValue;
 			newElement.appendChild(rootElement.getOwnerDocument().importNode(fieldValueElement, true));
 			rootElement.appendChild(newElement);
 		}
 		else if (Collection.class.isAssignableFrom(fieldValue.getClass()))
 		{			
 			exportCollection(newElementName, rootElement, (Collection) fieldValue, currentDepth);
 		}
 		else if (fieldValue.getClass().isArray() == true)
 		{
 			exportArray(newElementName, rootElement, fieldValue, currentDepth);
 		}
 		else if (Map.class.isAssignableFrom(fieldValue.getClass()))
 		{
 			exportMap(newElementName, rootElement, (Map) fieldValue, currentDepth);
 		}
 		else
 		{	
 		    if(newElement.getParentNode() == null) //check and see if this has already been added someplace, and if so, don't try to add it again.
 		    {
 		        rootElement.appendChild(newElement);
 		    }
 			newElement.setAttributeNS(namespaceURI, prefix == null ? CLASS_ATTRIBUTE : prefix+":"+CLASS_ATTRIBUTE, fieldValue.getClass().getCanonicalName());
 			export(fieldValue, newElement, currentDepth);
 		}
 	}
 
 	/**
 	 * This will export an array of any dimension
 	 * 
 	 * @param arrayName
 	 * @param rootElement
 	 * @param arrayObject
 	 * @param currentDepth
 	 * @throws Exception
 	 */
 	private  void exportArray(String arrayName, Element rootElement, Object arrayObject, int currentDepth) throws Exception
 	{
 		int length = Array.getLength(arrayObject);
 		Vector<Object> objectVector = new Vector<Object>(length);
 		for (int index = 0; index < length; index++)
 		{
 			objectVector.add(Array.get(arrayObject, index));
 
 		}
 		exportCollection(arrayName, rootElement, objectVector, currentDepth);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void exportCollection(String collectionName, Element rootElement, Collection collection, int currentDepth) throws Exception
 	{
 	
 		for (Object object : collection)
 		{
 			Element newElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? collectionName : prefix+":"+collectionName);
 			rootElement.appendChild(newElement);
 			if (object == null)
 			{
 				continue;
 			}
 			else if (ReflectionUtility.isPrimitive(object.getClass()))
 			{
 				newElement.appendChild(rootElement.getOwnerDocument().createTextNode(ReflectionUtility.getSerializedString(object)));				
 			}
 			else
 			{
 				if (Collection.class.isAssignableFrom(object.getClass()))
 				{
 					exportCollection(collectionName, newElement, (Collection) object, currentDepth);
 				}
 				else
 				{
 					exportComplexType(object, rootElement, newElement, collectionName, currentDepth);	
 				}
 				
 			}
 		}
 	}
 
 	/**
 	 * This will export any class that can be cast as a Map
 	 * 
 	 * @param mapName
 	 * @param rootElement
 	 * @param map
 	 * @param currentDepth
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	private void exportMap(String mapName, Element rootElement, Map map, int currentDepth) throws Exception
 	{
 		Set<Entry<Object, Object>> entrySet = map.entrySet();
 		for (Entry<Object, Object> entry : entrySet)
 		{
 			Element mapElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? mapName : prefix+":"+mapName);
 			rootElement.appendChild(mapElement);
 			Element keyElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? "key" : prefix+":"+"key");
 			
 			Element valueElement = rootElement.getOwnerDocument().createElementNS(namespaceURI,prefix == null ? "value" : prefix+":"+"value");
 			
 			Object value = entry.getValue();
 			Object key = entry.getKey();
 			if (ReflectionUtility.isPrimitive(key.getClass()))
 			{				
 				mapElement.setAttributeNS(namespaceURI,prefix == null ? "key" : prefix+":"+"key", ReflectionUtility.getSerializedString(key));
 			}
 			else
 			{
 				mapElement.appendChild(keyElement);
 				exportComplexType(key, mapElement, keyElement, "key", currentDepth);
 			}
 			// TODO this will ralph on any case where the key doesn't resolve as (This might be fixed now)
 			// a primitive or simple type
 			if (value != null)
 			{
 				if (ReflectionUtility.isPrimitive(value.getClass()))
 				{
 					mapElement.setAttributeNS(namespaceURI,prefix == null ? "value" : prefix+":"+"value", ReflectionUtility.getSerializedString(value));
 				}
 				else
 				{
 					mapElement.appendChild(valueElement);
 					exportComplexType(value, mapElement, valueElement, "value", currentDepth);
 				}
 			}
 		}
 	}
 
 	/**
 	 * TODO break this up for loop processing
 	 * @param rootElement
 	 * @param rootObject
 	 * @throws Exception
 	 */
 	@SuppressWarnings({ "unchecked", "deprecation" })
 	public void marshall(Element rootElement, Object rootObject) throws Exception
 	{
 		Vector<Field> fieldVector = ReflectionUtility.getFieldVector(rootObject);
 		
 		for (Field field : fieldVector)
 		{
 			Class type = null;
 			if (Modifier.isTransient(field.getModifiers()) == false && Modifier.isStatic(field.getModifiers()) == false)
 			{
 				field.setAccessible(true);
 				
 				//lets get the expected name for this field
 				String fieldName = field.getName();								
 				if (field.isAnnotationPresent(XMLAttribute.class) == true)
 				{
 					XMLAttribute xmlAnnotation = field.getAnnotation(XMLAttribute.class);
 
 					if (xmlAnnotation.name().equals(DEFAULT_STRING) == false)
 					{
 						fieldName = xmlAnnotation.name();
 					}
 				}
 				else if (field.isAnnotationPresent(XMLElement.class) == true)
 				{
 					XMLElement xmlAnnotation = field.getAnnotation(XMLElement.class);
 
 					if (xmlAnnotation.name().equals(DEFAULT_STRING) == false)
 					{
 						fieldName = xmlAnnotation.name();
 					}
 				}
 				
 				//START PRIMITIVE PROCESSING
 				//if this is a primitive type, then we can set the value right here
 				if (ReflectionUtility.isPrimitive(field.getType()))
 				{
 					String fieldValueString = null;
 					if (field.isAnnotationPresent(XMLElement.class) == true)
 					{
 						Vector<Element> elementVector = new Vector<Element>();
 						NodeList nodeList = rootElement.getChildNodes();
 						//this lest us ignore namespaces
 						for (int index = 0; index < nodeList.getLength(); index++)
 						{
 							Node node = nodeList.item(index);
 							if (node instanceof Element)
 							{
 								if (node.getLocalName().endsWith(fieldName))
 								{
 									elementVector.add((Element) node);
 								}
 							}
 						}
 												
 						if (elementVector.size() > 0)
 						{
 							fieldValueString = elementVector.firstElement().getTextContent();	
 						}
 						
 					}
 					else
 					{
 						//need a check here since getAttribute returns the empty string instead of null
 						if (rootElement.hasAttribute(fieldName))
 						{
 							fieldValueString = rootElement.getAttribute(fieldName);
 						}
 					}
 					
 					if (fieldValueString != null)
 					{
 
 						Object instanceObject = ReflectionUtility.getPrimitiveInstance(field.getType(), fieldValueString);
 						if (instanceObject != null)
 						{
 							field.set(rootObject, instanceObject);
 						}
 						else
 						{
 							//if we got null, and the type is a primitive, leave it alone
 							//but if it's an object, then set it to null;
 							if (field.getType().isPrimitive() == false)
 							{
 								field.set(rootObject, instanceObject);
 							}
 						}
 
 					}
 				}
 				else //this is a complex type
 				{
 					NodeList nodeList = rootElement.getChildNodes();
 					Vector<Element> elementVector = getChildElementVector(nodeList,fieldName); 
 					
 					if (elementVector.size() > 0)
 					{
 						//check for Array, Collection, Map or Element
 						if (Element.class.isAssignableFrom(field.getType()))
 						{
 							//TODO this will leave the element attached o the serialized document, need to detach some how
 							//Element fieldValueElement = (Element) fieldValue;
 							//rootElement.appendChild(rootElement.getOwnerDocument().importNode(fieldValueElement, true));
 							field.set(rootObject, elementVector.firstElement().getElementsByTagName("*").item(0));
 						}
 						else if (Collection.class.isAssignableFrom(field.getType()))
 						{
 							
 							Collection collection = (Collection) field.getType().newInstance();
 							field.set(rootObject, collection);								
 							
 							
 							//walk the nodes, and create an object foreach node
 							//add the new object
 							//mashall the new object
 							Type collectionType = field.getGenericType();
 							marshallCollection(collection,collectionType,elementVector,fieldName);
 							
 							
 						}
 						else if (field.getType().isArray() == true)
 						{
 							
 							Object arrayObject = Array.newInstance(field.getType().getComponentType(), elementVector.size());
 							field.set(rootObject, arrayObject);
 							
 							for (int elementIndex = 0; elementIndex < elementVector.size(); elementIndex++)
 							{
 								Element element = elementVector.get(elementIndex);
								if (ReflectionUtility.isPrimitive(field.getType()) == true)
 								{
									Object instanceObject = ReflectionUtility.getPrimitiveInstance(field.getType(), element.getTextContent());
 									if (instanceObject != null)
 									{																				
 										Array.set(arrayObject, elementIndex, instanceObject);
 									}
 								}
 								else
 								{	
 									Object instanceObject = null;
 									if(element.hasAttribute(CLASS_ATTRIBUTE))
 									{
 									    //skip anything we can't remarshall
 									    if (ReflectionUtility.hasDefaultContructor(Class.forName(element.getAttribute(CLASS_ATTRIBUTE))))
 									    {
 									        instanceObject = ReflectionUtility.getComplexInstance(Class.forName(element.getAttribute(CLASS_ATTRIBUTE)));//, marnodeList.item(currentNode).getTextContent());
 									    }									    
 									    else
 									    {
 									        instanceObject = ReflectionUtility.getMarshalWrapperInstance(element.getAttribute(CLASS_ATTRIBUTE));
 									        if (instanceObject == null)
 									        {
 									            Logger.global.log(Level.WARNING, "Skipping "+element.getAttribute(CLASS_ATTRIBUTE)+" because it has no default constructor, try creating a MarshalWrapper");
 									        }
 									    }
 									}
 									else
 									{
 									  //skip anything we can't remarshall
 									    if (ReflectionUtility.hasDefaultContructor(field.getType().getComponentType()))
                                         {
 									        instanceObject = ReflectionUtility.getComplexInstance(field.getType().getComponentType());//, marnodeList.item(currentNode).getTextContent());
                                         }
 									    else
 									    {
 									        instanceObject = ReflectionUtility.getMarshalWrapperInstance(field.getType().getComponentType().getCanonicalName());
                                             if (instanceObject == null)
                                             {
                                                 Logger.global.log(Level.WARNING, "Skipping "+field.getType().getComponentType()+" because it has no default constructor, try creating a MarshalWrapper");
                                             }
 									        
 									    }
 									}
 									
 									
 									if (instanceObject != null)
 									{																														
 										Array.set(arrayObject, elementIndex, instanceObject);
 										marshall(element, instanceObject);
 									}
 								}
 							}
 							
 						}
 						else if (Map.class.isAssignableFrom(field.getType()))
 						{
 							Map map = (Map) field.getType().newInstance();
 							field.set(rootObject, map);
 							Type[] types = null;
 							if (field.getGenericType() instanceof ParameterizedType)
 							{
 								 
 								types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();						
 								if (types.length != 2)
 								{
 									//TODO we do not yet support complex generic types on maps
 									throw new Exception("we do not yet support complex generic types on maps:"+field.getGenericType());
 								}								
 								else if (types[0] instanceof ParameterizedType || types[1] instanceof ParameterizedType)
 								{
 									//TODO we do not yet support deep generic types on maps
 									throw new Exception("we do not yet support deep generic types on maps:"+field.getGenericType());
 								}
 							}
 							else
 							{
 								//TODO we do not support generic type in maps yet
 								throw new Exception("we do not yet support  generic types on maps:"+field.getGenericType());
 							}
 							for (Element element : elementVector)
 							{
 								Object keyTypeObject = null;
 								Object valueTypeObject = null;
 								
 								
 								Element keyElement = null;
 								Element valueElement = null;
 								
 								NodeList childNodeList = element.getChildNodes();
 								for (int currentChildNode = 0; currentChildNode < childNodeList.getLength(); currentChildNode++)
 								{
 									String childNodeName =  childNodeList.item(currentChildNode).getNodeName();									
 									if (childNodeName.equals("key"))
 									{
 										keyElement = (Element) childNodeList.item(currentChildNode);
 									}
 									else if (childNodeName.equals("value"))
 									{
 										valueElement = (Element) childNodeList.item(currentChildNode);
 									}
 								}
 								
 								
 								
 								if (keyElement != null)
 								{
 									if (ReflectionUtility.isPrimitive((Class) types[0]) == true)
 									{
 										keyTypeObject = ReflectionUtility.getPrimitiveInstance(types[0], keyElement.getTextContent());
 									}
 									else
 									{
 										keyTypeObject = ReflectionUtility.getComplexInstance((Class) types[0]);
 										marshall(keyElement, keyTypeObject);
 									}
 								}								
 								else if (element.hasAttributeNS(namespaceURI,prefix == null ? "key" : prefix+":"+"key"))
 								{
 									keyTypeObject = ReflectionUtility.getPrimitiveInstance(types[0], element.getAttributeNS(namespaceURI,prefix == null ? "key" : prefix+":"+"key"));
 								}
 								
 								if (valueElement != null)
 								{
 									if (ReflectionUtility.isPrimitive((Class) types[1]) == true)
 									{
 										valueTypeObject = ReflectionUtility.getPrimitiveInstance(types[1], valueElement.getTextContent());
 									}
 									else
 									{
 										if (valueElement.hasAttribute(CLASS_ATTRIBUTE))
 										{
 											valueTypeObject = ReflectionUtility.getComplexInstance(Class.forName(valueElement.getAttribute(CLASS_ATTRIBUTE)));
 										}
 										else
 										{
 											valueTypeObject = ReflectionUtility.getComplexInstance((Class) types[1]);	
 										}
 										
 										marshall(valueElement, valueTypeObject);
 									}
 								}
 								else if (element.hasAttributeNS(namespaceURI,prefix == null ? "value" : prefix+":"+"value"))
 								{
 									valueTypeObject = ReflectionUtility.getPrimitiveInstance(types[0], element.getAttributeNS(namespaceURI,prefix == null ? "value" : prefix+":"+"value"));
 								}
 								
 								try 
 								{
 									map.put(keyTypeObject, valueTypeObject);
 								} 
 								catch (NullPointerException nullPointerException)
 								{
 									//not all maps support null as a key or value
 									//but we might as well try and just skip it if there is an error
 								}
 							}
 						}
 						else 
 						{							
 							if (type == null)
 							{
 								type =  field.getType();
 							}
 							if (elementVector.firstElement().hasAttribute(CLASS_ATTRIBUTE))
 							{
 								type = Class.forName(elementVector.firstElement().getAttribute(CLASS_ATTRIBUTE));
 							}
 							Object instanceObject = ReflectionUtility.getComplexInstance(type);							
 							if (instanceObject != null)
 							{
 								field.set(rootObject, instanceObject);
 							}
 							marshall(elementVector.firstElement(), instanceObject);							
 						}
 									
 					}
 					
 					
 				}
 			}
 		}
 
 	}
 
 	private static Vector<Element> getChildElementVector(NodeList nodeList, String fieldName)
 	{
 		Vector<Element> elementVector = new Vector<Element>();
 		
 		//this lest us ignore namespaces
 		for (int index = 0; index < nodeList.getLength(); index++)
 		{
 			Node node = nodeList.item(index);
 			if (node instanceof Element)
 			{
 				String nodeName = node.getLocalName();
 				if (nodeName == null)
 				{
 					nodeName = node.getNodeName();
 				}
 				
 				if (nodeName.equals(fieldName))
 				{
 					elementVector.add((Element) node);
 				}
 			}
 		}
 		return elementVector;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void marshallCollection(Collection collection, Type collectionType, Vector<Element> elementVector,String fieldName) throws Exception
 	{
 		for (Element element : elementVector)
 		{
 			if (collectionType instanceof ParameterizedType)
 			{
 				if (((ParameterizedType) collectionType).getActualTypeArguments().length != 1)
 				{
 					//TODO Don't know what to do with complex Collections yet
 					throw new Exception("Don't know what to do with complex Collections yet");
 				}
 				
 				Type[] collectionTypes = ((ParameterizedType) collectionType).getActualTypeArguments();
 				if (collectionTypes[0] instanceof Class)
 				{
 					Object instanceObject = null;
 					boolean isPrimitive = true;
 					if (ReflectionUtility.isPrimitive((Class) collectionTypes[0]))
 					{
 						instanceObject = ReflectionUtility.getPrimitiveInstance(collectionTypes[0],element.getTextContent());
 					}
 					else
 					{
 						isPrimitive = false;
 						if (element.hasAttribute(CLASS_ATTRIBUTE))
 						{
 							instanceObject = ReflectionUtility.getComplexInstance(Class.forName(element.getAttribute(CLASS_ATTRIBUTE)));
 						}
 						else
 						{
 							instanceObject = ReflectionUtility.getComplexInstance((Class) collectionTypes[0]);	
 						}
 						
 					}
 					 
 					if (instanceObject != null)
 					{
 						collection.add(instanceObject);
 						if (isPrimitive == false)
 						{
 							marshall(element, instanceObject);
 						}
 					}
 				}
 				//this is something else, something deeper
 				else if (collectionTypes[0] instanceof ParameterizedType)
 				{
 					ParameterizedType parameterizedType = (ParameterizedType) collectionTypes[0];
 					if (parameterizedType.getRawType() instanceof Class)
 					{
 						Object instanceObject = ((Class) parameterizedType.getRawType()).newInstance();
 						if (instanceObject != null)
 						{
 							collection.add(instanceObject);
 							if (Collection.class.isAssignableFrom(instanceObject.getClass()))
 							{
 								Vector<Element> subElementVector = getChildElementVector(element.getChildNodes(), fieldName);
 								marshallCollection((Collection) instanceObject, parameterizedType, subElementVector,fieldName);
 							}
 							else
 							{
 								//TODO Don't know what to do with compound generic collections who's parameterized types aren't classes or other generic collections
 								throw new Exception("Don't know what to do with compound generic collections who's parameterized types aren't classes or other generic collections");
 							}							
 						}
 					}
 				
 				}
 				
 			}
 			else
 			{
 				//TODO Don't know what to do with untyped Collections yet
 				throw new Exception("Don't know what to do with untyped Collections yet");
 			}
 		}
 		
 	}
 
 	
 	
 	
 	
 }
