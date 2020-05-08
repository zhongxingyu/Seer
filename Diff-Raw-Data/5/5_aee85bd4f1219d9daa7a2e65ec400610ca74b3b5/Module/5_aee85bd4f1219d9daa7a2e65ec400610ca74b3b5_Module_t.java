 package fedora.server;
 
 import java.util.Map;
 import java.util.Iterator;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 
 /**
  * The base class for Fedora server modules.
  *
  * A server module is a parameterized component of the system that can
  * be configured via a &lt;module&gt; element in conf/fedora.fcfc.  
  * The schema for this element is in fedora-config.xsd.
  *
  * @author cwilper@cs.cornell.edu
  */
 public abstract class Module {
 
     /** a reference to the provided params for this module (see constructor) */
     private Map m_moduleParameters;
 
     /**
      * Creates and initializes the Module.
      *
      * When the server is starting up, this is invoked as part of the
      * initialization process.
      *
      * @param moduleParameters A pre-loaded Map of name-value pairs comprising
      *                         the intended configuration of this Module.
      * @throws ModuleInitializationException If initilization values are
      *                                       invalid or initialization fails
      *                                       for some other reason.
      */
     public Module(Map moduleParameters)
             throws ModuleInitializationException {
         m_moduleParameters=moduleParameters;
         initModule();
     }
 
     /**
      * Initializes the Module based on configuration parameters.
      *
      * @throws ModuleInitializationException If initialization values are
      *                                       invalid or initialization fails
      *                                       for some other reason.
      */
     public abstract void initModule()
            throws ModuleInitializationException;
 
     /**
      * Gets the value of a named configuration parameter.
      *
      * @param name The parameter name.
      * @returns String The value, null if undefined.
      */
     public final String getParameter(String name) {
         return (String) m_moduleParameters.get(name);
     }
 
     /**
      * Gets an Iterator over the names of parameters for this Module.
      *
      * @returns Iterator The names
      */
     public final Iterator parameterNames() {
         return m_moduleParameters.keySet().iterator();
     }
 
     /**
      * Frees system resources allocated by this Module.
      *
      * @throws ModuleShutdownException If there is a problem freeing
      *                                 system resources.  Note that if there
      *                                 is a problem, it won't end up aborting
      *                                 the shutdown process.  Therefore, this
      *                                 method should do everything possible
      *                                 to recover from exceptional situations.
      */
    public abstract void shutdownModule() 
             throws ModuleShutdownException;
 
 }
