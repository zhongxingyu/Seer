 package org.netbeans.installer.javafx;
 
 import java.io.File;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 /**
  *
  * @author Adam
  */
 public class FirstFNameTask extends Task {
     String property;
     File dir;
 
     public void setProperty(String property) {
         this.property = property;
     }
 
     public void setDir(File dir) {
         this.dir = dir;
     }
 
     @Override
     public void execute() throws BuildException {
         String[] f = dir.list();
        if (f != null && f.length > 0) getProject().setNewProperty(property, f[0]);
     }
 
 }
