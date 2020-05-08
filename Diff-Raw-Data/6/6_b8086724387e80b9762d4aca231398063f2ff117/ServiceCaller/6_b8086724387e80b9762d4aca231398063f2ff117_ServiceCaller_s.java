 /*
  * $Id$
  */
 package org.xins.common.service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.util.TimeoutController;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Service caller. This abstract class must be subclasses by specific kinds
  * of service callers, for example for HTTP, FTP, JDBC, etc.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.115
  */
 public abstract class ServiceCaller extends Object {
 
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
     * Constructs a new <code>ServiceCaller</code> object.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     */
    protected ServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       // Set fields
       _descriptor = descriptor;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The descriptor for this service. Cannot be <code>null</code>.
     */
    private final Descriptor _descriptor;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the descriptor.
     *
     * @return
     *    the descriptor for this service, never <code>null</code>.
     */
    public final Descriptor getDescriptor() {
       return _descriptor;
    }
 
    /**
     * Performs a call using the specified subject. Target
     * {@link TargetDescriptor descriptors} will be picked and passed
     * to {@link #doCallImpl(TargetDescriptor,Object)} until there is one that
     * succeeds. If one of the calls succeeds, then the result is returned. If
     * none succeeds, then a {@link CallFailedException} is thrown.
     *
     * <p>Each attempt consists of a call to
     * {@link #doCallImpl(TargetDescriptor,Object)}.
     *
     * @param subject
     *    the subject passed, could possibly be <code>null</code>.
     *
     * @return
     *    a combination of the call result and a link to the
     *    {@link TargetDescriptor target} that returned this result, if and
     *    only if one of the calls succeeded, could be <code>null</code>.
     *
     * @throws CallFailedException
     *    if all calls failed.
     */
    protected final CallResult doCall(Object subject)
    throws CallFailedException {
 
       ArrayList failedTargets = null;
       ArrayList exceptions    = null;
 
       // Iterate over all targets
       Iterator iterator = _descriptor.iterateTargets();
       while (iterator.hasNext()) {
 
          // Determine the service descriptor target
          TargetDescriptor target = (TargetDescriptor) iterator.next();
 
          // Call using this target
          try {
 
             // Attempt the call
             Object result = doCallImpl(target, subject);
             Log.log_3312(target.toString());
 
             // Trim the collections to save on memory
             // XXX: Should we really trim the collections?
             if (failedTargets != null) {
                failedTargets.trimToSize();
                exceptions.trimToSize();
             }
 
             return new CallResult(failedTargets, exceptions, target, result);
 
          // If it fails, store the exception and try the next
          } catch (Throwable exception) {
             if (failedTargets == null) {
                failedTargets = new ArrayList();
                exceptions    = new ArrayList();
             }
             failedTargets.add(target);
             exceptions.add(exception);
 
             Log.log_3313(target.toString(), reasonFor(exception));
          }
       }
 
       // Loop ended, all calls failed
       Log.log_3314();
       throw new CallFailedException(subject, failedTargets, exceptions);
    }
 
    /**
     * Calls the specified target using the specified subject. This method must
     * be implemented by subclasses. It is called as soon as a target is
     * selected to be called. If the call fails, then an exception should be
     * thrown. If the call succeeds, then the call result should be returned
     * from this method.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @param subject
     *    the subject passed, could possibly be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, could be
     *    <code>null</code>.
     *
     * @throws Throwable
     *    if the call to the specified target failed.
     */
    protected abstract Object doCallImpl(TargetDescriptor target,
                                         Object           subject)
    throws Throwable;
 
    /**
     * Determines the reason for a specific exception. The reason should not
     * end with a punctuation mark like a period (<code>'.'</code>).
     *
     * @param exception
     *    the exception to convert to a reason, cannot be <code>null</code>.
     *
     * @return
     *    a description of the reason for the exception, never
     *    <code>null</code>, and never an empty character string.
     *
     * @throws IllegalArgumentException
     *    if <code>exception == null</code>.
     *
     * @since XINS 0.116
     */
    protected final String reasonFor(Throwable exception)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("exception", exception);
 
       // Allow subclass to determine reason
       String reason = reasonForImpl(exception);
       if (reason != null && reason.length() > 0) {
          return reason;
       }
 
       String clazz   = exception.getClass().getName();
       String message = exception.getMessage();
       if (message != null && message.length() > 0) {
          return clazz + ": " + message;
       } else {
          return clazz;
       }
    }
 
    /**
     * Determines the reason for a specific exception (implementation method).
     *
     * <p>Note that the reason should not end with a punctuation mark like a
     * period (<code>'.'</code>).
     *
     * @param exception
     *    the exception to convert to a reason, guaranteed not to be
     *    <code>null</code>.
     *
     * @return
     *    a description of the reason for the exception, or <code>null</code>
     *    if the calling method ({@link #reasonFor(Throwable)}) should
     *    determine the reason, never an empty character string.
     *
     * @since XINS 0.116
     */
    protected String reasonForImpl(Throwable exception) {
       return null;
    }
 
    /**
     * Runs the specified thread. If the thread does not finish within the
     * total time-out period, then the thread is interrupted using the
     * {@link Thread#interrupt()} method and a {@link TimeOutException} is
     * thrown.
     *
     * @param thread
     *    the thread to run, cannot be <code>null</code>.
     *
     * @param descriptor
     *    the descriptor for the target on which the thread is executed, cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
    *    if <code>thread == null</code>.
     *
     * @throws IllegalThreadStateException
     *    if the thread was already started.
     *
     * @throws SecurityException
     *    if the thread did not finish within the total time-out period, but
     *    the interruption of the thread was disallowed (see
     *    {@link Thread#interrupt()}).
     *
     * @throws TimeOutException
     *    if the thread did not finish within the total time-out period and was
     *    interrupted.
     *
     * @since XINS 0.195
     */
    protected final void controlTimeOut(Thread           thread,
                                        TargetDescriptor descriptor)
    throws IllegalArgumentException,
           IllegalThreadStateException,
           SecurityException,
           TimeOutException {
 
       // Check preconditions
      MandatoryArgumentChecker.check("thread", thread);
 
       // Start the thread. This may throw an IllegalThreadStateException.
       thread.start();
 
       // Wait for the thread to finish, within limits
       try {
          thread.join(descriptor.getTimeOut());
       } catch (InterruptedException exception) {
          // ignore
          // XXX: Log?
       }
 
       // If the thread is still running at this point, it should stop
       if (thread.isAlive()) {
 
          // Interrupt the thread. This may throw a SecurityException
          thread.interrupt();
 
          throw new TimeOutException();
       }
    }
 }
