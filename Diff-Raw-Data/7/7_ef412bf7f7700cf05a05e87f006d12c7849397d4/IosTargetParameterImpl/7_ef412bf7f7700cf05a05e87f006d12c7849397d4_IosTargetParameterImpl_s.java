 package com.photon.phresco.framework.param.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.FileListFilter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.util.PomProcessor;
 
 public class IosTargetParameterImpl implements DynamicParameter {
 
     private static final String IPHONE_XCODE_WORKSPACE_PROJ_EXTN = "xcworkspace";
     private static final String IPHONE_XCODE_PROJ_EXTN = "xcodeproj";
     private static final String SCHEMES = "Schemes";
     private static final String XCODEBUILD_LIST_WORKSPACE_CMD = "xcodebuild -list ";
     private static final String IPHONE_XCODE_WORKSPACE = "-workspace ";
     private static final String IPHONE_XCODE_PROJECT = "-project ";
     private static final String XCODE_PROJECT_TARGETS = "Targets:";
     private static final String XCODE_WORKSPACE_TARGETS = "Schemes:";
 
     private static final String POM = "pom.xml";
     
     @Override
     public PossibleValues getValues(Map<String, Object> paramMap) throws IOException, ParserConfigurationException, 
              SAXException, ConfigurationException, PhrescoException {
         PossibleValues possibleValues = new PossibleValues();
         try {
             ApplicationInfo applicationInfo = (ApplicationInfo) paramMap.get(KEY_APP_INFO);
             String appDirName = applicationInfo.getAppDirName();
             StringBuilder builder = new StringBuilder(Utility.getProjectHome());
             builder.append(appDirName);
             builder.append(File.separatorChar);
             
             File pomPath = new File(builder.toString(), POM);
             PomProcessor pomProcessor = new PomProcessor(pomPath);
             File sourceDir = null;
             String sourceDirectory = pomProcessor.getSourceDirectory();
 			if (sourceDirectory.startsWith("${project.basedir}")) {
 				sourceDirectory = sourceDirectory.substring("${project.basedir}".length());
             	sourceDir = new File(builder.toString() + sourceDirectory);
 			} else if (sourceDirectory.startsWith("/source")) {
 				sourceDir = new File(builder.toString() + File.separatorChar + sourceDirectory);
 			} else {
 				sourceDir = new File(sourceDirectory);
 			}
             
             FilenameFilter filter = new FileListFilter("", IPHONE_XCODE_WORKSPACE_PROJ_EXTN, IPHONE_XCODE_PROJ_EXTN);
             File[] listFiles = sourceDir.listFiles(filter);
             
             StringBuilder cmdBuilder = new StringBuilder();
             cmdBuilder.append(XCODEBUILD_LIST_WORKSPACE_CMD);
             if (listFiles[0].getName().contains(IPHONE_XCODE_WORKSPACE_PROJ_EXTN)) {
                 cmdBuilder.append(IPHONE_XCODE_WORKSPACE);
             } else if (listFiles[0].getName().contains(IPHONE_XCODE_PROJ_EXTN)) {
                 cmdBuilder.append(IPHONE_XCODE_PROJECT);
             }
             cmdBuilder.append(listFiles[0].getName());
             
             Commandline cl = new Commandline(cmdBuilder.toString());
             cl.setWorkingDirectory(sourceDir);
             Process p = cl.execute();
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
             String line = null;
             boolean isTarget = false;
             
             while ((line = reader.readLine()) != null) {       
                 if (line.trim().equals(XCODE_PROJECT_TARGETS) || line.trim().equals(XCODE_WORKSPACE_TARGETS)) { // getting only target
                    isTarget = true;
                 } else if (line.trim().contains(":")) { // omitt all other configurations
                     isTarget = false;
                 }
                     
                 if (isTarget && StringUtils.isNotEmpty(line) && !line.trim().equals(XCODE_PROJECT_TARGETS) && !line.trim().equals(XCODE_WORKSPACE_TARGETS)) {
                     Value value = new Value();
                     value.setValue(line.trim());
                     value.setKey(line.trim());
                     possibleValues.getValue().add(value);
                 }
             }
         
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
 
         return possibleValues;
     }
 
 }
