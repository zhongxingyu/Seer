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
 
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassDataModel;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.Servlet;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditOperationDataModel;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * The NewServletClassData model is a subclass of WTPOperationDataModel and follows the WTP Operation and WTP Wizard frameworks.
  * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel
  * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperation
  * 
  * This data model is a subclass of the NewJavaClassDataModel, which stores base properties necessary in
  * the creation of a default java class. 
  * @see org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassDataModel
  * 
  * The NewServletClassDataModel provides more specific properties for java class creation that are required
  * in creating a servlet java class.  The data model is used to store these values for the NewServletClassOperation.
  * That operation will create the servlet java class based on the settings defined here in the data model.
  * @see org.eclipse.jst.j2ee.internal.web.operations.NewServletClassOperation
  * 
  * This data model implements the IAnnotationsDataModel to get the USE_ANNOTATIONS property for determining
  * whether or not to generate an annotated java class.
  * @see org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel
  * 
  * Clients can subclass this data model to cache and provide their own specific attributes.  They should also provide their
  * own validation methods and default values for the properties they add.
  * 
  * The use of this class is EXPERIMENTAL and is subject to substantial changes.
  */
 public class NewServletClassDataModel extends NewJavaClassDataModel implements IAnnotationsDataModel {
 
 	/**
 	 * Optional, boolean property used to specify whether to generate the init method.
 	 * The default is false.
 	 */
 	public static final String INIT = "NewServletClassDataModel.INIT"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the doPost method.
 	 * The default is true.
 	 */
 	public static final String DO_POST = "NewServletClassDataModel.DO_POST"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the destroy method.
 	 * The default is false.
 	 */
 	public static final String DESTROY = "NewServletClassDataModel.DESTROY"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the toString method.
 	 * The default is false.
 	 */
 	public static final String TO_STRING = "NewServletClassDataModel.TO_STRING"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the doPut method.
 	 * The default is false.
 	 */
 	public static final String DO_PUT = "NewServletClassDataModel.DO_PUT"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the doGet method.
 	 * The default is true.
 	 */
 	public static final String DO_GET = "NewServletClassDataModel.DO_GET"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the getServletInfo method.
 	 * The default is false.
 	 */
 	public static final String GET_SERVLET_INFO = "NewServletClassDataModel.GET_SERVLET_INFO"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to specify whether to generate the doDelete method.
 	 * The default is false.
 	 */
 	public static final String DO_DELETE = "NewServletClassDataModel.DO_DELETE"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to determine if building JSP or servlet.
 	 * The default is true.
 	 */
 	public static final String IS_SERVLET_TYPE = "NewServletClassDataModel.IS_SERVLET_TYPE"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, List property used to cache all the init params defined on the servlet.
 	 */
 	public static final String INIT_PARAM = "NewServletClassDataModel.INIT_PARAM"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, List propety used to cache all the servlet mappings for this servlet on the web application.
 	 */
 	public static final String URL_MAPPINGS = "NewServletClassDataModel.URL_MAPPINGS"; //$NON-NLS-1$
 
 	/**
 	 * Required, String property of the display name for the servlet
 	 */
 	public static final String DISPLAY_NAME = "NewServletClassDataModel.DISPLAY_NAME"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, String property of the description info for the servlet
 	 */
 	public static final String DESCRIPTION = "NewServletClassDataModel.DESCRIPTION"; //$NON-NLS-1$
 	
 	/**
 	 * The fully qualified default servlet superclass: HttpServlet.
 	 */
 	public final static String SERVLET_SUPERCLASS = "javax.servlet.http.HttpServlet"; //$NON-NLS-1$ 
 	
 	/**
 	 * String array of the default, minimum required fully qualified Servlet interfaces
 	 */
 	public final static String[] SERVLET_INTERFACES = {"javax.servlet.Servlet"}; //$NON-NLS-1$
 	
 	/**
 	 * The javajet template file used in creating the servlet template class
 	 */
 	public static final String TEMPLATE_FILE = "servletXDoclet.javajet"; //$NON-NLS-1$
 	
 	public static final String NON_ANNOTATED_TEMPLATE_FILE = "servletXDocletNonAnnotated.javajet"; //$NON-NLS-1$
 	/**
 	 * The cache of all the interfaces the servlet java class will implement.
 	 */
 	private List interfaceList;
 	
 	private static boolean useAnnotations = true;
 	
 	/**
 	 * Subclasses may extend this method to provide their own default operation for this
 	 * data model.  This implementation uses the AddServletOperation to drive the servlet
 	 * creation.  It will not return null.
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#getDefaultOperation()
 	 * 
 	 * @return WTPOperation AddServletOperation
 	 */
 	public WTPOperation getDefaultOperation() {
 		return new AddServletOperation(this);
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own determination of whether or not
 	 * certain properties should be disabled or enabled.  This method does not accept null parameter.
 	 * It will not return null.  This implementation makes sure annotation support is only allowed
 	 * on web projects of J2EE version 1.3 or higher.
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#basicIsEnabled(String)
 	 * @see IAnnotationsDataModel#USE_ANNOTATIONS
 	 * 
 	 * @param propertyName
 	 * @return Boolean should property be enabled?
 	 */
 	protected Boolean basicIsEnabled(String propertyName) {
 		// Annotations should only be enabled on a valid j2ee project of version 1.3 or higher
 		if (USE_ANNOTATIONS.equals(propertyName)) {
 			if (!isAnnotationsSupported())
 				return Boolean.FALSE;
 			return Boolean.TRUE;
 		}
 		// Otherwise return super implementation
 		return super.basicIsEnabled(propertyName);
 	}
 	
 	/**
 	 * Subclasses may extend this method to add their own specific data model properties as valid
 	 * base properties.  This implementation adds the servlet specific properties to those added
 	 * by the NewJavaClassDataModel.
 	 * @see NewJavaClassDataModel#initValidBaseProperties()
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		// Add servlet specific properties defined in this data model
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
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own default values for
 	 * any of the properties in the data model hierarchy.  This method does not
 	 * accept a null parameter.  It may return null.  This implementation sets
 	 * annotation use to be true, and to generate a servlet with doGet and doPost.
 	 * @see NewJavaClassDataModel#getDefaultProperty(String)
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#getDefaultProperty(String)
 	 * 
 	 * @param propertyName
 	 * @return Object default value of property
 	 */
 	protected Object getDefaultProperty(String propertyName) {
 		// Generate a doPost method by default
 		if (propertyName.equals(DO_POST))
 			return new Boolean(true);
 		// Generate a doGet method by default
 		else if (propertyName.equals(DO_GET))
 			return new Boolean(true);
 		// Use servlet by default
 		else if (propertyName.equals(IS_SERVLET_TYPE))
 			return new Boolean(true);
 		// Create an annotated servlet java class by default
 		else if (propertyName.equals(USE_ANNOTATIONS))
 			return shouldDefaultAnnotations();
 		else if (propertyName.equals(DISPLAY_NAME))
 			return getProperty(CLASS_NAME);
 		else if (propertyName.equals(URL_MAPPINGS)) {
 			return getDefaultUrlMapping();
 		}
 		// Otherwise check super for default value for property
 		return super.getDefaultProperty(propertyName);
 	}
 
 	/**
 	 * Returns the default Url Mapping depending upon the display name of 
 	 * the Servlet
 	 * @return List containting the default Url Mapping 
 	 */
 	private Object getDefaultUrlMapping() {
 		List urlMappings = null;
 		String text = (String)getProperty(DISPLAY_NAME);
 		if (text != null) {
 			urlMappings = new ArrayList();
 			urlMappings.add(new String[]{"/" + text}); //$NON-NLS-1$
 		}
 		return urlMappings;
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own specific behaviour when a certain
 	 * property in the data model heirarchy is set.  This method does not accept null for
 	 * the property name, but it will for propertyValue.  It will not return null.  It will return
 	 * false if the set fails.  This implementation verifies the display name is set to the
 	 * classname, that the annotations is disabled/enabled properly, and that the target project
 	 * name is determined from the source folder setting.
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#doSetProperty(String, Object)
 	 * 
 	 * @param propertyName
 	 * @param propertyValue
 	 * @return boolean was property set?
 	 */
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		
 		// If annotations is changed, notify an enablement change
 		if (propertyName.equals(USE_ANNOTATIONS)) {
 			useAnnotations = ((Boolean) propertyValue).booleanValue();
 			if (useAnnotations && !isAnnotationsSupported())
 				return true;
 			notifyEnablementChange(USE_ANNOTATIONS);
 		}
 		// If the source folder is changed, ensure we have the correct project name
 		if (propertyName.equals(SOURCE_FOLDER)) {
 			// Get the project name from the source folder name
 			String sourceFolder = (String) propertyValue;
 			int index = sourceFolder.indexOf(File.separator);
 			String projectName = sourceFolder;
 			if (index == 0)
 				projectName = sourceFolder.substring(1);
 			index = projectName.indexOf(File.separator);
 			if (index != -1) {
 				projectName = projectName.substring(0, index);
 				setProperty(PROJECT_NAME, projectName);
 			}
 		}
 		// Call super to set the property on the data model
 		boolean result = super.doSetProperty(propertyName, propertyValue);
 		//	If class name is changed, update the display name to be the same
 		if (propertyName.equals(CLASS_NAME) && !isSet(DISPLAY_NAME)) {
 			notifyDefaultChange(DISPLAY_NAME);
 		}
 		// After the property is set, if project changed, update the nature and the annotations enablement
 		if (propertyName.equals(MODULE_NAME)) {
 			notifyEnablementChange(USE_ANNOTATIONS);
 		}
 		// After property is set, if annotations is set to true, update its value based on the new level of the project
 		if (getBooleanProperty(USE_ANNOTATIONS)) {
 			if (!isAnnotationsSupported())
 				setBooleanProperty(USE_ANNOTATIONS, false);
 		}
 		// Return whether property was set
 		return result;
 	}
 	
 	protected boolean isAnnotationsSupported() {
 		
 		if (!isSet(ArtifactEditOperationDataModel.PROJECT_NAME))
 			return true;
 		if (getStringProperty(ArtifactEditOperationDataModel.PROJECT_NAME).equals(""))
 			return true;
 		IProject project = ProjectUtilities.getProject(getStringProperty(ArtifactEditOperationDataModel.PROJECT_NAME));
 		String moduleName = getStringProperty(ArtifactEditOperationDataModel.MODULE_NAME);
 		if (project == null || moduleName == null || moduleName.equals("")) return true;
 		ComponentHandle handle = ComponentHandle.create(project,moduleName);
 		WebArtifactEdit webEdit = null;
 		try {
 			webEdit = WebArtifactEdit.getWebArtifactEditForRead(handle);
 			if (webEdit == null)
				return true;
 			return webEdit.getJ2EEVersion() > J2EEVersionConstants.VERSION_1_2;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			if (webEdit != null)
 				webEdit.dispose();
 		}
 	}
 	
 	/**
 	 * Subclasses may extend this method to provide their own validation on any of the valid
 	 * data model properties in the hierarchy.  This implementation adds validation for
 	 * the init params, servlet mappings, display name, and existing class
 	 * fields specific to the servlet java class creation.  It does not accept a null 
 	 * parameter.  This method will not return null.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * @see org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel#doValidateProperty(String)
 	 * 
 	 * @param propertyName
 	 * @return IStatus is property value valid?
 	 */
 	protected IStatus doValidateProperty(String propertyName) {
 		// If our default is the superclass, we know it is ok
 		if (propertyName.equals(SUPERCLASS) && getStringProperty(propertyName).equals(SERVLET_SUPERCLASS))
 			return WTPCommonPlugin.OK_STATUS;
 		IStatus result = super.doValidateProperty(propertyName);
 		if (!result.isOK())
 			return result;
 		// Validate init params
 		if (propertyName.equals(INIT_PARAM))
 			return validateInitParamList((List) getProperty(propertyName));
 		// Validate servlet mappings
 		if (propertyName.equals(URL_MAPPINGS))
 			return validateURLMappingList((List) getProperty(propertyName));
 		// Validate the servlet name in DD
 		if (propertyName.equals(DISPLAY_NAME))
 			return validateDisplayName(getStringProperty(propertyName));
 		
 		// Otherwise defer to super to validate the property
 		return result;
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own validation of the specified
 	 * java classname.  This implementation will ensure the class name is not set to Servlet
 	 * and then will forward on to the NewJavaClassDataModel to validate the class name as
 	 * valid java.  This method does not accept null as a parameter.  It will not return null.
 	 * @see NewServletClassDataModel#validateExistingClass(boolean)
 	 * @see NewJavaClassDataModel#validateJavaClassName(String)
 	 * 
 	 * @param className
 	 * @return IStatus is java classname valid?
 	 */
 	protected IStatus validateJavaClassName(String className) {
 		// First use the NewJavaClassDataModel to validate the classname as proper java syntax
 		IStatus status = super.validateJavaClassName(className);
 		if (status.isOK()) {
 			// Do not allow the name to be "Servlet"
 			if (className.equals("Servlet")) { //$NON-NLS-1$
 				String msg = WebMessages.getResourceString(WebMessages.ERR_SERVLET_JAVA_CLASS_NAME_INVALID);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			return WTPCommonPlugin.OK_STATUS;
 		}
 		// Return the status
 		return status;
 	}
 	
 	/**
 	 * This method is intended for internal use only.  It will be used to validate the init params list
 	 * to ensure there are not any duplicates.  This method will accept a null paramter.  It will
 	 * not return null.
 	 * @see NewServletClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param prop
 	 * @return IStatus is init params list valid?
 	 */
 	private IStatus validateInitParamList(List prop) {
 		if (prop != null && !prop.isEmpty()) {
 			// Ensure there are not duplicate entries in the list
 			boolean dup = hasDuplicatesInStringArrayList(prop);
 			if (dup) {
 				String msg = WebMessages.getResourceString(WebMessages.ERR_DUPLICATED_INIT_PARAMETER);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		// Return OK
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will validate the servlet mappings
 	 * list and ensure there are not duplicate entries.  It will accept a null parameter.
 	 * It will not return null.
 	 * @see NewServletClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param prop
 	 * @return IStatus is servlet mapping list valid?
 	 */
 	private IStatus validateURLMappingList(List prop) {
 		if (prop != null && !prop.isEmpty()) {
 			// Ensure there are not duplicates in the mapping list
 			boolean dup = hasDuplicatesInStringArrayList(prop);
 			if (dup) {
 				String msg = WebMessages.getResourceString(WebMessages.ERR_DUPLICATED_URL_MAPPING);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		} else {
 			String msg = WebMessages.getResourceString(WebMessages.ERR_SERVLET_MAPPING_URL_PATTERN_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Return OK
 		return WTPCommonPlugin.OK_STATUS;
 	}
 	
 	/**
 	 * This method is intended for internal use only. It provides a simple
 	 * algorithm for detecting if there are duplicate entries in a list.  It will
 	 * accept a null paramter.  It will not return null.
 	 * @see NewServletClassDataModel#validateInitParamList(List)
 	 * @see NewServletClassDataModel#validateURLMappingList(List)
 	 * 
 	 * @param input
 	 * @return boolean are there dups in the list?
 	 */
 	private boolean hasDuplicatesInStringArrayList(List input) {
 		// If list is null or empty return false
 		if (input == null)
 			return false;
 		int n = input.size();
 		boolean dup = false;
 		// nested for loops to check each element to see if other elements are the same
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
 		// Return boolean status for duplicates
 		return dup;
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This checks to see if the two string
 	 * arrays are equal.  If either of the arrays are null or empty, it returns false.
 	 * @see NewServletClassDataModel#hasDuplicatesInStringArrayList(List)
 	 * 
 	 * @param sArray1
 	 * @param sArray2
 	 * @return boolean are Arrays equal?
 	 */
 	private boolean isTwoStringArraysEqual(String[] sArray1, String[] sArray2) {
 		// If either array is null, return false
 		if (sArray1 == null || sArray2 == null)
 			return false;
 		int n1 = sArray1.length;
 		int n2 = sArray1.length;
 		// If either array is empty, return false
 		if (n1 == 0 || n2 == 0)
 			return false;
 		// If they don't have the same length, return false
 		if (n1 != n2)
 			return false;
 		// If their first elements do not match, return false
 		if (!sArray1[0].equals(sArray2[0]))
 			return false;
 		// Otherwise return true
 		return true;
 	}
 	
 	/**
 	 * This method will return the list of servlet interfaces to be implemented for the
 	 * new servlet java class.  It will intialize the list using lazy initialization to 
 	 * the minimum interfaces required by the data model SERVLET_INTERFACES.  This method
 	 * will not return null.
 	 * @see NewServletClassDataModel#SERVLET_INTERFACES
 	 * 
 	 * @return List of servlet interfaces to be implemented
 	 */
 	public final List getServletInterfaces() {
 		if (interfaceList == null) {
 			interfaceList = new ArrayList();
 			// Add minimum required list of servlet interfaces to be implemented
 			for (int i = 0; i < SERVLET_INTERFACES.length; i++) {
 				interfaceList.add(SERVLET_INTERFACES[i]);
 			}
 		}
 		// Return interface list
 		return interfaceList;
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This will validate whether the display name
 	 * selected is a valid display name for the servlet in the specified web application.  It will
 	 * make sure the name is not empty and that it doesn't already exist in the web app.  This
 	 * method will accept null as a parameter.  It will not return null.
 	 * @see NewServletClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param prop
 	 * @return IStatus is servlet display name valid?
 	 */
 	private IStatus validateDisplayName(String prop) {
 		// Ensure the servlet display name is not null or empty
 		if (prop == null || prop.trim().length() == 0) {
 			String msg = WebMessages.getResourceString(WebMessages.ERR_DISPLAY_NAME_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		if (getTargetProject()==null || getComponent()==null)
 			return WTPCommonPlugin.OK_STATUS;
 		ArtifactEdit edit = null;
 		try {
 			edit = getArtifactEditForRead();
 			WebApp webApp = (WebApp) edit.getContentModelRoot();
 			if (webApp == null)
 				return WTPCommonPlugin.OK_STATUS;
 			List servlets = webApp.getServlets();
 			boolean exists = false;
 			// Ensure the display does not already exist in the web application
 			if (servlets != null && !servlets.isEmpty()) {
 				for (int i = 0; i < servlets.size(); i++) {
 					String name = ((Servlet) servlets.get(i)).getServletName();
 					if (prop.equals(name))
 						exists = true;
 				}
 			}
 			// If the servlet name already exists, throw an error
 			if (exists) {
 				String msg = WebMessages.getResourceString(WebMessages.ERR_SERVLET_DISPLAY_NAME_EXIST, new String[]{prop});
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		} finally {
 			if (edit!=null)
 				edit.dispose();
 		}
 		
 		//Otherwise, return OK
 		return WTPCommonPlugin.OK_STATUS;
 	}
 	
 	/**
 	 * Subclasses may extend this method to perform their own retrieval mechanism.
 	 * This implementation simply returns the JDT package fragment root for the selected source
 	 * folder.  This method may return null.
 	 * @see IJavaProject#getPackageFragmentRoot(org.eclipse.core.resources.IResource)
 	 * 
 	 * @return IPackageFragmentRoot
 	 */
 	public IPackageFragmentRoot getJavaPackageFragmentRoot() {
 		// Ensure project is not null
 		IProject project = getTargetProject();
 		if (project == null)
 			return null;
 
 		IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 		if (javaProject != null) {
 			// Ensure source folder exists
 			IFolder sourcefolder = getJavaSourceFolder();
 			// Get the java package for the selected folder
 			if (sourcefolder != null)
 				return javaProject.getPackageFragmentRoot(sourcefolder);
 		}
 		return null;
 	}
 	
 	/**
 	 * Subclasses may extend this method to perform their own retrieval of a default java source folder.
 	 * This implementation returns the JavaSource folder as default since we are in a
 	 * web project.  This method may return null.
 	 * @see NewJavaClassDataModel#getDefaultJavaSourceFolder()
 	 * 
 	 * @return IFolder default java source folder
 	 */
 	protected IFolder getDefaultJavaSourceFolder() {
 		if (getComponent()!=null) {
 			IPackageFragmentRoot[] sourceContainers = ComponentUtilities.getSourceContainers(getComponent());
 			if (sourceContainers.length>0)
 				return (IFolder) sourceContainers[0].getResource();
 		}
 		return null;
 	}
 	
 	/**
 	 * @return boolean should the default annotations be true?
 	 */
 	private static Boolean shouldDefaultAnnotations() {
 		if (useAnnotations)
 			return Boolean.TRUE;
 		return Boolean.FALSE;
 	}
 }
