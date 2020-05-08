 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.moria;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 /**
  * Represents the central user-name-to-user-element index, responsible for
  * mapping a username to an authentication (LDAP) server, including a search
  * base.
  */
 public class BackendIndex {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(BackendIndex.class.toString());
     
     /** The domain name to LDAP URL hash map. */
     private static HashMap urlMap = new HashMap();
     
     /** Did we initialize already? */
     private static Boolean initialized = new Boolean(false);
     
     /**
      * The lookup method.
      * @param username A username, on the form prefix@suffix.
      * @return An LDAP URL, including search base (for example,
      *         <code>ldaps://my.ldap.server:636/ou=my,dc=search,dc=base</code>).
      *         Returns <code>null</code> if no match for the username.
      * @throws BackendException If the username doesn't include the '@'
      *                          character, or if the property
      *                                <code>no.feide.moria.backend.ldap</code>
      *                                is not set.
      */
     public static String lookup(String username)
     throws BackendException {
         log.finer("lookup(String)");
         
         // Initialize the hash map, if we haven't already.
         synchronized (initialized) {
            if (!initialized.booleanValue()) {
                 // Create hashtable of domain names to LDAP URLs.
                 String domain, url;
                 for (int i=1; ; i++) {
                     try {
                         domain = Configuration.getProperty("no.feide.moria.backend.ldap"+i+".domain");
                         if (domain == null)
                             break;  // No more mappings.
                         url = Configuration.getProperty("no.feide.moria.backend.ldap"+i+".url");
                     } catch (ConfigurationException e) {
                         log.severe("ConfigurationException caught and re-thrown as BackendException");
                         throw new BackendException("ConfigurationException caught and re-thrown as BackendException");
                     }
                     if (url == null)
                         break;  // More of a sanity check; possible syntax error in config file.
                     urlMap.put(domain, url);
                     log.config(domain+" mapped to "+url);
                 }
             }
         }
         
         // Map user ID domain to LDAP URL using the username suffix.
         String domain = username;
         if (domain.indexOf('@') == -1) {
             log.severe("Illegal user identifier; missing @: "+domain);
             throw new BackendException("Illegal user identifier; missing @: "+domain);
         } 
         domain = domain.substring(domain.indexOf('@')+1);
         String url = (String)urlMap.get(domain);
        if (url == null) {
        }
         log.info("Matched domain "+domain+" to LDAP URL "+url);
         
         return url;        
     }
     
 }
