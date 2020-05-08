 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client;
 
 import java.io.File;
 
 import net.rptools.maptool.client.ui.zone.PlayerView;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This class provides utility functions for maptool client.
  */
 public class AppUtil {
 
 	private static final String DEFAULT_DATADIR_NAME = ".maptool";
 	public static final String DATADIR_PROPERTY_NAME = "MAPTOOL_DATADIR";
 	
 	private static File dataDirPath;
 	
     /**
      * Returns a File object for USER_HOME
      * if USER_HOME is non-null, otherwise null.
      * 
      * @return the users home directory as a File object
      */
     private static File getUserHome() {
         return new File(System.getProperty("user.home"));
     }
     
     /**
      * Returns a File path that points to the AppHome base directory
      * along with the subpath denoted in the "subdir" argument  
      * <p>
      * For example subdir("cache") will return the path "{APPHOME}/cache
      * <p>
      * As a side-effect the function
      * creates the directory pointed to by File.
      *
      * @param subdir of the maptool home directory
      * @return the maptool data directory name subdir
      * @see getAppHome()
      */
     public static File getAppHome(String subdir) {
 
     	File path = getDataDir();
     	if (!StringUtils.isEmpty(subdir)) {
     		path = new File(path.getAbsolutePath() + "/" + subdir);
     	}

    	if (!path.mkdirs()) {
    		MapTool.showError("msg.error.unableToCreateDataDir");
    	}
     	
         return path;
     }
 
     /**
      * Set the state back to uninitialized
      */
     // Package protected for testing
     static void reset() {
     	dataDirPath = null;
     }
     
     /**  
      * Determine the actual directory to store data files, derived from the environment
      */
     // Package protected for testing
     static File getDataDir() {
     	
     	if (dataDirPath == null) {
     		String path = System.getProperty(DATADIR_PROPERTY_NAME);
     		if (StringUtils.isEmpty(path)) {
     			path = DEFAULT_DATADIR_NAME;
     		}
     		
     		if (path.indexOf("/") < 0 && path.indexOf("\\") < 0) {
     			path = getUserHome() + "/" + path;
     		}
     		
     		dataDirPath = new File(path);
     	}
     	
     	return dataDirPath;
     }
     
     /**
      * Returns a File path representing the base directory to store local data.
      * By default this is a ".maptool" directory in the user's home directory.
      * <p>
      * If you want to change the dir for data storage you can set the system
      * property MAPTOOL_DATADIR.  If the value of the MAPTOOL_DATADIR has any file separator
      * characters in it, it will assume you are using an absolute path.  If the
      * path does not include a file separator it will use it as a subdirectory in the user's 
      * home directory
      * <p>
      * As a side-effect the function
      * creates the directory pointed to by File.
      * 
      * @return the maptool data directory
      */
     public static File getAppHome() {
         return getAppHome("");
     }
     
     /**
      * Returns a File object for
      * the maptool tmp directory, or null
      * if the users home directory could
      * not be determined.
      * 
      * @return the maptool tmp directory
      */
     public static File getTmpDir() {
     	return getAppHome("tmp");
     }
     
     /**
      * Returns true if the player owns the token,
      * otherwise false. If the player is GM this
      * function always returns true. If strict
      * token management is disabled then this
      * function always returns true.
      * 
      * @param token
      * @return true if the player owns the token
      */
     public static boolean playerOwns(Token token) {
 
     	Player player = MapTool.getPlayer();
     	
     	if (player.isGM()) {
     		return true;
     	}
     	
     	if (!MapTool.getServerPolicy().useStrictTokenManagement()) {
     		return true;
     	}
         
     	return token.isOwner(player.getName());
     }
     
     /**
      * Returns true if the token is visible in the zone.
      * If the view is the GM view then this function always
      * returns true.
      * 
      * @param zone to check for visibility
      * @param token to check for visibility in zone
      * @param view to use when checking visibility
      * @return true if token is visible in zone given the view
      */
     public static boolean tokenIsVisible(Zone zone, Token token, PlayerView view) {
     	
     	if (view.isGMView()) {
     		return true;
     	}
     	
     	return zone.isTokenVisible(token);
     }
 }
