 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
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
 
 package com.elasticgrid.boot;
 
 import org.rioproject.boot.BootUtil;
 import org.rioproject.boot.RioServiceDescriptor;
 import com.sun.jini.start.ServiceDescriptor;
 import java.io.File;
 import java.io.IOException;
 
 public class ServiceDescriptorUtil extends org.rioproject.boot.ServiceDescriptorUtil {
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.monitor.ProvisionMonitor} using the Webster port created
      * by this utility.
      *
      * @param policy The security policy file to use
      * @param monitorConfig The configuration options the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getMonitor(String policy, String monitorConfig) throws IOException {
         return getMonitor(policy, getAnonymousPort(), monitorConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.monitor.ProvisionMonitor} using the Webster port created
      * by this utility.
      *
      * @param policy The security policy file to use
      * @param monitorConfig The configuration options the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getMonitor(String policy, String... monitorConfig) throws IOException {
         return getMonitor(policy, getAnonymousPort(), monitorConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.monitor.ProvisionMonitor}.
      *
      * @param policy The security policy file to use
      * @param port The port to use when constructing the codebase
      * @param monitorConfig The configuration options the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using the provided port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getMonitor(String policy, int port, String... monitorConfig) throws IOException {
         return getMonitor(policy, BootUtil.getHostAddress(), port, monitorConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.monitor.ProvisionMonitor}.
      *
      * @param policy The security policy file to use
      * @param hostAddress The address to use when constructing the codebase
      * @param port The port to use when constructing the codebase
      * @param monitorConfig The configuration options the Monitor will use
      *
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using the provided port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getMonitor(String policy, String hostAddress, int port,
                                                String... monitorConfig) throws IOException {
         String egHome = System.getProperty("EG_HOME");
         if (egHome == null)
             throw new RuntimeException("EG_HOME property not declared");
         String monitorRoot = egHome + File.separator + "lib" + File.separator + "elastic-grid";
         String monitorClasspath =
             egHome + File.separator + "lib" + File.separator + "monitor.jar"
             + File.pathSeparator +
             monitorRoot + File.separator + getJarName(monitorRoot, "elastic-grid-deploy-handlers");
         String monitorCodebase = BootUtil.getCodebase(new String[]{
             "monitor-dl.jar",
             "rio-dl.jar",
             "jsk-dl.jar"}, hostAddress, Integer.toString(port));
         String implClass = "org.rioproject.monitor.ProvisionMonitorImpl";
         return new RioServiceDescriptor(monitorCodebase,
                                         policy,
                                         monitorClasspath,
                                         implClass,
                                         monitorConfig);
 
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.cybernode.Cybernode}.
      *
      * @param policy The security policy file to use
      * @param agentConfig The configuration options the Agent will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getAgent(String policy, String agentConfig) throws IOException {
         return getAgent(policy, getAnonymousPort(), agentConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.cybernode.Cybernode}.
      *
      * @param policy The security policy file to use
      * @param agentConfig The configuration options the Agent will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getAgent(String policy, String... agentConfig) throws IOException {
         return getAgent(policy, getAnonymousPort(), agentConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.cybernode.Cybernode}.
      *
      * @param policy The security policy file to use
      * @param port The port to use when constructing the codebase
      * @param agentConfig The configuration options the Agent will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using the provided port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not set
      */
     public static ServiceDescriptor getAgent(String policy, int port, String... agentConfig) throws IOException {
         return getAgent(policy, BootUtil.getHostAddress(), port, agentConfig);
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for {@link
      * org.rioproject.monitor.ProvisionMonitor}.
      *
      * @param policy The security policy file to use
      * @param hostAddress The address to use when constructing the codebase
      * @param port The port to use when constructing the codebase
      * @param agentConfig The configuration options the Monitor will use
      *
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using the provided port.
      *
      * @throws IOException If there are problems getting the anonymous port
      * @throws RuntimeException If the <tt>EG_HOME</tt> system property is not
      * set
      */
     public static ServiceDescriptor getAgent(String policy,
                                              String hostAddress,
                                              int port,
                                              String... agentConfig) throws IOException {
         String egHome = System.getProperty("EG_HOME");
         if (egHome == null)
             throw new RuntimeException("EG_HOME property not declared");
        String agentRoot = egHome + File.separator + "lib" + File.separator + "elastic-grid";
         String agentClasspath =
            egHome + File.separator + "lib" + File.separator + "cybernode.jar"
            + File.pathSeparator +
            agentRoot + File.separator + getJarName(agentRoot, "amazon-ec2-provisioner");
         String agentCodebase = BootUtil.getCodebase(new String[]{
             "cybernode-dl.jar",
             "rio-dl.jar",
             "jsk-dl.jar"}, hostAddress, Integer.toString(port));
         String implClass = "org.rioproject.cybernode.CybernodeImpl";
         return new RioServiceDescriptor(agentCodebase,
                                         policy,
                                         agentClasspath,
                                         implClass,
                                         agentConfig);
     }
 
 
     public static ServiceDescriptor getRestApi(String policy, String restApiConfig) throws IOException {
         return getRestApi(policy, getAnonymousPort(), restApiConfig);
     }
 
     public static ServiceDescriptor getRestApi(String policy, String... restApiConfigs) throws IOException {
         return getRestApi(policy, getAnonymousPort(), restApiConfigs);
     }
 
     public static ServiceDescriptor getRestApi(String policy, int port, String... restApiConfig) throws IOException {
         return getRestApi(policy, BootUtil.getHostAddress(), port, restApiConfig);
     }
 
     public static ServiceDescriptor getRestApi(String policy, String hostAddress, int port,
                                                String... restApiConfig) throws IOException {
         String egHome = System.getProperty("EG_HOME");
         if (egHome == null)
             throw new RuntimeException("EG_HOME property not declared");
         String restApiRoot = egHome + File.separator + "lib" + File.separator + "elastic-grid";
         String restApiClasspath = restApiRoot + File.separator + getJarName(restApiRoot, "rest-api");
         String restApiCodebase = BootUtil.getCodebase(new String[]{"rio-dl.jar",
                                                                    "jsk-dl.jar"},
                                                       hostAddress,
                                                       Integer.toString(port));
         String implClass = "com.elasticgrid.rest.RestJSB";
         return new RioServiceDescriptor(restApiCodebase,
                                         policy,
                                         restApiClasspath,
                                         implClass,
                                         restApiConfig);
     }
 
     private static String getJarName(String egHome, String nameNoVersion) {
         String jarName = null;
         File f = new File(egHome);
         for(String s : f.list()) {
             if(s.startsWith(nameNoVersion)) {
                 jarName = s;
                 break;
             }
         }
         return jarName;
     }
 
 }
