 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.JavaComponentCreationDataModel;
 import org.eclipse.jst.j2ee.internal.earcreation.EARComponentCreationDataModel;
 import org.eclipse.jst.j2ee.internal.modulecore.util.EARArtifactEdit;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModelEvent;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPPropertyDescriptor;
 import org.eclipse.wst.common.modulecore.ModuleCore;
 import org.eclipse.wst.common.modulecore.WorkbenchComponent;
 import org.eclipse.wst.common.modulecore.internal.operation.ArtifactEditOperationDataModel;
 import org.eclipse.wst.common.modulecore.internal.util.IModuleConstants;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * This dataModel is a common super class used for to create Flexibile J2EE Components.
  * 
  * This class (and all its fields and methods) is likely to change during the WTP 1.0 milestones as
  * the new project structures are adopted. Use at your own risk.
  * 
  * @since WTP 1.0
  */
 public abstract class J2EEComponentCreationDataModel extends JavaComponentCreationDataModel implements IAnnotationsDataModel {
 
 	/**
 	 * type Boolean, default false
 	 */
 	public static final String ADD_TO_EAR = "J2EEComponentCreationDataModel.ADD_TO_EAR"; //$NON-NLS-1$
 
 	/**
 	 * type String
 	 */
 	public static final String EAR_MODULE_NAME = "J2EEComponentCreationDataModel.EAR_MODULE_NAME"; //$NON-NLS-1$
 	
 	public static final String EAR_MODULE_DEPLOY_NAME = "J2EEComponentCreationDataModel.EAR_MODULE_DEPLOY_NAME"; //$NON-NLS-1$
 	
 	/**
 	 * type boolean
 	 */	
 	private static final String NESTED_MODEL_ADD_TO_EAR = "J2EEComponentCreationDataModel.NESTED_MODEL_ADD_TO_EAR"; //$NON-NLS-1$
 	
 	private static final String NESTED_MODEL_EAR_CREATION = "J2EEComponentCreationDataModel.NESTED_MODEL_EAR_CREATION"; //$NON-NLS-1$
 	
 	/**
 	 * type boolean
 	 */		
 	private static final String NESTED_MODEL_JAR_DEPENDENCY = "J2EEComponentCreationDataModel.NESTED_MODEL_JAR_DEPENDENCY"; //$NON-NLS-1$
 	
 	/**
 	 * type Boolean; default true, UI only
 	 */
 	public static final String UI_SHOW_EAR_SECTION = "J2EEComponentCreationDataModel.UI_SHOW_EAR_SECTION"; //$NON-NLS-1$
 	
 	/**
 	 * type String
 	 */
 	public static final String DD_FOLDER = "J2EEComponentCreationDataModel.DD_FOLDER"; //$NON-NLS-1$
 	
 	
 	/**
 	 * This corresponds to the J2EE versions of 1.2, 1.3, 1.4, etc. Each subclass will convert this
 	 * version to its corresponding highest module version supported by the J2EE version and set the
 	 * J2EE_MODULE_VERSION property.
 	 * 
 	 * type Integer
 	 */
 	public static final String J2EE_VERSION = "J2EEComponentCreationDataModel.J2EE_VERSION"; //$NON-NLS-1$
     
 	public AddComponentToEnterpriseApplicationDataModel addComponentToEARDataModel;
 	
 	protected EARComponentCreationDataModel earComponentCreationDataModel;
 	private UpdateManifestDataModel jarDependencyDataModel;
 
 	private ClassPathSelection cachedSelection;
 
 	protected void init() {
 		super.init();
 	}
 
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(EAR_MODULE_NAME);
 		addValidBaseProperty(EAR_MODULE_DEPLOY_NAME);
 		addValidBaseProperty(ADD_TO_EAR);
 		addValidBaseProperty(USE_ANNOTATIONS);
 		addValidBaseProperty(UI_SHOW_EAR_SECTION);
 		addValidBaseProperty(DD_FOLDER);
 		addValidBaseProperty(JAVASOURCE_FOLDER);
 		addValidBaseProperty(J2EE_VERSION);
 		addValidBaseProperty(MANIFEST_FOLDER);
 		addValidBaseProperty(NESTED_MODEL_VALIDATION_HOOK);
 	}
 
 	protected void initNestedModels() {
 		super.initNestedModels();
 		earComponentCreationDataModel = new EARComponentCreationDataModel();
 		if(earComponentCreationDataModel != null)
 	        addNestedModel(NESTED_MODEL_EAR_CREATION, earComponentCreationDataModel);
 		addComponentToEARDataModel = createModuleNestedModel();
 		if (addComponentToEARDataModel != null)
 			addNestedModel(NESTED_MODEL_ADD_TO_EAR, addComponentToEARDataModel);
 		jarDependencyDataModel = new UpdateManifestDataModel();
 		addNestedModel(NESTED_MODEL_JAR_DEPENDENCY, jarDependencyDataModel);
 	}
 
 	protected AddComponentToEnterpriseApplicationDataModel createModuleNestedModel() {
 		return new AddComponentToEnterpriseApplicationDataModel();
 	}
 
 	protected Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(ADD_TO_EAR)) {
 			return Boolean.FALSE;
 		} else if (propertyName.equals(USE_ANNOTATIONS)) {
 			return Boolean.FALSE;
 		} else if (propertyName.equals(UI_SHOW_EAR_SECTION)) {
 			return Boolean.TRUE;
 		} else if (propertyName.equals(EAR_MODULE_NAME)) {
 			return getStringProperty(COMPONENT_NAME)+"EAR";
 		} else {
 			return super.getDefaultProperty(propertyName);
 		}
 	}
 
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		boolean returnValue = super.doSetProperty(propertyName, propertyValue);
 
 		if (propertyName.equals(EAR_MODULE_NAME)) {
 			setEARDeployNameProperty(propertyValue);
 		} else if(propertyName.equals(COMPONENT_NAME)){
 			if (!isSet(EAR_MODULE_NAME)) 
 				notifyDefaultChange(EAR_MODULE_NAME);
 			    setEARDeployNameProperty(getStringProperty(EAR_MODULE_NAME));
 		} else if (propertyName.equals(PROJECT_NAME)) {
 			WorkbenchComponent workbenchComp = getTargetWorkbenchComponent();
 			setEARComponentIfJ2EEModuleCreationOnly(workbenchComp,propertyValue);
 		} else if (propertyName.equals(ADD_TO_EAR)) {
 			notifyEnablementChange(ADD_TO_EAR);
 		} else if (propertyName.equals(J2EE_VERSION)) {
 			Integer modVersion = convertJ2EEVersionToModuleVersion((Integer) propertyValue);
 			setProperty(COMPONENT_VERSION, modVersion);
 			return false;
 		}
 		return returnValue;
 	}
 
 	private void setEARDeployNameProperty(Object propertyValue) {
 	   setProperty(EAR_MODULE_DEPLOY_NAME,propertyValue + ArchiveConstants.EAR_FILE_EXT);
 	}
 
 	/**
 	 * @param workbenchComp
 	 */
 	protected void setEARComponentIfJ2EEModuleCreationOnly(WorkbenchComponent workbenchComp, Object propertyValue) {
 		getAddModuleToApplicationDataModel().setProperty(AddComponentToEnterpriseApplicationDataModel.ARCHIVE_MODULE, workbenchComp);
 		getAddModuleToApplicationDataModel().setProperty(AddComponentToEnterpriseApplicationDataModel.PROJECT_NAME,getStringProperty(PROJECT_NAME));
 		getAddModuleToApplicationDataModel().setProperty(AddComponentToEnterpriseApplicationDataModel.EAR_MODULE_NAME,getStringProperty(EAR_MODULE_NAME));
 		if (!isSet(EAR_MODULE_NAME)) {
 			String earModuleName = getStringProperty(EAR_MODULE_NAME);
 			notifyListeners(EAR_MODULE_NAME);
 
 		}
 		jarDependencyDataModel.setProperty(UpdateManifestDataModel.PROJECT_NAME, propertyValue);
 	}
 
 	protected Boolean basicIsEnabled(String propertyName) {
 		Boolean enabled = super.basicIsEnabled(propertyName);
 		if (enabled.booleanValue()) {
 			if (propertyName.equals(EAR_MODULE_NAME)) {
 				enabled = (Boolean) getProperty(ADD_TO_EAR);
 			} 
 		}
 		return enabled;
 	}
 
 	protected final AddComponentToEnterpriseApplicationDataModel getAddModuleToApplicationDataModel() {
 		return addComponentToEARDataModel;
 	}
 
 	protected WTPPropertyDescriptor[] doGetValidPropertyDescriptors(String propertyName) {
 		if (propertyName.equals(COMPONENT_VERSION)) {
 			return getValidComponentVersionDescriptors();
 		}
 		if (propertyName.equals(EAR_MODULE_NAME)) {
 			int j2eeVersion = getJ2EEVersion();
 		 ModuleCore mc = null;
 		 try {
 			IProject flexProject = getProject();
 			if(flexProject != null) { 
 			mc = ModuleCore.getModuleCoreForRead(getProject());
 			WorkbenchComponent[] components = mc.getWorkbenchModules();
 			ArrayList earModuleList = new ArrayList();
 			int earVersion = 0;
 			for (int i = 0; i < components.length; i++) {
 				EARArtifactEdit earArtifactEdit = null;
 				try {
 					WorkbenchComponent wc = (WorkbenchComponent)components[i];
 					if(wc.getComponentType().getModuleTypeId().equals(IModuleConstants.JST_EAR_MODULE)) {  
 						earArtifactEdit = EARArtifactEdit.getEARArtifactEditForRead(wc);
 					    if(j2eeVersion <= earArtifactEdit.getJ2EEVersion())
 						   earModuleList.add(wc.getName());
 						}
 					} finally {
 						if(earArtifactEdit != null)
 							earArtifactEdit.dispose();
 					}
 				   
 			   }
 			WTPPropertyDescriptor[] descriptors = new WTPPropertyDescriptor[earModuleList.size()];
 			for (int i = 0; i < descriptors.length; i++) {
 				descriptors[i] = new WTPPropertyDescriptor(earModuleList.get(i));
 			}
 			return descriptors;
 		   }
 		 } finally {
 			 if(mc != null)
 				 mc.dispose();
 		 }
 	  }
 	  return super.doGetValidPropertyDescriptors(propertyName);
 	}
 	
 	public IProject getProject() {
 		String projName = getStringProperty(J2EEComponentCreationDataModel.PROJECT_NAME );
 		if(projName != null && projName.length() > 0)
 		  return ProjectUtilities.getProject( projName );
 		return null;
 	}
 	
 	public WorkbenchComponent getTargetWorkbenchComponent() {
 		ModuleCore core = null;
 		try {
 			IProject flexProject = getProject();
 			if(flexProject != null) {
 				core = ModuleCore.getModuleCoreForRead(getProject());
 				if(core != null) {
 					WorkbenchComponent component = core.findWorkbenchModuleByDeployName(COMPONENT_NAME);
 					return component;
 				}
 			}
 		} finally {
 			if(core != null)
 				core.dispose();
 		}
 		return null;
 	}
 
 	protected IStatus doValidateProperty(String propertyName) {
 		if (EAR_MODULE_NAME.equals(propertyName) && getBooleanProperty(ADD_TO_EAR)) {
 			return validateEARModuleNameProperty();
 		} else if (NESTED_MODEL_VALIDATION_HOOK.equals(propertyName)) 
 					return OK_STATUS;
 
 		return super.doValidateProperty(propertyName);
 	}
 	
 	public EARComponentCreationDataModel getEarComponentCreationDataModel() {
         return earComponentCreationDataModel;
     }
 
 	private IStatus validateEARModuleNameProperty() {
 //		IProject earProject = applicationCreationDataModel.getTargetProject();
 //		if (null != earProject && earProject.exists()) {
 //			if (earProject.isOpen()) {
 //				try {
 //					EARNatureRuntime earNature = (EARNatureRuntime) earProject.getNature(IEARNatureConstants.NATURE_ID);
 //					if (earNature == null) {
 //						return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.PROJECT_NOT_EAR, new Object[]{earProject.getName()}));
 //					} else if (earNature.getJ2EEVersion() < getJ2EEVersion()) {
 //						String earVersion = EnterpriseApplicationCreationDataModel.getVersionString(earNature.getJ2EEVersion());
 //						return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.INCOMPATABLE_J2EE_VERSIONS, new Object[]{earProject.getName(), earVersion}));
 //					}
 //					return OK_STATUS;
 //				} catch (CoreException e) {
 //					return new Status(IStatus.ERROR, J2EEPlugin.PLUGIN_ID, -1, null, e);
 //				}
 //			}
 //			return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.PROJECT_ClOSED, new Object[]{earProject.getName()}));
 //		} else if (null != earProject && null != getTargetProject()) {
 //			if (earProject.getName().equals(getTargetProject().getName())) {
 //				return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SAME_MODULE_AND_EAR_NAME, new Object[]{earProject.getName()}));
 //			} else if (!CoreFileSystemLibrary.isCaseSensitive()) {
 //				if (earProject.getName().toLowerCase().equals(getTargetProject().getName().toLowerCase())) {
 //					return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.SAME_MODULE_AND_EAR_NAME, new Object[]{earProject.getName()}));
 //				}
 //			}
 //		}
 //		IStatus status = applicationCreationDataModel.validateProperty(EnterpriseApplicationCreationDataModel.PROJECT_NAME);
 //		if (status.isOK()) {
 //			status = applicationCreationDataModel.validateProperty(EnterpriseApplicationCreationDataModel.PROJECT_LOCATION);
 //		}
 //		return status;
 		
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * @return
 	 */
 	public final UpdateManifestDataModel getUpdateManifestDataModel() {
 		return jarDependencyDataModel;
 	}
 
 
 
 	public final ClassPathSelection getClassPathSelection() {
 //		boolean createNew = false;
 //		if (null == cachedSelection || !getApplicationCreationDataModel().getTargetProject().getName().equals(cachedSelection.getEARFile().getURI())) {
 //			createNew = true;
 //		}
 //		// close an existing cachedSelection
 //		if (createNew && cachedSelection != null) {
 //			EARFile earFile = cachedSelection.getEARFile();
 //			if (earFile != null)
 //				earFile.close();
 //		}
 //
 //		if (createNew && getTargetProject() != null) {
 //			cachedSelection = ClasspathSelectionHelper.createClasspathSelection(getTargetProject(), getModuleExtension(), getApplicationCreationDataModel().getTargetProject(), getModuleType());
 //		}
 		return cachedSelection;
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#dispose()
 	 */
 	public void dispose() {
 		if (cachedSelection != null)
 			cachedSelection.getEARFile().close();
 		super.dispose();
 	}
 
 	public void propertyChanged(WTPOperationDataModelEvent event) {
 		super.propertyChanged(event);
 		if (event.getDataModel() == addComponentToEARDataModel && event.getFlag() == WTPOperationDataModelEvent.PROPERTY_CHG && event.getPropertyName().equals(ArtifactEditOperationDataModel.PROJECT_NAME)) {
 		  earComponentCreationDataModel.setProperty(EARComponentCreationDataModel.COMPONENT_NAME, event.getProperty());
 		}
 	}
 
 	public String getModuleName() {
 		return getStringProperty(COMPONENT_NAME);
 	}
 	
 	protected abstract WTPPropertyDescriptor[] getValidComponentVersionDescriptors();
 	
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
 
	/**
	 * @return Returns the addComponentToEARDataModel.
	 */
	public AddComponentToEnterpriseApplicationDataModel getAddComponentToEARDataModel() {
		return addComponentToEARDataModel;
	}
	

	/**
	 * @param addComponentToEARDataModel The addComponentToEARDataModel to set.
	 */
	public void setAddComponentToEARDataModel(AddComponentToEnterpriseApplicationDataModel addComponentToEARDataModel) {
		this.addComponentToEARDataModel = addComponentToEARDataModel;
	}
	

 }
