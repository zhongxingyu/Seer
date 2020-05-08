 /*
  * Created on Jan 14, 2005
  *
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.PerformanceStats;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IPreferencesService;
 import org.rubypeople.rdt.core.ILoadpathEntry;
 import org.rubypeople.rdt.core.IParent;
 import org.rubypeople.rdt.core.IProblemRequestor;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.WorkingCopyOwner;
 import org.rubypeople.rdt.core.parser.IProblem;
 import org.rubypeople.rdt.internal.core.buffer.BufferManager;
 import org.rubypeople.rdt.internal.core.builder.RubyBuilder;
 
 /**
  * @author cawilliams
  * 
  */
 public class RubyModelManager {
 
     private static final String BUFFER_MANAGER_DEBUG = RubyCore.PLUGIN_ID + "/debug/buffermanager"; //$NON-NLS-1$
     private static final String RUBYMODEL_DEBUG = RubyCore.PLUGIN_ID + "/debug/rubymodel"; //$NON-NLS-1$
     private static final String DELTA_DEBUG = RubyCore.PLUGIN_ID + "/debug/rubydelta"; //$NON-NLS-1$
     private static final String DELTA_DEBUG_VERBOSE = RubyCore.PLUGIN_ID
             + "/debug/rubydelta/verbose"; //$NON-NLS-1$
     private static final String POST_ACTION_DEBUG = RubyCore.PLUGIN_ID + "/debug/postaction"; //$NON-NLS-1$
     private static final String BUILDER_DEBUG = RubyCore.PLUGIN_ID + "/debug/builder"; //$NON-NLS-1$
 
     private static final String ENABLE_NEW_FORMATTER = RubyCore.PLUGIN_ID + "/formatter/enable_new"; //$NON-NLS-1$
 
     public static final String DELTA_LISTENER_PERF = RubyCore.PLUGIN_ID + "/perf/rubydeltalistener"; //$NON-NLS-1$
     public static final String RECONCILE_PERF = RubyCore.PLUGIN_ID + "/perf/reconcile"; //$NON-NLS-1$
 
     /**
      * The singleton manager
      */
     private final static RubyModelManager MANAGER = new RubyModelManager();
 
     /**
      * Holds the state used for delta processing.
      */
     public DeltaProcessingState deltaState = new DeltaProcessingState();
 
     /**
      * Unique handle onto the RubyModel
      */
     final RubyModel rubyModel = new RubyModel();
 
     /*
      * Temporary cache of newly opened elements
      */
     private ThreadLocal temporaryCache = new ThreadLocal();
 
     /**
      * Set of elements which are out of sync with their buffers.
      */
     protected Map elementsOutOfSynchWithBuffers = new HashMap(11);
 
     /*
      * A HashSet that contains the IJavaProject whose classpath is being
      * resolved.
      */
     private ThreadLocal classpathsBeingResolved = new ThreadLocal();
 
     /**
      * Infos cache.
      */
     protected RubyModelCache cache = new RubyModelCache();
 
     /**
      * Table from IProject to PerProjectInfo. NOTE: this object itself is used
      * as a lock to synchronize creation/removal of per project infos
      */
     protected Map perProjectInfos = new HashMap(5);
 
     /**
      * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy
      * handle) to PerWorkingCopyInfo. NOTE: this object itself is used as a lock
      * to synchronize creation/removal of per working copy infos
      */
     protected Map perWorkingCopyInfos = new HashMap(5);
 
     private static boolean verbose = false;
     public static boolean CP_RESOLVE_VERBOSE = false;
 
     // Preferences
     HashSet optionNames = new HashSet(20);
     Hashtable optionsCache;
 
     public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
     static final int PREF_INSTANCE = 0;
     static final int PREF_DEFAULT = 1;
 
     /**
      * Constructs a new RubyModelManager
      */
     private RubyModelManager() {
         // singleton: prevent others from creating a new instance
     }
 
     /**
      * Returns the singleton RubyModelManager
      */
     public final static RubyModelManager getRubyModelManager() {
         return MANAGER;
     }
 
     /**
      * Returns the info for the element.
      */
     public synchronized Object getInfo(IRubyElement element) {
         HashMap tempCache = (HashMap) this.temporaryCache.get();
         if (tempCache != null) {
             Object result = tempCache.get(element);
             if (result != null) { return result; }
         }
         return this.cache.getInfo(element);
     }
 
     /*
      * Removes all cached info for the given element (including all children)
      * from the cache. Returns the info for the given element, or null if it was
      * closed.
      */
     public synchronized Object removeInfoAndChildren(RubyElement element) throws RubyModelException {
         Object info = this.cache.peekAtInfo(element);
         if (info != null) {
             element.closing(info);
             if (element instanceof IParent && info instanceof RubyElementInfo) {
                 IRubyElement[] children = ((RubyElementInfo) info).getChildren();
                 for (int i = 0, size = children.length; i < size; ++i) {
                     RubyElement child = (RubyElement) children[i];
                     child.close();
                 }
             }
             this.cache.removeInfo(element);
             return info;
         }
         return null;
     }
 
     /**
      * Returns the info for this element without disturbing the cache ordering.
      */
     protected synchronized Object peekAtInfo(IRubyElement element) {
         HashMap tempCache = (HashMap) this.temporaryCache.get();
         if (tempCache != null) {
             Object result = tempCache.get(element);
             if (result != null) { return result; }
         }
         return this.cache.peekAtInfo(element);
     }
 
     /*
      * Puts the infos in the given map (keys are IRubyElements and values are
      * RubyElementInfos) in the Ruby model cache in an atomic way. First checks
      * that the info for the opened element (or one of its ancestors) has not
      * been added to the cache. If it is the case, another thread has opened the
      * element (or one of its ancestors). So returns without updating the cache.
      */
     protected synchronized void putInfos(IRubyElement openedElement, Map newElements) {
         // remove children
         Object existingInfo = this.cache.peekAtInfo(openedElement);
         if (openedElement instanceof IParent && existingInfo instanceof RubyElementInfo) {
             IRubyElement[] children = ((RubyElementInfo) existingInfo).getChildren();
             for (int i = 0, size = children.length; i < size; ++i) {
                 RubyElement child = (RubyElement) children[i];
                 try {
                     child.close();
                 } catch (RubyModelException e) {
                     // ignore
                 }
             }
         }
 
         Iterator iterator = newElements.keySet().iterator();
         while (iterator.hasNext()) {
             IRubyElement element = (IRubyElement) iterator.next();
             Object info = newElements.get(element);
             this.cache.putInfo(element, info);
         }
     }
 
     /**
      * Returns the temporary cache for newly opened elements for the current
      * thread. Creates it if not already created.
      */
     public HashMap getTemporaryCache() {
         HashMap result = (HashMap) this.temporaryCache.get();
         if (result == null) {
             result = new HashMap();
             this.temporaryCache.set(result);
         }
         return result;
     }
 
     /*
      * Returns whether there is a temporary cache for the current thread.
      */
     public boolean hasTemporaryCache() {
         return this.temporaryCache.get() != null;
     }
 
     /*
      * Reset project options stored in info cache.
      */
     public void resetProjectOptions(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
             if (info != null) {
                 info.options = null;
             }
         }
     }
 
     /*
      * Reset project preferences stored in info cache.
      */
     public void resetProjectPreferences(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
             if (info != null) {
                 info.preferences = null;
             }
         }
     }
 
     /*
      * Resets the temporary cache for newly created elements to null.
      */
     public void resetTemporaryCache() {
         this.temporaryCache.set(null);
     }
 
     /**
      * Returns the handle to the active Ruby Model.
      */
     public final RubyModel getRubyModel() {
         return this.rubyModel;
     }
 
     public static class PerWorkingCopyInfo implements IProblemRequestor {
 
         int useCount = 0;
         IRubyScript workingCopy;
         private IProblemRequestor problemRequestor;
 
         public PerWorkingCopyInfo(IRubyScript workingCopy, IProblemRequestor problemRequestor) {
             this.workingCopy = workingCopy;
             this.problemRequestor = problemRequestor;
         }
 
         public IRubyScript getWorkingCopy() {
             return this.workingCopy;
         }
 
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             buffer.append("Info for "); //$NON-NLS-1$
             buffer.append(((RubyElement) this.workingCopy).toString());
             buffer.append("\nUse count = "); //$NON-NLS-1$
             buffer.append(this.useCount);
             buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
             buffer.append(this.problemRequestor);
             return buffer.toString();
         }
 
         public void acceptProblem(IProblem problem) {
             if (this.problemRequestor == null) return;
             this.problemRequestor.acceptProblem(problem);
         }
 
         public void beginReporting() {
             if (this.problemRequestor == null) return;
             this.problemRequestor.beginReporting();
         }
 
         public void endReporting() {
             if (this.problemRequestor == null) return;
             this.problemRequestor.endReporting();
         }
 
         public boolean isActive() {
             return this.problemRequestor != null && this.problemRequestor.isActive();
         }
     }
 
     /**
      * @param script
      * @param create
      * @param recordUsage
      * @param problemRequestor
      * @param object
      * @return
      */
     public PerWorkingCopyInfo getPerWorkingCopyInfo(RubyScript workingCopy, boolean create,
             boolean recordUsage, IProblemRequestor problemRequestor) {
         synchronized (this.perWorkingCopyInfos) { // use the
             // perWorkingCopyInfo
             // collection as its own
             // lock
             WorkingCopyOwner owner = workingCopy.owner;
             Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
             if (workingCopyToInfos == null && create) {
                 workingCopyToInfos = new HashMap();
                 this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
             }
 
             PerWorkingCopyInfo info = workingCopyToInfos == null ? null
                     : (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
             if (info == null && create) {
                 info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
                 workingCopyToInfos.put(workingCopy, info);
             }
             if (info != null && recordUsage) info.useCount++;
             return info;
         }
     }
 
     /*
      * Discards the per working copy info for the given working copy (making it
      * a compilation unit) if its use count was 1. Otherwise, just decrement the
      * use count. If the working copy is primary, computes the delta between its
      * state and the original compilation unit and register it. Close the
      * working copy, its buffer and remove it from the shared working copy
      * table. Ignore if no per-working copy info existed. NOTE: it must NOT be
      * synchronized as it may interact with the element info cache (if useCount
      * is decremented to 0), see bug 50667. Returns the new use count (or -1 if
      * it didn't exist).
      */
     public int discardPerWorkingCopyInfo(RubyScript workingCopy) throws RubyModelException {
         PerWorkingCopyInfo info = null;
         synchronized (this.perWorkingCopyInfos) {
             WorkingCopyOwner owner = workingCopy.owner;
             Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
             if (workingCopyToInfos == null) return -1;
 
             info = (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
             if (info == null) return -1;
 
             if (--info.useCount == 0) {
                 // remove per working copy info
                 workingCopyToInfos.remove(workingCopy);
                 if (workingCopyToInfos.isEmpty()) {
                     this.perWorkingCopyInfos.remove(owner);
                 }
             }
         }
         if (info.useCount == 0) { // info cannot be null here (check was done
             // above)
             // remove infos + close buffer (since no longer working copy)
             // outside the perWorkingCopyInfos lock (see bug 50667)
             removeInfoAndChildren(workingCopy);
             workingCopy.closeBuffer();
         }
         return info.useCount;
     }
 
     /**
      * Returns the set of elements which are out of synch with their buffers.
      */
     protected Map getElementsOutOfSynchWithBuffers() {
         return this.elementsOutOfSynchWithBuffers;
     }
 
     /*
      * Returns the per-project info for the given project. If specified, create
      * the info if the info doesn't exist.
      */
     public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
             if (info == null && create) {
                 info = new PerProjectInfo(project);
                 this.perProjectInfos.put(project, info);
             }
             return info;
         }
     }
 
     public void removePerProjectInfo(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = (PerProjectInfo) this.perProjectInfos.get(project);
             if (info != null) {
                 this.perProjectInfos.remove(project);
             }
         }
     }
 
     public boolean isLoadpathBeingResolved(IRubyProject project) {
         return getLoadpathBeingResolved().contains(project);
     }
 
     private HashSet getLoadpathBeingResolved() {
         HashSet result = (HashSet) this.classpathsBeingResolved.get();
         if (result == null) {
             result = new HashSet();
             this.classpathsBeingResolved.set(result);
         }
         return result;
     }
 
     public static class PerProjectInfo {
 
         public IProject project;
         public Object savedState;
         public boolean triedRead;
         public ILoadpathEntry[] rawClasspath;
         public ILoadpathEntry[] resolvedClasspath;
         public Map resolvedPathToRawEntries; // reverse map from resolved
         // path to raw entries
         public IPath outputLocation;
 
         public IEclipsePreferences preferences;
         public Hashtable options;
 
         public PerProjectInfo(IProject project) {
 
             this.triedRead = false;
             this.savedState = null;
             this.project = project;
         }
 
         // updating raw classpath need to flush obsoleted cached information
         // about resolved entries
         public synchronized void updateClasspathInformation(ILoadpathEntry[] newRawClasspath) {
 
             this.rawClasspath = newRawClasspath;
             this.resolvedClasspath = null;
             this.resolvedPathToRawEntries = null;
         }
 
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             buffer.append("Info for "); //$NON-NLS-1$
             buffer.append(this.project.getFullPath());
             buffer.append("\nRaw classpath:\n"); //$NON-NLS-1$
             if (this.rawClasspath == null) {
                 buffer.append("  <null>\n"); //$NON-NLS-1$
             } else {
                 for (int i = 0, length = this.rawClasspath.length; i < length; i++) {
                     buffer.append("  "); //$NON-NLS-1$
                     buffer.append(this.rawClasspath[i]);
                     buffer.append('\n');
                 }
             }
             buffer.append("Resolved classpath:\n"); //$NON-NLS-1$
             ILoadpathEntry[] resolvedCP = this.resolvedClasspath;
             if (resolvedCP == null) {
                 buffer.append("  <null>\n"); //$NON-NLS-1$
             } else {
                 for (int i = 0, length = resolvedCP.length; i < length; i++) {
                     buffer.append("  "); //$NON-NLS-1$
                     buffer.append(resolvedCP[i]);
                     buffer.append('\n');
                 }
             }
             buffer.append("Output location:\n  "); //$NON-NLS-1$
             if (this.outputLocation == null) {
                 buffer.append("<null>"); //$NON-NLS-1$
             } else {
                 buffer.append(this.outputLocation);
             }
             return buffer.toString();
         }
     }
 
     /*
      * Returns the per-project info for the given project. If the info doesn't
      * exist, check for the project existence and create the info. @throws
      * RubyModelException if the project doesn't exist.
      */
     public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
             throws RubyModelException {
         RubyModelManager.PerProjectInfo info = getPerProjectInfo(project, false /*
                                                                                  * don't
                                                                                  * create
                                                                                  * info
                                                                                  */);
         if (info == null) {
             if (!RubyProject.hasRubyNature(project)) { throw ((RubyProject) RubyCore
                     .create(project)).newNotPresentException(); }
             info = getPerProjectInfo(project, true /* create info */);
         }
         return info;
     }
 
     public void setLoadpathBeingResolved(IRubyProject project, boolean classpathIsResolved) {
         if (classpathIsResolved) {
             getLoadpathBeingResolved().add(project);
         } else {
             getLoadpathBeingResolved().remove(project);
         }
     }
 
     public static void setVerbose(boolean verbose) {
         RubyModelManager.verbose = verbose;
     }
 
     public static boolean isVerbose() {
         return verbose;
     }
 
     public String getOption(String optionName) {
         if (RubyCore.CORE_ENCODING.equals(optionName)) { return RubyCore.getEncoding(); }
         String propertyName = optionName;
         if (this.optionNames.contains(propertyName)) {
             IPreferencesService service = Platform.getPreferencesService();
             String value = service.get(optionName, null, this.preferencesLookup);
             return value == null ? null : value.trim();
         }
         return null;
     }
 
     public Hashtable getOptions() {
 
         // return cached options if already computed
         if (this.optionsCache != null) return new Hashtable(this.optionsCache);
 
         // init
         Hashtable options = new Hashtable(10);
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
 
         // get encoding through resource plugin
         options.put(RubyCore.CORE_ENCODING, RubyCore.getEncoding());
 
         // store built map in cache
         this.optionsCache = new Hashtable(options);
 
         // return built map
         return options;
     }
 
     public DeltaProcessor getDeltaProcessor() {
         return this.deltaState.getDeltaProcessor();
     }
 
     public void startup() throws CoreException {
         try {
             configurePluginDebugOptions();
 
             // request state folder creation (workaround 19885)
             RubyCore.getPlugin().getStateLocation();
 
             // Listen to preference changes
             Preferences.IPropertyChangeListener propertyListener = new Preferences.IPropertyChangeListener() {
 
                 public void propertyChange(Preferences.PropertyChangeEvent event) {
                     RubyModelManager.this.optionsCache = null;
                 }
             };
             RubyCore.getPlugin().getPluginPreferences().addPropertyChangeListener(propertyListener);
 
             final IWorkspace workspace = ResourcesPlugin.getWorkspace();
             workspace.addResourceChangeListener(this.deltaState,
             /*
              * update spec in
              * JavaCore#addPreProcessingResourceChangedListener(...) if adding
              * more event types
              */
             IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD
                     | IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE
                     | IResourceChangeEvent.PRE_CLOSE);
 
         } catch (RuntimeException e) {
             shutdown();
             throw e;
         }
     }
 
     public void shutdown() {
         RubyCore javaCore = RubyCore.getRubyCore();
         javaCore.savePluginPreferences();
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         workspace.removeResourceChangeListener(this.deltaState);
         workspace.removeSaveParticipant(javaCore);
 
         // wait for the initialization job to finish
         try {
             Platform.getJobManager().join(RubyCore.PLUGIN_ID, null);
         } catch (InterruptedException e) {
             // ignore
         }
 
         // Note: no need to close the Java model as this just removes Java
         // element infos from the Java model cache
     }
 
     /**
      * Configure the plugin with respect to option settings defined in
      * ".options" file
      */
     public void configurePluginDebugOptions() {
         if (RubyCore.getPlugin().isDebugging()) {
             String option = Platform.getDebugOption(BUFFER_MANAGER_DEBUG);
             if (option != null) BufferManager.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(BUILDER_DEBUG);
             if (option != null) RubyBuilder.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(DELTA_DEBUG);
             if (option != null) DeltaProcessor.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(DELTA_DEBUG_VERBOSE);
             if (option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(RUBYMODEL_DEBUG);
             if (option != null) RubyModelManager.verbose = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(POST_ACTION_DEBUG);
             if (option != null)
                 RubyModelOperation.POST_ACTION_VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             // configure performance options
             if (PerformanceStats.ENABLED) {
                 DeltaProcessor.PERF = PerformanceStats.isEnabled(DELTA_LISTENER_PERF);
                 ReconcileWorkingCopyOperation.PERF = PerformanceStats.isEnabled(RECONCILE_PERF);
             }
         }
 
     }
 
 }
