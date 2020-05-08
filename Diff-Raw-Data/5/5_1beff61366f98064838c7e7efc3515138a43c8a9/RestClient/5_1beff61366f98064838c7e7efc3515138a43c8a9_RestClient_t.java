 package com.roobit.android.restclient;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import com.roobit.android.restclient.RestClientRequestTask.RestClientRequestListener;
 
 import android.net.Uri;
 
 public class RestClient implements RestClientRequestListener {
 
 	public interface OnCompletionListener {
 		public void success(RestClient client, RestResult result);
 		public void failedWithError(RestClient restClient, int responseCode, RestResult result);
 	}
 
 	public enum Operation { GET, POST, PUT, DELETE, PATCH };
 
 	String baseUrl;
 	String resource;
 	LinkedHashMap<String, String> queryParameters;
 	Properties httpHeaders;
 	Properties parameters;
 	ByteArrayOutputStream postData;
 	Operation operation;
 	OnCompletionListener completionListener;
 	
 	static RestClient instance;
 	
 	public static RestClient sharedClient() {
 		return instance;
 	}
 	
 	public static void clearSharedClient() {
 		instance = null;
 		
 	}
 	public static RestClient clientWithBaseUrl(String baseUrl) {
 		RestClient client =  new RestClient(baseUrl);
 		if (instance == null) {
 			instance = client;
 		}
 		return client;
 	}
 
 	protected RestClient(String baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 
 	public String getBaseUrl() {
 		return baseUrl;
 	}
 	
 	public String getUrl() {
 		return buildUri().toString();
 	}
 
 	private Uri buildUri() {
 		Uri.Builder builder = Uri.parse(getBaseUrl())
 			.buildUpon()
 			.appendEncodedPath(getResource());
 		
 		if (queryParameters != null && !queryParameters.isEmpty()) {
 			Iterator<Entry<String, String>> iter = queryParameters.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<String, String> entry = iter.next();
 				builder.appendQueryParameter(entry.getKey(), entry.getValue());				
 			}
 		}
 		return builder.build();
 	}
 	
 	private String getResource() {
 		return resource;
 	}
 	
 	public RestClient setResource(String resource) {
 		this.resource = resource;
 		return this;
 	}
 	
 	public RestClient setQueryParameters(LinkedHashMap<String,String> queryParameters) {
 		this.queryParameters = queryParameters;
 		return this;
 	}
 	
 	public RestClient execute(OnCompletionListener completionListener) {
 		this.completionListener = completionListener;
 		new RestClientRequestTask(this).execute(getOperation(), buildUri(), httpHeaders, parameters, postData);
 		return this;
 	}
 	
 	/**
 	 * For clients managing their own threads, provide a synchronous method.
 	 * 
 	 * @return the result of the request.
 	 */
 	public RestResult synchronousExecute() {
 		return RestClientRequest.synchronousExecute(getOperation(), buildUri(), httpHeaders, parameters, postData);
 	}
 
 
 	private Operation getOperation() {
 		if (operation == null) {
 			operation = Operation.GET;
 		}
 		return operation;
 	}
 	
 	public RestClient get() {
 		operation = Operation.GET;
 		setHttpHeaders(null);
 		setParameters(null);
 		return this;
 	}
 	
 	public RestClient get(Properties headers) {
 		get();
 		setHttpHeaders(headers);
 		return this;
 	}
 	
 	public RestClient get(Properties headers, Properties queryParams) {
 		get(headers);
 		setParameters(queryParams);
 		return this;
 	}
 	
 	public RestClient post() {
 		operation = Operation.POST;
 		setQueryParameters(null);
 		setHttpHeaders(null);
 		return this;
 	}
 
 	public RestClient post(Properties httpHeaders) {
 		post();
 		setHttpHeaders(httpHeaders);
 		return this;
 	}
 
 	public RestClient post(ByteArrayOutputStream postData) {
 		post(postData, "application/json", null);
 		return this;
 	}
 
 	public RestClient post(ByteArrayOutputStream postData, String contentType) {
 		post(postData, contentType, null);
 		return this;
 	}
 
 	public RestClient post(ByteArrayOutputStream postData, String contentType, Properties httpHeaders) {
 		post(httpHeaders == null ? new Properties() : httpHeaders);
 		setPostData(postData);
 		setContentType(contentType);
 		return this;
 	}
 
 	public RestClient postForm(Properties parameters) {
 		post();
 		setParameters(parameters);
 		return this;
 	}
 	
 	public RestClient patch() {
 		operation = Operation.PATCH;
 		setQueryParameters(null);
 		setHttpHeaders(new Properties());
 		return this;
 	}
 
 	private void setHttpHeaders(Properties httpHeaders) {
 		this.httpHeaders = httpHeaders;
 	}
 	
 	private void setParameters(Properties parameters) {
 		this.parameters = parameters;
 	}
 
 	private void setContentType(String contentType) {
 		if(contentType != null && !httpHeaders.contains("content-type")) {
 			httpHeaders.put("content-type", contentType);
 		}
 	}
 
 	private void setPostData(ByteArrayOutputStream postData) {
 		this.postData = postData;
 	}
 
 	public void requestStarted() {
 		// TODO Auto-generated method stub		
 	}
 
 	public void requestCancelled() {
 		// TODO Auto-generated method stub
 	}
 
 	public void requestFinished(RestResult result) {
 		if (completionListener != null) {
 			if(result.isSuccess()) {
 				completionListener.success(this, result);
 			} else {
 				completionListener.failedWithError(this, result.getResponseCode(), result);
 			}
 		}
 	}
 
 }
