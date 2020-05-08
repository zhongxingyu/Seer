 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.ui.tests;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jst.server.ui.internal.RuntimeLabelProvider;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentType;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
 
 import junit.framework.TestCase;
 
 public class LabelProviderTest extends TestCase {
 	private IRuntimeComponent rc = null;
 
 	public IRuntimeComponent getRuntimeComponent() {
 		if (rc == null) {
 			rc = new IRuntimeComponent() {
 				public Map<String, String> getProperties() {
 					return new HashMap<String, String>();
 				}
 	
 				public String getProperty(String name) {
 					return "not found";
 				}
 	
 				public IRuntime getRuntime() {
 					return null;
 				}
 	
 				public IRuntimeComponentType getRuntimeComponentType() {
 					return null;
 				}
 	
 				public IRuntimeComponentVersion getRuntimeComponentVersion() {
 					return new IRuntimeComponentVersion() {
 						public IRuntimeComponentType getRuntimeComponentType() {
 							return null;
 						}
 
 						public String getVersionString() {
 							return "1.0";
 						}
 
 						public int compareTo(Object o) {
 							return 0;
 						}
 						
 					};
 				}
 	
 				public Object getAdapter(Class arg0) {
 					return null;
 				}
 			};
 		}
 		return rc;
 	}
 
 	public void testLabelProvider() {
 		RuntimeLabelProvider lp = new RuntimeLabelProvider(getRuntimeComponent());
 		lp.getLabel();
 	}
 
 }
