 /*
  * ###
  * Framework Web Archive
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.framework.actions.applications;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.opensymphony.xwork2.Action;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.commons.ApplicationsUtil;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 
 public class Features extends FrameworkBaseAction {
 	private static final long serialVersionUID = 6608382760989903186L;
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 	private String projectCode = null;
 	private String externalCode = null;
 	private String fromPage = null;
 	
 	private String application = null;
 	private List<String> techVersion = null;
 	private String nameError = null;
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
 	
 	private String name = "";
 	private String code = "";
 	private String description = "";
 	private String appDir = "";
 	private String oldAppDirName = "";
 	private String technology = null;
 	private String applicationVersion = "";
 	private String appId = "";
 	private List<String> server = null;
 	private List<String> database = null;
 	private List<String> serverVersion = null;
     private List<String> databaseVersion = null;
     private List<String> webservice = null;
 	private String technologyId = "";
 	private String type = "";
 	private String customerId = "";
 	private String pilotProject = "";
 	
 	public String features() {
 		try {
 			ProjectInfo projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
 			ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 			if (appInfo == null) {
 				appInfo = new ApplicationInfo();
 			}
 			projectInfo.setAppInfos(Collections.singletonList(createApplicationInfo(appInfo)));
 			setReqAttribute(REQ_OLD_APPDIR, getOldAppDirName());
 			setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
 	    return APP_FEATURES;
 	}
 	
 	private ApplicationInfo createApplicationInfo(ApplicationInfo appInfo) {
     	appInfo.setId(getAppId());
     	appInfo.setName(getName());
     	appInfo.setAppDirName(getAppDir());
     	appInfo.setCode(getCode());
     	appInfo.setDescription(getDescription());
     	appInfo.setVersion(getApplicationVersion());
     	TechnologyInfo techInfo = new TechnologyInfo();
     	techInfo.setId(getTechnology());
 		appInfo.setTechInfo(techInfo );
		Element element = new Element();
		element.setId(getPilotProject());
		appInfo.setPilotInfo(element);
 		appInfo.setSelectedWebservices(getWebservice());
 		List<ArtifactGroupInfo> selectedServers = new ArrayList<ArtifactGroupInfo>();
 		if (CollectionUtils.isNotEmpty(getServer())) {
 			for (String serverId : getServer()) {
 				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 				artifactGroupInfo.setArtifactGroupId(serverId);
 				artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(serverId)));
 				selectedServers.add(artifactGroupInfo);
 			}
 			appInfo.setSelectedServers(selectedServers);
 		}
     	
     	
     	List<ArtifactGroupInfo> selectedDatabases = new ArrayList<ArtifactGroupInfo>();
     	if (CollectionUtils.isNotEmpty(getDatabase())) {
     		
 			for (String databaseId : getDatabase()) {
 				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 				artifactGroupInfo.setArtifactGroupId(databaseId);
 				artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(databaseId)));
 				selectedDatabases.add(artifactGroupInfo);
 				appInfo.setSelectedDatabases(selectedDatabases);
 			}
 			
 		}
     	return appInfo;
 	}
 
 	public String listFeatures() throws PhrescoException {
 		List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnologyId(), getType());
 		setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
 		setReqAttribute(REQ_FEATURES_TYPE, getType());
 		setReqAttribute(REQ_APP_ID, getAppId());
 		
 		return APP_FEATURES_LIST;
 	}
 	/*public String features1() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.features()");
 		}
 		String returnPage = APP_FEATURES_ONE_CLM;
 		boolean left = false;
 		boolean rightBottom = false;
 		boolean right = false;
 
 		try {
 			ApplicationInfo appInfo = null;
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			if (validate(administrator) && StringUtils.isEmpty(fromPage)) {
 				return Action.SUCCESS;
 			}
 			if (StringUtils.isEmpty(fromPage)
 					&& StringUtils.isNotEmpty(projectCode)) { // previous button
 																// clicked
 				appInfo = (ApplicationInfo) getHttpSession().getAttribute(
 						projectCode);
 			} else if (StringUtils.isNotEmpty(fromPage)) { // For edit project
 				appInfo = administrator.getProject(projectCode).getApplicationInfo();
 				if (description != null) {
 					appInfo.setDescription(description);
 				}
 				//TODO:Need to Handle
 //				if (externalCode != null) {
 //					appInfo.setProjectCode(externalCode);
 //				}
 //				if (StringUtils.isNotEmpty(projectVersion)) {
 //					appInfo.setVersion(projectVersion);
 //				}
 				//TODO:Need to Handle
 //				if (groupId != null) {
 //					appInfo.setGroupId(groupId);
 //				}
 //				if (artifactId != null) {
 //					appInfo.setArtifactId(artifactId);
 //				}
 					
 				application = appInfo.getTechInfo().getAppTypeId();
 				technology = appInfo.getTechInfo().getVersion();
 
 				setTechnology(appInfo, administrator);
 			} else { // For creating new project
 				appInfo = new ApplicationInfo();
 			}
 			if (StringUtils.isEmpty(fromPage)) {
 				setAppInfos(appInfo, administrator);
 			}
 
 			getHttpRequest().setAttribute(REQ_TEMP_SELECTED_PILOT_PROJ, getHttpRequest().getParameter(REQ_TEMP_SELECTED_PILOT_PROJ));
 
 			String selectedFeatures = getHttpRequest().getParameter(REQ_TEMP_SELECTEDMODULES);
 			if (StringUtils.isNotEmpty(selectedFeatures)) {
 				Map<String, String> mapFeatures = ApplicationsUtil.stringToMap(selectedFeatures);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTEDMODULES, mapFeatures);
 			}
 
 			String selectedJsLibs = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
 			if (StringUtils.isNotEmpty(selectedJsLibs)) {
 				Map<String, String> mapJsLibs = ApplicationsUtil.stringToMap(selectedJsLibs);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLibs);
 			}
 
 			setFeaturesInRequest(administrator, appInfo);
 			getHttpRequest().setAttribute(REQ_APPINFO, appInfo);
 
 			List<ArtifactGroup> coreModules = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_CORE_MODULES);
 			List<ArtifactGroup> customModules = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_CUSTOM_MODULES);
 			List<ArtifactGroup> allJsLibs = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_ALL_JS_LIBS);
 
 			// Assigning the position of the coreModule
 			if (CollectionUtils.isNotEmpty(coreModules)) { // Assigning coreModule to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_EXTERNAL_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, coreModules);
 			}
 
 			// Assigning the position of the customModule
 			if (!left && CollectionUtils.isNotEmpty(customModules)) { // Assigning customModule to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_CUSTOM_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, customModules);
 			} else if (left && CollectionUtils.isNotEmpty(customModules)) { // Assigning customModule to the right bottom position
 				right = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_SECOND_MDL_CAT, REQ_CUSTOM_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_RIGHT_MODULES, customModules);
 			}
 
 			// Assigning the position of the JSLibraries
 			if (left && right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the right bottom position
 				rightBottom = true;
 			} else if (left && !right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the right position
 				right = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_SECOND_MDL_CAT, REQ_JS_LIBS);
 				getHttpRequest().setAttribute(REQ_FEATURES_RIGHT_MODULES, allJsLibs);
 			} else if (!left && !right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_JS_LIBS);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, allJsLibs);
 			}
 
 			if (left && right && rightBottom) {
 				returnPage = APP_FEATURES_THREE_CLM;
 			} else if (left && right && !rightBottom) {
 				returnPage = APP_FEATURES_TWO_CLM;
 			}
 			getHttpRequest().setAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
 			getHttpRequest().setAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
 			FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 			getHttpRequest().setAttribute(REQ_SERVER_URL, configuration.getServerPath());
 			
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Features.list()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature list");
 		}
 		
 		return returnPage;
 	}
 
 	private void setAppInfos(ApplicationInfo appInfo, ProjectAdministrator administrator) throws PhrescoException {
 		HttpServletRequest request = getHttpRequest();
 		appInfo.setName(name);
 		appInfo.setCode(code);
 		//TODO:Need to handle
 //		if (externalCode != null) {
 //			appInfo.setProjectCode(externalCode);
 //		}
 //		if (groupId != null) {
 //			appInfo.setGroupId(groupId);
 //		}
 //		if (artifactId != null) {
 //			appInfo.setArtifactId(artifactId);
 //		}
 //		appInfo.setVersion(projectVersion);
 		appInfo.setDescription(description);
 		appInfo.setTechInfo(new TechnologyInfo(application, technology));
 		String pilotProjectName = getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ);
 		appInfo.setPilotInfo(new Element(pilotProjectName));
 
 		setTechnology(appInfo, administrator);
 		FrameworkUtil.setAppInfoDependents(request, getCustomerId());
 	}
 
 	private void setTechnology(ApplicationInfo appInfo, ProjectAdministrator administrator) throws PhrescoException {
 		ProjectInfo tempprojectInfo = null;
 		//TODO:Need to handle
 //		Technology selectedTechnology = administrator.getApplicationType(application).getTechonology(technology);
 		Technology technology = new Technology();
 
 		//TODO:Need to handle
 		technology.setId(selectedTechnology.getId());
 		technology.setName(selectedTechnology.getName());
 		if (StringUtils.isEmpty(fromPage)) {
 			technology.setVersions(techVersion);
 		} else {
 			tempprojectInfo = administrator.getProject(projectCode).getApplicationInfo();
 			List<String> projectInfoTechVersions = new ArrayList<String>();
 			List<String> tempPrjtInfoTechVersions = tempprojectInfo.getTechnology().getVersions();
 			if (tempPrjtInfoTechVersions != null && CollectionUtils.isNotEmpty(tempPrjtInfoTechVersions)) {
 				projectInfoTechVersions.addAll(tempprojectInfo.getTechnology().getVersions());
 				technology.setVersions(projectInfoTechVersions);
 			}
 		}
 		
 		if (StringUtils.isNotEmpty(fromPage)) {// For project edit
 			technology.setJsLibraries(appInfo.getTechnology()
 					.getJsLibraries());
 			technology.setModules(appInfo.getTechnology().getModules());
 		}
 
 		List<Server> servers = selectedTechnology.getServers();
 		List<Database> databases = selectedTechnology.getDatabases();
 		List<WebService> webservices = selectedTechnology.getWebservices();
 		
 		String selectedServers = getHttpRequest().getParameter("selectedServers");
 		String selectedDatabases = getHttpRequest().getParameter("selectedDatabases");
 		String[] selectedWebservices = getHttpRequest().getParameterValues(REQ_WEBSERVICES);
 		boolean isEmailSupported = false;
 		
 		if (StringUtils.isNotEmpty(fromTab)) {
 			if (selectedServers != null) {
 				List<String> listTempSelectedServers = null;
 				if (StringUtils.isNotEmpty(selectedServers)) {
 					listTempSelectedServers = new ArrayList<String>(
 							Arrays.asList(selectedServers.split("#SEP#")));
 				}
 				//TODO:Need to handle
 //				technology.setServers(ApplicationsUtil.getSelectedServers(servers, listTempSelectedServers));
 			}
 			
 			if (selectedDatabases != null) {
 				List<String> listTempSelectedDatabases = null;
 				if (StringUtils.isNotEmpty(selectedDatabases)) {
 					listTempSelectedDatabases = new ArrayList<String>(
 							Arrays.asList(selectedDatabases.split("#SEP#")));
 				}
 				//TODO:Need to handle
 //				technology.setDatabases(ApplicationsUtil.getSelectedDatabases(databases, listTempSelectedDatabases));
 			}
 			//TODO:Need to handle
 //			if (selectedWebservices != null) {
 //				technology.setWebservices(ApplicationsUtil.getSelectedWebservices(
 //						webservices, ApplicationsUtil.getArrayListFromStrArr(selectedWebservices)));
 //			}
 			
 			if (getHttpRequest().getParameter(REQ_EMAIL_SUPPORTED) != null) {
 				isEmailSupported = Boolean.parseBoolean(getHttpRequest().getParameter(REQ_EMAIL_SUPPORTED));
 			}
 //			technology.setEmailSupported(isEmailSupported);//TODO:Need to handle
 
 		} else {
 		  //TODO:Need to handle
 //			if (tempprojectInfo != null) {
 //				technology.setServers(tempprojectInfo.getTechnology().getServers());
 //				technology.setDatabases(tempprojectInfo.getTechnology().getDatabases());
 //				technology.setWebservices(tempprojectInfo.getTechnology().getWebservices());
 //				technology.setEmailSupported(tempprojectInfo.getTechnology().isEmailSupported());
 //			}
 		}
 		
 //		appInfo.setTechnology(technology);//TODO:Need to handle
 	}
 
     private boolean validate(ProjectAdministrator administrator) throws PhrescoException {
     	isValidated = true;
     	if (StringUtils.isEmpty(name)) {
     		setNameError(ERROR_NAME);
             return true;
         }
     	if (StringUtils.isEmpty(name.trim())) {
     		setNameError(ERROR_INVALID_NAME);
             return true;
         }
         if (administrator.getProject(code) != null) {
         	setNameError(ERROR_DUPLICATE_NAME);
             return true;
         }
         return false;
     }
     
 	public void setFeaturesInRequest(ProjectAdministrator administrator,
 			ApplicationInfo appInfo) throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.setFeaturesInRequest()");
 		}
 		//TODO:Need to handle
 		Technology selectedTechnology = appInfo.getTechnology();
 		ApplicationType applicationType = administrator
 				.getApplicationType(appInfo.getApplication());
 		Technology techonology = applicationType
 				.getTechonology(selectedTechnology.getId());
 
 		getHttpRequest().setAttribute(REQ_ALL_JS_LIBS,
 				techonology.getJsLibraries());
 		List<ModuleGroup> coreModule = (List<ModuleGroup>) administrator
 				.getCoreModules(techonology);
 		List<ModuleGroup> customModule = (List<ModuleGroup>) administrator
 				.getCustomModules(techonology);
 		if (CollectionUtils.isNotEmpty(coreModule)) {
 			getHttpRequest().setAttribute(REQ_CORE_MODULES, coreModule);
 		}
 
 		if (CollectionUtils.isNotEmpty(customModule)) {
 			getHttpRequest().setAttribute(REQ_CUSTOM_MODULES, customModule);
 		}
 
 		// This attribute for Pilot Project combo box
 		getHttpRequest().setAttribute(REQ_PILOTS_NAMES,
 				ApplicationsUtil.getPilotNames(selectedTechnology.getId()));
 
 		if (CollectionUtils.isNotEmpty(selectedTechnology.getModules())) {
 			// pilotModules.putAll(ApplicationsUtil.getMapFromModuleGroups(selectedTechnology.getModules()));
 			getHttpRequest().setAttribute(
 					REQ_ALREADY_SELECTED_MODULES,
 					ApplicationsUtil.getMapFromModuleGroups(selectedTechnology
 							.getModules()));
 		}
 
 		if (CollectionUtils.isNotEmpty(selectedTechnology.getJsLibraries())) {
 			getHttpRequest().setAttribute(
 					REQ_ALREADY_SELECTED_JSLIBS,
 					ApplicationsUtil.getMapFromModuleGroups(selectedTechnology
 							.getJsLibraries()));
 			// pilotJsLibs.putAll(ApplicationsUtil.getMapFromModuleGroups(selectedTechnology.getJsLibraries()));
 		}
 
 		getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 	}
 
 	public String getPilotProjectModules() {
 		String techId = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 		pilotModules = new ArrayList<String>(ApplicationsUtil
 				.getPilotModuleIds(techId).keySet());
 		pilotJSLibs = new ArrayList<String>(ApplicationsUtil.getPilotJsLibIds(
 				techId).keySet());
 		pilotModules.addAll(pilotJSLibs);
 		return SUCCESS;
 	}
 	
 	public String fetchDefaultModules() {
 		String techId = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 		try {
 			defaultModules = new ArrayList<String>();
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			//TODO:Need to handle
 			Technology technology = administrator.getTechnology(techId);
 			List<ModuleGroup> coreModules = (List<ModuleGroup>) administrator.getCoreModules(technology);
 			if (CollectionUtils.isNotEmpty(coreModules) && coreModules != null) {
 				for (ModuleGroup coreModule : coreModules) {
 					if (coreModule.isRequired()) {
 						defaultModules.add(coreModule.getId());
 					}
 				}
 			}
 			List<ModuleGroup> customModules = (List<ModuleGroup>) administrator.getCustomModules(technology);
 			if (CollectionUtils.isNotEmpty(customModules) && customModules != null) {
 				for (ModuleGroup customModule : customModules) {
 					if (customModule.isRequired()) {
 						defaultModules.add(customModule.getId());
 					}
 				}
 			}
 			
 			List<ModuleGroup> jsLibraries = technology.getJsLibraries();
 			if (CollectionUtils.isNotEmpty(jsLibraries) && jsLibraries != null) {
 				for (ModuleGroup jsLibrary : jsLibraries) {
 					if (jsLibrary.isRequired()) {
 						defaultModules.add(jsLibrary.getId());
 					}
 				}
 			}
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of fetchDefaultModules()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature fetchDefaultModules");
 		}
 		
 		return SUCCESS;
 	}
 
 	public String checkDependency() {
 		try {
 		  //TODO:Need to handle
 			List<ModuleGroup> allModules = getAllModule();
 
 			for (ModuleGroup module : allModules) {
 				if (module.getId().equals(moduleId)) {
 					Module checkedVersion = module.getVersion(version);
 					if (StringUtils.isNotEmpty(preVersion)) {
 						Module preVerModule = module.getVersion(preVersion);
 						preDependentIds = ApplicationsUtil.getIds(preVerModule
 								.getDependentModules());
 						preDependentVersions = ApplicationsUtil
 								.getDependentVersions();
 					}
 					if (checkedVersion != null) {
 						dependentIds = ApplicationsUtil.getIds(checkedVersion
 								.getDependentModules());
 						dependentVersions = ApplicationsUtil
 								.getDependentVersions();
 					}
 				}
 			}
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Features.checkDependency()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature Select Dependency");
 		}
 		return SUCCESS;
 	}
 
 	public List<ArtifactGroup> getAllModule() throws PhrescoException {
 		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 		//TODO:Need to handle
 //		Technology technology = administrator.getTechnology(techId);
 //		if (REQ_CORE_MODULE.equals(moduleType)) {
 //			return administrator.getCoreModules(technology);
 //		}
 //
 //		if (REQ_CUSTOM_MODULE.equals(moduleType)) {
 //			return administrator.getCustomModules(technology);
 //		}
 //
 //		if (REQ_JSLIB_MODULE.equals(moduleType)) {
 //			return technology.getJsLibraries();
 //		}
 
 		return null;
 	}*/
 
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
 }
