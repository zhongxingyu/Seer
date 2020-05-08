 package be.virtualsushi.wadisda.services.security;
 
 import javax.inject.Inject;
 
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.SimpleAuthenticationInfo;
 import org.tynamo.security.federatedaccounts.services.FederatedAccountService;
 
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 
 import be.virtualsushi.wadisda.services.repository.UserRepository;
 
 public class GoogleFederatedAccountService implements FederatedAccountService {
 
 	@Inject
 	private UserRepository userRepository;
 
 	@Override
 	public AuthenticationInfo federate(String realmName, Object remotePrincipal, AuthenticationToken authenticationToken, Object remoteAccount) {
 		
		GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, clientSecrets, scopes);
		
 		System.out.println("called");
 		return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), authenticationToken.getCredentials(), realmName);
 	}
 
 }
