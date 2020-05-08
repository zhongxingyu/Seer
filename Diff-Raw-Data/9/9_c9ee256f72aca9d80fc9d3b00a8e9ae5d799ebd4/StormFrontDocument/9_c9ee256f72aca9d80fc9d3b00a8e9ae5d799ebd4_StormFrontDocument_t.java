 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.stormfront.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.EmptyStackException;
 import java.util.Stack;
 
 import cc.warlock.core.stormfront.internal.ParseException;
 import cc.warlock.core.stormfront.internal.StormFrontProtocolParser;
 
 public class StormFrontDocument implements IStormFrontXMLHandler {
 
 	protected Stack<StormFrontElement> elementStack = new Stack<StormFrontElement>();
 	protected StormFrontElement rootElement;
 
 	public StormFrontElement getRootElement() {
 		return rootElement;
 	}
 	
 	public StormFrontDocument (InputStream stream)
 	{
 		StormFrontProtocolParser parser = new StormFrontProtocolParser(stream);
 		parser.setHandler(this);
 		
 		try {
 			parser.Document();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void startElement(String name, StormFrontAttributeList attributes, String rawXML) {
 		StormFrontElement currentElement = new StormFrontElement(name);
 		for (StormFrontAttribute attribute : attributes.getList())
 		{
 			currentElement.addAttribute(attribute);
 		}
 		
 		if (rootElement == null)
 		{
 			rootElement = currentElement;
 		}
 		else
 		{
 			if (elementStack.size() > 0)
 			{
 				elementStack.peek().addElement(currentElement);
 			}
 		}
 
 		elementStack.push(currentElement);
 	}
 	
 	public void characters(String characters) {
		// Check if there is an element to apply this to, 
		//   if not it's probably outside the root element so we ignore it.
		if (!elementStack.isEmpty()) {
 			StormFrontElement element = elementStack.peek();
 			element.appendText(characters);
 		}
 	}
 	
 	public void endElement(String name, String rawXML) {
 		elementStack.pop();
 	}
 	
 	public void saveTo (Writer writer, boolean prettyPrint)
 	{
 		try {
 			writer.append(rootElement.toXML("", prettyPrint, true));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
