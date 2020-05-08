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
 
 package org.jboss.ashiaro.build.plugin;
 
import java.util.Enumeration;
 import java.util.Properties;
import java.io.File;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Sets system properties.
 * 
  * @goal set-systemid-properties
  * @phase initialize
  */
 public class SetSystemIdPropertiesMojo
     extends AbstractMojo
 {
 
     private static Properties props;
     private static String     platform = null;
     private static String     model    = null;
     private static String     syscpu   = null;
 
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     static {
         props = System.getProperties();
     }
 
     /* Copy of the {@code SystemId.getSysname} method.
      * Make sure those methods are in sync!
      */
     private static String getPlatform()
     {
         if (platform != null)
             return platform;
         final String name = props.getProperty("os.name").toLowerCase();
 
         if (name.startsWith("windows"))
             platform = "windows";
         else if (name.startsWith("mac os") || name.startsWith("darwin"))
             platform = "macosx";
         else if (name.startsWith("linux"))
             platform = "linux";
         else if (name.startsWith("sunos") || name.startsWith("solaris"))
             platform = "solaris";
         else if (name.startsWith("hp-ux"))
             platform = "hpux";
         else if (name.startsWith("freebsd"))
             platform = "freebsd";
         else if (name.startsWith("openbsd"))
             platform = "openbsd";
         else
             platform = "unknown";
 
         return platform;
     }
 
     /* Returns windows or unix
      */
     private static String getFamily()
     {
         if (getPlatform().equals("windows"))
             return "windows";
         else
             return "unix";
     }
 
     private static String getDataModel()
     {
         return model;
     }
 
     /* Copy of the {@code SystemId.getProcessor} method.
      * Make sure those methods are in sync!
      */
     private static String getArchitecture()
     {
         if (syscpu != null)
             return syscpu;
         final String arch = props.getProperty("os.arch").toLowerCase();
         final String data = props.getProperty("sun.arch.data.model");
 
         if (data == null) {
             if (arch.indexOf("64") == -1) {
                 /* TODO: Investigate other JVM's property for
                  * figuring the data model (32 or 64)
                  */
                 model = "32";
             }
             else {
                 model = "64";
             }
         }
         else {
             model = data;
         }
         if (arch.endsWith("86")) {
             syscpu = "i686";
         }
         else if (arch.startsWith("ia64")) {
             if (model.equals("64"))
                 syscpu = "ia64";
             else
                 syscpu = "i686";
         }
         else if (arch.startsWith("sparc")) {
             if (model.equals("64"))
                 syscpu = "sparc64";
             else
                 syscpu = "sparc32";
         }
         else if (arch.startsWith("ppc") || arch.startsWith("power")) {
             if (model.equals("64"))
                 syscpu = "ppc64";
             else
                 syscpu = "ppc32";
         }
         else if (arch.equals("amd64") || arch.equals("x86_64")) {
             syscpu = "x86_64";
         }
         else {
             syscpu = arch;
         }
         return syscpu;
     }
 
     // Mojo methods -----------------------------------------------------------
 
     /**
      * {@inheritDoc}
      */
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
         project.getProperties().setProperty("systemid.arch",    getArchitecture());
         project.getProperties().setProperty("systemid.name",    getPlatform());
         project.getProperties().setProperty("systemid.family",  getFamily());
         project.getProperties().setProperty("systemid.bits",    getDataModel());
 
         getLog().info("SystemId: " + getPlatform() + "-" + getArchitecture() +
                       " --- " + getFamily() + "/" + getDataModel() + "-bits");
     }
 }
