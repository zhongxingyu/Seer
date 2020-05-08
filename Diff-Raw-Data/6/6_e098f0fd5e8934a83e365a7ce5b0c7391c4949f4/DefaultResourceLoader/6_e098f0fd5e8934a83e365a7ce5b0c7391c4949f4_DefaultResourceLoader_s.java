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
 
 import org.apache.http.HttpResponse;
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
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.BasicClientConnectionManager;
 import org.apache.http.impl.conn.SchemeRegistryFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.histone.evaluator.functions.node.object.ToQueryString;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.ObjectHistoneNode;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * Histone default resource loader<br/>
  * Supports file and http protocols
  */
 public class DefaultResourceLoader implements ResourceLoader {
     private static final Logger log = LoggerFactory.getLogger(DefaultResourceLoader.class);
     private ClientConnectionManager  httpClientConnectionManager = new BasicClientConnectionManager(SchemeRegistryFactory.createDefault());
 
     @Override
     public String resolveFullPath(String location, String baseLocation) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         URI fullLocation = makeFullLocation(location, baseLocation);
 
         return fullLocation.toString();
     }
 
     @Override
     public Resource load(String location, String baseLocation, Node... args) throws ResourceLoadException {
         log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});
 
         URI fullLocation = makeFullLocation(location, baseLocation);
 
         Resource resource = null;
 
         if (fullLocation.getScheme().equals("file")) {
             resource = loadFileResource(fullLocation);
         } else if (fullLocation.getScheme().equals("http")) {
             resource = loadHttpResource(fullLocation, args);
         } else {
             throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", fullLocation.getScheme()));
         }
 
 
         return resource;
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
 
         return new StreamResource(stream, location.toString());
     }
 
 	private Resource loadHttpResource(URI location, Node[] args) {
         URI newLocation = URI.create(location.toString().replace("#fragment", ""));
 		final Map<Object, Node> requestMap = args != null && args.length != 0 && args[0] instanceof ObjectHistoneNode ? ((ObjectHistoneNode) args[0])
 				.getElements() : new HashMap<Object, Node>();
 		final String method = requestMap.containsKey("method") ? requestMap.get("method").getAsString().getValue() : "GET";
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
         }
 
         for (Map.Entry<String, String> en : filteredHeaders.entrySet()) {
             request.setHeader(en.getKey(), en.getValue());
         }
         if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && data != null) {
             String stringData = null;
            String contentType = "";
             if (data.isNull() || data.isUndefined()) {
             } else if (data.isString()) {
                 stringData = data.getAsString().getValue();
             } else if (data.isBoolean()) {
                 stringData = data.getAsString().getValue();
             } else if (data.isObject()) {
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
                request.setHeader("Content-Type", contentType);
             }
         }
 		if (request.getHeaders("content-Type").length == 0) {
             request.setHeader("Content-Type","");
 		}
 
 		// Execute request
 		HttpClient client = new DefaultHttpClient(httpClientConnectionManager);
 		InputStream input = null;
 		try {
 			HttpResponse response = client.execute(request);
 			input = response.getEntity() == null ? null: response.getEntity().getContent();
 		} catch (IOException e) {
 			throw new ResourceLoadException(String.format("Can't load resource '%s'", location.toString()));
 		} finally {
 		}
 		return new StreamResource(input, location.toString());
 	}
 	
 	private Map<String, String> filterRequestHeaders(Map<String, String> requestHeaders) {
 		String[] prohibited = { "accept-charset", "accept-encoding", "access-control-request-headers", "access-control-request-method",
 				"connection", "content-length", "cookie", "cookie2", "content-transfer-encoding", "date", "expect", "host", "keep-alive",
 				"origin", "referer", "te", "trailer", "transfer-encoding", "upgrade", "user-agent", "via" };
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
 
     
 }
