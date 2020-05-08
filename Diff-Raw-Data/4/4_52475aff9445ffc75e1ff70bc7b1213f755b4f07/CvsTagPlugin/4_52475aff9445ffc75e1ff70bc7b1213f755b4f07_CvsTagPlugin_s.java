 package hudson.plugins.cvs_tag;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Array;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Hudson;
 import hudson.model.Result;
 import hudson.scm.CVSSCM;
 import hudson.util.ArgumentListBuilder;
 import org.codehaus.groovy.control.CompilerConfiguration;
 
 
 /**
  * @author Brendt Lucas
  */
 public class CvsTagPlugin
 {
 	static final String DESCRIPTION = "Perform CVS tagging on succesful build";
 
 	static final String CONFIG_PREFIX = "cvstag.";
 
 	private CvsTagPlugin()
 	{
 	}
 
 	private static AbstractProject getRootProject(AbstractProject abstractProject)
 	{
 		if (abstractProject.getParent() instanceof Hudson)
 		{
 			return abstractProject;
 		}
 		else
 		{
 			return getRootProject((AbstractProject) abstractProject.getParent());
 		}
 	}
 
 
 	public static boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, String tagName)
 	{
 		PrintStream logger = listener.getLogger();
 
 		if (!Result.SUCCESS.equals(build.getResult()))
 		{
 			logger.println("Skipping CVS Tagging as build result was not successful.");
 
 			return true;
 		}
 
 		AbstractProject rootProject = getRootProject(build.getProject());
 
 		if (!(rootProject.getScm() instanceof CVSSCM))
 		{
 			logger.println("CVS Tag plugin does not support tagging for SCM " + rootProject.getScm() + ".");
 
 			return true;
 		}
 
 		CVSSCM scm = CVSSCM.class.cast(rootProject.getScm());
 
 		// Evaluate the groovy tag name
 		Map<String, String> env = build.getEnvVars();
 		tagName = evalGroovyExpression(env, tagName);
 
 		// Get the modules we will be tagging
 		String modules = arrayToString(scm.getAllModulesNormalized(), " ");
 
 		// -D option for rtag command.
 		// Tag the most recent revision no later than <date> ...
 		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(build.getTimestamp().getTime());
 
 		ArgumentListBuilder cmd = new ArgumentListBuilder();
 
 		if (scm.getBranch() != null)
 		{
			// cvs -d cvsRoot rtag -r branchName -D toDate tagName modules
			cmd.add(scm.getDescriptor().getCvsExeOrDefault(), "-d", scm.getCvsRoot(), "rtag", "-r", scm.getBranch(), "-D", date, tagName, modules);
 		}
 		else
 		{
 			// cvs -d cvsRoot rtag -D toDate tagName modules
 			cmd.add(scm.getDescriptor().getCvsExeOrDefault(), "-d", scm.getCvsRoot(), "rtag", "-D", date, tagName, modules);
 		}
 
 		logger.println("Executing tag command: " + cmd.toStringWithQuote());
 
 		File tempDir = null;
 		try
 		{
 			tempDir = Util.createTempDir();
 			int exitCode = launcher.launch(cmd.toCommandArray(), env, logger, new FilePath(tempDir)).join();
 			if (exitCode != 0)
 			{
 				listener.fatalError(CvsTagPublisher.DESCRIPTOR.getDisplayName() + " failed. exit code=" + exitCode);
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace(listener.error(e.getMessage()));
 			logger.println("IOException occurred: " + e);
 			return false;
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace(listener.error(e.getMessage()));
 			logger.println("InterruptedException occurred: " + e);
 			return false;
 		}
 		finally
 		{
 			try
 			{
 				if (tempDir != null)
 				{
 					logger.println("cleaning up " + tempDir);
 					Util.deleteRecursive(tempDir);
 				}
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace(listener.error(e.getMessage()));
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Converts an array into a delimited String
 	 *
 	 * @param array     the array to convert
 	 * @param delimiter The delimiter used in the String representation
 	 * @return a delimited String representation of the array
 	 */
 	private static String arrayToString(Object array, String delimiter)
 	{
 		int length = Array.getLength(array);
 
 		if (length == 0)
 		{
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder(2 * length - 1);
 
 		for (int i = 0; i < length; i++)
 		{
 			if (i > 0)
 			{
 				sb.append(delimiter);
 			}
 
 			sb.append(Array.get(array, i));
 		}
 
 		return sb.toString();
 	}
 
 	static String evalGroovyExpression(Map<String, String> env, String expression)
 	{
 		Binding binding = new Binding();
 		binding.setVariable("env", env);
 		binding.setVariable("sys", System.getProperties());
 		CompilerConfiguration config = new CompilerConfiguration();
 		//config.setDebug(true);
 		GroovyShell shell = new GroovyShell(binding, config);
 		Object result = shell.evaluate("return \"" + expression + "\"");
 		if (result == null)
 		{
 			return "";
 		}
 		else
 		{
 			return result.toString().trim();
 		}
 	}
 }
