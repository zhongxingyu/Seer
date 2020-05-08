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
 package org.eclipse.jst.j2ee.application.operations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.project.J2EECreationResourceHandler;
 import org.eclipse.wst.common.frameworks.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.frameworks.operations.WTPOperationDataModelEvent;
 import org.eclipse.wst.common.frameworks.operations.WTPPropertyDescriptor;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.IProjectProperties;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonMessages;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 
 /**
  * This dataModel is a common super class used for creation of WTP Modules.
  * 
  * This class (and all its fields and methods) is likely to change during the
  * WTP 1.0 milestones as the new project structures are adopted. Use at your own
  * risk.
  * 
  * @since WTP 1.0
  */
 public abstract class J2EECreationDataModel extends WTPOperationDataModel {
     
     /**
      * Required
      */
    public static final String PROJECT_NAME = "FlexibleJ2EECreationDataModel.PROJECT_NAME"; //$NON-NLS-1$
 
     /**
      * Required
      */
    public static final String MODULE_NAME = "FlexibleJ2EECreationDataModel.MODULE_NAME"; //$NON-NLS-1$
 	/**
 	 * Required
 	 */	
 	
	public static final String MODULE_DEPLOY_NAME = "FlexibleJ2EECreationDataModel.MODULE_DEPLOY_NAME"; //$NON-NLS-1$
 	
     /**
      * An optional dataModel propertyName for a <code>Boolean</code> type. The
      * default value is <code>Boolean.TRUE</code>. If this property is set to
      * <code>Boolean.TRUE</code> then a default deployment descriptor and
      * supporting bindings files will be generated.
      */
    public static final String CREATE_DEFAULT_FILES = "FlexibleJ2EECreationDataModel.CREATE_DEFAULT_FILES"; //$NON-NLS-1$
 
     /**
      * An optional dataModel propertyName for a <code>Boolean</code> type. The
      * default value is <code>Boolean.TRUE</code>. If this property is set to
      * <code>Boolean.TRUE</code> then a default deployment descriptor and
      * supporting bindings files will be generated.
      */
    public static final String SHOULD_CREATE_PROJECT = "FlexibleJ2EECreationDataModel.SHOULD_CREATE_PROJECT"; //$NON-NLS-1$
 
     /**
      * Optional, type String
      */
    public static final String FINAL_PERSPECTIVE = "FlexibleJ2EECreationDataModel.FINAL_PERSPECTIVE"; //$NON-NLS-1$
 
    protected static final String IS_ENABLED = "FlexibleJ2EECreationDataModel.IS_ENABLED"; //$NON-NLS-1$
 	/**
 	 * type Integer
 	 */
	public static final String J2EE_MODULE_VERSION = "FlexibleJ2EEModuleCreationDataModel.J2EE_MODULE_VERSION"; //$NON-NLS-1$
 	/**
 	 * type Integer
 	 */
	public static final String VALID_MODULE_VERSIONS_FOR_PROJECT_RUNTIME = "FlexibleJ2EEModuleCreationDataModel.VALID_MODULE_VERSIONS_FOR_PROJECT_RUNTIME"; //$NON-NLS-1$
 
 	/**
 	 * This corresponds to the J2EE versions of 1.2, 1.3, 1.4, etc. Each subclass will convert this
 	 * version to its corresponding highest module version supported by the J2EE version and set the
 	 * J2EE_MODULE_VERSION property.
 	 * 
 	 * type Integer
 	 */
	public static final String J2EE_VERSION = "FlexibleJ2EEModuleCreationDataModel.J2EE_VERSION"; //$NON-NLS-1$
     /* (non-Javadoc)
      * @see org.eclipse.wst.common.frameworks.operations.WTPOperationDataModel#init()
      */
     protected void init() {
         super.init();
 		setProperty(J2EE_MODULE_VERSION, getDefaultProperty(J2EE_MODULE_VERSION));
     }
     
     protected void initValidBaseProperties() {
         addValidBaseProperty(PROJECT_NAME);
         addValidBaseProperty(MODULE_NAME);
         addValidBaseProperty(MODULE_DEPLOY_NAME);
         addValidBaseProperty(CREATE_DEFAULT_FILES);
         addValidBaseProperty(IS_ENABLED);
         addValidBaseProperty(FINAL_PERSPECTIVE);
 		addValidBaseProperty(J2EE_MODULE_VERSION);
 		addValidBaseProperty(VALID_MODULE_VERSIONS_FOR_PROJECT_RUNTIME);
 		addValidBaseProperty(J2EE_VERSION);
         super.initValidBaseProperties();
     }
 
     protected Boolean basicIsEnabled(String propertyName) {
         return (Boolean) getProperty(IS_ENABLED);
     }
 
     public void propertyChanged(WTPOperationDataModelEvent event) {
         if (event.getFlag() == WTPOperationDataModelEvent.PROPERTY_CHG) {
             event.getDataModel();
         }
         super.propertyChanged(event);
     }
 
     protected boolean doSetProperty(String propertyName, Object propertyValue) {
         super.doSetProperty(propertyName, propertyValue);
         if (PROJECT_NAME.equals(propertyName)) {
             IProject project = ProjectUtilities.getProject(propertyValue);
 			if (project != null) {
 	            IProjectProperties projProperties = ServerCore.getProjectProperties(project);
 	            String[] validModuleVersions = getServerVersions(getModuleID(), projProperties.getRuntimeTarget().getRuntimeType());
 	            setProperty(VALID_MODULE_VERSIONS_FOR_PROJECT_RUNTIME, validModuleVersions);
 			}
         } else if (IS_ENABLED.equals(propertyName)) {
             notifyEnablementChange(PROJECT_NAME);
         } else if (propertyName.equals(J2EE_VERSION)) {
 			Integer modVersion = convertJ2EEVersionToModuleVersion((Integer) propertyValue);
 			setProperty(J2EE_MODULE_VERSION, modVersion);
 			return false;
 		} else if (MODULE_NAME.equals(propertyName))
 			setProperty(MODULE_DEPLOY_NAME,propertyValue);
         return true;
     }
     
 	protected WTPPropertyDescriptor[] doGetValidPropertyDescriptors(String propertyName) {
 		if (propertyName.equals(J2EE_MODULE_VERSION)) {
 			return getValidJ2EEModuleVersionDescriptors();
 		}
 		return super.doGetValidPropertyDescriptors(propertyName);
 	}
 	
 	public final int getJ2EEVersion() {
 		return convertModuleVersionToJ2EEVersion(getIntProperty(J2EE_MODULE_VERSION));
 	}
 	
     protected IStatus doValidateProperty(String propertyName) {
         if (propertyName.equals(MODULE_NAME)) {
             IStatus status = OK_STATUS;
             String moduleName = getStringProperty(MODULE_NAME);
             if (status.isOK()) {
                 if (moduleName.indexOf("#") != -1) { //$NON-NLS-1$
                     String errorMessage = J2EECreationResourceHandler.getString("InvalidCharsError"); //$NON-NLS-1$
                     return WTPCommonPlugin.createErrorStatus(errorMessage);
                 } else if (moduleName==null || moduleName.equals("")) { //$NON-NLS-1$
 					String errorMessage = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_EMPTY_MODULE_NAME);
 					return WTPCommonPlugin.createErrorStatus(errorMessage); 
                 }
             } else
                 return status;
 
         } else if (propertyName.equals(NESTED_MODEL_VALIDATION_HOOK)) {
             return OK_STATUS;
         }  else if (J2EE_MODULE_VERSION.equals(propertyName)) {
 			return validateJ2EEModuleVersionProperty();
 		} else if (propertyName.equals(PROJECT_NAME)) {
 			String projectName = getStringProperty(PROJECT_NAME);
 			if (projectName == null || projectName.length()==0) {
 				String errorMessage = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_EMPTY_PROJECT_NAME);
 				return WTPCommonPlugin.createErrorStatus(errorMessage); 
 			}
 		}
         return super.doValidateProperty(propertyName);
     }
     
 	private IStatus validateJ2EEModuleVersionProperty() {
 		int j2eeVersion = getIntProperty(J2EE_MODULE_VERSION);
 		if (j2eeVersion == -1)
 			return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.J2EE_SPEC_LEVEL_NOT_FOUND));
 		return OK_STATUS;
 	}
 	
     protected Object getDefaultProperty(String propertyName) {
         if (propertyName.equals(CREATE_DEFAULT_FILES) || propertyName.equals(IS_ENABLED)) {
             return Boolean.TRUE;
         } else if (propertyName.equals(J2EE_MODULE_VERSION)) {
 			return getDefaultJ2EEModuleVersion();
 		}
         return super.getDefaultProperty(propertyName);
     }
 
     protected boolean isResultProperty(String propertyName) {
         if (propertyName.equals(FINAL_PERSPECTIVE))
             return true;
         return super.isResultProperty(propertyName);
     }
 	/**
 	 * Subclasses should override to convert the j2eeVersion to a module version id. By default we
 	 * return the j2eeVersion which is fine if no conversion is necessary.
 	 * 
 	 * @param integer
 	 * @return
 	 */
 	protected Integer convertJ2EEVersionToModuleVersion(Integer j2eeVersion) {
 		return j2eeVersion;
 	}
 	
 	protected abstract WTPPropertyDescriptor[] getValidJ2EEModuleVersionDescriptors();
 
 	protected abstract int convertModuleVersionToJ2EEVersion(int moduleVersion);
 
 	protected abstract EClass getModuleType();
 
 	protected abstract String getModuleExtension();
 	
 	protected abstract Integer getDefaultJ2EEModuleVersion();
 	
 	protected abstract String getModuleID();
 	
 	public static String[] getServerVersions(String moduleID, IRuntimeType type) {
         List list = new ArrayList();
         if (type == null)
             return null;
         IModuleType[] moduleTypes = type.getModuleTypes();
         if (moduleTypes != null) {
             int size = moduleTypes.length;
             for (int i = 0; i < size; i++) {
                 IModuleType moduleType = moduleTypes[i];
                 if (matches(moduleType.getId(), moduleID)) {
                     list.add(moduleType.getVersion());
                 }
 
             }
         }
         String[] versions = null;
         if (!list.isEmpty()) {
             versions = new String[list.size()];
             list.toArray(versions);
         }
         return versions;
     }
 
     private static boolean matches(String a, String b) {
         if (a == null || b == null || "*".equals(a) || "*".equals(b) || a.startsWith(b) || b.startsWith(a)) //$NON-NLS-1$ //$NON-NLS-2$
             return true;
         return false;
     }
 }
