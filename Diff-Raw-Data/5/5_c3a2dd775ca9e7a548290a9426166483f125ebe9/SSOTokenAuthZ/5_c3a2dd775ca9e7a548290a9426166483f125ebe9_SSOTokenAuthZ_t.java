 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SSOTokenAuthZ.java,v 1.2 2009-11-19 19:29:09 veiming Exp $
  */
 
 package com.sun.identity.rest.spi;
 
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.delegation.DelegationEvaluator;
 import com.sun.identity.delegation.DelegationPermission;
 import com.sun.identity.entitlement.opensso.SubjectUtils;
 import com.sun.identity.rest.ISubjectable;
 import com.sun.identity.rest.RestException;
 import com.sun.identity.rest.RestServiceManager;
 import com.sun.identity.shared.encode.Hash;
 import java.io.IOException;
 import java.security.Principal;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.security.auth.Subject;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.WebApplicationException;
 
 /**
  *
  * @author dennis
  */
 public class SSOTokenAuthZ implements IAuthorization {
     private static Map<String, String> mapMethodToAction =
         new HashMap<String, String>();
 
     static {
         mapMethodToAction.put("GET", "READ");
         mapMethodToAction.put("DELETE", "MODIFY");
         mapMethodToAction.put("POST", "MODIFY");
         mapMethodToAction.put("PUT", "MODIFY");
     }
 
     public String[] accept() {
         String[] method = { RestServiceManager.DEFAULT_AUTHZ_SCHEME };
         return method;
     }
 
     public void doFilter(
         ServletRequest request,
         ServletResponse response,
         FilterChain chain
     ) throws IOException, ServletException {
         Principal clientPrincipal = ((HttpServletRequest)request).
             getUserPrincipal();
         if (clientPrincipal instanceof ISubjectable) {
             try {
                 Subject clientSubject =
                     ((ISubjectable) clientPrincipal).createSubject();
                 DelegationEvaluator eval = new DelegationEvaluator();
                 SSOToken token = SubjectUtils.getSSOToken(clientSubject);
 
                 DelegationPermission permission = new DelegationPermission(
                    "/", "sunEntitlementService", "1.0", "application",
                     getURI(request), getAction(request), null);
                 if (!eval.isAllowed(token, permission,
                     Collections.EMPTY_MAP)) {
                     throw new WebApplicationException(
                         HttpServletResponse.SC_UNAUTHORIZED);
                 }
             } catch (Exception e) {
                 throw new WebApplicationException(
                     HttpServletResponse.SC_BAD_REQUEST);
             }
         } else {
             throw new WebApplicationException(
                 HttpServletResponse.SC_BAD_REQUEST);
         }
        
 
         validateTokenId((HttpServletRequest)request,
             (HttpServletResponse)response);
         chain.doFilter(request, response);
     }
 
     private String getURI(ServletRequest req) {
         String uri = ((HttpServletRequest)req).getRequestURI();
         int idx = uri.indexOf('/', 1);
         return (idx != -1) ? uri.substring(idx+1) : uri;
     }
 
     private Set<String> getAction(ServletRequest req)
         throws WebApplicationException {
         String action = mapMethodToAction.get((
             (HttpServletRequest)req).getMethod());
         Set<String> set = new HashSet<String>();
         if (action == null) {
             throw new WebApplicationException(
                 HttpServletResponse.SC_UNAUTHORIZED);
         }
         set.add(action);
         return set;
     }
 
     private void validateTokenId(
         HttpServletRequest request,
         HttpServletResponse response
     ) throws ServletException, IOException {
         String tokenId = request.getHeader(
             RestServiceManager.SUBJECT_HEADER_NAME);
 
         if ((tokenId == null) || (tokenId.trim().length() == 0)) {
             try {
                 SSOTokenManager mgr = SSOTokenManager.getInstance();
                 SSOToken token = mgr.createSSOToken(request);
                 tokenId = token.getTokenID().toString();
             } catch (SSOException e) {
                 throw new WebApplicationException(
                     HttpServletResponse.SC_BAD_REQUEST);
             }
         }
 
 
         if (!Boolean.parseBoolean(SystemProperties.get(
             RestServiceManager.DISABLE_HASHED_SUBJECT_CHECK, "false"))) {
             String hashed = request.getParameter(
                 RestServiceManager.HASHED_SUBJECT_QUERY);
 
             if ((hashed == null) || (hashed.trim().length() == 0)) {
                 throw new WebApplicationException(
                     HttpServletResponse.SC_BAD_REQUEST);
             } else {
                 int idx = tokenId.indexOf(':');
                 if (idx != -1) {
                     tokenId = tokenId.substring(idx + 1);
                 }
                 if (!Hash.hash(tokenId).equals(hashed)) {
                     throw new WebApplicationException(
                         HttpServletResponse.SC_BAD_REQUEST);
                 }
             }
         }
     }
 
     public void init(FilterConfig arg0) throws ServletException {
     }
 
     public void destroy() {
     }
 
     public Subject getAuthZSubject(HttpServletRequest req)
         throws RestException {
         try {
             String tokenId = req.getHeader(
                 RestServiceManager.SUBJECT_HEADER_NAME);
 
             if ((tokenId == null) || (tokenId.trim().length() == 0)) {
                 SSOTokenManager mgr = SSOTokenManager.getInstance();
                 SSOToken token = mgr.createSSOToken(req);
                 return SubjectUtils.createSubject(token);
             } else {
                 int idx = tokenId.indexOf(':');
                 if (idx != -1) {
                     tokenId = tokenId.substring(idx + 1);
                 }
                 SSOTokenManager mgr = SSOTokenManager.getInstance();
                 SSOToken token = mgr.createSSOToken(tokenId);
                 return SubjectUtils.createSubject(token);
             }
         } catch (SSOException ex) {
             throw new RestException(1, ex);
         }
     }
 }
