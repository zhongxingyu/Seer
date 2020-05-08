 package com.marimon.maven.continous.test.plugin;
 
import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
 * Maven plugin to continously run tests. Developed following the guide at:
 * http://maven.apache.org/guides/plugin/guide-java-plugin-development.html
 * 
 * @goal retest
  */
public class ContinousTestPlugin extends AbstractMojo {
 
    public void execute() throws
    // Thrown to cause a BUILD ERROR
            MojoExecutionException,
            // Thrown to cause a BUILD FAILURE
            MojoFailureException {
        getLog().info("ContinousTestPlugin started.");
     }
 
 }
