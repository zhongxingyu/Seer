 package com.abiquo.git;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Retrieves the revision number under the Git version control.
  *
  * @goal git.info
  * @phase initialize
  * @requiresProject
  */
 public class GitInfoMojo extends AbstractMojo {
 
 	private static String GIT_INFO_COMMAND = "git log -1 --format=format:%h";
 	
 	/**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @readonly
      */
     private MavenProject project;
     
     /**
      * Entities to get git information.
      * 
      * @parameter
      */
     private Entry[] entries;
     
     @Override
 	public void execute() throws MojoExecutionException, MojoFailureException {		
 		try {
			if (entries == null) {
				entries = new Entry[]{new Entry(null, "git.info")};
			}
 			
 			for (Entry entry : entries) {
 				String path = entry.getPath();
 				String prefix = entry.getPrefix() != null ? entry.getPrefix() : "git.info";
 				
 				String info = getInfo(path);			
				project.getProperties().setProperty(prefix + ".revision", info);			
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException("error retrieving git information", e);
 		}
 	}
 	
 	private String getInfo(String path) throws IOException {
 		
 
 		Runtime runtime = Runtime.getRuntime();
 		Process proc;
 		
 		if (path != null && !path.isEmpty()) {
 			proc = runtime.exec(GIT_INFO_COMMAND, new String[0], new File(path).getAbsoluteFile());
 		} else {
 			proc = runtime.exec(GIT_INFO_COMMAND);
 		}
 		
 		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 		
 		String line;
 		StringBuilder builder = new StringBuilder();
 		while (!procDone(proc)) {
 	        while((line = stdInput.readLine()) != null) {
 	        	builder.append(line);
 	        }
 		}
 		
 		return builder.toString().trim();
 	}
 	
 	private boolean procDone(Process p) {
         try {
             p.exitValue();
             return true;
         }
         catch(IllegalThreadStateException e) {
             return false;
         }
     }
 }
