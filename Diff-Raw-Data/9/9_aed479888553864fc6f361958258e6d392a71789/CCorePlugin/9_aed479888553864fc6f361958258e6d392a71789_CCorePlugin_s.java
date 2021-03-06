 package org.eclipse.cdt.core;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
 
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.eclipse.cdt.core.index.IndexModel;
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.resources.IConsole;
 import org.eclipse.cdt.internal.core.CDescriptorManager;
 import org.eclipse.cdt.internal.core.CPathEntry;
 import org.eclipse.cdt.internal.core.model.CModelManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IPluginDescriptor;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 
 public class CCorePlugin extends Plugin {
 
 	public static final int STATUS_CDTPROJECT_EXISTS = 1;
 	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
 	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;
 
 	public static final String PLUGIN_ID = "org.eclipse.cdt.core";
 
 	public static final String BUILDER_MODEL_ID = PLUGIN_ID + ".CBuildModel";
 	public static final String BINARY_PARSER_SIMPLE_ID = "BinaryParser";
 	public final static String BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + BINARY_PARSER_SIMPLE_ID;
 	public final static String PREF_BINARY_PARSER = "binaryparser";
 	public final static String DEFAULT_BINARY_PARSER_SIMPLE_ID = "ELF";
 	public final static String DEFAULT_BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + DEFAULT_BINARY_PARSER_SIMPLE_ID;
 	public final static String PREF_USE_NEW_PARSER = "useNewParser";
     
     /**
      * Possible configurable option ID.
      * @see #getDefaultOptions
      */
     public static final String TRANSLATION_TASK_PRIORITIES = PLUGIN_ID + ".translation.taskPriorities"; //$NON-NLS-1$
     /**
      * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
      * @see #getDefaultOptions
      */
     public static final String TRANSLATION_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
     /**
      * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
      * @see #getDefaultOptions
      */
     public static final String TRANSLATION_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
     /**
      * Possible configurable option value for TRANSLATION_TASK_PRIORITIES.
      * @see #getDefaultOptions
      */
     public static final String TRANSLATION_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
     /**
      * Possible configurable option ID.
      * @see #getDefaultOptions
      */
     public static final String TRANSLATION_TASK_TAGS = PLUGIN_ID + ".translation.taskTags"; //$NON-NLS-1$
     
     /**
      * Default task tag
      */
     public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
     /**
      * Default task priority
      */
     public static final String DEFAULT_TASK_PRIORITY = TRANSLATION_TASK_PRIORITY_NORMAL;
     /**
      * Possible  configurable option ID.
      * @see #getDefaultOptions
      */
     public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
 
 
 	private static CCorePlugin fgCPlugin;
 	private static ResourceBundle fgResourceBundle;
 
 	private CDescriptorManager fDescriptorManager;
 	private CoreModel fCoreModel;
 	private IndexModel fIndexModel;
 
 	// -------- static methods --------
 
 	static {
 		try {
 			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.internal.core.CCorePluginResources");
 		} catch (MissingResourceException x) {
 			fgResourceBundle = null;
 		}
 	}
 
 	public static String getResourceString(String key) {
 		try {
 			return fgResourceBundle.getString(key);
 		} catch (MissingResourceException e) {
 			return "!" + key + "!";
 		} catch (NullPointerException e) {
 			return "#" + key + "#";
 		}
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	public static String getFormattedString(String key, String arg) {
 		return MessageFormat.format(getResourceString(key), new String[] { arg });
 	}
 
 	public static String getFormattedString(String key, String[] args) {
 		return MessageFormat.format(getResourceString(key), args);
 	}
 
 	public static ResourceBundle getResourceBundle() {
 		return fgResourceBundle;
 	}
 
 	public static CCorePlugin getDefault() {
 		return fgCPlugin;
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
 	}
 
 	public static void log(IStatus status) {
 		((Plugin) getDefault()).getLog().log(status);
 	}
 
 	// ------ CPlugin
 
 	public CCorePlugin(IPluginDescriptor descriptor) {
 		super(descriptor);
 		fgCPlugin = this;
 	}
 
 	/**
 	 * @see Plugin#shutdown
 	 */
 	public void shutdown() throws CoreException {
 		super.shutdown();
 		if (fDescriptorManager != null) {
 			fDescriptorManager.shutdown();
 		}
 		if (fIndexModel != null) {
 			fIndexModel.shutdown();
 		}
 		if (fCoreModel != null) {
 			fCoreModel.shutdown();
 		}
 	}
 
 	/**
 	 * @see Plugin#startup
 	 */
 	public void startup() throws CoreException {
 		super.startup();
 
 		// Fired up the model.
 		fCoreModel = CoreModel.getDefault();
 		fCoreModel.startup();
 
 		// Fired up the indexer. It should delay itself for 10 seconds
 		fIndexModel = IndexModel.getDefault();
 		fIndexModel.startup();
 		
 		//Fired up the new indexer
 		fCoreModel.startIndexing();
 		
 		fDescriptorManager = new CDescriptorManager();
 		fDescriptorManager.startup();
 		
 		// Set the default for using the new parser
 		getPluginPreferences().setDefault(PREF_USE_NEW_PARSER, true);
 	}
     
     
     /**
      * TODO: Add all options here
      * Returns a table of all known configurable options with their default values.
      * These options allow to configure the behaviour of the underlying components.
      * The client may safely use the result as a template that they can modify and
      * then pass to <code>setOptions</code>.
      * 
      * Helper constants have been defined on CCorePlugin for each of the option ID and 
      * their possible constant values.
      * 
      * Note: more options might be added in further releases.
      * <pre>
      * RECOGNIZED OPTIONS:
      * TRANSLATION / Define the Automatic Task Tags
      *    When the tag list is not empty, translation will issue a task marker whenever it encounters
      *    one of the corresponding tags inside any comment in C/C++ source code.
      *    Generated task messages will include the tag, and range until the next line separator or comment ending.
      *    Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
      *    another letter or digit to be recognized ("fooToDo" will not be recognized as a task for tag "ToDo", but "foo#ToDo"
      *    will be detected for either tag "ToDo" or "#ToDo"). Respectively, a tag ending with a letter or digit cannot be followed
      *    by a letter or digit to be recognized ("ToDofoo" will not be recognized as a task for tag "ToDo", but "ToDo:foo" will
      *    be detected either for tag "ToDo" or "ToDo:").
      *     - option id:         "org.eclipse.cdt.core.translation.taskTags"
      *     - possible values:   { "<tag>[,<tag>]*" } where <tag> is a String without any wild-card or leading/trailing spaces 
      *     - default:           ""
      * 
      * TRANSLATION / Define the Automatic Task Priorities
      *    In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
      *    of the task markers issued by the translation.
      *    If the default is specified, the priority of each task marker is "NORMAL".
      *     - option id:         "org.eclipse.cdt.core.transltaion.taskPriorities"
      *     - possible values:   { "<priority>[,<priority>]*" } where <priority> is one of "HIGH", "NORMAL" or "LOW"
      *     - default:           ""
      * 
      * CORE / Specify Default Source Encoding Format
      *    Get the encoding format for translated sources. This setting is read-only, it is equivalent
      *    to 'ResourcesPlugin.getEncoding()'.
      *     - option id:         "org.eclipse.cdt.core.encoding"
      *     - possible values:   { any of the supported encoding names}.
      *     - default:           <platform default>
      * </pre>
      * 
      * @return a mutable map containing the default settings of all known options
      *   (key type: <code>String</code>; value type: <code>String</code>)
      * @see #setOptions
      */
     
     public static HashMap getDefaultOptions()
     {
         HashMap defaultOptions = new HashMap(10);
 
         // see #initializeDefaultPluginPreferences() for changing default settings
         Preferences preferences = getDefault().getPluginPreferences();
         HashSet optionNames = CModelManager.OptionNames;
         
         // get preferences set to their default
         String[] defaultPropertyNames = preferences.defaultPropertyNames();
         for (int i = 0; i < defaultPropertyNames.length; i++){
             String propertyName = defaultPropertyNames[i];
             if (optionNames.contains(propertyName)) {
                 defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
             }
         }       
         // get preferences not set to their default
         String[] propertyNames = preferences.propertyNames();
         for (int i = 0; i < propertyNames.length; i++){
             String propertyName = propertyNames[i];
             if (optionNames.contains(propertyName)) {
                 defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
             }
         }       
         // get encoding through resource plugin
         defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding()); 
         
         return defaultOptions;
     }
 
 
     /**
      * Initializes the default preferences settings for this plug-in.
      * TODO: Add all options here
      */
     protected void initializeDefaultPluginPreferences() 
     {
         Preferences preferences = getPluginPreferences();
         HashSet optionNames = CModelManager.OptionNames;
     
         // Compiler settings
         preferences.setDefault(TRANSLATION_TASK_TAGS, DEFAULT_TASK_TAG); 
         optionNames.add(TRANSLATION_TASK_TAGS);
 
         preferences.setDefault(TRANSLATION_TASK_PRIORITIES, DEFAULT_TASK_PRIORITY); 
         optionNames.add(TRANSLATION_TASK_PRIORITIES);
     }
     
     /**
      * Helper method for returning one option value only. Equivalent to <code>(String)CCorePlugin.getOptions().get(optionName)</code>
      * Note that it may answer <code>null</code> if this option does not exist.
      * <p>
      * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
      * </p>
      * 
      * @param optionName the name of an option
      * @return the String value of a given option
      * @see CCorePlugin#getDefaultOptions
      */
     public static String getOption(String optionName) {
         
         if (CORE_ENCODING.equals(optionName)){
             return ResourcesPlugin.getEncoding();
         }
         if (CModelManager.OptionNames.contains(optionName)){
             Preferences preferences = getDefault().getPluginPreferences();
             return preferences.getString(optionName).trim();
         }
         return null;
     }
     
     /**
      * Returns the table of the current options. Initially, all options have their default values,
      * and this method returns a table that includes all known options.
      * <p>
      * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
      * </p>
      * 
      * @return table of current settings of all options 
      *   (key type: <code>String</code>; value type: <code>String</code>)
      * @see CCorePlugin#getDefaultOptions
      */
     public static HashMap getOptions() {
         
         HashMap options = new HashMap(10);
 
         // see #initializeDefaultPluginPreferences() for changing default settings
         Plugin plugin = getDefault();
         if (plugin != null) {
             Preferences preferences = plugin.getPluginPreferences();
             HashSet optionNames = CModelManager.OptionNames;
             
             // get preferences set to their default
             String[] defaultPropertyNames = preferences.defaultPropertyNames();
             for (int i = 0; i < defaultPropertyNames.length; i++){
                 String propertyName = defaultPropertyNames[i];
                 if (optionNames.contains(propertyName)){
                     options.put(propertyName, preferences.getDefaultString(propertyName));
                 }
             }       
             // get preferences not set to their default
             String[] propertyNames = preferences.propertyNames();
             for (int i = 0; i < propertyNames.length; i++){
                 String propertyName = propertyNames[i];
                 if (optionNames.contains(propertyName)){
                     options.put(propertyName, preferences.getString(propertyName).trim());
                 }
             }       
             // get encoding through resource plugin
             options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
         }
         return options;
     }
 
     /**
      * Sets the current table of options. All and only the options explicitly included in the given table 
      * are remembered; all previous option settings are forgotten, including ones not explicitly
      * mentioned.
      * <p>
      * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
      * </p>
      * 
      * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
      *   or <code>null</code> to reset all options to their default values
      * @see CCorePlugin#getDefaultOptions
      */
     public static void setOptions(HashMap newOptions) {
     
         // see #initializeDefaultPluginPreferences() for changing default settings
         Preferences preferences = getDefault().getPluginPreferences();
 
         if (newOptions == null){
             newOptions = getDefaultOptions();
         }
         Iterator keys = newOptions.keySet().iterator();
         while (keys.hasNext()){
             String key = (String)keys.next();
             if (!CModelManager.OptionNames.contains(key)) continue; // unrecognized option
             if (key.equals(CORE_ENCODING)) continue; // skipped, contributed by resource prefs
             String value = (String)newOptions.get(key);
             preferences.setValue(key, value);
         }
     
         // persist options
         getDefault().savePluginPreferences();
     }    
     
 
 	public IConsole getConsole(String id) {
 		try {
 			IExtensionPoint extension = getDescriptor().getExtensionPoint("CBuildConsole");
 			if (extension != null) {
 				IExtension[] extensions = extension.getExtensions();
 				for (int i = 0; i < extensions.length; i++) {
 					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
 					for (int j = 0; j < configElements.length; j++) {
 						String builderID = configElements[j].getAttribute("builderID");
 						if ((id == null && builderID == null) || (id != null && builderID.equals(id))) {
 							return (IConsole) configElements[j].createExecutableExtension("class");
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 		}
 		return new IConsole() {
 			public void clear() {
 			}
 			public void start(IProject project) {
 			}
 			public ConsoleOutputStream getOutputStream() {
 				return new ConsoleOutputStream();
 			}
 		};
 	}
 
 	public IConsole getConsole() throws CoreException {
 		return getConsole(null);
 	}
 
 	public IBinaryParser getBinaryParser(IProject project) throws CoreException {
 		IBinaryParser parser = null;
 		if (project != null) {
 			try {
 				ICDescriptor cdesc = (ICDescriptor) getCProjectDescription(project);
 				ICExtensionReference[] cextensions = cdesc.get(BINARY_PARSER_UNIQ_ID);
 				if (cextensions.length > 0)
 					parser = (IBinaryParser) cextensions[0].createExtension();
 			} catch (CoreException e) {
 			}
 		}
 		if (parser == null) {
 			parser = getDefaultBinaryParser();
 		}
 		return parser;
 	}
 
 	public IBinaryParser getDefaultBinaryParser() throws CoreException {
 		IBinaryParser parser = null;
 		String id = getPluginPreferences().getDefaultString(PREF_BINARY_PARSER);
 		if (id == null || id.length() == 0) {
 			id = DEFAULT_BINARY_PARSER_UNIQ_ID;
 		}
 		IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(BINARY_PARSER_SIMPLE_ID);
 		IExtension extension = extensionPoint.getExtension(id);
 		if (extension != null) {
 			IConfigurationElement element[] = extension.getConfigurationElements();
 			for (int i = 0; i < element.length; i++) {
 				if (element[i].getName().equalsIgnoreCase("cextension")) {
 					parser = (IBinaryParser) element[i].createExecutableExtension("run");
 					break;
 				}
 			}
 		} else {
 			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, "No Binary Format", null);
 			throw new CoreException(s);
 		}
 		return parser;
 	}
 
 	public CoreModel getCoreModel() {
 		return fCoreModel;
 	}
 
 	public IndexModel getIndexModel() {
 		return fIndexModel;
 	}
 
 	public ICDescriptor getCProjectDescription(IProject project) throws CoreException {
 		return fDescriptorManager.getDescriptor(project);
 	}
 
 	public void mapCProjectOwner(IProject project, String id, boolean override) throws CoreException {
 		if (!override) {
 			fDescriptorManager.configure(project, id);
 		} else {
 			fDescriptorManager.convert(project, id);
 		}
 	}
 
 	/**
 	 * Creates a C project resource given the project handle and description.
 	 *
 	 * @param description the project description to create a project resource for
 	 * @param projectHandle the project handle to create a project resource for
 	 * @param monitor the progress monitor to show visual progress with
 	 * @param projectID required for mapping the project to an owner
 	 *
 	 * @exception CoreException if the operation fails
 	 * @exception OperationCanceledException if the operation is canceled
 	 */
 	public IProject createCProject(
 		IProjectDescription description,
 		IProject projectHandle,
 		IProgressMonitor monitor,
 		String projectID)
 		throws CoreException, OperationCanceledException {
 		try {
 			if (monitor == null) {
 				monitor = new NullProgressMonitor();
 			}
 			monitor.beginTask("Creating C Project", 3); //$NON-NLS-1$
 			if (!projectHandle.exists()) {
 				projectHandle.create(description, monitor);
 			}
 
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 
 			// Open first.
 			projectHandle.open(monitor);
 
 			// Add C Nature ... does not add duplicates
			CProjectNature.addCNature(projectHandle, new SubProgressMonitor(monitor, 1));
 			mapCProjectOwner(projectHandle, projectID, false);
 		} finally {
 			//monitor.done();
 		}
 		return projectHandle;
 	}
 
 	/**
 	 * Method convertProjectFromCtoCC converts
 	 * a C Project to a C++ Project
 	 * The newProject MUST, not be null, already have a C Nature 
 	 * && must NOT already have a C++ Nature
 	 * 
 	 * @param projectHandle
 	 * @param monitor
 	 * @throws CoreException
 	 */
 
 	public void convertProjectFromCtoCC(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
 		if ((projectHandle != null)
 			&& projectHandle.hasNature(CCProjectNature.C_NATURE_ID)
 			&& !projectHandle.hasNature(CCProjectNature.CC_NATURE_ID)) {
 			// Add C++ Nature ... does not add duplicates        
 			CCProjectNature.addCCNature(projectHandle, monitor);
 		}
 	}
 
 	/**
 	 * Method addDefaultCBuilder adds the default C make builder
 	 * @param projectHandle
 	 * @param monitor
 	 * @exception CoreException
 	 */
 	public void addDefaultCBuilder(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
 		// Set the Default C Builder.
 		CProjectNature.addCBuildSpec(projectHandle, monitor);
 	}
 
 	/**
 	 * Method to convert a project to a C nature 
 	 * & default make builder (Will always add a default builder)
 	 * All checks should have been done externally
 	 * (as in the Conversion Wizards). 
 	 * This method blindly does the conversion.
 	 * 
 	 * @param project
 	 * @param String targetNature
 	 * @param monitor
 	 * @param projectID
 	 * @exception CoreException
 	 */
 
 	public void convertProjectToC(IProject projectHandle, IProgressMonitor monitor, String projectID) throws CoreException {
 		this.convertProjectToC(projectHandle, monitor, projectID, true);
 
 	}
 	/**
 	 * Method to convert a project to a C nature 
 	 * & default make builder (if indicated)
 	 * All checks should have been done externally
 	 * (as in the Conversion Wizards). 
 	 * This method blindly does the conversion.
 	 * 
 	 * @param project
 	 * @param String targetNature
 	 * @param monitor
 	 * @param projectID
 	 * @param addMakeBuilder
 	 * @exception CoreException
 	 */
 
 	public void convertProjectToC(IProject projectHandle, IProgressMonitor monitor, String projectID, boolean addMakeBuilder)
 		throws CoreException {
 		if ((projectHandle == null) || (monitor == null) || (projectID == null)) {
 			return;
 		}
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
 		description.setLocation(projectHandle.getFullPath());
 		createCProject(description, projectHandle, monitor, projectID);
 		if (addMakeBuilder) {
 			addDefaultCBuilder(projectHandle, monitor);
 		}
 	}
 	/**
 	 * Method to convert a project to a C++ nature 
 	 * & default make builder(if indicated), if it does not have one already
 	 * 
 	 * @param project
 	 * @param String targetNature
 	 * @param monitor
 	 * @param projectID
 	 * @param addMakeBuilder
 	 * @exception CoreException
 	 */
 
 	public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID, boolean addMakeBuilder)
 		throws CoreException {
 		if ((projectHandle == null) || (monitor == null) || (projectID == null)) {
 			return;
 		}
 		createCProject(projectHandle.getDescription(), projectHandle, monitor, projectID);
 		// now add C++ nature
 		convertProjectFromCtoCC(projectHandle, monitor);
 		if (addMakeBuilder) {
 			addDefaultCBuilder(projectHandle, monitor);
 		}
 	}
 	/**
 	* Method to convert a project to a C++ nature 
 	* & default make builder,
 	* Note: Always adds the default Make builder
 	* 
 	* @param project
 	* @param String targetNature
 	* @param monitor
 	* @param projectID
 	* @exception CoreException
 	*/
 
 	public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID) throws CoreException {
 		this.convertProjectToCC(projectHandle, monitor, projectID, true);
 	}
 
 	// Extract the builder from the .cdtproject.  
 	//	public ICBuilder[] getBuilders(IProject project) throws CoreException {
 	//		ICExtension extensions[] = fDescriptorManager.createExtensions(BUILDER_MODEL_ID, project);
 	//		ICBuilder builders[] = new ICBuilder[extensions.length];
 	//		System.arraycopy(extensions, 0, builders, 0, extensions.length);
 	//		return builders;
 	//	}
 
 	public IProcessList getProcessList() {
 		IExtensionPoint extension = getDescriptor().getExtensionPoint("ProcessList");
 		if (extension != null) {
 			IExtension[] extensions = extension.getExtensions();
 			IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
 			if (configElements.length != 0) {
 				try {
 					return (IProcessList) configElements[0].createExecutableExtension("class");
 				} catch (CoreException e) {
 				}
 			}
 		}
 		return null;
 	}
 
 	// Preference to turn on/off the new parser
 	public void setUseNewParser(boolean useNewParser) {
 		getPluginPreferences().setValue(PREF_USE_NEW_PARSER, useNewParser);
 		savePluginPreferences();
 	}
 
 	public boolean useNewParser() {
 		return getPluginPreferences().getBoolean(PREF_USE_NEW_PARSER);
 	}
 
 	/**
 	 * @param path
 	 * @return
 	 */
 	public static ICPathEntry newProjectEntry(IPath path) {
 		return new CPathEntry(ICPathEntry.CDT_PROJECT, path, CPathEntry.NO_EXCLUSION_PATTERNS, null, null, null);
 	}
 
 	/**
 	 * @param path
 	 * @param sourceAttachmentPath
 	 * @param sourceAttachmentRootPath
 	 * @return
 	 */
 	public static ICPathEntry newLibraryEntry(IPath path, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath, IPath sourceAttachmentRootPrefixMapping) {
 		return new CPathEntry(
 			ICPathEntry.CDT_LIBRARY,
 			path,
 			CPathEntry.NO_EXCLUSION_PATTERNS,
 			sourceAttachmentPath,
 			sourceAttachmentRootPath, 
 			sourceAttachmentRootPrefixMapping);
 	}
 
 	/**
 	 * @param path
 	 * @param exclusionPatterns
 	 * @param outputLocation
 	 * @return
 	 */
 	public static ICPathEntry newSourceEntry(IPath path, IPath[] exclusionPatterns) {
 		return new CPathEntry(ICPathEntry.CDT_SOURCE, path, exclusionPatterns, null, null, null);  
 	}
 
 	/**
 	 * @param path
 	 * @param sourceAttachmentPath
 	 * @param sourceAttachmentRootPath
 	 * @return
 	 */
 	public static ICPathEntry newVariableEntry(IPath path, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath) {
 		return new CPathEntry(ICPathEntry.CDT_VARIABLE, path, null, sourceAttachmentPath, sourceAttachmentRootPath, null);
 	}
 
 	/**
 	 * @param path
 	 * @param exclusionPatterns
 	 * @return
 	 */
 	public static ICPathEntry newIncludeEntry(IPath path, IPath[] exclusionPatterns) {
 		return new CPathEntry(ICPathEntry.CDT_INCLUDE, path, exclusionPatterns, null, null, null);  
 	}
 }
