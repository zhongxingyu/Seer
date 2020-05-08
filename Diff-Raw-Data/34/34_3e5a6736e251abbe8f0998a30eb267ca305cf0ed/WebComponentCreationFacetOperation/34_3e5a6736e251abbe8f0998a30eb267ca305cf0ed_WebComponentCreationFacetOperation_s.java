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
 package org.eclipse.jst.j2ee.internal.web.archive.operations;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.J2EEComponentCreationFacetOperation;
 import org.eclipse.jst.j2ee.web.datamodel.properties.IWebComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.web.project.facet.WebFacetInstallDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class WebComponentCreationFacetOperation extends J2EEComponentCreationFacetOperation {
 
 	public WebComponentCreationFacetOperation(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 		String projectName = model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME);
 
 		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projectName);
 
 		FacetDataModelMap map = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 		map.add(setupJavaInstallAction());
 		IDataModel newModel = setupWebInstallAction();
 		map.add(newModel);
 		setRuntime(newModel, dm); // Setting runtime property

 		IStatus stat = dm.getDefaultOperation().execute(monitor, info);
 		if (stat.isOK()) {
 			String earProjectName = (String) model.getProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_NAME);
 			IProject earProject = ProjectUtilities.getProject(earProjectName);
 			if (earProject != null && earProject.exists())
 				stat = addtoEar(projectName, earProjectName);
 		}
 
 		return stat;
 	}
 
 	protected IDataModel setupWebInstallAction() {
 		String versionStr = model.getPropertyDescriptor(IJ2EEComponentCreationDataModelProperties.COMPONENT_VERSION).getPropertyDescription();
 		IDataModel webFacetInstallDataModel = DataModelFactory.createDataModel(new WebFacetInstallDataModelProvider());
 		webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME));
 		webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, versionStr);
 		webFacetInstallDataModel.setProperty(IWebFacetInstallDataModelProperties.CONFIG_FOLDER, model.getStringProperty(IWebComponentCreationDataModelProperties.WEBCONTENT_FOLDER));
 		webFacetInstallDataModel.setProperty(IWebFacetInstallDataModelProperties.CONTEXT_ROOT, model.getStringProperty(IWebComponentCreationDataModelProperties.CONTEXT_ROOT));
 		webFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,model.getBooleanProperty(IJ2EEComponentCreationDataModelProperties.ADD_TO_EAR));
 		if (model.getBooleanProperty(IJ2EEComponentCreationDataModelProperties.ADD_TO_EAR)) {
 			webFacetInstallDataModel.setProperty(IWebFacetInstallDataModelProperties.EAR_PROJECT_NAME, model.getProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_NAME));
 		}
 		webFacetInstallDataModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.MODULE_URI, model.getProperty(IJ2EEComponentCreationDataModelProperties.MODULE_URI));
 		webFacetInstallDataModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.RUNTIME_TARGET_ID, model.getProperty(IJ2EEComponentCreationDataModelProperties.RUNTIME_TARGET_ID));
 		return webFacetInstallDataModel;
 	}
 }
