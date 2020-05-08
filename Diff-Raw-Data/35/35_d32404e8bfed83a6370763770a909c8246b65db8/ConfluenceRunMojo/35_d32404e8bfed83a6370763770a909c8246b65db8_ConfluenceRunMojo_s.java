 package com.atlassian.maven.plugins.conf;
 
import com.atlassian.maven.plugins.refapp.RunMojo;
import com.atlassian.maven.plugins.refapp.MavenContext;
import com.atlassian.maven.plugins.refapp.WebappHandler;

 import java.io.File;
 import java.io.IOException;
 
import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.commons.io.FileUtils;
 
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
    protected void prepareWebapp(File webappWar) throws MojoExecutionException
     {
        ConfluenceMavenGoals goals = new ConfluenceMavenGoals(new MavenContext(project, session, pluginManager, getLog()));
         final File outputDir = new File(project.getBuild().getDirectory());
        File confHomeZip = goals.copyConfluenceHome(outputDir, testResourcesVersion);
        File tmpDir = new File(project.getBuild().getDirectory(), "tmp-resources");
         tmpDir.mkdir();
 
        File confHome = new File(outputDir, "confluence-home");
         try
         {
             unzip(confHomeZip, tmpDir.getPath());
             FileUtils.copyDirectory(new File(tmpDir.listFiles()[0], "confluence-home"),
                    confHome);
 
            File cfgFile = new File(confHome, "confluence.cfg.xml");
             String config = FileUtils.readFileToString(cfgFile);
            config = config.replaceAll("@project-dir@", project.getBuild().getDirectory());
             FileUtils.writeStringToFile(cfgFile, config);
         }
        catch (IOException ex)
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
