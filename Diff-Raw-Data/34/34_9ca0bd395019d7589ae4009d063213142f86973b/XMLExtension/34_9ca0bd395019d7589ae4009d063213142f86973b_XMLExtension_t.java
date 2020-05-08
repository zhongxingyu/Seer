 /*******************************************************************************
  * Extension points
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.extensionpoint.simple;
 
 import java.util.Arrays;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.tuilmenau.ics.extensionpoint.Extension;
 import de.tuilmenau.ics.extensionpoint.ExtensionRegistry;
 
 
 public class XMLExtension implements Extension
 {
 	public XMLExtension(Node configEntry)
 	{
 		this.configEntry = configEntry;
 	}
 
 	@Override
 	public String getName()
 	{
 		return configEntry.getNodeName();
 	}
 
 	@Override
 	public Object create(String attributeName) throws Exception
 	{
 		return ExtensionRegistry.createObjectViaDefaultConstructor(getAttribute(attributeName));
 	}
 
 	@Override
 	public String getAttribute(String name)
 	{
 		NamedNodeMap attributes = configEntry.getAttributes();
 		if(attributes != null) {
 			Node attr = attributes.getNamedItem(name);
 			if(attr != null) {
 				return attr.getNodeValue();
 			}
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Extension[] getChildren()
 	{
 		NodeList children = configEntry.getChildNodes();
 		if(children != null) {
 			Extension[] res = new Extension[children.getLength()];
 			int newIndex = 0;
 			
 			for(int i = 0; i < children.getLength(); i++) {
 				Node entry = children.item(i);
 				
 				// make sure that it is not a pure text entry
 				if(entry.hasAttributes() || entry.hasChildNodes()) {
 					res[newIndex] = new XMLExtension(entry);
 					newIndex++;
 				}
 			}
 			
 			// did we omit some entries? Resize array.
 			if(newIndex != children.getLength()) {
 				res = Arrays.copyOf(res, newIndex);
 			}
 			
 			return res;
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return this.getClass().getSimpleName() +"." +getName();
 	}
 	
 	private Node configEntry;
 }
