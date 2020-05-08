 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.util.Map;
 import org.jdom.Element;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Exception that indicates that an API call result was unsuccessful.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.36
  */
 public final class UnsuccessfulCallException
 extends CallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a message for the constructor.
     *
     * @param result
     *    the call result that is unsuccessful, cannot be <code>null</code>,
     *    and <code>result.</code>{@link XINSServiceCaller.Result#isSuccess() isSuccess()}
     *    should be <code>false</code>.
     *
     * @return
    *    the constructed message for the constructor to pass up to the
     *    superconstructor, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.{@link XINSServiceCaller.Result#getErrorCode() getErrorCode()}
     *          == null</code>.
     *
     * @since XINS 0.124
     */
    private static final String createMessage(XINSServiceCaller.Result result)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("result", result);
       String code = result.getErrorCode();
       if (code == null) {
          throw new IllegalArgumentException("result.getErrorCode() == null");
       }
 
       // Create message in buffer
       FastStringBuffer buffer = new FastStringBuffer(80);
       buffer.append("Call was unsuccessful");
       if (code != null && code.length() > 0) {
          buffer.append(", result code was \"");
          buffer.append(code);
          buffer.append('"');
       }
       buffer.append('.');
 
       // Return the message string
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnsuccessfulCallException</code> with the
     * specified call result.
     *
     * @param result
     *    the call result that is unsuccessful, cannot be <code>null</code>,
     *    and <code>result.</code>{@link XINSServiceCaller.Result#isSuccess() isSuccess()}
     *    should be <code>false</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.</code>{@link XINSServiceCaller.Result#isSuccess() isSuccess()}.
     */
    public UnsuccessfulCallException(XINSServiceCaller.Result result)
    throws IllegalArgumentException {
 
       super(createMessage(result), null);
 
       _result = result;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The call result. The value of this field cannot be <code>null</code>.
     */
    private final XINSServiceCaller.Result _result;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the result code.
     *
     * @return
     *    the result code or <code>null</code> if no code was returned.
     *
     * @since XINS 0.136
     * @deprecated
     *    Deprecated since XINS 0.181. Use {@link #getErrorCode()} instead,
     *    which is in fact a renamed version of this function.
     */
    public String getCode() {
       return getErrorCode();
    }
 
    /**
     * Returns the result code.
     *
     * @return
     *    the result code or <code>null</code> if no code was returned.
     *
     * @since XINS 0.181
     */
    public String getErrorCode() {
       return _result.getErrorCode();
    }
 
    /**
     * Gets all returned parameters.
     *
     * @return
     *    a <code>Map</code> containing all parameters, never
     *    <code>null</code>; the keys will be the names of the parameters
     *    ({@link String} objects, cannot be <code>null</code>), the values will be the parameter values
     *    ({@link String} objects as well, cannot be <code>null</code>).
     *
     * @since XINS 0.136
     */
    public Map getParameters() {
       return _result.getParameters();
    }
 
    /**
     * Gets the value of the specified returned parameter.
     *
     * @param name
     *    the parameter element name, not <code>null</code>.
     *
     * @return
     *    string containing the value of the parameter, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     *
     * @since XINS 0.136
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
       return _result.getParameter(name);
    }
 
    /**
     * Returns the optional extra data. The data is an XML {@link Element}, or
     * <code>null</code>.
     *
     * @return
     *    the extra data as an XML {@link Element}, can be <code>null</code>;
     *    if it is not <code>null</code>, then
     *    <code><em>return</em>.{@link Element#getName() getName()}.equals("data") &amp;&amp; <em>return</em>.{@link Element#getNamespace() getNamespace()}.equals({@link org.jdom.Namespace#NO_NAMESPACE NO_NAMESPACE})</code>.
     *
     * @since XINS 0.136
     */
    public Element getDataElement() {
       return _result.getDataElement();
    }
 }
