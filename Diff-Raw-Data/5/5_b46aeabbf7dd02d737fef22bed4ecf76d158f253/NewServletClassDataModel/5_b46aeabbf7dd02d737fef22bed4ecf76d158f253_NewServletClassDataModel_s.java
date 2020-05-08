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
  * Created on Apr 1, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.application.operations.IAnnotationsDataModel;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.webapplication.Servlet;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.frameworks.operations.WTPOperation;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 
 /**
  * @author jialin
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class NewServletClassDataModel extends NewWebJavaClassDataModel implements IAnnotationsDataModel {
 
 	public static final String INIT = "NewServletClassDataModel.INIT"; //$NON-NLS-1$
 	public static final String DO_POST = "NewServletClassDataModel.DO_POST"; //$NON-NLS-1$
 	public static final String DESTROY = "NewServletClassDataModel.DESTROY"; //$NON-NLS-1$
 	public static final String TO_STRING = "NewServletClassDataModel.TO_STRING"; //$NON-NLS-1$
 	public static final String DO_PUT = "NewServletClassDataModel.DO_PUT"; //$NON-NLS-1$
 	public static final String DO_GET = "NewServletClassDataModel.DO_GET"; //$NON-NLS-1$
 	public static final String GET_SERVLET_INFO = "NewServletClassDataModel.GET_SERVLET_INFO"; //$NON-NLS-1$
 	public static final String DO_DELETE = "NewServletClassDataModel.DO_DELETE"; //$NON-NLS-1$
 
 	//protected boolean USE_ANNOTATIONS = false;
 	protected String SERVLET_NAME = null;
 	
 	//Add servlet data model values
 	public final static String SERVLET_SUPERCLASS = "javax.servlet.http.HttpServlet"; //$NON-NLS-1$ 
 	public final static String[] SERVLET_INTERFACES = {"javax.servlet.Servlet"}; //$NON-NLS-1$
 	public static final String IS_SERVLET_TYPE = "AddServletDataModel.IS_SERVLET_TYPE"; //$NON-NLS-1$
 	public static final String INIT_PARAM = "AddServletDataModel.INIT_PARAM"; //$NON-NLS-1$
 	public static final String URL_MAPPINGS = "AddServletDataModel.URL_MAPPINGS"; //$NON-NLS-1$
 
 	//common data model values
 	public static final String DISPLAY_NAME = "AddServletFilterListenerCommonDataModel.DISPLAY_NAME"; //$NON-NLS-1$
 	public static final String DESCRIPTION = "AddServletFilterListenerCommonDataModel.DESCRIPTION"; //$NON-NLS-1$
 	public static final String USE_EXISTING_CLASS = "AddServletFilterListenerCommonDataModel.USE_EXISTING_CLASS"; //$NON-NLS-1$
 	
 	private List interfaceList;
 	
 	public WTPOperation getDefaultOperation() {
 		return new AddServletOperation(this);
 	}
 
 	protected Boolean basicIsEnabled(String propertyName) {
 		if (USE_ANNOTATIONS.equals(propertyName)) {
 			if (this.j2eeNature.getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3) {
				if (getBooleanProperty(USE_ANNOTATIONS))
					setBooleanProperty(USE_ANNOTATIONS, false);
 				return Boolean.FALSE;
 			}
 			return Boolean.TRUE;
 		}
 		return super.basicIsEnabled(propertyName);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.operation.ModelModifierOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(INIT);
 		addValidBaseProperty(DO_POST);
 		addValidBaseProperty(DESTROY);
 		addValidBaseProperty(TO_STRING);
 		addValidBaseProperty(DO_PUT);
 		addValidBaseProperty(DO_GET);
 		addValidBaseProperty(GET_SERVLET_INFO);
 		addValidBaseProperty(DO_DELETE);
 		addValidBaseProperty(IS_SERVLET_TYPE);
 		addValidBaseProperty(INIT_PARAM);
 		addValidBaseProperty(URL_MAPPINGS);
 		addValidBaseProperty(USE_ANNOTATIONS);
 		addValidBaseProperty(DISPLAY_NAME);
 		addValidBaseProperty(DESCRIPTION);
 		addValidBaseProperty(USE_EXISTING_CLASS);
 	}
 
 	protected Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(DO_POST))
 			return new Boolean(true);
 		if (propertyName.equals(DO_GET))
 			return new Boolean(true);
 		if (propertyName.equals(IS_SERVLET_TYPE))
 			return new Boolean(true);
 		else if (propertyName.equals(USE_ANNOTATIONS))
 			return Boolean.TRUE;
 		return super.getDefaultProperty(propertyName);
 	}
 
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		if (propertyName.equals(USE_ANNOTATIONS)) {
 			if (((Boolean) propertyValue).booleanValue() && this.j2eeNature.getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				return true;
 			notifyEnablementChange(USE_ANNOTATIONS);
 		}
 		if (propertyName.equals(DISPLAY_NAME)) {
 			setNotificationEnabled(false);
 			setProperty(CLASS_NAME,propertyValue);
 			setNotificationEnabled(true);
 		}
 		return super.doSetProperty(propertyName, propertyValue);
 	}
 	
 	protected IStatus doValidateProperty(String propertyName) {
 		if (propertyName.equals(INIT_PARAM))
 			return validateInitParamList((List) getProperty(propertyName));
 		if (propertyName.equals(URL_MAPPINGS))
 			return validateURLMappingList((List) getProperty(propertyName));
 		if (propertyName.equals(DISPLAY_NAME))
 			return validateDisplayName(getStringProperty(propertyName));
 		if (propertyName.equals(USE_EXISTING_CLASS))
 			return validateExistingClass(getBooleanProperty(propertyName));
 		return super.doValidateProperty(propertyName);
 	}
 	
 	private IStatus validateExistingClass(boolean prop) {
 		if (prop) {
 			return validateJavaClassName(getStringProperty(CLASS_NAME));
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	protected IStatus validateJavaClassName(String className) {
 		IStatus status = super.validateJavaClassName(className);
 		if (status.isOK()) {
 			// do not allow the name "Servlet"
 			if (className.equals("Servlet")) { //$NON-NLS-1$
 				String msg = WebMessages.getResourceString(WebMessages.ERR_SERVLET_JAVA_CLASS_NAME_INVALID);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			return WTPCommonPlugin.OK_STATUS;
 		}
 		return status;
 	}
 	
 	private IStatus validateInitParamList(List prop) {
 		if (prop != null && !prop.isEmpty()) {
 			boolean dup = hasDuplicatesInStringArrayList(prop);
 			if (dup) {
 				String msg = WebMessages.getResourceString(WebMessages.ERR_DUPLICATED_INIT_PARAMETER);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	private IStatus validateURLMappingList(List prop) {
 		if (prop != null && !prop.isEmpty()) {
 			boolean dup = hasDuplicatesInStringArrayList(prop);
 			if (dup) {
 				String msg = WebMessages.getResourceString(WebMessages.ERR_DUPLICATED_URL_MAPPING);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 	
 	private boolean hasDuplicatesInStringArrayList(List input) {
 		if (input == null)
 			return false;
 		int n = input.size();
 		boolean dup = false;
 		for (int i = 0; i < n; i++) {
 			String[] sArray1 = (String[]) input.get(i);
 			for (int j = i + 1; j < n; j++) {
 				String[] sArray2 = (String[]) input.get(j);
 				if (isTwoStringArraysEqual(sArray1, sArray2)) {
 					dup = true;
 					break;
 				}
 			}
 			if (dup)
 				break;
 		}
 		return dup;
 	}
 	
 	private boolean isTwoStringArraysEqual(String[] sArray1, String[] sArray2) {
 		if (sArray1 == null || sArray2 == null)
 			return false;
 		int n1 = sArray1.length;
 		int n2 = sArray1.length;
 		if (n1 == 0 || n2 == 0)
 			return false;
 		if (n1 != n2)
 			return false;
 		if (!sArray1[0].equals(sArray2[0]))
 			return false;
 		return true;
 	}
 	
 	public String getServletSuperclassName() {
 		return SERVLET_SUPERCLASS;
 	}
 
 	/**
 	 * @return Returns the USE_ANNOTATIONS.
 	 */
 	public boolean isAnnotated() {
 		return ((Boolean)getProperty(USE_ANNOTATIONS)).booleanValue();
 	}
 
 	/**
 	 * @param use_annotations
 	 *            The USE_ANNOTATIONS to set.
 	 */
 	public void setAnnotations(boolean use_annotations) {
 		setProperty(USE_ANNOTATIONS,new Boolean(use_annotations));
 	}
 
 	/**
 	 * @return Returns the SERVLET_NAME.
 	 */
 	public String getServletName() {
 		return this.SERVLET_NAME;
 	}
 
 	/**
 	 * @param servlet_name
 	 *            The SERVLET_NAME to set.
 	 */
 	public void setServletName(String servlet_name) {
 		this.SERVLET_NAME = servlet_name;
 	}
 	
 	public List getServletInterfaces() {
 		if (this.interfaceList == null) {
 			this.interfaceList = new ArrayList();
 			for (int i = 0; i < SERVLET_INTERFACES.length; i++) {
 				this.interfaceList.add(SERVLET_INTERFACES[i]);
 			}
 		}
 		return this.interfaceList;
 	}
 	
 	protected IStatus validateDisplayName(String prop) {
 		if (prop == null || prop.trim().length() == 0) {
 			String msg = WebMessages.getResourceString(WebMessages.ERR_DISPLAY_NAME_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		WebApp webApp = (WebApp) getDeploymentDescriptorRoot();
 		List servlets = webApp.getServlets();
 		boolean exists = false;
 		if (servlets != null && !servlets.isEmpty()) {
 			for (int i = 0; i < servlets.size(); i++) {
 				String name = ((Servlet) servlets.get(i)).getServletName();
 				if (prop.equals(name))
 					exists = true;
 			}
 		}
 		if (exists) {
 			String msg = WebMessages.getResourceString(WebMessages.ERR_SERVLET_DISPLAY_NAME_EXIST, new String[]{prop});
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 }
