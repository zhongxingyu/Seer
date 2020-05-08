 /*
  * $Id: XINSServiceCaller.java,v 1.181 2007/12/17 16:50:42 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import org.xins.common.FormattedParameters;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.http.HTTPCallConfig;
 import org.xins.common.http.HTTPCallException;
 import org.xins.common.http.HTTPCallRequest;
 import org.xins.common.http.HTTPCallResult;
 import org.xins.common.http.HTTPServiceCaller;
 import org.xins.common.http.StatusCodeHTTPCallException;
 import org.xins.common.service.CallConfig;
 import org.xins.common.service.CallException;
 import org.xins.common.service.CallExceptionList;
 import org.xins.common.service.CallRequest;
 import org.xins.common.service.CallResult;
 import org.xins.common.service.ConnectionTimeOutCallException;
 import org.xins.common.service.ConnectionRefusedCallException;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.GenericCallException;
 import org.xins.common.service.IOCallException;
 import org.xins.common.service.ServiceCaller;
 import org.xins.common.service.SocketTimeOutCallException;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.service.TotalTimeOutCallException;
 import org.xins.common.service.UnexpectedExceptionCallException;
 import org.xins.common.service.UnknownHostCallException;
 import org.xins.common.service.UnsupportedProtocolException;
 import org.xins.common.spec.ErrorCodeSpec;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.xml.Element;
 
 /**
  * XINS service caller. This class can be used to perform a call to a XINS
  * service, over HTTP, and fail-over to other XINS services if the first one
  * fails.
  *
  * <h2>Supported protocols</h2>
  *
  * <p>This service caller currently only supports the HTTP protocol. If a
  * {@link TargetDescriptor} is passed to the constructor with a different
  * protocol, then an {@link UnsupportedProtocolException} is thrown. In the
  * future, HTTPS and other protocols are expected to be supported as well.
  *
  * <h2>Load-balancing and fail-over</h2>
  *
  * <p>To perform a XINS call, use {@link #call(XINSCallRequest)}. Fail-over
  * and load-balancing can be performed automatically.
  *
  * <p>How load-balancing is done depends on the {@link Descriptor} passed to
  * the {@link #XINSServiceCaller(Descriptor)} constructor. If it is a
  * {@link TargetDescriptor}, then only this single target service is called
  * and no load-balancing is performed. If it is a
  * {@link org.xins.common.service.GroupDescriptor}, then the configuration of
  * the <code>GroupDescriptor</code> determines how the load-balancing is done.
  * A <code>GroupDescriptor</code> is a recursive data structure, which allows
  * for fairly advanced load-balancing algorithms.
  *
  * <p>If a call attempt fails and there are more available target services,
  * then the <code>XINSServiceCaller</code> may or may not fail-over to a next
  * target. If the request was not accepted by the target service, then
  * fail-over is considered acceptable and will be performed. This includes
  * the following situations:
  *
  * <ul>
  *    <li>if the <em>failOverAllowed</em> property is set to <code>true</code>
  *        for the {@link XINSCallRequest};
  *    <li>on connection refusal;
  *    <li>if a connection attempt times out;
  *    <li>if an HTTP status code other than 200-299 is returned;
  *    <li>if the XINS error code <em>_InvalidRequest</em> is returned;
  *    <li>if the XINS error code <em>_DisabledFunction</em> is returned.
  * </ul>
  *
  * <p>If none of these conditions holds, then fail-over is not considered
  * acceptable and will not be performed.
  *
  * <h2>Example code</h2>
  *
  * <p>The following example code snippet constructs a
  * <code>XINSServiceCaller</code> instance:
  *
  * <blockquote><pre>// Initialize properties for the services. Normally these
 // properties would come from a configuration source, like a file.
 {@link org.xins.common.collections.BasicPropertyReader} properties = new {@link org.xins.common.collections.BasicPropertyReader#BasicPropertyReader() org.xins.common.collections.BasicPropertyReader}();
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi",         "group, random, server1, server2");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server1", "service, http://server1/myapi, 10000");
 properties.{@link org.xins.common.collections.BasicPropertyReader#set(String,String) set}("myapi.server2", "service, http://server2/myapi, 12000");
 
 // Construct a descriptor and a XINSServiceCaller instance
 {@link Descriptor Descriptor} descriptor = {@link org.xins.common.service.DescriptorBuilder DescriptorBuilder}.{@link org.xins.common.service.DescriptorBuilder#build(PropertyReader,String) build}(properties, "myapi");
 XINSServiceCaller caller = new {@link #XINSServiceCaller(Descriptor) XINSServiceCaller}(descriptor);</pre></blockquote>
  *
  * <p>Then the following code snippet uses this <code>XINSServiceCaller</code>
  * to perform a call to a XINS function named <em>_GetStatistics</em>, using
  * HTTP POST:
  *
  * <blockquote><pre>// Prepare for the call
 {@link String}          function = "_GetStatistics";
 {@link org.xins.common.collections.PropertyReader}  params   = null;
 boolean         failOver = true;
 {@link org.xins.common.http.HTTPMethod}      method   = {@link org.xins.common.http.HTTPMethod}.{@link org.xins.common.http.HTTPMethod#POST POST};
 {@link XINSCallRequest} request  = new {@link XINSCallRequest#XINSCallRequest(String,PropertyReader,boolean,HTTPMethod) XINSCallRequest}(function, params, failOver, method);
 
 // Perform the call
 {@link XINSCallResult} result = caller.{@link #call(XINSCallRequest) call}(request);</pre></blockquote>
  *
  * @version $Revision: 1.181 $ $Date: 2007/12/17 16:50:42 $
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.0.0
  */
 public class XINSServiceCaller extends ServiceCaller {
 
    /**
     * Performs (client-side) transaction logging.
     * 
     * 
     */
   private static final void logTransaction(Throwable exception, long start, String url, String functionName, long duration, String errorCode, FormattedParameters inParams, FormattedParameters outParams) {
      // TODO FIXME
    }
    
    /**
     * The result parser. This field cannot be <code>null</code>.
     */
    private final XINSCallResultParser _parser;
 
    /**
     * The <code>CAPI</code> object that uses this caller. This field is
     * <code>null</code> if this caller is not used by a <code>CAPI</code>
     * class.
     */
    private AbstractCAPI _capi;
 
    /**
     * The map containing the service caller to call for the descriptor.
     * The key of the {@link HashMap} is a {@link TargetDescriptor} and the value
     * is a {@link ServiceCaller}.
     */
    private HashMap<TargetDescriptor, ServiceCaller> _serviceCallers;
 
    /**
     * Constructs a new <code>XINSServiceCaller</code> with the specified
     * descriptor and call configuration.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the call configuration object for this service caller, or
     *    <code>null</code> if a default one should be associated with this
     *    service caller.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws UnsupportedProtocolException
     *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
     *    with an unsupported protocol.
     *
     * @since XINS 1.1.0
     */
    public XINSServiceCaller(Descriptor descriptor, XINSCallConfig callConfig)
    throws IllegalArgumentException, UnsupportedProtocolException {
 
       // Call constructor of superclass
       super(descriptor, callConfig);
 
       // Initialize the fields
       _parser      = new XINSCallResultParser();
    }
 
    /**
     * Constructs a new <code>XINSServiceCaller</code> with the specified
     * descriptor and the default HTTP method.
     *
     * @param descriptor
     *    the descriptor of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws UnsupportedProtocolException
     *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
     *    with an unsupported protocol (<em>since XINS 1.1.0</em>).
     */
    public XINSServiceCaller(Descriptor descriptor)
    throws IllegalArgumentException, UnsupportedProtocolException {
       this(descriptor, null);
    }
 
    /**
     * Constructs a new <code>XINSServiceCaller</code> with no
     * descriptor (yet) and the default HTTP method.
     * 
     * <p>Before actual calls can be made, {@link #setDescriptor(Descriptor)}
     * should be used to set the descriptor.
     *
     * @since XINS 1.2.0
     */
    public XINSServiceCaller() {
       this((Descriptor) null, (XINSCallConfig) null);
    }
 
    /**
     * Checks if the specified protocol is supported (implementation method).
     * The protocol is the part in a URL before the string <code>"://"</code>).
     *
     * <p>This method should only ever be called from the
     * {@link #isProtocolSupported(String)} method.
     *
     * <p>The implementation of this method in class
     * <code>XINSServiceCaller</code> throws an
     * {@link UnsupportedOperationException} unless the protocol is
     * <code>"http"</code>, <code>"https"</code> or <code>"file"</code>.
     *
     * @param protocol
     *    the protocol, guaranteed not to be <code>null</code> and guaranteed
     *    to be in lower case.
     *
     * @return
     *    <code>true</code> if the specified protocol is supported, or
     *    <code>false</code> if it is not.
     *
     * @since XINS 1.2.0
     */
    protected boolean isProtocolSupportedImpl(String protocol) {
       return  "http".equals(protocol)
           || "https".equals(protocol)
           ||  "file".equals(protocol);
    }
 
    @Override
    public void setDescriptor(Descriptor descriptor) {
       super.setDescriptor(descriptor);
 
       // Create the ServiceCaller for each descriptor
       if (_serviceCallers == null) {
          _serviceCallers = new HashMap<TargetDescriptor, ServiceCaller>();
       } else {
          _serviceCallers.clear();
       }
       
       // Create an HTTP- or File-caller for each descriptor
       if (descriptor != null) {
          for (TargetDescriptor target : descriptor.targets()) {
             
             String protocol = target.getProtocol().toLowerCase();
             
             ServiceCaller caller;
 
             // HTTP or HTTPS protocol
             if ("http".equals(protocol) || "https".equals(protocol)) {
                caller = new HTTPServiceCaller(target);
 
             // FILE protocol
             } else if ("file".equals(protocol)) {
                caller = new FileServiceCaller(target);
                
             // Unsupported protocol
             } else {
                // TODO: Consider using a specific exception type 
                throw new RuntimeException("Unsupported protocol \"" + protocol + "\" in descriptor.");
             }
             
             _serviceCallers.put(target, caller);
          }
       }
    }
 
    /**
     * Sets the associated <code>CAPI</code> instance.
     *
     * <p>This method is expected to be called only once, before any calls are
     * made with this caller.
     *
     * @param capi
     *    the associated <code>CAPI</code> instance, or
     *    <code>null</code>.
     */
    void setCAPI(AbstractCAPI capi) {
       _capi = capi;
    }
 
    /**
     * Returns a default <code>CallConfig</code> object. This method is called
     * by the <code>ServiceCaller</code> constructor if no
     * <code>CallConfig</code> object was given.
     *
     * <p>The implementation of this method in class {@link XINSServiceCaller}
     * returns a standard {@link XINSCallConfig} object which has unconditional
     * fail-over disabled and the HTTP method set to
     * {@link org.xins.common.http.HTTPMethod#POST POST}.
     *
     * @return
     *    a new {@link XINSCallConfig} instance with default settings, never
     *    <code>null</code>.
     */
    protected CallConfig getDefaultCallConfig() {
       return new XINSCallConfig();
    }
 
    /**
     * Sets the <code>XINSCallConfig</code> associated with this XINS service
     * caller.
     *
     * @param config
     *    the fall-back {@link XINSCallConfig} object for this service caller,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null</code>.
     *
     * @since XINS 1.2.0
     */
    protected final void setXINSCallConfig(XINSCallConfig config)
    throws IllegalArgumentException {
       super.setCallConfig(config);
    }
 
    /**
     * Returns the <code>XINSCallConfig</code> associated with this service
     * caller.
     *
     * <p>This method is the type-safe equivalent of {@link #getCallConfig()}.
     *
     * @return
     *    the fall-back {@link XINSCallConfig} object for this XINS service
     *    caller, never <code>null</code>.
     *
     * @since XINS 1.2.0
     */
    public final XINSCallConfig getXINSCallConfig() {
       return (XINSCallConfig) getCallConfig();
    }
 
    /**
     * Executes the specified XINS call request towards one of the associated
     * targets. If the call succeeds with one of these targets, then a
     * {@link XINSCallResult} object is returned. Otherwise, if none of the
     * targets could successfully be called, a
     * {@link org.xins.common.service.CallException} is thrown.
     *
     * <p>If the call succeeds, but the result is unsuccessful, then an
     * {@link UnsuccessfulXINSCallException} is thrown, which contains the
     * result.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @param callConfig
     *    the call configuration, or <code>null</code> if the one specified in
     *    the request should be used, or -if the request does not specify any
     *    either- the one specified for this service caller.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     *
     * @throws XINSCallException
     *    if the first call attempt failed due to a XINS-related reason and
     *    all the other call attempts failed as well.
     *
     * @since XINS 1.1.0
     */
    public XINSCallResult call(XINSCallRequest request,
                               XINSCallConfig  callConfig)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       // Determine when we started the call
       long start = System.currentTimeMillis();
 
       // Perform the call
       XINSCallResult result;
       try {
          result = (XINSCallResult) doCall(request,callConfig);
 
       // Handle failures
       } catch (Throwable exception) {
 
          // Log that the call completely failed, unless the back-end returned
          // a functional error code. We assume that a functional error code
          // can never fail-over, so this issue will have been logged at the
          // correct (non-error) level already.
          if (!(exception instanceof UnsuccessfulXINSCallException) ||
                ((UnsuccessfulXINSCallException) exception).getType() != ErrorCodeSpec.FUNCTIONAL) {
 
             // Determine how long the call took
             long duration = System.currentTimeMillis() - start;
 
             // Serialize all parameters, including the data section, for logging
             PropertyReader parameters = request.getParameters();
             Element dataSection = request.getDataSection();
             FormattedParameters params = new FormattedParameters(parameters, dataSection, "(null)", "&", 160);
 
             // Serialize the exception chain
             String chain = exception.getMessage();
 
             Log.log_2113(request.getFunctionName(), params, duration, chain);
          }
 
          // Allow only GenericCallException, HTTPCallException and
          // XINSCallException to proceed
          if (exception instanceof GenericCallException) {
             throw (GenericCallException) exception;
          } if (exception instanceof HTTPCallException) {
             throw (HTTPCallException) exception;
          } if (exception instanceof XINSCallException) {
             throw (XINSCallException) exception;
 
          // Unknown kind of exception. This should never happen. Log and
          // re-throw the exception, wrapped within a ProgrammingException
          } else {
             throw Utils.logProgrammingError(exception);
          }
       }
 
       return result;
    }
 
    /**
     * Executes the specified XINS call request towards one of the associated
     * targets. If the call succeeds with one of these targets, then a
     * {@link XINSCallResult} object is returned. Otherwise, if none of the
     * targets could successfully be called, a
     * {@link org.xins.common.service.CallException} is thrown.
     *
     * <p>If the call succeeds, but the result is unsuccessful, then an
     * {@link UnsuccessfulXINSCallException} is thrown, which contains the
     * result.
     *
     * @param request
     *    the call request, not <code>null</code>.
     *
     * @return
     *    the result of the call, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws GenericCallException
     *    if the first call attempt failed due to a generic reason and all the
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the first call attempt failed due to an HTTP-related reason and
     *    all the other call attempts failed as well.
     *
     * @throws XINSCallException
     *    if the first call attempt failed due to a XINS-related reason and
     *    all the other call attempts failed as well.
     */
    public XINSCallResult call(XINSCallRequest request)
    throws IllegalArgumentException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
       return call(request, null);
    }
 
    /**
     * Executes the specified request on the given target. If the call
     * succeeds, then a {@link XINSCallResult} object is returned, otherwise a
     * {@link org.xins.common.service.CallException} is thrown.
     *
     * @param target
     *    the target to call, cannot be <code>null</code>.
     *
     * @param callConfig
     *    the call configuration, never <code>null</code>.
     *
     * @param request
     *    the call request to be executed, must be an instance of class
     *    {@link XINSCallRequest}, cannot be <code>null</code>.
     *
     * @return
     *    the result, if and only if the call succeeded, always an instance of
     *    class {@link XINSCallResult}, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request    == null
     *          || callConfig == null
     *          || target     == null</code>.
     *
     * @throws ClassCastException
     *    if the specified <code>request</code> object is not <code>null</code>
     *    and not an instance of class {@link XINSCallRequest}.
     *
     * @throws GenericCallException
     *    if the call attempt failed due to a generic reason.
     *    other call attempts failed as well.
     *
     * @throws HTTPCallException
     *    if the call attempt failed due to an HTTP-related reason.
     *
     * @throws XINSCallException
     *    if the call attempt failed due to a XINS-related reason.
     */
    public Object doCallImpl(CallRequest      request,
                             CallConfig       callConfig,
                             TargetDescriptor target)
    throws IllegalArgumentException,
           ClassCastException,
           GenericCallException,
           HTTPCallException,
           XINSCallException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("request",    request,
                                      "callConfig", callConfig,
                                      "target",     target);
 
       // Convert arguments to the appropriate classes
       XINSCallRequest xinsRequest = (XINSCallRequest) request;
       XINSCallConfig  xinsConfig  = (XINSCallConfig)  callConfig;
 
       // Get URL, function and parameters (for logging)
       String             url       = target.getURL();
       String             function  = xinsRequest.getFunctionName();
       PropertyReader     p         = xinsRequest.getParameters();
       Element dataSection = xinsRequest.getDataSection();
 
       FormattedParameters params = new FormattedParameters(p, dataSection, "", "&", 160);
 
       // Get the time-out values (for logging)
       int totalTimeOut      = target.getTotalTimeOut();
       int connectionTimeOut = target.getConnectionTimeOut();
       int socketTimeOut     = target.getSocketTimeOut();
 
       // Log: Right before the call is performed
       Log.log_2100(url, function, params);
 
       // Get the contained HTTP request from the XINS request
       HTTPCallRequest httpRequest = xinsRequest.getHTTPCallRequest();
 
       // Convert XINSCallConfig to HTTPCallConfig
       HTTPCallConfig httpConfig = xinsConfig.getHTTPCallConfig();
 
       // Determine the start time. Only required when an unexpected kind of
       // exception is caught.
       long start = System.currentTimeMillis();
 
       // Perform the HTTP call
       HTTPCallResult httpResult;
       long duration;
       try {
          ServiceCaller serviceCaller = _serviceCallers.get(target);
          httpResult = (HTTPCallResult) serviceCaller.doCallImpl(httpRequest, httpConfig, target);
 
       // Call failed due to a generic service calling error
       } catch (GenericCallException exception) {
          duration = exception.getDuration();
          String errorCode;
          if (exception instanceof UnknownHostCallException) {
             Log.log_2102(url, function, params, duration);
             errorCode = "=UnknownHost";
          } else if (exception instanceof ConnectionRefusedCallException) {
             Log.log_2103(url, function, params, duration);
             errorCode = "=ConnectionRefused";
          } else if (exception instanceof ConnectionTimeOutCallException) {
             Log.log_2104(url, function, params, duration, connectionTimeOut);
             errorCode = "=ConnectionTimeOut";
          } else if (exception instanceof SocketTimeOutCallException) {
             Log.log_2105(url, function, params, duration, socketTimeOut);
             errorCode = "=SocketTimeOut";
          } else if (exception instanceof TotalTimeOutCallException) {
             Log.log_2106(url, function, params, duration, totalTimeOut);
            errorCode = "=TotalTimeOut";
          } else if (exception instanceof IOCallException) {
             Log.log_2109(exception, url, function, params, duration);
             errorCode = "=IOError";
          } else if (exception instanceof UnexpectedExceptionCallException) {
             Log.log_2111(exception.getCause(), url, function, params, duration);
             errorCode = "=UnexpectedException";
          } else {
             String detail = "Unrecognized GenericCallException subclass " + exception.getClass().getName() + '.';
             Utils.logProgrammingError(detail);
             errorCode = "=UnrecognizedGenericCallException";
          }
          logTransaction(exception, start, url, function, duration, errorCode, params, null);
          throw exception;
 
       // Call failed due to an HTTP-related error
       } catch (HTTPCallException exception) {
          duration = exception.getDuration();
          if (exception instanceof StatusCodeHTTPCallException) {
             int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
             Log.log_2108(url, function, params, duration, code);
          } else {
             String detail = "Unrecognized HTTPCallException subclass "
                   + exception.getClass().getName() + '.';
             Utils.logProgrammingError(detail);
          }
          throw exception;
 
       // Unknown kind of exception. This should never happen. Log and re-throw
       // the exception, packed up as a CallException.
       } catch (Throwable exception) {
          duration = System.currentTimeMillis() - start;
          Utils.logProgrammingError(exception);
 
          String message = "Unexpected exception: " + exception.getClass().getName()
                + ". Message: " + TextUtils.quote(exception.getMessage()) + '.';
 
          Log.log_2111(exception, url, function, params, duration);
          throw new UnexpectedExceptionCallException(request, target, duration, message, exception);
       }
 
       // Determine duration
       duration = httpResult.getDuration();
 
       // Make sure data was received
       byte[] httpData = httpResult.getData();
       if (httpData == null || httpData.length == 0) {
 
          // Log: No data was received
          Log.log_2110(url, function, params, duration, "No data received.");
 
          // Throw an appropriate exception
          throw InvalidResultXINSCallException.noDataReceived(
             xinsRequest, target, duration);
       }
 
       // Parse the result
       XINSCallResultData resultData;
       try {
          resultData = _parser.parse(httpData);
 
       // If parsing failed, then abort
       } catch (ParseException e) {
 
          // Create a message for the new exception
          String detail = e.getDetail();
          String message = detail != null && detail.trim().length() > 0
                         ? "Failed to parse result: " + detail.trim()
                         : "Failed to parse result.";
 
          // Log: Parsing failed
          Log.log_2110(url, function, params, duration, message);
 
          // Throw an appropriate exception
          throw InvalidResultXINSCallException.parseError(
             httpData, xinsRequest, target, duration, e);
       }
 
       // If the result is unsuccessful, then throw an exception
       String errorCode = resultData.getErrorCode();
       if (errorCode != null) {
 
          boolean functionalError = false;
          ErrorCodeSpec.Type type = null;
          if (_capi != null) {
             functionalError = _capi.isFunctionalError(errorCode);
          }
 
          // Log this
          if (functionalError) {
             Log.log_2115(url, function, params, duration, errorCode);
          } else {
             Log.log_2112(url, function, params, duration, errorCode);
          }
 
          // Standard error codes (start with an underscore)
          if (errorCode.charAt(0) == '_') {
             if (errorCode.equals("_DisabledFunction")) {
                throw new DisabledFunctionException(xinsRequest,
                                                    target,
                                                    duration,
                                                    resultData);
             } else if (errorCode.equals("_InternalError")
                     || errorCode.equals("_InvalidResponse")) {
                throw new InternalErrorException(
                   xinsRequest, target, duration, resultData);
             } else if (errorCode.equals("_InvalidRequest")) {
                throw new InvalidRequestException(
                   xinsRequest, target, duration, resultData);
             } else {
                throw new UnacceptableErrorCodeXINSCallException(
                   xinsRequest, target, duration, resultData);
             }
 
          // Non-standard error codes, CAPI not used
          } else if (_capi == null) {
             throw new UnsuccessfulXINSCallException(
                xinsRequest, target, duration, resultData, null);
 
          // Non-standard error codes, CAPI used
          } else {
             AbstractCAPIErrorCodeException ex =
                _capi.createErrorCodeException(
                   xinsRequest, target, duration, resultData);
 
             if (ex != null) {
                ex.setType(type);
                throw ex;
             } else {
 
                // If the CAPI class was generated using a XINS release older
                // than 1.2.0, then it will not override the
                // 'createErrorCodeException' method and consequently the
                // method will return null. It cannot be determined here
                // whether the error code is acceptable or not
                String ver = _capi.getXINSVersion();
                if (ver.startsWith("0.")
                 || ver.startsWith("1.0.")
                 || ver.startsWith("1.1.")) {
                   throw new UnsuccessfulXINSCallException(
                      xinsRequest, target, duration, resultData, null);
 
                } else {
                   throw new UnacceptableErrorCodeXINSCallException(
                      xinsRequest, target, duration, resultData);
                }
             }
          }
       }
 
       // Call completely succeeded
       Log.log_2101(url, function, params, duration);
 
       return resultData;
    }
 
    /**
     * Constructs an appropriate <code>CallResult</code> object for a
     * successful call attempt. This method is called from
     * {@link #doCall(CallRequest,CallConfig)}.
     *
     * <p>The implementation of this method in class
     * {@link XINSServiceCaller} expects an {@link XINSCallRequest} and
     * returns an {@link XINSCallResult}.
     *
     * @param request
     *    the {@link CallRequest} that was to be executed, never
     *    <code>null</code> when called from {@link #doCall(CallRequest,CallConfig)};
     *    should be an instance of class {@link XINSCallRequest}.
     *
     * @param succeededTarget
     *    the {@link TargetDescriptor} for the service that was successfully
     *    called, never <code>null</code> when called from
     *    {@link #doCall(CallRequest,CallConfig)}.
     *
     * @param duration
     *    the call duration in milliseconds, must be a non-negative number.
     *
     * @param exceptions
     *    the list of {@link org.xins.common.service.CallException} instances,
     *    or <code>null</code> if there were no call failures.
     *
     * @param result
     *    the result from the call, which is the object returned by
     *    {@link #doCallImpl(CallRequest,CallConfig,TargetDescriptor)}, always an instance
     *    of class {@link XINSCallResult}, never <code>null</code>; .
     *
     * @return
     *    a {@link XINSCallResult} instance, never <code>null</code>.
     *
     * @throws ClassCastException
     *    if either <code>request</code> or <code>result</code> is not of the
     *    correct class.
     */
    protected CallResult createCallResult(CallRequest       request,
                                          TargetDescriptor  succeededTarget,
                                          long              duration,
                                          CallExceptionList exceptions,
                                          Object            result)
    throws ClassCastException {
 
       XINSCallResult r = new XINSCallResult((XINSCallRequest) request,
                                             succeededTarget,
                                             duration,
                                             exceptions,
                                             (XINSCallResultData) result);
 
       return r;
    }
 
    /**
     * Determines whether a call should fail-over to the next selected target
     * based on a request, call configuration and exception list.
     *
     * @param request
     *    the request for the call, as passed to {@link #doCall(CallRequest,CallConfig)},
     *    should not be <code>null</code>.
     *
     * @param callConfig
     *    the call config that is currently in use, never <code>null</code>.
     *
     * @param exceptions
     *    the current list of {@link CallException}s; never <code>null</code>.
     *
     * @return
     *    <code>true</code> if the call should fail-over to the next target, or
     *    <code>false</code> if it should not.
     */
    protected boolean shouldFailOver(CallRequest       request,
                                     CallConfig        callConfig,
                                     CallExceptionList exceptions) {
 
       // Get the most recent exception
       CallException exception = exceptions.last();
 
       boolean should;
 
       // Let the superclass look at this first.
       if (super.shouldFailOver(request, callConfig, exceptions)) {
          should = true;
 
       // Otherwise check if the request may fail-over from HTTP point-of-view
       //
       // XXX: Note that this duplicates code that is already in the
       //      HTTPServiceCaller. This may need to be refactored at some point.
       //      It has been decided to take this approach since the
       //      shouldFailOver method in class HTTPServiceCaller has protected
       //      access.
       //      An alternative solution that should be investigated is to
       //      subclass HTTPServiceCaller.
 
       // A non-2xx HTTP status code indicates the request was not handled
       } else if (exception instanceof StatusCodeHTTPCallException) {
          int code = ((StatusCodeHTTPCallException) exception).getStatusCode();
          should = (code < 200 || code > 299);
 
       // Some XINS error codes indicate the request was not accepted
       } else if (exception instanceof UnsuccessfulXINSCallException) {
          String s = ((UnsuccessfulXINSCallException) exception).getErrorCode();
          should = ("_InvalidRequest".equals(s)
                 || "_DisabledFunction".equals(s));
 
       // Otherwise do not fail over
       } else {
          should = false;
       }
 
       return should;
    }
 }
