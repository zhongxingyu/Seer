 /**
  * 
  */
 package org.dotplot.core;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.dotplot.core.services.DefaultErrorHandler;
 import org.dotplot.core.services.IJob;
 import org.dotplot.core.system.CoreSystem;
 import org.dotplot.util.DuplicateRegistrationException;
 import org.dotplot.util.UnknownIDException;
 
 /**
  * This class handles the creation of the <code>DotplotContext</code> of the
  * system.
  * <p>
  * Before invoking {@link #getContext()} it is possible to change some context
  * parametes, like the plugin directory, the working dirextory and the schema
  * file. After invokin {@link #getContext()} the <code>DotplotContext</code>
  * will be created and persistent in the <code>ContextFactory</code> for
  * runntime.
  * </p>
  * 
  * @author Christian Gerhardt <case42@gmx.net>
  * @see DotplotContext
  * @see org.dotplot.core.system.CoreSystem
  * @see org.dotplot.core.system.StartUpJob
  * @see org.dotplot.core.system.PluginLoadingJob
  */
 public class ContextFactory {
 
     /**
      * Logger for debug information.
      */
     private static Logger logger = Logger.getLogger(ContextFactory.class
 	    .getName());
 
     /**
      * The default plugin directory
      */
     private static String pluginDirectory = "./plugins/";
 
     /**
      * The default working directory.
      */
     private static String workingDirectory = ".";
 
     /**
      * The default shema file.
      */
     private static String shemaFile = "./ressources/dotplotschema.xsd";
 
     /**
      * Instance of the created <code>DotplotContext</code>.
      */
     private static DotplotContext context;
 
     /**
      * Creates a <code>DotplotContext</code>.
      * <p>
      * The <code>DotplotContext</code> will be created and the
      * <code>CoreSystem</code> plugin installed. After that, the
      * <code>PluginLoadingJob</code> and the <code>StartUpJob</code> are
      * executed.
      * </p>
      * 
      * @return The <code>DotplotContext</code>.
      * @see org.dotplot.core.system.CoreSystem
      * @see org.dotplot.core.system.StartUpJob
      * @see org.dotplot.core.system.PluginLoadingJob
      */
     private static DotplotContext createContext() {
	context = new DotplotContext(workingDirectory, pluginDirectory);
 	try {
 	    context.installPlugin(new CoreSystem());
 	} catch (DuplicateRegistrationException e) {
 	    /* hmm */
 	    logger.error("Error while installing CoreSystem", e);
 	}
 	try {
 	    File shemaFile = new File(ContextFactory.shemaFile);
 	    context.setShemaFile(shemaFile);
 	    DefaultErrorHandler handler = new DefaultErrorHandler();
 
 	    IJob job;
 	    job = context.getJobRegistry().get(CoreSystem.JOB_PLUGIN_LOADER_ID);
 	    job.setErrorHandler(handler);
 	    logger.debug("executing pluginloading job");
 	    context.executeJob(job);
 
 	    // no gui-output during context creation!
 	    context.setNoGui(true);
 
 	    job = context.getJobRegistry().get(CoreSystem.JOB_STARTUP_ID);
 	    job.setErrorHandler(handler);
 	    logger.debug("executing startup job");
 	    context.executeJob(job);
 
 	    context.setNoGui(false);
 	} catch (UnknownIDException e) {
 	    /* hmm */
 	}
 	return context;
     }
 
     /**
      * Returns the stored <code>DotplotContext</code>.
      * <p>
      * If the context isn't created yet, the context will be created at that
      * point.
      * </p>
      * 
      * @return The <code>DotplotContext</code>.
      */
    public static synchronized DotplotContext getContext() {
 	if (context == null) {
	    createContext();
 	}
 	return context;
     }
 
     /**
      * Sets the plugin directory.
      * 
      * @param directory
      *            Path of the plugin directory.
      */
     public static void setPluginDirectory(String directory) {
 	ContextFactory.pluginDirectory = directory;
     }
 
     /**
      * Sets the schema file.
      * 
      * @param schemaFile
      *            Path of the shema file.
      * @throws NullPointerException
      *             if shemaFile is <code>null</code>.
      */
     public static void setShemaFile(String schemaFile) {
 	if (schemaFile == null) {
 	    throw new NullPointerException();
 	}
 	logger.debug("Setting shemafile: " + schemaFile);
 	ContextFactory.shemaFile = schemaFile;
     }
 
     /**
      * Sets the working directory.
      * 
      * @param directory
      *            Path of the working directory.
      */
     public static void setWorkingDirectory(String directory) {
 	ContextFactory.workingDirectory = directory;
     }
 
     /**
      * Creates a new <code>ContextFactory</code>.
      */
     private ContextFactory() {
     }
 
 }
