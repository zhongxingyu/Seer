 /*******************************************************************************
  * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.servertarget.J2EEProjectServerTargetDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 public class FlexibleProjectCreationDataModel extends WTPOperationDataModel {
 	/**
 	 * An optional dataModel property for a <code>Boolean</code> type. The default value is
 	 * <code>Boolean.TRUE</code>. If this property is set to <code>Boolean.TRUE</code> then the
 	 * server target specified by dataModel property <code>SERVER_TARGET_ID</code> will be set on
 	 * the generated artifact.
 	 * 
 	 * @see SERVER_TARGET_ID
 	 */
 	public static final String ADD_SERVER_TARGET = "FlexibleProjectCreationDataModel.ADD_SERVER_TARGET"; //$NON-NLS-1$
 
 	/**
 	 * An optional dataModel property for a <code>java.lang.String</code> type. This is used to
 	 * specify the server target and is required if the <code>ADD_SERVER_TARGET</code> property is
 	 * set to <code>Boolean.TRUE</code>.
 	 * 
 	 * @see ServerTargetDataModel.RUNTIME_TARGET_ID
 	 */
 	public static final String SERVER_TARGET_ID = J2EEProjectServerTargetDataModel.RUNTIME_TARGET_ID;
 
 	private J2EEProjectServerTargetDataModel serverTargetDataModel;
 	private static final String NESTED_MODEL_SERVER_TARGET = "FlexibleProjectCreationDataModel.NESTED_MODEL_SERVER_TARGET"; //$NON-NLS-1$
 
 	private ProjectCreationDataModel projectDataModel;
	public static final String PROJECT_NAME = "FlexibleProjectCreationDataModel.PROJECT_NAME"; //$NON-NLS-1$
	public static final String PROJECT_LOCATION = "FlexibleProjectCreationDataModel.PROJECT_LOCATION"; //$NON-NLS-1$
 	private static final String NESTED_MODEL_PROJECT_CREATION = "FlexibleProjectCreationDataModel.NESTED_MODEL_PROJECT_CREATION"; //$NON-NLS-1$
 	
 	protected void initValidBaseProperties() {
 		addValidBaseProperty(ADD_SERVER_TARGET);
 		addValidBaseProperty(PROJECT_NAME);
 		addValidBaseProperty(PROJECT_LOCATION);
 		super.initValidBaseProperties();
 	}
 	
 	protected IStatus doValidateProperty(String propertyName) {
 		if (PROJECT_NAME.equals(propertyName)) {
 			return validateProjectName();
 		} 
 		return super.doValidateProperty(propertyName);
 	}
 	
 	private IStatus validateProjectName() {
 		String projectName = getStringProperty(PROJECT_NAME);
 		if (projectName != null && projectName.length() != 0) {
 			IProject project = ProjectUtilities.getProject(projectName);
 			if (project != null && project.exists()) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_PROJECT_NAME_EXISTS);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 	
 	protected Object getDefaultProperty(String propertyName) {
 		if (PROJECT_LOCATION.equals(propertyName)) {
 			return getDefaultLocation();
 		}
 		if (propertyName.equals(ADD_SERVER_TARGET)) {
 			return Boolean.TRUE;
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	private String getDefaultLocation() {
 		IPath path = getRootLocation();
 		String projectName = (String) getProperty(PROJECT_NAME);
 		if (projectName != null)
 			path = path.append(projectName);
 		return path.toOSString();
 	}
 
 	private IPath getRootLocation() {
 		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
 	}
 	
 	public final J2EEProjectServerTargetDataModel getServerTargetDataModel() {
 		return serverTargetDataModel;
 	}
 	
 	protected void initNestedModels() {
 		super.initNestedModels();
 		initProjectModel();
 		addNestedModel(NESTED_MODEL_PROJECT_CREATION, projectDataModel);
 
 		serverTargetDataModel = new J2EEProjectServerTargetDataModel();
 		addNestedModel(NESTED_MODEL_SERVER_TARGET, serverTargetDataModel);
 	}
 	
 	protected void initProjectModel() {
 		projectDataModel = new ProjectCreationDataModel();
 	}
 	
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		super.doSetProperty(propertyName, propertyValue);
 		if (PROJECT_NAME.equals(propertyName)) {
 		    projectDataModel.setProperty(ProjectCreationDataModel.PROJECT_NAME, propertyValue);
 			serverTargetDataModel.setProperty(J2EEProjectServerTargetDataModel.PROJECT_NAME, propertyValue);
 		}
 		return true;
 	}
 	
     public WTPOperation getDefaultOperation() {
         return new FlexibleProjectCreationOperation(this);
     }
     
 	protected final void setProjectDataModel(ProjectCreationDataModel projectDataModel) {
 		this.projectDataModel = projectDataModel;
 	}
 
 	public final ProjectCreationDataModel getProjectDataModel() {
 		return projectDataModel;
 	}
 }
