 /*
  * ConfluenceCasAuthenticator.java
  *
  * Created on Feb 12, 2007
  *
  * Copyright (C) 2006, 2007 Carl E Harris, Jr.
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  *
  * This software was derived substantially from the CasSeraphAuthenticator
  * class developed by Ingomar Otter (ingomar.otter@valtech.de) as modified by
  * Jason Shao (jayshao.rutgers.edu).  Thanks to the both of them for making
  * their work freely available to public.
  */
 
 package org.soulwing.cas.apps.atlassian;
 
 
 import java.security.Principal;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.soulwing.cas.client.ServiceValidationResponse;
 import org.soulwing.cas.support.ValidationUtils;
 
 import com.atlassian.confluence.user.ConfluenceAuthenticator;
 import com.atlassian.seraph.auth.AuthenticatorException;
 
 
 /**
  * Subclass of ConfluenceAuthenticator that provides CAS authentication for
  * Confluence.  This authenticator can be plugged into 
  * <code>seraph-config.xml</code> to enable CAS authentication for
  * Confluence.
  *
  * @author Carl Harris
  * 
  */
 public class ConfluenceCasAuthenticator extends ConfluenceAuthenticator {
 
   private static final long serialVersionUID = 1L;
   private static final Log log = 
       LogFactory.getLog(ConfluenceCasAuthenticator.class);
 
   /**
    * Gets the logged in user as a <code>CasPrincipal</code>.
    * @return If CAS authentication has been performed for the session 
    * associated with <code>request</code>, returns a <code>CasPrincipal</code>
    * for the CAS-authenticated username.  Otherwise the return value is
    * delegated to the superclass.
    */
   public Principal getUser(HttpServletRequest request, 
       HttpServletResponse response) {
 
     ServiceValidationResponse validation =
         ValidationUtils.getServiceValidationResponse(request);
     if (validation == null) {
       log.debug("no CAS validation");
       return super.getUser(request, response);
     }
     log.debug("CAS validation for user " + validation.getUserName());
     Principal user = super.getUser(validation.getUserName());
     request.getSession().setAttribute(LOGGED_IN_KEY, user);
     request.getSession().setAttribute(LOGGED_OUT_KEY, null);
     return user;
   }
 
   /**
    * Performs the logout function, setting Seraph session state such
    * that it appears that no user is logged in.
    * @return <code>true</code>
    */
   public boolean logout(HttpServletRequest request,
       HttpServletResponse response) throws AuthenticatorException {
     super.logout(request, response);
    // Possible fix for SCC-12
    request.getSession().invalidate();
//    // TODO: the superclass probably does the same thing we're doing here
//    request.getSession().setAttribute(LOGGED_IN_KEY, null);
//    request.getSession().setAttribute(LOGGED_OUT_KEY, new Boolean(true));
     return true;
   }
  
 }
