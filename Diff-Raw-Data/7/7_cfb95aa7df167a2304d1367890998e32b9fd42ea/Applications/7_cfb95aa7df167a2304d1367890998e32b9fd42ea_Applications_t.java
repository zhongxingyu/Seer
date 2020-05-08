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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.ListBranchCommand.ListMode;
 import org.eclipse.jgit.lib.Ref;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNException;
 
 import com.google.gson.Gson;
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionContext;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.SVNAccessor;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.Project;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.api.ValidationResult;
 import com.photon.phresco.framework.commons.ApplicationsUtil;
 import com.photon.phresco.framework.commons.DiagnoseUtil;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.model.CertificateInfo;
 import com.photon.phresco.framework.model.PropertyInfo;
 import com.photon.phresco.framework.model.SettingsInfo;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.model.Scm;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientHandlerException;
 
 public class Applications extends FrameworkBaseAction {
     
 	private static final long serialVersionUID = -4282767788002019870L;
 
 	private static final Logger S_LOGGER = Logger.getLogger(Applications.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String projectCode = "";
 	
 	private String globalValidationStatus = "";
 	
 	private String showSettings = "";
 	private List<String> settingsEnv = null;
 	
 	private List<String> versions = null;
 	private String selectedVersions = "";
 	private String selectedAttrType = "";
 	private String selectedParamName = "";
 	private String divTobeUpdated = "";
 	
 	private List<String> techVersions = null;
 	private boolean hasConfiguration = false;
 	private String configServerNames = "";
 	private String configDbNames = "";
 	
 	List<String> deletableDbs = new ArrayList<String>();
 	private String fileType = "";
 	private String fileorfolder = "";
 	
 	//svn info
 	private String repositoryUrl = "";
 	private String credential = "";
 	private String customerId = "";
 	private String application = "";
 	private String userName = "";
     private String password = "";
     private String revision = "";
     private String revisionVal = "";
     private boolean svnImport = false;
     private String svnImportMsg = "";
     
 	// import from git
 	private String repoType = "";
 	
 	private String fromPage = "";
 	private String fromTab = "";
 	
 	boolean hasError = false;
     private String envError = "";
 
 	public String list() {
 		long start = System.currentTimeMillis();
 		if (isDebugEnabled) {
 		    S_LOGGER.debug("Entering Method  Applications.list()");
 		}
 		
 		try {
 			getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			getHttpSession().removeAttribute(projectCode);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.list()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Listing projects");
 			
 			return LOG_ERROR;
 		}
 		String discover = discover();
 		long end = System.currentTimeMillis();
 		S_LOGGER.debug("Total Time : " + (end - start));
 		
 		return discover;
 	}
 
 	public String applicationDetails() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.applicationDetails()");
 	    }
 
 		try {
 			getHttpSession().removeAttribute(projectCode);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			if (StringUtils.isNotEmpty(projectCode)) {
 				ApplicationInfo appInfo = administrator.getProject(
 						projectCode).getApplicationInfo();
 				S_LOGGER.debug("project info value" + appInfo.toString());
 				getHttpRequest().setAttribute(REQ_APPINFO, appInfo);
 			}
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.addApplication()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, REQ_TITLE_ADD_APPLICATION);
 			
 			return LOG_ERROR;
 		}
 		
 		return APP_APPLICATION_DETAILS;
 	}
 
 	public String appInfo() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.add()");
 	    }
 
 		try {
 			if (StringUtils.isNotEmpty(fromPage)) {
 				getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 			}
 			getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			ApplicationInfo appInfo = null;
 			if (FEATURES.equals(fromTab)) {
 				appInfo = (ApplicationInfo) getHttpSession().getAttribute(projectCode);
 			}
 			if (FROM_PAGE_EDIT.equals(fromPage) && appInfo == null) {
 				appInfo = administrator.getProject(projectCode).getApplicationInfo();
 				customerId = appInfo.getPilotContent().getCustomerIds().get(0);
 				getHttpSession().setAttribute(projectCode, appInfo);
 			}
 			FrameworkUtil.setAppInfoDependents(getHttpRequest(), customerId);
 			getHttpRequest().setAttribute(REQ_TEMP_SELECTED_PILOT_PROJ,
 					getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ));
 			String modules = getHttpRequest().getParameter(REQ_SELECTEDMODULES);
 			if (StringUtils.isNotEmpty(modules)) {
 				Map<String, String> mapModules = ApplicationsUtil
 						.getIdAndVersionAsMap(getHttpRequest(), modules);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTEDMODULES, mapModules);
 			}
 
 			String jsLibs = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
 			if (StringUtils.isNotEmpty(jsLibs)) {
 				Map<String, String> mapJsLib = ApplicationsUtil
 						.getIdAndVersionAsMap(getHttpRequest(), jsLibs);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLib);
 			}
 		} catch (ClientHandlerException e) {
 			S_LOGGER.error("Entered into catch block of Applications.appInfo()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, REQ_TITLE_ADD_APPLICATION);
 			
 			return LOG_ERROR;
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.appInfo()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			addActionError(e.getLocalizedMessage());
 			new LogErrorReport(e, REQ_TITLE_ADD_APPLICATION);
 			
 			return LOG_ERROR;
 		}
 		getHttpRequest().setAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
 		getHttpRequest().setAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
 
 		return APP_APPINFO;
 	}
 
 	public String applicationType() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.applicationType()");
 	    }
 		
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			Project project = null;
 			if (StringUtils.isNotEmpty(projectCode) && FROM_PAGE_EDIT.equals(fromPage)) {
 				project = administrator.getProject(projectCode);
 				ApplicationInfo appInfo = project.getApplicationInfo();
 				getHttpRequest().setAttribute(REQ_APPINFO, appInfo);
 				application = appInfo.getTechInfo().getAppTypeId();
 			}
 			List<Technology> technologies = administrator.getTechnologies(application, customerId);
 			getHttpRequest().setAttribute(REQ_APPTYPE_TECHNOLOGIES, technologies);
 			getHttpRequest().setAttribute(REQ_SELECTED_JSLIBS,
 					getHttpRequest().getParameter(REQ_SELECTED_JSLIBS));
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.applicationType()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Getting Application Type");
 			
 			return LOG_ERROR;
 		}
 		
 		return APP_TYPE;
 	}
 
 	public String technology() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.technology()");
 	    }
 		
 		try {
 			String selectedTechnology = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Project project = null;
 			if (StringUtils.isNotEmpty(projectCode) && FROM_PAGE_EDIT.equals(fromPage)) {
 				project = administrator.getProject(projectCode);
 				ApplicationInfo appInfo = project.getApplicationInfo();
 				S_LOGGER.debug("project info value" + appInfo.toString());
 				getHttpRequest().setAttribute(REQ_APPINFO, appInfo);
 				application = appInfo.getTechInfo().getAppTypeId();
 				selectedTechnology = appInfo.getPilotContent().getId();
 			}
 			Technology techonology = administrator.getTechnology(application, selectedTechnology, customerId);
 			List<DownloadInfo> servers = administrator.getServersByTech(selectedTechnology, customerId);
 			List<DownloadInfo> databases = administrator.getDatabasesByTech(selectedTechnology, customerId);
 			List<WebService> webServices = administrator
 					.getWebServices(selectedTechnology, customerId);
 			S_LOGGER.debug("Selected technology" + techonology.toString());
 			getHttpRequest().setAttribute(REQ_PILOTS_NAMES, 
 				ApplicationsUtil.getPilotNames(techonology.getId(), customerId));
 			getHttpRequest().setAttribute(REQ_PILOT_PROJECT_INFO,
 				ApplicationsUtil.getPilotProjectInfo(techonology.getId(), customerId));
 			getHttpRequest().setAttribute(REQ_SELECTED_TECHNOLOGY, techonology);
 			getHttpRequest().setAttribute(REQ_APPLICATION_TYPE, application);
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 			getHttpRequest().setAttribute(REQ_SERVERS, servers);
 			getHttpRequest().setAttribute(REQ_DATABASES, databases);
 			getHttpRequest().setAttribute(REQ_WEBSERVICES, webServices);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Applications.technology()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Getting technology");
 			
 			return LOG_ERROR;
 		}
 
 		return APP_TECHNOLOGY;
 	}
 
 	public String techVersions() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.techVersions()");
 	    }
 	    
 		try {
 			String selectedTechnology = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			Technology techonology = administrator.getTechnology(application, selectedTechnology, customerId);
 			techVersions = techonology.getTechVersions();
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Applications.techVersions()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Getting technology versions");
 			
 			return LOG_ERROR;
 		}
 
 		return SUCCESS;
 	}
 
 	public String previous() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.previous()");
 	    }
 
 		try {
 			getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
 			ProjectInfo projectInfo = null;
 			if (StringUtils.isNotEmpty(projectCode)) {
 				projectInfo = (ProjectInfo) getHttpSession().getAttribute(projectCode);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTED_PILOT_PROJ,
 						getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ));
 				String modules = getHttpRequest().getParameter(REQ_SELECTEDMODULES);
 				if (StringUtils.isNotEmpty(modules)) {
 					Map<String, String> mapModules = ApplicationsUtil
 							.getIdAndVersionAsMap(getHttpRequest(), modules);
 					getHttpRequest().setAttribute(REQ_TEMP_SELECTEDMODULES,
 							mapModules);
 				}
 
 				String jsLibs = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
 				if (StringUtils.isNotEmpty(jsLibs)) {
 					Map<String, String> mapJsLib = ApplicationsUtil.getIdAndVersionAsMap(getHttpRequest(), jsLibs);
 					getHttpRequest().setAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLib);
 				}
 			}
 			getHttpSession().setAttribute(projectCode, projectInfo);
 			FrameworkUtil.setAppInfoDependents(getHttpRequest(), customerId);
 			getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			getHttpRequest().setAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
 			getHttpRequest().setAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
 			getHttpRequest().setAttribute(REQ_CUSTOMER_ID, customerId);
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Applications.previous()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "When previous button is clicked");
 			
 			return LOG_ERROR;
 		}
 
 		return APP_APPINFO;
 	}
 
 	public String save() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Applications.save()");
 	    }
 
 		try {
 		    ApplicationInfo appInfo = (ApplicationInfo) getHttpSession().getAttribute(projectCode);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			setFeatures(administrator, appInfo);
 			S_LOGGER.debug("Going to create project, Project info values "
 					+ appInfo.toString());
 			administrator.createProject(appInfo, null);
 			addActionMessage(getText(SUCCESS_PROJECT,
 					Collections.singletonList(appInfo.getName())));
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Applications.save()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Save Project");
 			
 			return LOG_ERROR;
 		}
 		getHttpSession().removeAttribute(projectCode);
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 
 		return discover();
 	}
 
 	public String update() throws PhrescoException, CloneNotSupportedException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.update()");
 	    }
 
 		BufferedReader reader = null;
 		ApplicationInfo appInfo = (ApplicationInfo) getHttpSession().getAttribute(
 				projectCode);
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			setFeatures(administrator, appInfo);
 			// TODO:Lohes
 //			ApplicationInfo originalAppInfo = appInfo.clone();
 			File projectPath = new File(Utility.getProjectHome(),
 					appInfo.getCode() + File.separator + FOLDER_DOT_PHRESCO
 							+ File.separator + PROJECT_INFO);
 			try {
 				reader = new BufferedReader(new FileReader(projectPath));
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e);
 
 			}
 			List<String> modules = appInfo.getSelectedModules();
 			List<String> jsLibraries = appInfo.getSelectedJSLibs();
 			if (CollectionUtils.isEmpty(modules)) {
 				appInfo.setSelectedModules(null);
 			}
 
 			if (CollectionUtils.isEmpty(jsLibraries)) {
 				appInfo.setSelectedJSLibs(null);
 			}
 			try {
 				ApplicationInfo tempAppInfo = administrator.getProject(
 						projectCode).getApplicationInfo();
 				List<String> newlySelectedDbs = appInfo.getSelectedDatabases();
 				List<String> newDbNames = new ArrayList<String>();
 				// TODO:Lohes
 //				if (CollectionUtils.isNotEmpty(newlySelectedDbs)) {
 //					for (String newDatabase : newlySelectedDbs) {
 //						newDbNames.add(newDatabase.getName());
 //					}
 //				}
 
 				List<String> appInfoDbs = tempAppInfo.getSelectedDatabases();
 				List<String> appInfoDbNames = new ArrayList<String>();
 				// TODO:Lohes
 //				if (CollectionUtils.isNotEmpty(projectInfoDbs)) {
 //					for (String projectInfoDb : projectInfoDbs) {
 //						projectInfoDbNames.add(projectInfoDb.getName());
 //					}
 //				}
 
 				if (CollectionUtils.isNotEmpty(appInfoDbNames)) {
 					for (String appInfoDbName : appInfoDbNames) {
 						if (!newDbNames.contains(appInfoDbName)) {
 							deletableDbs.add(appInfoDbName);
 						} else {
 							for (String newDatabase : newlySelectedDbs) {
 								for (String appInfoDb : appInfoDbs) {
 								 // TODO:Lohes
 //									if (newDatabase.getName().equals(appInfoDb.getName())) {
 //										List<String> newDbVersions = newDatabase.getVersions();
 //										List<String> appInfoDbVersions = appInfoDb.getVersions();
 //										compareVersions(appInfoDb.getName(), appInfoDbVersions, newDbVersions);
 //									}
 								}
 							}
 						}
 					}
 				}
 
 				administrator.deleteSqlFolder(deletableDbs, appInfo);
 				User userInfo = (User) getHttpSession().getAttribute(
 						REQ_USER_INFO);
 				// TODO:Lohes
 //				administrator.updateProject(appInfo, originalAppInfo,
 //						projectPath, userInfo);
 				removeConfiguration();
 				addActionMessage(getText(UPDATE_PROJECT,
 						Collections.singletonList(appInfo.getName())));
 			} catch (Exception e) {
 				throw new PhrescoException(e);
 			}
 
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into catch block of  Applications.update()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Update Project");
 			
 			return LOG_ERROR;
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 		getHttpSession().removeAttribute(projectCode);
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 
 		return discover();
 	}
 
 	private void compareVersions(String dbName, List<String> projectInfoDbVersions, List<String> newDbVersions) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.compareVersions()");
 	    }
 		try {
             for (String projectInfoDbVersion : projectInfoDbVersions) {
             	if (newDbVersions.contains(projectInfoDbVersion)) {
 
             	} else {
             		deletableDbs.add(dbName + "/" + projectInfoDbVersion.trim());
             	}
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
 	}
 
 	private void setFeatures(ProjectAdministrator administrator, ApplicationInfo appInfo) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.setFeatures()");
 	    }
 		
 		try {
 			String module = getHttpRequest().getParameter(REQ_SELECTEDMODULES);
 			String[] moduleGroupIds = module.split(",");
 			// TODO:Lohes
 			/*if (!ArrayUtils.isEmpty(moduleGroupIds)) {
 				List<ArtifactGroup> allModules = administrator.getAllModules(appInfo.getTechInfo().getId(), customerId);
 				List<ArtifactGroup> selectedModules = ApplicationsUtil
 						.getSelectedTuples(getHttpRequest(), allModules, moduleGroupIds);
 				appInfo.setSelectedModules(selectedModules)
 			}
 
 			String jsLib = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
 			String[] jsLibs = jsLib.split(",");
 			if (jsLibs != null) {
 				List<ModuleGroup> allModules = administrator.getJSLibs(appInfo.getTechnology().getId(), customerId);
 				List<ModuleGroup> selectedModules = ApplicationsUtil
 						.getSelectedTuples(getHttpRequest(), allModules, jsLibs);
 				appInfo.getTechnology().setJsLibraries(selectedModules);
 			}*/
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public String delete() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.delete()");
 	    }
 
 		try {
 			HttpServletRequest request = (HttpServletRequest) ActionContext
 					.getContext().get(ServletActionContext.HTTP_REQUEST);
 			String selectedProjects[] = request
 					.getParameterValues(REQ_SELECTEDPROJECTS);
 
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			List<String> projectCodes = new ArrayList<String>();
 			if (selectedProjects != null) {
 				for (String selctedProject : selectedProjects) {
 					projectCodes.add(selctedProject);
 				}
 			}
 
 			administrator.deleteProject(projectCodes);
 			addActionMessage(SUCCESS_PROJECT_DELETE);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.delete()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Project delete");
 			
 			return LOG_ERROR;
 		}
 
 		return list();
 	}
 
 	public String importSVNApplication() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.importApplication()");
 		    S_LOGGER.debug("repoType " + repoType);
 		    S_LOGGER.debug("repositoryUrl " + repositoryUrl);
 	    }
 		
 		try {
 			File checkOutDir = new File(Utility.getProjectHome());
 			if (StringUtils.isEmpty(credential)) {
 				String decryptedPass = new String(Base64.decodeBase64(password));
 				password = decryptedPass;
 			}
 			S_LOGGER.debug(customerId);
 			SVNAccessor svnAccessor = new SVNAccessor(repositoryUrl, userName, password);
 			ApplicationInfo appInfo = svnAccessor.getApplicationInfo(revision);
 			String projCode = appInfo.getCode();
 			String importProjectCustId = appInfo.getPilotContent().getCustomerIds().get(0);
 			if (customerId.equals(importProjectCustId)) {
 				S_LOGGER.debug("importProjectCustId " + importProjectCustId);
 				S_LOGGER.debug("Import Application repository Url"
 							+ repositoryUrl + " Username " + userName);
 				revision = !"HEAD".equals(revision) ? revisionVal : revision;
 				svnAccessor.checkout(checkOutDir, revision, true, projCode);
 				svnImport = true;
 				svnImportMsg = getText(IMPORT_SUCCESS_PROJECT);
 				// update connection url in pom.xml
 				updateSCMConnection(projCode, repositoryUrl);
 			} else {
 				svnImport = false;
 		        svnImportMsg = getText(INVALID_CUSTOMER_PROJECT);
 			}
 		} catch (SVNAuthenticationException e) {
 	         S_LOGGER.error("Entered into catch block of Applications.importApplication()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 	         svnImport = false;
 	         svnImportMsg = getText(INVALID_CREDENTIALS);
 	    } catch (SVNException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importApplication()"
 					+ FrameworkUtil.getStackTraceAsString(e)); 
 	    	svnImport = false;
 	    	if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 	    		svnImportMsg = getText(INVALID_URL);
 	    	} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 	    		svnImportMsg = getText(INVALID_REVISION);
 	    	} else {
 	    		svnImportMsg = getText(INVALID_FOLDER);
 	    	}
 	    } catch (PhrescoException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importApplication()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 	    	svnImport = false;
 	    	svnImportMsg = getText(PROJECT_ALREADY);
 	    } catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.importApplication()"
 					+ FrameworkUtil.getStackTraceAsString(e)); 
 			svnImport = false;
 			svnImportMsg = getText(IMPORT_PROJECT_FAIL);
 		}
 	    
 		return SUCCESS;
 	}
 	
 	public String importGITApplication() {
 	    if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Applications.importApplication()");
     		S_LOGGER.debug("repoType " + repoType);
     		S_LOGGER.debug("repositoryUrl " + repositoryUrl);
     		S_LOGGER.debug("Entering Method  Applications.importFromGit()");
 	    }
 	    
 		try {
 			File gitImportTemp = new File(Utility.getPhrescoTemp(), GIT_IMPORT_TEMP_DIR);
 			S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 			if (gitImportTemp.exists()) {
 				S_LOGGER.debug("Empty git directory need to be removed before importing from git ");
 				FileUtils.deleteDirectory(gitImportTemp);
 			}
 			S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 			importFromGit(repositoryUrl, gitImportTemp);
 			ApplicationInfo appInfo = getApplicationInfo(gitImportTemp);
 			S_LOGGER.debug(appInfo.getCode());
 			importToWorkspace(gitImportTemp, Utility.getProjectHome(), appInfo.getCode());
 			svnImport = true;
 			svnImportMsg = getText(IMPORT_SUCCESS_PROJECT);
 			//update connection in pom.xml
 			updateSCMConnection(appInfo.getCode(), repositoryUrl);
 		} catch (org.apache.commons.io.FileExistsException e) { // Destination '/Users/kaleeswaran/projects/PHR_Phpblog' already exists
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(PROJECT_ALREADY);
 		} catch (org.eclipse.jgit.api.errors.TransportException e) { //Invalid remote: origin (URL)
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(INVALID_URL);
 		} catch (org.eclipse.jgit.api.errors.InvalidRemoteException e) { //Invalid remote: origin (URL)
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(INVALID_URL);
 		}  catch (PhrescoException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + FrameworkUtil.getStackTraceAsString(e));
 	    	svnImport = false;
 	    	svnImportMsg = getText(INVALID_FOLDER);
 	    } catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(IMPORT_PROJECT_FAIL);
 		}
 	    
 		return SUCCESS;
 	}
 
 	public String importFromSvn() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.importFromSvn()");
 	    }
 	    
 		return APP_IMPORT_FROM_SVN;
 	}
 	
 	public String updateProjectPopup() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.updateProjectPopup()");
 	    }
 		
 		try {
 			String connectionUrl = "";
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			PomProcessor processor = frameworkUtil.getPomProcessor(projectCode);
 			Scm scm = processor.getSCM();
 			if (scm != null) {
 				connectionUrl = scm.getConnection();
 			}
 			S_LOGGER.debug("connectionUrl " + connectionUrl);
 			getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 			getHttpRequest().setAttribute(REPO_URL, connectionUrl);
 			getHttpRequest().setAttribute(REQ_FROM_TAB, "updateProject");
 			getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.updateProjectPopup()" + FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Updating project popup");
 			
 			return LOG_ERROR;
 		}
 		
 		return APP_IMPORT_FROM_SVN;
 	}
 	
 	public String updateGitProject() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.updateGitProject()");
 	    }
 	    
 		try {
 			S_LOGGER.debug("update SCM Connection " + repositoryUrl);
 			S_LOGGER.debug("userName " + userName);
 			S_LOGGER.debug("Repo type " + repoType);
 			S_LOGGER.debug("projectCode " + projectCode);
 			updateSCMConnection(projectCode, repositoryUrl);
 			
 			File updateDir = new File(Utility.getProjectHome() , projectCode);
 			S_LOGGER.debug("updateDir GIT... " + updateDir);
 			Git git = Git.open(updateDir); //checkout is the folder with .git
 			git.pull().call(); //succeeds
 			
 			svnImport = true;
 			svnImportMsg = getText(SUCCESS_PROJECT_UPDATE);
 		} catch (org.apache.commons.io.FileExistsException e) { // Destination '/Users/kaleeswaran/projects/PHR_Phpblog' already exists
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(FAILURE_PROJECT_UPDATE);
 		} catch (org.eclipse.jgit.api.errors.TransportException e) { //Invalid remote: origin (URL)
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(INVALID_URL);
 		} catch (org.eclipse.jgit.api.errors.InvalidRemoteException e) { //Invalid remote: origin (URL)
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(INVALID_URL);
 		}  catch (PhrescoException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 	    	svnImport = false;
 	    	svnImportMsg = getText(INVALID_FOLDER);
 	    } catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.importFromGit()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 			svnImport = false;
 			svnImportMsg = getText(FAILURE_PROJECT_UPDATE);
 		}
 	    
 		return SUCCESS;
 	}
 	
 	public String updateSVNProject() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.updateSVNProject()");
 	    }
 		try {
 			S_LOGGER.debug("update SCM Connection " + repositoryUrl);
 			S_LOGGER.debug("userName " + userName);
 			S_LOGGER.debug("Repo type " + repoType);
 			S_LOGGER.debug("projectCode " + projectCode);
 			updateSCMConnection(projectCode, repositoryUrl);
 			
 			SVNAccessor svnAccessor = new SVNAccessor(repositoryUrl, userName, password);
 			S_LOGGER.debug("Import Application repository Url" + repositoryUrl + " Username " + userName);
 			revision = !HEAD_REVISION.equals(revision) ? revisionVal : revision;
 			File updateDir = new File(Utility.getProjectHome() , projectCode);
 			S_LOGGER.debug("updateDir SVN... " + updateDir);
 			svnAccessor.update(updateDir, revision, false);
 			
 			svnImport = true;
 			svnImportMsg = getText(SUCCESS_PROJECT_UPDATE);
 		} catch (SVNAuthenticationException e) {
 	         S_LOGGER.error("Entered into catch block of Applications.importApplication()" + getText(INVALID_CREDENTIALS) + FrameworkUtil.getStackTraceAsString(e));
 	         svnImport = false;
 	         svnImportMsg = getText(INVALID_CREDENTIALS);
 	    } catch(SVNException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importApplication()" + FrameworkUtil.getStackTraceAsString(e)); 
 	    	svnImport = false;
 	    	if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 	    		svnImportMsg = getText(INVALID_URL);
 	    	} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 	    		svnImportMsg = getText(INVALID_REVISION);
 	    	} else {
 	    		svnImportMsg = getText(INVALID_FOLDER);
 	    	}
 	    } catch (PhrescoException e) {
 	    	S_LOGGER.error("Entered into catch block of Applications.importApplication()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e));
 	    	svnImport = false;
 	    	svnImportMsg = getText(FAILURE_PROJECT_UPDATE);
 	    } catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.importApplication()" + getText(FAILURE_PROJECT_UPDATE) + FrameworkUtil.getStackTraceAsString(e)); 
 			svnImport = false;
 			svnImportMsg = getText(FAILURE_PROJECT_UPDATE);
 		}
 	    
 		return SUCCESS;
 	}
 	
 	public String validateFramework() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.validateFramework()");
 	    }
 
 		try {
 			getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_FRAMEWORK);
 			// From home when clicking applications , it becomes true
 			String validateInBg = getHttpRequest().getParameter(VALIDATE_IN_BG);
 			if (validateInBg.equals("true")
 					&& getHttpSession().getAttribute(SESSION_FRMK_VLDT_RSLT) != null) {
 				setGlobalValidationStatus((String) getHttpSession()
 						.getAttribute(SESSION_FRMK_VLDT_STATUS));
 				return Action.SUCCESS;
 			} else {
 				getHttpSession().removeAttribute(SESSION_FRMK_VLDT_STATUS);
 				getHttpSession().removeAttribute(SESSION_FRMK_VLDT_RSLT);
 				ProjectAdministrator administrator = PhrescoFrameworkFactory
 						.getProjectAdministrator();
 				List<ValidationResult> validationResults = administrator
 						.validate();
 				String validationStatus = null;
 				for (ValidationResult validationResult : validationResults) {
 					validationStatus = validationResult.getStatus().toString();
 					if (validationStatus == "ERROR") {
 						getHttpSession().setAttribute(SESSION_FRMK_VLDT_STATUS,
 								"ERROR");
 					}
 				}
 				getHttpSession().setAttribute(SESSION_FRMK_VLDT_RSLT,
 						validationResults);
 				if (validateInBg.equals("true")) {
 					setGlobalValidationStatus(validationStatus);
 					return Action.SUCCESS;
 				}
 			}
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.validateFramework()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Validating framework");
 			
 			return LOG_ERROR;
 		}
 
 		return APP_VALIDATE_FRAMEWORK;
 	}
 
 	public String showFrameworkValidationResult() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.showFrameworkValidationResult()");
 	    }
 	    
 		getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_FRAMEWORK);
 
 		return APP_SHOW_FRAMEWORK_VLDT_RSLT;
 	}
 
 	public String validateProject() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.validateProject()");
 	    }
 
 		try {
 			getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_PROJECT);
 			String validateInBg = getHttpRequest().getParameter(VALIDATE_IN_BG);
 			if (getHttpSession().getAttribute(
 					projectCode + SESSION_PRJT_VLDT_RSLT) != null
 					&& "true".equals(validateInBg)) {
 				setGlobalValidationStatus((String) getHttpSession()
 						.getAttribute(projectCode + SESSION_PRJT_VLDT_STATUS));
 				return Action.SUCCESS;
 			} else {
 				getHttpSession().removeAttribute(
 						projectCode + SESSION_PRJT_VLDT_RSLT);
 				getHttpSession().removeAttribute(
 						projectCode + SESSION_PRJT_VLDT_STATUS);
 				ProjectAdministrator administrator = PhrescoFrameworkFactory
 						.getProjectAdministrator();
 				String validationStatus = null;
 				if (!StringUtils.isEmpty(projectCode)) {
 					Project project = administrator.getProject(projectCode);
 					List<ValidationResult> validationResults = administrator
 							.validate(project);
 					for (ValidationResult validationResult : validationResults) {
 						validationStatus = validationResult.getStatus()
 								.toString();
 						if (validationStatus == "ERROR") {
 							getHttpSession().setAttribute(
 									projectCode + SESSION_PRJT_VLDT_STATUS,
 									"ERROR");
 						}
 					}
 					getHttpSession().setAttribute(
 							projectCode + SESSION_PRJT_VLDT_RSLT,
 							validationResults);
 				}
 
 				if (validateInBg.equals("true")) {
 					setGlobalValidationStatus(validationStatus);
 					return Action.SUCCESS;
 				}
 			}
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.validateProject()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Validating project");
 			
 			return LOG_ERROR;
 		}
 
 		return APP_VALIDATE_PROJECT;
 	}
 
 	public String showProjectValidationResult() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.showProjectValidationResult()");
 	    }
 
 		getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
 		getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_PROJECT);
 
 		return APP_SHOW_PROJECT_VLDT_RSLT;
 	}
 
 	public String discover() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.discover()");
 	    }
 
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			List<Project> projects = administrator.discover(Collections
 					.singletonList(new File(Utility.getProjectHome())), customerId);
 			getHttpRequest().setAttribute(REQ_PROJECTS, projects);
 	         getHttpRequest().setAttribute(REQ_CUSTOMER_ID, customerId);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.discover()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Discovering projects");
 			
 			return LOG_ERROR;
 		}
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 
 		return APP_LIST;
 	}
 
 	public String showSettings() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("entered Applications.showSettings()");
 	    }
 		
 		try {
 			if (showSettings != null && Boolean.valueOf(showSettings)) {
 				if (CollectionUtils.isNotEmpty(getEnvironmentNames())) {
 					settingsEnv = getEnvironmentNames();
 				} else {
 					envError = getText(NO_SETTINGS_ENV);
 				}
 			} 
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.showSettings()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 		}
 		
 		return SUCCESS;
 	}
 
 	private List<String> getEnvironmentNames() {
 		List<String> names = new ArrayList<String>(5);
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			List<Environment> environments = administrator.getEnvironments();
 			if (CollectionUtils.isNotEmpty(environments)) {
 				for (Environment environment : environments) {
 					names.add(environment.getName());
 				}
 			}
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.getEnvironmentNames()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 		}
 
 		return names;
 	}
 
 	public String openAttrPopup() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("entered Applications.openAttrPopup()");
 	    }
 	    
 		try {
 			String techId = getHttpRequest().getParameter("techId");
 			String type = getHttpRequest().getParameter(ATTR_TYPE);
 			String from = getHttpRequest().getParameter(REQ_FROM);
 			getHttpRequest().setAttribute(REQ_FROM, from);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			String attrName = null;
 			if (Constants.SETTINGS_TEMPLATE_SERVER.equals(type)) {
 				List<String> listSelectedServerIds = null;
 				List<DownloadInfo> servers = administrator.getServersByTech(techId, customerId);
 				if (StringUtils.isEmpty(from)) {
 					List<String> listSelectedServers = null;
 					List<String> listSelectedServerNames = null;
 					String selectedServers = getHttpRequest().getParameter(
 							"selectedServers");
 					if (StringUtils.isNotEmpty(selectedServers)) {
 						listSelectedServerNames = new ArrayList<String>();
 						listSelectedServers = new ArrayList<String>(
 								Arrays.asList(selectedServers.split("#SEP#")));
 						for (String listSelectedServer : listSelectedServers) {
 							String[] split = listSelectedServer.split("#VSEP#");
 							listSelectedServerNames.add(split[0].trim());
 						}
 						listSelectedServerIds = new ArrayList<String>(2);
 						for (DownloadInfo server : servers) {
 							if (listSelectedServerNames.contains(server
 									.getName())) {
 								listSelectedServerIds.add(server.getId());
 							}
 						}
 					}
 					getHttpRequest().setAttribute("listSelectedServerIds",
 							listSelectedServerIds);
 					getHttpRequest().setAttribute(REQ_HEADER_TYPE, "Select");
 				} else {
 					attrName = getHttpRequest().getParameter("attrName");
 					String selectedVersions = getHttpRequest().getParameter(
 							"selectedVersions");
 					selectedVersions = selectedVersions.replaceAll(" ", "");
 					List<String> listSelectedVersions = new ArrayList<String>(
 							Arrays.asList(selectedVersions.split(",")));
 					listSelectedServerIds = new ArrayList<String>(2);
 					for (DownloadInfo server : servers) {
 						String serverName = server.getName().trim();
 						serverName = serverName.replaceAll("\\s+", "");
 						if (serverName.equals(attrName)) {
 							listSelectedServerIds.add(server.getId());
 						}
 					}
 					getHttpRequest().setAttribute("listSelectedServerIds",
 							listSelectedServerIds);
 					getHttpRequest().setAttribute(REQ_LISTSELECTED_VERSIONS,
 							listSelectedVersions);
 					getHttpRequest().setAttribute(REQ_HEADER_TYPE, "Edit");
 				}
 				getHttpRequest().setAttribute(REQ_SERVERS, servers);
 			}
 			if (Constants.SETTINGS_TEMPLATE_DB.equals(type)) {
 				List<String> listSelectedDatabaseIds = null;
 				List<DownloadInfo> databases = administrator.getDatabasesByTech(techId, customerId);
 				if (StringUtils.isEmpty(from)) {
 					List<String> listSelectedDbs = null;
 					List<String> listSelectedDbNames = null;
 					List<String> listSelectedDbIds = null;
 					String selectedDatabases = getHttpRequest().getParameter(
 							"selectedDatabases");
 					if (StringUtils.isNotEmpty(selectedDatabases)) {
 						listSelectedDbNames = new ArrayList<String>();
 						listSelectedDbs = new ArrayList<String>(
 								Arrays.asList(selectedDatabases.split("#SEP#")));
 						for (String listSelectedDb : listSelectedDbs) {
 							String[] split = listSelectedDb.split("#VSEP#");
 							listSelectedDbNames.add(split[0].trim());
 						}
 						listSelectedDbIds = new ArrayList<String>(2);
 						for (DownloadInfo database : databases) {
 							if (listSelectedDbNames
 									.contains(database.getName())) {
 								listSelectedDbIds.add(database.getId());
 							}
 						}
 					}
 					getHttpRequest().setAttribute("listSelectedDatabaseIds",
 							listSelectedDbIds);
 					getHttpRequest().setAttribute(REQ_HEADER_TYPE, "Select");
 				} else {
 					attrName = getHttpRequest().getParameter("attrName");
 					if (StringUtils.isNotEmpty(projectCode)) {
 						Project project = administrator.getProject(projectCode);
 						if (project != null) {
 							ApplicationInfo appInfo = project.getApplicationInfo();
 							List<String> appInfoDbs = appInfo.getSelectedDatabases();
 							List<String> appInfoDbVersions = new ArrayList<String>();
 							StringBuilder sb = new StringBuilder();
 							for (String appInfoDb : appInfoDbs) {
 							    // TODO:Lohes
 //								if (appInfoDb.getName().equals(attrName)) {
 //									appInfoDbVersions.addAll(appInfoDb
 //											.getVersions());
 //								}
 							}
 							if (appInfoDbVersions != null) {
 								for (String projectInfoDbVersion : appInfoDbVersions) {
 									sb.append(projectInfoDbVersion);
 									sb.append(",");
 								}
 
 							}
 							if (StringUtils.isNotEmpty(sb.toString())) {
 								getHttpRequest().setAttribute(
 										"projectInfoDbVersions",
 										sb.toString().substring(0,
 												sb.length() - 1));
 							}
 						}
 					}
 					String selectedVersions = getHttpRequest().getParameter(
 							"selectedVersions");
 					selectedVersions = selectedVersions.replaceAll(" ", "");
 					List<String> listSelectedVersions = new ArrayList<String>(
 							Arrays.asList(selectedVersions.split(",")));
 					listSelectedDatabaseIds = new ArrayList<String>(2);
 					for (DownloadInfo database : databases) {
 						String databaseName = database.getName().trim();
 						databaseName = databaseName.replaceAll("\\s+", "");
 						if (databaseName.equals(attrName)) {
 							listSelectedDatabaseIds.add(database.getId());
 						}
 					}
 					getHttpRequest().setAttribute("listSelectedDatabaseIds",
 							listSelectedDatabaseIds);
 					getHttpRequest().setAttribute(REQ_LISTSELECTED_VERSIONS,
 							listSelectedVersions);
 					getHttpRequest().setAttribute(REQ_HEADER_TYPE, "Edit");
 				}
 				getHttpRequest().setAttribute(REQ_DATABASES, databases);
 			}
 
 			getHttpRequest().setAttribute("attrName", attrName);
 			getHttpRequest().setAttribute("header", type);
 			getHttpRequest().setAttribute(REQ_FROM, from);
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of Applications.openAttrPopup()"
 					+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "Getting server and database");
 			
 			return LOG_ERROR;
 		}
 		
 		return "openAttrPopup";
 	}
 
 	public String allVersions() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("entered Applications.allVersions()");
         }
 	    
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			versions = new ArrayList<String>(2);
 			String techId = getHttpRequest().getParameter("techId");
 			String type = getHttpRequest().getParameter("type");
 			String selectedId = getHttpRequest().getParameter("selectedId");
 
 			if (Constants.SETTINGS_TEMPLATE_SERVER.equals(type)) {
 				List<DownloadInfo> servers = administrator.getServersByTech(techId, customerId);
 				if (CollectionUtils.isNotEmpty(servers)) {
 					for (DownloadInfo server : servers) {
 					    // TODO:Lohes
 						/*if (server.getId().equals(selectedId)) {
 							versions = server.getVersions();
 						}*/
 					}
 				}
 			}
 			if (Constants.SETTINGS_TEMPLATE_DB.equals(type)) {
 				List<DownloadInfo> databases = administrator.getDatabasesByTech(techId, customerId);
 				if (CollectionUtils.isNotEmpty(databases)) {
 					for (DownloadInfo database : databases) {
 					    // TODO:Lohes
 						/*if (database.getId().equals(selectedId)) {
 							versions.addAll(database.getVersions());
 						}*/
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return SUCCESS;
 	}
 
 	public String addDetails() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("entered Applications.addDetails()");
         }
 	    
 		String type = getHttpRequest().getParameter("type");
 		setSelectedParamName(getHttpRequest().getParameter("paramName"));
 		divTobeUpdated = getHttpRequest().getParameter("divTobeUpdated");
 		if ("Server".equals(type)) {
 			String[] selectedServerVersions = getHttpRequest()
 					.getParameterValues("serverVersion");
 			selectedVersions = convertToCommaDelimited(selectedServerVersions);
 		} else {
 			String[] selectedDbVersions = getHttpRequest().getParameterValues(
 					"databaseVersion");
 			selectedVersions = convertToCommaDelimited(selectedDbVersions);
 		}
 		setSelectedAttrType(type);
 
 		return SUCCESS;
 	}
 
 	private static String convertToCommaDelimited(String[] list) {
 		StringBuffer retString = new StringBuffer("");
 		for (int i = 0; list != null && i < list.length; i++) {
 			retString.append(list[i]);
 			if (i < list.length - 1) {
 				retString.append(',' + " ");
 			}
 		}
 
 		return retString.toString();
 	}
 
 	public String checkForRespectiveConfig() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("entered Applications.checkForRespectiveConfig()");
         }
 	    
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			Map<String, List<String>> deleteConfigs = new HashMap<String, List<String>>();
 			List<String> configNames = new ArrayList<String>();
 			List<SettingsInfo> configurations = administrator.configurations(
 					selectedAttrType, project);
 			if (CollectionUtils.isNotEmpty(configurations)) {
 				for (SettingsInfo config : configurations) {
 					deleteConfigs.clear();
 					configNames.clear();
 					PropertyInfo serverType = config
 							.getPropertyInfo(Constants.SERVER_TYPE);
 					if (serverType.getValue().equalsIgnoreCase(
 							selectedParamName)) {
 						setHasConfiguration(true);
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return SUCCESS;
 	}
 
 	private void removeConfiguration() throws PhrescoException {
 		try {
 			if (StringUtils.isNotEmpty(configServerNames)) {
 				deleteConfigurations("Server", configServerNames.substring(0, configServerNames.length() - 1));
 			}
 			if (StringUtils.isNotEmpty(configDbNames)) {
 				deleteConfigurations("Database",
 						configDbNames.substring(0, configDbNames.length() - 1));
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void deleteConfigurations(String type, String configName)
 			throws PhrescoException {
 		try {
 			String[] items = configName.split(",");
 			List<String> deleteConfigNames = Arrays.asList(items);
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			Project project = administrator.getProject(projectCode);
 			Map<String, List<String>> deleteConfigs = new HashMap<String, List<String>>();
 			List<String> configNames = new ArrayList<String>();
 			List<SettingsInfo> configurations = administrator.configurations(
 					type, project);
 			if (CollectionUtils.isNotEmpty(configurations)) {
 				for (SettingsInfo config : configurations) {
 					deleteConfigs.clear();
 					configNames.clear();
 					PropertyInfo serverType = config
 							.getPropertyInfo(Constants.SERVER_TYPE);
 					for (String deleteConfigName : deleteConfigNames) {
 						if (serverType.getValue().equalsIgnoreCase(
 								deleteConfigName)) {
 							configNames.add(config.getName());
 							deleteConfigs.put(config.getEnvName(), configNames);
 							administrator.deleteConfigurations(deleteConfigs,
 									project);
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public String checkForConfiguration() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("entered Applications.checkForConfiguration()");
         }
 	    
 		try {
 			boolean isError = false;
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			ApplicationInfo appInfo = administrator.getProject(projectCode).getApplicationInfo();
 
 			List<String> servers = appInfo.getSelectedServers();
 			List<String> databases = appInfo.getSelectedDatabases();
 			List<String> webservices = appInfo.getSelectedWebservices();
 			boolean emailSupported = appInfo.isEmailSupported();
 
 			String envs = getHttpRequest().getParameter(ENVIRONMENTS);
 			if (StringUtils.isEmpty(envs)) {
 				setHasError(false);
 				return SUCCESS;
 			}
 			String[] envArr = envs.split(",");
 			List<String> unAvailableTypes = new ArrayList<String>();
 			String from = getHttpRequest().getParameter(REQ_FROM);
 			for (String envName : envArr) {
 				if (NODEJS_RUN_AGAINST.equals(from)
 						|| JAVA_RUN_AGAINST.equals(from)) {
 					if (CollectionUtils.isEmpty(administrator.getSettingsInfos(
 							Constants.SETTINGS_TEMPLATE_SERVER, projectCode,
 							envName))) {
 						setEnvError(getText(ERROR_NO_CONFIG));
 						setHasError(true);
 						return SUCCESS;
 					}
 				}
 
 				if (servers != null
 						&& CollectionUtils.isNotEmpty(servers)
 						&& CollectionUtils.isEmpty(administrator
 								.getSettingsInfos(
 										Constants.SETTINGS_TEMPLATE_SERVER,
 										projectCode, envName))) {
 					isError = true;
 					unAvailableTypes.add("Server");
 				}
 
 				if (databases != null
 						&& CollectionUtils.isNotEmpty(databases)
 						&& CollectionUtils.isEmpty(administrator
 								.getSettingsInfos(
 										Constants.SETTINGS_TEMPLATE_DB,
 										projectCode, envName))) {
 					isError = true;
 					unAvailableTypes.add("Database");
 				}
 
 				if (webservices != null
 						&& CollectionUtils.isNotEmpty(webservices)
 						&& CollectionUtils.isEmpty(administrator
 								.getSettingsInfos(
 										Constants.SETTINGS_TEMPLATE_WEBSERVICE,
 										projectCode, envName))) {
 					isError = true;
 					unAvailableTypes.add("WebService");
 				}
 
 				if (emailSupported
 						&& CollectionUtils.isEmpty(administrator
 								.getSettingsInfos(
 										Constants.SETTINGS_TEMPLATE_EMAIL,
 										projectCode, envName))) {
 					isError = true;
 					unAvailableTypes.add("E-mail");
 				}
 
 				if (isError) {
 					String csvUnAvailableTypes = "";
 					if (CollectionUtils.isNotEmpty(unAvailableTypes)) {
 						csvUnAvailableTypes = StringUtils.join(
 								unAvailableTypes.toArray(), ",");
 					}
 					setEnvError(csvUnAvailableTypes
 							+ " Configurations not available for " + envName
 							+ " environment");
 					setHasError(true);
 					return SUCCESS;
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return SUCCESS;
 	}
 
 	public String checkForConfigType() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("entered Applications.checkForConfigType()");
         }
 	    
 		try {
 			String envs = getHttpRequest().getParameter(ENVIRONMENTS);
 			if (StringUtils.isEmpty(envs)) {
 				setHasError(false);
 				return SUCCESS;
 			}
 			String type = getHttpRequest().getParameter("type");
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			if (CollectionUtils.isEmpty(administrator.getSettingsInfos(type,
 					projectCode, envs))) {
 				setEnvError(getText(ERROR_ENV_CONFIG,
 						Collections.singletonList(type)));
 				setHasError(true);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return SUCCESS;
 	}
 	
 	public String browse() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.browse()");
 	    }
 	    
 		try {
 			getHttpRequest().setAttribute(FILE_TYPES, fileType);
 			getHttpRequest().setAttribute(FILE_BROWSE, fileorfolder);
 			String projectLocation = Utility.getProjectHome() + projectCode;
 			getHttpRequest().setAttribute(REQ_PROJECT_LOCATION, projectLocation.replace(File.separator, FORWARD_SLASH));
 			getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into catch block of  Applications.browse()"	+ FrameworkUtil.getStackTraceAsString(e));
 			new LogErrorReport(e, "File Browse");
 			
 			return LOG_ERROR;
 		}
 		return SUCCESS;
 	}
 
 	public String authenticateServer() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.authenticateServer()");
         }
 	    
 		try {
 			String host = (String)getHttpRequest().getParameter(SERVER_HOST);
 			int port = Integer.parseInt(getHttpRequest().getParameter(SERVER_PORT));
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			boolean connectionAlive = DiagnoseUtil.isConnectionAlive("https", host, port);
 			boolean isCertificateAvailable = false;
 			if (connectionAlive) {
 				List<CertificateInfo> certificates = administrator.getCertificate(host, port);
 				if (CollectionUtils.isNotEmpty(certificates)) {
 					isCertificateAvailable = true;
 					getHttpRequest().setAttribute("certificates", certificates);
 				}
 			}
 			getHttpRequest().setAttribute(FILE_TYPES, FILE_TYPE_CRT);
 			getHttpRequest().setAttribute(FILE_BROWSE, FILE_BROWSE);
 			String projectLocation = "";
 			if (StringUtils.isNotEmpty(projectCode)) {
 				projectLocation = Utility.getProjectHome() + projectCode;
 			} else {
 				projectLocation = Utility.getProjectHome();
 			}
 			getHttpRequest().setAttribute(REQ_PROJECT_LOCATION, projectLocation.replace(File.separator, FORWARD_SLASH));
 			getHttpRequest().setAttribute(REQ_RMT_DEP_IS_CERT_AVAIL, isCertificateAvailable);
 			getHttpRequest().setAttribute(REQ_RMT_DEP_FILE_BROWSE_FROM, CONFIGURATION);
 		} catch(Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return SUCCESS;
 	}
 
 	public boolean importFromGit(String url,File directory) throws Exception {
 	    if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Applications.importFromGit()");
     		S_LOGGER.debug("importing git " + url);
 	    }
	    Git repo = Git.cloneRepository().setURI(url).setDirectory(directory).call();
	    for (Ref b : repo.branchList().setListMode(ListMode.ALL).call()) {
 	    	S_LOGGER.debug("(standard): cloned branch " + b.getName());
 	    }
	    repo.getRepository().close();
 	    return true;
 	}
 	
 	public ApplicationInfo getApplicationInfo(File directory) throws Exception {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.getProjectInfo()");
 	    }
 	    
 		BufferedReader reader = null;
 		try {
 		    File dotProjectFile = new File(directory, FOLDER_DOT_PHRESCO + File.separator + PROJECT_INFO);
 		    S_LOGGER.debug("dotProjectFile" + dotProjectFile);
 		    if (!dotProjectFile.exists()) {
 		        throw new PhrescoException("Phresco Project definition not found");
 		    }
 		    reader = new BufferedReader(new FileReader(dotProjectFile));
 		    return new Gson().fromJson(reader, ApplicationInfo.class);
 		} finally {
 		    Utility.closeStream(reader);
 		}
 	}
 	
 	private void importToWorkspace(File gitImportTemp, String phrescoHomeDirectory, String projectCode) throws Exception {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.importToWorkspace()");
 	    }
 	    
 		File workspaceProjectDir = new File(phrescoHomeDirectory + projectCode);
 		S_LOGGER.debug("workspaceProjectDir "+ workspaceProjectDir);
 		if (workspaceProjectDir.exists()) {
 			S_LOGGER.debug("workspaceProjectDir exists"+ workspaceProjectDir);
 			throw new org.apache.commons.io.FileExistsException();
 		}
 		S_LOGGER.debug("gitImportTemp ====> " + gitImportTemp);
 		S_LOGGER.debug("workspaceProjectDir ====> " + workspaceProjectDir);
 		FileUtils.moveDirectory(gitImportTemp, workspaceProjectDir);
 	}
 	
 	private void updateSCMConnection(String projCode, String repoUrl) throws Exception {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method  Applications.updateSCMConnection()");
 	    }
 	    
 		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 		PomProcessor processor = frameworkUtil.getPomProcessor(projCode);
 		if (processor.getSCM() == null) {
 			S_LOGGER.debug("processor.getSCM() exists and repo url " + repoUrl);
 			processor.setSCM(repoUrl, "", "", "");
 			processor.save();
 		}
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
 
 	public String getRepourl() {
 		return repositoryUrl;
 	}
 
 	public void setRepourl(String repourl) {
 		this.repositoryUrl = repourl;
 	}
 
 	public String getUsername() {
 		return userName;
 	}
 
 	public void setUsername(String username) {
 		this.userName = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getRevision() {
 		return revision;
 	}
 
 	public void setRevision(String revision) {
 		this.revision = revision;
 	}
 
 	public String getRevisionVal() {
 		return revisionVal;
 	}
 
 	public void setRevisionVal(String revisionVal) {
 		this.revisionVal = revisionVal;
 	}
 
 	public String getShowSettings() {
 		return showSettings;
 	}
 
 	public void setShowSettings(String showSettings) {
 		this.showSettings = showSettings;
 	}
 
 	public String getGlobalValidationStatus() {
 		return globalValidationStatus;
 	}
 
 	public void setGlobalValidationStatus(String globalValidationStatus) {
 		this.globalValidationStatus = globalValidationStatus;
 	}
 	
 	public List<String> getVersions() {
 		return versions;
 	}
 
 	public void setVersions(List<String> versions) {
 		this.versions = versions;
 	}
 	
 	public String getSelectedVersions() {
 		return selectedVersions;
 	}
 
 	public void setSelectedVersions(String selectedVersions) {
 		this.selectedVersions = selectedVersions;
 	}
 	
 	public String getSelectedAttrType() {
 		return selectedAttrType;
 	}
 
 	public void setSelectedAttrType(String selectedAttrType) {
 		this.selectedAttrType = selectedAttrType;
 	}
 
 	public String getSelectedParamName() {
 		return selectedParamName;
 	}
 
 	public void setSelectedParamName(String selectedParamName) {
 		this.selectedParamName = selectedParamName;
 	}
 	
 	public String getDivTobeUpdated() {
 		return divTobeUpdated;
 	}
 
 	public void setDivTobeUpdated(String divTobeUpdated) {
 		this.divTobeUpdated = divTobeUpdated;
 	}
 	
 	public List<String> getSettingsEnv() {
 		return settingsEnv;
 	}
 
 	public void setSettingsEnv(List<String> settingsEnv) {
 		this.settingsEnv = settingsEnv;
 	}
 	
 	public boolean isHasError() {
 		return hasError;
 	}
 
 	public void setHasError(boolean hasError) {
 		this.hasError = hasError;
 	}
 	
 	public String getEnvError() {
 		return envError;
 	}
 
 	public void setEnvError(String envError) {
 		this.envError = envError;
 	}
 	
 	public List<String> getTechVersions() {
 		return techVersions;
 	}
 
 	public void setTechVersions(List<String> techVersions) {
 		this.techVersions = techVersions;
 	}
 	
 	public boolean isHasConfiguration() {
 		return hasConfiguration;
 	}
 
 	public void setHasConfiguration(boolean hasConfiguration) {
 		this.hasConfiguration = hasConfiguration;
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
 
 	public boolean isSvnImport() {
 		return svnImport;
 	}
 
 	public void setSvnImport(boolean svnImport) {
 		this.svnImport = svnImport;
 	}
 
 	public String getSvnImportMsg() {
 		return svnImportMsg;
 	}
 
 	public void setSvnImportMsg(String svnImportMsg) {
 		this.svnImportMsg = svnImportMsg;
 	}
 
 	public String getFromTab() {
 		return fromTab;
 	}
 
 	public void setFromTab(String fromTab) {
 		this.fromTab = fromTab;
 	}
 
 	public String getCredential() {
 		return credential;
 	}
 
 	public void setCredential(String credential) {
 		this.credential = credential;
 	}
 
 	public String getFileType() {
 		return fileType;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public String getFileorfolder() {
 		return fileorfolder;
 	}
 
 	public void setFileorfolder(String fileorfolder) {
 		this.fileorfolder = fileorfolder;
 	}
 
 	public String getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
     
     public String getApplication() {
 		return application;
 	}
 
 	public void setApplication(String application) {
 		this.application = application;
 	}
 	
 	public String getRepoType() {
 		return repoType;
 	}
 
 	public void setRepoType(String repoType) {
 		this.repoType = repoType;
 	}
 }
