 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.authentication;
 
 import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
 import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.WebAttributes;
 
 /**
  * Constant values used with the Spring Security integration.
  */
 public interface JAMWikiAuthenticationConstants {
 
 	/** The default Spring Security logout URL. */
 	public static final String SPRING_SECURITY_LOGOUT_URL = "/j_spring_security_logout";
 	/** Query parameter which stores the default Spring Security logout redirection URL as defined in LogoutFilter.determineTargetUrl(). */
 	public static final String SPRING_SECURITY_LOGOUT_REDIRECT_QUERY_PARAM = "logoutSuccessUrl";
 
 	/** Default Spring Security login URL */
 	public static final String SPRING_SECURITY_LOGIN_URL = "/j_spring_security_check";
 	/** The default Spring Security remember me login form field name. */
 	public static final String SPRING_SECURITY_LOGIN_REMEMBER_ME_FIELD_NAME = AbstractRememberMeServices.DEFAULT_PARAMETER;
 	/** The default Spring Security password login form field name. */
 	public static final String SPRING_SECURITY_LOGIN_PASSWORD_FIELD_NAME = UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
 	/** The default Spring Security username login form field name. */
 	public static final String SPRING_SECURITY_LOGIN_USERNAME_FIELD_NAME = UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
 	/** The default Spring Security target URL login form hidden field name. */
 	public static final String SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME = AbstractAuthenticationTargetUrlRequestHandler.DEFAULT_TARGET_PARAMETER;
 
 	/** When forcing a user to login, the previous request is saved in the session under this key. */
	public static final String SPRING_SECURITY_SAVED_REQUEST_SESSION_KEY = WebAttributes.SAVED_REQUEST;
 
 	/** Key used to store access denied message key in the session. */
 	public static final String JAMWIKI_ACCESS_DENIED_ERROR_KEY = "JAMWIKI_403_ERROR_KEY";
 	/** Key used to store access denied redirection URL in the session. */
 	public static final String JAMWIKI_ACCESS_DENIED_URI_KEY = "JAMWIKI_403_URI_KEY";
 	/** Key used to store authentication required message key in the session. */
 	public static final String JAMWIKI_AUTHENTICATION_REQUIRED_KEY = "JAMWIKI_AUTHENTICATION_REQUIRED_KEY";
 	/** Key used to store authentication required redirection URL in the session. */
 	public static final String JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY = "JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY";
 }
