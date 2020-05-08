 package org.charless.qxmaven.mojo.qooxdoo;
 
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Goal which builds the qooxdoo testrunner
  * 
  * @goal test-compile
  * @phase test-compile
  * @author charless
  * @requiresDependencyResolution test
  */
 public class TestCompileMojo extends AbstractGeneratorMojo {
 	
     /**
      * Name of the job used to build the application.
      *
     * @parameter expression="${qooxdoo.build.job}"
     * 			  default-value="build"
      */
     protected String buildJob;
 	
     public void execute()
         throws MojoExecutionException
     {
     	
     	this.generator(buildJob);
     }
 
 }
