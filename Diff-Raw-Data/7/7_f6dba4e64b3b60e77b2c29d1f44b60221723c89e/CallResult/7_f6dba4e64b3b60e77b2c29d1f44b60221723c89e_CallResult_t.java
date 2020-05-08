 /*
  * $Id$
  */
 package org.xins.util.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Result of a call to a service. The actual result is returned, combined with
  * links to the services that failed and a link to the service to which the
  * call succeeded.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.115
  */
 public final class CallResult extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Checks that the a list of failed targets and a list of exceptions are
     * valid individually and together.
     *
     * @param failedTargets
     *    the list of targets for which the call failed, can be
     *    <code>null</code>; all elements in this {@link List} must be
     *    {@link ServiceDescriptor} objects, no <code>null</code> elements are
     *    allowed, but duplicates are.
     *
     * @param exceptions
     *    the list of caught exceptions, matching the list of failed targets,
     *    can be <code>null</code>; all elements in this {@link List} must be
     *    {@link Throwable} objects, no <code>null</code> elements are allowed,
     *    but duplicates are.
     *
     * @throws IllegalArgumentException
     *    if <code>(failedTargets == null &amp;&amp; exceptions != null)
     *         || (failedTargets != null &amp;&amp; (
     *               exceptions == null
     *            || failedTargets.size() != exceptions.size()
     *            || !(exceptions.get(<em>i</em>) instanceof Throwable)
     *            || failedTargets.get(<em>i</em>) == null
     *            || !(failedTargets.get(<em>i</em>) instanceof ServiceDescriptor)
     *            || failedTargets.get(<em>x</em>).equals(failedTargets.get(<em>y</em>))))</code>,
     *    where <code>0 &lt;= <em>i</em> &lt; failedTargets.size()</code>
     *    and   <code>0 &lt;= <em>x</em> &lt; <em>y</em> &lt; failedTargets.size()</code>.
     */
    static void checkFailureLists(List failedTargets, List exceptions)
    throws IllegalArgumentException {
 
       // Either both are null, or none is null
       if (failedTargets == null && exceptions == null) {
          return;
       } else if (failedTargets == null) {
          throw new IllegalArgumentException("failedTargets == null && exceptions != null");
       } else if (exceptions == null) {
          throw new IllegalArgumentException("failedTargets != null && exceptions == null");
       }
 
       // Check size of both lists is equal
       int failureCount   = failedTargets == null ? 0 : failedTargets.size();
       int exceptionCount = exceptions    == null ? 0 : exceptions.size();
       if (failureCount != exceptionCount) {
          throw new IllegalArgumentException("failedTargets.size() (" + failureCount + ") != exceptions.size() (" + exceptionCount + ')');
       }
 
       // Check all elements
       for (int i = 0; i < failureCount; i++) {
          Object ftElem = failedTargets.get(i);
          Object exElem = exceptions.get(i);
 
          // Elements cannot be null
          if (ftElem == null) {
             throw new IllegalArgumentException("failedTargets.get(" + i + ") == null");
          } else if (exElem == null) {
             throw new IllegalArgumentException("exceptions.get(" + i + ") == null");
 
          // Elements must be instance of correct class
          } else if (!(ftElem instanceof ServiceDescriptor)) {
             throw new IllegalArgumentException("(failedTargets.get(" + i + ") instanceof ServiceDescriptor) == false");
          } else if (!(exElem instanceof Throwable)) {
             throw new IllegalArgumentException("(exceptions.get(" + i + ") instanceof Throwable) == false");
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallResult</code> object.
     *
     * @param failedTargets
     *    the list of targets for which the call failed, can be
     *    <code>null</code>; all elements in this {@link List} must be
     *    {@link ServiceDescriptor} objects, no <code>null</code> elements are
     *    allowed, but duplicates are.
     *
     * @param exceptions
     *    the list of caught exceptions, matching the list of failed targets,
     *    can be <code>null</code>; all elements in this {@link List} must be
     *    {@link Throwable} objects, no <code>null</code> elements are allowed,
     *    but duplicates are.
     *
     * @param succeededTarget
     *    the target for which the call succeeded, cannot be <code>null</code>.
     *
     * @param result
     *    the actual result object, can possibly be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>succeededTarget == null
     *         || (failedTargets == null &amp;&amp; exceptions != null)
     *         || (failedTargets != null &amp;&amp; (
     *               exceptions == null
     *            || failedTargets.size() != exceptions.size()
     *            || failedTargets.get(<em>i</em>) == null
     *            || !(failedTargets.get(<em>i</em>) instanceof ServiceDescriptor)
     *            || !(exceptions.get(<em>i</em>) instanceof Throwable)</code>
     *    where <code>0 &lt;= <em>i</em> &lt; failedTargets.size()</code>.
     */
    public CallResult(List              failedTargets,
                      List              exceptions,
                      ServiceDescriptor succeededTarget,
                      Object            result)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("succeededTarget", succeededTarget);
       checkFailureLists(failedTargets, exceptions);
 
       // Set fields
       _failedTargets   = failedTargets == null ? null : Collections.unmodifiableList(new ArrayList(failedTargets));
       _exceptions      = exceptions    == null ? null : Collections.unmodifiableList(new ArrayList(exceptions));
       _succeededTarget = succeededTarget;
       _result          = result;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The list of targets for which the call failed. Can be <code>null</code>.
     * All elements in this {@link List} are {@link ServiceDescriptor} objects.
     * The {@link List} contains no <code>null</code> elements, but it may
     * contain duplicates.
     *
     * <p>This is an unmodifiable {@link List}.
     */
    private final List _failedTargets;
 
    /**
     * The list of caught exceptions, one per failed target. Can be
     * <code>null</code> if and only if <code>_failedTargets == null</code>.
     * All elements in this {@link List} are {@link Throwable} objects.
     * The {@link List} contains no <code>null</code> elements, but it may
     * contain duplicates.
     *
     * <p>This is an unmodifiable {@link List}.
     */
    private final List _exceptions;
 
    /**
     * The target for which the call succeeded. This field cannot be
     * <code>null</code>.
     */
    private final ServiceDescriptor _succeededTarget;
 
    /**
     * The actual result object. Can possibly be <code>null</code>.
     */
    private final Object _result;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the list of targets for which the call failed. The returned
     * {@link List} can be <code>null</code>, but if it is not then all
     * elements in the {@link List} are {@link ServiceDescriptor} objects, and
     * it contains no <code>null</code> elements. It may contain duplicates,
     * though.
     *
     * <p>The returned {@link List} is unmodifiable.
     *
     * <p>If this method returns <code>null</code>, then so will
     * {@link #getExceptions()} and vice versa.
     *
     * @return
     *    the {@link List} of failed targets, or <code>null</code> if the first
     *    attempt succeeded.
     */
    public List getFailedTargets() {
       return _failedTargets;
    }
 
    /**
     * The list of caught exceptions, one per failed target. The returned
    * {@link List} can be <code>null</code>, but if it is not then all
    * elements in the {@link List} are {@link Throwable} objects, and it
    * contains no <code>null</code> elements. It may contain duplicates,
    * though.
     *
     * <p>The returned {@link List} is unmodifiable.
     *
     * <p>If this method returns <code>null</code>, then so will
     * {@link #getFailedTargets()} and vice versa.
     *
     * @return
     *    the {@link List} of exceptions, or <code>null</code> if the first
     *    attempt succeeded.
     */
    public List getExceptions() {
       return _exceptions;
    }
 
    /**
     * Returns the target for which the call succeeded.
     *
     * @return
     *    the {@link ServiceDescriptor target} for which the call succeeded,
     *    not <code>null</code>.
     */
    public ServiceDescriptor getSucceededTarget() {
       return _succeededTarget;
    }
 
    /**
     * Returns the actual result object.
     *
     * @return
     *    the result object, or <code>null</code>.
     */
    public Object getResult() {
       return _result;
    }
 }
