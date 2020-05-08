 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.http.HTTPRequester;
 
 /**
  * Abstract base class for <code>FunctionCaller</code> implementations.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.41
  */
 public abstract class AbstractFunctionCaller
 extends Object
 implements FunctionCaller {
 
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
     * Creates a new <code>AbstractFunctionCaller</code>.
    *
    * @param url
    *    the URL for the API, not <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>url == null</code>.
     */
    protected AbstractFunctionCaller() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public CallResult call(String functionName)
    throws IOException, InvalidCallResultException {
       return call(null, functionName, null);
    }
 
    public CallResult call(String functionName, Map parameters)
    throws IllegalArgumentException, IOException, InvalidCallResultException {
       return call(null, functionName, parameters);
    }
 
    public CallResult call(CallRequest request)
    throws IllegalArgumentException, IOException, InvalidCallResultException {
       MandatoryArgumentChecker.check("request", request);
       return call(request.getSessionID(),
                   request.getFunctionName(),
                   request.getParameters());
    }
 }
