 /*
  * Copyright (c) 2012 Corey Minter
  * (see LICENSE file)
  */
 package net.minterval.versioning;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import org.apache.tools.ant.BuildException;
 
 /**
  *
  * @author cminter
  */
 public class GitTag extends ExtTask {
     private static final String gitPathEnv= "GIT_EXE";
     private static final String gitDirName= ".git";
     private static final String dirtySuffix= "-dirty";
     private static final String gitOpts= "describe --dirty --long --match \"%s*\"";
     private static final String gitPathAttr= "gitpath";
     private static final String tagPrefixAttr= "tagprefix";
     private static final String longVersionAttr= "longversionproperty";
     private static final String shortVersionAttr= "shortversionproperty";
     private static final String cleanAttr= "cleanproperty";
     
     public GitTag() {
         setAttr(gitPathAttr, "git");
         setAttr(tagPrefixAttr, "v-");
     }
     
     @Override
     public void execute() throws BuildException {
         ensureProps(gitPathAttr, tagPrefixAttr, longVersionAttr,
                 shortVersionAttr, cleanAttr);
         
         File baseDir= getProject().getBaseDir();
         File gitDir= new File(baseDir, gitDirName);
         String longVersion= "none";
         String shortVersion= "none";
         Boolean clean= Boolean.TRUE;
         if (gitDir.exists()) {
             // use user's environment if supplied
             String commandPath= System.getenv(gitPathEnv);
             // otherwise fallback to build property
             if (commandPath == null) { commandPath= getAttr(gitPathAttr); }
             String commandOpts= String.format(gitOpts, getAttr(tagPrefixAttr));
             String command= String.format("%s %s", commandPath, commandOpts);
             try {
                 Process proc= Runtime.getRuntime().exec(command, null, baseDir);
                 int status= proc.waitFor();
                 if (status != 0) {
                     BufferedReader br= new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                     String errorOut= br.readLine();
                     String msg= String.format("failed executing '%s', %s (%d)",
                             command, errorOut, status);
                     throw new BuildException(msg);
                 }
                 BufferedReader br= new BufferedReader(new InputStreamReader(proc.getInputStream()));
                 longVersion= br.readLine().substring(getAttr(tagPrefixAttr).length());
                 clean= Boolean.valueOf(! longVersion.endsWith(dirtySuffix));
                 String[] tagParts= longVersion.split("-");
                 StringBuilder tmpShortVersion= new StringBuilder(tagParts[0]);
                 if (tagParts.length < 2) {
                     tmpShortVersion.append(".0");
                 } else {
                     tmpShortVersion.append('.').append(tagParts[1]);
                 }
                 shortVersion= tmpShortVersion.toString();
             } catch (InterruptedException ie) {
                 String msg= String.format("interrupted waiting for '%s' to finish", command);
                 throw new BuildException(msg);
             } catch (IOException ioe) {
                 String msg= String.format("failed reading output from command '%s', %s", command, ioe.getMessage());
                 throw new BuildException(msg);
             }
         }
        getProject().setNewProperty(getAttr(longVersionAttr), longVersion);
        getProject().setNewProperty(getAttr(cleanAttr), clean.toString());
        getProject().setNewProperty(getAttr(shortVersionAttr), shortVersion);
     }
     
     public void setGitpath(final String value) {
         setAttr(gitPathAttr, value);
     }
     
     public void setTagprefix(final String value) {
         setAttr(tagPrefixAttr, value);
     }
     
     public void setLongversionproperty(final String value) {
         setAttr(longVersionAttr, value);
     }
     
     public void setShortversionproperty(final String value) {
         setAttr(shortVersionAttr, value);
     }
     
     public void setCleanproperty(final String value) {
         setAttr(cleanAttr, value);
     }
 }
