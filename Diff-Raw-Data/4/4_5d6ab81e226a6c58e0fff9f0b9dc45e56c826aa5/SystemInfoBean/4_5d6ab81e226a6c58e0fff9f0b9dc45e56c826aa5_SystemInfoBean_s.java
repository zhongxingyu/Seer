 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.media.FxMediaEngine;
 import org.apache.commons.lang.StringUtils;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.Map;
 
 /**
  * JSF Bean exposing miscellaneous system/runtime parameters.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class SystemInfoBean {
 
     public Date getDateTime() {
         return new Date();
     }
 
     public String getJavaVersion() {
         return System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")";
     }
 
     public int getProcessors() {
         return Runtime.getRuntime().availableProcessors();
     }
 
     public String getFreeMemoryMB() {
         return new Formatter().format("%.2f MB (of %.2f MB, max. %.2f MB)",
                 (double) Runtime.getRuntime().freeMemory() / 1024. / 1024.,
                 (double) Runtime.getRuntime().totalMemory() / 1024. / 1024.,
                 (double) Runtime.getRuntime().maxMemory() / 1024. / 1024.).toString();
     }
 
     public String getOperatingSystem() {
         return System.getProperty("os.name") + " " + System.getProperty("os.arch");
     }
 
     public boolean isIMAvailable() {
         return FxMediaEngine.hasImageMagickInstalled();
     }
 
     public String getIMVersion() {
         return FxMediaEngine.getImageMagickVersion();
     }
 
     public boolean isUseIMIdentify() {
         return FxMediaEngine.isImageMagickIdentifySupported();
     }
 
     public String getApplicationServerName() {
         if (System.getProperty("product.name") != null) {
             String ver =  System.getProperty("product.name");
             if (System.getProperty("com.sun.jbi.domain.name") != null)
                ver = " Domain: " + System.getProperty("com.sun.jbi.domain.name");
             return ver;
         } else if (System.getProperty("jboss.home.dir") != null) {
             try {
                 final Class<?> cls = Class.forName("org.jboss.Version");
                 Method m = cls.getMethod("getInstance");
                 Object v = m.invoke(null);
                 Method pr = cls.getMethod("getProperties");
                 Map props = (Map)pr.invoke(v);
                 String ver = "JBoss";
                 if( props.containsKey("version.major") && props.containsKey("version.minor")) {
                     if( props.containsKey("version.name"))
                         ver = ver + " [" + props.get("version.name")+"]";
                     ver = ver + " "+props.get("version.major") + "." + props.get("version.minor");
                     if( props.containsKey("version.revision"))
                         ver = ver + "." + props.get("version.revision");
                     if( props.containsKey("version.tag"))
                         ver = ver + " " + props.get("version.tag");
                     if( props.containsKey("build.day"))
                         ver = ver + " built "+props.get("build.day");
                 }
                 return ver;
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             }
             return "JBoss";
         } else if (System.getProperty("openejb.version") != null) {
             // try to get Jetty version
             String jettyVersion = "";
             try {
                 final Class<?> cls = Class.forName("org.mortbay.jetty.Server");
                 jettyVersion = " (Jetty "
                         + cls.getClass().getPackage().getImplementationVersion()
                         + ")";
             } catch (ClassNotFoundException e) {
                 // no Jetty version...
             }
             return "OpenEJB " + System.getProperty("openejb.version") + jettyVersion;
         } else if (System.getProperty("weblogic.home") != null) {
             String server = System.getProperty("weblogic.Name");
             String wlVersion = "";
             try {
                 final Class<?> cls = Class.forName("weblogic.common.internal.VersionInfo");
                 Method m = cls.getMethod("theOne");
                 Object serverVersion = m.invoke(null);
                 Method sv = m.invoke(null).getClass().getMethod("getImplementationVersion");
                 wlVersion = " " + String.valueOf(sv.invoke(serverVersion));
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             }
             if (StringUtils.isEmpty(server))
                 return "WebLogic" + wlVersion;
             else
                 return "WebLogic" + wlVersion + " (server: " + server + ")";
         } else if (System.getProperty("org.apache.geronimo.home.dir") != null) {
             String gVersion = "";
             try {
                 final Class<?> cls = Class.forName("org.apache.geronimo.system.serverinfo.ServerConstants");
                 Method m = cls.getMethod("getVersion");
                 gVersion = " " + String.valueOf(m.invoke(null));
                 m = cls.getMethod("getBuildDate");
                 gVersion = gVersion + " ("+String.valueOf(m.invoke(null))+")";
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             }
             return "Apache Geronimo "+gVersion;
         } else {
             return "unknown";
         }
     }
 
     public String getDatabaseInfo() {
         return EJBLookup.getDivisionConfigurationEngine().getDatabaseInfo();
     }
 
     public Long getDatabaseSchemaVersion() throws FxApplicationException {
         return EJBLookup.getDivisionConfigurationEngine().get(SystemParameters.DB_VERSION);
     }
 }
