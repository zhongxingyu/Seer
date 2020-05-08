 /**
  * Phresco Framework Implementation
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
 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.Proxy.Type;
 import java.net.ProxySelector;
 import java.net.SocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.InitCommand;
 import org.eclipse.jgit.api.PushCommand;
 import org.eclipse.jgit.api.ResetCommand;
 import org.eclipse.jgit.api.ResetCommand.ResetType;
 import org.eclipse.jgit.api.Status;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.errors.UnsupportedCredentialItem;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.lib.StoredConfig;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 import org.eclipse.jgit.transport.CredentialItem;
 import org.eclipse.jgit.transport.CredentialsProvider;
 import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
 import org.eclipse.jgit.transport.JschConfigSessionFactory;
 import org.eclipse.jgit.transport.OpenSshConfig;
 import org.eclipse.jgit.transport.SshSessionFactory;
 import org.eclipse.jgit.transport.URIish;
 import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
 import org.tmatesoft.svn.core.SVNCommitInfo;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.ISVNCommitParameters;
 import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNCommitClient;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNStatus;
 import org.tmatesoft.svn.core.wc.SVNStatusType;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 import com.google.gson.Gson;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UserInfo;
 import com.microsoft.tfs.core.TFSTeamProjectCollection;
 import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
 import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
 import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
 import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissionProfile;
 import com.microsoft.tfs.core.clients.versioncontrol.path.LocalPath;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.GetRequest;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.ItemSpec;
 import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
 import com.microsoft.tfs.core.clients.workitem.project.ProjectCollection;
 import com.microsoft.tfs.core.httpclient.Credentials;
 import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
 import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
 import com.microsoft.tfs.core.util.CredentialsUtils;
 import com.microsoft.tfs.core.util.URIUtils;
 import com.perforce.p4java.client.IClient;
 import com.perforce.p4java.core.file.FileSpecBuilder;
 import com.perforce.p4java.core.file.FileSpecOpStatus;
 import com.perforce.p4java.core.file.IFileSpec;
 import com.perforce.p4java.exception.ConnectionException;
 import com.perforce.p4java.exception.P4JavaException;
 import com.perforce.p4java.exception.RequestException;
 import com.perforce.p4java.impl.generic.client.ClientOptions;
 import com.perforce.p4java.impl.generic.client.ClientView;
 import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
 import com.perforce.p4java.impl.mapbased.client.Client;
 import com.perforce.p4java.option.client.SyncOptions;
 import com.perforce.p4java.server.IOptionsServer;
 import com.perforce.p4java.server.ServerFactory;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.LockUtil;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ModuleInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.SCMManager;
 import com.photon.phresco.framework.model.RepoDetail;
 import com.photon.phresco.framework.model.RepoFileInfo;
 import com.photon.phresco.framework.model.RepoInfo;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.model.Build;
 import com.phresco.pom.model.Model.Profiles;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 
 
 public class SCMManagerImpl implements SCMManager, FrameworkConstants {
 
 	private static final Logger S_LOGGER = Logger.getLogger(SCMManagerImpl.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 	public static String HTTP_PROXY_URL = "";
 	boolean dotphresco ;
 	SVNClientManager cm = null;
 
 	public ApplicationInfo importProject(RepoInfo repoInfo, String displayName, String uniqueKey) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importProject()");
 		}
 	
 		ApplicationInfo applicaionInfo = null;
 		if(repoInfo.isSplitPhresco()) {
 			applicaionInfo = validatePhrescoProject(repoInfo.getPhrescoRepoDetail());
 		} else {
 			applicaionInfo = validatePhrescoProject(repoInfo.getSrcRepoDetail());
 		}
 		
 		if(applicaionInfo != null) {
 			RepoDetail srcRepoDetail = repoInfo.getSrcRepoDetail();
 			if (SVN.equals(srcRepoDetail.getType())) {
 				checkoutSVN(applicaionInfo, repoInfo, 
 						displayName, uniqueKey);
 				if(debugEnabled){
 					S_LOGGER.debug("SVN type");
 				}
 			} else if (GIT.equals(srcRepoDetail.getType())) {
 				if(debugEnabled){
 					S_LOGGER.debug("GIT type");
 				}
 				String uuid = UUID.randomUUID().toString();
 				File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				if(debugEnabled){
 					S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				}
 				if (gitImportTemp.exists()) {
 					if(debugEnabled){
 						S_LOGGER.debug("Empty git directory need to be removed before importing from git ");
 					}
 					FileUtils.deleteDirectory(gitImportTemp);
 				}
 				if(debugEnabled){
 					S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				}
 				importFromGit(repoInfo.getSrcRepoDetail(), gitImportTemp);
 				if(debugEnabled){
 					S_LOGGER.debug("Validating Phresco Definition");
 				}
 				cloneFilter(applicaionInfo, gitImportTemp, displayName, uniqueKey, repoInfo);
 				if (gitImportTemp.exists()) {
 					if(debugEnabled){
 						S_LOGGER.debug("Deleting ~Temp");
 					}
 					FileUtils.deleteDirectory(gitImportTemp);
 				}
 				if(debugEnabled){
 					S_LOGGER.debug("Completed");
 				}
 			} else if (BITKEEPER.equals(srcRepoDetail.getType())) {
 				String uuid = UUID.randomUUID().toString();
 				File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				boolean isImported = importFromBitKeeper(srcRepoDetail.getRepoUrl(), bkImportTemp);
 				if (isImported) {
                 	String appId = applicaionInfo.getId();
                 	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, 
                 			displayName, uniqueKey)), true);
                 	String workDirPath = workDirPath(repoInfo, applicaionInfo);
         			importToWorkspace(bkImportTemp, new File(workDirPath.toString()));
 		        } 	
 			} else if(PERFORCE.equals(srcRepoDetail.getType())){
 				String uuid = UUID.randomUUID().toString();
 				File tempFile = new File(Utility.getPhrescoTemp(), uuid);
 				importFromPerforce(srcRepoDetail, tempFile);
 				String workDirPath = workDirPath(repoInfo, applicaionInfo);
 				importToWorkspace(tempFile, new File(workDirPath));
 			} else if (TFS.equals(srcRepoDetail.getType())){
 				String uuid = UUID.randomUUID().toString();
 				File tfsImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				importFromTfs(srcRepoDetail, tfsImportTemp);
 				String workDirPath = workDirPath(repoInfo, applicaionInfo);
 				importToWorkspace(tfsImportTemp, new File(workDirPath));
 			}
 		}
 		
 		return applicaionInfo;
 	}
 	
 	private String workDirPath(RepoInfo repoInfo, ApplicationInfo applicaionInfo) throws Exception {
 		StringBuilder str = new StringBuilder(Utility.getProjectHome()).append(applicaionInfo.getAppDirName());
     	if(repoInfo.isSplitPhresco() || repoInfo.isSplitTest()) {
 //    		File pom = getPomFromRepository(applicaionInfo, repoInfo);
 //    		PomProcessor processor = new PomProcessor(pom);
 //    		String property = processor.getProperty("sourcename");
     		str.append(File.separator).append(applicaionInfo.getAppDirName());
     	}
     	return str.toString();
 	}
 	
 	private ApplicationInfo validatePhrescoProject(RepoDetail phrescoRepoDetail) throws PhrescoException {
 		ApplicationInfo appInfo = null;
 		try {
 			if(phrescoRepoDetail.getType().equals(SVN)) {
 				appInfo = checkOutFilter(phrescoRepoDetail);
 			}
 			if(phrescoRepoDetail.getType().equals(GIT)) {
 				String uuid = UUID.randomUUID().toString();
 				File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				importFromGit(phrescoRepoDetail, gitImportTemp);
 				ProjectInfo gitAppInfo = getGitAppInfo(gitImportTemp);
 				if(gitAppInfo != null) {
 					appInfo = returnAppInfo(gitAppInfo);
 				}
 			}
 			if(phrescoRepoDetail.getType().equals(BITKEEPER)) {
 				String uuid = UUID.randomUUID().toString();
 				File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				boolean imported = importFromBitKeeper(phrescoRepoDetail.getRepoUrl(), bkImportTemp);
 				if(imported) {
 					ProjectInfo projectInfo = getGitAppInfo(bkImportTemp);
 					if (projectInfo != null) {
 						appInfo = returnAppInfo(projectInfo);
 					}
 				}
 			}
 			if(phrescoRepoDetail.getType().equals(PERFORCE)) {
 				String uuid = UUID.randomUUID().toString();
 				File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				appInfo = importFromPerforce(phrescoRepoDetail, bkImportTemp);
 			}
 			if (phrescoRepoDetail.getType().equals(TFS)) {
 				String uuid = UUID.randomUUID().toString();
 				File tfsTemp = new File(Utility.getPhrescoTemp(), uuid);
 				appInfo = importFromTfs(phrescoRepoDetail, tfsTemp);
 			}
 		} catch(Exception e){
 			throw new PhrescoException(e);
 		}
 		return appInfo;
 	}
 
 	private SVNURL getSVNURL(String repoURL) throws PhrescoException {
 		SVNURL svnurl = null;
 		try {
 			svnurl = SVNURL.parseURIEncoded(repoURL);
 		} catch (SVNException e) {
 			throw new PhrescoException(e);
 		}
 		return svnurl;
 	}
 	
 	private SVNClientManager getSVNClientManager(String userName, String password) {
 		DAVRepositoryFactory.setup();
 		DefaultSVNOptions options = new DefaultSVNOptions();
 		return SVNClientManager.newInstance(options, userName, password);
 	}
 	
 	private ApplicationInfo returnAppInfo(ProjectInfo projectInfo) {
 		List<ApplicationInfo> appInfos = null;
 		ApplicationInfo applicationInfo = null;
 		if (projectInfo != null) {
 		appInfos = projectInfo.getAppInfos();
 		}
 		if (appInfos != null) {
 		applicationInfo = appInfos.get(0);
 		}
 		if (applicationInfo != null) {
 			return applicationInfo;
 		}
 		return null;
 	}
 
 	public boolean updateProject(RepoDetail repodetail, File updateDir) throws Exception  {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.updateproject()");
 		}
 		if (SVN.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("SVN type");
 			}
 			DAVRepositoryFactory.setup();
 			SVNClientManager svnClientManager = getSVNClientManager(repodetail.getUserName(), repodetail.getPassword());
 			if(debugEnabled){
 				S_LOGGER.debug("update SCM Connection " + repodetail.getRepoUrl());
 			}
 //			 updateSCMConnection(appInfo, repodetail.getRepoUrl());
 				// revision = HEAD_REVISION.equals(revision) ? revision
 				// : revisionVal;
 			if(debugEnabled){
 				S_LOGGER.debug("updateDir SVN... " + updateDir);
 				S_LOGGER.debug("Updating...");
 			}
 			SVNUpdateClient uc = svnClientManager.getUpdateClient();
 			uc.doUpdate(updateDir, SVNRevision.parse(repodetail.getRevision()), SVNDepth.UNKNOWN, true, true);
 			if(debugEnabled){
 				S_LOGGER.debug("Updated!");
 			}
 			return true;
 		} else if (GIT.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("GIT type");
 			}
 //			updateSCMConnection(appInfo, repodetail.getRepoUrl());
 			if(debugEnabled){
 				S_LOGGER.debug("updateDir GIT... " + updateDir);
 			}
 			//for https and ssh
 			additionalAuthentication(repodetail.getPassPhrase());
 			
 			Git git = Git.open(updateDir); // checkout is the folder with .git
 			git.pull().call(); // succeeds
 			git.getRepository().close();
 			if(debugEnabled){
 				S_LOGGER.debug("Updated!");
 			}
 			return true;
 		} else if (BITKEEPER.equals(repodetail.getType())) {
 		    if (debugEnabled) {
                 S_LOGGER.debug("BITKEEPER type");
             }
 		    updateFromBitKeeperRepo(repodetail.getRepoUrl(), updateDir.getPath());
 		} else if (PERFORCE.equals(repodetail.getType())) {
 		    if (debugEnabled) {
                 S_LOGGER.debug("PERFORCE type");
             }
 			String baseDir = updateDir.getAbsolutePath();
 		    perforceSync(repodetail, baseDir, updateDir.getName(),"update");
 //			updateSCMConnection(appInfo, repodetail.getRepoUrl()+repodetail.getStream());
 		}
 
 		return false;
 	}
 	
 	private boolean updateFromBitKeeperRepo(String repoUrl, String appDir) throws PhrescoException {
         BufferedReader reader = null;
         File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
         boolean isUpdated = false;
         try {
             List<String> commands = new ArrayList<String>();
             commands.add(BK_PARENT + SPACE + repoUrl);
             commands.add(BK_PULL);
             for (String command : commands) {
                 Utility.executeStreamconsumer(appDir, command, new FileOutputStream(file));
             }
             reader = new BufferedReader(new FileReader(file));
             String strLine;
             while ((strLine = reader.readLine()) != null) {
                 if (strLine.contains("Nothing to pull")) {
                     throw new PhrescoException("Nothing to pull");
                 } else if (strLine.contains("[pull] 100%")) {
                     isUpdated = true;
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     throw new PhrescoException(e);
                 }
             }
             if (file.exists()) {
                 file.delete();
             }
         }
         
         return isUpdated;
     }
 
 	private void importToWorkspace(File gitImportTemp, File workspaceProjectDir) throws Exception {
 		try {
 			if(debugEnabled){
 				S_LOGGER.debug("Entering Method  SCMManagerImpl.importToWorkspace()");
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("workspaceProjectDir " + workspaceProjectDir);
 			}
 			if (workspaceProjectDir.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("workspaceProjectDir exists "+ workspaceProjectDir);
 				}
 				throw new PhrescoException(PROJECT_ALREADY);
 			}
 
 			if(debugEnabled){
 				S_LOGGER.debug("Copyin from Temp to workspace...");
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				S_LOGGER.debug("workspaceProjectDir " + workspaceProjectDir);
 			}
 			
 			FileUtils.copyDirectory(gitImportTemp, workspaceProjectDir);
 			if(debugEnabled){
 				S_LOGGER.debug("Deleting pack file");
 			}
 			FileUtils.deleteDirectory(gitImportTemp);
 		} catch (IOException e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering into catch block of importToWorkspace() "+ e.getLocalizedMessage());
 				S_LOGGER.error("pack file is not deleted ");
 			}
 		}
 	}
 
 	private void updateSCMConnection(ApplicationInfo appInfo, String repoUrl)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method SCMManagerImpl.updateSCMConnection()");
 		}
 		
 		try {
 			PomProcessor processor = getPomProcessor(appInfo);
 				if(debugEnabled){
 					S_LOGGER.debug("processor.getSCM() exists and repo url "+ repoUrl);
 				}
 			processor.setSCM(repoUrl, "", "", "");
 			processor.save();
 			
 			// To write in phresco-pom.xml
 			PomProcessor phrescoPomProcessor = getPhrescoPomProcessor(appInfo);
 			phrescoPomProcessor.setSCM(repoUrl, "", "", "");
 			phrescoPomProcessor.save();
 		} catch (Exception e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering catch block of updateSCMConnection()"+ e.getLocalizedMessage());
 			}
 			
 			throw new PhrescoException(POM_URL_FAIL);
 		}
 	}
 	
 	public void updateSCMConnection(RepoInfo repoInfo, ApplicationInfo appInfo)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method SCMManagerImpl.updateSCMConnection()");
 		}
 		try {
 			//Update scm in pom.xml
 			String repoUrl = repoInfo.getSrcRepoDetail().getRepoUrl();
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome()).append(File.separator).append(appInfo.getAppDirName());
 			if(repoInfo.isSplitPhresco() || repoInfo.isSplitTest()) {
 //				File pomfile = getPomFromRepository(appInfo, repoInfo);
 //				PomProcessor processor = new PomProcessor(pomfile);
 				builder.append(File.separator).append(appInfo.getAppDirName());
 			}
 			builder.append(File.separator).append(appInfo.getPomFile());
 			File pomFile = new File(builder.toString());
 			PomProcessor processor = new PomProcessor(pomFile);
 			processor.setSCM(repoUrl, "", "", "");
 			processor.save();
 			
 			// To write in phresco-pom.xml
 			if(StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 				if(repoInfo.isSplitPhresco()) {
 					builder = new StringBuilder(Utility.getProjectHome()).append(File.separator).append(appInfo.getAppDirName());
 					builder.append(File.separator).append(appInfo.getAppDirName()+ Constants.SUFFIX_PHRESCO).append(File.separator).append(appInfo.getPhrescoPomFile());
 					pomFile = new File(builder.toString());
 					PomProcessor phrescoPomProcessor = new PomProcessor(pomFile);
 					phrescoPomProcessor.setSCM(repoUrl, "", "", "");
 					phrescoPomProcessor.save();
 				}
 			}
 		} catch (Exception e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering catch block of updateSCMConnection()"+ e.getLocalizedMessage());
 			}
 			throw new PhrescoException(POM_URL_FAIL);
 		}
 	}
 	
 	private File getPomFromRepository(ApplicationInfo applicationInfo, RepoInfo repoInfo) throws Exception {
 		File pomFile = null;
 		String pomFileName = applicationInfo.getPhrescoPomFile();
 		RepoDetail phrescoRepoDetail = repoInfo.getPhrescoRepoDetail();
 		if(StringUtils.isEmpty(pomFileName)) {
 			pomFileName = applicationInfo.getPomFile();
 			phrescoRepoDetail = repoInfo.getSrcRepoDetail();
 		}
 		if(phrescoRepoDetail.getType().equals(SVN)) {
 			pomFile = getPomFromSVN(applicationInfo, phrescoRepoDetail, pomFileName);
 		}
 		if(phrescoRepoDetail.getType().equals(GIT)) {
 			pomFile = getPomFromGit(applicationInfo, phrescoRepoDetail, pomFileName);
 		}
 		if(phrescoRepoDetail.getType().equals(BITKEEPER)) {
 			pomFile = getPomFromBitkeeper(applicationInfo, phrescoRepoDetail, pomFileName);
 		}
 		
 		return pomFile;
 	}
 	
 	private File getPomFromBitkeeper(ApplicationInfo applicationInfo, RepoDetail repoDetail, String pomName) throws PhrescoException {
 		File pomfile = null;
 		String uuid = UUID.randomUUID().toString();
 		File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 		boolean imported = importFromBitKeeper(repoDetail.getRepoUrl(), bkImportTemp);
 		if(imported) {
 			pomfile = new File(bkImportTemp, pomName);
 		}
 		return pomfile;
 	}
 	
 	private PomProcessor getPomProcessor(ApplicationInfo appInfo)throws Exception {
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(appInfo.getAppDirName());
 			builder.append(File.separatorChar);
 			builder.append(Utility.getPomFileName(appInfo));
 			File pomPath = new File(builder.toString());
 			return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			throw new PhrescoException(NO_POM_XML);
 		}
 	}
 	
 	private PomProcessor getPhrescoPomProcessor(ApplicationInfo appInfo)throws Exception {
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(appInfo.getAppDirName());
 			builder.append(File.separatorChar);
 			builder.append(Utility.getPhrescoPomFile(appInfo));
 			File pomPath = new File(builder.toString());
 			return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			throw new PhrescoException(NO_POM_XML);
 		}
 	}
 	
 	private static void setupLibrary() {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.setupLibrary()");
 		}
 		DAVRepositoryFactory.setup();
 		SVNRepositoryFactoryImpl.setup();
 		FSRepositoryFactory.setup();
 	}
 
 	private ApplicationInfo checkOutFilter(RepoDetail srcRepoDetail) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.checkOutFilter()");
 		}
 		String repoURL = srcRepoDetail.getRepoUrl();
 		String userName = srcRepoDetail.getUserName();
 		String password = srcRepoDetail.getPassword();
 		setupLibrary();
 		SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repoURL));
 		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
 		repository.setAuthenticationManager(authManager);
 			SVNNodeKind nodeKind = repository.checkPath("", -1);
 			if (nodeKind == SVNNodeKind.NONE) {
 				if(debugEnabled){
 					S_LOGGER.error("There is no entry at '" + srcRepoDetail.getRepoUrl() + "'.");
 				}
 			} else if (nodeKind == SVNNodeKind.FILE) {
 				if(debugEnabled){
 					S_LOGGER.error("The entry at '" + srcRepoDetail.getRepoUrl() + " is a file while a directory was expected.");
 				}
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("Repository Root: " + repository.getRepositoryRoot(true));
 				S_LOGGER.debug("Repository UUID: " + repository.getRepositoryUUID(true));
 			}
 			return recurseMethod(repository, "", srcRepoDetail, getSVNURL(srcRepoDetail.getRepoUrl()), true);
 	}
 	
 	private ApplicationInfo recurseMethod(SVNRepository repository, String path, RepoDetail repoDetail, SVNURL svnURL, 
 			boolean recursive) throws Exception {
 		Collection entries = repository.getDir(path, -1, null, (Collection) null);
 		ApplicationInfo appInfo = dotPhrescoEvaluator(entries, repoDetail);
 		if (recursive) {
 			String repoUrlString = repoDetail.getRepoUrl();
 			svnURL = getSVNURL(repoUrlString);
 			Iterator iterator = entries.iterator();
 			if (entries.size() != 0) {
 				while (iterator.hasNext()) {
 					SVNDirEntry entry = (SVNDirEntry) iterator.next();
 					if ((entry.getKind() == SVNNodeKind.DIR)) {
 						SVNURL svnnewURL = svnURL.appendPath(FORWARD_SLASH + entry.getName(), true);
 						if(debugEnabled){
 							S_LOGGER.debug("Appended SVNURL for subdir " + svnURL);
 							S_LOGGER.debug("checking subdirectories");
 						}
 						recurseMethod(repository,(path.equals("")) ? entry.getName() : path
 								+ FORWARD_SLASH + entry.getName(), repoDetail ,svnnewURL, false);
 					}
 				}
 			}
 		} 
 		return appInfo;
 	}
 	
 	private ApplicationInfo dotPhrescoEvaluator(Collection entries, RepoDetail repoDetail) throws Exception {
 		Iterator iterator = entries.iterator();
 		if(debugEnabled){
 			S_LOGGER.debug("Entry size " + entries.size());
 		}
 		if (entries.size() != 0) {
 			while (iterator.hasNext()) {
 				SVNDirEntry entry = (SVNDirEntry) iterator.next();
 				if ((entry.getName().equals(FOLDER_DOT_PHRESCO))
 						&& (entry.getKind() == SVNNodeKind.DIR)) {
 					if(debugEnabled){
 						S_LOGGER.debug("Entry name " + entry.getName());
 						S_LOGGER.debug("Entry Path " + entry.getURL());
 					}
 					return retrieveAppInfo(repoDetail);
 				}
 			}
 		}
 		return null;
 	}
 	
 	private ApplicationInfo retrieveAppInfo(RepoDetail repoDetail) throws Exception {
 		ProjectInfo projectInfo = getSvnAppInfo(repoDetail);
 		if (projectInfo == null) {
 			if(debugEnabled){
 				S_LOGGER.debug("ProjectInfo is Empty");
 			}
 			return null;
 		}
 		List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 		if (appInfos == null) {
 			if(debugEnabled){
 				S_LOGGER.debug("AppInfo is Empty");
 			}
 			return null;
 		}
 		ApplicationInfo appInfo = appInfos.get(0);
 		return appInfo;
 	}
 	
 	boolean checkoutSVN(ApplicationInfo appInfo, RepoInfo repoInfo, String displayName, String uniqueKey) throws Exception {
 		RepoDetail srcRepoDetail = repoInfo.getSrcRepoDetail();
 		SVNUpdateClient uc = getSVNClientManager(srcRepoDetail.getUserName(), srcRepoDetail.getPassword()).getUpdateClient();
 		StringBuilder strbuilder = new StringBuilder(Utility.getProjectHome());
 		strbuilder.append(File.separator);
 		strbuilder.append(appInfo.getAppDirName());
 		if(repoInfo.isSplitPhresco() || repoInfo.isSplitTest()) {
 			strbuilder.append(File.separator);
 			strbuilder.append(appInfo.getAppDirName());
 		}
 		File file = new File(strbuilder.toString());
 		if (file.exists()) {
 			throw new PhrescoException(PROJECT_ALREADY);
         } else {
         	//generate import lock
         	String appId = appInfo.getId();
         	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, displayName, uniqueKey)), true);
         }
 		if(debugEnabled){
 			S_LOGGER.debug("Checking out...");
 		}
 		uc.doCheckout(getSVNURL(srcRepoDetail.getRepoUrl()), file, SVNRevision.UNDEFINED,
 				SVNRevision.parse(srcRepoDetail.getRevision()), SVNDepth.UNKNOWN,
 				false);
 		if(debugEnabled){
 			S_LOGGER.debug("updating pom.xml");
 		}
 		return true;
 	}
 	
 	private ApplicationInfo importFromTfs(RepoDetail repodetail, File tfsImportTemp) throws PhrescoException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importFromTfs()");
 		}
 		ProjectInfo projectInfo = null;
 		try {
 			System.getProperty("com.microsoft.tfs.jni.native.base-directory"); 
 			String collectionUrl = repodetail.getRepoUrl();
 			String userName = repodetail.getUserName();
 			String password = repodetail.getPassword();
 			String proName = repodetail.getProName();
 			String serverPath = "$/" +repodetail.getServerPath();
 			String localPath = tfsImportTemp.getAbsolutePath();
 			
 			final TFSTeamProjectCollection tpc = connectToTFS(userName, password, collectionUrl);
 			ProjectCollection projects = tpc.getWorkItemClient().getProjects();
 			VersionControlClient client = tpc.getVersionControlClient();
 			final Workspace workspace = createAndMapWorkspace(tpc, localPath, serverPath);
 			addGetEventListeners(tpc);
 			getLatest(workspace, localPath);
 			projectInfo = getGitAppInfo(tfsImportTemp);
 		} catch(Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return returnAppInfo(projectInfo);
 	}
 	
 	private static Workspace createAndMapWorkspace(final TFSTeamProjectCollection tpc,String localPath, String serverPath) throws PhrescoException {
 		final String workspaceName = "VCWorkspace" + System.currentTimeMillis();
 		Workspace workspace = null;
 
 		try {
 			// Get the workspace
 			workspace = tpc.getVersionControlClient().tryGetWorkspace(localPath);
 			// Create and map the workspace if it does not exist
 			if (workspace == null) {
 				workspace = tpc.getVersionControlClient().createWorkspace(null, workspaceName, "workspace comment", WorkspaceLocation.SERVER,
 						null, WorkspacePermissionProfile.getPrivateProfile());
 
 				// Map the workspace
 				WorkingFolder workingFolder = new WorkingFolder(serverPath, LocalPath.canonicalize(localPath));
 				workspace.createWorkingFolder(workingFolder);
 			}
 		} catch(Exception e){
 			throw new PhrescoException(e);
 		}
 
 		return workspace;
 	}
 	
 	public static void addGetEventListeners(final TFSTeamProjectCollection tpc) throws PhrescoException {
 		
 		try {
 		// Adding a get operation started event listener, this is fired once per
 		// get call
 		TfsGetOperationStartedListener getOperationStartedListener = new TfsGetOperationStartedListener();
 		tpc.getVersionControlClient().getEventEngine().addOperationStartedListener(getOperationStartedListener);
 
 		// Adding a get event listener, this fired once per get operation(which
 		// might be multiple times per get call)
 		TfsGetEventListener getListener = new TfsGetEventListener();
 		tpc.getVersionControlClient().getEventEngine().addGetListener(getListener);
 
 		// Adding a get operation completed event listener, this is fired once
 		// per get call
 		TfsGetOperationCompletedListener getOperationCompletedListener = new TfsGetOperationCompletedListener();
 		tpc.getVersionControlClient().getEventEngine().addOperationCompletedListener(getOperationCompletedListener);
 		} catch (Exception e){
 			throw new PhrescoException(e);
 		}
 	}
 
 	public static void getLatest(final Workspace workspace, String localPath) {
 		ItemSpec spec = new ItemSpec(localPath, RecursionType.FULL);
 		GetRequest request = new GetRequest(spec, LatestVersionSpec.INSTANCE);
 		workspace.get(request, GetOptions.NONE);
 	}
 	
 	public static TFSTeamProjectCollection connectToTFS(String userName, String password, String collectionUrl ) throws PhrescoException {
 		TFSTeamProjectCollection tpc = null;
 		Credentials credentials;
 
 		try {
 		// In case no username is provided and the current platform supports
 		// default credentials, use default credentials
 		if ((userName == null || userName.length() == 0) && CredentialsUtils.supportsDefaultCredentials()) {
 			credentials = new DefaultNTCredentials();
 		} else {
 			credentials = new UsernamePasswordCredentials(userName, password);
 		}
 
 		URI httpProxyURI = null;
 		if (HTTP_PROXY_URL != null && HTTP_PROXY_URL.length() > 0) {
 			try {
 				httpProxyURI = new URI(HTTP_PROXY_URL);
 			} catch (URISyntaxException e) {
 				// Do Nothing
 			}
 		}
 		ConsoleSamplesConnectionAdvisor connectionAdvisor = new ConsoleSamplesConnectionAdvisor(httpProxyURI);
 		tpc = new TFSTeamProjectCollection(URIUtils.newURI(collectionUrl), credentials, connectionAdvisor);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		return tpc;
 	}
 
 	private void importFromGit(RepoDetail repodetail, File gitImportTemp)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importFromGit()");
 		}	
 			if (StringUtils.isEmpty(repodetail.getBranch())) {
 				repodetail.setBranch(MASTER);
 			}
 			// For https and ssh
 			additionalAuthentication(repodetail.getPassPhrase());
 			
 			UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(repodetail.getUserName(), repodetail.getPassword());
 			Git r = Git.cloneRepository().setDirectory(gitImportTemp)
 			.setCredentialsProvider(userCredential)
 			.setURI(repodetail.getRepoUrl())
 //			.setProgressMonitor(new TextProgressMonitor())
 			.setBranch(repodetail.getBranch())
 			.call();
 	        r.getRepository().close();      
 	}
 	
 	void additionalAuthentication(String passPhrase) {
 		final String passwordPhrase = passPhrase;
 		JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
         	@Override
         	protected void configure(OpenSshConfig.Host hc, Session session) {
         	    CredentialsProvider provider = new CredentialsProvider() {
         	        @Override
         	        public boolean isInteractive() {
         	            return false;
         	        }
 
         	        @Override
         	        public boolean supports(CredentialItem... items) {
         	            return true;
         	        }
         	        
         	        @Override
         	        public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
         	            for (CredentialItem item : items) {
         	            	if (item instanceof CredentialItem.StringType) {
         	            		((CredentialItem.StringType) item).setValue(passwordPhrase);
         	            	}
         	            }
         	            return true;
         	        }
         	    };
         	    UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
         	    // Unknown host key for ssh
         	    java.util.Properties config = new java.util.Properties(); 
         	    config.put(STRICT_HOST_KEY_CHECKING, NO);
         	    session.setConfig(config);
         	    
         	    session.setUserInfo(userInfo);
         	}
     	};
     	
     	SshSessionFactory.setInstance(sessionFactory);
     	
     	/*
     	 * Enable clone of https url by trusting those urls
     	 */
 		// Create a trust manager that does not validate certificate chains
 		TrustManager[] trustAllCerts = new TrustManager[] { 
 		    new X509TrustManager() {     
 		        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
 		            return null;
 		        } 
 		        public void checkClientTrusted( 
 		            java.security.cert.X509Certificate[] certs, String authType) {
 		            } 
 		        public void checkServerTrusted( 
 		            java.security.cert.X509Certificate[] certs, String authType) {
 		        }
 		    } 
 		}; 
 		
 		final String https_proxy = System.getenv(HTTPS_PROXY);
 		final String http_proxy = System.getenv(HTTP_PROXY);
 		
 		ProxySelector.setDefault(new ProxySelector() {
 			final ProxySelector delegate = ProxySelector.getDefault();
 
 			@Override
 			public List<Proxy> select(URI uri) {
 				// Filter the URIs to be proxied
 				
 				
 				if (uri.toString().contains(HTTPS) && StringUtils.isNotEmpty(http_proxy) && http_proxy != null) {
 					try {
 						URI httpsUri = new URI(https_proxy);
 						String host = httpsUri.getHost();
 						int port = httpsUri.getPort();
 						return Arrays.asList(new Proxy(Type.HTTP, InetSocketAddress
 								.createUnresolved(host, port)));
 					} catch (URISyntaxException e) {
 						if(debugEnabled){
 							S_LOGGER.debug("Url exception caught in https block of additionalAuthentication()");
 						}	
 					}
 				}
 				
 				if (uri.toString().contains(HTTP) && StringUtils.isNotEmpty(http_proxy) && http_proxy != null) {
 					try {
 						URI httpUri = new URI(http_proxy);
 						String host = httpUri.getHost();
 						int port = httpUri.getPort();
 						return Arrays.asList(new Proxy(Type.HTTP, InetSocketAddress
 								.createUnresolved(host, port)));
 					} catch (URISyntaxException e) {
 						if(debugEnabled){
 							S_LOGGER.debug("Url exception caught in http block of additionalAuthentication()");
 						}					
 					}
 				}
 				
 				// revert to the default behaviour
 				return delegate == null ? Arrays.asList(Proxy.NO_PROXY)
 						: delegate.select(uri);
 			}
 
 			@Override
 			public void connectFailed(URI uri, SocketAddress sa,
 					IOException ioe) {
 				if (uri == null || sa == null || ioe == null) {
 					throw new IllegalArgumentException("Arguments can't be null.");
 				}
 			}
 		});
 
 		// Install the all-trusting trust manager
 		try {
 		    SSLContext sc = SSLContext.getInstance(SSL); 
 		    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
 		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		} catch (GeneralSecurityException e) {
 			e.getLocalizedMessage();
 		} 
 	}
 	
 	private boolean importFromBitKeeper(String repoUrl, File bkImportTemp) throws PhrescoException {
 	    BufferedReader reader = null;
 	    File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
 	    boolean isImported = false;
 	    try {
 	        String command = BK_CLONE + SPACE + repoUrl;
 			if(debugEnabled){
 				S_LOGGER.debug("bkImportTemp " + bkImportTemp);
 			}
 			if (bkImportTemp.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("Empty Bk directory need to be removed before importing from BitKeeper ");
 				}
 				FileUtils.deleteDirectory(bkImportTemp);
 			}
 	        Utility.executeStreamconsumer(bkImportTemp.getPath(), command, new FileOutputStream(file));
 	        reader = new BufferedReader(new FileReader(file));
 	        String strLine;
 	        while ((strLine = reader.readLine()) != null) {
 	            if (strLine.contains("OK")) {
 	                isImported = true;
 	                break;
 	            } else if (strLine.contains(ALREADY_EXISTS)) {
 	                throw new PhrescoException("Project already imported");
 	            } else if (strLine.contains("FAILED")) {
 	                throw new PhrescoException("Failed to import project");
 	            }
 	        }
 	        return isImported;
 	    } catch (IOException e) {
 	        throw new PhrescoException(e);
 	    } catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 	        if (reader != null) {
 	            try {
 	                reader.close();
 	            } catch (IOException e) {
 	                throw new PhrescoException(e);
 	            }
 	        }
 	        if (file.exists()) {
 	            file.delete();
 	        }
 	    }
 	}
 
 	private void cloneFilter(ApplicationInfo applicationInfo, File appDir, String displayName, 
 			String uniqueKey, RepoInfo repoInfo)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.cloneFilter()");
 		}
		File pomFromGit = null;
 		if (appDir.isDirectory()) {
 			//generate import lock
         	String appId = applicationInfo.getId();
         	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, 
         			displayName, uniqueKey)), true);
         	StringBuilder str = new StringBuilder(Utility.getProjectHome()).append(applicationInfo.getAppDirName());
         	if(repoInfo.isSplitPhresco() || repoInfo.isSplitTest()) {
 //        		pomFromGit = getPomFromRepository(applicationInfo, repoInfo);
 //        		PomProcessor processor = new PomProcessor(pomFromGit);
 //        		String property = processor.getProperty("sourcename");
         		str.append(File.separator).append(applicationInfo.getAppDirName());
         	}
         	importToWorkspace(appDir, new File(str.toString()));
 			if(debugEnabled){
 				S_LOGGER.debug("updating pom.xml");
 			}
 			// update connection in pom.xml
 //			updateSCMConnection(applicationInfo, repoInfo.getSrcRepoDetail().getRepoUrl());
			if(pomFromGit.exists()) {
				FileUtil.delete(pomFromGit.getParentFile());
			}
 		}
 	}
 	
 	private File getPomFromGit(ApplicationInfo applicationInfo, RepoDetail repoDetail, String pomFile) throws Exception {
 		String uuid = UUID.randomUUID().toString();
 		File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 		importFromGit(repoDetail, gitImportTemp);
 		File pom = new File(gitImportTemp, pomFile);
 		return pom;
 	}
 	
 	private ProjectInfo getGitAppInfo(File directory)throws PhrescoException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getGitAppInfo()");
 		}
 		
 		BufferedReader reader = null;
 		try {
 			File dotProjectFile = new File(directory, FOLDER_DOT_PHRESCO+ File.separator + PROJECT_INFO);
 			if(debugEnabled){
 				S_LOGGER.debug(dotProjectFile.getAbsolutePath());
 				S_LOGGER.debug("dotProjectFile" + dotProjectFile);
 			}
 			if (!dotProjectFile.exists()) {
 				return null;
 			}
 			reader = new BufferedReader(new FileReader(dotProjectFile));
 			return new Gson().fromJson(reader, ProjectInfo.class);
 		} catch (FileNotFoundException e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering into catch block of getGitAppInfo() "+ e.getLocalizedMessage());
 			}
 			throw new PhrescoException(INVALID_FOLDER);
 		} finally {
 			Utility.closeStream(reader);
 		}
 	}
 
 	private ProjectInfo getSvnAppInfo(RepoDetail repoDetail) throws  Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getSvnAppInfo()");
 		}
 		BufferedReader reader = null;
 		File tempDir = new File(Utility.getSystemTemp(), UUID.randomUUID().toString());
 		if(debugEnabled){
 			S_LOGGER.debug("temp dir : SVNAccessor " + tempDir);
 		}
 		String repoURL = repoDetail.getRepoUrl();
 		String userName = repoDetail.getUserName();
 		String password = repoDetail.getPassword();
 		String revision = repoDetail.getRevision();
 		try {
 			SVNClientManager svnClientManager = getSVNClientManager(userName, password);
 			SVNUpdateClient uc = svnClientManager.getUpdateClient();
 			try {
 			uc.doCheckout(getSVNURL(repoURL).appendPath(PHRESCO, true), tempDir,
 					SVNRevision.UNDEFINED, SVNRevision.parse(revision),
 					SVNDepth.UNKNOWN, false);
 			} catch(SVNException er) {
 				er.printStackTrace();
 				ProjectInfo projInfo = new ProjectInfo();
 				projInfo.setId("#SEP#");
 				return projInfo;
 			}
 			File dotProjectFile = new File(tempDir, PROJECT_INFO);
 			if (!dotProjectFile.exists()) {
 				throw new PhrescoException(INVALID_FOLDER);
 			}
 			reader = new BufferedReader(new FileReader(dotProjectFile));
 			return new Gson().fromJson(reader, ProjectInfo.class);
 		} finally {
 			Utility.closeStream(reader);
 
 			if (tempDir.exists()) {
 				FileUtil.delete(tempDir);
 			}
 		}
 	}
 	
 	public File getPomFromSVN(ApplicationInfo appInfo, RepoDetail repoDetail, String pomName) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getPomFromSVN()");
 		}
 		File tempDir = new File(Utility.getSystemTemp(), UUID.randomUUID().toString());
 		if(debugEnabled){
 			S_LOGGER.debug("temp dir : SVNAccessor " + tempDir);
 		}
 		SVNUpdateClient uc = getSVNClientManager(repoDetail.getUserName(), repoDetail.getPassword()).getUpdateClient();
 		try {
 			uc.doCheckout(getSVNURL(repoDetail.getRepoUrl()), tempDir,
 			SVNRevision.UNDEFINED, SVNRevision.parse(repoDetail.getRevision()),
 				SVNDepth.UNKNOWN, false);
 		} catch(SVNException er) {
 			er.printStackTrace();
 		}
 		File pom = new File(tempDir, pomName);
 		return pom;
 	}
 	
 	public boolean importToRepo(RepoInfo repoInfo, ApplicationInfo appInfo) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importToRepo()");
 		}
 		String appDirName = appInfo.getAppDirName();
 		String tempBasePath = Utility.getPhrescoTemp() + appDirName;
 		
 		File tempPhrescoFile = new File(tempBasePath + Constants.SUFFIX_PHRESCO);
 		File tempTestFile = new File(tempBasePath + Constants.SUFFIX_TEST);
 		File tempSrcFile = new File(tempBasePath);
 		try {
 			String phrescoDirName = appDirName + Constants.SUFFIX_PHRESCO;
 			String srcDirName = appDirName;
 			String testDirName = appDirName + Constants.SUFFIX_TEST;
 			
 			RepoDetail srcRepoDetail = repoInfo.getSrcRepoDetail();
 			String repoType = srcRepoDetail.getType();
 			String srcRepoUrl = srcRepoDetail.getRepoUrl();
 			
 			StringBuilder appendedSrcUrl = new StringBuilder(srcRepoUrl);
 			if (SVN.equalsIgnoreCase(repoType) && !srcRepoUrl.endsWith(TRUNK) && !srcRepoUrl.endsWith(TRUNK + FORWARD_SLASH)) {
 				if (!srcRepoUrl.endsWith(FORWARD_SLASH)) {
 					appendedSrcUrl.append(FORWARD_SLASH);
 				}
 				appendedSrcUrl.append(TRUNK);
 			}
 			if (!appendedSrcUrl.toString().endsWith(FORWARD_SLASH)) {
 				appendedSrcUrl.append(FORWARD_SLASH);
 			}
 			appendedSrcUrl.append(srcDirName);
 			
 			File dir = new File(Utility.getProjectHome() + appDirName);
 			boolean hasSplit = false; 
 			if (repoInfo.isSplitPhresco() || repoInfo.isSplitTest()) {
 				hasSplit = true;
 			}
 			if (hasSplit) {
 				RepoDetail phrescoRepoDetail = repoInfo.getPhrescoRepoDetail();
 				RepoDetail testRepoDetail = repoInfo.getTestRepoDetail();
 				
 				String phrescoRepoUrl = "";
 				StringBuilder appendedPhrUrl = new StringBuilder();
 				if (phrescoRepoDetail != null) {
 					phrescoRepoUrl = phrescoRepoDetail.getRepoUrl();
 					appendedPhrUrl.append(phrescoRepoUrl);
 					if (SVN.equalsIgnoreCase(testRepoDetail.getType()) && !phrescoRepoUrl.endsWith(TRUNK) && !phrescoRepoUrl.endsWith(TRUNK + FORWARD_SLASH)) {
 						if (!phrescoRepoUrl.endsWith(FORWARD_SLASH)) {
 							appendedPhrUrl.append(FORWARD_SLASH);
 						}
 						appendedPhrUrl.append(TRUNK);
 					}
 					if (!appendedPhrUrl.toString().endsWith(FORWARD_SLASH)) {
 						appendedPhrUrl.append(FORWARD_SLASH);
 					}
 					appendedPhrUrl.append(phrescoDirName);
 				}
 				
 				String testRepoUrl = "";
 				StringBuilder appendedTestUrl = new StringBuilder();
 				if (testRepoDetail != null) {
 					testRepoUrl = testRepoDetail.getRepoUrl();
 					appendedTestUrl.append(testRepoUrl);
 					if (SVN.equalsIgnoreCase(testRepoDetail.getType()) && !testRepoUrl.endsWith(TRUNK) && !testRepoUrl.endsWith(TRUNK + FORWARD_SLASH)) {
 						if (!testRepoUrl.endsWith(FORWARD_SLASH)) {
 							appendedTestUrl.append(FORWARD_SLASH);
 						}
 						appendedTestUrl.append(TRUNK);
 					}
 					if (!appendedTestUrl.toString().endsWith(FORWARD_SLASH)) {
 						appendedTestUrl.append(FORWARD_SLASH);
 					}
 					appendedTestUrl.append(testDirName);
 				}
 				if (repoInfo.isSplitPhresco()) {
 					splitDotPhrescoContents(appInfo, tempPhrescoFile, appendedPhrUrl.toString(), appendedSrcUrl.toString(), appendedTestUrl.toString());
 					addToRepo(phrescoRepoDetail, appInfo, dir, phrescoDirName, tempPhrescoFile, hasSplit);
 				}
 				if (repoInfo.isSplitTest()) {
 					splitTestContents(appInfo, tempTestFile);
 					addToRepo(testRepoDetail, appInfo, dir, testDirName, tempTestFile, hasSplit);
 				}
 				if (hasSplit) {
 					splitSrcContents(appInfo, tempSrcFile, repoInfo, appendedPhrUrl.toString(), appendedSrcUrl.toString(), appendedTestUrl.toString());
 					addToRepo(srcRepoDetail, appInfo, dir, srcDirName, tempSrcFile, hasSplit);
 				}
 				FileUtil.delete(dir);
 				if (repoType.equals(SVN)) {
 					File copySrcDir = new File(Utility.getPhrescoTemp(), CHECKOUT_TEMP + File.separator + appDirName);
 					FileUtils.copyDirectoryToDirectory(copySrcDir, new File(Utility.getProjectHome()));
 				}
 				if (repoType.equals(GIT)) {
 					FileUtils.copyDirectoryToDirectory(tempPhrescoFile, dir);
 					FileUtils.copyDirectoryToDirectory(tempSrcFile, dir);
 					FileUtils.copyDirectoryToDirectory(tempTestFile, dir);
 				}
 			} else {
 				String pomFileName = appInfo.getPomFile();
 				if (StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 					pomFileName = appInfo.getPhrescoPomFile();
 				}
 				PomProcessor pomProcessor = new PomProcessor(new File(dir, pomFileName));
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_SRC_REPO_URL, appendedSrcUrl.toString());
 				pomProcessor.save();
 				addToRepo(srcRepoDetail, appInfo, dir, appDirName, dir, hasSplit);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 			FileUtil.delete(tempPhrescoFile);
 			FileUtil.delete(tempTestFile);
 			FileUtil.delete(tempSrcFile);
 			FileUtil.delete(new File(tempBasePath));
 			FileUtil.delete(new File(Utility.getPhrescoTemp(), CHECKOUT_TEMP));
 		}
 		return true;
 	}
 	
 	private void splitDotPhrescoContents(ApplicationInfo appInfo, File tempPhrescoFile, String phrescoRepoUrl, String srcRepoUrl, String testRepoUrl) throws PhrescoException {
 		try {
 			String appDirName = appInfo.getAppDirName();
 			String appHome = Utility.getProjectHome() + appDirName + File.separator;
 			List<ModuleInfo> modules = appInfo.getModules();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					String moduleAppInfoPath = appHome + module.getCode() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO;
 					ApplicationInfo moduleAppInfo = getApplicationInfo(moduleAppInfoPath);
 					File tempDest = new File(tempPhrescoFile, module.getCode());
 					tempDest.mkdirs();
 					File phrescoSrc = new File(appHome + module.getCode() + File.separator + Constants.DOT_PHRESCO_FOLDER);
 					FileUtils.copyDirectoryToDirectory(phrescoSrc, tempDest);
 					String phrescoPomFile = moduleAppInfo.getPhrescoPomFile();
 					if (StringUtils.isNotEmpty(phrescoPomFile)) {
 						File phrescoPomSrc = new File(appHome + module.getCode() + File.separator + phrescoPomFile);
 						File phrescoPomDest = new File(tempDest, phrescoPomFile);
 						FileUtils.copyFileToDirectory(phrescoPomSrc, tempDest);
 						updatePomProperties(appInfo, moduleAppInfo.getAppDirName(), phrescoPomDest, phrescoRepoUrl, srcRepoUrl, testRepoUrl);
 					}
 				}
 			}
 			tempPhrescoFile.mkdirs();
 			File phrescoSrc = new File(appHome + Constants.DOT_PHRESCO_FOLDER);
 			FileUtils.copyDirectoryToDirectory(phrescoSrc, tempPhrescoFile);
 			if (StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 				File phrescoPomSrc = new File(appHome + appInfo.getPhrescoPomFile());
 				FileUtils.copyFileToDirectory(phrescoPomSrc, tempPhrescoFile);
 				updatePomProperties(appInfo, "", new File(tempPhrescoFile, appInfo.getPhrescoPomFile()), phrescoRepoUrl, srcRepoUrl, testRepoUrl);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void splitTestContents(ApplicationInfo appInfo, File tempTestFile) throws PhrescoException {
 		try {
 			String appDirName = appInfo.getAppDirName();
 			String appHome = Utility.getProjectHome() + appDirName + File.separator;
 			List<ModuleInfo> modules = appInfo.getModules();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					String moduleAppInfoPath = appHome + module.getCode() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO;
 					ApplicationInfo moduleAppInfo = getApplicationInfo(moduleAppInfoPath);
 					String pomFile = moduleAppInfo.getPomFile();
 					if (StringUtils.isNotEmpty(moduleAppInfo.getPhrescoPomFile())) {
 						pomFile = moduleAppInfo.getPhrescoPomFile();
 					}
 					PomProcessor pomProcessor = new PomProcessor(new File(appHome + module.getCode() + File.separator + pomFile));
 					String testDir = pomProcessor.getProperty(Constants.POM_PROP_KEY_TEST_DIR);
 					if (StringUtils.isNotEmpty(testDir)) {
 						File tempDest = new File(tempTestFile, module.getCode());
 						tempDest.mkdirs();
 						File testSrc = new File(appHome + moduleAppInfo.getAppDirName() + File.separator + testDir);
 						FileUtils.copyDirectoryToDirectory(testSrc, tempDest);
 					}
 				}
 			} else {
 				tempTestFile.mkdirs();
 				String pomFile = appInfo.getPomFile();
 				if (StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 					pomFile = appInfo.getPhrescoPomFile();
 				}
 				PomProcessor pomProcessor = new PomProcessor(new File(appHome + pomFile));
 				String testDir = pomProcessor.getProperty(Constants.POM_PROP_KEY_TEST_DIR);
 				File testSrc = new File(appHome, testDir);
 				FileUtils.copyDirectoryToDirectory(testSrc, tempTestFile);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void splitSrcContents(ApplicationInfo appInfo, File tempSrcFile, RepoInfo repoInfo, String phrescoRepoUrl, String srcRepoUrl, String testRepoUrl) throws PhrescoException {
 		try {
 			String appDirName = appInfo.getAppDirName();
 			String appHome = Utility.getProjectHome() + appDirName + File.separator;
 			List<ModuleInfo> modules = appInfo.getModules();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					File srcDest = new File(tempSrcFile, module.getCode());
 					File srcDir = new File(appHome, module.getCode());
 					FileUtils.copyDirectory(srcDir, srcDest, false);
 					String moduleAppInfoPath = appHome + module.getCode() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + PROJECT_INFO;
 					ApplicationInfo moduleAppInfo = getApplicationInfo(moduleAppInfoPath);
 					
 					if (StringUtils.isEmpty(moduleAppInfo.getPhrescoPomFile())) {
 						File pomDest = new File(srcDest, moduleAppInfo.getPomFile());
 						updatePomProperties(appInfo, moduleAppInfo.getAppDirName(), pomDest, phrescoRepoUrl, srcRepoUrl, testRepoUrl);
 					}
 					
 					if (repoInfo.isSplitPhresco()) {
 						FileUtils.deleteDirectory(new File(srcDest, Constants.DOT_PHRESCO_FOLDER));
 						if (StringUtils.isNotEmpty(moduleAppInfo.getPhrescoPomFile())) {
 							File phrescoPomFile = new File(srcDest, moduleAppInfo.getPhrescoPomFile());
 							FileUtil.delete(phrescoPomFile);
 						}
 					}
 					if (repoInfo.isSplitTest()) {
 						String pomFile = moduleAppInfo.getPomFile();
 						if (StringUtils.isNotEmpty(moduleAppInfo.getPhrescoPomFile())) {
 							pomFile = moduleAppInfo.getPhrescoPomFile();
 						}
 						PomProcessor pomProcessor = new PomProcessor(new File(appHome + module.getCode() + File.separator + pomFile));
 						String testDir = pomProcessor.getProperty(Constants.POM_PROP_KEY_TEST_DIR);
 						if (StringUtils.isNotEmpty(testDir)) {
 							FileUtils.deleteDirectory(new File(srcDest, testDir));
 						}
 					}
 				}
 			} else {
 				tempSrcFile.mkdirs();
 				File srcDir = new File(Utility.getProjectHome() + appDirName);
 				FileUtils.copyDirectory(srcDir, tempSrcFile, false);
 				
 				if (StringUtils.isEmpty(appInfo.getPhrescoPomFile())) {
 					File pomDest = new File(tempSrcFile, appInfo.getPomFile());
 					updatePomProperties(appInfo, "", pomDest, phrescoRepoUrl, srcRepoUrl, testRepoUrl);
 				}
 				
 				if (repoInfo.isSplitPhresco()) {
 					FileUtils.deleteDirectory(new File(tempSrcFile, Constants.DOT_PHRESCO_FOLDER));
 					if (StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 						File phrescoPomFile = new File(tempSrcFile, appInfo.getPhrescoPomFile());
 						FileUtil.delete(phrescoPomFile);
 					}
 				}
 				if (repoInfo.isSplitTest()) {
 					String pomFile = appInfo.getPomFile();
 					if (StringUtils.isNotEmpty(appInfo.getPhrescoPomFile())) {
 						pomFile = appInfo.getPhrescoPomFile();
 					}
 					PomProcessor pomProcessor = new PomProcessor(new File(appHome, pomFile));
 					String testDir = pomProcessor.getProperty(Constants.POM_PROP_KEY_TEST_DIR);
 					FileUtils.deleteDirectory(new File(tempSrcFile, testDir));
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void updatePomProperties(ApplicationInfo appInfo, String moduleName, File pomFile, String phrescoRepoUrl, String srcRepoUrl, String testRepoUrl) throws PhrescoException {
 		try {
 			String appDirName = appInfo.getAppDirName();
 			PomProcessor pomProcessor = new PomProcessor(pomFile);
 			StringBuilder sb = new StringBuilder(PREV_DIR);
 			if (StringUtils.isNotEmpty(moduleName)) {
 				sb.append(PREV_DIR);
 			}
 			sb.append(appDirName);
 			if (StringUtils.isNotEmpty(moduleName)) {
 				sb.append(FORWARD_SLASH);
 				sb.append(moduleName);
 				sb.append(FORWARD_SLASH);
 			}
 			
 			String srcRootPrpty = pomProcessor.getProperty(Constants.POM_PROP_KEY_ROOT_SRC_DIR);
 			pomProcessor.setProperty(Constants.POM_PROP_KEY_ROOT_SRC_DIR, sb.toString() + srcRootPrpty);
 			
 			pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR, appDirName);
 			pomProcessor.setProperty(Constants.POM_PROP_KEY_SRC_REPO_URL, srcRepoUrl);
 			if (StringUtils.isNotEmpty(phrescoRepoUrl)) {
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR, appDirName + Constants.SUFFIX_PHRESCO);
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_PHRESCO_REPO_URL, phrescoRepoUrl);
 			}	
 			if (StringUtils.isNotEmpty(testRepoUrl)) {
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR, appDirName + Constants.SUFFIX_TEST);
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_TEST_REPO_URL, testRepoUrl);
 			}
 			pomProcessor.save();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private ApplicationInfo getApplicationInfo(String path) throws PhrescoException {
 		BufferedReader bufferedReader = null;
 		try {
 			if (!new File(path).exists()) {
 				return null;
 			}
 			bufferedReader = new BufferedReader(new FileReader(path));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 			return projectInfo.getAppInfos().get(0);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 			if (bufferedReader != null) {
 				try {
 					bufferedReader.close();
 				} catch (IOException e) {
 					throw new PhrescoException(e);
 				}
 			}
 		}
 	}
 	
 	private void addToRepo(RepoDetail repodetail, ApplicationInfo appInfo, File dir, String dirName, File srcDir, boolean hasSplit) throws PhrescoException {
 		try {
 			String repoType = repodetail.getType();
 			if (SVN.equals(repoType)) {
 				String repoUrl = repodetail.getRepoUrl();
 				StringBuilder appendedUrl = new StringBuilder(repoUrl);
 				if (!repoUrl.endsWith(TRUNK) && !repoUrl.endsWith(TRUNK + FORWARD_SLASH)) {
 					if (!repoUrl.endsWith(FORWARD_SLASH)) {
 						appendedUrl.append(FORWARD_SLASH);
 					}
 					appendedUrl.append(TRUNK + FORWARD_SLASH);
 				}
 				if (!appendedUrl.toString().endsWith(FORWARD_SLASH)) {
 					appendedUrl.append(FORWARD_SLASH);
 				}
 				appendedUrl.append(dirName);
 				repodetail.setRepoUrl(appendedUrl.toString());
 				importDirectoryContentToSubversion(repodetail, srcDir.getPath());
 				// checkout to get .svn folder
 				checkoutImportedApp(repodetail, appInfo, dirName, hasSplit);
 			} else if (GIT.equals(repoType)) {
 				importToGITRepo(repodetail, appInfo, srcDir);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private SVNCommitInfo importDirectoryContentToSubversion(RepoDetail repodetail, final String subVersionedDirectory) throws SVNException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		DefaultSVNOptions defaultSVNOptions = new DefaultSVNOptions();
         defaultSVNOptions.setIgnorePatterns(new String[] {DO_NOT_CHECKIN_DIR,RUN});
         final SVNClientManager cm = SVNClientManager.newInstance(defaultSVNOptions, repodetail.getUserName(), repodetail.getPassword());
         return cm.getCommitClient().doImport(new File(subVersionedDirectory), SVNURL.parseURIEncoded(repodetail.getRepoUrl()), repodetail.getCommitMessage(), null, true, true, SVNDepth.fromRecurse(true));
     }
 	
 	private void checkoutImportedApp(RepoDetail repodetail, ApplicationInfo appInfo, String dirName, boolean hasSplit) throws PhrescoException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.checkoutImportedApp()");
 		}
 		try {
 			DefaultSVNOptions options = new DefaultSVNOptions();
 			SVNClientManager cm = SVNClientManager.newInstance(options, repodetail.getUserName(), repodetail.getPassword());
 			SVNUpdateClient uc = cm.getUpdateClient();
 			SVNURL svnURL = SVNURL.parseURIEncoded(repodetail.getRepoUrl());
 			if(debugEnabled){
 				S_LOGGER.debug("Checking out...");
 			}
 			String subVersionedDirectory = Utility.getProjectHome() + appInfo.getAppDirName();
 			if (hasSplit) {
 				subVersionedDirectory = Utility.getPhrescoTemp() + CHECKOUT_TEMP + File.separator + appInfo.getAppDirName() + File.separator + dirName;
 			}
 			File subVersDir = new File(subVersionedDirectory);
 			if (!subVersDir.exists()) {
 				subVersDir.mkdirs();
 			}
 			uc.doCheckout(SVNURL.parseURIEncoded(repodetail.getRepoUrl()), subVersDir, SVNRevision.UNDEFINED, SVNRevision.parse(HEAD_REVISION), SVNDepth.INFINITY, true);
 			if(debugEnabled){
 				S_LOGGER.debug("updating pom.xml");
 			}
 			// update connection url in pom.xml
 			updateSCMConnection(appInfo, svnURL.toDecodedString());
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void importToGITRepo(RepoDetail repodetail,ApplicationInfo appInfo, File appDir) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importToGITRepo()");
 		}
 		boolean gitExists = false;
 		if(new File(appDir.getPath() + FORWARD_SLASH + DOT + GIT).exists()) {
 			gitExists = true;
 		}
 		try {
 			//For https and ssh
 			additionalAuthentication(repodetail.getPassPhrase());
 			
 			CredentialsProvider cp = new UsernamePasswordCredentialsProvider(repodetail.getUserName(), repodetail.getPassword());
 			FileRepositoryBuilder builder = new FileRepositoryBuilder();
 			Repository repository = builder.setGitDir(appDir).readEnvironment().findGitDir().build();
 			String dirPath = appDir.getPath();
 			File gitignore = new File(dirPath + GITIGNORE_FILE);
 			gitignore.createNewFile();
 			
 			if (gitignore.exists()) {
 				String contents = FileUtils.readFileToString(gitignore);
 				if (!contents.isEmpty() && !contents.contains(DO_NOT_CHECKIN_DIR)) {
 					String source = NEWLINE + DO_NOT_CHECKIN_DIR + NEWLINE;
 					OutputStream out = new FileOutputStream((dirPath + GITIGNORE_FILE), true);
 					byte buf[] = source.getBytes();
 					out.write(buf);
 					out.close();
 				} else if (contents.isEmpty()){
 				String source = NEWLINE + DO_NOT_CHECKIN_DIR + NEWLINE;
 				OutputStream out = new FileOutputStream((dirPath + GITIGNORE_FILE), true);
 				byte buf[] = source.getBytes();
 				out.write(buf);
 				out.close();
 				}
 			}
 		
 			Git git = new Git(repository);
 		
 			InitCommand initCommand = Git.init();
 			initCommand.setDirectory(appDir);
 			git = initCommand.call();
 		
 			AddCommand add = git.add();
 			add.addFilepattern(".");
 			add.call();
 
 			CommitCommand commit = git.commit().setAll(true);
 			commit.setMessage(repodetail.getCommitMessage()).call();
 			StoredConfig config = git.getRepository().getConfig();
 
 			config.setString(REMOTE, ORIGIN, URL, repodetail.getRepoUrl());
 			config.setString(REMOTE, ORIGIN, FETCH, REFS_HEADS_REMOTE_ORIGIN);
 			config.setString(BRANCH, MASTER, REMOTE, ORIGIN);
 			config.setString(BRANCH, MASTER, MERGE, REF_HEAD_MASTER);
 			config.save();
 
 			try {
 				PushCommand pc = git.push();
 				pc.setCredentialsProvider(cp).setForce(true);
 				pc.setPushAll().call();
 			} catch (Exception e){
 				git.getRepository().close();
 				throw e;
 			}
 		
 			if (appInfo != null) {
 				updateSCMConnection(appInfo, repodetail.getRepoUrl());
 			}
 			git.getRepository().close();
 		} catch (Exception e) {
 			Exception s = e;
 			resetLocalCommit(appDir, gitExists, e);
 			throw s;
 		}
 	}
 
 	private void resetLocalCommit(File appDir, boolean gitExists, Exception e) throws PhrescoException {
 		try {
 			if(gitExists == true && e.getLocalizedMessage().contains("not authorized")) {
 				FileRepositoryBuilder builder = new FileRepositoryBuilder();
 				Repository repository = builder.setGitDir(appDir).readEnvironment().findGitDir().build();
 				Git git = new Git(repository);
 
 				InitCommand initCommand = Git.init();
 				initCommand.setDirectory(appDir);
 				git = initCommand.call();
 			
 				ResetCommand reset = git.reset();
 				ResetType mode = ResetType.SOFT;
 				reset.setRef("HEAD~1").setMode(mode);
 				reset.call();
 						
 				git.getRepository().close();
 			}
 		} catch (Exception pe) {
 			new PhrescoException(pe);
 		}
 	}
 
 	public boolean commitToRepo(RepoDetail repodetail, File dir) throws Exception {
 		if(debugEnabled) {
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitToRepo()");
 		}
 		try {
 			if (SVN.equals(repodetail.getType())) {
 				commitDirectoryContentToSubversion(repodetail, dir.getPath());
 			} else if (GIT.equals(repodetail.getType())) {
 				importToGITRepo(repodetail, null, dir);
 			} else if (BITKEEPER.equals(repodetail.getType())) {
 			    return commitToBitKeeperRepo(repodetail.getRepoUrl(), dir.getPath(), repodetail.getCommitMessage());
 			}
 		} catch (PhrescoException e) {
 			throw e;
 		}
 		return true;
 	}
 	
 	private SVNCommitInfo commitDirectoryContentToSubversion(RepoDetail repodetail, String subVersionedDirectory) throws SVNException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), repodetail.getUserName(), repodetail.getPassword());
 		SVNWCClient wcClient = cm.getWCClient();
 		File subVerDir = new File(subVersionedDirectory);
 		// This one recursively adds an existing local item under version control (schedules for addition)
 		wcClient.doAdd(subVerDir, true, false, false, SVNDepth.INFINITY, false, false);
 		return cm.getCommitClient().doCommit(new File[]{subVerDir}, false, repodetail.getCommitMessage(), null, null, false, true, SVNDepth.INFINITY);
     }
 	
 	private boolean commitToBitKeeperRepo(String repoUrl, String appDir, String commitMsg) throws PhrescoException {
 	    BufferedReader reader = null;
         File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
         boolean isCommitted = false;
 	    try {
 	        List<String> commands = new ArrayList<String>();
 	        commands.add(BK_PARENT + SPACE + repoUrl);
 	        commands.add(BK_PULL);
 	        commands.add(BK_CI + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_ADD_FILES + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_COMMIT + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_PUSH);
 	        for (String command : commands) {
 	            Utility.executeStreamconsumer(appDir, command, new FileOutputStream(file));
             }
 	        reader = new BufferedReader(new FileReader(file));
             String strLine;
             while ((strLine = reader.readLine()) != null) {
                 if (strLine.contains("push") && strLine.contains("OK")) {
                     isCommitted = true;
                 } else if (strLine.contains("Nothing to push")) {
                     throw new PhrescoException("Nothing to push");
                 } else if (strLine.contains("Cannot resolve host")) {
                     throw new PhrescoException("Failed to commit");
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     throw new PhrescoException(e);
                 }
             }
             if (file.exists()) {
                 file.delete();
             }
         }
         
         return isCommitted;
 	}
 	
 	public SVNCommitInfo deleteDirectoryInSubversion(RepoDetail repodetail, String subVersionedDirectory) throws SVNException, IOException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		
 		File subVerDir = new File(subVersionedDirectory);		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), repodetail.getUserName(), repodetail.getPassword());
 		
 		SVNWCClient wcClient = cm.getWCClient();
 		//wcClient.doDelete(subVerDir, true, false);
 		FileUtils.listFiles(subVerDir, TrueFileFilter.TRUE, FileFilterUtils.makeSVNAware(null));
 		for (File child : subVerDir.listFiles()) {
 			if (!(DOT+SVN).equals(child.getName())) {
 				wcClient.doDelete(child, true, true, false);
 			}
 		}
 		
 		return cm.getCommitClient().doCommit(new File[]{subVerDir}, false, repodetail.getCommitMessage(), null, null, false, true, SVNDepth.INFINITY);
     }
 	
 	public List<RepoFileInfo> getCommitableFiles(File path, String revision) throws SVNException {
 		
 	    SVNClientManager svnClientManager = SVNClientManager.newInstance();
 	    final List<RepoFileInfo> filesList = new ArrayList<RepoFileInfo>();
 	    svnClientManager.getStatusClient().doStatus(path, SVNRevision.parse(revision), SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
 	        public void handleStatus(SVNStatus status) throws SVNException {
 	            SVNStatusType statusType = status.getContentsStatus();
 	            if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL
 	                    && statusType != SVNStatusType.STATUS_IGNORED) {
 	            	RepoFileInfo repoFileInfo = new RepoFileInfo();
 	            	String filePath = status.getFile().getPath();
 	            	 String FileStatus = Character.toString(statusType.getCode());
 	            	 repoFileInfo.setContentsStatus(statusType);
 	            	 repoFileInfo.setStatus(FileStatus);
 	            	 repoFileInfo.setCommitFilePath(filePath);
 	            	 filesList.add(repoFileInfo);
 	            }
 	        }
 	    }, null);
 	    
 	    return filesList;
 	}
 	
 	public List<RepoFileInfo> getGITCommitableFiles(File path) throws IOException, GitAPIException
 	{
 		FileRepositoryBuilder builder = new FileRepositoryBuilder();
 		Repository repository = builder.setGitDir(path).readEnvironment().findGitDir().build(); 
 		Git git = new Git(repository);
 		List<RepoFileInfo> fileslist = new ArrayList<RepoFileInfo>();
 		InitCommand initCommand = Git.init();
 		initCommand.setDirectory(path);
 		git = initCommand.call();
 		Status status = git.status().call();
 		
 		Set<String> added = status.getAdded();
 		Set<String> changed = status.getChanged();
 		Set<String> conflicting = status.getConflicting();
 		Set<String> missing= status.getMissing();
 		Set<String> modified = status.getModified();
 		Set<String> removed = status.getRemoved();
 		Set<String> untracked = status.getUntracked();
 		
 		if (!added.isEmpty()) {
 			for (String add : added) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + add;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("A");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!changed.isEmpty()) {
 			for (String change : changed) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + change;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("M");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!conflicting.isEmpty()) {
 			for (String conflict : conflicting) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + conflict;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("C");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!missing.isEmpty()) {
 			for (String miss : missing) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + miss;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("!");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!modified.isEmpty()) {
 			for (String modify : modified) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + modify;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("M");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!removed.isEmpty()) {
 			for (String remove : removed) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + remove;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("D");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!untracked.isEmpty()) {
 			for (String untrack : untracked) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + untrack;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("?");
 				fileslist.add(repoFileInfo);
 			}
 		}
 		git.getRepository().close();
 		return fileslist;
 	}
 
 	public List<String> getSvnLogMessages(String Url, String userName, String Password) throws PhrescoException {
 		setupLibrary();
 		long startRevision = 0;
 		long endRevision = -1; //HEAD (the latest) revision
 		SVNRepository repository = null;
 		List<String> logMessages = new ArrayList<String>();
 		try {
 			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(Url));
 			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, Password);
 			repository.setAuthenticationManager(authManager);
 			Collection logEntries = null;
 
 			logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );
 			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
 				SVNLogEntry logEntry = (SVNLogEntry) entries.next( );
 				logMessages.add(logEntry.getMessage());
 			}
 		} catch (SVNException e) {
 			throw new PhrescoException(e);
 		}
 
 		return logMessages;
 	}
 	
 	public SVNCommitInfo commitSpecifiedFiles(List<File> listModifiedFiles, String username, String password, String commitMessage) throws Exception {
 		setupLibrary();
 		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), username, password);
 		SVNWCClient wcClient = cm.getWCClient();
 		File[] comittableFiles = listModifiedFiles.toArray(new File[listModifiedFiles.size()]);
 
 		SVNCommitClient cc = cm.getCommitClient(); 
 		cc.setCommitParameters(new ISVNCommitParameters() { 
 
 			// delete even those files 
 			// that are not scheduled for deletion. 
 			public Action onMissingFile(File file) { 
 				return DELETE; 
 			} 
 			public Action onMissingDirectory(File file) { 
 				return DELETE; 
 			} 
 
 			// delete files from disk after committing deletion. 
 			public boolean onDirectoryDeletion(File directory) { 
 				return true; 
 			} 
 			public boolean onFileDeletion(File file) { 
 				return true; 
 			} 
 		}); 
 		
 		//to List Unversioned Files 
 		List<File> unversionedFiles = new ArrayList<File>();
 		for (File file : comittableFiles) {
 			List<RepoFileInfo> status = getCommitableFiles(new File(file.getPath()), HEAD_REVISION);
 			if (CollectionUtils.isNotEmpty(status)) {
 				RepoFileInfo svnStatus = status.get(0);
 				SVNStatusType contentsStatus = svnStatus.getContentsStatus();
 				if(UNVERSIONED.equalsIgnoreCase(contentsStatus.toString())) {
 					unversionedFiles.add(file);
 				} 
 			}
 		}
 		
 		//Add only Unversioned Files
 		if (CollectionUtils.isNotEmpty(unversionedFiles)) {
 			File[] newlyAddedFiles = unversionedFiles.toArray(new File[unversionedFiles.size()]);
 			wcClient.doAdd(newlyAddedFiles, true, false, false, SVNDepth.INFINITY, false, false, false);
 		}
 
 		SVNCommitInfo commitInfo = cc.doCommit(comittableFiles, false, commitMessage, null, null, false, true, SVNDepth.INFINITY);
 
 		return commitInfo;
 	}
 	
 	public String svnCheckout(String path, String userName, String password, String repoURL, String revision) {
 		DAVRepositoryFactory.setup();
 		SVNClientManager clientManager = SVNClientManager.newInstance();
 		ISVNAuthenticationManager authManager = new BasicAuthenticationManager(userName, password);
 		clientManager.setAuthenticationManager(authManager);
 		SVNUpdateClient updateClient = clientManager.getUpdateClient();
 		try
 		{
 			File file = new File(path);
 			SVNURL url = SVNURL.parseURIEncoded(repoURL);
 			updateClient.doCheckout(url, file, SVNRevision.UNDEFINED, SVNRevision.parse(revision), true);
 		}
 		catch (SVNException e) {
 			return e.getLocalizedMessage();
 		}
 		return SUCCESSFUL;
 	}
 	
 	public ApplicationInfo importFromPerforce(RepoDetail repodetail, File tempFile) throws Exception {
 		perforceSync(repodetail, tempFile.getAbsolutePath(), tempFile.getName(),"import");
 		String path = tempFile.getAbsolutePath();
 		String[] pathArr = repodetail.getStream().split("/");
 		String projName = pathArr[pathArr.length-1];
 		File actualFile = new  File(path+"/"+projName);
 		ProjectInfo projectInfo = getGitAppInfo(actualFile);
 		if(projectInfo!= null){
 			ApplicationInfo appInfo = returnAppInfo(projectInfo);
 			updateSCMConnection(appInfo, repodetail.getRepoUrl()+repodetail.getStream());
 		} 
 		return returnAppInfo(projectInfo);
 	}
 	
 	public void perforceSync(RepoDetail repodetail,String baseDir , String projectName,String flag) throws ConnectionException, RequestException  {
 		String url=repodetail.getRepoUrl();
 		String userName=repodetail.getUserName();
 		String password=repodetail.getPassword();
 		String stream=repodetail.getStream();
 		try {		
 			IOptionsServer server = ServerFactory.getOptionsServer("p4java://"+url, null, null);
 			server.connect();
 			server.setUserName(userName);
 			if(password!=""){
 				server.login(password);
 			}			
 			IClient client = new Client();
 			client.setName(projectName);
 			if(flag.equals("update")){
 				String[] rootArr=baseDir.split(projectName);
 				String root=rootArr[0].substring(0,rootArr[0].length()-1);
 				client.setRoot(root);
 			} else {
 				client.setRoot(baseDir);
 			}
 			client.setServer(server); 
 			server.setCurrentClient(client);
 			ClientViewMapping tempMappingEntry = new ClientViewMapping();
 			tempMappingEntry.setLeft(stream+"/...");
 			tempMappingEntry.setRight("//"+projectName+"/...");
 			ClientView clientView = new ClientView();
 			clientView.addEntry(tempMappingEntry);
 			try {
 			String[] arr=repodetail.getStream().split("//");
 			String[] arr1=arr[1].split("/");
 			client.setStream("//"+arr1[0]+"/"+arr1[1]);
 			client.setClientView(clientView);
 			client.setOptions(new ClientOptions("noallwrite clobber nocompress unlocked nomodtime normdir"));
 			}catch (ArrayIndexOutOfBoundsException e) {
 				throw new RequestException();
 			}
 			if (client != null) {	
 				List<IFileSpec> syncList = client.sync(FileSpecBuilder.makeFileSpecList(stream+"/..."),new SyncOptions());
 				for (IFileSpec fileSpec : syncList) {
 					if (fileSpec != null) {
 						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
 
 						} else {
 							System.err.println(fileSpec.getStatusMessage());
 						}
 					}
 				}}
 			IOFileFilter filter=new IOFileFilter() {
 				@Override
 				public boolean accept(File arg0, String arg1) {
 					return true;
 				}
 				@Override
 				public boolean accept(File arg0) {
 					return true;
 				}
 			};
 			Iterator<File> iterator= FileUtils.iterateFiles(new File(baseDir), filter, filter);
 			while(iterator.hasNext()){
 				File file = iterator.next();
 				file.setWritable(true);
 			}
 
 		} catch (RequestException rexc) {
 			System.err.println(rexc.getDisplayString());
 			rexc.printStackTrace();
 			throw new RequestException();
 		} catch (P4JavaException jexc) {
 			System.err.println(jexc.getLocalizedMessage());
 			jexc.printStackTrace();
 			throw new ConnectionException();
 		} catch (Exception exc) {
 			System.err.println(exc.getLocalizedMessage());
 			exc.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void importTest(ApplicationInfo applicationInfo, RepoInfo repoInfo) throws Exception {
 		StringBuilder builder = new StringBuilder(Utility.getProjectHome()).append(File.separator);
 		builder.append(applicationInfo.getAppDirName());
 		if(repoInfo.isSplitTest()) {
 			File pomFile = getPomFromRepository(applicationInfo, repoInfo);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String testDir = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			builder.append(File.separator).append(testDir);
 		}
 		RepoDetail testRepoDetail = repoInfo.getTestRepoDetail();
 		String type = testRepoDetail.getType();
 		if(type.equals(SVN)) {
 			svnCheckout(builder.toString(), testRepoDetail.getUserName(), testRepoDetail.getPassword(), 
 					testRepoDetail.getRepoUrl(), testRepoDetail.getRevision());
 		}
 		if(type.equals(GIT)) {
 			String uuid = UUID.randomUUID().toString();
 			File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 			importFromGit(testRepoDetail, gitImportTemp);
 			importToWorkspace(gitImportTemp, new File(builder.toString()));
 		}
 		if(type.equals(BITKEEPER)) {
 			String uuid = UUID.randomUUID().toString();
 			File importTemp = new File(Utility.getPhrescoTemp(), uuid);
 			boolean imported = importFromBitKeeper(testRepoDetail.getRepoUrl(), importTemp);
 			if(imported) {
 				importToWorkspace(importTemp, new File(builder.toString()));
 			}
 		}
 		if(type.equals(PERFORCE)) {
 			String uuid = UUID.randomUUID().toString();
 			File tempFile = new File(Utility.getPhrescoTemp(), uuid);
 			FileUtils.forceMkdir(tempFile);
 			importFromPerforce(testRepoDetail, tempFile);
 			importToWorkspace(tempFile, new File(builder.toString()));
 		}
 		if (type.equals(TFS)) {
 			String uuid = UUID.randomUUID().toString();
 			File tempFile = new File(Utility.getPhrescoTemp(), uuid);
 			FileUtils.forceMkdir(tempFile);
 			importFromTfs(testRepoDetail, tempFile);
 			importToWorkspace(tempFile, new File(builder.toString()));
 		}
 	}
 
 	@Override
 	public void importPhresco(ApplicationInfo applicationInfo, RepoInfo repoInfo)
 			throws Exception {
 		StringBuilder builder = new StringBuilder(Utility.getProjectHome()).append(File.separator);
 		builder.append(applicationInfo.getAppDirName());
 		if(repoInfo.isSplitPhresco()) {
 			builder.append(File.separator).append(applicationInfo.getAppDirName() + Constants.SUFFIX_PHRESCO);
 		}
 		RepoDetail phrescoRepoDetail = repoInfo.getPhrescoRepoDetail();
 		String type = phrescoRepoDetail.getType();
 		if(type.equals(SVN)) {
 			svnCheckout(builder.toString(), phrescoRepoDetail.getUserName(), phrescoRepoDetail.getPassword(), 
 					phrescoRepoDetail.getRepoUrl(), phrescoRepoDetail.getRevision());
 		}
 		if(type.equals(GIT)) {
 			String uuid = UUID.randomUUID().toString();
 			File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 			importFromGit(phrescoRepoDetail, gitImportTemp);
 			importToWorkspace(gitImportTemp, new File(builder.toString()));
 		}
 		if(type.equals(BITKEEPER)) {
 			String uuid = UUID.randomUUID().toString();
 			File importTemp = new File(Utility.getPhrescoTemp(), uuid);
 			boolean imported = importFromBitKeeper(phrescoRepoDetail.getRepoUrl(), importTemp);
 			if(imported) {
 				importToWorkspace(importTemp, new File(builder.toString()));
 			}
 		}
 		if(type.equals(PERFORCE)) {
 			String uuid = UUID.randomUUID().toString();
 			File tempFile = new File(Utility.getPhrescoTemp(), uuid);
 			importFromPerforce(phrescoRepoDetail, tempFile);
 			importToWorkspace(tempFile, new File(builder.toString()));
 		}
 		if(type.equals(TFS)) {
 			String uuid = UUID.randomUUID().toString();
 			File tempFile = new File(Utility.getPhrescoTemp(), uuid);
 			importFromTfs(phrescoRepoDetail, tempFile);
 			importToWorkspace(tempFile, new File(builder.toString()));
 		}
 	}
 }
