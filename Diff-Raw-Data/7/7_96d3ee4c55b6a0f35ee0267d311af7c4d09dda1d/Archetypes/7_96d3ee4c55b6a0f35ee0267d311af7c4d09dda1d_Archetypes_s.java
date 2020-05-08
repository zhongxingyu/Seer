 /**
  * Service Web Archive
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
 import java.util.Set;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.FunctionalFramework;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.util.ServerUtil;
 import com.phresco.pom.site.Reports;
 
 
 
 public class Archetypes extends ServiceBaseAction { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final SplunkLogger S_LOGGER = SplunkLogger.getSplunkLogger(Archetypes.class.getName());
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
 	private String funcFrameworksError = "";
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
 	private List<String> functionalFramework = null;
 	private List<String> applicableReports = null;
 	private List<String> applicableAtchetypeFeatures = null;
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
 	        S_LOGGER.debug("Archetypes.list : Entry");
 	    }
 
 		try {
 			if (isDebugEnabled) {
 				if (StringUtils.isEmpty(getCustomerId())) {
 					S_LOGGER.warn("Archetypes.list", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
 					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_ARCHETYPE_LIST));
 				}
 				S_LOGGER.info("Archetypes.list", "customerId=" + "\"" + getCustomerId() + "\"");
 			}
 			List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
 			List<ApplicationType> appTypes = getServiceManager().getApplicationTypes();
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_ARCHE_TYPES, technologies);
 			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.list", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_LIST));
 		}
 		
 		/* To clear all static variables after successfull create or update */
 		inputStreamMap.clear();
 		pluginArtfactInfoMap.clear();
 		archetypeJarByteArray = null;
 		pluginInfos.clear();
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.list : Exit");
 	    }
 
 		return COMP_ARCHETYPE_LIST;
 	}
 
 	public String add() throws PhrescoException {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.add : Entry");
 	    }
 
 		try {
 			ServiceManager serviceManager = getServiceManager();
 			List<ApplicationType> appTypes = serviceManager.getApplicationTypes();
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			List<TechnologyOptions> options = serviceManager.getOptions();
 			List<Reports> reports = serviceManager.getReports();
 			setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 			setReqAttribute(REQ_FROM_PAGE, ADD);
 			setReqAttribute(REQ_TECHNOLOGY_REPORTS, reports);
 			setReqAttribute(REQ_ARCHE_TYPES, serviceManager.getTechnologyByCustomer(getCustomerId()));
 			setReqAttribute(REQ_FUNCTIONAL_FRAMEWORKS, serviceManager.getFunctionalTestFramework());
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.add", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_ADD));
 		}
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.add : Exit");
 	    }
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.edit : Entry");
 	    }
 
 		try {
 		    ServiceManager serviceManager = getServiceManager();
 		    versionFile = getVersioning();
 		    if (isDebugEnabled) {
 				if (StringUtils.isEmpty(getTechId())) {
 					S_LOGGER.warn("Archetypes.edit", "status=\"Bad Request\"", "message=\"Technology Id is empty\"");
 					return showErrorPopup(new PhrescoException("Technology Id is empty"), getText(EXCEPTION_ARCHETYPE_EDIT));
 				}
 				if (StringUtils.isEmpty(getCustomerId())) {
 					S_LOGGER.warn("Archetypes.list", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
 					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_ARCHETYPE_EDIT));
 				}
 				S_LOGGER.info("Archetypes.edit", "customerId=" + "\"" + getCustomerId() + "\"", "techId=" + "\"" + getTechId() + "\"");
 			}
 			Technology technology = serviceManager.getArcheType(getTechId(), getCustomerId());
 			List<FunctionalFramework> functionalFrameworks = null;
 			if (CollectionUtils.isNotEmpty(functionalFrameworks)) {
 				List<String> selectedFrameworkIds = new ArrayList<String>();
 				for (FunctionalFramework functionalFramework : functionalFrameworks) {
 					selectedFrameworkIds.add(functionalFramework.getId());
 				}
 				setReqAttribute(REQ_SELECTED_FUNCTIONAL_FRAMEWORKS, selectedFrameworkIds);
 			}
             List<ApplicationType> appTypes = serviceManager.getApplicationTypes();
             List<TechnologyOptions> options = serviceManager.getOptions();
             setReqAttribute(REQ_ARCHE_TYPE,  technology);
             setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
 			List<Reports> reports = serviceManager.getReports();
 			setReqAttribute(REQ_TECHNOLOGY_REPORTS, reports);
             setReqAttribute(REQ_VERSIONING, getVersioning()); 
             setReqAttribute(REQ_ARCHE_TYPES, getServiceManager().getTechnologyByCustomer(getCustomerId()));
             setReqAttribute(REQ_FUNCTIONAL_FRAMEWORKS, serviceManager.getFunctionalTestFramework());
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.edit", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_EDIT));
 		}
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.edit : Exit");
 	    }
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.save : Entry");
 	    }
 
 		try {
 			if (isDebugEnabled) {
 				if (StringUtils.isEmpty(getCustomerId())) {
 					S_LOGGER.warn("Archetypes.save", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
 					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_ARCHETYPE_SAVE));
 				}
 				S_LOGGER.info("Archetypes.save", "customerId=" + "\"" + getCustomerId() + "\"");
 			}
 			Technology technology = createTechnology();
 			//save application jar files
 			if(archetypeJarByteArray != null){
 				inputStreamMap.put(technology.getName(),  new ByteArrayInputStream(archetypeJarByteArray));
 			}
 			getServiceManager().createArcheTypes(technology, inputStreamMap, getCustomerId());
 			addActionMessage(getText(ARCHETYPE_ADDED, Collections.singletonList(name)));
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.save", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_SAVE));
 		}
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.save : Exit");
 	    }
 		
 		return list();
 	}
 	
 	public String createPluginInfo() {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.createPluginInfo : Entry");
 	    }
 		try {
 			pluginInfos.clear();
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
 					//pluginInfos.add(pluginInfo);
 					pluginArtfactInfoMap.put(key, pluginInfo);
 				}
 			}
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.createPluginInfo", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_ARCHETYPE_SAVE));
 		}
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.createPluginInfo : Exit");
 	    }
 		
 		return null;
 	}
 
 	public String update() throws PhrescoException {
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.update : Entry");
 	    }
 		
 		try {
 			if (isDebugEnabled) {
 				if (StringUtils.isEmpty(getCustomerId())) {
 					S_LOGGER.warn("Archetypes.update", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
 					return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_ARCHETYPE_UPDATE));
 				}
 				if (StringUtils.isEmpty(getTechId())) {
 					S_LOGGER.warn("Archetypes.update", "status=\"Bad Request\"", "message=\"Technology Id is empty\"");
 					return showErrorPopup(new PhrescoException("Technology Id is empty"), getText(EXCEPTION_ARCHETYPE_UPDATE));
 				}
 				S_LOGGER.info("Archetypes.save", "customerId=" + "\"" + getCustomerId() + "\"", "techId=" + "\"" + getTechId() + "\"");
 			}
 			Technology technology = createTechnology();
 			//update application jar files
 			if (archetypeJarByteArray != null) {
 				inputStreamMap.put(technology.getName(),  new ByteArrayInputStream(archetypeJarByteArray));
 			} 
 			getServiceManager().updateArcheType(technology, inputStreamMap, getCustomerId());
 			addActionMessage(getText(ARCHETYPE_UPDATED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.update", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_UPDATE));
 		}
 		if (isDebugEnabled) {
 	        S_LOGGER.debug("Archetypes.update : Exit");
 	    }
 
 		return list();
 	}
 
     /**
      * @return
      * @throws PhrescoException
      */
 	private Technology createTechnology() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.createTechnology : Entry");
 		}
 		Technology technology = new Technology();
 		try {
 			ServiceManager serviceManager = getServiceManager();
 			String artifactId = getArtifactId();
 			String groupId = getGroupId();
 			String version = getVersion();
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
 
 			List<FunctionalFramework> functionalFrameworks = new ArrayList<FunctionalFramework>();
			List<FunctionalFramework> functionalTestFramework = serviceManager.getFunctionalTestFramework();
 			for (String funcFramework : getFunctionalFramework()) {
				for (FunctionalFramework functionalFramework : functionalTestFramework) {
 					if(functionalFramework.getName().equals(funcFramework)) {
 						FunctionalFramework e = new FunctionalFramework();
 						e.setId(functionalFramework.getId());
 						functionalFrameworks.add(e);
 					}
 				}
 			}
 			technology.setFunctionalFrameworks(null);
 
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
 			Set<String> keySet = pluginArtfactInfoMap.keySet();
 			for (String key : keySet) {
 				ArtifactGroup artifactGroup = pluginArtfactInfoMap.get(key);
 				pluginInfos.add(artifactGroup);
 			}
 			technology.setPlugins(pluginInfos);
 			technology.setArchetypeFeatures(getApplicableAtchetypeFeatures());
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.createTechnology", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw e;
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.createTechnology : Exit");
 		}
 		
 		return technology;
 	}
 
     /**
 	 * @param artifactGroupInfo 
 	 * @return
 	 */
 	/*public void createPluginInfos(ArtifactGroup artifactGroupInfo, String jarName) {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.createTechnology : Entry");
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
 	}*/
 
 	public String delete() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.delete : Entry");
 		}
 
 		try {
 			String[] techTypeIds = getReqParameterValues(REQ_ARCHE_TECHID);
 			if (isDebugEnabled) {
 				if (ArrayUtils.isEmpty(techTypeIds)) {
 					S_LOGGER.warn("Archetypes.delete", "status=\"Bad Request\"", "message=\"No Archetype Ids found to delete\"");
 					return showErrorPopup(new PhrescoException("No Applicaation Type Ids found to delete"), getText(EXCEPTION_ARCHETYPE_DELETE));
 				}
 			}
 			if (ArrayUtils.isNotEmpty(techTypeIds)) {
 				if (isDebugEnabled) {
 					S_LOGGER.info("Archetypes.delete", "techTypeIds=" + "\"" + techTypeIds.toString() + "\"");
 				}
                 ServiceManager serviceManager = getServiceManager();
 				for (String techid : techTypeIds) {
                     serviceManager.deleteArcheType(techid, getCustomerId());
 				}
 				addActionMessage(getText(ARCHETYPE_DELETED));
 			}
 		} catch (PhrescoException e) {
 			if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.delete", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 		    return showErrorPopup(e, getText(EXCEPTION_ARCHETYPE_DELETE));
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.delete : Exit");
 		}
 
 		return list();
 	}
 	
 	public String uploadJar() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadJar : Entry");
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
             if (isDebugEnabled) {
 		        S_LOGGER.error("Archetypes.uploadJar", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadJar : Exit");
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
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadPluginJar : Entry");
 		}
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
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadPluginJar : Exit");
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
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadArchetypeJar : Entry");
 		}
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
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.uploadArchetypeJar : Exit");
 		}
 	}
 	
 	public String downloadArchetype() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.downloadArchetype : Entry");
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
 			if (isDebugEnabled) {
 				S_LOGGER.error("Archetypes.downloadArchetype", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(new PhrescoException(e), getText(DOWNLOAD_FAILED));
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.downloadArchetype : Exit");
 		}
 
 		return SUCCESS;
 	}
 
 	public String showPluginJarPopup() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.showPluginJarPopup : Entry");
 		}
 		setReqAttribute(REQ_PLUGIN_INFO, pluginArtfactInfoMap);
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.showPluginJarPopup : Exit");
 		}
 		
 		return uploadPlugin;
 	}
 	
 	public void removeUploadedJar() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.removeUploadedJar : Entry");
 		}
 		
 		String type = getReqParameter(REQ_JAR_TYPE);
 		if (REQ_PLUGIN_JAR.equals(type)) {
 			String uploadedFileName = getReqParameter((REQ_UPLOADED_JAR));
 			inputStreamMap.remove(uploadedFileName);
 			pluginArtfactInfoMap.remove(uploadedFileName);
 		} else {
 			archetypeJarByteArray = null;
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.removeUploadedJar : Exit");
 		}
 	}
 	
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.validateForm : Entry");
 		}
 		
 		boolean isError = false;
 		isError = nameValidation(isError);
 
 		isError = versionValidation(isError);
 
 		isError = appTypeValidation(isError);
 		
 		isError = archJarValidation(isError);
 		
 		//Empty validation for applicable features
 		isError = featureValidation(isError);
 		
 		isError = functioanlFrameworkValidation(isError);
 		
 		if (isError) {
             setErrorFound(true);
         }
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.validateForm : Exit");
 		}
 		
 		return SUCCESS;
 	}
 	
 	public String getTechnologyGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.getTechnologyGroup : Entry");
 		}
 		
 		if (isDebugEnabled) {
 			if (StringUtils.isEmpty(getApptype())) {
 				S_LOGGER.warn("Archetypes.getTechnologyGroup", "status=\"Bad Request\"", "message=\"Application type is empty\"");
 				return showErrorPopup(new PhrescoException("Application type is empty"), getText(EXCEPTION_APPTYPES_TECH_GROUP));
 			}
 		}
 		List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 		for (ApplicationType appType : appTypes) {
 			if (appType.getId().equals(getApptype())) {
 				setAppTypeTechGroups(appType.getTechGroups());
 				return SUCCESS;
 			}
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.getTechnologyGroup : Exit");
 		}
 
 		return SUCCESS;
 	}
 	
 	public String showTechGroupPopup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.showTechGroupPopup : Entry");
 		}
 		
 		List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 		setReqAttribute(REQ_APP_TYPES, appTypes);
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.showTechGroupPopup : Exit");
 		}
 		
 		return REQ_TECH_GROUP;
 	}
 	
 	public String createTechGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.createTechGroup : Entry");
 		}
 		
 		try {
 			List<TechnologyGroup> technologyGroups = new ArrayList<TechnologyGroup>();
 			List<String> customerIds = new ArrayList<String>();
 			if (CollectionUtils.isNotEmpty(getTechGroups()) && StringUtils.isNotEmpty(getCustomerId())) {
 				for (TechnologyGroup groups : getTechGroups()) {
 					customerIds.add(getCustomerId());
 					groups.setCustomerIds(customerIds);
 					technologyGroups.add(groups);
 				}
 			}
 			getServiceManager().createTechnologyGroups(technologyGroups, getCustomerId());
 			addActionMessage(getText(TECH_GROUP_UPDATED));
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 				S_LOGGER.error("Archetypes.createTechGroup", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_APPTYPES_TECH_GROUP_CREATE));
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.createTechGroup : Exit");
 		}
 
 		return list();
 	}
 	
 	public String deleteTechnologyGroup() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.deleteTechnologyGroup : Entry");
 		}
 		if (isDebugEnabled) {
 			if (StringUtils.isEmpty(getRemoveTechGroup())) {
 				S_LOGGER.warn("Archetypes.deleteTechnologyGroup", "status=\"Bad Request\"", "message=\"Customer Id is empty\"");
 				return showErrorPopup(new PhrescoException("Customer Id is empty"), getText(EXCEPTION_ARCHETYPE_UPDATE));
 			}
 			S_LOGGER.info("Archetypes.createTechGroup", "customerId=" + "\"" + getCustomerId() + "\"");
 		}
 		getServiceManager().deleteTechnologyGroups(getRemoveTechGroup(), getCustomerId());			
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Archetypes.deleteTechnologyGroup : Exit");
 		}
 		
 		return TECHGROUP_LIST;
 	}
 
 	private boolean featureValidation(boolean isError) {
 		if (CollectionUtils.isEmpty(getApplicable())) {
 			setApplicableErr(getText(KEY_I18N_ERR_APPLICABLE_EMPTY ));
 			tempError = true;
 		}
 		
 		return tempError;
 	}
 
 	private boolean functioanlFrameworkValidation(boolean isError) {
 		if (CollectionUtils.isNotEmpty(getApplicable()) && getApplicable().contains("Functional_Test") && CollectionUtils.isEmpty(getFunctionalFramework())) {
 			setFuncFrameworksError(getText(KEY_I18N_ERR_FUNCTIONAL_FRAMEWORK_EMPTY));
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
 
 	public void setApplicableAtchetypeFeatures(
 			List<String> applicableAtchetypeFeatures) {
 		this.applicableAtchetypeFeatures = applicableAtchetypeFeatures;
 	}
 
 	public List<String> getApplicableAtchetypeFeatures() {
 		return applicableAtchetypeFeatures;
 	}
 
 	public List<String> getFunctionalFramework() {
 		return functionalFramework;
 	}
 
 	public void setFunctionalFramework(List<String> functionalFramework) {
 		this.functionalFramework = functionalFramework;
 	}
 
 	public String getFuncFrameworksError() {
 		return funcFrameworksError;
 	}
 
 	public void setFuncFrameworksError(String funcFrameworksError) {
 		this.funcFrameworksError = funcFrameworksError;
 	}
 }
