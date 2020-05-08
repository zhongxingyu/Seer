 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.dom;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * @author mengbo
  */
 public class DOMUtil {
 	private static Logger _logger = PDPlugin.getLogger(DOMUtil.class);
 
 	/**
 	 * Get a list of ancester nodes starting from the Document till the node.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private static List getAncesters(Node node) {
 		List list = new ArrayList();
 		while (node != null) {
 			list.add(node);
 			if (node instanceof Document) {
 				break;
 			}
             node = node.getParentNode();
 		}
 		if (node == null) {
 			// if part ==null, means we didn't find a DocumentEditPart,
 			// something must be wrong.
 			return null;
 		}
 		// reverse to make it starting from the docuemnteditpart node.
 		Collections.reverse(list);
 		list.add(null); // add an null terminator.
 		return list;
 	}
 
 	/**
 	 * find the smallest common ancester of two edit part.
 	 * 
 	 * @param node1
 	 * @param node2
 	 * @return the common ancestor
 	 */
 	public static Node findCommonAncester(Node node1, Node node2) {
 		List list1 = getAncesters(node1);
 		if (list1 == null) {
 			return null;
 		}
 		List list2 = getAncesters(node2);
 		if (list2 == null) {
 			return null;
 		}
 		if (list1.get(0) != list2.get(0)) {
 			return null;
 		}
 		Node common = (Node) list1.get(0);
 		for (int i = 1;; i++) {
 			Node p1 = (Node) list1.get(i);
 			Node p2 = (Node) list2.get(i);
 			if (p1 == null || p2 == null) {
 				return common;
 			}
 			if (p1 != p2) {
 				return common;
 			}
 			common = p1;
 		}
 
 	}
 
 	/**
 	 * this method is almost same as <code>cloneNodeDeep()</code>. The
 	 * difference is that this method will try to ignore all kinds of error.
 	 * 
 	 * In SSE, if the document model enforce some kinds of validation, then the
 	 * clone may fail. During some cases, we want to ignore the validation
 	 * errors.
 	 * 
 	 * @param destDoc
 	 * @param sourceNode
 	 * @return the node
 	 */
 	public static Node cloneNodeDeepIgnoreError(Document destDoc,
 			Node sourceNode) {
 		switch (sourceNode.getNodeType()) {
 		case Node.ELEMENT_NODE:
 			Element sourceEle = (Element) sourceNode;
 			Element resultEle = destDoc.createElement(sourceEle.getTagName());
 			NamedNodeMap attrs = sourceEle.getAttributes();
 			for (int i = 0, size = attrs.getLength(); i < size; i++) {
 				Attr a = (Attr) attrs.item(i);
 				try {
 					resultEle.setAttribute(a.getName(), a.getValue());
 				} catch (Exception ex) {
 					// ignore
					_logger.info("Exception", ex); //$NON-NLS-1$
 				}
 			}
 			NodeList children = sourceEle.getChildNodes();
 			for (int i = 0, size = children.getLength(); i < size; i++) {
 				Node n = children.item(i);
 				Node d = cloneNodeDeepIgnoreError(destDoc, n);
 				if (d != null) {
 					try {
 						resultEle.appendChild(d);
 					} catch (Exception ex) {
 						// ignore
						_logger.info("Exception", ex); //$NON-NLS-1$
 					}
 				}
 			}
 			return resultEle;
 		case Node.TEXT_NODE:
 			Text txt = destDoc.createTextNode(sourceNode.getNodeValue());
 			if (txt instanceof IDOMText && sourceNode instanceof IDOMText) {
 				try {
 					((IDOMText) txt).setSource(((IDOMText) sourceNode)
 							.getSource());
 				} catch (Exception ex) {
 					// ignore
 				}
 			}
 			return txt;
 		case Node.CDATA_SECTION_NODE:
 			return destDoc.createCDATASection(sourceNode.getNodeValue());
 		default:
 			return null; // not support.
 		}
 	}
 
 	/**
 	 * @param destDoc
 	 * @param sourceNode
 	 * @return the node
 	 */
 	public static Node cloneNodeDeep(Document destDoc, Node sourceNode) {
 		switch (sourceNode.getNodeType()) {
 		case Node.ELEMENT_NODE:
 			Element sourceEle = (Element) sourceNode;
 			Element resultEle = destDoc.createElement(sourceEle.getTagName());
 			NamedNodeMap attrs = sourceEle.getAttributes();
 			for (int i = 0, size = attrs.getLength(); i < size; i++) {
 				Attr a = (Attr) attrs.item(i);
 				resultEle.setAttribute(a.getName(), a.getValue());
 			}
 			NodeList children = sourceEle.getChildNodes();
 			for (int i = 0, size = children.getLength(); i < size; i++) {
 				Node n = children.item(i);
 				Node d = cloneNodeDeep(destDoc, n);
 				if (d != null) {
 					resultEle.appendChild(d);
 				}
 			}
 			return resultEle;
 		case Node.TEXT_NODE:
 			Text txt = destDoc.createTextNode(sourceNode.getNodeValue());
 			if (txt instanceof IDOMText && sourceNode instanceof IDOMText) {
 				try {
 					((IDOMText) txt).setSource(((IDOMText) sourceNode)
 							.getSource());
 				} catch (Exception ex) {
 					// ignore
					_logger.info("Exception", ex); //$NON-NLS-1$
 				}
 			}
 			return txt;
 		case Node.CDATA_SECTION_NODE:
 			return destDoc.createCDATASection(sourceNode.getNodeValue());
 		default:
 			return null; // not support.
 		}
 	}
 
 	/**
 	 * check whether the ancester relationship exist for the two nodes.
 	 * 
 	 * @param ancester
 	 * @param child
 	 * @return true if ancester is an ancestor of child
 	 */
 	public static boolean isAncester(Node ancester, Node child) {
 		while (child != null) {
 			if (child == ancester) {
 				return true;
 			}
 			child = child.getParentNode();
 		}
 		return false;
 	}
 
 	/**
 	 * insert the node at specified position.
 	 * 
 	 * @param domPosition 
 	 * @param node 
 	 * @return null if fail, otherwise return the inserted node.
 	 */
 	public static Node insertNode(IDOMPosition domPosition, Node node) {
 		IDOMPosition position = DOMPositionHelper.splitText(domPosition);
 		if (position == null || position.getContainerNode() == null) {
 			return null;
 		}
 		if (position.getNextSiblingNode() == null) {
 			position.getContainerNode().appendChild(node);
 		} else {
 			position.getContainerNode().insertBefore(node,
 					position.getNextSiblingNode());
 		}
 
 		return node;
 	}
 }
