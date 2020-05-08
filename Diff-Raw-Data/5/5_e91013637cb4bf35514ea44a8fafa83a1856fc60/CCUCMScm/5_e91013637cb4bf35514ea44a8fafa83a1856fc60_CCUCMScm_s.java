 package net.praqma.hudson.scm;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.Cool;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Project.Plevel;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
 import net.praqma.clearcase.ucm.utils.BaselineList;
 import net.praqma.hudson.Config;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.hudson.remoting.Util;
 import net.praqma.hudson.scm.Polling.PollingType;
 import net.praqma.hudson.scm.CCUCMState.State;
 import net.praqma.hudson.scm.StoredBaselines.StoredBaseline;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 import net.praqma.util.structure.Tuple;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.export.Exported;
 
 /**
  *  is responsible for everything regarding Hudsons connection to ClearCase
  * pre-build. This class defines all the files required by the user. The
  * information can be entered on the config page.
  *
  * @author Troels Selch
  * @author Margit Bennetzen
  *
  */
 public class CCUCMScm extends SCM {
 
     private String levelToPoll;
     private String loadModule;
     private String component;
     private String stream;
     private boolean newest;
     private String bl;
     private StringBuffer pollMsgs = new StringBuffer();
     private Stream integrationstream;
     private boolean doPostBuild = true;
     private String buildProject;
     private boolean multiSite = false;
     private String jobName = "";
     private Integer jobNumber;
     private String id = "";
     private Logger logger = null;
     public static CCUCMState ccucm = new CCUCMState();
     
     private Unstable treatUnstable;
     
     private boolean createBaseline;
     private String versionFrom;
     private String buildnumberMajor;
     private String buildnumberMinor;
     private String buildnumberPatch;
     private String buildnumberSequenceSelector;
 
     /* Threshold in milliseconds */
     public static final long __CCUCM_STORED_BASELINES_THRESHOLD = 5 * 60 * 1000;
     public static StoredBaselines storedBaselines = new StoredBaselines();
     public static final String CCUCM_LOGGER_STRING = "include_classes";
     private Polling polling;
     
     private String viewtag = "";
 
     /**
      * Default constructor, mainly used for unit tests.
      */
     public CCUCMScm() {
     }
     
 
     /**
      * 
      * @param component
      * @param levelToPoll
      * @param loadModule
      * @param newest
      * @param polling
      * @param stream
      * @param versionFrom
      * @param buildnumberMajor
      * @param buildnumberMinor
      * @param buildnumberPatch
      * @param buildnumberSequenceSelector
      * @param buildProject
      * @param multiSite
      */
    @DataBoundConstructor
    public CCUCMScm(String component, String levelToPoll, String loadModule, boolean newest, String polling, String stream, String treatUnstable
 		   /* Baseline creation */, boolean createBaseline, String versionFrom, String buildnumberMajor, String buildnumberMinor, String buildnumberPatch, String buildnumberSequenceSelector
 		   /* Build options     */, String buildProject, boolean multiSite  ) {
 
        /* Preparing the logger */
        logger = PraqmaLogger.getLogger();
        logger.subscribeAll();
        File rdir = new File(logger.getPath());
        logger.setLocalLog(new File(rdir + System.getProperty("file.separator") + "log.log"));
 
        /* Make sure the cool library is also affected */
        Cool.setLogger(logger);
 
        this.logger = PraqmaLogger.getLogger();
        logger.trace_function();
        logger.debug("CCUCMSCM constructor");
        this.component = component;
        this.levelToPoll = levelToPoll;
        this.loadModule = loadModule;
        this.stream = stream;
        this.newest = newest;
        this.buildProject = buildProject;
        this.multiSite = multiSite;
 
        this.polling = new Polling(polling);
        this.treatUnstable = new Unstable( treatUnstable );
        
        this.createBaseline = createBaseline;
        this.versionFrom = versionFrom;
        this.buildnumberMajor = buildnumberMajor;
        this.buildnumberMinor = buildnumberMinor;
        this.buildnumberPatch = buildnumberPatch;
        this.buildnumberSequenceSelector = buildnumberSequenceSelector;
 
    }
 
     @Override
     public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile)
             throws IOException, InterruptedException {
         /* Prepare job variables */
 
         jobName = build.getParent().getDisplayName().replace(' ', '_');
         jobNumber = build.getNumber();
         this.id = "[" + jobName + "::" + jobNumber + "]";
 
 
         /* Preparing the logger */
         logger = PraqmaLogger.getLogger();
         logger.subscribeAll();
         File rdir = build.getRootDir();
         logger.setLocalLog(new File(rdir + System.getProperty("file.separator") + "log.log"));
 
         /* Make sure the cool library is also affected */
         Cool.setLogger(logger);
 
         // logger.print(bl.getFullyQualifiedName());
 
         logger.info(id + "CCUCMSCM checkout v. " + net.praqma.hudson.Version.version);
         boolean result = true;
 
         PrintStream consoleOutput = listener.getLogger();
         consoleOutput.println("[" + Config.nameShort + "] ClearCase UCM Plugin version " + net.praqma.hudson.Version.version );
         
         /* Recalculate the states */
         int count = ccucm.recalculate(build.getProject());
         logger.info(id + "Removed " + count + " from states.");
 
         doPostBuild = true;
 
         /* If we polled, we should get the same object created at that point */
         State state = ccucm.getState(jobName, jobNumber);
         state.setLoadModule(loadModule);
         storeStateParameters( state );
         
         logger.debug(id + "The initial state:\n" + state.stringify());
 
         state.setLogger(logger);
 
         if (this.multiSite) {
             /* Get the time in milli seconds and store it to the state */
             state.setMultiSiteFrequency(((CCUCMScmDescriptor) getDescriptor()).getMultiSiteFrequencyAsInt() * 60000);
             logger.info(id + "Multi site frequency: " + state.getMultiSiteFrquency());
         } else {
             state.setMultiSiteFrequency(0);
         }
         
         /* Store baseline creation information */
         BaselineInformation bi         = new BaselineInformation();
         bi.createBaseline              = this.createBaseline;
         bi.buildnumberMajor            = this.buildnumberMajor;
         bi.buildnumberMinor            = this.buildnumberMinor;
         bi.buildnumberPatch            = this.buildnumberPatch;
         bi.versionFrom                 = this.versionFrom;
         bi.buildnumberSequenceSelector = this.buildnumberSequenceSelector;
         
         state.setBaselineInformation(bi);
         
 
         /* Determining the Baseline modifier */
         String baselineInput = getBaselineValue( build );
 
         /* The special Baseline case */
         if (build.getBuildVariables().get(baselineInput) != null) {
             logger.debug( "BASELINE: " + baselineInput );
             polling = new Polling( PollingType.none );
             result = doBaseline( build, baselineInput, state, listener );
         } else {
         	consoleOutput.println("[" + Config.nameShort + "] Polling streams: " + polling.toString());
        		result = pollStream( build, state, listener );
         }
         
         state.setPolling(polling);
         
         /* If a baseline is found */
         if (state.getBaseline() != null && result ) {
         	consoleOutput.println("[" + Config.nameShort + "] Using " + state.getBaseline());
         	build.setDescription("<small>" + state.getBaseline() + "</small>");
         	
             if( polling.isPollingSelf() || !polling.isPolling() ) {
             	result = bla( build, workspace, changelogFile, listener, state );
             } else {
             	/* Only start deliver when NOT polling self */
             	result = beginDeliver( build, state, listener, changelogFile );
             }
             
         }
         
         consoleOutput.println( "[" + Config.nameShort + "] Pre build steps done" );
 
         return result;
     }
     
     private boolean bla( AbstractBuild<?, ?> build, FilePath workspace, File changelogFile, BuildListener listener, State state ) {
 
     	PrintStream consoleOutput = listener.getLogger();
     	
     	EstablishResult er;
         try {
             /* Force the Baseline to be loaded */
             try {
                 state.getBaseline().load();
             } catch (UCMException e) {
                 logger.debug(id + "Could not load Baseline");
                 consoleOutput.println("[" + Config.nameShort + "] Could not load Baseline.");
             }
 
 
             
             CheckoutTask ct = new CheckoutTask(listener, jobName, build.getNumber(), state.getStream().getFullyQualifiedName(), loadModule, state.getBaseline().getFullyQualifiedName(), buildProject, logger);
 
             er = workspace.act(ct);
             String changelog = er.getMessage();
             logger.empty(er.getLog());
             this.viewtag = er.getViewtag();
             
 
             /* Write change log */
             try {
                 FileOutputStream fos = new FileOutputStream(changelogFile);
                 fos.write(changelog.getBytes());
                 fos.close();
             } catch (IOException e) {
                 logger.debug(id + "Could not write change log file");
                 consoleOutput.println("[" + Config.nameShort + "] Could not write change log file");
             }
 
         } catch (Exception e) {
             consoleOutput.println("[" + Config.nameShort + "] An unknown error occured: " + e.getMessage());
             logger.warning(e);
             e.printStackTrace(consoleOutput);
             doPostBuild = false;
             state.setPostBuild(false);
             return false;
         }
         
         if( er == null || er.isFailed() ) {
             consoleOutput.println("[" + Config.nameShort + "] Could not establish workspace: " + er);
             state.setPostBuild(false);
             return false;
         } else {       
         	return true;
         }
     }
     
     public boolean doBaseline( AbstractBuild<?, ?> build, String baselineInput, State state, BuildListener listener ) {
         
         PrintStream consoleOutput = listener.getLogger();
         boolean result = true;
         
         String baselinename = (String) build.getBuildVariables().get(baselineInput);
         try {
             state.setBaseline(UCMEntity.getBaseline(baselinename));
             state.setStream(state.getBaseline().getStream());
             consoleOutput.println("[" + Config.nameShort + "] Starting parameterized build with a CCUCM_baseline." );
             /* The component could be used in the post build section */
             state.setComponent(state.getBaseline().getComponent());
             state.setStream(state.getBaseline().getStream());
             logger.debug(id + "Saving the component for later use");
         } catch (UCMException e) {
             consoleOutput.println("[" + Config.nameShort + "] Could not find baseline from parameter '" + baselinename + "'.");
             state.setPostBuild(false);
             result = false;
             state.setBaseline(null);
         }
         
         return result;
     }
 
     
     public String getBaselineValue( AbstractBuild<?, ?> build ) {
         Collection<?> c = build.getBuildVariables().keySet();
         Iterator<?> i = c.iterator();
 
         while (i.hasNext()) {
             String next = i.next().toString();
             if (next.equalsIgnoreCase("baseline")) {
                 return next;
             }
         }
         
         return null;
     }
     
     public class AscendingDateSort implements Comparator<Baseline>{
 
 		@Override
 		public int compare( Baseline bl1, Baseline bl2 ) {
 			if( bl2.getDate() == null ){
 				return 1;
 			}
 			if( bl1.getDate() == null ){
 				return -1;
 			}
 			return (int) ( ( bl1.getDate().getTime() / 1000 ) - ( bl2.getDate().getTime() / 1000 ) );
 		}
     	
     }
     
     private boolean pollStream( AbstractBuild<?, ?> build, State state, BuildListener listener ) {
         boolean result = true;
         PrintStream consoleOutput = listener.getLogger();
         
         try {
 
             printParameters(consoleOutput);
             state.setStream(UCMEntity.getStream(stream));
             
             if (!state.isAddedByPoller()) {
             	
             	List<Baseline> baselines = null;
             	/* Old skool self polling */
             	if( polling.isPollingSelf() ) {
             		baselines = getValidBaselines(build.getProject(), state, Project.getPlevelFromString(levelToPoll), state.getStream(), state.getComponent());
             	} else {
                     /* Find the Baselines and store them */
                     baselines = getChildStreamBaselines( build.getProject(), consoleOutput, state, state.getStream(), state.getComponent(), polling.isPollingChilds() );
             	}
             	
             	int total = baselines.size();
             	int pruned = filterBaselines( baselines );
             	
             	if( pruned > 0 ) {
             		consoleOutput.println( "[" + Config.nameShort + "] Removed " + pruned + " of " + total + " Baselines." );
             	}
 
                 /* if we did not find any baselines we should return false */
                 if (baselines.size() < 1) {
                     return false;
                 }
                 
                 /* Sort by date */
                 Collections.sort( baselines, new AscendingDateSort() );
                 
                 state.setBaselines(baselines);
                 state.setBaseline(selectBaseline(state.getBaselines(), newest));
             }
             
             if( state.getBaselines() == null || state.getBaselines().size() < 1) {
                 return false;
             }
             
             /* Print the baselines to jenkins out */
             printBaselines(state.getBaselines(), consoleOutput);
             consoleOutput.println( "" );
             
         } catch( Exception e ) {
             consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             logger.warning( e );
             return false;
         }
         
         return result;
     }
     
     public boolean beginDeliver( AbstractBuild<?, ?> build, State state, BuildListener listener, File changelogFile ) {
         FilePath workspace = build.getWorkspace();
         PrintStream consoleOutput = listener.getLogger();
         boolean result = true;
         
         EstablishResult er = new EstablishResult();
     	
         try {
             logger.debug( "Remote delivering...." );
             consoleOutput.println("[" + Config.nameShort + "] Establishing deliver view");
             //RemoteDeliver rmDeliver = new RemoteDeliver(UCMEntity.getStream(stream).getFullyQualifiedName(), listener, component, loadModule, state.getBaseline().getFullyQualifiedName(), build.getParent().getDisplayName());
             RemoteDeliver rmDeliver = new RemoteDeliver(state.getBaseline().getStream().getFullyQualifiedName(), listener, component, loadModule, state.getBaseline().getFullyQualifiedName(), build.getParent().getDisplayName());
 
             er = workspace.act(rmDeliver);
             
             /* Write change log */
             try {
                 FileOutputStream fos = new FileOutputStream(changelogFile);
                 fos.write(er.getMessage().getBytes());
                 fos.close();
             } catch (IOException e) {
                 logger.debug(id + "Could not write change log file");
                 consoleOutput.println("[" + Config.nameShort + "] Could not write change log file");
             }
 
             /*Next line must be after the line above*/
             state.setSnapView(rmDeliver.getSnapShotView());
         } catch (IOException e) {
             consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             result = false;
         } catch (UCMException e) {
             consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             result = false;
         } catch( InterruptedException e ) {
             consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             logger.warning( e );
             result = false;
         }
         
         logger.debug( id + "RESULT: " + er.getResultType().toString() );
         
         /* The result must be false if the returnStatus is not equal to 0 */
         if( er.isFailed() ) {
         	logger.debug( id + "No need for completing deliver" );
         	result = false;
         }
         
         /* If returnStatus == 1|2|3 the deliver was not started and should not be tried cancelled later 
          * Perhaps isFailed could be used? */
        if( er.isDeliverRequiresRebase() || er.isInterprojectDeliverDenied() | er.isMergeError() || !er.isStarted() ) {
         	logger.debug( id + "No need for completing deliver" );
         	state.setNeedsToBeCompleted( false );
         }
         
         consoleOutput.println("[" + Config.nameShort + "] Deliver " + (result ? "succeeded" : "failed"));
         /* If failed, cancel the deliver 
          * But only if the deliver actually started */
         if( !result && er.isCancellable() ) {
             try {
                 consoleOutput.print("[" + Config.nameShort + "] Cancelling deliver. ");
                 Util.completeRemoteDeliver( workspace, listener, state, false );
                 consoleOutput.println("Success");
                 
                 /* Make sure, that the post step is not run */
                 state.setNeedsToBeCompleted( false );
                 
             } catch( Exception e ) {
                 consoleOutput.println("Failed");
                 logger.warning(e);
                 consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             }
         }
         
         try {
 			state.setStream(UCMEntity.getStream(stream));
 		} catch (UCMException e) {
             consoleOutput.println("[" + Config.nameShort + "] " + e.getMessage());
             logger.warning( e );
             result = false;
 		}
         
         return result;
     }
     
     /**
      * Get the {@link Baseline}'s from a {@link Stream}s child Streams.
      * @param build
      * @param consoleOutput
      * @param state
      * @return
      */
     private List<Baseline> getChildStreamBaselines( AbstractProject<?, ?> project, PrintStream consoleOutput, State state, Stream stream, Component component, boolean pollingChildStreams ) {
         
         List<Stream> streams = null;
         List<Baseline> baselines = new ArrayList<Baseline>();
         
         try {
         	if( pollingChildStreams ) {
         		streams = stream.getChildStreams();
         	} else {
         		streams = stream.getSiblingStreams();
         	}
         } catch( UCMException e1 ) {
             logger.warning( "Could not retrieve streams: " + e1.getMessage() );
         }
         
         consoleOutput.println("[" + Config.nameShort + "] Scanning " + streams.size() + " stream" + ( streams.size() == 1 ? "" : "s" ) + " for baselines." );
         
         int c = 1;
         for (Stream s : streams) {
             try {
                 consoleOutput.printf( "[" + Config.nameShort + "] [%02d] %s ", c, s.getShortname() );
                 c++;
                 List<Baseline> found = getValidBaselines(project, state, Project.getPlevelFromString(levelToPoll), s, component);
                 for (Baseline b : found ) {
                     baselines.add(b);
                 }
                 consoleOutput.println( found.size() + " baseline" + ( found.size() == 1 ? "" : "s" ) + " found" );
             } catch (ScmException e) {
                 consoleOutput.println("No baselines found");
             }
         }
         
         consoleOutput.println("");
         
         return baselines;
     }
 
     @Override
     public ChangeLogParser createChangeLogParser() {
         logger.trace_function();
         return new ChangeLogParserImpl();
     }
 
     @Override
     public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
         super.buildEnvVars(build, env);
 
         State state = ccucm.getState(jobName, jobNumber);
 
         if (state.getBaseline() != null) {
             env.put("CC_BASELINE", state.getBaseline().getFullyQualifiedName());
         } else {
             env.put("CC_BASELINE", "");
         }
         
         env.put( "CC_VIEWTAG", viewtag );
         String workspace = env.get( "WORKSPACE" );
         if (workspace != null) {
             env.put("CC_VIEWPATH", workspace + File.separator + viewtag);
         }
     }
 
     /**
      * This method polls the version control system to see if there are any
      * changes in the source code.
      *
      */
     @Override
     public PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener,
             SCMRevisionState rstate) throws IOException, InterruptedException {
         /* Preparing the logger */
         logger = PraqmaLogger.getLogger();
         logger.subscribeAll();
 
         this.id = "[" + project.getDisplayName() + "::" + project.getNextBuildNumber() + "]";
 
         /*
          * Make a state object, which is only temporary, only to determine if
          * there's baselines to build this object will be stored in checkout
          */
         jobName = project.getDisplayName().replace(' ', '_');
         jobNumber = project.getNextBuildNumber(); /*
          * This number is not the
          * final job number
          */
 
         State state = ccucm.getState(jobName, jobNumber);
         
         storeStateParameters( state );
         
         if (this.multiSite) {
             /* Get the time in milli seconds and store it to the state */
             state.setMultiSiteFrequency(((CCUCMScmDescriptor) getDescriptor()).getMultiSiteFrequencyAsInt() * 60000);
             logger.info(id + "Multi site frequency: " + state.getMultiSiteFrquency());
         } else {
             state.setMultiSiteFrequency(0);
         }
 
         PrintStream consoleOut = listener.getLogger();
         printParameters(consoleOut);
         
 
         PollingResult p = null;
         consoleOut.println("[" + Config.nameShort + "] polling streams: " + polling);
         
         try {
 	        List<Baseline> baselines = null;
 	    	/* Old skool self polling */
 	    	if( polling.isPollingSelf() ) {
 	    		
 				baselines = getValidBaselines(project, state, Project.getPlevelFromString(levelToPoll), state.getStream(), state.getComponent());
 
 	    	} else {
 	            /* Find the Baselines and store them */
 	            baselines = getChildStreamBaselines( project, consoleOut, state, state.getStream(), state.getComponent(), polling.isPollingChilds() );
 	    	}
 	            
 	        filterBaselines( baselines );
 	
 	        if( baselines.size() > 0 ) {
 	            p = PollingResult.BUILD_NOW;
 	            
 	            /* Sort by date */
 	            Collections.sort( baselines, new AscendingDateSort() );
 	            
 	            state.setBaselines(baselines);
 	            state.setBaseline(selectBaseline(state.getBaselines(), newest));
 	        } else {
 	            p = PollingResult.NO_CHANGES;
 	        }
 	
 	        logger.debug(id + "FINAL Polling result = " + p.change.toString());
 	
 	        logger.unsubscribeAll();
 	        
 	        logger.debug(id + "The POLL state:\n" + state.stringify());
 	
 	        /* Remove state if not being built */
 	        if( p == PollingResult.NO_CHANGES ) {
 	            state.remove();
 	        }
 	    } catch (ScmException e) {
 			logger.warning( "Could not get any baselines: " + e.getMessage() );
 			p = PollingResult.NO_CHANGES;
 		}
         
         /* Remove state if not being built */
         if( p == PollingResult.NO_CHANGES ) {
             state.remove();
         } else {
         	state.setAddedByPoller(true);
         }
         
         return p;
     }
     
     /**
      * Store the globally defined parameters in the state object
      * @param state
      */
     private void storeStateParameters( State state ) {
         
         try {
             state.setStream( UCMEntity.getStream( stream, false ) );
         } catch( UCMException e ) {
             logger.warning( e );
         }
         
         try {
             state.setComponent( UCMEntity.getComponent( component, false ) );
         } catch( UCMException e ) {
             logger.warning( e );
         }
         
         state.setUnstable( treatUnstable );
         
         /*
         try {
             state.setBaseline( UCMEntity.getBaseline( bl, false ) );
         } catch( UCMException e ) {
             logger.warning( e );
         }
         */
         
         state.setPlevel( Project.getPlevelFromString(levelToPoll) );
     }
 
     /**
      * Determine the valid {@link Baseline}s and return the polling result
      * @param project
      * @param listener
      * @param state
      * @param stream
      * @param component
      * @return
      */
     private PollingResult getPossibleBaselines(AbstractProject<?, ?> project, TaskListener listener, State state, Stream stream, Component component) {
 
         List<Baseline> baselines = null;
         PrintStream consoleOut = listener.getLogger();
         printParameters(consoleOut);
         PollingResult p;
 
         try {
             baselines = getValidBaselines(project, state, Project.getPlevelFromString(levelToPoll), stream, component);
             printBaselines(baselines, consoleOut);
             
             /* Storing possible baselines + the selected baseline */
             state.setBaselines(baselines);
             Baseline baseline = selectBaseline(baselines, newest);
             logger.info(id + "Using " + baseline);
             state.setBaseline(baseline);
 
         } catch (ScmException e) {
             e.printStackTrace(consoleOut);
             consoleOut.println(pollMsgs + "\n[" + Config.nameShort + "] " + e.getMessage());
             pollMsgs = new StringBuffer();
             logger.debug(id + "Removed job " + state.getJobNumber() + " from list");
             state.remove();
         }
 
         if (baselines.size() > 0) {
             p = PollingResult.BUILD_NOW;
         } else {
             p = PollingResult.NO_CHANGES;
         }
 
         logger.debug(id + "FINAL Polling result = " + p.change.toString());
 
         logger.unsubscribeAll();
 
         return p;
 
     }
 
     @Override
     public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException,
             InterruptedException {
         SCMRevisionState scmRS = null;
 
         if (bl != null) {
             scmRS = new SCMRevisionStateImpl();
         }
         return scmRS;
     }
 
     private Baseline selectBaseline(List<Baseline> baselines, boolean newest) {
         if (baselines.size() > 0) {
             if (newest) {
                 return baselines.get(baselines.size() - 1);
             } else {
                 return baselines.get(0);
             }
         } else {
             return null;
         }
     }
 
     /**
      * Given the {@link Stream}, {@link Component} and {@link Plevel} a list of valid {@link Baseline}s is returned.
      * @param project The jenkins project
      * @param state The CCUCM {@link State}
      * @param plevel The {@link Plevel}
      * @param stream {@link Stream}
      * @param component {@link Component}
      * @return A list of {@link Baseline}s
      * @throws ScmException
      */
     private List<Baseline> getValidBaselines(AbstractProject<?, ?> project, State state, Project.Plevel plevel, Stream stream, Component component) throws ScmException {
         logger.debug(id + "Retrieving valid baselines.");
 
         /* The baseline list */
         BaselineList baselines = null;
 
         try {
             baselines = component.getBaselines(stream, plevel);
         } catch (UCMException e) {
             throw new ScmException("Could not retrieve baselines from repository. " + e.getMessage());
         }
         logger.debug(id + "GetBaseline state:\n" + state.stringify());
         List<Baseline> validBaselines = new ArrayList<Baseline>();
         if (baselines.size() >= 1) {
             logger.debug(id + "CCUCM=" + ccucm.stringify());
 
             if (state.isMultiSite()) {
                 /* Prune the stored baselines */
                 int pruned = CCUCMScm.storedBaselines.prune(state.getMultiSiteFrquency());
                 logger.info(id + "I pruned " + pruned + " baselines from cache with threshold "
                         + StoredBaselines.milliToMinute(state.getMultiSiteFrquency()) + "m");
                 logger.debug(id + "My stored baselines:\n" + CCUCMScm.storedBaselines.toString());
             }
 
             try {
                 /* For each baseline in the list */
                 for (Baseline b : baselines) {
                     /* Get the state for the current baseline */
                     State cstate = ccucm.getStateByBaseline(jobName, b.getFullyQualifiedName());
 
                     /* Find the stored baseline if multi site, null if not */
                     StoredBaseline sbl = null;
                     if (state.isMultiSite()) {
                         /* Find the baseline if stored */
                         sbl = CCUCMScm.storedBaselines.getBaseline(b.getFullyQualifiedName());
                         logger.debug(id + "The found stored baseline: " + sbl);
                     }
 
                     /*
                      * The baseline is in progress, determine if the job is
                      * still running
                      */
                     if (cstate != null) {
                         Integer bnum = cstate.getJobNumber();
                         Object o = project.getBuildByNumber(bnum);
                         Build bld = (Build) o;
 
                         /* The job is not running */
                         if (!bld.isLogUpdated()) {
                             logger.debug(id + "Job " + bld.getNumber() + " is not building");
 
                             /*
                              * Verify that the found baseline has the same
                              * promotion as the stored(if stored)
                              */
                             if (sbl == null || sbl.plevel == b.getPromotionLevel(true)) {
                                 logger.debug(id + b + " was added to selected list");
                                 validBaselines.add(b);
                             }
                         } else {
                             logger.debug(id + "Job " + bld.getNumber() + " is building " + cstate.getBaseline().getFullyQualifiedName());
                         }
                     } /* The baseline is available */ else {
                         /*
                          * Verify that the found baseline has the same promotion
                          * as the stored(if stored)
                          */
                         if (sbl == null || sbl.plevel == b.getPromotionLevel(true)) {
                             logger.debug(id + b + " was added to selected list");
                             validBaselines.add(b);
                         }
                     }
                 }
 
                 if (validBaselines.size() == 0) {
                     logger.log(id + "No baselines available on chosen parameters.");
                     throw new ScmException("No baselines available on chosen parameters.");
                 }
 
             } catch (UCMException e) {
                 throw new ScmException("Could not get recommended baselines. " + e.getMessage());
             }
         } else {
             throw new ScmException("No baselines on chosen parameters.");
         }
 
         return validBaselines;
     }
     
     
     /**
      * Filter out baselines that is involved in a deliver or
      * does not have a label
      * @param baselines
      */
     private int filterBaselines( List<Baseline> baselines ) {
     	
     	int pruned = 0;
     	
     	/* Remove deliver baselines */
     	Iterator<Baseline> it = baselines.iterator();
     	while( it.hasNext() ) {
     		Baseline bl = it.next();
     		if( bl.getShortname().startsWith( "deliverbl." ) ) {
     			it.remove();
     			pruned++;
     		}
     	}
     	
     	/* Remove unlabeled baselines */
     	Iterator<Baseline> it2 = baselines.iterator();
     	while( it2.hasNext() ) {
     		Baseline bl = it2.next();
     		if( bl.getLabelStatus().equals( LabelStatus.UNLABLED ) ) {
     			it.remove();
     			pruned++;
     		}
     	}
     	
     	return pruned;
     }
 
     private void printParameters(PrintStream ps) {
         ps.println("[" + Config.nameShort + "] Getting baselines for :");
         ps.println("[" + Config.nameShort + "] * Stream:          " + stream);
         ps.println("[" + Config.nameShort + "] * Component:       " + component);
         ps.println("[" + Config.nameShort + "] * Promotion level: " + levelToPoll);
         ps.println("");
     }
 
     public void printBaselines(List<Baseline> baselines, PrintStream ps) {
         if (baselines != null) {
             ps.println("[" + Config.nameShort + "] Retrieved " + baselines.size() + " baseline" + ( baselines.size() == 1 ? "" : "s" ) + ":");
             if (!(baselines.size() > 8)) {
                 for (Baseline b : baselines) {
                     ps.println("[" + Config.nameShort + "] + " + b.getShortname() + "(" + b.getDate() + ")");
                 }
             } else {
                 int i = baselines.size();
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(0).getShortname() + "(" + baselines.get(0).getDate() + ")");
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(1).getShortname() + "(" + baselines.get(1).getDate() + ")");
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(2).getShortname() + "(" + baselines.get(2).getDate() + ")");
                 ps.println("[" + Config.nameShort + "]   ...");
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(i - 3).getShortname() + "(" + baselines.get(i-3).getDate() + ")");
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(i - 2).getShortname() + "(" + baselines.get(i-2).getDate() + ")");
                 ps.println("[" + Config.nameShort + "] + " + baselines.get(i - 1).getShortname() + "(" + baselines.get(i-1).getDate() + ")");
             }
         }
     }
 
     /*
      * The following getters and booleans (six in all) are used to display saved
      * userdata in Hudsons gui
      */
     public boolean getMultiSite() {
         return this.multiSite;
     }
 
     public String getLevelToPoll() {
         return levelToPoll;
     }
 
     public String getComponent() {
         return component;
     }
 
     public String getStream() {
         return stream;
     }
 
     public String getLoadModule() {
         return loadModule;
     }
 
     public boolean isNewest() {
         return newest;
     }
 
     /*
      * getStreamObject() and getBaseline() are used by CCUCMNotifier to get the
      * Baseline and Stream in use, but does not work with concurrent builds!!!
      */
     public Stream getStreamObject() {
         return integrationstream;
     }
 
     @Exported
     public String getBaseline() {
         return bl;
     }
 
     @Exported
     public String getPolling() {
         return polling.toString();
     }
     
     @Exported
     public String getTreatUnstable() {
         return treatUnstable.toString();
     }
 
     @Exported
     public boolean doPostbuild() {
         return doPostBuild;
     }
 
     public String getBuildProject() {
         return buildProject;
     }
     
     
     
     
     public boolean isCreateBaseline() {
         return this.createBaseline;
     }
     
     public String getVersionFrom() {
     	return this.versionFrom;
     }
     
     public String getBuildnumberMajor() {
     	return this.buildnumberMajor;
     }
     
     public String getBuildnumberMinor() {
     	return this.buildnumberMinor;
     }
     
     public String getBuildnumberPatch() {
     	return this.buildnumberPatch;
     }
     
     public String getBuildnumberSequenceSelector() {
     	return this.buildnumberSequenceSelector;
     }
 
     
     
 
     /**
      * This class is used to describe the plugin to Hudson
      *
      * @author Troels Selch
      * @author Margit Bennetzen
      *
      */
     @Extension
     public static class CCUCMScmDescriptor extends SCMDescriptor<CCUCMScm> implements hudson.model.ModelObject {
 
         private String cleartool;
         private String multiSiteFrequency;
         private List<String> loadModules;
 
         public CCUCMScmDescriptor() {
             super(CCUCMScm.class, null);
             loadModules = getLoadModules();
             load();
             Config.setContext();
         }
 
         /**
          * This method is called, when the user saves the global Hudson
          * configuration.
          */
         @Override
         public boolean configure(org.kohsuke.stapler.StaplerRequest req, JSONObject json) throws FormException {
             /* For backwards compatibility, check if parameters are null */
 
             cleartool = json.getString("CCUCM.cleartool");
             if (cleartool != null) {
                 cleartool = cleartool.trim();
             }
 
             multiSiteFrequency = json.getString("CCUCM.multiSiteFrequency");
             if (multiSiteFrequency != null) {
                 multiSiteFrequency = multiSiteFrequency.trim();
             }
 
             save();
             return true;
         }
 
         /**
          * This is called by Hudson to discover the plugin name
          */
         @Override
         public String getDisplayName() {
             return "ClearCase UCM";
         }
 
         /**
          * This method is called by the scm/CCUCM/global.jelly to validate the
          * input without reloading the global configuration page
          *
          * @param value
          * @return
          */
         public FormValidation doExecutableCheck(@QueryParameter String value) {
             return FormValidation.validateExecutable(value);
         }
 
         /**
          * Called by Hudson. If the user does not input a command for Hudson to
          * use when polling, default value is returned
          *
          * @return
          */
         public String getCleartool() {
             if (cleartool == null || cleartool.equals("")) {
                 return "cleartool";
             }
             return cleartool;
         }
 
         public String getMultiSiteFrequency() {
             return multiSiteFrequency;
         }
 
         public int getMultiSiteFrequencyAsInt() {
             try {
                 return Integer.parseInt(multiSiteFrequency);
             } catch (Exception e) {
                 return 0;
             }
         }
 
         /**
          * Used by Hudson to display a list of valid promotion levels to build
          * from. The list of promotion levels is hard coded in
          * net.praqma.hudson.Config.java
          *
          * @return
          */
         public List<String> getLevels() {
             return Config.getLevels();
         }
 
         /**
          * Used by Hudson to display a list of loadModules (whether to poll all
          * or only modifiable elements
          *
          * @return
          */
         public List<String> getLoadModules() {
             loadModules = new ArrayList<String>();
             loadModules.add("All");
             loadModules.add("Modifiable");
             return loadModules;
         }
     }
 }
