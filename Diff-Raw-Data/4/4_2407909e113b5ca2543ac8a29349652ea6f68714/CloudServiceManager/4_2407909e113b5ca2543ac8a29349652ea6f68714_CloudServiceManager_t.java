 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package test.cli.cloudify.cloud.services;
 
 import java.util.HashMap;
 import java.util.Map;
 
import test.cli.cloudify.cloud.services.azure.MicrosoftAzureCloudService;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import test.cli.cloudify.cloud.services.ec2.Ec2WinCloudService;
 import test.cli.cloudify.cloud.services.hp.HpCloudService;
 import test.cli.cloudify.cloud.services.rackspace.RackspaceCloudService;
 
 public class CloudServiceManager {
 
 	private static CloudServiceManager instance = null;
 	
 	private static final Map<String, CloudService> allCloudServices = new HashMap<String, CloudService>();
 
 	private CloudServiceManager() {
 		// Exists only to defeat instantiation.
 	}
 
 	public static CloudServiceManager getInstance() {
 		if (instance == null) {
 			instance = new CloudServiceManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Gets the service instance of the given cloud and with the specified unique name
 	 * @param cloudName The cloudName of the service
 	 * @param uniqueName The unique name of the service
 	 * @return The matching cached CloudService instance, or a new instance of non was cached.
 	 */
 	public CloudService getCloudService(String cloudName) {
 		CloudService cloud = allCloudServices.get(cloudName);
 		if (cloud == null) {
 			allCloudServices.put(cloudName, createCloudService(cloudName));
 			return allCloudServices.get(cloudName);
 		}
 		return cloud;
 	}
 	
 	public void clearCache() {
 		allCloudServices.clear();
 	}
 
 	private CloudService createCloudService(String cloudName) {
 		CloudService cloudService = null;
 
 		if ("byon".equalsIgnoreCase(cloudName)) {
 			cloudService = new ByonCloudService();
 		} else if ("byon-xap".equalsIgnoreCase(cloudName)) {
 			cloudService = new ByonCloudService();
 			((ByonCloudService)cloudService).setNoWebServices(true);
 		} else if ("ec2".equalsIgnoreCase(cloudName)) {
 			cloudService = new Ec2CloudService();
 		} else if ("ec2-win".equalsIgnoreCase(cloudName)) {
 			cloudService = new Ec2WinCloudService();
 		} else if ("hp".equalsIgnoreCase(cloudName)) {
 			cloudService = new HpCloudService();
 		} else if ("rackspace".equalsIgnoreCase(cloudName)) {
 			cloudService = new RackspaceCloudService();
		} else if ("azure".equalsIgnoreCase(cloudName)) {
			cloudService = new MicrosoftAzureCloudService();
 		}
 
 		return cloudService;
 	}
 }
