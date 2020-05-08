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
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.Content;
 import com.photon.phresco.service.client.api.ServiceManager;
 
 public class PilotProjects extends ServiceBaseAction { 
 	
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(PilotProjects.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	
 	private String name = "";
 	private String description = "";
     private String version = "";
     private String groupId = "";
     private String artifactId = "";
     private String jarVersion = "";
     
 	private String nameError = "";
 	private String artifactIdError = "";
     private String groupIdError = "";
 	private String verError = "";
 	private String jarVerError = "";
 
 	private String fileError = "";
 	private boolean errorFound = false;
 	
 	private String projectId = "";
 	private String fromPage = "";
 	
 	private String customerId = "";
 	
 	private String techId = "";
 	private String oldName = "";
 	private String versioning = "";
 	private static byte[] pilotProByteArray = null;
 	private boolean tempError = false;
 	
 	 /**
      * To get all Pilot Projects form DB
      * @return List of Pilot Projects
      * @throws PhrescoException
      */
 	public String list() throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method PilotProjects.list()");
         }
 
 		try {
 			List<ApplicationInfo> pilotProjects = getServiceManager().getPilotProjects(getCustomerId());
 			setReqAttribute(REQ_PILOT_PROJECTS, pilotProjects);
 			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_LIST));
 		}
 		
 		//to clear file input stream and byte array
 		inputStreamMap.clear();
 		pilotProByteArray = null;
 		
 		return COMP_PILOTPROJ_LIST;
 	}
 	
 	 /**
      * To return to the page to add Pilot projects
      * @return 
      * @throws PhrescoException
      */
     public String add() throws PhrescoException {
     	if (isDebugEnabled) {	
     		S_LOGGER.debug("Entering Method PilotProjects.add()");
     	}
     	
     	try {
     		List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
     		setReqAttribute(REQ_ARCHE_TYPES, technologies);
     		setReqAttribute(REQ_FROM_PAGE, ADD);
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_ADD));
 		}
     	
     	return COMP_PILOTPROJ_ADD;
     }
 	
     /**
 	 * To return the edit page with the details of the selected Pilot Projects
 	 * @return
 	 * @throws PhrescoException
 	 */
     public String edit() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method PilotProjects.edit()");
     	}
     	try {
     		ServiceManager serviceManager = getServiceManager();
 			ApplicationInfo applicationInfo = serviceManager.getPilotProject(getProjectId(), getCustomerId());
     		setReqAttribute(REQ_PILOT_PROINFO, applicationInfo);
     		List<Technology> technologies = serviceManager.getArcheTypes(getCustomerId());
     		setReqAttribute(REQ_ARCHE_TYPES, technologies);
     		setReqAttribute(REQ_FROM_PAGE, EDIT);
     		setReqAttribute(REQ_VERSIONING, getVersioning());
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_EDIT));
 		}
 
     	return COMP_PILOTPROJ_ADD;
     }
     
     /**
 	 * To create a pilot projects with the provided details
 	 * @return List of pilot projects
 	 * @throws PhrescoException
 	 */
     public String save() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method PilotProjects.save()");
     	}
     	
     	try {
             ApplicationInfo pilotProInfo = createPilotProj();     		
     		//save pilot project jar files
 			if(pilotProByteArray != null){
 				inputStreamMap.put(pilotProInfo.getName(),  new ByteArrayInputStream(pilotProByteArray));
 			} 
     		
     		getServiceManager().createPilotProjects(createPilotProj(), inputStreamMap, getCustomerId());
 			addActionMessage(getText(PLTPROJ_ADDED, Collections.singletonList(getName())));
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_SAVE));
 		}
 
     	return list();
     }
     
     /**
 	 * To update the pilot projects with the provided details
 	 * @return List of pilot projects
 	 * @throws PhrescoException
 	 */
     public String update() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  PilotProjects.update()");
     	}
     	try {
     		ApplicationInfo pilotProInfo = createPilotProj();
     		//update pilot project jar files
     		if(pilotProByteArray != null){
     			inputStreamMap.put(pilotProInfo.getName(),  new ByteArrayInputStream(pilotProByteArray));
     		} 
     		getServiceManager().updatePilotProject(createPilotProj(), inputStreamMap, getProjectId(), getCustomerId());
     		addActionMessage(getText(PLTPROJ_UPDATED, Collections.singletonList(getName())));
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_UPDATE));
 		}
 
     	return list();
     }
     
     private ApplicationInfo createPilotProj() throws PhrescoException {
         ApplicationInfo pilotProInfo = new ApplicationInfo();
        pilotProInfo.setPilot(true);
         if (StringUtils.isNotEmpty(getProjectId())) { 
         	pilotProInfo.setId(getProjectId());
         }
         List<String> customerIds = new ArrayList<String>();
         customerIds.add(getCustomerId());
         
         pilotProInfo.setName(getName());
         pilotProInfo.setDescription(getDescription());
         pilotProInfo.setVersion(getVersion());
         
         ArtifactGroup pilotContent = new ArtifactGroup();
        pilotContent.setName(getName());
         pilotContent.setGroupId(getGroupId());
         pilotContent.setArtifactId(getArtifactId());
         pilotContent.setPackaging(Content.Type.ZIP.name());
         List<ArtifactInfo> jarVersions = new ArrayList<ArtifactInfo>();
         ArtifactInfo jarversion = new ArtifactInfo();
        jarversion.setName(getName());
        jarversion.setVersion(getJarVersion());
         jarVersions.add(jarversion);
         pilotContent.setVersions(jarVersions);
         pilotContent.setCustomerIds(customerIds);
        
         pilotProInfo.setCustomerIds(customerIds);
         pilotProInfo.setPilotContent(pilotContent);
         
         TechnologyInfo techInfo = new TechnologyInfo();
        techInfo.setName(getTechId());
         techInfo.setVersion(getTechId());
         pilotProInfo.setTechInfo(techInfo);
         
         return pilotProInfo;
     }
 	
     /**
 	 * To delete selected pilot projects 
 	 * @return List of pilot projects
 	 * @throws PhrescoException
 	 */
     public String delete() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method PilotProjects.delete()");
     	}
     	
     	try {
     		String[] projectIds = getHttpRequest().getParameterValues(REQ_PILOT_PROJ_ID);
     		if (ArrayUtils.isNotEmpty(projectIds)) {
     			for (String projectid : projectIds) {
     				getServiceManager().deletePilotProject(projectid, getCustomerId());
     			}
     			addActionMessage(getText(PLTPROJ_DELETED));
     		}
     	}catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_PILOT_PROJECTS_DELETE));
 		}
 
     	return list();
     }
     
     /**
 	 * To upload file
 	 * @return
 	 * @throws PhrescoException
 	 */
     public String uploadFile() throws PhrescoException {
     	if (isDebugEnabled) {
     	S_LOGGER.debug("Entering Method PilotProjects.uploadFile()");
     	}
 
     	PrintWriter writer = null;
     	try {
     		writer = getHttpResponse().getWriter();
     		InputStream is = getHttpRequest().getInputStream();
     		pilotProByteArray = IOUtils.toByteArray(is);
     		getHttpResponse().setStatus(getHttpResponse().SC_OK);
     		writer.print(SUCCESS_TRUE);
     		writer.flush();
     		writer.close();
     	} catch (Exception e) {
     		getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
     		writer.print(SUCCESS_FALSE);
     		throw new PhrescoException(e);
     	}
 
     	return SUCCESS;
     }
 	
     /**
 	 * To remove uploaded file
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public void removeUploadedFile() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  PilotProjects.removeUploadedFile()");
 		}
 		
 		pilotProByteArray = null;
 	}
 	
     public String validateForm() throws PhrescoException {
     	if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  PilotProjects.validateForm()");
 		}
     	boolean isError = false;
     	//Empty validation for name
     	isError = nameValidation(isError);
     	
     	//empty validation for version
     	isError = versionValidation(isError);
     	
     	//empty validation for fileupload
     	isError = fileuploadValidation(isError);
     	
     	//Empty Validation for pilot Project
     	isError = pilotProjectValidation(isError);
 
 
     	if (isError) {
     		setErrorFound(true);
     	}
 		
     	return SUCCESS;
     }
 
 	public boolean pilotProjectValidation(boolean isError) {
 		if (pilotProByteArray != null) {
     		//Empty validation for groupId if file is selected
     		if (StringUtils.isEmpty(getGroupId())) {
     			setGroupIdError(getText(KEY_I18N_ERR_GROUPID_EMPTY));
     			tempError = true;
     		}
 
     		//Empty validation for artifactId if file is selected
     		if (StringUtils.isEmpty(getArtifactId())) {
     			setArtifactIdError(getText(KEY_I18N_ERR_ARTIFACTID_EMPTY));
     			tempError = true;
     		}
 
     		//Empty validation for version if file is selected
     		if (StringUtils.isEmpty(getJarVersion())) {
     			setJarVerError(getText(KEY_I18N_ERR_VER_EMPTY));
     			tempError = true;
     		}
     	}
 		return tempError;
 	}
 
 	public boolean fileuploadValidation(boolean isError) {
 		if (pilotProByteArray == null) {
     		setFileError(getText(KEY_I18N_ERR_PLTPROJ_EMPTY));
     		tempError = true;
     	}
 		return tempError;
 	}
 
 	public boolean versionValidation(boolean isError) {
 		if (StringUtils.isEmpty(getVersion())) {
     		setVerError(getText(KEY_I18N_ERR_VER_EMPTY ));
     		tempError = true;
     	}
 		return tempError;
 	}
 
 	public boolean nameValidation(boolean isError) throws PhrescoException {
 		if (StringUtils.isEmpty(getName())) {
     		setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
     		tempError = true;
     	} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
     		//To check whether the name already exist (Technology wise)
 			List<ApplicationInfo> pilotProjInfos = getServiceManager().getPilotProjects(getCustomerId());
 			if (pilotProjInfos != null) {
 				for (ApplicationInfo pilotProjectInfo : pilotProjInfos) {
 					if (pilotProjectInfo.getTechInfo().getVersion().equals(getTechId()) && pilotProjectInfo.getName().equalsIgnoreCase(getName())) {
 						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST_TECH));
 						tempError = true;
 			    		break;
 					} 
 				}	
 			}
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
 	
 	public String getFileError() {
 		return fileError;
 	}
 
 	public void setFileError(String fileError) {
 		this.fileError = fileError;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public String getProjectId() {
 		return projectId;
 	}
 
 	public void setProjectId(String projectId) {
 		this.projectId = projectId;
 	}
 	
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
     
     public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 	
 	public String getArtifactId() {
 		return artifactId;
 	}
 
 	public void setArtifactId(String artifactId) {
 		this.artifactId = artifactId;
 	}
 	
 	public String getGroupId() {
 		return groupId;
 	}
 
 	public void setGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 	
 	public String getJarVersion() {
 		return jarVersion;
 	}
 
 	public void setJarVersion(String jarVersion) {
 		this.jarVersion = jarVersion;
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
 
 	public String getArtifactIdError() {
 		return artifactIdError;
 	}
 
 	public void setArtifactIdError(String artifactIdError) {
 		this.artifactIdError = artifactIdError;
 	}
 
 	public String getGroupIdError() {
 		return groupIdError;
 	}
 
 	public void setGroupIdError(String groupIdError) {
 		this.groupIdError = groupIdError;
 	}
 	
 	public String getJarVerError() {
 		return jarVerError;
 	}
 
 	public void setJarVerError(String jarVerError) {
 		this.jarVerError = jarVerError;
 	}
     
     public String getVersioning() {
 		return versioning;
 	}
     
     public void setVersioning(String versioning) {
 		this.versioning = versioning;
 	} 
 }
