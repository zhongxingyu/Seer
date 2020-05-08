 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.as.security.providers.extension;
 
 import java.io.ByteArrayInputStream;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.security.PrivilegedExceptionAction;
 import java.security.Provider;
 import java.security.Security;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.logging.Logger;
 import org.jboss.msc.service.Service;
 import org.jboss.msc.service.ServiceName;
 import org.jboss.msc.service.StartContext;
 import org.jboss.msc.service.StartException;
 import org.jboss.msc.service.StopContext;
 
 /**
  * The SunPKCS11Service instance registers and removes a SunPKCS11 security provider.
  * 
  * @author Josef Cacek
  */
 public class SunPKCS11Service implements Service<SunPKCS11Service> {
 
     private static final String SUN_PKCS11_CLASS_NAME = "sun.security.pkcs11.SunPKCS11";
     private static final Logger LOGGER = Logger.getLogger(SunPKCS11Service.class);
 
     private final String name;
     private final Map<String, String> attributes;
     private String providerName;
 
     // Constructors ----------------------------------------------------------
 
     /**
      * Create a new SunPKCS11Service.
      * 
      * @param name
      * @param attributes
      */
     public SunPKCS11Service(String name, Map<String, String> attributes) {
         super();
         LOGGER.debug("Creating SunPKCS11 service: " + name);
         this.name = name;
         this.attributes = new HashMap<String, String>();
         if (attributes != null) {
             this.attributes.putAll(attributes);
         }
     }
 
     // Public methods --------------------------------------------------------
 
     /**
      * Returns this instance.
      * 
      * @return
      * @throws IllegalStateException
      * @throws IllegalArgumentException
      * @see org.jboss.msc.value.Value#getValue()
      */
     public SunPKCS11Service getValue() throws IllegalStateException, IllegalArgumentException {
         return this;
     }
 
     /**
      * Adds (registers) a SunPKCS11 security provider.
      * 
      * @param context
      * @throws StartException
      * @see org.jboss.msc.service.Service#start(org.jboss.msc.service.StartContext)
      */
     public void start(StartContext context) throws StartException {
         LOGGER.info("Adding SunPKCS11 security provider: " + name);
         final StringBuilder sb = new StringBuilder();
         appendConfigLine(sb, "name", name);
         if (attributes != null) {
             for (Map.Entry<String, String> attr : attributes.entrySet()) {
                 appendConfigLine(sb, attr.getKey(), attr.getValue());
             }
         }
         try {
             SecurityManager sm = System.getSecurityManager();
 
             if (sm != null) {
                 AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                     public Object run() throws Exception {
                         final Provider sunPKCS11Provider = (Provider) Class.forName(SUN_PKCS11_CLASS_NAME)
                                 .getConstructor(java.io.InputStream.class)
                                 .newInstance(new ByteArrayInputStream(sb.toString().getBytes()));
                         Security.addProvider(sunPKCS11Provider);
                        providerName = sunPKCS11Provider.getName();
                         return null;
                     }
                 });
             } else {
                 final Provider sunPKCS11Provider = (Provider) Class.forName(SUN_PKCS11_CLASS_NAME)
                         .getConstructor(java.io.InputStream.class)
                         .newInstance(new ByteArrayInputStream(sb.toString().getBytes()));
                Security.addProvider(sunPKCS11Provider);
                 providerName = sunPKCS11Provider.getName();
             }
         } catch (Exception e) {
             LOGGER.error("Adding SunPKCS11 security provider " + name + " failed.", e);
             new StartException("Unable to register SunPKCS11 provider", e);
         }
     }
 
     /**
      * Removes the SunPKCS11 security provider.
      * 
      * @param context
      * @see org.jboss.msc.service.Service#stop(org.jboss.msc.service.StopContext)
      */
     public void stop(StopContext context) {
         LOGGER.info("Removing SunPKCS11 security provider: " + name);
         SecurityManager sm = System.getSecurityManager();
         if (sm != null) {
             AccessController.doPrivileged(new PrivilegedAction<Object>() {
                 public Object run() {
                     Security.removeProvider(providerName);
                     return null;
                 }
             });
         } else {
             Security.removeProvider(providerName);
         }
 
     }
 
     /**
      * Creates a service name for the given SunPKCS11 provider name.
      * 
      * @param providerName
      * @return
      */
     public static ServiceName createServiceName(final String providerName) {
         return ServiceName.JBOSS.append("security-providers", "sunpkcs11", providerName);
     }
 
     // Private methods -------------------------------------------------------
 
     /**
      * Adds a config line (name=value pair) to the given {@link StringBuilder} instance.
      * 
      * @param sb
      * @param name
      * @param value
      */
     private void appendConfigLine(StringBuilder sb, String name, String value) {
         sb.append(name).append("=").append(value).append("\n");
     }
 }
