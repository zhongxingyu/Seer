 /**
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-10-21
  * Time: 16:03:58
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild.security;
 
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.db.Role;
 import org.acegisecurity.*;
 import org.acegisecurity.context.SecurityContext;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.providers.dao.cache.EhCacheBasedUserCache;
 import net.sf.ehcache.Cache;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 import java.io.IOException;
 
 /**
  * The SecurityHelper is used to validate autheticated pricipals
  * at runtime against a given set of required roles.
  *
  *
  * @author johannes plachy
  */
 public class SecurityHelper
 {
 
     final transient private static Log logger = LogFactory.getLog(SecurityHelper.class);
 
     final private static int NON_PRJ_ID = -1;
 
     /**
      * check current logged-in principal for assigned roles
      * @param rolesToBeChecked
      * @return true if needed roles are assigned
      */
     public static boolean isUserInRole(String rolesToBeChecked)
     {
         return isUserInRole(NON_PRJ_ID, rolesToBeChecked);
     }
 
     /**
      * check current logged-in principal for assigned roles
      *
      * @param username to be checked against principal
      * @param rolesToBeChecked
      * @return true if needed roles are assigned
      */
     public static boolean isUserInRoleOrPrincipal(String username, String rolesToBeChecked)
     {
         return (getPrincipal().toString().equals(username) || isUserInRole(NON_PRJ_ID, rolesToBeChecked));
     }
 
     /**
      * check curent logged-in principal for assigned roles
      * which have to be project specific
      *
      * @param rolesToBeChecked
      * @return true if needed roles are assigned
      */
     public static boolean isUserInRole(long prjId, String rolesToBeChecked)
     {
         boolean accessGraned = false;
 
         Collection granted = getPrincipalAuthorities();
 
         Set grantedCopy = retainAll(granted, parseAuthoritiesString(prjId, rolesToBeChecked));
 
         accessGraned = !grantedCopy.isEmpty();
 
         return accessGraned;
     }
 
     public static Object getPrincipal()
     {
         Object principal = null;
 
         Authentication currentUser = getCurrentUser();
 
         if ( currentUser != null)
         {
             principal = currentUser.getPrincipal();
         }
 
         return principal;
     }
 
     public static String getPrincipalAsString() {
        final Authentication authentication = getCurrentUser();
        if (authentication != null) {
               if (authentication.getPrincipal() instanceof UserDetails) {
                 return ((UserDetails) authentication.getPrincipal()).getUsername();
             }
 
             return authentication.getPrincipal().toString();
 
        }
 
        return null;
     }
 
     private static Authentication getCurrentUser()
     {
         Authentication currentUser = null;
 
         SecurityContext context = SecurityContextHolder.getContext();
 
         if (null != context)
         {
             currentUser = context.getAuthentication();
         }
 
         return currentUser;
     }
 
     private static Collection getPrincipalAuthorities()
     {
 
         Authentication currentUser = getCurrentUser();
 
         if (null == currentUser) { return Collections.EMPTY_LIST; }
 
         Collection granted = Arrays.asList(currentUser.getAuthorities());
 
         return granted;
     }
 
     /**
      * parse general roles string which is abstract so far
      *
      * @param prjid > 0 ( use it to construct prj specific roles)
      * @param rolesToBeChecked
      * @return
      */
     private static Set parseAuthoritiesString(long prjid, String rolesToBeChecked)
     {
         final Set requiredAuthorities = new HashSet();
         final StringTokenizer tokenizer;
 
         tokenizer = new StringTokenizer(rolesToBeChecked, ",", false);
 
         while (tokenizer.hasMoreTokens())
         {
             String role = tokenizer.nextToken();
 
             // construct prj specific roles
             if ( prjid > NON_PRJ_ID)
             {
                 // if base_roles are specific
                 if ( role.indexOf("PRJ") > 0 )
                 {
                     role = role +"_" + prjid;
                 }
             }
 
             requiredAuthorities.add(new GrantedAuthorityImpl(role.trim()));
         }
 
         return requiredAuthorities;
     }
 
     private static Set retainAll(final Collection granted, final Set requiredAuthorities)
     {
         Set grantedCopy = new HashSet(granted);
         grantedCopy.retainAll(requiredAuthorities);
 
         return grantedCopy;
     }
 
 	public static boolean isPrjReadable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, ROLE_AUTHENTICATED, ROLE_ANONYMOUS, " +
                 "LUNTBUILD_PRJ_ADMIN, LUNTBUILD_PRJ_BUILDER, LUNTBUILD_PRJ_VIEWER");
 	}
 
 	public static boolean isPrjBuildable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, LUNTBUILD_PRJ_ADMIN, LUNTBUILD_PRJ_BUILDER");
 	}
 
 	public static boolean isPrjAdministrable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, LUNTBUILD_PRJ_ADMIN");
 	}
 
 	public static boolean isSiteAdmin() {
 		return isUserInRole("ROLE_SITE_ADMIN");
 	}
 
 	public static void runAsSiteAdmin() {
         UserDetailsService authDao = (UserDetailsService) Luntbuild.appContext.getBean("inMemoryAuthenticationDAO");
 		if (authDao == null) {
 			logger.error("Can not find bean named by \"inMemoryAuthenticationDao\" " +
 					"in the application context!");
 			throw new RuntimeException("Can not find bean named by \"inMemoryAuthenticationDao\" " +
 					"in the application context!");
 		}
 		UserDetails userDetails = authDao.loadUserByUsername("luntbuild");
 		GrantedAuthority[] authorities = userDetails.getAuthorities();
 		// check if this user has site admin priviledge
 		boolean isSiteAdmin = false;
 		for (int i = 0; i < authorities.length; i++) {
 			GrantedAuthority authority = authorities[i];
 			if (authority.getAuthority().trim().equals(Role.ROLE_SITE_ADMIN)) {
 				isSiteAdmin = true;
 				break;
 			}
 		}
 		if (!isSiteAdmin) {
 			logger.error("Built-in user \"luntbuild\" should have \"ROLE_SITE_ADMIN\" priviledge!");
 			throw new RuntimeException("Built-in user \"luntbuild\" should have \"ROLE_SITE_ADMIN\" priviledge!");
 		}
 		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("luntbuild",
 				userDetails.getPassword());
 		AuthenticationManager authManager = Luntbuild.getAuthenticationManager();
 		Authentication authResult;
 		try {
 			authResult = authManager.authenticate(authRequest);
 		} catch (AuthenticationException failed) {
 			logger.error("Failed to authenticate build-in administrator: ", failed);
 			throw new RuntimeException(failed);
 		}
         SecurityContext context = SecurityContextHolder.getContext();
 		if (context == null) {
             logger.error("Failed to create SecurityContext");
             throw new RuntimeException();
 		}
 		context.setAuthentication(authResult);
 	}
 
 	public static void refreshUserCache() {
 		EhCacheBasedUserCache userCache = (EhCacheBasedUserCache) Luntbuild.appContext.getBean("userCache");
		try {
			Cache cache = userCache.getCache();
			cache.removeAll();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
 	}
 }
