 package amp.anubis.core;
 
 /**
  * Immutable Value Object which associates an identity to a token.
  */
 public class NamedToken {
 
 	//IMPORTANT; these fields must have these exact names in order to be properly 
 	//deserialzied from the JSON received from Anubis.
 	//E.g. Anubis response: {"token":"vyNjhlC2Spyqdj0EM6dBsw==","identity":"CN=AMP Test User, CN=Users, DC=openampere, DC=com"}
     private String identity;
     private String token;
 
 
     /**
      * Gets the identity attached to the token.
      * @return
      */
     public String getIdentity() { return identity; }
 
     /**
      * Gets the authentication token
      * @return
      */
     public String getToken() { return token; }
 
 
     /**
      * Constructs a NamedToken using the given identity and token.
      * @param identity
      * @param token
      */
     public NamedToken(String identity, String token) {
 
        this.identity = identity;
        this.token = token;
     }
 }
