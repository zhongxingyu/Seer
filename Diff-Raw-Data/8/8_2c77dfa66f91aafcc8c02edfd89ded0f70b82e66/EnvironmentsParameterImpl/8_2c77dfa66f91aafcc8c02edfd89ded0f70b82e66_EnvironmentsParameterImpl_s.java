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
 package com.photon.phresco.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 
 public class EnvironmentsParameterImpl implements DynamicParameter, Constants {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public PossibleValues getValues(Map<String, Object> paramsMap) throws IOException, ParserConfigurationException, SAXException, ConfigurationException, PhrescoException {
 		String rootModulePath = "";
 		String subModuleName = "";
 		PossibleValues possibleValues = new PossibleValues();
 		ApplicationInfo applicationInfo = (ApplicationInfo) paramsMap.get(KEY_APP_INFO);
 		MojoProcessor mojo = (MojoProcessor) paramsMap.get(KEY_MOJO);
 		String goal = (String) paramsMap.get(KEY_GOAL);
 		Parameter parameter = mojo.getParameter(goal, KEY_ENVIRONMENT);
 		String updateDefaultEnv = "";
 		String rootModule = (String) paramsMap.get(KEY_ROOT_MODULE);
 		
 		if (StringUtils.isNotEmpty(rootModule)) {
 			rootModulePath = Utility.getProjectHome() + rootModule;
 			subModuleName = applicationInfo.getAppDirName();
 		} else {
 			rootModulePath = Utility.getProjectHome() + applicationInfo.getAppDirName();
 		}
 		if (paramsMap != null) {
 			String showSettings = (String) paramsMap.get(KEY_SHOW_SETTINGS);
 			if (Boolean.parseBoolean(showSettings)) {
 				String projectCode = (String) paramsMap.get(KEY_PROJECT_CODE);
 				String settingsPath = getSettingsPath(projectCode);
 				ConfigManager configManager = new ConfigManagerImpl(new File(settingsPath)); 
 				List<Environment> environments = configManager.getEnvironments();
 				for (Environment environment : environments) {
 					Value value = new Value();
 					value.setValue(environment.getName());
 					possibleValues.getValue().add(value);
 					if(environment.isDefaultEnv()) {
 						updateDefaultEnv = environment.getName();
 					}
 				}
 			}
 		}
 
 		if (applicationInfo != null) {
 			String configPath = getConfigurationPath(subModuleName, rootModulePath).toString();
 			ConfigManager configManager = new ConfigManagerImpl(new File(configPath)); 
 			List<Environment> environments = configManager.getEnvironments();
 			for (Environment environment : environments) {
 				Value value = new Value();
 				value.setValue(environment.getName());
 				possibleValues.getValue().add(value);
 				if(environment.isDefaultEnv()) {
 					updateDefaultEnv = environment.getName();
 				}
 			}
 		}
 
 		if (parameter != null && StringUtils.isEmpty(parameter.getValue())) {
 			parameter.setValue(updateDefaultEnv);
 			mojo.save();
 		}
 
 		return possibleValues;
 	}
     
     private StringBuilder getConfigurationPath(String subModuleName, String rootModulePath) throws PhrescoException {
     	 String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModuleName);
     	 StringBuilder builder = new StringBuilder(dotPhrescoFolderPath);
 		 builder.append(File.separator);
 		 builder.append(CONFIGURATION_INFO_FILE);
 		 return builder;
 	 }
     
     private String getSettingsPath(String projectCode) {
    	return Utility.getProjectHome() + projectCode +"-settings.xml";
     }
 }
