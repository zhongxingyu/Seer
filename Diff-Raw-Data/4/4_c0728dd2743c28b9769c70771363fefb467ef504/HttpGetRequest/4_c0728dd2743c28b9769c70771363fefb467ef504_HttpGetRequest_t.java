 package com.grikly.request;
 
 import javax.ws.rs.core.MediaType;
 import com.google.gson.Gson;
 import com.grikly.JerseyUtil;
 import com.grikly.URL;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * HttpGetRequest is used to execute a HTTP
  * Request using the HTTP GET method. This class 
  * is thread-safe and cannot be sub-classed.
  * @author Mario Dennis
  *
  * @param <E>
  * @param <T>
  */
 public final class HttpGetRequest <E,T> extends HttpRequest<E, T> {
 	
 
 	/**
 	 * HttpGetRequest Default Constructor.
 	 * @author Mario Dennis
 	 * @param HttpBuilder<E, T>
 	 */
 	protected HttpGetRequest (HttpBuilder<E, T> builder)
 	{
 		super(builder);
 	}//end constructor 
 	
 	
 
 	/**
 	 * Executes HTTP GET Request to Grikly Server.
 	 * @author Mario Dennis
 	 * @return T
 	 */
 	public T execute() 
 	{
 		
 		if (getPath() == null)
 			throw new NullPointerException ("No Path was supplied");
 		
 		
 		Client client = JerseyUtil.getClient();
 		WebResource resource;
 		
 		resource = client.resource(String.format(URL.BASE.toString(),getPath()));
 		
 		ClientResponse response;
 		
 		//adds queryMap and authInfo when both are supplied
 		if (getQueryMap() != null && getAuthInfo() != null)
 			response = resource.queryParams(getQueryMap())
 							   .header("ApiKey",getApiKey())
 							   .header("Authorization","Basic " + getAuthInfo())
 							   .accept(MediaType.APPLICATION_JSON)
 							   .get(ClientResponse.class);
 		
 		//adds authInfo when supplied
		else if (getAuthInfo() != null)
 			response = resource.header("ApiKey",getApiKey())
 			   				   .header("Authorization","Basic " + getAuthInfo())
 			   				   .accept(MediaType.APPLICATION_JSON)
 			   				   .get(ClientResponse.class);
 		
 		else
 			response = resource.header("ApiKey",getApiKey()).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
 		if (response.getStatus() == 200)
 		{
 			
 			String result = response.getEntity(String.class);
 			Gson gson = new Gson();
 			return gson.fromJson(result, getType());
 			
 		}
 		System.err.println(response.getClientResponseStatus() + ": " + response.getStatus()  );
 		return null;
 	}//end execute method
 	
 }//end HttpGetRequest method
