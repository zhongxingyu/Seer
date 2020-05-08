 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.log;
 
 import java.io.Serializable;
 
 /**
  * This class represents access status type constants for the AccessLogger. It's an implementation of the "typesafe enum
  * pattern".
  *
  * @author Bjrn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
  * @version $Revision$
  */
 public final class AccessStatusType implements Serializable {
 
     /**
      * Description of status type.
      */
     private final String name;
 
     /**
      * Ordinal of next status type to be created.
      */
     private static int nextOrdinal = 0;
 
     /**
      * Assigns an ordinal to this status type.
      */
     private final int ordinal = nextOrdinal++;
 
     /**
      * Default private constructor.
      *
      * @param name The name of the status type. Used in the log and should be all caps.
      */
     private AccessStatusType(final String name) {
         this.name = name;
     }
 
     /**
      * Returns string representation of object.
      *
      * @return name of object
      */
     public String toString() {
         return name;
     }
 
     /**
      * Access log type used to indicate that the user failed to login because of bad credentials.
      */
     public static final AccessStatusType BAD_USER_CREDENTIALS = new AccessStatusType("BAD USER CREDENTIALS");
 
     /**
      * Access log type used to indicate that the service failed to authenticate itself.
      */
     public static final AccessStatusType BAD_SERVICE_CREDENTIALS = new AccessStatusType("BAD SERVICE CREDENTIALS");
 
     /**
      * Access log type used to indicate that the service requests illegal operations.
      */
     public static final AccessStatusType OPERATIONS_NOT_PERMITTED = new AccessStatusType("OPERATIONS NOT PERMITTED");
     
     /**
      * Access log type used to indicate that user from a specific userorg is not allowed to use.
      * this service
      */
     public static final AccessStatusType ACCESS_DENIED_USERORG = new AccessStatusType("ACCESS DENIED USERORG");
 
     /**
      * Access log type used to indicate that the principal is invalid for the requested attributes.
      */
     public static final AccessStatusType GET_USER_ATTRIBUTES_DENIED_INVALID_PRINCIPAL = new AccessStatusType("GET USER ATTRIBUTES DENIED INVALID PRINCIPAL");
     /**
      * Access log type used to indicate that the service requests illegal attributes.
      */
     public static final AccessStatusType ACCESS_DENIED_INITIATE_AUTH = new AccessStatusType(
             "ACCESS DENIED INITIATE AUTH");
 
     /**
      * Access log type used to indicate that the service requests illegal attributes.
      */
     public static final AccessStatusType ACCESS_DENIED_DIRECT_AUTH = new AccessStatusType("ACCESS DENIED DIRECT AUTH");
 
     /**
      * Access log type used to indicate that the service requests illegal attributes.
      */
     public static final AccessStatusType ACCESS_DENIED_VERIFY_USER_EXISTENCE = new AccessStatusType(
             "ACCESS DENIED VERIFY USER EXISTENCE");
     
     /**
      * Access log type used to indicate that the service requests illegal proxy authentication.
      */
     public static final AccessStatusType ACCESS_DENIED_PROXY_AUTH = new AccessStatusType("ACCESS DENIED PROXY AUTH");
 
     /**
      * Access log type used to indicate that the SSO ticket is invalid.
      */
     public static final AccessStatusType INVALID_SSO_TICKET = new AccessStatusType("INVALID SSO TICKET");
 
     /**
      * Access log type used to indicate that the SSO ticket does not exist.
      */
     public static final AccessStatusType NONEXISTENT_SSO_TICKET = new AccessStatusType("NONEXISTENT SSO TICKET");
 
     /**
      * Access log type used to indicate that the login ticket is invalid.
      */
     public static final AccessStatusType INVALID_LOGIN_TICKET = new AccessStatusType("INVALID LOGIN TICKET");
 
     /**
      * Access log type used to indicate that the login ticket does not exist.
      */
     public static final AccessStatusType NONEXISTENT_LOGIN_TICKET = new AccessStatusType("NONEXISTENT LOGIN TICKET");
 
     /**
      * Access log type used to indicate that the service ticket is invalid.
      */
     public static final AccessStatusType INVALID_SERVICE_TICKET = new AccessStatusType("INVALID SERVICE TICKET");
 
     /**
      * Access log type used to indicate that the service ticket does not exist.
      */
     public static final AccessStatusType NONEXISTENT_SERVICE_TICKET = new AccessStatusType(
             "NONEXISTENT SERVICE TICKET");
 
     /**
      * Access log type used to indicate that the proxy ticket is invalid.
      */
     public static final AccessStatusType INVALID_PROXY_TICKET = new AccessStatusType("INVALID PROXY TICKET");
 
     /**
      * Access log type used to indicate that the proxy ticket does not exist.
      */
     public static final AccessStatusType NONEXISTENT_PROXY_TICKET = new AccessStatusType("NONEXISTENT PROXY TICKET");
 
     /**
      * Access log type used to indicate that the TGT is invalid.
      */
     public static final AccessStatusType INVALID_TGT = new AccessStatusType("INVALID TGT");
 
     /**
      * Access log type used to indicate that the TGT does not exist.
      */
     public static final AccessStatusType NONEXISTENT_TGT = new AccessStatusType("NONEXISTENT TGT");
 
     /**
     * Access log type used to indicate that authentication initialization failed due to invalid prefix/postfix URL.
      */
     public static final AccessStatusType INITIATE_DENIED_INVALID_URL = new AccessStatusType(
             "INITIATE_DENIED_INVALID_URL");
 
     /**
      * Access log type used to indicate that proxy authentication was denied due to request for non-cached
      * attributes.
      */
     public static final AccessStatusType PROXY_AUTH_DENIED_UNCACHED_ATTRIBUTES = new AccessStatusType(
             "PROXY AUTH DENIED UNCACHED ATTRIBUTES");
 
     /**
      * Access log type used to indicate that generation of proxy ticket was denied, not authorized.
      */
     public static final AccessStatusType PROXY_TICKET_GENERATION_DENIED_UNAUTHORIZED = new AccessStatusType(
             "PROXY TICKET GENERATION DENIED UNAUTHORIZED");
 
     /**
      * Access log type used to indicate that generation of proxy ticket was denied, invalid subsystem.
      */
     public static final AccessStatusType PROXY_TICKET_GENERATION_DENIED_INVALID_PRINCIPAL = new AccessStatusType(
             "PROXY TICKET GENERATION DENIED INVALID PRINCIPAL");
 
     /**
      * Access log type used to indicate that SSO authentication was successful.
      */
     public static final AccessStatusType SUCCESSFUL_SSO_AUTHENTICATION = new AccessStatusType(
             "SUCCESSFUL SSO AUTHENTICATION");
 
     /**
      * Access log type used to indicate that interactive authentication was successful.
      */
     public static final AccessStatusType SUCCESSFUL_INTERACTIVE_AUTHENTICATION = new AccessStatusType(
             "SUCCESSFUL INTERACTIVE AUTHENTICATION");
 
     /**
      * Access log type used to indicate that direct authentication was successful.
      */
     public static final AccessStatusType SUCCESSFUL_DIRECT_AUTHENTICATION = new AccessStatusType(
             "SUCCESSFUL DIRECT AUTHENTICATION");
 
     /**
      * Access log type used to indicate that proxy authentication was successful.
      */
     public static final AccessStatusType SUCCESSFUL_PROXY_AUTHENTICATION = new AccessStatusType(
             "SUCCESSFUL PROXY AUTHENTICATION");
 
     /**
     * Access log type used to indicate that authentication initialization was succesful.
      */
     public static final AccessStatusType SUCCESSFUL_AUTH_INIT = new AccessStatusType("SUCCESSFUL AUTH INIT");
 
     /**
      * Access log type used to indicate that the attributes were successfully retrieved.
      */
     public static final AccessStatusType SUCCESSFUL_GET_ATTRIBUTES = new AccessStatusType("SUCCESSFUL GET ATTRIBUTES");
 
     /**
      * Access log type used to indicate that generation of proxy ticket was successful.
      */
     public static final AccessStatusType SUCCESSFUL_GET_PROXY_TICKET = new AccessStatusType(
             "SUCCESSFUL GET PROXY TICKET");
 
     /**
      * Access log type used to indicate that user verification was successful.
      */
     public static final AccessStatusType SUCCESSFUL_VERIFY_USER = new AccessStatusType("SUCCESSFUL VERIFY USER");
 
     /**
      * Access log type used to indicate that the SSO ticket was invalidated.
      */
     public static final AccessStatusType SSO_TICKET_INVALIDATED = new AccessStatusType("SSO TICKET INVALIDATED");
 
     /**
      * Static array that holds all objects. Used by readResolve() to return correct object after de-serialization.
      */
     private static final AccessStatusType[] TYPES = {BAD_USER_CREDENTIALS, BAD_SERVICE_CREDENTIALS,
                                                      OPERATIONS_NOT_PERMITTED, ACCESS_DENIED_USERORG,
                                                      ACCESS_DENIED_INITIATE_AUTH, ACCESS_DENIED_DIRECT_AUTH,
                                                      ACCESS_DENIED_VERIFY_USER_EXISTENCE, ACCESS_DENIED_PROXY_AUTH,
                                                      SSO_TICKET_INVALIDATED, SUCCESSFUL_VERIFY_USER,
                                                      SUCCESSFUL_GET_PROXY_TICKET
                                                      , SUCCESSFUL_GET_ATTRIBUTES, SUCCESSFUL_AUTH_INIT,
                                                      SUCCESSFUL_PROXY_AUTHENTICATION, SUCCESSFUL_DIRECT_AUTHENTICATION,
                                                      SUCCESSFUL_INTERACTIVE_AUTHENTICATION,
                                                      SUCCESSFUL_SSO_AUTHENTICATION,
                                                      PROXY_TICKET_GENERATION_DENIED_INVALID_PRINCIPAL,
                                                      PROXY_TICKET_GENERATION_DENIED_UNAUTHORIZED,
                                                      PROXY_AUTH_DENIED_UNCACHED_ATTRIBUTES,
                                                      INITIATE_DENIED_INVALID_URL, NONEXISTENT_TGT,
                                                      INVALID_TGT, NONEXISTENT_PROXY_TICKET, INVALID_PROXY_TICKET,
                                                      NONEXISTENT_SERVICE_TICKET, INVALID_SERVICE_TICKET,
                                                      NONEXISTENT_LOGIN_TICKET, INVALID_LOGIN_TICKET,
                                                      NONEXISTENT_SSO_TICKET, INVALID_SSO_TICKET};
 
     /**
      * Needed for serialization to work.
      *
      * @return The local classloader representation of the object.
      */
     Object readResolve() {
         return TYPES[ordinal];
     }
 }
 
