 package hudson.matrix;
 
 import hudson.FilePath;
 import hudson.model.AbstractProject;
 import hudson.model.DependencyGraph;
 import hudson.model.Descriptor;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.ItemGroup;
 import static hudson.model.ItemGroupMixIn.loadChildren;
 import hudson.model.JDK;
 import hudson.model.Label;
 import hudson.model.Node;
 import hudson.model.SCMedItem;
 import hudson.model.TopLevelItem;
 import hudson.model.TopLevelItemDescriptor;
 import hudson.tasks.BuildStep;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrappers;
 import hudson.tasks.Builder;
 import hudson.tasks.Publisher;
 import hudson.util.CopyOnWriteMap;
 import hudson.util.Function1;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class MatrixProject extends AbstractProject<MatrixProject,MatrixBuild> implements TopLevelItem, SCMedItem, ItemGroup<MatrixConfiguration> {
     /**
      * Other configuration axes.
      *
      * This also includes special axis "label" and "jdk" if they are configured.
      */
     private volatile AxisList axes = new AxisList();
 
     /**
      * List of active {@link Builder}s configured for this project.
      */
     private volatile List<Builder> builders = new Vector<Builder>();
 
     /**
      * List of active {@link Publisher}s configured for this project.
      */
     private volatile List<Publisher> publishers = new Vector<Publisher>();
 
     /**
      * List of active {@link BuildWrapper}s configured for this project.
      */
     private volatile List<BuildWrapper> buildWrappers = new Vector<BuildWrapper>();
 
     /**
      * All {@link MatrixConfiguration}s, keyed by their {@link MatrixConfiguration#getName() names}.
      */
     private transient /*final*/ Map<Combination,MatrixConfiguration> configurations = new CopyOnWriteMap.Tree<Combination,MatrixConfiguration>();
 
     /**
      * @see #getActiveConfigurations()
      */
     private transient /*final*/ Set<MatrixConfiguration> activeConfigurations = new LinkedHashSet<MatrixConfiguration>();
 
     public MatrixProject(String name) {
         super(Hudson.getInstance(), name);
     }
 
     public AxisList getAxes() {
         return axes;
     }
 
     /**
      * Gets the subset of {@link AxisList} that are not system axes.
      */
     public List<Axis> getUserAxes() {
         List<Axis> r = new ArrayList<Axis>();
         for (Axis a : axes)
             if(!a.isSystem())
                 r.add(a);
         return r;
     }
 
     public Layouter<MatrixConfiguration> getLayouter() {
         return new Layouter<MatrixConfiguration>(axes) {
             protected MatrixConfiguration getT(Combination c) {
                 return getItem(c);
             }
         };
     }
 
     public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
         super.onLoad(parent,name);
         Collections.sort(axes); // perhaps the file was edited on disk and the sort order might have been broken
 
         rebuildConfigurations();
     }
 
     /**
      * Rebuilds the {@link #configurations} list and {@link #activeConfigurations}.
      */
     private void rebuildConfigurations() throws IOException {
         configurations = loadChildren(this,getConfigurationsDir(), new Function1<Combination, MatrixConfiguration>() {
             public Combination call(MatrixConfiguration mc) { return mc.getCombination(); } });
 
         // find all active configurations
         Set<MatrixConfiguration> active = new LinkedHashSet<MatrixConfiguration>();
         for (Combination c : axes.list()) {
             MatrixConfiguration config = configurations.get(c);
             if(config==null) {
                 config = new MatrixConfiguration(this,c);
                 config.save();
                 configurations.put(config.getCombination(), config);
             }
             active.add(config);
         }
         this.activeConfigurations = active;
     }
 
     private File getConfigurationsDir() {
         return new File(getRootDir(),"configurations");
     }
 
     /**
      * Gets all active configurations.
      * <p>
      * In contract, inactive configurations are those that are left for archival purpose
      * and no longer built when a new {@link MatrixBuild} is executed.
      */
     public Collection<MatrixConfiguration> getActiveConfigurations() {
         return activeConfigurations;
     }
 
     public Collection<MatrixConfiguration> getItems() {
         return configurations.values();
     }
 
     public String getUrlChildPrefix() {
         return ".";
     }
 
     public MatrixConfiguration getItem(String name) {
         return getItem(Combination.fromString(name));
     }
 
     public MatrixConfiguration getItem(Combination c) {
         return configurations.get(c);
     }
 
     public File getRootDirFor(MatrixConfiguration child) {
         return new File(getConfigurationsDir(),child.getName());
     }
 
     public Hudson getParent() {
         return Hudson.getInstance();
     }
 
     /**
      * @see #getJDKs()
      */
     @Override @Deprecated
     public JDK getJDK() {
         return super.getJDK();
     }
 
     /**
      * Gets the {@link JDK}s where the builds will be run.
      * @return never null but can be empty
      */
     public Set<JDK> getJDKs() {
         Axis a = axes.find("jdk");
         if(a==null)  return Collections.emptySet();
         Set<JDK> r = new HashSet<JDK>();
         for (String j : a) {
             JDK jdk = Hudson.getInstance().getJDK(j);
             if(jdk!=null)
                 r.add(jdk);
         }
         return r;
     }
 
     /**
      * Gets the {@link Label}s where the builds will be run.
      * @return never null
      */
     public Set<Label> getLabels() {
         Axis a = axes.find("label");
         if(a==null) return Collections.emptySet();
 
         Set<Label> r = new HashSet<Label>();
         for (String l : a)
             r.add(Hudson.getInstance().getLabel(l));
         return r;
     }
 
     public Map<Descriptor<Builder>,Builder> getBuilders() {
         return Descriptor.toMap(builders);
     }
 
     public Map<Descriptor<Publisher>,Publisher> getPublishers() {
         return Descriptor.toMap(publishers);
     }
 
     public Map<Descriptor<BuildWrapper>,BuildWrapper> getBuildWrappers() {
         return Descriptor.toMap(buildWrappers);
     }
 
     public Publisher getPublisher(Descriptor<Publisher> descriptor) {
         for (Publisher p : publishers) {
             if(p.getDescriptor()==descriptor)
                 return p;
         }
         return null;
     }
 
     @Override
     public FilePath getWorkspace() {
         Node node = getLastBuiltOn();
         if(node==null)  node = getParent();
         return node.getWorkspaceFor(this);
     }
 
     protected Class<MatrixBuild> getBuildClass() {
         return MatrixBuild.class;
     }
 
     public boolean isFingerprintConfigured() {
         return false;
     }
 
     protected void buildDependencyGraph(DependencyGraph graph) {
         // TODO: perhaps support downstream build triggering
     }
 
     public MatrixProject asProject() {
         return this;
     }
 
     public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        MatrixConfiguration item = getItem(token);
        if(item!=null)
             return item;
         return super.getDynamic(token,req,rsp);
     }
 
     protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
         super.submit(req, rsp);
 
         AxisList newAxes = new AxisList();
 
         // parse user axes
         newAxes.addAll(req.bindParametersToList(Axis.class,"axis."));
         // get rid of empty values
         for (Iterator<Axis> itr = newAxes.iterator(); itr.hasNext();) {
             Axis a = itr.next();
             if(a.values.isEmpty())  itr.remove();
         }
 
         // parse system axes
         newAxes.add(Axis.parsePrefixed(req,"jdk"));
         if(req.getParameter("multipleNodes")!=null)
             newAxes.add(Axis.parsePrefixed(req,"label"));
         this.axes = newAxes;
 
         buildWrappers = buildDescribable(req, BuildWrappers.WRAPPERS, "wrapper");
         builders = buildDescribable(req, BuildStep.BUILDERS, "builder");
         publishers = buildDescribable(req, BuildStep.PUBLISHERS, "publisher");
 
         rebuildConfigurations();
     }
 
     public DescriptorImpl getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends TopLevelItemDescriptor {
         private DescriptorImpl() {
             super(MatrixProject.class);
         }
 
         public String getDisplayName() {
             return "Build multi-configuration project";
         }
 
         public MatrixProject newInstance(String name) {
             return new MatrixProject(name);
         }
     }
 }
