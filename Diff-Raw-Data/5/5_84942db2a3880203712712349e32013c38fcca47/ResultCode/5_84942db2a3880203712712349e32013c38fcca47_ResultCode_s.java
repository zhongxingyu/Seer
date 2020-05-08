 /*
  * $Id$
  */
 package org.xins.server;
 
 import org.apache.log4j.Logger;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * A result code. Result codes are either generic or API-specific. Result
  * codes do not automatically apply to all functions of an API if they have
  * been defined for that API.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class ResultCode
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private static final Logger LOG = Logger.getLogger(ResultCode.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new generic <code>ResultCode</code>. This constructor can
     * only be called by classes in the same package.
     *
     * @param name
     *    the symbolic name, can be <code>null</code>.
     *
     * @param value
     *    the actual value of this code, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>value == null</code>.
     *
     * @since XINS 0.117.
     */
    ResultCode(String name, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("value", value);
 
       _api     = null;
       _success = false;
       _name    = name;
       _value   = value;
    }
 
    /**
     * Constructs a new generic <code>ResultCode</code>. This constructor can
     * only be called by classes in the same package.
     *
     * @param success
     *    the success indication.
     *
     * @param name
     *    the symbolic name, can be <code>null</code>.
     *
     * @param value
     *    the actual value of this code, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>value == null</code>.
     *
     * @deprecated
    *    Deprecated since XINS 0.117. Use {@link ResultCode(String,String)}
     *    instead.
     */
    ResultCode(boolean success, String name, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("value", value);
 
       if (success) {
          LOG.warn("Result code \"" + name + "\" is marked as successful. Result codes should always indicate an error condition.");
       }
 
       _api     = null;
       _success = success;
       _name    = name;
       _value   = value;
    }
 
    /**
     * Constructs a new <code>ResultCode</code> for the specified API.
     *
     * @param api
     *    the API to which this result code belongs, not <code>null</code>.
     *
     * @param name
     *    the symbolic name, can be <code>null</code>.
     *
     * @param value
     *    the actual value of this code, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || value == null</code>.
     *
     * @since XINS 0.117
     */
    public ResultCode(API api, String name, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("api", api, "value", value);
 
       _api     = api;
       _success = false;
       _name    = name;
       _value   = value;
 
       _api.resultCodeAdded(this);
    }
 
    /**
     * Constructs a new <code>ResultCode</code> for the specified API.
     *
     * @param api
     *    the API to which this result code belongs, not <code>null</code>.
     *
     * @param success
     *    the success indication.
     *
     * @param name
     *    the symbolic name, can be <code>null</code>.
     *
     * @param value
     *    the actual value of this code, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null || value == null</code>.
     *
     * @deprecated
    *    Deprecated since XINS 0.117. Use {@link ResultCode(API,String,String)}
     *    instead.
     */
    public ResultCode(API api, boolean success, String name, String value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("api", api, "value", value);
 
       if (success) {
          LOG.warn("Result code \"" + name + "\" in the " + api.getName() + " API is marked as successful. Result codes should always indicate an error condition.");
       }
 
       _api     = api;
       _success = success;
       _name    = name;
       _value   = value;
 
       _api.resultCodeAdded(this);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API implementation this result code is defined within. Cannot be
     * <code>null</code>.
     */
    private final API _api;
 
    /**
     * The success indication.
     */
    private final boolean _success;
 
    /**
     * The symbolic name of this result code. Can be <code>null</code>.
     */
    private final String _name;
 
    /**
     * The value of this result code. This field cannot be <code>null</code>.
     */
    private final String _value;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the success indication.
     *
     * @return
     *    <code>true</code> if this result code indicates a successful call,
     *    <code>false</code> otherwise.
     *
     * @deprecated
     *    Deprecated since XINS 0.117. All result codes should be unsuccessful
     *    and indicate failure.
     */
    public final boolean getSuccess() {
       return _success;
    }
 
    /**
     * Returns the symbolic name of this result code.
     *
     * @return
     *    the symbolic name, can be <code>null</code>.
     */
    public final String getName() {
       return _name;
    }
 
    /**
     * Returns the value of this result code.
     *
     * @return
     *    the value, not <code>null</code>.
     */
    public final String getValue() {
       return _value;
    }
 }
