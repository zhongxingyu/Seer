 /**
  * This file is part of Atomic Tagging.
  * 
  * Atomic Tagging is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Atomic Tagging is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Atomic Tagging. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.atomictagging.core.configuration;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Iterator;
 
 import org.apache.commons.configuration.CombinedConfiguration;
 import org.apache.commons.configuration.HierarchicalINIConfiguration;
 
 /**
  * Representation of the users configuration<br>
  * <br>
  * Core is depending on meaningful values being returned, so it is the applications job to initialize this
  * configuration. The application can however take advantage of the provided {@link #init()} method.
  * 
  * @author Stephan Mann
  */
 public class Configuration {
 
 	private static CombinedConfiguration	conf		= new CombinedConfiguration();
 	private static final String				CONF_FILE	= "atomictagging.conf";
 
 
 	private Configuration() {
 		// Utility class
 	}
 
 
 	/**
 	 * Adds a configuration file to the global configuration.
 	 * 
 	 * @param file
 	 * @throws Exception
 	 */
 	public static void addFile( File file ) throws Exception {
 		conf.addConfiguration( new HierarchicalINIConfiguration( file ) );
 	}
 
 
 	/**
 	 * Returns a copy of the current configuration state.
 	 * 
 	 * @return Current global configuration
 	 */
 	public static CombinedConfiguration get() {
 		return (CombinedConfiguration) conf.clone();
 	}
 
 
 	/**
 	 * Initializes the configuration by trying to load a configuration file from one of the following locations:<br>
 	 * <ul>
 	 * <li>The directory the JAR was executed from</li>
 	 * <li>The users home directory (~/.atomictagging)</li>
 	 * <li>For Linux systems, /etc/atomictagging</li>
 	 * </ul>
 	 * 
 	 * @return Whether at least one configuration was found
 	 */
 	public static boolean init() {
 		boolean foundAnyConfig = false;
 
 		// Local configuration next to the executed JAR.
 		URL locationUrl = Configuration.class.getProtectionDomain().getCodeSource().getLocation();
 		File location = new File( locationUrl.getPath() );
 
 		if ( !location.isDirectory() ) {
 			location = new File( location.getParent() );
 		}
 
 		File localConf = new File( location.getAbsolutePath() + "/" + CONF_FILE );
 		if ( localConf.canRead() ) {
 			try {
 				Configuration.addFile( localConf );
 				foundAnyConfig = true;
 			} catch ( Exception e ) {
 				System.err.println( "Failed to add configuration from " + localConf.getAbsolutePath() );
 				System.err.println( "Cause: " + e.getMessage() );
 				System.err.println( "Trying to proceed without it." );
 			}
 		}
 
 		// User configuration.
 		File userConf = new File( System.getProperty( "user.home" ) + "/.atomictagging/" + CONF_FILE );
 		if ( userConf.canRead() ) {
 			try {
 				Configuration.addFile( userConf );
 				foundAnyConfig = true;
 			} catch ( Exception e ) {
 				System.err.println( "Failed to add configuration from " + userConf.getAbsolutePath() );
 				System.err.println( "Cause: " + e.getMessage() );
 				System.err.println( "Trying to proceed without it." );
 			}
 		}
 
 		// Global configuration.
 		if ( System.getProperty( "os.name" ) == "Linux" ) {
 			File globalConf = new File( "/etc/atomictagging/" + CONF_FILE );
 			if ( globalConf.canRead() ) {
 				try {
 					Configuration.addFile( globalConf );
 					foundAnyConfig = true;
 				} catch ( Exception e ) {
 					System.err.println( "Failed to add configuration from " + globalConf.getAbsolutePath() );
 					System.err.println( "Cause: " + e.getMessage() );
 					System.err.println( "Trying to proceed without it." );
 				}
 			}
 		}
 
 		return foundAnyConfig;
 	}
 
 
 	/**
 	 * Retrieve the value of the remote location option with the given name.
 	 * 
 	 * @param name
 	 * @return The remote location or null, if no location with the given name was found.
 	 */
 	public static String getRepository( String name ) {
 		Iterator<?> remoteItr = Configuration.get().getKeys( "remote" );
 		while ( remoteItr.hasNext() ) {
 			String remoteId = (String) remoteItr.next();
 			if ( name.equals( remoteId.split( "\\." )[1] ) ) {
 				return Configuration.get().getString( remoteId );
 			}
 		}
 
 		return null;
 	}
 
 }
