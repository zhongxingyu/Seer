 /*
  * The MIT License
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Brian Westrich, Martin Eigenbrodt
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.downstream_ext;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixProject;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.DependecyDeclarer;
 import hudson.model.DependencyGraph;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.Items;
 import hudson.model.Project;
 import hudson.model.Result;
 import hudson.model.listeners.ItemListener;
 import hudson.plugins.downstream_ext.DownstreamTrigger.DescriptorImpl.ItemListenerImpl;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.BuildTrigger;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Triggers builds of other projects.
  *
  * This class was inspired by {@link BuildTrigger} (rev. 21890) -
  * but has changed significantly in the mean time.
  */
 @SuppressWarnings("unchecked")
 public class DownstreamTrigger extends Notifier implements DependecyDeclarer {
 
     private static final Logger LOGGER = Logger.getLogger(DownstreamTrigger.class.getName());
     
     /**
      * Comma-separated list of other projects to be scheduled.
      */
     private String childProjects;
 
     /**
      * Threshold status to trigger other builds.
      */
     private Result threshold = Result.SUCCESS;
     
     /**
      * Defines how the result {@link #threshold} should
      * be evaluated.
      * @since 1.3
      */
     private Strategy thresholdStrategy;
     
     
     private final boolean onlyIfSCMChanges;
 
     @DataBoundConstructor
     public DownstreamTrigger(String childProjects, String threshold, boolean onlyIfSCMChanges,
             String strategy) {
         this(childProjects, resultFromString(threshold), onlyIfSCMChanges, Strategy.valueOf(strategy));
     }
 
     public DownstreamTrigger(String childProjects, Result threshold, boolean onlyIfSCMChanges,
             Strategy strategy) {
         if(childProjects==null)
             throw new IllegalArgumentException();
         this.childProjects = childProjects;
         this.threshold = threshold;
         this.onlyIfSCMChanges = onlyIfSCMChanges;
         this.thresholdStrategy = strategy;
     }
     
     private static Result resultFromString(String s) {
     	Result result = Result.fromString(s);
     	// fromString returns FAILURE for unknown strings instead of
     	// IllegalArgumentException. Don't know why the author thought that this
     	// is useful ...
     	if (!result.toString().equals(s)) {
     		throw new IllegalArgumentException("Unknown result type '" + s + "'");
     	}
     	return result;
     }
 
     public String getChildProjectsValue() {
         return childProjects;
     }
 
     public Result getThreshold() {
         if(threshold==null)
             return Result.SUCCESS;
         else
             return threshold;
     }
     
     public boolean isOnlyIfSCMChanges() {
     	return onlyIfSCMChanges;
     }
 
     public List<AbstractProject> getChildProjects() {
         return Items.fromNameList(childProjects,AbstractProject.class);
     }
     
     public Strategy getStrategy() {
         return this.thresholdStrategy;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.NONE;
     }
     
     @Override
 	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
     	// nothing to do here. Everything happens in buildDependencyGraph
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
     	for (AbstractProject downstream : getChildProjects()) {
     		graph.addDependency(new DownstreamDependency(owner, downstream, this));
     	}
     	
     	// workaround for problems with Matrixprojects
     	// see http://issues.hudson-ci.org/browse/HUDSON-5508
     	if (owner instanceof MatrixProject) {
     		MatrixProject proj = (MatrixProject) owner;
     		Collection<MatrixConfiguration> activeConfigurations = proj.getActiveConfigurations();
     		for (MatrixConfiguration conf : activeConfigurations) {
     			for (AbstractProject downstream : getChildProjects()) {
     	    		graph.addDependency(new DownstreamDependency(conf, downstream, this));
     	    	}
     		}
     	}
     }
 
     @Override
     public boolean needsToRunAfterFinalized() {
         return true;
     }
 
     /**
      * Called from {@link ItemListenerImpl} when a job is renamed.
      *
      * @return true
      *      if this {@link DownstreamTrigger} is changed and needs to be saved.
      */
     public boolean onJobRenamed(String oldName, String newName) {
         // quick test
         if(!childProjects.contains(oldName))
             return false;
 
         boolean changed = false;
 
         // we need to do this per string, since old Project object is already gone.
         String[] projects = childProjects.split(",");
         for( int i=0; i<projects.length; i++ ) {
             if(projects[i].trim().equals(oldName)) {
                 projects[i] = newName;
                 changed = true;
             }
         }
 
         if(changed) {
             StringBuilder b = new StringBuilder();
             for (String p : projects) {
                 if(b.length()>0)    b.append(',');
                 b.append(p);
             }
             childProjects = b.toString();
         }
 
         return changed;
     }
 
     private Object readResolve() {
         if (thresholdStrategy == null) {
         	// set to the single strategy used in downstream-ext <= 1.2
             thresholdStrategy = Strategy.AND_HIGHER;
         }
         return this;
     }
 
     @Extension
    // for some reason when running mvn from commandline the build fails,
    // if BuildTrigger is not fully qualified here!?
    public static class DescriptorImpl extends hudson.tasks.BuildTrigger.DescriptorImpl {
     	
     	public static final String[] THRESHOLD_VALUES = {
     		Result.SUCCESS.toString(), Result.UNSTABLE.toString(),
     		Result.FAILURE.toString(), Result.ABORTED.toString()
     	};
     	
     	public static final Strategy[] STRATEGY_VALUES = Strategy.values();
     	
         @Override
 		public String getDisplayName() {
             return hudson.plugins.downstream_ext.Messages.DownstreamTrigger_DisplayName();
         }
 
         @Override
         public String getHelpFile() {
             return "/plugin/downstream-ext/help.html";
         }
 
         @Override
         public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return new DownstreamTrigger(
                 formData.getString("childProjects"),
                 formData.getString("threshold"),
                 formData.has("onlyIfSCMChanges") && formData.getBoolean("onlyIfSCMChanges"),
                 formData.getString("strategy"));
         }
 
         @Extension
         public static class ItemListenerImpl extends ItemListener {
             @Override
             public void onRenamed(Item item, String oldName, String newName) {
                 // update DownstreamTrigger of other projects that point to this object.
                 // can't we generalize this?
                 for( Project<?,?> p : Hudson.getInstance().getProjects() ) {
                     DownstreamTrigger t = p.getPublishersList().get(DownstreamTrigger.class);
                     if(t!=null) {
                         if(t.onJobRenamed(oldName,newName)) {
                             try {
                                 p.save();
                             } catch (IOException e) {
                                 LOGGER.log(Level.WARNING, "Failed to persist project setting during rename from "+oldName+" to "+newName,e);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public enum Strategy {
         AND_HIGHER("equal or over") {
 			@Override
 			public boolean evaluate(Result threshold, Result actualResult) {
 				return actualResult.isBetterOrEqualTo(threshold);
 			}
         },
         EXACT("equal") {
 			@Override
 			public boolean evaluate(Result threshold, Result actualResult) {
 				return actualResult.equals(threshold);
 			}
 		},
         AND_LOWER("equal or under") {
 			@Override
 			public boolean evaluate(Result threshold, Result actualResult) {
 				return actualResult.isWorseOrEqualTo(threshold);
 			}
 		};
         
         public final String displayName;
 
         Strategy(String displayName) {
             this.displayName = displayName;
         }
         
         public String getDisplayName() {
             return this.displayName;
         }
         
         public abstract boolean evaluate(Result threshold, Result actualResult);
     }
 }
