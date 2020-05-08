 /*
 	This file is part of Brick.
 
     Brick is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Brick is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with Brick.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.irenical.brick.xml;
 
import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.irenical.brick.AbstractBundle;
 import com.irenical.brick.BundleInterface;
 
 public class DOMBundle extends AbstractBundle<String> {
 
 	private final Node node;
 
 	public DOMBundle(String xml) throws IOException, ParserConfigurationException, SAXException {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
		node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
 	}
 
 	public DOMBundle(Node node) {
 		this.node = node;
 	}
 
 	@Override
 	protected BundleInterface<String> createBundle(Object value) {
 		if (value instanceof Node) {
 			return new DOMBundle((Node) value);
 		}
 		return null;
 	}
 
 	@Override
 	public Set<String> getKeys() {
 		Set<String> result = new HashSet<String>();
 		NodeList children = node.getChildNodes();
 		for (int i = 0; i < children.getLength(); ++i) {
 			Node child = children.item(i);
 			if (Node.ELEMENT_NODE == child.getNodeType()) {
 				result.add(child.getNodeName());
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public Object getObject(String key) {
 		NodeList children = node.getChildNodes();
 		for (int i = 0; i < children.getLength(); ++i) {
 			Node child = children.item(i);
 			if (key.equals(child.getNodeName())) {
 				return findValue(child);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Iterable<Object> getObjects(String key) {
 		List<Object> result = new LinkedList<Object>();
 		NodeList children = node.getChildNodes();
 		for (int i = 0; i < children.getLength(); ++i) {
 			Node child = children.item(i);
 			if (key.equals(child.getNodeName())) {
 				result.add(findValue(child));
 			}
 		}
 		return result;
 	}
 
 	private static String findValue(Node node) {
 		String result = null;
 		if(node!=null&&Node.ELEMENT_NODE==node.getNodeType()){
 			node = node.getFirstChild();
 		}
 		if (node != null) {
 			switch (node.getNodeType()) {
 			case Node.CDATA_SECTION_NODE:
 				result = node.getNodeValue();
 				break;
 			case Node.TEXT_NODE:
 				result = node.getNodeValue();
 				break;
 			}
 		}
 		return result;
 	}
 
 }
