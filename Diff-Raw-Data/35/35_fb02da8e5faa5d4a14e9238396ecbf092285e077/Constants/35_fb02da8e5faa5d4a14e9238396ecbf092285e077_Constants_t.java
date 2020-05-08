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

 /**
  * @author ram
  * 
  */
 public class Constants {
	
 
 	/**
 	 * The version that gets printed in the log after booting.
 	 */
	public static final String VERSION = "0.1.0-BUILD-SNAPSHOT";
 	/**
 	 * The build date of the the jar. This might get tweaked by pulling the date
 	 * when maven jars the file.
 	 */
	public static final String BUILD_DATE = "20130829";
 
 	/**
	 * The current directory from where the code runs
	 * if this called from akka (/usr/local/share/megamakka, then that would 
	 * be the user.dir.
 	 */
 	public static String MEGAM_CHEF_ROOT = System.getProperty("user.dir");
	
 	/**
 	 * The user home directory. in unix this is ~ or /home/<user>
	 * Fix in AWS which doesn't allow /root/.megam to be stored. Hence
	 * in cases where we don't have access to user.home, we use user.dir.
 	 */
	public static final String MEGAM_USER_HOME = (new File(System
			.getProperty("user.home")).canWrite()) ? System.getProperty("user.home") : MEGAM_CHEF_ROOT;
 
 	/**
 	 * The location of the app yaml configuration file, relative to
 	 * MEGAM_CHEF_ROOT
 	 */
 	public static String MEGAM_CHEF_APP_YAML = MEGAM_USER_HOME
 			+ java.io.File.separator + ".megam" + java.io.File.separator
 			+ "chefapp.yaml";
 
 	public static final String MEGAM_DEFAULT_CHEF_APP_YAML = MEGAM_CHEF_ROOT
 			+ java.io.File.separator + "conf" + java.io.File.separator
 			+ "chefapp.yaml";
 
 	/**
 	 * The location of the log file
 	 */
 	public static final String MEGAM_CHEF_LOG = MEGAM_CHEF_ROOT
 			+ java.io.File.separator + "logs" + java.io.File.separator
 			+ "megam_chef.log";
 	
 	public static final String HELP_GITHUB = "Refer https://github.com/indykish/megam_chef.git for more info.";
 
 }
