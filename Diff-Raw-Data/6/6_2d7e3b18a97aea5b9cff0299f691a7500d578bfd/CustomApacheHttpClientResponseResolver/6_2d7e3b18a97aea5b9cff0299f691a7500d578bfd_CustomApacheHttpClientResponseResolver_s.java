 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.client.jersey.cache;
 
 import com.sun.jersey.api.client.ClientRequest;
 import com.sun.jersey.client.apache.ApacheHttpMethodExecutor;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.List;
 import org.apache.commons.httpclient.ConnectMethod;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.apache.commons.httpclient.methods.OptionsMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.TraceMethod;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.httpcache4j.HTTPException;
 import org.codehaus.httpcache4j.HTTPMethod;
 import org.codehaus.httpcache4j.HTTPRequest;
 import org.codehaus.httpcache4j.HTTPResponse;
 import org.codehaus.httpcache4j.Headers;
 import org.codehaus.httpcache4j.Status;
 import org.codehaus.httpcache4j.payload.DelegatingInputStream;
 import org.codehaus.httpcache4j.resolver.ResponseCreator;
 import org.codehaus.httpcache4j.resolver.ResponseResolver;
 
 /**
  *
  * @author hamnis
  * @author imyousuf
  */
 public class CustomApacheHttpClientResponseResolver implements ResponseResolver {
 
   private ResponseCreator responseCreator = new ResponseCreator();
   private ApacheHttpMethodExecutor methodProcessor;
   private final ThreadLocal<ClientRequest> request;
 
   public CustomApacheHttpClientResponseResolver(ApacheHttpMethodExecutor methodProcessor,
                                                 ThreadLocal<ClientRequest> reqLocal) {
     this.methodProcessor = methodProcessor;
     this.request = reqLocal;
   }
 
   private HttpMethod convertRequest(HTTPRequest request) {
     URI requestURI = request.getRequestURI();
     HttpMethod method = getMethod(request.getMethod(), requestURI);
     copyHeaders(request, method);
     return method;
   }
 
   private HTTPResponse convertResponse(HttpMethod method) {
     Headers headers = new Headers();
     for (Header header : method.getResponseHeaders()) {
       headers = headers.add(header.getName(), header.getValue());
     }
     InputStream stream = null;
     HTTPResponse response;
     try {
       stream = getInputStream(method);
       //TODO change it to latest code in next version
       response = responseCreator.createResponse(Status.valueOf(method.getStatusCode()), headers, stream);
     }
     finally {
       if (stream == null) {
         method.releaseConnection();
       }
     }
     return response;
   }
 
   private InputStream getInputStream(HttpMethod method) {
     try {
       return method.getResponseBodyAsStream() != null ? new HttpMethodStream(method) : null;
     }
     catch (IOException e) {
       method.releaseConnection();
       throw new HTTPException("Unable to get InputStream from HttpClient", e);
     }
   }
 
   protected HttpMethod getMethod(HTTPMethod method, URI requestURI) {
     if (HTTPMethod.CONNECT.equals(method)) {
       HostConfiguration config = new HostConfiguration();
       config.setHost(requestURI.getHost(), requestURI.getPort(), requestURI.getScheme());
       return new ConnectMethod(config);
     }
     else if (HTTPMethod.DELETE.equals(method)) {
       return new CustomHttpMethod(HTTPMethod.DELETE.name(), requestURI.toString());
     }
     else if (HTTPMethod.GET.equals(method)) {
       return new GetMethod(requestURI.toString());
     }
     else if (HTTPMethod.HEAD.equals(method)) {
       return new HeadMethod(requestURI.toString());
     }
     else if (HTTPMethod.OPTIONS.equals(method)) {
       return new OptionsMethod(requestURI.toString());
     }
     else if (HTTPMethod.POST.equals(method)) {
       return new PostMethod(requestURI.toString());
     }
     else if (HTTPMethod.PUT.equals(method)) {
       return new PutMethod(requestURI.toString());
     }
     else if (HTTPMethod.TRACE.equals(method)) {
       return new TraceMethod(requestURI.toString());
     }
     else {
       return new CustomHttpMethod(method.name(), requestURI.toString());
     }
   }
 
   protected void copyHeaders(HTTPRequest request, HttpMethod method) {
     Headers headers = request.getAllHeaders();
     for (String headerNames : headers.keySet()) {
       List<org.codehaus.httpcache4j.Header> headersForName = headers.getHeaders(headerNames);
       for (org.codehaus.httpcache4j.Header header : headersForName) {
         method.addRequestHeader(header.getName(), header.getValue());
       }
     }
   }
 
   private static class CustomHttpMethod extends EntityEnclosingMethod {
 
     private final String name;
 
     public CustomHttpMethod(String name, String uri) {
       super(uri);
       if (StringUtils.isBlank(name)) {
         throw new IllegalArgumentException("Name can not be blank!");
       }
       this.name = name;
     }
 
     @Override
     public String getName() {
       return name;
     }
   }
 
   @Override
   public HTTPResponse resolve(HTTPRequest httpr) throws IOException {
     HttpMethod method = convertRequest(httpr);
     methodProcessor.executeMethod(method, request.get());
     return convertResponse(method);
 
   }
 
   private static class HttpMethodStream extends DelegatingInputStream {
 
     private final HttpMethod method;
 
     public HttpMethodStream(final HttpMethod method) throws IOException {
       super(method.getResponseBodyAsStream());
       this.method = method;
     }
 
     @Override
     public void close() throws IOException {
       method.releaseConnection();
     }
   }
 }
