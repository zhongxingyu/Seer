 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.service;
 
 import javax.sql.DataSource;
 import java.net.*;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 
 /**
  * Auxiliary class for viewing server info. Created automatically by spring IoC (singleton scope). It provides the most
  * important information about the last deployment like its time, database, server IP, etc.
  *
  * @author Evgeny Kapinos
  * @see <a href="http://jira.jtalks.org/browse/POULPE-405">JIRA</a>
  */
 public class DeploymentProperties {
     private final DataSource dataSource;
     private String deploymentDate;
     private DatabaseInfo dbInfo;
     private String serverIP;
 
     /** Constructor for initialization variables */
     DeploymentProperties(DataSource dataSource) {
 
         this.dataSource = dataSource;
 
     }
 
     /** Initialization of calculated variables */
     public void init() {
         this.deploymentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
         this.dbInfo = DatabaseInfo.databaseInfo(dataSource);
         this.serverIP = serverIP();
     }
 
 
     /**
      * Method collect information about used server IP. If sever has global Internet addresses, they return. If no
      * global dresses, method returns all found Intranet addresses. If sever hasn't any IP loopback returns.
      *
      * @return Server IP
      */
     private String serverIP() {
         StringBuilder globalAddressesSB = new StringBuilder();
         StringBuilder localAddressesSB = new StringBuilder();
         try {
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                 NetworkInterface networkInterface = en.nextElement();
                 // Also we should skip loopback address, if we didn't find anything better.
                 if (!networkInterface.isUp() || networkInterface.isVirtual() || networkInterface.isLoopback()) {
                     continue;
                 }
                 for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress == null) {
                        continue;
                    }
                     InetAddress inetAddress = interfaceAddress.getAddress();
                     if (inetAddress.isLinkLocalAddress()) {
                         continue;
                     }
                     if (!inetAddress.isSiteLocalAddress()) {
                         if (globalAddressesSB.length() > 0) {
                             globalAddressesSB.append(", ");
                         }
                         globalAddressesSB.append(inetAddress.getHostAddress());
                     } else {
                         if (localAddressesSB.length() > 0) {
                             localAddressesSB.append(", ");
                         }
                         localAddressesSB.append(inetAddress.getHostAddress());
                     }
                 }
             }
             if (globalAddressesSB.length() > 0) {
                 return globalAddressesSB.toString();
             } else if (localAddressesSB.length() > 0) {
                 return localAddressesSB.toString();
             } else {
                 // Nothing found. So we'll show default (loopback)
                 return InetAddress.getLocalHost().getHostAddress();
             }
         } catch (SocketException e) {
             // No info data
         } catch (UnknownHostException e) {
             // No info data
         }
         return null;
     }
 
     /**
      * Returns date the app was deployed last time.
      *
      * @return deployment date
      */
     public String getDeploymentDate() {
         return deploymentDate;
     }
 
     /**
      * Returns database server address the app was connected to.
      *
      * @return database server
      */
     public String getDatabaseServer() {
         return dbInfo.getDatabaseServer();
     }
 
     /**
      * Returns database name Poulpe is connected to.
      *
      * @return database name
      */
     public String getDatabaseName() {
         return dbInfo.getDatabaseName();
     }
 
     /**
      * Returns database user Poulpe uses to connect to DB.
      *
      * @return database user
      */
     public String getDatabaseUser() {
         return dbInfo.getDatabaseUser();
     }
 
     /**
      * Returns current server IP Poulpe is deployed to.
      *
      * @return web server IP
      */
     public String getServerIP() {
         return serverIP;
     }
 
     /**
      * Auxiliary nested class, which contain all necessary info about database. Use {@link
      * #databaseInfo(javax.sql.DataSource)} method to build its instance.
      */
     private static class DatabaseInfo {
         private final String databaseServer;
         private final String databaseName;
         private final String databaseUser;
 
         private DatabaseInfo(String databaseServer, String databaseName, String databaseUser) {
             this.databaseServer = databaseServer;
             this.databaseName = databaseName;
             this.databaseUser = databaseUser;
         }
 
         /**
          * Collects the information about the database and puts it into the {@link DatabaseInfo} instance.
          *
          * @return database info
          */
         public static DatabaseInfo databaseInfo(DataSource dataSource) {
             String databaseName = "";
             String databaseUser = "";
             String databaseServer = "";
             try {
                 Connection connection = dataSource.getConnection();
                 databaseName = connection.getCatalog();
                 DatabaseMetaData connectionMetadata = connection.getMetaData();
                 databaseUser = connectionMetadata.getUserName();
 
                 String url = connectionMetadata.getURL();
                 String cleanURI = url.substring(5); // subtarct "jdbc:"
                 URI uri = URI.create(cleanURI);
                 databaseServer = (uri.getHost() != null ? uri.getHost() : "N/A") + (uri.getPort() == -1 ? "" : String.valueOf(uri.getPort()));
 
                 connection.close();
             } catch (SQLException e) {
                 // leave fields empty, it's a business case
             }
 
             return new DatabaseInfo(databaseServer, databaseName, databaseUser);
         }
 
         public String getDatabaseServer() {
             return databaseServer;
         }
 
         public String getDatabaseName() {
             return databaseName;
         }
 
         public String getDatabaseUser() {
             return databaseUser;
         }
     }
 
 }
