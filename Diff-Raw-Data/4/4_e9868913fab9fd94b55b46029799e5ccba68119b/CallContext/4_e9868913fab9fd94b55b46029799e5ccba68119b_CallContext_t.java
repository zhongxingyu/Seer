 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import javax.servlet.ServletRequest;
 import org.xins.types.TypeValueException;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.io.FastStringWriter;
 import org.znerd.xmlenc.XMLOutputter;
 import org.apache.commons.logging.Log;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 /**
  * Context for a function call. Objects of this kind are passed with a
  * function call.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class CallContext
 extends Object
 implements Responder, Log {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String FQCN = CallContext.class.getName();
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(CallContext.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallContext</code>. The state will be set
     * to {@link #UNINITIALIZED}.
     *
     * <p />Before this object can be used, {@link #reset(ServletRequest)} must
     * be called.
     *
     * @param api
     *    the API for which this <code>CallContext</code> will be used, cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null</code>.
     */
    CallContext(API api) throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("api", api);
 
       _api          = api;
       _state        = UNINITIALIZED;
       _success      = true;
       _code         = null;
       _stringWriter = new FastStringWriter();
       _xmlOutputter = new XMLOutputter();
       _callID       = -1;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API for which this CallContext is used. This field is initialized by
     * the constructor and can never be <code>null</code>.
     */
    private final API _api;
 
    /**
     * The start time of the call, as a number of milliseconds since midnight
     * January 1, 1970 UTC.
     */
    private long _start;
 
    /**
     * The original servlet request.
     */
    private ServletRequest _request;
 
    /**
     * The character stream to send the output to. This field is initialized by
     * the constructor and can never be <code>null</code>.
     */
    private final FastStringWriter _stringWriter;
 
    /**
     * The XML outputter. It is initialized by the constructor and sends its
     * output to {@link #_stringWriter}.
     */
    private final XMLOutputter _xmlOutputter;
 
    /**
     * The current state.
     */
    private ResponderState _state;
 
    /**
     * The number of element tags currently open within the data section.
     */
    private int _elementDepth;
 
    /**
     * The name of the function currently being called. This field is
     * initialized by {@link #reset(ServletRequest)} and can be set to
     * <code>null</code>.
     */
    private String _functionName;
 
    /**
     * The function currently being called. This field is initialized by
     * {@link #reset(ServletRequest)} and can be set to <code>null</code>.
     */
    private Function _function;
 
    /**
     * The response validator currently in effect. This field is initialized by
     * {@link #reset(ServletRequest)} and can be set to <code>null</code>. This
     * field is set if and only if {@link #_request} is set.
     */
    private ResponseValidator _responseValidator;
 
    /**
     * The logger associated with the function. This field is set if and only
     * if {@link #_function} is set.
     */
    private Logger _logger;
 
    /**
     * The log prefix for log messages.
     */
    private String _logPrefix;
 
    /**
     * The session for this call.
     */
    private Session _session;
 
    /**
     * Flag that indicates if the session ID should be added as a parameter to
     * the response.
     */
    private boolean _returnSessionID;
 
    /**
     * Success indication. Defaults to <code>true</code> and will <em>only</em>
     * be set to <code>false</code> if and only if
     * {@link #startResponse(boolean,String)} is called with the first
     * parameter (<em>success</em>) set to <code>false</code>.
     */
    private boolean _success;
 
    /**
     * Return code. The default is <code>null</code> and will <em>only</em> be
     * set to something else if and only if
     * {@link #startResponse(boolean,String)} is called with the second
     * parameter (<em>code</em>) set to a non-<code>null</code>, non-empty
     * value.
     */
    private String _code;
 
    /**
     * The call ID, unique in the context of the pertaining function.
     */
    private int _callID;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Resets this <code>CallContext</code>. The servlet request will be set to
     * <code>null</code> and the state will be set to {@link #UNINITIALIZED}.
     *
     * <p />Before this object can be used again,
     * {@link #reset(ServletRequest)} must be called.
     */
    void reset() {
       _request      = null;
       _state        = UNINITIALIZED;
       _success      = true;
       _code         = null;
       _functionName = null;
       _function     = null;
       _responseValidator = null;
       _logger       = null;
       _callID       = -1;
       _logPrefix    = null;
       _session      = null;
    }
 
    /**
     * Resets this <code>CallContext</code> and configures it for the specified
     * servlet request. This resets the state to {@link #BEFORE_START}.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws MissingSessionIDException
     *    if no session ID is specified in the request.
     *
     * @throws InvalidSessionIDException
     *    if the session ID specified in the request is considered invalid.
     *
     * @throws UnknownSessionIDException
     *    if the session ID specified in the request is valid, but unknown.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    void reset(ServletRequest request)
    throws IllegalArgumentException,
           MissingSessionIDException,
           InvalidSessionIDException,
           UnknownSessionIDException,
           IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       _start   = System.currentTimeMillis();
       _request = request;
       _state   = BEFORE_START;
       _success = true;
       _code    = null;
 
       _stringWriter.getBuffer().clear();
       _xmlOutputter.reset(_stringWriter, "UTF-8");
       _xmlOutputter.declaration();
 
       // Determine the function name
       String functionName = request.getParameter("_function");
       if (functionName == null) {
          functionName = request.getParameter("function");
       }
       if (functionName == null) {
          functionName = _api.getDefaultFunctionName();
       }
       _functionName = functionName;
 
       // Determine the XSLT stylesheet, if any
       String xslt = request.getParameter("_xslt");
       if (xslt != null) {
          _xmlOutputter.pi("xml-stylesheet", "type=\"text/xsl\" href=\"" + xslt + "\"");
       }
 
       // Determine the function object, logger, call ID, log prefix
       _function  = (functionName == null) ? null : _api.getFunction(functionName);
       _logger    = (_function    == null) ? null : _function.getLogger();
       _callID    = (_function    == null) ? -1   : _function.assignCallID();
       _logPrefix = (_function    == null) ? ""   : "Call " + _functionName + ':' + _callID + ": ";
 
       // Determine the response validator
       _responseValidator = (_function == null)
                          ? NullResponseValidator.SINGLETON
                          : _function.getResponseValidator();
 
       // Determine the active session
       if (_function != null && _function.isSessionBased()) {
          String sessionID = request.getParameter("_session");
          if (sessionID == null || sessionID.length() == 0) {
             throw MissingSessionIDException.SINGLETON;
          } else {
             try {
                _session = _api.getSessionByString(sessionID);
             } catch (TypeValueException exception) {
                LOG.error("Invalid value for session ID type: \"" + sessionID + "\".");
                throw InvalidSessionIDException.SINGLETON;
             }
             if (_session == null) {
                throw UnknownSessionIDException.SINGLETON;
             }
          }
       }
 
       _returnSessionID = false;
    }
 
    /**
     * Returns the start time of the call.
     *
     * @return
     *    the timestamp indicating when the call was started, as a number of
     *    milliseconds since midnight January 1, 1970 UTC.
     */
    public long getStart() {
       return _start;
    }
 
    /**
     * Returns the character stream the XML output is sent to.
     *
     * @return
     *    the underlying {@link FastStringWriter}, not <code>null</code>.
     */
    FastStringWriter getStringWriter() {
       return _stringWriter;
    }
 
    /**
     * Returns the <code>XMLOutputter</code> that is used to generate XML.
     *
     * @return
     *    the underlying {@link XMLOutputter} that sends its output to the
     *    {@link FastStringWriter}.
     */
    XMLOutputter getXMLOutputter() {
       return _xmlOutputter;
    }
 
    /**
     * Returns the stored success indication. The default is <code>true</code>
     * and it will <em>only</em> be set to <code>false</code> if and only if
     * {@link #startResponse(boolean,String)} is called with the first
     * parameter (<em>success</em>) set to <code>false</code>.
     *
     * @return
     *    the success indication.
     */
    final boolean getSuccess() {
       return _success;
    }
 
    /**
     * Returns the stored return code. The default is <code>null</code>
     * and it will <em>only</em> be set to something else if and only if
     * {@link #startResponse(boolean,String)} is called with the second
     * parameter (<em>code</em>) set to a non-<code>null</code>, non-empty
     * value.
     *
     * @return
     *    the return code, can be <code>null</code>.
     */
    final String getCode() {
       return _code;
    }
 
    /**
     * Returns the session for this call, if any.
     *
     * @return
     *    the session for this call, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if there is no current function (i.e.
     *    {@link #getFunction()}<code> == null</code> or if the current
     *    function is not session-based.
     */
    public Session getSession() throws IllegalStateException {
 
       // Check preconditions
       if (_function == null) {
          throw new IllegalStateException("There is no current function.");
       } else if (_function.isSessionBased() == false) {
          throw new IllegalStateException("The function " + _functionName + " is not session-based.");
       }
 
       // Get the session
       return _session;
    }
 
    public Session createSession() {
 
       // Check preconditions
       if (_function == null) {
          throw new InternalError("There is no current function.");
       }
 
       // Create the session
       Session session = _api.createSession();
 
       // Store the session and remember that we have to send it down
       _session         = session;
       _returnSessionID = true;
 
       return session;
    }
 
    /**
     * Returns the name of the function called.
     *
     * @return
     *    the name of the function called, or <code>null</code> if there is no
     *    function specificied.
     */
    public String getFunctionName() {
       return _functionName;
    }
 
    /**
     * Returns the function that is being called.
     *
     * @return
     *    the function called, or <code>null</code> if there is no function
     *    specificied or if there was no function in the API with the specified
     *    name (see {@link #getFunctionName()}).
     */
    public Function getFunction() {
       return _function;
    }
 
    /**
     * Returns the value of a parameter with the specificied name. Note that
     * reserved parameters, i.e. those starting with an underscore
     * (<code>'_'</code>) cannot be retrieved.
     *
     * @param name
     *    the name of the parameter, not <code>null</code>.
     *
     * @return
     *    the value of the parameter, or <code>null</code> if the parameter is
     *    not set, never an empty string (<code>""</code>) because it will be
     *    returned as being <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null</code>.
     */
    public String getParameter(String name)
    throws IllegalArgumentException {
 
       // Check arguments
       if (name == null) {
          throw new IllegalArgumentException("name == null");
       }
 
       // XXX: In a later version, support a parameter named 'function'
 
       if (_request != null && name.length() > 0 && !"function".equals(name) && name.charAt(0) != '_') {
          String value = _request.getParameter(name);
          return "".equals(value) ? null : value;
       }
       return null;
    }
 
    /**
     * Returns the assigned call ID. This ID is unique within the context of
     * the pertaining function. If no call ID is assigned, then <code>-1</code>
     * is returned.
     *
     * @return
     *    the assigned call ID for the function, or <code>-1</code> if none is
     *    assigned.
     */
    public int getCallID() {
       return _callID;
    }
 
    public final void startResponse(ResultCode resultCode)
    throws IllegalStateException, InvalidResponseException, IOException {
       if (resultCode == null) {
          startResponse(true, null);
       } else {
          startResponse(resultCode.getSuccess(), resultCode.getValue());
       }
    }
 
    public final void startResponse(boolean success)
    throws IllegalStateException, InvalidResponseException, IOException {
       startResponse(success, null);
    }
 
    public final void startResponse(boolean success, String returnCode)
    throws IllegalStateException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Validate
       _responseValidator.startResponse(success, returnCode);
 
       _xmlOutputter.startTag("result");
 
       if (success) {
          _xmlOutputter.attribute("success", "true");
       } else {
          _success = false;
          _xmlOutputter.attribute("success", "false");
       }
 
       if (returnCode != null && returnCode.length() > 0) {
          _code = returnCode;
          _xmlOutputter.attribute("code", returnCode);
       }
 
       // Reset the state
       _state = WITHIN_PARAMS;
 
       // Add the session ID, if any
       if (_returnSessionID) {
          _xmlOutputter.startTag("param");
          _xmlOutputter.attribute("name", "_session");
          _xmlOutputter.pcdata(_session.getIDString());
          _xmlOutputter.endTag();
          _returnSessionID = false;
       }
    }
 
    public final void param(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != BEFORE_START && _state != WITHIN_PARAMS) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Check arguments
       if (name == null || value == null) {
          if (name == null && value == null) {
             throw new IllegalArgumentException("name == null && value == null");
          } else if (name == null) {
             throw new IllegalArgumentException("name == null");
          } else {
             throw new IllegalArgumentException("value == null");
          }
       }
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Validate
       _responseValidator.param(name, value);
 
       // Write <param name="name">value</param>
       _xmlOutputter.startTag("param");
       _xmlOutputter.attribute("name", name);
       _xmlOutputter.pcdata(value);
       _xmlOutputter.endTag();
 
       // Reset the state
       _state = WITHIN_PARAMS;
    }
 
    private final void startDataSection()
    throws IOException {
       _state = ERROR;
       _xmlOutputter.startTag("data");
       _state = WITHIN_ELEMENT;
 
       _elementDepth = 0;
    }
 
    public final void startTag(String type)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException, IOException {
 
       // Check state
       if (_state == AFTER_END) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Check argument
       if (type == null) {
          throw new IllegalArgumentException("type == null");
       } else if (type.length() == 0) {
          throw new IllegalArgumentException("type.equals(\"\")");
       }
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Enter the <data/> section if necessary
       if (_state == WITHIN_PARAMS) {
          startDataSection();
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Validate
       _responseValidator.startTag(type);
 
       // Write the start tag
       _xmlOutputter.startTag(type);
       _elementDepth++;
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void attribute(String name, String value)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Validate
       _responseValidator.attribute(name, value);
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the attribute
       _xmlOutputter.attribute(name, value);
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void pcdata(String text)
    throws IllegalStateException, IllegalArgumentException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Validate
       _responseValidator.pcdata(text);
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the PCDATA
       _xmlOutputter.pcdata(text);
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public final void endTag()
    throws IllegalStateException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
       if (_elementDepth == 0) {
          throw new IllegalStateException("There are no more elements in the data section to close.");
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Validate
       _responseValidator.endTag();
 
       // End the tag
       _xmlOutputter.endTag();
       _elementDepth--;
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public void fail(ResultCode resultCode)
    throws IllegalArgumentException, IllegalStateException, InvalidResponseException, IOException {
 
       // Check state
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Check argument
      if (resultCode != null && resultCode.getSuccess()) {
          throw new IllegalArgumentException("resultCode.getSuccess() == true");
       }
 
       startResponse(resultCode);
       endResponse();
    }
 
    public final void endResponse() throws InvalidResponseException, IOException {
 
       // Short-circuit if the response is already ended
       if (_state == AFTER_END) {
          return;
       }
 
       // Start the response if necesary
       if (_state == BEFORE_START) {
          startResponse(true, null);
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Validate
       _responseValidator.endResponse();
 
       // Close all open elements
       _xmlOutputter.endDocument();
 
       // Set the state
       _state = AFTER_END;
    }
 
    public void trace(Object message) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, null);
    }
 
    public void trace(Object message, Throwable t) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, t);
    }
 
    public void debug(Object message) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, null);
    }
 
    public void debug(Object message, Throwable t) {
       _logger.log(FQCN, Priority.DEBUG, _logPrefix + message, t);
    }
 
    public void info(Object message) {
       _logger.log(FQCN, Priority.INFO, _logPrefix + message, null);
    }
 
    public void info(Object message, Throwable t) {
       _logger.log(FQCN, Priority.INFO, _logPrefix + message, t);
    }
 
    public void warn(Object message) {
       _logger.log(FQCN, Priority.WARN, _logPrefix + message, null);
    }
 
    public void warn(Object message, Throwable t) {
       _logger.log(FQCN, Priority.WARN, _logPrefix + message, t);
    }
 
    public void error(Object message) {
       _logger.log(FQCN, Priority.ERROR, _logPrefix + message, null);
    }
 
    public void error(Object message, Throwable t) {
       _logger.log(FQCN, Priority.ERROR, _logPrefix + message, t);
    }
 
    public void fatal(Object message) {
       _logger.log(FQCN, Priority.FATAL, _logPrefix + message, null);
    }
 
    public void fatal(Object message, Throwable t) {
       _logger.log(FQCN, Priority.FATAL, _logPrefix + message, t);
    }
 
    public boolean isDebugEnabled() {
       return _logger.isDebugEnabled();
    }
 
    public boolean isErrorEnabled() {
       return _logger.isEnabledFor(Priority.ERROR);
    }
 
    public boolean isFatalEnabled() {
       return _logger.isEnabledFor(Priority.FATAL);
    }
 
    public boolean isInfoEnabled() {
       return _logger.isInfoEnabled();
    }
 
    public boolean isTraceEnabled() {
       return _logger.isDebugEnabled();
    }
 
    public boolean isWarnEnabled() {
       return _logger.isEnabledFor(Priority.WARN);
    }
 }
