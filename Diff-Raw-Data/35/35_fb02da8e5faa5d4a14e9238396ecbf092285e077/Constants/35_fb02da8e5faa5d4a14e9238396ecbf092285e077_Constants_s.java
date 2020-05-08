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
 
 /**
  * @author ram
  * 
  */
 public class Constants {
 
 	/**
 	 * The version that gets printed in the log after booting.
 	 */
	public static final String VERSION = "1.0.0-BUILD-SNAPSHOT";
 	/**
 	 * The build date of the the jar. This might get tweaked by pulling the date
 	 * when maven jars the file.
 	 */
	public static final String BUILD_DATE = "20130523";
 
 	/**
	 * The root directory from where the code runs
 	 */
 	public static String MEGAM_CHEF_ROOT = System.getProperty("user.dir");
 	/**
 	 * The user home directory. in unix this is ~ or /home/<user>
 	 */
	public static final String MEGAM_USER_HOME = System
			.getProperty("user.home");
 
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
