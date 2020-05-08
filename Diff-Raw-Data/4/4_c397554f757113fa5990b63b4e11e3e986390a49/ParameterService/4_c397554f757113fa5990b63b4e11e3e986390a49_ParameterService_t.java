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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.plexus.util.FileUtils;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.api.DynamicPageParameter;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.CodeValidationReportType;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.framework.model.PerformanceDetails;
 import com.photon.phresco.framework.param.impl.IosTargetParameterImpl;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.DynamicParameter.Dependencies.Dependency;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class ParameterService.
  */
 @Path("/parameter")
 public class ParameterService extends RestBase implements FrameworkConstants, ServiceConstants, ResponseCodes {
 	private static final String COLORS_CUSTOMER_COLOR = "&colors=customerColor";
 	private static final String CSS_PHRESCO_STYLE = "css=phresco_style";
 	private static Map<String, PhrescoDynamicLoader> pdlMap = new HashMap<String, PhrescoDynamicLoader>();
 	private static Map<String, Map<String, DependantParameters>> valueMap = new HashMap<String, Map<String, DependantParameters>>();
 	private static Map<String, List<PerformanceDetails>> templateMap = new HashMap<String, List<PerformanceDetails>>();
 	private static String SUCCESS = "success";
 	
 	/**
 	 * Gets the parameter.
 	 *
 	 * @param appDirName the app dir name
 	 * @param goal the goal
 	 * @param phase the phase
 	 * @return the parameter
 	 */
 	@GET
 	@Path("/dynamic")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getParameter(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam("iphoneDeploy") String iphoneDeploy,
 			@QueryParam(REST_QUERY_GOAL) String goal, @QueryParam(REST_QUERY_PHASE) String phase, 
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,  @QueryParam("buildNumber") String buildNumber) {
 		ResponseInfo<List<Parameter>> responseData = new ResponseInfo<List<Parameter>>();
 		try {
 			ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			List<Parameter> parameters = null;
 			String filePath = getInfoFileDir(appDirName, goal, phase);
 			File file = new File(filePath);
 			if (file.exists()) {
 				MojoProcessor mojo = new MojoProcessor(file);
 				if (Constants.PHASE_FUNCTIONAL_TEST.equals(goal)) {
 					String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(appDirName);
 					goal = goal + HYPHEN + functionalTestFramework;
 				}
 				getValues(iphoneDeploy, appInfo, mojo, goal);
 				parameters = mojo.getParameters(goal);
 				Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
 
 				setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, goal, userId, customerId, buildNumber);
 				
 				ResponseInfo<List<Parameter>> finalOutput = responseDataEvaluation(responseData, null,
 						parameters, RESPONSE_STATUS_SUCCESS, PHR1C00001);
 				return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 			}
 			ResponseInfo<List<Parameter>> finalOutput = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_SUCCESS, PHR1C00002);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<Parameter>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR1C10002);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (PhrescoPomException e) {
 			ResponseInfo<List<Parameter>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR1C10003);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 
 	public static void getValues(String iphoneDeploy, ApplicationInfo appInfo, MojoProcessor mojo, String goal) throws PhrescoException {
 		try {
 			if(StringUtils.isEmpty(iphoneDeploy)) {
 				return;
 			}
 			
 				if (iphoneDeploy.equals("false")) { // if it is simulator, show popup for following dependency
 					setShowPropValue(mojo, "sdkVersion", true, goal);
 					setShowPropValue(mojo, "family", true, goal);
 					setShowPropValue(mojo, "logs", true, goal);
 					setShowPropValue(mojo, "buildNumber", true, goal);
 					setPropValue(mojo, "triggerSimulator", TRUE, goal);
 					setPropValue(mojo, "deviceType", "simulator", goal);
 				} else { // if it is device, it should return null and should not show any popup
 					setShowPropValue(mojo, "sdkVersion", false, goal);
 					setShowPropValue(mojo, "family", false, goal);
 					setShowPropValue(mojo, "logs", false, goal);
 					setShowPropValue(mojo, "buildNumber", false, goal);
 					setPropValue(mojo, "triggerSimulator", FALSE, goal);
 					setPropValue(mojo, "deviceType", "device", goal);
 				}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static void setPropValue(MojoProcessor mojo, String key, String value, String goal) throws PhrescoException {
     	Parameter parameter = mojo.getParameter(goal, key);
 		parameter.setValue(value);
     	mojo.save();
 	}
 	
 	private static void setShowPropValue(MojoProcessor mojo, String key, boolean isShow, String goal) throws PhrescoException {
     	Parameter parameter = mojo.getParameter(goal, key);
 		parameter.setShow(isShow);
     	mojo.save();
 	}
 
 	/**
 	 * Gets the file as string.
 	 *
 	 * @param appDirName the app dir name
 	 * @param goal the goal
 	 * @param phase the phase
 	 * @return the file as string
 	 */
 	@GET
 	@Path("/file")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response getFileAsString(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_GOAL) String goal, @QueryParam(REST_QUERY_PHASE) String phase) {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			String filePath = getInfoFileDir(appDirName, goal, phase);
 			File file = new File(filePath);
 			String xml = IOUtils.toString(new FileInputStream(file));
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, PARAMETER_RETURNED_SUCCESSFULLY,
 					xml);
 			return Response.status(200).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (Exception e) {
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, PARAMETER_NOT_FETCHED, null);
 			return Response.status(Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 	
 	/**
 	 * Update watcher map
 	 * 
 	 */
 	@POST
 	@Path("/updateWatcher")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response updateWatcher(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_GOAL) String goal,
 			@QueryParam(REST_QUERY_KEY) String key, @QueryParam(REST_QUERY_VALUE) String value) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			if (Constants.PHASE_FUNCTIONAL_TEST.equals(goal)) {
 				String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(appDirName);
 				goal = goal + HYPHEN + functionalTestFramework;
 			}
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			Map<String, DependantParameters> watcherMap = valueMap.get(applicationInfo.getId() + goal);
 			DependantParameters currentParameters = watcherMap.get(key);
 			if (currentParameters == null) {
 				currentParameters = new DependantParameters();
 			}
 			
 			currentParameters.setValue(value);
 			watcherMap.put(key, currentParameters);
 			valueMap.put(applicationInfo.getId() + goal, watcherMap);
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					SUCCESS, RESPONSE_STATUS_SUCCESS, PHR5C00001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					FAILURE, RESPONSE_STATUS_ERROR, PHR5C10001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (PhrescoPomException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					FAILURE, RESPONSE_STATUS_ERROR, PHR5C10001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} 
 	}
 	
 	/**
 	 * Gets the possible value.
 	 *
 	 * @param appDirName the app dir name
 	 * @param customerId the customer id
 	 * @param goal the goal
 	 * @param key the key
 	 * @param value the value
 	 * @param phase the phase
 	 * @return the possible value
 	 */
 	@POST
 	@Path("/dependency")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getDependencyPossibleValue(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_GOAL) String goal, @QueryParam(REST_QUERY_KEY) String key, 
 			@QueryParam(REST_QUERY_PHASE) String phase) {
 		ResponseInfo responseData = new ResponseInfo();
 		PossibleValues possibleValues = null;
 		ResponseInfo finalOutput = new ResponseInfo();
 		try {
 			if (Constants.PHASE_FUNCTIONAL_TEST.equals(goal)) {
 				String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(appDirName);
 				goal = goal + HYPHEN + functionalTestFramework;
 			}
 			
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			Map<String, DependantParameters> watcherMap = valueMap.get(applicationInfo.getId() + goal);
 			Map<String, Object> constructMapForDynVals = constructMapForDynVals(applicationInfo, watcherMap, key, customerId, null);
 			String filePath = getInfoFileDir(appDirName, goal, phase);
 			MojoProcessor mojo = new MojoProcessor(new File(filePath));
 			Parameter dependentParameter = mojo.getParameter(goal, key);
 			constructMapForDynVals.put(REQ_MOJO, mojo);
             constructMapForDynVals.put(REQ_GOAL, goal);
             List<Value> dependentPossibleValues = new ArrayList<Value>();
             if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(dependentParameter.getType()) && dependentParameter.getDynamicParameter() != null) {
             	dependentPossibleValues = getDynamicPossibleValues(constructMapForDynVals, dependentParameter, userId, customerId);
             	 finalOutput = responseDataEvaluation(responseData, null,
     					dependentPossibleValues, RESPONSE_STATUS_SUCCESS, PHR6C00001);
             } else if (TYPE_DYNAMIC_PAGE_PARAMETER.equalsIgnoreCase(dependentParameter.getType()) && dependentParameter.getDynamicParameter() != null) {
             	Map<String, Object> dynamicPageParameterMap = getDynamicPageParameter(applicationInfo, watcherMap, dependentParameter, userId, customerId);
     			List<? extends Object> dynamicPageParameter = (List<? extends Object>) dynamicPageParameterMap.get(REQ_VALUES_FROM_JSON);
     			List<PerformanceDetails> templateDetails = (List<PerformanceDetails>) dynamicPageParameter;
     			templateMap.put(applicationInfo.getId() + dependentParameter.getKey(), templateDetails);
     			StringTemplate constructDynamicTemplate = new StringTemplate();
     			constructDynamicTemplate = constructDynamicTemplate(customerId, userId, dependentParameter, templateDetails);
     			 finalOutput = responseDataEvaluation(responseData, null,
     					constructDynamicTemplate.toString(), RESPONSE_STATUS_SUCCESS, PHR6C00001);
             } else {
             	finalOutput = responseDataEvaluation(responseData, null,
     					null, RESPONSE_STATUS_SUCCESS, PHR6C00001);
             }
             
             if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(dependentParameter.getType())) {
             	updateDynamicValuesToWathcer(goal, key, applicationInfo, watcherMap, dependentParameter, dependentPossibleValues);
             }
             
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (Exception e) {
 			 finalOutput = responseDataEvaluation(responseData, new PhrescoException(e),
 					null, RESPONSE_STATUS_ERROR, PHR6C10001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 
 	private void updateDynamicValuesToWathcer(String goal, String key, ApplicationInfo applicationInfo, Map<String, DependantParameters> watcherMap,
 			Parameter dependentParameter, List<Value> dependentPossibleValues) {
 		
 		if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMap.containsKey(key)) {
 		    DependantParameters dependantParameters = (DependantParameters) watcherMap.get(key);
 		    dependantParameters.setValue(dependentPossibleValues.get(0).getValue());
 		} else {
 			DependantParameters dependantParameters = (DependantParameters) watcherMap.get(key);
 		    dependantParameters.setValue("");
 		}
 		if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMap.containsKey(dependentPossibleValues.get(0).getDependency())) {
 		    addValueDependToWatcher(watcherMap, dependentParameter.getKey(), dependentPossibleValues, "");
 		    if (CollectionUtils.isNotEmpty(dependentPossibleValues)) {
 		    	addWatcher(watcherMap, dependentParameter.getDependency(), 
 		    			dependentParameter.getKey(), dependentPossibleValues.get(0).getValue());
 		    }
 		}
 		valueMap.put(applicationInfo.getId() + goal, watcherMap);
 	}
 
 	/**
 	 * Gets the code validation report types.
 	 *
 	 * @param appDirName the app dir name
 	 * @param goal the goal
 	 * @param phase the phase
 	 * @param request the request
 	 * @return the code validation report types
 	 */
 	@GET
     @Path("/codeValidationReportTypes")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getCodeValidationReportTypes(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
             @QueryParam(REST_QUERY_GOAL) String goal, @QueryParam(REST_QUERY_PHASE) String phase,
             @Context HttpServletRequest request) {
         ResponseInfo<List<CodeValidationReportType>> responseData = new ResponseInfo<List<CodeValidationReportType>>();
         try {
 //            int responseCode = setSonarServerStatus(request);
 //            if (responseCode != 200) {
 //                ResponseInfo<List<CodeValidationReportType>> finalOutput = responseDataEvaluation(responseData, null,
 //                        null, RESPONSE_STATUS_FAILURE, PHR510001);
 //                return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 //                        ALL_HEADER).build();
 //            }
             String infoFileDir = getInfoFileDir(appDirName, goal, phase);
             ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
             List<CodeValidationReportType> codeValidationReportTypes = new ArrayList<CodeValidationReportType>();
 
             // To get parameter values for Iphone technology
             PomProcessor pomProcessor = FrameworkServiceUtil.getPomProcessor(appDirName);
             String validateReportUrl = pomProcessor.getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT);
             if (StringUtils.isNotEmpty(validateReportUrl)) {
                 List<Value> clangReports = getClangReports(appInfo);
                 for (Value value : clangReports) {
         			CodeValidationReportType codeValidationReportType = new CodeValidationReportType();
 					codeValidationReportType.setValidateAgainst(value);
 					codeValidationReportTypes.add(codeValidationReportType);
                 }
 //                ResponseInfo<List<CodeValidationReportType>> finalOutput = responseDataEvaluation(responseData, null,
 //                        DEPENDENCY_RETURNED_SUCCESSFULLY, codeValidationReportTypes);
                 return Response.ok(codeValidationReportTypes).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
             }
             MojoProcessor processor = new MojoProcessor(new File(infoFileDir));
             Parameter parameter = processor.getParameter(Constants.PHASE_VALIDATE_CODE, "sonar");
             PossibleValues possibleValues = parameter.getPossibleValues();
             List<Value> values = possibleValues.getValue();
             for (Value value : values) {
                 CodeValidationReportType codeValidationReportType = new CodeValidationReportType();
                 String key = value.getKey();
                 Parameter depParameter = processor.getParameter(Constants.PHASE_VALIDATE_CODE, key);
                 if (depParameter != null && depParameter.getPossibleValues() != null) {
                     PossibleValues depPossibleValues = depParameter.getPossibleValues();
                     List<Value> depValues = depPossibleValues.getValue();
                     codeValidationReportType.setOptions(depValues);
                 }
                 codeValidationReportType.setValidateAgainst(value);
                 codeValidationReportTypes.add(codeValidationReportType);
             }
 //            ResponseInfo<List<CodeValidationReportType>> finalOutput = responseDataEvaluation(responseData, null,
 //                    codeValidationReportTypes, RESPONSE_STATUS_SUCCESS, PHR500001);
             return Response.ok(codeValidationReportTypes).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
         } catch (PhrescoException e) {
             ResponseInfo<List<CodeValidationReportType>> finalOutput = responseDataEvaluation(responseData, e,
                     null, RESPONSE_STATUS_ERROR, PHR510002);
             return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
                     .build();
         } catch (PhrescoPomException e) {
             ResponseInfo<List<CodeValidationReportType>> finalOutput = responseDataEvaluation(responseData, e,
                     null, RESPONSE_STATUS_ERROR, PHR510005);
             return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
                     .build();
         }
     }
 
 	@GET
 	@Path("/sonarUrl")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getSonarUrl(@Context HttpServletRequest request) {
 		FrameworkUtil frameworkUtil = new FrameworkUtil(request);
 		 ResponseInfo<String> responseData = new ResponseInfo<String>();
 			try {
				URL sonarURL = new URL(frameworkUtil.getSonarURL());
				String url = sonarURL.toString();
 				ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 						url, RESPONSE_STATUS_SUCCESS, PHR500004);
 				return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 			} catch (MalformedURLException e) {
 				 ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e,
 		                    null, RESPONSE_STATUS_ERROR, PHR510010);
 		            return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		                    .build();
 			} catch (PhrescoException e) {
 				 ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e,
 		                    null, RESPONSE_STATUS_ERROR, PHR510011);
 		            return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		                    .build();
 			}
 	}
 	
 
 	/**
 	 * Gets the iframe report.
 	 *
 	 * @param customerId the customer id
 	 * @param userId the user id
 	 * @param appDirName the app dir name
 	 * @param validateAgainst the validate against
 	 * @param request the request
 	 * @return the iframe report
 	 */
 	@GET
 	@Path("/iFrameReport")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getIframeReport(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_VALIDATE_AGAINST) String validateAgainst, @Context HttpServletRequest request) {
 		ResponseInfo<PossibleValues> responseData = new ResponseInfo<PossibleValues>();
 		StringBuilder sb = new StringBuilder();
 		try {
 			ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			PomProcessor processor = FrameworkServiceUtil.getPomProcessor(appDirName);
 			String validateReportUrl = processor.getProperty(Constants.POM_PROP_KEY_VALIDATE_REPORT);
 			FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			Customer customer = serviceManager.getCustomer(customerId);
 			// Check whether iphone Technology or not
 			Properties sysProps = System.getProperties();
 			String phrescoFileServerNumber = sysProps.getProperty(PHRESCO_FILE_SERVER_PORT_NO);
 			if (StringUtils.isNotEmpty(validateReportUrl)) {
 				StringBuilder codeValidatePath = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 				codeValidatePath.append(validateReportUrl);
 				codeValidatePath.append(validateAgainst);
 				codeValidatePath.append(File.separatorChar);
 				codeValidatePath.append(INDEX_HTML);
 				File indexPath = new File(codeValidatePath.toString());
 				if (indexPath.isFile() && StringUtils.isNotEmpty(phrescoFileServerNumber)) {
 					sb.append(HTTP_PROTOCOL);
 					sb.append(PROTOCOL_POSTFIX);
 					InetAddress thisIp = InetAddress.getLocalHost();
 					sb.append(thisIp.getHostAddress());
 					sb.append(FrameworkConstants.COLON);
 					sb.append(phrescoFileServerNumber);
 					sb.append(FrameworkConstants.FORWARD_SLASH);
 					sb.append(appDirName);
 					sb.append(validateReportUrl);
 					sb.append(validateAgainst);
 					sb.append(FrameworkConstants.FORWARD_SLASH);
 					sb.append(INDEX_HTML);
 					ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 							sb.toString(), RESPONSE_STATUS_SUCCESS, PHR500003);
 					return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 				} else {
 					ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, null,
 							null, RESPONSE_STATUS_FAILURE, PHR510009);
 					return Response.status(Status.OK).entity(finalOutput).header(
 							ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 				}
 			}
 			String serverUrl = "";
 			FrameworkUtil frameworkUtil = new FrameworkUtil(request);
 			serverUrl = frameworkUtil.getSonarHomeURL();
 			StringBuilder reportPath = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 			if (StringUtils.isNotEmpty(validateAgainst) && FUNCTIONALTEST.equals(validateAgainst)) {
 				reportPath.append(frameworkUtil.getFunctionalTestDir(appInfo));
 			}
 			reportPath.append(File.separatorChar);
 			reportPath.append(POM_XML);
 			File file = new File(reportPath.toString());
 			processor = new PomProcessor(file);
 			String groupId = processor.getModel().getGroupId();
 			String artifactId = processor.getModel().getArtifactId();
 
 			sb.append(serverUrl);
 			sb.append(frameworkConfig.getSonarReportPath());
 			sb.append(groupId);
 			sb.append(FrameworkConstants.COLON);
 			sb.append(artifactId);
 
 			if (StringUtils.isNotEmpty(validateAgainst) && !REQ_SRC.equals(validateAgainst)) {
 				sb.append(FrameworkConstants.COLON);
 				sb.append(validateAgainst);
 			}
 			URL sonarURL = new URL(sb.toString());
 			HttpURLConnection connection = (HttpURLConnection) sonarURL.openConnection();
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_FAILURE, PHR510003);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						ALL_HEADER).build();
 			}
 			
 			Map<String, String> theme = customer.getFrameworkTheme();
 			if (MapUtils.isNotEmpty(theme)) {
 				sb.append("?");
 				sb.append(CUST_BASE_COLOR + theme.get("customerBaseColor"));
 				sb.append("&" + CSS_PHRESCO_STYLE);
 			} else {
 				sb.append("?");
 				sb.append(CSS_PHRESCO_STYLE);
 			}
 		} catch (PhrescoException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR510009);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (PhrescoPomException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR510006);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (UnknownHostException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR510007);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (IOException e) {
 			ResponseInfo<PossibleValues> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR510008);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 		ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 				sb.toString(), RESPONSE_STATUS_SUCCESS, PHR500003);
 		return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 	}
 
 	/**
 	 * Gets the clang reports.
 	 *
 	 * @param appInfo the app info
 	 * @return the clang reports
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<Value> getClangReports(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			IosTargetParameterImpl targetImpl = new IosTargetParameterImpl();
 			Map<String, Object> paramMap = new HashMap<String, Object>();
 			paramMap.put(FrameworkConstants.KEY_APP_INFO, appInfo);
 			PossibleValues possibleValues = targetImpl.getValues(paramMap);
 			List<Value> values = possibleValues.getValue();
 			return values;
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (SAXException e) {
 			throw new PhrescoException(e);
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Sets the sonar server status.
 	 *
 	 * @param request the request
 	 * @return the int
 	 * @throws PhrescoException the phresco exception
 	 */
 	private int setSonarServerStatus(HttpServletRequest request) throws PhrescoException {
 		FrameworkUtil frameworkUtil = new FrameworkUtil(request);
 		int responseCode = 0;
 		try {
 			URL sonarURL = new URL(frameworkUtil.getSonarHomeURL());
 			String protocol = sonarURL.getProtocol();
 			HttpURLConnection connection = null;
 			if (protocol.equals("http")) {
 				connection = (HttpURLConnection) sonarURL.openConnection();
 				responseCode = connection.getResponseCode();
 			} else {
 				responseCode = FrameworkUtil.getHttpsResponse(frameworkUtil.getSonarURL());
 			}
 			return responseCode;
 		} catch (Exception e) {
 			return responseCode;
 		}
 	}
 
 	/**
 	 * To setPossibleValuesInReq
 	 * @param mojo
 	 * @param appInfo
 	 * @param parameters
 	 * @param watcherMap
 	 * @param goal
 	 * @throws PhrescoException
 	 */
 	private void setPossibleValuesInReq(MojoProcessor mojo, ApplicationInfo appInfo, List<Parameter> parameters, 
     		Map<String, DependantParameters> watcherMap, String goal, String userId, String customerId, String buildNumber) throws PhrescoException {
         try {
             if (CollectionUtils.isNotEmpty(parameters)) {
                 StringBuilder paramBuilder = new StringBuilder();
 				ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
                 for (Parameter parameter : parameters) {
                     String parameterKey = parameter.getKey();
                     if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && parameter.getDynamicParameter() != null) { 
                     	//Dynamic parameter
                         Map<String, Object> constructMapForDynVals = constructMapForDynVals(appInfo, watcherMap, parameterKey, customerId, buildNumber);
                         constructMapForDynVals.put(REQ_MOJO, mojo);
                         constructMapForDynVals.put(REQ_GOAL, goal);
 						constructMapForDynVals.put(REQ_SERVICE_MANAGER, serviceManager);
                         // Get the values from the dynamic parameter class
                         List<Value> dynParamPossibleValues = getDynamicPossibleValues(constructMapForDynVals, parameter, userId, customerId);
                         addValueDependToWatcher(watcherMap, parameterKey, dynParamPossibleValues, parameter.getValue());
                         if (watcherMap.containsKey(parameterKey)) {
                             DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
                             if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                             	if (StringUtils.isNotEmpty(parameter.getValue())) {
                                 	dependantParameters.setValue(parameter.getValue());
                                 } else {
                                 	dependantParameters.setValue(dynParamPossibleValues.get(0).getValue());
                                 }
                             }
                         }
                         
                         PossibleValues possibleValues = new PossibleValues();
                         possibleValues.getValue().addAll(dynParamPossibleValues);
                         parameter.setPossibleValues(possibleValues);
                         if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                         	if (StringUtils.isNotEmpty(parameter.getValue())) {
                         		addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                         	} else {
                         		addWatcher(watcherMap, parameter.getDependency(), parameterKey, dynParamPossibleValues.get(0).getValue());
                         	}
                         }
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                             paramBuilder.append(dynParamPossibleValues.get(0).getValue());
                         }
                     } else if (parameter.getPossibleValues() != null) { //Possible values
                         List<Value> values = parameter.getPossibleValues().getValue();
                         
                         if (watcherMap.containsKey(parameterKey)) {
                             DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
                             if (StringUtils.isNotEmpty(parameter.getValue())) {
                             	dependantParameters.setValue(parameter.getValue());
                             } else {
                             	dependantParameters.setValue(values.get(0).getValue());
                             }
                         }
                         
                         addValueDependToWatcher(watcherMap, parameterKey, values, parameter.getValue());
                         if (CollectionUtils.isNotEmpty(values)) {
                         	if (StringUtils.isNotEmpty(parameter.getValue())) {
                             	addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                             } else {
                             	addWatcher(watcherMap, parameter.getDependency(), parameterKey, values.get(0).getKey());
                             }
                         }
                         
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         paramBuilder.append("");
                     } else if (parameter.getType().equalsIgnoreCase(TYPE_BOOLEAN) && StringUtils.isNotEmpty(parameter.getDependency())) {
                     	//Checkbox
                         addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         paramBuilder.append("");
                     } else if(TYPE_DYNAMIC_PAGE_PARAMETER.equalsIgnoreCase(parameter.getType())) {
             			Map<String, Object> dynamicPageParameterMap = getDynamicPageParameter(appInfo, watcherMap, parameter, userId, customerId);
             			List<? extends Object> dynamicPageParameter = (List<? extends Object>) dynamicPageParameterMap.get(REQ_VALUES_FROM_JSON);
             			List<PerformanceDetails> templateDetails = (List<PerformanceDetails>) dynamicPageParameter;
             			templateMap.put(appInfo.getId() + parameter.getKey(), templateDetails);
             		}
                 }
                 valueMap.put(appInfo.getId() + goal, watcherMap);
             }
         } catch (Exception e) {
         	throw new PhrescoException(e);
         }
     }
 	
 	/**
 	 * Gets the parameter.
 	 *
 	 * @param appDirName the app dir name
 	 * @param goal the goal
 	 * @param phase the phase
 	 * @return the parameter
 	 */
 	@GET
 	@Path("/template")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getTemplate(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_GOAL) String goal, @QueryParam(REST_QUERY_PHASE) String phase, 
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam("parameterKey") String parameterKey) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			List<Parameter> parameters = null;
 			StringTemplate constructDynamicTemplate = new StringTemplate();
 			String filePath = getInfoFileDir(appDirName, goal, phase);
 			File file = new File(filePath);
 			if (file.exists()) {
 				MojoProcessor mojo = new MojoProcessor(file);
 				if (Constants.PHASE_FUNCTIONAL_TEST.equals(goal)) {
 					String functionalTestFramework = FrameworkServiceUtil.getFunctionalTestFramework(appDirName);
 					goal = goal + HYPHEN + functionalTestFramework;
 				}
 				parameters = mojo.getParameters(goal);
 				Parameter templateParameter = mojo.getParameter(goal, parameterKey);
 				List<PerformanceDetails> performanceDetails = templateMap.get(appInfo.getId() + parameterKey);
 				constructDynamicTemplate = constructDynamicTemplate(customerId, userId, templateParameter, performanceDetails);
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					constructDynamicTemplate.toString(), RESPONSE_STATUS_SUCCESS, PHR7C00001);
 			return Response.ok(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 
 		} catch (Exception e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e,
 					null,RESPONSE_STATUS_ERROR, PHR7C10001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 	
 	@POST
 	@Path("/dynamicUpload")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
 	public Response dynamicFileUpload(@Context HttpServletRequest request) throws PhrescoException {
 		ResponseInfo<Boolean> responseData = new ResponseInfo<Boolean>();
 		ResponseInfo finalOuptut = new ResponseInfo();
 		try {
 			InputStream inputStream = request.getInputStream();
 			String appDirName = request.getHeader("appDirName");
 			String uploadedFileName = request.getHeader(X_FILE_NAME);
 			if (StringUtils.isNotEmpty(uploadedFileName)) {
 				if (PERFORMANCE_TEST.equals(request.getHeader("goal"))) {
 					String performanceTestDir = FrameworkServiceUtil.getPerformanceTestDir(appDirName);
 					String performanceUploadJmxDir = FrameworkServiceUtil.getPerformanceUploadJmxDir(appDirName);
 					finalOuptut = uploadFileForPerformanceLoad(request, appDirName, inputStream, performanceTestDir, performanceUploadJmxDir,uploadedFileName);
 				} else if (Constants.PHASE_LOAD_TEST.equals(request.getHeader("goal"))) {
 					String loadTestDir = FrameworkServiceUtil.getLoadTestDir(appDirName);
 					String loadUploadJmxDir = FrameworkServiceUtil.getLoadUploadJmxDir(appDirName);
 					finalOuptut = uploadFileForPerformanceLoad(request, appDirName, inputStream, loadTestDir, loadUploadJmxDir,uploadedFileName);
 				}
 			}
 			return Response.ok(finalOuptut).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (FileNotFoundException e) {
 			finalOuptut = responseDataEvaluation(responseData, e, "Upload Failed", false);
 			return Response.status(Status.EXPECTATION_FAILED).entity(finalOuptut).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					ALL_HEADER).build();
 		} catch (IOException e) {
 			finalOuptut = responseDataEvaluation(responseData, e, "Upload Failed", false);
 			return Response.status(Status.EXPECTATION_FAILED).entity(finalOuptut).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					ALL_HEADER).build();
 		} catch (PhrescoPomException e) {
 			finalOuptut = responseDataEvaluation(responseData, e, "Upload Failed", false);
 			return Response.status(Status.EXPECTATION_FAILED).entity(finalOuptut).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					ALL_HEADER).build();
 		}
 	}
 	
 	private ResponseInfo uploadFileForPerformanceLoad(HttpServletRequest request, String appDirName, InputStream inputStream, String testDirectory, String jmxUploadDir, String zipfileName) throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
     	File tempDirectory = null;
     	try {
 			StringBuilder uploadJmxDir = new StringBuilder(Utility.getProjectHome())
 			.append(appDirName)
 			.append(testDirectory)
 			.append(File.separator)
 			.append(request.getHeader(REQ_CUSTOM_TEST_AGAINST))
 			.append(jmxUploadDir);
 			
 			StringBuilder temp = new StringBuilder(uploadJmxDir);
 			temp.append(File.separator)
 			.append(Constants.PROJECTS_TEMP);
 
 			tempDirectory = new File(temp.toString());
 			if (!tempDirectory.exists()) {
 				tempDirectory.mkdir();
 			}
 			
 			 String tempZipPath = tempDirectory.getPath() + File.separator + zipfileName;
 
 			 //create zip file from inputstream
 			 File tempZipFile = FileUtil.writeFileFromInputStream(inputStream, tempZipPath);
 			 String folder = FileUtils.removeExtension(tempZipPath.replace(tempDirectory.getPath() + File.separator, ""));
 			 //extract the zip file inside temp directory
 			 boolean unzipped = ArchiveUtil.unzip(tempZipPath, tempDirectory.getPath(), folder);
 			 StringBuilder unzippedDir = new StringBuilder(tempDirectory.getPath());
 			 unzippedDir.append(File.separator)
 			 .append(Constants.PROJECTS_TEMP)
 			 .append(folder);
 			 
 			 File extractedPath = new File(unzippedDir.toString()); 
 			 boolean fileExist = false;
 			 if (unzipped) {
 				 if(extractedPath.exists()) {
 					 fileExist = checkFileExist(extractedPath, DOT_JMX, fileExist);
 				 }
 				 if (fileExist) {
 					 uploadJmxDir = uploadJmxDir.append(File.separator)
 					 .append(CUSTOM);
 					 
 					 File destination = new File(uploadJmxDir.toString());
 					 if (!destination.exists()) {
 						 destination.mkdir();
 					 }
 					 
 					 File extractedFile = new File(tempDirectory.getPath() + File.separator + Constants.PROJECTS_TEMP + folder);
 					 if (extractedFile.exists()) {
 						 FileUtil.copyFolder(extractedFile, destination);
 					 }
 					 responseDataEvaluation(responseData, null, "File uploaded successfully", "success", null);
 				 } else {
 					 responseDataEvaluation(responseData, null, "No jmx file exist", "failure", null);
 				 }
 			 } else {
 				 responseDataEvaluation(responseData, null, "Unable To Extract", "failure", null);
 			 }
 			 
 			 //after extracting, delete that zip file
 			 FileUtil.delete(tempZipFile);
 			 
 		} catch (Exception e) {
 			responseDataEvaluation(responseData, e, "Upload Failed", "failure", null);
 		} finally {
 			 FileUtil.delete(tempDirectory);
 		}
 		
 		return responseData;
     }
 	
 	 private boolean checkFileExist(File directory, String fileExtension, boolean flag) {
 	    	File[] childs = directory.listFiles();
 			if (childs  != null && childs.length != 0) {
 				for (File child : childs) {
 					if (child.isDirectory() && !flag) {
 						flag = checkFileExist(child, fileExtension, flag);//recursive call if the child is a directory
 					} else if (child.getName().endsWith(fileExtension) && !flag) {
 						flag = true;
 						return flag;
 					}
 					
 					if (flag) {
 						break;
 					}
 				}
 			}
 			
 			return flag;
 		}
 	
 	/**
 	 * gets the DynamicPageParameter
 	 * @param appInfo
 	 * @param watcherMap
 	 * @param parameter
 	 * @return
 	 * @throws PhrescoException
 	 */
 	private Map<String, Object> getDynamicPageParameter(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, Parameter parameter, 
 			String userId, String customerId) throws PhrescoException {
 		String parameterKey = parameter.getKey();
 		Map<String, Object> paramsMap = constructMapForDynVals(appInfo, watcherMap, parameterKey, customerId, null);
 		String className = parameter.getDynamicParameter().getClazz();
 		DynamicPageParameter dynamicPageParameter;
 		PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(customerId);
 		if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
 			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
 		} else {
 			//To get repo info from Customer object
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			Customer customer = serviceManager.getCustomer(customerId);
 			RepoInfo repoInfo = customer.getRepoInfo();
 			//To set groupid,artfid,type infos to List<ArtifactGroup>
 			List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 			ArtifactGroup artifactGroup = new ArtifactGroup();
 			artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
 			artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
 			artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
 			//to set version
 			List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 	        ArtifactInfo artifactInfo = new ArtifactInfo();
 	        artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
 			artifactInfos.add(artifactInfo);
 	        artifactGroup.setVersions(artifactInfos);
 			artifactGroups.add(artifactGroup);
 			
 			//dynamically loads specified Class
 			phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
 			pdlMap.put(customerId, phrescoDynamicLoader);
 		}
 		
 		return dynamicPageParameter.getObjects(paramsMap);
 	}
 	
 	 public StringTemplate constructDynamicTemplate(String CustomerId, String userId, Parameter parameter, List<? extends Object> obj) throws IOException {
 	    	try {
 	    		StringBuilder sb = new StringBuilder();
 	    		String line;
 	    		ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 	    		Customer customer = serviceManager.getCustomer(CustomerId);
 	    		RepoInfo repoInfo = customer.getRepoInfo();
 	    		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 	    		ArtifactGroup artifactGroup = new ArtifactGroup();
 	    		artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
 	    		artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
 	    		artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
 	    		//to set version
 	    		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 	    		ArtifactInfo artifactInfo = new ArtifactInfo();
 	    		artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
 	    		artifactInfos.add(artifactInfo);
 	    		artifactGroup.setVersions(artifactInfos);
 	    		artifactGroups.add(artifactGroup);
 	    		//dynamically loads Template Stream 
 	    		PhrescoDynamicLoader phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 	    		InputStream fileStream = phrescoDynamicLoader.getResourceAsStream(parameter.getKey()+".st");
 	    		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
 	    		while ((line = br.readLine()) != null) {
 	    			sb.append(line);
 	    		} 
 	    		
 	    		StringTemplate stringTemplate = new StringTemplate(sb.toString());
 	    		if (CollectionUtils.isNotEmpty(obj)) {
 	    			stringTemplate.setAttribute("myObject", obj);
 	    		} else {
 	    			stringTemplate.setAttribute("myObject", "");
 	    		}
 	    		
 	    		return stringTemplate;
 	    	} catch (Exception e) {
 	    	}
 
 	    	return null;
 	    }
 	 
 	 private static String getDynamicTemplateWholeDiv() {
 	    	StringBuilder sb = new StringBuilder();
 	    	sb.append("<div class='$templateClass$' id='$templateId$'> $templateDesign$")
 	    	.append("<input type='hidden' name='objectClass' value='$className$'/></div>");
 	    	
 	    	return sb.toString();
 	    }
 	
 	/**
 	 * constructMapForDynVals
 	 * @param appInfo
 	 * @param watcherMap
 	 * @param parameterKey
 	 * @return
 	 */
 	private Map<String, Object> constructMapForDynVals(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, String parameterKey, String customerId, String buildNumber) {
         Map<String, Object> paramMap = new HashMap<String, Object>(8);
         DependantParameters dependantParameters = watcherMap.get(parameterKey);
         if (dependantParameters != null) {
             paramMap.putAll(getDependantParameters(dependantParameters.getParentMap(), watcherMap));
         }
         paramMap.put(DynamicParameter.KEY_APP_INFO, appInfo);
         paramMap.put(REQ_CUSTOMER_ID, customerId);
         if (StringUtils.isNotEmpty(buildNumber)) {
         	paramMap.put(DynamicParameter.KEY_BUILD_NO, buildNumber);
         }
 
         return paramMap;
     }
 	
 	/**
 	 * gets the DynamicPossibleValues
 	 * @param watcherMap
 	 * @param parameter
 	 * @return
 	 * @throws PhrescoException
 	 */
 	private List<Value> getDynamicPossibleValues(Map<String, Object> watcherMap, Parameter parameter, String userId, String customerId) throws PhrescoException {
         PossibleValues possibleValue = getDynamicValues(watcherMap, parameter, userId, customerId);
         List<Value> possibleValues = (List<Value>) possibleValue.getValue();
         return possibleValues;
     }
 	
 	/**
 	 * gets the DynamicValues
 	 * @param watcherMap
 	 * @param parameter
 	 * @return
 	 * @throws PhrescoException
 	 */
 	private PossibleValues getDynamicValues(Map<String, Object> watcherMap, Parameter parameter, String userId, String customerId) throws PhrescoException {
 		try {
 			String className = parameter.getDynamicParameter().getClazz();
 			String grpId = parameter.getDynamicParameter().getDependencies().getDependency().getGroupId();
 			String artfId = parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId();
 			String jarVersion = parameter.getDynamicParameter().getDependencies().getDependency().getVersion();
 			DynamicParameter dynamicParameter;
 			PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(customerId + grpId + artfId + jarVersion);
 			if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
 				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
 			} else {
 				//To get repo info from Customer object
 				ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 				Customer customer = serviceManager.getCustomer(customerId);
 				RepoInfo repoInfo = customer.getRepoInfo();
 				//To set groupid,artfid,type infos to List<ArtifactGroup>
 				List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 				ArtifactGroup artifactGroup = new ArtifactGroup();
 				artifactGroup.setGroupId(grpId);
 				artifactGroup.setArtifactId(artfId);
 				artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
 				//to set version
 				List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		        ArtifactInfo artifactInfo = new ArtifactInfo();
 		        artifactInfo.setVersion(jarVersion);
 				artifactInfos.add(artifactInfo);
 		        artifactGroup.setVersions(artifactInfos);
 				artifactGroups.add(artifactGroup);
 				
 				//dynamically loads specified Class
 				phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
 				pdlMap.put(customerId + grpId + artfId + jarVersion, phrescoDynamicLoader);
 			}
 			
 			return dynamicParameter.getValues(watcherMap);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * To addValueDependToWatcher
 	 * @param watcherMap
 	 * @param parameterKey
 	 * @param values
 	 * @param previousValue
 	 */
 	private void addValueDependToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, List<Value> values, String previousValue) {
 		for (Value value : values) {
 		    if (StringUtils.isNotEmpty(value.getDependency())) {
 		    	if (StringUtils.isNotEmpty(previousValue)) {
 		    		addWatcher(watcherMap, value.getDependency(), parameterKey, previousValue);
 		    	} else {
 		    		addWatcher(watcherMap, value.getDependency(), parameterKey, value.getKey());
 		    	}
 		    }
 		}
 	}
 	
 	/**
 	 * To addWatcher
 	 * @param watcherMap
 	 * @param dependency
 	 * @param parameterKey
 	 * @param parameterValue
 	 */
 	private void addWatcher(Map<String, DependantParameters> watcherMap, String dependency, String parameterKey, String parameterValue) {
         if (StringUtils.isNotEmpty(dependency)) {
             List<String> dependencyKeys = Arrays.asList(dependency.split(CSV_PATTERN));
             for (String dependentKey : dependencyKeys) {
             	DependantParameters dependantParameters;
                 if (watcherMap.containsKey(dependentKey)) {
                     dependantParameters = (DependantParameters) watcherMap.get(dependentKey);
                 } else {
                     dependantParameters = new DependantParameters();
                 }
                 dependantParameters.getParentMap().put(parameterKey, parameterValue);
                 watcherMap.put(dependentKey, dependantParameters);
             }
         }
        
         addParentToWatcher(watcherMap, parameterKey, parameterValue);
     }
 	
 	/**
 	 * To addParentToWatcher
 	 * @param watcherMap
 	 * @param parameterKey
 	 * @param parameterValue
 	 */
 	private void addParentToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, String parameterValue) {
 
 		DependantParameters dependantParameters;
 		if (watcherMap.containsKey(parameterKey)) {
 			dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
 		} else {
 			dependantParameters = new DependantParameters();
 		}
 		dependantParameters.setValue(parameterValue);
 		watcherMap.put(parameterKey, dependantParameters);
 	}
 	
 	/**
 	 * Gets the dependent parameter
 	 * @param parentMap
 	 * @param watcherMap
 	 * @return
 	 */
 	private Map<String, Object> getDependantParameters(Map<String, String> parentMap, Map<String, DependantParameters> watcherMap) {
         Map<String, Object> paramMap = new HashMap<String, Object>(8);
         Set<String> keySet = parentMap.keySet();
         for (String key : keySet) {
             if (watcherMap.get(key) != null) {
                 String value = ((DependantParameters) watcherMap.get(key)).getValue();
                 paramMap.put(key, value);
             }
         }
         return paramMap;
     }
 
 	/**
 	 * Gets the info file dir.
 	 *
 	 * @param appDirName the app dir name
 	 * @param goal the goal
 	 * @param phase the phase
 	 * @return the info file dir
 	 */
 	private String getInfoFileDir(String appDirName, String goal, String phase) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(Utility.getProjectHome()).append(appDirName).append(File.separatorChar).append(
 				Constants.DOT_PHRESCO_FOLDER).append(File.separatorChar);
 		if (StringUtils.isNotEmpty(phase)) {
 			sb.append(Constants.PHRESCO + HYPHEN + phase + Constants.INFO_XML);
 		} else {
 			sb.append(Constants.PHRESCO + HYPHEN + goal + Constants.INFO_XML);
 		}
 		return sb.toString();
 	}
 }
