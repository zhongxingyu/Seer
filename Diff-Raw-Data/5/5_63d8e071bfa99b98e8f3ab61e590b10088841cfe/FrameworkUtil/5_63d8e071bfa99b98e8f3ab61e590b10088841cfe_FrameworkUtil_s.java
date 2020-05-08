 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.commons;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.servlet.http.HttpServletRequest;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.openxml4j.opc.OPCPackage;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.w3c.dom.Element;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.Role;
 import com.photon.phresco.commons.model.TestCase;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.UserPermissions;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.model.LockDetail;
 import com.photon.phresco.framework.model.TestSuite;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.model.Model.Profiles;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 
 public class FrameworkUtil extends FrameworkBaseAction implements Constants, FrameworkConstants {
 
 	private static final String PHRESCO_SQL_PATH = "phresco.sql.path";
 	private static final String PHRESCO_UNIT_TEST = "phresco.unitTest";
 	private static final String PHRESCO_CODE_VALIDATE_REPORT = "phresco.code.validate.report";
 	private static final long serialVersionUID = 1L;
 	private static FrameworkUtil frameworkUtil = null;
     private static final Logger S_LOGGER = Logger.getLogger(FrameworkUtil.class);
     private static HttpServletRequest request;
     
 	public static FrameworkUtil getInstance() throws PhrescoException {
         if (frameworkUtil == null) {
             frameworkUtil = new FrameworkUtil();
         }
         return frameworkUtil;
     }
 	
 	public FrameworkUtil() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public FrameworkUtil(HttpServletRequest request) {
 		this.request = request;
 	}
 	
 	public String getSqlFilePath(String oldAppDirName) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(oldAppDirName).getProperty(PHRESCO_SQL_PATH);
 	}
 
 	public String getUnitTestReportOptions(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(appinfo.getAppDirName()).getProperty(PHRESCO_UNIT_TEST);
 	}
 	
     public String getUnitTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_DIR);
     }
     
     public String getComponentTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_COMPONENTTEST_DIR);
     }
     
     public String getUnitTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR);
     }
     
     public String getComponentTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_COMPONENTTEST_RPT_DIR);
     }
     
     public String getUnitTestReportDir(ApplicationInfo appInfo, String option) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR_START + option + POM_PROP_KEY_UNITTEST_RPT_DIR_END);
     }
 
 	public String getUnitTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
     }
 	
 	public String getComponentTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_COMPONENTTEST_TESTSUITE_XPATH);
     }
 	
 	public String getUnitTestSuitePath(ApplicationInfo appInfo, String option) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_START + option + POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END);
     }
     
     public  String getUnitTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
     }
     
     public  String getComponentTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_COMPONENTTEST_TESTCASE_PATH);
     }
     
     public  String getUnitTestCasePath(ApplicationInfo appInfo, String option) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTCASE_PATH_START + option + POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END);
     }
     
     public String getManualTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_MANUALTEST_DIR);
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
 
     public String getSceenShotDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_SCREENSHOT_DIR);
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
     
     public String getPerformanceTestShowDevice(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERF_SHOW_DEVICE);
     }
     
     public String getPerformanceTestReportDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
     }
     
     public String getEmbedAppTargetDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_EMBED_APP_TARGET_DIR);
     }
     
     public String getLogFilePath(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_LOG_FILE_PATH);
     }
     
     public String isIphoneTagExists(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(PHRESCO_CODE_VALIDATE_REPORT);
     }
 	
 	public String getThemeFileExtension(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_THEME_EXT);
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
 	    	serverUrl = request.getRequestURL().toString();
 	    	StringBuilder tobeRemoved = new StringBuilder();
 	    	tobeRemoved.append(request.getContextPath());
 	    	tobeRemoved.append(request.getServletPath());
 	    	tobeRemoved.append(request.getPathInfo());
 
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
     
 	public List<String> getSonarProfiles(ApplicationInfo appInfo) throws PhrescoException {
 		List<String> sonarTechReports = new ArrayList<String>(6);
 		try {
 			StringBuilder pomBuilder = new StringBuilder(Utility.getProjectHome());
 			pomBuilder.append(File.separator);
 			pomBuilder.append(appInfo.getAppDirName());
 			pomBuilder.append(File.separator);
 			pomBuilder.append(Utility.getPomFileName(appInfo));
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
         byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(inputString.getBytes());
         String decodedString = new String(decodedBytes);
 
         return decodedString;
     }
 
     public static StringTemplate constructInputElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	String type = getInputType(pm.getInputType());
     	StringTemplate inputElement = null;
     	if(TYPE_SCHEDULER.equals(type)) {
     		inputElement = new StringTemplate(getSchedulerTemplate());
     	} else if(!TYPE_SCHEDULER.equals(type)) {
     		inputElement = new StringTemplate(getInputTemplate());
     	}
     	inputElement.setAttribute("type", type);
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("placeholder", pm.getPlaceHolder());
     	inputElement.setAttribute("value", pm.getValue());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	if (TYPE_NUMBER.equalsIgnoreCase(pm.getInputType()) && BUILD_NUMBER.equals(pm.getId())) {
     		inputElement.setAttribute("maxlength", 8);
     	}
     	if (DEPLOY_DIR.equals(pm.getId())) {
 	    	String btn = "&nbsp;&nbsp;<input type='button' class='btn btn-primary' value='Browse' onclick='browseDeployDir();'/>"; 
 	    	inputElement.setAttribute("btnElement", new StringTemplate(btn));
 	    	inputElement.setAttribute("readonly", "readonly");
     	}
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
 		} else if (TYPE_SCHEDULER.equalsIgnoreCase(inputType)) {
 			type = TYPE_SCHEDULER;
 		} else {
 			type = TEXT_BOX;
 		}
 		
 		return type;
 	}
     
 	public static StringTemplate constructMapElement(ParameterModel pm) throws IOException {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	Properties properties = new Properties();
 		StringReader reader = new StringReader(pm.getValue());
 		properties.load(reader);
 		Set<Object> keySet = properties.keySet();
     	
 		String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
     	if (pm.getValue().isEmpty()) {
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
     	} else {
     		StringTemplate mapElmnt = new StringTemplate(getMapTemplatesForValues());
 	    	if (pm.isMandatory()) {
 	    		mapElmnt.setAttribute("mandatory", getMandatoryTemplate());
 	    	}
 	    	
 	    	mapElmnt.setAttribute("legendHeader", pm.getLableText());
 	    	List<BasicParameterModel> childs = pm.getChilds();
 			
     	    StringTemplate childElement = new StringTemplate();
     	    
 	    	for (Object object : keySet) {
     			String key =(String)object;
     			String value = properties.getProperty(key);
     			StringTemplate parentTr = new StringTemplate(getMapTableRowElement());
     			//For left side controls
 	            if (childs.get(0).getInputType().equalsIgnoreCase(TYPE_LIST)) {
 	                childElement = new StringTemplate(getMapSelectElement());
 	                List<String> selectedKeys = new ArrayList<String>();
 	                selectedKeys.add(key);
 	                StringBuilder options = constructOptions(childs.get(0).getObjectValue(), selectedKeys, null, "");
 	                childElement.setAttribute("options", options);
 	                childElement.setAttribute("value", key);
 	                childElement.setAttribute("name", childs.get(0).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            } else if (childs.get(0).getInputType().equalsIgnoreCase(TYPE_STRING)) {
 	                childElement = new StringTemplate(getMapInputElement());
 	                childElement.setAttribute("value", key);
 	                childElement.setAttribute("name", childs.get(0).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            }
 	          //For right side controls  with plus, minus icons
 	            if (childs.get(1).getInputType().equalsIgnoreCase(TYPE_LIST)) {
 	                childElement = new StringTemplate(getMapSelectElement());
 	                List<String> selectedValues = new ArrayList<String>();
 	                selectedValues.add(value);
 	                StringBuilder options = constructOptions(childs.get(1).getObjectValue(), selectedValues, null, "");
 	                childElement.setAttribute("options", options);
 	                childElement.setAttribute("name", childs.get(1).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	                childElement = new StringTemplate(getMapPlusMinusIconElement(keySet.size()));
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	                childElement.setAttribute("value", value);
 	            } else if (childs.get(1).getInputType().equalsIgnoreCase(TYPE_STRING)) {
 	                childElement = new StringTemplate(getMapInputElement());
 	                childElement.setAttribute("value", value);
 	                childElement.setAttribute("name", childs.get(1).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
                 	childElement = new StringTemplate(getMapPlusMinusIconElement(keySet.size()));
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            }
 	            
 	            mapElmnt.setAttribute("mapTrContrls", parentTr);
 	        }
             updateChildLabels(mapElmnt, childs.get(0));
             
             
             updateChildLabels(mapElmnt, childs.get(1));
             
 	    	controlGroupElement.setAttribute("lable", mapElmnt);
          }
 		return controlGroupElement;
 	}
 	
 	public static StringTemplate constructCustomParameters(ParameterModel pm) {
 	    StringTemplate controlGroupElement = new StringTemplate(getCustomParamTableTemplate(pm.getValue(), pm.getObjectValue(), pm.isShowMinusIcon()));
 	    return controlGroupElement;
 	}
 	
 	private static String getCustomParamTableTemplate(String key, List<? extends Object> value, boolean showMinus) {
         StringBuilder sb = new StringBuilder();
         sb.append("<table class='custParamTable'>")
         .append("<tbody id='propTempTbodyForHeader'>")
         .append("<tr class='borderForLoad'>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"key\" placeholder=\"Key\" value="+key+">")
         .append("</td>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"value\" placeholder=\"Value\" value="+value.get(0).toString().trim().replace(" ", "&#32;")+">")
         .append("</td>")
         .append("<td class='borderForLoad noBorder'>")
         .append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>");
         if (showMinus) {
         	sb.append("<td class='borderForLoad noBorder'><a><img class='add imagealign' src='images/icons/minus_icon.png' onclick='removeRow(this)'></a></td>");
         }
         sb.append("</tr></tbody></table>");
         
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
     	
     	String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
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
     
     public static StringTemplate constructConfigMultiSelectBox(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate multiSelectElement = new StringTemplate(getConfigMultiSelectTemplate());
     	multiSelectElement.setAttribute("cssClass", pm.getCssClass());
     	multiSelectElement.setAttribute("id", pm.getId());
     	multiSelectElement.setAttribute("name", pm.getId());
     	
     	StringBuilder multiSelectOptions = constructConfigMultiSelectOptions(pm.getName(), pm.getObjectValue(), pm.getSelectedValues(), pm.getId());
     	multiSelectElement.setAttribute("multiSelectOptions", multiSelectOptions);
     	multiSelectElement.setAttribute("ctrlsId", pm.getControlId());
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", multiSelectElement);
     	
     	return controlGroupElement;
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
     	
     	String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate selectElement = new StringTemplate(getSelectTemplate());
     	StringBuilder options = constructOptions(pm.getObjectValue(), pm.getSelectedValues(), pm.getDependency(), pm.getOptionOnclickFunction());
     	selectElement.setAttribute("name", pm.getName());
     	selectElement.setAttribute("cssClass", pm.getCssClass());
     	selectElement.setAttribute("options", options);
     	selectElement.setAttribute("id", pm.getId());
     	if (CollectionUtils.isEmpty(pm.getObjectValue())) {
     		selectElement.setAttribute("dependency", pm.getDependency()	);
     	}
     	selectElement.setAttribute("isMultiple", pm.isMultiple());
     	selectElement.setAttribute("ctrlsId", pm.getControlId());
     	selectElement.setAttribute("onChangeFunction", pm.getOnChangeFunction());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", selectElement);
     	
     	return controlGroupElement;
     }
     
     public static StringTemplate constructTextBxWitBtn(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	StringTemplate inputElement = new StringTemplate(getInputTemplate());
     	inputElement.setAttribute("type", getInputType(pm.getInputType()));
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("placeholder", pm.getPlaceHolder());
     	inputElement.setAttribute("value", pm.getValue());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	String btn = "&nbsp;&nbsp;<input type='button' class='btn btn-primary' value='Authenticate' onclick='authenticateServer();'/>"; 
     	inputElement.setAttribute("btnElement", new StringTemplate(btn));
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
     	return controlGroupElement;
     }
     
     private static StringTemplate constructMultiSelectElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
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
         		} else if (value instanceof com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) {
         			optionKey = ((com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) value).getKey();	
         		}
 
         		if (CollectionUtils.isNotEmpty(selectedValues) && (selectedValues.contains(optionKey) || selectedValues.contains(optionValue))) {
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
                 String hideControls = getHideControls(values, value, dependency);
                 if (StringUtils.isNotEmpty(hideControls) && !hideControls.equals(additionalParam)) {
                 	builder.append("hide=");
                     builder.append(hideControls);
                 }
                 builder.append(" ");
                 builder.append(selectedStr);
                 builder.append(" ");
                 builder.append("onclick='");
                 builder.append(optionsOnclickFunctioin);
                 builder.append("'>");
                 builder.append(optionValue);
                 builder.append("</option>");
         	}
     	}
 
     	return builder;
     }
     
     private static String getHideControls(List<? extends Object> values, Object currentValue, String dep) {
     	StringBuilder sb = new StringBuilder();
     	String comma = "";
     	for (Object value : values) {
 			if (value instanceof Value) {
 				if (StringUtils.isNotEmpty(((Value) value).getDependency()) && StringUtils.isNotEmpty(((Value) value).getKey()) &&  
 						StringUtils.isNotEmpty(((Value) currentValue).getKey()) && !((Value) value).getKey().equals(((Value) currentValue).getKey()) 
 						&& !((Value) value).getDependency().equals(((Value) currentValue).getDependency())) {
 					sb.append(comma);
 					sb.append(((Value) value).getDependency());
 					comma = Constants.COMMA;
 				}
 			}
 		}
     	return sb.toString();
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
     
     private static StringBuilder constructConfigMultiSelectOptions(String name, List<? extends Object> values, List<String> selectedValues, String key) {
     	StringBuilder builder = new StringBuilder();
     	
     	String className = key;
     	String checkedStr = "";
     	for (Object value : values) {
     		String optionValue = getValue(value);
     		if (selectedValues != null && selectedValues.contains(optionValue)) {
     			checkedStr = "checked";
     		} else {
     			checkedStr = "";
     		}
     		String additionalParam = getAdditionalParam(value, "");
     		
     		builder.append("<li><input type='checkbox' additionalParam=\"dependency="+ additionalParam + "\" onclick='updateHdnFieldForMultType(this)' class=\""+ className + "\" value=\"");
     		builder.append(optionValue + "\" " + checkedStr + ">" + optionValue + "</li>");
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
 		} else if (value instanceof com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) {
 			optionValue = ((com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) value).getValue();	
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
 		
 		// existing selected scripts should be inserted in fetchsql hidden field
     	if (StringUtils.isNotEmpty(pm.getValue())) {
     		Gson gson = new Gson();
 			String json = gson.toJson(pm.getValue());
 			String jsonPath = json.replace("\\", "").replaceAll("^\"|\"$","");
 			st.setAttribute("selectedJsonData", jsonPath);
     	} 
     	
     	// all the available scripts for corresponding db values need to be added
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
     	
     	String ctrlGrpClass = pm.getControlGroupClass();
     	if (!pm.isShow()) {
     		ctrlGrpClass = ctrlGrpClass + " hideContent";
     	}
     	controlGroupElement.setAttribute("ctrlGrpClass", ctrlGrpClass);
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	StringTemplate inputElement = new StringTemplate(getBrowseFileTreeTemplate());
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	inputElement.setAttribute("fileTypes", pm.getFileType());
     	if (StringUtils.isNotEmpty(pm.getValue())) {
     		inputElement.setAttribute("path", pm.getValue());
     	} else {
     		inputElement.setAttribute("path", "");
     	}
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
 		return controlGroupElement;
     }
     
     public static StringTemplate constructFileBrowseForPackage(ParameterModel pm) {
         StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
         controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
         StringTemplate tableElement = new StringTemplate(constructTable());
         StringTemplate targetFolder = new StringTemplate(constructInputElement());
         targetFolder.setAttribute("name", "targetFolder");
         targetFolder.setAttribute("class", "input-small");
         tableElement.setAttribute("td1", targetFolder);
         StringTemplate fileOrFolder = new StringTemplate(constructInputElement());
         fileOrFolder.setAttribute("name", "selectedFileOrFolder");
         fileOrFolder.setAttribute("disabled", "disabled");
         fileOrFolder.setAttribute("class", "input-medium");
         tableElement.setAttribute("td2", fileOrFolder);
         tableElement.setAttribute("td3", constructButtonElement());
         controlGroupElement.setAttribute("controls", tableElement);
         
         return controlGroupElement;
     }
     
     private static String constructTable() {
         StringBuilder sb = new StringBuilder()
         .append("<div class='headerDiv'>")
         .append("<table align='center' class='package-file-browse-tbl'>")
         .append("<thead class='header-background'>")
         .append("<tr class='borderForLoad'>")
         .append("<th class='borderForLoad mapHeading'>Target Folder</th>")
         .append("<th class='borderForLoad mapHeading'>File/Folder</th>")
         .append("</tr></thead></table>")
         .append("</div>")
         .append("<div class='bldBrowseFilePrntDiv'>")
         .append("<div class='bldBrowseFileDiv'>")
         .append("<div class='bldBrowseFileLeftDiv'>")
         .append("<table align='center' class='package-file-browse-tbl'>")
         .append("<tbody id='propTempTbodyForHeader'>")
         .append("<tr>")
         .append("<td class='popuptable'>$td1$</td>")
         .append("<td class='popuptable'>$td2$</td>")
         .append("<td class='popuptable'>$td3$</td>")
         .append("</tr></tbody></table></div>")
         .append("<div class='bldBrowseFileRightDiv'>")
         .append("<img class='imagealign add_icon' src='images/icons/add_icon.png' onclick='addRowInPackageBrowse(this);'>")
         .append("<img class='imagealign hideContent minus_icon' src='images/icons/minus_icon.png' onclick='removeRowInPackageBrowse(this);'>")
         .append("</div><div style='clear:both;'></div></div></div>");
 
         return sb.toString();
     }
     
 	private static String constructInputElement() {
         StringBuilder sb = new StringBuilder("<input type='text' name='$name$' class='$class$' $disabled$ />");
         return sb.toString();
     }
     
     private static String constructButtonElement() {
         StringBuilder sb = new StringBuilder("<input type='button' value='Browse' class='btn btn-primary' fromPage='package' onclick='browseFiles(this)'/>");
         return sb.toString();
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
     	.append("id=\"$id$\" name=\"$name$\" dependencyAttr=\"$dependency$\" isMultiple=\"$isMultiple$\" ")
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
     	.append("<input type=\"$type$\" class=\"input-xlarge $class$\" id=\"$id$\" maxlength=\"$maxlength$\" ")
     	.append("name=\"$name$\" placeholder=\"$placeholder$\" $readonly$ value=\"$value$\">$btnElement$")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getSchedulerTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type=\"$type$\" class=\"input-xlarge $class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" placeholder=\"$placeholder$\" value=\"$value$\">")
     	.append("<span class='help-inline'><img class='add imagealign' src='images/icons/gear.png' style='cursor:pointer' connector=\"$id$\" onclick='callCron(this);'></span>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getBrowseFileTreeTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type='text' class=\"$class$\" value=\"$path$\" id='fileLocation' readonly='readonly' style='margin-right:5px;'")
     	.append("name=\"$name$\" >")
     	.append("<input id='browseButton' class='btn-primary btn_browse browseFileLocation'")
     	.append("value='Browse' type='button' fileTypes=\"$fileTypes$\" onclick='browseFiles(this);'></div>");
     	
     	return sb.toString();
     }
     
     private static String getMapTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<fieldset class='mfbox siteinnertooltiptxt popup-fieldset fieldSetClassForHeader'>")
     	.append("<legend class='fieldSetLegend'>$mandatory$ $legendHeader$</legend>")
     	.append("<table align='center'>")
     	.append("<thead class='header-background'>")
     	.append("<tr class='borderForLoad'>")
     	.append("<th class='borderForLoad mapHeading'>$keyMandatory$$keyLabel$</th>")
     	.append("<th class='borderForLoad mapHeading'>$valueMandatory$$valueLabel$</th><th></th><th></th></tr></thead>")
     	.append("<tbody id='propTempTbodyForHeader'>")
     	.append("<tr class='borderForLoad'>")
     	.append("$mapControls$")
     	.append("<td class='borderForLoad'>")
     	.append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>")
     	.append("</tr></tbody></table></fieldset>");
 
     	return sb.toString();
     }
     
     private static String getMapTemplatesForValues() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<fieldset class='mfbox siteinnertooltiptxt popup-fieldset fieldSetClassForHeader'>")
     	.append("<legend class='fieldSetLegend'>$mandatory$ $legendHeader$</legend>")
     	.append("<table align='center'>")
     	.append("<thead class='header-background'>")
     	.append("<tr class='borderForLoad'>")
     	.append("<th class='borderForLoad mapHeading'>$keyMandatory$$keyLabel$</th>")
     	.append("<th class='borderForLoad mapHeading'>$valueMandatory$$valueLabel$</th><th></th><th></th></tr></thead>")
     	.append("<tbody id='propTempTbodyForHeader'>")
     	.append("$mapTrContrls$")
     	.append("</tbody></table></fieldset>");
 
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
         .append("name=\"$name$\" value=\"$value$\" placeholder=\"$valuePlaceholder$\" />")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span>")
         .append("</td>");
         
         return sb.toString();
     }
     
     private static String getMapPlusMinusIconElement(int size) {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad addImage' name='addImage'>")
     	.append("<a><img class='add imagealign' src='images/icons/add_icon.png' style='cursor:pointer' onclick='addRow(this);'></a></td>");
         if (size == 1) {
         	sb.append("<td class='borderForLoad removeImage hideContent' >");
         	sb.append("<a><img class='add imagealign' src='images/icons/minus_icon.png' style='cursor:pointer' onclick='removeRow(this);'></a></td>");
         } else {
         	sb.append("<td class='borderForLoad removeImage'>");
         	sb.append("<a><img class='add imagealign' src='images/icons/minus_icon.png' style='cursor:pointer' onclick='removeRow(this);'></a></td>");
         }
         
         return sb.toString();
     }
     
     private static String getMapTableRowElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<tr class='borderForLoad'>")
     	.append("$mapTdContrls$")
         .append("</tr>");
         
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
     
     private static String getConfigMultiSelectTemplate() {
     	StringBuilder sb = new StringBuilder()
     	.append("<div class='controls'><div class='multiSelectBorder'>")
     	.append("<div class='multilist-scroller multiselect multiSelHeight $class$' id=\"$id$\"")
     	.append(" additionalParam=\"dependency=$additionalParam$\"><ul>$multiSelectOptions$</ul>")
     	.append("</div><span class='help-inline' id=\"$ctrlsId$\"></span></div>")
     	.append("<input type='hidden' value='' name=\"$name$\"></div>");
     	
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
     	.append("<input type='hidden' value='$selectedJsonData$' name='fetchSql' id='fetchSql'></fieldset>");	
 
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
         String sep = "";
         while (iter.hasNext()) {
             csvString += sep + iter.next();
             sep = ",";
         }
         
         return csvString;
     }
     
     public static UserPermissions getUserPermissions(ServiceManager serviceManager, User user) throws PhrescoException {
     	UserPermissions permissions = new UserPermissions();
     	try {
     		List<String> roleIds = user.getRoleIds();
 			if (CollectionUtils.isNotEmpty(roleIds)) {
 				List<String> permissionIds = new ArrayList<String>();
 				for (String roleId : roleIds) {
 					Role role = serviceManager.getRole(roleId);
 					permissionIds.addAll(role.getPermissionIds());
 				}
 				
 				if (CollectionUtils.isNotEmpty(permissionIds)) {
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_APPLICATIONS)) {
 						permissions.setManageApplication(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_REPO)) {
 						permissions.setManageRepo(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_IMPORT_APPLICATIONS)) {
 						permissions.setImportApplication(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_PDF_REPORTS)) {
 						permissions.setManagePdfReports(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_CODE_VALIDATION)) {
 						permissions.setManageCodeValidation(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_CONFIGURATIONS)) {
 						permissions.setManageConfiguration(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_BUILDS)) {
 						permissions.setManageBuilds(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_TEST)) {
 						permissions.setManageTests(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_CI_JOBS)) {
 						permissions.setManageCIJobs(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_EXECUTE_CI_JOBS)) {
 						permissions.setExecuteCIJobs(true);
 					}
 					
 					if (permissionIds.contains(FrameworkConstants.PER_MANAGE_MAVEN_REPORTS)) {
 						permissions.setManageMavenReports(true);
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw e;
 		}
 		
 		return permissions;
     }
 
     
     public static void generateLock(List<LockDetail> lockDetails, boolean toGenerate) throws PhrescoException {
     	BufferedWriter out = null;
 		FileWriter fstream = null;
 		try {
 			List<LockDetail> availableLockDetails = getLockDetails();
 			if (toGenerate && CollectionUtils.isNotEmpty(availableLockDetails)) {
				lockDetails.addAll(availableLockDetails);
 			}
 			
 			Gson gson = new Gson();
 			String infoJSON = gson.toJson(lockDetails);
 			fstream = new FileWriter(getLockFilePath());
 			out = new BufferedWriter(fstream);
 			out.write(infoJSON);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
     }
     
     public static List<LockDetail> getLockDetails() throws PhrescoException {
 		FileReader reader = null;
 		try {
 			File file = new File(getLockFilePath());
 			if (file.exists()) {
 				reader = new FileReader(file);
 				Gson gson = new Gson();
 				List<LockDetail> lockDetails = gson.fromJson(reader, new TypeToken<List<LockDetail>>(){}.getType());
 				return lockDetails;
 			}
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(reader);
 		}
 		return null;
 	}
     
     private static String getLockFilePath() {
     	StringBuilder sb = new StringBuilder(Utility.getPhrescoHome())
 		.append(File.separator)
 		.append(PROJECTS_WORKSPACE)
 		.append(File.separator)
     	.append(LOCK_FILE);
     	return sb.toString();
     }
 
 	public List<TestSuite> readManualTestSuiteFile(String filePath) {
 		List<TestSuite> testSuites = readTestSuites(filePath);
 		return testSuites;
 	}
 
     public  List<TestSuite> readTestSuites(String filePath)  {
             List<TestSuite> excels = new ArrayList<TestSuite>();
             Iterator<Row> rowIterator = null;
             try {
             	File testDir = new File(filePath);
           		StringBuilder sb = new StringBuilder(filePath);
        	        if(testDir.isDirectory()) {
 	       	        	FilenameFilter filter = new PhrescoFileFilter("", "xlsx");
 	       	        	File[] listFiles = testDir.listFiles(filter);
 	       	        	if (listFiles.length != 0) {
 							for (File file1 : listFiles) {
 								 if (file1.isFile()) {
 									sb.append(File.separator);
 							    	sb.append(file1.getName());
 							    }
 							}
 							readTestSuiteFromXLSX(excels, sb);
 	       	        	} else {
 	   	                	FilenameFilter filter1 = new PhrescoFileFilter("", "xls");
 	   	     	            File[] listFiles1 = testDir.listFiles(filter1);
 	   	     	            if (listFiles1.length != 0) {
 		   	     	            for(File file2 : listFiles1) {
 		   	     	            	if (file2.isFile()) {
 		   	     	            		sb.append(File.separator);
 		   	    	                	sb.append(file2.getName());
 		   	     	            	}
 		   	     	            }
 		   	     	            readTestSuitesFromXLS(excels, sb);
 	   	     	            } /*else {
 		   	     	            FilenameFilter filterOds = new PhrescoFileFilter("", "ods");
 		   	     	            File[] odsListFiles = testDir.listFiles(filterOds);
 		   	     	            for(File file2 : odsListFiles) {
 		   	     	            	if (file2.isFile()) {
 		   	     	            		sb.append(File.separator);
 		   	    	                	sb.append(file2.getName());
 		   	     	            	}
 		   	     	            }
 	   	     	            	readTestSuiteFromODS(sb, excels);
 	   	     	            }*/
 	   	                }
        	        	}
                     
             } catch (Exception e) {
                    // e.printStackTrace();
             }
             return excels;
     }
 
 	private void readTestSuiteFromXLSX(List<TestSuite> excels, StringBuilder sb)
 			throws FileNotFoundException, InvalidFormatException, IOException,
 			UnknownHostException, PhrescoException {
 		Iterator<Row> rowIterator;
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		    
 		OPCPackage opc=OPCPackage.open(myInput); 
 		
 		XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 		XSSFSheet mySheet = myWorkBook.getSheetAt(0);
 		rowIterator = mySheet.rowIterator();
 		for (int i = 0; i <=2; i++) {
 			rowIterator.next();
 		}
 		
 		while (rowIterator.hasNext()) {
 			Row next = rowIterator.next();
 			if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 				TestSuite createObject = createObject(next);
 		    	excels.add(createObject);
 			}
 		}
 	}
 
 	private void readTestSuitesFromXLS(List<TestSuite> excels, StringBuilder sb)
 			throws FileNotFoundException, IOException, UnknownHostException,
 			PhrescoException {
 		Iterator<Row> rowIterator;
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 
 		HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 		rowIterator = mySheet.rowIterator();
 		for (int i = 0; i <=2; i++) {
 			rowIterator.next();
 		}
 		while (rowIterator.hasNext()) {
 			Row next = rowIterator.next();
 			if (StringUtils.isNotEmpty(getValue(next.getCell(2))) && !getValue(next.getCell(2)).equalsIgnoreCase("Total")) {
 				TestSuite createObject = createObject(next);
 		    	excels.add(createObject);
 			}
 		}
 	}
     
 	/*private void readTestSuiteFromODS(StringBuilder sb, List<TestSuite> testSuites) throws PhrescoException {
 		File file = new File(sb.toString());
 		org.jopendocument.dom.spreadsheet.Sheet sheet;
 		try {
 			ODPackage createFromFile = ODPackage.createFromFile(file);
 			SpreadSheet spreadSheet = createFromFile.getSpreadSheet();
 			sheet = spreadSheet.getSheet(0);
 			
 			int nColCount = sheet.getColumnCount();
 			int nRowCount = sheet.getRowCount();
 
 			//Iterating through each row of the selected sheet
 			org.jopendocument.dom.spreadsheet.Cell cell = null;
 			for(int nRowIndex = 3; nRowIndex < nRowCount; nRowIndex++) {
 				//Iterating through each column
 				cell = sheet.getCellAt(1, nRowIndex);
 				if (cell.getValue() != null && cell.getValue() != "") {
 					TestSuite testSuite = readDataFromODS(nRowIndex, sheet);
 					testSuites.add(testSuite);
 				}
 			}
 
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
 	}
 	
 	private TestSuite readDataFromODS(int nRowIndex, org.jopendocument.dom.spreadsheet.Sheet sheet) throws UnknownHostException, PhrescoException{
     	TestSuite testSuite = new TestSuite();
     	org.jopendocument.dom.spreadsheet.Cell cell = null;
     	cell = sheet.getCellAt(1, nRowIndex);
     	String name= cell.getTextValue();
     	testSuite.setName(name);
     	
     	cell = sheet.getCellAt(2, nRowIndex);
     	String passVal = cell.getTextValue();
     	if(StringUtils.isNotEmpty(passVal)) {
 	    	float pass=Float.parseFloat(passVal);
 	    	testSuite.setTests(pass);
     	}
     	
     	cell = sheet.getCellAt(3, nRowIndex);
     	String failVal = cell.getTextValue();
     	if(StringUtils.isNotEmpty(failVal)) {
 	    	float fail=Float.parseFloat(failVal);
 	    	testSuite.setFailures(fail);
     	}
     	
     	cell = sheet.getCellAt(4, nRowIndex);
     	String notApp = cell.getTextValue();
     	if(StringUtils.isNotEmpty(notApp)) {
 	    	float notApplicable=Float.parseFloat(notApp);
 	    	testSuite.setNotApplicable(notApplicable);
     	}
     	
     	cell = sheet.getCellAt(5, nRowIndex);
     	String notExe = cell.getTextValue();
     	if(StringUtils.isNotEmpty(notExe)) {
 	    	float notExecuted =Float.parseFloat(notExe);
 	    	testSuite.setErrors(notExecuted);
     	}
     	
     	cell = sheet.getCellAt(6, nRowIndex);
     	String blockedVal = cell.getTextValue();
     	if(StringUtils.isNotEmpty(blockedVal)) {
 	    	float blocked =Float.parseFloat(blockedVal);
 	    	testSuite.setBlocked(blocked);
     	}
     	
     	cell = sheet.getCellAt(7, nRowIndex);
     	String totalVal = cell.getTextValue();
     	if(StringUtils.isNotEmpty(totalVal)) {
 	    	float total=Float.parseFloat(totalVal);
 	    	testSuite.setTotal(total);
     	}
     	
     	return testSuite;
 	}*/
 	private static TestSuite createObject(Row next) throws UnknownHostException, PhrescoException{
     	TestSuite testSuite = new TestSuite();
     	if(next.getCell(2) != null) {
     		Cell cell = next.getCell(2);
     		String value = getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testSuite.setName(value);
     		}
     	}
     	if(next.getCell(3)!=null){
     		Cell cell = next.getCell(3);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float pass=Float.parseFloat(value);
 	    		testSuite.setTests(pass);
     		}
     	}
     	if(next.getCell(4)!=null){
     		Cell cell = next.getCell(4);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float fail=Float.parseFloat(value);
 	    		testSuite.setFailures(fail);
     		}
     	}
     	if(next.getCell(5)!=null){
     		Cell cell = next.getCell(5);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float notApp=Float.parseFloat(value);
 	    		testSuite.setNotApplicable(notApp);
     		}
     	}
     	if(next.getCell(6)!=null){
     		Cell cell = next.getCell(6);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float notExecuted=Float.parseFloat(value);
 	    		testSuite.setErrors(notExecuted);
     		}
     	}
     	if(next.getCell(7)!=null){
     		Cell cell = next.getCell(7);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float blocked=Float.parseFloat(value);
 	    		testSuite.setBlocked(blocked);
     		}
     	}
     	if(next.getCell(8)!=null){
     		Cell cell = next.getCell(8);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float total=Float.parseFloat(value);
 	    		testSuite.setTotal(total);
     		}
     	}
     	if(next.getCell(9)!=null){
     		Cell cell=next.getCell(9);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
 	    		float testCoverage=Float.parseFloat(value);
 	    		testSuite.setTestCoverage(testCoverage);
     		}
     	}
     	return testSuite;
 	}
     
     public List<TestCase> readManualTestCaseFile(String filePath, String fileName, com.photon.phresco.commons.model.TestCase testCase) throws PhrescoException {
 		List<TestCase> testCases = readTestCase(filePath, fileName, testCase);
 		return testCases;
 	}
     
     private List<TestCase> readTestCase(String filePath,String fileName,com.photon.phresco.commons.model.TestCase tstCase) throws PhrescoException {
     	 List<TestCase> testCases = new ArrayList<TestCase>();
     	 try {
     		 File testDir = new File(filePath);
        		StringBuilder sb = new StringBuilder(filePath);
 	        if(testDir.isDirectory()) {
        	        	FilenameFilter filter = new PhrescoFileFilter("", "xlsx");
        	        	File[] listFiles = testDir.listFiles(filter);
        	        	if (listFiles.length != 0) {
 						for (File file1 : listFiles) {
 							 if (file1.isFile()) {
 								sb.append(File.separator);
 						    	sb.append(file1.getName());
 						    }
 						}
 						updateTestCaseToXLSX(fileName, tstCase, testCases, sb);
        	        	} else {
    	                	FilenameFilter filter1 = new PhrescoFileFilter("", "xls");
    	     	            File[] listFiles1 = testDir.listFiles(filter1);
 	   	     	            if (listFiles1.length != 0) {
 		   	     	            for(File file2 : listFiles1) {
 		   	     	            	if (file2.isFile()) {
 		   	     	            		sb.append(File.separator);
 		   	    	                	sb.append(file2.getName());
 		   	     	            	}
 		   	     	            }
 		   	     	            FileInputStream myInput = new FileInputStream(sb.toString());
 		   	     	            HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 			   	     	        int numberOfSheets = myWorkBook.getNumberOfSheets();
 						         for (int j = 0; j < numberOfSheets; j++) {
 						        	 HSSFSheet mySheet = myWorkBook.getSheetAt(j);
 						        	 if(mySheet.getSheetName().equals(fileName)) {
 						        		 Iterator<Row> rowIterator;
 						        		 readTestFromSheet(tstCase, testCases, mySheet);
 						    	         if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 						    	        	 updateIndexPage(fileName, tstCase,
 													testCases, myWorkBook);
 							    	         }
 						    	         if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 							    	         	myInput.close();
 				    	         			    FileOutputStream outFile =new FileOutputStream(sb.toString());
 				    	         			    myWorkBook.write(outFile);
 				    	         			    outFile.close();
 						    	         }
 						        	 }
 						         }
 	   	     	            }/* else {
 		   	     	            FilenameFilter odsFilter = new PhrescoFileFilter("", "ods");
 		   	     	            File[] odsListFiles = testDir.listFiles(odsFilter);
 			   	     	        for(File file2 : odsListFiles) {
 		   	     	            	if (file2.isFile()) {
 		   	     	            		sb.append(File.separator);
 		   	    	                	sb.append(file2.getName());
 		   	     	            	}
 		   	     	            }
 			   	     	        testCases = readTestCasesFormODS(sb, testCases, fileName, tstCase);
 				   	     	   
 	   	     	            }*/
    	                }
     	        }
     	 } catch (Exception e) {
 	     }
          return testCases;
     }
 
 	private void updateTestCaseToXLSX(String fileName,
 			com.photon.phresco.commons.model.TestCase tstCase,
 			List<TestCase> testCases, StringBuilder sb)
 			throws FileNotFoundException, InvalidFormatException, IOException,
 			UnknownHostException, PhrescoException {
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		    
 		OPCPackage opc=OPCPackage.open(myInput); 
 		
 		XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 		int numberOfSheets = myWorkBook.getNumberOfSheets();
 		 for (int j = 0; j < numberOfSheets; j++) {
 			 XSSFSheet mySheet = myWorkBook.getSheetAt(j);
 			 if(mySheet.getSheetName().equals(fileName)) {
 				 Iterator<Row> rowIterator = mySheet.rowIterator();
 		         for (int i = 0; i <=23; i++) {
 						rowIterator.next();
 					}
 		         while (rowIterator.hasNext()) {
 		     		Row next = rowIterator.next();
 		     		if (StringUtils.isNotEmpty(getValue(next.getCell(1)))) {
 		     			TestCase createObject = readTest(next);
 		     			testCases.add(createObject);
 		     			if (tstCase != null && createObject.getTestCaseId().equals(tstCase.getTestCaseId())) {
 		     				Cell stepsCell=next.getCell(5);
 		     				stepsCell.setCellValue(tstCase.getSteps());
 		     				
 		     				Cell expectedCell=next.getCell(8);
 		     				expectedCell.setCellValue(tstCase.getExpectedResult());
 		     				
 		     				Cell actualCell=next.getCell(9);
 		     				actualCell.setCellValue(tstCase.getActualResult());
 		     				
 		     				Cell statusCell=next.getCell(10);
 		     				statusCell.setCellValue(tstCase.getStatus());
 		     				
 		     				Cell commentCell=next.getCell(13);
 		     				commentCell.setCellValue(tstCase.getBugComment());
 		     			   
 		     			}
 		     		}
 		     		
 		         }
 		         if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 		     		float totalPass = 0;
 					float totalFail = 0;
 					float totalNotApplicable = 0;
 					float totalBlocked = 0;
 					float notExecuted = 0;
 					float totalTestCases = 0;
 		     		for (TestCase testCase: testCases) {
 		     			String testCaseStatus = testCase.getStatus();
 		     			String testId = tstCase.getTestCaseId();
 						String status = tstCase.getStatus();
 		     			if(testCaseStatus.equalsIgnoreCase("Pass") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalPass = totalPass + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("Fail") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalFail = totalFail + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("notApplicable") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalNotApplicable = totalNotApplicable + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("blocked") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalBlocked = totalBlocked + 1;
 						}
 						
 						if (testCase.getTestCaseId().equals(testId) && !testCase.getStatus().equalsIgnoreCase("Pass") 
 								&& !testCase.getStatus().equalsIgnoreCase("success")
 								&& status.equalsIgnoreCase("Pass") || status.equalsIgnoreCase("success")) {
 							totalPass = totalPass +1;
 						} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("Fail") 
 								&& !testCase.getStatus().equalsIgnoreCase("failure")
 								&& status.equalsIgnoreCase("Fail") || status.equalsIgnoreCase("failure")) {
 							totalFail = totalFail + 1;
 						}  else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("notApplicable") 
 								&& status.equalsIgnoreCase("notApplicable")) {
 							totalNotApplicable = totalNotApplicable + 1;
 						} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("blocked") 
 								&& status.equalsIgnoreCase("blocked")) {
 							totalBlocked = totalBlocked + 1;
 						}
 						totalTestCases = totalPass + totalFail + notExecuted + totalNotApplicable + totalBlocked;
 						XSSFSheet mySheet1 = myWorkBook.getSheetAt(0);
 						rowIterator = mySheet1.rowIterator();
 						 for (int i = 0; i <=2; i++) {
 								rowIterator.next();
 							}
 		                while (rowIterator.hasNext()) {
 		            		Row next1 = rowIterator.next();
 		            		if (StringUtils.isNotEmpty(getValue(next1.getCell(2))) && !getValue(next1.getCell(2)).equalsIgnoreCase("Total")) {
 		            			TestSuite createObject = createObject(next1);
 		                    	if (StringUtils.isNotEmpty(testId) && createObject.getName().equals(fileName)) {
 			         				updateIndex(totalPass,
 											totalFail,
 											totalNotApplicable,
 											totalBlocked, next1);
 			         			}
 		            		}
 		                }
 		     		}
 		         }
 		         if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 		         	myInput.close();
 				    FileOutputStream outFile =new FileOutputStream(sb.toString());
 				    myWorkBook.write(outFile);
 				    outFile.close();
 		         }
 			 }
 		}
 	}
 
 	private void updateIndexPage(String fileName,
 			com.photon.phresco.commons.model.TestCase tstCase,
 			List<TestCase> testCases, HSSFWorkbook myWorkBook)
 			throws UnknownHostException, PhrescoException {
 		Iterator<Row> rowIterator;
 		float totalPass = 0;
 			float totalFail = 0;
 			float totalNotApplicable = 0;
 			float totalBlocked = 0;
 			float notExecuted = 0;
 			float totalTestCases = 0;
 			for (TestCase testCase: testCases) {
 				String testCaseStatus = testCase.getStatus();
 				String testId = tstCase.getTestCaseId();
 				String status = tstCase.getStatus();
 				if(testCaseStatus.equalsIgnoreCase("Pass") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 					totalPass = totalPass + 1;
 				} else if (testCaseStatus.equalsIgnoreCase("Fail") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 					totalFail = totalFail + 1;
 				} else if (testCaseStatus.equalsIgnoreCase("notApplicable") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 					totalNotApplicable = totalNotApplicable + 1;
 				} else if (testCaseStatus.equalsIgnoreCase("blocked") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 					totalBlocked = totalBlocked + 1;
 				}
 				
 				if (testCase.getTestCaseId().equals(testId) && !testCase.getStatus().equalsIgnoreCase("Pass") 
 						&& !testCase.getStatus().equalsIgnoreCase("success")
 						&& status.equalsIgnoreCase("Pass") || status.equalsIgnoreCase("success")) {
 					totalPass = totalPass +1;
 				} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("Fail") 
 						&& !testCase.getStatus().equalsIgnoreCase("failure")
 						&& status.equalsIgnoreCase("Fail") || status.equalsIgnoreCase("failure")) {
 					totalFail = totalFail + 1;
 				}  else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("notApplicable") 
 						&& status.equalsIgnoreCase("notApplicable")) {
 					totalNotApplicable = totalNotApplicable + 1;
 				} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("blocked") 
 						&& status.equalsIgnoreCase("blocked")) {
 					totalBlocked = totalBlocked + 1;
 				}
 				totalTestCases = totalPass + totalFail + notExecuted + totalNotApplicable + totalBlocked;
 				HSSFSheet mySheet1 = myWorkBook.getSheetAt(0);
 				rowIterator = mySheet1.rowIterator();
 				 for (int i = 0; i <=2; i++) {
 						rowIterator.next();
 					}
 		        while (rowIterator.hasNext()) {
 		    		Row next1 = rowIterator.next();
 		    		if (StringUtils.isNotEmpty(getValue(next1.getCell(2))) && !getValue(next1.getCell(2)).equalsIgnoreCase("Total")) {
 		    			TestSuite createObject = createObject(next1);
 		            	if (StringUtils.isNotEmpty(testId) && createObject.getName().equals(fileName)) {
 		     				updateIndex(
 									totalPass,
 									totalFail,
 									totalNotApplicable,
 									totalBlocked,
 									next1);
 		     			}
 		    		}
 		        }
 			}
 	}
 
 	/*private List<TestCase> readTestCasesFormODS(StringBuilder sb, List<TestCase> testCases, String sheetName, com.photon.phresco.commons.model.TestCase tstCase) throws PhrescoException {
 		File file = new File(sb.toString());
 		org.jopendocument.dom.spreadsheet.Sheet sheet;
 		try {
 			ODPackage createFromFile = ODPackage.createFromFile(file);
 			SpreadSheet spreadSheet = createFromFile.getSpreadSheet();
 			sheet = spreadSheet.getSheet(sheetName);
 			
 			int nColCount = sheet.getColumnCount();
 			int nRowCount = sheet.getRowCount();
 			org.jopendocument.dom.spreadsheet.Cell cell = null;
 			for(int nRowIndex = 24; nRowIndex < nRowCount; nRowIndex++) {
 				cell = sheet.getCellAt(1, nRowIndex);
 				if (cell.getValue() != null && cell.getValue() != "") {
 					TestCase testCase = readTestCasesFromODS(nRowIndex, sheet);
 					testCases.add(testCase);
 					if (tstCase != null && testCase.getTestCaseId().equals(tstCase.getTestCaseId())) {
 						sheet.getCellAt(5, nRowIndex).clearValue();
 						sheet.setValueAt(tstCase.getSteps(), 5, nRowIndex);
 	     				
 						sheet.getCellAt(8, nRowIndex).clearValue();
 						sheet.setValueAt(tstCase.getExpectedResult(), 8, nRowIndex);
 	     				
 						sheet.getCellAt(9, nRowIndex).clearValue();
 						sheet.setValueAt(tstCase.getActualResult(), 9, nRowIndex);
 	     				
 						sheet.getCellAt(10, nRowIndex).clearValue();
 						sheet.setValueAt(tstCase.getStatus(), 10, nRowIndex);
 	     				
 						sheet.getCellAt(13, nRowIndex).clearValue();
 						sheet.setValueAt(tstCase.getBugComment(), 13, nRowIndex);
 	     			   
 	     			}
 				}
 				
 			}
 			float totalPass = 0;
 			float totalFail = 0;
 			float totalNotApplicable = 0;
 			float totalBlocked = 0;
 			float notExecuted = 0;
 			float totalTestCases = 0;
 			 if (tstCase != null && StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 		     		
 		     		for (TestCase testCase: testCases) {
 		     			String testCaseStatus = testCase.getStatus();
 		     			String testId = tstCase.getTestCaseId();
 						String status = tstCase.getStatus();
 		     			if(testCaseStatus.equalsIgnoreCase("Pass") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalPass = totalPass + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("Fail") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalFail = totalFail + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("notApplicable") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalNotApplicable = totalNotApplicable + 1;
 						} else if (testCaseStatus.equalsIgnoreCase("blocked") && !testCase.getTestCaseId().equalsIgnoreCase(testId)) {
 							totalBlocked = totalBlocked + 1;
 						}
 						
 						if (testCase.getTestCaseId().equals(testId) && !testCase.getStatus().equalsIgnoreCase("Pass") 
 								&& !testCase.getStatus().equalsIgnoreCase("success")
 								&& status.equalsIgnoreCase("Pass") || status.equalsIgnoreCase("success")) {
 							totalPass = totalPass +1;
 						} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("Fail") 
 								&& !testCase.getStatus().equalsIgnoreCase("failure")
 								&& status.equalsIgnoreCase("Fail") || status.equalsIgnoreCase("failure")) {
 							totalFail = totalFail + 1;
 						}  else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("notApplicable") 
 								&& status.equalsIgnoreCase("notApplicable")) {
 							totalNotApplicable = totalNotApplicable + 1;
 						} else if (testCase.getTestCaseId().equals(testId)&& !testCase.getStatus().equalsIgnoreCase("blocked") 
 								&& status.equalsIgnoreCase("blocked")) {
 							totalBlocked = totalBlocked + 1;
 						} 
 						totalTestCases = totalPass + totalFail + notExecuted + totalNotApplicable + totalBlocked;
 		     		}
 			  }
 			 
 			if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
    	     	    OutputStream out=new FileOutputStream(file);
    				createFromFile.save(out);
    				out.close();
      	    }
 			
 			if (StringUtils.isNotEmpty(tstCase.getTestCaseId())) {
 				ODPackage indexSheet = ODPackage.createFromFile(file);
 				SpreadSheet indexSpreadSheet = indexSheet.getSpreadSheet();
 	     		org.jopendocument.dom.spreadsheet.Sheet sheet2 = indexSpreadSheet.getSheet(0);
 	     		List<TestSuite> testSuites = new ArrayList<TestSuite>();
 	     		int indexColCount = sheet2.getColumnCount();
 				int indexRowCount = sheet2.getRowCount();
 				org.jopendocument.dom.spreadsheet.Cell indexCell = null;
 				for(int rowIndex = 3; rowIndex < indexRowCount; rowIndex++) {
 					//Iterating through each column
 					indexCell = sheet2.getCellAt(1, rowIndex);
 					if (indexCell.getValue() != null && indexCell.getValue() != "") {
 						TestSuite testSuite = readDataFromODS(rowIndex, sheet2);
 						testSuites.add(testSuite);
 						if (testSuite.getName().equalsIgnoreCase(sheetName)) {
 							sheet2.getCellAt(2, rowIndex).clearValue();
 							sheet2.setValueAt(totalPass, 2, rowIndex);
 							
 							sheet2.getCellAt(3, rowIndex).clearValue();
 							sheet2.setValueAt(totalFail, 3, rowIndex);
 							
 							sheet2.getCellAt(4, rowIndex).clearValue();
 							sheet2.setValueAt(totalNotApplicable, 4, rowIndex);
 							
 							sheet2.getCellAt(6, rowIndex).clearValue();
 							sheet2.setValueAt(totalBlocked, 6, rowIndex);
 							
 							indexCell = sheet2.getCellAt(7, rowIndex);
 							String textValue = indexCell.getTextValue();
 							double val = 0;
 							if (StringUtils.isNotEmpty(textValue)) {
 								val = Double.parseDouble(textValue);
 							} else {
 								val = totalTestCases;
 								sheet2.setValueAt(val, 7, rowIndex);
 							}
 							
 							sheet2.getCellAt(5, rowIndex).clearValue();
 							int notExe = (int) (val - (totalPass + totalFail + totalNotApplicable + totalBlocked));
 							sheet2.setValueAt(notExe, 5, rowIndex);
 							
 							sheet2.getCellAt(8, rowIndex).clearValue();
 							int total = (int) val;
 							float notExetd= notExe;
 							float testCovrge = (float)((total-notExetd)/total)*100;
 							sheet2.setValueAt(Math.round(testCovrge), 8, rowIndex);
 							break;
 						}
 					}
 				}
 			 	OutputStream out=new FileOutputStream(file);
 			 	indexSheet.save(out);
    				out.close();
 			}
 			
 		} catch (IOException e) {
 		}
 		return testCases;
 	}
 */
 	/*private TestCase readTestCasesFromODS(int nRowIndex,
 			org.jopendocument.dom.spreadsheet.Sheet sheet) {
 		TestCase testcase = new TestCase();
 		org.jopendocument.dom.spreadsheet.Cell cell = null;
 		
 		cell = sheet.getCellAt(1, nRowIndex);
     	String featureId = cell.getTextValue();
     	if(StringUtils.isNotEmpty(featureId)) {
     		testcase.setFeatureId(featureId);
     	}
     	
     	
     	cell = sheet.getCellAt(3, nRowIndex);
     	String testCaseId = cell.getTextValue();
     	if(StringUtils.isNotEmpty(testCaseId)) {
     		testcase.setTestCaseId(testCaseId);
     	}
     	
     	cell = sheet.getCellAt(4, nRowIndex);
     	String description = cell.getTextValue();
     	if(StringUtils.isNotEmpty(description)) {
     		testcase.setDescription(description);
     	}
     	
     	cell = sheet.getCellAt(5, nRowIndex);
     	String testSteps = cell.getTextValue();
     	if(StringUtils.isNotEmpty(testSteps)) {
     		testcase.setSteps(testSteps);
     	}
     	
     	cell = sheet.getCellAt(8, nRowIndex);
     	String expectedResult = cell.getTextValue();
     	if(StringUtils.isNotEmpty(expectedResult)) {
     		testcase.setExpectedResult(expectedResult);
     	}
     	
     	cell = sheet.getCellAt(9, nRowIndex);
     	String actualResult = cell.getTextValue();
     	if(StringUtils.isNotEmpty(actualResult)) {
     		testcase.setActualResult(actualResult);
     	}
     	
     	cell = sheet.getCellAt(10, nRowIndex);
     	String status = cell.getTextValue();
     	if(StringUtils.isNotEmpty(status)) {
     		testcase.setStatus(status);
     	}
     	
     	cell = sheet.getCellAt(13, nRowIndex);
     	String bugComment = cell.getTextValue();
     	if(StringUtils.isNotEmpty(bugComment)) {
     		testcase.setBugComment(bugComment);
     	}
     	return testcase;
 	}
 */
 	private void updateIndex(float totalPass, float totalFail,
 			float totalNotApplicable, float totalBlocked, Row next1) {
 		Cell successCell=next1.getCell(3);
 		int pass = (int)totalPass;
 		successCell.setCellValue(pass);
 		
 		Cell failureCell=next1.getCell(4);
 		int fail = (int)totalFail;
 		failureCell.setCellValue(fail);
 		
 		Cell notAppCell=next1.getCell(5);
 		int notApp = (int)totalNotApplicable;
 		notAppCell.setCellValue(notApp);
 		
 		Cell blockedCell=next1.getCell(7);
 		int blocked = (int)totalBlocked;
 		blockedCell.setCellValue(blocked);
 		
 		Cell cell = next1.getCell(8);
 		double numericCellValue = cell.getNumericCellValue();
 		
 		Cell notExeCell=next1.getCell(6);
 		int notExe = (int) (numericCellValue - (pass + fail + notApp + blocked));
 		notExeCell.setCellValue(notExe);
 		
 		Cell testCovrgeCell=next1.getCell(9);
 		int total = (int) cell.getNumericCellValue();
 		float notExetd= notExe;
 		float testCovrge = (float)((total-notExetd)/total)*100;
 		testCovrgeCell.setCellValue(Math.round(testCovrge));
 	}
 
 	private void readTestFromSheet(com.photon.phresco.commons.model.TestCase tstCase,
 			List<TestCase> testCases, HSSFSheet mySheet) {
 		Iterator<Row> rowIterator = mySheet.rowIterator();
 		 for (int i = 0; i <=23; i++) {
 				rowIterator.next();
 			}
 		 while (rowIterator.hasNext()) {
 			Row next = rowIterator.next();
 			if (StringUtils.isNotEmpty(getValue(next.getCell(1)))) {
 				TestCase createObject = readTest(next);
 				testCases.add(createObject);
 				if (tstCase != null && createObject.getTestCaseId().equals(tstCase.getTestCaseId())) {
 					Cell stepsCell=next.getCell(5);
 					stepsCell.setCellValue(tstCase.getSteps());
 					
 					Cell expectedCell=next.getCell(8);
 					expectedCell.setCellValue(tstCase.getExpectedResult());
 					
 					Cell actualCell=next.getCell(9);
 					actualCell.setCellValue(tstCase.getActualResult());
 					
 					Cell statusCell=next.getCell(10);
 					statusCell.setCellValue(tstCase.getStatus());
 					
 					Cell commentCell=next.getCell(13);
 					commentCell.setCellValue(tstCase.getBugComment());
 				   
 				}
 			}
 		 }
 	}
     
     private TestCase readTest(Row next){
     	TestCase testcase = new TestCase();
     	if(next.getCell(1) != null) {
     		Cell cell = next.getCell(1);
     		String value = getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setFeatureId(value);
     		}
     	}
     	if(next.getCell(3)!=null){
     		Cell cell = next.getCell(3);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setTestCaseId(value);
     		}
     	}
     	if(next.getCell(4)!=null){
     		Cell cell = next.getCell(4);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setDescription(value);
     		}
     	}
     	
     	if(next.getCell(5)!=null){
     		Cell cell=next.getCell(5);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setSteps(value);
     		}
     	}
     	if(next.getCell(8)!=null){
     		Cell cell=next.getCell(8);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setExpectedResult(value);
     		}
     	}
     	if(next.getCell(9)!=null){
     		Cell cell=next.getCell(9);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setActualResult(value);
     		}
     	}
     	if(next.getCell(10)!=null){
     		Cell cell=next.getCell(10);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setStatus(value);
     		}
     	}
     	if(next.getCell(13)!=null){
     		Cell cell=next.getCell(13);
     		String value=getValue(cell);
     		if(StringUtils.isNotEmpty(value)) {
     			testcase.setBugComment(value);
     		}
     	}
     	return testcase;
 	}
     
     private static String getValue(Cell cell) {
     	if (cell != null) {
     		if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
     			return cell.getStringCellValue();
     		}
 
     		if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
     			return String.valueOf(cell.getNumericCellValue());
     		}
     	}
     	
 		return null;
 	}
 	
 	public static String findPlatform() {
 		String osName = System.getProperty(OS_NAME);
 		String osBit = System.getProperty(OS_ARCH);
 		if (osName.contains(WINDOWS)) {
 			osName = WINDOWS;
 		} else if (osName.contains(LINUX)) {
 			osName = LINUX;
 		} else if (osName.contains(MAC)) {
 			osName = MAC;
 		} else if (osName.contains(SERVER)) {
 			osName = SERVER;
 		} else if (osName.contains(WINDOWS7)) {
 			osName = WINDOWS7.replace(" ", "");
 		}
 		if (osBit.contains(OS_BIT64)) {
 			osBit = OS_BIT64;
 		} else {
 			osBit = OS_BIT86;
 		}
 		return osName.concat(osBit);
 	}
 	
 	public void addNew(String filePath, String testName,String cellValue[]) {
 		try {
 			//FileInputStream myInput = new FileInputStream(filePath);
 
 			int numCol;
 			int cellno = 0;
 			CellStyle tryStyle[] = new CellStyle[20];
 			String sheetName = testName;
 			//String cellValue[] = {"","",testName,success, fail,"","","",total,testCoverage,"","",""};
 			Iterator<Row> rowIterator;
 			File testDir = new File(filePath);
       		StringBuilder sb = new StringBuilder(filePath);
    	        if(testDir.isDirectory()) {
        	        	FilenameFilter filter = new PhrescoFileFilter("", "xlsx");
        	        	File[] listFiles = testDir.listFiles(filter);
        	        	if (listFiles.length != 0) {
 						for (File file1 : listFiles) {
 							 if (file1.isFile()) {
 								sb.append(File.separator);
 						    	sb.append(file1.getName());
 						    }
 						}
 						FileInputStream myInput = new FileInputStream(sb.toString());
 						OPCPackage opc=OPCPackage.open(myInput); 
 						
 						XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 						XSSFSheet mySheet = myWorkBook.getSheetAt(0);
 						rowIterator = mySheet.rowIterator();
 						numCol = 13;
 						Row next;
 						for (Cell cell : mySheet.getRow((mySheet.getLastRowNum()) - 2)) {
 							tryStyle[cellno] = cell.getCellStyle();
 							cellno = cellno + 1;
 						}
 						do {
 
 							int flag = 0;
 							next = rowIterator.next();
 							if (mySheet.getSheetName().equalsIgnoreCase("Index")
 									&& ((mySheet.getLastRowNum() - next.getRowNum()) < 3)) {
 								for (Cell cell : next) {
 									cell.setCellType(1);
 									if (cell.getStringCellValue().equalsIgnoreCase("total")) {
 										mySheet.shiftRows((mySheet.getLastRowNum() - 1),
 												(mySheet.getPhysicalNumberOfRows() - 1), 1);
 										flag = 1;
 									}
 									if (flag == 1)
 										break;
 								}
 								if (flag == 1)
 									break;
 							}
 						} while (rowIterator.hasNext());
 
 						Row r = null;
 						if (mySheet.getSheetName().equalsIgnoreCase("Index")) {
 							r = mySheet.createRow(next.getRowNum() - 1);
 
 						} else {
 							r = mySheet.createRow(next.getRowNum() + 1);
 						}
 						for (int i = 0; i < numCol; i++) {
 							Cell cell = r.createCell(i);
 							cell.setCellValue(cellValue[i]);
 							// used only when sheet is 'index'
 							if (i == 2)
 								sheetName = cellValue[i];
 
 							cell.setCellStyle(tryStyle[i]);
 						}
 						if (mySheet.getSheetName().equalsIgnoreCase("Index")) {
 							Sheet fromSheet = myWorkBook.getSheetAt((myWorkBook
 									.getNumberOfSheets() - 1));
 							Sheet toSheet = myWorkBook.createSheet(sheetName);
 							int i = 0;
 							Iterator<Row> copyFrom = fromSheet.rowIterator();
 							Row fromRow, toRow;
 							CellStyle newSheetStyle[] = new CellStyle[20];
 							Integer newSheetType[] = new Integer[100];
 							String newSheetValue[] = new String[100];
 							do {
 								fromRow = copyFrom.next();
 								if (fromRow.getRowNum() == 24) {
 									break;
 								}
 								toRow = toSheet.createRow(i);
 								int numCell = 0;
 								for (Cell cell : fromRow) {
 									Cell newCell = toRow.createCell(numCell);
 
 									cell.setCellType(1);
 
 									newSheetStyle[numCell] = cell.getCellStyle();
 									newCell.setCellStyle(newSheetStyle[numCell]);
 
 									newSheetType[numCell] = cell.getCellType();
 									newCell.setCellType(newSheetType[numCell]);
 									if (fromRow.getCell(0).getStringCellValue().length() != 1
 											&& fromRow.getCell(0).getStringCellValue()
 													.length() != 2
 											&& fromRow.getCell(0).getStringCellValue()
 													.length() != 3) {
 										newSheetValue[numCell] = cell.getStringCellValue();
 										newCell.setCellValue(newSheetValue[numCell]);
 									}
 
 									numCell = numCell + 1;
 								}
 								i = i + 1;
 							} while (copyFrom.hasNext());
 						}
 						// write to file
 						FileOutputStream fileOut = new FileOutputStream(sb.toString());
 						myWorkBook.write(fileOut);
 						myInput.close();
 						fileOut.close();
        	        	} else {
    	                	FilenameFilter xlsFilter = new PhrescoFileFilter("", "xls");
    	     	            File[] xlsListFiles = testDir.listFiles(xlsFilter);
    	     	            if (xlsListFiles.length != 0) {
 		   	     	            for(File file2 : xlsListFiles) {
 		   	     				if (file2.isFile()) {
 		   	     					sb.append(File.separator);
 		   	     			    	sb.append(file2.getName());
 		   	     				}
 		   	     			}
 		   	     			FileInputStream myInput = new FileInputStream(sb.toString());
 		   	     			HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 		
 		   	     			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 		   	     			rowIterator = mySheet.rowIterator();
 			   	     		numCol = 13;
 							Row next;
 							for (Cell cell : mySheet.getRow((mySheet.getLastRowNum()) - 2)) {
 								tryStyle[cellno] = cell.getCellStyle();
 								cellno = cellno + 1;
 							}
 							do {
 	
 								int flag = 0;
 								next = rowIterator.next();
 								if (mySheet.getSheetName().equalsIgnoreCase("Index")
 										&& ((mySheet.getLastRowNum() - next.getRowNum()) < 3)) {
 									for (Cell cell : next) {
 										cell.setCellType(1);
 										if (cell.getStringCellValue().equalsIgnoreCase("total")) {
 											mySheet.shiftRows((mySheet.getLastRowNum() - 1),
 													(mySheet.getPhysicalNumberOfRows() - 1), 1);
 											flag = 1;
 										}
 										if (flag == 1)
 											break;
 									}
 									if (flag == 1)
 										break;
 								}
 							} while (rowIterator.hasNext());
 	
 							Row r = null;
 							if (mySheet.getSheetName().equalsIgnoreCase("Index")) {
 								r = mySheet.createRow(mySheet.getLastRowNum() - 2);
 							} else {
 								r = mySheet.createRow(next.getRowNum() + 1);
 							}
 							for (int i = 0; i < numCol; i++) {
 								Cell cell = r.createCell(i);
 								cell.setCellValue(cellValue[i]);
 								// used only when sheet is 'index'
 								if (i == 2)
 									sheetName = cellValue[i];
 	
 								cell.setCellStyle(tryStyle[i]);
 							}
 							if (mySheet.getSheetName().equalsIgnoreCase("Index")) {
 								Sheet fromSheet = myWorkBook.getSheetAt((myWorkBook
 										.getNumberOfSheets() - 1));
 								Sheet toSheet = myWorkBook.createSheet(sheetName);
 								int i = 0;
 								Iterator<Row> copyFrom = fromSheet.rowIterator();
 								Row fromRow, toRow;
 								CellStyle newSheetStyle[] = new CellStyle[20];
 								Integer newSheetType[] = new Integer[100];
 								String newSheetValue[] = new String[100];
 								do {
 									fromRow = copyFrom.next();
 									if (fromRow.getRowNum() == 24) {
 										break;
 									}
 									toRow = toSheet.createRow(i);
 									int numCell = 0;
 									for (Cell cell : fromRow) {
 										Cell newCell = toRow.createCell(numCell);
 	
 										cell.setCellType(1);
 	
 										newSheetStyle[numCell] = cell.getCellStyle();
 										newCell.setCellStyle(newSheetStyle[numCell]);
 	
 										newSheetType[numCell] = cell.getCellType();
 										newCell.setCellType(newSheetType[numCell]);
 										if (fromRow.getCell(0).getStringCellValue().length() != 1
 												&& fromRow.getCell(0).getStringCellValue()
 														.length() != 2
 												&& fromRow.getCell(0).getStringCellValue()
 														.length() != 3) {
 											newSheetValue[numCell] = cell.getStringCellValue();
 											newCell.setCellValue(newSheetValue[numCell]);
 										}
 	
 										numCell = numCell + 1;
 										if(numCell == 15) {
 											break;
 										}
 									}
 									i = i + 1;
 								} while (copyFrom.hasNext());
 							}
 							// write to file
 							FileOutputStream fileOut = new FileOutputStream(sb.toString());
 							myWorkBook.write(fileOut);
 							myInput.close();
 							fileOut.close();
 	       	        	}/*else {
 	       	        		FilenameFilter odsFilter = new PhrescoFileFilter("", "ods");
 	   	     	            File[] odsListFiles = testDir.listFiles(odsFilter);
 		   	     	        for (File file1 : odsListFiles) {
 								 if (file1.isFile()) {
 									sb.append(File.separator);
 							    	sb.append(file1.getName());
 							    }
 							}
 		   	     	        File file = new File(sb.toString());
 			   	     	    addTestSuiteToOds(file, cellValue);
 	       	        	}*/
    	        	}
    	        }
 		} catch (Exception e) {
 		}
 	}
 
 	/*private static void addTestSuiteToOds(File file, String cellValue[]) throws IOException {
 		org.jopendocument.dom.spreadsheet.Sheet sheet;
 		try {
 			ODPackage createFromFile = ODPackage.createFromFile(file);
 			SpreadSheet spreadSheet = createFromFile.getSpreadSheet();
 			sheet = spreadSheet.getSheet(0);
 			int nColCount = sheet.getColumnCount();
 			int nRowCount = sheet.getRowCount();
 			
 			org.jopendocument.dom.spreadsheet.Cell cell = null;
 			
 			for(int nRowIndex = 3; nRowIndex < nRowCount; nRowIndex++) {
 				cell = sheet.getCellAt(1, nRowIndex);
 				if(cell.getTextValue().equalsIgnoreCase("Total")){
 					sheet.duplicateRows(nRowIndex-1, 1, 1);
 					int rowCount = sheet.getRowCount();
 					for(int i=1; i<12;i++) {
 						sheet.getCellAt(i, rowCount-2).clearValue();
 					}
 					for(int j=1; j<12;j++) {
 						sheet.setValueAt(cellValue[j+1], j, rowCount-2);
 					}
 					org.jopendocument.dom.spreadsheet.Sheet addSheet = spreadSheet.getSheet(spreadSheet.getSheetCount()-1);
 					addSheet.copy(spreadSheet.getSheetCount(), cellValue[2]).setRowCount(25);
 					for(int i =1;i<12;i++){
 						spreadSheet.getSheet(cellValue[2]).getCellAt(i, 24).clearValue();
 					}
 				}
 			}
 			OutputStream out=new FileOutputStream(file);
 			createFromFile.save(out);
 			out.close();
 		} catch(Exception e) {
 			
 		}
 	}*/
    
 	public void addNewTestCase(String filePath, String testSuiteName,String cellValue[], String status) {
 		try {
 			int numCol = 14;
 			int cellno = 0;
 			CellStyle tryStyle[] = new CellStyle[20];
 			//String cellValue[] = {"",featureId,"",testCaseId,testDesc,testSteps,testCaseType,priority,expectedResult,actualResult,status,"","",bugComment};
 			Iterator<Row> rowIterator = null;
 			File testDir = new File(filePath);
       		StringBuilder sb = new StringBuilder(filePath);
    	        if(testDir.isDirectory()) {
        	        	FilenameFilter filter = new PhrescoFileFilter("", "xlsx");
        	        	File[] listFiles = testDir.listFiles(filter);
        	        	if (listFiles.length != 0) {
 						for (File file1 : listFiles) {
 							 if (file1.isFile()) {
 								sb.append(File.separator);
 						    	sb.append(file1.getName());
 						    }
 						}
 						writeTestCasesToXLSX(testSuiteName, cellValue, status, numCol, cellno, tryStyle, sb);
 						
    	        	} else {
    	        		FilenameFilter xlsFilter = new PhrescoFileFilter("", "xls");
      	            File[] xlsListFiles = testDir.listFiles(xlsFilter);
      	           if (xlsListFiles.length != 0) {
 	     	            for(File file2 : xlsListFiles) {
 	     	            	if (file2.isFile()) {
 	     	            		sb.append(File.separator);
 	    	                	sb.append(file2.getName());
 	     	            	}
 	     	            }
 	     	            writeTestCaseToXLS(testSuiteName, cellValue, status, numCol, cellno, tryStyle, sb);
      	           } /*else {
      	        	   	FilenameFilter odsFilter = new PhrescoFileFilter("", "ods");
        	            	File[] odsListFiles = testDir.listFiles(odsFilter);
        	            	if (odsListFiles.length != 0) {
 	  	     	            for(File file2 : odsListFiles) {
 	  	     	            	if (file2.isFile()) {
 	  	     	            		sb.append(File.separator);
 	  	    	                	sb.append(file2.getName());
 	  	     	            	}
 	  	     	            }
 	  	     	            writeTestCasesToODS(testSuiteName, cellValue, sb, status);
        	            	}
      	           }*/
                 }
    	        }
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	/*private void writeTestCasesToODS(String testSuiteName, String[] cellValue, StringBuilder sb, String status) throws PhrescoException {
 		org.jopendocument.dom.spreadsheet.Sheet sheet;
 		File file = new File(sb.toString());
 		try {
 			ODPackage createFromFile = ODPackage.createFromFile(file);
 			SpreadSheet spreadSheet = createFromFile.getSpreadSheet();
 			sheet = spreadSheet.getSheet(testSuiteName);
 			
 			//Get row count and column count
 			int nColCount = sheet.getColumnCount();
 			int nRowCount = sheet.getRowCount();
 			
 			float totalPass = 0;
 			float totalFail = 0;
 			float totalNotApp = 0;
 			float totalBlocked = 0;
 			float notExecuted = 0;
 			float totalTestCases = 0;
 			
 			//Iterating through each row of the selected sheet
 			org.jopendocument.dom.spreadsheet.Cell cell = null;
 			for(int nRowIndex = 24; nRowIndex < nRowCount; nRowIndex++)
 			{
 				//Iterating through each column
 				cell = sheet.getCellAt(1, nRowIndex);
 				if(nRowIndex == 24 && StringUtils.isEmpty(cell.getTextValue())){
 					sheet.duplicateRows(nRowIndex, 1, 1);
 				}
 				if (StringUtils.isNotEmpty(cell.getTextValue())) {
 					org.jopendocument.dom.spreadsheet.Cell cell1 = sheet.getCellAt(10,nRowIndex);
 					String value = cell1.getTextValue();
 					if (StringUtils.isNotEmpty(value)) {
 						if (value.equalsIgnoreCase("pass") || value.equalsIgnoreCase("success")) {
 							totalPass = totalPass + 1;
 						} else if(value.equalsIgnoreCase("fail") || value.equalsIgnoreCase("failure")) {
 							totalFail = totalFail + 1;
 						} else if(value.equalsIgnoreCase("notApplicable")) {
 							totalNotApp = totalNotApp + 1;
 						} else if(value.equalsIgnoreCase("blocked")) {
 							totalBlocked = totalBlocked + 1;
 						} 
 					}else {
 						notExecuted = notExecuted + 1;
 					}
 				}
 				
 				if(StringUtils.isEmpty(cell.getTextValue())) {
 					if(nRowIndex > 24) {
 						sheet.duplicateRows(nRowIndex-1, 1, 1);
 					}
 					for(int i=0; i<13;i++) {
 						sheet.getCellAt(i, nRowIndex).clearValue();
 					}
 					for(int j=1; j<14;j++) {
 						sheet.setValueAt(cellValue[j], j, nRowIndex);
 					}
 					break;
 				}
 				
 			}
 			if (status.equalsIgnoreCase("pass") || status.equalsIgnoreCase("success")) {
 				totalPass = totalPass + 1;
 			} else if (status.equalsIgnoreCase("fail") || status.equalsIgnoreCase("failure")) {
 				totalFail = totalFail + 1;
 			} else if (status.equalsIgnoreCase("notApplicable")) {
 				totalNotApp = totalNotApp + 1;
 			} else if (status.equalsIgnoreCase("blocked")) {
 				totalBlocked = totalBlocked + 1;
 			} else {
 				notExecuted = notExecuted + 1;
 			}
 			totalTestCases = totalPass + totalFail + totalNotApp + totalBlocked + notExecuted;
 			OutputStream out=new FileOutputStream(file);
 			createFromFile.save(out);
 			out.close();
 			
 				ODPackage indexSheet = ODPackage.createFromFile(file);
 				SpreadSheet indexSpreadSheet = indexSheet.getSpreadSheet();
 	     		org.jopendocument.dom.spreadsheet.Sheet sheet2 = indexSpreadSheet.getSheet(0);
 	     		List<TestSuite> testSuites = new ArrayList<TestSuite>();
 	     		int indexColCount = sheet2.getColumnCount();
 				int indexRowCount = sheet2.getRowCount();
 				org.jopendocument.dom.spreadsheet.Cell indexCell = null;
 				for(int rowIndex = 3; rowIndex < indexRowCount; rowIndex++) {
 					//Iterating through each column
 					indexCell = sheet2.getCellAt(1, rowIndex);
 					if (indexCell.getValue() != null && indexCell.getValue() != "") {
 						TestSuite testSuite = readDataFromODS(rowIndex, sheet2);
 						testSuites.add(testSuite);
 						if (testSuite.getName().equalsIgnoreCase(testSuiteName)) {
 							sheet2.getCellAt(2, rowIndex).clearValue();
 							sheet2.setValueAt(totalPass, 2, rowIndex);
 							
 							sheet2.getCellAt(3, rowIndex).clearValue();
 							sheet2.setValueAt(totalFail, 3, rowIndex);
 							
 							sheet2.getCellAt(4, rowIndex).clearValue();
 							sheet2.setValueAt(totalNotApp, 4, rowIndex);
 							
 							sheet2.getCellAt(5, rowIndex).clearValue();
 							sheet2.setValueAt(notExecuted, 5, rowIndex);
 							
 							sheet2.getCellAt(6, rowIndex).clearValue();
 							sheet2.setValueAt(totalBlocked, 6, rowIndex);
 							
 							sheet2.getCellAt(7, rowIndex).clearValue();
 							sheet2.setValueAt(totalTestCases, 7, rowIndex);
 					       			   
 							sheet2.getCellAt(8, rowIndex).clearValue();
 							float testCovrge = (float)((totalTestCases-notExecuted)/totalTestCases)*100;
 							sheet2.setValueAt(Math.round(testCovrge), 8, rowIndex);
 							break;
 						}
 					}
 				}
 			 	OutputStream out1=new FileOutputStream(file);
 			 	indexSheet.save(out1);
    				out.close();
 			
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
 	}
 */
 	private void writeTestCasesToXLSX(String testSuiteName, String[] cellValue,
 			String status, int numCol, int cellno, CellStyle[] tryStyle,
 			StringBuilder sb) throws FileNotFoundException,
 			InvalidFormatException, IOException, UnknownHostException,
 			PhrescoException {
 		Iterator<Row> rowIterator;
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		OPCPackage opc=OPCPackage.open(myInput); 
 		XSSFWorkbook myWorkBook = new XSSFWorkbook(opc);
 		int numberOfSheets = myWorkBook.getNumberOfSheets();
 		for (int j = 0; j < numberOfSheets; j++) {
 			XSSFSheet mySheet = myWorkBook.getSheetAt(j);
 			if(mySheet.getSheetName().equals(testSuiteName)) {
 				rowIterator = mySheet.rowIterator();
 				Row next;
 				for (Cell cell : mySheet.getRow((mySheet.getLastRowNum()) - 2)) {
 					tryStyle[cellno] = cell.getCellStyle();
 					cellno = cellno + 1;
 				}
 				float totalPass = 0;
 				float totalFail = 0;
 				float totalNotApp = 0;
 				float totalBlocked = 0;
 				float notExecuted = 0;
 				float totalTestCases = 0;
 				do {
 					next = rowIterator.next();
 					if (StringUtils.isNotEmpty(getValue(next.getCell(1))) && !getValue(next.getCell(0)).equalsIgnoreCase("S.NO")) {
 						String value = getValue(next.getCell(10));
 						if (StringUtils.isNotEmpty(value)) {
 							if (value.equalsIgnoreCase("pass") || value.equalsIgnoreCase("success")) {
 								totalPass = totalPass + 1;
 							} else if(value.equalsIgnoreCase("fail") || value.equalsIgnoreCase("failure")) {
 								totalFail = totalFail + 1;
 							} else if(value.equalsIgnoreCase("notApplicable")) {
 								totalNotApp = totalNotApp + 1;
 							} else if(value.equalsIgnoreCase("blocked")) {
 								totalBlocked = totalBlocked + 1;
 							} 
 						}else {
 							notExecuted = notExecuted + 1;
 						}
 					}
 				} while (rowIterator.hasNext());
 				//to update the status in the index page 
 				if (status.equalsIgnoreCase("pass") || status.equalsIgnoreCase("success")) {
 					totalPass = totalPass + 1;
 				} else if (status.equalsIgnoreCase("fail") || status.equalsIgnoreCase("failure")) {
 					totalFail = totalFail + 1;
 				} else if (status.equalsIgnoreCase("notApplicable")) {
 					totalNotApp = totalNotApp + 1;
 				} else if (status.equalsIgnoreCase("blocked")) {
 					totalBlocked = totalBlocked + 1;
 				} else {
 					notExecuted = notExecuted + 1;
 				}
 				totalTestCases = totalPass + totalFail + totalNotApp + totalBlocked + notExecuted;
 				XSSFSheet mySheet1 = myWorkBook.getSheetAt(0);
 				rowIterator = mySheet1.rowIterator();
 				 for (int i = 0; i <=2; i++) {
 						rowIterator.next();
 					}
 		            while (rowIterator.hasNext()) {
 		        		Row next1 = rowIterator.next();
 		        		if (StringUtils.isNotEmpty(getValue(next1.getCell(2))) && !getValue(next1.getCell(2)).equalsIgnoreCase("Total")) {
 		        			TestSuite createObject = createObject(next1);
 		                	if (createObject.getName().equals(testSuiteName)) {
 		         				addCalculationsToIndex(
 										totalPass, totalFail,
 										totalNotApp,
 										totalBlocked,
 										notExecuted,
 										totalTestCases, next1);
 		         			}
 		        		}
 		            }
 				
 				Row r = null;
 				if (mySheet.getSheetName().equalsIgnoreCase("Index")) {
 					r = mySheet.createRow(next.getRowNum() - 1);
 				
 				} else {
 					r = mySheet.createRow(next.getRowNum() + 1);
 				}
 				for (int i = 0; i < numCol; i++) {
 					Cell cell = r.createCell(i);
 					cell.setCellValue(cellValue[i]);
 				
 					cell.setCellStyle(tryStyle[i]);
 				}
 				FileOutputStream fileOut = new FileOutputStream(sb.toString());
 				myWorkBook.write(fileOut);
 				myInput.close();
 				fileOut.close();
 			}
 			 	
 		}
 	}
 
 	private void writeTestCaseToXLS(String testSuiteName, String[] cellValue,
 			String status, int numCol, int cellno, CellStyle[] tryStyle,
 			StringBuilder sb) throws FileNotFoundException, IOException,
 			UnknownHostException, PhrescoException {
 		Iterator<Row> rowIterator;
 		FileInputStream myInput = new FileInputStream(sb.toString());
 		HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
 		HSSFSheet mySheet;
 		int numberOfSheets = myWorkBook.getNumberOfSheets();
 		 for (int j = 0; j < numberOfSheets; j++) {
 				HSSFSheet myHssfSheet = myWorkBook.getSheetAt(j);
 				if(myHssfSheet.getSheetName().equals(testSuiteName)) {
 					rowIterator = myHssfSheet.rowIterator();
 					Row next;
 					for (Cell cell : myHssfSheet.getRow((myHssfSheet.getLastRowNum()) - 2)) {
 						tryStyle[cellno] = cell.getCellStyle();
 						cellno = cellno + 1;
 						if(cellno == 16){
 							break;
 						}
 					}
 					float totalPass = 0;
 					float totalFail = 0;
 					float totalNotApp = 0;
 					float totalBlocked = 0;
 					float notExecuted = 0;
 					float totalTestCases = 0;
 					do {
 						next = rowIterator.next();
 						if (StringUtils.isNotEmpty(getValue(next.getCell(1))) && !getValue(next.getCell(0)).equalsIgnoreCase("S.NO")) {
 							String value = getValue(next.getCell(10));
 							if (StringUtils.isNotEmpty(value)) {
 								if (value.equalsIgnoreCase("pass") || value.equalsIgnoreCase("success")) {
 									totalPass = totalPass + 1;
 								} else if(value.equalsIgnoreCase("fail") || value.equalsIgnoreCase("failure")) {
 									totalFail = totalFail + 1;
 								}  else if(value.equalsIgnoreCase("notApplicable")) {
 									totalNotApp = totalNotApp + 1;
 								}  else if(value.equalsIgnoreCase("blocked")) {
 									totalBlocked = totalBlocked + 1;
 								} 
 							}else {
 								notExecuted = notExecuted + 1;
 							}
 						}
 					} while (rowIterator.hasNext());
 					//to update the status in the index page 
 					if (status.equalsIgnoreCase("pass") || status.equalsIgnoreCase("success")) {
 						totalPass = totalPass + 1;
 					} else if (status.equalsIgnoreCase("fail") || status.equalsIgnoreCase("failure")) {
 						totalFail = totalFail + 1;
 					} else if (status.equalsIgnoreCase("notApplicable")) {
 						totalNotApp = totalNotApp + 1;
 					} else if (status.equalsIgnoreCase("blocked")) {
 						totalBlocked = totalBlocked + 1;
 					} else {
 						notExecuted = notExecuted + 1;
 					}
 					totalTestCases = totalPass + totalFail + totalNotApp + totalBlocked + notExecuted;
 					HSSFSheet mySheetHssf = myWorkBook.getSheetAt(0);
 					rowIterator = mySheetHssf.rowIterator();
 					 for (int i = 0; i <=2; i++) {
 							rowIterator.next();
 						}
 		                while (rowIterator.hasNext()) {
 		            		Row next1 = rowIterator.next();
 		            		if (StringUtils.isNotEmpty(getValue(next1.getCell(2))) && !getValue(next1.getCell(2)).equalsIgnoreCase("Total")) {
 		            			TestSuite createObject = createObject(next1);
 		                    	if (createObject.getName().equals(testSuiteName)) {
 			         				addCalculationsToIndex(
 											totalPass, totalFail,
 											totalNotApp,
 											totalBlocked,
 											notExecuted,
 											totalTestCases, next1);
 			         			}
 		            		}
 		                }
 					
 					Row r = null;
 					if (myHssfSheet.getSheetName().equalsIgnoreCase("Index")) {
 						r = myHssfSheet.createRow(next.getRowNum() - 1);
 					
 					} else {
 						r = myHssfSheet.createRow(next.getRowNum() + 1);
 					}
 					for (int i = 0; i < numCol; i++) {
 						Cell cell = r.createCell(i);
 						cell.setCellValue(cellValue[i]);
 						if (tryStyle[i] != null) {
 							cell.setCellStyle(tryStyle[i]);
 						}
 					}
 					FileOutputStream fileOut = new FileOutputStream(sb.toString());
 					myWorkBook.write(fileOut);
 					myInput.close();
 					fileOut.close();
 				}
 		    	 	
 			}
 	}
 
 	private static void addCalculationsToIndex(float totalPass,
 			float totalFail, float totalNotApp, float totalBlocked,
 			float notExecuted, float totalTestCases, Row next1) {
 		Cell successCell=next1.getCell(3);
 		int pass = (int)totalPass;
 		successCell.setCellValue(pass);
 		
 		Cell failureCell=next1.getCell(4);
 		int fail = (int)totalFail;
 		failureCell.setCellValue(fail);
 		
 		Cell notAppCell=next1.getCell(5);
 		int notApp = (int)totalNotApp;
 		notAppCell.setCellValue(notApp);
 		
 		Cell notExeCell=next1.getCell(6);
 		int notExe = (int)notExecuted;
 		notExeCell.setCellValue(notExe);
 		
 		Cell blockedCell=next1.getCell(7);
 		int blocked = (int)totalBlocked;
 		blockedCell.setCellValue(blocked);
 		
 		Cell totalCell=next1.getCell(8);
 		int totalTests = (int)totalTestCases;
 		totalCell.setCellValue(totalTests);
        			   
 		Cell testCovrgeCell=next1.getCell(9);
 		float notExetd= notExe;
 		float testCovrge = (float)((totalTests-notExetd)/totalTests)*100;
 		testCovrgeCell.setCellValue(Math.round(testCovrge));
 	}
 	
 	public static int getHttpsResponse(String url) throws PhrescoException {
 		URL httpsUrl;
 		try {
 			SSLContext ssl_ctx = SSLContext.getInstance("TLS");
 			TrustManager[] trust_mgr = get_trust_mgr();
 			ssl_ctx.init(null, trust_mgr, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());
 			httpsUrl = new URL(url);
 			HttpsURLConnection con = (HttpsURLConnection) httpsUrl.openConnection();
 			con.setHostnameVerifier(new HostnameVerifier() {
 				 // Guard against "bad hostname" errors during handshake.	
 				public boolean verify(String host, SSLSession sess) {
 					return true;
 				}
 			});
 			return con.getResponseCode();
 		} catch (MalformedURLException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (NoSuchAlgorithmException e) {
 			throw new PhrescoException(e);
 		} catch (KeyManagementException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static TrustManager[ ] get_trust_mgr() {
 	     TrustManager[ ] certs = new TrustManager[ ] {
 	        new X509TrustManager() {
 			@Override
 			public void checkClientTrusted(
 					java.security.cert.X509Certificate[] chain, String authType)
 					throws CertificateException {
 				
 			}
 			@Override
 			public void checkServerTrusted(
 					java.security.cert.X509Certificate[] chain, String authType)
 					throws CertificateException {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 	         }
 	      };
 	      return certs;
 	  }
 
 	public static boolean isCharacterExists(String string) {
 		if (string.matches(".*[a-zA-Z0-9]+.*")) {
 			return true;
 		}
 		return false;
 	}
 }
 
 class PhrescoFileFilter implements FilenameFilter {
 	private String name;
 	private String extension;
 
 	public PhrescoFileFilter(String name, String extension) {
 		this.name = name;
 		this.extension = extension;
 	}
 
 	public boolean accept(File directory, String filename) {
 		boolean fileOK = true;
 
 		if (name != null) {
 			fileOK &= filename.startsWith(name);
 		}
 
 		if (extension != null) {
 			fileOK &= filename.endsWith('.' + extension);
 		}
 		return fileOK;
 	}
 }
