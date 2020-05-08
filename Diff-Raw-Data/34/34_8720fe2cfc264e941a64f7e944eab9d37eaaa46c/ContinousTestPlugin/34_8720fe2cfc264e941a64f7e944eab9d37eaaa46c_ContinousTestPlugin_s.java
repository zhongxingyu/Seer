 package com.marimon.maven.continous.test.plugin;
 
import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
 
 /**
 * Maven plugin to continously run tests
  */
public class ContinousTestPlugin implements Mojo {
 
    private Log _log;

    public void execute()
            throws MojoExecutionException, MojoFailureException {
        _log.info("ContinousTestPlugin started.");
    }

    public Log getLog() {
        return _log;
     }
 
    public void setLog(final Log log) {
        _log = log;

    }
 }
