 /*
  * Copyright (C) 2011 OpenStack LLC.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.openstack.burrow.backend.http;
 
 import org.apache.http.*;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.nio.client.HttpAsyncClient;
 import org.apache.http.nio.concurrent.FutureCallback;
 import org.apache.http.nio.reactor.IOReactorException;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.openstack.burrow.backend.AsyncBackend;
 import org.openstack.burrow.client.*;
 import org.openstack.burrow.client.methods.*;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 public class AsyncHttp implements AsyncBackend{
   private HttpAsyncClient client;
   private String host;
   private int port;
   private String scheme = "http";
 
   public AsyncHttp (String host, int port) {
     this.host = host;
     this.port = port;
     try {
       this.client = new DefaultHttpAsyncClient();
     } catch (IOReactorException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
       }
   }
 
   @Override
   public Message execute(CreateMessage request) {
     try {
       return executeAsync(request).get();
       } catch (InterruptedException e) {
         e.printStackTrace();
         throw new RuntimeException("Error executing HTTP request: " + e);
       } catch (ExecutionException e) {
         e.printStackTrace();
         throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public Message execute(DeleteMessage request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Message> execute(DeleteMessages request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public Message execute(GetMessage request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Message> execute(GetMessages request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public Message execute(UpdateMessage request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Message> execute(UpdateMessages request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Account> execute(DeleteAccounts request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Queue> execute(DeleteQueues request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Account> execute(GetAccounts request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public List<Queue> execute(GetQueues request) {
     try {
       return executeAsync(request).get();
     } catch (InterruptedException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     } catch (ExecutionException e) {
       e.printStackTrace();
       throw new RuntimeException("Error executing HTTP request: " + e);
     }
   }
 
   @Override
   public Future<List<Account>> executeAsync(DeleteAccounts request) {
     HttpDelete httpRequest = getHttpRequest(request);
     final FutureAccountList futureAccountList = new FutureAccountList();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureAccountList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureAccountList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureAccountList;
   }
 
   @Override
   public Future<List<Queue>> executeAsync(DeleteQueues request) {
     HttpDelete httpRequest = getHttpRequest(request);
    final FutureQueueList futureQueueList = new FutureQueueList(request.getAccount());
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureQueueList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureQueueList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureQueueList;
   }
 
   @Override
   public Future<List<Account>> executeAsync(GetAccounts request) {
     HttpGet httpRequest = getHttpRequest(request);
     final FutureAccountList futureAccountList = new FutureAccountList();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureAccountList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureAccountList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureAccountList;
   }
 
   @Override
   public Future<List<Queue>> executeAsync(GetQueues request) {
     HttpGet httpRequest = getHttpRequest(request);
    final FutureQueueList futureQueueList = new FutureQueueList(request.getAccount());
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureQueueList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureQueueList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureQueueList;
   }
 
   @Override
   public Future<Message> executeAsync(CreateMessage request) {
     HttpPut httpRequest = getHttpRequest(request);
     final FutureMessage futureMessage = new FutureMessage();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessage.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessage.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessage;
   }
 
   @Override
   public Future<Message> executeAsync(GetMessage request) {
     HttpGet httpRequest = getHttpRequest(request);
     final FutureMessage futureMessage = new FutureMessage();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessage.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessage.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessage;
   }
 
   @Override
   public Future<Message> executeAsync(DeleteMessage request) {
     HttpDelete httpRequest = getHttpRequest(request);
     final FutureMessage futureMessage = new FutureMessage();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessage.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessage.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessage;
   }
 
   @Override
   public Future<List<Message>> executeAsync(DeleteMessages request) {
     HttpDelete httpRequest = getHttpRequest(request);
     final FutureMessageList futureMessageList = new FutureMessageList();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessageList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessageList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessageList;
   }
 
   @Override
   public Future<List<Message>> executeAsync(GetMessages request) {
     HttpGet httpRequest = getHttpRequest(request);
     final FutureMessageList futureMessageList = new FutureMessageList();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessageList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessageList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessageList;
   }
 
   @Override
   public Future<Message> executeAsync(UpdateMessage request) {
     HttpPost httpRequest = getHttpRequest(request);
     final FutureMessage futureMessage = new FutureMessage();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessage.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessage.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessage;
   }
 
   @Override
   public Future<List<Message>> executeAsync(UpdateMessages request) {
     HttpPost httpRequest = getHttpRequest(request);
     final FutureMessageList futureMessageList = new FutureMessageList();
     client.execute(httpRequest, new FutureCallback<HttpResponse>() {
 
       public void completed(final HttpResponse response) {
         futureMessageList.setHttpResponse(response);
       }
 
       public void failed(final Exception e) {
         futureMessageList.setException(e);
       }
 
       public void cancelled() {
         throw new RuntimeException("Should not have called .cancelled() on FutureCallBack");// raise a RuntimeError because we don't allow this.
       }
 
     });
     return futureMessageList;
   }
 
   /**
    * Construct a new HttpPut request for the CreateMessage action.
    *
    * @param request
    * @return
    */
   private HttpPut getHttpRequest(CreateMessage request) {
     URI uri = getUri(request);
     HttpPut httpRequest = new HttpPut(uri);
     try {
       HttpEntity bodyEntity = new StringEntity(request.getBody(), "UTF-8");
       httpRequest.setEntity(bodyEntity);
     } catch (UnsupportedEncodingException e) {
       // This should be impossible for any legal String.
       throw new RuntimeException("Unable to create body HttpEntity: " + e);
     }
     return httpRequest;
   }
 
   private HttpDelete getHttpRequest(DeleteMessage request) {
     URI uri = getUri(request);
     HttpDelete httpRequest = new HttpDelete(uri);
     return httpRequest;
   }
 
   private HttpDelete getHttpRequest(DeleteMessages request) {
     URI uri = getUri(request);
     HttpDelete httpRequest = new HttpDelete(uri);
     return httpRequest;
   }
 
   private HttpGet getHttpRequest(GetMessage request) {
     URI uri = getUri(request);
     HttpGet httpRequest = new HttpGet(uri);
     return httpRequest;
   }
 
   private HttpGet getHttpRequest(GetMessages request) {
     URI uri = getUri(request);
     HttpGet httpRequest = new HttpGet(uri);
     return httpRequest;
   }
 
   private HttpPost getHttpRequest(UpdateMessage request) {
     URI uri = getUri(request);
     HttpPost httpRequest = new HttpPost(uri);
     return httpRequest;
   }
 
   private HttpPost getHttpRequest(UpdateMessages request) {
     URI uri = getUri(request);
     HttpPost httpRequest = new HttpPost(uri);
     return httpRequest;
   }
 
   private List<NameValuePair> getQueryParamaters(CreateMessage request) {
     Long ttl = request.getTtl();
     Long hide = request.getHide();
     if ((ttl != null) || (hide != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(2);
       if (ttl != null)
         params.add(new BasicNameValuePair("ttl", ttl.toString()));
       if (hide != null)
         params.add(new BasicNameValuePair("hide", hide.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(DeleteMessage request) {
     Boolean matchHidden = request.getMatchHidden();
     if (matchHidden != null) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(1);
       params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(DeleteMessages request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     Boolean matchHidden = request.getMatchHidden();
     String detail = request.getDetail();
     Long wait = request.getWait();
     if ((marker != null) || (limit != null) || (matchHidden != null) || (detail != null)
         || (wait != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(5);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (matchHidden != null)
         params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       if (wait != null)
         params.add(new BasicNameValuePair("wait", wait.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(GetMessage request) {
     Boolean matchHidden = request.getMatchHidden();
     String detail = request.getDetail();
     Long wait = request.getWait();
     if ((matchHidden != null) || (detail != null) || (wait != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(3);
       if (matchHidden != null)
         params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       if (wait != null)
         params.add(new BasicNameValuePair("wait", wait.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(GetMessages request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     Boolean matchHidden = request.getMatchHidden();
     String detail = request.getDetail();
     Long wait = request.getWait();
     if ((marker != null) || (limit != null) || (matchHidden != null) || (detail != null)
         || (wait != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(5);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (matchHidden != null)
         params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       if (wait != null)
         params.add(new BasicNameValuePair("wait", wait.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(UpdateMessage request) {
     Boolean matchHidden = request.getMatchHidden();
     Long ttl = request.getTtl();
     Long hide = request.getHide();
     String detail = request.getDetail();
     Long wait = request.getWait();
     if ((matchHidden != null) || (ttl != null) || (hide != null) || (detail != null)
         || (wait != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(3);
       if (matchHidden != null)
         params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       if (ttl != null)
         params.add(new BasicNameValuePair("ttl", ttl.toString()));
       if (hide != null)
         params.add(new BasicNameValuePair("hide", hide.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       if (wait != null)
         params.add(new BasicNameValuePair("wait", wait.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(UpdateMessages request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     Boolean matchHidden = request.getMatchHidden();
     Long ttl = request.getTtl();
     Long hide = request.getHide();
     String detail = request.getDetail();
     Long wait = request.getWait();
     if ((marker != null) || (limit != null) || (matchHidden != null) || (ttl != null)
         || (hide != null) || (detail != null) || (wait != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(7);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (matchHidden != null)
         params.add(new BasicNameValuePair("match_hidden", matchHidden.toString()));
       if (ttl != null)
         params.add(new BasicNameValuePair("ttl", ttl.toString()));
       if (hide != null)
         params.add(new BasicNameValuePair("hide", hide.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       if (wait != null)
         params.add(new BasicNameValuePair("wait", wait.toString()));
       return params;
     } else {
       return null;
     }
   }
 
   URI getUri(CreateMessage request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), request.getId(), getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(DeleteMessage request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), request.getId(), getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(DeleteMessages request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(GetMessage request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), request.getId(), getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(GetMessages request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(String account, String queue, String message, List<NameValuePair> params)
       throws URISyntaxException {
     String path = "/v1.0";
     if (account != null)
       path += "/" + account;
     if (queue != null)
       path += "/" + queue;
     if (message != null)
       path += "/" + message;
     String encodedParams = null;
     if (params != null)
       encodedParams = URLEncodedUtils.format(params, "UTF-8");
     return URIUtils.createURI(scheme, host, port, path, encodedParams, null);
   }
 
   URI getUri(UpdateMessage request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), request.getId(), getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   URI getUri(UpdateMessages request) {
     Queue queue = request.getQueue();
     Account account = queue.getAccount();
     try {
       return getUri(account.getId(), queue.getId(), null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   static List<Message> handleMultipleMessageHttpResponse(HttpResponse response) {
     StatusLine status = response.getStatusLine();
     HttpEntity entity = response.getEntity();
     if (entity == null)
       return null;
     String mimeType = EntityUtils.getContentMimeType(entity);
     switch (status.getStatusCode()) {
       case HttpStatus.SC_OK:
         if (mimeType.equals("application/json")) {
           try {
             String body = EntityUtils.toString(entity);
             JSONArray arrayJson = new JSONArray(body);
             List<Message> messages = new ArrayList<Message>(arrayJson.length());
             for (int idx = 0; idx < arrayJson.length(); idx++) {
               JSONObject messageJson = arrayJson.getJSONObject(idx);
               Message message = new MessageResponse(messageJson);
               messages.add(idx, message);
             }
             return messages;
           } catch (IOException e) {
             try {
               // Consume the entity to release HttpClient resources.
               EntityUtils.consume(entity);
             } catch (IOException e1) {
               // If we ever stop throwing an exception in the outside block,
               // this needs to be handled.
             }
             // TODO: Throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("IOException reading http response");
           } catch (JSONException e) {
             // It is not necessary to consume the entity because at the
             // first point where this exception can be thrown,
             // the entity has already been consumed by toString();
             // TODO: throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("JSONException reading response");
           }
         } else {
           // This situation cannot be handled.
           try {
             // Consume the entity to release HttpClient resources.
             EntityUtils.consume(entity);
           } catch (IOException e1) {
             // If we ever stop throwing an exception in the outside block,
             // this needs to be handled.
           }
           // TODO: Throw something appropriate.
           throw new RuntimeException("Non-Json response");
         }
       case HttpStatus.SC_NO_CONTENT:
         // This is not an error.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e) {
           // TODO: Throw something more appropriate.
           e.printStackTrace();
           throw new RuntimeException("Failed to consume HttpEntity");
         }
         return null;
       default:
         // This is probably an error.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e1) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         // TODO: Throw something appropriate.
         throw new RuntimeException("Unhandled http response code " + status.getStatusCode());
     }
   }
 
   static Message handleSingleMessageHttpResponse(HttpResponse response) {
     StatusLine status = response.getStatusLine();
     HttpEntity entity = response.getEntity();
     switch (status.getStatusCode()) {
       case HttpStatus.SC_OK:
       case HttpStatus.SC_CREATED:
         String responseMimeType = EntityUtils.getContentMimeType(entity);
         if (responseMimeType == null) {
           try {
             EntityUtils.consume(entity);
           } catch (IOException e) {
             // TODO: Throw something more appropriate.
             e.printStackTrace();
             throw new RuntimeException("Failed to consume HttpEntity");
           }
           return null;
         } else if (responseMimeType.equals("application/json")) {
           try {
             String body = EntityUtils.toString(entity);
             JSONObject messageJson = new JSONObject(body);
             return new MessageResponse(messageJson);
           } catch (JSONException e) {
             // At the first place this could be thrown,
             // the entity has already been consumed by toString(entity);
             // TODO: Throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("Failed to decode json");
           } catch (IOException e) {
             try {
               // Consume the entity to release HttpClient resources.
               EntityUtils.consume(entity);
             } catch (IOException e1) {
               // If we ever stop throwing an exception in the outside block,
               // this needs to be handled.
             }
             // TODO: Throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("Failed to convert the entity to a string");
           }
         } else {
           // We can't do anything sensible yet.
           try {
             // Consume the entity to release HttpClient resources.
             EntityUtils.consume(entity);
           } catch (IOException e) {
             // If we ever stop throwing an exception in the outside block,
             // this needs to be handled.
           }
           // TODO: Handle body-only responses.
           throw new RuntimeException("Unhandled response mime type " + responseMimeType);
         }
       case HttpStatus.SC_NO_CONTENT:
         try {
           EntityUtils.consume(entity);
         } catch (IOException e) {
           // TODO: Throw something more appropriate.
           e.printStackTrace();
           throw new RuntimeException("Failed to consume HttpEntity");
         }
         return null;
       case HttpStatus.SC_NOT_FOUND:
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         throw new NoSuchMessageException();
       default:
         // There was an error or an unexpected return condition.
         // TODO: Throw something more appropriate for each error condition.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         throw new RuntimeException("Unhandled return code");
     }
   }
 
 
     private HttpDelete getHttpRequest(DeleteAccounts request) {
     URI uri = getUri(request);
     HttpDelete httpRequest = new HttpDelete(uri);
     return httpRequest;
   }
 
 
     private HttpGet getHttpRequest(GetQueues request) {
       URI uri = getUri(request);
       HttpGet httpRequest = new HttpGet(uri);
       return httpRequest;
     }
 
   private HttpDelete getHttpRequest(DeleteQueues request) {
     URI uri = getUri(request);
     HttpDelete httpRequest = new HttpDelete(uri);
     return httpRequest;
   }
 
   private HttpGet getHttpRequest(GetAccounts request) {
     URI uri = getUri(request);
     HttpGet httpRequest = new HttpGet(uri);
     return httpRequest;
   }
 
   private URI getUri(DeleteAccounts request) {
     try {
       return getUri(null, null, null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   private URI getUri(DeleteQueues request) {
     Account account = request.getAccount();
     try {
       return getUri(account.getId(), null, null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   private URI getUri(GetAccounts request) {
     try {
       return getUri(null, null, null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
   private URI getUri(GetQueues request) {
     Account account = request.getAccount();
     try {
       return getUri(account.getId(), null, null, getQueryParamaters(request));
     } catch (URISyntaxException e) {
       throw new RuntimeException("Unable to build request URI: " + e);
     }
   }
 
     private List<NameValuePair> getQueryParamaters(GetQueues request) {
       String marker = request.getMarker();
       Long limit = request.getLimit();
       if ((marker != null) || (limit != null)) {
         List<NameValuePair> params = new ArrayList<NameValuePair>(2);
         if (marker != null)
           params.add(new BasicNameValuePair("marker", marker));
         if (limit != null)
           params.add(new BasicNameValuePair("limit", limit.toString()));
         return params;
       } else {
         return null;
       }
     }
 
   private List<NameValuePair> getQueryParamaters(DeleteQueues request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     String detail = request.getDetail();
     if ((marker != null) || (limit != null) || (detail != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(3);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(GetAccounts request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     String detail = request.getDetail();
     if ((marker != null) || (limit != null) || (detail != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(3);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       return params;
     } else {
       return null;
     }
   }
 
   private List<NameValuePair> getQueryParamaters(DeleteAccounts request) {
     String marker = request.getMarker();
     Long limit = request.getLimit();
     String detail = request.getDetail();
     if ((marker != null) || (limit != null) || (detail != null)) {
       List<NameValuePair> params = new ArrayList<NameValuePair>(3);
       if (marker != null)
         params.add(new BasicNameValuePair("marker", marker));
       if (limit != null)
         params.add(new BasicNameValuePair("limit", limit.toString()));
       if (detail != null)
         params.add(new BasicNameValuePair("detail", detail));
       return params;
     } else {
       return null;
     }
   }
 
 
 
   static List<Account> handleMultipleAccountHttpResponse(HttpResponse response) {
     StatusLine status = response.getStatusLine();
     HttpEntity entity = response.getEntity();
     if (entity == null)
       return null; // Is this actually the right thing to do?
     String mimeType = EntityUtils.getContentMimeType(entity);
     switch (status.getStatusCode()) {
       case HttpStatus.SC_OK:
         if (mimeType.equals("application/json")) {
           try {
             String body = EntityUtils.toString(entity);
             JSONArray arrayJson = new JSONArray(body);
             List<Account> accounts = new ArrayList<Account>(arrayJson.length());
             try {
               // Assume the response is an array of JSON Objects.
               for (int idx = 0; idx < arrayJson.length(); idx++) {
                 JSONObject accountJson = arrayJson.getJSONObject(idx);
                 Account account = new AccountResponse(accountJson);
                 accounts.add(idx, account);
               }
             } catch (JSONException e) {
               // The response was not an array of JSON Objects. Try again,
               // assuming it was an array of strings.
               for (int idx = 0; idx < arrayJson.length(); idx++) {
                 String accountId = arrayJson.getString(idx);
                 Account account = new AccountResponse(accountId);
                 accounts.add(idx, account);
               }
             }
             return accounts;
           } catch (IOException e) {
             try {
               // Consume the entity to release HttpClient resources.
               EntityUtils.consume(entity);
             } catch (IOException e1) {
               // If we ever stop throwing an exception in the outside block,
               // this needs to be handled.
             }
             // TODO: Throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("IOException reading http response");
           } catch (JSONException e) {
             // It is not necessary to consume the entity because at the
             // first point where this exception can be thrown,
             // the entity has already been consumed by toString();
             // TODO: throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("JSONException reading response");
           }
         } else {
           // This situation cannot be handled.
           try {
             // Consume the entity to release HttpClient resources.
             EntityUtils.consume(entity);
           } catch (IOException e1) {
             // If we ever stop throwing an exception in the outside block,
             // this needs to be handled.
           }
           // TODO: Throw something appropriate.
           throw new RuntimeException("Non-Json response");
         }
         /*
          * // THIS (MIGHT) be an error! case HttpStatus.SC_NOT_FOUND: // This is
          * not necessarily an error, but we must throw something. try { //
          * Consume the entity to release HttpClient resources.
          * EntityUtils.consume(entity); } catch (IOException e1) { // If we ever
          * stop throwing an exception in the outside block, // this needs to be
          * handled. } throw new NoSuchAccountException();
          */
       default:
         // This is probably an error.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e1) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         // TODO: Throw something appropriate.
         throw new RuntimeException("Unhandled http response code " + status.getStatusCode());
     }
   }
 
 
   static List<Queue> handleMultipleQueueHttpResponse(Account account, HttpResponse response) {
     StatusLine status = response.getStatusLine();
     HttpEntity entity = response.getEntity();
     if (entity == null)
       return null; // Is this actually the right thing to do?
     String mimeType = EntityUtils.getContentMimeType(entity);
     switch (status.getStatusCode()) {
       case HttpStatus.SC_OK:
         if (mimeType.equals("application/json")) {
           try {
             String body = EntityUtils.toString(entity);
             JSONArray arrayJson = new JSONArray(body);
             List<Queue> queues = new ArrayList<Queue>(arrayJson.length());
             try {
               // Assume the response is an array of JSON Objects.
               for (int idx = 0; idx < arrayJson.length(); idx++) {
                 JSONObject queueJson = arrayJson.getJSONObject(idx);
                 Queue queue = new QueueResponse(account, queueJson);
                 queues.add(idx, queue);
               }
             } catch (JSONException e) {
               // The response was not an array of JSON Objects. Try again,
               // assuming it was an array of strings.
               for (int idx = 0; idx < arrayJson.length(); idx++) {
                 String queueId = arrayJson.getString(idx);
                 Queue queue = new QueueResponse(account, queueId);
                 queues.add(idx, queue);
               }
             }
             return queues;
           } catch (IOException e) {
             try {
               // Consume the entity to release HttpClient resources.
               EntityUtils.consume(entity);
             } catch (IOException e1) {
               // If we ever stop throwing an exception in the outside block,
               // this needs to be handled.
             }
             // TODO: Throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("IOException reading http response");
           } catch (JSONException e) {
             // It is not necessary to consume the entity because at the
             // first point where this exception can be thrown,
             // the entity has already been consumed by toString();
             // TODO: throw something appropriate.
             e.printStackTrace();
             throw new RuntimeException("JSONException reading response");
           }
         } else {
           // This situation cannot be handled.
           try {
             // Consume the entity to release HttpClient resources.
             EntityUtils.consume(entity);
           } catch (IOException e1) {
             // If we ever stop throwing an exception in the outside block,
             // this needs to be handled.
           }
           // TODO: Throw something appropriate.
           throw new RuntimeException("Non-Json response");
         }
       case HttpStatus.SC_NOT_FOUND:
         // This is not necessarily an error, but we must throw something.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e1) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         throw new NoSuchAccountException();
       default:
         // This is probably an error.
         try {
           // Consume the entity to release HttpClient resources.
           EntityUtils.consume(entity);
         } catch (IOException e1) {
           // If we ever stop throwing an exception in the outside block,
           // this needs to be handled.
         }
         // TODO: Throw something appropriate.
         throw new RuntimeException("Unhandled http response code " + status.getStatusCode());
     }
   }
 }
 
