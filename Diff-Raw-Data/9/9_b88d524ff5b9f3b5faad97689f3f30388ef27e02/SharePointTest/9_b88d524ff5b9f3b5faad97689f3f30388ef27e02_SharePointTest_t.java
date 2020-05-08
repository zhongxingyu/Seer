 package com.photon.phresco.plugins.sharepoint;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.codehaus.plexus.util.cli.Commandline;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 
 public class SharePointTest
 {
   private static String NUNIT_CMD = "nunit-console.exe";
   private static String STR_SPACE = " ";
   private static String NUNIT_REPORT_LOCATION = "\\source\\test\\target\\nunit-report\\";
   private static String TEST_FILE_LOC = "\\source\\test\\AllTest\\bin\\Release\\";
   private static String TEST_FILE_NAME = "AllTest.dll";
   private File baseDir;
   
   public void runUnitTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 	  baseDir = mavenProjectInfo.getBaseDir();
 	  init();
 	  try {
 		  fetchNUnit();
 		  executeTest();
 	  } catch (MojoExecutionException e) {
 		  throw new PhrescoException(e);
 	  }
   }
 
   private void init() {
     File temp = new File(new StringBuilder().append(this.baseDir.getPath()).append(NUNIT_REPORT_LOCATION).toString());
     if (!(temp.exists()))
       temp.mkdirs();
   }
 
   private void fetchNUnit() throws MojoExecutionException {
     StringBuilder sb;
     try {
       sb = new StringBuilder();
       sb.append("mvn");
       sb.append(STR_SPACE);
       sb.append("validate");
 
       Commandline cl = new Commandline(sb.toString());
       Process p = cl.execute();
       BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
       String line = null;
      while ((line = in.readLine()) != null) {
    	  System.out.println(line);
      }
     }
     catch (Exception e) {
       throw new MojoExecutionException(e.getMessage(), e);
     }
   }
 
   private void executeTest() throws MojoExecutionException {
     System.out.println("-----------------------------------------");
     System.out.println("T E S T S");
     System.out.println("-----------------------------------------");
     try
     {
       StringBuilder sb = new StringBuilder();
       sb.append(NUNIT_CMD);
       sb.append(STR_SPACE);
       sb.append("\"");
       sb.append(new StringBuilder().append(this.baseDir.getPath()).append(TEST_FILE_LOC).toString());
       sb.append(TEST_FILE_NAME);
       sb.append("\"");
       System.out.println("Command = " + sb.toString());
       Commandline cl = new Commandline(sb.toString());
       cl.setWorkingDirectory(new StringBuilder().append(this.baseDir.getPath()).append(NUNIT_REPORT_LOCATION).toString());
       Process execute = cl.execute();
       BufferedReader in = new BufferedReader(new InputStreamReader(execute.getInputStream()));
       String line = null;
      while ((line = in.readLine()) != null) {
    	  System.out.println(line);
      }
     }
     catch (Exception e) {
       throw new MojoExecutionException(e.getMessage(), e);
     }
   }
 }
