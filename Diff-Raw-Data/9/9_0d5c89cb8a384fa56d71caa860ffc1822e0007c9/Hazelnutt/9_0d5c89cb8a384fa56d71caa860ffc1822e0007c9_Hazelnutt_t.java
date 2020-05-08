 /**
  * This file is part of Hazelnutt.
  * 
  * Hazelnutt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Hazelnutt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Hazelnutt.  If not, see <http://www.gnu.org/licenses/>.
  */
 package es.darkhogg.hazelnutt;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javax.swing.LookAndFeel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.DailyRollingFileAppender;
 import org.apache.log4j.EnhancedPatternLayout;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.varia.NullAppender;
 
 import es.darkhogg.util.Version;
 
 public final class Hazelnutt {
 	
 	/**
 	 * Version of the program
 	 */
	private static final Version VERSION = new Version( 1, 0, 8, 2 );
 	
 	/**
 	 * Logger for the whole application
 	 */
 	private static final Logger LOGGER;
 	static {
 		Logger.getRootLogger().addAppender( NullAppender.getNullAppender() );
 		
 		try {
 			LOGGER = Logger.getLogger( Hazelnutt.class );
 			LOGGER.addAppender( new ConsoleAppender(
 				new EnhancedPatternLayout( "%d{HH:mm:ss.SSS} %5p : %m%n" ),
 				ConsoleAppender.SYSTEM_OUT
 			) );
 			LOGGER.addAppender( new DailyRollingFileAppender(
 				new EnhancedPatternLayout( "%d{HH:mm:ss.SSS} %5p : %m%n" ),
 				"log" + System.getProperty( "file.separator" ) + "Hazelnutt.log",
 				"'.'yyyy-MM-dd"
 			) );
 			//LOGGER.addAppender( new SwingPanelAppender( FRAME.getLogPanel(), true ) );
 			LOGGER.setLevel( Level.TRACE );
 		} catch ( IOException e ) {
 			throw new RuntimeException( e );
 		}
 	}
 	
 	/**
 	 * Configuration for the whole application
 	 */
 	private static final Configuration CONFIG;
 	static {
 		File cfgFile = new File( "./Hazelnutt.properties" );
 		PropertiesConfiguration cfg = null;
 		try {
 			cfg = new PropertiesConfiguration( cfgFile );
 		} catch ( ConfigurationException e ) {
 			throw new RuntimeException( e );
 		}
 
 		cfg.setAutoSave( true );
 		
 		CONFIG = cfg;
 	}
 	
 	/**
 	 * Main frame of the program
 	 */
 	private static final EditorFrame FRAME;
 	static {
 		FRAME = new EditorFrame();
 	}
 	
 	/**
 	 * Returns the current version of the application as an integer, where each
 	 * byte is a version component.
 	 * 
 	 * @return This application version
 	 */
 	public static Version getVersion () {
 		return VERSION;
 	}
 	
 	/**
 	 * Returns an already initialized and configured Logger for the whole
 	 * application.
 	 * 
 	 * @return This application logger
 	 */
 	public static Logger getLogger () {
 		return LOGGER;
 	}
 	
 	/**
 	 * Returns an already created and initialized JFrame which is the main
 	 * frame for this application.
 	 * 
 	 * @return This application main frame
 	 */
 	public static EditorFrame getFrame () {
 		return FRAME;
 	}
 	
 	/**
 	 * Returns an already created, loaded and ready to be used Configuration
 	 * for the whole application.
 	 * 
 	 * @return This application configuration object
 	 */
 	public static Configuration getConfiguration () {
 		return CONFIG;
 	}
 	
 	/**
 	 * Tries to restart the application in at most <i>time</i> milliseconds for
 	 * every alive thread.
 	 * 
 	 * @param time Number of milliseconds to wait for each thread to terminate
 	 * @return If something have happened before terminating the application
 	 */
 	public static boolean restart ( long time ) {
 		Logger logger = getLogger();
 		logger.info( "Trying to restart application..." );
 		
 		// Get the path to java executable
 		File javaBinDir = new File( System.getProperty( "java.home" ), "bin" );
 		logger.debug( "Java directory: '" + javaBinDir.getAbsolutePath() + "'" );
 		
 		File[] possiblePaths = {
 			new File( javaBinDir, "javaw.exe" ),
 			new File( javaBinDir, "java.exe" ),
 			new File( javaBinDir, "java" )
 		};
 		File javaPath;
 		int i = 0;
 		do {
 			javaPath = possiblePaths[ i ];
 			i++;
 		} while ( i < possiblePaths.length && !javaPath.exists() );
 		logger.debug( "Java executable: '" + javaPath.getAbsolutePath() + "'" );
 		
 		// Get path to JAR
 		File jarFile = null;
 		try {
 			jarFile = new File( Hazelnutt.class.getProtectionDomain()
 				.getCodeSource().getLocation().toURI() );
 		} catch ( URISyntaxException e ) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		String[] command = null;
 		if ( !jarFile.isDirectory() ) {
 			logger.debug( "JAR file: '" + jarFile.getAbsolutePath() + "'" );
 			command = new String[]{
 				javaPath.getPath(),
 				"-jar",
 				jarFile.getPath()
 			};
 		} else {
 			File classDir = jarFile;
 			logger.debug( "Class path: '" + classDir.getAbsolutePath() + "'" );
 			command = new String[]{
 				javaPath.getPath(),
 				"-classpath",
 				jarFile.getPath() + ";.;" + System.getProperty( "java.class.path" ),
 				Hazelnutt.class.getName()
 			};
 			System.out.println( "\"" + command[0] + "\" \"" + command[1] + "\" \"" + command[2] + "\" \"" + command[3] + "\"" );
 		}
 		
 		// Execute
 		try {
 			Runtime.getRuntime().exec( command );
 		} catch ( IOException e ) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		// Terminate this application
 		terminate( time );
 		return true;
 	}
 	
 	/**
 	 * Terminates the application in at most <i>time</i> milliseconds for
 	 * every alive thread. 
 	 * 
 	 * @param time Number of milliseconds to wait for each thread to terminate
 	 */
 	public static void terminate ( long time ) {
 		Logger logger = getLogger();
 		logger.info( "Terminating application..." );
 		
 		try {
 			getFrame().dispose();
 			
 			// Get the root thread group
 			ThreadGroup rootThreadGroup = Thread.currentThread().getThreadGroup();
 			while ( rootThreadGroup.getParent() != null ) {
 				rootThreadGroup = rootThreadGroup.getParent();
 			}
 			
 			// Declare some collections
 			Queue<ThreadGroup> threadGroups = new LinkedList<ThreadGroup>();
 			Queue<Thread> threads = new LinkedList<Thread>();
 			
 			// Get ALL groups
 			threadGroups.add( rootThreadGroup );
 			while ( !threadGroups.isEmpty() ) {
 				ThreadGroup group = threadGroups.remove();
 				
 				Thread[] subThreads = new Thread[ group.activeCount()*2 ];
 				for ( Thread subThread : subThreads ) {
 					if ( subThread != null ) {
 						threads.add( subThread );
 					}
 				}
 				
 				ThreadGroup[] subThreadGroups = new ThreadGroup[ group.activeGroupCount()*2 ];
 				for ( ThreadGroup subThreadGroup : subThreadGroups ) {
 					if ( subThreadGroup != null ) {
 						threadGroups.add( subThreadGroup );
 					}
 				}
 			}
 			
 			// Join a maximum of time milliseconds for all non-daemon threads
 			while ( !threads.isEmpty() ) {
 				Thread thread = threads.remove();
 				LOGGER.trace( thread );
 				
 				if ( !thread.isDaemon() && thread != Thread.currentThread() ) {
 					logger.trace( "Waiting for thread '" + thread.getName() + "'" );
 					thread.join( time );
 					if ( thread.isAlive() ) {
 						logger.trace( "Interrupting thread '" + thread.getName() + "'" );
 						thread.interrupt();
 					}
 				}
 			}
 		
 		} catch ( Throwable e ) {
 			e.printStackTrace();
 		} finally {
 			// Exit the program
 			System.exit( 0 );
 		}
 	}
 	
 	/**
 	 * Runs the application
 	 * 
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main ( String[] args )
 	throws Exception {
 		// Print some version information
 		LOGGER.log( Level.OFF, "----------------" );
 		LOGGER.info( "Hazelnutt " + VERSION );
 		
 		LOGGER.trace( "Selecting Look&Feel..." );
 		
 		// Select the L&F from configuration or the default if not present
 		String slaf = CONFIG.getString( "Hazelnutt.gui.lookAndFeel" );
 		if ( slaf == null ) {
 			LOGGER.info( "Configuration entry for L&F missing, creating default" );
 			slaf = UIManager.getSystemLookAndFeelClassName();
 		}
 		
 		// Set it or print an error
 		try {
 			UIManager.setLookAndFeel( slaf );
 		} catch ( Exception e ) {
 			LOGGER.warn( "Error while selecting the L&F \"" + slaf +
 				"\", leaving default" );
 		}
 		
 		// Update the configuration with the currently selected L&F
 		LookAndFeel laf = UIManager.getLookAndFeel();
 		LOGGER.debug( "L&F selected: " + laf.getName() +
 			" (" + laf.getClass().getName() + ")" );
 		CONFIG.setProperty( 
 			"Hazelnutt.gui.lookAndFeel",
 			laf.getClass().getName()
 		);
 		
 		// Load the frame
 		LOGGER.trace( "Launching main frame..." );
 		SwingUtilities.invokeLater( new Runnable () {
 			@Override public void run () {
 				SwingUtilities.updateComponentTreeUI( FRAME );
 				FRAME.setVisible( true );
 			}
 		});
 	}
 }
