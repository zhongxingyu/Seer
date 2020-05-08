 package org.tanukisoftware.wrapper.test;
 
 /*
  * Copyright (c) 1999, 2006 Tanuki Software Inc.
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of the Java Service Wrapper and associated
  * documentation files (the "Software"), to deal in the Software
  * without  restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sub-license,
  * and/or sell copies of the Software, and to permit persons to
  * whom the Software is furnished to do so, subject to the
  * following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  * 
  * 
  * Portions of the Software have been derived from source code
  * developed by Silver Egg Technology under the following license:
  * 
  * Copyright (c) 2001 Silver Egg Technology
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sub-license, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  */
 
 // $Log$
// Revision 1.32  2006/05/17 03:10:45  mortenson
// Fix a typo.
//
 // Revision 1.31  2006/02/24 05:45:59  mortenson
 // Update the copyright.
 //
 // Revision 1.30  2006/02/15 06:33:36  mortenson
 // Put content into a ScrollPane as the window was getting way too large.
 //
 // Revision 1.29  2006/02/15 06:04:50  mortenson
 // Fix a problem where the Wrapper would show the following error message
 // if user code called System.exit from within the WrapperListener.stop
 // callback method.
 //
 // Revision 1.28  2005/10/13 05:52:16  mortenson
 // Implement the ability to catch control events using the WrapperEventLisener.
 //
 // Revision 1.27  2005/09/12 03:54:42  mortenson
 // Fix some spelling.
 //
 // Revision 1.26  2005/08/24 06:53:39  mortenson
 // Add stopAndReturn and restartAndReturn methods.
 //
 // Revision 1.25  2005/05/23 02:39:30  mortenson
 // Update the copyright information.
 //
 // Revision 1.24  2004/12/08 04:54:27  mortenson
 // Make it possible to access the contents of the Wrapper configuration file from
 // within the JVM.
 //
 // Revision 1.23  2004/11/26 08:41:22  mortenson
 // Implement reading from System.in
 //
 // Revision 1.22  2004/11/22 09:35:46  mortenson
 // Add methods for controlling other services.
 //
 // Revision 1.21  2004/11/22 04:06:42  mortenson
 // Add an event model to make it possible to communicate with user applications in
 // a more flexible way.
 //
 // Revision 1.20  2004/11/12 09:52:19  mortenson
 // Don't set the console title from within java in the sample.  It makes the use of the
 // usage of the config file property confusing.
 //
 // Revision 1.19  2004/08/06 08:05:26  mortenson
 // Add test case which dumps the system properties.  Useful for testing.
 //
 // Revision 1.18  2004/08/06 07:56:20  mortenson
 // Add test case which runs idle.  Useful to test some operations.
 //
 // Revision 1.17  2004/06/30 09:02:33  mortenson
 // Remove unused imports.
 //
 // Revision 1.16  2004/04/15 06:42:11  mortenson
 // Fix a typo in the access_violation_native action.
 //
 // Revision 1.15  2004/03/27 14:39:20  mortenson
 // Add actions for the stopImmediate method.
 //
 // Revision 1.14  2004/01/16 04:41:55  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.13  2004/01/15 09:50:30  mortenson
 // Fix some problems where the Wrapper was not handling exit codes correctly.
 //
 // Revision 1.12  2004/01/10 15:44:15  mortenson
 // Rework the test wrapper app so there is less code duplication.
 //
 // Revision 1.11  2004/01/10 13:59:14  mortenson
 // Add a command button to test the user functions.
 //
 // Revision 1.10  2003/10/31 05:59:33  mortenson
 // Added a new method, setConsoleTitle, to the WrapperManager class which
 // enables the application to dynamically set the console title.
 //
 // Revision 1.9  2003/10/30 17:13:24  mortenson
 // Add an action to the WrapperActionServer which makes it possible to test
 // simulate a JVM hang for testing.
 //
 // Revision 1.8  2003/10/18 07:51:10  mortenson
 // The DeadlockPrintStream should not be set until after the WrapperManager class
 // has been initialized.
 //
 // Revision 1.7  2003/10/18 07:35:30  mortenson
 // Add test cases to test how the wrapper handles it when the System.out stream
 // becomes deadlocked.  This can happen if buggy usercode overrides those streams.
 //
 // Revision 1.5  2003/06/19 05:45:00  mortenson
 // Modified the suggested behavior of the WrapperListener.controlEvent() method.
 //
 // Revision 1.4  2003/06/07 05:19:10  mortenson
 // Add a new class, WrapperActionServer, which makes it easy to remotely control
 // the Wrapper remotely by opening a socket and sending commands.  See the
 // javadocs of the class for more details.
 //
 // Revision 1.3  2003/04/03 04:05:22  mortenson
 // Fix several typos in the docs.  Thanks to Mike Castle.
 //
 // Revision 1.2  2003/03/02 08:39:56  mortenson
 // Improve the example code for the controlEvent method.
 //
 // Revision 1.1  2003/02/03 06:55:29  mortenson
 // License transfer to TanukiSoftware.org
 //
 
 import java.awt.BorderLayout;
 import java.awt.Button;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Label;
 import java.awt.List;
 import java.awt.Panel;
 import java.awt.ScrollPane;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.tanukisoftware.wrapper.WrapperActionServer;
 import org.tanukisoftware.wrapper.WrapperManager;
 import org.tanukisoftware.wrapper.WrapperListener;
 import org.tanukisoftware.wrapper.event.WrapperEventListener;
 
 /**
  * This is a Test / Example program which can be used to test the
  *  main features of the Wrapper.
  * <p>
  * It is also an example of Integration Method #3, where you implement
  *  the WrapperListener interface manually.
  * <p>
  * <b>NOTE</b> that in most cases you will want to use Method #1, using the
  *  WrapperSimpleApp helper class to integrate your application.  Please
  *  see the <a href="http://wrapper.tanukisoftware.org/doc/english/integrate.html">integration</a>
  *  section of the documentation for more details.
  *
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public class Main
     extends AbstractActionApp
     implements WrapperListener
 {
     private MainFrame m_frame;
     
     private DeadlockPrintStream m_out;
     private DeadlockPrintStream m_err;
     
     private Thread m_userRunner;
     
     private List m_listenerFlags;
     private TextField m_serviceName;
     
     /**************************************************************************
      * Constructors
      *************************************************************************/
     private Main() {
     }
     
     private class MainFrame extends Frame implements ActionListener
     {
         MainFrame()
         {
             super( "Wrapper Test Application" );
             
             init();
             
             setLocation( 10, 10 );
             setSize( 750, 480 );
             
             setResizable( true );
         }
         
         private void init()
         {
             GridBagLayout gridBag = new GridBagLayout();
             GridBagConstraints c = new GridBagConstraints();
             
             Panel panel = new Panel();
             panel.setLayout( gridBag );
             
             ScrollPane scrollPane = new ScrollPane();
             scrollPane.add( panel );
             
             setLayout( new BorderLayout() );
             add( scrollPane, BorderLayout.CENTER );
             
             buildCommand( panel, gridBag, c, "Stop(0)", "stop0",
                 "Calls WrapperManager.stop( 0 ) to shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( panel, gridBag, c, "Stop(1)", "stop1",
                "Calls WrapperManager.stop( 1 ) to shutdown the JVM and Wrapper with a failure exit code." );
             
             buildCommand( panel, gridBag, c, "Exit(0)", "exit0",
                 "Calls System.exit( 0 ) to shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( panel, gridBag, c, "Exit(1)", "exit1",
                 "Calls System.exit( 1 ) to shutdown the JVM and Wrapper with a failure exit code." );
             
             buildCommand( panel, gridBag, c, "StopImmediate(0)", "stopimmediate0",
                 "Calls WrapperManager.stopImmediate( 0 ) to immediately shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( panel, gridBag, c, "StopImmediate(1)", "stopimmediate1",
                 "Calls WrapperManager.stopImmediate( 1 ) to immediately shutdown the JVM and Wrapper with a failure exir code." );
             
             buildCommand( panel, gridBag, c, "StopAndReturn(0)", "stopandreturn0",
                 "Calls WrapperManager.stopAndReturn( 0 ) to shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( panel, gridBag, c, "Nested Exit(1)", "nestedexit1",
                 "Calls System.exit(1) within WrapperListener.stop(1) callback." );
             
             buildCommand( panel, gridBag, c, "Halt", "halt",
                 "Calls Runtime.getRuntime().halt(0) to kill the JVM, the Wrapper will restart it." );
             
             buildCommand( panel, gridBag, c, "Restart()", "restart",
                 "Calls WrapperManager.restart() to shutdown the current JVM and start a new one." );
             
             buildCommand( panel, gridBag, c, "RestartAndReturn()", "restartandreturn",
                 "Calls WrapperManager.restartAndReturn() to shutdown the current JVM and start a new one." );
             
             buildCommand( panel, gridBag, c, "Access Violation", "access_violation",
                 "Attempts to cause an access violation within the JVM, relies on a JVM bug and may not work." );
             
             buildCommand( panel, gridBag, c, "Native Access Violation", "access_violation_native",
                 "Causes an access violation using native code, the JVM will crash and be restarted." );
             
             buildCommand( panel, gridBag, c, "Simulate JVM Hang", "appear_hung",
                 "Makes the JVM appear to be hung as viewed from the Wrapper, it will be killed and restarted." );
             
             buildCommand( panel, gridBag, c, "Request Thread Dump", "dump",
                 "Calls WrapperManager.requestThreadDump() to cause the JVM to dump its current thread state." );
             
             buildCommand( panel, gridBag, c, "System.out Deadlock", "deadlock_out",
                 "Simulates a failure mode where the System.out object has become deadlocked." );
             
             buildCommand( panel, gridBag, c, "Poll Users", "users",
                 "Begins calling WrapperManager.getUser() and getInteractiveUser() to monitor the current and interactive users." );
             
             buildCommand( panel, gridBag, c, "Poll Users with Groups", "groups",
                 "Same as above, but includes information about the user's groups." );
             
             buildCommand( panel, gridBag, c, "Console", "console", "Prompt for Actions in the console." );
             
             buildCommand( panel, gridBag, c, "Idle", "idle", "Run idly." );
             
             buildCommand( panel, gridBag, c, "Dump Properties", "properties",
                 "Dumps all System Properties to the console." );
             
             buildCommand( panel, gridBag, c, "Dump Configuration", "configuration",
                 "Dumps all Wrapper Configuration Properties to the console." );
             
             
             m_listenerFlags = new List( 2, true );
             m_listenerFlags.add( "Service" );
             m_listenerFlags.add( "Control" );
             m_listenerFlags.add( "Core" );
             
             Panel flagPanel = new Panel();
             flagPanel.setLayout( new BorderLayout() );
             flagPanel.add( new Label( "Event Flags: " ), BorderLayout.WEST );
             flagPanel.add( m_listenerFlags, BorderLayout.CENTER );
             flagPanel.setSize( 100, 10 );
             
             Panel flagPanel2 = new Panel();
             flagPanel2.setLayout( new BorderLayout() );
             flagPanel2.add( flagPanel, BorderLayout.WEST );
             
             buildCommand( panel, gridBag, c, "Update Event Listener", "listener", flagPanel2 );
             
             buildCommand( panel, gridBag, c, "Service List", "service_list", "Displays a list of registered services on Windows." );
             
             m_serviceName = new TextField( "testwrapper" );
             
             Panel servicePanel = new Panel();
             servicePanel.setLayout( new BorderLayout() );
             servicePanel.add( new Label( "Interrogate Service.  Service name: " ), BorderLayout.WEST );
             servicePanel.add( m_serviceName, BorderLayout.CENTER );
             
             Panel servicePanel2 = new Panel();
             servicePanel2.setLayout( new BorderLayout() );
             servicePanel2.add( servicePanel, BorderLayout.WEST );
             
             buildCommand( panel, gridBag, c, "Service Interrogate", "service_interrogate", servicePanel2 );
             
             buildCommand( panel, gridBag, c, "Service Start", "service_start", "Starts the above service." );
             
             buildCommand( panel, gridBag, c, "Service Stop", "service_stop", "Stops the above service." );
             
             buildCommand( panel, gridBag, c, "Service User Code", "service_user", "Sends a series of user codes to the above service." );
         }
         
         private void buildCommand( Container container,
                                    GridBagLayout gridBag,
                                    GridBagConstraints c,
                                    String label,
                                    String command,
                                    Object description )
         {
             Button button = new Button( label );
             button.setActionCommand( command );
             
             c.fill = GridBagConstraints.BOTH;
             c.gridwidth = 1;
             gridBag.setConstraints( button, c );
             container.add( button );
             button.addActionListener(this);
             
             c.gridwidth = GridBagConstraints.REMAINDER;
             Component desc;
             if ( description instanceof String )
             {
                 desc = new Label( (String)description );
             }
             else if ( description instanceof Component )
             {
                 desc = (Component)description;
             }
             else
             {
                 desc = new Label( description.toString() );
             }
             
             gridBag.setConstraints( desc, c );
             container.add( desc );
         }
         
         public void actionPerformed( ActionEvent event )
         {
             String action = event.getActionCommand();
             if ( action.equals( "listener" ) )
             {
                 // Create the mask.
                 long mask = 0;
                 String[] flags = m_listenerFlags.getSelectedItems();
                 for ( int i = 0; i < flags.length; i++ )
                 {
                     String flag = flags[i];
                     if ( flag.equals( "Service" ) )
                     {
                         mask |= WrapperEventListener.EVENT_FLAG_SERVICE;
                     }
                     else if ( flag.equals( "Control" ) )
                     {
                         mask |= WrapperEventListener.EVENT_FLAG_CONTROL;
                     }
                     else if ( flag.equals( "Core" ) )
                     {
                         mask |= WrapperEventListener.EVENT_FLAG_CORE;
                     }
                 }
                 
                 setEventMask( mask );
             }
             
             setServiceName( m_serviceName.getText() );
             
             Main.this.doAction( action );
         }
     }
     
     /**************************************************************************
      * WrapperListener Methods
      *************************************************************************/
     public Integer start( String[] args )
     {
         System.out.println( "start()" );
 
         prepareSystemOutErr();
         
         try
         {
             m_frame = new MainFrame();
             m_frame.setVisible( true );
         }
         catch ( java.lang.InternalError e )
         {
             System.out.println();
             System.out.println( "ERROR - Unable to display the Swing GUI:" );
             System.out.println( "          " + e.toString() );
             System.out.println( "Exiting" );
             System.out.println();
             return new Integer( 1 );
         }
 
         try
         {
             int port = 9999;
             WrapperActionServer server = new WrapperActionServer( port );
             server.enableShutdownAction( true );
             server.enableHaltExpectedAction( true );
             server.enableRestartAction( true );
             server.enableThreadDumpAction( true );
             server.enableHaltUnexpectedAction( true );
             server.enableAccessViolationAction( true );
             server.enableAppearHungAction( true );
             server.start();
             
             System.out.println( "ActionServer Enabled. " );
             System.out.println( "  Telnet localhost 9999" );
             System.out.println( "  Commands: " );
             System.out.println( "    S: Shutdown" );
             System.out.println( "    H: Expected Halt" );
             System.out.println( "    R: Restart" );
             System.out.println( "    D: Thread Dump" );
             System.out.println( "    U: Unexpected Halt (Simulate crash)" );
             System.out.println( "    V: Access Violation (Actual crash)" );
             System.out.println( "    G: Make the JVM appear to be hung." );
         }
         catch ( java.io.IOException e )
         {
             System.out.println( "Unable to open the action server socket: " + e.getMessage() );
         }
         
         return null;
     }
     
     public int stop( int exitCode )
     {
         System.out.println( "stop(" + exitCode + ")" );
         
         if ( m_frame != null )
         {
             if ( !WrapperManager.hasShutdownHookBeenTriggered() )
             {
                 m_frame.setVisible( false );
                 m_frame.dispose();
             }
             m_frame = null;
         }
         
         if ( isNestedExit() )
         {
             System.out.println( "calling System.exit(" + exitCode + ") within stop." );
             System.exit( exitCode );
         }
         
         return exitCode;
     }
     
     public void controlEvent( int event )
     {
         System.out.println( "controlEvent(" + event + ")" );
         
         if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
             && WrapperManager.isLaunchedAsService() )
         {
             System.out.println( "  Ignoring logoff event" );
             // Ignore
         }
         else
         {
             WrapperManager.stop( 0 );
         }
     }
     
     /**************************************************************************
      * Main Method
      *************************************************************************/
     /**
      * IMPORTANT: Please read the Javadocs for this class at the top of the
      *  page before you start to use this class as a template for integrating
      *  your own application.  This will save you a lot of time.
      */
     public static void main( String[] args )
     {
         System.out.println( "Initializing..." );
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start( new Main(), args );
     }
 }
 
