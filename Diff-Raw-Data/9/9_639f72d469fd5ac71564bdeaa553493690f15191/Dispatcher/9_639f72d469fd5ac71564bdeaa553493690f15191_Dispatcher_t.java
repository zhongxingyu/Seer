 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows;
 
 import org.eclipse.emf.ecore.EObject;
 import org.geoserver.ows.util.EncodingInfo;
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.ows.util.XmlCharsetDetector;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.Service;
 import org.geoserver.platform.ServiceException;
 import org.geotools.util.Version;
 import org.geotools.xml.EMFUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
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
 import java.net.MalformedURLException;
 import java.net.URL;
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
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 
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
     static Logger logger = Logger.getLogger("org.geoserver.ows");
 
     /** flag to control wether the dispatcher is cite compliant */
     boolean citeCompliant = false;
 
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
 
     protected void preprocessRequest(HttpServletRequest request)
         throws Exception {
         //initialize all OWS instances
         Collection services = getApplicationContext().getBeansOfType(OWS.class).values();
 
         for (Iterator s = services.iterator(); s.hasNext();) {
             OWS ows = (OWS) s.next();
 
             //set the online resource
             try {
                 URL url = new URL(RequestUtils.baseURL(request) + ows.getId());
                 ows.setOnlineResource(url);
             } catch (MalformedURLException e) {
                 throw new RuntimeException(e);
             }
 
             //set the schema base
             ows.setSchemaBaseURL(RequestUtils.baseURL(request) + "schemas");
 
             //set the charset
             Charset charSet = null;
 
             try {
                 charSet = Charset.forName(request.getCharacterEncoding());
             } catch (Exception e) {
                 //TODO: make this server settable
                 charSet = Charset.forName("UTF-8");
             }
 
             ows.setCharSet(charSet);
 
             request.setCharacterEncoding(charSet.name());
         }
     }
 
     protected ModelAndView handleRequestInternal(HttpServletRequest httpRequest,
         HttpServletResponse httpResponse) throws Exception {
         preprocessRequest(httpRequest);
 
         //create a new request instnace
         Request request = new Request();
 
         //set request / response
         request.httpRequest = httpRequest;
         request.httpResponse = httpResponse;
 
         Service service = null;
 
         try {
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
         } catch (Throwable t) {
             exception(t, service, request);
         }
 
         return null;
     }
 
     Request init(Request request) throws ServiceException, IOException {
         HttpServletRequest httpRequest = request.httpRequest;
 
         //figure out method
         request.get = "GET".equalsIgnoreCase(httpRequest.getMethod())
             || "application/x-www-form-urlencoded".equals(httpRequest.getContentType());
 
         if (request.get) {
             //create the kvp map
             parseKVP(request);
         } else {
             //wrap the input stream in a buffer input stream
             request.input = reader(httpRequest);
 
             //mark the input stream, support up to 2KB, TODO: make this configuratable
             request.input.mark(2048);
         }
 
         return request;
     }
 
     BufferedReader reader(HttpServletRequest httpRequest)
         throws IOException {
         //create a buffer so we can reset the input stream
         BufferedInputStream input = new BufferedInputStream(httpRequest.getInputStream());
         input.mark(2048);
 
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
         if (req.get) {
             //check kvp
             req.service = normalize((String) req.kvp.get("service"));
             req.version = normalize((String) req.kvp.get("version"));
             req.request = normalize((String) req.kvp.get("request"));
             req.outputFormat = normalize((String) req.kvp.get("outputFormat"));
         } else {
             //check the body
             if (req.input != null) {
                 Map xml = readOpPost(req.input);
                 req.service = normalize((String) xml.get("service"));
                 req.version = normalize((String) xml.get("version"));
                 req.request = normalize((String) xml.get("request"));
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
         return findService(service, req.version);
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
             String msg = "Could not determine request.";
             throw new ServiceException(msg, "MissingParameterValue", "request");
         }
 
         // lookup the operation, initial lookup based on (service,request)
         Object serviceBean = serviceDescriptor.getService();
         Method operation = OwsUtils.method(serviceBean.getClass(), req.request);
 
         if (operation == null) {
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
 
                 if (req.get) {
                     //use the kvp reader mechanism
                     requestBean = parseRequestKVP(parameterType, req);
                 } else {
                     //use the xml reader mechanism
                     requestBean = parseRequestXML(req.input, req);
                 }
 
                 // another couple of thos of those lovley cite things, version+service has to specified for 
                 // non capabilities request, so if we dont have either thus far, check the request
                 // objects to try and find one
                 // TODO: should make this configurable
                 if (requestBean != null) {
                     //if we dont have a version thus far, check the request object
                     if (req.service == null) {
                         req.service = lookupRequestBeanProperty(requestBean, "service");
                     }
 
                     if (req.version == null) {
                         req.version = lookupRequestBeanProperty(requestBean, "version");
                     }
 
                     if (req.outputFormat == null) {
                         req.outputFormat = lookupRequestBeanProperty(requestBean, "outputFormat");
                     }
 
                     parameters[i] = requestBean;
                 }
             }
         }
 
         if (!"GetCapabilities".equalsIgnoreCase(req.request)) {
             if (req.version == null) {
                 //must be a version on non-capabilities requests
                 throw new ServiceException("Could not determine version", "MissingParameterValue",
                     "version");
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
                 throw new ServiceException("Could not determine service", "MissingParameterValue",
                     "service");
             }
         }
 
         return new Operation(req.request, serviceDescriptor, operation, parameters);
     }
 
     String lookupRequestBeanProperty(Object requestBean, String property) {
         if (requestBean instanceof EObject && EMFUtils.has((EObject) requestBean, property)) {
             //special case hack for eObject, we should move 
             // this out into an extension ppint
             EObject eObject = (EObject) requestBean;
 
             if (EMFUtils.isSet(eObject, property)) {
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
 
         return result;
     }
 
     void response(Object result, Request req, Operation opDescriptor)
         throws Throwable {
         //step 6: write response
         if (result != null) {
             //look up respones
             List responses = GeoServerExtensions.extensions(Response.class);
 
             //first filter by binding, and canHandle
             for (Iterator itr = responses.iterator(); itr.hasNext();) {
                 Response response = (Response) itr.next();
 
                 Class binding = response.getBinding();
 
                 if (!binding.isAssignableFrom(result.getClass())
                         || !response.canHandle(opDescriptor)) {
                     itr.remove();
 
                     continue;
                 }
 
                 //filter by output format
                 String outputFormat = response.getOutputFormat();
 
                 if ((req.outputFormat != null) && (outputFormat != null)
                         && !req.outputFormat.equals(outputFormat)) {
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
 
             //load the output strategy to be used
             ServiceStrategy outputStrategy = findOutputStrategy(req.httpResponse);
 
             if (outputStrategy == null) {
                 outputStrategy = new DefaultOutputStrategy();
             }
 
             //set the mime type
             req.httpResponse.setContentType(response.getMimeType(result, opDescriptor));
 
             //TODO: initialize any header params (gzip,deflate,etc...)
             OutputStream output = outputStrategy.getDestination(req.httpResponse);
             response.write(result, output, opDescriptor);
 
             outputStrategy.flush(req.httpResponse);
 
             //flush the underlying out stream for good meaure
             req.httpResponse.getOutputStream().flush();
         }
     }
 
     Collection loadServices() {
         Collection services = getApplicationContext().getBeansOfType(Service.class).values();
 
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
             //try to instantiate one
             String msg = "No kvp reader: ( " + type + " )";
             throw new RuntimeException(msg);
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
         Collection xmlReaders = getApplicationContext().getBeansOfType(XmlRequestReader.class)
                                     .values();
 
         if (!(new HashSet(xmlReaders).size() == xmlReaders.size())) {
             String msg = "Two identical xml readers found";
             throw new IllegalStateException(msg);
         }
 
         return xmlReaders;
     }
 
     XmlRequestReader findXmlReader(String namespace, String element, String ver) {
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
             String msg = "No xml reader: (" + namespace + "," + element + ")";
             throw new RuntimeException(msg);
         }
 
         XmlRequestReader xmlReader = null;
 
         //if multiple, use version to filter match
         if (matches.size() > 1) {
             List vmatches = new ArrayList(matches);
 
             //match up the version
             if (ver != null) {
                 Version version = new Version(ver);
 
                 //version specified, look for a match
                 for (Iterator itr = vmatches.iterator(); itr.hasNext();) {
                     XmlRequestReader r = (XmlRequestReader) itr.next();
 
                     if (version.equals(r.getVersion())) {
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
 
             //multiple readers found, sort by version
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
 
                             return v1.compareTo(v2);
                         }
                     };
 
                 Collections.sort(vmatches, comparator);
             }
 
             xmlReader = (XmlRequestReader) vmatches.get(vmatches.size() - 1);
         } else {
             //only a single match, that was easy
             xmlReader = (XmlRequestReader) matches.get(0);
         }
 
         return xmlReader;
     }
 
     Collection loadOutputStrategyFactories() {
         return GeoServerExtensions.extensions(OutputStrategyFactory.class);
     }
 
     ServiceStrategy findOutputStrategy(HttpServletResponse response) {
         //load all available factories
         Collection strategyFactories = loadOutputStrategyFactories();
 
         for (Iterator i = strategyFactories.iterator(); i.hasNext();) {
             OutputStrategyFactory factory = (OutputStrategyFactory) i.next();
 
             //can this factory create a strategy for the response
             ServiceStrategy strategy = factory.createOutputStrategy(response);
 
             if (strategy != null) {
                 //yes it can, return it
                 return strategy;
             }
         }
 
         return null;
     }
 
     BufferedInputStream input(File cache) throws IOException {
         return (cache == null) ? null : new BufferedInputStream(new FileInputStream(cache));
     }
 
     void parseKVP(Request req) throws ServiceException {
         HttpServletRequest request = req.httpRequest;
 
         //unparsed kvp set
         Map kvp = request.getParameterMap();
 
         if (kvp == null) {
             req.kvp = Collections.EMPTY_MAP;
 
             return;
         }
 
         //look up parser objects
         Collection parsers = getApplicationContext().getBeansOfType(KvpParser.class).values();
         Map parsedKvp = new KvpMap();
 
         for (Iterator itr = kvp.entrySet().iterator(); itr.hasNext();) {
             Map.Entry entry = (Map.Entry) itr.next();
             String key = (String) entry.getKey();
             String value = null;
 
             if (entry.getValue() instanceof String) {
                 value = (String) entry.getValue();
             } else if (entry.getValue() instanceof String[]) {
                 //TODO: perhaps handle multiple values for a key
                 value = (String) ((String[]) entry.getValue())[0];
             }
 
             //find the parser for this key value pair
             Object parsed = null;
 
             for (Iterator pitr = parsers.iterator(); pitr.hasNext();) {
                 KvpParser parser = (KvpParser) pitr.next();
 
                 if (key.equalsIgnoreCase(parser.getKey())) {
                     try {
                         parsed = parser.parse(value);
                     } catch (Throwable t) {
                         //dont throw any exceptions yet, befor the service is
                         // known
                         req.error = t;
                     }
                 }
             }
 
             //if noone could parse, just set to string value
             if (parsed == null) {
                 parsed = value;
             }
 
             //convert key to lowercase 
             parsedKvp.put(key.toLowerCase(), parsed);
         }
 
         req.kvp = parsedKvp;
     }
 
     Object parseRequestKVP(Class type, Request request)
         throws Exception {
         KvpRequestReader kvpReader = findKvpRequestReader(type);
         Map kvp = request.kvp;
 
         if (kvpReader != null) {
             //check for http request awareness
             if (kvpReader instanceof HttpServletRequestAware) {
                 ((HttpServletRequestAware) kvpReader).setHttpRequest(request.httpRequest);
             }
 
             Object requestBean = kvpReader.createRequest();
 
             if (requestBean != null) {
                 requestBean = kvpReader.read(requestBean, kvp);
             }
 
             return requestBean;
         }
 
         return null;
     }
 
     Object parseRequestXML(BufferedReader input, Request request)
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
 
         for (int i = 0; i < parser.getAttributeCount(); i++) {
             if ("version".equals(parser.getAttributeName(i))) {
                 version = parser.getAttributeValue(i);
 
                 break;
             }
         }
 
         parser.setInput(null);
 
         //reset input stream
         input.reset();
 
         XmlRequestReader xmlReader = findXmlReader(namespace, element, version);
 
         if (xmlReader instanceof HttpServletRequestAware) {
             ((HttpServletRequestAware) xmlReader).setHttpRequest(request.httpRequest);
         }
 
         return xmlReader.read(input);
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
         t.printStackTrace();
 
         //wrap in service exception if necessary
         ServiceException se = null;
 
         if (t instanceof ServiceException) {
             se = (ServiceException) t;
         } else {
             //unwind the exception stack, look for a service exception
             Throwable cause = t.getCause();
 
             while (cause != null) {
                 if (cause instanceof ServiceException) {
                     ServiceException cse = (ServiceException) cause;
                     se = new ServiceException(cse.getMessage(), t, cse.getCode(), cse.getLocator());
 
                     break;
                 }
 
                 cause = cause.getCause();
             }
         }
 
         if (se == null) {
             //couldn't find one, just wrap in one
             se = new ServiceException(t);
         }
 
         //find an exception handler
         ServiceExceptionHandler handler = null;
 
         if (service != null) {
             //look up the service exception handler
             Collection handlers = getApplicationContext()
                                       .getBeansOfType(ServiceExceptionHandler.class).values();
 
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
 
         handler.handleServiceException(se, service, request.httpRequest, request.httpResponse);
     }
 
     /**
      * Map which makes keys case insensitive.
      *
      * @author Justin Deoliveira, The Open Planning Project
      *
      */
     private static class KvpMap extends HashMap {
         private static final long serialVersionUID = 1L;
 
         public boolean containsKey(Object key) {
             return super.containsKey(upper(key));
         }
 
         public Object get(Object key) {
             return super.get(upper(key));
         }
 
         public Object put(Object key, Object value) {
             return super.put(upper(key), value);
         }
 
         Object upper(Object key) {
             if ((key != null) && key instanceof String) {
                 return ((String) key).toUpperCase();
             }
 
             return key;
         }
     }
 
     /**
      * Helper class to hold attributes of hte request
      *
      */
     static class Request {
         /**
          * Http request / response
          */
         HttpServletRequest httpRequest;
         HttpServletResponse httpResponse;
 
         /**
          * flag indicating if the request is get
          */
         boolean get;
 
         /**
          * Kvp parameters, only non-null if get = true
          */
         Map kvp;
 
         /**
          * buffered input stream, only non-null if get = false
          */
         BufferedReader input;
 
         /**
          * The ows service,request,version
          */
         String service;
         String request;
         String version;
 
         /**
          * The output format of hte request
          */
         String outputFormat;
 
         /**
          * Any errors that occur tryinng to determine the service
          */
         Throwable error;

        public String toString() {
            return service + " " + version + " " + request;
        }
     }
 }
