 /**
  * Phresco Commons
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
 package com.photon.phresco.util;
 
 
 public interface Constants {
 
 	int STATUS_OPEN = 0;
 	int STATUS_EXPIRED = 1;
 	int STATUS_LOCKED = 2;
 	int STATUS_DELETED = 3;
 
     String SPACE = " ";
     String STR_HYPHEN = "-";
 	String REM_DELIMETER = "REM";
     String PROJECT_FOLDER = "project";
     String DEFAULT_ENVIRONMENT = "Production";
     String FEATURE_NAME = "featureName";
     String OSNAME  = "os.name";
     String WINDOWS = "Windows";
     String DOT_JSON = ".json";
     String FOLDER_JSON = "json";
     String KEY_ECLIPSE = "eclipse";
     String MACOSX = "__MACOSX";
 
     String PHRESCO_HOME = "PHRESCO_HOME";
     String USER_HOME = "user.home";
     String JAVA_TMP_DIR = "java.io.tmpdir";
     String PROJECTS_HOME = "projects";
     String PROJECTS_WORKSPACE = "workspace";
     String PROJECTS_TEMP = "temp";
     String ARCHIVE_HOME = "workspace/archive";
     String PROJECT_INFO_FILE = "project.info";
     String DASHBOARD_INFO_FILE = "dashboard.info";
 	String DASHBOARD_CONFIG_INFO_FILE = "dashboardconfig.info";
 	String SPLUNK_DATATYPE="Splunk";
     String DASHBOARD_WIGET_CONFIG_INFO_FILE = "dashboardwidgetconfig.info";
     String DASHBOARD_RESULT_OUTPUT_MODE = "output_mode";
     String DASHBOARD_RESULT_EARLIEST_TIME = "earliest_time";
     String DASHBOARD_RESULT_LATEST_TIME = "latest_time";
     String PROJECT_INFO_BACKUP_FILE = "projectinfo.backup";
     String TOOLS_DIR = "tools";
     String JENKINS_DIR = "jenkins";
     String PLUGIN_DIR = "plugins";
     String TEMPLATE_DIR = "template";
     String JENKINS_HOME = "JENKINS_HOME";
     String TYPE_TOMCAT	= "Apache Tomcat";
 	String TYPE_JBOSS = "JBoss";
 	String TYPE_WEBLOGIC ="WebLogic";
 	String PHRESCO = "phresco";
 	String POM = "pom";
 	String CONFIGURATION_INFO_FILE = "phresco-env-config.xml";
 	String CONFIGURATION_FILE = "settings.xml";
 	String DOT_PHRESCO_FOLDER = ".phresco";
 	String DOT_MARKER		= ".marker";
 	String SETTINGS_XML = "-settings.xml";
 	String PHRESCO_HYPEN = "phresco-";	
 	String CI_HYPHEN = "ci-";
 	String INFO_XML = "-info.xml";
 	String NAME = "name";
 	String ENVIRONMENTS = "environments";
 	String ECLIPSE_HOME = "eclipseHome";
 	String PHRESCO_WORKSPACE = "PHRESCO_WORKSPACE";
 	/*String FILE_SEPARATOR = "/";
 	String BUILD_INFO_FILE_NAME = "build.info";
 	String DO_NOT_CHECKIN_DIR = "do_not_checkin";
 	String BUILD_DIR = DO_NOT_CHECKIN_DIR + File.separator + "build";*/    
     // Constants for Maven
     String MVN_COMMAND = "mvn";
     String MVN_ARCHETYPE = "archetype";
     String MVN_GOAL_GENERATE = "generate";
     String MVN_GOAL_PACKAGE = "package";
     String MVN_GOAL_DEPLOY = "deploy";
     String MVN_GOAL_ECLIPSE = "eclipse:eclipse";
     /*String MVN_PLUGIN_PHP_ID = "php:";
     String MVN_PLUGIN_DRUPAL_ID = "drupal:";
     String MVN_PLUGIN_ANDROID_ID = "android:";
     String MVN_PLUGIN_JAVA_ID = "java:";
     String MVN_PLUGIN_NODEJS_ID = "nodejs:";
     String MVN_PLUGIN_SHAREPOINT_ID = "sharepoint:";
     String MVN_PLUGIN_DOTNET_ID = "dotnet:";
     String MVN_PLUGIN_SITECORE_ID ="sitecore:";
     String MVN_PLUGIN_IPHONE_ID = "xcode:";
     String MVN_PLUGIN_WORDPRESS_ID = "wordpress:";
     String MVN_PLUGIN_WINDOWS_PHONE_ID = "windows-phone:";
     String MVN_PLUGIN_BLACKBERRY_ID = "blackberry:"*/
 	String MVN_PLUGIN_PHRESCO_ID = "phresco:";
 	String NODE_CONFIG_JSON = "nodeconfig.json";
 	String HUB_CONFIG_JSON = "hubconfig.json";
 	String CONFIG_JSON	= "config.json";
 	String COMPONENTS   = "components";
 	String NODE_LOG = "node.log";
 	String HUB_LOG = "hub.log";
 	String POM_NAME = "pom.xml";
 	String DO_NOT_CHECKIN_DIRY = "do_not_checkin";
 	String MARKERS_DIR		= "markers";
 	String LOG_DIRECTORY = "log";
     //Constants for Authentication Token
     String AUTH_TOKEN = "auth_token";
     String READ	= "read";
     String WRITE = "write";
     
     // Constants for Info File
     String MOJO_KEY_ENVIRONMENT_NAME = "environmentName";
     String MOJO_KEY_THEME = "theme";
     String MOJO_KEY_DEFAULT_THEME = "defaultTheme";
     String MOJO_KEY_THEMES = "themes";
     
     // Constants for Server
     String SETTINGS_TEMPLATE_SERVER = "Server";
     String SETTINGS_TEMPLATE_BROWSER = "Browser";
     String SERVER_PROTOCOL = "protocol";
     String SERVER_HOST = "host";
     String SERVER_PORT = "port";
     String SERVER_ADMIN_USERNAME = "admin_username";
     String SERVER_ADMIN_PASSWORD = "admin_password";
     String SERVER_DEPLOY_DIR = "deploy_dir";
     String SERVER_TYPE = "type";
     String SERVER_CONTEXT = "context";
     String PERFORMANCE_CONTEXT = "performanceTestContext";
     String LOAD_CONTEXT = "loadTestContext";
     String SERVER_VERSION = "version";
 	String SERVER_REMOTE_DEPLOYMENT = "remoteDeployment";
 	String SITECORE_INST_PATH = "sitecoreInstPath";
     String SITE_NAME = "siteName";
     String APPLICATION_NAME = "applicationName";
 	String CERTIFICATE = "certificate";
 	String SLING_URL = "slingUrl";
 	String USER = "user";
 	String PASSWORD = "password";
     
 
     // Constants for Database
     String DB_PROTOCOL="http";
     String SETTINGS_TEMPLATE_DB = "Database";
     String DB_NAME = "dbname";
     String DB_HOST = "host";
     String DB_PORT = "port";
     String DB_USERNAME = "username";
     String DB_PASSWORD = "password";
     String DB_TYPE = "type";
     String DB_DRIVER = "driver";
     String DB_TABLE_PREFIX = "table_prefix";
     String DB_VERSION = "version";
 	String MONGO_DB = "mongodb";
     String ORACLE_DB = "oracle";
     String MYSQL_DB = "mysql";
     String HSQL_DB = "hsql";
     String DB2_DB = "db2";
     String MSSQL_DB = "mssql";
 
     // Constants for Email
     String SETTINGS_TEMPLATE_EMAIL = "Email";
     String EMAIL_HOST = "host";
     String EMAIL_PORT = "port";
     String EMAIL_USER = "username";
     String EMAIL_PASSWORD = "password";
 
     // Constants for Web Service
     String SETTINGS_TEMPLATE_WEBSERVICE = "WebService";
     String WEB_SERVICE_PROTOCOL = "protocol";
     String WEB_SERVICE_HOST = "host";
     String WEB_SERVICE_PORT = "port";
     String WEB_SERVICE_CONTEXT = "context";
     String WEB_SERVICE_CONFIG_URL="configUrl";
     String WEB_SERVICE_USERNAME = "username";
     String WEB_SERVICE_PASSWORD = "password";
 
     String BROWSER_NAME = "browser.name";
     
     //Common contants
     String STR_MINUSD = "-D";
     String STR_COLON = ":";
     String STR_EQUAL = "=";
     String STR_DOUBLE_QUOTES = "\"";
     String SLASH = "/";
     String KILLPROCESS_BUILD = "build";
 
     //Action constants
     String TEST_FUNCTIONAL = "Functional";
 	String SITE_SQL = "site.sql";
 	String JSON_PATH = "/.phresco/sqlfile.json";
 	String DB_MYSQL   = "mysql";
 	/*
 	 * Constants for String
 	 */
 	String STR_BLANK_SPACE = " ";
 	String STR_EQUALS = "=";
 	String STR_COMMA = ",";
 	String CMD_ARG_VAR = "-D";
 	String STR_UNDER_SCORE="_";
 	/*
 	 *  Constants for System properties
 	 */
 	String USER_HOME_DIR = "user.home";
 	String OS_NAME = "os.name";
 	
 	/*
 	 * Constants for Rest
 	 */
 	String PHR_AUTH_TOKEN = "X-phr-auth-token";
 	String ARTIFACT_COUNT_RESULT = "X-Result-Count";
 	
 	//constants for server version
 	
 	String WEBLOGIC_12c = "12c(12.1.1)";
 	String WEBLOGIC_11gR1 = "11gR1(10.3.6)";
 	String WEBLOGIC_12c_PLUGIN_VERSION = "12.1.1.0";
 	String WEBLOGIC_11gr1c_PLUGIN_VERSION = "10.3.6.0";
 	
 	/*
 	 * Constants for Framework Options
 	 */
 	
 	String ENABLE_CODE_VALIDATION = "lbl.framework.options.code.validation";
 	String ENABLE_BUILD = "lbl.framework.options.build";
 	String ENABLE_DEPLOY = "lbl.framework.options.deploy";
 	String ENABLE_RUN_AGAINST_SOURCE = "lbl.framework.options.run.against.source";
 	String ENABLE_UNIT_TEST = "lbl.framework.options.unit.test";
 	String ENABLE_FUNCTIONAL_TEST = "lbl.framework.options.functional.test";
 	String ENABLE_PERFORMANCE_TEST = "lbl.framework.options.performance.test";
 	String ENABLE_LOAD_TEST = "lbl.framework.options.load.test";
 	String ENABLE_CONTINUOUS_INTEGRATION = "lbl.framework.options.continous.integration";
 	String ENABLE_JS_LIBRARIES = "lbl.framework.options.js.libraries";
 	
 	/**
 	 * Test and test report directory constants
 	 */
 	String POM_PROP_KEY_SOURCE_DIR = "phresco.source.dir";
 	String POM_PROP_KEY_UNITTEST_DIR = "phresco.unitTest.dir";
 	
 	String POM_PROP_KEY_UNITTEST_RPT_DIR = "phresco.unitTest.report.dir";
 	String POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH = "phresco.unitTest.testsuite.xpath";
 	String POM_PROP_KEY_UNITTEST_TESTCASE_PATH = "phresco.unitTest.testcase.path";
 	
 	//component test report constants
 	String POM_PROP_KEY_COMPONENTTEST_DIR = "phresco.componentTest.dir";
 	String POM_PROP_KEY_COMPONENTTEST_RPT_DIR = "phresco.componentTest.report.dir";
 	String POM_PROP_KEY_COMPONENTTEST_ADAPT_CONFIG = "phresco.componentTest.adapt.config";
 	String POM_PROP_KEY_COMPONENTTEST_TESTSUITE_XPATH = "phresco.componentTest.testsuite.xpath";
 	String POM_PROP_KEY_COMPONENTTEST_TESTCASE_PATH = "phresco.componentTest.testcase.path";
 	String SOURCE ="source.dir";
 	// Unit test report dir starting constant
 	String POM_PROP_KEY_UNITTEST_RPT_DIR_START = "phresco.unitTest.";
 	String POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_START = "phresco.unitTest.";
 	String POM_PROP_KEY_UNITTEST_TESTCASE_PATH_START = "phresco.unitTest.";
 	
 	// unit test report ending constant
 	String POM_PROP_KEY_UNITTEST_RPT_DIR_END = ".report.dir";
 	String POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END = ".testsuite.xpath";
 	String POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END = ".testcase.path";
 	
 	// Integration test report constants
 	String POM_PROP_KEY_INTEGRATIONTEST_TESTCASE_PATH = "phresco.IntegrationTest.testcase.path";
 	String POM_PROP_KEY_INTEGRATIONTEST_TESTSUITE_XPATH = "phresco.IntegrationTest.testsuite.xpath";
 	String POM_PROP_KEY_INTGRATIONTEST_RPT_DIR = "phresco.IntegrationTest.report.dir";
 	
 	//manual test report constants
 	String POM_PROP_KEY_MANUALTEST_RPT_DIR = "phresco.manualTest.report.dir";
 	
 	String POM_PROP_KEY_LOADTEST_DIR = "phresco.loadTest.dir";
 	String POM_PROP_KEY_LOADTEST_RPT_DIR = "phresco.loadTest.report.dir";
 	String POM_PROP_KEY_LOADTEST_JMX_UPLOAD_DIR = "phresco.loadTest.jmx.upload.dir";
 	String POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL = "phresco.functionalTest.selenium.tool";
     String POM_PROP_KEY_FUNCTEST_RPT_DIR = "phresco.functionalTest.report.dir";
     String POM_PROP_KEY_SCREENSHOT_DIR = "phresco.screenShot.dir";
     String POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH = "phresco.functionalTest.testsuite.xpath";
     String POM_PROP_KEY_FUNCTEST_TESTCASE_PATH = "phresco.functionalTest.testcase.path";
 	String POM_PROP_KEY_FUNCTEST_DIR = "phresco.functionalTest.dir";
 	String PHRESCO_CODE_VALIDATE_REPORT = "phresco.code.validate.report";
 	String POM_PROP_KEY_PERFORMANCETEST_DIR = "phresco.performanceTest.dir";
 	String POM_PROP_KEY_PERFORMANCETEST_JMX_UPLOAD_DIR = "phresco.performanceTest.jmx.upload.dir";
 	String POM_PROP_KEY_PERFORMANCETEST_RESULT_EXTENSION = "phresco.performanceTest.result.extension";
 	String POM_PROP_KEY_LOADTEST_RESULT_EXTENSION = "phresco.loadTest.result.extension";
 	String POM_PROP_KEY_PERF_SHOW_DEVICE = "phresco.performance.device.report";
 	String POM_PROP_KEY_PERFORMANCETEST_RPT_DIR = "phresco.performanceTest.report.dir";
 	String PHRESCO_FUNCTIONAL_TEST_ADAPT_DIR = "phresco.functionalTest.adapt.config";
 	String POM_PROP_KEY_SQL_FILE_DIR = "phresco.sql.path";
 	String POM_PROP_KEY_MODULE_SOURCE_DIR = "phresco.module.source.dir";
 	String POM_PROP_KEY_JSLIBS_SOURCE_DIR = "phresco.jslibs.source.dir";
 	String POM_PROP_KEY_COMPONENTS_SOURCE_DIR = "phresco.components.source.dir";
 	String POM_PROP_KEY_VALIDATE_REPORT = "phresco.code.validate.report";
 	String POM_PROP_KEY_JAVA_STAND_ALONE_JAR_PATH = "Javastandalone.jarPath";
 	String POM_PROP_KEY_EMBED_APP_TARGET_DIR = "phresco.embed.app.target.dir";
 	String POM_PROP_KEY_LOG_FILE_PATH = "phresco.functional.logfile.path";
 	String POM_PROP_KEY_PHRESCO_ECLIPSE = "phresco.eclipse";
 	String POM_PROP_KEY_THEME_EXT = "phresco.theme.file.extension";
 	String PHRESCO_UNIT_TEST = "phresco.unitTest";
 	String POM_PROP_KEY_CONFIG_JSON_PATH = "phresco.config.json.path";
 	/* theme builder path*/
 	String POM_PROP_KEY_THEME_BUILDER = "phresco.theme.builder.path";
 	String POM_PROP_KEY_THEME_BROWSE_BUILDER = "phresco.theme.builder.browse.path";
 	String POM_PROP_KEY_THEME_BUILDER_IMAGE = "phresco.theme.builder.image.path";
 	String POM_PROP_KEY_THEME_BUNDLE_UPLOAD_DIR = "phresco.theme.bundle.upload.dir";
 	
 	/**
 	 * MVN Goal Constants
 	 */
 	String PHASE_PACKAGE = "package";
     String PHASE_DEPLOY = "deploy";
     String PHASE_VALIDATE_CODE = "validate-code";
     String PHASE_UNIT_TEST = "unit-test";
     String PHASE_COMPONENT_TEST = "component-test";
     String PHASE_RELEASE = "release";
     String PHASE_FUNCTIONAL_TEST = "functional-test";
     String PHASE_PERFORMANCE_TEST = "performance-test";
     String PHASE_LOAD_TEST = "load-test";
     String PHASE_INTEGRATION_TEST = "integration-test";
     String PHASE_START_HUB = "start-hub";
     String PHASE_START_NODE = "start-node";
     String PHASE_STOP_HUB = "stop-hub";
     String PHASE_STOP_NODE = "stop-node";
     String PHASE_RUNGAINST_SRC_START = "start";
     String PHASE_RUNGAINST_SRC_STOP = "stop";
     String PHASE_PDF_REPORT = "pdf-report";
     String PHASE_CI = "ci";
     String PHASE_PROCESS_BUILD = "process-build";
     String PHASE_THEME_CONVERTOR = "theme-convertor";
     String PHASE_THEME_VALIDATOR = "theme-validator";
     String PHASE_CONTENT_VALIDATOR = "content-validator";
     String PHASE_CONTENT_CONVERTOR = "content-convertor";
     String PHASE_RUNAGAINST_SOURCE = "run-against-source";
     String GLOBAL = "global";
     String LOCAL = "local";
     String CREATIONTYPE = " -DcreationType=";
     String ID = " -Did=";
     String CONTINUOUSNAME = " -DcontinuousDeliveryName=";
     String HYPHEN_DEV_VERSION = " -DdevelopmentVersion=";
     String HYPHEN_REL_VERSION = " -DreleaseVersion=";
     String HYPHEN_TAG = " -Dtag=";
     String HYPHEN_USERNAME = " -Dusername=";
     String HYPHEN_PASSWORD = " -Dpassword=";
     String HYPHEN_MESSAGE = " -Dmessage=";
     String HYPHEN_JOBNAME = " -DjobName=";
     String HYPHEN_PHASE = " -Dphase=";
     String HYPHEN_GOAL = " -Dgoal=";
     String HYPHEN_F_SPACE = " -f ";
     
     String JAVA_UNIX_PROCESS_KILL_CMD	= "kill -9 ";
 	 String WINDOWS_PLATFORM			= "Windows";
     /* Drupal Version */
 	String DRUPAL_VERSION = "drupal.version";
 	String WORDPRESS_VERSION = "wordpress.version";
 	String SONAR_EXCLUSION = "sonar.exclusions";
 	String COMMA = ",";
 	String SONAR_PHPPDEPEND_ARGUMENTLINE = "sonar.phpDepend.argumentLine";
 	String SONAR_PHPPMD_ARGUMENTLINE  = "sonar.phpPmd.argumentLine";
 	String IGNORE = "--ignore=";
 	String EXCLUDE = "--exclude ";
 	
 	/**
      *  Constants for Windows
      */
     String SOURCE_DIR 	= "source";
     String PROJECT_ROOT = "Metro.UI";
     String CSPROJ_FILE 	= ".csproj";
     String PROJECT 		= "Project";
     String ITEMGROUP 	= "ItemGroup";
     String REFERENCE 	= "Reference";
     String SRC_DIR		= "src";
     String INCLUDE = "Include";
     String DLL = ".dll";
     String DOUBLE_DOT  = "..";
     String HINTPATH = "HintPath";
     String COMMON = "\\Common";
     String METRO_BUILD_SEPARATOR = "~";
     
 
     /**
      * 
      *  Phresco-plugin-info files Directory constants
      */
     String PACKAGE_INFO_FILE			= ".phresco/phresco-package-info.xml";
     String DEPLOY_INFO_FILE				= ".phresco/phresco-deploy-info.xml";
     String VALIDATE_CODE_INFO_FILE		= ".phresco/phresco-validate-code-info.xml";
     String UNIT_TEST_INFO_FILE			= ".phresco/phresco-unit-test-info.xml";
     String COMPONENT_TEST_INFO_FILE		= ".phresco/phresco-component-test-info.xml";
     String FUNCTIONAL_TEST_INFO_FILE	= ".phresco/phresco-functional-test-info.xml";
     String PERFORMANCE_TEST_INFO_FILE	= ".phresco/phresco-performance-test-info.xml";
     String LOAD_TEST_INFO_FILE			= ".phresco/phresco-load-test-info.xml";
     String CI_INFO_FILE					= ".phresco/phresco-ci-info.xml";
     String PDF_REPORT_INFO_FILE 		= ".phresco/phresco-pdf-report-info.xml";
     String REPORT_INFO_FILE 			= ".phresco/phresco-report-info.xml";
     String START_INFO_FILE			    = ".phresco/phresco-run-against-source-info.xml";
     String STOP_INFO_FILE 				= ".phresco/phresco-run-against-source-info.xml";
     String START_HUB_INFO_FILE 			= ".phresco/phresco-start-hub-info.xml";
     String START_NODE_INFO_FILE 		= ".phresco/phresco-start-node-info.xml";
     String SONAR_INFO_FILE 				= ".phresco/phresco-sonar-info.xml";
     String INTEGRATION_TEST_INFO_FILE	= ".phresco/phresco-integration-test-info.xml";
     String APPLICATION_HANDLER_INFO_FILE= "phresco-application-handler-info.xml";
     String PROCESS_BUILD_INFO_FILE 		= "phresco-process-build-info.xml";
     String THEME_INFO_FILE				= "phresco-theme-info.xml";
     String CONTENT_INFO_FILE 			= "phresco-content-info.xml";
     String CI_INFO 						= "ciJob.info";
     String CI_GLOBAL_INFO 				= "global-ciJob.info";
     String PACKAGE_INFO_XML		    	= "phresco-package-info.xml";
     
 	/**
 	 * Feature Write in pom Constants
 	 */
     
     String GROUP_ID = "groupId" ;
     String ARTIFACT_ID = "artifactId";
     String VERSION = "version";
     String TYPE = "type";
     String ZIP = "zip";
     String OVER_WRITE = "overWrite";
     String OUTPUT_DIR = "outputDirectory";
     String OVER_WIRTE_VALUE = "false";
     String DEPENDENCY_PLUGIN_GROUPID = "org.apache.maven.plugins";
     String DEPENDENCY_PLUGIN_ARTIFACTID = "maven-dependency-plugin";
     String EXECUTION_ID = "unpack-module";
     String PHASE = "validate";
     String GOAL = "unpack";
     String HYPHEN_F = "-f";
     
     String MOJO_ERROR_MESSAGE = "Please resolve above Errors...";
     
     /**
 	 * Theme builder Constants
 	 */
     String THEME_TYPE = "type";
     String THEME_PROPERTY = "property";
     String THEME_SELECTOR = "selector";
     String THEME_VALUE = "value";
     String THEME_IMAGE = "image";
     String THEME_CSS = "css";
     String THEME_NAME = "themeName";
     String THEME_PATH = "themePath"; 
     String THEME_PROPERTIES = "properties";
     String SRC_MAIN_WEBAPP= "src/main/webapp/";
     String DOT_CSS = ".css";
     String DOT_BUNDLE = ".bundle";
     String THEME_BUNDLE_SUCCESS_MSG = "Theme bundle uploaded successfully";
     String THEME_BUNDLE_FAILURE_MSG = "Theme bundle upload failed";
     String THEME_BUNDLE_FAILURE_DESTINATION = "Theme bundle upload failed. Destination missing";
     String THEME_BUNDLE_INVALID_MSG = "Uploaded zip doesnot contain .bundle file";
     String THEME_BUNDLE_UNZIP_ERR = "Unable to extract the zip";
     
     /**
 	 * Dashboard Info file Constants
 	 */
     String DASHBOARD_DATA_TYPE = "datatype";
     String DASHBOARD_USER_NAME = "username";
     String DASHBOARD_PASSWORD = "password";
     String DASHBOARD_URL = "url";
     
     /**
 	 * Build Version Constants
 	 */
     
 	String SUFFIX_PHRESCO = "-phresco";
 	String SUFFIX_TEST = "-test";
 	String POM_PROP_KEY_ROOT_SRC_DIR = "phresco.src.root.dir";
 	String POM_PROP_KEY_SRC_DIR = "phresco.src.dir";
 	String POM_PROP_KEY_TEST_DIR = "phresco.test.dir";
 	String POM_PROP_KEY_SPLIT_PHRESCO_DIR = "phresco.split.phresco.dir";
 	String POM_PROP_KEY_SPLIT_SRC_DIR = "phresco.split.src.dir";
 	String POM_PROP_KEY_SPLIT_TEST_DIR = "phresco.split.test.dir";
 	String POM_PROP_KEY_SRC_REPO_URL = "phresco.src.repo.url";
 	String POM_PROP_KEY_PHRESCO_REPO_URL = "phresco.dotphresco.repo.url";
 	String POM_PROP_KEY_TEST_REPO_URL = "phresco.test.repo.url";
 	String RELEASE_PLUGIN = "org.apache.maven.plugins:maven-release-plugin:2.4";
	String POM_PROP_KEY_PREV_BUILD_NO = "phresco.previous.build.number";
	String CURRENT_VERSION = "currentVersion";
	String TAG_VERSION = "tagVersion";
	String DEV_VERSION = "devVersion";
 	
 	/**
 	 * SCM Constants
 	 */
 	String SCM   					= "scm";
 	String SCM_CHECKOUT   			= "checkout";
 	String SCM_HYPHEN_D   			= "-D";
 	String SCM_CONNECTION_URL   	= "connectionUrl";
 	String SCM_CHECKOUT_DIRECTORY   = "checkoutDirectory";
 	String SCM_VERSION_TYPE         = "scmVersionType";
 	String SCM_BRANCH  				= "branch";
 	String SCM_VERSION				= "scmVersion";
 	String SCM_RELEASE				= "release";
 	String SCM_BRANCH_NAME			= "branchName";
 	String SCM_USERNAME				= "username";
 	String SCM_PASSWORD				= "password";
 	String SCM_UPDATE_BRANCH_VERSIONS= "updateBranchVersions";
 	String SCM_UPDATE_WORKING_COPY_VERSIONS = "updateWorkingCopyVersions";
 	String SCM_RELEASE_VERSION		= "releaseVersion";
 	String SCM_PREPARE				= "prepare";
 	String SCM_TAG					= "tag";
 	String SCM_COMMENT_PREFIX		= "scmCommentPrefix";
 	String SCM_INCLUDES				= "includes";
 	String SCM_POM_FILE_NAME		= "pomFileName";
 	String SCM_GIT					= "git";
 	String SCM_GIT_CLONE			= "clone";
 	String SCM_HYPHEN_N			    = "-n";
 	String HYPHEN_DEPTH_ONE			= "--depth 1";
 	String HYPHEN_DEPTH				= "--depth";
 	String HYPHEN_B					="-b";
 	String DOUBLE_AMPERSAND			= "&&";
 	String CHANGE_DIRECTORY			= "cd";
 	String SCM_SVN					= "svn";
 	String EMPTY					= "empty";
 	String SVN_UPDATE			= "update";
 }
