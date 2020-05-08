 /*******************************************************************************
  * Copyright (c) 2007-2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.bundle;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * Contains Uril functions 
  * 
  * @author mareshkau
  *
  */
 public class BundleMapUtil {
	
	private static final String elPattern = "(?s:(.*)[#\\$]\\{(.+)\\}(.*))"; //$NON-NLS-1$
     /**
     * Checks if node contains text information from resource bundle
     * @param pageContext
     * @param sourceNode
     * @return
     */
    public static boolean isInResourcesBundle(BundleMap bundleMap, Node sourceNode) {
        boolean rst = findInResourcesBundle(bundleMap, sourceNode);
        return rst;
    }
 
    /**
     * @param pageContext
     * @param sourceNode
     * @return
     */
    private static boolean findInResourcesBundle(BundleMap bundleMap, Node sourceNode) {
        boolean rst = false;
 
        if (bundleMap != null) {
            String textValue = null;
 
            if (sourceNode.getNodeType() == Node.TEXT_NODE) {
                textValue = sourceNode.getNodeValue();
 
                if ((textValue != null) && isContainsEl(textValue)) {
                    final String newValue = bundleMap.getBundleValue(textValue);
 
                    if (!textValue.equals(newValue)) {
                        rst = true;
                    }
                }
            }
 
            if (!rst) {
                final NamedNodeMap nodeMap = sourceNode.getAttributes();
 
                if (nodeMap != null && nodeMap.getLength() > 0) {
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        final Attr attr = (Attr) nodeMap.item(i);
                        final String value = attr.getValue();
 
                        if (value != null && isContainsEl(value)) {
                            final String value2 = bundleMap.getBundleValue(value);
 
                            if (!value2.equals(value)) {
                                rst = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return rst;
    }
    
 	/**
 	 * @param value
 	 * @return
 	 */
 	public static boolean isContainsEl(final String value) {
		return value.matches(elPattern);
 	}
 }
