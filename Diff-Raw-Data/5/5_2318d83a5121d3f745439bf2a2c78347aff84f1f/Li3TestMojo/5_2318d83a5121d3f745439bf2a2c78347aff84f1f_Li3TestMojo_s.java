 package com.ning;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Goal which runs lithium php unit tests and fails if they fail.
  *
  * @goal li3-test
  * @phase test
  */
 public class Li3TestMojo extends AbstractMojo
 {
     /**
      * Location of the file.
      *
      * @parameter expression="${basedir}"
      * @required
      */
     private File baseDir;
 
     private Log log;
     private Pattern passesPattern;
     private Pattern failsPattern;
 
     public Li3TestMojo()
     {
         log = getLog();
         passesPattern = Pattern.compile("(\\d+) / (\\d+) passes");
         failsPattern = Pattern.compile("(\\d+) fails and (\\d+) exceptions");
     }
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         boolean failed = false;
         List<String> command = new ArrayList<String>();
         try {
             command.add(baseDir.getCanonicalPath() + "/libraries/lithium/console/li3");
         }
         catch (IOException e) {
             throw new MojoExecutionException("Couldn't get the canonical path of the basedir");
         }
         command.add("test");
         command.add("app/tests");
         ProcessBuilder builder = new ProcessBuilder(command);
         try {
             Process process = builder.start();
             BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
             String line;
             while ((line = input.readLine()) != null) {
                 log.info(line);
                 if(isFailure(line)){
                     failed = true;
                 }
             }
             input.close();
             while ((line = error.readLine()) != null) {
                 log.error(line);
                if(isFailure(line)){
                    failed = true;
                }
             }
             error.close();
             process.waitFor();
         }
         catch (IOException e) {
             throw new MojoExecutionException("Couldn't get process to start.");
         }
         catch (InterruptedException e) {
             throw new MojoExecutionException("Problem while waiting for process.");
         }
         if(failed){
             throw new MojoFailureException("Lithium Tests Failed");
         }
     }
 
     private boolean isFailure(String line)
     {
         List<String> failureStrings = new ArrayList<String>();
         failureStrings.add("Fatal error:");
         for(String failureString : failureStrings){
             if(line.contains(failureString)){
                 return true;
             }
         }
         Matcher passesMatcher = passesPattern.matcher(line);
         if(passesMatcher.find()){
             int first = Integer.parseInt(passesMatcher.group(1));
             int second = Integer.parseInt(passesMatcher.group(2));
             if (first != second){
                 return true;
             }
         }
         Matcher failsMatcher = failsPattern.matcher(line);
         if(failsMatcher.find()){
             int fails = Integer.parseInt(failsMatcher.group(1));
             int exceptions = Integer.parseInt(failsMatcher.group(2));
             if(fails != 0 || exceptions != 0){
                 return true;
             }
         }
         return false;
     }
 }
