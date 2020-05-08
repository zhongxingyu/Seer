 package com.mob.www.platform.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
 import org.apache.http.impl.nio.conn.PoolingAsyncClientConnectionManager;
 import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
 import org.apache.http.nio.reactor.IOReactorException;
 import org.apache.log4j.Logger;
 
 import com.mob.commons.plugins.servicemodel.PluginDataCall;
 import com.mob.commons.plugins.servicemodel.PluginDefinition;
 import com.mob.commons.plugins.servicemodel.PluginPage;
 import com.mob.commons.plugins.servicemodel.ServiceAlias;
 import com.mob.www.platform.controller.PlatformController;
 import com.mob.www.platform.model.DataCallResponse;
 
 public class ExternalServiceContract implements IServiceContracts {
 	private static final String SESSION_EXTERNALS = "externalservices";
 	private static final int STATUS_OK = 200;
 	private DefaultHttpAsyncClient httpClient = null;
 	
 	public void setExternalServices(PluginPage page, HttpSession session)
 	{
 		Map<String,String> externalServices = new HashMap<String,String>();
 		
 		for(PluginDefinition plugin : page.getPlugins())
 		{
 			if(plugin.getServiceAliases() != null)
 			{
 				for(ServiceAlias service : plugin.getServiceAliases())
 				{
 					String key = plugin.getRole() + "/" + service.getName();
 					if(!externalServices.containsKey(key))
 					{
 						externalServices.put(key, service.getEndpoint());
 					}
 				}
 			}
 		}
 		
 		session.setAttribute(SESSION_EXTERNALS, externalServices);
 	}
 	
 	public boolean isCallAllowed(HttpSession session, String serviceCall)
 	{
 		String alias = serviceCall;
 		int firstSlash = serviceCall.indexOf('/');
 		if(firstSlash >= 0 && serviceCall.indexOf('/', firstSlash + 1) > 0)
 		{
 			int secondSlash = serviceCall.indexOf('/', firstSlash + 1);
 			alias = serviceCall.substring(0, secondSlash);
 		}
 		
 		Object sessionObject = session.getAttribute(SESSION_EXTERNALS);
 		if(sessionObject instanceof Map)
 		{
 			@SuppressWarnings("unchecked")
 			Map<String,String> externalServices = (Map<String,String>)sessionObject;
 			
 			if(externalServices != null)
 			{
 				return externalServices.containsKey(alias);
 			}
 		}
 		
 		return false;
 	}
 
 	public HttpResponse callServiceWithGet(HttpSession session, String serviceCall) {
 		HttpResponse retval = null;
 		
 		try {
 			retval = this.callServiceWithGetAsync(session, serviceCall).get();
 		} catch (InterruptedException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		} catch (ExecutionException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		}
 		
 		return retval;
 	}
 	
 	public Future<HttpResponse> callServiceWithGetAsync(HttpSession session, String serviceCall) {
 		Logger logger = Logger.getLogger(this.getClass());
 		
 		String alias = serviceCall;
 		String params = null;
 		int firstSlash = serviceCall.indexOf('/');
 		if(firstSlash >= 0 && serviceCall.indexOf('/', firstSlash + 1) > 0)
 		{
 			int secondSlash = serviceCall.indexOf('/', firstSlash + 1);
 			alias = serviceCall.substring(0, secondSlash);
 			params = serviceCall.substring(secondSlash, serviceCall.length());
 		}
 		
 		
 		String endpoint = getEndpointFromAlias(session, alias);
 		if(endpoint == null)
 		{
 			logger.error("Could not find the specified alias " + alias);
 			return null;
 		}
 		
 		if(params != null)
 		{
 			endpoint += params;
 		}
 		
 		this.getConnection();
 		
 		Future<HttpResponse> response = null;
 		response = this.httpClient.execute(new HttpGet(endpoint), null);
 		
 		return response;
 	}
 
 	@Override
 	public HttpResponse callServiceWithPost(HttpSession session, String alias, String data, String contentType) {
 		HttpResponse retval = null;
 		
 		try {
 			retval = this.callServiceWithPostAsync(session, alias, data, contentType).get();
 		} catch (InterruptedException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		} catch (ExecutionException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		}
 		
 		return retval;
 	}
 	
 	public Future<HttpResponse> callServiceWithPostAsync(HttpSession session, String alias, String data, String contentType) {
 		Logger logger = Logger.getLogger(this.getClass());
 		
 		String endpoint = getEndpointFromAlias(session, alias);
 		if(endpoint == null)
 		{
 			logger.error("Could not find the specified alias " + alias);
 			return null;
 		}
 		
 		this.getConnection();
 		Future<HttpResponse> response = null;
 		try {
 			HttpPost postCommand = new HttpPost(endpoint);
 			StringEntity entity = new StringEntity(data);
 			entity.setContentType(contentType);
 			postCommand.setEntity(entity);
 			response = this.httpClient.execute(postCommand, null);
 		} catch (IOException e) {
 			logger.warn(e);
 		}
 		
 		return response;
 	}
 	
 	@Override
 	public HttpResponse callServiceWithPut(HttpSession session, String alias, String data, String contentType)
 	{
 		HttpResponse retval = null;
 		
 		try {
 			retval = this.callServiceWithPutAsync(session, alias, data, contentType).get();
 		} catch (InterruptedException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		} catch (ExecutionException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		}
 		
 		return retval;
 	}
 	
 	public Future<HttpResponse> callServiceWithPutAsync(HttpSession session, String alias, String data, String contentType) 
 	{
 		Logger logger = Logger.getLogger(this.getClass());
 		
 		String endpoint = getEndpointFromAlias(session, alias);
 		if(endpoint == null)
 		{
 			logger.error("Could not find the specified alias " + alias);
 			return null;
 		}
 		
 		this.getConnection();
 		Future<HttpResponse> response = null;
 		try {
 			HttpPut postCommand = new HttpPut(endpoint);
 			StringEntity entity = new StringEntity(data);
 			entity.setContentType(contentType);
 			postCommand.setEntity(entity);
 			response = this.httpClient.execute(postCommand, null);
 		} catch (IOException e) {
 			logger.warn(e);
 		}
 		
 		return response;
 	}
 	
 	@Override
 	public HttpResponse callServiceWithDelete(HttpSession session, String alias, String data, String contentType)
 	{
 		HttpResponse retval = null;
 		
 		try {
 			retval = this.callServiceWithDeleteAsync(session, alias, data, contentType).get();
 		} catch (InterruptedException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		} catch (ExecutionException e) {
 			Logger.getLogger(this.getClass()).warn(e);
 		}
 		
 		return retval;
 	}
 	
 	public Future<HttpResponse> callServiceWithDeleteAsync(HttpSession session, String alias, String data, String contentType) 
 	{
 		Logger logger = Logger.getLogger(this.getClass());
 		
 		String endpoint = getEndpointFromAlias(session, alias);
 		if(endpoint == null)
 		{
 			logger.error("Could not find the specified alias " + alias);
 			return null;
 		}
 		
 		this.getConnection();
 		Future<HttpResponse> response = null;
 		HttpDelete deleteCommand = new HttpDelete(endpoint);
 		deleteCommand.setHeader("data", data);
 		response = this.httpClient.execute(deleteCommand, null);
 		
 		return response;
 	}
 
 	@Override
 	public DataCallResponse[] callDataCalls(PluginDataCall[] dataCallInfo, HttpSession session, String userToken) {
 		List<DataCallResponse> retval = new LinkedList<DataCallResponse>();
 		Logger logger = Logger.getLogger(this.getClass());
 		
 		if(dataCallInfo != null)
 		{
 			for(PluginDataCall call : dataCallInfo)
 			{
 				if(this.isCallAllowed(session, call.getUri()))
 				{
 					String method = call.getMethod().toLowerCase();
 					String uri = call.getUri().replace("${usertoken}", userToken);
 					Future<HttpResponse> response = null;
 					if(method.equals("post"))
 					{
 						logger.debug("calling service with post" + uri + " , with data " + call.getContent());
 						response = this.callServiceWithPostAsync(session, uri, call.getContent(), call.getContentType());
 					}
 					else if(method.equals("get"))
 					{
 						logger.debug("calling service with get" + call.getUri());
 						response = this.callServiceWithGetAsync(session, call.getUri());
 					}
 					else if(method.equals("put"))
 					{
 						logger.debug("calling service with put" + call.getUri());
 						response = this.callServiceWithPutAsync(session, uri, call.getContent(), call.getContentType());
 					}
 					else if(method.equals("delete"))
 					{
 						logger.debug("calling service with delete" + call.getUri());
 						response = this.callServiceWithDeleteAsync(session, uri, call.getContent(), call.getContentType());
 					}
 					
 					DataCallResponse item = new DataCallResponse()
 												.setDataCallInfo(call)
 												.setResponse(response);
 
 					retval.add(item);
 				}
 			}
 			
 			for(DataCallResponse item : retval)
 			{
 				String data = "";
 				HttpResponse response = null;
 				try {
 					response = item.getResponse().get();
 				} catch (InterruptedException e2) {
 					logger.warn(e2);
 				} catch (ExecutionException e2) {
 					logger.warn(e2);
 				}
 				
 				if(item.getResponse() != null && response.getStatusLine().getStatusCode() == STATUS_OK)
 				{
 					InputStream serviceStream = null;
 					try {
 						serviceStream = response.getEntity().getContent();
 						StringWriter writer = new StringWriter();
 						IOUtils.copy(serviceStream, writer);
 						data = writer.toString();
 					} catch (IOException e) {
 						logger.error("An error occured while reading the response stream, check the network connection");
 						logger.info(e);
 					} catch (IllegalStateException e1) {
 						logger.error("An error occured while reading the response stream, check the network connection");
 						logger.info(e1);
 					}
 					
 					if(serviceStream != null)
 					{
 						try {
 							serviceStream.close();
 						} catch (IOException e) {
 						}
 					}
 					
 					item.setData(data);
 				}
 			}
 		}
 		return retval.toArray(new DataCallResponse[retval.size()]);
 	}
 	
 	private String getEndpointFromAlias(HttpSession session, String alias)
 	{
 		Object sessionObject = session.getAttribute(SESSION_EXTERNALS);
 		if(!(sessionObject instanceof Map))
 		{
 			return null;
 		}
 		@SuppressWarnings("unchecked")
 		Map<String,String> externalServices = (Map<String,String>)sessionObject;
 		
 		if(externalServices == null || !externalServices.containsKey(alias))
 		{
 			return null;
 		}
 		
 		String retval = externalServices.get(alias);
 		
 		if(retval != null)
 		{
 			String token = (String)session.getAttribute(PlatformController.SESSION_USER_TOKEN);
 			retval = retval.replace("${usertoken}", token);
 		}
 		
 		return retval;
 	}
 	
 	private void getConnection()
 	{
 		if(this.httpClient == null)
 		{
 			synchronized(this)
 			{
 				if(this.httpClient == null)
 				{
 					DefaultHttpAsyncClient client;
 					try {
 						DefaultConnectingIOReactor defaultioreactor = new DefaultConnectingIOReactor();
 						PoolingAsyncClientConnectionManager pool = new PoolingAsyncClientConnectionManager(defaultioreactor);
 
 						client = new DefaultHttpAsyncClient(pool);
 						client.start();
 
 						this.httpClient = client;
 					} catch (IOReactorException e) {
 						Logger logger = Logger.getLogger(this.getClass());
 						logger.error("Creating the http communication layer failed");
 						logger.info(e);
 					}
 				}
 			}
 		}
		
 	}
 }
