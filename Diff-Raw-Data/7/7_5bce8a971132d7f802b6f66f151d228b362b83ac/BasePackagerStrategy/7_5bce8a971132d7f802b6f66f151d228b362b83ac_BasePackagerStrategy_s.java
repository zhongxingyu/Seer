 /*******************************************************************************
  * Copyright 2011 iovation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.javadrop.packaging.impl;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.javadrop.TemplateProcessor;
 import org.javadrop.packaging.PackagerStrategy;
 import org.javadrop.runner.RunnerStrategy;
 
 /**
  * This goal combines a 'runner strategy' and a 'packager strategy' together to produce
  * an artifact that can be installed.
  * 
  * This plugin is intended to keep the configuration in a pom to a minimum. The various
  * strategies could get fairly complex in their processing.
  * 
  */
 public abstract class BasePackagerStrategy  implements PackagerStrategy {
 	/**
 	 * Variables that are used by the packager template(s) to create the final scripts that are needed
 	 * for the final packager solution.
 	 */
 	protected Map<String, String> packagerVariables;
 
 	/**
 	 * Maven mojo log
 	 */
 	private Log _log;
 	
 	private void applyDefaults()
 	{
 		packagerVariables = new HashMap<String, String>();
 		packagerVariables.put("PKG_NAME", "service");
 		packagerVariables.put("PKG_INSTALL_LOC","/usr/local/javadrop/service");
 		packagerVariables.put("PKG_USER", "javadrop");
 		packagerVariables.put("PKG_GROUP", "javadrop");
 		packagerVariables.put("PKG_USERID", "55");
 		packagerVariables.put("PKG_GROUPID", "700");
 		
 		packagerVariables.put("RUNNER_NAME", "service");
 		packagerVariables.put("RUNNER_INSTALL_LOC","/usr/local/javadrop/service");
 		packagerVariables.put("RUNNER_USER", "javadrop");
 		packagerVariables.put("RUNNER_GROUP", "javadrop");
 	}
 	
 	
 	/**
 	 * Check the values supplied in the pom to see if they are ok.
 	 * @throws MojoExecutionException
 	 */
 	@Override
 	public void applyParameters(Map<String, String> paramMap)// throws MojoExecutionException
 	{
 		applyDefaults();
 		for (Map.Entry<String,String> entry : paramMap.entrySet()) {
 			packagerVariables.put(entry.getKey(), entry.getValue());
 		}
 	}
 	
 	
 	@Override
 	public void processTemplates(RunnerStrategy runner,
 			TemplateProcessor processor, File workingDirectory) throws MojoExecutionException {
 		
 		// Go through and create all the destination locations.
 		if (!workingDirectory.exists()) {
 			workingDirectory.mkdirs();
 		}
 			
 		Map<File, File> conversionFiles = runner.getConversionFiles(workingDirectory);
 	
 		Map<String, String> templateParameters = new HashMap<String,String>();
 		templateParameters.putAll(packagerVariables);
 		templateParameters.putAll(runner.getParameters());
 		conversionFiles.putAll(getConversionFiles(workingDirectory, runner));
 		for (Entry<File, File> cfile : conversionFiles.entrySet()) {
			processor.applyVTemplate(cfile.getKey(), cfile.getValue(), templateParameters);
 		}
 	}
 
 	/**
 	 * Returns the install location
 	 * 
 	 * @return Where the package is to be installed... As a string.
 	 */
 	protected String getInstallLoc() {
 		return packagerVariables.get("PKG_INSTALL_LOC");
 	}
 
 
 	public void set_log(Log _log) {
 		this._log = _log;
 	}
 
 
 	public Log get_log() {
 		return _log;
 	}
 		
 	protected String getGroup() {
 	    return packagerVariables.get("PKG_GROUP");
 	}
 	
 	protected int getGid() {
 	    int gid = Integer.parseInt(packagerVariables.get("PKG_GROUPID"));
 	    return gid;
 	}
 	protected String getUser() {
 	    return packagerVariables.get("PKG_USER");
 	}
 	protected int getUid() {
 	    int userid = Integer.parseInt(packagerVariables.get("PKG_USERID"));
 	    return userid;
 	}
 }
