 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.requestor;
 
 import groovy.lang.GroovyShell;
 import net.jini.core.transaction.Transaction;
 import net.jini.core.transaction.TransactionException;
 import org.codehaus.groovy.control.CompilationFailedException;
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 import sorcer.resolver.Resolver;
 import sorcer.service.*;
 import sorcer.tools.webster.InternalWebster;
 import sorcer.util.Artifact;
 import sorcer.util.Sorcer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import sorcer.util.ArtifactCoordinates;
 
 import static sorcer.util.ArtifactCoordinates.coords;
 
 /**
  * The abstract class with the methods that define the initialization and
  * behavior of a service requestor. This class uses {@link sorcer.core.SorcerConstants} and
  * requires subclasses to implement {@link sorcer.core.requestor.Requestor#getExertion method. The
  * main method of the class initializes requestor properties and calls methods
  * of the {@link sorcer.core.requestor.Requestor} interface accordingly. It attempts to create a new
  * instance of the requestor defined by a subclass extending
  * {@link sorcer.core.requestor.ServiceRequestor}. If an internal class server (webster) is needed
  * then it attempts to start it with given class root paths that define the
  * codebase of the underlying service requestor.
  * 
  * @author M. W. Sobolewski
  * @see SorcerConstants
  */
 abstract public class ServiceRequestor implements Requestor, SorcerConstants {
 	/** Logger for logging information about this instance */
 	protected static final Logger logger = Logger
 			.getLogger(ServiceRequestor.class.getName());
 
 	final static String REQUESTOR_PROPERTIES_FILENAME = "requestor.properties";
 	protected Properties props;
 	protected Exertion exertion;
 	protected String jobberName;
 	protected GroovyShell shell;
     protected static String[] codebaseJars;
     protected static boolean isWebsterInt = false;
 	protected static ServiceRequestor requestor = null;
 	
 	public static void main(String... args) throws Exception {
         prepareCodebase();
         initialize(args);
         requestor.preprocess(args);
 		requestor.process(args);
 		requestor.postprocess(args);
 	}
 
 	public static void initialize(String... args) {
 		System.setSecurityManager(new RMISecurityManager());
 
 		// Initialize system properties: configs/sorcer.env
 		Sorcer.getEnvProperties();
 
 		String runnerType = null;
 		if (args.length == 0) {
 			System.err
 					.println("Usage: Java sorcer.core.requestor.ServiceRequestor  <runnerType>");
 			System.exit(1);
 		} else {
 			runnerType = args[0];
 		}
 		try {
 			requestor = (ServiceRequestor) Class.forName(runnerType)
 					.newInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.info("Not able to create service runner: " + runnerType);
 			System.exit(1);
 		}
 		String str = System.getProperty(REQUESTOR_PROPERTIES_FILENAME);
 		logger.info(REQUESTOR_PROPERTIES_FILENAME + " = " + str);
 		if (str != null) {
 			requestor.loadProperties(str); // search the provider package
 		} else {
 			requestor.loadProperties(REQUESTOR_PROPERTIES_FILENAME);
 		}
 		isWebsterInt = false;
 		String val = System.getProperty(SORCER_WEBSTER_INTERNAL);
 		if (val != null && val.length() != 0) {
 			isWebsterInt = val.equals("true");
 		}
 		if (isWebsterInt) {
 			String roots = System.getProperty(SorcerConstants.WEBSTER_ROOTS);
 			String[] tokens = null;
 			if (roots != null)
 				tokens = toArray(roots);
 			try {
 				InternalWebster.startWebster(codebaseJars, tokens);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void setExertion(Exertion exertion) {
 		this.exertion = exertion;
 	}
 
 	public String getJobberName() {
 		return jobberName;
 	}
 
 	public void preprocess(String... args) {
 		Exertion in = null;
 		try {
 			in = requestor.getExertion(args);
 			if (logger.isLoggable(Level.FINE))
 				logger.fine("Runner java.rmi.server.codebase: "
 						+ System.getProperty("java.rmi.server.codebase"));
 		} catch (ExertionException e) {
 			logger.throwing("ServiceRequestor", "main", e);
 			System.exit(1);
 		} catch (ContextException e) {
 			logger.throwing("ServiceRequestor", "main", e);
 			System.exit(1);
 		} catch (SignatureException e) {
 			logger.throwing("ServiceRequestor", "main", e);
 			System.exit(1);
 		}
 		if (in != null)
 			requestor.setExertion(in);
 		if (exertion != null)
 			logger.info(">>>>>>>>>> Input context: \n" + exertion.getContext());
 	}
 
 	public void process(String... args) throws ExertionException {
 		try {
 			exertion = ((ServiceExertion) exertion).exert(
 					requestor.getTransaction(), requestor.getJobberName());
 		} catch (RemoteException re) {
 			logger.throwing("ServiceRequestor", "main", re);
 		} catch (TransactionException te) {
 			logger.throwing("ServiceRequestor", "main", te);
 		} catch (ExertionException ee) {
 			logger.throwing("ServiceRequestor", "main", ee);
 		}
 	}
 	
 	public void postprocess(String... args) {
 		if (exertion != null) {
 			logger.info("<<<<<<<<<< Exceptions: \n" + exertion.getExceptions());
 			logger.info("<<<<<<<<<< Traces: \n" + exertion.getControlContext().getTrace());
 			logger.info("<<<<<<<<<< Ouput context: \n" + exertion.getContext());
 		}
 	}
 
 	public Object evaluate(File scriptFile) throws CompilationFailedException,
 			IOException {
 		shell = new GroovyShell();
 		return shell.evaluate(scriptFile);
 	}
 	
 	public Transaction getTransaction() {
 		return null;
 	}
 
 	/**
 	 * Loads service requestor properties from a <code>filename</code> file. By
 	 * default a service requestor loads its properties from
 	 * <code>requestor.properties</code> file located in the requestor's
 	 * package. Also, a service requestor properties file name can be specified
 	 * as a system property when starting the requestor with
 	 * <code>-DrequestorProperties=&ltfilename&gt<code>. In this case the requestor loads 
 	 * properties from <code>filename</code> file. Properties are accessible
 	 * calling the <code>
 	 * getProperty(String)</code> method.
 	 * 
 	 * @param filename
 	 *            the properties file name see #getProperty
 	 */
 	public void loadProperties(String filename) {
 		logger.info("loading requestor properties:" + filename);
 		String propsFile = System.getProperty("requestor.properties.file");
 
 		try {
 			if (propsFile != null) {
 				props.load(new FileInputStream(propsFile));
 			} else {
 				// check the class resource
 				InputStream is = this.getClass().getResourceAsStream(filename);
 				// check local resource
 				if (is == null)
 					is = new FileInputStream(filename);
 				if (is != null) {
 					props = new Properties();
 					props.load(is);
 				} else {
 					System.err
 							.println("Not able to open stream on properties: "
 									+ filename);
 					System.err.println("Service runner class: "
 							+ this.getClass());
 					return;
 				}
 			}
 		} catch (IOException ioe) {
 			logger.info("Not able to load requestor properties");
 			// ioe.printStackTrace();
 		}
 
 	}
 
 	public String getProperty(String key) {
 		return props.getProperty(key);
 	}
 
 	public String getProperty(String property, String defaultValue) {
 		return props.getProperty(property, defaultValue);
 	}
 
 	private static String[] toArray(String arg) {
 		StringTokenizer token = new StringTokenizer(arg, " ,;");
 		String[] array = new String[token.countTokens()];
 		int i = 0;
 		while (token.hasMoreTokens()) {
 			array[i] = token.nextToken();
 			i++;
 		}
 		return (array);
 	}
 
 
     public static void prepareCodebase() {
         // Initialize system properties: configs/sorcer.env
         Sorcer.getEnvProperties();
         String val = System.getProperty(SorcerConstants.SORCER_WEBSTER_INTERNAL);
         if (val != null && val.length() != 0) {
             isWebsterInt = val.equals("true");
         }
         String exertrun = System.getProperty(SorcerConstants.R_CODEBASE);
         StringBuilder codebase = new StringBuilder();
         if (exertrun!=null && !exertrun.isEmpty()) {
             String[] artifacts = exertrun.split(" ");
             for (String artifact : artifacts) {
                 if (codebase.length() > 0)
                     codebase.append(" ");
                 codebase.append(resolve(coords(artifact)));
             }
             // Add default codebase sos-platform and sos-env
             codebase.append(' ').append(resolve(Artifact.getSosEnv()));
             codebase.append(' ').append(resolve(Artifact.getSosPlatform()));
 
             logger.fine("ServiceRequestor generated codebase: " + codebase.toString());
             if (isWebsterInt)
                 System.setProperty(SorcerConstants.CODEBASE_JARS, codebase.toString());
             else
                 System.setProperty("java.rmi.server.codebase", codebase.toString());
 
             codebaseJars = toArray(codebase.toString());
         }
     }
 
     private static String resolve(ArtifactCoordinates coords) {
         return isWebsterInt
                 ? Resolver.resolveRelative(coords)
                 : Resolver.resolveAbsolute(Sorcer.getWebsterUrl() + "/", coords);
     }
 }
