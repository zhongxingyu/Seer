 package com.aptana.rdt;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IPreferencesService;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.osgi.framework.BundleContext;
 import org.rubypeople.rdt.internal.core.RubyModelManager.EclipsePreferencesListener;
 import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
 
 import com.aptana.rdt.core.gems.Gem;
 import com.aptana.rdt.core.gems.GemListener;
 import com.aptana.rdt.core.gems.IGemManager;
 import com.aptana.rdt.internal.core.gems.GemManager;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class AptanaRDTPlugin extends Plugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "com.aptana.rdt";
 	
     // Preferences
     public HashSet optionNames = new HashSet(20);
     public Hashtable<String, String> optionsCache;
 	
     public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
 	
 	static final int PREF_INSTANCE = 0;
     static final int PREF_DEFAULT = 1;
 
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 */
 	public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID + ".compiler.problem.unusedParameter"; //$NON-NLS-1$
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_UNUSED_PRIVATE_MEMBER = PLUGIN_ID + ".compiler.problem.unusedPrivateMember"; //$NON-NLS-1$
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_UNNECESSARY_ELSE = PLUGIN_ID + ".compiler.problem.unnecessaryElse"; //$NON-NLS-1$
 
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_LOCAL_MASKS_METHOD = PLUGIN_ID + ".compiler.problem.localVariableMasksMethod"; //$NON-NLS-1$
 		
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MISSPELLED_CONSTRUCTOR = PLUGIN_ID + ".compiler.problem.misspelledConstructor"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT = PLUGIN_ID + ".compiler.problem.possibleAccidentalBooleanAssignment"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_CODE_COMPLEXITY_BRANCHES = PLUGIN_ID + ".compiler.problem.codeComplexityBranches"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_CODE_COMPLEXITY_LINES = PLUGIN_ID + ".compiler.problem.codeComplexityLines"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_CODE_COMPLEXITY_RETURNS = PLUGIN_ID + ".compiler.problem.codeComplexityReturns"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_CODE_COMPLEXITY_LOCALS = PLUGIN_ID + ".compiler.problem.codeComplexityLocals"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_CODE_COMPLEXITY_ARGUMENTS = PLUGIN_ID + ".compiler.problem.codeComplexityArguments"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MAX_LOCALS = PLUGIN_ID + ".compiler.problem.maxLocals"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MAX_RETURNS = PLUGIN_ID + ".compiler.problem.maxReturns"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MAX_BRANCHES = PLUGIN_ID + ".compiler.problem.maxBranches"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MAX_LINES = PLUGIN_ID + ".compiler.problem.maxLines"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_MAX_ARGUMENTS = PLUGIN_ID + ".compiler.problem.maxArguments"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_SIMILAR_VARIABLE_NAMES = PLUGIN_ID + ".compiler.problem.similarVariableNames"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 0.9.0
 	 */
 	public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
 		
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
 	public static final String COMPILER_PB_COMPARABLE_MISSING_METHOD = PLUGIN_ID + ".compiler.problem.comparableMissingMethod"; //$NON-NLS-1$
 	
 	/**
 	 * Possible  configurable option ID.
 	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
 	public static final String COMPILER_PB_ENUMERABLE_MISSING_METHOD = PLUGIN_ID + ".compiler.problem.enumerableMissingMethod"; //$NON-NLS-1$
 	
 	/**
  	 * Possible  configurable option ID.
  	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
 	public static final String COMPILER_PB_SUBCLASS_DOESNT_CALL_SUPER = PLUGIN_ID + ".compiler.problem.subclassDoesntCallSuper"; //$NON-NLS-1$
 
 	/**
  	 * Possible  configurable option ID.
  	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
 	public static final String COMPILER_PB_ASSIGNMENT_PRECEDENCE = PLUGIN_ID + ".compiler.problem.assignmentPrecedence"; //$NON-NLS-1$
 
 	/**
  	 * Possible  configurable option ID.
  	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
 	public static final String COMPILER_PB_CONSTANT_NAMING_CONVENTION = PLUGIN_ID + ".compiler.problem.constantNamingConvention"; //$NON-NLS-1$
 
 	/**
  	 * Possible  configurable option ID.
  	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
	public static final String COMPILER_PB_METHOD_MISSING_NO_RESPOND_TO = PLUGIN_ID + "com.aptana.rdt.compiler.problem.methodMissingWithoutRespondTo";
 	
 	/**
  	 * Possible  configurable option ID.
  	 * @see #getDefaultOptions()
 	 * @since 1.0.0
 	 */
	public static final String COMPILER_PB_DYNAMIC_VARIABLE_ALIASES_LOCAL = PLUGIN_ID + "com.aptana.rdt.compiler.problem.dynamicVariableAliasesLocal";
 	
 	// The shared instance
 	private static AptanaRDTPlugin plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public AptanaRDTPlugin() {
 		super();
 		plugin = this;
 	}
 	
 	private static class RubyDebugGemListener extends Job {
 
 		private GemListener listener;
 
 		public RubyDebugGemListener(GemListener listener) {
 			super("Removing temporary gem listener");
 			this.listener = listener;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			GemManager.getInstance().removeGemListener(listener);
 			return Status.OK_STATUS;
 		}
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		
 		initializePreferences();
 		context.registerService(IGemManager.class.getName(), getGemManager(), null);
 		
 
 		getGemManager().addGemListener(new GemListener() {
 		
 			public void managerInitialized() {
 				if (getGemManager().gemInstalled("ruby-debug-ide")) {
 					setRubyDebugUp();
 				}				
 			}
 		
 			private void setRubyDebugUp() {
 				setRubyDebugAsDefault();
 				removeListener(this);					
 			}
 			
 			public void gemsRefreshed() {
 				if (getGemManager().gemInstalled("ruby-debug-ide")) {
 					setRubyDebugUp();
 				}					
 			}
 		
 			public void gemRemoved(Gem gem) {
 				// ignore		
 			}
 		
 			public void gemAdded(Gem gem) {
 				// ignore		
 			}
 		
 		});
 	}
 	
 	private void initializePreferences() {
 		 // Create lookups
         preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
         preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);
 
         // Listen to instance preferences node removal from parent in order to refresh stored one
         IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
             public void added(IEclipsePreferences.NodeChangeEvent event) {
                 // do nothing
             }
             public void removed(IEclipsePreferences.NodeChangeEvent event) {
                 if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
                     preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
                     preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
                 }
             }
         };
         ((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
         preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
 
         // Listen to default preferences node removal from parent in order to refresh stored one
         listener = new IEclipsePreferences.INodeChangeListener() {
             public void added(IEclipsePreferences.NodeChangeEvent event) {
                 // do nothing
             }
             public void removed(IEclipsePreferences.NodeChangeEvent event) {
                 if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
                     preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);
                 }
             }
         };
         ((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
 	}
 
 	protected void setRubyDebugAsDefault() {
 		LaunchingPlugin.getDefault().getPluginPreferences().setValue(org.rubypeople.rdt.internal.launching.PreferenceConstants.USE_RUBY_DEBUG, true);
 	}
 	
 	protected void removeListener(GemListener listener) {
 		Job job = new RubyDebugGemListener(listener);
 		job.schedule();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static AptanaRDTPlugin getDefault() {
 		return plugin;
 	}
 	
 	public static File getFileInPlugin(IPath path) {
 		try {
 			URL installURL = new URL(
 					getDefault().getBundle().getEntry("/"), path.toString()); //$NON-NLS-1$
 			URL localURL = FileLocator.toFileURL(installURL);
 			return new File(localURL.getFile());
 		} catch (IOException ioe) {
 			return null;
 		}
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, getPluginId(), -1, AptanaRDTMessages.RubyRedPlugin_internal_error, e)); 
 	}
 	
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 		
 	public static String getPluginId() {
 		return PLUGIN_ID;
 	}
 
 	public Hashtable<String, String> getOptions() {
 
         // return cached options if already computed
 //        if (this.optionsCache != null) return new Hashtable<String, String>(this.optionsCache);
 
         // init
         Hashtable<String, String> options = new Hashtable<String, String>(10);
         IPreferencesService service = Platform.getPreferencesService();
 
         // set options using preferences service lookup
         Iterator iterator = optionNames.iterator();
         while (iterator.hasNext()) {
             String propertyName = (String) iterator.next();
             String propertyValue = service.get(propertyName, null, this.preferencesLookup);
             if (propertyValue != null) {
                 options.put(propertyName, propertyValue);
             }
         }
         
         // store built map in cache
         this.optionsCache = new Hashtable<String, String>(options);
 
         // return built map
         return options;
     }
 	
 	public IGemManager getGemManager() {
 		return GemManager.getInstance();
 	}
 }
