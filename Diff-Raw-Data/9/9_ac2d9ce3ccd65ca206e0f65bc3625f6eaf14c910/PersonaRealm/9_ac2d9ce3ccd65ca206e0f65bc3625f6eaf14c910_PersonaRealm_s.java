 package com.leonti.quotes.auth;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.SimpleAuthenticationInfo;
 import org.apache.shiro.realm.AuthenticatingRealm;
 import org.codehaus.jettison.json.JSONException;
 
 import com.leonti.quotes.auth.BrowserIdResponse.Status;
 import com.leonti.quotes.services.UserService;
 
 public class PersonaRealm extends AuthenticatingRealm {
 
 	private final UserService userService;
 	private final BrowserIdVerifier browserIdVerifier = new BrowserIdVerifier();
 	private String audience;
 
 	@Inject
 	public PersonaRealm(UserService userService,
 			@Named("audience") String audience) {
 		super.setAuthenticationTokenClass(PersonaToken.class);
 
 		this.userService = userService;
 		this.audience = audience;
 	}
 
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(
 			AuthenticationToken token) throws AuthenticationException {
 
 		String assertion = (String) token.getCredentials();
 		
 		String email = verify(assertion);
 		
 		// DEVELOPMENT ONLY
 		//String email = "dev@test.com";
 		
 		if (userService.readByEmail(email) == null) {
 			userService.create(email);
 		}
 
 		return new SimpleAuthenticationInfo(email, token.getCredentials(), "persona");
 	}
 
 	private String verify(String assertion) {
 
 		try {
 			BrowserIdResponse response = browserIdVerifier.verify(assertion,
 					audience);
 
 			if (response.getStatus() == Status.FAILURE) {
 				throw new AuthenticationException(
 						"Could not verify Persona assertion: "
 								+ response.getReason());
 			}
 
 			return response.getEmail();
 		} catch (IOException | JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
