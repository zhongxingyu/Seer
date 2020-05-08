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
 package com.photon.phresco.framework.rest.api;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.quartz.CronExpression;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.api.NonEnvConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CertificateInfo;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.commons.FileBrowseInfo;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.AddCertificateInfo;
 import com.photon.phresco.framework.model.CronExpressionInfo;
 import com.photon.phresco.framework.model.RemoteCertificateInfo;
 import com.photon.phresco.framework.model.TemplateInfo;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.impl.NonEnvConfigManagerImpl;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class ConfigurationService.
  */
 @Path("/configuration")
 public class ConfigurationService extends RestBase implements FrameworkConstants, ServiceConstants, ResponseCodes {
 	
 	/** The Constant S_LOGGER. */
 	private static final Logger S_LOGGER = Logger.getLogger(ConfigurationService.class);
 	
 	/** The is_debug enabled. */
 	private static Boolean is_debugEnabled = S_LOGGER.isDebugEnabled();
 	
 	/**
 	 * Adds the environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param environments the environments
 	 * @return the response
 	 */
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response addEnvironment(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, List<Environment> environments) {
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			String configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			if(configManager.getEnvironments().size() == environments.size()) {
 				configManager.addEnvironments(environments);
 				ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 						environments, RESPONSE_STATUS_SUCCESS, PHR600020);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			configManager.addEnvironments(environments);
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600001);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610001);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 	/**
 	 * List environments.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @return the response
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response listEnvironments(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName, @QueryParam("isEnvSpecific") String isEnvSpecific, @QueryParam("configName") String configName) {
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			if (StringUtils.isNotEmpty(isEnvSpecific) && isEnvSpecific.equals("false")) {
 				String nonEnvConfigFile = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 				NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFile));
 				Configuration configurations = nonConfigManager.getConfiguration(configName);
 				ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, null,
 						configurations, RESPONSE_STATUS_SUCCESS, PHR600002);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			String configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			if (StringUtils.isNotEmpty(envName)) {
 				List<Environment> environments = configManager.getEnvironments(Arrays.asList(envName));
 				if (CollectionUtils.isNotEmpty(environments)) {
 					Environment environment = environments.get(0);
 					ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 							environment, RESPONSE_STATUS_SUCCESS, PHR600002);
 					return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 				} else {
 					ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
							null, RESPONSE_STATUS_ERROR, PHR610025);
 					return Response.status(Status.OK).entity(finalOuptut).header(
 							"Access-Control-Allow-Origin", "*").build();
 				}
 			}
 			List<Environment> environments = configManager.getEnvironmentsAlone();
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610002);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610026);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 	/**
 	 * Gets the all environments.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the all environments
 	 */
 	@GET
 	@Path("allEnvironments")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getAllEnvironments(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam("isEnvSpecific") String isEnvSpecific, @QueryParam("configType") String configType) {
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			if (StringUtils.isNotEmpty(isEnvSpecific) && isEnvSpecific.equals("false")) {
 				String nonEnvConfigFile = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 				NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFile));
 				List<Configuration> configurations = nonConfigManager.getConfigurations(configType);
 				ResponseInfo<List<Configuration>> finalOuptut = responseDataEvaluation(responseData, null,
 						configurations, RESPONSE_STATUS_SUCCESS, PHR600002);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			String configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			List<Environment> environments = configManager.getEnvironments();
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610002);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<Configuration>> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610027);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 
 	/**
 	 * Delete environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @return the response
 	 */
 	@DELETE
 	@Path("/deleteEnv")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteEnv(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName) {
 		String configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 			List<Environment> environments = configManager.getEnvironments(Arrays.asList(envName));
 			if (CollectionUtils.isNotEmpty(environments)) {
 				Environment environment = environments.get(0);
 				if (environment.isDefaultEnv()) {
 					ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 							null, RESPONSE_STATUS_FAILURE, PHR610003);
 					return Response.status(Status.OK).entity(finalOuptut).header(
 							"Access-Control-Allow-Origin", "*").build();
 				}
 				configManager.deleteEnvironment(envName);
 				ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_SUCCESS, PHR600003);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610004);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 		ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
				null, RESPONSE_STATUS_ERROR, PHR610005);
 		return Response.status(Status.OK).entity(finalOuptut)
 				.header("Access-Control-Allow-Origin", "*").build();
 	}
 	
 	
 	@DELETE
 	@Path("/deleteConfig")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteConfiguraion(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam("configName") String configName) {
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			String nonEnvConfigFie = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 			NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFie));
 			nonConfigManager.deleteConfiguration(configName);
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_SUCCESS, PHR600014);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_ERROR, PHR610023);
 			return Response.status(Status.OK).entity(finalOuptut)
 					.header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 	/**
 	 * Gets the settings template.
 	 *
 	 * @param appDirName the app dir name
 	 * @param techId the tech id
 	 * @param userId the user id
 	 * @param type the type
 	 * @return the settings template
 	 */
 	@GET
 	@Path("/settingsTemplate")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getSettingsTemplate(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_TECHID) String techId, @QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_TYPE) String type) {
 		ResponseInfo<List<SettingsTemplate>> responseData = new ResponseInfo<List<SettingsTemplate>>();
 		Map<String, Object> templateMap = new HashMap<String, Object>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_FAILURE, PHR610006);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 			SettingsTemplate settingsTemplate = serviceManager.getConfigTemplateByTechId(techId, type);
 			Map<String, List<String>> downloadInfo = getDownloadInfo(serviceManager, appDirName, userId, type);
 			templateMap.put("settingsTemplate", settingsTemplate);
 			templateMap.put("downloadInfo", downloadInfo);
 			ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, null,
 					templateMap, RESPONSE_STATUS_SUCCESS, PHR600004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Gets the config types.
 	 *
 	 * @param customerId the customer id
 	 * @param userId the user id
 	 * @param techId the tech id
 	 * @return the config types
 	 */
 	@GET
 	@Path("/types")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getConfigTypes(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_TECHID) String techId) {
 		ResponseInfo<List<TemplateInfo>> responseData = new ResponseInfo<List<TemplateInfo>>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_FAILURE, PHR610006);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 			List<TemplateInfo> settingsTypes = new ArrayList<TemplateInfo>();
 			List<SettingsTemplate> settingsTemplates = serviceManager.getConfigTemplates(customerId, techId);
 			if (CollectionUtils.isNotEmpty(settingsTemplates)) {
 				for (SettingsTemplate settingsTemplate : settingsTemplates) {
 					TemplateInfo template = new TemplateInfo();
 					template.setTemplateName(settingsTemplate.getName());
 					template.setFavourite(settingsTemplate.isFavourite());
 					template.setEnvSpecific(settingsTemplate.isEnvSpecific());
 					settingsTypes.add(template);
 				}
 			}
 			ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, null,
 					settingsTypes, RESPONSE_STATUS_SUCCESS, PHR600004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Connection alive check.
 	 *
 	 * @param url the url
 	 * @return the response
 	 */
 	@GET
 	@Path("/connectionAliveCheck")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response connectionAliveCheck(@QueryParam(REST_QUERY_URL) String url) {
 		ResponseInfo<Boolean> responseData = new ResponseInfo<Boolean>();
 		if (is_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Configurationservice.connectionAliveCheck()");
 		}
 
 		if (url == null || ("".equals(url)) == true) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_FAILURE, PHR610008);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 
 		boolean connection_status = false;
 		try {
 			String[] results = url.split(",");
 			String lprotocol = results[0];
 			String lhost = results[1];
 			int lport = Integer.parseInt(results[2]);
 			boolean tempConnectionAlive = isConnectionAlive(lprotocol, lhost, lport);
 			connection_status = tempConnectionAlive == true ? true : false;
 		} catch (Exception e) {
			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_ERROR, PHR610009);
 			S_LOGGER.error("Entered into catch block of Configurationservice.connectionAliveCheck()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 		ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null,
 				connection_status, RESPONSE_STATUS_SUCCESS, PHR600005);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Update configuration.
 	 *
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @param configurationlist the configurationlist
 	 * @return the response
 	 */
 	@POST
 	@Path("/updateConfig")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response updateConfiguration(@QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_ENV_NAME) String envName,
 			List<Configuration> configurationlist, @QueryParam("isEnvSpecific") String isEnvSpecific, @QueryParam("configName") String configName) {
 		if (is_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Configurationservice.updateConfiguration()");
 		}
 
 		String configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		ResponseInfo<Configuration> responseData = new ResponseInfo<Configuration>();
 		try {
 			if (StringUtils.isEmpty(isEnvSpecific)) {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 			List<Configuration> listofconfiguration = configManager.getConfigurations(envName);
 			List<String> configuration_names = new ArrayList<String>();
 			String validateConfiguration = validateConfiguration(userId, customerId, appDirName, configurationlist);
 			if(StringUtils.isNotEmpty(validateConfiguration)) {
 				ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
     					validateConfiguration, RESPONSE_STATUS_FAILURE, PHR610024);
     			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			for (Configuration configuration_temp : listofconfiguration) {
 
 				configuration_names.add(configuration_temp.getName());
 			}
 				configManager.deleteConfigurations(envName, configuration_names);
 	    		configManager.createConfiguration(envName, configurationlist);
 	    	} else {
 	    		String nonEnvConfigFie = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 	    		NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFie));
 	    		if(StringUtils.isEmpty(configName)) {
 	    			nonConfigManager.createConfiguration(configurationlist.get(0));
 	    			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 	    					"Success", RESPONSE_STATUS_SUCCESS, PHR600015);
 	    			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 	    		} else {
 	    		nonConfigManager.updateConfiguration(configName, configurationlist.get(0));
 	    		}
 	    	}
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 					"Success", RESPONSE_STATUS_SUCCESS, PHR600006);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			S_LOGGER.error("Entered into Configuration catch block of Configurationservice.updateConfiguration()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					"Failure", RESPONSE_STATUS_ERROR, PHR610010);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into PhrescoException catch block of Configurationservice.updateConfiguration()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e, "Failure", RESPONSE_STATUS_ERROR, PHR610010);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Configurationservice.updateConfiguration()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					"Failure", RESPONSE_STATUS_ERROR, PHR610010);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 
 	}
 
 	/**
 	 * Clone environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @param cloneEnvironment the clone environment
 	 * @return the response
 	 */
 	@POST
 	@Path("/cloneEnvironment")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response cloneEnvironment(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName, Environment cloneEnvironment) {
 
 		String configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		Environment clonedEnvironment = null;
 		ResponseInfo<Configuration> responseData = new ResponseInfo<Configuration>();
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 			clonedEnvironment = configManager.cloneEnvironment(envName, cloneEnvironment);
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 					clonedEnvironment, RESPONSE_STATUS_SUCCESS, PHR600007);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (ConfigurationException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					clonedEnvironment, RESPONSE_STATUS_ERROR, PHR610029);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 
 		} catch (PhrescoException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					clonedEnvironment, RESPONSE_STATUS_ERROR, PHR610011);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 
 		} catch (Exception e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					clonedEnvironment, RESPONSE_STATUS_ERROR, PHR610011);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 	/**
 	 * Cron expression for scheduler.
 	 *
 	 * @param cronExpInfo the cron exp info
 	 * @return the response
 	 */
 	@POST
 	@Path("/cronExpression")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response cronValidation(CronExpressionInfo cronExpInfo) {
 		ResponseInfo<CronExpressionInfo> responseData = new ResponseInfo<CronExpressionInfo>();
 		CronExpressionInfo cronResult = new CronExpressionInfo();
 		try {
 			String cronBy = cronExpInfo.getCronBy();
 			String cronExpression = "";
 			Date[] dates = null;
 			List<String> datesList = new ArrayList<String>();
 
 			if (REQ_CRON_BY_DAILY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				String every = cronExpInfo.getEvery();
 
 				if ("false".equals(every)) {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " 0 * * ?";
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + hours + " * * ?";
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " " + hours + " * * ?";
 					}
 				} else {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + "*/" + minutes + " * * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + "*/" + hours + " * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " */" + hours + " * * ?"; // 0 replace with *
 					}
 				}
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_WEEKLY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				List<String> week = cronExpInfo.getWeek();
 				String csv = week.toString().replace("[", "").replace("]", "")
 	            .replace(", ", ",");
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " ? * " + csv;
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_MONTHLY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				List<String> month = cronExpInfo.getMonth();
 				String csv = month.toString().replace("[", "").replace("]", "")
 	            .replace(", ", ",");
 				String day = cronExpInfo.getDay();
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " " + day + " " + csv + " ?";
 				dates = testCronExpression(cronExpression);
 			}
 
 			if (dates != null) {
 				cronExpression = cronExpression.replace('?', '*');
 				cronExpression = cronExpression.substring(2);
 				for (int i = 0; i < dates.length; i++) {
 					datesList.add(dates[i].toString());
 				}
 				cronResult.setDates(datesList);
 				
 			}
 			cronResult.setCronExpression(cronExpression);
 			ResponseInfo<CronExpressionInfo> finalOutput = responseDataEvaluation(responseData, null,
 					cronResult, RESPONSE_STATUS_SUCCESS, PHR600008);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (PhrescoException e) {
 			ResponseInfo<CronExpressionInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610012);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * List environments by project id.
 	 *
 	 * @param customerId the customer id
 	 * @param projectId the project id
 	 * @return the response
 	 */
 	@GET
 	@Path("/listEnvironmentsByProjectId")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response listEnvironmentsByProjectId(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			List<ApplicationInfo> appInfos = FrameworkServiceUtil.getAppInfos(customerId, projectId);
 			Set<String> environmentSet = new HashSet<String>();
 			for (ApplicationInfo appInfo : appInfos) {
 				List<Environment> environments = getEnvironments(appInfo);
 				for (Environment environment : environments) {
 					environmentSet.add(environment.getName());
 				}
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					environmentSet, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610013);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610028);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	
 	@GET
 	@Path("/environmentList")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getEnvironmentList(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			String configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			List<Environment> environments = configManager.getEnvironments();
 			Set<String> environmentSet = new HashSet<String>();
 			for (Environment environment : environments) {
 				environmentSet.add(environment.getName());
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					environmentSet, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610028);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * Authenticate server.
 	 *
 	 * @param host the host
 	 * @param port the port
 	 * @param appDirName the app dir name
 	 * @return the response
 	 * @throws PhrescoException the phresco exception
 	 */
 	@GET
 	@Path("/returnCertificate")
 	@Produces(MediaType.APPLICATION_JSON)
 	 public Response authenticateServer(@QueryParam("host") String host, @QueryParam("port") String port, @QueryParam("appDirName") String appDirName) throws PhrescoException {
 		ResponseInfo<RemoteCertificateInfo> responseData = new ResponseInfo<RemoteCertificateInfo>();
 	    		RemoteCertificateInfo remoteCertificateInfo = new RemoteCertificateInfo();
 	    		int portValue = Integer.parseInt(port);
 	    		boolean connctionAlive = Utility.isConnectionAlive("https", host, portValue);
 	    		boolean isCertificateAvailable = false;
 	    		String projectLocation = "";
     			projectLocation = Utility.getProjectHome() + appDirName;
 	    		if (connctionAlive) {
 	    			List<CertificateInfo> certificates = FrameworkServiceUtil.getCertificate(host, portValue);
 	    			if (CollectionUtils.isNotEmpty(certificates)) {
 	    				isCertificateAvailable = true;
 	    				remoteCertificateInfo.setCertificates(certificates);
 	    				remoteCertificateInfo.setProjectLocation(projectLocation);
 	    				remoteCertificateInfo.setCertificateAvailable(isCertificateAvailable);
 	    				ResponseInfo<RemoteCertificateInfo> finalOutput = responseDataEvaluation(responseData, null,
 	    						remoteCertificateInfo, RESPONSE_STATUS_SUCCESS, PHR600009);
 	    				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	    			}
 	    		}
 	    		ResponseInfo<RemoteCertificateInfo> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_SUCCESS, PHR600016);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	    }
 
 	@POST
 	@Path("/addCertificate")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response addCertificate(AddCertificateInfo addCertificateInfo) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		String certificatePath = "";
 		try {
 			String propValue = addCertificateInfo.getPropValue();
 			String fromPage = addCertificateInfo.getFromPage();
 			String appDirName = addCertificateInfo.getAppDirName();
 			if (StringUtils.isNotEmpty(propValue)) {
 				File file = new File(propValue);
 				if (fromPage.equals(CONFIGURATION)) {
 					certificatePath = configCertificateSave(propValue, file, appDirName, addCertificateInfo);
 				} else if (fromPage.equals(SETTINGS)) {
 					certificatePath = settingsCertificateSave(file, appDirName, addCertificateInfo);
 				}
 				ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 						certificatePath, RESPONSE_STATUS_SUCCESS, PHR600010);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR600017);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610016);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	@POST
 	@Path("/upload")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
 	public Response fileUpload(@Context HttpServletRequest request) {
 		ResponseInfo<Boolean> responseData = new ResponseInfo<Boolean>();
 		try {
 			InputStream inputStream = request.getInputStream();
 			String actionType = request.getHeader("actionType");
 			String appDirName = request.getHeader("appDirName");
 			if (actionType.equals("configuration")) {
 				File tempZipFile = new File(Utility.getProjectHome() + appDirName + File.separator 
 						+ FrameworkConstants.DO_NOT_CHECKIN_DIR + File.separator + FrameworkConstants.TARGET_DIR
 					+ File.separator + request.getHeader("X-File-Name"));
 			
 			 	if (!tempZipFile.getParentFile().exists()) {
 					tempZipFile.getParentFile().mkdirs();
 			} 
 				if (!tempZipFile.exists()) {
 					tempZipFile.createNewFile();
 				}
 				OutputStream out = new FileOutputStream(tempZipFile);
 				int read = 0;
 				byte[] bytes = new byte[1024];
 				while ((read = inputStream.read(bytes)) != -1) {
 					out.write(bytes, 0, read);
 				}
 				inputStream.close();
 				out.flush();
 				out.close();
 				ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, true, RESPONSE_STATUS_SUCCESS, PHR600011);
 				return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 		} catch (FileNotFoundException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e, false, RESPONSE_STATUS_ERROR, PHR610030);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (IOException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e, false, RESPONSE_STATUS_ERROR, PHR610017);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 		ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, false, RESPONSE_STATUS_FAILURE, PHR610018);
 		return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	@GET
 	@Path("/fileBrowseFolder")
 	@Produces(MediaType.APPLICATION_XML)
 	public Response returnFileBorwseFolderStructure(@QueryParam("browsePath") String browsePath) {
 		ResponseInfo<DOMSource> responseData = new ResponseInfo<DOMSource>();
 		try {
 			List<FileBrowseInfo> browseList = new ArrayList<FileBrowseInfo>();
 			File browseFile = new File(browsePath);
 			File[] files = browseFile.listFiles();
 			if (files == null) {
 				ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR600019);
 				return Response.status(Status.OK).entity(finalOuptut).header(
 						"Access-Control-Allow-Origin", "*").build();
 			}
 			for (int i = 0; i < files.length; i++) {
 				FileBrowseInfo fileBrowse = new FileBrowseInfo();
 				if (files[i].isDirectory()) {
 					fileBrowse.setName(files[i].getName());
 					fileBrowse.setPath(files[i].getPath());
 					fileBrowse.setType("Folder");
 					browseList.add(fileBrowse);
 				} else {
 					fileBrowse.setName(files[i].getName());
 					fileBrowse.setPath(files[i].getPath());
 					fileBrowse.setType("File");
 					browseList.add(fileBrowse);
 				}
 			}
 			DOMSource outputContent = constructXml(browseFile.getName(), browsePath.toString(), browseList);
 			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, outputContent, RESPONSE_STATUS_SUCCESS, PHR600012);
 			return Response.status(Status.OK).entity(outputContent).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610020);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 
 	}
 	
 	@GET
 	@Path("/fileBrowse")
 	@Produces(MediaType.APPLICATION_XML)
 	public Response returnFileBorwseEntireStructure(@QueryParam("appDirName") String appDirName , @QueryParam("fileType") String fileType) {
 		ResponseInfo<DOMSource> responseData = new ResponseInfo<DOMSource>();
 		try {
 			String browsePath = Utility.getProjectHome()  + appDirName;
 			DOMSource outputContent = createXML(browsePath, fileType);
 			if(outputContent == null) {
 				ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_FAILURE, PHR610021);
 				return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, outputContent, RESPONSE_STATUS_SUCCESS, PHR600013);
 			return Response.status(Status.OK).entity(outputContent).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR, PHR610022);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();	
 		}
 
 	}
 
 	// for the return of entire project structure as single xml
 	private static DOMSource createXML(String browsePath, String fileType) throws PhrescoException {
 		try {
 			File inputPath = new File(browsePath);
 			if (inputPath.isFile()) {
 				return null;
 			}
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.newDocument();
 			Element rootElement = document.createElement("root");
 			document.appendChild(rootElement);
 
 			Element mainFolder = document.createElement("Item");
 			mainFolder.setAttribute("name", inputPath.getName());
 			mainFolder.setAttribute("path", inputPath.toString());
 			mainFolder.setAttribute("type", "Folder");
 			rootElement.appendChild(mainFolder);
 
 			listDirectories(mainFolder, document, inputPath, fileType);
 
 			DOMSource source = new DOMSource(document);
 			return source;
 		} catch (DOMException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static void listDirectories(Element rootElement, Document document, File dir, String fileType) {
 		for (File childFile : dir.listFiles()) {
 			if (childFile.isDirectory()) {
 				Element childElement = document.createElement("Item");
 				childElement.setAttribute("name", childFile.getName());
 				childElement.setAttribute("path", childFile.getPath());
 				childElement.setAttribute("type", "Folder");
 				rootElement.appendChild(childElement);
 
 				listDirectories(childElement, document, childFile.getAbsoluteFile(), fileType);
 			} else { 
 				String fileName = childFile.getName();
 				 fileName = fileName.substring(fileName.lastIndexOf('.')+1,fileName.length());
 				if(StringUtils.isEmpty(fileType) || fileName.equals(fileType)) {
 				Element childElement = document.createElement("Item");
 				childElement.setAttribute("name", childFile.getName());
 				childElement.setAttribute("path", childFile.getPath());
 				childElement.setAttribute("type", "File");
 				rootElement.appendChild(childElement);
 				}
 			}
 		}
 	}
 	
 	// for the return of project structure on individual folders
 	private DOMSource constructXml(String name, String path, List<FileBrowseInfo> browseList) throws PhrescoException {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 			// root elements
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("root");
 			doc.appendChild(rootElement);
 
 			// Main folder
 			Element item = doc.createElement("item");
 			rootElement.appendChild(item);
 
 			item.setAttribute("type", "folder");
 			item.setAttribute("name", name);
 			item.setAttribute("path", path);
 
 			// Sub foldersfor
 			for (FileBrowseInfo browseField : browseList) {
 				Element childitem = doc.createElement("item");
 				childitem.setAttribute("type", browseField.getType());
 				childitem.setAttribute("name", browseField.getName());
 				childitem.setAttribute("path", browseField.getPath());
 				item.appendChild(childitem);
 			}
 			DOMSource source = new DOMSource(doc);
 			return source;
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private String settingsCertificateSave(File file, String appDirName,
 			AddCertificateInfo addCertificateInfo) throws PhrescoException {
 		String certifactPath = "";
 			StringBuilder sb = new StringBuilder(CERTIFICATES).append(File.separator).append(
 					addCertificateInfo.getEnvironmentName()).append(HYPHEN).append(addCertificateInfo.getConfigName())
 					.append(FrameworkConstants.DOT).append(FILE_TYPE_CRT);
 			certifactPath = sb.toString();
 			if (file.exists()) {
 				File dstFile = new File(Utility.getProjectHome() + certifactPath);
 				FrameworkUtil.copyFile(file, dstFile);
 			} else {
 				saveCertificateFile(certifactPath, addCertificateInfo.getHost(), Integer
 						.parseInt(addCertificateInfo.getPort()), addCertificateInfo.getCertificateName(), appDirName);
 			}
 		return certifactPath;
 	}
 
 	private String configCertificateSave(String value, File file, String appDirName,
 			AddCertificateInfo addCertificateInfo) throws PhrescoException {
 			if (file.exists()) {
 				String path = Utility.getProjectHome().replace("\\", "/");
 				value = value.replace(path + appDirName + "/", "");
 			} else {
 				StringBuilder sb = new StringBuilder(FOLDER_DOT_PHRESCO).append(File.separator).append(CERTIFICATES)
 						.append(File.separator).append(addCertificateInfo.getEnvironmentName()).append(HYPHEN).append(
 								addCertificateInfo.getConfigName()).append(FrameworkConstants.DOT)
 						.append(FILE_TYPE_CRT);
 				value = sb.toString();
 				saveCertificateFile(value, addCertificateInfo.getHost(), Integer
 						.parseInt(addCertificateInfo.getPort()), addCertificateInfo.getCertificateName(), appDirName);
 			}
 		return value;
 	}
 
 	private void saveCertificateFile(String certificatePath, String host, int port,
 			String certificateName, String appDirName) throws PhrescoException {
 		List<CertificateInfo> certificates = FrameworkServiceUtil.getCertificate(host, port);
 		if (CollectionUtils.isNotEmpty(certificates)) {
 			for (CertificateInfo certificate : certificates) {
 				if (certificate.getDisplayName().equals(certificateName)) {
 					File file = new File(Utility.getProjectHome() + appDirName + "/" + certificatePath);
 					FrameworkServiceUtil.addCertificate(certificate, file);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the environments.
 	 *
 	 * @param appInfo the app info
 	 * @return the environments
 	 * @throws ConfigurationException the configuration exception
 	 */
 	private List<Environment> getEnvironments(ApplicationInfo appInfo) throws ConfigurationException {
 		String configFile = FrameworkServiceUtil.getConfigFileDir(appInfo.getAppDirName());
 		ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 		List<Environment> environments = configManager.getEnvironmentsAlone();
 		return environments;
 	}
 
 	/**
 	 * Validate configuration.
 	 *
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @param appDirName the app dir name
 	 * @param configurationlist the configurationlist
 	 * @return 
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String validateConfiguration(String userId, String customerId, String appDirName,
 			List<Configuration> configurationlist) throws PhrescoException {
 		ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 		int serverCount = 0;
 		int emailCount = 0;
 		boolean serverTypeValidation = false;
 		boolean isRequired = false;
 		String techId = "";
 		String dynamicError = "";
 		for (int i = 0; i < configurationlist.size(); i++) {
 			if (StringUtils.isEmpty(configurationlist.get(i).getName())) {
 				return "Name is Empty";
 			} else {
 				String name = configurationlist.get(i).getName();
 				for (int j = 0; j < configurationlist.size(); j++) {
 					if (i != j) {
 						if (name.equals(configurationlist.get(j).getName())) {
 							return "Name already Exists";
 						}
 					}
 				}
 			}
 		}
 
 		for (Configuration configuration : configurationlist) {
 			SettingsTemplate configTemplateByType = serviceManager.getConfigTemplateByType(customerId, configuration
 					.getType());
 			if (StringUtils.isEmpty(configuration.getType())) {
 				return "Configuration Type is Empty";
 			}
 
 //			if (FrameworkConstants.SERVER.equals(configuration.getType())
 //					|| FrameworkConstants.EMAIL.equals(configuration.getType())) {
 				
 				if (FrameworkConstants.SERVER.equals(configuration.getType())) {
 					serverCount++;
 				}
 				
 			if(FrameworkConstants.EMAIL.equals(configuration.getType())) {
 					String propertyEmail = configuration.getProperties().getProperty(FrameworkConstants.EMAIL_ID);
 					emailCount++;
 					if (propertyEmail.isEmpty()) {
 						return "Email ID is Empty";
 					} else {
 						String emailvalidation = emailvalidation(propertyEmail);
 						 if(StringUtils.isNotEmpty(emailvalidation)) {
 							 return emailvalidation;
 						 }
 					}
 				}
 
 			if (serverCount > 1) {
 				return "Server Configuration type Already Exists";
 			}
 
 			if (emailCount > 1) {
 				return "Email Configuration type Already Exists";
 			}
 
 			if (!FrameworkConstants.OTHERS.equals(configuration.getType())) {
 				List<PropertyTemplate> properties = configTemplateByType.getProperties();
 				for (PropertyTemplate propertyTemplate : properties) {
 					String propKey = propertyTemplate.getKey();
 					String propValue = configuration.getProperties().getProperty(propKey);
 					if (FrameworkConstants.REQ_TYPE.equals(propKey)
 							&& FrameworkConstants.NODEJS_SERVER.equals(propValue)
 							|| FrameworkConstants.NODEJS_MAC_SERVER.equals(propValue)
 							|| FrameworkConstants.SHAREPOINT_SERVER.equals(propValue)
 							|| FrameworkConstants.IIS_SERVER.equals(propValue)) {
 						// If nodeJs and sharepoint server selected , there should not be validation for deploy dir.
 						serverTypeValidation = true;
 					}
 
 					techId = FrameworkServiceUtil.getApplicationInfo(appDirName).getTechInfo().getId();
 					if (techId != null && techId.equals(FrameworkConstants.TECH_SITE_CORE)) {
 						if (FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 							isRequired = false;
 						}
 					}
 
 					if (serverTypeValidation && FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 						isRequired = false;
 					}
 
 					// validation for UserName & Password for RemoteDeployment
 
 					if (FrameworkConstants.REMOTE_DEPLOYMENT.equals(propKey)
 							&& FrameworkConstants.TRUE.equals(propValue)) {
 						if (FrameworkConstants.ADMIN_USERNAME.equals(propKey)
 								|| FrameworkConstants.ADMIN_PASSWORD.equals(propKey)) {
 							isRequired = true;
 						}
 						if (FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 							isRequired = false;
 						}
 					}
 
 					if (isRequired && StringUtils.isEmpty(propValue)) {
 						String field = propertyTemplate.getName();
 						dynamicError += propKey + Constants.STR_COLON + field + "is missing" + Constants.STR_COMMA;
 					}
 
 					if (StringUtils.isNotEmpty(dynamicError)) {
 						dynamicError = dynamicError.substring(0, dynamicError.length() - 1);
 						return dynamicError;
 					}
 
 					// Version Validation for Server and Database configuration types
 //					if (FrameworkConstants.SERVER.equals(configuration.getType())
 //							|| FrameworkConstants.DATABASE.equals(configuration.getType())) {
 //						if (StringUtils.isEmpty(configuration.getProperties().getProperty(FrameworkConstants.VERSION))) {
 //							return "Version is Empty";
 //						}
 //					}
 					
 					// Site Core installation path check
 					if (techId.equals(FrameworkConstants.TECH_SITE_CORE)
 							&& FrameworkConstants.SERVER.equals(configuration.getType())
 							&& StringUtils.isEmpty(configuration.getProperties().getProperty(
 									FrameworkConstants.SETTINGS_TEMP_SITECORE_INST_PATH))) {
 						return "SiteCore Installation path Location is missing";
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Emailvalidation.
 	 *
 	 * @param propertyEmail the property email
 	 * @return 
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String emailvalidation(String propertyEmail) throws PhrescoException {
 		Pattern p = Pattern
 				.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
 		Matcher m = p.matcher(propertyEmail);
 		boolean b = m.matches();
 		if (!b) {
 			return "Email Format mismatch";
 		}
 		return null;
 
 	}
 
 	/**
 	 * Test cron expression.
 	 *
 	 * @param expression the expression
 	 * @return the date[]
 	 * @throws PhrescoException the phresco exception
 	 */
 	private Date[] testCronExpression(String expression) throws PhrescoException {
 		Date[] dates = null;
 		try {
 			final CronExpression cronExpression = new CronExpression(expression);
 			final Date nextValidDate1 = cronExpression.getNextValidTimeAfter(new Date());
 			final Date nextValidDate2 = cronExpression.getNextValidTimeAfter(nextValidDate1);
 			final Date nextValidDate3 = cronExpression.getNextValidTimeAfter(nextValidDate2);
 			final Date nextValidDate4 = cronExpression.getNextValidTimeAfter(nextValidDate3);
 			dates = new Date[] { nextValidDate1, nextValidDate2, nextValidDate3, nextValidDate4 };
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return dates;
 	}
 
 	/**
 	 * Checks if is connection alive.
 	 *
 	 * @param protocol the protocol
 	 * @param host the host
 	 * @param port the port
 	 * @return true, if is connection alive
 	 */
 	public boolean isConnectionAlive(String protocol, String host, int port) {
 		boolean isAlive = true;
 		try {
 			URL url = new URL(protocol, host, port, "");
 			URLConnection connection = url.openConnection();
 			connection.connect();
 		} catch (Exception e) {
 			isAlive = false;
 		}
 
 		return isAlive;
 	}
 
 	/**
 	 * Gets the download info.
 	 *
 	 * @param serviceManager the service manager
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @param type the type
 	 * @return the download info
 	 * @throws PhrescoException the phresco exception
 	 */
 	private Map<String, List<String>> getDownloadInfo(ServiceManager serviceManager, String appDirName, String userId,
 			String type) throws PhrescoException {
 		ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 		List<ArtifactGroupInfo> artifactGroupInfos = null;
 		Map<String, List<String>> nameMap = new HashMap<String, List<String>>();
 
 		if (Constants.SETTINGS_TEMPLATE_SERVER.equals(type)) {
 			artifactGroupInfos = appInfo.getSelectedServers();
 		} else if (Constants.SETTINGS_TEMPLATE_DB.equals(type)) {
 			artifactGroupInfos = appInfo.getSelectedDatabases();
 		}
 		if (CollectionUtils.isNotEmpty(artifactGroupInfos)) {
 			for (ArtifactGroupInfo artifactGroupInfo : artifactGroupInfos) {
 				ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactGroupInfo
 						.getArtifactGroupId());
 				List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
 				List<String> verstions = new ArrayList<String>();
 				for (String artifactInfoId : artifactInfoIds) {
 					ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifactInfoId);
 					verstions.add(artifactInfo.getVersion());
 				}
 				nameMap.put(artifactGroup.getName(), verstions);
 			}
 		}
 		return nameMap;
 	}
 }
