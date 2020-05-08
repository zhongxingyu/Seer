 /*
  * $Id$
  */
 package org.xins.common.service;
 
 /**
 * Exception that indicates the total time-out for a request was reached, so
 * the request was aborted.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.207
  */
public final class UnexpectedExceptionCallException extends GenericCallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnexpectedExceptionCallException</code>.
     *
     * @param request
     *    the original request, cannot be <code>null</code>.
     *
     * @param target
     *    descriptor for the target that was attempted to be called, cannot be
     *    <code>null</code>.
     *
     * @param duration
     *    the call duration in milliseconds, must be &gt;= 0.
     *
     * @param detail
     *    a detailed description of the problem, can be <code>null</code> if
     *    there is no more detail.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request     == null
     *          || target      == null
     *          || duration  &lt; 0</code>.
     */
    public UnexpectedExceptionCallException(CallRequest      request,
                                            TargetDescriptor target,
                                            long             duration,
                                            String           detail,
                                            Throwable        cause)
    throws IllegalArgumentException {
       super("Unexpected exception caught", request, target, duration, detail, cause);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
