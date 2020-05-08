 /*
  * $Id$
  */
 package org.xins.server;
 
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Base class for function implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class Function
 extends Object
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified value is <code>null</code> or an empty string.
     * Only if it is then <code>true</code> is returned.
     *
     * @param value
     *    the value to check.
     *
     * @return
     *    <code>true</code> if and only if <code>value == null ||
     *    value.length() == 0</code>.
     */
    protected static final boolean isMissing(String value) {
       return value == null || value.length() == 0;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Function</code> object.
     *
     * @param api
     *    the API to which this function belongs, not <code>null</code>.
     *
     * @param name
     *    the name, not <code>null</code>.
     *
     * @param version
     *    the version of the specification this function implements, not
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || name == null || version == null</code>.
     */
    protected Function(API api, String name, String version)
    throws IllegalArgumentException {
 
       // Check argument
       MandatoryArgumentChecker.check("api", api, "name", name, "version", version);
 
       _log     = Logger.getLogger(getClass().getName());
       _api     = api;
       _name    = name;
       _version = version;
 
       _api.functionAdded(this);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The logger used by this function. This field is initialized by the
     * constructor and set to a non-<code>null</code> value.
     */
    private final Logger _log;
 
    /**
     * The API implementation this function is part of.
     */
    private final API _api;
 
    /**
     * The name of this function.
     */
    private final String _name;
 
    /**
     * The version of the specification this function implements.
     */
    private final String _version;
 
    /**
     * Lock object for <code>_callCount</code>.
     */
    private final Object _callCountLock = new Object();
 
    /**
     * The total number of calls executed up until now.
     */
    private int _callCount;
 
    /**
     * Statistics object linked to this function.
     */
    private final Statistics _statistics = new Statistics();
 
    /**
     * Lock object for a successful call.
     */
    private final Object _successfulCallLock = new Object();
 
    /**
     * Lock object for an unsuccessful call.
     */
    private final Object _unsuccessfulCallLock = new Object();
 
    /**
     * Buffer for log messages for successful calls. This field is
     * initialized at construction time and cannot be <code>null</code>.
     */
    private final FastStringBuffer _successfulCallStringBuffer = new FastStringBuffer(256);
 
    /**
     * Buffer for log messages for unsuccessful calls. This field is
     * initialized at construction time and cannot be <code>null</code>.
     */
    private final FastStringBuffer _unsuccessfulCallStringBuffer = new FastStringBuffer(256);
 
    /**
     * The number of successful calls executed up until now.
     */
    private int _successfulCalls;
 
    /**
     * The number of unsuccessful calls executed up until now.
     */
    private int _unsuccessfulCalls;
 
    /**
     * The start time of the most recent successful call.
     */
    private long _lastSuccessfulStart;
 
    /**
     * The start time of the most recent unsuccessful call.
     */
    private long _lastUnsuccessfulStart;
 
    /**
     * The duration of the most recent successful call.
     */
    private long _lastSuccessfulDuration;
 
    /**
     * The duration of the most recent unsuccessful call.
     */
    private long _lastUnsuccessfulDuration;
 
    /**
     * The total duration of all successful calls up until now.
     */
    private long _successfulDuration;
 
    /**
     * The total duration of all unsuccessful calls up until now.
     */
    private long _unsuccessfulDuration;
 
    /**
     * The minimum time a successful call took.
     */
    private long _successfulMin = Long.MAX_VALUE;
 
    /**
     * The minimum time an unsuccessful call took.
     */
    private long _unsuccessfulMin = Long.MAX_VALUE;
 
    /**
     * The start time of the successful call that took the shortest.
     */
    private long _successfulMinStart;
 
    /**
     * The start time of the unsuccessful call that took the shortest.
     */
    private long _unsuccessfulMinStart;
 
    /**
     * The duration of the successful call that took the longest.
     */
    private long _successfulMax;
 
    /**
     * The duration of the unsuccessful call that took the longest.
     */
    private long _unsuccessfulMax;
 
    /**
     * The start time of the successful call that took the longest.
     */
    private long _successfulMaxStart;
 
    /**
     * The start time of the unsuccessful call that took the longest.
     */
    private long _unsuccessfulMaxStart;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the logger associated with this function.
     *
     * @return
     *    the associated logger, constant, and cannot be <code>null</code>.
     */
    final Logger getLogger() {
       return _log;
    }
 
    /**
     * Returns the name of this function.
     *
     * @return
     *    the name, not <code>null</code>.
     */
    final String getName() {
       return _name;
    }
 
    /**
     * Returns the specification version for this function.
     *
     * @return
     *    the version, not <code>null</code>.
     */
    final String getVersion() {
       return _version;
    }
 
    /**
     * Returns the call statistics for this function.
     *
     * @return
     *    the statistics, never <code>null</code>.
     */
    final Statistics getStatistics() {
       return _statistics;
    }
 
    /**
     * Assigns a new call ID for the caller. Every call to this method will
     * return an increasing number.
     *
     * @return
     *    the assigned call ID, &gt;= 0.
     */
    final int assignCallID() {
       synchronized (_callCountLock) {
          return _callCount++;
       }
    }
 
    /**
     * Handles a call to this function.
     *
     * @param context
     *    the context for this call, never <code>null</code>.
     *
     * @throws Throwable
     *    if anything goes wrong.
     */
    protected abstract void handleCall(CallContext context)
    throws Throwable;
 
    /**
     * Callback method that may be called after a call to this function. This
     * method will store statistics-related information.
     *
     * <p />This method does not <em>have</em> to be called. If statistics
     * gathering is disabled, then this method should not be called.
     *
     * @param context
     *    the used call context, not <code>null</code>.
     *
     * @param success
     *    indication if the call was successful.
     *
     * @param code
     *    the function result code, or <code>null</code>.
     *
     * @deprecated
     *    Deprecated since XINS 0.32. Use
     *    {@link #performedCall(CallContext,ResultCode)} instead.
     */
    final void performedCall(CallContext context, boolean success, String code) {
       long start    = context.getStart();
       long duration = System.currentTimeMillis() - start;
       boolean debugEnabled = context.isDebugEnabled();
       String message = null;
       if (success) {
          if (debugEnabled) {
             synchronized (_successfulCallStringBuffer) {
                _successfulCallStringBuffer.clear();
                _successfulCallStringBuffer.append("Call succeeded. Duration: ");
                _successfulCallStringBuffer.append(String.valueOf(duration));
                _successfulCallStringBuffer.append(" ms.");
                if (code != null) {
                   _successfulCallStringBuffer.append(" Code: \"");
                   _successfulCallStringBuffer.append(code);
                   _successfulCallStringBuffer.append("\".");
                }
                message = _successfulCallStringBuffer.toString();
             }
          }
 
          synchronized (_successfulCallLock) {
             _lastSuccessfulStart    = start;
             _lastSuccessfulDuration = duration;
             _successfulCalls++;
             _successfulDuration += duration;
             _successfulMin = _successfulMin > duration ? duration : _successfulMin;
             _successfulMax = _successfulMax < duration ? duration : _successfulMax;
             _successfulMinStart = (_successfulMin == duration) ? start : _successfulMinStart;
             _successfulMaxStart = (_successfulMax == duration) ? start : _successfulMaxStart;
          }
       } else {
          if (debugEnabled) {
             synchronized (_unsuccessfulCallStringBuffer) {
                _unsuccessfulCallStringBuffer.clear();
                _unsuccessfulCallStringBuffer.append("Call failed. Duration: ");
                _unsuccessfulCallStringBuffer.append(String.valueOf(duration));
                _unsuccessfulCallStringBuffer.append(" ms.");
                if (code != null) {
                   _unsuccessfulCallStringBuffer.append(" Code: \"");
                   _unsuccessfulCallStringBuffer.append(code);
                   _unsuccessfulCallStringBuffer.append("\".");
                }
                message = _unsuccessfulCallStringBuffer.toString();
             }
          }
 
          synchronized (_unsuccessfulCallLock) {
             _lastUnsuccessfulStart    = start;
             _lastUnsuccessfulDuration = duration;
             _unsuccessfulCalls++;
             _unsuccessfulDuration += duration;
             _unsuccessfulMin = _unsuccessfulMin > duration ? duration : _unsuccessfulMin;
             _unsuccessfulMax = _unsuccessfulMax < duration ? duration : _unsuccessfulMax;
             _unsuccessfulMinStart = (_unsuccessfulMin == duration) ? start : _unsuccessfulMinStart;
             _unsuccessfulMaxStart = (_unsuccessfulMax == duration) ? start : _unsuccessfulMaxStart;
          }
       }
 
       if (debugEnabled) {
          context.debug(message);
       }
    }
 
    /**
     * Callback method that may be called after a call to this function. This
     * method will store statistics-related information.
     *
     * <p />This method does not <em>have</em> to be called. If statistics
     * gathering is disabled, then this method should not be called.
     *
     * @param context
     *    the used call context, not <code>null</code>.
     *
     * @param code
     *    the function result code, or <code>null</code>.
     *
     * @since XINS 0.32
     */
    final void performedCall(CallContext context, ResultCode code) {
       if (code == null) {
          performedCall(context, true, null);
       } else {
          performedCall(context, code.getSuccess(), code.getValue());
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Call statistics pertaining to a certain function.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     */
    final class Statistics extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Statistics</code> object.
        */
       private Statistics() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the number of successful calls executed up until now.
        *
        * @return
        *    the number of successful calls executed up until now.
        */
       public int getSuccessfulCalls() {
          return _successfulCalls;
       }
 
       /**
        * Returns the number of unsuccessful calls executed up until now.
        *
        * @return
        *    the number of unsuccessful calls executed up until now.
        */
       public int getUnsuccessfulCalls() {
          return _unsuccessfulCalls;
       }
 
       /**
        * Returns the start time of the most recent successful call.
        *
        * @return
        *    the start time of the most recent successful call.
        */
       public long getLastSuccessfulStart() {
          return _lastSuccessfulStart;
       }
 
       /**
        * Returns the start time of the most recent unsuccessful call.
        *
        * @return
        *    the start time of the most recent unsuccessful call.
        */
       public long getLastUnsuccessfulStart() {
          return _lastUnsuccessfulStart;
       }
 
       /**
        * Returns the duration of the most recent successful call.
        *
        * @return
        *    the duration of the most recent successful call.
        */
       public long getLastSuccessfulDuration() {
          return _lastSuccessfulDuration;
       }
 
       /**
        * Returns the duration of the most recent unsuccessful call.
        *
        * @return
        *    the duration of the most recent unsuccessful call.
        */
       public long getLastUnsuccessfulDuration() {
          return _unsuccessfulDuration;
       }
 
       /**
        * Returns the total duration of all successful calls up until now.
        *
        * @return
        *    the total duration of all successful calls up until now.
        */
       public long getSuccessfulDuration() {
          return _successfulDuration;
       }
 
       /**
        * Returns the total duration of all unsuccessful calls up until now.
        *
        * @return
        *    the total duration of all unsuccessful calls up until now.
        */
       public long getUnsuccessfulDuration() {
          return _unsuccessfulDuration;
       }
 
       /**
        * Returns the minimum time a successful call took.
        *
        * @return
        *    the minimum time a successful call took.
        */
       public long getSuccessfulMin() {
          return _successfulMin;
       }
 
       /**
        * Returns the start time of the successful call that took the shortest.
        *
        * @return
        *    the start time of the successful call that took the shortest.
        */
       public long getSuccessfulMinStart() {
         return _successfulMinStart;
       }
 
       /**
        * Returns the minimum time an unsuccessful call took.
        *
        * @return
        *    the minimum time an unsuccessful call took.
        */
       public long getUnsuccessfulMin() {
          return _unsuccessfulMin;
       }
 
       /**
        * Returns the start time of the unsuccessful call that took the shortest.
        *
        * @return
        *    the start time of the unsuccessful call that took the shortest,
        *    always &gt;= 0.
        */
       public long getUnsuccessfulMinStart() {
         return _unsuccessfulMinStart;
       }
 
       // TODO: Have a similar description for all these getters
 
       /**
        * Returns the duration of the successful call that took the longest.
        *
        * @return
        *    the duration of the successful call that took the longest, always
        *    &gt;= 0.
        */
       public long getSuccessfulMax() {
          return _successfulMax;
       }
 
       /**
        * Returns the start time of the most recent successful call that took
        * the longest.
        *
        * @return
        *    the start time of the most recent successful call that took the
        *    longest, always &gt;= 0.
        */
       public long getSuccessfulMaxStart() {
          return _successfulMaxStart;
       }
 
       /**
        * Returns the duration of the unsuccessful call that took the longest.
        *
        * @return
        *    the duration of the unsuccessful call that took the longest,
        *    always &gt;= 0.
        */
       public long getUnsuccessfulMax() {
          return _unsuccessfulMax;
       }
 
       /**
        * Returns the start time of the most recent unsuccessful call that took
        * the longest.
        *
        * @return
        *    the start time of the most recent unsuccessful call that took the
        *    longest, always &gt;= 0.
        */
       public long getUnsuccessfulMaxStart() {
          return _unsuccessfulMaxStart;
       }
    }
 }
