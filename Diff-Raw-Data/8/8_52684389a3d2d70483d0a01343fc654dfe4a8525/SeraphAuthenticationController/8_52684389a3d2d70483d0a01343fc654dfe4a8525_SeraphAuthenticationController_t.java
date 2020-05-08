 package com.atlassian.sal.core.auth;
 
 import java.security.Principal;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.atlassian.sal.api.auth.AuthenticationController;
 import com.atlassian.sal.core.util.Assert;
 import com.atlassian.seraph.auth.RoleMapper;
 import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.config.SecurityConfigFactory;
 
 /**
  * Implementation of the {@link AuthenticationController} to integrate with Atlassian Seraph.
  */
 public class SeraphAuthenticationController implements AuthenticationController
 {
     private final RoleMapper roleMapper;
 
     /**
      * @throws IllegalArgumentException if the roleMapper is <code>null</code>.
      */
    public SeraphAuthenticationController()
     {
        RoleMapper roleMapper = SecurityConfigFactory.getInstance().getRoleMapper();
         this.roleMapper = Assert.notNull(roleMapper, "roleMapper");
     }
 
     /**
      * Checks the {@link RoleMapper} on whether or not the principal can login.
      *
      * @see AuthenticationController#canLogin(Principal, HttpServletRequest)
      */
     public boolean canLogin(final Principal principal, final HttpServletRequest request)
     {
         return roleMapper.canLogin(principal, request);
     }
 
     /**
      * Checks the request attibutes for the {@link BaseLoginFilter#OS_AUTHSTATUS_KEY}. Will return <code>true</code> if
      * the key is not present.
      */
     public boolean shouldAttemptAuthentication(final HttpServletRequest request)
     {
         return request.getAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY) == null;
     }
 }
