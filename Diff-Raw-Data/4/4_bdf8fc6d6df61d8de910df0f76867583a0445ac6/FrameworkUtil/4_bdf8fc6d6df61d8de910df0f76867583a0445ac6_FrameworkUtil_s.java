 /*
  * ###
  * Framework Web Archive
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
 package com.photon.phresco.framework.commons;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.model.PerformanceDetails;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.model.Model.Profiles;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 
 public class FrameworkUtil extends FrameworkBaseAction implements Constants {
 
 	private static final long serialVersionUID = 1L;
 	private static FrameworkUtil frameworkUtil = null;
     private static final Logger S_LOGGER = Logger.getLogger(FrameworkUtil.class);
     
     private Map<String, String> unitTestMap = new HashMap<String, String>(8);
     private Map<String, String> unitReportMap = new HashMap<String, String>(8);
     private Map<String, String> funcationTestMap = new HashMap<String, String>(8);
     private Map<String, String> funcationAdaptMap = new HashMap<String, String>(8);
     private Map<String, String> funcationReportMap = new HashMap<String, String>(8);
     private Map<String, String> performanceTestMap = new HashMap<String, String>(8);
     private Map<String, String> performanceReportMap = new HashMap<String, String>(8);
     private Map<String, String> loadTestMap = new HashMap<String, String>(8);
     private Map<String, String> loadReportMap = new HashMap<String, String>(8);
     private Map<String, String> unitTestSuitePathMap = new HashMap<String, String>(8);
     private Map<String, String> functionalTestSuitePathMap = new HashMap<String, String>(8);
     private Map<String, String> testCasePathMap = new HashMap<String, String>(8);
     private Properties qualityReportsProp;
     private String fileName = "quality-report.properties";
    
     public FrameworkUtil() throws PhrescoException {
         InputStream stream = null;
         stream = this.getClass().getClassLoader().getResourceAsStream(fileName);
         qualityReportsProp = new Properties();
         try {
             qualityReportsProp.load(stream);
         } catch (IOException e) {
             throw new PhrescoException(e);
         }
         initUnitTestMap();
         initUnitReportMap();
         initFunctionalTestMap();
         initFunctionalAdaptMap();
         initFunctionalReportMap();
         initPerformanceTestMap();
         initPerformanceReportMap();
         initLoadTestMap();
         initLoadReportMap();
         initUnitTestSuitePathMap();
         initFunctionalTestSuitePathMap();
         initTestCasePathMap();
     }
     
 	public static FrameworkUtil getInstance() throws PhrescoException {
         if (frameworkUtil == null) {
             frameworkUtil = new FrameworkUtil();
         }
         return frameworkUtil;
     }
     
     private void initUnitTestMap() {
         unitTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.HTML5, PATH_HTML5_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.PHP, PATH_PHP_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_UNIT_TEST);
         unitTestMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_UNIT_TEST);
     }
 
     private void initUnitReportMap() {
         unitReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.HTML5, PATH_HTML5_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.PHP, PATH_PHP_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_UNIT_TEST_REPORT);
         unitReportMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_UNIT_TEST_REPORT);
     }
 
     private void initFunctionalTestMap() {
         funcationTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.HTML5, PATH_HTML5_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.PHP, PATH_PHP_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_HYBRID_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_FUNCTIONAL_TEST);
         funcationTestMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_TEST);
     }
     
     private void initFunctionalAdaptMap() {
         funcationAdaptMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.HTML5, PATH_HTML5_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.PHP, PATH_PHP_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_FUNCTIONAL_ADAPT);
         funcationAdaptMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_ADAPT);
     } 
 
     private void initFunctionalReportMap() {
         funcationReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.HTML5, PATH_HTML5_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.PHP, PATH_PHP_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_HYBRID_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_FUNCTIONAL_TEST_REPORT);
         funcationReportMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_FUNCTIONAL_TEST_REPORT);
     }
     
     private void initPerformanceTestMap() {
         performanceTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.HTML5, PATH_HTML5_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.PHP, PATH_PHP_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_PERFORMANCE_TEST);
         performanceTestMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_PERFORMANCE_TEST);
     }
 
     private void initPerformanceReportMap() {
         performanceReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.HTML5, PATH_HTML5_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.PHP, PATH_PHP_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_PERFORMANCE_TEST_REPORT);
         performanceReportMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_PERFORMANCE_TEST_REPORT);
     }
     
     private void initLoadTestMap() {
         loadTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.HTML5, PATH_HTML5_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.PHP, PATH_PHP_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_WEBSERVICE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_LOAD_TEST);
         loadTestMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_LOAD_TEST);
     }
 
     private void initLoadReportMap() {
         loadReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.HTML5_WIDGET, PATH_HTML5_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, PATH_HTML5_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.HTML5, PATH_HTML5_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.SHAREPOINT, PATH_SHAREPOINT_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.PHP, PATH_PHP_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.PHP_DRUPAL6, PATH_DRUPAL_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.PHP_DRUPAL7, PATH_DRUPAL_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.PHP_WEBSERVICE, PATH_PHP_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, PATH_NODEJS_WEBSERVICE_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.NODE_JS, PATH_NODEJS_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.ANDROID_HYBRID, PATH_ANDROID_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.ANDROID_NATIVE, PATH_ANDROID_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.ANDROID_WEB, PATH_ANDROID_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.IPHONE_HYBRID, PATH_IPHONE_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.IPHONE_NATIVE, PATH_IPHONE_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.IPHONE_WEB, PATH_IPHONE_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.BLACKBERRY_HYBRID, PATH_BLACKBERRY_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.JAVA_WEBSERVICE, PATH_JAVA_WEBSERVICE_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.DOT_NET, PATH_SHAREPOINT_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.WORDPRESS, PATH_DRUPAL_LOAD_TEST_REPORT);
         loadReportMap.put(TechnologyTypes.JAVA_STANDALONE, PATH_JAVA_WEBSERVICE_LOAD_TEST_REPORT);
         
     }
 
     private void initUnitTestSuitePathMap() {
     	unitTestSuitePathMap.put(TechnologyTypes.JAVA, XPATH_JAVA_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.HTML5_WIDGET, XPATH_HTML5_WIDGET_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, XPATH_HTML5_WIDGET_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.HTML5, XPATH_HTML5_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_MULTICHANNEL_JQUERY_UNIT_TEST_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_JQUERY_MOBILE_WIDGET_UNIT_TEST_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.SHAREPOINT, XPATH_SHAREPOINT_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.PHP, XPATH_PHP_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.PHP_DRUPAL6, XPATH_PHP_DRUPAL6_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.PHP_DRUPAL7, XPATH_PHP_DRUPAL7_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.PHP_WEBSERVICE, XPATH_PHP_WEBSERVICE_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, XPATH_NODE_JS_WEBSERVICE_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.NODE_JS, XPATH_NODE_JS_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.ANDROID_HYBRID, XPATH_ANDROID_HYBRID_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.ANDROID_NATIVE, XPATH_ANDROID_NATIVE_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.ANDROID_WEB, XPATH_ANDROID_WEB_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.IPHONE_HYBRID, XPATH_IPHONE_HYBRID_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.IPHONE_NATIVE, XPATH_IPHONE_NATIVE_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.IPHONE_WEB, XPATH_IPHONE_WEB_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.BLACKBERRY_HYBRID, XPATH_BLACKBERRY_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.JAVA_WEBSERVICE, XPATH_JAVA_WEBSERVICE_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.DOT_NET, XPATH_SHAREPOINT_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.WORDPRESS, XPATH_PHP_WORDPRESS_UNIT_TESTSUITE);
     	unitTestSuitePathMap.put(TechnologyTypes.JAVA_STANDALONE, XPATH_JAVA_WEBSERVICE_UNIT_TESTSUITE);
 	}
     
     private void initFunctionalTestSuitePathMap() {
     	functionalTestSuitePathMap.put(TechnologyTypes.JAVA, XPATH_JAVA_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.HTML5_WIDGET, XPATH_HTML5_WIDGET_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, XPATH_HTML5_WIDGET_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.HTML5, XPATH_HTML5_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, PATH_HTML5_MULTICHANNEL_JQUERY_FUNCTIONAL_TEST_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, PATH_HTML5_JQUERY_MOBILE_WIDGET_FUNCTIONAL_TEST_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.SHAREPOINT, XPATH_SHAREPOINT_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.PHP, XPATH_PHP_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.PHP_DRUPAL6, XPATH_PHP_DRUPAL6_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.PHP_DRUPAL7, XPATH_PHP_DRUPAL7_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.PHP_WEBSERVICE, XPATH_PHP_WEBSERVICE_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, XPATH_NODE_JS_WEBSERVICE_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.NODE_JS, XPATH_NODE_JS_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.ANDROID_HYBRID, XPATH_ANDROID_HYBRID_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.ANDROID_NATIVE, XPATH_ANDROID_NATIVE_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.ANDROID_WEB, XPATH_ANDROID_WEB_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.IPHONE_HYBRID, XPATH_IPHONE_HYBRID_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.IPHONE_NATIVE, XPATH_IPHONE_NATIVE_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.IPHONE_WEB, XPATH_IPHONE_WEB_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.BLACKBERRY_HYBRID, XPATH_BLACKBERRY_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.JAVA_WEBSERVICE, XPATH_JAVA_WEBSERVICE_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.DOT_NET, XPATH_SHAREPOINT_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.WORDPRESS, XPATH_PHP_WORDPRESS_FUNCTIONAL_TESTSUITE);
     	functionalTestSuitePathMap.put(TechnologyTypes.JAVA_STANDALONE, XPATH_JAVA_WEBSERVICE_FUNCTIONAL_TESTSUITE);
 	}
     
 	private void initTestCasePathMap() {
 		testCasePathMap.put(TechnologyTypes.JAVA, XPATH_JAVA_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.HTML5_WIDGET, XPATH_HTML5_WIDGET_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, XPATH_HTML5_WIDGET_TESTCASE);
         testCasePathMap.put(TechnologyTypes.HTML5, XPATH_HTML5_TESTCASE);
         testCasePathMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, XPATH_HTML5_MULTICHANNEL_JQUERY_TESTCASE);
         testCasePathMap.put(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET, XPATH_HTML5_JQUERY_MOBILE_WIDGET_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.SHAREPOINT, XPATH_SHAREPOINT_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.PHP, XPATH_PHP_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.PHP_DRUPAL6, XPATH_PHP_DRUPAL7_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.PHP_DRUPAL7, XPATH_PHP_DRUPAL7_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.PHP_WEBSERVICE, XPATH_PHP_WEBSERVICE_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, XPATH_NODE_JS_WEBSERVICE_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.NODE_JS, XPATH_NODE_JS_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.ANDROID_HYBRID, XPATH_ANDROID_HYBRID_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.ANDROID_NATIVE, XPATH_ANDROID_NATIVE_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.ANDROID_WEB, XPATH_ANDROID_WEB_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.IPHONE_HYBRID, XPATH_IPHONE_HYBRID_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.IPHONE_NATIVE, XPATH_IPHONE_NATIVE_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.IPHONE_WEB, XPATH_IPHONE_WEB_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.BLACKBERRY_HYBRID, XPATH_BLACKBERRY_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.JAVA_WEBSERVICE, XPATH_JAVA_WEBSERVICE_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.DOT_NET, XPATH_SHAREPOINT_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.WORDPRESS, XPATH_PHP_DRUPAL7_TESTCASE);
 		testCasePathMap.put(TechnologyTypes.JAVA_STANDALONE, XPATH_JAVA_WEBSERVICE_TESTCASE);
 	}
 	
 	public String getSqlFilePath(String oldAppDirName) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(oldAppDirName).getProperty("phresco.sql.path");
 	}
 	
     public String getUnitTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_DIR);
     }
     
     public String getUnitTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR);
     }
 
 	public String getUnitTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
     }
     
     public  String getUnitTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
     }
     
     public String getSeleniumToolType(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
     }
     
     public String getFunctionalTestDir(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_DIR);
     }
     
     public String getFunctionalTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_RPT_DIR);
     }
 
     public String getFunctionalTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
     }
     
     public  String getFunctionalTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
     }
     
     public String getLoadTestDir(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
     	return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_LOADTEST_DIR);
     }
     
     public String getLoadTestReportDir(ApplicationInfo appinfo) throws PhrescoPomException, PhrescoException {
     	return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_LOADTEST_RPT_DIR);
     }
     
     public String getPerformanceTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERFORMANCETEST_DIR);
     }
     
     public String getPerformanceTestReportDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
     }
 
 	public String getHubConfigFile(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         StringBuilder sb = new StringBuilder(Utility.getProjectHome());
         sb.append(appInfo.getAppDirName());
         sb.append(File.separator);
         sb.append(getFunctionalTestDir(appInfo));
         sb.append(File.separator);
         sb.append("hubconfig.json");
         return sb.toString();
     }
 
     public String getFuncitonalAdaptDir(String technologyId) {
         String key = funcationAdaptMap.get(technologyId);
         return qualityReportsProp.getProperty(key);
     }
     
     public static void setAppInfoDependents(HttpServletRequest request, String customerId) throws PhrescoException {
         FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
         ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
         request.setAttribute(REQ_APPLICATION_TYPES, administrator.getApplicationTypes(customerId));
         request.setAttribute(REQ_CODE_PREFIX, configuration.getCodePrefix());
     }
     
     public static String getStackTraceAsString(Exception exception) {
 	    StringWriter sw = new StringWriter();
 	    PrintWriter pw = new PrintWriter(sw);
 	    pw.print(" " + SQUARE_OPEN + " ");
 	    pw.print(exception.getClass().getName());
 	    pw.print(" " + SQUARE_CLOSE + " ");
 	    pw.print(exception.getMessage());
 	    pw.print(" ");
 	    exception.printStackTrace(pw);
 	    return sw.toString();
     }
     
     public static String removeFileExtension(String fileName) {
     	fileName = fileName.substring(0, fileName.lastIndexOf('.'));
     	return fileName;
     }
     
     public static float roundFloat(int decimal, double value) {
 		BigDecimal roundThroughPut = new BigDecimal(value);
 		return roundThroughPut.setScale(decimal, BigDecimal.ROUND_HALF_EVEN).floatValue();
 	}
     
     public static String convertToCommaDelimited(String[] list) {
         StringBuffer ret = new StringBuffer("");
         for (int i = 0; list != null && i < list.length; i++) {
             ret.append(list[i]);
             if (i < list.length - 1) {
                 ret.append(',');
             }
         }
         return ret.toString();
     }
     
     public static void copyFile(File srcFile, File dstFile) throws PhrescoException {
     	try {
     		if (!dstFile.exists()) {
     			dstFile.getParentFile().mkdirs();
     			dstFile.createNewFile();
     		}
 			FileUtils.copyFile(srcFile, dstFile);
 		} catch (Exception e) {
 			throw new PhrescoException();
 		}
     }
     
     public PomProcessor getPomProcessor(String appDirName) throws PhrescoException {
     	try {
     		StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     		builder.append(appDirName);
     		builder.append(File.separatorChar);
     		builder.append(POM_XML);
     		S_LOGGER.debug("builder.toString() " + builder.toString());
     		File pomPath = new File(builder.toString());
     		S_LOGGER.debug("file exists " + pomPath.exists());
     		return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
     }
     
     //get server Url for sonar
     public String getSonarHomeURL() throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = "";
     	
 	    if (StringUtils.isNotEmpty(frameworkConfig.getSonarUrl())) {
 	    	serverUrl = frameworkConfig.getSonarUrl();
 	    	S_LOGGER.debug("if condition serverUrl  " + serverUrl);
 	    } else {
 	    	serverUrl = getHttpRequest().getRequestURL().toString();
 	    	StringBuilder tobeRemoved = new StringBuilder();
 	    	tobeRemoved.append(getHttpRequest().getContextPath());
 	    	tobeRemoved.append(getHttpRequest().getServletPath());
 
 	    	Pattern pattern = Pattern.compile(tobeRemoved.toString());
 	    	Matcher matcher = pattern.matcher(serverUrl);
 	    	serverUrl = matcher.replaceAll("");
 	    	S_LOGGER.debug("else condition serverUrl  " + serverUrl);
 	    }
 	    return serverUrl;
     }
     
     //get server Url for sonar
     public String getSonarURL() throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = getSonarHomeURL();
     	S_LOGGER.debug("serverUrl ... " + serverUrl);
 	    String sonarReportPath = frameworkConfig.getSonarReportPath();
 	    S_LOGGER.debug("sonarReportPath ... " + sonarReportPath);
 	    String[] sonar = sonarReportPath.split("/");
 	    S_LOGGER.debug("sonar[1] " + sonar[1]);
 	    serverUrl = serverUrl.concat(FORWARD_SLASH + sonar[1]);
 	    S_LOGGER.debug("Final url => " + serverUrl);
 	    return serverUrl;
     }
     
 	public List<String> getSonarProfiles() throws PhrescoException {
 		List<String> sonarTechReports = new ArrayList<String>(6);
 		try {
 			StringBuilder pomBuilder = new StringBuilder(getApplicationHome());
 			pomBuilder.append(File.separator);
 			pomBuilder.append(POM_XML);
 			File pomPath = new File(pomBuilder.toString());
 			PomProcessor pomProcessor = new PomProcessor(pomPath);
 			Model model = pomProcessor.getModel();
 			S_LOGGER.debug("model... " + model);
 			Profiles modelProfiles = model.getProfiles();
 			if (modelProfiles == null) {
 				return sonarTechReports;
 			}
 			S_LOGGER.debug("model Profiles... " + modelProfiles);
 			List<Profile> profiles = modelProfiles.getProfile();
 			if (profiles == null) {
 				return sonarTechReports;
 			}
 			S_LOGGER.debug("profiles... " + profiles);
 			for (Profile profile : profiles) {
 				S_LOGGER.debug("profile...  " + profile);
 				if (profile.getProperties() != null) {
 					List<Element> any = profile.getProperties().getAny();
 					int size = any.size();
 					
 					for (int i = 0; i < size; ++i) {
 						boolean tagExist = 	any.get(i).getTagName().equals(SONAR_LANGUAGE);
 						if (tagExist){
 							S_LOGGER.debug("profile.getId()... " + profile.getId());
 							sonarTechReports.add(profile.getId());
 						}
 					}
 				}
 			}
 			S_LOGGER.debug("return from getSonarProfiles");
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return sonarTechReports;
 	}
 	
 	/**
 	 * To encrypt the given string
 	 * @param inputString
 	 * @return
 	 */
 	public static String encryptString(String inputString) {
         byte[] encodeBase64 = Base64.encodeBase64(inputString.getBytes());
         String encodedString = new String(encodeBase64);
 
         return encodedString;
     }
 	
 	/**
 	 * To decrypt the given string
 	 * @param inputString
 	 * @return
 	 */
 	public static String decryptString(String inputString) {
         byte[] decodedBytes = Base64.decodeBase64(inputString);
         String decodedString = new String(decodedBytes);
 
         return decodedString;
     }
 
     public static StringTemplate constructInputElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	String type = getInputType(pm.getInputType());
     	StringTemplate inputElement = new StringTemplate(getInputTemplate());
     	inputElement.setAttribute("type", type);
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("placeholder", pm.getPlaceHolder());
     	inputElement.setAttribute("value", pm.getValue());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
 		return controlGroupElement;
     }
 
 	/**
 	 * @param inputType
 	 * @return
 	 */
 	private static String getInputType(String inputType) {
 		String type = "";
 		if (TYPE_PASSWORD.equalsIgnoreCase(inputType)) {
     		type = TYPE_PASSWORD;
 		}  else if (TYPE_HIDDEN.equalsIgnoreCase(inputType)) {
 			type = TYPE_HIDDEN;
 		} else {
 			type = TEXT_BOX;
 		}
 		
 		return type;
 	}
     
 	public static StringTemplate constructMapElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	
     	StringTemplate mapElement = new StringTemplate(getMapTemplate());
     	if (pm.isMandatory()) {
     		mapElement.setAttribute("mandatory", getMandatoryTemplate());
     	}
     	
     	mapElement.setAttribute("legendHeader", pm.getLableText());
     	List<BasicParameterModel> childs = pm.getChilds();
     	for (BasicParameterModel child : childs) {
     	    StringTemplate childElement = new StringTemplate();
             if (child.getInputType().equalsIgnoreCase(TYPE_LIST)) {
                 childElement = new StringTemplate(getMapSelectElement());
                 StringBuilder options = constructOptions(child.getObjectValue(), null, null, "");
                 childElement.setAttribute("options", options);
             } else if (child.getInputType().equalsIgnoreCase(TYPE_STRING)) {
                 childElement = new StringTemplate(getMapInputElement());
             }
             childElement.setAttribute("name", child.getName());
             updateChildLabels(mapElement, child);
             mapElement.setAttribute("mapControls", childElement);
         }
     	controlGroupElement.setAttribute("lable", mapElement);
     	
 		return controlGroupElement;
     }
 	
 	public static StringTemplate constructCustomParameters() {
 	    StringTemplate controlGroupElement = new StringTemplate(getCustomParamTableTemplate());
 	    
 	    return controlGroupElement;
 	}
 	
 	private static String getCustomParamTableTemplate() {
         StringBuilder sb = new StringBuilder();
         sb.append("<table class='custParamTable'>")
         .append("<tbody id='propTempTbodyForHeader'>")
         .append("<tr class='borderForLoad'>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"key\" placeholder=\"Key\" value=\"\">")
         .append("</td>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"value\" placeholder=\"Value\" value=\"\">")
         .append("</td>")
         .append("<td class='borderForLoad noBorder'>")
         .append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>")
         .append("</tr></tbody></table>");
         
         return sb.toString();
     }
 	
     private static void updateChildLabels(StringTemplate mapElement, BasicParameterModel child) {
         String keyLabel = (String) mapElement.getAttribute("keyLabel");
         if (StringUtils.isEmpty(keyLabel)) {
             mapElement.setAttribute("keyLabel", child.getLableText());
         } else {
             mapElement.setAttribute("valueLabel", child.getLableText());
         }
         mapElement.setAttribute("childLabel", child.getLableText());
     }
 
     public static StringTemplate constructCheckBoxElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate checkboxElement = new StringTemplate(getCheckBoxTemplate());
     	checkboxElement.setAttribute("class", pm.getCssClass());
     	checkboxElement.setAttribute("id", pm.getId());
     	checkboxElement.setAttribute("name", pm.getName());
     	checkboxElement.setAttribute("onClickFunction", pm.getOnClickFunction());
     	checkboxElement.setAttribute("onChangeFunction", pm.getOnChangeFunction());
     	
     	if (StringUtils.isNotEmpty(pm.getValue())) {
     		checkboxElement.setAttribute("value", pm.getValue());	
     	} else {
     		checkboxElement.setAttribute("value", false);
     	}
     	
     	if (Boolean.parseBoolean(pm.getValue())) {
     		checkboxElement.setAttribute("checked", "checked");
     	} else {
     		checkboxElement.setAttribute("checked", "");
     	}
     	checkboxElement.setAttribute("ctrlsId", pm.getControlId());
     	String additionalParam = getAdditionalParam(pm.getValue(), pm.getDependency());
         if (StringUtils.isNotEmpty(additionalParam)) {
             StringBuilder builder = new StringBuilder("additionalParam='dependency=");
             builder.append(additionalParam);
             builder.append("' ");
             checkboxElement.setAttribute("additionalParam", builder);
         }
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", checkboxElement);
     	
     	return controlGroupElement;
     }
     
     public static StringTemplate constructSelectElement(ParameterModel pm) {
     	if (pm.isMultiple()) {
     		return constructMultiSelectElement(pm);
     	} else {
     		return constructSingleSelectElement(pm);
     	}
     }
     
     public static StringTemplate constructActionsElement(ParameterModel pm) {
         StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
         controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
         StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), "");
         
         StringTemplate actionsElement = new StringTemplate(getActionsTemplate());
         actionsElement.setAttribute("value", pm.getLableText());
         actionsElement.setAttribute("id", pm.getId());
         actionsElement.setAttribute("onClickFunction", pm.getOnClickFunction());
         
         controlGroupElement.setAttribute("lable", lableElmnt);
         controlGroupElement.setAttribute("controls", actionsElement);
         return controlGroupElement;
     }
 
     public static StringTemplate constructSingleSelectElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate selectElement = new StringTemplate(getSelectTemplate());
     	StringBuilder options = constructOptions(pm.getObjectValue(), pm.getSelectedValues(), pm.getDependency(), pm.getOptionOnclickFunction());
     	selectElement.setAttribute("name", pm.getName());
     	selectElement.setAttribute("cssClass", pm.getCssClass());
     	selectElement.setAttribute("options", options);
     	selectElement.setAttribute("id", pm.getId());
     	selectElement.setAttribute("isMultiple", pm.isMultiple());
     	selectElement.setAttribute("ctrlsId", pm.getControlId());
     	selectElement.setAttribute("onChangeFunction", pm.getOnChangeFunction());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", selectElement);
     	
     	return controlGroupElement;
     }
     
     private static StringTemplate constructMultiSelectElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate multiSelectElement = new StringTemplate(getMultiSelectTemplate());
     	multiSelectElement.setAttribute("cssClass", pm.getCssClass());
     	multiSelectElement.setAttribute("id", pm.getId());
     	multiSelectElement.setAttribute("additionalParam", pm.getDependency());
     	
     	StringBuilder multiSelectOptions = constructMultiSelectOptions(pm.getName(), pm.getObjectValue(), pm.getSelectedValues());
     	multiSelectElement.setAttribute("multiSelectOptions", multiSelectOptions);
     	multiSelectElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", multiSelectElement);
     	
     	return controlGroupElement;
     }
     
     private static StringBuilder constructOptions(List<? extends Object> values, List<String> selectedValues, String dependency, String optionsOnclickFunctioin) {
     	StringBuilder builder = new StringBuilder();
     	String selectedStr = "";
     	if (CollectionUtils.isNotEmpty(values)) {
         	for (Object value : values) {
         		String optionValue = getValue(value);
         		String optionKey = "";
         		if (value instanceof Value) {
         		    optionKey = ((Value) value).getKey();
         		} else if (value instanceof com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) {
         		    optionKey = ((com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) value).getKey();
         		}
 
        		if (CollectionUtils.isNotEmpty(selectedValues) && selectedValues.contains(optionKey)) {
         			selectedStr = "selected";
         		} else {
         			selectedStr = "";
         		}
         		builder.append("<option value='");
         		if (StringUtils.isNotEmpty(optionKey)) {
         		    builder.append(optionKey);
     		    } else {
         		    builder.append(optionValue);
     		    }
                 builder.append("' ");
                 String additionalParam = getAdditionalParam(value, dependency);
                 if (StringUtils.isNotEmpty(additionalParam)) {
                     builder.append("additionalParam='dependency=");
                     builder.append(additionalParam);
                     builder.append("' ");
                 }
                 builder.append(selectedStr);
                 builder.append("onclick='");
                 builder.append(optionsOnclickFunctioin);
                 builder.append("'>");
                 builder.append(optionValue);
                 builder.append("</option>");
         	}
     	}
 
     	return builder;
     }
     
     private static String getAdditionalParam(Object value, String dependency) {
         StringBuilder builder = new StringBuilder();
         boolean appendComma = false;
         if (StringUtils.isNotEmpty(dependency)) {
             appendComma = true;
             builder.append(dependency);
         }
         if (value != null && value instanceof Value && StringUtils.isNotEmpty(((Value) value).getDependency())) {
             if (appendComma) {
                 builder.append(",");
             }
             builder.append(((Value) value).getDependency());
         }
         return builder.toString();
     }
 
     private static StringBuilder constructMultiSelectOptions(String name, List<? extends Object> values, List<String> selectedValues) {
     	StringBuilder builder = new StringBuilder();
 
     	String checkedStr = "";
     	for (Object value : values) {
     		String optionValue = getValue(value);
     		if (selectedValues!= null && selectedValues.contains(optionValue)) {
     			checkedStr = "checked";
     		} else {
     			checkedStr = "";
     		}
     		String additionalParam = getAdditionalParam(value, "");
     		String onClickFunction = "";
     		if (StringUtils.isNotEmpty(additionalParam)) {
     		    onClickFunction = "updateDepdForMultSelect(this)";
     		}
     		
     		builder.append("<li><input type='checkbox' additionalParam=\"dependency="+ additionalParam + "\" onclick=\""+ onClickFunction + "\" class='popUpChckBox' value=\"");
     		builder.append(optionValue + "\" name=\""+ name + "\" " + checkedStr + ">" + optionValue + "</li>");
     	}
 
     	return builder;
     }
     
 	/**
 	 * @param value
 	 * @return
 	 */
 	private static String getValue(Object value) {
 		String optionValue = "";
 		if (value instanceof Value) {
 			optionValue = ((Value) value).getValue();
 		} else if (value instanceof com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) {
 		    optionValue = ((com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) value).getValue();
 		} else {
 			optionValue = (String) value;
 		}
 		return optionValue;
 	}
 
     public static StringTemplate constructLabelElement(Boolean isMandatory, String cssClass, String Label) {
     	StringTemplate labelElement = new StringTemplate(getLableTemplate());
     	if (isMandatory) {
     		labelElement.setAttribute("mandatory", getMandatoryTemplate());
     	} else {
     		labelElement.setAttribute("mandatory", "");
     	}
     	labelElement.setAttribute("txt", Label);
     	labelElement.setAttribute("class", cssClass);
     	return labelElement;
     }
     
     public static StringTemplate constructFieldSetElement(ParameterModel pm) {
     	StringTemplate st = new StringTemplate(getFieldsetTemplate());
     	
     	if (!pm.isShow()) {
     		st.setAttribute("hideClass", "hideContent");
     	}
     	
     	List<? extends Object> objectValues = pm.getObjectValue();
     	StringBuilder builder = new StringBuilder();
     	if (CollectionUtils.isNotEmpty(objectValues)) {
         	for (Object objectValue : objectValues) {
         		String filePath = getValue(objectValue);
         		filePath = filePath.replace("#SEP#", "/");
         		int index = filePath.lastIndexOf("/");
         		String fileName = filePath.substring(index + 1);
         		builder.append("<option value=\"");
         		builder.append(filePath + "\" >" + fileName + "</option>");
         	}
     	}	
     	
     	st.setAttribute("fileList", builder);
     	return st;
     }
 
     public static StringTemplate constructBrowseFileTreeElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	StringTemplate inputElement = new StringTemplate(getBrowseFileTreeTemplate(pm.getFileType()));
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
 		return controlGroupElement;
     }
     
     public StringTemplate constructDynamicTemplate(String CustomerId, Parameter parameter,ParameterModel parameterModel, List<? extends Object> obj, String className) throws IOException {
     	try {
     		StringBuilder sb = new StringBuilder();
     		String line;
     		Customer customer = getServiceManager().getCustomer(CustomerId);
     		RepoInfo repoInfo = customer.getRepoInfo();
     		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
     		ArtifactGroup artifactGroup = new ArtifactGroup();
     		artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
     		artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
     		artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
     		//to set version
     		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
     		ArtifactInfo artifactInfo = new ArtifactInfo();
     		artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
     		artifactInfos.add(artifactInfo);
     		artifactGroup.setVersions(artifactInfos);
     		artifactGroups.add(artifactGroup);
     		//dynamically loads Template Stream 
     		PhrescoDynamicLoader phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
     		InputStream fileStream = phrescoDynamicLoader.getResourceAsStream(parameterModel.getName()+".st");
     		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
     		while ((line = br.readLine()) != null) {
     			sb.append(line);
     		} 
     		
     		StringTemplate dynamicTemplateDiv = new StringTemplate(getDynamicTemplateWholeDiv());
     		if (parameterModel.isShow()) {
     			dynamicTemplateDiv.setAttribute("templateClass", parameterModel.getName() + "PerformanceDivClass");
     		} else {
     			dynamicTemplateDiv.setAttribute("templateClass", parameterModel.getName() + "PerformanceDivClass  hideContent");
     		}
     		
     		//dynamicTemplateDiv.setAttribute("templateId", parameterModel.getName() + "DivId");
     		StringTemplate stringTemplate = new StringTemplate(sb.toString());
     		dynamicTemplateDiv.setAttribute("className", className);
     		if (CollectionUtils.isNotEmpty(obj)) {
     			stringTemplate.setAttribute("myObject", obj);
     		} else {
     			stringTemplate.setAttribute("myObject", "");
     		}
     		dynamicTemplateDiv.setAttribute("templateDesign", stringTemplate);
     		
     		return dynamicTemplateDiv;
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
 
     	return null;
     }
    
     private static String getControlGroupTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='control-group $ctrlGrpClass$' id=\"$ctrlGrpId$\">")
     	.append("$lable$ $controls$")
     	.append("</div>");
     	
     	return sb.toString();
     }
     
     private static String getLableTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<label for='xlInput' class='control-label labelbold $class$'>")
     	.append("$mandatory$ $txt$")
     	.append("</label>");
     	
     	return sb.toString();
     }
     
     private static String getMandatoryTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<span class='red'>*</span>&nbsp");
     	
     	return sb.toString();
     }
     
     private static String getSelectTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<select class=\"input-xlarge $cssClass$\" ")
     	.append("id=\"$id$\" name=\"$name$\" isMultiple=\"$isMultiple$\" ")
     	.append("additionalParam=\"\" ")
     	.append("onfocus=\"setPreviousDependent(this);\" ")
     	.append("onchange=\"$onChangeFunction$\">")
     	.append("$options$</select>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getInputTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type=\"$type$\" class=\"input-xlarge $class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" placeholder=\"$placeholder$\" value=\"$value$\">")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getBrowseFileTreeTemplate(String fileTypes) {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type='text' class=\"$class$\" id='fileLocation' style='margin-right:5px;'")
     	.append("name=\"$name$\" >")
     	.append("<input id='browseButton' class='btn-primary btn_browse browseFileLocation'")
     	.append("value='Browse' type='button' fileTypes="+fileTypes+" onclick='browseFiles(this);'></div>");
     	
     	return sb.toString();
     }
     
     private static String getMapTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<fieldset class='mfbox siteinnertooltiptxt popup-fieldset fieldSetClassForHeader'>")
     	.append("<legend class='fieldSetLegend'>$mandatory$ $legendHeader$</legend>")
     	.append("<table align='center'>")
     	.append("<thead class='header-background'>")
     	.append("<tr class='borderForLoad'>")
     	.append("<th class='borderForLoad'>$keyMandatory$$keyLabel$</th>")
     	.append("<th class='borderForLoad'>$valueMandatory$$valueLabel$</th><th></th><th></th></tr></thead>")
     	.append("<tbody id='propTempTbodyForHeader'>")
     	.append("<tr class='borderForLoad'>")
     	.append("$mapControls$")
     	.append("<td class='borderForLoad'>")
     	.append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>")
     	.append("</tr></tbody></table></fieldset>");
 
     	return sb.toString();
     }
     
     private static String getMapSelectElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad'>")
         .append("<select class=\"input-medium $cssClass$\" ")
         .append("id=\"$id$\" name=\"$name$\" isMultiple=\"$isMultiple$\">")
         .append("$options$</select>")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span>")
         .append("</td>");
         
         return sb.toString();
     }
     
     private static String getMapInputElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad'>")
         .append("<input type='text' class='input-mini' id=\"$id$\" ")
         .append("name=\"$name$\" placeholder=\"$valuePlaceholder$\" />")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span>")
         .append("</td>");
         
         return sb.toString();
     }
    
     private static String getCheckBoxTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type='checkbox' class=\"$class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" value=\"$value$\" $checked$ onchange=\"$onChangeFunction$\" onclick=\"$onClickFunction$\" $additionalParam$/>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	return sb.toString();
     }
     
     private static String getActionsTemplate() {
         StringBuilder sb = new StringBuilder();
         sb.append("<div class='controls'>")
         .append("<input type='button' class=\"btn btn-primary $class$\" id=\"$id$\" ")
         .append("name=\"$name$\" value=\"$value$\" $checked$ onclick=\"$onClickFunction$\" $additionalParam$/>")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
         return sb.toString();
     }
     
     private static String getMultiSelectTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'><div class='multiSelectBorder'>")
     	.append("<div class='multilist-scroller multiselect multiSelHeight $class$' id=\"$id$\"")
     	.append(" additionalParam=\"dependency=$additionalParam$\"><ul>$multiSelectOptions$</ul>")
     	.append("</div><span class='help-inline' id=\"$ctrlsId$\"></span></div></div>");
     	
     	return sb.toString();
     }
     
     private static String getFieldsetTemplate() {
     	StringBuilder sb = new StringBuilder();
     	
     	sb.append("<fieldset id='fetchSqlControl' class='popup-fieldset fieldset_center_align fieldSetClass $hideClass$'>")
     	.append("<legend class='fieldSetLegend'>DB Script Execution</legend>")
     	.append("<table class='fieldSetTbl'><tbody><tr class='fieldSetTr'><td  class='fieldSetTrTd'>")
     	.append("<select class='fieldSetSelect' multiple='multiple' id='avaliableSourceScript'>$fileList$</select></td>")
     	.append("<td class='fldSetSelectTd'><ul class='fldSetUl'>")
     	.append("<li class='fldSetLi'><input type='button' value='&gt;&gt;' id='btnAddAll' class='btn btn-primary arrowBtn' onclick='buttonAddAll();'></li>")
     	.append("<li class='fieldsetLi'><input type='button' value='&gt;' id='btnAdd' class='btn btn-primary arrowBtn' onclick='buttonAdd();'></li>") 
     	.append("<li class='fieldsetLi'><input type='button' value='&lt;' id='btnRemove' class='btn btn-primary arrowBtn' onclick='buttonRemove();'></li>")
     	.append("<li class='fieldsetLi'><input type='button' value='&lt;&lt;' id='btnRemoveAll' class='btn btn-primary arrowBtn' onclick='buttonRemoveAll();'></li>")
     	.append("</ul></td><td class='fieldSetTrTd'>")
     	.append("<select class='fieldSetSelect' multiple='multiple' name='selectedSourceScript' id='selectedSourceScript'></select>")
     	.append("</td><td class='fldSetRightTd'><img  class='moveUp'  id='up' title='Move up' src='images/icons/top_arrow.png' onclick='moveUp();'><br>")
     	.append("<img class='moveDown' id='down' title='Move down' src='images/icons/btm_arrow.png' onclick='moveDown();'></td></tr></tbody></table>")
     	.append("<input type='hidden' value='' name='fetchSql' id='fetchSql'></fieldset>");	
 
     	return sb.toString();
     }
     
     private static String getDynamicTemplateWholeDiv() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='$templateClass$' id='$templateId$'> $templateDesign$")
     	.append("<input type='hidden' name='objectClass' value='$className$'/></div>");
     	
     	return sb.toString();
     }
     
     public static List<String> getCsvAsList(String csv) {
         Pattern csvPattern = Pattern.compile(CSV_PATTERN);
         Matcher match = csvPattern.matcher(csv);
 
         List<String> list = new ArrayList<String>(match.groupCount());
         // For each field
         while (match.find()) {
             String value = match.group();
             if (value == null) {
                 break;
             }
             if (value.endsWith(",")) {  // trim trailing ,
                 value = value.substring(0, value.length() - 1);
             }
             if (value.startsWith("\"")) { // assume also ends with
                 value = value.substring(1, value.length() - 1);
             }
             if (value.length() == 0) {
                 value = null;
             }
             list.add(value.trim());
         }
         if (CollectionUtils.isEmpty(list)) {
             list.add(csv.trim());
         }
         
         return list;
     }
     
     public static String listToCsv(List<?> list) {
         Iterator<?> iter = list.iterator();
         String csvString = "";
         while (iter.hasNext()) {
             csvString += iter.next() + ",";
         }
 
         return csvString;
     }
 }
