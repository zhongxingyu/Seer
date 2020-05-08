 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceClientConstant;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 
 public class ProjectManagerImpl implements ProjectManager, FrameworkConstants, Constants, ServiceClientConstant {
 	
 	private static final Logger S_LOGGER= Logger.getLogger(ProjectManagerImpl.class);
 	private static boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private Map<String, ProjectInfo> projectInfosMap = null;
 	
 	public List<ProjectInfo> discover(String customerId) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.discover(String CustomerId)");
 		}
 
 		File projectsHome = new File(Utility.getProjectHome());
 		if (isDebugEnabled) {
 			S_LOGGER.debug("discover( )  projectHome = "+projectsHome);
 		}
 		if (!projectsHome.exists()) {
 			return null;
 		}
 		projectInfosMap = new HashMap<String, ProjectInfo>();
 		List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 	    File[] appDirs = projectsHome.listFiles();
 	    for (File appDir : appDirs) {
 	        if (appDir.isDirectory()) { // Only check the folders not files
 	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	            	continue;
 //	                throw new PhrescoException(".phresco folder not found in project " + appDir.getName());
 	            }
 	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 	            if (ArrayUtils.isEmpty(dotProjectFiles)) {
 	                throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
 	            }
 	            fillProjects(dotProjectFiles[0], projectInfos, customerId);
 	        }
 	    }
 	    
 	    Iterator<Entry<String, ProjectInfo>> iterator = projectInfosMap.entrySet().iterator();
         while (iterator.hasNext()) {
             projectInfos.add(iterator.next().getValue());
         }
 
         return projectInfos;
 	}
 
 	public ProjectInfo getProject(String projectId, String customerId) throws PhrescoException {
 		List<ProjectInfo> discover = discover(customerId);
 		for (ProjectInfo projectInfo : discover) {
 			if (projectInfo.getId().equals(projectId)) {
 				return projectInfo;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public ProjectInfo getProject(String projectId, String customerId, String appId) throws PhrescoException {
 		File[] appDirs = new File(Utility.getProjectHome()).listFiles();
 	    for (File appDir : appDirs) {
 	        if (appDir.isDirectory()) { // Only check the folders not files
 	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	                throw new PhrescoException(".phresco folder not found in project " + appDir.getName());
 	            }
 	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 	            ProjectInfo projectInfo = getProjectInfo(dotProjectFiles[0], projectId, customerId, appId);
 	            if (projectInfo != null) {
 	            	return projectInfo;
 	            }
 	        }
 	    }
 		
 		return null;
 	}
 
 	public ProjectInfo create(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.create(ProjectInfo projectInfo)");
 		}
 		ClientResponse response = serviceManager.createProject(projectInfo);
 
 		if (isDebugEnabled) {
 			S_LOGGER.debug("createProject response code " + response.getStatus());
 		}
 		if (response.getStatus() == 200) {
 			try {
 				extractArchive(response, projectInfo);
 				ProjectUtils projectUtils = new ProjectUtils();
 				String customerId = projectInfo.getCustomerIds().get(0);
 				Customer customer = serviceManager.getCustomer(customerId);
 				RepoInfo repoInfo = customer.getRepoInfo();
 				List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 				for (ApplicationInfo appInfo : appInfos) {
 					String pluginInfoFile = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + DOT_PHRESCO_FOLDER +File.separator +  APPLICATION_HANDLER_INFO_FILE;
 					File path = new File(Utility.getProjectHome() + appInfo.getAppDirName());
 					projectUtils.updateTestPom(path);
 					MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 					ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 					if (applicationHandler != null) {
 						List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
 						//Dynamic Class Loading
 						PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
 						ApplicationProcessor applicationProcessor = dynamicLoader.getApplicationProcessor(applicationHandler.getClazz());
 						applicationProcessor.postCreate(appInfo);
 					}
 				}
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e); 
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		} else if(response.getStatus() == 401){
 			throw new PhrescoException("Session expired");
 		} else {
 			throw new PhrescoException("Project creation failed");
 		}
 
 		//TODO This needs to be moved to service
 		try {
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			Environment defaultEnv = getEnvFromService(serviceManager);
 			for (ApplicationInfo applicationInfo : appInfos) {
 				createConfigurationXml(applicationInfo.getAppDirName(), serviceManager, defaultEnv);	
 			}
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into the catch block of Configuration creation failed Exception" + e.getLocalizedMessage());
 			throw new PhrescoException("Configuration creation failed"+e);
 		}
 		return projectInfo;
 	}
 
 	public ProjectInfo update(ProjectInfo projectInfo, ServiceManager serviceManager, String oldAppDirName) throws PhrescoException {
 		if (projectInfo.getNoOfApps() == 0 && CollectionUtils.isEmpty(projectInfo.getAppInfos())) {
 			ProjectInfo availableProjectInfo = getProject(projectInfo.getId(), projectInfo.getCustomerIds().get(0));
 			List<ApplicationInfo> appInfos = availableProjectInfo.getAppInfos();
 			for (ApplicationInfo applicationInfo : appInfos) {
 				projectInfo.setAppInfos(Collections.singletonList(applicationInfo));
 				StringBuilder sb = new StringBuilder(Utility.getProjectHome())
 				.append(applicationInfo.getAppDirName())
 				.append(File.separator)
 				.append(DOT_PHRESCO_FOLDER)
 				.append(File.separator)
 				.append(PROJECT_INFO_FILE);
 				ProjectUtils.updateProjectInfo(projectInfo, new File(sb.toString()));// To update the project.info file
 			}
 		} else {
 			ClientResponse response = serviceManager.updateProject(projectInfo);
 			if (response.getStatus() == 200) {
 				BufferedReader breader = null;
 				try {
 					//application path with old app dir
 					StringBuilder oldAppDirSb = new StringBuilder(Utility.getProjectHome());
 					oldAppDirSb.append(oldAppDirName);
 					File oldDir = new File(oldAppDirSb.toString());
 					
 					//application path with new app dir
 					StringBuilder newAppDirSb = new StringBuilder(Utility.getProjectHome());
 					newAppDirSb.append(projectInfo.getAppInfos().get(0).getAppDirName());
 					File projectInfoFile = new File(newAppDirSb.toString());
 					
 					//rename to application app dir
 					oldDir.renameTo(projectInfoFile);
 					
 					extractArchive(response, projectInfo);
 					updateProjectPom(projectInfo, newAppDirSb.toString());
 					StringBuilder dotPhrescoPathSb = new StringBuilder(projectInfoFile.getPath());
 					dotPhrescoPathSb.append(File.separator);
 					dotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 					dotPhrescoPathSb.append(File.separator);
 	
 					String customerId = projectInfo.getCustomerIds().get(0);
 					Customer customer = serviceManager.getCustomer(customerId);
 					RepoInfo repoInfo = customer.getRepoInfo();
 					ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 					
 					String pluginInfoFile = dotPhrescoPathSb.toString() + APPLICATION_HANDLER_INFO_FILE;
 					MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 					ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 					
 					createSqlFolder(appInfo, projectInfoFile, serviceManager);
 						
 					if (applicationHandler != null) {
 						String selectedFeatures = applicationHandler.getSelectedFeatures();
 						Gson gson = new Gson();
 						Type jsonType = new TypeToken<Collection<ArtifactGroup>>(){}.getType();
 						List<ArtifactGroup> artifactGroups = gson.fromJson(selectedFeatures, jsonType);
 						
 						List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
 	
 						// Dynamic Class Loading
 						PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
 						ApplicationProcessor applicationProcessor = dynamicLoader
 								.getApplicationProcessor(applicationHandler.getClazz());
 	
						applicationProcessor.postUpdate(appInfo, artifactGroups);
 	
 						File projectInfoPath = new File(dotPhrescoPathSb.toString() + PROJECT_INFO_FILE);
 						ProjectUtils.updateProjectInfo(projectInfo, projectInfoPath);// To update the project.info file
 	
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 					throw new PhrescoException(e);
 				} catch (IOException e) {
 					e.printStackTrace();
 					throw new PhrescoException(e);
 				} 
 			} else if (response.getStatus() == 401) {
 				throw new PhrescoException("Session expired");
 			} else {
 				throw new PhrescoException("Project updation failed");
 			}
 		}
 		return null;
 	}
 	
 	
 	private void updateProjectPom(ProjectInfo projectInfo, String newAppDirSb) throws PhrescoException {
 		File pomFile = new File(newAppDirSb, "pom.xml");
 		PomProcessor pomProcessor;
 		try {
 			pomProcessor = new PomProcessor(pomFile);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			pomProcessor.setArtifactId(applicationInfo.getCode());
 			pomProcessor.setName(applicationInfo.getName());
 			pomProcessor.setVersion(projectInfo.getVersion());
 			pomProcessor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private List<ArtifactGroup> setArtifactGroup(ApplicationHandler applicationHandler) {
 			List<ArtifactGroup> plugins = new ArrayList<ArtifactGroup>();
 			ArtifactGroup artifactGroup = new ArtifactGroup();
 			artifactGroup.setGroupId(applicationHandler.getGroupId());
 			artifactGroup.setArtifactId(applicationHandler.getArtifactId());
 			//artifactGroup.setType(Type.FEATURE); to set version
 			List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 			ArtifactInfo artifactInfo = new ArtifactInfo();
 			artifactInfo.setVersion(applicationHandler.getVersion());
 			artifactInfos.add(artifactInfo);
 			artifactGroup.setVersions(artifactInfos);
 			plugins.add(artifactGroup);
 			return plugins;
 	}
 
 	public boolean delete(List<ApplicationInfo> appInfos) throws PhrescoException {
 		boolean deletionSuccess = false;
 		String projectsPath = Utility.getProjectHome();
 		for (ApplicationInfo applicationInfo : appInfos) {
 			String appDirName = applicationInfo.getAppDirName();
 			File application = new File(projectsPath + appDirName);
 			deletionSuccess = FileUtil.delete(application);
 		}
 		
 		return deletionSuccess;
 	}
 	
     private void fillProjects(File dotProjectFile, List<ProjectInfo> projectInfos, String customerId) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectManagerImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
         Gson gson = new Gson();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(dotProjectFile));
             ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
             if (projectInfo.getCustomerIds().get(0).equalsIgnoreCase(customerId)) {
                 ProjectInfo projectInfoInMap = projectInfosMap.get(projectInfo.getId());
                 if (projectInfoInMap != null) {
                     projectInfoInMap.getAppInfos().add(projectInfo.getAppInfos().get(0));
                     projectInfosMap.put(projectInfo.getId(), projectInfoInMap);
                 } else {
                     projectInfosMap.put(projectInfo.getId(), projectInfo);
                 }
             }
         } catch (FileNotFoundException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(reader);
         }
     }
     
     private ProjectInfo getProjectInfo(File dotProjectFile, String projectId, String customerId, String appId) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectManagerImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
         Gson gson = new Gson();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(dotProjectFile));
             ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
             if (projectInfo.getCustomerIds().get(0).equalsIgnoreCase(customerId) && projectInfo.getId().equals(projectId)) {
                 List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
                 for (ApplicationInfo applicationInfo : appInfos) {
 					if (applicationInfo.getId().equals(appId)) {
 						return projectInfo;
 					}
 				}
             }
         } catch (FileNotFoundException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(reader);
         }
         
 		return null;
     }
 	
 	private void extractArchive(ClientResponse response, ProjectInfo info) throws IOException, PhrescoException {
 		InputStream inputStream = response.getEntityInputStream();
 		FileOutputStream fileOutputStream = null;
 		String archiveHome = Utility.getArchiveHome();
 		File archiveFile = new File(archiveHome + info.getProjectCode() + ARCHIVE_FORMAT);
 		fileOutputStream = new FileOutputStream(archiveFile);
 		try {
 			byte[] data = new byte[1024];
 			int i = 0;
 			while ((i = inputStream.read(data)) != -1) {
 				fileOutputStream.write(data, 0, i);
 			}
 			fileOutputStream.flush();
 			ArchiveUtil.extractArchive(archiveFile.getPath(), Utility.getProjectHome(), ArchiveType.ZIP);
 		} finally {
 			Utility.closeStream(inputStream);
 			Utility.closeStream(fileOutputStream);
 		}
 	}
 	
 	private void createSqlFolder(ApplicationInfo appInfo, File path, ServiceManager serviceManager)
 			throws PhrescoException {
 		String dbName = "";
 		try {
 			File pomPath = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + POM_FILE);
 			PomProcessor pompro = new PomProcessor(pomPath);
 			String sqlFolderPath = pompro.getProperty(POM_PROP_KEY_SQL_FILE_DIR);
 			File mysqlFolder = new File(path, sqlFolderPath + Constants.DB_MYSQL);
 			File mysqlVersionFolder = getMysqlVersionFolder(mysqlFolder);
 			File pluginInfoFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator
 					+ DOT_PHRESCO_FOLDER + File.separator + APPLICATION_HANDLER_INFO_FILE);
 			MojoProcessor mojoProcessor = new MojoProcessor(pluginInfoFile);
 			ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 			String selectedDatabases = applicationHandler.getSelectedDatabase();
 			if (StringUtils.isNotEmpty(selectedDatabases) && StringUtils.isNotEmpty(sqlFolderPath)) {
 				Gson gson = new Gson();
 				java.lang.reflect.Type jsonType = new TypeToken<Collection<DownloadInfo>>() {
 				}.getType();
 				List<DownloadInfo> dbInfos = gson.fromJson(selectedDatabases, jsonType);
 				List<ArtifactGroupInfo> newSelectedDatabases = appInfo.getSelectedDatabases();
 				for (ArtifactGroupInfo artifactGroupInfo : newSelectedDatabases) {
 					List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
 					for (String artifactId : artifactInfoIds) {
 					ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifactId);
 					String selectedVersion = artifactInfo.getVersion();
 				for (DownloadInfo dbInfo : dbInfos) {
 					dbName = dbInfo.getName().toLowerCase();
 					ArtifactGroup artifactGroup = dbInfo.getArtifactGroup();
 					mySqlFolderCreation(path, dbName, sqlFolderPath, mysqlVersionFolder,selectedVersion, artifactGroup);
 				}
 				}
 				}
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void mySqlFolderCreation(File path, String dbName, String sqlFolderPath, File mysqlVersionFolder, String selectedVersion,
 			ArtifactGroup artifactGroup) throws PhrescoException {
 		try {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo version : versions) {
 			if (selectedVersion.equals(version.getVersion())) {
 				String dbversion = version.getVersion();
 				String sqlPath = dbName + File.separator + dbversion.trim();
 				File sqlFolder = new File(path, sqlFolderPath + sqlPath);
 				sqlFolder.mkdirs();
 				if (dbName.equals(Constants.DB_MYSQL) && mysqlVersionFolder != null
 						&& !(mysqlVersionFolder.getPath().equals(sqlFolder.getPath()))) {
 					FileUtils.copyDirectory(mysqlVersionFolder, sqlFolder);
 				} else {
 					File sqlFile = new File(sqlFolder, Constants.SITE_SQL);
 					if (!sqlFile.exists()) {
 						sqlFile.createNewFile();
 					}
 				}
 			}
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	 
 	private File getMysqlVersionFolder(File mysqlFolder) {
 		File[] mysqlFolderFiles = mysqlFolder.listFiles();
 		if (mysqlFolderFiles != null && mysqlFolderFiles.length > 0) {
 			return mysqlFolderFiles[0];
 		}
 		return null;
 	}
 	
 	private File createConfigurationXml(String appDirName, ServiceManager serviceManager, Environment defaultEnv) throws PhrescoException {
 		File configFile = new File(getConfigurationPath(appDirName).toString());
 		if (!configFile.exists()) {
 			createEnvironments(configFile, defaultEnv, true);
 		}
 		return configFile;
 	}
 	
 	private StringBuilder getConfigurationPath(String projectCode) {
 		 S_LOGGER.debug("Entering Method ProjectManager.getConfigurationPath(Project project)");
 		 S_LOGGER.debug("removeSettingsInfos() ProjectCode = " + projectCode);
 
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(projectCode);
 		 builder.append(File.separator);
 		 builder.append(FOLDER_DOT_PHRESCO);
 		 builder.append(File.separator);
 		 builder.append(CONFIGURATION_INFO_FILE_NAME);
 
 		 return builder;
 	 }
 	
 	private Environment getEnvFromService(ServiceManager serviceManager) throws PhrescoException {
 		 try {
 			 return serviceManager.getDefaultEnvFromServer();
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error(ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 	
 	private void createEnvironments(File configPath, Environment defaultEnv, boolean isNewFile) throws PhrescoException {
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configPath);
 			 ConfigurationWriter writer = new ConfigurationWriter(reader, isNewFile);
 			 writer.createEnvironment(Collections.singletonList(defaultEnv));
 			 writer.saveXml(configPath);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 	
 	private class PhrescoFileNameFilter implements FilenameFilter {
 		 private String filter_;
 		 public PhrescoFileNameFilter(String filter) {
 			 filter_ = filter;
 		 }
 		 public boolean accept(File dir, String name) {
 			 return name.endsWith(filter_);
 		 }
 	 }
 }
