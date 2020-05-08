 package com.github.ashtonkem.mojos;
 
 
 import java.io.File;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.github.ashtonkem.Processes.SBCLProcess;
 import com.github.ashtonkem.command.LispCommand;
 import com.github.ashtonkem.command.SBCLCommand;
 import com.github.ashtonkem.configuration.SourceLayout;
 import com.github.ashtonkem.configuration.StandardLayout;
 
 
 /**
  * 
  * @author ashtonkemerling
  * @goal compile-sbcl
  * @phase compile
  * @requiresProject true
  */
 public class SBCLCompilerMojo extends AbstractMojo {
 
 	/**
 	 * @parameter default-value="${project}"
 	 */
 	private MavenProject project;
 	
 	/**
 	 * @parameter expression="${mainPackage}"
 	 * @required
 	 */
 	private String mainPackage;
 	
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		File f = new File ("target");
 		f.mkdir();
 		SBCLProcess process = new SBCLProcess();
 		SourceLayout layout = new StandardLayout(project);
		LispCommand command = new SBCLCommand(true);
		command.setLayout(layout);
 		command.setMainPackage(mainPackage);
 		process.addCommand(command);
 		process.start();
 		
 		
 	}
 
 }
