 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.pagedesigner.jsf.ui.converter.operations.jsf;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.jst.pagedesigner.converter.ConvertPosition;
 import org.eclipse.jst.pagedesigner.dom.EditModelQuery;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.operations.AbstractTransformOperation;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.operations.TransformOperationFactory;
 import org.eclipse.jst.pagedesigner.jsf.core.dom.JSFDOMUtil;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * ITransformOperation implementation specifically for the "column" JSF (HTML)
  * Element. 
  * 
  * <br><b>Note:</b> requires ITransformOperation.setTagConverterContext(...) to
  * have been called to provide a valid ITagConverterContext instance prior to
  * a call to the transform(...) method.
  * 
  * @author Ian Trimble - Oracle
  */
 public class ColumnOperation extends AbstractTransformOperation {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.operations.internal.provisional.AbstractTransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	public Element transform(Element srcElement, Element curElement) {
 
 		//create "td" Element
 		Element tdElement = createElement("td"); //$NON-NLS-1$
 
 		//get parent Node
 		Node parentNode = srcElement.getParentNode();
 
 		//process if parentNode is a "dataTable" Element
 		if (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE && parentNode.getLocalName().equals("dataTable")) { //$NON-NLS-1$
 			//tokenize "columnClasses" attribute into a List
 			List columnClassesList = new ArrayList();
 			String columnClassesAttribute = ((Element)parentNode).getAttribute("columnClasses"); //$NON-NLS-1$
 			if (columnClassesAttribute != null && columnClassesAttribute.length() > 0) {
 				StringTokenizer tokenizer = new StringTokenizer(columnClassesAttribute, ", "); //$NON-NLS-1$
 				while (tokenizer.hasMoreTokens()) {
 					columnClassesList.add(tokenizer.nextToken());
 				}
 			}
 			//set "class" attribute
 			int offset = EditModelQuery.getInstance().getSameTypeNodeIndex(srcElement);
 			if (offset < columnClassesList.size()) {
 				ITransformOperation operation =
 					TransformOperationFactory.getInstance().getTransformOperation(
 							TransformOperationFactory.OP_CreateAttributeOperation,
 							new String[]{"class", (String)columnClassesList.get(offset)}); //$NON-NLS-1$
 				operation.transform(srcElement, tdElement);
 			}
 		}
 
 		//add non-transparent (?), non-facet children (for further processing)
         if (EditModelQuery.getInstance().hasNonTransparentChild(srcElement, new String[] {"facet"})) { //$NON-NLS-1$
         	Node childNode = srcElement.getFirstChild();
         	int index = 0;
         	while (childNode != null) {
         		if (!(childNode instanceof Element) || !JSFDOMUtil.isFacet((Element)childNode)) {
        			tagConverterContext.addChild(childNode, new ConvertPosition(tdElement, index++));
         		}
         		childNode = childNode.getNextSibling();
         	}
         } else {
         	//append single space for esthetics of the "td" Element
         	appendChildText(" ", tdElement); //$NON-NLS-1$
         }
 
 		return tdElement;
 	}
 
 }
