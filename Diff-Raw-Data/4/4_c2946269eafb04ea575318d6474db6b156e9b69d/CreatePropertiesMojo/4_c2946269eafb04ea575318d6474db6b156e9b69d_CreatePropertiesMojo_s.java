 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin;
 
 import java.io.File;
 import java.io.FileFilter;
 

 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Goal which creates properties related to a cids project.
  *
  * @version                       $Revision$, $Date$
  * @goal                          create-properties
  * @phase                         process-classes
  * @requiresDependencyResolution  runtime
  */
 public class CreatePropertiesMojo extends AbstractCidsMojo {
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * The <code>de.cismet.cids.lib.local</code> property.
      *
      * @parameter  expression="${de.cismet.cids.lib.local}"
      * @required   false
      * @readonly   true
      */
     private transient File libLocalDir;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Generates several cids related properties that can be used by plugins executed in an subsequent phase.
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     @Override
     public void execute() throws MojoExecutionException {
         createClasspathProperty();
     }
 
     /**
      * Generates/replaces the <code>de.cismet.cids.classpath</code> property using:<br/>
      * <br/>
      *
      * <ul>
      *   <li>the project's output directory</li>
      *   <li>the project's runtime artifacts</li>
      *   <li>the project's system artifacts</li>
      *   <li>all jars within the folder specified by <code>de.cismet.cids.lib.local</code> property</li>
      * </ul>
      */
     private void createClasspathProperty() {
         final StringBuilder sb = new StringBuilder();
 
         // first add the project's output directory
         sb.append(project.getBuild().getOutputDirectory()).append(File.pathSeparatorChar);
 
         // collect runtime artifacts and append them to the classpath string
         for (final Object o : project.getRuntimeArtifacts()) {
             final Artifact artifact = (Artifact)o;
             sb.append(artifact.getFile().getAbsolutePath()).append(File.pathSeparatorChar);
         }
 
         // also collect system artifacts and append them to the classpath string [issue:1456]
         // we will have to iterate over all dependency artifacts because project.getSystemArtifacts() is a trap...
         boolean first = true;
         for (final Object o : project.getDependencyArtifacts()) {
             final Artifact artifact = (Artifact)o;
             if (Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
                 if (first && getLog().isWarnEnabled()) {
                     getLog().warn("adding system dependent libraries to classpath"); // NOI18N
                     first = false;
                 }
                 if (getLog().isDebugEnabled()) {
                     getLog().debug("system-dependent library: " + artifact);         // NOI18N
                 }
                 sb.append(artifact.getFile().getAbsolutePath()).append(File.pathSeparatorChar);
             }
         }
 
         // collect local jars and append them to the classpath string
         if (libLocalDir.exists()) {
             final File[] jars = libLocalDir.listFiles(
                     new FileFilter() {
 
                         @Override
                         public boolean accept(final File pathname) {
                             return pathname.getName().toLowerCase().endsWith(".jar"); // NOI18N
                         }
                     });
             if (jars == null) {
                 if (getLog().isWarnEnabled()) {
                     getLog().warn("an I/O error occured while fetching jars from lib local folder: " + libLocalDir); // NOI18N
                 }
             } else {
                 for (final File jar : jars) {
                     if (getLog().isDebugEnabled()) {
                         getLog().debug("add jar: " + jar);                            // NOI18N
                     }
                     sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
                 }
             }
         } else {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("lib local dir property does not denote an existing filename: " + libLocalDir); // NOI18N
             }
         }
 
         // remove the last colon
         sb.deleteCharAt(sb.length() - 1);
 
         // wrap into "" [issue:1457]
        sb.insert(0, "\"").insert(sb.length(), "\"");
 
         // double up all '\' [issue:1455]
         final String classpath = sb.toString().replace("\\", "\\\\"); // NOI18N
 
         if (getLog().isInfoEnabled()) {
             getLog().info("created classpath: " + classpath); // NOI18N
         }
 
         project.getProperties().put(PROP_CIDS_CLASSPATH, classpath);
     }
 }
