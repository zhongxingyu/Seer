 /* 
  ** Copyright [2012-2013] [Megam Systems]
  **
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  ** http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 package org.megam.chef;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.megam.chef.exception.BootStrapChefException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static org.megam.chef.Constants.*;
 
 /**
  * @author ram
  * 
  */
 public class BootStrapChef {
 
 	private static BootStrapChef bootSingleton;
 	private static AppYaml bootedYaml;
 	private Logger logger = LoggerFactory.getLogger(BootStrapChef.class);
 
 	/**
 	 * 
 	 * @throws BootStrapChefException
 	 * @throws IOException
 	 */
 	private BootStrapChef() throws BootStrapChefException, IOException {
 		bootedUp();
 		configureRoot();
 		yamlSetup();
 		configure();
 
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws BootStrapChefException
 	 * @throws IOException
 	 */
 	public static BootStrapChef boot() throws BootStrapChefException,
 			IOException {
 		if (bootSingleton == null) {
 			bootSingleton = new BootStrapChef();
 		}
 		return bootSingleton;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public AppYaml yaml() {
 		return bootedYaml;
 	}
 
 	/**
 	 * configure the MEGAM_ROOT_DIRECTORY
 	 */
 	private void configureRoot() {
 		/** MEGAM_ROOT_DIR **/
		logger.debug("user.dir="+System.getProperty("user.dir"));
 		MEGAM_CHEF_ROOT = System.getProperty("user.dir");
 	}
 
 	/**
 	 * 
 	 * @throws BootStrapChefException
 	 *             configure the yaml file
 	 */
 	private void configure() throws BootStrapChefException {
 		logger.debug("Yaml loading.." + MEGAM_CHEF_APP_YAML);
 		AppYamlLoader yaml = new AppYamlLoader(MEGAM_CHEF_APP_YAML);
 		if (yaml.notReady()) {
 			throw new BootStrapChefException(new IllegalArgumentException(
 					"Something wrong in your yaml configuration file located in "
 							+ MEGAM_CHEF_APP_YAML));
 		}
 		bootedYaml = yaml.current();
 		logger.debug(bootedYaml.toString());
 	}
 
 	/*
 	 * Use LoggerFactory, to instantiate a log at the top. Put logging
 	 * statements saying ------------------------- MEGAM CHEF bootedup version :
 	 * build date : ----------------------
 	 */
 	private void bootedUp() {
 		logger.info("------------------------- MEGAM CHEF version : " + VERSION
 				+ "Build Date : " + BUILD_DATE + "----------------------");
 
 	}
 
 	/**
 	 * Copy the default chefapp.yaml to .megam/chefappyaml if one doesn't exist.
 	 * If one exists then, use it.
 	 * 
 	 * @throws BootStrapChefException
 	 */
 	private void yamlSetup() throws BootStrapChefException {
 		try {
 			File file = new File(MEGAM_CHEF_APP_YAML);
 			logger.debug("user.home="+MEGAM_USER_HOME);
			logger.debug("user.dir="+MEGAM_CHEF_ROOT);

 			if (!file.exists()) {
 				String source = MEGAM_DEFAULT_CHEF_APP_YAML;
 				String target = MEGAM_USER_HOME + java.io.File.separator
 						+ ".megam" + java.io.File.separator;
 				logger.warn(file.getAbsolutePath()
 						+ " not found. copying default " + MEGAM_CHEF_APP_YAML
 						+ "to" + target);
 				File sourceFile = new File(source);
 				String name = sourceFile.getName();
 				File targetFile = new File(target + name);
 				logger.info("copying file : " + sourceFile.getName() + " to "
 						+ target);
 				FileUtils.copyFile(sourceFile, targetFile);
 				logger.info("copy of file :" + sourceFile.getName() + " to "
 						+ target + " successful.");
 			}
 		} catch (IOException ioe) {
 			throw new BootStrapChefException(ioe);
 		}
 	}
 }
