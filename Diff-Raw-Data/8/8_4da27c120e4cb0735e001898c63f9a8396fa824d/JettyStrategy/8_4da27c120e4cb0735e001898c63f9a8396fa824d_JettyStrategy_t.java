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
 package org.javadrop.runner.impl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 /**
  * This implementation of the service strategy is designed to jetty web
  * services (with a jetty.sh run script)
  * 
  * @author gcooperpdx
  *
  */
 public class JettyStrategy extends BaseRunnerStrategy {
 	
 	@Override
 	public Map<File, File> getConversionFiles(File outputDirectory) {
 		Map<File,File> conversionFiles = new HashMap<File, File>(); //super.getConversionFiles(outputDirectory, serviceName);
 
 		conversionFiles.put(new File(getPrefix() + File.separator + "bin" + File.separator + "jetty_sh.vm"),
 				new File(outputDirectory + File.separator + "runners" +File.separator + "bin" + File.separator + getServiceName() + ".sh"));
 		
 		conversionFiles.put(new File(getPrefix() + File.separator + "init.d" + File.separator + "service_template_main.vm"),
 				new File(outputDirectory + File.separator + "runners" + File.separator + "init.d" + File.separator + getServiceName()));
 		
 		conversionFiles.put(new File(getPrefix() + File.separator + "jettyconf" + File.separator + "env.vm"),
 				new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + "env"));
 		
 		conversionFiles.put(new File(getPrefix() + File.separator + "jettyconf" + File.separator + "jetty-spring_xml.vm"),
 				new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + "jetty-spring.xml"));
 		
 		conversionFiles.put(new File(getPrefix() + File.separator + "jettyconf" + File.separator + "log4j_xml.vm"),
 				new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + "log4j.xml"));
 		
 		conversionFiles.put(new File(getPrefix() + File.separator + "jettyconf" + File.separator + "jetty-webdefault.xml"),
 				new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + "jetty-webdefault.xml"));
 		
 		return conversionFiles;
 	}
 
 	@Override
 	public Map<File, Collection<File>> getInstallSet(File workingDirectory) {
 		// TODO Eliminate this synchronization bs.
 		Map<File, Collection<File>> installSet = super.getInstallSet(workingDirectory);
 		Collection<File> installFiles = new ArrayList<File>();
 		installFiles.add(new File(getServiceName() + ".sh"));
 		installSet.put(new File("runners" + File.separator + "bin"), installFiles);
 		
 		installFiles = new ArrayList<File>();
 		installFiles.add(new File(getServiceName()));
 		installSet.put(new File("runners" + File.separator + "init.d"), installFiles);
 		
 		installFiles = new ArrayList<File>();
		installFiles.add(new File("runners" + File.separator + "env"));
 		installFiles.add(new File("runners" + File.separator + "jetty-spring.xml"));
 		installFiles.add(new File("runners" + File.separator + "log4j.xml"));
 		installFiles.add(new File("runners" + File.separator + "jetty-ebdefault.xml"));
 		installSet.put(new File("runners" + File.separator + "conf"), installFiles);
 		
 		return installSet;
 	}
 
 	private String getPrefix() {
 		return "org" + File.separator + "javadrop" + File.separator + "runnerstrategy" + File.separator + "services";
 	}
 	
 	protected void applyDefaults() {
 		super.applyDefaults();
 
 		// Assign appropriate defaults to common variables
 		runnerVariables.put("SVC_CONTEXT_NAME", "test-service-facade");
 	}
 
 	protected String getServiceName() {
 		String jettyName = runnerVariables.get("JTY_NAME");
 		if (jettyName == null) {
 			get_log().error("No Jetty service name specified (JTY_NAME).");
 		}
 		return jettyName;
 	}
 }
