 package org.oddjob.ant;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.BuildLogger;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.PropertyHelper;
 import org.apache.tools.ant.Target;
 import org.apache.tools.ant.util.StringUtils;
 import org.oddjob.Stoppable;
 import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
 import org.oddjob.arooa.deploy.annotations.ArooaElement;
 import org.oddjob.framework.SerializableJob;
 import org.oddjob.util.ClassLoaderDiagnostics;
 import org.oddjob.util.OddjobConfigException;
 
 /**
  * @oddjob.description Run a series of <a href="http://ant.apache.org">Ant</a>
  * tasks. 
  * 
  * <p>
  * Oddjob creates it's own Ant project to use internally this project can be
  * shared between different AntJob jobs using the 'project' attribute. This
  * allows taskdefs and properties to be defined in one place and shared
  * in many jobs.
  * 
  * <p>Oddjob component properties can be referenced inside an Ant tasks using
  * the ${id.property} notation. Ant will look up the Oddjob property
  * before it looks up properties defined with the &lt;property&gt; tag.
  * The oddjob derived properties of an Ant task aren't 
  * constant. Oddjob variables can change unlike Ant properties.
  *
  * <p>
  * <em>Not all tasks have been tested.</em>
  * 
  * <p>
  * Note: Ant looks up properties beginning with 'ant.' - Therefore <em>no 
  * component can have an id of 'ant'</em> as the lookup will fail to retrieve the
  * properties from that component (unless of course the 'ant' component implements all
  * the properties that Ant requires!).
  * 
  * 
  * @oddjob.example
  * 
  * Running an Ant Echo Task. The resultant output is captured in a buffer.
  * 
  * {@oddjob.xml.resource org/oddjob/ant/AntEchoAndCapture.xml}
  * 
  * @oddjob.example
  * 
  * Defining Ant properties in an Ant Job.
  * 
  * {@oddjob.xml.resource org/oddjob/ant/AntSettingPropertiesInAnt.xml}
  * 
  * @oddjob.example
  * 
  * Using Oddjob variables. Variables and properties defined in Oddjob are
  * available in the Ant tasks.
  * 
 * {@oddjob.xml.resource org/oddjob/ant/AntUsingOddjobProperties.xml}
  * 
 * Note that the property defined in Ant does not override that defined
  * in Oddjob (as per the rules of Ant). the result of both one and two is
  * 'Apples'
  * 
  * @oddjob.example
  * 
  * Sharing a project.
  * 
 * {@oddjob.xml.resource org/oddjob/ant/AntSharingProjects.xml}
  * 
  * The first Ant job declares a task and properties that the second
  * Ant project can access.
  * 
  * @oddjob.example
  * 
  * Working with files in Ant.
  * 
  * {@oddjob.xml.resource org/oddjob/ant/AntWorkingWithFiles.xml}
  * 
  * @author rob
  */
 public class AntJob extends SerializableJob 
 implements Stoppable {
     static final long serialVersionUID = 2009042400L;
 
     private static Map<String, Integer> messageLevels = 
     	new HashMap<String, Integer>();
     
     static {
     	messageLevels.put("DEBUG", new Integer(Project.MSG_DEBUG));
     	messageLevels.put("ERR", new Integer(Project.MSG_ERR));
     	messageLevels.put("INFO", new Integer(Project.MSG_INFO));
     	messageLevels.put("VERBOSE", new Integer(Project.MSG_VERBOSE));
     	messageLevels.put("WARN", new Integer(Project.MSG_WARN));
     }
     
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description A reference to project in another ant job.
 	 * This allows reference ids to be shared.
 	 * 
 	 * @oddjob.required No.
 	 */
 	private volatile transient Project project;
 	
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description The message level for output. one of
 	 * DEBUG, ERROR, INFO, VERBOSE, WARN.
 	 * @oddjob.required No.
 	 */
 	private transient String messageLevel;
 	
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description Where to write the resultant output from ant.
 	 * @oddjob.required No. By default the output will only be written to 
 	 * the logger.
 	 */
 	private transient OutputStream output;
 	
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description The ant XML configuration for the tasks. The
 	 * document element for the task definitions is expected to be
 	 * &lt;tasks&gt;. Any type of content that is normally contained in
 	 * an Ant target is allowed as child elements of &lt;tasks&gt; including
 	 * properties and task definitions.
 	 * 
 	 * @oddjob.required Yes.
 	 */
 	private transient String tasks;
 	
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description The base directory. Equivalent to setting the
 	 * basedir attribute of an ant project.
 	 * @oddjob.required No.
 	 */
 	private transient File baseDir;
 	
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description How to handle build failure. If true, then a build 
 	 * failure will in an EXCEPTION state for this job.
 	 * @oddjob.required No, defaults to false.
 	 */
 	private transient boolean exception;
 
 	/** 
 	 * @oddjob.property 
 	 * @oddjob.description An optional class loader which will be set as
 	 * Ant's class loader.
 	 * @oddjob.required No.
 	 */
 	private transient ClassLoader classLoader;
 	
 	
 	private volatile transient Thread executionThread;
 	
 	
 	public void setOutput(OutputStream out) {
 		this.output = out;
 	}
 	
 	/**
 	 * Return the ant tasks output.
 	 * 
 	 * @return The output as a string.
 	 */	
 	public OutputStream getOutput() {
 		return output;
 	}
 	
 	/**
 	 * Get the project used for the tasks.
 	 * 
 	 * @return The project.
 	 */
 	public Project getProject() {
 		return project;
 	}
 	
 	/**
 	 * Set the project to use for the tasks. 
 	 * It is assumed the project is already initialised.
 	 * 
 	 * @param project The project.
 	 */	
 	@ArooaAttribute
 	public void setProject(Project project) {
 		this.project = project;
 	}
 
 	@ArooaAttribute
 	public void setMessageLevel(String messageLevel) {
 		messageLevel = messageLevel.toUpperCase();
 		if (messageLevels.get(messageLevel) == null) {
 			throw new OddjobConfigException("Message level of [" + messageLevel + "] not known.");
 		}
 		this.messageLevel = messageLevel;
 	}
 	
 	public String getMessageLevel() {
 		return messageLevel;
 	}
 	
 	int messageLevel() {
 		if (messageLevel == null) {
 			return Project.MSG_INFO;
 		}
 		return ((Integer)messageLevels.get(messageLevel)).intValue();
 	}
 	
 	/**
 	 * Execute the tasks.
 	 */	
 	protected int execute() throws Exception {
 
 		if (tasks == null) {
 			throw new IllegalStateException("Tasks Property must be provided.");
 		}
 		
 		if (project == null) {
 			project = new Project();			
 			if (classLoader != null) {
 				logger().debug("Setting classloader to: " + classLoader);
 			    project.setCoreLoader(classLoader);
 			}
 			else {
 				project.setCoreLoader(getClass().getClassLoader());
 			}
 			if (baseDir != null) {
 				project.setBaseDir(baseDir);
 			}
 			project.init();
 		}
 		
 		ClassLoader existing = Thread.currentThread().getContextClassLoader();
 		BuildLogger logger = new DefaultLogger();
 		DebugBuildListener debug = new DebugBuildListener();
 		
 		try {
 			Thread.currentThread().setContextClassLoader(
 					project.getCoreLoader());
 			
 			ClassLoaderDiagnostics.logClassLoaderStack(project.getCoreLoader());
 			
 			OJPropertyHelper ourPropertyHelper = new OJPropertyHelper(
 					getArooaSession());
 					
 			PropertyHelper ph = PropertyHelper.getPropertyHelper(project);
 			ph.add(ourPropertyHelper);
 			
 			AntParser parser = new AntParser(project);
 			
 			parser.parse(tasks);
 			
 			if (output != null) {
 				logger.setOutputPrintStream(new PrintStream(output));
 				logger.setErrorPrintStream(new PrintStream(output));
 				logger.setMessageOutputLevel(messageLevel());
 				project.addBuildListener(logger);
 			}
 			
 			debug.setMessageOutputLevel(messageLevel());
 			project.addBuildListener(debug);
 			
 			Target target = (Target) project.getTargets().get(
 					AntParser.TARGET_NAME);
 
 			executionThread = Thread.currentThread();
 			target.execute();
 			return 0;
 		}
 		catch (BuildException e) {
 			if (exception) {
 				throw e;
 			}
 			logger().warn("Build Exception:", e);
 			return 1;
 		} 
 		finally {
 			executionThread = null;
 			Thread.currentThread().setContextClassLoader(
 					existing);
 
 			if (output != null) {
 				project.removeBuildListener(logger);
 				output.close();
 			}
 			project.removeBuildListener(debug);
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		if (executionThread != null) {
 			executionThread.interrupt();
 		}
 	}
 
 	@Override
 	protected void onReset() {
 		project = null;
 	}
 	
 	private void writeObject(ObjectOutputStream s) 
 	throws IOException {
 		s.defaultWriteObject();
 	}
 	
 	private void readObject(ObjectInputStream s) 
 	throws IOException, ClassNotFoundException {
 		s.defaultReadObject();
 	}
 	
 	class DebugBuildListener implements BuildListener {
 	    /**
 	     * Size of left-hand column for right-justified task name.
 	     * @see #messageLogged(BuildEvent)
 	     */
 	    public static final int LEFT_COLUMN_SIZE = 12;
 
 	    /** Lowest level of message to write out */
 	    protected int msgOutputLevel = Project.MSG_ERR;
 
 	    /** Line separator */
 	    protected final String lSep = StringUtils.LINE_SEP;
 
 	    /**
 	     * Sets the highest level of message this logger should respond to.
 	     *
 	     * Only messages with a message level lower than or equal to the
 	     * given level should be written to the log.
 	     * <P>
 	     * Constants for the message levels are in the
 	     * {@link Project Project} class. The order of the levels, from least
 	     * to most verbose, is <code>MSG_ERR</code>, <code>MSG_WARN</code>,
 	     * <code>MSG_INFO</code>, <code>MSG_VERBOSE</code>,
 	     * <code>MSG_DEBUG</code>.
 	     * <P>
 	     * The default message level for DefaultLogger is Project.MSG_ERR.
 	     *
 	     * @param level the logging level for the logger.
 	     */
 	    public void setMessageOutputLevel(int level) {
 	        this.msgOutputLevel = level;
 	    }
 
 	    /**
 	     * Ignore - Ant job doesn't start a build.
 	     *
 	     * @param event Ignored.
 	     */
 	    public void buildStarted(BuildEvent event) {
 	    }
 
 	    /**
 	     * Ignore = Ant job doesn't finish a build.
 	     * 
 	     * @param event Ignored
 	     */
 	    public void buildFinished(BuildEvent event) {
 	    }
 
 	    /**
 	     * Ignore - Ant Job doesn't start targets.
 	     * 
 	     * @param event Ignored 
 	      */
 	    public void targetStarted(BuildEvent event) {
 	    }
 
 	    /**
 	     * No-op implementation.
 	     *
 	     * @param event Ignored.
 	     */
 	    public void targetFinished(BuildEvent event) {
 	    }
 
 	    /**
 	     * No-op implementation.
 	     *
 	     * @param event Ignored.
 	     */
 	    public void taskStarted(BuildEvent event) {
 	    }
 
 	    /**
 	     * No-op implementation.
 	     *
 	     * @param event Ignored.
 	     */
 	    public void taskFinished(BuildEvent event) {
 	    }
 
 	    /**
 	     * Logs a message, if the priority is suitable.
 	     * In non-emacs mode, task level messages are prefixed by the
 	     * task name which is right-justified.
 	     *
 	     * @param event A BuildEvent containing message information.
 	     *              Must not be <code>null</code>.
 	     */
 	    public void messageLogged(BuildEvent event) {
 	        int priority = event.getPriority();
 	        // Filter out messages based on priority
 	        if (priority <= msgOutputLevel) {
 
 	            StringBuffer message = new StringBuffer();
 	            if (event.getTask() != null) {
 	                // Print out the name of the task if we're in one
 	                String name = event.getTask().getTaskName();
 	                String label = "[" + name + "] ";
 	                int size = LEFT_COLUMN_SIZE - label.length();
 	                StringBuffer tmp = new StringBuffer();
 	                for (int i = 0; i < size; i++) {
 	                    tmp.append(" ");
 	                }
 	                tmp.append(label);
 	                label = tmp.toString();
 
 	                try {
 	                    BufferedReader r =
 	                        new BufferedReader(
 	                            new StringReader(event.getMessage()));
 	                    String line = r.readLine();
 	                    boolean first = true;
 	                    while (line != null) {
 	                        if (!first) {
 	                            message.append(StringUtils.LINE_SEP);
 	                        }
 	                        first = false;
 	                        message.append(label).append(line);
 	                        line = r.readLine();
 	                    }
 	                } catch (IOException e) {
 	                    // shouldn't be possible
 	                    message.append(label).append(event.getMessage());
 	                }
 	            } else {
 	                message.append(event.getMessage());
 	            }
 
 	            String msg = message.toString();
 		        logger().info(msg);
 	        }
 	        // A really bad way to try and stop the build.
 	        if (stop) {
 	        	throw new RuntimeException("Stop Has Been Requested.");
 	        }
 	    }
 
 	}
 
 	public boolean isException() {
 		return exception;
 	}
 
 	@ArooaAttribute
 	public void setException(boolean exception) {
 		this.exception = exception;
 	}
 
 	public File getBaseDir() {
 		return baseDir;
 	}
 
 	@ArooaAttribute
 	public void setBaseDir(File baseDir) {
 		this.baseDir = baseDir;
 	}
 
 	public String getTasks() {
 		return tasks;
 	}
 
 	@ArooaElement
 	public void setTasks(String xml) {
 		this.tasks = xml;
 	}
 
 	public ClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	public void setClassLoader(ClassLoader classLoader) {
 		this.classLoader = classLoader;
 	}
 	
 	/**
 	 * @oddjob.property version
 	 * @oddjob.description The ant version.
 	 * 
 	 * @return The ant version or nothing if the job hasn't run yet.
 	 */
 	public String getVersion() {
 		if (project == null) {
 			return null;
 		}
 		return project.getProperty("ant.version");
 	}
 }
