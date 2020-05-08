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
 
 import java.util.ArrayList;
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
      * @return One or more LDAP URLs, including search base (for example,
      *         <code>ldaps://my.ldap.server:636/ou=my,dc=search,dc=base</code>).
      *         Returns <code>null</code> if no match for the username.
      * @throws BackendException If the username doesn't include the '@'
      *                          character. 
      * @throws ConfigurationException If there is an error reading and parsing
      *                                the index configuration.
      */
     public static String[] lookup(String username)
     throws BackendException, ConfigurationException {
         log.finer("lookup(String)");
         
 		ArrayList urls = new ArrayList();
         
         // Initialize the hash map, if we haven't already.
         synchronized (initialized) {
             if (!initialized.booleanValue()) {
             	
                 // Create hashtable of domain names to LDAP URLs.
                 String domain, url;
                 for (int i=1; ; i++) {
                     domain = Configuration.getProperty("no.feide.moria.backend.ldap"+i+".domain");
                     if (domain == null)
                         break;  // No more domains.
                     for (int j = 1; ; j++) {
                     	url = Configuration.getProperty("no.feide.moria.backend.ldap"+i+".url"+j);
                     	if (url == null)
                     		break;  // No more URLs for this domain.
                     	urls.add(url);
 						log.config(domain+" mapped to "+url);
                     }
                     
                     // Sanity check.
                     if (urls.size() == 0) {
                    	log.config("Backend index is empty for domain "+domain);
                    	throw new ConfigurationException("Backend index is empty for domain "+domain);
                     }
                     
                     urlMap.put(domain, urls);
                    urls.clear();
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
         urls = (ArrayList)urlMap.get(domain);
         
         // Sanity check.
         if (urls == null) {
         	log.config("No URLs for domain "+domain);
         	throw new ConfigurationException("No URLs for domain "+domain);
         }
         
         log.info("Matched domain "+domain+" to LDAP URLs "+urls.toString());
         String[] urlArray = (String[])urls.toArray(new String[] {}); 
         return urlArray;        
     }
     
 }
