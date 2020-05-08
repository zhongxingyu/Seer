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
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
 import org.eclipse.jst.common.project.facet.JavaFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.IAddComponentToEnterpriseApplicationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJavaComponentCreationDataModelProperties;
 import org.eclipse.jst.server.core.FacetUtil;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerUtil;
 
 public class J2EEComponentCreationFacetOperation extends AbstractDataModelOperation {
 
 	
 	public J2EEComponentCreationFacetOperation(IDataModel model) {
 		super(model);
 	}
 	
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	protected void setRuntime(IDataModel newModel, IDataModel facetModel) {
 		String runtime = newModel.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.RUNTIME_TARGET_ID);
 		try {
 			if (runtime != null) {
 				IRuntime run = getRuntimeByID(runtime);
 				org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = null;
 				try {
 					if (run != null)
 						facetRuntime = FacetUtil.getRuntime(run);
 				}
 				catch (IllegalArgumentException ex)
 				{}
 				if (facetRuntime != null) {
 					facetModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME,facetRuntime);
 				}
 			}
 			} catch (IllegalArgumentException e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	protected IRuntime getRuntimeByID(String id) {
 		IRuntime[] targets = ServerUtil.getRuntimes("", "");
 		for (int i = 0; i < targets.length; i++) {
 			IRuntime target = targets[i];
 			if (id.equals(target.getId()))
 				return target;
 		}
 		return null;
 	}
 	
 	protected IDataModel setupJavaInstallAction() {
 		IDataModel dm = DataModelFactory.createDataModel(new JavaFacetInstallDataModelProvider());
 		dm.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME));
 		//dm.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, "1.4"); //$NON-NLS-1$
 		dm.setProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, model.getStringProperty(IJavaComponentCreationDataModelProperties.JAVASOURCE_FOLDER));
 		return dm;
 	}
 	
 	protected IStatus addtoEar(String projectName, String earProjectName){
 		
 		IStatus stat = OK_STATUS;
 		IProject moduleProject = ProjectUtilities.getProject( projectName );
 		IProject earProject = ProjectUtilities.getProject( earProjectName );
 
 		IVirtualComponent comp = ComponentCore.createComponent( moduleProject );
 		IVirtualComponent earComp = ComponentCore.createComponent( earProject );
 		if( comp != null && comp.exists() && earComp != null && earComp.exists()){
 			
 			IDataModel dataModel = DataModelFactory.createDataModel( new AddComponentToEnterpriseApplicationDataModelProvider());
 			dataModel.setProperty( ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComp );
 	        List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 	        modList.add(comp);
 	        dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
 	        Map map = new HashMap();
 			map.put(comp, model.getStringProperty(IJ2EEComponentCreationDataModelProperties.MODULE_URI));
 	        dataModel.setProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, map);
 	        try {
 				stat = dataModel.getDefaultOperation().execute(null, null);
 			} catch (ExecutionException e) {
 				Logger.getLogger().logError(e);
 			}
 		}	
 		return stat;
	}		
 }
