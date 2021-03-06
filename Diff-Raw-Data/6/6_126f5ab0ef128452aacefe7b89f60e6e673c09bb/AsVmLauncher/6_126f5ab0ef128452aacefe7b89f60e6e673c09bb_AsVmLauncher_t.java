 /**
  *  Copyright 2008 Marvin Herman Froeder
  *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
  *  http://www.apache.org/licenses/LICENSE-2.0
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
  *
  */
 package org.sonatype.flexmojos.test.threads;
 
 import java.awt.GraphicsEnvironment;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.codehaus.plexus.logging.AbstractLogEnabled;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 import org.codehaus.plexus.util.cli.StreamPumper;
 import org.sonatype.flexmojos.utilities.MavenUtils;
 
 /**
  * ActionScript runtime launcher. This class is used to launch the application that runs unit tests.
  * 
  * @plexus.component role="org.sonatype.flexmojos.test.threads.ASRLauncher"
  */
 public class AsVmLauncher
     extends AbstractLogEnabled
     implements ControlledThread
 {
 
     public static final String ROLE = AsVmLauncher.class.getName();
 
     private static final String WINDOWS_CMD = "FlashPlayer.exe";
 
     private static final String MAC_CMD = "Flash Player";
 
     private static final String UNIX_CMD = "flashplayer";
 
     private ThreadStatus status;
 
     private boolean holdStatus;
 
     private Error error;
 
     private Process process;
 
     private String getPlatformDefaultCommand()
     {
         if ( MavenUtils.isWindows() )
         {
             // Ideally we want to launch the SWF in the player so we can close
             // it, not so easy in a browser. We let 'rundll32' do the work based
             // on the extension of the file passed in.
             return WINDOWS_CMD;
         }
         else if ( MavenUtils.isMac() )
         {
             // Ideally we want to launch the SWF in the player so we can close
             // it, not so easy in a browser. We let 'open' do the work based
             // on the extension of the file passed in.
             return MAC_CMD;
         }
         else
         {
             // If we are running in UNIX the fallback is to the browser. To do
             // this Netscape must be running for the "-remote" flag to work. If
             // the browser is not running we need to start it.
             return UNIX_CMD;
         }
     }
 
     /**
      * Run the SWF that contains the FlexUnit tests.
      * 
      * @param targetSwf the SWF.
      * @throws IOException
      * @throws Exception if there is an error launching the tests.
      */
     public void init( String flashPlayer, File targetSwf, boolean allowHeadlessMode )
         throws IOException, FileNotFoundException
     {
         if ( targetSwf == null )
         {
             throw new FileNotFoundException( "Target SWF not defined" );
         }
 
         if ( !targetSwf.isFile() )
         {
             throw new FileNotFoundException( "Target SWF not found " + targetSwf );
         }
 
         if ( flashPlayer == null )
         {
             flashPlayer = getPlatformDefaultCommand();
         }
 
         setStatus( ThreadStatus.RUNNING );
 
         getLogger().debug( "exec: " + flashPlayer + " - " + targetSwf );
 
         getLogger().debug( "Creating process" );
         if ( allowHeadlessMode && MavenUtils.isLinux() && GraphicsEnvironment.isHeadless() )
         {
             getLogger().warn( "Using xvfb-run to launch headless tests" );
             process = Runtime.getRuntime().exec( new String[] { "xvfb-run", flashPlayer, targetSwf.getAbsolutePath() } );
         }
         else
         {
             process = Runtime.getRuntime().exec( new String[] { flashPlayer, targetSwf.getAbsolutePath() } );
         }
         getLogger().debug( "Process created " + process );
     }
 
     public void run()
     {
 
         StreamConsumer stdout = new StreamConsumer()
         {
             public void consumeLine( String line )
             {
                 getLogger().debug( "[SYSOUT]: " + line );
             }
         };
 
         StreamConsumer stderr = new StreamConsumer()
         {
             public void consumeLine( String line )
             {
                 getLogger().debug( "[SYSERR]: " + line );
             }
         };
 
         StreamPumper outputPumper = new StreamPumper( process.getInputStream(), stdout );
 
         StreamPumper errorPumper = new StreamPumper( process.getErrorStream(), stderr );
 
         outputPumper.start();
 
         errorPumper.start();
 
         getLogger().debug( "Output pumpers ON" );
 
         int returnCode;
         try
         {
             getLogger().debug( "Waiting for" );
             returnCode = process.waitFor();
         }
         catch ( InterruptedException e )
         {
             setStatus( ThreadStatus.ERROR );
             error = new Error( "Error while executing external command, process killed.", e );
             throw error;
         }
 
         if ( returnCode != 0 )
         {
             setStatus( ThreadStatus.ERROR );
             error = new Error( "Unexpected return code " + returnCode );
         }
         else
         {
             setStatus( ThreadStatus.DONE );
         }
     }
 
     public void setStatus( ThreadStatus status )
     {
         if ( !holdStatus )
         {
             this.status = status;
         }
     }
 
     public void setError( Error error )
     {
         this.error = error;
     }
 
     public void stop()
     {
         holdStatus = true;

        if ( process != null )
        {
            process.destroy();
        }
     }
 
     public ThreadStatus getStatus()
     {
         return status;
     }
 
     public Error getError()
     {
         return this.error;
     }
 
 }
