 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.resourceloaders;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.Header;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpOptions;
 import org.apache.http.client.methods.HttpPatch;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpTrace;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.impl.conn.BasicClientConnectionManager;
 import org.apache.http.impl.conn.SchemeRegistryFactory;
 import org.apache.http.protocol.HttpContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.histone.HistoneTokensHolder;
 import ru.histone.evaluator.functions.node.object.ToQueryString;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.evaluator.nodes.ObjectHistoneNode;
 import ru.histone.parser.Parser;
 import ru.histone.tokenizer.TokenizerFactory;
 import ru.histone.utils.PathUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Histone default resource loader<br/>
  * Supports file and http protocols
  */
 public class DefaultResourceLoader implements ResourceLoader {
     private static final Logger log = LoggerFactory.getLogger(DefaultResourceLoader.class);
 
     private ClientConnectionManager httpClientConnectionManager = new BasicClientConnectionManager(SchemeRegistryFactory.createDefault());
 
     private TokenizerFactory tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
     private NodeFactory nodeFactory = new NodeFactory(new ObjectMapper());
     private Parser parser = new Parser(tokenizerFactory, nodeFactory);
 
     @Override
     public String resolveFullPath(String location, String baseLocation) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         URI fullLocation = makeFullLocation(location, baseLocation);
 
         return fullLocation.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     protected Resource load(String location, String baseLocation, Node... args) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         String fullLocation = PathUtils.resolveUrl(location, baseLocation);
         ru.histone.evaluator.functions.global.URI uri =PathUtils.parseURI(fullLocation);

         Resource resource = null;
         if (uri.getScheme().equals("file")) {
            resource = loadFileResource(makeFullLocation(location, baseLocation));
         } else if (uri.getScheme().equals("http")) {
            resource = loadHttpResource(makeFullLocation(location, baseLocation), args);
         } else if (uri.getScheme().equals("data")) {
             resource = loadDataResource(fullLocation);
         } else {
             throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", uri.getScheme()));
         }
 
 
         return resource;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Resource load(String href, String baseHref, String[] contentTypes, Node... args) throws ResourceLoadException {
         final Set<String> contentTypesSet = new HashSet<String>();
         contentTypesSet.addAll(Arrays.asList(contentTypes));
 
         if (contentTypesSet.contains(ContentType.TEXT)) {
             return load(href, baseHref, args);
         } else {
             throw new UnsupportedContentTypeException(contentTypes, getClass());
         }
     }
 
    private Resource loadFileResource(URI location) {
         InputStream stream = null;
 
         File file = new File(location);
         if (file.exists() && file.isFile() && file.canRead()) {
             try {
                 stream = new FileInputStream(file);
             } catch (FileNotFoundException e) {
                 throw new ResourceLoadException("File not found", e);
             }
         } else {
            throw new ResourceLoadException(String.format("Can't read file '%s'", location.toString()));
         }
 
        return new StreamResource(stream, location.toString(), ContentType.TEXT);
     }
 
    private Resource loadHttpResource(URI location, Node[] args) {
        URI newLocation = URI.create(location.toString().replace("#fragment", ""));
         final Map<Object, Node> requestMap = args != null && args.length != 0 && args[0] instanceof ObjectHistoneNode ? ((ObjectHistoneNode) args[0])
                 .getElements() : new HashMap<Object, Node>();
         Node methodNode = requestMap.get("method");
         final String method = methodNode != null && methodNode.isString() && methodNode.getAsString().getValue().length() != 0 ? requestMap
                 .get("method").getAsString().getValue() : "GET";
         final Map<String, String> headers = new HashMap<String, String>();
         if (requestMap.containsKey("headers")) {
             for (Map.Entry<Object, Node> en : requestMap.get("headers").getAsObject().getElements().entrySet()) {
                 String value = null;
                 if (en.getValue().isUndefined())
                     value = "undefined";
                 else
                     value = en.getValue().getAsString().getValue();
                 headers.put(en.getKey().toString(), value);
             }
         }
 
         final Map<String, String> filteredHeaders = filterRequestHeaders(headers);
         final Node data = requestMap.containsKey("data") ? (Node) requestMap.get("data") : null;
 
         // Prepare request
         HttpRequestBase request = new HttpGet(newLocation);
         if ("POST".equalsIgnoreCase(method)) {
             request = new HttpPost(newLocation);
         } else if ("PUT".equalsIgnoreCase(method)) {
             request = new HttpPut(newLocation);
         } else if ("DELETE".equalsIgnoreCase(method)) {
             request = new HttpDelete(newLocation);
         } else if ("TRACE".equalsIgnoreCase(method)) {
             request = new HttpTrace(newLocation);
         } else if ("OPTIONS".equalsIgnoreCase(method)) {
             request = new HttpOptions(newLocation);
         } else if ("PATCH".equalsIgnoreCase(method)) {
             request = new HttpPatch(newLocation);
         } else if ("HEAD".equalsIgnoreCase(method)) {
             request = new HttpHead(newLocation);
         } else if (method != null && !"GET".equalsIgnoreCase(method)) {
            return new StreamResource(null, location.toString(), ContentType.TEXT);
         }
 
         for (Map.Entry<String, String> en : filteredHeaders.entrySet()) {
             request.setHeader(en.getKey(), en.getValue());
         }
         if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && data != null) {
             String stringData = null;
             String contentType = filteredHeaders.get("content-type") == null ? "" : filteredHeaders.get("content-type");
             if (data.isObject()) {
                 stringData = ToQueryString.toQueryString(data.getAsObject(), null, "&");
                 contentType = "application/x-www-form-urlencoded";
             } else {
                 stringData = data.getAsString().getValue();
             }
             if (stringData != null) {
                 StringEntity se;
                 try {
                     se = new StringEntity(stringData);
                 } catch (UnsupportedEncodingException e) {
                     throw new ResourceLoadException(String.format("Can't encode data '%s'", stringData));
                 }
                 ((HttpEntityEnclosingRequestBase) request).setEntity(se);
             }
             request.setHeader("Content-Type", contentType);
         }
         if (request.getHeaders("content-Type").length == 0) {
             request.setHeader("Content-Type", "");
         }
 
         // Execute request
         HttpClient client = new DefaultHttpClient(httpClientConnectionManager);
         ((AbstractHttpClient) client).setRedirectStrategy(new RedirectStrategy());
         InputStream input = null;
         try {
             HttpResponse response = client.execute(request);
             input = response.getEntity() == null ? null : response.getEntity().getContent();
         } catch (IOException e) {
            throw new ResourceLoadException(String.format("Can't load resource '%s'", location.toString()));
         } finally {
         }
        return new StreamResource(input, location.toString(), ContentType.TEXT);
     }
     
     private Resource loadDataResource(String location){
 
         InputStream input = null;
         if(!location.matches("data:(.*);base64,(.*)")){
             return null;
         }
         URI uri = makeFullLocation(location,"");
         String[] stringUri = uri.getSchemeSpecificPart().split(",");
 
         if(stringUri.length > 1){
             String toEncode = stringUri[1];
             if(stringUri[0].contains("base64") ){
                 input = new ByteArrayInputStream(Base64.decodeBase64(toEncode.getBytes()));
             } else {
                 input = new ByteArrayInputStream(toEncode.getBytes());
             }
         } else {
             input = new ByteArrayInputStream("".getBytes());
         }
 
        return new StreamResource(input, location.toString(), ContentType.TEXT);
     }
     
     
 
     private Map<String, String> filterRequestHeaders(Map<String, String> requestHeaders) {
         String[] prohibited = {"accept-charset", "accept-encoding", "access-control-request-headers", "access-control-request-method",
                 "connection", "content-length", "cookie", "cookie2", "content-transfer-encoding", "date", "expect", "host", "keep-alive",
                 "origin", "referer", "te", "trailer", "transfer-encoding", "upgrade", "user-agent", "via"};
         Map<String, String> headers = new HashMap<String, String>();
         for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
             if (entry.getValue() == null)
                 continue;
             String name = entry.getKey().toLowerCase();
             if (name.indexOf("sec-") == 0)
                 continue;
             if (name.indexOf("proxy-") == 0)
                 continue;
             if (Arrays.asList(prohibited).contains(name))
                 continue;
             headers.put(entry.getKey(), entry.getValue());
         }
         return headers;
     }
 
     public URI makeFullLocation(String location, String baseLocation) {
         if (location == null) {
             throw new ResourceLoadException("Resource location is undefined!");
         }
 
         URI locationURI = URI.create(location);
 
         if (baseLocation == null && !locationURI.isAbsolute()) {
             throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
         }
 
         if (baseLocation != null) {
             baseLocation = baseLocation.replace("\\", "/");
             baseLocation = baseLocation.replace("file://", "file:/");
         }
         URI baseLocationURI = (baseLocation != null) ? URI.create(baseLocation) : null;
 
         if (!locationURI.isAbsolute() && baseLocation != null) {
             locationURI = baseLocationURI.resolve(locationURI.normalize());
         }
 
         if (!locationURI.isAbsolute()) {
             throw new ResourceLoadException("Resource location is not absolute!");
         }
 
         return locationURI;
     }
 
     public ClientConnectionManager getHttpClientConnectionManager() {
         return httpClientConnectionManager;
     }
 
     public void setHttpClientConnectionManager(ClientConnectionManager httpClientConnectionManager) {
         this.httpClientConnectionManager = httpClientConnectionManager;
     }
 
 
     private class RedirectStrategy extends DefaultRedirectStrategy {
         @Override
         public boolean isRedirected(
                 final HttpRequest request,
                 final HttpResponse response,
                 final HttpContext context) throws ProtocolException {
             if (request == null) {
                 throw new IllegalArgumentException("HTTP request may not be null");
             }
             if (response == null) {
                 throw new IllegalArgumentException("HTTP response may not be null");
             }
 
             int statusCode = response.getStatusLine().getStatusCode();
             String method = request.getRequestLine().getMethod();
             Header locationHeader = response.getFirstHeader("location");
             if (301 <= statusCode && statusCode <= 399) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 
 }
