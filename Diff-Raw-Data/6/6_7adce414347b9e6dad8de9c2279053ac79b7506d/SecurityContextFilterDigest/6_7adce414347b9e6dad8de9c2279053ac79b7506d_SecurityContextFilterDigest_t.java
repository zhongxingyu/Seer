 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.filter;
 
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerRequestFilter;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.security.Principal;
 import java.util.Collection;
 import java.util.List;
 import javax.servlet.ServletContext;
 import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.ext.Provider;
 import org.apache.commons.lang3.Validate;
 import org.lafayette.server.Registry;
 import org.lafayette.server.ServerContextListener;
 import org.lafayette.server.domain.Role;
 import org.lafayette.server.domain.User;
 import org.lafayette.server.http.ForbiddenException;
 import org.lafayette.server.http.UnauthorizedException;
 import org.lafayette.server.http.digest.AuthorizationHeaderParser;
 import org.lafayette.server.http.digest.Digest;
 import org.lafayette.server.http.digest.RequestParameters;
 import org.lafayette.server.http.digest.ResponseParameters;
 import org.lafayette.server.log.Log;
 import org.lafayette.server.log.Logger;
 
 /**
  * Filters resource request/response for digest authentication.
  *
  * If a user is authorized then a {@link User} representing the user is injected as {@link Principal} to the
  * {@link SecurityContext}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Provider
class SecurityContextFilterDigest implements SecuirityContextFilter {
 
     /**
      * Logger facility.
      */
     private final Logger log = Log.getLogger(this);
     /**
      * The servlet context to get registry from.
      */
     private final ServletContext servlet;
     /**
      * The request's HTTP headers.
      */
     private final HttpHeaders headers;
 
     /**
      * Dedicated constructor.
      *
      * @param servlet must not be {2code null}
      * @param headers must not be {2code null}
      */
     public SecurityContextFilterDigest(final ServletContext servlet, final HttpHeaders headers) {
         super();
         Validate.notNull(servlet, "Servlet context must not be null!");
         this.servlet = servlet;
         Validate.notNull(headers, "HTTP headers must not be null!");
         this.headers = headers;
     }
 
     /**
      * Get global registry from servlet context.
      *
      * @return always same instance
      */
     private Registry registry() {
         return (Registry) servlet.getAttribute(ServerContextListener.REGISRTY);
     }
 
     @Override
     public ContainerRequestFilter getRequestFilter() {
         return this;
     }
 
     @Override
     public ContainerResponseFilter getResponseFilter() {
         return this;
     }
 
     @Override
     public ContainerRequest filter(final ContainerRequest request) {
         log.debug("Start authentication");
         final String header = getAuthorizationHeader();
 
         if (header.isEmpty()) {
             log.debug("Request doesn't have authentication header! Send authentication response.");
             final ResponseParameters params = new ResponseParameters();
             params.setRealm(registry().getServerConfig().getSecurotyRealm());
             params.setNonce(registry().getNongeGenerator().getNext());
             throw new UnauthorizedException(params);
         } else {
             log.debug("Verify authentication.");
             final RequestParameters params = AuthorizationHeaderParser.parseDigestHeaderValue(header);
 
             if (verifyAuthentiaction(params)) {
                 createAndSetPrincipal(request, params);
                 return request;
             } else {
                 log.debug("Not authorized! Send forbidden response.");
                 throw new ForbiddenException();
             }
         }
     }
 
     @Override
     public ContainerResponse filter(final ContainerRequest request, final ContainerResponse response) {
         return response;
     }
 
     /**
      * Extracts the authorization header from the requests headers.
      *
      * @return may return empty string, but never {@code null}
      */
     private String getAuthorizationHeader() {
         final List<String> authHeader = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
 
         if (authHeader == null || authHeader.isEmpty()) {
             log.debug("No authorization header sent by client.");
             return "";
         }
 
         if (authHeader.size() > 1) {
             log.warn("More than one authorization header sent by client! Using first one and ignore others.");
         }
 
         return authHeader.get(0);
     }
 
     private boolean verifyAuthentiaction(final RequestParameters params) {
         final User principal = findPrincipal(params);
 
         if (null == principal) {
             return false;
         }
 
         // FIXME Verify nonce
         final String expectedResponse = Digest.digest(principal.getHashedUserData(),
                 params.getNonce(),
                 Digest.digestRequestData(params.getHttpMethod(), params.getRequestedUri()));
         return expectedResponse.equalsIgnoreCase(params.getResponse());
     }
 
     private void createAndSetPrincipal(final ContainerRequest request, final RequestParameters params) {
         final User principal = findPrincipal(params);
         final Collection<Role> roles = registry().getMappers()
                 .createRoleMapper()
                 .findByUserId(principal.getId());
         principal.addAllRoles(roles);
         request.setSecurityContext(new SecurityContextImpl(principal, request.isSecure()));
     }
 
     private User findPrincipal(final RequestParameters params) {
         return registry().getMappers()
                 .createUserMapper()
                 .findByLoginName(params.getUsername());
     }
 }
