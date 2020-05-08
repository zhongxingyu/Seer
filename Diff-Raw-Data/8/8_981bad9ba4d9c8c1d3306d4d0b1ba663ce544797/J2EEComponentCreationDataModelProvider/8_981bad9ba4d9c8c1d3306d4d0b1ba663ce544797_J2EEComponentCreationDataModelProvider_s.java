 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.datamodel.properties.IEarComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.archive.operations.JavaComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.earcreation.EarComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.servertarget.ServerTargetHelper;
 import org.eclipse.jst.j2ee.project.datamodel.properties.IFlexibleJavaProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelPropertyDescriptor;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.FlexibleJavaProjectPreferenceUtil;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonMessages;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 
 public abstract class J2EEComponentCreationDataModelProvider extends JavaComponentCreationDataModelProvider implements IJ2EEComponentCreationDataModelProperties, IAnnotationsDataModel {
 
 	private IDataModel earCreationDM = null;
 	private static String MODULE_NOT_SUPPORTED = "MODULE_NOT_SUPPORTED";
 	private static String MODULEVERSION_NOT_SUPPORTED = "VERSION_NOT_SUPPORTED";
 	private static String OK = "OK";
 
 	public void init() {
 		super.init();
 		model.setProperty(COMPONENT_VERSION, getDefaultProperty(COMPONENT_VERSION));
 		
         IDataModel dm = DataModelFactory.createDataModel(createAddComponentToEAR());
 		model.setProperty(NESTED_ADD_COMPONENT_TO_EAR_DM, dm);
 		propertySet(CLASSPATH_SELECTION, null);
 		model.setProperty(NESTED_UPDATE_MANIFEST_DM, new UpdateManifestDataModel());
 		model.setProperty(USE_ANNOTATIONS, Boolean.FALSE);
 	}
 
 	public AddComponentToEnterpriseApplicationDataModelProvider createAddComponentToEAR() {
 		return new AddComponentToEnterpriseApplicationDataModelProvider();
 	}
 
 	public String[] getPropertyNames() {
 		String[] props = new String[]{EAR_COMPONENT_NAME, EAR_COMPONENT_DEPLOY_NAME, ADD_TO_EAR, UI_SHOW_EAR_SECTION, DD_FOLDER, COMPONENT_VERSION, VALID_COMPONENT_VERSIONS_FOR_PROJECT_RUNTIME, NESTED_ADD_COMPONENT_TO_EAR_DM, CLASSPATH_SELECTION, NESTED_EAR_COMPONENT_CREATION_DM, NESTED_UPDATE_MANIFEST_DM, EAR_COMPONENT_HANDLE, USE_ANNOTATIONS};
 		return combineProperties(super.getPropertyNames(), props);
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(ADD_TO_EAR)) {
 			return Boolean.FALSE;
 		} else if (propertyName.equals(UI_SHOW_EAR_SECTION)) {
 			return Boolean.TRUE;
 		} else if (propertyName.equals(EAR_COMPONENT_NAME)) {
 			return getDataModel().getStringProperty(COMPONENT_NAME) + "EAR";
 		} else if (propertyName.equals(COMPONENT_VERSION)) {
 			return getDefaultComponentVersion();
 		} else if (propertyName.equals(NESTED_EAR_COMPONENT_CREATION_DM))
 			return getDefaultEarCreationDM();
 		return super.getDefaultProperty(propertyName);
 	}
 
 	private Object getDefaultEarCreationDM() {
 		if (earCreationDM == null)
 			earCreationDM = DataModelFactory.createDataModel(new EarComponentCreationDataModelProvider());
 		return earCreationDM;
 	}
 
 	public boolean isPropertyEnabled(String propertyName) {
 		if (EAR_COMPONENT_NAME.equals(propertyName)) {
 			return getBooleanProperty(ADD_TO_EAR);
 		}
 		return super.isPropertyEnabled(propertyName);
 	}
 
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		boolean status = super.propertySet(propertyName, propertyValue);
 		if (propertyName.equals(EAR_COMPONENT_NAME)) {
 			model.setProperty(EAR_COMPONENT_DEPLOY_NAME, propertyValue);
 			ComponentHandle handle = computeEARHandle();
 			IDataModel earDM = (IDataModel) model.getProperty(NESTED_EAR_COMPONENT_CREATION_DM);
 			earDM.setProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_HANDLE, handle);
 			model.setProperty(EAR_COMPONENT_HANDLE, handle);
 
 		} else if (propertyName.equals(COMPONENT_NAME)) {
 			if (!model.isPropertySet(EAR_COMPONENT_NAME)) {
 				model.notifyPropertyChange(EAR_COMPONENT_NAME, IDataModel.VALID_VALUES_CHG);
 				model.setProperty(EAR_COMPONENT_DEPLOY_NAME, getProperty(EAR_COMPONENT_NAME));
 				IDataModel earDM = (IDataModel) model.getProperty(NESTED_EAR_COMPONENT_CREATION_DM);
 				ComponentHandle handle = computeEARHandle();
 				model.setProperty(EAR_COMPONENT_HANDLE, handle);
 				if (earDM != null)
 					earDM.setProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_HANDLE, handle);
 			}
		} 
		else if (propertyName.equals(ADD_TO_EAR)) {
 			model.notifyPropertyChange(NESTED_EAR_COMPONENT_CREATION_DM, IDataModel.DEFAULT_CHG);
		}else if (propertyName.equals(COMPONENT_VERSION)) {
             if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
                 setProperty(USE_ANNOTATIONS, Boolean.FALSE);
             model.notifyPropertyChange(USE_ANNOTATIONS, DataModelEvent.ENABLE_CHG);
 			//this will force to  reload all the server types which are valid for this component version
 			if(!FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp()){
 				model.notifyPropertyChange(SERVER_TARGET_ID, DataModelEvent.VALID_VALUES_CHG);
 	        }			
         }
 		return status;
 	}
 
 	private ComponentHandle computeEARHandle(){
 		String earCompName = (String) model.getProperty(EAR_COMPONENT_NAME);
 		String earProjname = (String) model.getProperty(EAR_COMPONENT_NAME);
 		
 		IDataModel earDM = (IDataModel) model.getProperty(NESTED_EAR_COMPONENT_CREATION_DM);	
 		earDM.setProperty(IEarComponentCreationDataModelProperties.PROJECT_NAME, earProjname);
 		
 		IStatus status = model.validateProperty(IEarComponentCreationDataModelProperties.PROJECT_NAME);
 		if( status.isOK()){
 			ComponentHandle handle = ComponentHandle.create(ProjectUtilities.getProject(earProjname), earCompName);
 			return handle;
 		}
 		return null;
 	}
 
 	/**
 	 * @param workbenchComp
 	 */
 //	protected void setEARComponentIfJ2EEModuleCreationOnly(WorkbenchComponent workbenchComp, Object propertyValue) {
 //
 //		
 //		getAddComponentToEARDataModel().setProperty(IAddComponentToEnterpriseApplicationDataModelProperties.ARCHIVE_MODULE, workbenchComp);
 //		getAddComponentToEARDataModel().setProperty(IAddComponentToEnterpriseApplicationDataModelProperties.PROJECT_NAME, model.getStringProperty(PROJECT_NAME));
 //		getAddComponentToEARDataModel().setProperty(IAddComponentToEnterpriseApplicationDataModelProperties.EAR_COMPONENT_NAME, model.getStringProperty(EAR_COMPONENT_NAME));
 //		if (!model.isPropertySet(EAR_COMPONENT_NAME)) {
 //			String earModuleName = model.getStringProperty(EAR_COMPONENT_NAME);
 //			model.notifyPropertyChange(EAR_COMPONENT_NAME, IDataModel.VALID_VALUES_CHG);
 //
 //		}
 //		((UpdateManifestDataModel) model.getProperty(NESTED_UPDATE_MANIFEST_DM)).setProperty(UpdateManifestDataModel.PROJECT_NAME, propertyValue);
 //		
 //	}
 
 	public DataModelPropertyDescriptor[] getValidPropertyDescriptors(String propertyName) {
 		if (propertyName.equals(COMPONENT_VERSION)) {
 			return getValidComponentVersionDescriptors();
 		}
 		if (propertyName.equals(EAR_COMPONENT_NAME)) {
 			int j2eeVersion = getJ2EEVersion();
 			return getEARPropertyDescriptor(j2eeVersion);
 		}else if(propertyName.equals(SERVER_TARGET_ID)){
 			return validJ2EEServerPropertyDescriptors();
 		}
 		return super.getValidPropertyDescriptors(propertyName);
 	}
 
 	protected String isvalidJComponentVersionsSupportedByServer(){
 		String serverID = model.getStringProperty(SERVER_TARGET_ID);
 		IRuntime runtime = getServerTargetByID(serverID);
 	
 		if( serverID.equals("") || runtime == null ){
 			return MODULEVERSION_NOT_SUPPORTED;
 		}
 		Integer version = (Integer)model.getProperty(COMPONENT_VERSION);
 		int nj2eeVer = convertModuleVersionToJ2EEVersion(version.intValue());
 		String j2eeVer = J2EEVersionUtil.getJ2EETextVersion(nj2eeVer);
 		return isTypeSupported(runtime.getRuntimeType(), getComponentID(), j2eeVer);		
 	}
 	                                       
 	protected DataModelPropertyDescriptor[] validJ2EEServerPropertyDescriptors(){
 		
 		Integer  version = (Integer)model.getProperty(COMPONENT_VERSION);
 		int j2eeversion = convertModuleVersionToJ2EEVersion(version.intValue());
 		String j2eeVersionText = J2EEVersionUtil.getJ2EETextVersion(j2eeversion);
 		
 		ArrayList validServers = new ArrayList();
 		
 		IDataModel projectdm = (IDataModel)model.getNestedModel(NESTED_PROJECT_CREATION_DM);
 		DataModelPropertyDescriptor[] desc =  projectdm.getValidPropertyDescriptors(IFlexibleJavaProjectCreationDataModelProperties.SERVER_TARGET_ID);
 		for (int i = 0; i < desc.length; i++) {
 			DataModelPropertyDescriptor descriptor  = desc[i];
 			String name = descriptor.getPropertyDescription();
 			String runtimeid = (String)descriptor.getPropertyValue();
 			IRuntime runtime = getServerTargetByID( runtimeid );
 			String ok = isTypeSupported( runtime.getRuntimeType(), getComponentID(), j2eeVersionText );
 			if( ok.equals("OK") )
 				validServers.add(descriptor);
 		}
 
 		if (!validServers.isEmpty()) {
 			int serverTargetListSize = validServers.size();
 			DataModelPropertyDescriptor[] result = new DataModelPropertyDescriptor[serverTargetListSize];
 			for (int i = 0; i < validServers.size(); i++) {
 				result[i] = (DataModelPropertyDescriptor) validServers.get(i);
 			}
 			return result;
 		} 
 		return new DataModelPropertyDescriptor[0];
 	}
 	
 	private IRuntime getServerTargetByID(String id) {
 		List targets = getValidServerTargets();
 		IRuntime target;
 		for (int i = 0; i < targets.size(); i++) {
 			target = (IRuntime) targets.get(i);
 			if (id.equals(target.getId()))
 				return target;
 		}
 		return null;
 	}	
 	
     private List getValidServerTargets() {
         List validServerTargets = null;
         validServerTargets = ServerTargetHelper.getServerTargets("", "");  //$NON-NLS-1$  //$NON-NLS-2$
         if (validServerTargets != null && validServerTargets.isEmpty())
             validServerTargets = null;
         if (validServerTargets == null)
             return Collections.EMPTY_LIST;
         return validServerTargets;
     }	
 	
 	
 	protected String isTypeSupported(IRuntimeType type, String moduleID, String j2eeVersion) {
 		IModuleType[] moduleTypes = type.getModuleTypes();
 
 		boolean moduleFound = false;
 		boolean moduleVersionFound = false;
 		
 		if (moduleTypes != null) {
 			int size = moduleTypes.length;
 			
 			for (int i = 0; i < size; i++) {
 				IModuleType moduleType = moduleTypes[i];
 				
 				if (matches(moduleType.getId(), moduleID)) {
 					moduleFound = true;
 					String version = moduleType.getVersion();
 					if( version.equals(j2eeVersion) || version.equals("*") ){
 						moduleVersionFound = true;
 						return OK;
 					}else{
 						if( i < size )
 							continue;
 					}	
 				}
 			}
 		}	
 		if( !moduleFound ){
 			return MODULE_NOT_SUPPORTED;
 		}else{
 			if(!moduleVersionFound)
 				return MODULEVERSION_NOT_SUPPORTED;
 		}	
 		
 		return "";
 	}	
 
 	
 	protected static String[] getServerVersions(IRuntimeType type, String moduleID ) {
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
 
 	protected static boolean matches(String serverTypeID, String j2eeModuleID) {
 
 		if (serverTypeID.equals("j2ee")) {
 			if (j2eeModuleID.equals(IModuleConstants.JST_WEB_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_EJB_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_EAR_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_APPCLIENT_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_CONNECTOR_MODULE)) {
 				return true;
 			}
 		}else if (serverTypeID.equals("j2ee.*")) {
 			if (j2eeModuleID.equals(IModuleConstants.JST_WEB_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_EJB_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_EAR_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_APPCLIENT_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_CONNECTOR_MODULE)) {
 				return true;
 			}
 		} else if (serverTypeID.equals("j2ee.web")) {//$NON-NLS-1$
 			if (j2eeModuleID.equals(IModuleConstants.JST_WEB_MODULE)) {
 				return true;
 			}
 		} else if (serverTypeID.equals("j2ee.ejb")) {//$NON-NLS-1$
 			if (j2eeModuleID.equals(IModuleConstants.JST_EJB_MODULE)) {
 				return true;
 			}
 		} else if (serverTypeID.equals("j2ee.ear")) {//$NON-NLS-1$
 			if (j2eeModuleID.equals(IModuleConstants.JST_EAR_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_APPCLIENT_MODULE) || j2eeModuleID.equals(IModuleConstants.JST_CONNECTOR_MODULE)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	
 	private DataModelPropertyDescriptor[] getEARPropertyDescriptor(int j2eeVersion) {
 		StructureEdit mc = null;
 		ArrayList earDescriptorList = new ArrayList();
 
 		IProject[] projs = ProjectUtilities.getAllProjects();
 
 		for (int index = 0; index < projs.length; index++) {
 			IProject flexProject = projs[index];
 			try {
 				if (flexProject != null) {
 					IFlexibleProject  fProject = ComponentCore.createFlexibleProject(flexProject);
 					if ( fProject.isFlexible()){
 						IVirtualComponent[] comps = fProject.getComponents();
 						int earVersion = 0;
 						for( int i=0; i< comps.length; i++ ){
 							if( comps[i].getComponentTypeId().equals(IModuleConstants.JST_EAR_MODULE)){
 								String sVer = comps[i].getVersion();
 								int ver = J2EEVersionUtil.convertVersionStringToInt(sVer);
 								if (j2eeVersion <= ver) {
 									DataModelPropertyDescriptor desc = new DataModelPropertyDescriptor(comps[i].getComponentHandle(), comps[i].getName());
 									earDescriptorList.add(desc);
 								}							
 							}
 						}
 					}
 				}
 			} finally {
 				if (mc != null)
 					mc.dispose();
 			}
 		}
 		DataModelPropertyDescriptor[] descriptors = new DataModelPropertyDescriptor[earDescriptorList.size()];
 		for (int i = 0; i < descriptors.length; i++) {
 			DataModelPropertyDescriptor desc = (DataModelPropertyDescriptor)earDescriptorList.get(i);
 			descriptors[i] = new DataModelPropertyDescriptor(desc.getPropertyDescription(), desc.getPropertyDescription());
 		}
 		return descriptors;
 	}
 	
 
 
 	public IProject getProject() {
 		String projName = getDataModel().getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME);
 		if (projName != null && projName.length() > 0)
 			return ProjectUtilities.getProject(projName);
 		return null;
 	}
 
 	public WorkbenchComponent getTargetWorkbenchComponent() {
 		StructureEdit core = null;
 		try {
 			IProject flexProject = getProject();
 			if (flexProject != null) {
 				core = StructureEdit.getStructureEditForRead(getProject());
 				if (core != null) {
 					String componentName = getProperty(COMPONENT_NAME) != null ? getProperty(COMPONENT_NAME).toString() : null;
 					if (componentName != null)
 						return core.findComponentByName(componentName);
 				}
 			}
 		} finally {
 			if (core != null)
 				core.dispose();
 		}
 		return null;
 	}
 
 	public IStatus validate(String propertyName) {
 		if (EAR_COMPONENT_NAME.equals(propertyName) && getBooleanProperty(ADD_TO_EAR)) {
 			return validateEARModuleNameProperty();
 		} else if (COMPONENT_VERSION.equals(propertyName)) {
 			return validateComponentVersionProperty();
 		} else if (propertyName.equals(VALID_COMPONENT_VERSIONS_FOR_PROJECT_RUNTIME)) {
 			return OK_STATUS;
 		}
 		return super.validate(propertyName);
 	}
 
 	private IStatus validateComponentVersionProperty() {
 		int componentVersion = model.getIntProperty(COMPONENT_VERSION);
 		String result = isvalidJComponentVersionsSupportedByServer();
 		if( result.equals(MODULEVERSION_NOT_SUPPORTED)){
 			return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SPEC_LEVEL_NOT_FOUND));
 		}else if ( result.equals(MODULE_NOT_SUPPORTED )){
 			return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.MODULE_NOT_SUPPORTED));
 		}
 		if (componentVersion == -1)
 			return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SPEC_LEVEL_NOT_FOUND));
 		return OK_STATUS;
 	}
 
 	private IStatus validateEARModuleNameProperty() {
 		IStatus status = OK_STATUS;
 		String earName = getStringProperty(EAR_COMPONENT_NAME);
 		if (status.isOK()) {
 			if (earName.indexOf("#") != -1 || earName.indexOf("/") != -1) { //$NON-NLS-1$
 				String errorMessage = WTPCommonPlugin.getResourceString(WTPCommonMessages.ERR_INVALID_CHARS); //$NON-NLS-1$
 				return WTPCommonPlugin.createErrorStatus(errorMessage);
 			} else if (earName == null || earName.equals("")) { //$NON-NLS-1$
 				String errorMessage = WTPCommonPlugin.getResourceString(WTPCommonMessages.ERR_EMPTY_MODULE_NAME);
 				return WTPCommonPlugin.createErrorStatus(errorMessage);
 			}
 		} else
 			return status;
 		// IProject earProject =
 		// applicationCreationDataModel.getTargetProject();
 		// if (null != earProject && earProject.exists()) {
 		// if (earProject.isOpen()) {
 		// try {
 		// EARNatureRuntime earNature = (EARNatureRuntime)
 		// earProject.getNature(IEARNatureConstants.NATURE_ID);
 		// if (earNature == null) {
 		// return
 		// WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.PROJECT_NOT_EAR,
 		// new Object[]{earProject.getName()}));
 		// } else if (earNature.getJ2EEVersion() < getJ2EEVersion()) {
 		// String earVersion =
 		// EnterpriseApplicationCreationDataModel.getVersionString(earNature.getJ2EEVersion());
 		// return
 		// WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.INCOMPATABLE_J2EE_VERSIONS,
 		// new Object[]{earProject.getName(), earVersion}));
 		// }
 		// return OK_STATUS;
 		// } catch (CoreException e) {
 		// return new Status(IStatus.ERROR, J2EEPlugin.PLUGIN_ID, -1, null, e);
 		// }
 		// }
 		// return
 		// WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.PROJECT_ClOSED,
 		// new Object[]{earProject.getName()}));
 		// } else if (null != earProject && null != getTargetProject()) {
 		// if (earProject.getName().equals(getTargetProject().getName())) {
 		// return
 		// WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SAME_MODULE_AND_EAR_NAME,
 		// new Object[]{earProject.getName()}));
 		// } else if (!CoreFileSystemLibrary.isCaseSensitive()) {
 		// if
 		// (earProject.getName().toLowerCase().equals(getTargetProject().getName().toLowerCase()))
 		// {
 		// return
 		// WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SAME_MODULE_AND_EAR_NAME,
 		// new Object[]{earProject.getName()}));
 		// }
 		// }
 		// }
 		// IStatus status =
 		// applicationCreationDataModel.validateProperty(EnterpriseApplicationCreationDataModel.PROJECT_NAME);
 		// if (status.isOK()) {
 		// status =
 		// applicationCreationDataModel.validateProperty(EnterpriseApplicationCreationDataModel.PROJECT_LOCATION);
 		// }
 		// return status;
 
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	public final ClassPathSelection getClassPathSelection() {
 		// boolean createNew = false;
 		// if (null == cachedSelection ||
 		// !getApplicationCreationDataModel().getTargetProject().getName().equals(cachedSelection.getEARFile().getURI()))
 		// {
 		// createNew = true;
 		// }
 		// // close an existing cachedSelection
 		// if (createNew && cachedSelection != null) {
 		// EARFile earFile = cachedSelection.getEARFile();
 		// if (earFile != null)
 		// earFile.close();
 		// }
 		//
 		// if (createNew && getTargetProject() != null) {
 		// cachedSelection =
 		// ClasspathSelectionHelper.createClasspathSelection(getTargetProject(),
 		// getModuleExtension(),
 		// getApplicationCreationDataModel().getTargetProject(),
 		// getModuleType());
 		// }
 		// return cachedSelection;
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 	}
 
 	// public void propertyChanged(WTPOperationDataModelEvent event) {
 	// super.propertyChanged(event);
 	// if (event.getDataModel() == addComponentToEARDataModel && event.getFlag()
 	// == WTPOperationDataModelEvent.PROPERTY_CHG &&
 	// event.getPropertyName().equals(ArtifactEditOperationDataModel.PROJECT_NAME))
 	// {
 	// earComponentCreationDataModel.setProperty(EARComponentCreationDataModel.COMPONENT_NAME,
 	// event.getProperty());
 	// }
 	// }
 	protected final IDataModel getAddComponentToEARDataModel() {
 		//return (AddComponentToEnterpriseApplicationDataModel) model.getProperty(NESTED_ADD_COMPONENT_TO_EAR_DM);
 		return (IDataModel) model.getProperty(NESTED_ADD_COMPONENT_TO_EAR_DM);
 	}
 
 	protected final UpdateManifestDataModel getUpdateManifestDataModel() {
 		return (UpdateManifestDataModel) model.getProperty(NESTED_UPDATE_MANIFEST_DM);
 	}
 
 	public String getModuleName() {
 		return getDataModel().getStringProperty(COMPONENT_NAME);
 	}
 
 	public final int getJ2EEVersion() {
 		return convertModuleVersionToJ2EEVersion(getIntProperty(COMPONENT_VERSION));
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
 
 	protected abstract int convertModuleVersionToJ2EEVersion(int moduleVersion);
 
 	protected abstract DataModelPropertyDescriptor[] getValidComponentVersionDescriptors();
 
 }
