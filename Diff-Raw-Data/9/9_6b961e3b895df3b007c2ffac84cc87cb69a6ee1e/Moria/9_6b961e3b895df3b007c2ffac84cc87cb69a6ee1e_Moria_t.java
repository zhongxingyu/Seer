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
 
package no.feide.mellon;
 
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import javax.xml.rpc.Stub;
 
 import no.feide.moria.service.Attribute;
 import no.feide.moria.service.AuthenticationIF;
 import no.feide.moria.service.Authentication_Impl;
 
 /**
  * Represents the interface to Moria, hiding the internals of the
  * Mellon-Moria communication.
  */
 public class Moria {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(Moria.class.toString());
     
     /** Singleton self. */
     private static Moria me;
     
     /** The JAX-RPC Moria interface proxy stub. */
     private Stub stub;
     
     
     /**
      * Private constructor. Will prepare JAX-RPC stub.
      */
     private Moria() {
         log.finer("Moria()");
                 
         // Create service proxy and set credentials.
         stub = (Stub)(new Authentication_Impl().getAuthenticationIFPort());
 	    String s = System.getProperty("no.feide.mellon.serviceUsername");
 	    log.config("no.feide.mellon.serviceUsername="+s);
         stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, s);
         stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, System.getProperty("no.feide.mellon.servicePassword"));
 	    AuthenticationIF service = (AuthenticationIF)stub;
     }
 
     
     /**
      * Get an instance of Moria.
      * @return An instance of the Moria interface.
      * @throws MoriaException If the singleton object couldn't be constructed.
      */
     public static Moria getInstance()
     throws MoriaException {
         log.finer("getInstance()");
         
         // Return singleton, creating it if necessary.
         if (me == null)
             me = new Moria();
         return me;
     }
     
     
     /**
      * Request an authentication session from Moria. A remote
      * procedure call is sent to Moria. If this is successful a URL is
      * returned. The user should be redirected to this URL for
      * authentication.
      * @param attributes The names of requested attributes, to be returned
      *                   later throught <code>getAttributes</code>.
      * @param prefix The prefix, used to build the <code>verifySession</code>
      *               return value.
      * @param postfix The postfix, used to build the
      *                <code>verifySession</code> return value.
      * @return A Moria session descriptor.
      * @throws MoriaException If a RemoteException is caught.
      */
     public String requestSession(String[] attributes, String prefix, String postfix, boolean denySso) 
     throws MoriaException {
         log.finer("requestSession(String[], String, String)");
         
 	    AuthenticationIF service = (AuthenticationIF)stub;
         try {
             return service.requestSession(attributes, prefix, postfix, denySso);
         } catch (RemoteException e) {
             log.severe("RemoteException caught and re-thrown as MoriaException");
             throw new MoriaException("RemoteException caught", e);
         }
     }
     
     
     /**
      * Returns user data from Moria. A remote procedure call is sent
      * to Moria, requesting user data. If an empty HashMap is
      * returned, the user has been authenticated but no more
      * information is available to the webservice. If the web service
      * has requested (and been authorized) user attributes, the
      * HashMap will contain the requested user data.
      * @param id The session ID returned from Moria following a successful
      *           authentication.
      * @return The attributes requested when the session was established,
      *         or an empty set if no attributes were requested.
      * @throws MoriaException If a RemoteException is caught.
      */
     public HashMap getAttributes(String id)
     throws MoriaException {
         log.finer("getAttributes(String)");
         
 	AuthenticationIF service = (AuthenticationIF)stub;
         try {
             
             // Map from Attribute array to HashMap.
             Attribute[] oldAttrs = service.getAttributes(id);
             HashMap newAttrs = new HashMap(oldAttrs.length);
             for (int i=0; i<oldAttrs.length; i++) {
                 String[] oldVals = oldAttrs[i].getValues();
                 Vector newVals = new Vector(oldVals.length);
                 for (int j=0; j<oldVals.length; j++)
                     newVals.add(oldVals[j]);
                 newAttrs.put(oldAttrs[i].getName(), newVals);
             }
             return newAttrs;
             
         } catch (RemoteException e) {
             log.severe("RemoteException caught and re-thrown as MoriaException");
             throw new MoriaException("RemoteException caught", e);
         }
     }
 
 
     /**
      * Returns the address of the service used.
      * @return The service's endpoint address.
      */
     public String getServiceAddress() {
 	    log.finer("getServiceAddress()");
 
 	    return (String)stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
     }
 
     
 }
