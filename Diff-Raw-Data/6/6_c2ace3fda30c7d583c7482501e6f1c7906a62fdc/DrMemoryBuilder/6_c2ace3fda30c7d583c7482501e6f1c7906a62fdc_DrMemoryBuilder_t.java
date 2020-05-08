 package net.praqma.jenkins.plugin.drmemory;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.logging.Logger;
 
 import jenkins.model.Jenkins;
 
 import net.praqma.drmemory.DrMemory;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
import java.io.File;
 
 public class DrMemoryBuilder extends Builder {
 	
 	private static final Logger logger = Logger.getLogger( DrMemoryBuilder.class.getName() );
 	
 	private String executable;
 	private String arguments;
 	private String logPath;
 	
 	private String finalLogPath;
 	
 	@DataBoundConstructor
 	public DrMemoryBuilder( String executable, String arguments, String logPath ) {
 		this.executable = executable;
 		this.arguments = arguments;
 		this.logPath = logPath;
 	}
 	
	@Override
 	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException {
 		PrintStream out = listener.getLogger();
 		//DrMemory.enableLogging();
 		
 		/* Add the action */
 		DrMemoryBuildAction dba = new DrMemoryBuildAction( build, this );
 		build.addAction( dba );
 		
 		String version = Jenkins.getInstance().getPlugin( "drmemory-plugin" ).getWrapper().getVersion();
 		out.println( "Dr Memory Plugin version " + version );
 		
 		try {
			finalLogPath = logPath += ( logPath.endsWith( File.separator ) ? "" : File.separator );
 			finalLogPath += build.getNumber();
 			build.getWorkspace().act( new DrMemoryRemoteBuilder( executable, arguments, finalLogPath, listener ) );
 			return true;
 		} catch( IOException e ) {
 			out.println( "Unable to execute Dr. Memory: " + e.getMessage() );
 			return false;
 		}
 	}
 	
 	public String getExecutable() {
 		return executable;
 	}
 	
 	public String getArguments() {
 		return arguments;
 	}
 	
 	public String getLogPath() {
 		return logPath; 
 	}
 	
 	public String getFinalLogPath() {
 		return finalLogPath;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
 		
 		@Override
 		public DrMemoryBuilder newInstance( StaplerRequest req, JSONObject data ) {
 
 			DrMemoryBuilder instance = req.bindJSON( DrMemoryBuilder.class, data );
 			
 			save();
 			return instance;
 		}
 		
 		@Override
 		public boolean isApplicable( Class<? extends AbstractProject> jobType ) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Execute with Dr. Memory";
 		}		
 	}
 }
