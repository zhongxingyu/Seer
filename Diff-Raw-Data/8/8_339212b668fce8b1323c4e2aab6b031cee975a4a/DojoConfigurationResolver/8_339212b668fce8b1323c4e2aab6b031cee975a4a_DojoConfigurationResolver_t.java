 /*
  * Copyright 2012 the original author or authors.
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
  */
 package com.evinceframework.web.dojo.mvc.view.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 
  * 
  * @author Craig Swing
  */
 public class DojoConfigurationResolver {
 	
 	public static final String DEFAULT_DJC_PARAM_NAME = "_djc";
 	
 	private String parameterName = DEFAULT_DJC_PARAM_NAME;
 	
 	private DojoConfiguration defaultConfiguration;
 	
 	private Map<String, DojoConfiguration> configurations = new HashMap<String, DojoConfiguration>();
 				
 	public String getParameterName() {
 		return parameterName;
 	}
 
 	public void setParameterName(String parameterName) {
 		this.parameterName = parameterName;
 	}
 
 	public DojoConfiguration getDefaultConfiguration() {
 		return defaultConfiguration;
 	}
 
 	public void setDefaultConfiguration(DojoConfiguration defaultConfiguration) {
 		this.defaultConfiguration = defaultConfiguration;
 	}
 
 	public Map<String, DojoConfiguration> getConfigurations() {
 		return configurations;
 	}
 
 	public void setConfigurations(Map<String, DojoConfiguration> configurations) {
 		this.configurations = configurations;
 	}
 
 	public DojoConfiguration resolve(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
 		
 		Object objDjc = request.getParameterMap().get(parameterName);
		if (objDjc != null && objDjc instanceof String[] && ((String[])objDjc).length > 0) {
			String key = ((String[])objDjc)[0];
			DojoConfiguration config = configurations.get(key);
 			if (config != null) {
				request.getSession().setAttribute(parameterName, key);
 				return config;
 			}
 		}
 				
 		objDjc = request.getSession().getAttribute(parameterName);
 		if (objDjc != null) {
 			DojoConfiguration config = configurations.get(objDjc);
 			if (config != null) {
 				return config;
 			}
 		}
 		
 		return defaultConfiguration;
 	}
 	
 }
