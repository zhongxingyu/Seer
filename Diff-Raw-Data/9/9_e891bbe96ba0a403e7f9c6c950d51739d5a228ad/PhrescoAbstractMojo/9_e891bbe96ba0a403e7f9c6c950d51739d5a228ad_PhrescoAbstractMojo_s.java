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
 
 import java.io.BufferedReader;
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.api.PhrescoPlugin;
import com.photon.phresco.plugins.commons.UIParameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Name.Value;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Implementation.Dependency;
 import com.photon.phresco.plugins.util.MavenPluginArtifactResolver;
 import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 
 public abstract class PhrescoAbstractMojo extends AbstractMojo {
 
 	/**
 	 * The entry point to Aether, i.e. the component doing all the work.
 	 * 
 	 * @component
 	 */
 	private RepositorySystem repoSystem;
 
 	/**
 	 * The current repository/network configuration of Maven.
 	 * 
 	 * @parameter default-value="${repositorySystemSession}"
 	 * @readonly
 	 */
 	private RepositorySystemSession repoSession;
 
 	/**
 	 * The project's remote repositories to use for the resolution of project dependencies.
 	 * 
 	 * @parameter default-value="${project.remoteProjectRepositories}"
 	 * @readonly
 	 */
 	private List<RemoteRepository> projectRepos;
 
 	/**
 	 * @parameter expression="${project.basedir}" required="true"
 	 * @readonly
 	 */
 	protected File baseDir;
 
 	private Map<String, Boolean> depMap = new HashMap<String, Boolean>();
 
 	public PhrescoPlugin getPlugin(Dependency dependency) throws PhrescoException {
 		//Caching not needed since it will be triggered as a new process every time from the maven
 		return constructClass(dependency);
 	}
 
 	private PhrescoPlugin constructClass(Dependency dependency) throws PhrescoException {
 		Log log = getLog();
 		try {
 			Class<PhrescoPlugin> apiClass = (Class<PhrescoPlugin>) Class
 			.forName(dependency.getClazz(), true, getURLClassLoader(dependency));
 			Constructor<PhrescoPlugin> constructor = apiClass.getDeclaredConstructor(Log.class);
 			return constructor.newInstance(log);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 	}
 
 	private URLClassLoader getURLClassLoader(Dependency dependency) throws PhrescoException {
 		List<Artifact> artifacts = new ArrayList<Artifact>();
 
 		List<ArtifactGroup> plugins  = new ArrayList<ArtifactGroup>();
 		ArtifactGroup artifactGroup = new ArtifactGroup();
 		artifactGroup.setGroupId(dependency.getGroupId());
 		artifactGroup.setArtifactId(dependency.getArtifactId());
 		artifactGroup.setPackaging(dependency.getType());
 		ArtifactInfo info = new ArtifactInfo();
 		info.setVersion(dependency.getVersion());
 		artifactGroup.setVersions(Collections.singletonList(info));
 		plugins.add(artifactGroup);
 
 		for (ArtifactGroup plugin : plugins) {
 			List<ArtifactInfo> versions = plugin.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				Artifact artifact = new DefaultArtifact(plugin.getGroupId(), plugin.getArtifactId(), PluginConstants.PACKAGING_TYPE_JAR, artifactInfo.getVersion());
 				artifacts.add(artifact);
 			}
 		}
 
 		URL[] artifactURLs;
 		try {
 			artifactURLs = MavenPluginArtifactResolver.resolve(projectRepos.get(0), artifacts, repoSystem, repoSession);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
 		if (clsLoader == null) {
 			clsLoader = this.getClass().getClassLoader();
 		}
 		URLClassLoader classLoader = new URLClassLoader(artifactURLs, clsLoader);
 		return classLoader;
 	}
 
 	protected Configuration getConfiguration(String infoFile, String goal) throws PhrescoException {
 		MojoProcessor processor = new MojoProcessor(new File(infoFile));
 		return processor.getConfiguration(goal);
 	}
 
 	protected MavenProjectInfo getMavenProjectInfo(MavenProject project) {
 		MavenProjectInfo mavenProjectInfo = new MavenProjectInfo();
 		mavenProjectInfo.setBaseDir(project.getBasedir());
 		mavenProjectInfo.setProject(project);
 		mavenProjectInfo.setProjectCode(project.getBasedir().getName());
 		return mavenProjectInfo;
 	}
 	
 	protected MavenProjectInfo getMavenProjectInfo(MavenProject project, String subModule) {
         MavenProjectInfo mavenProjectInfo = new MavenProjectInfo();
     	mavenProjectInfo.setBaseDir(project.getBasedir());
         mavenProjectInfo.setProject(project);
         mavenProjectInfo.setProjectCode(project.getBasedir().getName());
         mavenProjectInfo.setModuleName(subModule);
         return mavenProjectInfo;
     }
 
 	protected Dependency getDependency(String infoFile, String goal) throws PhrescoException {
 		MojoProcessor processor = new MojoProcessor(new File(infoFile));
 		if (processor.getImplementationDependency(goal) != null) {
 			return processor.getImplementationDependency(goal).getDependency();
 		}
 		return null;
 	}
 
 	protected boolean isGoalAvailable(String infoFile, String goal) throws PhrescoException {
 		MojoProcessor processor = new MojoProcessor(new File(infoFile));
 		return processor.isGoalAvailable(goal);
 	}
 	
 	private void setDependentMap(List<Parameter> parameters) {
 		List<String> arrayList = new ArrayList<String>();
 		PluginUtils pu = new PluginUtils();
 		for (Parameter parameter : parameters) {
 			if (StringUtils.isNotEmpty(parameter.getDependency())) {
 				String dependencies = parameter.getDependency();
 				List<String> depList = pu.csvToList(dependencies);
 				for (String dependency : depList) {
 					depMap.put(parameter.getKey() + dependency, true);
 				}
 			}
 			if(parameter.getType().equalsIgnoreCase("List")) {
 				List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value> possibleValues = parameter.getPossibleValues().getValue();
 				setDependentMap(arrayList, pu, possibleValues);
 			} if (!arrayList.contains(parameter.getKey())) {
 				depMap.put(parameter.getKey(), true);
 			}
 		}
 	}
 
 	private void setDependentMap(List<String> arrayList, PluginUtils pu,
 			List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value> possibleValues) {
 		List<String> dependencyList = null;
 		for (int i = 0; i < possibleValues.size(); i++) {
 			String key = possibleValues.get(i).getKey();
 			if (StringUtils.isNotEmpty(possibleValues.get(i).getDependency())) {
 				dependencyList = pu.csvToList(possibleValues.get(i).getDependency());
 				for (String dependency : dependencyList) {
 					depMap.put(key + dependency, true);
 					arrayList.add(dependency);
 				}
 			} if (CollectionUtils.isNotEmpty(dependencyList)) {
 				for (String dependency : dependencyList) {
 					if (!depMap.containsKey(key + dependency)) {
 						depMap.put(key + dependency, false);
 						arrayList.add(dependency);
 					}
 				}
 			}
 		}
 	}
 
 	protected Configuration getInteractiveConfiguration(Configuration configuration, MojoProcessor processor, MavenProject project, String goal) throws PhrescoException {
 		try {
 //		PhrescoCreate phrescoCreate = new PhrescoCreate();
 //		ServiceManager serviceManager = phrescoCreate.getServiceManager();
 		if (configuration == null) {
 			return null;
 		}
 		Parameters parameters = configuration.getParameters();
 		List<Parameter> parameter = parameters.getParameter();
 		setDependentMap(parameter);
 			List<String> valueList = new ArrayList<String>();
 			for (Parameter param : parameter) {
 				String value = "";
 				boolean show = false;
 				if (CollectionUtils.isNotEmpty(valueList)) {
 					for (String value1 : valueList) {
 						if (depMap.containsKey(value1 + param.getKey())) {
 							show = depMap.get(value1 + param.getKey());
 						} 
 					}
 				} if (depMap.containsKey(param.getKey())) {
 					show = depMap.get(param.getKey());
 				}
 				if (show) {
 					Value name = param.getName().getValue().get(0);
 					value = getValue(param, name, project, processor, goal);
 					valueList.add(value);
 					param.setValue(value);
 				}
 			}
 			processor.save();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 		return configuration;
 	}
 
 	private String getValue(Parameter parameter, Value value, MavenProject project, MojoProcessor processor, String goal) throws MojoExecutionException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String paramValue = "";
 		try {
 			if(parameter.getType().equalsIgnoreCase("Boolean") & ! parameter.getKey().equalsIgnoreCase("showSettings")) {
 				System.out.println("Enter Value For " + value.getValue() + " (Y/N)");
 				String readValue = br.readLine();
 				paramValue = String.valueOf(true);
 				if(readValue.equalsIgnoreCase("F")) {
 					paramValue = String.valueOf(false);
 				}
 			}
 
 			if(parameter.getType().equalsIgnoreCase("List")) {
 				Map<String, String> pMap = new HashMap<String, String>();
 				Map<String, com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value> possibleValMap = new HashMap<String, com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value>();
 				List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.
 				Parameter.PossibleValues.Value> possibleValues = parameter.getPossibleValues().getValue();
 				System.out.println("Enter value for " + value.getValue());
 				for (int i = 0; i < possibleValues.size(); i++) {
 					System.out.println(i + "." + possibleValues.get(i).getValue());
 					pMap.put(String.valueOf(i), possibleValues.get(i).getKey());
 					possibleValMap.put(String.valueOf(i), possibleValues.get(i));
 				}
 				String enteredValue = br.readLine();
 				paramValue = pMap.get(enteredValue);
 			}
 
 			if(parameter.getType().equalsIgnoreCase("DynamicParameter")) {
 				paramValue = getEnvironmentName(parameter, value, project, processor, goal);
 			}
 
 			if(parameter.getType().equalsIgnoreCase("Number")) {
 				System.out.println("Enter value for " + value.getValue());
 				paramValue = br.readLine();
 			}
 
 			if(parameter.getType().equalsIgnoreCase("String")) {
 				System.out.println("Enter value for " + value.getValue());
 				paramValue = br.readLine();
 			}
 			if(parameter.getType().equalsIgnoreCase("Hidden")) {
 				System.out.println("Enter value for " + value.getValue());
 				paramValue = br.readLine();
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return paramValue;
 	}
 
 	private String getEnvironmentName(Parameter parameter, Value value, MavenProject project, MojoProcessor processor, String goal) throws MojoExecutionException {
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			Map<String, String> envMap = new HashMap<String, String>(); 
 			System.out.println("Select " + parameter.getName().getValue().get(0).getValue());
 			PossibleValues possibleValue = dynamicClassLoader(parameter, project, processor, goal);
 			List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value> values = possibleValue.getValue();
 
 			for (int i = 0; i < values.size(); i++) {
 				System.out.println(i + "." + values.get(i).getValue());
 				envMap.put(String.valueOf(i), values.get(i).getValue());
 			} 
 			return envMap.get(br.readLine());
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private PossibleValues dynamicClassLoader(Parameter parameter, MavenProject project, MojoProcessor processor, String goal) throws PhrescoException {
 		try {
 			File projectInfoFile = new File(project.getBasedir().getPath() + File.separatorChar + Constants.DOT_PHRESCO_FOLDER + File.separatorChar + Constants.PROJECT_INFO_FILE);
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(new FileReader(projectInfoFile), ProjectInfo.class);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String customerId = projectInfo.getCustomerIds().get(0);
 			String clazz = parameter.getDynamicParameter().getClazz();
 			Class loadClass = getClassFromLocal(clazz);
 			Parameter buildNoParameter = processor.getParameter(goal, DynamicParameter.KEY_BUILD_NO);
 			String buildNo = "";
 			if (buildNoParameter != null) {
 				buildNo = buildNoParameter.getValue();
 			}
 			if(loadClass != null ) {
 				DynamicParameter dynamicParameter = (DynamicParameter) loadClass.newInstance();
 				Map<String, Object> dynamicParameterMap = new HashMap<String, Object>();
 				dynamicParameterMap.put(DynamicParameter.KEY_APP_INFO, applicationInfo);
 				dynamicParameterMap.put(DynamicParameter.KEY_CUSTOMER_ID, customerId);
 				dynamicParameterMap.put(DynamicParameter.KEY_MOJO, processor);
 				dynamicParameterMap.put(DynamicParameter.KEY_GOAL, goal);
 				dynamicParameterMap.put(DynamicParameter.KEY_MULTI_MODULE, false);
 				dynamicParameterMap.put(DynamicParameter.KEY_BUILD_NO, buildNo);
 				return dynamicParameter.getValues(dynamicParameterMap);
 			}
 		} catch (JsonSyntaxException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (SAXException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		return null;
 	}
 
 	private Class getClassFromLocal(String className) {
 		ClassLoader classLoader = this.getClass().getClassLoader();
 		try {
 			return classLoader.loadClass(className);
 		} catch (ClassNotFoundException e) {
 			return null;
 		}
 	}
 }
