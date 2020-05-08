 package net.praqma.jenkins.plugin.monkit;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.praqma.monkit.MonKit;
 import net.praqma.monkit.MonKitCategory;
 import net.praqma.monkit.MonKitException;
 import net.praqma.monkit.MonKitObservation;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Hudson;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 
 public class MonKitPublisher extends Recorder {
 
 	private String monKitFile = "monkit.xml";
 	public static final MonKitFilenameFilter __MONKIT_FILENAME_FILTER = new MonKitFilenameFilter();
 	private boolean onlyStable;
 	private boolean disableLegend = false;
 	
     private List<MonKitTarget> targets;
 	
     @DataBoundConstructor 
     public MonKitPublisher( String monKitFile, boolean onlyStable, boolean disableLegend ) {
         this.monKitFile = monKitFile;
         this.onlyStable = onlyStable;
         this.disableLegend = disableLegend;
         
         targets = new ArrayList<MonKitTarget>();
     }
    
    @Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 		
		String version = Hudson.getInstance().getPlugin( "monkit-plugin" ).getWrapper().getVersion();
 		listener.getLogger().println( "MonKit Plugin version " + version );
 		
         final FilePath[] moduleRoots = build.getModuleRoots();
         final boolean multipleModuleRoots = moduleRoots != null && moduleRoots.length > 1;
         final FilePath moduleRoot = multipleModuleRoots ? build.getWorkspace() : build.getModuleRoot();
                
         FilePath[] reports = new FilePath[0];
                 
         try {
             reports = moduleRoot.list(monKitFile);
 
             // if the build has failed, then there's not
             // much point in reporting an error
             if (build.getResult().isWorseOrEqualTo(Result.FAILURE) && reports.length == 0)
                 return true;
 
         } catch (IOException e) {
             Util.displayIOException(e, listener);
             e.printStackTrace(listener.fatalError("Unable to find MonKit files"));
             build.setResult(Result.FAILURE);
         }
         
         List<MonKit> mks = new ArrayList<MonKit>();
         
         for (int i = 0; i < reports.length; i++) {
         	try {
 				MonKit mke = MonKit.fromString( reports[i].readToString() );
 				mks.add(mke);
 			} catch (MonKitException e) {
                 e.printStackTrace(listener.fatalError("Unable to get " + reports[i].getName() + ". Skipping"));
 			}
         }
         
         MonKit mk = MonKit.merge(mks);
 
         final MonKitBuildAction mka = new MonKitBuildAction( build, mk.getCategories() );
         mka.setPublisher(this);
         build.getActions().add(mka);
         
         Case worst = getWorst(mk.getCategories());
         
         if(worst.health == null) {
         	build.setResult(Result.UNSTABLE);
         }
 		
 		return true;
 	}
 	
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 	
     private static class MonKitFilenameFilter implements FilenameFilter {
         public boolean accept(File dir, String name) {
             return name.matches("^monkit.*\\.xml$");
         }
     }
     
     public boolean getOnlyStable() {
         return onlyStable;
     }
     
     public String getMonKitFile() {
     	return monKitFile;
     }
     
     public boolean isOnlyStable() {
     	return onlyStable;
     }
     
     public boolean isDisableLegend() {
     	return disableLegend;
     }
     
     public List<MonKitTarget> getTargets() {
     	return targets;
     }
     
     public MonKitTarget getTarget( String category ) {
     	for( MonKitTarget mkt : getTargets() ) {
     		if( mkt.getCategory().equals( category ) ) {
     			return mkt;
     		}
     	}
     	
     	return null;
     }
     
     public MonKitCategory getMonKitCategory( List<MonKitCategory> monkit, MonKitTarget mkt ) {
 		for( MonKitCategory mkc : monkit ) {
 			if( mkt.getCategory().equalsIgnoreCase(mkc.getName()) ) {
 				return mkc;
 			}
 		}
 		
 		return null;
     }
     
     private void setTargets( List<MonKitTarget> targets ) {
     	this.targets.clear();
     	this.targets = targets;
     }
     
     public class Case {
     	Float health = 100f;
     	String category = "";
     	String name = "";
     	
     	public Case() {}
     	
     	public Case( Float health, String category, String name ) {
     		this.health = health;
     		this.category = category;
     		this.name = name;
     	}
     }
     
     public Case getWorst( List<MonKitCategory> monkit ) {
 		//float worst = 100f;
 		//String worstStr = "Unknown";
 		boolean healthy = true;
 		
 		Case worst = new Case();
 		
 		/* Only consider latest build */
 		for( MonKitTarget mkt : getTargets() ) {
 			
 			for( MonKitCategory mkc : monkit ) {
 				
 				/* We got the correct category */
 				if( mkt.getCategory().equalsIgnoreCase(mkc.getName()) ) {
 					
 					/* Loop the observations */
 					for( MonKitObservation mko : mkc ) {
 						
 						/* Calculate health */
 						Float f = new Float( mko.getValue() );
 						
 						Float fu = new Float( mkt.getUnstable() );
 						Float fh = new Float( mkt.getHealthy() );
 						
 						boolean isGreater = fu < fh;
 						
 						//System.out.println( "F=" + f + ". FU=" + fu + ". FH=" + fh + ". ISGREATER=" + isGreater );
 						
 						/* Mark build as unstable */
 						if( ( isGreater && f < fu ) || ( !isGreater && f > fu ) ) {
 							return new Case( null, mkc.getName(), mko.getName() );
 						}
 						
 						if( ( isGreater && f < fh ) || (  !isGreater && f > fh ) ) {
 							float diff = fh - fu;
 							float nf1 = f - fu;
 							float inter = ( nf1 / diff ) * 100;
 							
 							//System.out.println( "DIFF=" + diff + ". NF1=" + nf1 + ". INTER=" +  inter );
 							
 							//System.out.println( "INTER=" +  inter );
 							
 							if( inter < worst.health ) {
 								worst.health = inter;
 								//worstStr = mkc.getName() + " for " + mko.getName();
 								worst.category = mkc.getName();
 								worst.name = mko.getName();
 							}
 							healthy = false;
 						}
 					}
 				}
 			}
 		}
 		
 		if( healthy ) {
 			return new Case( 100f, null, null );
 		} else {
 			return worst;
 		}
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public Action getProjectAction(AbstractProject<?, ?> project) {
         return new MonKitProjectAction(project, getOnlyStable());
     }
     
     @Extension
     public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 		public String getDisplayName() {
 			return "MonKit Report";
 		}
 
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
 			return true;
 		}
 		
         @Override
         public MonKitPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
         	MonKitPublisher instance = req.bindJSON(MonKitPublisher.class, formData);
             List<MonKitTarget> targets = req.bindParametersToList(MonKitTarget.class, "monkit.target.");
             instance.setTargets(targets);
             return instance;
         }
         
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             req.bindParameters(this, "monkit.");
             save();
             return super.configure(req, formData);
         }
         
         public List<MonKitTarget> getDefaultTargets() {
             List<MonKitTarget> result = new ArrayList<MonKitTarget>();
             return result;
         }
         
         public List<MonKitTarget> getTargets(MonKitPublisher instance) {
             if (instance == null) {
                 return getDefaultTargets();
             }
             
             return instance.getTargets();
         }
     }
 
 }
