 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import javax.servlet.ServletRequest;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.io.FastStringWriter;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Context for a function call. Objects of this kind are passed with a
  * function call.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class CallContext
 extends Object
 implements Responder {
 
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
     * Constructs a new <code>CallContext</code>. The state will be set
     * to {@link #UNINITIALIZED}.
     *
     * <p />Before this object can be used, {@link #reset(ServletRequest)} must
     * be called.
     */
    CallContext() {
      _state = UNINITIALIZED;
       _success      = true;
       _stringWriter = new FastStringWriter();
       _xmlOutputter = new XMLOutputter();
    }
 
    /**
     * Constructs a new <code>CallContext</code> and initializes it for the
     * specified servlet request. The state will be set to
     * {@link #BEFORE_START}.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    CallContext(ServletRequest request)
    throws IllegalArgumentException, IOException {
 
       this();
       reset(request);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
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
       _request = null;
       _state   = UNINITIALIZED;
       _success = true;
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
     * @throws IOException
     *    if an I/O error occurs.
     */
    void reset(ServletRequest request)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       _start   = System.currentTimeMillis();
       _request = request;
       _state   = BEFORE_START;
       _success = true;
 
       _stringWriter.getBuffer().clear();
       _xmlOutputter.reset(_stringWriter, "UTF-8");
    }
 
    /**
     * Returns the start time of the call.
     *
     * @return
     *    the timestamp indicating when the call was started, as a number of
     *    milliseconds since midnight January 1, 1970 UTC.
     */
    long getStart() {
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
     * Returns the name of the function called.
     *
     * @return
     *    the function called, or <code>null</code> if there is no function
     *    specificied.
     */
    public String getFunction() {
 
       // Check arguments
       if (_request != null) {
          String function = _request.getParameter("_function");
          if (function == null) {
             function = _request.getParameter("function");
          }
          return function;
       } else {
          return null;
       }
    }
 
    /**
     * Returns the value of a parameter with the specificied name.
     *
     * @param name
     *    the name of the parameter, not <code>null</code>.
     *
     * @return
     *    the value of the parameter, or <code>null</code> if the parameter is
     *    not set.
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
 
       if (_request != null && !"function".equals(name)) {
          return _request.getParameter(name);
       } else {
          return null;
       }
    }
 
    public final void startResponse(boolean success, String returnCode)
    throws IllegalStateException, IOException {
 
       // Check state
       if (_state != BEFORE_START) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state   = ERROR;
 
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
    }
 
    public final void param(String name, String value)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
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
    throws IllegalStateException, IllegalArgumentException, IOException {
 
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
 
       // Write the start tag
       _xmlOutputter.startTag(type);
       _elementDepth++;
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void attribute(String name, String value)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the attribute
       _xmlOutputter.attribute(name, value);
 
       // Reset the state
       _state = START_TAG_OPEN;
    }
 
    public final void pcdata(String text)
    throws IllegalStateException, IllegalArgumentException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // Write the PCDATA
       _xmlOutputter.pcdata(text);
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public final void endTag()
    throws IllegalStateException, IOException {
 
       // Check state
       if (_state != START_TAG_OPEN && _state != WITHIN_ELEMENT) {
          throw new IllegalStateException("The state is " + _state + '.');
       }
       if (_elementDepth == 0) {
          throw new IllegalStateException("There are no more elements in the data section to close.");
       }
 
       // Temporarily enter the ERROR state
       _state = ERROR;
 
       // End the tag
       _xmlOutputter.endTag();
       _elementDepth--;
 
       // Reset the state
       _state = WITHIN_ELEMENT;
    }
 
    public final void endResponse() throws IOException {
 
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
 
       // Close all open elements
       _xmlOutputter.close();
 
       // Flush the output stream
       _xmlOutputter.getWriter().flush();
    }
 }
