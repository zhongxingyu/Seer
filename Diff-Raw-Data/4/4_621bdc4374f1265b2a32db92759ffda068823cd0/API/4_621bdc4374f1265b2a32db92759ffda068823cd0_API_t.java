 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Properties;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class API
 extends Object
 implements DefaultReturnCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified value is <code>null</code> or an empty string.
     * Only if it is then <code>true</code> is returned.
     *
     * @param value
     *    the value to check.
     *
     * @return
     *    <code>true</code> if and only if <code>value != null &amp;&amp;
     *    value.length() != 0</code>.
     */
    protected final static boolean isMissing(String value) {
       return value == null || value.length() == 0;
    }
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>API</code> object.
     */
    protected API() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Initialises this API.
     *
     * <p />The implementation of this method in class {@link API} is empty.
     *
     * @param properties
     *    the properties, can be <code>null</code>.
     *
     * @throws Throwable
     *    if the initialisation fails.
     */
    public void init(Properties properties)
    throws Throwable {
       // empty
    }
 
 
    /**
     * Forwards a call to the <code>handleCall(CallContext)</code> method.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @param map
     *    the parameters, not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    final void handleCall(PrintWriter out, Map map) throws IOException {
 
       // Reset the XMLOutputter
       StringWriter stringWriter = new StringWriter();
       XMLOutputter xmlOutputter = new XMLOutputter(stringWriter, "UTF-8");
 
       // Create a new call context
       CallContext context = new CallContext(xmlOutputter, map);
 
       // Forward the call
       boolean succeeded = false;
       try {
          handleCall(context);
          succeeded = true;
       } catch (Throwable exception) {
          long end = System.currentTimeMillis();
          long start = context.getStart();
          long duration = end - start;
          final String code = INTERNAL_ERROR;
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", code);
          xmlOutputter.startTag("param");
          xmlOutputter.attribute("name", "_exception.class");
          xmlOutputter.pcdata(exception.getClass().getName());
 
          String message = exception.getMessage();
          if (message != null && message.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.message");
             xmlOutputter.pcdata(message);
          }
 
          StringWriter stWriter = new StringWriter();
          PrintWriter printWriter = new PrintWriter(stWriter);
          exception.printStackTrace(printWriter);
          String stackTrace = stWriter.toString();
          if (stackTrace != null && stackTrace.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.stacktrace");
             xmlOutputter.pcdata(stackTrace);
          }
          xmlOutputter.close();
 
          callFailed(context.getFunction(), start, duration, code);
       }
 
       if (succeeded) {
          out.print(stringWriter.toString());
       }
       out.flush();
    }
 
    /**
     * Handles a call to this API.
     *
     * @param context
     *    the context for this call, never <code>null</code>.
     *
     * @throws Throwable
     *    if anything goes wrong.
     */
    protected abstract void handleCall(CallContext context)
    throws Throwable;
 
    /**
     * Callback method invoked when a function throws an exception. This method
    * will be invoked if and only if {@link #handleCall(CallContext)} throws
    * an exception of some sort.
     *
     * @param function
     *    the name of the function, will not be <code>null</code>.
     *
     * @param start
     *    the timestamp indicating when the call was started, as a number of
     *    milliseconds since midnight January 1, 1970 UTC.
     *
     * @param duration
     *    the duration of the function call, as a number of milliseconds.
     *
     * @param code
     *    the function result code, or <code>null</code>.
     */
    protected void callFailed(String function, long start, long duration, String code) {
       // empty
    }
 }
