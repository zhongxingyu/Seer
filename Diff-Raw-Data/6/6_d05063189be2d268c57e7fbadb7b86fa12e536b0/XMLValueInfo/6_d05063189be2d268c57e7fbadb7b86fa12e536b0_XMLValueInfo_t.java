  /*******************************************************************************
   * Copyright (c) 2007 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributor:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.model.project.ext.store;
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.ext.IValueInfo;
 import org.jboss.tools.common.model.project.ext.store.XMLStoreConstants;
 import org.jboss.tools.common.model.util.PositionHolder;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.w3c.dom.Element;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class XMLValueInfo implements IValueInfo {
 	XModelObject object;
 	String attribute;
 	
 	PositionHolder h = null;
 	
 	public XMLValueInfo() {
 	}
 	
 	public XMLValueInfo(XModelObject object, String attribute) {
 		this.object = object;
		this.attribute = attribute.intern();
 	}
 
 	public int getLength() {
 		getPositionHolder();
 		int length = h.getEnd() - h.getStart();
 		return length < 0 ? 0 : length;
 	}
 
 	public int getStartPosition() {
 		getPositionHolder();
 		return h.getStart();
 	}
 
 	public String getValue() {
 		return object.getAttributeValue(attribute);
 	}
 	
 	PositionHolder getPositionHolder() {
 		if(h == null) {
 			h = PositionHolder.getPosition(object, attribute);
 		}
 		h.update();
 		return h;
 	}
 	
 	public XModelObject getObject() {
 		return object;
 	}
 
 	public Element toXML(Element parent, Properties context) {
 		Element element = XMLUtilities.createElement(parent, XMLStoreConstants.TAG_VALUE_INFO);
 		element.setAttribute(XMLStoreConstants.ATTR_CLASS, XMLStoreConstants.CLS_XML);
 		if(attribute != null) element.setAttribute("attr", attribute); //$NON-NLS-1$
 		if(object != null) {
 			XMLStoreHelper.saveModelObject(element, object, "object", context); //$NON-NLS-1$
 		}
 		return element;
 	}
 
 	public void loadXML(Element element, Properties context) {
 		attribute = element.getAttribute("attr"); //$NON-NLS-1$
 		object = XMLStoreHelper.loadModelObject(element, "object", context); //$NON-NLS-1$
 	}
 
 	public IFile getResource() {
 		return object == null ? null : (IFile)object.getAdapter(IFile.class);
 	}
 	
 }
