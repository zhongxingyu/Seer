 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.session.SessionManager;
 import org.wings.util.PropertyUtils;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public class CSSLookAndFeel
         extends org.wings.plaf.LookAndFeel {
 
     private final transient static Log log = LogFactory.getLog(CSSLookAndFeel.class);
    private static final String PROPERTIES_FILENAME_DEFAULT = "default";
     private static final String PROPERTIES_FILENAME_END = ".properties";
    private static final String PROPERTIES_CLASSPATH = CSSLookAndFeel.class.getPackage().getName().replace('.','/').concat("/");
 
     public CSSLookAndFeel() throws IOException {
         super(loadProperties());
     }
 
     private static Properties loadProperties() throws IOException {
         // default properties
        StringBuffer propertiesFilename = new StringBuffer(PROPERTIES_CLASSPATH);
        propertiesFilename.append(PROPERTIES_FILENAME_DEFAULT);
         propertiesFilename.append(PROPERTIES_FILENAME_END);
         // browser dependent properties
         String browserType = SessionManager.getSession().getUserAgent().getBrowserType().getShortName();
 
        StringBuffer browserPropertiesFilename = new StringBuffer(PROPERTIES_CLASSPATH);
         browserPropertiesFilename.append(browserType);
         browserPropertiesFilename.append(PROPERTIES_FILENAME_END);
 
        Properties properties = PropertyUtils.loadProperties(propertiesFilename.toString());
         try {
            properties.putAll(PropertyUtils.loadProperties(browserPropertiesFilename.toString()));
         } catch (IOException e) {
            log.info("Unable to open browser specific properties file '"+browserPropertiesFilename.toString()+"'. This is OK.");
         }
         return properties;
     }
 }
 
 
