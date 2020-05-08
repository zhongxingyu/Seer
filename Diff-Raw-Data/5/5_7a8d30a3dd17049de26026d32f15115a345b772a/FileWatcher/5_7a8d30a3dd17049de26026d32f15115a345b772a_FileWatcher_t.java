 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.io;
 
 import java.io.File;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * File watcher thread. This thread checks if a file changed and if it has, it
  * notifies the listener. The check is performed every <em>n</em> seconds,
  * where <em>n</em> can be configured.
  *
  * <p>Initially this thread will be a daemon thread. This can be changed by
  * calling {@link #setDaemon(boolean)}.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class FileWatcher extends Thread {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = FileWatcher.class.getName();
 
    /**
     * Instance counter. Used to generate a unique ID for each instance.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * State in which this file watcher thread is not running.
     */
    private static final int NOT_RUNNING = 1;
 
    /**
     * State in which this file watcher thread is currently running and has not
     * been told to stop.
     */
    private static final int RUNNING = 2;
 
    /**
     * State in which this file watcher thread is currently running, but has
     * been told to stop.
     */
    private static final int SHOULD_STOP = 3;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified file, with the
     * specified interval.
     *
     * @param file
     *    the name of the file to watch, cannot be <code>null</code>.
     *
     * @param interval
     *    the interval in seconds, must be greater than or equal to 1.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || listener == null || interval &lt; 1</code>
     */
    public FileWatcher(String file, int interval, Listener listener)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("file", file, "listener", listener);
       if (interval < 1) {
          throw new IllegalArgumentException("interval (" + interval + ") < 1");
       }
 
       // Initialize the fields
       _instanceID    = INSTANCE_COUNT++;
       _file          = new File(file);
       _interval      = interval;
       _listener      = listener;
       _listenerClass = listener.getClass().getName();
       _state         = NOT_RUNNING;
 
       // Configure thread as daemon
       setDaemon(true);
 
       // Set the name of this thread
      FastStringBuffer name = new FastStringBuffer(47, CLASSNAME);
       name.append(' ');
       name.append(_instanceID);
       name.append(" [file=\"");
       name.append(file);
       name.append("\"; interval=");
       name.append(interval);
       name.append(']');
       setName(name.toString());
 
       // Immediately check if the file can be read from
       firstCheck();
    }
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified file.
     *
     * <p>The interval must be set before the thread can be started.
     *
     * @param file
     *    the name of the file to watch, cannot be <code>null</code>.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || listener == null</code>
     *
     * @since XINS 1.2.0
     */
    public FileWatcher(String file, Listener listener)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("file", file, "listener", listener);
 
       // Initialize the fields
       _instanceID    = INSTANCE_COUNT++;
       _file          = new File(file);
       _interval      = 0;
       _listener      = listener;
       _listenerClass = listener.getClass().getName();
       _state         = NOT_RUNNING;
 
       // Configure thread as daemon
       setDaemon(true);
 
       // Immediately check if the file can be read from
       firstCheck();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Unique instance identifier.
     */
    private final int _instanceID;
 
    /**
     * The file to watch. Not <code>null</code>.
     */
    private final File _file;
 
    /**
     * Delay in seconds, at least 1. When the interval is uninitialized, the
     * value of this field is less than 1.
     */
    private int _interval;
 
    /**
     * The listener. Not <code>null</code>
     */
    private final Listener _listener;
 
    /**
     * The name of the class of the listener. Not <code>null</code>
     */
    private final String _listenerClass;
 
    /**
     * Timestamp of the last modification of the file. The value
     * <code>-1L</code> indicates that the file could not be found the last
     * time this was checked.
     *
     * <p>Initially this field is <code>-1L</code>.
     */
    private long _lastModified;
 
    /**
     * Current state. Never <code>null</code>. Value is one of the following
     * values:
     *
     * <ul>
     *    <li>{@link #NOT_RUNNING}
     *    <li>{@link #RUNNING}
     *    <li>{@link #SHOULD_STOP}
     * </ul>
     *
     * Once the thread is stopped, the state will be changed to
     * {@link #NOT_RUNNING} again.
     */
    private int _state;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Performs the first check on the file to determine the date the file was
     * last modified. This method is called from the constructors. If the file
     * cannot be accessed due to a {@link SecurityException}, then this
     * exception is logged and ignored.
     */
    private void firstCheck() {
 
       String subjectMethod = "canRead";
       try {
          if (_file.canRead()) {
             subjectMethod = "lastModified()";
             _lastModified = _file.lastModified();
          }
 
       // Ignore a SecurityException
       } catch (SecurityException exception) {
          String thisMethod = "<init>(java.lang.String,int,"
                                   + Listener.class.getName()
                                   + ")";
          String subjectClass = "java.io.File";
          Utils.logIgnoredException(exception,
                                    CLASSNAME,    thisMethod,
                                    subjectClass, subjectMethod);
       }
    }
 
    /**
     * Runs this thread. This method should not be called directly, call
     * {@link #start()} instead. That method will call this method.
     *
     * @throws IllegalStateException
     *    if <code>{@link Thread#currentThread()} != this</code>, if the thread
     *    is already running or should stop, or if the interval was not set yet.
     */
    public void run() throws IllegalStateException {
 
       // Check preconditions
       if (Thread.currentThread() != this) {
          throw new IllegalStateException("Thread.currentThread() != this");
       } else if (_state == RUNNING) {
          throw new IllegalStateException("The thread is already running.");
       } else if (_state == SHOULD_STOP) {
          throw new IllegalStateException("The thread should stop running.");
       } else if (_interval < 1) {
          throw new IllegalStateException("The interval has not been set yet.");
       }
 
       Log.log_1200(_instanceID, _file.getPath(), _interval);
 
       // Move to the RUNNING state
       synchronized (this) {
          _state = RUNNING;
       }
 
       // Loop while we should keep running
       boolean shouldStop = false;
       while (! shouldStop) {
          try {
             while(! shouldStop) {
 
                // Wait for the designated amount of time
                sleep(((long)_interval) * 1000L);
 
                // Should we stop?
                synchronized (this) {
                   shouldStop = (_state != RUNNING);
                }
 
                // If we do not have to stop yet, check if the file changed
                if (! shouldStop) {
                   check();
                }
             }
          } catch (InterruptedException exception) {
             // TODO: (#HERE#) Compute how much time we still need to sleep
             // before checking the file, depending on the new interval (if
             // that caused the InterruptedException here)
          }
       }
 
       // Thread stopped
       Log.log_1203(_instanceID);
    }
 
    /**
     * Returns the current interval.
     *
     * @return interval
     *    the current interval in seconds, always greater than or equal to 1,
     *    except if the interval is not initialized yet, in which case 0 is
     *    returned.
     */
    public synchronized int getInterval() {
       return _interval;
    }
 
    /**
     * Changes the file check interval.
     *
     * @param newInterval
     *    the new interval in seconds, must be greater than or equal to 1.
     *
     * @throws IllegalArgumentException
     *    if <code>interval &lt; 1</code>
     */
    public synchronized void setInterval(int newInterval)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (newInterval < 1) {
          throw new IllegalArgumentException("newInterval (" + newInterval + ") < 1");
       }
 
       // Change the interval
       if (newInterval != _interval) {
          Log.log_1201(_instanceID, _file.getPath(), _interval, newInterval);
          _interval = newInterval;
       }
 
       // TODO: Interrupt the thread (see #HERE#)
    }
 
    /**
     * Stops this thread.
     *
     * @throws IllegalStateException
     *    if the thread is currently not running or already stopping.
     */
    public synchronized void end() throws IllegalStateException {
 
       // Check state
       if (_state == NOT_RUNNING) {
          throw new IllegalStateException("The thread is currently not running.");
       } else if (_state == SHOULD_STOP) {
          throw new IllegalStateException("The thread is already stopping.");
       }
 
       Log.log_1202(_instanceID, _file.getPath());
 
       // Change the state and interrupt the thread
       _state = SHOULD_STOP;
       this.interrupt();
    }
 
    /**
     * Checks if the file changed. The following algorithm is used:
     *
     * <ul>
     *    <li>check if the file is readable;
     *    <li>if so, then determine when the file was last modified;
     *    <li>if either the file existence check or the file modification check
     *        causes a {@link SecurityException} to be thrown, then
     *        {@link Listener#securityException(SecurityException)} is called
     *        and the method returns;
     *    <li>otherwise if the file is not readable (it may not exist), then
     *        {@link Listener#fileNotFound()} is called and the method returns;
     *    <li>otherwise if the file is readable, but previously was not,
     *        then {@link Listener#fileFound()} is called and the method
     *        returns;
     *    <li>otherwise if the file was modified, then {@link Listener#fileModified()} is
     *        called and the method returns;
     *    <li>otherwise the file was not modified, then
     *        {@link Listener#fileNotModified()} is called and the method
     *        returns.
     * </ul>
     *
     * @since XINS 1.2.0
     */
    public synchronized void check() {
 
       String thisMethod = "check()";
 
       // Variable to store the file modification timestamp in. The value -1
       // indicates the file does not exist.
       long lastModified;
 
       // Check if the file can be read from and if so, when it was last
       // modified
       try {
          if (_file.canRead()) {
             lastModified = _file.lastModified();
          } else {
             lastModified = -1L;
          }
 
       // Authorisation problem; our code is not allowed to call canRead()
       // and/or lastModified() on the File object
       } catch (SecurityException securityException) {
 
          // Notify the listener
          try {
             _listener.securityException(securityException);
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             String subjectMethod = "securityException(java.lang.SecurityException)";
             Utils.logIgnoredException(exception,
                                       CLASSNAME,      thisMethod,
                                       _listenerClass, subjectMethod);
          }
 
          // Short-circuit
          return;
       }
 
       // File can not be found
       if (lastModified == -1L) {
 
          // Set _lastModified to -1, which indicates the file did not exist
          // last time it was checked.
          _lastModified = -1L;
 
          // Notify the listener
          try {
             _listener.fileNotFound();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             String subjectMethod = "fileNotFound()";
             Utils.logIgnoredException(exception,
                                       CLASSNAME,      thisMethod,
                                       _listenerClass, subjectMethod);
          }
 
       // Previously the file could not be found, but now it can
       } else if (_lastModified == -1L) {
 
          // Update the field that stores the last known modification date
          _lastModified = lastModified;
 
          // Notify the listener
          try {
             _listener.fileFound();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             String subjectMethod = "fileFound()";
             Utils.logIgnoredException(exception,
                                       CLASSNAME,      thisMethod,
                                       _listenerClass, subjectMethod);
          }
 
       // File has been modified
       } else if (lastModified != _lastModified) {
 
          // Update the field that stores the last known modification date
          _lastModified = lastModified;
 
          // Notify listener
          try {
             _listener.fileModified();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             String subjectMethod = "fileModified()";
             Utils.logIgnoredException(exception,
                                       CLASSNAME,      thisMethod,
                                       _listenerClass, subjectMethod);
          }
 
       // File has not been modified
       } else {
 
          // Notify listener
          try {
             _listener.fileNotModified();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             String subjectMethod = "fileNotModified()";
             Utils.logIgnoredException(exception,
                                       CLASSNAME,      thisMethod,
                                       _listenerClass, subjectMethod);
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Interface for file watcher listeners.
     *
     * <p>Note that exceptions thrown by these callback methods will be ignored
     * by the <code>FileWatcher</code>.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    public interface Listener {
 
       /**
        * Callback method, called if the file is checked but cannot be found.
        * This method is called the first time the file is determined not to
        * exist, but also each consecutive time the file is still determined
        * not to be found.
        */
       void fileNotFound();
 
       /**
        * Callback method, called if the file is found for the first time since
        * the <code>FileWatcher</code> was started. Each consecutive time the
        * file still exists (and is readable), either
        * {@link #fileModified()} or {@link #fileNotModified()} is called.
        */
       void fileFound();
 
       /**
        * Callback method, called if an authorisation error prevents that the
        * file is checked for existence and last modification date.
        *
        * @param exception
        *    the caught exception, not <code>null</code>.
        */
       void securityException(SecurityException exception);
 
       /**
        * Callback method, called if the file was checked and found to be
        * modified.
        */
       void fileModified();
 
       /**
        * Callback method, called if the file was checked but found not to be
        * modified.
        */
       void fileNotModified();
    }
 }
