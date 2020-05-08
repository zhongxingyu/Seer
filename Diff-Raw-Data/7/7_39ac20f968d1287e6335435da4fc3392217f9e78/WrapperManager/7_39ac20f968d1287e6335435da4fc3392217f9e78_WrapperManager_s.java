 package com.silveregg.wrapper;
 
 /*
  * Copyright (c) 2001 Silver Egg Technology
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 // $Log$
 // Revision 1.21  2002/09/11 02:56:00  mortenson
 // A debug message was always being displayed.
 //
 // Revision 1.20  2002/09/10 16:03:35  mortenson
 // Add some more documentation to the log method.
 //
 // Revision 1.19  2002/09/09 17:19:47  mortenson
 // Add ability to log to specific log levels from within the Wrapper.
 //
 // Revision 1.18  2002/08/22 13:38:08  mortenson
 // Open up the thread count method to other classes in the Wrapper package.
 //
 // Revision 1.17  2002/07/19 02:06:12  mortenson
 // Added a new property: wrapper.cpu.timeout to control the cpu timeout added in
 // v2.2.7
 //
 // Revision 1.16  2002/07/06 00:50:08  mortenson
 // Modified to show a stack trace when the Server Daemon Dies.  Needed for
 // tracking down problems.
 //
 // Revision 1.15  2002/06/06 00:52:21  mortenson
 // If a JVM tries to reconnect to the Wrapper after it has started shutting down, the
 // Wrapper was getting confused in some cases.  I think that this was just a problem
 // with the "Appear Hung" test, but the Wrapper should be more stable now.
 //
 // Revision 1.14  2002/06/02 13:38:58  mortenson
 // Added support for System Suspend and made the Wrapper handle heavy loads
 // better by avoiding unwanted timeouts.
 //
 // Revision 1.13  2002/05/28 11:54:06  mortenson
 // Add a check for the stop method being called recursively by user code.
 //
 // Revision 1.12  2002/05/24 00:45:12  mortenson
 // Add some comments to the code.
 //
 // Revision 1.11  2002/05/22 16:05:35  mortenson
 // Comment out a debug error stack trace that would show up when the socket to
 // the native Wrapper was closed.  It was being shown on Linux systems.
 //
 // Revision 1.10  2002/05/20 10:03:43  mortenson
 // Make the startup banner less intrusive.
 //
 // Revision 1.9  2002/05/17 09:15:13  mortenson
 // Rework the way the shutdown process works so that System.exit will never be
 // called before the stop method in WrapperListener has had a chance to complete.
 //
 // Revision 1.8  2002/05/16 04:30:30  mortenson
 // JVM info was not being displayed if the Wrapper.DLL file was not loaded.
 // Modify so that dispose is called at the correct times.
 // Add a debug message stating which thread lead to System.exit being called
 //   via a call to shutdown.
 //
 // Revision 1.7  2002/05/08 03:18:16  mortenson
 // Fix a problem where the JVM was not exiting correctly when all non-daemon
 // threads completed.
 //
 // Revision 1.6  2002/03/29 06:09:21  rybesh
 // minor style fix
 //
 // Revision 1.5  2002/03/07 08:10:14  mortenson
 // Add support for Thread Dumping
 // Fix a problem locating java on the path.
 //
 // Revision 1.4  2001/12/07 06:50:28  mortenson
 // Remove an unwanted debug message
 //
 // Revision 1.3  2001/12/06 09:36:24  mortenson
 // Docs changes, Added sample apps, Fixed some problems with
 // relative paths  (See revisions.txt)
 //
 // Revision 1.2  2001/11/08 09:06:58  mortenson
 // Improve JavaDoc text.
 //
 // Revision 1.1.1.1  2001/11/07 08:54:20  mortenson
 // no message
 //
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.BindException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Properties;
 import com.silveregg.wrapper.resources.ResourceManager;
 
 /**
  * Handles all communication with the native portion of the Wrapper code.
  *	The native wrapper code will launch Java in a separate process and set
  *	up a server socket which the Java code is expected to open a socket to
  *	on startup.  When the server socket is created, a port will be chosen
  *	depending on what is available to the system.  This port will then be
  *	passed to the Java process as property named "wrapper.port".
  *
  * For security reasons, the native code will only allow connections from
  *	localhost and will expect to receive the key specified in a property
  *	named "wrapper.key".
  *
  * This class is implemented as a singleton class.
  *
  * Generate JNI Headers with the following command in the build/classes
  *  directory:
  *    javah -jni -classpath ./ com.silveregg.wrapper.WrapperManager
  */
 public final class WrapperManager implements Runnable {
     private static final String  WRAPPER_CONNECTION_THREAD_NAME = "Wrapper-Connection";
     
     private static final int DEFAULT_PORT                = 15003;
     private static final int DEFAULT_SO_TIMEOUT          = 10000;
     private static final long DEFAULT_CPU_TIMEOUT        = 10000L;
     
     private static final byte WRAPPER_MSG_START          = (byte)100;
     private static final byte WRAPPER_MSG_STOP           = (byte)101;
     private static final byte WRAPPER_MSG_RESTART        = (byte)102;
     private static final byte WRAPPER_MSG_PING           = (byte)103;
     private static final byte WRAPPER_MSG_STOP_PENDING   = (byte)104;
     private static final byte WRAPPER_MSG_START_PENDING  = (byte)105;
     private static final byte WRAPPER_MSG_STARTED        = (byte)106;
     private static final byte WRAPPER_MSG_STOPPED        = (byte)107;
     private static final byte WRAPPER_MSG_KEY            = (byte)110;
     private static final byte WRAPPER_MSG_BADKEY         = (byte)111;
     private static final byte WRAPPER_MSG_LOW_LOG_LEVEL  = (byte)112;
     
     /** Log commands are actually 116 + the LOG LEVEL. */
     private static final byte WRAPPER_MSG_LOG            = (byte)116;
     
     public static final int WRAPPER_CTRL_C_EVENT         = 200;
     public static final int WRAPPER_CTRL_CLOSE_EVENT     = 201;
     public static final int WRAPPER_CTRL_LOGOFF_EVENT    = 202;
     public static final int WRAPPER_CTRL_SHUTDOWN_EVENT  = 203;
     
     /** Log message at debug log level. */
     public static final int WRAPPER_LOG_LEVEL_DEBUG      = 1;
     /** Log message at info log level. */
     public static final int WRAPPER_LOG_LEVEL_INFO       = 2;
     /** Log message at status log level. */
     public static final int WRAPPER_LOG_LEVEL_STATUS     = 3;
     /** Log message at warn log level. */
     public static final int WRAPPER_LOG_LEVEL_WARN       = 4;
     /** Log message at error log level. */
     public static final int WRAPPER_LOG_LEVEL_ERROR      = 5;
     /** Log message at fatal log level. */
     public static final int WRAPPER_LOG_LEVEL_FATAL      = 6;
     
     private static boolean _disposed = false;
     private static boolean _started = false;
     private static WrapperManager _instance = null;
     private static Thread _hook = null;
     private static boolean _hookTriggered = false;
     
     private static String[] _args;
     private static int _port    = DEFAULT_PORT;
     private static String _key;
     private static int _soTimeout = DEFAULT_SO_TIMEOUT;
     private static long _cpuTimeout = DEFAULT_CPU_TIMEOUT;
     
     /** The lowest configured log level in the Wrapper's configuration.  This 
      *   is set to a high value by default to disable all logging if the
      *   Wrapper does not register its low level or is not present. */
     private static int _lowLogLevel = WRAPPER_LOG_LEVEL_FATAL + 1;
     
     /** Thread which processes all communications with the native code. */
     private static Thread _commRunner;
     private static boolean _commRunnerStarted = false;
     private static Thread _eventRunner;
     private static long _eventRunnerTime;
     
     private static WrapperListener _listener;
     
     private static long _lastPing;
     private static ServerSocket _serverSocket;
     private static Socket _socket;
     private static boolean _shuttingDown = false;
     private static boolean _appearHung = false;
     
     private static boolean _service = false;
     private static boolean _debug = false;
     private static int _jvmId = 0;
     private static boolean _stopping = false;
     private static Thread _stoppingThread;
     private static boolean _libraryOK = false;
     private static byte[] _commandBuffer = new byte[512];
     
     // message resources: eventually these will be split up
     private static ResourceManager _res        = ResourceManager.getResourceManager();
     private static ResourceManager _error      = _res;
     private static ResourceManager _warning    = _res;
     private static ResourceManager _info       = _res;
     
     /*---------------------------------------------------------------
      * Class Initializer
      *-------------------------------------------------------------*/
     /**
      * When the WrapperManager class is first loaded, it attempts to load the
      *	configuration file specified using the 'wrapper.config' system property.
      *	When the JVM is launched from the Wrapper native code, the
      *	'wrapper.config' and 'wrapper.key' parameters are specified.
      *	The 'wrapper.key' parameter is a password which is used to verify that
      *	connections are only coming from the native Wrapper which launched the
      *	current JVM.
      */
     static {
         // Check for the debug flag
         if (System.getProperty("wrapper.debug") == null) {
             _debug = false;
         } else {
             _debug = true;
         }
         
         // Check for the jvmID
         String jvmId = System.getProperty("wrapper.jvmid");
         if (jvmId != null) {
             try {
                 _jvmId = Integer.parseInt(jvmId);
             } catch (NumberFormatException e) {
                 _jvmId = 1;
             }
         } else {
             _jvmId = 1;
         }
         if (_debug) {
             System.out.println("Wrapper Manager: JVM #" + _jvmId);
         }
         
         // Check to see if we should register a shutdown hook.
         if (System.getProperty("wrapper.disable_shutdown_hook") == null) {
             if (_debug) {
                 System.out.println("Wrapper Manager: Registering shutdown hook");
             }
             _hook = new Thread("Wrapper-Shutdown-Hook") {
                 /**
                  * Run the shutdown hook. (Triggered by the JVM when it is about to shutdown)
                  */
                 public void run() {
                     if (_debug) {
                         System.out.println("Wrapper Manager: ShutdownHook started");
                     }
                     
                     // Stop the Wrapper cleanly.
                     _hookTriggered = true;
                     
                     // If we are not already stopping, then do so.
                     WrapperManager.stop(0);
                     
                     if (_debug) {
                         System.out.println("Wrapper Manager: ShutdownHook complete");
                     }
                 }
             };
             Runtime.getRuntime().addShutdownHook(_hook);
         }
         
         // A key is required for the wrapper to work correctly.  If it is not
         //  present, then assume that we are not being controlled by the native
         //  wrapper.
         if ((_key = System.getProperty("wrapper.key")) == null) {
             if (_debug) {
                 System.out.println("Wrapper Manager: Not using wrapper.  (key not specified)");
             }
             
             // The wrapper will not be used, so other values will not be used.
             _port = 0;
             _service = false;
             _cpuTimeout = 31557600000L; // One Year.  Effectively never.
         } else {
             if (_debug) {
                 System.out.println("Wrapper Manager: Using wrapper");
             }
             
             // A port must have been specified.
             String sPort;
             if ((sPort = System.getProperty("wrapper.port")) == null) {
                 String msg = _res.format("MISSING_PORT");
                 System.out.println(msg);
                 throw new ExceptionInInitializerError(msg);
             }
             try {
                 _port = Integer.parseInt(sPort);
             } catch (NumberFormatException e) {
                 String msg = _res.format("BAD_PORT", sPort);
                 System.out.println(msg);
                 throw new ExceptionInInitializerError(msg);
             }
             
             // If this is being run as a headless server, then a flag would have been set
             if (System.getProperty("wrapper.service") == null) {
                 _service = false;
             } else {
                 _service = true;
             }
             
             // Get the cpuTimeout
             String sCPUTimeout = System.getProperty("wrapper.cpu.timeout");
             if ( sCPUTimeout == null ) {
                 _cpuTimeout = DEFAULT_CPU_TIMEOUT;
             } else {
                 try {
                     _cpuTimeout = Integer.parseInt( sCPUTimeout ) * 1000L;
                 } catch (NumberFormatException e) {
                     String msg = _res.format("BAD_CPU_TIMEOUT", sCPUTimeout);
                     System.out.println(msg);
                     throw new ExceptionInInitializerError(msg);
                 }
             }
         }
         // Initialize the native code to trap system signals
         try {
             System.loadLibrary("wrapper");
             _libraryOK = true;
         } catch (UnsatisfiedLinkError e) {
             System.out.println
                 ("WARNING - Unable to load native library 'wrapper' for class WrapperManager.");
             System.out.println
                 ("  System signals will not be handled correctly.");
             _libraryOK = false;
         }
 
         if (_libraryOK) {
             if (_debug) {
                 System.out.println("Calling native initialization method.");
             }
             nativeInit(_debug);
         }
         
         // Start a thread which looks for control events sent to the
         //  process.  The thread is also used to keep track of whether
         //  the VM has been getting CPU to avoid invalid timeouts.
         _eventRunnerTime = System.currentTimeMillis();
         _eventRunner = new Thread("Wrapper-Control-Event-Monitor") {
             public void run() {
                 while (!_shuttingDown) {
                     long now = System.currentTimeMillis();
                     long age = now - _eventRunnerTime;
                     if (age > _cpuTimeout) {
                         System.out.println("JVM Process has not received any CPU time for " +
                             (age / 1000) + " seconds.  Extending timeouts.");
                         
                         // Make sure that we don't get any ping timeouts in this event
                         _lastPing = now;
                     }
                     _eventRunnerTime = now;
                     
                     if (_libraryOK) {
                         // Look for a control event in the wrapper library
                         int event = WrapperManager.nativeGetControlEvent();
                         if (event != 0) {
                             WrapperManager.controlEvent(event);
                         }
                     }
                     
                     // Wait before checking for another control event.
                     try {
                         Thread.sleep(200);
                     } catch (InterruptedException e) {
                     }
                 }
             }
         };
         _eventRunner.setDaemon(true);
         _eventRunner.start();
         
         if (_debug) {
             // Display more JVM infor right after the call initialization of the library.
             String fullVersion = System.getProperty("java.fullversion");
             if (fullVersion == null) {
                 fullVersion = System.getProperty("java.runtime.version") + " " + 
                     System.getProperty("java.vm.name");
             }
             System.out.println("Java Version   : " + fullVersion);
             System.out.println("Java VM Vendor : " + System.getProperty("java.vm.vendor"));
             System.out.println();
         }
         
         // Create the singleton
         _instance = new WrapperManager();
     }
 
     /*---------------------------------------------------------------
      * Native Methods
      *-------------------------------------------------------------*/
     private static native void nativeInit(boolean debug);
     private static native int nativeGetControlEvent();
     
     private static native void accessViolationInner();
     
     /*---------------------------------------------------------------
      * Public Methods
      *-------------------------------------------------------------*/
     /**
      * Obtain the current version of Wrapper.
      */
     public static String getVersion() {
         return WrapperInfo.getVersion();
     }
     
     /**
      * Obtain the build time of Wrapper.
      */
     public static String getBuildTime() {
         return WrapperInfo.getBuildTime();
     }
     
     /**
      * Returns the Id of the current JVM.  JVM Ids increment from 1 each time the wrapper
      *  restarts a new one.
      */
     public static int getJVMId() {
         return _jvmId;
     }
     
     /**
      * (Testing Method) Causes the WrapperManager to go into a state which makes the JVM appear
      *  to be hung when viewed from the native Wrapper code.  Does not have any effect when the
      *  JVM is not being controlled from the native Wrapper. Useful for testing the Wrapper 
      *  functions.
      */
     public static void appearHung() {
         System.out.println("WARNING: Making JVM appear to be hung...");
         _appearHung = true;
     }
     
     /**
      * (Testing Method) Cause an access violation within the Java code.  Useful for testing the
      *  Wrapper functions.  This currently only crashes Sun JVMs and takes advantage of 
      *  Bug #4369043
      */
     public static void accessViolation() {
         System.out.println("WARNING: Attempting to cause an access violation...");
         
         try {
             Class c = Class.forName("java.lang.String");
             java.lang.reflect.Method m = c.getDeclaredMethod(null, null);
         } catch(NoSuchMethodException ex) {
             // Correctly did not find method.  access_violation attempt failed.  Not Sun JVM?
         } catch(Exception ex) {
             if (ex instanceof NoSuchFieldException) {
                 // Can't catch this in a catch because the compiler doesn't think it is being thrown.
                 //	But it is thrown on IBM jvms at least
                 // Correctly did not find method.  access_violation attempt failed.  Not Sun JVM?
             } else {
                 // Shouldn't get here.
                 ex.printStackTrace();
             }
         }					
         
         System.out.println("  Attempt to cause access violation failed.  JVM is still alive.");
     }
 
     /**
      * (Testing Method) Cause an access violation within native JNI code.  Useful for testing the
      *  Wrapper functions. This currently causes the access violation by attempting to write to 
      *  a null pointer.
      */
     public static void accessViolationNative() {
         System.out.println("WARNING: Attempting to cause an access violation...");
         if (_libraryOK) {
             accessViolationInner();
         
             System.out.println("  Attempt to cause access violation failed.  JVM is still alive.");
         } else {
             System.out.println("  wrapper library not loaded.");
         }
     }
         
     /**
      * Returns true if the JVM was launched by the Wrapper application.  False if the JVM
      *  was launched manually without the Wrapper controlling it.
      */
     public static boolean isControlledByNativeWrapper() {
         return _key != null;
     }
     
     /**
      * Returns true if the Wrapper was launched as a service.  False if launched as a console.
      *  This can be useful if you wish to display a user interface when in Console mode.
      */
     public static boolean isLaunchedAsService() {
         return _service;
     }
     
     /**
      * Returns true if the wrapper.debug property is set the wrapper config file.
      */
     public static boolean isDebugEnabled() {
         return _debug;
     }
     
     /**
      * Start the Java side of the Wrapper code running.  This will make it
      *  possible for the native side of the Wrapper to detect that the Java
      *  Wrapper is up and running.
      */
     public static synchronized void start(WrapperListener listener, String[] args) {
         System.out.println("Wrapper (Version " + getVersion() + ")");
         System.out.println();
         
         // Make sure that the class has not already been disposed.
         if (_disposed) {
             throw new IllegalStateException("WrapperManager has already been disposed.");
         }
         
         if (_listener != null) {
             throw new IllegalStateException("WrapperManager has already been started with a WrapperListener.");
         }
         if (listener == null) {
             throw new IllegalStateException("A WrapperListener must be specified.");
         }
         _listener = listener;
         
         _args = args;
         
         startRunner();
         
         // If this JVM is being controlled by a native wrapper, then we want to
         //  wait for the command to start.  However, if this is a standalone
         //  JVM, then we want to start now.
         if (!isControlledByNativeWrapper()) {
             startInner();
         }
     }
     
     /**
      * Tells the native wrapper that the JVM wants to restart, then informs
      *	all listeners that the JVM is about to shutdown before killing the JVM.
      */
     public static void restart() {
         boolean stopping;
         synchronized(_instance) {
             stopping = _stopping;
             if (!stopping) {
                 _stopping = true;
             }
         }
         
         if (!stopping) {
             if (!_commRunnerStarted) {
                 startRunner();
                 // Wait to give the runner a chance to connect.
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                 }
             }
             
             // Always send the stop command
             sendCommand(WRAPPER_MSG_RESTART, "restart");
         }
         
         // Give the Wrapper a chance to register the stop command before stopping.
         // This avoids any errors thrown by the Wrapper because the JVM died before
         //  it was expected to.
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
         
         stopInner(0);
     }
     
     /**
      * Tells the native wrapper that the JVM wants to shut down, then informs
      *	all listeners that the JVM is about to shutdown before killing the JVM.
      */
     public static void stop(int exitCode) {
         boolean stopping;
         synchronized(_instance) {
             stopping = _stopping;
             if (!stopping) {
                 _stopping = true;
             }
         }
         
         if (!stopping) {
             if (!_commRunnerStarted) {
                 startRunner();
                 // Wait to give the runner a chance to connect.
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                 }
             }
             
             // Always send the stop command
             sendCommand(WRAPPER_MSG_STOP, Integer.toString(exitCode));
         }
         
         // Give the Wrapper a chance to register the stop command before stopping.
         // This avoids any errors thrown by the Wrapper because the JVM died before
         //  it was expected to.
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
         
         stopInner(exitCode);
     }
 
     /**
      * Signal the native wrapper that the startup is progressing but that more
      *  time is needed.
      */
     public static void signalStarting(int waitHint) {
         sendCommand(WRAPPER_MSG_START_PENDING, Integer.toString(waitHint));
     }
 
     /**
      * Signal the native wrapper that the shutdown is progressing but that more
      *  time is needed.
      */
     public static void signalStopping(int waitHint) {
         _stopping = true;
         sendCommand(WRAPPER_MSG_STOP_PENDING, Integer.toString(waitHint));
     }
     
     /**
      * This method should not normally be called by user code as it is called
      *  from within the stop and restart methods.  However certain applications
      *  which stop the JVM may need to call this method to let the wrapper code
      *  know that the shutdown was intentional.
      */
     public static void signalStopped(int exitCode) {
         _stopping = true;
         sendCommand(WRAPPER_MSG_STOPPED, Integer.toString(exitCode));
     }
     
     /**
      * Returns true if the ShutdownHook for the JVM has already been triggered.
      */
     public static boolean hasShutdownHookBeenTriggered() {
         return _hookTriggered;
     }
     
     /**
      * Requests that the Wrapper log a message at the specified log level.
      *  If the JVM is not being managed by the Wrapper then calls to this
      *  method will be ignored.  This method has been optimized to ignore
      *  messages at a log level which will not be logged given the current
      *  log levels of the Wrapper.
      * <p>
      * Log messages will currently by trimmed by the Wrapper at 4k (4096 bytes).
      * <p>
      * Because of differences in the way console output is collected and
      *  messages logged via this method, it is expected that interspersed
      *  console and log messages will not be in the correct order in the
      *  resulting log file.
      * <p>
      * This method was added to allow simple logging to the wrapper.log
      *  file.  This is not meant to be a full featured log file and should
      *  not be used as such.  Please look into a logging package for most
      *  application logging.
      *
      * @param logLevel The level to log the message at can be one of
      *                 WRAPPER_LOG_LEVEL_DEBUG, WRAPPER_LOG_LEVEL_INFO,
      *                 WRAPPER_LOG_LEVEL_STATUS, WRAPPER_LOG_LEVEL_WARN,
      *                 WRAPPER_LOG_LEVEL_ERROR, or WRAPPER_LOG_LEVEL_FATAL.
      * @param message The message to be logged.
      */
     public static void log(int logLevel, String message) {
         // Make sure that the logLevel is valid to avoid problems with the
         //  command sent to the server.
         
         if ((logLevel < WRAPPER_LOG_LEVEL_DEBUG) || (logLevel > WRAPPER_LOG_LEVEL_FATAL)) {
             throw new IllegalArgumentException( "The specified logLevel is not valid." );
         }
         if (message == null) {
             throw new IllegalArgumentException( "The message parameter can not be null." );
         }
         
         if (_lowLogLevel <= logLevel) {
             sendCommand((byte)(WRAPPER_MSG_LOG + logLevel), message);
         }
     }
     
     /*---------------------------------------------------------------
      * Constructors
      *-------------------------------------------------------------*/
     /** 
      * This class can not be instantiated.
      */
     private WrapperManager() {
     }
     
     /*---------------------------------------------------------------
      * Private methods
      *-------------------------------------------------------------*/
     /**
      * Dispose of all resources used by the WrapperManager.  Closes the server
      *	socket which is used to listen for events from the 
      */
     private static void dispose() {
         synchronized(_instance.getClass()) {
             _disposed = true;
             
             // Close the open socket if it exists.
             closeSocket();
             
             // Give the Connection Thread a chance to stop itself.
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {}
         }
     }
     
     /**
      * Informs the listener that it should start.
      */
     private static void startInner() {
         // Set the thread priority back to normal so that any spawned threads
         //	will use the normal priority
         int oldPriority = Thread.currentThread().getPriority();
         Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
         
         if (_debug) {
             System.out.println("calling listener.start()");
         }
         if (_listener != null) {
             // This is user code, so don't trust it.
             try {
                 Integer result = _listener.start(_args);
                 if (result != null) {
                     int exitCode = result.intValue();
                     // Signal the native code.
                     stop(exitCode);
                     // Won't make it here.
                     return;
                 }
             } catch (Throwable t) {
                 System.out.println("Error in WrapperListener.start callback.  " + t);
                 t.printStackTrace();
                 // Kill the JVM, but don't tell the wrapper that we want to stop.  This may be a problem with this instantiation only.
                 stopInner(1);
                 // Won't make it here.
                 return;
             }
         }
         if (_debug) {
             System.out.println("returned from listener.start()");
         }
         
         // Crank the priority back up.
         Thread.currentThread().setPriority(oldPriority);
         
         // Signal that the application has started.
         signalStarted();
     }
     
     private static void shutdownJVM(int exitCode) {
         // Signal that the application has stopped and the JVM is about to shutdown.
         //signalStopped(exitCode);
         
         // Give the native end of things a chance to receive the stopped event
         //  before actually killing the JVM
         //try {
         //	Thread.sleep(1000);
         //} catch (InterruptedException e) {
         //}
         
         // Do not call System.exit if this is the ShutdownHook
         if (Thread.currentThread() == _hook) {
             // Signal that the application has stopped and the JVM is about to shutdown.
             signalStopped(0);
             
             // Dispose the wrapper.  (If the hook runs, it will do this.)
             dispose();
             
             // This is the shutdown hook, so fall through because things are
             //  already shutting down.
         } else {
             //  We do not want the ShutdownHook to execute, so unregister it before calling exit
             if (_hook != null) {
                 Runtime.getRuntime().removeShutdownHook(_hook);
                 _hook = null;
             }
             // Signal that the application has stopped and the JVM is about to shutdown.
             signalStopped(0);
             
             // Dispose the wrapper.  (If the hook runs, it will do this.)
             dispose();
             
             if (_debug) {
                 System.out.println("calling System.exit(" + exitCode + ")");
             }
             System.exit(exitCode);
         }
     }
     
     /**
      * Informs the listener that the JVM will be shut down.
      */
     private static void stopInner(int exitCode) {
         boolean block;
         synchronized(_instance) {
             // Always set the stopping flag.
             _stopping = true;
             
             // Only one thread can be allowed to continue.
             if (_stoppingThread == null) {
                 _stoppingThread = Thread.currentThread();
                 block = false;
             } else {
                 if (Thread.currentThread() == _stoppingThread) {
                     throw new IllegalStateException("WrapperManager.stop() can not be called recursively.");
                 }
                 
                 if (Thread.currentThread() == _hook) {
                     // The hook should be allowed to fall through.
                     return;
                 }
                 block = true;
             }
         }
         
         if (block) {
             if (_debug) {
                 System.out.println("Thread, " + Thread.currentThread().getName() + ", waiting for the JVM to exit.");
             }
             
             // This thread needs to be put into an infinite loop until the JVM exits.
             //  This thread can not be allowed to return to the caller, but another
             //  thread is already responsible for shutting down the JVM, so this
             //  one can do nothing but wait.
             while(true) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {}
             }
         }
         
         if (_debug) {
             System.out.println("Thread, " + Thread.currentThread().getName() + ", handling the shutdown process.");
         }
         
         // Only stop the listener if the app has been started.
         int code = exitCode;
         if (_started) {
             // Set the thread priority back to normal so that any spawned threads
             //	will use the normal priority
             int oldPriority = Thread.currentThread().getPriority();
             Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
             
             if (_debug) {
                 System.out.println("calling listener.stop()");
             }
             if (_listener != null) {
                 // This is user code, so don't trust it.
                 try {
                     code = _listener.stop(code);
                 } catch (Throwable t) {
                     System.out.println("Error in WrapperListener.stop callback.  " + t);
                     t.printStackTrace();
                 }
             }
             if (_debug) {
                 System.out.println("returned from listener.stop()");
             }
             
             // Crank the priority back up.
             Thread.currentThread().setPriority(oldPriority);
         }
 
         shutdownJVM(code);
     }
     
     private static void signalStarted() {
         sendCommand(WRAPPER_MSG_STARTED, "");
         _started = true;
     }
     
     /**
      * Called by the native code when a control event is trapped by native code.
      * Can have the values: WRAPPER_CTRL_C_EVENT, WRAPPER_CTRL_CLOSE_EVENT, 
      *    WRAPPER_CTRL_LOGOFF_EVENT, or WRAPPER_CTRL_SHUTDOWN_EVENT
      * Calls 
      */
     private static void controlEvent(int event) {
         if (_debug) {
             String eventName;
             switch(event) {
             case WRAPPER_CTRL_C_EVENT:
                 eventName = "WRAPPER_CTRL_C_EVENT";
                 break;
             case WRAPPER_CTRL_CLOSE_EVENT:
                 eventName = "WRAPPER_CTRL_CLOSE_EVENT";
                 break;
             case WRAPPER_CTRL_LOGOFF_EVENT:
                 eventName = "WRAPPER_CTRL_LOGOFF_EVENT";
                 break;
             case WRAPPER_CTRL_SHUTDOWN_EVENT:
                 eventName = "WRAPPER_CTRL_SHUTDOWN_EVENT";
                 break;
             default:
                 eventName = "Unexpected event: " + event;
                 break;
             }
             System.out.println("Processing control event(" + eventName + ")");
         }
         
         // This is user code, so don't trust it.
         if (_listener != null) {
             try {
                 _listener.controlEvent(event);
             } catch (Throwable t) {
                 System.out.println("Error in WrapperListener.controlEvent callback.  " + t);
                 t.printStackTrace();
             }
         }
     }
 
     private static synchronized Socket openSocket() {
         if (_debug) {
             System.out.println("Open socket to wrapper...");
         }
 
         InetAddress iNetAddress;
         try {
             iNetAddress = InetAddress.getByName("127.0.0.1");
         } catch (UnknownHostException e) {
             // This is pretty fatal.
             System.out.println(e);
             stop(1);
             return null; //please the compiler
         }
         
         try {
             _socket = new Socket(iNetAddress, _port);
             if (_debug) {
                 System.out.println("Opened Socket");
             }
         } catch (BindException e) {
             System.out.println("Failed to bind to the Wrapper port.");
             System.out.println(e);
             // This is fatal because the port was bad.
             System.out.println("Exiting JVM...");
             System.exit(1);
         } catch (ConnectException e) {
             System.out.println("Failed to connect to the Wrapper.");
             System.out.println(e);
             // This is fatal because there is nobody listening.
             System.out.println("Exiting JVM...");
             System.exit(1);
         } catch (IOException e) {
             System.out.println(e);
             _socket = null;
             return null;
         }
         try {
             // Turn on the TCP_NODELAY flag.  This is very important for speed!!
             _socket.setTcpNoDelay(true);
             
             // Set the SO_TIMEOUT for the socket (max block time)
             if (_soTimeout > 0) {
                 _socket.setSoTimeout(_soTimeout);
             }
         } catch (IOException e) {
             System.out.println(e);
         }
         
         // Send the key back to the wrapper so that the wrapper can feel safe
         //  that it is talking to the correct JVM
         sendCommand(WRAPPER_MSG_KEY, _key);
             
         return _socket;
     }
     
     private static synchronized void closeSocket() {
         if (_socket != null) {
             if (_debug) {
                 System.out.println("Closing socket.");
             }
             
             try {
                 _socket.close();
             } catch (IOException e) {
             } finally {
                 _socket = null;
             }
         }
     }
     
     private static synchronized void sendCommand(byte code, String message) {
         if (_debug) {
             System.out.println("Send a packet " + code + " : " + message);
         }
         if (_appearHung) {
             // The WrapperManager is attempting to make the JVM appear hung, so do nothing
         } else {
             // Make a copy of the reference to make this more thread safe.
             Socket socket = _socket;
             if (socket == null && isControlledByNativeWrapper() && (!_stopping)) {
                 // The socket is not currently open, try opening it.
                 socket = openSocket();
             }
             
             if ((code == WRAPPER_MSG_START_PENDING) || (code == WRAPPER_MSG_STARTED)) {
                 // Set the last ping time so that the startup process does not time out
                 //  thinking that the JVM has not received a Ping for too long.
                 _lastPing = System.currentTimeMillis();
             }
             
             // If the socket is open, then send the command, otherwise just throw it away.
             if (socket != null) {
                 try {
                     // It is possible that a logged message is quite large.  Expand the size
                     // of the command buffer if necessary so that it can be included.  This
                     //  means that the command buffer will be the size of the largest message.
                     byte[] messageBytes = message.getBytes();
                     if ( _commandBuffer.length < messageBytes.length + 2 ) {
                         _commandBuffer = new byte[messageBytes.length + 2];
                     }
                     
                     // Writing the bytes one by one was sometimes causing the first byte to be lost.
                     // Try to work around this problem by creating a buffer and sending the whole lot
                     // at once.
                     _commandBuffer[0] = code;
                     System.arraycopy(messageBytes, 0, _commandBuffer, 1, messageBytes.length);
                     int len = messageBytes.length + 2;
                     _commandBuffer[len - 1] = 0;
                     
                     OutputStream os = socket.getOutputStream();
                     os.write(_commandBuffer, 0, len);
                     os.flush();
                 } catch (IOException e) {
                     System.out.println(e);
                     e.printStackTrace();
                     closeSocket();
                 }
             }
         }
     }
     
     /**
      * Loop reading packets from the native side of the Wrapper until the 
      *  connection is closed or the WrapperManager class is disposed.
      *  Each packet consists of a packet code followed by a null terminated
      *  string up to 256 characters in length.  If the entire packet has not
      *  yet been received, then it must not be read until the complete packet
      *  has arived.
      */
     private static void handleSocket() {
         byte[] buffer = new byte[256];
         try {
             if (_debug) {
                 System.out.println("handleSocket(" + _socket + ")");
             }
             DataInputStream is = new DataInputStream(_socket.getInputStream());
             while (!_disposed) {
                 try {
                     // A Packet code must exist.
                     byte code = is.readByte();
                     
                     // Always read from the buffer until a null '\0' is encountered.  But only
                     //  place the first 256 characters into the buffer.
                     byte b;
                     int i = 0;
                     do {
                         b = is.readByte();
                         if ((b != 0) && (i < 256)) {
                             buffer[i] = b;
                             i++;
                         }
                     } while (b != 0);
                     
                     String msg = new String(buffer, 0, i);
                     
                     if (_appearHung) {
                         // The WrapperManager is attempting to make the JVM appear hung,
                         //   so ignore all incoming requests
                     } else {
                         if (_debug) {
                             System.out.println("Received a packet " + code + " : " + msg);
                         }
                         
                         // Ok, we got a packet.  Do something with it.
                         switch(code) {
                         case WRAPPER_MSG_START:
                             startInner();
                             break;
                             
                         case WRAPPER_MSG_STOP:
                             // Don't do anything if we are already stopping
                             if (!_stopping) {
                                 stopInner(0);
                                 // Should never get back here.
                             }
                             break;
                             
                         case WRAPPER_MSG_PING:
                             _lastPing = System.currentTimeMillis();
                             sendCommand(WRAPPER_MSG_PING, "ok");
                             break;
                             
                         case WRAPPER_MSG_BADKEY:
                             // The key sent to the wrapper was incorrect.  We need to shutdown.
                             System.out.println("Authorization key rejected by Wrapper.  Exiting JVM.");
                             closeSocket();
                             stopInner(1);
                             break;
                             
                         case WRAPPER_MSG_LOW_LOG_LEVEL:
                             try {
                                 _lowLogLevel = Integer.parseInt( msg );
                                 if (_debug) {
                                     System.out.println("Wrapper Manager: LowLogLevel from Wrapper is " + _lowLogLevel);
                                 }
                             } catch (NumberFormatException e) {
                                 System.out.println("Encountered an Illegal LowLogLevel from the Wrapper: " + msg);
                             }
                             break;
                             
                         default:
                             // Ignore unknown messages
                             System.out.println("Wrapper code received an unknown packet type: " + code);
                             break;
                         }
                     }
                 } catch (InterruptedIOException e) {
                     long now = System.currentTimeMillis();
                     
                     // Unless the JVM is shutting dowm we want to show warning messages and maybe exit.
                    if (!_stopping) {
                         if (_debug) {
                             System.out.println("Read Timed out. (Last Ping was " + (now - _lastPing) + " milliseconds ago)");
                         }
                         
                         if (!_appearHung) {
                             long lastPingAge = now - _lastPing;
                             long eventRunnerAge = now - _eventRunnerTime;
                             
                             // We may have timed out because the system was extremely busy or suspended.
                             //  Only restart due to a lack of ping events if the event thread has been
                             //  running.
                             if (eventRunnerAge < 10000) {
                                 // How long has it been since we received the last ping from the Wrapper?
                                 if (lastPingAge > 120000) {
                                     // It has been more than 2 minutes, so just give up and kill the JVM
                                     System.out.println("JVM did not exit.  Give up.");
                                     System.exit(1);
                                 } else if (lastPingAge > 30000) {
                                     // It has been more than 30 seconds, so give a warning.
                                     System.out.println("The Wrapper code did not ping the JVM for " + (lastPingAge / 1000) + " seconds.  Quit and let the wrapper resynch.");
                                     
                                     // Don't do anything if we are already stopping
                                     if (!_stopping) {
                                         stopInner(1);
                                     }
                                 }
                             }
                         }
                     }
                 }
                 
                 // Check to see if all non-daemon threads have exited.
                 if (_started) {
                     checkThreads();
                 }
             }
             return;
 
         } catch (SocketException e) {
             if (_debug) {
                 if (_socket == null) {
                     // This error happens if the socket is closed while reading:
                     // java.net.SocketException: Descriptor not a socket: JVM_recv in socket input stream read
                 } else {
                     System.out.println("Closed socket: " + e);
                 }
             }
             return;
         } catch (IOException e) {
             // This means that the connection was closed.  Allow this to return.
             //System.out.println(e);
             //e.printStackTrace();
             return;
         }
     }
     
     protected static int getNonDaemonThreadCount() {
         Thread[] threads = new Thread[Thread.activeCount() * 2];
         Thread.enumerate(threads);
         
         // Only count any non daemon threads which are 
         //  still alive other than this thread.
         int liveCount = 0;
         for (int i = 0; i < threads.length; i++) {
             /*
             if (threads[i] != null) {
             System.out.println("Check " + threads[i].getName() + " daemon=" + 
             threads[i].isDaemon() + " alive=" + threads[i].isAlive());
             }
              */
             if ((threads[i] != null) && (threads[i].isAlive() && (!threads[i].isDaemon()))) {
                 // Do not count this thread or the wrapper connection thread
                 if ((Thread.currentThread() != threads[i]) && (threads[i] != _commRunner)) {
                     // Non-Daemon living thread
                     liveCount++;
                     //System.out.println("  -> Non-Daemon");
                 }
             }
         }
         //System.out.println("  => liveCount = " + liveCount);
         
         return liveCount;
     }
     
     /**
      * With a normal Java application, the JVM will exit when all non-daemon
      *  threads have completed.  This does not work correctly with the wrapper
      *  because the connection thread is not a daemon.  It would also cause
      *  problems because the wrapper would not know whether the exit had been
      *  intentional or not.  This method takes care of making sure that the
      *  JVM exits when it is supposed to and makes sure that the Wrapper is
      *  propperly informed.
      */
     private static void checkThreads() {
         int liveCount = getNonDaemonThreadCount();
         
         // There will always be one non-daemon thread alive.  This thread is either the main
         //  thread which has not yet completed, or a thread launched by java when the main
         //  thread completes whose job is to wait around for all other non-daemon threads to
         //  complete.  We are overriding that thread here.
         if (liveCount <= 1) {
             if (_debug) {
                 System.out.println("All non-daemon threads have stopped.  Exiting.");
             }
             
             // Exit normally
             WrapperManager.stop(0);
             // Will not get here.
         } else {
             // There are daemons running, let the JVM continue to run.
         }
     }
     
     private static void startRunner() {
         if (isControlledByNativeWrapper()) {
             if (_commRunner == null) {
                 // Create and launch a new thread to manage this connection
                 _commRunner = new Thread(_instance, WRAPPER_CONNECTION_THREAD_NAME);
                 // This thread can not be a daemon or the JVM will quit immediately
                 _commRunner.start();
             }
         }
     }
     
     /*---------------------------------------------------------------
      * Runnable Methods
      *-------------------------------------------------------------*/
     public void run() {
         _commRunnerStarted = true;
         
         // This thread needs to have a very high priority so that it never
         //	gets put behind other threads.
         Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
         
         // Initialize the last ping time.
         _lastPing = System.currentTimeMillis();
         
         boolean gotPortOnce = false;
         while (!_disposed) {
             try {
                 try {
                     openSocket();
                     if (_socket != null) {
                         handleSocket();
                     } else {
                         // Failed, so wait for just a moment
                         try {
                             Thread.sleep(10);
                         } catch (InterruptedException e) {
                         }
                     }
                 } finally {
                     // Always close the socket here.
                     closeSocket();
                 }
             } catch (ThreadDeath td) {
                 System.out.println(_warning.format("SERVER_DAEMON_KILLED"));
             } catch (Throwable t) {
                 if (! _shuttingDown) {
                     // Show a stack trace here because this is fairly critical
                     System.out.println(_error.format("SERVER_DAEMON_DIED"));
                     t.printStackTrace();
                 }
             }
         }
         if (_debug) {
             System.out.println(_info.format("SERVER_DAEMON_SHUT_DOWN"));
         }
     }
 }
 
