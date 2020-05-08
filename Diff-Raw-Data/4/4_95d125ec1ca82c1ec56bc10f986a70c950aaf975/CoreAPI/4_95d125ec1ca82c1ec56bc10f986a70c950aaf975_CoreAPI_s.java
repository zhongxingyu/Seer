 /**
  * Copyright (C) 2012 Maxim Gurkin <redmax3d@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ru.redcraft.pinterest4j.core.api;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import ru.redcraft.pinterest4j.exceptions.PinterestRuntimeException;
 
 import com.sun.jersey.api.client.AsyncWebResource;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.async.TypeListener;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.client.non.blocking.NonBlockingClient;
 import com.sun.jersey.client.non.blocking.config.DefaultNonBlockingClientConfig;
 import com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataBodyPart;
 
 public abstract class CoreAPI {
 
 	private final PinterestAccessToken accessToken; 
 	private final InternalAPIManager apiManager;
 	private Client client;
 	private NonBlockingClient asyncClient;
 	
 	private static final Logger LOG = Logger.getLogger(CoreAPI.class);
 	
 	private static final String PINTEREST_DOMAIN = "pinterest.com";
 	private static final String COOKIE_HEADER_NAME = "Cookie";
 	
 	protected static final String PINTEREST_URL = "http://" + PINTEREST_DOMAIN;
 	protected static final String RESPONSE_STATUS_FIELD = "status";
 	protected static final String RESPONSE_MESSAGE_FIELD = "message";
 	protected static final String RESPONSE_SUCCESS_STATUS = "success";
 	protected static final String BAD_SERVER_RESPONSE = " bad server response";
 	
 	protected static final String VALUE_TAG_ATTR = "value";
 	protected static final String CHECKED_TAG_ATTR = "checked";
 	protected static final String HREF_TAG_ATTR = "href";
 	protected static final String DATA_ID_TAG_ATTR = "data-id";
 	
 	protected static final Locale PINTEREST_LOCALE = Locale.ENGLISH;
 	
 	private static final int REPEATS_ON_ERROR = 4;
 	private static final long ERROR_WAIT_INTERVAL = 5000; 
 	
 	enum Protocol {HTTP, HTTPS};
 	
 	enum Method {GET, POST, DELETE};
 	
 	private static final Class<ClientResponse> RESPONSE_CLASS = ClientResponse.class;
 
 	class APIRequestBuilder {
 		private Protocol protocol = Protocol.HTTP;
 		private Method method = Method.GET;
 		private MediaType mediaType = null;
 		private Object requestEntity = null;
 		private final String url;
 		private boolean ajaxUsage = true;
 		private Map<Status, PinterestRuntimeException> exceptionMap = new HashMap<Status, PinterestRuntimeException>();
 		private Status httpSuccessStatus = Status.OK;
 		private String errorMessage = null;
 		
 		class APIResponse {
 			private final ClientResponse response;
 			
 			APIResponse(ClientResponse response) {
 				this.response = response;
 			}
 			
 			public ClientResponse getResponse() {
 				return response;
 			}
 			
 			public Document getDocument() {
 				String entety = response.getEntity(String.class);
 				return Jsoup.parse(entety);	
 			}
 			
 			public Map<String, String> parseResponse() {
 				return parseResponse(null, null);
 			}
 			
 			public Map<String, String> parseResponse(String errorMsg, PinterestRuntimeException exception) {
 				Map<String, String> resultMap = new HashMap<String, String>();
 				try{
 					JSONObject jResponse = new JSONObject(response.getEntity(String.class));
 					Iterator<?> responseIterator = jResponse.keys();
 					while(responseIterator.hasNext()) {
 						String key = (String) responseIterator.next();
 						resultMap.put(key, jResponse.getString(key));
 					}
 				} catch(JSONException e) {
 					String msg = errorMessage + e.getMessage();
 					throw new PinterestRuntimeException(response, msg, e);
 				}
 				if(!resultMap.get(RESPONSE_STATUS_FIELD).equals(RESPONSE_SUCCESS_STATUS)) {
 					if(resultMap.get(RESPONSE_MESSAGE_FIELD).equals(errorMsg)) {
 						throw exception;
 					}
 					else {
 						throw new PinterestRuntimeException(errorMessage + resultMap.get(RESPONSE_MESSAGE_FIELD));
 					}
 				}
 				return resultMap;
 			}
 		}
 		
 		APIRequestBuilder(String url) {
 			this.url = url;
 		}
 		
 		public APIRequestBuilder setProtocol(Protocol protocol) {
 			this.protocol = protocol;
 			return this;
 		}
 		public APIRequestBuilder setMethod(Method method) {
 			this.method = method;
 			return this;
 		}
 		public APIRequestBuilder setMethod(Method method, Object requestEntity) {
 			this.method = method;
 			this.requestEntity = requestEntity;
 			return this;
 		}
 		public APIRequestBuilder setHttpSuccessStatus(Status httpSuccessStatus) {
 			this.httpSuccessStatus = httpSuccessStatus;
 			return this;
 		}
 		public APIRequestBuilder setErrorMessage(String errorMessage) {
 			this.errorMessage = errorMessage;
 			return this;
 		}
 		public APIRequestBuilder addExceptionMapping(Status status, PinterestRuntimeException exception) {
 			exceptionMap.put(status, exception);
 			return this;
 		}
 		public APIRequestBuilder setMediaType(MediaType mediaType) {
 			this.mediaType = mediaType;
 			return this;
 		}
 		public APIRequestBuilder setAjaxUsage(boolean ajaxUsage) {
 			this.ajaxUsage = ajaxUsage;
 			return this;
 		}
 		
 		public APIResponse build() {
 			WebResource.Builder builder = getWR(protocol, url, ajaxUsage);
 			if(mediaType != null) {
 				builder = builder.type(mediaType);
 			}
 			ClientResponse response = null;
 			
 			boolean responseResieved = false;
 			int errorRepeats = REPEATS_ON_ERROR;
 			
 			while(!responseResieved) {
 				response = null;
 				try {
 					switch(method) {
 					case GET :
 						response = builder.get(RESPONSE_CLASS);
 						break;
 					case POST :
 						response = builder.post(RESPONSE_CLASS, requestEntity);
 						break;
 					case DELETE :
 						response = builder.delete(RESPONSE_CLASS);
 						break;
 					default :
 						throw new PinterestRuntimeException("Unknown HTTP method");
 					}
 				} catch(Exception e) {
 					LOG.error("ERROR in client request to Pinterest", e);
 				}
 				if((response == null || response.getStatus() >= Status.INTERNAL_SERVER_ERROR.getStatusCode()) && errorRepeats > 0) {
					LOG.error(String.format("ERROR in request to Pinterest with code=%d and message='%s'. Start repeat with counter=%d AND timeout=%d", response.getStatus(), response.getEntity(String.class), errorRepeats, ERROR_WAIT_INTERVAL));
 					-- errorRepeats;
 					try {
 						Thread.sleep(ERROR_WAIT_INTERVAL * (REPEATS_ON_ERROR - errorRepeats));
 					} catch (InterruptedException e) {
 						throw new PinterestRuntimeException(e.getMessage(), e);
 					}
 				}
 				else {
 					responseResieved = true;
 				}
 			}
 			
 			Status status = Status.fromStatusCode(response.getStatus());
 			if(!status.equals(httpSuccessStatus)) {
 				if(exceptionMap.containsKey(status)) {
 					throw exceptionMap.get(status);
 				}
 				else {
 					throw new PinterestRuntimeException(response, errorMessage + BAD_SERVER_RESPONSE);
 				}
 			}
 			return new APIResponse(response);
 		}
 		
 		public void buildAsync(TypeListener<ClientResponse> listener) {
 			AsyncWebResource.Builder builder = getAsyncWR(protocol, url, ajaxUsage);
 			if(mediaType != null) {
 				builder = builder.type(mediaType);
 			}
 			switch(method) {
 				case GET :
 					builder.get(listener);
 					break;
 				case POST :
 					builder.post(listener, requestEntity);
 					break;
 				case DELETE :
 					builder.delete(listener);
 					break;
 				default :
 					throw new PinterestRuntimeException("Unknown HTTP method");
 			}
 		}
 		
 	}
 	
 	CoreAPI(PinterestAccessToken accessToken, InternalAPIManager apiManager) {
 		this.accessToken = accessToken;
 		this.apiManager = apiManager;
 	}
 	
 	protected PinterestAccessToken getAccessToken() {
 		return accessToken;
 	}
 
 	protected InternalAPIManager getApiManager() {
 		return apiManager;
 	}
 
 	protected WebResource.Builder getWR(Protocol protocol, String url) {
 		return getWR(protocol, url, true);
 	}
 	
 	private ClientConfig getClientConfig() {
 		ClientConfig cc = new DefaultNonBlockingClientConfig();
 		cc.getProperties().put(NonBlockingClientConfig.PROPERTY_THREADPOOL_SIZE, 30);
 		cc.getProperties().put(NonBlockingClientConfig.PROPERTY_READ_TIMEOUT, 120000);
 		cc.getProperties().put(NonBlockingClientConfig.PROPERTY_CONNECT_TIMEOUT, 120000);
 		return cc;
 	}
 	
 	protected WebResource.Builder getWR(Protocol protocol, String url, boolean useAJAX) {
 		if(client == null) {
 			client = Client.create(getClientConfig());
 		}
 		WebResource.Builder wr = null;
 		String requestURL = String.format("%s://%s/%s", protocol.name().toLowerCase(PINTEREST_LOCALE), PINTEREST_DOMAIN, url);
 		wr = client.resource(UriBuilder.fromUri(requestURL).build()).getRequestBuilder();
 		wr.header("Referer", "https://pinterest.com/");
 		if(accessToken != null) {
 			wr = wr.header(COOKIE_HEADER_NAME, accessToken.generateCookieHeader());
 			wr = wr.header("X-CSRFToken", accessToken.getCsrfToken().getValue());
 			if(useAJAX) {
 				wr = wr.header("X-Requested-With", "XMLHttpRequest");
 			}
 		}
 		return wr;
 	}
 	
 	protected AsyncWebResource.Builder getAsyncWR(Protocol protocol, String url, boolean useAJAX) {
 		if(asyncClient == null) {
 			asyncClient = NonBlockingClient.create(getClientConfig());
 		}
 		AsyncWebResource.Builder wr = null;
 		String requestURL = String.format("%s://%s/%s", protocol.name().toLowerCase(PINTEREST_LOCALE), PINTEREST_DOMAIN, url);
 		wr = asyncClient.asyncResource(UriBuilder.fromUri(requestURL).build()).getRequestBuilder();
 		wr.header("Referer", "https://pinterest.com/");
 		if(accessToken != null) {
 			wr = wr.header(COOKIE_HEADER_NAME, accessToken.generateCookieHeader());
 			wr = wr.header("X-CSRFToken", accessToken.getCsrfToken().getValue());
 			if(useAJAX) {
 				wr = wr.header("X-Requested-With", "XMLHttpRequest");
 			}
 		}
 		return wr;
 	}
 	
 	public void close() {
 		if(asyncClient != null) {
 			asyncClient.close();
 		}
 	}
 	
 	protected FormDataBodyPart createImageBodyPart(File imgFile) {
 		String[] mimeInfo = new MimetypesFileTypeMap().getContentType(imgFile).split("/");
 		MediaType imageType = new MediaType(mimeInfo[0], mimeInfo[1]);
 		FormDataBodyPart f = new FormDataBodyPart(
 	                FormDataContentDisposition.name("img").fileName(imgFile.getName()).build(),
 	                imgFile, imageType);
 		return f;
 	}
 	
 	protected Form getSwitchForm(String parameter, boolean state) {
 		Form switchForm = new Form();
 		switchForm.add("bla", "bla");
 		if(!state) {
 			switchForm.add(parameter, 1);
 		}
 		return switchForm;
 	}
 	
 	
 }
