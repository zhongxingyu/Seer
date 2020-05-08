 // Copyright 2007-2009, PensioenPage B.V.
 package org.xins.logdoc.ant;
 
 import org.apache.tools.ant.BuildException;
 import static org.apache.tools.ant.Project.MSG_VERBOSE;
 
 import org.xins.logdoc.def.LogDef;
 
 /**
  * An Apache Ant task for generating Java source files from Logdoc
  * definitions.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class LogdocJavaTask extends AbstractLogdocTask {
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>LogdocJavaTask</code> object.
     */
    public LogdocJavaTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The access level.
     */
    private String _accessLevel;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
    * Sets the access level, <code>"package"</code> or <code>"public"</code>.
    * The default is <code>"package"</code>.
     */
    public void setAccessLevel(String s) {
       log("Setting \"accessLevel\" to: " + quote(s) + '.', MSG_VERBOSE);
       _accessLevel = s;
    }
 
    @Override
    protected void executeImpl(LogDef def) throws BuildException {
       // TODO FIXME
    }
 }
