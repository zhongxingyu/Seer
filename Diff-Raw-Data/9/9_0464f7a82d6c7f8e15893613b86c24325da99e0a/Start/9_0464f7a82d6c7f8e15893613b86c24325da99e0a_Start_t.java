 /**
  * java-phresco-plugin
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
 package com.photon.phresco.plugins.felix;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.configuration.ConfigurationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.DatabaseUtil;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 
 public class Start implements PluginConstants {
 
 	/**
 	 * @parameter expression="${projectModule}" required="true"
 	 */
 	protected String projectModule;
 
 	private String environmentName;
 	private MavenProject project;
 	private String serverPort;
 	private String serverContext;
 	private File baseDir;
 	private File workingDirectory;
 	private String subModule = "";
 	private Log log;
 	private String sourceDir;
 	private boolean importSql;
 	private String sqlPath;
 	private PluginUtils pu;
 	private File pomFile;
 	private String pomFileName;
 
 	public void start(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
 		project = mavenProjectInfo.getProject();
 		subModule = mavenProjectInfo.getModuleName();
 		workingDirectory = baseDir;
 		if (StringUtils.isNotEmpty(subModule)) {
 			workingDirectory = new File(baseDir + File.separator + subModule);
 		}
 		pu = new PluginUtils();
 		pomFile = project.getFile();
 		pomFileName = project.getFile().getName();
 		Map<String, String> configs = MojoUtil.getAllValues(configuration);
 		environmentName = configs.get(ENVIRONMENT_NAME);
 		importSql = Boolean.parseBoolean(configs.get(EXECUTE_SQL));
 	    sqlPath = configs.get(FETCH_SQL);
 	    PluginUtils.checkForConfigurations(workingDirectory, environmentName);
 		try {
 			if (environmentName != null) {
 				updateFinalName();
 				configure();
 				storeEnvName();
 			}
 		    createDb();
 			executePhase();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void updateFinalName() throws MojoExecutionException {
 		try {
 			List<com.photon.phresco.configuration.Configuration> configuration = pu.getConfiguration(workingDirectory, environmentName, Constants.SETTINGS_TEMPLATE_SERVER);
 			if(CollectionUtils.isEmpty(configuration)) {
 				throw new PhrescoException("Configuration is Empty...");
 			}
 			for (com.photon.phresco.configuration.Configuration serverConfiguration : configuration) {
 				serverPort = serverConfiguration.getProperties().getProperty(Constants.SERVER_PORT);
 				serverContext = serverConfiguration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 			}
 			PomProcessor pomprocessor = new PomProcessor(pomFile);
 			sourceDir = pomprocessor.getProperty(POM_PROP_KEY_SOURCE_DIR);
 			pomprocessor.setFinalName(serverContext);
 			pomprocessor.save();
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void configure() throws MojoExecutionException {
 		log.info("Configuring the project....");
 		try {
 			adaptSourceConfig();
 			pu.writeDatabaseDriverToConfigXml(workingDirectory, sourceDir, environmentName);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	private void storeEnvName() throws MojoExecutionException {
 		ConfigurationInfo info = new ConfigurationInfo();
 		info.setContext(serverContext);
 		info.setEnvironmentName(environmentName);
 		info.setServerPort(serverPort);
 		Gson gson = new Gson();
 		String envName = gson.toJson(info);
 		FileOutputStream fos = null;
 		File runAgstSrcFile = new File(workingDirectory + File.separator + DOT_PHRESCO_FOLDER + File.separator + ENV_FILE);
 		try {
 			fos = new FileOutputStream(runAgstSrcFile, false);
 			fos.write(envName.getBytes());
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} finally {
 			try {
 				if (fos != null) {
 					fos.close();
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.getMessage());
 			}
 		}
 	}
 
 	private void createDb() throws MojoExecutionException {
 		DatabaseUtil util = new DatabaseUtil();
 		try {
			util.fetchSqlConfiguration(sqlPath, importSql, workingDirectory, environmentName,workingDirectory);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void adaptSourceConfig() throws MojoExecutionException {
 		File wsConfigFile = new File(workingDirectory + sourceDir + FORWARD_SLASH +  CONFIG_FILE);
 		File parentFile = wsConfigFile.getParentFile();
 		try {
 			if (parentFile.exists()) {
 				pu.executeUtil(environmentName, workingDirectory.getPath(), wsConfigFile);
 			}
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void executePhase() throws MojoExecutionException {
 		FileOutputStream fos = null;
 			File errorLog = new File(workingDirectory + File.separator + LOG_FILE_DIRECTORY + RUN_AGS_LOG_FILE);
 			try {
 				StringBuilder sb = new StringBuilder();
 				sb.append(MVN_CMD);
 				sb.append(STR_SPACE);
 				sb.append(JAVA_TOMCAT_RUN);
 				sb.append(STR_SPACE);
 				sb.append(SERVER_PORT);
 				sb.append(serverPort);
 				sb.append(STR_SPACE);
 				sb.append(SERVER_ENV);
 				sb.append(environmentName);
 				sb.append(STR_SPACE);
 				sb.append(Constants.HYPHEN_F);
 				sb.append(STR_SPACE);
 				sb.append(pomFileName);
 				fos = new FileOutputStream(errorLog, false);
 				Utility.executeStreamconsumerFOS(workingDirectory.toString(),sb.toString(), fos);
 			} catch (FileNotFoundException e) {
 				throw new MojoExecutionException(e.getMessage(), e);
 			}
 	}
 }
