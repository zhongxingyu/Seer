 /*
  * Copyright (C) 2010 Laurent Caillette
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
 package org.novelang.outfit.shell;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.RuntimeMXBean;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Predicate;
 import javax.management.InstanceNotFoundException;
 import org.apache.commons.io.FileUtils;
 import org.fest.reflect.core.Reflection;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.TcpPortBooker;
 import org.novelang.outfit.shell.insider.Insider;
 import org.novelang.testing.RepeatedAssert;
 import org.novelang.testing.StandalonePredicate;
 import org.novelang.testing.junit.MethodSupport;
 
 import static com.google.common.collect.ImmutableList.of;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * Tests for {@link JavaShell}.
  * This test needs the embedded Insider jar in the classpath (see insider Maven project).
  * For running from an IDE, needs parameters like this:
  * <pre>
 -Dlogback.configurationFile=configuration/test/logback-test.xml
 -Dorg.novelang.outfit.shell.agentjarfile=/Users/currentuser/.m2/repository/org/novelang/Novelang-insider/${project.version}/Novelang-insider-${project.version}.jar
 -Dorg.novelang.outfit.shell.fixturejarfile=/Users/currentuser/.m2/repository/org/novelang/Novelang-shell-fixture/${project.version}/Novelang-shell-fixture-${project.version}.jar
 -Dorg.novelang.outfit.shell.versionoverride=SNAPSHOT
  * </pre>
  * 
  * @author Laurent Caillette
  */
 public class JavaShellTest {
 
   @Test
   public void getTheOfficialJar() throws IOException {
     final File jarFile = AgentFileInstaller.getInstance().getJarFile() ;
     assertThat( jarFile ).isNotNull() ;
   }
 
 
   @Test( expected = ProcessInitializationException.class )
   public void cannotStart() throws Exception {
     final ShellFixture shellFixture = new ShellFixture( methodSupport ) ;
     final JavaShell javaShell = new JavaShell( shellFixture.getParameters()
         .withJavaClasses( new JavaClasses.ClasspathAndMain(
             "ThisClassDoesNotExist",
             shellFixture.getJarFile()
         )
     ) ) ;
     javaShell.start() ;
   }
 
 
   @Test
 //  @Ignore( "Takes too long (about 40 s)" )
   public void startTwice() throws Exception {
     startAndShutdown( new JavaShell( new ShellFixture( methodSupport ).getParameters() ) ) ;
     startAndShutdown( new JavaShell( new ShellFixture( methodSupport ).getParameters() ) ) ;
   }
 
   @Test
   public void useAsJmxConnector() throws Exception {
     final JavaShell javaShell =
         new JavaShell( new ShellFixture( methodSupport ).getParameters() ) ;
     javaShell.start() ;
     try {
       final String virtualMachineName = queryJvmName( javaShell ) ;
       assertNotNull( virtualMachineName ) ;
       LOGGER.info( "Returned VM name: '", virtualMachineName, "'" ) ;
     } finally {
       javaShell.shutdown( ShutdownStyle.FORCED ) ;
     }
   }
 
 
 
   @Test
   public void detectProgramExitedOnItsOwn() throws Exception {
     final ShellFixture shellFixture = new ShellFixture( methodSupport ) ;
     final int heartbeatFatalDelay = 1000 ;
     final JavaShell javaShell = new JavaShell( shellFixture.getParameters()
             .withHeartbeatFatalDelayMilliseconds( heartbeatFatalDelay )
             .withHeartbeatPeriodMilliseconds( 100 )
     ) ;
     javaShell.start() ;
     final MaybeDown maybeDown = new MaybeDown( javaShell ) ;
 
     // Be sure that JMX stuff started in watched JVM.
     assertThat( queryJvmName( javaShell ) ).isNotNull() ;
 
     shellFixture.askForSelfTermination() ;
     Thread.sleep( ( long ) heartbeatFatalDelay ) ;
     assertThat( maybeDown.apply() ).isTrue() ;
   }
 
 
 
   @Test
   public void missHeartbeat() throws Exception {
 
     final int heartbeatPeriod = 100 ;
     final int heartbeatFatalDelay = 1000 ;
     final long maybeDownCheckPeriod = 100 ;
     final int maybeDownRetryCount = 2 ;
 
     final ShellFixture shellFixture = new ShellFixture( methodSupport ) ;
 
     final JavaShellParameters parameters = shellFixture.getParameters()
         .withHeartbeatPeriodMilliseconds( heartbeatPeriod )
         .withHeartbeatFatalDelayMilliseconds( heartbeatFatalDelay )
         .withJmxPortConfiguredAtJvmStartup( TcpPortBooker.THIS.find() )
         .withJmxKit( new DefaultJmxKit() )
     ;
 
     final JavaShell javaShell = new JavaShell( parameters ) ;
     try {
       javaShell.start() ;
 
       final MaybeDown maybeDown = new MaybeDown( javaShell ) ;
       LOGGER.debug( "At this time, the JVM should be up." ) ;
       assertThat( maybeDown.apply() ).isFalse() ; // Test health.
 
       final HeartbeatSender heartbeatSender = Reflection.field( "heartbeatSender" )
           .ofType( HeartbeatSender.class ).in( javaShell ).get() ;
       heartbeatSender.stop() ;
 
       RepeatedAssert.assertEventually(
           maybeDown,
           maybeDownCheckPeriod,
           TimeUnit.MILLISECONDS,
           maybeDownRetryCount
       ) ;
 
       final List< String > log = readLines( shellFixture.getLogFile() ) ;
       assertThat( log ).isNotEmpty() ;
       assertThat( log.get( 0 ) ).contains( "Starting up" ).contains( "listening..." ) ;
     } finally {
       javaShell.shutdown( ShutdownStyle.FORCED ) ;
     }
   }
 
 
   @Test
   public void justStartForeignProgram() throws Exception {
     final ShellFixture shellFixture = new ShellFixture( methodSupport ) ;
 
     final JavaShell javaShell = new JavaShell( shellFixture.getParameters() ) ;
     try {
       javaShell.start() ;
       LOGGER.info( "Started process known as ", javaShell.getNickname(), "." ) ;
       javaShell.shutdown( ShutdownStyle.GENTLE ) ;
     } catch( Exception e ) {
       javaShell.shutdown( ShutdownStyle.FORCED ) ;
       throw e ;
     }
 
     final List< String > log = readLines( shellFixture.getLogFile() ) ;
     assertThat( log ).hasSize( 2 ) ;
     assertThat( log.get( 0 ) ).contains( "Starting up" ).contains( "listening" ) ;
     assertThat( log.get( 1 ) ).contains( "Terminated." ) ;
 
   }
 
 
 // =======
 // Fixture
 // =======
 
   static final Logger LOGGER = LoggerFactory.getLogger( JavaShellTest.class ) ;
 
 
   static final Predicate< String > STUPID_LISTENER_STARTED = new Predicate< String >() {
     @Override
     public boolean apply( final String input ) {
       return input.startsWith( "Started." ) ;
     }
   } ;
 
   @Rule
   public final MethodSupport methodSupport = new MethodSupport() {
     @Override
     protected String mayEvaluateInContext() {
       return mayEvaluate() ;
     }
   } ;
 
 
   @SuppressWarnings( { "ThrowableInstanceNeverThrown" } )
   private String mayEvaluate() {
 
     for( final StackTraceElement element : new Exception().getStackTrace() ) {
       if( element.getClassName().contains( "org.apache.maven.surefire.Surefire" ) ) {
         // Maven tests should work all time unless broken for good reason.
         return null ;
       }
     }
 
     final String warning = "Not running as Maven test, nor couldn't find agent jar file" +
             " (check system properties). Skipping " + methodSupport.getTestName() +
             " because needed resources may be missing."
     ;
     String message = warning ;
 
     if( AgentFileInstaller.mayHaveValidInstance() ) {
       try {
         AgentFileInstaller.getInstance().getJarFile() ;
         message = null ;
       } catch( MissingResourceException ignore ) { }
     }
 
     if( message == null ) {
       return null ;
     } else {
       return message ;
     }
 
 
   }
 
   @SuppressWarnings( { "unchecked" } )
   private static List< String > readLines( final File logFile ) throws IOException {
     return FileUtils.readLines( logFile ) ;
   }
 
 
   private static final String FIXTUREJARFILE_PROPERTYNAME =
       "org.novelang.outfit.shell.fixturejarfile" ;
 
 
   static File installFixturePrograms( final File directory ) throws IOException {
     final String fixtureJarFileAsString = System.getProperty( FIXTUREJARFILE_PROPERTYNAME ) ;
     if( fixtureJarFileAsString == null ) {
       final File jarFile = new File( directory, "java-program.jar" ) ;
       AgentFileInstaller.getInstance().copyVersionedJarToFile(
           FIXTURE_PROGRAM_JAR_RESOURCE_RADIX, jarFile ) ;
       return jarFile ;
     } else {
       final File existingJarFile = AgentFileInstaller.getInstance()
           .resolveWithVersion( fixtureJarFileAsString ) ;
       if( ! existingJarFile.isFile() ) {
         throw new IllegalArgumentException( "Not an existing file: '" + existingJarFile + "'" ) ;
       }
       return existingJarFile ;
     }
 
   }
 
   private static void startAndShutdown( final JavaShell javaShell ) throws Exception {
     javaShell.start() ;
     try {
       assertNotNull( javaShell.getManagedBean( RuntimeMXBean.class,
           JavaShellTools.RUNTIME_MX_BEAN_OBJECTNAME ).getVmName() ) ;
     } finally {
       javaShell.shutdown( ShutdownStyle.GENTLE ) ;
     }
   }
 
 
 
   /**
    * TODO: Make this work for non-SNAPSHOT versions.
    */
   @SuppressWarnings( { "HardcodedFileSeparator" } )
   private static final String FIXTURE_PROGRAM_JAR_RESOURCE_RADIX =
       "/Novelang-shell-fixture-" ;
 
 
   private static class MaybeDown implements StandalonePredicate {
 
     private final Insider insider ;
 
     public MaybeDown( final JavaShell javaShell ) throws IOException, InterruptedException {
       insider = javaShell.getManagedBean( Insider.class, Insider.NAME ) ;
     }
 
     @Override
     public boolean apply() {
       try {
         return ! insider.isAlive() ;
       } catch( UndeclaredThrowableException e ) {
        final Throwable cause = e.getCause() ;
         if( denotesConnectionLoss( cause ) ) {
           return true ;
         } else {
           throw e ;
         }
       }
     }
 
     private static boolean denotesConnectionLoss( final Throwable cause ) {
       return cause instanceof java.rmi.ConnectException
           || cause instanceof InstanceNotFoundException
           || ( cause instanceof IOException
               && cause.getMessage().contains( "The client has been closed." ) )
       ;
     }
 
     @Override
     public String toString() {
       return getClass().getSimpleName() ;
     }
   }
 
   private static String queryJvmName( final JavaShell javaShell )
       throws IOException, InterruptedException
   {
     final RuntimeMXBean runtimeMXBean = javaShell.getManagedBean(
         RuntimeMXBean.class, JavaShellTools.RUNTIME_MX_BEAN_OBJECTNAME ) ;
     final String virtualMachineName = runtimeMXBean.getVmName() ;
     return virtualMachineName;
   }
 
 }
