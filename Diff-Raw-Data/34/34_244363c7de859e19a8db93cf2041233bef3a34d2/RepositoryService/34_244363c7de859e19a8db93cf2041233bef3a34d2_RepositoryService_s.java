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
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileExistsException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.InitCommand;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.eclipse.jgit.lib.Config;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNException;
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.impl.SCMManagerImpl;
 import com.photon.phresco.framework.model.RepoDetail;
 import com.photon.phresco.framework.model.RepoFileInfo;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.service.client.impl.ServiceManagerImpl;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.model.Scm;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class RepositoryService.
  */
 @Path("/repository")
 public class RepositoryService extends RestBase implements FrameworkConstants, ServiceConstants, ResponseCodes {
 	
 	String status;
 	String errorCode;
 	String successCode;
 	
 	/**
 	 * Import application from repository.
 	 *
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	@POST
 	@Path("/importApplication")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response importApplication(RepoDetail repodetail) {
 		Response response = null;
 		String type = repodetail.getType();
 		if (type.equals(SVN)) {
 			response = importSVNApplication(type, repodetail);
 		} else if (type.equals(GIT)) {
 			response = importGITApplication(type, repodetail);
 		} else if (type.equals(BITKEEPER)) {
 			response = importBitKeeperApplication(type, repodetail);
 		}
 		return response;
 	}
 
 	/**
 	 * Update imported applicaion  from repository.
 	 *
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	@POST
 	@Path("/updateImportedApplication")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateImportedApplicaion(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam("displayName") String displayName,
 			RepoDetail repodetail) {
 		Response response = null;
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			UUID uniqueKey = UUID.randomUUID();
 			String unique_key = uniqueKey.toString();
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			String type = repodetail.getType();
 			if (type.equals(SVN)) {
 				response = updateSVNProject(responseData, type, applicationInfo, repodetail, unique_key, displayName);
 			} else if (type.equals(GIT)) {
 				response = updateGitProject(responseData, type, applicationInfo, repodetail, unique_key, displayName);
 			} else if (type.equals(BITKEEPER)) {
 				response = updateBitKeeperProject(type, applicationInfo, repodetail, unique_key, displayName);
 			}
 			return response;
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Adds the project to repository.
 	 *
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @param userId the user id
 	 * @param projectId the project id
 	 * @param appId the app id
 	 * @return the response
 	 */
 	@POST
 	@Path("/addProjectToRepo")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response addProjectToRepo(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, RepoDetail repodetail,
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_PROJECTID) String projectId,
 			@QueryParam(REST_QUERY_APPID) String appId, @QueryParam("displayName") String displayName) {
 		Response response = null;
 		String type = repodetail.getType();
 		UUID uniqueKey = UUID.randomUUID();
 		String unique_key = uniqueKey.toString();
 		if (type.equals(SVN)) {
 			response = addSVNProject(appDirName, repodetail, userId, projectId, appId, type, unique_key, displayName);
 		} else if (type.equals(GIT)) {
 			response = addGitProject(userId, projectId, appId, appDirName, repodetail, type, unique_key, displayName);
 		}
 		return response;
 	}
 
 	/**
 	 * Commit imported project to repository.
 	 *
 	 * @param repodetail the repodetail
 	 * @param appDirName the app dir name
 	 * @return the response
 	 */
 	@POST
 	@Path("/commitProjectToRepo")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response commitImportedProject(RepoDetail repodetail, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,  @QueryParam("displayName") String displayName) {
 		Response response = null;
 		String type = repodetail.getType();
 		UUID uniqueKey = UUID.randomUUID();
 		String unique_key = uniqueKey.toString();
 		if (type.equals(SVN)) {
 			response = commitSVNProject(repodetail, appDirName, unique_key, displayName);
 		} else if (type.equals(GIT)) {
 			response = commitGitProject(appDirName, repodetail, type, unique_key, displayName);
 		} else if (type.equals(BITKEEPER)) {
 			response = commitBitKeeperProject(appDirName, repodetail, type, unique_key, displayName);
 		}
 		return response;
 	}
 
 	/**
 	 * Fetch pop up values.
 	 *
 	 * @param appDirName the app dir name
 	 * @param action the action
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@GET
 	@Path("/popupValues")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response fetchPopUpValues(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ACTION) String action, @QueryParam(REST_QUERY_USERID) String userId) {
 		Response response = null;
 		if (action.equals(COMMIT)) {
 			response = repoExistCheckForCommit(appDirName, action, userId);
 		} else if (action.equals(FrameworkConstants.UPDATE)) {
 			response = repoExistCheckForUpdate(appDirName, action, userId);
 		}
 		return response;
 	}
 
 	/**
 	 * Fetch log messages.
 	 *
 	 * @param repodetail the repodetail
 	 * @return the response
 	 * @throws PhrescoException the phresco exception
 	 */
 	@POST
 	@Path("/logMessages")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response fetchLogMessages(RepoDetail repodetail) throws PhrescoException {
 		List<String> restrictedLogs = null;
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			List<String> svnLogMessages = scmi.getSvnLogMessages(repodetail.getRepoUrl(), repodetail.getUserName(),
 					repodetail.getPassword());
 			restrictedLogs = restrictLogs(svnLogMessages);
 		} catch (PhrescoException e) {
 			if (e.getLocalizedMessage().contains("Authorization Realm")) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210023;
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()),
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if (e.getLocalizedMessage().contains("OPTIONS request failed on")
 					|| (e.getLocalizedMessage().contains("PROPFIND") && e.getLocalizedMessage().contains(
 							"405 Method Not Allowed"))
 					|| e.getLocalizedMessage().contains("Repository moved temporarily to")) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210038;
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()),
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210039;
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()),
 						null, status ,errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		}
 		status = RESPONSE_STATUS_SUCCESS;
 		successCode = PHR200023;
 		ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 				restrictedLogs, status, successCode);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Restrict logs.
 	 *
 	 * @param svnLogMessages the svn log messages
 	 * @return the list
 	 */
 	private List<String> restrictLogs(List<String> svnLogMessages) {
 		List<String> Messages = new ArrayList<String>();
 		if (svnLogMessages.size() > 5) {
 			for (int i = svnLogMessages.size() - 5; i <= svnLogMessages.size() - 1; i++) {
 				Messages.add(svnLogMessages.get(i));
 			}
 		} else {
 			for (int i = 0; i <= svnLogMessages.size() - 1; i++) {
 				Messages.add(svnLogMessages.get(i));
 			}
 		}
 		return Messages;
 	}
 
 	/**
 	 * Import svn application.
 	 *
 	 * @param type the type
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response importSVNApplication(String type, RepoDetail repodetail) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		ResponseInfo responseData = new ResponseInfo();
 		String revision = "";
 		try {
 			ApplicationInfo importProject = scmi.importProject(type, repodetail.getRepoUrl(), repodetail.getUserName(),
 					repodetail.getPassword(), null, repodetail.getRevision());
 			if (importProject != null) {
 				if (repodetail.isTestCheckOut()) {
 					String path = Utility.getProjectHome() + File.separator + importProject.getAppDirName()
 							+ File.separator;
 					File testFolder = new File(path, TEST);
 					if (!testFolder.exists()) {
 						testFolder.mkdirs();
 					}
 					FileUtils.cleanDirectory(testFolder);
 					String testSvnCheckout = scmi.svnCheckout(repodetail.getTestUserName(), repodetail
 							.getTestPassword(), repodetail.getTestRepoUrl(), testFolder.getPath(), repodetail
 							.getTestRevision());
 				}
 				status = RESPONSE_STATUS_SUCCESS;
 				successCode = PHR200017;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			} else {
				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210022;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (SVNAuthenticationException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210023;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (SVNException e) {
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210024;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210025;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210026;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (FileExistsException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210027;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210026;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210026;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Import git application.
 	 *
 	 * @param type the type
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response importGITApplication(String type, RepoDetail repodetail) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			ApplicationInfo importProject = scmi.importProject(type, repodetail.getRepoUrl(), repodetail.getUserName(),
 					repodetail.getPassword(), MASTER, repodetail.getRevision());
 			if (importProject != null) {
 				status = RESPONSE_STATUS_SUCCESS;
 				successCode = PHR200017;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			} else {
				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210022;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (FileExistsException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210027;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210026;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210026;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	/**
 	 * Import bit keeper application.
 	 *
 	 * @param type the type
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response importBitKeeperApplication(String type, RepoDetail repodetail) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			ApplicationInfo importProject = scmi.importProject(type, repodetail.getRepoUrl(), repodetail.getUserName(),
 					repodetail.getPassword(), null, null);
 			if (importProject != null) {
 				status = RESPONSE_STATUS_SUCCESS;
 				successCode = PHR200017;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			} else {
				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210022;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (Exception e) {
 			if ("Project already imported".equals(e.getLocalizedMessage())) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210027;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if ("Failed to import project".equals(e.getLocalizedMessage())) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210026;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210026;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		}
 	}
 
 	/**
 	 * Update git project.
 	 *
 	 * @param responseData the response data
 	 * @param type the type
 	 * @param applicationInfo the application info
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response updateGitProject(ResponseInfo responseData, String type, ApplicationInfo applicationInfo,
 			RepoDetail repodetail, String uniqueKey, String displayName) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), FrameworkConstants.UPDATE, displayName, uniqueKey)), true);
 			
 			scmi.updateProject(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(),
 					MASTER, null, applicationInfo);
 			
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200018;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (InvalidRemoteException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210024;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (TransportException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210024;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (SVNAuthenticationException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210023;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (SVNException e) {
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210024;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210025;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210028;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (FileExistsException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210027;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Update svn project.
 	 *
 	 * @param responseData the response data
 	 * @param type the type
 	 * @param applicationInfo the application info
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response updateSVNProject(ResponseInfo responseData, String type, ApplicationInfo applicationInfo,
 			RepoDetail repodetail, String uniqueKey, String displayName) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		String revision = "";
 		revision = !HEAD_REVISION.equals(repodetail.getRevision()) ? repodetail.getRevisionVal() : repodetail
 				.getRevision();
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), FrameworkConstants.UPDATE, displayName, uniqueKey)), true);
 			scmi.updateProject(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(), null,
 					revision, applicationInfo);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200018;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null,
 					null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (InvalidRemoteException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210024;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (TransportException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210024;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (SVNAuthenticationException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210023;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (SVNException e) {
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210024;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210025;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else if (e.getMessage().indexOf(SVN_IS_NOT_WORKING_COPY) != -1) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210029;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210028;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (FileExistsException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210027;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Update bit keeper project.
 	 *
 	 * @param type the type
 	 * @param applicationInfo the application info
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	private Response updateBitKeeperProject(String type, ApplicationInfo applicationInfo, RepoDetail repodetail, String uniqueKey, String displayName) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 	    	//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), FrameworkConstants.UPDATE, displayName, uniqueKey)), true);
 			scmi.updateProject(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(), null,
 					repodetail.getRevision(), applicationInfo);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200018;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (PhrescoException e) {
 			if (e.getLocalizedMessage().contains("Nothing to pull")) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210030;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210028;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210028;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Adds the svn project.
 	 *
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @param userId the user id
 	 * @param projectId the project id
 	 * @param appId the app id
 	 * @param type the type
 	 * @return the response
 	 */
 	private Response addSVNProject(String appDirName, RepoDetail repodetail, String userId, String projectId,
 			String appId, String type, String uniqueKey, String displayName) {
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), ADD_TO_REPO, displayName, uniqueKey)), true);
 			scmi.importToRepo(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(), null,
 					null, applicationInfo, repodetail.getCommitMessage());
 			User user = ServiceManagerImpl.USERINFO_MANAGER_MAP.get(userId);
 			// updateLatestProject(user, projectId, appId);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200019;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null,
 					null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210031;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Adds the git project.
 	 *
 	 * @param userId the user id
 	 * @param projectId the project id
 	 * @param appId the app id
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @param type the type
 	 * @return the response
 	 */
 	private Response addGitProject(String userId, String projectId, String appId, String appDirName,
 			RepoDetail repodetail, String type, String uniqueKey, String displayName) {
 		ResponseInfo responseData = new ResponseInfo();
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), ADD_TO_REPO, displayName, uniqueKey)), true);
 			scmi.importToRepo(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(), null,
 					null, applicationInfo, repodetail.getCommitMessage());
 			User user = ServiceManagerImpl.USERINFO_MANAGER_MAP.get(userId);
 			// updateLatestProject(user, projectId, appId);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200019;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null,
 					null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			if (e.getLocalizedMessage().contains("git-receive-pack not found")) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210032;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()),
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210031;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Commit svn project.
 	 *
 	 * @param repodetail the repodetail
 	 * @return the response
 	 */
 	public Response commitSVNProject(RepoDetail repodetail, String appDirName, String uniqueKey, String displayName) {
 		ResponseInfo responseData = new ResponseInfo();
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			if (CollectionUtils.isNotEmpty(repodetail.getCommitableFiles())) {
 				List<File> listModifiedFiles = new ArrayList<File>(repodetail.getCommitableFiles().size());
 				for (String commitableFile : repodetail.getCommitableFiles()) {
 					listModifiedFiles.add(new File(commitableFile));
 				}
 				//To generate the lock for the particular operation
 				FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), COMMIT, displayName, uniqueKey)), true);
 				
 				scmi.commitSpecifiedFiles(listModifiedFiles, repodetail.getUserName(), repodetail.getPassword(),
 						repodetail.getCommitMessage());
 			}
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200020;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null,
 					null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210033;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Commit bit keeper project.
 	 *
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @param type the type
 	 * @return the response
 	 */
 	public Response commitBitKeeperProject(String appDirName, RepoDetail repodetail, String type, String uniqueKey, String displayName) {
 		ResponseInfo responseData = new ResponseInfo();
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			File appDir = new File(Utility.getProjectHome() + appDirName);
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), COMMIT , displayName, uniqueKey)), true);
 			scmi.commitToRepo(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(), null,
 					null, appDir, repodetail.getCommitMessage());
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200020;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			if (e.getLocalizedMessage().contains("Nothing to push")) {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210034;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			} else {
 				status = RESPONSE_STATUS_ERROR;
 				errorCode = PHR210033;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), COMMIT_PROJECT_FAIL, null);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210033;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Commit git project.
 	 *
 	 * @param appDirName the app dir name
 	 * @param repodetail the repodetail
 	 * @param type the type
 	 * @return the response
 	 */
 	public Response commitGitProject(String appDirName, RepoDetail repodetail, String type, String uniqueKey, String displayName) {
 		ResponseInfo responseData = new ResponseInfo();
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(FrameworkServiceUtil.getLockDetail(applicationInfo.getId(), COMMIT , displayName, uniqueKey)), true);
 			String applicationHome = Utility.getProjectHome() + appDirName;
 			File appDir = new File(applicationHome);
 			if (!repodetail.getCommitableFiles().isEmpty()) {
 				scmi.commitToRepo(type, repodetail.getRepoUrl(), repodetail.getUserName(), repodetail.getPassword(),
 						null, null, appDir, repodetail.getCommitMessage());
 			}
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200020;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (Exception e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210033;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} finally {
 			try {
 				FrameworkServiceUtil.removeLock(uniqueKey);
 			} catch (PhrescoException e) {
 
 			}
 		}
 	}
 
 	/**
 	 * Gets the repo type.
 	 *
 	 * @param repoUrl the repo url
 	 * @return the repo type
 	 */
 	private String getRepoType(String repoUrl) {
 		String repoType = "";
 		if (repoUrl.startsWith("bk")) {
 			repoType = BITKEEPER;
 		} else if (repoUrl.endsWith(".git")) {
 			repoType = GIT;
 		} else if (repoUrl.contains("svn")) {
 			repoType = SVN;
 		}
 		return repoType;
 	}
 
 	/**
 	 * Repo exist check for commit.
 	 *
 	 * @param appDirName the app dir name
 	 * @param action the action
 	 * @param userId the user id
 	 * @return the response
 	 */
 	private Response repoExistCheckForCommit(String appDirName, String action, String userId) {
 		RepoDetail repodetail = new RepoDetail();
 		boolean setRepoExistForCommit = false;
 		ResponseInfo<RepoDetail> responseData = new ResponseInfo<RepoDetail>();
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			String repoUrl = getConnectionUrl(applicationInfo);
 			if (repoUrl.startsWith("bk")) {
 				setRepoExistForCommit = true;
 				repodetail.setRepoExist(setRepoExistForCommit);
 			} else if (repoUrl.endsWith(".git")) {
 				setRepoExistForCommit = checkGitProject(applicationInfo, setRepoExistForCommit);
 				repodetail.setRepoExist(setRepoExistForCommit);
 				repodetail = updateProjectPopup(appDirName, action, repodetail);
 			} else if (!setRepoExistForCommit) {
 				repodetail = updateProjectPopup(appDirName, action, repodetail);
 			}
 
 			repodetail.setUserName(userId);
 			repodetail.setType(getRepoType(repoUrl));
 			repodetail.setRepoUrl(repoUrl);
 			if (!repodetail.isRepoExist()) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210035;
 				ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, null,
 						repodetail, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			}
 
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210036;
 			ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 		status = RESPONSE_STATUS_SUCCESS;
 		successCode = PHR200021;
 		ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, null,
 				repodetail, status, successCode);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Repo exist check for update.
 	 *
 	 * @param appDirName the app dir name
 	 * @param action the action
 	 * @param userId the user id
 	 * @return the response
 	 */
 	private Response repoExistCheckForUpdate(String appDirName, String action, String userId) {
 		RepoDetail repodetail = new RepoDetail();
 		ResponseInfo<RepoDetail> responseData = new ResponseInfo<RepoDetail>();
 		try {
 			repodetail.setRepoExist(true);
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			String repoUrl = getConnectionUrl(applicationInfo);
 			if(StringUtils.isEmpty(repoUrl)) {
 				repodetail.setRepoExist(false);
 			}
 			repodetail.setType(getRepoType(repoUrl));
 			repodetail.setRepoUrl(repoUrl);
 			repodetail.setUserName(userId);
 			if (!repodetail.isRepoExist()) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210037;
 				ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, null,
 						repodetail, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			}
 
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_FAILURE;
 			errorCode = PHR210036;
 			ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, new Exception(e.getMessage()), null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 		status = RESPONSE_STATUS_SUCCESS;
 		successCode = PHR200022;
 		ResponseInfo<RepoDetail> finalOutput = responseDataEvaluation(responseData, null,
 				repodetail, status, successCode);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Update project popup.
 	 *
 	 * @param appDirName the app dir name
 	 * @param action the action
 	 * @param repodetail the repodetail
 	 * @return the repo detail
 	 */
 	private RepoDetail updateProjectPopup(String appDirName, String action, RepoDetail repodetail) {
 		boolean setRepoExistForCommit = false;
 		ResponseInfo<List<RepoFileInfo>> responseData = new ResponseInfo<List<RepoFileInfo>>();
 		List<RepoFileInfo> commitableFiles = null;
 		try {
 			ApplicationInfo applicationInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			setRepoExistForCommit = true;
 
 			// getting commitable files for SVN repo
 			if (COMMIT.equals(action) && !getConnectionUrl(applicationInfo).contains(BITKEEPER)
 					&& !getConnectionUrl(applicationInfo).contains(GIT)) {
 				commitableFiles = svnCommitableFiles(appDirName);
 
 				// getting commitable files for Git repo
 			} else if (COMMIT.equals(action) && !getConnectionUrl(applicationInfo).contains(BITKEEPER)
 					&& !getConnectionUrl(applicationInfo).contains(SVN)) {
 				commitableFiles = gitCommitableFiles(appDirName);
 			}
 		} catch (PhrescoException e) {
 			if (e.getLocalizedMessage().contains(IS_NOT_WORKING_COPY)) {
 				setRepoExistForCommit = false;
 			}
 		}
 		repodetail.setRepoInfoFile(commitableFiles);
 		repodetail.setRepoExist(setRepoExistForCommit);
 		return repodetail;
 	}
 
 	/**
 	 * Gets the connection url.
 	 *
 	 * @param applicationInfo the application info
 	 * @return the connection url
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String getConnectionUrl(ApplicationInfo applicationInfo) throws PhrescoException {
 		try {
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			PomProcessor processor = frameworkUtil.getPomProcessor(applicationInfo.getAppDirName());
 			Scm scm = processor.getSCM();
 			if (scm != null && !scm.getConnection().isEmpty()) {
 				return scm.getConnection();
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 		return "";
 	}
 
 	/**
 	 * Check git project.
 	 *
 	 * @param applicationInfo the application info
 	 * @param setRepoExistForCommit the set repo exist for commit
 	 * @return true, if successful
 	 * @throws PhrescoException the phresco exception
 	 */
 	private boolean checkGitProject(ApplicationInfo applicationInfo, boolean setRepoExistForCommit)
 			throws PhrescoException {
 		setRepoExistForCommit = true;
 		String url = "";
 		String Path = "";
 		if (applicationInfo != null) {
 			String appDirName = applicationInfo.getAppDirName();
 			Path = Utility.getProjectHome() + appDirName;
 		}
 		File projectPath = new File(Path);
 		InitCommand initCommand = Git.init();
 		initCommand.setDirectory(projectPath);
 		Git git = null;
 		try {
 			git = initCommand.call();
 		} catch (GitAPIException e) {
 			throw new PhrescoException(e);
 		}
 
 		Config storedConfig = git.getRepository().getConfig();
 		url = storedConfig.getString(REMOTE, ORIGIN, URL);
 		if (StringUtils.isEmpty(url)) {
 			File toDelete = git.getRepository().getDirectory();
 			try {
 				FileUtils.deleteDirectory(toDelete);
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 			setRepoExistForCommit = false;
 
 		}
 		git.getRepository().close();
 
 		return setRepoExistForCommit;
 	}
 
 	/**
 	 * Svn commitable files.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the list
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<RepoFileInfo> svnCommitableFiles(String appDirName) throws PhrescoException {
 		List<RepoFileInfo> commitableFiles = null;
 		String revision = "";
 		try {
 
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = FrameworkServiceUtil.getApplicationHome(appDirName);
 			File appDir = new File(applicationHome);
 			revision = HEAD_REVISION;
 			commitableFiles = scmi.getCommitableFiles(appDir, revision);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return commitableFiles;
 	}
 
 	/**
 	 * Git commitable files.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the list
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<RepoFileInfo> gitCommitableFiles(String appDirName) throws PhrescoException {
 		List<RepoFileInfo> gitCommitableFiles = null;
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = FrameworkServiceUtil.getApplicationHome(appDirName);
 			File appDir = new File(applicationHome);
 			gitCommitableFiles = scmi.getGITCommitableFiles(appDir);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return gitCommitableFiles;
 	}
 
 	/**
 	 * Update latest project.
 	 *
 	 * @param user the user
 	 * @param projectId the project id
 	 * @param appId the app id
 	 * @throws PhrescoException the phresco exception
 	 */
 	private void updateLatestProject(User user, String projectId, String appId) throws PhrescoException {
 		try {
 			File tempPath = new File(Utility.getPhrescoTemp() + File.separator + USER_PROJECT_JSON);
 			JSONObject userProjJson = null;
 			JSONParser parser = new JSONParser();
 			if (tempPath.exists()) {
 				FileReader reader = new FileReader(tempPath);
 				userProjJson = (JSONObject) parser.parse(reader);
 				reader.close();
 			} else {
 				userProjJson = new JSONObject();
 			}
 
 			userProjJson.put(user.getId(), projectId + Constants.STR_COMMA + appId);
 			FileWriter writer = new FileWriter(tempPath);
 			writer.write(userProjJson.toString());
 			writer.close();
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (ParseException e) {
 			throw new PhrescoException(e);
 		}
 	}
 }
