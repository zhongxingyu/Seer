 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.system.shell;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Predicate;
 import novelang.DirectoryFixture;
 import novelang.system.Husk;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 import novelang.system.TcpPortBooker;
 import org.apache.commons.io.FileUtils;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.NameAwareTestClassRunner;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.ImmutableList.of;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.fail;
 
 /**
  * @author Laurent Caillette
  */
 @RunWith( NameAwareTestClassRunner.class )
 public class JavaShellTest {
 
   @Test
   public void getTheOfficialJar() throws IOException {
     if( isLikelyToWork() ) {
       final File jarFile = AgentFileInstaller.getJarFile() ;
       assertThat( jarFile ).isNotNull() ;
     }
   }
 
 
 
   @Test
   @Ignore( "Eats 10 seconds of build time" )
   public void startForeignProgramAndMissHeartbeat() throws Exception {
 
     final int heartbeatPeriod = 100 * 1000 ;
    final int heartbeatFatalDelay = 10 * 1000 ;
     final long isDownCheckPeriod = 500L ;
     final int isDownRetryCount = ( int ) ( ( long ) heartbeatFatalDelay / isDownCheckPeriod + 1L ) ;
 
     if( isLikelyToWork() ) {
 
       final ShellFixture shellFixture = new ShellFixture() ;
 
       final JavaShell.Parameters parameters = shellFixture.getParameters()
           .withHeartbeatPeriodMilliseconds( heartbeatPeriod )
           .withHeartbeatFatalDelayMilliseconds( heartbeatFatalDelay )
       ;
       final JavaShell javaShell = new JavaShell( parameters ) ;
 
       javaShell.start( SHELL_STARTUP_TIMEOUT_DURATION, SHELL_STARTUP_TIMEOUT_UNIT ) ;
       try {
         final IsDown isDown = new IsDown( javaShell );
         assertEventually( isDown, isDownCheckPeriod, TimeUnit.MILLISECONDS, isDownRetryCount ) ;
       } finally {
         javaShell.shutdown( ShutdownStyle.FORCED ) ;
       }
 
       final List< String > log = readLines( shellFixture.getLogFile() ) ;
       assertThat( log )
           .hasSize( 1 )
           .contains( "Starting up and listening..." )
       ;
     }
 
   }
 
   @Test
   public void startForeignProgram() throws Exception {
     if( isLikelyToWork() ) {
       final ShellFixture shellFixture = new ShellFixture() ;
 
       final JavaShell javaShell = new JavaShell( shellFixture.getParameters() ) ;
 
       javaShell.start( SHELL_STARTUP_TIMEOUT_DURATION, SHELL_STARTUP_TIMEOUT_UNIT ) ;
       LOG.info( "Started process known as " + javaShell.getNickname() + "." ) ;
       javaShell.shutdown( ShutdownStyle.GENTLE ) ;
 
       final List< String > log = readLines( shellFixture.getLogFile() ) ;
       assertThat( log )
           .hasSize( 2 )
           .contains( "Starting up and listening...", "Terminated." )
       ;
     }
 
   }
 
 
 // =======
 // Fixture
 // =======
 
   private static final Log LOG = LogFactory.getLog( JavaShellTest.class ) ;
 
   public static final long SHELL_STARTUP_TIMEOUT_DURATION = 10L ;
   public static final TimeUnit SHELL_STARTUP_TIMEOUT_UNIT = TimeUnit.SECONDS ;
 
 
  private static final Predicate< String > STUPID_LISTENER_STARTED = new Predicate<String>() {
     @Override
     public boolean apply( final String input ) {
       return input.startsWith( "Started." ) ;
     }
   } ;
 
   @SuppressWarnings( { "ThrowableInstanceNeverThrown" } )
   private static boolean isLikelyToWork() {
 
     for( final StackTraceElement element : new Exception().getStackTrace() ) {
       if( element.getClassName().contains( "org.apache.maven.surefire.Surefire" ) ) {
         // Maven tests should work all time.
         return true ;
       }
     }
 
     try {
       AgentFileInstaller.getJarFile() ;
     } catch( MissingResourceException e ) {
       LOG.warn( "Not running as Maven test, nor couldn't find agent jar file" +
           " (check system properties). Skipping " + NameAwareTestClassRunner.getTestName() +
           " because needed resources may be missing."
       ) ;
       return false ;
     }
 
     return true ;
   }
 
   @SuppressWarnings( { "unchecked" } )
   private static List< String > readLines( final File logFile ) throws IOException {
     return FileUtils.readLines( logFile ) ;
   }
 
 
   private static final String FIXTUREJARFILE_PROPERTYNAME =
       "novelang.system.shell.test.fixturejarfile" ;
 
 
   private static File installFixturePrograms( final File directory ) throws IOException {
     final String fixtureJarFileAsString = System.getProperty( FIXTUREJARFILE_PROPERTYNAME ) ;
     if( fixtureJarFileAsString == null ) {
       final File jarFile = new File( directory, "java-program.jar" ) ;
       AgentFileInstaller.copyResourceToFile( FIXTURE_PROGRAM_JAR_RESOURCE_NAME, jarFile ) ;
       return jarFile ;
     } else {
       final File existingJarFile = new File( fixtureJarFileAsString ) ;
       if( ! existingJarFile.isFile() ) {
         throw new IllegalArgumentException( "Not an existing file: '" + existingJarFile + "'" ) ;
       }
       return existingJarFile ;
     }
 
   }
 
   /**
    * TODO: Make this work for non-SNAPSHOT versions.
    */
   @SuppressWarnings( { "HardcodedFileSeparator" } )
   private static final String FIXTURE_PROGRAM_JAR_RESOURCE_NAME =
       "/Novelang-shell-fixture-SNAPSHOT.jar" ;
 
 
 
   private static class ShellFixture {
     private final File logFile;
     private final JavaShell.Parameters parameters ;
 
     public File getLogFile() {
       return logFile;
     }
 
     public JavaShell.Parameters getParameters() {
       return parameters;
     }
 
     public ShellFixture() throws IOException {
       final int jmxPort = TcpPortBooker.THIS.find() ;
       final int dummyListenerPort = TcpPortBooker.THIS.find() ;
       final File scratchDirectory = new DirectoryFixture().getDirectory() ;
       logFile = new File( scratchDirectory, "dummy.txt" );
       final File jarFile = installFixturePrograms( scratchDirectory ) ;
 
       parameters = Husk.create( JavaShell.Parameters.class )
           .withNickname( "Stupid" )
           .withWorkingDirectory( scratchDirectory )
           .withJavaClasses( new JavaClasses.ClasspathAndMain(
               "novelang.system.shell.StupidListener",
               jarFile
           ) )
           .withStartupSensor( STUPID_LISTENER_STARTED )
           .withProgramArguments( of(
               logFile.getAbsolutePath(),
               Integer.toString( dummyListenerPort )
           ) )
           .withJmxPort( jmxPort )
       ;
     }
   }
 
 
   public interface StandalonePredicate {
     boolean apply() ;
   }
 
   private static class IsDown implements StandalonePredicate {
 
     private final JavaShell javaShell ;
 
     public IsDown( final JavaShell javaShell ) {
       this.javaShell = javaShell ;
     }
 
     @Override
     public boolean apply() {
       return ! javaShell.isUp() ;
     }
 
     @Override
     public String toString() {
       return getClass().getSimpleName() ;
     }
   }
 
   
   public static void assertEventually(
       final StandalonePredicate predicate,
       final long period,
       final TimeUnit timeUnit,
       final int retries
   ) {
     checkArgument( retries > 0 ) ;
     checkArgument( period > 0L ) ;
 
     int retryCount = 0 ;
     while( true ) {
 
       if( predicate.apply() ) {
         return ;
       }
       if( retryCount ++ < retries ) {
         try {
           LOG.debug( "Unmatched predicate " + predicate + ", waiting a bit and retrying..." );
           timeUnit.sleep( period ) ;
         } catch( InterruptedException e ) {
           throw new RuntimeException( "Should not happen", e ) ;
         }
         continue ;
       }
       fail( "Unmatched predicate after " + retries + " retries." ) ;
     }
 
   }
 }
