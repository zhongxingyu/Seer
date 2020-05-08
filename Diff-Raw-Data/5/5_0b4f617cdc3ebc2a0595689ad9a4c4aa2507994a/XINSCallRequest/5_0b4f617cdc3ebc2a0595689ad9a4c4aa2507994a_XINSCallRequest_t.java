 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import java.util.Iterator;
 
 import org.apache.log4j.NDC;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.collections.ProtectedPropertyReader;
 import org.xins.common.service.CallRequest;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Abstraction of a XINS request.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class XINSCallRequest extends CallRequest {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * HTTP status code verifier that will only approve 2xx codes.
     */
    private static final org.xins.common.http.HTTPStatusCodeVerifier HTTP_STATUS_CODE_VERIFIER = new HTTPStatusCodeVerifier();
 
    /**
     * Perl 5 pattern compiler.
     */
    private static final Perl5Compiler PATTERN_COMPILER = new Perl5Compiler();
 
    /**
     * Pattern matcher.
     */
    private static final Perl5Matcher PATTERN_MATCHER = new Perl5Matcher();
 
    /**
     * The pattern for a parameter name, as a character string.
     */
    public static final String PARAMETER_NAME_PATTERN_STRING = "[a-zA-Z][a-zA-Z0-9_]*";
 
    /**
     * The pattern for a parameter name.
     */
    private static final Pattern PARAMETER_NAME_PATTERN;
 
    /**
     * The name of the HTTP parameter that specifies the diagnostic context
     * identifier.
     */
    private static final String CONTEXT_ID_HTTP_PARAMETER_NAME = "_context";
 
    /**
     * The number of instances of this class. Initially zero.
     */
    private static int INSTANCE_COUNT;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class. This function compiles
     * {@link #PARAMETER_NAME_PATTERN_STRING} to a {@link Pattern} and then
     * stores that in {@link #PARAMETER_NAME_PATTERN}.
     */
    static {
       try {
          PARAMETER_NAME_PATTERN = PATTERN_COMPILER.compile(PARAMETER_NAME_PATTERN_STRING, Perl5Compiler.READ_ONLY_MASK);
       } catch (MalformedPatternException mpe) {
          throw new Error("The pattern \"" + PARAMETER_NAME_PATTERN_STRING + "\" is malformed.");
       }
    }
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSCallRequest</code> for the specified function
     * and parameters, disallowing fail-over unless the request was definitely
     * not (yet) accepted by the service.
     *
     * @param functionName
     *    the name of the function to call, cannot be <code>null</code>.
     *
     * @param parameters
     *    the input parameters, if any, can be <code>null</code> and should not
     *    be modifiable.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     */
    public XINSCallRequest(String functionName, PropertyReader parameters)
    throws IllegalArgumentException {
       this(functionName, parameters, false, null);
    }
 
    /**
     * Constructs a new <code>XINSCallRequest</code> for the specified function
     * and parameters, possibly allowing fail-over even if the request was
     * possibly already received by a target service.
     *
     * @param functionName
     *    the name of the function to call, cannot be <code>null</code>.
     *
     * @param parameters
     *    the input parameters, if any, can be <code>null</code> and should not
     *    be modifiable.
     *
     * @param failOverAllowed
     *    flag that indicates whether fail-over is in principle allowed, even
     *    if the request was already sent to the other end.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code>.
     */
    public XINSCallRequest(String         functionName,
                           PropertyReader parameters,
                           boolean        failOverAllowed)
    throws IllegalArgumentException {
       this(functionName, parameters, failOverAllowed, null);
    }
 
    /**
     * Constructs a new <code>XINSCallRequest</code> for the specified function
     * and parameters, possibly allowing fail-over, optionally specifying the
     * HTTP method to use.
     *
     * @param functionName
     *    the name of the function to call, cannot be <code>null</code>.
     *
     * @param parameters
     *    the input parameters, if any, can be <code>null</code> and should not
     *    be modifiable.
     *
     * @param failOverAllowed
     *    flag that indicates whether fail-over is in principle allowed, even
     *    if the request was already sent to the other end.
     *
     * @param method
     *    the HTTP method to use, or <code>null</code> if the default HTTP
     *    method (POST) should be used.
     *
     * @throws IllegalArgumentException
     *    if <code>functionName == null</code> or if <code>parameters</code>
     *    contains a name that does not match the constraints for a parameter
     *    name, see {@link #PARAMETER_NAME_PATTERN_STRING} or if it equals
     *    <code>"function"</code>, which is currently still reserved.
     */
    public XINSCallRequest(String         functionName,
                           PropertyReader parameters,
                           boolean        failOverAllowed,
                           HTTPMethod     method)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
 
       // HTTP method defaults to POST
       if (method == null) {
          method = HTTPMethod.POST;
       }
 
       // Create PropertyReader for the HTTP parameters
       final Object SECRET_KEY = new Object();
       ProtectedPropertyReader httpParams = new ProtectedPropertyReader(SECRET_KEY);
 
       // Check and copy all XINS parameters to HTTP parameters
       if (parameters != null) {
          Iterator names = parameters.getNames();
          while (names.hasNext()) {
 
             // Get the name and value
             String name  = (String) names.next();
             String value = parameters.get(name);
 
             // Name cannot violate the pattern
             if (! PATTERN_MATCHER.matches(name, PARAMETER_NAME_PATTERN)) {
                // XXX: Consider using a different kind of exception for this
                //      specific case. For backwards compatibility, this
                //      exception class should derive from
                //      IllegalArgumentException.
 
                FastStringBuffer buffer = new FastStringBuffer(121, "The parameter name \"");
                buffer.append(name);
                buffer.append("\" does not match the pattern \"");
                buffer.append(PARAMETER_NAME_PATTERN_STRING);
                buffer.append("\".");
                throw new IllegalArgumentException(buffer.toString());
 
             // Name cannot be "function"
             } else if ("function".equals(name)) {
                throw new IllegalArgumentException("Parameter name \"function\" is reserved.");
 
             // Name is considered valid, store it
             } else {
                httpParams.set(SECRET_KEY, name, value);
             }
          }
       }
 
       // Add the function to the parameter list
       httpParams.set(SECRET_KEY, "_function", functionName);
 
       // XXX: For backwards compatibility, also add the parameter "function"
       //      to the list of HTTP parameters. This is, however, very likely to
       //      change in the future.
       httpParams.set(SECRET_KEY, "function", functionName);
 
       // Add the diagnostic context ID to the parameter list, if there is one
       String contextID = NDC.peek();
       if (contextID != null) {
          httpParams.set(SECRET_KEY, CONTEXT_ID_HTTP_PARAMETER_NAME, contextID);
       }
 
       // Initialize fields
       _instanceNumber  = ++INSTANCE_COUNT;
       _functionName    = functionName;
       _parameters      = parameters; // XXX: Make unmodifiable and change @param?
       _httpRequest     = new HTTPCallRequest(method,
                                              httpParams,
                                              failOverAllowed,
                                              HTTP_STATUS_CODE_VERIFIER);
 
       // XXX: Note that _asString is lazily initialized.
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The 1-based sequence number of this instance. Since this number is
     * 1-based, the first instance of this class will have instance number 1
     * assigned to it.
     */
    private final int _instanceNumber;
 
    /**
     * Description of this XINS call request. This field cannot be
     * <code>null</code>, it is initialized during construction.
     */
    private String _asString;
 
    /**
     * The name of the function to call. This field cannot be
     * <code>null</code>.
     */
    private final String _functionName;
 
    /**
     * The parameters to pass in the request, and their respective values. This
     * field can be <code>null</code>.
     */
    private final PropertyReader _parameters;
 
    /**
     * The HTTP service caller used to execute the request to a XINS service
     * over HTTP. This field is never <code>null</code>.
     */
    private final HTTPCallRequest _httpRequest;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Describes this request.
     *
     * @return
     *    the description of this request, never <code>null</code>.
     */
    public String describe() {
 
       // Lazily initialize the description of this call request object
       if (_asString == null) {
          FastStringBuffer buffer = new FastStringBuffer(208, "XINS HTTP ");
          buffer.append(_httpRequest.getMethod().toString());
          buffer.append(" request #");
          buffer.append(_instanceNumber);
          buffer.append(", parameters: ");
          PropertyReaderUtils.serialize(_parameters, buffer, "-");
          if (isFailOverAllowed()) {
             buffer.append(", fail-over allowed, ");
          } else {
             buffer.append(", fail-over disallowed, ");
          }
 
          String contextID = _httpRequest.getParameters().get(CONTEXT_ID_HTTP_PARAMETER_NAME);
 
          if (contextID == null) {
            buffer.append("no diagnostic context ID");
          } else {
            buffer.append("diagnostic context ID: \"");
             buffer.append(contextID);
             buffer.append('"');
          }
 
          _asString = buffer.toString();
       }
 
       return _asString;
    }
 
    /**
     * Returns the name of the function to call.
     *
     * @return
     *    the name of the function to call, never <code>null</code>.
     */
    public String getFunctionName() {
       return _functionName;
    }
 
    /**
     * Gets all parameters to pass with the call, with their respective values.
     *
     * @return
     *    the parameters, or <code>null</code> if there are none.
     */
    public PropertyReader getParameters() {
       return _parameters;
    }
 
    /**
     * Gets the value of the specified parameter.
     *
     * @param name
     *    the parameter name, not <code>null</code>.
     *
     * @return
     *    string containing the value of the parameter, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
 
       return (_parameters == null) ? null : _parameters.get(name);
    }
 
    /**
     * Determines whether fail-over is in principle allowed, even if the
     * request was already sent to the other end.
     *
     * @return
     *    <code>true</code> if fail-over is in principle allowed, even if the
     *    request was already sent to the other end, <code>false</code>
     *    otherwise.
     */
    public boolean isFailOverAllowed() {
       return _httpRequest.isFailOverAllowed();
    }
 
    /**
     * Returns the underlying <code>HTTPCallRequest</code>.
     *
     * @return
     *    the underlying {@link HTTPCallRequest}, never <code>null</code>.
     */
    HTTPCallRequest getHTTPCallRequest() {
       return _httpRequest;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * HTTP status code verifier that will only approve 2xx codes.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
     */
    private static final class HTTPStatusCodeVerifier
    extends Object
    implements org.xins.common.http.HTTPStatusCodeVerifier {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>HTTPStatusCodeVerifier</code>.
        */
       private HTTPStatusCodeVerifier() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Checks if the specified HTTP status code is considered acceptable or
        * unacceptable.
        *
        * <p>The implementation of this method in class
        * {@link XINSCallRequest.HTTPStatusCodeVerifier} returns
        * <code>true</code> only for 2xx status codes.
        *
        * @param code
        *    the HTTP status code to check.
        *
        * @return
        *    <code>true</code> if <code>code &gt;= 200 &amp;&amp; code &lt;=
        *    299</code>.
        */
       public boolean isAcceptable(int code) {
          return (code >= 200) && (code <= 299);
       }
    }
 }
