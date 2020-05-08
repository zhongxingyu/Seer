 package org.sopeco.webui.server.rest;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.WebTarget;
 
 import org.glassfish.jersey.client.ClientConfig;
 import org.sopeco.service.rest.json.CustomObjectMapper;
 
 import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
 
 /**
 * This class is used to create a {@link WebTarget} with the Jersey {@link Client}.
 * As we don't want always to create a new {@link Client}, it's once created (singleton)
 * and reinitialized with {@link #getClient(String...)} with a given URL.
  * 
  * @author Peter Merkert
  */
 public class ClientFactory {
 
 	private static ClientFactory clientFactory 	= null;
 	private static Client client 				= null;
 	private static String URLprefix				= "http://localhost:8080";
 	private static String URLsplitter			= "/";
 	
 	/**
 	 * Private constructor for singleton.
 	 */
 	private ClientFactory() {
     	client = ClientBuilder.newClient(createClientConfig());
 	}
 	
 	/**
 	 * Singleton constructor to create a {@link ClientFactory} build 
 	 * {@link WebResource}s.
 	 * 
 	 * @return a {@link ClientFactory} to create a {@link WebResource}
 	 */
 	public static ClientFactory getInstance() {
 		
 		if (clientFactory == null) {
 			clientFactory = new ClientFactory();
 		}
 		
 		return clientFactory;
 		
 	}
 	
 	/**
	 * Returns the {@link WebTarget} to do requests. Already sticks the prefix 
 	 * <code>http://localhost:8080/</code> to the URL.
 	 * 
 	 * @param URL 	the URL where the service is
 	 * @return		the {@link WebResource} to do requests
 	 */
 	public WebTarget getClient(String... url) {
 		String myurl = "";
 		
 		for (String suburl : url) {
 			myurl += URLsplitter + suburl;
 		}
 		
 		return client.target(URLprefix + myurl);
 	}
 	
 	/////////////////////////////////////////////////////////////////////////////////////////////////
 	////////////////////////////////////////// HELPER ///////////////////////////////////////////////
 	/////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Sets the client config for the client. The method adds a special {@link CustomObjectWrapper}
 	 * to the normal Jackson wrapper for JSON.<br />
 	 * This method is called by the constructor.
 	 * 
 	 * @return ClientConfig to work with JSON
 	 */
 	private ClientConfig createClientConfig() {
 		ClientConfig config = new ClientConfig();
 		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
         provider.setMapper(new CustomObjectMapper());
         config.register(provider);
 	    return config;
 	}
 }
