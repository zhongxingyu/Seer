 /*
  * ###
  * Service Web Archive
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
 package com.photon.phresco.service.admin.actions.components;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.util.ServerUtil;
 import com.phresco.pom.site.Reports;
 
 
 
 public class Archetypes extends ServiceBaseAction { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Archetypes.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	/* plugin and archetype jar InputStream Map*/
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	
 	//Plugin jar artifact info map
 	private static Map<String, ArtifactGroup> pluginArtfactInfoMap = new HashMap<String, ArtifactGroup>();
 
 	private static byte[] archetypeJarByteArray = null;
 	private static List<ArtifactGroup> pluginInfos = new ArrayList<ArtifactGroup>();
 	
 	private List<TechnologyGroup> techGroups = new ArrayList<TechnologyGroup>();
 	
 	private String name = "";
 	private String nameError = "";
 	private String version = "";
 	
 	private String verError = "";
 	private String techvernError = "";
 	private String apptype = "";
 	private String appError = "";
 	private String fileError = "";
 	private String applicableErr = "";
 	private boolean errorFound = false;
 	private String oldName = "";
 
 	private String description = "";
 	private String fromPage = "";
 	private String techId = "";
     private String customerId = "";
 	
 	private String versionComment = "";
 	private String techVersion = "";
 	private String techGroup = "";
 	
 	private String jarVersion = "";
 	private String groupId = "";
 	private String artifactId = "";
 	private String uploadPlugin = "";
 	private String archArchetypeId = "";
 	private String archGroupId = "";
 	private String archVersions = "";
 	private List<String> applicable = null;
 	private List<String> applicableReports = null;
 	private boolean archType = false;
 	private String versioning = "";
 	private boolean tempError = false;
 	private String ArchetypeUrl = "";
 	private String extFileName = "";
 	private InputStream fileInputStream;
 	private String contentType = "";
 	private int contentLength;
 	private String removeTechGroup = "";
 	private List<TechnologyGroup> appTypeTechGroups = new ArrayList<TechnologyGroup>();
 	private static String versionFile= "";
 	
 	private byte[] newtempApplnByteArray = null;
 	
 	public String list() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.list()");
 		}
 
 		try {
 			List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
 			List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_ARCHE_TYPES, technologies);
 			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_LIST));
 		}
 		
 		/* To clear all static variables after successfull create or update */
 		inputStreamMap.clear();
 		pluginArtfactInfoMap.clear();
 		archetypeJarByteArray = null;
 
 		return COMP_ARCHETYPE_LIST;
 	}
 
 	public String add() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.add()");
 	    }
 
 		try {
 			ServiceManager serviceManager = getServiceManager();
 			List<ApplicationType> appTypes = serviceManager.getApplicationTypes(getCustomerId());
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			List<TechnologyOptions> options = serviceManager.getOptions();
 			List<Reports> reports = serviceManager.getReports();
 			setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 			setReqAttribute(REQ_FROM_PAGE, ADD);
 			setReqAttribute(REQ_TECHNOLOGY_REPORTS, reports);
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_ADD));
 		}
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.edit()");
 		}
 
 		try {
 		    ServiceManager serviceManager = getServiceManager();
 		    versionFile = getVersioning();
 			Technology technology = serviceManager.getArcheType(getTechId(), getCustomerId());
             List<ApplicationType> appTypes = serviceManager.getApplicationTypes(getCustomerId());
             List<TechnologyOptions> options = serviceManager.getOptions();
             setReqAttribute(REQ_ARCHE_TYPE,  technology);
             setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
 			List<Reports> reports = serviceManager.getReports();
 			setReqAttribute(REQ_TECHNOLOGY_REPORTS, reports);
             setReqAttribute(REQ_VERSIONING, getVersioning()); 
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_EDIT));
 		}
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.save()");
 		}
 
 		try {
 			Technology technology = createTechnology();
 			//save application jar files
 			if(archetypeJarByteArray != null){
 				inputStreamMap.put(technology.getName(),  new ByteArrayInputStream(archetypeJarByteArray));
 			} 
 			getServiceManager().createArcheTypes(technology, inputStreamMap, getCustomerId());
 			addActionMessage(getText(ARCHETYPE_ADDED, Collections.singletonList(name)));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_SAVE));
 		} 
 		return list();
 	}
 	
 	public void createPluginInfo() {
 		String key = "";
 		if (MapUtils.isNotEmpty(inputStreamMap)) {
 			Iterator iter = inputStreamMap.keySet().iterator();
 			while (iter.hasNext()) {
 				key = (String) iter.next();
 				artifactId = getReqParameter(key + "_artifactId");
 				groupId = getReqParameter(key + "_groupId");
 				version = getReqParameter(key + "_version");
 				ArtifactGroup pluginInfo = new ArtifactGroup();
 				pluginInfo.setArtifactId(getArtifactId());
 				pluginInfo.setGroupId(getGroupId());
 
 				List<ArtifactInfo> artifactVersions = new ArrayList<ArtifactInfo>();
 				ArtifactInfo artifactVersion = new ArtifactInfo();
 				artifactVersion.setVersion(getVersion());
 				artifactVersions.add(artifactVersion);
 				pluginInfo.setVersions(artifactVersions);
 				List<String> customerIds = new ArrayList<String>();
 				customerIds.add(getCustomerId());
 				pluginInfo.setCustomerIds(customerIds);
 				pluginInfo.setName(key);
 				int pos = name.lastIndexOf('.');
 				String ext = key.substring(pos+1);
 				pluginInfo.setPackaging(ext);
 				pluginInfos.add(pluginInfo);
 			}
 		}
 	}
 
 	public String update() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.update()");
 		}
 		
 		try {
 			Technology technology = createTechnology();
 			//update application jar files
 			if (archetypeJarByteArray != null) {
 				inputStreamMap.put(technology.getName(),  new ByteArrayInputStream(archetypeJarByteArray));
 			} 
 			getServiceManager().updateArcheType(technology, inputStreamMap, getCustomerId());
 			addActionMessage(getText(ARCHETYPE_UPDATED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_UPDATE));
 		}
 
 		return list();
 	}
 
     /**
      * @return
      * @throws PhrescoException
      */
     private Technology createTechnology() throws PhrescoException {
     	if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.getTechnology()");
 		}
     	
     	String artifactId = getArtifactId();
     	String groupId = getGroupId();
     	String version = getVersion();
         Technology technology = new Technology();
         if (StringUtils.isNotEmpty(getTechId())) {
         	technology.setId(getTechId());
         }
         technology.setName(getName());
         technology.setDescription(getDescription());
         technology.setAppTypeId(getApptype());
         technology.setTechGroupId(getTechGroup());
         
         //To set the applicable features
         List<String> options = new ArrayList<String>();
         for (String selectedOption : getApplicable()) {
         	options.add(selectedOption);
 		}
         technology.setOptions(options);
         //To create the ArtifactGroup with groupId, artifactId and version for archetype jar
         if ((StringUtils.isEmpty(artifactId) && StringUtils.isEmpty(groupId) && StringUtils.isEmpty(version))) {
         	artifactId = getArchArchetypeId();
         	groupId = getArchGroupId();
         	version = getArchVersions();
         }
         if (StringUtils.isNotEmpty(artifactId) && StringUtils.isNotEmpty(groupId) && StringUtils.isNotEmpty(version)) {
 	        ArtifactGroup archetypeArtfGroup = getArtifactGroupInfo(getName(), artifactId, groupId, REQ_JAR_FILE, version, getCustomerId());
 	        technology.setArchetypeInfo(archetypeArtfGroup);
         } else {
         	throw new PhrescoException(getText(EXCEPTION_ARTIFACTINFO_MISSING));
         }
         technology.setCustomerIds(Arrays.asList(getCustomerId()));
        
        String[] techVersions = getTechVersion().split(",");
        List<String> listTechVersion = Arrays.asList(techVersions);
        technology.setTechVersions(listTechVersion);
         technology.setReports(getApplicableReports());
         technology.setPlugins(pluginInfos);
         
         
         return technology;
     }
 
     /**
 	 * @param artifactGroupInfo 
 	 * @return
 	 */
 	public void createPluginInfos(ArtifactGroup artifactGroupInfo, String jarName) {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.createPluginInfos(ArtifactGroup artifactGroupInfo, String jarName)");
 		}
 		
 		if (inputStreamMap != null) {
 		    ArtifactGroup pluginInfo = new ArtifactGroup();
 		    pluginInfo.setName(jarName);
 		    pluginInfo.setArtifactId(artifactGroupInfo.getArtifactId());
 		    pluginInfo.setGroupId(artifactGroupInfo.getGroupId());
 		    pluginInfo.setVersions(artifactGroupInfo.getVersions());
 		    pluginInfo.setCustomerIds(Arrays.asList(getCustomerId()));
 		    pluginArtfactInfoMap.put(jarName, pluginInfo);
 		}
 	}
 
 	public String delete() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.delete()");
 		}
 
 		try {
 			String[] techTypeIds = getReqParameterValues(REQ_ARCHE_TECHID);
 			if (ArrayUtils.isNotEmpty(techTypeIds)) {
                 ServiceManager serviceManager = getServiceManager();
 				for (String techid : techTypeIds) {
                     serviceManager.deleteArcheType(techid, getCustomerId());
 				}
 				addActionMessage(getText(ARCHETYPE_DELETED));
 			}
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_DELETE));
 		}
 
 		return list();
 	}
 	
 	public String uploadJar() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.uploadAppJar()");
 		}
 		
 		PrintWriter writer = null;
 		try {
             writer = getHttpResponse().getWriter();
 	        byte[] tempApplnByteArray = getByteArray();
 	        
 	        if (isArchType()) {
 	        	uploadArchetypeJar(writer, tempApplnByteArray);
 	        } else {
 	        	uploadPluginJar(writer, tempApplnByteArray);
 	        }
 	        writer.flush();
 	        writer.close();
 		} catch (Exception e) { //If upload fails it will be shown in UI, so need not to throw error popup
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 		}
 		
 		return SUCCESS;
 	}
 
 	/**
 	 * @param writer
 	 * @param tempApplnByteArray
 	 * @throws PhrescoException
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	private void uploadPluginJar(PrintWriter writer, byte[] tempApplnByteArray) throws PhrescoException {
 		try {
 			String pluginJarName = getFileName();
 			byte[] byteArray = tempApplnByteArray;
 		    getArtifactGroupInfo(writer, tempApplnByteArray);
 		    if(!inputStreamMap.containsKey(pluginJarName)){
 		    	inputStreamMap.put(pluginJarName, new ByteArrayInputStream(byteArray));
 		    }
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * @param writer
 	 * @param tempApplnByteArray
 	 * @throws PhrescoException
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	private void uploadArchetypeJar(PrintWriter writer, byte[] tempApplnByteArray) throws PhrescoException {
 		if (tempApplnByteArray == null) {
 			this.newtempApplnByteArray = new byte[0];
 		} else {
 			this.newtempApplnByteArray = Arrays.copyOf(tempApplnByteArray, tempApplnByteArray.length);
 		}
 		boolean isArchetypeJar = ServerUtil.validateArchetypeJar(new ByteArrayInputStream(newtempApplnByteArray));
 		if (isArchetypeJar) {
 			archetypeJarByteArray = newtempApplnByteArray;
 			getArtifactGroupInfo(writer, newtempApplnByteArray);
 		} else {
 			archetypeJarByteArray = null;
 			writer.print(INVALID_ARCHETYPE_JAR);
 		}
 	}
 	
 	public String downloadArchetype() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Archetypes.downloadArchetype()");
 		}
 
 		try {
 			Technology technology = (Technology) getServiceManager().getArcheType(getTechId(), getCustomerId());
 			ArtifactGroup archetypeInfo = technology.getArchetypeInfo();
 			ArchetypeUrl = archetypeInfo.getVersions().get(0).getDownloadURL();
 
 			URL url = new URL(ArchetypeUrl);
 			fileInputStream = url.openStream();
 			String[] parts = ArchetypeUrl.split(FORWARD_SLASH);
 			extFileName = parts[parts.length - 1];
 			contentType = url.openConnection().getContentType();
 			contentLength = url.openConnection().getContentLength();
 		} catch (Exception e) {
 			return showErrorPopup(new PhrescoException(e), getText(DOWNLOAD_FAILED));
 		}
 
 		return SUCCESS;
 	}
 
 	public String showPluginJarPopup() {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.showPluginJarPopup()");
 	    }
 		
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		
 		return uploadPlugin;
 	}
 	
 	public void removeUploadedJar() {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.removeUploadedJar()");
 	    }
 		
 		String type = getReqParameter(REQ_JAR_TYPE);
 		if (REQ_PLUGIN_JAR.equals(type)) {
 			String uploadedFileName = getReqParameter((REQ_UPLOADED_JAR));
 			inputStreamMap.remove(uploadedFileName);
 			pluginArtfactInfoMap.remove(uploadedFileName);
 		} else {
 			archetypeJarByteArray = null;
 		}
 	}
 	
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.validateForm()");
 		}
 		
 		boolean isError = false;
 		isError = nameValidation(isError);
 
 		isError = versionValidation(isError);
 
 		isError = appTypeValidation(isError);
 		
 		isError = archJarValidation(isError);
 		
 		//Empty validation for applicable features
 		isError = featureValidation(isError);
 		
 		if (isError) {
             setErrorFound(true);
         }
 		
 		return SUCCESS;
 	}
 	
 	public String getTechnologyGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.getTechnologyGroup()");
 		}
 
 		List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 		for (ApplicationType appType : appTypes) {
 			if (appType.getId().equals(getApptype())) {
 				setAppTypeTechGroups(appType.getTechGroups());
 				return SUCCESS;
 			}
 		}
 
 		return SUCCESS;
 	}
 	
 	public String showTechGroupPopup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.openTechGroup()");
 		}
 		
 		List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 		setReqAttribute(REQ_APP_TYPES, appTypes);
 		
 		return REQ_TECH_GROUP;
 	}
 	
 	public String createTechGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.createTechGroup()");
 		}
 
 		List<TechnologyGroup> technologyGroups = new ArrayList<TechnologyGroup>();
 		if (CollectionUtils.isNotEmpty(getTechGroups())) {
 			for (TechnologyGroup groups : getTechGroups()) {
 				technologyGroups.add(groups);
 			}
 		}
 		getServiceManager().createTechnologyGroups(technologyGroups, getCustomerId());
 
 		return list();
 	}
 	
 	public String deleteTechnologyGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.deleteTechGroup()");
 		}
 		
 		getServiceManager().deleteTechnologyGroups(getRemoveTechGroup(), getCustomerId());
 		
 		return TECHGROUP_LIST;
 	}
 
 	private boolean featureValidation(boolean isError) {
 		if (CollectionUtils.isEmpty(getApplicable())) {
 			setApplicableErr(getText(KEY_I18N_ERR_APPLICABLE_EMPTY ));
 			tempError = true;
 		}
 		
 		return tempError;
 	}
 
 	private boolean nameValidation(boolean isError) throws PhrescoException {
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
 			tempError = true;
 		} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
 			// To check whether the name already exist (Application type wise)
 			List<Technology> archetypes = getServiceManager().getArcheTypes(getCustomerId());
 			if (CollectionUtils.isNotEmpty(archetypes)) {
 				for (Technology archetype : archetypes) {
 					if (archetype.getAppTypeId().equals(getApptype()) && archetype.getName().equalsIgnoreCase(getName())) {
 						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST_APPTYPE));
 						tempError = true;
 						break;
 					}
 				}
 			}
 		}
 		
 		return tempError;
 	}
 
 	private boolean archJarValidation(boolean isError) {
 		if ((!EDIT.equals(getFromPage())&& archetypeJarByteArray == null) || (StringUtils.isNotEmpty(versionFile)&& archetypeJarByteArray == null)) {
 			setFileError(getText(KEY_I18N_ERR_ARCHETYPEJAR_EMPTY));
 			tempError = true;
 		}
 		
 		return tempError;
 	}
 
 	private boolean appTypeValidation(boolean isError) {
 		if (StringUtils.isEmpty(getApptype())) {
 			setAppError(getText(KEY_I18N_ERR_APPTYPE_EMPTY));
 			tempError = true;
 		}
 		
 		return tempError;
 	}
 
 	private boolean versionValidation(boolean isError) {
 		if (StringUtils.isEmpty(getTechVersion())) {
 			setTechvernError(getText(KEY_I18N_ERR_TECHVER_EMPTY));
 			tempError = true;
 		}
 		
 		return tempError;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getVerError() {
 		return verError;
 	}
 
 	public void setVerError(String verError) {
 		this.verError = verError;
 	}
 
 	public String getApptype() {
 		return apptype;
 	}
 
 	public void setApptype(String apptype) {
 		this.apptype = apptype;
 	}
 
 	public String getAppError() {
 		return appError;
 	}
 
 	public void setAppError(String appError) {
 		this.appError = appError;
 	}
 
 	public String getFileError() {
 		return fileError;
 	}
 
 	public void setFileError(String fileError) {
 		this.fileError = fileError;
 	} 
 
 	public String getApplicableErr() {
 		return applicableErr;
 	}
 
 	public void setApplicableErr(String applicableErr) {
 		this.applicableErr = applicableErr;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getVersionComment() {
 		return versionComment;
 	}
 
 	public void setVersionComment(String versionComment) {
 		this.versionComment = versionComment;
 	}
 
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 	
 	public String getJarVersion() {
 		return jarVersion;
 	}
 
 	public void setJarVersion(String jarVersion) {
 		this.jarVersion = jarVersion;
 	}
 
 	public String getGroupId() {
 		return groupId;
 	}
 
 	public void setGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 
 	public String getArtifactId() {
 		return artifactId;
 	}
 
 	public void setArtifactId(String artifactId) {
 		this.artifactId = artifactId;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 
 	public String getTechVersion() {
 		return techVersion;
 	}
 
 	public void setTechVersion(String techVersion) {
 		this.techVersion = techVersion;
 	}
 
 	public String getTechvernError() {
 		return techvernError;
 	}
 
 	public void setTechvernError(String techvernError) {
 		this.techvernError = techvernError;
 	}
 
 	public String getUploadPlugin() {
 		return uploadPlugin;
 	}
 
 	public void setUploadPlugin(String uploadPlugin) {
 		this.uploadPlugin = uploadPlugin;
 	}
 
 	public void setArchType(boolean archType) {
 		this.archType = archType;
 	}
 
 	public boolean isArchType() {
 		return archType;
 	}
 
 	public void setApplicable(List<String> applicable) {
 		this.applicable = applicable;
 	}
 
 	public List<String> getApplicable() {
 		return applicable;
 	}
 
 	public void setApplicableReports(List<String> applicableReports) {
 		this.applicableReports = applicableReports;
 	}
 
 	public List<String> getApplicableReports() {
 		return applicableReports;
 	}
 	
     public String getVersioning() {
 		return versioning;
 	}
 
 	public void setVersioning(String versioning) {
 		this.versioning = versioning;
 	} 
 	
 	public String getTechGroup() {
 		return techGroup;
 	}
 
 	public void setTechGroup(String techGroup) {
 		this.techGroup = techGroup;
 	}
 
 
 	public List<TechnologyGroup> getAppTypeTechGroups() {
 		return appTypeTechGroups;
 	}
 
 	public void setAppTypeTechGroups(List<TechnologyGroup> appTypeTechGroup) {
 		this.appTypeTechGroups = appTypeTechGroup;
 	}
 	
 	public List<TechnologyGroup> getTechGroups() {
 		return techGroups;
 	}
 
 	public void setTechGroups(List<TechnologyGroup> techGroups) {
 		this.techGroups = techGroups;
 	}
 
 	public String getArchetypeUrl() {
 		return ArchetypeUrl;
 	}
 
 	public void setArchetypeUrl(String archetypeUrl) {
 		ArchetypeUrl = archetypeUrl;
 	}
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
 	}
 
 	public String getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	public int getContentLength() {
 		return contentLength;
 	}
 
 	public void setContentLength(int contentLength) {
 		this.contentLength = contentLength;
 	}
 
 	public String getExtFileName() {
 		return extFileName;
 	}
 
 	public void setExtFileName(String extFileName) {
 		this.extFileName = extFileName;
 	}
 
 	public String getRemoveTechGroup() {
 		return removeTechGroup;
 	}
 
 	public void setRemoveTechGroup(String removeTechGroup) {
 		this.removeTechGroup = removeTechGroup;
 	}
 
 	public String getArchArchetypeId() {
 		return archArchetypeId;
 	}
 
 	public void setArchArchetypeId(String archArchetypeId) {
 		this.archArchetypeId = archArchetypeId;
 	}
 
 	public String getArchGroupId() {
 		return archGroupId;
 	}
 
 	public void setArchGroupId(String archGroupId) {
 		this.archGroupId = archGroupId;
 	}
 
 	public String getArchVersions() {
 		return archVersions;
 	}
 
 	public void setArchVersions(String archVersions) {
 		this.archVersions = archVersions;
 	}
 }
