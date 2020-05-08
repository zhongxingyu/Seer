package org.deegree.securityproxy.authorization;
 
 import org.deegree.securityproxy.authorization.RequestAuthorizationManager;
 import org.deegree.securityproxy.authorization.logging.AuthorizationReport;
 import org.deegree.securityproxy.request.OwsRequest;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.core.Authentication;
 
public class TestRequestAuthorizationManager implements RequestAuthorizationManager {
 
     public static final String SERVICE_URL = "url";
 
     private boolean isAuthorized = true;
 
    public TestRequestAuthorizationManager() {
     }
 
    public TestRequestAuthorizationManager( boolean isAuthorized ) {
         this.isAuthorized = isAuthorized;
     }
 
     @Override
     public AuthorizationReport decide( Authentication authentication, OwsRequest request )
                             throws AccessDeniedException, InsufficientAuthenticationException {
         return new AuthorizationReport( "", isAuthorized, SERVICE_URL );
     }
 
     @Override
     public boolean supports( Class<?> clazz ) {
         return true;
     }
 
 }
