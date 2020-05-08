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
  * Created on Mar 2, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.webservice.operation;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.common.QName;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.common.operations.J2EEModelModifierOperationDataModel;
 import org.eclipse.jst.j2ee.webservice.wsclient.Handler;
 import org.eclipse.jst.j2ee.webservice.wscommon.SOAPHeader;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * @author jialin
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class AddHandlerSOAPHeaderDataModel extends J2EEModelModifierOperationDataModel {
 
 
 	public static final String HANDLER = "AddHandlerSOAPHeaderDataModel.HANDLER"; //$NON-NLS-1$
 
 	public static final String NAMESPACE_URL = "AddHandlerSOAPHeaderDataModel.NAMESPACE_URL"; //$NON-NLS-1$
 
 	public static final String LOCAL_PART = "AddHandlerSOAPHeaderDataModel.LOCAL_PART"; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultOperation()
 	 */
 	public WTPOperation getDefaultOperation() {
 		return new AddHandlerSOAPHeaderOperation(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.operation.ModelModifierOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(NAMESPACE_URL);
 		addValidBaseProperty(LOCAL_PART);
 		addValidBaseProperty(HANDLER);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doValidateProperty(java.lang.String)
 	 */
 	protected IStatus doValidateProperty(String propertyName) {
 		if (propertyName.equals(NAMESPACE_URL)) {
 			return validateNamespaceURL(getStringProperty(propertyName));
 		}
 		return super.doValidateProperty(propertyName);
 	}
 
 
 	/**
 	 * @param servletName
 	 * @return
 	 */
 	private IStatus validateNamespaceURL(String name) {
 		if (name == null || name.trim().length() == 0) {
 			String msg = WebServiceMessages.getResourceString(WebServiceMessages.ERR_HANDLER_NAMESPACE_URL_EMPTY, new String[]{name});
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		Handler handler = (Handler) getProperty(AddHandlerSOAPHeaderDataModel.HANDLER);
 		List headers = handler.getSoapHeaders();
 		for (int i = 0; i < headers.size(); i++) {
 			Object header = headers.get(i);
 			String namespaceURL = null;
 			if (header instanceof SOAPHeader)
 				namespaceURL = ((SOAPHeader) header).getNamespaceURI();
 			else if (header instanceof QName)
 				namespaceURL = ((QName) header).getNamespaceURI();
 			if (namespaceURL != null && namespaceURL.equals(name))
 				return WTPCommonPlugin.createErrorStatus(WebServiceMessages.ERR_HANDLER_NAMESPACE_URL_EXISTS);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	public int getVersionID() {
		if (artifactEdit == null)
			return -1;
		return ((EnterpriseArtifactEdit)artifactEdit).getJ2EEVersion();
 	}
 }
