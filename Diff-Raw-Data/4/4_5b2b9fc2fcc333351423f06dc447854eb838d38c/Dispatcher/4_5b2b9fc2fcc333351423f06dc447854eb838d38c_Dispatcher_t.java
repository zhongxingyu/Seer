 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 import org.acegisecurity.AcegiSecurityException;
 import org.eclipse.emf.ecore.EObject;
 import org.geoserver.ows.security.OperationInterceptor;
 import org.geoserver.ows.util.EncodingInfo;
 import org.geoserver.ows.util.KvpMap;
 import org.geoserver.ows.util.KvpUtils;
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.ows.util.XmlCharsetDetector;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.Service;
 import org.geoserver.platform.ServiceException;
 import org.geotools.util.Version;
 import org.geotools.xml.EMFUtils;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.xml.sax.SAXException;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 
 /**
  * Dispatches an http request to an open web service (OWS).
  * <p>
  * An OWS request contains three bits of information:
  *         <ol>
  *                 <li>The service being called
  *                 <li>The operation of the service to execute
  *                 <li>The version of the service ( optional )
  *  </ol>
  *  Additional, an OWS request can contain an arbitray number of additional
  *  parameters.
  * </p>
  * <p>
  * An OWS request can be specified in two forms. The first form is known as "KVP"
  * in which all the parameters come in the form of a set of key-value pairs.
  * Commonly this type of request is made in an http "GET" request, the parameters
  * being specified in the query string:
  *
  *  <pre>
  *          <code>http://www.xyz.com/geoserver?service=someService&request=someRequest&version=X.Y.Z&param1=...&param2=...
  *  </pre>
  *
  *  This type of request can also be made in a "POST" request in with a
  *  mime-type of "application/x-www-form-urlencoded".
  * </p>
  * <p>
  * The second form is known as "XML" in which all the parameters come in the
  * form of an xml document. This type of request is made in an http "POST"
  * request.
  *
  *         <pre>
  *                 <code>
  *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;
  *  &lt;SomeRequest service="someService" version="X.Y.Z"&gt;
  *    &lt;Param1&gt;...&lt;/Param1&gt;
  *    &lt;Param2&gt;...&lt;/Param2&gt;
  *    ...
  *  &lt;/SomeRequest&gt;
  *                 </code>
  *         </pre>
  * </p>
  * <p>
  * When a request is received, the <b>service</b> the <b>version</b> parameters
  * are used to locate a service desciptor, an instance of {@link Service}. With
  * the service descriptor, the <b>request</b> parameter is used to locate the
  * operation of the service to call.
  * </p>
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class Dispatcher extends AbstractController {
     /**
      * Logging instance
      */
     static Logger logger = org.geotools.util.logging.Logging.getLogger("org.geoserver.ows");
 
     /** flag to control wether the dispatcher is cite compliant */
     boolean citeCompliant = false;
 
     /** thread local variable for the request */
     public static final ThreadLocal<Request> REQUEST = new ThreadLocal<Request>();
     
     static final Charset UTF8 = Charset.forName("UTF-8");
     
     /**
      * The amount of bytes to be read to determine the proper xml reader in POST request
      */
     int XML_LOOKAHEAD = 8192;
 
     /**
      * list of callbacks 
      */
     List<DispatcherCallback> callbacks = Collections.EMPTY_LIST;
     
     /**
      * Sets the flag to control wether the dispatcher is cite compliante.
      * <p>
      * If set to <code>true</code>, the dispatcher with throw exceptions when
      * it encounters something that is not 100% compliant with CITE standards.
      * An example would be a request which specifies the servce in the context
      * path: '.../geoserver/wfs?request=...' and not with the kvp '&service=wfs'.
      * </p>
      *
      * @param citeCompliant <code>true</code> to set compliance,
      *         <code>false</code> to unset it.
      */
     public void setCiteCompliant(boolean citeCompliant) {
         this.citeCompliant = citeCompliant;
     }
 
     public boolean isCiteCompliant() {
         return citeCompliant;
     }
     
     @Override
     protected void initApplicationContext(ApplicationContext context) {
         //load life cycle callbacks
         callbacks = GeoServerExtensions.extensions( DispatcherCallback.class, context);
         
         // setup the xml lookahead value
         String lookahead = GeoServerExtensions.getProperty("XML_LOOKAHEAD", context);
         if(lookahead != null) {
             try {
                 int lookaheadValue = Integer.valueOf(lookahead);
                 if(lookaheadValue <= 0)
                     logger.log(Level.SEVERE, "Invalid XML_LOOKAHEAD value, " +
                             "will use " + XML_LOOKAHEAD + " instead");
                 XML_LOOKAHEAD = lookaheadValue;
             } catch(Exception e) {
                 logger.log(Level.SEVERE, "Invalid XML_LOOKAHEAD value, " +
                         "will use " + XML_LOOKAHEAD + " instead");
             }
         }
     }
     
     protected void preprocessRequest(HttpServletRequest request)
         throws Exception {
         //set the charset
         Charset charSet = null;
 
        //TODO: make this server settable
         charSet = UTF8;
         if(request.getCharacterEncoding() != null)
 	        try {
 	            charSet = Charset.forName(request.getCharacterEncoding());
 	        } catch (Exception e) {
 	            // ok, we tried...
 	        }
 
         request.setCharacterEncoding(charSet.name());
     }
 
     protected ModelAndView handleRequestInternal(HttpServletRequest httpRequest,
         HttpServletResponse httpResponse) throws Exception {
         preprocessRequest(httpRequest);
 
         //create a new request instance
         Request request = new Request();
 
         //set request / response
         request.httpRequest = httpRequest;
         request.httpResponse = httpResponse;
 
         Service service = null;
 
         try {
             REQUEST.set(request);
             
             //initialize the request
             init(request);
             
             //find the service
             try {
                 service = service(request);
             } catch (Throwable t) {
                 exception(t, null, request);
 
                 return null;
             }
             
             //throw any outstanding errors
             if (request.error != null) {
                 throw request.error;
             }
 
             //dispatch the operation
             Operation operation = dispatch(request, service);
 
             //execute it
             Object result = execute(request, operation);
 
             //write the response
             if (result != null) {
                 response(result, request, operation);
             }
         } catch (AcegiSecurityException e) {
             // make Acegi exceptions flow so that exception transformer filter can handle them
             throw e;
         } catch (Throwable t) {
             exception(t, service, request);
         } finally {
            fireFinishedCallback(request);
             REQUEST.remove();
         }
 
         return null;
     }
 
     void fireFinishedCallback(Request req) {
         for ( DispatcherCallback cb : callbacks ) {
             cb.finished( req );
         }
     }
     
     Request init(Request request) throws ServiceException, IOException {
         HttpServletRequest httpRequest = request.httpRequest;
 
         //figure out method
         request.get = "GET".equalsIgnoreCase(httpRequest.getMethod())
             || "application/x-www-form-urlencoded".equals(httpRequest.getContentType());
 
         //create the kvp map
         parseKVP(request);
         
         if ( !request.get ) { // && httpRequest.getInputStream().available() > 0) {
             //wrap the input stream in a buffered input stream
             request.input = reader(httpRequest);
 
             //mark the input stream
             char[] req = new char[1024];
             request.input.mark(XML_LOOKAHEAD);
             int read = request.input.read(req, 0, 1024);
             
             if (logger.isLoggable(Level.FINE)) {
                 if (read == -1) {
                     request.input = null;
                 } else if (read < 1024) {
                     logger.fine("Raw XML request starts with: " + new String(req));
                 } else {
                     logger.fine("Raw XML request starts with: " + new String(req) + "...");
                 }
             }
             if (read == -1)
                 request.input = null;
             else
                 request.input.reset();
         }
 
         return fireInitCallback(request);
     }
 
     Request fireInitCallback(Request req) {
         for ( DispatcherCallback cb : callbacks ) {
             Request r = cb.init( req );
             req = r != null ? r : req;
         }
         return req;
     }
     
     BufferedReader reader(HttpServletRequest httpRequest)
         throws IOException {
         //create a buffer so we can reset the input stream
         BufferedInputStream input = new BufferedInputStream(httpRequest.getInputStream());
         input.mark(XML_LOOKAHEAD);
 
         //create object to hold encoding info
         EncodingInfo encoding = new EncodingInfo();
 
         //call this method to set the encoding info
         XmlCharsetDetector.getCharsetAwareReader(input, encoding);
 
         //call this method to create the reader
         Reader reader = XmlCharsetDetector.createReader(input, encoding);
 
         //rest the input
         input.reset();
 
         //ensure the reader is a buffered reader
         if (reader instanceof BufferedReader) {
             return (BufferedReader) reader;
         }
 
         return new BufferedReader(reader);
     }
 
     Service service(Request req) throws Exception {
         //check kvp
         if (req.kvp != null) {
 
             req.service = normalize((String) req.kvp.get("service"));
             req.version = normalize((String) req.kvp.get("version"));
             req.request = normalize((String) req.kvp.get("request"));
             req.outputFormat = normalize((String) req.kvp.get("outputFormat"));
         } 
         //check the body
         if (req.input != null) {
             Map xml = readOpPost(req.input);
             if (req.service == null) {
                 req.service = normalize((String) xml.get("service"));    
             }
             if (req.version == null) {
                 req.version = normalize((String) xml.get("version"));    
             }
             if (req.request == null) {
                 req.request = normalize((String) xml.get("request"));    
             }
             if (req.outputFormat == null) {
                 req.outputFormat = normalize((String) xml.get("outputFormat"));    
             }
         }
 
         //try to infer from context
         //JD: for cite compliance, a service *must* be specified explicitley by 
         // either a kvp, or an xml attribute, however in reality the context 
         // is often a good way to infer the service or request 
         String service = req.service;
 
         if ((service == null) || (req.request == null)) {
             Map map = readOpContext(req.httpRequest);
 
             if (service == null) {
                 service = normalize((String) map.get("service"));
 
                 if ((service != null) && !citeCompliant) {
                     req.service = service;
                 }
             }
 
             if (req.request == null) {
                 req.request = normalize((String) map.get("request"));
             }
         }
 
         if (service == null) {
             //give up 
             throw new ServiceException("Could not determine service", "MissingParameterValue",
                 "service");
         }
 
         //load from teh context
         Service serviceDescriptor = findService(service, req.version);
         return fireServiceDispatchedCallback(req,serviceDescriptor);
     }
     
     Service fireServiceDispatchedCallback(Request req, Service service ) {
         for ( DispatcherCallback cb : callbacks ) {
             Service s = cb.serviceDispatched( req, service );
             service = s != null ? s : service;
         }
         return service;
     }
 
     String normalize(String value) {
         if (value == null) {
             return null;
         }
 
         if ("".equals(value.trim())) {
             return null;
         }
 
         return value.trim();
     }
 
     Operation dispatch(Request req, Service serviceDescriptor)
         throws Throwable {
         if (req.request == null) {
             String msg = "Could not determine geoserver request from http request " + req.httpRequest;
             throw new ServiceException(msg, "MissingParameterValue", "request");
         }
 
         // ensure the requested operation exists
         boolean exists = false;
         for ( String op : serviceDescriptor.getOperations() ) {
             if ( op.equalsIgnoreCase( req.request ) ) {
                 exists = true;
                 break;
             }
         }
 
         // lookup the operation, initial lookup based on (service,request)
         Object serviceBean = serviceDescriptor.getService();
         Method operation = OwsUtils.method(serviceBean.getClass(), req.request);
 
         if (operation == null || !exists) {
             String msg = "No such operation " + req;
             throw new ServiceException(msg, "OperationNotSupported", req.request);
         }
 
         //step 4: setup the paramters
         Object[] parameters = new Object[operation.getParameterTypes().length];
 
         for (int i = 0; i < parameters.length; i++) {
             Class parameterType = operation.getParameterTypes()[i];
 
             //first check for servlet request and response
             if (parameterType.isAssignableFrom(HttpServletRequest.class)) {
                 parameters[i] = req.httpRequest;
             } else if (parameterType.isAssignableFrom(HttpServletResponse.class)) {
                 parameters[i] = req.httpResponse;
             }
             //next check for input and output
             else if (parameterType.isAssignableFrom(InputStream.class)) {
                 parameters[i] = req.httpRequest.getInputStream();
             } else if (parameterType.isAssignableFrom(OutputStream.class)) {
                 parameters[i] = req.httpResponse.getOutputStream();
             } else {
                 //check for a request object
                 Object requestBean = null;
                 
                 //track an exception 
                 Throwable t = null;
 
                 if (req.kvp != null && req.kvp.size() > 0) {
                     //use the kvp reader mechanism
                     try {
                         requestBean = parseRequestKVP(parameterType, req);
                     } 
                     catch (Exception e) {
                         //dont die now, there might be a body to parse
                         t = e;
                     }
                 }
                 if (req.input != null) {
                     //use the xml reader mechanism
                     requestBean = parseRequestXML(requestBean,req.input, req);
                 }
                 
                 //if no reader found for the request, throw exception
                 //TODO: we may wish to make this configurable, as perhaps there
                 // might be cases when the service prefers that null be passed in?
                 if ( requestBean == null ) {
                     //unable to parse request object, throw exception if we 
                     // caught one
                     if ( t != null ) {
                         throw t;
                     }
                     throw new ServiceException( "Could not find request reader (either kvp or xml) for: " + parameterType.getName() );
                 }
                 
                 // GEOS-934  and GEOS-1288
                 Method setBaseUrl = OwsUtils.setter(requestBean.getClass(), "baseUrl", String.class);
                 if (setBaseUrl != null) {
                     setBaseUrl.invoke(requestBean, new String[] { RequestUtils.baseURL(req.httpRequest)});
                 }
 
                 // another couple of thos of those lovley cite things, version+service has to specified for 
                 // non capabilities request, so if we dont have either thus far, check the request
                 // objects to try and find one
                 // TODO: should make this configurable
                 if (requestBean != null) {
                     //if we dont have a version thus far, check the request object
                     if (req.service == null) {
                         req.service = lookupRequestBeanProperty(requestBean, "service", false);
                     }
 
                     if (req.version == null) {
                         req.version = lookupRequestBeanProperty(requestBean, "version", false);
                     }
 
                     if (req.outputFormat == null) {
                         req.outputFormat = lookupRequestBeanProperty(requestBean, "outputFormat",
                                 true);
                     }
 
                     parameters[i] = requestBean;
                 }
             }
         }
 
         //if we are in cite compliant mode, do some additional checks to make
         // sure the "mandatory" parameters are specified, even though we 
         // succesfully dispatched the request.
         if (citeCompliant) {
             if (!"GetCapabilities".equalsIgnoreCase(req.request)) {
                 if (req.version == null) {
                     //must be a version on non-capabilities requests
                     throw new ServiceException("Could not determine version",
                         "MissingParameterValue", "version");
                 } else {
                     //version must be valid
                     if (!req.version.matches("[0-99].[0-99].[0-99]")) {
                         throw new ServiceException("Invalid version: " + req.version,
                             "InvalidParameterValue", "version");
                     }
 
                     //make sure the versoin actually exists
                     boolean found = false;
                     Version version = new Version(req.version);
 
                     for (Iterator s = loadServices().iterator(); s.hasNext();) {
                         Service service = (Service) s.next();
 
                         if (version.equals(service.getVersion())) {
                             found = true;
 
                             break;
                         }
                     }
 
                     if (!found) {
                         throw new ServiceException("Invalid version: " + req.version,
                             "InvalidParameterValue", "version");
                     }
                 }
 
                 if (req.service == null) {
                     //give up 
                     throw new ServiceException("Could not determine service",
                         "MissingParameterValue", "service");
                 }
             }
         }
 
         Operation op = new Operation(req.request, serviceDescriptor, operation, parameters);
         return fireOperationDispatchedCallback(req,op);
     }
 
     Operation fireOperationDispatchedCallback(Request req, Operation op ) {
         for ( DispatcherCallback cb : callbacks ) {
             Operation o = cb.operationDispatched( req, op );
             op = o != null ? o : op;
         }
         return op;
     }
     
     String lookupRequestBeanProperty(Object requestBean, String property, boolean allowDefaultValues) {
         if (requestBean instanceof EObject && EMFUtils.has((EObject) requestBean, property)) {
             //special case hack for eObject, we should move 
             // this out into an extension ppint
             EObject eObject = (EObject) requestBean;
 
             if (allowDefaultValues || EMFUtils.isSet(eObject, property)) {
                 return normalize((String) EMFUtils.get(eObject, property));
             }
         } else {
             //straight reflection
             String version = (String) OwsUtils.property(requestBean, property, String.class);
 
             if (version != null) {
                 return normalize(version);
             }
         }
 
         return null;
     }
 
     Object execute(Request req, Operation opDescriptor)
         throws Throwable {
         Service serviceDescriptor = opDescriptor.getService();
         Object serviceBean = serviceDescriptor.getService();
         Method operation = opDescriptor.getMethod();
         Object[] parameters = opDescriptor.getParameters();
 
         //step 5: execute
         Object result = null;
 
         try {
             result = operation.invoke(serviceBean, parameters);
          } catch (InvocationTargetException e) {
             if (e.getTargetException() != null) {
                 throw e.getTargetException();
             }
         }
 
         return fireOperationExecutedCallback(req, opDescriptor, result);
     }
 
     Object fireOperationExecutedCallback(Request req, Operation op, Object result ) {
         for ( DispatcherCallback cb : callbacks ) {
             Object r = cb.operationExecuted( req, op, result );
             result = r != null ? r : result;
         }
         return result;
     }
     
     void response(Object result, Request req, Operation opDescriptor)
         throws Throwable {
         //step 6: write response
         if (result != null) {
             //look up respones
             List responses = GeoServerExtensions.extensions(Response.class);
 
             //first filter by binding, and canHandle
          O: for (Iterator itr = responses.iterator(); itr.hasNext();) {
                 Response response = (Response) itr.next();
 
                 Class binding = response.getBinding();
 
                 if (!binding.isAssignableFrom(result.getClass())
                         || !response.canHandle(opDescriptor)) {
                     itr.remove();
 
                     continue;
                 }
 
                 //filter by output format
                 Set outputFormats = response.getOutputFormats();
 
                 if ((req.outputFormat != null) && (!outputFormats.isEmpty())
                         && !outputFormats.contains(req.outputFormat)) {
                     
                     //must do a case insensitive check
                     for ( Iterator of = outputFormats.iterator(); of.hasNext(); ) {
                         String outputFormat = (String) of.next();
                         if( req.outputFormat.equalsIgnoreCase( outputFormat ) ) {
                             continue O;
                         }
                     }
                     
                     itr.remove();
                 }
             }
 
             if (responses.isEmpty()) {
                 String msg = "No response: ( object = " + result.getClass();
 
                 if (req.outputFormat != null) {
                     msg += (", outputFormat = " + req.outputFormat);
                 }
 
                 msg += " )";
 
                 throw new RuntimeException(msg);
             }
 
             if (responses.size() > 1) {
                 //sort by class hierarchy
                 Collections.sort(responses,
                     new Comparator() {
                         public int compare(Object o1, Object o2) {
                             Class c1 = ((Response) o1).getBinding();
                             Class c2 = ((Response) o2).getBinding();
 
                             if (c1.equals(c2)) {
                                 return 0;
                             }
 
                             if (c1.isAssignableFrom(c2)) {
                                 return 1;
                             }
 
                             if (c2.isAssignableFrom(c1)) {
                                 ;
                             }
 
                             return -1;
                         }
                     });
 
                 //check first two and make sure bindings are not equal
                 Response r1 = (Response) responses.get(0);
                 Response r2 = (Response) responses.get(1);
 
                 if (r1.getBinding().equals(r2.getBinding())) {
                     String msg = "Multiple responses: (" + result.getClass() + ")";
                     throw new RuntimeException(msg);
                 }
             }
 
             Response response = (Response) responses.get(0);
             response = fireResponseDispatchedCallback(req,opDescriptor,result,response);
 
             //load the output strategy to be used
             ServiceStrategy outputStrategy = findOutputStrategy(req.httpResponse);
 
             if (outputStrategy == null) {
                 outputStrategy = new DefaultOutputStrategy();
             }
 
             //set the mime type
             req.httpResponse.setContentType(response.getMimeType(result, opDescriptor));
 
             //set any extra headers, other than the mime-type
             if (response.getHeaders(result, opDescriptor) != null) {
                 String[][] headers = response.getHeaders(result, opDescriptor);
 
                 for (int i = 0; i < headers.length; i++) {
                     req.httpResponse.addHeader(headers[i][0], headers[i][1]);
                 }
             }
             
             //TODO: initialize any header params (gzip,deflate,etc...)
             OutputStream output = outputStrategy.getDestination(req.httpResponse);
             response.write(result, output, opDescriptor);
 
             outputStrategy.flush(req.httpResponse);
 
             //flush the underlying out stream for good meaure
             req.httpResponse.getOutputStream().flush();
         }
     }
 
     Response fireResponseDispatchedCallback(Request req, Operation op, Object result, Response response ) {
         for ( DispatcherCallback cb : callbacks ) {
             Response r = cb.responseDispatched(req, op, result, response);
             response = r != null ? r : response;
         }
         return response;
     }
     
     Collection loadServices() {
         Collection services = GeoServerExtensions.extensions(Service.class);
 
         if (!(new HashSet(services).size() == services.size())) {
             String msg = "Two identical service descriptors found";
             throw new IllegalStateException(msg);
         }
 
         return services;
     }
 
     Service findService(String id, String ver) throws ServiceException {
         Version version = (ver != null) ? new Version(ver) : null;
         Collection services = loadServices();
 
         //first just match on service,request
         List matches = new ArrayList();
 
         for (Iterator itr = services.iterator(); itr.hasNext();) {
             Service sBean = (Service) itr.next();
 
             if (sBean.getId().equalsIgnoreCase(id)) {
                 matches.add(sBean);
             }
         }
 
         if (matches.isEmpty()) {
             String msg = "No service: ( " + id + " )";
             throw new ServiceException(msg, "InvalidParameterValue", "service");
         }
 
         Service sBean = null;
 
         //if multiple, use version to filter match
         if (matches.size() > 1) {
             List vmatches = new ArrayList(matches);
 
             //match up the version
             if (version != null) {
                 //version specified, look for a match
                 for (Iterator itr = vmatches.iterator(); itr.hasNext();) {
                     Service s = (Service) itr.next();
 
                     if (version.equals(s.getVersion())) {
                         continue;
                     }
 
                     itr.remove();
                 }
 
                 if (vmatches.isEmpty()) {
                     //no matching version found, drop out and next step 
                     // will sort to return highest version
                     vmatches = new ArrayList(matches);
                 }
             }
 
             //multiple services found, sort by version
             if (vmatches.size() > 1) {
                 //use highest version
                 Comparator comparator = new Comparator() {
                         public int compare(Object o1, Object o2) {
                             Service s1 = (Service) o1;
                             Service s2 = (Service) o2;
 
                             return s1.getVersion().compareTo(s2.getVersion());
                         }
                     };
 
                 Collections.sort(vmatches, comparator);
             }
 
             sBean = (Service) vmatches.get(vmatches.size() - 1);
         } else {
             //only a single match, that was easy
             sBean = (Service) matches.get(0);
         }
 
         return sBean;
     }
 
     Collection loadKvpRequestReaders() {
         Collection kvpReaders = GeoServerExtensions.extensions(KvpRequestReader.class);
 
         if (!(new HashSet(kvpReaders).size() == kvpReaders.size())) {
             String msg = "Two identical kvp readers found";
             throw new IllegalStateException(msg);
         }
 
         return kvpReaders;
     }
 
     KvpRequestReader findKvpRequestReader(Class type) {
         Collection kvpReaders = loadKvpRequestReaders();
 
         List matches = new ArrayList();
 
         for (Iterator itr = kvpReaders.iterator(); itr.hasNext();) {
             KvpRequestReader kvpReader = (KvpRequestReader) itr.next();
 
             if (kvpReader.getRequestBean().isAssignableFrom(type)) {
                 matches.add(kvpReader);
             }
         }
 
         if (matches.isEmpty()) {
             return null;
         }
 
         if (matches.size() > 1) {
             //sort by class hierarchy
             Comparator comparator = new Comparator() {
                     public int compare(Object o1, Object o2) {
                         KvpRequestReader kvp1 = (KvpRequestReader) o1;
                         KvpRequestReader kvp2 = (KvpRequestReader) o2;
 
                         if (kvp2.getRequestBean().isAssignableFrom(kvp1.getRequestBean())) {
                             return -1;
                         }
 
                         return 1;
                     }
                 };
 
             Collections.sort(matches, comparator);
         }
 
         return (KvpRequestReader) matches.get(0);
     }
 
     Collection loadXmlReaders() {
         List<XmlRequestReader> xmlReaders = GeoServerExtensions.extensions(XmlRequestReader.class);
 
         if (!(new HashSet<XmlRequestReader>(xmlReaders).size() == xmlReaders.size())) {
         
             String msg = "Two identical xml readers found";
             for (int i = 0; i < xmlReaders.size(); i++) {
                 XmlRequestReader r1 = xmlReaders.get(i);
                 for (int j = i + 1; j < xmlReaders.size(); j++) {
                     XmlRequestReader r2 = xmlReaders.get(j);
                     if(r1.equals(r2)) {
                         msg += ": " + r1 + " and " + r2;
                         break;
                     }
                 }
                 
             }
             
             throw new IllegalStateException(msg);
         }
 
         return xmlReaders;
     }
 
     XmlRequestReader findXmlReader(String namespace, String element, String serviceId, String ver) {
         Collection xmlReaders = loadXmlReaders();
 
         //first just match on namespace, element
         List matches = new ArrayList();
 
         for (Iterator itr = xmlReaders.iterator(); itr.hasNext();) {
             XmlRequestReader xmlReader = (XmlRequestReader) itr.next();
             QName xmlElement = xmlReader.getElement();
 
             if (xmlElement.getLocalPart().equalsIgnoreCase(element)) {
                 if (xmlElement.getNamespaceURI().equalsIgnoreCase(namespace)) {
                     matches.add(xmlReader);
                 }
             }
         }
 
         if (matches.isEmpty()) {
             //do a more lax serach, search only on the element name if the 
             // namespace was unspecified
             if ( namespace == null || namespace.equals( "" ) ) {
                 String msg = "No namespace specified in request, searching for "
                     + " xml reader by element name only";
                 logger.info( msg );
                 
                 for ( Iterator itr = xmlReaders.iterator(); itr.hasNext(); ) {
                     XmlRequestReader xmlReader = (XmlRequestReader) itr.next();
                     if ( xmlReader.getElement().getLocalPart().equals( element ) ) {
                         matches.add( xmlReader );
                     }
                 }
                 
                 if ( !matches.isEmpty() ) {
                     //we found some matches, make sure they are all in the 
                     // same namespace
                     Iterator itr = matches.iterator();
                     XmlRequestReader first = (XmlRequestReader) itr.next();
                     while( itr.hasNext() ) {
                         XmlRequestReader xmlReader = (XmlRequestReader ) itr.next();
                         if ( !first.getElement().equals( xmlReader.getElement() ) ) {
                             //abort
                             matches.clear();
                             break;
                         }
                     }
                 }
             }
         }
         
         if ( matches.isEmpty() ) {
             String msg = "No xml reader: (" + namespace + "," + element + ")";
             logger.info(msg);
             return null;
         }
 
         XmlRequestReader xmlReader = null;
 
         //if multiple, use version to filter match
         if (matches.size() > 1) {
             List vmatches = new ArrayList(matches);
             
             // match up the service
             if(serviceId != null) {
                 for (Iterator itr = vmatches.iterator(); itr.hasNext();) {
                     XmlRequestReader r = (XmlRequestReader) itr.next();
 
                     if (r.getServiceId() == null || serviceId.equalsIgnoreCase(r.getServiceId())) {
                         continue;
                     }
 
                     itr.remove();
                 }
                 
                 // if no reader matching the service is found, we should
                 // not return a reader, as service is key to identify the reader
                 // we cannot just assume a meaningful default
             }
 
             // match up the version
             if (ver != null) {
                 Version version = new Version(ver);
 
                 // version specified, look for a match (and allow version
                 // generic ones to live by)
                 for (Iterator itr = vmatches.iterator(); itr.hasNext();) {
                     XmlRequestReader r = (XmlRequestReader) itr.next();
 
                     if (r.getVersion() == null || version.equals(r.getVersion())) {
                         continue;
                     }
 
                     itr.remove();
                 }
 
                 if (vmatches.isEmpty()) {
                     // no matching version found, drop out and next step 
                     // will sort to return highest version
                     vmatches = new ArrayList(matches);
                 }
             }
 
             //multiple readers found, sort by version and by service match
             if (vmatches.size() > 1) {
                 //use highest version
                 Comparator comparator = new Comparator() {
                         public int compare(Object o1, Object o2) {
                             XmlRequestReader r1 = (XmlRequestReader) o1;
                             XmlRequestReader r2 = (XmlRequestReader) o2;
 
                             Version v1 = r1.getVersion();
                             Version v2 = r2.getVersion();
 
                             if ((v1 == null) && (v2 == null)) {
                                 return 0;
                             }
 
                             if ((v1 != null) && (v2 == null)) {
                                 return 1;
                             }
 
                             if ((v1 == null) && (v2 != null)) {
                                 return -1;
                             }
 
                             int versionCompare = v1.compareTo(v2);
 
                             if (versionCompare != 0) {
                                 return versionCompare;
                             }
 
                             String sid1 = r1.getServiceId();
                             String sid2 = r2.getServiceId();
 
                             if ((sid1 == null) && (sid2 == null)) {
                                 return 0;
                             }
 
                             if ((sid1 != null) && (sid2 == null)) {
                                 return 1;
                             }
 
                             if ((sid1 == null) && (sid2 != null)) {
                                 return -1;
                             }
 
                             return sid1.compareTo(sid2);
                         }
                     };
 
                 Collections.sort(vmatches, comparator);
             }
 
             if(vmatches.size() > 0 )
                 xmlReader = (XmlRequestReader) vmatches.get(vmatches.size() - 1);
         } else {
             //only a single match, that was easy
             xmlReader = (XmlRequestReader) matches.get(0);
         }
 
         return xmlReader;
     }
 
     ServiceStrategy findOutputStrategy(HttpServletResponse response) {
         OutputStrategyFactory factory = null;
         try {
             factory = (OutputStrategyFactory) GeoServerExtensions.bean("serviceStrategyFactory");
         } catch(NoSuchBeanDefinitionException e) {
             return null;
         }
         return factory.createOutputStrategy(response);
     }
 
     BufferedInputStream input(File cache) throws IOException {
         return (cache == null) ? null : new BufferedInputStream(new FileInputStream(cache));
     }
 
     void preParseKVP(Request req) throws ServiceException {
         HttpServletRequest request = req.httpRequest;
 
         //unparsed kvp set
         Map kvp = request.getParameterMap();
 
         if (kvp == null || kvp.isEmpty()) {
             req.kvp = Collections.EMPTY_MAP;
             //req.kvp = null;
             return;
         }
 
         //track parsed kvp and unparsd
         Map parsedKvp = KvpUtils.normalize(kvp);
         Map rawKvp = new KvpMap( parsedKvp );
         
         req.kvp = parsedKvp;
         req.rawKvp = rawKvp;
     }
     
     void parseKVP(Request req) throws ServiceException {
         
         preParseKVP( req );
         List<Throwable> errors = KvpUtils.parse( req.kvp );
         if ( !errors.isEmpty() ) {
             req.error = errors.get(0);
         }
     }
 
     Object parseRequestKVP(Class type, Request request)
         throws Exception {
         KvpRequestReader kvpReader = findKvpRequestReader(type);
 
         if (kvpReader != null) {
             //check for http request awareness
             if (kvpReader instanceof HttpServletRequestAware) {
                 ((HttpServletRequestAware) kvpReader).setHttpRequest(request.httpRequest);
             }
 
             Object requestBean = kvpReader.createRequest();
 
             if (requestBean != null) {
                 requestBean = kvpReader.read(requestBean, request.kvp, request.rawKvp);
             }
 
             return requestBean;
         }
 
         return null;
     }
 
     Object parseRequestXML(Object requestBean, BufferedReader input, Request request)
         throws Exception {
         //check for an empty input stream
         //if (input.available() == 0) {
         if (!input.ready()) {
             return null;
         }
 
         //create stream parser
         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setValidating(false);
 
         //parse root element
         XmlPullParser parser = factory.newPullParser();
         //parser.setInput(input, "UTF-8");
         parser.setInput(input);
         parser.nextTag();
 
         String namespace = (parser.getNamespace() != null) ? parser.getNamespace() : "";
         String element = parser.getName();
         String version = null;
         String service = null;
 
         for (int i = 0; i < parser.getAttributeCount(); i++) {
             if ("version".equals(parser.getAttributeName(i))) {
                 version = parser.getAttributeValue(i);
             }
             if ("service".equals(parser.getAttributeName(i))) {
                 service = parser.getAttributeValue(i);
             }
         }
         
         
 
         parser.setInput(null);
 
         //reset input stream
         input.reset();
 
         XmlRequestReader xmlReader = findXmlReader(namespace, element, service, version);
         if (xmlReader == null ) {
             //no xml reader, just return object passed in
             return requestBean;
         }
 
         if (xmlReader instanceof HttpServletRequestAware) {
             ((HttpServletRequestAware) xmlReader).setHttpRequest(request.httpRequest);
         }
 
         //return xmlReader.read(input);
         return xmlReader.read( requestBean, input, request.kvp );
     }
 
     Map readOpContext(HttpServletRequest request) {
         //try to get from request url
         String ctxPath = request.getContextPath();
         String reqPath = request.getRequestURI();
         reqPath = reqPath.substring(ctxPath.length());
 
         if (reqPath.startsWith("/")) {
             reqPath = reqPath.substring(1, reqPath.length());
         }
 
         if (reqPath.endsWith("/")) {
             reqPath = reqPath.substring(0, reqPath.length() - 1);
         }
 
         Map map = new HashMap();
         int index = reqPath.indexOf('/');
 
         if (index != -1) {
             map.put("service", reqPath.substring(0, index));
             map.put("request", reqPath.substring(index + 1));
         } else {
             map.put("service", reqPath);
         }
 
         return map;
     }
 
     Map readOpPost(BufferedReader input) throws Exception {
         //create stream parser
         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setValidating(false);
 
         //parse root element
         XmlPullParser parser = factory.newPullParser();
         parser.setInput(input);
         parser.nextTag();
 
         Map map = new HashMap();
         map.put("request", parser.getName());
 
         for (int i = 0; i < parser.getAttributeCount(); i++) {
             String attName = parser.getAttributeName(i);
 
             if ("service".equals(attName)) {
                 map.put("service", parser.getAttributeValue(i));
             }
 
             if ("version".equals(parser.getAttributeName(i))) {
                 map.put("version", parser.getAttributeValue(i));
             }
 
             if ("outputFormat".equals(attName)) {
                 map.put("outputFormat", parser.getAttributeValue(i));
             }
         }
 
         //close parser + release resources
         parser.setInput(null);
 
         //reset the input stream
         input.reset();
 
         return map;
     }
 
     void exception(Throwable t, Service service, Request request) {
         Throwable current = t;
         while (current != null && !(current instanceof ClientStreamAbortedException) && !(current instanceof AcegiSecurityException)) {
             if(current instanceof SAXException)
                 current = ((SAXException) current).getException();
             else
                 current = current.getCause();
         }
         if (current instanceof ClientStreamAbortedException) {
             logger.log(Level.FINER, "Client has closed stream", t);
             return;
         }
         if ( current instanceof AcegiSecurityException)
             throw (AcegiSecurityException) current;
         
         
         //unwind the exception stack until we find one we know about 
         Throwable cause = t;
         while( cause != null ) {
             if ( cause instanceof ServiceException ) {
                 break;
             }
             if ( cause instanceof HttpErrorCodeException ) {
                 break;
             }
             if ( cause instanceof AcegiSecurityException ) {
                 break;
             }
             
             cause = cause.getCause();
         }
         
         if ( cause == null ) {
             //did not fine a "special" exception, create a service exception
             // by default
             cause = new ServiceException(t);
         }
         
         if (!(cause instanceof HttpErrorCodeException)) {
             logger.log(Level.SEVERE, "", t);
         } else {
             int errorCode = ((HttpErrorCodeException)cause).getErrorCode();
             if (errorCode < 199 || errorCode > 299) {
                 logger.log(Level.FINE, "", t);
             }
             else{
                 logger.log(Level.FINER, "", t);
             }
         }
 
         
         if ( cause instanceof ServiceException ) {
             ServiceException se = (ServiceException) cause;
             if ( cause != t ) {
                 //copy the message, code + locator, but set cause equal to root
                 se = new ServiceException( se.getMessage(), t, se.getCode(), se.getLocator() ); 
             }
             
             handleServiceException(se,service,request);
         }
         else if ( cause instanceof HttpErrorCodeException ) {
             //TODO: log the exception stack trace
             
             //set the error code
             HttpErrorCodeException ece = (HttpErrorCodeException) cause;
             try {
             	if(ece.getMessage() != null) {
                 	request.httpResponse.sendError(ece.getErrorCode(),ece.getMessage());
             	} else {
             		request.httpResponse.sendError(ece.getErrorCode());
             	}
             } 
             catch (IOException e) {
                 //means the resposne was already commited
                 //TODO: something
             }
         }
     }
 
     void handleServiceException( ServiceException se, Service service, Request request ) {
         //find an exception handler
         ServiceExceptionHandler handler = null;
 
         if (service != null) {
             //look up the service exception handler
             Collection handlers = GeoServerExtensions.extensions(ServiceExceptionHandler.class);
             for (Iterator h = handlers.iterator(); h.hasNext();) {
                 ServiceExceptionHandler seh = (ServiceExceptionHandler) h.next();
 
                 if (seh.getServices().contains(service)) {
                     //found one,
                     handler = seh;
 
                     break;
                 }
             }
         }
 
         if (handler == null) {
             //none found, fall back on default
             handler = new DefaultServiceExceptionHandler();
         }
 
         handler.handleServiceException(se, request);
     }
 
     
 }
