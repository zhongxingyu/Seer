 package org.purl.wf4ever.rosrs.client.users;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import org.purl.wf4ever.rosrs.client.ROSRSException;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Client for User Management 1 API.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public final class UserManagementService {
 
     /** RODL URI. */
     private URI rodlURI;
 
     /** RODL access token. */
     private String token;
 
     /** web client. */
     private Client client;
 
 
     /**
      * Constructor.
      * 
      * @param rodlURI
      *            RODL URI
      * @param token
      *            RODL access token
      */
     public UserManagementService(URI rodlURI, String token) {
         this.rodlURI = rodlURI;
         this.token = token;
         this.client = Client.create();
     }
 
 
     /**
      * Check if the user id exists.
      * 
      * @param userId
      *            user id
      * @return true if the user id is already taken
      */
     public boolean userExistsInDlibra(String userId) {
         WebResource webResource = client.resource(rodlURI.toString()).path("users")
                 .path(Base64.encodeBase64URLSafeString(userId.getBytes()));
         ClientResponse response = webResource.header("Authorization", "Bearer " + token).type("text/plain")
                 .get(ClientResponse.class);
         try {
             return response.getStatus() == HttpURLConnection.HTTP_OK;
         } finally {
             response.close();
         }
     }
 
 
     /**
      * Create a user.
      * 
      * @param openId
      *            the user OpenID
      * @param username
      *            nice name
      * @return RODL response
      */
     public ClientResponse createUser(String openId, String username) {
         String payload = username != null && !username.isEmpty() ? username : openId;
         WebResource webResource = client.resource(rodlURI.toString()).path("users")
                 .path(Base64.encodeBase64URLSafeString(openId.getBytes()));
         return webResource.header("Authorization", "Bearer " + token).type("text/plain")
                 .put(ClientResponse.class, payload);
     }
 
 
     /**
      * Delete a user from RODL.
      * 
      * @param userId
      *            .java RODL user
      * @return RODL response
      */
     public ClientResponse deleteUser(String userId) {
         WebResource webResource = client.resource(rodlURI.toString()).path("users")
                 .path(Base64.encodeBase64URLSafeString(userId.getBytes()));
         return webResource.header("Authorization", "Bearer " + token).type("text/plain").delete(ClientResponse.class);
     }
 
 
     /**
      * Get an OAuth client from RODL.
      * 
      * @param clientId
      *            client id
      * @return the OAuth client
      */
     public OAuthClient getClient(String clientId) {
         WebResource webResource = client.resource(rodlURI.toString()).path("clients")
                 .path(Base64.encodeBase64URLSafeString(clientId.getBytes()));
         return webResource.header("Authorization", "Bearer " + token).type("text/plain").get(OAuthClient.class);
     }
 
 
     /**
      * Get all clients from RODL.
      * 
      * @return a list of OAuth clients
      */
     public List<OAuthClient> getClients() {
         WebResource webResource = client.resource(rodlURI.toString()).path("clients").path("/");
         return webResource.header("Authorization", "Bearer " + token).type("text/plain").get(OAuthClientList.class)
                 .getList();
     }
 
 
     /**
      * Create an access token in RODL.
      * 
      * @param userId
      *            user id
      * @param clientId
      *            client id
      * @return the access token
      * @throws UniformInterfaceException
      *             when the RODL response status is different from 201
      */
     public String createAccessToken(String userId, String clientId)
             throws UniformInterfaceException {
         String payload = clientId + "\r\n" + userId;
         WebResource webResource = client.resource(rodlURI.toString()).path("accesstokens");
         ClientResponse response = webResource.header("Authorization", "Bearer " + token).type("text/plain")
                 .post(ClientResponse.class, payload);
         if (response.getStatus() == HttpURLConnection.HTTP_CREATED) {
             URI at = response.getLocation();
             response.close();
             String[] segments = at.getPath().split("/");
             return segments[segments.length - 1];
         } else {
             throw new UniformInterfaceException(response);
         }
     }
 
 
     /**
      * Get all access tokens belonging to a user.
      * 
      * @param userId
      *            user id
      * @return a list of {@link AccessToken}
      */
     public List<AccessToken> getAccessTokens(String userId) {
         WebResource webResource = client.resource(rodlURI.toString()).path("accesstokens")
                 .queryParam("user_id", userId);
         return webResource.header("Authorization", "Bearer " + token).type("text/plain").get(AccessTokenList.class)
                 .getList();
     }
 
 
     /**
      * Delete an access token.
      * 
      * @param accesstoken
      *            the token to delete
      * @return RODL response
      */
     public ClientResponse deleteAccessToken(String accesstoken) {
         WebResource webResource = client.resource(rodlURI.toString()).path("accesstokens").path(accesstoken);
         return webResource.header("Authorization", "Bearer " + token).type("text/plain").delete(ClientResponse.class);
     }
 
 
     /**
      * Return data about a RODL user.
      * 
      * @param userURI
      *            URI of the user in RODL
      * @return RDF graph input stream
      * @throws ROSRSException
      *             when the response code is not 2xx
      */
     public InputStream getUser(URI userURI)
             throws ROSRSException {
         WebResource webResource = client.resource(rodlURI.toString()).path("users")
                 .path(Base64.encodeBase64URLSafeString(userURI.toString().getBytes()));
         try {
             return webResource.get(InputStream.class);
         } catch (UniformInterfaceException e) {
             throw new ROSRSException(e.getLocalizedMessage(), e.getResponse().getStatus(), e.getResponse()
                     .getClientResponseStatus().getReasonPhrase());
         }
     }
 
 
     /**
      * Return data about the access token owner.
      * 
      * @return RDF graph input stream
      * @throws ROSRSException
      *             when the response code is not 2xx
      */
    public InputStream getWhoAmi()
             throws ROSRSException {
         WebResource webResource = client.resource(rodlURI.toString()).path("whoami");
         try {
             return webResource.header("Authorization", "Bearer " + token).get(InputStream.class);
         } catch (UniformInterfaceException e) {
             throw new ROSRSException(e.getLocalizedMessage(), e.getResponse().getStatus(), e.getResponse()
                     .getClientResponseStatus().getReasonPhrase());
         }
     }
 
 }
