 package org.carlspring.maven.relocation;
 
 /**
  * Copyright 2012 Martin Todorov.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.model.DistributionManagement;
 import org.apache.maven.model.FileSet;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Relocation;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.carlspring.maven.io.ArtifactFilter;
 import org.carlspring.maven.io.PomFilenameFilter;
 import org.carlspring.maven.model.ModelParser;
 import org.carlspring.maven.model.ModelWriter;
 import org.carlspring.maven.util.ChecksumUtils;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 
 import java.io.*;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author mtodorov
  */
 public abstract class AbstractRelocationMojo
         extends AbstractMojo
 {
 
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
 
     /**
      * @parameter expression="${basedir}"
      */
     private String basedir;
 
     /**
      * @parameter expression="${repositoryBasedir}" default-value="${basedir}"
      */
     private String repositoryBasedir;
 
     /**
      * @parameter expression="${interactive}" default-value="false"
      */
     private boolean interactiveMode;
 
     /**
      * @parameter expression="${originalGAV}"
      */
     private String originalGAV;
 
     /**
      * @parameter expression="${groupId}"
      */
     private String groupId;
 
     /**
      * @parameter expression="${artifactId}"
      */
     private String artifactId;
 
     /**
      * @parameter expression="${version}"
      */
     private String version;
 
     /**
      * @parameter expression="${relocationGAV}"
      */
     private String relocationGAV;
 
     /**
      * @parameter expression="${relocationGroupId}"
      */
     private String relocationGroupId;
 
     /**
      * @parameter expression="${relocationArtifactId}"
      */
     private String relocationArtifactId;
 
     /**
      * @parameter expression="${relocationVersion}"
      */
     private String relocationVersion;
 
     /**
      * @parameter expression="${relocationMessage}"
      */
     private String relocationMessage;
 
     /**
      * @parameter expression="${repositoryBaseDir}"
      */
     private String repositoryBaseDir;
 
 
     public void relocateArtifacts(File artifactsSourceDir)
             throws IOException, MojoFailureException
     {
         File relocatedDir = new File(getRelocatedArtifactBasedir());
         FileUtils.copyDirectory(artifactsSourceDir, relocatedDir, "*.*", "**/*pom*");
     }
 
     public File getOriginalArtifactBasedir()
     {
         String path = getRepositoryBaseDir() + File.separator;
        path += getGroupId().replaceAll("\\.", File.separator) + File.separator;
         path += getArtifactId() + File.separator;
         path += getVersion() != null ? getVersion() : "";
 
         return new File(path).getAbsoluteFile();
     }
 
     protected String getPomFile(File artifactBasedir)
     {
         final String[] list = artifactBasedir.list(new PomFilenameFilter());
 
         // There should *really* not be more than one .pom per artifact, thus:
         return list[0];
     }
 
     protected Model getOriginalModel(File pomFile)
             throws IOException, XmlPullParserException
     {
         ModelParser projectParser = new ModelParser(pomFile.getAbsolutePath());
 
         return projectParser.getModel();
     }
 
     /**
      * This method generates the Model describing the relocation.
      *
      * @param   originalModel
      * @param   pomFile
      * @return
      * @throws  MojoFailureException
      * @throws  IOException
      * @throws  XmlPullParserException
      * @throws  MojoExecutionException
      */
     protected Model generateRelocationModel(Model originalModel, File pomFile)
             throws MojoFailureException, IOException, XmlPullParserException, MojoExecutionException
     {
         Model model = new Model();
         model.setGroupId(originalModel.getGroupId());
         model.setArtifactId(originalModel.getArtifactId());
         model.setVersion(originalModel.getVersion());
 
         Relocation relocation = new Relocation();
         relocation.setGroupId(getRelocationGroupId());
         relocation.setArtifactId(getRelocationArtifactId());
         relocation.setVersion(getRelocationVersion());
         relocation.setMessage(getRelocationMessage());
 
         DistributionManagement distributionManagement = new DistributionManagement();
         distributionManagement.setRelocation(relocation);
 
         model.setDistributionManagement(distributionManagement);
 
         ModelWriter modelWriter = new ModelWriter(model, pomFile);
         modelWriter.write();
         generateSignatures(pomFile);
 
         return model;
     }
 
     protected void generateModelForRelocatedArtifacts(Model originalModel, File relocatedPomFile)
             throws MojoFailureException,
                    IOException,
                    XmlPullParserException,
                    MojoExecutionException
     {
         originalModel.setGroupId(getRelocationGroupId());
         originalModel.setArtifactId(getRelocationArtifactId());
         originalModel.setVersion(getRelocationVersion());
 
         ModelWriter modelWriter = new ModelWriter(originalModel, relocatedPomFile);
         modelWriter.write();
 
         generateSignatures(relocatedPomFile);
     }
 
     protected void backupFiles(File artifactBasedir)
             throws MojoFailureException
     {
         try
         {
             File backupDir = new File(artifactBasedir, "backup");
             //noinspection ResultOfMethodCallIgnored
             backupDir.mkdir();
 
             getLog().info("Backing up artifact files from " + artifactBasedir.getAbsolutePath() +
                           " to " + backupDir + "...");
 
             FileUtils.copyDirectory(artifactBasedir, backupDir, "*.*", "backup/**");
         }
         catch (Exception e)
         {
             throw new MojoFailureException(e.getMessage(), e);
         }
     }
 
     protected void removeOriginalArtifacts()
             throws MojoFailureException
     {
         try
         {
             getLog().info("Removing artifact files from " + getOriginalArtifactBasedir().getAbsolutePath() + "...");
 
             FilenameFilter filter = new ArtifactFilter();
 
             File[] artifactFiles = getOriginalArtifactBasedir().listFiles(filter);
             for (File artifactFile : artifactFiles){
                 if (artifactFile.isFile())
                 {
                     getLog().debug(" -> " +artifactFile.getAbsoluteFile().getAbsolutePath());
 
                     //noinspection ResultOfMethodCallIgnored
                     artifactFile.delete();
                 }
             }
         }
         catch (Exception e)
         {
             throw new MojoFailureException(e.getMessage(), e);
         }
     }
 
     protected List<String> getArtifactFiles(String repositoryBaseDirectory,
                                             String artifactBaseDirPattern)
     {
         DirectoryScanner scanner = new DirectoryScanner();
 
         FileSet fileSet = new FileSet();
         fileSet.setDirectory(new File(repositoryBaseDirectory).getAbsolutePath());
 
         // Specify where to look, so that it doesn't scan through the entire repository
         fileSet.addInclude(artifactBaseDirPattern);
 
         // Exclude default artifact types:
         fileSet.addExclude("**/*.md5");
         fileSet.addExclude("**/*.sha1");
         fileSet.addExclude("**/*.tmp*");
 
         File resourceDirectory = new File(fileSet.getDirectory());
         String[] finalIncludes = new String[fileSet.getIncludes().size()];
         String[] finalExcludes = new String[fileSet.getExcludes().size()];
 
         finalIncludes = fileSet.getIncludes().toArray(finalIncludes);
         finalExcludes = fileSet.getExcludes().toArray(finalExcludes);
 
         scanner.setBasedir(resourceDirectory);
         scanner.setIncludes(finalIncludes);
         scanner.setExcludes(finalExcludes);
         scanner.addDefaultExcludes();
         scanner.scan();
 
         if (scanner.getIncludedFiles() != null && scanner.getIncludedFiles().length > 0)
         {
             return Arrays.asList(scanner.getIncludedFiles());
         }
         else
         {
             return Collections.emptyList();
         }
     }
 
     protected void generateSignatures(File file)
             throws MojoExecutionException
     {
         try
         {
             getLog().info("Generating MD5 checksum for " + file.getAbsolutePath());
             ChecksumUtils.generateMD5ChecksumFile(file);
 
             getLog().info("Generating SHA1 checksum for " + file.getAbsolutePath());
             ChecksumUtils.generateSHA1ChecksumFile(file);
         }
         catch (IOException e)
         {
             throw new MojoExecutionException(e.getMessage(), e);
         }
         catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace();
         }
     }
 
     public String interact(String message)
             throws MojoFailureException
     {
         System.out.println(message);
 
         String response = null;
         try
         {
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
             response = br.readLine();
 
             br.close();
         }
         catch (IOException e)
         {
             throw new MojoFailureException(e.getMessage(), e);
         }
 
         return response;
     }
 
     public String getRelocationGroupId()
             throws MojoFailureException
     {
         if (isInteractiveMode())
         {
             relocationGroupId = interact("What groupId should the artifact have after relocation?"
                                          /* [" + groupId + "]"*/);
         }
         else
         {
             if (relocationGroupId == null)
             {
                 relocationGroupId = relocationGAV.split(":")[0];
             }
         }
 
         return relocationGroupId;
     }
 
     public void setRelocationGroupId(String relocationGroupId)
     {
         this.relocationGroupId = relocationGroupId;
     }
 
     public String getRelocationArtifactId()
             throws MojoFailureException
     {
         if (isInteractiveMode())
         {
             relocationArtifactId = interact("What artifactId should the artifact have after relocation?"
                                             /* [" + artifactId + "]"*/);
         }
         else
         {
             if (relocationArtifactId== null)
             {
                 relocationArtifactId = relocationGAV.split(":")[1];
             }
         }
 
         return relocationArtifactId;
     }
 
     public void setRelocationArtifactId(String relocationArtifactId)
     {
         this.relocationArtifactId = relocationArtifactId;
     }
 
     public String getRelocationVersion()
             throws MojoFailureException
     {
         if (isInteractiveMode())
         {
             relocationVersion = interact("What version should the artifact have after relocation?"
                                          /* [" + version + "]"*/);
         }
         else
         {
             final String[] split = relocationGAV.split(":");
             if (relocationVersion == null && split.length == 3)
             {
                 relocationVersion = split[2];
             }
         }
 
         return relocationVersion;
     }
 
     public void setRelocationVersion(String relocationVersion)
     {
         this.relocationVersion = relocationVersion;
     }
 
     public String getRelocationMessage()
     {
         return relocationMessage;
     }
 
     public void setRelocationMessage(String relocationMessage)
     {
         this.relocationMessage = relocationMessage;
     }
 
     public boolean isInteractiveMode()
     {
         return interactiveMode;
     }
 
     public void setInteractiveMode(boolean interactiveMode)
     {
         this.interactiveMode = interactiveMode;
     }
 
     public MavenProject getProject()
     {
         return project;
     }
 
     public void setProject(MavenProject project)
     {
         this.project = project;
     }
 
     public String getBasedir()
     {
         return basedir;
     }
 
     public void setBasedir(String basedir)
     {
         this.basedir = basedir;
     }
 
     public String getOriginalGAV()
     {
         return originalGAV;
     }
 
     public void setOriginalGAV(String originalGAV)
     {
         this.originalGAV = originalGAV;
     }
 
     public String getRelocationGAV()
     {
         return relocationGAV;
     }
 
     public void setRelocationGAV(String relocationGAV)
     {
         this.relocationGAV = relocationGAV;
     }
 
     public String getRepositoryBaseDir()
     {
         return repositoryBaseDir;
     }
 
     public void setRepositoryBaseDir(String repositoryBaseDir)
     {
         this.repositoryBaseDir = repositoryBaseDir;
     }
 
     public String getRelocatedArtifactBasedir()
             throws MojoFailureException
     {
         return getRepositoryBaseDir() + File.separatorChar +
                getRelocationGroupId().replaceAll("\\.", File.separator) +
                File.separatorChar + getRelocationArtifactId() + File.separatorChar + getRelocationVersion();
     }
 
     public String getGroupId()
     {
         if (groupId == null)
             groupId = originalGAV.split(":")[0];
 
         return groupId;
     }
 
     public void setGroupId(String groupId)
     {
         this.groupId = groupId;
     }
 
     public String getArtifactId()
     {
         if (artifactId == null)
             artifactId = originalGAV.split(":")[1];
 
         return artifactId;
     }
 
     public void setArtifactId(String artifactId)
     {
         this.artifactId = artifactId;
     }
 
     public String getVersion()
     {
         if (version == null)
         {
             final String[] split = originalGAV.split(":");
             version = split.length >= 3 ? split[2] : null;
         }
 
         return version;
     }
 
     public void setVersion(String version)
     {
         this.version = version;
     }
 
 }
