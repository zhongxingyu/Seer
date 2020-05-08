 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.server.rmi;
 
 // Java 2 standard packages
 import java.io.*;
 import java.net.URI;
 import java.rmi.*;
 import java.rmi.server.*;
 import java.util.*;
 
 // Third party packages
 import org.apache.log4j.*;
 
 // Locally written packages
 import org.mulgara.jrdf.*;
 import org.mulgara.query.QueryException;
 import org.mulgara.server.JRDFSession;
 import org.mulgara.server.JenaSession;
 import org.mulgara.server.ServerInfo;
 import org.mulgara.server.Session;
 import org.mulgara.server.SessionFactory;
 
 /**
  * RMI wrapper for {@link SessionFactory} implementations.
  *
  * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
  *
  * @created 2001-07-14
  *
  * @version $Revision: 1.12 $
  *
  * @modified $Date: 2005/06/26 12:48:16 $
  *
  * @maintenanceAuthor $Author: pgearon $
  *
  * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
  *
  * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
  *      Software Pty Ltd</A>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 class RemoteSessionFactoryImpl implements RemoteSessionFactory
 {
   /**
    * Logger. This is named after the classname.
    */
   private final static Logger logger =
       Logger.getLogger(RemoteSessionFactoryImpl.class);
 
   /**
    * The local database.
    */
   private SessionFactory sessionFactory;
 
   /**
    * The server URI used to create this factory.
    * Used for reconnection
    */
   private URI serverURI = null;
 
   /**
    * Set of {@link RemoteSession}s, the local copies of exported stubs. This is
    * necessary to prevent garbage collection.
    */
   private Set remoteSessionSet = new HashSet();
 
   /**
    * Constructor to create an RMI database server.
    *
    * @param sessionFactory PARAMETER TO DO
    * @throws IllegalArgumentException if <var>database</var> is <code>null</code>
    * @throws RemoteException if RMI communication fails
    */
   RemoteSessionFactoryImpl(SessionFactory sessionFactory) throws
       RemoteException {
 
     // Validate "sessionFactory" parameter
     if (sessionFactory == null) {
 
       throw new IllegalArgumentException("Null \"sessionFactory\" parameter");
     }
 
     // Initialize fields
     this.sessionFactory = sessionFactory;
   }
 
   //
   // Methods implementing the RemoteSessionFactory interface
   //
 
   /**
    * {@inheritDoc RemoteSessionFactory}
    *
    * @return The SecurityDomain value
    * @throws QueryException EXCEPTION TO DO
    */
   public URI getSecurityDomain() throws QueryException {
 
     return sessionFactory.getSecurityDomain();
   }
 
   /**
    * Get the server URI used to create the current remoteSessionFactory
    */
   public URI getServerURI()  {
     return this.serverURI;
   }
 
   /**
    * Set the server URI used to create the current remoteSessionFactory
    */
   public void setServerURI( URI serverURI ) {
     this.serverURI = serverURI;
   }
 
   /**
    * Get the default URI used by this server.
    */
   public URI getDefaultServerURI()  {
     return ServerInfo.getServerURI();
   }
 
   /**
    * {@inheritDoc RemoteSessionFactory}
    *
    * @return RETURNED VALUE TO DO
    * @throws QueryException EXCEPTION TO DO
    */
   public Session newSession() throws QueryException {
     try {
 
       // Create the session
       RemoteSession remoteSession =
           new RemoteSessionImpl(sessionFactory.newSession(), this);
       remoteSessionSet.add(remoteSession);
 
       RemoteSession exportedRemoteSession =
           (RemoteSession) UnicastRemoteObject.exportObject(remoteSession);
 
       // Apply two wrappers to hide the RemoteExceptions of the
       // RemoteSession interface so everything looks like a Session
       return new RemoteSessionWrapperSession(exportedRemoteSession,
                                              this.getServerURI());
     }
     catch (RemoteException e) {
 
       throw new QueryException("Couldn't export session", e);
     }
   }
 
   /**
    * {@inheritDoc RemoteSessionFactory}
    *
    * @return RETURNED VALUE TO DO
    * @throws QueryException EXCEPTION TO DO
    */
   public Session newJRDFSession() throws QueryException {
 
     try {
 
       // Create the session
       RemoteSession remoteSession =
           new RemoteJRDFSessionImpl(
           (LocalJRDFSession) sessionFactory.newJRDFSession(), this);
       remoteSessionSet.add(remoteSession);
 
       RemoteJRDFSession exportedRemoteSession =
           (RemoteJRDFSession) UnicastRemoteObject.exportObject(remoteSession);
 
       // Apply two wrappers to hide the RemoteExceptions of the
       // RemoteSession interface so everything looks like a Session
       return new RemoteSessionWrapperJRDFSession(exportedRemoteSession,
           this.getServerURI());
     }
     catch (RemoteException e) {
 
       throw new QueryException("Couldn't export session", e);
     }
   }
 
 
   /**
    * {@inheritDoc RemoteSessionFactory}
   */
   public RemoteSession newRemoteSession() throws QueryException, RemoteException {
     try {
 
       // Create the session
       RemoteSession remoteSession =
           new RemoteSessionImpl(sessionFactory.newSession(), this);
       remoteSessionSet.add(remoteSession);
 
       return (RemoteSession) UnicastRemoteObject.exportObject(remoteSession);
     }
     catch (RemoteException e) {
 
       throw new QueryException("Couldn't export session", e);
     }
   }
 
 
   /**
    * {@inheritDoc RemoteSessionFactory}
    *
    * @throws QueryException EXCEPTION TO DO
    */
   public void removeSession(RemoteSession session)  {
 
     remoteSessionSet.remove(session);
     try {
       UnicastRemoteObject.unexportObject(session, true);
     } catch ( NoSuchObjectException ex ) {
       // do nothing
     }
   }
 
   /**
    * {@inheritDoc RemoteSessionFactory}
    */
  public void close() throws QueryException {

    sessionFactory.close();
  }
 }
