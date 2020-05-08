 package ch.windmobile.server.security;
 
 import java.util.Arrays;
 
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.ProviderNotFoundException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 
 import ch.windmobile.server.socialmodel.AuthenticationService;
 import ch.windmobile.server.socialmodel.ServiceLocator;
 import ch.windmobile.server.socialmodel.ServiceLocator.ServiceLocatorException;
 
 public class WindMobileAuthenticationProvider implements AuthenticationProvider {
     public static final String roleAnonymous = "ROLE_ANONYMOUS";
     public static final String roleUser = "ROLE_USER";
     public static final GrantedAuthority roleAnonymousAuthority = new SimpleGrantedAuthority(roleAnonymous);
     public static final GrantedAuthority roleUserAuthority = new SimpleGrantedAuthority(roleUser);
 
     private AuthenticationService authenticationService;
 
     public WindMobileAuthenticationProvider(ServiceLocator serviceLocator) throws ServiceLocatorException {
         authenticationService = serviceLocator.connect(null).getService(AuthenticationService.class);
     }
 
     @Override
     public Authentication authenticate(Authentication authentication) throws AuthenticationException {
         if (authentication instanceof UsernamePasswordAuthenticationToken == false) {
             throw new ProviderNotFoundException("Only UsernamePasswordAuthenticationToken is supported");
         }
 
         UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
         try {
             authenticationService.authenticate((String) token.getPrincipal(), (String) token.getCredentials());
             return new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials(), Arrays.asList(roleUserAuthority));
         } catch (Exception e) {
             // Silently return ROLE_ANONYMOUS instead throwing an HTTP exception (401: Unauthorized) which will be
             // intercepted by the container
            return new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials(), Arrays.asList(roleAnonymousAuthority));
         }
     }
 
     @Override
     public boolean supports(Class<? extends Object> authentication) {
         return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
     }
 }
