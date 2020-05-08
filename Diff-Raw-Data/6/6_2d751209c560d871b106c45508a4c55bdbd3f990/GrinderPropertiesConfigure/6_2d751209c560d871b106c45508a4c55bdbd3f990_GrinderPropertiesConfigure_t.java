 //   Copyright 2012 Giuseppe Iacono, Felipe Munoz Castillo
 //
 //   Licensed under the Apache License, Version 2.0 (the "License");
 //   you may not use this file except in compliance with the License.
 //   You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 //   Unless required by applicable law or agreed to in writing, software
 //   distributed under the License is distributed on an "AS IS" BASIS,
 //   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //   See the License for the specific language governing permissions and
 //   limitations under the License.
 
 package com.fides;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.shared.filtering.MavenFilteringException;
 import org.apache.maven.shared.filtering.MavenResourcesExecution;
 import org.apache.maven.shared.filtering.MavenResourcesFiltering;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Provide methods to configure the plug-in
  *
  * @author Giuseppe Iacono
 * @requiresDependencyResolution test
  */
 public abstract class GrinderPropertiesConfigure extends AbstractMojo {
 
 	// Grinder JVM classpath system property name constant.
 	protected static final String GRINDER_JVM_CLASSPATH = "grinder.jvm.classpath";
 
 	// default agent
 	private static final boolean DEFAULT_DAEMON_OPTION = false;
 
 	// default agent sleep time in milliseconds
 	private static final long DEFAULT_DAEMON_PERIOD = 60000;
 
 	// local configuration directory
 	public static final String CONFIG = "target/grinder/config";
 
 	// local grinder properties directory
 	public static final String PATH_PROPERTIES_DIR = "src/grinder/config";
 
 	// local log directory
 	public static final String LOG_DIRECTORY = "target/grinder/log_files";
 
 	// local tcpproxy directory
 	public static final String TCP_PROXY_DIRECTORY = "target/grinder/tcpproxy";
 
 	// grinder properties
 	private Properties propertiesPlugin = new Properties();
 
 	// configuration file
 	private File fileProperties = null;
 
 	// grinder properties file path
 	private String pathProperties = null;
 
 	private String analyzerProperties = null;
 
 	// Test path
 	private String test = null;
 
 	// value of agent daemon option
 	private boolean daemonOption = DEFAULT_DAEMON_OPTION;
 
 	// value of agent sleep time
 	private long daemonPeriod = DEFAULT_DAEMON_PERIOD;
 
 	// GrinderPropertiesConfigure logger
 	private final Logger logger = LoggerFactory.getLogger("GrinderPropertiesConfigure");
 
 	/**
 	 * List of properties defined in the pom.xml file of Maven project.
 	 *
 	 * @parameter
 	 */
 	private Map<String, String> properties;
 
 	/**
 	 * The grinder properties file path defined in the pom.xml file of Maven project.
 	 *
 	 * @parameter
 	 */
 	private String path;
 
 	/**
 	 * The absolute path of test script directory defined in the pom.xml file of Maven project.
 	 *
 	 * @parameter
 	 * @required
 	 */
 	private String pathTest;
 
 	/**
 	 * Agent daemon option defined in the pom.xml file of Maven project.
 	 *
 	 * @parameter
 	 */
 	private boolean daemon_option;
 
 	/**
 	 * Agent sleep time in milliseconds defined in the pom.xml file of Maven project.
 	 *
 	 * @parameter
 	 */
 	private long daemon_period;
 
 	/**
 	 * List of Plugin dependencies
 	 *
 	 * @parameter expression="${plugin.artifacts}"
 	 */
 	private List<Artifact> pluginArtifacts;
 
 	/**
 	 * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
 	 * @required
 	 */
 	private MavenResourcesFiltering mavenResourcesFiltering;
 
 	/**
 	 * The character encoding scheme to be applied when filtering resources.
 	 *
 	 * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
 	 */
 	private String encoding;
 
 	/**
 	 * @parameter default-value="${project}"
 	 * @required
 	 * @readonly
 	 */
 	private MavenProject mavenProject;
 
 	/**
 	 * @parameter default-value="${session}"
 	 * @readonly
 	 * @required
 	 */
 	private MavenSession mavenSession;
 
 	/**
 	 * The list of additional filter properties files to be used along with System and project
 	 * properties, which would be used for the filtering.
 	 * <br/>
 	 * See also: {@link org.apache.maven.plugin.resources.ResourcesMojo#filters}.
 	 *
 	 * @parameter default-value="${project.build.filters}"
 	 * @readonly
 	 * @since 2.4
 	 */
 	private List<?> buildFilters;
 
 	/**
 	 * Indicates if the project dependencies should be used when executing
      * the main class.
 	 * @parameter expression="${includeProjectDependencies}" default-value="true"
 	 * @since 2.4
 	 */
 	private boolean includeProjectDependencies;
 
 	/**
 	 * @parameter default-value="true"
 	 */
 	private boolean filteringEnabled;
 
 	public List<Artifact> getPluginArtifacts() {
 		return pluginArtifacts;
 	}
 
 	public void setPluginArtifacts(final List<Artifact> pluginArtifacts) {
 		this.pluginArtifacts = pluginArtifacts;
 	}
 
 	public static boolean getDEFAULT_DAEMON_OPTION() {
 		return DEFAULT_DAEMON_OPTION;
 	}
 
 	public static long getDEFAULT_DAEMON_PERIOD() {
 		return DEFAULT_DAEMON_PERIOD;
 	}
 
 	public static String getCONFIG() {
 		return CONFIG;
 	}
 
 	public static String getPATH_PROPERTIES_DIR() {
 		return PATH_PROPERTIES_DIR;
 	}
 
 	public static String getLOG_DIRECTORY() {
 		return LOG_DIRECTORY;
 	}
 
 	public static String getTCP_PROXY_DIRECTORY() {
 		return TCP_PROXY_DIRECTORY;
 	}
 
 	public Properties getPropertiesPlugin() {
 		return propertiesPlugin;
 	}
 
 	public void setPropertiesPlugin(final Properties propertiesPlugin) {
 		this.propertiesPlugin = propertiesPlugin;
 	}
 
 	public File getFileProperties() {
 		return fileProperties;
 	}
 
 	public void setFileProperties(final File fileProperties) {
 		this.fileProperties = fileProperties;
 	}
 
 	public String getPathProperties() {
 		return pathProperties;
 	}
 
 	public void setPathProperties(final String pathProperties) {
 		this.pathProperties = pathProperties;
 	}
 
 	public String getAnalyzerProperties() {
 		return analyzerProperties;
 	}
 
 	public void setAnalyzerProperties(final String analyzerProperties) {
 		this.analyzerProperties = analyzerProperties;
 	}
 
 	public String getTest() {
 		return test;
 	}
 
 	public void setTest(final String test) {
 		this.test = test;
 	}
 
 	public boolean isDaemonOption() {
 		return daemonOption;
 	}
 
 	public boolean getdaemonOption() {
 		return daemonOption;
 	}
 
 	public void setDaemonOption(final boolean daemonOption) {
 		this.daemonOption = daemonOption;
 	}
 
 	public long getDaemonPeriod() {
 		return daemonPeriod;
 	}
 
 	public void setDaemonPeriod(final long daemonPeriod) {
 		this.daemonPeriod = daemonPeriod;
 	}
 
 	public Logger getLogger() {
 		return logger;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setAgentJVMArg() {
 		try {
 			final List<Artifact> artifacts = new ArrayList<Artifact>();
 			if(includeProjectDependencies) {
 				artifacts.addAll(mavenProject.getArtifacts());
			} else {
				artifacts.addAll(pluginArtifacts);
 			}
 
 			for (final Artifact a : artifacts) {
 				if(a.getArtifactId().startsWith("grinder-dcr-agent")) {
 					String agentJarPath;
 					agentJarPath = MavenUtilities.normalizePath(
 							MavenUtilities.getPluginAbsolutePath(
 									a.getGroupId(),
 									a.getArtifactId(),
 									a.getVersion(),
 									a.getClassifier()));
 
 					// Add the -javaagent JVM argument to the list of JVM arguments, if they already exist.
 					final StringBuilder jvmArgs = new StringBuilder();
 					jvmArgs.append(propertiesPlugin.getProperty("grinder.jvm.arguments", ""));
 					if(jvmArgs.length() > 0) {
 						jvmArgs.append(" ");
 					}
 					jvmArgs.append("-javaagent:");
 					jvmArgs.append(agentJarPath);
 					propertiesPlugin.setProperty("grinder.jvm.arguments", jvmArgs.toString());
 					break;
 				}
 			}
 
 			if(logger.isDebugEnabled()) {
 				logger.debug("Grinder JVM arguments: " + propertiesPlugin.getProperty("grinder.jvm.arguments"));
 			}
 		} catch (final FileNotFoundException e) {
 			logger.error("Unable to set agent JVM argument.", e);
 		} catch (final IOException e) {
 			logger.error("Unable to set agent JVM argument.", e);
 		} catch (final XmlPullParserException e) {
 			logger.error("Unable to set agent JVM argument.", e);
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	private void setClassPath() {
 		Artifact a = null;
 		final Collection artifacts = pluginArtifacts;
 		final StringBuffer pluginDependencies = new StringBuffer();
 		String grinderJar = null;
 
 		for (final Iterator i = artifacts.iterator();  i.hasNext();) {
 			a = (Artifact) i.next();
 
 			grinderJar = MavenUtilities.normalizePath(a.getFile().getPath());
 			pluginDependencies.append(grinderJar);
 			if (i.hasNext()) {
 				pluginDependencies.append(File.pathSeparator);
 			}
 		}
 
 		propertiesPlugin.setProperty(GRINDER_JVM_CLASSPATH, pluginDependencies.toString());
 	}
 
 	/**
 	 * Set grinder properties
 	 */
 	private void initPropertiesFile() {
 		if (path == null) {		// try to find grinder properties file in the PATH_PROPERTIES_DIR
 			final File[] configFiles = new File(PATH_PROPERTIES_DIR).listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(final File dir, final String name) {
 					return name.endsWith(".properties");
 				}
 			});
 
 			if(configFiles.length == 0) {
 				logger.error(PATH_PROPERTIES_DIR + " must at least contain a grinder.properties file!");
 				System.exit(-1);
 			}
 
 			if(configFiles.length > 2) {
 				logger.error(PATH_PROPERTIES_DIR + " must contain no more than one grinder.properties file and one analyzer.properties file!");
 				System.exit(-1);
 			}
 
 			for(final File propertyFile : configFiles) {
 				if(propertyFile.getName().equals("grinder.properties")) {
 					setPathProperties(PATH_PROPERTIES_DIR + File.separator + propertyFile.getName());
 				} else if(propertyFile.getName().equals("analyzer.properties")) {
 					setAnalyzerProperties(PATH_PROPERTIES_DIR + File.separator + propertyFile.getName());
 				}
 			}
 		} else {
 			setPathProperties(path);
 		}
 
 		// load grinder properties from the grinder properties file
 		FileInputStream is = null;
 		try {
 			final File filteredFile = (filteringEnabled) ? filterGrinderPropertiesFile(pathProperties) : new File(pathProperties);
 			is = new FileInputStream(filteredFile);
 			propertiesPlugin.load(is);
 		} catch (final FileNotFoundException e) {
 			if(logger.isDebugEnabled()){
 				logger.error("");
 				logger.error(" ----------------------------");
 				logger.error("|   Configuration ERROR!!!   |");
 				logger.error(" ----------------------------");
 				logger.error("");
 				logger.error(" The grinder properties file path ");
 				logger.error(" " + pathProperties);
 				logger.error(" set into your POM file do not exists! ");
 				System.exit(0);
 			}
 		} catch (final MavenFilteringException e) {
 			logger.error("Unable to load Grinder properties file.", e);
 		} catch (final IOException e) {
 			logger.error("Unable to load Grinder properties file.", e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (final IOException e) {}
 			}
 		}
 
 		// load grinder properties from pom.xml file of Maven project
 		if (properties != null) {
 			final Iterator <Map.Entry <String, String>> iterator = properties.entrySet().iterator();
 			Map.Entry<String, String> pair;
 			while (iterator.hasNext()) {
 				pair = iterator.next();
 				if (pair.getValue() != null && pair.getKey().startsWith("grinder.")) {
 					propertiesPlugin.setProperty(pair.getKey(), pair.getValue());
 				}
 			}
 		}
 
 		setAgentJVMArg();
 		setClassPath();
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("--- Grinder properties file:  " + pathProperties);
 		}
 	}
 
 	/**
 	 * Set test file path to execute
 	 */
 	private void initTestFile() {
 		File testScript = null;		// test script configured
 		String nameTest = null;		// test script path
 		final String nameScript = propertiesPlugin.getProperty("grinder.script");
 
 		if (nameScript == null) {
 			if(logger.isDebugEnabled()) {
 				logger.error("");
 				logger.error(" ----------------------------");
 				logger.error("|   Configuration ERROR!!!   |");
 				logger.error(" ----------------------------");
 				logger.error("");
 				logger.error(" The 'grinder.script' property is missing from your Grinder properties file. ");
 				System.exit(0);
 			}
 		} else {
 			nameTest = pathTest + File.separator + nameScript;
 		}
 
 		testScript = new File(nameTest);
 
 		if (!testScript.exists()) {
 			if (logger.isDebugEnabled()) {
 				logger.error("");
 				logger.error(" ----------------------------");
 				logger.error("|   Configuration ERROR!!!   |");
 				logger.error(" ----------------------------");
 				logger.error("");
 				logger.error(" The test file path ");
 				logger.error(" " + nameTest);
 				logger.error(" set into your POM file do not exists!");
 				System.exit(0);
 			}
 		}
 		setTest(nameTest);
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("--- Grinder test file:  " + test);
 		}
 	}
 
 	/**
 	 * Set log directory
 	 */
 	private void initLogDirectory() {
 		// make sure the logDirectory exists
 		final File logDirectory = new File(LOG_DIRECTORY);
 		if (logDirectory != null && !logDirectory.exists()) {
 			logDirectory.mkdirs();
 		}
 
 		// set logDirectory
 		propertiesPlugin.setProperty("grinder.logDirectory", LOG_DIRECTORY);
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("--- Log directory:  " + LOG_DIRECTORY);
 		}
 	}
 
 	/**
 	 * Set agent daemon option and sleep time
 	 */
 	private void initAgentOption() {
 		if (daemon_option == true) {
 			daemonOption = true;
 			if (daemon_period > 0) {
 				daemonPeriod = daemon_period;
 			} else {
 				daemonPeriod = DEFAULT_DAEMON_PERIOD;
 			}
 		} else {
 			daemonOption = DEFAULT_DAEMON_OPTION;
 			daemonPeriod = DEFAULT_DAEMON_PERIOD;
 		}
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("--- Agent -daemon option:  " + daemonOption);
 
 			if (daemonOption == true) {
 				logger.debug("--- Agent sleep time:  " + daemonPeriod);
 			}
 		}
 	}
 
 	/**
 	 * Initialize configuration directory
 	 */
 	private void initConfigurationDirectory() {
 		// make sure the configDirectory exists
 		final File configDirectory = new File(CONFIG);
 		if (configDirectory != null && !configDirectory.exists()) {
 			configDirectory.mkdirs();
 		}
 
 		// copy grinder properties to configuration directory
 		final String pathProperties = CONFIG + File.separator + "grinder_agent.properties";
 		BufferedWriter out;
 
 		try {
 			out = new BufferedWriter(new FileWriter(pathProperties));
 			propertiesPlugin.store(out, "Grinder Agent Properties");
 		} catch (final FileNotFoundException e) {
 			logger.error("File " + pathProperties + " does not exists!!!", e);
 		} catch (final IOException e) {
 			logger.error("Unable to load " + pathProperties + ".", e);
 		}
 
 		// create configuration file
 		fileProperties = new File(pathProperties);
 
 		// copy test file to configuration directory
 		if (test != null) {
 			String line = null;
 			final String dest_test = CONFIG + File.separator +
 					propertiesPlugin.getProperty("grinder.script");
 			BufferedReader source = null;
 			BufferedWriter dest = null;
 
 			try {
 				source = new BufferedReader(new FileReader(test));
 				dest = new BufferedWriter(new FileWriter(dest_test));
 				line = source.readLine();
 				while (line != null) {
 					dest.write(line + "\n");
 					line = source.readLine();
 				}
 			} catch (final FileNotFoundException e) {
 				logger.error("Test file does not exist.", e);
 			} catch (final IOException e) {
 				logger.error("Test file does not exist.", e);
 			} finally {
 				if (source != null || dest != null) {
 					try {
 						source.close();
 						dest.close();
 					} catch (final IOException e) {
 						logger.error("Unable to close streams.", e);
 					}
 				}
 			}
 		}
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("--- Grinderplugin to be configured ---");
 		}
 	}
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		initPropertiesFile();
 
 		initTestFile();
 
 		initLogDirectory();
 
 		initAgentOption();
 
 		initConfigurationDirectory();
 	}
 
 	/**
 	 * Filters the selected Grinder properties file, replacing variable place-holders with their
 	 * associated values found in the project's pom.xml file.
 	 * @param grinderProperteisFile The path to the selected Grinder properties file as a String.
 	 * @return The filtered Grinder properties file.
 	 * @throws MavenFilterException if unable to filter the properties file.
 	 */
 	private File filterGrinderPropertiesFile(final String grinderPropertiesFile) throws MavenFilteringException {
 		final File propertiesFile = new File(grinderPropertiesFile);
 		final Resource resource = new Resource();
 		resource.setDirectory(propertiesFile.getParent());
 		resource.setFiltering(true);
 		resource.setIncludes(Collections.singletonList("**/*.properties"));
 
 		final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution ( Collections.singletonList(resource), new File(CONFIG), mavenProject,
 				encoding, buildFilters,
 				Collections.EMPTY_LIST, mavenSession );
 
 		mavenResourcesFiltering.filterResources( mavenResourcesExecution );
 		return new File(CONFIG, propertiesFile.getName());
 	}
 }
