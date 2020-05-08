 package de.mittwald.jenkins.typo3surf;
 
 import hudson.EnvVars;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import net.sf.json.JSONObject;
 
 import org.apache.tools.ant.BuildException;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Typo3SurfBuilder extends Builder
 {
 
 	private final String	deployment;
 
 	// Fields in config.jelly must match the parameter names in the
 	// "DataBoundConstructor"
 	@DataBoundConstructor
 	public Typo3SurfBuilder(String deployment)
 	{
 		this.deployment = deployment;
 	}
 
 	/**
 	 * We'll use this from the <tt>config.jelly</tt>.
 	 */
 	public String getDeployment()
 	{
 		return deployment;
 	}
 
 	protected boolean deploymentExists(PrintStream out)
 	{
 		try
 		{
 			Process process = new ProcessBuilder(getDescriptor().getSurfPath(),
 					"surf:list", "--quiet").start();
 			BufferedReader bri = new BufferedReader(new InputStreamReader(
 					process.getInputStream()));
 			String line;
 			boolean found = false;
 
 			while ((line = bri.readLine()) != null)
 			{
 				if (line.equals(deployment))
 				{
 					found = true;
 				}
 			}
 			bri.close();
 
 			return found;
 		}
 		catch (Exception e)
 		{
 			out.println("Error on surf:list " + e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws InterruptedException, IOException
 	{
 		if (!deploymentExists(listener.getLogger()))
 		{
			listener.fatalError(
 					"Deployment " + deployment + " does not exist!");
 			return false;
 		}
 
 		ArgumentListBuilder args = new ArgumentListBuilder();
 		EnvVars env = build.getEnvironment(listener);
 
 		env.put("FLOW_CONTEXT", getDescriptor().getSurfContext());
 		args.add(getDescriptor().getSurfPath(), "surf:deploy", "--disable-ansi", deployment);
 
 		try
 		{
 			int result = launcher.launch().cmds(args).envs(env).stdout(listener.getLogger()).join();
 			return result == 0;
 		}
 		catch (IOException e)
 		{
			listener.fatalError(e.getMessage());
 			return false;
 		}
 	}
 
 	// Overridden for better type safety.
 	// If your plugin doesn't really define any property on Descriptor,
 	// you don't have to do this.
 	@Override
 	public DescriptorImpl getDescriptor()
 	{
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends
 			BuildStepDescriptor<Builder>
 	{
 		private String	surfPath	= "/usr/bin/surf";
 		private String	surfContext	= "Production";
 
 		public FormValidation doCheckSurfPath(@QueryParameter String value)
 				throws IOException, ServletException
 		{
 			if (new File(value).canExecute() == false)
 			{
 				return FormValidation
 						.error("No TYPO3 Surf installation found at " + value);
 			}
 			return FormValidation.ok();
 		}
 
 		public FormValidation doCheckSurfContext(@QueryParameter String value)
 				throws IOException, ServletException
 		{
 			if (!value.equals("Development") && !value.equals("Production"))
 			{
 				return FormValidation.error("Invalid TYPO3 Flow context: "
 						+ value);
 			}
 			return FormValidation.ok();
 		}
 
 		@SuppressWarnings("rawtypes")
 		public boolean isApplicable(Class<? extends AbstractProject> aClass)
 		{
 			return true;
 		}
 
 		public String getDisplayName()
 		{
 			return "TYPO3 Surf Deployment";
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject formData)
 				throws FormException
 		{
 			surfPath = formData.getString("surfPath");
 			surfContext = formData.getString("surfContext");
 
 			save();
 
 			return super.configure(req, formData);
 		}
 
 		public String getSurfPath()
 		{
 			return surfPath;
 		}
 
 		public String getSurfContext()
 		{
 			return surfContext;
 		}
 	}
 }
