 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb and izpack)
  * Copyright (C) 2000-2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 14 June 2007
  * Elmar Geese, CEO tarent GmbH.
  */
 
 /* $Id: AbstractPackagingMojo.java,v 1.16 2007/08/07 11:29:59 robert Exp $
  *
  * maven-pkg-plugin, Packaging plugin for Maven2 
  * Copyright (C) 2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'maven-pkg-plugin'
  * written by Robert Schuster, Fabian Koester. 
  * signature of Elmar Geese, 1 June 2002
  * Elmar Geese, CEO tarent GmbH
  */
 
 package de.tarent.maven.plugins.pkg;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.codehaus.plexus.util.FileUtils;
 
 /**
  * Base Mojo for all packaging mojos. It provides convenient access to a mean to
  * resolve the project's complete dependencies.
  */
 public abstract class AbstractPackagingMojo extends AbstractMojo
 {
 
   /**
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   protected MavenProject project;
 
   /**
    * Artifact factory, needed to download source jars.
    * @component role="org.apache.maven.project.MavenProjectBuilder"
    * @required
    * @readonly
    */
   protected MavenProjectBuilder mavenProjectBuilder;
 
   /**
    * Temporary directory that contains the files to be assembled.
    * 
    * @parameter expression="${project.build.directory}"
    * @required
    * @readonly
    */
   protected File buildDir;
   
   /**
    * Used to look up Artifacts in the remote repository.
    * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
    * @required
    * @readonly
    */
   protected ArtifactFactory factory;
 
   /**
    * Used to look up Artifacts in the remote repository.
    * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
    * @required
    * @readonly
    */
   protected ArtifactResolver resolver;
 
   /**
    * Used to look up Artifacts in the remote repository.
    * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
    * @required
    * @readonly
    */
   protected ArtifactMetadataSource metadataSource;
 
   /**
    * Location of the local repository.
    * @parameter expression="${localRepository}"
    * @readonly
    * @required
    */
   protected ArtifactRepository local;
 
   /**
    * List of Remote Repositories used by the resolver
    * @parameter expression="${project.remoteArtifactRepositories}"
    * @readonly
    * @required
    */
   protected List remoteRepos;
 
   /**
    * @parameter expression="${project.artifact}"
    * @required
    * @readonly
    */
   protected Artifact artifact;
 
   /**
    * @parameter expression="${project.artifactId}"
    * @required
    * @readonly
    */
   protected String artifactId;
 
   /**
    * Look up Archiver/UnArchiver implementations.
    * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
    * @required
    * @readonly
    */
   protected ArchiverManager archiverManager;
   
   /**
    * @parameter expression="${project.build.finalName}"
    * @required
    * @readonly
    */
   protected String finalName;
 
   /**
    * @parameter expression="${project.build.directory}"
    * @required
    * @readonly
    */
   protected File outputDirectory;
 
   /**
    * @parameter expression="${project.version}"
    * @required
    * @readonly
    */
   protected String version;
 
   /**
    * JVM binary used to run Java programs from within the Mojo.
    * 
    * @parameter expression="${javaExec}" default-value="java"
    * @required
    * 
    */
   protected String javaExec;
 
   /**
    * 7Zip binary used to run Java programs from within the Mojo.
    * 
    * @parameter expression="${7zipExec}" default-value="7zr"
    * @required
    * 
    */
   protected String _7zipExec;
   
   /**
    * Location of the custom package map file. When specifying this one
    * the internal package map will be overridden completely. 
    * 
    * @parameter expression="${defPackageMapURL}"
    */
   protected URL defaultPackageMapURL;
 
   /**
    * Location of the auxiliary package map file. When this is specified
    * the information in the document will be added to the default one.
    * 
    * @parameter expression="${auxPackageMapURL}"
    */
   protected URL auxPackageMapURL;
 
 
   /**
    * Set default distribution to package for.
    * 
    * @parameter expression="${defaultDistro}"
    * @required
    */
   protected String defaultDistro;
 
   /**
    * Overrides "defaultDistro" parameter. For use on the command-line. 
    * 
    * @parameter expression="${distro}"
    */
   protected String distro;
 
   /**
    * Set default target configuration to package for.
    * 
    * @parameter expression="${defaultTarget}"
    * @required
    */
   protected String defaultTarget;
 
   /**
    * Overrides "defaultTarget" parameter. For use on the command-line. 
    * 
    * @parameter expression="${target}"
    */
   protected String target;
 
   /**
    * Gathers the project's artifacts and the artifacts of all its (transitive)
    * dependencies filtered by the given filter instance.
    * @param filter
    * @return
    * @throws ArtifactResolutionException
    * @throws ArtifactNotFoundException
    * @throws ProjectBuildingException
    * @throws InvalidDependencyVersionException
    */
   protected final Set findArtifacts(ArtifactFilter filter)
       throws ArtifactResolutionException, ArtifactNotFoundException,
       ProjectBuildingException, InvalidDependencyVersionException
   {
     Set deps = project.createArtifacts(factory, Artifact.SCOPE_COMPILE, filter);
 
     ArtifactResolutionResult result = resolver.resolveTransitively(
                                                                    deps,
                                                                    artifact,
                                                                    local,
                                                                    remoteRepos,
                                                                    metadataSource,
                                                                    filter);
 
     return result.getArtifacts();
   }
 
   /**
    * Gathers the project's artifacts and the artifacts of all its (transitive)
    * compilation (and implicitly runtime) dependencies.
    * @param filter
    * @return
    * @throws ArtifactResolutionException
    * @throws ArtifactNotFoundException
    * @throws ProjectBuildingException
    * @throws InvalidDependencyVersionException
    */
   protected final Set findArtifacts() throws ArtifactResolutionException,
       ArtifactNotFoundException, ProjectBuildingException,
       InvalidDependencyVersionException
   {
     return findArtifacts(new ScopeArtifactFilter(Artifact.SCOPE_COMPILE));
   }
 
   /**
    * Copies the Artifacts contained in the set to the folder denoted by
    * <code>dst</code> and returns the amount of bytes copied.
    * 
    * <p>If an artifact is a zip archive it is unzipped in this folder.</p>
    * 
    * @param l
    * @param artifacts
    * @param dst
    * @return
    * @throws MojoExecutionException
    */
   protected final long copyArtifacts(Log l, Set artifacts, File dst)
       throws MojoExecutionException
   {
     long byteAmount = 0;
 
     if (artifacts.size() == 0)
       {
         l.info("no artifact to copy.");
         return byteAmount;
       }
 
     l.info("copying " + artifacts.size() + " dependency artifacts.");
     l.info("destination: " + dst.toString());
 
     try
       {
         Iterator ite = artifacts.iterator();
         while (ite.hasNext())
           {
             Artifact a = (Artifact) ite.next();
             l.info("copying artifact: " + a);
             File f = a.getFile();
             if (f != null)
               {
                 l.debug("from file: " + f);
                 if (a.getType().equals("zip"))
                   {
                     // Assume that this is a ZIP file with native libraries
                     // inside.
                     
                     // TODO: Determine size of all entries and add this
                     // to the byteAmount.
                     unpack(a.getFile(), dst);
                   }
                 else
                   {
                     FileUtils.copyFileToDirectory(f, dst);
                     byteAmount += (long) f.length();
                   }
 
               }
             else
               throw new MojoExecutionException(
                                                "Unable to copy Artifact "
                                                    + a
                                                    + " because it is not locally available.");
           }
       }
     catch (IOException ioe)
       {
         throw new MojoExecutionException(
                                          "IOException while copying dependency artifacts.",ioe);
       }
 
     return byteAmount;
   }
 
   /**
    * Unpacks the given file.
    * @param file
    *          File to be unpacked.
    * @param dst
    *          Location where to put the unpacked files.
    */
   protected void unpack(File file, File dst) throws MojoExecutionException
   {
     try
       {
         dst.mkdirs();
 
         UnArchiver unArchiver = archiverManager.getUnArchiver(file);
         unArchiver.setSourceFile(file);
         unArchiver.setDestDirectory(dst);
         unArchiver.extract();
       }
     catch (NoSuchArchiverException e)
       {
         throw new MojoExecutionException("Unknown archiver type", e);
       }
     catch (ArchiverException e)
       {
         throw new MojoExecutionException("Error unpacking file: " + file
                                          + " to: " + dst + "\r\n"
                                          + e.toString(), e);
       }
   }
   
   /**
    * Makes the version string compatible to the system's requirements.
    * 
    * @param v
    * @return
    */
   protected final String fixVersion(String v)
   {
     int i = v.indexOf("-SNAPSHOT");
     if (i > 0)
       return v.substring(0, i) + "~SNAPSHOT~" + createSnapshotTimestamp();
   
     return v;
   }
   
   /**
    * Returns a String representing the current time (UTC) in format yyyyMMddHHmmss
    * 
    * @return
    */
   private final String createSnapshotTimestamp() {
 	  
 	return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());  
   }
   
   /**
    * Makes a valid and useful package version string from one that cannot
    * be used or is unsuitable.
    *  
    * <p>At the moment the method removes underscores only.</p>
    * 
    * @param string The string which might contain underscores
    * @return The given string without underscores
    */
   protected final String sanitizePackageVersion(String string) {
 	  
 	  return string.replaceAll("_", "");	
   }
   
 }
