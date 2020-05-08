 package be.virtualsushi.wadisda.services;
 
 import org.apache.tapestry5.ioc.Configuration;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.ioc.annotations.Contribute;
 import org.apache.tapestry5.ioc.annotations.Marker;
 import org.apache.tapestry5.ioc.annotations.Symbol;
 import org.apache.tapestry5.ioc.services.ApplicationDefaults;
 import org.apache.tapestry5.services.HttpServletRequestFilter;
 import org.tynamo.security.Security;
 import org.tynamo.security.SecuritySymbols;
 import org.tynamo.security.services.SecurityFilterChainFactory;
 import org.tynamo.security.services.impl.SecurityFilterChain;
 
 import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
 import com.google.api.client.auth.oauth2.CredentialStore;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.common.collect.ImmutableList;
 
 public class SecurityModule {
 
 	@ApplicationDefaults
 	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
 		configuration.add(SecuritySymbols.LOGIN_URL, "/login");
 	}
 
 	@Contribute(HttpServletRequestFilter.class)
 	@Marker(Security.class)
 	public static void setupSecurity(Configuration<SecurityFilterChain> configuration, SecurityFilterChainFactory factory) {
 		configuration.add(factory.createChain("/assets/**").add(factory.anon()).build());
 		configuration.add(factory.createChain("/login*/**").add(factory.anon()).build());
 		configuration.add(factory.createChain("/openid/**").add(factory.anon()).build());
 		configuration.add(factory.createChain("/**").add(factory.authc()).build());
 	}
 
 	public AuthorizationCodeFlow buildAuthorizationCodeFlow(@Symbol("google.client.id") String clientId, @Symbol("google.client.secret") String clientSecret) {
 		return new GoogleAuthorizationCodeFlow.Builder(new ApacheHttpTransport(), new JacksonFactory(), clientId, clientSecret, ImmutableList.of("https://www.googleapis.com/auth/userinfo.profile",
 				"https://www.googleapis.com/auth/userinfo.email")).setCredentialStore(null).build();
 	}
 	
 	public CredentialStore buildCredentialStore(){
		
 	}
 	
 }
