 package com.atlassian.maven.plugins.conf;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import com.atlassian.maven.plugins.refapp.MavenContext;
import com.atlassian.maven.plugins.refapp.RunMojo;
import com.atlassian.maven.plugins.refapp.WebappHandler;
 
 /**
  * @extendsPlugin refapp
  * @goal run
  * @execute phase="package"
  */
 public class ConfluenceRunMojo extends RunMojo
 {
     /**
      * The Confluence test resources version
      *
      * @parameter expression="${testResourcesVersion}" default-value="RELEASE"
      */
     protected String testResourcesVersion;
 
     @Override
    protected void prepareWebapp(final File webappWar) throws MojoExecutionException
     {
        final ConfluenceMavenGoals goals = new ConfluenceMavenGoals(new MavenContext(project, session, pluginManager, getLog()));
         final File outputDir = new File(project.getBuild().getDirectory());
        final File confHomeZip = goals.copyConfluenceHome(outputDir, testResourcesVersion);
        final File tmpDir = new File(project.getBuild().getDirectory(), "tmp-resources");
         tmpDir.mkdir();
 
        final File confHome = new File(outputDir, "confluence-home");
         try
         {
             unzip(confHomeZip, tmpDir.getPath());
             FileUtils.copyDirectory(new File(tmpDir.listFiles()[0], "confluence-home"),
                    confHome);
 
            final File cfgFile = new File(confHome, "confluence.cfg.xml");
             String config = FileUtils.readFileToString(cfgFile);
            config = config.replace("@project-dir@", project.getBuild().getDirectory());
             FileUtils.writeStringToFile(cfgFile, config);
         }
        catch (final IOException ex)
         {
             throw new MojoExecutionException("Unable to prepare confluence webapp", ex);
         }
     }
 
     @Override
     protected WebappHandler getWebappHandler()
     {
         return new ConfluenceWebappHandler();
     }
 }
