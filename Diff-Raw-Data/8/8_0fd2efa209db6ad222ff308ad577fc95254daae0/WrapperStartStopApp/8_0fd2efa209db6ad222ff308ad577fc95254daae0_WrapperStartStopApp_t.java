 package org.tanukisoftware.wrapper;
 
 /*
  * Copyright (c) 1999, 2004 Tanuki Software
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
// Revision 1.7  2004/08/06 14:57:40  mortenson
// Fix a problem when using the WrapperStartStopApp helper class.  The usage
// text was incorrectly being displayed in the console if an exception was
// thrown while executing the main method of the configured stop class.
//
 // Revision 1.6  2004/02/16 04:37:20  mortenson
 // Modify the WrapperSimpleApp and WrapperStartStopApp so that the main method
 // of a class is located even if it exists in a parent class rather than the
 // class specified.
 //
 // Revision 1.5  2004/01/16 04:42:00  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.4  2003/06/19 05:45:02  mortenson
 // Modified the suggested behavior of the WrapperListener.controlEvent() method.
 //
 // Revision 1.3  2003/04/03 04:05:23  mortenson
 // Fix several typos in the docs.  Thanks to Mike Castle.
 //
 // Revision 1.2  2003/02/17 09:52:16  mortenson
 // Modify the way exceptions thrown by an application's main method are
 // presented to the user by the WrapperSimpleApp and WrapperStartStopApp so
 // they no longer look like a problem with Wrapper configuration.
 //
 // Revision 1.1  2003/02/03 06:55:28  mortenson
 // License transfer to TanukiSoftware.org
 //
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 /**
  *
  *
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public class WrapperStartStopApp
     implements WrapperListener, Runnable
 {
     /**
      * Application's start main method
      */
     private Method m_startMainMethod;
     
     /**
      * Command line arguments to be passed on to the start main method
      */
     private String[] m_startMainArgs;
     
     /**
      * Application's stop main method
      */
     private Method m_stopMainMethod;
     
     /**
      * Should the stop process force the JVM to exit, or wait for all threads to die on their own.
      */
     private boolean m_stopWait;
     
     /**
      * Command line arguments to be passed on to the stop main method
      */
     private String[] m_stopMainArgs;
     
     /**
      * Gets set to true when the thread used to launch the application completes.
      */
     private boolean m_mainComplete;
     
     /**
      * Exit code to be returned if the application fails to start.
      */
     private Integer m_mainExitCode;
     
     /**
      * Flag used to signify that the start method is done waiting for the application to start.
      */
     private boolean m_waitTimedOut;
     
     /*---------------------------------------------------------------
      * Constructors
      *-------------------------------------------------------------*/
     private WrapperStartStopApp( Method startMainMethod,
                                  Method stopMainMethod,
                                  boolean stopWait,
                                  String[] stopMainArgs )
     {
         m_startMainMethod = startMainMethod;
         m_stopMainMethod = stopMainMethod;
         m_stopWait = stopWait;
         m_stopMainArgs = stopMainArgs;
     }
     
     /*---------------------------------------------------------------
      * Runnable Methods
      *-------------------------------------------------------------*/
     /**
      * Used to launch the application in a separate thread.
      */
     public void run()
     {
         Throwable t = null;
         try
         {
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: invoking start main method" );
             }
             m_startMainMethod.invoke( null, new Object[] { m_startMainArgs } );
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: start main method completed" );
             }
             
             synchronized(this)
             {
                 // Let the start() method know that the main method returned, in case it is 
                 //  still waiting.
                 m_mainComplete = true;
                 this.notifyAll();
             }
             
             return;
         }
         catch ( IllegalAccessException e )
         {
             t = e;
         }
         catch ( IllegalArgumentException e )
         {
             t = e;
         }
         catch ( InvocationTargetException e )
         {
             t = e.getTargetException();
             if ( t == null )
             {
                 t = e;
             }
         }
         
         // If we get here, then an error was thrown.  If this happened quickly 
         // enough, the start method should be allowed to shut things down.
         System.out.println();
         System.out.println( "WrapperStartStopApp: Encountered an error running start main: " + t );
 
         // We should print a stack trace here, because in the case of an 
         // InvocationTargetException, the user needs to know what exception
         // their app threw.
         t.printStackTrace();
 
         synchronized(this)
         {
             if ( m_waitTimedOut )
             {
                 // Shut down here.
                 WrapperManager.stop( 1 );
                 return; // Will not get here.
             }
             else
             {
                 // Let start method handle shutdown.
                 m_mainComplete = true;
                 m_mainExitCode = new Integer( 1 );
                 this.notifyAll();
                 return;
             }
         }
     }
     
     /*---------------------------------------------------------------
      * WrapperListener Methods
      *-------------------------------------------------------------*/
     /**
      * The start method is called when the WrapperManager is signalled by the 
      *	native wrapper code that it can start its application.  This
      *	method call is expected to return, so a new thread should be launched
      *	if necessary.
      * If there are any problems, then an Integer should be returned, set to
      *	the desired exit code.  If the application should continue,
      *	return null.
      */
     public Integer start( String[] args )
     {
         if ( WrapperManager.isDebugEnabled() )
         {
             System.out.println( "WrapperStartStopApp: start(args)" );
         }
         
         Thread mainThread = new Thread( this, "WrapperStartStopAppMain" );
         synchronized(this)
         {
             m_startMainArgs = args;
             mainThread.start();
             // Wait for two seconds to give the application a chance to have failed.
             try
             {
                 this.wait( 2000 );
             }
             catch ( InterruptedException e )
             {
             }
             m_waitTimedOut = true;
             
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: start(args) end.  Main Completed="
                     + m_mainComplete + ", exitCode=" + m_mainExitCode );
             }
             return m_mainExitCode;
         }
     }
     
     /**
      * Called when the application is shutting down.
      */
     public int stop( int exitCode )
     {
         if ( WrapperManager.isDebugEnabled() )
         {
             System.out.println( "WrapperStartStopApp: stop(" + exitCode + ")" );
         }
         
         // Execute the main method in the stop class
         Throwable t = null;
         try
         {
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: invoking stop main method" );
             }
             m_stopMainMethod.invoke( null, new Object[] { m_stopMainArgs } );
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: stop main method completed" );
             }
             
             if ( m_stopWait )
             {
                 int threadCnt;
                 while( ( threadCnt = WrapperManager.getNonDaemonThreadCount() ) > 1 )
                 {
                     if ( WrapperManager.isDebugEnabled() )
                     {
                         System.out.println( "WrapperStartStopApp: stopping.  Waiting for "
                             + (threadCnt - 1) + " threads to complete." );
                     }
                     try
                     {
                         Thread.sleep( 1000 );
                     }
                     catch ( InterruptedException e )
                     {
                     }
                 }
             }
             
             // Success
             return exitCode;
         }
         catch ( IllegalAccessException e )
         {
             t = e;
         }
         catch ( IllegalArgumentException e )
         {
             t = e;
         }
         catch ( InvocationTargetException e )
         {
             t = e;
         }
         
         // If we get here, then an error was thrown.
         System.out.println( "Encountered an error running stop main: " + t );
 
         // We should print a stack trace here, because in the case of an 
         // InvocationTargetException, the user needs to know what exception
         // their app threw.
         t.printStackTrace();
         
         // Return a failure exit code
         return 1;
     }
     
     /**
      * Called whenever the native wrapper code traps a system control signal
      *  against the Java process.  It is up to the callback to take any actions
      *  necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT, 
      *    WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or 
      *    WRAPPER_CTRL_SHUTDOWN_EVENT
      */
     public void controlEvent( int event )
     {
         if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
             && WrapperManager.isLaunchedAsService() )
         {
             // Ignore
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: controlEvent(" + event + ") Ignored" );
             }
         }
         else
         {
             if ( WrapperManager.isDebugEnabled() )
             {
                 System.out.println( "WrapperStartStopApp: controlEvent(" + event + ") Stopping" );
             }
             WrapperManager.stop( 0 );
             // Will not get here.
         }
     }
     
     /*---------------------------------------------------------------
      * Methods
      *-------------------------------------------------------------*/
     /**
      * Returns the main method of the specified class.  If there are any problems,
      *  an error message will be displayed and the Wrapper will be stopped.  This
      *  method will only return if it has a valid method.
      */
     private static Method getMainMethod( String className )
     {
         // Look for the start class by name
         Class mainClass;
         try
         {
             mainClass = Class.forName( className );
         }
         catch ( ClassNotFoundException e )
         {
             System.out.println( "WrapperStartStopApp: Unable to locate the class " + className
                 + ": " + e );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         catch ( LinkageError e )
         {
             System.out.println( "WrapperStartStopApp: Unable to locate the class " + className
                 + ": " + e );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         
         // Look for the start method
         Method mainMethod;
         try
         {
             // getDeclaredMethod will return any method named main in the specified class,
             //  while getMethod will only return public methods, but it will search up the
             //  inheritance path.
             mainMethod = mainClass.getMethod( "main", new Class[] { String[].class } );
         }
         catch ( NoSuchMethodException e )
         {
             System.out.println(
                 "WrapperStartStopApp: Unable to locate a public static main method in "
                 + "class " + className + ": " + e );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         catch ( SecurityException e )
         {
             System.out.println(
                 "WrapperStartStopApp: Unable to locate a public static main method in "
                 + "class " + className + ": " + e );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         
         // Make sure that the method is public and static
         int modifiers = mainMethod.getModifiers();
         if ( !( Modifier.isPublic( modifiers ) && Modifier.isStatic( modifiers ) ) )
         {
             System.out.println( "WrapperStartStopApp: The main method in class " + className
                 + " must be declared public and static." );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         
         return mainMethod;
     }
     
     private static String[] getArgs( String[] args, int argBase )
     {
         // The arg at the arg base should be a count of the number of available arguments.
         int argCount;
         try
         {
             argCount = Integer.parseInt( args[argBase] );
         }
         catch ( NumberFormatException e )
         {
             System.out.println( "WrapperStartStopApp: Illegal argument count: " + args[argBase] );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         if ( argCount < 0 )
         {
             System.out.println( "WrapperStartStopApp: Illegal argument count: " + args[argBase] );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         
         // Make sure that there are enough arguments in the array.
         if ( args.length < argBase + 1 + argCount )
         {
             System.out.println( "WrapperStartStopApp: Not enough argments.  Argument count of "
                 + argCount + " was specified." );
             System.out.println( "( " + args.length + " < " + argBase + " + 2 + " + argCount + ")" );
             showUsage();
             WrapperManager.stop( 1 );
             return null;  // Will not get here
         }
         
         // Create the argument array
         String[] mainArgs = new String[argCount];
         System.arraycopy( args, argBase + 1, mainArgs, 0, argCount );
         
         return mainArgs;
     }
     
     /**
      * Displays application usage
      */
     private static void showUsage()
     {
         System.out.println();
         System.out.println(
             "WrapperStartStopApp Usage:" );
         System.out.println(
             "  java org.tanukisoftware.wrapper.WrapperStartStopApp {start_class} {start_arg_count} "
             + "[start_arguments] {stop_class} {stop_wait} {stop_arg_count} [stop_arguments]" );
         System.out.println();
         System.out.println(
             "Where:" );
         System.out.println(
             "  start_class:     The fully qualified class name to run to start the " );
         System.out.println(
             "                   application." );
         System.out.println(
             "  start_arg_count: The number of arguments to be passed to the start class's " );
         System.out.println(
             "                   main method." );
         System.out.println(
             "  stop_class:      The fully qualified class name to run to stop the " );
         System.out.println(
             "                   application." );
         System.out.println(
             "  stop_wait:       When stopping, should the Wrapper wait for all threads to " );
         System.out.println(
             "                   complete before exiting (true/false)." );
         System.out.println(
             "  stop_arg_count:  The number of arguments to be passed to the stop class's " );
         System.out.println(
             "                   main method." );
         
         System.out.println(
             "  app_parameters:  The parameters that would normally be passed to the" );
         System.out.println(
             "                   application." );
     }
     
     /*---------------------------------------------------------------
      * Main Method
      *-------------------------------------------------------------*/
     /**
      * Used to Wrapper enable a standard Java application.  This main
      *  expects the first argument to be the class name of the application
      *  to launch.  All remaining arguments will be wrapped into a new
      *  argument list and passed to the main method of the specified
      *  application.
      */
     public static void main( String args[] )
     {
         // Get the class name of the application
         if ( args.length < 5 )
         {
             System.out.println( "WrapperStartStopApp: Not enough argments.  Minimum 5 required." );
             showUsage();
             WrapperManager.stop( 1 );
             return;  // Will not get here
         }
         
         
         // Look for the start main method.
         Method startMainMethod = getMainMethod( args[0] );
         // Get the start arguments
         String[] startArgs = getArgs( args, 1 );
         
         
         // Where do the stop arguments start
         int stopArgBase = 2 + startArgs.length;
         if ( args.length < stopArgBase + 3 )
         {
             System.out.println( "WrapperStartStopApp: Not enough argments. Minimum 3 after start "
                 + "arguments." );
             showUsage();
             WrapperManager.stop( 1 );
             return;  // Will not get here
         }
         // Look for the stop main method.
         Method stopMainMethod = getMainMethod( args[stopArgBase] );
         // Get the stopWait flag
         boolean stopWait;
         if ( args[stopArgBase + 1].equalsIgnoreCase( "true" ) )
         {
             stopWait = true;
         }
         else if ( args[stopArgBase + 1].equalsIgnoreCase( "false" ) )
         {
             stopWait = false;
         }
         else
         {
             System.out.println( "WrapperStartStopApp: The stop_wait argument must be either true "
                 + "or false." );
             showUsage();
             WrapperManager.stop( 1 );
             return;  // Will not get here
         }
         // Get the start arguments
         String[] stopArgs = getArgs( args, stopArgBase + 2 );
         
         
         // Create the WrapperStartStopApp
         WrapperStartStopApp app =
             new WrapperStartStopApp( startMainMethod, stopMainMethod, stopWait, stopArgs );
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start( app, startArgs );
         
         // This thread ends, the WrapperManager will start the application after the Wrapper has
         //  been propperly initialized by calling the start method above.
     }
 }
 
