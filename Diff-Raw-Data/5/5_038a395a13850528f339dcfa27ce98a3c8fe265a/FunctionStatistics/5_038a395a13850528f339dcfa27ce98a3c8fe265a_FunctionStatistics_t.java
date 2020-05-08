 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 /**
  * Statistics of a function.
  *
 * @version $Revision$ $Date$
 * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
 * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 class FunctionStatistics {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Lock object for a successful call.
     */
    private final Object _successfulCallLock = new Object();
 
    /**
     * Lock object for an unsuccessful call.
     */
    private final Object _unsuccessfulCallLock = new Object();
 
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
     * Callback method that may be called after a call to this function. This
     * method will store statistics-related information.
     *
     * <p />This method does not <em>have</em> to be called. If statistics
     * gathering is disabled, then this method should not be called.
     *
     * @param start
     *    the start time, in milliseconds since January 1, 1970, not
     *    <code>null</code>.
     *
     * @param success
     *    indication if the call was successful.
     *
     * @return
     *    returns the duration in milliseconds of the call of the function.
     *    The duration is computed as the difference in between
     *    the start time and the time that this method has been invoked.
     */
    final long recordCall(long start, boolean success) {
 
       long duration = System.currentTimeMillis() - start;
 
       // Call succeeded
       if (success) {
 
          synchronized (_successfulCallLock) {
             _lastSuccessfulStart    = start;
             _lastSuccessfulDuration = duration;
             _successfulCalls++;
             _successfulDuration += duration;
             _successfulMin      = _successfulMin > duration ? duration : _successfulMin;
             _successfulMax      = _successfulMax < duration ? duration : _successfulMax;
             _successfulMinStart = (_successfulMin == duration) ? start : _successfulMinStart;
             _successfulMaxStart = (_successfulMax == duration) ? start : _successfulMaxStart;
          }
 
       // Call failed
       } else {
 
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
       return duration;
    }
 
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
       return _lastUnsuccessfulDuration;
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
 
    /**
     * Resets the statistics for this function.
     */
    final void resetStatistics() {
       synchronized (_successfulCallLock) {
          _successfulCalls = 0;
          _lastSuccessfulStart = 0L;
          _lastSuccessfulDuration = 0L;
          _successfulDuration = 0L;
          _successfulMin = Long.MAX_VALUE;
          _successfulMinStart = 0L;
          _successfulMax = 0L;
          _successfulMaxStart = 0L;
       }
       synchronized (_unsuccessfulCallLock) {
          _unsuccessfulCalls = 0;
          _lastUnsuccessfulStart = 0L;
          _lastUnsuccessfulDuration = 0L;
          _unsuccessfulDuration = 0L;
          _unsuccessfulMin = Long.MAX_VALUE;
          _unsuccessfulMinStart = 0L;
          _unsuccessfulMax = 0L;
          _unsuccessfulMaxStart = 0L;
       }
    }
 }
