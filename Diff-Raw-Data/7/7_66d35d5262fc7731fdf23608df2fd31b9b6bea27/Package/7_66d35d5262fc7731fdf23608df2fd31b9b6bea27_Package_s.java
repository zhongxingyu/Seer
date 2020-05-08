 /**
  * windows-phone-phresco-plugin
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
 package com.photon.phresco.plugins.windows;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Type;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.WP8PackageInfo;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 
 
 public class Package implements PluginConstants {
 
 	private File baseDir;
 	private String environmentName;
 	private String buildName;
 	private String buildNumber;
 	private String type;
 	private String config;
 	private String platform;
 	private int buildNo;
 
 	private File buildDir;
 	private File buildInfoFile;
 	private File tempDir;
 	private int nextBuildNo;
 	private String zipName;
 	private String zipFilePath;
 	private Date currentDate;
 	private String sourceDirectory = "\\source";
 	private File[] solutionFile;
 	private File[] projFile;
 	private WP8PackageInfo packageInfo;
 	private File rootDir;
 	private Log log;
 	private PluginPackageUtil util;
 	
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         platform = configs.get(PLATFORM);
         config = configs.get(CONFIG);
         type = configs.get(WINDOWS_PLATFORM_TYPE);
         util = new PluginPackageUtil();
         PluginUtils.checkForConfigurations(baseDir, environmentName);
 		try {
 			init();
 			WinBuildInfo info = new WinBuildInfo();
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				try {
 					String[] platforms = platform.split(WP_COMMA);
 					for (String platform : platforms) {
 						generateWP8Package(platform);
 						boolean buildStatus = build(platform);
 						writeBuildInfo(buildStatus, (platform.equalsIgnoreCase("any cpu")?"AnyCPU":platform), info);
 					}
 				} catch (PhrescoException e) {				
 				}
 			} else {
 				generateWP7Package();
 				boolean buildStatus = build(platform);
 				writeBuildInfo(buildStatus, platform, info);
 			}
 			
 			 deleteOutputDir(new File(baseDir.getPath() + sourceDirectory));
 			cleanUp();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 
 	private void init() throws MojoExecutionException {
 		try {
 			File projectInfoPath = new File (baseDir + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.PROJECT_INFO_FILE);
 			Gson gson = new Gson();
 			Type listType = new TypeToken<ProjectInfo>() {}.getType();
 			BufferedReader reader = new BufferedReader(new FileReader(projectInfoPath));
 			ProjectInfo info  = (ProjectInfo) gson.fromJson(reader, listType);
 			ApplicationInfo applicationInfo = info.getAppInfos().get(0);
 			String version = applicationInfo.getTechInfo().getVersion();
 			if (version.startsWith("8.x")) {
				String property = System.getProperty("os.arch");
				if (!property.contains("64")) {
 					throw new PhrescoException("Windows8 project require 64 bit version to build");
 				}
 			}
 			
 			if (StringUtils.isEmpty(environmentName) || StringUtils.isEmpty(type) || (!type.equals(WIN_PHONE) && !type.equals(WIN_STORE))) {
 				callUsage();
 			}
 			
 			getSolutionFile();
 			
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				getProjectRoot();
 				getProjectFile();
 				packageInfo = new WP8PackageInfo(rootDir);
 			}
 			
 			buildDir = new File(baseDir.getPath() + BUILD_DIRECTORY);
 			if (!buildDir.exists()) {
 				buildDir.mkdirs();
 			}
 			buildInfoFile = new File(buildDir.getPath() + BUILD_INFO_FILE);
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void callUsage() throws MojoExecutionException {
 		log.error("Invalid usage.");
 		log.info("Usage of Package Goal");
 		log.info("mvn windows-phone:package -DenvironmentName=\"Multivalued evnironment names\"" 
 					+ " -Dtype=\"Windows Phone platform\"");
 		throw new MojoExecutionException("Invalid Usage. Please see the Usage of Package Goal");
 	}
 	
 	private void getSolutionFile() throws MojoExecutionException {
 		try {
 			// Get .sln file from the source folder
 			File sourceDir = new File(baseDir.getPath() + sourceDirectory);
 			solutionFile = sourceDir.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(WP_SLN);
 				}
 			});			
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	
 	private void getProjectFile() throws MojoExecutionException {
 		try {
 			// Get .csproj file from the source folder
 			File projRootDir = new File(rootDir.getPath());
 			projFile = projRootDir.listFiles(new FilenameFilter() { 
 				public boolean accept(File dir, String name) { 
 					return name.endsWith(WP_CSPROJ) || name.endsWith(VB_CSPROJ);
 				}
 			});			
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void getProjectRoot() throws PhrescoException {
 		try {
 			String solutionFilename = solutionFile[0].getName().substring(0, (solutionFile[0].getName().length())-4);
 			rootDir = new File(baseDir.getPath() + sourceDirectory + File.separator + WP_SOURCE + File.separator + solutionFilename);
 			Boolean checkRootDir = checkRootDir(rootDir);
 			if (!checkRootDir) {
 				throw new PhrescoException();
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new PhrescoException("Startup project must be placed in a folder named same as solution (.sln) file name.");
 		}
 	}
 	
 	public static Boolean checkRootDir(File fileObject) throws PhrescoException {
 		Boolean dirExists =  false;
 		try {
 			if (fileObject.exists()) {
 				dirExists = true;
 			}
 			
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return dirExists;
 	}
 	
 	
 	private void generateWP7Package() throws MojoExecutionException {
 		BufferedReader in = null;
 		try {
 			log.info("Building project ...");
 						
 			// MSBuild MyApp.sln /t:Clean;Rebuild /p:Configuration=Release;Platform="Any CPU"
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(solutionFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_TARGET);
 			sb.append(WP_STR_COLON);
 			sb.append(WIN_CLEAN);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(REBUILD);
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(WP_STR_COLON);
 			sb.append(WP_STR_CONFIGURATION + "=" + config);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(WP_STR_PLATFORM + "=" + WP_STR_DOUBLEQUOTES + platform + WP_STR_DOUBLEQUOTES);
 			
 			log.info("Command = "+ sb.toString());
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(baseDir.getPath() + sourceDirectory);
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			Utility.closeStream(in);
 		}
 	}
 	
 	private void generateWP8Package(String platform) throws MojoExecutionException, PhrescoException {
 		BufferedReader in = null;
 		try {
 			
 			checkPackageVersionNo();
 			
 			log.info("Building project ...");
 			
 			// MSBuild MyApp.sln /t:Clean;Rebuild /p:Configuration=Release;Platform="Any CPU"
 			StringBuilder sb = new StringBuilder();
 			sb.append(WP_MSBUILD_PATH);
 			sb.append(STR_SPACE);
 			sb.append(solutionFile[0].getName());
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_TARGET);
 			sb.append(WP_STR_COLON);
 			sb.append(WIN_CLEAN);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(REBUILD);
 			sb.append(STR_SPACE);
 			sb.append(WP_STR_PROPERTY);
 			sb.append(WP_STR_COLON);
 			sb.append(WP_STR_CONFIGURATION + "=" + config);
 			sb.append(WP_STR_SEMICOLON);
 			sb.append(WP_STR_PLATFORM + "=" + WP_STR_DOUBLEQUOTES + platform + WP_STR_DOUBLEQUOTES);
 			log.info("Command = "+ sb.toString());
 			Commandline cl = new Commandline(sb.toString());
 			cl.setWorkingDirectory(baseDir.getPath() + sourceDirectory);
 			Process process = cl.execute();
 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			while ((in.readLine()) != null) {
 			}
 		} catch (CommandLineException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(in);
 		}
 	}
 
 	
 	private void checkPackageVersionNo() throws PhrescoException {
 		try {
 			SAXBuilder builder = new SAXBuilder();
 			File path = new File (rootDir.getPath() + File.separator + projFile[0].getName());
 			
 			Document doc = (Document) builder.build(path);
 			Element rootNode = doc.getRootElement();
 			Namespace ns = rootNode.getNamespace();
 			elementIdentifier(rootNode, WP_PROPERTYGROUP, ns);
 
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void elementIdentifier(Element rootNode, String elementName,	Namespace ns) {
 		List child = rootNode.getChildren(elementName, ns);
 		for (int i = 0; i < child.size(); i++) {
 			Object object = child.get(i);
 			Element project = (Element) object;
 			List children = project.getChildren();
 			for (Object object2 : children) {
 				Element propertyGroup = (Element) object2;
 				findChild(propertyGroup, ns);
 			}
 		}
 	}
 
 	private void findChild(Element element, Namespace ns) {				
 		try {
 			if (element.getName().equalsIgnoreCase(WP_AUTO_INCREMENT_PKG_VERSION_NO) && element.getValue().trim().equalsIgnoreCase("true")) {
 				packageInfo.incrementPackageVersionNo();
 			}
 		} catch (Exception e) {
 		}
 	}
 	
 	private boolean build(String platform) throws MojoExecutionException {
 		boolean isBuildSuccess = true;
 		try {
 			createPackage(platform);
 		} catch (Exception e) {
 			isBuildSuccess = false;
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return isBuildSuccess;
 	}
 
 	private void createPackage(String platform) throws MojoExecutionException {
 		try {
 			zipName = util.createPackage(buildName, buildNumber, nextBuildNo, new Date());
 			zipFilePath = buildDir.getPath() + File.separator + (platform.equalsIgnoreCase("any cpu")?"AnyCPU":platform) + METRO_BUILD_SEPERATOR + zipName;
 			String solutionFilename = solutionFile[0].getName().substring(0, (solutionFile[0].getName().length())-4);
 			if(type.equalsIgnoreCase(WIN_STORE)) {
 				String packageVersion = packageInfo.getPackageVersion();
 				String tempFilePath = rootDir.getPath() + WP_APP_PACKAGE + File.separator + solutionFilename + STR_UNDERSCORE + packageVersion + STR_UNDERSCORE + (platform.equalsIgnoreCase("any cpu")?"AnyCPU":platform) + (config.equalsIgnoreCase("debug")? STR_UNDERSCORE + config : "") + WP_TEST;
 				tempDir = new File(tempFilePath);
 			} else if(type.equalsIgnoreCase(WIN_PHONE)) {
 				tempDir = new File(baseDir + sourceDirectory + File.separator + WP_SOURCE + File.separator + solutionFilename + WP7_BIN_FOLDER + File.separator + config);
 			}
 			if (!tempDir.exists()) {
 				throw new PhrescoException("Startup project must be placed in a folder named same as solution (.sln) file name");
 			}
 			
 			File packageInfoFile = new File(baseDir.getPath() + File.separator + DOT_PHRESCO_FOLDER + File.separator + PHRESCO_PACKAGE_FILE);
 			// To Copy Contents From Package Info File
 			if(packageInfoFile.exists()) {
 				PluginUtils.createBuildResources(packageInfoFile, baseDir, tempDir);
 			}
 			ArchiveUtil.createArchive(tempDir.getPath(), zipFilePath, ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 
 	private void writeBuildInfo(boolean isBuildSuccess, String platform, WinBuildInfo wInfo) throws MojoExecutionException {
 		wInfo.generateBuildInfo(isBuildSuccess, platform, buildNumber, nextBuildNo, environmentName, buildName, new Date(), buildInfoFile, zipName);
 	}
 	
 	
 	
 	private static void deleteOutputDir(File path) throws MojoExecutionException {
 		try {
 			File[] listFiles = path.listFiles();
 			for (File file : listFiles) {
 				if(file.isDirectory()) {
 					deleteOutputDir(file);
 					File outputFolder = new File(file.getParent());
 					if (outputFolder.getName().equalsIgnoreCase(BIN) || outputFolder.getName().equalsIgnoreCase(OBJ) || outputFolder.getName().equalsIgnoreCase(WP_APP_PACKAGE_FOLDER)) {
 						FileUtils.deleteDirectory(outputFolder); 
 					}
 				} else {
 					File outputFolder = new File(file.getParent());
 					if (outputFolder.getName().equalsIgnoreCase(BIN) || outputFolder.getName().equalsIgnoreCase(OBJ) || outputFolder.getName().equalsIgnoreCase(WP_APP_PACKAGE_FOLDER)) {
 						FileUtils.deleteDirectory(outputFolder); 
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void cleanUp() throws MojoExecutionException {
 		try {
 			FileUtils.deleteDirectory(tempDir);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 
 	}
 }
