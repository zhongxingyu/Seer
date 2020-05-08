 /*
  * Fabric3
  * Copyright (c) 2009 Metaform Systems
  *
  * Fabric3 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version, with the
  * following exception:
  *
  * Linking this software statically or dynamically with other
  * modules is making a combined work based on this software.
  * Thus, the terms and conditions of the GNU General Public
  * License cover the whole combination.
  *
  * As a special exception, the copyright holders of this software
  * give you permission to link this software with independent
  * modules to produce an executable, regardless of the license
  * terms of these independent modules, and to copy and distribute
  * the resulting executable under terms of your choice, provided
  * that you also meet, for each linked independent module, the
  * terms and conditions of the license of that module. An
  * independent module is a module which is not derived from or
  * based on this software. If you modify this software, you may
  * extend this exception to your version of the software, but
  * you are not obligated to do so. If you do not wish to do so,
  * delete this exception statement from your version.
  *
  * Fabric3 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the
  * GNU General Public License along with Fabric3.
  * If not, see <http://www.gnu.org/licenses/>.
  *
  * ----------------------------------------------------
  *
  * Portions originally based on Apache Tuscany 2007
  * licensed under the Apache 2.0 license.
  *
  */
 package org.fabric3.assembly;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Plugin that builds a Fabric3 runtime image based on a set of profiles and/or extensions. Standalone and Tomcat runtime images are supported.
  *
  * @version $Rev$ $Date$
  * @goal fabric3-assembly
  * @phase generate-resources
  */
 public class Fabric3RuntimeAssemblyMojo extends AbstractMojo {
     private static final int BUFFER = 2048;
     private static final String RUNTIME_STANDALONE = "standalone";
     private static final String RUNTIME_TOMCAT = "tomcat";
 
     /**
      * Directory where the runtime image is built.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     public File buildDirectory;
 
     /**
      * Project source directory.
      *
      * @parameter expression="${project.build.sourceDirectory}"
      * @required
      */
     public File sourceDirectory;
 
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
      * @required
      * @readonly
      */
     public ArtifactFactory artifactFactory;
 
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
      * @required
      * @readonly
      */
     public ArtifactResolver resolver;
 
     /**
      * Location of the local repository.
      *
      * @parameter expression="${localRepository}"
      * @readonly
      * @required
      */
     public ArtifactRepository localRepository;
 
     /**
      * List of Remote Repositories used by the resolver
      *
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @readonly
      * @required
      */
     public List remoteRepositories;
 
     /**
      * The default version of the runtime to use.
      *
      * @parameter expression="RELEASE"
      */
     public String runtimeVersion;
 
     /**
      * Sets the runtime type, standalone or Tomcat
      *
      * @parameter
      */
     public String type = "standalone";
 
     /**
      * Set of profiles for the runtime.
      *
      * @parameter
      */
     public Dependency[] profiles = new Dependency[0];
 
     /**
      * Set of extensions for the runtime.
      *
      * @parameter
      */
     public Dependency[] extensions = new Dependency[0];
 
     /**
      * Set of configuration files
      *
      * @parameter
      */
     public ConfigFile[] configurationFiles = new ConfigFile[0];
 
     /**
      * Set of extensions for the runtime.
      *
      * @parameter
      */
     public Dependency[] removeExtensions = new Dependency[0];
 
     public Fabric3RuntimeAssemblyMojo() throws ParserConfigurationException {
     }
 
     public void execute() throws MojoExecutionException {
         String artifactId;
         File baseDirectory = new File(buildDirectory, "image");
         baseDirectory.mkdirs();
         File rootDirectory;
         if (RUNTIME_STANDALONE.equalsIgnoreCase(type)) {
             artifactId = "runtime-standalone";
             rootDirectory = baseDirectory;
         } else if (RUNTIME_TOMCAT.equalsIgnoreCase(type)) {
             artifactId = "runtime-tomcat";
             // tomcat is installed as <tomcat home>/fabric3
             rootDirectory = new File(baseDirectory, "fabric3");
         } else {
             throw new MojoExecutionException("Invalid runtime type specified: " + type);
         }
         extractRuntime("org.codehaus.fabric3", artifactId, baseDirectory);
         installProfiles(rootDirectory);
         installExtensions(rootDirectory);
         installConfiguration(rootDirectory);
         removeExtensions(rootDirectory);
     }
 
     /**
      * Resolves and unzips the contents of a base runtime distribution to a directory.
      *
      * @param groupId       the distribution group id
      * @param artifactId    the distribution artifact id
      * @param baseDirectory the extract directory
      * @throws MojoExecutionException if there is an error extracting the distribution
      */
     private void extractRuntime(String groupId, String artifactId, File baseDirectory) throws MojoExecutionException {
         String version = runtimeVersion;
         Artifact runtimeArtifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, "zip", "bin");
         try {
             getLog().info("Installing the Fabric3 runtime");
             resolver.resolve(runtimeArtifact, remoteRepositories, localRepository);
             File source = runtimeArtifact.getFile();
             extract(source, baseDirectory);
         } catch (ArtifactResolutionException e) {
             throw new MojoExecutionException(e.getMessage(), e);
         } catch (ArtifactNotFoundException e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 
     /**
      * Resolves and installs a set of configured profiles by extracting their contents to a runtime image repository
      *
      * @param rootDirectory the top-level runtime image directory
      * @throws MojoExecutionException if there is an error during installation
      */
     private void installProfiles(File rootDirectory) throws MojoExecutionException {
         for (Dependency profile : profiles) {
             String groupId = profile.getGroupId();
             String artifactId = profile.getArtifactId();
             String version = profile.getVersion();
             getLog().info("Installing profile: " + groupId + ":" + artifactId);
             Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, "zip", "bin");
             try {
                 resolver.resolve(artifact, remoteRepositories, localRepository);
             } catch (ArtifactResolutionException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             } catch (ArtifactNotFoundException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             }
             File source = artifact.getFile();
             extract(source, rootDirectory);
         }
     }
 
     /**
     * Resolves and installs a set of configured extensions by copying them to a runtime image repository
      *
      * @param rootDirectory the top-level runtime image directory
      * @throws MojoExecutionException if there is an error during installation
      */
     private void installExtensions(File rootDirectory) throws MojoExecutionException {
         for (Dependency extension : extensions) {
             String groupId = extension.getGroupId();
             String artifactId = extension.getArtifactId();
             String version = extension.getVersion();
             String type = extension.getType();
             String classifier = extension.getClassifier();
             getLog().info("Installing extension: " + groupId + ":" + artifactId);
             Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
             try {
                 resolver.resolve(artifact, remoteRepositories, localRepository);
             } catch (ArtifactResolutionException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             } catch (ArtifactNotFoundException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             }
             File source = artifact.getFile();
             InputStream sourceStream = null;
             OutputStream targetStream = null;
             try {
                 File repository = new File(rootDirectory, "extensions");
                 sourceStream = new BufferedInputStream(new FileInputStream(source));
                 File targetFile = new File(repository, source.getName());
                 targetStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                 copy(sourceStream, targetStream);
             } catch (IOException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             } finally {
                 close(targetStream);
                 close(sourceStream);
             }
         }
     }
 
 
     /**
      * Installs configuration files to the server image.
      *
      * @param rootDirectory the top-level runtime image directory
      * @throws MojoExecutionException if there is an error during installation
      */
     private void installConfiguration(File rootDirectory) throws MojoExecutionException {
         for (ConfigFile file : configurationFiles) {
             InputStream sourceStream = null;
             OutputStream targetStream = null;
             try {
                 // main directory is parent of the source directory
                 File source = new File(sourceDirectory.getParent(), file.getSource());
                 File targetDirectory = new File(rootDirectory, file.getDestination());
                 targetDirectory.mkdirs();
                 sourceStream = new BufferedInputStream(new FileInputStream(source));
                 File targetFile = new File(targetDirectory, source.getName());
                 targetStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                 copy(sourceStream, targetStream);
             } catch (IOException e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             } finally {
                 close(targetStream);
                 close(sourceStream);
             }
         }
     }
 
     /**
      * Removes extensions from the server image.
      *
      * @param rootDirectory the top-level runtime image directory
      * @throws MojoExecutionException if there is an error
      */
     private void removeExtensions(File rootDirectory) throws MojoExecutionException {
         for (Dependency extension : removeExtensions) {
             String id = extension.getArtifactId();
             String version = extension.getVersion();
             if (version == null) {
                 throw new MojoExecutionException("Version not specified for: " + id);
             }
             String fileName = id + "-" + version + ".jar";
             File extensionsDir = new File(rootDirectory, "extensions");
             File file = new File(extensionsDir, fileName);
             boolean result = file.delete();
             if (!result) {
                 throw new MojoExecutionException("Unable to exclude: " + file);
             }
         }
     }
 
     /**
      * Extracts the contents of a zip file to a target directory.
      *
      * @param source      the zip file
      * @param destination the target directory
      * @throws MojoExecutionException if there is an error during extraction
      */
     private void extract(File source, File destination) throws MojoExecutionException {
         ZipFile zipfile;
         try {
             zipfile = new ZipFile(source);
         } catch (IOException e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
         Enumeration enumeration = zipfile.entries();
         while (enumeration.hasMoreElements()) {
             ZipEntry entry = (ZipEntry) enumeration.nextElement();
             if (entry.isDirectory()) {
                 new File(destination, entry.getName()).mkdirs();
             } else {
                 InputStream sourceStream = null;
                 OutputStream targetStream = null;
                 try {
                     sourceStream = new BufferedInputStream(zipfile.getInputStream(entry));
                     FileOutputStream fos = new FileOutputStream(new File(destination, entry.getName()));
                     targetStream = new BufferedOutputStream(fos, BUFFER);
                     copy(sourceStream, targetStream);
                     targetStream.flush();
                 } catch (FileNotFoundException e) {
                     throw new MojoExecutionException(e.getMessage(), e);
                 } catch (IOException e) {
                     throw new MojoExecutionException(e.getMessage(), e);
                 } finally {
                     close(targetStream);
                     close(sourceStream);
                 }
             }
         }
     }
 
     private int copy(InputStream input, OutputStream output) throws IOException {
         byte[] buffer = new byte[BUFFER];
         int count = 0;
         int n;
         while (-1 != (n = input.read(buffer))) {
             output.write(buffer, 0, n);
             count += n;
         }
         return count;
     }
 
     private void close(Closeable closeable) {
         try {
             if (closeable != null) {
                 closeable.close();
             }
         } catch (IOException e) {
             getLog().error(e);
         }
     }
 }
