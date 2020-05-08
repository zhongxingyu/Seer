 /* 
  * 
  * PROJECT
  *     Name
  *         APS APIs
  *     
  *     Code Version
  *         0.9.0
  *     
  *     Description
  *         Provides the APIs for the application platform services.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2013-02-20: Created!
  *         
  */
 package se.natusoft.osgi.aps.api.auth.user;
 
 import se.natusoft.osgi.aps.api.auth.user.exceptions.APSAuthMethodNotSupportedException;
 
 import java.util.Properties;
 
 /**
  * This is intended to be used as a wrapper to other means of authentication. Things in APS
  * that needs authentication uses this service.
  * <p/>
  * Implementations can lookup the user in an LDAP for example, or use some other user service.
  * <p/>
  * APS supplies an APSSimpleUserServiceAuthServiceProvider that uses the
  * APSSimpleUserService to authenticate. It is provided in its own bundle.
  */
 public interface APSAuthService<Credential> {
 
 
     /**
      * This authenticates a user. A Properties object is returned on successful authentication. null is returned
      * on failure. The Properties object returned contains misc information about the user. It can contain anything
     * or nothing at all. There can be no assumptions about its contents! If the specified AuthMethod is not
     * supported by the service implementation it should simply fail the auth by returning null.
      *
      * @param userId The id of the user to authenticate.
      * @param credentials What this is depends on the value of AuthMethod. It is up to the service implementation to resolve this.
      * @param authMethod This hints at how to interpret the credentials.
      *
      * @return User properties on success, null on failure.
      */
     Properties authUser(String userId, Credential credentials, AuthMethod authMethod) throws APSAuthMethodNotSupportedException;
 
     /**
      * Returns an array of the AuthMethods supported by the implementation.
      */
     AuthMethod[] getSupportedAuthMethods();
 
     //
     // Inner Classes
     //
 
     /**
      * This hints at how to use the credentials.
      */
     public static enum AuthMethod {
         /** Only userid is required. */
         NONE,
 
         /** toString() on the credentials object should return a password. */
         PASSWORD,
 
         /** The credential object is a key of some sort. */
         KEY,
 
         /** The credential object is a certificate of some sort. */
         CERTIFICATE,
 
         /** The credential object is a digest password. */
         DIGEST,
 
         /** The credential object contains information for participating in a single sign on. */
         SSO
     }
 }
