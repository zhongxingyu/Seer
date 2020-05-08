 /**
  * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.common.maven.testing;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.deployer.ArtifactDeployer;
 import org.apache.maven.artifact.installer.ArtifactInstaller;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.DefaultModelWriter;
 import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.PlexusContainer;
 
 @Named
 public class ArtifactRepositoryFacade
 {
    @Inject
   private PlexusContainer plexus;
 
    @Inject
    private LegacySupport legacySupport;
 
    private EmbeddedMaven embeddedMaven;
 
    @Inject
    public ArtifactRepositoryFacade(EmbeddedMaven embeddedMaven)
    {
       this.embeddedMaven = embeddedMaven;
    }
 
    public void setEmbeddedMaven(EmbeddedMaven embeddedMaven)
    {
       this.embeddedMaven = embeddedMaven;
    }
 
    public EmbeddedMaven getEmbeddedMaven()
    {
       return embeddedMaven;
    }
 
 
    public void install(Model pom)
    {
       try
       {
          deploy(pom, true, false);
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw new IllegalStateException(e);
       }
    }
 
    public void deploy(Model pom)
    {
       try
       {
          deploy(pom, false, true);
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw new IllegalStateException(e);
       }
    }
 
    public void installAndDeploy(Model pom)
    {
       try
       {
          deploy(pom, true, true);
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw new IllegalStateException(e);
       }
    }
 
    private void deploy(Model pom, boolean install, boolean deploy) throws Exception, RuntimeException
    {
       final File localRepo = new File(embeddedMaven.getLocalRepository().getBasedir());
       final File source = createStubJar(localRepo);
       try
       {
          deploy(source, pom, install, deploy);
       }
       finally
       {
          FileUtils.forceDelete(source);
       }
    }
 
    private void deploy(final File source, Model pom, boolean install, boolean deploy)
    {
       final ArtifactRepository localRepository = embeddedMaven.getLocalRepository();
 
       final File localRepo = new File(localRepository.getBasedir());
 
       final MavenExecutionResult2 result = embeddedMaven.buildStubProject(localRepo);
 
       final MavenSession session = legacySupport.getSession();
       try
       {
         final ArtifactInstaller installer = install ? plexus.lookup(ArtifactInstaller.class) : null;
         final ArtifactDeployer deployer = deploy ? plexus.lookup(ArtifactDeployer.class) : null;

          legacySupport.setSession(result.getSession());
 
          final ArtifactRepository deploymentRepository = embeddedMaven.getRemoteRepository();
 
         final Artifact artifact = embeddedMaven.createArtifact(pom);
         if (install)
         {
            installer.install(source, artifact, localRepository);
         }
         if (deploy)
         {
            deployer.deploy(source, artifact, deploymentRepository, localRepository);
         }

          if (!"pom".equals(pom.getPackaging()))
          {
             final Artifact pomArtifact = embeddedMaven.createProjectArtifact(pom);
             final File pomFile = File.createTempFile(pom.getArtifactId() + "-" + pom.getVersion(), ".pom", localRepo);
             try
             {
                new DefaultModelWriter().write(pomFile, null, pom);
                if (install)
                {
                   installer.install(pomFile, pomArtifact, localRepository);
                }
                if (deploy)
                {
                   deployer.deploy(pomFile, pomArtifact, deploymentRepository, localRepository);
                }
             }
             finally
             {
                FileUtils.forceDelete(pomFile);
             }
          }
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw new IllegalStateException(e);
       }
       finally
       {
          legacySupport.setSession(session);
       }
    }
 
    private static File createStubJar(File dir) throws IOException
    {
       final File jarFile = File.createTempFile("stub", ".jar", dir);
 
       JarOutputStream jarOut = null;
       try
       {
          jarOut = new JarOutputStream(new FileOutputStream(jarFile));
 
          final JarEntry mfEntry = new JarEntry(JarFile.MANIFEST_NAME);
          jarOut.putNextEntry(mfEntry);
 
          final Manifest mf = new Manifest();
          mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1");
          mf.write(jarOut);
 
          jarOut.closeEntry();
       }
       finally
       {
          IOUtils.closeQuietly(jarOut);
       }
 
       return jarFile;
    }
 }
