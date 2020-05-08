 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Oct 29, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.webservices;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 
 import com.ibm.wtp.common.RegistryReader;
 
 /**
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class WSDLServiceExtensionRegistry extends RegistryReader {
 
 	static final String EXTENSION_NAME = "WSDLServiceHelper"; //$NON-NLS-1$
 	static final String ELEMENT_WSDL_HELPER = "wsdlHelper"; //$NON-NLS-1$
 	static final String WSDL_HELPER_CLASS = "helperClass"; //$NON-NLS-1$
 	private static WSDLServiceExtensionRegistry INSTANCE = null;
 
 	public WSDLServiceExtensionRegistry() {
 		super(J2EEPlugin.PLUGIN_ID, EXTENSION_NAME);
 	}
 
 	public static WSDLServiceExtensionRegistry getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new WSDLServiceExtensionRegistry();
 			INSTANCE.readRegistry();
 		}
 		return INSTANCE;
 	}
 
 	/**
 	 * readElement() - parse and deal w/ an extension like: <earModuleExtension extensionClass =
 	 * "com.ibm.etools.web.plugin.WebModuleExtensionImpl"/>
 	 */
 	public boolean readElement(IConfigurationElement element) {
 		if (!element.getName().equals(ELEMENT_WSDL_HELPER))
 			return false;
 
 		WSDLServiceHelper extension = null;
 		try {
 			extension = (WSDLServiceHelper) element.createExecutableExtension(WSDL_HELPER_CLASS);
		} catch (CoreException e) {
 			//Register default do nothing helper......
 			addModuleExtension(new DefaultWSDLServiceHelper());
		}
 		if (extension != null)
 			addModuleExtension(extension);
 		return true;
 	}
 
 
 	private static void addModuleExtension(WSDLServiceHelper ext) {
 		WSDLServiceExtManager.registerWSDLServiceHelper(ext);
 	}
 
 }
