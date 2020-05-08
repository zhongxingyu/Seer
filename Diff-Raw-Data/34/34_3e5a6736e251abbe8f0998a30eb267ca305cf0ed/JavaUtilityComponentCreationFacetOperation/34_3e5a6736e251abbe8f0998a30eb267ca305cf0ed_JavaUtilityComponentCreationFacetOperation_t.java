 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.project.facet;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 
 public class JavaUtilityComponentCreationFacetOperation extends J2EEComponentCreationFacetOperation {
 
 	public JavaUtilityComponentCreationFacetOperation(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 
 		IStatus result = OK_STATUS;
 		try {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, (String) model.getProperty(JavaUtilityComponentCreationDataModelProvider.PROJECT_NAME));
 
 			FacetDataModelMap map = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			map.add(setupJavaInstallAction());
 			
 			IDataModel newModel = setupUtilityInstallAction(model);
 			map.add( newModel );
 
 			setRuntime(newModel, dm); // Setting runtime property
			setAddToEARFromWizard(newModel);
 			result = dm.getDefaultOperation().execute(monitor, null);
 		} catch (ExecutionException e) {
 			Logger.getLogger().logError(e);
 		}
 		return result;
 	}
 
 	protected IDataModel setupUtilityInstallAction(IDataModel aDM) {
 		IDataModel dm = DataModelFactory.createDataModel(new UtilityFacetInstallDataModelProvider());
 		try {
 			dm.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, model.getStringProperty(JavaUtilityComponentCreationDataModelProvider.PROJECT_NAME));
 			dm.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, "1.0"); //$NON-NLS-1$
 			dm.setProperty(IUtilityFacetInstallDataModelProperties.RUNTIME_TARGET_ID, model.getProperty(JavaUtilityComponentCreationDataModelProvider.RUNTIME_TARGET_ID));
 			dm.setProperty(IUtilityFacetInstallDataModelProperties.CONFIG_FOLDER, 
 					model.getProperty(JavaUtilityComponentCreationDataModelProvider.JAVASOURCE_FOLDER));
 			dm.setStringProperty(IUtilityFacetInstallDataModelProperties.EAR_PROJECT_NAME,model.getStringProperty(JavaUtilityComponentCreationDataModelProvider.EAR_PROJECT_NAME));
 			
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		}
 
 		return dm;
 	}
 
 }
