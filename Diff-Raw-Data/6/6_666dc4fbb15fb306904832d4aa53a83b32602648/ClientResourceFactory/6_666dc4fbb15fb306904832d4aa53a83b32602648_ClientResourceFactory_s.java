 package ch.hsr.bieridee.android.http;
 
 import org.restlet.Client;
 import org.restlet.Context;
 import org.restlet.data.Protocol;
 import org.restlet.resource.ClientResource;
 
 /**
  * Factory to configure and create a ClientResource.
  */
 public final class ClientResourceFactory {
 
 	private ClientResourceFactory() {
 		// Do not instantiate
 	}
 	
 	/**
 	 * Return a new ClientResource for the given resource URI.
 	 * The ClientResource instance is async capable and properly configured.
 	 * 
 	 * @param resourceURI Resource URI string
 	 * @return ClientResource instance
 	 */
 	public static ClientResource getClientResource(String resourceURI) {
 		final Client client = new Client(new Context(), Protocol.HTTP);
 		final ClientResource cr = new ClientResource(resourceURI);
 		cr.setNext(client);
 		return cr;
 	}
 }
