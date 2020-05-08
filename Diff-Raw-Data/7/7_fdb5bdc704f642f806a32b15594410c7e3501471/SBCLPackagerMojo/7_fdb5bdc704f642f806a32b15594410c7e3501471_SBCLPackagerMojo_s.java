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
  * @goal package-sbcl
  * @phase package
  * @requiresProject true
  */
 public class SBCLPackagerMojo extends AbstractMojo {
 	
 	/**
 	 * @parameter default-value="${project}"
 	 */
 	private MavenProject project;
 	
 	/**
 	 * @parameter expression="${mainPackage}"
 	 * @required
 	 */
 	private String mainPackage;
 	
 	/**
 	 * @parameter expression="${coreName}" default-value="main"
 	 */
 	private String coreName;
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		// TODO Auto-generated method stub
 		File f = new File ("target");
 		f.mkdir();
 		SBCLProcess process = new SBCLProcess();
 		SourceLayout layout = new StandardLayout(project);
		LispCommand command = new SBCLCommand(true);
		command.setLayout(layout);
		command.silenceWarnings();
		command.setMainPackage(mainPackage);
 		command.setCoreName(coreName);
 		process.addCommand(command);
 		process.start();
 	}
 
 }
