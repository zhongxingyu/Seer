 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.ant;
 
 import java.io.File;
 import java.util.StringTokenizer;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.XSLTProcess;
 import org.apache.tools.ant.types.Mapper;
 import org.apache.tools.ant.util.FileNameMapper;
 import org.apache.tools.ant.util.GlobPatternMapper;
 
 /**
  * Apache Ant task similar to the &lt;xslt&gt; task with the option that allow to never
  * overwrite the destination.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 2.1
  */
 public class XsltPlusTask extends XSLTProcess {
 
    /**
     * Name of the property to store that the target should never be overwritten.
     */
    private boolean neverOverwrite = false;
 
    /**
     * Store the names of the files as it may change.
     */
    private String implicitIncludes;
 
    /**
     * Store the mapper if any.
     */
    private Mapper mapper;
 
    /**
     * Store the destination directory.
     */
    private File destDir;
 
    /**
     * Sets to never overwrite the destination.
     *
     * @param neverOverwrite
     *    <code>true</code> if the destination should never be overwritten,
     *    <code>false</code> otherwise.
     */
    public void setNeverOverwrite(boolean neverOverwrite) {
       this.neverOverwrite = neverOverwrite;
    }
 
    /**
     * Sets the files to include in the transformation.
     *
     * @param includes
     *    The files to tranform.
     */
    public void setIncludes(String includes) {
       this.implicitIncludes = includes;
    }
 
    /**
     * Sets the mapper.
     *
     * @param mapper
     *    The mapper.
     */
    public void addMapper(Mapper mapper) {
       super.addMapper(mapper);
       this.mapper = mapper;
    }
 
    /**
     * Sets the destination directory.
     *
     * @param destDir
     *    The destination directory.
     */
    public void setDestdir(File destDir) {
       super.setDestdir(destDir);
      this.destDir = destDir;
    }
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
       
       if (neverOverwrite) {
          //if (mapper == null) mapper = new GlobPatternMapper(
          if (mapper == null) throw new BuildException("Please specify a mapper");
          FileNameMapper mapperImpl = mapper.getImplementation();
          String newIncludes = "";
          StringTokenizer stIncludes = new StringTokenizer(implicitIncludes, " ,");
          while (stIncludes.hasMoreTokens()) {
             String nextInclude = stIncludes.nextToken();
             String includeDest = mapperImpl.mapFileName(nextInclude)[0];
             File destination = new File(destDir, includeDest);
             if (!destination.exists()) {
                if (newIncludes.equals("")) {
                   newIncludes = nextInclude;
                } else {
                   newIncludes += "," + nextInclude;
                }
             }
          }
          super.setIncludes(newIncludes);
         if (newIncludes.equals("")) {
            return;
         }
       } else {
          super.setIncludes(implicitIncludes);
       }
       super.execute();
    }
 }
