 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.outfit.shell;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.MissingResourceException;
 import java.util.regex.Pattern;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.apache.commons.io.FileUtils;
 
 /**
  * Installs the jar of the {@link org.novelang.outfit.shell.insider.InsiderAgent} somewhere
  * on the local filesystem.
  * The JVM installs agents from plain jar files.
  * <p>
  * This class applies the
  * <a href="http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom">Initialization on demand idiom</a>.
  *
  * @author Laurent Caillette
  */
 public class AgentFileInstaller {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( AgentFileInstaller.class ) ;
 
   /**
    * Name of the system property to set the agent jar file externally.
    * This is useful when running tests from an IDE.
    * The "{@value #VERSION_PLACEHOLDER}" substring evaluates to
    */
   public static final String AGENTJARFILE_SYSTEMPROPERTYNAME =
       "org.novelang.outfit.shell.agentjarfile" ;
 
   private static final String VERSION_PLACEHOLDER = "${project.version}" ;
 
   private static final String DEFAULT_VERSION = "SNAPSHOT" ;
 
   /**
    * Name of the system property to set the version overriding the content of the
    * {@value #VERSION_RESOURCE_NAME} resource.
    */
   public static final String VERSIONOVERRIDE_SYSTEMPROPERTYNAME =
       "org.novelang.outfit.shell.versionoverride" ;
 
   /**
    * Version of the embedded jar. Needed to find its resource name.
    */
   private final String version ;
 
   private final File jarFile ;
   public static final String VERSION_PLACEHOLDER_REGEX = Pattern.quote( VERSION_PLACEHOLDER );
 
   public static boolean mayHaveValidInstance() {
     return
            System.getProperty( VERSIONOVERRIDE_SYSTEMPROPERTYNAME ) != null 
         || AgentFileInstaller.class.getResource( VERSION_RESOURCE_NAME ) != null
     ;
   }
 
   private AgentFileInstaller() throws IOException {
 
     final String versionOverride = System.getProperty( VERSIONOVERRIDE_SYSTEMPROPERTYNAME ) ;
     if( versionOverride == null ) {
       final URL versionResource = AgentFileInstaller.class.getResource( VERSION_RESOURCE_NAME ) ;
       version = Resources.toString( versionResource, Charsets.UTF_8 ) ;
       LOGGER.info( "Using version '", version, "' as found inside ",
           "'", VERSION_RESOURCE_NAME, "' resource."  ) ;
 
     } else {
       version = versionOverride ;
       LOGGER.info( "Using version override '", version, "' from system property ",
           "'", VERSIONOVERRIDE_SYSTEMPROPERTYNAME, "'."  ) ;
     }
 
 
     final String jarFileNameFromSystemProperty =
         System.getProperty( AGENTJARFILE_SYSTEMPROPERTYNAME ) ;
 
     if( jarFileNameFromSystemProperty == null ) {
       try {
         jarFile = File.createTempFile( "Novelang-insider-agent", ".jar" ).getCanonicalFile() ;
         copyResourceToFile( JAR_RESOURCE_NAME_RADIX + version + ".jar", jarFile ) ;
         LOGGER.info( "Using jar file '", jarFile.getAbsolutePath(), "'." ) ;
       } catch( IOException e ) {
         throw new RuntimeException( e ) ;
       }
     } else {
       jarFile = resolveWithVersion( jarFileNameFromSystemProperty ) ;
       if( ! jarFile.isFile() ) {
         throw new IllegalArgumentException(
             "Jar file '" + jarFile.getAbsolutePath() + "' doesn't exist as a file" ) ;
       }
       LOGGER.info( "Using jar file '", jarFile.getAbsolutePath(), "' ",
           "set from system property '", AGENTJARFILE_SYSTEMPROPERTYNAME, "'." ) ;
     }
 
   }
 
   public File resolveWithVersion( final String filenameWithVersionPlaceholders ) {
     final String resolvedJarFileName = filenameWithVersionPlaceholders.replaceAll(
         VERSION_PLACEHOLDER_REGEX, version ) ;
     final File resolvedFile = new File( resolvedJarFileName );
     return resolvedFile;
   }
 
 
   /**
    * Returns a {@code File} object referencing the jar containing the
    * {@link org.novelang.outfit.shell.insider.InsiderAgent}.
    *
    * @return a non-null object.
    */
   public File getJarFile() {
     return jarFile ;
   }
 
 
 
   public void copyVersionedJarToFile( final String jarResourceRadix, final File file )
       throws IOException
   {
     copyResourceToFile( jarResourceRadix + version + ".jar", file ) ;
   }
 
   public static void copyResourceToFile( final String resourceName, final File file )
       throws IOException {
     final URL resourceUrl = AgentFileInstaller.class.getResource( resourceName ) ;
     if( resourceUrl == null ) {
       throw new MissingResourceException(
           "The resource '" + resourceName +"' does not appear in the classpath " +
               "(Maven should handle this, IDEs probably won't)",
           AgentFileInstaller.class.getName(),
           resourceName
       ) ;
     }
 
     LOGGER.info( "Copying resource '", resourceName, "' to ",
             "'", file.getAbsolutePath(), "'" ) ;
     FileUtils.copyURLToFile( resourceUrl, file ) ;
   }
 
   /**
    * Defined by the assembly of the Insider project.
    */
   @SuppressWarnings( { "HardcodedFileSeparator" } )
   private static final String JAR_RESOURCE_NAME_RADIX =
       "/org/novelang/outfit/shell/Novelang-insider-" ;
 
   /**
    * Defined by the assembly of the Insider project.
    */
   @SuppressWarnings( { "HardcodedFileSeparator" } )
   private static final String VERSION_RESOURCE_NAME=
       "/org/novelang/outfit/shell/version.txt" ;
 
 
 // ========================
 // Initialization on demand
 // ========================
 
   @SuppressWarnings( { "UtilityClassWithoutPrivateConstructor" } )
   private static class LazyHolder {
     private static final AgentFileInstaller INSTANCE ;
     static {
       try {
         INSTANCE = new AgentFileInstaller() ;
       } catch( IOException e ) {
         throw new RuntimeException( e ) ;
       }
     }
   }
 
   public static AgentFileInstaller getInstance() {
     return LazyHolder.INSTANCE ;
   }
 
 }
