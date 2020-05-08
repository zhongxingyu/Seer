 package no.tornado.brap.auth;
 
 import no.tornado.brap.common.InvocationRequest;
 import no.tornado.brap.exception.AuthenticationFailedException;
 
 /**
  * Simple <code>AuthenticationProvider</code> that can be pre-configured to
  * authenticate incoming method invocations with a single username/password combination.
  */
 public class SingleUsernamePasswordAuthenticator implements AuthenticationProvider {
     private String username;
     private String password;
 
     public SingleUsernamePasswordAuthenticator() {
     }
 
     public SingleUsernamePasswordAuthenticator(String username, String password) {
         this.username = username;
         this.password = password;
     }
 
     public void authenticate(InvocationRequest invocationRequest) throws AuthenticationFailedException {
         if (invocationRequest.getCredentials() != null && invocationRequest.getCredentials() instanceof UsernamePasswordPrincipal) {
             UsernamePasswordPrincipal upp = (UsernamePasswordPrincipal) invocationRequest.getCredentials();
             if (username.equals(upp.getUsername()) && password.equals(upp.getPassword()))
                 AuthenticationContext.setPrincipal(upp);
             else
                 throw new AuthenticationFailedException("Authentication failed");
 
        }
        throw new AuthenticationFailedException("Missing credentials");
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 }
