 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Call target group that tries to call the underlying function callers in
  * order.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.41
  */
 final class OrderedCallTargetGroup extends CallTargetGroup {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(RandomCallTargetGroup.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>OrderedCallTargetGroup</code>.
     *
     * @param members
     *    the members of this group, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>members == null</code>.
     */
    OrderedCallTargetGroup(List members) throws IllegalArgumentException {
       super(ORDERED_TYPE, members);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    CallResult callImpl(String sessionID, String functionName, Map parameters)
    throws IllegalArgumentException,
           CallIOException,
           InvalidCallResultException {
 
       List members = getMembers();
       int count = (members == null) ? 0 : members.size();
       Object result;
       int i = 0;
       boolean divert;
       do {
          FunctionCaller caller = (FunctionCaller) members.get(i);
 
          // Increase the counter, both since the log message is 1-based and
          // for the loop condition
          i++;
 
          // Log this attempt
          if (LOG.isDebugEnabled()) {
             LOG.debug("Trying to call " + caller + " (attempt " + i + '/' + count + ')');
          }
 
          // Attempt the call
          result = tryCall(caller, sessionID, functionName, parameters);
 
          // Determine if the call failed
          if (i == count) {
             divert = false;
          } else {
             divert = result instanceof Throwable;
             if (!divert) {
                CallResult callResult = (CallResult) result;
               String code = callResult.getCode();
                if (code != null) {
                   divert = divertOnCode(code);
                }
             }
          }
 
          // Log wether the call was successful
          if (LOG.isDebugEnabled()) {
             if (divert) {
                if (result instanceof Throwable) {
                   LOG.debug("Call attempt " + i + '/' + count + " failed due to " + result.getClass().getName() + '.');
                } else {
                   LOG.debug("Call attempt " + i + '/' + count + " failed due to result code \"" + code + "\".");
                }
             } else {
                LOG.debug("Call attempt " + i + '/' + count + " succeeded.");
             }
       } while (divert);
 
       return callImplResult(result);
    }
 }
