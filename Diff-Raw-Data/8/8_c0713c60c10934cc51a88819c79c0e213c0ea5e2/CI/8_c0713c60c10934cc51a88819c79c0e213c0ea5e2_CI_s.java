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
 
 import static com.photon.phresco.util.Constants.PHASE_FUNCTIONAL_TEST;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.quartz.CronExpression;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import com.photon.phresco.commons.CIPasswordScrambler;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.CIManager;
 import com.photon.phresco.framework.commons.ApplicationsUtil;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.CIBuild;
 import com.photon.phresco.framework.model.CIJob;
 import com.photon.phresco.framework.model.CIJobStatus;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 
 public class CI extends DynamicParameterAction implements FrameworkConstants {
 
 	private static final long serialVersionUID = -2040671011555139339L;
 	private static final Logger S_LOGGER = Logger.getLogger(CI.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 
 	private String name = null;
 	private String svnurl = null;
 	private String username = null;
 	private String password = null;
 	private String[] emails = null;
 	private String successEmailIds = null;
 	private String failureEmailIds = null;
 	private String schedule = null;
 	private String cronExpression = null;
 	private String email = null;
 	private String buildDownloadUrl = null;
 	private InputStream fileInputStream;
 	private String fileName = "";
 	private String senderEmailId = null;
 	private String senderEmailPassword = null;
 	private int totalBuildSize;
 	boolean buildInProgress = false;
 
 	private List<String> triggers = null;
 	private String buildNumber = null;
 	CIJob job = null;
 	private String oldJobName = null;
 	private int numberOfJobsInProgress = 0;
 	private String downloadJobName = null;
 	private String svnType = null;
 	private String branch = null;
 	private String localJenkinsAlive = "";
 
 	// collabNet implementation
 	private boolean enableBuildRelease = false;
 	private String collabNetURL = "";
 	private String collabNetusername = "";
 	private String collabNetpassword = "";
 	private String collabNetProject = "";
 	private String collabNetPackage = "";
 	private String collabNetRelease = "";
 	private boolean collabNetoverWriteFiles = false;
 
 	// Test automation in jenkins
 	private String usedClonnedWorkspace = "";
 	private String operation = "";
 	private String upstreamProject = "";
 	private String downstreamProject = "";
 	private String downstreamCriteria = "";
 	// whether we want to clone this workspace
 	private boolean cloneWorkspace = false;
 	private String projectModule = "";
 	
     // pdf report generation
     private String reportType = "";
     private String testType = "";
     private String theme = "";
     private String logo = "";
     private String sonarUrl = "";
     
 	public String ci() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method CI.ci()");
 		}	
 		try {
 		    removeSessionAttribute(getAppId() + SESSION_APPINFO);//To remove the appInfo from the session
 			boolean jenkinsAlive = false;
 			// Other methods like build() will call this after triggered build,
 			// it will set trigger_from_ui=true, we need to set value when this
 			// variable is empty
 			// UI didnt trigger anybuild from here
 			String buildTriggeredVal = (String) getHttpRequest().getAttribute(CI_BUILD_TRIGGERED_FROM_UI);
 			if (debugEnabled) {
 				S_LOGGER.debug("buildTriggeredVal " + buildTriggeredVal);
 			}
 			if (StringUtils.isEmpty(buildTriggeredVal)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Build triggered set to false ");
 				}
 				setReqAttribute(CI_BUILD_TRIGGERED_FROM_UI, FALSE);
 			}
 			if (debugEnabled) {
 				S_LOGGER.debug("buildTriggeredVal in Ci " + getHttpRequest().getAttribute(CI_BUILD_TRIGGERED_FROM_UI));
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			
 			setReqAttribute(REQ_APPINFO, appInfo);
 			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
 			setReqAttribute(REQ_PROJECT_ID, getProjectId());
 			setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			setReqAttribute(CI_JENKINS_ALIVE, jenkinsAlive);
 
 			jenkinsAlive = Utility.isConnectionAlive(HTTP_PROTOCOL, LOCALHOST,	Integer.parseInt(getPortNo(Utility.getJenkinsHome())));
 			if (debugEnabled) {
 				S_LOGGER.debug("jenkins Alive " + jenkinsAlive);
 			}
 			setReqAttribute(CI_JENKINS_ALIVE, jenkinsAlive);
 			if (debugEnabled) {
 				S_LOGGER.debug("get jobs called in CI ");
 			}
 			List<CIJob> existingJobs = ciManager.getJobs(appInfo);
 			if (CollectionUtils.isNotEmpty(existingJobs)) {
 				Collections.sort(existingJobs, sortJobsInAlphaOrder());
 			}
 			
 			if (debugEnabled) {
 				S_LOGGER.debug("Return jobs got in CI ");
 			}
 			Map<String, List<CIBuild>> ciJobsAndBuilds = new HashMap<String, List<CIBuild>>();
 			if (existingJobs != null) {
 				for (CIJob ciJob : existingJobs) {
 					boolean buildJenkinsAlive = false;
 					boolean isJobCreatingBuild = false;
 					List<CIBuild> builds = null;
 					buildInProgress = false;
 					buildJenkinsAlive = Utility.isConnectionAlive(HTTP_PROTOCOL, ciJob.getJenkinsUrl(), Integer.parseInt(ciJob.getJenkinsPort()));
 					isJobCreatingBuild = ciManager.isJobCreatingBuild(ciJob);
 					if (debugEnabled) {
 						S_LOGGER.debug("ciJob.getName() ... " + ciJob.getName());
 						S_LOGGER.debug("ciJob.getName() alive ... " + buildJenkinsAlive);
 						S_LOGGER.debug("ciJob.getName() isJobCreatingBuild .... " + isJobCreatingBuild);
 					}
 					setReqAttribute(CI_BUILD_JENKINS_ALIVE + ciJob.getName(), buildJenkinsAlive);
 					setReqAttribute(CI_BUILD_IS_IN_PROGRESS + ciJob.getName(), isJobCreatingBuild);
 					if (buildJenkinsAlive == true) {
 						builds = ciManager.getBuilds(ciJob);
 					}
 					if (debugEnabled) {
 						S_LOGGER.debug("ciJob.getName() builds .... " + builds);
 					}
 					ciJobsAndBuilds.put(ciJob.getName(), builds);
 				}
 			}
 			setReqAttribute(REQ_EXISTING_JOBS, ciJobsAndBuilds);
 			numberOfJobsIsInProgress();
 			if (debugEnabled) {
 				S_LOGGER.debug("numberOfJobsInProgress " + numberOfJobsInProgress);
 			}
 			setReqAttribute(CI_NO_OF_JOBS_IN_PROGRESS, numberOfJobsInProgress);
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			setReqAttribute(REQ_FUNCTEST_SELENIUM_TOOL, frameworkUtil.getSeleniumToolType(appInfo));
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.ci()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CI_JOB_LIST));
 		} catch (PhrescoPomException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.ci()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CI_JOB_LIST));
 		}
 		return APP_CI;
 	}
 	
 	private Comparator sortJobsInAlphaOrder() {
 		return new Comparator() {
 		    public int compare(Object firstObject, Object secondObject) {
 		    	CIJob job1 = (CIJob) firstObject;
 		    	CIJob job2 = (CIJob) secondObject;
 		       return job1.getName().compareToIgnoreCase(job2.getName());
 		    }
 		};
 	}
 
 	public String configure() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.configure()");
 		}
 		try {
 			String[] selectedJobsName = getHttpRequest().getParameterValues(REQ_SELECTED_JOBS_LIST);
 			String jobName = "";
 			if (!ArrayUtils.isEmpty(selectedJobsName)) {
 				jobName = selectedJobsName[0];
 			}
 			if (debugEnabled) {
 				S_LOGGER.debug("selectedJobs for configuration " + jobName);
 			}
 
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			setReqAttribute(REQ_APPINFO, appInfo);
 			
 			List<String> clonedWorkspaces = null;
 			List<String> existingJobsNames = null;
 			CIJob existJob = ciManager.getJob(appInfo, jobName);
 			List<CIJob> existJobs = ciManager.getJobs(appInfo);
 			if (CollectionUtils.isNotEmpty(existJobs)) {
 				clonedWorkspaces = new ArrayList<String>(existJobs.size());
 				existingJobsNames = new ArrayList<String>(existJobs.size());
 				for (CIJob ciJob : existJobs) {
 					if (debugEnabled) {
 						S_LOGGER.debug("Exist jobs size ... " + existJobs.size());
 					}
 					if (ciJob.isCloneWorkspace()) {
 						if (debugEnabled) {
 							S_LOGGER.debug("Cloned names .... " + ciJob.getName());
 						}
 						clonedWorkspaces.add(ciJob.getName());
 					}
 					if (debugEnabled) {
 						S_LOGGER.debug("existJob names in code .... " + existJob);
 					}
 					existingJobsNames.add(ciJob.getName());
 				}
 			}
 			if (existJob != null && StringUtils.isNotEmpty(existJob.getCollabNetpassword())) {
 				existJob.setCollabNetpassword(CIPasswordScrambler.unmask(existJob.getCollabNetpassword()));
 			}
 			
 			if (existJob != null) {
 				existJob.setPassword(CIPasswordScrambler.unmask(existJob.getPassword()));
 				
 				// restore values in popup
 				restoreValues(existJob, appInfo);
 			}
 			setReqAttribute(REQ_EXISTING_JOB, existJob);
 			setReqAttribute(REQ_EXISTING_JOBS_NAMES, existingJobsNames);
 			setReqAttribute(REQ_EXISTING_CLONNED_JOBS, clonedWorkspaces);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.configure()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CI_CONFIGURE_POPUP));
 		}
 		return APP_CI_CONFIGURE;
 	}
 
 	private void restoreValues(CIJob ciJob, ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			// restore values
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 			String phase = "";
 			String operation = ciJob.getOperation();
 			if (BUILD.equals(operation)) {
 				phase = PHASE_PACKAGE;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        } else if (DEPLOY.equals(operation)) { 
 	        	phase = PHASE_DEPLOY;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        } else if (PDF_REPORT.equals(operation)) { 
 	        	phase = PHASE_PDF_REPORT;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        } else if (CODE_VALIDATION.equals(operation)) { 
 	        	phase = PHASE_VALIDATE_CODE;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        } else if (UNIT_TEST.equals(operation)) { 
 	        	phase = PHASE_UNIT_TEST;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        } else if (FUNCTIONAL_TEST.equals(operation)) {
 				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				String seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
 				phase =  PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType;
 	        	restoreValuesToXml(mojo, phase, ciJob);
 	        }
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void restoreValuesToXml(MojoProcessor mojo, String phase, CIJob ciJob) throws PhrescoException {
 	    try {
 	        List<Parameter> parameters = getMojoParameters(mojo, phase);
 	        if (CollectionUtils.isNotEmpty(parameters)) {
 	        	BeanUtils bu = new BeanUtils();
 	            for (Parameter parameter : parameters) {
 	                if (parameter != null) {
 	                	String value = bu.getProperty(ciJob, parameter.getKey());
 	                	if (StringUtils.isNotEmpty(value)) {
 	                		parameter.setValue(value);
 	                	}
 	                }
 	            }
 	        }
 	        mojo.save();
 	    } catch (Exception e) {
 	        throw new PhrescoException(e);
 	    }
 	}
 	
 	public String dynamicOperationPopup() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.dynamicOperationPopup()");
 			S_LOGGER.debug("operation " + operation);
 		}
 		try {
 		    ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_CI + SESSION_WATCHER_MAP);
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
             List<Parameter> parameters = null;
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_CI)));
             String goal = "";
             // based on operation it should do it
             if (BUILD.equals(operation)) {
             	setProjModulesInReq();
                 parameters = getMojoParameters(mojo, PHASE_PACKAGE);
                 goal = PHASE_PACKAGE;
                 setReqAttribute(REQ_PHASE, PHASE_CI);
                 setReqAttribute(REQ_GOAL, goal);
             } else if (DEPLOY.equals(operation)) {
                 parameters = getMojoParameters(mojo, PHASE_DEPLOY);
                 setReqAttribute(REQ_PHASE, PHASE_CI);
                 goal = PHASE_DEPLOY;
                 setReqAttribute(REQ_GOAL, goal);
             } else if (PDF_REPORT.equals(operation)) {
                 parameters = getMojoParameters(mojo, PHASE_PDF_REPORT);
                 setReqAttribute(REQ_PHASE, PHASE_CI);
                 goal = PHASE_PDF_REPORT;
                 setReqAttribute(REQ_GOAL, goal);
             } else if (CODE_VALIDATION.equals(operation)) {
                 parameters = getMojoParameters(mojo, PHASE_VALIDATE_CODE);
                 setReqAttribute(REQ_PHASE, PHASE_CI);
                 goal = PHASE_VALIDATE_CODE;
                 setReqAttribute(REQ_GOAL, goal);
             } else if (UNIT_TEST.equals(operation)) {
             	setProjModulesInReq();
                 parameters = getMojoParameters(mojo, PHASE_UNIT_TEST);
                 setReqAttribute(REQ_PHASE, PHASE_CI);
                 goal = PHASE_UNIT_TEST;
                 setReqAttribute(REQ_GOAL, goal);
             } else if (FUNCTIONAL_TEST.equals(operation)) {
                 FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
                 String seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
                 parameters = getMojoParameters(mojo, PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 	            setReqAttribute(REQ_PHASE, PHASE_CI);
 	            goal = PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType;
 	            setReqAttribute(REQ_GOAL, goal);
             }
             
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, goal);
             setSessionAttribute(appInfo.getId() + PHASE_CI + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_APP_INFO, appInfo);
 		} catch (Exception e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_BUILD_POPUP));
 		}
 		return SUCCESS;
 	}
 	
 	private void constructCiJobObj(MojoProcessor mojo, String goal, CIJob cijob) throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.constructCiJobObj()");
 			S_LOGGER.debug("goal " + goal);
 		}
 	    try {
 	    	BeanUtils bu = new BeanUtils();
 	        List<Parameter> parameters = getMojoParameters(mojo, goal);
 	        StringBuilder csParamVal = new StringBuilder();
 	        String sep = "";
 	        if (CollectionUtils.isNotEmpty(parameters)) {
 	            for (Parameter parameter : parameters) {
 	            	String key = parameter.getKey();
 	                if (Boolean.parseBoolean(parameter.getMultiple())) {
 	                    String[] parameterValues = getReqParameterValues(parameter.getKey());
 	                    for (String parameterValue : parameterValues) {
 	                        csParamVal.append(sep);
 	                        csParamVal.append(parameterValue);
 	                        sep = ",";
 	                    }
 	                    bu.setProperty(cijob, parameter.getKey(), csParamVal.toString());
 	                } else if (TYPE_BOOLEAN.equalsIgnoreCase(parameter.getType())) {
 	                    if (getReqParameter(parameter.getKey()) != null) {
 	                    	bu.setProperty(cijob, parameter.getKey(), getReqParameter(parameter.getKey()));
 	                    } else {
 	                        bu.setProperty(cijob, parameter.getKey(), Boolean.FALSE.toString());
 	                    }
 	                } else if (parameter.getType().equalsIgnoreCase(TYPE_MAP)) {
 	                    List<Child> childs = parameter.getChilds().getChild();
 	                    String[] keys = getReqParameterValues(childs.get(0).getKey());
 	                    String[] values = getReqParameterValues(childs.get(1).getKey());
 	                    Properties properties = new Properties();
 	                    for (int i = 0; i < keys.length; i++) {
 	                        properties.put(keys[i], values[i]);
 	                    }
 	                    StringWriter writer = new StringWriter();
 	                    properties.store(writer, "");
 	                    String value = writer.getBuffer().toString();
 	                    bu.setProperty(cijob, parameter.getKey(), value);
 	                } else if (parameter.getType().equalsIgnoreCase(TYPE_PACKAGE_FILE_BROWSE)) {
 	                	bu.setProperty(cijob, parameter.getKey(), getSelectedFiles());
 	                } else if (PDF_REPORT.equals(operation) && "sonarUrl".equals(key)) {
 	                	// for pdf report operation we have to assign our own values for few parameters
 	    				bu.setProperty(cijob, parameter.getKey(), sonarUrl);
 	                } else if (PDF_REPORT.equals(operation) && "logo".equals(key)) {
 	                	// for pdf report operation we have to assign our own values for few parameters
 	    				bu.setProperty(cijob, parameter.getKey(), logo);
 	                } else if (PDF_REPORT.equals(operation) && "theme".equals(key)) {
 	                	// for pdf report operation we have to assign our own values for few parameters
 	    				bu.setProperty(cijob, parameter.getKey(), theme);
 	                } else {
 	                    bu.setProperty(cijob, parameter.getKey(), getReqParameter(parameter.getKey()));
                 	}
 	            }
 	        }
 	    } catch (Exception e) {
 	    	throw new PhrescoException(e);
 	    }
 	}
 	
 	private void setProjModulesInReq() throws PhrescoException {
         List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
         setReqAttribute(REQ_PROJECT_MODULES, projectModules);
     }
 	
 	public String setup() {
 		S_LOGGER.debug("Entering Method  CI.setup()");
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			if (debugEnabled) {
 				S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
 			}
 
 			List<String> buildArgCmds = new ArrayList<String>(1);
 			buildArgCmds.add(SKIP_TESTS);
 			String workingDirectory = Utility.getJenkinsHome();
 			BufferedReader reader = ciManager.setup(projectInfo, ActionType.INSTALL, buildArgCmds , workingDirectory);
 			
 			setSessionAttribute(getAppId() + CI_SETUP, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, CI_SETUP);
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.setup()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 		return APP_ENVIRONMENT_READER;
 	}
 
 	public String save() {
 		if (debugEnabled) {
 			S_LOGGER.debug("clonedWorkspace ... " + svnType);
 			S_LOGGER.debug("usedClonnedWorkspace " + usedClonnedWorkspace);
 			S_LOGGER.debug("operation " + operation);
 			S_LOGGER.debug("upstreamProject " + upstreamProject);
 			S_LOGGER.debug("downstreamProject  " + downstreamProject);
 			S_LOGGER.debug("cloneWorkspace " + cloneWorkspace);
 		}
 		return doUpdateSave(CI_CREATE_JOB_COMMAND);
 	}
 
 	public String update() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.update()");
 		}
 		return doUpdateSave(CI_UPDATE_JOB_COMMAND);
 	}
 
 	public String doUpdateSave(String jobType) {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.doUpdateSave()");
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			CIJob existJob = null;
 			if (StringUtils.isNotEmpty(oldJobName)) {
 				existJob = ciManager.getJob(appInfo, oldJobName);
 			}
 			if (existJob == null) {
 				existJob = new CIJob();
 			}
 			InetAddress thisIp = InetAddress.getLocalHost();
 			// basic information
 			existJob.setName(name);
 			existJob.setSvnUrl(svnurl);
 			existJob.setUserName(username);
 			existJob.setPassword(CIPasswordScrambler.mask(password));
 			existJob.setJenkinsUrl(thisIp.getHostAddress());
 			existJob.setJenkinsPort(getPortNo(Utility.getJenkinsHome()));
 			existJob.setTriggers(triggers);
 			Map<String, String> emails = new HashMap<String, String>(2);
 			emails.put(REQ_KEY_SUCCESS_EMAILS, successEmailIds);
 			emails.put(REQ_KEY_FAILURE_EMAILS, failureEmailIds);
 			existJob.setEmail(emails);
 			existJob.setScheduleType(schedule);
 			existJob.setScheduleExpression(cronExpression);
 			existJob.setSenderEmailId(senderEmailId);
 			existJob.setSenderEmailPassword(senderEmailPassword);
 			existJob.setBranch(branch);
 			existJob.setRepoType(svnType);
 			
 			// collabNet file release plugin imple
 			existJob.setEnableBuildRelease(enableBuildRelease);
 			existJob.setCollabNetURL(collabNetURL);
 			existJob.setCollabNetusername(collabNetusername);
 			existJob.setCollabNetpassword(CIPasswordScrambler.mask(collabNetpassword));
 			existJob.setCollabNetProject(collabNetProject);
 			existJob.setCollabNetPackage(collabNetPackage);
 			existJob.setCollabNetRelease(collabNetRelease);
 			existJob.setCollabNetoverWriteFiles(collabNetoverWriteFiles);
 
 			// Automate values
 			existJob.setCloneWorkspace(cloneWorkspace);
 			existJob.setUsedClonnedWorkspace(usedClonnedWorkspace);
 			existJob.setDownStreamProject(downstreamProject);
 			existJob.setDownStreamCriteria(downstreamCriteria);
 			existJob.setOperation(operation);
 			existJob.setProjectModule(projectModule);
 			
 			// pdf report changes
 			existJob.setSonarUrl(sonarUrl);
 			existJob.setLogo(logo);
 			existJob.setTheme(theme);
 			
 			// Build info
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			List<String> preBuildStepCmds = new ArrayList<String>();
 			
 			List<Parameter> parameters = null;
 			String mvncmd = "";
 			if (debugEnabled) {
 				S_LOGGER.debug("Operation .... " + operation);
 			}
 			
 			if (BUILD.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Build operation!!!!!!");
 				}
 				// enable archiving
 				existJob.setEnableArtifactArchiver(true);
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				persistValuesToXml(mojo, Constants.PHASE_PACKAGE);
 				constructCiJobObj(mojo, Constants.PHASE_PACKAGE, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_PACKAGE);
 				ActionType actionType = ActionType.BUILD;
 				mvncmd =  actionType.getActionType().toString();
 				String buildPrebuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_PACKAGE;
 				// To handle multi module project
 				buildPrebuildCmd = buildPrebuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(buildPrebuildCmd);
 			} else if (DEPLOY.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Deploy operation!!!!!!");
 				}
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				persistValuesToXml(mojo, Constants.PHASE_DEPLOY);
 				constructCiJobObj(mojo, Constants.PHASE_DEPLOY, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_DEPLOY);
 				ActionType actionType = ActionType.DEPLOY;
 				mvncmd =  actionType.getActionType().toString();
 				
 				String deployPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_DEPLOY;
 				// To handle multi module project
 				deployPreBuildCmd = deployPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(deployPreBuildCmd);
 			} else if (PDF_REPORT.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("PDF report operation!!!!!!");
 				}
 				// for pdf report attachment patterns
 				// based on test report type, it should be specified
 				String testTypeParam = getReqParameter("testType");
 				String attacheMentPattern = "";
 				if ("All".equals(testTypeParam)) {
 					attacheMentPattern = "cumulativeReports";
 				} else {
 					attacheMentPattern = testTypeParam;
 				}
 				existJob.setAttachmentsPattern("do_not_checkin/archives/" + attacheMentPattern + "/*.pdf"); //do_not_checkin/archives/cumulativeReports/*.pdf
 				
 				// here we can set necessary values in request and we can change object value as well...
 				// getting sonar url
 				String usingSonarUrl = "";
 				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				usingSonarUrl = frameworkUtil.getSonarURL();
 				// logo and theme
 				String logoImageString = getLogoImageString();
 				String themeColorJson = getThemeColorJson();
 				
 				// object change
 				existJob.setSonarUrl(usingSonarUrl);
 				existJob.setLogo(logoImageString);
 				existJob.setTheme(themeColorJson);
 				
 				// set values in request
 				sonarUrl = usingSonarUrl;
 				logo = logoImageString;
 				theme = themeColorJson;
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				persistValuesToXml(mojo, Constants.PHASE_PDF_REPORT);
 				constructCiJobObj(mojo, Constants.PHASE_PDF_REPORT, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_PDF_REPORT);
 				ActionType actionType = ActionType.PDF_REPORT;
 				mvncmd =  actionType.getActionType().toString();
 				
 				String deployPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_PDF_REPORT;
 				// To handle multi module project
 				deployPreBuildCmd = deployPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(deployPreBuildCmd);
 			} else if (CODE_VALIDATION.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("CodeValidation operation!!!!!!");
 				}
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				persistValuesToXml(mojo, Constants.PHASE_VALIDATE_CODE);
 				constructCiJobObj(mojo, Constants.PHASE_VALIDATE_CODE, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_VALIDATE_CODE);
 				ActionType actionType = ActionType.CODE_VALIDATE;
 				mvncmd =  actionType.getActionType().toString();
 				
 				String deployPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_VALIDATE_CODE;
 				// To handle multi module project
 				deployPreBuildCmd = deployPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(deployPreBuildCmd);
 			} else if (UNIT_TEST.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Unit test operation!!!!!!");
 				}
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				persistValuesToXml(mojo, Constants.PHASE_UNIT_TEST);
 				constructCiJobObj(mojo, Constants.PHASE_UNIT_TEST, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_UNIT_TEST);
 				ActionType actionType = ActionType.UNIT_TEST;
 				mvncmd =  actionType.getActionType().toString();
 				
 				String unitTestPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_UNIT_TEST;
 				// To handle multi module project
 				unitTestPreBuildCmd = unitTestPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(unitTestPreBuildCmd);
 			} else if (FUNCTIONAL_TEST.equals(operation)) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Functional test operation!!!!!");
 				}
 				
 				MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(Constants.PHASE_CI)));
 				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				String seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
 				if (debugEnabled) {
 					S_LOGGER.debug("functional test type " + Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 				}
 				persistValuesToXml(mojo, Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 				constructCiJobObj(mojo, Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType, existJob);
 				
 				//To get maven build arguments
 				parameters = getMojoParameters(mojo, Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 				ActionType actionType = ActionType.FUNCTIONAL_TEST;
 				mvncmd =  actionType.getActionType().toString();
 				String functionalTestPrebuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_FUNCTIONAL_TEST;
 				// To handle multi module project
 				functionalTestPrebuildCmd = functionalTestPrebuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(functionalTestPrebuildCmd);
 			}
 			
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			
 			if (!CollectionUtils.isEmpty(buildArgCmds)) {
 				for (String buildArgCmd : buildArgCmds) {
 					mvncmd = mvncmd + FrameworkConstants.SPACE + buildArgCmd;
 				}
 			}
 			
 			// for build job alone existing do_not_checkin need to be cleared
 			// For pdf report, it should clear existing pdf reports in do_not_checkin folder
			if ((!"clonedWorkspace".equals(svnType) && BUILD.equals(operation)) || PDF_REPORT.equals(operation)) {
 				mvncmd = CI_PROFILE + mvncmd;
 			}
 			
 			if (debugEnabled) {
 				S_LOGGER.debug("mvn command" + mvncmd);
 			}
 			
 			// To handle multi module project
 			mvncmd = mvncmd + FrameworkConstants.SPACE + HYPHEN_N;
 			existJob.setMvnCommand(mvncmd);
 			// prebuild step enable
 			existJob.setEnablePreBuildStep(true);
 			
 			existJob.setPrebuildStepCommands(preBuildStepCmds);
 			
 			// configure job here
 			if (CI_CREATE_JOB_COMMAND.equals(jobType)) {
 				ciManager.createJob(appInfo, existJob);
 				addActionMessage(getText(SUCCESS_JOB));
 			} else if (CI_UPDATE_JOB_COMMAND.equals(jobType)) {
 				ciManager.updateJob(appInfo, existJob);
 				addActionMessage(getText(SUCCESS_UPDATE));
 			}

 			restartJenkins(); // reload config
 
 			setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of CI.doUpdateSave()" + FrameworkUtil.getStackTraceAsString(e));
 			addActionMessage(getText(CI_SAVE_UPDATE_FAILED, e.getLocalizedMessage()));
 		}
 		return ci();
 	}
 
 	public String build() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.build()");
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			String[] selectedJobs = getHttpRequest().getParameterValues(REQ_SELECTED_JOBS_LIST);
 
 			CIJobStatus ciJobStatus = ciManager.buildJobs(appInfo, Arrays.asList(selectedJobs));
 			if (ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins build job status code " + ciJobStatus.getCode());
 				}
 				addActionError(getText(ciJobStatus.getMessage()));
 			} else {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins build job status code " + ciJobStatus.getCode());
 				}
 				addActionMessage(getText(ciJobStatus.getMessage()));
 			}
 			setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			setReqAttribute(CI_BUILD_TRIGGERED_FROM_UI, TRUE);
 		} catch (PhrescoException e) {
 			setReqAttribute(CI_BUILD_TRIGGERED_FROM_UI, FALSE);
 			S_LOGGER.error("Entered into catch block of CI.build()" + FrameworkUtil.getStackTraceAsString(e));
 			addActionMessage(getText(CI_BUILD_FAILED, e.getLocalizedMessage()));
 		}
 		return ci();
 	}
 
 	public String deleteCIBuild() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.deleteCIBuild()");
 		}
 		String[] selectedBuilds = getHttpRequest().getParameterValues(REQ_SELECTED_BUILDS_LIST);
 		if (debugEnabled) {
 			S_LOGGER.debug("selectedBuilds " + selectedBuilds.toString());
 		}
 		// job name and builds
 		Map<String, List<String>> buildsTobeDeleted = new HashMap<String, List<String>>();
 		for (String build : selectedBuilds) {
 			if (debugEnabled) {
 				S_LOGGER.debug("Build" + build);
 			}
 			String[] split = build.split(",");
 			if (debugEnabled) {
 				S_LOGGER.debug("split " + split[0]);
 			}
 			List<String> buildNumbers = new ArrayList<String>();
 			if (buildsTobeDeleted.containsKey(split[0])) {
 				List<String> listBuildNos = buildsTobeDeleted.get(split[0]);
 				listBuildNos.add(split[1]);
 				buildsTobeDeleted.put(split[0], listBuildNos);
 			} else {
 				buildNumbers.add(split[1]);
 				buildsTobeDeleted.put(split[0], buildNumbers);
 			}
 
 		}
 
 		Iterator iterator = buildsTobeDeleted.keySet().iterator();
 		while (iterator.hasNext()) {
 			String jobName = iterator.next().toString();
 			List<String> versions = buildsTobeDeleted.get(jobName);
 			if (debugEnabled) {
 				S_LOGGER.debug("jobName " + jobName + " builds " + versions);
 			}
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			CIJobStatus ciJobStatus = ciManager.deleteBuilds(appInfo, buildsTobeDeleted);
 			if (ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins delete build status code " + ciJobStatus.getCode());
 				}
 				addActionError(getText(ciJobStatus.getMessage()));
 			} else {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins delete build status code " + ciJobStatus.getCode());
 				}
 				addActionMessage(getText(ciJobStatus.getMessage()));
 			}
 			setReqAttribute(REQ_APPINFO, appInfo);
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of CI.deleteCIBuild()" + FrameworkUtil.getStackTraceAsString(e));
 			return showErrorPopup(e, getText(EXCEPTION_CI_BUILD_DELETION));
 		}
 		setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 		return ci();
 	}
 
 	public String deleteCIJob() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.deleteCIJob()");
 		}
 		String[] selectedJobs = getHttpRequest().getParameterValues(REQ_SELECTED_JOBS_LIST);
 		if (debugEnabled) {
 			S_LOGGER.debug("delete selectedJobs " + selectedJobs);
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			CIJobStatus ciJobStatus = ciManager.deleteJobs(appInfo, Arrays.asList(selectedJobs));
 			if (ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins delete job status code " + ciJobStatus.getCode());
 				}
 				addActionError(getText(ciJobStatus.getMessage()));
 			} else {
 				if (debugEnabled) {
 					S_LOGGER.debug("Jenkins delete job status code " + ciJobStatus.getCode());
 				}
 				addActionMessage(getText(ciJobStatus.getMessage()));
 			}
 			setReqAttribute(REQ_APPINFO, appInfo);
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of CI.deleteCIJob()" + FrameworkUtil.getStackTraceAsString(e));
 			return showErrorPopup(e, getText(EXCEPTION_CI_JOB_DELETION));
 		}
 		setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 		return ci();
 	}
 
 	public String start() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.startJenkins()");
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			if (debugEnabled) {
 				S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
 			}
 			
 			List<String> buildArgCmds = null;
 			String workingDirectory = Utility.getJenkinsHome();
 
 			BufferedReader reader = ciManager.start(projectInfo, ActionType.START, buildArgCmds , workingDirectory);
 			
 			setSessionAttribute(getAppId() + CI_START, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, CI_START);
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of CI.startJenkins()" + FrameworkUtil.getStackTraceAsString(e));
 		}
 		return APP_ENVIRONMENT_READER;
 	}
 
 	public String stop() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.stopJenkins()");
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ProjectInfo projectInfo = getProjectInfo();
 			if (debugEnabled) {
 				S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
 			}
 			
 			List<String> buildArgCmds = null;
 			String workingDirectory = Utility.getJenkinsHome();
 			BufferedReader reader = ciManager.stop(projectInfo, ActionType.STOP, buildArgCmds , workingDirectory);
 			
 			setSessionAttribute(getAppId() + CI_STOP, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, CI_STOP);
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of CI.stopJenkins()" + FrameworkUtil.getStackTraceAsString(e));
 		}
 		return APP_ENVIRONMENT_READER;
 	}
 
 	public String restartJenkins() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.restartJenkins()");
 		}
 		try {
 			stop();
 			if (debugEnabled) {
 				S_LOGGER.debug("stopJenkins method called");
 			}
 			BufferedReader reader = (BufferedReader) getHttpSession().getAttribute(getAppId() + CI_STOP);
 			String line;
 			line = reader.readLine();
 			while (!line.startsWith("[INFO] BUILD SUCCESS")) {
 				line = reader.readLine();
 				if (debugEnabled) {
 					S_LOGGER.debug("Restart Stop Console : " + line);
 				}
 			}
 			waitForTime(5);
 
 			start();
 			if (debugEnabled) {
 				S_LOGGER.debug("startJenkins method called");
 			}
 
 			BufferedReader reader1 = (BufferedReader) getHttpSession().getAttribute(getAppId() + CI_START);
 			String line1;
 			line1 = reader1.readLine();
 			while (line1 != null && !line1.startsWith("[INFO] BUILD SUCCESS")) {
 				line1 = reader1.readLine();
 				if (debugEnabled) {
 					S_LOGGER.debug("Restart Start Console : " + line);
 				}
 			}
 			waitForTime(2);
 
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.restartJenkins()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 
 		return RESTARTED;
 	}
 
 	public void waitForTime(int waitSec) {
 		if (debugEnabled) {
 			S_LOGGER.debug("waitForTime : " + waitSec);
 		}
 		long startTime = 0;
 		startTime = new Date().getTime();
 		while (new Date().getTime() < startTime + waitSec * 1000) {
 			// Dont do anything for some seconds. It waits till the log is
 			// written to file
 		}
 		if (debugEnabled) {
 			S_LOGGER.debug("Return from waitForTime ");
 		}
 	}
 
 	public String cronValidation() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  CI.cronValidation()");
 		}
 		try {
 			HttpServletRequest request = getHttpRequest();
 			String cronBy = request.getParameter(REQ_CRON_BY);
 			String jobName = "Job Name";
 			String cronExpression = "";
 			Date[] dates = null;
 
 			if (REQ_CRON_BY_DAILY.equals(cronBy)) {
 				String hours = request.getParameter(REQ_HOURS);
 				String minutes = request.getParameter(REQ_MINUTES);
 				String every = request.getParameter(REQ_SCHEDULE_EVERY);
 
 				if ("false".equals(every)) {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " 0 * * ?";
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + hours + " * * ?";
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " " + hours
 								+ " * * ?";
 					}
 				} else {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + "*/" + minutes + " * * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + "*/" + hours + " * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " */" + hours
 								+ " * * ?"; // 0 replace with *
 					}
 				}
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_WEEKLY.equals(cronBy)) {
 				String hours = request.getParameter(REQ_HOURS);
 				String minutes = request.getParameter(REQ_MINUTES);
 				String week = request.getParameter(REQ_CRON_BY_WEEK);
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " ? * " + week;
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_MONTHLY.equals(cronBy)) {
 				String hours = request.getParameter(REQ_HOURS);
 				String minutes = request.getParameter(REQ_MINUTES);
 				String month = request.getParameter(REQ_MONTH);
 				String day = request.getParameter(REQ_DAY);
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " " + day + " "
 						+ month + " ?";
 				dates = testCronExpression(cronExpression);
 			}
 
 			if (dates != null) {
 				cronExpression = cronExpression.replace('?', '*');
 				cronExpression = cronExpression.substring(2);
 				setReqAttribute(REQ_CRON_EXPRESSION, cronExpression);
 				setReqAttribute(REQ_CRON_DATES, dates);
 				setReqAttribute(REQ_JOB_NAME, jobName);
 				return CRON_VALIDATION;
 			}
 
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.cronValidation()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 		return CRON_VALIDATION;
 	}
 
 	public Date[] testCronExpression(String expression) throws PhrescoException {
 		Date[] dates = null;
 		try {
 			if (debugEnabled) {
 				S_LOGGER.debug("Entering Method  CI.testCronExpression(String expression)");
 				S_LOGGER.debug("testCronExpression() Expression = " + expression);
 			}
 			final CronExpression cronExpression = new CronExpression(expression);
 			final Date nextValidDate1 = cronExpression.getNextValidTimeAfter(new Date());
 			final Date nextValidDate2 = cronExpression.getNextValidTimeAfter(nextValidDate1);
 			final Date nextValidDate3 = cronExpression.getNextValidTimeAfter(nextValidDate2);
 			final Date nextValidDate4 = cronExpression.getNextValidTimeAfter(nextValidDate3);
 			dates = new Date[] { nextValidDate1, nextValidDate2, nextValidDate3, nextValidDate4 };
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.testCronExpression()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		}
 		return dates;
 	}
 
 	public static String getPortNo(String path) throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method CI.getPortNo()");
 		}
 		String portNo = "";
 		try {
 			Document document = ApplicationsUtil.getDocument(new File(path + File.separator + POM_FILE));
 			String portNoNode = CI_TOMCAT_HTTP_PORT;
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, portNoNode);
 			portNo = nodelist.item(0).getTextContent();
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.getPortNo()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			throw new PhrescoException(e);
 		}
 		return portNo;
 	}
 
 	public String CIBuildDownload() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method CI.CIBuildDownload()");
 		}
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			CIJob existJob = ciManager.getJob(appInfo, downloadJobName);
 			// Get it from web path
 			URL url = new URL(buildDownloadUrl);
 			if (debugEnabled) {
 				S_LOGGER.debug("Entering Method CI.CIBuildDownload() buildDownloadUrl " + buildDownloadUrl);
 			}
 			fileInputStream = url.openStream();
 			fileName = existJob.getName();
 			return SUCCESS;
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of CI.CIBuildDownload()" + FrameworkUtil.getStackTraceAsString(e));
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CI_BUILD_DOWNLOAD_NOT_AVAILABLE));
 		}
 	}
 
 	public String numberOfJobsIsInProgress() {
 		S_LOGGER.debug("Entering Method CI.numberOfJobsIsInProgress()");
 		try {
 			S_LOGGER.debug("numberOfJobsIsInProgress jobs monitoring");
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationInfo appInfo = getApplicationInfo();
 			List<CIJob> jobs = ciManager.getJobs(appInfo);
 			if (CollectionUtils.isNotEmpty(jobs)) {
 				for (CIJob ciJob : jobs) {
 					boolean jobCreatingBuild = ciManager.isJobCreatingBuild(ciJob);
 					if (jobCreatingBuild) {
 						numberOfJobsInProgress++;
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of CI.buildProgress()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String localJenkinsLocalAlive() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method CI.localJenkinsLocalAlive()");
 		}
 		try {
 			URL url = new URL(HTTP_PROTOCOL + PROTOCOL_POSTFIX + LOCALHOST + COLON + Integer.parseInt(getPortNo(Utility.getJenkinsHome())) + FORWARD_SLASH + CI);
 			URLConnection connection = url.openConnection();
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 			int code = httpConnection.getResponseCode();
 			localJenkinsAlive = code + "";
 			if (debugEnabled) {
 				S_LOGGER.debug("localJenkinsAlive => " + localJenkinsAlive);
 			}
 		} catch (ConnectException e) {
 			localJenkinsAlive = CODE_404;
 			if (debugEnabled) {
 				S_LOGGER.debug("localJenkinsAlive => " + localJenkinsAlive);
 			}
 		} catch (Exception e) {
 			localJenkinsAlive = CODE_404;
 			if (debugEnabled) {
 				S_LOGGER.debug("localJenkinsAlive => " + localJenkinsAlive);
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getSvnurl() {
 		return svnurl;
 	}
 
 	public void setSvnurl(String svnurl) {
 		this.svnurl = svnurl;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String[] getEmails() {
 		return emails;
 	}
 
 	public void setEmails(String[] email) {
 		this.emails = email;
 	}
 
 	public String getSuccessEmailIds() {
 		return successEmailIds;
 	}
 
 	public void setSuccessEmailIds(String successEmailId) {
 		this.successEmailIds = successEmailId;
 	}
 
 	public String getFailureEmailIds() {
 		return failureEmailIds;
 	}
 
 	public void setFailureEmailIds(String failureEmailId) {
 		this.failureEmailIds = failureEmailId;
 	}
 
 	public String getSchedule() {
 		return schedule;
 	}
 
 	public void setSchedule(String schedule) {
 		this.schedule = schedule;
 	}
 
 	public String getCronExpression() {
 		return cronExpression;
 	}
 
 	public void setCronExpression(String cronExpression) {
 		this.cronExpression = cronExpression;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getBuildDownloadUrl() {
 		return buildDownloadUrl;
 	}
 
 	public void setBuildDownloadUrl(String buildDownloadUrl) {
 		this.buildDownloadUrl = buildDownloadUrl;
 	}
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public String getSenderEmailId() {
 		return senderEmailId;
 	}
 
 	public void setSenderEmailId(String senderEmailId) {
 		this.senderEmailId = senderEmailId;
 	}
 
 	public String getSenderEmailPassword() {
 		return senderEmailPassword;
 	}
 
 	public void setSenderEmailPassword(String senderEmailPassword) {
 		this.senderEmailPassword = senderEmailPassword;
 	}
 
 	public int getTotalBuildSize() {
 		return totalBuildSize;
 	}
 
 	public void setTotalBuildSize(int totalBuildSize) {
 		this.totalBuildSize = totalBuildSize;
 	}
 
 	public boolean isBuildInProgress() {
 		return buildInProgress;
 	}
 
 	public void setBuildInProgress(boolean buildInProgress) {
 		this.buildInProgress = buildInProgress;
 	}
 
 	public List<String> getTriggers() {
 		return triggers;
 	}
 
 	public void setTriggers(List<String> triggers) {
 		this.triggers = triggers;
 	}
 
 	public String getBuildNumber() {
 		return buildNumber;
 	}
 
 	public void setBuildNumber(String buildNumber) {
 		this.buildNumber = buildNumber;
 	}
 
 	public String getOldJobName() {
 		return oldJobName;
 	}
 
 	public void setOldJobName(String oldJobName) {
 		this.oldJobName = oldJobName;
 	}
 
 	public int getNumberOfJobsInProgress() {
 		return numberOfJobsInProgress;
 	}
 
 	public void setNumberOfJobsInProgress(int numberOfJobsInProgress) {
 		this.numberOfJobsInProgress = numberOfJobsInProgress;
 	}
 
 	public String getDownloadJobName() {
 		return downloadJobName;
 	}
 
 	public void setDownloadJobName(String downloadJobName) {
 		this.downloadJobName = downloadJobName;
 	}
 
 	public String getSvnType() {
 		return svnType;
 	}
 
 	public void setSvnType(String svnType) {
 		this.svnType = svnType;
 	}
 
 	public String getBranch() {
 		return branch;
 	}
 
 	public void setBranch(String branch) {
 		this.branch = branch;
 	}
 
 	public String getLocalJenkinsAlive() {
 		return localJenkinsAlive;
 	}
 
 	public void setLocalJenkinsAlive(String localJenkinsAlive) {
 		this.localJenkinsAlive = localJenkinsAlive;
 	}
 
 	public boolean isEnableBuildRelease() {
 		return enableBuildRelease;
 	}
 
 	public void setEnableBuildRelease(boolean enableBuildRelease) {
 		this.enableBuildRelease = enableBuildRelease;
 	}
 
 	public String getCollabNetURL() {
 		return collabNetURL;
 	}
 
 	public void setCollabNetURL(String collabNetURL) {
 		this.collabNetURL = collabNetURL;
 	}
 
 	public String getCollabNetusername() {
 		return collabNetusername;
 	}
 
 	public void setCollabNetusername(String collabNetusername) {
 		this.collabNetusername = collabNetusername;
 	}
 
 	public String getCollabNetpassword() {
 		return collabNetpassword;
 	}
 
 	public void setCollabNetpassword(String collabNetpassword) {
 		this.collabNetpassword = collabNetpassword;
 	}
 
 	public String getCollabNetProject() {
 		return collabNetProject;
 	}
 
 	public void setCollabNetProject(String collabNetProject) {
 		this.collabNetProject = collabNetProject;
 	}
 
 	public String getCollabNetPackage() {
 		return collabNetPackage;
 	}
 
 	public void setCollabNetPackage(String collabNetPackage) {
 		this.collabNetPackage = collabNetPackage;
 	}
 
 	public String getCollabNetRelease() {
 		return collabNetRelease;
 	}
 
 	public void setCollabNetRelease(String collabNetRelease) {
 		this.collabNetRelease = collabNetRelease;
 	}
 
 	public boolean isCollabNetoverWriteFiles() {
 		return collabNetoverWriteFiles;
 	}
 
 	public void setCollabNetoverWriteFiles(boolean collabNetoverWriteFiles) {
 		this.collabNetoverWriteFiles = collabNetoverWriteFiles;
 	}
 
 	public String getUsedClonnedWorkspace() {
 		return usedClonnedWorkspace;
 	}
 
 	public void setUsedClonnedWorkspace(String usedClonnedWorkspace) {
 		this.usedClonnedWorkspace = usedClonnedWorkspace;
 	}
 
 	public String getOperation() {
 		return operation;
 	}
 
 	public void setOperation(String operation) {
 		this.operation = operation;
 	}
 
 	public String getUpstreamProject() {
 		return upstreamProject;
 	}
 
 	public void setUpstreamProject(String upstreamProject) {
 		this.upstreamProject = upstreamProject;
 	}
 
 	public String getDownstreamProject() {
 		return downstreamProject;
 	}
 
 	public void setDownstreamProject(String downstreamProject) {
 		this.downstreamProject = downstreamProject;
 	}
 
 	public boolean isCloneWorkspace() {
 		return cloneWorkspace;
 	}
 
 	public void setCloneWorkspace(boolean cloneWorkspace) {
 		this.cloneWorkspace = cloneWorkspace;
 	}
 
 	public String getProjectModule() {
 		return projectModule;
 	}
 
 	public void setProjectModule(String projectModule) {
 		this.projectModule = projectModule;
 	}
 
 	public String getTheme() {
 		return theme;
 	}
 
 	public void setTheme(String theme) {
 		this.theme = theme;
 	}
 
 	public String getLogo() {
 		return logo;
 	}
 
 	public void setLogo(String logo) {
 		this.logo = logo;
 	}
 
 	public String getSonarUrl() {
 		return sonarUrl;
 	}
 
 	public void setSonarUrl(String sonarUrl) {
 		this.sonarUrl = sonarUrl;
 	}
 
 	public String getReportType() {
 		return reportType;
 	}
 
 	public void setReportType(String reportType) {
 		this.reportType = reportType;
 	}
 
 	public String getTestType() {
 		return testType;
 	}
 
 	public void setTestType(String testType) {
 		this.testType = testType;
 	}
 
 	public String getDownstreamCriteria() {
 		return downstreamCriteria;
 	}
 
 	public void setDownstreamCriteria(String downstreamCriteria) {
 		this.downstreamCriteria = downstreamCriteria;
 	}
 }
