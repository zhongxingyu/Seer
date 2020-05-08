 /*
  * ###
  * Phresco Commons
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.plugin.commons;
 
 public interface PluginConstants {
 	
 	//Plugin Common Constants
 	 String DOT_PHRESCO_FOLDER 			= ".phresco";
 	 String PROJECT_FOLDER 				= "source";
 	 String DO_NOT_CHECKIN_FOLDER  		= "/do_not_checkin";
 	 String BUILD_DIRECTORY				= DO_NOT_CHECKIN_FOLDER + "/build";
 	 String BUILD_INFO_FILE 			= "/build.info";
      String TEMP_DIR		 			= "/temp";
      String PROJECT_CODE	 			= "PHR";
      String DEPLOY_LOCATION				= "com.phresco.server.deploy.location";
      String SUCCESS		 				= "SUCCESS";
      String FAILURE		 				= "FAILURE";
      String STR_SPACE 					= " ";
      String STR_UNDERSCORE				= "_";
      String PROJECT_INFO_FILE			= "project.info";
      String PLUGIN_INFO_FILE			= "phresco-plugin-info.xml";
      String FORWARD_SLASH               = "/";
      String CONFIG_FILE					= "phresco-env-config.xml";
      String SETTINGS_FILE				= "settings.xml";
      String BASH 						= "bash";
      String ENV_FILE					= "runagainstsource.info";
      String POM_PROP_KEY_SOURCE_DIR     = "phresco.source.resource.dir";
      String BUILD_XML_FILE				= "build.xml";
      String LOAD_TEST_CONFIG_FILE		= "phresco-env-config.csv";
      String HYPHEN_D					= "-D";
      String EQUAL						= "=";
      String TRUE 						= "true";
      String PDF 						= "pdf";
      String XML 						= "xml";
      String POM_XML 					= "pom.xml";
      String STATIC_ANALYSIS_REPORT 		= "static-analysis-report";
      String INDEX_HTML 					= "index.html";
      String ARCHIVES 					= "archives";
      String DOT 						= ".";
      String COLON 						= ":";
      String JAVA 						= "java";
      String WEB 						= "web";
      String JS 							= "js";
      String SONAR_LANGUAGE 				= "sonar.language";
      String CI_INFO_FILE				= "cijob.info";
      String WAR_CONFIG_FILE				= "war-config.xml";
      
 	//Constants for Maven commands
      String MVN_CMD 					= "mvn";
      String MVN_PHASE_VALIDATE			= "validate";
      String MVN_PHASE_INITIALIZE		= "initialize";
      String MVN_PHASE_CLEAN				= "clean";
      String MVN_PHASE_PACKAGE			= "package";
 	 String MVN_PHASE_VERIFY			= "verify";
 	 String MVN_PHASE_VALDATE			= "validate";
 	 String MVN_PHASE_INSTALL			= "install";
 	 String JAVA_LIB_PATH				= "java.library.path";
 	 String MVN_PHASE_CODE_VALDATE		= "codevalidate";
 	 String MVN_PHASE_XCODE_PACKAGE		= "xcodebuild";
 	 String MVN_PHASE_DEPLOY			= "deploy";
     
 	// Constants for Php maven plugin
 	 
      String PHP_TEST_CONFIG_FILE 		= "/test/functional/src/test/php/phresco/tests/" + CONFIG_FILE;
      String PHP_SOURCE_CONFIG_FILE 		= "/source/config/" + CONFIG_FILE;
 	 String PHP_SQL_DIR				    = "/source/sql/";
 	 String PHP_SQL_FILE				= "/site.sql";
     
     //Constants for Drupal Plugin
      String DRUPAL_TEST_CONFIG_FILE 	= "/test/functional/src/test/php/phresco/tests/" + CONFIG_FILE;
      String DRUPAL_SOURCE_CONFIG_FILE	= "/source/sites/default/config/" + CONFIG_FILE;
      String DRUPAL_SQL_DIR				= "/source/sql/";
 	 String DRUPAL_SQL_FILE				= "/site.sql";
      String BINARIES_DIR				=  DO_NOT_CHECKIN_FOLDER+"/binaries";
     
     //Constants for Java Plugin
      String MVN_HOME 					= "maven.home";
      String MVN_EXE_PATH				= "\\bin\\mvn.bat";
      String SKIP_TESTS					= "-DskipTests=true";
      String JAVA_TEST_CONFIG_FILE 		= "/test/functional/src/main/resources/" + CONFIG_FILE;
 	 String JAVA_CONFIG_FILE 			= "/src/main/resources/" + CONFIG_FILE;
 	 String JAVA_WEBAPP_CONFIG_FILE 	= "/src/main/webapp/WEB-INF/resources/" + CONFIG_FILE;
 	 String JAVA_SERVER_CONFIG_FILE		= "/serverconfig.properties";
 	 String JAVA_CMD					= "java";
 	 String JAR_PATH					= "/lib/driver/";
 	 String JAVA_JAR_CMD				= "-jar";
 	 String JAVA_DO_NOT_CHECKIN_FOLDER  = "/do_not_checkin";
 	 String JAVA_LOG_FILE_DIRECTORY		= "/log";
 	 String TYPE_TOMCAT					= "Apache Tomcat";
 	 String TYPE_JBOSS					= "JBoss";
 	 String TYPE_WEBLOGIC 				= "WebLogic";
 	 String PROP_SERVER_PORT			= "server.port";
 	 String PROP_SERVER_SHUTDOWN_PORT	= "server.shutdown.port";
 	 String JAVA_LOG_FILE				= "/javaLog.log";
 	 String JAVA_SQL_DIR				= "/src/sql/";
 	 String JAVA_SQL_FILE				= "/site.sql";
 	 String JAVA_POM_ARCHIVE			= "archive";
 	 String JAVA_POM_MANIFEST			= "manifest";
 	 String JAVA_POM_MAINCLASS			= "mainClass";
 	 String JAVA_POM_ADD_PATH 			= "addClasspath";
 	 String JAR_PLUGIN_GROUPID			="org.apache.maven.plugins";
 	 String JAR_PLUGIN_ARTIFACT_ID		= "maven-jar-plugin";
 	 String TOMCAT_GOAL					= "org.codehaus.mojo:tomcat-maven-plugin:1.1:redeploy";
 	 String WEBLOGIC_GOAL 				= "com.oracle.weblogic:weblogic-maven-plugin:";
 	 String WEBLOGIC_REDEPLOY 			= ":redeploy";
 	 String JBOSS_GOAL					= "cargo:redeploy";
 	 //String T7_START_GOAL				= "t7:run-forked";
 	 //String T7_STOP_GOAL				= "t7:stop-forked";
 	 String SERVER_PORT					= "-Dserver.port=";
 	 String SERVER_HOST					= "-Dserver.host=";
 	 String SERVER_USERNAME				= "-Dserver.username=";
 	 String SERVER_PASSWORD				= "-Dserver.password=";
 	 String SERVER_SHUTDOWN_PORT		= "-Dserver.shutdown.port=";
 	 String JAVA_TOMCAT_RUN             = "tomcat:run";
 	 String JAVA_UNIX_PROCESS_KILL_CMD	= "kill -9 ";
 	 String PACKAGING_TYPE_JAR			= "jar";
 	 String JAR_NAME					= "jarName";
 	 String MAIN_CLASS_NAME				= "mainClassName";
 	
 	//Constants for NodeJs Plugin
 	 String NODE_EXE_PATH				= "\\node.exe";
 	 String NODEJS_DIR_NAME				= "NodeJS";
 	 String NODE_CONFIG_FILE 			= "/source/public/resources/" + CONFIG_FILE;
 	 String NODE_CMD		 			= "node";
 	 String NODE_SERVER_FILE			= "server.js";
 	 String NODE_SQL_DIR				= "/source/sql/";
 	 String NODE_SQL_FILE 				= "/site.sql";
 	
 	//Constants for SharePoint Plugin
 	 String STSADM_PATH					= "\\Program Files\\Common Files\\Microsoft Shared\\Web Server Extensions\\12\\BIN\\STSADM.EXE";
 	 String SP_DIR_NAME					= "system32";
 	 String SHAREPOINT_STSADM 			= "stsadm";
 	 String SHAREPOINT_RESTORE 			= " restore";
 	 String SHAREPOINT_ADDSOLUTION 		= "addsolution";
 	 String SHAREPOINT_DEPLOYSOLUTION 	= "deploysolution";
 	 String SHAREPOINT_WSP_CONFIG_FILE 	= "/WSPBuilder.exe.config";
 	 String SHAREPOINT_SOLUTION_PATH	= "SolutionPath";
 	 String SHAREPOINT_OUTPUT_PATH 		= "OutputPath";
 	 String SHAREPOINT_WSPNAME 			= "WSPName";
 	 String SHAREPOINT_APPSETTINGS 		= "appSettings";
 	 String SHAREPOINT_KEY 				= "key";
 	 String SHAREPOINT_VALUE 			= "value";
 	 String SHAREPOINT_ADD 				= "add";
 	 String SHAREPOINT_STR_O 			= "-o";
 	 String SHAREPOINT_STR_URL 			= "url";
 	 String SHAREPOINT_STR_FILENAME 	= "filename";
 	 String SHAREPOINT_STR_NAME 		= "name";
 	 String SHAREPOINT_STR_IMMEDIATE 	= "immediate";
 	 String SHAREPOINT_STR_ALLOWACDEP 	= "allowgacdeployment";
 	 String SHAREPOINT_STR_OVERWRITE 	= "overwrite";
 	 String SHAREPOINT_STR_COLON 		= ":";
 	 String SHAREPOINT_STR_DOUBLESLASH 	= "//";
 	 String SHAREPOINT_STR_BACKSLASH 	= "/";
 	 String SHAREPOINT_STR_HYPEN 		= "-";
 	 String SHAREPOINT_STR_DOUBLEQUOTES = "\"";
 	 
 	//Constants for WordPress Plugin
 	//Constants for Drupal Plugin
 	 String WORDPRESS_SOURCE_DIR        = "/source/wordpress/";
      String WORDPRESS_TEST_CONFIG_FILE 	 = "/source/wordpress/config/" + CONFIG_FILE;
      String APACHE_DEFAULT_PORT			 = "80";	
      String WORDPRESS_UPDATE_TABLE       = "update wp_options set option_value='";
      String WORDPRESS_UPDATE_WHERE       = "' where option_name = 'siteurl'";  
      String WORDPRESS_UPDATE_HOME_WHERE  = "' where option_name = 'home'";  
      String WORDPRESS_SQL_DIR			 = "/source/sql/";
 	 String WORDPRESS_SQL_FILE			 = "/site.sql";
 	 
 	 String LOG_FILE_DIRECTORY			=  DO_NOT_CHECKIN_FOLDER + "/log";
 	 String COMPILE_LOG_FILE			= "/sourcecompile.log";
 	 String RUN_AGS_LOG_FILE			= "/runagainstsource.log";
 	 
 	 // Constants for Windows Phone Plugin
 	 // Author: Viral
 	 // Date: July 27, 2012
 	 String WP_MSBUILD_PATH				= "MSBuild ";
 	 String WP_STR_TARGET				= "/t";
 	 String WP_STR_PROPERTY				= "/p";	 
 	 String WP_STR_COLON 		 		= ":";
 	 String WP_STR_SEMICOLON 		 	= ";";
 	 String WINDOWS_STR_BACKSLASH   	= "\\";
 	 String WP_STR_DOUBLEQUOTES 		= "\"";
 	 
 	 String WP_SLN 						= ".sln";
 	 String WP_CSPROJ 					= ".csproj";
 	 
 	 // Variables related to Windows 8 build process
 	 String WP_STR_CONFIGURATION		= "Configuration";
 	 String WP_STR_PLATFORM				= "Platform";
 	 String WP_APP_PACKAGE				= "\\AppPackages";
 	 String WP_TEST						= "_Test";
 	 String WP_PROJECT_ROOT 			= "Metro.UI";
 	 String WP_PROPERTYGROUP 			= "PropertyGroup";
 	 String WP_AUTO_INCREMENT_PKG_VERSION_NO = "AppxAutoIncrementPackageRevision";
 	 
 	 
 	 // Variables related to Windows 8 deploy process	 
 	 String WP_POWERSHELL_PATH			= "PowerShell ";
 	 String WP_STR_DOT					= ".";
 	 String WP_ADD_APPX_PACKAGE 		= "Add-AppxPackage ";
 	 String WP_GET_APPX_PACKAGE 		= "Get-AppxPackage ";
 	 String WP_REMOVE_APPX_PACKAGE 		= "Remove-AppxPackage ";
 	 String WP8_PLATFORM 				= "wp8";
 	 // Variables related to Windows 7 build process
 	 String WP7_BIN_FOLDER				= "\\bin";
 	 String WP7_RELEASE_FOLDER			= "\\release";
 	 
 	 // Variables related to Windows 7 deploy process
 	 // wptools.exe -target:emulator -xap:WindowsPhoneApplication1.xap -install
 	 String WP7_WPTOOLS_PATH			= "wptools.exe ";
 	 String WP7_TARGET					= "-target";
 	 String WP7_XAP_FILE				= "-xap";
 	 String WP7_INSTALL					= "-install"; 
 	 
 	 //Xcode plugin constants
 	 String WAXSIM_HOME 				= "WAXSIM_HOME";
 	 String OCUNIT2JUNIT_HOME 			= "OCUNIT2JUNIT_HOME";
 	 String XCODE_CODE_VALIDATOR_COMMAND= "mvn xcode:codevalidate";
 	 String XCODE_BUILD_COMMAND			= "mvn xcode:xcodebuild";
 	 String XCODE_DEPLOY_COMMAND		= "mvn xcode:deploy";
 	 String TARGET						= "target";
 	 String PROJECT_TYPE				= "projectType";
 	 String SCHEME						= "scheme";
 	 String SIMULATOR_VERSION			= "simulator.version";
 	 String SDK							= "sdk";
 	 String TARGET_NAME					= "targetName";
 	 String CONFIGURATION				= "configuration";
 	 String ENCRYPT						= "encrypt";
 	 String PLIST_FILE					= "plistfile";
 	 
 	 //Constants for Android
 	 
 	 String PROGUARD	 				= "proguard";
 	 String SIGNING 					= "signing";
 	 String SDK_VERSION 				= "sdk";
 	 String DEVICES						= "devices";
 	 String DEVICES_LIST				= "deviceList";
 	 String ANDROID_BUILD_COMMAND		= "mvn install";
 	 String ANDROID_DEPLOY_COMMAND		= "mvn android:deploy";
 	 String ANDROID_TEST_COMMAND 		= "mvn clean install";
 	 String SKIP_TEST					= "skipTest";
 	 String ANDROID_VERSION				= "android.version";
 	 String ANDROID_DEVICE				= "android.device";
 	 String ANDROID_EMULATOR			= "android.emulator.avd";
 	 String PROGUARD_SKIP				= "android.proguard.skip";
 	 String SERIAL_NUMBER				= "serialNumber";
 	 String DEFAULT						= "default";
 	 String PROFILE_ID					= "sign";
 	 String GOAL_INSTALL 				= "install";
 	 String GOAL_SIGN 					= "sign";
 	 String ANDROID_PROFILE_GROUP_ID 	= "org.apache.maven.plugins";
 	 String ANDROID_PROFILE_ARTIFACT_ID = "maven-jarsigner-plugin";
 	 String ANDROID_PROFILE_VERSION 	= "1.2";
 	 String PHASE_PACKAGE 				= "package";
 	 String ANDROID_EXECUTION_ID 		= "signing";
 	 String ELEMENT_ARCHIVE_DIR 		= "archiveDirectory";
 	 String ELEMENT_REMOVE_EXIST_SIGN 	= "removeExistingSignatures";
 	 String ELEMENT_INCLUDES 			= "includes";
 	 String ELEMENT_INCLUDE 			= "include";
 	 String ELEMENT_BUILD 				= "do_not_checkin/build/*.apk";
 	 String ELEMENT_TARGET 				= "do_not_checkin/target/*.apk";
 	 String ELEMENT_VERBOS 				= "verbose";
 	 String ELEMENT_VERIFY 				= "verify";
 	 String ALIAS 						= "alias";
 	 String KEYPASS 					= "keypass";
 	 String STOREPASS 					= "storepass";
 	 String KEYSTORE 					= "keystore";
 	 
 	 // Constants report plugin
 	 String ATTR_TIME 					= "time";
 	 String ATTR_TESTS 					= "tests";
 	 String ATTR_NAME 					= "name";
 	 String ATTR_FILE 					= "file";
 	 String ATTR_FAILURES 				= "failures";
 	 String ATTR_ERRORS 				= "errors";
 	 String ATTR_ASSERTIONS 			= "assertions";
 	 String ATTR_CLASS 					= "class";
 	 String ATTR_CLASSNAME 				= "classname";
 	 String ATTR_LINE 					= "line";
 	 String ATTR_TYPE 					= "type";
 	 String ATTR_RESULT 				= "result";
 	 String ATTR_JM_THREAD_NAME 		= "tn";
 	 String ATTR_JM_LABEL 				= "lb";
 	 String ATTR_JM_SUCCESS_FLAG 		= "s";
 	 String ATTR_JM_TIMESTAMP 			= "ts";
 	 String ATTR_JM_LATENCY_TIME 		= "lt";
 	 String ATTR_JM_TIME 				= "t";
 	 String ATTR_JM_BYTES	 			= "by";
 	 String ATTR_JM_RESPONSE_CODE 		= "rc";
 	 String ATTR_ID 					= "id";
 	 String FUNCTIONAL 					= "functional";
 	 String UNIT 						= "unit";
 	 String LOAD 						= "load";
 	 String PERFORMACE 					= "performance";
      String ELEMENT_FAILURE 			= "failure";
      String ELEMENT_ERROR 				= "error";
      String NAME_FILTER_PREFIX 			= "[@name='";
      String NAME_FILTER_SUFIX 			= "']";
 //	 String REPORTS_JASPER 				= "reports/jasper/";
      String XPATH_SINGLE_TESTSUITE 		= "/testsuites/testsuite";
      String XPATH_MULTIPLE_TESTSUITE 	= "/testsuites/testsuite/testsuite";
      String XPATH_TESTCASE 				= "/testcase";
      String XPATH_TEST_RESULT 			= "/testResults/*";
      String XPATH_TESTSUTE_TESTCASE 	= "/testsuite/testcase";
      String PERFORMANCE_TEST_REPORTS 	= "performanceTestReports";
      String PERFORMANCE_SPECIAL_HANDLE 	= "performanceSpecialHandle";
      String LOAD_TEST_RESULTS 			= "loadTestResults";
      String LOAD_TEST_REPORTS 			= "loadTestReports";
      String FUNCTIONAL_TEST_REPORTS 	= "functionalTestReports";
      String UNIT_TEST_REPORTS 			= "unitTestReports";
      String FUNCTIONAL_SURE_FIRE_REPORTS = "functionalSureFireReports";
      String JMETER_TEST_RESULTS_FOR_ANDROID = "jmeterTestResultsForAndroid";
      String JMETER_TEST_RESULTS 		= "jmeterTestResults";
      String DATE_TIME_FORMAT 			= "dd-MMM-yyyy HH:mm:ss";
      String REQ_TITLE_EXCEPTION 		= "Exception";
      String REQ_TITLE_ERROR 			= "Error";
      String CUMULATIVE 					= "cumulativeReports";
      String SONAR_LANGUAGE_PROFILE 		= "sonar.language";
      String SONAR_BRANCH   				= "sonar.branch";
      String SONAR_SOURCE 				= "source"; 
      String FUNCTIONALTEST 				= "functional";
      String REPORT_TYPE 				= "reportType";
      String DIR_TYPE 					= "dir_type";
      String TEST_TYPE 					= "testType";
      String UTF_8 						= "utf-8";
  	
 	 //Constants for dynamic parameter
 	 String BUILD_NAME 					= "buildName";
 	 String ENVIRONMENT_NAME 			= "environmentName";
 	 String WINDOWS_PLATFORM_TYPE		= "type";
 	 String CONFIG 						= "config";
 	 String PLATFORM 					= "platform";
 	 String DOT_ZIP 					= ".zip";
 	 String BUILD_NUMBER 				= "buildNumber";
 	 String BROWSER						= "browser";
 	 String RESOLUTION					= "resolution";
 	 String DEVICE_TYPE 				= "deviceType";
 	 String SIM_VERSION 				= "sdkVersion";
 	 String FAMILY 						= "family";
 	 String PSIGN 						= "-Psign";
 	 String TEST_AGAINST				= "testAgainst";
 	 String SERVER						= "server";
 	 String BUILD						= "build";
 	 String BUILD_ENVIRONMENT_NAME		= "buildEnvironmentName";
 	 String PACK_MINIFIED_FILES			= "packMinifiedFiles";
 	 String EXCLUDE_FILE				= "excludeFile";
 	 
 	 //Constants for Time Formatter
 	 String TIME_STAMP_FOR_BUILD_NAME 	= "dd-MMM-yyyy-HH-mm-ss";
 	 String TIME_STAMP_FOR_DISPLAY 		= "dd/MMM/yyyy HH:mm:ss";
 	 
 	 //Constants for CodeValidator
 	 String SONAR_COMMAND				= "mvn sonar:sonar";
 	 String SONAR						= "sonar";
 	 String PLUGIN_PARAMETER 			= "plugin";
 	 
 	 //Constants for Tests
 	 String TEST_COMMAND				= "mvn clean test";	 
 	 
 	// BlackBerry maven plugin constants
 	// Author: Viral
 	// Date: Sept 24, 2012
 	String BB_BBWP_HOME = "bbwp ";
 	String BB_JAVA_LOADER_HOME = "javaloader ";
 	String BB_USB = "-usb";
 	String BB_LOAD = "load";
 	String BB_STANDARD_INSTALL = "StandardInstall";
 	String BB_OTA_INSTALL = "OTAInstall";
 	
 	 //Constants for Jenkins
 	String JENKINS_HOME 			= "JENKINS_HOME";
 	String WORKSPACE_DIR 			= "workspace";
 	String PHRESCO_HYPEN 			= "phresco-";	
 	String INFO_XML 				= "-info.xml";
 	
 	//Load Test Parameter Keys
 	String KEY_LOAD					= "load";
 	String KEY_TEST_NAME			= "testName";
 	String KEY_NO_OF_USERS			= "noOfUsers";
 	String KEY_RAMP_UP_PERIOD		= "rampUpPeriod";
 	String KEY_LOOP_COUNT			= "loopCount";
 	String ADD_HEADER				= "addHeader";
 	String EXECUTE_SQL			    = "executeSql";
 	String FETCH_SQL                = "fetchSql";
 	
 	 /**
      * 
      * Functional-test selenium type
      */
     
     String FUNCTIONAL_TEST_SELENIUM_TYPE = "phresco.functionalTest.selenium.tool";
     String WEBDRIVER = "webdriver";
     String GRID = "grid";
     String HYPEN = "-";
     
     String PHASE_PACKAGE_INFO = "phresco-package-info.xml";
 	
 }
