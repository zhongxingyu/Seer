 /**
  * Phresco Maven Plugin
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
 package com.photon.phresco.plugins;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.Type;
 import java.util.Map;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.api.PhrescoPlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Implementation.Dependency;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 
 /**
  * Goal which validate the code
  * 
  * @goal validate-code
  * 
  */
 public class SonarCodeValidator extends PhrescoAbstractMojo implements PluginConstants {
 
 	private static final String VALIDATE_CODE = Constants.PHASE_VALIDATE_CODE;
 
 	/**
 	 * The Maven project.
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter expression="${project.basedir}" required="true"
 	 * @readonly
 	 */
 	protected File baseDir;
 	private File testConfigPath;
 
     /**
      * @parameter expression="${moduleName}"
      * @readonly
      */
     protected String moduleName = "";
     
     /**
      * @parameter expression="${interactive}" required="true"
      * @readonly
      */
     private boolean interactive;
     private Configuration config;
     private String dotPhrescoDirName;
     private File dotPhrescoDir;
     private File srcDirectory;
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		getLog().info("Executing Code Validation");
 		File targetDir = null;
 		Gson gson = new Gson();
 		try {
 			PluginUtils utils = new PluginUtils();
 			File workingDirectory = baseDir;
 			if (StringUtils.isNotEmpty(moduleName)) {
 				workingDirectory = new File(baseDir.getPath() + File.separator + moduleName);
 			} 
 			dotPhrescoDir = workingDirectory;
 			dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				dotPhrescoDir = new File(baseDir.getParent() +  File.separatorChar + dotPhrescoDirName);
         	}
         	if (StringUtils.isNotEmpty(dotPhrescoDirName) && StringUtils.isNotEmpty(moduleName)) {
         		dotPhrescoDir = new File(baseDir.getParentFile().getPath() +  File.separatorChar + dotPhrescoDirName + File.separatorChar + moduleName);
         	}
         	srcDirectory = workingDirectory;
         	File splitProjectDirectory = utils.getSplitProjectSrcDir(project.getFile(), dotPhrescoDir, moduleName);
         	if (splitProjectDirectory != null) {
         		srcDirectory = splitProjectDirectory;
         	}
 			String projectInfoPath = dotPhrescoDir + File.separator + DOT_PHRESCO_FOLDER + File.separatorChar + Constants.PROJECT_INFO_FILE;
 			targetDir = new File(workingDirectory + File.separator + DO_NOT_CHECKIN_FOLDER + File.separatorChar + TARGET);
 			String infoFile = dotPhrescoDir + File.separator + Constants.VALIDATE_CODE_INFO_FILE;
 			FileReader projectInfoJson = new FileReader(new File(projectInfoPath));
 			Type projectInfoType = new TypeToken<ProjectInfo>(){}.getType();
 			ProjectInfo projectInfo = gson.fromJson(projectInfoJson , projectInfoType);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String techId = applicationInfo.getTechInfo().getId();
 			MojoProcessor mojoProcessor = new MojoProcessor(new File(infoFile));
 			config = mojoProcessor.getConfiguration(VALIDATE_CODE);
 	    	if(interactive) {
 	    		config = getInteractiveConfiguration(config, mojoProcessor, project,VALIDATE_CODE);
 	    	} 
 			Map<String, String> parameters = MojoUtil.getAllValues(config);
 			String testAgainst = parameters.get("sonar");
 			String environmentName = parameters.get(ENVIRONMENT_NAME);
 			if (((techId.equals(TechnologyTypes.ANDROID_NATIVE) && testAgainst.equals("functional")) || (techId.equals(TechnologyTypes.ANDROID_HYBRID) && testAgainst.equals("functional"))) ||  (techId.equals(TechnologyTypes.JAVA_STANDALONE) && testAgainst.equals("functional"))) {
 				String[] list = targetDir.list(new JarFileNameFilter());
 				if (list == null || list.length == 0) {
 					throw new MojoExecutionException("Code Validation for functional test scripts requires a build. Generate a build and try again.");
 				}
 			} 
 
 			if (techId.equals(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET) || techId.equals(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET) ||
 						techId.equals(TechnologyTypes.HTML5_MOBILE_WIDGET) || techId.equals(TechnologyTypes.HTML5_WIDGET) || techId.equals(TechnologyTypes.HTML5)  ) {
 					try {
 						File pomFile = utils.getPomFile(dotPhrescoDir, workingDirectory);
 						PomProcessor processor = new PomProcessor(pomFile);
 						String testSourcePath = processor.getProperty("phresco.env.test.config.xml");
 						if (!techId.equals(TechnologyTypes.JAVA_STANDALONE) && !techId.equals(TechnologyTypes.JAVA_WEBSERVICE) ) {
 							if(StringUtils.isNotEmpty(testSourcePath)){
 								testConfigPath = new File(srcDirectory + File.separator + testSourcePath);
 								String fullPathNoEndSeparator = FilenameUtils.getFullPathNoEndSeparator(testConfigPath.getAbsolutePath());
 								File fullPathNoEndSeparatorFile = new File(fullPathNoEndSeparator);
 								fullPathNoEndSeparatorFile.mkdirs();
							if (StringUtils.isNotEmpty(environmentName)) {
 								utils.executeUtil(environmentName, dotPhrescoDir.getPath(), testConfigPath);
							}
						}
 						}
 					} catch (PhrescoPomException e) {
 						throw new MojoExecutionException(e.getMessage(), e);
 					} catch (PhrescoException e) {
 						throw new MojoExecutionException(e.getMessage(), e);
 					} 
 				}
 
 
 			pluginValidate(infoFile);
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} 
 	}
 
 	
 	private void pluginValidate(String infoFile) throws PhrescoException {
 		Map<String, String> allValues = MojoUtil.getAllValues(config);
 		String mvnDependencyId = allValues.get(SRC);
         Dependency dependency = getDependency(infoFile, VALIDATE_CODE, mvnDependencyId);
       
         String processName = ManagementFactory.getRuntimeMXBean().getName();
  		String[] split = processName.split("@");
  		String processId = split[0].toString();
  		
  		Utility.writeProcessid(baseDir.getPath(), PluginConstants.CODE_VALIDATE, processId);
  		getLog().info("Writing Process Id...");
  		
 		if (isGoalAvailable(infoFile, VALIDATE_CODE) && dependency != null) {
 			PhrescoPlugin plugin = getPlugin(dependency);
 			plugin.validate(config, getMavenProjectInfo(project, moduleName));
 		} else {
 			PhrescoPlugin plugin = new PhrescoBasePlugin(getLog());
 			plugin.validate(config, getMavenProjectInfo(project, moduleName));
 		}
 	}
 }
 
 class JarFileNameFilter implements FilenameFilter {
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".jar");
 	}
 }
