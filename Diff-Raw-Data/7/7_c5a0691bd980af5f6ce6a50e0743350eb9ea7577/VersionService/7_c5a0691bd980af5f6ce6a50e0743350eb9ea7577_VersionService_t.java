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
 package com.photon.phresco.service.rest.api;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.sonatype.aether.resolution.VersionRangeResolutionException;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.VersionInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.DbManager;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.util.ServerConstants;
 import com.photon.phresco.util.ServiceConstants;
 import com.wordnik.swagger.annotations.ApiOperation;
 import com.wordnik.swagger.annotations.ApiParam;
 
 @Controller
 @RequestMapping(value = ServiceConstants.REST_API_VERSION)
 public class VersionService implements ServerConstants {
 	
 	private static final String UPDATE_ZIP = "/update.zip";
 	private static final String UPDATE = "/update";
 	private static final String VERSION_PATH = "{version}";
 	private static final String VERSION = "version";
 	private static final String STR_DOT = ".";
 	private static final String STR_HIPHEN = "-";
 	private static final String ALPHA = "alpha";
 	private static final String BETA = "beta";	
 	private static final String SNAPSHOT = "SNAPSHOT";
 	private static final String UPDATE_FILE_PATH = "/com/photon/phresco/framework/release/";
 	private static final Logger S_LOGGER = Logger.getLogger("SplunkLogger");
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	@ApiOperation(value = " Get latest version ")
 	@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody VersionInfo getVersionInfo(
 		@ApiParam(value = "Current version of framework", name = "version") @QueryParam(VERSION) String version) 
 		throws VersionRangeResolutionException, PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method VersionServic.getVersionJSON(@PathParam(version) String currentVersion)");
 		}
 		return getVersion(version);
 	}
 	
 	@ApiOperation(value = " Get latest version ")
     @RequestMapping(value= VERSION_PATH, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody VersionInfo getVersionJSON(@ApiParam(value = "Current version of framework", name = "version") 
 			@PathVariable(VERSION) String version) throws VersionRangeResolutionException, PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method VersionServic.getVersionJSON(@PathParam(version) String currentVersion)");
 		}
 		
 		return getVersion(version);
 	}
 	
 	@ApiOperation(value = " Get update content ")
     @RequestMapping(value= UPDATE, produces = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.GET)
	public @ResponseBody byte[] getLatestVersionContent(
 			@ApiParam(value = "Customerid", name = ServiceConstants.REST_QUERY_CUSTOMERID) 
 			@QueryParam(ServiceConstants.REST_QUERY_CUSTOMERID) String customerId) throws PhrescoException, IOException  {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method VersionServic.getVersionJSON(@PathParam(version) String currentVersion)");
 		}
 		DbManager dbManager = PhrescoServerFactory.getDbManager();
 		URL url = null;
 		try {
 			String repoURL = "";
 			RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
 			if(repoInfo == null) {
 				repoInfo = dbManager.getRepoInfoById(ServiceConstants.UPDATE_REPO_ID);
 				repoURL = repoInfo.getReleaseRepoURL();
 			} else {
 				repoURL = repoInfo.getGroupRepoURL();
 			}
 			url = new URL(repoURL + UPDATE_FILE_PATH + 
 					dbManager.getLatestFrameWorkVersion() + UPDATE_ZIP);
 		} catch (MalformedURLException e) {
 			throw new PhrescoException(e);
 		}
		return IOUtils.toByteArray(url.openStream());
 	}
 	
 	private VersionInfo getVersion(String currentVersion) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("getVersionJSON() Getting the current Version=" + currentVersion + "");
 		}
 		
 		String latestVersion = PhrescoServerFactory.getDbManager().getLatestFrameWorkVersion();
 		VersionInfo versionInfo = new VersionInfo();
 		if (isUpdateRequired(currentVersion, latestVersion)) {
 			versionInfo.setUpdateAvailable(true);
 			versionInfo.setMessage("Update is available");
 		} else {
 			versionInfo.setUpdateAvailable(false);
 			versionInfo.setMessage("No update is available");
 		}
 		versionInfo.setFrameworkVersion(latestVersion);
 		
 		return versionInfo;
 	}
 	
 	private boolean isUpdateRequired(String currentVersion, String latestVersion) {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method VersionServic.isUpdateRequired(String currentVersion, String latestVersion)");
 		}
 		if (isDebugEnabled) {
 			S_LOGGER.debug("isUpdateRequired() currentVersion=" + currentVersion + " LatestVersion=" + latestVersion);
 		}
 		boolean updateRequired = false;
 		if (isSnapshot(currentVersion, latestVersion)) {
 			if (isDebugEnabled) {
 				S_LOGGER.debug("Ignoring update check because of SNAPSHOT version");
 			}
 			return false;
 		}
 
 		String curVerNumericPart = "";
 		String curVerAlphabetPart = "";
 		if (currentVersion.contains(ALPHA) || currentVersion.contains(BETA)) {
 			curVerNumericPart = currentVersion.substring(0, currentVersion.indexOf(STR_HIPHEN));
 			curVerAlphabetPart = currentVersion.substring(currentVersion.indexOf(STR_HIPHEN) + 1);
 		} else {
 			curVerNumericPart = currentVersion;
 		}
 
 		String latVerNumericPart = "";
 		String latVerAlphabetPart = "";
 		if (latestVersion.contains(ALPHA) || latestVersion.contains(BETA)) {
 			latVerNumericPart = latestVersion.substring(0, latestVersion.indexOf(STR_HIPHEN));
 			latVerAlphabetPart = latestVersion.substring(latestVersion.indexOf(STR_HIPHEN) + 1);
 		} else {
 			latVerNumericPart = latestVersion;
 		}
 
 		if (isUpdateRequiredNumeric(curVerNumericPart, latVerNumericPart)) {
 			updateRequired = true;
 		} else if (!curVerAlphabetPart.isEmpty() && !latVerAlphabetPart.isEmpty()) {
 			updateRequired = isUpdateRequiredAlphabetic(curVerAlphabetPart, latVerAlphabetPart);
 		}
 
 		return updateRequired;
 	}
 
 	private boolean isSnapshot(String currentVersion, String latestVersion) {
 		boolean isSnapshotVersion = false;
 		if (currentVersion.contains(SNAPSHOT) || latestVersion.contains(SNAPSHOT)) {
 			isSnapshotVersion = true;
 		}
 		return isSnapshotVersion;
 	}
 
 	private boolean isUpdateRequiredAlphabetic(String curVerAlphabetPart, String latVerAlphabetPart) {
 		boolean isUpdate = false;
 		String currentVersion = "";
 		String latestVersion = "";
 
 		if (curVerAlphabetPart.contains(ALPHA) && latVerAlphabetPart.contains(BETA)) {
 			return true;
 		}
 
 		if (curVerAlphabetPart.indexOf(STR_HIPHEN) > 0) {
 			currentVersion = curVerAlphabetPart.substring(curVerAlphabetPart.indexOf(STR_HIPHEN) + 1);
 		}
 		if (latVerAlphabetPart.indexOf(STR_HIPHEN) > 0) {
 			latestVersion = latVerAlphabetPart.substring(latVerAlphabetPart.indexOf(STR_HIPHEN) + 1);
 		}
 		if (currentVersion.isEmpty() && !latestVersion.isEmpty()) {
 			isUpdate = true;
 		}
 		if (!currentVersion.isEmpty() && !latestVersion.isEmpty()) {
 			return isUpdateRequiredNumeric(currentVersion, latestVersion);
 		}
 		return isUpdate;
 	}
 
 	private boolean isUpdateRequiredNumeric(String currentVersion, String latestVersion) {
 		StringTokenizer cvst = new StringTokenizer(currentVersion, STR_DOT);
 		List<String> currentVersionList = new ArrayList<String>();
 		while (cvst.hasMoreTokens()) {
 			currentVersionList.add(cvst.nextToken());
 		}
 		StringTokenizer lvst = new StringTokenizer(latestVersion, STR_DOT);
 		List<String> latestVersionList = new ArrayList<String>();
 		while (lvst.hasMoreTokens()) {
 			latestVersionList.add(lvst.nextToken());
 		}
 		
 		//Below condition solves the major release vs minor release
 		// For Ex: if we release the major release as 1.2.0 and the previous
 		// minor release as 1.2.0.16000 then the below condition provides
 		// the update available option
 		if (latestVersionList.size() < currentVersionList.size()) {
 			return true;
 		}
 
 		alignVersionSize(currentVersionList, latestVersionList);
 
 		int count = currentVersionList.size();
 		for (int i = 0; i < count; i++) {
 			int cvInt = Integer.parseInt(currentVersionList.get(i));
 			int lvInt = Integer.parseInt(latestVersionList.get(i));
 			if (lvInt > cvInt) {
 				return true;
 			} else if (cvInt > lvInt) {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	private void alignVersionSize(List<String> currentVersionList, List<String> latestVersionList) {
 		int cvSize = currentVersionList.size();
 		int lvSize = latestVersionList.size();
 		if (cvSize > lvSize) {
 			int count = cvSize - lvSize;
 			for (int i = 0; i < count; i++) {
 				latestVersionList.add("0");
 			}
 		} else if (cvSize < lvSize) {
 			int count = lvSize - cvSize;
 			for (int i = 0; i < count; i++) {
 				currentVersionList.add("0");
 			}
 		}
 	}
 	
 }
