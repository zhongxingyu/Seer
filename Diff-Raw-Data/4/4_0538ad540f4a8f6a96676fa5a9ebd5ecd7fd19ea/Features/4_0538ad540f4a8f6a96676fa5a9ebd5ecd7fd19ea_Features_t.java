 /**
  * Framework Web Archive
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.framework.actions.applications;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.ArtifactInfo.Scope;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.RequiredOption;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.ModulesProcessor;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class Features extends DynamicParameterModule {
     
     private static final long serialVersionUID = 6608382760989903186L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String projectCode = null;
 	private String externalCode = null;
 	private String fromPage = null;
 	private String selectedType = null;
 	private String application = null;
 	private List<String> techVersion = null;
 	private String moduleId = null;
 	private String version = null;
 	private String moduleType = null;
 	private String techId = null;
 	private String preVersion = null;
 	private Collection<String> dependentIds = null;
 	private Collection<String> dependentVersions = null;
 	private Collection<String> preDependentIds = null;
 	private Collection<String> preDependentVersions = null;
 	private List<String> pilotModules = null;
 	private List<String> pilotJSLibs = null;
 	private boolean isValidated = false;
 	private String configServerNames = null;
 	private String configDbNames = null;
 	private String fromTab = null;
 	private List<String> defaultModules =  null;
 	private String embedAppId = "";
 	private String appTypeId = "";
 	
 	private String name = "";
 	private String code = "";
 	private String description = "";
 	private String appDir = "";
 	private String oldAppDirName = "";
 	private String technology = "";
 	private String technologyVersion = "";
 	private String applicationVersion = "";
 	private String appId = "";
 	private String serverLayer = "";
 	private String dbLayer = "";
 	private String webserviceLayer = "";
 	private List<String> server = null;
 	private List<String> database = null;
 	private List<String> serverVersion = null;
     private List<String> databaseVersion = null;
     private List<String> webservice = null;
     private String webServiceError = "";
 	private String technologyId = "";
 	private String type = "";
 	private String customerId = "";
 	private String pilotProject = "";
 	
 	private String nameError = "";
 	private String codeError = "";
 	private String appDirError = "";
 	private boolean errorFound = false;
 	private String applicationVersionError = "";
 	private String serverError = "";
 	private String databaseError = "";
 	private String serverName = "";
 	private String databaseName = "";
 	private String featureType = "";
 	
 	private String configTemplateType = "";
 	
 	private String featureName = "";
 	
 	private List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 	private List<String> depArtifactGroupNames = new ArrayList<String>();
 	private List<String> depArtifactInfoIds = new ArrayList<String>();
 	private List<String> selArtifactGroupNames = new ArrayList<String>();
 	private List<String> selArtifactInfoIds = new ArrayList<String>();
 	private List<String> dependencyIds = new ArrayList<String>();
 	private boolean dependency = false;
 	
 	public String features() {
 		try {
 		    ProjectInfo projectInfo = null;
 			if (APP_INFO.equals(getFromTab())) {
 				projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
 			} else {
 				ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 				projectInfo = projectManager.getProject(getProjectId(), getCustomerId(), getAppId());
 			}
 			ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 			if (appInfo == null) {
                 appInfo = getApplicationInfo();
             } else if (appInfo == null) {
 				appInfo = new ApplicationInfo();
 			}
 			
 			appInfo.getAppDirName();
 			String compile = Scope.COMPILE.name().toLowerCase();
 			String runtime = Scope.RUNTIME.name().toLowerCase();
 			String provided = Scope.PROVIDED.name().toLowerCase();
 			String test = Scope.TEST.name().toLowerCase();
 			List<String> scopeList = new ArrayList<String>();
 			scopeList.add(compile);
 			scopeList.add(runtime);
 			scopeList.add(provided);
 			scopeList.add(test);
 			setReqAttribute(REQ_SCOPE, scopeList);
 			
 			List<SelectedFeature> listFeatures = new ArrayList<SelectedFeature>();
 			List<SelectedFeature> defaultFeatures = new ArrayList<SelectedFeature>();
 			setFeatures(appInfo, listFeatures);
 			if (StringUtils.isNotEmpty(getPilotProject()) || appInfo.getPilotInfo() != null) {
 				String id = null;
 				if (StringUtils.isNotEmpty(getPilotProject())) {
 					id = getPilotProject();
 				} else {
 					Element pilotInfo = appInfo.getPilotInfo();
 					id = pilotInfo.getId();
 				}
 				List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>)getSessionAttribute(REQ_PILOT_PROJECTS);
 				for (ApplicationInfo applicationInfo : pilotProjects) {
 					if (applicationInfo.getId().equals(id)) {
 						applicationInfo.setAppDirName(appInfo.getAppDirName());
 						List<String> selectedModules = applicationInfo.getSelectedModules();
 						List<ArtifactGroup> moduleGroups = new ArrayList<ArtifactGroup>();
 						if (CollectionUtils.isNotEmpty(selectedModules)) {
 							for (String selectedModule : selectedModules) {
 								ArtifactInfo moduleArtifactInfo = getServiceManager().getArtifactInfo(selectedModule);
 								String moduleArtifactGroupId = moduleArtifactInfo.getArtifactGroupId();
 								ArtifactGroup moduleArtifactGroupInfo = getServiceManager().getArtifactGroupInfo(moduleArtifactGroupId);
 								moduleGroups.add(moduleArtifactGroupInfo);
 							}
 							createArtifactInfoForPilotProject(moduleGroups, defaultFeatures, applicationInfo);
 						}
 						List<String> selectedJSLibs = applicationInfo.getSelectedJSLibs();
 						List<ArtifactGroup> jsLibs = new ArrayList<ArtifactGroup>();
 						if (CollectionUtils.isNotEmpty(selectedJSLibs)) {
 							for (String selectedJSLib : selectedJSLibs) {
 								ArtifactInfo jsLibArtifactInfo = getServiceManager().getArtifactInfo(selectedJSLib);
 								String jsLibArtifactGroupId = jsLibArtifactInfo.getArtifactGroupId();
 								ArtifactGroup jsLibArtifactGroupInfo = getServiceManager().getArtifactGroupInfo(jsLibArtifactGroupId);
 								jsLibs.add(jsLibArtifactGroupInfo);
 							}
 							createArtifactInfoForPilotProject(jsLibs, defaultFeatures, applicationInfo);
 						}
 						List<String> selectedComponents = applicationInfo.getSelectedComponents();
 						List<ArtifactGroup> components = new ArrayList<ArtifactGroup>();
 						if (CollectionUtils.isNotEmpty(selectedComponents)) {
 							for (String selectedComponent : selectedComponents) {
 								ArtifactInfo componentArtifactInfo = getServiceManager().getArtifactInfo(selectedComponent);
 								String componentArtifactGroupId = componentArtifactInfo.getArtifactGroupId();
 								ArtifactGroup componentArtifactGroupInfo = getServiceManager().getArtifactGroupInfo(componentArtifactGroupId);
 								components.add(componentArtifactGroupInfo);
 							}
 							createArtifactInfoForPilotProject(components, defaultFeatures, applicationInfo);
 						}
 					}
 				}
 			}
 			if (StringUtils.isEmpty(getTechnology()) && appInfo != null) {
 			    setTechnology(appInfo.getTechInfo().getId());
 			}
 			List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), ArtifactGroup.Type.FEATURE.name());
 			boolean hasModules = false;
 			if (CollectionUtils.isNotEmpty(moduleGroups)) {
 			    hasModules = true;
 				createArtifactInfoForDefault(moduleGroups, defaultFeatures, appInfo);
 			}
 			setReqAttribute(REQ_HAS_MODULES, hasModules);
 			
 			List<ArtifactGroup> jsLibsGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), ArtifactGroup.Type.JAVASCRIPT.name());
 			boolean hasJsLibs = false;
 			if (CollectionUtils.isNotEmpty(jsLibsGroups)) {
 			    hasJsLibs = true;
 				createArtifactInfoForDefault(jsLibsGroups, defaultFeatures, appInfo);
 			}
 			setReqAttribute(REQ_HAS_JSLIBS, hasJsLibs);
 			
 			List<ArtifactGroup> componentGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), ArtifactGroup.Type.COMPONENT.name());
 			boolean hasComponents = false;
 			if (CollectionUtils.isNotEmpty(componentGroups)) {
 			    hasComponents = true;
 				createArtifactInfoForDefault(componentGroups, defaultFeatures, appInfo);
 			}
 			setReqAttribute(REQ_HAS_COMPONENTS, hasComponents);
 			setReqAttribute(REQ_DEFAULT_FEATURES, defaultFeatures);
 			setSessionAttribute(REQ_SELECTED_FEATURES, listFeatures);
 			if (APP_INFO.equals(getFromTab())) {
 				projectInfo.setAppInfos(Collections.singletonList(createApplicationInfo(appInfo)));
 			}
 			if (StringUtils.isNotEmpty(getOldAppDirName())) {
 				setReqAttribute(REQ_OLD_APPDIR, getOldAppDirName());
 			} else {
 				setReqAttribute(REQ_OLD_APPDIR, appInfo.getAppDirName());
 			}
 			setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
 		} catch (Exception e) {
 			return showErrorPopup(new PhrescoException(e), getText("Feature Not Available"));
 		}
     	
 	    return APP_FEATURES;
 	}
 
 	private void createArtifactInfoForDefault(List<ArtifactGroup> moduleGroups, List<SelectedFeature> defaultFeatures, ApplicationInfo appInfo) throws PhrescoException, FileNotFoundException {
 		for (ArtifactGroup artifactGroup : moduleGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				if (CollectionUtils.isNotEmpty(appliesTo)) {
 					for (RequiredOption requiredOption : appliesTo) {
 						if (requiredOption.isRequired() && requiredOption.getTechId().equals(getTechnology())) {
 							SelectedFeature selectFeature = new SelectedFeature();
 							selectFeature.setDispValue(artifactInfo.getVersion());
 							selectFeature.setVersionID(artifactInfo.getId());
 							selectFeature.setModuleId(artifactInfo.getArtifactGroupId());
 							selectFeature.setDispName(artifactGroup.getDisplayName());
 							selectFeature.setName(artifactGroup.getName());
 							selectFeature.setType(artifactGroup.getType().name());
 							selectFeature.setArtifactGroupId(artifactGroup.getId());
 							selectFeature.setDefaultModule(true);
 							selectFeature.setPackaging(artifactGroup.getPackaging());
 							getScope(appInfo, artifactInfo.getId(), selectFeature);
 							getDefaultDependentFeatures(moduleGroups, defaultFeatures, artifactInfo);
 							getModulesFromProjectInfo(appInfo, artifactGroup, selectFeature);
 						    defaultFeatures.add(selectFeature);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void createArtifactInfoForPilotProject(List<ArtifactGroup> moduleGroups, List<SelectedFeature> defaultFeatures, ApplicationInfo appInfo) throws PhrescoException, FileNotFoundException {
 		for (ArtifactGroup artifactGroup : moduleGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				SelectedFeature selectFeature = new SelectedFeature();
 				selectFeature.setDispValue(artifactInfo.getVersion());
 				selectFeature.setVersionID(artifactInfo.getId());
 				selectFeature.setModuleId(artifactInfo.getArtifactGroupId());
 				selectFeature.setDispName(artifactGroup.getDisplayName());
 				selectFeature.setName(artifactGroup.getName());
 				selectFeature.setType(artifactGroup.getType().name());
 				selectFeature.setArtifactGroupId(artifactGroup.getId());
 				selectFeature.setDefaultModule(true);
 				selectFeature.setPackaging(artifactGroup.getPackaging());
 				getScope(appInfo, artifactInfo.getId(), selectFeature);
 				getDefaultDependentFeatures(moduleGroups, defaultFeatures, artifactInfo);
 				getModulesFromProjectInfo(appInfo, artifactGroup, selectFeature);
 			    defaultFeatures.add(selectFeature);
 			}
 		}
 	}
 
 	private void getModulesFromProjectInfo(ApplicationInfo appInfo, ArtifactGroup artifactGroup, SelectedFeature selectFeature) throws FileNotFoundException {
 		StringBuilder dotPhrescoPathSb = new StringBuilder(Utility.getProjectHome());
 		dotPhrescoPathSb.append(appInfo.getAppDirName());
 		dotPhrescoPathSb.append(File.separator);
 		dotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 		dotPhrescoPathSb.append(File.separator);
		String projectInfoFile = dotPhrescoPathSb.toString() + PROJECT_INFO;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(projectInfoFile));
 		Type type = new TypeToken<ProjectInfo>() {}.getType();
 		Gson gson = new Gson();
 		ProjectInfo projectinfo = gson.fromJson(bufferedReader, type);
 		ApplicationInfo applicationInfo = projectinfo.getAppInfos().get(0);
 		if (CollectionUtils.isNotEmpty(applicationInfo.getSelectedComponents())) {
 			List<String> selectedComponents = applicationInfo.getSelectedComponents();
 			if (selectedComponents.contains(selectFeature.getVersionID())) {
 				List<CoreOption> appliesTo1 = artifactGroup.getAppliesTo();
 				for (CoreOption coreOption : appliesTo1) {
 				    if (coreOption.getTechId().equals(appInfo.getTechInfo().getId()) && !coreOption.isCore()) {
 				    	selectFeature.setCanConfigure(true);
 				    }
 				}
 			}
 		}
 		
 		if (CollectionUtils.isNotEmpty(applicationInfo.getSelectedModules())) {
 			List<String> selectedModules = applicationInfo.getSelectedModules();
 			if (selectedModules.contains(selectFeature.getVersionID())) {
 				List<CoreOption> appliesTo1 = artifactGroup.getAppliesTo();
 				for (CoreOption coreOption : appliesTo1) {
 				    if (coreOption.getTechId().equals(appInfo.getTechInfo().getId()) && !coreOption.isCore()) {
 				    	selectFeature.setCanConfigure(true);
 				    }
 				}
 			}
 		}
 		if (CollectionUtils.isNotEmpty(applicationInfo.getSelectedJSLibs())) {
 			List<String> selectedJsLibs = applicationInfo.getSelectedJSLibs();
 			if (selectedJsLibs.contains(selectFeature.getVersionID())) {
 				List<CoreOption> appliesTo1 = artifactGroup.getAppliesTo();
 				for (CoreOption coreOption : appliesTo1) {
 				    if (coreOption.getTechId().equals(appInfo.getTechInfo().getId()) && !coreOption.isCore()) {
 				    	selectFeature.setCanConfigure(true);
 				    }
 				}
 			}
 		}
 	}
 	
 	private void getDefaultDependentFeatures(List<ArtifactGroup> moduleGroups, List<SelectedFeature> defaultFeatures, ArtifactInfo artifactInfo) {
 		dependencyIds = artifactInfo.getDependencyIds();
 		if (CollectionUtils.isNotEmpty(artifactInfo.getDependencyIds())) {
 		    dependencyIds.addAll(artifactInfo.getDependencyIds());
 		}
 		
 		if (CollectionUtils.isNotEmpty(getDependencyIds())) {
 			for (String dependencyId : getDependencyIds()) {
 				for (ArtifactGroup artifatGroup : moduleGroups) {
 					List<ArtifactInfo> versins = artifatGroup.getVersions();
 					for (ArtifactInfo artifatInfo : versins) {
 						if (artifatInfo.getId().equals(dependencyId)) {
 							SelectedFeature dependntFeature = new SelectedFeature();
 							dependntFeature.setDispValue(artifatInfo.getVersion());
 							dependntFeature.setVersionID(artifatInfo.getId());
 							dependntFeature.setModuleId(artifatInfo.getArtifactGroupId());
 							dependntFeature.setName(artifatGroup.getName());
 							dependntFeature.setDispName(artifatGroup.getDisplayName());
 							dependntFeature.setType(artifatGroup.getType().name());
 							dependntFeature.setArtifactGroupId(artifatGroup.getId());
 							dependntFeature.setDefaultModule(true);
 							defaultFeatures.add(dependntFeature);	
 							if (CollectionUtils.isNotEmpty(artifatInfo.getDependencyIds())) {
 								getDefaultDependentFeatures(moduleGroups, defaultFeatures, artifatInfo);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void setFeatures(ApplicationInfo appInfo, List<SelectedFeature> listFeatures) throws PhrescoException {
 		try {
 		    String selectedTechId = appInfo.getTechInfo().getId();
 			List<String> selectedModules = appInfo.getSelectedModules();
 			if (CollectionUtils.isNotEmpty(selectedModules)) {
 				for (String selectedModule : selectedModules) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedModule, selectedTechId, appInfo);
 					listFeatures.add(selectFeature);
 				}
 			}
 			
 			List<String> selectedJSLibs = appInfo.getSelectedJSLibs();
 			if (CollectionUtils.isNotEmpty(selectedJSLibs)) {
 				for (String selectedJSLib : selectedJSLibs) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedJSLib, selectedTechId, appInfo);
 					listFeatures.add(selectFeature);
 				}
 			}
 			
 			List<String> selectedComponents = appInfo.getSelectedComponents();
 			if (CollectionUtils.isNotEmpty(selectedComponents))	{
 				for (String selectedComponent : selectedComponents) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedComponent, selectedTechId, appInfo);
 					listFeatures.add(selectFeature);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private SelectedFeature createArtifactInformation(String selectedModule, String techId, ApplicationInfo appInfo) throws PhrescoException {
 		SelectedFeature slctFeature = new SelectedFeature();
 		ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(selectedModule);
 		
 		slctFeature.setDispValue(artifactInfo.getVersion());
 		slctFeature.setVersionID(artifactInfo.getId());
 		slctFeature.setModuleId(artifactInfo.getArtifactGroupId());
 		
 		String artifactGroupId = artifactInfo.getArtifactGroupId();
 		ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactGroupId);
 		slctFeature.setName(artifactGroupInfo.getName());
 		slctFeature.setDispName(artifactGroupInfo.getDisplayName());
 		slctFeature.setType(artifactGroupInfo.getType().name());
 		slctFeature.setArtifactGroupId(artifactGroupInfo.getId());
 		slctFeature.setPackaging(artifactGroupInfo.getPackaging());
 		getScope(appInfo, artifactInfo.getId(), slctFeature);
 		
 		List<CoreOption> appliesTo = artifactGroupInfo.getAppliesTo();
 		for (CoreOption coreOption : appliesTo) {
 		    if (coreOption.getTechId().equals(techId) && !coreOption.isCore()) {
 		        slctFeature.setCanConfigure(true);
 		    }
 		}
 		List<RequiredOption> appliesToReqird = artifactInfo.getAppliesTo();
 		if (CollectionUtils.isNotEmpty(appliesToReqird)) {
 			for (RequiredOption requiredOption : appliesToReqird) {
 				if (requiredOption.isRequired() && requiredOption.getTechId().equals(techId)) {
 					slctFeature.setDefaultModule(true);
 				}
 			}
 		}
 		
 		return slctFeature;
 		
 	}
 	
 	private void getScope(ApplicationInfo appInfo, String id, SelectedFeature selectFeature) throws PhrescoException {
 		StringBuilder dotPhrescoPathSb = new StringBuilder(Utility.getProjectHome());
 		dotPhrescoPathSb.append(appInfo.getAppDirName());
 		dotPhrescoPathSb.append(File.separator);
 		dotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 		dotPhrescoPathSb.append(File.separator);
 		String pluginInfoFile = dotPhrescoPathSb.toString() + APPLICATION_HANDLER_INFO_FILE;
 		MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 		ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 		String selectedFeatures = applicationHandler.getSelectedFeatures();
 		if(StringUtils.isNotEmpty(selectedFeatures)) {
 			Gson gson = new Gson();
 			Type jsonType = new TypeToken<Collection<ArtifactGroup>>(){}.getType();
 			List<ArtifactGroup> selectedArtifactGroups = gson.fromJson(selectedFeatures, jsonType);
 			for (ArtifactGroup artifactGroup : selectedArtifactGroups) {
 				for (ArtifactInfo artifactInfo : artifactGroup.getVersions()) {
 					if (artifactInfo.getId().equals(id)) {
 						selectFeature.setScope(artifactInfo.getScope());
 					}
 				}
 			}
 		}
 	}
 
 	/**
      * To validate the form fields
      * @return
      * @throws PhrescoException 
      */
     public String validateForm() throws PhrescoException {
     	if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Features.validateForm()");
         }
     	
     	ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
     	List<ProjectInfo> projects = projectManager.discover(getCustomerId());
     	boolean hasError = false;
     	if (APP_INFO.equals(getFromTab())) {
 	    	if (StringUtils.isEmpty(getName().trim())) {
 	    		 setNameError(getText(ERROR_NAME));
 	             hasError = true;
 	    	}
 	    	
 	    	if (StringUtils.isEmpty(getCode().trim())) {
 	    		setCodeError(getText(ERROR_CODE));
 	            hasError = true;
 	    	}
 	    	
 	    	if(StringUtils.isNotEmpty(getAppDir())) {
 		    	for(ProjectInfo project : projects) {
 		    		List<ApplicationInfo> appInfos = project.getAppInfos();
 		    		for (ApplicationInfo applicationInfo : appInfos) {
 		    			if(applicationInfo.getAppDirName().equals(getAppDir()) && !applicationInfo.getId().equals(getAppId())) {
 						 	setAppDirError(getText(ERROR_APP_DIR_EXISTS));
 						    hasError = true;
 						    break;
 						}
 					}
 		    	}
 			}
 	    	
 	    	if (StringUtils.isEmpty(getApplicationVersion())) {
 	    		setApplicationVersionError(getText(ERROR_VERSION));
 	            hasError = true;
 	    	}
 	    	
 	    	if (StringUtils.isNotEmpty(getWebserviceLayer()) && CollectionUtils.isEmpty(getWebservice())) {
 				setWebServiceError(getText(ERROR_WS_MISSING));
 				hasError = true;
 			}
 	    	
 	    	if (StringUtils.isNotEmpty(getDbLayer()) && CollectionUtils.isNotEmpty(getDatabase()) && StringUtils.isEmpty(getDatabase().get(0))) {
 				setDatabaseError(getText(ERROR_DB_MISSING));
 				hasError = true;
 			}
 	    	
 	    	if (StringUtils.isNotEmpty(getServerLayer()) && CollectionUtils.isNotEmpty(getServer()) && StringUtils.isEmpty(getServer().get(0))) {
 				setServerError(getText(ERROR_SERV_MISSING));
 				hasError = true;
 			}
 	    	
 	    	if (StringUtils.isNotEmpty(getServerLayer()) && CollectionUtils.isNotEmpty(getServer())) {
 				for (String serverId : getServer()) {
 					if (StringUtils.isNotEmpty(serverId) && ArrayUtils.isEmpty(getReqParameterValues(serverId))) {
 						DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(serverId);
 						setServerName(downloadInfo.getName());
 						setServerError(getText(ERROR_SERV_VER_MISSING, downloadInfo.getName()));
 						hasError = true;
 					}
 				}
 			}
 	    	
 	    	if (StringUtils.isNotEmpty(getDbLayer()) && CollectionUtils.isNotEmpty(getDatabase())) {
 				for (String databaeId : getDatabase()) {
 					if (StringUtils.isNotEmpty(databaeId) && ArrayUtils.isEmpty(getReqParameterValues(databaeId))) {
 						DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(databaeId);
 						setDatabaseName(downloadInfo.getName());
 						setDatabaseError(getText(ERROR_DB_VER_MISSING, downloadInfo.getName()));
 						hasError = true;
 					}
 				}
 			}
 	    	   	
 	    	if (hasError) {
 	            setErrorFound(true);
 	        }
     	}
     	return SUCCESS;
     }
 	
 	private ApplicationInfo createApplicationInfo(ApplicationInfo appInfo) {
     	appInfo.setId(getAppId());
     	appInfo.setName(getName());
     	appInfo.setAppDirName(getAppDir());
     	appInfo.setCode(getCode());
     	appInfo.setDescription(getDescription());
     	appInfo.setVersion(getApplicationVersion());
     	appInfo.setEmbedAppId(getEmbedAppId());
     	TechnologyInfo techInfo = new TechnologyInfo();
     	techInfo.setId(getTechnology());
     	techInfo.setVersion(getTechnologyVersion());
     	techInfo.setAppTypeId(getAppTypeId());
 		appInfo.setTechInfo(techInfo );
 		if (StringUtils.isNotEmpty(getPilotProject())) {
 			Element element = new Element();
 			element.setId(getPilotProject());
 			appInfo.setPilotInfo(element);
 		}
 
 		if (CollectionUtils.isNotEmpty(appInfo.getSelectedServers())) {
 			appInfo.getSelectedServers().clear();
 		}
 		
 		List<ArtifactGroupInfo> selectedServers = new ArrayList<ArtifactGroupInfo>();
 		if (StringUtils.isNotEmpty(getServerLayer()) && CollectionUtils.isNotEmpty(getServer())) {
 			for (String serverId : getServer()) {
 				if(StringUtils.isNotEmpty(serverId)) {
 					ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 					artifactGroupInfo.setArtifactGroupId(serverId);
 					artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(serverId)));
 					selectedServers.add(artifactGroupInfo);
 					appInfo.setSelectedServers(selectedServers);
 				}
 			}
 		}
     	
 		if (CollectionUtils.isNotEmpty(appInfo.getSelectedDatabases())) {
 			appInfo.getSelectedDatabases().clear();
 		}
 		
     	List<ArtifactGroupInfo> selectedDatabases = new ArrayList<ArtifactGroupInfo>();
     	if (StringUtils.isNotEmpty(getDbLayer()) && CollectionUtils.isNotEmpty(getDatabase())) {
 			for (String databaseId : getDatabase()) {
 				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 				artifactGroupInfo.setArtifactGroupId(databaseId);
 				artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(databaseId)));
 				selectedDatabases.add(artifactGroupInfo);
 				appInfo.setSelectedDatabases(selectedDatabases);
 			}
 			
 		}
     	
     	if (CollectionUtils.isNotEmpty(appInfo.getSelectedWebservices())) {
 			appInfo.getSelectedWebservices().clear();
 		}
 		
     	if (StringUtils.isNotEmpty(getWebserviceLayer()) && CollectionUtils.isNotEmpty(getWebservice())) {
 			appInfo.setSelectedWebservices(getWebservice());
 		}
     	
     	return appInfo;
 	}
 	
 	public String showFeatureConfigPopup() throws PhrescoException {
 	    try {
 	    	boolean returnStatus = populateFeatureConfigPopup();
 	    	if(!returnStatus) {
 		        setConfigTemplateType(CONFIG_FEATURES);
 		        setReqAttribute(REQ_TYPE, featureType);
 		        setReqAttribute(REQ_FEATURE_NAME, getFeatureName());
 		        List<PropertyTemplate> propertyTemplates = getTemplateConfigFile();
 		        setReqAttribute(REQ_PROPERTIES, propertyTemplates);
 		        setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
 		        return CONFIG;
 	    	}
 	    	return FEATURE;
 	    } catch (PhrescoException e) {
 	        return showErrorPopup(e, getText(EXCEPTION_FEATURE_MANIFEST_NOT_AVAILABLE));
 	    }
 	}
 	
 	public boolean populateFeatureConfigPopup() throws PhrescoException {
 	    try {
 	    	ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + FEATURE_CONFIG + SESSION_WATCHER_MAP);
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
             File featureManifest = new File(getManifest(appInfo, featureName));
             //If Xml does not exist, return false
             if(!featureManifest.exists()) {
             	return false;
             }
             ModulesProcessor module = new ModulesProcessor(new File(featureManifest.getPath()));
             List<Parameter> parameters = module.getParameters();
             if (CollectionUtils.isNotEmpty(parameters) && parameters.get(0) != null) {
 	            setPossibleValuesInReq(module, appInfo, parameters, watcherMap);
 	            setSessionAttribute(appInfo.getId() + FEATURE_CONFIG + SESSION_WATCHER_MAP, watcherMap);
 	            setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
 	            setReqAttribute(REQ_FEATURE_NAME, featureName);
 	            setReqAttribute(REQ_APP_INFO, appInfo);
 	        	return true;
             }
 	    } catch (PhrescoException e) {
 	    	throw e;
 	    }
 		return false;
 	}
 	
 	public String getThirdPartyFolder(ApplicationInfo appInfo) throws PhrescoException { 
 		File pomPath = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + Constants.POM_NAME);
 		try {
 			PomProcessor processor = new PomProcessor(pomPath);
 			String property = processor.getProperty(Constants.POM_PROP_KEY_MODULE_SOURCE_DIR);
 			if(StringUtils.isNotEmpty(property)) {
 				return property;
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return "";
 	}
 	
 	public String getManifest(ApplicationInfo appInfo, String FeatureName) throws PhrescoException {
 		try {
 			return Utility.getProjectHome() + appInfo.getAppDirName() + getThirdPartyFolder(appInfo) + File.separator + FeatureName + File.separator + "feature-manifest" + DOT + XML;
 		} catch (PhrescoException e) {
 			throw e;
 		}
 	}
 	
 	public String configureFeature() {
 	    try {
 	    	ApplicationInfo appInfo = getApplicationInfo();
             List<PropertyTemplate> propertyTemplates = getTemplateConfigFile();
             Properties properties = new Properties();
             for (PropertyTemplate propertyTemplate : propertyTemplates) {
                 String key  = propertyTemplate.getKey();
                 String value = getReqParameter(key);
                 properties.setProperty(key, value);
             }
             String[] keys = getReqParameterValues(REQ_KEY);
             String[] values = getReqParameterValues(REQ_VALUE);
             if (!ArrayUtils.isEmpty(keys) && !ArrayUtils.isEmpty(values)) {
                 for (int i = 0; i < keys.length; i++) {
                     if (StringUtils.isNotEmpty(keys[i]) && StringUtils.isNotEmpty(values[i])) {
                         properties.setProperty(keys[i], values[i]);
                     }
                 }
             }
             Configuration configuration = new Configuration();
             configuration.setName(getFeatureName());
             List<Environment> allEnvironments = getAllEnvironments(appInfo);
 	        for (Environment environment : allEnvironments) {
 	        	if(environment.isDefaultEnv()) {
 	        		configuration.setEnvName(environment.getName());
 	        	}
 	        }
             configuration.setProperties(properties);
             configuration.setType(getFeatureType());
             List<Configuration> configs = new ArrayList<Configuration>();
             configs.add(configuration);
             getApplicationProcessor().postFeatureConfiguration(getApplicationInfo(), configs, getFeatureName());
         } catch (PhrescoException  e) {
             return showErrorPopup(e, getText(EXCEPTION_FEATURE_MANIFEST_NOT_AVAILABLE));
         } catch (ConfigurationException e) {
         	return showErrorPopup(new PhrescoException(e), getText("Environments Not Available"));
 		}
 	    
 	    return SUCCESS;
 	}
 	
 	public String configureFeatureparam() {
 		try {
 			ApplicationInfo appInfo = getApplicationInfo();
 	        File featureManifest = new File(getManifest(appInfo, featureName));
 	        ModulesProcessor module = new ModulesProcessor(new File(featureManifest.getPath()));
 	        List<Parameter> parameters = module.getParameters();
 	        Properties properties = new Properties();
 	        for(Parameter parameter : parameters) {
 	        	String paramType = parameter.getType();
 	        	String paramName = parameter.getName().getValue().getValue();
 		    	String key  = parameter.getKey();
 		        String value = getReqParameter(key);
 		        if(TYPE_BOOLEAN.equalsIgnoreCase(paramType)) {
 		        	value = (StringUtils.isNotEmpty(value) ? value : FALSE);
 		        } else {
 		        	value = (StringUtils.isNotEmpty(value)? value : " ");
 		        }
 		        String configName = module.getConfigName(paramName);
 		        properties.setProperty(configName, value);
 	    	}
 	        
 	        String[] keys = getReqParameterValues(REQ_KEY);
 	        String[] values = getReqParameterValues(REQ_VALUE);
 	        if (!ArrayUtils.isEmpty(keys) && !ArrayUtils.isEmpty(values)) {
 	            for (int i = 0; i < keys.length; i++) {
 	                if (StringUtils.isNotEmpty(keys[i]) && StringUtils.isNotEmpty(values[i])) {
 	                    properties.setProperty(keys[i], values[i]);
 	                }
 	            }
 	        }
 	        
 	        
 	        Configuration configuration = new Configuration();
 	        configuration.setName(getFeatureName());
 	        List<Environment> allEnvironments = getAllEnvironments(appInfo);
 	        for (Environment environment : allEnvironments) {
 	        	if(environment.isDefaultEnv()) {
 	        		configuration.setEnvName(environment.getName());
 	        	}
 	        }
 	        configuration.setProperties(properties);
 	        List<Configuration> configs = new ArrayList<Configuration>();
 	        configs.add(configuration);
 	        getApplicationProcessor().postFeatureConfiguration(getApplicationInfo(), configs, getFeatureName());
 		} catch(PhrescoException e ){
 			return showErrorPopup(e, getText(EXCEPTION_FEATURE_MANIFEST_NOT_AVAILABLE));
 		} catch (ConfigurationException e) {
 			return showErrorPopup(new PhrescoException(e), getText("Environments Not Available"));
 		}
 		
 		return SUCCESS;
 	}
 	
 	private List<Environment> getAllEnvironments(ApplicationInfo appInfo) throws PhrescoException, ConfigurationException {
 		String configPath = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + FOLDER_DOT_PHRESCO + File.separator + CONFIGURATION_INFO_FILE_NAME ;
 		ConfigManager configManager = getConfigManager(configPath);
 		return configManager.getEnvironments();
 	}
 	
 	private ConfigManager getConfigManager(String configPath) throws PhrescoException {
 	        File appDirectory = new File(configPath);
 	        return PhrescoFrameworkFactory.getConfigManager(appDirectory);
     }
 	
 	private List<PropertyTemplate> getTemplateConfigFile() throws PhrescoException {
 	    List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
 	    try {
 	        List<Configuration> featureConfigurations = getApplicationProcessor().preFeatureConfiguration(getApplicationInfo(), getFeatureName());
 	        Properties properties = null;
 	        if (CollectionUtils.isNotEmpty(featureConfigurations)) {
 	            for (Configuration featureConfiguration : featureConfigurations) {
 	                properties = featureConfiguration.getProperties();
 	                Set<Object> keySet = properties.keySet();
 	                for (Object key : keySet) {
 	                    String keyStr = (String) key;
 	                    String dispName = keyStr.replace(".", " ");
 	                    PropertyTemplate propertyTemplate = new PropertyTemplate();
 	                    propertyTemplate.setKey(keyStr);
 	                    propertyTemplate.setName(dispName);
 	                    propertyTemplates.add(propertyTemplate);
 	                }
 	            }
 	        }
 	        setReqAttribute(REQ_PROPERTIES_INFO, properties);
 	        setReqAttribute(REQ_HAS_CUSTOM_PROPERTY, true);
 	    } catch (PhrescoException e) {
 	        throw e;
 	    }
 	    
 	    return propertyTemplates;
 	}
 	
 	public String listFeatures() throws PhrescoException {
 		List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnologyId(), getType());
 		if (CollectionUtils.isNotEmpty(moduleGroups)) {
 			Collections.sort(moduleGroups, sortFeaturesNameInAlphaOrder());
 		}
 		setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
 		setReqAttribute(REQ_FEATURES_TYPE, getType());
 		setReqAttribute(REQ_APP_ID, getAppId());
 		setReqAttribute(REQ_TECHNOLOGY, getTechnologyId());
 		
 		return APP_FEATURES_LIST;
 	}
 	
 	private Comparator sortFeaturesNameInAlphaOrder() {
 		return new Comparator() {
 		    public int compare(Object firstObject, Object secondObject) {
 		    	ArtifactGroup feature1 = (ArtifactGroup) firstObject;
 		    	ArtifactGroup feature2 = (ArtifactGroup) secondObject;
 		       return feature1.getName().compareToIgnoreCase(feature2.getName());
 		    }
 		};
 	}
 	
 	public String fetchDefaultFeatures() {
 		Gson gson = new Gson();
 		String json = gson.toJson(getArtifactGroups());
 		List<ArtifactGroup> artifactGroups = gson.fromJson(json, new TypeToken<List<ArtifactGroup>>(){}.getType());
 		for (ArtifactGroup artifactGroup : artifactGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				if(CollectionUtils.isNotEmpty(appliesTo)) {
 					for (RequiredOption requiredOption : appliesTo) {
 						if (requiredOption.isRequired() && requiredOption.getTechId().equals(getTechnology())) {
 							depArtifactGroupNames.add(artifactGroup.getName());
 							depArtifactInfoIds.add(artifactInfo.getId());
 							if(CollectionUtils.isNotEmpty(artifactInfo.getDependencyIds())) {
 								getDependentForDefaultFeatures(artifactGroups, artifactInfo);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return SUCCESS;
 	}
 
 	private void getDependentForDefaultFeatures(List<ArtifactGroup> artifactGroups, ArtifactInfo artifactInfo) {
 		for (String dependentId : artifactInfo.getDependencyIds()) {
 			for (ArtifactGroup dependentArtifactGroup : artifactGroups) {
 				List<ArtifactInfo> depdntVersions = dependentArtifactGroup.getVersions();
 				for (ArtifactInfo depdntArtifactInfo : depdntVersions) {
 					if (depdntArtifactInfo.getId().equals(dependentId)) {
 						depArtifactGroupNames.add(dependentArtifactGroup.getName());
 						depArtifactInfoIds.add(depdntArtifactInfo.getId());
 						if (CollectionUtils.isNotEmpty(depdntArtifactInfo.getDependencyIds())) {
 							getDependentForDefaultFeatures(artifactGroups, depdntArtifactInfo);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public String fetchSelectedFeatures() throws PhrescoException {
 		List<SelectedFeature> allFeatures = (List<SelectedFeature>)getSessionAttribute(REQ_SELECTED_FEATURES);
 		List<ArtifactGroup> selectedArtifactGroups = getServiceManager().getFeatures(getCustomerId(), getTechId(), getType());
 		getSelectedFeatures(allFeatures, selectedArtifactGroups);
 		
 		return SUCCESS;
 	}
 	
 	private void getSelectedFeatures(List<SelectedFeature> selectedFeatues, List<ArtifactGroup> artifactGroups) {
 		if (CollectionUtils.isNotEmpty(selectedFeatues)) {
 			for (SelectedFeature selectedModule : selectedFeatues) {
 				for (ArtifactGroup artifactGroup : artifactGroups) {
 					List<ArtifactInfo> versions = artifactGroup.getVersions();
 					for (ArtifactInfo artifactInfo : versions) {
 						if (artifactInfo.getId().equals(selectedModule.getVersionID())) {
 							selArtifactGroupNames.add(artifactGroup.getName());
 							selArtifactInfoIds.add(artifactInfo.getId());
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public String fetchDependentFeatures() {
 		Gson gson = new Gson();
 		String json = gson.toJson(getArtifactGroups());
 		List<ArtifactGroup> dependentArtifactGroups = gson.fromJson(json, new TypeToken<List<ArtifactGroup>>(){}.getType());
 		for (ArtifactGroup artifactGroup : dependentArtifactGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				if (artifactInfo.getId().equals(getModuleId())) {
 				    dependencyIds = artifactInfo.getDependencyIds();
 				    if (CollectionUtils.isNotEmpty(artifactInfo.getDependencyIds())) {
 				        dependencyIds.addAll(artifactInfo.getDependencyIds());
 				    }
 				}
 			}
 		}
 		//To get the artifactgroup name for the dependent artifactInfo ids
 		if (CollectionUtils.isNotEmpty(getDependencyIds())) {
     		for (String dependencyId : getDependencyIds()) {
     			for (ArtifactGroup artifactGroup : dependentArtifactGroups) {
     				List<ArtifactInfo> versions = artifactGroup.getVersions();
     				for (ArtifactInfo artifactInfo : versions) {
     					if (artifactInfo.getId().equals(dependencyId)) {
     						depArtifactGroupNames.add(artifactGroup.getName());
     						if(CollectionUtils.isNotEmpty(artifactInfo.getDependencyIds())) {
     							getDependentForDefaultFeatures(dependentArtifactGroups, artifactInfo);
     						}
     					}
     				}
     			}
     		}
     		setDependency(true);
 		}
 		
 		return SUCCESS;
 	}
 	
 	public Collection<String> getDependentIds() {
 		return dependentIds;
 	}
 
 	public void setDependentIds(Collection<String> dependentIds) {
 		this.dependentIds = dependentIds;
 	}
 
 	public Collection<String> getDependentVersions() {
 		return dependentVersions;
 	}
 
 	public void setDependentVersions(Collection<String> dependentVersions) {
 		this.dependentVersions = dependentVersions;
 	}
 
 	public Collection<String> getPreDependentIds() {
 		return preDependentIds;
 	}
 
 	public void setPreDependentIds(Collection<String> dependentIds) {
 		this.preDependentIds = dependentIds;
 	}
 
 	public Collection<String> getPreDependentVersions() {
 		return preDependentVersions;
 	}
 
 	public void setPreDependentVersions(Collection<String> dependentVersions) {
 		this.preDependentVersions = dependentVersions;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getPreVersion() {
 		return preVersion;
 	}
 
 	public void setPreVersion(String preVersion) {
 		this.preVersion = preVersion;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 
 	public String getModuleType() {
 		return moduleType;
 	}
 
 	public void setModuleType(String moduleType) {
 		this.moduleType = moduleType;
 	}
 
 	public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 
 	public String getProjectCode() {
 		return projectCode;
 	}
 
 	public void setProjectCode(String projectCode) {
 		this.projectCode = projectCode;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getApplication() {
 		return application;
 	}
 
 	public void setApplication(String application) {
 		this.application = application;
 	}
 
 	public String getTechnology() {
 		return technology;
 	}
 
 	public void setTechnology(String technology) {
 		this.technology = technology;
 	}
 
 	public String getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 
 	public List<String> getPilotModules() {
 		return pilotModules;
 	}
 
 	public void setPilotModules(List<String> pilotModules) {
 		this.pilotModules = pilotModules;
 	}
 
 	public List<String> getPilotJSLibs() {
 		return pilotJSLibs;
 	}
 
 	public void setPilotJSLibs(List<String> pilotJSLibs) {
 		this.pilotJSLibs = pilotJSLibs;
 	}
 
 	public boolean isValidated() {
 		return isValidated;
 	}
 
 	public void setValidated(boolean isValidated) {
 		this.isValidated = isValidated;
 	}
 
 	public List<String> getTechVersion() {
 		return techVersion;
 	}
 
 	public void setTechVersion(List<String> techVersion) {
 		this.techVersion = techVersion;
 	}
 
 	public String getConfigServerNames() {
 		return configServerNames;
 	}
 
 	public void setConfigServerNames(String configServerNames) {
 		this.configServerNames = configServerNames;
 	}
 
 	public String getConfigDbNames() {
 		return configDbNames;
 	}
 
 	public void setConfigDbNames(String configDbNames) {
 		this.configDbNames = configDbNames;
 	}
 
 	public String getExternalCode() {
 		return externalCode;
 	}
 
 	public void setExternalCode(String externalCode) {
 		this.externalCode = externalCode;
 	}
 	
 	public String getFromTab() {
 		return fromTab;
 	}
 
 	public void setFromTab(String fromTab) {
 		this.fromTab = fromTab;
 	}
 	
 	public List<String> getDefaultModules() {
 		return defaultModules;
 	}
 
 	public void setDefaultModules(List<String> defaultModules) {
 		this.defaultModules = defaultModules;
 	}
 	
 	public String getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
     
     public List<String> getServerVersion() {
 		return serverVersion;
 	}
     
 	public void setServerVersion(List<String> serverVersion) {
 		this.serverVersion = serverVersion;
 	}
 	
 	public List<String> getDatabaseVersion() {
 		return databaseVersion;
 	}
 	
 	public void setDatabaseVersion(List<String> databaseVersion) {
 		this.databaseVersion = databaseVersion;
 	}
 	
 	public List<String> getWebservice() {
 		return webservice;
 	}
 	
 	public void setWebservice(List<String> webservice) {
 		this.webservice = webservice;
 	}
 	
 	public String getApplicationVersion() {
 		return applicationVersion;
 	}
 
 	public void setApplicationVersion(String applicationVersion) {
 		this.applicationVersion = applicationVersion;
 	}
 
 	public String getTechnologyId() {
 		return technologyId;
 	}
 
 	public void setTechnologyId(String technologyId) {
 		this.technologyId = technologyId;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getAppId() {
 		return appId;
 	}
 
 	public void setAppId(String appId) {
 		this.appId = appId;
 	}
 
 	public String getPilotProject() {
 		return pilotProject;
 	}
 
 	public void setPilotProject(String pilotProject) {
 		this.pilotProject = pilotProject;
 	}
 
 	public List<String> getServer() {
 		return server;
 	}
 
 	public void setServer(List<String> server) {
 		this.server = server;
 	}
 
 	public List<String> getDatabase() {
 		return database;
 	}
 
 	public String getAppDir() {
 		return appDir;
 	}
 
 	public void setAppDir(String appDir) {
 		this.appDir = appDir;
 	}
 
 	public void setDatabase(List<String> database) {
 		this.database = database;
 	}
 
 	public String getOldAppDirName() {
 		return oldAppDirName;
 	}
 
 	public void setOldAppDirName(String oldAppDirName) {
 		this.oldAppDirName = oldAppDirName;
 	}
 
     public String getConfigTemplateType() {
         return configTemplateType;
     }
 
     public void setConfigTemplateType(String configTemplateType) {
         this.configTemplateType = configTemplateType;
     }
 
     public void setFeatureName(String featureName) {
         this.featureName = featureName;
     }
 
     public String getFeatureName() {
         return featureName;
     }
 
 	public List<ArtifactGroup> getArtifactGroups() {
 		return artifactGroups;
 	}
 
 	public void setArtifactGroups(List<ArtifactGroup> artifactGroups) {
 		this.artifactGroups = artifactGroups;
 	}
 
 	public List<String> getDepArtifactGroupNames() {
 		return depArtifactGroupNames;
 	}
 
 	public List<String> getDepArtifactInfoIds() {
 		return depArtifactInfoIds;
 	}
 
 	public void setDepArtifactGroupNames(List<String> depArtifactGroupNames) {
 		this.depArtifactGroupNames = depArtifactGroupNames;
 	}
 
 	public void setDepArtifactInfoIds(List<String> depArtifactInfoIds) {
 		this.depArtifactInfoIds = depArtifactInfoIds;
 	}
 
 	public String getCodeError() {
 		return codeError;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setCodeError(String codeError) {
 		this.codeError = codeError;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getTechnologyVersion() {
 		return technologyVersion;
 	}
 
 	public void setTechnologyVersion(String technologyVersion) {
 		this.technologyVersion = technologyVersion;
 	}
 
 	public List<String> getDependencyIds() {
 		return dependencyIds;
 	}
 
 	public void setDependencyIds(List<String> dependencyIds) {
 		this.dependencyIds = dependencyIds;
 	}
 
     public boolean isDependency() {
         return dependency;
     }
 
     public void setDependency(boolean dependency) {
         this.dependency = dependency;
     }
 
     public String getSelectedType() {
         return selectedType;
     }
 
     public void setSelectedType(String selectedType) {
         this.selectedType = selectedType;
     }
 
 	public String getApplicationVersionError() {
 		return applicationVersionError;
 	}
 
 	public void setApplicationVersionError(String applicationVersionError) {
 		this.applicationVersionError = applicationVersionError;
 	}
 	
 	public String getAppDirError() {
 		return appDirError;
 	}
 
 	public void setAppDirError(String appDirError) {
 		this.appDirError = appDirError;
 	}
 	
 	public void setServerError(String serverError) {
 		this.serverError = serverError;
 	}
 
 	public String getServerError() {
 		return serverError;
 	}
 
 	public void setDatabaseError(String databaseError) {
 		this.databaseError = databaseError;
 	}
 
 	public String getDatabaseError() {
 		return databaseError;
     }
 
 	public void setServerName(String serverName) {
 		this.serverName = serverName;
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public void setDatabaseName(String databaseName) {
 		this.databaseName = databaseName;
 	}
 
 	public String getDatabaseName() {
 		return databaseName;
 	}
 
 	public List<String> getSelArtifactGroupNames() {
 		return selArtifactGroupNames;
 	}
 
 	public void setSelArtifactGroupNames(List<String> selArtifactGroupNames) {
 		this.selArtifactGroupNames = selArtifactGroupNames;
 	}
 
 	public List<String> getSelArtifactInfoIds() {
 		return selArtifactInfoIds;
 	}
 
 	public void setSelArtifactInfoIds(List<String> selArtifactInfoIds) {
 		this.selArtifactInfoIds = selArtifactInfoIds;
 	}
 
 	public String getEmbedAppId() {
         return embedAppId;
     }
 
     public void setEmbedAppId(String embedAppId) {
         this.embedAppId = embedAppId;
     }
 
     public String getAppTypeId() {
         return appTypeId;
     }
 
     public void setAppTypeId(String appTypeId) {
         this.appTypeId = appTypeId;
     }
 
 	public String getServerLayer() {
 		return serverLayer;
 	}
 
 	public void setServerLayer(String serverLayer) {
 		this.serverLayer = serverLayer;
 	}
 
 	public String getDbLayer() {
 		return dbLayer;
 	}
 
 	public void setDbLayer(String dbLayer) {
 		this.dbLayer = dbLayer;
 	}
 
 	public void setWebserviceLayer(String webserviceLayer) {
 		this.webserviceLayer = webserviceLayer;
 	}
 
 	public String getWebserviceLayer() {
 		return webserviceLayer;
 	}
 
 	public void setWebServiceError(String webServiceError) {
 		this.webServiceError = webServiceError;
 	}
 
 	public String getWebServiceError() {
 		return webServiceError;
 	}
 	
 	public String getFeatureType() {
 		return featureType;
 	}
 
 	public void setFeatureType(String featureType) {
 		this.featureType = featureType;
 	}
 }
