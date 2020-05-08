 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.etsaijavautils.ant.taskdef;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 /**
  * Generates the version based on the tag of the GIT repository
  * @author etsai
  */
 public class Describe extends Task {
     private static String gitCommand= "git describe --dirty --long";
     private String dirtyProperty;
     private String versionProperty;
     
     @Override
     public void execute() {
         try {
            File baseDir= getProject().getBaseDir();
            Process proc= Runtime.getRuntime().exec(gitCommand, null, baseDir);
             String describe= new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
             String[] versionParts= describe.split("-");
             
             if (dirtyProperty != null) {
                 getProject().setProperty(dirtyProperty, String.valueOf(describe.contains("dirty")));
             }
             if (versionProperty != null) {
                 String version= versionParts[0] + ".";
                 if (versionParts.length == 1) {
                     version+= "0";
                 } else {
                     version+= versionParts[1];
                 }
                 getProject().setProperty(versionProperty, version);
             }
             
         } catch (IOException ex) {
             ex.printStackTrace(System.err);
             throw new BuildException("Error execuing command: " + gitCommand);
         }
     }
     
     public void setDirtyProperty(String dirtyProperty) {
         this.dirtyProperty= dirtyProperty;
     }
     
     public void setVersionProperty(String versionProperty) {
         this.versionProperty= versionProperty;
     }
 }
