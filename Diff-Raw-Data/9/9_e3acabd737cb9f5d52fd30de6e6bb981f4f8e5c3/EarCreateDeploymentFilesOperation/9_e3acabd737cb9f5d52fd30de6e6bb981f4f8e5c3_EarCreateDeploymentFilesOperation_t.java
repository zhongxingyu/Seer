 package org.eclipse.jst.jee.project.facet;
 
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.jee.JEEPlugin;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class EarCreateDeploymentFilesOperation extends
 		CreateDeploymentFilesDataModelOperation {
 
 
 	public EarCreateDeploymentFilesOperation(IDataModel model) {
 		super(model);
 	}
 	
 	@Override
 	protected void createDeploymentFiles(IProject project, final IProgressMonitor monitor) {
 		final IVirtualComponent component = ComponentCore.createComponent(project);
 		final IModelProvider provider = ModelProviderManager.getModelProvider(project);
 			provider.modify(new Runnable(){
 				public void run() {
 					IVirtualReference[] componentReferences = J2EEProjectUtilities.getJ2EEModuleReferences(component);
 					if(componentReferences != null && componentReferences.length > 0){					
 						final IDataModel dataModel = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider());
 						dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, component);
 						List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
						Map<IVirtualComponent, String> uriMap = new HashMap<IVirtualComponent, String>();
 						for(int i = 0; i < componentReferences.length; i++) {
 							IVirtualComponent referencedComponent = componentReferences[i].getReferencedComponent();
 							modList.add(referencedComponent);
							uriMap.put(referencedComponent, componentReferences[i].getArchiveName());
 						}
						dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, uriMap);
 						dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
 						try {
 							dataModel.getDefaultOperation().execute(monitor, null);
 						} catch (ExecutionException e) {
 							JEEPlugin.logError(e);
 						}
 					}
 				}
 			}, IModelProvider.FORCESAVE);
 				
 		
 	}
 
 }
