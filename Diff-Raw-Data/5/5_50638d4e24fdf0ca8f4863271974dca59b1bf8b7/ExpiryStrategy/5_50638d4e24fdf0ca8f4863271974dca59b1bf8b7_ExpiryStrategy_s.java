 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections.expiry;
 
 import java.lang.ref.WeakReference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 /**
  * Expiry strategy. A strategy maintains a time-out and a time-out precision.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class ExpiryStrategy extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = ExpiryStrategy.class.getName();
 
    /**
     * The number of instances of this class.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * Lock object for <code>INSTANCE_COUNT</code>.
     */
    private static final Object INSTANCE_COUNT_LOCK = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ExpiryStrategy</code>.
     *
     * @param timeOut
     *    the time-out, in milliseconds.
     *
     * @param precision
     *    the time-out precision, in milliseconds.
     *
     * @throws IllegalArgumentException
     *    if <code>timeOut   &lt; 1L
     *          || precision &lt; 1L
     *          || timeOut   &lt; precision</code>
     */
    public ExpiryStrategy(final long timeOut,
                          final long precision)
    throws IllegalArgumentException {
 
       // Determine instance number
       synchronized (INSTANCE_COUNT_LOCK) {
          _instanceNum = INSTANCE_COUNT++;
       }
 
       final String CONSTRUCTOR_DETAIL = "#"
                                       + _instanceNum
                                       + " [timeOut="
                                       + timeOut
                                       + "L; precision="
                                       + precision
                                       + "L]";
       final String THIS_METHOD = "<init>" + CONSTRUCTOR_DETAIL;
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, CONSTRUCTOR_DETAIL);
 
       // Check preconditions
       if (timeOut < 1) {
          final String DETAIL = "timeOut (" + timeOut + "L) < 1L";
          Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                    Utils.getCallingClass(),
                                    Utils.getCallingMethod(),
                                    DETAIL);
          throw new IllegalArgumentException(DETAIL);
 
       } else if (precision < 1) {
          final String DETAIL = "precision (" + precision + "L) < 1L";
          Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                    Utils.getCallingClass(),
                                    Utils.getCallingMethod(),
                                    DETAIL);
          throw new IllegalArgumentException(DETAIL);
 
       } else if (timeOut < precision) {
          final String DETAIL = "timeOut ("
                              + timeOut
                              + "L) < precision ("
                              + precision
                              + "L)";
          Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                    Utils.getCallingClass(),
                                    Utils.getCallingMethod(),
                                    DETAIL);
          throw new IllegalArgumentException(DETAIL);
       }
 
       // Determine number of slots
       long slotCount = timeOut / precision;
       long remainder = timeOut % precision;
       if (remainder != 0L) {
          slotCount++;
       }
 
       // Initialize fields
       _timeOut   = timeOut;
       _precision = precision;
       _slotCount = (int) slotCount;
       _folders   = new ArrayList();
       _asString  = CLASSNAME + ' ' + CONSTRUCTOR_DETAIL;
 
       // Create and start the timer thread. If no other threads are active,
       // then neither should this timer thread, so do not mark as a daemon
       // thread.
       _timerThread = new TimerThread();
       _timerThread.setDaemon(false);
       _timerThread.start();
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, CONSTRUCTOR_DETAIL);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The instance number of this instance.
     */
    private final int _instanceNum;
 
    /**
     * The time-out, in milliseconds.
     */
    private final long _timeOut;
 
    /**
     * The time-out precision, in milliseconds.
     */
    private final long _precision;
 
    /**
     * The number of slots that should be used by expiry collections that use
     * this strategy.
     */
    private final int _slotCount;
 
    /**
     * A textual presentation of this object. This is returned by
     * {@link #toString()}.
     */
    private final String _asString;
 
    /**
     * The list of folders associated with this strategy.
     */
    private final ArrayList _folders;
 
    /**
     * The timer thread. Not <code>null</code>.
     */
    private final TimerThread _timerThread;
 
    /**
     * Flag that indicates if the time thread should stop or not. Initially
     * <code>false</code>, ofcourse.
     */
    private boolean _stop;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the time-out.
     *
     * @return
     *    the time-out, in milliseconds.
     */
    public final long getTimeOut() {
       return _timeOut;
    }
 
    /**
     * Returns the time-out precision.
     *
     * @return
     *    the time-out precision, in milliseconds.
     */
    public final long getPrecision() {
       return _precision;
    }
 
    /**
     * Returns the number of slots that should be used by expiry collections
     * that use this strategy.
     *
     * @return
     *    the slot count, always &gt;= 1.
     */
    public final int getSlotCount() {
       return _slotCount;
    }
 
    /**
     * Callback method indicating an <code>ExpiryFolder</code> is now
     * associated with this strategy.
     *
     * @param folder
     *    the {@link ExpiryFolder} that is now associated with this strategy,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this strategy was already stopped.
     *
     * @throws IllegalArgumentException
     *    if <code>folder == null</code>.
     */
    void folderAdded(ExpiryFolder folder)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       if (_stop) {
          throw new IllegalStateException("Already stopped.");
       }
 
       // Check arguments
       MandatoryArgumentChecker.check("folder", folder);
 
       // XXX: Review this log message. Generally, toString() is not wise.
       Log.log_1401(folder.toString(), toString());
 
       synchronized (_folders) {
          _folders.add(new WeakReference(folder));
       }
    }
 
    /**
     * Stops the thread that generates ticks that are passed to the registered
     * expiry folders.
     *
     * @throws IllegalStateException
     *    if this strategy was already stopped.
     */
    public void stop()
    throws IllegalStateException {
 
       // Check state
       if (_stop) {
          throw new IllegalStateException("Already stopped.");
       }
 
       // Set the stop flag
       _stop = true;
 
       // Notify the timer thread
       _timerThread.interrupt();
 
       // Notify all the associated ExpiryFolder instances that we are stopping
       for (int i = 0; i < _folders.size(); i++) {
          WeakReference ref = (WeakReference) _folders.get(i);
         ExpiryFolder folder = ref.get();
          if (folder != null) {
            f.strategyStopped();
          }
       }
    }
 
    /**
     * Callback method indicating the next tick has taken place. This method is
     * called from (and on) the timer thread.
     */
    private void doTick() {
 
       // Do nothing if this strategy was already stopped
       if (_stop) {
          return;
       }
 
       int emptyRefIndex = -1;
 
       synchronized (_folders) {
          int count = _folders.size();
          for (int i = 0; i < count; i++) {
             WeakReference ref = (WeakReference) _folders.get(i);
             ExpiryFolder folder = (ExpiryFolder) ref.get();
             if (folder != null) {
                folder.tick();
             } else {
                emptyRefIndex = i;
             }
          }
 
          // Remove last empty WeakReference
          if (emptyRefIndex >= 0) {
             _folders.remove(emptyRefIndex);
          }
       }
    }
 
    /**
     * Returns a textual representation of this object.
     *
     * @return
     *    a textual representation of this object, never <code>null</code>.
     */
    public String toString() {
       return _asString;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Timer thread for an expiry strategy. It calls back the expiry strategy
     * at each so-called 'tick'. The interval between ticks is the precision of
     * the strategy.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private final class TimerThread extends Thread {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>TimerThread</code>.
        */
       public TimerThread() {
          super(ExpiryStrategy.this.toString() + " timer thread");
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Runs this thread. The thread keeps running until the expiry strategy
        * is stopped.
        */
       public void run() {
 
          Log.log_1402(getName());
 
          long now  = System.currentTimeMillis();
          long next = now + _precision;
 
          while (! _stop) {
             boolean interrupted;
             long sleep = (next - now);
             if (sleep > 0) {
                Log.log_1404(sleep);
                try {
                   Thread.sleep(sleep);
                   interrupted = false;
 
                // Sleep was interrupted
                } catch (InterruptedException exception) {
                   interrupted = true;
                }
 
                // Determine how much time we spent since we started sleeping
                long after = System.currentTimeMillis();
                long slept = after - now;
                now        = after;
 
                // Perform logging
                if (interrupted) {
                   Log.log_1405(slept);
                } else {
                   Log.log_1406(slept);
                }
             }
 
             // If we should stop, then exit the loop
             if (_stop) {
                break;
             }
 
             while (next <= now) {
                Log.log_1407();
                doTick();
                now = System.currentTimeMillis();
                next += _precision;
             }
          }
 
          Log.log_1403(getName());
       }
 
       /**
        * Returns a textual representation of this timer thread object.
        *
        * @return
        *    a textual representation of this object, never <code>null</code>.
        */
       public String toString() {
          return getName();
       }
    }
 }
