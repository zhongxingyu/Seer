 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.sasxremwin.provider;
 
 import com.jme.math.Vector2f;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.appbase.client.ProcessReporterFactory;
 import org.jdesktop.wonderland.modules.sas.provider.SasProvider;
 import org.jdesktop.wonderland.modules.sas.provider.SasProviderConnectionListener;
 import org.jdesktop.wonderland.modules.sas.provider.SasProviderSession;
 import org.jdesktop.wonderland.modules.xremwin.client.AppXrwMaster;
 
 /**
  * The main logic for the SAS Xremwin provider client.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public class SasXrwProviderMain implements SasProviderConnectionListener {
 
     /** The session associated with this provider. */
     private SasProviderSession session;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         SasXrwProviderMain providerMain = new SasXrwProviderMain();
     }
 
     private SasXrwProviderMain () {
 
         checkPlatform();
 
         System.err.println("****** Enter SasXrwProviderMain");
 
         // TODO: parse args
 
         String userName = "sasxprovider";
         String fullName = "SAS Provider for Xremwin";
         String password = "foo";

        
        String serverUrl = System.getProperty("wonderland.web.server.url", "http://localhost:8080");
        System.err.println("Connecting to server " + serverUrl);
 
         try {
             SasProvider provider = new SasProvider(userName, fullName, password, serverUrl, this);
         } catch (Exception ex) {
             System.err.println("Exception " + ex);
             ex.printStackTrace();
             System.err.println("Cannot connect to server " + serverUrl);
             System.exit(1);
         }        
     }
 
     /**
      * SAS can only run on a Unix platform. Exit if this is not the case.
      */
     private void checkPlatform() {
         String osName = System.getProperty("os.name");
         if (!"Linux".equals(osName) &&
             !"SunOS".equals(osName)) {
             System.err.println("SasXrwProviderMain cannot run on platform " + osName);
             System.err.println("Program terminated.");
             System.exit(1);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSession (SasProviderSession session) {
         this.session = session;
     }
 
     /**
      * {@inheritDoc}
      */
     public String launch (String appName, String command, Vector2f pixelScale) {
         AppXrwMaster app = null;
         try {
             app = new AppXrwMaster(appName, command, pixelScale, 
                                    ProcessReporterFactory.getFactory().create(appName), session, true);
         } catch (InstantiationException ex) {
             return null;
         }
 
         // Now it is time to enable the master client loop
         app.getClient().enable();
 
         return app.getConnectionInfo().toString();
     }
 }
 
