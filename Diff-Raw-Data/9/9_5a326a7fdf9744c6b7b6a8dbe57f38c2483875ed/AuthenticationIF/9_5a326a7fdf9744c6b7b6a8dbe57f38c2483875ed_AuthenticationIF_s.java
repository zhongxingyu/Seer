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
 
 package no.feide.moria.webservices.v1_1;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 /**
  * @author Bjrn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
  * @version $Revision$
  */
 public interface AuthenticationIF extends Remote {
 
     /**
     * @see no.feide.moria.webservices.v1_0.AuthenticationIF#
     *      initiateAuthentication(java.lang.String[], java.lang.String, java.lang.String, boolean)
      */
     String initiateAuthentication(String[] attributes, String returnURLPrefix, String returnURLPostfix,
             boolean forceInteractiveAuthentication) throws RemoteException;
 
     /**
     * @see no.feide.moria.webservices.v1_0.AuthenticationIF#
     * directNonInteractiveAuthentication(java.lang.String[], java.lang.String, java.lang.String)
      */
     Attribute[] directNonInteractiveAuthentication(String[] attributes, String username, String password) throws RemoteException;
 
     /**
      * Called by a subsystem to authenticate a user.
      *
      * @param attributes
      *          the attributes the service wants returned on login
      * @param proxyTicket
      *          the proxy ticket given to the calling system by its initiator
      * @return array of attributes as requested
      * @throws RemoteException
      *          if anything fails during the call
      */
     Attribute[] proxyAuthentication(String[] attributes, String proxyTicket) throws RemoteException;
 
     /**
      * A service may as part of the initial attribute request ask for
      * a ticket granting ticket that later may be used in this call.
      *
      * The returned proxy ticket is to be handed over to the specified
      * underlying system and may be used to authenticatethe request
      * by that system only.
      *
      * @param ticketGrantingTicket
      *          a TGT that has issued previously
      * @param proxyServicePrincipal
      *          the service which the proxy ticket should be issued for
      * @return a proxy ticket
      * @throws RemoteException
      *          if anything fails during the call
      */
     String getProxyTicket(String ticketGrantingTicket, String proxyServicePrincipal) throws RemoteException;
 
     /**
      * @see no.feide.moria.webservices.v1_0.AuthenticationIF#getUserAttributes(java.lang.String)
      */
     Attribute[] getUserAttributes(String serviceTicket) throws RemoteException;
 
     /**
      * Get public attributes for a given group.
      *
      * @param groupname
      *          the name of the group
      * @return array of public attributes
      * @throws RemoteException
      *          if anything fails during the call
      */
     Attribute[] getGroupAttributes(String groupname) throws RemoteException;
 
     /**
      * @see no.feide.moria.webservices.v1_0.AuthenticationIF#getUserExistence(java.lang.String)
      */
     boolean verifyUserExistence(String username) throws RemoteException;
 
     /**
      * Verifies the existence of a given group in the underlying directories.
      *
      * @param groupname
      *          the groupname to be validated
      * @return true if the group exists
      * @throws RemoteException
      *           if anything fails during the call
      */
     boolean verifyGroupExistence(String groupname) throws RemoteException;
 
     /**
      * Verify that a given user is member of a specific group.
      *
      * @param username
      *          the username to be validated
      * @param groupname
      *          the name of the group that may contain the user
      * @return true if the user is a member of the group
      * @throws RemoteException
      *          if anything fails during the call
      */
     boolean verifyUserMemberOfGroup(String username, String groupname) throws RemoteException;
 }
