 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.variables.IStringVariableManager;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.sourcelookup.ISourceContainer;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.internal.environment.LocalEnvironment;
 import org.eclipse.dltk.internal.launching.CompositeId;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.DefaultEntryResolver;
 import org.eclipse.dltk.internal.launching.DefaultProjectBuildpathEntry;
 import org.eclipse.dltk.internal.launching.InterpreterContainerInitializer;
 import org.eclipse.dltk.internal.launching.InterpreterDefinitionsContainer;
 import org.eclipse.dltk.internal.launching.ListenerList;
 import org.eclipse.dltk.internal.launching.RuntimeBuildpathEntry;
 import org.eclipse.dltk.internal.launching.RuntimeBuildpathEntryResolver;
 import org.eclipse.dltk.internal.launching.RuntimeBuildpathProvider;
 import org.eclipse.dltk.internal.launching.ScriptSourceLookupUtil;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.ibm.icu.text.MessageFormat;
 
 /**
  * The central access point for launching support. This class manages the
  * registered interpreters types contributed through the
  * <code>"org.eclipse.dltk.launching.interpreterType"</code> extension point. As
  * well, this class provides interpreter install change notification, and
  * computes buildpaths and source lookup paths for launch configurations.
  * <p>
  * This class provides static methods only; it is not intended to be
  * instantiated or subclassed by clients.
  * </p>
  */
 public final class ScriptRuntime {
 
 	/**
 	 * Classpath container used for a project's InterpreterEnvironment (value
 	 * <code>"org.eclipse.dltk.launching.InterpreterEnvironment_CONTAINER"</code>
 	 * ). A container is resolved in the context of a specific Script project,
 	 * to one or more system libraries contained in a InterpreterEnvironment.
 	 * The container can have zero or two path segments following the container
 	 * name. When no segments follow the container name, the workspace default
 	 * InterpreterEnvironment is used to build a project. Otherwise the segments
 	 * identify a specific InterpreterEnvironment used to build a project:
 	 * <ol>
 	 * <li>Interpreter Install Type Identifier - identifies the type of
 	 * InterpreterEnvironment used to build the project. For example, the
 	 * standard Interpreter.</li>
 	 * <li>Interpreter Install Name - a user defined name that identifies that a
 	 * specific Interpreter of the above kind. For example,
 	 * <code>IBM 1.3.1</code>. This information is shared in a projects
 	 * buildpath file, so teams must agree on InterpreterEnvironment naming
 	 * conventions.</li>
 	 * </ol>
 	 * <p>
 	 * The path may also identify an execution environment as follows:
 	 * <ol>
 	 * <li>Execution environment extension point name (value
 	 * <code>executionEnvironments</code>)</li>
 	 * <li>Identifier of a contributed execution environment</li>
 	 * </ol>
 	 * </p>
 	 * 
 	 */
 	public static final String INTERPRETER_CONTAINER = DLTKLaunchingPlugin
 			.getUniqueIdentifier()
 			+ ".INTERPRETER_CONTAINER"; //$NON-NLS-1$
 
 	/**
 	 * Simple identifier constant (value
 	 * <code>"runtimeBuildpathEntryResolvers"</code>) for the runtime buildpath
 	 * entry resolvers extension point.
 	 * 
 	 * 
 	 */
 	public static final String EXTENSION_POINT_RUNTIME_BUILDPATH_ENTRY_RESOLVERS = "runtimeBuildpathEntryResolvers"; //$NON-NLS-1$	
 
 	/**
 	 * Simple identifier constant (value <code>"buildpathProviders"</code>) for
 	 * the runtime buildpath providers extension point.
 	 * 
 	 * 
 	 */
 	public static final String EXTENSION_POINT_RUNTIME_BUILDPATH_PROVIDERS = "buildpathProviders"; //$NON-NLS-1$		
 
 	/**
 	 * Simple identifier constant (value <code>"interpreterInstalls"</code>) for
 	 * the interpreters installs extension point.
 	 * 
 	 * 
 	 */
 	public static final String EXTENSION_POINT_INTERPRETER_INSTALLS = "interpreterInstalls"; //$NON-NLS-1$		
 
 	/**
 	 * A status code indicating that a interpreter could not be resolved for a
 	 * project. When a interpreter cannot be resolved for a project by this
 	 * plug-in's container initializer, an exception is thrown with this status
 	 * code. A status handler may be registered for this status code. The
 	 * <code>source</code> object provided to the status handler is the script
 	 * project for which the path could not be resolved. The status handler must
 	 * return an <code>IInterpreterInstall</code> or <code>null</code>. The
 	 * container resolver will re-set the project's buildpath if required.
 	 * 
 	 * 
 	 */
 	public static final int ERR_UNABLE_TO_RESOLVE_INTERPRETER = 160;
 
 	/**
 	 * Preference key for launch/connect timeout. Interpreter Runners should
 	 * honor this timeout value when attempting to launch and connect to a
 	 * debugger. The value is an int, indicating a number of milliseconds.
 	 * 
 	 * 
 	 */
 	public static final String PREF_CONNECT_TIMEOUT = DLTKLaunchingPlugin
 			.getUniqueIdentifier()
 			+ ".PREF_CONNECT_TIMEOUT"; //$NON-NLS-1$
 
 	/**
 	 * Preference key for the String of XML that defines all installed
 	 * Interpreters.
 	 * 
 	 * 
 	 */
 	public static final String PREF_INTERPRETER_XML = DLTKLaunchingPlugin
 			.getUniqueIdentifier()
 			+ ".PREF_INTERPRETER_XML"; //$NON-NLS-1$
 
 	/**
 	 * Default launch/connect timeout (ms).
 	 * 
 	 * 
 	 */
 	public static final int DEF_CONNECT_TIMEOUT = 20000;
 
 	/**
 	 * Attribute key for a buildpath attribute referencing a list of shared
 	 * libraries that should appear on the
 	 * <code>-Dinterpreter.library.path</code> system property.
 	 * <p>
 	 * The factory methods <code>newLibraryPathsAttribute(String[])</code> and
 	 * <code>getLibraryPaths(IBuildpathAttribute)</code> should be used to
 	 * encode and decode the attribute value.
 	 * </p>
 	 * <p>
 	 * Each string is used to create an <code>IPath</code> using the constructor
 	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code>
 	 * 's. Variable substitution is performed on the string prior to
 	 * constructing a path from the string. If the resulting <code>IPath</code>
 	 * is a relative path, it is interpreted as relative to the workspace
 	 * location. If the path is absolute, it is interpreted as an absolute path
 	 * in the local file system.
 	 * </p>
 	 * 
 	 */
 	public static final String BUILDPATH_ATTR_LIBRARY_PATH_ENTRY = DLTKLaunchingPlugin
 			.getUniqueIdentifier()
 			+ ".CLASSPATH_ATTR_LIBRARY_PATH_ENTRY"; //$NON-NLS-1$
 
 	// lock for interpreter initialization
 	private static Object fgInterpreterLock = new Object();
 	private static boolean fgInitializingInterpreters = false;
 
 	private static IInterpreterInstallType[] fgInterpreterTypes = null;
 
 	public static class DefaultInterpreterEntry {
 		private String nature;
 		private String environment;
 
 		public DefaultInterpreterEntry(String nature, String environment) {
 			this.nature = nature;
 			this.environment = environment;
 		}
 
 		public String getNature() {
 			return nature;
 		}
 
 		public String getEnvironment() {
 			return environment;
 		}
 
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((environment == null) ? 0 : environment.hashCode());
 			result = prime * result
 					+ ((nature == null) ? 0 : nature.hashCode());
 			return result;
 		}
 
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			DefaultInterpreterEntry other = (DefaultInterpreterEntry) obj;
 			if (environment == null) {
 				if (other.environment != null)
 					return false;
 			} else if (!environment.equals(other.environment))
 				return false;
 			if (nature == null) {
 				if (other.nature != null)
 					return false;
 			} else if (!nature.equals(other.nature))
 				return false;
 			return true;
 		}
 	}
 
 	/**
 	 * Contain association of default interpreter entries to interprter
 	 * identifiers.
 	 */
 	private static Map fgDefaultInterpreterId = new HashMap();
 
 	/**
 	 * Contain DefaultInterpreterEntry entry assocications
 	 */
 	private static Map fgDefaultInterpreterConnectorId = new HashMap();
 
 	/**
 	 * Resolvers keyed by variable name, container id, and runtime buildpath
 	 * entry id.
 	 */
 	private static Map fgContainerResolvers = null;
 	private static Map fgRuntimeBuildpathEntryResolvers = null;
 
 	/**
 	 * Path providers keyed by id
 	 */
 	private static Map fgPathProviders = null;
 
 	/**
 	 * Default buildpath and source path providers.
 	 */
 	private static IRuntimeBuildpathProvider fgDefaultBuildpathProvider = new StandardBuildpathProvider();
 	private static IRuntimeBuildpathProvider fgDefaultSourcepathProvider = new StandardSourcepathProvider();
 
 	/**
 	 * Interpreter change listeners
 	 */
 	private static ListenerList fgInterpreterListeners = new ListenerList(5);
 
 	/**
 	 * Cache of already resolved projects in container entries. Used to avoid
 	 * cycles in project dependencies when resolving buildpath container
 	 * entries. Counters used to know when entering/exiting to clear cache
 	 */
 	private static ThreadLocal fgProjects = new ThreadLocal(); // Lists
 	private static ThreadLocal fgEntryCount = new ThreadLocal(); // Integers
 
 	/**
 	 * Set of IDs of Interpreters contributed via InterpreterInstalls extension
 	 * point.
 	 */
 	private static Set fgContributedInterpreters = new HashSet();
 
 	/**
 	 * This class contains only static methods, and is not intended to be
 	 * instantiated.
 	 */
 	private ScriptRuntime() {
 	}
 
 	/**
 	 * Initializes interpreter type extensions.
 	 */
 	private static void initializeInterpreterTypeExtensions() {
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKLaunchingPlugin.PLUGIN_ID,
 						"interpreterInstallTypes"); //$NON-NLS-1$
 		IConfigurationElement[] configs = extensionPoint
 				.getConfigurationElements();
 
 		MultiStatus status = new MultiStatus(DLTKLaunchingPlugin
 				.getUniqueIdentifier(), IStatus.OK,
 				LaunchingMessages.ScriptRuntime_exceptionsOccurred, null);
 		fgInterpreterTypes = new IInterpreterInstallType[configs.length];
 
 		for (int i = 0; i < configs.length; i++) {
 			try {
 				IInterpreterInstallType installType = (IInterpreterInstallType) configs[i]
 						.createExecutableExtension("class"); //$NON-NLS-1$
 				fgInterpreterTypes[i] = installType;
 			} catch (CoreException e) {
 				status.add(e.getStatus());
 			}
 		}
 		if (!status.isOK()) {
 			// only happens on a CoreException
 			DLTKLaunchingPlugin.log(status);
 			// cleanup null entries in fgInterpreterTypes
 			List temp = new ArrayList(fgInterpreterTypes.length);
 			for (int i = 0; i < fgInterpreterTypes.length; i++) {
 				if (fgInterpreterTypes[i] != null) {
 					temp.add(fgInterpreterTypes[i]);
 				}
 				fgInterpreterTypes = new IInterpreterInstallType[temp.size()];
 				fgInterpreterTypes = (IInterpreterInstallType[]) temp
 						.toArray(fgInterpreterTypes);
 			}
 		}
 	}
 
 	private static String getNatureFromProject(IScriptProject project) {
 		try {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(project);
 			if (toolkit != null)
 				return toolkit.getNatureId();
 		} catch (Exception e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 		return null;
 	}
 
 	private static String getEnvironmentFromProject(IScriptProject project) {
 		IEnvironment environment = EnvironmentManager.getEnvironment(project);
 		if (environment == null) {
 			return null;
 		}
 		return environment.getId();
 	}
 
 	/**
 	 * Returns the interpreter assigned to build the given script project. The
 	 * project must exist. The interpreter assigned to a project is determined
 	 * from its build path.
 	 * 
 	 * @param project
 	 *            the project to retrieve the interpreter from
 	 * @return the interpreter instance that is assigned to build the given
 	 *         script project Returns <code>null</code> if no interp. is
 	 *         referenced on the project's build path.
 	 * @throws CoreException
 	 *             if unable to determine the project's interpreter install
 	 */
 	public static IInterpreterInstall getInterpreterInstall(
 			IScriptProject project) throws CoreException {
 		// check the buildpath
 		IInterpreterInstall interpreter = null;
 
 		IBuildpathEntry[] buildpath = project.getRawBuildpath();
 
 		for (int i = 0; i < buildpath.length; i++) {
 			IBuildpathEntry entry = buildpath[i];
 			switch (entry.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				IRuntimeBuildpathEntryResolver resolver = getContainerResolver(entry
 						.getPath().segment(0));
 				if (resolver != null) {
 					interpreter = resolver.resolveInterpreterInstall(
 							getNatureFromProject(project),
 							getEnvironmentFromProject(project), entry);
 				}
 				break;
 			}
 
 			if (interpreter != null) {
 				return interpreter;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the interpreter install type with the given unique id.
 	 * 
 	 * @param id
 	 *            the interpreter install type unique id
 	 * @return The interpreter install type for the given id, or
 	 *         <code>null</code> if no interpreter install type with the given
 	 *         id is registered.
 	 */
 	public static IInterpreterInstallType getInterpreterInstallType(String id) {
 		IInterpreterInstallType[] installTypes = getInterpreterInstallTypes();
 		if (installTypes == null) {
 			return null;
 		}
 
 		for (int i = 0; i < installTypes.length; i++) {
 			IInterpreterInstallType type = installTypes[i];
 			if (type != null && type.getId().equals(id)) {
 				return type;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Sets a Interpreter as the system-wide default Interpreter, and notifies
 	 * registered Interpreter install change listeners of the change.
 	 * 
 	 * @param interpreter
 	 *            The Interpreter to make the default. May be <code>null</code>
 	 *            to clear the default.
 	 * @param monitor
 	 *            progress monitor or <code>null</code>
 	 */
 	public static void setDefaultInterpreterInstall(
 			IInterpreterInstall interpreter, IProgressMonitor monitor)
 			throws CoreException {
 		setDefaultInterpreterInstall(interpreter, monitor, true);
 	}
 
 	/**
 	 * Sets a Interpreter as the nature default Interpreter, and notifies
 	 * registered Interpreter install change listeners of the change.
 	 * 
 	 * @param interpreter
 	 *            The Interpreter to make the default. May be <code>null</code>
 	 *            to clear the default.
 	 * @param monitor
 	 *            progress monitor or <code>null</code>
 	 * @param savePreference
 	 *            If <code>true</code>, update workbench preferences to reflect
 	 *            the new default Interpreter.
 	 * 
 	 */
 	public static void setDefaultInterpreterInstall(
 			IInterpreterInstall interpreter, IProgressMonitor monitor,
 			boolean savePreference) throws CoreException {
 		final IEnvironment env = interpreter.getEnvironment();
 		if (env == null) {
 			return;
 		}
 		final String environmentId = env.getId();
 		IInterpreterInstall previous = null;
 		String nature = interpreter.getInterpreterInstallType().getNatureId();
 
 		DefaultInterpreterEntry defaultInterpreterID = new DefaultInterpreterEntry(
 				nature, environmentId);
 		if (fgDefaultInterpreterId.get(defaultInterpreterID) != null) {
 			previous = getInterpreterFromCompositeId((String) fgDefaultInterpreterId
 					.get(defaultInterpreterID));
 		}
 		fgDefaultInterpreterId.put(defaultInterpreterID,
 				getCompositeIdFromInterpreter(interpreter));
 		if (savePreference) {
 			saveInterpreterConfiguration();
 		}
 		IInterpreterInstall current = null;
 		if (fgDefaultInterpreterId.get(defaultInterpreterID) != null) {
 			current = getInterpreterFromCompositeId((String) fgDefaultInterpreterId
 					.get(defaultInterpreterID));
 		}
 		if (previous != current) {
 			notifyDefaultInterpreterChanged(previous, current);
 		}
 	}
 
 	/**
 	 * Return the default Interpreter set with
 	 * <code>setDefaultInterpreter()</code>.
 	 * 
 	 * @return Returns the default Interpreter.
 	 */
 	public static IInterpreterInstall getDefaultInterpreterInstall(
 			DefaultInterpreterEntry entry) {
 		IInterpreterInstall install = getInterpreterFromCompositeId(getDefaultInterpreterId(entry));
 		if (install != null && install.getInstallLocation().exists()) {
 			return install;
 		}
 		// if the default interp. goes missing, re-detect
 		if (install != null) {
 			// install.getInterpreterInstallType().disposeInterpreterInstall(
 			// install.getId());
 		}
 		synchronized (fgInterpreterLock) {
 			// fgDefaultInterpreterId = null;
 			fgDefaultInterpreterId.clear();
 			fgInterpreterTypes = null;
 			initializeInterpreters();
 		}
 
 		return getInterpreterFromCompositeId(getDefaultInterpreterId(entry));
 	}
 
 	/**
 	 * Returns the list of registered interpreter types. Interpreter types are
 	 * registered via <code>"org.eclipse.dltk.launching.interpreterTypes"</code>
 	 * extension point. Returns an empty list if there are no registered
 	 * interpreter types.
 	 * 
 	 * @return the list of registered Interpreter types
 	 */
 	public static IInterpreterInstallType[] getInterpreterInstallTypes() {
 		initializeInterpreters();
 		return fgInterpreterTypes;
 	}
 
 	/**
 	 * Returns FILTERED list of registered interpreter types. Interpreter types
 	 * are registered via
 	 * <code>"org.eclipse.dltk.launching.interpreterTypes"</code> extension
 	 * point. Returns an empty list if there are no registered interpreter
 	 * types.
 	 * 
 	 * @return the list of registered Interpreter types
 	 */
 	public static IInterpreterInstallType[] getInterpreterInstallTypes(
 			String nature) {
 		initializeInterpreters();
 
 		List res = new ArrayList();
 		for (int i = 0; i < fgInterpreterTypes.length; i++) {
 			IInterpreterInstallType t = fgInterpreterTypes[i];
 			if (t.getNatureId().equals(nature))
 				res.add(t);
 		}
 		return (IInterpreterInstallType[]) res
 				.toArray(new IInterpreterInstallType[res.size()]);
 	}
 
 	public static DefaultInterpreterEntry[] getDefaultInterpreterIDs() {
 		Set set = fgDefaultInterpreterId.keySet();
 		return (DefaultInterpreterEntry[]) set
 				.toArray(new DefaultInterpreterEntry[set.size()]);
 	}
 
 	private static String getDefaultInterpreterId(DefaultInterpreterEntry entry) {
 		initializeInterpreters();
 		return (String) fgDefaultInterpreterId.get(entry);
 	}
 
 	private static String getDefaultInterpreterConnectorId(
 			DefaultInterpreterEntry entry) {
 		initializeInterpreters();
 		return (String) fgDefaultInterpreterConnectorId.get(entry);
 	}
 
 	/**
 	 * Returns a String that uniquely identifies the specified Interpreter
 	 * across all Interpreter types.
 	 * 
 	 * @param Interpreter
 	 *            the instance of IInterpreterInstallType to be identified
 	 * 
 	 * 
 	 */
 	public static String getCompositeIdFromInterpreter(
 			IInterpreterInstall Interpreter) {
 		if (Interpreter == null) {
 			return null;
 		}
 		IInterpreterInstallType InterpreterType = Interpreter
 				.getInterpreterInstallType();
 		String typeID = InterpreterType.getId();
 		CompositeId id = new CompositeId(new String[] { typeID,
 				Interpreter.getId() });
 		return id.toString();
 	}
 
 	/**
 	 * Return the Interpreter corresponding to the specified composite Id. The
 	 * id uniquely identifies a Interpreter across all Interpreter types.
 	 * 
 	 * @param idString
 	 *            the composite id that specifies an instance of
 	 *            IInterpreterInstall
 	 * 
 	 * 
 	 */
 	public static IInterpreterInstall getInterpreterFromCompositeId(
 			String idString) {
 		if (idString == null || idString.length() == 0) {
 			return null;
 		}
 		CompositeId id = CompositeId.fromString(idString);
 		if (id.getPartCount() == 2) {
 			IInterpreterInstallType InterpreterType = getInterpreterInstallType(id
 					.get(0));
 			if (InterpreterType != null) {
 				return InterpreterType.findInterpreterInstall(id.get(1));
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the <code>IScriptProject</code> referenced in the specified
 	 * configuration or <code>null</code> if none.
 	 * 
 	 * @exception CoreException
 	 *                if the referenced Script project does not exist
 	 * 
 	 */
 	public static IScriptProject getScriptProject(
 			ILaunchConfiguration configuration) throws CoreException {
 		String projectName = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 				(String) null);
 		if ((projectName == null) || (projectName.trim().length() < 1)) {
 			return null;
 		}
 		IScriptProject scriptProject = getScriptModel().getScriptProject(
 				projectName);
 		if (scriptProject != null && scriptProject.getProject().exists()
 				&& !scriptProject.getProject().isOpen()) {
 			abort(MessageFormat.format(LaunchingMessages.ScriptRuntime_28,
 					new String[] { configuration.getName(), projectName }),
 					ScriptLaunchConfigurationConstants.ERR_PROJECT_CLOSED, null);
 		}
 		if ((scriptProject == null) || !scriptProject.exists()) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.ScriptRuntime_Launch_configuration__0__references_non_existing_project__1___1,
 									new String[] { configuration.getName(),
 											projectName }),
 					ScriptLaunchConfigurationConstants.ERR_NOT_A_SCRIPT_PROJECT,
 					null);
 		}
 		return scriptProject;
 	}
 
 	/**
 	 * Convenience method to get the script model.
 	 */
 	private static IScriptModel getScriptModel() {
 		return DLTKCore.create(ResourcesPlugin.getWorkspace().getRoot());
 	}
 
 	/**
 	 * Returns the Interpreter install for the given launch configuration. The
 	 * Interpreter install is determined in the following prioritized way:
 	 * <ol>
 	 * <li>The Interpreter install is explicitly specified on the launch
 	 * configuration via the <code>ATTR_CONTAINER_PATH</code> attribute.</li>
 	 * <li>If no explicit Interpreter install is specified, the Interpreter
 	 * install associated with the launch configuration's project is returned.</li>
 	 * <li>If no project is specified, or the project does not specify a custom
 	 * Interpreter install, the workspace default Interpreter install is
 	 * returned.</li>
 	 * </ol>
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return interpreter install
 	 * @exception CoreException
 	 *                if unable to compute a Interpreter install
 	 * 
 	 */
 	public static IInterpreterInstall computeInterpreterInstall(
 			ILaunchConfiguration configuration) throws CoreException {
 		// get field ATTR_NATURE from launch configuration
 		String nature = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SCRIPT_NATURE,
 				(String) null);
 		String containerPath = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_CONTAINER_PATH,
 				(String) null);
 		IScriptProject proj = getScriptProject(configuration);
 		String environment = getEnvironmentFromProject(proj);
 		if (containerPath == null) {
 			if (proj != null) {
 				IInterpreterInstall install = getInterpreterInstall(proj);
 				if (install != null) {
 					return install;
 				}
 				if (nature == null) {
 					IDLTKLanguageToolkit tk = DLTKLanguageManager
 							.getLanguageToolkit(proj);
 					nature = tk.getNatureId();
 				}
 			}
 		} else {
 			IPath interpreterPath = Path.fromPortableString(containerPath);
 			IBuildpathEntry entry = DLTKCore.newContainerEntry(interpreterPath);
 			IRuntimeBuildpathEntryResolver2 resolver = getContainerResolver(interpreterPath
 					.segment(0));
 			if (resolver != null) {
 				return resolver.resolveInterpreterInstall(nature, environment,
 						entry);
 			} else {
 				resolver = getContainerResolver(interpreterPath.segment(0));
 				if (resolver != null) {
 					return resolver.resolveInterpreterInstall(nature,
 							environment, entry);
 				}
 			}
 		}
 
 		if (nature == null) {
 			abort(
 					LaunchingMessages.ScriptRuntime_notDefaultInterpreter,
 					ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
 					null);
 		}
 		// OR extract project nature from it's name (what about several
 		// natures?)
 		DefaultInterpreterEntry entry = new DefaultInterpreterEntry(nature,
 				environment);
 		IInterpreterInstall res = getDefaultInterpreterInstall(entry);
 		if (res == null) {
 			abort(
 					LaunchingMessages.ScriptRuntime_notDefaultInterpreter,
 					ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
 					null);
 		}
 		return res;
 	}
 
 	/**
 	 * Throws a core exception with an internal error status.
 	 * 
 	 * @param message
 	 *            the status message
 	 * @param exception
 	 *            lower level exception associated with the error, or
 	 *            <code>null</code> if none
 	 */
 	private static void abort(String message, Throwable exception)
 			throws CoreException {
 		abort(message, ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
 				exception);
 	}
 
 	/**
 	 * Throws a core exception with an internal error status.
 	 * 
 	 * @param message
 	 *            the status message
 	 * @param code
 	 *            status code
 	 * @param exception
 	 *            lower level exception associated with the
 	 * 
 	 *            error, or <code>null</code> if none
 	 */
 	private static void abort(String message, int code, Throwable exception)
 			throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR,
 				DLTKLaunchingPlugin.PLUGIN_ID, code, message, exception));
 	}
 
 	/**
 	 * Saves the Interpreter configuration information to the preferences. This
 	 * includes the following information:
 	 * <ul>
 	 * <li>The list of all defined IInterpreterInstall instances.</li>
 	 * <li>The default Interpreter</li>
 	 * <ul>
 	 * This state will be read again upon first access to Interpreter
 	 * configuration information.
 	 */
 	public static void saveInterpreterConfiguration() throws CoreException {
 		if (fgInterpreterTypes == null) {
 			// if the Interpreter types have not been instantiated, there can be
 			// no changes.
 			return;
 		}
 		try {
 			String xml = getInterpretersAsXML();
 			getPreferences().setValue(PREF_INTERPRETER_XML, xml);
 			savePreferences();
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR,
 					LaunchingMessages.ScriptRuntime_exceptionsOccurred, e));
 		} catch (ParserConfigurationException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR,
 					LaunchingMessages.ScriptRuntime_exceptionsOccurred, e));
 		} catch (TransformerException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKLaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR,
 					LaunchingMessages.ScriptRuntime_exceptionsOccurred, e));
 		}
 	}
 
 	private static String getInterpretersAsXML() throws IOException,
 			ParserConfigurationException, TransformerException {
 		InterpreterDefinitionsContainer container = new InterpreterDefinitionsContainer();
 
 		DefaultInterpreterEntry[] entries = getDefaultInterpreterIDs();
 		for (int i = 0; i < entries.length; i++) {
 			String id = getDefaultInterpreterId(entries[i]);
 			if (id != null)
 				container.setDefaultInterpreterInstallCompositeID(entries[i],
 						id);
 			id = getDefaultInterpreterConnectorId(entries[i]);
 			if (id != null)
 				container.setDefaultInterpreterInstallConnectorTypeID(
 						entries[i], id);
 		}
 
 		IInterpreterInstallType[] InterpreterTypes = getInterpreterInstallTypes();
 		for (int i = 0; i < InterpreterTypes.length; ++i) {
 			IInterpreterInstall[] Interpreters = InterpreterTypes[i]
 					.getInterpreterInstalls();
 			for (int j = 0; j < Interpreters.length; j++) {
 				IInterpreterInstall install = Interpreters[j];
 				container.addInterpreter(install);
 			}
 		}
 		return container.getAsXML();
 	}
 
 	/**
 	 * This method loads installed interpreters based an existing user
 	 * preference or old Interpreter configurations file. The interpreters found
 	 * in the preference or interpreter configurations file are added to the
 	 * given Interpreter definitions container.
 	 * 
 	 * Returns whether the user preferences should be set - i.e. if it was not
 	 * already set when initialized.
 	 */
 	private static boolean addPersistedInterpreters(
 			InterpreterDefinitionsContainer interpreterDefs) throws IOException {
 		// Try retrieving the Interpreter preferences from the preference store
 		String InterpreterXMLString = getPreferences().getString(
 				PREF_INTERPRETER_XML);
 
 		// If the preference was found, load Interpreters from it into memory
 		if (InterpreterXMLString.length() > 0) {
 			try {
 				ByteArrayInputStream inputStream = new ByteArrayInputStream(
 						InterpreterXMLString.getBytes());
 				InterpreterDefinitionsContainer.parseXMLIntoContainer(
 						inputStream, interpreterDefs);
 				return false;
 			} catch (IOException ioe) {
 				DLTKLaunchingPlugin.log(ioe);
 			}
 		} else {
 			// Otherwise, look for the old file that previously held the
 			// Interpreter definitions
 			IPath stateLocation = DLTKLaunchingPlugin.getDefault()
 					.getStateLocation();
 			IPath stateFile = stateLocation
 					.append("interpreterConfiguration.xml"); //$NON-NLS-1$
 			File file = new File(stateFile.toOSString());
 
 			if (file.exists()) {
 				// If file exists, load Interpreter definitions from it into
 				// memory and write the definitions to
 				// the preference store WITHOUT triggering any processing of the
 				// new value
 				FileInputStream fileInputStream = new FileInputStream(file);
 				InterpreterDefinitionsContainer.parseXMLIntoContainer(
 						fileInputStream, interpreterDefs);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Performs string substitution on the given expression.
 	 * 
 	 * @param expression
 	 * @return expression after string substitution
 	 * @throws CoreException
 	 * 
 	 */
 	private static String substitute(String expression) throws CoreException {
 		return VariablesPlugin.getDefault().getStringVariableManager()
 				.performStringSubstitution(expression);
 	}
 
 	/**
 	 * Returns whether the Interpreter install with the specified id was
 	 * contributed via the InterpreterInstalls extension point.
 	 * 
 	 * @param id
 	 *            Interpreter id
 	 * @return whether the Interpreter install was contributed via extension
 	 *         point
 	 * 
 	 */
 	public static boolean isContributedInterpreterInstall(String id) {
 		getInterpreterInstallTypes(); // ensure Interpreters are initialized
 		return fgContributedInterpreters.contains(id);
 	}
 
 	/**
 	 * Evaluates library locations for a IInterpreterInstall. If no library
 	 * locations are set on the install, a default location is evaluated and
 	 * checked if it exists.
 	 * 
 	 * @return library locations with paths that exist or are empty
 	 * 
 	 */
 	public static LibraryLocation[] getLibraryLocations(
 			IInterpreterInstall interperterInstall) {
 		return getLibraryLocations(interperterInstall, null);
 	}
 
 	public static LibraryLocation[] getLibraryLocations(
 			IInterpreterInstall interperterInstall, IProgressMonitor monitor) {
 
 		LibraryLocation[] locations = interperterInstall.getLibraryLocations();
 		if (locations != null) {
 			return locations;
 		}
 
 		LibraryLocation[] defaultLocations = interperterInstall
 				.getInterpreterInstallType().getDefaultLibraryLocations(
 						interperterInstall.getInstallLocation(),
 						interperterInstall.getEnvironmentVariables(), monitor);
 
 		List existingDefaultLocations = new ArrayList();
 		for (int i = 0; i < defaultLocations.length; ++i) {
 			LibraryLocation location = defaultLocations[i];
 
 			IFileHandle file = EnvironmentPathUtils.getFile(location
 					.getLibraryPath());
 			if (file.exists()) {
 				existingDefaultLocations.add(location);
 			}
 		}
 
 		return (LibraryLocation[]) existingDefaultLocations
 				.toArray(new LibraryLocation[existingDefaultLocations.size()]);
 	}
 
 	/**
 	 * Creates and returns a buildpath entry describing the default interpreter
 	 * container entry.
 	 * 
 	 * @return a new IBuildpathEntry that describes the default interpreter
 	 *         container entry
 	 * 
 	 */
 	public static IBuildpathEntry getDefaultInterpreterContainerEntry() {
 		return DLTKCore.newContainerEntry(newDefaultInterpreterContainerPath());
 	}
 
 	/**
 	 * Returns a path for the interpreter buildpath container identifying the
 	 * default interpreter install.
 	 * 
 	 * @return buildpath container path
 	 * 
 	 */
 	public static IPath newDefaultInterpreterContainerPath() {
 		return new Path(INTERPRETER_CONTAINER);
 	}
 
 	/**
 	 * Returns a path for the interpreter buildpath container identifying the
 	 * specified Interpreter install by type and name.
 	 * 
 	 * @param Interpreter
 	 *            Interpreter install
 	 * @return buildpath container path
 	 * 
 	 */
 	public static IPath newInterpreterContainerPath(
 			IInterpreterInstall interpreter) {
 		if (interpreter != null) {
 			String name = interpreter.getName();
 			String typeId = interpreter.getInterpreterInstallType().getId();
 			return newInterpreterContainerPath(typeId, name);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a path for the InterpreterEnvironment buildpath container
 	 * identifying the specified Interpreter install by type and name.
 	 * 
 	 * @param typeId
 	 *            Interpreter install type identifier
 	 * @param name
 	 *            Interpreter install name
 	 * @return buildpath container path
 	 * 
 	 */
 	public static IPath newInterpreterContainerPath(String typeId, String name) {
 		if (typeId == null || name == null) {
 			return null;
 		}
 
 		IPath path = newDefaultInterpreterContainerPath();
 		path = path.append(typeId);
 		path = path.append(name.replaceAll("/", "%2F")); //$NON-NLS-1$ //$NON-NLS-2$
 		return path;
 	}
 
 	/**
 	 * Returns the InterpreterEnvironment referenced by the specified
 	 * InterpreterEnvironment buildpath container path or <code>null</code> if
 	 * none.
 	 * 
 	 * @param InterpreterEnvironmentContainerPath
 	 * @return InterpreterEnvironment referenced by the specified
 	 *         InterpreterEnvironment buildpath container path or
 	 *         <code>null</code>
 	 * 
 	 */
 	public static IInterpreterInstall getInterpreterInstall(String nature,
 			String environment, IPath InterpreterEnvironmentContainerPath) {
 		try {
 			return InterpreterContainerInitializer.resolveInterpreter(nature,
 					environment, InterpreterEnvironmentContainerPath);
 		} catch (CoreException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the <code>IInterpreterInstall</code> represented by the specified
 	 * <code>compositeId</code>.
 	 * 
 	 * <p>
 	 * If an interpreter can not be found for the given <code>compositeId</code>
 	 * , the default interpreter for the specified <code>natureId</code> will be
 	 * returned.
 	 * 
 	 * If no default interpreter has been configured, <code>null</code> will be
 	 * returned.
 	 * </p>
 	 * 
 	 * @param compositeId
 	 *            the composite id that specifies an instance of
 	 *            IInterpreterInstall
 	 * @param natureId
 	 *            nature id
 	 * 
 	 * @return IInterpreterInstall instance or <code>null</code> if one can not
 	 *         be found.
 	 */
 	public static IInterpreterInstall getInterpreterInstall(String compositeId,
 			String natureId) {
 		IInterpreterInstall install = getInterpreterFromCompositeId(compositeId);
 		if (install == null) {
 			DefaultInterpreterEntry entry = new DefaultInterpreterEntry(
 					natureId, LocalEnvironment.ENVIRONMENT_ID);
 			install = ScriptRuntime.getDefaultInterpreterInstall(entry);
 		}
 
 		return install;
 	}
 
 	/**
 	 * Returns the identifier of the Interpreter install type referenced by the
 	 * given InterpreterEnvironment buildpath container path, or
 	 * <code>null</code> if none.
 	 * 
 	 * @param InterpreterEnvironmentContainerPath
 	 * @return Interpreter install type identifier or <code>null</code>
 	 * 
 	 */
 	public static String getInterpreterInstallTypeId(
 			IPath InterpreterEnvironmentContainerPath) {
 		return InterpreterContainerInitializer
 				.getInterpreterTypeId(InterpreterEnvironmentContainerPath);
 	}
 
 	/**
 	 * Returns the name of the Interpreter install referenced by the given
 	 * InterpreterEnvironment buildpath container path, or <code>null</code> if
 	 * none.
 	 * 
 	 * @param InterpreterEnvironmentContainerPath
 	 * @return Interpreter name or <code>null</code>
 	 * 
 	 */
 	public static String getInterpreterInstallName(
 			IPath InterpreterEnvironmentContainerPath) {
 		return InterpreterContainerInitializer
 				.getInterpreterName(InterpreterEnvironmentContainerPath);
 	}
 
 	/**
 	 * Returns a runtime buildpath entry identifying the InterpreterEnvironment
 	 * to use when launching the specified configuration or <code>null</code> if
 	 * none is specified. The entry returned represents a either abuildpath
 	 * container that resolves to a interpreter.
 	 * <p>
 	 * The entry is resolved as follows:
 	 * <ol>
 	 * <li>If the <code>ATTR_CONTAINER_PATH</code> is present, it is used to
 	 * create a buildpath container referring to a interpreter.</li> <li>When
 	 * such attribute are not specified, a default entry is created which refers
 	 * to the interpreter referenced by the build path of the configuration's
 	 * associated script project. This could be a buildpath variable or
 	 * buildpath container.</li> <li>When there is no script project associated
 	 * with a configuration, the workspace default interpreter is used to create
 	 * a container path.</li>
 	 * </ol>
 	 * </p>
 	 * 
 	 * @param configuration
 	 * @return buildpath container path identifying a interpreter or
 	 *         <code>null</code>
 	 * @exception org.eclipse.core.runtime.CoreException
 	 *                if an exception occurs retrieving attributes from the
 	 *                specified launch configuration
 	 */
 	public static IRuntimeBuildpathEntry computeInterpreterEntry(
 			ILaunchConfiguration configuration) throws CoreException {
 		String containerAttr = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_CONTAINER_PATH,
 				(String) null);
 		IPath containerPath = null;
 		if (containerAttr == null) {
 			// default interpreter for the launch configuration
 			IScriptProject proj = getScriptProject(configuration);
 			if (proj == null) {
 				containerPath = newDefaultInterpreterContainerPath();
 			} else {
 				return computeInterpreterEntry(proj);
 			}
 		} else {
 			containerPath = Path.fromPortableString(containerAttr);
 		}
 		if (containerPath != null) {
 			return newRuntimeContainerBuildpathEntry(containerPath,
 					IRuntimeBuildpathEntry.STANDARD_ENTRY);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a runtime buildpath entry identifying the InterpreterEnvironment
 	 * referenced by the specified project, or <code>null</code> if none. The
 	 * entry returned represents a either a buildpath variable or buildpath
 	 * container that resolves to a InterpreterEnvironment.
 	 * 
 	 * @param project
 	 *            Script project
 	 * @return InterpreterEnvironment runtime buildpath entry or
 	 *         <code>null</code>
 	 * @exception org.eclipse.core.runtime.CoreException
 	 *                if an exception occurs accessing the project's buildpath
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry computeInterpreterEntry(
 			IScriptProject project) throws CoreException {
 		IBuildpathEntry[] rawBuildpath = project.getRawBuildpath();
 		IRuntimeBuildpathEntryResolver2 resolver = null;
 		for (int i = 0; i < rawBuildpath.length; i++) {
 			IBuildpathEntry entry = rawBuildpath[i];
 			switch (entry.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				resolver = getContainerResolver(entry.getPath().segment(0));
 				if (resolver != null) {
 					if (resolver.isInterpreterInstallReference(
 							getNatureFromProject(project),
 							getEnvironmentFromProject(project), entry)) {
 						IBuildpathContainer container = DLTKCore
 								.getBuildpathContainer(entry.getPath(), project);
 						if (container != null) {
 							switch (container.getKind()) {
 							case IBuildpathContainer.K_APPLICATION:
 								break;
 							case IBuildpathContainer.K_DEFAULT_SYSTEM:
 								return newRuntimeContainerBuildpathEntry(entry
 										.getPath(),
 										IRuntimeBuildpathEntry.STANDARD_ENTRY);
 							case IBuildpathContainer.K_SYSTEM:
 								return newRuntimeContainerBuildpathEntry(entry
 										.getPath(),
 										IRuntimeBuildpathEntry.BOOTSTRAP_ENTRY);
 							}
 						}
 					}
 				}
 				break;
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Returns whether the given runtime buildpath entry refers to a Interpreter
 	 * install.
 	 * 
 	 * @param entry
 	 * @return whether the given runtime buildpath entry refers to a Interpreter
 	 *         install
 	 * 
 	 */
 	public static boolean isInterpreterInstallReference(String lang,
 			String environment, IRuntimeBuildpathEntry entry) {
 		IBuildpathEntry buildpathEntry = entry.getBuildpathEntry();
 		if (buildpathEntry != null) {
 			switch (buildpathEntry.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				IRuntimeBuildpathEntryResolver2 resolver = getContainerResolver(buildpathEntry
 						.getPath().segment(0));
 				if (resolver != null) {
 					return resolver.isInterpreterInstallReference(lang,
 							environment, buildpathEntry);
 				}
 				break;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the given listener to the list of registered Interpreter install
 	 * changed listeners. Has no effect if an identical listener is already
 	 * registered.
 	 * 
 	 * @param listener
 	 *            the listener to add
 	 * 
 	 */
 	public static void addInterpreterInstallChangedListener(
 			IInterpreterInstallChangedListener listener) {
 		fgInterpreterListeners.add(listener);
 	}
 
 	/**
 	 * Removes the given listener from the list of registered Interpreter
 	 * install changed listeners. Has no effect if an identical listener is not
 	 * already registered.
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 * 
 	 */
 	public static void removeInterpreterInstallChangedListener(
 			IInterpreterInstallChangedListener listener) {
 		fgInterpreterListeners.remove(listener);
 	}
 
 	private static void notifyDefaultInterpreterChanged(
 			IInterpreterInstall previous, IInterpreterInstall current) {
 		Object[] listeners = fgInterpreterListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IInterpreterInstallChangedListener listener = (IInterpreterInstallChangedListener) listeners[i];
 			listener.defaultInterpreterInstallChanged(previous, current);
 		}
 	}
 
 	/**
 	 * Notifies all Interpreter install changed listeners of the given property
 	 * change.
 	 * 
 	 * @param event
 	 *            event describing the change.
 	 * 
 	 */
 	public static void fireInterpreterChanged(PropertyChangeEvent event) {
 		Object[] listeners = fgInterpreterListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IInterpreterInstallChangedListener listener = (IInterpreterInstallChangedListener) listeners[i];
 			listener.interpreterChanged(event);
 		}
 	}
 
 	/**
 	 * Notifies all Interpreter install changed listeners of the Interpreter
 	 * addition
 	 * 
 	 * @param Interpreter
 	 *            the Interpreter that has been added
 	 * 
 	 */
 	public static void fireInterpreterAdded(IInterpreterInstall Interpreter) {
 		if (!fgInitializingInterpreters) {
 			Object[] listeners = fgInterpreterListeners.getListeners();
 			for (int i = 0; i < listeners.length; i++) {
 				IInterpreterInstallChangedListener listener = (IInterpreterInstallChangedListener) listeners[i];
 				listener.interpreterAdded(Interpreter);
 			}
 		}
 	}
 
 	/**
 	 * Notifies all Interpreter install changed listeners of the Interpreter
 	 * removal
 	 * 
 	 * @param Interpreter
 	 *            the Interpreter that has been removed
 	 * 
 	 */
 	public static void fireInterpreterRemoved(IInterpreterInstall Interpreter) {
 		Object[] listeners = fgInterpreterListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			IInterpreterInstallChangedListener listener = (IInterpreterInstallChangedListener) listeners[i];
 			listener.interpreterRemoved(Interpreter);
 		}
 	}
 
 	/**
 	 * Returns the preference store for the launching plug-in.
 	 * 
 	 * @return the preference store for the launching plug-in
 	 * 
 	 */
 	public static Preferences getPreferences() {
 		return DLTKLaunchingPlugin.getDefault().getPluginPreferences();
 	}
 
 	/**
 	 * Saves the preferences for the launching plug-in.
 	 * 
 	 * 
 	 */
 	public static void savePreferences() {
 		DLTKLaunchingPlugin.getDefault().savePluginPreferences();
 	}
 
 	/**
 	 * Registers the given resolver for the specified container.
 	 * 
 	 * @param resolver
 	 *            runtime buildpath entry resolver
 	 * @param containerIdentifier
 	 *            identifier of the buildpath container to register for
 	 * 
 	 */
 	public static void addContainerResolver(
 			IRuntimeBuildpathEntryResolver resolver, String containerIdentifier) {
 		Map map = getContainerResolvers();
 		map.put(containerIdentifier, resolver);
 	}
 
 	/**
 	 * Returns all registered container resolvers.
 	 */
 	private static Map getContainerResolvers() {
 		if (fgContainerResolvers == null) {
 			initializeResolvers();
 		}
 		return fgContainerResolvers;
 	}
 
 	private static void initializeResolvers() {
 		IExtensionPoint point = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKLaunchingPlugin.PLUGIN_ID,
 						EXTENSION_POINT_RUNTIME_BUILDPATH_ENTRY_RESOLVERS);
 		IConfigurationElement[] extensions = point.getConfigurationElements();
 		fgContainerResolvers = new HashMap(extensions.length);
 		fgRuntimeBuildpathEntryResolvers = new HashMap(extensions.length);
 		for (int i = 0; i < extensions.length; i++) {
 			RuntimeBuildpathEntryResolver res = new RuntimeBuildpathEntryResolver(
 					extensions[i]);
 			String container = res.getContainerId();
 			String entryId = res.getRuntimeBuildpathEntryId();
 			if (container != null) {
 				fgContainerResolvers.put(container, res);
 			}
 			if (entryId != null) {
 				fgRuntimeBuildpathEntryResolvers.put(entryId, res);
 			}
 		}
 	}
 
 	/**
 	 * Returns all registered buildpath providers.
 	 */
 	private static Map getBuildpathProviders() {
 		if (fgPathProviders == null) {
 			initializeProviders();
 		}
 		return fgPathProviders;
 	}
 
 	private static void initializeProviders() {
 		IExtensionPoint point = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKLaunchingPlugin.PLUGIN_ID,
 						EXTENSION_POINT_RUNTIME_BUILDPATH_PROVIDERS);
 		IConfigurationElement[] extensions = point.getConfigurationElements();
 		fgPathProviders = new HashMap(extensions.length);
 		for (int i = 0; i < extensions.length; i++) {
 			RuntimeBuildpathProvider res = new RuntimeBuildpathProvider(
 					extensions[i]);
 			fgPathProviders.put(res.getIdentifier(), res);
 		}
 	}
 
 	/**
 	 * Returns the resolver registered for the given container id, or
 	 * <code>null</code> if none.
 	 * 
 	 * @param containerId
 	 *            the container to determine the resolver for
 	 * @return the resolver registered for the given container id, or
 	 *         <code>null</code> if none
 	 */
 	private static IRuntimeBuildpathEntryResolver2 getContainerResolver(
 			String containerId) {
 		return (IRuntimeBuildpathEntryResolver2) getContainerResolvers().get(
 				containerId);
 	}
 
 	/**
 	 * Returns a collection of paths that should be appended to the given
 	 * project's library path when launched. Entries are searched for on the
 	 * project's build path as extra buildpath attributes. Each entry represents
 	 * an absolute path in the local file system.
 	 * 
 	 * @param project
 	 *            the project to compute the <code>java.library.path</code> for
 	 * @param requiredProjects
 	 *            whether to consider entries in required projects
 	 * @return a collection of paths representing entries that should be
 	 *         appended to the given project's <code>java.library.path</code>
 	 * @throws CoreException
 	 *             if unable to compute the Script library path
 	 * 
 	 * @see org.eclipse.dltk.core.IBuildpathAttribute
 	 * @see ScriptRuntime#BUILDPATH_ATTR_LIBRARY_PATH_ENTRY
 	 */
 	public static String[] computeScriptLibraryPath(IScriptProject project,
 			boolean requiredProjects) throws CoreException {
 		Set visited = new HashSet();
 		List entries = new ArrayList();
 		gatherScriptLibraryPathEntries(project, requiredProjects, visited,
 				entries);
 		List resolved = new ArrayList(entries.size());
 		Iterator iterator = entries.iterator();
 		IStringVariableManager manager = VariablesPlugin.getDefault()
 				.getStringVariableManager();
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		while (iterator.hasNext()) {
 			String entry = (String) iterator.next();
 			String resolvedEntry = manager.performStringSubstitution(entry);
 			IPath path = new Path(resolvedEntry);
 			if (path.isAbsolute()) {
 				File file = path.toFile();
 				resolved.add(file.getAbsolutePath());
 			} else {
 				IResource resource = root.findMember(path);
 				if (resource != null) {
 					IPath location = resource.getLocation();
 					if (location != null) {
 						resolved.add(location.toFile().getAbsolutePath());
 					}
 				}
 			}
 		}
 		return (String[]) resolved.toArray(new String[resolved.size()]);
 	}
 
 	/**
 	 * Gathers all Script library entries for the given project and optionally
 	 * its required projects.
 	 * 
 	 * @param project
 	 *            project to gather entries for
 	 * @param requiredProjects
 	 *            whether to consider required projects
 	 * @param visited
 	 *            projects already considered
 	 * @param entries
 	 *            collection to add library entries to
 	 * @throws CoreException
 	 *             if unable to gather buildpath entries
 	 * 
 	 */
 	private static void gatherScriptLibraryPathEntries(IScriptProject project,
 			boolean requiredProjects, Set visited, List entries)
 			throws CoreException {
 		if (visited.contains(project)) {
 			return;
 		}
 		visited.add(project);
 		IBuildpathEntry[] rawBuildpath = project.getRawBuildpath();
 		IBuildpathEntry[] required = processScriptLibraryPathEntries(project,
 				requiredProjects, rawBuildpath, entries);
 		if (required != null) {
 			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 			for (int i = 0; i < required.length; i++) {
 				IBuildpathEntry entry = required[i];
 				String projectName = entry.getPath().segment(0);
 				IProject p = root.getProject(projectName);
 				if (p.exists()) {
 					IScriptProject requiredProject = DLTKCore.create(p);
 					if (requiredProject != null) {
 						gatherScriptLibraryPathEntries(requiredProject,
 								requiredProjects, visited, entries);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds all library path extra buildpath entry values to the given entries
 	 * collection specified on the given project's buildpath, and returns a
 	 * collection of required projects, or <code>null</code>.
 	 * 
 	 * @param project
 	 *            project being processed
 	 * @param collectRequired
 	 *            whether to collect required projects
 	 * @param buildpathEntries
 	 *            the project's raw buildpath
 	 * @param entries
 	 *            collection to add script library path entries to
 	 * @return required project buildpath entries or <code>null</code>
 	 * @throws CoreException
 	 * 
 	 */
 	private static IBuildpathEntry[] processScriptLibraryPathEntries(
 			IScriptProject project, boolean collectRequired,
 			IBuildpathEntry[] buildpathEntries, List entries)
 			throws CoreException {
 		List req = null;
 		for (int i = 0; i < buildpathEntries.length; i++) {
 			IBuildpathEntry entry = buildpathEntries[i];
 			IBuildpathAttribute[] extraAttributes = entry.getExtraAttributes();
 			for (int j = 0; j < extraAttributes.length; j++) {
 				String[] paths = getLibraryPaths(extraAttributes[j]);
 				if (paths != null) {
 					for (int k = 0; k < paths.length; k++) {
 						entries.add(paths[k]);
 					}
 				}
 			}
 			if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
 				IBuildpathContainer container = DLTKCore.getBuildpathContainer(
 						entry.getPath(), project);
 				if (container != null) {
 					IBuildpathEntry[] requiredProjects = processScriptLibraryPathEntries(
 							project, collectRequired, container
 									.getBuildpathEntries(project), entries);
 					if (requiredProjects != null) {
 						if (req == null) {
 							req = new ArrayList();
 						}
 						for (int j = 0; j < requiredProjects.length; j++) {
 							req.add(requiredProjects[j]);
 						}
 					}
 				}
 			} else if (collectRequired
 					&& entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 				if (req == null) {
 					req = new ArrayList();
 				}
 				req.add(entry);
 			}
 		}
 		if (req != null) {
 			return (IBuildpathEntry[]) req.toArray(new IBuildpathEntry[req
 					.size()]);
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a new buildpath attribute referencing a list of shared libraries
 	 * that should appear on the <code>-Djava.library.path</code> system
 	 * property at runtime for an associated {@link IBuildpathEntry}.
 	 * <p>
 	 * The factory methods <code>newLibraryPathsAttribute(String[])</code> and
 	 * <code>getLibraryPaths(IBuildpathAttribute)</code> should be used to
 	 * encode and decode the attribute value.
 	 * </p>
 	 * 
 	 * @param paths
 	 *            an array of strings representing paths of shared libraries.
 	 *            Each string is used to create an <code>IPath</code> using the
 	 *            constructor <code>Path(String)</code>, and may contain
 	 *            <code>IStringVariable</code>'s. Variable substitution is
 	 *            performed on each string before a path is constructed from a
 	 *            string.
 	 * @return a buildpath attribute with the name
 	 *         <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code> and an value
 	 *         encoded to the specified paths.
 	 * 
 	 */
 	public static IBuildpathAttribute newLibraryPathsAttribute(String[] paths) {
 		StringBuffer value = new StringBuffer();
 		for (int i = 0; i < paths.length; i++) {
 			value.append(paths[i]);
 			if (i < (paths.length - 1)) {
 				value.append("|"); //$NON-NLS-1$
 			}
 		}
 		return DLTKCore.newBuildpathAttribute(
 				BUILDPATH_ATTR_LIBRARY_PATH_ENTRY, value.toString());
 	}
 
 	/**
 	 * Returns an array of strings referencing shared libraries that should
 	 * appear on the <code>-Djava.library.path</code> system property at runtime
 	 * for an associated {@link IBuildpathEntry}, or <code>null</code> if the
 	 * given attribute is not a <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>.
 	 * Each string is used to create an <code>IPath</code> using the constructor
 	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code> 
 	 * 's.
 	 * <p>
 	 * The factory methods <code>newLibraryPathsAttribute(String[])</code> and
 	 * <code>getLibraryPaths(IBuildpathAttribute)</code> should be used to
 	 * encode and decode the attribute value.
 	 * </p>
 	 * 
 	 * @param attribute
 	 *            a <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code> buildpath
 	 *            attribute
 	 * @return an array of strings referencing shared libraries that should
 	 *         appear on the <code>-Djava.library.path</code> system property at
 	 *         runtime for an associated {@link IBuildpathEntry}, or
 	 *         <code>null</code> if the given attribute is not a
 	 *         <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>. Each string is
 	 *         used to create an <code>IPath</code> using the constructor
 	 *         <code>Path(String)</code>, and may contain
 	 *         <code>IStringVariable</code>'s.
 	 * 
 	 */
 	public static String[] getLibraryPaths(IBuildpathAttribute attribute) {
 		if (BUILDPATH_ATTR_LIBRARY_PATH_ENTRY.equals(attribute.getName())) {
 			String value = attribute.getValue();
 			return value.split("\\|"); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * Loads contributed interpreter installs
 	 * 
 	 */
 	private static void addInterpreterExtensions(
 			InterpreterDefinitionsContainer InterpreterDefs) {
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKLaunchingPlugin.PLUGIN_ID,
 						ScriptRuntime.EXTENSION_POINT_INTERPRETER_INSTALLS);
 		IConfigurationElement[] configs = extensionPoint
 				.getConfigurationElements();
 		for (int i = 0; i < configs.length; i++) {
 			IConfigurationElement element = configs[i];
 			try {
 				if ("interpreterInstall".equals(element.getName())) { //$NON-NLS-1$
 					String InterpreterType = element
 							.getAttribute("interpreterInstallType"); //$NON-NLS-1$
 					if (InterpreterType == null) {
 						abort(
 								MessageFormat
 										.format(
 												"Missing required interpreterInstallType attribute for interpreterInstall contributed by {0}", //$NON-NLS-1$
 												new String[] { element
 														.getContributor()
 														.getName() }), null);
 					}
 					String id = element.getAttribute("id"); //$NON-NLS-1$
 					if (id == null) {
 						abort(
 								MessageFormat
 										.format(
 												"Missing required id attribute for interpreterInstall contributed by {0}", //$NON-NLS-1$
 												new String[] { element
 														.getContributor()
 														.getName() }), null);
 					}
 					IInterpreterInstallType installType = getInterpreterInstallType(InterpreterType);
 					if (installType == null) {
 						abort(
 								MessageFormat
 										.format(
 												"InterpreterInstall {0} contributed by {1} references undefined Interpreter install type {2}", //$NON-NLS-1$
 												new String[] {
 														id,
 														element
 																.getContributor()
 																.getName(),
 														InterpreterType }),
 								null);
 					}
 					IInterpreterInstall install = installType
 							.findInterpreterInstall(id);
 					if (install == null) {
 						// only load/create if first time we've seen this
 						// Interpreter install
 						String name = element.getAttribute("name"); //$NON-NLS-1$
 						if (name == null) {
 							abort(
 									MessageFormat
 											.format(
 													"interpreterInstall {0} contributed by {1} missing required attribute name", //$NON-NLS-1$
 													new String[] {
 															id,
 															element
 																	.getContributor()
 																	.getName() }),
 									null);
 						}
 						String home = element.getAttribute("home"); //$NON-NLS-1$
 						if (home == null) {
 							abort(
 									MessageFormat
 											.format(
 													"interpreterInstall {0} contributed by {1} missing required attribute home", //$NON-NLS-1$
 													new String[] {
 															id,
 															element
 																	.getContributor()
 																	.getName() }),
 									null);
 						}
 						String InterpreterArgs = element
 								.getAttribute("interpreterArgs"); //$NON-NLS-1$
 						InterpreterStandin standin = new InterpreterStandin(
 								installType, id);
 						standin.setName(name);
 						home = substitute(home);
 
 						// Only local installs can be contributed, so
 						// use local environment
 						IEnvironment localEnv = EnvironmentManager
 								.getLocalEnvironment();
 
 						IFileHandle homeDir = localEnv.getFile(new Path(home));
 						if (homeDir.exists()) {
 							// adjust for relative path names
 							home = homeDir.getCanonicalPath();
 							homeDir = localEnv.getFile(new Path(home));
 						}
 						IStatus status = installType
 								.validateInstallLocation(homeDir);
 						if (!status.isOK()) {
 							abort(
 									MessageFormat
 											.format(
 													"Illegal install location {0} for interpreterInstall {1} contributed by {2}: {3}", //$NON-NLS-1$
 													new String[] {
 															home,
 															id,
 															element
 																	.getContributor()
 																	.getName(),
 															status.getMessage() }),
 									null);
 						}
 						standin.setInstallLocation(homeDir);
 
 						if (InterpreterArgs != null) {
 							standin.setInterpreterArgs(InterpreterArgs);
 						}
 						IConfigurationElement[] libraries = element
 								.getChildren("library"); //$NON-NLS-1$
 						LibraryLocation[] locations = null;
 						if (libraries.length > 0) {
 							locations = new LibraryLocation[libraries.length];
 							for (int j = 0; j < libraries.length; j++) {
 								IConfigurationElement library = libraries[j];
 								String libPathStr = library
 										.getAttribute("path"); //$NON-NLS-1$
 								if (libPathStr == null) {
 									abort(
 											MessageFormat
 													.format(
 															"library for interpreterInstall {0} contributed by {1} missing required attribute libPath", //$NON-NLS-1$
 															new String[] {
 																	id,
 																	element
 																			.getContributor()
 																			.getName() }),
 											null);
 								}
 
 								IPath homePath = new Path(home);
 								IPath libPath = homePath
 										.append(substitute(libPathStr));
 
 								locations[j] = new LibraryLocation(libPath);
 							}
 						}
 						standin.setLibraryLocations(locations);
 						InterpreterDefs.addInterpreter(standin);
 					}
 					fgContributedInterpreters.add(id);
 				} else {
 					abort(
 							MessageFormat
 									.format(
 											"Illegal element {0} in InterpreterInstalls extension contributed by {1}", //$NON-NLS-1$
 											new String[] {
 													element.getName(),
 													element.getContributor()
 															.getName() }), null);
 				}
 			} catch (CoreException e) {
 				DLTKLaunchingPlugin.log(e);
 			}
 		}
 	}
 
 	/**
 	 * Perform Interpreter type and Interpreter install initialization. Does not
 	 * hold locks while performing change notification.
 	 * 
 	 * 
 	 */
 	private static void initializeInterpreters() {
 		InterpreterDefinitionsContainer defs = null;
 		boolean setPref = false;
 		synchronized (fgInterpreterLock) {
 			if (fgInterpreterTypes == null) {
 				try {
 					fgInitializingInterpreters = true;
 					// 1. load Interpreter type extensions
 					initializeInterpreterTypeExtensions();
 					try {
 						defs = new InterpreterDefinitionsContainer();
 
 						// 2. add persisted Interpreters
 						setPref = addPersistedInterpreters(defs);
 
 						// 4. load contributed Interpreter installs
 						addInterpreterExtensions(defs);
 
 						// 5. verify default interpreters is valid
 						DefaultInterpreterEntry[] natures = defs
 								.getInterpreterNatures();
 						for (int i = 0; i < natures.length; i++) {
 							String defId = defs
 									.getDefaultInterpreterInstallCompositeID(natures[i]);
 							boolean validDef = false;
 							if (defId != null) {
 								Iterator iterator = defs
 										.getValidInterpreterList().iterator();
 								while (iterator.hasNext()) {
 									IInterpreterInstall Interpreter = (IInterpreterInstall) iterator
 											.next();
 									if (getCompositeIdFromInterpreter(
 											Interpreter).equals(defId)) {
 										validDef = true;
 										break;
 									}
 								}
 							}
 
 							if (!validDef) {
 								// use the first as the default
 								setPref = true;
 								List list = defs
 										.getValidInterpreterList(natures[i]);
 								if (!list.isEmpty()) {
 									IInterpreterInstall Interpreter = (IInterpreterInstall) list
 											.get(0);
 									defs
 											.setDefaultInterpreterInstallCompositeID(
 													natures[i],
 													getCompositeIdFromInterpreter(Interpreter));
 								}
 							}
 
 							String defInstCID = defs
 									.getDefaultInterpreterInstallCompositeID(natures[i]);
 							fgDefaultInterpreterId.put(natures[i], defInstCID);
 							String defIntCTypeID = defs
 									.getDefaultInterpreterInstallConnectorTypeID(natures[i]);
 							fgDefaultInterpreterConnectorId.put(natures[i],
 									defIntCTypeID);
 						}
 						// Create the underlying interpreters for each valid
 						// Interpreter
 						List InterpreterList = defs.getValidInterpreterList();
 						Iterator InterpreterListIterator = InterpreterList
 								.iterator();
 						while (InterpreterListIterator.hasNext()) {
 							InterpreterStandin InterpreterStandin = (InterpreterStandin) InterpreterListIterator
 									.next();
 							InterpreterStandin.convertToRealInterpreter();
 						}
 
 					} catch (IOException e) {
 						DLTKLaunchingPlugin.log(e);
 					}
 				} finally {
 					fgInitializingInterpreters = false;
 				}
 			}
 		}
 		if (defs != null) {
 			// notify of initial Interpreters for backwards compatibility
 			IInterpreterInstallType[] installTypes = getInterpreterInstallTypes();
 			for (int i = 0; i < installTypes.length; i++) {
 				IInterpreterInstallType type = installTypes[i];
 				IInterpreterInstall[] installs = type.getInterpreterInstalls();
 				if (installs != null) {
 					for (int j = 0; j < installs.length; j++) {
 						fireInterpreterAdded(installs[j]);
 					}
 				}
 			}
 
 			// save settings if required
 			if (setPref) {
 				try {
 					String xml = defs.getAsXML();
 					DLTKLaunchingPlugin.getDefault().getPluginPreferences()
 							.setValue(PREF_INTERPRETER_XML, xml);
 				} catch (ParserConfigurationException e) {
 					DLTKLaunchingPlugin.log(e);
 				} catch (IOException e) {
 					DLTKLaunchingPlugin.log(e);
 				} catch (TransformerException e) {
 					DLTKLaunchingPlugin.log(e);
 				}
 
 			}
 
 		}
 	}
 
 	/**
 	 * Returns a new runtime buildpath entry containing the default buildpath
 	 * for the specified script project.
 	 * 
 	 * @param project
 	 *            project
 	 * @return runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newDefaultProjectBuildpathEntry(
 			IScriptProject project) {
 		return new DefaultProjectBuildpathEntry(project);
 	}
 
 	/**
 	 * Returns a new runtime buildpath entry for the given project.
 	 * 
 	 * @param project
 	 *            Script project
 	 * @return runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newProjectRuntimeBuildpathEntry(
 			IScriptProject project) {
 		IBuildpathEntry cpe = DLTKCore.newProjectEntry(project.getProject()
 				.getFullPath());
 		return newRuntimeBuildpathEntry(cpe);
 	}
 
 	/**
 	 * Returns a new runtime buildpath entry for the given archive.
 	 * 
 	 * @param resource
 	 *            archive resource
 	 * @return runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newArchiveRuntimeBuildpathEntry(
 			IResource resource) {
 		IBuildpathEntry cpe = DLTKCore.newLibraryEntry(resource.getFullPath());
 		return newRuntimeBuildpathEntry(cpe);
 	}
 
 	/**
 	 * Returns a new runtime buildpath entry for the given archive (possibly
 	 * external).
 	 * 
 	 * @param path
 	 *            absolute path to an archive
 	 * @return runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newArchiveRuntimeBuildpathEntry(
 			IPath path) {
 		IBuildpathEntry cpe = DLTKCore.newLibraryEntry(path);
 		return newRuntimeBuildpathEntry(cpe);
 	}
 
 	/**
 	 * Returns a runtime buildpath entry for the given container path with the
 	 * given buildpath property.
 	 * 
 	 * @param path
 	 *            container path
 	 * @param buildpathProperty
 	 *            the type of entry - one of <code>USER_CLASSES</code>, or
 	 *            <code>STANDARD_CLASSES</code>
 	 * @return runtime buildpath entry
 	 * @exception CoreException
 	 *                if unable to construct a runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newRuntimeContainerBuildpathEntry(
 			IPath path, int buildpathProperty) throws CoreException {
 		return newRuntimeContainerBuildpathEntry(path, buildpathProperty, null);
 	}
 
 	/**
 	 * Returns a runtime buildpath entry for the given container path with the
 	 * given buildpath property to be resolved in the context of the given
 	 * Script project.
 	 * 
 	 * @param path
 	 *            container path
 	 * @param buildpathProperty
 	 *            the type of entry - one of <code>USER_CLASSES</code>, or
 	 *            <code>STANDARD_CLASSES</code>
 	 * @param project
 	 *            Script project context used for resolution, or
 	 *            <code>null</code> if to be resolved in the context of the
 	 *            launch configuration this entry is referenced in
 	 * @return runtime buildpath entry
 	 * @exception CoreException
 	 *                if unable to construct a runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newRuntimeContainerBuildpathEntry(
 			IPath path, int buildpathProperty, IScriptProject project)
 			throws CoreException {
 		IBuildpathEntry cpe = DLTKCore.newContainerEntry(path);
 		RuntimeBuildpathEntry entry = new RuntimeBuildpathEntry(cpe,
 				buildpathProperty);
 		entry.setScriptProject(project);
 		return entry;
 	}
 
 	/**
 	 * Returns a runtime buildpath entry constructed from the given memento.
 	 * 
 	 * @param memento
 	 *            a memento for a runtime buildpath entry
 	 * @return runtime buildpath entry
 	 * @exception CoreException
 	 *                if unable to construct a runtime buildpath entry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry newRuntimeBuildpathEntry(String memento)
 			throws CoreException {
 		try {
 			Element root = null;
 			DocumentBuilder parser = DLTKLaunchingPlugin.getParser();
 			StringReader reader = new StringReader(memento);
 			InputSource source = new InputSource(reader);
 			root = parser.parse(source).getDocumentElement();
 
 			String id = root.getAttribute("id"); //$NON-NLS-1$
 			if (id == null || id.length() == 0) {
 				// assume an old format
 				return new RuntimeBuildpathEntry(root);
 			}
 			// get the extension & create a new one
 			IRuntimeBuildpathEntry2 entry = DLTKLaunchingPlugin.getDefault()
 					.newRuntimeBuildpathEntry(id);
 			NodeList list = root.getChildNodes();
 			for (int i = 0; i < list.getLength(); i++) {
 				Node node = list.item(i);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					Element element = (Element) node;
 					if ("memento".equals(element.getNodeName())) { //$NON-NLS-1$
 						entry.initializeFrom(element);
 					}
 				}
 			}
 			return entry;
 		} catch (SAXException e) {
 			abort(LaunchingMessages.ScriptRuntime_31, e);
 		} catch (IOException e) {
 			abort(LaunchingMessages.ScriptRuntime_32, e);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a runtime buildpath entry that corresponds to the given buildpath
 	 * entry. The buildpath entry may not be of type <code>CPE_SOURCE</code> or
 	 * <code>BPE_CONTAINER</code>.
 	 * 
 	 * @param entry
 	 *            a buildpath entry
 	 * @return runtime buildpath entry
 	 * 
 	 */
 	private static IRuntimeBuildpathEntry newRuntimeBuildpathEntry(
 			IBuildpathEntry entry) {
 		return new RuntimeBuildpathEntry(entry);
 	}
 
 	/**
 	 * Computes and returns the default unresolved runtime buildpath for the
 	 * given project.
 	 * 
 	 * @return runtime buildpath entries
 	 * @exception CoreException
 	 *                if unable to compute the runtime buildpath
 	 * @see IRuntimeBuildpathEntry
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry[] computeUnresolvedRuntimeBuildpath(
 			IScriptProject project) throws CoreException {
 		IBuildpathEntry[] entries = project.getRawBuildpath();
 		List buildpathEntries = new ArrayList(3);
 		for (int i = 0; i < entries.length; i++) {
 			IBuildpathEntry entry = entries[i];
 			switch (entry.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				IBuildpathContainer container = DLTKCore.getBuildpathContainer(
 						entry.getPath(), project);
 				if (container != null) {
 					switch (container.getKind()) {
 					case IBuildpathContainer.K_APPLICATION:
 						// don't look at application entries
 						break;
 					case IBuildpathContainer.K_DEFAULT_SYSTEM:
 					case IBuildpathContainer.K_SYSTEM:
 						buildpathEntries
 								.add(newRuntimeContainerBuildpathEntry(
 										container.getPath(),
 										IRuntimeBuildpathEntry.STANDARD_ENTRY,
 										project));
 						break;
 					}
 				}
 				break;
 			default:
 				break;
 			}
 		}
 		buildpathEntries.add(newDefaultProjectBuildpathEntry(project));
 		return (IRuntimeBuildpathEntry[]) buildpathEntries
 				.toArray(new IRuntimeBuildpathEntry[buildpathEntries.size()]);
 	}
 
 	/**
 	 * Returns the buildpath provider for the given launch configuration.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return buildpath provider
 	 * @exception CoreException
 	 *                if unable to resolve the path provider
 	 * 
 	 */
 	public static IRuntimeBuildpathProvider getBuildpathProvider(
 			ILaunchConfiguration configuration) throws CoreException {
 		String providerId = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_BUILDPATH_PROVIDER,
 				(String) null);
 		IRuntimeBuildpathProvider provider = null;
 		if (providerId == null) {
 			provider = fgDefaultBuildpathProvider;
 		} else {
 			provider = (IRuntimeBuildpathProvider) getBuildpathProviders().get(
 					providerId);
 			if (provider == null) {
 				abort(MessageFormat.format(LaunchingMessages.ScriptRuntime_26,
 						new String[] { providerId }), null);
 			}
 		}
 		return provider;
 	}
 
 	public static IRuntimeBuildpathProvider getScriptpathProvider(
 			ILaunchConfiguration configuration) throws CoreException {
 
 		String providerId = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SOURCEPATH_PROVIDER,
 				(String) null);
 		IRuntimeBuildpathProvider provider = null;
 		if (providerId == null) {
 			provider = fgDefaultSourcepathProvider;
 		} else {
 
 			provider = (IRuntimeBuildpathProvider) getBuildpathProviders().get(
 					providerId);
 			if (provider == null) {
 				abort(MessageFormat.format(LaunchingMessages.ScriptRuntime_27,
 						new String[] { providerId }), null);
 			}
 		}
 		return provider;
 	}
 
 	/**
 	 * Performs default resolution for a container entry. Delegates to the
 	 * Script model.
 	 */
 	private static IRuntimeBuildpathEntry[] computeDefaultContainerEntries(
 			IRuntimeBuildpathEntry entry, ILaunchConfiguration config)
 			throws CoreException {
 		IScriptProject project = entry.getScriptProject();
 
 		if (project == null) {
 			project = getScriptProject(config);
 		}
 		return computeDefaultContainerEntries(entry, project);
 	}
 
 	/**
 	 * Performs default resolution for a container entry. Delegates to the
 	 * Script model.
 	 */
 	private static IRuntimeBuildpathEntry[] computeDefaultContainerEntries(
 			IRuntimeBuildpathEntry entry, IScriptProject project)
 			throws CoreException {
 		if (project == null || entry == null) {
 			// cannot resolve without entry or project context
 			return new IRuntimeBuildpathEntry[0];
 		}
 		IBuildpathContainer container = DLTKCore.getBuildpathContainer(entry
 				.getPath(), project);
 		if (container == null) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.ScriptRuntime_Could_not_resolve_classpath_container___0__1,
 									new String[] { entry.getPath().toString() }),
 					null);
 			// execution will not reach here - exception will be thrown
 			return null;
 		}
 		IBuildpathEntry[] cpes = container.getBuildpathEntries(project);
 		int property = -1;
 		switch (container.getKind()) {
 		case IBuildpathContainer.K_APPLICATION:
 			property = IRuntimeBuildpathEntry.USER_ENTRY;
 			break;
 		case IBuildpathContainer.K_DEFAULT_SYSTEM:
 			property = IRuntimeBuildpathEntry.STANDARD_ENTRY;
 			break;
 		case IBuildpathContainer.K_SYSTEM:
 			property = IRuntimeBuildpathEntry.BOOTSTRAP_ENTRY;
 			break;
 		}
 		List resolved = new ArrayList(cpes.length);
 		List projects = (List) fgProjects.get();
 		Integer count = (Integer) fgEntryCount.get();
 		if (projects == null) {
 			projects = new ArrayList();
 			fgProjects.set(projects);
 			count = new Integer(0);
 		}
 		int intCount = count.intValue();
 		intCount++;
 		fgEntryCount.set(new Integer(intCount));
 		try {
 			for (int i = 0; i < cpes.length; i++) {
 				IBuildpathEntry cpe = cpes[i];
 				if (cpe.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 					IProject p = ResourcesPlugin.getWorkspace().getRoot()
 							.getProject(cpe.getPath().segment(0));
 					IScriptProject jp = DLTKCore.create(p);
 					if (!projects.contains(jp)) {
 						projects.add(jp);
 						IRuntimeBuildpathEntry buildpath = newDefaultProjectBuildpathEntry(jp);
 						IRuntimeBuildpathEntry[] entries = resolveRuntimeBuildpathEntry(
 								buildpath, jp);
 						for (int j = 0; j < entries.length; j++) {
 							IRuntimeBuildpathEntry e = entries[j];
 							if (!resolved.contains(e)) {
 								resolved.add(entries[j]);
 							}
 						}
 					}
 				} else {
 					IRuntimeBuildpathEntry e = newRuntimeBuildpathEntry(cpe);
 					if (!resolved.contains(e)) {
 						resolved.add(e);
 					}
 				}
 			}
 		} finally {
 			intCount--;
 			if (intCount == 0) {
 				fgProjects.set(null);
 				fgEntryCount.set(null);
 			} else {
 				fgEntryCount.set(new Integer(intCount));
 			}
 		}
 		// set buildpath property
 		IRuntimeBuildpathEntry[] result = new IRuntimeBuildpathEntry[resolved
 				.size()];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = (IRuntimeBuildpathEntry) resolved.get(i);
 			result[i].setBuildpathProperty(property);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns resolved entries for the given entry in the context of the given
 	 * launch configuration. If the entry is of kind <code>CONTAINER</code>,
 	 * container resolvers are consulted. Otherwise, the given entry is
 	 * returned.
 	 * <p>
 	 * If the given entry is a container, and a resolver is not registered,
 	 * resolved runtime buildpath entries are calculated from the associated
 	 * container buildpath entries, in the context of the project associated
 	 * with the given launch configuration.
 	 * </p>
 	 * 
 	 * @param entry
 	 *            runtime classpath entry
 	 * @param configuration
 	 *            launch configuration
 	 * @return resolved runtime classpath entry
 	 * @exception CoreException
 	 *                if unable to resolve
 	 * @see IRuntimeBuildpathEntryResolver
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry[] resolveRuntimeBuildpathEntry(
 			IRuntimeBuildpathEntry entry, ILaunchConfiguration configuration)
 			throws CoreException {
 		switch (entry.getType()) {
 		case IRuntimeBuildpathEntry.PROJECT:
 			// if the project has multiple output locations, they must be
 			// returned
 			IResource resource = entry.getResource();
 			if (resource instanceof IProject) {
 				IProject p = (IProject) resource;
 				IScriptProject project = DLTKCore.create(p);
 				if (project == null || !p.isOpen() || !project.exists()) {
 					return new IRuntimeBuildpathEntry[0];
 				}
 			} else {
 				abort(
 						MessageFormat
 								.format(
 										LaunchingMessages.ScriptRuntime_Buildpath_references_non_existant_project___0__3,
 										new String[] { entry.getPath()
 												.lastSegment() }), null);
 			}
 			break;
 		case IRuntimeBuildpathEntry.CONTAINER:
 			IRuntimeBuildpathEntryResolver resolver = getContainerResolver(entry
 					.getContainerName());
 			if (resolver == null) {
 				return computeDefaultContainerEntries(entry, configuration);
 			}
 			return resolver.resolveRuntimeBuildpathEntry(entry, configuration);
 		case IRuntimeBuildpathEntry.ARCHIVE:
 			// verify the archive exists
 			String location = entry.getLocation();
 			if (location == null) {
 				abort(
 						MessageFormat
 								.format(
 										LaunchingMessages.ScriptRuntime_Buildpath_references_non_existant_archive___0__4,
 										new String[] { entry.getPath()
 												.toString() }), null);
 			}
			IFileHandle fileHandle = EnvironmentPathUtils.getFile(Path
					.fromPortableString(location));
 			if (!fileHandle.exists()) {
 				abort(
 						MessageFormat
 								.format(
 										LaunchingMessages.ScriptRuntime_Buildpath_references_non_existant_archive___0__4,
 										new String[] { entry.getPath()
 												.toString() }), null);
 			}
 			break;
 		case IRuntimeBuildpathEntry.OTHER:
 			resolver = getContributedResolver(((IRuntimeBuildpathEntry2) entry)
 					.getTypeId());
 			return resolver.resolveRuntimeBuildpathEntry(entry, configuration);
 		default:
 			break;
 		}
 		return new IRuntimeBuildpathEntry[] { entry };
 	}
 
 	/**
 	 * Returns resolved entries for the given entry in the context of the given
 	 * script project. If the entry is of kind <code>CONTAINER</code>, container
 	 * resolvers are consulted.
 	 * <p>
 	 * If the given entry is a container, and a resolver is not registered,
 	 * resolved runtime classpath entries are calculated from the associated
 	 * container classpath entries, in the context of the given project.
 	 * </p>
 	 * 
 	 * @param entry
 	 *            runtime classpath entry
 	 * @param project
 	 *            Script project context
 	 * @return resolved runtime classpath entry
 	 * @exception CoreException
 	 *                if unable to resolve
 	 * @see IRuntimeBuildpathEntryResolver
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry[] resolveRuntimeBuildpathEntry(
 			IRuntimeBuildpathEntry entry, IScriptProject project)
 			throws CoreException {
 		switch (entry.getType()) {
 		case IRuntimeBuildpathEntry.CONTAINER:
 			IRuntimeBuildpathEntryResolver resolver = getContainerResolver(entry
 					.getContainerName());
 			if (resolver == null) {
 				return computeDefaultContainerEntries(entry, project);
 			}
 			return resolver.resolveRuntimeBuildpathEntry(entry, project);
 		case IRuntimeBuildpathEntry.OTHER:
 			resolver = getContributedResolver(((IRuntimeBuildpathEntry2) entry)
 					.getTypeId());
 			return resolver.resolveRuntimeBuildpathEntry(entry, project);
 		default:
 			break;
 		}
 		return new IRuntimeBuildpathEntry[] { entry };
 	}
 
 	/**
 	 * Computes and returns the unresolved build path for the given launch
 	 * configuration. Variable and container entries are unresolved.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return unresolved runtime buildpath entries
 	 * @exception CoreException
 	 *                if unable to compute the buildpath
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry[] computeUnresolvedRuntimeBuildpath(
 			ILaunchConfiguration configuration) throws CoreException {
 		return getBuildpathProvider(configuration).computeUnresolvedBuildpath(
 				configuration);
 	}
 
 	/**
 	 * Resolves the given buildpath, returning the resolved buildpath in the
 	 * context of the given launch configuration.
 	 * 
 	 * @param entries
 	 *            unresolved buildpath
 	 * @param configuration
 	 *            launch configuration
 	 * @return resolved runtime buildpath entries
 	 * @exception CoreException
 	 *                if unable to compute the buildpath
 	 * 
 	 */
 	public static IRuntimeBuildpathEntry[] resolveRuntimeBuildpath(
 			IRuntimeBuildpathEntry[] entries, ILaunchConfiguration configuration)
 			throws CoreException {
 		return getBuildpathProvider(configuration).resolveBuildpath(entries,
 				configuration);
 	}
 
 	/**
 	 * Returns all registered runtime buildpath entry resolvers.
 	 */
 	private static Map getEntryResolvers() {
 		if (fgRuntimeBuildpathEntryResolvers == null) {
 			initializeResolvers();
 		}
 		return fgRuntimeBuildpathEntryResolvers;
 	}
 
 	/**
 	 * Returns the resolver registered for the given contributed buildpath entry
 	 * type.
 	 * 
 	 * @param typeId
 	 *            the id of the contributed buildpath entry
 	 * @return the resolver registered for the given buildpath entry
 	 */
 	private static IRuntimeBuildpathEntryResolver getContributedResolver(
 			String typeId) {
 		IRuntimeBuildpathEntryResolver resolver = (IRuntimeBuildpathEntryResolver) getEntryResolvers()
 				.get(typeId);
 		if (resolver == null) {
 			return new DefaultEntryResolver();
 		}
 		return resolver;
 	}
 
 	/**
 	 * Computes the default application buildpath entries for the given project.
 	 * 
 	 * @param jproject
 	 *            The project to compute the buildpath for
 	 * @return The computed buildpath. May be empty, but not null.
 	 * @throws CoreException
 	 *             if unable to compute the default buildpath
 	 */
 	public static String[] computeDefaultRuntimeClassPath(
 			IScriptProject jproject) throws CoreException {
 		IRuntimeBuildpathEntry[] unresolved = computeUnresolvedRuntimeBuildpath(jproject);
 		// 1. remove bootpath entries
 		// 2. resolve & translate to local file system paths
 		List resolved = new ArrayList(unresolved.length);
 		for (int i = 0; i < unresolved.length; i++) {
 			IRuntimeBuildpathEntry entry = unresolved[i];
 			if (entry.getBuildpathProperty() == IRuntimeBuildpathEntry.USER_ENTRY) {
 				IRuntimeBuildpathEntry[] entries = resolveRuntimeBuildpathEntry(
 						entry, jproject);
 				for (int j = 0; j < entries.length; j++) {
 					String location = entries[j].getLocation();
 					if (location != null) {
 						resolved.add(location);
 					}
 				}
 			}
 		}
 		return (String[]) resolved.toArray(new String[resolved.size()]);
 	}
 
 	public static ISourceContainer[] getSourceContainers(
 			IRuntimeBuildpathEntry[] resolved) {
 		return ScriptSourceLookupUtil.translate(resolved);
 	}
 
 	public static IRuntimeBuildpathEntry[] computeUnresolvedSourceBuildpath(
 			ILaunchConfiguration configuration) throws CoreException {
 		return getScriptpathProvider(configuration).computeUnresolvedBuildpath(
 				configuration);
 	}
 
 }
