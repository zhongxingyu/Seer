 package org.mosaic.launcher;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.status.ErrorStatus;
 import ch.qos.logback.core.status.Status;
 import ch.qos.logback.core.status.StatusChecker;
 import ch.qos.logback.core.util.StatusPrinter;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import javax.annotation.Nonnull;
 import org.apache.felix.framework.Felix;
 import org.apache.felix.framework.cache.BundleCache;
 import org.apache.felix.framework.util.FelixConstants;
 import org.mosaic.launcher.logging.AppenderRegistry;
 import org.mosaic.launcher.logging.LogbackBuiltinConfigurator;
 import org.mosaic.launcher.logging.LogbackRestrictedConfigurator;
 import org.mosaic.launcher.util.SystemError;
 import org.mosaic.launcher.util.Utils;
 import org.osgi.framework.*;
 import org.osgi.framework.startlevel.FrameworkStartLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import static ch.qos.logback.core.status.StatusUtil.filterStatusListByTimeThreshold;
 import static java.lang.System.currentTimeMillis;
 import static java.nio.file.Files.*;
 import static java.nio.file.StandardOpenOption.READ;
 import static org.mosaic.launcher.logging.EventsLogger.printEmphasizedWarnMessage;
 import static org.mosaic.launcher.util.Header.printHeader;
 import static org.mosaic.launcher.util.SystemError.BootstrapException;
 import static org.mosaic.launcher.util.SystemError.bootstrapError;
 import static org.mosaic.launcher.util.SystemPackages.getExtraSystemPackages;
 import static org.mosaic.launcher.util.Utils.requireClasspathResource;
 import static org.mosaic.launcher.util.Utils.resolveDirectoryInHome;
 
 /**
  * @author arik
  */
 public class MosaicInstance
 {
     private static final Logger LOG = LoggerFactory.getLogger( MosaicInstance.class );
 
     private static final Integer FELIX_CACHE_BUFSIZE = 1024 * 64;
 
     @Nonnull
     private final Properties properties;
 
     @Nonnull
     private final String version;
 
     private final boolean devMode;
 
     @Nonnull
     private final Path home;
 
     @Nonnull
     private final Path apps;
 
     @Nonnull
     private final Path etc;
 
     @Nonnull
     private final Path lib;
 
     @Nonnull
     private final Path logs;
 
     @Nonnull
     private final Path work;
 
     @Nonnull
     private final Path felixWork;
 
     @Nonnull
     private final MosaicInstance.MosaicShutdownHook mosaicShutdownHook = new MosaicShutdownHook();
 
     /**
      * The exact time initialization started.
      */
     private long initializationStartTime;
 
     /**
      * The exact time initialization finished.
      */
     private long initializationFinishTime;
 
     /**
      * The OSGi container instance.
      */
     private Felix felix;
 
     MosaicInstance( @Nonnull Properties properties )
     {
         this.properties = properties;
         this.version = properties.getProperty( "mosaic.version" );
         this.devMode = "true".equalsIgnoreCase( properties.getProperty( "dev" ) );
         this.home = Paths.get( this.properties.getProperty( "mosaic.home" ) );
         this.apps = resolveDirectoryInHome( this.properties, this.home, "apps" );
         this.etc = resolveDirectoryInHome( this.properties, this.home, "etc" );
         this.lib = resolveDirectoryInHome( this.properties, this.home, "lib" );
         this.logs = resolveDirectoryInHome( this.properties, this.home, "logs" );
         this.work = resolveDirectoryInHome( this.properties, this.home, "work" );
         this.felixWork = this.work.resolve( "felix" );
     }
 
     @Nonnull
     public String getVersion()
     {
         return version;
     }
 
     @Nonnull
     public Properties getProperties()
     {
         return properties;
     }
 
     public boolean isDevMode()
     {
         return devMode;
     }
 
     @Nonnull
     public Path getHome()
     {
         return home;
     }
 
     @Nonnull
     public Path getApps()
     {
         return apps;
     }
 
     @Nonnull
     public Path getEtc()
     {
         return etc;
     }
 
     @Nonnull
     public Path getLib()
     {
         return lib;
     }
 
     @Nonnull
     public Path getLogs()
     {
         return logs;
     }
 
     @Nonnull
     public Path getWork()
     {
         return work;
     }
 
     public long getInitializationStartTime()
     {
         return initializationStartTime;
     }
 
     @Nonnull
     public String getInitializationTime()
     {
         long seconds = ( this.initializationFinishTime - this.initializationStartTime ) / 1000;
         return seconds + " seconds";
     }
 
     @Nonnull
     public String getUpTime()
     {
         long seconds = ( currentTimeMillis() - this.initializationFinishTime ) / 1000;
         return seconds + " seconds";
     }
 
     public void start()
     {
         this.initializationStartTime = System.currentTimeMillis();
         this.initializationFinishTime = -1;
 
         printHeader( this );
         prepareHome();
         initializeLogging();
         startFelix();
         try
         {
             installMosaicBundle( "api" );
             installMosaicBundle( "lifecycle" );
             installMosaicBundle( "core" );
         }
         catch( Exception e )
         {
             stop();
             throw e;
         }
 
         // done!
         MosaicInstance.this.initializationFinishTime = currentTimeMillis();
         printEmphasizedWarnMessage( "Mosaic server is running (initialized in {})", getInitializationTime() );
     }
 
     public void stop()
     {
         if( this.felix != null )
         {
             try
             {
                 this.felix.stop();
                 this.felix.waitForStop( 30000 );
             }
             catch( BundleException e )
             {
                 throw new IllegalStateException( "Could not stop OSGi container: " + e.getMessage(), e );
             }
             catch( InterruptedException e )
             {
                 throw new IllegalStateException( "Timed-out while waiting for OSGi container to stop.", e );
             }
             catch( Exception e )
             {
                 throw new IllegalStateException( "Unknown error occurred while stopping Mosaic: " + e.getMessage(), e );
             }
             finally
             {
                 this.felix = null;
                 try
                 {
                     Runtime.getRuntime().removeShutdownHook( this.mosaicShutdownHook );
                 }
                 catch( Exception ignore )
                 {
                 }
             }
         }
     }
 
     private void prepareHome()
     {
         try
         {
             // if dev mode, clean logs and all work dirs
             if( this.devMode )
             {
                 LOG.warn( "Cleaning logs directory..." );
                 if( exists( this.logs ) )
                 {
                     Utils.deleteContents( this.logs );
                 }
 
                 LOG.warn( "Cleaning work directory..." );
                 if( exists( this.work ) )
                 {
                     Utils.deleteContents( this.work );
                 }
             }
             else
             {
                 // not dev mode - just clean felix storage directory
                 if( exists( this.felixWork ) )
                 {
                     Utils.deletePath( this.felixWork );
                 }
             }
         }
         catch( BootstrapException e )
         {
             throw e;
         }
         catch( Exception e )
         {
             throw bootstrapError( "Could not clean temporary work directories: {}", e.getMessage(), e );
         }
 
         try
         {
             createDirectories( this.apps );
             createDirectories( this.etc );
             createDirectories( this.lib );
             createDirectories( this.logs );
             createDirectories( this.work );
         }
         catch( IOException e )
         {
             throw bootstrapError( "Could not create server home directories: {}", e.getMessage(), e );
         }
     }
 
     private void initializeLogging()
     {
         try
         {
             // obtain logger context from Logback
             LoggerContext lc = ( LoggerContext ) LoggerFactory.getILoggerFactory();
             lc.reset();
 
             // disable logback packaging source calculation (causes problems when bundles disappear, on felix shutdown, etc)
             lc.setPackagingDataEnabled( false );
 
             // apply our properties on the logger context so we can use them in logback*.xml files
             for( String propertyName : this.properties.stringPropertyNames() )
             {
                 lc.putProperty( propertyName, this.properties.getProperty( propertyName ) );
             }
 
             // apply built-in & user-customizable configurations
             AppenderRegistry appenderRegistry = new AppenderRegistry();
             applyBuiltinLogbackConfiguration( lc, appenderRegistry );
             applyServerLogbackConfiguration( lc, appenderRegistry );
 
             // install JUL-to-SLF4J adapter
             SLF4JBridgeHandler.removeHandlersForRootLogger();
             SLF4JBridgeHandler.install();
         }
         catch( BootstrapException e )
         {
             throw e;
         }
         catch( Exception e )
         {
             throw bootstrapError( "Could not initialize Mosaic logging framework: {}", e.getMessage(), e );
         }
     }
 
     private void applyBuiltinLogbackConfiguration( @Nonnull LoggerContext lc,
                                                    @Nonnull org.mosaic.launcher.logging.AppenderRegistry appenderRegistry )
     {
         JoranConfigurator configurator = new LogbackBuiltinConfigurator( appenderRegistry );
         configurator.setContext( lc );
         try
         {
             configurator.doConfigure( requireClasspathResource( this.properties, "logbackBuiltin", "/logback-builtin.xml" ) );
             checkLogbackContextForErrors( lc );
         }
         catch( JoranException e )
         {
             throw bootstrapError( "Error while applying built-in Logback configuration: {}", e.getMessage(), e );
         }
     }
 
     private void applyServerLogbackConfiguration( @Nonnull LoggerContext lc,
                                                   @Nonnull AppenderRegistry appenderRegistry )
     {
         Path logbackConfigFile = this.etc.resolve( "logback.xml" );
         if( exists( logbackConfigFile ) )
         {
             try
             {
                 LogbackRestrictedConfigurator configurator = new LogbackRestrictedConfigurator( appenderRegistry );
                 configurator.setContext( lc );
                 configurator.doConfigure( logbackConfigFile.toFile() );
                 checkLogbackContextForErrors( lc );
             }
             catch( JoranException e )
             {
                 throw SystemError.bootstrapError( "Error while applying Logback configuration in '{}': {}", logbackConfigFile, e.getMessage(), e );
             }
         }
     }
 
     private void checkLogbackContextForErrors( @Nonnull LoggerContext lc )
     {
         if( new StatusChecker( lc ).getHighestLevel( 0 ) >= ErrorStatus.WARN )
         {
             System.out.println();
             System.out.printf( "LOGGING CONFIGURATION ERRORS DETECTED:\n" );
             System.out.println();
             System.out.println();
             StatusPrinter.printInCaseOfErrorsOrWarnings( lc );
 
             StringBuilder sb = new StringBuilder();
             for( Status s : filterStatusListByTimeThreshold( lc.getStatusManager().getCopyOfStatusList(), 0 ) )
             {
                 StatusPrinter.buildStr( sb, "", s );
             }
 
             throw SystemError.bootstrapError( "LOGGING CONFIGURATION ERRORS DETECTED:\n" + sb );
         }
     }
 
     private void startFelix()
     {
         // create felix configuration
         Map<Object, Object> felixConfig = new HashMap<>( this.properties );
         felixConfig.put( FelixConstants.FRAMEWORK_STORAGE, this.felixWork.toString() );                 // specify work location for felix
         felixConfig.put( BundleCache.CACHE_BUFSIZE_PROP, FELIX_CACHE_BUFSIZE.toString() );              // buffer size for reading from storage
         felixConfig.put( FelixConstants.LOG_LEVEL_PROP, "0" );                                          // disable Felix logging output (we'll only log OSGi events)
         felixConfig.put( FelixConstants.FRAMEWORK_BEGINNING_STARTLEVEL, "1" );                          // the framework should start at start-level 1
         felixConfig.put( FelixConstants.BUNDLE_STARTLEVEL_PROP, "1" );                                  // boot bundles should start at start-level 1 as well (will be modified later for app bundles)
         felixConfig.put( FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, getExtraSystemPackages() );     // extra packages exported by system bundle
         //        felixConfig.put( FelixConstants.IMPLICIT_BOOT_DELEGATION_PROP, "false" );                       // disable auto-boot delegation
 
         //        felixConfig.put( FelixConstants.FRAMEWORK_BOOTDELEGATION,
         //                         SystemPackages.getSystemPackagesSpecWithoutVersions() );                       // extra packages exported by boot delegation
         try
         {
             // initialize felix
             Felix felix = new Felix( felixConfig );
             felix.init();
 
             // start felix
             felix.start();
             this.felix = felix;
 
             // add a framework listener which logs framework lifecycle events
             this.felix.getBundleContext().addFrameworkListener( new LoggingFrameworkListener() );
 
             // install a shutdown hook to ensure we close the server when the JVM process dies
             Runtime.getRuntime().addShutdownHook( this.mosaicShutdownHook );
 
             // start a thread that monitors felix
             new FrameworkStateMonitor().start();
         }
         catch( BootstrapException e )
         {
             stop();
             throw e;
         }
         catch( Exception e )
         {
             stop();
             throw bootstrapError( "Could not initialize OSGi container: {}", e.getMessage(), e );
         }
     }
 
     private void installMosaicBundle( @Nonnull String fileName )
     {
         installMosaicBundle( fileName, fileName );
     }
 
     private void installMosaicBundle( @Nonnull String fileName, @Nonnull String propertyName )
     {
         Path bundlePath = null;
 
         String location = this.properties.getProperty( "mosaic.boot." + propertyName );
         if( location != null )
         {
             bundlePath = this.home.resolve( location ).normalize().toAbsolutePath();
             verifyInstallableBundle( fileName, bundlePath );
         }
 
        String versionedFilename = fileName + "-" + this.version + ".jar";
         if( bundlePath == null )
         {
             String delim = System.getProperty( "path.separator" );
             StringTokenizer tokenizer = new StringTokenizer( ManagementFactory.getRuntimeMXBean().getClassPath(), delim, false );
             while( tokenizer.hasMoreTokens() )
             {
                 String item = tokenizer.nextToken();
                 if( item.contains( "/" + fileName ) )
                 {
                     bundlePath = Paths.get( item );
                     if( bundlePath.endsWith( "target/classes" ) )
                     {
                        bundlePath = bundlePath.getParent().resolve( versionedFilename );
                     }
                     verifyInstallableBundle( fileName, bundlePath );
                     break;
                 }
             }
         }
 
         if( bundlePath == null )
         {
            bundlePath = this.home.resolve( "boot" ).resolve( versionedFilename ).normalize().toAbsolutePath();
             verifyInstallableBundle( fileName, bundlePath );
         }
 
         String bundleLocation = bundlePath.toString();
         try
         {
             BundleContext bc = this.felix.getBundleContext();
             Bundle bundle = bc.installBundle( bundleLocation, newInputStream( bundlePath, READ ) );
             bundle.start();
         }
         catch( Exception e )
         {
             throw bootstrapError( "Could not install boot bundle at '{}': {}", bundleLocation, e.getMessage(), e );
         }
     }
 
     private void verifyInstallableBundle( @Nonnull String name, @Nonnull Path file )
     {
         if( !exists( file ) )
         {
             throw bootstrapError( "Could not find bundle '{}' at '{}'", name, file );
         }
         else if( !isRegularFile( file ) )
         {
             throw bootstrapError( "Bundle at '{}' is not a file", file );
         }
         else if( !isReadable( file ) )
         {
             throw bootstrapError( "Bundle at '{}' is not readable", file );
         }
     }
 
     private class MosaicShutdownHook extends Thread
     {
         public MosaicShutdownHook()
         {
             super( "Mosaic Shutdown Hook" );
         }
 
         @Override
         public void run()
         {
             try
             {
                 MosaicInstance.this.stop();
             }
             catch( Exception e )
             {
                 LOG.warn( e.getMessage(), e );
             }
         }
     }
 
     private class FrameworkStateMonitor extends Thread
     {
         private FrameworkStateMonitor()
         {
             setName( "FelixMonitor" );
             setDaemon( false );
             setPriority( Thread.MIN_PRIORITY );
         }
 
         @Override
         public void run()
         {
             while( MosaicInstance.this.felix != null )
             {
                 try
                 {
                     int event = MosaicInstance.this.felix.waitForStop( 1000l ).getType();
                     if( event == FrameworkEvent.STOPPED )
                     {
                         // clear Felix reference
                         MosaicInstance.this.felix = null;
 
                         printEmphasizedWarnMessage( "Mosaic system has been stopped (up-time was {})", getUpTime() );
 
                         // if restarting - start a new thread which will call Main again
                         if( Boolean.getBoolean( "mosaic.restarting" ) )
                         {
                             // reset the 'restarting' flag
                             System.setProperty( "mosaic.restarting", "false" );
 
                             // wait a little for things to calm down
                             Thread.sleep( 2000 );
 
                             // start a new thread which will start the server
                             new StartRunnable().start();
                         }
                         return;
                     }
                 }
                 catch( InterruptedException e )
                 {
                     break;
                 }
             }
         }
     }
 
     private class StartRunnable extends Thread
     {
         private StartRunnable()
         {
             setName( "MosaicRunner" );
             setDaemon( false );
             setPriority( Thread.MAX_PRIORITY );
         }
 
         @Override
         public void run()
         {
             MosaicInstance.this.start();
         }
     }
 
     private class LoggingFrameworkListener implements FrameworkListener
     {
         @Override
         public void frameworkEvent( @Nonnull FrameworkEvent event )
         {
             Throwable throwable = event.getThrowable();
             String throwableMsg = throwable != null ? throwable.getMessage() : "";
 
             switch( event.getType() )
             {
                 case FrameworkEvent.INFO:
                     LOG.warn( "OSGi framework informational has occurred: {}", throwableMsg, throwable );
                     break;
 
                 case FrameworkEvent.WARNING:
                     LOG.warn( "OSGi framework warning has occurred: {}", throwableMsg, throwable );
                     break;
 
                 case FrameworkEvent.ERROR:
                     Bundle bundle = event.getBundle();
                     String bstr = bundle == null ? "unknown" : bundle.getSymbolicName() + "-" + bundle.getVersion() + "[" + bundle.getBundleId() + "]";
                     LOG.error( "OSGi framework error for/from bundle '{}' has occurred:", bstr, throwable );
                     break;
 
                 case FrameworkEvent.STARTLEVEL_CHANGED:
                     LOG.info( "OSGi framework start level has been changed to: {}", event.getBundle().adapt( FrameworkStartLevel.class ).getStartLevel() );
                     break;
             }
         }
     }
 }
