 /*
  * Copyright (C) 2005 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util.j2ee;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.OSSObject;
 
 /**
  * Collection of useful utilities to work with j2ee environment.
  *
  * @author OpenSubsystems
  */
 public final class J2EEUtils extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
 
    /**
     * Constant defining j2ee server was not yet detected.
     */
    public static final int J2EE_SERVER_UNINITIALIZED = -1;
 
    /**
     * Constant defining no j2ee server.
     */
    public static final int J2EE_SERVER_NO = 0;
 
    /**
     * Constant defining JOnAS j2ee server.
     */
    public static final int J2EE_SERVER_JONAS = 1;
 
    /**
     * Constant defining JBoss j2ee server.
     */
    public static final int J2EE_SERVER_JBOSS = 2;
 
    /**
     * Constant defining WebLogic j2ee server.
     */
    public static final int J2EE_SERVER_WEBLOGIC = 3;
 
    /**
     * Constant defining WebSphere j2ee server.
     */
    public static final int J2EE_SERVER_WEBSPHERE = 4;
 
    /**
     * Constant identifying JOnAS j2ee server. There is retrieved parent of the 
     * classloader because it identify jonas server. Using just classloader there 
     * is retireved particular web server identifier (jetty, tomcat, ...).
     * ClassLoader parent for JOnAS 4.2.3: 
     *    org.objectweb.jonas_lib.loader.SimpleWebappClassLoader
     */
    public static final String JONAS_IDENTIFIER = ".objectweb.jonas";
 
    /**
     * Constant identifying JBoss j2ee server.  
     * ClassLoader parent for JBoss 3.2.6 and 4.0.1: 
     *    org.jboss.system.server.NoAnnotationURLClassLoader
     */
    public static final String JBOSS_IDENTIFIER = ".jboss.";
 
    /**
     * Constant identifying WebLogic j2ee server.
     * ClassLoader parent for BEA WebLogic 7.0 and 8.1: 
     *    weblogic.utils.classloaders.GenericClassLoader
     */
    public static final String WEBLOGIC_IDENTIFIER = "weblogic.";
 
    /**
     * Constant identifying WebSphere j2ee server.
     * ClassLoader parent for IBM WebSphere 6:
     *    com.ibm.ws.classloader.JarClassLoader 
     */
    public static final String WEBSPHERE_IDENTIFIER = ".ibm.ws.";
 
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(J2EEUtils.class);
    
    /**
     * Since J2EE server doesn't changes during execution we can cache the
     * value for detected server.
     */
   private static int s_iDetectedServer = J2EE_SERVER_UNINITIALIZED;
   
    // Constructors /////////////////////////////////////////////////////////////
    
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private J2EEUtils(
    )
    {
       // Do nothing
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
 
    /**
     * Method detects and returns type of the current running j2ee server. For 
     * detecting actual running j2ee server we will use ClassLoader because it is 
     * server specific.
     * 
     * @return int - representation of the current running j2ee server
     */
    public static int getJ2EEServerType(
    )
    {
       // No need to synchronize since in the worst case we execute this 
       // multiple times
       if (s_iDetectedServer == J2EE_SERVER_UNINITIALIZED)
       {
          int    iRetValue;
          String strClassLoader;
    
          strClassLoader = J2EEUtils.class.getClassLoader().getClass().getName();
          iRetValue = detectJ2EEServerType(strClassLoader);
          if (iRetValue == J2EE_SERVER_NO)
          {
             strClassLoader = J2EEUtils.class.getClassLoader().getParent(
                                 ).getClass().getName();
             iRetValue = detectJ2EEServerType(strClassLoader);
          }
          
          s_iDetectedServer = iRetValue;
       }
       
 
       return s_iDetectedServer;
    }
    
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Detect current running j2ee server based on the specified string 
     * because it is server specific.
     * 
     * @param strIdentifier - string which should uniquely identify the AS
     * @return int - representation of the current running j2ee server
     */
   private static int detectJ2EEServerType(
       String strIdentifier
    )
    {
       int iRetValue = J2EE_SERVER_NO;
 
       if (strIdentifier != null)
       {
          s_logger.log(Level.FINEST, "Trying to detect J2EE application server"
                       + " using identifier {0}", strIdentifier);
          if (strIdentifier.indexOf(JONAS_IDENTIFIER) > -1)
          {
             s_logger.fine("JOnAS application server detected.");
             iRetValue = J2EE_SERVER_JONAS;
          }
          else if (strIdentifier.indexOf(JBOSS_IDENTIFIER) > -1)
          {
             s_logger.fine("JBoss application server detected.");
             iRetValue = J2EE_SERVER_JBOSS;
          }
          else if (strIdentifier.indexOf(WEBLOGIC_IDENTIFIER) > -1)
          {
             s_logger.fine("Weblogic application server detected.");
             iRetValue = J2EE_SERVER_WEBLOGIC;
          }
          else if (strIdentifier.indexOf(WEBSPHERE_IDENTIFIER) > -1)
          {
             s_logger.fine("Websphere application server detected.");
             iRetValue = J2EE_SERVER_WEBSPHERE;
          }
       }
       else
       {
          s_logger.finest("No J2EE application server detected since identifier" +
                          " is null.");
       }
 
       return iRetValue;
    }
 }
