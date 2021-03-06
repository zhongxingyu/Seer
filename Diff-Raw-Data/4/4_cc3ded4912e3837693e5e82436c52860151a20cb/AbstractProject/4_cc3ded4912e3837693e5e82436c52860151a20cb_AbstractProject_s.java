 package hudson.model;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Launcher.LocalLauncher;
 import hudson.maven.MavenModule;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Fingerprint.RangeSet;
 import hudson.model.RunMap.Constructor;
 import hudson.scm.NullSCM;
 import hudson.scm.SCM;
 import hudson.scm.SCMS;
 import hudson.triggers.Trigger;
 import hudson.triggers.Triggers;
 import hudson.util.EditDistance;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Vector;
 
 /**
  * Base implementation of {@link Job}s that build software.
  *
  * For now this is primarily the common part of {@link Project} and {@link MavenModule}.
  *
  * @author Kohsuke Kawaguchi
  * @see AbstractBuild
  */
 public abstract class AbstractProject<P extends AbstractProject<P,R>,R extends AbstractBuild<P,R>> extends Job<P,R> {
 
     private SCM scm = new NullSCM();
 
     /**
      * All the builds keyed by their build number.
      */
     protected transient /*almost final*/ RunMap<R> builds = new RunMap<R>();
 
     /**
      * The quiet period. Null to delegate to the system default.
      */
     private Integer quietPeriod = null;
 
     /**
      * If this project is configured to be only built on a certain node,
      * this value will be set to that node. Null to indicate the affinity
      * with the master node.
      *
      * see #canRoam
      */
     private String assignedNode;
 
     /**
      * True if this project can be built on any node.
      *
      * <p>
      * This somewhat ugly flag combination is so that we can migrate
      * existing Hudson installations nicely.
      */
     private boolean canRoam;
 
     /**
      * True to suspend new builds.
      */
     protected boolean disabled;
 
     /**
      * Identifies {@link JDK} to be used.
      * Null if no explicit configuration is required.
      *
      * <p>
      * Can't store {@link JDK} directly because {@link Hudson} and {@link Project}
      * are saved independently.
      *
      * @see Hudson#getJDK(String)
      */
     private String jdk;
 
     private boolean enableRemoteTrigger = false;
 
     private String authToken = null;
 
     /**
      * List of all {@link Trigger}s for this project.
      */
     protected List<Trigger> triggers = new Vector<Trigger>();
 
     protected AbstractProject(ItemGroup parent, String name) {
         super(parent,name);
 
         if(!Hudson.getInstance().getSlaves().isEmpty()) {
             // if a new job is configured with Hudson that already has slave nodes
             // make it roamable by default
             canRoam = true;
         }
     }
 
     @Override
     public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
         super.onLoad(parent, name);
 
         this.builds = new RunMap<R>();
         this.builds.load(this,new Constructor<R>() {
             public R create(File dir) throws IOException {
                 return loadBuild(dir);
             }
         });
 
         if(triggers==null)
             // it didn't exist in < 1.28
             triggers = new Vector<Trigger>();
         for (Trigger t : triggers)
             t.start(this,false);
     }
 
     /**
      * If this project is configured to be always built on this node,
      * return that {@link Node}. Otherwise null.
      */
     public Node getAssignedNode() {
         if(canRoam)
             return null;
 
         if(assignedNode ==null)
             return Hudson.getInstance();
         return Hudson.getInstance().getSlave(assignedNode);
     }
 
     /**
      * Gets the directory where the module is checked out.
      */
     public abstract FilePath getWorkspace();
 
     /**
      * Returns the root directory of the checked-out module.
      * <p>
      * This is usually where <tt>pom.xml</tt>, <tt>build.xml</tt>
      * and so on exists. 
      */
     public FilePath getModuleRoot() {
         return getScm().getModuleRoot(getWorkspace());
     }
 
     public int getQuietPeriod() {
         return quietPeriod!=null ? quietPeriod : Hudson.getInstance().getQuietPeriod();
     }
 
     // ugly name because of EL
     public boolean getHasCustomQuietPeriod() {
         return quietPeriod!=null;
     }
 
     public final boolean isBuildable() {
         return true;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     /**
      * Schedules a build of this project.
      */
     public void scheduleBuild() {
         if(!disabled)
             Hudson.getInstance().getQueue().add(this);
     }
 
     /**
      * Returns true if the build is in the queue.
      */
     @Override
     public boolean isInQueue() {
         return Hudson.getInstance().getQueue().contains(this);
     }
 
     public JDK getJDK() {
         return Hudson.getInstance().getJDK(jdk);
     }
 
     /**
      * Overwrites the JDK setting.
      */
     public synchronized void setJDK(JDK jdk) throws IOException {
         this.jdk = jdk.getName();
         save();
     }
 
     public boolean isEnableRemoteTrigger() {
         // no need to enable this option if security disabled
         return (Hudson.getInstance().isUseSecurity())
                 && enableRemoteTrigger;
     }
 
     public String getAuthToken() {
             return authToken;
     }
 
     public SortedMap<Integer, ? extends R> _getRuns() {
         return builds.getView();
     }
 
     public void removeRun(R run) {
         this.builds.remove(run);
     }
 
     /**
      * Creates a new build of this project for immediate execution.
      */
     protected abstract R newBuild() throws IOException;
 
     /**
      * Loads an existing build record from disk.
      */
     protected abstract R loadBuild(File dir) throws IOException;
 
     /**
      * Gets the {@link Node} where this project was last built on.
      *
      * @return
      *      null if no information is available (for example,
      *      if no build was done yet.)
      */
     public Node getLastBuiltOn() {
         // where was it built on?
         AbstractBuild b = getLastBuild();
         if(b==null)
             return null;
         else
             return b.getBuiltOn();
     }
 
     public boolean checkout(AbstractBuild build, Launcher launcher, BuildListener listener, File changelogFile) throws IOException {
         if(scm==null)
             return true;    // no SCM
 
         try {
             FilePath workspace = getWorkspace();
             workspace.mkdirs();
 
             return scm.checkout(build, launcher, workspace, listener, changelogFile);
         } catch (InterruptedException e) {
             e.printStackTrace(listener.fatalError("SCM check out aborted"));
             return false;
         }
     }
 
     /**
      * Checks if there's any update in SCM, and returns true if any is found.
      *
      * <p>
      * The caller is responsible for coordinating the mutual exclusion between
      * a build and polling, as both touches the workspace.
      */
     public boolean pollSCMChanges( TaskListener listener ) {
         if(scm==null) {
             listener.getLogger().println("No SCM");
             return false;   // no SCM
         }
 
         try {
             FilePath workspace = getWorkspace();
             if(!workspace.exists()) {
                 // no workspace. build now, or nothing will ever be built
                 listener.getLogger().println("No workspace is available, so can't check for updates.");
                 listener.getLogger().println("Scheduling a new build to get a workspace.");
                 return true;
             }
 
             // TODO: do this by using the right slave
             return scm.pollChanges(this, new LocalLauncher(listener), workspace, listener );
         } catch (IOException e) {
             e.printStackTrace(listener.fatalError(e.getMessage()));
             return false;
         } catch (InterruptedException e) {
             e.printStackTrace(listener.fatalError("SCM polling aborted"));
             return false;
         }
     }
 
     public SCM getScm() {
         return scm;
     }
 
     public void setScm(SCM scm) {
         this.scm = scm;
     }
 
     /**
      * Adds a new {@link Trigger} to this {@link Project} if not active yet.
      */
     public void addTrigger(Trigger trigger) throws IOException {
         addToList(trigger,triggers);
     }
 
     public void removeTrigger(Descriptor<Trigger> trigger) throws IOException {
         removeFromList(trigger,triggers);
     }
 
     protected final synchronized <T extends Describable<T>>
     void addToList( T item, List<T> collection ) throws IOException {
         for( int i=0; i<collection.size(); i++ ) {
             if(collection.get(i).getDescriptor()==item.getDescriptor()) {
                 // replace
                 collection.set(i,item);
                 save();
                 return;
             }
         }
         // add
         collection.add(item);
         save();
     }
 
     protected final synchronized <T extends Describable<T>>
     void removeFromList(Descriptor<T> item, List<T> collection) throws IOException {
         for( int i=0; i< collection.size(); i++ ) {
             if(collection.get(i).getDescriptor()==item) {
                 // found it
                 collection.remove(i);
                 save();
                 return;
             }
         }
     }
 
     public synchronized Map<Descriptor<Trigger>,Trigger> getTriggers() {
         return Descriptor.toMap(triggers);
     }
 
 //
 //
 // fingerprint related
 //
 //
     /**
      * True if the builds of this project produces {@link Fingerprint} records.
      */
     public abstract boolean isFingerprintConfigured();
 
     /**
      * Gets the other {@link AbstractProject}s that should be built
      * when a build of this project is completed.
      */
     public final List<AbstractProject> getDownstreamProjects() {
         return Hudson.getInstance().getDependencyGraph().getDownstream(this);
     }
 
     public final List<AbstractProject> getUpstreamProjects() {
         return Hudson.getInstance().getDependencyGraph().getUpstream(this);
     }
 
     /**
      * Gets the dependency relationship map between this project (as the source)
      * and that project (as the sink.)
      *
      * @return
      *      can be empty but not null. build number of this project to the build
      *      numbers of that project.
      */
     public SortedMap<Integer, RangeSet> getRelationship(AbstractProject that) {
         TreeMap<Integer,RangeSet> r = new TreeMap<Integer,RangeSet>(REVERSE_INTEGER_COMPARATOR);
 
         checkAndRecord(that, r, this.getBuilds());
         // checkAndRecord(that, r, that.getBuilds());
 
         return r;
     }
 
     /**
      * Helper method for getDownstreamRelationship.
      *
      * For each given build, find the build number range of the given project and put that into the map.
      */
     private void checkAndRecord(AbstractProject that, TreeMap<Integer, RangeSet> r, Collection<R> builds) {
         for (R build : builds) {
             RangeSet rs = build.getDownstreamRelationship(that);
             if(rs==null || rs.isEmpty())
                 continue;
 
             int n = build.getNumber();
 
             RangeSet value = r.get(n);
             if(value==null)
                 r.put(n,rs);
             else
                 value.add(rs);
         }
     }
 
     /**
      * Builds the dependency graph.
      * @see DependencyGraph
      */
     protected abstract void buildDependencyGraph(DependencyGraph graph);
 
 //
 //
 // actions
 //
 //
     /**
      * Schedules a new build command.
      */
     public void doBuild( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
         if (authorizedToStartBuild(req, rsp)) {
             scheduleBuild();
             rsp.forwardToPreviousPage(req);
         }
     }
 
     private boolean authorizedToStartBuild(StaplerRequest req, StaplerResponse rsp) throws IOException {

         if (isEnableRemoteTrigger()) {
             String providedToken = req.getParameter("token");
             if (providedToken != null && providedToken.equals(getAuthToken())) {
                 return true;
             }
         }
 
         return Hudson.adminCheck(req, rsp);
     }
 
     /**
      * Cancels a scheduled build.
      */
     public void doCancelQueue( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
         if(!Hudson.adminCheck(req,rsp))
             return;
 
         Hudson.getInstance().getQueue().cancel(this);
         rsp.forwardToPreviousPage(req);
     }
 
     public synchronized void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         super.doConfigSubmit(req, rsp);
 
         disabled = req.getParameter("disable")!=null;
 
         jdk = req.getParameter("jdk");
         if(req.getParameter("hasCustomQuietPeriod")!=null) {
             quietPeriod = Integer.parseInt(req.getParameter("quiet_period"));
         } else {
             quietPeriod = null;
         }
 
         if(req.getParameter("hasSlaveAffinity")!=null) {
             canRoam = false;
             assignedNode = req.getParameter("slave");
             if(assignedNode !=null) {
                 if(Hudson.getInstance().getSlave(assignedNode)==null) {
                     assignedNode = null;   // no such slave
                 }
             }
         } else {
             canRoam = true;
             assignedNode = null;
         }
 
         if (req.getParameter("pseudoRemoteTrigger") != null) {
             authToken = req.getParameter("authToken");
             enableRemoteTrigger = true;
         } else {
             enableRemoteTrigger = false;
         }
 
         try {
             setScm(SCMS.parseSCM(req));
 
             for (Trigger t : triggers)
                 t.stop();
             buildDescribable(req, Triggers.TRIGGERS, triggers, "trigger");
             for (Trigger t : triggers)
                 t.start(this,true);
         } catch (FormException e) {
             throw new ServletException(e);
         }
     }
 
     protected final <T extends Describable<T>> void buildDescribable(StaplerRequest req, List<Descriptor<T>> descriptors, List<T> result, String prefix)
         throws FormException {
 
         result.clear();
         for( int i=0; i< descriptors.size(); i++ ) {
             if(req.getParameter(prefix +i)!=null) {
                 T instance = descriptors.get(i).newInstance(req);
                 result.add(instance);
             }
         }
     }
 
     /**
      * Serves the workspace files.
      */
     public void doWs( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
         FilePath ws = getWorkspace();
         if(!ws.exists()) {
             // if there's no workspace, report a nice error message
             rsp.forward(this,"noWorkspace",req);
         } else {
             new DirectoryBrowserSupport(this).serveFile(req, rsp, ws, "folder.gif", true);
         }
     }
 
     /**
      * Finds a {@link AbstractProject} that has the name closest to the given name.
      */
     public static AbstractProject findNearest(String name) {
         List<AbstractProject> projects = Hudson.getInstance().getAllItems(AbstractProject.class);
         String[] names = new String[projects.size()];
         for( int i=0; i<projects.size(); i++ )
             names[i] = projects.get(i).getName();
 
         String nearest = EditDistance.findNearest(name, names);
         return (AbstractProject)Hudson.getInstance().getItem(nearest);
     }
 
     private static final Comparator<Integer> REVERSE_INTEGER_COMPARATOR = new Comparator<Integer>() {
         public int compare(Integer o1, Integer o2) {
             return o2-o1;
         }
     };
 }
