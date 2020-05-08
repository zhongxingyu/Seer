 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.earcreation;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.application.internal.operations.DefaultJ2EEComponentCreationOperation;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.AppClientFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.internal.moduleextension.EjbModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.JcaModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.WebModuleExtension;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonMessages;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class DefaultJ2EEComponentCreationDataModelProvider extends AbstractDataModelProvider implements IDefaultJ2EEComponentCreationDataModelProperties {
 	private static String CREATE_BASE = "DefaultJ2EEComponentCreationDataModel.CREATE_"; //$NON-NLS-1$
 
 	private static final int EJB = 0;
 	private static final int WEB = 1;
 	private static final int RAR = 2;
 	private static final int CLIENT = 3;
 
 	private static final String WEB_SUFFIX = "Web"; //$NON-NLS-1$
 	private static final String EJB_SUFFIX = "EJB"; //$NON-NLS-1$
 	private static final String CLIENT_SUFFIX = "Client"; //$NON-NLS-1$
 	private static final String CONNECTOR_SUFFIX = "Connector"; //$NON-NLS-1$
 
 	private IDataModel ejbModel;
 	private IDataModel ejbFacetModel;
 	private IDataModel webModel;
 	private IDataModel webFacetModel;
 	private IDataModel jcaModel;
 	private IDataModel jcaFacetModel;
 	private IDataModel clientModel;
 	private IDataModel clientFacetModel;
 
 	public DefaultJ2EEComponentCreationDataModelProvider() {
 		super();
 	}
 
 	public Set getPropertyNames() {
 		Set propertyNames = super.getPropertyNames();
 		propertyNames.add(PROJECT_NAME);
 		propertyNames.add(EAR_COMPONENT_NAME);
 		propertyNames.add(APPCLIENT_COMPONENT_NAME);
 		propertyNames.add(WEB_COMPONENT_NAME);
 		propertyNames.add(EJB_COMPONENT_NAME);
 		propertyNames.add(CONNECTOR_COMPONENT_NAME);
 		propertyNames.add(J2EE_VERSION);
 		propertyNames.add(CREATE_EJB);
 		propertyNames.add(CREATE_WEB);
 		propertyNames.add(CREATE_APPCLIENT);
 		propertyNames.add(CREATE_CONNECTOR);
 		propertyNames.add(MODULE_NAME_COLLISIONS_VALIDATION);
 		propertyNames.add(ENABLED);
 		propertyNames.add(NESTED_MODEL_CLIENT);
 		propertyNames.add(NESTED_MODEL_EJB);
 		propertyNames.add(NESTED_MODEL_JCA);
 		propertyNames.add(NESTED_MODEL_WEB);
 		propertyNames.add(FACET_RUNTIME);
 		return propertyNames;
 	}
 
 
 	public IDataModelOperation getDefaultOperation() {
 		return new DefaultJ2EEComponentCreationOperation(model);
 	}
 
 	public void init() {
 		initNestedCreationModels();
 		super.init();
 	}
 
 	protected void initNestedCreationModels() {
 		clientModel = DataModelFactory.createDataModel(new AppClientFacetProjectCreationDataModelProvider());
 		model.addNestedModel(NESTED_MODEL_CLIENT, clientModel);
 		clientFacetModel = ((FacetDataModelMap)clientModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP)).getFacetDataModel(J2EEProjectUtilities.APPLICATION_CLIENT);
 		clientFacetModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR, false);
 		
 		EjbModuleExtension ejbExt = EarModuleManager.getEJBModuleExtension();
 		if (ejbExt != null) {
 			ejbModel = ejbExt.createProjectDataModel();
 			if (ejbModel != null){
 				model.addNestedModel(NESTED_MODEL_EJB, ejbModel);
 				ejbFacetModel = ((FacetDataModelMap)ejbModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP)).getFacetDataModel(J2EEProjectUtilities.EJB);
 				ejbFacetModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR, false);
 			}
 		}
 		WebModuleExtension webExt = EarModuleManager.getWebModuleExtension();
 		if (webExt != null) {
 			webModel = webExt.createProjectDataModel();
 			if (webModel != null){
 				model.addNestedModel(NESTED_MODEL_WEB, webModel);
 				webFacetModel = ((FacetDataModelMap)webModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP)).getFacetDataModel(J2EEProjectUtilities.DYNAMIC_WEB);
 				webFacetModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR, false);
 			}
 		}
 		JcaModuleExtension rarExt = EarModuleManager.getJCAModuleExtension();
 		if (rarExt != null) {
 			jcaModel = rarExt.createProjectDataModel();
 			if (jcaModel != null){
 				model.addNestedModel(NESTED_MODEL_JCA, jcaModel);
 				jcaFacetModel = ((FacetDataModelMap)jcaModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP)).getFacetDataModel(J2EEProjectUtilities.JCA);
 				jcaFacetModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR, false);
 			}
 		}
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.startsWith(CREATE_BASE))
 			return getDefaultCreateValue(propertyName);
 		if (propertyName.equals(ENABLED))
 			return Boolean.TRUE;
 		return super.getDefaultProperty(propertyName);
 	}
 
 
 	private Object getDefaultCreateValue(String propertyName) {
 		if (propertyName.equals(CREATE_CONNECTOR)) {
 			int version = getIntProperty(J2EE_VERSION);
 			if (version < J2EEVersionConstants.J2EE_1_3_ID)
 				return Boolean.FALSE;
 		}
 		return Boolean.TRUE;
 	}
 
 	private int convertPropertyNameToInt(String propertyName) {
 		if (propertyName.equals(CREATE_WEB)) {
 			return WEB;
 		} else if (propertyName.equals(CREATE_EJB)) {
 			return EJB;
 		} else if (propertyName.equals(CREATE_CONNECTOR)) {
 			return RAR;
 		} else if (propertyName.equals(CREATE_APPCLIENT)) {
 			return CLIENT;
 		}
 		return -1;
 	}
 
 	private String ensureUniqueProjectName(String projectName) {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		String newName = projectName;
 		int index = 0;
 		IProject proj = root.getProject(newName);
 		while (proj.exists()) {
 			index++;
 			newName = projectName + index;
 			proj = root.getProject(newName);
 		}
 		return newName;
 	}
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		boolean notify = super.propertySet(propertyName, propertyValue);
 		if (propertyName.equals(J2EE_VERSION)) {
 			updatedJ2EEVersion((Integer) propertyValue);
 			return true;
 		}else if (propertyName.startsWith(CREATE_BASE)){
 			notifyEnablement(convertPropertyNameToInt(propertyName));
 		}else if (propertyName.equals(EAR_COMPONENT_NAME)) {
 			setDefaultComponentNames((String) propertyValue);
 		}else if(webModel != null && propertyName.equals(WEB_COMPONENT_NAME)){
 			webModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, propertyValue);
 		}else if(clientModel != null && propertyName.equals(APPCLIENT_COMPONENT_NAME)){
 			clientModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, propertyValue);
 		}else if(ejbModel != null && propertyName.equals(EJB_COMPONENT_NAME)){
 			ejbModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, propertyValue);
 		}else if(jcaModel != null && propertyName.equals(CONNECTOR_COMPONENT_NAME)){
 			jcaModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, propertyValue);
 		} 
 		else if(FACET_RUNTIME.equals(propertyName)){
 			if(webModel != null){
 				webModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, propertyValue);
 			}
 			if(clientModel != null){
 				clientModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, propertyValue);
 			}
 			if(ejbModel != null){
 				ejbModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, propertyValue);
 			}
 			if(jcaModel != null){
 				jcaModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, propertyValue);
 			}
 		}
 		return notify;
 	}
 
 	private void notifyEnablement(int flag) {
 		String propertyName = null;
 		switch (flag) {
 			case EJB :
 				propertyName = EJB_COMPONENT_NAME;
 				break;
 			case WEB :
 				propertyName = WEB_COMPONENT_NAME;
 				break;
 			case CLIENT :
 				propertyName = APPCLIENT_COMPONENT_NAME;
 				break;
 			case RAR :
 				propertyName = CONNECTOR_COMPONENT_NAME;
 		}
 		if (propertyName != null)
 			model.notifyPropertyChange(propertyName, IDataModel.ENABLE_CHG);
 	}
 
 	private void updatedJ2EEVersion(Integer version) {
 		setNestedJ2EEVersion(version);
 		if (version.intValue() < J2EEVersionConstants.J2EE_1_3_ID && model.isPropertySet(CREATE_CONNECTOR)) {
 			model.setProperty(CREATE_CONNECTOR, Boolean.FALSE);
 		}
 	}
 
 	public IStatus validateModuleNameCollisions() {
 		if (getBooleanProperty(ENABLED)) {
 			ArrayList list = new ArrayList();
 			String projectName = null;
 			String actualProjectName = null;
 			boolean errorCollision = false;
 			boolean errorNoSelection = true;
 			if (getBooleanProperty(CREATE_APPCLIENT)) {
 				actualProjectName = clientModel.getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 				projectName = WTPPlugin.isPlatformCaseSensitive() ? actualProjectName : actualProjectName.toLowerCase();
 				list.add(projectName);
 				errorNoSelection = false;
 			}
 			if (getBooleanProperty(CREATE_EJB)) {
 				actualProjectName = ejbModel.getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 				projectName = WTPPlugin.isPlatformCaseSensitive() ? actualProjectName : actualProjectName.toLowerCase();
 				if (!list.contains(projectName)) {
 					list.add(projectName);
 				} else {
 					errorCollision = true;
 				}
 				errorNoSelection = false;
 			}
 			if (!errorCollision && getBooleanProperty(CREATE_WEB)) {
 				actualProjectName = webModel.getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 				projectName = WTPPlugin.isPlatformCaseSensitive() ? actualProjectName : actualProjectName.toLowerCase();
 				if (!list.contains(projectName)) {
 					list.add(projectName);
 				} else {
 					errorCollision = true;
 				}
 				errorNoSelection = false;
 			}
 			if (!errorCollision && getBooleanProperty(CREATE_CONNECTOR)) {
 				actualProjectName = jcaModel.getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 				projectName = WTPPlugin.isPlatformCaseSensitive() ? actualProjectName : actualProjectName.toLowerCase();
 				if (!list.contains(projectName)) {
 					list.add(projectName);
 				} else {
 					errorCollision = true;
 				}
 				errorNoSelection = false;
 			}
 			if (errorCollision) {
 				return J2EEPlugin.newErrorStatus(EARCreationResourceHandler.getString("DuplicateModuleNames", new Object[]{actualProjectName}), null); //$NON-NLS-1$
 			} else if (errorNoSelection) {
 				return J2EEPlugin.newErrorStatus(EARCreationResourceHandler.NoModulesSelected, null); 
 			}
 		}
 
 		return OK_STATUS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doValidateProperty(java.lang.String)
 	 */
 	public IStatus validate(String propertyName) {
 
 		if (getBooleanProperty(CREATE_APPCLIENT) && propertyName.equals(APPCLIENT_COMPONENT_NAME)) {
 			return clientModel.validateProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 		}
 
 		if (getBooleanProperty(CREATE_WEB) && propertyName.equals(WEB_COMPONENT_NAME)) {
 			return webModel.validateProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 		}
 
 		if (getBooleanProperty(CREATE_EJB) && propertyName.equals(EJB_COMPONENT_NAME)) {
 			return ejbModel.validateProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 		}
 
 		if (getBooleanProperty(CREATE_CONNECTOR) && propertyName.equals(CONNECTOR_COMPONENT_NAME)) {
 			return jcaModel.validateProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 		}
 		return super.validate(propertyName);
 	}
 
 //	private IStatus validateComponentName(String componentName) {
 //		IStatus status = OK_STATUS;
 //		if (status.isOK()) {
 //			if (componentName.indexOf("#") != -1) { //$NON-NLS-1$
 //				String errorMessage = WTPCommonPlugin.getResourceString(WTPCommonMessages.ERR_INVALID_CHARS); 
 //				return WTPCommonPlugin.createErrorStatus(errorMessage);
 //			} else if (componentName == null || componentName.equals("")) { //$NON-NLS-1$
 //				String errorMessage = WTPCommonPlugin.getResourceString(WTPCommonMessages.ERR_EMPTY_MODULE_NAME);
 //				return WTPCommonPlugin.createErrorStatus(errorMessage);
 //			}
 //		}
 //		return status;
 //	}
 
 	private void setDefaultNestedComponentName(String name, int flag) {
 		IDataModel modModule = getNestedModel(flag);
 		if (modModule != null) {
 			String compName = ensureUniqueProjectName(name);
 			modModule.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, compName);
 		}
 	}
 
 	private void setDefaultComponentNames(String base) {
 		String componentName;
 		if (base.endsWith(EJB_SUFFIX))
 			componentName = base;
 		else
 			componentName = base + EJB_SUFFIX;
 		setDefaultNestedComponentName(componentName, EJB);
 		setProperty(EJB_COMPONENT_NAME, componentName);
 		if (base.endsWith(WEB_SUFFIX))
 			componentName = base;
 		else
 			componentName = base + WEB_SUFFIX;
 		setDefaultNestedComponentName(componentName, WEB);
 		setProperty(WEB_COMPONENT_NAME, componentName);
 		if (base.endsWith(CLIENT_SUFFIX))
 			componentName = base;
 		else
 			componentName = base + CLIENT_SUFFIX;
 		setDefaultNestedComponentName(componentName, CLIENT);
 		setProperty(APPCLIENT_COMPONENT_NAME, componentName);
 		if (base.endsWith(CONNECTOR_SUFFIX))
 			componentName = base;
 		else
 			componentName = base + CONNECTOR_SUFFIX;
 		setDefaultNestedComponentName(componentName, RAR);
 		setProperty(CONNECTOR_COMPONENT_NAME, componentName);
 	}
 
 	private void setNestedJ2EEVersion(Object j2eeVersion) {
 		int j2eeVer = ((Integer) j2eeVersion).intValue();
 		if (ejbFacetModel != null) {
 			String facetVersionString = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToEJBVersionID(j2eeVer));
 			IProjectFacetVersion facetVersion = ProjectFacetsManager.getProjectFacet(ejbFacetModel.getStringProperty(IFacetDataModelProperties.FACET_ID)).getVersion(facetVersionString);
 			ejbFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, facetVersion);
 		}
 		if (webFacetModel != null) {
 			String facetVersionString = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToWebVersionID(j2eeVer));
 			IProjectFacetVersion facetVersion = ProjectFacetsManager.getProjectFacet(webFacetModel.getStringProperty(IFacetDataModelProperties.FACET_ID)).getVersion(facetVersionString);
 			webFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, facetVersion);
 		}
 		if (jcaFacetModel != null) {
 			String facetVersionString = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertJ2EEVersionIDToConnectorVersionID(j2eeVer));
 			IProjectFacetVersion facetVersion = ProjectFacetsManager.getProjectFacet(jcaFacetModel.getStringProperty(IFacetDataModelProperties.FACET_ID)).getVersion(facetVersionString);
 			jcaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, facetVersion);
 		}
 		if (clientFacetModel != null){
 			String facetVersionString = J2EEVersionUtil.convertVersionIntToString(j2eeVer);
 			IProjectFacetVersion facetVersion = ProjectFacetsManager.getProjectFacet(clientFacetModel.getStringProperty(IFacetDataModelProperties.FACET_ID)).getVersion(facetVersionString);
 			clientFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, facetVersion);
 		}
 	}
 
 	private IDataModel getNestedModel(int flag) {
 		switch (flag) {
 			case EJB :
 				return ejbModel;
 			case WEB :
 				return webModel;
 			case RAR :
 				return jcaModel;
 			case CLIENT :
 				return clientModel;
 		}
 		return null;
 	}
 
 	public boolean isPropertyEnabled(String propertyName) {
 		if (propertyName.equals(CREATE_CONNECTOR)) {
 			int version = getIntProperty(J2EE_VERSION);
 			return version > J2EEVersionConstants.J2EE_1_2_ID;
 		}
 		if( propertyName.equals(CONNECTOR_COMPONENT_NAME)){
 			return getBooleanProperty(CREATE_CONNECTOR);
 		}
 		if (propertyName.equals(APPCLIENT_COMPONENT_NAME))
 			return getBooleanProperty(CREATE_APPCLIENT);
 		if (propertyName.equals(EJB_COMPONENT_NAME))
 			return getBooleanProperty(CREATE_EJB);
 		if (propertyName.equals(WEB_COMPONENT_NAME))
 			return getBooleanProperty(CREATE_WEB);
 		return super.isPropertyEnabled(propertyName);
 	}
 }
