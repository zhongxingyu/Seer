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
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import org.apache.commons.lang.*;
 import org.apache.log4j.*;
 
 import com.photon.phresco.commons.model.*;
 import com.photon.phresco.exception.*;
 import com.photon.phresco.framework.*;
 import com.photon.phresco.framework.api.*;
 import com.photon.phresco.framework.commons.*;
 import com.photon.phresco.framework.model.*;
 import com.photon.phresco.framework.param.impl.*;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.*;
 import com.photon.phresco.util.*;
 import com.phresco.pom.util.*;
 
 public class Code extends DynamicParameterAction implements Constants {
 	private static final long serialVersionUID = 8217209827121703596L;
     private static final Logger S_LOGGER = Logger.getLogger(Code.class);
     private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
     
 	private String skipTest = null;
     private String codeTechnology = null;
     private String report = null;
     private String validateAgainst = null;
 	private String target = null;
 	
 	/*
 	 * populate drop down with targets or list of code validation(js, web)
 	 */
 	public String code() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Code.code()");
 		}
 		try {
         	ApplicationInfo appInfo = getApplicationInfo();
         	setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         	setReqAttribute(REQ_APP_INFO, appInfo);
         	
 			File pomPath = getPOMFile();
 			PomProcessor pomProcessor = new PomProcessor(pomPath);
 			String validateReportUrl = pomProcessor.getProperty(PHRESCO_CODE_VALIDATE_REPORT);
 			
 			// when the report url is not available, it is for sonar
 			// if the report url is available, it is for clang report(iphone)
 			if (StringUtils.isNotEmpty(validateReportUrl)) {
 				setReqAttribute(CLANG_REPORT, validateReportUrl);
 				List<Value> values = getClangReports(appInfo);
 				setReqAttribute(REQ_VALUES, values);
 			} else {
 	        	MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(appInfo)));
 				Parameter parameter = mojo.getParameter(PHASE_VALIDATE_CODE, SONAR);
 				PossibleValues possibleValues = parameter.getPossibleValues();
 				List<Value> values = possibleValues.getValue();
 				setReqAttribute(REQ_VALUES, values);
 				setSonarServerStatus();
 			}
     	} catch (Exception e) {
     		if (debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
    		new LogErrorReport(e, "Code code()");
         }
 		return APP_CODE;
 	}
 
 	private File getPOMFile() throws PhrescoException {
 		StringBuilder builder = new StringBuilder(getApplicationHome());
 		builder.append(File.separator);
 		builder.append(POM_XML);
 		File pomPath = new File(builder.toString());
 		return pomPath;
 	}
 
 	private void setSonarServerStatus() throws PhrescoException {
 		String serverUrl;
 		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 		serverUrl = frameworkUtil.getSonarURL();
 		if (debugEnabled) {
 			S_LOGGER.debug("sonar home url to check server status " + serverUrl);
 		}
 		try {
 			URL sonarURL = new URL(serverUrl);
 			HttpURLConnection connection = null;
 			connection = (HttpURLConnection) sonarURL.openConnection();
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				setReqAttribute(REQ_ERROR, getText(SONAR_NOT_STARTED));
 		    }
 		} catch(Exception e) {
 			setReqAttribute(REQ_ERROR, getText(SONAR_NOT_STARTED));
 		}
 	}
 
 	private List<Value> getClangReports(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			IosTargetParameterImpl targetImpl = new IosTargetParameterImpl();
 			Map<String, Object> paramMap = new HashMap<String, Object>();
 			paramMap.put(KEY_APP_INFO, appInfo);
 			PossibleValues possibleValues = targetImpl.getValues(paramMap);
 			List<Value> values = possibleValues.getValue();
 			return values;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/*
 	 * show code validation report
 	 */
 	public String check() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Code.check()");
 		}
 		StringBuilder sb = new StringBuilder();
     	try {
 	        Properties sysProps = System.getProperties();
 	        if (debugEnabled) {
 	        	S_LOGGER.debug( "Phresco FileServer Value of " + PHRESCO_FILE_SERVER_PORT_NO + " is " + sysProps.getProperty(PHRESCO_FILE_SERVER_PORT_NO) );
 			}
 	        String phrescoFileServerNumber = sysProps.getProperty(PHRESCO_FILE_SERVER_PORT_NO);
             FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
             ApplicationInfo applicationInfo = getApplicationInfo();
             
         	File pomPath = getPOMFile();
             PomProcessor processor = new PomProcessor(pomPath);
             String validateAgainst = getReqParameter(REQ_VALIDATE_AGAINST); //getHttpRequest().getParameter("validateAgainst");
             String validateReportUrl = processor.getProperty(PHRESCO_CODE_VALIDATE_REPORT);
             
             //Check whether iphone Technology or not
 			if (StringUtils.isNotEmpty(validateReportUrl)) {
             	StringBuilder codeValidatePath = new StringBuilder(getApplicationHome());
             	codeValidatePath.append(validateReportUrl);
             	codeValidatePath.append(validateAgainst);
             	codeValidatePath.append(File.separatorChar);
             	codeValidatePath.append(INDEX_HTML);
                 File indexPath = new File(codeValidatePath.toString());
                 S_LOGGER.debug("indexPath ..... " + indexPath);
              	if (indexPath.isFile() && StringUtils.isNotEmpty(phrescoFileServerNumber)) {
                 	sb.append(HTTP_PROTOCOL);
                 	sb.append(PROTOCOL_POSTFIX);
                 	InetAddress thisIp =InetAddress.getLocalHost();
                 	sb.append(thisIp.getHostAddress());
                 	sb.append(COLON);
                 	sb.append(phrescoFileServerNumber);
                 	sb.append(FORWARD_SLASH);
                 	sb.append(codeValidatePath.toString().replace(File.separator, FORWARD_SLASH));
              	} else {
              		setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
              	}
              	setReqAttribute(CLANG_REPORT, validateReportUrl);
         	} else {
 	        	String serverUrl = "";
 	    		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 	    		serverUrl = frameworkUtil.getSonarHomeURL();
 				StringBuilder reportPath = new StringBuilder(getApplicationHome());
 
 				if (StringUtils.isNotEmpty(validateAgainst) && FUNCTIONALTEST.equals(validateAgainst)) {
 					reportPath.append(frameworkUtil.getFunctionalTestDir(applicationInfo));
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
 	        	sb.append(COLON);
 	        	sb.append(artifactId);
 	        	
 	        	if (StringUtils.isNotEmpty(validateAgainst) && !REQ_SRC.equals(validateAgainst)) {
 	        		sb.append(COLON);
 	        		sb.append(validateAgainst);
 	        	}
 	    		try {
 	    			if (debugEnabled) {
 	    				S_LOGGER.debug("Url to access API " + sb.toString());
 	    			}
 					URL sonarURL = new URL(sb.toString());
 					HttpURLConnection connection = (HttpURLConnection) sonarURL.openConnection();
 					int responseCode = connection.getResponseCode();
 					if (debugEnabled) {
 	    				S_LOGGER.debug("Response code value " + responseCode);
 		    		}
 					if (responseCode != 200) {
 						setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
 					    return APP_CODE;
 		            }
 				} catch (Exception e) {
 					if (debugEnabled) {
 						S_LOGGER.error("Entered into catch block of Code.check()"+ FrameworkUtil.getStackTraceAsString(e));
 		    		}
 					new LogErrorReport(e, "Code review");
 					setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
 					return APP_CODE;
 				}
         	}
     	} catch (Exception e) {
     		if (debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.check()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
     	}
     	if (debugEnabled) {
     		S_LOGGER.debug("Sonar final report path " + sb.toString());
     	}
     	setReqAttribute(REQ_SONAR_PATH, sb.toString());
         return APP_CODE;
     }
 	
 	/*
 	 * code validate popup
 	 */
 	public String showCodeValidatePopup() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Code.showCodeValidatePopup()");
 		}
 		try {
 		    ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_VALIDATE_CODE + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
             
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(getApplicationInfo())));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_VALIDATE_CODE);
             
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap);
             setSessionAttribute(appInfo.getId() + PHASE_VALIDATE_CODE + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_VALIDATE_CODE);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Code.showCodeValidatePopup()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 		return SHOW_CODE_VALIDATE_POPUP;
 	}
 	
 	private void setProjModulesInReq() throws PhrescoException {
         List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
         setReqAttribute(REQ_PROJECT_MODULES, projectModules);
     }
 	
 	/*
 	 * code validate progress
 	 */
 	public String codeValidate() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method Code.codeValidate()");
 		}
 		try {
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(applicationInfo)));
 			persistValuesToXml(mojo, PHASE_VALIDATE_CODE);
 			
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_VALIDATE_CODE);
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.CODE_VALIDATE, buildArgCmds, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_CODE, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_CODE);
 		} catch (PhrescoException e) {
     		if (debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
    		new LogErrorReport(e, "Code code()");
 		}
 		
 		return APP_ENVIRONMENT_READER;
 	}
 	
 	public String getCodeTechnology() {
 		return codeTechnology;
 	}
 
 	public void setCodeTechnology(String codeTechnology) {
 		this.codeTechnology = codeTechnology;
 	}
 
 	public String getReport() {
 		return report;
 	}
 
 	public void setReport(String report) {
 		this.report = report;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 	
 	public String getSkipTest() {
 		return skipTest;
 	}
 
 	public void setSkipTest(String skipTest) {
 		this.skipTest = skipTest;
 	}
 	
 	public String getValidateAgainst() {
 		return validateAgainst;
 	}
 
 	public void setValidateAgainst(String validateAgainst) {
 		this.validateAgainst = validateAgainst;
 	}
 }
