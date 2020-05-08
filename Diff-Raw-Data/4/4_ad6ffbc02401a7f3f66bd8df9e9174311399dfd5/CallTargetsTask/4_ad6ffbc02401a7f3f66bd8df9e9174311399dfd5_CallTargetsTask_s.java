 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.ant;
 
 import java.io.File;
 
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.PropertySet;
 
 /**
  * Apache Ant task that calls one or more targets in a separate build file.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class CallTargetsTask extends Task {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallTargetsTask</code> instance.
     */
    public CallTargetsTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of the build file to execute.
     */
    private String _antFile;
 
    /**
     * The list of targets to execute.
     */
    private Vector _targets;
 
    /**
     * The directory for the build file to operate in.
     */
    private File _dir;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Sets the name of the build file to execute. If <code>null</code> or
     * <code>""</code> is passed as the argument, then <code>build.xml</code>
     * is assumed.
     *
     * @param antFile
     *    the name of the Apache Ant build file to execute, can be
     *    <code>null</code>.
     */
    public void setAntFile(String antFile) {
       _antFile = (antFile == null || antFile.length() < 1)
                ? "build.xml"
                : antFile;
    }
 
    /**
     * Sets the targets to execute within the build file.
     *
     * @param targets
     *    the targets to execute, space-separated.
     */
    public void setTargets(String targets) {
       _targets = (targets == null)
                ? new Vector()
                : new Vector(Arrays.asList(targets.split(" ")));
    }
 
    /**
     * Sets the working directory during the execution of the Ant buildfile.
     *
    * @param targets
    *    the targets to execute, space-separated.
     */
    public void setDir(String dir) {
       _dir = (dir != null && dir.length() > 0)
            ? new File(dir)
            : getProject().getBaseDir();
    }
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
 
       Project thisProject = getProject();
 
       // Create a temporary Project object
       Project tempProject = new Project();
 
       // Set the default input stream
       tempProject.setDefaultInputStream(thisProject.getDefaultInputStream());
 
       // Set the Java version
       tempProject.setJavaVersionProperty();
 
       // Set the input handler
       tempProject.setInputHandler(thisProject.getInputHandler());
 
       // Copy all listeners
       Iterator bl = thisProject.getBuildListeners().iterator();
       while (bl.hasNext()) {
          tempProject.addBuildListener((BuildListener) bl.next());
       }
 
       // Make the temporary project a sub-project
       thisProject.initSubProject(tempProject);
 
       // Set user-defined properties
       thisProject.copyUserProperties(tempProject);
 
       // Copy JVM system properties
       tempProject.setSystemProperties();
 
       // Inherit all properties from this project
       Hashtable properties = thisProject.getProperties();
       Enumeration e = properties.keys();
       while (e.hasMoreElements()) {
 
          // Get the property name
          String key = e.nextElement().toString();
 
          // Set the property if we should
          if (!"basedir".equals(key) && !"ant.file".equals(key) && tempProject.getProperty(key) == null) {
             String value = properties.get(key).toString();
             tempProject.setNewProperty(key, value);
          }
       }
 
       // Set the base directory
       tempProject.setBaseDir(_dir);
       tempProject.setInheritedProperty("basedir", _dir.getAbsolutePath());
 
       log("Calling target \"" + _targets + "\" in build file \"" + _antFile + '"', Project.MSG_VERBOSE);
 
       // Set the ant.file property
       tempProject.setUserProperty("ant.file", _antFile);
 
       // Configure the project
       try {
          ProjectHelper.configureProject(tempProject, new File(_antFile));
       } catch (BuildException ex) {
          throw ProjectHelper.addLocationToBuildException(ex, getLocation());
       }
 
       // Really execute the build file
       Throwable t = null;
       try {
          log("Entering build file \"" + _antFile + "\".", Project.MSG_VERBOSE);
          tempProject.executeTargets(_targets);
       } catch (BuildException ex) {
          t = ProjectHelper.addLocationToBuildException(ex, getLocation());
          throw (BuildException) t;
       } finally {
          log("Exiting build file \"" + _antFile + "\".", Project.MSG_VERBOSE);
       }
    }
 }
